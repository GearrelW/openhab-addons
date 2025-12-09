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
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
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

    private @Nullable ScheduledFuture<?> dailyJob;
    private @Nullable ScheduledFuture<?> hourlyJob;

    private String testDataE = "{\"status\":\"true\",\"data\":[{\"datum\":\"2025-12-09T00:00:00+01:00\",\"prijsZP\":\"0.251052\"},{\"datum\":\"2025-12-09T01:00:00+01:00\",\"prijsZP\":\"0.250399\"},{\"datum\":\"2025-12-09T02:00:00+01:00\",\"prijsZP\":\"0.246046\"},{\"datum\":\"2025-12-09T03:00:00+01:00\",\"prijsZP\":\"0.244025\"},{\"datum\":\"2025-12-09T04:00:00+01:00\",\"prijsZP\":\"0.237875\"},{\"datum\":\"2025-12-09T05:00:00+01:00\",\"prijsZP\":\"0.239660\"},{\"datum\":\"2025-12-09T06:00:00+01:00\",\"prijsZP\":\"0.234820\"},{\"datum\":\"2025-12-09T07:00:00+01:00\",\"prijsZP\":\"0.275960\"},{\"datum\":\"2025-12-09T08:00:00+01:00\",\"prijsZP\":\"0.296155\"},{\"datum\":\"2025-12-09T09:00:00+01:00\",\"prijsZP\":\"0.242788\"},{\"datum\":\"2025-12-09T10:00:00+01:00\",\"prijsZP\":\"0.254749\"},{\"datum\":\"2025-12-09T11:00:00+01:00\",\"prijsZP\":\"0.254183\"},{\"datum\":\"2025-12-09T12:00:00+01:00\",\"prijsZP\":\"0.250623\"},{\"datum\":\"2025-12-09T13:00:00+01:00\",\"prijsZP\":\"0.252643\"},{\"datum\":\"2025-12-09T14:00:00+01:00\",\"prijsZP\":\"0.261440\"},{\"datum\":\"2025-12-09T15:00:00+01:00\",\"prijsZP\":\"0.260436\"},{\"datum\":\"2025-12-09T16:00:00+01:00\",\"prijsZP\":\"0.303209\"},{\"datum\":\"2025-12-09T17:00:00+01:00\",\"prijsZP\":\"0.298418\"},{\"datum\":\"2025-12-09T18:00:00+01:00\",\"prijsZP\":\"0.289403\"},{\"datum\":\"2025-12-09T19:00:00+01:00\",\"prijsZP\":\"0.284433\"},{\"datum\":\"2025-12-09T20:00:00+01:00\",\"prijsZP\":\"0.276320\"},{\"datum\":\"2025-12-09T21:00:00+01:00\",\"prijsZP\":\"0.259247\"},{\"datum\":\"2025-12-09T22:00:00+01:00\",\"prijsZP\":\"0.253191\"},{\"datum\":\"2025-12-09T23:00:00+01:00\",\"prijsZP\":\"0.245710\"}],\"code\":\"5\"}";
    private String testDataG = "{\"status\":\"true\",\"data\":[{\"datum\":\"2024-09-24 06:00:00\",\"prijsEGSI\":\"0.350059\",\"prijsEOD\":\"0.354690\",\"prijsAA\":\"1.201611\",\"prijsAIP\":\"1.236701\",\"prijsANWB\":\"1.188121\",\"prijsBE\":\"1.204021\",\"prijsEE\":\"1.254199\",\"prijsEN\":\"1.208001\",\"prijsEVO\":\"1.188121\",\"prijsEZ\":\"1.189011\",\"prijsFR\":\"1.214475\",\"prijsGSL\":\"1.188121\",\"prijsMDE\":\"1.188121\",\"prijsNE\":\"1.188011\",\"prijsVDB\":\"1.235631\",\"prijsVON\":\"1.208911\",\"prijsWE\":\"1.213711\",\"prijsZG\":\"1.188121\",\"prijsZP\":\"1.209011\"}],\"code\":\"5\"}";
    private String testEPrijzen = "{\"status\": \"true\", \"data\": [{\"datum\": \"2025-02-13 00:00:00\", \"prijsZP\": \"0.31054\"}, {\"datum\": \"2025-02-13 01:00:00\", \"prijsZP\": \"0.30634999999999996\"}, {\"datum\": \"2025-02-13 02:00:00\", \"prijsZP\": \"0.3115\"}, {\"datum\": \"2025-02-13 03:00:00\", \"prijsZP\": \"0.30469999999999997\"}, {\"datum\": \"2025-02-13 04:00:00\", \"prijsZP\": \"0.30723999999999996\"}, {\"datum\": \"2025-02-13 05:00:00\", \"prijsZP\": \"0.30621\"}, {\"datum\": \"2025-02-13 06:00:00\", \"prijsZP\": \"0.32237\"}, {\"datum\": \"2025-02-13 07:00:00\", \"prijsZP\": \"0.371\"}, {\"datum\": \"2025-02-13 08:00:00\", \"prijsZP\": \"0.40847999999999995\"}, {\"datum\": \"2025-02-13 09:00:00\", \"prijsZP\": \"0.40192\"}, {\"datum\": \"2025-02-13 10:00:00\", \"prijsZP\": \"0.38181\"}, {\"datum\": \"2025-02-13 11:00:00\", \"prijsZP\": \"0.36074\"}, {\"datum\": \"2025-02-13 12:00:00\", \"prijsZP\": \"0.34453\"}, {\"datum\": \"2025-02-13 13:00:00\", \"prijsZP\": \"0.34147\"}, {\"datum\": \"2025-02-13 14:00:00\", \"prijsZP\": \"0.33953\"}, {\"datum\": \"2025-02-13 15:00:00\", \"prijsZP\": \"0.3419\"}, {\"datum\": \"2025-02-13 16:00:00\", \"prijsZP\": \"0.35457\"}, {\"datum\": \"2025-02-13 17:00:00\", \"prijsZP\": \"0.3911\"}, {\"datum\": \"2025-02-13 18:00:00\", \"prijsZP\": \"0.38998\"}, {\"datum\": \"2025-02-13 19:00:00\", \"prijsZP\": \"0.38065\"}, {\"datum\": \"2025-02-13 20:00:00\", \"prijsZP\": \"0.35708999999999996\"}, {\"datum\": \"2025-02-13 21:00:00\", \"prijsZP\": \"0.33998999999999996\"}, {\"datum\": \"2025-02-13 22:00:00\", \"prijsZP\": \"0.32813\"}, {\"datum\": \"2025-02-13 23:00:00\", \"prijsZP\": \"0.31100999999999995\"}]}";

    private String token = "";

    private double treshold = 0;

    private boolean excludeNightlyHours = false;

    private Hashtable<LocalDate, Hashtable<Integer, Double>> ePrices = new Hashtable<>();
    private EPrices ePricesNew = new EPrices() {

    };
    private Hashtable<LocalDate, Hashtable<Integer, String>> ePricesStatus = new Hashtable<>();
    private Hashtable<LocalDate, List<Integer>> cheapHours = new Hashtable<>();
    private Hashtable<LocalDate, List<Integer>> expensiveHours = new Hashtable<>();
    private Hashtable<LocalDate, Integer> peakHour = new Hashtable<>();

    private Hashtable<LocalDate, Hashtable<Integer, Double>> cheapPrices = new Hashtable<>();
    private Hashtable<LocalDate, Hashtable<Integer, Double>> expensivePrices = new Hashtable<>();

    private double gasPrice = 0;

    private int numberOfHours = 0;

    private int numberOfHoursBeforeWarning = 0;
    private double warningTreshold = 0;

    private boolean debug = true;

    public EneVerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // None
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(EneVerConfiguration.class);
        if (configure()) {

            // get prices for today
            if (retrieveElectricityPrices(LocalDate.now()) && retrieveGasPrice()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
            var now = LocalDateTime.now();
            retrieveElectricityPrices(LocalDate.now().plusDays(1));

            // update channels
            determineCheapAndExpensiveHours();
            updateDailyChannels();
            updateHourlyChannels();

            ePricesNew.processPrices(treshold, numberOfHours);

            logger.error("prices : " + ePricesNew.getPrices().toString());
            logger.error("averagesNew : " + ePricesNew.averagePrices.toString());
            logger.error("cheap : " + cheapPrices.toString());
            logger.error("expensive : " + expensivePrices.toString());
            logger.error("status : " + ePricesStatus.toString());

            // schedule get prices next day
            long nextDailyScheduleInNanos = Duration
                    .between(now, now.withHour(20).withMinute(10).withSecond(0).withNano(0)).toNanos();
            dailyJob = scheduler.scheduleWithFixedDelay(this::scheduleDailyPrices, nextDailyScheduleInNanos,
                    TimeUnit.HOURS.toNanos(12), TimeUnit.NANOSECONDS);

            // schedule update channels hourly
            long nextHourlyScheduleInNanos = Duration
                    .between(now, now.plusHours(1).withMinute(0).withSecond(0).withNano(0)).toNanos();
            hourlyJob = scheduler.scheduleWithFixedDelay(this::scheduleHourlyPrices, nextHourlyScheduleInNanos,
                    TimeUnit.HOURS.toNanos(1), TimeUnit.NANOSECONDS);
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing token configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            token = config.token;
            numberOfHours = config.numberOfHours;
            numberOfHoursBeforeWarning = config.numberOfHoursBeforeWarning;
            warningTreshold = (double) config.warningTreshold / 100;
            debug = config.debug;
            excludeNightlyHours = config.excludeNightlyHours;
            treshold = (double) config.priceTreshold / 100;
            return true;
        }
    }

    private boolean retrieveElectricityPrices(LocalDate date) {

        if (ePricesNew.containsDate(date) && ePricesNew.averagePrices.containsKey(date)) {
            return true;
        }

        logger.info("Retrieving prices for " + date);
        var today = LocalDate.now();

        String url = "https://enever.nl/api/stroomprijs_vandaag.php?token=" + token;
        if (date.isAfter(today)) {
            url = "https://enever.nl/api/stroomprijs_morgen.php?token=" + token;
        }

        Payload payload = null;
        if (!debug) {
            payload = retrievePayload(url);
        } else {
            logger.info("Using test electricity data");
            payload = gson.fromJson(testDataE, Payload.class);
        }

        if (payload == null) {
            logger.info("Nothing to retrieve");
            return false;
        }

        if (payload.getStatus() && (payload.getDate().isEqual(date) || debug)) {
            ePrices.put(date, new Hashtable<Integer, Double>());
            // averagePrices.put(date, 0.0);

            payload.getPrices().forEach((price) -> {
                ePricesNew.addPrice(price.getDatumTijd(), price.getPrijs());
                // ePrices.get(date).put(price.getDatumTijd().getHour(), price.getPrijs());

                // averagePrices.put(date, averagePrices.get(date) + price.getPrijs());

            });

            // averagePrices.put(date, averagePrices.get(date) / ePrices.get(date).size());

            var yesterday = today.minusDays(1);
            ePrices.remove(yesterday);
            // averagePrices.remove(yesterday);

            logger.info("Retrieved for " + date);
        }

        return payload.getStatus();
    }

    private boolean retrieveGasPrice() {
        String url = "https://enever.nl/api/gasprijs_vandaag.php?token=" + token;

        Payload p = null;
        if (!debug) {
            p = retrievePayload(url);
        } else {
            logger.debug("Using test gas data");
            p = gson.fromJson(testDataG, Payload.class);
        }

        if (p == null) {
            return false;
        }
        if (p.getStatus()) {
            gasPrice = 0;
        }

        p.getPrices().forEach((price) -> {
            gasPrice = price.getPrijs();
        });

        return p.getStatus();
    }

    private @Nullable Payload retrievePayload(String url) {
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

        Payload payload = null;
        try {
            payload = gson.fromJson(dataResult, Payload.class);
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
        var datum = LocalDate.now();

        updateState(EneVerBindingConstants.CHANNEL_GAS_DAILY_PRICE, new DecimalType(gasPrice));
        // updateState(EneVerBindingConstants.CHANNEL_PEAK_HOUR, new DecimalType(peakHour.get(datum)));

        // if (averagePrices.containsKey(datum)) {
        // updateState(EneVerBindingConstants.CHANNEL_AVG_ELECTRICITY_PRICE,
        // new DecimalType(averagePrices.get(datum)));
        // }
    }

    private void updateHourlyChannels() {
        var now = LocalDateTime.now();
        var datum = now.toLocalDate();
        var hour = now.getHour();
        logger.debug("updating channels for " + now);
        var prijs = ePricesNew.getPriceFor(datum, hour);
        if (prijs != null) {

            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE, new DecimalType(prijs.getPrijs()));

            // if (ePrices.containsKey(now.plusHours(numberOfHoursBeforeWarning).toLocalDate())) {
            // var warn = ePrices.get(datum).get(hour) * (1 + warningTreshold) < ePrices
            // .get(now.plusHours(numberOfHoursBeforeWarning).toLocalDate()).get(hour);
            // updateState(EneVerBindingConstants.CHANNEL_PRICE_WARNING, OnOffType.from(warn));
            // } else {
            // updateState(EneVerBindingConstants.CHANNEL_PRICE_WARNING, OnOffType.from(false));
            // }
        }

        // if (cheapHours.containsKey(datum) && cheapHours.get(datum).contains(hour)) {
        // updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(1));
        // } else if (expensiveHours.containsKey(datum) && expensiveHours.get(datum).contains(hour)) {
        // updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(-1));
        // } else {
        // updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(0));
        // }
    }

    protected void scheduleDailyPrices() {
        var today = LocalDate.now();
        retrieveElectricityPrices(today);
        retrieveElectricityPrices(today.plusDays(1));
        retrieveGasPrice();
        determineCheapAndExpensiveHours();
        updateDailyChannels();
    }

    protected void scheduleHourlyPrices() {
        updateHourlyChannels();
    }

    private void determineCheapAndExpensiveHours() {
        cheapHours.clear();
        expensiveHours.clear();

        // ePrices.forEach((datum, prijzen) -> {
        // cheapHours.put(datum, new ArrayList<Integer>());
        // expensiveHours.put(datum, new ArrayList<Integer>());

        // prijzen.entrySet().stream().filter(e -> !excludeNightlyHours || e.getKey() > 5)
        // .filter(e -> e.getValue() <= averagePrices.get(datum) * (1 - treshold))
        // .sorted(Map.Entry.comparingByValue()).limit(numberOfHours)
        // .forEach(e -> cheapHours.get(datum).add(e.getKey()));

        // peakHour.put(datum, prijzen.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey());

        // prijzen.entrySet().stream().filter(e -> !excludeNightlyHours || e.getKey() > 5)
        // .filter(e -> e.getValue() >= averagePrices.get(datum) * (1 + treshold))
        // .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(numberOfHours)
        // .forEach(e -> expensiveHours.get(datum).add(e.getKey()));
        // });

        cheapPrices.clear();
        expensivePrices.clear();

        // ePrices.forEach((datum, prijzen) -> {
        // ePricesStatus.put(datum, new Hashtable<>());
        // cheapPrices.put(datum, new Hashtable<>());
        // expensivePrices.put(datum, new Hashtable<>());

        // prijzen.entrySet().stream().filter(e -> e.getValue() <= averagePrices.get(datum))
        // // .sorted(Map.Entry.comparingByValue())
        // .forEach(e -> {
        // cheapPrices.get(datum).put(e.getKey(), e.getValue());
        // ePricesStatus.get(datum).put(e.getKey(), "zero_charge");
        // });

        // prijzen.entrySet().stream().filter(e -> e.getValue() > averagePrices.get(datum))
        // // .sorted(Map.Entry.comparingByValue())
        // .forEach(e -> {
        // expensivePrices.get(datum).put(e.getKey(), e.getValue());
        // ePricesStatus.get(datum).put(e.getKey(), "zero_discharge");
        // });

        // });

        // expensivePrices.entrySet().stream().sorted(Map.Entry.comparingByValue());
        logger.error("prices : " + ePricesNew.getPrices().toString());
        logger.error("cheap : " + cheapPrices.toString());
        logger.error("expensive : " + expensivePrices.toString());
        logger.error("status : " + ePricesStatus.toString());
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
    }
}
