/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.PlayerPositionAccessor
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Relative
 */
package lightning.modules.impl.player;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.mixins.accessors.PlayerPositionAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.minecraft.PositionUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;

@RegisterModule(name="NoRotate", description="Prevents the server from forcing rotations on you.", category=Module.Category.PLAYER)
public class NoRotateModule
extends Module {
    public BooleanSetting inBlocks = new BooleanSetting("InBlocks", "Whether or not to stop rotations whenever inside of a block.", false);
    public BooleanSetting spoof = new BooleanSetting("Spoof", "Sends rotation packets once you have been rubberbanded.", false);

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (NoRotateModule.mc.player == null || NoRotateModule.mc.level == null) {
            return;
        }
        if (!this.inBlocks.getValue() && !NoRotateModule.mc.level.getBlockState(PositionUtils.getFlooredPosition((Entity)NoRotateModule.mc.player)).canBeReplaced()) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket clientboundPlayerPositionPacket = (ClientboundPlayerPositionPacket)packet;
            if (this.spoof.getValue()) {
                Night.ROTATION_MANAGER.packetRotate(clientboundPlayerPositionPacket.change().yRot(), clientboundPlayerPositionPacket.change().xRot());
                Night.ROTATION_MANAGER.packetRotate(NoRotateModule.mc.player.getYRot(), NoRotateModule.mc.player.getXRot());
            }
            ((PlayerPositionAccessor)(Object)clientboundPlayerPositionPacket.change()).setYaw(NoRotateModule.mc.player.getYRot());
            ((PlayerPositionAccessor)(Object)clientboundPlayerPositionPacket.change()).setPitch(NoRotateModule.mc.player.getXRot());
            clientboundPlayerPositionPacket.relatives().remove(Relative.X_ROT);
            clientboundPlayerPositionPacket.relatives().remove(Relative.Y_ROT);
        }
    }
}

