/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package lightning.modules.impl.player;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.MainhandModule;
import lightning.modules.impl.combat.MainhandSlotGuard;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@RegisterModule(name="Replenish", description="Automatically replenishes stacks in your hotbar with new ones when they meet a specified threshold.", category=Module.Category.PLAYER)
public class ReplenishModule
extends Module {
    public ModeSetting switchMode = new ModeSetting("Switch", "The mode that will be used for switching items.", "Swap", new String[]{"Pickup", "Swap", "Quick"});
    public NumberSetting threshold = new NumberSetting("Threshold", "The minimum amount of items in a stack before that stack is replaced.", 12, 1, 64);
    public NumberSetting minimumCount = new NumberSetting("MinimumCount", "The minimum amount of items that should be in the new stack.", 48, 1, 64);
    public BooleanSetting unstackable = new BooleanSetting("Unstackable", "Replenishes items that cannot be stacked (like Totems).", false);
    public BooleanSetting inventory = new BooleanSetting("Inventory", "Allows replenishing items while an inventory, container, or custom GUI is open.", false);
    private int ticks;
    private final Item[] hotbarItems = new Item[9];
    private final int[] hotbarCounts = new int[9];

    @Override
    public void onEnable() {
        if (ReplenishModule.mc.player == null) {
            return;
        }
        for (int i = 0; i <= 8; ++i) {
            ItemStack itemStack = ReplenishModule.mc.player.getInventory().getItem(i);
            this.hotbarItems[i] = itemStack.getItem();
            this.hotbarCounts[i] = itemStack.getCount();
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        int n;
        int n2;
        if (ReplenishModule.mc.player == null || ReplenishModule.mc.level == null) {
            return;
        }
        if (!this.inventory.getValue()) {
            n2 = ReplenishModule.mc.gui != null && ReplenishModule.mc.gui.screen() != null || ReplenishModule.mc.player.containerMenu != null && ReplenishModule.mc.player.containerMenu != ReplenishModule.mc.player.inventoryMenu ? 1 : 0;
            n = n2;
            if (n2 != 0) {
                for (int i = 0; i <= 8; ++i) {
                    ItemStack itemStack = ReplenishModule.mc.player.getInventory().getItem(i);
                    this.hotbarItems[i] = itemStack.getItem();
                    this.hotbarCounts[i] = itemStack.getCount();
                }
                return;
            }
        }
        if (ReplenishModule.mc.player.containerMenu != null && ReplenishModule.mc.player.containerMenu != ReplenishModule.mc.player.inventoryMenu && ReplenishModule.mc.player.containerMenu.slots.size() < 36) {
            return;
        }
        if (this.ticks <= 0) {
            for (n2 = 0; n2 <= 8; ++n2) {
                if (MainhandSlotGuard.isLocked(n2)) continue;
                ItemStack itemStack = ReplenishModule.mc.player.getInventory().getItem(n2);
                Item item = itemStack.getItem();
                int n3 = itemStack.getCount();
                Item item2 = this.hotbarItems[n2];
                int n4 = this.hotbarCounts[n2];
                this.hotbarItems[n2] = item;
                this.hotbarCounts[n2] = n3;
                if (item2 == null || item2 == Items.AIR || item == item2 && n3 >= n4) continue;
                Item item3 = item == Items.AIR ? item2 : item;
                ItemStack itemStack2 = item == Items.AIR ? new ItemStack((ItemLike)item2) : itemStack;
                ItemStack itemStack3 = itemStack2;
                if (!itemStack2.isStackable() && !this.unstackable.getValue() || item3 == Items.TOTEM_OF_UNDYING && Night.MODULE_MANAGER.getModule(MainhandModule.class).legitOwnsTotems() || (itemStack2.isStackable() ? n3 > (int)(this.threshold.getValue().floatValue() / 64.0f * (float)itemStack2.getMaxStackSize()) : n3 > 0) || (n = InventoryUtils.findInventory(item3, (int)((float)this.minimumCount.getValue().intValue() / 64.0f * (float)itemStack2.getMaxStackSize()))) == -1) continue;
                if (this.switchMode.getValue().equalsIgnoreCase("Quick")) {
                    ReplenishModule.mc.gameMode.handleContainerInput(ReplenishModule.mc.player.containerMenu.containerId, InventoryUtils.indexToSlot(n), MainhandSlotGuard.quickButton(n2), MainhandSlotGuard.quickInput(), (Player)ReplenishModule.mc.player);
                } else {
                    InventoryUtils.swap(this.switchMode.getValue(), n, n2);
                }
                this.ticks = 2 + Night.SERVER_MANAGER.getPingDelay();
            }
        }
        --this.ticks;
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.threshold.getValue().intValue());
    }
}

