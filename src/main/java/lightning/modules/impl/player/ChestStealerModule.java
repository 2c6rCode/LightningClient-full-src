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
        int n;
        int n2;
        long l;
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
        int n3 = ChestStealerModule.mc.player.containerMenu.containerId;
        if (n3 != this.lastContainerId) {
            this.lastContainerId = n3;
            this.taskStep = 0;
            this.autoStealStep = 0;
            this.autoDumpStep = 0;
            this.autoDropStep = 0;
            this.activeTask = Task.NONE;
        }
        if ((l = System.currentTimeMillis()) - RekitModule.lastContainerActionMs < 200L || l - InventorySorterModule.lastContainerActionMs < 200L) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        int n4 = this.actionsPerTick.getValue().intValue();
        if (this.activeTask != Task.NONE) {
            boolean bl = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
            boolean bl2 = ChestStealerModule.mc.gui.screen() instanceof InventoryScreen;
            if (!bl && !bl2) {
                this.activeTask = Task.NONE;
                this.taskStep = 0;
                return;
            }
            switch (this.activeTask.ordinal()) {
                case 1: {
                    if (bl) {
                        int n5 = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
                        this.taskStep += this.processStealStep(this.stealMode.getValue(), this.stealWhitelist, n4, this.taskStep);
                        if (this.taskStep < n5) break;
                        this.activeTask = Task.NONE;
                        this.taskStep = 0;
                        break;
                    }
                    this.activeTask = Task.NONE;
                    this.taskStep = 0;
                    break;
                }
                case 2: {
                    if (bl) {
                        this.taskStep += this.processDumpStep(this.dumpMode.getValue(), this.dumpWhitelist, n4, this.taskStep);
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
                    boolean bl3 = bl;
                    int n6 = bl3 ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
                    int n7 = n6 + 36;
                    this.taskStep += this.processDropStep(this.dropMode.getValue(), this.dropWhitelist, n4, this.taskStep);
                    if (this.taskStep < n7) break;
                    this.activeTask = Task.NONE;
                    this.taskStep = 0;
                }
            }
            return;
        }
        if (this.buttonMode.getValue()) {
            return;
        }
        boolean bl = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
        boolean bl4 = bl;
        if (bl && this.ignoreCustomName.getValue() && this.hasCustomContainerTitle() && !(ChestStealerModule.mc.player.containerMenu instanceof ShulkerBoxMenu)) {
            return;
        }
        if (this.steal.getValue() && bl && n4 > 0) {
            n2 = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
            n = this.processStealStep(this.stealMode.getValue(), this.stealWhitelist, n4, this.autoStealStep);
            this.autoStealStep += n;
            if (this.autoStealStep >= n2) {
                this.autoStealStep = 0;
            }
        }
        if (this.dump.getValue() && bl && n4 > 0) {
            n = this.processDumpStep(this.dumpMode.getValue(), this.dumpWhitelist, n4, this.autoDumpStep);
            this.autoDumpStep += n;
            if (this.autoDumpStep >= 36) {
                this.autoDumpStep = 0;
            }
        }
        if (this.drop.getValue() && n4 > 0) {
            n2 = bl ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
            n = n2 + 36;
            int n8 = this.processDropStep(this.dropMode.getValue(), this.dropWhitelist, n4, this.autoDropStep);
            this.autoDropStep += n8;
            if (this.autoDropStep >= n) {
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

    private boolean canAct(boolean bl) {
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
        if (bl) {
            boolean bl2 = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
            boolean bl3 = ChestStealerModule.mc.gui.screen() instanceof InventoryScreen;
            if (!bl2 && !bl3) {
                return false;
            }
        }
        return true;
    }

    private int processStealStep(String string, WhitelistSetting whitelistSetting, int n, int n2) {
        int n3;
        if (ChestStealerModule.mc.gui.screen() instanceof InventoryScreen) {
            return 0;
        }
        int n4 = ChestStealerModule.mc.player.containerMenu.containerId;
        int n5 = ChestStealerModule.mc.player.containerMenu.slots.size() - 36;
        int n6 = 0;
        for (n3 = n2; n3 < n5 && n6 < n; ++n3) {
            ItemStack itemStack = ((Slot)ChestStealerModule.mc.player.containerMenu.slots.get(n3)).getItem();
            if (itemStack.isEmpty() || !this.matches(itemStack, string, whitelistSetting)) continue;
            this.quickMove(n4, n3);
            ++n6;
        }
        return Math.max(1, n3 - n2);
    }

    private int processDumpStep(String string, WhitelistSetting whitelistSetting, int n, int n2) {
        int n3;
        if (ChestStealerModule.mc.gui.screen() instanceof InventoryScreen) {
            return 0;
        }
        int n4 = ChestStealerModule.mc.player.containerMenu.containerId;
        int n5 = 0;
        for (n3 = n2; n3 < 36 && n5 < n; ++n3) {
            int n6;
            ItemStack itemStack;
            if (!this.inInteractRange(n3) || (itemStack = ChestStealerModule.mc.player.getInventory().getItem(n3)).isEmpty() || !this.matches(itemStack, string, whitelistSetting) || (n6 = this.invToHandlerSlot(n3)) < 0) continue;
            this.quickMove(n4, n6);
            ++n5;
        }
        return Math.max(1, n3 - n2);
    }

    private int processDropStep(String string, WhitelistSetting whitelistSetting, int n, int n2) {
        int n3;
        int n4 = ChestStealerModule.mc.player.containerMenu.containerId;
        boolean bl = ChestStealerModule.mc.gui.screen() instanceof AbstractContainerScreen && !(ChestStealerModule.mc.gui.screen() instanceof InventoryScreen);
        int n5 = bl ? ChestStealerModule.mc.player.containerMenu.slots.size() - 36 : 0;
        int n6 = n5 + 36;
        int n7 = 0;
        for (n3 = n2; n3 < n6 && n7 < n; ++n3) {
            int n8;
            ItemStack itemStack;
            if (n3 < n5) {
                ItemStack itemStack2 = ((Slot)ChestStealerModule.mc.player.containerMenu.slots.get(n3)).getItem();
                if (itemStack2.isEmpty() || !this.matches(itemStack2, string, whitelistSetting)) continue;
                this.throwSlot(n4, n3);
                ++n7;
                continue;
            }
            int n9 = n3 - n5;
            if (!this.inInteractRange(n9) || (itemStack = ChestStealerModule.mc.player.getInventory().getItem(n9)).isEmpty() || !this.matches(itemStack, string, whitelistSetting) || (n8 = this.invToHandlerSlot(n9)) < 0) continue;
            this.throwSlot(n4, n8);
            ++n7;
        }
        return Math.max(1, n3 - n2);
    }

    private void quickMove(int n, int n2) {
        ChestStealerModule.mc.gameMode.handleContainerInput(n, n2, 0, ContainerInput.QUICK_MOVE, (Player)ChestStealerModule.mc.player);
        lastContainerActionMs = System.currentTimeMillis();
    }

    private void throwSlot(int n, int n2) {
        ChestStealerModule.mc.gameMode.handleContainerInput(n, n2, 1, ContainerInput.THROW, (Player)ChestStealerModule.mc.player);
        lastContainerActionMs = System.currentTimeMillis();
    }

    private boolean matches(ItemStack itemStack, String string, WhitelistSetting whitelistSetting) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        boolean bl = whitelistSetting != null && whitelistSetting.isWhitelistContains(itemStack.getItem());
        return switch (string) {
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

    private boolean inInteractRange(int n) {
        return switch (this.interact.getValue()) {
            case "Hotbar" -> {
                if (n <= 8) {
                    yield true;
                }
                yield false;
            }
            case "Inventory" -> {
                if (n >= 9) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    private int invToHandlerSlot(int n) {
        Inventory inventory = ChestStealerModule.mc.player.getInventory();
        for (Slot slot : ChestStealerModule.mc.player.containerMenu.slots) {
            if (slot.container != inventory || slot.getContainerSlot() != n) continue;
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

