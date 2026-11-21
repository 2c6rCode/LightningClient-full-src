/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 */
package lightning.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;

@RegisterModule(name="Notifications", description="Notifies you in chat whenever something significant happens.", category=Module.Category.MISCELLANEOUS)
public class NotificationsModule
extends Module {
    public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Notifies you in chat whenever a player pops a totem.", true);
    public BooleanSetting visualRange = new BooleanSetting("VisualRange", "Notifies you in chat whenever a player enters your render distance.", false);
    public BooleanSetting pearlThrows = new BooleanSetting("PearlThrows", "Notifies you in chat whenever a player throws a pearl.", true);
    private final Map<UUID, String> loadedPlayers = new LinkedHashMap<UUID, String>();
    private final ArrayList<Integer> thrownPearls = new ArrayList<Integer>();

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (this.getNull()) {
            return;
        }
        if (this.visualRange.getValue()) {
            HashSet<UUID> loadedSet = new HashSet<UUID>();
            for (Entity entity : NotificationsModule.mc.level.entitiesForRendering()) {
                if (!(entity instanceof Player player)) continue;
                if (player == NotificationsModule.mc.player || EntityUtils.isGhost(player)) continue;
                FakePlayerModule fakePlayerModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
                if (fakePlayerModule != null && fakePlayerModule.isToggled() && player == fakePlayerModule.getPlayer()) continue;
                loadedSet.add(player.getUUID());
                if (this.loadedPlayers.containsKey(player.getUUID())) continue;
                this.loadedPlayers.put(player.getUUID(), player.getName().getString());
                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + player.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has entered your visual range.", "visual-range-" + player.getName().getString());
            }
            if (!this.loadedPlayers.isEmpty()) {
                var iterator = this.loadedPlayers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, String> entry = iterator.next();
                    if (loadedSet.contains(entry.getKey())) continue;
                    iterator.remove();
                    Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + entry.getValue() + String.valueOf(ChatUtils.getSecondary()) + " has left your visual range.", "visual-range-" + entry.getValue());
                }
            }
        }
        if (this.pearlThrows.getValue()) {
            for (Entity entity : NotificationsModule.mc.level.entitiesForRendering()) {
                if (!(entity instanceof ThrownEnderpearl pearl) || pearl.getOwner() == null || this.thrownPearls.contains(pearl.getId())) continue;
                String ownerName = pearl.getOwner().getName().getString();
                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + ownerName + String.valueOf(ChatUtils.getSecondary()) + " threw a pearl towards " + EntityUtils.getPearlDirection(pearl).toString() + ".", "pearl-throws-" + ownerName);
                this.thrownPearls.add(pearl.getId());
            }
            this.thrownPearls.removeIf(n -> !(NotificationsModule.mc.level.getEntity(n.intValue()) instanceof ThrownEnderpearl));
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent clientConnectEvent) {
        this.loadedPlayers.clear();
    }

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent playerPopEvent) {
        if (this.totemPops.getValue()) {
            Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + playerPopEvent.getPlayer().getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has popped " + String.valueOf(ChatUtils.getPrimary()) + playerPopEvent.getPops() + String.valueOf(ChatUtils.getSecondary()) + " totem" + (playerPopEvent.getPops() > 1 ? "s" : "") + ".", "totem-pop-" + playerPopEvent.getPlayer().getName().getString());
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        int n = Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(playerDeathEvent.getPlayer().getUUID(), 0);
        if (this.totemPops.getValue() && n > 0) {
            Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + playerDeathEvent.getPlayer().getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " has died after popping " + String.valueOf(ChatUtils.getPrimary()) + n + String.valueOf(ChatUtils.getSecondary()) + " totem" + (n > 1 ? "s" : "") + ".", "totem-pop-" + playerDeathEvent.getPlayer().getName().getString());
        }
    }
}

