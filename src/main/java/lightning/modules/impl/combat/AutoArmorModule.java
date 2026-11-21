/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.EquipmentSlot$Type
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.ExperienceBottleItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.ItemAttributeModifiers
 *  net.minecraft.world.item.equipment.Equippable
 */
package lightning.modules.impl.combat;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

@RegisterModule(name="AutoArmor", description="Automatically equips the best armor.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class AutoArmorModule
extends Module {
    public ModeSetting health = new ModeSetting("Health", "The health priority to apply.", "Highest", new String[]{"Highest", "Lowest", "Any"});
    public BooleanSetting elytraPriority = new BooleanSetting("ElytraPriority", "Prioritizes elytra over armor pieces.", true);
    public BooleanSetting preserve = new BooleanSetting("Preserve", "Preserve low health armor to avoid it from breaking.", false);
    public NumberSetting preserveHealth = new NumberSetting("PreserveHealth", "The minimum health of armor to preserve it.", new BooleanSetting.Visibility(this.preserve, true), (Number)Float.valueOf(20.0f), (Number)Float.valueOf(10.0f), (Number)Float.valueOf(50.0f));
    public NumberSetting safeRange = new NumberSetting("SafeRange", "Range to check for nearby players before re-equipping preserved armor.", new BooleanSetting.Visibility(this.preserve, true), (Number)0, (Number)0, (Number)10);
    public BooleanSetting elytra = new BooleanSetting("Elytra", "Equips an elytra instead of a chestplate.", false);
    public BindSetting elytraBind = new BindSetting("ElytraBind", "Keybind that toggles Elytra on/off.", 0).disableHoldModes();
    public BooleanSetting smartElytra = new BooleanSetting("SmartElytra", "Chooses when to enable elytra in a more convenient way.", false);
    public ModeSetting headMode = new ModeSetting("HeadMode", "Which protection enchant Head armor is ranked by.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting chestMode = new ModeSetting("ChestMode", "Which protection enchant Chest armor is ranked by.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting legsMode = new ModeSetting("LegsMode", "Which protection enchant Legs armor is ranked by.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting feetMode = new ModeSetting("FeetMode", "Which protection enchant Feet armor is ranked by.", "Prot", new String[]{"Blast", "Prot"});
    private int ticks = 0;

    @SubscribeEvent
    public void onElytraBind(KeyInputEvent keyInputEvent) {
        if (this.shouldRunOnProxy() || this.getNull() || AutoArmorModule.mc.gui != null && AutoArmorModule.mc.gui.screen() != null) {
            return;
        }
        if (this.elytraBind.getValue() != 0 && keyInputEvent.getKey() == this.elytraBind.getValue() && this.elytraBind.getMode().equals("Bind")) {
            this.elytra.setValue(!this.elytra.getValue());
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent keyInputEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (this.getNull() || !this.smartElytra.getValue()) {
            return;
        }
        if (!(keyInputEvent.getKey() != 32 || this.elytra.getValue() || AutoArmorModule.mc.player.onGround() || EntityUtils.isInWeb((Entity)AutoArmorModule.mc.player))) {
            this.elytra.setValue(true);
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (AutoArmorModule.mc.player == null || AutoArmorModule.mc.level == null) {
            return;
        }
        if (this.smartElytra.getValue() && this.elytra.getValue() && (AutoArmorModule.mc.player.onGround() && !(AutoArmorModule.mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) || EntityUtils.isInWeb((Entity)AutoArmorModule.mc.player))) {
            this.elytra.setValue(false);
        }
        if (this.ticks <= 0) {
            if (InventoryUtils.inInventoryScreen()) {
                return;
            }
            this.update(EquipmentSlot.HEAD, 5);
            if (!this.elytra.getValue() || !this.elytraPriority.getValue() || AutoArmorModule.mc.player.getInventory().getItem(38).getItem() != Items.ELYTRA) {
                this.update(EquipmentSlot.CHEST, 6);
            }
            this.update(EquipmentSlot.LEGS, 7);
            this.update(EquipmentSlot.FEET, 8);
        }
        --this.ticks;
    }

    private boolean isTierDiamondOrNetherite(ItemStack itemStack) {
        String string = itemStack.getItem().toString().toLowerCase();
        return string.contains("diamond") || string.contains("netherite");
    }

    private boolean isValidCombatArmor(ItemStack itemStack, String string) {
        if (!this.isArmor(itemStack)) {
            return false;
        }
        return this.isTierDiamondOrNetherite(itemStack) || this.getEnchantLevel(itemStack, string) > 0;
    }

    private void update(EquipmentSlot equipmentSlot, int n) {
        int n2 = this.findElytra();
        boolean bl = this.elytra.getValue() && equipmentSlot == EquipmentSlot.CHEST;
        boolean bl2 = bl;
        int n3 = equipmentSlot == EquipmentSlot.HEAD ? 39 : (equipmentSlot == EquipmentSlot.CHEST ? 38 : (equipmentSlot == EquipmentSlot.LEGS ? 37 : 36));
        int n4 = bl ? n2 : this.findArmor(equipmentSlot);
        int n5 = bl ? this.compareElytra(38, n4) : this.compare(n3, n4, true, equipmentSlot);
        int n6 = n5;
        if (n4 != -1 && n5 != n3) {
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, n, 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, InventoryUtils.indexToSlot(n4), 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, n, 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            this.ticks = 2 + Night.SERVER_MANAGER.getPingDelay();
        }
    }

    private int compare(int n, int n2, boolean bl, EquipmentSlot equipmentSlot) {
        if (n2 == -1) {
            return n;
        }
        if (!this.isArmor(AutoArmorModule.mc.player.getInventory().getItem(n))) {
            return n2;
        }
        String string = switch (equipmentSlot) {
            case EquipmentSlot.HEAD -> this.headMode.getValue();
            case EquipmentSlot.CHEST -> this.chestMode.getValue();
            case EquipmentSlot.LEGS -> this.legsMode.getValue();
            default -> this.feetMode.getValue();
        };
        string = string.equalsIgnoreCase("Blast") ? "blast_protection" : "protection";
        ItemStack itemStack = AutoArmorModule.mc.player.getInventory().getItem(n);
        ItemStack itemStack2 = AutoArmorModule.mc.player.getInventory().getItem(n2);
        if (this.getEnchantLevel(itemStack, string) < this.getEnchantLevel(itemStack2, string)) {
            return n2;
        }
        if (this.preserve.getValue()) {
            boolean bl2 = this.isSafe();
            if (bl2) {
                if (this.getDurability(n2) < this.preserveHealth.getValue().floatValue() && this.getDurability(n) >= this.preserveHealth.getValue().floatValue() && this.isValidCombatArmor(itemStack2, string)) {
                    return n2;
                }
            } else if (this.getDurability(n) < this.preserveHealth.getValue().floatValue() && this.isValidCombatArmor(itemStack2, string)) {
                return this.getDurability(n) < this.getDurability(n2) ? n2 : n;
            }
        }
        if (!bl) {
            if (this.health.getValue().equals("Highest") && this.getDurability(n) < this.getDurability(n2)) {
                return n2;
            }
            if (this.health.getValue().equals("Lowest") && this.getDurability(n) > this.getDurability(n2)) {
                return n2;
            }
        }
        return n;
    }

    private boolean isSafe() {
        if (this.safeRange.getValue().doubleValue() <= 0.0) {
            return false;
        }
        if (AutoArmorModule.mc.level == null || AutoArmorModule.mc.player == null) {
            return false;
        }
        double d = this.safeRange.getValue().doubleValue() * this.safeRange.getValue().doubleValue();
        for (Player player : AutoArmorModule.mc.level.players()) {
            if (player == AutoArmorModule.mc.player || player.isSpectator() || !player.isAlive() || Night.FRIEND_MANAGER != null && Night.FRIEND_MANAGER.contains(player.getName().getString()) || !(AutoArmorModule.mc.player.distanceToSqr((Entity)player) <= d)) continue;
            return false;
        }
        return true;
    }

    private int compareElytra(int n, int n2) {
        if (n2 == -1) {
            return n;
        }
        if (AutoArmorModule.mc.player.getInventory().getItem(n).getItem() != Items.ELYTRA) {
            return n2;
        }
        if (this.health.getValue().equals("Highest") && this.getDurability(n) < this.getDurability(n2)) {
            return n2;
        }
        if (this.health.getValue().equals("Lowest") && this.getDurability(n) > this.getDurability(n2)) {
            return n2;
        }
        return n;
    }

    private int findArmor(EquipmentSlot equipmentSlot) {
        int n = -1;
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.INVENTORY_END; ++i) {
            ItemStack itemStack = AutoArmorModule.mc.player.getInventory().getItem(i);
            if (!this.isArmor(itemStack) || !this.getSlotType(itemStack).equals((Object)equipmentSlot)) continue;
            n = this.compare(i, n, false, equipmentSlot);
        }
        return n;
    }

    private int findElytra() {
        int n = -1;
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.INVENTORY_END; ++i) {
            ItemStack itemStack = AutoArmorModule.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() != Items.ELYTRA) continue;
            n = this.compareElytra(i, n);
        }
        return n;
    }

    private int getEnchantLevel(ItemStack itemStack, String string) {
        for (Holder holder : itemStack.getEnchantments().keySet()) {
            if (!holder.getRegisteredName().replace("minecraft:", "").equals(string)) continue;
            return itemStack.getEnchantments().getLevel(holder);
        }
        return 0;
    }

    private float getDurability(int n) {
        ItemStack itemStack = AutoArmorModule.mc.player.getInventory().getItem(n);
        return (float)(itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f / (float)itemStack.getMaxDamage();
    }

    private EquipmentSlot getSlotType(ItemStack itemStack) {
        if (itemStack.has(DataComponents.GLIDER)) {
            return EquipmentSlot.CHEST;
        }
        return ((Equippable)itemStack.get(DataComponents.EQUIPPABLE)).slot();
    }

    private boolean isArmor(ItemStack itemStack) {
        if (itemStack.getItem() == Items.ELYTRA) {
            return false;
        }
        boolean bl = ((ItemAttributeModifiers)itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, (Object)ItemAttributeModifiers.EMPTY)).modifiers().stream().anyMatch(entry -> entry.attribute().is(Attributes.ARMOR));
        if (!bl) {
            return false;
        }
        Equippable equippable = (Equippable)itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }
}

