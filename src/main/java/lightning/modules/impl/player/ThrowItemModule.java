/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package lightning.modules.impl.player;

import java.util.Map;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@RegisterModule(name="ThrowItem", description="Throw out items that arent belong to your loaded kit. Useful when you need to loot gears during combat", category=Module.Category.PLAYER)
public class ThrowItemModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay between throw actions.", 1, 0, 20);
    public NumberSetting actionsPerTick = new NumberSetting("ActionsPerTick", "Maximum items to drop per pass.", 3, 1, 20);
    public BooleanSetting autoDisable = new BooleanSetting("AutoDisable", "Automatically disables the module when no items are left to throw.", false);
    public BooleanSetting keepKitItems = new BooleanSetting("KeepKitItems", "Keep items that belong to the loaded kit even if in a different slot.", true);
    public BooleanSetting minimumCount = new BooleanSetting("MinimumCount", "Keep matching items only if their stack count reaches minimum count.", false);
    public NumberSetting minCount = new NumberSetting("MinCount", "The minimum stack count to keep an item.", new BooleanSetting.Visibility(this.minimumCount, true), (Number)16, (Number)1, (Number)64);
    public BooleanSetting ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Do not throw items from the hotbar (slots 0-8).", false);
    public BooleanSetting onlyInventory = new BooleanSetting("OnlyInventory", "Only throw items when in regular gameplay or InventoryScreen (not in Chests/Shulkers).", true);
    private int ticks = 0;

    @Override
    public void onEnable() {
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (ThrowItemModule.mc.player == null || ThrowItemModule.mc.level == null || ThrowItemModule.mc.gameMode == null) {
            return;
        }
        if (ThrowItemModule.mc.player.isCreative() || ThrowItemModule.mc.player.isSpectator()) {
            return;
        }
        if (!ThrowItemModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        RekitModule rekitModule = Night.MODULE_MANAGER.getModule(RekitModule.class);
        if (rekitModule == null) {
            return;
        }
        Map<Integer, RekitModule.KitItem> map = rekitModule.getActiveKit();
        if (map == null || map.isEmpty()) {
            return;
        }
        if (rekitModule.isAutoActive() || System.currentTimeMillis() - RekitModule.lastContainerActionMs < 200L) {
            return;
        }
        if (ThrowItemModule.mc.gui.screen() != null && !(ThrowItemModule.mc.gui.screen() instanceof InventoryScreen)) {
            return;
        }
        boolean bl = ThrowItemModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ThrowItemModule.mc.gui.screen() instanceof InventoryScreen);
        boolean bl2 = bl;
        if (this.onlyInventory.getValue() && bl) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        int n = ThrowItemModule.mc.player.containerMenu.containerId;
        int n2 = 0;
        int n3 = this.actionsPerTick.getValue().intValue();
        int n4 = 0;
        for (int i = 0; i < 36; ++i) {
            int n5;
            ItemStack itemStack;
            if (this.ignoreHotbar.getValue() && i <= 8 || (itemStack = ThrowItemModule.mc.player.getInventory().getItem(i)).isEmpty()) continue;
            RekitModule.KitItem kitItem = map.get(i);
            String string = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            boolean bl3 = false;
            if (kitItem != null && kitItem.id != null && string.equalsIgnoreCase(kitItem.id)) {
                bl3 = true;
            } else if (this.keepKitItems.getValue()) {
                for (RekitModule.KitItem kitItem2 : map.values()) {
                    if (kitItem2 == null || kitItem2.id == null || !string.equalsIgnoreCase(kitItem2.id)) continue;
                    bl3 = true;
                    break;
                }
            }
            boolean bl4 = false;
            if (!bl3) {
                bl4 = true;
            } else if (this.minimumCount.getValue() && itemStack.getMaxStackSize() > 1 && itemStack.getCount() < this.minCount.getValue().intValue()) {
                bl4 = true;
            }
            if (!bl4) continue;
            ++n4;
            if (n2 >= n3 || (n5 = this.invToHandlerSlot(i)) < 0) continue;
            this.throwSlot(n, n5);
            ++n2;
        }
        if (n4 == 0 && this.autoDisable.getValue()) {
            this.setToggled(false);
        }
    }

    private void throwSlot(int n, int n2) {
        ThrowItemModule.mc.gameMode.handleContainerInput(n, n2, 1, ContainerInput.THROW, (Player)ThrowItemModule.mc.player);
    }

    private int invToHandlerSlot(int n) {
        Inventory inventory = ThrowItemModule.mc.player.getInventory();
        for (Slot slot : ThrowItemModule.mc.player.containerMenu.slots) {
            if (slot.container != inventory || slot.getContainerSlot() != n) continue;
            return slot.index;
        }
        return -1;
    }
}

