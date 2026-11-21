/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package lightning.utils;

import java.util.ArrayList;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BlockEntityInstanceTracker {
    private static BlockEntityInstanceTracker instance;
    private final ArrayList<BlockEntity> instances = new ArrayList();

    private BlockEntityInstanceTracker() {
    }

    public static void initialize() {
        instance = new BlockEntityInstanceTracker();
    }

    public static void constructed(BlockEntity blockEntity) {
        if (instance != null) {
            BlockEntityInstanceTracker.instance.instances.add(blockEntity);
        }
    }

    public static void removed(BlockEntity blockEntity) {
        if (instance != null) {
            BlockEntityInstanceTracker.instance.instances.remove(blockEntity);
        }
    }

    public static void localPlayerConstructed() {
        if (instance != null) {
            BlockEntityInstanceTracker.instance.instances.clear();
        }
    }

    public static ArrayList<BlockEntity> snapshot() {
        return new ArrayList<BlockEntity>(BlockEntityInstanceTracker.instance.instances);
    }
}

