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
        ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket = (ClientboundContainerSetSlotPacket)packet;
        if (clientboundContainerSetSlotPacket.getContainerId() != 0) {
            return;
        }
        int n = clientboundContainerSetSlotPacket.getSlot();
        if (n < 36 || n > 44) {
            return;
        }
        int n2 = n - 36;
        ItemStack itemStack = clientboundContainerSetSlotPacket.getItem();
        ItemStack itemStack2 = PatchModule.mc.player.getInventory().getItem(n2);
        ItemStack itemStack3 = PatchModule.mc.player.getMainHandItem();
        if (!itemStack.isEmpty() && !itemStack2.isEmpty() && itemStack.getItem() == itemStack2.getItem() && itemStack.getCount() == itemStack2.getCount()) {
            packetReceiveEvent.setCancelled(true);
            return;
        }
        if (PatchModule.mc.gui.screen() == null && !itemStack2.isEmpty() && !itemStack.isEmpty() && itemStack2.getItem() != itemStack.getItem()) {
            int n3 = PatchModule.mc.player.getInventory().getSelectedSlot();
            int n4 = Night.POSITION_MANAGER.getServerSlot();
            if (n2 != n3 && (itemStack.getItem() == itemStack3.getItem() || n4 >= 0 && n4 <= 8 && itemStack.getItem() == PatchModule.mc.player.getInventory().getItem(n4).getItem())) {
                packetReceiveEvent.setCancelled(true);
                return;
            }
        }
    }
}

