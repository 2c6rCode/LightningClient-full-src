/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ConnectScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.ServerData$Type
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  net.minecraft.network.Connection
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Player
 */
package lightning.modules.impl.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.events.impl.TickEvent;
import lightning.gui.ClickGuiScreen;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.input.ClientInputForwarder;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.PbPacket;
import lightning.pingbypass.protocol.packets.C2SFriendSyncPacket;
import lightning.pingbypass.protocol.packets.C2SJoinPacket;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.pingbypass.protocol.packets.C2SPasswordPacket;
import lightning.pingbypass.protocol.packets.C2SSettingChangePacket;
import lightning.pingbypass.protocol.packets.S2CBlockRenderPacket;
import lightning.pingbypass.protocol.packets.S2CErrorPacket;
import lightning.pingbypass.protocol.packets.S2CMiningStatePacket;
import lightning.pingbypass.protocol.packets.S2CModuleStatePacket;
import lightning.pingbypass.protocol.packets.S2CRenderPositionPacket;
import lightning.pingbypass.protocol.packets.S2CServerNamePacket;
import lightning.pingbypass.protocol.packets.S2CSettingStatePacket;
import lightning.pingbypass.protocol.packets.S2CSlotSyncPacket;
import lightning.pingbypass.protocol.packets.S2CWindowClickPacket;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.miscellaneous.RenderPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

