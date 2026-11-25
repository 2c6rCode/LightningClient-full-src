/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@RegisterCommand(name="itemdrop", aliases={"drop"}, tag="ItemDrop", description="Drops items from your inventory.", syntax="<all|item name>")
public class ItemDropCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            ArrayList<String> suggestions = new ArrayList<String>(List.of("all"));
            for (Item item : BuiltInRegistries.ITEM) {
                suggestions.add(BuiltInRegistries.ITEM.getKey(item).getPath());
            }
            return suggestions;
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            this.messageSyntax();
            return;
        }
        if (args[0].equalsIgnoreCase("all")) {
            this.dropAllItems();
            return;
        }
        String itemName = String.join((CharSequence)" ", args);
        this.dropSpecificItem(itemName);
    }

    private void dropAllItems() {
        if (ItemDropCommand.mc.player == null || ItemDropCommand.mc.gameMode == null) {
            return;
        }
        int containerId = ItemDropCommand.mc.player.containerMenu.containerId;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = ItemDropCommand.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty()) continue;
            ItemDropCommand.mc.gameMode.handleContainerInput(containerId, ItemDropCommand.invToHandlerSlot(i), 1, ContainerInput.THROW, (Player)ItemDropCommand.mc.player);
        }
    }

    private void dropSpecificItem(String targetName) {
        if (ItemDropCommand.mc.player == null || ItemDropCommand.mc.gameMode == null) {
            return;
        }
        int containerId = ItemDropCommand.mc.player.containerMenu.containerId;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = ItemDropCommand.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty()) continue;
            String itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            String hoverName = itemStack.getHoverName().getString();
            if (!itemId.equalsIgnoreCase(targetName) && !itemId.substring(itemId.indexOf(58) + 1).equalsIgnoreCase(targetName) && !hoverName.equalsIgnoreCase(targetName)) continue;
            ItemDropCommand.mc.gameMode.handleContainerInput(containerId, ItemDropCommand.invToHandlerSlot(i), 1, ContainerInput.THROW, (Player)ItemDropCommand.mc.player);
        }
    }

    private static int invToHandlerSlot(int slot) {
        return slot <= 8 ? 36 + slot : slot;
    }
}

