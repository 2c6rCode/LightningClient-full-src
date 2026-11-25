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
                MeshData toClose = meshData;
                try {
                    IndexType indexType;
                    GpuBuffer indexBuffer;
                    GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning immediate vertices", 32, meshData.vertexBuffer());
                    GpuBuffer allocatedIndexBuffer = null;
                    if (meshData.indexBuffer() == null) {
                        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer((PrimitiveTopology)meshData.drawState().primitiveTopology());
                        indexBuffer = autoStorageIndexBuffer.getBuffer(meshData.drawState().indexCount());
                        indexType = autoStorageIndexBuffer.type();
                    } else {
                        indexBuffer = allocatedIndexBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning immediate indices", 64, meshData.indexBuffer());
                        indexType = meshData.drawState().indexType();
                    }
                    try {
                        renderType.prepare().drawFromBuffer(vertexBuffer, indexBuffer, indexType, 0, 0, meshData.drawState().indexCount());
                    }
                    catch (Exception exception) {
                    }
                    finally {
                        vertexBuffer.close();
                        if (allocatedIndexBuffer != null) {
                            allocatedIndexBuffer.close();
                        }
                    }
                }
                catch (Throwable throwable) {
                    if (meshData != null) {
                        try {
                            toClose.close();
                        }
                        catch (Throwable suppressed) {
                            throwable.addSuppressed(suppressed);
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

    public static void renderBox(PoseStack poseStack, AABB box, Color color) {
        Renderer3D.renderGradientBox(poseStack, box, color, color);
    }

    public static void renderGradientBox(PoseStack poseStack, AABB box, Color topColor, Color bottomColor) {
        Renderer3D.renderGradientBox(QUADS, poseStack, box, topColor, bottomColor);
    }

    public static void renderGradientBox(List<VertexCollection> quads, PoseStack poseStack, AABB box, Color topColor, Color bottomColor) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(box)) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        box = Renderer3D.cameraTransform(box);
        quads.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ, topColor.getRGB())));
        quads.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ, topColor.getRGB())));
        quads.add(new VertexCollection(new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ, topColor.getRGB())));
        quads.add(new VertexCollection(new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ, topColor.getRGB())));
        quads.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ, topColor.getRGB())));
        quads.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB())));
    }

    public static void renderBoxOutline(PoseStack poseStack, AABB box, Color color) {
        Renderer3D.renderGradientBoxOutline(poseStack, box, color, color);
    }

    public static void renderGradientBoxOutline(PoseStack poseStack, AABB box, Color topColor, Color bottomColor) {
        Renderer3D.renderGradientBoxOutline(DEBUG_LINES, poseStack, box, topColor, bottomColor);
    }

    public static void renderGradientBoxOutline(List<VertexCollection> lines, PoseStack poseStack, AABB box, Color topColor, Color bottomColor) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(box)) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        box = Renderer3D.cameraTransform(box);
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB())));
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ, bottomColor.getRGB())));
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ, topColor.getRGB())));
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ, topColor.getRGB())));
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ, topColor.getRGB())));
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ, topColor.getRGB())));
    }

    public static void renderTargetCircle(PoseStack poseStack, Entity entity, float tickDelta, Color color) {
        Renderer3D.renderTargetCircle(SHINE_QUADS, SHINE_DEBUG_LINES, poseStack, entity, tickDelta, color);
    }

    public static void renderTargetCircle(List<VertexCollection> quads, List<VertexCollection> lines, PoseStack poseStack, Entity entity, float tickDelta, Color color) {
        if (!RENDERING || entity == null || Renderer3D.mc.player == null) {
            return;
        }
        Vec3 entityPos = EntityUtils.getRenderPos(entity, tickDelta);
        double height = Math.max(0.5, entity.getBoundingBox().getYsize());
        double radius = Math.max(0.4, (double)entity.getBbWidth() / 2.0 + 0.25);
        AABB bounds = new AABB(entityPos.x - radius, entityPos.y - 0.5, entityPos.z - radius, entityPos.x + radius, entityPos.y + height + 0.5, entityPos.z + radius);
        if (!Renderer3D.isFrustumVisible(bounds)) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        double phase = (double)System.currentTimeMillis() / 420.0;
        double sinPhase = Math.sin(phase);
        double cosPhase = Math.cos(phase);
        double normalizedY = (sinPhase + 1.0) / 2.0;
        double ringY = entityPos.y + normalizedY * height;
        double trailOffset = -cosPhase * 0.225;
        double trailY = ringY + trailOffset;
        float alphaFactor = (float)color.getAlpha() / 255.0f;
        if (alphaFactor <= 0.001f) {
            return;
        }
        int topAlpha = (int)(180.0f * alphaFactor);
        Color topColor = ColorUtils.getColor(color, topAlpha);
        Color fadeColor = ColorUtils.getColor(color, 0);
        Color lineColor = ColorUtils.getColor(color, (int)(255.0f * alphaFactor));
        int segments = 48;
        for (int i = 0; i < segments; ++i) {
            double angle1 = (double)i * 2.0 * Math.PI / (double)segments;
            double angle2 = (double)(i + 1) * 2.0 * Math.PI / (double)segments;
            double x1 = entityPos.x + Math.cos(angle1) * radius;
            double z1 = entityPos.z + Math.sin(angle1) * radius;
            double x2 = entityPos.x + Math.cos(angle2) * radius;
            double z2 = entityPos.z + Math.sin(angle2) * radius;
            Vec3 p1 = Renderer3D.cameraTransform(new Vec3(x1, ringY, z1));
            Vec3 p2 = Renderer3D.cameraTransform(new Vec3(x2, ringY, z2));
            Vec3 p3 = Renderer3D.cameraTransform(new Vec3(x2, trailY, z2));
            Vec3 p4 = Renderer3D.cameraTransform(new Vec3(x1, trailY, z1));
            quads.add(new VertexCollection(new Vertex(matrix4f, (float)p1.x, (float)p1.y, (float)p1.z, topColor.getRGB()), new Vertex(matrix4f, (float)p2.x, (float)p2.y, (float)p2.z, topColor.getRGB()), new Vertex(matrix4f, (float)p3.x, (float)p3.y, (float)p3.z, fadeColor.getRGB()), new Vertex(matrix4f, (float)p4.x, (float)p4.y, (float)p4.z, fadeColor.getRGB())));
            quads.add(new VertexCollection(new Vertex(matrix4f, (float)p4.x, (float)p4.y, (float)p4.z, fadeColor.getRGB()), new Vertex(matrix4f, (float)p3.x, (float)p3.y, (float)p3.z, fadeColor.getRGB()), new Vertex(matrix4f, (float)p2.x, (float)p2.y, (float)p2.z, topColor.getRGB()), new Vertex(matrix4f, (float)p1.x, (float)p1.y, (float)p1.z, topColor.getRGB())));
            lines.add(new VertexCollection(new Vertex(matrix4f, (float)p1.x, (float)p1.y, (float)p1.z, lineColor.getRGB(), 2.0f), new Vertex(matrix4f, (float)p2.x, (float)p2.y, (float)p2.z, lineColor.getRGB(), 2.0f)));
        }
    }

    public static void renderBoxWireframe(PoseStack poseStack, AABB box, Color color) {
        Renderer3D.renderBoxWireframe(DEBUG_LINES, poseStack, box, color, color);
    }

    public static void renderBoxWireframe(PoseStack poseStack, AABB box, Color topColor, Color bottomColor) {
        Renderer3D.renderBoxWireframe(DEBUG_LINES, poseStack, box, topColor, bottomColor);
    }

    public static void renderBoxWireframe(List<VertexCollection> lines, PoseStack poseStack, AABB box, Color topColor, Color bottomColor) {
        if (!RENDERING) {
            return;
        }
        if (!Renderer3D.isFrustumVisible(box)) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        box = Renderer3D.cameraTransform(box);
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB())));
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.maxX, (float)box.minY, (float)box.minZ, bottomColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.minY, (float)box.maxZ, bottomColor.getRGB())));
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.maxZ, topColor.getRGB())));
        lines.add(new VertexCollection(new Vertex(matrix4f, (float)box.maxX, (float)box.maxY, (float)box.minZ, topColor.getRGB()), new Vertex(matrix4f, (float)box.minX, (float)box.maxY, (float)box.maxZ, topColor.getRGB())));
    }

    public static void renderQuad(PoseStack poseStack, float x1, float y1, float x2, float y2, Color color) {
        if (!RENDERING) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        int argb = color.getRGB();
        QUADS.add(new VertexCollection(new Vertex(matrix4f, x1, y1, 0.0f, argb), new Vertex(matrix4f, x1, y2, 0.0f, argb), new Vertex(matrix4f, x2, y2, 0.0f, argb), new Vertex(matrix4f, x2, y1, 0.0f, argb)));
    }

    public static void renderOutline(PoseStack poseStack, float x1, float y1, float x2, float y2, Color color) {
        if (!RENDERING) {
            return;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        int argb = color.getRGB();
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, x1, y1, 0.0f, argb), new Vertex(matrix4f, x2, y1, 0.0f, argb)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, x2, y1, 0.0f, argb), new Vertex(matrix4f, x2, y2, 0.0f, argb)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, x2, y2, 0.0f, argb), new Vertex(matrix4f, x1, y2, 0.0f, argb)));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, x1, y2, 0.0f, argb), new Vertex(matrix4f, x1, y1, 0.0f, argb)));
    }

    public static void renderLine(PoseStack poseStack, Vec3 start, Vec3 end, Color color) {
        Renderer3D.renderLine(DEBUG_LINES, poseStack, start, end, color);
    }

    public static void renderLine(List<VertexCollection> lines, PoseStack poseStack, Vec3 start, Vec3 end, Color color) {
        Renderer3D.renderLine(lines, poseStack, start, end, color, color);
    }

    public static void renderLine(PoseStack poseStack, Vec3 start, Vec3 end, Color startColor, Color endColor) {
        Renderer3D.renderLine(DEBUG_LINES, poseStack, start, end, startColor, endColor);
    }

    public static void renderLine(List<VertexCollection> lines, PoseStack poseStack, Vec3 start, Vec3 end, Color startColor, Color endColor) {
        if (RENDERING && Renderer3D.mc.gameRenderer != null && Renderer3D.mc.gameRenderer.mainCamera() != null) {
            Vec3 camPos = Renderer3D.mc.gameRenderer.mainCamera().position();
            Vec3 camDir = Vec3.directionFromRotation((float)Renderer3D.mc.gameRenderer.mainCamera().xRot(), (float)Renderer3D.mc.gameRenderer.mainCamera().yRot());
            double startDot = (start.x - camPos.x) * camDir.x + (start.y - camPos.y) * camDir.y + (start.z - camPos.z) * camDir.z;
            double endDot = (end.x - camPos.x) * camDir.x + (end.y - camPos.y) * camDir.y + (end.z - camPos.z) * camDir.z;
            double nearClip = 0.2;
            if (!(startDot < nearClip) || !(endDot < nearClip)) {
                if (startDot >= nearClip && endDot < nearClip) {
                    double t = (startDot - nearClip) / (startDot - endDot);
                    end = new Vec3(start.x + (end.x - start.x) * t, start.y + (end.y - start.y) * t, start.z + (end.z - start.z) * t);
                    if (!startColor.equals(endColor)) {
                        endColor = ColorUtils.getColor(endColor, (int)((double)endColor.getAlpha() * (1.0 - t) + (double)startColor.getAlpha() * t));
                    }
                } else if (startDot < nearClip && endDot >= nearClip) {
                    double t = (nearClip - startDot) / (endDot - startDot);
                    start = new Vec3(start.x + (end.x - start.x) * t, start.y + (end.y - start.y) * t, start.z + (end.z - start.z) * t);
                    if (!startColor.equals(endColor)) {
                        startColor = ColorUtils.getColor(startColor, (int)((double)startColor.getAlpha() * (1.0 - t) + (double)endColor.getAlpha() * t));
                    }
                }
                Matrix4f matrix4f = poseStack.last().pose();
                start = Renderer3D.cameraTransform(start);
                end = Renderer3D.cameraTransform(end);
                lines.add(new VertexCollection(new Vertex(matrix4f, (float)start.x, (float)start.y, (float)start.z, startColor.getRGB()), new Vertex(matrix4f, (float)end.x, (float)end.y, (float)end.z, endColor.getRGB())));
            }
        }
    }

    private static RenderType noDepthItemRenderType(BakedQuad.MaterialInfo materialInfo) {
        boolean translucent = materialInfo.layer().translucent();
        Identifier atlasLocation = materialInfo.sprite().atlasLocation();
        Map<Identifier, RenderType> map = translucent ? NO_DEPTH_ITEM_TRANSLUCENT : NO_DEPTH_ITEM_CUTOUT;
        return map.computeIfAbsent(atlasLocation, identifier -> {
            RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getItemSnippet()}).withLocation(translucent ? "lightning/no_depth_item_translucent" : "lightning/no_depth_item_cutout").withShaderDefine("ALPHA_CUTOUT", 0.1f).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false));
            if (translucent) {
                builder.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT));
            }
            return RenderType.create((String)(translucent ? "night_no_depth_item_translucent" : "night_no_depth_item_cutout"), (RenderSetup)RenderSetup.builder((RenderPipeline)builder.build()).withTexture("Sampler0", identifier).useOverlay().useLightmap().affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup());
        });
    }

    private static BufferBuilder itemBuffer(Map<RenderType, ByteBufferBuilder> buffers, Map<RenderType, BufferBuilder> builders, RenderType targetType) {
        return builders.computeIfAbsent(targetType, renderType -> {
            ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
            buffers.put((RenderType)renderType, byteBufferBuilder);
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
        HashMap<RenderType, ByteBufferBuilder> buffers = new HashMap<RenderType, ByteBufferBuilder>();
        HashMap<RenderType, BufferBuilder> builders = new HashMap<RenderType, BufferBuilder>();
        try {
            IntList intList;
            for (int i = 0; i < itemStackRenderStateAccessor.night$getActiveLayerCount(); ++i) {
                LayerRenderStateAccessor layer = (LayerRenderStateAccessor)itemStackRenderStateAccessor.night$getLayers()[i];
                poseStack.pushPose();
                layer.night$applyTransform(poseStack.last());
                intList = layer.night$getTintLayers();
                int[] tintColors = intList != null ? intList.toArray(new int[0]) : new int[]{};
                boolean hasFoil = layer.night$getFoilType() != ItemStackRenderState.FoilType.NONE;
                for (BakedQuad bakedQuad : layer.night$getQuads()) {
                    BakedQuad.MaterialInfo materialInfo = bakedQuad.materialInfo();
                    RenderType renderType = Renderer3D.noDepthItemRenderType(materialInfo);
                    int tintIndex = materialInfo.isTinted() ? materialInfo.tintIndex() : -1;
                    QUAD_INSTANCE.setColor(tintIndex >= 0 && tintIndex < tintColors.length ? tintColors[tintIndex] : -1);
                    if (hasFoil) {
                        boolean useTranslucentGlint = Renderer3D.mc.gameRenderer.gameRenderState().useShaderTransparency() && renderType.outputTarget() == OutputTarget.ITEM_ENTITY_TARGET;
                        RenderType glintType = useTranslucentGlint ? RenderTypes.glintTranslucent() : RenderTypes.glint();
                        Renderer3D.itemBuffer(buffers, builders, glintType).putBakedQuad(poseStack.last(), bakedQuad, QUAD_INSTANCE);
                    }
                    Renderer3D.itemBuffer(buffers, builders, renderType).putBakedQuad(poseStack.last(), bakedQuad, QUAD_INSTANCE);
                }
                poseStack.popPose();
            }
            for (Map.Entry entry : builders.entrySet()) {
                MeshData meshData = ((BufferBuilder)entry.getValue()).build();
                Renderer3D.draw((RenderType)entry.getKey(), meshData);
            }
        }
        finally {
            for (ByteBufferBuilder byteBufferBuilder : buffers.values()) {
                byteBufferBuilder.close();
            }
            RenderSystem.setShaderFog((GpuBufferSlice)fogRenderer.getBuffer(FogRenderer.FogMode.WORLD));
        }
    }

    public static void renderScaledText(PoseStack poseStack, String text, double x, double y, double z, int fontSize, boolean background, Color color) {
        Renderer3D.renderScaledText(poseStack, text, x, y, z, fontSize, background, false, color, false);
    }

    public static void renderScaledText(PoseStack poseStack, String text, double x, double y, double z, int fontSize, boolean background, Color color, boolean shadow) {
        Renderer3D.renderScaledText(poseStack, text, x, y, z, fontSize, background, false, color, shadow);
    }

    public static void renderCenteredScaledText(PoseStack poseStack, String text, double x, double y, double z, int fontSize, boolean background, Color color) {
        Renderer3D.renderScaledText(poseStack, text, x, y, z, fontSize, background, true, color, false);
    }

    public static void renderCenteredScaledText(PoseStack poseStack, String text, double x, double y, double z, int fontSize, boolean background, Color color, boolean shadow) {
        Renderer3D.renderScaledText(poseStack, text, x, y, z, fontSize, background, true, color, shadow);
    }

    public static void renderScaledText(PoseStack poseStack, String text, double x, double y, double z, int fontSize, boolean background, boolean centered, Color color) {
        Renderer3D.renderScaledText(poseStack, text, x, y, z, fontSize, background, centered, color, false);
    }

    public static void renderScaledText(PoseStack poseStack, String text, double x, double y, double z, int fontSize, boolean background, boolean centered, Color color, boolean shadow) {
        Vec3 camPos = Renderer3D.mc.gameRenderer.mainCamera().position();
        float distance = (float)Math.sqrt(camPos.distanceToSqr(x, y, z));
        float scale = 0.0018f + (float)fontSize / 10000.0f * Math.max(distance, 8.0f);
        Renderer3D.renderText(poseStack, text, x, y, z, scale, background, centered, color, shadow);
    }

    public static void renderText(PoseStack poseStack, String text, double x, double y, double z, float scale, boolean background, Color color) {
        Renderer3D.renderText(poseStack, text, x, y, z, scale, background, false, color, false);
    }

    public static void renderText(PoseStack poseStack, String text, double x, double y, double z, float scale, boolean background, Color color, boolean shadow) {
        Renderer3D.renderText(poseStack, text, x, y, z, scale, background, false, color, shadow);
    }

    public static void renderText(PoseStack poseStack, String text, double x, double y, double z, float scale, boolean background, boolean centered, Color color) {
        Renderer3D.renderText(poseStack, text, x, y, z, scale, background, centered, color, false);
    }

    public static void renderText(PoseStack poseStack, String text, double x, double y, double z, float scale, boolean background, boolean centered, Color color, boolean shadow) {
        Vec3 camPos = Renderer3D.mc.gameRenderer.mainCamera().position();
        Vec3 offset = new Vec3(x - camPos.x, y - camPos.y, z - camPos.z);
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose((Quaternionfc)Renderer3D.mc.gameRenderer.mainCamera().rotation());
        poseStack.scale(scale, -scale, scale);
        String[] lines = text.split("\n");
        float lineHeight = Night.FONT_MANAGER.getHeight();
        float lineStep = lineHeight + 2.0f;
        float totalHeight = (float)(lines.length - 1) * lineStep + lineHeight;
        float drawY = centered ? -totalHeight / 2.0f : (float)(-lines.length) * lineStep + 1.0f;
        if (background) {
            float maxWidth = 0.0f;
            for (String line : lines) {
                float lineWidth = Night.FONT_MANAGER.getWidth(line);
                if (!(lineWidth > maxWidth)) continue;
                maxWidth = lineWidth;
            }
            float bgLeft = -maxWidth / 2.0f - 2.0f;
            float bgTop = drawY - 2.0f;
            float bgRight = maxWidth / 2.0f + 2.0f;
            float bgBottom = drawY + totalHeight + 2.0f;
            int bgColor = new Color(0, 0, 0, 100).getRGB();
            Matrix4f matrix4f = poseStack.last().pose();
            try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
                BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, bgLeft, bgTop, 0.0f).setColor(bgColor);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, bgLeft, bgBottom, 0.0f).setColor(bgColor);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, bgRight, bgBottom, 0.0f).setColor(bgColor);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, bgRight, bgTop, 0.0f).setColor(bgColor);
                MeshData meshData = bufferBuilder.build();
                Renderer3D.draw(NO_DEPTH_QUADS, meshData);
            }
        }
        for (String line : lines) {
            if (shadow) {
                Night.FONT_MANAGER.drawText(poseStack, line, (float)(-Night.FONT_MANAGER.getWidth(line)) / 2.0f, drawY, color, true);
            } else {
                Night.FONT_MANAGER.drawTextWithShadow(poseStack, line, (float)(-Night.FONT_MANAGER.getWidth(line)) / 2.0f, drawY, color, false);
            }
            drawY += lineStep;
        }
        poseStack.popPose();
    }

    public static RenderType getNoDepthTexturedType(Identifier textureId) {
        return NO_DEPTH_TEXTURE_TYPES.computeIfAbsent(textureId, identifier -> RenderType.create((String)("night_no_depth_tex_" + identifier.getNamespace() + "_" + identifier.getPath().replace('/', '_')), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getGuiTexturedSnippet()}).withLocation("lightning/no_depth_tex").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA))).build()).withTexture("Sampler0", identifier).sortOnUpload().createRenderSetup()));
    }

    public static void renderTexture(PoseStack poseStack, Identifier identifier, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix4f = poseStack.last().pose();
        RenderType renderType = Renderer3D.getNoDepthTexturedType(identifier);
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x1, y2, 0.0f).setUv(0.0f, 1.0f).setColor(color);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x2, y2, 0.0f).setUv(1.0f, 1.0f).setColor(color);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x2, y1, 0.0f).setUv(1.0f, 0.0f).setColor(color);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x1, y1, 0.0f).setUv(0.0f, 0.0f).setColor(color);
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
    public static void draw(List<VertexCollection> quads, List<VertexCollection> lines, boolean shine) {
        MeshData meshData;
        BufferBuilder bufferBuilder;
        ByteBufferBuilder byteBufferBuilder;
        int count;
        if (!quads.isEmpty()) {
            count = Renderer3D.vertexCount(quads);
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(count * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
            try {
                bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                for (VertexCollection vertexCollection : quads) {
                    vertexCollection.quad(bufferBuilder);
                }
                meshData = bufferBuilder.build();
                Renderer3D.draw(shine ? NO_DEPTH_QUADS_SHINE : NO_DEPTH_QUADS, meshData);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        if (!lines.isEmpty()) {
            count = Renderer3D.vertexCount(lines) * 2;
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(count * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize()));
            try {
                bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
                Renderer3D.buildLines(bufferBuilder, lines);
                meshData = bufferBuilder.build();
                Renderer3D.draw(shine ? NO_DEPTH_LINES_SHINE : NO_DEPTH_LINES, meshData);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        RenderSystem.getDevice();
        quads.clear();
        lines.clear();
    }

    static int vertexCount(List<VertexCollection> collections) {
        int count = 0;
        for (VertexCollection vertexCollection : collections) {
            count += vertexCollection.vertices().length;
        }
        return count;
    }

    public static void buildLines(BufferBuilder bufferBuilder, List<VertexCollection> lines) {
        for (VertexCollection vertexCollection : lines) {
            Vertex[] vertices = vertexCollection.vertices();
            int idx = 0;
            while (idx + 1 < vertices.length) {
                Vertex v1 = vertices[idx];
                Vertex v2 = vertices[idx + 1];
                float dx = v2.x - v1.x;
                float dy = v2.y - v1.y;
                float dz = v2.z - v1.z;
                float lenSq = dx * dx + dy * dy + dz * dz;
                float normalScale = 0.05f;
                if (lenSq > 1.0E-6f) {
                    float len = (float)Math.sqrt(lenSq);
                    float invLen = 1.0f / len;
                    dx *= invLen;
                    dy *= invLen;
                    dz *= invLen;
                    normalScale = Math.min(0.05f, len * 0.45f);
                } else {
                    dx = 0.0f;
                    dy = 1.0f;
                    dz = 0.0f;
                }
                bufferBuilder.addVertex((Matrix4fc)v1.matrix, v1.x, v1.y, v1.z).setColor(v1.color).setNormal(dx * normalScale, dy * normalScale, dz * normalScale).setLineWidth(v1.lineWidth);
                bufferBuilder.addVertex((Matrix4fc)v2.matrix, v2.x, v2.y, v2.z).setColor(v2.color).setNormal(-dx * normalScale, -dy * normalScale, -dz * normalScale).setLineWidth(v2.lineWidth);
                idx += 2;
            }
        }
    }

    public static boolean isFrustumVisible(AABB box) {
        return Renderer3D.mc.gameRenderer.mainCamera().getCullFrustum().isVisible(box);
    }

    private static Vec3 cameraTransform(Vec3 pos) {
        Vec3 camPos = Renderer3D.mc.gameRenderer.mainCamera().position();
        return new Vec3(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);
    }

    private static AABB cameraTransform(AABB box) {
        Vec3 camPos = Renderer3D.mc.gameRenderer.mainCamera().position();
        return new AABB(box.minX - camPos.x, box.minY - camPos.y, box.minZ - camPos.z, box.maxX - camPos.x, box.maxY - camPos.y, box.maxZ - camPos.z);
    }

    public record VertexCollection(Vertex... vertices) {
        public void quad(BufferBuilder bufferBuilder) {
            for (Vertex vertex : this.vertices) {
                bufferBuilder.addVertex((Matrix4fc)vertex.matrix, vertex.x, vertex.y, vertex.z).setColor(vertex.color);
            }
        }
    }

    public record Vertex(Matrix4f matrix, float x, float y, float z, int color, float lineWidth) {
        public Vertex(Matrix4f matrix, float x, float y, float z, int color) {
            this(matrix, x, y, z, color, 1.75f);
        }
    }
}

