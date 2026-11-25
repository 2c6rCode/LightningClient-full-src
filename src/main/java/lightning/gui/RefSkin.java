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

    public static Color pick(int alpha) {
        if (alpha == 253) {
            return new Color(168, 140, 255);
        }
        if (alpha == 254) {
            return new Color(56, 242, 196);
        }
        return null;
    }

    public static void fq(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        float width = x2 - x1;
        float height = y2 - y1;
        if (width == 14.0f && height == 7.0f) {
            boolean isOffTrack = color.getRed() == 40 && color.getGreen() == 40 && color.getBlue() == 50 && color.getAlpha() == 160;
            Color trackColor = isOffTrack ? new Color(60, 60, 60, 235) : new Color(127, 127, 127, 240);
            trackX = x1;
            trackY = y1;
            haveTrack = true;
            LightningSkin.rrAll(guiGraphicsExtractor, x1 - 1.0f, y1 - 1.0f, x1 + 15.0f, y1 + 8.0f, 4.5f, trackColor);
            return;
        }
        if (width == 5.0f && height == 9.0f && haveTrack) {
            haveTrack = false;
            float t = (x1 - trackX) / 9.0f;
            if (t < 0.0f) {
                t = 0.0f;
            }
            if (t > 1.0f) {
                t = 1.0f;
            }
            float knobX = trackX + t * 7.0f;
            float knobY = trackY;
            LightningSkin.rrAll(guiGraphicsExtractor, knobX, knobY, knobX + 7.0f, knobY + 7.0f, 3.5f, color);
            return;
        }
        LightningSkin.fq(guiGraphicsExtractor, x1, y1, x2, y2, color);
    }

    public static void ol(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        if (x2 - x1 == 14.0f && y2 - y1 == 7.0f) {
            return;
        }
        LightningSkin.ol(guiGraphicsExtractor, x1, y1, x2, y2, color);
    }
}

