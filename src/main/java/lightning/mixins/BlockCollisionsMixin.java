/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  lightning.utils.minecraft.WorldUtils
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.world.level.BlockCollisions
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BlockCollisions.class})
public class BlockCollisionsMixin {
    @Shadow
    @Final
    private BlockPos.MutableBlockPos pos;

    @ModifyExpressionValue(method={"computeNext"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")})
    private BlockState night$predictPlacedCollision(BlockState blockState) {
        try {
            if (!blockState.isAir()) {
                return blockState;
            }
            BlockState blockState2 = WorldUtils.getPredictedCollisionState((BlockPos)this.pos);
            return blockState2 != null ? blockState2 : blockState;
        }
        catch (NullPointerException nullPointerException) {
            return blockState;
        }
    }
}

