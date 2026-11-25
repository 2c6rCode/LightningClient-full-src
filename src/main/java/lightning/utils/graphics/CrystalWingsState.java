/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.graphics;

public final class CrystalWingsState {
    private long key;
    private long last;
    private long appeared;
    private boolean hasKey;
    private int count;

    public void clear() {
        this.hasKey = false;
        this.count = 0;
        this.appeared = 0L;
        this.last = 0L;
    }

    public void record(long entityKey, long nowNanos, int requiredSamples, int timeoutMs) {
        if (!this.hasKey || this.key != entityKey || nowNanos - this.last > (long)timeoutMs * 1000000L) {
            this.clear();
        }
        this.key = entityKey;
        this.hasKey = true;
        this.last = nowNanos;
        this.count = Math.min(10000, this.count + 1);
        if (this.count >= Math.max(1, requiredSamples) && this.appeared == 0L) {
            this.appeared = nowNanos;
        }
    }

    public int count() {
        return this.count;
    }

    public void expire(long nowNanos, int timeoutMs) {
        if (this.hasKey && nowNanos - this.last > (long)timeoutMs * 1000000L) {
            this.clear();
        }
    }

    public long appearedAt() {
        return this.appeared;
    }

    public boolean visible(long nowNanos, int staleMs, long appearedNanos, int maxAgeMs) {
        return this.hasKey && this.appeared != 0L && appearedNanos != 0L && nowNanos >= this.last && nowNanos >= appearedNanos && nowNanos - this.last <= (long)staleMs * 1000000L && nowNanos - appearedNanos <= (long)maxAgeMs * 1000000L;
    }
}

