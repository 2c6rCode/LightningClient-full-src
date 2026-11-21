/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.miscellaneous.BetterChatModule
 *  lightning.utils.animations.Easing
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.FormattedCharSequence
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.miscellaneous.BetterChatModule;
import lightning.utils.animations.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraft.client.gui.components.ChatComponent$DrawingBackgroundGraphicsAccess"})
public class ChatComponentDrawingBackgroundMixin {
    @Unique
    private int night$xOffset = 0;

    @Inject(method={"handleMessage"}, at={@At(value="HEAD")})
    private void night$computeOffset(int n, float f, FormattedCharSequence formattedCharSequence, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            this.night$xOffset = 0;
            BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
            if (!betterChatModule.isToggled() || !betterChatModule.animation.getValue()) {
                return;
            }
            Long l = betterChatModule.getAnimationStart(formattedCharSequence);
            if (l == null) {
                return;
            }
            int n2 = betterChatModule.delay.getValue().intValue();
            float f2 = n2 <= 0 ? 1.0f : Easing.toDelta((long)l, (int)n2);
            float f3 = f2;
            if (f2 >= 1.0f) {
                return;
            }
            float f4 = Minecraft.getInstance().font.width(formattedCharSequence);
            this.night$xOffset = Math.round(-f4 * (1.0f - f2));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyArg(method={"handleMessage"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/ActiveTextCollector;accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V")}, index=1)
    private int night$slideInX(int n) {
        return n + this.night$xOffset;
    }
}

