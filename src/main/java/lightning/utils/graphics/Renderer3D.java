/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.IndexType
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.BlendFactor
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.QuadInstance
 *  it.unimi.dsi.fastutil.ints.IntList
 *  lightning.mixins.accessors.GameRendererAccessor
 *  lightning.mixins.accessors.ItemStackRenderStateAccessor
 *  lightning.mixins.accessors.LayerRenderStateAccessor
 *  lightning.mixins.accessors.RenderPipelinesAccessor
 *  net.minecraft.client.renderer.fog.FogRenderer
 *  net.minecraft.client.renderer.fog.FogRenderer$FogMode
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.item.ItemStackRenderState$FoilType
 *  net.minecraft.client.renderer.rendertype.OutputTarget
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderSetup$OutlineProperty
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.geometry.BakedQuad
 *  net.minecraft.client.resources.model.geometry.BakedQuad$MaterialInfo
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ItemOwner
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import it.unimi.dsi.fastutil.ints.IntList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lightning.Night;
import lightning.mixins.accessors.GameRendererAccessor;
import lightning.mixins.accessors.ItemStackRenderStateAccessor;
import lightning.mixins.accessors.LayerRenderStateAccessor;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.utils.IMinecraft;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Glint;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

public class Renderer3D
implements IMinecraft {
    public static boolean RENDERING = false;
    private static final RenderType NO_DEPTH_QUADS = RenderType.create((String)"night_no_depth_quads", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()}).withLocation("lightning/no_depth_quads").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build()).sortOnUpload().createRenderSetup());
    private static final RenderType NO_DEPTH_LINES = RenderType.create((String)"night_no_depth_lines", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getLinesSnippet()}).withLocation("lightning/no_depth_lines").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).createRenderSetup());
    private static final BlendFunction SHINE_BLEND = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_CONSTANT_ALPHA);
    private static final RenderType NO_DEPTH_QUADS_SHINE = RenderType.create((String)"night_no_depth_quads_shine", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()}).withLocation("lightning/no_depth_quads_shine").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(SHINE_BLEND)).build()).sortOnUpload().createRenderSetup());
    private static final RenderType NO_DEPTH_LINES_SHINE = RenderType.create((String)"night_no_depth_lines_shine", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getLinesSnippet()}).withLocation("lightning/no_depth_lines_shine").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(SHINE_BLEND)).build()).createRenderSetup());
    public static List<VertexCollection> QUADS = new ArrayList<VertexCollection>();
    public static List<VertexCollection> DEBUG_LINES = new ArrayList<VertexCollection>();
    public static List<VertexCollection> SHINE_QUADS = new ArrayList<VertexCollection>();
    public static List<VertexCollection> SHINE_DEBUG_LINES = new ArrayList<VertexCollection>();
    public static List<VertexCollection> SHADER_QUADS = new ArrayList<VertexCollection>();
    public static List<VertexCollection> SHADER_DEBUG_LINES = new ArrayList<VertexCollection>();
    public static List<VertexCollection> TRACER_DEBUG_LINES = new ArrayList<VertexCollection>();
    private static final QuadInstance QUAD_INSTANCE = new QuadInstance();
    private static final ItemStackRenderState ITEM_RENDER_STATE = new ItemStackRenderState();
    private static final Map<Identifier, RenderType> NO_DEPTH_ITEM_CUTOUT = new HashMap<Identifier, RenderType>();
    private static final Map<Identifier, RenderType> NO_DEPTH_ITEM_TRANSLUCENT = new HashMap<Identifier, RenderType>();
    private static final Map<Identifier, RenderType> NO_DEPTH_TEXTURE_TYPES = new HashMap<Identifier, RenderType>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void draw(RenderType renderType, MeshData meshData) {
        if (meshData != null) {
            if (meshData.drawState().indexCount() == 0) {
                meshData.close();
            } else {
                MeshData meshData2 = meshData;
                try {
                    IndexType indexType;
                    GpuBuffer gpuBuffer;
                    GpuBuffer gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "Lightning immediate vertices", 32, meshData.vertexBuffer());
                    GpuBuffer gpuBuffer3 = null;
                    if (meshData.indexBuffer() == null) {
                        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer((PrimitiveTopology)meshData.drawState().primitiveTopology());
                        gpuBuffer = autoStorageIndexBuffer.getBuffer(meshData.drawState().indexCount());
                        indexType = autoStorageIndexBuffer.type();
                    } else {
                        gpuBuffer = gpuBuffer3 = RenderSystem.getDevice().createBuffer(() -> "Lightning immediate indices", 64, meshData.indexBuffer());
                        indexType = meshData.drawState().indexType();
                    }
                    try {
                        renderType.prepare().drawFromBuffer(gpuBuffer2, gpuBuffer, indexType, 0, 0, meshData.drawState().indexCount());
                    }
                    catch (Exception exception) {
                    }
                    finally {
                        gpuBuffer2.close();
                        if (gpuBuffer3 != null) {
                            gpuBuffer3.close();
                        }
                    }
                }
                catch (Throwable throwable) {
                    if (meshData != null) {
                        try {
                            meshData2.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                if (meshData != null) {
                    meshData.close();
                }
            }
        }
    }

    public static void renderBox(PoseStack poseStack, AABB aABB, Color color) {
        Renderer3D.renderGradientBox(poseStack, aABB, color, color);
    }

    public static void renderGradientBox(PoseStack poseStack, AABB aABB, Color color, Color color2) {
        Renderer3D.renderGradientBox(QUADS, poseStack, aABB, color, color2);
    }

    public static void renderGradientBox(List<VertexCollection> list, PoseStack poseStack, AABB aABB, Color color, Color color2) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(aABB)) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        aABB = Renderer3D.cameraTransform(aABB);
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB())));
    }

    public static void renderBoxOutline(PoseStack poseStack, AABB aABB, Color color) {
        Renderer3D.renderGradientBoxOutline(poseStack, aABB, color, color);
    }

    public static void renderGradientBoxOutline(PoseStack poseStack, AABB aABB, Color color, Color color2) {
        Renderer3D.renderGradientBoxOutline(DEBUG_LINES, poseStack, aABB, color, color2);
    }

    public static void renderGradientBoxOutline(List<VertexCollection> list, PoseStack poseStack, AABB aABB, Color color, Color color2) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(aABB)) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        aABB = Renderer3D.cameraTransform(aABB);
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB())));
    }

    public static void renderTargetCircle(PoseStack poseStack, Entity entity, float f, Color color) {
        Renderer3D.renderTargetCircle(SHINE_QUADS, SHINE_DEBUG_LINES, poseStack, entity, f, color);
    }

    public static void renderTargetCircle(List<VertexCollection> list, List<VertexCollection> list2, PoseStack poseStack, Entity entity, float f, Color color) {
        if (!RENDERING || entity == null || Renderer3D.mc.player == null) {
            return;
        }
        Vec3 vec3 = EntityUtils.getRenderPos(entity, f);
        double d = Math.max(0.5, entity.getBoundingBox().getYsize());
        double d2 = Math.max(0.4, (double)entity.getBbWidth() / 2.0 + 0.25);
        AABB aABB = new AABB(vec3.x - d2, vec3.y - 0.5, vec3.z - d2, vec3.x + d2, vec3.y + d + 0.5, vec3.z + d2);
        if (!Renderer3D.isFrustumVisible(aABB)) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        double d3 = (double)System.currentTimeMillis() / 420.0;
        double d4 = Math.sin(d3);
        double d5 = Math.cos(d3);
        double d6 = (d4 + 1.0) / 2.0;
        double d7 = vec3.y + d6 * d;
        double d8 = -d5 * 0.225;
        double d9 = d7 + d8;
        float f2 = (float)color.getAlpha() / 255.0f;
        if (f2 <= 0.001f) {
            return;
        }
        int n = (int)(180.0f * f2);
        Color color2 = ColorUtils.getColor(color, n);
        Color color3 = ColorUtils.getColor(color, 0);
        Color color4 = ColorUtils.getColor(color, (int)(255.0f * f2));
        int n2 = 48;
        for (int i = 0; i < n2; ++i) {
            double d10 = (double)i * 2.0 * Math.PI / (double)n2;
            double d11 = (double)(i + 1) * 2.0 * Math.PI / (double)n2;
            double d12 = vec3.x + Math.cos(d10) * d2;
            double d13 = vec3.z + Math.sin(d10) * d2;
            double d14 = vec3.x + Math.cos(d11) * d2;
            double d15 = vec3.z + Math.sin(d11) * d2;
            Vec3 vec32 = Renderer3D.cameraTransform(new Vec3(d12, d7, d13));
            Vec3 vec33 = Renderer3D.cameraTransform(new Vec3(d14, d7, d15));
            Vec3 vec34 = Renderer3D.cameraTransform(new Vec3(d14, d9, d15));
            Vec3 vec35 = Renderer3D.cameraTransform(new Vec3(d12, d9, d13));
            list.add(new VertexCollection(new Vertex(matrix4f, (float)vec32.x, (float)vec32.y, (float)vec32.z, color2.getRGB()), new Vertex(matrix4f, (float)vec33.x, (float)vec33.y, (float)vec33.z, color2.getRGB()), new Vertex(matrix4f, (float)vec34.x, (float)vec34.y, (float)vec34.z, color3.getRGB()), new Vertex(matrix4f, (float)vec35.x, (float)vec35.y, (float)vec35.z, color3.getRGB())));
            list.add(new VertexCollection(new Vertex(matrix4f, (float)vec35.x, (float)vec35.y, (float)vec35.z, color3.getRGB()), new Vertex(matrix4f, (float)vec34.x, (float)vec34.y, (float)vec34.z, color3.getRGB()), new Vertex(matrix4f, (float)vec33.x, (float)vec33.y, (float)vec33.z, color2.getRGB()), new Vertex(matrix4f, (float)vec32.x, (float)vec32.y, (float)vec32.z, color2.getRGB())));
            list2.add(new VertexCollection(new Vertex(matrix4f, (float)vec32.x, (float)vec32.y, (float)vec32.z, color4.getRGB(), 2.0f), new Vertex(matrix4f, (float)vec33.x, (float)vec33.y, (float)vec33.z, color4.getRGB(), 2.0f)));
        }
    }

    public static void renderBoxWireframe(PoseStack poseStack, AABB aABB, Color color) {
        Renderer3D.renderBoxWireframe(DEBUG_LINES, poseStack, aABB, color, color);
    }

    public static void renderBoxWireframe(PoseStack poseStack, AABB aABB, Color color, Color color2) {
        Renderer3D.renderBoxWireframe(DEBUG_LINES, poseStack, aABB, color, color2);
    }

    public static void renderBoxWireframe(List<VertexCollection> list, PoseStack poseStack, AABB aABB, Color color, Color color2) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(aABB)) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        aABB = Renderer3D.cameraTransform(aABB);
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.minY, (float)aABB.minZ, color2.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.minY, (float)aABB.maxZ, color2.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB())));
        list.add(new VertexCollection(new Vertex(matrix4f, (float)aABB.maxX, (float)aABB.maxY, (float)aABB.minZ, color.getRGB()), new Vertex(matrix4f, (float)aABB.minX, (float)aABB.maxY, (float)aABB.maxZ, color.getRGB())));
    }

    public static void renderQuad(PoseStack poseStack, float f, float f2, float f3, float f4, Color color) {
        if (!RENDERING) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        int n = color.getRGB();
        QUADS.add(new VertexCollection(new Vertex(matrix4f, f, f2, 0.0f, n), new Vertex(matrix4f, f, f4, 0.0f, n), new Vertex(matrix4f, f3, f4, 0.0f, n), new Vertex(matrix4f, f3, f2, 0.0f, n)));
    }

    public static void renderOutline(PoseStack poseStack, float f, float f2, float f3, float f4, Color color) {
        if (!RENDERING) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        int n = color.getRGB();
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, f, f2, 0.0f, n), new Vertex(matrix4f, f3, f2, 0.0f, n)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, f3, f2, 0.0f, n), new Vertex(matrix4f, f3, f4, 0.0f, n)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, f3, f4, 0.0f, n), new Vertex(matrix4f, f, f4, 0.0f, n)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, f, f4, 0.0f, n), new Vertex(matrix4f, f, f2, 0.0f, n)));
    }

    public static void renderLine(PoseStack poseStack, Vec3 vec3, Vec3 vec32, Color color) {
        Renderer3D.renderLine(DEBUG_LINES, poseStack, vec3, vec32, color);
    }

    public static void renderLine(List<VertexCollection> list, PoseStack poseStack, Vec3 vec3, Vec3 vec32, Color color) {
        Renderer3D.renderLine(list, poseStack, vec3, vec32, color, color);
    }

    public static void renderLine(PoseStack poseStack, Vec3 vec3, Vec3 vec32, Color color, Color color2) {
        Renderer3D.renderLine(DEBUG_LINES, poseStack, vec3, vec32, color, color2);
    }

    public static void renderLine(List<VertexCollection> list, PoseStack poseStack, Vec3 vec3, Vec3 vec32, Color color, Color color2) {
        if (RENDERING && Renderer3D.mc.gameRenderer != null && Renderer3D.mc.gameRenderer.mainCamera() != null) {
            Vec3 vec33 = Renderer3D.mc.gameRenderer.mainCamera().position();
            Vec3 vec34 = Vec3.directionFromRotation((float)Renderer3D.mc.gameRenderer.mainCamera().xRot(), (float)Renderer3D.mc.gameRenderer.mainCamera().yRot());
            double d = (vec3.x - vec33.x) * vec34.x + (vec3.y - vec33.y) * vec34.y + (vec3.z - vec33.z) * vec34.z;
            double d2 = (vec32.x - vec33.x) * vec34.x + (vec32.y - vec33.y) * vec34.y + (vec32.z - vec33.z) * vec34.z;
            double d3 = 0.2;
            if (!(d < d3) || !(d2 < d3)) {
                if (d >= d3 && d2 < d3) {
                    double var14_11 = (d - d3) / (d - d2);
                    vec32 = new Vec3(vec3.x + (vec32.x - vec3.x) * var14_11, vec3.y + (vec32.y - vec3.y) * var14_11, vec3.z + (vec32.z - vec3.z) * var14_11);
                    if (!color.equals(color2)) {
                        color2 = ColorUtils.getColor(color2, (int)((double)color2.getAlpha() * (1.0 - var14_11) + (double)color.getAlpha() * var14_11));
                    }
                } else if (d < d3 && d2 >= d3) {
                    double var14_11 = (d3 - d) / (d2 - d);
                    vec3 = new Vec3(vec3.x + (vec32.x - vec3.x) * var14_11, vec3.y + (vec32.y - vec3.y) * var14_11, vec3.z + (vec32.z - vec3.z) * var14_11);
                    if (!color.equals(color2)) {
                        color = ColorUtils.getColor(color, (int)((double)color.getAlpha() * (1.0 - var14_11) + (double)color2.getAlpha() * var14_11));
                    }
                }
                Matrix4f matrix4f = poseStack.last().pose();
                vec3 = Renderer3D.cameraTransform(vec3);
                vec32 = Renderer3D.cameraTransform(vec32);
                list.add(new VertexCollection(new Vertex(matrix4f, (float)vec3.x, (float)vec3.y, (float)vec3.z, color.getRGB()), new Vertex(matrix4f, (float)vec32.x, (float)vec32.y, (float)vec32.z, color2.getRGB())));
            }
        }
    }

    private static RenderType noDepthItemRenderType(BakedQuad.MaterialInfo materialInfo) {
        boolean bl = materialInfo.layer().translucent();
        Identifier identifier2 = materialInfo.sprite().atlasLocation();
        Map<Identifier, RenderType> map = bl ? NO_DEPTH_ITEM_TRANSLUCENT : NO_DEPTH_ITEM_CUTOUT;
        return map.computeIfAbsent(identifier2, identifier -> {
            RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getItemSnippet()}).withLocation(bl ? "lightning/no_depth_item_translucent" : "lightning/no_depth_item_cutout").withShaderDefine("ALPHA_CUTOUT", 0.1f).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false));
            if (bl) {
                builder.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT));
            }
            return RenderType.create((String)(bl ? "night_no_depth_item_translucent" : "night_no_depth_item_cutout"), (RenderSetup)RenderSetup.builder((RenderPipeline)builder.build()).withTexture("Sampler0", identifier).useOverlay().useLightmap().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup());
        });
    }

    private static BufferBuilder itemBuffer(Map<RenderType, ByteBufferBuilder> map, Map<RenderType, BufferBuilder> map2, RenderType renderType2) {
        return map2.computeIfAbsent(renderType2, renderType -> {
            ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
            map.put((RenderType)renderType, byteBufferBuilder);
            return new BufferBuilder(byteBufferBuilder, renderType.primitiveTopology(), renderType.format());
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderItem(PoseStack poseStack, ItemStack itemStack, ItemOwner itemOwner) {
        if (!RENDERING || itemStack.isEmpty()) {
            return;
        }
        ITEM_RENDER_STATE.clear();
        mc.getItemModelResolver().updateForTopItem(ITEM_RENDER_STATE, itemStack, ItemDisplayContext.GUI, (Level)Renderer3D.mc.level, itemOwner, 0);
        ItemStackRenderStateAccessor itemStackRenderStateAccessor = (ItemStackRenderStateAccessor)ITEM_RENDER_STATE;
        QUAD_INSTANCE.setLightCoords(0xF000F0);
        QUAD_INSTANCE.setOverlayCoords(OverlayTexture.NO_OVERLAY);
        FogRenderer fogRenderer = ((GameRendererAccessor)Renderer3D.mc.gameRenderer).getFogRenderer();
        RenderSystem.setShaderFog((GpuBufferSlice)fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        HashMap<RenderType, ByteBufferBuilder> hashMap = new HashMap<RenderType, ByteBufferBuilder>();
        HashMap<RenderType, BufferBuilder> hashMap2 = new HashMap<RenderType, BufferBuilder>();
        try {
            IntList intList;
            for (int i = 0; i < itemStackRenderStateAccessor.night$getActiveLayerCount(); ++i) {
                LayerRenderStateAccessor object = (LayerRenderStateAccessor)itemStackRenderStateAccessor.night$getLayers()[i];
                poseStack.pushPose();
                object.night$applyTransform(poseStack.last());
                intList = object.night$getTintLayers();
                int[] nArray = intList != null ? intList.toArray(new int[0]) : new int[]{};
                boolean bl = object.night$getFoilType() != ItemStackRenderState.FoilType.NONE;
                for (BakedQuad bakedQuad : object.night$getQuads()) {
                    BakedQuad.MaterialInfo materialInfo = bakedQuad.materialInfo();
                    RenderType renderType = Renderer3D.noDepthItemRenderType(materialInfo);
                    int n = materialInfo.isTinted() ? materialInfo.tintIndex() : -1;
                    QUAD_INSTANCE.setColor(n >= 0 && n < nArray.length ? nArray[n] : -1);
                    if (bl) {
                        boolean bl2 = Renderer3D.mc.gameRenderer.gameRenderState().useShaderTransparency() && renderType.outputTarget() == OutputTarget.ITEM_ENTITY_TARGET;
                        RenderType renderType2 = bl2 ? RenderTypes.glintTranslucent() : RenderTypes.glint();
                        Renderer3D.itemBuffer(hashMap, hashMap2, renderType2).putBakedQuad(poseStack.last(), bakedQuad, QUAD_INSTANCE);
                    }
                    Renderer3D.itemBuffer(hashMap, hashMap2, renderType).putBakedQuad(poseStack.last(), bakedQuad, QUAD_INSTANCE);
                }
                poseStack.popPose();
            }
            for (Map.Entry entry : hashMap2.entrySet()) {
                MeshData meshData = ((BufferBuilder)entry.getValue()).build();
                Renderer3D.draw((RenderType)entry.getKey(), meshData);
            }
        }
        finally {
            for (ByteBufferBuilder byteBufferBuilder : hashMap.values()) {
                byteBufferBuilder.close();
            }
            RenderSystem.setShaderFog((GpuBufferSlice)fogRenderer.getBuffer(FogRenderer.FogMode.WORLD));
        }
    }

    public static void renderScaledText(PoseStack poseStack, String string, double d, double d2, double d3, int n, boolean bl, Color color) {
        Renderer3D.renderScaledText(poseStack, string, d, d2, d3, n, bl, false, color, false);
    }

    public static void renderScaledText(PoseStack poseStack, String string, double d, double d2, double d3, int n, boolean bl, Color color, boolean bl2) {
        Renderer3D.renderScaledText(poseStack, string, d, d2, d3, n, bl, false, color, bl2);
    }

    public static void renderCenteredScaledText(PoseStack poseStack, String string, double d, double d2, double d3, int n, boolean bl, Color color) {
        Renderer3D.renderScaledText(poseStack, string, d, d2, d3, n, bl, true, color, false);
    }

    public static void renderCenteredScaledText(PoseStack poseStack, String string, double d, double d2, double d3, int n, boolean bl, Color color, boolean bl2) {
        Renderer3D.renderScaledText(poseStack, string, d, d2, d3, n, bl, true, color, bl2);
    }

    public static void renderScaledText(PoseStack poseStack, String string, double d, double d2, double d3, int n, boolean bl, boolean bl2, Color color) {
        Renderer3D.renderScaledText(poseStack, string, d, d2, d3, n, bl, bl2, color, false);
    }

    public static void renderScaledText(PoseStack poseStack, String string, double d, double d2, double d3, int n, boolean bl, boolean bl2, Color color, boolean bl3) {
        Vec3 vec3 = Renderer3D.mc.gameRenderer.mainCamera().position();
        float f = (float)Math.sqrt(vec3.distanceToSqr(d, d2, d3));
        float f2 = 0.0018f + (float)n / 10000.0f * Math.max(f, 8.0f);
        Renderer3D.renderText(poseStack, string, d, d2, d3, f2, bl, bl2, color, bl3);
    }

    public static void renderText(PoseStack poseStack, String string, double d, double d2, double d3, float f, boolean bl, Color color) {
        Renderer3D.renderText(poseStack, string, d, d2, d3, f, bl, false, color, false);
    }

    public static void renderText(PoseStack poseStack, String string, double d, double d2, double d3, float f, boolean bl, Color color, boolean bl2) {
        Renderer3D.renderText(poseStack, string, d, d2, d3, f, bl, false, color, bl2);
    }

    public static void renderText(PoseStack poseStack, String string, double d, double d2, double d3, float f, boolean bl, boolean bl2, Color color) {
        Renderer3D.renderText(poseStack, string, d, d2, d3, f, bl, bl2, color, false);
    }

    public static void renderText(PoseStack poseStack, String string, double d, double d2, double d3, float f, boolean bl, boolean bl2, Color color, boolean bl3) {
        Vec3 vec3 = Renderer3D.mc.gameRenderer.mainCamera().position();
        Vec3 vec32 = new Vec3(d - vec3.x, d2 - vec3.y, d3 - vec3.z);
        poseStack.pushPose();
        poseStack.translate(vec32.x, vec32.y, vec32.z);
        poseStack.mulPose((Quaternionfc)Renderer3D.mc.gameRenderer.mainCamera().rotation());
        poseStack.scale(f, -f, f);
        String[] stringArray = string.split("\n");
        float f2 = Night.FONT_MANAGER.getHeight();
        float f3 = f2 + 2.0f;
        float f4 = (float)(stringArray.length - 1) * f3 + f2;
        float f5 = bl2 ? -f4 / 2.0f : (float)(-stringArray.length) * f3 + 1.0f;
        float f6 = f5;
        if (bl) {
            float f7 = 0.0f;
            for (String string2 : stringArray) {
                float f8 = Night.FONT_MANAGER.getWidth(string2);
                if (!(f8 > f7)) continue;
                f7 = f8;
            }
            float f9 = -f7 / 2.0f - 2.0f;
            float f10 = f5 - 2.0f;
            float f11 = f7 / 2.0f + 2.0f;
            float f12 = f5 + f4 + 2.0f;
            int n = new Color(0, 0, 0, 100).getRGB();
            Matrix4f matrix4f = poseStack.last().pose();
            try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
                BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, f9, f10, 0.0f).setColor(n);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, f9, f12, 0.0f).setColor(n);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, f11, f12, 0.0f).setColor(n);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, f11, f10, 0.0f).setColor(n);
                MeshData meshData = bufferBuilder.build();
                Renderer3D.draw(NO_DEPTH_QUADS, meshData);
            }
        }
        for (String string3 : stringArray) {
            if (bl3) {
                Night.FONT_MANAGER.drawText(poseStack, string3, (float)(-Night.FONT_MANAGER.getWidth(string3)) / 2.0f, f5, color, true);
            } else {
                Night.FONT_MANAGER.drawTextWithShadow(poseStack, string3, (float)(-Night.FONT_MANAGER.getWidth(string3)) / 2.0f, f5, color, false);
            }
            f5 += f3;
        }
        poseStack.popPose();
    }

    public static RenderType getNoDepthTexturedType(Identifier identifier2) {
        return NO_DEPTH_TEXTURE_TYPES.computeIfAbsent(identifier2, identifier -> RenderType.create((String)("night_no_depth_tex_" + identifier.getNamespace() + "_" + identifier.getPath().replace('/', '_')), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getGuiTexturedSnippet()}).withLocation("lightning/no_depth_tex").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).withTexture("Sampler0", identifier).sortOnUpload().createRenderSetup()));
    }

    public static void renderTexture(PoseStack poseStack, Identifier identifier, float f, float f2, float f3, float f4, int n) {
        Matrix4f matrix4f = poseStack.last().pose();
        RenderType renderType = Renderer3D.getNoDepthTexturedType(identifier);
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f4, 0.0f).setUv(0.0f, 1.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f3, f4, 0.0f).setUv(1.0f, 1.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f3, f2, 0.0f).setUv(1.0f, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f2, 0.0f).setUv(0.0f, 0.0f).setColor(n);
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(renderType, meshData);
        }
    }

    public static void prepare() {
        QUADS.clear();
        DEBUG_LINES.clear();
        SHINE_QUADS.clear();
        SHINE_DEBUG_LINES.clear();
        SHADER_QUADS.clear();
        SHADER_DEBUG_LINES.clear();
        TRACER_DEBUG_LINES.clear();
        Glint.QUADS.clear();
        RENDERING = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void draw(List<VertexCollection> list, List<VertexCollection> list2, boolean bl) {
        MeshData meshData;
        BufferBuilder bufferBuilder;
        ByteBufferBuilder byteBufferBuilder;
        int n;
        if (!list.isEmpty()) {
            n = Renderer3D.vertexCount(list);
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(n * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
            try {
                bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                for (VertexCollection vertexCollection : list) {
                    vertexCollection.quad(bufferBuilder);
                }
                meshData = bufferBuilder.build();
                Renderer3D.draw(bl ? NO_DEPTH_QUADS_SHINE : NO_DEPTH_QUADS, meshData);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        if (!list2.isEmpty()) {
            n = Renderer3D.vertexCount(list2) * 2;
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(n * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize()));
            try {
                bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
                Renderer3D.buildLines(bufferBuilder, list2);
                meshData = bufferBuilder.build();
                Renderer3D.draw(bl ? NO_DEPTH_LINES_SHINE : NO_DEPTH_LINES, meshData);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        RenderSystem.getDevice();
        list.clear();
        list2.clear();
    }

    static int vertexCount(List<VertexCollection> list) {
        int n = 0;
        for (VertexCollection vertexCollection : list) {
            n += vertexCollection.vertices().length;
        }
        return n;
    }

    public static void buildLines(BufferBuilder bufferBuilder, List<VertexCollection> list) {
        for (VertexCollection vertexCollection : list) {
            Vertex[] vertexArray = vertexCollection.vertices();
            int n = 0;
            while (n + 1 < vertexArray.length) {
                Vertex vertex = vertexArray[n];
                Vertex vertex2 = vertexArray[n + 1];
                float f = vertex2.x - vertex.x;
                float f2 = vertex2.y - vertex.y;
                float f3 = vertex2.z - vertex.z;
                float f4 = f * f + f2 * f2 + f3 * f3;
                float f5 = 0.05f;
                if (f4 > 1.0E-6f) {
                    float f6 = (float)Math.sqrt(f4);
                    float f7 = 1.0f / f6;
                    f *= f7;
                    f2 *= f7;
                    f3 *= f7;
                    f5 = Math.min(0.05f, f6 * 0.45f);
                } else {
                    f = 0.0f;
                    f2 = 1.0f;
                    f3 = 0.0f;
                }
                bufferBuilder.addVertex((Matrix4fc)vertex.matrix, vertex.x, vertex.y, vertex.z).setColor(vertex.color).setNormal(f * f5, f2 * f5, f3 * f5).setLineWidth(vertex.lineWidth);
                bufferBuilder.addVertex((Matrix4fc)vertex2.matrix, vertex2.x, vertex2.y, vertex2.z).setColor(vertex2.color).setNormal(-f * f5, -f2 * f5, -f3 * f5).setLineWidth(vertex2.lineWidth);
                n += 2;
            }
        }
    }

    public static boolean isFrustumVisible(AABB aABB) {
        return Renderer3D.mc.gameRenderer.mainCamera().getCullFrustum().isVisible(aABB);
    }

    private static Vec3 cameraTransform(Vec3 vec3) {
        Vec3 vec32 = Renderer3D.mc.gameRenderer.mainCamera().position();
        return new Vec3(vec3.x - vec32.x, vec3.y - vec32.y, vec3.z - vec32.z);
    }

    private static AABB cameraTransform(AABB aABB) {
        Vec3 vec3 = Renderer3D.mc.gameRenderer.mainCamera().position();
        return new AABB(aABB.minX - vec3.x, aABB.minY - vec3.y, aABB.minZ - vec3.z, aABB.maxX - vec3.x, aABB.maxY - vec3.y, aABB.maxZ - vec3.z);
    }

    public record VertexCollection(Vertex... vertices) {
        public void quad(BufferBuilder bufferBuilder) {
            for (Vertex vertex : this.vertices) {
                bufferBuilder.addVertex((Matrix4fc)vertex.matrix, vertex.x, vertex.y, vertex.z).setColor(vertex.color);
            }
        }
    }

    public record Vertex(Matrix4f matrix, float x, float y, float z, int color, float lineWidth) {
        public Vertex(Matrix4f matrix4f, float f, float f2, float f3, int n) {
            this(matrix4f, f, f2, f3, n, 1.75f);
        }
    }
}

