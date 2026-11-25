/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.ShulkerBoxProgressAccessor
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import lightning.mixins.accessors.ShulkerBoxProgressAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ModeSetting;
import lightning.utils.BlockEntityInstanceTracker;
import lightning.utils.EarlyTickHooks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="HighJump", description="Alek Grim Shulker HighJump.", category=Module.Category.MOVEMENT)
public class HighJumpModule
extends Module {
    private long timestamp = System.currentTimeMillis();
    public final ModeSetting mode = new ModeSetting("Mode", "Selects the type of mode", "Grim Shulker", new String[]{"Grim Shulker"});
    private final Runnable earlyTickCallback = this::onEarlyTick;

    @Override
    public void onEnable() {
        EarlyTickHooks.register(this.earlyTickCallback);
    }

    @Override
    public void onDisable() {
        EarlyTickHooks.unregister(this.earlyTickCallback);
    }

    private boolean passed(int delayMs) {
        return System.currentTimeMillis() - (long)delayMs >= this.timestamp;
    }

    private void onEarlyTick() {
        if (this.getNull()) {
            return;
        }
        if (this.passed(1000)) {
            BlockEntityInstanceTracker.snapshot().stream().filter(HighJumpModule::qualifies).findFirst().ifPresent(blockEntity -> {
                this.timestamp = System.currentTimeMillis();
            });
        }
        if (!this.passed(199)) {
            Vec3 velocity = HighJumpModule.mc.player.getDeltaMovement();
            HighJumpModule.mc.player.setDeltaMovement(velocity.x, velocity.y + 0.899, velocity.z);
        }
    }

    private static boolean qualifies(BlockEntity blockEntity) {
        if (!(blockEntity instanceof ShulkerBoxBlockEntity)) {
            return false;
        }
        ShulkerBoxBlockEntity shulkerBoxBlockEntity = (ShulkerBoxBlockEntity)blockEntity;
        if (((Direction)shulkerBoxBlockEntity.getBlockState().getValue((Property)ShulkerBoxBlock.FACING)).getStepY() == 0) {
            return false;
        }
        float progress = ((ShulkerBoxProgressAccessor)shulkerBoxBlockEntity).night$getCurrentProgress();
        if (progress <= 0.0f) {
            return false;
        }
        return HighJumpModule.mc.player.getBoundingBox().inflate(0.7, 1.0, 0.7).intersects(new AABB(shulkerBoxBlockEntity.getBlockPos()));
    }
}

