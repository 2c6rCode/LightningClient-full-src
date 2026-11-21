/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CPasswordRequestPacket
extends PbPacket {
    public static final int ID = 1;

    public S2CPasswordRequestPacket() {
    }

    public S2CPasswordRequestPacket(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public int getPacketId() {
        return 1;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
    }
}

