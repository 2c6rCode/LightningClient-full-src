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

    public MicrosoftLoginScreen(Screen screen) {
        super((Component)Component.literal((String)"Microsoft Login"));
        this.parent = screen;
    }

    protected void init() {
        int n = this.width / 2;
        int n2 = this.height / 2;
        int n3 = 160;
        int n4 = n2 - n3 / 2;
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
        }).bounds(n - 105, n4 + 85, 210, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Cancel"), button -> this.onClose()).bounds(n - 105, n4 + 112, 210, 20).build());
    }

    private void startAuth() {
        try {
            this.session = MicrosoftAuth.startBrowserLogin(string -> {
                this.status = string;
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

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(18, 14, 30), new Color(8, 6, 14));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        int n3 = this.width / 2;
        int n4 = this.height / 2;
        int n5 = 290;
        int n6 = 160;
        int n7 = n3 - n5 / 2;
        int n8 = n4 - n6 / 2;
        Renderer2D.renderQuad(guiGraphicsExtractor, n7, n8, n7 + n5, n8 + n6, new Color(28, 28, 28, 245));
        Renderer2D.renderOutline(guiGraphicsExtractor, n7, n8, n7 + n5, n8 + n6, new Color(70, 70, 70, 200));
        String string = "Microsoft Account Login";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n3 - Night.FONT_MANAGER.getWidth(string) / 2, n8 + 12, Color.WHITE);
        String string2 = "A browser tab has been opened.";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, n3 - Night.FONT_MANAGER.getWidth(string2) / 2, n8 + 32, new Color(180, 180, 180));
        String string3 = "Log in to your account and approve to continue.";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string3, n3 - Night.FONT_MANAGER.getWidth(string3) / 2, n8 + 45, new Color(180, 180, 180));
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.status, n3 - Night.FONT_MANAGER.getWidth(this.status) / 2, n8 + 66, new Color(85, 255, 255));
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
    }

    public void onClose() {
        if (this.session != null && this.session.cancelAction() != null) {
            this.session.cancelAction().run();
        }
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}

