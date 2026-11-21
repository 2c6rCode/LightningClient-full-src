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
import lightning.utils.IMinecraft;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

public class MenuShader
implements IMinecraft {
    private static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"menu_bg");
    private static final Identifier TARGET_ID = Identifier.withDefaultNamespace((String)"menu_bg_target");
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"menu_bg_texture");
    private static final String UNIFORM_NAME = "MenuBgConfig";
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().align(16).get();
    private static PostChain chain;
    private static GpuBuffer uboBuffer;
    private static long lastProcessedAtMs;
    private static MenuTexture texture;
    private static final long START_TIME;
    private static final Identifier STATIC_BG;

    private static void ensureProcessed(float f, float f2, boolean bl) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getShaderManager() == null || minecraft.gameRenderer == null || minecraft.gameRenderer.mainRenderTarget() == null) {
            return;
        }
        if (!bl) {
            return;
        }
        try {
            PostChain postChain = minecraft.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
            if (postChain != chain) {
                chain = postChain;
                uboBuffer = null;
            }
            if (chain == null) {
                return;
            }
            MenuShader.writeUniforms(f, f2);
            chain.process(minecraft.gameRenderer.mainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
            RenderTarget renderTarget = (RenderTarget)((PostEffectProcessorAccessor)chain).getPersistentTargets().get(TARGET_ID);
            if (renderTarget == null || renderTarget.getColorTextureView() == null) {
                return;
            }
            if (texture == null || !texture.wraps(renderTarget.getColorTextureView())) {
                if (texture != null) {
                    minecraft.getTextureManager().release(TEXTURE_ID);
                }
                texture = new MenuTexture(renderTarget.getColorTexture(), renderTarget.getColorTextureView());
                minecraft.getTextureManager().register(TEXTURE_ID, (AbstractTexture)texture);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeUniforms(float f, float f2) {
        if (chain == null) {
            return;
        }
        for (PostPass postPass : ((PostEffectProcessorAccessor)chain).getPasses()) {
            GpuBuffer gpuBuffer;
            Map<String, GpuBuffer> map = ((PostPassAccessor)postPass).night$getCustomUniforms();
            if (!map.containsKey(UNIFORM_NAME)) continue;
            if ((uboBuffer == null || map.get(UNIFORM_NAME) != uboBuffer) && (gpuBuffer = map.put(UNIFORM_NAME, uboBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning Menu BG UBO", 136, (long)UBO_SIZE))) != null) {
                gpuBuffer.close();
            }
            float f3 = (float)(System.currentTimeMillis() - START_TIME) / 1000.0f;
            MemoryStack memoryStack = MemoryStack.stackPush();
            try {
                ByteBuffer byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)UBO_SIZE).putVec4(f3, f, f2, 0.0f).get();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uboBuffer.slice(), byteBuffer);
            }
            finally {
                if (memoryStack == null) continue;
                memoryStack.close();
            }
        }
    }

    public static void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, float f2) {
        MenuShader.render(guiGraphicsExtractor, n, n2, f, f2, true);
    }

    public static void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, float f2, boolean bl) {
        try {
            MenuShader.ensureProcessed(f, f2, bl);
            int n3 = Math.min(255, Math.max(0, Math.round(f * 255.0f)));
            int n4 = n3 << 24 | 0xFFFFFF;
            if (texture != null) {
                guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, 0, 0, 0.0f, 0.0f, n, n2, n, n2, n, n2, n4);
            } else {
                guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, STATIC_BG, 0, 0, 0.0f, 0.0f, n, n2, n, n2, n, n2, n4);
            }
        }
        catch (Throwable throwable) {
            Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, n, n2, new Color(8, 8, 12, (int)(255.0f * f)));
        }
    }

    public static void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        MenuShader.render(guiGraphicsExtractor, n, n2, f, 1.0f);
    }

    static {
        lastProcessedAtMs = -1L;
        START_TIME = System.currentTimeMillis();
        STATIC_BG = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/gui/moonlight_bg.png");
    }

    private static class MenuTexture
    extends AbstractTexture {
        MenuTexture(GpuTexture gpuTexture, GpuTextureView gpuTextureView) {
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

