/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.animations;

public class AnimateUtil {
    public static double animate(double d, double d2, double d3) {
        if (d3 >= 1.0) {
            return d2;
        }
        if (d3 <= 0.0) {
            return d;
        }
        boolean bl = d2 > d;
        double d4 = Math.abs(d2 - d);
        if (d4 <= 0.001) {
            return d2;
        }
        double d5 = d4 * d3;
        return d + (bl ? d5 : -d5);
    }

    public static float animate(float f, float f2, float f3) {
        if (f3 >= 1.0f) {
            return f2;
        }
        if (f3 <= 0.0f) {
            return f;
        }
        boolean bl = f2 > f;
        float f4 = Math.abs(f2 - f);
        if (f4 <= 0.001f) {
            return f2;
        }
        float f5 = f4 * f3;
        return f + (bl ? f5 : -f5);
    }
}

