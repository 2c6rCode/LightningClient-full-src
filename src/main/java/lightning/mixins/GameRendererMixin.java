/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.mojang.blaze3d.ProjectionType
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.RenderWorldEvent
 *  lightning.events.impl.RenderWorldEvent$Post
 *  lightning.modules.impl.visuals.NoRenderModule
 *  lightning.modules.impl.visuals.ShadersModule
 *  lightning.utils.graphics.EspShader
 *  lightning.utils.graphics.EspShader$Settings
 *  lightning.utils.graphics.Glint
 *  lightning.utils.graphics.Renderer2D
 *  lightning.utils.graphics.Renderer3D
 *  lightning.utils.graphics.Renderer3D$VertexCollection
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.LevelTargetBundle
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.ProjectionMatrixBuffer
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.RenderWorldEvent;
import lightning.mixins.accessors.LevelRendererAccessor;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.Glint;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererMixin {
    private static ProjectionMatrixBuffer night$noBobProjectionBuffer;
    private static final List<Renderer3D.VertexCollection> night$emptyQuads;

    @Inject(method={"renderLevel"}, at={@At(value="HEAD")})
    private void renderWorld$HEAD(DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        try {
            Renderer3D.prepare();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"render(Lnet/minecraft/client/DeltaTracker;Z)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V")})
    private void night$resolveOutline(DeltaTracker deltaTracker, boolean bl, CallbackInfo callbackInfo) {
        try {
            Identifier identifier = ShadersModule.pickActiveOutlineChain();
            if (identifier == null) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.levelRenderer == null) {
                return;
            }
            RenderTarget renderTarget = ((LevelRendererAccessor)minecraft.levelRenderer).night$getEntityOutlineTarget();
            if (renderTarget == null) {
                return;
            }
            PostChain postChain = minecraft.getShaderManager().getPostChain(identifier, LevelTargetBundle.MAIN_TARGETS);
            if (postChain == null) {
                return;
            }
            EspShader.writeOutlineSettings((PostChain)postChain, (EspShader.Settings)ShadersModule.shaderSettings(), (float)ShadersModule.glowIntensity());
            postChain.process(renderTarget, GraphicsResourceAllocator.UNPOOLED);
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"renderLevel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift=At.Shift.AFTER)})
    private void renderWorld$swap(DeltaTracker deltaTracker, CallbackInfo callbackInfo, @Local(ordinal=0) Matrix4fc matrix4fc, @Local CameraRenderState cameraRenderState) {
        try {
            float f = deltaTracker.getGameTimeDeltaPartialTick(false);
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().mul(matrix4fc);
            PoseStack poseStack = new PoseStack();
            Night.EVENT_HANDLER.post((Event)new RenderWorldEvent(poseStack, f));
            if (!Renderer3D.QUADS.isEmpty() || !Renderer3D.DEBUG_LINES.isEmpty()) {
                Renderer3D.draw((List)Renderer3D.QUADS, (List)Renderer3D.DEBUG_LINES, (boolean)false);
            }
            if (!Renderer3D.SHINE_QUADS.isEmpty() || !Renderer3D.SHINE_DEBUG_LINES.isEmpty()) {
                Renderer3D.draw((List)Renderer3D.SHINE_QUADS, (List)Renderer3D.SHINE_DEBUG_LINES, (boolean)true);
            }
            if (!Renderer3D.SHADER_QUADS.isEmpty() || !Renderer3D.SHADER_DEBUG_LINES.isEmpty()) {
                EspShader.draw((List)Renderer3D.SHADER_QUADS, (List)Renderer3D.SHADER_DEBUG_LINES);
            }
            if (!Glint.QUADS.isEmpty()) {
                Glint.draw();
            }
            Night.EVENT_HANDLER.post((Event)new RenderWorldEvent.Post(poseStack, f));
            if (!Renderer3D.QUADS.isEmpty() || !Renderer3D.DEBUG_LINES.isEmpty()) {
                Renderer3D.draw((List)Renderer3D.QUADS, (List)Renderer3D.DEBUG_LINES, (boolean)false);
            }
            if (!Renderer3D.SHINE_QUADS.isEmpty() || !Renderer3D.SHINE_DEBUG_LINES.isEmpty()) {
                Renderer3D.draw((List)Renderer3D.SHINE_QUADS, (List)Renderer3D.SHINE_DEBUG_LINES, (boolean)true);
            }
            if (!Renderer3D.SHADER_QUADS.isEmpty() || !Renderer3D.SHADER_DEBUG_LINES.isEmpty()) {
                EspShader.draw((List)Renderer3D.SHADER_QUADS, (List)Renderer3D.SHADER_DEBUG_LINES);
            }
            if (!Glint.QUADS.isEmpty()) {
                Glint.draw();
            }
            if (!Renderer3D.TRACER_DEBUG_LINES.isEmpty()) {
                if (night$noBobProjectionBuffer == null) {
                    night$noBobProjectionBuffer = new ProjectionMatrixBuffer("night_no_bob_tracer");
                }
                RenderSystem.backupProjectionMatrix();
                RenderSystem.setProjectionMatrix((GpuBufferSlice)night$noBobProjectionBuffer.getBuffer(cameraRenderState.projectionMatrix), (ProjectionType)ProjectionType.PERSPECTIVE);
                Renderer3D.draw(night$emptyQuads, (List)Renderer3D.TRACER_DEBUG_LINES, (boolean)false);
                RenderSystem.restoreProjectionMatrix();
            }
            RenderSystem.getModelViewStack().popMatrix();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"bobHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void tiltViewWhenHurt(CameraRenderState cameraRenderState, PoseStack poseStack, CallbackInfo callbackInfo) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).hurtCamera.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"displayItemActivation"}, at={@At(value="HEAD")}, cancellable=true)
    private void showFloatingItem(ItemStack itemStack, CallbackInfo callbackInfo) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).totemAnimation.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"renderLevel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift=At.Shift.AFTER)})
    private void renderWorld(DeltaTracker deltaTracker, CallbackInfo callbackInfo, @Local(ordinal=0) Matrix4fc matrix4fc, @Local(ordinal=0) Matrix4f matrix4f) {
        try {
            PoseStack poseStack = new PoseStack();
            poseStack.last().pose().mul(matrix4fc);
            Renderer2D.LAST_PROJECTION_MATRIX.set((Matrix4fc)matrix4f);
            Renderer2D.LAST_MODEL_MATRIX.set((Matrix4fc)RenderSystem.getModelViewMatrixCopy());
            Renderer2D.LAST_WORLD_MATRIX.set((Matrix4fc)poseStack.last().pose());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    static {
        night$emptyQuads = new ArrayList<Renderer3D.VertexCollection>();
    }
}

