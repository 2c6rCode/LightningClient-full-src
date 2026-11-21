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

    public CategoryButton(CategorySetting categorySetting, BooleanSetting booleanSetting, Frame frame, int n) {
        super(categorySetting, frame, n, categorySetting.getDescription());
        this.setting = categorySetting;
        this.enableSetting = booleanSetting;
        float f = booleanSetting != null && booleanSetting.getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(f, f, 180, Easing.Method.EASE_OUT_CUBIC);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Object object;
        LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        float f2 = this.fillAnim.get(this.enableSetting != null && this.enableSetting.getValue() ? 1.0f : 0.0f);
        if (f2 > 0.001f) {
            int n3 = this.getX() + this.getPadding() + 1 + Math.round((float)(this.getWidth() - (this.getPadding() + 1) * 2) * f2);
            object = ClickGuiScreen.getButtonColor(this.getY(), Math.round(100.0f * f2));
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), n3, this.getY() + this.getHeight() - 1, (Color)object);
        }
        Color color = this.enableSetting != null && this.enableSetting.getValue() ? Color.WHITE : ClickGuiScreen.getButtonColor(this.getY(), 180);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), this.getX() + this.getTextPadding() + 4, this.getY() + 2, color);
        object = this.setting.isOpen() ? "..." : "+";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)object, this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth((String)object), this.getY() + 2, Color.WHITE);
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (!this.isHovering(d, d2)) {
            return;
        }
        if (this.enableSetting != null && n == 0) {
            this.enableSetting.setValue(!this.enableSetting.getValue());
            this.playClickSound();
        } else if (n == 1 || this.enableSetting == null && n == 0) {
            this.setting.setOpen(!this.setting.isOpen());
            this.playClickSound();
        }
    }
}

