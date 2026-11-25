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

    private static Object call(Object target, String methodName) {
        try {
            if (target == null) {
                return null;
            }
            return target.getClass().getMethod(methodName, new Class[0]).invoke(target, new Object[0]);
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static boolean isPlayer(Object obj) {
        Class<?> currentClass = obj == null ? null : obj.getClass();
        while (currentClass != null) {
            if (currentClass.getName().equals("net.minecraft.world.entity.player.Player")) {
                return true;
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

    private static String nameOf(Object entity) {
        Object nameComponent = FightLogModule.call(entity, "getName");
        Object nameString = FightLogModule.call(nameComponent, "getString");
        return nameString == null ? null : String.valueOf(nameString);
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

    private static boolean isSelf(Object obj) {
        if (obj == null) {
            return false;
        }
        Object self = FightLogModule.selfObj();
        if (self == null) {
            return false;
        }
        if (obj == self) {
            return true;
        }
        Object objUuid = FightLogModule.call(obj, "getUUID");
        Object selfUuid = FightLogModule.call(self, "getUUID");
        if (objUuid != null && objUuid.equals(selfUuid)) {
            return true;
        }
        String objName = FightLogModule.nameOf(obj);
        String selfName = FightLogModule.nameOf(self);
        return objName != null && objName.equals(selfName);
    }

    private static String selfName() {
        String name = FightLogModule.nameOf(FightLogModule.selfObj());
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return "?";
    }

    private static boolean isMe(Object obj) {
        if (obj == null) {
            return false;
        }
        Object self = FightLogModule.selfObj();
        if (self == null) {
            return false;
        }
        if (obj == self) {
            return true;
        }
        Object objUuid = FightLogModule.call(obj, "getUUID");
        Object selfUuid = FightLogModule.call(self, "getUUID");
        if (objUuid != null) {
            return objUuid.equals(selfUuid);
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
            String targetName = FightLogModule.nameOf(target);
            if (targetName == null || targetName.isEmpty()) {
                return;
            }
            opponent = targetName;
            opponentTime = System.currentTimeMillis();
        }

        @SubscribeEvent
        public void onPlayerPop(PlayerPopEvent event) {
            Player player = event.getPlayer();
            if (FightLogModule.isMe(player)) {
                ++totemsLost;
            }
        }

        @SubscribeEvent
        public void onPlayerDeath(PlayerDeathEvent event) {
            Player player = event.getPlayer();
            long now = System.currentTimeMillis();
            boolean recent = opponent != null && now - opponentTime <= 25000L;
            if (FightLogModule.isSelf(player)) {
                FightLogModule.record(false, recent ? opponent : FightLogModule.selfName());
                return;
            }
            String playerName = FightLogModule.nameOf(player);
            if (recent && playerName != null && playerName.equals(opponent)) {
                FightLogModule.record(true, playerName);
            }
        }

        @SubscribeEvent
        public void onTargetDeath(TargetDeathEvent event) {
            Player player = event.getPlayer();
            if (FightLogModule.isSelf(player)) {
                return;
            }
            String playerName = FightLogModule.nameOf(player);
            if (playerName != null && !playerName.isEmpty()) {
                FightLogModule.record(true, playerName);
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

