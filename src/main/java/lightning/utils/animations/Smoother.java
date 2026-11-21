/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.animations;

public class Smoother {
    private double smoothedValue;

    public double smooth(double d, double d2, double d3) {
        double d4 = 1.0 - Math.exp(-d2 * d3);
        this.smoothedValue += (d - this.smoothedValue) * d4;
        return this.smoothedValue;
    }

    public void clear() {
        this.smoothedValue = 0.0;
    }

    public double getSmoothedValue() {
        return this.smoothedValue;
    }
}

