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
import java.util.LinkedHashMap;
import java.util.List;
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
    public String controlStrategy = SOLAR_CONTROL;
    private Double treshold = 0.15;
    private Double minMaxTreshold = 0.4;
    private int numberOfHours = 2;
    public Map<LocalDate, Double> averagePrices = new Hashtable<>();

    public EPrices(String controlStrategy, Double minMaxTreshold, Double priceTreshold, int numberOfHours) {
        this.controlStrategy = controlStrategy;
        this.minMaxTreshold = minMaxTreshold;
        this.treshold = priceTreshold;
        this.numberOfHours = numberOfHours;
    }

    public void addPrices(Map<LocalDateTime, Double> prices) {
        prices.entrySet().forEach(entry -> {
            allPrices.add(new EPrice(entry.getKey(), entry.getValue()));
        });
        allPrices.stream().sorted();
    }

    public TreeSet<EPrice> getAllPrices() {
        return allPrices;
    }

    public boolean containsDate(LocalDate date) {
        return allPrices.stream().anyMatch(ep -> ep.getDatum().equals(date));
    }

    public EPrice getPriceFor(LocalDateTime datetime) {
        return allPrices.stream()
                .filter(ep -> ep.getDatum().equals(datetime.toLocalDate()) && ep.getUur() == datetime.getHour())
                .findFirst().orElse(null);
    }

    public EPrice getMaxPrice(LocalDate date) {
        return allPrices.stream().filter(ep -> ep.getDatum().equals(date))
                .max((p1, p2) -> p1.getPrijs().compareTo(p2.getPrijs())).orElse(null);
    }

    public List<EPrice> getCheapPrices(LocalDateTime date, int limit) {
        return allPrices.stream().filter(ep -> ep.getDatum().equals(date.toLocalDate()) && ep.isGoedkoop).limit(limit)
                .collect(Collectors.toList());
    }

    public List<EPrice> getExpensivePrices(LocalDateTime date, int limit) {
        return allPrices.stream().filter(ep -> ep.getDatum().equals(date.toLocalDate()) && ep.isDuur).limit(limit)
                .collect(Collectors.toList());
    }

    private void initModes(List<EPrice> prices) {
        for (var entry : averagePrices.entrySet()) {
            var avgPrice = entry.getValue();
            var date = entry.getKey();

            prices.stream().filter(ep -> ep.getMode().isEmpty() && ep.getDatum().equals(date)).forEach(ep -> {
                if (ep.getPrijs() <= avgPrice) {
                    ep.setMode(EPrice.ZERO_CHARGE_ONLY);
                } else {
                    ep.setMode(EPrice.ZERO_DISCHARGE_ONLY);
                }

                if (ep.getPrijs() <= (avgPrice * (1 - treshold))) {
                    ep.isGoedkoop = true;
                }
                if (ep.getPrijs() > (avgPrice * (1 + treshold))) {
                    ep.isDuur = true;
                }
            });
        }
        // logger.error("initModes: prices " + prices.toString());
    }

    public void processPrices() {
        allPrices.removeIf(eprice -> eprice.getDatum().isBefore(LocalDate.now()));
        averagePrices.keySet().removeIf(date -> date.isBefore(LocalDate.now()));

        averagePrices = allPrices.stream()
                .collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)));
        setModes();
    }

    public void resetModes() {
        setModes(LocalDateTime.now(), null);
    }

    public void setModes() {
        setModes(LocalDateTime.now(), null);
    }

    public void setModes(LocalDateTime dateTime, String strategy) {
        var dt = dateTime.withMinute(0).withSecond(0).withNano(0);
        var myPrices = allPrices.stream().filter(ep -> ep.getDatumTijd().isAfter(dt) || dt.isEqual(ep.getDatumTijd()))
                .collect(Collectors.toList());

        initModes(myPrices);

        var matches = findMatchingPrices(myPrices);

        if (strategy == null) {
            if (matches.isEmpty()) {
                strategy = SOLAR_CONTROL;
            } else {
                strategy = PRICES_CONTROL;
            }
        }
        controlStrategy = strategy;

        if (PRICES_CONTROL.equals(controlStrategy)) {
            setPricesMode(myPrices, matches);
        } else { // SOLAR_MODE
            setSolarMode(myPrices);
        }
        // logger.error("setModes: prices " + allPrices.toString());
    }

    private Map<EPrice, EPrice> findMatchingPrices(List<EPrice> prices) {
        var matches = new LinkedHashMap<EPrice, EPrice>();

        var low = prices.stream().sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1)
                .collect(Collectors.toList());
        var high = prices.stream().sorted((ep1, ep2) -> ep1.getPrijs() < ep2.getPrijs() ? 1 : -1)
                .collect(Collectors.toList());

        low.sort((p1, p2) -> p1.getPrijs() < p2.getPrijs() ? 1 : -1);
        high.sort((p1, p2) -> p1.getPrijs() < p2.getPrijs() ? 1 : -1);

        for (int i = 0; i < numberOfHours; i++) {
            var highPrice = high.get(i);
            low.stream()
                    .filter(lowPrice -> lowPrice.getUur() < highPrice.getUur()
                            && highPrice.getPrijs() >= lowPrice.getPrijs() * (1 + minMaxTreshold))
                    .findFirst().ifPresent(foundLow -> {
                        matches.put(highPrice, foundLow);
                        low.remove(foundLow);
                    });
        }

        logger.error("findMatchingPrices: matches " + matches.toString());
        return matches;
    }

    private void setPricesMode(List<EPrice> prices, Map<EPrice, EPrice> matches) {
        if (matches.isEmpty()) {
            setSolarMode(prices);
            return;
        }

        prices.stream().forEach(ep -> ep.setMode(EPrice.STANDBY));

        matches.forEach((h, l) -> {
            l.setMode(EPrice.TO_FULL);
            h.setMode(EPrice.ZERO_DISCHARGE_ONLY);
        });

        var full = prices.stream().filter(l -> l.getMode().equals(EPrice.TO_FULL)).collect(Collectors.toList());
        var discharge = prices.stream().filter(l -> l.getMode().equals(EPrice.ZERO_DISCHARGE_ONLY))
                .collect(Collectors.toList());

        var firstToFull = full.getFirst().getUur();
        var lastToFull = full.getLast().getUur();

        var firstToDischarge = discharge.getFirst().getUur();
        var lastToDischarge = discharge.getLast().getUur();

        prices.stream().forEach(ep -> {
            if (ep.getUur() > lastToFull && ep.getUur() < firstToDischarge) {
                ep.setMode(EPrice.ZERO_CHARGE_ONLY);
                return;
            }
            if (ep.getUur() < firstToFull || ep.getUur() > lastToDischarge) {
                ep.setMode(EPrice.ZERO);
            }
        });
        logger.error("setPricesMode: " + prices.toString());
    }

    private void setSolarMode(List<EPrice> prices) {
        var date = prices.getFirst().getDatum();

        prices.stream().forEach(ep -> {
            if (ep.getUur() < 9) {
                ep.setMode(EPrice.ZERO);
                return;
            }
            if (ep.getUur() > 16) {
                var maxPrice = getMaxPrice(date);
                if (maxPrice != null && (maxPrice.getUur() - 1) > 16) {
                    if (ep.getUur() < (maxPrice.getUur() - 1)) {
                        ep.setMode(EPrice.ZERO_CHARGE_ONLY);
                    } else {
                        ep.setMode(EPrice.ZERO);
                    }
                } else {
                    ep.setMode(EPrice.ZERO);
                }
            }
        });
        logger.error("setSolarMode: " + prices.toString());
    }
}
