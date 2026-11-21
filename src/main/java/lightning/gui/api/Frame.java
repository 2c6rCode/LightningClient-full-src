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

    public Frame(Module.Category category, int n, int n2, int n3, int n4) {
        this.category = category;
        this.headerLabel = null;
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
        for (Module module : Night.MODULE_MANAGER.getModules(category)) {
            if (module instanceof PingBypassModule) continue;
            this.buttons.add(new ModuleButton(module, this, 16));
        }
    }

    public Frame(String string, List<Button> list, int n, int n2, int n3, int n4) {
        this.category = null;
        this.headerLabel = string;
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
        this.buttons.addAll(list);
    }

    public int getRequiredWidth() {
        int n = 0;
        for (Button button : this.buttons) {
            if (!(button instanceof ModuleButton)) continue;
            n = Math.max(n, ((ModuleButton)button).getRequiredWidth());
        }
        return n;
    }

    private int visibleContentHeight() {
        return Math.min(this.baseContentHeight, this.maxViewHeight);
    }

    public int getMaxViewHeight() {
        return this.maxViewHeight;
    }

    public void setMaxViewHeight(int n) {
        this.maxViewHeight = n;
    }

    private String getHeaderText() {
        return this.category != null ? this.category.getName() : this.headerLabel;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        this.render(guiGraphicsExtractor, n, n2, f, "");
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, String string) {
        int shift;
        if (this.dragging) {
            this.setX(n - this.dragX);
            this.setY(n2 - this.dragY);
        }
        boolean searching = string != null && !string.isEmpty();
        this.totalHeight = this.height;
        this.baseContentHeight = 0;
        ++this.totalHeight;
        ExpandableRow row;
        for (Button button : this.buttons) {
            if (searching && button instanceof ExpandableRow) {
                row = (ExpandableRow)((Object)button);
                if (!row.getRowName().toLowerCase().contains(string.toLowerCase())) {
                    button.setVisible(false);
                    continue;
                }
                button.setVisible(true);
                row.setSearchQuery(string);
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
            List<Button> list = expandableRow.getButtons();
            float offset = 0.0f;
            float sum = 0.0f;
            for (Button child : list) {
                child.getSetting().getVisibility().update();
                boolean childVisible = child.getSetting().getVisibility().isVisible();
                child.setVisible(childVisible);
                if (!childVisible) continue;
                float amount = 1.0f;
                Setting.Visibility visibility = child.getSetting().getVisibility();
                if (visibility instanceof CategorySetting.Visibility) {
                    CategorySetting.Visibility v = (CategorySetting.Visibility)visibility;
                    amount = v.getValue().getOpenAmount();
                } else if (visibility instanceof BooleanSetting.Visibility) {
                    BooleanSetting.Visibility v = (BooleanSetting.Visibility)visibility;
                    amount = v.getOpenAmount();
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
                boolean bl = outside = button.getY() + button.getHeight() < top || button.getY() > bottom;
                if (outside && (!(button instanceof ExpandableRow) || ((ExpandableRow)((Object)button)).getOpenAmount() <= 0.001f)) continue;
                button.render(guiGraphicsExtractor, n, n2, f);
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

    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2)) {
            if (n == 0) {
                this.dragging = true;
                this.dragX = (int)(d - (double)this.getX());
                this.dragY = (int)(d2 - (double)this.getY());
            } else if (n == 1) {
                boolean bl = this.open = !this.open;
            }
        }
        if (this.open) {
            int top = this.y + this.height;
            int bottom = top + Math.round((float)this.visibleContentHeight() * this.openAnim.get(this.open ? 1.0f : 0.0f)) + 4;
            if (d >= (double)this.x && d <= (double)(this.x + this.width) && d2 >= (double)top && d2 <= (double)bottom) {
                for (Button button : this.buttons) {
                    if (!button.isVisible()) continue;
                    button.mouseClicked(d, d2, n);
                }
            }
        }
    }

    public void mouseReleased(double d, double d2, int n) {
        if (n == 0) {
            this.dragging = false;
        }
        for (Button button : this.buttons) {
            if (!button.isVisible()) continue;
            button.mouseReleased(d, d2, n);
        }
    }

    public void mouseScrolled(double d, double d2, double d3, double d4) {
        int top = this.y;
        int bottom = this.y + this.height + (this.open ? Math.round((float)this.visibleContentHeight() * this.openAnim.get(this.open ? 1.0f : 0.0f)) + 4 : 0);
        if (d >= (double)this.x && d <= (double)(this.x + this.width) && d2 >= (double)top && d2 <= (double)bottom) {
            boolean handled = false;
            for (Button rowButton : this.buttons) {
                ExpandableRow expandableRow;
                if (!(rowButton instanceof ExpandableRow) || !(expandableRow = (ExpandableRow)((Object)rowButton)).isOpen()) continue;
                for (Button child : expandableRow.getButtons()) {
                    WhitelistButton whitelistButton;
                    if (!(child instanceof WhitelistButton) || !(whitelistButton = (WhitelistButton)child).isHandlingScroll(d, d2)) continue;
                    whitelistButton.mouseScrolled(d, d2, d3, d4);
                    handled = true;
                    break;
                }
                if (!handled) continue;
                break;
            }
            if (!handled && d4 != 0.0) {
                float speed = Night.MODULE_MANAGER.getModule(ClickGuiModule.class).scrollSpeed.getValue().floatValue();
                float contentH = this.totalHeight - this.height - 1;
                float viewH = this.visibleContentHeight();
                float maxScroll = 0.0f;
                float minScroll = -Math.max(0.0f, contentH - viewH);
                float delta = (float)(d4 * (double)speed * 12.0);
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

    public void mouseDragged(double d, double d2, int n, double d3, double d4) {
        for (Button button : this.buttons) {
            button.mouseDragged(d, d2, n, d3, d4);
        }
    }

    public void keyPressed(int n, int n2, int n3) {
        if (this.open) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.keyPressed(n, n2, n3);
            }
        }
    }

    public void charTyped(char c, int n) {
        if (this.open) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.charTyped(c, n);
            }
        }
    }

    public boolean isHovering(double d, double d2) {
        return (double)this.x <= d && (double)this.y <= d2 && (double)(this.x + this.width) > d && (double)(this.y + this.height) > d2;
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

    public void setX(int n) {
        this.x = n;
    }

    public void setY(int n) {
        this.y = n;
    }

    public void setWidth(int n) {
        this.width = n;
    }

    public void setHeight(int n) {
        this.height = n;
    }

    public void setTotalHeight(int n) {
        this.totalHeight = n;
    }

    public void setBaseContentHeight(int n) {
        this.baseContentHeight = n;
    }

    public void setDragX(int n) {
        this.dragX = n;
    }

    public void setDragY(int n) {
        this.dragY = n;
    }

    public void setTextPadding(int n) {
        this.textPadding = n;
    }

    public void setOpen(boolean bl) {
        this.open = bl;
    }

    public void setDragging(boolean bl) {
        this.dragging = bl;
    }

    public void setScrollOffset(float f) {
        this.scrollOffset = f;
    }

    public void setTargetScroll(float f) {
        this.targetScroll = f;
    }

    public void setLastScrollTime(long l) {
        this.lastScrollTime = l;
    }
}

