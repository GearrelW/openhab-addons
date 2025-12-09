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

public class EPrice implements Comparable<EPrice> {

    private LocalDateTime datum = LocalDateTime.now();

    private Double prijs = 0.0;

    public String status = "";

    public Boolean isDuur = false;
    public Boolean isGoedkoop = false;

    public EPrice(LocalDateTime datum, Double prijs) {
        this.datum = datum;
        this.prijs = prijs;
    }

    public Double getPrijs() {
        return prijs;
    }

    public LocalDate getDatum() {
        return datum.toLocalDate();
    }

    public int getUur() {
        return datum.getHour();
    }

    @Override
    public boolean equals(Object o) {
        return datum.equals(((EPrice) o).datum);
    }

    @Override
    public String toString() {
        return "[" + this.datum + " : " + this.prijs + "]";
    }

    @Override
    public int compareTo(EPrice o) {
        return this.prijs.compareTo(o.prijs);
    }
}
