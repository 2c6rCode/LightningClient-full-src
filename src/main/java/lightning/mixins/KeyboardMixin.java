/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.KeyInputEvent
 *  lightning.events.impl.UnfilteredKeyInputEvent
 *  net.minecraft.client.KeyboardHandler
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.input.KeyEvent
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
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.UnfilteredKeyInputEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={KeyboardHandler.class})
public class KeyboardMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"keyPress"}, at={@At(value="HEAD")})
    private void keyPress(long l, int n, KeyEvent keyEvent, CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new UnfilteredKeyInputEvent(keyEvent.key(), keyEvent.scancode(), n, keyEvent.modifiers()));
            if (l == this.minecraft.getWindow().handle() && n == 1 && this.minecraft.gui.screen() == null) {
                Night.EVENT_HANDLER.post((Event)new KeyInputEvent(keyEvent.key(), keyEvent.modifiers()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

