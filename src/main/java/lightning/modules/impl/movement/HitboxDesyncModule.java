/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="HitboxDesync", description="Precisely offsets your position to glitch out Minecraft hitbox calculations.", category=Module.Category.MOVEMENT)
public class HitboxDesyncModule
extends Module {
    public BooleanSetting alternating = new BooleanSetting("Alternating", "Modifies your position in such a way that it messes with other clients.", false);
    public BooleanSetting minimal = new BooleanSetting("Minimal", "Makes alternating minimal, only required on certain servers.", new BooleanSetting.Visibility(this.alternating, true), false);
    public BooleanSetting specific = new BooleanSetting("Specific", "Specific alternating mode, only required against certain clients.", new BooleanSetting.Visibility(this.alternating, true), false);
    public BooleanSetting close = new BooleanSetting("Close", "Whether or not to have the surround place blocks on desynced positions.", false);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module after having desynced your hitbox.", new BooleanSetting.Visibility(this.alternating, false), true);
    public BooleanSetting jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever your Y level changes.", new BooleanSetting.Visibility(this.alternating, true), true);
    private final Timer timer = new Timer();
    private double prevY;

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onTick(TickEvent tickEvent) {
        if (this.getNull() || this.jumpDisable.getValue() && HitboxDesyncModule.mc.player.getY() != this.prevY) {
            this.setToggled(false);
            return;
        }
        Vec3 blockCenter = Vec3.atCenterOf((Vec3i)HitboxDesyncModule.mc.player.blockPosition());
        double offsetAmount = this.minimal.getValue() ? 0.001 : 0.002;
        double intervalMs = this.specific.getValue() ? 500.0 : 1500.0;
        boolean shouldAlternate = this.timer.hasTimeElapsed(intervalMs) && this.alternating.getValue() && !HitboxDesyncModule.mc.player.isShiftKeyDown();
        boolean centerEast = blockCenter.x - HitboxDesyncModule.mc.player.getX() > 0.0;
        boolean centerSouth = blockCenter.z - HitboxDesyncModule.mc.player.getZ() > 0.0;
        double targetX = blockCenter.x + (shouldAlternate ? offsetAmount : 0.0) * (double)(centerEast ? 1 : -1) + 0.20000000009497754 * (double)(centerEast ? -1 : 1);
        double targetZ = blockCenter.z + (shouldAlternate ? offsetAmount : 0.0) * (double)(centerSouth ? 1 : -1) + 0.2000000000949811 * (double)(centerSouth ? -1 : 1);
        HitboxDesyncModule.mc.player.setPos(targetX, HitboxDesyncModule.mc.player.getY(), targetZ);
        if (this.timer.hasTimeElapsed(intervalMs)) {
            this.timer.reset();
        }
        if (this.selfDisable.getValue() && !this.alternating.getValue()) {
            this.setToggled(false);
        }
    }

    @Override
    public void onEnable() {
        if (HitboxDesyncModule.mc.player == null || HitboxDesyncModule.mc.level == null) {
            this.setToggled(false);
            return;
        }
        this.prevY = HitboxDesyncModule.mc.player.getY();
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
    }
}

