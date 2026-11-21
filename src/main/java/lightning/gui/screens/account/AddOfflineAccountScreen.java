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

    public AddOfflineAccountScreen(Screen screen) {
        super((Component)Component.literal((String)"Add Offline Account"));
        this.parent = screen;
    }

    protected void init() {
        int n = this.width / 2;
        int n2 = this.height / 2;
        int n3 = 175;
        int n4 = n2 - n3 / 2;
        this.usernameField = new EditBox(this.font, n - 115, n4 + 42, 230, 20, (Component)Component.literal((String)"Username"));
        this.usernameField.setMaxLength(16);
        this.usernameField.setHint((Component)Component.literal((String)"Enter username..."));
        this.addRenderableWidget(this.usernameField);
        this.setInitialFocus((GuiEventListener)this.usernameField);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Add & Login"), button -> {
            String string = this.usernameField.getValue().trim();
            if (string.isEmpty()) {
                this.error = "Username cannot be empty!";
                return;
            }
            Account account = Night.ACCOUNT_MANAGER.loginOffline(string);
            if (account != null) {
                Minecraft.getInstance().gui.setScreen(this.parent);
            }
        }).bounds(n - 115, n4 + 68, 230, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Random Name"), button -> {
            String[] stringArray = new String[]{"Shadow", "Lightning", "Ghost", "Viper", "Phantom", "Raven", "Frost", "Nova", "Dark", "Storm"};
            String string = stringArray[new Random().nextInt(stringArray.length)] + "_" + (100 + new Random().nextInt(900));
            this.usernameField.setValue(string);
        }).bounds(n - 115, n4 + 94, 230, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Cancel"), button -> this.onClose()).bounds(n - 115, n4 + 120, 230, 20).build());
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(18, 14, 30), new Color(8, 6, 14));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        int n3 = this.width / 2;
        int n4 = this.height / 2;
        int n5 = 270;
        int n6 = 175;
        int n7 = n3 - n5 / 2;
        int n8 = n4 - n6 / 2;
        Renderer2D.renderQuad(guiGraphicsExtractor, n7, n8, n7 + n5, n8 + n6, new Color(28, 28, 28, 245));
        Renderer2D.renderOutline(guiGraphicsExtractor, n7, n8, n7 + n5, n8 + n6, new Color(70, 70, 70, 200));
        String string = "Add Offline / Cracked Account";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n3 - Night.FONT_MANAGER.getWidth(string) / 2, n8 + 12, Color.WHITE);
        if (!this.error.isEmpty()) {
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "\u00a7c" + this.error, n3 - Night.FONT_MANAGER.getWidth(this.error) / 2, n8 + 28, Color.RED);
        }
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}

