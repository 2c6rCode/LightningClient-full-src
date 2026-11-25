/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.ping.ClientboundPongResponsePacket
 *  net.minecraft.network.protocol.ping.ServerboundPingRequestPacket
 *  net.minecraft.network.protocol.status.ClientboundStatusResponsePacket
 *  net.minecraft.network.protocol.status.ServerStatus
 *  net.minecraft.network.protocol.status.ServerStatusPacketListener
 *  net.minecraft.network.protocol.status.ServerboundStatusRequestPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.handler;

import java.util.Optional;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbStatusHandler
implements ServerStatusPacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbStatusHandler.class);
    private final ProxyServer proxyServer;
    private final Connection connection;
    private boolean responseSent;

    public PbStatusHandler(ProxyServer proxyServer, Connection connection) {
        this.proxyServer = proxyServer;
        this.connection = connection;
    }

    public void handleStatusRequest(ServerboundStatusRequestPacket serverboundStatusRequestPacket) {
        if (this.responseSent) {
            this.connection.disconnect((Component)Component.literal((String)"Status already sent"));
            return;
        }
        this.responseSent = true;
        Component component = this.buildDescription();
        ServerStatus serverStatus = new ServerStatus(component, Optional.empty(), Optional.empty(), Optional.empty(), false);
        this.connection.send((Packet)new ClientboundStatusResponsePacket(serverStatus));
    }

    public void handlePingRequest(ServerboundPingRequestPacket serverboundPingRequestPacket) {
        this.connection.send((Packet)new ClientboundPongResponsePacket(serverboundPingRequestPacket.getTime()));
        this.connection.disconnect((Component)Component.literal((String)"Ping done"));
    }

    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    private Component buildDescription() {
        Minecraft minecraft = Minecraft.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\u00a7dLightning PingBypass\u00a7r\n");
        if (minecraft.getConnection() != null && minecraft.player != null && minecraft.level != null) {
            String serverBrand = minecraft.getConnection().serverBrand();
            stringBuilder.append("\u00a7aConnected\u00a7r");
            if (this.proxyServer.getServerConnection() != null) {
                stringBuilder.append(" \u2014 ").append(minecraft.getCurrentServer() != null ? minecraft.getCurrentServer().ip : "unknown");
            }
        } else {
            stringBuilder.append("\u00a77Idle\u00a7r \u2014 not connected to any server");
        }
        return Component.literal((String)stringBuilder.toString());
    }
}

