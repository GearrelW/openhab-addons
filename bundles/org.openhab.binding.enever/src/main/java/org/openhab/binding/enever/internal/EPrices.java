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
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EPrices {

    public static final String SOLAR_CONTROL = "solar";
    public static final String PRICES_CONTROL = "prices";

    private final Logger logger = LoggerFactory.getLogger(EneVerHandler.class);

    private TreeSet<EPrice> allPrices = new TreeSet<EPrice>();
    public Plan plan = new Plan();
    public String controlStrategy = SOLAR_CONTROL;
    private Double treshold = 0.15;

    private LocalDateTime lastControlledDateTime = LocalDateTime.now().minusDays(1);
    public Map<LocalDate, Double> averagePrices = new Hashtable<>();
    public Map<LocalDate, EPrice> maxPrices = new Hashtable<>();

    public EPrices(Double minMaxTreshold, Double priceTreshold, int numberOfHours) {
        this.treshold = priceTreshold;
        this.plan = new Plan(numberOfHours, minMaxTreshold);
    }

    public void addPrices(Map<LocalDateTime, Double> prices) {
        prices.entrySet().forEach(entry -> {
            allPrices.add(new EPrice(entry.getKey(), entry.getValue()));
        });
        allPrices.removeIf(ep -> ep.getDatum().isBefore(LocalDate.now()));
        allPrices.stream().sorted();

        averagePrices.keySet().removeIf(date -> date.isBefore(LocalDate.now()));

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
        plan.plan(allPrices);
    }

    public TreeSet<EPrice> getAllPrices() {
        return allPrices;
    }

    public boolean containsDate(LocalDate date) {
        return allPrices.stream().anyMatch(ep -> ep.getDatum().equals(date));
    }

    public EPrice getPriceFor(LocalDateTime datetime) {
        var price = allPrices.stream()
                .filter(ep -> ep.getDatum().equals(datetime.toLocalDate()) && ep.getUur() == datetime.getHour())
                .findFirst().orElse(null);
        if (price != null && price.getMode() == EPrice.NONE) {
            plan.plan(allPrices);
            price = allPrices.stream()
                    .filter(ep -> ep.getDatum().equals(datetime.toLocalDate()) && ep.getUur() == datetime.getHour())
                    .findFirst().orElse(null);
        }

        return price;
    }
}
