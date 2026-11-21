/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockEntityRenderDispatcher.class})
public class BlockEntityRenderDispatcherMixin
implements IMinecraft {
    @Inject(method={"submit"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$submit(BlockEntityRenderState blockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class);
            if (noRenderModule.isToggled() && !noRenderModule.tileEntities.getValue().equals("Never") && (noRenderModule.tileEntities.getValue().equals("Always") || noRenderModule.tileEntities.getValue().equals("Distance") && Math.sqrt(BlockEntityRenderDispatcherMixin.mc.player.distanceToSqr((double)blockEntityRenderState.blockPos.getX(), (double)blockEntityRenderState.blockPos.getY(), (double)blockEntityRenderState.blockPos.getZ())) > (double)noRenderModule.tileDistance.getValue().floatValue())) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

