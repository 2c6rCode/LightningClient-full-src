/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CrystalPlacementHelper
implements IMinecraft {
    public static PlacementResult getVisiblePlacement(BlockPos blockPos) {
        if (CrystalPlacementHelper.mc.player == null || CrystalPlacementHelper.mc.level == null) {
            return new PlacementResult(Direction.UP, Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.5, 0.0), false);
        }
        Vec3 eyePos = CrystalPlacementHelper.mc.player.getEyePosition();
        Direction[] directions = new Direction[]{Direction.UP, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN};
        PlacementResult bestResult = null;
        double bestDistSq = Double.MAX_VALUE;
        double[][] cornerOffsets = new double[][]{{0.0, 0.5}, {1.0, 0.5}, {0.5, 0.0}, {0.5, 1.0}, {0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}};
        int maxSubdivisions = 10;
        for (Direction direction : directions) {
            BlockPos adjacentPos = blockPos.relative(direction);
            if (direction != Direction.UP && !CrystalPlacementHelper.mc.level.getBlockState(adjacentPos).canBeReplaced() && CrystalPlacementHelper.mc.level.getBlockState(adjacentPos).isSolidRender()) continue;
            PlacementResult centerResult = CrystalPlacementHelper.testFacePoint(blockPos, direction, eyePos, 0.5, 0.5);
            if (centerResult != null) {
                double distSq = eyePos.distanceToSqr(centerResult.hitVec);
                if (!(distSq < bestDistSq)) continue;
                bestDistSq = distSq;
                bestResult = centerResult;
                continue;
            }
            block1: for (double[] corner : cornerOffsets) {
                for (int i = 1; i <= maxSubdivisions; ++i) {
                    double factor = 1.0 - 1.0 / (double)(1 << i);
                    double offsetU = 0.5 + (corner[0] - 0.5) * factor;
                    double offsetV = 0.5 + (corner[1] - 0.5) * factor;
                    PlacementResult sampleResult = CrystalPlacementHelper.testFacePoint(blockPos, direction, eyePos, offsetU, offsetV);
                    if (sampleResult == null) continue;
                    double sampleDistSq = eyePos.distanceToSqr(sampleResult.hitVec);
                    if (!(sampleDistSq < bestDistSq)) continue block1;
                    bestDistSq = sampleDistSq;
                    bestResult = sampleResult;
                    continue block1;
                }
            }
        }
        if (bestResult != null) {
            return bestResult;
        }
        Direction fallbackDir = null;
        double fallbackDistSq = Double.MAX_VALUE;
        for (Direction dir : directions) {
            Vec3 surfacePoint;
            double distSq;
            BlockPos neighborPos = blockPos.relative(dir);
            BlockState blockState = CrystalPlacementHelper.mc.level.getBlockState(neighborPos);
            if (!blockState.canBeReplaced() || !((distSq = eyePos.distanceToSqr(surfacePoint = CrystalPlacementHelper.getSurfacePoint(blockPos, dir, 0.5, 0.5))) < fallbackDistSq)) continue;
            fallbackDistSq = distSq;
            fallbackDir = dir;
        }
        if (fallbackDir != null) {
            return new PlacementResult(fallbackDir, CrystalPlacementHelper.getSurfacePoint(blockPos, fallbackDir, 0.5, 0.5), false);
        }
        return new PlacementResult(Direction.UP, Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.5, 0.0), false);
    }

    private static PlacementResult testFacePoint(BlockPos blockPos, Direction direction, Vec3 eyePos, double u, double v) {
        Vec3 targetPoint = CrystalPlacementHelper.getPointOnFace(blockPos, direction, u, v);
        BlockHitResult blockHitResult = CrystalPlacementHelper.mc.level.clip(new ClipContext(eyePos, targetPoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)CrystalPlacementHelper.mc.player));
        if (blockHitResult == null) {
            return null;
        }
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return new PlacementResult(direction, CrystalPlacementHelper.getSurfacePoint(blockPos, direction, u, v), true);
        }
        if (blockHitResult.getType() == HitResult.Type.BLOCK && blockHitResult.getBlockPos().equals((Object)blockPos)) {
            return new PlacementResult(blockHitResult.getDirection(), blockHitResult.getLocation(), true);
        }
        return null;
    }

    public static boolean isPlacementVisible(BlockPos blockPos) {
        if (CrystalPlacementHelper.mc.player == null || CrystalPlacementHelper.mc.level == null) {
            return false;
        }
        PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(blockPos);
        return placementResult != null && placementResult.visible;
    }

    public static Vec3 getPointOnFace(BlockPos blockPos, Direction direction, double u, double v) {
        double baseX = blockPos.getX();
        double baseY = blockPos.getY();
        double baseZ = blockPos.getZ();
        switch (direction) {
            case UP: {
                return new Vec3(baseX + u, baseY + 0.999, baseZ + v);
            }
            case DOWN: {
                return new Vec3(baseX + u, baseY + 0.001, baseZ + v);
            }
            case SOUTH: {
                return new Vec3(baseX + u, baseY + v, baseZ + 0.999);
            }
            case NORTH: {
                return new Vec3(baseX + u, baseY + v, baseZ + 0.001);
            }
            case EAST: {
                return new Vec3(baseX + 0.999, baseY + u, baseZ + v);
            }
            case WEST: {
                return new Vec3(baseX + 0.001, baseY + u, baseZ + v);
            }
        }
        return new Vec3(baseX + 0.5, baseY + 0.5, baseZ + 0.5);
    }

    public static Vec3 getSurfacePoint(BlockPos blockPos, Direction direction, double u, double v) {
        double baseX = blockPos.getX();
        double baseY = blockPos.getY();
        double baseZ = blockPos.getZ();
        switch (direction) {
            case UP: {
                return new Vec3(baseX + u, baseY + 1.0, baseZ + v);
            }
            case DOWN: {
                return new Vec3(baseX + u, baseY + 0.0, baseZ + v);
            }
            case SOUTH: {
                return new Vec3(baseX + u, baseY + v, baseZ + 1.0);
            }
            case NORTH: {
                return new Vec3(baseX + u, baseY + v, baseZ + 0.0);
            }
            case EAST: {
                return new Vec3(baseX + 1.0, baseY + u, baseZ + v);
            }
            case WEST: {
                return new Vec3(baseX + 0.0, baseY + u, baseZ + v);
            }
        }
        return new Vec3(baseX + 0.5, baseY + 0.5, baseZ + 0.5);
    }

    public static class PlacementResult {
        public Direction direction;
        public Vec3 hitVec;
        public boolean visible;

        public PlacementResult(Direction direction, Vec3 hitVec) {
            this(direction, hitVec, true);
        }

        public PlacementResult(Direction direction, Vec3 hitVec, boolean visible) {
            this.direction = direction;
            this.hitVec = hitVec;
            this.visible = visible;
        }
    }
}

