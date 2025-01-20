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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class PayloadPriceItem {

    @SerializedName("datum")
    private String datum = "";

    @SerializedName("prijsZP")
    private String prijs = "";

    public LocalDate getDatum() {
        return LocalDateTime.parse(datum, DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss")).toLocalDate();
    }

    public LocalTime getTime() {
        return LocalDateTime.parse(datum, DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss")).toLocalTime();
    }

    public Double getPrijs() {
        return Double.parseDouble(prijs);
    }

}