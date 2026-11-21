/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.api.IFOVModifierModule
 *  lightning.modules.impl.visuals.AspectRatioModule
 *  net.minecraft.client.renderer.Projection
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.api.IFOVModifierModule;
import lightning.modules.impl.visuals.AspectRatioModule;
import net.minecraft.client.renderer.Projection;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Projection.class})
public class ProjectionMixin {
    @Shadow
    private boolean isMatrixDirty;

    @Inject(method={"getMatrix"}, at={@At(value="HEAD")})
    private void getMatrix$forceDirty(Matrix4f matrix4f, CallbackInfoReturnable<Matrix4f> callbackInfoReturnable) {
        this.isMatrixDirty = true;
    }

    @ModifyArgs(method={"getMatrix"}, at={@At(value="INVOKE", target="Lorg/joml/Matrix4f;setPerspective(FFFFZ)Lorg/joml/Matrix4f;")})
    private void getMatrix$aspectRatio(Args args) {
        try {
            IFOVModifierModule iFOVModifierModule;
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            AspectRatioModule aspectRatioModule = (AspectRatioModule)Night.MODULE_MANAGER.getModule(AspectRatioModule.class);
            if (aspectRatioModule != null && aspectRatioModule.isToggled()) {
                args.set(1, (Object)Float.valueOf(aspectRatioModule.ratio.getValue().floatValue()));
            }
            if ((iFOVModifierModule = (IFOVModifierModule)Night.MODULE_MANAGER.getModule("FOVModifier")) != null && iFOVModifierModule.isToggled()) {
                args.set(0, (Object)Float.valueOf((float)Math.toRadians(iFOVModifierModule.getFovValue())));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

