/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.gui.impl;

import java.awt.Color;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BooleanButton
extends Button {
    private final BooleanSetting setting;
    private final Animation fillAnim;
    private final Animation hoverAnim;
    private final Animation textAnim;
    private final Animation slideAnim;
    private boolean prevVisible = false;

    public BooleanButton(BooleanSetting booleanSetting, Frame frame, int n) {
        super(booleanSetting, frame, n, booleanSetting.getDescription());
        this.setting = booleanSetting;
        float f = booleanSetting.getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(f, f, 180, Easing.Method.EASE_OUT_CUBIC);
        this.hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.textAnim = new Animation(f, f, 150, Easing.Method.EASE_OUT_CUBIC);
        this.slideAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
    }

    public void setPrevVisible(boolean bl) {
        this.prevVisible = bl;
    }

    public void resetSlide() {
        this.slideAnim.setCurrent(0.0f);
        this.slideAnim.setPrev(0.0f);
        this.slideAnim.setActiveDuration(220);
        this.slideAnim.setStartTime(System.currentTimeMillis());
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Object object;
        boolean bl;
        if (!this.prevVisible) {
            this.resetSlide();
            this.prevVisible = true;
        }
        float f2 = this.hoverAnim.get((bl = this.isHovering(n, n2)) ? 1.0f : 0.0f);
        float f3 = this.fillAnim.get(this.setting.getValue() ? 1.0f : 0.0f);
        float f4 = this.textAnim.get(this.setting.getValue() || bl ? 1.0f : 0.0f);
        float f5 = this.slideAnim.get(1.0f);
        int n3 = Math.round((1.0f - f5) * -20.0f);
        int n4 = this.getX() + this.getPadding() + 1 + n3;
        int n5 = this.getX() + this.getWidth() - this.getPadding() - 1 + n3;
        LightningSkin.fq(guiGraphicsExtractor, n4, this.getY(), n5, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        if (f2 > 0.001f) {
            Color color = new Color(255, 255, 255, (int)(20.0f * f2));
            LightningSkin.fq(guiGraphicsExtractor, n4, this.getY(), n5, this.getY() + this.getHeight() - 1, color);
        }
        if (f3 > 0.001f) {
            int n6 = n4 + Math.round((float)(n5 - n4) * f3);
            object = ClickGuiScreen.getButtonColor(this.getY(), Math.round(100.0f * f3));
            LightningSkin.fq(guiGraphicsExtractor, n4, this.getY(), n6, this.getY() + this.getHeight() - 1, (Color)object);
        }
        float f6 = (float)(this.getX() + this.getTextPadding() + 3) + f4 * 1.5f + (float)n3;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(this.setting.getValue() ? "" : ChatFormatting.GRAY) + this.setting.getTag(), (int)f6, this.getY() + 2, Color.WHITE);
        object = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (object != null) {
            if (true) {
                int n7 = 14;
                int n8 = 7;
                int n9 = n5 - n7 - 1;
                int n10 = this.getY() + (this.getHeight() - n8) / 2;
                Color color = this.setting.getValue() ? ClickGuiScreen.getButtonColor(this.getY(), Math.round(180.0f * f3)) : new Color(41, 41, 41, 160);
                LightningSkin.fq(guiGraphicsExtractor, n9, n10, n9 + n7, n10 + n8, color);
                LightningSkin.ol(guiGraphicsExtractor, n9, n10, n9 + n7, n10 + n8, new Color(0, 0, 0, 80));
                int n11 = 5;
                int n12 = n9 + Math.round(f3 * (float)(n7 - n11));
                Color color2 = this.setting.getValue() ? Color.WHITE : new Color(180, 180, 190);
                LightningSkin.fq(guiGraphicsExtractor, n12, n10 - 1, n12 + n11, n10 + n8 + 1, color2);
            }
        }
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2) && n == 0) {
            this.setting.setValue(!this.setting.getValue());
            this.playClickSound();
        }
    }
}

