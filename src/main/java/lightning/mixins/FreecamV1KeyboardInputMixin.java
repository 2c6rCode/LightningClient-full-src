/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.FreecamV1Module
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
import lightning.modules.impl.visuals.FreecamV1Module;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={KeyboardInput.class})
public class FreecamV1KeyboardInputMixin
extends ClientInput {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void night$freecamV1$tick(CallbackInfo callbackInfo) {
        try {
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            FreecamV1Module freecamV1Module = (FreecamV1Module)Night.MODULE_MANAGER.getModule(FreecamV1Module.class);
            if (freecamV1Module != null && freecamV1Module.isToggled()) {
                freecamV1Module.onInput(this.moveVector.y, this.moveVector.x);
                this.keyPresses = new Input(false, false, false, false, false, false, false);
                this.moveVector = new Vec2(0.0f, 0.0f);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

