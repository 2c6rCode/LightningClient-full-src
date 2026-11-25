/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.CrystalWingsMath;
import lightning.utils.graphics.CrystalWingsState;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@RegisterModule(name="CrystalWings", description="One animated wing pair after repeated own crystal placements on one block in combat.", category=Module.Category.VISUALS)
public class CrystalWingsModule
extends Module {
    public ModeSetting style = new ModeSetting("Style", "Simple wing silhouette.", "Curl", CrystalWingsMath.STYLES);
    public NumberSetting crystalCount = new NumberSetting("CrystalCount", "Successful own placements on the same block before wings appear.", 50, 1, 500);
    public NumberSetting resetDelay = new NumberSetting("ResetDelay", "Idle milliseconds before the block counter resets.", 3000, 500, 15000);
    public NumberSetting holdTime = new NumberSetting("HoldTime", "Keep wings after the last placement, in milliseconds.", 1000, 100, 5000);
    public NumberSetting targetGrace = new NumberSetting("TargetGrace", "Grace for brief AutoCrystal target flicker, in milliseconds.", 350, 0, 1500);
    public NumberSetting scale = new NumberSetting("Scale", "Overall wing size.", Float.valueOf(0.75f), Float.valueOf(0.15f), Float.valueOf(2.0f));
    public NumberSetting width = new NumberSetting("Width", "Horizontal size multiplier.", Float.valueOf(1.0f), Float.valueOf(0.4f), Float.valueOf(2.0f));
    public NumberSetting height = new NumberSetting("Height", "Vertical size multiplier.", Float.valueOf(1.0f), Float.valueOf(0.4f), Float.valueOf(2.0f));
    public NumberSetting gap = new NumberSetting("Gap", "Distance between the two wing roots, in blocks.", Float.valueOf(0.35f), Float.valueOf(0.0f), Float.valueOf(1.5f));
    public NumberSetting heightOffset = new NumberSetting("HeightOffset", "Attachment height above the crystal base.", Float.valueOf(1.1f), Float.valueOf(0.1f), Float.valueOf(2.5f));
    public NumberSetting backOffset = new NumberSetting("BackOffset", "Move wings behind the crystal relative to the camera.", Float.valueOf(0.16f), Float.valueOf(0.0f), Float.valueOf(0.75f));
    public NumberSetting opacity = new NumberSetting("Opacity", "Wing opacity percent; 0 hides wings.", 55, 0, 100);
    public ColorSetting color = new ColorSetting("Color", "Wing color.", new ColorSetting.Color(Color.WHITE, false, false));
    public BooleanSetting animate = new BooleanSetting("Animate", "Gently flap the wings.", true);
    public NumberSetting flapSpeed = new NumberSetting("FlapSpeed", "Flaps per second.", Float.valueOf(0.65f), Float.valueOf(0.0f), Float.valueOf(3.0f));
    public NumberSetting flapAngle = new NumberSetting("FlapAngle", "Maximum flap angle in degrees.", Float.valueOf(12.0f), Float.valueOf(0.0f), Float.valueOf(40.0f));
    public BooleanSetting glow = new BooleanSetting("Glow", "One soft edge layer; no postprocessing shader.", false);
    public NumberSetting glowStrength = new NumberSetting("GlowStrength", "Edge glow intensity.", Float.valueOf(0.2f), Float.valueOf(0.0f), Float.valueOf(0.6f));
    public NumberSetting lineWidth = new NumberSetting("LineWidth", "Contour thickness in local wing units.", Float.valueOf(0.018f), Float.valueOf(0.005f), Float.valueOf(0.06f));
    public ModeSetting appearStyle = new ModeSetting("AppearStyle", "Appearance runs once when the block reaches CrystalCount.", "Grow", new String[]{"Grow", "Fade", "Unfold", "Pop", "None"});
    public NumberSetting appearDuration = new NumberSetting("AppearDuration", "Appearance duration in milliseconds.", 250, 50, 1500);
    public NumberSetting startScale = new NumberSetting("StartScale", "Initial size for Grow and Pop.", Float.valueOf(0.2f), Float.valueOf(0.05f), Float.valueOf(1.0f));
    public NumberSetting overshoot = new NumberSetting("Overshoot", "Small size bounce for Pop.", Float.valueOf(0.08f), Float.valueOf(0.0f), Float.valueOf(0.15f));
    public NumberSetting range = new NumberSetting("Range", "Maximum visible distance in blocks.", 32, 4, 96);
    private final ArrayDeque<Pending> pending = new ArrayDeque();
    private final LinkedHashMap<UUID, Long> seen = new LinkedHashMap();
    private final CrystalWingsState state = new CrystalWingsState();
    private ClientLevel world;
    private Object player;
    private Vec3 anchor;
    private long targetSeen;
    private long animationOrigin;
    private String cachedStyle;
    private CrystalWingsMath.Mesh mesh;

    private void clear() {
        this.pending.clear();
        this.seen.clear();
        this.state.clear();
        this.anchor = null;
        this.targetSeen = 0L;
        this.animationOrigin = System.nanoTime();
    }

    @Override
    public void onEnable() {
        this.clear();
        this.world = CrystalWingsModule.mc.level;
        this.player = CrystalWingsModule.mc.player;
    }

    @Override
    public void onDisable() {
        this.clear();
        this.world = null;
        this.player = null;
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        ClientLevel clientLevel = CrystalWingsModule.mc.level;
        LocalPlayer localPlayer = CrystalWingsModule.mc.player;
        mc.execute(() -> {
            if (CrystalWingsModule.mc.level == clientLevel && CrystalWingsModule.mc.player == localPlayer) {
                this.onDisable();
            }
        });
    }

    private void context() {
        if (this.world != CrystalWingsModule.mc.level || this.player != CrystalWingsModule.mc.player) {
            this.clear();
            this.world = CrystalWingsModule.mc.level;
            this.player = CrystalWingsModule.mc.player;
        }
    }

    private boolean combat(long now) {
        AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        if (autoCrystalModule != null && autoCrystalModule.isToggled() && autoCrystalModule.getTarget() != null && autoCrystalModule.getTarget().isAlive()) {
            this.targetSeen = now;
        }
        return this.targetSeen != 0L && now - this.targetSeen <= this.targetGrace.getValue().longValue() * 1000000L;
    }

    private void expire(long now) {
        this.pending.removeIf(pending -> now - pending.time > 2000000000L);
        this.seen.entrySet().removeIf(entry -> now - (Long)entry.getValue() > 3000000000L);
        this.state.expire(now, this.resetDelay.getValue().intValue());
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (!this.isToggled()) {
            return;
        }
        this.context();
        if (CrystalWingsModule.mc.player != null && CrystalWingsModule.mc.level != null) {
            long now = System.nanoTime();
            this.combat(now);
            this.expire(now);
        }
    }

    @SubscribeEvent
    public void onUse(PacketSendEvent.Post post) {
        Packet<?> packet;
        if (!this.isToggled() || CrystalWingsModule.mc.player == null || CrystalWingsModule.mc.level == null || !((packet = post.getPacket()) instanceof ServerboundUseItemOnPacket)) {
            return;
        }
        ServerboundUseItemOnPacket serverboundUseItemOnPacket = (ServerboundUseItemOnPacket)packet;
        int serverSlot = Night.POSITION_MANAGER.getServerSlot();
        boolean isCrystal = serverboundUseItemOnPacket.getHand() == InteractionHand.OFF_HAND ? CrystalWingsModule.mc.player.getOffhandItem().is(Items.END_CRYSTAL) : (serverSlot >= 0 && serverSlot < 9 && CrystalWingsModule.mc.player.getInventory().getItem(serverSlot).is(Items.END_CRYSTAL));
        if (!isCrystal) {
            return;
        }
        Vec3 placePos = Vec3.atBottomCenterOf((Vec3i)serverboundUseItemOnPacket.getHitResult().getBlockPos().above());
        ClientLevel clientLevel = CrystalWingsModule.mc.level;
        LocalPlayer localPlayer = CrystalWingsModule.mc.player;
        mc.execute(() -> {
            if (!this.isToggled() || CrystalWingsModule.mc.level != clientLevel || CrystalWingsModule.mc.player != localPlayer) {
                return;
            }
            this.context();
            long now = System.nanoTime();
            if (!this.combat(now)) {
                return;
            }
            this.expire(now);
            while (this.pending.size() >= 64) {
                this.pending.removeFirst();
            }
            this.pending.addLast(new Pending(placePos, now));
        });
    }

    @SubscribeEvent
    public void onSpawn(EntitySpawnEvent entitySpawnEvent) {
        Entity entity = entitySpawnEvent.getEntity();
        if (!this.isToggled() || CrystalWingsModule.mc.player == null || CrystalWingsModule.mc.level == null || !(entity instanceof EndCrystal endCrystal)) {
            return;
        }
        Vec3 pos = endCrystal.position();
        UUID crystalId = endCrystal.getUUID();
        ClientLevel clientLevel = CrystalWingsModule.mc.level;
        LocalPlayer localPlayer = CrystalWingsModule.mc.player;
        mc.execute(() -> this.handleWingSpawn(clientLevel, localPlayer, crystalId, pos));
    }

    @SubscribeEvent
    public void onRender(RenderWorldEvent renderWorldEvent) {
        Vec3 camPos;
        Vec3 toAnchorFlat;
        double flapRad;
        if (!this.isToggled()) {
            return;
        }
        this.context();
        if (CrystalWingsModule.mc.player == null || CrystalWingsModule.mc.level == null || !Renderer3D.RENDERING) {
            return;
        }
        long now = System.nanoTime();
        this.combat(now);
        this.state.expire(now, this.resetDelay.getValue().intValue());
        if (this.anchor == null || !this.state.visible(now, this.holdTime.getValue().intValue(), this.targetSeen, this.targetGrace.getValue().intValue()) || this.opacity.getValue().intValue() == 0) {
            return;
        }
        double wingScale = this.scale.getValue().doubleValue();
        double cullRadius = wingScale * (1.0 + this.overshoot.getValue().doubleValue()) * (Math.max(this.width.getValue().doubleValue() * 2.0, this.height.getValue().doubleValue() * 1.5) + this.gap.getValue().doubleValue()) + this.heightOffset.getValue().doubleValue() + this.backOffset.getValue().doubleValue() + 1.0;
        if (CrystalWingsModule.mc.player.position().distanceToSqr(this.anchor) > Math.pow(this.range.getValue().doubleValue(), 2.0) || !Renderer3D.isFrustumVisible(new AABB(this.anchor.x - cullRadius, this.anchor.y - cullRadius, this.anchor.z - cullRadius, this.anchor.x + cullRadius, this.anchor.y + cullRadius + 3.0, this.anchor.z + cullRadius))) {
            return;
        }
        String activeStyle = this.style.getValue();
        if (this.mesh == null || !activeStyle.equals(this.cachedStyle)) {
            this.mesh = CrystalWingsMath.model(activeStyle);
            this.cachedStyle = activeStyle;
        }
        double appearProgress = CrystalWingsMath.progress(now - this.state.appearedAt(), this.appearDuration.getValue().intValue());
        String activeAppearStyle = this.appearStyle.getValue();
        double wingOpacity = this.opacity.getValue().doubleValue() / 100.0 * (activeAppearStyle.equals("None") ? 1.0 : CrystalWingsMath.ease(appearProgress));
        if (wingOpacity < 0.001) {
            return;
        }
        wingScale *= CrystalWingsMath.appearScale(activeAppearStyle, appearProgress, this.startScale.getValue().doubleValue(), this.overshoot.getValue().doubleValue());
        flapRad = this.animate.getValue() ? CrystalWingsMath.flap((double)(now - this.animationOrigin) / 1.0E9, this.flapSpeed.getValue().doubleValue(), this.flapAngle.getValue().doubleValue()) : 0.0;
        if (activeAppearStyle.equals("Unfold")) {
            flapRad += Math.toRadians(65.0) * (1.0 - CrystalWingsMath.ease(appearProgress));
        }
        toAnchorFlat = (toAnchorFlat = this.anchor.subtract(camPos = CrystalWingsModule.mc.gameRenderer.mainCamera().position()).multiply(1.0, 0.0, 1.0)).lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : toAnchorFlat.normalize();
        Vec3 wingRight = new Vec3(toAnchorFlat.z, 0.0, -toAnchorFlat.x);
        Vec3 wingCenter = this.anchor.add(0.0, this.heightOffset.getValue().doubleValue(), 0.0).add(toAnchorFlat.scale(this.backOffset.getValue().doubleValue())).subtract(camPos);
        Matrix4f matrix4f = renderWorldEvent.getMatrices().last().pose();
        Color color = this.color.getColor();
        for (int side : new int[]{-1, 1}) {
            if (!activeStyle.equals("Outline")) {
                for (double[][] triangle : this.mesh.triangles()) {
                    Renderer3D.Vertex vertA = this.vertex(matrix4f, wingCenter, wingRight, toAnchorFlat, triangle[0], side, wingScale, flapRad, color, wingOpacity);
                    Renderer3D.Vertex vertB = this.vertex(matrix4f, wingCenter, wingRight, toAnchorFlat, triangle[1], side, wingScale, flapRad, color, wingOpacity);
                    Renderer3D.Vertex vertC = this.vertex(matrix4f, wingCenter, wingRight, toAnchorFlat, triangle[2], side, wingScale, flapRad, color, wingOpacity);
                    Renderer3D.QUADS.add(new Renderer3D.VertexCollection(vertA, vertB, vertC, vertC));
                }
            }
            this.lines(matrix4f, wingCenter, wingRight, toAnchorFlat, this.mesh.outline(), true, side, wingScale, flapRad, color, wingOpacity);
            if (activeStyle.equals("Outline")) continue;
            for (List<double[]> seam : this.mesh.seams()) {
                this.lines(matrix4f, wingCenter, wingRight, toAnchorFlat, seam, false, side, wingScale, flapRad, new Color(color.getRed() / 4, color.getGreen() / 4, color.getBlue() / 4, color.getAlpha()), wingOpacity * 0.8);
            }
        }
    }

    private Renderer3D.Vertex vertex(Matrix4f matrix4f, Vec3 origin, Vec3 rightVec, Vec3 backVec, double[] point, int side, double wingScale, double flapRad, Color wingColor, double wingOpacity) {
        double localX = (double)side * (this.gap.getValue().doubleValue() / 2.0 + point[0] * Math.cos(flapRad)) * wingScale * this.width.getValue().doubleValue();
        double localY = point[1] * wingScale * this.height.getValue().doubleValue();
        double localZ = point[0] * Math.sin(flapRad) * wingScale * this.width.getValue().doubleValue();
        Vec3 worldPos = origin.add(rightVec.scale(localX)).add(0.0, localY, 0.0).add(backVec.scale(localZ));
        int rgba = new Color(wingColor.getRed(), wingColor.getGreen(), wingColor.getBlue(), (int)Math.clamp((double)wingColor.getAlpha() * wingOpacity, 0.0, 255.0)).getRGB();
        return new Renderer3D.Vertex(matrix4f, (float)worldPos.x, (float)worldPos.y, (float)worldPos.z, rgba);
    }

    private void lines(Matrix4f matrix4f, Vec3 origin, Vec3 rightVec, Vec3 backVec, List<double[]> points, boolean closedLoop, int side, double wingScale, double flapRad, Color wingColor, double wingOpacity) {
        for (int i = 0; i < (closedLoop ? points.size() : points.size() - 1); ++i) {
            double dy;
            double[] p1 = points.get(i);
            double[] p2 = points.get((i + 1) % points.size());
            double dx = p2[0] - p1[0];
            double len = Math.hypot(dx, dy = p2[1] - p1[1]);
            if (len < 1.0E-8) continue;
            double halfWidth = this.lineWidth.getValue().doubleValue() / 2.0;
            this.band(matrix4f, origin, rightVec, backVec, p1, p2, -dy / len * halfWidth, dx / len * halfWidth, side, wingScale, flapRad, wingColor, wingOpacity, false);
            if (!this.glow.getValue() || !closedLoop) continue;
            this.band(matrix4f, origin, rightVec, backVec, p1, p2, -dy / len * halfWidth * 3.0, dx / len * halfWidth * 3.0, side, wingScale, flapRad, wingColor, wingOpacity * this.glowStrength.getValue().doubleValue(), true);
        }
    }

    private void band(Matrix4f matrix4f, Vec3 origin, Vec3 rightVec, Vec3 backVec, double[] p1, double[] p2, double offsetX, double offsetY, int side, double wingScale, double flapRad, Color wingColor, double wingOpacity, boolean isGlow) {
        List<Renderer3D.VertexCollection> targetQuads = isGlow ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS;
        targetQuads.add(new Renderer3D.VertexCollection(this.vertex(matrix4f, origin, rightVec, backVec, new double[]{p1[0] - offsetX, p1[1] - offsetY}, side, wingScale, flapRad, wingColor, wingOpacity), this.vertex(matrix4f, origin, rightVec, backVec, new double[]{p2[0] - offsetX, p2[1] - offsetY}, side, wingScale, flapRad, wingColor, wingOpacity), this.vertex(matrix4f, origin, rightVec, backVec, new double[]{p2[0] + offsetX, p2[1] + offsetY}, side, wingScale, flapRad, wingColor, wingOpacity), this.vertex(matrix4f, origin, rightVec, backVec, new double[]{p1[0] + offsetX, p1[1] + offsetY}, side, wingScale, flapRad, wingColor, wingOpacity)));
    }

    @Override
    public String getMetaData() {
        return this.state.count() + "/" + this.crystalCount.getValue().intValue();
    }

    private void handleWingSpawn(ClientLevel clientLevel, Object localPlayerObj, UUID crystalId, Vec3 pos) {
        if (!this.isToggled() || CrystalWingsModule.mc.level != clientLevel || CrystalWingsModule.mc.player != localPlayerObj) {
            return;
        }
        this.context();
        long now = System.nanoTime();
        this.expire(now);
        if (this.seen.containsKey(crystalId) || !this.combat(now)) {
            return;
        }
        Pending matchedPending = null;
        for (Pending candidate : this.pending) {
            if (!(candidate.position.distanceToSqr(pos) < 0.04)) continue;
            matchedPending = candidate;
            break;
        }
        if (matchedPending == null) {
            return;
        }
        this.pending.remove(matchedPending);
        if (CrystalWingsModule.mc.player.position().distanceToSqr(pos) > Math.pow(this.range.getValue().doubleValue(), 2.0)) {
            return;
        }
        while (this.seen.size() >= 128) {
            this.seen.remove(this.seen.keySet().iterator().next());
        }
        this.seen.put(crystalId, now);
        this.state.record(BlockPos.containing((Position)pos).asLong(), now, this.crystalCount.getValue().intValue(), this.resetDelay.getValue().intValue());
        this.anchor = pos;
    }

    private record Pending(Vec3 position, long time) {
    }
}

