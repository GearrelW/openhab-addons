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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Class that provides storage for the json objects obtained from EneVer.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
public class ZonneplanPayload implements IPayload {
    protected final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private @Nullable List<PayloadPriceItem> eprices = new ArrayList<PayloadPriceItem>();
    private @Nullable List<PayloadPriceItem> gprices = new ArrayList<PayloadPriceItem>();

    public ZonneplanPayload(String payload) {
        var ed = JsonParser.parseString(payload).getAsJsonObject().get("pageProps").getAsJsonObject().get("data")
                .getAsJsonObject().get("templateProps").getAsJsonObject().get("energyData").getAsJsonObject();
        var ep = ed.get("electricity").getAsJsonObject().get("hours");
        var gp = ed.get("gas").getAsJsonObject().get("days");

        Type listType = new TypeToken<ArrayList<ZonneplanPayloadPriceItem>>() {
        }.getType();
        this.eprices = gson.fromJson(ep.toString(), listType);
        this.gprices = gson.fromJson(gp.toString(), listType);
    }

    @Override
    public List<PayloadPriceItem> getElectricityPrices() {
        return eprices;
    }

    @Override
    public List<PayloadPriceItem> getGasPrices() {
        return gprices;
    }

    @Override
    public boolean getStatus() {
        return eprices.size() > 0;
    }
}
