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

    public NumberButton(NumberSetting numberSetting, Frame frame, int height) {
        super(numberSetting, frame, height, numberSetting.getDescription());
        this.setting = numberSetting;
        this.slideAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
        this.sliderAnim = new Animation(0.0f, 0.0f, 220, Easing.Method.EASE_OUT_CUBIC);
    }

    public void setPrevVisible(boolean prevVisible) {
        this.prevVisible = prevVisible;
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
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        float animatedWidth;
        int slideOffset;
        double valueWidth;
        if (!this.prevVisible) {
            this.resetSlide();
            this.prevVisible = true;
        }
        double trackWidth = this.getWidth() - 2 - this.getPadding() * 2;
        double mouseOffset = Math.min(trackWidth, (double)Math.max(0, mouseX - this.getX() - 1 - this.getPadding()));
        if (this.setting.getType() == NumberSetting.Type.INTEGER) {
            valueWidth = trackWidth * (double)(this.setting.getValue().intValue() - this.setting.getMinimum().intValue()) / (double)(this.setting.getMaximum().intValue() - this.setting.getMinimum().intValue());
            if (this.dragging) {
                int rawInt = (int)MathUtils.round(mouseOffset / trackWidth * (double)(this.setting.getMaximum().intValue() - this.setting.getMinimum().intValue()) + (double)this.setting.getMinimum().intValue(), 0);
                int stepInt = this.setting.getStep().intValue();
                this.setting.setValue(stepInt > 0 ? Math.round((float)rawInt / (float)stepInt) * stepInt : rawInt);
            }
        } else if (this.setting.getType() == NumberSetting.Type.FLOAT) {
            valueWidth = trackWidth * (double)(this.setting.getValue().floatValue() - this.setting.getMinimum().floatValue()) / (double)(this.setting.getMaximum().floatValue() - this.setting.getMinimum().floatValue());
            if (this.dragging) {
                float rawFloat = (float)MathUtils.round(mouseOffset / trackWidth * (double)(this.setting.getMaximum().floatValue() - this.setting.getMinimum().floatValue()) + (double)this.setting.getMinimum().floatValue(), 2);
                float stepFloat = this.setting.getStep().floatValue();
                float steppedFloat = stepFloat > 0.0f ? (float)MathUtils.round((float)Math.round(rawFloat / stepFloat) * stepFloat, 2) : rawFloat;
                this.setting.setValue(Float.valueOf(steppedFloat));
            }
        } else if (this.setting.getType() == NumberSetting.Type.DOUBLE) {
            valueWidth = trackWidth * (this.setting.getValue().doubleValue() - this.setting.getMinimum().doubleValue()) / (this.setting.getMaximum().doubleValue() - this.setting.getMinimum().doubleValue());
            if (this.dragging) {
                double rawDouble = MathUtils.round(mouseOffset / trackWidth * (this.setting.getMaximum().doubleValue() - this.setting.getMinimum().doubleValue()) + this.setting.getMinimum().doubleValue(), 2);
                double stepDouble = this.setting.getStep().doubleValue();
                double steppedDouble = stepDouble > 0.0 ? MathUtils.round((double)Math.round(rawDouble / stepDouble) * stepDouble, 2) : rawDouble;
                this.setting.setValue(steppedDouble);
            }
        } else {
            valueWidth = trackWidth * (double)(this.setting.getValue().longValue() - this.setting.getMinimum().longValue()) / (double)(this.setting.getMaximum().longValue() - this.setting.getMinimum().longValue());
            if (this.dragging) {
                long rawLong = (long)MathUtils.round(mouseOffset / trackWidth * (double)(this.setting.getMaximum().longValue() - this.setting.getMinimum().longValue()) + (double)this.setting.getMinimum().longValue(), 0);
                this.setting.setValue(rawLong);
            }
        }
        float slideProgress = this.slideAnim.get(1.0f);
        slideOffset = Math.round((1.0f - slideProgress) * -20.0f);
        int leftX = this.getX() + this.getPadding() + 1 + slideOffset;
        int rightX = this.getX() + this.getWidth() - this.getPadding() - 1 + slideOffset;
        LightningSkin.fq(guiGraphicsExtractor, leftX, this.getY(), rightX, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 45));
        if (this.isHovering(mouseX, mouseY)) {
            LightningSkin.fq(guiGraphicsExtractor, leftX, this.getY(), rightX, this.getY() + this.getHeight() - 1, new Color(255, 255, 255, 12));
        }
        float targetWidth = (float)Math.max(0.0, Math.min(trackWidth, valueWidth));
        if (this.dragging) {
            this.sliderAnim.setCurrent(targetWidth);
            this.sliderAnim.setPrev(targetWidth);
            animatedWidth = targetWidth;
        } else {
            animatedWidth = this.sliderAnim.get(targetWidth);
        }
        if (animatedWidth > 0.5f) {
            LightningSkin.fq(guiGraphicsExtractor, leftX, this.getY(), (float)leftX + animatedWidth, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 95));
            LightningSkin.fq(guiGraphicsExtractor, (float)leftX + animatedWidth - 1.0f, this.getY(), (float)leftX + animatedWidth + 1.0f, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 230));
        }
        boolean showCursor = System.currentTimeMillis() / 500L % 2L == 0L;
        String cursor = this.selecting ? "" : (showCursor ? "|" : "");
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)(this.listening ? this.currentString + cursor : this.setting.getTag()), leftX + this.getTextPadding() - this.getPadding() + 2, this.getY() + 2, Color.WHITE);
        if (!this.listening) {
            String valueText = this.setting.isZeroIsIgnore() && this.setting.getValue().doubleValue() == 0.0 ? "Ignore" : String.valueOf(this.setting.getValue());
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(ChatFormatting.GRAY) + valueText, rightX - this.getTextPadding() + this.getPadding() - Night.FONT_MANAGER.getWidth(valueText), this.getY() + 2, Color.WHITE);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 0) {
            this.dragging = true;
        }
        if (button == 1) {
            if (this.isHovering(mouseX, mouseY) && !this.listening) {
                this.listening = true;
                this.currentString = "";
            } else {
                this.listening = false;
                this.selecting = false;
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.dragging) {
                this.playClickSound();
            }
            this.dragging = false;
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
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
    public void charTyped(char ch, int modifiers) {
        if (this.listening) {
            this.currentString = this.selecting ? String.valueOf(ch) : this.currentString + ch;
            if (this.selecting) {
                this.selecting = false;
            }
        }
    }
}

