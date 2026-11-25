/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.llamalad7.mixinextras.sugar.ref.LocalRef
 *  lightning.utils.IMinecraft
 *  lightning.utils.text.CustomFormatting
 *  lightning.utils.text.FormattingUtils
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.StringDecomposer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import lightning.utils.IMinecraft;
import lightning.utils.text.CustomFormatting;
import lightning.utils.text.FormattingUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={StringDecomposer.class})
public class TextVisitFactoryMixin
implements IMinecraft {
    @WrapOperation(method={"iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z"}, at={@At(value="INVOKE", target="Ljava/lang/String;charAt(I)C", ordinal=1)})
    private static char visitFormatted(String text, int index, Operation<Character> operation, @Local(ordinal=2) LocalRef<Style> localRef) {
        CustomFormatting customFormatting = CustomFormatting.byCode((char)text.charAt(index));
        if (customFormatting != null) {
            localRef.set(FormattingUtils.withExclusiveFormatting(localRef.get(), customFormatting));
        }
        return ((Character)operation.call(new Object[]{text, index})).charValue();
    }
}

