/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 */
package lightning.utils.minecraft;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class EChestMemory {
    private static final EChestMemory INSTANCE = new EChestMemory();
    private static boolean registered = false;
    private static NonNullList<ItemStack> items = null;
    private int trackedContainerId = -1;

    private EChestMemory() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        registered = true;
        Night.EVENT_HANDLER.subscribe(INSTANCE);
    }

    public static boolean hasItems() {
        return items != null;
    }

    public static NonNullList<ItemStack> getItems() {
        return items;
    }

    @SubscribeEvent
    public void onPacket(PacketReceiveEvent packetReceiveEvent) {
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundOpenScreenPacket openScreenPacket) {
            if (this.isLookingAtEnderChest()) {
                this.trackedContainerId = openScreenPacket.getContainerId();
                items = NonNullList.withSize(27, ItemStack.EMPTY);
            } else {
                this.trackedContainerId = -1;
            }
        } else if (this.trackedContainerId != -1) {
            if (packet instanceof ClientboundContainerSetContentPacket contentPacket) {
                if (contentPacket.containerId() == this.trackedContainerId) {
                    NonNullList<ItemStack> newItems = NonNullList.withSize(27, ItemStack.EMPTY);
                    for (int i = 0; i < 27 && i < contentPacket.items().size(); ++i) {
                        newItems.set(i, contentPacket.items().get(i).copy());
                    }
                    items = newItems;
                }
            } else if (packet instanceof ClientboundContainerSetSlotPacket slotPacket) {
                if (slotPacket.getContainerId() != this.trackedContainerId) {
                    return;
                }
                int slot = slotPacket.getSlot();
                if (items != null && slot >= 0 && slot < 27) {
                    items.set(slot, slotPacket.getItem().copy());
                }
            }
        }
    }

    private boolean isLookingAtEnderChest() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }
        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult)) {
            return false;
        }
        BlockHitResult blockHitResult = (BlockHitResult)hitResult;
        BlockPos blockPos = blockHitResult.getBlockPos();
        return minecraft.level.getBlockState(blockPos).is(Blocks.ENDER_CHEST);
    }
}

