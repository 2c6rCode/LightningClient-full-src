/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.Connection
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.protocol.game.GameProtocols
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.server;

import com.mojang.authlib.GameProfile;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.handler.PbPlayHandler;
import lightning.pingbypass.server.S2CForwarder;
import lightning.pingbypass.server.WorldStateReplay;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.GameProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferRehook {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferRehook.class);

    public TransferRehook() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        Connection connection;
        Connection connection2;
        Minecraft minecraft;
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && PingBypassFlags.proxyForwardingActive && Night.PROXY_SERVER != null && (minecraft = Minecraft.getInstance()).getConnection() != null && minecraft.player != null && minecraft.level != null && (connection2 = minecraft.getConnection().getConnection()) != (connection = Night.PROXY_SERVER.getServerConnection()) && connection2.isConnected()) {
            LOGGER.warn("[PB] Proxy's own server connection changed underneath us (transfer/backend switch) -- rewiring {} connected client(s)", (Object)Night.PROXY_SERVER.getConnections().size());
            Night.PROXY_SERVER.setServerConnection(connection2);
            RegistryAccess registryAccess = minecraft.level.registryAccess();
            for (Connection connection3 : Night.PROXY_SERVER.getConnections()) {
                PacketListener packetListener;
                if (!connection3.isConnected() || !((packetListener = connection3.getPacketListener()) instanceof PbPlayHandler)) continue;
                PbPlayHandler pbPlayHandler = (PbPlayHandler)packetListener;
                GameProfile profile = pbPlayHandler.getProfile();
                try {
                    connection3.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator((RegistryAccess)registryAccess)));
                    S2CForwarder s2CForwarder = new S2CForwarder(connection3);
                    s2CForwarder.start();
                    int teleportId = WorldStateReplay.replay(connection3, registryAccess);
                    PbPlayHandler pbPlayHandler2 = new PbPlayHandler(Night.PROXY_SERVER, connection3, profile, s2CForwarder, teleportId);
                    connection3.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator((RegistryAccess)registryAccess), () -> false), pbPlayHandler2);
                    LOGGER.info("[PB] Rewired {} onto the new server connection after transfer", (Object)profile.name());
                }
                catch (Exception exception) {
                    LOGGER.error("[PB] Failed to rewire {} after transfer", (Object)profile.name(), (Object)exception);
                }
            }
        }
    }
}

