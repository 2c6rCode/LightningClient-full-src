/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.ClientPlayerEntityAccessor
 *  lombok.Generated
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.Vec2
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientRotationEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.api.ISprintModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.HoleSnapModule;
import lightning.modules.impl.movement.NoSlowModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.MovementUtils;
import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Sprint", description="Makes it so that you are always sprinting when possible.", category=Module.Category.MOVEMENT)
public class SprintModule
extends Module
implements ISprintModule {
    public ModeSetting mode = new ModeSetting("Mode", "The limits to when you can be sprinting.", "Rage", new String[]{"Legit", "Rage", "Instant", "Grim"});
    public NumberSetting instantSpeed = new NumberSetting("InstantSpeed", "Per-tick horizontal speed to move at (blocks/tick) when Mode is Instant.", new ModeSetting.Visibility(this.mode, "Instant"), (Number)Float.valueOf((float)MovementUtils.DEFAULT_SPEED), (Number)Float.valueOf(0.05f), (Number)Float.valueOf(0.6f));
    public BooleanSetting instantWater = new BooleanSetting("Water", "Keeps applying the Instant speed override while in water.", new ModeSetting.Visibility(this.mode, "Instant"), true);
    public BooleanSetting instantLava = new BooleanSetting("Lava", "Keeps applying the Instant speed override while in lava.", new ModeSetting.Visibility(this.mode, "Instant"), false);
    public BooleanSetting pauseOnSetback = new BooleanSetting("PauseOnSetback", "Briefly pauses sprinting when receiving a setback from the server to allow GrimAC buffer to decay.", true);
    private float cachedYaw;
    private Vec2 cachedMove;
    private boolean grimQueued;
    private Float pendingYaw;
    private float pendingPitch;
    private int grimStrafe;

    @Override
    public int getGrimStrafe() {
        return this.grimStrafe;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (SprintModule.mc.player == null) {
            return;
        }
        this.cachedYaw = SprintModule.mc.player.getYRot();
        this.cachedMove = SprintModule.mc.player.input.getMoveVector();
        if (this.isGrim()) {
            this.grimUpdate();
        } else {
            this.pendingYaw = null;
        }
        boolean canSprint = this.shouldSprint();
        if (canSprint) {
            SprintModule.mc.player.setSprinting(true);
        } else if (SprintModule.mc.player.isSprinting()) {
            SprintModule.mc.player.setSprinting(false);
        }
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        if (SprintModule.mc.player == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Instant")) {
            return;
        }
        ElytraFlyModule elytraFlyModule = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (elytraFlyModule.isToggled() && elytraFlyModule.mode.getValue().equalsIgnoreCase("Control") && SprintModule.mc.player.isFallFlying()) {
            return;
        }
        HoleSnapModule holeSnapModule = Night.MODULE_MANAGER.getModule(HoleSnapModule.class);
        if (holeSnapModule.isToggled() && holeSnapModule.hole != null) {
            return;
        }
        boolean canInstantSprint = !(SprintModule.mc.player.getFoodData().getFoodLevel() <= 6 || !this.instantLava.getValue() && SprintModule.mc.player.isInLava() || !this.instantWater.getValue() && SprintModule.mc.player.isInWater() || this.pauseOnSetback.getValue() && Night.SERVER_MANAGER != null && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L));
        if (!canInstantSprint) {
            return;
        }
        Vec2 moveVec = this.cachedMove;
        if (moveVec.x != 0.0f || moveVec.y != 0.0f) {
            double magnitude = Math.sqrt(moveVec.x * moveVec.x + moveVec.y * moveVec.y);
            double strafe = (double)moveVec.x / magnitude;
            double forward = (double)moveVec.y / magnitude;
            double yawRad = Math.toRadians(this.cachedYaw);
            double sinYaw = Math.sin(yawRad);
            double cosYaw = Math.cos(yawRad);
            double dirX = strafe * cosYaw - forward * sinYaw;
            double dirZ = strafe * sinYaw + forward * cosYaw;
            double targetSpeed = this.instantSpeed.getValue().doubleValue();
            playerMoveEvent.setMovement(new Vec3(dirX * targetSpeed, playerMoveEvent.getMovement().y, dirZ * targetSpeed));
        } else {
            playerMoveEvent.setMovement(new Vec3(0.0, playerMoveEvent.getMovement().y, 0.0));
        }
        playerMoveEvent.setCancelled(true);
    }

    @SubscribeEvent
    public void onClientRotation(ClientRotationEvent clientRotationEvent) {
        if (this.pendingYaw == null || clientRotationEvent.isCancelled()) {
            return;
        }
        clientRotationEvent.setYaw(this.pendingYaw.floatValue());
        clientRotationEvent.setPitch(this.pendingPitch);
        clientRotationEvent.setOwner(this);
    }

    private boolean isGrim() {
        return this.mode.getValue().equalsIgnoreCase("Grim");
    }

    private void grimUpdate() {
        float targetYaw;
        this.grimQueued = false;
        this.pendingYaw = null;
        if (SprintModule.mc.player.isFallFlying() || SprintModule.mc.player.isPassenger()) {
            return;
        }
        if (this.pauseOnSetback.getValue() && Night.SERVER_MANAGER != null && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L)) {
            return;
        }
        if (((IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"))).isToggled()) {
            return;
        }
        int strafeInput = (SprintModule.mc.options.keyRight.isDown() ? 1 : 0) - (SprintModule.mc.options.keyLeft.isDown() ? 1 : 0);
        int forwardInput = (SprintModule.mc.options.keyUp.isDown() ? 1 : 0) - (SprintModule.mc.options.keyDown.isDown() ? 1 : 0);
        if (strafeInput == 0 && forwardInput == 0) {
            return;
        }
        float moveAngle = (float)Math.toDegrees(Math.atan2(strafeInput, forwardInput));
        int strafeDir = 0;
        if (strafeInput != 0 && forwardInput != 0) {
            strafeDir = strafeInput < 0 ? 1 : -1;
            targetYaw = Mth.wrapDegrees((float)(SprintModule.mc.player.getYRot() + moveAngle + (strafeDir > 0 ? 45.0f : -45.0f)));
        } else {
            targetYaw = Mth.wrapDegrees((float)(SprintModule.mc.player.getYRot() + moveAngle));
        }
        this.pendingYaw = Float.valueOf(targetYaw);
        this.pendingPitch = SprintModule.mc.player.getXRot();
        this.grimStrafe = strafeDir;
        this.grimQueued = true;
    }

    @Override
    public boolean isGrimCompensating() {
        if (!(SprintModule.mc.player != null && this.isToggled() && this.isGrim() && this.grimQueued)) {
            return false;
        }
        if (this.pauseOnSetback.getValue() && Night.SERVER_MANAGER != null && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L)) {
            return false;
        }
        return Night.ROTATION_MANAGER.getRotation() != null && Night.ROTATION_MANAGER.getRotationOwner() == this;
    }

    @Override
    public float getGrimYaw() {
        return Night.ROTATION_MANAGER.getRotation().getYaw();
    }

    @Override
    public boolean isInstantMode() {
        return this.mode.getValue().equalsIgnoreCase("Instant");
    }

    @Override
    public void onEnable() {
        if (SprintModule.mc.player == null) {
            return;
        }
        SprintModule.mc.player.setSprinting(this.shouldSprint());
    }

    @Override
    public void onDisable() {
        this.grimQueued = false;
        if (SprintModule.mc.player == null) {
            return;
        }
        SprintModule.mc.player.setSprinting(false);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }

    @Override
    public boolean shouldSprint() {
        if (this.pauseOnSetback.getValue() && Night.SERVER_MANAGER != null && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(150L)) {
            return false;
        }
        if (!((ClientPlayerEntityAccessor)SprintModule.mc.player).invokeCanSprint(true)) {
            return false;
        }
        if (SprintModule.mc.player.isInWater() && !SprintModule.mc.player.isUnderWater()) {
            return false;
        }
        if (SprintModule.mc.player.isSwimming() && !SprintModule.mc.player.onGround() && !SprintModule.mc.player.input.keyPresses.shift() && !SprintModule.mc.player.isInWater()) {
            return false;
        }
        if (SprintModule.mc.player.isInLava()) {
            return false;
        }
        if (SprintModule.mc.player.onClimbable()) {
            return false;
        }
        if (SprintModule.mc.player.isFallFlying()) {
            ElytraFlyModule elytraFlyModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ElytraFlyModule.class) : null;
            if (elytraFlyModule == null || !elytraFlyModule.isToggled() || !elytraFlyModule.mode.getValue().equalsIgnoreCase("Bounce")) {
                return false;
            }
        }
        if (SprintModule.mc.player.getFoodData().getFoodLevel() <= 6) {
            return false;
        }
        if (!(!SprintModule.mc.player.isUsingItem() || Night.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled() && Night.MODULE_MANAGER.getModule(NoSlowModule.class).items.getValue())) {
            return false;
        }
        if (this.mode.getValue().equalsIgnoreCase("Rage") || this.mode.getValue().equalsIgnoreCase("Instant") || this.isGrim()) {
            if (SprintModule.mc.player.horizontalCollision && !SprintModule.mc.player.minorHorizontalCollision) {
                return false;
            }
            if (this.isGrim() && !this.isGrimCompensating()) {
                return SprintModule.mc.player.input.keyPresses.forward();
            }
            return SprintModule.mc.player.input.keyPresses.forward() || SprintModule.mc.player.input.keyPresses.backward() || SprintModule.mc.player.input.keyPresses.left() || SprintModule.mc.player.input.keyPresses.right();
        }
        if (!((ClientPlayerEntityAccessor)SprintModule.mc.player).invokeIsWalking()) {
            return false;
        }
        if (SprintModule.mc.player.horizontalCollision && !SprintModule.mc.player.minorHorizontalCollision) {
            return false;
        }
        return SprintModule.mc.player.input.hasForwardImpulse();
    }

    @Generated
    public Float getPendingYaw() {
        return this.pendingYaw;
    }
}

