/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.RegistryAccess$Frozen
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.TickablePacketListener
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundClientInformationPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundPongPacket
 *  net.minecraft.network.protocol.common.ServerboundResourcePackPacket
 *  net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket
 *  net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks
 *  net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener
 *  net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket
 *  net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket
 *  net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks
 *  net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket
 *  net.minecraft.network.protocol.game.GameProtocols
 *  net.minecraft.network.protocol.game.GameProtocols$Context
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.util.List;
import lightning.Night;
import lightning.pingbypass.handler.PbPasswordHandler;
import lightning.pingbypass.handler.PbWaitingHandler;
import lightning.pingbypass.server.ProxyServer;
import lightning.pingbypass.server.RegistryCache;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbConfigurationHandler
implements ServerConfigurationPacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbConfigurationHandler.class);
    private static final GameProtocols.Context NO_INFINITE_MATERIALS = () -> false;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final GameProfile profile;
    private boolean knownPacksSent;

    public PbConfigurationHandler(ProxyServer proxyServer, Connection connection, GameProfile gameProfile) {
        this.proxyServer = proxyServer;
        this.connection = connection;
        this.profile = gameProfile;
    }

    public void tick() {
        if (!this.knownPacksSent) {
            this.knownPacksSent = true;
            RegistryCache registryCache = this.proxyServer.getRegistryCache();
            if (registryCache.isLoaded()) {
                this.connection.send((Packet)new ClientboundSelectKnownPacks(registryCache.getKnownPacks()));
            } else {
                this.connection.send((Packet)new ClientboundSelectKnownPacks(List.of()));
            }
            LOGGER.info("Sent SelectKnownPacks to {}", (Object)this.profile.name());
        }
    }

    public void handleSelectKnownPacks(ServerboundSelectKnownPacks serverboundSelectKnownPacks) {
        LOGGER.info("Client {} responded with known packs, sending registries", (Object)this.profile.name());
        RegistryCache registryCache = this.proxyServer.getRegistryCache();
        if (registryCache.isLoaded()) {
            for (Packet<?> packet : registryCache.getRegistryPackets()) {
                this.connection.send(packet);
            }
        }
        this.connection.send((Packet)ClientboundFinishConfigurationPacket.INSTANCE);
        LOGGER.info("Sent {} registry packets and FINISH_CONFIGURATION to {}", (Object)(registryCache.isLoaded() ? registryCache.getRegistryPackets().size() : 0), (Object)this.profile.name());
    }

    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket serverboundFinishConfigurationPacket) {
        LOGGER.info("Client {} acknowledged FINISH_CONFIGURATION, transitioning to PLAY", (Object)this.profile.name());
        RegistryCache registryCache = this.proxyServer.getRegistryCache();
        RegistryAccess registryAccess = registryCache.isLoaded() ? registryCache.getRegistryManager() : RegistryAccess.EMPTY;
        this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(registryAccess)));
        if (Night.PINGBYPASS_CONFIG.hasPassword()) {
            this.connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), NO_INFINITE_MATERIALS), new PbPasswordHandler(this.proxyServer, this.connection, this.profile, registryAccess));
            LOGGER.info("Transitioning {} to password verification", (Object)this.profile.name());
        } else {
            this.connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), NO_INFINITE_MATERIALS), new PbWaitingHandler(this.proxyServer, this.connection, this.profile, registryAccess));
            LOGGER.info("Transitioning {} to waiting state", (Object)this.profile.name());
        }
    }

    public void handleAcceptCodeOfConduct(ServerboundAcceptCodeOfConductPacket serverboundAcceptCodeOfConductPacket) {
    }

    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        LOGGER.info("Client {} disconnected during configuration: {}", (Object)this.profile.name(), (Object)disconnectionDetails.reason());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket) {
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket) {
    }

    public void handlePong(ServerboundPongPacket serverboundPongPacket) {
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket) {
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket serverboundCookieResponsePacket) {
    }

    public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket) {
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket serverboundCustomClickActionPacket) {
    }
}

