/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.mixins.accessors.LimbAnimatorAccessor
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundRespawnPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package lightning.modules.impl.visuals;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerDisconnectEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.ServerConnectEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.LimbAnimatorAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.core.IgnoreNakedModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

@RegisterModule(name="LogoutSpot", description="Renders a frozen chams model and nametag where players logged out.", category=Module.Category.VISUALS)
public class LogoutSpotModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the logout chams.", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting nametag = new BooleanSetting("Nametag", "Renders player info above the logout spot.", true);
    public BooleanSetting health = new BooleanSetting("Health", "Renders the health of the logged out player.", true);
    public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Renders the popped totems count of the logged out player.", true);
    public NumberSetting scale = new NumberSetting("Scale", "The scaling applied to the nametag rendering.", 30, 10, 100);
    private static final EquipmentSlot[] COPIED_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Map<UUID, Spot> spots = new ConcurrentHashMap<UUID, Spot>();
    private final Map<RemotePlayer, Spot> ghosts = new ConcurrentHashMap<RemotePlayer, Spot>();
    private final Map<UUID, TrackedPlayer> trackedPlayers = new ConcurrentHashMap<UUID, TrackedPlayer>();
    private int nextId = -80000;
    private static final long DISCONNECT_DEBOUNCE_MS = 1500L;
    private final Map<UUID, Long> pendingDisconnects = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, Long> recentPearlThrowers = new ConcurrentHashMap<UUID, Long>();
    private final Map<Integer, UUID> activePearls = new ConcurrentHashMap<Integer, UUID>();
    private long lastLocalTeleportTime = 0L;
    private Vec3 lastLocalPos = null;

    private boolean hasRecentlyPearled(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        Long throwTime = this.recentPearlThrowers.get(playerId);
        if (throwTime != null && System.currentTimeMillis() - throwTime < 12000L) {
            return true;
        }
        for (UUID activeThrowerId : this.activePearls.values()) {
            if (!playerId.equals(activeThrowerId)) continue;
            return true;
        }
        return false;
    }

    private void onLocalPlayerTeleport() {
        this.lastLocalTeleportTime = System.currentTimeMillis();
        this.pendingDisconnects.clear();
        this.trackedPlayers.clear();
    }

    public boolean isPlayerInServer(UUID playerId, String playerName) {
        if (playerId == null && playerName == null) {
            return false;
        }
        if (LogoutSpotModule.mc.level != null) {
            if (playerId != null && LogoutSpotModule.mc.level.getPlayerByUUID(playerId) != null) {
                return true;
            }
            if (playerName != null) {
                for (Player player : LogoutSpotModule.mc.level.players()) {
                    if (this.isGhost((Entity)player) || !playerName.equalsIgnoreCase(player.getName().getString())) continue;
                    return true;
                }
            }
        }
        if (mc.getConnection() != null) {
            if (playerId != null && mc.getConnection().getPlayerInfo(playerId) != null) {
                return true;
            }
            if (playerName != null && mc.getConnection().getPlayerInfo(playerName) != null) {
                return true;
            }
            for (PlayerInfo player : mc.getConnection().getOnlinePlayers()) {
                if (player == null || player.getProfile() == null) continue;
                if (playerId != null && playerId.equals(player.getProfile().id())) {
                    return true;
                }
                if (playerName == null || !playerName.equalsIgnoreCase(player.getProfile().name())) continue;
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (LogoutSpotModule.mc.level != null && LogoutSpotModule.mc.player != null) {
            UUID playerId;
            if (this.lastLocalPos != null && LogoutSpotModule.mc.player.position().distanceToSqr(this.lastLocalPos) > 100.0) {
                this.onLocalPlayerTeleport();
            }
            this.lastLocalPos = LogoutSpotModule.mc.player.position();
            long now = System.currentTimeMillis();
            this.recentPearlThrowers.entrySet().removeIf(entry -> now - (Long)entry.getValue() > 15000L);
            this.activePearls.entrySet().removeIf(entry -> LogoutSpotModule.mc.level.getEntity(((Integer)entry.getKey()).intValue()) == null);
            FakePlayerModule fakePlayerModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
            RemotePlayer remotePlayer = fakePlayerModule != null && fakePlayerModule.isToggled() ? fakePlayerModule.getPlayer() : null;
            boolean ignoreNaked = Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled();
            for (Player player : new ArrayList<Player>(LogoutSpotModule.mc.level.players())) {
                boolean holdingPearl;
                if (player == LogoutSpotModule.mc.player || player == remotePlayer || this.isGhost((Entity)player) || !player.isAlive() || ignoreNaked && EntityUtils.isNaked(player)) continue;
                playerId = player.getUUID();
                this.pendingDisconnects.remove(playerId);
                if (this.spots.containsKey(playerId)) {
                    this.removeSpot(playerId);
                }
                holdingPearl = player.getMainHandItem().getItem() == Items.ENDER_PEARL || player.getOffhandItem().getItem() == Items.ENDER_PEARL;
                if (holdingPearl && (player.swinging || player.isUsingItem())) {
                    this.recentPearlThrowers.put(playerId, now);
                }
                int pops = Night.WORLD_MANAGER != null ? Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(playerId, 0) : 0;
                this.trackedPlayers.put(playerId, new TrackedPlayer(player, pops));
            }
            if (!this.pendingDisconnects.isEmpty()) {
                for (Map.Entry<UUID, Long> pendingEntry : this.pendingDisconnects.entrySet()) {
                    playerId = pendingEntry.getKey();
                    TrackedPlayer trackedPlayer = this.trackedPlayers.get(playerId);
                    String playerName = trackedPlayer != null ? trackedPlayer.name : null;
                    if (this.isPlayerInServer(playerId, playerName)) {
                        this.pendingDisconnects.remove(playerId);
                        continue;
                    }
                    if (this.hasRecentlyPearled(playerId)) {
                        this.pendingDisconnects.remove(playerId);
                        continue;
                    }
                    if (now - pendingEntry.getValue() < 1500L) continue;
                    this.pendingDisconnects.remove(playerId);
                    this.spawnGhost(playerId);
                }
            }
            if (!this.spots.isEmpty()) {
                for (Map.Entry<UUID, Spot> spotEntry : this.spots.entrySet()) {
                    playerId = spotEntry.getKey();
                    Spot spot = spotEntry.getValue();
                    String playerName = spot.data != null ? spot.data.name : null;
                    if (!this.isPlayerInServer(playerId, playerName)) continue;
                    this.removeSpot(playerId);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerDisconnectEvent playerDisconnectEvent) {
        if (!this.isToggled() || LogoutSpotModule.mc.level == null || LogoutSpotModule.mc.player == null) {
            return;
        }
        UUID playerId = playerDisconnectEvent.getId();
        if (playerId == null || playerId.equals(LogoutSpotModule.mc.player.getUUID())) {
            return;
        }
        if (System.currentTimeMillis() - this.lastLocalTeleportTime < 3000L) {
            return;
        }
        FakePlayerModule fakePlayerModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
        if (fakePlayerModule != null && fakePlayerModule.isToggled() && fakePlayerModule.getPlayer() != null && playerId.equals(fakePlayerModule.getPlayer().getUUID())) {
            return;
        }
        if (!this.trackedPlayers.containsKey(playerId)) {
            return;
        }
        if (this.hasRecentlyPearled(playerId)) {
            this.trackedPlayers.remove(playerId);
            return;
        }
        this.pendingDisconnects.put(playerId, System.currentTimeMillis());
    }

    private void spawnGhost(UUID playerId) {
        TrackedPlayer trackedPlayer = this.trackedPlayers.get(playerId);
        if (trackedPlayer == null) {
            return;
        }
        mc.execute(() -> {
            if (LogoutSpotModule.mc.level == null || this.spots.containsKey(playerId)) {
                return;
            }
            if (this.isPlayerInServer(playerId, trackedPlayer.name)) {
                return;
            }
            if (this.hasRecentlyPearled(playerId)) {
                return;
            }
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), trackedPlayer.name);
            RemotePlayer remotePlayer = new RemotePlayer(LogoutSpotModule.mc.level, gameProfile);
            remotePlayer.setId(this.nextId--);
            remotePlayer.setPos(trackedPlayer.x, trackedPlayer.y, trackedPlayer.z);
            remotePlayer.xo = trackedPlayer.xo;
            remotePlayer.yo = trackedPlayer.yo;
            remotePlayer.zo = trackedPlayer.zo;
            remotePlayer.xOld = trackedPlayer.x;
            remotePlayer.yOld = trackedPlayer.y;
            remotePlayer.zOld = trackedPlayer.z;
            remotePlayer.setYRot(trackedPlayer.yRot);
            remotePlayer.setXRot(trackedPlayer.xRot);
            remotePlayer.yRotO = trackedPlayer.yRot;
            remotePlayer.xRotO = trackedPlayer.xRot;
            remotePlayer.setYHeadRot(trackedPlayer.yHeadRot);
            remotePlayer.yHeadRotO = trackedPlayer.yHeadRot;
            remotePlayer.yBodyRot = trackedPlayer.yBodyRot;
            remotePlayer.yBodyRotO = trackedPlayer.yBodyRot;
            remotePlayer.setPose(trackedPlayer.pose);
            remotePlayer.setShiftKeyDown(trackedPlayer.shift);
            remotePlayer.setSwimming(trackedPlayer.swim);
            remotePlayer.setSprinting(trackedPlayer.sprint);
            remotePlayer.refreshDimensions();
            remotePlayer.walkAnimation.setSpeed(trackedPlayer.walkSpeed);
            ((LimbAnimatorAccessor)remotePlayer.walkAnimation).setPos(trackedPlayer.walkPos);
            remotePlayer.swinging = trackedPlayer.swinging;
            remotePlayer.swingTime = trackedPlayer.swingTime;
            remotePlayer.swingingArm = trackedPlayer.swingingArm;
            remotePlayer.attackAnim = trackedPlayer.attackAnim;
            remotePlayer.oAttackAnim = trackedPlayer.oAttackAnim;
            remotePlayer.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            remotePlayer.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            for (EquipmentSlot equipmentSlot : COPIED_SLOTS) {
                remotePlayer.setItemSlot(equipmentSlot, trackedPlayer.armor.getOrDefault(equipmentSlot, ItemStack.EMPTY).copy());
            }
            LogoutSpotModule.mc.level.addEntity((Entity)remotePlayer);
            Spot spot = new Spot(remotePlayer, trackedPlayer);
            this.spots.put(playerId, spot);
            this.ghosts.put(remotePlayer, spot);
        });
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent playerConnectEvent) {
        UUID playerId = playerConnectEvent.getId();
        if (playerId == null) {
            return;
        }
        this.pendingDisconnects.remove(playerId);
        if (this.spots.containsKey(playerId)) {
            mc.execute(() -> this.removeSpot(playerId));
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (playerDeathEvent.getPlayer() != LogoutSpotModule.mc.player) {
            return;
        }
        this.clearAll();
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.clearAll();
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent clientConnectEvent) {
        this.clearAll();
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent serverConnectEvent) {
        this.clearAll();
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent entitySpawnEvent) {
        if (LogoutSpotModule.mc.level == null) {
            return;
        }
        Entity entity = entitySpawnEvent.getEntity();
        if (entity instanceof ThrownEnderpearl) {
            ThrownEnderpearl thrownEnderpearl = (ThrownEnderpearl)entity;
            UUID throwerId = null;
            Entity owner = thrownEnderpearl.getOwner();
            if (owner instanceof Player) {
                Player player = (Player)owner;
                if (player != LogoutSpotModule.mc.player && !this.isGhost((Entity)player)) {
                    throwerId = player.getUUID();
                }
            } else {
                double bestDistSq = 36.0;
                for (Player player : LogoutSpotModule.mc.level.players()) {
                    double distSq;
                    if (player == LogoutSpotModule.mc.player || this.isGhost((Entity)player) || !((distSq = player.distanceToSqr(thrownEnderpearl.position())) < bestDistSq)) continue;
                    bestDistSq = distSq;
                    throwerId = player.getUUID();
                }
            }
            if (throwerId != null) {
                this.recentPearlThrowers.put(throwerId, System.currentTimeMillis());
                this.activePearls.put(thrownEnderpearl.getId(), throwerId);
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (packetReceiveEvent.getPacket() instanceof ClientboundRespawnPacket) {
            mc.execute(this::clearAll);
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket clientboundPlayerPositionPacket = (ClientboundPlayerPositionPacket)packet;
            if (LogoutSpotModule.mc.player != null) {
                Vec3 playerPos = LogoutSpotModule.mc.player.position();
                double targetX = clientboundPlayerPositionPacket.change().position().x();
                double targetY = clientboundPlayerPositionPacket.change().position().y();
                double targetZ = clientboundPlayerPositionPacket.change().position().z();
                if (playerPos.distanceToSqr(targetX, targetY, targetZ) > 100.0) {
                    mc.execute(this::onLocalPlayerTeleport);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent.Post post) {
        if (!this.isToggled() || LogoutSpotModule.mc.level == null || LogoutSpotModule.mc.player == null) {
            return;
        }
        if (!this.nametag.getValue() || this.spots.isEmpty()) {
            return;
        }
        PoseStack poseStack = post.getMatrices();
        for (Spot spot : this.spots.values()) {
            RemotePlayer remotePlayer = spot.ghost;
            if (remotePlayer == null) continue;
            double gx = remotePlayer.getX();
            double gy = remotePlayer.getY() + (double)(remotePlayer.isShiftKeyDown() ? 1.9f : 2.1f);
            double gz = remotePlayer.getZ();
            Vec3 relPos = new Vec3(gx - LogoutSpotModule.mc.gameRenderer.mainCamera().position().x, gy - LogoutSpotModule.mc.gameRenderer.mainCamera().position().y, gz - LogoutSpotModule.mc.gameRenderer.mainCamera().position().z);
            float distance = (float)Math.sqrt(LogoutSpotModule.mc.gameRenderer.mainCamera().position().distanceToSqr(gx, gy, gz));
            float tagScale = 0.0018f + (float)this.scale.getValue().intValue() / 10000.0f * distance;
            if (distance <= 8.0f) {
                tagScale = 0.0245f;
            }
            poseStack.pushPose();
            poseStack.translate(relPos.x, relPos.y, relPos.z);
            poseStack.mulPose((Quaternionfc)LogoutSpotModule.mc.gameRenderer.mainCamera().rotation());
            poseStack.scale(tagScale, -tagScale, tagScale);
            StringBuilder stringBuilder = new StringBuilder(spot.data.name);
            if (this.health.getValue()) {
                float totalHealth = spot.data.health + spot.data.absorption;
                stringBuilder.append(" HP: ").append(new DecimalFormat("0.0").format(totalHealth));
            }
            if (this.totemPops.getValue() && spot.data.totemPops > 0) {
                stringBuilder.append(" -").append(spot.data.totemPops);
            }
            String tagText = stringBuilder.toString();
            int textWidth = Night.FONT_MANAGER.getWidth(tagText);
            int textHeight = Night.FONT_MANAGER.getHeight();
            Renderer3D.renderQuad(poseStack, (float)(-textWidth) / 2.0f - 2.0f, -textHeight - 1, (float)textWidth / 2.0f + 2.0f, 1.0f, new Color(0, 0, 0, 120));
            Renderer3D.renderOutline(poseStack, (float)(-textWidth) / 2.0f - 2.0f, -textHeight - 1, (float)textWidth / 2.0f + 2.0f, 1.0f, new Color(0, 0, 0, 160));
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
            Renderer3D.QUADS.clear();
            Renderer3D.DEBUG_LINES.clear();
            Night.FONT_MANAGER.drawText(poseStack, tagText, -textWidth / 2, -textHeight, ColorUtils.getColor(this.fillColor.getColor(), 255));
            poseStack.popPose();
        }
    }

    @Override
    public void onDisable() {
        this.clearAll();
    }

    private void removeSpot(UUID playerId) {
        Spot spot = this.spots.remove(playerId);
        if (spot != null) {
            this.ghosts.remove(spot.ghost);
            this.despawn(spot.ghost);
        }
    }

    private void clearAll() {
        this.spots.values().forEach(spot -> this.despawn(spot.ghost));
        this.spots.clear();
        this.ghosts.clear();
        this.trackedPlayers.clear();
        this.pendingDisconnects.clear();
        this.recentPearlThrowers.clear();
        this.activePearls.clear();
        this.lastLocalPos = null;
    }

    private void despawn(RemotePlayer remotePlayer) {
        if (remotePlayer == null) {
            return;
        }
        Runnable runnable = () -> {
            if (LogoutSpotModule.mc.level != null) {
                LogoutSpotModule.mc.level.removeEntity(remotePlayer.getId(), Entity.RemovalReason.DISCARDED);
            }
        };
        if (mc.isSameThread()) {
            runnable.run();
        } else {
            mc.execute(runnable);
        }
    }

    public boolean isGhost(Entity entity) {
        return entity instanceof RemotePlayer && this.ghosts.containsKey(entity);
    }

    public Collection<RemotePlayer> getGhosts() {
        return this.ghosts.keySet();
    }

    public Spot getSpot(RemotePlayer remotePlayer) {
        return this.ghosts.get(remotePlayer);
    }

    public boolean shouldFill() {
        return this.mode.getValue().equals("Fill") || this.mode.getValue().equals("Both");
    }

    public boolean shouldOutline() {
        return this.mode.getValue().equals("Outline") || this.mode.getValue().equals("Both");
    }

    public Color getFillColor(RemotePlayer remotePlayer) {
        return this.fillColor.getColor();
    }

    public Color getOutlineColor(RemotePlayer remotePlayer) {
        return this.outlineColor.getColor();
    }

    public static class TrackedPlayer {
        public final String name;
        public final double x;
        public final double y;
        public final double z;
        public final double xo;
        public final double yo;
        public final double zo;
        public final float yRot;
        public final float xRot;
        public final float yHeadRot;
        public final float yBodyRot;
        public final Pose pose;
        public final boolean shift;
        public final boolean swim;
        public final boolean sprint;
        public final float walkSpeed;
        public final float walkPos;
        public final boolean swinging;
        public final int swingTime;
        public final InteractionHand swingingArm;
        public final float attackAnim;
        public final float oAttackAnim;
        public final ItemStack mainHand;
        public final ItemStack offHand;
        public final Map<EquipmentSlot, ItemStack> armor = new ConcurrentHashMap<EquipmentSlot, ItemStack>();
        public final float health;
        public final float absorption;
        public final int totemPops;

        public TrackedPlayer(Player player, int totemPops) {
            this.name = player.getName().getString();
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.xo = player.xo;
            this.yo = player.yo;
            this.zo = player.zo;
            this.yRot = player.getYRot();
            this.xRot = player.getXRot();
            this.yHeadRot = player.getYHeadRot();
            this.yBodyRot = player.yBodyRot;
            this.pose = player.getPose();
            this.shift = player.isShiftKeyDown();
            this.swim = player.isSwimming();
            this.sprint = player.isSprinting();
            this.walkSpeed = player.walkAnimation.speed();
            this.walkPos = player.walkAnimation.position();
            this.swinging = player.swinging;
            this.swingTime = player.swingTime;
            this.swingingArm = player.swingingArm;
            this.attackAnim = player.attackAnim;
            this.oAttackAnim = player.oAttackAnim;
            this.mainHand = player.getMainHandItem().copy();
            this.offHand = player.getOffhandItem().copy();
            for (EquipmentSlot equipmentSlot : COPIED_SLOTS) {
                this.armor.put(equipmentSlot, player.getItemBySlot(equipmentSlot).copy());
            }
            this.health = player.getHealth();
            this.absorption = player.getAbsorptionAmount();
            this.totemPops = totemPops;
        }
    }

    public static class Spot {
        public final RemotePlayer ghost;
        public final TrackedPlayer data;
        public final long timestamp;

        public Spot(RemotePlayer remotePlayer, TrackedPlayer trackedPlayer) {
            this.ghost = remotePlayer;
            this.data = trackedPlayer;
            this.timestamp = System.currentTimeMillis();
        }
    }
}

