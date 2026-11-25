/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.utils.minecraft;

import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PositionUtils
implements IMinecraft {
    public static BlockPos getFlooredPosition(Entity entity) {
        return new BlockPos(entity.getBlockX(), Mth.floor((double)(entity.getY() - Math.floor(entity.getY()) > 0.8 ? Math.floor(entity.getY()) + 1.0 : Math.floor(entity.getY()))), entity.getBlockZ());
    }

    public static AABB getRadius(Entity entity, double radius) {
        return new AABB((double)Mth.floor((double)(entity.getX() - radius)), (double)Mth.floor((double)(entity.getY() - radius)), (double)Mth.floor((double)(entity.getZ() - radius)), (double)Mth.floor((double)(entity.getX() + radius)), (double)Mth.floor((double)(entity.getY() + radius)), (double)Mth.floor((double)(entity.getZ() + radius)));
    }

    public static AABB extrapolate(Player player, int ticks) {
        return PositionUtils.extrapolate(player, ticks, true);
    }

    public static AABB extrapolate(Player player, int ticks, boolean includeY) {
        if (player == null) {
            return null;
        }
        if (ticks <= 0 || PositionUtils.mc.level == null) {
            return player.getBoundingBox();
        }
        double deltaX = player.getX() - player.xo;
        double deltaY = includeY ? player.getY() - player.yo : 0.0;
        double deltaZ = player.getZ() - player.zo;
        double offsetX = 0.0;
        double offsetY = 0.0;
        double offsetZ = 0.0;
        for (double step = 0.5; step <= (double)ticks; step += 0.5) {
            Vec3 candidateOffset = new Vec3(deltaX * step, deltaY * step, deltaZ * step);
            if (PositionUtils.mc.level.noCollision((Entity)player, player.getBoundingBox().move(candidateOffset))) {
                offsetX = deltaX * step;
                offsetY = deltaY * step;
                offsetZ = deltaZ * step;
                continue;
            }
            if (!includeY) break;
            Vec3 horizontalOffset = new Vec3(deltaX * step, offsetY, deltaZ * step);
            if (!PositionUtils.mc.level.noCollision((Entity)player, player.getBoundingBox().move(horizontalOffset))) break;
            offsetX = deltaX * step;
            offsetZ = deltaZ * step;
        }
        return player.getBoundingBox().move(new Vec3(offsetX, offsetY, offsetZ));
    }
}

