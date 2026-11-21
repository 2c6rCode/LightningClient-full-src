/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EarlyTickHooks {
    private static final List<Runnable> callbacks = new CopyOnWriteArrayList<Runnable>();

    private EarlyTickHooks() {
    }

    public static void register(Runnable runnable) {
        callbacks.add(runnable);
    }

    public static void unregister(Runnable runnable) {
        callbacks.remove(runnable);
    }

    public static void dispatch() {
        for (Runnable runnable : callbacks) {
            runnable.run();
        }
    }
}

