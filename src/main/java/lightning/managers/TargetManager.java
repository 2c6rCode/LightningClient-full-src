/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package lightning.managers;

import java.util.ArrayList;
import java.util.Objects;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.TargetDeathEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.modules.impl.combat.KillAuraModule;
import lightning.utils.IMinecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class TargetManager
implements IMinecraft {
    private final ArrayList<Target> targets = new ArrayList();

    public TargetManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent clientConnectEvent) {
        this.targets.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        ArrayList<Target> arrayList;
        if (TargetManager.mc.player == null || TargetManager.mc.level == null) {
            return;
        }
        Player player = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class).getTarget();
        Entity entity = Night.MODULE_MANAGER.getModule(KillAuraModule.class).target;
        ArrayList<Target> arrayList2 = arrayList = this.targets;
        synchronized (arrayList2) {
            this.targets.removeIf(target -> System.currentTimeMillis() - target.time > 15000L);
            if (player != null) {
                this.targets.add(new Target(this, this, player));
            }
            if (entity instanceof Player) {
                this.targets.add(new Target(this, this, (Player)entity));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        ArrayList<Target> arrayList;
        if (TargetManager.mc.player == null || TargetManager.mc.level == null || !this.isTarget(playerDeathEvent.getPlayer())) {
            return;
        }
        ArrayList<Target> arrayList2 = arrayList = this.targets;
        synchronized (arrayList2) {
            Night.EVENT_HANDLER.post(new TargetDeathEvent(playerDeathEvent.getPlayer()));
            this.targets.remove(this.getTarget(playerDeathEvent.getPlayer()));
        }
    }

    private Target getTarget(Player player) {
        for (Target target : this.targets) {
            if (target.player != player) continue;
            return target;
        }
        return null;
    }

    public boolean isTarget(Player player) {
        for (Target target : this.targets) {
            if (target.player != player) continue;
            return true;
        }
        return false;
    }

    private class Target {
        private final Player player;
        private final long time;

        public Target(TargetManager targetManager, TargetManager targetManager2, Player player) {
            Objects.requireNonNull(targetManager);
            Objects.requireNonNull(targetManager2);
            this.player = player;
            this.time = System.currentTimeMillis();
        }
    }
}

