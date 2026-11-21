/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.miscellaneous.ScreenshotModule
 *  net.minecraft.client.Screenshot
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package lightning.mixins;

import java.util.function.Consumer;
import lightning.Night;
import lightning.modules.impl.miscellaneous.ScreenshotModule;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={Screenshot.class})
public class ScreenshotMixin {
    @ModifyVariable(method={"grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V"}, at={@At(value="HEAD")}, argsOnly=true, ordinal=0)
    private static Consumer<Component> night$wrapScreenshotConsumer(Consumer<Component> consumer) {
        return component -> {
            ScreenshotModule screenshotModule = Night.MODULE_MANAGER != null ? (ScreenshotModule)Night.MODULE_MANAGER.getModule(ScreenshotModule.class) : null;
            boolean bl = screenshotModule != null && screenshotModule.isToggled();
            boolean bl2 = bl;
            if (!bl) {
                consumer.accept((Component)component);
            }
            try {
                if (bl) {
                    screenshotModule.onScreenshotCaptured(component);
                }
            }
            catch (Throwable throwable) {
                Night.LOGGER.error("Failed to process screenshot callback in ScreenshotModule", throwable);
            }
        };
    }
}

