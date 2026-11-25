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

    public static void ring(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, float radius, float thickness, Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        LightningSkin.ring(guiGraphicsExtractor, x1 - 3.0f, y1 - 3.0f, x2 + 3.0f, y2 + 3.0f, radius + 3.0f, 4.0f, new Color(red, green, blue, 34));
        LightningSkin.ring(guiGraphicsExtractor, x1 - 1.0f, y1 - 1.0f, x2 + 1.0f, y2 + 1.0f, radius + 1.0f, 2.4f, new Color(red, green, blue, 90));
        LightningSkin.ring(guiGraphicsExtractor, x1, y1, x2, y2, radius, 1.8f, new Color(red, green, blue, 255));
    }

    public static void glowText(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String text, int x, int y, Color color, int glowRgb) {
        int red = color.getRed();
        if (red > 170 && text != null && !text.isEmpty()) {
            int glowAlpha = Math.min(95, (int)((float)(red - 170) * 1.15f));
            if ((glowAlpha = glowAlpha * color.getAlpha() / 255) > 3) {
                Color glowColor = new Color(glowRgb >> 16 & 0xFF, glowRgb >> 8 & 0xFF, glowRgb & 0xFF, glowAlpha);
                fontManager.drawText(guiGraphicsExtractor, text, x - 1, y, glowColor);
                fontManager.drawText(guiGraphicsExtractor, text, x + 1, y, glowColor);
                fontManager.drawText(guiGraphicsExtractor, text, x, y - 1, glowColor);
                fontManager.drawText(guiGraphicsExtractor, text, x, y + 1, glowColor);
            }
        }
        fontManager.drawTextWithShadow(guiGraphicsExtractor, text, x, y, color);
    }
}

