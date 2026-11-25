/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.utils.IMinecraft
 *  lightning.utils.graphics.LoadingText
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.LoadingOverlay
 *  net.minecraft.client.gui.screens.Overlay
 *  net.minecraft.server.packs.resources.ReloadInstance
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Util
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import java.util.Optional;
import java.util.function.Consumer;
import lightning.utils.IMinecraft;
import lightning.utils.graphics.LoadingText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LoadingOverlay.class})
public abstract class LoadingOverlayMixin
extends Overlay
implements IMinecraft {
    @Shadow
    @Final
    private ReloadInstance reload;
    @Shadow
    @Final
    private Consumer<Optional<Throwable>> onFinish;
    @Shadow
    @Final
    private boolean fadeIn;
    @Shadow
    private float currentProgress;
    @Shadow
    private long fadeOutStart;
    @Shadow
    private long fadeInStart;

    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$renderLoadingScreen(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
        try {
            callbackInfo.cancel();
            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            int scaledHeight = mc.getWindow().getGuiScaledHeight();
            long now = Util.getMillis();
            if (this.fadeIn && this.fadeInStart == -1L) {
                this.fadeInStart = now;
            }
            float fadeOutProgress = this.fadeOutStart > -1L ? (float)(now - this.fadeOutStart) / 1000.0f : -1.0f;
            float fadeInProgress = this.fadeInStart > -1L ? (float)(now - this.fadeInStart) / 500.0f : -1.0f;
            float alpha = 1.0f;
            if (fadeOutProgress >= 1.0f) {
                if (LoadingOverlayMixin.mc.gui.screen() != null) {
                    LoadingOverlayMixin.mc.gui.screen().init(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                }
                try {
                    this.reload.checkExceptions();
                    this.onFinish.accept(Optional.empty());
                }
                catch (Throwable throwable) {
                    this.onFinish.accept(Optional.of(throwable));
                }
                LoadingOverlayMixin.mc.gui.setOverlay(null);
                return;
            }
            if (fadeOutProgress >= 0.0f) {
                alpha = 1.0f - Mth.clamp((float)fadeOutProgress, (float)0.0f, (float)1.0f);
            } else if (this.fadeIn && fadeInProgress >= 0.0f) {
                alpha = Mth.clamp((float)fadeInProgress, (float)0.0f, (float)1.0f);
            }
            float actualProgress = this.reload.getActualProgress();
            this.currentProgress = Mth.clamp((float)(this.currentProgress * 0.95f + actualProgress * 0.05f), (float)0.0f, (float)1.0f);
            LoadingText.render((GuiGraphicsExtractor)guiGraphicsExtractor, (int)scaledWidth, (int)scaledHeight, (float)alpha, (float)this.currentProgress, (boolean)this.reload.isDone());
            if (this.reload.isDone() && LoadingText.ready() && this.fadeOutStart == -1L) {
                this.fadeOutStart = now;
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

