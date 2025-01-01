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
package org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaDeviceCommand;

/**
 * Class to implement the BlueGiga command <b>indicateConfirm</b>.
 * <p>
 * This command can be used to send an acknowledge a received indication from a remote device.
 * This function allows the application to manually confirm the indicated values instead of
 * the smart stack Bluetooth automatically doing it. The benefit of this is extra reliability
 * since the application can for example store the received value on the flash memory before
 * confirming the indication to the remote device.
 * <p>
 * This class provides methods for processing BlueGiga API commands.
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
@NonNullByDefault
public class BlueGigaIndicateConfirmCommand extends BlueGigaDeviceCommand {
    public static final int COMMAND_CLASS = 0x04;
    public static final int COMMAND_METHOD = 0x07;

    @Override
    public int[] serialize() {
        // Serialize the header
        serializeHeader(COMMAND_CLASS, COMMAND_METHOD);

        // Serialize the fields
        serializeUInt8(connection);

        return getPayload();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BlueGigaIndicateConfirmCommand [connection=");
        builder.append(connection);
        builder.append(']');
        return builder.toString();
    }
}
