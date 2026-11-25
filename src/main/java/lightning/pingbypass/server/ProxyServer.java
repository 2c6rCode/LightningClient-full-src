/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  io.netty.bootstrap.ServerBootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.EventLoopGroup
 *  io.netty.channel.epoll.Epoll
 *  io.netty.channel.epoll.EpollEventLoopGroup
 *  io.netty.channel.epoll.EpollServerSocketChannel
 *  io.netty.channel.nio.NioEventLoopGroup
 *  io.netty.channel.socket.nio.NioServerSocketChannel
 *  io.netty.handler.timeout.ReadTimeoutHandler
 *  net.minecraft.network.Connection
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.PacketFlow
 *  net.minecraft.network.protocol.common.ClientboundDisconnectPacket
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.server;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lightning.pingbypass.PingBypassConfig;
import lightning.pingbypass.handler.PbHandshakeHandler;
import lightning.pingbypass.protocol.PbProtocolHandler;
import lightning.pingbypass.server.PbSession;
import lightning.pingbypass.server.RegistryCache;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServer.class);
    private static final Lazy<NioEventLoopGroup> SERVER_NIO_EVENTLOOP = new Lazy<NioEventLoopGroup>(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("PingBypass Server IO #%d").setDaemon(true).build()));
    private static final Lazy<EpollEventLoopGroup> SERVER_EPOLL_EVENTLOOP = new Lazy<EpollEventLoopGroup>(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("PingBypass Epoll Server IO #%d").setDaemon(true).build()));
    private final List<ChannelFuture> endpoints = Collections.synchronizedList(Lists.newArrayList());
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());
    private final PingBypassConfig config;
    private final PbProtocolHandler protocolHandler;
    private volatile boolean alive;
    private volatile boolean stayConnected;
    private volatile Connection serverConnection;
    private final RegistryCache registryCache;

    public ProxyServer(PingBypassConfig pingBypassConfig) {
        this.config = pingBypassConfig;
        this.alive = true;
        this.protocolHandler = new PbProtocolHandler();
        this.registryCache = new RegistryCache();
        this.protocolHandler.registerStayHandler(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void bind(InetAddress address, int port) throws IOException {
        LOGGER.info("PingBypass proxy binding to {}:{}", (Object)address, (Object)port);
        synchronized (this.endpoints) {
            EventLoopGroup eventLoopGroup;
            Class<? extends ServerChannel> clazz;
            if (Epoll.isAvailable()) {
                clazz = EpollServerSocketChannel.class;
                eventLoopGroup = (EventLoopGroup)SERVER_EPOLL_EVENTLOOP.get();
                LOGGER.info("Using epoll channel type");
            } else {
                clazz = NioServerSocketChannel.class;
                eventLoopGroup = (EventLoopGroup)SERVER_NIO_EVENTLOOP.get();
                LOGGER.info("Using default NIO channel type");
            }
            this.endpoints.add(((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(clazz)).childHandler((ChannelHandler)new ChannelInitializer<Channel>(){

                protected void initChannel(Channel channel) {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                    }
                    catch (ChannelException channelException) {
                        // empty catch block
                    }
                    ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                    Connection.configureSerialization((ChannelPipeline)channelPipeline, (PacketFlow)PacketFlow.SERVERBOUND, (boolean)false, null);
                    PbSession pbSession = new PbSession();
                    ProxyServer.this.connections.add(pbSession);
                    pbSession.configurePacketHandler(channelPipeline);
                    pbSession.setListenerForServerboundHandshake((PacketListener)new PbHandshakeHandler(ProxyServer.this, pbSession));
                }
            }).group(eventLoopGroup).localAddress(address, port)).bind().syncUninterruptibly());
            LOGGER.info("PingBypass proxy is now listening on {}:{}", (Object)address, (Object)port);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        synchronized (this.connections) {
            Iterator<Connection> iterator = this.connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                if (!connection.isConnected()) {
                    iterator.remove();
                    connection.handleDisconnection();
                    continue;
                }
                try {
                    connection.tick();
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to handle packet for {}", (Object)connection.getLoggableAddress(false), (Object)exception);
                    connection.send((Packet)new ClientboundDisconnectPacket((Component)Component.literal((String)"Internal server error")));
                    connection.disconnect((Component)Component.literal((String)"Internal server error"));
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void shutdown() {
        this.alive = false;
        LOGGER.info("Shutting down PingBypass proxy server...");
        for (ChannelFuture channelFuture : this.endpoints) {
            try {
                channelFuture.channel().close().sync();
            }
            catch (InterruptedException interruptedException) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
        this.endpoints.clear();
        synchronized (this.connections) {
            for (Connection connection : this.connections) {
                connection.disconnect((Component)Component.literal((String)"Proxy server shutting down"));
            }
            this.connections.clear();
        }
        LOGGER.info("PingBypass proxy server shut down.");
    }

    public boolean isAlive() {
        return this.alive;
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    public List<ChannelFuture> getEndpoints() {
        return this.endpoints;
    }

    public PingBypassConfig getConfig() {
        return this.config;
    }

    public PbProtocolHandler getProtocolHandler() {
        return this.protocolHandler;
    }

    public boolean isStayConnected() {
        return this.stayConnected;
    }

    public void setStayConnected(boolean stayConnected) {
        this.stayConnected = stayConnected;
    }

    public Connection getServerConnection() {
        return this.serverConnection;
    }

    public void setServerConnection(Connection connection) {
        this.serverConnection = connection;
    }

    public RegistryCache getRegistryCache() {
        return this.registryCache;
    }

    private static class Lazy<T> {
        private final Supplier<T> supplier;
        private volatile T value;

        Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        T get() {
            if (this.value == null) {
                Lazy lazy;
                Lazy lazy2 = lazy = this;
                synchronized (lazy2) {
                    if (this.value == null) {
                        this.value = this.supplier.get();
                    }
                }
            }
            return this.value;
        }
    }
}

