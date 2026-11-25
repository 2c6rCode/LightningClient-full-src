/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.utils.minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lightning.Night;
import lightning.modules.impl.movement.HitboxDesyncModule;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HoleUtils
implements IMinecraft {
    private static final Vec3i[] holeOffsets = new Vec3i[]{new Vec3i(0, -1, 0), new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)};
    private static final Vec3i[] singleOffsets = new Vec3i[]{new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(0, -1, 0)};
    private static final Vec3i[] doubleXOffsets = new Vec3i[]{new Vec3i(-1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(0, -1, 0), new Vec3i(2, 0, 0), new Vec3i(1, 0, -1), new Vec3i(1, 0, 1), new Vec3i(1, -1, 0)};
    private static final Vec3i[] doubleZOffsets = new Vec3i[]{new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, -1, 0), new Vec3i(0, 0, 2), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1), new Vec3i(0, -1, 1)};
    private static final Vec3i[] quadOffsets = new Vec3i[]{new Vec3i(-1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, -1, 0), new Vec3i(2, 0, 0), new Vec3i(1, 0, -1), new Vec3i(1, -1, 0), new Vec3i(-1, 0, 1), new Vec3i(0, 0, 2), new Vec3i(0, -1, 1), new Vec3i(2, 0, 1), new Vec3i(1, 0, 2), new Vec3i(1, -1, 1)};

    public static boolean isPlayerInHole(Player player) {
        return HoleUtils.getFeetPositions(player, true, true, false).stream().noneMatch(blockPos -> HoleUtils.mc.level.getBlockState(blockPos).canBeReplaced());
    }

    public static List<BlockPos> getInsidePositions(Entity entity) {
        LivingEntity livingEntity;
        if (entity == null || HoleUtils.mc.level == null) {
            return Collections.emptyList();
        }
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        AABB aABB = entity.getBoundingBox();
        boolean isHorizontal = entity.isSwimming() || entity.isVisuallySwimming() || entity.isVisuallyCrawling() || entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).isFallFlying() || entity.getPose() == Pose.SWIMMING || entity.getPose() == Pose.FALL_FLYING || aABB.maxY - aABB.minY <= 1.0;
        if (isHorizontal) {
            float yaw = entity.getYRot();
            double yawRad = Math.toRadians(yaw);
            double forwardX = -Math.sin(yawRad);
            double forwardZ = Math.cos(yawRad);
            double entityX = entity.getX();
            double entityY = entity.getY();
            double entityZ = entity.getZ();
            for (double forwardStep = -0.9; forwardStep <= 0.9; forwardStep += 0.2) {
                for (double sideStep = -0.3; sideStep <= 0.3; sideStep += 0.3) {
                    double sampleX = entityX + forwardX * forwardStep - forwardZ * sideStep;
                    double sampleZ = entityZ + forwardZ * forwardStep + forwardX * sideStep;
                    BlockPos blockPos = new BlockPos((int)Math.floor(sampleX), (int)Math.floor(entityY), (int)Math.floor(sampleZ));
                    if (arrayList.contains(blockPos)) continue;
                    arrayList.add(blockPos);
                }
            }
            int minX = (int)Math.floor(aABB.minX);
            int maxX = (int)Math.ceil(aABB.maxX);
            int minY = (int)Math.floor(aABB.minY);
            int maxY = (int)Math.ceil(aABB.maxY);
            int minZ = (int)Math.floor(aABB.minZ);
            int maxZ = (int)Math.ceil(aABB.maxZ);
            for (int i = minX; i < maxX; ++i) {
                for (int j = minY; j < maxY; ++j) {
                    for (int k = minZ; k < maxZ; ++k) {
                        BlockPos blockPos = new BlockPos(i, j, k);
                        if (arrayList.contains(blockPos)) continue;
                        arrayList.add(blockPos);
                    }
                }
            }
        } else {
            int minX = (int)Math.floor(aABB.minX);
            int maxX = (int)Math.ceil(aABB.maxX);
            int minY = (int)Math.floor(aABB.minY);
            int minZ = (int)Math.floor(aABB.minZ);
            int maxZ = (int)Math.ceil(aABB.maxZ);
            int maxY = (int)Math.ceil(aABB.maxY);
            for (int i = minX; i < maxX; ++i) {
                for (int j = minY; j < maxY; ++j) {
                    for (int k = minZ; k < maxZ; ++k) {
                        BlockPos blockPos = new BlockPos(i, j, k);
                        if (arrayList.contains(blockPos)) continue;
                        arrayList.add(blockPos);
                    }
                }
            }
        }
        if (arrayList.isEmpty()) {
            arrayList.add(PositionUtils.getFlooredPosition(entity));
        }
        return arrayList;
    }

    public static BlockPos getTargetFeetPosition(Player player) {
        if (player == null || HoleUtils.mc.level == null) {
            return BlockPos.ZERO;
        }
        BlockPos blockPos = PositionUtils.getFlooredPosition((Entity)player);
        if (HoleUtils.mc.level.getBlockState(blockPos.below()).canBeReplaced()) {
            for (int i = 2; i <= 3; ++i) {
                BlockPos belowPos = blockPos.below(i);
                if (HoleUtils.mc.level.getBlockState(belowPos).canBeReplaced()) continue;
                return belowPos.above();
            }
        }
        return blockPos;
    }

    public static HashSet<BlockPos> getFeetPositions(Player player, boolean extrapolate, boolean includeFloor, boolean ignoreFriends) {
        return HoleUtils.getFeetPositions(player, extrapolate, includeFloor, includeFloor, ignoreFriends);
    }

    public static HashSet<BlockPos> getFeetPositions(Player player, boolean extrapolate, boolean includeFloor, boolean includeCenterFloor, boolean ignoreFriends) {
        BlockPos neighborPos;
        HashSet<BlockPos> positions = new HashSet<BlockPos>();
        HashSet<BlockPos> occupiedCells = new HashSet<BlockPos>();
        HitboxDesyncModule hitboxDesyncModule = Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
        AABB aABB = player.getBoundingBox();
        double yFraction = aABB.minY - Math.floor(aABB.minY);
        int feetY = (int)(yFraction > 0.8 ? Math.floor(aABB.minY) + 1.0 : Math.floor(aABB.minY));
        int minX = (int)Math.floor(aABB.minX);
        int maxX = (int)Math.floor(aABB.maxX);
        int minZ = (int)Math.floor(aABB.minZ);
        int maxZ = (int)Math.floor(aABB.maxZ);
        BlockPos feetPos = new BlockPos((int)Math.floor(player.getX()), feetY, (int)Math.floor(player.getZ()));
        if (HoleUtils.mc.level != null && !player.onGround() && HoleUtils.mc.level.getBlockState(feetPos.below()).canBeReplaced()) {
            for (int i = 2; i <= 3; ++i) {
                BlockPos belowPos = feetPos.below(i);
                if (HoleUtils.mc.level.getBlockState(belowPos).canBeReplaced()) continue;
                feetY = belowPos.above().getY();
                break;
            }
        }
        BlockPos flooredPos = PositionUtils.getFlooredPosition((Entity)player);
        occupiedCells.add(new BlockPos(flooredPos.getX(), feetY, flooredPos.getZ()));
        for (int i = minX; i <= maxX; ++i) {
            for (int j = minZ; j <= maxZ; ++j) {
                if (!aABB.intersects(new AABB((double)i, (double)feetY, (double)j, (double)(i + 1), (double)(feetY + 1), (double)(j + 1)))) continue;
                occupiedCells.add(new BlockPos(i, feetY, j));
            }
        }
        if (extrapolate && HoleUtils.mc.level != null) {
            for (BlockPos cell : Set.copyOf(occupiedCells)) {
                for (Direction direction : Direction.values()) {
                    List<Player> list;
                    if (direction.getAxis().isVertical() || !HoleUtils.mc.level.getBlockState(neighborPos = cell.relative(direction)).canBeReplaced() || (list = WorldUtils.getCollisions(neighborPos)).isEmpty()) continue;
                    for (Player player2 : list) {
                        if (player2 == player || player2 == HoleUtils.mc.player && player == HoleUtils.mc.player || ignoreFriends && player2 != player || player == HoleUtils.mc.player && !Night.FRIEND_MANAGER.contains(player2.getName().getString()) || Math.abs(player2.getY() - player.getY()) > 1.0) continue;
                        AABB aABB2 = player2.getBoundingBox();
                        int collMinX = (int)Math.floor(aABB2.minX + 0.001);
                        int collMaxX = (int)Math.floor(aABB2.maxX - 0.001);
                        int collMinZ = (int)Math.floor(aABB2.minZ + 0.001);
                        int collMaxZ = (int)Math.floor(aABB2.maxZ - 0.001);
                        for (int i = collMinX; i <= collMaxX; ++i) {
                            for (int j = collMinZ; j <= collMaxZ; ++j) {
                                occupiedCells.add(new BlockPos(i, feetY, j));
                            }
                        }
                    }
                }
            }
        }
        for (BlockPos cell : occupiedCells) {
            if (includeFloor && includeCenterFloor) {
                positions.add(cell.below());
            }
            for (Direction direction : Direction.values()) {
                if (!direction.getAxis().isHorizontal() || occupiedCells.contains(neighborPos = cell.relative(direction))) continue;
                positions.add(neighborPos);
                if (!includeFloor || HoleUtils.mc.level == null || !HoleUtils.mc.level.getBlockState(neighborPos).canBeReplaced()) continue;
                positions.add(neighborPos.below());
            }
        }
        int finalFeetY = feetY;
        positions.removeIf(blockPos -> blockPos.getY() == finalFeetY && player.getBoundingBox().intersects(new AABB(blockPos)));
        if (player == HoleUtils.mc.player && hitboxDesyncModule.isToggled() && hitboxDesyncModule.close.getValue()) {
            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
            Vec3 centerPos = Vec3.atCenterOf((Vec3i)HoleUtils.mc.player.blockPosition());
            boolean offsetXPositive = centerPos.x - HoleUtils.mc.player.getX() > 0.0;
            boolean offsetZPositive = centerPos.z - HoleUtils.mc.player.getZ() > 0.0;
            if (offsetXPositive && offsetZPositive) {
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(-1, 0, 0)));
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(0, 0, -1)));
            }
            if (!offsetXPositive && offsetZPositive) {
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(1, 0, 0)));
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(0, 0, -1)));
            }
            if (offsetXPositive && !offsetZPositive) {
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(-1, 0, 0)));
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(0, 0, 1)));
            }
            if (!offsetXPositive && !offsetZPositive) {
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(1, 0, 0)));
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(0, 0, 1)));
            }
            positions.removeIf(arrayList::contains);
        }
        return positions;
    }

    public static List<BlockPos> getTrapPositions(Player player, boolean fullTrap, boolean roof, boolean antiStep, boolean head, boolean strictDirection) {
        return HoleUtils.getTrapPositions(player, fullTrap, roof, antiStep, head, strictDirection, false, false);
    }

    public static List<BlockPos> getTrapPositions(Player player, boolean fullTrap, boolean roof, boolean antiStep, boolean head, boolean strictDirection, boolean airPlace) {
        return HoleUtils.getTrapPositions(player, null, fullTrap, roof, antiStep, head, strictDirection, false, airPlace);
    }

    public static List<BlockPos> getTrapPositions(Player player, BlockPos overridePos, boolean fullTrap, boolean roof, boolean antiStep, boolean head, boolean strictDirection, boolean airPlace) {
        return HoleUtils.getTrapPositions(player, overridePos, fullTrap, roof, antiStep, head, strictDirection, false, airPlace);
    }

    public static List<BlockPos> getTrapPositions(Player player, boolean fullTrap, boolean roof, boolean antiStep, boolean head, boolean strictDirection, boolean feet, boolean airPlace) {
        return HoleUtils.getTrapPositions(player, null, fullTrap, roof, antiStep, head, strictDirection, feet, airPlace);
    }

    public static List<BlockPos> getTrapPositions(Player player, BlockPos overridePos, boolean fullTrap, boolean roof, boolean antiStep, boolean head, boolean strictDirection, boolean feet, boolean airPlace) {
        HashSet<BlockPos> occupiedCells;
        LinkedHashSet<BlockPos> linkedHashSet = new LinkedHashSet<BlockPos>();
        if (overridePos != null && !player.onGround()) {
            occupiedCells = new HashSet<BlockPos>();
            occupiedCells.add(overridePos);
        } else {
            occupiedCells = HoleUtils.getOccupiedFloorCells(player);
        }
        for (BlockPos cell : occupiedCells) {
            BlockPos roofPos;
            Direction direction = null;
            for (Direction direction2 : Direction.values()) {
                if (!direction2.getAxis().isHorizontal() || occupiedCells.contains(cell.relative(direction2))) continue;
                if (direction == null) {
                    direction = direction2;
                }
                if (WorldUtils.getDirection(cell.relative(direction2).above(), strictDirection) == null) continue;
                direction = direction2;
                break;
            }
            if (direction == null) {
                direction = Direction.EAST;
            }
            if (antiStep) {
                HoleUtils.addWallOffset(linkedHashSet, occupiedCells, cell, new Vec3i(1, 2, 0));
                HoleUtils.addWallOffset(linkedHashSet, occupiedCells, cell, new Vec3i(-1, 2, 0));
                HoleUtils.addWallOffset(linkedHashSet, occupiedCells, cell, new Vec3i(0, 2, 1));
                HoleUtils.addWallOffset(linkedHashSet, occupiedCells, cell, new Vec3i(0, 2, -1));
            }
            if (head) {
                linkedHashSet.add(cell.offset(0, 3, 0));
            }
            if (feet) {
                for (Direction direction2 : Direction.values()) {
                    if (!direction2.getAxis().isHorizontal() || occupiedCells.contains(cell.relative(direction2))) continue;
                    linkedHashSet.add(cell.relative(direction2));
                }
            }
            if (fullTrap) {
                roofPos = cell.offset(0, 2, 0);
                if (HoleUtils.mc.level != null && !HoleUtils.mc.level.getBlockState(roofPos).canBeReplaced()) {
                    linkedHashSet.add(roofPos);
                    continue;
                }
                if (!airPlace && WorldUtils.getDirection(roofPos, strictDirection) == null) {
                    linkedHashSet.add(cell.relative(direction).above());
                    linkedHashSet.add(cell.relative(direction).above(2));
                    linkedHashSet.add(roofPos);
                    continue;
                }
                linkedHashSet.add(roofPos);
                continue;
            }
            for (Direction direction2 : Direction.values()) {
                if (!direction2.getAxis().isHorizontal() || occupiedCells.contains(cell.relative(direction2))) continue;
                linkedHashSet.add(cell.relative(direction2).above());
            }
            if (!roof) continue;
            roofPos = cell.offset(0, 2, 0);
            if (!airPlace && (HoleUtils.mc.level == null || HoleUtils.mc.level.getBlockState(roofPos).canBeReplaced())) {
                linkedHashSet.add(cell.relative(direction).above(2));
            }
            linkedHashSet.add(roofPos);
        }
        return new ArrayList<BlockPos>(linkedHashSet);
    }

    private static void addWallOffset(Set<BlockPos> set, HashSet<BlockPos> occupiedCells, BlockPos origin, Vec3i offset) {
        BlockPos wallPos;
        if ((offset.getX() != 0 || offset.getZ() != 0) && occupiedCells.contains(wallPos = origin.offset(offset.getX(), 0, offset.getZ()))) {
            return;
        }
        set.add(origin.offset(offset));
    }

    private static HashSet<BlockPos> getOccupiedFloorCells(Player player) {
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        BlockPos blockPos = PositionUtils.getFlooredPosition((Entity)player);
        hashSet.add(blockPos);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isVertical()) continue;
            AABB aABB = player.getBoundingBox();
            BlockPos neighborPos = blockPos.relative(direction);
            if (!(aABB.minX < (double)(neighborPos.getX() + 1) && aABB.maxX > (double)neighborPos.getX() && aABB.minZ < (double)(neighborPos.getZ() + 1) && aABB.maxZ > (double)neighborPos.getZ())) continue;
            int cellX = (int)Math.floor(aABB.minX);
            while ((double)cellX < Math.ceil(aABB.maxX)) {
                int cellZ = (int)Math.floor(aABB.minZ);
                while ((double)cellZ < Math.ceil(aABB.maxZ)) {
                    hashSet.add(new BlockPos(cellX, blockPos.getY(), cellZ));
                    ++cellZ;
                }
                ++cellX;
            }
        }
        return hashSet;
    }

    private static HoleSafety classifyWalls(BlockPos blockPos, Vec3i[] offsets) {
        boolean allBedrock = true;
        boolean allValid = true;
        for (Vec3i offset : offsets) {
            BlockPos wallPos = blockPos.offset(offset);
            Block block = HoleUtils.mc.level.getBlockState(wallPos).getBlock();
            boolean isBedrock = block.equals(Blocks.BEDROCK);
            boolean isBlastResistant = block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.RESPAWN_ANCHOR) || block.equals(Blocks.ENDER_CHEST);
            if (!isBedrock && !isBlastResistant) {
                allValid = false;
                if (block instanceof ShulkerBoxBlock || !HoleUtils.mc.level.getBlockState(wallPos).getCollisionShape((BlockGetter)HoleUtils.mc.level, wallPos).isEmpty()) continue;
                return null;
            }
            if (isBedrock) continue;
            allBedrock = false;
        }
        if (!allValid) {
            return HoleSafety.NONE;
        }
        return allBedrock ? HoleSafety.SAFE : HoleSafety.UNSAFE;
    }

    public static Hole getSingleHole(BlockPos blockPos, double height) {
        return HoleUtils.getSingleHole(blockPos, height, true);
    }

    public static Hole getSingleHole(BlockPos blockPos, double height, boolean checkHeadroom) {
        if (!HoleUtils.mc.level.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above()).getBlock().equals(Blocks.AIR) && checkHeadroom) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above().above()).getBlock().equals(Blocks.AIR) && checkHeadroom) {
            return null;
        }
        HoleSafety holeSafety = HoleUtils.classifyWalls(blockPos, singleOffsets);
        if (holeSafety == null) {
            return null;
        }
        return new Hole(new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 1), (double)blockPos.getY() + height, (double)(blockPos.getZ() + 1)), HoleType.SINGLE, holeSafety);
    }

    public static Hole getDoubleHole(BlockPos blockPos, double height) {
        HoleSafety holeSafety;
        if (!HoleUtils.mc.level.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        boolean openX = HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR);
        boolean openZ = HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR);
        if (!openX && !openZ) {
            return null;
        }
        AABB aABB = null;
        HoleSafety holeSafety2 = null;
        if (openX && (holeSafety = HoleUtils.classifyWalls(blockPos, doubleXOffsets)) != null) {
            aABB = new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 2), (double)blockPos.getY() + height, (double)(blockPos.getZ() + 1));
            holeSafety2 = holeSafety;
        }
        if (openZ && aABB == null && (holeSafety = HoleUtils.classifyWalls(blockPos, doubleZOffsets)) != null) {
            aABB = new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 1), (double)blockPos.getY() + height, (double)(blockPos.getZ() + 2));
            holeSafety2 = holeSafety;
        }
        if (aABB == null) {
            return null;
        }
        return new Hole(aABB, HoleType.DOUBLE, holeSafety2);
    }

    public static Hole getQuadHole(BlockPos blockPos, double height) {
        if (!HoleUtils.mc.level.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 1))).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 1)).above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 1)).above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        HoleSafety holeSafety = HoleUtils.classifyWalls(blockPos, quadOffsets);
        if (holeSafety == null) {
            return null;
        }
        return new Hole(new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 2), (double)blockPos.getY() + height, (double)(blockPos.getZ() + 2)), HoleType.QUAD, holeSafety);
    }

    public static Hole getPartialHole(BlockPos blockPos, double height) {
        if (HoleUtils.mc.level == null) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above().above()).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (HoleUtils.getSingleHole(blockPos, height) != null) {
            return null;
        }
        if (HoleUtils.getDoubleHole(blockPos, height) != null) {
            return null;
        }
        if (HoleUtils.getQuadHole(blockPos, height) != null) {
            return null;
        }
        if (HoleUtils.mc.level.getBlockState(blockPos.below()).canBeReplaced()) {
            return null;
        }
        int solidWalls = 0;
        BlockPos openSidePos = null;
        for (Direction direction : Direction.values()) {
            if (!direction.getAxis().isHorizontal()) continue;
            BlockPos neighborPos = blockPos.relative(direction);
            if (!HoleUtils.mc.level.getBlockState(neighborPos).canBeReplaced() && !HoleUtils.mc.level.getBlockState(neighborPos).getCollisionShape((BlockGetter)HoleUtils.mc.level, neighborPos).isEmpty()) {
                ++solidWalls;
                continue;
            }
            openSidePos = neighborPos;
        }
        if (solidWalls != 3) {
            return null;
        }
        if (openSidePos == null || HoleUtils.mc.level.getBlockState(openSidePos.below()).canBeReplaced()) {
            return null;
        }
        return new Hole(new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 1), (double)blockPos.getY() + height, (double)(blockPos.getZ() + 1)), HoleType.PARTIAL, HoleSafety.PARTIAL);
    }

    public static enum HoleSafety {
        SAFE,
        UNSAFE,
        PARTIAL,
        NONE;

    }

    public record Hole(AABB box, HoleType type, HoleSafety safety) {
    }

    public static enum HoleType {
        SINGLE,
        DOUBLE,
        QUAD,
        PARTIAL;

    }
}

