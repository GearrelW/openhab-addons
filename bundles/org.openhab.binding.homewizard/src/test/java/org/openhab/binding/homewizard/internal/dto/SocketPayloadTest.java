/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests deserialization of HomeWizard API responses from JSON.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class SocketPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        DataPayload key = DATA_UTIL.fromJson("Socket-response.json", DataPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getActiveCurrent(), is(2.346));
        assertThat(key.getActiveCurrentL1(), is(0.0));
        assertThat(key.getActiveCurrentL2(), is(0.0));
        assertThat(key.getActiveCurrentL3(), is(0.0));
        assertThat(key.getActivePowerW(), is(543.312));
        assertThat(key.getActivePowerL1W(), is(543.312));
        assertThat(key.getActivePowerL2W(), is(0.0));
        assertThat(key.getActivePowerL3W(), is(0.0));
        assertThat(key.getActiveVoltage(), is(231.539));
        assertThat(key.getActiveVoltageL1(), is(0.0));
        assertThat(key.getActiveVoltageL2(), is(0.0));
        assertThat(key.getActiveVoltageL3(), is(0.0));
        assertThat(key.getTotalEnergyExportKwh(), is(85.951));
        assertThat(key.getTotalEnergyExportT1Kwh(), is(85.951));
        assertThat(key.getTotalEnergyImportKwh(), is(30.511));
        assertThat(key.getTotalEnergyImportT1Kwh(), is(30.511));
        assertThat(key.getTotalEnergyImportT2Kwh(), is(0.0));
        assertThat(key.getAnyPowerFailCount(), is(0));
        assertThat(key.getLongPowerFailCount(), is(0));
        assertNull(key.getGasTimestamp(ZoneId.systemDefault()));
        assertThat(key.getTotalGasM3(), is(0.0));

        assertThat(key.getMeterModel(), is(""));
        assertThat(key.getSmrVersion(), is(0));
        assertThat(key.getWifiSsid(), is("My Wi-Fi"));
        assertThat(key.getWifiStrength(), is(100));

    }

}
