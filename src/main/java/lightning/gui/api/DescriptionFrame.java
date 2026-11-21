/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.gui.api;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.GuiDraw;
import lightning.gui.api.DescriptionLayout;
import lightning.gui.api.Frame;
import lightning.gui.api.GuiDescriptionTranslations;
import lightning.gui.api.SearchFrame;
import lightning.utils.graphics.Renderer2D;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DescriptionFrame {
    private static final float CARD_R = 6.0f;
    private static final Color CARD_BG = new Color(21, 20, 35, 242);
    private static final Color CARD_RING = new Color(255, 255, 255, 20);
    private static final Color CARD_LINE = new Color(255, 255, 255, 18);
    private static final Color HINT_COL = new Color(150, 125, 255);
    private String description = "";
    private int x;
    private int y;
    private int width;
    private int preferredWidth;
    private int preferredX;
    private int preferredY;
    private int originalHeaderHeight;
    private boolean rendered;
    private int height;
    private int dragX = 0;
    private int dragY = 0;
    private int textPadding = 3;
    private boolean dragging = false;

    public DescriptionFrame(int n, int n2, int n3, int n4) {
        this.x = n;
        this.preferredX = n;
        this.y = n2;
        this.preferredY = n2;
        this.width = n3;
        this.preferredWidth = n3;
        this.height = n4;
        this.originalHeaderHeight = n4;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        int n3;
        if (this.dragging) {
            this.setX(n - this.dragX);
            this.setY(n2 - this.dragY);
        }
        this.rendered = false;
        ClickGuiScreen clickGuiScreen = Night.CLICK_GUI;
        if (clickGuiScreen.isClosing() || System.currentTimeMillis() - clickGuiScreen.getSlideAnim().getStartTime() < 320L + (long)Math.max(0, clickGuiScreen.getFrames().size() - 1) * 35L) {
            return;
        }
        float f2 = clickGuiScreen.getAppliedScale();
        if (!(f2 > 0.0f) || !Float.isFinite(f2)) {
            return;
        }
        int n4 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int n5 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int n6 = (int)Math.floor((float)n4 / f2);
        int n7 = (int)Math.floor((float)n5 / f2);
        ArrayList<DescriptionLayout.Rect> arrayList = new ArrayList<DescriptionLayout.Rect>();
        for (Frame object2 : clickGuiScreen.getFrames()) {
            n3 = Math.round((float)object2.getBaseContentHeight() * object2.getOpenAnim().get(object2.isOpen() ? 1.0f : 0.0f));
            arrayList.add(new DescriptionLayout.Rect(object2.getX(), object2.getY(), object2.getWidth(), object2.getHeight() + (n3 > 0 ? n3 + 4 : 0)));
        }
        SearchFrame searchFrame = clickGuiScreen.getSearchFrame();
        if (searchFrame.isVisible()) {
            arrayList.add(new DescriptionLayout.Rect((n4 - searchFrame.getWidth()) / 2, n5 - searchFrame.getHeight() - 8, searchFrame.getWidth(), searchFrame.getHeight()));
        }
        String string = this.description.isEmpty() ? "Hover over a module or setting to view details.\nCtrl+F to search" : this.description;
        n3 = Math.max(1, Night.FONT_MANAGER.getHeight());
        DescriptionLayout.Panel panel = DescriptionLayout.create(string, this.description.isEmpty() ? "" : GuiDescriptionTranslations.translate(this.description), "\u2139 Description", "[Ctrl+F]", this.textPadding, n3, this.originalHeaderHeight, this.preferredWidth, Integer.MAX_VALUE, Integer.MAX_VALUE, n6 - 8, n7 - 8, List.of(), Night.FONT_MANAGER::getWidth);
        if (panel == null) {
            return;
        }
        this.x = panel.bounds().x();
        this.y = panel.bounds().y();
        this.width = panel.bounds().width();
        this.height = panel.headerHeight();
        if (this.dragging) {
            this.dragX = n - this.x;
            this.dragY = n2 - this.y;
        }
        this.rendered = true;
        int totalH = panel.totalHeight();
        GuiDraw.rrAll(guiGraphicsExtractor, this.x, this.y, this.x + this.width, this.y + totalH, 6.0f, CARD_BG);
        GuiDraw.ring(guiGraphicsExtractor, this.x, this.y, this.x + this.width, this.y + totalH, 6.0f, 1.0f, CARD_RING);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "\u2139 Description", this.x + this.textPadding + 3, this.y + 2, Color.WHITE);
        String string2 = "[Ctrl+F]";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, this.x + this.width - this.textPadding - 3 - Night.FONT_MANAGER.getWidth(string2), this.y + 2, HINT_COL);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.x + 5, this.y + this.height, this.x + this.width - 5, this.y + this.height + 1, CARD_LINE);
        int n9 = 0;
        for (String string3 : panel.english()) {
            Color color2 = this.description.isEmpty() ? (string3.equals("Ctrl+F to search") ? new Color(125, 125, 135) : new Color(140, 140, 150)) : Color.WHITE;
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string3, this.x + this.textPadding + 3, this.y + this.height + this.textPadding + n3 * n9++, color2);
        }
        for (String string3 : panel.vietnamese()) {
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string3, this.x + this.textPadding + 3, this.y + this.height + this.textPadding + n3 * n9++, new Color(190, 190, 200));
        }
    }

    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2) && n == 0) {
            this.dragging = true;
            this.dragX = (int)(d - (double)this.getX());
            this.dragY = (int)(d2 - (double)this.getY());
        }
    }

    public void mouseReleased(double d, double d2, int n) {
        if (n == 0) {
            this.dragging = false;
        }
    }

    public boolean isHovering(double d, double d2) {
        return this.rendered && (double)this.x <= d && (double)this.y <= d2 && (double)(this.x + this.width) > d && (double)(this.y + this.height) > d2;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public int getX() {
        return this.x;
    }

    @Generated
    public int getY() {
        return this.y;
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
    public int getDragX() {
        return this.dragX;
    }

    @Generated
    public int getDragY() {
        return this.dragY;
    }

    @Generated
    public int getTextPadding() {
        return this.textPadding;
    }

    @Generated
    public boolean isDragging() {
        return this.dragging;
    }

    @Generated
    public void setDescription(String string) {
        this.description = string;
    }

    @Generated
    public void setX(int n) {
        this.x = n;
        this.preferredX = n;
    }

    @Generated
    public void setY(int n) {
        this.y = n;
        this.preferredY = n;
    }

    @Generated
    public void setWidth(int n) {
        this.width = n;
        this.preferredWidth = n;
    }

    @Generated
    public void setHeight(int n) {
        this.height = n;
        this.originalHeaderHeight = n;
    }

    @Generated
    public void setDragX(int n) {
        this.dragX = n;
    }

    @Generated
    public void setDragY(int n) {
        this.dragY = n;
    }

    @Generated
    public void setTextPadding(int n) {
        this.textPadding = n;
    }

    @Generated
    public void setDragging(boolean bl) {
        this.dragging = bl;
    }
}

