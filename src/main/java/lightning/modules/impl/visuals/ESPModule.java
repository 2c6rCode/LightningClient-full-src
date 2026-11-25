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

    private static long chunkKey(int chunkX, int chunkZ) {
        return (long)chunkX & 0xFFFFFFFFL | (long)chunkZ << 32;
    }

    private void queueChunk(int chunkX, int chunkZ) {
        this.pendingChunks.putIfAbsent(ESPModule.chunkKey(chunkX, chunkZ), 5);
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
        int playerChunkX = ESPModule.mc.player.blockPosition().getX() >> 4;
        int playerChunkZ = ESPModule.mc.player.blockPosition().getZ() >> 4;
        int renderDistance = ESPModule.mc.options.getEffectiveRenderDistance();
        for (int i = -renderDistance; i <= renderDistance; ++i) {
            for (int j = -renderDistance; j <= renderDistance; ++j) {
                int chunkX = playerChunkX + i;
                int chunkZ = playerChunkZ + j;
                if (!ESPModule.mc.level.hasChunk(chunkX, chunkZ)) continue;
                this.queueChunk(chunkX, chunkZ);
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
        int evalBudget = 2;
        Iterator<Map.Entry<Long, Integer>> iterator = this.pendingChunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Integer> entry = iterator.next();
            int remainingTicks = entry.getValue() - 1;
            if (remainingTicks > 0) {
                entry.setValue(remainingTicks);
                continue;
            }
            if (evalBudget == 0) {
                entry.setValue(1);
                continue;
            }
            --evalBudget;
            iterator.remove();
            long key = entry.getKey();
            int chunkX = (int)key;
            int chunkZ = (int)(key >>> 32);
            this.evaluateChunk(chunkX, chunkZ, key);
        }
    }

    private void evaluateChunk(int chunkX, int chunkZ, long chunkKey) {
        if (ESPModule.mc.level == null || !ESPModule.mc.level.hasChunk(chunkX, chunkZ)) {
            return;
        }
        LevelChunk levelChunk = ESPModule.mc.level.getChunk(chunkX, chunkZ);
        ArrayList<BlockPos> foundPositions = new ArrayList<BlockPos>();
        LevelChunkSection[] levelChunkSectionArray = levelChunk.getSections();
        int minSectionY = ESPModule.mc.level.getMinSectionY();
        for (int i = 0; i < levelChunkSectionArray.length; ++i) {
            LevelChunkSection levelChunkSection = levelChunkSectionArray[i];
            if (levelChunkSection == null || levelChunkSection.hasOnlyAir() || !levelChunkSection.maybeHas(this.blockPredicate)) continue;
            int baseY = minSectionY + i << 4;
            int baseX = chunkX << 4;
            int baseZ = chunkZ << 4;
            for (int localX = 0; localX < 16; ++localX) {
                for (int localY = 0; localY < 16; ++localY) {
                    for (int localZ = 0; localZ < 16; ++localZ) {
                        BlockState blockState = levelChunkSection.getBlockState(localX, localY, localZ);
                        if (!this.blockPredicate.test(blockState)) continue;
                        foundPositions.add(new BlockPos(baseX + localX, baseY + localY, baseZ + localZ));
                    }
                }
            }
        }
        if (foundPositions.isEmpty()) {
            this.matchedBlocks.remove(chunkKey);
        } else {
            this.matchedBlocks.put(chunkKey, foundPositions);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (ESPModule.mc.level == null) {
            return;
        }
        ArrayList<Entity> entities = new ArrayList<Entity>();
        for (Entity entity : ESPModule.mc.level.entitiesForRendering()) {
            if (entity == ESPModule.mc.player || !this.isValidEntity(entity)) continue;
            entities.add(entity);
        }
        this.targetEntities = entities;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (ESPModule.mc.level == null) {
            return;
        }
        if (!this.targetEntities.isEmpty()) {
            for (Entity entity : this.targetEntities) {
                Vec3 renderPos = EntityUtils.getRenderPos(entity, renderWorldEvent.getTickDelta());
                AABB aABB = new AABB(renderPos.x - entity.getBoundingBox().getXsize() / 2.0, renderPos.y, renderPos.z - entity.getBoundingBox().getZsize() / 2.0, renderPos.x + entity.getBoundingBox().getXsize() / 2.0, renderPos.y + entity.getBoundingBox().getYsize(), renderPos.z + entity.getBoundingBox().getZsize() / 2.0);
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
        boolean renderFill = this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both");
        boolean renderOutline = this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both");
        if (!renderFill && !renderOutline) {
            return;
        }
        for (List<BlockPos> positions : this.matchedBlocks.values()) {
            for (BlockPos blockPos : positions) {
                BlockState blockState = ESPModule.mc.level.getBlockState(blockPos);
                if (!this.blockPredicate.test(blockState)) continue;
                VoxelShape voxelShape = blockState.getShape((BlockGetter)ESPModule.mc.level, blockPos);
                List<AABB> boxes = voxelShape.isEmpty() ? List.of(new AABB(blockPos)) : voxelShape.toAabbs().stream().map(aABB -> aABB.move(blockPos)).toList();
                for (AABB box : boxes) {
                    if (renderFill) {
                        Renderer3D.renderBox(renderWorldEvent.getMatrices(), box, this.fillColor.getColor());
                    }
                    if (!renderOutline) continue;
                    Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), box, this.outlineColor.getColor());
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
                if (eSPModule == null) {
                    return;
                }
                long chunkKey = ESPModule.chunkKey(levelChunk.getPos().x(), levelChunk.getPos().z());
                eSPModule.matchedBlocks.remove(chunkKey);
                eSPModule.pendingChunks.remove(chunkKey);
            });
        }
    }
}

