/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.FreecamV1Module
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.visuals.FreecamV1Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public class FreecamV1MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"onScroll"}, at={@At(value="HEAD")})
    private void night$freecamV1$onScroll(long l, double d, double d2, CallbackInfo callbackInfo) {
        try {
            if (Night.MODULE_MANAGER == null || this.minecraft.gui.screen() != null || l != this.minecraft.getWindow().handle()) {
                return;
            }
            FreecamV1Module freecamV1Module = (FreecamV1Module)Night.MODULE_MANAGER.getModule(FreecamV1Module.class);
            if (freecamV1Module != null && freecamV1Module.isToggled() && d2 != 0.0) {
                freecamV1Module.onScroll(d2);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

