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

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        if (this.visible) {
            String string;
            int n3 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int n4 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int n5 = (n3 - this.width) / 2;
            int n6 = n4 - this.height - 8;
            if (this.cursorTimer.hasTimeElapsed(400L)) {
                this.showCursor = !this.showCursor;
                this.cursorTimer.reset();
            }
            LightningSkin.fq(guiGraphicsExtractor, n5, n6, n5 + this.width, n6 + this.height, new Color(14, 14, 14, 230));
            Color color = this.focused ? ClickGuiScreen.getButtonColor(n6, 220) : ClickGuiScreen.getButtonColor(n6, 90);
            LightningSkin.ol(guiGraphicsExtractor, n5, n6, n5 + this.width, n6 + this.height, color);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "\u2315", n5 + 5, n6 + 4, Color.WHITE);
            int n7 = 18;
            if (this.query.isEmpty() && !this.focused) {
                string = "[ESC]";
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n5 + this.width - Night.FONT_MANAGER.getWidth(string) - 4, n6 + 4, new Color(120, 120, 130));
            }
            if (this.query.isEmpty() && !this.focused) {
                string = "Search modules...";
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n5 + n7, n6 + 4, new Color(140, 140, 150));
            } else {
                string = this.focused && this.showCursor ? "|" : "";
                String string2 = this.query.substring(0, this.cursorIndex);
                String string3 = this.query.substring(this.cursorIndex);
                String string4 = string2 + string + string3;
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string4, n5 + n7, n6 + 4, Color.WHITE);
            }
        }
    }

    public boolean mouseClicked(double d, double d2, int n) {
        if (!this.visible) {
            return false;
        }
        int n2 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int n3 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int n4 = (n2 - this.width) / 2;
        int n5 = n3 - this.height - 6;
        if (n == 0 && d >= (double)n4 && d <= (double)(n4 + this.width) && d2 >= (double)n5 && d2 <= (double)(n5 + this.height)) {
            this.focused = true;
            return true;
        }
        this.focused = false;
        return false;
    }

    public boolean keyPressed(int n, int n2, int n3) {
        if (!this.visible || !this.focused) {
            return false;
        }
        Window window = Minecraft.getInstance().getWindow();
        boolean bl = InputConstants.isKeyDown((Window)window, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (n == 256) {
            if (!this.query.isEmpty()) {
                this.query = "";
                this.cursorIndex = 0;
                return true;
            }
            this.visible = false;
            this.focused = false;
            return true;
        }
        if (n == 259) {
            if (!this.query.isEmpty() && this.cursorIndex > 0) {
                this.query = this.query.substring(0, this.cursorIndex - 1) + this.query.substring(this.cursorIndex);
                --this.cursorIndex;
            }
            return true;
        }
        if (n == 261) {
            if (this.cursorIndex < this.query.length()) {
                this.query = this.query.substring(0, this.cursorIndex) + this.query.substring(this.cursorIndex + 1);
            }
            return true;
        }
        if (n == 263) {
            if (this.cursorIndex > 0) {
                --this.cursorIndex;
            }
            return true;
        }
        if (n == 262) {
            if (this.cursorIndex < this.query.length()) {
                ++this.cursorIndex;
            }
            return true;
        }
        if (n == 268) {
            this.cursorIndex = 0;
            return true;
        }
        if (n == 269) {
            this.cursorIndex = this.query.length();
            return true;
        }
        if (bl && n == 86) {
            try {
                String string = Minecraft.getInstance().keyboardHandler.getClipboard();
                this.query = this.query.substring(0, this.cursorIndex) + string + this.query.substring(this.cursorIndex);
                this.cursorIndex += string.length();
            }
            catch (Exception exception) {
                // empty catch block
            }
            return true;
        }
        if (bl && n == 65) {
            this.cursorIndex = this.query.length();
            return true;
        }
        return true;
    }

    public boolean charTyped(char c, int n) {
        if (!this.visible || !this.focused) {
            return false;
        }
        if (Character.isISOControl(c)) {
            return false;
        }
        this.query = this.query.substring(0, this.cursorIndex) + c + this.query.substring(this.cursorIndex);
        ++this.cursorIndex;
        return true;
    }

    public void toggle() {
        this.visible = !this.visible;
        boolean bl = this.visible;
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

    public boolean matches(String string) {
        if (this.query.isEmpty()) {
            return true;
        }
        return string.toLowerCase().contains(this.query.toLowerCase());
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
    public void setQuery(String string) {
        this.query = string;
    }

    @Generated
    public void setVisible(boolean bl) {
        this.visible = bl;
    }

    @Generated
    public void setFocused(boolean bl) {
        this.focused = bl;
    }

    @Generated
    public void setCursorIndex(int n) {
        this.cursorIndex = n;
    }

    @Generated
    public void setShowCursor(boolean bl) {
        this.showCursor = bl;
    }

    @Generated
    public void setWidth(int n) {
        this.width = n;
    }

    @Generated
    public void setHeight(int n) {
        this.height = n;
    }
}

