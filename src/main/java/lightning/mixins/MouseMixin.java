/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.MouseInputEvent
 *  lightning.events.impl.UnfilteredMouseInputEvent
 *  lightning.modules.impl.combat.MainhandModule
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.client.input.MouseButtonInfo
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.UnfilteredMouseInputEvent;
import lightning.modules.impl.combat.MainhandModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"onButton"}, at={@At(value="HEAD")})
    private void onMouseButton(long l, MouseButtonInfo mouseButtonInfo, int n, CallbackInfo callbackInfo) {
        try {
            int n2 = mouseButtonInfo.button();
            int n3 = mouseButtonInfo.modifiers();
            Night.EVENT_HANDLER.post((Event)new UnfilteredMouseInputEvent(n2, n, n3));
            if (l == this.minecraft.getWindow().handle() && n == 1 && this.minecraft.gui.screen() == null) {
                Night.EVENT_HANDLER.post((Event)new MouseInputEvent(n2));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"onScroll"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onScroll(long l, double d, double d2, CallbackInfo callbackInfo) {
        try {
            MainhandModule mainhandModule;
            if (this.minecraft.gui.screen() == null && l == this.minecraft.getWindow().handle() && Night.MODULE_MANAGER != null && (mainhandModule = (MainhandModule)Night.MODULE_MANAGER.getModule(MainhandModule.class)) != null && mainhandModule.shouldLockSlot()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

