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
package org.openhab.binding.intergas_gateway.internal;

import static org.openhab.binding.intergas_gateway.internal.IntergasGatewayBindingConstants.CHANNEL_SET_POINT;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.naming.CommunicationException;

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
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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

    protected ScheduledExecutorService executorService = this.scheduler;
    private @Nullable ScheduledFuture<?> dataPollingJob;

    private boolean heaterOn = false;

    private double lastSetpoint = 0.0;
    private int dailyTotalPower = 0;

    protected String gatewayURL = "";
    protected String xtendURL = "";

    protected final AtomicLong lifecycleGeneration = new AtomicLong();

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
                // updateStatus(ThingStatus.OFFLINE, String.format("Unable to query device data: %s", e.getMessage()));
                updateStatus(ThingStatus.OFFLINE);
            }
            if (dataResult != null) {
                GatewayPayload payload = gson.fromJson(dataResult, GatewayPayload.class);
                if (payload != null) {
                    updateSetpointState(payload.getRoomSetPointTemperature());
                }
            }
            updateSetpointState(setPoint);
        }
    }

    protected final void updateStatusIfCurrent(long generation, ThingStatus status) {
        synchronized (this) {
            if (generation == lifecycleGeneration.get()) {
                updateStatus(status);
            }
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
        long generation;
        synchronized (this) {
            generation = lifecycleGeneration.incrementAndGet();
        }
        config = getConfigAs(IntergasGatewayConfiguration.class);
        if (configure()) {
            dataPollingJob = executorService.scheduleWithFixedDelay(() -> retrieveData(generation), 0,
                    config.refreshInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.intergasGateway.trim().isEmpty() || config.xtendGateway.trim().isEmpty()) {
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Missing ipAddress/host configuration");
            updateStatus(ThingStatus.OFFLINE);
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            gatewayURL = String.format("http://admin:lo9988qo@%s/protect/data.json?heater=0",
                    config.intergasGateway.trim());
            xtendURL = String.format("http://%s:72/xtend/xtend", config.xtendGateway.trim());
            return true;
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
            String xtendData = getXtendData();
            synchronized (this) {
                if (generation != lifecycleGeneration.get()) {
                    logger.debug("Generation mismatch, aborting data retrieval");
                    return false;
                }
                handleXtendData(xtendData);
                updateStatus(ThingStatus.ONLINE);
            }

            if (xtendData == null || xtendData.trim().isEmpty()) {
                String intergasData = getIntergasData();
                synchronized (this) {
                    if (generation != lifecycleGeneration.get()) {
                        logger.debug("Generation mismatch, aborting data retrieval");
                        return false;
                    }
                    handleIntergasData(intergasData);
                    updateStatus(ThingStatus.OFFLINE);
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

    public String getXtendData() throws IOException {
        return getContentFrom(xtendURL);
    }

    protected void handleXtendData(String data) throws JsonSyntaxException, CommunicationException {
        XtendPayload payload = gson.fromJson(data, XtendPayload.class);
        if (payload != null) {
            if (payload.getRoomTemperature() > 0) {
                if (dailyTotalPower == 0) {
                    dailyTotalPower = payload.getTotalPower();
                    logger.info("Resetting daily total power");
                }
                updateState(IntergasGatewayBindingConstants.CHANNEL_ROOM_TEMPERATURE,
                        new QuantityType<>(payload.getRoomTemperature(), SIUnits.CELSIUS));
                updateState(IntergasGatewayBindingConstants.CHANNEL_SET_POINT,
                        new QuantityType<>(payload.getSetpoint(), SIUnits.CELSIUS));

                updateState(IntergasGatewayBindingConstants.CHANNEL_TOTAL_ENERGIE,
                        new QuantityType<>(payload.getTotal(), Units.WATT));
                updateState(IntergasGatewayBindingConstants.CHANNEL_WARMTEPOMP_ENERGIE,
                        new QuantityType<>(payload.getHeatpump(), Units.WATT));
                updateState(IntergasGatewayBindingConstants.CHANNEL_WARMTEPOMP_POWER,
                        new QuantityType<>(payload.getPower(), Units.WATT));
                updateState(IntergasGatewayBindingConstants.CHANNEL_WARMTEPOMP_TOTAL_POWER,
                        new QuantityType<>(payload.getTotalPower(), Units.KILOWATT_HOUR));
                updateState(IntergasGatewayBindingConstants.CHANNEL_WARMTEPOMP_TOTAL_POWER_DAG,
                        new QuantityType<>(payload.getTotalPower() - dailyTotalPower, Units.KILOWATT_HOUR));
                updateState(IntergasGatewayBindingConstants.CHANNEL_CV_KETEL_ENERGIE,
                        new QuantityType<>(payload.getCv(), Units.WATT));
                updateState(IntergasGatewayBindingConstants.CHANNEL_COP, new DecimalType(payload.getCop()));
            }
        }
    }

    private String getIntergasData() throws IOException {
        return getContentFrom(gatewayURL);
    }

    protected void handleIntergasData(String data) throws JsonSyntaxException, CommunicationException {
        GatewayPayload gsPayload = gson.fromJson(data, GatewayPayload.class);
        if (gsPayload != null) {
            updateState(IntergasGatewayBindingConstants.CHANNEL_ROOM_TEMPERATURE,
                    new QuantityType<>(gsPayload.getRoomTemperature(), SIUnits.CELSIUS));
            updateState(IntergasGatewayBindingConstants.CHANNEL_SET_POINT,
                    new QuantityType<>(gsPayload.getRoomSetPointTemperature(), SIUnits.CELSIUS));
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
    }
}
