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
    public void renderInfo(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, ItemStack itemStack) {
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
            Renderer2D.renderQuad(guiGraphicsExtractor, n += 4, n2 -= 62 + Night.FONT_MANAGER.getHeight(), n + 164, n2 + 60 + Night.FONT_MANAGER.getHeight(), new Color(0, 0, 0, 200));
            Renderer2D.renderOutline(guiGraphicsExtractor, n, n2, n + 164, n2 + 60 + Night.FONT_MANAGER.getHeight(), color);
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, itemStack.getHoverName().getString(), n + 3, n2 + 3, Color.WHITE);
            int n3 = 0;
            int n4 = 0;
            for (ItemStack itemStack2 : itemContainerContents.nonEmptyItemCopyStream().toList()) {
                int n5 = n + 1 + n4 * 18;
                int n6 = n2 + Night.FONT_MANAGER.getHeight() + 5 + n3 * 18;
                guiGraphicsExtractor.item(itemStack2, n5, n6);
                guiGraphicsExtractor.itemDecorations(ShulkerInfoModule.mc.font, itemStack2, n5, n6);
                if (++n4 < 9) continue;
                n4 = 0;
                ++n3;
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

