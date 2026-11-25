/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@RegisterModule(name="ChestStealer", description="Automatically steals, dumps, or drops items to and from an open container.", category=Module.Category.PLAYER)
public class ChestStealerModule
extends Module {
    public BooleanSetting steal = new BooleanSetting("Steal", "Move matching items FROM the open container INTO your inventory.", true);
    public ModeSetting stealMode = new ModeSetting("Mode", "WhiteList = steal items IN the list. BlackList = steal items NOT in the list. All = steal everything.", new BooleanSetting.Visibility(this.steal, true), "All", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting stealWhitelist = new WhitelistSetting("List", "Items to compare against.", new BooleanSetting.Visibility(this.steal, true), WhitelistSetting.Type.ITEMS);
    public BooleanSetting dump = new BooleanSetting("Dump", "Move matching items FROM your inventory INTO the open container.", false);
    public ModeSetting dumpMode = new ModeSetting("Mode", "WhiteList = dump items IN the list. BlackList = dump items NOT in the list. All = dump everything.", new BooleanSetting.Visibility(this.dump, true), "WhiteList", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting dumpWhitelist = new WhitelistSetting("List", "Items to compare against.", new BooleanSetting.Visibility(this.dump, true), WhitelistSetting.Type.ITEMS);
    public BooleanSetting drop = new BooleanSetting("Drop", "Throw matching items OUT (Q / THROW).", false);
    public ModeSetting dropMode = new ModeSetting("DropMode", "Mode", "WhiteList = drop items IN the list. BlackList = drop items NOT in the list. All = drop everything.", new BooleanSetting.Visibility(this.drop, true), "All", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting dropWhitelist = new WhitelistSetting("DropList", "List", "Items to compare against for drop.", new BooleanSetting.Visibility(this.drop, true), WhitelistSetting.Type.ITEMS);
    public ModeSetting interact = new ModeSetting("Interact", "Which of YOUR slots Dump/Drop act on: Hotbar (0-8), Inventory (9-35), or Both.", "Both", new String[]{"Hotbar", "Inventory", "Both"});
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay between action passes.", 1, 0, 20);
    public NumberSetting actionsPerTick = new NumberSetting("ActionsPerTick", "Max slot actions per pass.", 5, 1, 36);
    public BooleanSetting buttonMode = new BooleanSetting("ButtonMode", "Adds vanilla Steal/Dump/Drop buttons at the top-right of the container/inventory GUI.", false);
    public BooleanSetting ignoreCustomName = new BooleanSetting("IgnoreCustomName", "Skip any container with a custom (non-vanilla) title such as a shop GUI, except shulker boxes.", false);
    private int ticks = 0;
    public static volatile long lastContainerActionMs = 0L;
    private Task activeTask = Task.NONE;
    private int taskStep = 0;
    private int lastContainerId = -1;
    private int autoStealStep = 0;
    private int autoDumpStep = 0;
    private int autoDropStep = 0;

    @Override
    public void onDisable() {
        this.activeTask = Task.NONE;
        this.taskStep = 0;
        this.autoStealStep = 0;
        this.autoDumpStep = 0;
        this.autoDropStep = 0;
        this.lastContainerId = -1;
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        int processed;
        int containerSlots;
        long now;
        if (ChestStealerModule.mc.player == null || ChestStealerModule.mc.level == null || ChestStealerModule.mc.gameMode == null) {
            return;
        }
        if (ChestStealerModule.mc.player.isCreative()) {
            return;
        }
        if (ChestStealerModule.mc.player.containerMenu == null) {
            return;
        }
        if (!ChestStealerModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        int containerId = ChestStealerModule.mc.player.containerMenu.containerId;
        if (containerId != this.lastContainerId) {
            this.lastContainerId = containerId;
            this.taskStep = 0;
            this.autoStealStep = 0;
            this.autoDumpStep = 0;
            this.autoDropStep = 0;
            this.activeTask = Task.NONE;
        }
        if ((now = System.currentTimeMillis()) - RekitModule.lastContainerActionMs < 200L || now - InventorySorterModule.lastContainerActionMs < 200L) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        int maxActions = this.actionsPerTick.getValue().intValue();
        if (this.activeTask != Task.NONE) {
            boolean inContainer = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
            boolean inInventory = ChestStealerModule.mc.gui.screen() instanceof InventoryScreen;
            if (!inContainer && !inInventory) {
                this.activeTask = Task.NONE;
                this.taskStep = 0;
                return;
            }
            switch (this.activeTask.ordinal()) {
                case 1: {
                    if (inContainer) {
                        int totalContainerSlots = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
                        this.taskStep += this.processStealStep(this.stealMode.getValue(), this.stealWhitelist, maxActions, this.taskStep);
                        if (this.taskStep < totalContainerSlots) break;
                        this.activeTask = Task.NONE;
                        this.taskStep = 0;
                        break;
                    }
                    this.activeTask = Task.NONE;
                    this.taskStep = 0;
                    break;
                }
                case 2: {
                    if (inContainer) {
                        this.taskStep += this.processDumpStep(this.dumpMode.getValue(), this.dumpWhitelist, maxActions, this.taskStep);
                        if (this.taskStep < 36) break;
                        this.activeTask = Task.NONE;
                        this.taskStep = 0;
                        break;
                    }
                    this.activeTask = Task.NONE;
                    this.taskStep = 0;
                    break;
                }
                case 3: {
                    int containerOffset = inContainer ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
                    int totalDropSlots = containerOffset + 36;
                    this.taskStep += this.processDropStep(this.dropMode.getValue(), this.dropWhitelist, maxActions, this.taskStep);
                    if (this.taskStep < totalDropSlots) break;
                    this.activeTask = Task.NONE;
                    this.taskStep = 0;
                }
            }
            return;
        }
        if (this.buttonMode.getValue()) {
            return;
        }
        boolean inContainer = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
        if (inContainer && this.ignoreCustomName.getValue() && this.hasCustomContainerTitle() && !(ChestStealerModule.mc.player.containerMenu instanceof ShulkerBoxMenu)) {
            return;
        }
        if (this.steal.getValue() && inContainer && maxActions > 0) {
            containerSlots = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
            processed = this.processStealStep(this.stealMode.getValue(), this.stealWhitelist, maxActions, this.autoStealStep);
            this.autoStealStep += processed;
            if (this.autoStealStep >= containerSlots) {
                this.autoStealStep = 0;
            }
        }
        if (this.dump.getValue() && inContainer && maxActions > 0) {
            processed = this.processDumpStep(this.dumpMode.getValue(), this.dumpWhitelist, maxActions, this.autoDumpStep);
            this.autoDumpStep += processed;
            if (this.autoDumpStep >= 36) {
                this.autoDumpStep = 0;
            }
        }
        if (this.drop.getValue() && maxActions > 0) {
            containerSlots = inContainer ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
            int totalSlots = containerSlots + 36;
            int dropped = this.processDropStep(this.dropMode.getValue(), this.dropWhitelist, maxActions, this.autoDropStep);
            this.autoDropStep += dropped;
            if (this.autoDropStep >= totalSlots) {
                this.autoDropStep = 0;
            }
        }
    }

    public void triggerSteal() {
        if (this.canAct(true)) {
            this.activeTask = Task.STEAL;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    public void triggerDump() {
        if (this.canAct(true)) {
            this.activeTask = Task.DUMP;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    public void triggerDrop() {
        if (this.canAct(false)) {
            this.activeTask = Task.DROP;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    private boolean hasCustomContainerTitle() {
        Screen screen = ChestStealerModule.mc.gui.screen();
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

    private boolean canAct(boolean requireScreen) {
        if (ChestStealerModule.mc.player == null || ChestStealerModule.mc.level == null || ChestStealerModule.mc.gameMode == null) {
            return false;
        }
        if (ChestStealerModule.mc.player.isCreative()) {
            return false;
        }
        if (ChestStealerModule.mc.player.containerMenu == null) {
            return false;
        }
        if (!ChestStealerModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return false;
        }
        if (requireScreen) {
            boolean inContainer = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
            boolean inInventory = ChestStealerModule.mc.gui.screen() instanceof InventoryScreen;
            if (!inContainer && !inInventory) {
                return false;
            }
        }
        return true;
    }

    private int processStealStep(String filterMode, WhitelistSetting whitelistSetting, int maxActions, int startSlot) {
        int slot;
        if (ChestStealerModule.mc.gui.screen() instanceof InventoryScreen) {
            return 0;
        }
        int containerId = ChestStealerModule.mc.player.containerMenu.containerId;
        int containerSlots = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
        int moved = 0;
        for (slot = startSlot; slot < containerSlots && moved < maxActions; ++slot) {
            ItemStack itemStack = ((Slot)ChestStealerModule.mc.player.containerMenu.slots.get(slot)).getItem();
            if (itemStack.isEmpty() || !this.matches(itemStack, filterMode, whitelistSetting)) continue;
            this.quickMove(containerId, slot);
            ++moved;
        }
        return Math.max(1, slot - startSlot);
    }

    private int processDumpStep(String filterMode, WhitelistSetting whitelistSetting, int maxActions, int startSlot) {
        int invSlot;
        if (ChestStealerModule.mc.gui.screen() instanceof InventoryScreen) {
            return 0;
        }
        int containerId = ChestStealerModule.mc.player.containerMenu.containerId;
        int moved = 0;
        for (invSlot = startSlot; invSlot < 36 && moved < maxActions; ++invSlot) {
            int handlerSlot;
            ItemStack itemStack;
            if (!this.inInteractRange(invSlot) || (itemStack = ChestStealerModule.mc.player.getInventory().getItem(invSlot)).isEmpty() || !this.matches(itemStack, filterMode, whitelistSetting) || (handlerSlot = this.invToHandlerSlot(invSlot)) < 0) continue;
            this.quickMove(containerId, handlerSlot);
            ++moved;
        }
        return Math.max(1, invSlot - startSlot);
    }

    private int processDropStep(String filterMode, WhitelistSetting whitelistSetting, int maxActions, int startSlot) {
        int step;
        int containerId = ChestStealerModule.mc.player.containerMenu.containerId;
        boolean inContainer = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
        int containerSlots = inContainer ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
        int totalSlots = containerSlots + 36;
        int dropped = 0;
        for (step = startSlot; step < totalSlots && dropped < maxActions; ++step) {
            int handlerSlot;
            ItemStack itemStack;
            if (step < containerSlots) {
                ItemStack containerStack = ((Slot)ChestStealerModule.mc.player.containerMenu.slots.get(step)).getItem();
                if (containerStack.isEmpty() || !this.matches(containerStack, filterMode, whitelistSetting)) continue;
                this.throwSlot(containerId, step);
                ++dropped;
                continue;
            }
            int invSlot = step - containerSlots;
            if (!this.inInteractRange(invSlot) || (itemStack = ChestStealerModule.mc.player.getInventory().getItem(invSlot)).isEmpty() || !this.matches(itemStack, filterMode, whitelistSetting) || (handlerSlot = this.invToHandlerSlot(invSlot)) < 0) continue;
            this.throwSlot(containerId, handlerSlot);
            ++dropped;
        }
        return Math.max(1, step - startSlot);
    }

    private void quickMove(int containerId, int slotIndex) {
        ChestStealerModule.mc.gameMode.handleContainerInput(containerId, slotIndex, 0, ContainerInput.QUICK_MOVE, (Player)ChestStealerModule.mc.player);
        lastContainerActionMs = System.currentTimeMillis();
    }

    private void throwSlot(int containerId, int slotIndex) {
        ChestStealerModule.mc.gameMode.handleContainerInput(containerId, slotIndex, 1, ContainerInput.THROW, (Player)ChestStealerModule.mc.player);
        lastContainerActionMs = System.currentTimeMillis();
    }

    private boolean matches(ItemStack itemStack, String filterMode, WhitelistSetting whitelistSetting) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        boolean inWhitelist = whitelistSetting != null && whitelistSetting.isWhitelistContains(itemStack.getItem());
        return switch (filterMode) {
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

    private boolean inInteractRange(int invSlot) {
        return switch (this.interact.getValue()) {
            case "Hotbar" -> {
                if (invSlot <= 8) {
                    yield true;
                }
                yield false;
            }
            case "Inventory" -> {
                if (invSlot >= 9) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    private int invToHandlerSlot(int invSlot) {
        Inventory inventory = ChestStealerModule.mc.player.getInventory();
        for (Slot slot : ChestStealerModule.mc.player.containerMenu.slots) {
            if (slot.container != inventory || slot.getContainerSlot() != invSlot) continue;
            return slot.index;
        }
        return -1;
    }

    @Override
    public String getMetaData() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.steal.getValue()) {
            stringBuilder.append("Steal");
        }
        if (this.dump.getValue()) {
            stringBuilder.append(stringBuilder.length() > 0 ? "/Dump" : "Dump");
        }
        if (this.drop.getValue()) {
            stringBuilder.append(stringBuilder.length() > 0 ? "/Drop" : "Drop");
        }
        return stringBuilder.toString();
    }

    private static enum Task {
        NONE,
        STEAL,
        DUMP,
        DROP;

    }
}

