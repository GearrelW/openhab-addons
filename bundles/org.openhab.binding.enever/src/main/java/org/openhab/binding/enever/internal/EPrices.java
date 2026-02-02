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
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
    private LocalDateTime lastControlledDateTime = LocalDateTime.now().minusDays(1);
    public Map<LocalDate, Double> averagePrices = new Hashtable<>();
    public Map<LocalDate, EPrice> maxPrices = new Hashtable<>();

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
        if (LocalDateTime.now().isAfter(lastControlledDateTime)) {
            allPrices.removeIf(eprice -> eprice.getDatum().isBefore(LocalDate.now()));
            averagePrices.keySet().removeIf(date -> date.isBefore(LocalDate.now()));

            allPrices.stream()
                    .collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)))
                    .forEach((date, avg) -> {
                        if (!averagePrices.containsKey(date)) {
                            averagePrices.put(date, avg);
                        }
                    });
            allPrices.stream()
                    .collect(Collectors.groupingBy(EPrice::getDatum,
                            Collectors.maxBy((p1, p2) -> p1.getPrijs().compareTo(p2.getPrijs()))))
                    .forEach((date, max) -> {
                        maxPrices.put(date, max.get());
                    });
            setModes();
            logger.info("Next control after: " + lastControlledDateTime);
        }
    }

    public void resetModes() {
        setModes(LocalDateTime.now(), null);
    }

    private void setModes() {
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

    private LinkedHashMap<EPrice, LinkedList<EPrice>> findMatchingPrices(List<EPrice> prices) {
        var matches = new LinkedHashMap<EPrice, LinkedList<EPrice>>();

        var lowPrices = prices.stream().sorted((ep1, ep2) -> ep1.getPrijs() < ep2.getPrijs() ? -1 : 1)
                .filter(l -> prices.stream()
                        .anyMatch(h -> h.getUur() > l.getUur() && h.getPrijs() >= l.getPrijs() * (1 + minMaxTreshold)))
                .limit(numberOfHours).collect(Collectors.toList());

        // logger.error("findMatchingPrices: lowPrices " + lowPrices.toString());

        var lowsWithHighs = new LinkedHashMap<EPrice, List<EPrice>>();
        for (EPrice l : lowPrices) {
            var his = prices.stream()
                    .filter(highPrice -> highPrice.getUur() > l.getUur()
                            && highPrice.getPrijs() >= l.getPrijs() * (1 + minMaxTreshold))
                    .collect(Collectors.toList());
            if (!his.isEmpty()) {
                lowsWithHighs.put(l, his.stream().sorted((h1, h2) -> h1.getPrijs() < h2.getPrijs() ? -1 : 1)
                        .collect(Collectors.toList()));
            }
        }

        lowsWithHighs.forEach((k, v) -> {
            matches.put(k, new LinkedList<>());
        });

        var chosenHighs = new ArrayList<EPrice>();

        lowsWithHighs.forEach((low, highsForLow) -> {
            var match = matches.get(low);
            var numberOfChosenHighs = 0;

            for (int h = 0; h < highsForLow.size(); h++) {
                var high = highsForLow.get(h);
                if (!chosenHighs.contains(high) && numberOfChosenHighs < 2) {
                    match.add(high);
                    chosenHighs.add(high);
                    numberOfChosenHighs++;
                }
            }
        });

        String chosenStart = chosenHighs.toString();
        String chosenEnd = "";

        do {
            chosenStart = chosenEnd;

            lowsWithHighs.forEach((low, highsForLow) -> {
                var match = matches.get(low);
                var numberOfChosenHighs = 0;

                for (int h = 0; h < highsForLow.size(); h++) {
                    var high = highsForLow.get(h);
                    if (!chosenHighs.contains(high) && numberOfChosenHighs < 1) {
                        var removed = match.remove(0);
                        match.add(high);
                        chosenHighs.remove(removed);
                        chosenHighs.add(high);
                        highsForLow.remove(removed);
                        numberOfChosenHighs++;
                    }
                }
            });
            chosenEnd = chosenHighs.toString();
        } while (chosenStart.equals(chosenEnd) == false);

        // logger.error("findMatchingPrices: matches " + matches.toString());
        return matches;
    }

    private void setPricesMode(List<EPrice> prices, LinkedHashMap<EPrice, LinkedList<EPrice>> matches) {
        if (matches.isEmpty()) {
            setSolarMode(prices);
            return;
        }

        prices.stream().forEach(ep -> ep.setMode(EPrice.STANDBY));

        matches.forEach((low, highs) -> {
            low.setMode(EPrice.TO_FULL);
            highs.stream().forEach(h -> h.setMode(EPrice.ZERO_DISCHARGE_ONLY));
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

        lastControlledDateTime = full.getLast().getDatumTijd();
        logger.info("setPricesMode: " + prices.toString());
    }

    private void setSolarMode(List<EPrice> prices) {
        prices.stream().forEach(ep -> {
            if (ep.getUur() < 9) {
                ep.setMode(EPrice.ZERO);
                return;
            }
            if (ep.getUur() > 16) {
                var maxPrice = maxPrices.get(ep.getDatum());
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
        try {
            lastControlledDateTime = prices.stream().filter(ep -> !ep.getMode().equals(EPrice.ZERO))
                    .collect(Collectors.toList()).getLast().getDatumTijd();
        } catch (Exception e) {
            // lastControlledDateTime = LocalDateTime.now().plusDays(1).withHour(0);
        }

        logger.info("setSolarMode: " + prices.toString());
    }
}
