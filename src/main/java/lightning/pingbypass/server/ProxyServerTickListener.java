/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundClientInformationPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
 *  net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket
 *  net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.server;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.TickEvent;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerTickListener {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Lightning/Rubberband");
    private final ProxyServer proxyServer;
    private static final ThreadLocal<Boolean> ALLOW_SEND = ThreadLocal.withInitial(() -> false);

    public ProxyServerTickListener(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        Night.PB_MODULE_MANAGER.tick();
        if (this.proxyServer.isAlive()) {
            this.proxyServer.tick();
        }
    }

    public static void allowSend(Runnable runnable) {
        ALLOW_SEND.set(true);
        try {
            runnable.run();
        }
        finally {
            ALLOW_SEND.set(false);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket clientboundPlayerPositionPacket = (ClientboundPlayerPositionPacket)packet;
            LOGGER.info("[PB] Real server sent teleport/correction id={} pos={} rel={} onGround(from client, stale on ghost until next move)={}", new Object[]{clientboundPlayerPositionPacket.id(), clientboundPlayerPositionPacket.change().position(), clientboundPlayerPositionPacket.relatives(), PingBypassFlags.clientOnGround});
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        Packet<?> packet;
        Packet<?> packet2 = packetSendEvent.getPacket();
        if (packet2 instanceof ServerboundAcceptTeleportationPacket serverboundAcceptTeleportationPacket) {
            LOGGER.info("[PB] AcceptTeleportation id={} isRealClientConfirm(allowSend)={}", serverboundAcceptTeleportationPacket.getId(), ALLOW_SEND.get());
        }
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        if (ALLOW_SEND.get().booleanValue()) {
            return;
        }
        packet = packetSendEvent.getPacket();
        if (packet instanceof ServerboundClientTickEndPacket || packet instanceof ServerboundPlayerInputPacket || packet instanceof ServerboundMoveVehiclePacket || packet instanceof ServerboundPaddleBoatPacket || packet instanceof ServerboundClientInformationPacket || packet instanceof ServerboundPlayerAbilitiesPacket || packet instanceof ServerboundContainerClosePacket || packet instanceof ServerboundPlayerLoadedPacket || packet instanceof ServerboundMovePlayerPacket || packet instanceof ServerboundAcceptTeleportationPacket || packet instanceof ServerboundPlayerCommandPacket) {
            packetSendEvent.setCancelled(true);
        }
    }
}

