/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package lightning.mixins.accessors;

import javax.annotation.Nullable;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Style.class})
public interface StyleAccessor {
    @Invoker(value="<init>")
    public static Style create(@Nullable TextColor textColor, @Nullable Integer n, @Nullable Boolean bl, @Nullable Boolean bl2, @Nullable Boolean bl3, @Nullable Boolean bl4, @Nullable Boolean bl5, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String string, @Nullable FontDescription fontDescription) {
        return null;
    }
}

