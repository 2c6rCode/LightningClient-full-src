/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.network.chat.Component
 */
package lightning.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.managers.ImageManager;
import lightning.settings.impl.ImageSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.system.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class ImageSelectScreen
extends Screen {
    private static final int COL_WIDTH = 175;
    private static final int COL_GAP = 14;
    private static final int HEADER_H = 15;
    private static final int ROW_H = 18;
    private static final int SLIDE_DURATION_MS = 320;
    private static final float SLIDE_DISTANCE = 160.0f;
    private final ImageSetting setting;
    private final Screen parent;
    private List<File> allFiles = new ArrayList<File>();
    private String query = "";
    private String lastQuery = "";
    private long queryChangeTime = System.currentTimeMillis();
    private float scrollList = 0.0f;
    private float targetScrollList = 0.0f;
    private String selectedPreviewName = null;
    private ImageManager.ImageEntry currentPreviewEntry = null;
    private final Animation slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
    private boolean closing = false;
    private final Timer lineTimer = new Timer();
    private boolean showLine = false;
    private int colX1;
    private int colX2;
    private int colY;
    private int colHeight;
    private long lastClickTime = 0L;
    private String lastClickedItem = null;

    public ImageSelectScreen(ImageSetting imageSetting, Screen screen) {
        super((Component)Component.literal((String)"night-image-select"));
        this.setting = imageSetting;
        this.parent = screen;
        this.selectedPreviewName = imageSetting.getValue();
        this.refreshFiles();
    }

    public void refreshFiles() {
        if (Night.IMAGE_MANAGER != null) {
            this.allFiles = Night.IMAGE_MANAGER.getImageFiles();
            if ((this.selectedPreviewName == null || this.selectedPreviewName.equals("None")) && !this.allFiles.isEmpty()) {
                this.selectedPreviewName = this.allFiles.get(0).getName();
            }
            this.updatePreview();
        }
    }

    private void updatePreview() {
        this.currentPreviewEntry = this.selectedPreviewName != null && !this.selectedPreviewName.equalsIgnoreCase("None") && Night.IMAGE_MANAGER != null ? Night.IMAGE_MANAGER.getImageEntry(this.selectedPreviewName) : null;
    }

    public void requestClose() {
        this.closing = true;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(10, 10, 10, 160));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float slideProgress = this.slideAnim.get(this.closing ? 0.0f : 1.0f);
        if (this.closing && slideProgress <= 0.001f) {
            Minecraft.getInstance().gui.setScreen(this.parent);
            return;
        }
        int totalWidth = 364;
        this.colX1 = (this.width - totalWidth) / 2;
        this.colX2 = this.colX1 + 175 + 14;
        this.colY = Math.max(15, (this.height - 240) / 2);
        this.colHeight = Math.min(260, this.height - 30);
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate(0.0f, (1.0f - slideProgress) * -160.0f);
        List<File> files = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
        this.renderListColumn(guiGraphicsExtractor, mouseX, mouseY, this.colX1, files);
        this.renderPreviewColumn(guiGraphicsExtractor, mouseX, mouseY, this.colX2);
        guiGraphicsExtractor.pose().popMatrix();
    }

    private List<File> filterFiles(List<File> files, String searchQuery) {
        String lowerQuery = searchQuery.toLowerCase().trim();
        return files.stream().filter(file -> file.getName().toLowerCase().contains(lowerQuery)).toList();
    }

    private void renderListColumn(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, int colX, List<File> files) {
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, this.colY, colX + 175, this.colY + 15, new Color(21, 21, 21, 220));
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, this.colY + 15 - 1, colX + 175, this.colY + 15, ClickGuiScreen.getButtonColor(this.colY, 200));
        String text;
        if (this.query.isEmpty()) {
            text = "Images (search...)";
            int spaceWidth = Night.FONT_MANAGER.getWidth(" ");
            if (this.showLine) {
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "|", colX + 3, this.colY + 3, Color.WHITE);
            }
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, text, colX + 3 + spaceWidth, this.colY + 3, Color.GRAY);
        } else {
            text = this.query + (this.showLine ? "|" : " ");
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, text, colX + 3, this.colY + 3, Color.WHITE);
        }
        int bodyY = this.colY + 15;
        int bodyHeight = this.colHeight - 15;
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, bodyY, colX + 175, bodyY + bodyHeight, new Color(16, 16, 16, 190));
        Renderer2D.renderOutline(guiGraphicsExtractor, colX, this.colY, colX + 175, bodyY + bodyHeight, ClickGuiScreen.getButtonColor(this.colY, 120));
        if (files.isEmpty()) {
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "No images found in:", colX + 10, bodyY + 20, Color.GRAY);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Lightning/images", colX + 10, bodyY + 32, ClickGuiScreen.getButtonColor(bodyY, 220));
            int btnY = bodyY + 55;
            int btnH = 16;
            boolean btnHovered = mouseX >= colX + 10 && mouseX <= colX + 175 - 10 && mouseY >= btnY && mouseY <= btnY + btnH;
            Color color = btnHovered ? ClickGuiScreen.getButtonColor(btnY, 200) : new Color(31, 31, 31, 200);
            Renderer2D.renderQuad(guiGraphicsExtractor, colX + 10, btnY, colX + 175 - 10, btnY + btnH, color);
            Renderer2D.renderOutline(guiGraphicsExtractor, colX + 10, btnY, colX + 175 - 10, btnY + btnH, ClickGuiScreen.getButtonColor(btnY, 150));
            int btnTextWidth = Night.FONT_MANAGER.getWidth("Open Folder");
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Open Folder", colX + (175 - btnTextWidth) / 2, btnY + 4, Color.WHITE);
            return;
        }
        guiGraphicsExtractor.enableScissor(colX, bodyY, colX + 175, bodyY + bodyHeight);
        int visibleRows = bodyHeight / 18 + 2;
        int startIdx = (int)this.scrollList;
        for (int i = 0; i < visibleRows; ++i) {
            int idx = startIdx + i;
            if (idx < 0 || idx >= files.size()) continue;
            File file = files.get(idx);
            int rowY = bodyY + Math.round((float)(i * 18) - (this.scrollList - (float)startIdx) * 18.0f);
            if (rowY + 18 < bodyY || rowY > bodyY + bodyHeight) continue;
            this.renderRow(guiGraphicsExtractor, mouseX, mouseY, colX, rowY, file);
        }
        guiGraphicsExtractor.disableScissor();
    }

    private void renderRow(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, int colX, int rowY, File file) {
        String name = file.getName();
        boolean isPreviewing = name.equals(this.selectedPreviewName);
        boolean isSelected = name.equals(this.setting.getValue());
        boolean isHovered = mouseX >= colX && mouseX < colX + 175 && mouseY >= rowY && mouseY < rowY + 18 - 1;
        Color bgColor = isPreviewing ? new Color(255, 255, 255, 30) : (isHovered ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0));
        if (bgColor.getAlpha() > 0) {
            Renderer2D.renderQuad(guiGraphicsExtractor, colX, rowY, colX + 175, rowY + 18 - 1, bgColor);
        }
        if (isSelected) {
            Renderer2D.renderQuad(guiGraphicsExtractor, colX, rowY, colX + 3, rowY + 18 - 1, ClickGuiScreen.getButtonColor(rowY, 255));
        }
        Color textColor = isSelected ? Color.YELLOW : (isPreviewing ? ClickGuiScreen.getButtonColor(rowY, 255) : Color.WHITE);
        String displayName = name;
        if (Night.FONT_MANAGER.getWidth(displayName) > 155) {
            while (Night.FONT_MANAGER.getWidth(displayName + "...") > 155 && displayName.length() > 3) {
                displayName = displayName.substring(0, displayName.length() - 1);
            }
            displayName = displayName + "...";
        }
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, displayName, colX + 8, rowY + (18 - Night.FONT_MANAGER.getHeight()) / 2, textColor);
    }

    private void renderPreviewColumn(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, int colX) {
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, this.colY, colX + 175, this.colY + 15, new Color(21, 21, 21, 220));
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, this.colY + 15 - 1, colX + 175, this.colY + 15, ClickGuiScreen.getButtonColor(this.colY, 200));
        String headerTitle = "Preview" + (this.selectedPreviewName != null ? ": " + this.selectedPreviewName : "");
        if (Night.FONT_MANAGER.getWidth(headerTitle) > 165) {
            headerTitle = "Preview";
        }
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, headerTitle, colX + 5, this.colY + 3, Color.WHITE);
        int bodyY = this.colY + 15;
        int bodyHeight = this.colHeight - 15;
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, bodyY, colX + 175, bodyY + bodyHeight, new Color(16, 16, 16, 190));
        Renderer2D.renderOutline(guiGraphicsExtractor, colX, this.colY, colX + 175, bodyY + bodyHeight, ClickGuiScreen.getButtonColor(this.colY, 120));
        if (this.currentPreviewEntry != null && this.currentPreviewEntry.texture != null && this.currentPreviewEntry.identifier != null) {
            int drawH;
            int drawW;
            int pad = 12;
            int boxX = colX + pad;
            int boxY = bodyY + pad;
            int boxW = 175 - pad * 2;
            int boxH = bodyHeight - pad * 2 - 28;
            Renderer2D.renderQuad(guiGraphicsExtractor, boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, new Color(5, 5, 5, 220));
            Renderer2D.renderOutline(guiGraphicsExtractor, boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, new Color(41, 41, 41, 150));
            float aspect = (float)this.currentPreviewEntry.width / (float)Math.max(1, this.currentPreviewEntry.height);
            float boxAspect = (float)boxW / (float)boxH;
            if (aspect > boxAspect) {
                drawW = boxW;
                drawH = Math.max(1, Math.round((float)boxW / aspect));
            } else {
                drawH = boxH;
                drawW = Math.max(1, Math.round((float)boxH * aspect));
            }
            int drawX = boxX + (boxW - drawW) / 2;
            int drawY = boxY + (boxH - drawH) / 2;
            guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, this.currentPreviewEntry.identifier, drawX, drawY, 0.0f, 0.0f, drawW, drawH, drawW, drawH, drawW, drawH, -1);
            String dimText = this.currentPreviewEntry.width + " x " + this.currentPreviewEntry.height + " px";
            int dimWidth = Night.FONT_MANAGER.getWidth(dimText);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, dimText, colX + (175 - dimWidth) / 2, boxY + boxH + 4, Color.LIGHT_GRAY);
            String selectHint = "Double-click item to select";
            int selectHintWidth = Night.FONT_MANAGER.getWidth(selectHint);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, selectHint, colX + (175 - selectHintWidth) / 2, boxY + boxH + 15, Color.GRAY);
        } else {
            String hintText = "Select an image on the left";
            int emptyHintWidth = Night.FONT_MANAGER.getWidth(hintText);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, hintText, colX + (175 - emptyHintWidth) / 2, bodyY + bodyHeight / 2 - 6, Color.GRAY);
        }
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (mouseButtonEvent.button() != 0) {
            return super.mouseClicked(mouseButtonEvent, doubleClick);
        }
        int bodyY = this.colY + 15;
        int bodyHeight = this.colHeight - 15;
        if (this.allFiles.isEmpty() && mouseX >= (double)(this.colX1 + 10) && mouseX <= (double)(this.colX1 + 175 - 10) && mouseY >= (double)(bodyY + 55) && mouseY <= (double)(bodyY + 71)) {
            try {
                Desktop.getDesktop().open(ImageManager.IMAGES_DIR.toFile());
            }
            catch (Exception exception) {
                // empty catch block
            }
            return true;
        }
        if (mouseX >= (double)this.colX1 && mouseX < (double)(this.colX1 + 175) && mouseY >= (double)bodyY && mouseY < (double)(bodyY + bodyHeight)) {
            List<File> files = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
            int relY = (int)(mouseY - (double)bodyY);
            int clickedIdx = (int)this.scrollList + relY / 18;
            if (clickedIdx >= 0 && clickedIdx < files.size()) {
                File file = files.get(clickedIdx);
                String fileName = file.getName();
                long now = System.currentTimeMillis();
                if (doubleClick || fileName.equals(this.lastClickedItem) && now - this.lastClickTime < 350L) {
                    this.setting.setValue(fileName);
                    if (Night.IMAGE_MANAGER != null) {
                        Night.IMAGE_MANAGER.setCurrentActiveImage(fileName);
                    }
                    this.requestClose();
                    return true;
                }
                this.selectedPreviewName = fileName;
                this.lastClickedItem = fileName;
                this.lastClickTime = now;
                this.setting.setValue(fileName);
                if (Night.IMAGE_MANAGER != null) {
                    Night.IMAGE_MANAGER.setCurrentActiveImage(fileName);
                }
                this.updatePreview();
                return true;
            }
        }
        return super.mouseClicked(mouseButtonEvent, doubleClick);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= (double)this.colX1 && mouseX < (double)(this.colX1 + 175)) {
            List<File> files = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
            int bodyHeight = this.colHeight - 15;
            float maxScroll = Math.max(0.0f, (float)files.size() - (float)bodyHeight / 18.0f);
            this.targetScrollList = Math.max(0.0f, Math.min(this.targetScrollList - (float)verticalAmount, maxScroll));
            this.scrollList += (this.targetScrollList - this.scrollList) * 0.5f;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        if (keyCode == 256) {
            this.requestClose();
            return true;
        }
        if (keyCode == 259 && !this.query.isEmpty()) {
            this.query = this.query.substring(0, this.query.length() - 1);
            this.scrollList = 0.0f;
            this.targetScrollList = 0.0f;
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char ch = (char)characterEvent.codepoint();
        if (!Character.isISOControl(ch)) {
            this.query = this.query + ch;
            this.scrollList = 0.0f;
            this.targetScrollList = 0.0f;
            return true;
        }
        return super.charTyped(characterEvent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

