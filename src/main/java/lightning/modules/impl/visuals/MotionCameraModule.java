/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 */
package lightning.modules.impl.visuals;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IMotionCameraModule;
import lightning.modules.impl.visuals.ViewClipModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.AnimateUtil;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;

@RegisterModule(name="MotionCamera", description="Smooths camera movement and adds smooth perspective transitions.", category=Module.Category.VISUALS)
public class MotionCameraModule
extends Module
implements IMotionCameraModule {
    public BooleanSetting noFirstPerson = new BooleanSetting("NoFirstPerson", "Disables motion camera in first person.", true);
    public BooleanSetting pearlException = new BooleanSetting("PearlException", "Still smooths in 1st person for a moment after throwing an ender pearl (ignores NoFirstPerson).", new BooleanSetting.Visibility(this.noFirstPerson, true), true);
    public BooleanSetting rubberbandException = new BooleanSetting("RubberbandException", "Still smooths in 1st person for a moment after a rubberband/teleport correction (ignores NoFirstPerson).", new BooleanSetting.Visibility(this.noFirstPerson, true), true);
    public NumberSetting exceptionDuration = new NumberSetting("ExceptionDuration", "How long the pearl/rubberband exception window lasts, in ms.", new BooleanSetting.Visibility(this.noFirstPerson, true), (Number)500, (Number)100, (Number)2000);
    public NumberSetting firstPersonSpeed = new NumberSetting("FirstPersonSpeed", "Movement smoothness speed in first person.", 0.6, 0.01, 1.0);
    public NumberSetting speed = new NumberSetting("Speed", "Movement smoothness speed in third person.", 0.3, 0.01, 1.0);
    public BooleanSetting smoothPerspective = new BooleanSetting("SmoothPerspective", "Smoothly transitions distance when switching between 1st and 3rd person.", true);
    public NumberSetting perspectiveSpeed = new NumberSetting("PerspectiveSpeed", "Speed of perspective switch transition.", 0.5, 0.05, 1.0);
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;
    private double distance = 0.0;
    private double prevDistance = 0.0;
    private final Timer pearlTimer = new Timer();
    private final Timer rubberbandTimer = new Timer();

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent entitySpawnEvent) {
        Player player;
        ThrownEnderpearl thrownEnderpearl;
        if (!this.pearlException.getValue() || MotionCameraModule.mc.player == null) {
            return;
        }
        Entity entity = entitySpawnEvent.getEntity();
        if (entity instanceof ThrownEnderpearl && (entity = (thrownEnderpearl = (ThrownEnderpearl)entity).getOwner()) instanceof Player && (player = (Player)entity) == MotionCameraModule.mc.player) {
            this.pearlTimer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (!this.rubberbandException.getValue()) {
            return;
        }
        if (packetReceiveEvent.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.rubberbandTimer.reset();
        }
    }

    private boolean inExceptionWindow() {
        long l = this.exceptionDuration.getValue().longValue();
        return this.pearlException.getValue() && !this.pearlTimer.hasTimeElapsed(l) || this.rubberbandException.getValue() && !this.rubberbandTimer.hasTimeElapsed(l);
    }

    private boolean isRidingOrFlying() {
        return MotionCameraModule.mc.player != null && (MotionCameraModule.mc.player.isPassenger() || MotionCameraModule.mc.player.isFallFlying());
    }

    @Override
    public boolean isSmoothPerspective() {
        return this.smoothPerspective.getValue();
    }

    @Override
    public boolean on() {
        if (MotionCameraModule.mc.options == null) {
            return false;
        }
        return this.isToggled() && !this.isRidingOrFlying() && (!this.noFirstPerson.getValue() || !MotionCameraModule.mc.options.getCameraType().isFirstPerson() || this.inExceptionWindow());
    }

    @Override
    public boolean shouldBeDetached() {
        if (MotionCameraModule.mc.options == null) {
            return false;
        }
        return this.isToggled() && this.smoothPerspective.getValue() && (this.distance > 0.01 || !MotionCameraModule.mc.options.getCameraType().isFirstPerson());
    }

    @Override
    public void onEnable() {
        if (MotionCameraModule.mc.player != null) {
            this.fakeX = MotionCameraModule.mc.player.getX();
            this.fakeY = MotionCameraModule.mc.player.getY() + (double)MotionCameraModule.mc.player.getEyeHeight(MotionCameraModule.mc.player.getPose());
            this.fakeZ = MotionCameraModule.mc.player.getZ();
            this.prevFakeX = this.fakeX;
            this.prevFakeY = this.fakeY;
            this.prevFakeZ = this.fakeZ;
            double d = MotionCameraModule.mc.options.getCameraType().isFirstPerson() ? 0.0 : this.getTargetPerspectiveDistance();
            this.distance = d;
            this.prevDistance = d;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (MotionCameraModule.mc.player == null || MotionCameraModule.mc.options == null) {
            return;
        }
        this.prevFakeX = this.fakeX;
        this.prevFakeY = this.fakeY;
        this.prevFakeZ = this.fakeZ;
        double d = MotionCameraModule.mc.player.getX();
        double d2 = MotionCameraModule.mc.player.getY() + (double)MotionCameraModule.mc.player.getEyeHeight(MotionCameraModule.mc.player.getPose());
        double d3 = MotionCameraModule.mc.player.getZ();
        if (this.isRidingOrFlying()) {
            this.fakeX = d;
            this.fakeY = d2;
            this.fakeZ = d3;
        } else {
            double d4 = MotionCameraModule.mc.options.getCameraType().isFirstPerson() ? this.firstPersonSpeed.getValue().doubleValue() : this.speed.getValue().doubleValue();
            this.fakeX = AnimateUtil.animate(this.fakeX, d, d4);
            this.fakeY = AnimateUtil.animate(this.fakeY, d2, d4);
            this.fakeZ = AnimateUtil.animate(this.fakeZ, d3, d4);
        }
        this.prevDistance = this.distance;
        boolean bl = MotionCameraModule.mc.options.getCameraType().isFirstPerson();
        double d5 = bl ? 0.0 : this.getTargetPerspectiveDistance();
        double d6 = this.distance = this.smoothPerspective.getValue() && !bl ? AnimateUtil.animate(this.distance, d5, this.perspectiveSpeed.getValue().doubleValue()) : d5;
        if (bl) {
            this.prevDistance = 0.0;
        }
    }

    private double getTargetPerspectiveDistance() {
        ViewClipModule viewClipModule = Night.MODULE_MANAGER.getModule(ViewClipModule.class);
        if (viewClipModule != null && viewClipModule.isToggled() && viewClipModule.extend.getValue()) {
            return viewClipModule.distance.getValue().doubleValue();
        }
        return 4.0;
    }

    @Override
    public double getFakeX() {
        float f = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0f;
        return MathUtils.interpolate(this.prevFakeX, this.fakeX, f);
    }

    @Override
    public double getFakeY() {
        float f = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0f;
        return MathUtils.interpolate(this.prevFakeY, this.fakeY, f);
    }

    @Override
    public double getFakeZ() {
        float f = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0f;
        return MathUtils.interpolate(this.prevFakeZ, this.fakeZ, f);
    }

    @Override
    public double getDistance() {
        float f = mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 1.0f;
        return MathUtils.interpolate(this.prevDistance, this.distance, f);
    }
}

