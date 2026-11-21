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

public final class GuiDraw {
    private GuiDraw() {
    }

    public static void rr(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float r, boolean tl, boolean tr, boolean br, boolean bl, Color c1, Color c2) {
        LightningSkin.rr(g, x1, y1, x2, y2, r, tl, tr, br, bl, c1, c2);
    }

    public static void rrAll(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float r, Color c) {
        LightningSkin.rrAll(g, x1, y1, x2, y2, r, c);
    }

    public static void ring(GuiGraphicsExtractor g, float x1, float y1, float x2, float y2, float r, float thickness, Color c) {
        LightningSkin.ring(g, x1, y1, x2, y2, r, thickness, c);
    }
}

