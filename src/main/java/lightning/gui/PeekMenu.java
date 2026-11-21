/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 */
package lightning.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class PeekMenu
extends ChestMenu {
    public PeekMenu(int n, Inventory inventory, Container container) {
        super(MenuType.GENERIC_9x3, n, inventory, container, 3);
    }

    public void clicked(int n, int n2, ContainerInput containerInput, Player player) {
    }

    public ItemStack quickMoveStack(Player player, int n) {
        return ItemStack.EMPTY;
    }
}

