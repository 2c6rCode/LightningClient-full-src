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

    public static AABB getRadius(Entity entity, double d) {
        return new AABB((double)Mth.floor((double)(entity.getX() - d)), (double)Mth.floor((double)(entity.getY() - d)), (double)Mth.floor((double)(entity.getZ() - d)), (double)Mth.floor((double)(entity.getX() + d)), (double)Mth.floor((double)(entity.getY() + d)), (double)Mth.floor((double)(entity.getZ() + d)));
    }

    public static AABB extrapolate(Player player, int n) {
        return PositionUtils.extrapolate(player, n, true);
    }

    public static AABB extrapolate(Player player, int n, boolean bl) {
        if (player == null) {
            return null;
        }
        if (n <= 0 || PositionUtils.mc.level == null) {
            return player.getBoundingBox();
        }
        double d = player.getX() - player.xo;
        double d2 = bl ? player.getY() - player.yo : 0.0;
        double d3 = player.getZ() - player.zo;
        double d4 = 0.0;
        double d5 = 0.0;
        double d6 = 0.0;
        for (double d7 = 0.5; d7 <= (double)n; d7 += 0.5) {
            Vec3 vec3 = new Vec3(d * d7, d2 * d7, d3 * d7);
            if (PositionUtils.mc.level.noCollision((Entity)player, player.getBoundingBox().move(vec3))) {
                d4 = d * d7;
                d5 = d2 * d7;
                d6 = d3 * d7;
                continue;
            }
            if (!bl) break;
            Vec3 vec32 = new Vec3(d * d7, d5, d3 * d7);
            if (!PositionUtils.mc.level.noCollision((Entity)player, player.getBoundingBox().move(vec32))) break;
            d4 = d * d7;
            d6 = d3 * d7;
        }
        return player.getBoundingBox().move(new Vec3(d4, d5, d6));
    }
}

