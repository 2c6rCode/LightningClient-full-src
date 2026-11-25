/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 */
package lightning.modules.impl.miscellaneous;

import java.awt.Color;
import lightning.Night;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

@RegisterModule(name="ShulkerInfo", description="Renders a preview of the contents of the shulker you are hovering.", category=Module.Category.MISCELLANEOUS)
public class ShulkerInfoModule
extends Module {
    public void renderInfo(GuiGraphicsExtractor guiGraphicsExtractor, int x, int y, ItemStack itemStack) {
        try {
            BlockItem blockItem;
            Block block;
            ItemContainerContents itemContainerContents = (ItemContainerContents)itemStack.get(DataComponents.CONTAINER);
            Item item = itemStack.getItem();
            Color color = Color.WHITE;
            if (item instanceof BlockItem && (block = (blockItem = (BlockItem)item).getBlock()) instanceof ShulkerBoxBlock) {
                ShulkerBoxBlock shulkerBoxBlock = (ShulkerBoxBlock)block;
                try {
                    color = new Color(shulkerBoxBlock.getColor().getTextureDiffuseColor());
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            Renderer2D.renderQuad(guiGraphicsExtractor, x += 4, y -= 62 + Night.FONT_MANAGER.getHeight(), x + 164, y + 60 + Night.FONT_MANAGER.getHeight(), new Color(0, 0, 0, 200));
            Renderer2D.renderOutline(guiGraphicsExtractor, x, y, x + 164, y + 60 + Night.FONT_MANAGER.getHeight(), color);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, itemStack.getHoverName().getString(), x + 3, y + 3, Color.WHITE);
            int row = 0;
            int col = 0;
            for (ItemStack slotStack : itemContainerContents.nonEmptyItemCopyStream().toList()) {
                int slotX = x + 1 + col * 18;
                int slotY = y + Night.FONT_MANAGER.getHeight() + 5 + row * 18;
                guiGraphicsExtractor.item(slotStack, slotX, slotY);
                guiGraphicsExtractor.itemDecorations(ShulkerInfoModule.mc.font, slotStack, slotX, slotY);
                if (++col < 9) continue;
                col = 0;
                ++row;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public boolean hasItems(ItemStack itemStack) {
        ItemContainerContents itemContainerContents = (ItemContainerContents)itemStack.get(DataComponents.CONTAINER);
        return itemContainerContents != null && !itemContainerContents.nonEmptyItemCopyStream().toList().isEmpty();
    }
}

