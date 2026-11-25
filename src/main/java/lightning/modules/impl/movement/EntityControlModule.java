/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector2d
 */
package lightning.modules.impl.movement;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.MovementUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

@RegisterModule(name="EntityControl", description="Allows you to control and fly with ridden entities.", category=Module.Category.MOVEMENT)
public class EntityControlModule
extends Module {
    public static EntityControlModule INSTANCE;
    public BooleanSetting fly = new BooleanSetting("Fly", "Allows you to fly with your vehicle.", true);
    public NumberSetting speed = new NumberSetting("Speed", "Horizontal flying speed.", 5.0, 0.1, 50.0);
    public NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", "Vertical flying speed.", 6.0, 0.0, 20.0);
    public NumberSetting fallSpeed = new NumberSetting("FallSpeed", "Glide down speed when no vertical key is pressed.", 0.1, 0.0, 50.0);
    public BooleanSetting control = new BooleanSetting("Control", "Steer entities even without a saddle.", true);
    public BooleanSetting noSync = new BooleanSetting("NoSync", "Cancels server vehicle sync packets to prevent rubberbanding.", false);
    private Entity controlledVehicle;

    public EntityControlModule() {
        INSTANCE = this;
    }

    public static EntityControlModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        this.releaseVehicleGravity();
    }

    private void releaseVehicleGravity() {
        if (this.controlledVehicle != null) {
            this.controlledVehicle.setNoGravity(false);
            this.controlledVehicle = null;
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        double motionY;
        if (this.getNull() || !this.fly.getValue()) {
            this.releaseVehicleGravity();
            return;
        }
        Entity entity = EntityControlModule.mc.player.getVehicle();
        if (entity == null) {
            this.releaseVehicleGravity();
            return;
        }
        if (this.controlledVehicle != null && this.controlledVehicle != entity) {
            this.releaseVehicleGravity();
        }
        this.controlledVehicle = entity;
        entity.setNoGravity(true);
        entity.setYRot(EntityControlModule.mc.player.getYRot());
        Vector2d forward = MovementUtils.forward(this.speed.getValue().doubleValue());
        double motionX = forward.x;
        double motionZ = forward.y;
        if (EntityControlModule.mc.gui.screen() instanceof ChatScreen || EntityControlModule.mc.gui.screen() != null) {
            motionY = -this.fallSpeed.getValue().doubleValue() / 20.0;
        } else {
            boolean jumping = EntityControlModule.mc.options.keyJump.isDown();
            boolean descending = EntityControlModule.mc.options.keySprint.isDown() || EntityControlModule.mc.options.keyShift.isDown();
            motionY = jumping ? (descending ? -this.fallSpeed.getValue().doubleValue() / 20.0 : this.verticalSpeed.getValue().doubleValue() / 20.0) : (descending ? -this.verticalSpeed.getValue().doubleValue() / 20.0 : -this.fallSpeed.getValue().doubleValue() / 20.0);
        }
        entity.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (this.getNull()) {
            return;
        }
        if (packetReceiveEvent.getPacket() instanceof ClientboundMoveVehiclePacket && this.noSync.getValue()) {
            packetReceiveEvent.cancel();
        }
    }
}

