/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.world.item.ItemStack
 */
package lightning.modules.impl.core;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IPatchModule;
import lightning.settings.impl.BooleanSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;

@RegisterModule(name="Patch", description="Server/anticheat-specific hotfixes.", category=Module.Category.CORE, persistent=true, drawn=false)
public class PatchModule
extends Module
implements IPatchModule {
    public BooleanSetting grimAttackVelocity = new BooleanSetting("GrimAttackVelocity", "Stops your own sprint and movement from being cut after landing extra knockback on a hit.", false);
    public BooleanSetting silentSwapFix = new BooleanSetting("SilentSwapFix", "Stops hotbar slot desync and ghost item duplication during silent switch.", true);

    @Override
    public boolean isGrimAttackVelocity() {
        return this.grimAttackVelocity.getValue();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (PatchModule.mc.player == null) {
            return;
        }
        if (!this.silentSwapFix.getValue()) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (!(packet instanceof ClientboundContainerSetSlotPacket)) {
            return;
        }
        ClientboundContainerSetSlotPacket setSlotPacket = (ClientboundContainerSetSlotPacket)packet;
        if (setSlotPacket.getContainerId() != 0) {
            return;
        }
        int slot = setSlotPacket.getSlot();
        if (slot < 36 || slot > 44) {
            return;
        }
        int hotbarIndex = slot - 36;
        ItemStack packetStack = setSlotPacket.getItem();
        ItemStack inventoryStack = PatchModule.mc.player.getInventory().getItem(hotbarIndex);
        ItemStack mainHandStack = PatchModule.mc.player.getMainHandItem();
        if (!packetStack.isEmpty() && !inventoryStack.isEmpty() && packetStack.getItem() == inventoryStack.getItem() && packetStack.getCount() == inventoryStack.getCount()) {
            packetReceiveEvent.setCancelled(true);
            return;
        }
        if (PatchModule.mc.gui.screen() == null && !inventoryStack.isEmpty() && !packetStack.isEmpty() && inventoryStack.getItem() != packetStack.getItem()) {
            int selectedSlot = PatchModule.mc.player.getInventory().getSelectedSlot();
            int serverSlot = Night.POSITION_MANAGER.getServerSlot();
            if (hotbarIndex != selectedSlot && (packetStack.getItem() == mainHandStack.getItem() || serverSlot >= 0 && serverSlot <= 8 && packetStack.getItem() == PatchModule.mc.player.getInventory().getItem(serverSlot).getItem())) {
                packetReceiveEvent.setCancelled(true);
                return;
            }
        }
    }
}

