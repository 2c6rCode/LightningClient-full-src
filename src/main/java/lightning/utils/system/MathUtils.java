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
    public static double random(double d, double d2) {
        return Math.random() * (d - d2) + d2;
    }

    public static double round(double d, int n) {
        int n2 = (int)Math.pow(10.0, n);
        return (double)Math.round(d * (double)n2) / (double)n2;
    }

    public static Vec3 getVec(BlockPos blockPos) {
        return new Vec3((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
    }

    public static AABB getBox(Vec3 vec3) {
        return new AABB(vec3.x, vec3.y, vec3.z, vec3.x + 1.0, vec3.y + 1.0, vec3.z + 1.0);
    }

    public static Vec3 scale(Vec3 vec3, float f) {
        return vec3.scale((double)f);
    }

    public static double interpolate(double d, double d2, double d3) {
        return d + (d2 - d) * d3;
    }

    public static boolean inRange(double d, double d2, double d3) {
        return d > d2 - d3 && d < d2 + d3;
    }

    public static float wrapAngle(float angle) {
        angle %= 360.0f;
        if (angle < 0.0f) {
            angle += 360.0f;
        }
        return angle;
    }
}

