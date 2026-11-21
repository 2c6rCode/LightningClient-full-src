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
        int n;
        Object object;
        int n2;
        AbstractContainerMenu abstractContainerMenu = InventorySorterModule.mc.player.containerMenu;
        if (!abstractContainerMenu.getCarried().isEmpty()) {
            ++this.cursorWaitTicks;
            if (this.cursorWaitTicks >= 20) {
                this.cursorWaitTicks = 0;
                int n3 = this.findEmptySlot(abstractContainerMenu);
                if (n3 != -1) {
                    this.click(n3, 0, ContainerInput.PICKUP);
                }
            }
            return false;
        }
        this.cursorWaitTicks = 0;
        HashMap<String, List<Integer>> hashMap = new HashMap<>();
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack;
            if (this.ignoreHotbar.getValue() && i <= 8 || (itemStack = abstractContainerMenu.getSlot(n2 = this.getHandlerSlot(i)).getItem()).isEmpty() || this.isShulkerBox(itemStack) || itemStack.getCount() >= itemStack.getItem().getDefaultMaxStackSize() || this.isCorrectItem(itemStack, rekitModule.getActiveKit().get(i))) continue;
            String key = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            hashMap.computeIfAbsent(key, string -> new ArrayList<>()).add(n2);
        }
        for (List<Integer> slotList : hashMap.values()) {
            if (slotList.size() <= 1) continue;
            for (n2 = 0; n2 < slotList.size(); ++n2) {
                int n4 = slotList.get(n2);
                ItemStack stackA = abstractContainerMenu.getSlot(n4).getItem();
                for (n = n2 + 1; n < slotList.size(); ++n) {
                    int n5 = slotList.get(n);
                    ItemStack itemStack = abstractContainerMenu.getSlot(n5).getItem();
                    if (!ItemStack.isSameItemSameComponents(stackA, itemStack)) continue;
                    this.atomicSwap(n5, n4);
                    return true;
                }
            }
        }
        for (int i = 0; i < 36; ++i) {
            int n6;
            RekitModule.KitItem kitItem;
            int n7;
            if (this.ignoreHotbar.getValue() && i <= 8 || this.isShulkerBox((ItemStack)(object = abstractContainerMenu.getSlot(n7 = this.getHandlerSlot(i)).getItem())) || this.isCorrectItem((ItemStack)object, kitItem = rekitModule.getActiveKit().get(i)) || (n6 = this.findItemForKit(abstractContainerMenu, rekitModule, kitItem)) == -1) continue;
            n = this.getHandlerSlot(n6);
            if (i <= 8 && n6 >= 9) {
                this.click(n, i, ContainerInput.SWAP);
                return true;
            }
            if (n6 <= 8 && i >= 9) {
                this.click(n7, n6, ContainerInput.SWAP);
                return true;
            }
            this.atomicSwap(n, n7);
            return true;
        }
        return false;
    }

    private void atomicSwap(int n, int n2) {
        this.click(n, 0, ContainerInput.PICKUP);
        this.click(n2, 0, ContainerInput.PICKUP);
        this.click(n, 0, ContainerInput.PICKUP);
    }

    private void click(int n, int n2, ContainerInput containerInput) {
        lastContainerActionMs = System.currentTimeMillis();
        InventorySorterModule.mc.gameMode.handleContainerInput(InventorySorterModule.mc.player.containerMenu.containerId, n, n2, containerInput, (Player)InventorySorterModule.mc.player);
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
            RekitModule.KitItem kitItem2;
            int n;
            ItemStack itemStack;
            if (this.ignoreHotbar.getValue() && i <= 8 || (itemStack = abstractContainerMenu.getSlot(n = this.getHandlerSlot(i)).getItem()).isEmpty() || this.isShulkerBox(itemStack) || itemStack.getItem() != item || this.isCorrectItem(itemStack, kitItem2 = rekitModule.getActiveKit().get(i))) continue;
            return i;
        }
        return -1;
    }

    private int findEmptySlot(AbstractContainerMenu abstractContainerMenu) {
        int n;
        int n2;
        RekitModule rekitModule = Night.MODULE_MANAGER.getModule(RekitModule.class);
        for (n2 = 0; n2 < 36; ++n2) {
            if (this.ignoreHotbar.getValue() && n2 <= 8 || !abstractContainerMenu.getSlot(n = this.getHandlerSlot(n2)).getItem().isEmpty() || rekitModule.getActiveKit().get(n2) != null) continue;
            return n;
        }
        for (n2 = 0; n2 < 36; ++n2) {
            if (this.ignoreHotbar.getValue() && n2 <= 8 || !abstractContainerMenu.getSlot(n = this.getHandlerSlot(n2)).getItem().isEmpty()) continue;
            return n;
        }
        return -1;
    }

    private int getHandlerSlot(int n) {
        if (n >= 0 && n <= 8) {
            return 36 + n;
        }
        if (n >= 9 && n <= 35) {
            return n;
        }
        return -1;
    }

    private boolean isShulkerBox(ItemStack itemStack) {
        BlockItem blockItem;
        Item item;
        return !itemStack.isEmpty() && (item = itemStack.getItem()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock;
    }
}

