/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.PlayerMoveC2SPacketAccessor
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 */
package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

@RegisterModule(name="AntiHunger", description="Reduces the amount of hunger consumption.", category=Module.Category.PLAYER)
public class AntiHungerModule
extends Module {
    public BooleanSetting ground = new BooleanSetting("Ground", "Modifies movement packets to decrease hunger consumption.", true);
    public BooleanSetting sprint = new BooleanSetting("Sprint", "Spoofs sprinting packets to decrease hunger consumption.", true);
    private boolean lastOnGround = false;
    private boolean ignore = false;

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (AntiHungerModule.mc.player != null && AntiHungerModule.mc.level != null) {
            if (this.ignore && packetSendEvent.getPacket() instanceof ServerboundMovePlayerPacket) {
                this.ignore = false;
            } else if (!(AntiHungerModule.mc.player.isPassenger() || AntiHungerModule.mc.player.isInWater() || AntiHungerModule.mc.player.isUnderWater())) {
                ServerboundMovePlayerPacket serverboundMovePlayerPacket;
                Packet<?> packet = packetSendEvent.getPacket();
                if (packet instanceof ServerboundMovePlayerPacket) {
                    serverboundMovePlayerPacket = (ServerboundMovePlayerPacket)packet;
                    if (this.ground.getValue() && AntiHungerModule.mc.player.onGround() && AntiHungerModule.mc.player.fallDistance <= 0.0 && !AntiHungerModule.mc.gameMode.isDestroying()) {
                        ((PlayerMoveC2SPacketAccessor)serverboundMovePlayerPacket).setOnGround(false);
                    }
                }
                if ((packet = packetSendEvent.getPacket()) instanceof ServerboundPlayerCommandPacket serverboundPlayerCommandPacket) {
                    if (this.sprint.getValue() && serverboundPlayerCommandPacket.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
                        packetSendEvent.setCancelled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent updateMovementEvent) {
        if (AntiHungerModule.mc.player == null || AntiHungerModule.mc.level == null) {
            return;
        }
        if (AntiHungerModule.mc.player.onGround() && !this.lastOnGround && this.ground.getValue()) {
            this.ignore = true;
        }
        this.lastOnGround = AntiHungerModule.mc.player.onGround();
    }

    @Override
    public void onEnable() {
        if (AntiHungerModule.mc.player == null) {
            return;
        }
        this.lastOnGround = AntiHungerModule.mc.player.onGround();
    }
}

