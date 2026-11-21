/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CErrorPacket
extends PbPacket {
    public static final int ID = 7;
    private final String message;

    public S2CErrorPacket(String string) {
        this.message = string;
    }

    public S2CErrorPacket(FriendlyByteBuf friendlyByteBuf) {
        this.message = friendlyByteBuf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 7;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.message);
    }

    public String getMessage() {
        return this.message;
    }
}

