/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.system;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadExecutor {
    private static final Map<Class<?>, Thread> RUNNING = new ConcurrentHashMap();

    public static void execute(Runnable runnable) {
        Thread thread;
        Class<?> clazz = runnable.getClass().getEnclosingClass();
        if (clazz == null) {
            clazz = runnable.getClass();
        }
        if ((thread = RUNNING.get(clazz)) != null && thread.isAlive()) {
            return;
        }
        Class<?> clazz2 = clazz;
        OneTimeThread oneTimeThread = new OneTimeThread(() -> {
            try {
                runnable.run();
            }
            finally {
                RUNNING.remove(clazz2);
            }
        });
        RUNNING.put(clazz2, oneTimeThread);
        oneTimeThread.start();
    }

    private static class OneTimeThread
    extends Thread {
        private final Runnable runnable;

        public OneTimeThread(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            this.runnable.run();
        }
    }
}

