/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.Std140Builder
 *  com.mojang.blaze3d.buffers.Std140SizeCalculator
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  lightning.mixins.accessors.PostEffectProcessorAccessor
 *  lightning.mixins.accessors.PostPassAccessor
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.LevelTargetBundle
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.PostPass
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.resources.Identifier
 *  org.lwjgl.system.MemoryStack
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Map;
import lightning.mixins.accessors.PostEffectProcessorAccessor;
import lightning.mixins.accessors.PostPassAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

public class ClickGuiBackground {
    private static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"clickgui_bg");
    private static final Identifier TARGET_ID = Identifier.withDefaultNamespace((String)"clickgui_bg_target");
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"clickgui_bg_texture");
    private static final String UNIFORM_NAME = "ClickGuiBgConfig";
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().align(16).get();
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static BgTexture texture;
    private static float phaseSeconds;
    private static long lastPhaseUpdateMs;

    private static void ensureProcessed(int n, float f, float f2, Color color, boolean bl) {
        long l = System.currentTimeMillis();
        if (l - lastProcessedAtMs < 8L) {
            return;
        }
        lastProcessedAtMs = l;
        PostChain postChain = Minecraft.getInstance().getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (postChain != chain) {
            chain = postChain;
            uboBuffer = null;
        }
        if (chain == null) {
            return;
        }
        ClickGuiBackground.writeUniforms(n, f, f2, color, bl);
        chain.process(Minecraft.getInstance().gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
        RenderTarget renderTarget = (RenderTarget)((PostEffectProcessorAccessor)chain).getPersistentTargets().get(TARGET_ID);
        if (renderTarget == null || renderTarget.getColorTextureView() == null) {
            return;
        }
        if (texture == null || !texture.wraps(renderTarget.getColorTextureView())) {
            if (texture != null) {
                Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
            }
            texture = new BgTexture(renderTarget.getColorTexture(), renderTarget.getColorTextureView());
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, (AbstractTexture)texture);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeUniforms(int n, float f, float f2, Color color, boolean bl) {
        for (PostPass postPass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            GpuBuffer gpuBuffer;
            Map<String, GpuBuffer> map = ((PostPassAccessor)postPass).night$getCustomUniforms();
            if (!map.containsKey(UNIFORM_NAME)) continue;
            if ((uboBuffer == null || map.get(UNIFORM_NAME) != uboBuffer) && (gpuBuffer = map.put(UNIFORM_NAME, uboBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning ClickGUI BG UBO", 136, (long)UBO_SIZE))) != null) {
                gpuBuffer.close();
            }
            long l = System.currentTimeMillis();
            if (lastPhaseUpdateMs >= 0L) {
                phaseSeconds += (float)(l - lastPhaseUpdateMs) / 1000.0f * f;
            }
            lastPhaseUpdateMs = l;
            float f3 = (float)color.getRed() / 255.0f;
            float f4 = (float)color.getGreen() / 255.0f;
            float f5 = (float)color.getBlue() / 255.0f;
            float f6 = bl ? 1.0f : 0.0f;
            MemoryStack memoryStack = MemoryStack.stackPush();
            try {
                ByteBuffer byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)UBO_SIZE).putVec4(phaseSeconds, (float)n, f2, f).putVec4(f3, f4, f5, f6).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), byteBuffer);
            }
            finally {
                if (memoryStack == null) continue;
                memoryStack.close();
            }
        }
    }

    public static void renderBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3, float f, float f2, Color color, boolean bl) {
        if (f2 <= 0.001f || n3 < 0) {
            return;
        }
        ClickGuiBackground.ensureProcessed(n3, f, f2, color, bl);
        if (texture == null) {
            return;
        }
        int n4 = Math.min(255, Math.max(0, Math.round(f2 * 255.0f)));
        int n5 = n4 << 24 | 0xFFFFFF;
        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, 0, 0, 0.0f, 0.0f, n, n2, n, n2, n, n2, n5);
    }

    static {
        lastProcessedAtMs = -1L;
        phaseSeconds = 0.0f;
        lastPhaseUpdateMs = -1L;
    }

    private static class BgTexture
    extends AbstractTexture {
        BgTexture(GpuTexture gpuTexture, GpuTextureView gpuTextureView) {
            this.texture = gpuTexture;
            this.textureView = gpuTextureView;
        }

        boolean wraps(GpuTextureView gpuTextureView) {
            return this.textureView == gpuTextureView;
        }

        public void close() {
        }
    }
}

