/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.AxeItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 */
package lightning.modules.impl.combat;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.SuicideModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.modules.impl.player.MultiTaskModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@RegisterModule(name="AutoTotem", description="Automatically puts a specified item in your offhand slot.", category=Module.Category.COMBAT)
public class AutoTotemModule
extends Module {
    public ModeSetting item = new ModeSetting("Item", "The item that will be placed in your offhand slot when safety conditions are met.", "Totem", new String[]{"Totem", "Crystal", "Gapple"});
    public NumberSetting health = new NumberSetting("Health", "The health at which a totem will be prioritized.", new ModeSetting.Visibility(this.item, "Crystal", "Gapple"), (Number)16, (Number)0, (Number)36);
    public BooleanSetting elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
    public NumberSetting fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", Float.valueOf(20.0f), Float.valueOf(0.0f), Float.valueOf(80.0f));
    public BooleanSetting useGapple = new BooleanSetting("UseGapple", "Switches to a golden apple in your offhand based on main hand items when health is safe.", true);
    public BooleanSetting gappleSword = new BooleanSetting("Sword", "Switches to a gapple when holding a sword or axe in main hand.", new BooleanSetting.Visibility(this.useGapple, true), true);
    public BooleanSetting gapplePickaxe = new BooleanSetting("Pickaxe", "Switches to a gapple when holding a pickaxe in main hand.", new BooleanSetting.Visibility(this.useGapple, true), true);
    public BooleanSetting gappleTotem = new BooleanSetting("Totem", "Switches to a gapple when holding a totem in main hand.", new BooleanSetting.Visibility(this.useGapple, true), false);
    public BooleanSetting lethalOverride = new BooleanSetting("LethalOverride", "Overrides any necessity for a totem when gappling.", new BooleanSetting.Visibility(this.useGapple, true), false);
    public BooleanSetting noTotemGap = new BooleanSetting("NoTotemGap", "Swap your gap into offhand when no totems left", false);
    public BooleanSetting version117 = new BooleanSetting("1.17+", "1.17+ uses the SWAP inventory action, while having it off uses inventory clicks.", true);
    public BooleanSetting smartMine = new BooleanSetting("SmartMine", "Switches to a crystal whenever you start mining and a totem when you aren't mining.", new ModeSetting.Visibility(this.item, "Crystal"), false);
    public BooleanSetting antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
    public NumberSetting maceRange = new NumberSetting("MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", new BooleanSetting.Visibility(this.antiMace, true), (Number)Float.valueOf(12.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(24.0f));
    public BooleanSetting debug = new BooleanSetting("Debug", "Logs why you died and why the totem swap failed at that moment.", false);
    private int totemCount = 0;
    private int gappleStagedSlot = -1;
    private boolean wasAlive = true;
    private String lastReason = "";

    @Override
    public void onDisable() {
        this.gappleStagedSlot = -1;
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onPlayerPop(PlayerPopEvent playerPopEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (playerPopEvent.getPlayer() == AutoTotemModule.mc.player && !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            if (this.debug.getValue()) {
                this.log("TOTEM POPPED (survived). health+absorb=" + (AutoTotemModule.mc.player.getHealth() + AutoTotemModule.mc.player.getAbsorptionAmount()) + ", totemsLeft=" + this.totemCount + ", offhandNow=" + this.itemName(AutoTotemModule.mc.player.getOffhandItem().getItem()));
            }
            this.updateTotem();
        }
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        this.updateTotem();
    }

    private void updateTotem() {
        AutoShopModule autoShopModule;
        int targetSlot;
        int emptySlot;
        ItemStack carried;
        if (AutoTotemModule.mc.player == null || AutoTotemModule.mc.level == null) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) {
            this.reason("skip: PingBypass active");
            return;
        }
        Screen screen = AutoTotemModule.mc.gui.screen();
        boolean inInventory = screen instanceof InventoryScreen;
        boolean inContainer = screen instanceof AbstractContainerScreen;
        if (inContainer && !AutoTotemModule.mc.player.containerMenu.getCarried().isEmpty()) {
            this.reason("skip: cursor is holding an item in an open container");
            return;
        }
        Item item = this.getItem();
        if (item == null) {
            this.reason("skip: getItem() returned null (no valid target item)");
            return;
        }
        if (!AutoTotemModule.mc.player.containerMenu.getCarried().isEmpty()) {
            carried = AutoTotemModule.mc.player.containerMenu.getCarried();
            if (carried.getItem() == item) {
                int freeSlot;
                InventoryUtils.click(45, 0, ContainerInput.PICKUP);
                if (!inInventory && !AutoTotemModule.mc.player.containerMenu.getCarried().isEmpty() && (freeSlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END)) != -1) {
                    InventoryUtils.click(InventoryUtils.indexToSlot(freeSlot), 0, ContainerInput.PICKUP);
                }
                if (this.debug.getValue()) {
                    this.log("RECOVERED stuck cursor -> placed " + this.itemName(item) + " into offhand");
                }
                return;
            }
            if (!inInventory) {
                emptySlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
                if (emptySlot != -1) {
                    InventoryUtils.click(InventoryUtils.indexToSlot(emptySlot), 0, ContainerInput.PICKUP);
                }
                if (!AutoTotemModule.mc.player.containerMenu.getCarried().isEmpty()) {
                    this.reason("skip: cursor is holding an item and could not clear it");
                    return;
                }
            }
        }
        if (item == Items.TOTEM_OF_UNDYING && Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && Night.MODULE_MANAGER.getModule(SuicideModule.class).offhandOverride.getValue()) {
            if (AutoTotemModule.mc.player.getOffhandItem().isEmpty()) {
                this.reason("skip: Suicide offhandOverride, offhand already empty");
                return;
            }
            targetSlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
            if (targetSlot == -1) {
                this.reason("Suicide override: no empty slot to move offhand item into");
            }
        } else {
            if (AutoTotemModule.mc.player.getOffhandItem().getItem() == item) {
                this.reason("ok: offhand already holds target (" + this.itemName(item) + ")");
                return;
            }
            if (this.gappleStagedSlot != -1 && item == Items.TOTEM_OF_UNDYING) {
                if (AutoTotemModule.mc.player.getInventory().getItem(this.gappleStagedSlot).getItem() == Items.TOTEM_OF_UNDYING) {
                    targetSlot = this.gappleStagedSlot;
                } else {
                    targetSlot = InventoryUtils.findInventory(item);
                    if (targetSlot == -1) {
                        targetSlot = InventoryUtils.find(item);
                    }
                }
                this.gappleStagedSlot = -1;
            } else if (item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.GOLDEN_APPLE) {
                targetSlot = InventoryUtils.findHotbar(item);
                if (targetSlot == -1) {
                    targetSlot = InventoryUtils.findInventory(item);
                }
                if (targetSlot != -1) {
                    this.gappleStagedSlot = targetSlot;
                }
            } else {
                targetSlot = InventoryUtils.findInventory(item);
                if (targetSlot == -1) {
                    targetSlot = InventoryUtils.find(item);
                }
            }
            if (targetSlot == -1) {
                if (item == Items.TOTEM_OF_UNDYING && this.hasItem(Items.TOTEM_OF_UNDYING)) {
                    targetSlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
                    if (targetSlot == -1) {
                        this.reason("FAIL: have a totem but no empty slot to stage the swap");
                    }
                } else {
                    this.reason("FAIL: target " + this.itemName(item) + " NOT FOUND in inventory (none left)");
                    return;
                }
            }
        }
        if (targetSlot == -1) {
            this.reason("FAIL: no usable slot resolved (see previous reason)");
            return;
        }
        MultiTaskModule multiTask = Night.MODULE_MANAGER.getModule(MultiTaskModule.class);
        boolean allowMultiTask = multiTask != null && multiTask.isToggled() && multiTask.autoTotem.getValue();
        if (!allowMultiTask && AutoTotemModule.mc.player.isUsingItem()) {
            AutoTotemModule.mc.gameMode.releaseUsingItem((Player)AutoTotemModule.mc.player);
        }
        if ((autoShopModule = Night.MODULE_MANAGER.getModule(AutoShopModule.class)).antiGhostTotem.getValue()) {
            if (autoShopModule.isRunning()) {
                autoShopModule.abort("AntiGhostTotem: shop cancelled so the totem can be swapped now.");
            }
        }
        if (this.version117.getValue()) {
            InventoryUtils.swap("Swap", targetSlot, 40);
        } else {
            int freeSlot;
            InventoryUtils.swap("Pickup", targetSlot, 45);
            if (!inInventory && !AutoTotemModule.mc.player.containerMenu.getCarried().isEmpty() && (freeSlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END)) != -1) {
                InventoryUtils.click(InventoryUtils.indexToSlot(freeSlot), 0, ContainerInput.PICKUP);
            }
        }
        if (this.debug.getValue()) {
            this.log("SWAP -> offhand: " + this.itemName(item) + " (from slot " + targetSlot + ") [mode=" + (this.version117.getValue() ? "1.17+ (Swap)" : "Clicks (Pickup)") + "]");
        }
        this.lastReason = "";
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (AutoTotemModule.mc.player == null || AutoTotemModule.mc.level == null) {
            return;
        }
        this.totemCount = AutoTotemModule.mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING) + (!AutoTotemModule.mc.player.containerMenu.getCarried().isEmpty() && AutoTotemModule.mc.player.containerMenu.getCarried().getItem() == Items.TOTEM_OF_UNDYING ? AutoTotemModule.mc.player.containerMenu.getCarried().getCount() : 0);
        boolean isAlive = AutoTotemModule.mc.player.getHealth() > 0.0f && !AutoTotemModule.mc.player.isDeadOrDying();
        if (this.debug.getValue() && this.wasAlive && !isAlive) {
            this.logDeath();
        }
        this.wasAlive = isAlive;
    }

    private void logDeath() {
        String deathCause;
        try {
            deathCause = AutoTotemModule.mc.player.getCombatTracker().getDeathMessage().getString();
        }
        catch (Throwable throwable) {
            deathCause = "(unknown)";
        }
        ItemStack offhandStack = AutoTotemModule.mc.player.getOffhandItem();
        boolean hadTotem = offhandStack.getItem() == Items.TOTEM_OF_UNDYING;
        this.log("================ DEATH ================");
        this.log("Cause: " + deathCause);
        this.log("Offhand at death: " + (offhandStack.isEmpty() ? "EMPTY" : this.itemName(offhandStack.getItem())) + (hadTotem ? " (totem WAS present!)" : ""));
        this.log("Totems in inventory at death: " + this.totemCount);
        this.log("Health+Absorb at death: " + (AutoTotemModule.mc.player.getHealth() + AutoTotemModule.mc.player.getAbsorptionAmount()));
        this.log("needsTotem()=" + this.needsTotem());
        this.log("Item mode=" + this.item.getValue() + ", target now=" + this.itemName(this.getItem()));
        this.log("Last swap reason: " + (this.lastReason.isEmpty() ? "(none)" : this.lastReason));
        if (hadTotem) {
            this.log("=> A totem WAS in offhand but you still died (one-shot exceeding totem heal, or totem pop not registered before death).");
        } else if (this.totemCount == 0) {
            this.log("=> No totems left in inventory -- nothing to place.");
        } else {
            this.log("=> Totems available but offhand didn't have one -- see 'Last swap reason' above for why the swap didn't happen.");
        }
        this.log("=======================================");
    }

    private Item getItem() {
        SpeedMineModule speedMineModule;
        boolean hasTotem = this.hasItem(Items.TOTEM_OF_UNDYING);
        Item gappleItem = this.hasItem(Items.ENCHANTED_GOLDEN_APPLE) ? Items.ENCHANTED_GOLDEN_APPLE : (this.hasItem(Items.GOLDEN_APPLE) ? Items.GOLDEN_APPLE : null);
        boolean hasGapple = gappleItem != null;
        boolean holdingWeapon = AutoTotemModule.mc.player.getMainHandItem().is(ItemTags.SWORDS) || AutoTotemModule.mc.player.getMainHandItem().getItem() instanceof AxeItem;
        boolean holdingPickaxe = AutoTotemModule.mc.player.getMainHandItem().is(ItemTags.PICKAXES);
        boolean holdingTotem = AutoTotemModule.mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING);
        boolean shouldUseGapple = this.gappleSword.getValue() && holdingWeapon || this.gapplePickaxe.getValue() && holdingPickaxe || this.gappleTotem.getValue() && holdingTotem;
        if (this.useGapple.getValue() && AutoTotemModule.mc.options.keyUse.isDown() && shouldUseGapple && hasGapple && (this.lethalOverride.getValue() || !this.needsTotem() || !hasTotem)) {
            return gappleItem;
        }
        if (this.needsTotem()) {
            if (hasTotem) {
                return Items.TOTEM_OF_UNDYING;
            }
            if (this.noTotemGap.getValue() && hasGapple) {
                return gappleItem;
            }
        }
        if (!(!hasTotem || !this.item.getValue().equalsIgnoreCase("Crystal") || !this.smartMine.getValue() || (speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class)).getPrimary() != null && speedMineModule.getPrimary().isMining() || speedMineModule.getSecondary() != null && speedMineModule.getSecondary().isMining())) {
            return Items.TOTEM_OF_UNDYING;
        }
        switch (this.item.getValue()) {
            case "Crystal": {
                if (!this.hasItem(Items.END_CRYSTAL)) break;
                return Items.END_CRYSTAL;
            }
            case "Gapple": {
                if (!hasGapple) break;
                return gappleItem;
            }
            default: {
                if (!hasTotem) break;
                return Items.TOTEM_OF_UNDYING;
            }
        }
        if (hasTotem) {
            return Items.TOTEM_OF_UNDYING;
        }
        if (this.noTotemGap.getValue() && hasGapple) {
            return gappleItem;
        }
        return AutoTotemModule.mc.player.getOffhandItem().getItem();
    }

    private boolean needsTotem() {
        if (Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && Night.MODULE_MANAGER.getModule(SuicideModule.class).offhandOverride.getValue()) {
            return false;
        }
        if (AutoTotemModule.mc.player.getHealth() + AutoTotemModule.mc.player.getAbsorptionAmount() <= this.health.getValue().floatValue()) {
            return true;
        }
        if (AutoTotemModule.mc.player.fallDistance > (double)this.fallDistance.getValue().floatValue()) {
            return true;
        }
        if (this.elytraCheck.getValue() && AutoTotemModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            return true;
        }
        return this.antiMace.getValue() && AutoTotemModule.mc.level.players().stream().anyMatch(abstractClientPlayer -> abstractClientPlayer != AutoTotemModule.mc.player && !Night.FRIEND_MANAGER.contains(abstractClientPlayer.getName().getString()) && AutoTotemModule.mc.player.distanceToSqr((Entity)abstractClientPlayer) <= (double)Mth.square((float)this.maceRange.getValue().floatValue()) && abstractClientPlayer.fallDistance >= 1.5 && abstractClientPlayer.getMainHandItem().getItem().equals(Items.MACE));
    }

    private boolean hasItem(Item item) {
        return InventoryUtils.find(item) != -1 || AutoTotemModule.mc.player.getOffhandItem().getItem() == item || !AutoTotemModule.mc.player.containerMenu.getCarried().isEmpty() && AutoTotemModule.mc.player.containerMenu.getCarried().getItem() == item;
    }

    private void reason(String msg) {
        if (!this.debug.getValue()) {
            return;
        }
        if (msg.equals(this.lastReason)) {
            return;
        }
        this.lastReason = msg;
        Night.CHAT_MANAGER.tagged(msg, this.getName());
    }

    private void log(String msg) {
        Night.CHAT_MANAGER.tagged(msg, this.getName());
    }

    private String itemName(Item item) {
        if (item == null || item == Items.AIR) {
            return "none";
        }
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.totemCount);
    }
}

