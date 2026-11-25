/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.animations;

public class AnimateUtil {
    public static double animate(double current, double target, double speed) {
        if (speed >= 1.0) {
            return target;
        }
        if (speed <= 0.0) {
            return current;
        }
        boolean increasing = target > current;
        double diff = Math.abs(target - current);
        if (diff <= 0.001) {
            return target;
        }
        double step = diff * speed;
        return current + (increasing ? step : -step);
    }

    public static float animate(float current, float target, float speed) {
        if (speed >= 1.0f) {
            return target;
        }
        if (speed <= 0.0f) {
            return current;
        }
        boolean increasing = target > current;
        float diff = Math.abs(target - current);
        if (diff <= 0.001f) {
            return target;
        }
        float step = diff * speed;
        return current + (increasing ? step : -step);
    }
}

