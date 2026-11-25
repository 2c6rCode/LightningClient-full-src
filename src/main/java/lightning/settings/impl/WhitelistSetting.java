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

    public WhitelistSetting(String name, String description, Type type) {
        super(name, name, description, new Setting.Visibility());
        this.type = type;
    }

    public WhitelistSetting(String name, String tag, String description, Type type) {
        super(name, tag, description, new Setting.Visibility());
        this.type = type;
    }

    public WhitelistSetting(String name, String description, Setting.Visibility visibility, Type type) {
        super(name, name, description, visibility);
        this.type = type;
    }

    public WhitelistSetting(String name, String tag, String description, Setting.Visibility visibility, Type type) {
        super(name, tag, description, visibility);
        this.type = type;
    }

    public void add(Object entry) {
        this.whitelist.add(entry);
    }

    public void remove(Object entry) {
        this.whitelist.remove(entry);
    }

    public boolean isWhitelistContains(Object entry) {
        return this.whitelist.contains(entry);
    }

    public List<String> getWhitelistIds() {
        return this.whitelist.stream().<String>map(entry -> {
            if (entry instanceof Item) {
                Item item = (Item)entry;
                return BuiltInRegistries.ITEM.getKey(item).toString();
            }
            if (entry instanceof Block) {
                Block block = (Block)entry;
                return BuiltInRegistries.BLOCK.getKey(block).toString();
            }
            if (entry instanceof Potion) {
                Potion potion = (Potion)entry;
                return BuiltInRegistries.POTION.getKey(potion).toString();
            }
            return null;
        }).toList();
    }

    public List<Potion> getWhitelistedPotions() {
        return this.whitelist.stream().filter(entry -> entry instanceof Potion).map(entry -> (Potion)entry).toList();
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

