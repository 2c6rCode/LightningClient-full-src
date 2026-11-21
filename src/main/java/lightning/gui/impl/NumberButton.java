/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package lightning.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.chat.ChatUtils;
import lightning.utils.system.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

public class NumberButton
extends Button {
    private final NumberSetting setting;
    private boolean dragging = false;
    private String currentString = "";
    private boolean listening = false;
    private boolean selecting = false;
    private final Animation slideAnim;
    private final Animation sliderAnim;
    private boolean prevVisible = false;

    public NumberButton(NumberSetting numberSetting, Frame frame, int n) {
        super(numberSetting, frame, n, numberSetting.getDescription());
        this.setting = numberSetting;
        this.slideAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
        this.sliderAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
    }

    public void setPrevVisible(boolean bl) {
        this.prevVisible = bl;
    }

    public void resetSlide() {
        this.slideAnim.setCurrent(0.0f);
        this.slideAnim.setPrev(0.0f);
        this.slideAnim.setActiveDuration(220);
        this.slideAnim.setStartTime(System.currentTimeMillis());
        this.sliderAnim.setCurrent(0.0f);
        this.sliderAnim.setPrev(0.0f);
        this.sliderAnim.setActiveDuration(220);
        this.sliderAnim.setStartTime(System.currentTimeMillis());
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        float f2;
        int n3;
        double d;
        if (!this.prevVisible) {
            this.resetSlide();
            this.prevVisible = true;
        }
        double d2 = this.getWidth() - 2 - this.getPadding() * 2;
        double d3 = Math.min(d2, (double)Math.max(0, n - this.getX() - 1 - this.getPadding()));
        if (this.setting.getType() == NumberSetting.Type.INTEGER) {
            d = d2 * (double)(this.setting.getValue().intValue() - this.setting.getMinimum().intValue()) / (double)(this.setting.getMaximum().intValue() - this.setting.getMinimum().intValue());
            if (this.dragging) {
                int n4 = (int)MathUtils.round(d3 / d2 * (double)(this.setting.getMaximum().intValue() - this.setting.getMinimum().intValue()) + (double)this.setting.getMinimum().intValue(), 0);
                n3 = this.setting.getStep().intValue();
                this.setting.setValue(n3 > 0 ? Math.round((float)n4 / (float)n3) * n3 : n4);
            }
        } else if (this.setting.getType() == NumberSetting.Type.FLOAT) {
            d = d2 * (double)(this.setting.getValue().floatValue() - this.setting.getMinimum().floatValue()) / (double)(this.setting.getMaximum().floatValue() - this.setting.getMinimum().floatValue());
            if (this.dragging) {
                float f3 = (float)MathUtils.round(d3 / d2 * (double)(this.setting.getMaximum().floatValue() - this.setting.getMinimum().floatValue()) + (double)this.setting.getMinimum().floatValue(), 2);
                float f4 = this.setting.getStep().floatValue();
                float f5 = f4 > 0.0f ? (float)MathUtils.round((float)Math.round(f3 / f4) * f4, 2) : f3;
                this.setting.setValue(Float.valueOf(f5));
            }
        } else if (this.setting.getType() == NumberSetting.Type.DOUBLE) {
            d = d2 * (this.setting.getValue().doubleValue() - this.setting.getMinimum().doubleValue()) / (this.setting.getMaximum().doubleValue() - this.setting.getMinimum().doubleValue());
            if (this.dragging) {
                double d4 = MathUtils.round(d3 / d2 * (this.setting.getMaximum().doubleValue() - this.setting.getMinimum().doubleValue()) + this.setting.getMinimum().doubleValue(), 2);
                double d5 = this.setting.getStep().doubleValue();
                double d6 = d5 > 0.0 ? MathUtils.round((double)Math.round(d4 / d5) * d5, 2) : d4;
                this.setting.setValue(d6);
            }
        } else {
            d = d2 * (double)(this.setting.getValue().longValue() - this.setting.getMinimum().longValue()) / (double)(this.setting.getMaximum().longValue() - this.setting.getMinimum().longValue());
            if (this.dragging) {
                long l = (long)MathUtils.round(d3 / d2 * (double)(this.setting.getMaximum().longValue() - this.setting.getMinimum().longValue()) + (double)this.setting.getMinimum().longValue(), 0);
                this.setting.setValue(l);
            }
        }
        float f6 = this.slideAnim.get(1.0f);
        n3 = Math.round((1.0f - f6) * -20.0f);
        int n5 = this.getX() + this.getPadding() + 1 + n3;
        int n6 = this.getX() + this.getWidth() - this.getPadding() - 1 + n3;
        LightningSkin.fq(guiGraphicsExtractor, n5, this.getY(), n6, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 45));
        if (this.isHovering(n, n2)) {
            LightningSkin.fq(guiGraphicsExtractor, n5, this.getY(), n6, this.getY() + this.getHeight() - 1, new Color(255, 255, 255, 12));
        }
        float f7 = (float)Math.max(0.0, Math.min(d2, d));
        if (this.dragging) {
            this.sliderAnim.setCurrent(f7);
            this.sliderAnim.setPrev(f7);
            f2 = f7;
        } else {
            f2 = this.sliderAnim.get(f7);
        }
        if (f2 > 0.5f) {
            LightningSkin.fq(guiGraphicsExtractor, n5, this.getY(), (float)n5 + f2, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 95));
            LightningSkin.fq(guiGraphicsExtractor, (float)n5 + f2 - 1.0f, this.getY(), (float)n5 + f2 + 1.0f, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 230));
        }
        boolean bl = System.currentTimeMillis() / 500L % 2L == 0L;
        boolean bl2 = bl;
        String string = this.selecting ? "" : (bl ? "|" : "");
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)(this.listening ? this.currentString + string : this.setting.getTag()), n5 + this.getTextPadding() - this.getPadding() + 2, this.getY() + 2, Color.WHITE);
        if (!this.listening) {
            String string2 = this.setting.isZeroIsIgnore() && this.setting.getValue().doubleValue() == 0.0 ? "Ignore" : String.valueOf(this.setting.getValue());
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(ChatFormatting.GRAY) + string2, n6 - this.getTextPadding() + this.getPadding() - Night.FONT_MANAGER.getWidth(string2), this.getY() + 2, Color.WHITE);
        }
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2) && n == 0) {
            this.dragging = true;
        }
        if (n == 1) {
            if (this.isHovering(d, d2) && !this.listening) {
                this.listening = true;
                this.currentString = "";
            } else {
                this.listening = false;
                this.selecting = false;
            }
        }
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        if (n == 0) {
            if (this.dragging) {
                this.playClickSound();
            }
            this.dragging = false;
        }
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (!this.listening) {
            return;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)256)) {
            this.selecting = false;
            return;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)257)) {
            try {
                switch (this.setting.getType()) {
                    case LONG: {
                        this.setting.setValue(Long.parseLong(this.currentString));
                        break;
                    }
                    case DOUBLE: {
                        this.setting.setValue(Double.parseDouble(this.currentString));
                        break;
                    }
                    case FLOAT: {
                        this.setting.setValue(Float.valueOf(Float.parseFloat(this.currentString)));
                        break;
                    }
                    default: {
                        this.setting.setValue(Integer.parseInt(this.currentString));
                        break;
                    }
                }
            }
            catch (NumberFormatException numberFormatException) {
                Night.CHAT_MANAGER.warn("Please input a valid " + String.valueOf(ChatUtils.getPrimary()) + this.setting.getType().name().toLowerCase() + String.valueOf(ChatUtils.getSecondary()) + " number.");
            }
            this.selecting = false;
            this.listening = false;
            return;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)259)) {
            this.currentString = this.selecting ? "" : (!this.currentString.isEmpty() ? this.currentString.substring(0, this.currentString.length() - 1) : this.currentString);
            this.selecting = false;
            return;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341)) && InputConstants.isKeyDown((Window)mc.getWindow(), (int)65)) {
            this.selecting = true;
        }
    }

    @Override
    public void charTyped(char c, int n) {
        if (this.listening) {
            this.currentString = this.selecting ? String.valueOf(c) : this.currentString + c;
            String string = this.currentString;
            if (this.selecting) {
                this.selecting = false;
            }
        }
    }
}

