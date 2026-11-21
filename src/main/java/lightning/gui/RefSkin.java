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

public final class RefSkin {
    private static float trackX;
    private static float trackY;
    private static boolean haveTrack;

    private RefSkin() {
    }

    public static Color pick(int n) {
        if (n == 253) {
            return new Color(168, 140, 255);
        }
        if (n == 254) {
            return new Color(56, 242, 196);
        }
        return null;
    }

    public static void fq(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        float f5 = f3 - f;
        float f6 = f4 - f2;
        if (f5 == 14.0f && f6 == 7.0f) {
            boolean bl = color.getRed() == 40 && color.getGreen() == 40 && color.getBlue() == 50 && color.getAlpha() == 160;
            Color color2 = bl ? new Color(60, 60, 60, 235) : new Color(127, 127, 127, 240);
            trackX = f;
            trackY = f2;
            haveTrack = true;
            LightningSkin.rrAll(guiGraphicsExtractor, f - 1.0f, f2 - 1.0f, f + 15.0f, f2 + 8.0f, 4.5f, color2);
            return;
        }
        if (f5 == 5.0f && f6 == 9.0f && haveTrack) {
            haveTrack = false;
            float f7 = (f - trackX) / 9.0f;
            if (f7 < 0.0f) {
                f7 = 0.0f;
            }
            if (f7 > 1.0f) {
                f7 = 1.0f;
            }
            float f8 = trackX + f7 * 7.0f;
            float f9 = trackY;
            LightningSkin.rrAll(guiGraphicsExtractor, f8, f9, f8 + 7.0f, f9 + 7.0f, 3.5f, color);
            return;
        }
        LightningSkin.fq(guiGraphicsExtractor, f, f2, f3, f4, color);
    }

    public static void ol(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        if (f3 - f == 14.0f && f4 - f2 == 7.0f) {
            return;
        }
        LightningSkin.ol(guiGraphicsExtractor, f, f2, f3, f4, color);
    }
}

