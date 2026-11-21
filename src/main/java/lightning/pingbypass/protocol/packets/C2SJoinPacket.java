/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SJoinPacket
extends PbPacket {
    public static final int ID = 0;
    private final String serverIp;
    private final int serverPort;

    public C2SJoinPacket(String string, int n) {
        this.serverIp = string;
        this.serverPort = n;
    }

    public C2SJoinPacket(FriendlyByteBuf friendlyByteBuf) {
        this.serverIp = friendlyByteBuf.readUtf();
        this.serverPort = friendlyByteBuf.readVarInt();
    }

    @Override
    public int getPacketId() {
        return 0;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.serverIp);
        friendlyByteBuf.writeVarInt(this.serverPort);
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public int getServerPort() {
        return this.serverPort;
    }
}

