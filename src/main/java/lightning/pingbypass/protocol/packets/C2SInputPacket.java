/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SInputPacket
extends PbPacket {
    public static final int ID = 10;
    public static final int TYPE_MOUSE = 0;
    public static final int TYPE_KEY = 1;
    public static final int ACTION_PRESS = 1;
    public static final int ACTION_RELEASE = 0;
    private final int inputType;
    private final int button;
    private final int action;

    public C2SInputPacket(int n, int n2, int n3) {
        this.inputType = n;
        this.button = n2;
        this.action = n3;
    }

    public C2SInputPacket(FriendlyByteBuf friendlyByteBuf) {
        this.inputType = friendlyByteBuf.readVarInt();
        this.button = friendlyByteBuf.readVarInt();
        this.action = friendlyByteBuf.readVarInt();
    }

    @Override
    public int getPacketId() {
        return 10;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.inputType);
        friendlyByteBuf.writeVarInt(this.button);
        friendlyByteBuf.writeVarInt(this.action);
    }

    public int getInputType() {
        return this.inputType;
    }

    public int getButton() {
        return this.button;
    }

    public int getAction() {
        return this.action;
    }
}

