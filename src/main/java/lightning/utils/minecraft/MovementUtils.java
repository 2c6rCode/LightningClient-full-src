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

    public static Vector2d forward(double d) {
        ISprintModule iSprintModule;
        float f = MovementUtils.mc.player.input.getMoveVector().y;
        float f2 = MovementUtils.mc.player.input.getMoveVector().x;
        float f3 = MovementUtils.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (iSprintModule = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && iSprintModule.isGrimCompensating()) {
            f3 = iSprintModule.getGrimYaw();
        }
        if (f == 0.0f && f2 == 0.0f) {
            return new Vector2d(0.0, 0.0);
        }
        float f4 = (float)Math.sqrt(f * f + f2 * f2);
        if (f4 > 1.0E-4f && Math.abs(f4 - 1.0f) > 1.0E-4f) {
            f /= f4;
            f2 /= f4;
        }
        double d2 = Math.cos(Math.toRadians(f3 + 90.0f));
        double d3 = Math.sin(Math.toRadians(f3 + 90.0f));
        return new Vector2d((double)f * d * d2 + (double)f2 * d * d3, (double)f * d * d3 - (double)f2 * d * d2);
    }

    public static double[] straightForward(double d) {
        ISprintModule iSprintModule;
        float f = MovementUtils.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (iSprintModule = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && iSprintModule.isGrimCompensating()) {
            f = iSprintModule.getGrimYaw();
        }
        return new double[]{d * Math.cos(Math.toRadians(f + 90.0f)), d * Math.sin(Math.toRadians(f + 90.0f))};
    }

    public static double getPotionSpeed(double d) {
        if (MovementUtils.mc.player.hasEffect(MobEffects.SPEED)) {
            d *= 1.0 + 0.2 * (double)(MovementUtils.mc.player.getEffect(MobEffects.SPEED).getAmplifier() + 1);
        }
        if (MovementUtils.mc.player.hasEffect(MobEffects.SLOWNESS)) {
            d /= 1.0 + 0.2 * (double)(MovementUtils.mc.player.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1);
        }
        return d;
    }

    public static double getPotionJump(double d) {
        if (MovementUtils.mc.player.hasEffect(MobEffects.JUMP_BOOST)) {
            d += (double)((float)(MovementUtils.mc.player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f);
        }
        return d;
    }

    public static boolean isMoving() {
        if (MovementUtils.mc.player == null) {
            return false;
        }
        return MovementUtils.mc.options.keyUp.isDown() || MovementUtils.mc.options.keyDown.isDown() || MovementUtils.mc.options.keyLeft.isDown() || MovementUtils.mc.options.keyRight.isDown() || MovementUtils.mc.player.input != null && (MovementUtils.mc.player.input.getMoveVector().x != 0.0f || MovementUtils.mc.player.input.getMoveVector().y != 0.0f) || MovementUtils.mc.player.xxa != 0.0f || MovementUtils.mc.player.zza != 0.0f;
    }

    public static void moveTowards(PlayerMoveEvent playerMoveEvent, Vec3 vec3, double d) {
        double d2 = Math.toRadians(RotationUtils.getRotations(vec3)[0]);
        double d3 = -Math.sin(d2) * d;
        double d4 = Math.cos(d2) * d;
        double[] dArray = new double[]{vec3.x - MovementUtils.mc.player.getX(), vec3.z - MovementUtils.mc.player.getZ()};
        playerMoveEvent.setMovement(new Vec3(Math.abs(d3) < Math.abs(dArray[0]) ? d3 : dArray[0], playerMoveEvent.getMovement().y, playerMoveEvent.getMovement().z));
        playerMoveEvent.setMovement(new Vec3(playerMoveEvent.getMovement().x, playerMoveEvent.getMovement().y, Math.abs(d4) < Math.abs(dArray[1]) ? d4 : dArray[1]));
        ((Vec3dAccessor)MovementUtils.mc.player.getDeltaMovement()).setX(0.0);
        ((Vec3dAccessor)MovementUtils.mc.player.getDeltaMovement()).setZ(0.0);
        playerMoveEvent.setCancelled(true);
    }
}

