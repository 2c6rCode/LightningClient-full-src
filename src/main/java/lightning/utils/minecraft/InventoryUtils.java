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

    public static void markLongHold(int n) {
        LONG_HOLD.add(n);
    }

    public static void clearLongHold(int n) {
        LONG_HOLD.remove(n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean hasActiveSilentSwitch() {
        ArrayDeque<Frame> arrayDeque;
        ArrayDeque<Frame> arrayDeque2 = arrayDeque = STACK;
        synchronized (arrayDeque2) {
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
        ArrayDeque<Frame> arrayDeque;
        if (InventoryUtils.mc.player == null) {
            ArrayDeque<Frame> arrayDeque2;
            ArrayDeque<Frame> arrayDeque3 = arrayDeque2 = STACK;
            synchronized (arrayDeque3) {
                STACK.clear();
            }
            LONG_HOLD.clear();
            return;
        }
        boolean bl = InventoryUtils.mc.options.keyUse.isDown() || InventoryUtils.mc.player.isUsingItem();
        boolean bl2 = bl;
        if (bl || InventoryUtils.mc.options.keyAttack.isDown()) {
            InventoryUtils.flushPendingRestores();
        }
        if (bl) {
            InventoryUtils.send(InventoryUtils.mc.player.getInventory().getSelectedSlot());
        }
        ArrayDeque<Frame> arrayDeque4 = arrayDeque = STACK;
        synchronized (arrayDeque4) {
            if (STACK.removeIf(frame -> !LONG_HOLD.contains(frame.key()) && System.currentTimeMillis() - frame.time() >= 30000L)) {
                InventoryUtils.settle();
            }
            if (!bl) {
                InventoryUtils.settle();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void flushPendingRestores() {
        ArrayDeque<Frame> arrayDeque;
        ArrayDeque<Frame> arrayDeque2 = arrayDeque = STACK;
        synchronized (arrayDeque2) {
            InventoryUtils.settle();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void releaseAllBorrows() {
        ArrayDeque<Frame> arrayDeque;
        ArrayDeque<Frame> arrayDeque2 = arrayDeque = STACK;
        synchronized (arrayDeque2) {
            STACK.removeIf(frame -> !LONG_HOLD.contains(frame.key()));
            InventoryUtils.settle();
        }
    }

    private static void settle() {
        Frame frame = STACK.peek();
        InventoryUtils.send(frame != null ? frame.held() : InventoryUtils.restingSlot());
    }

    private static void send(int n) {
        if (n < 0 || mc.getConnection() == null) {
            return;
        }
        if (n == Night.POSITION_MANAGER.getServerSlot()) {
            return;
        }
        boolean bl = PROGRAMMATIC_SWITCH.get();
        PROGRAMMATIC_SWITCH.set(true);
        try {
            mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket(n));
        }
        finally {
            PROGRAMMATIC_SWITCH.set(bl);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void push(int n, int n2) {
        ArrayDeque<Frame> arrayDeque;
        ArrayDeque<Frame> arrayDeque2 = arrayDeque = STACK;
        synchronized (arrayDeque2) {
            STACK.push(new Frame(n, n2, System.currentTimeMillis()));
            InventoryUtils.send(n2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void release(int n) {
        ArrayDeque<Frame> arrayDeque;
        ArrayDeque<Frame> arrayDeque2 = arrayDeque = STACK;
        synchronized (arrayDeque2) {
            boolean bl = false;
            Iterator<Frame> iterator = STACK.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().key() != n) continue;
                iterator.remove();
                bl = true;
                break;
            }
            if (!bl) {
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
    public static boolean switchSlot(String string, int n, int n2) {
        if (string.equalsIgnoreCase("None")) {
            return true;
        }
        if (n == -1 || n2 == -1) {
            return false;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            boolean bl;
            if (!mc.isSameThread() && !string.equalsIgnoreCase("Silent")) {
                if (n > HOTBAR_END) {
                    boolean bl2;
                    boolean bl3 = bl2 = false;
                    return bl3;
                }
                string = "Silent";
            }
            switch (string) {
                case "Normal": {
                    InventoryUtils.mc.player.getInventory().setSelectedSlot(n);
                    ((ClientPlayerInteractionManagerAccessor)InventoryUtils.mc.gameMode).invokeSyncSelectedSlot();
                    InventoryUtils.syncSlotToClientIfProxy(n);
                    break;
                }
                case "Silent": {
                    InventoryUtils.push(n, n);
                    break;
                }
                case "AltPickup": 
                case "AltSwap": {
                    if (n <= HOTBAR_END) {
                        InventoryUtils.push(n, n);
                        break;
                    }
                    InventoryUtils.swap(string.equals("AltPickup") ? "Pickup" : "Swap", n, n2);
                    InventoryUtils.push(n, n2);
                }
            }
            boolean bl4 = bl = true;
            return bl4;
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    public static void switchBackNormal(int n) {
        if (n == -1 || n == Night.POSITION_MANAGER.getServerSlot()) {
            return;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            InventoryUtils.mc.player.getInventory().setSelectedSlot(n);
            ((ClientPlayerInteractionManagerAccessor)InventoryUtils.mc.gameMode).invokeSyncSelectedSlot();
            InventoryUtils.syncSlotToClientIfProxy(n);
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    private static void syncSlotToClientIfProxy(int n) {
        if (Night.PINGBYPASS_CONFIG == null || !Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        if (!PingBypassFlags.proxyForwardingActive || Night.PROXY_SERVER == null) {
            return;
        }
        ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CSlotSyncPacket(n)));
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundCustomPayloadPacket);
        }
    }

    public static void switchBack(String string, int n, int n2) {
        if (string.equalsIgnoreCase("None")) {
            return;
        }
        if (n2 == -1) {
            return;
        }
        PROGRAMMATIC_SWITCH.set(true);
        try {
            if ((string.equals("AltPickup") || string.equals("AltSwap")) && n > HOTBAR_END && mc.isSameThread()) {
                InventoryUtils.swap(string.equals("AltPickup") ? "Pickup" : "Swap", n, n2);
            }
            InventoryUtils.release(n);
        }
        finally {
            PROGRAMMATIC_SWITCH.set(false);
        }
    }

    public static void swapEquipment(int n, int n2) {
        if (n >= 0 && n <= 8) {
            InventoryUtils.click(n2, n, ContainerInput.SWAP);
            return;
        }
        InventoryUtils.click(InventoryUtils.indexToSlot(n), 0, ContainerInput.PICKUP);
        InventoryUtils.click(n2, 0, ContainerInput.PICKUP);
        InventoryUtils.click(InventoryUtils.indexToSlot(n), 0, ContainerInput.PICKUP);
    }

    public static void swap(String string, int n, int n2) {
        switch (string) {
            case "Pickup": {
                InventoryUtils.click(InventoryUtils.indexToSlot(n), 0, ContainerInput.PICKUP);
                InventoryUtils.click(InventoryUtils.indexToSlot(n2), 0, ContainerInput.PICKUP);
                InventoryUtils.click(InventoryUtils.indexToSlot(n), 0, ContainerInput.PICKUP);
                break;
            }
            case "Swap": {
                InventoryUtils.click(InventoryUtils.indexToSlot(n), n2, ContainerInput.SWAP);
            }
        }
    }

    public static void click(int n, int n2, ContainerInput containerInput) {
        InventoryUtils.mc.gameMode.handleContainerInput(InventoryUtils.mc.player.containerMenu.containerId, n, n2, containerInput, (Player)InventoryUtils.mc.player);
        InventoryUtils.broadcastClickToClientIfProxy(n, n2, containerInput);
    }

    private static void broadcastClickToClientIfProxy(int n, int n2, ContainerInput containerInput) {
        if (Night.PINGBYPASS_CONFIG == null || !Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        if (!PingBypassFlags.proxyForwardingActive || Night.PROXY_SERVER == null) {
            return;
        }
        ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CWindowClickPacket(InventoryUtils.mc.player.containerMenu.containerId, n, n2, containerInput)));
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundCustomPayloadPacket);
        }
    }

    public static int indexToSlot(int n) {
        int n2;
        AbstractContainerMenu abstractContainerMenu = InventoryUtils.mc.player != null ? InventoryUtils.mc.player.containerMenu : null;
        AbstractContainerMenu abstractContainerMenu2 = abstractContainerMenu;
        if (abstractContainerMenu != null && InventoryUtils.mc.player.inventoryMenu != abstractContainerMenu && n >= 0 && n <= 35 && (n2 = abstractContainerMenu.slots.size() - 36) >= 0) {
            return n <= 8 ? n2 + 27 + n : n2 + (n - 9);
        }
        if (n >= 0 && n <= 8) {
            return 36 + n;
        }
        return n;
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

    public static int find(Item item, int n, int n2) {
        for (int i = n2; i >= n; --i) {
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

    public static int find(Class<? extends Item> clazz, int n, int n2) {
        for (int i = n2; i >= n; --i) {
            ItemStack itemStack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (!itemStack.getItem().getClass().isAssignableFrom(clazz)) continue;
            return i;
        }
        return -1;
    }

    public static int findInventory(Item item, int n) {
        for (int i = INVENTORY_END; i >= INVENTORY_START; --i) {
            ItemStack itemStack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() != item || InventoryUtils.mc.player.getInventory().getItem(i).getCount() < n) continue;
            return i;
        }
        return -1;
    }

    public static int findHardestBlock(int n, int n2) {
        float f = -1.0f;
        int n3 = -1;
        for (int i = n; i <= n2; ++i) {
            Item item = InventoryUtils.mc.player.getInventory().getItem(i).getItem();
            if (!(item instanceof BlockItem)) continue;
            BlockItem blockItem = (BlockItem)item;
            float f2 = blockItem.getBlock().defaultDestroyTime();
            if (f2 == -1.0f) {
                return i;
            }
            if (!(f2 > f)) continue;
            f = f2;
            n3 = i;
        }
        return n3;
    }

    public static int findFastestItem(BlockState blockState, int n, int n2) {
        double d = -1.0;
        int n3 = -1;
        for (int i = n; i <= n2; ++i) {
            double d2 = InventoryUtils.mc.player.getInventory().getItem(i).getItem().getDestroySpeed(InventoryUtils.mc.player.getInventory().getItem(i), blockState);
            if (!(d2 > d)) continue;
            d = d2;
            n3 = i;
        }
        return n3;
    }

    public static int findBestSword(int n, int n2) {
        int n3 = -1;
        int n4 = -1;
        int n5 = -1;
        int n6 = -1;
        int n7 = -1;
        int n8 = -1;
        for (int i = n2; i >= n; --i) {
            ItemStack itemStack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() == Items.NETHERITE_SWORD) {
                n3 = i;
            }
            if (itemStack.getItem() == Items.DIAMOND_SWORD) {
                n4 = i;
            }
            if (itemStack.getItem() == Items.IRON_SWORD) {
                n5 = i;
            }
            if (itemStack.getItem() == Items.GOLDEN_SWORD) {
                n6 = i;
            }
            if (itemStack.getItem() == Items.STONE_SWORD) {
                n7 = i;
            }
            if (itemStack.getItem() != Items.WOODEN_SWORD) continue;
            n8 = i;
        }
        if (n3 != -1) {
            return n3;
        }
        if (n4 != -1) {
            return n4;
        }
        if (n5 != -1) {
            return n5;
        }
        if (n6 != -1) {
            return n6;
        }
        if (n7 != -1) {
            return n7;
        }
        return n8;
    }

    public static int findEmptySlot(int n, int n2) {
        for (int i = n2; i >= n; --i) {
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

