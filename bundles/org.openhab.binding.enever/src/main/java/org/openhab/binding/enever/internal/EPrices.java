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
import java.util.HashMap;
import java.util.Hashtable;
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
        processPrices();
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
        var avgPrice = averagePrices.get(prices.getFirst().getDatum());
        prices.stream().forEach(ep -> {
            if (ep.getPrijs() <= avgPrice) {
                ep.setStatus(EPrice.ZERO_CHARGE_ONLY);
            } else {
                ep.setStatus(EPrice.ZERO_DISCHARGE_ONLY);
            }

            if (ep.getPrijs() <= (avgPrice * (1 - treshold))) {
                ep.isGoedkoop = true;
            }
            if (ep.getPrijs() > (avgPrice * (1 + treshold))) {
                ep.isDuur = true;
            }
        });
    }

    public void processPrices() {
        averagePrices = allPrices.stream()
                .collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)));
        var dates = allPrices.stream().filter(ep -> ep.getStatus().isEmpty()).map(EPrice::getDatum).distinct()
                .collect(Collectors.toList());

        dates.forEach(date -> {
            setModes(date);
        });

        allPrices.removeIf(ep -> ep.getDatum().isBefore(LocalDate.now()));
        averagePrices.keySet().removeIf(d -> d.isBefore(LocalDate.now()));
    }

    public void resetModes() {
        setModes(LocalDateTime.now());
    }

    private void setModes(LocalDate date) {
        setModes(LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT));
    }

    public void setModes(LocalDateTime dateTime) {
        var myPrices = allPrices.stream()
                .filter(ep -> ep.getDatum().equals(dateTime.toLocalDate()) && ep.getUur() >= dateTime.getHour())
                .collect(Collectors.toList());

        initModes(myPrices);

        var matches = findMatchingPrices(myPrices);

        if (PRICES_CONTROL.equals(controlStrategy)) {
            if (matches.isEmpty()) {
                setSolarStatus(myPrices);
            } else {
                setPricesStatus(myPrices, matches);
            }
        } else { // SOLAR_MODE
            setSolarStatus(myPrices);
        }
        logger.error("prices " + allPrices.toString());
    }

    private Map<EPrice, EPrice> findMatchingPrices(List<EPrice> prices) {
        var matches = new HashMap<EPrice, EPrice>();

        var low = prices.stream().filter(ep -> ep.isGoedkoop)
                .sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1).limit(numberOfHours)
                .collect(Collectors.toList());
        var high = prices.stream().filter(ep -> ep.isDuur)
                .sorted((ep1, ep2) -> ep1.getPrijs() < ep2.getPrijs() ? 1 : -1).limit(numberOfHours)
                .collect(Collectors.toList());

        if (high.isEmpty() || low.isEmpty()) {
            return matches;
        }

        low.sort((p1, p2) -> p1.getPrijs() < p2.getPrijs() ? 1 : -1);
        high.sort((p1, p2) -> p1.getPrijs() < p2.getPrijs() ? 1 : -1);

        high.forEach(h -> {
            low.stream().filter(l -> l.getUur() < h.getUur() && h.getPrijs() >= l.getPrijs() * (1 + minMaxTreshold))
                    .findFirst().ifPresent(l -> {
                        matches.put(h, l);
                        low.remove(l);
                    });
        });
        logger.error("matches " + matches.toString());
        return matches;
    }

    private void setPricesStatus(List<EPrice> prices, Map<EPrice, EPrice> matches) {
        controlStrategy = PRICES_CONTROL;

        prices.stream().forEach(ep -> ep.setStatus(EPrice.STANDBY));

        matches.forEach((h, l) -> {
            l.setStatus(EPrice.TO_FULL);
            h.setStatus(EPrice.ZERO_DISCHARGE_ONLY);
        });

        var full = prices.stream().filter(l -> l.getStatus().equals(EPrice.TO_FULL)).collect(Collectors.toList());
        var discharge = prices.stream().filter(l -> l.getStatus().equals(EPrice.ZERO_DISCHARGE_ONLY))
                .collect(Collectors.toList());

        if (full.isEmpty()) {
            initModes(prices);
            return;
        }

        var firstToFull = full.getFirst().getUur();
        var lastToFull = full.getLast().getUur();

        var firstToDischarge = discharge.getFirst().getUur();
        var lastToDischarge = discharge.getLast().getUur();

        prices.stream().forEach(ep -> {
            if (ep.getUur() > lastToFull && ep.getUur() < firstToDischarge) {
                ep.setStatus(EPrice.ZERO_CHARGE_ONLY);
                return;
            }
            if (ep.getUur() < firstToFull || ep.getUur() > lastToDischarge) {
                ep.setStatus(EPrice.ZERO);
            }
        });
    }

    private void setSolarStatus(List<EPrice> prices) {
        controlStrategy = SOLAR_CONTROL;
        var date = prices.getFirst().getDatum();

        prices.stream().forEach(ep -> {
            if (ep.getUur() < 9) {
                ep.setStatus(EPrice.ZERO);
                return;
            }
            if (ep.getUur() > 16) {
                var maxPrice = getMaxPrice(date);
                if (maxPrice != null && (maxPrice.getUur() - 1) > 16) {
                    if (ep.getUur() < (maxPrice.getUur() - 1)) {
                        ep.setStatus(EPrice.ZERO_CHARGE_ONLY);
                    } else {
                        ep.setStatus(EPrice.ZERO);
                    }
                } else {
                    ep.setStatus(EPrice.ZERO);
                }
            }
        });
    }
}
