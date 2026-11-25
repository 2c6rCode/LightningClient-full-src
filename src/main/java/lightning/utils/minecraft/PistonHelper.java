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
        double bestDistance = Double.MAX_VALUE;
        for (Direction direction2 : Direction.values()) {
            double distance;
            if (!PistonHelper.isGrimDirection(blockPos, direction2) || !((distance = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos.relative(direction2)))) < bestDistance)) continue;
            bestDistance = distance;
            direction = direction2;
        }
        return direction;
    }

    public static Direction getClickSide(BlockPos blockPos) {
        double distance;
        if (PistonHelper.mc.player == null) {
            return null;
        }
        double bestDistance = Double.MAX_VALUE;
        Direction direction = null;
        for (Direction direction2 : Direction.values()) {
            if (!PistonHelper.canSee(blockPos, direction2) || !((distance = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos.relative(direction2)))) < bestDistance)) continue;
            bestDistance = distance;
            direction = direction2;
        }
        if (direction != null) {
            return direction;
        }
        bestDistance = Double.MAX_VALUE;
        direction = Direction.UP;
        for (Direction direction2 : Direction.values()) {
            if (!PistonHelper.isGrimDirection(blockPos, direction2) || !((distance = PistonHelper.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos.relative(direction2)))) < bestDistance)) continue;
            bestDistance = distance;
            direction = direction2;
        }
        return direction;
    }

    public static Direction getPlaceSide(BlockPos blockPos, Predicate<Direction> predicate) {
        return PistonHelper.getPlaceSide(blockPos, predicate, false);
    }

    public static Direction getPlaceSide(BlockPos blockPos, Predicate<Direction> predicate, boolean ignoreInteractable) {
        double distance;
        Vec3 hitVec;
        BlockState blockState;
        BlockPos neighborPos;
        if (blockPos == null || PistonHelper.mc.player == null || PistonHelper.mc.level == null) {
            return null;
        }
        double bestDistance = Double.MAX_VALUE;
        Direction direction = null;
        for (Direction direction2 : Direction.values()) {
            if (predicate != null && !predicate.test(direction2)) continue;
            neighborPos = blockPos.relative(direction2);
            blockState = PistonHelper.mc.level.getBlockState(neighborPos);
            if (!PistonHelper.canClick(neighborPos, ignoreInteractable) || blockState.canBeReplaced() || !PistonHelper.isGrimDirection(neighborPos, direction2.getOpposite())) continue;
            hitVec = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction2.getStepX() * 0.5, (double)direction2.getStepY() * 0.5, (double)direction2.getStepZ() * 0.5);
            distance = PistonHelper.mc.player.getEyePosition().distanceToSqr(hitVec);
            if (!(distance < bestDistance)) continue;
            bestDistance = distance;
            direction = direction2;
        }
        if (direction != null) {
            return direction;
        }
        for (Direction direction2 : Direction.values()) {
            if (predicate != null && !predicate.test(direction2)) continue;
            neighborPos = blockPos.relative(direction2);
            blockState = PistonHelper.mc.level.getBlockState(neighborPos);
            if (!PistonHelper.canClick(neighborPos, ignoreInteractable) || blockState.canBeReplaced()) continue;
            hitVec = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction2.getStepX() * 0.5, (double)direction2.getStepY() * 0.5, (double)direction2.getStepZ() * 0.5);
            distance = PistonHelper.mc.player.getEyePosition().distanceToSqr(hitVec);
            if (!(distance < bestDistance)) continue;
            bestDistance = distance;
            direction = direction2;
        }
        return direction;
    }

    public static List<Direction> getPlaceSides(BlockPos blockPos, Predicate<Direction> predicate) {
        return PistonHelper.getPlaceSides(blockPos, predicate, false);
    }

    public static List<Direction> getPlaceSides(BlockPos blockPos, Predicate<Direction> predicate, boolean ignoreInteractable) {
        ArrayList<Direction> arrayList = new ArrayList<Direction>();
        if (blockPos == null || PistonHelper.mc.level == null) {
            return arrayList;
        }
        for (Direction direction : Direction.values()) {
            if (predicate != null && !predicate.test(direction)) continue;
            BlockPos neighborPos = blockPos.relative(direction);
            BlockState blockState = PistonHelper.mc.level.getBlockState(neighborPos);
            if (!PistonHelper.canClick(neighborPos, ignoreInteractable) || blockState.canBeReplaced() || !PistonHelper.isGrimDirection(neighborPos, direction.getOpposite())) continue;
            arrayList.add(direction);
        }
        return arrayList;
    }

    public static boolean canClick(BlockPos blockPos) {
        return PistonHelper.canClick(blockPos, false);
    }

    public static boolean canClick(BlockPos blockPos, boolean ignoreInteractable) {
        if (PistonHelper.mc.level == null || PistonHelper.mc.player == null) {
            return false;
        }
        BlockState blockState = PistonHelper.mc.level.getBlockState(blockPos);
        boolean interactable = WorldUtils.isInteractable(blockState);
        return blockState.isSolid() && (!interactable || PistonHelper.mc.player.isShiftKeyDown() || ignoreInteractable);
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

    public static boolean canPlace(BlockPos blockPos, Predicate<Direction> predicate, boolean ignoreSide) {
        return PistonHelper.canPlace(blockPos, predicate, ignoreSide, false);
    }

    public static boolean canPlace(BlockPos blockPos, Predicate<Direction> predicate, boolean ignoreSide, boolean ignoreCrystal) {
        if (!PistonHelper.canReplace(blockPos)) {
            return false;
        }
        if (PistonHelper.hasEntity(blockPos, ignoreCrystal)) {
            return false;
        }
        if (ignoreSide) {
            return true;
        }
        return PistonHelper.getPlaceSide(blockPos, predicate) != null;
    }

    public static boolean canPlaceCrystal(BlockPos blockPos) {
        return PistonHelper.canPlaceCrystal(blockPos, false);
    }

    public static boolean canPlaceCrystal(BlockPos blockPos, boolean ignoreSide) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        if (!PistonHelper.mc.level.getBlockState(blockPos).isAir()) {
            return false;
        }
        BlockPos abovePos = blockPos.above();
        if (!PistonHelper.mc.level.getBlockState(abovePos).isAir()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(blockPos, false) || PistonHelper.hasEntityBlockCrystal(abovePos, false)) {
            return false;
        }
        BlockPos belowPos = blockPos.below();
        Block block = PistonHelper.mc.level.getBlockState(belowPos).getBlock();
        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
            return false;
        }
        if (ignoreSide) {
            return true;
        }
        return PistonHelper.getClickSide(belowPos) != null;
    }

    public static boolean canPlaceCrystalIgnoreHeadroom(BlockPos blockPos, boolean ignoreSide) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        if (!PistonHelper.mc.level.getBlockState(blockPos).isAir()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(blockPos, false)) {
            return false;
        }
        BlockPos belowPos = blockPos.below();
        Block block = PistonHelper.mc.level.getBlockState(belowPos).getBlock();
        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
            return false;
        }
        if (ignoreSide) {
            return true;
        }
        return PistonHelper.getClickSide(belowPos) != null;
    }

    public static boolean hasEntity(BlockPos blockPos, boolean ignoreCrystal) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB aABB = new AABB(blockPos).deflate(0.01);
        for (Entity entity : PistonHelper.mc.level.getEntitiesOfClass(Entity.class, aABB)) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof Projectile || ignoreCrystal && entity instanceof EndCrystal) continue;
            return true;
        }
        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos blockPos, boolean ignoreCrystal) {
        if (PistonHelper.mc.level == null) {
            return false;
        }
        AABB aABB = new AABB(blockPos);
        for (Entity entity : PistonHelper.mc.level.getEntitiesOfClass(Entity.class, aABB)) {
            if (!entity.isAlive() || ignoreCrystal && entity instanceof EndCrystal) continue;
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
        BlockPos neighborPos = blockPos.relative(direction);
        Vec3 hitVec = new Vec3((double)neighborPos.getX() + 0.5 + (double)direction.getOpposite().getStepX() * 0.5, (double)neighborPos.getY() + 0.5 + (double)direction.getOpposite().getStepY() * 0.5, (double)neighborPos.getZ() + 0.5 + (double)direction.getOpposite().getStepZ() * 0.5);
        BlockHitResult blockHitResult = new BlockHitResult(hitVec, direction.getOpposite(), neighborPos, false);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, sequence));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        placeList.add(blockPos);
    }

    public static void clickBlock(BlockPos blockPos, Direction direction, boolean rotate) {
        if (PistonHelper.mc.player == null || mc.getConnection() == null) {
            return;
        }
        Vec3 hitVec = new Vec3((double)blockPos.getX() + 0.5 + (double)direction.getStepX() * 0.5, (double)blockPos.getY() + 0.5 + (double)direction.getStepY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)direction.getStepZ() * 0.5);
        if (rotate) {
            float[] rotations = RotationUtils.getRotations(hitVec);
            Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
        }
        BlockHitResult blockHitResult = new BlockHitResult(hitVec, direction, blockPos, false);
        NetworkUtils.sendSequencedPacket(sequence -> PistonHelper.createClickPacket(blockHitResult, sequence));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        placeList.add(blockPos);
    }

    public static void placeBlock(BlockPos blockPos, Direction direction, boolean rotate) {
        PistonHelper.clickBlock(blockPos.relative(direction), direction.getOpposite(), rotate);
    }

    public static boolean canSee(BlockPos blockPos, Direction direction) {
        if (PistonHelper.mc.level == null || PistonHelper.mc.player == null) {
            return false;
        }
        Vec3 faceCenter = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
        BlockHitResult blockHitResult = PistonHelper.mc.level.clip(new ClipContext(PistonHelper.mc.player.getEyePosition(), faceCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PistonHelper.mc.player));
        return blockHitResult == null || blockHitResult.getType() == HitResult.Type.MISS;
    }

    public static Vec3 getClosestPointToBox(Vec3 point, AABB box) {
        return new Vec3(Mth.clamp((double)point.x, (double)box.minX, (double)box.maxX), Mth.clamp((double)point.y, (double)box.minY, (double)box.maxY), Mth.clamp((double)point.z, (double)box.minZ, (double)box.maxZ));
    }

    private static AABB getCombinedBox(BlockPos blockPos) {
        if (PistonHelper.mc.level == null) {
            return new AABB(blockPos);
        }
        AABB combinedBox = new AABB(blockPos);
        for (AABB shapeBox : PistonHelper.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)PistonHelper.mc.level, blockPos).toAabbs()) {
            AABB movedBox = shapeBox.move((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
            double minX = Math.max(movedBox.minX, combinedBox.minX);
            double minY = Math.max(movedBox.minY, combinedBox.minY);
            double minZ = Math.max(movedBox.minZ, combinedBox.minZ);
            double maxX = Math.min(movedBox.maxX, combinedBox.maxX);
            double maxY = Math.min(movedBox.maxY, combinedBox.maxY);
            double maxZ = Math.min(movedBox.maxZ, combinedBox.maxZ);
            combinedBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return combinedBox;
    }

    private static boolean isIntersected(AABB first, AABB second) {
        return second.maxX - 1.0E-7 > first.minX && second.minX + 1.0E-7 < first.maxX && second.maxY - 1.0E-7 > first.minY && second.minY + 1.0E-7 < first.maxY && second.maxZ - 1.0E-7 > first.minZ && second.minZ + 1.0E-7 < first.maxZ;
    }

    private static Packet createClickPacket(BlockHitResult blockHitResult, int sequence) {
        return new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, sequence);
    }
}

