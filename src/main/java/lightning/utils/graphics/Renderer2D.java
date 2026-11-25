/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.mixins.accessors.GuiGraphicsExtractorAccessor
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lightning.Night;
import lightning.mixins.accessors.GuiGraphicsExtractorAccessor;
import lightning.utils.IMinecraft;
import lightning.utils.graphics.GuiQuadRenderState;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Renderer2D
implements IMinecraft {
    public static final Matrix4f LAST_PROJECTION_MATRIX = new Matrix4f();
    public static final Matrix4f LAST_MODEL_MATRIX = new Matrix4f();
    public static final Matrix4f LAST_WORLD_MATRIX = new Matrix4f();
    private static final Map<Identifier, RenderType> TEXTURE_TYPES = new HashMap<Identifier, RenderType>();
    private static MethodHandle SCISSOR_GET;
    private static MethodHandle SCISSOR_PEEK;
    private static boolean scissorHandleInitialized;

    public static void renderQuad(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        int argb = color.getRGB();
        Renderer2D.submit(guiGraphicsExtractor, new float[]{x1, x1, x2, x2}, new float[]{y1, y2, y2, y1}, new int[]{argb, argb, argb, argb});
    }

    public static void renderGradient(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color topColor, Color bottomColor) {
        int topArgb = topColor.getRGB();
        int bottomArgb = bottomColor.getRGB();
        Renderer2D.submit(guiGraphicsExtractor, new float[]{x1, x1, x2, x2}, new float[]{y1, y2, y2, y1}, new int[]{topArgb, bottomArgb, bottomArgb, topArgb});
    }

    public static void renderSidewaysGradient(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color leftColor, Color rightColor) {
        int leftArgb = leftColor.getRGB();
        int rightArgb = rightColor.getRGB();
        Renderer2D.submit(guiGraphicsExtractor, new float[]{x1, x1, x2, x2}, new float[]{y1, y2, y2, y1}, new int[]{leftArgb, leftArgb, rightArgb, rightArgb});
    }

    public static void renderCheckerboard(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, float cellSize, Color colorA, Color colorB) {
        int rows;
        int argbA = colorA.getRGB();
        int argbB = colorB.getRGB();
        int cols = (int)Math.ceil((x2 - x1) / cellSize);
        int cellCount = cols * (rows = (int)Math.ceil((y2 - y1) / cellSize));
        if (cellCount <= 0) {
            return;
        }
        float[] xs = new float[cellCount * 4];
        float[] ys = new float[cellCount * 4];
        int[] colors = new int[cellCount * 4];
        int quadIndex = 0;
        for (int i = 0; i < rows; ++i) {
            float cellY1 = y1 + (float)i * cellSize;
            float cellY2 = Math.min(y1 + (float)(i + 1) * cellSize, y2);
            for (int j = 0; j < cols; ++j) {
                float cellX1 = x1 + (float)j * cellSize;
                float cellX2 = Math.min(x1 + (float)(j + 1) * cellSize, x2);
                int cellColor = (i + j) % 2 == 0 ? argbA : argbB;
                Renderer2D.quad(xs, ys, colors, quadIndex++, cellX1, cellY1, cellX2, cellY2, cellColor);
            }
        }
        Renderer2D.submit(guiGraphicsExtractor, xs, ys, colors);
    }

    public static void renderOutline(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, Color color) {
        int argb = color.getRGB();
        float[] xs = new float[16];
        float[] ys = new float[16];
        int[] colors = new int[16];
        Renderer2D.quad(xs, ys, colors, 0, x1, y1, x1 + 0.5f, y2, argb);
        Renderer2D.quad(xs, ys, colors, 1, x2 - 0.5f, y1, x2, y2, argb);
        Renderer2D.quad(xs, ys, colors, 2, x1, y2 - 0.5f, x2, y2, argb);
        Renderer2D.quad(xs, ys, colors, 3, x1, y1, x2, y1 + 0.5f, argb);
        Renderer2D.submit(guiGraphicsExtractor, xs, ys, colors);
    }

    public static ScreenRectangle peekScissor(GuiGraphicsExtractor guiGraphicsExtractor) {
        if (!scissorHandleInitialized) {
            scissorHandleInitialized = true;
            try {
                Field scissorField = GuiGraphicsExtractor.class.getDeclaredField("scissorStack");
                scissorField.setAccessible(true);
                SCISSOR_GET = MethodHandles.lookup().unreflectGetter(scissorField);
                Method method = scissorField.getType().getDeclaredMethod("peek", new Class[0]);
                method.setAccessible(true);
                SCISSOR_PEEK = MethodHandles.lookup().unreflect(method);
            }
            catch (Throwable throwable) {
                Night.LOGGER.debug("Could not bind scissorStack handle: {}", (Object)throwable.getMessage());
            }
        }
        if (SCISSOR_GET != null && SCISSOR_PEEK != null) {
            try {
                Object scissorStack = SCISSOR_GET.invoke(guiGraphicsExtractor);
                if (scissorStack != null) {
                    return (ScreenRectangle)SCISSOR_PEEK.invoke(scissorStack);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private static void submit(GuiGraphicsExtractor guiGraphicsExtractor, float[] xs, float[] ys, int[] colors) {
        GuiRenderState guiRenderState = ((GuiGraphicsExtractorAccessor)guiGraphicsExtractor).night$getGuiRenderState();
        guiRenderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState((Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)guiGraphicsExtractor.pose()), xs, ys, colors, Renderer2D.peekScissor(guiGraphicsExtractor)));
    }

    private static void quad(float[] xs, float[] ys, int[] colors, int quadIndex, float x1, float y1, float x2, float y2, int color) {
        int base = quadIndex * 4;
        xs[base] = x1;
        ys[base] = y1;
        xs[base + 1] = x1;
        ys[base + 1] = y2;
        xs[base + 2] = x2;
        ys[base + 2] = y2;
        xs[base + 3] = x2;
        ys[base + 3] = y1;
        colors[base + 3] = color;
        colors[base + 2] = color;
        colors[base + 1] = color;
        colors[base] = color;
    }

    public static void renderImmediateQuad(PoseStack poseStack, float x1, float y1, float x2, float y2, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int argb = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x1, y1, 0.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x1, y2, 0.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x2, y2, 0.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x2, y1, 0.0f).setColor(argb);
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(RenderTypes.debugQuads(), meshData);
        }
    }

    public static void renderCircle(PoseStack poseStack, float centerX, float centerY, float radius, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int argb = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(361 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= 360; ++i) {
                bufferBuilder.addVertex((Matrix4fc)matrix4f, (float)((double)centerX + Math.sin((double)i * Math.PI / 180.0) * (double)radius), (float)((double)centerY + Math.cos((double)i * Math.PI / 180.0) * (double)radius), 0.0f).setColor(argb);
            }
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(RenderTypes.debugTriangleFan(), meshData);
        }
    }

    public static void renderTexture(PoseStack poseStack, float x1, float y1, float x2, float y2, Identifier identifier, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int argb = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x1, y1, 0.0f).setUv(0.0f, 0.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x1, y2, 0.0f).setUv(0.0f, 1.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x2, y2, 0.0f).setUv(1.0f, 1.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, x2, y1, 0.0f).setUv(1.0f, 0.0f).setColor(argb);
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(Renderer2D.texturedType(identifier), meshData);
        }
    }

    public static void renderArrow(GuiGraphicsExtractor guiGraphicsExtractor, float tipX, float tipY, float halfWidth, float height, Color color) {
        int argb = color.getRGB();
        float notchY = tipY + height * 0.75f;
        float[] xs = new float[]{tipX, tipX - halfWidth, tipX, tipX, tipX, tipX, tipX + halfWidth, tipX};
        float[] ys = new float[]{tipY, tipY + height, notchY, tipY, tipY, notchY, tipY + height, tipY};
        int[] colors = new int[]{argb, argb, argb, argb, argb, argb, argb, argb};
        Renderer2D.submit(guiGraphicsExtractor, xs, ys, colors);
    }

    public static void renderArrowOutline(GuiGraphicsExtractor guiGraphicsExtractor, float tipX, float tipY, float halfWidth, float height, Color color) {
        int argb = color.getRGB();
        float notchY = tipY + height * 0.75f;
        float[] xs = new float[16];
        float[] ys = new float[16];
        int[] colors = new int[16];
        Renderer2D.quadLine(xs, ys, colors, 0, tipX, tipY, tipX - halfWidth, tipY + height, 0.5f, argb);
        Renderer2D.quadLine(xs, ys, colors, 1, tipX - halfWidth, tipY + height, tipX, notchY, 0.5f, argb);
        Renderer2D.quadLine(xs, ys, colors, 2, tipX, notchY, tipX + halfWidth, tipY + height, 0.5f, argb);
        Renderer2D.quadLine(xs, ys, colors, 3, tipX + halfWidth, tipY + height, tipX, tipY, 0.5f, argb);
        Renderer2D.submit(guiGraphicsExtractor, xs, ys, colors);
    }

    private static void quadLine(float[] xs, float[] ys, int[] colors, int quadIndex, float x1, float y1, float x2, float y2, float halfThickness, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length < 1.0E-4f) {
            return;
        }
        float nx = -dy / length * halfThickness;
        float ny = dx / length * halfThickness;
        int base = quadIndex * 4;
        xs[base] = x1 + nx;
        ys[base] = y1 + ny;
        xs[base + 1] = x1 - nx;
        ys[base + 1] = y1 - ny;
        xs[base + 2] = x2 - nx;
        ys[base + 2] = y2 - ny;
        xs[base + 3] = x2 + nx;
        ys[base + 3] = y2 + ny;
        colors[base + 3] = color;
        colors[base + 2] = color;
        colors[base + 1] = color;
        colors[base] = color;
    }

    public static void renderArrow(PoseStack poseStack, float tipX, float tipY, float halfWidth, float height, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int argb = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(5 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, tipX, tipY, 0.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, tipX - halfWidth, tipY + height, 0.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, tipX, tipY + height, 0.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, tipX + halfWidth, tipY + height, 0.0f).setColor(argb);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, tipX, tipY, 0.0f).setColor(argb);
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(RenderTypes.debugTriangleFan(), meshData);
        }
    }

    public static void renderArrowOutline(PoseStack poseStack, float tipX, float tipY, float halfWidth, float height, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int argb = color.getRGB();
        float[] xs = new float[]{tipX, tipX - halfWidth, tipX, tipX + halfWidth, tipX};
        float[] ys = new float[]{tipY, tipY + height, tipY + height, tipY + height, tipY};
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)((xs.length - 1) * 4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < xs.length - 1; ++i) {
                Renderer2D.segment(bufferBuilder, matrix4f, xs[i], ys[i], xs[i + 1], ys[i + 1], 0.5f, argb);
            }
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(RenderTypes.debugQuads(), meshData);
        }
    }

    private static void segment(BufferBuilder bufferBuilder, Matrix4f matrix4f, float x1, float y1, float x2, float y2, float halfThickness, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        if (length == 0.0f) {
            return;
        }
        float nx = -dy / length * halfThickness;
        float ny = dx / length * halfThickness;
        bufferBuilder.addVertex((Matrix4fc)matrix4f, x1 + nx, y1 + ny, 0.0f).setColor(color);
        bufferBuilder.addVertex((Matrix4fc)matrix4f, x1 - nx, y1 - ny, 0.0f).setColor(color);
        bufferBuilder.addVertex((Matrix4fc)matrix4f, x2 - nx, y2 - ny, 0.0f).setColor(color);
        bufferBuilder.addVertex((Matrix4fc)matrix4f, x2 + nx, y2 + ny, 0.0f).setColor(color);
    }

    private static RenderType texturedType(Identifier textureId) {
        return TEXTURE_TYPES.computeIfAbsent(textureId, identifier -> RenderType.create((String)("night_textured_" + String.valueOf(identifier)), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", identifier).createRenderSetup()));
    }

    public static Vec3 project(Vec3 worldPos) {
        Vec3 camPos = Renderer2D.mc.gameRenderer.mainCamera().position();
        int[] viewport = new int[]{0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight()};
        Vector3f vector3f = new Vector3f();
        Vector4f vector4f = new Vector4f((float)(worldPos.x - camPos.x), (float)(worldPos.y - camPos.y), (float)(worldPos.z - camPos.z), 1.0f).mul((Matrix4fc)LAST_WORLD_MATRIX);
        Matrix4f projectionMatrix = new Matrix4f((Matrix4fc)LAST_PROJECTION_MATRIX);
        Matrix4f modelMatrix = new Matrix4f((Matrix4fc)LAST_MODEL_MATRIX);
        projectionMatrix.mul((Matrix4fc)modelMatrix).project(vector4f.x(), vector4f.y(), vector4f.z(), viewport, vector3f);
        return new Vec3((double)(vector3f.x / (float)mc.getWindow().getGuiScale()), (double)(((float)mc.getWindow().getHeight() - vector3f.y) / (float)mc.getWindow().getGuiScale()), (double)vector3f.z);
    }

    static {
        scissorHandleInitialized = false;
    }
}

