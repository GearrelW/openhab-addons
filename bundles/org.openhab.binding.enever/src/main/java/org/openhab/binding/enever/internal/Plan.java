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
    private int numberOfChargingHours = 2;
    private Double minMaxTreshold = 0.4;
    private Map<LocalDate, Double> averagePrices = new Hashtable<>();
    private Map<LocalDate, EPrice> maxPrices = new Hashtable<>();

    private LocalDateTime morningChargeStart;
    private LocalDateTime afternoonChargeStart;
    private LocalDateTime morningChargeEnd;
    private LocalDateTime afternoonChargeEnd;

    private LocalDateTime morningDischargeStart;
    private LocalDateTime afternoonDischargeStart;
    private LocalDateTime morningDischargeEnd;
    private LocalDateTime afternoonDischargeEnd;

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

    public Map<LocalDate, EPrice> getMaxPrices() {
        return maxPrices;
    }

    public LocalDateTime getChargeStart(LocalDateTime start) {
        if (morningChargeStart != null && morningChargeStart.isAfter(start)) {
            return morningChargeStart;
        } else {
            return afternoonChargeStart;
        }
    }

    public LocalDateTime getChargeEnd(LocalDateTime start) {
        if (morningChargeEnd != null && morningChargeEnd.isAfter(start)) {
            return morningChargeEnd;
        } else {
            return afternoonChargeEnd;
        }
    }

    public LocalDateTime getDischargeStart(LocalDateTime start) {
        if (morningDischargeStart != null && morningDischargeStart.isAfter(start)) {
            return morningDischargeStart;
        } else {
            return afternoonDischargeStart;
        }
    }

    public LocalDateTime getDischargeEnd(LocalDateTime start) {
        if (morningDischargeEnd != null && morningDischargeEnd.isAfter(start)) {
            return morningDischargeEnd;
        } else {
            return afternoonDischargeEnd;
        }
    }

    public Map<LocalDate, Double> getAveragePrices() {
        return averagePrices;
    }

    public boolean isSolarModeEnabled() {
        return highPrices.isEmpty();
    }

    private void init(TreeSet<EPrice> pr) {
        highPrices.clear();
        averagePrices.clear();
        prices = pr;

        prices.stream().collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)))
                .forEach((date, avg) -> {
                    if (!averagePrices.containsKey(date)) {
                        averagePrices.put(date, avg);
                    }
                });

        prices.stream().collect(Collectors.groupingBy(EPrice::getDatum,
                Collectors.maxBy((p1, p2) -> p1.getPrijs().compareTo(p2.getPrijs())))).forEach((date, max) -> {
                    maxPrices.put(date, max.get());
                });
    }

    public void plan(TreeSet<EPrice> pr) {
        init(pr);
        morningChargeStart = null;
        morningChargeEnd = null;
        morningDischargeStart = null;
        morningDischargeEnd = null;

        afternoonChargeStart = null;
        afternoonChargeEnd = null;
        afternoonDischargeStart = null;
        afternoonDischargeEnd = null;

        if (prices.isEmpty()) {
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

        processMorningHighs(morning);

        processAfternoonHighs(afternoonStart, afternoon);

        highPrices = highPrices.stream().sorted((ep1, ep2) -> ep1.getUur() > ep2.getUur() ? 1 : -1)
                .collect(Collectors.toList());

        setPricesModes();
    }

    private void processMorningHighs(List<EPrice> morning) {
        if (!morning.isEmpty()) {
            for (EPrice high : morning) {
                prices.stream()
                        .filter(lowPrice -> lowPrice.getDatumTijd().isBefore(high.getDatumTijd())
                                && high.getPrijs() >= lowPrice.getPrijs() * (1 + minMaxTreshold))
                        .forEach(low -> lowPrices.add(low));
            }
            var morningLowNumber = Math.ceilDiv(morning.size(), 2);
            lowPrices = lowPrices.stream().sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1)
                    .limit(morningLowNumber).collect(Collectors.toCollection(() -> new TreeSet<EPrice>()));

            morningChargeStart = lowPrices.stream()
                    .min((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1).orElse(lowPrices.last())
                    .getDatumTijd();
            morningChargeEnd = lowPrices.stream()
                    .max((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1).orElse(lowPrices.last())
                    .getDatumTijd().plusHours(1);
            morningDischargeStart = morning.stream()
                    .min((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1).orElse(lowPrices.last())
                    .getDatumTijd();
            morningDischargeEnd = morning.stream()
                    .max((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1).orElse(lowPrices.last())
                    .getDatumTijd().plusHours(1);
        }
    }

    private void processAfternoonHighs(EPrice afternoonStart, List<EPrice> afternoon) {
        TreeSet<EPrice> afternoonLows = new TreeSet<EPrice>();
        for (EPrice high : afternoon) {
            prices.stream()
                    .filter(lowPrice -> lowPrice.getDatumTijd().isBefore(high.getDatumTijd())
                            && lowPrice.getDatumTijd().isAfter(afternoonStart.getDatumTijd())
                            && high.getPrijs() >= lowPrice.getPrijs() * (1 + minMaxTreshold))
                    .forEach(low -> afternoonLows.add(low));
        }

        var afternoonLowNumber = Math.ceilDiv(afternoon.size(), 2);
        var al = afternoonLows.stream().sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1)
                .limit(afternoonLowNumber).collect(Collectors.toCollection(() -> new TreeSet<EPrice>()));
        al.forEach(low -> lowPrices.add(low));

        afternoonChargeStart = al.stream().min((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1)
                .orElse(al.last()).getDatumTijd();
        afternoonChargeEnd = al.stream().max((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1)
                .orElse(al.last()).getDatumTijd().plusHours(1);
        afternoonDischargeStart = afternoon.stream()
                .min((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1).orElse(afternoonLows.last())
                .getDatumTijd();
        afternoonDischargeEnd = afternoon.stream()
                .max((l1, l2) -> l1.getDatumTijd().isBefore(l2.getDatumTijd()) ? -1 : 1).orElse(afternoonLows.last())
                .getDatumTijd().plusHours(1);
    }

    private void setPricesModes() {
        if (afternoonChargeStart != null && afternoonDischargeEnd != null) {
            prices.stream().forEach(ep -> {
                if (ep.getDatumTijd().isBefore(afternoonChargeStart)) {
                    ep.setMode(EPrice.ZERO_DISCHARGE_ONLY);
                }
                if (ep.getDatumTijd().isAfter(afternoonChargeStart)
                        && ep.getDatumTijd().isBefore(afternoonDischargeEnd)) {
                    ep.setMode(EPrice.STANDBY);
                }
            });
        }

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
