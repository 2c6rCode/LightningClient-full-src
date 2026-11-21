/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.player;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name="Timer", description="Makes your game run at a faster tick speed.", category=Module.Category.PLAYER)
public class TimerModule
extends Module {
    public NumberSetting multiplier = new NumberSetting("Multiplier", "The multiplier that will be added to the game's speed.", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(20.0f));

    @SubscribeEvent(priority=-2147483648)
    public void onTick(TickEvent tickEvent) {
        if (TimerModule.mc.player == null || TimerModule.mc.level == null) {
            return;
        }
        Night.WORLD_MANAGER.setTimerMultiplier(this.multiplier.getValue().floatValue());
    }

    @Override
    public void onEnable() {
        Night.WORLD_MANAGER.setTimerMultiplier(this.multiplier.getValue().floatValue());
    }

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.multiplier.getValue().floatValue());
    }
}

