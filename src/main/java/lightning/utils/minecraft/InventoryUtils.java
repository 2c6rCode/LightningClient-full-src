/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor
 *  net.minecraft.client.gui.screens.inventory.ContainerScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.state.BlockState
 */
package lightning.utils.minecraft;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lightning.Night;
import lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CSlotSyncPacket;
import lightning.pingbypass.protocol.packets.S2CWindowClickPacket;
import lightning.utils.IMinecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class InventoryUtils
implements IMinecraft {
    public static String[] SWITCH_MODES = new String[]{"None", "Normal", "Silent", "AltPickup", "AltSwap"};
    public static String[] SWAP_MODES = new String[]{"Pickup", "Swap"};
    public static int HOTBAR_START = 0;
    public static int HOTBAR_END = 8;
    public static int INVENTORY_START = 9;
    public static int INVENTORY_END = 35;
    private static final ArrayDeque<Frame> STACK = new ArrayDeque();
    private static final Set<Integer> LONG_HOLD = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<Boolean> PROGRAMMATIC_SWITCH = ThreadLocal.withInitial(() -> false);
    private static final long STALE_FRAME_MS = 30000L;

    public static void markLongHold(int key) {
        LONG_HOLD.add(key);
    }

    public static void clearLongHold(int key) {
        LONG_HOLD.remove(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean hasActiveSilentSwitch() {
        synchronized (STACK) {
            for (Frame frame : STACK) {
                if (LONG_HOLD.contains(frame.key())) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean isProgrammaticSwitching() {
        return PROGRAMMATIC_SWITCH.get();
    }

    private static int restingSlot() {
        return InventoryUtils.mc.player == null ? Night.POSITION_MANAGER.getServerSlot() : InventoryUtils.mc.player.getInventory().getSelectedSlot();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tickPendingRestore() {
        if (InventoryUtils.mc.player == null) {
            synchronized (STACK) {
                STACK.clear();
            }
            LONG_HOLD.clear();
            return;
        }
        boolean usingItem = InventoryUtils.mc.options.keyUse.isDown() || InventoryUtils.mc.player.isUsingItem();
        if (usingItem || InventoryUtils.mc.options.keyAttack.isDown()) {
            InventoryUtils.flushPendingRestores();
        }
        if (usingItem) {
            InventoryUtils.send(InventoryUtils.mc.player.getInventory().getSelectedSlot());
        }
        synchronized (STACK) {
            if (STACK.removeIf(frame -> !LONG_HOLD.contains(frame.key()) && System.currentTimeMillis() - frame.time() >= 30000L)) {
                InventoryUtils.settle();
            }
            if (!usingItem) {
                InventoryUtils.settle();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void flushPendingRestores() {
        synchronized (STACK) {
            InventoryUtils.settle();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void releaseAllBorrows() {
        synchronized (STACK) {
            STACK.removeIf(frame -> !LONG_HOLD.contains(frame.key()));
            InventoryUtils.settle();
        }
    }

    private static void settle() {
        Frame frame = STACK.peek();
        InventoryUtils.send(frame != null ? frame.held() : InventoryUtils.restingSlot());
    }

    private static void send(int slot) {
        if (slot < 0 || mc.getConnection() == null) {
            return;
        }
        if (slot == Night.POSITION_MANAGER.getServerSlot()) {
            return;
        }
        boolean wasProgrammatic = PROGRAMMATIC_SWITCH.get();
        PROGRAMMATIC_SWITCH.set(true);
        try {
            mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket(slot));
        }
        finally {
            PROGRAMMATIC_SWITCH.set(wasProgrammatic);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void push(int key, int slot) {
        synchronized (STACK) {
            STACK.push(new Frame(key, slot, System.currentTimeMillis()));
            InventoryUtils.send(slot);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void release(int key) {
        synchronized (STACK) {
            boolean removed = false;
            Iterator<Frame> iterator = STACK.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().key() != key) continue;
                iterator.remove();
                removed = true;
                break;
            }
            if (!removed) {
                return;
            }
            Frame frame = STACK.peek();
            if (frame != null) {
                InventoryUtils.send(frame.held());
                return;
            }
            InventoryUtils.settle();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean switchSlot(String mode, int slot, int previousSlot) {
        if (mode.equalsIgnoreCase("None")) {
            return true;
        }
        if (slot == -1 || previousSlot == -1) {
            return false;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            if (!mc.isSameThread() && !mode.equalsIgnoreCase("Silent")) {
                if (slot > HOTBAR_END) {
                    return false;
                }
                mode = "Silent";
            }
            switch (mode) {
                case "Normal": {
                    InventoryUtils.mc.player.getInventory().setSelectedSlot(slot);
                    ((ClientPlayerInteractionManagerAccessor)InventoryUtils.mc.gameMode).invokeSyncSelectedSlot();
                    InventoryUtils.syncSlotToClientIfProxy(slot);
                    break;
                }
                case "Silent": {
                    InventoryUtils.push(slot, slot);
                    break;
                }
                case "AltPickup": 
                case "AltSwap": {
                    if (slot <= HOTBAR_END) {
                        InventoryUtils.push(slot, slot);
                        break;
                    }
                    InventoryUtils.swap(mode.equals("AltPickup") ? "Pickup" : "Swap", slot, previousSlot);
                    InventoryUtils.push(slot, previousSlot);
                }
            }
            return true;
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    public static void switchBackNormal(int slot) {
        if (slot == -1 || slot == Night.POSITION_MANAGER.getServerSlot()) {
            return;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            InventoryUtils.mc.player.getInventory().setSelectedSlot(slot);
            ((ClientPlayerInteractionManagerAccessor)InventoryUtils.mc.gameMode).invokeSyncSelectedSlot();
            InventoryUtils.syncSlotToClientIfProxy(slot);
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    private static void syncSlotToClientIfProxy(int slot) {
        if (Night.PINGBYPASS_CONFIG == null || !Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        if (!PingBypassFlags.proxyForwardingActive || Night.PROXY_SERVER == null) {
            return;
        }
        ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CSlotSyncPacket(slot)));
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundCustomPayloadPacket);
        }
    }

    public static void switchBack(String mode, int slot, int previousSlot) {
        if (mode.equalsIgnoreCase("None")) {
            return;
        }
        if (previousSlot == -1) {
            return;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            if ((mode.equals("AltPickup") || mode.equals("AltSwap")) && slot > HOTBAR_END && mc.isSameThread()) {
                InventoryUtils.swap(mode.equals("AltPickup") ? "Pickup" : "Swap", slot, previousSlot);
            }
            InventoryUtils.release(slot);
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    public static void swapEquipment(int slot, int equipmentSlot) {
        if (slot >= 0 && slot <= 8) {
            InventoryUtils.click(equipmentSlot, slot, ContainerInput.SWAP);
            return;
        }
        InventoryUtils.click(InventoryUtils.indexToSlot(slot), 0, ContainerInput.PICKUP);
        InventoryUtils.click(equipmentSlot, 0, ContainerInput.PICKUP);
        InventoryUtils.click(InventoryUtils.indexToSlot(slot), 0, ContainerInput.PICKUP);
    }

    public static void swap(String mode, int slot, int targetSlot) {
        switch (mode) {
            case "Pickup": {
                InventoryUtils.click(InventoryUtils.indexToSlot(slot), 0, ContainerInput.PICKUP);
                InventoryUtils.click(InventoryUtils.indexToSlot(targetSlot), 0, ContainerInput.PICKUP);
                InventoryUtils.click(InventoryUtils.indexToSlot(slot), 0, ContainerInput.PICKUP);
                break;
            }
            case "Swap": {
                InventoryUtils.click(InventoryUtils.indexToSlot(slot), targetSlot, ContainerInput.SWAP);
            }
        }
    }

    public static void click(int slot, int button, ContainerInput containerInput) {
        InventoryUtils.mc.gameMode.handleContainerInput(InventoryUtils.mc.player.containerMenu.containerId, slot, button, containerInput, (Player)InventoryUtils.mc.player);
        InventoryUtils.broadcastClickToClientIfProxy(slot, button, containerInput);
    }

    private static void broadcastClickToClientIfProxy(int slot, int button, ContainerInput containerInput) {
        if (Night.PINGBYPASS_CONFIG == null || !Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        if (!PingBypassFlags.proxyForwardingActive || Night.PROXY_SERVER == null) {
            return;
        }
        ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CWindowClickPacket(InventoryUtils.mc.player.containerMenu.containerId, slot, button, containerInput)));
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundCustomPayloadPacket);
        }
    }

    public static int indexToSlot(int index) {
        int offset;
        AbstractContainerMenu abstractContainerMenu = InventoryUtils.mc.player != null ? InventoryUtils.mc.player.containerMenu : null;
        if (abstractContainerMenu != null && InventoryUtils.mc.player.inventoryMenu != abstractContainerMenu && index >= 0 && index <= 35 && (offset = abstractContainerMenu.slots.size() - 36) >= 0) {
            return index <= 8 ? offset + 27 + index : offset + (index - 9);
        }
        if (index >= 0 && index <= 8) {
            return 36 + index;
        }
        return index;
    }

    public static int find(Item item) {
        return InventoryUtils.find(item, HOTBAR_START, INVENTORY_END);
    }

    public static int findHotbar(Item item) {
        return InventoryUtils.find(item, HOTBAR_START, HOTBAR_END);
    }

    public static int findInventory(Item item) {
        return InventoryUtils.find(item, INVENTORY_START, INVENTORY_END);
    }

    public static int find(Item item, int start, int end) {
        for (int i = end; i >= start; --i) {
            ItemStack itemStack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public static int find(Class<? extends Item> clazz) {
        return InventoryUtils.find(clazz, HOTBAR_START, INVENTORY_END);
    }

    public static int findHotbar(Class<? extends Item> clazz) {
        return InventoryUtils.find(clazz, HOTBAR_START, HOTBAR_END);
    }

    public static int findInventory(Class<? extends Item> clazz) {
        return InventoryUtils.find(clazz, INVENTORY_START, INVENTORY_END);
    }

    public static int find(Class<? extends Item> clazz, int start, int end) {
        for (int i = end; i >= start; --i) {
            ItemStack itemStack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (!itemStack.getItem().getClass().isAssignableFrom(clazz)) continue;
            return i;
        }
        return -1;
    }

    public static int findInventory(Item item, int minCount) {
        for (int i = INVENTORY_END; i >= INVENTORY_START; --i) {
            ItemStack itemStack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() != item || InventoryUtils.mc.player.getInventory().getItem(i).getCount() < minCount) continue;
            return i;
        }
        return -1;
    }

    public static int findHardestBlock(int start, int end) {
        float bestHardness = -1.0f;
        int bestSlot = -1;
        for (int i = start; i <= end; ++i) {
            Item item = InventoryUtils.mc.player.getInventory().getItem(i).getItem();
            if (!(item instanceof BlockItem)) continue;
            BlockItem blockItem = (BlockItem)item;
            float hardness = blockItem.getBlock().defaultDestroyTime();
            if (hardness == -1.0f) {
                return i;
            }
            if (!(hardness > bestHardness)) continue;
            bestHardness = hardness;
            bestSlot = i;
        }
        return bestSlot;
    }

    public static int findFastestItem(BlockState blockState, int start, int end) {
        double bestSpeed = -1.0;
        int bestSlot = -1;
        for (int i = start; i <= end; ++i) {
            double speed = InventoryUtils.mc.player.getInventory().getItem(i).getItem().getDestroySpeed(InventoryUtils.mc.player.getInventory().getItem(i), blockState);
            if (!(speed > bestSpeed)) continue;
            bestSpeed = speed;
            bestSlot = i;
        }
        return bestSlot;
    }

    public static int findBestSword(int start, int end) {
        int netheriteSlot = -1;
        int diamondSlot = -1;
        int ironSlot = -1;
        int goldenSlot = -1;
        int stoneSlot = -1;
        int woodenSlot = -1;
        for (int i = end; i >= start; --i) {
            ItemStack itemStack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() == Items.NETHERITE_SWORD) {
                netheriteSlot = i;
            }
            if (itemStack.getItem() == Items.DIAMOND_SWORD) {
                diamondSlot = i;
            }
            if (itemStack.getItem() == Items.IRON_SWORD) {
                ironSlot = i;
            }
            if (itemStack.getItem() == Items.GOLDEN_SWORD) {
                goldenSlot = i;
            }
            if (itemStack.getItem() == Items.STONE_SWORD) {
                stoneSlot = i;
            }
            if (itemStack.getItem() != Items.WOODEN_SWORD) continue;
            woodenSlot = i;
        }
        if (netheriteSlot != -1) {
            return netheriteSlot;
        }
        if (diamondSlot != -1) {
            return diamondSlot;
        }
        if (ironSlot != -1) {
            return ironSlot;
        }
        if (goldenSlot != -1) {
            return goldenSlot;
        }
        if (stoneSlot != -1) {
            return stoneSlot;
        }
        return woodenSlot;
    }

    public static int findEmptySlot(int start, int end) {
        for (int i = end; i >= start; --i) {
            ItemStack itemStack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (!itemStack.isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public static boolean inInventoryScreen() {
        return InventoryUtils.mc.gui.screen() instanceof InventoryScreen || InventoryUtils.mc.gui.screen() instanceof CreativeModeInventoryScreen || InventoryUtils.mc.gui.screen() instanceof ContainerScreen || InventoryUtils.mc.gui.screen() instanceof ShulkerBoxScreen;
    }

    private record Frame(int key, int held, long time) {
    }
}

