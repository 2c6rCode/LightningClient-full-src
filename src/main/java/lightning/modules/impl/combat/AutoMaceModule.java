/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="AutoMace", description="Elytra-boosts above the target, free-falls in, and mace-smashes on repeat.", category=Module.Category.COMBAT)
public class AutoMaceModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "Search radius to find a target.", 20.0, 5.0, 50.0);
    public NumberSetting minHeight = new NumberSetting("MinHeight", "Vertical height above the target to reach before diving.", 15.0, 3.0, 30.0);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Distance to the target that triggers the mace attack.", 4.0, 1.0, 6.0);
    public NumberSetting angle = new NumberSetting("Angle", "Angle", "Flight angle relative to the target: 0 = horizontal, 90 = straight vertical.", new Setting.Visibility(), 60.0, 0.0, 90.0, 1);
    public BooleanSetting smartTerrain = new BooleanSetting("SmartTerrain", "Predicts terrain ahead during ascend/dive and steepens the boost to fly over/around it instead of Angle's fixed value.", true);
    public NumberSetting terrainLookAhead = new NumberSetting("TerrainLookAhead", "SmartTerrain: how many steps ahead along the boost line are checked for obstacles.", 6.0, 2.0, 15.0);
    public NumberSetting terrainClimbRate = new NumberSetting("TerrainClimbRate", "SmartTerrain: pitch (degrees) added per tick while the line ahead is blocked.", 4.0, 1.0, 15.0);
    public NumberSetting terrainMaxClimb = new NumberSetting("TerrainMaxClimb", "SmartTerrain: max pitch lift before falling back to the escape sweep.", 40.0, 10.0, 85.0);
    public BooleanSetting fakeFly = new BooleanSetting("FakeFly", "Keeps real armor on and cycles the elytra in/out (GrimV3 trick) instead of wearing it the whole combo. Opt-in, reliability unverified.", false);
    public BooleanSetting swing = new BooleanSetting("Swing", "Whether to swing your hand when attacking.", true);
    public ModeSetting swap = new ModeSetting("Swap", "Weapon switch mode.", "Silent", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting render = new BooleanSetting("Render", "Renders an indicator around the current target.", true);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "Target render mode.", new BooleanSetting.Visibility(this.render, true), "Circle", new String[]{"Box", "Circle", "Both"});
    public CategorySetting entitiesCategory = new CategorySetting("Entities", "Target entity filter settings.");
    public BooleanSetting players = new BooleanSetting("Players", "Target player entities.", new CategorySetting.Visibility(this.entitiesCategory), true);
    public BooleanSetting friends = new BooleanSetting("Friends", "Target friends.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting hostiles = new BooleanSetting("Hostiles", "Target hostile mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting neutrals = new BooleanSetting("Neutrals", "Target neutral mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public BooleanSetting animals = new BooleanSetting("Animals", "Target passive animal mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
    public ColorSetting boxColor = new ColorSetting("TargetColor", "Color of the target render box.", new BooleanSetting.Visibility(this.render, true), ColorUtils.getDefaultFillColor());
    private static final long FADE_TIME_MS = 300L;
    private static final float SMASH_FALL_DISTANCE = 1.5f;
    private static final int MIN_FREEFALL_TICKS = 3;
    private static final float STEEP_PITCH = 85.0f;
    private static final double LEAD_TICKS = 4.0;
    private static final int STUCK_SWEEP_TICKS = 20;
    private static final double SCAN_LOOKAHEAD = 8.0;
    private static final double SCAN_CLEAR_DIST = 6.0;
    private static final double SCAN_MIN_DIST = 2.5;
    private static final float[] SCAN_YAW_OFFSETS = new float[]{0.0f, -25.0f, 25.0f, -50.0f, 50.0f, -75.0f, 75.0f};
    public Entity target = null;
    private Entity lastTarget = null;
    private long lastTargetTime = 0L;
    private int originalSlot = -1;
    private boolean ascending = true;
    private boolean elytraUnequipped = false;
    private int elytraParkedSlot = -1;
    private int freefallTicks = 0;
    private double freefallLastDistance = 0.0;
    private double freefallLastTargetDist = -1.0;
    private boolean launchKicked = false;
    private int armorParkedSlot = -1;
    private int escapeStuckTicks = 0;
    private double terrainLift = 0.0;
    private int escapeSteepTicks = 0;
    private int fakeFlyTicks = 0;
    private static final int ESCAPE_STEEP_TICKS = 10;
    private static final int FAKE_FLY_DELAY_TICKS = 2;
    private boolean freeLookInitialized = false;
    private float freeYaw;
    private float freePitch;

    public Entity getTarget() {
        return this.target;
    }

    public boolean isFreeLookActive() {
        return this.isToggled() && this.target != null;
    }

    public float getFreeYaw() {
        return this.freeYaw;
    }

    public float getFreePitch() {
        return this.freePitch;
    }

    public void onMouseTurn(double d, double d2) {
        if (!this.freeLookInitialized && AutoMaceModule.mc.player != null) {
            this.freeYaw = AutoMaceModule.mc.player.getYRot();
            this.freePitch = AutoMaceModule.mc.player.getXRot();
            this.freeLookInitialized = true;
        }
        this.freeYaw += (float)(d * 0.15);
        this.freePitch = Mth.clamp((float)(this.freePitch + (float)(d2 * 0.15)), (float)-90.0f, (float)90.0f);
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.lastTarget = null;
        this.ascending = true;
        this.elytraUnequipped = false;
        this.elytraParkedSlot = -1;
        this.freefallTicks = 0;
        this.freefallLastDistance = 0.0;
        this.freefallLastTargetDist = -1.0;
        this.originalSlot = -1;
        this.freeLookInitialized = false;
        this.launchKicked = false;
        this.armorParkedSlot = -1;
        this.escapeStuckTicks = 0;
        this.escapeSteepTicks = 0;
        this.terrainLift = 0.0;
        this.fakeFlyTicks = 0;
        if (AutoMaceModule.mc.player != null && InventoryUtils.find(Items.MACE) == -1) {
            this.setToggled(false);
        }
    }

    @Override
    public void onDisable() {
        if (AutoMaceModule.mc.player == null) {
            return;
        }
        this.reequipElytra();
        this.restoreArmorIfParked();
        if (this.originalSlot != -1) {
            InventoryUtils.switchSlot("Normal", this.originalSlot, AutoMaceModule.mc.player.getInventory().getSelectedSlot());
            this.originalSlot = -1;
        }
        this.target = null;
        this.lastTarget = null;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        ElytraFlyModule elytraFlyModule;
        if (AutoMaceModule.mc.player == null || AutoMaceModule.mc.level == null || AutoMaceModule.mc.player.isDeadOrDying()) {
            return;
        }
        if (this.target == null || !this.target.isAlive()) {
            this.target = this.findTarget();
        }
        if (this.target == null) {
            this.reequipElytra();
            this.restoreArmorIfParked();
            this.ascending = true;
            this.freeLookInitialized = false;
            return;
        }
        if (!this.freeLookInitialized) {
            this.freeYaw = AutoMaceModule.mc.player.getYRot();
            this.freePitch = AutoMaceModule.mc.player.getXRot();
            this.freeLookInitialized = true;
        }
        if ((elytraFlyModule = Night.MODULE_MANAGER.getModule(ElytraFlyModule.class)) == null) {
            return;
        }
        if (this.ascending) {
            this.tickAscend(elytraFlyModule);
        } else {
            this.tickDive(elytraFlyModule);
        }
    }

    private void tickAscend(ElytraFlyModule elytraFlyModule) {
        this.reequipElytra();
        double d = AutoMaceModule.mc.player.getY() - this.target.getY();
        if (d >= this.minHeight.getValue().doubleValue()) {
            double d2 = Math.hypot(AutoMaceModule.mc.player.getX() - this.target.getX(), AutoMaceModule.mc.player.getZ() - this.target.getZ());
            Vec3 vec3 = AutoMaceModule.mc.player.getDeltaMovement();
            Night.LOGGER.info("[MaceDbg] ASCEND->DIVE height={} horizDist={} vel=({},{},{})", new Object[]{d, d2, vec3.x, vec3.y, vec3.z});
            this.ascending = false;
            this.freefallTicks = 0;
            this.escapeStuckTicks = 0;
            return;
        }
        if (!this.ensureFlying(elytraFlyModule)) {
            return;
        }
        Vec3 vec3 = this.target.getBoundingBox().getCenter();
        float f = (float)RotationUtils.getYRotToVec((Entity)AutoMaceModule.mc.player, vec3);
        float f2 = f + 180.0f;
        float f3 = -this.angle.getValue().floatValue();
        if (this.escapeSteepTicks > 0) {
            --this.escapeSteepTicks;
            f3 = -85.0f;
        }
        boolean bl = this.smartTerrain.getValue();
        boolean bl2 = AutoMaceModule.mc.player.getDeltaMovement().horizontalDistanceSqr() < 0.0025;
        float[] fArray = bl && !bl2 ? this.steerClear(f2, f3) : null;
        float[] fArray2 = fArray;
        if (fArray != null) {
            this.escapeStuckTicks = 0;
            this.snapRotation(fArray[0], fArray[1]);
            elytraFlyModule.tryUseGrimFirework(fArray[0], fArray[1]);
            return;
        }
        boolean bl3 = bl && this.ceilingBlocked();
        boolean bl4 = bl && (this.predictsCollision() || bl2);
        boolean bl5 = bl4;
        int n = this.escapeStuckTicks = bl3 || bl4 ? (this.escapeStuckTicks = this.escapeStuckTicks + 1) : 0;
        if (bl3 || this.escapeStuckTicks > 20) {
            f3 = -15.0f;
            f2 += 90.0f * (float)(this.escapeStuckTicks / 20 % 4);
        } else if (bl4) {
            f3 = -85.0f;
        }
        this.snapRotation(f2, f3);
        elytraFlyModule.tryUseGrimFirework(f2, f3);
    }

    private void tickDive(ElytraFlyModule elytraFlyModule) {
        Vec3 vec3 = this.target.getBoundingBox().getCenter();
        Vec3 vec32 = vec3.add(this.target.getDeltaMovement().scale(4.0));
        float f = (float)RotationUtils.getYRotToVec((Entity)AutoMaceModule.mc.player, vec32);
        float f2 = (float)RotationUtils.getXRotToVec((Entity)AutoMaceModule.mc.player, vec32);
        if (!this.elytraUnequipped) {
            double d = AutoMaceModule.mc.player.getY() - this.target.getY();
            double d2 = this.attackRange.getValue().doubleValue() + 3.0;
            double d3 = Math.sqrt(Math.pow(AutoMaceModule.mc.player.getX() - vec3.x, 2.0) + Math.pow(AutoMaceModule.mc.player.getZ() - vec3.z, 2.0));
            double d4 = this.attackRange.getValue().doubleValue() * 1.5;
            Vec3 vec33 = AutoMaceModule.mc.player.getDeltaMovement();
            Night.LOGGER.info("[MaceDbg] DIVE-HOMING height={} horizDist={} cutHeight={} cutHorizDist={} vel=({},{},{})", new Object[]{d, d3, d2, d4, vec33.x, vec33.y, vec33.z});
            if (d < 1.0 && d3 > d4) {
                Night.LOGGER.info("[MaceDbg] DIVE ABORT-TO-ASCEND (ran out of height, still far) height={} horizDist={}", (Object)d, (Object)d3);
                this.ascending = true;
                return;
            }
            if (d > d2 || d3 > d4) {
                if (!this.ensureFlying(elytraFlyModule)) {
                    return;
                }
                float f3 = f2;
                boolean bl = AutoMaceModule.mc.player.getDeltaMovement().horizontalDistanceSqr() < 0.0025;
                float[] fArray = this.smartTerrain.getValue() && !bl ? this.steerClear(f, f2) : null;
                float[] fArray2 = fArray;
                if (fArray != null) {
                    this.escapeStuckTicks = 0;
                    this.snapRotation(fArray[0], fArray[1]);
                    elytraFlyModule.tryUseGrimFirework(fArray[0], fArray[1]);
                    return;
                }
                boolean bl2 = this.smartTerrain.getValue() && (this.predictsCollision() || bl);
                boolean bl3 = bl2;
                if (bl2) {
                    ++this.escapeStuckTicks;
                    if (this.escapeStuckTicks > 20) {
                        this.ascending = true;
                        return;
                    }
                    f3 = f2 < 0.0f ? -85.0f : 85.0f;
                } else {
                    this.escapeStuckTicks = 0;
                }
                this.snapRotation(f, f3);
                elytraFlyModule.tryUseGrimFirework(f, f3);
                return;
            }
            Vec3 vec34 = AutoMaceModule.mc.player.getDeltaMovement();
            Night.LOGGER.info("[MaceDbg] CUT-ELYTRA (commit to freefall) height={} horizDist={} vel=({},{},{})", new Object[]{d, d3, vec34.x, vec34.y, vec34.z});
            this.unequipElytra();
            this.freefallTicks = 0;
            this.freefallLastDistance = AutoMaceModule.mc.player.fallDistance;
            this.freefallLastTargetDist = -1.0;
            return;
        }
        Vec3 vec35 = AutoMaceModule.mc.player.getEyePosition(1.0f);
        Vec3 vec36 = RotationUtils.getClampClosestPoint(vec35, this.target.getBoundingBox());
        Night.ROTATION_MANAGER.silentRotate(RotationUtils.getYRotToVec(vec35, vec36), RotationUtils.getXRotToVec(vec35, vec36));
        ++this.freefallTicks;
        double d = vec35.distanceTo(vec36);
        Night.LOGGER.info("[MaceDbg] FREEFALL tick={} dist={} fallDistance={} y={} targetY={} onGround={}", new Object[]{this.freefallTicks, d, AutoMaceModule.mc.player.fallDistance, AutoMaceModule.mc.player.getY(), this.target.getY(), AutoMaceModule.mc.player.onGround()});
        if (this.freefallTicks >= 3 && d <= this.attackRange.getValue().doubleValue() && AutoMaceModule.mc.player.fallDistance >= 1.5) {
            Night.LOGGER.info("[MaceDbg] ATTACK dist={} attackRange={} fallDistance={} onGround={}", new Object[]{d, this.attackRange.getValue().doubleValue(), AutoMaceModule.mc.player.fallDistance, AutoMaceModule.mc.player.onGround()});
            this.attack(this.target);
            elytraFlyModule.resetFireworkCooldown();
            this.escapeSteepTicks = 10;
            this.reequipElytra();
            elytraFlyModule.sendGlideStart();
            this.ascending = true;
            this.launchKicked = false;
            return;
        }
        if (AutoMaceModule.mc.player.onGround()) {
            Night.LOGGER.info("[MaceDbg] MISS-ONGROUND dist={} attackRange={} freefallTicks={}", new Object[]{d, this.attackRange.getValue().doubleValue(), this.freefallTicks});
            elytraFlyModule.resetFireworkCooldown();
            this.reequipElytra();
            elytraFlyModule.sendGlideStart();
            this.ascending = true;
            return;
        }
        if (AutoMaceModule.mc.player.fallDistance + 0.01 < this.freefallLastDistance) {
            this.reequipElytra();
            this.ascending = true;
            return;
        }
        this.freefallLastDistance = AutoMaceModule.mc.player.fallDistance;
        if (this.freefallLastTargetDist >= 0.0 && d > this.freefallLastTargetDist + 0.05) {
            Night.LOGGER.info("[MaceDbg] MISS-OVERSHOOT-DETECTOR dist={} prevDist={}", (Object)d, (Object)this.freefallLastTargetDist);
            this.reequipElytra();
            this.freefallLastTargetDist = -1.0;
            return;
        }
        this.freefallLastTargetDist = d;
    }

    private void snapRotation(float f, float f2) {
        AutoMaceModule.mc.player.setYRot(f);
        AutoMaceModule.mc.player.setXRot(f2);
        AutoMaceModule.mc.player.setYBodyRot(f);
        AutoMaceModule.mc.player.setYHeadRot(f);
    }

    private boolean predictsCollision() {
        if (AutoMaceModule.mc.level == null) {
            return false;
        }
        Vec3 vec3 = AutoMaceModule.mc.player.getDeltaMovement();
        if (vec3.lengthSqr() < 1.0E-4) {
            return false;
        }
        Vec3 vec32 = AutoMaceModule.mc.player.position().add(0.0, (double)AutoMaceModule.mc.player.getBbHeight() * 0.5, 0.0);
        double d = Math.max(3.0, vec3.length() * 6.0);
        Vec3 vec33 = vec32.add(vec3.normalize().scale(d));
        BlockHitResult blockHitResult = AutoMaceModule.mc.level.clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)AutoMaceModule.mc.player));
        return blockHitResult.getType() == HitResult.Type.BLOCK;
    }

    private boolean ceilingBlocked() {
        Vec3 vec3;
        if (AutoMaceModule.mc.level == null) {
            return false;
        }
        Vec3 vec32 = AutoMaceModule.mc.player.position().add(0.0, (double)AutoMaceModule.mc.player.getBbHeight(), 0.0);
        BlockHitResult blockHitResult = AutoMaceModule.mc.level.clip(new ClipContext(vec32, vec3 = vec32.add(0.0, 5.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)AutoMaceModule.mc.player));
        return blockHitResult.getType() == HitResult.Type.BLOCK;
    }

    private double rayClearDistance(float f, float f2) {
        return this.rayClearDistance(f, f2, 8.0);
    }

    private double rayClearDistance(float f, float f2, double d) {
        if (AutoMaceModule.mc.level == null) {
            return 0.0;
        }
        double d2 = Math.toRadians(f);
        double d3 = Math.toRadians(f2);
        double d4 = Math.cos(d3);
        Vec3 vec3 = new Vec3(-Math.sin(d2) * d4, -Math.sin(d3), Math.cos(d2) * d4);
        Vec3 vec32 = AutoMaceModule.mc.player.position().add(0.0, (double)AutoMaceModule.mc.player.getBbHeight() * 0.5, 0.0);
        BlockHitResult blockHitResult = AutoMaceModule.mc.level.clip(new ClipContext(vec32, vec32.add(vec3.scale(d)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)AutoMaceModule.mc.player));
        return blockHitResult.getType() == HitResult.Type.BLOCK ? blockHitResult.getLocation().distanceTo(vec32) : d;
    }

    private float[] steerClear(float f, float f2) {
        double d;
        if (AutoMaceModule.mc.level == null) {
            return null;
        }
        int n = this.terrainLookAhead.getValue().intValue();
        double d2 = 8.0 / (double)n;
        boolean bl = false;
        for (int i = 1; i <= n; ++i) {
            float f3 = f2 - (float)(this.terrainLift * (double)i / (double)n);
            d = d2 * (double)i;
            if (!(this.rayClearDistance(f, f3, d) < d - 0.001)) continue;
            bl = true;
            break;
        }
        double d3 = this.terrainClimbRate.getValue().doubleValue();
        d = this.terrainMaxClimb.getValue().doubleValue();
        double d4 = this.terrainLift = bl ? Math.min(this.terrainLift + d3, d) : Math.max(0.0, this.terrainLift - d3 * 0.5);
        if (!bl || this.terrainLift < d) {
            return new float[]{f, f2 - (float)this.terrainLift};
        }
        return null;
    }

    private boolean ensureFlying(ElytraFlyModule elytraFlyModule) {
        if (AutoMaceModule.mc.player.onGround()) {
            Vec3 vec3 = AutoMaceModule.mc.player.getDeltaMovement();
            if (vec3.y < 0.42) {
                AutoMaceModule.mc.player.setDeltaMovement(vec3.x, 0.45, vec3.z);
            }
            this.launchKicked = true;
            return false;
        }
        if (this.launchKicked) {
            this.launchKicked = false;
            this.ensureElytraWorn();
            this.fakeFlyTicks = 0;
            elytraFlyModule.sendGlideStart();
            return false;
        }
        this.fakeFlyCycle(elytraFlyModule);
        if (AutoMaceModule.mc.player.isFallFlying()) {
            return true;
        }
        this.ensureElytraWorn();
        elytraFlyModule.sendGlideStart();
        return false;
    }

    private void fakeFlyCycle(ElytraFlyModule elytraFlyModule) {
        if (!this.fakeFly.getValue() || this.elytraUnequipped || AutoMaceModule.mc.player.onGround()) {
            return;
        }
        if (this.fakeFlyTicks++ < 2) {
            return;
        }
        this.fakeFlyTicks = 0;
        elytraFlyModule.sendGlideStart();
    }

    public boolean shouldPinFallFlying() {
        return this.isToggled() && this.fakeFly.getValue() && this.target != null && !this.elytraUnequipped && AutoMaceModule.mc.player != null && !AutoMaceModule.mc.player.onGround();
    }

    private void ensureElytraWorn() {
        int n;
        if (this.fakeFly.getValue()) {
            return;
        }
        if (AutoMaceModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            return;
        }
        int n2 = InventoryUtils.find(Items.ELYTRA);
        if (n2 == -1) {
            return;
        }
        if (this.armorParkedSlot == -1 && !AutoMaceModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && (n = this.findEmptySlot()) != -1) {
            InventoryUtils.swapEquipment(n, 6);
            this.armorParkedSlot = n;
        }
        InventoryUtils.swapEquipment(n2, 6);
    }

    private void restoreArmorIfParked() {
        if (this.armorParkedSlot == -1) {
            return;
        }
        InventoryUtils.swapEquipment(this.armorParkedSlot, 6);
        this.armorParkedSlot = -1;
    }

    private void unequipElytra() {
        if (this.elytraUnequipped) {
            return;
        }
        if (this.fakeFly.getValue()) {
            this.elytraUnequipped = true;
            return;
        }
        if (AutoMaceModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            return;
        }
        int n = this.findEmptySlot();
        if (n == -1) {
            return;
        }
        InventoryUtils.swapEquipment(n, 6);
        this.elytraParkedSlot = n;
        this.elytraUnequipped = true;
    }

    private void reequipElytra() {
        if (!this.elytraUnequipped) {
            return;
        }
        if (this.elytraParkedSlot != -1) {
            InventoryUtils.swapEquipment(this.elytraParkedSlot, 6);
        }
        this.elytraUnequipped = false;
        this.elytraParkedSlot = -1;
    }

    private int findEmptySlot() {
        for (int i = 0; i <= 35; ++i) {
            if (!AutoMaceModule.mc.player.getInventory().getItem(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private void attack(Entity entity) {
        int n = AutoMaceModule.mc.player.getInventory().getSelectedSlot();
        int n2 = InventoryUtils.find(Items.MACE);
        boolean bl = false;
        if (n2 != -1 && n2 != n) {
            if (this.swap.getValue().equalsIgnoreCase("Normal")) {
                if (this.originalSlot == -1) {
                    this.originalSlot = n;
                }
                InventoryUtils.switchSlot("Normal", n2, n);
            } else if (this.swap.getValue().equalsIgnoreCase("Silent")) {
                InventoryUtils.switchSlot("Silent", n2, n);
                bl = true;
            }
        }
        mc.getConnection().send((Packet)new ServerboundAttackPacket(entity.getId()));
        AutoMaceModule.mc.player.resetAttackStrengthTicker();
        if (this.swing.getValue()) {
            AutoMaceModule.mc.player.swing(InteractionHand.MAIN_HAND);
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        if (bl) {
            InventoryUtils.switchBack("Silent", n2, n);
        }
    }

    private Entity findTarget() {
        if (AutoMaceModule.mc.player == null || AutoMaceModule.mc.level == null) {
            return null;
        }
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        Vec3 vec3 = AutoMaceModule.mc.player.getEyePosition(1.0f);
        double d = this.range.getValue().doubleValue();
        for (Entity entity2 : AutoMaceModule.mc.level.entitiesForRendering()) {
            if (entity2 == AutoMaceModule.mc.player || !entity2.isAlive() || EntityUtils.isGhost(entity2)) continue;
            double d3 = vec3.distanceTo(RotationUtils.getClampClosestPoint(vec3, entity2.getBoundingBox()));
            if (d3 > d) continue;
            if (entity2 instanceof Player) {
                Player player = (Player)entity2;
                if (!this.players.getValue() || !this.friends.getValue() && Night.FRIEND_MANAGER.contains(player.getName().getString())) continue;
                arrayList.add(entity2);
                continue;
            }
            if (EntityUtils.isNeutral(entity2)) {
                if (!this.neutrals.getValue() && !EntityUtils.isAngry(entity2)) continue;
                arrayList.add(entity2);
                continue;
            }
            if (this.hostiles.getValue() && EntityUtils.isHostile(entity2)) {
                arrayList.add(entity2);
                continue;
            }
            if (!this.animals.getValue() || !EntityUtils.isAnimal(entity2)) continue;
            arrayList.add(entity2);
        }
        return arrayList.stream().min(Comparator.comparingDouble(entity -> vec3.distanceTo(RotationUtils.getClampClosestPoint(vec3, entity.getBoundingBox())))).orElse(null);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (!this.render.getValue() || AutoMaceModule.mc.player == null || AutoMaceModule.mc.player.isDeadOrDying()) {
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
}

