/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundLoginPacket
 *  net.minecraft.network.protocol.game.ClientboundRespawnPacket
 *  net.minecraft.network.protocol.game.ClientboundSystemChatPacket
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket
 */
package lightning.modules.impl.player;

import java.text.Normalizer;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;

@RegisterModule(name="NoSpamKeyBind", description="Blocks commands sent by modules/keybinds (AutoShop, KeyAction, auto totem...) while you are at the login screen or in the lobby, so the server never flags command spam.", category=Module.Category.PLAYER, toggled=true)
public class NoSpamKeyBindModule
extends Module {
    private final StringSetting server = new StringSetting("Server", "Only active on servers whose address contains one of these words (comma separated). Empty = every server.", "kingmc");
    private final NumberSetting lobbyGap = new NumberSetting("LobbyGap", "Seconds without any login/lobby message before a world change counts as entering the real server.", 3, 1, 30);
    private final BooleanSetting blockManual = new BooleanSetting("BlockManual", "Also block commands you type yourself (except the allowed login commands).", false);
    private final StringSetting allowed = new StringSetting("Allowed", "Commands that are never blocked (comma separated, without the slash).", "dn,dk,dmk,login,register,l,reg");
    private final BooleanSetting notify = new BooleanSetting("Notify", "Prints a chat message when blocking starts/stops.", true);
    private static final String[] STRONG = new String[]{"\u0111\u0103ng nh\u1eadp b\u1eb1ng l\u1ec7nh", "c\u1ea7n \u0111\u0103ng nh\u1eadp", "/dn ", "/dk "};
    private static final String[] WEAK = new String[]{"s\u1ea3nh", "\u0111\u0103ng nh\u1eadp th\u00e0nh c\u00f4ng", "k\u1ebft n\u1ed1i tr\u1edf l\u1ea1i", "m\u00e3 pin"};
    private volatile boolean blocked;
    private volatile boolean sawJoin;
    private volatile long lastMarker;
    private volatile long lastNotify;

    private static String norm(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFC).toLowerCase();
    }

    private static boolean containsAny(String text, String[] patterns) {
        for (String pattern : patterns) {
            if (!text.contains(NoSpamKeyBindModule.norm(pattern))) continue;
            return true;
        }
        return false;
    }

    private boolean serverMatches() {
        try {
            String serverFilter = this.server.getValue();
            if (serverFilter == null || serverFilter.isBlank()) {
                return true;
            }
            String currentServer = Night.SERVER_MANAGER.getServer();
            if (currentServer == null) {
                return false;
            }
            currentServer = currentServer.toLowerCase();
            for (String token : serverFilter.toLowerCase().split(",")) {
                if ((token = token.trim()).isEmpty() || !currentServer.contains(token)) continue;
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private void say(String message) {
        if (!this.notify.getValue()) {
            return;
        }
        try {
            Night.CHAT_MANAGER.tagged(message, this.getName());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void startBlocking(boolean fromChatJoin) {
        long now = System.currentTimeMillis();
        boolean wasBlocked = this.blocked;
        this.blocked = true;
        this.sawJoin = fromChatJoin || this.sawJoin;
        this.lastMarker = now;
        if (!wasBlocked) {
            this.say("Login/lobby detected - commands from modules & keybinds are paused.");
        }
    }

    @Override
    public void onEnable() {
        this.blocked = false;
        this.sawJoin = false;
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent clientConnectEvent) {
        this.sawJoin = false;
        if (this.serverMatches()) {
            this.startBlocking(false);
        } else {
            this.blocked = false;
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.blocked = false;
        this.sawJoin = false;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundSystemChatPacket) {
            String normalizedMsg;
            ClientboundSystemChatPacket clientboundSystemChatPacket = (ClientboundSystemChatPacket)packet;
            if (!this.serverMatches()) {
                return;
            }
            try {
                normalizedMsg = NoSpamKeyBindModule.norm(clientboundSystemChatPacket.content().getString());
            }
            catch (Throwable throwable) {
                return;
            }
            if (NoSpamKeyBindModule.containsAny(normalizedMsg, STRONG)) {
                this.startBlocking(true);
            } else if (this.blocked && NoSpamKeyBindModule.containsAny(normalizedMsg, WEAK)) {
                this.lastMarker = System.currentTimeMillis();
            }
        } else if (packet instanceof ClientboundLoginPacket || packet instanceof ClientboundRespawnPacket) {
            if (!this.blocked) {
                return;
            }
            long now = System.currentTimeMillis();
            if (!this.sawJoin) {
                this.sawJoin = true;
                this.lastMarker = now;
                return;
            }
            long gapMs = this.lobbyGap.getValue().longValue() * 1000L;
            if (now - this.lastMarker >= gapMs) {
                this.blocked = false;
                this.say("Entered the server - commands from modules & keybinds work again.");
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        int spaceIndex;
        String baseCommand;
        String rawCommand;
        block11: {
            if (!this.blocked) {
                return;
            }
            Packet<?> packet = packetSendEvent.getPacket();
            try {
                if (packet instanceof ServerboundChatCommandPacket) {
                    ServerboundChatCommandPacket serverboundChatCommandPacket = (ServerboundChatCommandPacket)packet;
                    rawCommand = serverboundChatCommandPacket.command();
                    break block11;
                }
                if (packet instanceof ServerboundChatCommandSignedPacket) {
                    ServerboundChatCommandSignedPacket signedPacket = (ServerboundChatCommandSignedPacket)packet;
                    rawCommand = signedPacket.command();
                    break block11;
                }
                return;
            }
            catch (Throwable throwable) {
                rawCommand = "";
            }
        }
        if (rawCommand == null) {
            rawCommand = "";
        }
        if ((baseCommand = rawCommand.trim().toLowerCase()).startsWith("/")) {
            baseCommand = baseCommand.substring(1);
        }
        if ((spaceIndex = baseCommand.indexOf(32)) >= 0) {
            baseCommand = baseCommand.substring(0, spaceIndex);
        }
        if (this.isAllowed(baseCommand)) {
            return;
        }
        if (!this.blockManual.getValue() && !this.fromClientFeature()) {
            return;
        }
        packetSendEvent.cancel();
        long now = System.currentTimeMillis();
        if (now - this.lastNotify > 3000L) {
            this.lastNotify = now;
            this.say("Blocked /" + baseCommand + " (login/lobby).");
        }
    }

    private boolean isAllowed(String commandName) {
        String allowedList = this.allowed.getValue();
        if (allowedList == null) {
            return false;
        }
        for (String token : allowedList.toLowerCase().split(",")) {
            if (!token.trim().equals(commandName)) continue;
            return true;
        }
        return false;
    }

    private boolean fromClientFeature() {
        String selfClassName = NoSpamKeyBindModule.class.getName();
        try {
            return StackWalker.getInstance().walk(stream -> stream.anyMatch(stackFrame -> {
                String callerClass = stackFrame.getClassName();
                if (callerClass.equals(selfClassName) || callerClass.startsWith("lightning.modules.impl.miscellaneous.AutoLoginModule")) {
                    return false;
                }
                return callerClass.startsWith("lightning.modules.") || callerClass.startsWith("lightning.managers.MacroManager");
            }));
        }
        catch (Throwable throwable) {
            return true;
        }
    }
}

