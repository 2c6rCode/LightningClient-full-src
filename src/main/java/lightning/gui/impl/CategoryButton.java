/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.gui.impl;

import java.awt.Color;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CategoryButton
extends Button {
    private final CategorySetting setting;
    private final BooleanSetting enableSetting;
    private final Animation fillAnim;

    public CategoryButton(CategorySetting categorySetting, BooleanSetting enableSetting, Frame frame, int height) {
        super(categorySetting, frame, height, categorySetting.getDescription());
        this.setting = categorySetting;
        this.enableSetting = enableSetting;
        float initial = enableSetting != null && enableSetting.getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(initial, initial, 180, Easing.Method.EASE_OUT_CUBIC);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        float fillProgress = this.fillAnim.get(this.enableSetting != null && this.enableSetting.getValue() ? 1.0f : 0.0f);
        if (fillProgress > 0.001f) {
            int fillEndX = this.getX() + this.getPadding() + 1 + Math.round((float)(this.getWidth() - (this.getPadding() + 1) * 2) * fillProgress);
            Color fillCol = ClickGuiScreen.getButtonColor(this.getY(), Math.round(100.0f * fillProgress));
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), fillEndX, this.getY() + this.getHeight() - 1, fillCol);
        }
        Color color = this.enableSetting != null && this.enableSetting.getValue() ? Color.WHITE : ClickGuiScreen.getButtonColor(this.getY(), 180);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), this.getX() + this.getTextPadding() + 4, this.getY() + 2, color);
        String indicator = this.setting.isOpen() ? "..." : "+";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, indicator, this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth(indicator), this.getY() + 2, Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isHovering(mouseX, mouseY)) {
            return;
        }
        if (this.enableSetting != null && button == 0) {
            this.enableSetting.setValue(!this.enableSetting.getValue());
            this.playClickSound();
        } else if (button == 1 || this.enableSetting == null && button == 0) {
            this.setting.setOpen(!this.setting.isOpen());
            this.playClickSound();
        }
    }
}

