/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class S2CBlockRenderPacket
extends PbPacket {
    public static final int ID = 11;
    private final BlockPos position;

    public S2CBlockRenderPacket(BlockPos blockPos) {
        this.position = blockPos;
    }

    public S2CBlockRenderPacket(FriendlyByteBuf friendlyByteBuf) {
        this.position = new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
    }

    @Override
    public int getPacketId() {
        return 11;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.position.getX());
        friendlyByteBuf.writeInt(this.position.getY());
        friendlyByteBuf.writeInt(this.position.getZ());
    }

    public BlockPos getPosition() {
        return this.position;
    }
}

