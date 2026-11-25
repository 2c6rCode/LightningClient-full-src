/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandlerContext
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.ClientDisconnectEvent
 *  lightning.events.impl.PacketReceiveEvent
 *  lightning.events.impl.PacketSendEvent
 *  lightning.events.impl.PacketSendEvent$Post
 *  lightning.modules.impl.miscellaneous.FakePlayerModule
 *  lightning.modules.impl.miscellaneous.NoPacketKickModule
 *  lightning.pingbypass.ProtocolTransitionTracker
 *  lightning.utils.NightBeacon
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ConnectionProtocol
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundBundlePacket
 *  net.minecraft.network.protocol.handshake.ClientIntentionPacket
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.miscellaneous.NoPacketKickModule;
import lightning.pingbypass.ProtocolTransitionTracker;
import lightning.utils.NightBeacon;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
public class ClientConnectionMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Lightning/Disconnect");
    private static final long PROTOCOL_TRANSITION_WINDOW_MS = 1000L;

    @Inject(method={"send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void send$HEAD(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo callbackInfo) {
        try {
            NightBeacon.pre((Object)this, packet, (Object)channelFutureListener, (boolean)flush, (Object)callbackInfo);
            if (packet instanceof ClientIntentionPacket) {
                Connection connection = (Connection)(Object)this;
                PacketListener packetListener = connection.getPacketListener();
                boolean pastHandshake = packetListener != null && packetListener.protocol() != ConnectionProtocol.HANDSHAKING;
                boolean unusedPastHandshake = pastHandshake;
                if (pastHandshake) {
                    LOGGER.error("[PB] Caught an outgoing ClientIntentionPacket on {} -- this connection is already past handshake, this send will crash it", (Object)connection.getLoggableAddress(false), (Object)new Throwable("call site"));
                }
            }
            PacketSendEvent packetSendEvent = new PacketSendEvent(packet);
            Night.EVENT_HANDLER.post((Event)packetSendEvent);
            if (packetSendEvent.isCancelled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"}, at={@At(value="TAIL")}, cancellable=true)
    private void send$TAIL(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new PacketSendEvent.Post(packet));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"doSendPacket"}, at={@At(value="HEAD")}, cancellable=true)
    private void doSendPacket$HEAD(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo callbackInfo) {
        try {
            boolean inTransitionWindow = System.currentTimeMillis() - ProtocolTransitionTracker.lastTransitionAt() < 1000L;
            String packageName = packet.getClass().getPackageName();
            boolean isGameOrHandshake = packageName.equals("net.minecraft.network.protocol.game") || packageName.equals("net.minecraft.network.protocol.handshake");
            boolean unusedIsGameOrHandshake = isGameOrHandshake;
            if (inTransitionWindow && isGameOrHandshake) {
                LOGGER.warn("[PB] Dropped {} queued right as the connection was mid protocol-transition -- would have crashed the session", (Object)packet.getClass().getSimpleName());
                callbackInfo.cancel();
                return;
            }
            if (packet instanceof ClientIntentionPacket) {
                Connection connection = (Connection)(Object)this;
                PacketListener packetListener = connection.getPacketListener();
                boolean pastHandshake = packetListener != null && packetListener.protocol() != ConnectionProtocol.HANDSHAKING;
                boolean unusedPastHandshake = pastHandshake;
                if (pastHandshake) {
                    return;
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callbackInfo) {
        try {
            PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(packet, (Connection)(Object)this);
            Night.EVENT_HANDLER.post((Event)packetReceiveEvent);
            if (packet instanceof ClientboundBundlePacket) {
                ClientboundBundlePacket clientboundBundlePacket = (ClientboundBundlePacket)packet;
                for (Packet packet2 : clientboundBundlePacket.subPackets()) {
                    Night.EVENT_HANDLER.post((Event)new PacketReceiveEvent(packet2, (Connection)(Object)this));
                }
            }
            if (packetReceiveEvent.isCancelled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"disconnect(Lnet/minecraft/network/DisconnectionDetails;)V"}, at={@At(value="HEAD")})
    private void disconnect(DisconnectionDetails disconnectionDetails, CallbackInfo callbackInfo) {
        try {
            Connection connection = (Connection)(Object)this;
            String reason = disconnectionDetails.reason() != null ? disconnectionDetails.reason().getString() : "unknown";
            boolean isProxyServerConnection = Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && Night.PROXY_SERVER != null && connection == Night.PROXY_SERVER.getServerConnection();
            boolean unusedIsProxyServerConnection = isProxyServerConnection;
            if (isProxyServerConnection) {
                LOGGER.warn("[PB] Proxy's connection to the real server dropped: {}", (Object)reason);
                FakePlayerModule fakePlayerModule = (FakePlayerModule)Night.MODULE_MANAGER.getModule(FakePlayerModule.class);
                if (fakePlayerModule != null && fakePlayerModule.isToggled()) {
                    fakePlayerModule.setToggled(false);
                }
            } else {
                LOGGER.info("Connection disconnected ({}): {}", (Object)connection.getLoggableAddress(false), (Object)reason);
            }
            Night.EVENT_HANDLER.post((Event)new ClientDisconnectEvent());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"exceptionCaught"}, at={@At(value="HEAD")}, cancellable=true)
    private void exceptionCaught$HEAD(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo callbackInfo) {
        try {
            NoPacketKickModule noPacketKickModule = Night.MODULE_MANAGER != null ? (NoPacketKickModule)Night.MODULE_MANAGER.getModule(NoPacketKickModule.class) : null;
            NoPacketKickModule noPacketKickModule2 = noPacketKickModule;
            if (noPacketKickModule != null && noPacketKickModule.isToggled() && noPacketKickModule.shouldSuppress(throwable)) {
                noPacketKickModule.onExceptionCaught(throwable);
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

