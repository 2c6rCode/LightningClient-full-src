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

    private boolean combat(long l) {
        AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        if (autoCrystalModule != null && autoCrystalModule.isToggled() && autoCrystalModule.getTarget() != null && autoCrystalModule.getTarget().isAlive()) {
            this.targetSeen = l;
        }
        return this.targetSeen != 0L && l - this.targetSeen <= this.targetGrace.getValue().longValue() * 1000000L;
    }

    private void expire(long l) {
        this.pending.removeIf(pending -> l - pending.time > 2000000000L);
        this.seen.entrySet().removeIf(entry -> l - (Long)entry.getValue() > 3000000000L);
        this.state.expire(l, this.resetDelay.getValue().intValue());
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (!this.isToggled()) {
            return;
        }
        this.context();
        if (CrystalWingsModule.mc.player != null && CrystalWingsModule.mc.level != null) {
            long l = System.nanoTime();
            this.combat(l);
            this.expire(l);
        }
    }

    @SubscribeEvent
    public void onUse(PacketSendEvent.Post post) {
        boolean bl;
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
        Vec3 vec3 = Vec3.atBottomCenterOf((Vec3i)serverboundUseItemOnPacket.getHitResult().getBlockPos().above());
        ClientLevel clientLevel = CrystalWingsModule.mc.level;
        LocalPlayer localPlayer = CrystalWingsModule.mc.player;
        mc.execute(() -> {
            if (!this.isToggled() || CrystalWingsModule.mc.level != clientLevel || CrystalWingsModule.mc.player != localPlayer) {
                return;
            }
            this.context();
            long l = System.nanoTime();
            if (!this.combat(l)) {
                return;
            }
            this.expire(l);
            while (this.pending.size() >= 64) {
                this.pending.removeFirst();
            }
            this.pending.addLast(new Pending(vec3, l));
        });
    }

    @SubscribeEvent
    public void onSpawn(EntitySpawnEvent entitySpawnEvent) {
        Entity entity = entitySpawnEvent.getEntity();
        if (!this.isToggled() || CrystalWingsModule.mc.player == null || CrystalWingsModule.mc.level == null || !(entity instanceof EndCrystal endCrystal)) {
            return;
        }
        Vec3 pos = endCrystal.position();
        UUID uUID = endCrystal.getUUID();
        ClientLevel clientLevel = CrystalWingsModule.mc.level;
        LocalPlayer localPlayer = CrystalWingsModule.mc.player;
        mc.execute(() -> this.handleWingSpawn(clientLevel, localPlayer, uUID, pos));
    }

    @SubscribeEvent
    public void onRender(RenderWorldEvent renderWorldEvent) {
        Vec3 vec3;
        Vec3 vec32;
        double d;
        if (!this.isToggled()) {
            return;
        }
        this.context();
        if (CrystalWingsModule.mc.player == null || CrystalWingsModule.mc.level == null || !Renderer3D.RENDERING) {
            return;
        }
        long l = System.nanoTime();
        this.combat(l);
        this.state.expire(l, this.resetDelay.getValue().intValue());
        if (this.anchor == null || !this.state.visible(l, this.holdTime.getValue().intValue(), this.targetSeen, this.targetGrace.getValue().intValue()) || this.opacity.getValue().intValue() == 0) {
            return;
        }
        double d2 = this.scale.getValue().doubleValue();
        double d3 = d2 * (1.0 + this.overshoot.getValue().doubleValue()) * (Math.max(this.width.getValue().doubleValue() * 2.0, this.height.getValue().doubleValue() * 1.5) + this.gap.getValue().doubleValue()) + this.heightOffset.getValue().doubleValue() + this.backOffset.getValue().doubleValue() + 1.0;
        if (CrystalWingsModule.mc.player.position().distanceToSqr(this.anchor) > Math.pow(this.range.getValue().doubleValue(), 2.0) || !Renderer3D.isFrustumVisible(new AABB(this.anchor.x - d3, this.anchor.y - d3, this.anchor.z - d3, this.anchor.x + d3, this.anchor.y + d3 + 3.0, this.anchor.z + d3))) {
            return;
        }
        String string = this.style.getValue();
        if (this.mesh == null || !string.equals(this.cachedStyle)) {
            this.mesh = CrystalWingsMath.model(string);
            this.cachedStyle = string;
        }
        double d4 = CrystalWingsMath.progress(l - this.state.appearedAt(), this.appearDuration.getValue().intValue());
        String string2 = this.appearStyle.getValue();
        double d5 = this.opacity.getValue().doubleValue() / 100.0 * (string2.equals("None") ? 1.0 : CrystalWingsMath.ease(d4));
        if (d5 < 0.001) {
            return;
        }
        d2 *= CrystalWingsMath.appearScale(string2, d4, this.startScale.getValue().doubleValue(), this.overshoot.getValue().doubleValue());
        double d6 = d = this.animate.getValue() ? CrystalWingsMath.flap((double)(l - this.animationOrigin) / 1.0E9, this.flapSpeed.getValue().doubleValue(), this.flapAngle.getValue().doubleValue()) : 0.0;
        if (string2.equals("Unfold")) {
            d += Math.toRadians(65.0) * (1.0 - CrystalWingsMath.ease(d4));
        }
        vec32 = (vec32 = this.anchor.subtract(vec3 = CrystalWingsModule.mc.gameRenderer.mainCamera().position()).multiply(1.0, 0.0, 1.0)).lengthSqr() < 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : vec32.normalize();
        Vec3 vec33 = new Vec3(vec32.z, 0.0, -vec32.x);
        Vec3 vec34 = this.anchor.add(0.0, this.heightOffset.getValue().doubleValue(), 0.0).add(vec32.scale(this.backOffset.getValue().doubleValue())).subtract(vec3);
        Matrix4f matrix4f = renderWorldEvent.getMatrices().last().pose();
        Color color = this.color.getColor();
        for (int n : new int[]{-1, 1}) {
            if (!string.equals("Outline")) {
                for (double[][] triangle : this.mesh.triangles()) {
                    Renderer3D.Vertex vertex = this.vertex(matrix4f, vec34, vec33, vec32, triangle[0], n, d2, d, color, d5);
                    Renderer3D.Vertex vertex2 = this.vertex(matrix4f, vec34, vec33, vec32, triangle[1], n, d2, d, color, d5);
                    Renderer3D.Vertex vertex3 = this.vertex(matrix4f, vec34, vec33, vec32, triangle[2], n, d2, d, color, d5);
                    Renderer3D.QUADS.add(new Renderer3D.VertexCollection(vertex, vertex2, vertex3, vertex3));
                }
            }
            this.lines(matrix4f, vec34, vec33, vec32, this.mesh.outline(), true, n, d2, d, color, d5);
            if (string.equals("Outline")) continue;
            for (List<double[]> seam : this.mesh.seams()) {
                this.lines(matrix4f, vec34, vec33, vec32, seam, false, n, d2, d, new Color(color.getRed() / 4, color.getGreen() / 4, color.getBlue() / 4, color.getAlpha()), d5 * 0.8);
            }
        }
    }

    private Renderer3D.Vertex vertex(Matrix4f matrix4f, Vec3 vec3, Vec3 vec32, Vec3 vec33, double[] dArray, int n, double d, double d2, Color color, double d3) {
        double d4 = (double)n * (this.gap.getValue().doubleValue() / 2.0 + dArray[0] * Math.cos(d2)) * d * this.width.getValue().doubleValue();
        double d5 = dArray[1] * d * this.height.getValue().doubleValue();
        double d6 = dArray[0] * Math.sin(d2) * d * this.width.getValue().doubleValue();
        Vec3 vec34 = vec3.add(vec32.scale(d4)).add(0.0, d5, 0.0).add(vec33.scale(d6));
        int n2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)Math.clamp((double)color.getAlpha() * d3, 0.0, 255.0)).getRGB();
        return new Renderer3D.Vertex(matrix4f, (float)vec34.x, (float)vec34.y, (float)vec34.z, n2);
    }

    private void lines(Matrix4f matrix4f, Vec3 vec3, Vec3 vec32, Vec3 vec33, List<double[]> list, boolean bl, int n, double d, double d2, Color color, double d3) {
        for (int i = 0; i < (bl ? list.size() : list.size() - 1); ++i) {
            double d4;
            double[] dArray = list.get(i);
            double[] dArray2 = list.get((i + 1) % list.size());
            double d5 = dArray2[0] - dArray[0];
            double d6 = Math.hypot(d5, d4 = dArray2[1] - dArray[1]);
            if (d6 < 1.0E-8) continue;
            double d7 = this.lineWidth.getValue().doubleValue() / 2.0;
            this.band(matrix4f, vec3, vec32, vec33, dArray, dArray2, -d4 / d6 * d7, d5 / d6 * d7, n, d, d2, color, d3, false);
            if (!this.glow.getValue() || !bl) continue;
            this.band(matrix4f, vec3, vec32, vec33, dArray, dArray2, -d4 / d6 * d7 * 3.0, d5 / d6 * d7 * 3.0, n, d, d2, color, d3 * this.glowStrength.getValue().doubleValue(), true);
        }
    }

    private void band(Matrix4f matrix4f, Vec3 vec3, Vec3 vec32, Vec3 vec33, double[] dArray, double[] dArray2, double d, double d2, int n, double d3, double d4, Color color, double d5, boolean bl) {
        List<Renderer3D.VertexCollection> list = bl ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS;
        list.add(new Renderer3D.VertexCollection(this.vertex(matrix4f, vec3, vec32, vec33, new double[]{dArray[0] - d, dArray[1] - d2}, n, d3, d4, color, d5), this.vertex(matrix4f, vec3, vec32, vec33, new double[]{dArray2[0] - d, dArray2[1] - d2}, n, d3, d4, color, d5), this.vertex(matrix4f, vec3, vec32, vec33, new double[]{dArray2[0] + d, dArray2[1] + d2}, n, d3, d4, color, d5), this.vertex(matrix4f, vec3, vec32, vec33, new double[]{dArray[0] + d, dArray[1] + d2}, n, d3, d4, color, d5)));
    }

    @Override
    public String getMetaData() {
        return this.state.count() + "/" + this.crystalCount.getValue().intValue();
    }

    private void handleWingSpawn(ClientLevel clientLevel, Object object, UUID uUID, Vec3 vec3) {
        if (!this.isToggled() || CrystalWingsModule.mc.level != clientLevel || CrystalWingsModule.mc.player != object) {
            return;
        }
        this.context();
        long l = System.nanoTime();
        this.expire(l);
        if (this.seen.containsKey(uUID) || !this.combat(l)) {
            return;
        }
        Pending pending = null;
        for (Pending pending2 : this.pending) {
            if (!(pending2.position.distanceToSqr(vec3) < 0.04)) continue;
            pending = pending2;
            break;
        }
        if (pending == null) {
            return;
        }
        this.pending.remove(pending);
        if (CrystalWingsModule.mc.player.position().distanceToSqr(vec3) > Math.pow(this.range.getValue().doubleValue(), 2.0)) {
            return;
        }
        while (this.seen.size() >= 128) {
            this.seen.remove(this.seen.keySet().iterator().next());
        }
        this.seen.put(uUID, l);
        this.state.record(BlockPos.containing((Position)vec3).asLong(), l, this.crystalCount.getValue().intValue(), this.resetDelay.getValue().intValue());
        this.anchor = vec3;
    }

    private record Pending(Vec3 position, long time) {
    }
}

