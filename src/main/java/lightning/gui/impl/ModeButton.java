/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 */
package lightning.gui.impl;

import java.awt.Color;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.ModeSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class ModeButton
extends Button {
    private final ModeSetting setting;
    private boolean open = false;
    private final Animation openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);
    private final Animation hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private final Animation textAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private static final int VALUE_SLIDE_MS = 180;
    private static final int VALUE_SLIDE_DISTANCE = 10;
    private String lastValue;
    private String previousValue;
    private long valueChangeTime;

    public ModeButton(ModeSetting modeSetting, Frame frame, int n) {
        super(modeSetting, frame, n, modeSetting.getDescription());
        this.setting = modeSetting;
        this.lastValue = modeSetting.getValue();
    }

    private float getOpenAmount() {
        return this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        String string;
        int n3;
        int n4;
        boolean bl = this.isHovering(n, n2);
        float f2 = this.hoverAnim.get(bl ? 1.0f : 0.0f);
        float f3 = this.textAnim.get(bl ? 1.0f : 0.0f);
        LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, new Color(0, 0, 0, 40));
        if (f2 > 0.001f) {
            Color color = new Color(255, 255, 255, (int)(20.0f * f2));
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, color);
        }
        float f4 = (float)(this.getX() + this.getTextPadding() + 3) + f3 * 1.5f;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), (int)f4, this.getY() + 2, Color.WHITE);
        String string2 = this.setting.getValue();
        if (!string2.equals(this.lastValue)) {
            this.previousValue = this.lastValue;
            this.lastValue = string2;
            this.valueChangeTime = System.currentTimeMillis();
        }
        float f5 = this.valueChangeTime == 0L ? 1.0f : Easing.ease(Easing.toDelta(this.valueChangeTime, 180), Easing.Method.EASE_OUT_CUBIC);
        int n5 = this.getX() + this.getWidth() - this.getTextPadding() - 1;
        if (f5 < 1.0f && this.previousValue != null) {
            n4 = Math.round(f5 * 10.0f);
            n3 = Math.round(255.0f * (1.0f - f5));
            string = String.valueOf(ChatFormatting.GRAY) + this.previousValue;
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n5 - Night.FONT_MANAGER.getWidth(string) - n4, this.getY() + 2, new Color(255, 255, 255, n3));
        }
        n4 = Math.round((1.0f - f5) * 10.0f);
        n3 = Math.round(255.0f * f5);
        string = String.valueOf(ChatFormatting.GRAY) + string2;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n5 - Night.FONT_MANAGER.getWidth(string) + n4, this.getY() + 2, new Color(255, 255, 255, n3));
        float f6 = this.getOpenAmount();
        if (f6 > 0.001f) {
            int n6 = Mth.clamp((int)Math.round((float)this.setting.getModes().size() * f6), (int)0, (int)this.setting.getModes().size());
            int n7 = n6 * this.getParent().getHeight();
            int n8 = this.getY() + this.getParent().getHeight();
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 2, n8, this.getX() + this.getWidth() - this.getPadding() - 2, n8 + n7, new Color(11, 11, 11, 210));
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 2, n8, this.getX() + this.getPadding() + 4, n8 + n7, ClickGuiScreen.getButtonColor(this.getY(), 160));
            for (int i = 0; i < n6; ++i) {
                String string3 = this.setting.getModes().get(i);
                int n9 = n8 + i * this.getParent().getHeight();
                boolean bl2 = this.setting.getValue().equals(string3);
                boolean bl3 = n >= this.getX() + this.getPadding() && n <= this.getX() + this.getWidth() - this.getPadding() && n2 >= n9 && n2 < n9 + this.getParent().getHeight();
                boolean bl4 = bl3;
                if (bl3) {
                    LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 4, n9, this.getX() + this.getWidth() - this.getPadding() - 2, n9 + this.getParent().getHeight() - 1, new Color(255, 255, 255, 18));
                }
                if (bl2) {
                    LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 4, n9, this.getX() + this.getWidth() - this.getPadding() - 2, n9 + this.getParent().getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 45));
                }
                Color color = bl2 ? Color.WHITE : (bl3 ? new Color(220, 220, 230) : new Color(150, 150, 160));
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (bl2 ? "\u2022 " : "  ") + string3, this.getX() + this.getTextPadding() + 5, n9 + 2, color);
            }
        }
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2)) {
            if (n == 0) {
                int n2 = this.setting.getModes().indexOf(this.setting.getValue());
                if (n2 < this.setting.getModes().size() - 1) {
                    this.setting.setValue(this.setting.getModes().get(n2 + 1));
                } else {
                    this.setting.setValue(this.setting.getModes().get(0));
                }
                this.playClickSound();
            } else if (n == 1) {
                this.open = !this.open;
                this.playClickSound();
            }
        }
        if (this.open) {
            float f = this.getOpenAmount();
            int n3 = Mth.clamp((int)Math.round((float)this.setting.getModes().size() * f), (int)0, (int)this.setting.getModes().size());
            for (int i = 0; i < n3; ++i) {
                String string = this.setting.getModes().get(i);
                int n4 = this.getY() + this.getParent().getHeight() + i * this.getParent().getHeight();
                boolean bl = d >= (double)(this.getX() + this.getPadding()) && d <= (double)(this.getX() + this.getWidth() - this.getPadding()) && d2 >= (double)n4 && d2 < (double)(n4 + this.getParent().getHeight());
                boolean bl2 = bl;
                if (!bl || n != 0) continue;
                this.setting.setValue(string);
                this.playClickSound();
            }
        }
    }

    @Override
    public int getHeight() {
        return Math.round((float)this.getParent().getHeight() + (float)(this.setting.getModes().size() * this.getParent().getHeight()) * this.getOpenAmount());
    }
}

