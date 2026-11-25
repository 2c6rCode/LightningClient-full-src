/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair
 *  lombok.Generated
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTimePacket
 */
package lightning.managers;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.Arrays;
import java.util.UUID;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerConnectEvent;
import lightning.events.impl.PlayerDisconnectEvent;
import lightning.events.impl.ServerConnectEvent;
import lightning.modules.impl.miscellaneous.FastLatencyModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.utils.IMinecraft;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class ServerManager
implements IMinecraft {
    private final Timer setbackTimer = new Timer();
    private final Timer responseTimer = new Timer();
    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long lastUpdate = -1L;
    private long timeJoined;
    private Pair<ServerAddress, ServerData> lastConnection;

    public ServerManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        this.responseTimer.reset();
        if (packetReceiveEvent.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.setbackTimer.reset();
        }
        if (packetReceiveEvent.getPacket() instanceof ClientboundSetTimePacket) {
            this.tickRates[this.nextIndex] = Math.clamp(20.0f / ((float)(System.currentTimeMillis() - this.lastUpdate) / 1000.0f), 0.0f, 20.0f);
            this.nextIndex = (this.nextIndex + 1) % this.tickRates.length;
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent clientConnectEvent) {
        Arrays.fill(this.tickRates, 0.0f);
        this.nextIndex = 0;
        this.timeJoined = System.currentTimeMillis();
        this.lastUpdate = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void handleConnections(PacketReceiveEvent packetReceiveEvent) {
        block5: {
            Packet<?> packet;
            block4: {
                if (ServerManager.mc.level == null) {
                    return;
                }
                packet = packetReceiveEvent.getPacket();
                if (!(packet instanceof ClientboundPlayerInfoUpdatePacket)) break block4;
                ClientboundPlayerInfoUpdatePacket clientboundPlayerInfoUpdatePacket = (ClientboundPlayerInfoUpdatePacket)packet;
                if (!clientboundPlayerInfoUpdatePacket.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) break block5;
                for (ClientboundPlayerInfoUpdatePacket.Entry entry : clientboundPlayerInfoUpdatePacket.newEntries()) {
                    Night.EVENT_HANDLER.post(new PlayerConnectEvent(entry.profile().id()));
                }
                break block5;
            }
            packet = packetReceiveEvent.getPacket();
            if (packet instanceof ClientboundPlayerInfoRemovePacket) {
                ClientboundPlayerInfoRemovePacket clientboundPlayerInfoRemovePacket = (ClientboundPlayerInfoRemovePacket)packet;
                for (UUID uUID : clientboundPlayerInfoRemovePacket.profileIds()) {
                    Night.EVENT_HANDLER.post(new PlayerDisconnectEvent(uUID));
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent serverConnectEvent) {
        this.lastConnection = new ObjectObjectImmutablePair((Object)serverConnectEvent.getAddress(), (Object)serverConnectEvent.getInfo());
    }

    public float getTickRate() {
        if (ServerManager.mc.player == null) {
            return 0.0f;
        }
        if (System.currentTimeMillis() - this.timeJoined < 4000L) {
            return 20.0f;
        }
        int sampleCount = 0;
        float tickRateSum = 0.0f;
        for (float rate : this.tickRates) {
            if (!(rate > 0.0f)) continue;
            tickRateSum += rate;
            ++sampleCount;
        }
        return tickRateSum / (float)sampleCount;
    }

    public int getPingDelay() {
        return (int)((float)this.getPing() / 25.0f);
    }

    public int getPing() {
        if (Night.MODULE_MANAGER.getModule(FastLatencyModule.class).isToggled()) {
            return Night.MODULE_MANAGER.getModule(FastLatencyModule.class).getLatency();
        }
        if (mc.getConnection() == null || ServerManager.mc.player == null) {
            return 0;
        }
        PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(ServerManager.mc.player.getUUID());
        if (playerInfo != null) {
            return playerInfo.getLatency();
        }
        if (PingBypassFlags.proxyForwardingActive) {
            for (PlayerInfo playerInfo2 : mc.getConnection().getOnlinePlayers()) {
                if (!playerInfo2.getProfile().name().equals(ServerManager.mc.player.getName().getString())) continue;
                return playerInfo2.getLatency();
            }
            for (PlayerInfo playerInfo2 : mc.getConnection().getOnlinePlayers()) {
                if (playerInfo2.getLatency() <= 0) continue;
                return playerInfo2.getLatency();
            }
        }
        return 0;
    }

    public String getServerBrand() {
        if (mc.getCurrentServer() == null || mc.getConnection() == null || mc.getConnection().serverBrand() == null) {
            return "Vanilla";
        }
        return mc.getConnection().serverBrand();
    }

    public String getServer() {
        return mc.hasSingleplayerServer() ? "Singleplayer" : ServerAddress.parseString((String)ServerManager.mc.getCurrentServer().ip).getHost();
    }

    @Generated
    public Timer getSetbackTimer() {
        return this.setbackTimer;
    }

    @Generated
    public Timer getResponseTimer() {
        return this.responseTimer;
    }

    @Generated
    public float[] getTickRates() {
        return this.tickRates;
    }

    @Generated
    public int getNextIndex() {
        return this.nextIndex;
    }

    @Generated
    public long getLastUpdate() {
        return this.lastUpdate;
    }

    @Generated
    public long getTimeJoined() {
        return this.timeJoined;
    }

    @Generated
    public Pair<ServerAddress, ServerData> getLastConnection() {
        return this.lastConnection;
    }
}

