/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 */
package lightning.modules.impl.movement;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;
import net.minecraft.world.entity.Entity;

@RegisterModule(name="ReverseStep", description="Makes it so that you fall down instantly at a specified height.", category=Module.Category.MOVEMENT)
public class ReverseStepModule
extends Module {
    public NumberSetting height = new NumberSetting("Height", "The maximum height at which instant falling will be applied to.", 3.0, 0.0, 12.0);

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (!Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(300L)) {
            return;
        }
        if (ReverseStepModule.mc.player.isPassenger() || ReverseStepModule.mc.player.isFallFlying() || ReverseStepModule.mc.player.onClimbable() || ReverseStepModule.mc.player.isInLava() || ReverseStepModule.mc.player.isInWater() || ReverseStepModule.mc.player.input.keyPresses.jump() || ReverseStepModule.mc.player.input.keyPresses.shift()) {
            return;
        }
        if (ReverseStepModule.mc.player.onGround() && this.nearBlock(this.height.getValue().doubleValue())) {
            ReverseStepModule.mc.player.setDeltaMovement(ReverseStepModule.mc.player.getDeltaMovement().x, -this.height.getValue().doubleValue(), ReverseStepModule.mc.player.getDeltaMovement().z);
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.height.getValue().floatValue());
    }

    private boolean nearBlock(double d) {
        for (double d2 = 0.0; d2 < d + 0.5; d2 += 0.01) {
            if (ReverseStepModule.mc.level.noCollision((Entity)ReverseStepModule.mc.player, ReverseStepModule.mc.player.getBoundingBox().move(0.0, -d2, 0.0))) continue;
            return true;
        }
        return false;
    }
}

