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
                boolean bl;
                boolean bl2 = bl = KillAuraModule.mc.options.keyUse.isDown() || KillAuraModule.mc.player.isUsingItem() || KillAuraModule.mc.player.getUseItemRemainingTicks() > 0;
                if (this.pauseOnEat.getValue() && bl) {
                    this.target = null;
                    this.releaseSilentHold();
                } else if (!this.multiTask.getValue() && bl) {
                    this.target = null;
                    this.releaseSilentHold();
                } else {
                    Entity entity;
                    boolean bl3;
                    ItemStack itemStack = KillAuraModule.mc.player.getMainHandItem();
                    if (this.swap.getValue().equalsIgnoreCase("Silent")) {
                        boolean bl4 = this.onlyGapHold.getValue();
                        bl3 = this.onlyTotemHold.getValue();
                        if (bl4 || bl3) {
                            boolean bl5;
                            boolean bl6 = itemStack.is(Items.GOLDEN_APPLE) || itemStack.is(Items.ENCHANTED_GOLDEN_APPLE);
                            boolean bl7 = itemStack.is(Items.TOTEM_OF_UNDYING);
                            boolean bl8 = bl5 = bl4 && bl6 || bl3 && bl7;
                            if (!bl5) {
                                this.target = null;
                                this.releaseSilentHold();
                                return;
                            }
                        }
                    }
                    if ((entity = this.findOptimalTarget()) != null && (!this.swap.getValue().equalsIgnoreCase("Require") || KillAuraModule.isItemAWeapon(itemStack))) {
                        double d;
                        this.target = entity;
                        bl3 = false;
                        if (!(this.target instanceof ShulkerBullet) && !(this.target instanceof LargeFireball)) {
                            LivingEntity livingEntity;
                            float f = this.calculatePlayerAttackDamage();
                            Entity entity2 = this.target;
                            if (entity2 instanceof LivingEntity && (livingEntity = (LivingEntity)entity2).getMaxHealth() <= f) {
                                bl3 = true;
                            }
                        } else {
                            bl3 = true;
                        }
                        int n = this.getBestWeaponSlot();
                        double d2 = this.getCooldownMs(n);
                        long l = System.currentTimeMillis();
                        boolean bl9 = bl3 || (double)(l - this.lastAttackTime) >= d2;
                        switch (this.rotate.getValue()) {
                            case "Hold": {
                                d = 1000.0;
                                break;
                            }
                            case "Normal": {
                                d = 100.0;
                                break;
                            }
                            default: {
                                d = 0.0;
                                break;
                            }
                        }
                        if (bl3 || bl9 || d2 - (double)(l - this.lastAttackTime) <= d) {
                            int n2;
                            Vec3 eyePos = KillAuraModule.mc.player.getEyePosition(1.0f);
                            if (this.stanceAbuse.getValue()) {
                                double d4 = Double.MAX_VALUE;
                                for (Vec3 pos : GrimUtils.getPossibleEyePositions((Player)KillAuraModule.mc.player)) {
                                    Vec3 closest = RotationUtils.getClampClosestPoint(pos, this.target.getBoundingBox());
                                    double d5 = pos.distanceTo(closest);
                                    if (!(d5 < d4)) continue;
                                    d4 = d5;
                                    eyePos = pos;
                                }
                            }
                            Vec3 vec3 = this.target.getBoundingBox().getCenter();
                            Entity entity4 = this.target;
                            if (entity4 instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity)entity4;
                                if (this.target.getBoundingBox().getYsize() >= 0.8) {
                                    vec3 = new Vec3(this.target.getX(), this.target.getY() + (double)livingEntity.getEyeHeight() * 0.75, this.target.getZ());
                                }
                            }
                            float f = RotationUtils.getYRotToVec(eyePos, vec3);
                            float f2 = RotationUtils.getXRotToVec(eyePos, vec3);
                            int n3 = this.target.getBoundingBox().contains(eyePos) ? 1 : 0;
                            if (this.getTargetDistance(eyePos, this.target) > this.range.getValue().doubleValue()) {
                                this.target = null;
                                this.releaseSilentHold();
                                return;
                            }
                            int n4 = n2 = this.rotate.getValue().equalsIgnoreCase("None") || n3 != 0 ? 1 : 0;
                            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                                if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                    Night.ROTATION_MANAGER.silentRotate(f, f2);
                                    n2 = 1;
                                } else {
                                    Night.ROTATION_MANAGER.legacyRotate(f, f2, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                    boolean raycastHit = RotationUtils.raycastTarget(eyePos, this.target, this.range.getValue().doubleValue(), Night.ROTATION_MANAGER.getServerYaw(), Night.ROTATION_MANAGER.getServerPitch()) != null;
                                    int n5 = n2 = raycastHit || n3 != 0 ? 1 : 0;
                                }
                            }
                            if (n2 == 0) {
                                return;
                            }
                        }
                        if (bl9) {
                            boolean bl10 = false;
                            if (this.stopSprinting.getValue().equalsIgnoreCase("Packet") && KillAuraModule.mc.player.isSprinting() && !KillAuraModule.mc.player.isShiftKeyDown()) {
                                mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)KillAuraModule.mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                                bl10 = true;
                            }
                            int n6 = KillAuraModule.mc.player.getInventory().getSelectedSlot();
                            if (this.swap.getValue().equalsIgnoreCase("Normal") && n != -1) {
                                if (this.originalSlot == -1 && n6 != n) {
                                    this.originalSlot = n6;
                                }
                                InventoryUtils.switchSlot("Normal", n, n6);
                            } else if (this.swap.getValue().equalsIgnoreCase("Silent") && n != -1 && this.silentHeldWeaponSlot != n) {
                                this.releaseSilentHold();
                                InventoryUtils.switchSlot("Silent", n, n6);
                                InventoryUtils.markLongHold(n);
                                this.silentHeldWeaponSlot = n;
                                this.silentHeldReturnSlot = n6;
                            }
                            mc.getConnection().send((Packet)new ServerboundAttackPacket(this.target.getId()));
                            KillAuraModule.mc.player.resetAttackStrengthTicker();
                            if (this.swing.getValue()) {
                                KillAuraModule.mc.player.swing(InteractionHand.MAIN_HAND);
                                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                            }
                            if (bl10) {
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
        Entity entity = this.target;
        float f = 1.0f;
        if (entity != null) {
            this.lastTarget = entity;
            this.lastTargetTime = System.currentTimeMillis();
        } else if (this.lastTarget != null) {
            long l = System.currentTimeMillis() - this.lastTargetTime;
            if (l < 300L) {
                entity = this.lastTarget;
                f = 1.0f - (float)l / 300.0f;
            } else {
                this.lastTarget = null;
            }
        }
        if (entity == null) {
            return;
        }
        Color color = ColorUtils.getColor(this.boxColor.getColor(), (int)((float)this.boxColor.getColor().getAlpha() * f));
        if (this.renderMode.getValue().equalsIgnoreCase("Box") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            Vec3 vec3 = EntityUtils.getRenderPos(entity, renderWorldEvent.getTickDelta());
            AABB aABB = entity.getBoundingBox().move(vec3.x - entity.getX(), vec3.y - entity.getY(), vec3.z - entity.getZ());
            Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, color);
            Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, color);
        }
        if (this.renderMode.getValue().equalsIgnoreCase("Circle") || this.renderMode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderTargetCircle(renderWorldEvent.getMatrices(), entity, renderWorldEvent.getTickDelta(), color);
        }
    }

    public double getTargetDistance(Vec3 vec3, Entity entity) {
        if (KillAuraModule.mc.player == null || entity == null) {
            return Double.MAX_VALUE;
        }
        if (this.stanceAbuse.getValue()) {
            double d = Double.MAX_VALUE;
            for (Vec3 vec32 : GrimUtils.getPossibleEyePositions((Player)KillAuraModule.mc.player)) {
                double d2 = vec32.distanceTo(RotationUtils.getClampClosestPoint(vec32, entity.getBoundingBox()));
                if (!(d2 < d)) continue;
                d = d2;
            }
            return d;
        }
        return vec3.distanceTo(RotationUtils.getClampClosestPoint(vec3, entity.getBoundingBox()));
    }

    private Entity findOptimalTarget() {
        if (KillAuraModule.mc.player == null || KillAuraModule.mc.level == null) {
            return null;
        }
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        Vec3 vec3 = KillAuraModule.mc.player.getEyePosition(1.0f);
        double d = this.range.getValue().doubleValue();
        for (Entity entity : KillAuraModule.mc.level.entitiesForRendering()) {
            if (entity == KillAuraModule.mc.player || !entity.isAlive() || EntityUtils.isGhost(entity)) continue;
            double dist = this.getTargetDistance(vec3, entity);
            if (dist > d) continue;
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (!this.players.getValue() || !this.friends.getValue() && Night.FRIEND_MANAGER.contains(player.getName().getString()) || this.ignoreNaked.getValue() && EntityUtils.isNaked(player)) continue;
                arrayList.add(entity);
                continue;
            }
            if (this.projectiles.getValue() && (entity instanceof ShulkerBullet || entity instanceof LargeFireball)) {
                arrayList.add(entity);
                continue;
            }
            if (EntityUtils.isNeutral(entity)) {
                if (!this.neutrals.getValue() && !EntityUtils.isAngry(entity)) continue;
                arrayList.add(entity);
                continue;
            }
            if (this.hostiles.getValue() && EntityUtils.isHostile(entity)) {
                arrayList.add(entity);
                continue;
            }
            if (!this.animals.getValue() || !EntityUtils.isAnimal(entity)) continue;
            arrayList.add(entity);
        }
        List<Entity> list = arrayList.stream().filter(entity -> entity instanceof Player).sorted(Comparator.comparingDouble(entity -> this.getTargetDistance(vec3, entity))).toList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        List<Entity> creepers = arrayList.stream().filter(entity -> entity instanceof Creeper && this.getTargetDistance(vec3, entity) <= 3.0).sorted(Comparator.comparingDouble(entity -> this.getTargetDistance(vec3, entity))).toList();
        if (!creepers.isEmpty()) {
            return creepers.get(0);
        }
        List<Entity> list2 = arrayList.stream().filter(entity -> entity instanceof ShulkerBullet || entity instanceof LargeFireball).sorted(Comparator.comparingDouble(entity -> this.getTargetDistance(vec3, entity))).toList();
        if (!list2.isEmpty()) {
            return list2.get(0);
        }
        return arrayList.stream().min(Comparator.comparingDouble(entity -> this.getTargetDistance(vec3, entity))).orElse(null);
    }

    private float getTps() {
        if (this.tpsMode.getValue().equalsIgnoreCase("None")) {
            return 20.0f;
        }
        return Night.SERVER_MANAGER.getTickRate();
    }

    private double getCooldownMs(int n) {
        ItemAttributeModifiers itemAttributeModifiers;
        double d;
        if (KillAuraModule.mc.player == null) {
            return 500.0;
        }
        int n2 = n == -1 ? KillAuraModule.mc.player.getInventory().getSelectedSlot() : n;
        ItemStack itemStack = KillAuraModule.mc.player.getInventory().getItem(n2);
        double d2 = d = KillAuraModule.mc.player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
        if (itemStack.has(DataComponents.ATTRIBUTE_MODIFIERS) && (itemAttributeModifiers = (ItemAttributeModifiers)itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS)) != null) {
            for (ItemAttributeModifiers.Entry entry : itemAttributeModifiers.modifiers()) {
                if (!entry.attribute().is(Attributes.ATTACK_SPEED)) continue;
                d2 += entry.modifier().amount();
            }
        }
        double d3 = 1.0 / Math.max(0.1, d2) * 20.0;
        double d4 = d3 * 50.0 * this.delay.getValue().doubleValue();
        if (this.tpsMode.getValue().equalsIgnoreCase("Average")) {
            float f = Math.max(1.0f, Night.SERVER_MANAGER.getTickRate());
            d4 *= (double)(20.0f / f);
        }
        return d4;
    }

    public int getBestWeaponSlot() {
        if (KillAuraModule.mc.player == null) {
            return -1;
        }
        int n = -1;
        float f = -1.0f;
        boolean bl = !KillAuraModule.mc.player.getAbilities().flying && !KillAuraModule.mc.player.onGround();
        for (int i = 0; i < 9; ++i) {
            int n2;
            int n3;
            int n4;
            ItemAttributeModifiers itemAttributeModifiers;
            ItemStack itemStack = KillAuraModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty()) continue;
            boolean bl2 = itemStack.is(ItemTags.SWORDS);
            boolean bl3 = itemStack.is(ItemTags.AXES);
            boolean bl4 = itemStack.getItem() instanceof TridentItem;
            boolean bl5 = itemStack.getItem() instanceof MaceItem;
            if (!bl2 && !bl3 && !bl4 && !bl5) continue;
            if (bl5 && bl) {
                return i;
            }
            float f2 = 0.0f;
            if (itemStack.has(DataComponents.ATTRIBUTE_MODIFIERS) && (itemAttributeModifiers = (ItemAttributeModifiers)itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS)) != null) {
                for (ItemAttributeModifiers.Entry entry : itemAttributeModifiers.modifiers()) {
                    if (!entry.attribute().is(Attributes.ATTACK_DAMAGE)) continue;
                    f2 += (float)entry.modifier().amount();
                }
            }
            if (bl2) {
                f2 += 5.0f;
            }
            if (!((f2 += (float)(n4 = EnchantmentUtils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.SHARPNESS)) * 1.25f + (float)(n3 = EnchantmentUtils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.SMITE)) * 2.5f + (float)(n2 = EnchantmentUtils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS)) * 2.5f) > f)) continue;
            f = f2;
            n = i;
        }
        return n;
    }

    private float calculatePlayerAttackDamage() {
        MobEffectInstance mobEffectInstance;
        MobEffectInstance mobEffectInstance2;
        if (KillAuraModule.mc.player == null) {
            return 1.0f;
        }
        float f = 1.0f;
        if (KillAuraModule.mc.player.hasEffect(MobEffects.STRENGTH) && (mobEffectInstance2 = KillAuraModule.mc.player.getEffect(MobEffects.STRENGTH)) != null) {
            f += 3.0f * (float)(mobEffectInstance2.getAmplifier() + 1);
        }
        if (KillAuraModule.mc.player.hasEffect(MobEffects.WEAKNESS) && (mobEffectInstance = KillAuraModule.mc.player.getEffect(MobEffects.WEAKNESS)) != null) {
            f -= 4.0f * (float)(mobEffectInstance.getAmplifier() + 1);
        }
        return Math.max(f, 0.0f);
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

