/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.IndexType
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.buffers.Std140Builder
 *  com.mojang.blaze3d.buffers.Std140SizeCalculator
 *  com.mojang.blaze3d.pipeline.BindGroupLayout
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.shaders.UniformType
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  lightning.mixins.accessors.PostEffectProcessorAccessor
 *  lightning.mixins.accessors.PostPassAccessor
 *  lightning.mixins.accessors.RenderPipelinesAccessor
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.PostPass
 *  net.minecraft.client.renderer.rendertype.OutputTarget
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryStack
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import lightning.Night;
import lightning.mixins.accessors.PostEffectProcessorAccessor;
import lightning.mixins.accessors.PostPassAccessor;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.modules.impl.core.ColorModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.utils.IMinecraft;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

public class EspShader
implements IMinecraft {
    public static final String[] MODES = new String[]{"None", "Gradient", "Rainbow", "Garmadon", "Nebula", "Blue", "Purple", "Glowing", "Pink", "Neekeri", "Image"};
    private static final Identifier FRAGMENT = Identifier.fromNamespaceAndPath((String)"lightning", (String)"core/esp");
    private static final BindGroupLayout ESP_SETTINGS_LAYOUT = BindGroupLayout.builder().withUniform("EspSettings", UniformType.UNIFORM_BUFFER).build();
    private static final RenderPipeline QUADS_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getDebugFilledSnippet()}).withLocation("lightning/esp_shader_quads").withFragmentShader(FRAGMENT).withBindGroupLayout(ESP_SETTINGS_LAYOUT).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
    private static final RenderPipeline LINES_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getLinesSnippet()}).withLocation("lightning/esp_shader_lines").withFragmentShader(FRAGMENT).withBindGroupLayout(ESP_SETTINGS_LAYOUT).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec2().putFloat().putFloat().putFloat().putFloat().putInt().putVec2().align(16).get();
    private static final String OUTLINE_UNIFORM = "EspSettings";
    private static final int OUTLINE_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec4().putVec4().get();
    private static final String STAR_GLOW_UNIFORM = "StarGlowConfig";
    private static final String STAR_GLOW_UNIFORM_V = "StarGlowConfigV";
    private static final int STAR_GLOW_UBO_SIZE = new Std140SizeCalculator().putVec4().get();
    private static GpuBuffer starGlowUboBuffer;
    private static GpuBuffer starGlowUboBufferV;
    private static final String OUTLINE_GLOW_UNIFORM = "OutlineGlowConfig";
    private static final String OUTLINE_GLOW_UNIFORM_V = "OutlineGlowConfigV";
    private static final int OUTLINE_GLOW_UBO_SIZE;
    private static GpuBuffer outlineGlowUboBuffer;
    private static GpuBuffer outlineGlowUboBufferV;
    private static final String BLOOM_UNIFORM = "BloomConfig";
    private static final int BLOOM_UBO_SIZE;
    private static GpuBuffer bloomUboBuffer;
    private static GpuBuffer uboBuffer;
    private static GpuBuffer outlineUboBuffer;
    private static Settings settings;
    public static final String[] SHADER_ACTIVE_MODES;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeStarGlowSettings(PostChain postChain, float radius) {
        float sigma = Math.min(4.0f + radius * 0.05f, 14.0f);
        for (PostPass postPass : ((PostEffectProcessorAccessor)postChain).getPasses()) {
            ByteBuffer byteBuffer;
            MemoryStack memoryStack;
            GpuBuffer gpuBuffer;
            Map<String, GpuBuffer> map = ((PostPassAccessor)postPass).night$getCustomUniforms();
            if (map.containsKey(STAR_GLOW_UNIFORM)) {
                if ((starGlowUboBuffer == null || map.get(STAR_GLOW_UNIFORM) != starGlowUboBuffer) && (gpuBuffer = map.put(STAR_GLOW_UNIFORM, starGlowUboBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning star glow H shader UBO", 136, (long)STAR_GLOW_UBO_SIZE))) != null) {
                    gpuBuffer.close();
                }
                memoryStack = MemoryStack.stackPush();
                try {
                    byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)STAR_GLOW_UBO_SIZE).putVec4(radius, sigma, 1.0f, 0.0f).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(starGlowUboBuffer.slice(), byteBuffer);
                }
                finally {
                    if (memoryStack != null) {
                        memoryStack.close();
                    }
                }
            }
            if (!map.containsKey(STAR_GLOW_UNIFORM_V)) continue;
            if ((starGlowUboBufferV == null || map.get(STAR_GLOW_UNIFORM_V) != starGlowUboBufferV) && (gpuBuffer = map.put(STAR_GLOW_UNIFORM_V, starGlowUboBufferV = RenderSystem.getDevice().createBuffer(() -> "Lightning star glow V shader UBO", 136, (long)STAR_GLOW_UBO_SIZE))) != null) {
                gpuBuffer.close();
            }
            memoryStack = MemoryStack.stackPush();
            try {
                byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)STAR_GLOW_UBO_SIZE).putVec4(radius, sigma, 0.0f, 1.0f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(starGlowUboBufferV.slice(), byteBuffer);
            }
            finally {
                if (memoryStack == null) continue;
                memoryStack.close();
            }
        }
    }

    public static int modeIndex(String mode) {
        for (int i = 0; i < MODES.length; ++i) {
            if (!MODES[i].equalsIgnoreCase(mode)) continue;
            return i;
        }
        return 0;
    }

    public static void setSettings(Settings settings) {
        EspShader.settings = settings;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeOutlineSettings(PostChain postChain, Settings settings, float glowRadius) {
        float glowSigma = Math.min(6.0f + glowRadius * 18.0f, 24.0f);
        for (PostPass postPass : ((PostEffectProcessorAccessor)postChain).getPasses()) {
            GpuBuffer gpuBuffer;
            ByteBuffer byteBuffer;
            ShadersModule shadersModule;
            GpuBuffer gpuBuffer2;
            Map<String, GpuBuffer> map = ((PostPassAccessor)postPass).night$getCustomUniforms();
            if (map.containsKey(BLOOM_UNIFORM)) {
                if ((bloomUboBuffer == null || map.get(BLOOM_UNIFORM) != bloomUboBuffer) && (gpuBuffer2 = map.put(BLOOM_UNIFORM, bloomUboBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning outline bloom UBO", 136, (long)BLOOM_UBO_SIZE))) != null) {
                    gpuBuffer2.close();
                }
                shadersModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ShadersModule.class) : null;
                float bloomWidth = shadersModule != null ? shadersModule.bloomWidth.getValue().floatValue() : 3.0f;
                float bloomGlowInside = shadersModule != null && shadersModule.bloomGlowInside.getValue() ? 1.0f : 0.0f;
                float bloomGlowQuality = shadersModule != null ? shadersModule.bloomGlowQuality.getValue().floatValue() : 1.0f;
                float bloomGlowMultiplier = shadersModule != null ? shadersModule.bloomGlowMultiplier.getValue().floatValue() : 1.0f;
                float bloomFillAlpha = shadersModule != null ? shadersModule.bloomFillAlpha.getValue().floatValue() / 100.0f : 1.0f;
                float bloomOutlineAlpha = shadersModule != null ? shadersModule.bloomOutlineAlpha.getValue().floatValue() / 100.0f : 1.0f;
                float bloomFillMode = shadersModule != null ? (float)ShadersModule.getBloomFillModeIndex(shadersModule.bloomFillMode.getValue()) : 0.0f;
                float bloomGradientFactor = shadersModule != null ? shadersModule.bloomGradientFactor.getValue().floatValue() : 200.0f;
                Color color = shadersModule != null ? shadersModule.bloomGradientColor.getColor() : Color.RED;
                float timeMs = System.currentTimeMillis() % 2000000L;
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    ByteBuffer byteBuffer2 = Std140Builder.onStack((MemoryStack)memoryStack, (int)BLOOM_UBO_SIZE).putVec4(bloomWidth, bloomGlowInside, bloomGlowQuality, bloomGlowMultiplier).putVec4(bloomFillAlpha, bloomOutlineAlpha, bloomFillMode, bloomGradientFactor).putVec4((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f).putVec4(timeMs, 0.0f, (float)mc.getWindow().getWidth(), (float)mc.getWindow().getHeight()).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(bloomUboBuffer.slice(), byteBuffer2);
                }
            }
            if (map.containsKey(OUTLINE_GLOW_UNIFORM)) {
                if ((outlineGlowUboBuffer == null || map.get(OUTLINE_GLOW_UNIFORM) != outlineGlowUboBuffer) && (gpuBuffer2 = map.put(OUTLINE_GLOW_UNIFORM, outlineGlowUboBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning outline glow H UBO", 136, (long)OUTLINE_GLOW_UBO_SIZE))) != null) {
                    gpuBuffer2.close();
                }
                shadersModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ShadersModule.class) : null;
                Color glowColor = shadersModule != null && shadersModule.glowColor != null ? shadersModule.glowColor.getColor() : Color.MAGENTA;
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    ByteBuffer byteBuffer3 = Std140Builder.onStack((MemoryStack)memoryStack, (int)OUTLINE_GLOW_UBO_SIZE).putVec4(glowRadius, glowSigma, 1.0f, 0.0f).putVec4((float)glowColor.getRed() / 255.0f, (float)glowColor.getGreen() / 255.0f, (float)glowColor.getBlue() / 255.0f, (float)glowColor.getAlpha() / 255.0f).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineGlowUboBuffer.slice(), byteBuffer3);
                }
            }
            shadersModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ShadersModule.class) : null;
            ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
            if (map.containsKey(OUTLINE_GLOW_UNIFORM_V)) {
                GpuBuffer gpuBuffer3;
                if ((outlineGlowUboBufferV == null || map.get(OUTLINE_GLOW_UNIFORM_V) != outlineGlowUboBufferV) && (gpuBuffer3 = map.put(OUTLINE_GLOW_UNIFORM_V, outlineGlowUboBufferV = RenderSystem.getDevice().createBuffer(() -> "Lightning outline glow V UBO", 136, (long)OUTLINE_GLOW_UBO_SIZE))) != null) {
                    gpuBuffer3.close();
                }
                Color color = shadersModule != null && shadersModule.glowColor != null ? shadersModule.glowColor.getColor() : Color.MAGENTA;
                boolean rainbow = shadersModule != null && shadersModule.glowColor != null && (shadersModule.glowColor.isRainbow() || shadersModule.glowColor.isSync() && colorModule != null && colorModule.color.isRainbow());
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)OUTLINE_GLOW_UBO_SIZE).putVec4(glowRadius, glowSigma, rainbow ? 1.0f : 0.0f, 1.0f).putVec4((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f).get();
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineGlowUboBufferV.slice(), byteBuffer);
                }
            }
            if (!map.containsKey(OUTLINE_UNIFORM)) continue;
            if ((outlineUboBuffer == null || map.get(OUTLINE_UNIFORM) != outlineUboBuffer) && (gpuBuffer = map.put(OUTLINE_UNIFORM, outlineUboBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning outline shader UBO", 136, (long)OUTLINE_UBO_SIZE))) != null) {
                gpuBuffer.close();
            }
            boolean rainbow = shadersModule != null && shadersModule.color != null && (shadersModule.color.isRainbow() || shadersModule.color.isSync() && colorModule != null && colorModule.color.isRainbow());
            float rainbowSaturation = colorModule != null ? colorModule.rainbowSaturation.getValue().floatValue() / 100.0f : 1.0f;
            MemoryStack memoryStack = MemoryStack.stackPush();
            try {
                byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)OUTLINE_UBO_SIZE).putVec4((Vector4fc)settings.color0).putVec4((Vector4fc)settings.color1).putVec4((Vector4fc)settings.color2).putVec4((Vector4fc)settings.color3).putVec4(settings.time, settings.step, settings.distance, (float)settings.effect).putVec4(settings.alpha, glowRadius, rainbow ? 1.0f : 0.0f, rainbowSaturation).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(outlineUboBuffer.slice(), byteBuffer);
            }
            finally {
                if (memoryStack == null) continue;
                memoryStack.close();
            }
        }
    }

    public static Settings createSettings(String mode, float alphaPercent, float speed, float step, Color color0, Color color1, Color color2, Color color3, Color glowColor, Vec3 targetPos) {
        return EspShader.createSettings(mode, alphaPercent, speed, step, color0, color1, color2, color3, glowColor, targetPos, new Vector2f(0.0f, 0.0f));
    }

    public static Settings createSettings(String mode, float alphaPercent, float speed, float step, Color color0, Color color1, Color color2, Color color3, Color glowColor, Vec3 targetPos, Vector2f offset) {
        int effectIndex = EspShader.modeIndex(mode);
        if (effectIndex == 0) {
            return null;
        }
        float time = (float)(System.currentTimeMillis() % 2000000L) / 1000.0f * speed * (effectIndex == 1 ? step : 1.0f);
        float distance = targetPos != null && EspShader.mc.player != null ? (float)Math.sqrt(EspShader.mc.player.distanceToSqr(targetPos)) : 1.0f;
        return new Settings(effectIndex, time, step, alphaPercent / 100.0f, distance, effectIndex == 7 ? glowColor : color0, color1, color2, color3, offset);
    }

    public static void draw(List<Renderer3D.VertexCollection> quads, List<Renderer3D.VertexCollection> lines) {
        EspShader.draw(quads, lines, settings);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void draw(List<Renderer3D.VertexCollection> quads, List<Renderer3D.VertexCollection> lines, Settings settings) {
        BufferBuilder bufferBuilder;
        ByteBufferBuilder byteBufferBuilder;
        if (settings == null || quads.isEmpty() && lines.isEmpty()) {
            quads.clear();
            lines.clear();
            return;
        }
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning ESP shader UBO", 136, (long)UBO_SIZE);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)UBO_SIZE).putVec4((Vector4fc)settings.color0).putVec4((Vector4fc)settings.color1).putVec4((Vector4fc)settings.color2).putVec4((Vector4fc)settings.color3).putVec2((float)mc.getWindow().getWidth(), (float)mc.getWindow().getHeight()).putFloat(settings.time).putFloat(settings.step).putFloat(settings.alpha).putFloat(settings.distance).putInt(settings.effect).putVec2(settings.offset.x, settings.offset.y).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBuffer.slice(), byteBuffer);
        }
        if (!quads.isEmpty()) {
            int vertexCount = Renderer3D.vertexCount(quads);
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(vertexCount * DefaultVertexFormat.POSITION_COLOR.getVertexSize()));
            try {
                bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
                for (Renderer3D.VertexCollection vertexCollection : quads) {
                    vertexCollection.quad(bufferBuilder);
                }
                EspShader.submit(QUADS_PIPELINE, bufferBuilder.build(), gpuBuffer);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        if (!lines.isEmpty()) {
            int vertexCount = Renderer3D.vertexCount(lines) * 2;
            byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(vertexCount * DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize()));
            try {
                bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
                Renderer3D.buildLines(bufferBuilder, lines);
                EspShader.submit(LINES_PIPELINE, bufferBuilder.build(), gpuBuffer);
            }
            finally {
                if (byteBufferBuilder != null) {
                    byteBufferBuilder.close();
                }
            }
        }
        gpuBuffer.close();
        quads.clear();
        lines.clear();
    }

    private static void submit(RenderPipeline renderPipeline, MeshData meshData, GpuBuffer gpuBuffer) {
        if (meshData == null) {
            return;
        }
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector3f(), new Matrix4f());
        try (MeshData meshData2 = meshData;){
            IndexType indexType;
            GpuBuffer gpuBuffer2;
            RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer;
            GpuBuffer gpuBuffer3 = RenderSystem.getDevice().createBuffer(() -> "Lightning ESP shader vertices", 32, meshData.vertexBuffer());
            GpuBuffer gpuBuffer4 = null;
            if (meshData.indexBuffer() == null) {
                autoStorageIndexBuffer = RenderSystem.getSequentialBuffer((PrimitiveTopology)meshData.drawState().primitiveTopology());
                gpuBuffer2 = autoStorageIndexBuffer.getBuffer(meshData.drawState().indexCount());
                indexType = autoStorageIndexBuffer.type();
            } else {
                gpuBuffer2 = gpuBuffer4 = RenderSystem.getDevice().createBuffer(() -> "Lightning ESP shader indices", 64, meshData.indexBuffer());
                indexType = meshData.drawState().indexType();
            }
            com.mojang.blaze3d.pipeline.RenderTarget renderTarget = OutputTarget.MAIN_TARGET.getRenderTarget();
            GpuTextureView gpuTextureView = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.getColorTextureView();
            GpuTextureView gpuTextureView2 = gpuTextureView;
            GpuTextureView gpuTextureView3 = renderTarget.useDepth ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView()) : null;
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Lightning ESP shader draw", gpuTextureView, Optional.empty(), gpuTextureView3, OptionalDouble.empty());){
                renderPass.setPipeline(renderPipeline);
                RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
                renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                renderPass.setUniform(OUTLINE_UNIFORM, gpuBuffer);
                renderPass.setVertexBuffer(0, gpuBuffer3.slice());
                renderPass.setIndexBuffer(gpuBuffer2, indexType);
                renderPass.drawIndexed(meshData.drawState().indexCount(), 1, 0, 0, 0);
            }
            gpuBuffer3.close();
            if (gpuBuffer4 != null) {
                gpuBuffer4.close();
            }
        }
    }

    static {
        OUTLINE_GLOW_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().get();
        BLOOM_UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().get();
        SHADER_ACTIVE_MODES = Arrays.copyOfRange(MODES, 1, MODES.length);
    }

    public static class Settings {
        public final int effect;
        public final float time;
        public final float step;
        public final float alpha;
        public final float distance;
        public final Vector4f color0;
        public final Vector4f color1;
        public final Vector4f color2;
        public final Vector4f color3;
        public final Vector2f offset;

        public Settings(int effect, float time, float step, float alpha, float distance, Color color0, Color color1, Color color2, Color color3) {
            this(effect, time, step, alpha, distance, color0, color1, color2, color3, new Vector2f(0.0f, 0.0f));
        }

        public Settings(int effect, float time, float step, float alpha, float distance, Color color0, Color color1, Color color2, Color color3, Vector2f offset) {
            this.effect = effect;
            this.time = time;
            this.step = step;
            this.alpha = alpha;
            this.distance = distance;
            this.color0 = Settings.toVector(color0);
            this.color1 = Settings.toVector(color1);
            this.color2 = Settings.toVector(color2);
            this.color3 = Settings.toVector(color3);
            this.offset = offset != null ? offset : new Vector2f(0.0f, 0.0f);
        }

        private static Vector4f toVector(Color color) {
            return new Vector4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        }
    }
}

