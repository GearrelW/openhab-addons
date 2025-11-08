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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 *
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class XtendPayload {
    @SerializedName("79b3")
    private double roomTemperature = 0;

    @SerializedName("7921")
    private double roomSetpoint = 0;

    @SerializedName("5041")
    private double cop = 0;

    @SerializedName("5077")
    private double total = 0;

    @SerializedName("503e")
    private double heatpump = 0;

    @SerializedName("5088")
    private double cv = 0;

    public double getSetpoint() {
        return roomSetpoint / 100;
    }

    public double getTotal() {
        return total;
    }

    public double getHeatpump() {
        return heatpump;
    }

    public double getCv() {
        return cv;
    }

    public double getCop() {
        return cop / 10;
    }

    public double getRoomTemperature() {
        return roomTemperature / 100;
    }

    @Override
    public String toString() {
        return String.format("Data [roomTemperature: %d roomSetpoint: %d cop: %d total: %d heatpump: %d cv: %d ] ",
                roomTemperature, roomSetpoint, cop, total, heatpump, cv);
    }
}