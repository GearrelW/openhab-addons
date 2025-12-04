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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class Payload {
    @SerializedName("status")
    private boolean status = true;

    @SerializedName("data")
    private List<PayloadPriceItem> prices = new ArrayList<PayloadPriceItem>();

    public boolean getStatus() {
        return status;
    }

    public LocalDate getDate() {
        return prices.getFirst().getDatum();
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<PayloadPriceItem> getPrices() {
        return prices;
    }
}
