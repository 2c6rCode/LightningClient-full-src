/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.joml.Matrix3x2fStack
 */
package lightning.gui;

import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.ColorGui;
import lightning.gui.LightningSkin;
import lightning.gui.api.DescriptionFrame;
import lightning.gui.api.Frame;
import lightning.gui.api.PingBypassFrame;
import lightning.managers.FontManager;
import lightning.modules.Module;
import lightning.utils.font.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

public final class HeaderFx {
    public static final int GAP = 10;
    public static final int MIN_PANEL_WIDTH = 96;
    private static final int HARD_MIN_WIDTH = 60;
    private static final int SCREEN_MARGIN = 8;
    private static FontRenderer headerFont;
    private static FontRenderer titleFont;
    private static FontRenderer headerSmall;
    private static float bgX1;
    private static float bgY1;
    private static float bgX2;
    private static float bgY2;
    private static float titleY;
    private static float titleCx;
    private static boolean haveBg;
    private static boolean failed;
    private static ClickGuiScreen current;
    private static float hx1;
    private static float hy1;
    private static float hx2;
    private static float hy2;
    private static FontRenderer countFont;

    private HeaderFx() {
    }

    private static String pickFamily(String ... families) {
        try {
            HashSet<String> available = new HashSet<String>();
            for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                available.add(family);
            }
            for (String family : families) {
                if (!available.contains(family)) continue;
                return family;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "SansSerif";
    }

    private static Font[] chain(String family, int style) {
        return new Font[]{new Font(family, style, 1), new Font("Segoe UI Symbol", style, 1), new Font("Segoe UI Emoji", style, 1), new Font("Arial Unicode MS", style, 1), new Font("SansSerif", style, 1), new Font("Dialog", style, 1)};
    }

    private static void init() {
        if (headerFont != null || failed) {
            return;
        }
        try {
            String family = HeaderFx.pickFamily("Bahnschrift", "Segoe UI Black", "Segoe UI Semibold", "Segoe UI", "Verdana");
            headerFont = new FontRenderer(HeaderFx.chain(family, 1), 10.5f);
            titleFont = new FontRenderer(HeaderFx.chain(family, 1), 24.0f);
            headerSmall = new FontRenderer(HeaderFx.chain(family, 1), 8.3f);
        }
        catch (Throwable throwable) {
            failed = true;
        }
    }

    private static Color c(int r, int g, int b, int a) {
        return new Color(r, g, b, a);
    }

    public static void roundRect(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, float radius, Color topColor, Color bottomColor) {
        LightningSkin.rr(guiGraphicsExtractor, x1, y1, x2, y2, radius, true, true, true, true, topColor, bottomColor);
    }

    public static void fitWidths(List<Frame> frames, float viewWidth, int gap) {
        int availableWidth;
        int count = frames.size();
        if (count == 0) {
            return;
        }
        int[] widths = new int[count];
        int totalWidth = 0;
        for (int i = 0; i < count; ++i) {
            widths[i] = Math.max(96, frames.get(i).getRequiredWidth());
            totalWidth += widths[i];
        }
        availableWidth = Math.round(viewWidth) - 16 - (count - 1) * gap;
        if (totalWidth > availableWidth && totalWidth > 0) {
            float scale = (float)availableWidth / (float)totalWidth;
            for (int i = 0; i < count; ++i) {
                widths[i] = Math.max(60, Math.round((float)widths[i] * scale));
            }
        }
        for (int i = 0; i < count; ++i) {
            frames.get(i).setWidth(widths[i]);
        }
    }

    public static void headerBg(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        ColorGui.headerBg(guiGraphicsExtractor, x1, y1, x2, y2, color);
    }

    public static void bodyBg(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        ColorGui.bodyBg(guiGraphicsExtractor, x1, y1, x2, y2, color);
    }

    public static void idleRow(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        float yTop = y1 + 0.75f;
        float yBot = y2 - 0.75f;
        float radius = Math.min(4.0f, (yBot - yTop) / 2.0f);
        LightningSkin.rr(guiGraphicsExtractor, x1, yTop, x2, yBot, radius, true, true, true, true, HeaderFx.c(24, 22, 52, 140), HeaderFx.c(17, 15, 40, 140));
    }

    public static void chargeFill(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        float fillWidth = x2 - x1;
        if (fillWidth <= 0.0f) {
            return;
        }
        float yTop = y1 + 0.75f;
        float yBot = y2 - 0.75f;
        float radius = Math.min(4.0f, Math.min((yBot - yTop) / 2.0f, fillWidth / 2.0f));
        LightningSkin.rr(guiGraphicsExtractor, x1, yTop, x2, yBot, radius, true, true, true, true, HeaderFx.c(112, 92, 235, 175), HeaderFx.c(62, 52, 150, 160));
    }

    public static void hover(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        if (color.getAlpha() == 0) {
            return;
        }
        float yTop = y1 + 0.75f;
        float yBot = y2 - 0.75f;
        float radius = Math.min(4.0f, (yBot - yTop) / 2.0f);
        LightningSkin.rr(guiGraphicsExtractor, x1, yTop, x2, yBot, radius, true, true, true, true, color, color);
    }

    public static void nop(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
    }

    private static int[] counts(String categoryName) {
        try {
            for (Module.Category category : Module.Category.values()) {
                if (!category.getName().equalsIgnoreCase(categoryName) && !category.name().equalsIgnoreCase(categoryName)) continue;
                int toggled = 0;
                int total = 0;
                for (Module module : Night.MODULE_MANAGER.getModules(category)) {
                    ++total;
                    if (!module.isToggled()) continue;
                    ++toggled;
                }
                return new int[]{toggled, total};
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    public static void header(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String text, int x, int y, Color color) {
        ColorGui.header(fontManager, guiGraphicsExtractor, text, x, y, color);
    }

    public static DescriptionFrame desc(ClickGuiScreen clickGuiScreen) {
        current = clickGuiScreen;
        return clickGuiScreen.getDescriptionFrame();
    }

    public static void descRender(DescriptionFrame descriptionFrame, GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        descriptionFrame.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
    }

    public static Matrix3x2fStack poseHook(GuiGraphicsExtractor guiGraphicsExtractor) {
        try {
            ClickGuiScreen clickGuiScreen = current;
            if (clickGuiScreen != null && haveBg && !clickGuiScreen.isClosing()) {
                float scale = clickGuiScreen.getAppliedScale();
                if (scale <= 0.0f) {
                    scale = 1.0f;
                }
                float x1 = bgX1 * scale;
                float y1 = bgY1 * scale;
                float x2 = bgX2 * scale;
                float y2 = bgY2 * scale;
                float radius = 9.0f * scale;
                LightningSkin.rr(guiGraphicsExtractor, x1, y1, x2, y2, radius, true, true, true, true, new Color(16, 16, 16, 0), new Color(8, 8, 8, 0));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return guiGraphicsExtractor.pose();
    }

    public static void applyScale(ClickGuiScreen clickGuiScreen, float requestedScale) {
        clickGuiScreen.setAppliedScale(requestedScale);
        try {
            DescriptionFrame descriptionFrame;
            int cursorX;
            float scale = requestedScale <= 0.0f ? 1.0f : requestedScale;
            Window window = Minecraft.getInstance().getWindow();
            float viewWidth = (float)window.getGuiScaledWidth() / scale;
            float viewHeight = (float)window.getGuiScaledHeight() / scale;
            ArrayList<Frame> frames = clickGuiScreen.getFrames();
            int count = frames.size();
            if (count == 0) {
                return;
            }
            float availableHeight = viewHeight - 22.0f - 46.0f;
            for (Frame frame : frames) {
                frame.setMaxViewHeight(Math.max(40, Math.round(availableHeight - (float)frame.getHeight() - 4.0f)));
            }
            HeaderFx.fitWidths(frames, viewWidth, 10);
            int totalWidth = (count - 1) * 10;
            for (Frame frame : frames) {
                totalWidth += frame.getWidth();
            }
            int startX = Math.round((viewWidth - (float)totalWidth) / 2.0f);
            int maxFrameHeight = 0;
            for (Frame frame : frames) {
                int bodyHeight = Math.min(frame.getButtons().size() * 14, frame.getMaxViewHeight());
                maxFrameHeight = Math.max(maxFrameHeight, frame.getHeight() + bodyHeight + 4);
            }
            HeaderFx.init();
            float titleHeight = 24.0f;
            try {
                if (titleFont != null) {
                    titleHeight = titleFont.getHeight();
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            int startY = 22 + Math.round(Math.max(0.0f, (availableHeight - (float)maxFrameHeight) / 2.0f));
            startY = Math.max(startY, Math.round(titleHeight + 22.0f));
            cursorX = startX;
            for (Frame frame : frames) {
                frame.setX(cursorX);
                frame.setY(startY);
                cursorX += frame.getWidth() + 10;
            }
            float endX = cursorX - 10;
            current = clickGuiScreen;
            titleCx = ((float)startX + endX) / 2.0f;
            titleY = (float)startY - titleHeight - 9.0f;
            bgX1 = (float)startX - 12.0f;
            bgX2 = endX + 12.0f;
            bgY1 = titleY - 7.0f;
            bgY2 = (float)(startY + maxFrameHeight) + 9.0f;
            haveBg = true;
            PingBypassFrame frame2 = clickGuiScreen.getPingBypassFrame();
            if (frame2 != null) {
                frame2.setX(cursorX);
                frame2.setY(startY);
            }
            if ((descriptionFrame = clickGuiScreen.getDescriptionFrame()) != null) {
                descriptionFrame.setX(Math.round((viewWidth - (float)descriptionFrame.getWidth()) / 2.0f));
                descriptionFrame.setY(Math.round(viewHeight - 34.0f));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

