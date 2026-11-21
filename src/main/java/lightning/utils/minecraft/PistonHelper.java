/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.utils.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import lightning.Night;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PistonHelper
implements IMinecraft {
    public static final CopyOnWriteArrayList<BlockPos> placeList = new CopyOnWriteArrayList();

    public static boolean isGrimDirection(BlockPos blockPos, Direction direction) {
        if (PistonHelper.mc.player == null || PistonHelper.mc.level == null) {
            return false;
        }
        AABB aABB = PistonHelper.getCombinedBox(blockPos);
        AABB aABB2 = new AABB(PistonHelper.mc.player.getX(), PistonHelper.mc.player.getY() + 0.4, PistonHelper.mc.player.getZ(), PistonHelper.mc.player.getX(), PistonHelper.mc.player.getY() + 1.62, PistonHelper.mc.player.getZ()).inflate(2.0E-4);
        if (PistonHelper.isIntersected(aABB2, aABB)) {
            return true;
        }
        return switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> {
                if (aABB2.minZ <= aABB.minZ) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (aABB2.maxZ >= aABB.maxZ) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (aABB2.maxX >= aABB.maxX) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (aABB2.minX <= aABB.minX) {
                    yield true;
                }
                yield false;
            }
            case Direction.UP -> {
                if (aABB2.maxY >= aABB.maxY) {
                    yield true;
                }
                yield false;
            }
            case Direction.DOWN -> aABB2.minY <= aABB.minY;
        };
    }

    public static Direction getClickSideStrict(BlockPos blockPos) {
        if (PistonHelper.mc.player == null) {
            return null;
        }
        Direction direction = null;
        double d = Double.MAX_VALUE;
        for (Direction direction2 : Direction.values()) {
            double d2;
            if (!PistonHelper.isGrimDirection(blockPos, direction2) || !((d2 = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos.relative(direction2)))) < d)) continue;
            d = d2;
            direction = direction2;
        }
        return direction;
    }

    public static Direction getClickSide(BlockPos blockPos) {
        double d;
        if (PistonHelper.mc.player == null) {
            return null;
        }
        double d2 = Double.MAX_VALUE;
        Direction direction = null;
        for (Direction direction2 : Direction.values()) {
            if (!PistonHelper.canSee(blockPos, direction2) || !((d = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos.relative(direction2)))) < d2)) continue;
            d2 = d;
            direction = direction2;
        }
        if (direction != null) {
            return direction;
        }
        d2 = Double.MAX_VALUE;
        direction = Direction.UP;
        for (Direction direction2 : Direction.values()) {
            if (!PistonHelper.isGrimDirection(blockPos, direction2) || !((d = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos.relative(direction2)))) < d2)) continue;
            d2 = d;
            direction = direction2;
        }
        return direction;
    }

    public static Direction getPlaceSide(BlockPos blockPos, Predicate<Direction> predicate) {
        return PistonHelper.getPlaceSide(blockPos, predicate, false);
    }

    public static Direction getPlaceSide(BlockPos blockPos, Predicate<Direction> predicate, boolean bl) {
        double d;
        Vec3 vec3;
        BlockState blockState;
        BlockPos blockPos2;
        if (blockPos == null || PistonHelper.mc.player == null || PistonHelper.mc.level == null) {
            return null;
        }
        double d2 = Double.MAX_VALUE;
        Direction direction = null;
        for (Direction direction2 : Direction.values()) {
            if (predicate != null && !predicate.test(direction2)) continue;
            blockPos2 = blockPos.relative(direction2);
            blockState = PistonHelper.mc.level.getBlockState(blockPos2);
            if (!PistonHelper.canClick(blockPos2, bl) || blockState.canBeReplaced() || !PistonHelper.isGrimDirection(blockPos2, direction2.getOpposite())) continue;
            vec3 = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction2.getStepX() * 0.5, (double)direction2.getStepY() * 0.5, (double)direction2.getStepZ() * 0.5);
            d = PistonHelper.mc.player.getEyePosition().distanceToSqr(vec3);
            if (!(d < d2)) continue;
            d2 = d;
            direction = direction2;
        }
        if (direction != null) {
            return direction;
        }
        for (Direction direction2 : Direction.values()) {
            if (predicate != null && !predicate.test(direction2)) continue;
            blockPos2 = blockPos.relative(direction2);
            blockState = PistonHelper.mc.level.getBlockState(blockPos2);
            if (!PistonHelper.canClick(blockPos2, bl) || blockState.canBeReplaced()) continue;
            vec3 = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction2.getStepX() * 0.5, (double)direction2.getStepY() * 0.5, (double)direction2.getStepZ() * 0.5);
            d = PistonHelper.mc.player.getEyePosition().distanceToSqr(vec3);
            if (!(d < d2)) continue;
            d2 = d;
            direction = direction2;
        }
        return direction;
    }

    public static List<Direction> getPlaceSides(BlockPos blockPos, Predicate<Direction> predicate) {
        return PistonHelper.getPlaceSides(blockPos, predicate, false);
    }

    public static List<Direction> getPlaceSides(BlockPos blockPos, Predicate<Direction> predicate, boolean bl) {
        ArrayList<Direction> arrayList = new ArrayList<Direction>();
        if (blockPos == null || PistonHelper.mc.level == null) {
            return arrayList;
        }
        for (Direction direction : Direction.values()) {
            if (predicate != null && !predicate.test(direction)) continue;
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState = PistonHelper.mc.level.getBlockState(blockPos2);
            if (!PistonHelper.canClick(blockPos2, bl) || blockState.canBeReplaced() || !PistonHelper.isGrimDirection(blockPos2, direction.getOpposite())) continue;
            arrayList.add(direction);
        }
        return arrayList;
    }

    public static boolean canClick(BlockPos blockPos) {
        return PistonHelper.canClick(blockPos, false);
    }

    public static boolean canClick(BlockPos blockPos, boolean bl) {
        if (PistonHelper.mc.level == null || PistonHelper.mc.player == null) {
            return false;
        }
        BlockState blockState = PistonHelper.mc.level.getBlockState(blockPos);
        boolean bl2 = WorldUtils.isInteractable(blockState);
        return blockState.isSolid() && (!bl2 || PistonHelper.mc.player.isShiftKeyDown() || bl);
    }

    public static boolean canReplace(BlockPos blockPos) {
        if (PistonHelper.mc.level == null || blockPos == null || blockPos.getY() >= 320 || blockPos.getY() < -64) {
            return false;
        }
        return PistonHelper.mc.level.getBlockState(blockPos).canBeReplaced();
    }

    public static boolean canPlace(BlockPos blockPos) {
        return PistonHelper.canPlace(blockPos, null, false, false);
    }

    public static boolean canPlace(BlockPos blockPos, Predicate<Direction> predicate) {
        return PistonHelper.canPlace(blockPos, predicate, false, false);
    }

    public static boolean canPlace(BlockPos blockPos, Predicate<Direction> predicate, boolean bl) {
        return PistonHelper.canPlace(blockPos, predicate, bl, false);
    }

    public static boolean canPlace(BlockPos blockPos, Predicate<Direction> predicate, boolean bl, boolean bl2) {
        if (!PistonHelper.canReplace(blockPos)) {
            return false;
        }
        if (PistonHelper.hasEntity(blockPos, bl2)) {
            return false;
        }
        if (bl) {
            return true;
        }
        return PistonHelper.getPlaceSide(blockPos, predicate) != null;
    }

    public static boolean canPlaceCrystal(BlockPos blockPos) {
        return PistonHelper.canPlaceCrystal(blockPos, false);
    }

    public static boolean canPlaceCrystal(BlockPos blockPos, boolean bl) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        if (!PistonHelper.mc.level.getBlockState(blockPos).isAir()) {
            return false;
        }
        BlockPos blockPos2 = blockPos.above();
        if (!PistonHelper.mc.level.getBlockState(blockPos2).isAir()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(blockPos, false) || PistonHelper.hasEntityBlockCrystal(blockPos2, false)) {
            return false;
        }
        BlockPos blockPos3 = blockPos.below();
        Block block = PistonHelper.mc.level.getBlockState(blockPos3).getBlock();
        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
            return false;
        }
        if (bl) {
            return true;
        }
        return PistonHelper.getClickSide(blockPos3) != null;
    }

    public static boolean canPlaceCrystalIgnoreHeadroom(BlockPos blockPos, boolean bl) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        if (!PistonHelper.mc.level.getBlockState(blockPos).isAir()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(blockPos, false)) {
            return false;
        }
        BlockPos blockPos2 = blockPos.below();
        Block block = PistonHelper.mc.level.getBlockState(blockPos2).getBlock();
        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
            return false;
        }
        if (bl) {
            return true;
        }
        return PistonHelper.getClickSide(blockPos2) != null;
    }

    public static boolean hasEntity(BlockPos blockPos, boolean bl) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB aABB = new AABB(blockPos).deflate(0.01);
        for (Entity entity : PistonHelper.mc.level.getEntitiesOfClass(Entity.class, aABB)) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof Projectile || bl && entity instanceof EndCrystal) continue;
            return true;
        }
        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos blockPos, boolean bl) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB aABB = new AABB(blockPos);
        for (Entity entity : PistonHelper.mc.level.getEntitiesOfClass(Entity.class, aABB)) {
            if (!entity.isAlive() || bl && entity instanceof EndCrystal) continue;
            return true;
        }
        return false;
    }

    public static boolean hasCrystal(BlockPos blockPos) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB aABB = new AABB(blockPos);
        for (EndCrystal endCrystal : PistonHelper.mc.level.getEntitiesOfClass(EndCrystal.class, aABB)) {
            if (!endCrystal.isAlive()) continue;
            return true;
        }
        return false;
    }

    public static boolean hasCrystalAt(BlockPos blockPos) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB aABB = new AABB(blockPos);
        for (EndCrystal endCrystal : PistonHelper.mc.level.getEntitiesOfClass(EndCrystal.class, aABB)) {
            if (!endCrystal.isAlive() || !endCrystal.blockPosition().equals((Object)blockPos)) continue;
            return true;
        }
        return false;
    }

    public static void placeBlockDirect(BlockPos blockPos, Direction direction) {
        if (PistonHelper.mc.player == null || mc.getConnection() == null) {
            return;
        }
        BlockPos blockPos2 = blockPos.relative(direction);
        Vec3 vec3 = new Vec3((double)blockPos2.getX() + 0.5 + (double)direction.getOpposite().getStepX() * 0.5, (double)blockPos2.getY() + 0.5 + (double)direction.getOpposite().getStepY() * 0.5, (double)blockPos2.getZ() + 0.5 + (double)direction.getOpposite().getStepZ() * 0.5);
        BlockHitResult blockHitResult = new BlockHitResult(vec3, direction.getOpposite(), blockPos2, false);
        NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, n));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        placeList.add(blockPos);
    }

    public static void clickBlock(BlockPos blockPos, Direction direction, boolean bl) {
        if (PistonHelper.mc.player == null || mc.getConnection() == null) {
            return;
        }
        Vec3 vec3 = new Vec3((double)blockPos.getX() + 0.5 + (double)direction.getStepX() * 0.5, (double)blockPos.getY() + 0.5 + (double)direction.getStepY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)direction.getStepZ() * 0.5);
        if (bl) {
            float[] rotations = RotationUtils.getRotations(vec3);
            Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
        }
        BlockHitResult blockHitResult = new BlockHitResult(vec3, direction, blockPos, false);
        NetworkUtils.sendSequencedPacket(arg_0 -> PistonHelper.lambda$clickBlock$0(blockHitResult, arg_0));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        placeList.add(blockPos);
    }

    public static void placeBlock(BlockPos blockPos, Direction direction, boolean bl) {
        PistonHelper.clickBlock(blockPos.relative(direction), direction.getOpposite(), bl);
    }

    public static boolean canSee(BlockPos blockPos, Direction direction) {
        if (PistonHelper.mc.level == null || PistonHelper.mc.player == null) {
            return false;
        }
        Vec3 vec3 = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
        BlockHitResult blockHitResult = PistonHelper.mc.level.clip(new ClipContext(PistonHelper.mc.player.getEyePosition(), vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PistonHelper.mc.player));
        return blockHitResult == null || blockHitResult.getType() == HitResult.Type.MISS;
    }

    public static Vec3 getClosestPointToBox(Vec3 vec3, AABB aABB) {
        return new Vec3(Mth.clamp((double)vec3.x, (double)aABB.minX, (double)aABB.maxX), Mth.clamp((double)vec3.y, (double)aABB.minY, (double)aABB.maxY), Mth.clamp((double)vec3.z, (double)aABB.minZ, (double)aABB.maxZ));
    }

    private static AABB getCombinedBox(BlockPos blockPos) {
        if (PistonHelper.mc.level == null) {
            return new AABB(blockPos);
        }
        AABB aABB = new AABB(blockPos);
        for (AABB aABB2 : PistonHelper.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)PistonHelper.mc.level, blockPos).toAabbs()) {
            AABB aABB3 = aABB2.move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
            double d = Math.max(aABB3.minX, aABB.minX);
            double d2 = Math.max(aABB3.minY, aABB.minY);
            double d3 = Math.max(aABB3.minZ, aABB.minZ);
            double d4 = Math.min(aABB3.maxX, aABB.maxX);
            double d5 = Math.min(aABB3.maxY, aABB.maxY);
            double d6 = Math.min(aABB3.maxZ, aABB.maxZ);
            aABB = new AABB(d, d2, d3, d4, d5, d6);
        }
        return aABB;
    }

    private static boolean isIntersected(AABB aABB, AABB aABB2) {
        return aABB2.maxX - 1.0E-7 > aABB.minX && aABB2.minX + 1.0E-7 < aABB.maxX && aABB2.maxY - 1.0E-7 > aABB.minY && aABB2.minY + 1.0E-7 < aABB.maxY && aABB2.maxZ - 1.0E-7 > aABB.minZ && aABB2.minZ + 1.0E-7 < aABB.maxZ;
    }

    private static /* synthetic */ Packet lambda$clickBlock$0(BlockHitResult blockHitResult, int n) {
        return new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, n);
    }
}

