/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.BlendFactor
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.mixins.accessors.RenderPipelinesAccessor
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.utils.IMinecraft;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class ParticleManager
implements IMinecraft {
    private static final int MAX_PARTICLES = 600;
    private static final double CULL_DISTANCE_SQ = 4096.0;
    private static final int CIRCLE_SEGMENTS = 16;
    private static final float[] CIRCLE_X = new float[17];
    private static final float[] CIRCLE_Y = new float[17];
    private static final RenderType NO_DEPTH_PARTICLES;
    private final List<Particle> particles = new CopyOnWriteArrayList<Particle>();
    private static final RandomSource RANDOM;
    private final Quaternionf rotYaw = new Quaternionf();
    private final Quaternionf rotPitch = new Quaternionf();

    public void addBouncy(Vec3 vec3, Vec3 vec32, double d, int n, int n2, boolean bl) {
        if (this.particles.size() >= 600) {
            return;
        }
        this.particles.add(new BouncyParticle(vec3, vec32, d, n, n2, bl));
    }

    public void addFriction(Vec3 vec3, Vec3 vec32, double d, double d2, int n, int n2, boolean bl) {
        if (this.particles.size() >= 600) {
            return;
        }
        this.particles.add(new FrictionParticle(vec3, vec32, d, d2, n, n2, bl));
    }

    public void tick() {
        this.particles.removeIf(Particle::tick);
    }

    public void render(PoseStack poseStack, float f) {
        if (this.particles.isEmpty() || ParticleManager.mc.gameRenderer == null) {
            return;
        }
        Camera camera = ParticleManager.mc.gameRenderer.mainCamera();
        if (camera == null) {
            return;
        }
        Vec3 vec3 = camera.position();
        long l = System.currentTimeMillis();
        int n = 0;
        for (Particle particle : this.particles) {
            if (!particle.canRender(vec3, f, l)) continue;
            n += particle.getCircleCount();
        }
        if (n == 0) {
            return;
        }
        this.rotYaw.identity().rotationY((float)Math.toRadians(-camera.yRot()));
        this.rotPitch.identity().rotationX((float)Math.toRadians(camera.xRot()));
        int n2 = n * 16 * 3;
        ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(n2 * DefaultVertexFormat.POSITION_COLOR.getVertexSize());
        try {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            for (Particle particle3 : this.particles) {
                if (!particle3.canRender(vec3, f, l)) continue;
                particle3.render(poseStack, bufferBuilder, vec3, f, l, this.rotYaw, this.rotPitch);
            }
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(NO_DEPTH_PARTICLES, meshData);
        }
        finally {
            if (byteBufferBuilder != null) {
                byteBufferBuilder.close();
            }
        }
    }

    public void clear() {
        this.particles.clear();
    }

    public boolean isEmpty() {
        return this.particles.isEmpty();
    }

    public int size() {
        return this.particles.size();
    }

    private static int alphaMulti(int n, float f) {
        int n2 = n >> 24 & 0xFF;
        int n3 = (int)((float)n2 * f);
        return (n3 & 0xFF) << 24 | n & 0xFFFFFF;
    }

    private static void renderCircle(Matrix4f matrix4f, BufferBuilder bufferBuilder, float f, float f2, int n) {
        for (int i = 0; i < 16; ++i) {
            float f3 = CIRCLE_X[i] * f;
            float f4 = CIRCLE_Y[i] * f;
            float f5 = CIRCLE_X[i + 1] * f;
            float f6 = CIRCLE_Y[i + 1] * f;
            bufferBuilder.addVertex((Matrix4fc)matrix4f, 0.0f, 0.0f, f2).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f3, f4, f2).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f5, f6, f2).setColor(n);
        }
    }

    private static AABB expandTowards(AABB aABB, double d, double d2, double d3) {
        return new AABB(aABB.minX + (d < 0.0 ? d : 0.0), aABB.minY + (d2 < 0.0 ? d2 : 0.0), aABB.minZ + (d3 < 0.0 ? d3 : 0.0), aABB.maxX + (d > 0.0 ? d : 0.0), aABB.maxY + (d2 > 0.0 ? d2 : 0.0), aABB.maxZ + (d3 > 0.0 ? d3 : 0.0));
    }

    private static float calcAlpha(double d) {
        if (d < 0.1) {
            return (float)(d * 10.0);
        }
        if (d > 0.5) {
            return (float)(1.0 - (d - 0.5) * 2.0);
        }
        return 1.0f;
    }

    public static Vec3 randomMotion(double d) {
        double d2 = RANDOM.nextDouble() * 2.0 * Math.PI;
        double d3 = RANDOM.nextDouble() * 2.0 * Math.PI;
        double d4 = Math.abs(Math.cos(d3));
        return new Vec3(d * Math.cos(d2) * d4, d * -Math.sin(d3), d * Math.sin(d2) * d4);
    }

    static {
        for (int i = 0; i <= 16; ++i) {
            double d = Math.PI * 2 * (double)i / 16.0;
            ParticleManager.CIRCLE_X[i] = (float)Math.cos(d);
            ParticleManager.CIRCLE_Y[i] = (float)Math.sin(d);
        }
        NO_DEPTH_PARTICLES = RenderType.create((String)"night_no_depth_particles", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()}).withLocation("lightning/no_depth_particles").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).createRenderSetup());
        RANDOM = RandomSource.create();
    }

    private static class BouncyParticle
    extends BaseParticle {
        private double motionX;
        private double motionY;
        private double motionZ;

        private BouncyParticle(Vec3 vec3, Vec3 vec32, double d, int n, int n2, boolean bl) {
            super(vec3, d, n, n2, bl);
            this.motionX = vec32.x;
            this.motionY = vec32.y;
            this.motionZ = vec32.z;
            this.tick();
        }

        @Override
        public boolean tick() {
            AABB aABB;
            if (System.currentTimeMillis() - this.startTime > this.lifetimeMillis) {
                return true;
            }
            this.prevPos = this.pos;
            if (IMinecraft.mc.level != null && IMinecraft.mc.player != null && IMinecraft.mc.level.getBlockCollisions((Entity)IMinecraft.mc.player, ParticleManager.expandTowards(aABB = new AABB(this.pos.x - 0.025, this.pos.y - 0.025, this.pos.z - 0.025, this.pos.x + 0.025, this.pos.y + 0.025, this.pos.z + 0.025), this.motionX, this.motionY, this.motionZ)).iterator().hasNext()) {
                if (this.motionX != 0.0 && IMinecraft.mc.level.getBlockCollisions((Entity)IMinecraft.mc.player, ParticleManager.expandTowards(aABB, this.motionX, 0.0, 0.0)).iterator().hasNext()) {
                    this.motionX *= -0.7;
                }
                if (this.motionY != 0.0 && IMinecraft.mc.level.getBlockCollisions((Entity)IMinecraft.mc.player, ParticleManager.expandTowards(aABB, 0.0, this.motionY, 0.0)).iterator().hasNext()) {
                    this.motionY *= -0.7;
                }
                if (this.motionZ != 0.0 && IMinecraft.mc.level.getBlockCollisions((Entity)IMinecraft.mc.player, ParticleManager.expandTowards(aABB, 0.0, 0.0, this.motionZ)).iterator().hasNext()) {
                    this.motionZ *= -0.7;
                }
            }
            this.pos = this.pos.add(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.98;
            this.motionZ *= 0.98;
            this.motionY = (this.motionY - 0.08) * 0.98;
            return false;
        }
    }

    private static class FrictionParticle
    extends BaseParticle {
        private final double friction;
        private Vec3 motion;

        private FrictionParticle(Vec3 vec3, Vec3 vec32, double d, double d2, int n, int n2, boolean bl) {
            super(vec3, d2, n, n2, bl);
            this.motion = vec32;
            this.friction = d;
            this.tick();
        }

        @Override
        public boolean tick() {
            if (System.currentTimeMillis() - this.startTime > this.lifetimeMillis) {
                return true;
            }
            this.prevPos = this.pos;
            this.motion = this.motion.scale(this.friction);
            this.pos = this.pos.add(this.motion);
            return false;
        }
    }

    private static interface Particle {
        public boolean tick();

        public boolean canRender(Vec3 var1, float var2, long var3);

        public int getCircleCount();

        public void render(PoseStack var1, BufferBuilder var2, Vec3 var3, float var4, long var5, Quaternionf var7, Quaternionf var8);
    }

    private static abstract class BaseParticle
    implements Particle {
        protected final int color;
        protected final int shadowColor;
        protected final boolean glow;
        protected final long startTime;
        protected final double lifetime;
        protected final long lifetimeMillis;
        protected Vec3 pos;
        protected Vec3 prevPos;

        protected BaseParticle(Vec3 vec3, double d, int n, int n2, boolean bl) {
            this.pos = vec3;
            this.prevPos = vec3;
            this.color = n;
            this.shadowColor = n2;
            this.glow = bl;
            this.startTime = System.currentTimeMillis();
            this.lifetime = d;
            this.lifetimeMillis = (long)(d * 1000.0);
        }

        @Override
        public int getCircleCount() {
            return this.glow ? 4 : 2;
        }

        @Override
        public boolean canRender(Vec3 vec3, float f, long l) {
            double d;
            double d2;
            double d3;
            double d4;
            long l2 = l - this.startTime;
            if (l2 > this.lifetimeMillis) {
                return false;
            }
            double d5 = (double)l2 / 1000.0 / this.lifetime;
            float f2 = ParticleManager.calcAlpha(Mth.clamp((double)d5, (double)0.0, (double)1.0));
            if (f2 <= 0.0f) {
                return false;
            }
            double dx = Mth.lerp((double)f, (double)this.prevPos.x, (double)this.pos.x) - vec3.x;
            double dy = Mth.lerp((double)f, (double)this.prevPos.y, (double)this.pos.y) - vec3.y;
            double dz = Mth.lerp((double)f, (double)this.prevPos.z, (double)this.pos.z) - vec3.z;
            return dx * dx + dy * dy + dz * dz <= 4096.0;
        }

        @Override
        public void render(PoseStack poseStack, BufferBuilder bufferBuilder, Vec3 vec3, float f, long l, Quaternionf quaternionf, Quaternionf quaternionf2) {
            long l2 = l - this.startTime;
            double d = (double)l2 / 1000.0 / this.lifetime;
            float f2 = ParticleManager.calcAlpha(Mth.clamp((double)d, (double)0.0, (double)1.0));
            double d2 = Mth.lerp((double)f, (double)this.prevPos.x, (double)this.pos.x) - vec3.x;
            double d3 = Mth.lerp((double)f, (double)this.prevPos.y, (double)this.pos.y) - vec3.y;
            double d4 = Mth.lerp((double)f, (double)this.prevPos.z, (double)this.pos.z) - vec3.z;
            poseStack.pushPose();
            poseStack.translate(d2, d3, d4);
            poseStack.scale(0.02f, 0.02f, 0.02f);
            poseStack.mulPose((Quaternionfc)quaternionf);
            poseStack.mulPose((Quaternionfc)quaternionf2);
            Matrix4f matrix4f = poseStack.last().pose();
            int n = ParticleManager.alphaMulti(this.shadowColor, f2);
            int n2 = ParticleManager.alphaMulti(this.color, f2);
            if (this.glow) {
                int n3 = ParticleManager.alphaMulti(this.color, f2 * 0.4f);
                ParticleManager.renderCircle(matrix4f, bufferBuilder, 4.0f, -0.02f, n3);
                ParticleManager.renderCircle(matrix4f, bufferBuilder, 2.5f, -0.015f, n3);
            }
            ParticleManager.renderCircle(matrix4f, bufferBuilder, 2.0f, -0.01f, n);
            ParticleManager.renderCircle(matrix4f, bufferBuilder, 1.0f, 0.0f, n2);
            poseStack.popPose();
        }
    }
}

