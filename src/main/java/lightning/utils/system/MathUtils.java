/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.utils.system;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MathUtils {
    public static double random(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static double round(double value, int places) {
        int scale = (int)Math.pow(10.0, places);
        return (double)Math.round(value * (double)scale) / (double)scale;
    }

    public static Vec3 getVec(BlockPos blockPos) {
        return new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
    }

    public static AABB getBox(Vec3 pos) {
        return new AABB(pos.x, pos.y, pos.z, pos.x + 1.0, pos.y + 1.0, pos.z + 1.0);
    }

    public static Vec3 scale(Vec3 vec, float factor) {
        return vec.scale((double)factor);
    }

    public static double interpolate(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    public static boolean inRange(double value, double target, double range) {
        return value > target - range && value < target + range;
    }

    public static float wrapAngle(float angle) {
        angle %= 360.0f;
        if (angle < 0.0f) {
            angle += 360.0f;
        }
        return angle;
    }
}

