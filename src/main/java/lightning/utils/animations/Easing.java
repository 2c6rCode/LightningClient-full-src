/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 */
package lightning.utils.animations;

import net.minecraft.util.Mth;

public class Easing {
    public static float ease(float progress, Method method) {
        return switch (method.ordinal()) {
            case 1 -> Easing.easeInSine(progress);
            case 2 -> Easing.easeOutSine(progress);
            case 3 -> Easing.easeInCubic(progress);
            case 4 -> Easing.easeOutCubic(progress);
            case 5 -> Easing.easeInQuad(progress);
            case 6 -> Easing.easeOutQuad(progress);
            case 7 -> Easing.easeInQuart(progress);
            case 8 -> Easing.easeOutQuart(progress);
            case 9 -> Easing.easeInOutSine(progress);
            case 10 -> Easing.easeInOutQuad(progress);
            case 11 -> Easing.easeInOutCubic(progress);
            case 12 -> Easing.easeInOutQuart(progress);
            case 13 -> Easing.easeInElastic(progress);
            case 14 -> Easing.easeOutElastic(progress);
            case 15 -> Easing.easeInOutElastic(progress);
            default -> Easing.linear(progress);
        };
    }

    public static float toDelta(long startTime, int duration) {
        return Mth.clamp((float)((float)Easing.toDelta(startTime) / (float)duration), (float)0.0f, (float)1.0f);
    }

    public static long toDelta(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    private static float linear(float progress) {
        return progress;
    }

    private static float easeInSine(float progress) {
        return (float)(1.0 - Math.cos((double)progress * Math.PI / 2.0));
    }

    private static float easeOutSine(float progress) {
        return (float)Math.sin((double)progress * Math.PI / 2.0);
    }

    private static float easeInCubic(float progress) {
        return progress * progress * progress;
    }

    private static float easeOutCubic(float progress) {
        return (float)(1.0 - Math.pow(1.0f - progress, 3.0));
    }

    private static float easeInQuad(float progress) {
        return progress * progress;
    }

    private static float easeOutQuad(float progress) {
        return 1.0f - (1.0f - progress) * (1.0f - progress);
    }

    private static float easeInQuart(float progress) {
        return progress * progress * progress * progress;
    }

    private static float easeOutQuart(float progress) {
        return (float)(1.0 - Math.pow(1.0f - progress, 4.0));
    }

    private static float easeInOutSine(float progress) {
        return (float)(-(Math.cos(Math.PI * (double)progress) - 1.0) / 2.0);
    }

    private static float easeInOutQuad(float progress) {
        return progress < 0.5f ? 2.0f * progress * progress : (float)(1.0 - Math.pow(-2.0f * progress + 2.0f, 2.0) / 2.0);
    }

    private static float easeInOutCubic(float progress) {
        return progress < 0.5f ? 4.0f * progress * progress * progress : (float)(1.0 - Math.pow(-2.0f * progress + 2.0f, 3.0) / 2.0);
    }

    private static float easeInOutQuart(float progress) {
        return progress < 0.5f ? 8.0f * progress * progress * progress * progress : (float)(1.0 - Math.pow(-2.0f * progress + 2.0f, 4.0) / 2.0);
    }

    private static float easeInElastic(float progress) {
        float period = 2.0943952f;
        return progress == 0.0f ? 0.0f : (float)(progress == 1.0f ? 1.0 : -Math.pow(2.0, 10.0f * progress - 10.0f) * Math.sin(((double)(progress * 10.0f) - 10.75) * (double)period));
    }

    private static float easeOutElastic(float progress) {
        float period = 2.0943952f;
        return progress == 0.0f ? 0.0f : (float)(progress == 1.0f ? 1.0 : Math.pow(2.0, -10.0f * progress) * Math.sin(((double)(progress * 10.0f) - 0.75) * (double)period) + 1.0);
    }

    private static float easeInOutElastic(float progress) {
        float period = 1.3962635f;
        return progress == 0.0f ? 0.0f : (float)(progress == 1.0f ? 1.0 : ((double)progress < 0.5 ? -(Math.pow(2.0, 20.0f * progress - 10.0f) * Math.sin(((double)(20.0f * progress) - 11.125) * (double)period)) / 2.0 : Math.pow(2.0, -20.0f * progress + 10.0f) * Math.sin(((double)(20.0f * progress) - 11.125) * (double)period) / 2.0 + 1.0));
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

