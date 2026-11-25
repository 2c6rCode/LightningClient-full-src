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
import lightning.settings.impl.BindSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.input.KeyboardUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BindButton
extends Button {
    private final BindSetting setting;
    private boolean listening = false;
    private final Animation hoverAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private final Animation listenAnim = new Animation(150, Easing.Method.EASE_OUT_CUBIC);
    private static int listenersActive = 0;

    public static boolean isAnyListening() {
        return listenersActive > 0;
    }

    public BindButton(BindSetting bindSetting, Frame frame, int height) {
        super(bindSetting, frame, height, bindSetting.getDescription());
        this.setting = bindSetting;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        float hoverProgress = this.hoverAnim.get(hovered ? 1.0f : 0.0f);
        float listenProgress = this.listenAnim.get(this.listening ? 1.0f : 0.0f);
        LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        if (hoverProgress > 0.001f || listenProgress > 0.001f) {
            int bgAlpha = Math.min(255, (int)(20.0f * hoverProgress + 50.0f * listenProgress));
            Color color = this.listening ? ClickGuiScreen.getButtonColor(this.getY(), bgAlpha) : new Color(255, 255, 255, bgAlpha);
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, color);
        }
        String label = this.setting.getMode().equals("Bind") ? this.setting.getTag() : this.setting.getMode();
        float textX = (float)(this.getX() + this.getTextPadding() + 3) + hoverProgress * 1.5f;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, label, (int)textX, this.getY() + 2, Color.WHITE);
        String keyLabel = this.listening ? "..." : KeyboardUtils.getKeyName(this.setting.getValue());
        Color color = this.listening ? Color.YELLOW : Color.WHITE;
        int keyX = this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth(keyLabel) - (int)(listenProgress * 2.0f);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(this.listening ? ChatFormatting.YELLOW : ChatFormatting.GRAY) + keyLabel, keyX, this.getY() + 2, color);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isHovering(mouseX, mouseY)) {
            return;
        }
        if (this.listening) {
            if (button >= 0 && button <= 4) {
                this.setting.setValue(-button - 1);
                this.stopListening();
            }
            return;
        }
        if (button == 0) {
            this.startListening();
            this.playClickSound();
        } else if (button == 1) {
            this.cycleMode();
            this.playClickSound();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.listening) {
            return;
        }
        if (keyCode == 261 || keyCode == 256) {
            this.setting.setValue(0);
            this.stopListening();
            return;
        }
        this.setting.setValue(keyCode);
        this.stopListening();
    }

    public void keyPressed(int keyCode) {
        this.keyPressed(keyCode, 0, 0);
    }

    private void startListening() {
        this.listening = true;
        ++listenersActive;
    }

    private void stopListening() {
        if (this.listening) {
            this.listening = false;
            listenersActive = Math.max(0, listenersActive - 1);
        }
    }

    private void cycleMode() {
        switch (this.setting.getMode()) {
            case "Bind": {
                this.setting.setMode("Hold");
                break;
            }
            case "Hold": {
                this.setting.setMode("ReverseHold");
                break;
            }
            case "ReverseHold": {
                this.setting.setMode("Bind");
            }
        }
    }
}

