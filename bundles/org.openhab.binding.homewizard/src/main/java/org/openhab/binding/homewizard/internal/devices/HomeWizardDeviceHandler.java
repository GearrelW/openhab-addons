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
package org.openhab.binding.homewizard.internal.devices;

import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.homewizard.internal.HomeWizardBindingConstants;
import org.openhab.binding.homewizard.internal.HomeWizardConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link HomeWizardDeviceHandler} is a base class for all
 * HomeWizard devices. It provides configuration and polling of
 * data from a device. It also processes common data.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - changes to API calls and support for APi v2 (beta).
 *
 */
@NonNullByDefault
public abstract class HomeWizardDeviceHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(HomeWizardDeviceHandler.class);
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    protected ScheduledExecutorService executorService = this.scheduler;
    protected HomeWizardConfiguration config = new HomeWizardConfiguration();
    private @Nullable ScheduledFuture<?> pollingJob;
    private HttpClient httpClient = new HttpClient();

    protected List<String> supportedTypes = new ArrayList<String>();
    protected String apiURL = "";

    /**
     * Constructor
     *
     * @param thing The thing to handle
     */
    public HomeWizardDeviceHandler(Thing thing) {
        super(thing);
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        config = getConfigAs(HomeWizardConfiguration.class);

        if (config.apiVersion > 1) {
            String caCertPath = "homewizard-ca-cert.pem";

            // Create an SSL context factory and set the CA certificate
            KeyStore keyStore = null;
            ClassLoader classloader = this.getClass().getClassLoader();
            if (classloader != null) {
                try {
                    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("caCert", CertificateFactory.getInstance("X.509")
                            .generateCertificate(classloader.getResourceAsStream(caCertPath)));
                } catch (Exception ex) {
                }

                SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
                sslContextFactory.setTrustStore(keyStore);
                sslContextFactory.setEndpointIdentificationAlgorithm(null);
                sslContextFactory.setHostnameVerifier((hostname, sslSession) -> true);
                httpClient = new HttpClient(sslContextFactory);
            }
        }

        if (configure() && processDeviceInformation()) {
            pollingJob = executorService.scheduleWithFixedDelay(this::pollingCode, 0, config.refreshDelay,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.ipAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing ipAddress/host configuration");
            return false;
        }
        if (config.apiVersion == 2 && config.bearerToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Missing bearer token");
            return false;
        }

        updateStatus(ThingStatus.UNKNOWN);
        if (config.apiVersion > 1) {
            apiURL = String.format("https://%s/api/", config.ipAddress.trim());
        } else {
            apiURL = String.format("http://%s/api/", config.ipAddress.trim());
        }

        try {
            httpClient.setConnectTimeout(30000);
            httpClient.start();
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to communicate with the device: %s", ex.getMessage()));
            return false;
        }

        return true;
    }

    @SuppressWarnings("null")
    private boolean processDeviceInformation() {
        final String deviceInformation;

        try {
            deviceInformation = getDeviceInformationData();
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query device: %s", ex.getMessage()));
            return false;
        }

        if (deviceInformation.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty data");
            return false;
        }

        var payload = gson.fromJson(deviceInformation, HomeWizardDeviceInformationPayload.class);

        if ("".equals(payload.getProductType())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty data");
            return false;
        }

        if (!supportedTypes.contains(payload.getProductType())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Device is not compatible with this thing type");
            return false;
        }

        updateProperty("productName", payload.getProductName());
        updateProperty("productType", payload.getProductType());
        updateProperty("firmwareVersion", payload.getFirmwareVersion());
        updateProperty("apiVersion", payload.getApiVersion());

        return true;
    }

    /**
     * dispose: stop the poller
     */
    @Override
    public void dispose() {
        var job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        pollingJob = null;
        try {
            httpClient.stop();
        } catch (Exception ex) {

        }
    }

    /**
     *
     * Updates the state of the thing.
     *
     * @param groupID id of the channel, which was updated
     * @param channelID id of the channel, which was updated
     * @param state new state
     */
    protected void updateState(String groupID, String channelID, State state) {
        updateState(groupID + "#" + channelID, state);
    }

    protected void handleSystemDataPayload(String data) {
        var payload = gson.fromJson(data, HomeWizardSystemPayload.class);
        if (payload != null) {

            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM, HomeWizardBindingConstants.CHANNEL_WIFI_SSID,
                    new StringType(payload.getWifiSSID()));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM, HomeWizardBindingConstants.CHANNEL_WIFI_RSSI,
                    new QuantityType<>(payload.getWifiRSSI(), Units.DECIBEL));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                    HomeWizardBindingConstants.CHANNEL_CLOUD_ENABLED, OnOffType.from(payload.getCloudEnabled()));
            updateState(HomeWizardBindingConstants.CHANNEL_GROUP_SYSTEM,
                    HomeWizardBindingConstants.CHANNEL_STATUS_LED_BRIGHTNESS,
                    new DecimalType(payload.getStatusLEDBrightness()));
        }
    }

    /**
     * Device specific handling of the returned data.
     *
     * @param payload The data obtained form the API call
     */
    protected abstract void handleMeasurementDataPayload(String data);

    protected ContentResponse getResponseFrom(String url)
            throws InterruptedException, TimeoutException, ExecutionException {
        var request = httpClient.newRequest(url);

        if (config.apiVersion > 1) {
            request = request.header("Authorization", "Bearer " + config.bearerToken);
            request = request.header("X-Api-Version", "" + config.apiVersion);
        }
        return request.send();
    }

    /**
     * @return json response from the device information api
     * @throws InterruptedException, TimeoutException, ExecutionException, SecurityException
     */
    public String getDeviceInformationData()
            throws InterruptedException, TimeoutException, ExecutionException, SecurityException {
        var response = getResponseFrom(apiURL);
        if (response.getStatus() == 401) {
            throw new SecurityException("Bearer token is invalid.");
        }
        return response.getContentAsString();
    }

    /**
     * @return json response from the measurement api
     * @throws InterruptedException, TimeoutException, ExecutionException
     */
    public String getMeasurementData() throws InterruptedException, TimeoutException, ExecutionException {
        var url = apiURL;
        if (config.apiVersion > 1) {
            url += "measurement";
        } else {
            url += "v1/data";
        }

        return getResponseFrom(url).getContentAsString();
    }

    /**
     * @return json response from the system api
     * @throws InterruptedException, TimeoutException, ExecutionException
     */
    public String getSystemData() throws InterruptedException, TimeoutException, ExecutionException {
        return getResponseFrom(apiURL + "system").getContentAsString();
    }

    protected void pollMeasurementData() {
        final String measurementData;

        try {
            measurementData = getMeasurementData();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Device is offline or doesn't support the API version"));
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        handleMeasurementDataPayload(measurementData);
    }

    protected void pollSystemData() {
        final String systemData;

        try {
            systemData = getSystemData();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Device is offline or doesn't support the API version"));
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        handleSystemDataPayload(systemData);
    }

    /**
     * The actual polling loop
     */
    protected void pollingCode() {
        pollMeasurementData();
        if (config.apiVersion > 1) {
            pollSystemData();
        }
    }
}
