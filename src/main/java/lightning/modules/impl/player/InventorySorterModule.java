/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 */
package lightning.modules.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.RekitModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;

@RegisterModule(name="InventorySorter", description="Auto-sorts your inventory into the active Rekit kit's layout.", category=Module.Category.PLAYER)
public class InventorySorterModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay.", 1, 0, 10);
    public NumberSetting actionsPerTick = new NumberSetting("ActionsPerTick", "Max actions per tick.", 1, 1, 5);
    public BooleanSetting ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Skip hotbar slots (0-8) when sorting.", false);
    private int ticks = 0;
    public static volatile long lastContainerActionMs = 0L;
    private int cursorWaitTicks = 0;
    private static final int CURSOR_DUMP_AFTER_TICKS = 20;

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (InventorySorterModule.mc.player == null) {
            return;
        }
        RekitModule rekitModule = Night.MODULE_MANAGER.getModule(RekitModule.class);
        if (rekitModule.getActiveKit().isEmpty()) {
            return;
        }
        if (InventorySorterModule.mc.player.isCreative()) {
            return;
        }
        if (InventorySorterModule.mc.gui.screen() instanceof AbstractContainerScreen && !(InventorySorterModule.mc.gui.screen() instanceof InventoryScreen)) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        for (int i = 0; i < this.actionsPerTick.getValue().intValue() && this.sortTick(rekitModule); ++i) {
        }
    }

    private boolean sortTick(RekitModule rekitModule) {
        AbstractContainerMenu abstractContainerMenu = InventorySorterModule.mc.player.containerMenu;
        if (!abstractContainerMenu.getCarried().isEmpty()) {
            ++this.cursorWaitTicks;
            if (this.cursorWaitTicks >= 20) {
                this.cursorWaitTicks = 0;
                int emptySlot = this.findEmptySlot(abstractContainerMenu);
                if (emptySlot != -1) {
                    this.click(emptySlot, 0, ContainerInput.PICKUP);
                }
            }
            return false;
        }
        this.cursorWaitTicks = 0;
        HashMap<String, List<Integer>> partialStacksByItem = new HashMap<>();
        for (int i = 0; i < 36; ++i) {
            int handlerSlot;
            ItemStack itemStack;
            if (this.ignoreHotbar.getValue() && i <= 8 || (itemStack = abstractContainerMenu.getSlot(handlerSlot = this.getHandlerSlot(i)).getItem()).isEmpty() || this.isShulkerBox(itemStack) || itemStack.getCount() >= itemStack.getItem().getDefaultMaxStackSize() || this.isCorrectItem(itemStack, rekitModule.getActiveKit().get(i))) continue;
            String key = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            partialStacksByItem.computeIfAbsent(key, ignoredKey -> new ArrayList<>()).add(handlerSlot);
        }
        for (List<Integer> slotList : partialStacksByItem.values()) {
            if (slotList.size() <= 1) continue;
            for (int idxA = 0; idxA < slotList.size(); ++idxA) {
                int slotA = slotList.get(idxA);
                ItemStack stackA = abstractContainerMenu.getSlot(slotA).getItem();
                for (int idxB = idxA + 1; idxB < slotList.size(); ++idxB) {
                    int slotB = slotList.get(idxB);
                    ItemStack stackB = abstractContainerMenu.getSlot(slotB).getItem();
                    if (!ItemStack.isSameItemSameComponents(stackA, stackB)) continue;
                    this.atomicSwap(slotB, slotA);
                    return true;
                }
            }
        }
        for (int i = 0; i < 36; ++i) {
            int sourceInvSlot;
            RekitModule.KitItem kitItem;
            int targetHandlerSlot;
            ItemStack currentStack;
            if (this.ignoreHotbar.getValue() && i <= 8 || this.isShulkerBox(currentStack = abstractContainerMenu.getSlot(targetHandlerSlot = this.getHandlerSlot(i)).getItem()) || this.isCorrectItem(currentStack, kitItem = rekitModule.getActiveKit().get(i)) || (sourceInvSlot = this.findItemForKit(abstractContainerMenu, rekitModule, kitItem)) == -1) continue;
            int sourceHandlerSlot = this.getHandlerSlot(sourceInvSlot);
            if (i <= 8 && sourceInvSlot >= 9) {
                this.click(sourceHandlerSlot, i, ContainerInput.SWAP);
                return true;
            }
            if (sourceInvSlot <= 8 && i >= 9) {
                this.click(targetHandlerSlot, sourceInvSlot, ContainerInput.SWAP);
                return true;
            }
            this.atomicSwap(sourceHandlerSlot, targetHandlerSlot);
            return true;
        }
        return false;
    }

    private void atomicSwap(int slotA, int slotB) {
        this.click(slotA, 0, ContainerInput.PICKUP);
        this.click(slotB, 0, ContainerInput.PICKUP);
        this.click(slotA, 0, ContainerInput.PICKUP);
    }

    private void click(int slotIndex, int button, ContainerInput containerInput) {
        lastContainerActionMs = System.currentTimeMillis();
        InventorySorterModule.mc.gameMode.handleContainerInput(InventorySorterModule.mc.player.containerMenu.containerId, slotIndex, button, containerInput, (Player)InventorySorterModule.mc.player);
    }

    private boolean isCorrectItem(ItemStack itemStack, RekitModule.KitItem kitItem) {
        if (kitItem == null) {
            return true;
        }
        if (itemStack.isEmpty()) {
            return false;
        }
        if (kitItem.id == null || kitItem.id.isEmpty()) {
            return false;
        }
        Identifier identifier = Identifier.tryParse((String)kitItem.id);
        if (identifier == null) {
            return false;
        }
        Item item = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        return item != null && itemStack.getItem() == item;
    }

    private int findItemForKit(AbstractContainerMenu abstractContainerMenu, RekitModule rekitModule, RekitModule.KitItem kitItem) {
        if (kitItem == null || kitItem.id == null || kitItem.id.isEmpty()) {
            return -1;
        }
        Identifier identifier = Identifier.tryParse((String)kitItem.id);
        if (identifier == null) {
            return -1;
        }
        Item item = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        if (item == null) {
            return -1;
        }
        for (int i = 0; i < 36; ++i) {
            RekitModule.KitItem slotKitItem;
            int handlerSlot;
            ItemStack itemStack;
            if (this.ignoreHotbar.getValue() && i <= 8 || (itemStack = abstractContainerMenu.getSlot(handlerSlot = this.getHandlerSlot(i)).getItem()).isEmpty() || this.isShulkerBox(itemStack) || itemStack.getItem() != item || this.isCorrectItem(itemStack, slotKitItem = rekitModule.getActiveKit().get(i))) continue;
            return i;
        }
        return -1;
    }

    private int findEmptySlot(AbstractContainerMenu abstractContainerMenu) {
        int handlerSlot;
        int invSlot;
        RekitModule rekitModule = Night.MODULE_MANAGER.getModule(RekitModule.class);
        for (invSlot = 0; invSlot < 36; ++invSlot) {
            if (this.ignoreHotbar.getValue() && invSlot <= 8 || !abstractContainerMenu.getSlot(handlerSlot = this.getHandlerSlot(invSlot)).getItem().isEmpty() || rekitModule.getActiveKit().get(invSlot) != null) continue;
            return handlerSlot;
        }
        for (invSlot = 0; invSlot < 36; ++invSlot) {
            if (this.ignoreHotbar.getValue() && invSlot <= 8 || !abstractContainerMenu.getSlot(handlerSlot = this.getHandlerSlot(invSlot)).getItem().isEmpty()) continue;
            return handlerSlot;
        }
        return -1;
    }

    private int getHandlerSlot(int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return 36 + invSlot;
        }
        if (invSlot >= 9 && invSlot <= 35) {
            return invSlot;
        }
        return -1;
    }

    private boolean isShulkerBox(ItemStack itemStack) {
        BlockItem blockItem;
        Item item;
        return !itemStack.isEmpty() && (item = itemStack.getItem()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock;
    }
}

