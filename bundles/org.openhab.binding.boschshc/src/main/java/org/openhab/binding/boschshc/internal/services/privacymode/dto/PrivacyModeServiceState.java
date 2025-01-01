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
package org.openhab.binding.boschshc.internal.services.privacymode.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.dto.EnabledDisabledState;

/**
 * Represents the privacy mode of cameras as reported by the Smart Home Controller.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class PrivacyModeServiceState extends BoschSHCServiceState {

    public PrivacyModeServiceState() {
        super("privacyModeState");
    }

    /**
     * The name of this member has to be <code>value</code>, otherwise JSON requests and responses can not be
     * serialized/deserialized. The JSON message looks like this:
     * 
     * <pre>
     * {"@type":"privacyModeState","value":"ENABLED"}
     * </pre>
     */
    public EnabledDisabledState value;
}
