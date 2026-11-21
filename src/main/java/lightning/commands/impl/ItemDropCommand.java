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
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            ArrayList<String> arrayList = new ArrayList<String>(List.of("all"));
            for (Item item : BuiltInRegistries.ITEM) {
                arrayList.add(BuiltInRegistries.ITEM.getKey(item).getPath());
            }
            return arrayList;
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        if (stringArray.length == 0) {
            this.messageSyntax();
            return;
        }
        if (stringArray[0].equalsIgnoreCase("all")) {
            this.dropAllItems();
            return;
        }
        String string = String.join((CharSequence)" ", stringArray);
        this.dropSpecificItem(string);
    }

    private void dropAllItems() {
        if (ItemDropCommand.mc.player == null || ItemDropCommand.mc.gameMode == null) {
            return;
        }
        int n = ItemDropCommand.mc.player.containerMenu.containerId;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = ItemDropCommand.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty()) continue;
            ItemDropCommand.mc.gameMode.handleContainerInput(n, ItemDropCommand.invToHandlerSlot(i), 1, ContainerInput.THROW, (Player)ItemDropCommand.mc.player);
        }
    }

    private void dropSpecificItem(String string) {
        if (ItemDropCommand.mc.player == null || ItemDropCommand.mc.gameMode == null) {
            return;
        }
        int n = ItemDropCommand.mc.player.containerMenu.containerId;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = ItemDropCommand.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty()) continue;
            String string2 = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            String string3 = itemStack.getHoverName().getString();
            if (!string2.equalsIgnoreCase(string) && !string2.substring(string2.indexOf(58) + 1).equalsIgnoreCase(string) && !string3.equalsIgnoreCase(string)) continue;
            ItemDropCommand.mc.gameMode.handleContainerInput(n, ItemDropCommand.invToHandlerSlot(i), 1, ContainerInput.THROW, (Player)ItemDropCommand.mc.player);
        }
    }

    private static int invToHandlerSlot(int n) {
        return n <= 8 ? 36 + n : n;
    }
}

