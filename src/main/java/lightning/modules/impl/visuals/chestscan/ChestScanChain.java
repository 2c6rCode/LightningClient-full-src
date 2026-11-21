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

    public static Map<BlockPos, BlockPos> findEdges(Level level, Set<BlockPos> set, BlockPos blockPos, int n) {
        HashMap<BlockPos, BlockPos> hashMap = new HashMap<BlockPos, BlockPos>();
        double d = (double)n * (double)n;
        ArrayDeque<BlockPos> arrayDeque = new ArrayDeque<BlockPos>(set);
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>(set);
        while (!arrayDeque.isEmpty()) {
            Direction direction;
            BlockPos blockPos2;
            BlockPos blockPos3 = arrayDeque.poll();
            if (blockPos3.distSqr((Vec3i)blockPos) > d) continue;
            BlockPos blockPos4 = blockPos3.below();
            BlockState blockState = level.getBlockState(blockPos4);
            if (blockState.getBlock() instanceof HopperBlock && ChestScanChain.isChest(level.getBlockState(blockPos2 = blockPos4.relative(direction = (Direction)blockState.getValue((Property)HopperBlock.FACING))))) {
                hashMap.put(blockPos3, blockPos2);
                if (hashSet.add(blockPos2)) {
                    arrayDeque.add(blockPos2);
                }
            }
            for (Direction direction2 : Direction.values()) {
                BlockPos blockPos5;
                BlockPos blockPos6 = blockPos3.relative(direction2);
                BlockState blockState2 = level.getBlockState(blockPos6);
                if (!(blockState2.getBlock() instanceof HopperBlock) || blockState2.getValue((Property)HopperBlock.FACING) != direction2.getOpposite() || !ChestScanChain.isChest(level.getBlockState(blockPos5 = blockPos6.above()))) continue;
                hashMap.put(blockPos5, blockPos3);
                if (!hashSet.add(blockPos5)) continue;
                arrayDeque.add(blockPos5);
            }
        }
        return hashMap;
    }

    private static boolean isChest(BlockState blockState) {
        return blockState.getBlock() instanceof ChestBlock;
    }

    public static Set<BlockPos> inferEmpty(Map<BlockPos, BlockPos> map, Map<BlockPos, ChestScanStore.ChestStatus> map2) {
        Map<BlockPos, List<BlockPos>> hashMap = new HashMap<>();
        for (Map.Entry<BlockPos, BlockPos> entry : map.entrySet()) {
            hashMap.computeIfAbsent(entry.getValue(), blockPos -> new ArrayList<>()).add(entry.getKey());
        }
        Set<BlockPos> hashSet = new HashSet<>();
        Set<BlockPos> hashSet2 = new HashSet<>();
        for (Map.Entry<BlockPos, ChestScanStore.ChestStatus> entry : map2.entrySet()) {
            BlockPos blockPos2;
            if (entry.getValue() == ChestScanStore.ChestStatus.FULL || !hashSet2.add(blockPos2 = entry.getKey())) continue;
            ArrayDeque<BlockPos> arrayDeque = new ArrayDeque<>();
            arrayDeque.add(blockPos2);
            while (!arrayDeque.isEmpty()) {
                BlockPos blockPos3 = arrayDeque.poll();
                for (BlockPos blockPos4 : hashMap.getOrDefault(blockPos3, List.of())) {
                    if (!hashSet2.add(blockPos4)) continue;
                    if (!map2.containsKey(blockPos4)) {
                        hashSet.add(blockPos4);
                    }
                    arrayDeque.add(blockPos4);
                }
            }
        }
        return hashSet;
    }
}

