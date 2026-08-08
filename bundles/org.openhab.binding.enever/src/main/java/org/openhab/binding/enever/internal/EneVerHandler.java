/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.time.ZoneId;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enever.internal.payloads.EneVerPayload;
import org.openhab.binding.enever.internal.payloads.IPayload;
import org.openhab.binding.enever.internal.payloads.PayloadPriceItem;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    private @Nullable ScheduledFuture<?> gasJob;
    private @Nullable ScheduledFuture<?> nextDayJob;
    private @Nullable ScheduledFuture<?> electricityJob;
    private @Nullable ScheduledFuture<?> dailyJob;
    private @Nullable ScheduledFuture<?> hourlyJob;

    private String token = "";

    private String controlStrategy = EPrices.SOLAR_CONTROL;

    private double treshold = 0;
    private double minMaxTreshold = 0;

    private int numberOfChargingMoments = 0;

    private EPrices ePrices = new EPrices(minMaxTreshold, treshold, numberOfChargingMoments);

    private @Nullable PayloadPriceItem gasPrice = new PayloadPriceItem();

    private boolean debug = true;

    public EneVerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (channelUID.getId().equals(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_STRATEGY)) {
        // if (command.toString().equals(EPrices.SOLAR_CONTROL) || command.toString().equals(EPrices.PRICES_CONTROL)) {
        // controlStrategy = command.toString();
        // ePrices.controlStrategy = controlStrategy;
        // ePrices.setModes(controlStrategy);

        // var prijs = ePrices.getPriceFor(LocalDateTime.now());
        // updateState(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_MODE, new StringType(prijs.getMode()));
        // }
        // }
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(EneVerConfiguration.class);
        if (configure()) {
            ePrices = new EPrices(minMaxTreshold, treshold, numberOfChargingMoments);

            // get prices for today
            gasPrice = retrieveGasPrice();
            if (retrieveElectricityPrices() && gasPrice != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            var now = LocalDateTime.now();

            // update channels
            updateDailyChannels();
            updateHourlyChannels();

            // schedule update channels hourly
            long nextHourlyScheduleInNanos = Duration
                    .between(now, now.plusHours(1).withMinute(0).withSecond(0).withNano(0)).toNanos();
            hourlyJob = scheduler.scheduleWithFixedDelay(this::updateHourlyChannels, nextHourlyScheduleInNanos,
                    TimeUnit.HOURS.toNanos(1), TimeUnit.NANOSECONDS);

            // schedule update channels daily
            long nextDailyScheduleInNanos = Duration
                    .between(now, now.plusDays(1).withHour(0).withMinute(5).withSecond(0).withNano(0)).toNanos();
            dailyJob = scheduler.scheduleWithFixedDelay(this::updateDailyChannels, nextDailyScheduleInNanos,
                    TimeUnit.DAYS.toNanos(1), TimeUnit.NANOSECONDS);

            // schedule update channels electricity prices
            int minutes = 0;
            if (now.getMinute() < 15) {
                minutes = 15;
            } else if (now.getMinute() < 30) {
                minutes = 30;
            } else if (now.getHour() < 45) {
                minutes = 45;
            }

            long nextElectricityScheduleInNanos = Duration
                    .between(now, now.withMinute(minutes).withSecond(0).withNano(0)).toNanos();
            electricityJob = scheduler.scheduleWithFixedDelay(this::updateElectricityChannels,
                    nextElectricityScheduleInNanos, TimeUnit.MINUTES.toNanos(15), TimeUnit.NANOSECONDS);

            // schedule update gas channels
            long nextGasScheduleInNanos = Duration
                    .between(now, now.plusDays(1).withHour(6).withMinute(45).withSecond(0).withNano(0)).toNanos();
            gasJob = scheduler.scheduleWithFixedDelay(this::scheduleGasPrice, nextGasScheduleInNanos,
                    TimeUnit.DAYS.toNanos(1), TimeUnit.NANOSECONDS);
        }
    }

    protected void scheduleGasPrice() {
        gasPrice = retrieveGasPrice();
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    @SuppressWarnings("null")
    private boolean configure() {
        if (config == null || config.token.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing token configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            token = config.token;
            numberOfChargingMoments = config.numberOfChargingMoments;
            debug = config.debug;
            treshold = (double) config.priceTreshold / 100;
            minMaxTreshold = (double) config.minMaxTreshold / 100;
            return true;
        }
    }

    private boolean retrieveElectricityPrices() {
        var date = LocalDate.now();

        if (ePrices.containsDate(date)) {
            date = date.plusDays(1);
            if ((ePrices.containsDate(date)) || LocalDateTime.now().getHour() < 14) {
                return true;
            }
        }

        logger.info("Retrieving prices for " + date);

        String url = "https://enever.nl/apiv3/stroomprijs_vandaag.php?resolution=15&price=prijsZP&token=" + token;
        if (date.isAfter(LocalDate.now())) {
            url = "https://enever.nl/apiv3/stroomprijs_morgen.php?resolution=15&price=prijsZP&token=" + token;
        }

        IPayload payload = retrievePayload(url);

        if (payload == null) {
            return false;
        }

        if (payload.getStatus() || debug) {
            var prices = payload.getElectricityPrices().stream()
                    .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
            ePrices.addPrices(prices);

            updateState(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_STRATEGY,
                    new StringType(ePrices.getControlStrategy()));

            logger.info("Retrieved for " + date);
        }

        return payload.getStatus();
    }

    private @Nullable PayloadPriceItem retrieveGasPrice() {
        PayloadPriceItem gasPrijs = null;
        String url = "https://enever.nl/apiv3/gasprijs_vandaag.php?token=" + token;

        IPayload payload = retrievePayload(url);

        if (payload != null) {
            gasPrijs = payload.getGasPrices().stream().filter(price -> price.getDatum().isEqual(LocalDate.now()))
                    .findFirst().orElse(null);
        }

        return gasPrijs;
    }

    private @Nullable IPayload retrievePayload(String url) {
        @Nullable
        String dataResult = null;
        try {
            dataResult = HttpUtil.executeUrl("GET", url, 30000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Request failed: %s", e.getMessage()));
        }

        if (dataResult == null || dataResult.trim().isEmpty()) {
            return null;
        }
        IPayload payload = null;
        try {
            payload = gson.fromJson(dataResult, EneVerPayload.class);
        } catch (JsonSyntaxException ex) {
            logger.debug(dataResult);
        }

        if (payload == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to parse data response from " + url);
            return null;
        }

        if (!payload.getStatus()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Results from API are empty");
            return null;
        }
        return payload;
    }

    private void updateDailyChannels() {
        var today = LocalDate.now();

        var maxPrice = ePrices.getMaxPriceFor(today);
        if (maxPrice != null) {
            var ph = maxPrice.getDatumTijd().atZone(ZoneId.of("Europe/Amsterdam")).toInstant();
            updateState(EneVerBindingConstants.CHANNEL_PEAK_HOUR, new DateTimeType(ph));
        }

        var average = ePrices.getAveragePriceFor(today);
        if (average != null) {
            updateState(EneVerBindingConstants.CHANNEL_AVG_ELECTRICITY_PRICE, new DecimalType(average));
        }
        updateState(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_STRATEGY,
                new StringType(ePrices.getControlStrategy()));
    }

    private void updateElectricityChannels() {
        var now = LocalDateTime.now();
        var prijs = getElectriciteitPrijs(now);
        if (prijs != null) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE, new DecimalType(prijs.getPrijs()));

            if (prijs.isGoedkoop) {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new StringType("cheap"));
            } else if (prijs.isDuur) {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new StringType("expensive"));
            } else {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new StringType("neutral"));
            }

            updateState(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_MODE, new StringType(prijs.getMode()));
        }
        prijs = getElectriciteitPrijs(now.plusMinutes(15));
        if (prijs != null) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE_PLUS_1,
                    new DecimalType(prijs.getPrijs()));
        }

        prijs = getElectriciteitPrijs(now.plusMinutes(30));
        if (prijs != null) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE_PLUS_2,
                    new DecimalType(prijs.getPrijs()));
        }
    }

    private void updateHourlyChannels() {
        var now = LocalDateTime.now();
        logger.debug("updating channels for " + now);

        var gasPrijs = getGasPrijs();
        if (gasPrijs != 0) {
            updateState(EneVerBindingConstants.CHANNEL_GAS_DAILY_PRICE, new DecimalType(gasPrijs));
        }

        var chargeStart = ePrices.getPlan().getChargeStart(now);
        if (chargeStart != null) {
            var cs = chargeStart.atZone(ZoneId.of("Europe/Amsterdam")).toInstant();
            updateState(EneVerBindingConstants.CHANNEL_CHARGE_START, new DateTimeType(cs));
        }

        var chargeEnd = ePrices.getPlan().getChargeEnd(now);
        if (chargeEnd != null) {
            var ce = chargeEnd.atZone(ZoneId.of("Europe/Amsterdam")).toInstant();
            updateState(EneVerBindingConstants.CHANNEL_CHARGE_END, new DateTimeType(ce));
        }

        var dischargeStart = ePrices.getPlan().getDischargeStart(now);
        if (dischargeStart != null) {
            var ds = dischargeStart.atZone(ZoneId.of("Europe/Amsterdam")).toInstant();
            updateState(EneVerBindingConstants.CHANNEL_DISCHARGE_START, new DateTimeType(ds));
        }

        var dischargeEnd = ePrices.getPlan().getDischargeEnd(now);
        if (dischargeEnd != null) {
            var de = dischargeEnd.atZone(ZoneId.of("Europe/Amsterdam")).toInstant();
            updateState(EneVerBindingConstants.CHANNEL_DISCHARGE_END, new DateTimeType(de));
        }
    }

    private EPrice getElectriciteitPrijs(LocalDateTime now) {
        var prijs = ePrices.getPriceFor(now);
        if (prijs == null) {
            retrieveElectricityPrices();
            prijs = ePrices.getPriceFor(now);
        }
        return prijs;
    }

    private Double getGasPrijs() {
        if (gasPrice == null) {
            retrieveGasPrice();
        }
        return gasPrice.getPrijs();
    }

    @Override
    public void dispose() {
        var job = dailyJob;
        if (job != null) {
            job.cancel(true);
        }
        dailyJob = null;

        job = hourlyJob;
        if (job != null) {
            job.cancel(true);
        }
        hourlyJob = null;

        job = nextDayJob;
        if (job != null) {
            job.cancel(true);
        }
        nextDayJob = null;

        job = gasJob;
        if (job != null) {
            job.cancel(true);
        }
        gasJob = null;
    }
}
