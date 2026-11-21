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

    public BindButton(BindSetting bindSetting, Frame frame, int n) {
        super(bindSetting, frame, n, bindSetting.getDescription());
        this.setting = bindSetting;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        boolean bl = this.isHovering(n, n2);
        float f2 = this.hoverAnim.get(bl ? 1.0f : 0.0f);
        float f3 = this.listenAnim.get(this.listening ? 1.0f : 0.0f);
        LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        if (f2 > 0.001f || f3 > 0.001f) {
            int n3 = Math.min(255, (int)(20.0f * f2 + 50.0f * f3));
            Color color = this.listening ? ClickGuiScreen.getButtonColor(this.getY(), n3) : new Color(255, 255, 255, n3);
            LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, color);
        }
        String string = this.setting.getMode().equals("Bind") ? this.setting.getTag() : this.setting.getMode();
        float f4 = (float)(this.getX() + this.getTextPadding() + 3) + f2 * 1.5f;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, (int)f4, this.getY() + 2, Color.WHITE);
        String string2 = this.listening ? "..." : KeyboardUtils.getKeyName(this.setting.getValue());
        Color color = this.listening ? Color.YELLOW : Color.WHITE;
        int n4 = this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth(string2) - (int)(f3 * 2.0f);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(this.listening ? ChatFormatting.YELLOW : ChatFormatting.GRAY) + string2, n4, this.getY() + 2, color);
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (!this.isHovering(d, d2)) {
            return;
        }
        if (this.listening) {
            if (n >= 0 && n <= 4) {
                this.setting.setValue(-n - 1);
                this.stopListening();
            }
            return;
        }
        if (n == 0) {
            this.startListening();
            this.playClickSound();
        } else if (n == 1) {
            this.cycleMode();
            this.playClickSound();
        }
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (!this.listening) {
            return;
        }
        if (n == 261 || n == 256) {
            this.setting.setValue(0);
            this.stopListening();
            return;
        }
        this.setting.setValue(n);
        this.stopListening();
    }

    public void keyPressed(int n) {
        this.keyPressed(n, 0, 0);
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

