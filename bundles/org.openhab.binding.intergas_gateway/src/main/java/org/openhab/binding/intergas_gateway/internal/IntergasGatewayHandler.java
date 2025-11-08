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
package org.openhab.binding.intergas_gateway.internal;

import static org.openhab.binding.intergas_gateway.internal.IntergasGatewayBindingConstants.CHANNEL_SET_POINT;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link IntergasGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class IntergasGatewayHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IntergasGatewayHandler.class);

    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private IntergasGatewayConfiguration config = new IntergasGatewayConfiguration();

    private @Nullable ScheduledFuture<?> pollingJob;

    private boolean heaterOn = false;

    private double lastSetpoint = 0.0;

    protected String gatewayURL = "";
    protected String xtendURL = "";

    public IntergasGatewayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(XtendActions.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_SET_POINT.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                return;
            }

            Double setPoint = (Double.valueOf(command.toFullString().split(" ")[0]) - 5) * 10;
            String dataResult = null;
            try {
                dataResult = HttpUtil.executeUrl("GET", gatewayURL + "&setpoint=" + setPoint + "&thermostat=0", 30000);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Unable to query device data: %s", e.getMessage()));
            }
            if (dataResult != null) {
                GatewayPayload payload = gson.fromJson(dataResult, GatewayPayload.class);
                if (payload != null) {
                    // updateSetpointState(payload.getRoomSetPointTemperature());
                }
            }
            updateSetpointState(setPoint);
        }
    }

    private void updateSetpointState(double temp) {
        if (temp < 40 && temp != lastSetpoint) {
            updateState(IntergasGatewayBindingConstants.CHANNEL_SET_POINT, new QuantityType<>(temp, SIUnits.CELSIUS));
            lastSetpoint = temp;
        }
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(IntergasGatewayConfiguration.class);
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
        if (config.intergasGateway.trim().isEmpty() || config.xtendGateway.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing ipAddress/host configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            gatewayURL = String.format("http://admin:lo9988qo@%s/protect/data.json?heater=0",
                    config.intergasGateway.trim());
            xtendURL = String.format("http://%s:72/xtend/xtend", config.xtendGateway.trim());
            return true;
        }
    }

    private @Nullable String executeUrl(String url) {
        @Nullable
        String dataResult = null;
        try {
            dataResult = HttpUtil.executeUrl("GET", url, 30000);

            if (dataResult == null || dataResult.trim().isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty data");
                return null;
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query device data: %s", e.getMessage()));
        }

        // GatewayPayload payload = gson.fromJson(dataResult, GatewayPayload.class);
        // if (payload == null) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Unable to parse data response from device");
        // return null;
        // }
        //
        // if (payload.getNodeNr() == 0) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Results from API are empty");
        // return null;
        // }
        return dataResult;
    }

    protected void pollData() {
        @Nullable
        String dataResult;

        // dataResult = executeUrl(gatewayURL);
        // if (dataResult == null) {
        // return;
        // }

        // GatewayPayload gsPayload = gson.fromJson(dataResult, GatewayPayload.class);
        // if (gsPayload != null) {
        // updateState(IntergasGatewayBindingConstants.CHANNEL_ROOM_TEMPERATURE,
        // new QuantityType<>(gsPayload.getRoomTemperature(), SIUnits.CELSIUS));
        // updateState(IntergasGatewayBindingConstants.CHANNEL_SET_POINT,
        // new QuantityType<>(gsPayload.getRoomSetPointTemperature(), SIUnits.CELSIUS));
        //
        // updateStatus(ThingStatus.ONLINE);
        // }

        dataResult = executeUrl(xtendURL);
        if (dataResult == null) {
            dataResult = "";
        }
        // logger.info("Data = " + dataResult);

        XtendPayload payload = gson.fromJson(dataResult, XtendPayload.class);
        if (payload != null) {
            updateStatus(ThingStatus.ONLINE);

            if (payload.getRoomTemperature() > 0) {
                updateState(IntergasGatewayBindingConstants.CHANNEL_ROOM_TEMPERATURE,
                        new QuantityType<>(payload.getRoomTemperature(), SIUnits.CELSIUS));
                updateState(IntergasGatewayBindingConstants.CHANNEL_SET_POINT,
                        new QuantityType<>(payload.getSetpoint(), SIUnits.CELSIUS));

                updateState(IntergasGatewayBindingConstants.CHANNEL_TOTAL_ENERGIE,
                        new QuantityType<>(payload.getTotal(), Units.WATT));
                updateState(IntergasGatewayBindingConstants.CHANNEL_WARMTEPOMP_ENERGIE,
                        new QuantityType<>(payload.getHeatpump(), Units.WATT));
                updateState(IntergasGatewayBindingConstants.CHANNEL_CV_KETEL_ENERGIE,
                        new QuantityType<>(payload.getCv(), Units.WATT));
                updateState(IntergasGatewayBindingConstants.CHANNEL_COP, new DecimalType(payload.getCop()));
            }
        } else {
            dataResult = executeUrl(gatewayURL);
            if (dataResult == null) {
                return;
            }
            updateStatus(ThingStatus.ONLINE);

            GatewayPayload gsPayload = gson.fromJson(dataResult, GatewayPayload.class);
            if (gsPayload != null) {
                updateState(IntergasGatewayBindingConstants.CHANNEL_ROOM_TEMPERATURE,
                        new QuantityType<>(gsPayload.getRoomTemperature(), SIUnits.CELSIUS));
                updateState(IntergasGatewayBindingConstants.CHANNEL_SET_POINT,
                        new QuantityType<>(gsPayload.getRoomSetPointTemperature(), SIUnits.CELSIUS));
            }
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
