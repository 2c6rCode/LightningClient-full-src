/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  org.joml.Matrix3x2fStack
 */
package lightning.gui;

import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.HeaderFx;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.DescriptionFrame;
import lightning.gui.api.Frame;
import lightning.gui.impl.BooleanButton;
import lightning.gui.impl.ModuleButton;
import lightning.gui.impl.NumberButton;
import lightning.managers.FontManager;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.input.KeyboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix3x2fStack;

public final class ColorGui {
    private static final Color PURPLE = new Color(139, 92, 246);
    private static final Color PANEL = new Color(21, 20, 31, 240);
    private static final Color PANEL_H = new Color(26, 24, 38, 246);
    private static final Color OFF_PILL = new Color(44, 43, 62);
    private static final Color OFF_KNOB = new Color(126, 124, 152);
    private static final Color TXT_OFF = new Color(150, 148, 172);
    private static final Color PILL = new Color(31, 29, 46, 240);
    private static final Color PILL_EDGE = new Color(150, 135, 215, 62);
    private static final float TS = 0.9f;
    private static final Color TXT_DIM = new Color(150, 148, 175);
    private static final Color[] CAT = new Color[]{new Color(168, 85, 247), new Color(139, 92, 246), new Color(129, 140, 248), new Color(52, 211, 153), new Color(251, 146, 60), new Color(139, 92, 246), new Color(129, 140, 248)};
    private static final float RADIUS = 8.0f;
    private static final Color EDGE = new Color(150, 135, 215, 70);
    private static final int HDR = 22;
    private static final int ROW = 16;
    private static final int GAP = 10;
    private static final Color ICON_PURPLE = new Color(164, 120, 255);
    private static final float ICON_SIZE = 13.0f;
    private static final float ICON_DY = -0.5f;
    private static final float TAU = (float)Math.PI * 2;
    private static float hx1;
    private static float hy1;
    private static float hx2;
    private static float hy2;
    private static Object laidFor;
    private static int laidW;
    private static int laidH;
    private static boolean userMoved;
    private static Frame renderFrame;
    private static final IdentityHashMap<Frame, int[]> placed;
    private static boolean logged;
    private static Method poseM;
    private static Method mPush;
    private static Method mPop;
    private static Method mTrans;
    private static Method mScale;
    private static boolean poseFail;

    private ColorGui() {
    }

