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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardSystemPayload {
    @SerializedName(value = "wifi_ssid")
    private String wifiSSID = "";
    @SerializedName(value = "wifi_rssi_db")
    private double wifiRSSI = 0;
    private boolean cloudEnabled = true;
    @SerializedName(value = "status_led_brightness_pct")
    private double statusLEDBrightness = 0;

    /**
     * Getter for the wifi SSID
     *
     * @return The wifi SSID obtained from the System API
     */
    public String getWifiSSID() {
        return wifiSSID;
    }

    /**
     * Getter for the wifi RSSI
     *
     * @return The wifi RSSI obtained from the System API
     */
    public int getWifiRSSI() {
        return (int) wifiRSSI;
    }

    /**
     * Getter for the cloud enabled
     *
     * @return The cloud enabled obtained from the System API
     */
    public boolean getCloudEnabled() {
        return cloudEnabled;
    }

    /**
     * Getter for the status LED brightness
     *
     * @return The status LED brightness obtained from the System API
     */
    public int getStatusLEDBrightness() {
        return (int) statusLEDBrightness;
    }

    @Override
    public String toString() {
        return String.format("""
                Data [wifiSSID: %s wifiRSSI: %s cloudEnabled: %s statusLEDBrightness: %s]
                """, wifiSSID, wifiRSSI, cloudEnabled, statusLEDBrightness);
    }
}
