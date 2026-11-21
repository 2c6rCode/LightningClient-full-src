/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.alchemy.Potion
 *  net.minecraft.world.level.block.Block
 */
package lightning.settings.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lightning.settings.Setting;
import lombok.Generated;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;

public class WhitelistSetting
extends Setting {
    private final Type type;
    private final Set<Object> whitelist = new HashSet<Object>();

    public WhitelistSetting(String string, String string2, Type type) {
        super(string, string, string2, new Setting.Visibility());
        this.type = type;
    }

    public WhitelistSetting(String string, String string2, String string3, Type type) {
        super(string, string2, string3, new Setting.Visibility());
        this.type = type;
    }

    public WhitelistSetting(String string, String string2, Setting.Visibility visibility, Type type) {
        super(string, string, string2, visibility);
        this.type = type;
    }

    public WhitelistSetting(String string, String string2, String string3, Setting.Visibility visibility, Type type) {
        super(string, string2, string3, visibility);
        this.type = type;
    }

    public void add(Object object) {
        this.whitelist.add(object);
    }

    public void remove(Object object) {
        this.whitelist.remove(object);
    }

    public boolean isWhitelistContains(Object object) {
        return this.whitelist.contains(object);
    }

    public List<String> getWhitelistIds() {
        return this.whitelist.stream().<String>map(object -> {
            if (object instanceof Item) {
                Item item = (Item)object;
                return BuiltInRegistries.ITEM.getKey(item).toString();
            }
            if (object instanceof Block) {
                Block block = (Block)object;
                return BuiltInRegistries.BLOCK.getKey(block).toString();
            }
            if (object instanceof Potion) {
                Potion potion = (Potion)object;
                return BuiltInRegistries.POTION.getKey(potion).toString();
            }
            return null;
        }).toList();
    }

    public List<Potion> getWhitelistedPotions() {
        return this.whitelist.stream().filter(object -> object instanceof Potion).map(object -> (Potion)object).toList();
    }

    public void clear() {
        this.whitelist.clear();
    }

    @Generated
    public Type getType() {
        return this.type;
    }

    @Generated
    public Set<Object> getWhitelist() {
        return this.whitelist;
    }

    public static enum Type {
        ITEMS,
        BLOCKS,
        POTIONS;

    }
}

