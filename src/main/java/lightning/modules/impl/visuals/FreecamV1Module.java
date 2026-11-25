/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package lightning.modules.impl.visuals;

import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.system.MathUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@RegisterModule(name="Freeze", description="ThunderHack-style freecam: fly the camera around, optionally freeze your player or track an entity.", category=Module.Category.VISUALS)
public class FreecamV1Module
extends Module {
    public NumberSetting hSpeed = new NumberSetting("HSpeed", "Horizontal speed of the camera (scroll wheel changes it).", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(3.0f));
    public NumberSetting vSpeed = new NumberSetting("VSpeed", "Vertical speed of the camera (jump / sneak).", Float.valueOf(0.42f), Float.valueOf(0.1f), Float.valueOf(3.0f));
    public BooleanSetting freeze = new BooleanSetting("Freeze", "Freezes your real player: cancels movement and movement packets.", false);
    public BooleanSetting track = new BooleanSetting("Track", "Attacking an entity makes the camera follow it. Press Shift or Escape to stop.", false);
    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;
    private LivingEntity trackEntity;

    @Override
    public void onEnable() {
        if (FreecamV1Module.mc.player == null || FreecamV1Module.mc.level == null) {
            this.setToggled(false);
            return;
        }
        FreecamV1Module.mc.smartCull = false;
        this.trackEntity = null;
        this.fakePitch = this.prevFakePitch = FreecamV1Module.mc.player.getXRot();
        this.fakeYaw = this.prevFakeYaw = FreecamV1Module.mc.player.getYRot();
        this.fakeX = this.prevFakeX = FreecamV1Module.mc.player.getX();
        this.fakeY = FreecamV1Module.mc.player.getY() + (double)FreecamV1Module.mc.player.getEyeHeight(FreecamV1Module.mc.player.getPose());
        this.prevFakeY = FreecamV1Module.mc.player.getY();
        this.fakeZ = this.prevFakeZ = FreecamV1Module.mc.player.getZ();
    }

    @Override
    public void onDisable() {
        this.trackEntity = null;
        FreecamV1Module.mc.smartCull = true;
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent attackEntityEvent) {
        Entity entity;
        if (this.track.getValue() && (entity = attackEntityEvent.getTarget()) instanceof LivingEntity) {
            LivingEntity livingEntity;
            this.trackEntity = livingEntity = (LivingEntity)entity;
        }
    }

    @SubscribeEvent
    public void onKey(KeyInputEvent keyInputEvent) {
        int key = keyInputEvent.getKey();
        if (key == 256 || key == 340 || key == 344) {
            this.trackEntity = null;
        }
    }

    @SubscribeEvent(priority=100)
    public void onTick(TickEvent tickEvent) {
        if (FreecamV1Module.mc.player == null) {
            return;
        }
        this.prevFakeYaw = this.fakeYaw;
        this.prevFakePitch = this.fakePitch;
        if (this.trackEntity != null && this.trackEntity.isRemoved()) {
            this.trackEntity = null;
        }
        if (this.trackEntity != null) {
            this.fakeYaw = this.trackEntity.getYRot();
            this.fakePitch = this.trackEntity.getXRot();
            this.prevFakeX = this.fakeX;
            this.prevFakeY = this.fakeY;
            this.prevFakeZ = this.fakeZ;
            this.fakeX = this.trackEntity.getX();
            this.fakeY = this.trackEntity.getY() + (double)this.trackEntity.getEyeHeight(this.trackEntity.getPose());
            this.fakeZ = this.trackEntity.getZ();
        } else {
            this.fakeYaw = FreecamV1Module.mc.player.getYRot();
            this.fakePitch = FreecamV1Module.mc.player.getXRot();
        }
    }

    public void onInput(float forward, float strafe) {
        if (FreecamV1Module.mc.player == null) {
            return;
        }
        if (this.trackEntity != null) {
            return;
        }
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
        double speed = this.hSpeed.getValue().doubleValue();
        if (forward != 0.0f || strafe != 0.0f) {
            double length = Math.sqrt(forward * forward + strafe * strafe);
            double normForward = (double)forward / length;
            double normStrafe = (double)strafe / length;
            double yawRad = Math.toRadians(FreecamV1Module.mc.player.getYRot());
            double sinYaw = Math.sin(yawRad);
            double cosYaw = Math.cos(yawRad);
            this.fakeX += (-sinYaw * normForward + cosYaw * normStrafe) * speed;
            this.fakeZ += (cosYaw * normForward + sinYaw * normStrafe) * speed;
        }
        if (FreecamV1Module.mc.options.keyJump.isDown()) {
            this.fakeY += this.vSpeed.getValue().doubleValue();
        }
        if (FreecamV1Module.mc.options.keyShift.isDown()) {
            this.fakeY -= this.vSpeed.getValue().doubleValue();
        }
    }

    @SubscribeEvent(priority=-100)
    public void onMove(PlayerMoveEvent playerMoveEvent) {
        if (this.freeze.getValue()) {
            playerMoveEvent.setX(0.0);
            playerMoveEvent.setY(0.0);
            playerMoveEvent.setZ(0.0);
            playerMoveEvent.cancel();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (this.freeze.getValue() && packetSendEvent.getPacket() instanceof ServerboundMovePlayerPacket) {
            packetSendEvent.cancel();
        }
    }

    public void onScroll(double delta) {
        float step = delta > 0.0 ? 0.05f : -0.05f;
        this.hSpeed.setValue(Float.valueOf(this.hSpeed.getValue().floatValue() + step));
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (playerDeathEvent.getPlayer() == null || FreecamV1Module.mc.player == null) {
            return;
        }
        this.setToggled(false);
    }

    @SubscribeEvent
    public void onConnect(ClientConnectEvent clientConnectEvent) {
        this.setToggled(false);
    }

    public boolean isTracking() {
        return this.trackEntity != null;
    }

    public float getFakeYaw() {
        return (float)MathUtils.interpolate(this.prevFakeYaw, this.fakeYaw, FreecamV1Module.partial());
    }

    public float getFakePitch() {
        return (float)MathUtils.interpolate(this.prevFakePitch, this.fakePitch, FreecamV1Module.partial());
    }

    public double getFakeX() {
        return MathUtils.interpolate(this.prevFakeX, this.fakeX, FreecamV1Module.partial());
    }

    public double getFakeY() {
        return MathUtils.interpolate(this.prevFakeY, this.fakeY, FreecamV1Module.partial());
    }

    public double getFakeZ() {
        return MathUtils.interpolate(this.prevFakeZ, this.fakeZ, FreecamV1Module.partial());
    }

    private static float partial() {
        return mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }
}

