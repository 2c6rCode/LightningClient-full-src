/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.TickablePacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ClientboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundClientInformationPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundPongPacket
 *  net.minecraft.network.protocol.common.ServerboundResourcePackPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket
 *  net.minecraft.network.protocol.game.GameProtocols
 *  net.minecraft.network.protocol.game.ServerGamePacketListener
 *  net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket
 *  net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket
 *  net.minecraft.network.protocol.game.ServerboundChangeGameModePacket
 *  net.minecraft.network.protocol.game.ServerboundChatAckPacket
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket
 *  net.minecraft.network.protocol.game.ServerboundChatPacket
 *  net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket
 *  net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket
 *  net.minecraft.network.protocol.game.ServerboundClientCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
 *  net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket
 *  net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClickPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket
 *  net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket
 *  net.minecraft.network.protocol.game.ServerboundEditBookPacket
 *  net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket
 *  net.minecraft.network.protocol.game.ServerboundInteractPacket
 *  net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket
 *  net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket
 *  net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket
 *  net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket
 *  net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket
 *  net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket
 *  net.minecraft.network.protocol.game.ServerboundRenameItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket
 *  net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSelectTradePacket
 *  net.minecraft.network.protocol.game.ServerboundSetBeaconPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
 *  net.minecraft.network.protocol.game.ServerboundSetGameRulePacket
 *  net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
 *  net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.network.protocol.ping.ServerboundPingRequestPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.util.concurrent.ThreadLocalRandom;
