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
package org.openhab.binding.homewizard.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.homewizard.internal.dto.DataUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * Tests for the HomeWizard Energy Socket Handler
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardEnergySocketHandlerTest {

    private static final Configuration CONFIG = createConfig();

    private static Configuration createConfig() {
        final Configuration config = new Configuration();
        config.put("ipAddress", "1.2.3.4");
        return config;
    }

    private static Thing mockThing() {
        final Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(
                new ThingUID(HomeWizardBindingConstants.THING_TYPE_ENERGY_SOCKET, "homewizard-test-sockt-thing"));
        when(thing.getConfiguration()).thenReturn(CONFIG);

        final List<Channel> channelList = Arrays.asList(
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1), //
                mockChannel(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1)); //

        when(thing.getChannels()).thenReturn(channelList);
        return thing;
    }

    private static Channel mockChannel(final ThingUID thingId, final String channelId) {
        final Channel channel = Mockito.mock(Channel.class);
        when(channel.getUID()).thenReturn(new ChannelUID(thingId, channelId));
        return channel;
    }

    private static HomeWizardEnergySocketHandlerMock createAndInitHandler(final ThingHandlerCallback callback,
            final Thing thing) {
        final TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        doReturn(ZoneId.systemDefault()).when(timeZoneProvider).getTimeZone();
        final HomeWizardEnergySocketHandlerMock handler = spy(
                new HomeWizardEnergySocketHandlerMock(thing, timeZoneProvider));

        try {
            doReturn(DataUtil.fromFile("Socket-response.json")).when(handler).getData();
        } catch (IOException e) {
            assertFalse(true);
        }

        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    private static State getState(final double input, Unit<?> unit) {
        return new QuantityType<>(input, unit);
    }

    @Test
    public void testUpdateChannels() {
        final Thing thing = mockThing();
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final HomeWizardEnergySocketHandlerMock handler = createAndInitHandler(callback, thing);

        try {
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER),
                    getState(543.312, Units.WATT));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1),
                    getState(30.511, Units.KILOWATT_HOUR));
            verify(callback).stateUpdated(
                    new ChannelUID(thing.getUID(), HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1),
                    getState(85.951, Units.KILOWATT_HOUR));

        } finally {
            handler.dispose();
        }
    }
}
