/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  lombok.Generated
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package lightning.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.WhitelistEditorScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.minecraft.IdentifierUtils;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;

public class WhitelistButton
extends Button {
    private final WhitelistSetting setting;
    private boolean open = false;
    private String searchQuery = "";
    private boolean searching = false;
    private boolean selecting = false;
    private int cursorPos = 0;
    private String oldSearchQuery = "";
    private boolean draggingScrollbar = false;
    private int dragStartY = 0;
    private float dragStartScroll = 0.0f;
    private final List<String> allElements;
    private List<String> visibleElements = new ArrayList<String>();
    private final int maxDisplayed = 6;
    private float scrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;
    private final int searchBarHeight = 12;
    private final int elementHeight = 12;
    private final int offsetX = 2;
    private final int scrollbarWidth = 5;
    private final int symbolWidth = 15;

    public WhitelistButton(WhitelistSetting whitelistSetting, Frame frame, int n) {
        super(whitelistSetting, frame, n, whitelistSetting.getDescription());
        this.setting = whitelistSetting;
        this.allElements = this.getAllElements();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        boolean bl = !this.searchQuery.equals(this.oldSearchQuery);
        this.oldSearchQuery = this.searchQuery;
        float f2 = 0.0f;
        if (bl) {
            f2 = this.getScrollRatio();
        }
        this.updateVisibleElements();
        this.clampScrollValues();
        if (bl) {
            this.applyScrollRatio(f2);
            this.clampScrollValues();
        }
        this.scrollOffset += (this.targetScrollOffset - this.scrollOffset) * 0.5f;
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + 13 - 1, ClickGuiScreen.getButtonColor(this.getY(), 100));
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getPadding() + 2, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 30));
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), this.getX() + this.getTextPadding() + 1, this.getY() + 2, Color.WHITE);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(ChatFormatting.GRAY) + this.setting.getWhitelist().size(), this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth("" + this.setting.getWhitelist().size()), this.getY() + 2, Color.WHITE);
        if (this.open) {
            int n3;
            String string;
            int n4 = this.getY() + this.getParent().getHeight();
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1 + 2, n4, this.getX() + this.getWidth() - this.getPadding() - 1, n4 + 12, ClickGuiScreen.getButtonColor(this.getY(), 30));
            Object object = this.searchQuery;
            if (this.searching) {
                string = Night.CLICK_GUI.isShowLine() ? "|" : " ";
                String string2 = string;
                if (this.cursorPos <= ((String)object).length()) {
                    object = ((String)object).substring(0, this.cursorPos) + string + ((String)object).substring(this.cursorPos);
                }
            } else {
                object = "Search...";
            }
            if (this.searching && this.searchQuery.isEmpty()) {
                string = Night.CLICK_GUI.isShowLine() ? "|" : " ";
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, this.getX() + this.getTextPadding() + 2 + 2, n4 + 6 - Night.FONT_MANAGER.getHeight() / 2, Color.WHITE);
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Search...", this.getX() + this.getTextPadding() + 2 + 2 + Night.FONT_MANAGER.getWidth(" "), n4 + 6 - Night.FONT_MANAGER.getHeight() / 2, Color.GRAY);
            } else {
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)object, this.getX() + this.getTextPadding() + 2 + 2, n4 + 6 - Night.FONT_MANAGER.getHeight() / 2, this.searching ? (this.selecting ? ClickGuiScreen.getButtonColor(this.getY(), 255) : Color.WHITE) : Color.GRAY);
            }
            int n5 = n4 + 12;
            int n6 = Math.min(this.visibleElements.size(), 6);
            int n7 = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
            int n8 = 72;
            List<String> list = this.setting.getWhitelistIds();
            String[] stringArray = this.searchQuery.isEmpty() ? new String[]{} : this.searchQuery.toLowerCase().split(" ");
            for (int i = 0; i < n6; ++i) {
                n3 = (int)((float)i + this.scrollOffset);
                if (n3 < 0 || n3 >= this.visibleElements.size()) continue;
                String string3 = this.visibleElements.get(n3);
                boolean bl2 = list.contains(string3);
                String string4 = string3.replace("minecraft:", "");
                Color color = bl2 ? Color.WHITE : Color.GRAY;
                Color color2 = bl2 ? Color.YELLOW : Color.LIGHT_GRAY;
                int n9 = n5 + i * 12;
                Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1 + 2, n9, this.getX() + this.getWidth() - this.getPadding() - 1, n9 + 12, ClickGuiScreen.getButtonColor(this.getY(), 30));
                int n10 = this.getX() + this.getTextPadding() + 2 + 2;
                int n11 = n7 - 15 + 5;
                boolean bl3 = n >= n10 && n <= n11 && n2 >= n9 && n2 < n9 + 12;
                List<ColoredSegment> list2 = this.highlightMatches(string4, stringArray, color, color2);
                int n12 = 0;
                for (ColoredSegment coloredSegment : list2) {
                    n12 += Night.FONT_MANAGER.getWidth(coloredSegment.text);
                }
                int n13 = n11 - n10;
                List<ColoredSegment> object22 = bl3 || n12 <= n13 ? list2 : this.truncateSegments(list2, n13);
                int n14 = n10;
                for (ColoredSegment coloredSegment : object22) {
                    Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, coloredSegment.text, n14, n9 + 2, coloredSegment.color);
                    n14 += Night.FONT_MANAGER.getWidth(coloredSegment.text);
                }
                String string2 = !this.searching || bl2 ? "-" : "+";
                int n15 = n7 - 15 + 5;
                boolean bl4 = n >= n15 && n <= n15 + 15 && n2 >= n9 && n2 < n9 + 12;
                boolean bl5 = bl4;
                Color color3 = string2.equals("+") ? (bl4 ? new Color(0, 255, 0) : Color.WHITE) : (bl4 ? new Color(255, 0, 0) : Color.WHITE);
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, n15, n9 + 2, color3);
            }
            if (this.visibleElements.size() > 6) {
                float f3 = Math.max(0, this.visibleElements.size() - 6);
                float f4 = f3 == 0.0f ? 0.0f : this.scrollOffset / f3;
                n3 = (int)((float)n8 * (6.0f / (float)this.visibleElements.size()));
                if (n3 < 20) {
                    n3 = 20;
                }
                int n16 = (int)((float)(n8 - n3) * f4);
                Renderer2D.renderQuad(guiGraphicsExtractor, n7, n5, n7 + 5, n5 + n8, ClickGuiScreen.getButtonColor(this.getY(), 40));
                Renderer2D.renderQuad(guiGraphicsExtractor, n7, n5 + n16, n7 + 5, n5 + n16 + n3, ClickGuiScreen.getButtonColor(this.getY(), 200));
            }
        }
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        int n2;
        if (this.isHovering(d, d2) && n == 1) {
            this.open = !this.open;
            n2 = this.open ? 1 : 0;
        }
        if (this.isHovering(d, d2) && n == 0 && (!this.open || d2 < (double)(this.getY() + this.getParent().getHeight()))) {
            WhitelistButton.mc.gui.setScreen((Screen)new WhitelistEditorScreen(this.setting, WhitelistButton.mc.gui.screen()));
            return;
        }
        if (!this.open) {
            return;
        }
        if (n == 0) {
            int n3;
            int n4 = this.getY() + this.getParent().getHeight();
            if (d >= (double)(this.getX() + this.getPadding() + 1 + 2) && d <= (double)(this.getX() + this.getWidth() - this.getPadding() - 1) && d2 >= (double)n4 && d2 <= (double)(n4 + 12)) {
                this.searching = true;
                this.selecting = false;
                this.cursorPos = this.searchQuery.length();
                return;
            }
            if (!this.isWithinOpenArea(d, d2)) {
                this.searching = false;
                this.selecting = false;
                this.searchQuery = "";
                this.cursorPos = 0;
            }
            if (this.visibleElements.size() > 6 && this.isHoveringScrollbar(d, d2)) {
                this.draggingScrollbar = true;
                this.dragStartY = (int)d2;
                this.dragStartScroll = this.scrollOffset;
                return;
            }
            if (!this.isWithinOpenArea(d, d2)) {
                return;
            }
            int n5 = this.getX() + this.getPadding() + 1 + 2;
            int n6 = this.getX() + this.getWidth() - this.getPadding() - 1;
            if (d < (double)n5 || d > (double)n6) {
                return;
            }
            int n7 = this.getY() + this.getParent().getHeight();
            int n8 = n7 + 12;
            if (d2 >= (double)n8 && d2 < (double)(n8 + 72) && (n3 = (int)this.scrollOffset + (n2 = (int)(d2 - (double)n8)) / 12) >= 0 && n3 < this.visibleElements.size()) {
                String string = this.visibleElements.get(n3);
                int n9 = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
                int n10 = n9 - 15 + 5;
                int n11 = n8 + n2 / 12 * 12;
                if (d >= (double)n10 && d <= (double)(n10 + 15) && d2 >= (double)n11 && d2 < (double)(n11 + 12)) {
                    boolean bl = this.setting.getWhitelistIds().contains(string);
                    if (bl) {
                        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                            this.setting.remove(IdentifierUtils.getBlock(string));
                        } else {
                            this.setting.remove(IdentifierUtils.getItem(string));
                        }
                        this.setting.remove(string);
                    } else if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                        this.setting.add(IdentifierUtils.getBlock(string));
                    } else {
                        this.setting.add(IdentifierUtils.getItem(string));
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        if (n == 0 && this.draggingScrollbar) {
            this.draggingScrollbar = false;
        }
    }

    @Override
    public void mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.draggingScrollbar && this.visibleElements.size() > 6) {
            int n2 = 72;
            float f = Math.max(0, this.visibleElements.size() - 6);
            float f2 = (float)(d2 - (double)this.dragStartY);
            float f3 = f2 / (float)n2;
            this.targetScrollOffset = this.dragStartScroll + f * f3;
            this.clampScrollValues();
        }
    }

    @Override
    public void mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.open && this.isWithinOpenArea(d, d2)) {
            this.targetScrollOffset -= (float)d4;
            this.clampScrollValues();
        }
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (!this.searching) {
            return;
        }
        Window window = mc.getWindow();
        boolean bl = InputConstants.isKeyDown((Window)window, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (n == 256) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
        } else if (n == 257) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
        } else if (n == 259) {
            if (this.selecting) {
                this.searchQuery = "";
                this.cursorPos = 0;
                this.selecting = false;
            } else if (this.cursorPos > 0) {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos - 1) + this.searchQuery.substring(this.cursorPos);
                --this.cursorPos;
            }
        } else if (n == 261) {
            if (this.cursorPos < this.searchQuery.length()) {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + this.searchQuery.substring(this.cursorPos + 1);
            }
        } else if (n == 263) {
            if (this.cursorPos > 0) {
                --this.cursorPos;
            }
        } else if (n == 262) {
            if (this.cursorPos < this.searchQuery.length()) {
                ++this.cursorPos;
            }
        } else if (bl) {
            if (n == 86) {
                try {
                    String string = WhitelistButton.mc.keyboardHandler.getClipboard();
                    if (string != null) {
                        this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + string + this.searchQuery.substring(this.cursorPos);
                        this.cursorPos += string.length();
                    }
                }
                catch (Exception exception) {
                    Night.LOGGER.error("{}: Failed to process clipboard paste", (Object)exception.getClass().getName(), (Object)exception);
                }
            } else if (n == 67 && this.selecting) {
                try {
                    WhitelistButton.mc.keyboardHandler.setClipboard(this.searchQuery);
                }
                catch (Exception exception) {
                    Night.LOGGER.error("{}: Failed to process clipboard change", (Object)exception.getClass().getName(), (Object)exception);
                }
            } else if (n == 65) {
                this.selecting = true;
                this.cursorPos = this.searchQuery.length();
            }
        }
    }

    @Override
    public void charTyped(char c, int n) {
        if (this.searching && !Character.isISOControl(c)) {
            if (this.selecting) {
                this.searchQuery = String.valueOf(c);
                this.selecting = false;
                this.cursorPos = 1;
            } else {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + c + this.searchQuery.substring(this.cursorPos);
                ++this.cursorPos;
            }
        }
    }

    @Override
    public int getHeight() {
        int n = this.getParent().getHeight();
        if (!this.open) {
            return n;
        }
        return n + 12 + 72;
    }

    @Override
    public boolean isHovering(double d, double d2) {
        return (double)(this.getX() + this.getPadding()) <= d && (double)this.getY() <= d2 && (double)(this.getX() + this.getWidth() - this.getPadding()) > d && (double)(this.getY() + this.getParent().getHeight()) > d2;
    }

    public boolean isHandlingScroll(double d, double d2) {
        return this.open && (this.isWithinOpenArea(d, d2) || this.draggingScrollbar);
    }

    private boolean isWithinOpenArea(double d, double d2) {
        int n = this.getY() + this.getParent().getHeight();
        int n2 = 84;
        return d >= (double)(this.getX() + this.getPadding() + 1 + 2) && d <= (double)(this.getX() + this.getWidth() - this.getPadding() - 1) && d2 >= (double)n && d2 <= (double)(n + n2);
    }

    private boolean isHoveringScrollbar(double d, double d2) {
        if (!this.open || this.visibleElements.size() <= 6) {
            return false;
        }
        int n = this.getY() + this.getParent().getHeight();
        int n2 = n + 12;
        int n3 = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
        int n4 = 72;
        return d >= (double)n3 && d <= (double)(n3 + 5) && d2 >= (double)n2 && d2 <= (double)(n2 + n4);
    }

    private void updateVisibleElements() {
        List<String> list;
        if (this.searchQuery.isEmpty()) {
            if (this.searching) {
                list = new ArrayList<String>(this.allElements);
            } else {
                List<String> list2 = this.setting.getWhitelistIds();
                list = this.allElements.stream().filter(list2::contains).collect(Collectors.toList());
            }
            list.sort((string, string2) -> {
                int n = string.compareToIgnoreCase((String)string2);
                if (n == 0) {
                    return Integer.compare(string.length(), string2.length());
                }
                return n;
            });
        } else {
            String[] stringArray = this.searchQuery.toLowerCase().split(" ");
            list = this.allElements.stream().filter(string -> {
                String string2 = string.toLowerCase();
                for (String string3 : stringArray) {
                    if (string3.isEmpty() || string2.contains(string3)) continue;
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
            list.sort((string, string2) -> {
                int n = this.compareBySearchPriority((String)string, (String)string2, stringArray);
                if (n != 0) {
                    return n;
                }
                int n2 = string.compareToIgnoreCase((String)string2);
                if (n2 == 0) {
                    return Integer.compare(string.length(), string2.length());
                }
                return n2;
            });
        }
        this.visibleElements = list;
    }

    private int compareBySearchPriority(String string, String string2, String[] stringArray) {
        int n = this.countStarts(string, stringArray);
        int n2 = this.countStarts(string2, stringArray);
        return Integer.compare(n, n2);
    }

    private int countStarts(String string, String[] stringArray) {
        int n = string.length();
        String string2 = string.toLowerCase();
        for (String string3 : stringArray) {
            int n2;
            if (string3.isEmpty() || !string2.contains(string3.toLowerCase()) || (n2 = string2.split(string3.toLowerCase())[0].length()) >= n) continue;
            n = n2;
        }
        return n;
    }

    private float getScrollRatio() {
        int n = this.visibleElements.size();
        int n2 = Math.max(0, n - 6);
        if (n2 == 0) {
            return 0.0f;
        }
        return this.scrollOffset / (float)n2;
    }

    private void applyScrollRatio(float f) {
        float f2;
        int n = this.visibleElements.size();
        int n2 = Math.max(0, n - 6);
        this.scrollOffset = f2 = f * (float)n2;
        this.targetScrollOffset = f2;
    }

    private void clampScrollValues() {
        int n = this.visibleElements.size();
        int n2 = Math.max(0, n - 6);
        if (this.targetScrollOffset < 0.0f) {
            this.targetScrollOffset = 0.0f;
        }
        if (this.targetScrollOffset > (float)n2) {
            this.targetScrollOffset = n2;
        }
        if (this.scrollOffset < 0.0f) {
            this.scrollOffset = 0.0f;
        }
        if (this.scrollOffset > (float)n2) {
            this.scrollOffset = n2;
        }
    }

    private List<String> getAllElements() {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            BuiltInRegistries.BLOCK.entrySet().forEach(entry -> arrayList.add(((ResourceKey)entry.getKey()).identifier().toString()));
        } else {
            BuiltInRegistries.ITEM.entrySet().forEach(entry -> arrayList.add(((ResourceKey)entry.getKey()).identifier().toString()));
        }
        arrayList.sort((string, string2) -> {
            int n = string.compareToIgnoreCase((String)string2);
            if (n == 0) {
                return Integer.compare(string.length(), string2.length());
            }
            return n;
        });
        return arrayList;
    }

    private List<ColoredSegment> truncateSegments(List<ColoredSegment> list, int n) {
        ArrayList<ColoredSegment> arrayList = new ArrayList<ColoredSegment>();
        int n2 = 0;
        for (ColoredSegment coloredSegment : list) {
            int n3 = Night.FONT_MANAGER.getWidth(coloredSegment.text);
            if (n2 + n3 <= n) {
                arrayList.add(coloredSegment);
                n2 += n3;
                continue;
            }
            int n4 = n - n2;
            if (n4 > 0) {
                char c;
                int n5;
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < coloredSegment.text.length() && (n5 = Night.FONT_MANAGER.getWidth(String.valueOf(c = coloredSegment.text.charAt(i)))) <= n4 - Night.FONT_MANAGER.getWidth("\u2026"); ++i) {
                    stringBuilder.append(c);
                    n4 -= n5;
                }
                stringBuilder.append("\u2026");
                arrayList.add(new ColoredSegment(stringBuilder.toString(), coloredSegment.color));
                break;
            }
            arrayList.add(new ColoredSegment("\u2026", Color.GRAY));
            break;
        }
        return arrayList;
    }

    private List<ColoredSegment> highlightMatches(String string, String[] stringArray, Color color, Color color2) {
        if (stringArray.length == 0) {
            ArrayList<ColoredSegment> arrayList = new ArrayList<ColoredSegment>();
            arrayList.add(new ColoredSegment(string, color));
            return arrayList;
        }
        String string2 = string.toLowerCase();
        List<Range> list = new ArrayList<Range>();
        for (String object : stringArray) {
            int n;
            if (object.isEmpty()) continue;
            int n2 = 0;
            while ((n = string2.indexOf(object, n2)) != -1) {
                list.add(new Range(n, n + object.length()));
                n2 = n + object.length();
            }
        }
        list = this.mergeRanges(list);
        ArrayList arrayList = new ArrayList();
        int n = 0;
        for (Range range : list) {
            if (range.start > n) {
                arrayList.add(new ColoredSegment(string.substring(n, range.start), color));
            }
            arrayList.add(new ColoredSegment(string.substring(range.start, range.end), color2));
            n = range.end;
        }
        if (n < string.length()) {
            arrayList.add(new ColoredSegment(string.substring(n), color));
        }
        return arrayList;
    }

    private List<Range> mergeRanges(List<Range> list) {
        if (list.isEmpty()) {
            return list;
        }
        list.sort(Comparator.comparingInt(range -> range.start));
        ArrayList<Range> arrayList = new ArrayList<Range>();
        Range range2 = list.getFirst();
        for (int i = 1; i < list.size(); ++i) {
            Range range3 = list.get(i);
            if (range3.start <= range2.end) {
                range2.end = Math.max(range2.end, range3.end);
                continue;
            }
            arrayList.add(range2);
            range2 = range3;
        }
        arrayList.add(range2);
        return arrayList;
    }

    private static class ColoredSegment {
        String text;
        Color color;

        @Generated
        public ColoredSegment(String string, Color color) {
            this.text = string;
            this.color = color;
        }
    }

    private static class Range {
        int start;
        int end;

        @Generated
        public Range(int n, int n2) {
            this.start = n;
            this.end = n2;
        }
    }
}