    private static Color col(int r, int g, int b, int a) {
        return new Color(ColorGui.clamp255(r), ColorGui.clamp255(g), ColorGui.clamp255(b), ColorGui.clamp255(a));
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static Color lerp(Color start, Color end, float progress) {
        progress = ColorGui.clamp01(progress);
        return new Color(Math.round((float)start.getRed() + (float)(end.getRed() - start.getRed()) * progress), Math.round((float)start.getGreen() + (float)(end.getGreen() - start.getGreen()) * progress), Math.round((float)start.getBlue() + (float)(end.getBlue() - start.getBlue()) * progress), Math.round((float)start.getAlpha() + (float)(end.getAlpha() - start.getAlpha()) * progress));
    }

    private static Frame findFrame(float x, float y) {
        try {
            ClickGuiScreen gui = ColorGui.currentGui();
            if (gui == null) {
                return null;
            }
            for (Frame frame : gui.getFrames()) {
                if (!(Math.abs((float)frame.getX() - x) < 1.5f) || !(Math.abs((float)frame.getY() - y) < 1.5f)) continue;
                return frame;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static boolean triangles() {
        try {
            ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            return clickGuiModule.triangles.getValue();
        }
        catch (Throwable ignored) {
            return true;
        }
    }

    private static void r(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, float radius, Color color) {
        LightningSkin.rrAll(graphics, x1, y1, x2, y2, radius, color);
    }

    private static Object poseOf(GuiGraphicsExtractor graphics) {
        try {
            if (poseM == null) {
                poseM = graphics.getClass().getMethod("pose", new Class[0]);
                poseM.setAccessible(true);
            }
            return poseM.invoke((Object)graphics, new Object[0]);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void ts(GuiGraphicsExtractor graphics, FontManager fontManager, String text, float x, float y, float scale, Color color) {
        Object poseStack = ColorGui.poseOf(graphics);
        boolean pushed = false;
        if (poseStack != null && !poseFail) {
            try {
                if (mPush == null) {
                    Class<?> poseClass = poseStack.getClass();
                    mPush = poseClass.getMethod("pushMatrix", new Class[0]);
                    mPop = poseClass.getMethod("popMatrix", new Class[0]);
                    mTrans = poseClass.getMethod("translate", Float.TYPE, Float.TYPE);
                    mScale = poseClass.getMethod("scale", Float.TYPE, Float.TYPE);
                }
                mPush.invoke(poseStack, new Object[0]);
                pushed = true;
                mTrans.invoke(poseStack, Float.valueOf(x), Float.valueOf(y));
                mScale.invoke(poseStack, Float.valueOf(scale), Float.valueOf(scale));
                fontManager.drawText(graphics, text, 0, 0, color);
                return;
            }
            catch (Throwable ignored) {
                poseFail = true;
            }
            finally {
                if (pushed) {
                    try {
                        mPop.invoke(poseStack, new Object[0]);
                    }
                    catch (Throwable ignored) {}
                }
            }
        }
        fontManager.drawText(graphics, text, Math.round(x), Math.round(y), color);
    }

    public static void headerBg(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, Color color) {
        hx1 = x1;
        hy1 = y1;
        hx2 = x2;
        hy2 = y2;
        Frame frame = ColorGui.findFrame(x1, y1);
        boolean open = frame == null || frame.isOpen();
        LightningSkin.rr(graphics, x1, y1, x2, y2, 8.0f, true, true, !open, !open, PANEL, PANEL);
        float px1 = x1 + 4.0f;
        float py1 = y1 + 4.0f;
        float px2 = x2 - 4.0f;
        float py2 = y2 - 2.0f;
        LightningSkin.rr(graphics, px1, py1, px2, py2, 6.0f, true, true, true, true, PILL, PILL);
        LightningSkin.ring(graphics, px1, py1, px2, py2, 6.0f, 0.75f, PILL_EDGE);
    }

    public static void bodyBg(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, Color color) {
        LightningSkin.rr(graphics, x1, y1, x2, y2, 8.0f, false, false, true, true, PANEL, PANEL);
    }

    public static void outline(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2) {
        LightningSkin.ring(graphics, x1, y1, x2, y2, 8.0f, 0.75f, EDGE);
    }

    public static void panelOutline(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, Color color) {
        ColorGui.outline(graphics, x1, y1, x2, y2);
    }

    public static void header(FontManager fontManager, GuiGraphicsExtractor graphics, String rawTitle, int x, int y, Color color) {
        int i;
        Frame frame = ColorGui.findFrame(hx1, hy1);
        String label = null;
        int cat = 0;
        int on = 0;
        int tot = 0;
        if (frame != null) {
            label = frame.getHeaderLabel();
            if (frame.getCategory() != null) {
                cat = frame.getCategory().ordinal();
            }
            if (frame == renderFrame) {
                label = "Render";
                cat = 6;
            } else if (frame.getCategory() == Module.Category.MISCELLANEOUS) {
                label = "Misc";
            }
            for (Button button : frame.getButtons()) {
                if (!(button instanceof ModuleButton)) continue;
                ModuleButton moduleButton = (ModuleButton)button;
                ++tot;
                if (!moduleButton.getModule().isToggled()) continue;
                ++on;
            }
        }
        String src = label == null || label.isEmpty() ? rawTitle : label;
        for (i = 0; i < src.length() && !Character.isLetter(src.charAt(i)); ++i) {
        }
        String name = src.substring(i);
        if (name.toLowerCase().startsWith("misc")) {
            name = "Misc";
        }
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
        }
        float cy = (hy1 + 4.0f + hy2 - 2.0f) / 2.0f;
        int ix = Math.round(hx1) + 10;
        ColorGui.drawIcon(graphics, cat, (float)ix + 6.5f, cy + -0.5f);
        int ty = Math.round(cy - (float)fontManager.getHeight() / 2.0f);
        fontManager.drawTextWithShadow(graphics, name, ix + 19, ty, Color.WHITE);
        if (tot > 0) {
            String cnt = on + "/" + tot;
            float cw = (float)fontManager.getWidth(cnt) * 0.9f;
            ColorGui.ts(graphics, fontManager, cnt, hx2 - 20.0f - cw, cy - (float)fontManager.getHeight() * 0.9f / 2.0f, 0.9f, TXT_DIM);
        }
    }

    private static void px(GuiGraphicsExtractor graphics, int x, int y, Color color) {
        LightningSkin.fq(graphics, x, y, x + 1, y + 1, color);
    }

    private static void il(float ox, float oy, float scale, float x0, float y0, float x1, float y1, float strokeWidth, Color color) {
        LightningSkin.lineQ(ox + x0 * scale, oy + y0 * scale, ox + x1 * scale, oy + y1 * scale, strokeWidth, color);
    }

    private static void idisc(float ox, float oy, float scale, float x, float y, float radius, Color color) {
        LightningSkin.arcQ(ox + x * scale, oy + y * scale, 0.0f, radius * scale, 0.0f, (float)Math.PI * 2, color);
    }

    private static void iring(float ox, float oy, float scale, float x, float y, float radius, float strokeWidth, Color color) {
        LightningSkin.arcQ(ox + x * scale, oy + y * scale, radius * scale - strokeWidth / 2.0f, radius * scale + strokeWidth / 2.0f, 0.0f, (float)Math.PI * 2, color);
    }

    public static void chevron(GuiGraphicsExtractor graphics, float cx, float cy, boolean up, Color color) {
        float sign = up ? 1.0f : -1.0f;
        LightningSkin.lineQ(cx - 2.8f, cy + 1.3f * sign, cx, cy - 1.3f * sign, 1.05f, color);
        LightningSkin.lineQ(cx, cy - 1.3f * sign, cx + 2.8f, cy + 1.3f * sign, 1.05f, color);
        LightningSkin.commit(graphics);
    }

    public static void chevronRight(GuiGraphicsExtractor graphics, float cx, float cy, Color color) {
        LightningSkin.lineQ(cx - 1.3f, cy - 2.8f, cx + 1.3f, cy, 1.05f, color);
        LightningSkin.lineQ(cx + 1.3f, cy, cx - 1.3f, cy + 2.8f, 1.05f, color);
        LightningSkin.commit(graphics);
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, int categoryIndex, float cx, float cy) {
        Color iconColor = ICON_PURPLE;
        float scale = 0.5416667f;
        float ox = cx - 6.5f;
        float oy = cy - 6.5f;
        float strokeWidth = 2.2f * scale;
        switch (categoryIndex) {
            case 0: {
                ColorGui.il(ox, oy, scale, 4.0f, 4.0f, 16.0f, 16.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 13.0f, 19.0f, 19.0f, 13.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 16.0f, 16.0f, 20.0f, 20.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 19.0f, 21.0f, 21.0f, 19.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 20.0f, 4.0f, 8.0f, 16.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 11.0f, 19.0f, 5.0f, 13.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 8.0f, 16.0f, 4.0f, 20.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 5.0f, 21.0f, 3.0f, 19.0f, strokeWidth, iconColor);
                break;
            }
            case 1: {
                ColorGui.idisc(ox, oy, scale, 12.0f, 7.0f, 4.3f, iconColor);
                LightningSkin.arcQ(ox + 12.0f * scale, oy + 22.0f * scale, 0.0f, 9.5f * scale, (float)Math.PI, (float)Math.PI * 2, iconColor);
                break;
            }
            case 2: {
                int steps = 14;
                for (int i = 0; i < steps; ++i) {
                    float t0 = -1.0f + 2.0f * (float)i / (float)steps;
                    float t1 = -1.0f + 2.0f * (float)(i + 1) / (float)steps;
                    float a0 = 6.2f * (1.0f - t0 * t0);
                    float a1 = 6.2f * (1.0f - t1 * t1);
                    ColorGui.il(ox, oy, scale, 12.0f + 10.0f * t0, 12.0f - a0, 12.0f + 10.0f * t1, 12.0f - a1, strokeWidth, iconColor);
                    ColorGui.il(ox, oy, scale, 12.0f + 10.0f * t0, 12.0f + a0, 12.0f + 10.0f * t1, 12.0f + a1, strokeWidth, iconColor);
                }
                ColorGui.idisc(ox, oy, scale, 12.0f, 12.0f, 3.4f, iconColor);
                break;
            }
            case 3: {
                float w2 = strokeWidth * 1.1f;
                ColorGui.idisc(ox, oy, scale, 15.5f, 4.2f, 2.5f, iconColor);
                ColorGui.il(ox, oy, scale, 13.2f, 8.2f, 10.6f, 14.2f, w2, iconColor);
                ColorGui.il(ox, oy, scale, 12.8f, 9.2f, 17.4f, 11.4f, w2, iconColor);
                ColorGui.il(ox, oy, scale, 12.6f, 9.4f, 8.2f, 10.2f, w2, iconColor);
                ColorGui.il(ox, oy, scale, 10.6f, 14.2f, 14.0f, 17.4f, w2, iconColor);
                ColorGui.il(ox, oy, scale, 14.0f, 17.4f, 13.2f, 22.0f, w2, iconColor);
                ColorGui.il(ox, oy, scale, 10.6f, 14.2f, 7.2f, 17.6f, w2, iconColor);
                ColorGui.il(ox, oy, scale, 7.2f, 17.6f, 4.4f, 16.8f, w2, iconColor);
                break;
            }
            case 4: {
                ColorGui.iring(ox, oy, scale, 12.0f, 12.0f, 6.2f, 2.8f * scale, iconColor);
                for (int j = 0; j < 8; ++j) {
                    float angle = (float)j * ((float)Math.PI * 2) / 8.0f;
                    float cos = (float)Math.cos(angle);
                    float sin = (float)Math.sin(angle);
                    ColorGui.il(ox, oy, scale, 12.0f + cos * 7.0f, 12.0f + sin * 7.0f, 12.0f + cos * 10.0f, 12.0f + sin * 10.0f, 3.2f * scale, iconColor);
                }
                break;
            }
            case 6: {
                ColorGui.il(ox, oy, scale, 3.0f, 4.0f, 21.0f, 4.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 21.0f, 4.0f, 21.0f, 16.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 21.0f, 16.0f, 3.0f, 16.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 3.0f, 16.0f, 3.0f, 4.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 12.0f, 16.0f, 12.0f, 20.0f, strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 8.0f, 20.0f, 16.0f, 20.0f, strokeWidth, iconColor);
                break;
            }
            default: {
                int i;
                float[] hx = new float[6];
                float[] hy = new float[6];
                for (i = 0; i < 6; ++i) {
                    float ang = -1.5707964f + (float)i * ((float)Math.PI * 2) / 6.0f;
                    hx[i] = 12.0f + (float)Math.cos(ang) * 10.0f;
                    hy[i] = 12.0f + (float)Math.sin(ang) * 10.0f;
                }
                for (i = 0; i < 6; ++i) {
                    int j = (i + 1) % 6;
                    ColorGui.il(ox, oy, scale, hx[i], hy[i], hx[j], hy[j], strokeWidth, iconColor);
                }
                ColorGui.il(ox, oy, scale, 12.0f, 12.0f, hx[1], hy[1], strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 12.0f, 12.0f, hx[3], hy[3], strokeWidth, iconColor);
                ColorGui.il(ox, oy, scale, 12.0f, 12.0f, hx[5], hy[5], strokeWidth, iconColor);
            }
        }
        LightningSkin.commit(graphics);
    }

    public static void renderModule(ModuleButton moduleButton, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        float open;
        DescriptionFrame descFrame;
        FontManager fontManager = Night.FONT_MANAGER;
        int x = moduleButton.getX();
        int y = moduleButton.getY();
        int w = moduleButton.getWidth();
        int h = moduleButton.getHeight();
        boolean hover = moduleButton.isHovering(mouseX, mouseY);
        if (hover && (descFrame = Night.CLICK_GUI.getDescriptionFrame()).getDescription().isEmpty()) {
            descFrame.setDescription(moduleButton.getDescription());
        }
        Module module = moduleButton.getModule();
        float on = ColorGui.clamp01(moduleButton.getFillAnim().get(module.isToggled() ? 1.0f : 0.0f));
        int bgA = Math.round(Math.max(hover ? 62.0f : 0.0f, (open = ColorGui.clamp01(moduleButton.getOpenAnim().get(moduleButton.isOpen() ? 1.0f : 0.0f))) * 62.0f));
        if (bgA > 0) {
            ColorGui.r(graphics, x + 4, y + 1, x + w - 4, y + h - 1, 5.0f, ColorGui.col(120, 100, 220, bgA));
        }
        float pr = (float)(x + w) - 9.0f;
        float pl = pr - 17.0f;
        float py = (float)y + ((float)h - 9.0f) / 2.0f;
        ColorGui.r(graphics, pl, py, pr, py + 9.0f, 4.5f, ColorGui.lerp(OFF_PILL, PURPLE, on));
        float kx = pl + 1.25f + 8.0f * on;
        ColorGui.r(graphics, kx, py + 1.25f, kx + 6.5f, py + 7.75f, 3.25f, ColorGui.lerp(OFF_KNOB, Color.WHITE, on));
        float lineH = (float)fontManager.getHeight() * 0.9f;
        float textY = (float)y + ((float)h - lineH) / 2.0f;
        float rightEdge = pl - 5.0f;
        List<Button> kids = moduleButton.getButtons();
        boolean showExtras = hover || moduleButton.isOpen();
        if (showExtras && module.getBind() != 0) {
            String bind = KeyboardUtils.getKeyName(module.getBind()).toUpperCase();
            float bw = (float)fontManager.getWidth(bind) * 0.9f;
            ColorGui.ts(graphics, fontManager, bind, rightEdge - bw, textY, 0.9f, TXT_DIM);
            rightEdge -= bw + 5.0f;
        }
        if (showExtras && ColorGui.triangles() && !kids.isEmpty()) {
            float acx = rightEdge - 4.0f;
            float acy = (float)y + (float)h / 2.0f;
            if (moduleButton.isOpen()) {
                ColorGui.chevron(graphics, acx, acy, false, PURPLE);
            } else {
                ColorGui.chevronRight(graphics, acx, acy, ColorGui.col(255, 255, 255, 190));
            }
            rightEdge -= 10.0f;
        }
        String name = module.getName();
        float nx = (float)x + 9.0f;
        Color tc = ColorGui.lerp(hover ? new Color(205, 203, 224) : TXT_OFF, Color.WHITE, on);
        String query = moduleButton.getSearchQuery();
        int idx = query != null && !query.isEmpty() ? name.toLowerCase().indexOf(query.toLowerCase()) : -1;
        if (idx >= 0) {
            String pre = name.substring(0, idx);
            String mid = name.substring(idx, idx + query.length());
            String post = name.substring(idx + query.length());
            ColorGui.ts(graphics, fontManager, pre, nx, textY, 0.9f, tc);
            float mx2 = nx + (float)fontManager.getWidth(pre) * 0.9f;
            ColorGui.ts(graphics, fontManager, mid, mx2, textY, 0.9f, PURPLE);
            ColorGui.ts(graphics, fontManager, post, mx2 + (float)fontManager.getWidth(mid) * 0.9f, textY, 0.9f, tc);
        } else {
            ColorGui.ts(graphics, fontManager, name, nx, textY, 0.9f, tc);
        }
        if (moduleButton.getRevealHeight() > 0) {
            graphics.enableScissor(x, y + h, x + w, y + h + moduleButton.getRevealHeight());
            for (Button button : kids) {
                DescriptionFrame childDescFrame;
                if (!button.isVisible()) {
                    ColorGui.prevOff(button);
                    continue;
                }
                button.render(graphics, mouseX, mouseY, partialTicks);
                if (!button.isHovering(mouseX, mouseY) || !(childDescFrame = Night.CLICK_GUI.getDescriptionFrame()).getDescription().isEmpty()) continue;
                childDescFrame.setDescription(button.getDescription());
            }
            graphics.disableScissor();
        } else {
            for (Button button : kids) {
                ColorGui.prevOff(button);
            }
        }
    }

    private static void prevOff(Button button) {
        if (button instanceof BooleanButton) {
            ((BooleanButton)button).setPrevVisible(false);
        } else if (button instanceof NumberButton) {
            ((NumberButton)button).setPrevVisible(false);
        }
    }

    public static Color catAccent(int index) {
        return CAT[Math.floorMod(index, CAT.length)];
    }

    public static void resetCur() {
    }

    public static Color accent(int index) {
        return PURPLE;
    }

    public static int overviewRgb(String categoryName) {
        if (categoryName == null) {
            return 9133302;
        }
        switch (categoryName.toUpperCase()) {
            case "COMBAT": {
                return 16486972;
            }
            case "PLAYER": {
                return 9133302;
            }
            case "VISUALS": {
                return 10980346;
            }
            case "MOVEMENT": {
                return 3462041;
            }
            case "MISCELLANEOUS": 
            case "MISC": {
                return 16096779;
            }
        }
        return 8141549;
    }

    public static void card(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, float radius, boolean tl, boolean tr, boolean br, boolean bl, Color c1, Color c2) {
        LightningSkin.rr(graphics, x1, y1, x2, y2, 8.0f, tl, tr, br, bl, PANEL, PANEL);
        LightningSkin.ring(graphics, x1, y1, x2, y2, 8.0f, 0.75f, EDGE);
    }

    public static void noGlow(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, float radius, Color color) {
    }

    public static void noRing(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, float radius, float thickness, Color color) {
    }

    public static void noSparkle(GuiGraphicsExtractor graphics, float x, float y, float radius, Color color) {
    }

    static ClickGuiScreen currentGui() {
        try {
            Screen screen = Minecraft.getInstance().gui.screen();
            if (screen instanceof ClickGuiScreen) {
                return (ClickGuiScreen)screen;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return Night.CLICK_GUI;
    }

    public static Matrix3x2fStack poseHook(GuiGraphicsExtractor graphics) {
        block9: {
            try {
                ColorGui.relayout();
            }
            catch (Throwable t) {
                if (logged) break block9;
                logged = true;
                try (PrintWriter pw = new PrintWriter(new FileWriter("lightning_gui_layout.log", true));){
                    t.printStackTrace(pw);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        return HeaderFx.poseHook(graphics);
    }

    private static Frame byCat(ArrayList<Frame> list, Module.Category category) {
        for (Frame frame : list) {
            if (frame.getCategory() != category || frame == renderFrame) continue;
            return frame;
        }
        return null;
    }

    private static void split(ArrayList<Frame> list) {
        Frame misc;
        if (renderFrame != null && !list.contains(renderFrame)) {
            renderFrame = null;
        }
        Frame core = ColorGui.byCat(list, Module.Category.CORE);
        if (renderFrame == null && core != null && core.getButtons().size() > 8) {
            Frame rf = new Frame(Module.Category.CORE, 0, 22, 100, 22);
            int keep = core.getButtons().size() - 8;
            while (rf.getButtons().size() > keep) {
                rf.getButtons().remove(0);
            }
            while (core.getButtons().size() > 8) {
                core.getButtons().remove(core.getButtons().size() - 1);
            }
            rf.setOpen(true);
            renderFrame = rf;
        }
        Module.Category[] order = new Module.Category[]{Module.Category.COMBAT, Module.Category.PLAYER, Module.Category.VISUALS, Module.Category.MOVEMENT};
        ArrayList<Frame> out = new ArrayList<Frame>();
        for (Module.Category category : order) {
            Frame frame = ColorGui.byCat(list, category);
            if (frame == null) continue;
            out.add(frame);
        }
        Frame core2 = ColorGui.byCat(list, Module.Category.CORE);
        if (core2 != null) {
            out.add(core2);
        }
        if (renderFrame != null) {
            out.add(renderFrame);
        }
        if ((misc = ColorGui.byCat(list, Module.Category.MISCELLANEOUS)) != null) {
            out.add(misc);
        }
        for (Frame frame : list) {
            if (out.contains(frame)) continue;
            out.add(frame);
        }
        list.clear();
        list.addAll(out);
    }

    private static boolean deviates(ArrayList<Frame> list) {
        for (Frame frame : list) {
            int[] pos = placed.get(frame);
            if (pos == null) {
                return true;
            }
            if (frame.getX() == pos[0] && frame.getY() == pos[1] && frame.getWidth() == pos[2] && frame.getMaxViewHeight() == pos[3]) continue;
            return true;
        }
        return false;
    }

    private static void relayout() {
        Frame[] main;
        ClickGuiScreen gui = ColorGui.currentGui();
        if (gui == null) {
            return;
        }
        ArrayList<Frame> frames = gui.getFrames();
        if (frames.size() < 6) {
            return;
        }
        Window window = Minecraft.getInstance().getWindow();
        int sw = window.getGuiScaledWidth();
        int sh = window.getGuiScaledHeight();
        boolean redo = false;
        if (laidFor != gui) {
            laidFor = gui;
            userMoved = false;
            placed.clear();
            ColorGui.split(frames);
            redo = true;
        }
        for (Frame frame : frames) {
            if (!frame.isDragging()) continue;
            userMoved = true;
            return;
        }
        if (!userMoved && (redo || sw != laidW || sh != laidH || frames.size() != placed.size() || ColorGui.deviates(frames))) {
            redo = true;
        }
        if (!redo) {
            return;
        }
        Frame combat = ColorGui.byCat(frames, Module.Category.COMBAT);
        Frame player = ColorGui.byCat(frames, Module.Category.PLAYER);
        Frame visuals = ColorGui.byCat(frames, Module.Category.VISUALS);
        Frame movement = ColorGui.byCat(frames, Module.Category.MOVEMENT);
        Frame misc = ColorGui.byCat(frames, Module.Category.MISCELLANEOUS);
        Frame core = ColorGui.byCat(frames, Module.Category.CORE);
        for (Frame frame : main = new Frame[]{combat, player, visuals, movement, misc}) {
            if (frame != null) continue;
            return;
        }
        int w = 112;
        for (Frame frame : frames) {
            w = Math.max(w, frame.getRequiredWidth());
        }
        for (Frame frame : frames) {
            frame.setWidth(w);
        }
        int lowRows = core != null || renderFrame != null ? 8 : 0;
        int lowBody = lowRows * 16;
        int lowH = lowRows > 0 ? 22 + lowBody + 4 : 0;
        int avail = sh - 44;
        int view = avail - (lowRows > 0 ? lowH + 10 : 0) - 22 - 4;
        view = Math.max(96, Math.min(view, 224));
        view = view / 16 * 16;
        int mainH = 22 + view + 4;
        int totalH = mainH + (lowRows > 0 ? 10 + lowH : 0);
        int top = Math.max(22, (sh - totalH) / 2);
        int rowW = 5 * w + 40;
        int left = Math.max(150, (sw - rowW) / 2);
        for (int i = 0; i < main.length; ++i) {
            main[i].setMaxViewHeight(view);
            main[i].setX(left + i * (w + 10));
            main[i].setY(top);
        }
        int lowY = top + mainH + 10;
        if (core != null) {
            core.setMaxViewHeight(lowBody);
            core.setX(left);
            core.setY(lowY);
        }
        if (renderFrame != null) {
            renderFrame.setMaxViewHeight(lowBody);
            renderFrame.setX(left + w + 10);
            renderFrame.setY(lowY);
        }
        placed.clear();
        for (Frame frame : frames) {
            placed.put(frame, new int[]{frame.getX(), frame.getY(), frame.getWidth(), frame.getMaxViewHeight()});
        }
        laidW = sw;
        laidH = sh;
    }

    static {
        laidW = -1;
        laidH = -1;
        placed = new IdentityHashMap();
    }
}

