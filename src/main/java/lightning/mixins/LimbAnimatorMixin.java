/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  net.minecraft.world.entity.WalkAnimationState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={WalkAnimationState.class})
public class LimbAnimatorMixin {
    @ModifyReturnValue(method={"position()F"}, at={@At(value="RETURN")})
    private float getPos(float f) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).limbSwing.getValue()) {
                return 0.0f;
            }
            return f;
        }
        catch (NullPointerException nullPointerException) {
            return f;
        }
    }

    @ModifyReturnValue(method={"position(F)F"}, at={@At(value="RETURN")})
    private float getPos2(float f) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).limbSwing.getValue()) {
                return 0.0f;
            }
            return f;
        }
        catch (NullPointerException nullPointerException) {
            return f;
        }
    }

    @ModifyReturnValue(method={"speed()F"}, at={@At(value="RETURN")})
    private float getSpeed(float f) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).limbSwing.getValue()) {
                return 0.0f;
            }
            return f;
        }
        catch (NullPointerException nullPointerException) {
            return f;
        }
    }

    @ModifyReturnValue(method={"speed(F)F"}, at={@At(value="RETURN")})
    private float getSpeed2(float f) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).limbSwing.getValue()) {
                return 0.0f;
            }
            return f;
        }
        catch (NullPointerException nullPointerException) {
            return f;
        }
    }
}

