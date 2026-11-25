/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 */
package lightning.modules.impl.combat;

import java.lang.reflect.Field;
import lightning.Night;
import lightning.modules.impl.combat.MainhandModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MainhandSlotGuard {
    private static int cooldown = 0;
    private static volatile boolean active = false;
    private static volatile int lo = 0;
    private static volatile int hi = -1;
    private static boolean turnedOnTotemAuto = false;

    private MainhandSlotGuard() {
    }

    public static void onModuleEnable() {
        try {
            AutoShopModule autoShopModule = Night.MODULE_MANAGER.getModule(AutoShopModule.class);
            if (autoShopModule == null) {
                return;
            }
            turnedOnTotemAuto = false;
            if (!autoShopModule.totemBelowThreshold.getValue()) {
                autoShopModule.totemBelowThreshold.setValue(true);
                turnedOnTotemAuto = true;
            }
            if (!autoShopModule.isToggled()) {
                autoShopModule.setToggled(true);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void onModuleDisable() {
        try {
            if (!turnedOnTotemAuto) {
                return;
            }
            turnedOnTotemAuto = false;
            AutoShopModule autoShopModule = Night.MODULE_MANAGER.getModule(AutoShopModule.class);
            if (autoShopModule != null) {
                autoShopModule.totemBelowThreshold.setValue(false);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static boolean isLocked(int slot) {
        return active && slot >= lo && slot <= hi;
    }

    public static int quickButton(int button) {
        return active ? button : 0;
    }

    public static ContainerInput quickInput() {
        return active ? ContainerInput.SWAP : ContainerInput.QUICK_MOVE;
    }

    private static boolean flag(Object instance, String fieldName) {
        try {
            Field field = MainhandModule.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getBoolean(instance);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static void tick(MainhandModule mainhandModule, BooleanSetting guardEnabled, ModeSetting guardMode, NumberSetting minSlotSetting, NumberSetting maxSlotSetting) {
        try {
            MainhandSlotGuard.run(mainhandModule, guardEnabled, guardMode, minSlotSetting, maxSlotSetting);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void run(MainhandModule mainhandModule, BooleanSetting guardEnabled, ModeSetting guardMode, NumberSetting minSlotSetting, NumberSetting maxSlotSetting) {
        if (guardEnabled == null || !guardEnabled.getValue() || !mainhandModule.isToggled()) {
            active = false;
            return;
        }
        int minSlot = minSlotSetting.getValue().intValue();
        int maxSlot = maxSlotSetting.getValue().intValue();
        lo = Math.max(1, Math.min(minSlot, maxSlot)) - 1;
        hi = Math.min(9, Math.max(minSlot, maxSlot)) - 1;
        active = true;
        Minecraft minecraft = IMinecraft.mc;
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null || minecraft.gameMode == null || localPlayer.isDeadOrDying()) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (cooldown > 0) {
            --cooldown;
            return;
        }
        if (localPlayer.containerMenu != localPlayer.inventoryMenu) {
            return;
        }
        if (!localPlayer.containerMenu.getCarried().isEmpty()) {
            return;
        }
        if (MainhandSlotGuard.flag(mainhandModule, "eatingHotbar") || MainhandSlotGuard.flag(mainhandModule, "eatingInventory")) {
            return;
        }
        if (InventoryUtils.hasActiveSilentSwitch()) {
            return;
        }
        boolean dropMode = "Drop".equals(guardMode == null ? "Inventory" : guardMode.getValue());
        Inventory inventory = localPlayer.getInventory();
        int movedCount = 0;
        for (int i = lo; i <= hi && movedCount < 4; ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.isEmpty() || itemStack.is(Items.TOTEM_OF_UNDYING)) continue;
            int emptySlot = -1;
            if (!dropMode) {
                for (int j = 9; j <= 35; ++j) {
                    if (!inventory.getItem(j).isEmpty()) continue;
                    emptySlot = j;
                    break;
                }
            }
            if (emptySlot != -1) {
                InventoryUtils.click(emptySlot, i, ContainerInput.SWAP);
            } else {
                InventoryUtils.click(36 + i, 1, ContainerInput.THROW);
            }
            ++movedCount;
        }
        if (movedCount > 0) {
            cooldown = 1;
        }
    }
}

