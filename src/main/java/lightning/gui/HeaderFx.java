/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.joml.Matrix3x2fStack
 */
package lightning.gui;

import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.ColorGui;
import lightning.gui.LightningSkin;
import lightning.gui.api.DescriptionFrame;
import lightning.gui.api.Frame;
import lightning.gui.api.PingBypassFrame;
import lightning.managers.FontManager;
import lightning.modules.Module;
import lightning.utils.font.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

public final class HeaderFx {
    public static final int GAP = 10;
    public static final int MIN_PANEL_WIDTH = 96;
    private static final int HARD_MIN_WIDTH = 60;
    private static final int SCREEN_MARGIN = 8;
    private static FontRenderer headerFont;
    private static FontRenderer titleFont;
    private static FontRenderer headerSmall;
    private static float bgX1;
    private static float bgY1;
    private static float bgX2;
    private static float bgY2;
    private static float titleY;
    private static float titleCx;
    private static boolean haveBg;
    private static boolean failed;
    private static ClickGuiScreen current;
    private static float hx1;
    private static float hy1;
    private static float hx2;
    private static float hy2;
    private static FontRenderer countFont;

    private HeaderFx() {
    }

    private static String pickFamily(String ... stringArray) {
        try {
            HashSet<String> hashSet = new HashSet<String>();
            for (String string : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                hashSet.add(string);
            }
            for (String string : stringArray) {
                if (!hashSet.contains(string)) continue;
                return string;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "SansSerif";
    }

    private static Font[] chain(String string, int n) {
        return new Font[]{new Font(string, n, 1), new Font("Segoe UI Symbol", n, 1), new Font("Segoe UI Emoji", n, 1), new Font("Arial Unicode MS", n, 1), new Font("SansSerif", n, 1), new Font("Dialog", n, 1)};
    }

    private static void init() {
        if (headerFont != null || failed) {
            return;
        }
        try {
            String string = HeaderFx.pickFamily("Bahnschrift", "Segoe UI Black", "Segoe UI Semibold", "Segoe UI", "Verdana");
            headerFont = new FontRenderer(HeaderFx.chain(string, 1), 10.5f);
            titleFont = new FontRenderer(HeaderFx.chain(string, 1), 24.0f);
            headerSmall = new FontRenderer(HeaderFx.chain(string, 1), 8.3f);
        }
        catch (Throwable throwable) {
            failed = true;
        }
    }

    private static Color c(int n, int n2, int n3, int n4) {
        return new Color(n, n2, n3, n4);
    }

    public static void roundRect(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, Color color, Color color2) {
        LightningSkin.rr(guiGraphicsExtractor, f, f2, f3, f4, f5, true, true, true, true, color, color2);
    }

    public static void fitWidths(List<Frame> list, float f, int n) {
        int n2;
        int n3 = list.size();
        if (n3 == 0) {
            return;
        }
        int[] nArray = new int[n3];
        int n4 = 0;
        for (n2 = 0; n2 < n3; ++n2) {
            nArray[n2] = Math.max(96, list.get(n2).getRequiredWidth());
            n4 += nArray[n2];
        }
        n2 = Math.round(f) - 16 - (n3 - 1) * n;
        if (n4 > n2 && n4 > 0) {
            float f2 = (float)n2 / (float)n4;
            for (int i = 0; i < n3; ++i) {
                nArray[i] = Math.max(60, Math.round((float)nArray[i] * f2));
            }
        }
        for (int i = 0; i < n3; ++i) {
            list.get(i).setWidth(nArray[i]);
        }
    }

    public static void headerBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        ColorGui.headerBg(guiGraphicsExtractor, f, f2, f3, f4, color);
    }

    public static void bodyBg(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        ColorGui.bodyBg(guiGraphicsExtractor, f, f2, f3, f4, color);
    }

    public static void idleRow(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        float f5 = f2 + 0.75f;
        float f6 = f4 - 0.75f;
        float f7 = Math.min(4.0f, (f6 - f5) / 2.0f);
        LightningSkin.rr(guiGraphicsExtractor, f, f5, f3, f6, f7, true, true, true, true, HeaderFx.c(24, 22, 52, 140), HeaderFx.c(17, 15, 40, 140));
    }

    public static void chargeFill(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        float f5 = f3 - f;
        if (f5 <= 0.0f) {
            return;
        }
        float f6 = f2 + 0.75f;
        float f7 = f4 - 0.75f;
        float f8 = Math.min(4.0f, Math.min((f7 - f6) / 2.0f, f5 / 2.0f));
        LightningSkin.rr(guiGraphicsExtractor, f, f6, f3, f7, f8, true, true, true, true, HeaderFx.c(112, 92, 235, 175), HeaderFx.c(62, 52, 150, 160));
    }

    public static void hover(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        if (color.getAlpha() == 0) {
            return;
        }
        float f5 = f2 + 0.75f;
        float f6 = f4 - 0.75f;
        float f7 = Math.min(4.0f, (f6 - f5) / 2.0f);
        LightningSkin.rr(guiGraphicsExtractor, f, f5, f3, f6, f7, true, true, true, true, color, color);
    }

    public static void nop(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
    }

    private static int[] counts(String string) {
        try {
            for (Module.Category category : Module.Category.values()) {
                if (!category.getName().equalsIgnoreCase(string) && !category.name().equalsIgnoreCase(string)) continue;
                int n = 0;
                int n2 = 0;
                for (Module module : Night.MODULE_MANAGER.getModules(category)) {
                    ++n2;
                    if (!module.isToggled()) continue;
                    ++n;
                }
                return new int[]{n, n2};
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    public static void header(FontManager fontManager, GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
        ColorGui.header(fontManager, guiGraphicsExtractor, string, n, n2, color);
    }

    public static DescriptionFrame desc(ClickGuiScreen clickGuiScreen) {
        current = clickGuiScreen;
        return clickGuiScreen.getDescriptionFrame();
    }

    public static void descRender(DescriptionFrame descriptionFrame, GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        descriptionFrame.render(guiGraphicsExtractor, n, n2, f);
    }

    public static Matrix3x2fStack poseHook(GuiGraphicsExtractor guiGraphicsExtractor) {
        try {
            ClickGuiScreen clickGuiScreen = current;
            if (clickGuiScreen != null && haveBg && !clickGuiScreen.isClosing()) {
                float f = clickGuiScreen.getAppliedScale();
                if (f <= 0.0f) {
                    f = 1.0f;
                }
                float f2 = bgX1 * f;
                float f3 = bgY1 * f;
                float f4 = bgX2 * f;
                float f5 = bgY2 * f;
                float f6 = 9.0f * f;
                LightningSkin.rr(guiGraphicsExtractor, f2, f3, f4, f5, f6, true, true, true, true, new Color(16, 16, 16, 0), new Color(8, 8, 8, 0));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return guiGraphicsExtractor.pose();
    }

    public static void applyScale(ClickGuiScreen clickGuiScreen, float f) {
        clickGuiScreen.setAppliedScale(f);
        try {
            DescriptionFrame descriptionFrame;
            int n;
            float f2 = f <= 0.0f ? 1.0f : f;
            Window window = Minecraft.getInstance().getWindow();
            float f3 = (float)window.getGuiScaledWidth() / f2;
            float f4 = (float)window.getGuiScaledHeight() / f2;
            ArrayList<Frame> arrayList = clickGuiScreen.getFrames();
            int n2 = arrayList.size();
            if (n2 == 0) {
                return;
            }
            float f5 = f4 - 22.0f - 46.0f;
            for (Frame frame : arrayList) {
                frame.setMaxViewHeight(Math.max(40, Math.round(f5 - (float)frame.getHeight() - 4.0f)));
            }
            HeaderFx.fitWidths(arrayList, f3, 10);
            int n3 = (n2 - 1) * 10;
            for (Frame n5 : arrayList) {
                n3 += n5.getWidth();
            }
            int n4 = Math.round((f3 - (float)n3) / 2.0f);
            int n5 = 0;
            for (Frame throwable : arrayList) {
                n = Math.min(throwable.getButtons().size() * 14, throwable.getMaxViewHeight());
                n5 = Math.max(n5, throwable.getHeight() + n + 4);
            }
            HeaderFx.init();
            float f6 = 24.0f;
            try {
                if (titleFont != null) {
                    f6 = titleFont.getHeight();
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            int n6 = 22 + Math.round(Math.max(0.0f, (f5 - (float)n5) / 2.0f));
            n6 = Math.max(n6, Math.round(f6 + 22.0f));
            n = n4;
            for (Frame fr : arrayList) {
                fr.setX(n);
                fr.setY(n6);
                n += fr.getWidth() + 10;
            }
            float f7 = n - 10;
            current = clickGuiScreen;
            titleCx = ((float)n4 + f7) / 2.0f;
            titleY = (float)n6 - f6 - 9.0f;
            bgX1 = (float)n4 - 12.0f;
            bgX2 = f7 + 12.0f;
            bgY1 = titleY - 7.0f;
            bgY2 = (float)(n6 + n5) + 9.0f;
            haveBg = true;
            PingBypassFrame frame2 = clickGuiScreen.getPingBypassFrame();
            if (frame2 != null) {
                frame2.setX(n);
                frame2.setY(n6);
            }
            if ((descriptionFrame = clickGuiScreen.getDescriptionFrame()) != null) {
                descriptionFrame.setX(Math.round((f3 - (float)descriptionFrame.getWidth()) / 2.0f));
                descriptionFrame.setY(Math.round(f4 - 34.0f));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

