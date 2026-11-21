/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 */
package lightning.modules.impl.visuals;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFullBrightModule;
import lightning.settings.impl.ModeSetting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@RegisterModule(name="FullBright", description="Gives you the ability to clearly see even when in the dark.", category=Module.Category.VISUALS)
public class FullBrightModule
extends Module
implements IFullBrightModule {
    public ModeSetting mode = new ModeSetting("Mode", "The way that will be used to change the game's brightness.", "Gamma", new String[]{"Gamma", "Potion"});

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent settingChangeEvent) {
        if (settingChangeEvent.getSetting() != this.mode || !this.isToggled() || FullBrightModule.mc.player == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Potion")) {
            if (!FullBrightModule.mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
                FullBrightModule.mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
            }
        } else if (FullBrightModule.mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            FullBrightModule.mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (!this.mode.getValue().equalsIgnoreCase("Potion")) {
            return;
        }
        if (!FullBrightModule.mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            FullBrightModule.mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
        }
    }

    @Override
    public void onEnable() {
        if (FullBrightModule.mc.player == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Potion")) {
            return;
        }
        if (!FullBrightModule.mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            FullBrightModule.mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
        }
    }

    @Override
    public void onDisable() {
        if (FullBrightModule.mc.player == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Potion")) {
            return;
        }
        if (FullBrightModule.mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            FullBrightModule.mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }

    @Override
    public String getModeValue() {
        return this.mode.getValue();
    }
}

