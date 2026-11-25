/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Util
 */
package lightning.gui.screens.account;

import java.awt.Color;
import java.net.URI;
import lightning.Night;
import lightning.accounts.Account;
import lightning.accounts.AccountType;
import lightning.accounts.MicrosoftAuth;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class MicrosoftLoginScreen
extends Screen {
    private final Screen parent;
    private volatile MicrosoftAuth.BrowserLoginSession session;
    private volatile String status = "Opening browser for Microsoft login...";
    private volatile boolean failed = false;

    public MicrosoftLoginScreen(Screen parent) {
        super((Component)Component.literal((String)"Microsoft Login"));
        this.parent = parent;
    }

    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelHeight = 160;
        int panelTop = centerY - panelHeight / 2;
        if (this.session == null && !this.failed) {
            this.startAuth();
        }
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Open Browser Again"), button -> {
            if (this.session != null && this.session.loginUrl() != null) {
                try {
                    Util.getPlatform().openUri(URI.create(this.session.loginUrl()));
                }
                catch (Exception exception) {
                    this.status = "\u00a7cFailed to open browser: " + exception.getMessage();
                }
            }
        }).bounds(centerX - 105, panelTop + 85, 210, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Cancel"), button -> this.onClose()).bounds(centerX - 105, panelTop + 112, 210, 20).build());
    }

    private void startAuth() {
        try {
            this.session = MicrosoftAuth.startBrowserLogin(statusMessage -> {
                this.status = statusMessage;
            });
            this.session.future().thenAccept(authResult -> {
                if (authResult.success()) {
                    Account account = new Account(authResult.username(), authResult.uuid(), AccountType.MICROSOFT, authResult.refreshToken(), authResult.accessToken());
                    Night.ACCOUNT_MANAGER.addAccount(account);
                    Night.ACCOUNT_MANAGER.login(account);
                    Minecraft.getInstance().execute(this::onClose);
                } else {
                    this.status = "\u00a7c" + authResult.errorMessage();
                    this.failed = true;
                }
            });
        }
        catch (Exception exception) {
            this.status = "\u00a7cFailed to start login: " + exception.getMessage();
            this.failed = true;
        }
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(18, 14, 30), new Color(8, 6, 14));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 290;
        int panelHeight = 160;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        Renderer2D.renderQuad(guiGraphicsExtractor, panelX, panelY, panelX + panelWidth, panelY + panelHeight, new Color(28, 28, 28, 245));
        Renderer2D.renderOutline(guiGraphicsExtractor, panelX, panelY, panelX + panelWidth, panelY + panelHeight, new Color(70, 70, 70, 200));
        String title = "Microsoft Account Login";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, title, centerX - Night.FONT_MANAGER.getWidth(title) / 2, panelY + 12, Color.WHITE);
        String subtitle = "A browser tab has been opened.";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, subtitle, centerX - Night.FONT_MANAGER.getWidth(subtitle) / 2, panelY + 32, new Color(180, 180, 180));
        String instruction = "Log in to your account and approve to continue.";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, instruction, centerX - Night.FONT_MANAGER.getWidth(instruction) / 2, panelY + 45, new Color(180, 180, 180));
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.status, centerX - Night.FONT_MANAGER.getWidth(this.status) / 2, panelY + 66, new Color(85, 255, 255));
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
    }

    public void onClose() {
        if (this.session != null && this.session.cancelAction() != null) {
            this.session.cancelAction().run();
        }
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}

