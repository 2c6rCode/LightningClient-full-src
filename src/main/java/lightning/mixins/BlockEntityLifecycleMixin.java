/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.utils.BlockEntityInstanceTracker
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.utils.BlockEntityInstanceTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockEntity.class})
public abstract class BlockEntityLifecycleMixin {
    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void night$trackConstructed(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, CallbackInfo callbackInfo) {
        try {
            BlockEntityInstanceTracker.constructed((BlockEntity)(Object)this);
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"setRemoved"}, at={@At(value="HEAD")})
    private void night$trackRemoved(CallbackInfo callbackInfo) {
        try {
            BlockEntityInstanceTracker.removed((BlockEntity)(Object)this);
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

