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
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EPrices {

    public static final String SOLAR_CONTROL = "solar";
    public static final String PRICES_CONTROL = "prices";

    private Map<LocalDate, Double> averagePrices = new Hashtable<>();
    private Map<LocalDate, EPrice> maxPrices = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(EPrices.class);

    private TreeSet<EPrice> allPrices = new TreeSet<EPrice>();
    private Double treshold = 0.15;

    public EPrices(Double minMaxTreshold, Double priceTreshold) {
        this.treshold = priceTreshold;
    }

    public void addPrices(Map<ZonedDateTime, Double> prices) {
        prices.entrySet().forEach(entry -> {
            allPrices.add(new EPrice(entry.getKey(), entry.getValue()));
        });
        allPrices.removeIf(ep -> ep.getDatumTijd().isBefore(ZonedDateTime.now().minusHours(1)));
        allPrices.stream().sorted();

        allPrices.stream()
                .collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)))
                .forEach((date, avg) -> {
                    if (!averagePrices.containsKey(date)) {
                        averagePrices.put(date, avg);
                    }
                });

        allPrices.stream().collect(Collectors.groupingBy(EPrice::getDatum,
                Collectors.maxBy((p1, p2) -> p1.getPrijs().compareTo(p2.getPrijs())))).forEach((date, max) -> {
                    maxPrices.put(date, max.get());
                });
    }

    public TreeSet<EPrice> getAllPrices() {
        return allPrices;
    }

    public boolean containsDate(LocalDate date) {
        return allPrices.stream().anyMatch(ep -> ep.getDatum().equals(date));
    }

    public EPrice getMaxPriceFor(LocalDate date) {
        return maxPrices.get(date);
    }

    public Double getAveragePriceFor(LocalDate date) {
        return averagePrices.get(date);
    }

    public boolean hasPriceFor(ZonedDateTime datetime) {
        return getPriceFor(datetime) != null;
    }

    public EPrice getPriceFor(ZonedDateTime datetime) {
        var price = allPrices.stream().filter(ep -> ep.getDatumTijd().isBefore(datetime))
                .reduce((first, second) -> second).orElse(null);
        if (price != null) {
            var avgPrice = averagePrices.get(datetime.toLocalDate());

            if (price.getPrijs() <= (avgPrice * (1 - treshold))) {
                price.isGoedkoop = true;
            }
            if (price.getPrijs() > (avgPrice * (1 + treshold))) {
                price.isDuur = true;
            }
        }

        return price;
    }
}
