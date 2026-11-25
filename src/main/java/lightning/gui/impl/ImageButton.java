/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 */
package lightning.gui.impl;

import java.awt.Color;
import lightning.Night;
import lightning.gui.ImageSelectScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.ImageSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class ImageButton
extends Button {
    private final ImageSetting setting;
    private final Animation hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private final Animation textAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);

    public ImageButton(ImageSetting imageSetting, Frame frame, int height) {
        super(imageSetting, frame, height, imageSetting.getDescription());
        this.setting = imageSetting;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        float hoverProgress = this.hoverAnim.get(hovered ? 1.0f : 0.0f);
        float textProgress = this.textAnim.get(hovered ? 1.0f : 0.0f);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, new Color(0, 0, 0, 40));
        if (hoverProgress > 0.001f) {
            Color color = new Color(255, 255, 255, (int)(20.0f * hoverProgress));
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, color);
        }
        float textX = (float)(this.getX() + this.getTextPadding() + 3) + textProgress * 1.5f;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), (int)textX, this.getY() + 2, Color.WHITE);
        String value = this.setting.getValue();
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("None")) {
            value = "[Select...]";
        }
        String displayValue = value;
        int maxWidth = this.getWidth() - Night.FONT_MANAGER.getWidth(this.setting.getTag()) - this.getTextPadding() * 2 - 15;
        if (Night.FONT_MANAGER.getWidth(displayValue) > maxWidth) {
            while (Night.FONT_MANAGER.getWidth(displayValue + "...") > maxWidth && displayValue.length() > 3) {
                displayValue = displayValue.substring(0, displayValue.length() - 1);
            }
            displayValue = displayValue + "...";
        }
        String formattedValue = String.valueOf(ChatFormatting.GRAY) + displayValue;
        int rightEdge = this.getX() + this.getWidth() - this.getTextPadding() - 1;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, formattedValue, rightEdge - Night.FONT_MANAGER.getWidth(formattedValue), this.getY() + 2, Color.LIGHT_GRAY);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && (button == 0 || button == 1)) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.gui.setScreen((Screen)new ImageSelectScreen(this.setting, minecraft.gui.screen()));
            this.playClickSound();
        }
    }
}

