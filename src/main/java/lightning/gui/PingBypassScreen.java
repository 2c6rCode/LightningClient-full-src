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
        int n = this.width / 2;
        int n2 = 50;
        this.ipField = new EditBox(this.font, n - 150, n2, 300, 20, (Component)Component.literal((String)"Proxy IP"));
        this.ipField.setMaxLength(256);
        this.ipField.setValue(this.getConfig().getIp());
        this.ipField.setHint((Component)Component.literal((String)"127.0.0.1"));
        this.addRenderableWidget(this.ipField);
        this.portField = new EditBox(this.font, n - 150, n2 + 34, 145, 20, (Component)Component.literal((String)"Port"));
        this.portField.setMaxLength(5);
        this.portField.setValue(String.valueOf(this.getConfig().getPort()));
        this.portField.setHint((Component)Component.literal((String)"25565"));
        this.addRenderableWidget(this.portField);
        this.passwordField = new EditBox(this.font, n + 5, n2 + 34, 145, 20, (Component)Component.literal((String)"Password"));
        this.passwordField.setMaxLength(64);
        this.passwordField.setValue(this.getConfig().getPassword());
        this.passwordField.setHint((Component)Component.literal((String)"Password"));
        this.addRenderableWidget(this.passwordField);
        this.serverField = new EditBox(this.font, n - 150, n2 + 68, 300, 20, (Component)Component.literal((String)"Target Server"));
        this.serverField.setMaxLength(256);
        String string = Night.MODULE_MANAGER.getModule(PingBypassModule.class).server.getValue();
        this.serverField.setValue(string.isEmpty() ? "" : string);
        this.serverField.setHint((Component)Component.literal((String)"mc.server.com:25565"));
        this.addRenderableWidget(this.serverField);
        this.connectButton = Button.builder((Component)Component.literal((String)"Connect"), button -> this.connect()).bounds(n - 150, n2 + 110, 300, 20).build();
        this.addRenderableWidget(this.connectButton);
        PingBypassModule pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        this.resumeButton = Button.builder((Component)Component.literal((String)"Resume Session"), button -> this.resume()).bounds(n - 150, n2 + 138, 300, 20).build();
        this.resumeButton.active = pingBypassModule != null && pingBypassModule.isToggled();
        this.addRenderableWidget(this.resumeButton);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Back"), button -> this.onClose()).bounds(n - 150, this.height - 30, 300, 20).build());
        this.pingProxy();
    }

    private void pingProxy() {
        this.pinged = false;
        this.proxyStatus = "Pinging...";
        String string = this.getConfig().getIp();
        int n = this.getConfig().getPort();
        new Thread(() -> {
            try (Socket socket = new Socket();){
                socket.connect(new InetSocketAddress(string, n), 2000);
                this.proxyStatus = "Proxy online";
                this.pinged = true;
            }
            catch (Exception exception) {
                this.proxyStatus = "\u00a7cOffline \u2014 cannot reach proxy";
                this.pinged = true;
            }
        }, "PingBypass-Pinger").start();
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
        guiGraphicsExtractor.centeredText(this.font, "PingBypass", this.width / 2, 20, 0xFFFFFF);
        int n3 = this.width / 2;
        int n4 = 50;
        guiGraphicsExtractor.text(this.font, "Proxy IP", n3 - 150, n4 - 11, 0xA0A0A0);
        guiGraphicsExtractor.text(this.font, "Port", n3 - 150, n4 + 23, 0xA0A0A0);
        guiGraphicsExtractor.text(this.font, "Password", n3 + 5, n4 + 23, 0xA0A0A0);
        guiGraphicsExtractor.text(this.font, "Target Server", n3 - 150, n4 + 57, 0xA0A0A0);
        int n5 = this.proxyStatus.contains("Connected") ? 0x55FF55 : (this.proxyStatus.contains("Idle") ? 0xFFAA00 : (this.proxyStatus.contains("Offline") ? 0xFF5555 : 0xAAAAAA));
        guiGraphicsExtractor.centeredText(this.font, this.proxyStatus, this.width / 2, n4 + 165, n5);
        if (PingBypassFlags.proxyForwardingActive) {
            PingBypassModule pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
            String string = "Session active \u2014 " + (pingBypassModule.getServerName() != null ? pingBypassModule.getServerName() : "connected");
            guiGraphicsExtractor.centeredText(this.font, string, this.width / 2, n4 + 178, 0x55FF55);
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

