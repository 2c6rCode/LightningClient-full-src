/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.NeutralMob
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.decoration.ItemFrame
 *  net.minecraft.world.entity.vehicle.VehicleEntity
 */
package lightning.modules.impl.player;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.WhitelistSetting;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.VehicleEntity;

@RegisterModule(name="NoEntityTrace", description="Allows you to interact with blocks, bypassing the entities between you and the block.", category=Module.Category.PLAYER)
public class NoEntityTraceModule
extends Module {
    public BooleanSetting pickaxeOnly = new BooleanSetting("PickaxeOnly", "Only bypasses entities if you're holding a pickaxe.", false);
    public BooleanSetting angryMobs = new BooleanSetting("AngryMobs", "Only bypasses non-living entities (Crystals, Boats, Minecarts) and neutral/angerable mobs (Endermen, Piglins, Golems, etc.).", false);
    public WhitelistSetting ignoredItem = new WhitelistSetting("IgnoredItem", "Do not bypasses entities if you're holding these items.", WhitelistSetting.Type.ITEMS);

    public boolean shouldIgnore() {
        return this.shouldIgnore(null);
    }

    public boolean shouldIgnore(Entity entity) {
        if (NoEntityTraceModule.mc.player == null) {
            return false;
        }
        if (this.pickaxeOnly.getValue() && !NoEntityTraceModule.mc.player.getMainHandItem().is(ItemTags.PICKAXES)) {
            return false;
        }
        if (this.ignoredItem.isWhitelistContains(NoEntityTraceModule.mc.player.getMainHandItem().getItem())) {
            return false;
        }
        if (this.angryMobs.getValue() && entity != null) {
            boolean isNonLiving = entity instanceof EndCrystal || entity instanceof VehicleEntity || entity instanceof ItemFrame || entity instanceof ArmorStand || !(entity instanceof LivingEntity);
            boolean isNeutral = entity instanceof NeutralMob;
            return isNonLiving || isNeutral;
        }
        return true;
    }
}

