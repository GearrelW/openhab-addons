/**
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
package org.openhab.binding.prowl.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ProwlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class ProwlBindingConstants {

    private static final String BINDING_ID = "prowl";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BROKER = new ThingTypeUID(BINDING_ID, "broker");

    // List of all Channel ids
    public static final String CHANNEL_REMAINING = "remaining";

    // constants
    public static final String PROWL_ADD_URI = "https://api.prowlapp.com/publicapi/add";
    public static final String PROWL_VERIFY_URI = "https://api.prowlapp.com/publicapi/verify";
}
