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

    public static void renderQuad(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        int n = color.getRGB();
        Renderer2D.submit(guiGraphicsExtractor, new float[]{f, f, f3, f3}, new float[]{f2, f4, f4, f2}, new int[]{n, n, n, n});
    }

    public static void renderGradient(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color, Color color2) {
        int n = color.getRGB();
        int n2 = color2.getRGB();
        Renderer2D.submit(guiGraphicsExtractor, new float[]{f, f, f3, f3}, new float[]{f2, f4, f4, f2}, new int[]{n, n2, n2, n});
    }

    public static void renderSidewaysGradient(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color, Color color2) {
        int n = color.getRGB();
        int n2 = color2.getRGB();
        Renderer2D.submit(guiGraphicsExtractor, new float[]{f, f, f3, f3}, new float[]{f2, f4, f4, f2}, new int[]{n, n, n2, n2});
    }

    public static void renderCheckerboard(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, Color color, Color color2) {
        int n;
        int n2 = color.getRGB();
        int n3 = color2.getRGB();
        int n4 = (int)Math.ceil((f3 - f) / f5);
        int n5 = n4 * (n = (int)Math.ceil((f4 - f2) / f5));
        if (n5 <= 0) {
            return;
        }
        float[] fArray = new float[n5 * 4];
        float[] fArray2 = new float[n5 * 4];
        int[] nArray = new int[n5 * 4];
        int n6 = 0;
        for (int i = 0; i < n; ++i) {
            float f6 = f2 + (float)i * f5;
            float f7 = Math.min(f2 + (float)(i + 1) * f5, f4);
            for (int j = 0; j < n4; ++j) {
                float f8 = f + (float)j * f5;
                float f9 = Math.min(f + (float)(j + 1) * f5, f3);
                int n7 = (i + j) % 2 == 0 ? n2 : n3;
                Renderer2D.quad(fArray, fArray2, nArray, n6++, f8, f6, f9, f7, n7);
            }
        }
        Renderer2D.submit(guiGraphicsExtractor, fArray, fArray2, nArray);
    }

    public static void renderOutline(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        int n = color.getRGB();
        float[] fArray = new float[16];
        float[] fArray2 = new float[16];
        int[] nArray = new int[16];
        Renderer2D.quad(fArray, fArray2, nArray, 0, f, f2, f + 0.5f, f4, n);
        Renderer2D.quad(fArray, fArray2, nArray, 1, f3 - 0.5f, f2, f3, f4, n);
        Renderer2D.quad(fArray, fArray2, nArray, 2, f, f4 - 0.5f, f3, f4, n);
        Renderer2D.quad(fArray, fArray2, nArray, 3, f, f2, f3, f2 + 0.5f, n);
        Renderer2D.submit(guiGraphicsExtractor, fArray, fArray2, nArray);
    }

    public static ScreenRectangle peekScissor(GuiGraphicsExtractor guiGraphicsExtractor) {
        Object object;
        if (!scissorHandleInitialized) {
            scissorHandleInitialized = true;
            try {
                object = GuiGraphicsExtractor.class.getDeclaredField("scissorStack");
                ((Field)object).setAccessible(true);
                SCISSOR_GET = MethodHandles.lookup().unreflectGetter((Field)object);
                Method method = ((Field)object).getType().getDeclaredMethod("peek", new Class[0]);
                method.setAccessible(true);
                SCISSOR_PEEK = MethodHandles.lookup().unreflect(method);
            }
            catch (Throwable throwable) {
                Night.LOGGER.debug("Could not bind scissorStack handle: {}", (Object)throwable.getMessage());
            }
        }
        if (SCISSOR_GET != null && SCISSOR_PEEK != null) {
            try {
                object = SCISSOR_GET.invoke(guiGraphicsExtractor);
                if (object != null) {
                    return (ScreenRectangle)SCISSOR_PEEK.invoke(object);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private static void submit(GuiGraphicsExtractor guiGraphicsExtractor, float[] fArray, float[] fArray2, int[] nArray) {
        GuiRenderState guiRenderState = ((GuiGraphicsExtractorAccessor)guiGraphicsExtractor).night$getGuiRenderState();
        guiRenderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState((Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)guiGraphicsExtractor.pose()), fArray, fArray2, nArray, Renderer2D.peekScissor(guiGraphicsExtractor)));
    }

    private static void quad(float[] fArray, float[] fArray2, int[] nArray, int n, float f, float f2, float f3, float f4, int n2) {
        int n3;
        int n4 = n * 4;
        fArray[n4] = f;
        fArray2[n4] = f2;
        fArray[n4 + 1] = f;
        fArray2[n4 + 1] = f4;
        fArray[n4 + 2] = f3;
        fArray2[n4 + 2] = f4;
        fArray[n4 + 3] = f3;
        fArray2[n4 + 3] = f2;
        nArray[n4 + 3] = n3 = n2;
        nArray[n4 + 2] = n3;
        nArray[n4 + 1] = n3;
        nArray[n4] = n3;
    }

    public static void renderImmediateQuad(PoseStack poseStack, float f, float f2, float f3, float f4, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int n = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f2, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f4, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f3, f4, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f3, f2, 0.0f).setColor(n);
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(RenderTypes.debugQuads(), meshData);
        }
    }

    public static void renderCircle(PoseStack poseStack, float f, float f2, float f3, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int n = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(361 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= 360; ++i) {
                bufferBuilder.addVertex((Matrix4fc)matrix4f, (float)((double)f + Math.sin((double)i * Math.PI / 180.0) * (double)f3), (float)((double)f2 + Math.cos((double)i * Math.PI / 180.0) * (double)f3), 0.0f).setColor(n);
            }
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(RenderTypes.debugTriangleFan(), meshData);
        }
    }

    public static void renderTexture(PoseStack poseStack, float f, float f2, float f3, float f4, Identifier identifier, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int n = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f2, 0.0f).setUv(0.0f, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f4, 0.0f).setUv(0.0f, 1.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f3, f4, 0.0f).setUv(1.0f, 1.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f3, f2, 0.0f).setUv(1.0f, 0.0f).setColor(n);
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(Renderer2D.texturedType(identifier), meshData);
        }
    }

    public static void renderArrow(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        int n = color.getRGB();
        float f5 = f2 + f4 * 0.75f;
        float[] fArray = new float[]{f, f - f3, f, f, f, f, f + f3, f};
        float[] fArray2 = new float[]{f2, f2 + f4, f5, f2, f2, f5, f2 + f4, f2};
        int[] nArray = new int[]{n, n, n, n, n, n, n, n};
        Renderer2D.submit(guiGraphicsExtractor, fArray, fArray2, nArray);
    }

    public static void renderArrowOutline(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, Color color) {
        int n = color.getRGB();
        float f5 = f2 + f4 * 0.75f;
        float[] fArray = new float[16];
        float[] fArray2 = new float[16];
        int[] nArray = new int[16];
        Renderer2D.quadLine(fArray, fArray2, nArray, 0, f, f2, f - f3, f2 + f4, 0.5f, n);
        Renderer2D.quadLine(fArray, fArray2, nArray, 1, f - f3, f2 + f4, f, f5, 0.5f, n);
        Renderer2D.quadLine(fArray, fArray2, nArray, 2, f, f5, f + f3, f2 + f4, 0.5f, n);
        Renderer2D.quadLine(fArray, fArray2, nArray, 3, f + f3, f2 + f4, f, f2, 0.5f, n);
        Renderer2D.submit(guiGraphicsExtractor, fArray, fArray2, nArray);
    }

    private static void quadLine(float[] fArray, float[] fArray2, int[] nArray, int n, float f, float f2, float f3, float f4, float f5, int n2) {
        int n3;
        float f6 = f3 - f;
        float f7 = f4 - f2;
        float f8 = (float)Math.sqrt(f6 * f6 + f7 * f7);
        if (f8 < 1.0E-4f) {
            return;
        }
        float f9 = -f7 / f8 * f5;
        float f10 = f6 / f8 * f5;
        int n4 = n * 4;
        fArray[n4] = f + f9;
        fArray2[n4] = f2 + f10;
        fArray[n4 + 1] = f - f9;
        fArray2[n4 + 1] = f2 - f10;
        fArray[n4 + 2] = f3 - f9;
        fArray2[n4 + 2] = f4 - f10;
        fArray[n4 + 3] = f3 + f9;
        fArray2[n4 + 3] = f4 + f10;
        nArray[n4 + 3] = n3 = n2;
        nArray[n4 + 2] = n3;
        nArray[n4 + 1] = n3;
        nArray[n4] = n3;
    }

    public static void renderArrow(PoseStack poseStack, float f, float f2, float f3, float f4, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int n = color.getRGB();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(5 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f2, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f - f3, f2 + f4, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f2 + f4, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f + f3, f2 + f4, 0.0f).setColor(n);
            bufferBuilder.addVertex((Matrix4fc)matrix4f, f, f2, 0.0f).setColor(n);
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(RenderTypes.debugTriangleFan(), meshData);
        }
    }

    public static void renderArrowOutline(PoseStack poseStack, float f, float f2, float f3, float f4, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        int n = color.getRGB();
        float[] fArray = new float[]{f, f - f3, f, f + f3, f};
        float[] fArray2 = new float[]{f2, f2 + f4, f2 + f4, f2 + f4, f2};
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)((fArray.length - 1) * 4 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < fArray.length - 1; ++i) {
                Renderer2D.segment(bufferBuilder, matrix4f, fArray[i], fArray2[i], fArray[i + 1], fArray2[i + 1], 0.5f, n);
            }
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(RenderTypes.debugQuads(), meshData);
        }
    }

    private static void segment(BufferBuilder bufferBuilder, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int n) {
        float f6 = f3 - f;
        float f7 = f4 - f2;
        float f8 = (float)Math.sqrt(f6 * f6 + f7 * f7);
        if (f8 == 0.0f) {
            return;
        }
        float f9 = -f7 / f8 * f5;
        float f10 = f6 / f8 * f5;
        bufferBuilder.addVertex((Matrix4fc)matrix4f, f + f9, f2 + f10, 0.0f).setColor(n);
        bufferBuilder.addVertex((Matrix4fc)matrix4f, f - f9, f2 - f10, 0.0f).setColor(n);
        bufferBuilder.addVertex((Matrix4fc)matrix4f, f3 - f9, f4 - f10, 0.0f).setColor(n);
        bufferBuilder.addVertex((Matrix4fc)matrix4f, f3 + f9, f4 + f10, 0.0f).setColor(n);
    }

    private static RenderType texturedType(Identifier identifier2) {
        return TEXTURE_TYPES.computeIfAbsent(identifier2, identifier -> RenderType.create((String)("night_textured_" + String.valueOf(identifier)), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", identifier).createRenderSetup()));
    }

    public static Vec3 project(Vec3 vec3) {
        Vec3 vec32 = Renderer2D.mc.gameRenderer.mainCamera().position();
        int[] nArray = new int[]{0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight()};
        Vector3f vector3f = new Vector3f();
        Vector4f vector4f = new Vector4f((float)(vec3.x - vec32.x), (float)(vec3.y - vec32.y), (float)(vec3.z - vec32.z), 1.0f).mul((Matrix4fc)LAST_WORLD_MATRIX);
        Matrix4f matrix4f = new Matrix4f((Matrix4fc)LAST_PROJECTION_MATRIX);
        Matrix4f matrix4f2 = new Matrix4f((Matrix4fc)LAST_MODEL_MATRIX);
        matrix4f.mul((Matrix4fc)matrix4f2).project(vector4f.x(), vector4f.y(), vector4f.z(), nArray, vector3f);
        return new Vec3((double)(vector3f.x / (float)mc.getWindow().getGuiScale()), (double)(((float)mc.getWindow().getHeight() - vector3f.y) / (float)mc.getWindow().getGuiScale()), (double)vector3f.z);
    }

    static {
        scissorHandleInitialized = false;
    }
}

