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

public class S2CMiningStatePacket
extends PbPacket {
    public static final int ID = 12;
    private final boolean hasPrimary;
    @Nullable
    private final BlockPos primaryPos;
    private final float primaryProgress;
    private final boolean hasSecondary;
    @Nullable
    private final BlockPos secondaryPos;
    private final float secondaryProgress;

    public S2CMiningStatePacket(@Nullable BlockPos blockPos, float f, @Nullable BlockPos blockPos2, float f2) {
        this.hasPrimary = blockPos != null;
        this.primaryPos = blockPos;
        this.primaryProgress = f;
        this.hasSecondary = blockPos2 != null;
        this.secondaryPos = blockPos2;
        this.secondaryProgress = f2;
    }

    public S2CMiningStatePacket(FriendlyByteBuf friendlyByteBuf) {
        this.hasPrimary = friendlyByteBuf.readBoolean();
        if (this.hasPrimary) {
            this.primaryPos = new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
            this.primaryProgress = friendlyByteBuf.readFloat();
        } else {
            this.primaryPos = null;
            this.primaryProgress = 0.0f;
        }
        this.hasSecondary = friendlyByteBuf.readBoolean();
        if (this.hasSecondary) {
            this.secondaryPos = new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
            this.secondaryProgress = friendlyByteBuf.readFloat();
        } else {
            this.secondaryPos = null;
            this.secondaryProgress = 0.0f;
        }
    }

    @Override
    public int getPacketId() {
        return 12;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(this.hasPrimary);
        if (this.hasPrimary) {
            friendlyByteBuf.writeInt(this.primaryPos.getX());
            friendlyByteBuf.writeInt(this.primaryPos.getY());
            friendlyByteBuf.writeInt(this.primaryPos.getZ());
            friendlyByteBuf.writeFloat(this.primaryProgress);
        }
        friendlyByteBuf.writeBoolean(this.hasSecondary);
        if (this.hasSecondary) {
            friendlyByteBuf.writeInt(this.secondaryPos.getX());
            friendlyByteBuf.writeInt(this.secondaryPos.getY());
            friendlyByteBuf.writeInt(this.secondaryPos.getZ());
            friendlyByteBuf.writeFloat(this.secondaryProgress);
        }
    }

    public boolean hasPrimary() {
        return this.hasPrimary;
    }

    @Nullable
    public BlockPos getPrimaryPos() {
        return this.primaryPos;
    }

    public float getPrimaryProgress() {
        return this.primaryProgress;
    }

    public boolean hasSecondary() {
        return this.hasSecondary;
    }

    @Nullable
    public BlockPos getSecondaryPos() {
        return this.secondaryPos;
    }

    public float getSecondaryProgress() {
        return this.secondaryProgress;
    }
}

