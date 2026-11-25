/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 */
package lightning.commands.impl;

import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.gui.PeekMenu;
import lightning.gui.PeekScreen;
import lightning.utils.minecraft.EChestMemory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

@RegisterCommand(name="peek", tag="Peek", description="Peeks a shulker box held in hand, or your last-opened ender chest, without opening it.", syntax="")
public class PeekCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (PeekCommand.mc.player == null) {
            return;
        }
        EChestMemory.init();
        ItemStack itemStack = PeekCommand.mc.player.getMainHandItem();
        ItemContainerContents itemContainerContents = (ItemContainerContents)itemStack.get(DataComponents.CONTAINER);
        if (itemContainerContents == null) {
            itemStack = PeekCommand.mc.player.getOffhandItem();
            itemContainerContents = (ItemContainerContents)itemStack.get(DataComponents.CONTAINER);
        }
        if (itemContainerContents != null) {
            NonNullList nonNullList = NonNullList.withSize((int)27, (Object)ItemStack.EMPTY);
            itemContainerContents.copyInto(nonNullList);
            this.openPreview((NonNullList<ItemStack>)nonNullList, itemStack.getHoverName());
            return;
        }
        if (EChestMemory.hasItems()) {
            this.openPreview(EChestMemory.getItems(), (Component)Component.literal((String)"Ender Chest (remembered)"));
            return;
        }
        Night.CHAT_MANAGER.tagged("Hold a shulker box to peek it, or open your ender chest once so it can be remembered.", this.getTag(), this.getName());
    }

    private void openPreview(NonNullList<ItemStack> nonNullList, Component component) {
        SimpleContainer simpleContainer = new SimpleContainer(27);
        for (int i = 0; i < 27 && i < nonNullList.size(); ++i) {
            simpleContainer.setItem(i, ((ItemStack)nonNullList.get(i)).copy());
        }
        mc.execute(() -> {
            PeekMenu peekMenu = new PeekMenu(0, PeekCommand.mc.player.getInventory(), (Container)simpleContainer);
            PeekCommand.mc.player.containerMenu = peekMenu;
            PeekCommand.mc.gui.setScreen((Screen)new PeekScreen(peekMenu, PeekCommand.mc.player.getInventory(), component));
        });
    }
}

