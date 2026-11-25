/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.EarlyTickHooks;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="AirStuck", description="Suspends movement in the air by freezing movement packets.", category=Module.Category.MOVEMENT)
public class AirStuckModule
extends Module {
    public final BooleanSetting onlyWhenFalling = new BooleanSetting("Only When Falling", "Only captures velocity while falling.", false);
    private Vec3 savedVelocity = Vec3.ZERO;
    private final Runnable earlyTickCallback = this::onEarlyTick;

    @Override
    public void onEnable() {
        EarlyTickHooks.register(this.earlyTickCallback);
    }

    @Override
    public void onDisable() {
        EarlyTickHooks.unregister(this.earlyTickCallback);
        if (!this.getNull() && !this.savedVelocity.equals((Object)Vec3.ZERO)) {
            AirStuckModule.mc.player.setDeltaMovement(this.savedVelocity);
        }
        this.savedVelocity = Vec3.ZERO;
    }

    private void onEarlyTick() {
        if (!this.isToggled() || this.getNull()) {
            return;
        }
        if (!(this.onlyWhenFalling.getValue() && AirStuckModule.mc.player.fallDistance <= 0.0 || !this.savedVelocity.equals((Object)Vec3.ZERO) || AirStuckModule.mc.player.isSprinting())) {
            this.savedVelocity = AirStuckModule.mc.player.getDeltaMovement();
        }
    }

    private boolean isActive() {
        return this.isToggled() && !this.getNull() && !this.savedVelocity.equals((Object)Vec3.ZERO);
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (!this.isActive()) {
            return;
        }
        Packet<?> packet = packetSendEvent.getPacket();
        if (packet instanceof ServerboundClientTickEndPacket || packet instanceof ServerboundPlayerCommandPacket || packet instanceof ServerboundMovePlayerPacket) {
            packetSendEvent.setCancelled(true);
        }
    }

    private static AirStuckModule instance() {
        return Night.MODULE_MANAGER == null ? null : Night.MODULE_MANAGER.getModule(AirStuckModule.class);
    }

    public static Vec3 movementDisplacement(Vec3 displacement) {
        AirStuckModule airStuckModule = AirStuckModule.instance();
        return airStuckModule != null && airStuckModule.isActive() ? new Vec3(0.0, -1.0E-10, 0.0) : displacement;
    }

    public static void localPlayerConstructed() {
        AirStuckModule airStuckModule = AirStuckModule.instance();
        if (airStuckModule != null && airStuckModule.isToggled()) {
            airStuckModule.setToggled(false, false);
        }
    }
}

