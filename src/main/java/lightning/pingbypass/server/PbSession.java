/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelPipeline
 *  lightning.mixins.accessors.ConnectionAccessor
 *  net.minecraft.network.Connection
 *  net.minecraft.network.ConnectionProtocol
 *  net.minecraft.network.PacketBundlePacker
 *  net.minecraft.network.PacketBundleUnpacker
 *  net.minecraft.network.PacketDecoder
 *  net.minecraft.network.PacketEncoder
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.ProtocolInfo
 *  net.minecraft.network.protocol.BundlerInfo
 *  net.minecraft.network.protocol.PacketFlow
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.server;

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
import net.minecraft.network.protocol.PacketFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbSession
extends Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbSession.class);

    public PbSession() {
        super(PacketFlow.SERVERBOUND);
    }

    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocolInfo, T t) {
        if (protocolInfo.flow() != this.getReceiving()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf(protocolInfo.id()));
        }
        ConnectionAccessor connectionAccessor = (ConnectionAccessor)this;
        connectionAccessor.setPacketListener(t);
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
                LOGGER.error("[PbSession] Failed inbound transition to {}", (Object)protocolInfo.id(), (Object)exception);
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
    }

    public void setupOutboundProtocol(ProtocolInfo<?> protocolInfo) {
        if (protocolInfo.flow() != this.getSending()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf(protocolInfo.id()));
        }
        ConnectionAccessor connectionAccessor = (ConnectionAccessor)this;
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
                LOGGER.error("[PbSession] Failed outbound transition to {}", (Object)protocolInfo.id(), (Object)exception);
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
    }
}

