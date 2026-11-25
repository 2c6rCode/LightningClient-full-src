/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.Vec3dAccessor
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector2d
 */
package lightning.utils.minecraft;

import lightning.Night;
import lightning.events.impl.PlayerMoveEvent;
import lightning.mixins.accessors.Vec3dAccessor;
import lightning.modules.api.ISprintModule;
import lightning.utils.IMinecraft;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public class MovementUtils
implements IMinecraft {
    public static double DEFAULT_SPEED = 0.2873;

    public static Vector2d forward(double speed) {
        ISprintModule iSprintModule;
        float forwardInput = MovementUtils.mc.player.input.getMoveVector().y;
        float strafeInput = MovementUtils.mc.player.input.getMoveVector().x;
        float yaw = MovementUtils.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (iSprintModule = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && iSprintModule.isGrimCompensating()) {
            yaw = iSprintModule.getGrimYaw();
        }
        if (forwardInput == 0.0f && strafeInput == 0.0f) {
            return new Vector2d(0.0, 0.0);
        }
        float magnitude = (float)Math.sqrt(forwardInput * forwardInput + strafeInput * strafeInput);
        if (magnitude > 1.0E-4f && Math.abs(magnitude - 1.0f) > 1.0E-4f) {
            forwardInput /= magnitude;
            strafeInput /= magnitude;
        }
        double cosYaw = Math.cos(Math.toRadians(yaw + 90.0f));
        double sinYaw = Math.sin(Math.toRadians(yaw + 90.0f));
        return new Vector2d((double)forwardInput * speed * cosYaw + (double)strafeInput * speed * sinYaw, (double)forwardInput * speed * sinYaw - (double)strafeInput * speed * cosYaw);
    }

    public static double[] straightForward(double speed) {
        ISprintModule iSprintModule;
        float yaw = MovementUtils.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (iSprintModule = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && iSprintModule.isGrimCompensating()) {
            yaw = iSprintModule.getGrimYaw();
        }
        return new double[]{speed * Math.cos(Math.toRadians(yaw + 90.0f)), speed * Math.sin(Math.toRadians(yaw + 90.0f))};
    }

    public static double getPotionSpeed(double baseSpeed) {
        if (MovementUtils.mc.player.hasEffect(MobEffects.SPEED)) {
            baseSpeed *= 1.0 + 0.2 * (double)(MovementUtils.mc.player.getEffect(MobEffects.SPEED).getAmplifier() + 1);
        }
        if (MovementUtils.mc.player.hasEffect(MobEffects.SLOWNESS)) {
            baseSpeed /= 1.0 + 0.2 * (double)(MovementUtils.mc.player.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1);
        }
        return baseSpeed;
    }

    public static double getPotionJump(double baseJump) {
        if (MovementUtils.mc.player.hasEffect(MobEffects.JUMP_BOOST)) {
            baseJump += (double)((float)(MovementUtils.mc.player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f);
        }
        return baseJump;
    }

    public static boolean isMoving() {
        if (MovementUtils.mc.player == null) {
            return false;
        }
        return MovementUtils.mc.options.keyUp.isDown() || MovementUtils.mc.options.keyDown.isDown() || MovementUtils.mc.options.keyLeft.isDown() || MovementUtils.mc.options.keyRight.isDown() || MovementUtils.mc.player.input != null && (MovementUtils.mc.player.input.getMoveVector().x != 0.0f || MovementUtils.mc.player.input.getMoveVector().y != 0.0f) || MovementUtils.mc.player.xxa != 0.0f || MovementUtils.mc.player.zza != 0.0f;
    }

    public static void moveTowards(PlayerMoveEvent playerMoveEvent, Vec3 target, double speed) {
        double yawRad = Math.toRadians(RotationUtils.getRotations(target)[0]);
        double motionX = -Math.sin(yawRad) * speed;
        double motionZ = Math.cos(yawRad) * speed;
        double[] delta = new double[]{target.x - MovementUtils.mc.player.getX(), target.z - MovementUtils.mc.player.getZ()};
        playerMoveEvent.setMovement(new Vec3(Math.abs(motionX) < Math.abs(delta[0]) ? motionX : delta[0], playerMoveEvent.getMovement().y, playerMoveEvent.getMovement().z));
        playerMoveEvent.setMovement(new Vec3(playerMoveEvent.getMovement().x, playerMoveEvent.getMovement().y, Math.abs(motionZ) < Math.abs(delta[1]) ? motionZ : delta[1]));
        ((Vec3dAccessor)MovementUtils.mc.player.getDeltaMovement()).setX(0.0);
        ((Vec3dAccessor)MovementUtils.mc.player.getDeltaMovement()).setZ(0.0);
        playerMoveEvent.setCancelled(true);
    }
}

