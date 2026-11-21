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

    public void record(long l, long l2, int n, int n2) {
        if (!this.hasKey || this.key != l || l2 - this.last > (long)n2 * 1000000L) {
            this.clear();
        }
        this.key = l;
        this.hasKey = true;
        this.last = l2;
        this.count = Math.min(10000, this.count + 1);
        if (this.count >= Math.max(1, n) && this.appeared == 0L) {
            this.appeared = l2;
        }
    }

    public int count() {
        return this.count;
    }

    public void expire(long l, int n) {
        if (this.hasKey && l - this.last > (long)n * 1000000L) {
            this.clear();
        }
    }

    public long appearedAt() {
        return this.appeared;
    }

    public boolean visible(long l, int n, long l2, int n2) {
        return this.hasKey && this.appeared != 0L && l2 != 0L && l >= this.last && l >= l2 && l - this.last <= (long)n * 1000000L && l - l2 <= (long)n2 * 1000000L;
    }
}

