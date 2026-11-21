/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package lightning.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.modules.impl.core.ColorModule;
import lightning.settings.impl.ColorSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class ColorButton
extends Button {
    private final ColorSetting setting;
    private boolean open = false;
    private boolean hoveringHex = false;
    private boolean hoveringHue = false;
    private boolean hoveringColor = false;
    private boolean hoveringAlpha = false;
    private boolean hoveringCopy = false;
    private boolean hoveringPaste = false;
    private boolean hoveringSync = false;
    private boolean hoveringRainbow = false;
    private boolean draggingHue = false;
    private boolean draggingColor = false;
    private boolean draggingAlpha = false;
    private boolean listeningHex = false;
    private String hexInput = "";
    private int hexCursor = 0;
    private int colorWidth = 84;
    private int totalHeight = 0;
    private float[] hsb;

    public ColorButton(ColorSetting colorSetting, Frame frame, int n) {
        super(colorSetting, frame, n, colorSetting.getDescription());
        this.setting = colorSetting;
        this.hsb = Color.RGBtoHSB(colorSetting.getColor().getRed(), colorSetting.getColor().getGreen(), colorSetting.getColor().getBlue(), null);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Color color;
        if (!(this.draggingHue || this.draggingColor || this.listeningHex)) {
            color = this.setting.getValue().getColor();
            this.hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        }
        color = Color.BLACK;
        Color color2 = Color.getHSBColor(this.hsb[0], 1.0f, 1.0f);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, new Color(0, 0, 0, 40));
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), this.getX() + this.getTextPadding() + 3, this.getY() + 2, Color.WHITE);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + 2, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 3, ColorUtils.getColor(this.setting.getColor(), 255));
        Renderer2D.renderOutline(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + 2, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 3, color);
        if (this.open) {
            Color color3;
            int n3;
            int n4 = this.getParent().getHeight();
            int n5 = 13;
            int n6 = this.getX() + this.getPadding() + 1;
            int n7 = this.getX() + this.getWidth() - this.getPadding() - 1;
            int n8 = this.getY() + n4;
            int n9 = n8 + n5;
            Renderer2D.renderCheckerboard(guiGraphicsExtractor, n6, n8, n7, n9, 4.0f, new Color(210, 210, 210), Color.WHITE);
            Renderer2D.renderOutline(guiGraphicsExtractor, n6, n8, n7, n9, color);
            String string = Night.CLICK_GUI.isShowLine() ? "|" : "";
            String string2 = string;
            if (this.listeningHex) {
                if (this.hexInput.isEmpty()) {
                    String hexStr = this.toHex(this.setting.getValue().getColor(), this.setting.getAlpha());
                    Night.FONT_MANAGER.drawText(guiGraphicsExtractor, "Hex: " + string, n6 + 3, n8 + 2, Color.BLACK);
                    n3 = Night.FONT_MANAGER.getWidth("Hex: ");
                    Night.FONT_MANAGER.drawText(guiGraphicsExtractor, hexStr, n6 + 3 + n3, n8 + 2, new Color(130, 130, 130));
                } else {
                    int n10 = Math.max(0, Math.min(this.hexCursor, this.hexInput.length()));
                    String string3 = this.hexInput.substring(0, n10);
                    String string4 = this.hexInput.substring(n10);
                    String string5 = "Hex: " + string3 + string + string4;
                    Night.FONT_MANAGER.drawText(guiGraphicsExtractor, string5, n6 + 3, n8 + 2, Color.BLACK);
                }
            } else {
                String hexText = "Hex: " + this.toHex(this.setting.getValue().getColor(), this.setting.getAlpha());
                Night.FONT_MANAGER.drawText(guiGraphicsExtractor, hexText, n6 + 3, n8 + 2, Color.BLACK);
            }
            this.hoveringHex = this.isHoveringComponent(n, n2, n6, n8, n7, n9);
            int n11 = Mth.clamp((int)(n - this.getX() - this.getPadding() - 1), (int)0, (int)this.colorWidth);
            n3 = Mth.clamp((int)(n2 - this.getY() - (n4 += n5 + 2)), (int)0, (int)this.colorWidth);
            float f2 = (float)this.colorWidth * this.hsb[0];
            float f3 = (float)this.colorWidth * this.hsb[1];
            float f4 = (float)this.colorWidth * (1.0f - this.hsb[2]);
            float f5 = (float)this.colorWidth * ((float)this.setting.getAlpha() / 255.0f);
            for (float f6 = 0.0f; f6 < (float)this.colorWidth; f6 += 0.5f) {
                Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, (float)(this.getY() + n4) + f6, this.getX() + this.getWidth() - this.getPadding() - 1, (float)(this.getY() + n4) + f6 + 0.5f, Color.getHSBColor(f6 / (float)this.colorWidth, 1.0f, 1.0f));
            }
            Renderer2D.renderOutline(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + n4, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + n4 + this.colorWidth, color);
            this.hoveringHue = this.isHoveringComponent(n, n2, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + n4, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + n4 + this.colorWidth);
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 10, (float)(this.getY() + n4) + f2 - 1.5f, this.getX() + this.getWidth() - this.getPadding(), (float)(this.getY() + n4) + f2 + 1.5f, color);
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, (float)(this.getY() + n4) + f2 - 0.5f, this.getX() + this.getWidth() - this.getPadding() - 1, (float)(this.getY() + n4) + f2 + 0.5f, Color.WHITE);
            if (this.draggingHue) {
                this.setting.setSync(false);
                this.hsb[0] = Math.clamp((float)n3 / (float)this.colorWidth, 0.0f, 1.0f);
                Color color4 = new Color(Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]));
                color3 = new Color(color4.getRed(), color4.getGreen(), color4.getBlue(), this.setting.getAlpha());
                this.setting.setColor(color3);
            }
            Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + n4 + this.colorWidth, Color.WHITE, color2);
            Renderer2D.renderGradient(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + n4 + this.colorWidth, new Color(0, 0, 0, 0), Color.BLACK);
            Renderer2D.renderOutline(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + n4 + this.colorWidth, color);
            this.hoveringColor = this.isHoveringComponent(n, n2, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + n4 + this.colorWidth);
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)(this.getX() + this.getPadding() + 1) + f3 - 1.5f, (float)(this.getY() + n4) + f4 - 1.5f, (float)(this.getX() + this.getPadding() + 1) + f3 + 1.5f, (float)(this.getY() + n4) + f4 + 1.5f, color);
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)(this.getX() + this.getPadding() + 1) + f3 - 0.5f, (float)(this.getY() + n4) + f4 - 0.5f, (float)(this.getX() + this.getPadding() + 1) + f3 + 0.5f, (float)(this.getY() + n4) + f4 + 0.5f, Color.WHITE);
            if (this.draggingColor) {
                this.setting.setSync(false);
                this.hsb[1] = Math.clamp((float)n11 / (float)this.colorWidth, 0.0f, 1.0f);
                this.hsb[2] = Math.clamp(1.0f - (float)n3 / (float)this.colorWidth, 0.0f, 1.0f);
                Color color5 = new Color(Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]));
                color3 = new Color(color5.getRed(), color5.getGreen(), color5.getBlue(), this.setting.getAlpha());
                this.setting.setColor(color3);
            }
            Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + (n4 += this.colorWidth + 2), this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + n4 + 8, Color.BLACK, ColorUtils.getColor(this.setting.getColor(), 255));
            Renderer2D.renderOutline(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + n4 + 8, color);
            this.hoveringAlpha = this.isHoveringComponent(n, n2, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + n4 + 8);
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)(this.getX() + this.getPadding() + 1) + f5 - 1.5f, this.getY() + n4 - 1, (float)(this.getX() + this.getPadding() + 1) + f5 + 1.5f, this.getY() + n4 + 9, color);
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)(this.getX() + this.getPadding() + 1) + f5 - 0.5f, this.getY() + n4, (float)(this.getX() + this.getPadding() + 1) + f5 + 0.5f, this.getY() + n4 + 8, Color.WHITE);
            if (this.draggingAlpha) {
                int n12 = Math.clamp((long)((int)(255.0f * (float)n11 / (float)this.colorWidth)), 0, 255);
                Color color6 = this.setting.getValue().getColor();
                Color color7 = new Color(color6.getRed(), color6.getGreen(), color6.getBlue(), n12);
                this.setting.setColor(color7);
            }
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + (n4 += 10), (float)this.getX() + (float)this.getWidth() / 2.0f - 0.5f, this.getY() + n4 + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Copy", this.getX() + this.getPadding() + this.getWidth() / 4 - 1 - Night.FONT_MANAGER.getWidth("Copy") / 2, this.getY() + n4 + 2, Color.WHITE);
            this.hoveringCopy = this.isHoveringComponent(n, n2, this.getX() + this.getPadding() + 1, this.getY() + n4, (float)this.getX() + (float)this.getWidth() / 2.0f - 0.5f, this.getY() + n4 + this.getParent().getHeight());
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)this.getX() + (float)this.getWidth() / 2.0f + 0.5f, this.getY() + n4, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + n4 + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Paste", this.getX() + this.getWidth() / 2 + this.getWidth() / 4 - 1 - Night.FONT_MANAGER.getWidth("Paste") / 2, this.getY() + n4 + 2, Color.WHITE);
            this.hoveringPaste = this.isHoveringComponent(n, n2, (float)this.getX() + (float)this.getWidth() / 2.0f + 0.5f, this.getY() + n4, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + n4 + this.getParent().getHeight());
            n4 += this.getParent().getHeight() + 1;
            if (this.hasSync()) {
                if (this.setting.isSync()) {
                    Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + n4 + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
                }
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Sync", this.getX() + this.getWidth() / 2 - Night.FONT_MANAGER.getWidth("Sync") / 2, this.getY() + n4 + 2, Color.WHITE);
                this.hoveringSync = this.isHoveringComponent(n, n2, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + n4 + this.getParent().getHeight());
                n4 += this.getParent().getHeight() + 1;
            } else {
                this.hoveringSync = false;
            }
            if (this.setting.isRainbow()) {
                Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + n4 + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            }
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Rainbow", this.getX() + this.getWidth() / 2 - Night.FONT_MANAGER.getWidth("Rainbow") / 2, this.getY() + n4 + 2, Color.WHITE);
            this.hoveringRainbow = this.isHoveringComponent(n, n2, this.getX() + this.getPadding() + 1, this.getY() + n4, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + n4 + this.getParent().getHeight());
        }
    }

    private String toHex(Color color, int n) {
        return String.format("%02X%02X%02X%02X", n, color.getRed(), color.getGreen(), color.getBlue());
    }

    private Color parseHex(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim().replace("#", "");
        try {
            if (string2.length() == 6) {
                int n = Integer.parseInt(string2.substring(0, 2), 16);
                int n2 = Integer.parseInt(string2.substring(2, 4), 16);
                int n3 = Integer.parseInt(string2.substring(4, 6), 16);
                return new Color(n, n2, n3, this.setting.getAlpha());
            }
            if (string2.length() == 8) {
                int n = Integer.parseInt(string2.substring(0, 2), 16);
                int n4 = Integer.parseInt(string2.substring(2, 4), 16);
                int n5 = Integer.parseInt(string2.substring(4, 6), 16);
                int n6 = Integer.parseInt(string2.substring(6, 8), 16);
                return new Color(n4, n5, n6, n);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private void applyHexLive() {
        if (this.hexInput == null || this.hexInput.isEmpty()) {
            return;
        }
        Color color = this.parseHex(this.hexInput);
        if (color != null) {
            this.setting.setSync(false);
            this.setting.setColor(color);
            this.hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        }
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (!this.listeningHex) {
            return;
        }
        Window window = mc.getWindow();
        boolean bl = InputConstants.isKeyDown((Window)window, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (n == 256 || n == 257) {
            this.applyHexLive();
            this.listeningHex = false;
            this.hexInput = "";
            return;
        }
        if (n == 263) {
            if (this.hexCursor > 0) {
                --this.hexCursor;
            }
            return;
        }
        if (n == 262) {
            if (this.hexCursor < this.hexInput.length()) {
                ++this.hexCursor;
            }
            return;
        }
        if (n == 259) {
            if (this.hexCursor > 0 && !this.hexInput.isEmpty()) {
                this.hexInput = this.hexInput.substring(0, this.hexCursor - 1) + this.hexInput.substring(this.hexCursor);
                --this.hexCursor;
                this.applyHexLive();
            }
            return;
        }
        if (n == 261) {
            if (this.hexCursor < this.hexInput.length()) {
                this.hexInput = this.hexInput.substring(0, this.hexCursor) + this.hexInput.substring(this.hexCursor + 1);
                this.applyHexLive();
            }
            return;
        }
        if (bl) {
            if (n == 86) {
                try {
                    String string = ColorButton.mc.keyboardHandler.getClipboard();
                    if (string != null) {
                        string = string.trim().replace("#", "").toUpperCase();
                        StringBuilder stringBuilder = new StringBuilder();
                        for (char c : string.toCharArray()) {
                            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F')) continue;
                            stringBuilder.append(c);
                        }
                        Object object = stringBuilder.toString();
                        if (this.hexInput.length() + ((String)object).length() <= 8) {
                            this.hexInput = this.hexInput.substring(0, this.hexCursor) + (String)object + this.hexInput.substring(this.hexCursor);
                            this.hexCursor += ((String)object).length();
                        } else {
                            this.hexInput = ((String)object).substring(0, Math.min(8, ((String)object).length()));
                            this.hexCursor = this.hexInput.length();
                        }
                        this.applyHexLive();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
            if (n == 67) {
                try {
                    ColorButton.mc.keyboardHandler.setClipboard(this.hexInput);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                return;
            }
        }
    }

    @Override
    public void charTyped(char c, int n) {
        if (!this.listeningHex) {
            return;
        }
        char c2 = Character.toUpperCase(c);
        if ((c2 >= '0' && c2 <= '9' || c2 >= 'A' && c2 <= 'F') && this.hexInput.length() < 8) {
            this.hexInput = this.hexInput.substring(0, this.hexCursor) + c2 + this.hexInput.substring(this.hexCursor);
            ++this.hexCursor;
            this.applyHexLive();
        }
    }

    private boolean hasSync() {
        ColorModule colorModule = Night.MODULE_MANAGER.getModule(ColorModule.class);
        return colorModule == null || this.setting != colorModule.color;
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2) && n == 1) {
            this.open = !this.open;
            boolean bl = this.open;
            if (this.listeningHex) {
                this.applyHexLive();
                this.listeningHex = false;
                this.hexInput = "";
            }
            this.playClickSound();
        }
        if (n == 0) {
            if (this.open && this.hoveringHex) {
                this.listeningHex = true;
                this.hexInput = "";
                this.hexCursor = 0;
                this.playClickSound();
                return;
            }
            if (this.listeningHex) {
                this.applyHexLive();
                this.listeningHex = false;
                this.hexInput = "";
            }
            if (this.hoveringHue) {
                this.draggingHue = true;
            }
            if (this.hoveringColor) {
                this.draggingColor = true;
            }
            if (this.hoveringAlpha) {
                this.draggingAlpha = true;
            }
            if (this.hoveringCopy) {
                Night.CLICK_GUI.setColorClipboard(this.setting.getValue().getColor());
                this.playClickSound();
            }
            if (this.hoveringPaste && Night.CLICK_GUI.getColorClipboard() != null) {
                Color color = Night.CLICK_GUI.getColorClipboard();
                this.setting.setColor(color);
                this.hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                this.playClickSound();
            }
            if (this.hasSync() && this.hoveringSync) {
                this.setting.setSync(!this.setting.isSync());
                this.playClickSound();
            }
            if (this.hoveringRainbow) {
                this.setting.setRainbow(!this.setting.isRainbow());
                this.playClickSound();
            }
        }
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        this.draggingHue = false;
        this.draggingColor = false;
        this.draggingAlpha = false;
    }

    @Override
    public int getHeight() {
        return this.open ? (this.hasSync() ? 166 : 152) : this.getParent().getHeight();
    }

    @Override
    public boolean isHovering(double d, double d2) {
        return (double)(this.getX() + this.getPadding()) <= d && (double)this.getY() <= d2 && (double)(this.getX() + this.getWidth() - this.getPadding()) > d && (double)(this.getY() + this.getParent().getHeight()) > d2;
    }

    private boolean isHoveringComponent(double d, double d2, double d3, double d4, double d5, double d6) {
        return d3 <= d && d4 <= d2 && d5 > d && d6 > d2;
    }
}

