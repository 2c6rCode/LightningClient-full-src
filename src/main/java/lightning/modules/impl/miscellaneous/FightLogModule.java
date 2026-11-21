/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package lightning.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TargetDeathEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@RegisterModule(name="FightLog", description="Records your fights: kills, totems lost and who you fought. Shown in the ClickGui player card.", category=Module.Category.MISCELLANEOUS, toggled=true, drawn=false)
public class FightLogModule
extends Module {
    private static final int MAX_HISTORY = 30;
    private static final long OPPONENT_WINDOW_MS = 25000L;
    private static final List<Entry> HISTORY = new ArrayList<Entry>();
    private static int kills = 0;
    private static int survived = 0;
    private static int totemsLost = 0;
    private static int baseline = 0;
    private static boolean hasLast = false;
    private static boolean lastWin = false;
    private static String opponent = null;
    private static long opponentTime = 0L;
    private static String lastKillName = null;
    private static long lastKillTime = 0L;
    private static boolean subscribed = false;
    private static final Listener LISTENER = new Listener();

    public FightLogModule() {
        if (!subscribed) {
            subscribed = true;
            try {
                Night.EVENT_HANDLER.subscribe(LISTENER);
            }
            catch (Throwable t) {
                subscribed = false;
            }
        }
    }

    public static int getKills() {
        return kills;
    }

    public static int getSurvived() {
        return survived;
    }

    public static int getTotemsLost() {
        return totemsLost;
    }

    public static boolean hasLast() {
        return hasLast;
    }

    public static boolean isLastWin() {
        return lastWin;
    }

    public static List<Entry> getHistory() {
        return new ArrayList<Entry>(HISTORY);
    }

    public static void reset() {
        HISTORY.clear();
        kills = 0;
        survived = 0;
        totemsLost = 0;
        baseline = 0;
        hasLast = false;
        lastWin = false;
        opponent = null;
    }

    private static void record(boolean win, String name) {
        long now = System.currentTimeMillis();
        if (win) {
            if (name != null && name.equals(lastKillName) && now - lastKillTime < 5000L) {
                return;
            }
            lastKillName = name;
            lastKillTime = now;
        }
        int lost = Math.max(0, totemsLost - baseline);
        baseline = totemsLost;
        HISTORY.add(0, new Entry(name, win, lost));
        while (HISTORY.size() > 30) {
            HISTORY.remove(HISTORY.size() - 1);
        }
        if (win) {
            ++kills;
            ++survived;
        }
        hasLast = true;
        lastWin = win;
        opponent = null;
    }

    private static Object call(Object o, String m) {
        try {
            if (o == null) {
                return null;
            }
            return o.getClass().getMethod(m, new Class[0]).invoke(o, new Object[0]);
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static boolean isPlayer(Object o) {
        Class<?> c;
        Class<?> clazz = c = o == null ? null : o.getClass();
        while (c != null) {
            if (c.getName().equals("net.minecraft.world.entity.player.Player")) {
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }

    private static String nameOf(Object o) {
        Object comp = FightLogModule.call(o, "getName");
        Object s = FightLogModule.call(comp, "getString");
        return s == null ? null : String.valueOf(s);
    }

    private static Object selfObj() {
        try {
            Minecraft mc = Minecraft.getInstance();
            return mc.getClass().getField("player").get(mc);
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static boolean isSelf(Object o) {
        if (o == null) {
            return false;
        }
        Object me = FightLogModule.selfObj();
        if (me == null) {
            return false;
        }
        if (o == me) {
            return true;
        }
        Object a = FightLogModule.call(o, "getUUID");
        Object b = FightLogModule.call(me, "getUUID");
        if (a != null && a.equals(b)) {
            return true;
        }
        String n1 = FightLogModule.nameOf(o);
        String n2 = FightLogModule.nameOf(me);
        return n1 != null && n1.equals(n2);
    }

    private static String selfName() {
        String string = FightLogModule.nameOf(FightLogModule.selfObj());
        if (string != null && !string.isEmpty()) {
            return string;
        }
        return "?";
    }

    private static boolean isMe(Object object) {
        if (object == null) {
            return false;
        }
        Object object2 = FightLogModule.selfObj();
        if (object2 == null) {
            return false;
        }
        if (object == object2) {
            return true;
        }
        Object object3 = FightLogModule.call(object, "getUUID");
        Object object4 = FightLogModule.call(object2, "getUUID");
        if (object3 != null) {
            return object3.equals(object4);
        }
        return false;
    }

    public static final class Listener {
        @SubscribeEvent
        public void onAttack(AttackEntityEvent event) {
            Entity target = event.getTarget();
            if (!FightLogModule.isPlayer(target) || FightLogModule.isSelf(target)) {
                return;
            }
            String n = FightLogModule.nameOf(target);
            if (n == null || n.isEmpty()) {
                return;
            }
            opponent = n;
            opponentTime = System.currentTimeMillis();
        }

        @SubscribeEvent
        public void onPlayerPop(PlayerPopEvent event) {
            Player p = event.getPlayer();
            if (FightLogModule.isMe(p)) {
                ++totemsLost;
            }
        }

        @SubscribeEvent
        public void onPlayerDeath(PlayerDeathEvent event) {
            boolean recent;
            Player p = event.getPlayer();
            long now = System.currentTimeMillis();
            boolean bl = recent = opponent != null && now - opponentTime <= 25000L;
            if (FightLogModule.isSelf(p)) {
                FightLogModule.record(false, recent ? opponent : FightLogModule.selfName());
                return;
            }
            String n = FightLogModule.nameOf(p);
            if (recent && n != null && n.equals(opponent)) {
                FightLogModule.record(true, n);
            }
        }

        @SubscribeEvent
        public void onTargetDeath(TargetDeathEvent event) {
            Player p = event.getPlayer();
            if (FightLogModule.isSelf(p)) {
                return;
            }
            String n = FightLogModule.nameOf(p);
            if (n != null && !n.isEmpty()) {
                FightLogModule.record(true, n);
            }
        }

        @SubscribeEvent
        public void onClientConnect(ClientConnectEvent event) {
            opponent = null;
            baseline = totemsLost;
        }
    }

    public static final class Entry {
        public final String name;
        public final boolean win;
        public final int totems;

        public Entry(String name, boolean win, int totems) {
            this.name = name;
            this.win = win;
            this.totems = totems;
        }
    }
}

