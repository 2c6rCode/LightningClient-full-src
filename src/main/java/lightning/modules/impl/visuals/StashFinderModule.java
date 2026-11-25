/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.animal.equine.Donkey
 *  net.minecraft.world.entity.animal.equine.Llama
 *  net.minecraft.world.entity.vehicle.boat.ChestBoat
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.block.entity.BarrelBlockEntity
 *  net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.ChestBlockEntity
 *  net.minecraft.world.level.block.entity.CrafterBlockEntity
 *  net.minecraft.world.level.block.entity.DispenserBlockEntity
 *  net.minecraft.world.level.block.entity.DropperBlockEntity
 *  net.minecraft.world.level.block.entity.EnderChestBlockEntity
 *  net.minecraft.world.level.block.entity.FurnaceBlockEntity
 *  net.minecraft.world.level.block.entity.HopperBlockEntity
 *  net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity
 *  net.minecraft.world.level.block.entity.SmokerBlockEntity
 *  net.minecraft.world.level.block.entity.TrappedChestBlockEntity
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.phys.AABB
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.visuals.stashfinder.StashFinderStore;
import lightning.modules.impl.visuals.stashfinder.StashWebhook;
import lightning.modules.impl.visuals.stashfinder.StructureSignature;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.graphics.Renderer3D;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

