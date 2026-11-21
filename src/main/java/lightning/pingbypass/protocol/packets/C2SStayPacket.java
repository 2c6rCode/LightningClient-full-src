/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SStayPacket
extends PbPacket {
    public static final int ID = 9;
    private final boolean stay;

    public C2SStayPacket(boolean bl) {
        this.stay = bl;
    }

    public C2SStayPacket(FriendlyByteBuf friendlyByteBuf) {
        this.stay = friendlyByteBuf.readBoolean();
    }

    @Override
    public int getPacketId() {
        return 9;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(this.stay);
    }

    public boolean isStay() {
        return this.stay;
    }
}

