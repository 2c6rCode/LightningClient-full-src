/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  lombok.Generated
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package lightning.gui.api;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;

public class SearchFrame {
    private String query = "";
    private boolean visible = false;
    private boolean focused = false;
    private int cursorIndex = 0;
    private final Timer cursorTimer = new Timer();
    private boolean showCursor = true;
    private int width = 190;
    private int height = 16;

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int boxX = (screenWidth - this.width) / 2;
            int boxY = screenHeight - this.height - 8;
            if (this.cursorTimer.hasTimeElapsed(400L)) {
                this.showCursor = !this.showCursor;
                this.cursorTimer.reset();
            }
            LightningSkin.fq(guiGraphicsExtractor, boxX, boxY, boxX + this.width, boxY + this.height, new Color(14, 14, 14, 230));
            Color color = this.focused ? ClickGuiScreen.getButtonColor(boxY, 220) : ClickGuiScreen.getButtonColor(boxY, 90);
            LightningSkin.ol(guiGraphicsExtractor, boxX, boxY, boxX + this.width, boxY + this.height, color);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "\u2315", boxX + 5, boxY + 4, Color.WHITE);
            int textOffset = 18;
            if (this.query.isEmpty() && !this.focused) {
                String escHint = "[ESC]";
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, escHint, boxX + this.width - Night.FONT_MANAGER.getWidth(escHint) - 4, boxY + 4, new Color(120, 120, 130));
            }
            if (this.query.isEmpty() && !this.focused) {
                String placeholder = "Search modules...";
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, placeholder, boxX + textOffset, boxY + 4, new Color(140, 140, 150));
            } else {
                String cursor = this.focused && this.showCursor ? "|" : "";
                String beforeCursor = this.query.substring(0, this.cursorIndex);
                String afterCursor = this.query.substring(this.cursorIndex);
                String displayText = beforeCursor + cursor + afterCursor;
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, displayText, boxX + textOffset, boxY + 4, Color.WHITE);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) {
            return false;
        }
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int boxX = (screenWidth - this.width) / 2;
        int boxY = screenHeight - this.height - 6;
        if (button == 0 && mouseX >= (double)boxX && mouseX <= (double)(boxX + this.width) && mouseY >= (double)boxY && mouseY <= (double)(boxY + this.height)) {
            this.focused = true;
            return true;
        }
        this.focused = false;
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.visible || !this.focused) {
            return false;
        }
        Window window = Minecraft.getInstance().getWindow();
        boolean ctrlDown = InputConstants.isKeyDown((Window)window, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (keyCode == 256) {
            if (!this.query.isEmpty()) {
                this.query = "";
                this.cursorIndex = 0;
                return true;
            }
            this.visible = false;
            this.focused = false;
            return true;
        }
        if (keyCode == 259) {
            if (!this.query.isEmpty() && this.cursorIndex > 0) {
                this.query = this.query.substring(0, this.cursorIndex - 1) + this.query.substring(this.cursorIndex);
                --this.cursorIndex;
            }
            return true;
        }
        if (keyCode == 261) {
            if (this.cursorIndex < this.query.length()) {
                this.query = this.query.substring(0, this.cursorIndex) + this.query.substring(this.cursorIndex + 1);
            }
            return true;
        }
        if (keyCode == 263) {
            if (this.cursorIndex > 0) {
                --this.cursorIndex;
            }
            return true;
        }
        if (keyCode == 262) {
            if (this.cursorIndex < this.query.length()) {
                ++this.cursorIndex;
            }
            return true;
        }
        if (keyCode == 268) {
            this.cursorIndex = 0;
            return true;
        }
        if (keyCode == 269) {
            this.cursorIndex = this.query.length();
            return true;
        }
        if (ctrlDown && keyCode == 86) {
            try {
                String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
                this.query = this.query.substring(0, this.cursorIndex) + clipboard + this.query.substring(this.cursorIndex);
                this.cursorIndex += clipboard.length();
            }
            catch (Exception exception) {
                // empty catch block
            }
            return true;
        }
        if (ctrlDown && keyCode == 65) {
            this.cursorIndex = this.query.length();
            return true;
        }
        return true;
    }

    public boolean charTyped(char ch, int modifiers) {
        if (!this.visible || !this.focused) {
            return false;
        }
        if (Character.isISOControl(ch)) {
            return false;
        }
        this.query = this.query.substring(0, this.cursorIndex) + ch + this.query.substring(this.cursorIndex);
        ++this.cursorIndex;
        return true;
    }

    public void toggle() {
        this.visible = !this.visible;
        if (this.visible) {
            this.focused = true;
            this.query = "";
            this.cursorIndex = 0;
        } else {
            this.focused = false;
        }
    }

    public boolean isSearching() {
        return !this.query.isEmpty();
    }

    public boolean matches(String text) {
        if (this.query.isEmpty()) {
            return true;
        }
        return text.toLowerCase().contains(this.query.toLowerCase());
    }

    @Generated
    public String getQuery() {
        return this.query;
    }

    @Generated
    public boolean isVisible() {
        return this.visible;
    }

    @Generated
    public boolean isFocused() {
        return this.focused;
    }

    @Generated
    public int getCursorIndex() {
        return this.cursorIndex;
    }

    @Generated
    public Timer getCursorTimer() {
        return this.cursorTimer;
    }

    @Generated
    public boolean isShowCursor() {
        return this.showCursor;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public void setQuery(String query) {
        this.query = query;
    }

    @Generated
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Generated
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Generated
    public void setCursorIndex(int cursorIndex) {
        this.cursorIndex = cursorIndex;
    }

    @Generated
    public void setShowCursor(boolean showCursor) {
        this.showCursor = showCursor;
    }

    @Generated
    public void setWidth(int width) {
        this.width = width;
    }

    @Generated
    public void setHeight(int height) {
        this.height = height;
    }
}

