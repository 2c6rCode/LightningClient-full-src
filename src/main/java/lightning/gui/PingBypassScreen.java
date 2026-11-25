/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package lightning.gui;

import java.net.InetSocketAddress;
import java.net.Socket;
import lightning.Night;
import lightning.modules.impl.core.PingBypassModule;
import lightning.pingbypass.PingBypassConfig;
import lightning.pingbypass.PingBypassFlags;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PingBypassScreen
extends Screen {
    private final Screen parent;
    private EditBox ipField;
    private EditBox portField;
    private EditBox passwordField;
    private EditBox serverField;
    private Button connectButton;
    private Button resumeButton;
    private volatile String proxyStatus = "Pinging...";
    private volatile boolean pinged = false;

    public PingBypassScreen(Screen screen) {
        super((Component)Component.literal((String)"PingBypass"));
        this.parent = screen;
    }

    protected void init() {
        int centerX = this.width / 2;
        int startY = 50;
        this.ipField = new EditBox(this.font, centerX - 150, startY, 300, 20, (Component)Component.literal((String)"Proxy IP"));
        this.ipField.setMaxLength(256);
        this.ipField.setValue(this.getConfig().getIp());
        this.ipField.setHint((Component)Component.literal((String)"127.0.0.1"));
        this.addRenderableWidget(this.ipField);
        this.portField = new EditBox(this.font, centerX - 150, startY + 34, 145, 20, (Component)Component.literal((String)"Port"));
        this.portField.setMaxLength(5);
        this.portField.setValue(String.valueOf(this.getConfig().getPort()));
        this.portField.setHint((Component)Component.literal((String)"25565"));
        this.addRenderableWidget(this.portField);
        this.passwordField = new EditBox(this.font, centerX + 5, startY + 34, 145, 20, (Component)Component.literal((String)"Password"));
        this.passwordField.setMaxLength(64);
        this.passwordField.setValue(this.getConfig().getPassword());
        this.passwordField.setHint((Component)Component.literal((String)"Password"));
        this.addRenderableWidget(this.passwordField);
        this.serverField = new EditBox(this.font, centerX - 150, startY + 68, 300, 20, (Component)Component.literal((String)"Target Server"));
        this.serverField.setMaxLength(256);
        String targetServer = Night.MODULE_MANAGER.getModule(PingBypassModule.class).server.getValue();
        this.serverField.setValue(targetServer.isEmpty() ? "" : targetServer);
        this.serverField.setHint((Component)Component.literal((String)"mc.server.com:25565"));
        this.addRenderableWidget(this.serverField);
        this.connectButton = Button.builder((Component)Component.literal((String)"Connect"), button -> this.connect()).bounds(centerX - 150, startY + 110, 300, 20).build();
        this.addRenderableWidget(this.connectButton);
        PingBypassModule pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        this.resumeButton = Button.builder((Component)Component.literal((String)"Resume Session"), button -> this.resume()).bounds(centerX - 150, startY + 138, 300, 20).build();
        this.resumeButton.active = pingBypassModule != null && pingBypassModule.isToggled();
        this.addRenderableWidget(this.resumeButton);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Back"), button -> this.onClose()).bounds(centerX - 150, this.height - 30, 300, 20).build());
        this.pingProxy();
    }

    private void pingProxy() {
        this.pinged = false;
        this.proxyStatus = "Pinging...";
        String ip = this.getConfig().getIp();
        int port = this.getConfig().getPort();
        new Thread(() -> {
            try (Socket socket = new Socket();){
                socket.connect(new InetSocketAddress(ip, port), 2000);
                this.proxyStatus = "Proxy online";
                this.pinged = true;
            }
            catch (Exception exception) {
                this.proxyStatus = "\u00a7cOffline \u2014 cannot reach proxy";
                this.pinged = true;
            }
        }, "PingBypass-Pinger").start();
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
        guiGraphicsExtractor.centeredText(this.font, "PingBypass", this.width / 2, 20, 0xFFFFFF);
        int centerX = this.width / 2;
        int startY = 50;
        guiGraphicsExtractor.text(this.font, "Proxy IP", centerX - 150, startY - 11, 0xA0A0A0);
        guiGraphicsExtractor.text(this.font, "Port", centerX - 150, startY + 23, 0xA0A0A0);
        guiGraphicsExtractor.text(this.font, "Password", centerX + 5, startY + 23, 0xA0A0A0);
        guiGraphicsExtractor.text(this.font, "Target Server", centerX - 150, startY + 57, 0xA0A0A0);
        int statusColor = this.proxyStatus.contains("Connected") ? 0x55FF55 : (this.proxyStatus.contains("Idle") ? 0xFFAA00 : (this.proxyStatus.contains("Offline") ? 0xFF5555 : 0xAAAAAA));
        guiGraphicsExtractor.centeredText(this.font, this.proxyStatus, this.width / 2, startY + 165, statusColor);
        if (PingBypassFlags.proxyForwardingActive) {
            PingBypassModule pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
            String sessionText = "Session active \u2014 " + (pingBypassModule.getServerName() != null ? pingBypassModule.getServerName() : "connected");
            guiGraphicsExtractor.centeredText(this.font, sessionText, this.width / 2, startY + 178, 0x55FF55);
        }
    }

    private void connect() {
        PingBypassModule pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        pingBypassModule.ip.setValue(this.ipField.getValue().trim());
        try {
            pingBypassModule.port.setValue(Integer.parseInt(this.portField.getValue().trim()));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        pingBypassModule.password.setValue(this.passwordField.getValue());
        pingBypassModule.server.setValue(this.serverField.getValue().trim());
        if (pingBypassModule.isToggled()) {
            pingBypassModule.setToggled(false, false);
        }
        pingBypassModule.setToggled(true);
    }

    private void resume() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private PingBypassConfig getConfig() {
        return Night.PINGBYPASS_CONFIG;
    }
}

