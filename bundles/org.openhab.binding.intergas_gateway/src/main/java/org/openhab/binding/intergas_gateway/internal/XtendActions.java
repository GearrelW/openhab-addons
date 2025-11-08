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
package org.openhab.binding.intergas_gateway.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = XtendActions.class)
@ThingActionsScope(name = "intergas_gateway")
@NonNullByDefault
public class XtendActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(XtendActions.class);
    private @Nullable IntergasGatewayHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (IntergasGatewayHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Set Prices", description = "Set Prices")
    public void setPrices(@ActionInput(name = "gasPrice") int gasPrice,
            @ActionInput(name = "electrcityPrice") int electrictyPrice) {

        try {
            var url = String.format("%s?e=%d&g=%d", handler.xtendURL, electrictyPrice, gasPrice);
            HttpUtil.executeUrl("GET", url, 30000);
        } catch (IOException e) {
        }

    }

    public static void setPrices(ThingActions actions, int gasPrice, int electricityPrice) {
        ((XtendActions) actions).setPrices(gasPrice, electricityPrice);
    }
}
