/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 */
package lightning.utils.minecraft;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantmentUtils {
    public static int getEnchantmentLevel(ItemStack itemStack, ResourceKey<Enchantment> resourceKey) {
        if (itemStack == null || itemStack.isEmpty()) {
            return 0;
        }
        ItemEnchantments itemEnchantments = (ItemEnchantments)itemStack.get(DataComponents.ENCHANTMENTS);
        if (itemEnchantments == null || itemEnchantments.isEmpty()) {
            return 0;
        }
        for (Object2IntMap.Entry entry : itemEnchantments.entrySet()) {
            Optional optional = ((Holder)entry.getKey()).unwrapKey();
            if (!optional.isPresent() || !((ResourceKey)optional.get()).equals(resourceKey)) continue;
            return entry.getIntValue();
        }
        return 0;
    }
}

