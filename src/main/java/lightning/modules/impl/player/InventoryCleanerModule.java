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
        int handlerSlot;
        int invSlot;
        if (InventoryCleanerModule.mc.player == null || InventoryCleanerModule.mc.level == null || InventoryCleanerModule.mc.gameMode == null) {
            return;
        }
        if (InventoryCleanerModule.mc.player.isCreative()) {
            return;
        }
        if (!InventoryCleanerModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - RekitModule.lastContainerActionMs < 200L || now - InventorySorterModule.lastContainerActionMs < 200L) {
            return;
        }
        boolean inOtherContainer = InventoryCleanerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(InventoryCleanerModule.mc.gui.screen() instanceof InventoryScreen);
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        int containerId = InventoryCleanerModule.mc.player.containerMenu.containerId;
        int dropped = 0;
        int maxActions = this.actionsPerTick.getValue().intValue();
        for (invSlot = 0; invSlot < 36 && dropped < maxActions; ++invSlot) {
            ItemStack itemStack;
            if (this.ignoreHotbar.getValue() && invSlot <= 8 || (itemStack = InventoryCleanerModule.mc.player.getInventory().getItem(invSlot)).isEmpty() || !this.shouldDrop(itemStack) || (handlerSlot = this.invToHandlerSlot(invSlot)) < 0) continue;
            this.throwSlot(containerId, handlerSlot);
            ++dropped;
        }
        if (this.throwWorse.getValue() && dropped < maxActions) {
            dropped += this.runThrowWorsePass(containerId, maxActions - dropped);
        }
        boolean skipCustomContainer = this.ignoreCustomName.getValue() && this.hasCustomContainerTitle() && !(InventoryCleanerModule.mc.player.containerMenu instanceof ShulkerBoxMenu);
        if (inOtherContainer && this.others.getValue() && !skipCustomContainer && dropped < maxActions) {
            int totalSlots = InventoryCleanerModule.mc.player.containerMenu.slots.size();
            int containerSlots = totalSlots - 36;
            for (int i = 0; i < containerSlots && dropped < maxActions; ++i) {
                ItemStack itemStack = ((Slot)InventoryCleanerModule.mc.player.containerMenu.slots.get(i)).getItem();
                if (itemStack.isEmpty() || !this.shouldDrop(itemStack)) continue;
                this.throwSlot(containerId, i);
                ++dropped;
            }
        }
    }

    private void throwSlot(int containerId, int slotIndex) {
        InventoryCleanerModule.mc.gameMode.handleContainerInput(containerId, slotIndex, 1, ContainerInput.THROW, (Player)InventoryCleanerModule.mc.player);
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
        boolean inWhitelist = this.whitelist.isWhitelistContains(itemStack.getItem());
        return switch (this.mode.getValue()) {
            case "All" -> true;
            case "BlackList" -> {
                if (!inWhitelist) {
                    yield true;
                }
                yield false;
            }
            default -> inWhitelist;
        };
    }

    private int runThrowWorsePass(int containerId, int budget) {
        String[] gearSuffixes = new String[]{"_pickaxe", "_axe", "_shovel", "_hoe", "_sword", "_helmet", "_chestplate", "_leggings", "_boots"};
        int remaining = budget;
        block0: for (String suffix : gearSuffixes) {
            if (remaining <= 0) break;
            ArrayList<int[]> candidates = new ArrayList<int[]>();
            for (int invSlot = 0; invSlot < 36; ++invSlot) {
                if (this.ignoreHotbar.getValue() && invSlot <= 8) continue;
                ItemStack stack = InventoryCleanerModule.mc.player.getInventory().getItem(invSlot);
                if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().endsWith(suffix)) continue;
                candidates.add(new int[]{invSlot, stack.getMaxDamage()});
            }
            if (candidates.size() < 2) continue;
            int maxDamage = candidates.stream().mapToInt(entry -> entry[1]).max().orElse(0);
            for (int[] entry : candidates) {
                if (remaining <= 0) continue block0;
                int handlerSlot;
                if (entry[1] >= maxDamage || (handlerSlot = this.invToHandlerSlot(entry[0])) < 0) continue;
                this.throwSlot(containerId, handlerSlot);
                --remaining;
            }
        }
        return budget - remaining;
    }

    private int invToHandlerSlot(int invSlot) {
        Inventory inventory = InventoryCleanerModule.mc.player.getInventory();
        for (Slot slot : InventoryCleanerModule.mc.player.containerMenu.slots) {
            if (slot.container != inventory || slot.getContainerSlot() != invSlot) continue;
            return slot.index;
        }
        return -1;
    }
}

