/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.font.GlyphInfo
 *  lightning.Night
 *  lightning.managers.FontManager
 *  lightning.modules.impl.core.FontModule
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import com.mojang.blaze3d.font.GlyphInfo;
import lightning.Night;
import lightning.managers.FontManager;
import lightning.modules.impl.core.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GlyphInfo.class})
public interface GlyphMixin {
    @Inject(method={"getShadowOffset"}, at={@At(value="HEAD")}, cancellable=true)
    private void getShadowOffset(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        FontModule fontModule;
        if (!FontManager.isClientTextRendering()) {
            return;
        }
        if (Night.MODULE_MANAGER != null && (fontModule = (FontModule)Night.MODULE_MANAGER.getModule(FontModule.class)) != null && fontModule.isToggled() && !fontModule.shadowMode.getValue().equalsIgnoreCase("Default")) {
            callbackInfoReturnable.setReturnValue(Float.valueOf(Night.FONT_MANAGER.getShadowOffset()));
        }
    }
}

