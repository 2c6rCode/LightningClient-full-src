/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.movement.NoSlowModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.SlimeBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.movement.NoSlowModule;
import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SlimeBlock.class})
public class SlimeBlockMixin
implements IMinecraft {
    @Inject(method={"stepOn"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSteppedOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity, CallbackInfo callbackInfo) {
        try {
            if (entity == SlimeBlockMixin.mc.player && ((NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class)).isToggled() && ((NoSlowModule)Night.MODULE_MANAGER.getModule(NoSlowModule.class)).slimeBlocks.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

