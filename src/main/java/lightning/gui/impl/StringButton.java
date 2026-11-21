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
import lightning.settings.impl.StringSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

public class StringButton
extends Button {
    private final StringSetting setting;
    private String currentString = "";
    private boolean listening = false;
    private boolean selecting = false;
    private int cursorIndex = 0;

    public StringButton(StringSetting stringSetting, Frame frame, int n) {
        super(stringSetting, frame, n, stringSetting.getDescription());
        this.setting = stringSetting;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, new Color(0, 0, 0, 40));
        LightningSkin.fq(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 100));
        Object object = this.currentString;
        if (this.listening) {
            if (this.selecting) {
                object = this.currentString;
            } else {
                int n3 = Math.max(0, Math.min(this.cursorIndex, this.currentString.length()));
                String string = this.currentString.substring(0, n3);
                String string2 = this.currentString.substring(n3);
                String string3 = Night.CLICK_GUI.isShowLine() ? "|" : " ";
                object = string + string3 + string2;
            }
        } else {
            object = this.setting.getTag() + " " + String.valueOf(ChatFormatting.GRAY) + this.setting.getValue();
        }
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)object, this.getX() + this.getTextPadding() + 1, this.getY() + 2, this.selecting ? ClickGuiScreen.getButtonColor(this.getY(), 255) : Color.WHITE);
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (n == 0) {
            if (this.isHovering(d, d2) && !this.listening) {
                this.listening = true;
                this.currentString = this.setting.getValue();
                this.cursorIndex = this.currentString.length();
                this.selecting = false;
                this.playClickSound();
            } else if (!this.isHovering(d, d2)) {
                this.listening = false;
                this.selecting = false;
            }
        }
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (!this.listening) {
            return;
        }
        Window window = mc.getWindow();
        boolean bl = InputConstants.isKeyDown((Window)window, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (n == 256) {
            this.selecting = false;
            return;
        }
        if (n == 257) {
            this.setting.setValue(this.currentString);
            this.selecting = false;
            this.listening = false;
            return;
        }
        if (n == 263 && !this.currentString.isEmpty()) {
            if (this.selecting) {
                this.selecting = false;
                this.cursorIndex = 0;
            } else if (this.cursorIndex > 0) {
                --this.cursorIndex;
            }
            return;
        }
        if (n == 262 && !this.currentString.isEmpty()) {
            if (this.selecting) {
                this.selecting = false;
                this.cursorIndex = this.currentString.length();
            } else if (this.cursorIndex < this.currentString.length()) {
                ++this.cursorIndex;
            }
            return;
        }
        if (n == 259) {
            if (this.selecting) {
                this.currentString = "";
                this.cursorIndex = 0;
                this.selecting = false;
            } else if (this.cursorIndex > 0) {
                this.currentString = this.currentString.substring(0, this.cursorIndex - 1) + this.currentString.substring(this.cursorIndex);
                --this.cursorIndex;
            }
            return;
        }
        if (n == 261) {
            if (this.selecting) {
                this.currentString = "";
                this.cursorIndex = 0;
                this.selecting = false;
            } else if (this.cursorIndex < this.currentString.length()) {
                this.currentString = this.currentString.substring(0, this.cursorIndex) + this.currentString.substring(this.cursorIndex + 1);
            }
            return;
        }
        if (bl) {
            if (n == 86) {
                try {
                    String string = StringButton.mc.keyboardHandler.getClipboard();
                    if (this.selecting) {
                        this.currentString = string;
                        this.cursorIndex = this.currentString.length();
                        this.selecting = false;
                    } else {
                        this.currentString = this.currentString.substring(0, this.cursorIndex) + string + this.currentString.substring(this.cursorIndex);
                        this.cursorIndex += string.length();
                    }
                }
                catch (Exception exception) {
                    Night.LOGGER.error("{}: Failed to process clipboard paste", (Object)exception.getClass().getName(), (Object)exception);
                }
                return;
            }
            if (n == 67 && this.selecting) {
                try {
                    StringButton.mc.keyboardHandler.setClipboard(this.currentString);
                }
                catch (Exception exception) {
                    Night.LOGGER.error("{}: Failed to process clipboard change", (Object)exception.getClass().getName(), (Object)exception);
                }
                return;
            }
            if (n == 65 && !this.currentString.isEmpty()) {
                this.selecting = true;
                this.cursorIndex = this.currentString.length();
            }
        }
    }

    @Override
    public void charTyped(char c, int n) {
        if (!this.listening) {
            return;
        }
        if (Character.isISOControl(c)) {
            return;
        }
        if (this.selecting) {
            this.currentString = String.valueOf(c);
            this.cursorIndex = 1;
            this.selecting = false;
        } else {
            this.currentString = this.currentString.substring(0, this.cursorIndex) + c + this.currentString.substring(this.cursorIndex);
            ++this.cursorIndex;
        }
    }
}

