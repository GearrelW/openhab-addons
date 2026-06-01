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
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EPrices {

    public static final String SOLAR_CONTROL = "solar";
    public static final String PRICES_CONTROL = "prices";

    private final Logger logger = LoggerFactory.getLogger(EneVerHandler.class);

    private TreeSet<EPrice> allPrices = new TreeSet<EPrice>();
    private Plan plan = new Plan();
    private Double treshold = 0.15;

    public EPrices(Double minMaxTreshold, Double priceTreshold, int numberOfHours) {
        this.treshold = priceTreshold;
        this.plan = new Plan(numberOfHours, minMaxTreshold);
    }

    public void addPrices(Map<LocalDateTime, Double> prices) {
        prices.entrySet().forEach(entry -> {
            allPrices.add(new EPrice(entry.getKey(), entry.getValue()));
        });
        allPrices.removeIf(ep -> ep.getDatumTijd().isBefore(LocalDateTime.now().minusHours(1)));
        allPrices.stream().sorted();

        plan.plan(allPrices);
    }

    public TreeSet<EPrice> getAllPrices() {
        return allPrices;
    }

    public Plan getPlan() {
        return plan;
    }

    public String getControlStrategy() {
        return plan.isSolarModeEnabled() ? SOLAR_CONTROL : PRICES_CONTROL;
    }

    public boolean containsDate(LocalDate date) {
        return allPrices.stream().anyMatch(ep -> ep.getDatum().equals(date));
    }

    public EPrice getMaxPriceFor(LocalDate date) {
        return plan.getMaxPrices().get(date);
    }

    public Double getAveragePriceFor(LocalDate date) {
        return plan.getAveragePrices().get(date);
    }

    public EPrice getPriceFor(LocalDateTime datetime) {
        var price = allPrices.stream()
                .filter(ep -> ep.getDatum().equals(datetime.toLocalDate()) && ep.getUur() == datetime.getHour())
                .findFirst().orElse(null);
        if (price != null) {
            if (price.getMode() == EPrice.NONE) {
                plan.plan(allPrices);
                price = allPrices.stream()
                        .filter(ep -> ep.getDatum().equals(datetime.toLocalDate()) && ep.getUur() == datetime.getHour())
                        .findFirst().orElse(null);
            }

            var avgPrice = plan.getAveragePrices().get(datetime.toLocalDate());

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
