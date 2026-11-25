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

    public ColorButton(ColorSetting colorSetting, Frame frame, int height) {
        super(colorSetting, frame, height, colorSetting.getDescription());
        this.setting = colorSetting;
        this.hsb = Color.RGBtoHSB(colorSetting.getColor().getRed(), colorSetting.getColor().getGreen(), colorSetting.getColor().getBlue(), null);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        Color outlineColor;
        if (!(this.draggingHue || this.draggingColor || this.listeningHex)) {
            outlineColor = this.setting.getValue().getColor();
            this.hsb = Color.RGBtoHSB(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), null);
        }
        outlineColor = Color.BLACK;
        Color hueColor = Color.getHSBColor(this.hsb[0], 1.0f, 1.0f);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 1, new Color(0, 0, 0, 40));
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), this.getX() + this.getTextPadding() + 3, this.getY() + 2, Color.WHITE);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + 2, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 3, ColorUtils.getColor(this.setting.getColor(), 255));
        Renderer2D.renderOutline(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + 2, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + this.getParent().getHeight() - 3, outlineColor);
        if (this.open) {
            Color selectedColor;
            int offsetY = this.getParent().getHeight();
            int hexBarHeight = 13;
            int hexStartX = this.getX() + this.getPadding() + 1;
            int hexEndX = this.getX() + this.getWidth() - this.getPadding() - 1;
            int hexStartY = this.getY() + offsetY;
            int hexEndY = hexStartY + hexBarHeight;
            Renderer2D.renderCheckerboard(guiGraphicsExtractor, hexStartX, hexStartY, hexEndX, hexEndY, 4.0f, new Color(210, 210, 210), Color.WHITE);
            Renderer2D.renderOutline(guiGraphicsExtractor, hexStartX, hexStartY, hexEndX, hexEndY, outlineColor);
            String caret = Night.CLICK_GUI.isShowLine() ? "|" : "";
            if (this.listeningHex) {
                if (this.hexInput.isEmpty()) {
                    String hexStr = this.toHex(this.setting.getValue().getColor(), this.setting.getAlpha());
                    Night.FONT_MANAGER.drawText(guiGraphicsExtractor, "Hex: " + caret, hexStartX + 3, hexStartY + 2, Color.BLACK);
                    int hexLabelWidth = Night.FONT_MANAGER.getWidth("Hex: ");
                    Night.FONT_MANAGER.drawText(guiGraphicsExtractor, hexStr, hexStartX + 3 + hexLabelWidth, hexStartY + 2, new Color(130, 130, 130));
                } else {
                    int cursorIndex = Math.max(0, Math.min(this.hexCursor, this.hexInput.length()));
                    String beforeCursor = this.hexInput.substring(0, cursorIndex);
                    String afterCursor = this.hexInput.substring(cursorIndex);
                    String displayText = "Hex: " + beforeCursor + caret + afterCursor;
                    Night.FONT_MANAGER.drawText(guiGraphicsExtractor, displayText, hexStartX + 3, hexStartY + 2, Color.BLACK);
                }
            } else {
                String hexText = "Hex: " + this.toHex(this.setting.getValue().getColor(), this.setting.getAlpha());
                Night.FONT_MANAGER.drawText(guiGraphicsExtractor, hexText, hexStartX + 3, hexStartY + 2, Color.BLACK);
            }
            this.hoveringHex = this.isHoveringComponent(mouseX, mouseY, hexStartX, hexStartY, hexEndX, hexEndY);
            int localX = Mth.clamp((int)(mouseX - this.getX() - this.getPadding() - 1), (int)0, (int)this.colorWidth);
            int localY = Mth.clamp((int)(mouseY - this.getY() - (offsetY += hexBarHeight + 2)), (int)0, (int)this.colorWidth);
            float hueHandleY = (float)this.colorWidth * this.hsb[0];
            float satHandleX = (float)this.colorWidth * this.hsb[1];
            float briHandleY = (float)this.colorWidth * (1.0f - this.hsb[2]);
            float alphaHandleX = (float)this.colorWidth * ((float)this.setting.getAlpha() / 255.0f);
            for (float hueStepY = 0.0f; hueStepY < (float)this.colorWidth; hueStepY += 0.5f) {
                Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, (float)(this.getY() + offsetY) + hueStepY, this.getX() + this.getWidth() - this.getPadding() - 1, (float)(this.getY() + offsetY) + hueStepY + 0.5f, Color.getHSBColor(hueStepY / (float)this.colorWidth, 1.0f, 1.0f));
            }
            Renderer2D.renderOutline(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + offsetY, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offsetY + this.colorWidth, outlineColor);
            this.hoveringHue = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getWidth() - this.getPadding() - 9, this.getY() + offsetY, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offsetY + this.colorWidth);
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 10, (float)(this.getY() + offsetY) + hueHandleY - 1.5f, this.getX() + this.getWidth() - this.getPadding(), (float)(this.getY() + offsetY) + hueHandleY + 1.5f, outlineColor);
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getWidth() - this.getPadding() - 9, (float)(this.getY() + offsetY) + hueHandleY - 0.5f, this.getX() + this.getWidth() - this.getPadding() - 1, (float)(this.getY() + offsetY) + hueHandleY + 0.5f, Color.WHITE);
            if (this.draggingHue) {
                this.setting.setSync(false);
                this.hsb[0] = Math.clamp((float)localY / (float)this.colorWidth, 0.0f, 1.0f);
                Color hsbRgb = new Color(Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]));
                selectedColor = new Color(hsbRgb.getRed(), hsbRgb.getGreen(), hsbRgb.getBlue(), this.setting.getAlpha());
                this.setting.setColor(selectedColor);
            }
            Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offsetY + this.colorWidth, Color.WHITE, hueColor);
            Renderer2D.renderGradient(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offsetY + this.colorWidth, new Color(0, 0, 0, 0), Color.BLACK);
            Renderer2D.renderOutline(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offsetY + this.colorWidth, outlineColor);
            this.hoveringColor = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offsetY + this.colorWidth);
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)(this.getX() + this.getPadding() + 1) + satHandleX - 1.5f, (float)(this.getY() + offsetY) + briHandleY - 1.5f, (float)(this.getX() + this.getPadding() + 1) + satHandleX + 1.5f, (float)(this.getY() + offsetY) + briHandleY + 1.5f, outlineColor);
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)(this.getX() + this.getPadding() + 1) + satHandleX - 0.5f, (float)(this.getY() + offsetY) + briHandleY - 0.5f, (float)(this.getX() + this.getPadding() + 1) + satHandleX + 0.5f, (float)(this.getY() + offsetY) + briHandleY + 0.5f, Color.WHITE);
            if (this.draggingColor) {
                this.setting.setSync(false);
                this.hsb[1] = Math.clamp((float)localX / (float)this.colorWidth, 0.0f, 1.0f);
                this.hsb[2] = Math.clamp(1.0f - (float)localY / (float)this.colorWidth, 0.0f, 1.0f);
                Color hsbRgb = new Color(Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]));
                selectedColor = new Color(hsbRgb.getRed(), hsbRgb.getGreen(), hsbRgb.getBlue(), this.setting.getAlpha());
                this.setting.setColor(selectedColor);
            }
            Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + (offsetY += this.colorWidth + 2), this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offsetY + 8, Color.BLACK, ColorUtils.getColor(this.setting.getColor(), 255));
            Renderer2D.renderOutline(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offsetY + 8, outlineColor);
            this.hoveringAlpha = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getPadding() + 1 + this.colorWidth, this.getY() + offsetY + 8);
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)(this.getX() + this.getPadding() + 1) + alphaHandleX - 1.5f, this.getY() + offsetY - 1, (float)(this.getX() + this.getPadding() + 1) + alphaHandleX + 1.5f, this.getY() + offsetY + 9, outlineColor);
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)(this.getX() + this.getPadding() + 1) + alphaHandleX - 0.5f, this.getY() + offsetY, (float)(this.getX() + this.getPadding() + 1) + alphaHandleX + 0.5f, this.getY() + offsetY + 8, Color.WHITE);
            if (this.draggingAlpha) {
                int alphaValue = Math.clamp((long)((int)(255.0f * (float)localX / (float)this.colorWidth)), 0, 255);
                Color currentColor = this.setting.getValue().getColor();
                Color updatedColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), alphaValue);
                this.setting.setColor(updatedColor);
            }
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + (offsetY += 10), (float)this.getX() + (float)this.getWidth() / 2.0f - 0.5f, this.getY() + offsetY + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Copy", this.getX() + this.getPadding() + this.getWidth() / 4 - 1 - Night.FONT_MANAGER.getWidth("Copy") / 2, this.getY() + offsetY + 2, Color.WHITE);
            this.hoveringCopy = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offsetY, (float)this.getX() + (float)this.getWidth() / 2.0f - 0.5f, this.getY() + offsetY + this.getParent().getHeight());
            Renderer2D.renderQuad(guiGraphicsExtractor, (float)this.getX() + (float)this.getWidth() / 2.0f + 0.5f, this.getY() + offsetY, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offsetY + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Paste", this.getX() + this.getWidth() / 2 + this.getWidth() / 4 - 1 - Night.FONT_MANAGER.getWidth("Paste") / 2, this.getY() + offsetY + 2, Color.WHITE);
            this.hoveringPaste = this.isHoveringComponent(mouseX, mouseY, (float)this.getX() + (float)this.getWidth() / 2.0f + 0.5f, this.getY() + offsetY, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offsetY + this.getParent().getHeight());
            offsetY += this.getParent().getHeight() + 1;
            if (this.hasSync()) {
                if (this.setting.isSync()) {
                    Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offsetY + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
                }
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Sync", this.getX() + this.getWidth() / 2 - Night.FONT_MANAGER.getWidth("Sync") / 2, this.getY() + offsetY + 2, Color.WHITE);
                this.hoveringSync = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offsetY + this.getParent().getHeight());
                offsetY += this.getParent().getHeight() + 1;
            } else {
                this.hoveringSync = false;
            }
            if (this.setting.isRainbow()) {
                Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offsetY + this.getParent().getHeight(), ClickGuiScreen.getButtonColor(this.getY(), 100));
            }
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Rainbow", this.getX() + this.getWidth() / 2 - Night.FONT_MANAGER.getWidth("Rainbow") / 2, this.getY() + offsetY + 2, Color.WHITE);
            this.hoveringRainbow = this.isHoveringComponent(mouseX, mouseY, this.getX() + this.getPadding() + 1, this.getY() + offsetY, this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + offsetY + this.getParent().getHeight());
        }
    }

    private String toHex(Color color, int alpha) {
        return String.format("%02X%02X%02X%02X", alpha, color.getRed(), color.getGreen(), color.getBlue());
    }

    private Color parseHex(String hex) {
        if (hex == null) {
            return null;
        }
        String cleaned = hex.trim().replace("#", "");
        try {
            if (cleaned.length() == 6) {
                int red = Integer.parseInt(cleaned.substring(0, 2), 16);
                int green = Integer.parseInt(cleaned.substring(2, 4), 16);
                int blue = Integer.parseInt(cleaned.substring(4, 6), 16);
                return new Color(red, green, blue, this.setting.getAlpha());
            }
            if (cleaned.length() == 8) {
                int alpha = Integer.parseInt(cleaned.substring(0, 2), 16);
                int red = Integer.parseInt(cleaned.substring(2, 4), 16);
                int green = Integer.parseInt(cleaned.substring(4, 6), 16);
                int blue = Integer.parseInt(cleaned.substring(6, 8), 16);
                return new Color(red, green, blue, alpha);
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
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.listeningHex) {
            return;
        }
        Window window = mc.getWindow();
        boolean ctrlHeld = InputConstants.isKeyDown((Window)window, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (keyCode == 256 || keyCode == 257) {
            this.applyHexLive();
            this.listeningHex = false;
            this.hexInput = "";
            return;
        }
        if (keyCode == 263) {
            if (this.hexCursor > 0) {
                --this.hexCursor;
            }
            return;
        }
        if (keyCode == 262) {
            if (this.hexCursor < this.hexInput.length()) {
                ++this.hexCursor;
            }
            return;
        }
        if (keyCode == 259) {
            if (this.hexCursor > 0 && !this.hexInput.isEmpty()) {
                this.hexInput = this.hexInput.substring(0, this.hexCursor - 1) + this.hexInput.substring(this.hexCursor);
                --this.hexCursor;
                this.applyHexLive();
            }
            return;
        }
        if (keyCode == 261) {
            if (this.hexCursor < this.hexInput.length()) {
                this.hexInput = this.hexInput.substring(0, this.hexCursor) + this.hexInput.substring(this.hexCursor + 1);
                this.applyHexLive();
            }
            return;
        }
        if (ctrlHeld) {
            if (keyCode == 86) {
                try {
                    String clipboard = ColorButton.mc.keyboardHandler.getClipboard();
                    if (clipboard != null) {
                        clipboard = clipboard.trim().replace("#", "").toUpperCase();
                        StringBuilder filteredBuilder = new StringBuilder();
                        for (char ch : clipboard.toCharArray()) {
                            if (!(ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'F')) continue;
                            filteredBuilder.append(ch);
                        }
                        String filtered = filteredBuilder.toString();
                        if (this.hexInput.length() + filtered.length() <= 8) {
                            this.hexInput = this.hexInput.substring(0, this.hexCursor) + filtered + this.hexInput.substring(this.hexCursor);
                            this.hexCursor += filtered.length();
                        } else {
                            this.hexInput = filtered.substring(0, Math.min(8, filtered.length()));
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
            if (keyCode == 67) {
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
    public void charTyped(char typedChar, int modifiers) {
        if (!this.listeningHex) {
            return;
        }
        char upperChar = Character.toUpperCase(typedChar);
        if ((upperChar >= '0' && upperChar <= '9' || upperChar >= 'A' && upperChar <= 'F') && this.hexInput.length() < 8) {
            this.hexInput = this.hexInput.substring(0, this.hexCursor) + upperChar + this.hexInput.substring(this.hexCursor);
            ++this.hexCursor;
            this.applyHexLive();
        }
    }

    private boolean hasSync() {
        ColorModule colorModule = Night.MODULE_MANAGER.getModule(ColorModule.class);
        return colorModule == null || this.setting != colorModule.color;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 1) {
            this.open = !this.open;
            if (this.listeningHex) {
                this.applyHexLive();
                this.listeningHex = false;
                this.hexInput = "";
            }
            this.playClickSound();
        }
        if (button == 0) {
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
    public void mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingHue = false;
        this.draggingColor = false;
        this.draggingAlpha = false;
    }

    @Override
    public int getHeight() {
        return this.open ? (this.hasSync() ? 166 : 152) : this.getParent().getHeight();
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        return (double)(this.getX() + this.getPadding()) <= mouseX && (double)this.getY() <= mouseY && (double)(this.getX() + this.getWidth() - this.getPadding()) > mouseX && (double)(this.getY() + this.getParent().getHeight()) > mouseY;
    }

    private boolean isHoveringComponent(double mouseX, double mouseY, double minX, double minY, double maxX, double maxY) {
        return minX <= mouseX && minY <= mouseY && maxX > mouseX && maxY > mouseY;
    }
}

