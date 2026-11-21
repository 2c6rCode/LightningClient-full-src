/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.EventInput
 *  lightning.events.impl.KeyboardTickEvent
 *  lightning.modules.api.IAutoWalkModule
 *  lightning.modules.api.IFreecamModule
 *  lightning.modules.api.ISprintModule
 *  lightning.utils.minecraft.BaritoneUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.ClientInput
 *  net.minecraft.client.player.KeyboardInput
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.phys.Vec2
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.EventInput;
import lightning.events.impl.KeyboardTickEvent;
import lightning.modules.api.IAutoWalkModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.api.ISprintModule;
import lightning.utils.minecraft.BaritoneUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={KeyboardInput.class})
public class KeyboardInputMixin
extends ClientInput {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void tick$TAIL(CallbackInfo callbackInfo) {
        try {
            IFreecamModule iFreecamModule;
            KeyboardTickEvent keyboardTickEvent = new KeyboardTickEvent(this.moveVector.y, this.moveVector.x);
            Night.EVENT_HANDLER.post((Event)keyboardTickEvent);
            if (keyboardTickEvent.isCancelled()) {
                this.moveVector = new Vec2(keyboardTickEvent.getMovementSideways(), keyboardTickEvent.getMovementForward());
            }
            if (Night.MODULE_MANAGER != null) {
                iFreecamModule = (IFreecamModule)Night.MODULE_MANAGER.getModule("Freecam");
                if (iFreecamModule != null && iFreecamModule.isToggled()) {
                    this.keyPresses = new Input(false, false, false, false, false, false, false);
                    this.moveVector = new Vec2(0.0f, 0.0f);
                    return;
                }
                IAutoWalkModule iAutoWalkModule = (IAutoWalkModule)Night.MODULE_MANAGER.getModule("AutoWalk");
                if (iAutoWalkModule != null && iAutoWalkModule.isToggled() && !BaritoneUtils.isActive()) {
                    boolean bl = iAutoWalkModule.isBackward();
                    boolean bl2 = !bl;
                    this.keyPresses = new Input(bl2, bl, this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
                    this.moveVector = new Vec2(this.moveVector.x, bl ? -1.0f : 1.0f);
                }
                EventInput eventInput = new EventInput(this.keyPresses.forward(), this.keyPresses.backward(), this.keyPresses.left(), this.keyPresses.right(), this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
                Night.EVENT_HANDLER.post((Event)eventInput);
                this.keyPresses = new Input(eventInput.forward, eventInput.backward, eventInput.left, eventInput.right, eventInput.jumping, eventInput.sneaking, eventInput.sprinting);
            }
            ISprintModule iSprintModule = Night.MODULE_MANAGER == null ? null : (ISprintModule)Night.MODULE_MANAGER.getModule("Sprint");
            if (iSprintModule != null && iSprintModule.isGrimCompensating()) {
                int n = iSprintModule.getGrimStrafe();
                this.keyPresses = new Input(true, false, n > 0, n < 0, this.keyPresses.jump(), this.keyPresses.shift(), this.keyPresses.sprint());
                this.moveVector = n == 0 ? new Vec2(0.0f, 1.0f) : new Vec2(n > 0 ? 1.0f : -1.0f, 1.0f);
                Minecraft.getInstance().player.setSprinting(true);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

