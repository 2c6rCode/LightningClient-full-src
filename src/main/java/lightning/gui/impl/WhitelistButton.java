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

    public WhitelistButton(WhitelistSetting whitelistSetting, Frame frame, int height) {
        super(whitelistSetting, frame, height, whitelistSetting.getDescription());
        this.setting = whitelistSetting;
        this.allElements = this.getAllElements();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        boolean queryChanged = !this.searchQuery.equals(this.oldSearchQuery);
        this.oldSearchQuery = this.searchQuery;
        float savedRatio = 0.0f;
        if (queryChanged) {
            savedRatio = this.getScrollRatio();
        }
        this.updateVisibleElements();
        this.clampScrollValues();
        if (queryChanged) {
            this.applyScrollRatio(savedRatio);
            this.clampScrollValues();
        }
        this.scrollOffset += (this.targetScrollOffset - this.scrollOffset) * 0.5f;
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getWidth() - this.getPadding() - 1, this.getY() + 13 - 1, ClickGuiScreen.getButtonColor(this.getY(), 100));
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1, this.getY(), this.getX() + this.getPadding() + 2, this.getY() + this.getHeight() - 1, ClickGuiScreen.getButtonColor(this.getY(), 30));
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.setting.getTag(), this.getX() + this.getTextPadding() + 1, this.getY() + 2, Color.WHITE);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(ChatFormatting.GRAY) + this.setting.getWhitelist().size(), this.getX() + this.getWidth() - this.getTextPadding() - 1 - Night.FONT_MANAGER.getWidth("" + this.setting.getWhitelist().size()), this.getY() + 2, Color.WHITE);
        if (this.open) {
            int searchY = this.getY() + this.getParent().getHeight();
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1 + 2, searchY, this.getX() + this.getWidth() - this.getPadding() - 1, searchY + 12, ClickGuiScreen.getButtonColor(this.getY(), 30));
            String searchDisplay = this.searchQuery;
            if (this.searching) {
                String caret = Night.CLICK_GUI.isShowLine() ? "|" : " ";
                if (this.cursorPos <= searchDisplay.length()) {
                    searchDisplay = searchDisplay.substring(0, this.cursorPos) + caret + searchDisplay.substring(this.cursorPos);
                }
            } else {
                searchDisplay = "Search...";
            }
            if (this.searching && this.searchQuery.isEmpty()) {
                String caret = Night.CLICK_GUI.isShowLine() ? "|" : " ";
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, caret, this.getX() + this.getTextPadding() + 2 + 2, searchY + 6 - Night.FONT_MANAGER.getHeight() / 2, Color.WHITE);
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Search...", this.getX() + this.getTextPadding() + 2 + 2 + Night.FONT_MANAGER.getWidth(" "), searchY + 6 - Night.FONT_MANAGER.getHeight() / 2, Color.GRAY);
            } else {
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, searchDisplay, this.getX() + this.getTextPadding() + 2 + 2, searchY + 6 - Night.FONT_MANAGER.getHeight() / 2, this.searching ? (this.selecting ? ClickGuiScreen.getButtonColor(this.getY(), 255) : Color.WHITE) : Color.GRAY);
            }
            int listTop = searchY + 12;
            int visibleCount = Math.min(this.visibleElements.size(), 6);
            int scrollbarX = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
            int listHeight = 72;
            List<String> whitelistIds = this.setting.getWhitelistIds();
            String[] searchTokens = this.searchQuery.isEmpty() ? new String[]{} : this.searchQuery.toLowerCase().split(" ");
            for (int i = 0; i < visibleCount; ++i) {
                int elementIndex = (int)((float)i + this.scrollOffset);
                if (elementIndex < 0 || elementIndex >= this.visibleElements.size()) continue;
                String elementId = this.visibleElements.get(elementIndex);
                boolean isWhitelisted = whitelistIds.contains(elementId);
                String displayName = elementId.replace("minecraft:", "");
                Color baseColor = isWhitelisted ? Color.WHITE : Color.GRAY;
                Color highlightColor = isWhitelisted ? Color.YELLOW : Color.LIGHT_GRAY;
                int rowY = listTop + i * 12;
                Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding() + 1 + 2, rowY, this.getX() + this.getWidth() - this.getPadding() - 1, rowY + 12, ClickGuiScreen.getButtonColor(this.getY(), 30));
                int textX = this.getX() + this.getTextPadding() + 2 + 2;
                int textMaxX = scrollbarX - 15 + 5;
                boolean isHoveringText = mouseX >= textX && mouseX <= textMaxX && mouseY >= rowY && mouseY < rowY + 12;
                List<ColoredSegment> segments = this.highlightMatches(displayName, searchTokens, baseColor, highlightColor);
                int totalWidth = 0;
                for (ColoredSegment coloredSegment : segments) {
                    totalWidth += Night.FONT_MANAGER.getWidth(coloredSegment.text);
                }
                int maxWidth = textMaxX - textX;
                List<ColoredSegment> renderSegments = isHoveringText || totalWidth <= maxWidth ? segments : this.truncateSegments(segments, maxWidth);
                int drawX = textX;
                for (ColoredSegment coloredSegment : renderSegments) {
                    Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, coloredSegment.text, drawX, rowY + 2, coloredSegment.color);
                    drawX += Night.FONT_MANAGER.getWidth(coloredSegment.text);
                }
                String actionSymbol = !this.searching || isWhitelisted ? "-" : "+";
                int actionX = scrollbarX - 15 + 5;
                boolean isHoveringAction = mouseX >= actionX && mouseX <= actionX + 15 && mouseY >= rowY && mouseY < rowY + 12;
                Color actionColor = actionSymbol.equals("+") ? (isHoveringAction ? new Color(0, 255, 0) : Color.WHITE) : (isHoveringAction ? new Color(255, 0, 0) : Color.WHITE);
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, actionSymbol, actionX, rowY + 2, actionColor);
            }
            if (this.visibleElements.size() > 6) {
                float maxScroll = Math.max(0, this.visibleElements.size() - 6);
                float scrollRatio = maxScroll == 0.0f ? 0.0f : this.scrollOffset / maxScroll;
                int thumbHeight = (int)((float)listHeight * (6.0f / (float)this.visibleElements.size()));
                if (thumbHeight < 20) {
                    thumbHeight = 20;
                }
                int thumbY = (int)((float)(listHeight - thumbHeight) * scrollRatio);
                Renderer2D.renderQuad(guiGraphicsExtractor, scrollbarX, listTop, scrollbarX + 5, listTop + listHeight, ClickGuiScreen.getButtonColor(this.getY(), 40));
                Renderer2D.renderQuad(guiGraphicsExtractor, scrollbarX, listTop + thumbY, scrollbarX + 5, listTop + thumbY + thumbHeight, ClickGuiScreen.getButtonColor(this.getY(), 200));
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 1) {
            this.open = !this.open;
        }
        if (this.isHovering(mouseX, mouseY) && button == 0 && (!this.open || mouseY < (double)(this.getY() + this.getParent().getHeight()))) {
            WhitelistButton.mc.gui.setScreen((Screen)new WhitelistEditorScreen(this.setting, WhitelistButton.mc.gui.screen()));
            return;
        }
        if (!this.open) {
            return;
        }
        if (button == 0) {
            int elementIndex;
            int relY;
            int searchY = this.getY() + this.getParent().getHeight();
            if (mouseX >= (double)(this.getX() + this.getPadding() + 1 + 2) && mouseX <= (double)(this.getX() + this.getWidth() - this.getPadding() - 1) && mouseY >= (double)searchY && mouseY <= (double)(searchY + 12)) {
                this.searching = true;
                this.selecting = false;
                this.cursorPos = this.searchQuery.length();
                return;
            }
            if (!this.isWithinOpenArea(mouseX, mouseY)) {
                this.searching = false;
                this.selecting = false;
                this.searchQuery = "";
                this.cursorPos = 0;
            }
            if (this.visibleElements.size() > 6 && this.isHoveringScrollbar(mouseX, mouseY)) {
                this.draggingScrollbar = true;
                this.dragStartY = (int)mouseY;
                this.dragStartScroll = this.scrollOffset;
                return;
            }
            if (!this.isWithinOpenArea(mouseX, mouseY)) {
                return;
            }
            int areaLeft = this.getX() + this.getPadding() + 1 + 2;
            int areaRight = this.getX() + this.getWidth() - this.getPadding() - 1;
            if (mouseX < (double)areaLeft || mouseX > (double)areaRight) {
                return;
            }
            int listTop = this.getY() + this.getParent().getHeight() + 12;
            if (mouseY >= (double)listTop && mouseY < (double)(listTop + 72) && (elementIndex = (int)this.scrollOffset + (relY = (int)(mouseY - (double)listTop)) / 12) >= 0 && elementIndex < this.visibleElements.size()) {
                String elementId = this.visibleElements.get(elementIndex);
                int scrollbarX = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
                int actionX = scrollbarX - 15 + 5;
                int rowY = listTop + relY / 12 * 12;
                if (mouseX >= (double)actionX && mouseX <= (double)(actionX + 15) && mouseY >= (double)rowY && mouseY < (double)(rowY + 12)) {
                    boolean isWhitelisted = this.setting.getWhitelistIds().contains(elementId);
                    if (isWhitelisted) {
                        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                            this.setting.remove(IdentifierUtils.getBlock(elementId));
                        } else {
                            this.setting.remove(IdentifierUtils.getItem(elementId));
                        }
                        this.setting.remove(elementId);
                    } else if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                        this.setting.add(IdentifierUtils.getBlock(elementId));
                    } else {
                        this.setting.add(IdentifierUtils.getItem(elementId));
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingScrollbar) {
            this.draggingScrollbar = false;
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.draggingScrollbar && this.visibleElements.size() > 6) {
            int listHeight = 72;
            float maxScroll = Math.max(0, this.visibleElements.size() - 6);
            float dragDeltaY = (float)(mouseY - (double)this.dragStartY);
            float scrollDelta = dragDeltaY / (float)listHeight;
            this.targetScrollOffset = this.dragStartScroll + maxScroll * scrollDelta;
            this.clampScrollValues();
        }
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.open && this.isWithinOpenArea(mouseX, mouseY)) {
            this.targetScrollOffset -= (float)verticalAmount;
            this.clampScrollValues();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.searching) {
            return;
        }
        Window window = mc.getWindow();
        boolean ctrlHeld = InputConstants.isKeyDown((Window)window, (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (keyCode == 256) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
        } else if (keyCode == 257) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
        } else if (keyCode == 259) {
            if (this.selecting) {
                this.searchQuery = "";
                this.cursorPos = 0;
                this.selecting = false;
            } else if (this.cursorPos > 0) {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos - 1) + this.searchQuery.substring(this.cursorPos);
                --this.cursorPos;
            }
        } else if (keyCode == 261) {
            if (this.cursorPos < this.searchQuery.length()) {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + this.searchQuery.substring(this.cursorPos + 1);
            }
        } else if (keyCode == 263) {
            if (this.cursorPos > 0) {
                --this.cursorPos;
            }
        } else if (keyCode == 262) {
            if (this.cursorPos < this.searchQuery.length()) {
                ++this.cursorPos;
            }
        } else if (ctrlHeld) {
            if (keyCode == 86) {
                try {
                    String clipboard = WhitelistButton.mc.keyboardHandler.getClipboard();
                    if (clipboard != null) {
                        this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + clipboard + this.searchQuery.substring(this.cursorPos);
                        this.cursorPos += clipboard.length();
                    }
                }
                catch (Exception exception) {
                    Night.LOGGER.error("{}: Failed to process clipboard paste", (Object)exception.getClass().getName(), (Object)exception);
                }
            } else if (keyCode == 67 && this.selecting) {
                try {
                    WhitelistButton.mc.keyboardHandler.setClipboard(this.searchQuery);
                }
                catch (Exception exception) {
                    Night.LOGGER.error("{}: Failed to process clipboard change", (Object)exception.getClass().getName(), (Object)exception);
                }
            } else if (keyCode == 65) {
                this.selecting = true;
                this.cursorPos = this.searchQuery.length();
            }
        }
    }

    @Override
    public void charTyped(char typedChar, int modifiers) {
        if (this.searching && !Character.isISOControl(typedChar)) {
            if (this.selecting) {
                this.searchQuery = String.valueOf(typedChar);
                this.selecting = false;
                this.cursorPos = 1;
            } else {
                this.searchQuery = this.searchQuery.substring(0, this.cursorPos) + typedChar + this.searchQuery.substring(this.cursorPos);
                ++this.cursorPos;
            }
        }
    }

    @Override
    public int getHeight() {
        int baseHeight = this.getParent().getHeight();
        if (!this.open) {
            return baseHeight;
        }
        return baseHeight + 12 + 72;
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        return (double)(this.getX() + this.getPadding()) <= mouseX && (double)this.getY() <= mouseY && (double)(this.getX() + this.getWidth() - this.getPadding()) > mouseX && (double)(this.getY() + this.getParent().getHeight()) > mouseY;
    }

    public boolean isHandlingScroll(double mouseX, double mouseY) {
        return this.open && (this.isWithinOpenArea(mouseX, mouseY) || this.draggingScrollbar);
    }

    private boolean isWithinOpenArea(double mouseX, double mouseY) {
        int areaTop = this.getY() + this.getParent().getHeight();
        int areaHeight = 84;
        return mouseX >= (double)(this.getX() + this.getPadding() + 1 + 2) && mouseX <= (double)(this.getX() + this.getWidth() - this.getPadding() - 1) && mouseY >= (double)areaTop && mouseY <= (double)(areaTop + areaHeight);
    }

    private boolean isHoveringScrollbar(double mouseX, double mouseY) {
        if (!this.open || this.visibleElements.size() <= 6) {
            return false;
        }
        int searchY = this.getY() + this.getParent().getHeight();
        int listTop = searchY + 12;
        int scrollbarX = this.getX() + this.getWidth() - this.getPadding() - 5 - 1;
        int listHeight = 72;
        return mouseX >= (double)scrollbarX && mouseX <= (double)(scrollbarX + 5) && mouseY >= (double)listTop && mouseY <= (double)(listTop + listHeight);
    }

    private void updateVisibleElements() {
        List<String> filtered;
        if (this.searchQuery.isEmpty()) {
            if (this.searching) {
                filtered = new ArrayList<String>(this.allElements);
            } else {
                List<String> whitelistIds = this.setting.getWhitelistIds();
                filtered = this.allElements.stream().filter(whitelistIds::contains).collect(Collectors.toList());
            }
            filtered.sort((first, second) -> {
                int cmp = first.compareToIgnoreCase(second);
                if (cmp == 0) {
                    return Integer.compare(first.length(), second.length());
                }
                return cmp;
            });
        } else {
            String[] searchTokens = this.searchQuery.toLowerCase().split(" ");
            filtered = this.allElements.stream().filter(element -> {
                String lower = element.toLowerCase();
                for (String token : searchTokens) {
                    if (token.isEmpty() || lower.contains(token)) continue;
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
            filtered.sort((first, second) -> {
                int priorityCmp = this.compareBySearchPriority(first, second, searchTokens);
                if (priorityCmp != 0) {
                    return priorityCmp;
                }
                int cmp = first.compareToIgnoreCase(second);
                if (cmp == 0) {
                    return Integer.compare(first.length(), second.length());
                }
                return cmp;
            });
        }
        this.visibleElements = filtered;
    }

    private int compareBySearchPriority(String first, String second, String[] searchTokens) {
        int firstStart = this.countStarts(first, searchTokens);
        int secondStart = this.countStarts(second, searchTokens);
        return Integer.compare(firstStart, secondStart);
    }

    private int countStarts(String text, String[] searchTokens) {
        int bestIndex = text.length();
        String lower = text.toLowerCase();
        for (String token : searchTokens) {
            int matchIndex;
            if (token.isEmpty() || !lower.contains(token.toLowerCase()) || (matchIndex = lower.split(token.toLowerCase())[0].length()) >= bestIndex) continue;
            bestIndex = matchIndex;
        }
        return bestIndex;
    }

    private float getScrollRatio() {
        int total = this.visibleElements.size();
        int maxScroll = Math.max(0, total - 6);
        if (maxScroll == 0) {
            return 0.0f;
        }
        return this.scrollOffset / (float)maxScroll;
    }

    private void applyScrollRatio(float ratio) {
        float offset;
        int total = this.visibleElements.size();
        int maxScroll = Math.max(0, total - 6);
        this.scrollOffset = offset = ratio * (float)maxScroll;
        this.targetScrollOffset = offset;
    }

    private void clampScrollValues() {
        int total = this.visibleElements.size();
        int maxScroll = Math.max(0, total - 6);
        if (this.targetScrollOffset < 0.0f) {
            this.targetScrollOffset = 0.0f;
        }
        if (this.targetScrollOffset > (float)maxScroll) {
            this.targetScrollOffset = maxScroll;
        }
        if (this.scrollOffset < 0.0f) {
            this.scrollOffset = 0.0f;
        }
        if (this.scrollOffset > (float)maxScroll) {
            this.scrollOffset = maxScroll;
        }
    }

    private List<String> getAllElements() {
        ArrayList<String> elements = new ArrayList<String>();
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            BuiltInRegistries.BLOCK.entrySet().forEach(entry -> elements.add(((ResourceKey)entry.getKey()).identifier().toString()));
        } else {
            BuiltInRegistries.ITEM.entrySet().forEach(entry -> elements.add(((ResourceKey)entry.getKey()).identifier().toString()));
        }
        elements.sort((first, second) -> {
            int cmp = first.compareToIgnoreCase(second);
            if (cmp == 0) {
                return Integer.compare(first.length(), second.length());
            }
            return cmp;
        });
        return elements;
    }

    private List<ColoredSegment> truncateSegments(List<ColoredSegment> segments, int maxWidth) {
        ArrayList<ColoredSegment> truncated = new ArrayList<ColoredSegment>();
        int currentWidth = 0;
        for (ColoredSegment coloredSegment : segments) {
            int segmentWidth = Night.FONT_MANAGER.getWidth(coloredSegment.text);
            if (currentWidth + segmentWidth <= maxWidth) {
                truncated.add(coloredSegment);
                currentWidth += segmentWidth;
                continue;
            }
            int remainingWidth = maxWidth - currentWidth;
            if (remainingWidth > 0) {
                char ch;
                int charWidth;
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < coloredSegment.text.length() && (charWidth = Night.FONT_MANAGER.getWidth(String.valueOf(ch = coloredSegment.text.charAt(i)))) <= remainingWidth - Night.FONT_MANAGER.getWidth("\u2026"); ++i) {
                    stringBuilder.append(ch);
                    remainingWidth -= charWidth;
                }
                stringBuilder.append("\u2026");
                truncated.add(new ColoredSegment(stringBuilder.toString(), coloredSegment.color));
                break;
            }
            truncated.add(new ColoredSegment("\u2026", Color.GRAY));
            break;
        }
        return truncated;
    }

    private List<ColoredSegment> highlightMatches(String text, String[] searchTokens, Color baseColor, Color highlightColor) {
        if (searchTokens.length == 0) {
            ArrayList<ColoredSegment> segments = new ArrayList<ColoredSegment>();
            segments.add(new ColoredSegment(text, baseColor));
            return segments;
        }
        String lower = text.toLowerCase();
        List<Range> ranges = new ArrayList<Range>();
        for (String token : searchTokens) {
            int matchIndex;
            if (token.isEmpty()) continue;
            int searchFrom = 0;
            while ((matchIndex = lower.indexOf(token, searchFrom)) != -1) {
                ranges.add(new Range(matchIndex, matchIndex + token.length()));
                searchFrom = matchIndex + token.length();
            }
        }
        ranges = this.mergeRanges(ranges);
        ArrayList<ColoredSegment> segments = new ArrayList<ColoredSegment>();
        int cursor = 0;
        for (Range range : ranges) {
            if (range.start > cursor) {
                segments.add(new ColoredSegment(text.substring(cursor, range.start), baseColor));
            }
            segments.add(new ColoredSegment(text.substring(range.start, range.end), highlightColor));
            cursor = range.end;
        }
        if (cursor < text.length()) {
            segments.add(new ColoredSegment(text.substring(cursor), baseColor));
        }
        return segments;
    }

    private List<Range> mergeRanges(List<Range> ranges) {
        if (ranges.isEmpty()) {
            return ranges;
        }
        ranges.sort(Comparator.comparingInt(range -> range.start));
        ArrayList<Range> merged = new ArrayList<Range>();
        Range current = ranges.getFirst();
        for (int i = 1; i < ranges.size(); ++i) {
            Range next = ranges.get(i);
            if (next.start <= current.end) {
                current.end = Math.max(current.end, next.end);
                continue;
            }
            merged.add(current);
            current = next;
        }
        merged.add(current);
        return merged;
    }

    private static class ColoredSegment {
        String text;
        Color color;

        @Generated
        public ColoredSegment(String text, Color color) {
            this.text = text;
            this.color = color;
        }
    }

    private static class Range {
        int start;
        int end;

        @Generated
        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}

