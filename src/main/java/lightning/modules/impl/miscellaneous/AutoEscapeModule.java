/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 */
package lightning.modules.impl.miscellaneous;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.system.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;

@RegisterModule(name="AutoEscape", description="Eats a chorus fruit to escape as soon as you get trapped on all sides.", category=Module.Category.MISCELLANEOUS)
public class AutoEscapeModule
extends Module {
    public BooleanSetting swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were previously holding after eating the chorus fruit.", true);
    public NumberSetting cooldown = new NumberSetting("Cooldown", "The amount of time that has to pass before escaping again.", 1000, 0, 5000);
    public BooleanSetting autoDisable = new BooleanSetting("AutoDisable", "Disables the module after it escapes once.", false);
    private static final long EAT_DURATION_MS = 1600L;
    private final Timer timer = new Timer();
    private boolean eating = false;
    private long eatingSince = 0L;
    private int previousSlot = -1;

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (this.getNull()) {
            return;
        }
        if (this.eating) {
            if (System.currentTimeMillis() - this.eatingSince < 1600L) {
                return;
            }
            if (this.swapBack.getValue()) {
                InventoryUtils.switchBackNormal(this.previousSlot);
            }
            this.eating = false;
            this.timer.reset();
            Night.CHAT_MANAGER.tagged("Escaped by eating a chorus fruit.", this.getName());
            if (this.autoDisable.getValue()) {
                this.setToggled(false, true);
            }
            return;
        }
        if (!this.timer.hasTimeElapsed(this.cooldown.getValue().longValue())) {
            return;
        }
        if (!this.isTrapped()) {
            return;
        }
        int n = InventoryUtils.findHotbar(Items.CHORUS_FRUIT);
        if (n == -1) {
            return;
        }
        this.previousSlot = AutoEscapeModule.mc.player.getInventory().getSelectedSlot();
        if (n != this.previousSlot) {
            InventoryUtils.switchSlot("Normal", n, this.previousSlot);
        }
        AutoEscapeModule.mc.gameMode.useItem((Player)AutoEscapeModule.mc.player, InteractionHand.MAIN_HAND);
        this.eating = true;
        this.eatingSince = System.currentTimeMillis();
    }

    public boolean isEating() {
        return this.eating;
    }

    @Override
    public void onDisable() {
        this.eating = false;
    }

    private boolean isTrapped() {
        BlockPos blockPos = AutoEscapeModule.mc.player.blockPosition().above();
        return this.isSolid(blockPos.above()) && this.isSolid(blockPos.north()) && this.isSolid(blockPos.south()) && this.isSolid(blockPos.east()) && this.isSolid(blockPos.west());
    }

    private boolean isSolid(BlockPos blockPos) {
        return !AutoEscapeModule.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)AutoEscapeModule.mc.level, blockPos).isEmpty();
    }
}

