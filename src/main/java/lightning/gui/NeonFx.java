/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.gui;

import java.awt.Color;
import lightning.gui.LightningSkin;
import lightning.managers.FontManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class NeonFx {
    private NeonFx() {
    }

    public static void ring(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, float f6, Color color) {
        int n = color.getRed();
        int n2 = color.getGreen();
        int n3 = color.getBlue();
        LightningSkin.ring(guiGraphicsExtractor, f - 3.0f, f2 - 3.0f, f3 + 3.0f, f4 + 3.0f, f5 + 3.0f, 4.0f, new Color(n, n2, n3, 34));
        LightningSkin.ring(guiGraphicsExtractor, f - 1.0f, f2 - 1.0f, f3 + 1.0f, f4 + 1.0f, f5 + 1.0f, 2.4f, new Color(n, n2, n3, 90));
        LightningSkin.ring(guiGraphicsExtractor, f, f2, f3, f4, f5, 1.8f, new Color(n, n2, n3, 255));
    }

    public static void glowText(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color, int n3) {
        int n4 = color.getRed();
        if (n4 > 170 && string != null && !string.isEmpty()) {
            int n5 = Math.min(95, (int)((float)(n4 - 170) * 1.15f));
            if ((n5 = n5 * color.getAlpha() / 255) > 3) {
                Color color2 = new Color(n3 >> 16 & 0xFF, n3 >> 8 & 0xFF, n3 & 0xFF, n5);
                fontManager.drawText(guiGraphicsExtractor, string, n - 1, n2, color2);
                fontManager.drawText(guiGraphicsExtractor, string, n + 1, n2, color2);
                fontManager.drawText(guiGraphicsExtractor, string, n, n2 - 1, color2);
                fontManager.drawText(guiGraphicsExtractor, string, n, n2 + 1, color2);
            }
        }
        fontManager.drawTextWithShadow(guiGraphicsExtractor, string, n, n2, color);
    }
}

