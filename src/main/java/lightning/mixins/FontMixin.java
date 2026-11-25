/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.core.FontModule
 *  net.minecraft.client.gui.Font
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.core.FontModule;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Font.class})
public class FontMixin {
    @Inject(method={"width(Ljava/lang/String;)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void width$String(String text, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        try {
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            FontModule fontModule = (FontModule)Night.MODULE_MANAGER.getModule(FontModule.class);
            if (fontModule != null && fontModule.isToggled() && fontModule.customFont.getValue() && fontModule.global.getValue() && Night.FONT_MANAGER.getFontRenderer() != null) {
                callbackInfoReturnable.setReturnValue((int)Night.FONT_MANAGER.getFontRenderer().getTextWidth(text));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"width(Lnet/minecraft/network/chat/FormattedText;)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void width$FormattedText(FormattedText formattedText, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        try {
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            FontModule fontModule = (FontModule)Night.MODULE_MANAGER.getModule(FontModule.class);
            if (fontModule != null && fontModule.isToggled() && fontModule.customFont.getValue() && fontModule.global.getValue() && Night.FONT_MANAGER.getFontRenderer() != null) {
                callbackInfoReturnable.setReturnValue((int)Night.FONT_MANAGER.getFontRenderer().getTextWidth(formattedText.getString()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"width(Lnet/minecraft/util/FormattedCharSequence;)I"}, at={@At(value="HEAD")}, cancellable=true)
    private void width$FormattedCharSequence(FormattedCharSequence formattedCharSequence, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        try {
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            FontModule fontModule = (FontModule)Night.MODULE_MANAGER.getModule(FontModule.class);
            if (fontModule != null && fontModule.isToggled() && fontModule.customFont.getValue() && fontModule.global.getValue() && Night.FONT_MANAGER.getFontRenderer() != null) {
                callbackInfoReturnable.setReturnValue((int)Night.FONT_MANAGER.getFontRenderer().getTextWidth(formattedCharSequence));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

