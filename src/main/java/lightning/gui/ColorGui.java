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

    private static int clamp255(int n) {
        return Math.max(0, Math.min(255, n));
    }

    private static float clamp01(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    private static Color lerp(Color a, Color b, float f) {
        f = ColorGui.clamp01(f);
        return new Color(Math.round((float)a.getRed() + (float)(b.getRed() - a.getRed()) * f), Math.round((float)a.getGreen() + (float)(b.getGreen() - a.getGreen()) * f), Math.round((float)a.getBlue() + (float)(b.getBlue() - a.getBlue()) * f), Math.round((float)a.getAlpha() + (float)(b.getAlpha() - a.getAlpha()) * f));
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
            ClickGuiModule m = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            return m.triangles.getValue();
        }
        catch (Throwable t) {
            return true;
        }
    }

    private static void r(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float rad, Color c) {
        LightningSkin.rrAll(g, x1, y1, x2, y2, rad, c);
    }

    private static Object poseOf(GuiGraphicsExtractor g) {
        try {
            if (poseM == null) {
                poseM = g.getClass().getMethod("pose", new Class[0]);
                poseM.setAccessible(true);
            }
            return poseM.invoke((Object)g, new Object[0]);
        }
        catch (Throwable t) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void ts(GuiGraphicsExtractor g, FontManager fm, String text, float x, float y, float sc, Color c) {
        Object st = ColorGui.poseOf(g);
        boolean pushed = false;
        if (st != null && !poseFail) {
            try {
                if (mPush == null) {
                    Class<?> k = st.getClass();
                    mPush = k.getMethod("pushMatrix", new Class[0]);
                    mPop = k.getMethod("popMatrix", new Class[0]);
                    mTrans = k.getMethod("translate", Float.TYPE, Float.TYPE);
                    mScale = k.getMethod("scale", Float.TYPE, Float.TYPE);
                }
                mPush.invoke(st, new Object[0]);
                pushed = true;
                mTrans.invoke(st, Float.valueOf(x), Float.valueOf(y));
                mScale.invoke(st, Float.valueOf(sc), Float.valueOf(sc));
                fm.drawText(g, text, 0, 0, c);
                return;
            }
            catch (Throwable t) {
                poseFail = true;
            }
            finally {
                if (pushed) {
                    try {
                        mPop.invoke(st, new Object[0]);
                    }
                    catch (Throwable k) {}
                }
            }
        }
        fm.drawText(g, text, Math.round(x), Math.round(y), c);
    }

    public static void headerBg(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, Color color) {
        hx1 = x1;
        hy1 = y1;
        hx2 = x2;
        hy2 = y2;
        Frame frame = ColorGui.findFrame(x1, y1);
        boolean open = frame == null || frame.isOpen();
        LightningSkin.rr(g, x1, y1, x2, y2, 8.0f, true, true, !open, !open, PANEL, PANEL);
        float px1 = x1 + 4.0f;
        float py1 = y1 + 4.0f;
        float px2 = x2 - 4.0f;
        float py2 = y2 - 2.0f;
        LightningSkin.rr(g, px1, py1, px2, py2, 6.0f, true, true, true, true, PILL, PILL);
        LightningSkin.ring(g, px1, py1, px2, py2, 6.0f, 0.75f, PILL_EDGE);
    }

    public static void bodyBg(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, Color color) {
        LightningSkin.rr(g, x1, y1, x2, y2, 8.0f, false, false, true, true, PANEL, PANEL);
    }

    public static void outline(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2) {
        LightningSkin.ring(g, x1, y1, x2, y2, 8.0f, 0.75f, EDGE);
    }

    public static void panelOutline(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, Color color) {
        ColorGui.outline(g, x1, y1, x2, y2);
    }

    public static void header(FontManager fm, GuiGraphicsExtractor g, String string, int x, int y, Color color) {
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
            for (Button b : frame.getButtons()) {
                if (!(b instanceof ModuleButton)) continue;
                ModuleButton mb = (ModuleButton)b;
                ++tot;
                if (!mb.getModule().isToggled()) continue;
                ++on;
            }
        }
        String src = label == null || label.isEmpty() ? string : label;
        for (i = 0; i < src.length() && !Character.isLetter(src.charAt(i)); ++i) {
        }
        Object name = src.substring(i);
        if (((String)name).toLowerCase().startsWith("misc")) {
            name = "Misc";
        }
        if (!((String)name).isEmpty()) {
            name = Character.toUpperCase(((String)name).charAt(0)) + ((String)name).substring(1).toLowerCase();
        }
        float cy = (hy1 + 4.0f + hy2 - 2.0f) / 2.0f;
        int ix = Math.round(hx1) + 10;
        ColorGui.drawIcon(g, cat, (float)ix + 6.5f, cy + -0.5f);
        int ty = Math.round(cy - (float)fm.getHeight() / 2.0f);
        fm.drawTextWithShadow(g, (String)name, ix + 19, ty, Color.WHITE);
        if (tot > 0) {
            String cnt = on + "/" + tot;
            float cw = (float)fm.getWidth(cnt) * 0.9f;
            ColorGui.ts(g, fm, cnt, hx2 - 20.0f - cw, cy - (float)fm.getHeight() * 0.9f / 2.0f, 0.9f, TXT_DIM);
        }
    }

    private static void px(GuiGraphicsExtractor g, int x, int y, Color c) {
        LightningSkin.fq(g, x, y, x + 1, y + 1, c);
    }

    private static void il(float ox, float oy, float k, float x0, float y0, float x1, float y1, float sw, Color c) {
        LightningSkin.lineQ(ox + x0 * k, oy + y0 * k, ox + x1 * k, oy + y1 * k, sw, c);
    }

    private static void idisc(float ox, float oy, float k, float x, float y, float rad, Color c) {
        LightningSkin.arcQ(ox + x * k, oy + y * k, 0.0f, rad * k, 0.0f, (float)Math.PI * 2, c);
    }

    private static void iring(float ox, float oy, float k, float x, float y, float rad, float sw, Color c) {
        LightningSkin.arcQ(ox + x * k, oy + y * k, rad * k - sw / 2.0f, rad * k + sw / 2.0f, 0.0f, (float)Math.PI * 2, c);
    }

    public static void chevron(GuiGraphicsExtractor g, float cx, float cy, boolean up, Color c) {
        float s = up ? 1.0f : -1.0f;
        LightningSkin.lineQ(cx - 2.8f, cy + 1.3f * s, cx, cy - 1.3f * s, 1.05f, c);
        LightningSkin.lineQ(cx, cy - 1.3f * s, cx + 2.8f, cy + 1.3f * s, 1.05f, c);
        LightningSkin.commit(g);
    }

    public static void chevronRight(GuiGraphicsExtractor g, float cx, float cy, Color c) {
        LightningSkin.lineQ(cx - 1.3f, cy - 2.8f, cx + 1.3f, cy, 1.05f, c);
        LightningSkin.lineQ(cx + 1.3f, cy, cx - 1.3f, cy + 2.8f, 1.05f, c);
        LightningSkin.commit(g);
    }

    private static void drawIcon(GuiGraphicsExtractor g, int cat, float cx, float cy) {
        Color c = ICON_PURPLE;
        float k = 0.5416667f;
        float ox = cx - 6.5f;
        float oy = cy - 6.5f;
        float sw = 2.2f * k;
        switch (cat) {
            case 0: {
                ColorGui.il(ox, oy, k, 4.0f, 4.0f, 16.0f, 16.0f, sw, c);
                ColorGui.il(ox, oy, k, 13.0f, 19.0f, 19.0f, 13.0f, sw, c);
                ColorGui.il(ox, oy, k, 16.0f, 16.0f, 20.0f, 20.0f, sw, c);
                ColorGui.il(ox, oy, k, 19.0f, 21.0f, 21.0f, 19.0f, sw, c);
                ColorGui.il(ox, oy, k, 20.0f, 4.0f, 8.0f, 16.0f, sw, c);
                ColorGui.il(ox, oy, k, 11.0f, 19.0f, 5.0f, 13.0f, sw, c);
                ColorGui.il(ox, oy, k, 8.0f, 16.0f, 4.0f, 20.0f, sw, c);
                ColorGui.il(ox, oy, k, 5.0f, 21.0f, 3.0f, 19.0f, sw, c);
                break;
            }
            case 1: {
                ColorGui.idisc(ox, oy, k, 12.0f, 7.0f, 4.3f, c);
                LightningSkin.arcQ(ox + 12.0f * k, oy + 22.0f * k, 0.0f, 9.5f * k, (float)Math.PI, (float)Math.PI * 2, c);
                break;
            }
            case 2: {
                int n = 14;
                for (int i = 0; i < n; ++i) {
                    float t0 = -1.0f + 2.0f * (float)i / (float)n;
                    float t1 = -1.0f + 2.0f * (float)(i + 1) / (float)n;
                    float a0 = 6.2f * (1.0f - t0 * t0);
                    float a1 = 6.2f * (1.0f - t1 * t1);
                    ColorGui.il(ox, oy, k, 12.0f + 10.0f * t0, 12.0f - a0, 12.0f + 10.0f * t1, 12.0f - a1, sw, c);
                    ColorGui.il(ox, oy, k, 12.0f + 10.0f * t0, 12.0f + a0, 12.0f + 10.0f * t1, 12.0f + a1, sw, c);
                }
                ColorGui.idisc(ox, oy, k, 12.0f, 12.0f, 3.4f, c);
                break;
            }
            case 3: {
                float w2 = sw * 1.1f;
                ColorGui.idisc(ox, oy, k, 15.5f, 4.2f, 2.5f, c);
                ColorGui.il(ox, oy, k, 13.2f, 8.2f, 10.6f, 14.2f, w2, c);
                ColorGui.il(ox, oy, k, 12.8f, 9.2f, 17.4f, 11.4f, w2, c);
                ColorGui.il(ox, oy, k, 12.6f, 9.4f, 8.2f, 10.2f, w2, c);
                ColorGui.il(ox, oy, k, 10.6f, 14.2f, 14.0f, 17.4f, w2, c);
                ColorGui.il(ox, oy, k, 14.0f, 17.4f, 13.2f, 22.0f, w2, c);
                ColorGui.il(ox, oy, k, 10.6f, 14.2f, 7.2f, 17.6f, w2, c);
                ColorGui.il(ox, oy, k, 7.2f, 17.6f, 4.4f, 16.8f, w2, c);
                break;
            }
            case 4: {
                ColorGui.iring(ox, oy, k, 12.0f, 12.0f, 6.2f, 2.8f * k, c);
                for (int j = 0; j < 8; ++j) {
                    float a = (float)j * ((float)Math.PI * 2) / 8.0f;
                    float cs = (float)Math.cos(a);
                    float sn = (float)Math.sin(a);
                    ColorGui.il(ox, oy, k, 12.0f + cs * 7.0f, 12.0f + sn * 7.0f, 12.0f + cs * 10.0f, 12.0f + sn * 10.0f, 3.2f * k, c);
                }
                break;
            }
            case 6: {
                ColorGui.il(ox, oy, k, 3.0f, 4.0f, 21.0f, 4.0f, sw, c);
                ColorGui.il(ox, oy, k, 21.0f, 4.0f, 21.0f, 16.0f, sw, c);
                ColorGui.il(ox, oy, k, 21.0f, 16.0f, 3.0f, 16.0f, sw, c);
                ColorGui.il(ox, oy, k, 3.0f, 16.0f, 3.0f, 4.0f, sw, c);
                ColorGui.il(ox, oy, k, 12.0f, 16.0f, 12.0f, 20.0f, sw, c);
                ColorGui.il(ox, oy, k, 8.0f, 20.0f, 16.0f, 20.0f, sw, c);
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
                    ColorGui.il(ox, oy, k, hx[i], hy[i], hx[j], hy[j], sw, c);
                }
                ColorGui.il(ox, oy, k, 12.0f, 12.0f, hx[1], hy[1], sw, c);
                ColorGui.il(ox, oy, k, 12.0f, 12.0f, hx[3], hy[3], sw, c);
                ColorGui.il(ox, oy, k, 12.0f, 12.0f, hx[5], hy[5], sw, c);
            }
        }
        LightningSkin.commit(g);
    }

    public static void renderModule(ModuleButton mb, GuiGraphicsExtractor g, int mx, int my, float pt) {
        int idx;
        boolean showExtras;
        float open;
        DescriptionFrame df;
        FontManager fm = Night.FONT_MANAGER;
        int x = mb.getX();
        int y = mb.getY();
        int w = mb.getWidth();
        int h = mb.getHeight();
        boolean hover = mb.isHovering(mx, my);
        if (hover && (df = Night.CLICK_GUI.getDescriptionFrame()).getDescription().isEmpty()) {
            df.setDescription(mb.getDescription());
        }
        Module m = mb.getModule();
        float on = ColorGui.clamp01(mb.getFillAnim().get(m.isToggled() ? 1.0f : 0.0f));
        int bgA = Math.round(Math.max(hover ? 62.0f : 0.0f, (open = ColorGui.clamp01(mb.getOpenAnim().get(mb.isOpen() ? 1.0f : 0.0f))) * 62.0f));
        if (bgA > 0) {
            ColorGui.r(g, x + 4, y + 1, x + w - 4, y + h - 1, 5.0f, ColorGui.col(120, 100, 220, bgA));
        }
        float pr = (float)(x + w) - 9.0f;
        float pl = pr - 17.0f;
        float py = (float)y + ((float)h - 9.0f) / 2.0f;
        ColorGui.r(g, pl, py, pr, py + 9.0f, 4.5f, ColorGui.lerp(OFF_PILL, PURPLE, on));
        float kx = pl + 1.25f + 8.0f * on;
        ColorGui.r(g, kx, py + 1.25f, kx + 6.5f, py + 7.75f, 3.25f, ColorGui.lerp(OFF_KNOB, Color.WHITE, on));
        float lineH = (float)fm.getHeight() * 0.9f;
        float textY = (float)y + ((float)h - lineH) / 2.0f;
        float rightEdge = pl - 5.0f;
        List<Button> kids = mb.getButtons();
        boolean bl = showExtras = hover || mb.isOpen();
        if (showExtras && m.getBind() != 0) {
            String bind = KeyboardUtils.getKeyName(m.getBind()).toUpperCase();
            float bw = (float)fm.getWidth(bind) * 0.9f;
            ColorGui.ts(g, fm, bind, rightEdge - bw, textY, 0.9f, TXT_DIM);
            rightEdge -= bw + 5.0f;
        }
        if (showExtras && ColorGui.triangles() && !kids.isEmpty()) {
            float acx = rightEdge - 4.0f;
            float acy = (float)y + (float)h / 2.0f;
            if (mb.isOpen()) {
                ColorGui.chevron(g, acx, acy, false, PURPLE);
            } else {
                ColorGui.chevronRight(g, acx, acy, ColorGui.col(255, 255, 255, 190));
            }
            rightEdge -= 10.0f;
        }
        String name = m.getName();
        float nx = (float)x + 9.0f;
        Color tc = ColorGui.lerp(hover ? new Color(205, 203, 224) : TXT_OFF, Color.WHITE, on);
        String q = mb.getSearchQuery();
        int n = idx = q != null && !q.isEmpty() ? name.toLowerCase().indexOf(q.toLowerCase()) : -1;
        if (idx >= 0) {
            String pre = name.substring(0, idx);
            String mid = name.substring(idx, idx + q.length());
            String post = name.substring(idx + q.length());
            ColorGui.ts(g, fm, pre, nx, textY, 0.9f, tc);
            float mx2 = nx + (float)fm.getWidth(pre) * 0.9f;
            ColorGui.ts(g, fm, mid, mx2, textY, 0.9f, PURPLE);
            ColorGui.ts(g, fm, post, mx2 + (float)fm.getWidth(mid) * 0.9f, textY, 0.9f, tc);
        } else {
            ColorGui.ts(g, fm, name, nx, textY, 0.9f, tc);
        }
        if (mb.getRevealHeight() > 0) {
            g.enableScissor(x, y + h, x + w, y + h + mb.getRevealHeight());
            for (Button b : kids) {
                DescriptionFrame d2;
                if (!b.isVisible()) {
                    ColorGui.prevOff(b);
                    continue;
                }
                b.render(g, mx, my, pt);
                if (!b.isHovering(mx, my) || !(d2 = Night.CLICK_GUI.getDescriptionFrame()).getDescription().isEmpty()) continue;
                d2.setDescription(b.getDescription());
            }
            g.disableScissor();
        } else {
            for (Button b : kids) {
                ColorGui.prevOff(b);
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

    public static Color catAccent(int n) {
        return CAT[Math.floorMod(n, CAT.length)];
    }

    public static void resetCur() {
    }

    public static Color accent(int n) {
        return PURPLE;
    }

    public static int overviewRgb(String string) {
        if (string == null) {
            return 9133302;
        }
        switch (string.toUpperCase()) {
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

    public static void card(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float rad, boolean tl, boolean tr, boolean br, boolean bl, Color c1, Color c2) {
        LightningSkin.rr(g, x1, y1, x2, y2, 8.0f, tl, tr, br, bl, PANEL, PANEL);
        LightningSkin.ring(g, x1, y1, x2, y2, 8.0f, 0.75f, EDGE);
    }

    public static void noGlow(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float rad, Color c) {
    }

    public static void noRing(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float rad, float t, Color c) {
    }

    public static void noSparkle(GuiGraphicsExtractor g, float x, float y, float rad, Color c) {
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

    public static Matrix3x2fStack poseHook(GuiGraphicsExtractor g) {
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
        return HeaderFx.poseHook(g);
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
            Frame f = ColorGui.byCat(list, category);
            if (f == null) continue;
            out.add(f);
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
        for (Frame f : list) {
            if (out.contains(f)) continue;
            out.add(f);
        }
        list.clear();
        list.addAll(out);
    }

    private static boolean deviates(ArrayList<Frame> list) {
        for (Frame frame : list) {
            int[] p = placed.get(frame);
            if (p == null) {
                return true;
            }
            if (frame.getX() == p[0] && frame.getY() == p[1] && frame.getWidth() == p[2] && frame.getMaxViewHeight() == p[3]) continue;
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
        for (Frame f : frames) {
            if (!f.isDragging()) continue;
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
        for (Frame f : main = new Frame[]{combat, player, visuals, movement, misc}) {
            if (f != null) continue;
            return;
        }
        int w = 112;
        for (Frame f : frames) {
            w = Math.max(w, f.getRequiredWidth());
        }
        for (Frame f : frames) {
            f.setWidth(w);
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
        for (Frame f : frames) {
            placed.put(f, new int[]{f.getX(), f.getY(), f.getWidth(), f.getMaxViewHeight()});
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

