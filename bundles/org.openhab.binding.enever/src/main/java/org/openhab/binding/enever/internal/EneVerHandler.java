/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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

    private String token = "";

    private @Nullable Prices prices = null;

    private double treshold = 0;

    private boolean excludeNightlyHours = false;

    private int numberOfHours = 0;

    private int numberOfHoursBeforeWarning = 0;
    private double warningTreshold = 0;

    private boolean debug = false;

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
            prices = new Prices(token);
            try {
                prices.refresh();
                updateStatus(ThingStatus.ONLINE);
            } catch (EneVerException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to parse data response from device");
                return;
            }

            // update channels
            updateDailyElectricityChannels();
            updateDailyGasChannels();
            updateHourlyChannels();

            var now = LocalDateTime.now();

            // schedule update daily channels
            long nextDailyElecScheduleInNanos = Duration
                    .between(now, now.plusDays(1).withHour(0).withMinute(0).withSecond(5).withNano(0)).toNanos();
            dailyJob = scheduler.scheduleWithFixedDelay(this::updateDailyElectricityChannels,
                    nextDailyElecScheduleInNanos, TimeUnit.DAYS.toNanos(1), TimeUnit.NANOSECONDS);

            long nextDailyGasScheduleInNanos = Duration
                    .between(now, now.plusDays(1).withHour(7).withMinute(0).withSecond(5).withNano(0)).toNanos();
            dailyJob = scheduler.scheduleWithFixedDelay(this::updateDailyGasChannels, nextDailyGasScheduleInNanos,
                    TimeUnit.DAYS.toNanos(1), TimeUnit.NANOSECONDS);

            // schedule update channels hourly
            long nextHourlyScheduleInNanos = Duration
                    .between(now, now.plusHours(1).withMinute(0).withSecond(0).withNano(0)).toNanos();
            hourlyJob = scheduler.scheduleWithFixedDelay(this::updateHourlyChannels, nextHourlyScheduleInNanos,
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

    private void updateDailyElectricityChannels() {
        if (prices != null) {
            updateState(EneVerBindingConstants.CHANNEL_AVG_ELECTRICITY_PRICE,
                    new DecimalType(prices.getAverageElectricityPrice()));
        }
    }

    private void updateDailyGasChannels() {
        if (prices != null) {
            updateState(EneVerBindingConstants.CHANNEL_GAS_DAILY_PRICE, new DecimalType(prices.getGasPrice()));
        }
    }

    @SuppressWarnings("null")
    private void updateHourlyChannels() {

        if (prices != null) {

            updateState(EneVerBindingConstants.CHANNEL_ELECTRICITY_HOURLY_PRICE,
                    new DecimalType(prices.getElectricityPrice()));

            var isCheap = prices.isElectricityCheap(treshold, numberOfHours, excludeNightlyHours);
            var isExpensive = prices.isElectricityExpensive(treshold, numberOfHours, excludeNightlyHours);

            if (isCheap) {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(1));
            } else if (isExpensive) {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(-1));
            } else {
                updateState(EneVerBindingConstants.CHANNEL_HOUR_INDICATION, new DecimalType(0));
            }

            var warn = prices.getElectricityPrice() * (1 + warningTreshold) < prices
                    .getElectricityPrice(numberOfHoursBeforeWarning);
            updateState(EneVerBindingConstants.CHANNEL_PRICE_WARNING, OnOffType.from(warn));
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
    }

}
