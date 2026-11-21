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

    public static void register(String string, BooleanSetting booleanSetting, PositionSetting positionSetting) {
        HudElementRegistry.register(string, booleanSetting, positionSetting, null);
    }

    public static void register(String string, BooleanSetting booleanSetting, PositionSetting positionSetting, CategorySetting categorySetting) {
        ELEMENTS.put(string, new Element(string, booleanSetting, positionSetting, categorySetting));
    }

    public static void reportBounds(String string, float f, float f2, float f3, float f4) {
        RAW_BOUNDS.put(string, new float[]{f, f2, f3, f4});
    }

    public static Map<String, Element> getElements() {
        return ELEMENTS;
    }

    public static float[] getBounds(String string) {
        float[] fArray = RAW_BOUNDS.get(string);
        Element element = ELEMENTS.get(string);
        if (fArray == null || element == null) {
            return fArray;
        }
        float f = element.offset().getX();
        float f2 = element.offset().getY();
        return new float[]{fArray[0] + f, fArray[1] + f2, fArray[2] + f, fArray[3] + f2};
    }

    public static void clamp(String string, int n, int n2) {
        Element element = ELEMENTS.get(string);
        float[] fArray = RAW_BOUNDS.get(string);
        if (element == null || fArray == null) {
            return;
        }
        float f = fArray[2] - fArray[0];
        float f2 = fArray[3] - fArray[1];
        float f3 = -fArray[0];
        float f4 = Math.max(f3, (float)n - f - fArray[0]);
        float f5 = -fArray[1];
        float f6 = Math.max(f5, (float)n2 - f2 - fArray[1]);
        element.offset().set(Math.clamp(element.offset().getX(), f3, f4), Math.clamp(element.offset().getY(), f5, f6));
    }

    public record Element(String name, BooleanSetting enabled, PositionSetting offset, CategorySetting category) {
    }
}

