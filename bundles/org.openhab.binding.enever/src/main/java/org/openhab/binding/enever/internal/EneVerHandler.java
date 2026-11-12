/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.enever.internal;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.naming.CommunicationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enever.internal.payloads.EneVerPayload;
import org.openhab.binding.enever.internal.payloads.IPayload;
import org.openhab.binding.enever.internal.payloads.PayloadPriceItem;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EneVerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class EneVerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EneVerHandler.class);

    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private @Nullable EneVerConfiguration config;

    protected ScheduledExecutorService executorService = this.scheduler;
    private @Nullable ScheduledFuture<?> dataPollingJob;
    private @Nullable ScheduledFuture<?> gasPriceResetJob;

    private String token = "";

    private double treshold = 0;
    private double minMaxTreshold = 0;

    private EPrices ePrices = new EPrices(minMaxTreshold, treshold);

    private @Nullable PayloadPriceItem gasPrice = null;

    protected final AtomicLong lifecycleGeneration = new AtomicLong();

    public EneVerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        long generation;
        synchronized (this) {
            generation = lifecycleGeneration.incrementAndGet();
        }
        config = getConfigAs(EneVerConfiguration.class);

        if (configure()) {
            ePrices = new EPrices(minMaxTreshold, treshold);
            updateStatus(ThingStatus.UNKNOWN);
            retrieveData(generation);

            var now = LocalDateTime.now();
            int minutes = 0;
            if (now.getMinute() < 15) {
                minutes = 15;
            } else if (now.getMinute() < 30) {
                minutes = 30;
            } else if (now.getMinute() < 45) {
                minutes = 45;
            }
            long initialDelayInSecondsForData = Duration.between(now, now.withMinute(minutes).withSecond(0).withNano(0))
                    .toSeconds() + 1;

            dataPollingJob = executorService.scheduleWithFixedDelay(() -> retrieveData(generation),
                    initialDelayInSecondsForData, 15 * 60, TimeUnit.SECONDS);
            var nextResetTime = now.withHour(5).withMinute(50).withSecond(0).withNano(0);
            if (now.isAfter(nextResetTime)) {
                nextResetTime = nextResetTime.plusDays(1);
            }
            long initialDelayInMinutesForReset = Duration.between(now, nextResetTime).toMinutes();
            gasPriceResetJob = executorService.scheduleWithFixedDelay(() -> {
                synchronized (this) {
                    gasPrice = null;
                }
            }, initialDelayInMinutesForReset, 24 * 60, TimeUnit.MINUTES);
        }
    }

    protected final void updateStatusIfCurrent(long generation, ThingStatus status) {
        synchronized (this) {
            if (generation == lifecycleGeneration.get()) {
                updateStatus(status);
            }
        }
    }

    /**
     * The data polling loop
     *
     * @return true if data was retrieved and published for the current generation
     */
    protected boolean retrieveData(long generation) {
        var now = ZonedDateTime.now();
        if (generation != lifecycleGeneration.get()) {
            logger.debug("Generation mismatch, aborting data retrieval");
            return false;
        }

        try {
            String electricityData = "";
            if (!ePrices.hasPriceFor(now)) {
                electricityData = getElectricityData(true);
                synchronized (this) {
                    if (generation != lifecycleGeneration.get()) {
                        logger.debug("Generation mismatch, aborting data retrieval");
                        return false;
                    }
                    handleElectricityData(electricityData, true);
                    updateStatus(ThingStatus.ONLINE);
                }
            }
            if (!ePrices.hasPriceFor(now.plusHours(10))) {
                electricityData = getElectricityData(false);
                synchronized (this) {
                    if (generation != lifecycleGeneration.get()) {
                        logger.debug("Generation mismatch, aborting data retrieval");
                        return false;
                    }
                    handleElectricityData(electricityData, false);
                }
            }
            updateElectricityChannels();

            if (gasPrice == null) {
                String gasData = getGasData();
                synchronized (this) {
                    if (generation != lifecycleGeneration.get()) {
                        logger.debug("Generation mismatch, aborting data retrieval");
                        return false;
                    }
                    handleGasData(gasData);
                }
            }

            return true;
        } catch (JsonSyntaxException | IOException | CommunicationException ex) {
            updateStatusIfCurrent(generation, ThingStatus.OFFLINE);
            logger.debug("Unable to get data from the API", ex);
            return false;
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    @SuppressWarnings("null")
    private boolean configure() {
        if (config == null || config.token.trim().isEmpty()) {
            logger.error("No token configured for thing {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE);
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            token = config.token;
            treshold = (double) config.priceTreshold / 100;
            minMaxTreshold = (double) config.minMaxTreshold / 100;
            return true;
        }
    }

    /**
     * Get the content of the response from the given url. If the bearer token is invalid a SecurityException is thrown.
     * 
     * @param url
     * @return the content of the response from the given url
     * @throws IOException
     */
    public String getContentFrom(String url) throws IOException {
        logger.debug("Retrieving data from {}", url);
        return HttpUtil.executeUrl("GET", url, 30000);
    }

    public String getElectricityData(boolean forToday) throws IOException {
        String url = "https://enever.nl/apiv3/stroomprijs_vandaag.php?resolution=15&price=prijsZP&token=" + token;
        if (!forToday) {
            url = "https://enever.nl/apiv3/stroomprijs_morgen.php?resolution=15&price=prijsZP&token=" + token;
        }
        return getContentFrom(url);
    }

    protected void handleElectricityData(String data, boolean forToday)
            throws JsonSyntaxException, CommunicationException {
        IPayload payload = gson.fromJson(data, EneVerPayload.class);

        if (forToday && (payload == null || !payload.getStatus())) {
            throw new CommunicationException("Unable to parse data response from API");
        }

        if (payload != null && payload.getStatus()) {
            var prices = payload.getElectricityPrices().stream()
                    .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
            ePrices.addPrices(prices);
            if (forToday) {
                logger.info("Retrieved electricity for today");
            } else {
                logger.info("Retrieved electricity for tomorrow");
            }
        }
    }

    private String getGasData() throws IOException {
        String url = "https://enever.nl/apiv3/gasprijs_vandaag.php?token=" + token;
        return getContentFrom(url);
    }

    protected void handleGasData(String data) throws JsonSyntaxException, CommunicationException {
        IPayload payload = gson.fromJson(data, EneVerPayload.class);

        if (payload != null && payload.getStatus()) {
            gasPrice = payload.getGasPrices().stream().filter(price -> price.getDatum().isEqual(LocalDate.now()))
                    .findFirst().orElse(null);
            logger.info("Retrieved gas for today");

            if (gasPrice != null) {
                updateState(EneVerBindingConstants.CHANNEL_GAS_DAILY_PRICE, new DecimalType(gasPrice.getPrijs()));
            }
        }
    }

    private void updateElectricityChannels() {
        var now = ZonedDateTime.now();
        var prijs = ePrices.getPriceFor(now);
        if (prijs != null) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE, new DecimalType(prijs.getPrijs()));

            if (prijs.isGoedkoop) {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new StringType("cheap"));
            } else if (prijs.isDuur) {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new StringType("expensive"));
            } else {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new StringType("neutral"));
            }
        }
        prijs = ePrices.getPriceFor(now.plusMinutes(15));
        if (prijs != null) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE_PLUS_1,
                    new DecimalType(prijs.getPrijs()));
        }

        prijs = ePrices.getPriceFor(now.plusMinutes(30));
        if (prijs != null) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE_PLUS_2,
                    new DecimalType(prijs.getPrijs()));
        }
    }

    @Override
    public void dispose() {
        synchronized (this) {
            lifecycleGeneration.incrementAndGet();
        }
        var job = dataPollingJob;
        if (job != null) {
            job.cancel(true);
        }
        dataPollingJob = null;

        job = gasPriceResetJob;
        if (job != null) {
            job.cancel(true);
        }
        gasPriceResetJob = null;
    }
}
