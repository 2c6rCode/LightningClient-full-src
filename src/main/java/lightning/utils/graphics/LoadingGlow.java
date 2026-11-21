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

    public static void draw(FontRenderer fontRenderer, GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2, float f3) {
        if (fontRenderer == null || f3 <= 0.01f) {
            return;
        }
        int n = LoadingGlow.col(f3 * 0.1f);
        int n2 = LoadingGlow.col(f3 * 0.22f);
        for (float[] fArray : RING2) {
            fontRenderer.drawString(guiGraphicsExtractor, string, f + fArray[0], f2 + fArray[1], n, false);
        }
        for (float[] fArray : RING1) {
            fontRenderer.drawString(guiGraphicsExtractor, string, f + fArray[0], f2 + fArray[1], n2, false);
        }
    }

    private static int col(float f) {
        int n = Math.max(0, Math.min(255, Math.round(f * 255.0f)));
        return n << 24 | 0xB90000 | 0x8200 | 0xFF;
    }
}