@RegisterModule(name="PingBypass", description="Connects to a PingBypass proxy server for low-latency combat.", category=Module.Category.CORE)
public class PingBypassModule
extends Module {
    public final StringSetting ip = new StringSetting("IP", "The IP address of the PingBypass proxy server.", "127.0.0.1");
    public final NumberSetting port = new NumberSetting("Port", "The port of the PingBypass proxy server.", 25565, 1, 65535);
    public final StringSetting password = new StringSetting("Password", "The password for the PingBypass proxy server.", "");
    public final StringSetting server = new StringSetting("Server", "The target Minecraft server to join through the proxy (e.g. mc.hypixel.net or mc.hypixel.net:25565).", "");
    private volatile boolean joinSent;
    public volatile int proxyPing;
    private volatile String serverName;
    private final Map<String, Boolean> proxyModuleStates = new ConcurrentHashMap<String, Boolean>();
    private final Map<String, String> proxySettingValues = new ConcurrentHashMap<String, String>();
    private volatile Connection proxyConnection;
    private final List<PendingPayload> pendingPayloads = Collections.synchronizedList(new ArrayList());
    private int pendingDelay = 10;
    private ClientInputForwarder inputForwarder;

    @Override
    public void onEnable() {
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            Night.CHAT_MANAGER.error("Cannot enable PingBypass on a PingBypass server!");
            this.setToggled(false, false);
            return;
        }
        if (PingBypassModule.mc.gui.overlay() != null) {
            Night.LOGGER.info("[PingBypass] Skipping auto-connect during startup");
            return;
        }
        if (Night.CONFIG_MANAGER != null && Night.CONFIG_MANAGER.isLoadingConfig()) {
            Night.LOGGER.info("[PingBypass] Skipping auto-connect during config load");
            return;
        }
        if (PingBypassModule.mc.gui.screen() instanceof ClickGuiScreen) {
            PingBypassModule.mc.gui.setScreen(null);
        }
        this.proxyModuleStates.clear();
        this.proxySettingValues.clear();
        this.serverName = null;
        this.joinSent = false;
        if (PingBypassModule.mc.level != null) {
            PingBypassModule.mc.level.disconnect((Component)Component.literal((String)"PingBypass enabled."));
        }
        if (mc.getConnection() != null) {
            mc.getConnection().getConnection().disconnect((Component)Component.literal((String)"PingBypass enabled."));
        }
        String host = this.ip.getValue();
        int proxyPort = this.port.getValue().intValue();
        ServerAddress serverAddress = new ServerAddress(host, proxyPort);
        ServerData serverData = new ServerData("PingBypass Proxy", serverAddress.getHost() + ":" + serverAddress.getPort(), ServerData.Type.OTHER);
        ConnectScreen.startConnecting((Screen)new TitleScreen(), (Minecraft)mc, (ServerAddress)serverAddress, (ServerData)serverData, (boolean)false, null);
    }

    @Override
    public void onDisable() {
        if (PingBypassModule.mc.level != null) {
            PingBypassModule.mc.level.disconnect((Component)Component.literal((String)"PingBypass disabled."));
        }
        if (mc.getConnection() != null) {
            mc.getConnection().getConnection().disconnect((Component)Component.literal((String)"PingBypass disabled."));
        }
        this.proxyPing = 0;
        this.serverName = null;
        this.proxyConnection = null;
        PingBypassFlags.proxyForwardingActive = false;
        if (this.inputForwarder != null) {
            this.inputForwarder.stop();
            this.inputForwarder = null;
        }
        this.proxyModuleStates.clear();
        this.proxySettingValues.clear();
    }

    @Override
    public String getMetaData() {
        return this.proxyPing + "ms";
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        if (this.isToggled() && PingBypassFlags.proxyForwardingActive) {
            mc.execute(() -> this.setToggled(false, false));
        }
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent settingChangeEvent) {
        if (!this.isToggled() || !PingBypassFlags.proxyForwardingActive) {
            return;
        }
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        Setting setting = settingChangeEvent.getSetting();
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (!module.isProxyEnhanced() || !module.getSettings().contains(setting)) continue;
            String serialized = this.serializeSetting(setting);
            if (serialized == null) {
                return;
            }
            Connection connection = this.proxyConnection;
            if (connection == null && mc.getConnection() != null) {
                connection = mc.getConnection().getConnection();
            }
            if (connection != null && connection.isConnected()) {
                connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new C2SSettingChangePacket(module.getName(), setting.getName(), serialized))));
            }
            return;
        }
    }

    private String serializeSetting(Setting setting) {
        if (setting instanceof BooleanSetting) {
            BooleanSetting booleanSetting = (BooleanSetting)setting;
            return String.valueOf(booleanSetting.getValue());
        }
        if (setting instanceof NumberSetting) {
            NumberSetting numberSetting = (NumberSetting)setting;
            return numberSetting.getValue().toString();
        }
        if (setting instanceof ModeSetting) {
            ModeSetting modeSetting = (ModeSetting)setting;
            return modeSetting.getValue();
        }
        if (setting instanceof StringSetting) {
            StringSetting stringSetting = (StringSetting)setting;
            return stringSetting.getValue();
        }
        if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting)setting;
            Color color = colorSetting.getValue().getColor();
            return color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha() + "," + colorSetting.isSync() + "," + colorSetting.isRainbow();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (!(packet instanceof ClientboundCustomPayloadPacket)) {
            return;
        }
        ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = (ClientboundCustomPayloadPacket)packet;
        CustomPacketPayload customPacketPayload = clientboundCustomPayloadPacket.payload();
        if (!(customPacketPayload instanceof PbCustomPayload)) {
            return;
        }
        PbCustomPayload pbCustomPayload = (PbCustomPayload)customPacketPayload;
        if (packetReceiveEvent.getConnection() != null) {
            this.proxyConnection = packetReceiveEvent.getConnection();
        }
        FriendlyByteBuf friendlyByteBuf = pbCustomPayload.toBuf();
        try {
            int packetId = friendlyByteBuf.readVarInt();
            switch (packetId) {
                case 1: {
                    this.handlePasswordRequest();
                    return;
                }
                case 5: {
                    this.handleModuleState(new S2CModuleStatePacket(friendlyByteBuf));
                    return;
                }
                case 6: {
                    this.handleSettingState(new S2CSettingStatePacket(friendlyByteBuf));
                    return;
                }
                case 7: {
                    this.handleError(new S2CErrorPacket(friendlyByteBuf));
                    return;
                }
                case 8: {
                    this.handleServerName(new S2CServerNamePacket(friendlyByteBuf));
                    return;
                }
                case 10: {
                    this.handleRenderPosition(new S2CRenderPositionPacket(friendlyByteBuf));
                    return;
                }
                case 11: {
                    this.handleBlockRender(new S2CBlockRenderPacket(friendlyByteBuf));
                    return;
                }
                case 12: {
                    this.handleMiningState(new S2CMiningStatePacket(friendlyByteBuf));
                    return;
                }
                case 13: {
                    this.handleSlotSync(new S2CSlotSyncPacket(friendlyByteBuf));
                    return;
                }
                case 16: {
                    this.handleWindowClick(new S2CWindowClickPacket(friendlyByteBuf));
                    return;
                }
            }
            return;
        }
        catch (Exception exception) {
            Night.LOGGER.warn("[PingBypass] Failed to handle S2C packet", (Throwable)exception);
            return;
        }
        finally {
            friendlyByteBuf.release();
        }
    }

    private void handlePasswordRequest() {
        Connection connection = this.proxyConnection;
        if (connection == null && mc.getConnection() != null) {
            connection = mc.getConnection().getConnection();
        }
        if (connection == null || !connection.isConnected()) {
            Night.LOGGER.warn("[PingBypass] Cannot send password response \u2014 no active connection");
            return;
        }
        this.sendRawCustomPayload(connection, new C2SPasswordPacket(this.password.getValue()));
        Night.LOGGER.info("[PingBypass] Sent password response to proxy");
        this.sendJoinIfReady(connection);
    }

    private void sendJoinIfReady(Connection connection) {
        String targetHost;
        if (this.joinSent) {
            return;
        }
        String targetServer = this.server.getValue();
        if (targetServer == null || targetServer.isBlank()) {
            return;
        }
        int targetPort = 25565;
        if (targetServer.contains(":")) {
            String[] parts = targetServer.split(":", 2);
            targetHost = parts[0];
            try {
                targetPort = Integer.parseInt(parts[1]);
            }
            catch (NumberFormatException numberFormatException) {
                Night.LOGGER.warn("[PingBypass] Invalid port in server setting: {}", (Object)targetServer);
                return;
            }
        } else {
            targetHost = targetServer;
        }
        this.joinSent = true;
        this.sendRawCustomPayload(connection, new C2SJoinPacket(targetHost, targetPort));
        Night.LOGGER.info("[PingBypass] Sent join request to proxy: {}:{}", (Object)targetHost, (Object)targetPort);
    }

    private void sendRawCustomPayload(Connection connection, PbPacket pbPacket) {
        PbCustomPayload pbCustomPayload = PbCustomPayload.fromPacket(pbPacket);
        this.pendingPayloads.add(new PendingPayload(pbCustomPayload, connection));
        this.pendingDelay = 10;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (this.pendingPayloads.isEmpty()) {
            return;
        }
        if (this.pendingDelay > 0) {
            --this.pendingDelay;
            return;
        }
        Iterator<PendingPayload> iterator = this.pendingPayloads.iterator();
        while (iterator.hasNext()) {
            PendingPayload pendingPayload = iterator.next();
            if (!pendingPayload.connection.isConnected()) {
                iterator.remove();
                continue;
            }
            try {
                pendingPayload.connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)pendingPayload.payload));
                iterator.remove();
                Night.LOGGER.info("[PingBypass] Sent deferred payload via vanilla networking");
            }
            catch (IllegalStateException illegalStateException) {
                this.pendingDelay = 5;
            }
        }
    }

    private void handleModuleState(S2CModuleStatePacket s2CModuleStatePacket) {
        this.proxyModuleStates.put(s2CModuleStatePacket.getModuleName(), s2CModuleStatePacket.isEnabled());
        Module module = Night.MODULE_MANAGER.getModule(s2CModuleStatePacket.getModuleName());
        if (module != null && module.isToggled() != s2CModuleStatePacket.isEnabled()) {
            mc.execute(() -> module.setToggled(s2CModuleStatePacket.isEnabled(), false));
        }
        Night.LOGGER.debug("[PingBypass] Module {} state synced to {}", (Object)s2CModuleStatePacket.getModuleName(), (Object)s2CModuleStatePacket.isEnabled());
    }

    private void handleSettingState(S2CSettingStatePacket s2CSettingStatePacket) {
        String key = s2CSettingStatePacket.getModuleName() + "." + s2CSettingStatePacket.getSettingName();
        this.proxySettingValues.put(key, s2CSettingStatePacket.getValue());
        Night.LOGGER.debug("[PingBypass] Setting {}.{} updated to {}", new Object[]{s2CSettingStatePacket.getModuleName(), s2CSettingStatePacket.getSettingName(), s2CSettingStatePacket.getValue()});
    }

    private void handleError(S2CErrorPacket s2CErrorPacket) {
        Night.CHAT_MANAGER.error("[PingBypass] " + s2CErrorPacket.getMessage());
    }

    private void handleServerName(S2CServerNamePacket s2CServerNamePacket) {
        this.serverName = s2CServerNamePacket.getServerIp();
        PingBypassFlags.proxyForwardingActive = true;
        if (this.inputForwarder == null) {
            this.inputForwarder = new ClientInputForwarder();
            this.inputForwarder.start();
        }
        this.syncAllSettingsToProxy();
        Night.CHAT_MANAGER.message("[PingBypass] Proxy connected to: " + s2CServerNamePacket.getServerIp());
    }

    public void syncAllSettingsToProxy() {
        Connection connection = this.proxyConnection;
        if (connection == null && mc.getConnection() != null) {
            connection = mc.getConnection().getConnection();
        }
        if (connection == null || !connection.isConnected()) {
            return;
        }
        int syncedCount = 0;
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (!module.isProxyEnhanced()) continue;
            connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new C2SModuleTogglePacket(module.getName(), module.isToggled()))));
            for (Setting setting : module.getSettings()) {
                String serialized = this.serializeSetting(setting);
                if (serialized == null) continue;
                connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new C2SSettingChangePacket(module.getName(), setting.getName(), serialized))));
                ++syncedCount;
            }
        }
        Night.LOGGER.info("[PingBypass] Synced {} settings to proxy", (Object)syncedCount);
        this.syncFriendsToProxy();
    }

    public void syncFriendsToProxy() {
        Connection connection = this.proxyConnection;
        if (connection == null && mc.getConnection() != null) {
            connection = mc.getConnection().getConnection();
        }
        if (connection == null || !connection.isConnected()) {
            return;
        }
        connection.send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new C2SFriendSyncPacket(new ArrayList<String>(Night.FRIEND_MANAGER.getFriends())))));
    }

    private void handleRenderPosition(S2CRenderPositionPacket s2CRenderPositionPacket) {
        Night.RENDER_MANAGER.setRenderPosition(s2CRenderPositionPacket.getPosition());
    }

    private void handleBlockRender(S2CBlockRenderPacket s2CBlockRenderPacket) {
        RenderPosition renderPosition = new RenderPosition(s2CBlockRenderPacket.getPosition());
        if (!Night.RENDER_MANAGER.renderPositions.contains(renderPosition)) {
            Night.RENDER_MANAGER.renderPositions.add(renderPosition);
        }
    }

    private void handleMiningState(S2CMiningStatePacket s2CMiningStatePacket) {
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        if (speedMineModule != null) {
            speedMineModule.updateProxyMiningState(s2CMiningStatePacket.getPrimaryPos(), s2CMiningStatePacket.getPrimaryProgress(), s2CMiningStatePacket.getSecondaryPos(), s2CMiningStatePacket.getSecondaryProgress());
        }
    }

    private void handleSlotSync(S2CSlotSyncPacket s2CSlotSyncPacket) {
        if (PingBypassModule.mc.player != null) {
            PingBypassModule.mc.player.getInventory().setSelectedSlot(s2CSlotSyncPacket.getSlot());
        }
    }

    private void handleWindowClick(S2CWindowClickPacket s2CWindowClickPacket) {
        mc.execute(() -> {
            if (PingBypassModule.mc.player == null || PingBypassModule.mc.player.containerMenu.containerId != s2CWindowClickPacket.getContainerId()) {
                return;
            }
            try {
                PingBypassModule.mc.player.containerMenu.clicked(s2CWindowClickPacket.getSlotNum(), s2CWindowClickPacket.getButtonNum(), s2CWindowClickPacket.getContainerInput(), (Player)PingBypassModule.mc.player);
            }
            catch (Exception exception) {
                Night.LOGGER.warn("[PingBypass] Failed to replay proxy window click", (Throwable)exception);
            }
        });
    }

    public String getServerName() {
        return this.serverName;
    }

    public Map<String, Boolean> getProxyModuleStates() {
        return this.proxyModuleStates;
    }

    public Map<String, String> getProxySettingValues() {
        return this.proxySettingValues;
    }

    public boolean isProxyModuleEnabled(String moduleName) {
        return this.proxyModuleStates.getOrDefault(moduleName, false);
    }

    public String getProxySettingValue(String moduleName, String settingName) {
        return this.proxySettingValues.get(moduleName + "." + settingName);
    }

    private record PendingPayload(PbCustomPayload payload, Connection connection) {
    }
}

