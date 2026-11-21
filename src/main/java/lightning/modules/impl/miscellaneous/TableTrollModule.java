/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.level.block.Blocks
 */
package lightning.modules.impl.miscellaneous;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;

public class TableTrollModule
extends Module {
    public NumberSetting limit = new NumberSetting("Limit", "The maximum number of blocks that can be placed each group.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The delay in ticks between each group of placements.", 0, 0, 20);
    private int ticks = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        int n = 0;
        for (int i = 0; i < Night.WORLD_MANAGER.getRadius(5.0) && n <= this.limit.getValue().intValue(); ++i) {
            BlockPos blockPos = TableTrollModule.mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
            if (!WorldUtils.isPlaceable(blockPos) || TableTrollModule.mc.level.getBlockState(blockPos.below()).canBeReplaced() || TableTrollModule.mc.level.getBlockState(blockPos.below()).getBlock() == Blocks.ENDER_CHEST) continue;
            WorldUtils.placeBlock(blockPos, WorldUtils.getDirection(blockPos, false), InteractionHand.MAIN_HAND, false, false);
            this.ticks = this.delay.getValue().intValue();
            ++n;
        }
    }
}

