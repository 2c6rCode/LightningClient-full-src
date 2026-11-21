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

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(10, 10, 10, 160));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float f2 = this.slideAnim.get(this.closing ? 0.0f : 1.0f);
        if (this.closing && f2 <= 0.001f) {
            Minecraft.getInstance().gui.setScreen(this.parent);
            return;
        }
        int n3 = 364;
        this.colX1 = (this.width - n3) / 2;
        this.colX2 = this.colX1 + 175 + 14;
        this.colY = Math.max(15, (this.height - 240) / 2);
        this.colHeight = Math.min(260, this.height - 30);
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate(0.0f, (1.0f - f2) * -160.0f);
        List<File> list = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
        this.renderListColumn(guiGraphicsExtractor, n, n2, this.colX1, list);
        this.renderPreviewColumn(guiGraphicsExtractor, n, n2, this.colX2);
        guiGraphicsExtractor.pose().popMatrix();
    }

    private List<File> filterFiles(List<File> list, String string) {
        String string2 = string.toLowerCase().trim();
        return list.stream().filter(file -> file.getName().toLowerCase().contains(string2)).toList();
    }

    private void renderListColumn(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3, List<File> list) {
        int n4;
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, this.colY, n3 + 175, this.colY + 15, new Color(21, 21, 21, 220));
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, this.colY + 15 - 1, n3 + 175, this.colY + 15, ClickGuiScreen.getButtonColor(this.colY, 200));
        String text;
        if (this.query.isEmpty()) {
            text = "Images (search...)";
            n4 = Night.FONT_MANAGER.getWidth(" ");
            if (this.showLine) {
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "|", n3 + 3, this.colY + 3, Color.WHITE);
            }
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, text, n3 + 3 + n4, this.colY + 3, Color.GRAY);
        } else {
            text = this.query + (this.showLine ? "|" : " ");
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, text, n3 + 3, this.colY + 3, Color.WHITE);
        }
        int n5 = this.colY + 15;
        n4 = this.colHeight - 15;
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, n5, n3 + 175, n5 + n4, new Color(16, 16, 16, 190));
        Renderer2D.renderOutline(guiGraphicsExtractor, n3, this.colY, n3 + 175, n5 + n4, ClickGuiScreen.getButtonColor(this.colY, 120));
        if (list.isEmpty()) {
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "No images found in:", n3 + 10, n5 + 20, Color.GRAY);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Lightning/images", n3 + 10, n5 + 32, ClickGuiScreen.getButtonColor(n5, 220));
            int n6 = n5 + 55;
            int n7 = 16;
            boolean bl = n >= n3 + 10 && n <= n3 + 175 - 10 && n2 >= n6 && n2 <= n6 + n7;
            Color color = bl ? ClickGuiScreen.getButtonColor(n6, 200) : new Color(31, 31, 31, 200);
            Renderer2D.renderQuad(guiGraphicsExtractor, n3 + 10, n6, n3 + 175 - 10, n6 + n7, color);
            Renderer2D.renderOutline(guiGraphicsExtractor, n3 + 10, n6, n3 + 175 - 10, n6 + n7, ClickGuiScreen.getButtonColor(n6, 150));
            int n8 = Night.FONT_MANAGER.getWidth("Open Folder");
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "Open Folder", n3 + (175 - n8) / 2, n6 + 4, Color.WHITE);
            return;
        }
        guiGraphicsExtractor.enableScissor(n3, n5, n3 + 175, n5 + n4);
        int n9 = n4 / 18 + 2;
        int n10 = (int)this.scrollList;
        for (int i = 0; i < n9; ++i) {
            int n11 = n10 + i;
            if (n11 < 0 || n11 >= list.size()) continue;
            File file = list.get(n11);
            int n12 = n5 + Math.round((float)(i * 18) - (this.scrollList - (float)n10) * 18.0f);
            if (n12 + 18 < n5 || n12 > n5 + n4) continue;
            this.renderRow(guiGraphicsExtractor, n, n2, n3, n12, file);
        }
        guiGraphicsExtractor.disableScissor();
    }

    private void renderRow(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3, int n4, File file) {
        String string = file.getName();
        boolean bl = string.equals(this.selectedPreviewName);
        boolean bl2 = string.equals(this.setting.getValue());
        boolean bl3 = n >= n3 && n < n3 + 175 && n2 >= n4 && n2 < n4 + 18 - 1;
        Color color = bl ? new Color(255, 255, 255, 30) : (bl3 ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0));
        if (color.getAlpha() > 0) {
            Renderer2D.renderQuad(guiGraphicsExtractor, n3, n4, n3 + 175, n4 + 18 - 1, color);
        }
        if (bl2) {
            Renderer2D.renderQuad(guiGraphicsExtractor, n3, n4, n3 + 3, n4 + 18 - 1, ClickGuiScreen.getButtonColor(n4, 255));
        }
        Color color3 = bl2 ? Color.YELLOW : (bl ? ClickGuiScreen.getButtonColor(n4, 255) : Color.WHITE);
        Object object = string;
        if (Night.FONT_MANAGER.getWidth((String)object) > 155) {
            while (Night.FONT_MANAGER.getWidth((String)object + "...") > 155 && ((String)object).length() > 3) {
                object = ((String)object).substring(0, ((String)object).length() - 1);
            }
            object = (String)object + "...";
        }
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)object, n3 + 8, n4 + (18 - Night.FONT_MANAGER.getHeight()) / 2, color3);
    }

    private void renderPreviewColumn(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3) {
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, this.colY, n3 + 175, this.colY + 15, new Color(21, 21, 21, 220));
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, this.colY + 15 - 1, n3 + 175, this.colY + 15, ClickGuiScreen.getButtonColor(this.colY, 200));
        Object object = "Preview" + (String)(this.selectedPreviewName != null ? ": " + this.selectedPreviewName : "");
        if (Night.FONT_MANAGER.getWidth((String)object) > 165) {
            object = "Preview";
        }
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)object, n3 + 5, this.colY + 3, Color.WHITE);
        int n4 = this.colY + 15;
        int n5 = this.colHeight - 15;
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, n4, n3 + 175, n4 + n5, new Color(16, 16, 16, 190));
        Renderer2D.renderOutline(guiGraphicsExtractor, n3, this.colY, n3 + 175, n4 + n5, ClickGuiScreen.getButtonColor(this.colY, 120));
        if (this.currentPreviewEntry != null && this.currentPreviewEntry.texture != null && this.currentPreviewEntry.identifier != null) {
            int n6;
            int n7;
            int n8 = 12;
            int n9 = n3 + n8;
            int n10 = n4 + n8;
            int n11 = 175 - n8 * 2;
            int n12 = n5 - n8 * 2 - 28;
            Renderer2D.renderQuad(guiGraphicsExtractor, n9 - 1, n10 - 1, n9 + n11 + 1, n10 + n12 + 1, new Color(5, 5, 5, 220));
            Renderer2D.renderOutline(guiGraphicsExtractor, n9 - 1, n10 - 1, n9 + n11 + 1, n10 + n12 + 1, new Color(41, 41, 41, 150));
            float f = (float)this.currentPreviewEntry.width / (float)Math.max(1, this.currentPreviewEntry.height);
            float f2 = (float)n11 / (float)n12;
            if (f > f2) {
                n7 = n11;
                n6 = Math.max(1, Math.round((float)n11 / f));
            } else {
                n6 = n12;
                n7 = Math.max(1, Math.round((float)n12 * f));
            }
            int n13 = n9 + (n11 - n7) / 2;
            int n14 = n10 + (n12 - n6) / 2;
            guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, this.currentPreviewEntry.identifier, n13, n14, 0.0f, 0.0f, n7, n6, n7, n6, n7, n6, -1);
            String string = this.currentPreviewEntry.width + " x " + this.currentPreviewEntry.height + " px";
            int n15 = Night.FONT_MANAGER.getWidth(string);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n3 + (175 - n15) / 2, n10 + n12 + 4, Color.LIGHT_GRAY);
            String string2 = "Double-click item to select";
            int n16 = Night.FONT_MANAGER.getWidth(string2);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, n3 + (175 - n16) / 2, n10 + n12 + 15, Color.GRAY);
        } else {
            String string = "Select an image on the left";
            int n17 = Night.FONT_MANAGER.getWidth(string);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n3 + (175 - n17) / 2, n4 + n5 / 2 - 6, Color.GRAY);
        }
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double d = mouseButtonEvent.x();
        double d2 = mouseButtonEvent.y();
        if (mouseButtonEvent.button() != 0) {
            return super.mouseClicked(mouseButtonEvent, bl);
        }
        int n = this.colY + 15;
        int n2 = this.colHeight - 15;
        if (this.allFiles.isEmpty() && d >= (double)(this.colX1 + 10) && d <= (double)(this.colX1 + 175 - 10) && d2 >= (double)(n + 55) && d2 <= (double)(n + 71)) {
            try {
                Desktop.getDesktop().open(ImageManager.IMAGES_DIR.toFile());
            }
            catch (Exception exception) {
                // empty catch block
            }
            return true;
        }
        if (d >= (double)this.colX1 && d < (double)(this.colX1 + 175) && d2 >= (double)n && d2 < (double)(n + n2)) {
            List<File> list = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
            int n3 = (int)(d2 - (double)n);
            int n4 = (int)this.scrollList + n3 / 18;
            if (n4 >= 0 && n4 < list.size()) {
                File file = list.get(n4);
                String string = file.getName();
                long l = System.currentTimeMillis();
                if (bl || string.equals(this.lastClickedItem) && l - this.lastClickTime < 350L) {
                    this.setting.setValue(string);
                    if (Night.IMAGE_MANAGER != null) {
                        Night.IMAGE_MANAGER.setCurrentActiveImage(string);
                    }
                    this.requestClose();
                    return true;
                }
                this.selectedPreviewName = string;
                this.lastClickedItem = string;
                this.lastClickTime = l;
                this.setting.setValue(string);
                if (Night.IMAGE_MANAGER != null) {
                    Night.IMAGE_MANAGER.setCurrentActiveImage(string);
                }
                this.updatePreview();
                return true;
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (d >= (double)this.colX1 && d < (double)(this.colX1 + 175)) {
            List<File> list = this.query.isEmpty() ? this.allFiles : this.filterFiles(this.allFiles, this.query);
            int n = this.colHeight - 15;
            float f = Math.max(0.0f, (float)list.size() - (float)n / 18.0f);
            this.targetScrollList = Math.max(0.0f, Math.min(this.targetScrollList - (float)d4, f));
            this.scrollList += (this.targetScrollList - this.scrollList) * 0.5f;
            return true;
        }
        return super.mouseScrolled(d, d2, d3, d4);
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int n = keyEvent.key();
        if (n == 256) {
            this.requestClose();
            return true;
        }
        if (n == 259 && !this.query.isEmpty()) {
            this.query = this.query.substring(0, this.query.length() - 1);
            this.scrollList = 0.0f;
            this.targetScrollList = 0.0f;
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char c = (char)characterEvent.codepoint();
        if (!Character.isISOControl(c)) {
            this.query = this.query + c;
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

