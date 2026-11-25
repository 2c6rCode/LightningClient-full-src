/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 */
package lightning.pingbypass.input;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.TickEvent;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.C2SInputPacket;
import lightning.pingbypass.protocol.packets.C2SOpenInventoryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClientInputForwarder {
    private boolean inventoryOpen = false;

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        InventoryScreen inventoryScreen;
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }
        Screen screen = minecraft.gui.screen();
        boolean isInventoryOpen = screen instanceof InventoryScreen && (inventoryScreen = (InventoryScreen)screen).getMenu() == minecraft.player.inventoryMenu;
        if (isInventoryOpen == this.inventoryOpen) {
            return;
        }
        this.inventoryOpen = isInventoryOpen;
        try {
            PbCustomPayload pbCustomPayload = PbCustomPayload.fromPacket(new C2SOpenInventoryPacket(isInventoryOpen));
            minecraft.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)pbCustomPayload));
        }
        catch (Exception exception) {
            Night.LOGGER.warn("[PingBypass] Failed to send inventory-open state", (Throwable)exception);
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent mouseInputEvent) {
        if (!PingBypassFlags.proxyForwardingActive) {
            return;
        }
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return;
        }
        int button = mouseInputEvent.getButton();
        if (button != 0 && button != 1) {
            return;
        }
        this.sendInput(minecraft, 0, button, 1);
    }

    private void sendInput(Minecraft minecraft, int inputType, int keyCode, int action) {
        try {
            PbCustomPayload pbCustomPayload = PbCustomPayload.fromPacket(new C2SInputPacket(inputType, keyCode, action));
            minecraft.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)pbCustomPayload));
        }
        catch (Exception exception) {
            Night.LOGGER.warn("[PingBypass] Failed to send input", (Throwable)exception);
        }
    }

    public void start() {
        Night.EVENT_HANDLER.subscribe(this);
        Night.LOGGER.info("[PingBypass] Client input forwarder started");
    }

    public void stop() {
        Night.EVENT_HANDLER.unsubscribe(this);
    }
}

