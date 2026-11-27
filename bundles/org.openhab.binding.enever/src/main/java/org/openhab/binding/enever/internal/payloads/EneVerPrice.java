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
package org.openhab.binding.enever.internal.payloads;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

@NonNullByDefault
public class EneVerPrice implements Comparable<EneVerPrice> {

    @SerializedName(value = "datum")
    protected String datum = "";

    @SerializedName(value = "prijsZP", alternate = { "prijsAN", "prijsBE", "prijsCB", "prijsEE", "prijsNE", "prijsED",
            "prijsEG", "prijsES", "prijsEV", "prijsEZ", "prijsFR", "prijsGS", "prijsHE", "prijsIN", "prijsMD",
            "prijsPE", "prijsQU", "prijsSS", "prijsTI", "prijsVD", "prijsVO", "prijsVF", "prijsWE" })
    protected String prijs = "";

    @SerializedName(value = "prijs")
    protected String marktPrijs = "";

    private ZonedDateTime datumTijd = ZonedDateTime.of(LocalDate.MIN, LocalTime.MIDNIGHT, ZoneId.systemDefault());

    public Boolean isDuur = false;
    public Boolean isGoedkoop = false;

    public EneVerPrice() {
    }

    public EneVerPrice(ZonedDateTime datum, Double prijs, Double marktPrijs) {
        this.datumTijd = datum;
        this.prijs = prijs.toString();
        this.marktPrijs = marktPrijs.toString();
    }

    public EneVerPrice(LocalDate datum, Double prijs, Double marktPrijs) {
        this(ZonedDateTime.of(datum, LocalTime.MIDNIGHT, ZoneId.systemDefault()), prijs, marktPrijs);
    }

    public EneVerPrice(LocalDate datum, Double prijs) {
        this(datum, prijs, 0.0);
    }

    public Double getPrijs() {
        try {
            return Double.parseDouble(prijs);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    public Double getMarktPrijs() {
        try {
            return Double.parseDouble(marktPrijs);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    public LocalDate getDatum() {
        return getDatumTijd().toLocalDate();
    }

    public ZonedDateTime getDatumTijd() {
        if (datumTijd.toLocalDate().isBefore(LocalDate.now())) {
            try {
                datumTijd = ZonedDateTime.parse(datum).withSecond(0).withSecond(0);
            } catch (DateTimeParseException ex) {
                datumTijd = ZonedDateTime.of(LocalDate.MIN, LocalTime.MIDNIGHT, ZoneId.systemDefault());
            }
        }
        return datumTijd;
    }

    public int getUur() {
        return getDatumTijd().getHour();
    }

    public int getMinuut() {
        return getDatumTijd().getMinute();
    }

    public boolean isValid() {
        return getDatumTijd().toLocalDate().isAfter(LocalDate.MIN);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || !(o instanceof EneVerPrice)) {
            return false;
        }
        return datum.equals(((EneVerPrice) o).datum);
    }

    @Override
    public String toString() {
        return "[" + this.getDatumTijd() + " : " + this.getPrijs() + " : " + this.getMarktPrijs() + "]\n";
    }

    @Override
    public int compareTo(EneVerPrice o) {
        return this.datum.compareTo(o.datum);
    }
}
