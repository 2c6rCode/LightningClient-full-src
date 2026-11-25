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

    public DescriptionFrame(int x, int y, int width, int height) {
        this.x = x;
        this.preferredX = x;
        this.y = y;
        this.preferredY = y;
        this.width = width;
        this.preferredWidth = width;
        this.height = height;
        this.originalHeaderHeight = height;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        if (this.dragging) {
            this.setX(mouseX - this.dragX);
            this.setY(mouseY - this.dragY);
        }
        this.rendered = false;
        ClickGuiScreen clickGuiScreen = Night.CLICK_GUI;
        if (clickGuiScreen.isClosing() || System.currentTimeMillis() - clickGuiScreen.getSlideAnim().getStartTime() < 320L + (long)Math.max(0, clickGuiScreen.getFrames().size() - 1) * 35L) {
            return;
        }
        float scale = clickGuiScreen.getAppliedScale();
        if (!(scale > 0.0f) || !Float.isFinite(scale)) {
            return;
        }
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int viewWidth = (int)Math.floor((float)screenWidth / scale);
        int viewHeight = (int)Math.floor((float)screenHeight / scale);
        ArrayList<DescriptionLayout.Rect> obstacles = new ArrayList<DescriptionLayout.Rect>();
        for (Frame frame : clickGuiScreen.getFrames()) {
            int openContentHeight = Math.round((float)frame.getBaseContentHeight() * frame.getOpenAnim().get(frame.isOpen() ? 1.0f : 0.0f));
            obstacles.add(new DescriptionLayout.Rect(frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight() + (openContentHeight > 0 ? openContentHeight + 4 : 0)));
        }
        SearchFrame searchFrame = clickGuiScreen.getSearchFrame();
        if (searchFrame.isVisible()) {
            obstacles.add(new DescriptionLayout.Rect((screenWidth - searchFrame.getWidth()) / 2, screenHeight - searchFrame.getHeight() - 8, searchFrame.getWidth(), searchFrame.getHeight()));
        }
        String displayDesc = this.description.isEmpty() ? "Hover over a module or setting to view details.\nCtrl+F to search" : this.description;
        int lineHeight = Math.max(1, Night.FONT_MANAGER.getHeight());
        DescriptionLayout.Panel panel = DescriptionLayout.create(displayDesc, this.description.isEmpty() ? "" : GuiDescriptionTranslations.translate(this.description), "\u2139 Description", "[Ctrl+F]", this.textPadding, lineHeight, this.originalHeaderHeight, this.preferredWidth, Integer.MAX_VALUE, Integer.MAX_VALUE, viewWidth - 8, viewHeight - 8, List.of(), Night.FONT_MANAGER::getWidth);
        if (panel == null) {
            return;
        }
        this.x = panel.bounds().x();
        this.y = panel.bounds().y();
        this.width = panel.bounds().width();
        this.height = panel.headerHeight();
        if (this.dragging) {
            this.dragX = mouseX - this.x;
            this.dragY = mouseY - this.y;
        }
        this.rendered = true;
        int totalH = panel.totalHeight();
        GuiDraw.rrAll(guiGraphicsExtractor, this.x, this.y, this.x + this.width, this.y + totalH, 6.0f, CARD_BG);
        GuiDraw.ring(guiGraphicsExtractor, this.x, this.y, this.x + this.width, this.y + totalH, 6.0f, 1.0f, CARD_RING);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "\u2139 Description", this.x + this.textPadding + 3, this.y + 2, Color.WHITE);
        String hintText = "[Ctrl+F]";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, hintText, this.x + this.width - this.textPadding - 3 - Night.FONT_MANAGER.getWidth(hintText), this.y + 2, HINT_COL);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.x + 5, this.y + this.height, this.x + this.width - 5, this.y + this.height + 1, CARD_LINE);
        int lineIndex = 0;
        for (String line : panel.english()) {
            Color lineColor = this.description.isEmpty() ? (line.equals("Ctrl+F to search") ? new Color(125, 125, 135) : new Color(140, 140, 150)) : Color.WHITE;
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, line, this.x + this.textPadding + 3, this.y + this.height + this.textPadding + lineHeight * lineIndex++, lineColor);
        }
        for (String line : panel.vietnamese()) {
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, line, this.x + this.textPadding + 3, this.y + this.height + this.textPadding + lineHeight * lineIndex++, new Color(190, 190, 200));
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 0) {
            this.dragging = true;
            this.dragX = (int)(mouseX - (double)this.getX());
            this.dragY = (int)(mouseY - (double)this.getY());
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.dragging = false;
        }
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return this.rendered && (double)this.x <= mouseX && (double)this.y <= mouseY && (double)(this.x + this.width) > mouseX && (double)(this.y + this.height) > mouseY;
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
    public void setDescription(String description) {
        this.description = description;
    }

    @Generated
    public void setX(int x) {
        this.x = x;
        this.preferredX = x;
    }

    @Generated
    public void setY(int y) {
        this.y = y;
        this.preferredY = y;
    }

    @Generated
    public void setWidth(int width) {
        this.width = width;
        this.preferredWidth = width;
    }

    @Generated
    public void setHeight(int height) {
        this.height = height;
        this.originalHeaderHeight = height;
    }

    @Generated
    public void setDragX(int dragX) {
        this.dragX = dragX;
    }

    @Generated
    public void setDragY(int dragY) {
        this.dragY = dragY;
    }

    @Generated
    public void setTextPadding(int textPadding) {
        this.textPadding = textPadding;
    }

    @Generated
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }
}

