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
package org.openhab.binding.enever.internal;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class EPrice implements Comparable<EPrice> {

    private ZonedDateTime datum = ZonedDateTime.now();

    private Double prijs = 0.0;

    public Boolean isDuur = false;
    public Boolean isGoedkoop = false;

    public EPrice(ZonedDateTime datum, Double prijs) {
        this.datum = datum;
        this.prijs = prijs;
    }

    public Double getPrijs() {
        return prijs;
    }

    public LocalDate getDatum() {
        return datum.toLocalDate();
    }

    public ZonedDateTime getDatumTijd() {
        return datum.withSecond(0).withNano(0);
    }

    public int getUur() {
        return datum.getHour();
    }

    public int getMinuut() {
        return datum.getMinute();
    }

    @Override
    public boolean equals(Object o) {
        return datum.equals(((EPrice) o).datum);
    }

    @Override
    public String toString() {
        return "[" + this.getDatumTijd() + " : " + this.prijs + "]\n";
    }

    @Override
    public int compareTo(EPrice o) {
        return this.datum.compareTo(o.datum);
    }
}
