/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.graphics;

public final class CrystalPlacementMath {
    private CrystalPlacementMath() {
    }

    public static double progress(long l, int n) {
        return Math.max(0.0, Math.min(1.0, (double)l / ((double)Math.max(1, n) * 1000000.0)));
    }

    public static double ease(double d) {
        d = Math.max(0.0, Math.min(1.0, d));
        return 1.0 - Math.pow(1.0 - d, 3.0);
    }

    public static double opacity(double d) {
        d = Math.max(0.0, Math.min(1.0, d));
        double d2 = Math.min(1.0, d / 0.12);
        d2 = d2 * d2 * (3.0 - 2.0 * d2);
        return d2 * (1.0 - d) * (1.0 - d);
    }

    public static double radius(double d, double d2, double d3) {
        return Math.max(0.01, d * (0.72 + 0.28 * CrystalPlacementMath.ease(d3)) + d2 * CrystalPlacementMath.ease(d3));
    }

    public static double petal(double d) {
        return 1.0 + 0.12 * Math.cos(d * 6.0);
    }
}

