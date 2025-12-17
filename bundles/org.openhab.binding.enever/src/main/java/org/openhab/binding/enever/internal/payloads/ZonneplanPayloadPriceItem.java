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

/**
 * Class that provides storage for the json objects obtained from EneVer.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */

public class ZonneplanPayloadPriceItem extends PayloadPriceItem {

    @Override
    public Double getPrijs() {
        try {
            return super.getPrijs() / 10000000.0;
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
