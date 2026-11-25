/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.network.protocol.game.ClientboundExplodePacket
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 */
package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@RegisterModule(name="CrystalSplash", description="Water ripple effect on the ground when an end crystal explodes.", category=Module.Category.VISUALS)
public class CrystalSplashModule
extends Module {
    private static final double RING_LIFE = 1.6;
    private static final double RING_GAP = 0.22;
    private static final int MAX_SPLASHES = 24;
    public CategorySetting splashCategory = new CategorySetting("CrystalSplash", "Settings for the crystal explosion water effect.");
    public NumberSetting rings = new NumberSetting("Rings", "Rings", "Number of ripple rings.", new CategorySetting.Visibility(this.splashCategory), 3, 1, 5);
    public NumberSetting size = new NumberSetting("Size", "Size", "Maximum ripple radius in blocks.", new CategorySetting.Visibility(this.splashCategory), 3.0, 1.0, 6.0);
    public NumberSetting speed = new NumberSetting("Speed", "Speed", "Animation speed multiplier.", new CategorySetting.Visibility(this.splashCategory), 1.0, 0.5, 2.0);
    public NumberSetting range = new NumberSetting("Range", "Range", "Maximum distance at which the effect is shown.", new CategorySetting.Visibility(this.splashCategory), 64.0, 8.0, 128.0);
    public ColorSetting colorA = new ColorSetting("ColorA", "Water color", "Main color of the water ripples.", new CategorySetting.Visibility(this.splashCategory), new ColorSetting.Color(new Color(55, 138, 221, 255), false, false));
    public ColorSetting colorB = new ColorSetting("ColorB", "Highlight color", "Color of the ripple crest and sparkles.", new CategorySetting.Visibility(this.splashCategory), new ColorSetting.Color(new Color(230, 241, 251, 255), false, false));
    public BooleanSetting droplets = new BooleanSetting("Droplets", "Droplets", "Small low water droplets in the middle.", new CategorySetting.Visibility(this.splashCategory), true);
    public BooleanSetting crystalOnly = new BooleanSetting("CrystalOnly", "Crystal only", "Only show the effect for end crystal explosions.", new CategorySetting.Visibility(this.splashCategory), true);
    private final Queue<Vec3> pending = new ConcurrentLinkedQueue<Vec3>();
    private final List<Splash> splashes = new ArrayList<Splash>();
    private final List<Vec3> knownNow = new ArrayList<Vec3>();
    private final List<Vec3> knownPrev = new ArrayList<Vec3>();

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        ClientboundExplodePacket clientboundExplodePacket;
        Vec3 center;
        if (packetReceiveEvent.getPacket() instanceof ClientboundExplodePacket && (center = (clientboundExplodePacket = (ClientboundExplodePacket)packetReceiveEvent.getPacket()).center()) != null) {
            this.pending.add(center);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        Vec3 center;
        if (CrystalSplashModule.mc.level == null || CrystalSplashModule.mc.player == null) {
            this.pending.clear();
            this.splashes.clear();
            this.knownNow.clear();
            this.knownPrev.clear();
            return;
        }
        long now = System.currentTimeMillis();
        while ((center = this.pending.poll()) != null && this.splashes.size() < 24) {
            if (this.isDuplicate(center, now) || this.crystalOnly.getValue() && !this.isCrystalAt(center)) continue;
            this.splashes.add(new Splash(center, now));
        }
        this.knownPrev.clear();
        this.knownPrev.addAll(this.knownNow);
        this.knownNow.clear();
        AABB aABB = CrystalSplashModule.mc.player.getBoundingBox().inflate(128.0);
        for (EndCrystal endCrystal : CrystalSplashModule.mc.level.getEntitiesOfClass(EndCrystal.class, aABB)) {
            this.knownNow.add(endCrystal.position());
        }
        double maxLife = this.lifeEnd(this.rings.getValue().intValue());
        double animSpeed = this.speed.getValue().doubleValue();
        this.splashes.removeIf(splash -> (double)(now - splash.start) / 1000.0 * animSpeed > maxLife);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (this.splashes.isEmpty() || !Renderer3D.RENDERING) {
            return;
        }
        if (CrystalSplashModule.mc.gameRenderer == null || CrystalSplashModule.mc.gameRenderer.mainCamera() == null) {
            return;
        }
        PoseStack poseStack = renderWorldEvent.getMatrices();
        Matrix4f matrix4f = poseStack.last().pose();
        Vec3 camPos = CrystalSplashModule.mc.gameRenderer.mainCamera().position();
        long now = System.currentTimeMillis();
        double maxRange = this.range.getValue().doubleValue();
        for (Splash splash : this.splashes) {
            if (splash.pos.distanceToSqr(camPos) > maxRange * maxRange) continue;
            this.drawSplash(poseStack, matrix4f, camPos, splash, now);
        }
    }

    private double lifeEnd(int ringCount) {
        return 1.6 + 0.22 * (double)(ringCount - 1) + 1.0;
    }

    private boolean isDuplicate(Vec3 center, long now) {
        for (Splash splash : this.splashes) {
            if (now - splash.start >= 300L || !(splash.pos.distanceToSqr(center) < 0.25)) continue;
            return true;
        }
        return false;
    }

    private boolean isCrystalAt(Vec3 center) {
        AABB aABB = new AABB(center.x - 1.5, center.y - 1.5, center.z - 1.5, center.x + 1.5, center.y + 1.5, center.z + 1.5);
        if (!CrystalSplashModule.mc.level.getEntitiesOfClass(EndCrystal.class, aABB).isEmpty()) {
            return true;
        }
        for (Vec3 pos : this.knownNow) {
            if (!(pos.distanceToSqr(center) < 2.25)) continue;
            return true;
        }
        for (Vec3 pos : this.knownPrev) {
            if (!(pos.distanceToSqr(center) < 2.25)) continue;
            return true;
        }
        return false;
    }

    private static double hash(int xSeed, int ySeed) {
        double val = Math.sin((double)xSeed * 127.1 + (double)ySeed * 311.7) * 43758.5453;
        return val - Math.floor(val);
    }

    private static int argb(int rgb, double alpha) {
        int alphaByte = (int)(Math.max(0.0, Math.min(1.0, alpha)) * 255.0);
        return alphaByte << 24 | rgb & 0xFFFFFF;
    }

    private static Color color(int rgb, double alpha) {
        return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, (int)(Math.max(0.0, Math.min(1.0, alpha)) * 255.0));
    }

    private void drawSplash(PoseStack poseStack, Matrix4f matrix4f, Vec3 camPos, Splash splash, long now) {
        double frac;
        double puddleAlpha;
        int ringCount = this.rings.getValue().intValue();
        double maxSize = this.size.getValue().doubleValue();
        double elapsed = (double)(now - splash.start) / 1000.0 * this.speed.getValue().doubleValue();
        int primaryRgb = this.colorA.getColor().getRGB() & 0xFFFFFF;
        int highlightRgb = this.colorB.getColor().getRGB() & 0xFFFFFF;
        int deepRgb = 533594;
        double cx = splash.pos.x;
        double cy = splash.pos.y + 0.02;
        double cz = splash.pos.z;
        double baseSize = maxSize;
        double puddleProgress = Math.min(1.0, elapsed / 0.9);
        puddleAlpha = elapsed < 0.4 ? elapsed / 0.4 : Math.max(0.0, 1.0 - (elapsed - 0.4) / 2.2);
        if (puddleAlpha > 0.0) {
            frac = baseSize * (0.25 + 0.7 * puddleProgress);
            double[] radii = new double[]{0.0, frac * 0.7, frac};
            int[] colors = new int[]{CrystalSplashModule.argb(primaryRgb, 0.34 * puddleAlpha), CrystalSplashModule.argb(primaryRgb, 0.2 * puddleAlpha), CrystalSplashModule.argb(primaryRgb, 0.0)};
            this.bands(matrix4f, camPos, cx, cy, cz, radii, colors, 0.0, 0.0);
        }
        if (elapsed < 0.22) {
            frac = elapsed / 0.22;
            double flashRadius = 0.8 + 2.2 * frac;
            double[] radii = new double[]{0.0, flashRadius * 0.5, flashRadius};
            int[] colors = new int[]{CrystalSplashModule.argb(0xFFFFFF, 0.7 * (1.0 - frac)), CrystalSplashModule.argb(highlightRgb, 0.35 * (1.0 - frac)), CrystalSplashModule.argb(highlightRgb, 0.0)};
            this.bands(matrix4f, camPos, cx, cy, cz, radii, colors, 0.0, 0.0);
        }
        if (elapsed < 0.28) {
            frac = elapsed / 0.28;
            double burstRadius = baseSize * 0.55 * frac;
            double burstWidth = 0.12 * (1.0 - frac) + 0.03;
            double[] radii = new double[]{Math.max(0.02, burstRadius - burstWidth), burstRadius, burstRadius + burstWidth * 0.4};
            int[] colors = new int[]{CrystalSplashModule.argb(0xFFFFFF, 0.0), CrystalSplashModule.argb(0xFFFFFF, 0.7 * (1.0 - frac)), CrystalSplashModule.argb(0xFFFFFF, 0.0)};
            this.bands(matrix4f, camPos, cx, cy, cz, radii, colors, 0.0, 0.0);
        }
        for (int i = 0; i < ringCount; ++i) {
            double ringAge = elapsed - (double)i * 0.22;
            if (ringAge < 0.0 || ringAge > 1.6) continue;
            double ringProgress = ringAge / 1.6;
            double easedProgress = 1.0 - Math.pow(1.0 - ringProgress, 2.2);
            double ringRadius = baseSize * (0.08 + 0.92 * easedProgress);
            double bandWidth = (9.0 * (1.0 - ringProgress) + 3.5) / 36.0;
            double ringAlpha = Math.min(1.0, ringAge / 0.06) * Math.pow(1.0 - ringProgress, 1.15);
            double wobblePhase = elapsed * 6.0 + (double)i * 1.7;
            double[] radii = new double[]{Math.max(0.02, ringRadius - bandWidth), ringRadius - bandWidth * 0.4, ringRadius + bandWidth * 0.1, ringRadius + bandWidth * 0.56, ringRadius + bandWidth};
            int[] colors = new int[]{CrystalSplashModule.argb(primaryRgb, 0.0), CrystalSplashModule.argb(deepRgb, 0.32 * ringAlpha), CrystalSplashModule.argb(highlightRgb, 0.9 * ringAlpha), CrystalSplashModule.argb(primaryRgb, 0.42 * ringAlpha), CrystalSplashModule.argb(primaryRgb, 0.0)};
            this.bands(matrix4f, camPos, cx, cy, cz, radii, colors, wobblePhase, 0.025);
            this.crestLine(poseStack, cx, cy, cz, ringRadius, ringAlpha, wobblePhase, highlightRgb);
            if (i != 0) continue;
            this.sparkles(poseStack, cx, cy, cz, ringRadius, ringAlpha, elapsed, highlightRgb);
        }
        if (this.droplets.getValue()) {
            this.drawDroplets(poseStack, cx, cy, cz, maxSize, elapsed, primaryRgb, highlightRgb);
        }
    }

    private void bands(Matrix4f matrix4f, Vec3 camPos, double cx, double cy, double cz, double[] radii, int[] colors, double wobblePhase, double wobbleAmp) {
        int seg;
        int segments = 48;
        int bandCount = radii.length;
        float[][] xs = new float[bandCount][49];
        float[][] zs = new float[bandCount][49];
        float relY = (float)(cy - camPos.y);
        for (seg = 0; seg <= 48; ++seg) {
            double angle = (double)seg * Math.PI * 2.0 / 48.0;
            double wobble = 1.0 + wobbleAmp * (Math.sin(angle * 5.0 + wobblePhase) + 0.6 * Math.sin(angle * 9.0 - wobblePhase * 1.3));
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            for (int i = 0; i < bandCount; ++i) {
                double radius = radii[i] * wobble;
                xs[i][seg] = (float)(cx + cos * radius - camPos.x);
                zs[i][seg] = (float)(cz + sin * radius - camPos.z);
            }
        }
        for (seg = 0; seg < bandCount - 1; ++seg) {
            for (int i = 0; i < 48; ++i) {
                Renderer3D.QUADS.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(matrix4f, xs[seg][i], relY, zs[seg][i], colors[seg]), new Renderer3D.Vertex(matrix4f, xs[seg + 1][i], relY, zs[seg + 1][i], colors[seg + 1]), new Renderer3D.Vertex(matrix4f, xs[seg + 1][i + 1], relY, zs[seg + 1][i + 1], colors[seg + 1]), new Renderer3D.Vertex(matrix4f, xs[seg][i + 1], relY, zs[seg][i + 1], colors[seg])));
            }
        }
    }

    private void crestLine(PoseStack poseStack, double cx, double cy, double cz, double radius, double alpha, double wobblePhase, int highlightRgb) {
        int segments = 40;
        Color color = CrystalSplashModule.color(0xFFFFFF, 0.6 * alpha);
        Vec3 prevPoint = null;
        for (int i = 0; i <= 40; ++i) {
            double angle = (double)i * Math.PI * 2.0 / 40.0;
            double wobble = 1.0 + 0.025 * (Math.sin(angle * 5.0 + wobblePhase) + 0.6 * Math.sin(angle * 9.0 - wobblePhase * 1.3));
            Vec3 nextPoint = new Vec3(cx + Math.cos(angle) * radius * wobble, cy + 0.005, cz + Math.sin(angle) * radius * wobble);
            if (prevPoint != null) {
                Renderer3D.renderLine(poseStack, prevPoint, nextPoint, color);
            }
            prevPoint = nextPoint;
        }
    }

    private void sparkles(PoseStack poseStack, double cx, double cy, double cz, double radius, double alpha, double elapsed, int highlightRgb) {
        for (int i = 0; i < 14; ++i) {
            double angle = CrystalSplashModule.hash(i, 3) * Math.PI * 2.0;
            double twinkle = 0.5 + 0.5 * Math.sin(elapsed * 16.0 + (double)i * 2.3);
            double sx = cx + Math.cos(angle) * radius;
            double sz = cz + Math.sin(angle) * radius;
            AABB aABB = new AABB(sx - 0.03, cy, sz - 0.03, sx + 0.03, cy + 0.01, sz + 0.03);
            Renderer3D.renderBox(poseStack, aABB, CrystalSplashModule.color(0xFFFFFF, alpha * twinkle * 0.9));
        }
    }

    private void drawDroplets(PoseStack poseStack, double cx, double cy, double cz, double maxSize, double elapsed, int primaryRgb, int highlightRgb) {
        double spikeHeight;
        double dz;
        double dx;
        double crownRadius;
        double speedFactor;
        double angle;
        int idx;
        double crownProgress;
        if (elapsed < 0.5) {
            crownProgress = elapsed / 0.5;
            for (idx = 0; idx < 12; ++idx) {
                angle = (double)idx * Math.PI / 6.0 + 0.2;
                speedFactor = 0.6 + 0.4 * CrystalSplashModule.hash(idx, 7);
                crownRadius = maxSize * 0.1 * (0.4 + crownProgress);
                dx = cx + Math.cos(angle) * crownRadius;
                dz = cz + Math.sin(angle) * crownRadius;
                spikeHeight = 0.42 * Math.sin(Math.PI * Math.min(1.0, crownProgress)) * speedFactor;
                Color color = CrystalSplashModule.color(highlightRgb, (1.0 - crownProgress) * 0.85);
                Renderer3D.renderLine(poseStack, new Vec3(dx, cy, dz), new Vec3(dx, cy + spikeHeight, dz), color);
            }
        }
        double dropElapsed = elapsed - 0.02;
        if (dropElapsed < 0.0) {
            return;
        }
        for (idx = 0; idx < 10; ++idx) {
            angle = (double)idx * 0.628 + 0.5 + CrystalSplashModule.hash(idx, 1) * 0.4;
            double radialSpeed = maxSize * (0.35 + 0.25 * CrystalSplashModule.hash(idx, 2));
            double upVelocity = 1.94 + 0.83 * CrystalSplashModule.hash(idx, 4);
            double gravity = 8.33;
            double dropY = upVelocity * dropElapsed - 0.5 * gravity * dropElapsed * dropElapsed;
            if (dropY < 0.0) continue;
            double dropX = cx + Math.cos(angle) * radialSpeed * dropElapsed;
            double dropZ = cz + Math.sin(angle) * radialSpeed * dropElapsed;
            AABB aABB = new AABB(dropX - 0.03, cy + dropY - 0.03, dropZ - 0.03, dropX + 0.03, cy + dropY + 0.03, dropZ + 0.03);
            Renderer3D.renderBox(poseStack, aABB, CrystalSplashModule.color(primaryRgb, 0.95));
        }
    }

    private static final class Splash {
        final Vec3 pos;
        final long start;

        Splash(Vec3 pos, long start) {
            this.pos = pos;
            this.start = start;
        }
    }
}

