/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  lightning.mixins.accessors.RenderPipelinesAccessor
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.BindGroupLayouts
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderSetup$OutlineProperty
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.utils.IMinecraft;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class KillEffectMemeRenderer
implements IMinecraft {
    public static final KillEffectMemeRenderer INSTANCE = new KillEffectMemeRenderer();
    private static final Identifier CIRCLE_TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/effect/meme_circle.png");
    private static final Identifier ARROW_TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/effect/meme_arrow.png");
    private static final int BLINK_COUNT = 3;
    private static final float BLINK_ON_SEC = 0.18f;
    private static final float BLINK_OFF_SEC = 0.12f;
    private static final float CYCLE_SEC = 0.3f;
    private static RenderType circleLayer;
    private static RenderType arrowLayer;
    private static final RenderPipeline NO_DEPTH_MEME_PIPELINE;
    private final List<MemeInstance> instances = new ArrayList<MemeInstance>();
    private long lastNanos = 0L;

    private static RenderType buildLayer(String string, Identifier identifier) {
        return RenderType.create((String)("night_" + string), (RenderSetup)RenderSetup.builder((RenderPipeline)NO_DEPTH_MEME_PIPELINE).withTexture("Sampler0", identifier).useLightmap().useOverlay().affectsCrumbling().sortOnUpload().setOutline(RenderSetup.OutlineProperty.NONE).createRenderSetup());
    }

    private static RenderType getCircleLayer() {
        if (circleLayer != null) {
            return circleLayer;
        }
        circleLayer = KillEffectMemeRenderer.buildLayer("kill_effect_meme_circle", CIRCLE_TEXTURE_ID);
        return circleLayer;
    }

    private static RenderType getArrowLayer() {
        if (arrowLayer != null) {
            return arrowLayer;
        }
        arrowLayer = KillEffectMemeRenderer.buildLayer("kill_effect_meme_arrow", ARROW_TEXTURE_ID);
        return arrowLayer;
    }

    public void spawn(Vector3f vector3f, float f) {
        MemeInstance memeInstance = new MemeInstance();
        memeInstance.pos.set((Vector3fc)vector3f);
        memeInstance.size = f;
        this.instances.add(memeInstance);
    }

    public void render(PoseStack poseStack) {
        if (this.instances.isEmpty()) {
            this.lastNanos = 0L;
            return;
        }
        if (KillEffectMemeRenderer.mc.player == null || KillEffectMemeRenderer.mc.level == null) {
            return;
        }
        long l = System.nanoTime();
        float f = this.lastNanos == 0L ? 0.0f : Math.min((float)(l - this.lastNanos) / 1.0E9f, 0.1f);
        this.lastNanos = l;
        float f2 = 0.90000004f;
        Iterator<MemeInstance> iterator = this.instances.iterator();
        while (iterator.hasNext()) {
            MemeInstance instance = iterator.next();
            instance.age += f;
            if (!(instance.age >= f2)) continue;
            iterator.remove();
        }
        if (this.instances.isEmpty()) {
            return;
        }
        PoseStack.Pose pose = poseStack.last();
        Camera camera = KillEffectMemeRenderer.mc.gameRenderer.mainCamera();
        Vector3f vector3f = new Vector3f(camera.upVector());
        Vector3f vector3f2 = new Vector3f(camera.leftVector());
        Vec3 vec3 = camera.position();
        Vector3f vector3f3 = new Vector3f((float)vec3.x, (float)vec3.y, (float)vec3.z);
        Vector3f vector3f4 = new Vector3f();
        RenderType circleLayer = KillEffectMemeRenderer.getCircleLayer();
        try (ByteBufferBuilder circleByteBuffer = new ByteBufferBuilder(1024)) {
            BufferBuilder circleBufferBuilder = new BufferBuilder(circleByteBuffer, circleLayer.primitiveTopology(), circleLayer.format());
            for (MemeInstance instance : this.instances) {
                if (instance.age % 0.3f >= 0.18f) continue;
                vector3f4.set((Vector3fc)instance.pos).sub((Vector3fc)vector3f3);
                KillEffectMemeRenderer.emitBillboard(circleBufferBuilder, pose, vector3f4, vector3f, vector3f2, instance.size, 255);
            }
            MeshData meshData = circleBufferBuilder.build();
            if (meshData != null) {
                Renderer3D.draw(circleLayer, meshData);
            }
        }
        Vector3f arrowPos = new Vector3f();
        RenderType arrowLayer = KillEffectMemeRenderer.getArrowLayer();
        try (ByteBufferBuilder arrowByteBuffer = new ByteBufferBuilder(1024)) {
            BufferBuilder arrowBufferBuilder = new BufferBuilder(arrowByteBuffer, arrowLayer.primitiveTopology(), arrowLayer.format());
            for (MemeInstance instance : this.instances) {
                if (instance.age % 0.3f >= 0.18f) continue;
                vector3f4.set((Vector3fc)instance.pos).sub((Vector3fc)vector3f3);
                arrowPos.set((Vector3fc)vector3f2).mul(-instance.size * 1.1f).add(vector3f.x * instance.size * 1.1f, vector3f.y * instance.size * 1.1f, vector3f.z * instance.size * 1.1f).add((Vector3fc)vector3f4);
                KillEffectMemeRenderer.emitBillboard(arrowBufferBuilder, pose, arrowPos, vector3f, vector3f2, instance.size * 0.8f, 255);
            }
            MeshData meshData2 = arrowBufferBuilder.build();
            if (meshData2 != null) {
                Renderer3D.draw(arrowLayer, meshData2);
            }
        }
    }

    private static void emitBillboard(BufferBuilder bufferBuilder, PoseStack.Pose pose, Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3, float f, int n) {
        Vector3f vector3f4 = new Vector3f((Vector3fc)vector3f2).mul(f);
        Vector3f vector3f5 = new Vector3f((Vector3fc)vector3f3).mul(f);
        Vector3f vector3f6 = new Vector3f((Vector3fc)vector3f).sub((Vector3fc)vector3f4).sub((Vector3fc)vector3f5);
        Vector3f vector3f7 = new Vector3f((Vector3fc)vector3f).sub((Vector3fc)vector3f4).add((Vector3fc)vector3f5);
        Vector3f vector3f8 = new Vector3f((Vector3fc)vector3f).add((Vector3fc)vector3f4).add((Vector3fc)vector3f5);
        Vector3f vector3f9 = new Vector3f((Vector3fc)vector3f).add((Vector3fc)vector3f4).sub((Vector3fc)vector3f5);
        int n2 = n << 24 | 0xFFFFFF;
        bufferBuilder.addVertex(pose, vector3f6.x, vector3f6.y, vector3f6.z).setColor(n2).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        bufferBuilder.addVertex(pose, vector3f7.x, vector3f7.y, vector3f7.z).setColor(n2).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        bufferBuilder.addVertex(pose, vector3f8.x, vector3f8.y, vector3f8.z).setColor(n2).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        bufferBuilder.addVertex(pose, vector3f9.x, vector3f9.y, vector3f9.z).setColor(n2).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    static {
        NO_DEPTH_MEME_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getEntitySnippet()}).withLocation("lightning/no_depth_meme").withShaderDefine("ALPHA_CUTOUT", 0.1f).withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build();
    }

    private static final class MemeInstance {
        final Vector3f pos = new Vector3f();
        float age;
        float size;

        private MemeInstance() {
        }
    }
}

