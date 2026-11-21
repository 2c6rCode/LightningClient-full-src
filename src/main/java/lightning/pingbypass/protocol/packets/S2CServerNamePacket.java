/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CServerNamePacket
extends PbPacket {
    public static final int ID = 8;
    private final String serverIp;

    public S2CServerNamePacket(String string) {
        this.serverIp = string;
    }

    public S2CServerNamePacket(FriendlyByteBuf friendlyByteBuf) {
        this.serverIp = friendlyByteBuf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 8;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.serverIp);
    }

    public String getServerIp() {
        return this.serverIp;
    }
}

