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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EneVerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class EneVerBindingConstants {

    private static final String BINDING_ID = "enever";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENEVER = new ThingTypeUID(BINDING_ID, "enever");

    // List of all Channel ids
    public static final String CHANNEL_ELECTRICITY_HOURLY_PRICE = "electricity-price";
    public static final String CHANNEL_ELECTRICITY_HOURLY_PRICE_PLUS_1 = "electricity-price-plus-1";
    public static final String CHANNEL_ELECTRICITY_HOURLY_PRICE_PLUS_2 = "electricity-price-plus-2";
    public static final String CHANNEL_AVG_ELECTRICITY_PRICE = "average-electricity-price";
    public static final String CHANNEL_GAS_DAILY_PRICE = "gas-price";
    public static final String CHANNEL_HOUR_INDICATION = "indication-hour";
    public static final String CHANNEL_BATTERY_STATUS = "battery-status";
    public static final String CHANNEL_BATTERY_STATUS_MODE = "battery-status-mode";
    public static final String CHANNEL_PRICE_WARNING = "warning-hour";
    public static final String CHANNEL_PEAK_HOUR = "peak-hour";
}
