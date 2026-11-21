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

    private boolean hasRecentlyPearled(UUID uUID) {
        if (uUID == null) {
            return false;
        }
        Long l = this.recentPearlThrowers.get(uUID);
        if (l != null && System.currentTimeMillis() - l < 12000L) {
            return true;
        }
        for (UUID uUID2 : this.activePearls.values()) {
            if (!uUID.equals(uUID2)) continue;
            return true;
        }
        return false;
    }

    private void onLocalPlayerTeleport() {
        this.lastLocalTeleportTime = System.currentTimeMillis();
        this.pendingDisconnects.clear();
        this.trackedPlayers.clear();
    }

    public boolean isPlayerInServer(UUID uUID, String string) {
        if (uUID == null && string == null) {
            return false;
        }
        if (LogoutSpotModule.mc.level != null) {
            if (uUID != null && LogoutSpotModule.mc.level.getPlayerByUUID(uUID) != null) {
                return true;
            }
            if (string != null) {
                for (Player player : LogoutSpotModule.mc.level.players()) {
                    if (this.isGhost((Entity)player) || !string.equalsIgnoreCase(player.getName().getString())) continue;
                    return true;
                }
            }
        }
        if (mc.getConnection() != null) {
            if (uUID != null && mc.getConnection().getPlayerInfo(uUID) != null) {
                return true;
            }
            if (string != null && mc.getConnection().getPlayerInfo(string) != null) {
                return true;
            }
            for (PlayerInfo player : mc.getConnection().getOnlinePlayers()) {
                if (player == null || player.getProfile() == null) continue;
                if (uUID != null && uUID.equals(player.getProfile().id())) {
                    return true;
                }
                if (string == null || !string.equalsIgnoreCase(player.getProfile().name())) continue;
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (LogoutSpotModule.mc.level != null && LogoutSpotModule.mc.player != null) {
            UUID uUID;
            if (this.lastLocalPos != null && LogoutSpotModule.mc.player.position().distanceToSqr(this.lastLocalPos) > 100.0) {
                this.onLocalPlayerTeleport();
            }
            this.lastLocalPos = LogoutSpotModule.mc.player.position();
            long l = System.currentTimeMillis();
            this.recentPearlThrowers.entrySet().removeIf(entry -> l - (Long)entry.getValue() > 15000L);
            this.activePearls.entrySet().removeIf(entry -> LogoutSpotModule.mc.level.getEntity(((Integer)entry.getKey()).intValue()) == null);
            FakePlayerModule fakePlayerModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
            RemotePlayer remotePlayer = fakePlayerModule != null && fakePlayerModule.isToggled() ? fakePlayerModule.getPlayer() : null;
            boolean bl = Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled();
            for (Player object : new ArrayList<Player>(LogoutSpotModule.mc.level.players())) {
                boolean bl2;
                if (object == LogoutSpotModule.mc.player || object == remotePlayer || this.isGhost((Entity)object) || !object.isAlive() || bl && EntityUtils.isNaked(object)) continue;
                uUID = object.getUUID();
                this.pendingDisconnects.remove(uUID);
                if (this.spots.containsKey(uUID)) {
                    this.removeSpot(uUID);
                }
                boolean bl3 = bl2 = object.getMainHandItem().getItem() == Items.ENDER_PEARL || object.getOffhandItem().getItem() == Items.ENDER_PEARL;
                if (bl2 && (object.swinging || object.isUsingItem())) {
                    this.recentPearlThrowers.put(uUID, l);
                }
                int n = Night.WORLD_MANAGER != null ? Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(uUID, 0) : 0;
                this.trackedPlayers.put(uUID, new TrackedPlayer(object, n));
            }
            if (!this.pendingDisconnects.isEmpty()) {
                for (Map.Entry entry2 : this.pendingDisconnects.entrySet()) {
                    String string;
                    uUID = (UUID)entry2.getKey();
                    TrackedPlayer trackedPlayer = this.trackedPlayers.get(uUID);
                    String string2 = string = trackedPlayer != null ? trackedPlayer.name : null;
                    if (this.isPlayerInServer(uUID, string)) {
                        this.pendingDisconnects.remove(uUID);
                        continue;
                    }
                    if (this.hasRecentlyPearled(uUID)) {
                        this.pendingDisconnects.remove(uUID);
                        continue;
                    }
                    if (l - (Long)entry2.getValue() < 1500L) continue;
                    this.pendingDisconnects.remove(uUID);
                    this.spawnGhost(uUID);
                }
            }
            if (!this.spots.isEmpty()) {
                for (Map.Entry entry3 : this.spots.entrySet()) {
                    uUID = (UUID)entry3.getKey();
                    Spot spot = (Spot)entry3.getValue();
                    String string = spot.data != null ? spot.data.name : null;
                    if (!this.isPlayerInServer(uUID, string)) continue;
                    this.removeSpot(uUID);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerDisconnectEvent playerDisconnectEvent) {
        if (!this.isToggled() || LogoutSpotModule.mc.level == null || LogoutSpotModule.mc.player == null) {
            return;
        }
        UUID uUID = playerDisconnectEvent.getId();
        if (uUID == null || uUID.equals(LogoutSpotModule.mc.player.getUUID())) {
            return;
        }
        if (System.currentTimeMillis() - this.lastLocalTeleportTime < 3000L) {
            return;
        }
        FakePlayerModule fakePlayerModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
        FakePlayerModule fakePlayerModule2 = fakePlayerModule;
        if (fakePlayerModule != null && fakePlayerModule.isToggled() && fakePlayerModule.getPlayer() != null && uUID.equals(fakePlayerModule.getPlayer().getUUID())) {
            return;
        }
        if (!this.trackedPlayers.containsKey(uUID)) {
            return;
        }
        if (this.hasRecentlyPearled(uUID)) {
            this.trackedPlayers.remove(uUID);
            return;
        }
        this.pendingDisconnects.put(uUID, System.currentTimeMillis());
    }

    private void spawnGhost(UUID uUID) {
        TrackedPlayer trackedPlayer = this.trackedPlayers.get(uUID);
        if (trackedPlayer == null) {
            return;
        }
        mc.execute(() -> {
            if (LogoutSpotModule.mc.level == null || this.spots.containsKey(uUID)) {
                return;
            }
            if (this.isPlayerInServer(uUID, trackedPlayer.name)) {
                return;
            }
            if (this.hasRecentlyPearled(uUID)) {
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
            this.spots.put(uUID, spot);
            this.ghosts.put(remotePlayer, spot);
        });
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent playerConnectEvent) {
        UUID uUID = playerConnectEvent.getId();
        if (uUID == null) {
            return;
        }
        this.pendingDisconnects.remove(uUID);
        if (this.spots.containsKey(uUID)) {
            mc.execute(() -> this.removeSpot(uUID));
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
            UUID uUID = null;
            Entity entity2 = thrownEnderpearl.getOwner();
            if (entity2 instanceof Player) {
                Player player = (Player)entity2;
                if (player != LogoutSpotModule.mc.player && !this.isGhost((Entity)player)) {
                    uUID = player.getUUID();
                }
            } else {
                double d = 36.0;
                for (Player player : LogoutSpotModule.mc.level.players()) {
                    double d2;
                    if (player == LogoutSpotModule.mc.player || this.isGhost((Entity)player) || !((d2 = player.distanceToSqr(thrownEnderpearl.position())) < d)) continue;
                    d = d2;
                    uUID = player.getUUID();
                }
            }
            if (uUID != null) {
                this.recentPearlThrowers.put(uUID, System.currentTimeMillis());
                this.activePearls.put(thrownEnderpearl.getId(), uUID);
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
            double d = remotePlayer.getX();
            double d2 = remotePlayer.getY() + (double)(remotePlayer.isShiftKeyDown() ? 1.9f : 2.1f);
            double d3 = remotePlayer.getZ();
            Vec3 vec3 = new Vec3(d - LogoutSpotModule.mc.gameRenderer.mainCamera().position().x, d2 - LogoutSpotModule.mc.gameRenderer.mainCamera().position().y, d3 - LogoutSpotModule.mc.gameRenderer.mainCamera().position().z);
            float f = (float)Math.sqrt(LogoutSpotModule.mc.gameRenderer.mainCamera().position().distanceToSqr(d, d2, d3));
            float f2 = 0.0018f + (float)this.scale.getValue().intValue() / 10000.0f * f;
            if (f <= 8.0f) {
                f2 = 0.0245f;
            }
            poseStack.pushPose();
            poseStack.translate(vec3.x, vec3.y, vec3.z);
            poseStack.mulPose((Quaternionfc)LogoutSpotModule.mc.gameRenderer.mainCamera().rotation());
            poseStack.scale(f2, -f2, f2);
            StringBuilder stringBuilder = new StringBuilder(spot.data.name);
            if (this.health.getValue()) {
                float f3 = spot.data.health + spot.data.absorption;
                stringBuilder.append(" HP: ").append(new DecimalFormat("0.0").format(f3));
            }
            if (this.totemPops.getValue() && spot.data.totemPops > 0) {
                stringBuilder.append(" -").append(spot.data.totemPops);
            }
            String string = stringBuilder.toString();
            int n = Night.FONT_MANAGER.getWidth(string);
            int n2 = Night.FONT_MANAGER.getHeight();
            Renderer3D.renderQuad(poseStack, (float)(-n) / 2.0f - 2.0f, -n2 - 1, (float)n / 2.0f + 2.0f, 1.0f, new Color(0, 0, 0, 120));
            Renderer3D.renderOutline(poseStack, (float)(-n) / 2.0f - 2.0f, -n2 - 1, (float)n / 2.0f + 2.0f, 1.0f, new Color(0, 0, 0, 160));
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
            Renderer3D.QUADS.clear();
            Renderer3D.DEBUG_LINES.clear();
            Night.FONT_MANAGER.drawText(poseStack, string, -n / 2, -n2, ColorUtils.getColor(this.fillColor.getColor(), 255));
            poseStack.popPose();
        }
    }

    @Override
    public void onDisable() {
        this.clearAll();
    }

    private void removeSpot(UUID uUID) {
        Spot spot = this.spots.remove(uUID);
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

        public TrackedPlayer(Player player, int n) {
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
            this.totemPops = n;
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

