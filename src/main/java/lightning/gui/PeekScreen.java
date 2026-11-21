/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.ContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.ChestMenu
 */
package lightning.gui;

import lightning.gui.PeekMenu;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

public class PeekScreen
extends ContainerScreen {
    public PeekScreen(PeekMenu peekMenu, Inventory inventory, Component component) {
        super((ChestMenu)peekMenu, inventory, component);
    }
}

