/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
 *  net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package lightning.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TickEvent;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WorldManager
implements IMinecraft {
    private static final Vec3i[] SPHERE = new Vec3i[4187707];
    private static final int[] INDICES = new int[101];
    private final Map<UUID, Integer> poppedTotems = new ConcurrentHashMap<UUID, Integer>();
    private final Set<UUID> deadPlayers = ConcurrentHashMap.newKeySet();
    private float timerMultiplier = 1.0f;
    private final Timer placeTimer = new Timer();
    private final Set<BlockPos> reservedPlacements = ConcurrentHashMap.newKeySet();
    private final Map<Integer, Runnable> pendingCrystalRetries = new ConcurrentHashMap<Integer, Runnable>();

    public void reservePlacement(BlockPos blockPos) {
        this.reservedPlacements.add(blockPos);
    }

    public boolean isReserved(BlockPos blockPos) {
        return this.reservedPlacements.contains(blockPos);
    }

    public Set<BlockPos> getReservedPlacements() {
        return this.reservedPlacements;
    }

    public void onCrystalAttacked(int n, Runnable runnable) {
        this.pendingCrystalRetries.put(n, runnable);
    }

    public WorldManager() {
        int n;
        int n2;
        Night.EVENT_HANDLER.subscribe(this);
        final BlockPos blockPos = BlockPos.ZERO;
        TreeSet<BlockPos> treeSet = new TreeSet<BlockPos>(new Comparator<BlockPos>(){
            @Override
            public int compare(BlockPos blockPos3, BlockPos blockPos2) {
                if (blockPos3.equals((Object)blockPos2)) {
                    return 0;
                }
                int n = Double.compare(blockPos.distSqr((Vec3i)blockPos3), blockPos.distSqr((Vec3i)blockPos2));
                if (n == 0) {
                    n = Integer.compare(Math.abs(blockPos3.getX()) + Math.abs(blockPos3.getY()) + Math.abs(blockPos3.getZ()), Math.abs(blockPos2.getX()) + Math.abs(blockPos2.getY()) + Math.abs(blockPos2.getZ()));
                }
                return n == 0 ? 1 : n;
            }
        });
        for (n2 = blockPos.getX() - 100; n2 <= blockPos.getX() + 100; ++n2) {
            for (n = blockPos.getZ() - 100; n <= blockPos.getZ() + 100; ++n) {
                for (int i = blockPos.getY() - 100; i < blockPos.getY() + 100; ++i) {
                    double d = (blockPos.getX() - n2) * (blockPos.getX() - n2) + (blockPos.getZ() - n) * (blockPos.getZ() - n) + (blockPos.getY() - i) * (blockPos.getY() - i);
                    if (!(d < (double)Mth.square((int)100))) continue;
                    treeSet.add(new BlockPos(n2, i, n));
                }
            }
        }
        n2 = 0;
        n = 0;
        for (BlockPos blockPos2 : treeSet) {
            if (Math.sqrt(blockPos.distSqr((Vec3i)blockPos2)) > (double)n) {
                WorldManager.INDICES[n++] = n2;
            }
            WorldManager.SPHERE[n2++] = blockPos2;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        this.reservedPlacements.clear();
        InventoryUtils.tickPendingRestore();
        if (WorldManager.mc.level != null) {
            List<Player> arrayList = new ArrayList<>(WorldManager.mc.level.players());
            for (UUID uUID : new ArrayList<UUID>(this.poppedTotems.keySet())) {
                if (!arrayList.stream().noneMatch(player -> player.getUUID().equals(uUID))) continue;
                this.poppedTotems.remove(uUID);
            }
            for (Player player : arrayList) {
                if (player.deathTime <= 0 && player.getHealth() > 0.0f) {
                    this.deadPlayers.remove(player.getUUID());
                    continue;
                }
                if (this.deadPlayers.contains(player.getUUID())) continue;
                Night.EVENT_HANDLER.post(new PlayerDeathEvent(player));
                this.deadPlayers.add(player.getUUID());
                this.poppedTotems.remove(player.getUUID());
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (WorldManager.mc.level == null) return;
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundSetHeldSlotPacket clientboundSetHeldSlotPacket) {
            if (WorldManager.mc.player != null && clientboundSetHeldSlotPacket.slot() != WorldManager.mc.player.getInventory().getSelectedSlot()) {
                packetReceiveEvent.setCancelled(true);
                return;
            }
        }
        if (packet instanceof ClientboundEntityEventPacket clientboundEntityEventPacket && clientboundEntityEventPacket.getEventId() == 35) {
            Entity entity = clientboundEntityEventPacket.getEntity((Level)WorldManager.mc.level);
            if (entity instanceof Player player) {
                int n = this.poppedTotems.getOrDefault(player.getUUID(), 0);
                this.poppedTotems.put(player.getUUID(), ++n);
                Night.EVENT_HANDLER.post(new PlayerPopEvent(player, n));
            }
        }
        if (this.pendingCrystalRetries.isEmpty()) return;
        if (packet instanceof ClientboundRemoveEntitiesPacket removePacket) {
            for (int id : removePacket.getEntityIds()) {
                Runnable runnable = this.pendingCrystalRetries.remove(id);
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }

    public int getRadius(double d) {
        return INDICES[Mth.clamp((int)((int)Math.ceil(d)), (int)0, (int)INDICES.length)];
    }

    public Vec3i getOffset(int n) {
        return SPHERE[n];
    }

    @Generated
    public Map<UUID, Integer> getPoppedTotems() {
        return this.poppedTotems;
    }

    @Generated
    public float getTimerMultiplier() {
        return this.timerMultiplier;
    }

    @Generated
    public void setTimerMultiplier(float f) {
        this.timerMultiplier = f;
    }

    @Generated
    public Timer getPlaceTimer() {
        return this.placeTimer;
    }
}

