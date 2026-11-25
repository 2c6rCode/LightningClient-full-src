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

    public BooleanButton(BooleanSetting booleanSetting, Frame frame, int height) {
        super(booleanSetting, frame, height, booleanSetting.getDescription());
        this.setting = booleanSetting;
        float initial = booleanSetting.getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(initial, initial, 180, Easing.Method.EASE_OUT_CUBIC);
        this.hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
        this.textAnim = new Animation(initial, initial, 150, Easing.Method.EASE_OUT_CUBIC);
        this.slideAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
    }

    public void setPrevVisible(boolean prevVisible) {
        this.prevVisible = prevVisible;
    }

    public void resetSlide() {
        this.slideAnim.setCurrent(0.0f);
        this.slideAnim.setPrev(0.0f);
        this.slideAnim.setActiveDuration(220);
        this.slideAnim.setStartTime(System.currentTimeMillis());
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        boolean hovered;
        if (!this.prevVisible) {
            this.resetSlide();
            this.prevVisible = true;
        }
        float hoverProgress = this.hoverAnim.get((hovered = this.isHovering(mouseX, mouseY)) ? 1.0f : 0.0f);
        float fillProgress = this.fillAnim.get(this.setting.getValue() ? 1.0f : 0.0f);
        float textProgress = this.textAnim.get(this.setting.getValue() || hovered ? 1.0f : 0.0f);
        float slideProgress = this.slideAnim.get(1.0f);
        int slideOffset = Math.round((1.0f - slideProgress) * -20.0f);
        int leftX = this.getX() + this.getPadding() + 1 + slideOffset;
        int rightX = this.getX() + this.getWidth() - this.getPadding() - 1 + slideOffset;
        LightningSkin.fq(guiGraphicsExtractor, leftX, this.getY(), rightX, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        if (hoverProgress > 0.001f) {
            Color color = new Color(255, 255, 255, (int)(20.0f * hoverProgress));
            LightningSkin.fq(guiGraphicsExtractor, leftX, this.getY(), rightX, this.getY() + this.getHeight() - 1, color);
        }
        if (fillProgress > 0.001f) {
            int fillEndX = leftX + Math.round((float)(rightX - leftX) * fillProgress);
            Color fillCol = ClickGuiScreen.getButtonColor(this.getY(), Math.round(100.0f * fillProgress));
            LightningSkin.fq(guiGraphicsExtractor, leftX, this.getY(), fillEndX, this.getY() + this.getHeight() - 1, fillCol);
        }
        float textX = (float)(this.getX() + this.getTextPadding() + 3) + textProgress * 1.5f + (float)slideOffset;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(this.setting.getValue() ? "" : ChatFormatting.GRAY) + this.setting.getTag(), (int)textX, this.getY() + 2, Color.WHITE);
        ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (clickGuiModule != null) {
            int switchW = 14;
            int switchH = 7;
            int switchX = rightX - switchW - 1;
            int switchY = this.getY() + (this.getHeight() - switchH) / 2;
            Color color = this.setting.getValue() ? ClickGuiScreen.getButtonColor(this.getY(), Math.round(180.0f * fillProgress)) : new Color(41, 41, 41, 160);
            LightningSkin.fq(guiGraphicsExtractor, switchX, switchY, switchX + switchW, switchY + switchH, color);
            LightningSkin.ol(guiGraphicsExtractor, switchX, switchY, switchX + switchW, switchY + switchH, new Color(0, 0, 0, 80));
            int knobW = 5;
            int knobX = switchX + Math.round(fillProgress * (float)(switchW - knobW));
            Color knobColor = this.setting.getValue() ? Color.WHITE : new Color(180, 180, 190);
            LightningSkin.fq(guiGraphicsExtractor, knobX, switchY - 1, knobX + knobW, switchY + switchH + 1, knobColor);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 0) {
            this.setting.setValue(!this.setting.getValue());
            this.playClickSound();
        }
    }
}

