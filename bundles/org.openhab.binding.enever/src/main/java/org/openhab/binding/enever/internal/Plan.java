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
    private int numberOfChargingHours = 2;
    private Double minMaxTreshold = 0.4;
    public Map<LocalDate, Double> averagePrices = new Hashtable<>();
    public Map<LocalDate, EPrice> maxPrices = new Hashtable<>();

    public Plan() {
    }

    public Plan(int numberOfChargingHours, Double minMaxTreshold) {
        this.numberOfChargingHours = numberOfChargingHours;
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

    private void init(TreeSet<EPrice> prices) {
        highPrices.clear();
        averagePrices.clear();

        prices.stream()
                .collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)))
                .forEach((date, avg) -> {
                    if (!averagePrices.containsKey(date)) {
                        averagePrices.put(date, avg);
                    }
                });
        
        this.prices = prices.stream().filter(ep -> ep.getMode() == EPrice.NONE).collect(Collectors.toCollection(() -> new TreeSet<EPrice>()));     
    }

    public void plan(TreeSet<EPrice> prices) {
        init(prices);
        if (this.prices.isEmpty()) {
            return;
        }
        logger.error("plan: planning");
        setSolarMode();

        var hp = prices.stream().sorted((ep1, ep2) -> ep1.getPrijs() < ep2.getPrijs() ? 1 : -1)
                .filter(high -> prices.stream()
                        .anyMatch(low -> low.getDatumTijd().isBefore(high.getDatumTijd())
                                && high.getPrijs() >= low.getPrijs() * (1 + minMaxTreshold)))
                .collect(Collectors.toList());

        EPrice morningLimit = prices.stream().filter(h -> h.getUur() == 12).findFirst().orElse(null);
        EPrice afternoonStart = prices.stream().filter(h -> h.getUur() == 9).findFirst().orElse(prices.first());

        List<EPrice> morning = new ArrayList<EPrice>();
        List<EPrice> afternoon = new ArrayList<EPrice>();

        if (morningLimit != null) {
            morning = hp.stream().filter(h -> h.getDatumTijd().isBefore(morningLimit.getDatumTijd()))
                    .limit(numberOfChargingHours).collect(Collectors.toList());
        }

        afternoon = hp.stream().filter(h -> h.getDatumTijd().isAfter(afternoonStart.getDatumTijd()))
                    .limit(numberOfChargingHours * 2).collect(Collectors.toList());

        morning.forEach(h -> highPrices.add(h));
        afternoon.forEach(h -> highPrices.add(h));

        if (highPrices.isEmpty()) {
            return;
        }

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
                    .filter(lowPrice -> lowPrice.getDatumTijd().isBefore(high.getDatumTijd()) && lowPrice.getDatumTijd().isAfter(afternoonStart.getDatumTijd())
                            && high.getPrijs() >= lowPrice.getPrijs() * (1 + minMaxTreshold))
                    .forEach(low -> afternoonLows.add(low));
        }
        
        var afternoonLowNumber = Math.ceilDiv(afternoon.size(), 2);
        afternoonLows.stream().sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1).limit(afternoonLowNumber)
                .forEach(low -> lowPrices.add(low));     

        highPrices = highPrices.stream().sorted((ep1, ep2) -> ep1.getUur() > ep2.getUur() ? 1 : -1)
                .collect(Collectors.toList());

        setPricesModes();
    }

    private void setPricesModes() {
        var firstCharge = lowPrices.stream().min((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1)
                .orElse(null);
        var lastDischarge = highPrices.stream().max((h1, h2) -> h1.getDatumTijd().isBefore(h2.getDatumTijd()) ? -1 : 1)
                .orElse(null);

        prices.stream().forEach(ep -> {
            if (ep.getDatumTijd().isBefore(firstCharge.getDatumTijd()) && lowPrices.size() == numberOfChargingHours) {
                ep.setMode(EPrice.ZERO_DISCHARGE_ONLY);
            }
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

        logger.info("setSolarMode: " + prices.toString());
    }
}
