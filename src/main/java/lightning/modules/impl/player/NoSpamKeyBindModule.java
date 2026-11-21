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

    private static String norm(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFC).toLowerCase();
    }

    private static boolean containsAny(String string, String[] stringArray) {
        for (String string2 : stringArray) {
            if (!string.contains(NoSpamKeyBindModule.norm(string2))) continue;
            return true;
        }
        return false;
    }

    private boolean serverMatches() {
        try {
            String string = this.server.getValue();
            if (string == null || string.isBlank()) {
                return true;
            }
            String string2 = Night.SERVER_MANAGER.getServer();
            if (string2 == null) {
                return false;
            }
            string2 = string2.toLowerCase();
            for (String string3 : string.toLowerCase().split(",")) {
                if ((string3 = string3.trim()).isEmpty() || !string2.contains(string3)) continue;
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private void say(String string) {
        if (!this.notify.getValue()) {
            return;
        }
        try {
            Night.CHAT_MANAGER.tagged(string, this.getName());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void startBlocking(boolean bl) {
        long l = System.currentTimeMillis();
        boolean bl2 = this.blocked;
        this.blocked = true;
        this.sawJoin = bl || this.sawJoin;
        this.lastMarker = l;
        if (!bl2) {
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
            String string;
            ClientboundSystemChatPacket clientboundSystemChatPacket = (ClientboundSystemChatPacket)packet;
            if (!this.serverMatches()) {
                return;
            }
            try {
                string = NoSpamKeyBindModule.norm(clientboundSystemChatPacket.content().getString());
            }
            catch (Throwable throwable) {
                return;
            }
            if (NoSpamKeyBindModule.containsAny(string, STRONG)) {
                this.startBlocking(true);
            } else if (this.blocked && NoSpamKeyBindModule.containsAny(string, WEAK)) {
                this.lastMarker = System.currentTimeMillis();
            }
        } else if (packet instanceof ClientboundLoginPacket || packet instanceof ClientboundRespawnPacket) {
            if (!this.blocked) {
                return;
            }
            long l = System.currentTimeMillis();
            if (!this.sawJoin) {
                this.sawJoin = true;
                this.lastMarker = l;
                return;
            }
            long l2 = this.lobbyGap.getValue().longValue() * 1000L;
            if (l - this.lastMarker >= l2) {
                this.blocked = false;
                this.say("Entered the server - commands from modules & keybinds work again.");
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        int n;
        Object object;
        String string;
        block11: {
            if (!this.blocked) {
                return;
            }
            Packet<?> packet = packetSendEvent.getPacket();
            try {
                if (packet instanceof ServerboundChatCommandPacket) {
                    ServerboundChatCommandPacket serverboundChatCommandPacket = (ServerboundChatCommandPacket)packet;
                    string = serverboundChatCommandPacket.command();
                    break block11;
                }
                if (packet instanceof ServerboundChatCommandSignedPacket) {
                    ServerboundChatCommandSignedPacket signedPacket = (ServerboundChatCommandSignedPacket)packet;
                    string = signedPacket.command();
                    break block11;
                }
                return;
            }
            catch (Throwable throwable) {
                string = "";
            }
        }
        if (string == null) {
            string = "";
        }
        if (((String)(object = string.trim().toLowerCase())).startsWith("/")) {
            object = ((String)object).substring(1);
        }
        if ((n = ((String)object).indexOf(32)) >= 0) {
            object = ((String)object).substring(0, n);
        }
        if (this.isAllowed((String)object)) {
            return;
        }
        if (!this.blockManual.getValue() && !this.fromClientFeature()) {
            return;
        }
        packetSendEvent.cancel();
        long l = System.currentTimeMillis();
        if (l - this.lastNotify > 3000L) {
            this.lastNotify = l;
            this.say("Blocked /" + (String)object + " (login/lobby).");
        }
    }

    private boolean isAllowed(String string) {
        String string2 = this.allowed.getValue();
        if (string2 == null) {
            return false;
        }
        for (String string3 : string2.toLowerCase().split(",")) {
            if (!string3.trim().equals(string)) continue;
            return true;
        }
        return false;
    }

    private boolean fromClientFeature() {
        String string = NoSpamKeyBindModule.class.getName();
        try {
            return StackWalker.getInstance().walk(stream -> stream.anyMatch(stackFrame -> {
                String string2 = stackFrame.getClassName();
                if (string2.equals(string) || string2.startsWith("lightning.modules.impl.miscellaneous.AutoLoginModule")) {
                    return false;
                }
                return string2.startsWith("lightning.modules.") || string2.startsWith("lightning.managers.MacroManager");
            }));
        }
        catch (Throwable throwable) {
            return true;
        }
    }
}

