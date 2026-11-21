/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.BossHealthOverlay
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BossHealthOverlay.class})
public class BossBarHudMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void render(GuiGraphicsExtractor guiGraphicsExtractor, CallbackInfo callbackInfo) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).bossBar.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

