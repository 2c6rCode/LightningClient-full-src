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

    public static float getCrystalDamage(Entity entity, AABB box, EndCrystal endCrystal, boolean ignoreTerrain) {
        if (endCrystal == null) {
            return 0.0f;
        }
        return DamageUtils.getDamage(entity, box, endCrystal.position(), 6.0f, null, ignoreTerrain);
    }

    public static float getCrystalDamage(Entity entity, AABB box, BlockPos blockPos, BlockPos exceptionPos, boolean ignoreTerrain) {
        if (blockPos == null) {
            return 0.0f;
        }
        return DamageUtils.getDamage(entity, box, new Vec3((double)blockPos.getX() + 0.5, (double)(blockPos.getY() + 1), (double)blockPos.getZ() + 0.5), 6.0f, exceptionPos, ignoreTerrain);
    }

    public static float getDamage(Entity entity, AABB box, Vec3 explosionPos, float power, BlockPos exceptionPos, boolean ignoreTerrain) {
        Player player;
        if (DamageUtils.mc.level.getDifficulty() == Difficulty.PEACEFUL) {
            return 0.0f;
        }
        if (!(entity instanceof RemotePlayer) && entity instanceof Player && EntityUtils.getGameMode(player = (Player)entity) == GameType.CREATIVE) {
            return 0.0f;
        }
        float doublePower = power * 2.0f;
        AABB targetBox = box != null ? box : entity.getBoundingBox();
        Vec3 centerBottom = new Vec3(targetBox.getCenter().x, targetBox.minY, targetBox.getCenter().z);
        double distanceRatio = Math.sqrt(centerBottom.distanceToSqr(explosionPos)) / (double)doublePower;
        if (distanceRatio > 1.0) {
            return 0.0f;
        }
        boolean shouldIgnoreTerrain = entity == DamageUtils.mc.player ? false : ignoreTerrain;
        double impact = (1.0 - distanceRatio) * (double)DamageUtils.getExposure(explosionPos, targetBox, exceptionPos, shouldIgnoreTerrain);
        float damage = (int)((impact * impact + impact) / 2.0 * 7.0 * (double)doublePower + 1.0);
        if (damage <= 0.0f) {
            return 0.0f;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            damage = DamageUtils.mc.level.getDifficulty() == Difficulty.EASY ? Math.min(damage / 2.0f + 1.0f, damage) : (DamageUtils.mc.level.getDifficulty() == Difficulty.HARD ? damage * 3.0f / 2.0f : damage);
            damage = CombatRules.getDamageAfterAbsorb((LivingEntity)livingEntity, (float)damage, (DamageSource)DamageUtils.mc.level.damageSources().explosion(null), (float)livingEntity.getArmorValue(), (float)((float)livingEntity.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()));
            damage = (float)((double)damage * (livingEntity.hasEffect(MobEffects.RESISTANCE) ? 1.0 - (double)(livingEntity.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 0.2 : 1.0));
            damage = CombatRules.getDamageAfterMagicAbsorb((float)damage, (float)DamageUtils.getProtectionAmount(List.of(livingEntity.getItemBySlot(EquipmentSlot.FEET), livingEntity.getItemBySlot(EquipmentSlot.LEGS), livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.getItemBySlot(EquipmentSlot.HEAD))));
        }
        return Math.max(damage, 0.0f);
    }

    public static int getProtectionAmount(Iterable<ItemStack> armorItems) {
        int totalProtection = 0;
        for (ItemStack itemStack : armorItems) {
            totalProtection += DamageUtils.getProtectionAmount(itemStack);
        }
        return totalProtection;
    }

    public static int getProtectionAmount(ItemStack itemStack) {
        int protection = 0;
        ItemEnchantments itemEnchantments = itemStack.getEnchantments();
        for (Holder holder : itemEnchantments.keySet()) {
            String enchantName = holder.getRegisteredName().replace("minecraft:", "");
            if (!PROTECTION_MAP.containsKey(enchantName)) continue;
            protection += itemEnchantments.getLevel(holder) * PROTECTION_MAP.get(enchantName);
            break;
        }
        return protection;
    }

    private static float getExposure(Vec3 explosionPos, AABB box, BlockPos exceptionPos, boolean ignoreTerrain) {
        int misses = 0;
        int totalRays = 0;
        for (double xStep = 0.0; xStep <= 1.0; xStep += 0.4545454446934474) {
            for (double yStep = 0.0; yStep <= 1.0; yStep += 0.21739130885479366) {
                for (double zStep = 0.0; zStep <= 1.0; zStep += 0.4545454446934474) {
                    Vec3 samplePoint = new Vec3(Mth.lerp((double)xStep, (double)box.minX, (double)box.maxX) + 0.045454555306552624, Mth.lerp((double)yStep, (double)box.minY, (double)box.maxY), Mth.lerp((double)zStep, (double)box.minZ, (double)box.maxZ) + 0.045454555306552624);
                    if (DamageUtils.raycast(samplePoint, explosionPos, exceptionPos, ignoreTerrain) == HitResult.Type.MISS) {
                        ++misses;
                    }
                    ++totalRays;
                }
            }
        }
        return (float)misses / (float)totalRays;
    }

    private static HitResult.Type raycast(Vec3 start, Vec3 end, BlockPos exceptionPos, boolean ignoreTerrain) {
        if (DamageUtils.mc.level == null) {
            return HitResult.Type.MISS;
        }
        return (HitResult.Type)BlockGetter.traverseBlocks((Vec3)start, (Vec3)end, null, (context, pos) -> {
            BlockState blockState;
            if (pos.equals((Object)exceptionPos)) {
                blockState = Blocks.AIR.defaultBlockState();
            } else {
                blockState = DamageUtils.mc.level.getBlockState(pos);
                if (blockState.getBlock().getExplosionResistance() < 600.0f && ignoreTerrain) {
                    blockState = Blocks.AIR.defaultBlockState();
                }
            }
            BlockHitResult blockHitResult = blockState.getCollisionShape((BlockGetter)DamageUtils.mc.level, pos).clip(start, end, pos);
            return blockHitResult == null ? null : blockHitResult.getType();
        }, context -> HitResult.Type.MISS);
    }
}

