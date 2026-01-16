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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enever.internal.payloads.EneVerPayload;
import org.openhab.binding.enever.internal.payloads.IPayload;
import org.openhab.binding.enever.internal.payloads.PayloadPriceItem;
import org.openhab.binding.enever.internal.payloads.ZonneplanPayload;
import org.openhab.core.io.net.http.HttpUtil;
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
    private @Nullable ScheduledFuture<?> dailyJob;
    private @Nullable ScheduledFuture<?> hourlyJob;

    private String testDataE = "{\"status\":\"true\",\"data\":[{\"datum\":\"2025-12-15T00:00:00+01:00\",\"prijsZP\":\"0.251052\"},{\"datum\":\"2025-12-15T01:00:00+01:00\",\"prijsZP\":\"0.250399\"},{\"datum\":\"2025-12-15T02:00:00+01:00\",\"prijsZP\":\"0.246046\"},{\"datum\":\"2025-12-15T03:00:00+01:00\",\"prijsZP\":\"0.244025\"},{\"datum\":\"2025-12-15T04:00:00+01:00\",\"prijsZP\":\"0.137875\"},{\"datum\":\"2025-12-15T05:00:00+01:00\",\"prijsZP\":\"0.239660\"},{\"datum\":\"2025-12-15T06:00:00+01:00\",\"prijsZP\":\"0.234820\"},{\"datum\":\"2025-12-15T07:00:00+01:00\",\"prijsZP\":\"0.175960\"},{\"datum\":\"2025-12-15T08:00:00+01:00\",\"prijsZP\":\"0.296155\"},{\"datum\":\"2025-12-15T09:00:00+01:00\",\"prijsZP\":\"0.112788\"},{\"datum\":\"2025-12-15T10:00:00+01:00\",\"prijsZP\":\"0.254749\"},{\"datum\":\"2025-12-15T11:00:00+01:00\",\"prijsZP\":\"0.254183\"},{\"datum\":\"2025-12-15T12:00:00+01:00\",\"prijsZP\":\"0.250623\"},{\"datum\":\"2025-12-15T13:00:00+01:00\",\"prijsZP\":\"0.252643\"},{\"datum\":\"2025-12-15T14:00:00+01:00\",\"prijsZP\":\"0.261440\"},{\"datum\":\"2025-12-15T15:00:00+01:00\",\"prijsZP\":\"0.260436\"},{\"datum\":\"2025-12-15T16:00:00+01:00\",\"prijsZP\":\"0.503209\"},{\"datum\":\"2025-12-15T17:00:00+01:00\",\"prijsZP\":\"0.598418\"},{\"datum\":\"2025-12-15T18:00:00+01:00\",\"prijsZP\":\"0.289403\"},{\"datum\":\"2025-12-15T19:00:00+01:00\",\"prijsZP\":\"0.284433\"},{\"datum\":\"2025-12-15T20:00:00+01:00\",\"prijsZP\":\"0.576320\"},{\"datum\":\"2025-12-15T21:00:00+01:00\",\"prijsZP\":\"0.259247\"},{\"datum\":\"2025-12-15T22:00:00+01:00\",\"prijsZP\":\"0.253191\"},{\"datum\":\"2025-12-15T23:00:00+01:00\",\"prijsZP\":\"0.245710\"}],\"code\":\"5\"}";
    private String testDataG = "{\"status\":\"true\",\"data\":[{\"datum\":\"2024-09-24 06:00:00\",\"prijsEGSI\":\"0.350059\",\"prijsEOD\":\"0.354690\",\"prijsAA\":\"1.201611\",\"prijsAIP\":\"1.236701\",\"prijsANWB\":\"1.188121\",\"prijsBE\":\"1.204021\",\"prijsEE\":\"1.254199\",\"prijsEN\":\"1.208001\",\"prijsEVO\":\"1.188121\",\"prijsEZ\":\"1.189011\",\"prijsFR\":\"1.214475\",\"prijsGSL\":\"1.188121\",\"prijsMDE\":\"1.188121\",\"prijsNE\":\"1.188011\",\"prijsVDB\":\"1.235631\",\"prijsVON\":\"1.208911\",\"prijsWE\":\"1.213711\",\"prijsZG\":\"1.188121\",\"prijsZP\":\"1.209011\"}],\"code\":\"5\"}";

    private String token = "";

    private String controlStrategy = EPrices.SOLAR_CONTROL;

    private double treshold = 0;
    private double minMaxTreshold = 0;

    private int numberOfHours = 0;

    private EPrices ePrices = new EPrices(controlStrategy, minMaxTreshold, treshold, numberOfHours);

    private @Nullable PayloadPriceItem gasPrice = new PayloadPriceItem();

    private boolean debug = true;

    public EneVerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_STRATEGY)) {
            if (command.toString().equals(EPrices.SOLAR_CONTROL) || command.toString().equals(EPrices.PRICES_CONTROL)) {
                controlStrategy = command.toString();
                ePrices.controlStrategy = controlStrategy;
                ePrices.setModes(LocalDateTime.now(), controlStrategy);

                var prijs = ePrices.getPriceFor(LocalDateTime.now());
                updateState(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_MODE, new StringType(prijs.getMode()));
            }
        }
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(EneVerConfiguration.class);
        if (configure()) {
            ePrices = new EPrices(controlStrategy, minMaxTreshold, treshold, numberOfHours);

            // get prices for today
            if (retrieveElectricityPrices() && retrieveGasPrice()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            var now = LocalDateTime.now();
            if (now.getHour() >= 20) {
                retrieveElectricityPrices();
            }

            // update channels
            updateGasChannels();
            updateDailyChannels();
            updateHourlyChannels();

            // schedule update channels hourly
            long nextHourlyScheduleInNanos = Duration
                    .between(now, now.plusHours(1).withMinute(0).withSecond(0).withNano(0)).toNanos();
            hourlyJob = scheduler.scheduleWithFixedDelay(this::updateHourlyChannels, nextHourlyScheduleInNanos,
                    TimeUnit.HOURS.toNanos(1), TimeUnit.NANOSECONDS);

            // schedule get electricity prices next day
            long nextDayScheduleInNanos = Duration
                    .between(now, now.withHour(21).withMinute(55).withSecond(0).withNano(0)).toNanos();
            nextDayJob = scheduler.scheduleWithFixedDelay(this::retrieveElectricityPrices, nextDayScheduleInNanos,
                    TimeUnit.DAYS.toNanos(1), TimeUnit.NANOSECONDS);

            // schedule update channels daily
            long nextDailyScheduleInNanos = Duration
                    .between(now, now.plusDays(1).withHour(0).withMinute(5).withSecond(0).withNano(0)).toNanos();
            dailyJob = scheduler.scheduleWithFixedDelay(this::updateDailyChannels, nextDailyScheduleInNanos,
                    TimeUnit.DAYS.toNanos(1), TimeUnit.NANOSECONDS);

            // schedule update gas channels
            long nextGasScheduleInNanos = Duration
                    .between(now, now.plusDays(1).withHour(6).withMinute(45).withSecond(0).withNano(0)).toNanos();
            gasJob = scheduler.scheduleWithFixedDelay(this::scheduleGasPrice, nextGasScheduleInNanos,
                    TimeUnit.DAYS.toNanos(1), TimeUnit.NANOSECONDS);
        }
    }

    protected void scheduleGasPrice() {
        retrieveGasPrice();
        updateGasChannels();
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
            numberOfHours = config.numberOfHours;
            debug = config.debug;
            treshold = (double) config.priceTreshold / 100;
            minMaxTreshold = (double) config.minMaxTreshold / 100;
            return true;
        }
    }

    private boolean retrieveElectricityPrices() {
        var date = LocalDate.now();

        if (ePrices.containsDate(date) && ePrices.averagePrices.containsKey(date)) {
            date = date.plusDays(1);
            if (ePrices.containsDate(date) && ePrices.averagePrices.containsKey(date)) {
                return true;
            }
        }

        logger.info("Retrieving prices for " + date);

        String url = "https://enever.nl/apiv3/stroomprijs_vandaag.php?token=" + token;
        if (date.isAfter(LocalDate.now())) {
            url = "https://enever.nl/apiv3/stroomprijs_morgen.php?token=" + token;
        }

        IPayload payload = null;
        if (!debug) {
            payload = retrievePayload(url, true);
        } else {
            logger.info("Using backup electricity data");
            return retrieveBackupPrices(date);
        }

        if (payload == null) {
            logger.info("Retrieving backup prices for " + date);
            return retrieveBackupPrices(date);
        }

        if (payload.getStatus() || debug) {
            var prices = payload.getElectricityPrices().stream()
                    .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
            ePrices.addPrices(prices);
            ePrices.processPrices();

            updateState(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_STRATEGY,
                    new StringType(ePrices.controlStrategy));

            logger.info("Retrieved for " + date);
        }

        return payload.getStatus();
    }

    private boolean retrieveBackupPrices(LocalDate date) {
        String url = "https://www.zonneplan.nl/_next/data/-1MTIpwQhw6uPMo8SUryh/energie/dynamisch-energiecontract.json?slug=energie&slug=dynamisch-energiecontract";

        IPayload payload = null;
        payload = retrievePayload(url, false);

        if (payload == null) {
            return false;
        }

        if (payload.getStatus()) {
            var prices = payload.getElectricityPrices().stream()
                    .collect(Collectors.toMap(PayloadPriceItem::getDatumTijd, PayloadPriceItem::getPrijs));
            ePrices.addPrices(prices);
            ePrices.processPrices();
            gasPrice = payload.getGasPrices().stream().filter(p -> p.getDatum().isEqual(LocalDate.now())).findFirst()
                    .orElse(gasPrice);

            logger.info("Retrieved for " + date);
        }

        return payload.getStatus();
    }

    private boolean retrieveGasPrice() {
        if (gasPrice.getDatum().isEqual(LocalDate.now())) {
            return true;
        }
        String url = "https://enever.nl/apiv3/gasprijs_vandaag.php?token=" + token;

        IPayload p = null;
        if (!debug) {
            p = retrievePayload(url, true);
        } else {
            logger.debug("Using test gas data");
            p = gson.fromJson(testDataG, EneVerPayload.class);
        }

        if (p == null) {
            return retrieveBackupPrices(LocalDate.now());
        }

        p.getGasPrices().stream().filter(price -> price.getDatum().isEqual(LocalDate.now())).findFirst()
                .ifPresent(price -> {
                    gasPrice = price;
                });

        return p.getStatus();
    }

    private @Nullable IPayload retrievePayload(String url, boolean primary) {
        @Nullable
        String dataResult = null;
        try {
            dataResult = HttpUtil.executeUrl("GET", url, 30000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query device data: %s", e.getMessage()));
        }

        if (dataResult == null || dataResult.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty data");
            return null;
        }
        IPayload payload = null;
        if (primary) {
            try {
                payload = gson.fromJson(dataResult, EneVerPayload.class);
            } catch (JsonSyntaxException ex) {
                logger.debug(dataResult);
            }
        } else {
            payload = new ZonneplanPayload(dataResult);
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

    private void updateGasChannels() {
        if (!gasPrice.getDatum().isEqual(LocalDate.now())) {
            return;
        }
        updateState(EneVerBindingConstants.CHANNEL_GAS_DAILY_PRICE, new DecimalType(gasPrice.getPrijs()));
    }

    private void updateDailyChannels() {
        var now = LocalDate.now();

        var maxPrice = ePrices.getMaxPrice(now);
        if (maxPrice != null) {
            updateState(EneVerBindingConstants.CHANNEL_PEAK_HOUR, new DecimalType(maxPrice.getUur()));
        }

        var average = ePrices.averagePrices.get(now);
        if (average != null) {
            updateState(EneVerBindingConstants.CHANNEL_AVG_ELECTRICITY_PRICE, new DecimalType(average));
        }
        updateState(EneVerBindingConstants.CHANNEL_BATTERY_CONTROL_STRATEGY, new StringType(ePrices.controlStrategy));
    }

    private void updateHourlyChannels() {
        var now = LocalDateTime.now();
        logger.debug("updating channels for " + now);
        var prijs = ePrices.getPriceFor(now);
        if (prijs == null) {
            retrieveElectricityPrices();
            prijs = ePrices.getPriceFor(now);
        }
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
        prijs = ePrices.getPriceFor(now.plusHours(1));
        if (prijs != null) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE_PLUS_1,
                    new DecimalType(prijs.getPrijs()));
        }

        prijs = ePrices.getPriceFor(now.plusHours(2));
        if (prijs != null) {
            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE_PLUS_2,
                    new DecimalType(prijs.getPrijs()));
        }
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
