/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.util.Mth
 *  net.minecraft.world.Difficulty
 *  net.minecraft.world.damagesource.CombatRules
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.utils.minecraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DamageUtils
implements IMinecraft {
    private static final Map<String, Integer> PROTECTION_MAP = new HashMap<String, Integer>(){
        {
            this.put("protection", 1);
            this.put("blast_protection", 2);
            this.put("projectile_protection", 1);
            this.put("feather_falling", 1);
            this.put("fire_protection", 1);
        }
    };

    public static float getCrystalDamage(Entity entity, AABB aABB, EndCrystal endCrystal, boolean bl) {
        if (endCrystal == null) {
            return 0.0f;
        }
        return DamageUtils.getDamage(entity, aABB, endCrystal.position(), 6.0f, null, bl);
    }

    public static float getCrystalDamage(Entity entity, AABB aABB, BlockPos blockPos, BlockPos blockPos2, boolean bl) {
        if (blockPos == null) {
            return 0.0f;
        }
        return DamageUtils.getDamage(entity, aABB, new Vec3((double)blockPos.getX() + 0.5, (double)(blockPos.getY() + 1), (double)blockPos.getZ() + 0.5), 6.0f, blockPos2, bl);
    }

    public static float getDamage(Entity entity, AABB aABB, Vec3 vec3, float f, BlockPos blockPos, boolean bl) {
        Player player;
        if (DamageUtils.mc.level.getDifficulty() == Difficulty.PEACEFUL) {
            return 0.0f;
        }
        if (!(entity instanceof RemotePlayer) && entity instanceof Player && EntityUtils.getGameMode(player = (Player)entity) == GameType.CREATIVE) {
            return 0.0f;
        }
        float f2 = f * 2.0f;
        AABB aABB2 = aABB != null ? aABB : entity.getBoundingBox();
        Vec3 vec32 = new Vec3(aABB2.getCenter().x, aABB2.minY, aABB2.getCenter().z);
        double d = Math.sqrt(vec32.distanceToSqr(vec3)) / (double)f2;
        if (d > 1.0) {
            return 0.0f;
        }
        boolean bl2 = entity == DamageUtils.mc.player ? false : bl;
        double d2 = (1.0 - d) * (double)DamageUtils.getExposure(vec3, aABB2, blockPos, bl2);
        float f3 = (int)((d2 * d2 + d2) / 2.0 * 7.0 * (double)f2 + 1.0);
        if (f3 <= 0.0f) {
            return 0.0f;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            f3 = DamageUtils.mc.level.getDifficulty() == Difficulty.EASY ? Math.min(f3 / 2.0f + 1.0f, f3) : (DamageUtils.mc.level.getDifficulty() == Difficulty.HARD ? f3 * 3.0f / 2.0f : f3);
            f3 = CombatRules.getDamageAfterAbsorb((LivingEntity)livingEntity, (float)f3, (DamageSource)DamageUtils.mc.level.damageSources().explosion(null), (float)livingEntity.getArmorValue(), (float)((float)livingEntity.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()));
            f3 = (float)((double)f3 * (livingEntity.hasEffect(MobEffects.RESISTANCE) ? 1.0 - (double)(livingEntity.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 0.2 : 1.0));
            f3 = CombatRules.getDamageAfterMagicAbsorb((float)f3, (float)DamageUtils.getProtectionAmount(List.of(livingEntity.getItemBySlot(EquipmentSlot.FEET), livingEntity.getItemBySlot(EquipmentSlot.LEGS), livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.getItemBySlot(EquipmentSlot.HEAD))));
        }
        return Math.max(f3, 0.0f);
    }

    public static int getProtectionAmount(Iterable<ItemStack> iterable) {
        int n = 0;
        for (ItemStack itemStack : iterable) {
            n += DamageUtils.getProtectionAmount(itemStack);
        }
        return n;
    }

    public static int getProtectionAmount(ItemStack itemStack) {
        int n = 0;
        ItemEnchantments itemEnchantments = itemStack.getEnchantments();
        for (Holder holder : itemEnchantments.keySet()) {
            String string = holder.getRegisteredName().replace("minecraft:", "");
            if (!PROTECTION_MAP.containsKey(string)) continue;
            n += itemEnchantments.getLevel(holder) * PROTECTION_MAP.get(string);
            break;
        }
        return n;
    }

    private static float getExposure(Vec3 vec3, AABB aABB, BlockPos blockPos, boolean bl) {
        int n = 0;
        int n2 = 0;
        for (double d = 0.0; d <= 1.0; d += 0.4545454446934474) {
            for (double d2 = 0.0; d2 <= 1.0; d2 += 0.21739130885479366) {
                for (double d3 = 0.0; d3 <= 1.0; d3 += 0.4545454446934474) {
                    Vec3 vec32 = new Vec3(Mth.lerp((double)d, (double)aABB.minX, (double)aABB.maxX) + 0.045454555306552624, Mth.lerp((double)d2, (double)aABB.minY, (double)aABB.maxY), Mth.lerp((double)d3, (double)aABB.minZ, (double)aABB.maxZ) + 0.045454555306552624);
                    if (DamageUtils.raycast(vec32, vec3, blockPos, bl) == HitResult.Type.MISS) {
                        ++n;
                    }
                    ++n2;
                }
            }
        }
        return (float)n / (float)n2;
    }

    private static HitResult.Type raycast(Vec3 vec3, Vec3 vec32, BlockPos blockPos, boolean bl) {
        if (DamageUtils.mc.level == null) {
            return HitResult.Type.MISS;
        }
        return (HitResult.Type)BlockGetter.traverseBlocks((Vec3)vec3, (Vec3)vec32, null, (object, blockPos2) -> {
            BlockState blockState;
            if (blockPos2.equals((Object)blockPos)) {
                blockState = Blocks.AIR.defaultBlockState();
            } else {
                blockState = DamageUtils.mc.level.getBlockState(blockPos2);
                if (blockState.getBlock().getExplosionResistance() < 600.0f && bl) {
                    blockState = Blocks.AIR.defaultBlockState();
                }
            }
            BlockHitResult blockHitResult = blockState.getCollisionShape((BlockGetter)DamageUtils.mc.level, blockPos2).clip(vec3, vec32, blockPos2);
            return blockHitResult == null ? null : blockHitResult.getType();
        }, object -> HitResult.Type.MISS);
    }
}

