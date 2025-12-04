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
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

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

    public LocalDateTime getDatumTijd() {
        try {
            return ZonedDateTime.parse(datum).withMinute(0).withSecond(0).withSecond(0).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            return LocalDateTime.MIN.withMinute(0).withSecond(0).withSecond(0);
        }
    }

    public LocalDate getDatum() {
        return getDatumTijd().toLocalDate();
    }

    public Double getPrijs() {
        try {
            return Double.parseDouble(prijs);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
