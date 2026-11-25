/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package lightning.gui.screens.account;

import java.awt.Color;
import java.util.Random;
import lightning.Night;
import lightning.accounts.Account;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AddOfflineAccountScreen
extends Screen {
    private final Screen parent;
    private EditBox usernameField;
    private String error = "";

    public AddOfflineAccountScreen(Screen parent) {
        super((Component)Component.literal((String)"Add Offline Account"));
        this.parent = parent;
    }

    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelHeight = 175;
        int panelTop = centerY - panelHeight / 2;
        this.usernameField = new EditBox(this.font, centerX - 115, panelTop + 42, 230, 20, (Component)Component.literal((String)"Username"));
        this.usernameField.setMaxLength(16);
        this.usernameField.setHint((Component)Component.literal((String)"Enter username..."));
        this.addRenderableWidget(this.usernameField);
        this.setInitialFocus((GuiEventListener)this.usernameField);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Add & Login"), button -> {
            String username = this.usernameField.getValue().trim();
            if (username.isEmpty()) {
                this.error = "Username cannot be empty!";
                return;
            }
            Account account = Night.ACCOUNT_MANAGER.loginOffline(username);
            if (account != null) {
                Minecraft.getInstance().gui.setScreen(this.parent);
            }
        }).bounds(centerX - 115, panelTop + 68, 230, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Random Name"), button -> {
            String[] prefixes = new String[]{"Shadow", "Lightning", "Ghost", "Viper", "Phantom", "Raven", "Frost", "Nova", "Dark", "Storm"};
            String randomName = prefixes[new Random().nextInt(prefixes.length)] + "_" + (100 + new Random().nextInt(900));
            this.usernameField.setValue(randomName);
        }).bounds(centerX - 115, panelTop + 94, 230, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Cancel"), button -> this.onClose()).bounds(centerX - 115, panelTop + 120, 230, 20).build());
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(18, 14, 30), new Color(8, 6, 14));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 270;
        int panelHeight = 175;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        Renderer2D.renderQuad(guiGraphicsExtractor, panelX, panelY, panelX + panelWidth, panelY + panelHeight, new Color(28, 28, 28, 245));
        Renderer2D.renderOutline(guiGraphicsExtractor, panelX, panelY, panelX + panelWidth, panelY + panelHeight, new Color(70, 70, 70, 200));
        String title = "Add Offline / Cracked Account";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, title, centerX - Night.FONT_MANAGER.getWidth(title) / 2, panelY + 12, Color.WHITE);
        if (!this.error.isEmpty()) {
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "\u00a7c" + this.error, centerX - Night.FONT_MANAGER.getWidth(this.error) / 2, panelY + 28, Color.RED);
        }
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}

