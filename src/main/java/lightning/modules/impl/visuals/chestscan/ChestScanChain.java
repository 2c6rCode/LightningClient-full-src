/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.HopperBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package lightning.modules.impl.visuals.chestscan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lightning.modules.impl.visuals.chestscan.ChestScanStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class ChestScanChain {
    private ChestScanChain() {
    }

    public static Map<BlockPos, BlockPos> findEdges(Level level, Set<BlockPos> seeds, BlockPos playerPos, int radius) {
        HashMap<BlockPos, BlockPos> edges = new HashMap<BlockPos, BlockPos>();
        double radiusSq = (double)radius * (double)radius;
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>(seeds);
        HashSet<BlockPos> visited = new HashSet<BlockPos>(seeds);
        while (!queue.isEmpty()) {
            Direction hopperFacing;
            BlockPos targetChest;
            BlockPos currentPos = queue.poll();
            if (currentPos.distSqr((Vec3i)playerPos) > radiusSq) continue;
            BlockPos belowPos = currentPos.below();
            BlockState blockState = level.getBlockState(belowPos);
            if (blockState.getBlock() instanceof HopperBlock && ChestScanChain.isChest(level.getBlockState(targetChest = belowPos.relative(hopperFacing = (Direction)blockState.getValue((Property)HopperBlock.FACING))))) {
                edges.put(currentPos, targetChest);
                if (visited.add(targetChest)) {
                    queue.add(targetChest);
                }
            }
            for (Direction dir : Direction.values()) {
                BlockPos sourceChest;
                BlockPos sidePos = currentPos.relative(dir);
                BlockState sideState = level.getBlockState(sidePos);
                if (!(sideState.getBlock() instanceof HopperBlock) || sideState.getValue((Property)HopperBlock.FACING) != dir.getOpposite() || !ChestScanChain.isChest(level.getBlockState(sourceChest = sidePos.above()))) continue;
                edges.put(sourceChest, currentPos);
                if (!visited.add(sourceChest)) continue;
                queue.add(sourceChest);
            }
        }
        return edges;
    }

    private static boolean isChest(BlockState blockState) {
        return blockState.getBlock() instanceof ChestBlock;
    }

    public static Set<BlockPos> inferEmpty(Map<BlockPos, BlockPos> edges, Map<BlockPos, ChestScanStore.ChestStatus> knownStatuses) {
        Map<BlockPos, List<BlockPos>> reverseEdges = new HashMap<>();
        for (Map.Entry<BlockPos, BlockPos> entry : edges.entrySet()) {
            reverseEdges.computeIfAbsent(entry.getValue(), blockPos -> new ArrayList<>()).add(entry.getKey());
        }
        Set<BlockPos> inferredEmpty = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        for (Map.Entry<BlockPos, ChestScanStore.ChestStatus> entry : knownStatuses.entrySet()) {
            BlockPos startPos;
            if (entry.getValue() == ChestScanStore.ChestStatus.FULL || !visited.add(startPos = entry.getKey())) continue;
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            queue.add(startPos);
            while (!queue.isEmpty()) {
                BlockPos currentPos = queue.poll();
                for (BlockPos upstreamPos : reverseEdges.getOrDefault(currentPos, List.of())) {
                    if (!visited.add(upstreamPos)) continue;
                    if (!knownStatuses.containsKey(upstreamPos)) {
                        inferredEmpty.add(upstreamPos);
                    }
                    queue.add(upstreamPos);
                }
            }
        }
        return inferredEmpty;
    }
}