import lightning.Night;
import lightning.pingbypass.handler.PbWaitingHandler;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.server.LobbyWorldSender;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetGameRulePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbPasswordHandler
implements ServerGamePacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbPasswordHandler.class);
    private static final long KEEP_ALIVE_INTERVAL_MS = 15000L;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final GameProfile profile;
    private final RegistryAccess registryManager;
    private boolean passwordRequestSent;
    private boolean lobbyWorldSent;
    private boolean clientInPlayState;
    private long lastKeepAliveTime;

    public PbPasswordHandler(ProxyServer proxyServer, Connection connection, GameProfile gameProfile, RegistryAccess registryAccess) {
        this.proxyServer = proxyServer;
        this.connection = connection;
        this.profile = gameProfile;
        this.registryManager = registryAccess;
        this.lastKeepAliveTime = 0L;
        LOGGER.info("Password handler initialized for {}", (Object)gameProfile.name());
    }

    public void tick() {
        long l = System.currentTimeMillis();
        if (!this.lobbyWorldSent) {
            this.lobbyWorldSent = true;
            LobbyWorldSender.sendLobbyWorld(this.connection, this.registryManager);
        }
        if (l - this.lastKeepAliveTime >= 15000L) {
            this.lastKeepAliveTime = l;
            this.connection.send((Packet)new ClientboundKeepAlivePacket(l));
        }
        if (!this.passwordRequestSent && this.clientInPlayState) {
            this.passwordRequestSent = true;
            this.connection.send((Packet)new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.passwordRequest()));
            LOGGER.info("Sent password request to {}", (Object)this.profile.name());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket) {
        CustomPacketPayload customPacketPayload = serverboundCustomPayloadPacket.payload();
        if (!PbCustomPayload.CHANNEL.equals((Object)customPacketPayload.type().id())) {
            return;
        }
        if (!(customPacketPayload instanceof PbCustomPayload)) {
            return;
        }
        PbCustomPayload pbCustomPayload = (PbCustomPayload)customPacketPayload;
        FriendlyByteBuf friendlyByteBuf = pbCustomPayload.toBuf();
        try {
            int n = friendlyByteBuf.readVarInt();
            if (n == 2) {
                this.handlePassword(friendlyByteBuf.readUtf());
            } else {
                LOGGER.warn("Unexpected packet ID {} from {} during password verification", (Object)n, (Object)this.profile.name());
            }
        }
        finally {
            friendlyByteBuf.release();
        }
    }

    private void handlePassword(String string) {
        String string2 = Night.PINGBYPASS_CONFIG.getPassword();
        if (string2.equals(string)) {
            LOGGER.info("Password accepted for {}", (Object)this.profile.name());
            this.connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator((RegistryAccess)RegistryAccess.EMPTY), () -> false), new PbWaitingHandler(this.proxyServer, this.connection, this.profile, this.registryManager));
        } else {
            LOGGER.warn("Wrong password from {}", (Object)this.profile.name());
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(1L, 11L));
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            this.connection.disconnect((Component)Component.literal((String)"Wrong password"));
        }
    }

    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        LOGGER.info("Client {} disconnected during password verification", (Object)this.profile.name());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void handleAnimate(ServerboundSwingPacket serverboundSwingPacket) {
    }

    public void handleChat(ServerboundChatPacket serverboundChatPacket) {
    }

    public void handleChatCommand(ServerboundChatCommandPacket serverboundChatCommandPacket) {
    }

    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket serverboundChatCommandSignedPacket) {
    }

    public void handleChatAck(ServerboundChatAckPacket serverboundChatAckPacket) {
    }

    public void handleClientCommand(ServerboundClientCommandPacket serverboundClientCommandPacket) {
    }

    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundContainerButtonClickPacket) {
    }

    public void handleContainerClick(ServerboundContainerClickPacket serverboundContainerClickPacket) {
    }

    public void handlePlaceRecipe(ServerboundPlaceRecipePacket serverboundPlaceRecipePacket) {
    }

    public void handleContainerClose(ServerboundContainerClosePacket serverboundContainerClosePacket) {
    }

    public void handleAttack(ServerboundAttackPacket serverboundAttackPacket) {
    }

    public void handleInteract(ServerboundInteractPacket serverboundInteractPacket) {
    }

    public void handleSpectatorAction(ServerboundSpectatorActionPacket serverboundSpectatorActionPacket) {
    }

    public void handleMovePlayer(ServerboundMovePlayerPacket serverboundMovePlayerPacket) {
    }

    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket serverboundPlayerAbilitiesPacket) {
    }

    public void handlePlayerAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket) {
    }

    public void handlePlayerCommand(ServerboundPlayerCommandPacket serverboundPlayerCommandPacket) {
    }

    public void handlePlayerInput(ServerboundPlayerInputPacket serverboundPlayerInputPacket) {
    }

    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket serverboundSetCarriedItemPacket) {
    }

    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundSetCreativeModeSlotPacket) {
    }

    public void handleSignUpdate(ServerboundSignUpdatePacket serverboundSignUpdatePacket) {
    }

    public void handleUseItemOn(ServerboundUseItemOnPacket serverboundUseItemOnPacket) {
    }

    public void handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket) {
    }

    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket serverboundTeleportToEntityPacket) {
    }

    public void handlePaddleBoat(ServerboundPaddleBoatPacket serverboundPaddleBoatPacket) {
    }

    public void handleMoveVehicle(ServerboundMoveVehiclePacket serverboundMoveVehiclePacket) {
    }

    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket serverboundAcceptTeleportationPacket) {
    }

    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket serverboundPlayerLoadedPacket) {
    }

    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket serverboundRecipeBookSeenRecipePacket) {
    }

    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket serverboundSelectBundleItemPacket) {
    }

    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket serverboundRecipeBookChangeSettingsPacket) {
    }

    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket serverboundSeenAdvancementsPacket) {
    }

    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket serverboundCommandSuggestionPacket) {
    }

    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket serverboundSetCommandBlockPacket) {
    }

    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket serverboundSetCommandMinecartPacket) {
    }

    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket serverboundPickItemFromBlockPacket) {
    }

    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket serverboundPickItemFromEntityPacket) {
    }

    public void handleRenameItem(ServerboundRenameItemPacket serverboundRenameItemPacket) {
    }

    public void handleSetBeaconPacket(ServerboundSetBeaconPacket serverboundSetBeaconPacket) {
    }

    public void handleSetGameRule(ServerboundSetGameRulePacket serverboundSetGameRulePacket) {
    }

    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket serverboundSetStructureBlockPacket) {
    }

    public void handleSetTestBlock(ServerboundSetTestBlockPacket serverboundSetTestBlockPacket) {
    }

    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket serverboundTestInstanceBlockActionPacket) {
    }

    public void handleSelectTrade(ServerboundSelectTradePacket serverboundSelectTradePacket) {
    }

    public void handleEditBook(ServerboundEditBookPacket serverboundEditBookPacket) {
    }

    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket serverboundEntityTagQueryPacket) {
    }

    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket serverboundContainerSlotStateChangedPacket) {
    }

    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket serverboundBlockEntityTagQueryPacket) {
    }

    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket serverboundSetJigsawBlockPacket) {
    }

    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket serverboundJigsawGeneratePacket) {
    }

    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket serverboundChangeDifficultyPacket) {
    }

    public void handleChangeGameMode(ServerboundChangeGameModePacket serverboundChangeGameModePacket) {
    }

    public void handleLockDifficulty(ServerboundLockDifficultyPacket serverboundLockDifficultyPacket) {
    }

    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundChatSessionUpdatePacket) {
    }

    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket serverboundConfigurationAcknowledgedPacket) {
    }

    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket serverboundChunkBatchReceivedPacket) {
    }

    public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket serverboundDebugSubscriptionRequestPacket) {
    }

    public void handleClientTickEnd(ServerboundClientTickEndPacket serverboundClientTickEndPacket) {
    }

    public void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket) {
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket) {
        this.clientInPlayState = true;
    }

    public void handlePong(ServerboundPongPacket serverboundPongPacket) {
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket) {
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket serverboundCookieResponsePacket) {
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket serverboundCustomClickActionPacket) {
    }

    public void handlePingRequest(ServerboundPingRequestPacket serverboundPingRequestPacket) {
    }
}