@RegisterModule(name="StashFinder", description="Scans loaded chunks for loot stashes and storage animals, alerting via chat and Discord webhook.", category=Module.Category.VISUALS)
public class StashFinderModule
extends Module {
    public CategorySetting containersCategory = new CategorySetting("Containers", "Threshold settings for containers.");
    public NumberSetting minChests = new NumberSetting("Chests", "Minimum chests required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minBarrels = new NumberSetting("Barrels", "Minimum barrels required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minShulkers = new NumberSetting("Shulkers", "Minimum shulker boxes required.", new CategorySetting.Visibility(this.containersCategory), (Number)4, (Number)1, (Number)64);
    public NumberSetting minEnderChests = new NumberSetting("EnderChests", "Minimum ender chests required.", new CategorySetting.Visibility(this.containersCategory), (Number)2, (Number)1, (Number)64);
    public NumberSetting minHoppers = new NumberSetting("Hoppers", "Minimum hoppers required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minDispensersDroppers = new NumberSetting("Dispensers/Droppers", "Minimum dispensers or droppers required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minFurnaces = new NumberSetting("Furnaces", "Minimum furnaces required.", new CategorySetting.Visibility(this.containersCategory), (Number)10, (Number)1, (Number)64);
    public NumberSetting minCrafters = new NumberSetting("Crafters", "Minimum crafters required.", new CategorySetting.Visibility(this.containersCategory), (Number)4, (Number)1, (Number)64);
    public CategorySetting animalsCategory = new CategorySetting("Animals", "Threshold settings for storage animals.");
    public NumberSetting minDonkeys = new NumberSetting("Donkeys", "Minimum donkeys required.", new CategorySetting.Visibility(this.animalsCategory), (Number)2, (Number)1, (Number)16);
    public NumberSetting minLlamas = new NumberSetting("Llamas", "Minimum llamas required.", new CategorySetting.Visibility(this.animalsCategory), (Number)2, (Number)1, (Number)16);
    public NumberSetting minChestBoats = new NumberSetting("ChestBoats", "Minimum chest boats required.", new CategorySetting.Visibility(this.animalsCategory), (Number)1, (Number)1, (Number)16);
    public CategorySetting generalCategory = new CategorySetting("General", "General settings.");
    public BooleanSetting ignoreNatural = new BooleanSetting("IgnoreNatural", "Ignores naturally generated structures.", new CategorySetting.Visibility(this.generalCategory), true);
    public BooleanSetting chatNotify = new BooleanSetting("ChatNotify", "Notifies in chat when a stash is found.", new CategorySetting.Visibility(this.generalCategory), true);
    public BooleanSetting renderHighlight = new BooleanSetting("RenderHighlight", "Highlights found stash chunks in the world.", new CategorySetting.Visibility(this.generalCategory), true);
    public ColorSetting highlightColor = new ColorSetting("HighlightColor", "Highlight color for stash chunks.", new CategorySetting.Visibility(this.generalCategory), new ColorSetting.Color(new Color(0, 230, 120, 100), false, false));
    public CategorySetting webhookCategory = new CategorySetting("Webhook", "Discord webhook settings.");
    public BooleanSetting sendWebhook = new BooleanSetting("SendWebhook", "Sends stash alerts to Discord webhook.", new CategorySetting.Visibility(this.webhookCategory), true);
    public StringSetting webhookUrl = new StringSetting("WebhookURL", "Discord webhook URL.", new CategorySetting.Visibility(this.webhookCategory), "");
    public StringSetting userId = new StringSetting("UserID", "Discord User ID to ping.", new CategorySetting.Visibility(this.webhookCategory), "");
    private final StashFinderStore store = new StashFinderStore();
    private String lastWorldKey = null;
    private final Map<Long, Integer> pending = new HashMap<Long, Integer>();
    private static final int SCAN_DELAY_TICKS = 25;
    private static final int MAX_EVALS_PER_TICK = 4;
    private static final int BOOTSTRAP_RADIUS_CHUNKS = 12;
    private static boolean listenerRegistered = false;

    public StashFinderModule() {
        this.registerChunkListener();
    }

    private void registerChunkListener() {
        if (listenerRegistered) {
            return;
        }
        listenerRegistered = true;
        try {
            Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents");
            FabricChunkEventsHelper.register();
        }
        catch (Throwable throwable) {
            Night.LOGGER.warn("Fabric Chunk Events API not available or optional: {}", (Object)throwable.getMessage());
        }
    }

    @Override
    public void onEnable() {
        this.pending.clear();
        this.store.loadForWorld();
        this.lastWorldKey = StashFinderStore.currentWorldKey();
        if (StashFinderModule.mc.player == null || StashFinderModule.mc.level == null) {
            return;
        }
        int playerChunkX = StashFinderModule.mc.player.blockPosition().getX() >> 4;
        int playerChunkZ = StashFinderModule.mc.player.blockPosition().getZ() >> 4;
        for (int i = -12; i <= 12; ++i) {
            for (int j = -12; j <= 12; ++j) {
                long key;
                int chunkX = playerChunkX + i;
                int chunkZ = playerChunkZ + j;
                if (!StashFinderModule.mc.level.hasChunk(chunkX, chunkZ) || this.store.isEvaluated(key = StashFinderStore.chunkKey(chunkX, chunkZ))) continue;
                this.pending.putIfAbsent(key, 25);
            }
        }
    }

    @Override
    public void onDisable() {
        this.store.flush();
        this.pending.clear();
    }

    private void maybeReloadStoreForWorld() {
        String worldKey = StashFinderStore.currentWorldKey();
        if (!worldKey.equals(this.lastWorldKey)) {
            this.store.loadForWorld();
            this.lastWorldKey = worldKey;
            this.pending.clear();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (StashFinderModule.mc.player == null || StashFinderModule.mc.level == null) {
            return;
        }
        this.maybeReloadStoreForWorld();
        this.store.flushIfStale();
        if (this.pending.isEmpty()) {
            return;
        }
        int evalBudget = 4;
        Iterator<Map.Entry<Long, Integer>> iterator = this.pending.entrySet().iterator();
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
            int chunkX = (int)(key & 0xFFFFFFFFL);
            int chunkZ = (int)(key >>> 32);
            this.evaluateChunk(chunkX, chunkZ, key);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (!this.renderHighlight.getValue() || StashFinderModule.mc.level == null) {
            return;
        }
        Color color = this.highlightColor.getColor();
        Color outlineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        for (StashFinderStore.StashData stashData : this.store.getStashes()) {
            AABB aABB = new AABB((double)stashData.x(), (double)StashFinderModule.mc.level.getMinY(), (double)stashData.z(), (double)(stashData.x() + 16), (double)StashFinderModule.mc.level.getMaxY(), (double)(stashData.z() + 16));
            Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, color);
            Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, outlineColor);
        }
    }

    public void resetCurrentWorldStore() {
        this.store.loadForWorld();
        this.store.clearAll();
        this.pending.clear();
        this.lastWorldKey = StashFinderStore.currentWorldKey();
    }

    private void evaluateChunk(int chunkX, int chunkZ, long chunkKey) {
        if (StashFinderModule.mc.level == null || !StashFinderModule.mc.level.hasChunk(chunkX, chunkZ)) {
            return;
        }
        LevelChunk levelChunk = StashFinderModule.mc.level.getChunk(chunkX, chunkZ);
        this.store.markEvaluated(chunkKey);
        Map<String, Integer> counts = this.countContainers(levelChunk);
        counts.putAll(this.countAnimals(levelChunk));
        if (!this.meetsAnyThreshold(counts)) {
            return;
        }
        if (this.ignoreNatural.getValue() && StructureSignature.isNatural(mc, levelChunk, counts)) {
            return;
        }
        LinkedHashMap<String, Integer> nonZeroCounts = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() <= 0) continue;
            nonZeroCounts.put(entry.getKey(), entry.getValue());
        }
        ChunkPos chunkPos = levelChunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        String dimension = StashFinderModule.mc.level.dimension().identifier().toString();
        this.store.recordStash(minX, minZ, dimension, nonZeroCounts);
        if (this.chatNotify.getValue()) {
            StringBuilder summary = new StringBuilder();
            summary.append("Stash found at X: ").append(minX).append(" Z: ").append(minZ).append(" (");
            int idx = 0;
            for (Map.Entry<String, Integer> entry : nonZeroCounts.entrySet()) {
                if (idx++ > 0) {
                    summary.append(", ");
                }
                summary.append(entry.getKey()).append(": ").append(entry.getValue());
            }
            summary.append(")");
            Night.CHAT_MANAGER.tagged(summary.toString(), "StashFinder");
        }
        if (this.sendWebhook.getValue()) {
            String url = this.webhookUrl.getValue();
            String pingUserId = this.userId.getValue();
            StashWebhook.send(minX, minZ, dimension, nonZeroCounts, url, pingUserId);
        }
    }

    private boolean meetsAnyThreshold(Map<String, Integer> counts) {
        return this.check(counts, "Chests", this.minChests) || this.check(counts, "Barrels", this.minBarrels) || this.check(counts, "Shulkers", this.minShulkers) || this.check(counts, "Ender Chests", this.minEnderChests) || this.check(counts, "Hoppers", this.minHoppers) || this.check(counts, "Dispensers/Droppers", this.minDispensersDroppers) || this.check(counts, "Furnaces", this.minFurnaces) || this.check(counts, "Crafters", this.minCrafters) || this.check(counts, "Donkey", this.minDonkeys) || this.check(counts, "Llama", this.minLlamas) || this.check(counts, "Chest Boat", this.minChestBoats);
    }

    private boolean check(Map<String, Integer> counts, String key, NumberSetting setting) {
        int threshold = setting.getValue().intValue();
        if (threshold <= 0) {
            return false;
        }
        return counts.getOrDefault(key, 0) >= threshold;
    }

    private Map<String, Integer> countContainers(LevelChunk levelChunk) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        int chests = 0;
        int barrels = 0;
        int shulkers = 0;
        int enderChests = 0;
        int hoppers = 0;
        int dispensersDroppers = 0;
        int furnaces = 0;
        int crafters = 0;
        for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
            if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof TrappedChestBlockEntity) {
                ++chests;
                continue;
            }
            if (blockEntity instanceof BarrelBlockEntity) {
                ++barrels;
                continue;
            }
            if (blockEntity instanceof ShulkerBoxBlockEntity) {
                ++shulkers;
                continue;
            }
            if (blockEntity instanceof EnderChestBlockEntity) {
                ++enderChests;
                continue;
            }
            if (blockEntity instanceof HopperBlockEntity) {
                ++hoppers;
                continue;
            }
            if (blockEntity instanceof DispenserBlockEntity || blockEntity instanceof DropperBlockEntity) {
                ++dispensersDroppers;
                continue;
            }
            if (blockEntity instanceof FurnaceBlockEntity || blockEntity instanceof BlastFurnaceBlockEntity || blockEntity instanceof SmokerBlockEntity) {
                ++furnaces;
                continue;
            }
            if (!(blockEntity instanceof CrafterBlockEntity)) continue;
            ++crafters;
        }
        counts.put("Chests", chests);
        counts.put("Barrels", barrels);
        counts.put("Shulkers", shulkers);
        counts.put("Ender Chests", enderChests);
        counts.put("Hoppers", hoppers);
        counts.put("Dispensers/Droppers", dispensersDroppers);
        counts.put("Furnaces", furnaces);
        counts.put("Crafters", crafters);
        return counts;
    }

    private Map<String, Integer> countAnimals(LevelChunk levelChunk) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        int donkeys = 0;
        int llamas = 0;
        int chestBoats = 0;
        if (StashFinderModule.mc.level == null) {
            return counts;
        }
        ChunkPos chunkPos = levelChunk.getPos();
        AABB aABB = new AABB((double)chunkPos.getMinBlockX(), (double)StashFinderModule.mc.level.getMinY(), (double)chunkPos.getMinBlockZ(), (double)(chunkPos.getMaxBlockX() + 1), (double)StashFinderModule.mc.level.getMaxY(), (double)(chunkPos.getMaxBlockZ() + 1));
        for (Entity entity : StashFinderModule.mc.level.getEntities((Entity)null, aABB, candidate -> true)) {
            if (entity instanceof Donkey) {
                ++donkeys;
                continue;
            }
            if (entity instanceof Llama) {
                ++llamas;
                continue;
            }
            if (!(entity instanceof ChestBoat)) continue;
            ++chestBoats;
        }
        counts.put("Donkey", donkeys);
        counts.put("Llama", llamas);
        counts.put("Chest Boat", chestBoats);
        return counts;
    }

    private static class FabricChunkEventsHelper {
        private FabricChunkEventsHelper() {
        }

        static void register() {
            ClientChunkEvents.CHUNK_LOAD.register((clientLevel, levelChunk) -> {
                StashFinderModule stashFinderModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(StashFinderModule.class) : null;
                if (stashFinderModule == null || !stashFinderModule.isToggled()) {
                    return;
                }
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != clientLevel) {
                    return;
                }
                long key = StashFinderStore.chunkKey(levelChunk.getPos().x(), levelChunk.getPos().z());
                if (stashFinderModule.store.isEvaluated(key)) {
                    return;
                }
                stashFinderModule.pending.putIfAbsent(key, 25);
            });
        }
    }
}

