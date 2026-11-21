/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Mth
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.NeutralMob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.animal.Animal
 *  net.minecraft.world.entity.animal.bee.Bee
 *  net.minecraft.world.entity.animal.golem.IronGolem
 *  net.minecraft.world.entity.animal.panda.Panda
 *  net.minecraft.world.entity.animal.polarbear.PolarBear
 *  net.minecraft.world.entity.monster.EnderMan
 *  net.minecraft.world.entity.monster.Endermite
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.monster.piglin.Piglin
 *  net.minecraft.world.entity.monster.piglin.PiglinArmPose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.item.EggItem
 *  net.minecraft.world.item.EnderpearlItem
 *  net.minecraft.world.item.ExperienceBottleItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.LingeringPotionItem
 *  net.minecraft.world.item.SnowballItem
 *  net.minecraft.world.item.SplashPotionItem
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package lightning.utils.minecraft;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lightning.Night;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EntityUtils
implements IMinecraft {
    public static Map<MobEffect, Color> POTION_COLORS = new HashMap<MobEffect, Color>();
    private static final Identifier ATTACKING_SPEED_MODIFIER;

    public static boolean isPhased(Entity entity) {
        if (entity == null || EntityUtils.mc.level == null) {
            return false;
        }
        AABB aABB = entity.getBoundingBox();
        int n = Mth.floor((double)aABB.minX);
        int n2 = Mth.ceil((double)aABB.maxX);
        int n3 = Mth.floor((double)aABB.minY);
        int n4 = Mth.ceil((double)aABB.maxY);
        int n5 = Mth.floor((double)aABB.minZ);
        int n6 = Mth.ceil((double)aABB.maxZ);
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    VoxelShape voxelShape = EntityUtils.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)EntityUtils.mc.level, blockPos);
                    if (voxelShape.isEmpty() || !voxelShape.bounds().move(blockPos).intersects(aABB)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static List<BlockPos> getPhasedBlocks(Entity entity) {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        if (entity == null || EntityUtils.mc.level == null) {
            return arrayList;
        }
        AABB aABB = entity.getBoundingBox();
        int n = Mth.floor((double)aABB.minX);
        int n2 = Mth.ceil((double)aABB.maxX);
        int n3 = Mth.floor((double)aABB.minY);
        int n4 = Mth.ceil((double)aABB.maxY);
        int n5 = Mth.floor((double)aABB.minZ);
        int n6 = Mth.ceil((double)aABB.maxZ);
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    VoxelShape voxelShape = EntityUtils.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)EntityUtils.mc.level, blockPos);
                    if (voxelShape.isEmpty() || !voxelShape.bounds().move(blockPos).intersects(aABB)) continue;
                    arrayList.add(blockPos);
                }
            }
        }
        return arrayList;
    }

    public static boolean isEating() {
        if (EntityUtils.mc.player == null) {
            return false;
        }
        if (EntityUtils.mc.player.isUsingItem()) {
            return true;
        }
        if (EntityUtils.mc.options.keyUse.isDown()) {
            ItemStack itemStack = EntityUtils.mc.player.getMainHandItem();
            ItemStack itemStack2 = EntityUtils.mc.player.getOffhandItem();
            if (!itemStack.isEmpty() && itemStack.getUseDuration((LivingEntity)EntityUtils.mc.player) > 0) {
                return true;
            }
            if (!itemStack2.isEmpty() && itemStack2.getUseDuration((LivingEntity)EntityUtils.mc.player) > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBot(Player player) {
        if (Night.MODULE_MANAGER.getModule(FakePlayerModule.class).isToggled() && player == Night.MODULE_MANAGER.getModule(FakePlayerModule.class).getPlayer()) {
            return false;
        }
        PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(player.getUUID());
        return playerInfo == null || playerInfo.getProfile() == null || player.getUUID().toString().startsWith(player.getName().getString()) || !player.getGameProfile().name().equals(player.getName().getString());
    }

    public static boolean isNaked(Player player) {
        if (player == null) {
            return false;
        }
        for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack itemStack = player.getItemBySlot(equipmentSlot);
            if (itemStack.isEmpty() || equipmentSlot == EquipmentSlot.CHEST && itemStack.getItem() == Items.ELYTRA) continue;
            return false;
        }
        return true;
    }

    public static boolean isGhost(Entity entity) {
        if (entity == null || Night.MODULE_MANAGER == null) {
            return false;
        }
        PopChamsModule popChamsModule = Night.MODULE_MANAGER.getModule(PopChamsModule.class);
        if (popChamsModule != null && popChamsModule.isGhost(entity)) {
            return true;
        }
        LogoutSpotModule logoutSpotModule = Night.MODULE_MANAGER.getModule(LogoutSpotModule.class);
        return logoutSpotModule != null && logoutSpotModule.isGhost(entity);
    }

    public static int getLatency(Player player) {
        PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(player.getUUID());
        return playerInfo == null ? 0 : playerInfo.getLatency();
    }

    public static GameType getGameMode(Player player) {
        PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(player.getUUID());
        return playerInfo == null ? GameType.CREATIVE : playerInfo.getGameMode();
    }

    public static String getGameModeName(GameType gameType) {
        return switch (gameType) {
            case GameType.CREATIVE -> "C";
            case GameType.ADVENTURE -> "A";
            case GameType.SPECTATOR -> "SP";
            default -> "S";
        };
    }

    public static double getSpeed(Entity entity, SpeedUnit speedUnit) {
        double d = Math.sqrt(Mth.square((double)Math.abs(entity.getX() - entity.xo)) + Mth.square((double)Math.abs(entity.getZ() - entity.zo)));
        if (speedUnit == SpeedUnit.KILOMETERS) {
            return d * 3.6 * (double)Night.WORLD_MANAGER.getTimerMultiplier() * 20.0;
        }
        return d / 0.05 * (double)Night.WORLD_MANAGER.getTimerMultiplier();
    }

    public static boolean hasNegativeEffects(Player player) {
        for (MobEffectInstance mobEffectInstance : player.getActiveEffects()) {
            if (((MobEffect)mobEffectInstance.getEffect().value()).isBeneficial()) continue;
            return true;
        }
        return false;
    }

    public static Vec3 getRenderPos(Entity entity, float f) {
        double d = Mth.lerp((double)f, (double)entity.xo, (double)entity.getX());
        double d2 = Mth.lerp((double)f, (double)entity.yo, (double)entity.getY());
        double d3 = Mth.lerp((double)f, (double)entity.zo, (double)entity.getZ());
        return new Vec3(d, d2, d3);
    }

    public static LivingEntity getClosestEntity(Entity entity) {
        LivingEntity closest = null;
        for (Entity candidate : EntityUtils.mc.level.entitiesForRendering()) {
            if (!(candidate instanceof LivingEntity target)) continue;
            if (entity.distanceTo(target) > 10.0f || target.getHealth() <= 0.0f || !target.isAlive() || entity == target) continue;
            if (closest == null || entity.distanceTo(target) < entity.distanceTo(closest)) {
                closest = target;
            }
        }
        return closest;
    }

    public static Direction getPearlDirection(ThrownEnderpearl thrownEnderpearl) {
        Direction direction = thrownEnderpearl.getDirection();
        if (direction.equals((Object)Direction.WEST)) {
            return Direction.EAST;
        }
        if (direction.equals((Object)Direction.EAST)) {
            return Direction.WEST;
        }
        return direction;
    }

    public static boolean isThrowable(Item item) {
        return item instanceof EnderpearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    public static boolean isInWeb(Entity entity) {
        for (float f : new float[]{0.0f, 0.3f, -0.3f}) {
            for (float f2 : new float[]{0.0f, 0.3f, -0.3f}) {
                for (int n : new int[]{-1, 0, 1, 2}) {
                    BlockPos blockPos = BlockPos.containing((double)(entity.getX() + (double)f), (double)entity.getY(), (double)(entity.getZ() + (double)f2)).above(n);
                    if (!new AABB(blockPos).intersects(entity.getBoundingBox()) || EntityUtils.mc.level.getBlockState(blockPos).getBlock() != Blocks.COBWEB) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isNeutral(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity.getType() == EntityTypes.PIGLIN || entity.getType() == EntityTypes.PIGLIN_BRUTE) {
            return false;
        }
        if (entity instanceof NeutralMob) {
            return true;
        }
        EntityType entityType = entity.getType();
        return entityType == EntityTypes.ZOMBIFIED_PIGLIN || entityType == EntityTypes.ENDERMAN || entityType == EntityTypes.IRON_GOLEM || entityType == EntityTypes.WOLF || entityType == EntityTypes.POLAR_BEAR || entityType == EntityTypes.BEE || entityType == EntityTypes.DOLPHIN || entityType == EntityTypes.GOAT || entityType == EntityTypes.PANDA || entityType == EntityTypes.LLAMA || entityType == EntityTypes.TRADER_LLAMA;
    }

    public static boolean isAngry(Entity entity) {
        Piglin piglin;
        PiglinArmPose piglinArmPose;
        Panda panda;
        Bee bee;
        IronGolem ironGolem;
        PolarBear polarBear;
        EnderMan enderMan;
        NeutralMob neutralMob;
        if (!(entity instanceof Mob)) {
            return false;
        }
        Mob mob = (Mob)entity;
        if (mob.isAggressive()) {
            return true;
        }
        AttributeInstance attributeInstance = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeInstance != null && attributeInstance.hasModifier(ATTACKING_SPEED_MODIFIER)) {
            return true;
        }
        if (entity instanceof NeutralMob && (neutralMob = (NeutralMob)entity).isAngry()) {
            return true;
        }
        if (entity instanceof EnderMan && ((enderMan = (EnderMan)entity).isCreepy() || enderMan.hasBeenStaredAt())) {
            return true;
        }
        if (entity instanceof PolarBear && (polarBear = (PolarBear)entity).isStanding()) {
            return true;
        }
        if (entity instanceof IronGolem && (ironGolem = (IronGolem)entity).getAttackAnimationTick() > 0) {
            return true;
        }
        if (entity instanceof Bee && ((bee = (Bee)entity).hasStung() || bee.isAngry())) {
            return true;
        }
        if (entity instanceof Panda && (panda = (Panda)entity).isAggressive()) {
            return true;
        }
        if (entity instanceof Piglin && ((piglinArmPose = (piglin = (Piglin)entity).getArmPose()) == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON || piglinArmPose == PiglinArmPose.CROSSBOW_HOLD || piglinArmPose == PiglinArmPose.CROSSBOW_CHARGE)) {
            return true;
        }
        if (EntityUtils.mc.player != null) {
            DamageSource damageSource = EntityUtils.mc.player.getLastDamageSource();
            if (damageSource != null && damageSource.getEntity() == entity) {
                return true;
            }
            DamageSource damageSource2 = mob.getLastDamageSource();
            if (damageSource2 != null && damageSource2.getEntity() == EntityUtils.mc.player) {
                return true;
            }
        }
        if (mob.getTarget() != null) {
            return true;
        }
        return entity instanceof NeutralMob && (neutralMob = (NeutralMob)entity).getPersistentAngerTarget() != null;
    }

    public static boolean isHostile(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (EntityUtils.isAngry(entity)) {
            return true;
        }
        if (EntityUtils.isNeutral(entity)) {
            return false;
        }
        if (entity instanceof Enemy || entity instanceof Monster || entity instanceof Endermite) {
            return true;
        }
        if (entity.getType() == EntityTypes.ENDERMITE || entity.getType() == EntityTypes.SILVERFISH) {
            return true;
        }
        return entity.getType().getCategory() == MobCategory.MONSTER;
    }

    public static boolean isAnimal(Entity entity) {
        if (entity == null || EntityUtils.isNeutral(entity) || EntityUtils.isHostile(entity)) {
            return false;
        }
        if (entity instanceof Animal) {
            return true;
        }
        MobCategory mobCategory = entity.getType().getCategory();
        return mobCategory == MobCategory.CREATURE || mobCategory == MobCategory.WATER_CREATURE || mobCategory == MobCategory.WATER_AMBIENT || mobCategory == MobCategory.UNDERGROUND_WATER_CREATURE || mobCategory == MobCategory.AXOLOTLS || mobCategory == MobCategory.AMBIENT;
    }

    public static boolean isPassive(Entity entity) {
        return EntityUtils.isAnimal(entity);
    }

    static {
        POTION_COLORS.put((MobEffect)MobEffects.SPEED.value(), new Color(124, 175, 198));
        POTION_COLORS.put((MobEffect)MobEffects.SLOWNESS.value(), new Color(90, 108, 129));
        POTION_COLORS.put((MobEffect)MobEffects.HASTE.value(), new Color(217, 192, 67));
        POTION_COLORS.put((MobEffect)MobEffects.MINING_FATIGUE.value(), new Color(74, 66, 23));
        POTION_COLORS.put((MobEffect)MobEffects.STRENGTH.value(), new Color(147, 36, 35));
        POTION_COLORS.put((MobEffect)MobEffects.INSTANT_HEALTH.value(), new Color(67, 10, 9));
        POTION_COLORS.put((MobEffect)MobEffects.INSTANT_DAMAGE.value(), new Color(67, 10, 9));
        POTION_COLORS.put((MobEffect)MobEffects.JUMP_BOOST.value(), new Color(34, 255, 76));
        POTION_COLORS.put((MobEffect)MobEffects.NAUSEA.value(), new Color(85, 29, 74));
        POTION_COLORS.put((MobEffect)MobEffects.REGENERATION.value(), new Color(205, 92, 171));
        POTION_COLORS.put((MobEffect)MobEffects.RESISTANCE.value(), new Color(153, 69, 58));
        POTION_COLORS.put((MobEffect)MobEffects.FIRE_RESISTANCE.value(), new Color(228, 154, 58));
        POTION_COLORS.put((MobEffect)MobEffects.WATER_BREATHING.value(), new Color(46, 82, 153));
        POTION_COLORS.put((MobEffect)MobEffects.INVISIBILITY.value(), new Color(127, 131, 146));
        POTION_COLORS.put((MobEffect)MobEffects.BLINDNESS.value(), new Color(31, 31, 35));
        POTION_COLORS.put((MobEffect)MobEffects.NIGHT_VISION.value(), new Color(31, 31, 161));
        POTION_COLORS.put((MobEffect)MobEffects.HUNGER.value(), new Color(88, 118, 83));
        POTION_COLORS.put((MobEffect)MobEffects.WEAKNESS.value(), new Color(72, 77, 72));
        POTION_COLORS.put((MobEffect)MobEffects.POISON.value(), new Color(78, 147, 49));
        POTION_COLORS.put((MobEffect)MobEffects.WITHER.value(), new Color(53, 42, 39));
        POTION_COLORS.put((MobEffect)MobEffects.HEALTH_BOOST.value(), new Color(248, 125, 35));
        POTION_COLORS.put((MobEffect)MobEffects.ABSORPTION.value(), new Color(37, 82, 165));
        POTION_COLORS.put((MobEffect)MobEffects.SATURATION.value(), new Color(248, 36, 35));
        POTION_COLORS.put((MobEffect)MobEffects.GLOWING.value(), new Color(148, 160, 97));
        POTION_COLORS.put((MobEffect)MobEffects.LEVITATION.value(), new Color(206, 255, 255));
        POTION_COLORS.put((MobEffect)MobEffects.LUCK.value(), new Color(51, 153, 0));
        POTION_COLORS.put((MobEffect)MobEffects.UNLUCK.value(), new Color(192, 164, 77));
        ATTACKING_SPEED_MODIFIER = Identifier.withDefaultNamespace((String)"attacking");
    }

    public static enum SpeedUnit {
        METERS,
        KILOMETERS;

    }
}

