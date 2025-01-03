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
package org.openhab.binding.intergas_gateway.internal;

import static org.openhab.binding.intergas_gateway.internal.Intergas_GatewayBindingConstants.CHANNEL_SET_POINT;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link Intergas_GatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class Intergas_GatewayHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(Intergas_GatewayHandler.class);

    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private @Nullable Intergas_GatewayConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private boolean heaterOn = false;

    protected String apiURL = "";

    public Intergas_GatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                return;
            }

            Double setPoint = (Double.valueOf(command.toFullString().split(" ")[0]) - 5) * 10;
            // logger.debug("setPoint = " + setPoint);
            String dataResult = null;
            try {
                dataResult = HttpUtil.executeUrl("GET", apiURL + "&setpoint=" + setPoint + "&thermostat=0", 30000);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Unable to query device data: %s", e.getMessage()));
            }
            if (dataResult != null) {
                Payload payload = gson.fromJson(dataResult, Payload.class);
                if (payload != null) {
                    updateState(Intergas_GatewayBindingConstants.CHANNEL_SET_POINT,
                            new QuantityType<>(setPoint + 273.15, Units.KELVIN));
                }
            }
        }
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(Intergas_GatewayConfiguration.class);
        if (configure()) {
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.hostname.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing ipAddress/host configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            apiURL = String.format("http://admin:lo9988qo@%s/protect/data.json?heater=0", config.hostname.trim());
            return true;
        }
    }

    private @Nullable String executeUrl(String url) {
        @Nullable
        String dataResult = null;
        try {
            dataResult = HttpUtil.executeUrl("GET", apiURL, 30000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query device data: %s", e.getMessage()));
        }

        if (dataResult == null || dataResult.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty data");
            return null;
        }

        Payload payload = gson.fromJson(dataResult, Payload.class);
        if (payload == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to parse data response from device");
            return null;
        }

        if (payload.getNodeNr() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Results from API are empty");
            return null;
        }
        return dataResult;
    }

    protected void pollData() {
        @Nullable
        String dataResult;

        dataResult = executeUrl(apiURL);
        if (dataResult == null) {
            return;
        }

        Payload payload = gson.fromJson(dataResult, Payload.class);
        if (payload != null) {
            updateStatus(ThingStatus.ONLINE);
            updateState(Intergas_GatewayBindingConstants.CHANNEL_ROOM_TEMPERATURE,
                    new QuantityType<>(payload.getRoomTemperature() + 273.15, Units.KELVIN));
            updateState(Intergas_GatewayBindingConstants.CHANNEL_SET_POINT,
                    new QuantityType<>(payload.getRoomSetPointTemperature() + 273.15, Units.KELVIN));
            if (payload.getIo() == 10) {
                heaterOn = true;
            }
            if (payload.getIo() == 0) {
                heaterOn = false;
            }
            updateState(Intergas_GatewayBindingConstants.CHANNEL_HEATING, OnOffType.from(heaterOn));
        }
        // handleDataPayload(dataPayload);
    }

    @Override
    public void dispose() {
        var job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        pollingJob = null;
    }

    /**
     * The actual polling loop
     */
    protected void pollingCode() {
        pollData();
    }
}
