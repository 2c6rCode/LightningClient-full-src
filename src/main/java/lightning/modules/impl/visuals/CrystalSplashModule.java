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
        Vec3 vec3;
        if (packetReceiveEvent.getPacket() instanceof ClientboundExplodePacket && (vec3 = (clientboundExplodePacket = (ClientboundExplodePacket)packetReceiveEvent.getPacket()).center()) != null) {
            this.pending.add(vec3);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        Vec3 vec3;
        if (CrystalSplashModule.mc.level == null || CrystalSplashModule.mc.player == null) {
            this.pending.clear();
            this.splashes.clear();
            this.knownNow.clear();
            this.knownPrev.clear();
            return;
        }
        long l = System.currentTimeMillis();
        while ((vec3 = this.pending.poll()) != null && this.splashes.size() < 24) {
            if (this.isDuplicate(vec3, l) || this.crystalOnly.getValue() && !this.isCrystalAt(vec3)) continue;
            this.splashes.add(new Splash(vec3, l));
        }
        this.knownPrev.clear();
        this.knownPrev.addAll(this.knownNow);
        this.knownNow.clear();
        AABB aABB = CrystalSplashModule.mc.player.getBoundingBox().inflate(128.0);
        for (EndCrystal endCrystal : CrystalSplashModule.mc.level.getEntitiesOfClass(EndCrystal.class, aABB)) {
            this.knownNow.add(endCrystal.position());
        }
        double d = this.lifeEnd(this.rings.getValue().intValue());
        double d2 = this.speed.getValue().doubleValue();
        this.splashes.removeIf(splash -> (double)(l - splash.start) / 1000.0 * d2 > d);
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
        Vec3 vec3 = CrystalSplashModule.mc.gameRenderer.mainCamera().position();
        long l = System.currentTimeMillis();
        double d = this.range.getValue().doubleValue();
        for (Splash splash : this.splashes) {
            if (splash.pos.distanceToSqr(vec3) > d * d) continue;
            this.drawSplash(poseStack, matrix4f, vec3, splash, l);
        }
    }

    private double lifeEnd(int n) {
        return 1.6 + 0.22 * (double)(n - 1) + 1.0;
    }

    private boolean isDuplicate(Vec3 vec3, long l) {
        for (Splash splash : this.splashes) {
            if (l - splash.start >= 300L || !(splash.pos.distanceToSqr(vec3) < 0.25)) continue;
            return true;
        }
        return false;
    }

    private boolean isCrystalAt(Vec3 vec3) {
        AABB aABB = new AABB(vec3.x - 1.5, vec3.y - 1.5, vec3.z - 1.5, vec3.x + 1.5, vec3.y + 1.5, vec3.z + 1.5);
        if (!CrystalSplashModule.mc.level.getEntitiesOfClass(EndCrystal.class, aABB).isEmpty()) {
            return true;
        }
        for (Vec3 vec32 : this.knownNow) {
            if (!(vec32.distanceToSqr(vec3) < 2.25)) continue;
            return true;
        }
        for (Vec3 vec32 : this.knownPrev) {
            if (!(vec32.distanceToSqr(vec3) < 2.25)) continue;
            return true;
        }
        return false;
    }

    private static double hash(int n, int n2) {
        double d = Math.sin((double)n * 127.1 + (double)n2 * 311.7) * 43758.5453;
        return d - Math.floor(d);
    }

    private static int argb(int n, double d) {
        int n2 = (int)(Math.max(0.0, Math.min(1.0, d)) * 255.0);
        return n2 << 24 | n & 0xFFFFFF;
    }

    private static Color color(int n, double d) {
        return new Color(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, (int)(Math.max(0.0, Math.min(1.0, d)) * 255.0));
    }

    private void drawSplash(PoseStack poseStack, Matrix4f matrix4f, Vec3 vec3, Splash splash, long l) {
        double d;
        double d2;
        int n = this.rings.getValue().intValue();
        double d3 = this.size.getValue().doubleValue();
        double d4 = (double)(l - splash.start) / 1000.0 * this.speed.getValue().doubleValue();
        int n2 = this.colorA.getColor().getRGB() & 0xFFFFFF;
        int n3 = this.colorB.getColor().getRGB() & 0xFFFFFF;
        int n4 = 533594;
        double d5 = splash.pos.x;
        double d6 = splash.pos.y + 0.02;
        double d7 = splash.pos.z;
        double d8 = d3;
        double d9 = Math.min(1.0, d4 / 0.9);
        double d10 = d2 = d4 < 0.4 ? d4 / 0.4 : Math.max(0.0, 1.0 - (d4 - 0.4) / 2.2);
        if (d2 > 0.0) {
            d = d8 * (0.25 + 0.7 * d9);
            double[] dArray = new double[]{0.0, d * 0.7, d};
            int[] nArray = new int[]{CrystalSplashModule.argb(n2, 0.34 * d2), CrystalSplashModule.argb(n2, 0.2 * d2), CrystalSplashModule.argb(n2, 0.0)};
            this.bands(matrix4f, vec3, d5, d6, d7, dArray, nArray, 0.0, 0.0);
        }
        if (d4 < 0.22) {
            d = d4 / 0.22;
            double d11 = 0.8 + 2.2 * d;
            double[] dArray = new double[]{0.0, d11 * 0.5, d11};
            int[] nArray = new int[]{CrystalSplashModule.argb(0xFFFFFF, 0.7 * (1.0 - d)), CrystalSplashModule.argb(n3, 0.35 * (1.0 - d)), CrystalSplashModule.argb(n3, 0.0)};
            this.bands(matrix4f, vec3, d5, d6, d7, dArray, nArray, 0.0, 0.0);
        }
        if (d4 < 0.28) {
            d = d4 / 0.28;
            double d12 = d8 * 0.55 * d;
            double d13 = 0.12 * (1.0 - d) + 0.03;
            double[] dArray = new double[]{Math.max(0.02, d12 - d13), d12, d12 + d13 * 0.4};
            int[] nArray = new int[]{CrystalSplashModule.argb(0xFFFFFF, 0.0), CrystalSplashModule.argb(0xFFFFFF, 0.7 * (1.0 - d)), CrystalSplashModule.argb(0xFFFFFF, 0.0)};
            this.bands(matrix4f, vec3, d5, d6, d7, dArray, nArray, 0.0, 0.0);
        }
        for (int i = 0; i < n; ++i) {
            double d14 = d4 - (double)i * 0.22;
            if (d14 < 0.0 || d14 > 1.6) continue;
            double d15 = d14 / 1.6;
            double d16 = 1.0 - Math.pow(1.0 - d15, 2.2);
            double d17 = d8 * (0.08 + 0.92 * d16);
            double d18 = (9.0 * (1.0 - d15) + 3.5) / 36.0;
            double d19 = Math.min(1.0, d14 / 0.06) * Math.pow(1.0 - d15, 1.15);
            double d20 = d4 * 6.0 + (double)i * 1.7;
            double[] dArray = new double[]{Math.max(0.02, d17 - d18), d17 - d18 * 0.4, d17 + d18 * 0.1, d17 + d18 * 0.56, d17 + d18};
            int[] nArray = new int[]{CrystalSplashModule.argb(n2, 0.0), CrystalSplashModule.argb(n4, 0.32 * d19), CrystalSplashModule.argb(n3, 0.9 * d19), CrystalSplashModule.argb(n2, 0.42 * d19), CrystalSplashModule.argb(n2, 0.0)};
            this.bands(matrix4f, vec3, d5, d6, d7, dArray, nArray, d20, 0.025);
            this.crestLine(poseStack, d5, d6, d7, d17, d19, d20, n3);
            if (i != 0) continue;
            this.sparkles(poseStack, d5, d6, d7, d17, d19, d4, n3);
        }
        if (this.droplets.getValue()) {
            this.drawDroplets(poseStack, d5, d6, d7, d3, d4, n2, n3);
        }
    }

    private void bands(Matrix4f matrix4f, Vec3 vec3, double d, double d2, double d3, double[] dArray, int[] nArray, double d4, double d5) {
        int n;
        int n2 = 48;
        int n3 = dArray.length;
        float[][] fArray = new float[n3][49];
        float[][] fArray2 = new float[n3][49];
        float f = (float)(d2 - vec3.y);
        for (n = 0; n <= 48; ++n) {
            double d6 = (double)n * Math.PI * 2.0 / 48.0;
            double d7 = 1.0 + d5 * (Math.sin(d6 * 5.0 + d4) + 0.6 * Math.sin(d6 * 9.0 - d4 * 1.3));
            double d8 = Math.cos(d6);
            double d9 = Math.sin(d6);
            for (int i = 0; i < n3; ++i) {
                double d10 = dArray[i] * d7;
                fArray[i][n] = (float)(d + d8 * d10 - vec3.x);
                fArray2[i][n] = (float)(d3 + d9 * d10 - vec3.z);
            }
        }
        for (n = 0; n < n3 - 1; ++n) {
            for (int i = 0; i < 48; ++i) {
                Renderer3D.QUADS.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(matrix4f, fArray[n][i], f, fArray2[n][i], nArray[n]), new Renderer3D.Vertex(matrix4f, fArray[n + 1][i], f, fArray2[n + 1][i], nArray[n + 1]), new Renderer3D.Vertex(matrix4f, fArray[n + 1][i + 1], f, fArray2[n + 1][i + 1], nArray[n + 1]), new Renderer3D.Vertex(matrix4f, fArray[n][i + 1], f, fArray2[n][i + 1], nArray[n])));
            }
        }
    }

    private void crestLine(PoseStack poseStack, double d, double d2, double d3, double d4, double d5, double d6, int n) {
        int n2 = 40;
        Color color = CrystalSplashModule.color(0xFFFFFF, 0.6 * d5);
        Vec3 vec3 = null;
        for (int i = 0; i <= 40; ++i) {
            double d7 = (double)i * Math.PI * 2.0 / 40.0;
            double d8 = 1.0 + 0.025 * (Math.sin(d7 * 5.0 + d6) + 0.6 * Math.sin(d7 * 9.0 - d6 * 1.3));
            Vec3 vec32 = new Vec3(d + Math.cos(d7) * d4 * d8, d2 + 0.005, d3 + Math.sin(d7) * d4 * d8);
            if (vec3 != null) {
                Renderer3D.renderLine(poseStack, vec3, vec32, color);
            }
            vec3 = vec32;
        }
    }

    private void sparkles(PoseStack poseStack, double d, double d2, double d3, double d4, double d5, double d6, int n) {
        for (int i = 0; i < 14; ++i) {
            double d7 = CrystalSplashModule.hash(i, 3) * Math.PI * 2.0;
            double d8 = 0.5 + 0.5 * Math.sin(d6 * 16.0 + (double)i * 2.3);
            double d9 = d + Math.cos(d7) * d4;
            double d10 = d3 + Math.sin(d7) * d4;
            AABB aABB = new AABB(d9 - 0.03, d2, d10 - 0.03, d9 + 0.03, d2 + 0.01, d10 + 0.03);
            Renderer3D.renderBox(poseStack, aABB, CrystalSplashModule.color(0xFFFFFF, d5 * d8 * 0.9));
        }
    }

    private void drawDroplets(PoseStack poseStack, double d, double d2, double d3, double d4, double d5, int n, int n2) {
        double d6;
        double d7;
        double d8;
        double d9;
        double d10;
        double d11;
        int n3;
        double d12;
        if (d5 < 0.5) {
            d12 = d5 / 0.5;
            for (n3 = 0; n3 < 12; ++n3) {
                d11 = (double)n3 * Math.PI / 6.0 + 0.2;
                d10 = 0.6 + 0.4 * CrystalSplashModule.hash(n3, 7);
                d9 = d4 * 0.1 * (0.4 + d12);
                d8 = d + Math.cos(d11) * d9;
                d7 = d3 + Math.sin(d11) * d9;
                d6 = 0.42 * Math.sin(Math.PI * Math.min(1.0, d12)) * d10;
                Color color = CrystalSplashModule.color(n2, (1.0 - d12) * 0.85);
                Renderer3D.renderLine(poseStack, new Vec3(d8, d2, d7), new Vec3(d8, d2 + d6, d7), color);
            }
        }
        if ((d12 = d5 - 0.02) < 0.0) {
            return;
        }
        for (n3 = 0; n3 < 10; ++n3) {
            d11 = (double)n3 * 0.628 + 0.5 + CrystalSplashModule.hash(n3, 1) * 0.4;
            d10 = d4 * (0.35 + 0.25 * CrystalSplashModule.hash(n3, 2));
            d9 = 1.94 + 0.83 * CrystalSplashModule.hash(n3, 4);
            d7 = d9 * d12 - 0.5 * (d8 = 8.33) * d12 * d12;
            if (d7 < 0.0) continue;
            d6 = d + Math.cos(d11) * d10 * d12;
            double d13 = d3 + Math.sin(d11) * d10 * d12;
            AABB aABB = new AABB(d6 - 0.03, d2 + d7 - 0.03, d13 - 0.03, d6 + 0.03, d2 + d7 + 0.03, d13 + 0.03);
            Renderer3D.renderBox(poseStack, aABB, CrystalSplashModule.color(n, 0.95));
        }
    }

    private static final class Splash {
        final Vec3 pos;
        final long start;

        Splash(Vec3 vec3, long l) {
            this.pos = vec3;
            this.start = l;
        }
    }
}

