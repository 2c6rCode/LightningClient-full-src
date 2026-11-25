/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.gui;

import java.awt.Color;
import lightning.gui.LightningSkin;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class ModuleOutline {
    private ModuleOutline() {
    }

    static void draw(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, int rgb, boolean enabled, boolean hovered, float progress) {
        if (!enabled || progress <= 0.01f || x2 - x1 < 6.0f || y2 - y1 < 6.0f) {
            return;
        }
        int baseAlpha = hovered ? 210 : 150;
        int alpha = Math.round((float)baseAlpha * Math.min(1.0f, progress));
        if (alpha > 255) {
            alpha = 255;
        }
        if (alpha < 0) {
            alpha = 0;
        }
        Color color = new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha);
        LightningSkin.ring(guiGraphicsExtractor, x1, y1, x2, y2, 4.0f, 1.7f, color);
    }
}

