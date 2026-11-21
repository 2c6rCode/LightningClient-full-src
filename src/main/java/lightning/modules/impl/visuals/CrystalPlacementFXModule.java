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
        boolean bl;
        Packet<?> packet;
        if (!this.isToggled() || CrystalPlacementFXModule.mc.player == null || CrystalPlacementFXModule.mc.level == null || !((packet = post.getPacket()) instanceof ServerboundUseItemOnPacket)) {
            return;
        }
        ServerboundUseItemOnPacket serverboundUseItemOnPacket = (ServerboundUseItemOnPacket)packet;
        boolean bl2 = bl = serverboundUseItemOnPacket.getHand() == InteractionHand.OFF_HAND ? CrystalPlacementFXModule.mc.player.getOffhandItem().is(Items.END_CRYSTAL) : CrystalPlacementFXModule.mc.player.getInventory().getItem(Night.POSITION_MANAGER.getServerSlot()).is(Items.END_CRYSTAL);
        if (!bl) {
            return;
        }
        Vec3 vec3 = Vec3.atBottomCenterOf((Vec3i)serverboundUseItemOnPacket.getHitResult().getBlockPos().above());
        ClientLevel clientLevel = CrystalPlacementFXModule.mc.level;
        long l = System.nanoTime();
        mc.execute(() -> {
            if (!this.isToggled() || CrystalPlacementFXModule.mc.level != clientLevel) {
                return;
            }
            if (this.effectLevel != clientLevel) {
                this.clear();
                this.effectLevel = clientLevel;
            }
            this.placements.removeIf(pending -> l - pending.start > 2000000000L);
            while (this.placements.size() >= 128) {
                this.placements.removeFirst();
            }
            this.placements.addLast(new Pending(vec3, l));
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
        UUID uUID = endCrystal.getUUID();
        ClientLevel clientLevel = CrystalPlacementFXModule.mc.level;
        long l = System.nanoTime();
        mc.execute(() -> this.lambda$onSpawn$0(clientLevel, l, pos, uUID));
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
        long l = System.nanoTime();
        int n = this.duration.getValue().intValue();
        this.bursts.values().removeIf(burst -> CrystalPlacementMath.progress(l - burst.start, n) >= 1.0);
        while (this.bursts.size() > this.maxEffects.getValue().intValue()) {
            this.bursts.remove(this.bursts.keySet().iterator().next());
        }
        if (!Renderer3D.RENDERING) {
            return;
        }
        double d = this.range.getValue().doubleValue();
        for (Burst burst2 : this.bursts.values()) {
            String string;
            double d2;
            double d3;
            double d4;
            if (CrystalPlacementFXModule.mc.player.position().distanceToSqr(burst2.center) > d * d || !Renderer3D.isFrustumVisible(new AABB(burst2.center.x - (d4 = this.radius.getValue().doubleValue() + this.expansion.getValue().doubleValue() + 0.8), burst2.center.y, burst2.center.z - d4, burst2.center.x + d4, burst2.center.y + this.riseHeight.getValue().doubleValue() + 1.0, burst2.center.z + d4)) || (d3 = CrystalPlacementMath.opacity(d2 = CrystalPlacementMath.progress(l - burst2.start, n))) < 0.001) continue;
            double d5 = CrystalPlacementMath.radius(this.radius.getValue().doubleValue(), this.expansion.getValue().doubleValue(), d2);
            double d6 = (double)(l - burst2.start) / 1.0E9 * Math.toRadians(this.rotationSpeed.getValue().doubleValue());
            Matrix4f matrix4f = renderWorldEvent.getMatrices().last().pose();
            Vec3 vec3 = burst2.center.add(0.0, this.heightOffset.getValue().doubleValue(), 0.0).subtract(CrystalPlacementFXModule.mc.gameRenderer.mainCamera().position());
            if (this.risingRing.getValue()) {
                double d7 = CrystalPlacementMath.ease(Math.min(1.0, d2 * this.riseSpeed.getValue().doubleValue()));
                this.ring(matrix4f, vec3.add(0.0, this.riseHeight.getValue().doubleValue() * d7, 0.0), d5 * 0.9, d6, d3, "Pulse");
            }
            if ((string = this.style.getValue()).equals("GroundGlow")) {
                this.disk(matrix4f, vec3, d5, d3);
                continue;
            }
            this.ring(matrix4f, vec3, d5, d6, d3, string);
            if (string.equals("Double")) {
                double d8 = Math.max(0.0, (d2 - 0.12) / 0.88);
                if (!(d2 > 0.12)) continue;
                this.ring(matrix4f, vec3, d5 * 0.72, -d6, CrystalPlacementMath.opacity(d8), "Pulse");
                continue;
            }
            if (string.equals("Rune")) {
                this.ring(matrix4f, vec3, d5 * 0.72, -d6, d3 * 0.7, "Segmented");
                for (int i = 0; i < 12; ++i) {
                    double d9 = d6 + (double)i * Math.PI / 6.0;
                    this.stroke(matrix4f, vec3, d5 * 1.08, d5 * 1.2, d9, d9 + 0.035, d3);
                }
                continue;
            }
            if (!string.equals("Orbit")) continue;
            for (int i = 0; i < 3; ++i) {
                this.arc(matrix4f, vec3, d5 * 1.2, d6 * 1.8 + (double)i * Math.PI * 2.0 / 3.0, 0.42, d3);
            }
        }
    }

    private Color tint(double d, double d2) {
        Color color = this.color.getColor();
        Color color2 = this.dualColor.getValue() ? this.secondaryColor.getColor() : color;
        double d3 = (Math.sin(d) + 1.0) / 2.0;
        int n = (int)Math.max(0.0, Math.min(255.0, ((double)color.getAlpha() * (1.0 - d3) + (double)color2.getAlpha() * d3) * d2));
        return new Color((int)((double)color.getRed() * (1.0 - d3) + (double)color2.getRed() * d3), (int)((double)color.getGreen() * (1.0 - d3) + (double)color2.getGreen() * d3), (int)((double)color.getBlue() * (1.0 - d3) + (double)color2.getBlue() * d3), n);
    }

    private void ring(Matrix4f matrix4f, Vec3 vec3, double d, double d2, double d3, String string) {
        int n = string.equals("Hexagon") ? 6 : this.quality.getValue().intValue();
        double d4 = this.thickness.getValue().doubleValue();
        for (int i = 0; i < n; ++i) {
            if (string.equals("Segmented") && i % 6 >= 4) continue;
            double d5 = d2 + (double)i * Math.PI * 2.0 / (double)n;
            double d6 = d2 + (double)(i + 1) * Math.PI * 2.0 / (double)n;
            double d7 = d * (string.equals("Petal") ? CrystalPlacementMath.petal(d5 - d2) : 1.0);
            double d8 = d * (string.equals("Petal") ? CrystalPlacementMath.petal(d6 - d2) : 1.0);
            this.band(matrix4f, vec3, d7 - d4 / 2.0, d8 - d4 / 2.0, d7 + d4 / 2.0, d8 + d4 / 2.0, d5, d6, d3, d3, false);
            if (!this.glow.getValue()) continue;
            double d9 = d3 * this.glowStrength.getValue().doubleValue() * 0.45;
            this.band(matrix4f, vec3, d7 - d4 * 3.0, d8 - d4 * 3.0, d7 - d4 / 2.0, d8 - d4 / 2.0, d5, d6, 0.0, d9, true);
            this.band(matrix4f, vec3, d7 + d4 / 2.0, d8 + d4 / 2.0, d7 + d4 * 3.0, d8 + d4 * 3.0, d5, d6, d9, 0.0, true);
        }
    }

    private void arc(Matrix4f matrix4f, Vec3 vec3, double d, double d2, double d3, double d4) {
        for (int i = 0; i < 8; ++i) {
            this.stroke(matrix4f, vec3, d - 0.025, d + 0.025, d2 + d3 * (double)i / 8.0, d2 + d3 * (double)(i + 1) / 8.0, d4 * (0.35 + 0.65 * (double)i / 8.0));
        }
    }

    private void stroke(Matrix4f matrix4f, Vec3 vec3, double d, double d2, double d3, double d4, double d5) {
        this.band(matrix4f, vec3, d, d, d2, d2, d3, d4, d5, d5, false);
    }

    private void disk(Matrix4f matrix4f, Vec3 vec3, double d, double d2) {
        int n = this.quality.getValue().intValue();
        for (int i = 0; i < n; ++i) {
            double d3 = (double)i * Math.PI * 2.0 / (double)n;
            double d4 = (double)(i + 1) * Math.PI * 2.0 / (double)n;
            this.band(matrix4f, vec3, 0.0, 0.0, d, d, d3, d4, d2 * 0.65, 0.0, this.glow.getValue());
        }
    }

    private Renderer3D.Vertex vertex(Matrix4f matrix4f, Vec3 vec3, double d, double d2, double d3) {
        return new Renderer3D.Vertex(matrix4f, (float)(vec3.x + Math.cos(d2) * Math.max(0.0, d)), (float)vec3.y, (float)(vec3.z + Math.sin(d2) * Math.max(0.0, d)), this.tint(d2, d3).getRGB());
    }

    private void band(Matrix4f matrix4f, Vec3 vec3, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, boolean bl) {
        List<Renderer3D.VertexCollection> list = bl ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS;
        list.add(new Renderer3D.VertexCollection(this.vertex(matrix4f, vec3, d, d5, d7), this.vertex(matrix4f, vec3, d2, d6, d7), this.vertex(matrix4f, vec3, d4, d6, d8), this.vertex(matrix4f, vec3, d3, d5, d8)));
    }

    private /* synthetic */ void lambda$onSpawn$0(ClientLevel clientLevel, long l, Vec3 vec3, UUID uUID) {
        if (!this.isToggled() || CrystalPlacementFXModule.mc.level != clientLevel || CrystalPlacementFXModule.mc.player == null) {
            return;
        }
        if (this.effectLevel != clientLevel) {
            this.clear();
            this.effectLevel = clientLevel;
        }
        this.placements.removeIf(pending -> l - pending.start > 2000000000L);
        Pending pending2 = null;
        for (Pending pending3 : this.placements) {
            if (!(pending3.center.distanceToSqr(vec3) < 0.04)) continue;
            pending2 = pending3;
            break;
        }
        if (pending2 == null) {
            return;
        }
        this.placements.remove(pending2);
        double d = this.range.getValue().doubleValue();
        if (CrystalPlacementFXModule.mc.player.position().distanceToSqr(vec3) > d * d || this.bursts.containsKey(uUID)) {
            return;
        }
        while (this.bursts.size() >= this.maxEffects.getValue().intValue()) {
            this.bursts.remove(this.bursts.keySet().iterator().next());
        }
        this.bursts.put(uUID, new Burst(vec3, l));
    }

    private record Burst(Vec3 center, long start) {
    }

    private record Pending(Vec3 center, long start) {
    }
}

