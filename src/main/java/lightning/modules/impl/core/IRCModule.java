/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonObject
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 */
package lightning.modules.impl.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.text.FormattingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

@RegisterModule(name="IRC", description="Internet Relay Chat with other Lightning users.", category=Module.Category.CORE, toggled=true, persistent=true, drawn=false)
public class IRCModule
extends Module {
    private static final Gson GSON = new Gson();
    private static final String[] IRC_SERVERS = new String[]{"wss://nightclient.info.vn/irc", "ws://nightclient.info.vn/irc", "ws://103.78.2.201/irc"};
    public StringSetting prefix = new StringSetting("Prefix", "The prefix character for IRC messages in chat.", "$");
    public BooleanSetting showPrefix = new BooleanSetting("ShowPrefix", "Show [Lightning] tag on IRC messages.", true);
    public ModeSetting primaryColor = new ModeSetting("PrimaryColor", "Color of username and tag.", "Client", FormattingUtils.FORMATS);
    public ModeSetting secondaryColor = new ModeSetting("SecondaryColor", "Color of IRC message text.", "White", FormattingUtils.FORMATS);
    public BooleanSetting notifyConnect = new BooleanSetting("NotifyConnect", "Show connection status notifications in chat.", false);
    private WebSocket webSocket;
    private volatile boolean connected = false;
    private volatile String myUsername = null;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Lightning-IRC-Service");
        thread.setDaemon(true);
        return thread;
    });
    private long lastMessageTime = 0L;
    private static final long CHAT_COOLDOWN_MS = 3000L;

    public IRCModule() {
        this.connect();
    }

    @Override
    public void onEnable() {
        if (!this.connected && (this.webSocket == null || this.webSocket.isInputClosed() || this.webSocket.isOutputClosed())) {
            this.connect();
        }
    }

    @Override
    public void onDisable() {
        this.disconnect();
    }

    public synchronized void connect() {
    }

    private void doConnect() {
    }

    private void scheduleReconnect(long delaySeconds) {
    }

    public synchronized void disconnect() {
        this.connected = false;
        if (this.webSocket != null) {
            try {
                this.webSocket.sendClose(1000, "Module disabled");
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.webSocket = null;
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent chatInputEvent) {
        if (!this.isToggled()) {
            return;
        }
        String message = chatInputEvent.getMessage();
        if (message == null) {
            return;
        }
        String prefixValue = this.prefix.getValue();
        if (prefixValue == null || prefixValue.isEmpty()) {
            prefixValue = "$";
        }
        if (message.startsWith(prefixValue)) {
            chatInputEvent.setCancelled(true);
            String content = message.substring(prefixValue.length()).trim();
            if (content.isEmpty()) {
                Night.CHAT_MANAGER.warn("Usage: " + prefixValue + "<message>");
                return;
            }
            this.sendMessage(content);
        }
    }

    public void sendMessage(String message) {
        if (!this.connected || this.webSocket == null || this.webSocket.isOutputClosed()) {
            Night.CHAT_MANAGER.warn("IRC is currently not connected to server. Reconnecting...");
            this.connect();
            return;
        }
        long now = System.currentTimeMillis();
        long elapsed = now - this.lastMessageTime;
        if (elapsed < 3000L) {
            double remainingSeconds = (double)(3000L - elapsed) / 1000.0;
            Night.CHAT_MANAGER.warn(String.format("Please wait %.1fs before sending another message", remainingSeconds));
            return;
        }
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "chat");
            jsonObject.addProperty("content", message);
            this.webSocket.sendText(jsonObject.toString(), true);
            this.lastMessageTime = now;
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.error("Failed to send IRC message: " + exception.getMessage());
            this.scheduleReconnect(3L);
        }
    }

    private void handleServerMessage(String rawMessage) {
    }

    private Component createRoleComponent(String role) {
        String normalizedRole;
        if (role == null || role.isBlank()) {
            role = "stable";
        }
        if ((normalizedRole = role.toLowerCase()).equals("user") || normalizedRole.equals("member")) {
            normalizedRole = "stable";
        }
        TextColor textColor = switch (normalizedRole) {
            case "admin", "owner" -> TextColor.fromRgb((int)15277667);
            case "beta", "tester", "vip" -> TextColor.fromRgb((int)15277667);
            case "stable" -> TextColor.fromRgb((int)12616956);
            case "dev", "developer" -> TextColor.fromRgb((int)0x55FFFF);
            case "moderator", "mod" -> TextColor.fromRgb((int)0x55FF55);
            default -> TextColor.fromRgb((int)12616956);
        };
        return Component.literal((String)"<").withStyle(ChatFormatting.DARK_GRAY).append((Component)Component.literal((String)normalizedRole).withStyle(Style.EMPTY.withColor(textColor))).append((Component)Component.literal((String)"> ").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Style getStyleForSetting(String colorName) {
        if (colorName == null) {
            return Style.EMPTY;
        }
        if ("client".equalsIgnoreCase(colorName) || "rainbow".equalsIgnoreCase(colorName)) {
            return Style.EMPTY.withColor(TextColor.fromRgb((int)ColorUtils.getGlobalColor().getRGB()));
        }
        ChatFormatting chatFormatting = switch (colorName.toLowerCase()) {
            case "black" -> ChatFormatting.BLACK;
            case "blue" -> ChatFormatting.BLUE;
            case "dark blue" -> ChatFormatting.DARK_BLUE;
            case "green" -> ChatFormatting.GREEN;
            case "dark green" -> ChatFormatting.DARK_GREEN;
            case "aqua" -> ChatFormatting.AQUA;
            case "dark aqua" -> ChatFormatting.DARK_AQUA;
            case "red" -> ChatFormatting.RED;
            case "dark red" -> ChatFormatting.DARK_RED;
            case "light purple" -> ChatFormatting.LIGHT_PURPLE;
            case "dark purple" -> ChatFormatting.DARK_PURPLE;
            case "yellow" -> ChatFormatting.YELLOW;
            case "gold" -> ChatFormatting.GOLD;
            case "gray" -> ChatFormatting.GRAY;
            case "dark gray" -> ChatFormatting.DARK_GRAY;
            default -> ChatFormatting.WHITE;
        };
        return Style.EMPTY.withColor(chatFormatting);
    }

    private class IrcWebSocketListener
    implements WebSocket.Listener {
        private final StringBuilder buffer;
        final IRCModule this$0;

        private IrcWebSocketListener(IRCModule outer, IRCModule owner) {
            Objects.requireNonNull(outer);
            IRCModule target = owner;
            Objects.requireNonNull(target);
            this.this$0 = target;
            this.buffer = new StringBuilder();
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1L);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            this.buffer.append(data);
            if (last) {
                String fullMessage = this.buffer.toString();
                this.buffer.setLength(0);
                this.this$0.handleServerMessage(fullMessage);
            }
            webSocket.request(1L);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            this.this$0.connected = false;
            this.this$0.scheduleReconnect(3L);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable throwable) {
            this.this$0.connected = false;
            this.this$0.scheduleReconnect(5L);
        }
    }
}

