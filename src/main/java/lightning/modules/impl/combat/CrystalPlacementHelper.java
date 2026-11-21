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
        Vec3 vec3 = CrystalPlacementHelper.mc.player.getEyePosition();
        Direction[] directions = new Direction[]{Direction.UP, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN};
        PlacementResult placementResult = null;
        double d = Double.MAX_VALUE;
        double[][] dArrayArray = new double[][]{{0.0, 0.5}, {1.0, 0.5}, {0.5, 0.0}, {0.5, 1.0}, {0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}};
        int n = 10;
        for (Direction direction2 : directions) {
            BlockPos blockPos2 = blockPos.relative(direction2);
            if (direction2 != Direction.UP && !CrystalPlacementHelper.mc.level.getBlockState(blockPos2).canBeReplaced() && CrystalPlacementHelper.mc.level.getBlockState(blockPos2).isSolidRender()) continue;
            PlacementResult placementResult2 = CrystalPlacementHelper.testFacePoint(blockPos, direction2, vec3, 0.5, 0.5);
            if (placementResult2 != null) {
                double d2 = vec3.distanceToSqr(placementResult2.hitVec);
                if (!(d2 < d)) continue;
                d = d2;
                placementResult = placementResult2;
                continue;
            }
            block1: for (double[] dArray : dArrayArray) {
                for (int i = 1; i <= n; ++i) {
                    double d3 = 1.0 - 1.0 / (double)(1 << i);
                    double d4 = 0.5 + (dArray[0] - 0.5) * d3;
                    double d5 = 0.5 + (dArray[1] - 0.5) * d3;
                    PlacementResult placementResult3 = CrystalPlacementHelper.testFacePoint(blockPos, direction2, vec3, d4, d5);
                    if (placementResult3 == null) continue;
                    double d6 = vec3.distanceToSqr(placementResult3.hitVec);
                    if (!(d6 < d)) continue block1;
                    d = d6;
                    placementResult = placementResult3;
                    continue block1;
                }
            }
        }
        if (placementResult != null) {
            return placementResult;
        }
        Direction direction3 = null;
        double d7 = Double.MAX_VALUE;
        for (Direction direction4 : directions) {
            Vec3 vec32;
            double d8;
            BlockPos blockPos3 = blockPos.relative(direction4);
            BlockState blockState = CrystalPlacementHelper.mc.level.getBlockState(blockPos3);
            if (!blockState.canBeReplaced() || !((d8 = vec3.distanceToSqr(vec32 = CrystalPlacementHelper.getSurfacePoint(blockPos, direction4, 0.5, 0.5))) < d7)) continue;
            d7 = d8;
            direction3 = direction4;
        }
        if (direction3 != null) {
            return new PlacementResult(direction3, CrystalPlacementHelper.getSurfacePoint(blockPos, direction3, 0.5, 0.5), false);
        }
        return new PlacementResult(Direction.UP, Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.5, 0.0), false);
    }

    private static PlacementResult testFacePoint(BlockPos blockPos, Direction direction, Vec3 vec3, double d, double d2) {
        Vec3 vec32 = CrystalPlacementHelper.getPointOnFace(blockPos, direction, d, d2);
        BlockHitResult blockHitResult = CrystalPlacementHelper.mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)CrystalPlacementHelper.mc.player));
        if (blockHitResult == null) {
            return null;
        }
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return new PlacementResult(direction, CrystalPlacementHelper.getSurfacePoint(blockPos, direction, d, d2), true);
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

    public static Vec3 getPointOnFace(BlockPos blockPos, Direction direction, double d, double d2) {
        double d3 = blockPos.getX();
        double d4 = blockPos.getY();
        double d5 = blockPos.getZ();
        switch (direction) {
            case UP: {
                return new Vec3(d3 + d, d4 + 0.999, d5 + d2);
            }
            case DOWN: {
                return new Vec3(d3 + d, d4 + 0.001, d5 + d2);
            }
            case SOUTH: {
                return new Vec3(d3 + d, d4 + d2, d5 + 0.999);
            }
            case NORTH: {
                return new Vec3(d3 + d, d4 + d2, d5 + 0.001);
            }
            case EAST: {
                return new Vec3(d3 + 0.999, d4 + d, d5 + d2);
            }
            case WEST: {
                return new Vec3(d3 + 0.001, d4 + d, d5 + d2);
            }
        }
        return new Vec3(d3 + 0.5, d4 + 0.5, d5 + 0.5);
    }

    public static Vec3 getSurfacePoint(BlockPos blockPos, Direction direction, double d, double d2) {
        double d3 = blockPos.getX();
        double d4 = blockPos.getY();
        double d5 = blockPos.getZ();
        switch (direction) {
            case UP: {
                return new Vec3(d3 + d, d4 + 1.0, d5 + d2);
            }
            case DOWN: {
                return new Vec3(d3 + d, d4 + 0.0, d5 + d2);
            }
            case SOUTH: {
                return new Vec3(d3 + d, d4 + d2, d5 + 1.0);
            }
            case NORTH: {
                return new Vec3(d3 + d, d4 + d2, d5 + 0.0);
            }
            case EAST: {
                return new Vec3(d3 + 1.0, d4 + d, d5 + d2);
            }
            case WEST: {
                return new Vec3(d3 + 0.0, d4 + d, d5 + d2);
            }
        }
        return new Vec3(d3 + 0.5, d4 + 0.5, d5 + 0.5);
    }

    public static class PlacementResult {
        public Direction direction;
        public Vec3 hitVec;
        public boolean visible;

        public PlacementResult(Direction direction, Vec3 vec3) {
            this(direction, vec3, true);
        }

        public PlacementResult(Direction direction, Vec3 vec3, boolean bl) {
            this.direction = direction;
            this.hitVec = vec3;
            this.visible = bl;
        }
    }
}

