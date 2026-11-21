/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 */
package lightning.utils.rotations;

import lightning.utils.IMinecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class RotationUtils
implements IMinecraft {
    public static float[] getRotations(Entity entity) {
        return RotationUtils.getRotations(entity.getX(), entity.getY() + (double)entity.getEyeHeight(entity.getPose()) / 2.0, entity.getZ());
    }

    public static float[] getRotations(Vec3 vec3) {
        return RotationUtils.getRotations(vec3.x, vec3.y, vec3.z);
    }

    public static float[] getRotations(double d, double d2, double d3) {
        return RotationUtils.getRotations((Entity)RotationUtils.mc.player, d, d2, d3);
    }

    public static float[] getRotations(Entity entity, double d, double d2, double d3) {
        Vec3 vec3 = entity.position().add(0.0, (double)entity.getEyeHeight(entity.getPose()), 0.0);
        double d4 = d - vec3.x;
        double d5 = (d2 - vec3.y) * -1.0;
        double d6 = d3 - vec3.z;
        double d7 = Mth.sqrt((float)((float)(d4 * d4 + d6 * d6)));
        float f = (float)Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(d6, d4)) - 90.0));
        float f2 = (float)Mth.clamp((double)Mth.wrapDegrees((double)Math.toDegrees(Math.atan2(d5, d7))), (double)-90.0, (double)90.0);
        return new float[]{f + ((float)Math.random() - 0.5f) * 4.0f, f2 + ((float)Math.random() - 0.5f) * 4.0f};
    }

    public static double getYRotToVec(Entity entity, Vec3 vec3) {
        double d = vec3.x - entity.getX();
        double d2 = vec3.z - entity.getZ();
        return Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(d2, d)) - 90.0));
    }

    public static double getXRotToVec(Entity entity, Vec3 vec3) {
        double d = vec3.x - entity.getX();
        double d2 = vec3.y - (entity.getY() + (double)entity.getEyeHeight());
        double d3 = vec3.z - entity.getZ();
        double d4 = Math.sqrt(d * d + d3 * d3);
        return Mth.clamp((double)Mth.wrapDegrees((double)(-Math.toDegrees(Math.atan2(d2, d4)))), (double)-90.0, (double)90.0);
    }

    public static float[] getExactRotations(Entity entity, Vec3 vec3) {
        return new float[]{(float)RotationUtils.getYRotToVec(entity, vec3), (float)RotationUtils.getXRotToVec(entity, vec3)};
    }

    public static float getYRotToVec(Vec3 vec3, Vec3 vec32) {
        double d = vec32.x - vec3.x;
        double d2 = vec32.z - vec3.z;
        return (float)Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(d2, d)) - 90.0));
    }

    public static float getXRotToVec(Vec3 vec3, Vec3 vec32) {
        double d = vec32.x - vec3.x;
        double d2 = vec32.y - vec3.y;
        double d3 = vec32.z - vec3.z;
        return (float)(-Math.toDegrees(Math.atan2(d2, Math.sqrt(d * d + d3 * d3))));
    }

    public static Vec3 getClosestPointToEye(Vec3 vec3, AABB aABB) {
        double d = vec3.x;
        double d2 = vec3.y;
        double d3 = vec3.z;
        double d4 = 0.0625;
        double d5 = 1.0E-9;
        if (vec3.x < aABB.minX) {
            d = aABB.minX;
        } else if (vec3.x > aABB.maxX) {
            d = aABB.maxX;
        }
        if (vec3.y < aABB.minY) {
            d2 = aABB.minY;
        } else if (vec3.y > aABB.maxY) {
            d2 = aABB.maxY;
        }
        if (vec3.z < aABB.minZ) {
            d3 = aABB.minZ;
        } else if (vec3.z > aABB.maxZ) {
            d3 = aABB.maxZ;
        }
        if (Math.abs(d - aABB.minX) < 1.0E-9) {
            d = Math.min(aABB.minX + 0.0625, aABB.maxX - 1.0E-9);
        } else if (Math.abs(d - aABB.maxX) < 1.0E-9) {
            d = Math.max(aABB.maxX - 0.0625, aABB.minX + 1.0E-9);
        }
        if (Math.abs(d3 - aABB.minZ) < 1.0E-9) {
            d3 = Math.min(aABB.minZ + 0.0625, aABB.maxZ - 1.0E-9);
        } else if (Math.abs(d3 - aABB.maxZ) < 1.0E-9) {
            d3 = Math.max(aABB.maxZ - 0.0625, aABB.minZ + 1.0E-9);
        }
        return new Vec3(d, d2, d3);
    }

    public static Vec3 getClampClosestPoint(Vec3 vec3, AABB aABB) {
        double d = Mth.clamp((double)vec3.x, (double)aABB.minX, (double)aABB.maxX);
        double d2 = Mth.clamp((double)vec3.y, (double)aABB.minY, (double)aABB.maxY);
        double d3 = Mth.clamp((double)vec3.z, (double)aABB.minZ, (double)aABB.maxZ);
        return new Vec3(d, d2, d3);
    }

    public static Vec3 getLookVectorFromYRotXRot(float f, float f2) {
        float f3 = f2 * ((float)Math.PI / 180);
        float f4 = -f * ((float)Math.PI / 180);
        float f5 = Mth.cos((double)f4);
        float f6 = Mth.sin((double)f4);
        float f7 = Mth.cos((double)f3);
        float f8 = Mth.sin((double)f3);
        return new Vec3((double)(f6 * f7), (double)(-f8), (double)(f5 * f7));
    }

    public static EntityHitResult raycastTarget(Vec3 vec3, Entity entity, double d, float f, float f2) {
        Vec3 vec32 = RotationUtils.getLookVectorFromYRotXRot(f, f2);
        Vec3 vec33 = vec3.add(vec32.scale(d));
        float f3 = entity.getPickRadius();
        AABB aABB = entity.getBoundingBox().inflate((double)Math.max(f3, 0.05f));
        if (aABB.clip(vec3, vec33).isPresent()) {
            return new EntityHitResult(entity);
        }
        return null;
    }
}

