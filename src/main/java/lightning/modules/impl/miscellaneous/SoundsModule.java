/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.miscellaneous;

import java.io.File;
import java.io.IOException;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.TargetDeathEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.FileUtils;

@RegisterModule(name="Sounds", description="Plays custom sounds when something happens. Put .wav files in .minecraft/Lightning/Client/", category=Module.Category.MISCELLANEOUS)
public class SoundsModule
extends Module {
    private static final String SOUND_DIR = "Lightning/Client/";
    CategorySetting killsCategory = new CategorySetting("Kills", "The kills category for the sounds.");
    BooleanSetting killSound = new BooleanSetting("KillSound", "Play sounds when you kill a player.", new CategorySetting.Visibility(this.killsCategory), true);
    NumberSetting killVolume = new NumberSetting("KillVolume", "Volume", "The volume for the kill sounds.", new CategorySetting.Visibility(this.killsCategory), Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(1.0f));
    StringSetting killName = new StringSetting("KillName", "Name", "The name of the kill sound file. Put it in .minecraft/Lightning/Client/", new CategorySetting.Visibility(this.killsCategory), "killsound.wav");
    CategorySetting hitsCategory = new CategorySetting("Hits", "The hits category for the sounds.");
    BooleanSetting hitSound = new BooleanSetting("HitSound", "Play sounds when you hit an entity.", new CategorySetting.Visibility(this.hitsCategory), true);
    NumberSetting hitVolume = new NumberSetting("HitVolume", "Volume", "The volume for the hit sounds.", new CategorySetting.Visibility(this.hitsCategory), Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(1.0f));
    StringSetting hitName = new StringSetting("HitName", "Name", "The name of the hit sound file. Put it in .minecraft/Lightning/Client/", new CategorySetting.Visibility(this.hitsCategory), "hitsound.wav");

    @Override
    public void onEnable() {
        try {
            FileUtils.createDirectory(SOUND_DIR);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    @SubscribeEvent
    public void onTargetDeath(TargetDeathEvent targetDeathEvent) {
        if (this.getNull()) {
            return;
        }
        if (this.killSound.getValue()) {
            FileUtils.playSound(new File(SOUND_DIR + this.killName.getValue()), this.killVolume.getValue().floatValue());
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent attackEntityEvent) {
        if (attackEntityEvent.getPlayer() != SoundsModule.mc.player) {
            return;
        }
        if (this.hitSound.getValue()) {
            FileUtils.playSound(new File(SOUND_DIR + this.hitName.getValue()), this.hitVolume.getValue().floatValue());
        }
    }
}

