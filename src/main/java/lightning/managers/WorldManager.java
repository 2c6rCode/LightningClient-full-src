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

    public void onCrystalAttacked(int entityId, Runnable retryAction) {
        this.pendingCrystalRetries.put(entityId, retryAction);
    }

    public WorldManager() {
        Night.EVENT_HANDLER.subscribe(this);
        final BlockPos origin = BlockPos.ZERO;
        TreeSet<BlockPos> sortedOffsets = new TreeSet<BlockPos>(new Comparator<BlockPos>(){
            @Override
            public int compare(BlockPos first, BlockPos second) {
                if (first.equals((Object)second)) {
                    return 0;
                }
                int cmp = Double.compare(origin.distSqr((Vec3i)first), origin.distSqr((Vec3i)second));
                if (cmp == 0) {
                    cmp = Integer.compare(Math.abs(first.getX()) + Math.abs(first.getY()) + Math.abs(first.getZ()), Math.abs(second.getX()) + Math.abs(second.getY()) + Math.abs(second.getZ()));
                }
                return cmp == 0 ? 1 : cmp;
            }
        });
        for (int x = origin.getX() - 100; x <= origin.getX() + 100; ++x) {
            for (int z = origin.getZ() - 100; z <= origin.getZ() + 100; ++z) {
                for (int y = origin.getY() - 100; y < origin.getY() + 100; ++y) {
                    double distSq = (origin.getX() - x) * (origin.getX() - x) + (origin.getZ() - z) * (origin.getZ() - z) + (origin.getY() - y) * (origin.getY() - y);
                    if (!(distSq < (double)Mth.square((int)100))) continue;
                    sortedOffsets.add(new BlockPos(x, y, z));
                }
            }
        }
        int sphereIndex = 0;
        int radiusIndex = 0;
        for (BlockPos offset : sortedOffsets) {
            if (Math.sqrt(origin.distSqr((Vec3i)offset)) > (double)radiusIndex) {
                WorldManager.INDICES[radiusIndex++] = sphereIndex;
            }
            WorldManager.SPHERE[sphereIndex++] = offset;
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
                int pops = this.poppedTotems.getOrDefault(player.getUUID(), 0);
                this.poppedTotems.put(player.getUUID(), ++pops);
                Night.EVENT_HANDLER.post(new PlayerPopEvent(player, pops));
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

    public int getRadius(double radius) {
        return INDICES[Mth.clamp((int)((int)Math.ceil(radius)), (int)0, (int)INDICES.length)];
    }

    public Vec3i getOffset(int index) {
        return SPHERE[index];
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
    public void setTimerMultiplier(float timerMultiplier) {
        this.timerMultiplier = timerMultiplier;
    }

    @Generated
    public Timer getPlaceTimer() {
        return this.placeTimer;
    }
}

