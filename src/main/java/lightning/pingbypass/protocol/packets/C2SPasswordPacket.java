/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SPasswordPacket
extends PbPacket {
    public static final int ID = 2;
    private final String password;

    public C2SPasswordPacket(String password) {
        this.password = password;
    }

    public C2SPasswordPacket(FriendlyByteBuf friendlyByteBuf) {
        this.password = friendlyByteBuf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 2;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.password);
    }

    public String getPassword() {
        return this.password;
    }
}

