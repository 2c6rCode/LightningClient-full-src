/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.buffers.GpuBuffer
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
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  lightning.mixins.accessors.RenderPipelinesAccessor
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.rendertype.OutputTarget
 *  net.minecraft.resources.Identifier
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryStack
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
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
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import lightning.Night;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.modules.impl.visuals.AtmosphereModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

public class SkyShader
implements IMinecraft {
    private static final Identifier FRAGMENT = Identifier.fromNamespaceAndPath((String)"lightning", (String)"core/sky");
    private static final BindGroupLayout SKY_SETTINGS_LAYOUT = BindGroupLayout.builder().withUniform("SkySettings", UniformType.UNIFORM_BUFFER).build();
    private static final RenderPipeline SKY_PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getGlobalsSnippet()}).withLocation("lightning/sky_shader").withVertexShader("core/screenquad").withFragmentShader(FRAGMENT).withBindGroupLayout(SKY_SETTINGS_LAYOUT).withPrimitiveTopology(PrimitiveTopology.TRIANGLES).withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build();
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec4().putVec4().putVec4().putVec2().putFloat().putFloat().putFloat().putInt().align(16).get();
    private static float time = 0.0f;

    public static void renderSky() {
        ByteBuffer byteBuffer;
        Camera camera;
        if (SkyShader.mc.level == null || SkyShader.mc.player == null) {
            return;
        }
        AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
        if (atmosphereModule == null || !atmosphereModule.isToggled() || !atmosphereModule.modifySky.getValue()) {
            return;
        }
        if (!atmosphereModule.skyMode.getValue().equalsIgnoreCase("Custom")) {
            return;
        }
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        time += partialTick * 0.05f * atmosphereModule.speed.getValue().floatValue();
        int stormMode = atmosphereModule.customSkyMode.getValue().equalsIgnoreCase("Storm") ? 1 : 0;
        Color color = atmosphereModule.skyTint.getColor();
        float red = (float)color.getRed() / 255.0f;
        float green = (float)color.getGreen() / 255.0f;
        float blue = (float)color.getBlue() / 255.0f;
        float alpha = (float)color.getAlpha() / 255.0f;
        Vector4f tintVec = new Vector4f(red, green, blue, alpha);
        Vector4f ambientVec = new Vector4f(red * 0.1f, green * 0.1f, blue * 0.1f, 1.0f);
        Vector4f highlightVec = new Vector4f(red * 0.8f, green * 0.8f, blue * 0.8f, 1.0f);
        float yawRad = 0.0f;
        float pitchRad = 0.0f;
        float fovRad = (float)Math.toRadians(70.0);
        if (SkyShader.mc.gameRenderer != null && SkyShader.mc.gameRenderer.mainCamera() != null) {
            camera = SkyShader.mc.gameRenderer.mainCamera();
            yawRad = (float)Math.toRadians(camera.yRot());
            pitchRad = (float)Math.toRadians(camera.xRot());
        }
        if (SkyShader.mc.options != null) {
            fovRad = (float)Math.toRadians(((Integer)SkyShader.mc.options.fov().get()).intValue());
        }
        Vector4f cameraVec = new Vector4f(yawRad, pitchRad, fovRad, 0.0f);
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Lightning Sky shader UBO", 136, (long)UBO_SIZE);
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            byteBuffer = Std140Builder.onStack((MemoryStack)memoryStack, (int)UBO_SIZE).putVec4((Vector4fc)tintVec).putVec4((Vector4fc)ambientVec).putVec4((Vector4fc)highlightVec).putVec4((Vector4fc)cameraVec).putVec2((float)width, (float)height).putFloat(time).putFloat(atmosphereModule.intensity.getValue().floatValue()).putFloat(atmosphereModule.speed.getValue().floatValue()).putInt(stormMode).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBuffer.slice(), byteBuffer);
        }
        com.mojang.blaze3d.pipeline.RenderTarget renderTarget = OutputTarget.MAIN_TARGET.getRenderTarget();
        GpuTextureView colorView = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.getColorTextureView();
        GpuTextureView depthView = renderTarget.useDepth ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView()) : null;
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Lightning Sky shader draw", colorView, Optional.empty(), depthView, OptionalDouble.empty());){
            renderPass.setPipeline(SKY_PIPELINE);
            renderPass.setUniform("SkySettings", gpuBuffer);
            renderPass.draw(3, 1, 0, 0);
        }
        gpuBuffer.close();
    }
}

