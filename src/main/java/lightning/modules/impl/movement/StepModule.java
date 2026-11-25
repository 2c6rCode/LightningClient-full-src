/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 */
package lightning.modules.impl.movement;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@RegisterModule(name="Step", description="Gives you the ability to instantly climb over a customizable amount of blocks.", category=Module.Category.MOVEMENT)
public class StepModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The mode that will be used to climb over blocks.", "Vanilla", new String[]{"Vanilla", "NCP"});
    public NumberSetting height = new NumberSetting("Height", "The maximum height at which blocks can be climbed over.", Float.valueOf(2.0f), Float.valueOf(0.0f), Float.valueOf(12.0f));
    public BooleanSetting useTimer = new BooleanSetting("UseTimer", "Uses timer to slow you down while climbing.", new ModeSetting.Visibility(this.mode, "NCP"), true);
    private boolean resetTimer = false;

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.resetTimer) {
            Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
            this.resetTimer = false;
        }
    }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent updateMovementEvent) {
        if (StepModule.mc.player == null || StepModule.mc.level == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("NCP")) {
            return;
        }
        double stepHeight = StepModule.mc.player.getY() - StepModule.mc.player.yo;
        if (stepHeight <= 0.75 || stepHeight > this.height.getValue().doubleValue()) {
            return;
        }
        double[] offsets = this.getOffset(stepHeight);
        if (offsets != null && offsets.length > 1) {
            if (this.useTimer.getValue()) {
                Night.WORLD_MANAGER.setTimerMultiplier(1.0f / (float)offsets.length);
                this.resetTimer = true;
            }
            for (double yOffset : offsets) {
                mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(StepModule.mc.player.xo, StepModule.mc.player.yo + yOffset, StepModule.mc.player.zo, false, StepModule.mc.player.horizontalCollision));
            }
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.height.getValue().floatValue());
    }

    public double[] getOffset(double stepHeight) {
        double[] offsets;
        switch ((int)(stepHeight * 10000.0)) {
            case 7500: 
            case 10000: {
                double[] stepOffsets = new double[2];
                stepOffsets[0] = 0.42;
                offsets = stepOffsets;
                stepOffsets[1] = 0.753;
                break;
            }
            case 8125: 
            case 8750: {
                double[] stepOffsets = new double[2];
                stepOffsets[0] = 0.39;
                offsets = stepOffsets;
                stepOffsets[1] = 0.7;
                break;
            }
            case 15000: {
                double[] stepOffsets = new double[6];
                stepOffsets[0] = 0.42;
                stepOffsets[1] = 0.75;
                stepOffsets[2] = 1.0;
                stepOffsets[3] = 1.16;
                stepOffsets[4] = 1.23;
                offsets = stepOffsets;
                stepOffsets[5] = 1.2;
                break;
            }
            case 20000: {
                double[] stepOffsets = new double[8];
                stepOffsets[0] = 0.42;
                stepOffsets[1] = 0.78;
                stepOffsets[2] = 0.63;
                stepOffsets[3] = 0.51;
                stepOffsets[4] = 0.9;
                stepOffsets[5] = 1.21;
                stepOffsets[6] = 1.45;
                offsets = stepOffsets;
                stepOffsets[7] = 1.43;
                break;
            }
            case 250000: {
                double[] stepOffsets = new double[10];
                stepOffsets[0] = 0.425;
                stepOffsets[1] = 0.821;
                stepOffsets[2] = 0.699;
                stepOffsets[3] = 0.599;
                stepOffsets[4] = 1.022;
                stepOffsets[5] = 1.372;
                stepOffsets[6] = 1.652;
                stepOffsets[7] = 1.869;
                stepOffsets[8] = 2.019;
                offsets = stepOffsets;
                stepOffsets[9] = 1.907;
                break;
            }
            default: {
                offsets = null;
            }
        }
        return offsets;
    }
}

