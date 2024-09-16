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
package org.openhab.binding.intergas_gateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Intergas_GatewayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class Intergas_GatewayBindingConstants {

    private static final String BINDING_ID = "intergas_gateway";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids
    public static final String CHANNEL_ROOM_TEMPERATURE = "room-temperature";
    public static final String CHANNEL_SET_POINT = "room-set-point";
    public static final String CHANNEL_HEATING = "heating";
}
