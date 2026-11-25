/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.animations;

public class Smoother {
    private double smoothedValue;

    public double smooth(double target, double speed, double deltaTime) {
        double factor = 1.0 - Math.exp(-speed * deltaTime);
        this.smoothedValue += (target - this.smoothedValue) * factor;
        return this.smoothedValue;
    }

    public void clear() {
        this.smoothedValue = 0.0;
    }

    public double getSmoothedValue() {
        return this.smoothedValue;
    }
}

