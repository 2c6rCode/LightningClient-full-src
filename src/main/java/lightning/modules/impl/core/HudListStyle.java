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
        int bind = module.getBind();
        if (bind == 0) {
            return "";
        }
        String keyName = KeyboardUtils.getKeyName(bind);
        return keyName == null ? "" : keyName;
    }

    private static String metaOf(HUDModule hudModule, Module module) {
        String metaData = module.getMetaData();
        return metaData == null || metaData.isEmpty() || !hudModule.metaData.getValue() ? "" : metaData;
    }

    public static String moduleText(HUDModule hudModule, Module module) {
        if (!HudListStyle.newbie(hudModule)) {
            try {
                if (original == null) {
                    original = HUDModule.class.getDeclaredMethod("getModuleText", Module.class);
                    original.setAccessible(true);
                }
                return (String)original.invoke((Object)hudModule, module);
            }
            catch (Throwable throwable) {
                return module.getName();
            }
        }
        String bindText = HudListStyle.bindOf(module);
        String metaText = HudListStyle.metaOf(hudModule, module);
        StringBuilder stringBuilder = new StringBuilder();
        if (!bindText.isEmpty()) {
            stringBuilder.append(bindText).append(' ');
        }
        stringBuilder.append(module.getName());
        if (!metaText.isEmpty()) {
            stringBuilder.append(' ').append(metaText);
        }
        return stringBuilder.toString();
    }

    private static Color lerp(Color fromColor, Color toColor, float progress, int alpha) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        return new Color((int)((float)fromColor.getRed() + (float)(toColor.getRed() - fromColor.getRed()) * progress), (int)((float)fromColor.getGreen() + (float)(toColor.getGreen() - fromColor.getGreen()) * progress), (int)((float)fromColor.getBlue() + (float)(toColor.getBlue() - fromColor.getBlue()) * progress), alpha);
    }

    public static void drawNewbie(HUDModule hudModule, Module module, GuiGraphicsExtractor graphics, float x, float y, float progress, Color color, boolean unused) {
        String bindText = HudListStyle.bindOf(module);
        String metaText = HudListStyle.metaOf(hudModule, module);
        String moduleName = module.getName();
        if (moduleName == null || moduleName.isEmpty()) {
            return;
        }
        FontManager fontManager = Night.FONT_MANAGER;
        int fontHeight = fontManager.getHeight();
        String fullText = HudListStyle.moduleText(hudModule, module);
        float totalWidth = fontManager.getWidth(fullText);
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, progress));
        Color resolvedColor = color == null ? Color.WHITE : color;
        Matrix3x2fStack matrixStack = graphics.pose();
        matrixStack.pushMatrix();
        matrixStack.translate(x, y);
        int drawX = 0;
        if (!bindText.isEmpty()) {
            fontManager.drawTextWithShadow(graphics, bindText, drawX, 0, HudListStyle.lerp(BIND_OFF, BIND_ON, clampedProgress, 255));
            drawX += fontManager.getWidth(bindText + " ");
        }
        fontManager.drawTextWithShadow(graphics, moduleName, drawX, 0, HudListStyle.lerp(NAME_OFF, resolvedColor, clampedProgress, 255));
        if (!metaText.isEmpty()) {
            fontManager.drawTextWithShadow(graphics, metaText, drawX += fontManager.getWidth(moduleName + " "), 0, META);
        }
        matrixStack.popMatrix();
    }

    static {
        BIND_OFF = new Color(88, 88, 98);
        BIND_ON = new Color(150, 150, 165);
        NAME_OFF = new Color(125, 125, 138);
        META = new Color(150, 150, 162);
    }
}

