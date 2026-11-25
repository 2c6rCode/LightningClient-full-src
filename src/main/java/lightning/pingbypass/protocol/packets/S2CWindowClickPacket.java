/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.inventory.ContainerInput
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerInput;

public class S2CWindowClickPacket
extends PbPacket {
    public static final int ID = 16;
    private final int containerId;
    private final int slotNum;
    private final int buttonNum;
    private final String containerInput;

    public S2CWindowClickPacket(int containerId, int slotNum, int buttonNum, ContainerInput containerInput) {
        this.containerId = containerId;
        this.slotNum = slotNum;
        this.buttonNum = buttonNum;
        this.containerInput = containerInput.name();
    }

    public S2CWindowClickPacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readVarInt();
        this.slotNum = friendlyByteBuf.readVarInt();
        this.buttonNum = friendlyByteBuf.readVarInt();
        this.containerInput = friendlyByteBuf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 16;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.containerId);
        friendlyByteBuf.writeVarInt(this.slotNum);
        friendlyByteBuf.writeVarInt(this.buttonNum);
        friendlyByteBuf.writeUtf(this.containerInput);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlotNum() {
        return this.slotNum;
    }

    public int getButtonNum() {
        return this.buttonNum;
    }

    public ContainerInput getContainerInput() {
        return ContainerInput.valueOf((String)this.containerInput);
    }
}

