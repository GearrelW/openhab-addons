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
package org.openhab.binding.enever.internal.payloads;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from EneVer.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */

public class PayloadPriceItem {

    @SerializedName(value = "datum", alternate = "dateTime")
    protected String datum = "";

    @SerializedName(value = "prijsZP", alternate = "priceTotalTaxIncluded")
    protected String prijs = "";

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

    // public void setDatum(String newDate) {
    // datum = datum
    // }

    public Double getPrijs() {
        try {
            return Double.parseDouble(prijs);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    @Override
    public String toString() {
        return "Datum: " + getDatumTijd() + " Prijs: " + getPrijs();
    }
}
