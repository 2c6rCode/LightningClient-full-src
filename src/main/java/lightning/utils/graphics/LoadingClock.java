/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.graphics;

public final class LoadingClock {
    private static final long STALL_MS = 150L;
    private static final long NEW_SESSION_GAP_MS = 3000L;
    private static final long HOLD_SMOOTH_MS = 800L;
    private static final long GIVE_UP_MS = 30000L;
    private static long last = -1L;
    private static long acc = 0L;
    private static long smooth = 0L;
    private static long sessionStart = -1L;

    private LoadingClock() {
    }

    public static synchronized long tick(long l) {
        if (last < 0L || l - last > 3000L) {
            acc = 0L;
            smooth = 0L;
            last = l;
            sessionStart = l;
            return 0L;
        }
        long l2 = l - last;
        if (l2 < 0L) {
            l2 = 0L;
        }
        last = l;
        if (l2 > 150L) {
            smooth = 0L;
        } else {
            acc += l2;
            smooth += l2;
        }
        return acc;
    }

    public static synchronized long elapsed() {
        if (sessionStart >= 0L && last - sessionStart > 30000L) {
            return 0x1FFFFFFFFFFFFFFFL;
        }
        return smooth >= 800L ? acc : 0L;
    }
}

