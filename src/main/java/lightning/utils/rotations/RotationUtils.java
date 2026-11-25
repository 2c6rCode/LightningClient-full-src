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

    public static float[] getRotations(Vec3 target) {
        return RotationUtils.getRotations(target.x, target.y, target.z);
    }

    public static float[] getRotations(double x, double y, double z) {
        return RotationUtils.getRotations((Entity)RotationUtils.mc.player, x, y, z);
    }

    public static float[] getRotations(Entity entity, double x, double y, double z) {
        Vec3 eyePos = entity.position().add(0.0, (double)entity.getEyeHeight(entity.getPose()), 0.0);
        double deltaX = x - eyePos.x;
        double deltaY = (y - eyePos.y) * -1.0;
        double deltaZ = z - eyePos.z;
        double horizontalDistance = Mth.sqrt((float)((float)(deltaX * deltaX + deltaZ * deltaZ)));
        float yaw = (float)Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0));
        float pitch = (float)Mth.clamp((double)Mth.wrapDegrees((double)Math.toDegrees(Math.atan2(deltaY, horizontalDistance))), (double)-90.0, (double)90.0);
        return new float[]{yaw + ((float)Math.random() - 0.5f) * 4.0f, pitch + ((float)Math.random() - 0.5f) * 4.0f};
    }

    public static double getYRotToVec(Entity entity, Vec3 target) {
        double deltaX = target.x - entity.getX();
        double deltaZ = target.z - entity.getZ();
        return Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0));
    }

    public static double getXRotToVec(Entity entity, Vec3 target) {
        double deltaX = target.x - entity.getX();
        double deltaY = target.y - (entity.getY() + (double)entity.getEyeHeight());
        double deltaZ = target.z - entity.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        return Mth.clamp((double)Mth.wrapDegrees((double)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance)))), (double)-90.0, (double)90.0);
    }

    public static float[] getExactRotations(Entity entity, Vec3 target) {
        return new float[]{(float)RotationUtils.getYRotToVec(entity, target), (float)RotationUtils.getXRotToVec(entity, target)};
    }

    public static float getYRotToVec(Vec3 from, Vec3 to) {
        double deltaX = to.x - from.x;
        double deltaZ = to.z - from.z;
        return (float)Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0));
    }

    public static float getXRotToVec(Vec3 from, Vec3 to) {
        double deltaX = to.x - from.x;
        double deltaY = to.y - from.y;
        double deltaZ = to.z - from.z;
        return (float)(-Math.toDegrees(Math.atan2(deltaY, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ))));
    }

    public static Vec3 getClosestPointToEye(Vec3 eyePos, AABB box) {
        double closestX = eyePos.x;
        double closestY = eyePos.y;
        double closestZ = eyePos.z;
        double inset = 0.0625;
        double epsilon = 1.0E-9;
        if (eyePos.x < box.minX) {
            closestX = box.minX;
        } else if (eyePos.x > box.maxX) {
            closestX = box.maxX;
        }
        if (eyePos.y < box.minY) {
            closestY = box.minY;
        } else if (eyePos.y > box.maxY) {
            closestY = box.maxY;
        }
        if (eyePos.z < box.minZ) {
            closestZ = box.minZ;
        } else if (eyePos.z > box.maxZ) {
            closestZ = box.maxZ;
        }
        if (Math.abs(closestX - box.minX) < epsilon) {
            closestX = Math.min(box.minX + inset, box.maxX - epsilon);
        } else if (Math.abs(closestX - box.maxX) < epsilon) {
            closestX = Math.max(box.maxX - inset, box.minX + epsilon);
        }
        if (Math.abs(closestZ - box.minZ) < epsilon) {
            closestZ = Math.min(box.minZ + inset, box.maxZ - epsilon);
        } else if (Math.abs(closestZ - box.maxZ) < epsilon) {
            closestZ = Math.max(box.maxZ - inset, box.minZ + epsilon);
        }
        return new Vec3(closestX, closestY, closestZ);
    }

    public static Vec3 getClampClosestPoint(Vec3 point, AABB box) {
        double clampedX = Mth.clamp((double)point.x, (double)box.minX, (double)box.maxX);
        double clampedY = Mth.clamp((double)point.y, (double)box.minY, (double)box.maxY);
        double clampedZ = Mth.clamp((double)point.z, (double)box.minZ, (double)box.maxZ);
        return new Vec3(clampedX, clampedY, clampedZ);
    }

    public static Vec3 getLookVectorFromYRotXRot(float yaw, float pitch) {
        float pitchRad = pitch * ((float)Math.PI / 180);
        float yawRad = -yaw * ((float)Math.PI / 180);
        float cosYaw = Mth.cos((double)yawRad);
        float sinYaw = Mth.sin((double)yawRad);
        float cosPitch = Mth.cos((double)pitchRad);
        float sinPitch = Mth.sin((double)pitchRad);
        return new Vec3((double)(sinYaw * cosPitch), (double)(-sinPitch), (double)(cosYaw * cosPitch));
    }

    public static EntityHitResult raycastTarget(Vec3 origin, Entity entity, double range, float yaw, float pitch) {
        Vec3 lookVec = RotationUtils.getLookVectorFromYRotXRot(yaw, pitch);
        Vec3 endVec = origin.add(lookVec.scale(range));
        float pickRadius = entity.getPickRadius();
        AABB box = entity.getBoundingBox().inflate((double)Math.max(pickRadius, 0.05f));
        if (box.clip(origin, endVec).isPresent()) {
            return new EntityHitResult(entity);
        }
        return null;
    }
}

