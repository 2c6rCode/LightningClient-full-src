/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.blockentity.AbstractSignRenderer
 *  net.minecraft.client.renderer.blockentity.state.SignRenderState
 *  net.minecraft.world.level.block.entity.SignText
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractSignRenderer.class})
public abstract class AbstractSignBlockEntityRendererMixin<S extends SignRenderState> {
    @Inject(method={"submitSignText"}, at={@At(value="HEAD")}, cancellable=true)
    private void submitSignText(S s, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, SignText signText, CallbackInfo callbackInfo) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).signText.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

