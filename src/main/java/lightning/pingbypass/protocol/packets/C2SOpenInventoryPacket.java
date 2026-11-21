/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SOpenInventoryPacket
extends PbPacket {
    public static final int ID = 15;
    private final boolean open;

    public C2SOpenInventoryPacket(boolean bl) {
        this.open = bl;
    }

    public C2SOpenInventoryPacket(FriendlyByteBuf friendlyByteBuf) {
        this.open = friendlyByteBuf.readBoolean();
    }

    @Override
    public int getPacketId() {
        return 15;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(this.open);
    }

    public boolean isOpen() {
        return this.open;
    }
}

