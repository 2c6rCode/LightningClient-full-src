/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.alchemy.Potion
 *  net.minecraft.world.level.block.Block
 */
package lightning.utils.minecraft;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;

public class IdentifierUtils {
    public static Item getItem(String name) {
        try {
            Item item = (Item)IdentifierUtils.getIdentifier(BuiltInRegistries.ITEM, name);
            if (item != null) {
                return item;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    public static Block getBlock(String name) {
        try {
            Block block = (Block)IdentifierUtils.getIdentifier(BuiltInRegistries.BLOCK, name);
            if (block != null) {
                return block;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    public static Potion getPotion(String name) {
        try {
            Potion potion = (Potion)IdentifierUtils.getIdentifier(BuiltInRegistries.POTION, name);
            if (potion != null) {
                return potion;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    public static <T> T getIdentifier(Registry<T> registry, String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String normalizedName = name.trim();
        Identifier identifier = Identifier.tryParse((String)(normalizedName.contains(":") ? normalizedName : "minecraft:" + normalizedName));
        if (identifier == null) {
            return null;
        }
        if (registry.containsKey(identifier)) {
            return (T)registry.getValue(identifier);
        }
        return null;
    }
}

