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
package org.openhab.binding.enever.internal.payloads;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from EneVer.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
public class EneVerPayload implements IPayload {
    @SerializedName("status")
    private boolean status = true;

    @SerializedName("data")
    private List<PayloadPriceItem> prices = new ArrayList<PayloadPriceItem>();

    @Override
    public boolean getStatus() {
        return status;
    }

    @Override
    public List<PayloadPriceItem> getElectricityPrices() {
        if (prices.size() > 1) {
            return prices;
        }
        return new ArrayList<PayloadPriceItem>();
    }

    @Override
    public List<PayloadPriceItem> getGasPrices() {
        if (prices.size() == 1) {
            return prices;
        }
        return new ArrayList<PayloadPriceItem>();
    }
}
