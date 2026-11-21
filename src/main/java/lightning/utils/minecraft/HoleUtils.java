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
        boolean bl = entity.isSwimming() || entity.isVisuallySwimming() || entity.isVisuallyCrawling() || entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).isFallFlying() || entity.getPose() == Pose.SWIMMING || entity.getPose() == Pose.FALL_FLYING || aABB.maxY - aABB.minY <= 1.0;
        boolean bl2 = bl;
        if (bl) {
            float f = entity.getYRot();
            double d = Math.toRadians(f);
            double d2 = -Math.sin(d);
            double d3 = Math.cos(d);
            double d4 = entity.getX();
            double d5 = entity.getY();
            double d6 = entity.getZ();
            for (double d7 = -0.9; d7 <= 0.9; d7 += 0.2) {
                for (double d8 = -0.3; d8 <= 0.3; d8 += 0.3) {
                    double d9 = d4 + d2 * d7 - d3 * d8;
                    double d10 = d6 + d3 * d7 + d2 * d8;
                    BlockPos blockPos = new BlockPos((int)Math.floor(d9), (int)Math.floor(d5), (int)Math.floor(d10));
                    if (arrayList.contains(blockPos)) continue;
                    arrayList.add(blockPos);
                }
            }
            int n = (int)Math.floor(aABB.minX);
            int n2 = (int)Math.ceil(aABB.maxX);
            int n3 = (int)Math.floor(aABB.minY);
            int n4 = (int)Math.ceil(aABB.maxY);
            int n5 = (int)Math.floor(aABB.minZ);
            int n6 = (int)Math.ceil(aABB.maxZ);
            for (int i = n; i < n2; ++i) {
                for (int j = n3; j < n4; ++j) {
                    for (int k = n5; k < n6; ++k) {
                        BlockPos blockPos = new BlockPos(i, j, k);
                        if (arrayList.contains(blockPos)) continue;
                        arrayList.add(blockPos);
                    }
                }
            }
        } else {
            int n = (int)Math.floor(aABB.minX);
            int n7 = (int)Math.ceil(aABB.maxX);
            int n8 = (int)Math.floor(aABB.minY);
            int n9 = (int)Math.floor(aABB.minZ);
            int n10 = (int)Math.ceil(aABB.maxZ);
            int n11 = (int)Math.ceil(aABB.maxY);
            for (int i = n; i < n7; ++i) {
                for (int j = n8; j < n11; ++j) {
                    for (int k = n9; k < n10; ++k) {
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
                BlockPos blockPos2 = blockPos.below(i);
                if (HoleUtils.mc.level.getBlockState(blockPos2).canBeReplaced()) continue;
                return blockPos2.above();
            }
        }
        return blockPos;
    }

    public static HashSet<BlockPos> getFeetPositions(Player player, boolean bl, boolean bl2, boolean bl3) {
        return HoleUtils.getFeetPositions(player, bl, bl2, bl2, bl3);
    }

    public static HashSet<BlockPos> getFeetPositions(Player player, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        BlockPos blockPos2;
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        HashSet<BlockPos> hashSet2 = new HashSet<BlockPos>();
        HitboxDesyncModule hitboxDesyncModule = Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
        AABB aABB = player.getBoundingBox();
        double d = aABB.minY - Math.floor(aABB.minY);
        int n = (int)(d > 0.8 ? Math.floor(aABB.minY) + 1.0 : Math.floor(aABB.minY));
        int n2 = (int)Math.floor(aABB.minX);
        int n3 = (int)Math.floor(aABB.maxX);
        int n4 = (int)Math.floor(aABB.minZ);
        int n5 = (int)Math.floor(aABB.maxZ);
        BlockPos blockPos3 = new BlockPos((int)Math.floor(player.getX()), n, (int)Math.floor(player.getZ()));
        if (HoleUtils.mc.level != null && !player.onGround() && HoleUtils.mc.level.getBlockState(blockPos3.below()).canBeReplaced()) {
            for (int i = 2; i <= 3; ++i) {
                BlockPos blockPos4 = blockPos3.below(i);
                if (HoleUtils.mc.level.getBlockState(blockPos4).canBeReplaced()) continue;
                n = blockPos4.above().getY();
                break;
            }
        }
        BlockPos blockPos5 = PositionUtils.getFlooredPosition((Entity)player);
        hashSet2.add(new BlockPos(blockPos5.getX(), n, blockPos5.getZ()));
        for (int i = n2; i <= n3; ++i) {
            for (int j = n4; j <= n5; ++j) {
                if (!aABB.intersects(new AABB((double)i, (double)n, (double)j, (double)(i + 1), (double)(n + 1), (double)(j + 1)))) continue;
                hashSet2.add(new BlockPos(i, n, j));
            }
        }
        if (bl && HoleUtils.mc.level != null) {
            for (BlockPos blockPos6 : Set.copyOf(hashSet2)) {
                for (Direction direction : Direction.values()) {
                    List<Player> list;
                    if (direction.getAxis().isVertical() || !HoleUtils.mc.level.getBlockState(blockPos2 = blockPos6.relative(direction)).canBeReplaced() || (list = WorldUtils.getCollisions(blockPos2)).isEmpty()) continue;
                    for (Player player2 : list) {
                        if (player2 == player || player2 == HoleUtils.mc.player && player == HoleUtils.mc.player || bl4 && player2 != player || player == HoleUtils.mc.player && !Night.FRIEND_MANAGER.contains(player2.getName().getString()) || Math.abs(player2.getY() - player.getY()) > 1.0) continue;
                        AABB aABB2 = player2.getBoundingBox();
                        int n6 = (int)Math.floor(aABB2.minX + 0.001);
                        int n7 = (int)Math.floor(aABB2.maxX - 0.001);
                        int n8 = (int)Math.floor(aABB2.minZ + 0.001);
                        int n9 = (int)Math.floor(aABB2.maxZ - 0.001);
                        for (int i = n6; i <= n7; ++i) {
                            for (int j = n8; j <= n9; ++j) {
                                hashSet2.add(new BlockPos(i, n, j));
                            }
                        }
                    }
                }
            }
        }
        for (BlockPos blockPos7 : hashSet2) {
            if (bl2 && bl3) {
                hashSet.add(blockPos7.below());
            }
            for (Direction direction : Direction.values()) {
                if (!direction.getAxis().isHorizontal() || hashSet2.contains(blockPos2 = blockPos7.relative(direction))) continue;
                hashSet.add(blockPos2);
                if (!bl2 || HoleUtils.mc.level == null || !HoleUtils.mc.level.getBlockState(blockPos2).canBeReplaced()) continue;
                hashSet.add(blockPos2.below());
            }
        }
        int n10 = n;
        hashSet.removeIf(blockPos -> blockPos.getY() == n10 && player.getBoundingBox().intersects(new AABB(blockPos)));
        if (player == HoleUtils.mc.player && hitboxDesyncModule.isToggled() && hitboxDesyncModule.close.getValue()) {
            int n11;
            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
            Vec3 vec3 = Vec3.atCenterOf((Vec3i)HoleUtils.mc.player.blockPosition());
            int n12 = vec3.x - HoleUtils.mc.player.getX() > 0.0 ? 1 : 0;
            int n6 = n11 = vec3.z - HoleUtils.mc.player.getZ() > 0.0 ? 1 : 0;
            if (n12 != 0 && n11 != 0) {
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(-1, 0, 0)));
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(0, 0, -1)));
            }
            if (n12 == 0 && n11 != 0) {
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(1, 0, 0)));
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(0, 0, -1)));
            }
            if (n12 != 0 && n11 == 0) {
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(-1, 0, 0)));
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(0, 0, 1)));
            }
            if (n12 == 0 && n11 == 0) {
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(1, 0, 0)));
                arrayList.add(new BlockPos((Vec3i)HoleUtils.mc.player.blockPosition().offset(0, 0, 1)));
            }
            hashSet.removeIf(arrayList::contains);
        }
        return hashSet;
    }

    public static List<BlockPos> getTrapPositions(Player player, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
        return HoleUtils.getTrapPositions(player, bl, bl2, bl3, bl4, bl5, false, false);
    }

    public static List<BlockPos> getTrapPositions(Player player, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6) {
        return HoleUtils.getTrapPositions(player, null, bl, bl2, bl3, bl4, bl5, false, bl6);
    }

    public static List<BlockPos> getTrapPositions(Player player, BlockPos blockPos, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6) {
        return HoleUtils.getTrapPositions(player, blockPos, bl, bl2, bl3, bl4, bl5, false, bl6);
    }

    public static List<BlockPos> getTrapPositions(Player player, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7) {
        return HoleUtils.getTrapPositions(player, null, bl, bl2, bl3, bl4, bl5, bl6, bl7);
    }

    public static List<BlockPos> getTrapPositions(Player player, BlockPos blockPos, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7) {
        HashSet<BlockPos> hashSet;
        LinkedHashSet<BlockPos> linkedHashSet = new LinkedHashSet<BlockPos>();
        if (blockPos != null && !player.onGround()) {
            hashSet = new HashSet<BlockPos>();
            hashSet.add(blockPos);
        } else {
            hashSet = HoleUtils.getOccupiedFloorCells(player);
        }
        for (BlockPos blockPos2 : hashSet) {
            BlockPos blockPos3;
            Direction direction = null;
            for (Direction direction2 : Direction.values()) {
                if (!direction2.getAxis().isHorizontal() || hashSet.contains(blockPos2.relative(direction2))) continue;
                if (direction == null) {
                    direction = direction2;
                }
                if (WorldUtils.getDirection(blockPos2.relative(direction2).above(), bl5) == null) continue;
                direction = direction2;
                break;
            }
            if (direction == null) {
                direction = Direction.EAST;
            }
            if (bl3) {
                HoleUtils.addWallOffset(linkedHashSet, hashSet, blockPos2, new Vec3i(1, 2, 0));
                HoleUtils.addWallOffset(linkedHashSet, hashSet, blockPos2, new Vec3i(-1, 2, 0));
                HoleUtils.addWallOffset(linkedHashSet, hashSet, blockPos2, new Vec3i(0, 2, 1));
                HoleUtils.addWallOffset(linkedHashSet, hashSet, blockPos2, new Vec3i(0, 2, -1));
            }
            if (bl4) {
                linkedHashSet.add(blockPos2.offset(0, 3, 0));
            }
            if (bl6) {
                for (Direction direction2 : Direction.values()) {
                    if (!direction2.getAxis().isHorizontal() || hashSet.contains(blockPos2.relative(direction2))) continue;
                    linkedHashSet.add(blockPos2.relative(direction2));
                }
            }
            if (bl) {
                blockPos3 = blockPos2.offset(0, 2, 0);
                if (HoleUtils.mc.level != null && !HoleUtils.mc.level.getBlockState(blockPos3).canBeReplaced()) {
                    linkedHashSet.add(blockPos3);
                    continue;
                }
                if (!bl7 && WorldUtils.getDirection(blockPos3, bl5) == null) {
                    linkedHashSet.add(blockPos2.relative(direction).above());
                    linkedHashSet.add(blockPos2.relative(direction).above(2));
                    linkedHashSet.add(blockPos3);
                    continue;
                }
                linkedHashSet.add(blockPos3);
                continue;
            }
            for (Direction direction2 : Direction.values()) {
                if (!direction2.getAxis().isHorizontal() || hashSet.contains(blockPos2.relative(direction2))) continue;
                linkedHashSet.add(blockPos2.relative(direction2).above());
            }
            if (!bl2) continue;
            blockPos3 = blockPos2.offset(0, 2, 0);
            if (!bl7 && (HoleUtils.mc.level == null || HoleUtils.mc.level.getBlockState(blockPos3).canBeReplaced())) {
                linkedHashSet.add(blockPos2.relative(direction).above(2));
            }
            linkedHashSet.add(blockPos3);
        }
        return new ArrayList<BlockPos>(linkedHashSet);
    }

    private static void addWallOffset(Set<BlockPos> set, HashSet<BlockPos> hashSet, BlockPos blockPos, Vec3i vec3i) {
        BlockPos blockPos2;
        if ((vec3i.getX() != 0 || vec3i.getZ() != 0) && hashSet.contains(blockPos2 = blockPos.offset(vec3i.getX(), 0, vec3i.getZ()))) {
            return;
        }
        set.add(blockPos.offset(vec3i));
    }

    private static HashSet<BlockPos> getOccupiedFloorCells(Player player) {
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        BlockPos blockPos = PositionUtils.getFlooredPosition((Entity)player);
        hashSet.add(blockPos);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isVertical()) continue;
            AABB aABB = player.getBoundingBox();
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!(aABB.minX < (double)(blockPos2.getX() + 1) && aABB.maxX > (double)blockPos2.getX() && aABB.minZ < (double)(blockPos2.getZ() + 1) && aABB.maxZ > (double)blockPos2.getZ())) continue;
            int n = (int)Math.floor(aABB.minX);
            while ((double)n < Math.ceil(aABB.maxX)) {
                int n2 = (int)Math.floor(aABB.minZ);
                while ((double)n2 < Math.ceil(aABB.maxZ)) {
                    hashSet.add(new BlockPos(n, blockPos.getY(), n2));
                    ++n2;
                }
                ++n;
            }
        }
        return hashSet;
    }

    private static HoleSafety classifyWalls(BlockPos blockPos, Vec3i[] vec3iArray) {
        boolean bl = true;
        boolean bl2 = true;
        for (Vec3i vec3i : vec3iArray) {
            BlockPos blockPos2 = blockPos.offset(vec3i);
            Block block = HoleUtils.mc.level.getBlockState(blockPos2).getBlock();
            boolean bl3 = block.equals(Blocks.BEDROCK);
            boolean bl4 = block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.RESPAWN_ANCHOR) || block.equals(Blocks.ENDER_CHEST);
            boolean bl5 = bl4;
            if (!bl3 && !bl4) {
                bl2 = false;
                if (block instanceof ShulkerBoxBlock || !HoleUtils.mc.level.getBlockState(blockPos2).getCollisionShape((BlockGetter)HoleUtils.mc.level, blockPos2).isEmpty()) continue;
                return null;
            }
            if (bl3) continue;
            bl = false;
        }
        if (!bl2) {
            return HoleSafety.NONE;
        }
        return bl ? HoleSafety.SAFE : HoleSafety.UNSAFE;
    }

    public static Hole getSingleHole(BlockPos blockPos, double d) {
        return HoleUtils.getSingleHole(blockPos, d, true);
    }

    public static Hole getSingleHole(BlockPos blockPos, double d, boolean bl) {
        if (!HoleUtils.mc.level.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above()).getBlock().equals(Blocks.AIR) && bl) {
            return null;
        }
        if (!HoleUtils.mc.level.getBlockState(blockPos.above().above()).getBlock().equals(Blocks.AIR) && bl) {
            return null;
        }
        HoleSafety holeSafety = HoleUtils.classifyWalls(blockPos, singleOffsets);
        if (holeSafety == null) {
            return null;
        }
        return new Hole(new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 1), (double)blockPos.getY() + d, (double)(blockPos.getZ() + 1)), HoleType.SINGLE, holeSafety);
    }

    public static Hole getDoubleHole(BlockPos blockPos, double d) {
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
        boolean bl = HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR);
        boolean bl2 = HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR) && HoleUtils.mc.level.getBlockState(blockPos.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR);
        boolean bl3 = bl2;
        if (!bl && !bl2) {
            return null;
        }
        AABB aABB = null;
        HoleSafety holeSafety2 = null;
        if (bl && (holeSafety = HoleUtils.classifyWalls(blockPos, doubleXOffsets)) != null) {
            aABB = new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 2), (double)blockPos.getY() + d, (double)(blockPos.getZ() + 1));
            holeSafety2 = holeSafety;
        }
        if (bl2 && aABB == null && (holeSafety = HoleUtils.classifyWalls(blockPos, doubleZOffsets)) != null) {
            aABB = new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 1), (double)blockPos.getY() + d, (double)(blockPos.getZ() + 2));
            holeSafety2 = holeSafety;
        }
        if (aABB == null) {
            return null;
        }
        return new Hole(aABB, HoleType.DOUBLE, holeSafety2);
    }

    public static Hole getQuadHole(BlockPos blockPos, double d) {
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
        return new Hole(new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 2), (double)blockPos.getY() + d, (double)(blockPos.getZ() + 2)), HoleType.QUAD, holeSafety);
    }

    public static Hole getPartialHole(BlockPos blockPos, double d) {
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
        if (HoleUtils.getSingleHole(blockPos, d) != null) {
            return null;
        }
        if (HoleUtils.getDoubleHole(blockPos, d) != null) {
            return null;
        }
        if (HoleUtils.getQuadHole(blockPos, d) != null) {
            return null;
        }
        if (HoleUtils.mc.level.getBlockState(blockPos.below()).canBeReplaced()) {
            return null;
        }
        int n = 0;
        BlockPos blockPos2 = null;
        for (Direction direction : Direction.values()) {
            if (!direction.getAxis().isHorizontal()) continue;
            BlockPos blockPos3 = blockPos.relative(direction);
            if (!HoleUtils.mc.level.getBlockState(blockPos3).canBeReplaced() && !HoleUtils.mc.level.getBlockState(blockPos3).getCollisionShape((BlockGetter)HoleUtils.mc.level, blockPos3).isEmpty()) {
                ++n;
                continue;
            }
            blockPos2 = blockPos3;
        }
        if (n != 3) {
            return null;
        }
        if (blockPos2 == null || HoleUtils.mc.level.getBlockState(blockPos2.below()).canBeReplaced()) {
            return null;
        }
        return new Hole(new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)(blockPos.getX() + 1), (double)blockPos.getY() + d, (double)(blockPos.getZ() + 1)), HoleType.PARTIAL, HoleSafety.PARTIAL);
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

