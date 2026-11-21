/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 */
package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

@RegisterModule(name="XCarry", description="Allows you to carry items in your crafting slots.", category=Module.Category.PLAYER)
public class XCarryModule
extends Module {
    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (XCarryModule.mc.player == null) {
            return;
        }
        if (packetSendEvent.getPacket() instanceof ServerboundContainerClosePacket) {
            packetSendEvent.setCancelled(true);
        }
    }

    @Override
    public void onDisable() {
        if (XCarryModule.mc.player == null) {
            return;
        }
        mc.getConnection().send((Packet)new ServerboundContainerClosePacket(XCarryModule.mc.player.inventoryMenu.containerId));
    }
}

