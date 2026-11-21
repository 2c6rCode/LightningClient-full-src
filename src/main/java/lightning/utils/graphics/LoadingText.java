/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.resources.Identifier
 */
package lightning.utils.graphics;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import lightning.utils.font.FontRenderer;
import lightning.utils.graphics.LoadingClock;
import lightning.utils.graphics.LoadingGlow;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class LoadingText {
    private static final String TEXT = "LightningClient";
    private static final long STAGGER_MS = 200L;
    private static final long DROP_MS = 1400L;
    private static final float DROP_HEIGHT = 120.0f;
    private static final Color TRACK = new Color(70, 45, 120);
    private static long start = -1L;
    private static float shown = 0.0f;
    private static long lastFrame = -1L;
    private static FontRenderer title;
    private static FontRenderer small;
    private static boolean fontFailed;
    private static Identifier bg;

    private LoadingText() {
    }

    private static void initFonts() {
        if (title != null || fontFailed) {
            return;
        }
        try (InputStream inputStream = LoadingText.class.getResourceAsStream("/assets/lightning/fonts/LightningRound.ttf");){
            Font font = Font.createFont(0, inputStream);
            title = new FontRenderer(new Font[]{font}, 40.0f);
            small = new FontRenderer(new Font[]{font}, 17.0f);
        }
        catch (Throwable throwable) {
            fontFailed = true;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean ready() {
        if (start < 0L) return false;
        if (LoadingClock.elapsed() <= (long)(TEXT.length() - 1) * 200L + 1400L + 600L) return false;
        return true;
    }

    public static void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, float f2, boolean bl) {
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        long l = System.currentTimeMillis();
        if (start < 0L || l - lastFrame > 3000L) {
            start = l;
            shown = 0.0f;
        }
        lastFrame = l;
        long l2 = LoadingClock.tick(l);
        float f8 = Math.min(0.1f, (float)(l - lastFrame) / 1000.0f);
        LoadingText.drawBackground(guiGraphicsExtractor, n, n2, f);
        LoadingText.initFonts();
        if (title == null) {
            return;
        }
        float f9 = bl ? 1.0f : Math.max(0.0f, Math.min(1.0f, f2));
        shown += (f9 - shown) * 0.12f;
        if (bl && shown > 0.995f) {
            shown = 1.0f;
        }
        int n3 = TEXT.length();
        float[] fArray = new float[n3];
        float f10 = 0.0f;
        for (int i = 0; i < n3; ++i) {
            fArray[i] = title.getTextWidth(String.valueOf(TEXT.charAt(i)));
            f10 += fArray[i];
        }
        float f11 = title.getHeight();
        float f12 = (float)n / 2.0f - f10 / 2.0f;
        float f13 = (float)n2 / 2.0f - f11 / 2.0f - 14.0f;
        float f14 = f12;
        for (int i = 0; i < n3; ++i) {
            f7 = (float)(l2 - (long)i * 200L) / 1400.0f;
            if (f7 > 0.0f) {
                f6 = Math.min(f7, 1.0f);
                f5 = f13 - 120.0f * (1.0f - LoadingText.bounce(f6));
                f4 = Math.min(1.0f, f6 * 3.0f) * f;
                f3 = (float)i / (float)(n3 - 1);
                int n4 = LoadingText.argb(f4, LoadingText.lerp(240, 127, f3), LoadingText.lerp(210, 240, f3), LoadingText.lerp(255, 255, f3));
                LoadingGlow.draw(title, guiGraphicsExtractor, String.valueOf(TEXT.charAt(i)), f14, f5, f4);
                title.drawString(guiGraphicsExtractor, String.valueOf(TEXT.charAt(i)), f14, f5, n4, false);
            }
            f14 += fArray[i];
        }
        float f15 = Math.min(1.0f, Math.max(0.0f, ((float)l2 - 500.0f) / 400.0f)) * f;
        if (f15 <= 0.0f) {
            return;
        }
        f7 = f10 * 0.92f;
        f6 = 6.0f;
        f5 = (float)n / 2.0f - f7 / 2.0f;
        f4 = f13 + f11 + 18.0f;
        LoadingText.roundedBar(guiGraphicsExtractor, f5, f4, f7, f6, 1.0f, TRACK, TRACK, f15 * 0.85f);
        f3 = f7 * shown;
        if (f3 > 1.0f) {
            LoadingText.roundedBar(guiGraphicsExtractor, f5, f4, f3, f6, f3 / f7, new Color(220, 185, 255), new Color(127, 240, 255), f15);
        }
        String string = Math.round(shown * 100.0f) + "%";
        float f16 = small.getTextWidth(string);
        small.drawString(guiGraphicsExtractor, string, (float)n / 2.0f - f16 / 2.0f, f4 + f6 + 8.0f, LoadingText.argb(f15, 245, 230, 255), false);
    }

    private static void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        if (bg == null) {
            bg = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/gui/moonlight_lg.png");
        }
        int n3 = Math.max(0, Math.min(255, Math.round(f * 255.0f)));
        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, bg, 0, 0, 0.0f, 0.0f, n, n2, n, n2, n, n2, n3 << 24 | 0xFFFFFF);
        int n4 = Math.round(70.0f * f);
    }

    private static void roundedBar(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, Color color, Color color2, float f6) {
        float f7 = f4 / 2.0f;
        Color color3 = LoadingText.mix(color, color2, Math.min(1.0f, f5));
        for (int i = 0; i < (int)f4; ++i) {
            float f8;
            float f9 = f7 - ((float)i + 0.5f);
            float f10 = f7 - (float)Math.sqrt(Math.max(0.0f, f7 * f7 - f9 * f9));
            float f11 = f + f3 - f10;
            if (f11 <= (f8 = f + f10)) continue;
            float f12 = (f8 - f) / f3;
            float f13 = (f11 - f) / f3;
            Color color4 = LoadingText.withA(LoadingText.mix(color, color3, f12), f6);
            Color color5 = LoadingText.withA(LoadingText.mix(color, color3, f13), f6);
            Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, f8, f2 + (float)i, f11, f2 + (float)i + 1.0f, color4, color5);
        }
    }

    private static Color mix(Color color, Color color2, float f) {
        return new Color(LoadingText.lerp(color.getRed(), color2.getRed(), f), LoadingText.lerp(color.getGreen(), color2.getGreen(), f), LoadingText.lerp(color.getBlue(), color2.getBlue(), f));
    }

    private static Color withA(Color color, float f) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, Math.round(f * 255.0f))));
    }

    private static int lerp(int n, int n2, float f) {
        return Math.round((float)n + (float)(n2 - n) * f);
    }

    private static int argb(float f, int n, int n2, int n3) {
        return Math.max(0, Math.min(255, Math.round(f * 255.0f))) << 24 | n << 16 | n2 << 8 | n3;
    }

    private static float bounce(float f) {
        float f2 = 7.5625f;
        float f3 = 2.75f;
        if (f < 1.0f / f3) {
            return f2 * f * f;
        }
        if (f < 2.0f / f3) {
            return f2 * (f -= 1.5f / f3) * f + 0.75f;
        }
        if (f < 2.5f / f3) {
            return f2 * (f -= 2.25f / f3) * f + 0.9375f;
        }
        return f2 * (f -= 2.625f / f3) * f + 0.984375f;
    }
}

