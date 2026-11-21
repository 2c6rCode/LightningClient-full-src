/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  lightning.mixins.accessors.PlayerListS2CPacketAccessor
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket
 *  net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
 *  net.minecraft.network.protocol.game.ClientboundGameEventPacket
 *  net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
 *  net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
 *  net.minecraft.network.protocol.game.ClientboundLoginPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundRespawnPacket
 *  net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
 *  net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket
 *  net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
 *  net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
 *  net.minecraft.network.protocol.game.ClientboundSetExperiencePacket
 *  net.minecraft.network.protocol.game.ClientboundSetHealthPacket
 *  net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTimePacket
 *  net.minecraft.network.protocol.game.CommonPlayerSpawnInfo
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.PositionMoveRotation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.status.ChunkStatus
 *  net.minecraft.world.phys.Vec3
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.server;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lightning.mixins.accessors.PlayerListS2CPacketAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldStateReplay {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldStateReplay.class);
    private static final Random RANDOM = new Random();

    public static int replay(Connection connection, RegistryAccess registryAccess) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        ClientLevel clientLevel = minecraft.level;
        ClientPacketListener clientPacketListener = minecraft.getConnection();
        if (localPlayer != null && clientLevel != null && clientPacketListener != null) {
            LOGGER.info("Replaying world state to client...");
            long l = System.currentTimeMillis();
            int n = -Math.abs(RANDOM.nextInt());
            if (n == 0) {
                n = -1;
            }
            GameType gameType = minecraft.gameMode != null ? minecraft.gameMode.getPlayerMode() : GameType.SURVIVAL;
            GameType gameType2 = minecraft.gameMode != null ? minecraft.gameMode.getPreviousPlayerMode() : null;
            Set set = clientPacketListener.levels();
            CommonPlayerSpawnInfo commonPlayerSpawnInfo = WorldStateReplay.createSpawnInfo(clientLevel, gameType, gameType2, localPlayer, registryAccess);
            WorldStateReplay.send(connection, new ClientboundLoginPacket(localPlayer.getId(), clientLevel.getLevelData().isHardcore(), set, 1, 16, 16, false, true, false, commonPlayerSpawnInfo, false, false));
            WorldStateReplay.send(connection, new ClientboundRespawnPacket(commonPlayerSpawnInfo, (byte)0));
            WorldStateReplay.send(connection, new ClientboundChangeDifficultyPacket(clientLevel.getLevelData().getDifficulty(), clientLevel.getLevelData().isDifficultyLocked()));
            WorldStateReplay.send(connection, new ClientboundPlayerAbilitiesPacket(localPlayer.getAbilities()));
            WorldStateReplay.send(connection, new ClientboundSetHeldSlotPacket(localPlayer.getInventory().getSelectedSlot()));
            WorldStateReplay.sendPlayerInfo(connection, clientPacketListener);
            WorldStateReplay.sendLevelInfo(connection, clientLevel);
            WorldStateReplay.send(connection, new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0f));
            WorldStateReplay.sendChunks(connection, localPlayer, clientLevel);
            WorldStateReplay.send(connection, new ClientboundSetHealthPacket(localPlayer.getHealth(), localPlayer.getFoodData().getFoodLevel(), localPlayer.getFoodData().getSaturationLevel()));
            WorldStateReplay.send(connection, new ClientboundSetExperiencePacket(localPlayer.experienceProgress, localPlayer.totalExperience, localPlayer.experienceLevel));
            WorldStateReplay.send(connection, new ClientboundContainerSetContentPacket(localPlayer.inventoryMenu.containerId, localPlayer.inventoryMenu.incrementStateId(), (List)localPlayer.inventoryMenu.getItems(), localPlayer.inventoryMenu.getCarried()));
            WorldStateReplay.sendEntities(connection, clientLevel, localPlayer);
            WorldStateReplay.send(connection, new ClientboundPlayerPositionPacket(n, new PositionMoveRotation(localPlayer.position(), Vec3.ZERO, localPlayer.getYRot(), localPlayer.getXRot()), Set.of()));
            WorldStateReplay.send(connection, new ClientboundSetEntityMotionPacket((Entity)localPlayer));
            long l2 = System.currentTimeMillis() - l;
            LOGGER.info("Level state replay completed in {}ms (teleportId={})", (Object)l2, (Object)n);
            return n;
        }
        LOGGER.error("Cannot replay world state: player/world/handler is null");
        return 0;
    }

    private static void sendLevelInfo(Connection connection, ClientLevel clientLevel) {
        WorldStateReplay.send(connection, new ClientboundInitializeBorderPacket(clientLevel.getWorldBorder()));
        WorldStateReplay.send(connection, new ClientboundSetTimePacket(clientLevel.getLevelData().getGameTime(), Map.of()));
        WorldStateReplay.send(connection, new ClientboundSetDefaultSpawnPositionPacket(clientLevel.getLevelData().getRespawnData()));
        if (clientLevel.isRaining()) {
            WorldStateReplay.send(connection, new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0f));
            WorldStateReplay.send(connection, new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, clientLevel.getRainLevel(1.0f)));
            WorldStateReplay.send(connection, new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, clientLevel.getThunderLevel(1.0f)));
        }
    }

    private static void sendPlayerInfo(Connection connection, ClientPacketListener clientPacketListener) {
        Collection<PlayerInfo> collection = clientPacketListener.getOnlinePlayers();
        if (!collection.isEmpty()) {
            ArrayList<ClientboundPlayerInfoUpdatePacket.Entry> arrayList = new ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>();
            for (PlayerInfo playerInfo : collection) {
                arrayList.add(new ClientboundPlayerInfoUpdatePacket.Entry(playerInfo.getProfile().id(), playerInfo.getProfile(), clientPacketListener.getListedOnlinePlayers().contains(playerInfo), playerInfo.getLatency(), playerInfo.getGameMode(), playerInfo.getTabListDisplayName(), false, 0, null));
            }
            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> enumSet = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
            ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), Collections.emptyList());
            ((PlayerListS2CPacketAccessor)packet).setActions((EnumSet)enumSet);
            ((PlayerListS2CPacketAccessor)packet).setEntries(arrayList);
            WorldStateReplay.send(connection, packet);
            LOGGER.info("Sent {} player list entries to client", (Object)arrayList.size());
        }
    }

    private static CommonPlayerSpawnInfo createSpawnInfo(ClientLevel clientLevel, GameType gameType, GameType gameType2, LocalPlayer localPlayer, RegistryAccess registryAccess) {
        ResourceKey resourceKey = (ResourceKey)clientLevel.dimensionTypeRegistration().unwrapKey().orElseThrow();
        Holder.Reference reference = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(resourceKey);
        return new CommonPlayerSpawnInfo((Holder)reference, clientLevel.dimension(), 0L, gameType, gameType2, clientLevel.isDebug(), false, localPlayer.getLastDeathLocation(), localPlayer.getPortalCooldown(), clientLevel.getSeaLevel());
    }

    private static void sendEntities(Connection connection, ClientLevel clientLevel, LocalPlayer localPlayer) {
        int n = 0;
        for (Entity entity : clientLevel.entitiesForRendering()) {
            if (entity == localPlayer) continue;
            try {
                WorldStateReplay.send(connection, new ClientboundAddEntityPacket(entity.getId(), entity.getUUID(), entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot(), entity.getType(), 0, entity.getDeltaMovement(), (double)entity.getYHeadRot()));
                List list = entity.getEntityData().getNonDefaultValues();
                if (list != null && !list.isEmpty()) {
                    WorldStateReplay.send(connection, new ClientboundSetEntityDataPacket(entity.getId(), list));
                }
                WorldStateReplay.send(connection, new ClientboundSetEntityMotionPacket(entity));
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)entity;
                    WorldStateReplay.send(connection, new ClientboundRotateHeadPacket(entity, (byte)(livingEntity.getYHeadRot() * 256.0f / 360.0f)));
                    ArrayList<Pair<EquipmentSlot, ItemStack>> arrayList = new ArrayList<Pair<EquipmentSlot, ItemStack>>();
                    for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                        ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
                        if (itemStack.isEmpty()) continue;
                        arrayList.add(new Pair<EquipmentSlot, ItemStack>(equipmentSlot, itemStack));
                    }
                    if (!arrayList.isEmpty()) {
                        WorldStateReplay.send(connection, new ClientboundSetEquipmentPacket(entity.getId(), arrayList));
                    }
                }
                ++n;
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to send entity {} (ID: {})", new Object[]{entity.getType().getDescriptionId(), entity.getId(), exception});
            }
        }
        LOGGER.info("Sent {} entities to client", (Object)n);
    }

    private static void sendChunks(Connection connection, LocalPlayer localPlayer, ClientLevel clientLevel) {
        int n = localPlayer.chunkPosition().x();
        int n2 = localPlayer.chunkPosition().z();
        WorldStateReplay.send(connection, new ClientboundSetChunkCacheCenterPacket(n, n2));
        int n3 = Math.min((Integer)Minecraft.getInstance().options.renderDistance().get(), 8);
        int n4 = 0;
        for (int i = n2 - n3; i <= n2 + n3; ++i) {
            for (int j = n - n3; j <= n + n3; ++j) {
                LevelChunk levelChunk = clientLevel.getChunkSource().getChunk(j, i, ChunkStatus.FULL, false);
                if (levelChunk == null) continue;
                WorldStateReplay.send(connection, new ClientboundLevelChunkWithLightPacket(levelChunk, clientLevel.getChunkSource().getLightEngine(), null, null));
                ++n4;
            }
        }
        LOGGER.info("Sent {} chunks to client", (Object)n4);
    }

    private static void send(Connection connection, Packet<?> packet) {
        if (connection.isConnected()) {
            connection.send(packet);
        }
    }
}

