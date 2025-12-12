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
import java.util.Collections;
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

    private void initStatus(List<LocalDate> dates) {
        dates.forEach(date -> {
            var avgPrice = averagePrices.get(date);
            allPrices.stream().filter(ep -> ep.getDatum().equals(date)).forEach(ep -> {
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
        });
    }

    private void processPrices() {
        averagePrices = allPrices.stream()
                .collect(Collectors.groupingBy(EPrice::getDatum, Collectors.averagingDouble(EPrice::getPrijs)));
        var dates = allPrices.stream().filter(ep -> ep.getStatus().isEmpty()).map(EPrice::getDatum).distinct().collect(Collectors.toList());
        
        setStatus(dates);

        allPrices.removeIf(ep -> ep.getDatum().isBefore(LocalDate.now()));
        averagePrices.keySet().removeIf(d -> d.isBefore(LocalDate.now()));
    }

    private void setStatus(List<LocalDate> dates) {
        initStatus(dates);
        dates.forEach(date -> {
            var winter = (date.getMonthValue() > 10 || date.getMonthValue() < 3);
            if (winter) {
                setWinterStatus(date);
            } else {
                setSummerStatus(date);
            }
        });
        logger.error("prices " + allPrices.toString());
    }

    private void setWinterStatus(LocalDate date) {
        var myPrices = allPrices.stream().filter(ep -> ep.getDatum().equals(date)).collect(Collectors.toList());

        var low = myPrices.stream().filter(ep -> ep.isGoedkoop)
                .sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1).limit(numberOfHours)
                .collect(Collectors.toList());
        var high = myPrices.stream().filter(ep -> ep.isDuur)
                .sorted((ep1, ep2) -> ep1.getPrijs() < ep2.getPrijs() ? 1 : -1).limit(numberOfHours)
                .collect(Collectors.toList());

        if (high.isEmpty() || low.isEmpty()) {
            return;
        }
        myPrices.stream().forEach(ep -> ep.setStatus(EPrice.STANDBY));

        low.sort((p1, p2) -> p1.getPrijs() < p2.getPrijs() ? 1 : -1);
        high.sort((p1, p2) -> p1.getPrijs() < p2.getPrijs() ? 1 : -1);

        high.forEach(h -> {
            low.stream().filter(l -> !l.getStatus().equals(EPrice.TO_FULL) && l.getUur() < h.getUur()
                    && h.getPrijs() >= l.getPrijs() + 0.15).findFirst().ifPresent(l -> {
                        l.setStatus(EPrice.TO_FULL);
                        h.setStatus(EPrice.ZERO_DISCHARGE_ONLY);
                    });
        });

        var full = allPrices.stream().filter(l -> l.getDatum().equals(date) && l.getStatus().equals(EPrice.TO_FULL))
                .collect(Collectors.toList());
        var discharge = allPrices.stream().filter(l -> l.getStatus().equals(EPrice.ZERO_DISCHARGE_ONLY))
                .collect(Collectors.toList());

        if (full.isEmpty()) {
            initStatus(Collections.singletonList(date));
            return;
        }

        var firstToFull = full.getFirst().getUur();
        var lastToFull = full.getLast().getUur();

        var firstToDischarge = discharge.getFirst().getUur();
        var lastToDischarge = discharge.getLast().getUur();

        myPrices.stream().forEach(ep -> {
            if (ep.getUur() > lastToFull && ep.getUur() < firstToDischarge) {
                ep.setStatus(EPrice.ZERO_CHARGE_ONLY);
            }
            if (ep.getUur() < firstToFull || ep.getUur() > lastToDischarge) {
                ep.setStatus(EPrice.ZERO);
            }
        });
    }

    private void setSummerStatus(LocalDate date) {
        var myPrices = allPrices.stream().filter(ep -> ep.getDatum().equals(date)).collect(Collectors.toList());

        var low = myPrices.stream().filter(ep -> ep.isGoedkoop)
                .sorted((ep1, ep2) -> ep1.getPrijs() > ep2.getPrijs() ? 1 : -1).limit(numberOfHours)
                .collect(Collectors.toList());
        var high = myPrices.stream().filter(ep -> ep.isDuur)
                .sorted((ep1, ep2) -> ep1.getPrijs() < ep2.getPrijs() ? 1 : -1).limit(numberOfHours)
                .collect(Collectors.toList());

        if (high.isEmpty()) {
            return;
        }

        myPrices.stream().filter(ep -> ep.getStatus().equals(EPrice.ZERO_DISCHARGE_ONLY))
                .forEach(ep -> ep.setStatus(EPrice.STANDBY));

        low.forEach(l -> l.setStatus(EPrice.TO_FULL));
        high.forEach(h -> h.setStatus(EPrice.ZERO_DISCHARGE_ONLY));

        var full = allPrices.stream().filter(l -> l.getDatum().equals(date) && l.getStatus().equals(EPrice.TO_FULL))
                .collect(Collectors.toList());
        var discharge = allPrices.stream().filter(l -> l.getStatus().equals(EPrice.ZERO_DISCHARGE_ONLY))
                .collect(Collectors.toList());

        var firstToFull = full.getFirst().getUur();
        var lastToFull = full.getLast().getUur();

        var firstToDischarge = discharge.getFirst().getUur();
        var lastToDischarge = discharge.getLast().getUur();

        myPrices.stream().forEach(ep -> {
            if (ep.getUur() > firstToDischarge) {
                ep.setStatus(EPrice.ZERO_DISCHARGE_ONLY);
            }
        });
    }
}
