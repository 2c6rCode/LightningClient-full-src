/*
 * Decompiled with CFR 0.152.
 */
package lightning.managers;

import java.util.LinkedHashMap;
import java.util.Map;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.PositionSetting;

public class HudElementRegistry {
    private static final Map<String, Element> ELEMENTS = new LinkedHashMap<String, Element>();
    private static final Map<String, float[]> RAW_BOUNDS = new LinkedHashMap<String, float[]>();

    public static void register(String name, BooleanSetting booleanSetting, PositionSetting positionSetting) {
        HudElementRegistry.register(name, booleanSetting, positionSetting, null);
    }

    public static void register(String name, BooleanSetting booleanSetting, PositionSetting positionSetting, CategorySetting categorySetting) {
        ELEMENTS.put(name, new Element(name, booleanSetting, positionSetting, categorySetting));
    }

    public static void reportBounds(String name, float minX, float minY, float maxX, float maxY) {
        RAW_BOUNDS.put(name, new float[]{minX, minY, maxX, maxY});
    }

    public static Map<String, Element> getElements() {
        return ELEMENTS;
    }

    public static float[] getBounds(String name) {
        float[] rawBounds = RAW_BOUNDS.get(name);
        Element element = ELEMENTS.get(name);
        if (rawBounds == null || element == null) {
            return rawBounds;
        }
        float offsetX = element.offset().getX();
        float offsetY = element.offset().getY();
        return new float[]{rawBounds[0] + offsetX, rawBounds[1] + offsetY, rawBounds[2] + offsetX, rawBounds[3] + offsetY};
    }

    public static void clamp(String name, int screenWidth, int screenHeight) {
        Element element = ELEMENTS.get(name);
        float[] rawBounds = RAW_BOUNDS.get(name);
        if (element == null || rawBounds == null) {
            return;
        }
        float width = rawBounds[2] - rawBounds[0];
        float height = rawBounds[3] - rawBounds[1];
        float minOffsetX = -rawBounds[0];
        float maxOffsetX = Math.max(minOffsetX, (float)screenWidth - width - rawBounds[0]);
        float minOffsetY = -rawBounds[1];
        float maxOffsetY = Math.max(minOffsetY, (float)screenHeight - height - rawBounds[1]);
        element.offset().set(Math.clamp(element.offset().getX(), minOffsetX, maxOffsetX), Math.clamp(element.offset().getY(), minOffsetY, maxOffsetY));
    }

    public record Element(String name, BooleanSetting enabled, PositionSetting offset, CategorySetting category) {
    }
}

