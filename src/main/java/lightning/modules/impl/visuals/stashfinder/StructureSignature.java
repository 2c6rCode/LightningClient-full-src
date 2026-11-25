/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.level.levelgen.structure.StructureStart
 */
package lightning.modules.impl.visuals.stashfinder;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureSignature {
    public static boolean isNatural(Minecraft minecraft, LevelChunk levelChunk, Map<String, Integer> map) {
        ChunkPos chunkPos;
        LevelChunk levelChunk2;
        ServerLevel serverLevel;
        if (minecraft.level == null) {
            return false;
        }
        int shulkers = map.getOrDefault("Shulkers", 0);
        int chests = map.getOrDefault("Chests", 0);
        int hoppers = map.getOrDefault("Hoppers", 0);
        int barrels = map.getOrDefault("Barrels", 0);
        int crafters = map.getOrDefault("Crafters", 0);
        int enderChests = map.getOrDefault("Ender Chests", 0);
        int donkeys = map.getOrDefault("Donkey", 0);
        int chestBoats = map.getOrDefault("Chest Boat", 0);
        if (shulkers >= 8 || chests >= 20 || hoppers >= 8 || barrels >= 16 || crafters >= 6 || enderChests >= 6 || donkeys >= 4 || chestBoats >= 3) {
            return false;
        }
        if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null && (serverLevel = minecraft.getSingleplayerServer().getLevel(minecraft.level.dimension())) != null && (levelChunk2 = serverLevel.getChunk((chunkPos = levelChunk.getPos()).x(), chunkPos.z())) != null) {
            for (Map.Entry longSet : levelChunk2.getAllStarts().entrySet()) {
                if (longSet.getValue() == null || !((StructureStart)longSet.getValue()).isValid()) continue;
                return true;
            }
            for (LongSet longSet : levelChunk2.getAllReferences().values()) {
                if (longSet == null || longSet.isEmpty()) continue;
                return true;
            }
            return false;
        }
        return StructureSignature.matchesMultiplayerHeuristic(minecraft, levelChunk);
    }

    private static boolean matchesMultiplayerHeuristic(Minecraft minecraft, LevelChunk levelChunk) {
        ResourceKey resourceKey = minecraft.level != null ? minecraft.level.dimension() : Level.OVERWORLD;
        int[] sigCounts = new int[Sig.values().length];
        LevelChunkSection[] levelChunkSectionArray = levelChunk.getSections();
        for (int i = 0; i < levelChunkSectionArray.length; ++i) {
            LevelChunkSection levelChunkSection = levelChunkSectionArray[i];
            if (levelChunkSection == null || levelChunkSection.hasOnlyAir()) continue;
            int baseY = levelChunk.getSectionYFromSectionIndex(i) << 4;
            for (int localY = 0; localY < 16; ++localY) {
                int worldY = baseY + localY;
                for (int localX = 0; localX < 16; ++localX) {
                    for (int localZ = 0; localZ < 16; ++localZ) {
                        Block block = levelChunkSection.getBlockState(localX, localY, localZ).getBlock();
                        for (Sig sig : Sig.values()) {
                            if (sig.dimension != null && sig.dimension != resourceKey || worldY < sig.yMin || worldY > sig.yMax || !sig.blocks.contains(block)) continue;
                            int sigIdx = sig.ordinal();
                            sigCounts[sigIdx] = sigCounts[sigIdx] + 1;
                        }
                    }
                }
            }
        }
        for (Sig sig : Sig.values()) {
            if (sig.dimension != null && sig.dimension != resourceKey || sigCounts[sig.ordinal()] < sig.threshold || sig.biomeTag != null && !StructureSignature.chunkHasBiome(levelChunk, sig)) continue;
            return true;
        }
        return false;
    }

    private static boolean chunkHasBiome(LevelChunk levelChunk, Sig sig) {
        if (sig.biomeTag == null) {
            return true;
        }
        ChunkPos chunkPos = levelChunk.getPos();
        int sampleY = Math.clamp((long)((sig.yMin + sig.yMax) / 2), levelChunk.getMinY(), levelChunk.getMaxY());
        int quartY = sampleY >> 2;
        int quartX = chunkPos.getMinBlockX() >> 2;
        int quartZ = chunkPos.getMinBlockZ() >> 2;
        int[][] offsets = new int[][]{{0, 0}, {3, 0}, {0, 3}, {3, 3}, {2, 2}};
        for (int[] offset : offsets) {
            Holder holder = levelChunk.getNoiseBiome(quartX + offset[0], quartY, quartZ + offset[1]);
            if (holder == null || !holder.is(sig.biomeTag)) continue;
            return true;
        }
        return false;
    }

    private static enum Sig {
        TRIAL_CHAMBERS(Set.of(Blocks.VAULT, Blocks.TRIAL_SPAWNER, Blocks.HEAVY_CORE, (Block)Blocks.CHISELED_COPPER.weathering().unaffected(), (Block)Blocks.COPPER_GRATE.weathering().unaffected(), Blocks.TUFF_BRICKS, Blocks.CHISELED_TUFF, Blocks.POLISHED_TUFF), 2, (TagKey<Biome>)BiomeTags.HAS_TRIAL_CHAMBERS, (ResourceKey<Level>)Level.OVERWORLD, -60, 20),
        ANCIENT_CITY(Set.of(Blocks.REINFORCED_DEEPSLATE, Blocks.SCULK_CATALYST, Blocks.SCULK_SHRIEKER, Blocks.SCULK_SENSOR, Blocks.SOUL_LANTERN, Blocks.SCULK), 8, (TagKey<Biome>)BiomeTags.HAS_ANCIENT_CITY, (ResourceKey<Level>)Level.OVERWORLD, -64, -10),
        END_CITY(Set.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_STAIRS, Blocks.END_STONE_BRICKS, Blocks.END_ROD), 15, (TagKey<Biome>)BiomeTags.HAS_END_CITY, (ResourceKey<Level>)Level.END, 40, 255),
        NETHER_FORTRESS(Set.of(Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART), 25, (TagKey<Biome>)BiomeTags.HAS_NETHER_FORTRESS, (ResourceKey<Level>)Level.NETHER, 30, 110),
        BASTION_REMNANT(Set.of(Blocks.GILDED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.CRYING_OBSIDIAN, Blocks.BLACKSTONE), 25, (TagKey<Biome>)BiomeTags.HAS_BASTION_REMNANT, (ResourceKey<Level>)Level.NETHER, 0, 128),
        OCEAN_MONUMENT(Set.of(Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.SEA_LANTERN, Blocks.WET_SPONGE), 35, (TagKey<Biome>)BiomeTags.HAS_OCEAN_MONUMENT, (ResourceKey<Level>)Level.OVERWORLD, 30, 70),
        WOODLAND_MANSION(Set.of(Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.COBBLESTONE), 120, (TagKey<Biome>)BiomeTags.HAS_WOODLAND_MANSION, (ResourceKey<Level>)Level.OVERWORLD, 50, 150),
        DESERT_PYRAMID(Set.of((Block)Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE), (Block)Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE), Blocks.CUT_SANDSTONE, Blocks.CHISELED_SANDSTONE), 6, (TagKey<Biome>)BiomeTags.HAS_DESERT_PYRAMID, (ResourceKey<Level>)Level.OVERWORLD, 40, 100),
        JUNGLE_TEMPLE(Set.of(Blocks.MOSSY_COBBLESTONE, Blocks.CHISELED_STONE_BRICKS, Blocks.TRIPWIRE_HOOK, Blocks.LEVER, Blocks.STICKY_PISTON), 8, (TagKey<Biome>)BiomeTags.HAS_JUNGLE_TEMPLE, (ResourceKey<Level>)Level.OVERWORLD, 50, 100),
        VILLAGE(Set.of(Blocks.BELL, Blocks.COMPOSTER, Blocks.DIRT_PATH, Blocks.HAY_BLOCK, Blocks.FARMLAND), 8, null, (ResourceKey<Level>)Level.OVERWORLD, 50, 150),
        MINESHAFT(Set.of(Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.ACTIVATOR_RAIL, Blocks.COBWEB, Blocks.OAK_FENCE, Blocks.DARK_OAK_FENCE, Blocks.SPRUCE_FENCE), 15, (TagKey<Biome>)BiomeTags.HAS_MINESHAFT, (ResourceKey<Level>)Level.OVERWORLD, -60, 60),
        STRONGHOLD(Set.of(Blocks.END_PORTAL_FRAME, Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.IRON_BARS), 12, (TagKey<Biome>)BiomeTags.HAS_STRONGHOLD, (ResourceKey<Level>)Level.OVERWORLD, -60, 40),
        SHIPWRECK(Set.of(Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_CHERRY_LOG), 15, (TagKey<Biome>)BiomeTags.HAS_SHIPWRECK, (ResourceKey<Level>)Level.OVERWORLD, 20, 80),
        PILLAGER_OUTPOST(Set.of(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.BIRCH_PLANKS, Blocks.TARGET), 30, null, (ResourceKey<Level>)Level.OVERWORLD, 50, 150),
        DUNGEON(Set.of(Blocks.SPAWNER, Blocks.MOSSY_COBBLESTONE), 6, null, (ResourceKey<Level>)Level.OVERWORLD, -60, 60),
        RUINED_PORTAL(Set.of(Blocks.CRYING_OBSIDIAN, Blocks.MAGMA_BLOCK, Blocks.GOLD_BLOCK), 4, (TagKey<Biome>)BiomeTags.HAS_RUINED_PORTAL_STANDARD, null, 30, 120);

        final Set<Block> blocks;
        final int threshold;
        final TagKey<Biome> biomeTag;
        final ResourceKey<Level> dimension;
        final int yMin;
        final int yMax;

        private Sig(Set<Block> blocks, int threshold, TagKey<Biome> biomeTag, ResourceKey<Level> dimension, int yMin, int yMax) {
            this.blocks = blocks;
            this.threshold = threshold;
            this.biomeTag = biomeTag;
            this.dimension = dimension;
            this.yMin = yMin;
            this.yMax = yMax;
        }
    }
}

