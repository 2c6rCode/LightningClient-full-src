/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.CrystalPlacementMath;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.multiplayer.ClientLevel;
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

@RegisterModule(name="CrystalPlacementFX", description="Smooth rings and ground glow for your own crystal placements.", category=Module.Category.VISUALS)
public class CrystalPlacementFXModule
extends Module {
    public ModeSetting style = new ModeSetting("Style", "Placement effect preset.", "Pulse", new String[]{"Pulse", "Double", "Segmented", "Hexagon", "Rune", "GroundGlow", "Petal", "Orbit"});
    public ColorSetting color = new ColorSetting("Color", "Primary effect color.", new ColorSetting.Color(new Color(30, 240, 205, 210), false, false));
    public ColorSetting secondaryColor = new ColorSetting("SecondaryColor", "Secondary effect color.", new ColorSetting.Color(new Color(115, 140, 255, 180), false, false));
    public BooleanSetting dualColor = new BooleanSetting("DualColor", "Blend both colors around the effect.", true);
    public NumberSetting duration = new NumberSetting("Duration", "Effect lifetime in milliseconds.", 300, 100, 1500);
    public NumberSetting radius = new NumberSetting("Radius", "Starting ring radius in blocks.", Float.valueOf(0.55f), Float.valueOf(0.15f), Float.valueOf(2.0f));
    public NumberSetting expansion = new NumberSetting("Expansion", "Additional radial expansion in blocks.", Float.valueOf(0.25f), Float.valueOf(0.0f), Float.valueOf(2.0f));
    public NumberSetting thickness = new NumberSetting("Thickness", "Ring band width in blocks.", Float.valueOf(0.035f), Float.valueOf(0.01f), Float.valueOf(0.2f));
    public NumberSetting rotationSpeed = new NumberSetting("RotationSpeed", "Effect rotation in degrees per second.", Float.valueOf(90.0f), Float.valueOf(-360.0f), Float.valueOf(360.0f));
    public NumberSetting heightOffset = new NumberSetting("HeightOffset", "Height above the crystal base to avoid surface flicker.", Float.valueOf(0.025f), Float.valueOf(0.005f), Float.valueOf(0.5f));
    public BooleanSetting glow = new BooleanSetting("Glow", "Add soft additive glow; no post-processing shader.", true);
    public NumberSetting glowStrength = new NumberSetting("GlowStrength", "Soft glow intensity.", Float.valueOf(0.5f), Float.valueOf(0.0f), Float.valueOf(1.0f));
    public NumberSetting quality = new NumberSetting("Quality", "Segments per circular ring.", 48, 24, 96);
    public NumberSetting range = new NumberSetting("Range", "Maximum distance for placement effects.", 32, 4, 96);
    public NumberSetting maxEffects = new NumberSetting("MaxEffects", "Maximum simultaneous effects; oldest expire first.", 16, 1, 48);
    public BooleanSetting risingRing = new BooleanSetting("RisingRing", "An extra luminous ring rises from your crystal placement.", true);
    public NumberSetting riseHeight = new NumberSetting("RiseHeight", "Maximum rising ring height in blocks.", Float.valueOf(1.2f), Float.valueOf(0.1f), Float.valueOf(3.0f));
    public NumberSetting riseSpeed = new NumberSetting("RiseSpeed", "Rising animation speed multiplier.", Float.valueOf(1.0f), Float.valueOf(0.25f), Float.valueOf(3.0f));
    private final ArrayDeque<Pending> placements = new ArrayDeque();
    private final Map<UUID, Burst> bursts = new LinkedHashMap<UUID, Burst>();
    private ClientLevel effectLevel;

    @Override
    public void onEnable() {
        this.clear();
        this.effectLevel = CrystalPlacementFXModule.mc.level;
    }

    @Override
    public void onDisable() {
        this.clear();
    }

    private void clear() {
        this.bursts.clear();
        this.placements.clear();
        this.effectLevel = null;
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        mc.execute(this::clear);
    }

    @SubscribeEvent
    public void onPlaced(PacketSendEvent.Post post) {
        boolean holdingCrystal;
        Packet<?> packet;
        if (!this.isToggled() || CrystalPlacementFXModule.mc.player == null || CrystalPlacementFXModule.mc.level == null || !((packet = post.getPacket()) instanceof ServerboundUseItemOnPacket)) {
            return;
        }
        ServerboundUseItemOnPacket serverboundUseItemOnPacket = (ServerboundUseItemOnPacket)packet;
        holdingCrystal = serverboundUseItemOnPacket.getHand() == InteractionHand.OFF_HAND ? CrystalPlacementFXModule.mc.player.getOffhandItem().is(Items.END_CRYSTAL) : CrystalPlacementFXModule.mc.player.getInventory().getItem(Night.POSITION_MANAGER.getServerSlot()).is(Items.END_CRYSTAL);
        if (!holdingCrystal) {
            return;
        }
        Vec3 placePos = Vec3.atBottomCenterOf((Vec3i)serverboundUseItemOnPacket.getHitResult().getBlockPos().above());
        ClientLevel clientLevel = CrystalPlacementFXModule.mc.level;
        long now = System.nanoTime();
        mc.execute(() -> {
            if (!this.isToggled() || CrystalPlacementFXModule.mc.level != clientLevel) {
                return;
            }
            if (this.effectLevel != clientLevel) {
                this.clear();
                this.effectLevel = clientLevel;
            }
            this.placements.removeIf(pending -> now - pending.start > 2000000000L);
            while (this.placements.size() >= 128) {
                this.placements.removeFirst();
            }
            this.placements.addLast(new Pending(placePos, now));
        });
    }

    @SubscribeEvent
    public void onSpawn(EntitySpawnEvent entitySpawnEvent) {
        EndCrystal endCrystal;
        Entity entity;
        block3: {
            block2: {
                if (!this.isToggled() || !((entity = entitySpawnEvent.getEntity()) instanceof EndCrystal)) break block2;
                endCrystal = (EndCrystal)entity;
                if (CrystalPlacementFXModule.mc.player != null && CrystalPlacementFXModule.mc.level != null) break block3;
            }
            return;
        }
        Vec3 pos = endCrystal.position();
        UUID crystalId = endCrystal.getUUID();
        ClientLevel clientLevel = CrystalPlacementFXModule.mc.level;
        long now = System.nanoTime();
        mc.execute(() -> this.handleSpawnEffect(clientLevel, now, pos, crystalId));
    }

    @SubscribeEvent
    public void onRender(RenderWorldEvent renderWorldEvent) {
        if (!this.isToggled()) {
            return;
        }
        if (CrystalPlacementFXModule.mc.level == null || CrystalPlacementFXModule.mc.player == null || this.effectLevel != CrystalPlacementFXModule.mc.level) {
            this.clear();
            return;
        }
        long now = System.nanoTime();
        int durationMs = this.duration.getValue().intValue();
        this.bursts.values().removeIf(burst -> CrystalPlacementMath.progress(now - burst.start, durationMs) >= 1.0);
        while (this.bursts.size() > this.maxEffects.getValue().intValue()) {
            this.bursts.remove(this.bursts.keySet().iterator().next());
        }
        if (!Renderer3D.RENDERING) {
            return;
        }
        double maxRange = this.range.getValue().doubleValue();
        for (Burst burst : this.bursts.values()) {
            String activeStyle;
            double progress;
            double opacity;
            double cullRadius;
            if (CrystalPlacementFXModule.mc.player.position().distanceToSqr(burst.center) > maxRange * maxRange || !Renderer3D.isFrustumVisible(new AABB(burst.center.x - (cullRadius = this.radius.getValue().doubleValue() + this.expansion.getValue().doubleValue() + 0.8), burst.center.y, burst.center.z - cullRadius, burst.center.x + cullRadius, burst.center.y + this.riseHeight.getValue().doubleValue() + 1.0, burst.center.z + cullRadius)) || (opacity = CrystalPlacementMath.opacity(progress = CrystalPlacementMath.progress(now - burst.start, durationMs))) < 0.001) continue;
            double ringRadius = CrystalPlacementMath.radius(this.radius.getValue().doubleValue(), this.expansion.getValue().doubleValue(), progress);
            double rotationRad = (double)(now - burst.start) / 1.0E9 * Math.toRadians(this.rotationSpeed.getValue().doubleValue());
            Matrix4f matrix4f = renderWorldEvent.getMatrices().last().pose();
            Vec3 relPos = burst.center.add(0.0, this.heightOffset.getValue().doubleValue(), 0.0).subtract(CrystalPlacementFXModule.mc.gameRenderer.mainCamera().position());
            if (this.risingRing.getValue()) {
                double riseProgress = CrystalPlacementMath.ease(Math.min(1.0, progress * this.riseSpeed.getValue().doubleValue()));
                this.ring(matrix4f, relPos.add(0.0, this.riseHeight.getValue().doubleValue() * riseProgress, 0.0), ringRadius * 0.9, rotationRad, opacity, "Pulse");
            }
            if ((activeStyle = this.style.getValue()).equals("GroundGlow")) {
                this.disk(matrix4f, relPos, ringRadius, opacity);
                continue;
            }
            this.ring(matrix4f, relPos, ringRadius, rotationRad, opacity, activeStyle);
            if (activeStyle.equals("Double")) {
                double innerProgress = Math.max(0.0, (progress - 0.12) / 0.88);
                if (!(progress > 0.12)) continue;
                this.ring(matrix4f, relPos, ringRadius * 0.72, -rotationRad, CrystalPlacementMath.opacity(innerProgress), "Pulse");
                continue;
            }
            if (activeStyle.equals("Rune")) {
                this.ring(matrix4f, relPos, ringRadius * 0.72, -rotationRad, opacity * 0.7, "Segmented");
                for (int i = 0; i < 12; ++i) {
                    double tickAngle = rotationRad + (double)i * Math.PI / 6.0;
                    this.stroke(matrix4f, relPos, ringRadius * 1.08, ringRadius * 1.2, tickAngle, tickAngle + 0.035, opacity);
                }
                continue;
            }
            if (!activeStyle.equals("Orbit")) continue;
            for (int i = 0; i < 3; ++i) {
                this.arc(matrix4f, relPos, ringRadius * 1.2, rotationRad * 1.8 + (double)i * Math.PI * 2.0 / 3.0, 0.42, opacity);
            }
        }
    }

    private Color tint(double angle, double opacity) {
        Color primaryColor = this.color.getColor();
        Color endColor = this.dualColor.getValue() ? this.secondaryColor.getColor() : primaryColor;
        double blend = (Math.sin(angle) + 1.0) / 2.0;
        int alpha = (int)Math.max(0.0, Math.min(255.0, ((double)primaryColor.getAlpha() * (1.0 - blend) + (double)endColor.getAlpha() * blend) * opacity));
        return new Color((int)((double)primaryColor.getRed() * (1.0 - blend) + (double)endColor.getRed() * blend), (int)((double)primaryColor.getGreen() * (1.0 - blend) + (double)endColor.getGreen() * blend), (int)((double)primaryColor.getBlue() * (1.0 - blend) + (double)endColor.getBlue() * blend), alpha);
    }

    private void ring(Matrix4f matrix4f, Vec3 center, double radius, double rotationRad, double opacity, String styleName) {
        int segments = styleName.equals("Hexagon") ? 6 : this.quality.getValue().intValue();
        double bandThickness = this.thickness.getValue().doubleValue();
        for (int i = 0; i < segments; ++i) {
            if (styleName.equals("Segmented") && i % 6 >= 4) continue;
            double angleStart = rotationRad + (double)i * Math.PI * 2.0 / (double)segments;
            double angleEnd = rotationRad + (double)(i + 1) * Math.PI * 2.0 / (double)segments;
            double radiusStart = radius * (styleName.equals("Petal") ? CrystalPlacementMath.petal(angleStart - rotationRad) : 1.0);
            double radiusEnd = radius * (styleName.equals("Petal") ? CrystalPlacementMath.petal(angleEnd - rotationRad) : 1.0);
            this.band(matrix4f, center, radiusStart - bandThickness / 2.0, radiusEnd - bandThickness / 2.0, radiusStart + bandThickness / 2.0, radiusEnd + bandThickness / 2.0, angleStart, angleEnd, opacity, opacity, false);
            if (!this.glow.getValue()) continue;
            double glowOpacity = opacity * this.glowStrength.getValue().doubleValue() * 0.45;
            this.band(matrix4f, center, radiusStart - bandThickness * 3.0, radiusEnd - bandThickness * 3.0, radiusStart - bandThickness / 2.0, radiusEnd - bandThickness / 2.0, angleStart, angleEnd, 0.0, glowOpacity, true);
            this.band(matrix4f, center, radiusStart + bandThickness / 2.0, radiusEnd + bandThickness / 2.0, radiusStart + bandThickness * 3.0, radiusEnd + bandThickness * 3.0, angleStart, angleEnd, glowOpacity, 0.0, true);
        }
    }

    private void arc(Matrix4f matrix4f, Vec3 center, double radius, double startAngle, double arcSpan, double opacity) {
        for (int i = 0; i < 8; ++i) {
            this.stroke(matrix4f, center, radius - 0.025, radius + 0.025, startAngle + arcSpan * (double)i / 8.0, startAngle + arcSpan * (double)(i + 1) / 8.0, opacity * (0.35 + 0.65 * (double)i / 8.0));
        }
    }

    private void stroke(Matrix4f matrix4f, Vec3 center, double innerRadius, double outerRadius, double startAngle, double endAngle, double opacity) {
        this.band(matrix4f, center, innerRadius, innerRadius, outerRadius, outerRadius, startAngle, endAngle, opacity, opacity, false);
    }

    private void disk(Matrix4f matrix4f, Vec3 center, double radius, double opacity) {
        int segments = this.quality.getValue().intValue();
        for (int i = 0; i < segments; ++i) {
            double angleStart = (double)i * Math.PI * 2.0 / (double)segments;
            double angleEnd = (double)(i + 1) * Math.PI * 2.0 / (double)segments;
            this.band(matrix4f, center, 0.0, 0.0, radius, radius, angleStart, angleEnd, opacity * 0.65, 0.0, this.glow.getValue());
        }
    }

    private Renderer3D.Vertex vertex(Matrix4f matrix4f, Vec3 center, double radius, double angle, double opacity) {
        return new Renderer3D.Vertex(matrix4f, (float)(center.x + Math.cos(angle) * Math.max(0.0, radius)), (float)center.y, (float)(center.z + Math.sin(angle) * Math.max(0.0, radius)), this.tint(angle, opacity).getRGB());
    }

    private void band(Matrix4f matrix4f, Vec3 center, double innerStart, double innerEnd, double outerStart, double outerEnd, double angleStart, double angleEnd, double innerOpacity, double outerOpacity, boolean isGlow) {
        List<Renderer3D.VertexCollection> targetQuads = isGlow ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS;
        targetQuads.add(new Renderer3D.VertexCollection(this.vertex(matrix4f, center, innerStart, angleStart, innerOpacity), this.vertex(matrix4f, center, innerEnd, angleEnd, innerOpacity), this.vertex(matrix4f, center, outerEnd, angleEnd, outerOpacity), this.vertex(matrix4f, center, outerStart, angleStart, outerOpacity)));
    }

    private void handleSpawnEffect(ClientLevel clientLevel, long now, Vec3 pos, UUID crystalId) {
        if (!this.isToggled() || CrystalPlacementFXModule.mc.level != clientLevel || CrystalPlacementFXModule.mc.player == null) {
            return;
        }
        if (this.effectLevel != clientLevel) {
            this.clear();
            this.effectLevel = clientLevel;
        }
        this.placements.removeIf(pending -> now - pending.start > 2000000000L);
        Pending matchedPending = null;
        for (Pending candidate : this.placements) {
            if (!(candidate.center.distanceToSqr(pos) < 0.04)) continue;
            matchedPending = candidate;
            break;
        }
        if (matchedPending == null) {
            return;
        }
        this.placements.remove(matchedPending);
        double maxRange = this.range.getValue().doubleValue();
        if (CrystalPlacementFXModule.mc.player.position().distanceToSqr(pos) > maxRange * maxRange || this.bursts.containsKey(crystalId)) {
            return;
        }
        while (this.bursts.size() >= this.maxEffects.getValue().intValue()) {
            this.bursts.remove(this.bursts.keySet().iterator().next());
        }
        this.bursts.put(crystalId, new Burst(pos, now));
    }

    private record Burst(Vec3 center, long start) {
    }

    private record Pending(Vec3 center, long start) {
    }
}

