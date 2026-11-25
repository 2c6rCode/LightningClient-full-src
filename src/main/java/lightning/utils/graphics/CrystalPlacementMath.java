/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.graphics;

public final class CrystalPlacementMath {
    private CrystalPlacementMath() {
    }

    public static double progress(long elapsedNanos, int durationMs) {
        return Math.max(0.0, Math.min(1.0, (double)elapsedNanos / ((double)Math.max(1, durationMs) * 1000000.0)));
    }

    public static double ease(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return 1.0 - Math.pow(1.0 - t, 3.0);
    }

    public static double opacity(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        double fadeIn = Math.min(1.0, t / 0.12);
        fadeIn = fadeIn * fadeIn * (3.0 - 2.0 * fadeIn);
        return fadeIn * (1.0 - t) * (1.0 - t);
    }

    public static double radius(double baseRadius, double expansion, double t) {
        return Math.max(0.01, baseRadius * (0.72 + 0.28 * CrystalPlacementMath.ease(t)) + expansion * CrystalPlacementMath.ease(t));
    }

    public static double petal(double angle) {
        return 1.0 + 0.12 * Math.cos(angle * 6.0);
    }
}

