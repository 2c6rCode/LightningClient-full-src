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

    public ModeButton(ModeSetting modeSetting, Frame frame, int height) {
        super(modeSetting, frame, height, modeSetting.getDescription());
        this.setting = modeSetting;
        this.lastValue = modeSetting.getValue();
    }

    private float getOpenAmount() {
        return this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        String curText;
        int alpha;
        int slideOffset;
        boolean hovered = this.isHovering(mouseX, mouseY);
        float hoverProgress = this.hoverAnim.get(hovered ? 1.0f : 0.0f);
        float textProgress = this.textAnim.get(hovered ? 1.0f : 0.0f);
        LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, new Color(0, 0, 0, 40));
        if (hoverProgress > 0.001f) {
            Color color = new Color(255, 255, 255, (int)(20.0f * hoverProgress));
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, color);
        }
        float textX = (float)(this.getX() + this.getTextPadding() + 3) + textProgress * 1.5f;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), (int)textX, this.getY() + 2, Color.WHITE);
        String currentValue = this.setting.getValue();
        if (!currentValue.equals(this.lastValue)) {
            this.previousValue = this.lastValue;
            this.lastValue = currentValue;
            this.valueChangeTime = System.currentTimeMillis();
        }
        float slideProgress = this.valueChangeTime == 0L ? 1.0f : Easing.ease(Easing.toDelta(this.valueChangeTime, 180), Easing.Method.EASE_OUT_CUBIC);
        int rightEdge = this.getX() + this.getWidth() - this.getTextPadding() - 1;
        if (slideProgress < 1.0f && this.previousValue != null) {
            slideOffset = Math.round(slideProgress * 10.0f);
            alpha = Math.round(255.0f * (1.0f - slideProgress));
            String prevText = String.valueOf(ChatFormatting.GRAY) + this.previousValue;
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, prevText, rightEdge - Night.FONT_MANAGER.getWidth(prevText) - slideOffset, this.getY() + 2, new Color(255, 255, 255, alpha));
        }
        slideOffset = Math.round((1.0f - slideProgress) * 10.0f);
        alpha = Math.round(255.0f * slideProgress);
        curText = String.valueOf(ChatFormatting.GRAY) + currentValue;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, curText, rightEdge - Night.FONT_MANAGER.getWidth(curText) + slideOffset, this.getY() + 2, new Color(255, 255, 255, alpha));
        float openAmount = this.getOpenAmount();
        if (openAmount > 0.001f) {
            int visibleModes = Mth.clamp((int)Math.round((float)this.setting.getModes().size() * openAmount), (int)0, (int)this.setting.getModes().size());
            int dropdownH = visibleModes * this.getParent().getHeight();
            int dropdownY = this.getY() + this.getParent().getHeight();
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 2, dropdownY, this.getX() + this.getWidth() - this.getPadding() - 2, dropdownY + dropdownH, new Color(11, 11, 11, 210));
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 2, dropdownY, this.getX() + this.getPadding() + 4, dropdownY + dropdownH, ClickGuiScreen.getButtonColor(this.getY(), 160));
            for (int i = 0; i < visibleModes; ++i) {
                String modeName = this.setting.getModes().get(i);
                int itemY = dropdownY + i * this.getParent().getHeight();
                boolean isSelected = this.setting.getValue().equals(modeName);
                boolean itemHovered = mouseX >= this.getX() + this.getPadding() && mouseX <= this.getX() + this.getWidth() - this.getPadding() && mouseY >= itemY && mouseY < itemY + this.getParent().getHeight();
                if (itemHovered) {
                    LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 4, itemY, this.getX() + this.getWidth() - this.getPadding() - 2, itemY + this.getParent().getHeight() - 1, new Color(255, 255, 255, 18));
                }
                if (isSelected) {
                    LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 4, itemY, this.getX() + this.getWidth() - this.getPadding() - 2, itemY + this.getParent().getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 45));
                }
                Color color = isSelected ? Color.WHITE : (itemHovered ? new Color(220, 220, 230) : new Color(150, 150, 160));
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (isSelected ? "\u2022 " : "  ") + modeName, this.getX() + this.getTextPadding() + 5, itemY + 2, color);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                int idx = this.setting.getModes().indexOf(this.setting.getValue());
                if (idx < this.setting.getModes().size() - 1) {
                    this.setting.setValue(this.setting.getModes().get(idx + 1));
                } else {
                    this.setting.setValue(this.setting.getModes().get(0));
                }
                this.playClickSound();
            } else if (button == 1) {
                this.open = !this.open;
                this.playClickSound();
            }
        }
        if (this.open) {
            float openAmount = this.getOpenAmount();
            int visibleModes = Mth.clamp((int)Math.round((float)this.setting.getModes().size() * openAmount), (int)0, (int)this.setting.getModes().size());
            for (int i = 0; i < visibleModes; ++i) {
                String modeName = this.setting.getModes().get(i);
                int itemY = this.getY() + this.getParent().getHeight() + i * this.getParent().getHeight();
                boolean itemHovered = mouseX >= (double)(this.getX() + this.getPadding()) && mouseX <= (double)(this.getX() + this.getWidth() - this.getPadding()) && mouseY >= (double)itemY && mouseY < (double)(itemY + this.getParent().getHeight());
                if (!itemHovered || button != 0) continue;
                this.setting.setValue(modeName);
                this.playClickSound();
            }
        }
    }

    @Override
    public int getHeight() {
        return Math.round((float)this.getParent().getHeight() + (float)(this.setting.getModes().size() * this.getParent().getHeight()) * this.getOpenAmount());
    }
}

