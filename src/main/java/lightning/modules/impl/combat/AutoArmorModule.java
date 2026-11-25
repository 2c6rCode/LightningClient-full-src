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
        String itemName = itemStack.getItem().toString().toLowerCase();
        return itemName.contains("diamond") || itemName.contains("netherite");
    }

    private boolean isValidCombatArmor(ItemStack itemStack, String enchantName) {
        if (!this.isArmor(itemStack)) {
            return false;
        }
        return this.isTierDiamondOrNetherite(itemStack) || this.getEnchantLevel(itemStack, enchantName) > 0;
    }

    private void update(EquipmentSlot equipmentSlot, int containerSlot) {
        int elytraSlot = this.findElytra();
        boolean equipElytra = this.elytra.getValue() && equipmentSlot == EquipmentSlot.CHEST;
        int currentSlot = equipmentSlot == EquipmentSlot.HEAD ? 39 : (equipmentSlot == EquipmentSlot.CHEST ? 38 : (equipmentSlot == EquipmentSlot.LEGS ? 37 : 36));
        int bestSlot = equipElytra ? elytraSlot : this.findArmor(equipmentSlot);
        int chosenSlot = equipElytra ? this.compareElytra(38, bestSlot) : this.compare(currentSlot, bestSlot, true, equipmentSlot);
        if (bestSlot != -1 && chosenSlot != currentSlot) {
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, containerSlot, 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, InventoryUtils.indexToSlot(bestSlot), 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            AutoArmorModule.mc.gameMode.handleContainerInput(AutoArmorModule.mc.player.inventoryMenu.containerId, containerSlot, 0, ContainerInput.PICKUP, (Player)AutoArmorModule.mc.player);
            this.ticks = 2 + Night.SERVER_MANAGER.getPingDelay();
        }
    }

    private int compare(int currentSlot, int candidateSlot, boolean equippedCheck, EquipmentSlot equipmentSlot) {
        if (candidateSlot == -1) {
            return currentSlot;
        }
        if (!this.isArmor(AutoArmorModule.mc.player.getInventory().getItem(currentSlot))) {
            return candidateSlot;
        }
        String enchantName = switch (equipmentSlot) {
            case EquipmentSlot.HEAD -> this.headMode.getValue();
            case EquipmentSlot.CHEST -> this.chestMode.getValue();
            case EquipmentSlot.LEGS -> this.legsMode.getValue();
            default -> this.feetMode.getValue();
        };
        enchantName = enchantName.equalsIgnoreCase("Blast") ? "blast_protection" : "protection";
        ItemStack currentStack = AutoArmorModule.mc.player.getInventory().getItem(currentSlot);
        ItemStack candidateStack = AutoArmorModule.mc.player.getInventory().getItem(candidateSlot);
        if (this.getEnchantLevel(currentStack, enchantName) < this.getEnchantLevel(candidateStack, enchantName)) {
            return candidateSlot;
        }
        if (this.preserve.getValue()) {
            boolean safe = this.isSafe();
            if (safe) {
                if (this.getDurability(candidateSlot) < this.preserveHealth.getValue().floatValue() && this.getDurability(currentSlot) >= this.preserveHealth.getValue().floatValue() && this.isValidCombatArmor(candidateStack, enchantName)) {
                    return candidateSlot;
                }
            } else if (this.getDurability(currentSlot) < this.preserveHealth.getValue().floatValue() && this.isValidCombatArmor(candidateStack, enchantName)) {
                return this.getDurability(currentSlot) < this.getDurability(candidateSlot) ? candidateSlot : currentSlot;
            }
        }
        if (!equippedCheck) {
            if (this.health.getValue().equals("Highest") && this.getDurability(currentSlot) < this.getDurability(candidateSlot)) {
                return candidateSlot;
            }
            if (this.health.getValue().equals("Lowest") && this.getDurability(currentSlot) > this.getDurability(candidateSlot)) {
                return candidateSlot;
            }
        }
        return currentSlot;
    }

    private boolean isSafe() {
        if (this.safeRange.getValue().doubleValue() <= 0.0) {
            return false;
        }
        if (AutoArmorModule.mc.level == null || AutoArmorModule.mc.player == null) {
            return false;
        }
        double safeRangeSq = this.safeRange.getValue().doubleValue() * this.safeRange.getValue().doubleValue();
        for (Player player : AutoArmorModule.mc.level.players()) {
            if (player == AutoArmorModule.mc.player || player.isSpectator() || !player.isAlive() || Night.FRIEND_MANAGER != null && Night.FRIEND_MANAGER.contains(player.getName().getString()) || !(AutoArmorModule.mc.player.distanceToSqr((Entity)player) <= safeRangeSq)) continue;
            return false;
        }
        return true;
    }

    private int compareElytra(int currentSlot, int candidateSlot) {
        if (candidateSlot == -1) {
            return currentSlot;
        }
        if (AutoArmorModule.mc.player.getInventory().getItem(currentSlot).getItem() != Items.ELYTRA) {
            return candidateSlot;
        }
        if (this.health.getValue().equals("Highest") && this.getDurability(currentSlot) < this.getDurability(candidateSlot)) {
            return candidateSlot;
        }
        if (this.health.getValue().equals("Lowest") && this.getDurability(currentSlot) > this.getDurability(candidateSlot)) {
            return candidateSlot;
        }
        return currentSlot;
    }

    private int findArmor(EquipmentSlot equipmentSlot) {
        int bestSlot = -1;
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.INVENTORY_END; ++i) {
            ItemStack itemStack = AutoArmorModule.mc.player.getInventory().getItem(i);
            if (!this.isArmor(itemStack) || !this.getSlotType(itemStack).equals((Object)equipmentSlot)) continue;
            bestSlot = this.compare(i, bestSlot, false, equipmentSlot);
        }
        return bestSlot;
    }

    private int findElytra() {
        int bestSlot = -1;
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.INVENTORY_END; ++i) {
            ItemStack itemStack = AutoArmorModule.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() != Items.ELYTRA) continue;
            bestSlot = this.compareElytra(i, bestSlot);
        }
        return bestSlot;
    }

    private int getEnchantLevel(ItemStack itemStack, String enchantName) {
        for (Holder holder : itemStack.getEnchantments().keySet()) {
            if (!holder.getRegisteredName().replace("minecraft:", "").equals(enchantName)) continue;
            return itemStack.getEnchantments().getLevel(holder);
        }
        return 0;
    }

    private float getDurability(int slot) {
        ItemStack itemStack = AutoArmorModule.mc.player.getInventory().getItem(slot);
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
        boolean hasArmorAttr = ((ItemAttributeModifiers)itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, (Object)ItemAttributeModifiers.EMPTY)).modifiers().stream().anyMatch(entry -> entry.attribute().is(Attributes.ARMOR));
        if (!hasArmorAttr) {
            return false;
        }
        Equippable equippable = (Equippable)itemStack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }
}

