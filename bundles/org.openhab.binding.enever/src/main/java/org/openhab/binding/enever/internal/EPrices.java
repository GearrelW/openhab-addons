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
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EPrices {

    private final Logger logger = LoggerFactory.getLogger(EneVerHandler.class);

    private TreeSet<EPrice> allPrices = new TreeSet<EPrice>();
    private Double treshold = 0.15;
    private int numberOfHours = 2;
    public Map<LocalDate, Double> averagePrices = new Hashtable<>();

    public EPrices(Double priceTreshold, int numberOfHours) {
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

    public EPrice getMaxPrice(LocalDateTime date) {
        return allPrices.stream().filter(ep -> ep.getDatum().equals(date.toLocalDate()))
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

    private void initStatus() {
        allPrices.stream().forEach(ep -> {
            Double avgPrice = averagePrices.get(ep.getDatum());
            if (ep.getPrijs() <= avgPrice) {
                ep.status = EPrice.ZERO_CHARGE;
            } else {
                ep.status = EPrice.ZERO_DISCHARGE;
            }

            if (ep.getPrijs() <= (avgPrice * (1 - treshold))) {
                ep.isGoedkoop = true;
            }
            if (ep.getPrijs() > (avgPrice * (1 + treshold))) {
                ep.isDuur = true;
            }
        });
    }

    private void processPrices() {
        averagePrices = allPrices.stream()
                .collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)));

        setStatus();

        allPrices.removeIf(ep -> ep.getDatum().isBefore(LocalDate.now()));
        averagePrices.keySet().removeIf(d -> d.isBefore(LocalDate.now()));
    }

    private void setStatus() {
        var dates = allPrices.stream().map(EPrice::getDatum).distinct().collect(Collectors.toList());

        dates.forEach(date -> {
            logger.error("Processing date: " + date.toString());
            var low = allPrices.stream().filter(ep -> ep.getDatum().equals(date) && ep.isGoedkoop).limit(numberOfHours)
                    .collect(Collectors.toList());
            var high = allPrices.stream().filter(ep -> ep.getDatum().equals(date) && ep.isDuur).limit(numberOfHours)
                    .collect(Collectors.toList());

            if (high.isEmpty() || low.isEmpty()) {
                initStatus();
                return;
            }

            allPrices.stream().filter(ep -> ep.getDatum().equals(date)).forEach(ep -> ep.status = EPrice.STANDBY);

            low.sort((p1, p2) -> p1.getPrijs() < p2.getPrijs() ? 1 : -1);
            high.sort((p1, p2) -> p1.getPrijs() < p2.getPrijs() ? 1 : -1);

            high.forEach(h -> {
                low.stream().filter(l -> !l.status.equals(EPrice.TO_FULL) && l.getUur() < h.getUur()
                        && h.getPrijs() >= l.getPrijs() + 0.15).findFirst().ifPresent(l -> {
                            l.status = EPrice.TO_FULL;
                            h.status = EPrice.ZERO_DISCHARGE;
                        });
            });

            var full = allPrices.stream().filter(l -> l.status.equals(EPrice.TO_FULL)).collect(Collectors.toList());
            var charge = allPrices.stream().filter(l -> l.status.equals(EPrice.ZERO_DISCHARGE))
                    .collect(Collectors.toList());

            if (full.isEmpty()) {
                initStatus();
                return;
            }

            var firstToFull = full.getFirst().getUur();
            var lastToFull = full.getLast().getUur();

            var firstToCharge = charge.getFirst().getUur();
            var lastToCharge = charge.getLast().getUur();

            allPrices.stream().filter(ep -> ep.getDatum().equals(date)).forEach(ep -> {
                if (ep.getUur() > lastToFull && ep.getUur() < firstToCharge) {
                    ep.status = EPrice.ZERO_CHARGE;
                }
                if (ep.getUur() < firstToFull || ep.getUur() > lastToCharge) {
                    ep.status = EPrice.ZERO_DISCHARGE;
                }
            });
        });

        logger.info("pr: " + allPrices.toString());
    }
}
