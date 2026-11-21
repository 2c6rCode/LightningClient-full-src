/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.managers.RenderManager
 *  net.minecraft.client.gui.render.GuiRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.managers.RenderManager;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiRenderer.class})
public class GuiRendererMixin {
    @Inject(method={"draw"}, at={@At(value="RETURN")})
    private void night$postGuiRender(CallbackInfo callbackInfo) {
        try {
            for (Runnable runnable : RenderManager.POST_GUI_RENDERERS) {
                runnable.run();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

