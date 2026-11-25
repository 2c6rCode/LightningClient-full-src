/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.utils.graphics;

import lightning.utils.font.FontRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class LoadingGlow {
    private static final float[][] RING1 = new float[][]{{1.5f, 0.0f}, {-1.5f, 0.0f}, {0.0f, 1.5f}, {0.0f, -1.5f}, {1.1f, 1.1f}, {-1.1f, 1.1f}, {1.1f, -1.1f}, {-1.1f, -1.1f}};
    private static final float[][] RING2 = new float[][]{{3.2f, 0.0f}, {-3.2f, 0.0f}, {0.0f, 3.2f}, {0.0f, -3.2f}, {2.3f, 2.3f}, {-2.3f, 2.3f}, {2.3f, -2.3f}, {-2.3f, -2.3f}};

    public static void draw(FontRenderer fontRenderer, GuiGraphicsExtractor guiGraphicsExtractor, String text, float x, float y, float alpha) {
        if (fontRenderer == null || alpha <= 0.01f) {
            return;
        }
        int outerColor = LoadingGlow.col(alpha * 0.1f);
        int innerColor = LoadingGlow.col(alpha * 0.22f);
        for (float[] offset : RING2) {
            fontRenderer.drawString(guiGraphicsExtractor, text, x + offset[0], y + offset[1], outerColor, false);
        }
        for (float[] offset : RING1) {
            fontRenderer.drawString(guiGraphicsExtractor, text, x + offset[0], y + offset[1], innerColor, false);
        }
    }

    private static int col(float alpha) {
        int alphaByte = Math.max(0, Math.min(255, Math.round(alpha * 255.0f)));
        return alphaByte << 24 | 0xB90000 | 0x8200 | 0xFF;
    }
}

