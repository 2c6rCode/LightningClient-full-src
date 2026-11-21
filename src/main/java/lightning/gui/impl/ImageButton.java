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

    public ImageButton(ImageSetting imageSetting, Frame frame, int n) {
        super(imageSetting, frame, n, imageSetting.getDescription());
        this.setting = imageSetting;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        boolean bl = this.isHovering(n, n2);
        float f2 = this.hoverAnim.get(bl ? 1.0f : 0.0f);
        float f3 = this.textAnim.get(bl ? 1.0f : 0.0f);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, new Color(0, 0, 0, 40));
        if (f2 > 0.001f) {
            Color color = new Color(255, 255, 255, (int)(20.0f * f2));
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, color);
        }
        float f4 = (float)(this.getX() + this.getTextPadding() + 3) + f3 * 1.5f;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), (int)f4, this.getY() + 2, Color.WHITE);
        String string = this.setting.getValue();
        if (string == null || string.isEmpty() || string.equalsIgnoreCase("None")) {
            string = "[Select...]";
        }
        Object object = string;
        int n3 = this.getWidth() - Night.FONT_MANAGER.getWidth(this.setting.getTag()) - this.getTextPadding() * 2 - 15;
        if (Night.FONT_MANAGER.getWidth((String)object) > n3) {
            while (Night.FONT_MANAGER.getWidth((String)object + "...") > n3 && ((String)object).length() > 3) {
                object = ((String)object).substring(0, ((String)object).length() - 1);
            }
            object = (String)object + "...";
        }
        String string2 = String.valueOf(ChatFormatting.GRAY) + (String)object;
        int n4 = this.getX() + this.getWidth() - this.getTextPadding() - 1;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, n4 - Night.FONT_MANAGER.getWidth(string2), this.getY() + 2, Color.LIGHT_GRAY);
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2) && (n == 0 || n == 1)) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.gui.setScreen((Screen)new ImageSelectScreen(this.setting, minecraft.gui.screen()));
            this.playClickSound();
        }
    }
}

