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
package org.openhab.binding.homewizard.internal.devices;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homewizard.internal.dto.DataUtil;

/**
 * Tests deserialization of HomeWizard System API responses from JSON.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */

@NonNullByDefault
public class HomeWizardSystemPayloadTest {

    private static final DataUtil DATA_UTIL = new DataUtil();

    @Test
    public void deserializeResponse() throws IOException {
        HomeWizardSystemPayload key = DATA_UTIL.fromJson("response-system.json", HomeWizardSystemPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getWifiSSID(), is("My Wi-Fi"));
        assertThat(key.getWifiRSSI(), is(-77));
        assertThat(key.getCloudEnabled(), is(false));
        assertThat(key.getStatusLEDBrightness(), is(100));
    }

    @Test
    public void deserializeResponseEmpty() throws IOException {
        HomeWizardSystemPayload key = DATA_UTIL.fromJson("response-empty.json", HomeWizardSystemPayload.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.getWifiSSID(), is(""));
        assertThat(key.getWifiRSSI(), is(0));
        assertThat(key.getCloudEnabled(), is(true));
        assertThat(key.getStatusLEDBrightness(), is(0));
    }
}
