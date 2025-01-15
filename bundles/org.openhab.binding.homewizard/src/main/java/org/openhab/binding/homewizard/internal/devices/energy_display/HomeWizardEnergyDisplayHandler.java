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
package org.openhab.binding.homewizard.internal.devices.energy_display;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homewizard.internal.devices.HomeWizardDeviceHandler;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeWizardEnergyDisplayHandler} implements functionality to handle a HomeWizard Energy Display.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardEnergyDisplayHandler extends HomeWizardDeviceHandler {

    /**
     * Constructor
     *
     * @param thing The thing to handle
     *
     */
    public HomeWizardEnergyDisplayHandler(Thing thing) {
        super(thing);
        supportedTypes.add("HWE-DSP");
    }

    /**
     * Device specific handling of the returned data.
     *
     * @param data The data obtained from the API call
     */
    @Override
    protected void processMeasurementData(String data) {

        // var payload = gson.fromJson(data, HomeWizardEnergySocketMeasurementPayload.class);
        // if (payload != null) {
        // updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
        // HomeWizardBindingConstants.CHANNEL_REACTIVE_POWER,
        // new QuantityType<>(payload.getReactivePower(), Units.VAR));
        // updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
        // HomeWizardBindingConstants.CHANNEL_APPARENT_POWER,
        // new QuantityType<>(payload.getApparentPower(), Units.VOLT_AMPERE));
        // updateState(HomeWizardBindingConstants.CHANNEL_GROUP_ENERGY,
        // HomeWizardBindingConstants.CHANNEL_POWER_FACTOR, new DecimalType(payload.getPowerFactor()));
        // }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }
}
