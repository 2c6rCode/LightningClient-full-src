/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.NoRenderModule
 *  net.minecraft.client.renderer.LevelRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelRenderer.class})
public class LevelRendererMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void night$resetItemCounter(CallbackInfo callbackInfo) {
        try {
            NoRenderModule noRenderModule = (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class);
            if (noRenderModule != null) {
                noRenderModule.resetItemCounter();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

