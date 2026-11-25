/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.system;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Counter {
    private final ConcurrentLinkedQueue<Long> count = new ConcurrentLinkedQueue();

    public void increment() {
        this.count.add(System.currentTimeMillis() + 1000L);
    }

    public int getCount() {
        this.count.removeIf(expiry -> expiry < System.currentTimeMillis());
        return this.count.size();
    }

    public void reset() {
        this.count.clear();
    }
}

