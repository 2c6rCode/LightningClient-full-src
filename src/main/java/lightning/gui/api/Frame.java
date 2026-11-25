/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 */
package lightning.gui.api;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.GuiDraw;
import lightning.gui.HeaderFx;
import lightning.gui.api.Button;
import lightning.gui.api.ExpandableRow;
import lightning.gui.impl.ModuleButton;
import lightning.gui.impl.WhitelistButton;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.modules.impl.core.PingBypassModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class Frame {
    public static final int HEADER_H = 22;
    public static final int ROW_H = 16;
    public static final int CHILD_H = 13;
    private static final int BODY_PAD = 4;
    private static final float PANEL_R = 6.0f;
    private static final Color PANEL_BG = new Color(21, 20, 35, 242);
    private static final Color HEADER_COVER = new Color(21, 20, 35, 255);
    private static final Color PANEL_RING = new Color(255, 255, 255, 20);
    private static final Color HEADER_LINE = new Color(255, 255, 255, 18);
    private static final Color ICON_COL = new Color(170, 140, 255);
    private static final Color COUNT_COL = new Color(140, 135, 170);
    private static final Color ARROW_COL = new Color(130, 125, 160);
    private final Module.Category category;
    private final String headerLabel;
    private int x;
    private int y;
    private int width;
    private int height;
    private int totalHeight;
    private int baseContentHeight;
    private int dragX = 0;
    private int dragY = 0;
    private int textPadding = 3;
    private int maxViewHeight = Integer.MAX_VALUE;
    public boolean open = true;
    public boolean dragging = false;
    private final ArrayList<Button> buttons = new ArrayList();
    private float scrollOffset = 0.0f;
    private float targetScroll = 0.0f;
    private long lastScrollTime = System.currentTimeMillis();
    private final Animation openAnim = new Animation(1.0f, 1.0f, 220, Easing.Method.EASE_OUT_CUBIC);

    public Frame(Module.Category category, int x, int y, int width, int height) {
        this.category = category;
        this.headerLabel = null;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        for (Module module : Night.MODULE_MANAGER.getModules(category)) {
            if (module instanceof PingBypassModule) continue;
            this.buttons.add(new ModuleButton(module, this, 16));
        }
    }

    public Frame(String headerLabel, List<Button> buttons, int x, int y, int width, int height) {
        this.category = null;
        this.headerLabel = headerLabel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.buttons.addAll(buttons);
    }

    public int getRequiredWidth() {
        int maxWidth = 0;
        for (Button button : this.buttons) {
            if (!(button instanceof ModuleButton)) continue;
            maxWidth = Math.max(maxWidth, ((ModuleButton)button).getRequiredWidth());
        }
        return maxWidth;
    }

    private int visibleContentHeight() {
        return Math.min(this.baseContentHeight, this.maxViewHeight);
    }

    public int getMaxViewHeight() {
        return this.maxViewHeight;
    }

    public void setMaxViewHeight(int maxViewHeight) {
        this.maxViewHeight = maxViewHeight;
    }

    private String getHeaderText() {
        return this.category != null ? this.category.getName() : this.headerLabel;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        this.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks, "");
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks, String searchQuery) {
        int shift;
        if (this.dragging) {
            this.setX(mouseX - this.dragX);
            this.setY(mouseY - this.dragY);
        }
        boolean searching = searchQuery != null && !searchQuery.isEmpty();
        this.totalHeight = this.height;
        this.baseContentHeight = 0;
        ++this.totalHeight;
        ExpandableRow row;
        for (Button button : this.buttons) {
            if (searching && button instanceof ExpandableRow) {
                row = (ExpandableRow)((Object)button);
                if (!row.getRowName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    button.setVisible(false);
                    continue;
                }
                button.setVisible(true);
                row.setSearchQuery(searchQuery);
            } else if (!searching && button instanceof ExpandableRow) {
                row = (ExpandableRow)((Object)button);
                button.setVisible(true);
                row.setSearchQuery("");
            }
            if (!button.isVisible()) continue;
            this.baseContentHeight += button.getHeight();
            button.setX(this.x);
            button.setY(this.y + this.totalHeight);
            this.totalHeight += button.getHeight();
            if (!(button instanceof ExpandableRow)) continue;
            ExpandableRow expandableRow = (ExpandableRow)((Object)button);
            float openAmount = expandableRow.getOpenAmount();
            List<Button> childButtons = expandableRow.getButtons();
            float offset = 0.0f;
            float sum = 0.0f;
            for (Button child : childButtons) {
                child.getSetting().getVisibility().update();
                boolean childVisible = child.getSetting().getVisibility().isVisible();
                child.setVisible(childVisible);
                if (!childVisible) continue;
                float amount = 1.0f;
                Setting.Visibility visibility = child.getSetting().getVisibility();
                if (visibility instanceof CategorySetting.Visibility) {
                    CategorySetting.Visibility vis = (CategorySetting.Visibility)visibility;
                    amount = vis.getValue().getOpenAmount();
                } else if (visibility instanceof BooleanSetting.Visibility) {
                    BooleanSetting.Visibility vis = (BooleanSetting.Visibility)visibility;
                    amount = vis.getOpenAmount();
                }
                float childH = (float)child.getHeight() * amount;
                child.setX(this.x);
                child.setY(this.y + this.totalHeight + Math.round(offset));
                offset += childH;
                sum += childH;
            }
            int reveal = Math.round(sum * openAmount);
            expandableRow.setRevealHeight(reveal);
            this.totalHeight += reveal;
            if (!searching) continue;
            this.baseContentHeight += reveal;
        }
        float contentH = this.totalHeight - this.height - 1;
        float viewH = this.visibleContentHeight();
        float maxScroll = 0.0f;
        float minScroll = -Math.max(0.0f, contentH - viewH);
        long now = System.currentTimeMillis();
        float dt = Math.min((float)(now - this.lastScrollTime) / 1000.0f, 0.05f);
        this.lastScrollTime = now;
        if (dt <= 1.0E-4f) {
            dt = 0.016f;
        }
        if (this.targetScroll > maxScroll) {
            this.targetScroll += (maxScroll - this.targetScroll) * Math.min(1.0f, 8.0f * dt);
        } else if (this.targetScroll < minScroll) {
            this.targetScroll += (minScroll - this.targetScroll) * Math.min(1.0f, 8.0f * dt);
        }
        float k = 1.0f - (float)Math.exp(-9.0 * (double)dt);
        this.scrollOffset = Mth.lerp((float)k, (float)this.scrollOffset, (float)this.targetScroll);
        if (Math.abs(this.scrollOffset - this.targetScroll) < 0.01f && this.targetScroll >= minScroll && this.targetScroll <= maxScroll) {
            this.scrollOffset = this.targetScroll;
        }
        if ((shift = Math.round(this.scrollOffset)) != 0) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.setY(button.getY() + shift);
                if (!(button instanceof ExpandableRow)) continue;
                ExpandableRow scrollRow = (ExpandableRow)((Object)button);
                for (Button child : scrollRow.getButtons()) {
                    if (!child.isVisible()) continue;
                    child.setY(child.getY() + shift);
                }
            }
        }
        ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        boolean icons = clickGuiModule.categoryIcons.getValue();
        float openProgress = this.openAnim.get(this.open ? 1.0f : 0.0f);
        int bodyH = Math.round((float)this.visibleContentHeight() * openProgress);
        boolean hasBody = bodyH > 0;
        HeaderFx.headerBg(guiGraphicsExtractor, this.x, this.y, this.x + this.width, this.y + this.height, ClickGuiScreen.getButtonColor(this.y, 55));
        GuiDraw.rr(guiGraphicsExtractor, this.x, this.y, this.x + this.width, this.y + this.height, 6.0f, true, true, !hasBody, !hasBody, HEADER_COVER, HEADER_COVER);
        this.renderHeaderContent(guiGraphicsExtractor, clickGuiModule, icons);
        if (hasBody) {
            int top = this.y + this.height;
            int bottom = top + bodyH + 4;
            guiGraphicsExtractor.enableScissor(this.x, top, this.x + this.width, bottom);
            GuiDraw.rr(guiGraphicsExtractor, this.x, top, this.x + this.width, bottom, 6.0f, false, false, true, true, PANEL_BG, PANEL_BG);
            Renderer2D.renderQuad(guiGraphicsExtractor, this.x + 5, top, this.x + this.width - 5, top + 1, HEADER_LINE);
            for (Button button : this.buttons) {
                boolean outside;
                if (!button.isVisible()) continue;
                outside = button.getY() + button.getHeight() < top || button.getY() > bottom;
                if (outside && (!(button instanceof ExpandableRow) || ((ExpandableRow)((Object)button)).getOpenAmount() <= 0.001f)) continue;
                button.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
            }
            guiGraphicsExtractor.disableScissor();
        }
        int panelBottom = this.open ? this.y + this.height + bodyH + 4 : this.y + this.height;
        GuiDraw.ring(guiGraphicsExtractor, this.x, this.y, this.x + this.width, panelBottom, 6.0f, 1.0f, PANEL_RING);
    }

    private void renderHeaderContent(GuiGraphicsExtractor g, ClickGuiModule clickGuiModule, boolean icons) {
        String prefix = icons ? this.getCategoryIcon() + " " : "";
        HeaderFx.header(Night.FONT_MANAGER, g, prefix + this.getHeaderText(), this.x + this.textPadding, this.y + 2, Color.WHITE);
        if (clickGuiModule.triangles.getValue()) {
            String arrow = this.open ? "\u25b2" : "\u25bc";
            int ax = this.x + this.width - this.textPadding - Night.FONT_MANAGER.getWidth(arrow);
            int ay = this.y + Math.round((float)(this.height - Night.FONT_MANAGER.getHeight()) / 2.0f);
            Night.FONT_MANAGER.drawTextWithShadow(g, arrow, ax, ay, new Color(255, 255, 255, 180));
        }
    }

    private String getCategoryIcon() {
        if (this.category == null) {
            return "\u2726";
        }
        switch (this.category) {
            case COMBAT: {
                return "\u2694";
            }
            case MOVEMENT: {
                return "\u26a1";
            }
            case VISUALS: {
                return "\u25c9";
            }
            case PLAYER: {
                return "\u265f";
            }
            case MISCELLANEOUS: {
                return "\u2699";
            }
        }
        return "\u25c6";
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.dragging = true;
                this.dragX = (int)(mouseX - (double)this.getX());
                this.dragY = (int)(mouseY - (double)this.getY());
            } else if (button == 1) {
                this.open = !this.open;
            }
        }
        if (this.open) {
            int top = this.y + this.height;
            int bottom = top + Math.round((float)this.visibleContentHeight() * this.openAnim.get(this.open ? 1.0f : 0.0f)) + 4;
            if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)top && mouseY <= (double)bottom) {
                for (Button child : this.buttons) {
                    if (!child.isVisible()) continue;
                    child.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.dragging = false;
        }
        for (Button child : this.buttons) {
            if (!child.isVisible()) continue;
            child.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int top = this.y;
        int bottom = this.y + this.height + (this.open ? Math.round((float)this.visibleContentHeight() * this.openAnim.get(this.open ? 1.0f : 0.0f)) + 4 : 0);
        if (mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)top && mouseY <= (double)bottom) {
            boolean handled = false;
            for (Button rowButton : this.buttons) {
                ExpandableRow expandableRow;
                if (!(rowButton instanceof ExpandableRow) || !(expandableRow = (ExpandableRow)((Object)rowButton)).isOpen()) continue;
                for (Button child : expandableRow.getButtons()) {
                    WhitelistButton whitelistButton;
                    if (!(child instanceof WhitelistButton) || !(whitelistButton = (WhitelistButton)child).isHandlingScroll(mouseX, mouseY)) continue;
                    whitelistButton.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                    handled = true;
                    break;
                }
                if (!handled) continue;
                break;
            }
            if (!handled && verticalAmount != 0.0) {
                float speed = Night.MODULE_MANAGER.getModule(ClickGuiModule.class).scrollSpeed.getValue().floatValue();
                float contentH = this.totalHeight - this.height - 1;
                float viewH = this.visibleContentHeight();
                float maxScroll = 0.0f;
                float minScroll = -Math.max(0.0f, contentH - viewH);
                float delta = (float)(verticalAmount * (double)speed * 12.0);
                float damp = 1.0f;
                if (this.targetScroll > maxScroll) {
                    damp = 1.0f / (1.0f + (this.targetScroll - maxScroll) * 0.05f);
                } else if (this.targetScroll < minScroll) {
                    damp = 1.0f / (1.0f + (minScroll - this.targetScroll) * 0.05f);
                }
                this.targetScroll += delta * damp;
            }
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Button child : this.buttons) {
            child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.open) {
            for (Button child : this.buttons) {
                if (!child.isVisible()) continue;
                child.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    public void charTyped(char ch, int modifiers) {
        if (this.open) {
            for (Button child : this.buttons) {
                if (!child.isVisible()) continue;
                child.charTyped(ch, modifiers);
            }
        }
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return (double)this.x <= mouseX && (double)this.y <= mouseY && (double)(this.x + this.width) > mouseX && (double)(this.y + this.height) > mouseY;
    }

    public Module.Category getCategory() {
        return this.category;
    }

    public String getHeaderLabel() {
        return this.headerLabel;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getTotalHeight() {
        return this.totalHeight;
    }

    public int getBaseContentHeight() {
        return this.baseContentHeight;
    }

    public int getDragX() {
        return this.dragX;
    }

    public int getDragY() {
        return this.dragY;
    }

    public int getTextPadding() {
        return this.textPadding;
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public ArrayList<Button> getButtons() {
        return this.buttons;
    }

    public float getScrollOffset() {
        return this.scrollOffset;
    }

    public float getTargetScroll() {
        return this.targetScroll;
    }

    public long getLastScrollTime() {
        return this.lastScrollTime;
    }

    public Animation getOpenAnim() {
        return this.openAnim;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setTotalHeight(int totalHeight) {
        this.totalHeight = totalHeight;
    }

    public void setBaseContentHeight(int baseContentHeight) {
        this.baseContentHeight = baseContentHeight;
    }

    public void setDragX(int dragX) {
        this.dragX = dragX;
    }

    public void setDragY(int dragY) {
        this.dragY = dragY;
    }

    public void setTextPadding(int textPadding) {
        this.textPadding = textPadding;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public void setScrollOffset(float scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    public void setTargetScroll(float targetScroll) {
        this.targetScroll = targetScroll;
    }

    public void setLastScrollTime(long lastScrollTime) {
        this.lastScrollTime = lastScrollTime;
    }
}

