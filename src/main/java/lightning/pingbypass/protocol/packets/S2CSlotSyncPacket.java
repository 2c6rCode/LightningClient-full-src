/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CSlotSyncPacket
extends PbPacket {
    public static final int ID = 13;
    private final int slot;

    public S2CSlotSyncPacket(int slot) {
        this.slot = slot;
    }

    public S2CSlotSyncPacket(FriendlyByteBuf friendlyByteBuf) {
        this.slot = friendlyByteBuf.readVarInt();
    }

    @Override
    public int getPacketId() {
        return 13;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.slot);
    }

    public int getSlot() {
        return this.slot;
    }
}

