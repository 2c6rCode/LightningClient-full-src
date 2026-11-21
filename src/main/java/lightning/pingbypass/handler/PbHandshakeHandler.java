/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ConnectionProtocol
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.handshake.ClientIntentionPacket
 *  net.minecraft.network.protocol.handshake.ServerHandshakePacketListener
 *  net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket
 *  net.minecraft.network.protocol.login.LoginProtocols
 *  net.minecraft.network.protocol.status.StatusProtocols
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.handler;

import lightning.pingbypass.handler.PbLoginHandler;
import lightning.pingbypass.handler.PbStatusHandler;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbHandshakeHandler
implements ServerHandshakePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbHandshakeHandler.class);
    private static final int PROTOCOL_VERSION = 775;
    private final ProxyServer proxyServer;
    private final Connection connection;

    public PbHandshakeHandler(ProxyServer proxyServer, Connection connection) {
        this.proxyServer = proxyServer;
        this.connection = connection;
    }

    public void handleIntention(ClientIntentionPacket clientIntentionPacket) {
        switch (clientIntentionPacket.intention()) {
            case LOGIN: {
                this.handleLogin(clientIntentionPacket);
                break;
            }
            case STATUS: {
                this.handleStatus();
                break;
            }
            default: {
                throw new UnsupportedOperationException("Invalid intention " + String.valueOf(clientIntentionPacket.intention()));
            }
        }
    }

    private void handleLogin(ClientIntentionPacket clientIntentionPacket) {
        this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        if (clientIntentionPacket.protocolVersion() != 775) {
            MutableComponent mutableComponent = clientIntentionPacket.protocolVersion() < 775 ? Component.translatable((String)"multiplayer.disconnect.outdated_client", (Object[])new Object[]{"26.1.2"}) : Component.translatable((String)"multiplayer.disconnect.incompatible", (Object[])new Object[]{"26.1.2"});
            this.connection.send((Packet)new ClientboundLoginDisconnectPacket((Component)mutableComponent));
            this.connection.disconnect((Component)mutableComponent);
            LOGGER.info("Rejected connection from {}: wrong protocol version {}", (Object)this.connection.getLoggableAddress(false), (Object)clientIntentionPacket.protocolVersion());
        } else if (this.hasActivePlayConnection()) {
            MutableComponent mutableComponent = Component.literal((String)"This PingBypass server is already in use!");
            this.connection.send((Packet)new ClientboundLoginDisconnectPacket((Component)mutableComponent));
            this.connection.disconnect((Component)mutableComponent);
            LOGGER.info("Rejected connection from {}: another client is already connected", (Object)this.connection.getLoggableAddress(false));
        } else {
            this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new PbLoginHandler(this.proxyServer, this.connection));
            LOGGER.info("Client {} starting login", (Object)this.connection.getLoggableAddress(false));
        }
    }

    private void handleStatus() {
        this.connection.setupOutboundProtocol(StatusProtocols.CLIENTBOUND);
        this.connection.setupInboundProtocol(StatusProtocols.SERVERBOUND, new PbStatusHandler(this.proxyServer, this.connection));
        LOGGER.debug("Received STATUS handshake, serving proxy status");
    }

    private boolean hasActivePlayConnection() {
        for (Connection connection : this.proxyServer.getConnections()) {
            if (connection == this.connection || !connection.isConnected() || connection.getPacketListener() == null || connection.getPacketListener().protocol() != ConnectionProtocol.PLAY) continue;
            return true;
        }
        return false;
    }

    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}

