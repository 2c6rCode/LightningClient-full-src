/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.shaded0.discordipc.connection.Connection
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package lightning.mixins;

import lightning.shaded0.discordipc.connection.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={Connection.class})
public class DiscordConnectionMixin {
    @ModifyArg(method={"open"}, at={@At(value="INVOKE", target="Llightning/shaded0/discordipc/connection/WinConnection;<init>(Ljava/lang/String;Ljava/util/function/Consumer;)V")}, index=0)
    private static String night$fixWindowsPipePrefix(String string) {
        return string.replace("\\\\?\\pipe\\", "\\\\.\\pipe\\");
    }
}

