/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.ShulkerBoxMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package lightning.modules.impl.player;

import java.util.ArrayList;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.InventorySorterModule;
import lightning.modules.impl.player.RekitModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@RegisterModule(name="InventoryCleaner", description="Auto-drops unwanted items from your inventory each tick.", category=Module.Category.PLAYER)
public class InventoryCleanerModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "WhiteList = drop items IN list. BlackList = drop items NOT in list. All = drop everything.", "WhiteList", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting whitelist = new WhitelistSetting("Whitelist", "Items this mode's WhiteList/BlackList compares against.", WhitelistSetting.Type.ITEMS);
    public BooleanSetting ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Skip hotbar slots (0-8) when cleaning.", true);
    public BooleanSetting throwWorse = new BooleanSetting("ThrowWorse", "Drop lower-tier duplicate tools/armor of the same type (e.g. diamond+iron pickaxe -> drop iron).", true);
    public BooleanSetting others = new BooleanSetting("Others", "Also drop from other open GUIs like Chest, Shulker, EnderChest.", false);
    public BooleanSetting ignoreCustomName = new BooleanSetting("IgnoreCustomName", "(With Others on) skip acting on custom-titled containers such as a shop GUI, except shulker boxes. Does not affect individual items.", false);
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay between drop passes.", 1, 0, 20);
    public NumberSetting actionsPerTick = new NumberSetting("ActionsPerTick", "Max items to drop per pass.", 5, 1, 20);
    private int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        int n;
        int n2;
        if (InventoryCleanerModule.mc.player == null || InventoryCleanerModule.mc.level == null || InventoryCleanerModule.mc.gameMode == null) {
            return;
        }
        if (InventoryCleanerModule.mc.player.isCreative()) {
            return;
        }
        if (!InventoryCleanerModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - RekitModule.lastContainerActionMs < 200L || l - InventorySorterModule.lastContainerActionMs < 200L) {
            return;
        }
        boolean bl = InventoryCleanerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(InventoryCleanerModule.mc.gui.screen() instanceof InventoryScreen);
        boolean bl2 = bl;
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        int n3 = InventoryCleanerModule.mc.player.containerMenu.containerId;
        int n4 = 0;
        int n5 = this.actionsPerTick.getValue().intValue();
        for (n2 = 0; n2 < 36 && n4 < n5; ++n2) {
            ItemStack itemStack;
            if (this.ignoreHotbar.getValue() && n2 <= 8 || (itemStack = InventoryCleanerModule.mc.player.getInventory().getItem(n2)).isEmpty() || !this.shouldDrop(itemStack) || (n = this.invToHandlerSlot(n2)) < 0) continue;
            this.throwSlot(n3, n);
            ++n4;
        }
        if (this.throwWorse.getValue() && n4 < n5) {
            n4 += this.runThrowWorsePass(n3, n5 - n4);
        }
        int n6 = this.ignoreCustomName.getValue() && this.hasCustomContainerTitle() && !(InventoryCleanerModule.mc.player.containerMenu instanceof ShulkerBoxMenu) ? 1 : 0;
        n2 = n6;
        if (bl && this.others.getValue() && n6 == 0 && n4 < n5) {
            n = InventoryCleanerModule.mc.player.containerMenu.slots.size();
            int n7 = n - 36;
            for (int i = 0; i < n7 && n4 < n5; ++i) {
                ItemStack itemStack = ((Slot)InventoryCleanerModule.mc.player.containerMenu.slots.get(i)).getItem();
                if (itemStack.isEmpty() || !this.shouldDrop(itemStack)) continue;
                this.throwSlot(n3, i);
                ++n4;
            }
        }
    }

    private void throwSlot(int n, int n2) {
        InventoryCleanerModule.mc.gameMode.handleContainerInput(n, n2, 1, ContainerInput.THROW, (Player)InventoryCleanerModule.mc.player);
    }

    private boolean hasCustomContainerTitle() {
        Screen screen = InventoryCleanerModule.mc.gui.screen();
        if (!(screen instanceof AbstractContainerScreen)) {
            return false;
        }
        AbstractContainerScreen abstractContainerScreen = (AbstractContainerScreen)screen;
        Component component = abstractContainerScreen.getTitle();
        if (component == null) {
            return false;
        }
        return !(component.getContents() instanceof TranslatableContents);
    }

    private boolean shouldDrop(ItemStack itemStack) {
        boolean bl = this.whitelist.isWhitelistContains(itemStack.getItem());
        return switch (this.mode.getValue()) {
            case "All" -> true;
            case "BlackList" -> {
                if (!bl) {
                    yield true;
                }
                yield false;
            }
            default -> bl;
        };
    }

    private int runThrowWorsePass(int n, int n2) {
        String[] stringArray = new String[]{"_pickaxe", "_axe", "_shovel", "_hoe", "_sword", "_helmet", "_chestplate", "_leggings", "_boots"};
        int n3 = n2;
        block0: for (String string : stringArray) {
            if (n3 <= 0) break;
            ArrayList<int[]> arrayList = new ArrayList<int[]>();
            for (int n4 = 0; n4 < 36; ++n4) {
                if (this.ignoreHotbar.getValue() && n4 <= 8) continue;
                ItemStack stack = InventoryCleanerModule.mc.player.getInventory().getItem(n4);
                if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().endsWith(string)) continue;
                arrayList.add(new int[]{n4, stack.getMaxDamage()});
            }
            if (arrayList.size() < 2) continue;
            int maxDamage = arrayList.stream().mapToInt(nArray -> nArray[1]).max().orElse(0);
            for (int[] entry : arrayList) {
                if (n3 <= 0) continue block0;
                int n5;
                if (entry[1] >= maxDamage || (n5 = this.invToHandlerSlot(entry[0])) < 0) continue;
                this.throwSlot(n, n5);
                --n3;
            }
        }
        return n2 - n3;
    }

    private int invToHandlerSlot(int n) {
        Inventory inventory = InventoryCleanerModule.mc.player.getInventory();
        for (Slot slot : InventoryCleanerModule.mc.player.containerMenu.slots) {
            if (slot.container != inventory || slot.getContainerSlot() != n) continue;
            return slot.index;
        }
        return -1;
    }
}

