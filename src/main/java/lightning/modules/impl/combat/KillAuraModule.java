/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.Creeper
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ShulkerBullet
 *  net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.MaceItem
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.item.component.ItemAttributeModifiers
 *  net.minecraft.world.item.component.ItemAttributeModifiers$Entry
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EnchantmentUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.GrimUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="KillAura", description="Automatically attacks optimal targets using Nami's combat engine.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class KillAuraModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "The reach distance to entities.", 3.0, 1.0, 6.0);
    public BooleanSetting stanceAbuse = new BooleanSetting("StanceAbuse", "Abuses Grim stance eye positions to optimize reach.", false);
    public NumberSetting delay = new NumberSetting("Delay", "Attack cooldown delay multiplier.", 0.92, 0.0, 1.0);
    public ModeSetting swap = new ModeSetting("Swap", "Weapon switch mode.", "Require", new String[]{"None", "Require", "Normal", "Silent"});
    public BooleanSetting onlyGapHold = new BooleanSetting("OnlyGapHold", "Only attacks using silent swap when holding a golden apple in main hand.", new ModeSetting.Visibility(this.swap, "Silent"), false);
    public BooleanSetting onlyTotemHold = new BooleanSetting("OnlyTotemHold", "Only attacks using silent swap when holding a totem in main hand.", new ModeSetting.Visibility(this.swap, "Silent"), false);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses attacking while eating or using items.", false);
    public ModeSetting tpsMode = new ModeSetting("TPS", "TPS synchronization mode for attack cooldown.", "Average", new String[]{"None", "Average"});
    public BooleanSetting multiTask = new BooleanSetting("Multitask", "Allows attacking while eating or using items.", true);
    public ModeSetting stopSprinting = new ModeSetting("Sprinting", "Sprint management during attacks.", "None", new String[]{"None", "Packet"});
    public ModeSetting rotate = new ModeSetting("Rotate", "Rotation mode.", "Normal", new String[]{"None", "Normal", "Hold", "Silent"});
    public BooleanSetting swing = new BooleanSetting("Swing", "Whether to swing your hand when attacking.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Renders an indicator around the current target.", true);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "Target render mode.", new BooleanSetting.Visibility(this.render, true), "Circle", new String[]{"Box", "Circle", "Both"});
    public CategorySetting entitiesCategory = new CategorySetting("Entities", "Target entity filter settings.");
    public BooleanSetting players = new BooleanSetting("Players", "Target player entities.", new CategorySetting.Visibility(this.entitiesCategory), true);
    public BooleanSetting friends = new BooleanSetting("Friends", "Target friends.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Target hostile mobs (Zombies, Skeletons, Spiders, Piglins, etc.).", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting neutrals = new BooleanSetting("Neutrals", "Target neutral mobs (Endermen, Zombie Piglins, etc.).", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting animals = new BooleanSetting("Animals", "Target passive animal mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting projectiles = new BooleanSetting("Projectiles", "Target shulker bullets & fireballs.", new CategorySetting.Visibility(this.entitiesCategory), true);
    public ColorSetting boxColor = new ColorSetting("TargetColor", "Color of the target render box.", new BooleanSetting.Visibility(this.render, true), ColorUtils.getDefaultFillColor());
    public Entity target = null;
    private Entity lastTarget = null;
    private long lastTargetTime = 0L;
    private static final long FADE_TIME_MS = 300L;
    private long lastAttackTime = 0L;
    private int originalSlot = -1;
    private int silentHeldWeaponSlot = -1;
    private int silentHeldReturnSlot = -1;

    public Entity getTarget() {
        return this.target;
    }

    private void releaseSilentHold() {
        if (this.silentHeldWeaponSlot == -1) {
            return;
        }
        InventoryUtils.clearLongHold(this.silentHeldWeaponSlot);
        if (KillAuraModule.mc.player != null) {
            InventoryUtils.switchBack("Silent", this.silentHeldWeaponSlot, this.silentHeldReturnSlot);
        }
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        if (this.silentHeldWeaponSlot != -1) {
            InventoryUtils.clearLongHold(this.silentHeldWeaponSlot);
        }
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
        this.originalSlot = -1;
        this.target = null;
        this.lastTarget = null;
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.lastTarget = null;
        this.originalSlot = -1;
        this.lastAttackTime = 0L;
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
    }

    @Override
    public void onDisable() {
        if (this.originalSlot != -1 && KillAuraModule.mc.player != null) {
            InventoryUtils.switchSlot("Normal", this.originalSlot, KillAuraModule.mc.player.getInventory().getSelectedSlot());
            this.originalSlot = -1;
        }
        this.releaseSilentHold();
        this.target = null;
        this.lastTarget = null;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy()) {
            this.releaseSilentHold();
        } else if (KillAuraModule.mc.player != null && KillAuraModule.mc.level != null && !KillAuraModule.mc.player.isDeadOrDying()) {
            AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
            if (autoCrystalModule != null && autoCrystalModule.isToggled() && autoCrystalModule.getTarget() != null) {
                this.releaseSilentHold();
            } else {
                boolean isUsingItem = KillAuraModule.mc.options.keyUse.isDown() || KillAuraModule.mc.player.isUsingItem() || KillAuraModule.mc.player.getUseItemRemainingTicks() > 0;
                if (this.pauseOnEat.getValue() && isUsingItem) {
                    this.target = null;
                    this.releaseSilentHold();
                } else if (!this.multiTask.getValue() && isUsingItem) {
                    this.target = null;
                    this.releaseSilentHold();
                } else {
                    Entity bestTarget;
                    ItemStack itemStack = KillAuraModule.mc.player.getMainHandItem();
                    if (this.swap.getValue().equalsIgnoreCase("Silent")) {
                        boolean onlyGapple = this.onlyGapHold.getValue();
                        boolean onlyTotem = this.onlyTotemHold.getValue();
                        if (onlyGapple || onlyTotem) {
                            boolean holdingGapple = itemStack.is(Items.GOLDEN_APPLE) || itemStack.is(Items.ENCHANTED_GOLDEN_APPLE);
                            boolean holdingTotem = itemStack.is(Items.TOTEM_OF_UNDYING);
                            boolean validHold = onlyGapple && holdingGapple || onlyTotem && holdingTotem;
                            if (!validHold) {
                                this.target = null;
                                this.releaseSilentHold();
                                return;
                            }
                        }
                    }
                    if ((bestTarget = this.findOptimalTarget()) != null && (!this.swap.getValue().equalsIgnoreCase("Require") || KillAuraModule.isItemAWeapon(itemStack))) {
                        double preRotateWindowMs;
                        this.target = bestTarget;
                        boolean instantKill = false;
                        if (!(this.target instanceof ShulkerBullet) && !(this.target instanceof LargeFireball)) {
                            LivingEntity livingEntity;
                            float playerDamage = this.calculatePlayerAttackDamage();
                            Entity targetEntity = this.target;
                            if (targetEntity instanceof LivingEntity && (livingEntity = (LivingEntity)targetEntity).getMaxHealth() <= playerDamage) {
                                instantKill = true;
                            }
                        } else {
                            instantKill = true;
                        }
                        int bestWeaponSlot = this.getBestWeaponSlot();
                        double cooldownMs = this.getCooldownMs(bestWeaponSlot);
                        long now = System.currentTimeMillis();
                        boolean readyToAttack = instantKill || (double)(now - this.lastAttackTime) >= cooldownMs;
                        switch (this.rotate.getValue()) {
                            case "Hold": {
                                preRotateWindowMs = 1000.0;
                                break;
                            }
                            case "Normal": {
                                preRotateWindowMs = 100.0;
                                break;
                            }
                            default: {
                                preRotateWindowMs = 0.0;
                                break;
                            }
                        }
                        if (instantKill || readyToAttack || cooldownMs - (double)(now - this.lastAttackTime) <= preRotateWindowMs) {
                            boolean canAttack;
                            Vec3 eyePos = KillAuraModule.mc.player.getEyePosition(1.0f);
                            if (this.stanceAbuse.getValue()) {
                                double bestEyeDist = Double.MAX_VALUE;
                                for (Vec3 pos : GrimUtils.getPossibleEyePositions((Player)KillAuraModule.mc.player)) {
                                    Vec3 closest = RotationUtils.getClampClosestPoint(pos, this.target.getBoundingBox());
                                    double dist = pos.distanceTo(closest);
                                    if (!(dist < bestEyeDist)) continue;
                                    bestEyeDist = dist;
                                    eyePos = pos;
                                }
                            }
                            Vec3 aimPoint = this.target.getBoundingBox().getCenter();
                            Entity aimEntity = this.target;
                            if (aimEntity instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity)aimEntity;
                                if (this.target.getBoundingBox().getYsize() >= 0.8) {
                                    aimPoint = new Vec3(this.target.getX(), this.target.getY() + (double)livingEntity.getEyeHeight() * 0.75, this.target.getZ());
                                }
                            }
                            float yaw = RotationUtils.getYRotToVec(eyePos, aimPoint);
                            float pitch = RotationUtils.getXRotToVec(eyePos, aimPoint);
                            boolean eyeInsideBox = this.target.getBoundingBox().contains(eyePos);
                            if (this.getTargetDistance(eyePos, this.target) > this.range.getValue().doubleValue()) {
                                this.target = null;
                                this.releaseSilentHold();
                                return;
                            }
                            canAttack = this.rotate.getValue().equalsIgnoreCase("None") || eyeInsideBox;
                            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                                if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                    Night.ROTATION_MANAGER.silentRotate(yaw, pitch);
                                    canAttack = true;
                                } else {
                                    Night.ROTATION_MANAGER.legacyRotate(yaw, pitch, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                    boolean raycastHit = RotationUtils.raycastTarget(eyePos, this.target, this.range.getValue().doubleValue(), Night.ROTATION_MANAGER.getServerYaw(), Night.ROTATION_MANAGER.getServerPitch()) != null;
                                    canAttack = raycastHit || eyeInsideBox;
                                }
                            }
                            if (!canAttack) {
                                return;
                            }
                        }
                        if (readyToAttack) {
                            boolean stoppedSprinting = false;
                            if (this.stopSprinting.getValue().equalsIgnoreCase("Packet") && KillAuraModule.mc.player.isSprinting() && !KillAuraModule.mc.player.isShiftKeyDown()) {
                                mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)KillAuraModule.mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                                stoppedSprinting = true;
                            }
                            int currentSlot = KillAuraModule.mc.player.getInventory().getSelectedSlot();
                            if (this.swap.getValue().equalsIgnoreCase("Normal") && bestWeaponSlot != -1) {
                                if (this.originalSlot == -1 && currentSlot != bestWeaponSlot) {
                                    this.originalSlot = currentSlot;
                                }
                                InventoryUtils.switchSlot("Normal", bestWeaponSlot, currentSlot);
                            } else if (this.swap.getValue().equalsIgnoreCase("Silent") && bestWeaponSlot != -1 && this.silentHeldWeaponSlot != bestWeaponSlot) {
                                this.releaseSilentHold();
                                InventoryUtils.switchSlot("Silent", bestWeaponSlot, currentSlot);
                                InventoryUtils.markLongHold(bestWeaponSlot);
                                this.silentHeldWeaponSlot = bestWeaponSlot;
                                this.silentHeldReturnSlot = currentSlot;
                            }
                            mc.getConnection().send((Packet)new ServerboundAttackPacket(this.target.getId()));
                            KillAuraModule.mc.player.resetAttackStrengthTicker();
                            if (this.swing.getValue()) {
                                KillAuraModule.mc.player.swing(InteractionHand.MAIN_HAND);
                                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                            }
                            if (stoppedSprinting) {
                                mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)KillAuraModule.mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                            }
                            this.lastAttackTime = System.currentTimeMillis();
                        }
                    } else {
                        this.target = null;
                        this.releaseSilentHold();
                    }
                }
            }
        } else {
            this.releaseSilentHold();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (!(packetReceiveEvent.getPacket() instanceof ServerboundSetCarriedItemPacket)) {
            return;
        }
        if (KillAuraModule.mc.player == null || KillAuraModule.mc.level == null) {
            return;
        }
        mc.execute(() -> {
            this.lastAttackTime = System.currentTimeMillis();
        });
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (!this.render.getValue() || KillAuraModule.mc.player == null || KillAuraModule.mc.player.isDeadOrDying()) {
            return;
        }
        Entity renderTarget = this.target;
        float alphaFactor = 1.0f;
        if (renderTarget != null) {
            this.lastTarget = renderTarget;
            this.lastTargetTime = System.currentTimeMillis();
        } else if (this.lastTarget != null) {
            long elapsedMs = System.currentTimeMillis() - this.lastTargetTime;
            if (elapsedMs < 300L) {
                renderTarget = this.lastTarget;
                alphaFactor = 1.0f - (float)elapsedMs / 300.0f;
            } else {
                this.lastTarget = null;
            }
        }
        if (renderTarget == null) {
            return;
        }
        Color color = ColorUtils.getColor(this.boxColor.getColor(), (int)((float)this.boxColor.getColor().getAlpha() * alphaFactor));
        if (this.renderMode.getValue().equalsIgnoreCase("Box") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            Vec3 renderPos = EntityUtils.getRenderPos(renderTarget, renderWorldEvent.getTickDelta());
            AABB aABB = renderTarget.getBoundingBox().move(renderPos.x - renderTarget.getX(), renderPos.y - renderTarget.getY(), renderPos.z - renderTarget.getZ());
            Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, color);
            Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, color);
        }
        if (this.renderMode.getValue().equalsIgnoreCase("Circle") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderTargetCircle(renderWorldEvent.getMatrices(), renderTarget, renderWorldEvent.getTickDelta(), color);
        }
    }

    public double getTargetDistance(Vec3 eyePos, Entity entity) {
        if (KillAuraModule.mc.player == null || entity == null) {
            return Double.MAX_VALUE;
        }
        if (this.stanceAbuse.getValue()) {
            double bestDist = Double.MAX_VALUE;
            for (Vec3 pos : GrimUtils.getPossibleEyePositions((Player)KillAuraModule.mc.player)) {
                double dist = pos.distanceTo(RotationUtils.getClampClosestPoint(pos, entity.getBoundingBox()));
                if (!(dist < bestDist)) continue;
                bestDist = dist;
            }
            return bestDist;
        }
        return eyePos.distanceTo(RotationUtils.getClampClosestPoint(eyePos, entity.getBoundingBox()));
    }

    private Entity findOptimalTarget() {
        if (KillAuraModule.mc.player == null || KillAuraModule.mc.level == null) {
            return null;
        }
        ArrayList<Entity> candidates = new ArrayList<Entity>();
        Vec3 eyePos = KillAuraModule.mc.player.getEyePosition(1.0f);
        double maxRange = this.range.getValue().doubleValue();
        for (Entity entity : KillAuraModule.mc.level.entitiesForRendering()) {
            if (entity == KillAuraModule.mc.player || !entity.isAlive() || EntityUtils.isGhost(entity)) continue;
            double dist = this.getTargetDistance(eyePos, entity);
            if (dist > maxRange) continue;
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (!this.players.getValue() || !this.friends.getValue() && Night.FRIEND_MANAGER.contains(player.getName().getString()) || this.ignoreNaked.getValue() && EntityUtils.isNaked(player)) continue;
                candidates.add(entity);
                continue;
            }
            if (this.projectiles.getValue() && (entity instanceof ShulkerBullet || entity instanceof LargeFireball)) {
                candidates.add(entity);
                continue;
            }
            if (EntityUtils.isNeutral(entity)) {
                if (!this.neutrals.getValue() && !EntityUtils.isAngry(entity)) continue;
                candidates.add(entity);
                continue;
            }
            if (this.hostiles.getValue() && EntityUtils.isHostile(entity)) {
                candidates.add(entity);
                continue;
            }
            if (!this.animals.getValue() || !EntityUtils.isAnimal(entity)) continue;
            candidates.add(entity);
        }
        List<Entity> players = candidates.stream().filter(entity -> entity instanceof Player).sorted(Comparator.comparingDouble(entity -> this.getTargetDistance(eyePos, entity))).toList();
        if (!players.isEmpty()) {
            return players.get(0);
        }
        List<Entity> creepers = candidates.stream().filter(entity -> entity instanceof Creeper && this.getTargetDistance(eyePos, entity) <= 3.0).sorted(Comparator.comparingDouble(entity -> this.getTargetDistance(eyePos, entity))).toList();
        if (!creepers.isEmpty()) {
            return creepers.get(0);
        }
        List<Entity> projectiles = candidates.stream().filter(entity -> entity instanceof ShulkerBullet || entity instanceof LargeFireball).sorted(Comparator.comparingDouble(entity -> this.getTargetDistance(eyePos, entity))).toList();
        if (!projectiles.isEmpty()) {
            return projectiles.get(0);
        }
        return candidates.stream().min(Comparator.comparingDouble(entity -> this.getTargetDistance(eyePos, entity))).orElse(null);
    }

    private float getTps() {
        if (this.tpsMode.getValue().equalsIgnoreCase("None")) {
            return 20.0f;
        }
        return Night.SERVER_MANAGER.getTickRate();
    }

    private double getCooldownMs(int weaponSlot) {
        ItemAttributeModifiers itemAttributeModifiers;
        double baseAttackSpeed;
        if (KillAuraModule.mc.player == null) {
            return 500.0;
        }
        int slot = weaponSlot == -1 ? KillAuraModule.mc.player.getInventory().getSelectedSlot() : weaponSlot;
        ItemStack itemStack = KillAuraModule.mc.player.getInventory().getItem(slot);
        double attackSpeed = baseAttackSpeed = KillAuraModule.mc.player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
        if (itemStack.has(DataComponents.ATTRIBUTE_MODIFIERS) && (itemAttributeModifiers = (ItemAttributeModifiers)itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS)) != null) {
            for (ItemAttributeModifiers.Entry entry : itemAttributeModifiers.modifiers()) {
                if (!entry.attribute().is(Attributes.ATTACK_SPEED)) continue;
                attackSpeed += entry.modifier().amount();
            }
        }
        double cooldownTicks = 1.0 / Math.max(0.1, attackSpeed) * 20.0;
        double cooldownMs = cooldownTicks * 50.0 * this.delay.getValue().doubleValue();
        if (this.tpsMode.getValue().equalsIgnoreCase("Average")) {
            float tickRate = Math.max(1.0f, Night.SERVER_MANAGER.getTickRate());
            cooldownMs *= (double)(20.0f / tickRate);
        }
        return cooldownMs;
    }

    public int getBestWeaponSlot() {
        if (KillAuraModule.mc.player == null) {
            return -1;
        }
        int bestSlot = -1;
        float bestScore = -1.0f;
        boolean isFalling = !KillAuraModule.mc.player.getAbilities().flying && !KillAuraModule.mc.player.onGround();
        for (int i = 0; i < 9; ++i) {
            int baneLevel;
            int smiteLevel;
            int sharpnessLevel;
            ItemAttributeModifiers itemAttributeModifiers;
            ItemStack itemStack = KillAuraModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty()) continue;
            boolean isSword = itemStack.is(ItemTags.SWORDS);
            boolean isAxe = itemStack.is(ItemTags.AXES);
            boolean isTrident = itemStack.getItem() instanceof TridentItem;
            boolean isMace = itemStack.getItem() instanceof MaceItem;
            if (!isSword && !isAxe && !isTrident && !isMace) continue;
            if (isMace && isFalling) {
                return i;
            }
            float score = 0.0f;
            if (itemStack.has(DataComponents.ATTRIBUTE_MODIFIERS) && (itemAttributeModifiers = (ItemAttributeModifiers)itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS)) != null) {
                for (ItemAttributeModifiers.Entry entry : itemAttributeModifiers.modifiers()) {
                    if (!entry.attribute().is(Attributes.ATTACK_DAMAGE)) continue;
                    score += (float)entry.modifier().amount();
                }
            }
            if (isSword) {
                score += 5.0f;
            }
            if (!((score += (float)(sharpnessLevel = EnchantmentUtils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.SHARPNESS)) * 1.25f + (float)(smiteLevel = EnchantmentUtils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.SMITE)) * 2.5f + (float)(baneLevel = EnchantmentUtils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS)) * 2.5f) > bestScore)) continue;
            bestScore = score;
            bestSlot = i;
        }
        return bestSlot;
    }

    private float calculatePlayerAttackDamage() {
        MobEffectInstance weakness;
        MobEffectInstance strength;
        if (KillAuraModule.mc.player == null) {
            return 1.0f;
        }
        float damage = 1.0f;
        if (KillAuraModule.mc.player.hasEffect(MobEffects.STRENGTH) && (strength = KillAuraModule.mc.player.getEffect(MobEffects.STRENGTH)) != null) {
            damage += 3.0f * (float)(strength.getAmplifier() + 1);
        }
        if (KillAuraModule.mc.player.hasEffect(MobEffects.WEAKNESS) && (weakness = KillAuraModule.mc.player.getEffect(MobEffects.WEAKNESS)) != null) {
            damage -= 4.0f * (float)(weakness.getAmplifier() + 1);
        }
        return Math.max(damage, 0.0f);
    }

    public static boolean isItemAWeapon(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return itemStack.is(ItemTags.SWORDS) || itemStack.is(ItemTags.AXES) || itemStack.getItem() instanceof TridentItem || itemStack.getItem() instanceof MaceItem;
    }

    @Override
    public String getMetaData() {
        return this.target == null ? "None" : this.target.getName().getString();
    }
}

