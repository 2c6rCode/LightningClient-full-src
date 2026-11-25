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

    public static Color accent(int yOffset) {
        float cyclePos = (float)(Math.abs(yOffset) % 480) / 240.0f;
        float blend = cyclePos <= 1.0f ? cyclePos : 2.0f - cyclePos;
        return LightningSkin.mix(blend, 255);
    }

    private static int lerp(int a, int b, float t) {
        return Math.round((float)a + (float)(b - a) * t);
    }

    private static Color mix(float t, int alpha) {
        return new Color(LightningSkin.lerp(181, 56, t), LightningSkin.lerp(139, 242, t), LightningSkin.lerp(255, 196, t), alpha);
    }

    private static Color volt(int alpha) {
        return new Color(181, 139, 255, alpha);
    }

    private static Color arc(int alpha) {
        return new Color(56, 242, 196, alpha);
    }

    private static Color lerpC(Color color, Color color2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return new Color(LightningSkin.lerp(color.getRed(), color2.getRed(), t), LightningSkin.lerp(color.getGreen(), color2.getGreen(), t), LightningSkin.lerp(color.getBlue(), color2.getBlue(), t), LightningSkin.lerp(color.getAlpha(), color2.getAlpha(), t));
    }

    private static void bq(float x1, float y1, float x2, float y2, int leftColor, int rightColor) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        if (bn + 4 > BX.length) {
            int newCapacity = BX.length * 2;
            BX = Arrays.copyOf(BX, newCapacity);
            BY = Arrays.copyOf(BY, newCapacity);
            BC = Arrays.copyOf(BC, newCapacity);
        }
        LightningSkin.BX[LightningSkin.bn] = x1;
        LightningSkin.BY[LightningSkin.bn] = y1;
        LightningSkin.BC[LightningSkin.bn] = leftColor;
        LightningSkin.BX[++LightningSkin.bn] = x1;
        LightningSkin.BY[LightningSkin.bn] = y2;
        LightningSkin.BC[LightningSkin.bn] = leftColor;
        LightningSkin.BX[++LightningSkin.bn] = x2;
        LightningSkin.BY[LightningSkin.bn] = y2;
        LightningSkin.BC[LightningSkin.bn] = rightColor;
        LightningSkin.BX[++LightningSkin.bn] = x2;
        LightningSkin.BY[LightningSkin.bn] = y1;
        LightningSkin.BC[LightningSkin.bn] = rightColor;
        ++bn;
    }

    private static void bqc(float x1, float y1, float x2, float y2, Color color) {
        int rgba = color.getRGB();
        LightningSkin.bq(x1, y1, x2, y2, rgba, rgba);
    }

    private static void flush(GuiGraphicsExtractor guiGraphicsExtractor) {
        if (bn == 0) {
            return;
        }
        int count = bn;
        bn = 0;
        GuiRenderState guiRenderState = ((lightning.mixins.accessors.GuiGraphicsExtractorAccessor)guiGraphicsExtractor).night$getGuiRenderState();
        guiRenderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState((Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)guiGraphicsExtractor.pose()), Arrays.copyOf(BX, count), Arrays.copyOf(BY, count), Arrays.copyOf(BC, count), Renderer2D.peekScissor(guiGraphicsExtractor)));
    }

    private static void strip(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float x2, float y1, float y2, float gradStartX, float gradEndX, Color colorLeft, Color colorRight) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        if (colorLeft == colorRight) {
            LightningSkin.bqc(x1, y1, x2, y2, colorLeft);
            return;
        }
        float gradSpan = gradEndX - gradStartX;
        LightningSkin.bq(x1, y1, x2, y2, LightningSkin.lerpC(colorLeft, colorRight, (x1 - gradStartX) / gradSpan).getRGB(), LightningSkin.lerpC(colorLeft, colorRight, (x2 - gradStartX) / gradSpan).getRGB());
    }

    private static float inset(float radius, float dy) {
        float dxSq = radius * radius - dy * dy;
        return radius - (float)Math.sqrt(Math.max(0.0f, dxSq));
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
        bot = bl || br ? rad : 0.0f;
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

    static void rrAll(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, float radius, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, x1, y1, x2, y2, radius, true, true, true, true, color, color);
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

    public static void fq(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        float width = x2 - x1;
        float height = y2 - y1;
        if (color.getAlpha() == 0) {
            return;
        }
        if (height <= 1.01f && width > 20.0f) {
            return;
        }
        if (width >= 4.0f && height >= 4.0f) {
            LightningSkin.rrAll(guiGraphicsExtractor, x1, y1, x2, y2, Math.min(3.5f, Math.min(width, height) / 2.0f), color);
        } else {
            Renderer2D.renderQuad(guiGraphicsExtractor, x1, y1, x2, y2, color);
        }
    }

    public static void ol(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        LightningSkin.glow(guiGraphicsExtractor, x1, y1, x2, y2, color);
        ColorGui.panelOutline(guiGraphicsExtractor, x1, y1, x2, y2, color);
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

    public static void dfTop(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, x1, y1, x2, y2, 4.5f, true, true, false, false, color, color);
    }

    public static void dfBottom(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, x1, y1, x2, y2, 4.5f, false, false, true, true, color, color);
    }

    public static void nop(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
    }

    public static void bolt(GuiGraphicsExtractor guiGraphicsExtractor, float x, float y, float size, int alpha) {
        LightningSkin.boltFlat(guiGraphicsExtractor, x, y, size, RefSkin.pick(alpha), alpha);
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

    public static void headerBg(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, x1, y1, x2, y2, 4.5f, true, true, false, false, PANEL, PANEL);
        float innerX1 = x1 + 2.0f;
        float innerX2 = x2 - 2.0f;
        float innerY1 = y1 + 1.5f;
        float innerY2 = y2 - 1.5f;
        float innerRadius = Math.min(4.0f, (innerY2 - innerY1) / 2.0f);
        LightningSkin.rr(guiGraphicsExtractor, innerX1, innerY1, innerX2, innerY2, innerRadius, true, true, true, true, LightningSkin.volt(120), LightningSkin.arc(60));
        LightningSkin.ring(guiGraphicsExtractor, innerX1, innerY1, innerX2, innerY2, innerRadius, 0.5f, LightningSkin.mix(0.35f, 150));
        float badgeSize = innerY2 - innerY1 - 2.5f;
        float badgeX = innerX2 - 1.2f - badgeSize;
        float badgeY = innerY1 + (innerY2 - innerY1 - badgeSize) / 2.0f;
        LightningSkin.rr(guiGraphicsExtractor, badgeX, badgeY, badgeX + badgeSize, badgeY + badgeSize, badgeSize / 2.0f, true, true, true, true, LightningSkin.volt(255), LightningSkin.arc(255));
        float boltSize = badgeSize * 0.68f;
        LightningSkin.boltFlat(guiGraphicsExtractor, badgeX + (badgeSize - boltSize * 0.561f) / 2.0f, badgeY + (badgeSize - boltSize) / 2.0f, boltSize, new Color(12, 12, 12, 235), 255);
    }

    public static void headerLine(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
    }

    public static void bodyBg(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        LightningSkin.rr(guiGraphicsExtractor, x1, y1, x2, y2, 4.5f, false, false, true, true, PANEL, PANEL);
    }

    public static void idleRow(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        LightningSkin.rrAll(guiGraphicsExtractor, x1, y1 + 0.5f, x2, y2, 3.5f, ROW);
    }

    public static void chargeFill(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        float fillWidth = x2 - x1;
        if (fillWidth <= 0.0f) {
            return;
        }
        float fillTop = y1 + 0.5f;
        float fillHeight = y2 - fillTop;
        LightningSkin.rr(guiGraphicsExtractor, x1, fillTop, x2, y2, 3.5f, true, true, true, true, new Color(127, 127, 127, 175), new Color(127, 127, 127, 80));
        LightningSkin.rr(guiGraphicsExtractor, x1 + 0.5f, fillTop + 0.5f, x2 - 0.5f, fillTop + fillHeight / 2.0f, 3.0f, true, true, false, false, new Color(255, 255, 255, 30), new Color(255, 255, 255, 6));
        if (fillWidth > 6.0f) {
            LightningSkin.ring(guiGraphicsExtractor, x1, fillTop, x2, y2, 3.5f, 0.5f, LightningSkin.mix(0.2f, 190));
            float pipHeight = Math.min(6.0f, fillHeight - 3.0f);
            LightningSkin.rr(guiGraphicsExtractor, x1 + 0.45f, fillTop + (fillHeight - pipHeight) / 2.0f, x1 + 1.25f, fillTop + (fillHeight + pipHeight) / 2.0f, 0.4f, true, true, true, true, new Color(255, 255, 255, 235), new Color(255, 255, 255, 235));
        }
    }

    public static void rowLine(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
    }

    private static float hash(float seed) {
        float val = (float)Math.sin(seed * 127.1f + 311.7f) * 43758.547f;
        return val - (float)Math.floor(val);
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

    private static void strike(GuiGraphicsExtractor guiGraphicsExtractor, int width, int height, long cycleIndex, float intensity, float screenAlpha) {
        int idx;
        Random random = new Random(cycleIndex * 7919L + 13L);
        float curX = (float)width * (0.1f + 0.8f * random.nextFloat());
        float strikeHeight = (float)height * (0.55f + 0.3f * random.nextFloat());
        int segments = 18;
        float[] xs = new float[segments + 1];
        float[] ys = new float[segments + 1];
        for (idx = 0; idx <= segments; ++idx) {
            ys[idx] = strikeHeight * (float)idx / (float)segments;
            xs[idx] = curX;
            curX += (random.nextFloat() - 0.5f) * (float)height * 0.075f;
        }
        LightningSkin.boltPath(guiGraphicsExtractor, xs, ys, intensity, screenAlpha, 1.0f);
        idx = 3 + random.nextInt(8);
        float branchDir = random.nextBoolean() ? 1.0f : -1.0f;
        int branchSegments = 7;
        float[] branchXs = new float[branchSegments + 1];
        float[] branchYs = new float[branchSegments + 1];
        branchXs[0] = xs[idx];
        branchYs[0] = ys[idx];
        for (int i = 1; i <= branchSegments; ++i) {
            branchXs[i] = branchXs[i - 1] + branchDir * (float)height * (0.02f + 0.03f * random.nextFloat());
            branchYs[i] = branchYs[i - 1] + (float)height * (0.02f + 0.03f * random.nextFloat());
        }
        LightningSkin.boltPath(guiGraphicsExtractor, branchXs, branchYs, intensity, screenAlpha, 0.55f);
    }

    private static void boltPath(GuiGraphicsExtractor guiGraphicsExtractor, float[] xs, float[] ys, float intensity, float screenAlpha, float scale) {
        float halfWidth = 2.0f * scale;
        for (int i = 0; i < xs.length - 1; ++i) {
            float y0 = ys[i];
            float y1 = ys[i + 1];
            float x0 = xs[i];
            float x1 = xs[i + 1];
            int steps = Math.max(1, (int)Math.ceil((y1 - y0) / 2.0f));
            for (int j = 0; j < steps; ++j) {
                float t = (float)j / (float)steps;
                float px = x0 + (x1 - x0) * t;
                float py = y0 + (y1 - y0) * t;
                Color glowColor = new Color(56, 242, 196, Math.round(46.0f * intensity * screenAlpha));
                Color coreColor = new Color(245, 240, 255, Math.round(235.0f * intensity * screenAlpha));
                Renderer2D.renderQuad(guiGraphicsExtractor, px - halfWidth * 3.0f, py, px + halfWidth * 3.0f, py + 2.0f, glowColor);
                Renderer2D.renderQuad(guiGraphicsExtractor, px - halfWidth * 0.5f, py, px + halfWidth * 0.5f, py + 2.0f, coreColor);
            }
        }
    }

    public static void menuFx(GuiGraphicsExtractor guiGraphicsExtractor, int width, int height, float alpha) {
        try {
            float clampedAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
            float timeSec = timeOverride >= 0.0f ? timeOverride : (float)(System.currentTimeMillis() - T0) / 1000.0f;
            float cyclePeriod = 4.2f;
            long cycleIndex = (long)Math.floor(timeSec / cyclePeriod);
            float cycleTime = timeSec - (float)cycleIndex * cyclePeriod;
            float flash = (float)(Math.exp(-cycleTime * 6.0f) + 0.5 * Math.exp(-Math.abs(cycleTime - 0.25f) * 25.0f));
            if (cycleTime > 1.5f) {
                flash = 0.0f;
            }
            flash = Math.min(1.0f, flash);
            Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, width, height, new Color(6, 4, 20, Math.round(165.0f * clampedAlpha)));
            Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, width, height, new Color(56, 242, 196, Math.round(40.0f * clampedAlpha)), new Color(181, 139, 255, Math.round(34.0f * clampedAlpha)));
            float boltSize = (float)height * 0.84f;
            float boltX = (float)width * 0.5f - boltSize * 0.561f * 0.5f;
            float boltY = (float)height * 0.08f;
            float pulse = 0.5f + 0.5f * (float)Math.sin(timeSec * 0.9f);
            LightningSkin.boltStep(guiGraphicsExtractor, boltX, boltY, boltSize, Math.round((20.0f + 18.0f * pulse + 120.0f * flash) * clampedAlpha), 2.0f, 5.0f);
            LightningSkin.boltStep(guiGraphicsExtractor, boltX, boltY, boltSize, Math.round((34.0f + 26.0f * pulse + 110.0f * flash) * clampedAlpha), 2.0f, 0.0f);
            if (cycleTime < 0.55f) {
                float strikeIntensity = 0.65f + 0.35f * (float)((int)(cycleTime * 22.0f) % 2);
                LightningSkin.strike(guiGraphicsExtractor, width, height, cycleIndex, strikeIntensity * (1.0f - cycleTime / 0.55f * 0.35f), clampedAlpha);
            }
            if (flash > 0.01f) {
                Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, width, height, new Color(200, 190, 255, Math.round(70.0f * flash * clampedAlpha)));
            }
            for (int i = 0; i < 38; ++i) {
                float px = (LightningSkin.hash((float)i * 1.3f) * (float)width + (float)Math.sin(timeSec * 0.35f + (float)i) * 14.0f + (float)width) % (float)width;
                float speed = 8.0f + 20.0f * LightningSkin.hash((float)i * 2.7f + 5.0f);
                float py = (float)height - (timeSec * speed + LightningSkin.hash((float)i * 4.1f) * (float)height) % (float)height;
                float size = 1.0f + 1.6f * LightningSkin.hash((float)i * 9.9f);
                int sparkAlpha = Math.round((50.0f + 150.0f * (0.5f + 0.5f * (float)Math.sin(timeSec * 2.4f + (float)i * 1.7f))) * clampedAlpha);
                Renderer2D.renderQuad(guiGraphicsExtractor, px, py, px + size, py + size, i % 3 == 0 ? LightningSkin.volt(sparkAlpha) : LightningSkin.arc(sparkAlpha));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void signature(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String text, int x, int y, Color color) {
        int textY = 7;
        int alpha = Math.min(255, color.getAlpha() + 70);
        int textWidth = fontManager.getWidth(text);
        int textHeight = fontManager.getHeight();
        fontManager.drawTextWithShadow(guiGraphicsExtractor, text, x, textY, new Color(181, 139, 255, alpha));
        Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, x, textY + textHeight + 1, x + textWidth, textY + textHeight + 2, LightningSkin.volt(alpha), LightningSkin.arc(alpha));
        float boltSize = (float)textHeight + 3.0f;
        LightningSkin.boltStep(guiGraphicsExtractor, (float)x - 5.0f - boltSize * 0.561f, (float)textY - 1.0f, boltSize, alpha, 1.0f, 0.0f);
        LightningSkin.boltStep(guiGraphicsExtractor, (float)(x + textWidth) + 5.0f, (float)textY - 1.0f, boltSize, alpha, 1.0f, 0.0f);
    }

    public static void skipText(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String text, int x, int y, Color color) {
    }

    public static void screenBg(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        MenuShader.render(guiGraphicsExtractor, Math.round(x2), Math.round(y2), 1.0f, 1.0f);
        Renderer2D.renderQuad(guiGraphicsExtractor, x1, y1, x2, y2, new Color(5, 5, 5, 95));
    }

    public static void topLine(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        Renderer2D.renderQuad(guiGraphicsExtractor, x1, y1, x2, y2 + 1.0f, LightningSkin.accent((int)x1));
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
            int newCapacity = BX.length * 2;
            BX = Arrays.copyOf(BX, newCapacity);
            BY = Arrays.copyOf(BY, newCapacity);
            BC = Arrays.copyOf(BC, newCapacity);
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

