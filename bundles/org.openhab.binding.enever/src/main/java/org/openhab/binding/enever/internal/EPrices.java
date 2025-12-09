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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

public class EPrices {

    private HashSet<EPrice> prices = new HashSet<EPrice>();
    public Map<LocalDate, Double> averagePrices = new Hashtable<>();

    public void addPrice(LocalDateTime date, Double price) {
        prices.add(new EPrice(date, price));
        prices.stream().sorted();
    }

    public HashSet<EPrice> getPrices() {
        return prices;
    }

    public boolean containsDate(LocalDate date) {
        return prices.stream().anyMatch(ep -> ep.getDatum().equals(date));
    }

    public EPrice getPriceFor(LocalDate date, int hour) {
        return prices.stream().filter(ep -> ep.getDatum().equals(date) && ep.getUur() == hour).findFirst().orElse(null);
    }

    public void processPrices(Double treshold, int limit) {
        averagePrices = prices.stream()
                .collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)));
    }

    // @Override
    // public boolean equals(Object o) {
    // return datum.equals(((EPrice) o).datum);
    // }
}
