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

    private static RenderType buildLayer(String name, Identifier identifier) {
        return RenderType.create((String)("night_" + name), (RenderSetup)RenderSetup.builder((RenderPipeline)NO_DEPTH_MEME_PIPELINE).withTexture("Sampler0", identifier).useLightmap().useOverlay().affectsCrumbling().sortOnUpload().setOutline(RenderSetup.OutlineProperty.NONE).createRenderSetup());
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

    public void spawn(Vector3f position, float size) {
        MemeInstance memeInstance = new MemeInstance();
        memeInstance.pos.set((Vector3fc)position);
        memeInstance.size = size;
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
        long now = System.nanoTime();
        float deltaSeconds = this.lastNanos == 0L ? 0.0f : Math.min((float)(now - this.lastNanos) / 1.0E9f, 0.1f);
        this.lastNanos = now;
        float totalDuration = 0.90000004f;
        Iterator<MemeInstance> iterator = this.instances.iterator();
        while (iterator.hasNext()) {
            MemeInstance instance = iterator.next();
            instance.age += deltaSeconds;
            if (!(instance.age >= totalDuration)) continue;
            iterator.remove();
        }
        if (this.instances.isEmpty()) {
            return;
        }
        PoseStack.Pose pose = poseStack.last();
        Camera camera = KillEffectMemeRenderer.mc.gameRenderer.mainCamera();
        Vector3f up = new Vector3f(camera.upVector());
        Vector3f left = new Vector3f(camera.leftVector());
        Vec3 camPosVec = camera.position();
        Vector3f camPos = new Vector3f((float)camPosVec.x, (float)camPosVec.y, (float)camPosVec.z);
        Vector3f relativePos = new Vector3f();
        RenderType circleLayer = KillEffectMemeRenderer.getCircleLayer();
        try (ByteBufferBuilder circleByteBuffer = new ByteBufferBuilder(1024)) {
            BufferBuilder circleBufferBuilder = new BufferBuilder(circleByteBuffer, circleLayer.primitiveTopology(), circleLayer.format());
            for (MemeInstance instance : this.instances) {
                if (instance.age % 0.3f >= 0.18f) continue;
                relativePos.set((Vector3fc)instance.pos).sub((Vector3fc)camPos);
                KillEffectMemeRenderer.emitBillboard(circleBufferBuilder, pose, relativePos, up, left, instance.size, 255);
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
                relativePos.set((Vector3fc)instance.pos).sub((Vector3fc)camPos);
                arrowPos.set((Vector3fc)left).mul(-instance.size * 1.1f).add(up.x * instance.size * 1.1f, up.y * instance.size * 1.1f, up.z * instance.size * 1.1f).add((Vector3fc)relativePos);
                KillEffectMemeRenderer.emitBillboard(arrowBufferBuilder, pose, arrowPos, up, left, instance.size * 0.8f, 255);
            }
            MeshData meshData2 = arrowBufferBuilder.build();
            if (meshData2 != null) {
                Renderer3D.draw(arrowLayer, meshData2);
            }
        }
    }

    private static void emitBillboard(BufferBuilder bufferBuilder, PoseStack.Pose pose, Vector3f center, Vector3f up, Vector3f left, float size, int alpha) {
        Vector3f halfUp = new Vector3f((Vector3fc)up).mul(size);
        Vector3f halfLeft = new Vector3f((Vector3fc)left).mul(size);
        Vector3f bottomLeft = new Vector3f((Vector3fc)center).sub((Vector3fc)halfUp).sub((Vector3fc)halfLeft);
        Vector3f bottomRight = new Vector3f((Vector3fc)center).sub((Vector3fc)halfUp).add((Vector3fc)halfLeft);
        Vector3f topRight = new Vector3f((Vector3fc)center).add((Vector3fc)halfUp).add((Vector3fc)halfLeft);
        Vector3f topLeft = new Vector3f((Vector3fc)center).add((Vector3fc)halfUp).sub((Vector3fc)halfLeft);
        int color = alpha << 24 | 0xFFFFFF;
        bufferBuilder.addVertex(pose, bottomLeft.x, bottomLeft.y, bottomLeft.z).setColor(color).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        bufferBuilder.addVertex(pose, bottomRight.x, bottomRight.y, bottomRight.z).setColor(color).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        bufferBuilder.addVertex(pose, topRight.x, topRight.y, topRight.z).setColor(color).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
        bufferBuilder.addVertex(pose, topLeft.x, topLeft.y, topLeft.z).setColor(color).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
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

