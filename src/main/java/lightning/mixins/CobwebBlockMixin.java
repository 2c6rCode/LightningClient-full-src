/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.movement.FastWebModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.InsideBlockEffectApplier
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.WebBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.movement.FastWebModule;
import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WebBlock.class})
public class CobwebBlockMixin
implements IMinecraft {
    @Inject(method={"entityInside"}, at={@At(value="HEAD")}, cancellable=true)
    private void onEntityCollision(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean isPrecise, CallbackInfo callbackInfo) {
        try {
            if (((FastWebModule)Night.MODULE_MANAGER.getModule(FastWebModule.class)).isToggled()) {
                if (((FastWebModule)Night.MODULE_MANAGER.getModule(FastWebModule.class)).sneak.getValue() && !CobwebBlockMixin.mc.player.isShiftKeyDown()) {
                    return;
                }
                if (((FastWebModule)Night.MODULE_MANAGER.getModule(FastWebModule.class)).mode.getValue().equalsIgnoreCase("Ignore")) {
                    entity.resetFallDistance();
                    callbackInfo.cancel();
                }
                if (((FastWebModule)Night.MODULE_MANAGER.getModule(FastWebModule.class)).mode.getValue().equalsIgnoreCase("Strong")) {
                    entity.makeStuckInBlock(blockState, new Vec3(((FastWebModule)Night.MODULE_MANAGER.getModule(FastWebModule.class)).horizontal.getValue().doubleValue(), ((FastWebModule)Night.MODULE_MANAGER.getModule(FastWebModule.class)).vertical.getValue().doubleValue(), ((FastWebModule)Night.MODULE_MANAGER.getModule(FastWebModule.class)).horizontal.getValue().doubleValue()));
                    callbackInfo.cancel();
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

