/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 *  net.minecraft.core.LayeredRegistryAccess
 *  net.minecraft.core.Registry$PendingTags
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.RegistryAccess$Frozen
 *  net.minecraft.core.RegistrySynchronization
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket
 *  net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket
 *  net.minecraft.resources.RegistryDataLoader
 *  net.minecraft.resources.RegistryOps
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.RegistryLayer
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.repository.KnownPack
 *  net.minecraft.server.packs.repository.PackRepository
 *  net.minecraft.server.packs.repository.ServerPacksSource
 *  net.minecraft.server.packs.resources.MultiPackResourceManager
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.tags.TagLoader
 *  net.minecraft.tags.TagNetworkSerialization
 *  net.minecraft.world.level.WorldDataConfiguration
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.server;

import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryCache.class);
    private static final Executor SYNC_EXECUTOR = Runnable::run;
    private final List<Packet<?>> registryPackets = new ArrayList();
    private final List<KnownPack> knownPacks = new ArrayList<KnownPack>();
    private LayeredRegistryAccess<RegistryLayer> registries;
    private RegistryAccess registryManager;
    private boolean loaded;
    private volatile boolean loadAttempted;

    public void load() {
        LOGGER.info("Loading vanilla registries for proxy...");
        long startTime = System.currentTimeMillis();
        try {
            PackRepository packRepository = ServerPacksSource.createVanillaTrustedRepository();
            MinecraftServer.configurePackRepository((PackRepository)packRepository, (WorldDataConfiguration)WorldDataConfiguration.DEFAULT, (boolean)true, (boolean)true);
            List list2 = packRepository.openAllSelected();
            try (MultiPackResourceManager multiPackResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, list2);){
                LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = RegistryLayer.createRegistryAccess();
                List<Registry.PendingTags<?>> list3 = TagLoader.loadTagsForExistingRegistries((ResourceManager)multiPackResourceManager, (RegistryAccess)layeredRegistryAccess.getLayer(RegistryLayer.STATIC));
                RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(RegistryLayer.WORLDGEN);
                List list4 = TagLoader.buildUpdatedLookups((RegistryAccess.Frozen)frozen, (List)list3);
                RegistryAccess.Frozen frozen2 = (RegistryAccess.Frozen)RegistryDataLoader.load((ResourceManager)multiPackResourceManager, (List)list4, (List)RegistryDataLoader.WORLDGEN_REGISTRIES, (Executor)SYNC_EXECUTOR).join();
                List list5 = Stream.concat(list4.stream(), frozen2.listRegistries()).toList();
                RegistryAccess.Frozen frozen3 = (RegistryAccess.Frozen)RegistryDataLoader.load((ResourceManager)multiPackResourceManager, list5, (List)RegistryDataLoader.DIMENSION_REGISTRIES, (Executor)SYNC_EXECUTOR).join();
                this.registries = layeredRegistryAccess.replaceFrom(RegistryLayer.WORLDGEN, new RegistryAccess.Frozen[]{frozen2, frozen3});
                for (Registry.PendingTags<?> pendingTags : list3) {
                    pendingTags.apply();
                }
                this.registryManager = this.registries.compositeAccess();
                this.knownPacks.addAll(multiPackResourceManager.listPacks().flatMap(packResources -> packResources.knownPackInfo().stream()).toList());
                RegistryOps registryOps = this.registries.compositeAccess().createSerializationContext((DynamicOps)NbtOps.INSTANCE);
                RegistrySynchronization.packRegistries((DynamicOps)registryOps, (RegistryAccess)this.registries.getAccessFrom(RegistryLayer.WORLDGEN), Set.of(), (resourceKey, list) -> this.registryPackets.add((Packet<?>)new ClientboundRegistryDataPacket(resourceKey, list)));
                this.registryPackets.add((Packet<?>)new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
                this.loaded = true;
            }
            long elapsedMs = System.currentTimeMillis() - startTime;
            LOGGER.info("Loaded {} registry packets in {}ms", (Object)this.registryPackets.size(), (Object)elapsedMs);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to load vanilla registries", (Throwable)exception);
        }
    }

    public boolean isLoaded() {
        if (!this.loaded && !this.loadAttempted) {
            this.loadAttempted = true;
            this.load();
        }
        return this.loaded;
    }

    public List<Packet<?>> getRegistryPackets() {
        return this.registryPackets;
    }

    public List<KnownPack> getKnownPacks() {
        return this.knownPacks;
    }

    public LayeredRegistryAccess<RegistryLayer> getRegistries() {
        return this.registries;
    }

    public RegistryAccess getRegistryManager() {
        return this.registryManager;
    }
}

