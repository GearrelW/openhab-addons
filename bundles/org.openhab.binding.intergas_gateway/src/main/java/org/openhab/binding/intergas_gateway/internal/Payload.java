package org.openhab.binding.intergas_gateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class Payload {
    @SerializedName("nodenr")
    private int nodeNr = 0;
    @SerializedName("room_temp_1_lsb")
    private int roomTemp1Lsb = 0;
    @SerializedName("room_temp_1_msb")
    private int roomTemp1Msb = 0;
    @SerializedName("room_temp_set_1_lsb")
    private int roomSetTemp1Lsb = 0;
    @SerializedName("room_temp_set_1_msb")
    private int roomSetTemp1Msb = 0;
    @SerializedName("IO")
    private int io = 0;

    public int getNodeNr() {
        return nodeNr;
    }

    public void setNodeNr(int nodeNr) {
        this.nodeNr = nodeNr;
    }

    public double getRoomTemp1Lsb() {
        return roomTemp1Lsb;
    }

    public void setRoomTemp1Lsb(int tempLsb) {
        this.roomTemp1Lsb = tempLsb;
    }

    public int getRoomTemp1Msb() {
        return roomTemp1Msb;
    }

    public void setRoomTemp1Msb(int tempMsb) {
        this.roomTemp1Msb = tempMsb;
    }

    public double getRoomSetTemp1Lsb() {
        return roomSetTemp1Lsb;
    }

    public void setRoomSetTemp1Lsb(int tempLsb) {
        this.roomSetTemp1Lsb = tempLsb;
    }

    public int getRoomSetTemp1Msb() {
        return roomSetTemp1Msb;
    }

    public void setRoomSetTemp1Msb(int tempMsb) {
        this.roomSetTemp1Msb = tempMsb;
    }

    public int getIo() {
        return io;
    }

    public void setIo(int io) {
        this.io = io;
    }

    public double getRoomTemperature() {
        return ((double) roomTemp1Msb * 256 + roomTemp1Lsb) / 100;
    }

    public double getRoomSetPointTemperature() {
        return ((double) roomSetTemp1Msb * 256 + roomSetTemp1Lsb) / 100;
    }

    @Override
    public String toString() {
        return String.format(
                "Data [nodeNr: %d, roomTemp1Lsb: %d, roomTemp1Msb: %d, roomSetTemp1Lsb: %d, roomSetTemp1Msb: %d, io: %d] ",
                nodeNr, roomTemp1Lsb, roomTemp1Msb, roomSetTemp1Lsb, roomSetTemp1Msb, io);
    }
}