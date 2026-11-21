/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package lightning.utils.animations;

import net.minecraft.util.Mth;

public class Easing {
    public static float ease(float f, Method method) {
        return switch (method.ordinal()) {
            case 1 -> Easing.easeInSine(f);
            case 2 -> Easing.easeOutSine(f);
            case 3 -> Easing.easeInCubic(f);
            case 4 -> Easing.easeOutCubic(f);
            case 5 -> Easing.easeInQuad(f);
            case 6 -> Easing.easeOutQuad(f);
            case 7 -> Easing.easeInQuart(f);
            case 8 -> Easing.easeOutQuart(f);
            case 9 -> Easing.easeInOutSine(f);
            case 10 -> Easing.easeInOutQuad(f);
            case 11 -> Easing.easeInOutCubic(f);
            case 12 -> Easing.easeInOutQuart(f);
            case 13 -> Easing.easeInElastic(f);
            case 14 -> Easing.easeOutElastic(f);
            case 15 -> Easing.easeInOutElastic(f);
            default -> Easing.linear(f);
        };
    }

    public static float toDelta(long l, int n) {
        return Mth.clamp((float)((float)Easing.toDelta(l) / (float)n), (float)0.0f, (float)1.0f);
    }

    public static long toDelta(long l) {
        return System.currentTimeMillis() - l;
    }

    private static float linear(float f) {
        return f;
    }

    private static float easeInSine(float f) {
        return (float)(1.0 - Math.cos((double)f * Math.PI / 2.0));
    }

    private static float easeOutSine(float f) {
        return (float)Math.sin((double)f * Math.PI / 2.0);
    }

    private static float easeInCubic(float f) {
        return f * f * f;
    }

    private static float easeOutCubic(float f) {
        return (float)(1.0 - Math.pow(1.0f - f, 3.0));
    }

    private static float easeInQuad(float f) {
        return f * f;
    }

    private static float easeOutQuad(float f) {
        return 1.0f - (1.0f - f) * (1.0f - f);
    }

    private static float easeInQuart(float f) {
        return f * f * f * f;
    }

    private static float easeOutQuart(float f) {
        return (float)(1.0 - Math.pow(1.0f - f, 4.0));
    }

    private static float easeInOutSine(float f) {
        return (float)(-(Math.cos(Math.PI * (double)f) - 1.0) / 2.0);
    }

    private static float easeInOutQuad(float f) {
        return f < 0.5f ? 2.0f * f * f : (float)(1.0 - Math.pow(-2.0f * f + 2.0f, 2.0) / 2.0);
    }

    private static float easeInOutCubic(float f) {
        return f < 0.5f ? 4.0f * f * f * f : (float)(1.0 - Math.pow(-2.0f * f + 2.0f, 3.0) / 2.0);
    }

    private static float easeInOutQuart(float f) {
        return f < 0.5f ? 8.0f * f * f * f * f : (float)(1.0 - Math.pow(-2.0f * f + 2.0f, 4.0) / 2.0);
    }

    private static float easeInElastic(float f) {
        float f2 = 2.0943952f;
        return f == 0.0f ? 0.0f : (float)(f == 1.0f ? 1.0 : -Math.pow(2.0, 10.0f * f - 10.0f) * Math.sin(((double)(f * 10.0f) - 10.75) * (double)f2));
    }

    private static float easeOutElastic(float f) {
        float f2 = 2.0943952f;
        return f == 0.0f ? 0.0f : (float)(f == 1.0f ? 1.0 : Math.pow(2.0, -10.0f * f) * Math.sin(((double)(f * 10.0f) - 0.75) * (double)f2) + 1.0);
    }

    private static float easeInOutElastic(float f) {
        float f2 = 1.3962635f;
        return f == 0.0f ? 0.0f : (float)(f == 1.0f ? 1.0 : ((double)f < 0.5 ? -(Math.pow(2.0, 20.0f * f - 10.0f) * Math.sin(((double)(20.0f * f) - 11.125) * (double)f2)) / 2.0 : Math.pow(2.0, -20.0f * f + 10.0f) * Math.sin(((double)(20.0f * f) - 11.125) * (double)f2) / 2.0 + 1.0));
    }

    public static enum Method {
        LINEAR,
        EASE_IN_SINE,
        EASE_OUT_SINE,
        EASE_IN_CUBIC,
        EASE_OUT_CUBIC,
        EASE_IN_QUAD,
        EASE_OUT_QUAD,
        EASE_IN_QUART,
        EASE_OUT_QUART,
        EASE_IN_OUT_SINE,
        EASE_IN_OUT_QUAD,
        EASE_IN_OUT_CUBIC,
        EASE_IN_OUT_QUART,
        EASE_IN_ELASTIC,
        EASE_OUT_ELASTIC,
        EASE_IN_OUT_ELASTIC;

    }
}

