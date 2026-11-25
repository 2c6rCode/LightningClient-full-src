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

    private static void ensureProcessed(int blurRadius, float speed, float alpha, Color color, boolean darkMode) {
        long now = System.currentTimeMillis();
        if (now - lastProcessedAtMs < 8L) {
            return;
        }
        lastProcessedAtMs = now;
        PostChain postChain = Minecraft.getInstance().getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (postChain != chain) {
            chain = postChain;
            uboBuffer = null;
        }
        if (chain == null) {
            return;
        }
        ClickGuiBackground.writeUniforms(blurRadius, speed, alpha, color, darkMode);
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
    private static void writeUniforms(int blurRadius, float speed, float alpha, Color color, boolean darkMode) {
        for (PostPass postPass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            GpuBuffer gpuBuffer;
            Map<String, GpuBuffer> map = ((PostPassAccessor)postPass).night$getCustomUniforms();
            if (!map.containsKey(UNIFORM_NAME)) continue;
            if ((uboBuffer == null || map.get(UNIFORM_NAME) != uboBuffer) && (gpuBuffer = map.put(UNIFORM_NAME, uboBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning ClickGUI BG UBO", 136, (long)UBO_SIZE))) != null) {
                gpuBuffer.close();
            }
            long now = System.currentTimeMillis();
            if (lastPhaseUpdateMs >= 0L) {
                phaseSeconds += (float)(now - lastPhaseUpdateMs) / 1000.0f * speed;
            }
            lastPhaseUpdateMs = now;
            float red = (float)color.getRed() / 255.0f;
            float green = (float)color.getGreen() / 255.0f;
            float blue = (float)color.getBlue() / 255.0f;
            float darkFlag = darkMode ? 1.0f : 0.0f;
            MemoryStack memoryStack = MemoryStack.stackPush();
            try {
                ByteBuffer byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)UBO_SIZE).putVec4(phaseSeconds, (float)blurRadius, alpha, speed).putVec4(red, green, blue, darkFlag).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), byteBuffer);
            }
            finally {
                if (memoryStack == null) continue;
                memoryStack.close();
            }
        }
    }

    public static void renderBackground(GuiGraphicsExtractor guiGraphicsExtractor, int width, int height, int blurRadius, float speed, float alpha, Color color, boolean darkMode) {
        if (alpha <= 0.001f || blurRadius < 0) {
            return;
        }
        ClickGuiBackground.ensureProcessed(blurRadius, speed, alpha, color, darkMode);
        if (texture == null) {
            return;
        }
        int alphaByte = Math.min(255, Math.max(0, Math.round(alpha * 255.0f)));
        int tintColor = alphaByte << 24 | 0xFFFFFF;
        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, 0, 0, 0.0f, 0.0f, width, height, width, height, width, height, tintColor);
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

