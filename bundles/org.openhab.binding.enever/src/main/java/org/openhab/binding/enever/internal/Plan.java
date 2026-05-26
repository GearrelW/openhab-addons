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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Plan {

    public static final String SOLAR_CONTROL = "solar";
    public static final String PRICES_CONTROL = "prices";

    private final Logger logger = LoggerFactory.getLogger(EneVerHandler.class);

    private TreeSet<EPrice> prices = new TreeSet<EPrice>();
    private List<EPrice> highPrices = new ArrayList<EPrice>();
    private TreeSet<EPrice> lowPrices = new TreeSet<EPrice>();
    private Double minMaxTreshold = 0.4;
    private LocalDateTime lastControlledDateTime = LocalDateTime.now().minusDays(1);
    public Map<LocalDate, Double> averagePrices = new Hashtable<>();
    public Map<LocalDate, EPrice> maxPrices = new Hashtable<>();

    public Plan() {
    }

    public Plan(TreeSet<EPrice> prices, Map<LocalDate, Double> averagePrices, Double minMaxTreshold) {
        this.prices = prices;
        this.averagePrices = averagePrices;
        this.minMaxTreshold = minMaxTreshold;
    }

    public void setPrices(TreeSet<EPrice> prices) {
        this.prices = prices;
    }

    public TreeSet<EPrice> getPrices() {
        return prices;
    }

    public boolean isSolarModeEnabled() {
        return highPrices.isEmpty();
    }

    public void plan() {
        plan(false);
    }

    public void plan(boolean forceSolarMode) {
        highPrices.clear();
        setSolarMode();
        var hp = prices.stream().sorted((ep1, ep2) -> ep1.getPrijs() < ep2.getPrijs() ? 1 : -1)
                .filter(high -> prices.stream()
                        .anyMatch(low -> low.getDatumTijd().isBefore(high.getDatumTijd())
                                && high.getPrijs() >= low.getPrijs() * (1 + minMaxTreshold)))
                .collect(Collectors.toList());

        var morning = hp.stream().filter(h -> h.getUur() <= 12).limit(4).collect(Collectors.toList());
        var afternoon = hp.stream().filter(h -> h.getUur() > 12).limit(8).collect(Collectors.toList());

        morning.forEach(h -> highPrices.add(h));
        afternoon.forEach(h -> highPrices.add(h));

        for (EPrice high : morning) {
            prices.stream()
                    .filter(lowPrice -> lowPrice.getDatumTijd().isBefore(high.getDatumTijd())
                            && high.getPrijs() >= lowPrice.getPrijs() * (1 + minMaxTreshold))
                    .forEach(low -> lowPrices.add(low));
        }
        var morningLowNumber = Math.ceilDiv(morning.size(), 2);
        lowPrices = lowPrices.stream().sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1)
                .limit(morningLowNumber).collect(Collectors.toCollection(() -> new TreeSet<EPrice>()));

        TreeSet<EPrice> afternoonLows = new TreeSet<EPrice>();
        for (EPrice high : afternoon) {
            prices.stream()
                    .filter(lowPrice -> lowPrice.getDatumTijd().isBefore(high.getDatumTijd()) && lowPrice.getUur() > 12
                            && high.getPrijs() >= lowPrice.getPrijs() * (1 + minMaxTreshold))
                    .forEach(low -> afternoonLows.add(low));
        }
        var afternoonLowNumber = Math.ceilDiv(afternoon.size(), 2);
        afternoonLows.stream().sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1).limit(afternoonLowNumber)
                .forEach(low -> lowPrices.add(low));

        highPrices = highPrices.stream().sorted((ep1, ep2) -> ep1.getUur() > ep2.getUur() ? 1 : -1)
                .collect(Collectors.toList());

        if (!forceSolarMode && !highPrices.isEmpty()) {
            setPricesModes();
        }
    }

    private void setPricesModes() {
        var firstCharge = lowPrices.stream().min((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1)
                .orElse(null);
        var lastDischarge = highPrices.stream().max((h1, h2) -> h1.getDatumTijd().isBefore(h2.getDatumTijd()) ? -1 : 1)
                .orElse(null);

        prices.stream().forEach(ep -> {
            if (ep.getDatumTijd().isAfter(firstCharge.getDatumTijd())
                    && ep.getDatumTijd().isBefore(lastDischarge.getDatumTijd())) {
                ep.setMode(EPrice.STANDBY);
            }
        });

        lowPrices.stream().forEach(ep -> {
            ep.setMode(EPrice.TO_FULL);
        });

        highPrices.stream().forEach(ep -> {
            ep.setMode(EPrice.ZERO_DISCHARGE_ONLY);
        });

        logger.error("setPricesModes: modes " + prices.toString());
    }

    private void setSolarMode() {
        prices.stream().forEach(ep -> {
            ep.setMode(EPrice.ZERO);
        });

        prices.stream().forEach(ep -> {
            if (ep.getUur() > 16) {
                var maxPrice = maxPrices.get(ep.getDatum());
                if (maxPrice != null && (maxPrice.getUur() - 1) > 16 && ep.getUur() < (maxPrice.getUur() - 1)) {
                    ep.setMode(EPrice.ZERO_CHARGE_ONLY);
                }
            } else {
                var avg = averagePrices.get(ep.getDatum());
                if (ep.getPrijs() < avg) {
                    ep.setMode(EPrice.ZERO_CHARGE_ONLY);
                } else {
                    ep.setMode(EPrice.ZERO_DISCHARGE_ONLY);
                }
            }
        });
        try {
            lastControlledDateTime = prices.stream().filter(ep -> !ep.getMode().equals(EPrice.ZERO))
                    .collect(Collectors.toList()).getLast().getDatumTijd();
        } catch (Exception e) {
            // lastControlledDateTime = LocalDateTime.now().plusDays(1).withHour(0);
        }

        logger.info("setSolarMode: " + prices.toString());
    }
}
