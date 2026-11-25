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

    public static void render(GuiGraphicsExtractor guiGraphicsExtractor, int width, int height, float alpha, float progress, boolean finished) {
        long now = System.currentTimeMillis();
        if (start < 0L || now - lastFrame > 3000L) {
            start = now;
            shown = 0.0f;
        }
        lastFrame = now;
        long elapsedMs = LoadingClock.tick(now);
        LoadingText.drawBackground(guiGraphicsExtractor, width, height, alpha);
        LoadingText.initFonts();
        if (title == null) {
            return;
        }
        float targetProgress = finished ? 1.0f : Math.max(0.0f, Math.min(1.0f, progress));
        shown += (targetProgress - shown) * 0.12f;
        if (finished && shown > 0.995f) {
            shown = 1.0f;
        }
        int length = TEXT.length();
        float[] charWidths = new float[length];
        float totalWidth = 0.0f;
        for (int i = 0; i < length; ++i) {
            charWidths[i] = title.getTextWidth(String.valueOf(TEXT.charAt(i)));
            totalWidth += charWidths[i];
        }
        float textHeight = title.getHeight();
        float startX = (float)width / 2.0f - totalWidth / 2.0f;
        float baseY = (float)height / 2.0f - textHeight / 2.0f - 14.0f;
        float cursorX = startX;
        for (int i = 0; i < length; ++i) {
            float charProgress = (float)(elapsedMs - (long)i * 200L) / 1400.0f;
            if (charProgress > 0.0f) {
                float clampedProgress = Math.min(charProgress, 1.0f);
                float charY = baseY - 120.0f * (1.0f - LoadingText.bounce(clampedProgress));
                float charAlpha = Math.min(1.0f, clampedProgress * 3.0f) * alpha;
                float gradientT = (float)i / (float)(length - 1);
                int charColor = LoadingText.argb(charAlpha, LoadingText.lerp(240, 127, gradientT), LoadingText.lerp(210, 240, gradientT), LoadingText.lerp(255, 255, gradientT));
                LoadingGlow.draw(title, guiGraphicsExtractor, String.valueOf(TEXT.charAt(i)), cursorX, charY, charAlpha);
                title.drawString(guiGraphicsExtractor, String.valueOf(TEXT.charAt(i)), cursorX, charY, charColor, false);
            }
            cursorX += charWidths[i];
        }
        float barAlpha = Math.min(1.0f, Math.max(0.0f, ((float)elapsedMs - 500.0f) / 400.0f)) * alpha;
        if (barAlpha <= 0.0f) {
            return;
        }
        float barWidth = totalWidth * 0.92f;
        float barHeight = 6.0f;
        float barX = (float)width / 2.0f - barWidth / 2.0f;
        float barY = baseY + textHeight + 18.0f;
        LoadingText.roundedBar(guiGraphicsExtractor, barX, barY, barWidth, barHeight, 1.0f, TRACK, TRACK, barAlpha * 0.85f);
        float fillWidth = barWidth * shown;
        if (fillWidth > 1.0f) {
            LoadingText.roundedBar(guiGraphicsExtractor, barX, barY, fillWidth, barHeight, fillWidth / barWidth, new Color(220, 185, 255), new Color(127, 240, 255), barAlpha);
        }
        String percentText = Math.round(shown * 100.0f) + "%";
        float percentWidth = small.getTextWidth(percentText);
        small.drawString(guiGraphicsExtractor, percentText, (float)width / 2.0f - percentWidth / 2.0f, barY + barHeight + 8.0f, LoadingText.argb(barAlpha, 245, 230, 255), false);
    }

    private static void drawBackground(GuiGraphicsExtractor guiGraphicsExtractor, int width, int height, float alpha) {
        if (bg == null) {
            bg = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/gui/moonlight_lg.png");
        }
        int alphaByte = Math.max(0, Math.min(255, Math.round(alpha * 255.0f)));
        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, bg, 0, 0, 0.0f, 0.0f, width, height, width, height, width, height, alphaByte << 24 | 0xFFFFFF);
    }

    private static void roundedBar(GuiGraphicsExtractor guiGraphicsExtractor, float x, float y, float width, float height, float progress, Color startColor, Color endColor, float alpha) {
        float radius = height / 2.0f;
        Color filledEndColor = LoadingText.mix(startColor, endColor, Math.min(1.0f, progress));
        for (int i = 0; i < (int)height; ++i) {
            float rowLeft;
            float dy = radius - ((float)i + 0.5f);
            float inset = radius - (float)Math.sqrt(Math.max(0.0f, radius * radius - dy * dy));
            float rowRight = x + width - inset;
            if (rowRight <= (rowLeft = x + inset)) continue;
            float tLeft = (rowLeft - x) / width;
            float tRight = (rowRight - x) / width;
            Color leftColor = LoadingText.withA(LoadingText.mix(startColor, filledEndColor, tLeft), alpha);
            Color rightColor = LoadingText.withA(LoadingText.mix(startColor, filledEndColor, tRight), alpha);
            Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, rowLeft, y + (float)i, rowRight, y + (float)i + 1.0f, leftColor, rightColor);
        }
    }

    private static Color mix(Color first, Color second, float t) {
        return new Color(LoadingText.lerp(first.getRed(), second.getRed(), t), LoadingText.lerp(first.getGreen(), second.getGreen(), t), LoadingText.lerp(first.getBlue(), second.getBlue(), t));
    }

    private static Color withA(Color color, float alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, Math.round(alpha * 255.0f))));
    }

    private static int lerp(int start, int end, float t) {
        return Math.round((float)start + (float)(end - start) * t);
    }

    private static int argb(float alpha, int red, int green, int blue) {
        return Math.max(0, Math.min(255, Math.round(alpha * 255.0f))) << 24 | red << 16 | green << 8 | blue;
    }

    private static float bounce(float t) {
        float n1 = 7.5625f;
        float d1 = 2.75f;
        if (t < 1.0f / d1) {
            return n1 * t * t;
        }
        if (t < 2.0f / d1) {
            return n1 * (t -= 1.5f / d1) * t + 0.75f;
        }
        if (t < 2.5f / d1) {
            return n1 * (t -= 2.25f / d1) * t + 0.9375f;
        }
        return n1 * (t -= 2.625f / d1) * t + 0.984375f;
    }
}

