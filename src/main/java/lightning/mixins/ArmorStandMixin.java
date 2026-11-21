/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ArmorStand.class})
public abstract class ArmorStandMixin {
    @Inject(method={"isPickable"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$hideFromRaytrace(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            NoRenderModule noRenderModule = Night.MODULE_MANAGER != null ? (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
            NoRenderModule noRenderModule2 = noRenderModule;
            if (noRenderModule != null && noRenderModule.isToggled() && noRenderModule.armorStand.getValue()) {
                callbackInfoReturnable.setReturnValue(false);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

