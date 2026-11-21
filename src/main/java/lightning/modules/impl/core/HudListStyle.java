/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.joml.Matrix3x2fStack
 */
package lightning.modules.impl.core;

import java.awt.Color;
import java.lang.reflect.Method;
import lightning.Night;
import lightning.managers.FontManager;
import lightning.modules.Module;
import lightning.modules.impl.core.HUDModule;
import lightning.utils.input.KeyboardUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

public final class HudListStyle {
    private static Method original;
    private static final Color BIND_OFF;
    private static final Color BIND_ON;
    private static final Color NAME_OFF;
    private static final Color META;

    private HudListStyle() {
    }

    private static boolean newbie(HUDModule hUDModule) {
        try {
            return "Newbie".equalsIgnoreCase(hUDModule.moduleListMode.getValue());
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private static String bindOf(Module module) {
        int n = module.getBind();
        if (n == 0) {
            return "";
        }
        String string = KeyboardUtils.getKeyName(n);
        return string == null ? "" : string;
    }

    private static String metaOf(HUDModule hUDModule, Module module) {
        String string = module.getMetaData();
        return string == null || string.isEmpty() || !hUDModule.metaData.getValue() ? "" : string;
    }

    public static String moduleText(HUDModule hUDModule, Module module) {
        if (!HudListStyle.newbie(hUDModule)) {
            try {
                if (original == null) {
                    original = HUDModule.class.getDeclaredMethod("getModuleText", Module.class);
                    original.setAccessible(true);
                }
                return (String)original.invoke((Object)hUDModule, module);
            }
            catch (Throwable throwable) {
                return module.getName();
            }
        }
        String string = HudListStyle.bindOf(module);
        String string2 = HudListStyle.metaOf(hUDModule, module);
        StringBuilder stringBuilder = new StringBuilder();
        if (!string.isEmpty()) {
            stringBuilder.append(string).append(' ');
        }
        stringBuilder.append(module.getName());
        if (!string2.isEmpty()) {
            stringBuilder.append(' ').append(string2);
        }
        return stringBuilder.toString();
    }

    private static Color lerp(Color color, Color color2, float f, int n) {
        f = Math.max(0.0f, Math.min(1.0f, f));
        return new Color((int)((float)color.getRed() + (float)(color2.getRed() - color.getRed()) * f), (int)((float)color.getGreen() + (float)(color2.getGreen() - color.getGreen()) * f), (int)((float)color.getBlue() + (float)(color2.getBlue() - color.getBlue()) * f), n);
    }

    public static void drawNewbie(HUDModule hUDModule, Module module, GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, Color color, boolean bl) {
        String string = HudListStyle.bindOf(module);
        String string2 = HudListStyle.metaOf(hUDModule, module);
        String string3 = module.getName();
        if (string3 == null || string3.isEmpty()) {
            return;
        }
        FontManager fontManager = Night.FONT_MANAGER;
        int n = fontManager.getHeight();
        String string4 = HudListStyle.moduleText(hUDModule, module);
        float f4 = fontManager.getWidth(string4);
        float f5 = Math.max(0.0f, Math.min(1.0f, f3));
        Color color2 = color == null ? Color.WHITE : color;
        Matrix3x2fStack matrix3x2fStack = guiGraphicsExtractor.pose();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(f, f2);
        int n2 = 0;
        if (!string.isEmpty()) {
            fontManager.drawTextWithShadow(guiGraphicsExtractor, string, n2, 0, HudListStyle.lerp(BIND_OFF, BIND_ON, f5, 255));
            n2 += fontManager.getWidth(string + " ");
        }
        fontManager.drawTextWithShadow(guiGraphicsExtractor, string3, n2, 0, HudListStyle.lerp(NAME_OFF, color2, f5, 255));
        if (!string2.isEmpty()) {
            fontManager.drawTextWithShadow(guiGraphicsExtractor, string2, n2 += fontManager.getWidth(string3 + " "), 0, META);
        }
        matrix3x2fStack.popMatrix();
    }

    static {
        BIND_OFF = new Color(88, 88, 98);
        BIND_ON = new Color(150, 150, 165);
        NAME_OFF = new Color(125, 125, 138);
        META = new Color(150, 150, 162);
    }
}

