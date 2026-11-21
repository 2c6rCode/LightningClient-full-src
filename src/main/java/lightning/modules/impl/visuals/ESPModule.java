/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package lightning.modules.impl.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@RegisterModule(name="ESP", description="Renders a box ESP around entities that you have selected.", category=Module.Category.VISUALS)
public class ESPModule
extends Module {
    public BooleanSetting players = new BooleanSetting("Players", "Renders the box ESP on player entities.", true);
    public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Renders the box ESP on hostile entities.", true);
    public BooleanSetting animals = new BooleanSetting("Animals", "Renders the box ESP on animal entities.", true);
    public BooleanSetting ambient = new BooleanSetting("Ambient", "Renders the box ESP on ambient entities.", false);
    public BooleanSetting invisibles = new BooleanSetting("Invisibles", "Renders the box ESP on invisible entities.", true);
    public BooleanSetting items = new BooleanSetting("Items", "Renders the box ESP on item entities.", true);
    public BooleanSetting others = new BooleanSetting("Others", "Renders the box ESP on miscellaneous entities.", false);
    public BooleanSetting blocks = new BooleanSetting("Block", "Renders a box ESP on whitelisted blocks (portals, ores, ...).", false);
    public WhitelistSetting blockWhitelist = new WhitelistSetting("Whitelist", "Blocks to highlight.", new BooleanSetting.Visibility(this.blocks, true), WhitelistSetting.Type.BLOCKS);
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the target entities.", "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private List<Entity> targetEntities = new ArrayList<Entity>();
    private final Map<Long, List<BlockPos>> matchedBlocks = new HashMap<Long, List<BlockPos>>();
    private final Map<Long, Integer> pendingChunks = new HashMap<Long, Integer>();
    private static final int SCAN_DELAY_TICKS = 5;
    private static final int MAX_CHUNK_EVALS_PER_TICK = 2;
    private static boolean chunkListenerRegistered = false;
    private Predicate<BlockState> blockPredicate = blockState -> false;
    private String lastWhitelistSignature = "";
    private boolean lastBlocksEnabled = false;

    public ESPModule() {
        this.registerChunkListener();
    }

    private void registerChunkListener() {
        if (chunkListenerRegistered) {
            return;
        }
        chunkListenerRegistered = true;
        try {
            Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents");
            FabricChunkEventsHelper.register();
        }
        catch (Throwable throwable) {
            Night.LOGGER.warn("Fabric Chunk Events API not available or optional: {}", (Object)throwable.getMessage());
        }
    }

    private static long chunkKey(int n, int n2) {
        return (long)n & 0xFFFFFFFFL | (long)n2 << 32;
    }

    private void queueChunk(int n, int n2) {
        this.pendingChunks.putIfAbsent(ESPModule.chunkKey(n, n2), 5);
    }

    @Override
    public void onEnable() {
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        this.lastBlocksEnabled = this.blocks.getValue();
        if (!this.blocks.getValue()) {
            return;
        }
        this.rebuildPredicate();
    }

    @Override
    public void onDisable() {
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        this.lastBlocksEnabled = false;
    }

    private String whitelistSignature() {
        return String.join((CharSequence)",", this.blockWhitelist.getWhitelistIds());
    }

    private void rebuildPredicate() {
        this.lastWhitelistSignature = this.whitelistSignature();
        this.blockPredicate = blockState -> !blockState.isAir() && this.blockWhitelist.isWhitelistContains(blockState.getBlock());
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        if (ESPModule.mc.player == null || ESPModule.mc.level == null) {
            return;
        }
        int n = ESPModule.mc.player.blockPosition().getX() >> 4;
        int n2 = ESPModule.mc.player.blockPosition().getZ() >> 4;
        int n3 = ESPModule.mc.options.getEffectiveRenderDistance();
        for (int i = -n3; i <= n3; ++i) {
            for (int j = -n3; j <= n3; ++j) {
                int n4 = n + i;
                int n5 = n2 + j;
                if (!ESPModule.mc.level.hasChunk(n4, n5)) continue;
                this.queueChunk(n4, n5);
            }
        }
    }

    @SubscribeEvent
    public void onBlockScanTick(TickEvent tickEvent) {
        if (ESPModule.mc.player == null || ESPModule.mc.level == null) {
            return;
        }
        if (!this.blocks.getValue()) {
            if (this.lastBlocksEnabled) {
                this.lastBlocksEnabled = false;
                this.matchedBlocks.clear();
                this.pendingChunks.clear();
            }
            return;
        }
        if (!this.lastBlocksEnabled) {
            this.lastBlocksEnabled = true;
            this.rebuildPredicate();
            return;
        }
        if (!this.whitelistSignature().equals(this.lastWhitelistSignature)) {
            this.rebuildPredicate();
            return;
        }
        if (this.pendingChunks.isEmpty()) {
            return;
        }
        int n = 2;
        Iterator<Map.Entry<Long, Integer>> iterator = this.pendingChunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Integer> entry = iterator.next();
            int n2 = entry.getValue() - 1;
            if (n2 > 0) {
                entry.setValue(n2);
                continue;
            }
            if (n == 0) {
                entry.setValue(1);
                continue;
            }
            --n;
            iterator.remove();
            long l = entry.getKey();
            int n3 = (int)l;
            int n4 = (int)(l >>> 32);
            this.evaluateChunk(n3, n4, l);
        }
    }

    private void evaluateChunk(int n, int n2, long l) {
        if (ESPModule.mc.level == null || !ESPModule.mc.level.hasChunk(n, n2)) {
            return;
        }
        LevelChunk levelChunk = ESPModule.mc.level.getChunk(n, n2);
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        LevelChunkSection[] levelChunkSectionArray = levelChunk.getSections();
        int n3 = ESPModule.mc.level.getMinSectionY();
        for (int i = 0; i < levelChunkSectionArray.length; ++i) {
            LevelChunkSection levelChunkSection = levelChunkSectionArray[i];
            if (levelChunkSection == null || levelChunkSection.hasOnlyAir() || !levelChunkSection.maybeHas(this.blockPredicate)) continue;
            int n4 = n3 + i << 4;
            int n5 = n << 4;
            int n6 = n2 << 4;
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int i2 = 0; i2 < 16; ++i2) {
                        BlockState blockState = levelChunkSection.getBlockState(j, k, i2);
                        if (!this.blockPredicate.test(blockState)) continue;
                        arrayList.add(new BlockPos(n5 + j, n4 + k, n6 + i2));
                    }
                }
            }
        }
        if (arrayList.isEmpty()) {
            this.matchedBlocks.remove(l);
        } else {
            this.matchedBlocks.put(l, arrayList);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (ESPModule.mc.level == null) {
            return;
        }
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        for (Entity entity : ESPModule.mc.level.entitiesForRendering()) {
            if (entity == ESPModule.mc.player || !this.isValidEntity(entity)) continue;
            arrayList.add(entity);
        }
        this.targetEntities = arrayList;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (ESPModule.mc.level == null) {
            return;
        }
        if (!this.targetEntities.isEmpty()) {
            for (Entity entity : this.targetEntities) {
                Vec3 vec3 = EntityUtils.getRenderPos(entity, renderWorldEvent.getTickDelta());
                AABB aABB = new AABB(vec3.x - entity.getBoundingBox().getXsize() / 2.0, vec3.y, vec3.z - entity.getBoundingBox().getZsize() / 2.0, vec3.x + entity.getBoundingBox().getXsize() / 2.0, vec3.y + entity.getBoundingBox().getYsize(), vec3.z + entity.getBoundingBox().getZsize() / 2.0);
                if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                    Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, this.fillColor.getColor());
                }
                if (!this.mode.getValue().equalsIgnoreCase("Outline") && !this.mode.getValue().equalsIgnoreCase("Both")) continue;
                Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, this.outlineColor.getColor());
            }
        }
        this.renderBlockTargets(renderWorldEvent);
    }

    private void renderBlockTargets(RenderWorldEvent renderWorldEvent) {
        if (ESPModule.mc.level == null || !this.blocks.getValue() || this.matchedBlocks.isEmpty()) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("None")) {
            return;
        }
        boolean bl = this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both");
        boolean bl2 = this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both");
        boolean bl3 = bl2;
        if (!bl && !bl2) {
            return;
        }
        for (List<BlockPos> list : this.matchedBlocks.values()) {
            for (BlockPos blockPos : list) {
                BlockState blockState = ESPModule.mc.level.getBlockState(blockPos);
                if (!this.blockPredicate.test(blockState)) continue;
                VoxelShape voxelShape = blockState.getShape((BlockGetter)ESPModule.mc.level, blockPos);
                List<AABB> list2 = voxelShape.isEmpty() ? List.of(new AABB(blockPos)) : voxelShape.toAabbs().stream().map(aABB -> aABB.move(blockPos)).toList();
                for (AABB aABB2 : list2) {
                    if (bl) {
                        Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB2, this.fillColor.getColor());
                    }
                    if (!bl2) continue;
                    Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB2, this.outlineColor.getColor());
                }
            }
        }
    }

    private boolean isValidEntity(Entity entity) {
        if (this.players.getValue() && entity.getType() == EntityTypes.PLAYER) {
            return true;
        }
        if (this.hostiles.getValue() && EntityUtils.isHostile(entity)) {
            return true;
        }
        if (this.animals.getValue() && EntityUtils.isAnimal(entity) && !EntityUtils.isHostile(entity)) {
            return true;
        }
        if (this.ambient.getValue() && entity.getType().getCategory() == MobCategory.AMBIENT) {
            return true;
        }
        if (this.invisibles.getValue() && entity.isInvisible()) {
            return true;
        }
        if (this.items.getValue() && (entity.getType() == EntityTypes.ITEM || entity.getType() == EntityTypes.EXPERIENCE_BOTTLE)) {
            return true;
        }
        return this.others.getValue();
    }

    private static class FabricChunkEventsHelper {
        private FabricChunkEventsHelper() {
        }

        static void register() {
            ClientChunkEvents.CHUNK_LOAD.register((clientLevel, levelChunk) -> {
                ESPModule eSPModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ESPModule.class) : null;
                ESPModule eSPModule2 = eSPModule;
                if (eSPModule == null || !eSPModule.isToggled() || !eSPModule.blocks.getValue()) {
                    return;
                }
                if (Minecraft.getInstance().level != clientLevel) {
                    return;
                }
                eSPModule.queueChunk(levelChunk.getPos().x(), levelChunk.getPos().z());
            });
            ClientChunkEvents.CHUNK_UNLOAD.register((clientLevel, levelChunk) -> {
                ESPModule eSPModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ESPModule.class) : null;
                ESPModule eSPModule2 = eSPModule;
                if (eSPModule == null) {
                    return;
                }
                long l = ESPModule.chunkKey(levelChunk.getPos().x(), levelChunk.getPos().z());
                eSPModule.matchedBlocks.remove(l);
                eSPModule.pendingChunks.remove(l);
            });
        }
    }
}

