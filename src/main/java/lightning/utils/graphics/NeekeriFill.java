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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import lightning.Night;
import lightning.mixins.accessors.PostEffectProcessorAccessor;
import lightning.mixins.accessors.PostPassAccessor;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.graphics.EspShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

public class NeekeriFill {
    private static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"neekeri_ui");
    private static final Identifier PATTERN_TARGET_ID = Identifier.withDefaultNamespace((String)"pattern");
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"neekeri_ui_pattern");
    private static final String UNIFORM_NAME = "NeekeriConfig";
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().get();
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static PatternTexture texture;
    private static float phaseSeconds;
    private static long lastPhaseUpdateMs;

    private static void ensureProcessed() {
        long now = System.currentTimeMillis();
        if (now - lastProcessedAtMs < 10L) {
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
        try {
            NeekeriFill.writeTime();
            chain.process(Minecraft.getInstance().gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
        }
        catch (IllegalStateException illegalStateException) {
            return;
        }
        RenderTarget renderTarget = (RenderTarget)((PostEffectProcessorAccessor)chain).getPersistentTargets().get(PATTERN_TARGET_ID);
        if (renderTarget == null || renderTarget.getColorTextureView() == null) {
            return;
        }
        if (texture == null || !texture.wraps(renderTarget.getColorTextureView())) {
            if (texture != null) {
                Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
            }
            texture = new PatternTexture(renderTarget.getColorTexture(), renderTarget.getColorTextureView());
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, (AbstractTexture)texture);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeTime() {
        for (PostPass postPass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            GpuBuffer gpuBuffer;
            Map<String, GpuBuffer> map = ((PostPassAccessor)postPass).night$getCustomUniforms();
            if (!map.containsKey(UNIFORM_NAME)) continue;
            if ((uboBuffer == null || map.get(UNIFORM_NAME) != uboBuffer) && (gpuBuffer = map.put(UNIFORM_NAME, uboBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning neekeri fill UBO", 136, (long)UBO_SIZE))) != null) {
                gpuBuffer.close();
            }
            ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            long now = System.currentTimeMillis();
            float speed = clickGuiModule.neekeriSpeed.getValue().floatValue();
            if (lastPhaseUpdateMs >= 0L) {
                phaseSeconds += (float)(now - lastPhaseUpdateMs) / 1000.0f * speed;
            }
            lastPhaseUpdateMs = now;
            int modeIndex = Arrays.asList(EspShader.MODES).indexOf(clickGuiModule.fillMode.getValue());
            if (modeIndex <= 0) {
                modeIndex = EspShader.MODES.length - 1;
            }
            MemoryStack memoryStack = MemoryStack.stackPush();
            try {
                ByteBuffer byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)UBO_SIZE).putVec4(phaseSeconds, (float)modeIndex, 0.0f, 0.0f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), byteBuffer);
            }
            finally {
                if (memoryStack == null) continue;
                memoryStack.close();
            }
        }
    }

    public static void fill(GuiGraphicsExtractor guiGraphicsExtractor, int x, int y, int width, int height, int alpha) {
        NeekeriFill.ensureProcessed();
        if (texture == null) {
            return;
        }
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, x, y, (float)x, (float)y, width, height, width, height, screenWidth, screenHeight, alpha << 24 | 0xFFFFFF);
    }

    static {
        lastProcessedAtMs = -1L;
        phaseSeconds = 0.0f;
        lastPhaseUpdateMs = -1L;
    }

    private static class PatternTexture
    extends AbstractTexture {
        PatternTexture(GpuTexture gpuTexture, GpuTextureView gpuTextureView) {
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

