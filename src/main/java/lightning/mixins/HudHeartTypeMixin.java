/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.minecraft.client.gui.Hud$HeartType"})
public class HudHeartTypeMixin {
    @ModifyExpressionValue(method={"forPlayer"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal=1)})
    private static boolean night$hideWitherHeart(boolean bl) {
        try {
            NoRenderModule noRenderModule;
            if (bl && Night.MODULE_MANAGER != null && (noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && noRenderModule.wither.getValue()) {
                return false;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }
}

