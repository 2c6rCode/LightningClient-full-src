/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 *  net.minecraft.network.FriendlyByteBuf
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lightning.pingbypass.protocol.PbPacket;
import lightning.pingbypass.protocol.PbPacketHandler;
import lightning.pingbypass.protocol.packets.C2SJoinPacket;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.pingbypass.protocol.packets.C2SPasswordPacket;
import lightning.pingbypass.protocol.packets.C2SSettingChangePacket;
import lightning.pingbypass.protocol.packets.C2SStayPacket;
import lightning.pingbypass.protocol.packets.S2CErrorPacket;
import lightning.pingbypass.protocol.packets.S2CModuleStatePacket;
import lightning.pingbypass.protocol.packets.S2CPasswordRequestPacket;
import lightning.pingbypass.protocol.packets.S2CServerNamePacket;
import lightning.pingbypass.protocol.packets.S2CSettingStatePacket;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbProtocolHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbProtocolHandler.class);
    private final Map<Integer, Function<FriendlyByteBuf, ? extends PbPacket>> factories = new HashMap<Integer, Function<FriendlyByteBuf, ? extends PbPacket>>();
    private final Map<Integer, PbPacketHandler<?>> handlers = new HashMap<Integer, PbPacketHandler<?>>();

    public PbProtocolHandler() {
        this.registerFactories();
    }

    public <T extends PbPacket> void register(int n, Function<FriendlyByteBuf, T> function, PbPacketHandler<T> pbPacketHandler) {
        this.factories.put(n, function);
        this.handlers.put(n, pbPacketHandler);
    }

    public <T extends PbPacket> void registerFactory(int n, Function<FriendlyByteBuf, T> function) {
        this.factories.put(n, function);
    }

    public <T extends PbPacket> void registerHandler(int n, PbPacketHandler<T> pbPacketHandler) {
        this.handlers.put(n, pbPacketHandler);
    }

    public void handle(FriendlyByteBuf friendlyByteBuf, Connection connection) {
        int n;
        try {
            n = friendlyByteBuf.readVarInt();
        }
        catch (Exception exception) {
            LOGGER.warn("[PbProtocol] Failed to read packet ID from buffer", (Throwable)exception);
            return;
        }
        Function<FriendlyByteBuf, ? extends PbPacket> function = this.factories.get(n);
        if (function == null) {
            LOGGER.warn("[PbProtocol] Unknown packet ID: {}", (Object)n);
        } else {
            PbPacket pbPacket;
            try {
                pbPacket = function.apply(friendlyByteBuf);
            }
            catch (Exception exception) {
                LOGGER.warn("[PbProtocol] Failed to deserialize packet ID {}", (Object)n, (Object)exception);
                return;
            }
            PbPacketHandler<?> pbPacketHandler = this.handlers.get(n);
            if (pbPacketHandler == null) {
                LOGGER.warn("[PbProtocol] No handler registered for packet ID: {}", (Object)n);
            } else {
                try {
                    handlePacket(pbPacketHandler, pbPacket, connection);
                }
                catch (Exception exception) {
                    LOGGER.warn("[PbProtocol] Error handling packet ID {}", (Object)n, (Object)exception);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void handlePacket(PbPacketHandler handler, PbPacket packet, Connection connection) {
        handler.handle(packet, connection);
    }

    private void registerFactories() {
        this.registerFactory(0, C2SJoinPacket::new);
        this.registerFactory(1, S2CPasswordRequestPacket::new);
        this.registerFactory(2, C2SPasswordPacket::new);
        this.registerFactory(3, C2SModuleTogglePacket::new);
        this.registerFactory(4, C2SSettingChangePacket::new);
        this.registerFactory(5, S2CModuleStatePacket::new);
        this.registerFactory(6, S2CSettingStatePacket::new);
        this.registerFactory(7, S2CErrorPacket::new);
        this.registerFactory(8, S2CServerNamePacket::new);
        this.registerFactory(9, C2SStayPacket::new);
    }

    public void registerStayHandler(ProxyServer proxyServer) {
        this.register(9, C2SStayPacket::new, (c2SStayPacket, connection) -> {
            proxyServer.setStayConnected(c2SStayPacket.isStay());
            LOGGER.info("[PbProtocol] Stay Connected set to {} via protocol handler", (Object)c2SStayPacket.isStay());
        });
    }
}

