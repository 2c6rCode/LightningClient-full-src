/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 */
package lightning.modules.impl.player;

import lightning.modules.impl.player.AutoShopModule;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

public final class AutoShopSync {
    private static final int CONFIRM_SLOT = 23;
    private static final long MAX_WAIT_NANOS = 1000000000L;
    private static Object lastMenu;
    private static int lastStateId;
    private static boolean armed;
    private static long armedAt;
    private static long lastActive;
    private static final long GRACE_NANOS = 1500000000L;

    private AutoShopSync() {
    }

    private static int stateId(AbstractContainerMenu abstractContainerMenu) {
        try {
            return abstractContainerMenu.getStateId();
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    public static void click(int slot, int button, ContainerInput containerInput) {
        lastActive = System.nanoTime();
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (slot != 23 && minecraft != null && minecraft.player != null && minecraft.player.containerMenu != null) {
                lastMenu = minecraft.player.containerMenu;
                lastStateId = AutoShopSync.stateId(minecraft.player.containerMenu);
                armedAt = System.nanoTime();
                armed = true;
            } else {
                armed = false;
            }
        }
        catch (Throwable throwable) {
            armed = false;
        }
        InventoryUtils.click(slot, button, containerInput);
    }

    public static int delay(Number number) {
        int defaultDelay = number.intValue();
        lastActive = System.nanoTime();
        if (!armed) {
            return defaultDelay;
        }
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.player == null) {
                armed = false;
                return defaultDelay;
            }
            AbstractContainerMenu abstractContainerMenu = minecraft.player.containerMenu;
            if (abstractContainerMenu != lastMenu || abstractContainerMenu != null && AutoShopSync.stateId(abstractContainerMenu) != lastStateId || System.nanoTime() - armedAt > 1000000000L) {
                armed = false;
                return defaultDelay;
            }
            return Integer.MAX_VALUE;
        }
        catch (Throwable throwable) {
            armed = false;
            return defaultDelay;
        }
    }

    public static AutoShopModule.ScreenDecision classify(AutoShopModule autoShopModule, Screen screen) {
        AutoShopModule.ScreenDecision screenDecision = autoShopModule.classifyScreen(screen);
        if (screenDecision == AutoShopModule.ScreenDecision.SUPPRESS_EXPECTED) {
            return screenDecision;
        }
        try {
            if (lastActive != 0L && autoShopModule.silent.getValue() && screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen) && System.nanoTime() - lastActive < 1500000000L) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.closeContainer();
                }
                return AutoShopModule.ScreenDecision.SUPPRESS_EXPECTED;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return screenDecision;
    }
}

