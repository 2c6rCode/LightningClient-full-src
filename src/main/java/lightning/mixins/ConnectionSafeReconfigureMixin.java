/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelPipeline
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ConnectionProtocol
 *  net.minecraft.network.PacketBundlePacker
 *  net.minecraft.network.PacketBundleUnpacker
 *  net.minecraft.network.PacketDecoder
 *  net.minecraft.network.PacketEncoder
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.ProtocolInfo
 *  net.minecraft.network.protocol.BundlerInfo
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lightning.mixins.accessors.ConnectionAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.BundlerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
public class ConnectionSafeReconfigureMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Lightning/SafeReconfigure");

    @Inject(method={"setupOutboundProtocol"}, at={@At(value="HEAD")}, cancellable=true)
    private void setupOutboundProtocol$safe(ProtocolInfo<?> protocolInfo, CallbackInfo callbackInfo) {
        Connection connection = (Connection)(Object)this;
        if (protocolInfo.flow() != connection.getSending()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf(protocolInfo.id()));
        }
        ConnectionAccessor connectionAccessor = (ConnectionAccessor)((Object)this);
        Channel channel = connectionAccessor.getChannel();
        connectionAccessor.setSendLoginDisconnect(protocolInfo.id() == ConnectionProtocol.LOGIN);
        Runnable runnable = () -> {
            try {
                ChannelPipeline channelPipeline = channel.pipeline();
                if (channelPipeline.get("fabric:splitter") != null) {
                    channelPipeline.remove("fabric:splitter");
                }
                PacketEncoder packetEncoder = new PacketEncoder(protocolInfo);
                if (channelPipeline.get("encoder") != null) {
                    channelPipeline.replace("encoder", "encoder", (ChannelHandler)packetEncoder);
                } else if (channelPipeline.get("outbound_config") != null) {
                    channelPipeline.replace("outbound_config", "encoder", (ChannelHandler)packetEncoder);
                } else {
                    channelPipeline.addAfter("prepender", "encoder", (ChannelHandler)packetEncoder);
                }
                BundlerInfo bundlerInfo = protocolInfo.bundlerInfo();
                if (bundlerInfo != null) {
                    PacketBundleUnpacker packetBundleUnpacker = new PacketBundleUnpacker(bundlerInfo);
                    if (channelPipeline.get("unbundler") != null) {
                        channelPipeline.replace("unbundler", "unbundler", (ChannelHandler)packetBundleUnpacker);
                    } else {
                        channelPipeline.addAfter("encoder", "unbundler", (ChannelHandler)packetBundleUnpacker);
                    }
                }
            }
            catch (Exception exception) {
                LOGGER.error("[PB] Failed safe outbound transition to {}", (Object)protocolInfo.id(), (Object)exception);
            }
        };
        if (channel.eventLoop().inEventLoop()) {
            runnable.run();
        } else {
            try {
                channel.eventLoop().submit(runnable).sync();
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        callbackInfo.cancel();
    }

    @Inject(method={"setupInboundProtocol"}, at={@At(value="HEAD")}, cancellable=true)
    private <T extends PacketListener> void setupInboundProtocol$safe(ProtocolInfo<T> protocolInfo, T packetListener, CallbackInfo callbackInfo) {
        Connection connection = (Connection)(Object)this;
        if (protocolInfo.flow() != connection.getReceiving()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf(protocolInfo.id()));
        }
        ConnectionAccessor connectionAccessor = (ConnectionAccessor)((Object)this);
        connectionAccessor.setPacketListener(packetListener);
        Channel channel = connectionAccessor.getChannel();
        Runnable runnable = () -> {
            try {
                ChannelPipeline channelPipeline = channel.pipeline();
                if (channelPipeline.get("fabric:merger") != null) {
                    channelPipeline.remove("fabric:merger");
                }
                PacketDecoder packetDecoder = new PacketDecoder(protocolInfo);
                if (channelPipeline.get("decoder") != null) {
                    channelPipeline.replace("decoder", "decoder", (ChannelHandler)packetDecoder);
                } else if (channelPipeline.get("inbound_config") != null) {
                    channelPipeline.replace("inbound_config", "decoder", (ChannelHandler)packetDecoder);
                }
                channel.config().setAutoRead(true);
                BundlerInfo bundlerInfo = protocolInfo.bundlerInfo();
                if (bundlerInfo != null) {
                    PacketBundlePacker packetBundlePacker = new PacketBundlePacker(bundlerInfo);
                    if (channelPipeline.get("bundler") != null) {
                        channelPipeline.replace("bundler", "bundler", (ChannelHandler)packetBundlePacker);
                    } else {
                        channelPipeline.addAfter("decoder", "bundler", (ChannelHandler)packetBundlePacker);
                    }
                }
            }
            catch (Exception exception) {
                LOGGER.error("[PB] Failed safe inbound transition to {}", (Object)protocolInfo.id(), (Object)exception);
            }
        };
        if (channel.eventLoop().inEventLoop()) {
            runnable.run();
        } else {
            try {
                channel.eventLoop().submit(runnable).sync();
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        callbackInfo.cancel();
    }
}

