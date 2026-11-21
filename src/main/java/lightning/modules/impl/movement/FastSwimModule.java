/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector2d
 */
package lightning.modules.impl.movement;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.MovementUtils;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

@RegisterModule(name="FastSwim", description="Modifies your swim speed in water and lava.", category=Module.Category.MOVEMENT)
public class FastSwimModule
extends Module {
    private static final double DEFAULT_VERTICAL_SPEED = 0.04;
    public NumberSetting hWater = new NumberSetting("H-Water", "Horizontal swim speed multiplier in water.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting vWater = new NumberSetting("V-Water", "Vertical (up/down) swim speed multiplier in water.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting hLava = new NumberSetting("H-Lava", "Horizontal swim speed multiplier in lava.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting vLava = new NumberSetting("V-Lava", "Vertical (up/down) swim speed multiplier in lava.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        float f;
        float f2;
        if (FastSwimModule.mc.player == null) {
            return;
        }
        if (FastSwimModule.mc.player.onGround()) {
            return;
        }
        ElytraFlyModule elytraFlyModule = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (elytraFlyModule.isToggled() && elytraFlyModule.mode.getValue().equalsIgnoreCase("Control") && FastSwimModule.mc.player.isFallFlying()) {
            return;
        }
        if (FastSwimModule.mc.player.isInLava()) {
            f2 = this.hLava.getValue().floatValue();
            f = this.vLava.getValue().floatValue();
        } else if (FastSwimModule.mc.player.isInWater()) {
            f2 = this.hWater.getValue().floatValue();
            f = this.vWater.getValue().floatValue();
        } else {
            return;
        }
        Vector2d vector2d = MovementUtils.forward(MovementUtils.DEFAULT_SPEED * (double)f2);
        double d = playerMoveEvent.getMovement().y;
        if (FastSwimModule.mc.options.keyJump.isDown()) {
            d = 0.04 * (double)f;
        } else if (FastSwimModule.mc.options.keyShift.isDown()) {
            d = -0.04 * (double)f;
        }
        playerMoveEvent.setMovement(new Vec3(vector2d.x, d, vector2d.y));
        playerMoveEvent.setCancelled(true);
    }
}

