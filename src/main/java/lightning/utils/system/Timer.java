/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.system;

public class Timer {
    private long startTime = System.currentTimeMillis();

    public boolean hasTimeElapsed(Number number) {
        return System.currentTimeMillis() - this.startTime >= number.longValue();
    }

    public long timeElapsed() {
        return System.currentTimeMillis() - this.startTime;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }
}

