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

    static void draw(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, int n, boolean bl, boolean bl2, float f5) {
        if (!bl || f5 <= 0.01f || f3 - f < 6.0f || f4 - f2 < 6.0f) {
            return;
        }
        int n2 = bl2 ? 210 : 150;
        int n3 = Math.round((float)n2 * Math.min(1.0f, f5));
        if (n3 > 255) {
            n3 = 255;
        }
        if (n3 < 0) {
            n3 = 0;
        }
        Color color = new Color(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, n3);
        LightningSkin.ring(guiGraphicsExtractor, f, f2, f3, f4, 4.0f, 1.7f, color);
    }
}

