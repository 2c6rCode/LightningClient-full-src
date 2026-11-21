/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 */
package lightning.gui;

import java.awt.Color;
import java.util.Arrays;
import java.util.Random;
import lightning.gui.ColorGui;
import lightning.gui.RefSkin;
import lightning.managers.FontManager;
import lightning.utils.graphics.GuiQuadRenderState;
import lightning.utils.graphics.MenuShader;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public final class LightningSkin {
    private static final int VR = 181;
    private static final int VG = 139;
    private static final int VB = 255;
    private static final int AR = 56;
    private static final int AG = 242;
    private static final int AB = 196;
    private static final float PR = 4.5f;
    private static final float RR = 3.5f;
    private static final float STEP = 0.5f;
    private static final Color PANEL = new Color(8, 8, 8, 222);
    private static final Color ROW = new Color(24, 24, 24, 150);
    private static final float[] PX = new float[]{752.0f, 853.0f, 666.0f, 928.0f, 477.0f, 589.0f, 351.0f};
    private static final float[] PY = new float[]{125.0f, 125.0f, 539.0f, 539.0f, 1154.0f, 690.0f, 690.0f};
    private static final long T0 = System.currentTimeMillis();
    static float timeOverride = -1.0f;
    private static float[] BX = new float[1024];
    private static float[] BY = new float[1024];
    private static int[] BC = new int[1024];
    private static int bn = 0;
    private static final float FEATHER = 0.55f;

    private LightningSkin() {
    }

    public static Color accent(int n) {
        float f = (float)(Math.abs(n) % 480) / 240.0f;
        float f2 = f <= 1.0f ? f : 2.0f - f;
        return LightningSkin.mix(f2, 255);
    }

    private static int lerp(int n, int n2, float f) {
        return Math.round((float)n + (float)(n2 - n) * f);
    }

    private static Color mix(float f, int n) {
        return new Color(LightningSkin.lerp(181, 56, f), LightningSkin.lerp(139, 242, f), LightningSkin.lerp(255, 196, f), n);
    }

    private static Color volt(int n) {
        return new Color(181, 139, 255, n);
    }

    private static Color arc(int n) {
        return new Color(56, 242, 196, n);
    }

    private static Color lerpC(Color color, Color color2, float f) {
        f = Math.max(0.0f, Math.min(1.0f, f));
        return new Color(LightningSkin.lerp(color.getRed(), color2.getRed(), f), LightningSkin.lerp(color.getGreen(), color2.getGreen(), f), LightningSkin.lerp(color.getBlue(), color2.getBlue(), f), LightningSkin.lerp(color.getAlpha(), color2.getAlpha(), f));
    }

    private static void bq(float f, float f2, float f3, float f4, int n, int n2) {
        if (f3 <= f || f4 <= f2) {
            return;
        }
        if (bn + 4 > BX.length) {
            int n3 = BX.length * 2;
            BX = Arrays.copyOf(BX, n3);
            BY = Arrays.copyOf(BY, n3);
            BC = Arrays.copyOf(BC, n3);
        }
        LightningSkin.BX[LightningSkin.bn] = f;
        LightningSkin.BY[LightningSkin.bn] = f2;
        LightningSkin.BC[LightningSkin.bn] = n;
        LightningSkin.BX[++LightningSkin.bn] = f;
        LightningSkin.BY[LightningSkin.bn] = f4;
        LightningSkin.BC[LightningSkin.bn] = n;
        LightningSkin.BX[++LightningSkin.bn] = f3;
        LightningSkin.BY[LightningSkin.bn] = f4;
        LightningSkin.BC[LightningSkin.bn] = n2;
        LightningSkin.BX[++LightningSkin.bn] = f3;
        LightningSkin.BY[LightningSkin.bn] = f2;
        LightningSkin.BC[LightningSkin.bn] = n2;
        ++bn;
    }

    private static void bqc(float f, float f2, float f3, float f4, Color color) {
        int n = color.getRGB();
        LightningSkin.bq(f, f2, f3, f4, n, n);
    }

    private static void flush(GuiGraphicsExtractor guiGraphicsExtractor) {
        if (bn == 0) {
            return;
        }
        int n = bn;
        bn = 0;
        GuiRenderState guiRenderState = ((lightning.mixins.accessors.GuiGraphicsExtractorAccessor)guiGraphicsExtractor).night$getGuiRenderState();
        guiRenderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState((Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)guiGraphicsExtractor.pose()), Arrays.copyOf(BX, n), Arrays.copyOf(BY, n), Arrays.copyOf(BC, n), Renderer2D.peekScissor(guiGraphicsExtractor)));
    }

    private static void strip(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, float f6, Color color, Color color2) {
        if (f2 <= f || f4 <= f3) {
            return;
        }
        if (color == color2) {
            LightningSkin.bqc(f, f3, f2, f4, color);
            return;
        }
        float f7 = f6 - f5;
        LightningSkin.bq(f, f3, f2, f4, LightningSkin.lerpC(color, color2, (f - f5) / f7).getRGB(), LightningSkin.lerpC(color, color2, (f2 - f5) / f7).getRGB());
    }

    private static float inset(float f, float f2) {
        float f3 = f * f - f2 * f2;
        return f - (float)Math.sqrt(Math.max(0.0f, f3));
    }

    static void rr(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float rad, boolean tl, boolean tr, boolean br, boolean bl, Color color, Color color2) {
        float ins;
        float d;
        float ye;
        float y;
        float bot;
        float w = x2 - x1;
        float h = y2 - y1;
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        rad = Math.min(rad, Math.min(w, h) / 2.0f);
        float top = tl || tr ? rad : 0.0f;
        float f = bot = bl || br ? rad : 0.0f;
        if (rad < 0.05f) {
            LightningSkin.strip(g, x1, x2, y1, y2, x1, x2, color, color2);
            LightningSkin.flush(g);
            return;
        }
        for (y = y1; y < y1 + top - 0.001f; y += 0.5f) {
            ye = Math.min(y + 0.5f, y1 + top);
            d = y1 + rad - (y + ye) / 2.0f;
            ins = LightningSkin.inset(rad, d);
            LightningSkin.strip(g, x1 + (tl ? ins : 0.0f), x2 - (tr ? ins : 0.0f), y, ye, x1, x2, color, color2);
        }
        LightningSkin.strip(g, x1, x2, y1 + top, y2 - bot, x1, x2, color, color2);
        for (y = y2 - bot; y < y2 - 0.001f; y += 0.5f) {
            ye = Math.min(y + 0.5f, y2);
            d = (y + ye) / 2.0f - (y2 - rad);
            ins = LightningSkin.inset(rad, d);
            LightningSkin.strip(g, x1 + (bl ? ins : 0.0f), x2 - (br ? ins : 0.0f), y, ye, x1, x2, color, color2);
        }
        LightningSkin.flush(g);
    }

    static void rrAll(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, f, f2, f3, f4, f5, true, true, true, true, color, color);
    }

    static void ring(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float rad, float thick, Color color) {
        float i;
        float o;
        float d;
        float mid;
        float ye;
        float y;
        float w = x2 - x1;
        float h = y2 - y1;
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        if ((rad = Math.min(rad, Math.min(w, h) / 2.0f)) < 0.05f) {
            Renderer2D.renderOutline(g, x1, y1, x2, y2, color);
            return;
        }
        float inner = Math.max(0.0f, rad - thick);
        for (y = y1; y < y1 + rad - 0.001f; y += 0.5f) {
            ye = Math.min(y + 0.5f, y1 + rad);
            mid = (y + ye) / 2.0f;
            d = y1 + rad - mid;
            o = LightningSkin.inset(rad, d);
            if (mid < y1 + thick || d >= inner) {
                LightningSkin.bqc(x1 + o, y, x2 - o, ye, color);
                continue;
            }
            i = rad - (float)Math.sqrt(Math.max(0.0f, inner * inner - d * d));
            LightningSkin.bqc(x1 + o, y, x1 + i, ye, color);
            LightningSkin.bqc(x2 - i, y, x2 - o, ye, color);
        }
        for (y = y2 - rad; y < y2 - 0.001f; y += 0.5f) {
            ye = Math.min(y + 0.5f, y2);
            mid = (y + ye) / 2.0f;
            d = mid - (y2 - rad);
            o = LightningSkin.inset(rad, d);
            if (mid > y2 - thick || d >= inner) {
                LightningSkin.bqc(x1 + o, y, x2 - o, ye, color);
                continue;
            }
            i = rad - (float)Math.sqrt(Math.max(0.0f, inner * inner - d * d));
            LightningSkin.bqc(x1 + o, y, x1 + i, ye, color);
            LightningSkin.bqc(x2 - i, y, x2 - o, ye, color);
        }
        if (y2 - rad > y1 + rad) {
            LightningSkin.bqc(x1, y1 + rad, x1 + thick, y2 - rad, color);
            LightningSkin.bqc(x2 - thick, y1 + rad, x2, y2 - rad, color);
        }
        LightningSkin.flush(g);
    }

    public static void fq(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        float f5 = f3 - f;
        float f6 = f4 - f2;
        if (color.getAlpha() == 0) {
            return;
        }
        if (f6 <= 1.01f && f5 > 20.0f) {
            return;
        }
        if (f5 >= 4.0f && f6 >= 4.0f) {
            LightningSkin.rrAll(guiGraphicsExtractor, f, f2, f3, f4, Math.min(3.5f, Math.min(f5, f6) / 2.0f), color);
        } else {
            Renderer2D.renderQuad(guiGraphicsExtractor, f, f2, f3, f4, color);
        }
    }

    public static void ol(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        LightningSkin.glow(guiGraphicsExtractor, f, f2, f3, f4, color);
        ColorGui.panelOutline(guiGraphicsExtractor, f, f2, f3, f4, color);
    }

    public static void glow(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, Color c) {
        LightningSkin.glow(g, x1, y1, x2, y2, 4.5f, c);
    }

    public static void glow(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float r, Color c) {
        int layers = 8;
        float t = (float)(System.currentTimeMillis() - T0) / 1000.0f;
        float pulse = 0.85f + 0.15f * (float)Math.sin(t * 2.0f);
        for (int i = layers; i >= 1; --i) {
            float k = 1.0f - ((float)i - 1.0f) / (float)layers;
            int a = Math.round(120.0f * k * k * pulse);
            if (a <= 0) continue;
            LightningSkin.ring(g, x1 - (float)i, y1 - (float)i, x2 + (float)i, y2 + (float)i, r + (float)i, 1.0f, new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
        }
    }

    public static void dfTop(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, f, f2, f3, f4, 4.5f, true, true, false, false, color, color);
    }

    public static void dfBottom(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, f, f2, f3, f4, 4.5f, false, false, true, true, color, color);
    }

    public static void nop(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
    }

    public static void bolt(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, int n) {
        LightningSkin.boltFlat(guiGraphicsExtractor, f, f2, f3, RefSkin.pick(n), n);
    }

    private static int boltCross(float scanY, float[] xs) {
        int cnt = 0;
        for (int j = 0; j < 7; ++j) {
            int nx = (j + 1) % 7;
            float ya = PY[j];
            float yb = PY[nx];
            if (!(ya <= scanY && yb > scanY || yb <= scanY && ya > scanY)) continue;
            xs[cnt++] = PX[j] + (scanY - ya) * (PX[nx] - PX[j]) / (yb - ya);
        }
        if (cnt < 2) {
            return cnt;
        }
        if (xs[0] > xs[1]) {
            float t = xs[0];
            xs[0] = xs[1];
            xs[1] = t;
        }
        if (cnt >= 4) {
            if (xs[2] > xs[3]) {
                float t = xs[2];
                xs[2] = xs[3];
                xs[3] = t;
            }
            if (xs[0] > xs[2]) {
                float t = xs[0];
                xs[0] = xs[2];
                xs[2] = t;
                t = xs[1];
                xs[1] = xs[3];
                xs[3] = t;
            }
        }
        return cnt;
    }

    private static void boltFlat(GuiGraphicsExtractor g, float x, float y, float size, Color color, int alpha) {
        int rows = Math.max(4, Math.round(size * 2.0f));
        float k = size / 1029.0f;
        float rowH = size / (float)rows;
        float[] xs = new float[8];
        for (int i = 0; i < rows; ++i) {
            float scanY = 125.0f + ((float)i + 0.5f) / (float)rows * 1029.0f;
            int cnt = LightningSkin.boltCross(scanY, xs);
            if (cnt < 2) continue;
            Color c = color != null ? color : LightningSkin.mix((float)i / (float)(rows - 1), alpha);
            int p = 0;
            while (p + 1 < cnt) {
                float xa = x + (xs[p] - 351.0f) * k;
                float xb = x + (xs[p + 1] - 351.0f) * k;
                LightningSkin.bqc(xa, y + (float)i * rowH, xb, y + (float)(i + 1) * rowH, c);
                p += 2;
            }
        }
        LightningSkin.flush(g);
    }

    public static void headerBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, f, f2, f3, f4, 4.5f, true, true, false, false, PANEL, PANEL);
        float f5 = f + 2.0f;
        float f6 = f3 - 2.0f;
        float f7 = f2 + 1.5f;
        float f8 = f4 - 1.5f;
        float f9 = Math.min(4.0f, (f8 - f7) / 2.0f);
        LightningSkin.rr(guiGraphicsExtractor, f5, f7, f6, f8, f9, true, true, true, true, LightningSkin.volt(120), LightningSkin.arc(60));
        LightningSkin.ring(guiGraphicsExtractor, f5, f7, f6, f8, f9, 0.5f, LightningSkin.mix(0.35f, 150));
        float f10 = f8 - f7 - 2.5f;
        float f11 = f6 - 1.2f - f10;
        float f12 = f7 + (f8 - f7 - f10) / 2.0f;
        LightningSkin.rr(guiGraphicsExtractor, f11, f12, f11 + f10, f12 + f10, f10 / 2.0f, true, true, true, true, LightningSkin.volt(255), LightningSkin.arc(255));
        float f13 = f10 * 0.68f;
        LightningSkin.boltFlat(guiGraphicsExtractor, f11 + (f10 - f13 * 0.561f) / 2.0f, f12 + (f10 - f13) / 2.0f, f13, new Color(12, 12, 12, 235), 255);
    }

    public static void headerLine(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
    }

    public static void bodyBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, f, f2, f3, f4, 4.5f, false, false, true, true, PANEL, PANEL);
    }

    public static void idleRow(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        LightningSkin.rrAll(guiGraphicsExtractor, f, f2 + 0.5f, f3, f4, 3.5f, ROW);
    }

    public static void chargeFill(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        float f5 = f3 - f;
        if (f5 <= 0.0f) {
            return;
        }
        float f6 = f2 + 0.5f;
        float f7 = f4 - f6;
        LightningSkin.rr(guiGraphicsExtractor, f, f6, f3, f4, 3.5f, true, true, true, true, new Color(127, 127, 127, 175), new Color(127, 127, 127, 80));
        LightningSkin.rr(guiGraphicsExtractor, f + 0.5f, f6 + 0.5f, f3 - 0.5f, f6 + f7 / 2.0f, 3.0f, true, true, false, false, new Color(255, 255, 255, 30), new Color(255, 255, 255, 6));
        if (f5 > 6.0f) {
            LightningSkin.ring(guiGraphicsExtractor, f, f6, f3, f4, 3.5f, 0.5f, LightningSkin.mix(0.2f, 190));
            float f8 = Math.min(6.0f, f7 - 3.0f);
            LightningSkin.rr(guiGraphicsExtractor, f + 0.45f, f6 + (f7 - f8) / 2.0f, f + 1.25f, f6 + (f7 + f8) / 2.0f, 0.4f, true, true, true, true, new Color(255, 255, 255, 235), new Color(255, 255, 255, 235));
        }
    }

    public static void rowLine(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
    }

    private static float hash(float f) {
        float f2 = (float)Math.sin(f * 127.1f + 311.7f) * 43758.547f;
        return f2 - (float)Math.floor(f2);
    }

    private static void boltStep(GuiGraphicsExtractor g, float x, float y, float size, int alpha, float step, float grow) {
        int rows = Math.max(4, Math.round(size / step));
        float k = size / 1029.0f;
        float rowH = size / (float)rows;
        float[] xs = new float[8];
        for (int i = 0; i < rows; ++i) {
            float scanY = 125.0f + ((float)i + 0.5f) / (float)rows * 1029.0f;
            int cnt = LightningSkin.boltCross(scanY, xs);
            if (cnt < 2) continue;
            Color c = LightningSkin.mix((float)i / (float)(rows - 1), alpha);
            int p = 0;
            while (p + 1 < cnt) {
                float xa = x + (xs[p] - 351.0f) * k - grow;
                float xb = x + (xs[p + 1] - 351.0f) * k + grow;
                Renderer2D.renderQuad(g, xa, y + (float)i * rowH, xb, y + (float)(i + 1) * rowH, c);
                p += 2;
            }
        }
    }

    private static void strike(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, long l, float f, float f2) {
        int n3;
        Random random = new Random(l * 7919L + 13L);
        float f3 = (float)n * (0.1f + 0.8f * random.nextFloat());
        float f4 = (float)n2 * (0.55f + 0.3f * random.nextFloat());
        int n4 = 18;
        float[] fArray = new float[n4 + 1];
        float[] fArray2 = new float[n4 + 1];
        for (n3 = 0; n3 <= n4; ++n3) {
            fArray2[n3] = f4 * (float)n3 / (float)n4;
            fArray[n3] = f3;
            f3 += (random.nextFloat() - 0.5f) * (float)n2 * 0.075f;
        }
        LightningSkin.boltPath(guiGraphicsExtractor, fArray, fArray2, f, f2, 1.0f);
        n3 = 3 + random.nextInt(8);
        float f5 = random.nextBoolean() ? 1.0f : -1.0f;
        int n5 = 7;
        float[] fArray3 = new float[n5 + 1];
        float[] fArray4 = new float[n5 + 1];
        fArray3[0] = fArray[n3];
        fArray4[0] = fArray2[n3];
        for (int i = 1; i <= n5; ++i) {
            fArray3[i] = fArray3[i - 1] + f5 * (float)n2 * (0.02f + 0.03f * random.nextFloat());
            fArray4[i] = fArray4[i - 1] + (float)n2 * (0.02f + 0.03f * random.nextFloat());
        }
        LightningSkin.boltPath(guiGraphicsExtractor, fArray3, fArray4, f, f2, 0.55f);
    }

    private static void boltPath(GuiGraphicsExtractor guiGraphicsExtractor, float[] fArray, float[] fArray2, float f, float f2, float f3) {
        float f4 = 2.0f * f3;
        for (int i = 0; i < fArray.length - 1; ++i) {
            float f5 = fArray2[i];
            float f6 = fArray2[i + 1];
            float f7 = fArray[i];
            float f8 = fArray[i + 1];
            int n = Math.max(1, (int)Math.ceil((f6 - f5) / 2.0f));
            for (int j = 0; j < n; ++j) {
                float f9 = (float)j / (float)n;
                float f10 = f7 + (f8 - f7) * f9;
                float f11 = f5 + (f6 - f5) * f9;
                Color color = new Color(56, 242, 196, Math.round(46.0f * f * f2));
                Color color2 = new Color(245, 240, 255, Math.round(235.0f * f * f2));
                Renderer2D.renderQuad(guiGraphicsExtractor, f10 - f4 * 3.0f, f11, f10 + f4 * 3.0f, f11 + 2.0f, color);
                Renderer2D.renderQuad(guiGraphicsExtractor, f10 - f4 * 0.5f, f11, f10 + f4 * 0.5f, f11 + 2.0f, color2);
            }
        }
    }

    public static void menuFx(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        try {
            float f2 = Math.max(0.0f, Math.min(1.0f, f));
            float f3 = timeOverride >= 0.0f ? timeOverride : (float)(System.currentTimeMillis() - T0) / 1000.0f;
            float f4 = 4.2f;
            long l = (long)Math.floor(f3 / f4);
            float f5 = f3 - (float)l * f4;
            float f6 = (float)(Math.exp(-f5 * 6.0f) + 0.5 * Math.exp(-Math.abs(f5 - 0.25f) * 25.0f));
            if (f5 > 1.5f) {
                f6 = 0.0f;
            }
            f6 = Math.min(1.0f, f6);
            Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, n, n2, new Color(6, 4, 20, Math.round(165.0f * f2)));
            Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, n, n2, new Color(56, 242, 196, Math.round(40.0f * f2)), new Color(181, 139, 255, Math.round(34.0f * f2)));
            float f7 = (float)n2 * 0.84f;
            float f8 = (float)n * 0.5f - f7 * 0.561f * 0.5f;
            float f9 = (float)n2 * 0.08f;
            float f10 = 0.5f + 0.5f * (float)Math.sin(f3 * 0.9f);
            LightningSkin.boltStep(guiGraphicsExtractor, f8, f9, f7, Math.round((20.0f + 18.0f * f10 + 120.0f * f6) * f2), 2.0f, 5.0f);
            LightningSkin.boltStep(guiGraphicsExtractor, f8, f9, f7, Math.round((34.0f + 26.0f * f10 + 110.0f * f6) * f2), 2.0f, 0.0f);
            if (f5 < 0.55f) {
                float f11 = 0.65f + 0.35f * (float)((int)(f5 * 22.0f) % 2);
                LightningSkin.strike(guiGraphicsExtractor, n, n2, l, f11 * (1.0f - f5 / 0.55f * 0.35f), f2);
            }
            if (f6 > 0.01f) {
                Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, n, n2, new Color(200, 190, 255, Math.round(70.0f * f6 * f2)));
            }
            for (int i = 0; i < 38; ++i) {
                float f12 = (LightningSkin.hash((float)i * 1.3f) * (float)n + (float)Math.sin(f3 * 0.35f + (float)i) * 14.0f + (float)n) % (float)n;
                float f13 = 8.0f + 20.0f * LightningSkin.hash((float)i * 2.7f + 5.0f);
                float f14 = (float)n2 - (f3 * f13 + LightningSkin.hash((float)i * 4.1f) * (float)n2) % (float)n2;
                float f15 = 1.0f + 1.6f * LightningSkin.hash((float)i * 9.9f);
                int n3 = Math.round((50.0f + 150.0f * (0.5f + 0.5f * (float)Math.sin(f3 * 2.4f + (float)i * 1.7f))) * f2);
                Renderer2D.renderQuad(guiGraphicsExtractor, f12, f14, f12 + f15, f14 + f15, i % 3 == 0 ? LightningSkin.volt(n3) : LightningSkin.arc(n3));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void signature(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
        int n3 = 7;
        int n4 = Math.min(255, color.getAlpha() + 70);
        int n5 = fontManager.getWidth(string);
        int n6 = fontManager.getHeight();
        fontManager.drawTextWithShadow(guiGraphicsExtractor, string, n, n3, new Color(181, 139, 255, n4));
        Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, n, n3 + n6 + 1, n + n5, n3 + n6 + 2, LightningSkin.volt(n4), LightningSkin.arc(n4));
        float f = (float)n6 + 3.0f;
        LightningSkin.boltStep(guiGraphicsExtractor, (float)n - 5.0f - f * 0.561f, (float)n3 - 1.0f, f, n4, 1.0f, 0.0f);
        LightningSkin.boltStep(guiGraphicsExtractor, (float)(n + n5) + 5.0f, (float)n3 - 1.0f, f, n4, 1.0f, 0.0f);
    }

    public static void skipText(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
    }

    public static void screenBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        MenuShader.render(guiGraphicsExtractor, Math.round(f3), Math.round(f4), 1.0f, 1.0f);
        Renderer2D.renderQuad(guiGraphicsExtractor, f, f2, f3, f4, new Color(5, 5, 5, 95));
    }

    public static void topLine(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        Renderer2D.renderQuad(guiGraphicsExtractor, f, f2, f3, f4 + 1.0f, LightningSkin.accent((int)f));
    }

    private static int fade(Color c, float k) {
        int a = Math.max(0, Math.min(255, Math.round((float)c.getAlpha() * k)));
        return a << 24 | c.getRGB() & 0xFFFFFF;
    }

    private static void pv(float x, float y, int c) {
        LightningSkin.BX[LightningSkin.bn] = x;
        LightningSkin.BY[LightningSkin.bn] = y;
        LightningSkin.BC[LightningSkin.bn] = c;
        ++bn;
    }

    private static void qv(float x0, float y0, int c0, float x1, float y1, int c1, float x2, float y2, int c2, float x3, float y3, int c3) {
        float area;
        if (bn + 4 > BX.length) {
            int n = BX.length * 2;
            BX = Arrays.copyOf(BX, n);
            BY = Arrays.copyOf(BY, n);
            BC = Arrays.copyOf(BC, n);
        }
        if ((area = x0 * y1 - x1 * y0 + x1 * y2 - x2 * y1 + x2 * y3 - x3 * y2 + x3 * y0 - x0 * y3) > 0.0f) {
            LightningSkin.pv(x0, y0, c0);
            LightningSkin.pv(x3, y3, c3);
            LightningSkin.pv(x2, y2, c2);
            LightningSkin.pv(x1, y1, c1);
        } else {
            LightningSkin.pv(x0, y0, c0);
            LightningSkin.pv(x1, y1, c1);
            LightningSkin.pv(x2, y2, c2);
            LightningSkin.pv(x3, y3, c3);
        }
    }

    static void arcQ(float cx, float cy, float rIn, float rOut, float a0, float a1, Color col) {
        float sweep = a1 - a0;
        if (rOut <= rIn || Math.abs(sweep) < 1.0E-4f || col.getAlpha() == 0) {
            return;
        }
        float h = 0.275f;
        float k1 = rOut - h;
        boolean disc = rIn <= 0.0f;
        float k0 = disc ? 0.0f : rIn + h;
        if (k1 < k0) {
            k1 = k0 = (rIn + rOut) * 0.5f;
        }
        float o = rOut + h;
        float ri = Math.max(0.0f, rIn - h);
        int steps = Math.max(6, (int)Math.ceil(Math.abs(sweep) * rOut / 0.7f));
        int full = LightningSkin.fade(col, 1.0f);
        int none = LightningSkin.fade(col, 0.0f);
        float pc = (float)Math.cos(a0);
        float ps = (float)Math.sin(a0);
        for (int i = 1; i <= steps; ++i) {
            float t = a0 + sweep * (float)i / (float)steps;
            float c = (float)Math.cos(t);
            float s = (float)Math.sin(t);
            LightningSkin.qv(cx + pc * k0, cy + ps * k0, full, cx + pc * k1, cy + ps * k1, full, cx + c * k1, cy + s * k1, full, cx + c * k0, cy + s * k0, full);
            LightningSkin.qv(cx + pc * k1, cy + ps * k1, full, cx + pc * o, cy + ps * o, none, cx + c * o, cy + s * o, none, cx + c * k1, cy + s * k1, full);
            if (!disc) {
                LightningSkin.qv(cx + pc * ri, cy + ps * ri, none, cx + pc * k0, cy + ps * k0, full, cx + c * k0, cy + s * k0, full, cx + c * ri, cy + s * ri, none);
            }
            pc = c;
            ps = s;
        }
    }

    static void lineQ(float x0, float y0, float x1, float y1, float w, Color col) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        float hw = w * 0.5f;
        if (len > 1.0E-4f) {
            float nx = -dy / len;
            float ny = dx / len;
            float h = 0.275f;
            float ci = Math.max(0.0f, hw - h);
            float co = hw + h;
            int full = LightningSkin.fade(col, 1.0f);
            int none = LightningSkin.fade(col, 0.0f);
            LightningSkin.qv(x0 + nx * ci, y0 + ny * ci, full, x1 + nx * ci, y1 + ny * ci, full, x1 - nx * ci, y1 - ny * ci, full, x0 - nx * ci, y0 - ny * ci, full);
            LightningSkin.qv(x0 + nx * co, y0 + ny * co, none, x1 + nx * co, y1 + ny * co, none, x1 + nx * ci, y1 + ny * ci, full, x0 + nx * ci, y0 + ny * ci, full);
            LightningSkin.qv(x0 - nx * ci, y0 - ny * ci, full, x1 - nx * ci, y1 - ny * ci, full, x1 - nx * co, y1 - ny * co, none, x0 - nx * co, y0 - ny * co, none);
        }
        LightningSkin.arcQ(x0, y0, 0.0f, hw, 0.0f, (float)Math.PI * 2, col);
        LightningSkin.arcQ(x1, y1, 0.0f, hw, 0.0f, (float)Math.PI * 2, col);
    }

    static void commit(GuiGraphicsExtractor g) {
        LightningSkin.flush(g);
    }
}

