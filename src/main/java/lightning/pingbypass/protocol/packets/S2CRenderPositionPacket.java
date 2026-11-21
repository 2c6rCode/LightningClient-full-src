/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  org.jetbrains.annotations.Nullable
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class S2CRenderPositionPacket
extends PbPacket {
    public static final int ID = 10;
    @Nullable
    private final BlockPos position;

    public S2CRenderPositionPacket(@Nullable BlockPos blockPos) {
        this.position = blockPos;
    }

    public S2CRenderPositionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.position = friendlyByteBuf.readBoolean() ? new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt()) : null;
    }

    @Override
    public int getPacketId() {
        return 10;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(this.position != null);
        if (this.position != null) {
            friendlyByteBuf.writeInt(this.position.getX());
            friendlyByteBuf.writeInt(this.position.getY());
            friendlyByteBuf.writeInt(this.position.getZ());
        }
    }

    @Nullable
    public BlockPos getPosition() {
        return this.position;
    }
}

