/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.system.Timer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="PearlChase", description="Automatically throws an Ender Pearl to chase enemy pearl throws with trajectory prediction and obstacle avoidance.", category=Module.Category.COMBAT)
public class PearlChaseModule
extends Module {
    public ModeSetting target = new ModeSetting("Target", "Priority used to select which enemy to chase.", "Nearest", new String[]{"Lowest", "Furthest", "Nearest"});
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore naked players, even if wearing only elytra.", false);
    public ModeSetting rotate = new ModeSetting("Rotate", "Rotation mode used before throwing the pearl.", "Silent", new String[]{"None", "Normal", "Silent"});
    public ModeSetting switchMode = new ModeSetting("Switch", "The mode that will be used for automatically switching to ender pearls.", "Silent", InventoryUtils.SWITCH_MODES);
    public NumberSetting offset = new NumberSetting("Offset", "Distance offset relative to target landing position along flight path (positive = ahead, negative = behind).", 0.0, -10.0, 10.0);
    public NumberSetting distance = new NumberSetting("Distance", "Minimum horizontal travel distance that enemy will move for pearl to be chased.", 10.0, 0.0, 50.0);
    public ModeSetting arcMode = new ModeSetting("ArcMode", "Trajectory selection mode: Smart (low if clear, high if blocked), Low (fast direct), High (maximum lob).", "Smart", new String[]{"Smart", "Low", "High"});
    public BooleanSetting avoidObstacles = new BooleanSetting("AvoidObstacles", "Calculates alternate high arcs or yaw offsets if direct path is blocked by terrain.", true);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "Maximum range to enemy player throwing pearl.", 64.0, 8.0, 128.0);
    public NumberSetting delay = new NumberSetting("Delay", "Delay in ticks before throwing the pearl.", 0, 0, 20);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Allows throwing pearl while eating.", true);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off module if no ender pearls are found in inventory.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Renders predicted landing position and pearl trajectory line.", true);
    public NumberSetting renderDuration = new NumberSetting("RenderTime", "Duration in seconds to render trajectory after throwing.", 3.0, 0.5, 10.0);
    private final Set<Integer> processedPearls = new HashSet<Integer>();
    private PendingThrow pendingThrow = null;
    private final Timer renderTimer = new Timer();
    private Vec3 renderLandingPos = null;
    private List<Vec3> renderPath = null;

    @Override
    public void onEnable() {
        this.processedPearls.clear();
        this.pendingThrow = null;
        this.renderLandingPos = null;
        this.renderPath = null;
    }

    @Override
    public void onDisable() {
        this.processedPearls.clear();
        this.pendingThrow = null;
        this.renderLandingPos = null;
        this.renderPath = null;
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent entitySpawnEvent) {
        TrajectorySolution trajectorySolution;
        double d;
        Vec3 vec3;
        Player player;
        if (PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return;
        }
        Entity entity = entitySpawnEvent.getEntity();
        if (!(entity instanceof ThrownEnderpearl)) {
            return;
        }
        ThrownEnderpearl thrownEnderpearl = (ThrownEnderpearl)entity;
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        Player player2 = null;
        Entity entity2 = thrownEnderpearl.getOwner();
        if (entity2 instanceof Player) {
            player2 = player = (Player)entity2;
        } else {
            double d2 = 16.0;
            for (Player player3 : PearlChaseModule.mc.level.players()) {
                double d3;
                if (!this.validTarget(player3) || !((d3 = player3.distanceToSqr(thrownEnderpearl.position())) < d2)) continue;
                d2 = d3;
                player2 = player3;
            }
        }
        if (player2 == null || !this.validTarget(player2)) {
            return;
        }
        player = this.getBestTarget();
        if (player != null && player2 != player) {
            return;
        }
        if (!this.processedPearls.add(thrownEnderpearl.getId())) {
            return;
        }
        if (this.processedPearls.size() > 100) {
            this.processedPearls.clear();
        }
        if ((vec3 = this.predictEnemyLanding(thrownEnderpearl, player2)) == null) {
            return;
        }
        double d4 = vec3.x - thrownEnderpearl.getX();
        double d5 = Math.sqrt(d4 * d4 + (d = vec3.z - thrownEnderpearl.getZ()) * d);
        if (d5 < this.distance.getValue().doubleValue()) {
            return;
        }
        Vec3 vec32 = new Vec3(d4, 0.0, d);
        vec32 = d5 > 1.0E-4 ? vec32.scale(1.0 / d5) : Vec3.ZERO;
        Vec3 vec33 = vec3.add(vec32.scale(this.offset.getValue().doubleValue()));
        BlockHitResult blockHitResult = PearlChaseModule.mc.level.clip(new ClipContext(vec33.add(0.0, 2.0, 0.0), vec33.subtract(0.0, 10.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PearlChaseModule.mc.player));
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            vec33 = blockHitResult.getLocation();
        }
        if ((trajectorySolution = this.solveTrajectory(vec33)) == null) {
            return;
        }
        int n = this.delay.getValue().intValue();
        if (n <= 0) {
            this.executeThrow(trajectorySolution.yaw(), trajectorySolution.pitch(), vec33, trajectorySolution.path());
        } else {
            this.pendingThrow = new PendingThrow(vec33, trajectorySolution.yaw(), trajectorySolution.pitch(), n, trajectorySolution.path());
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return;
        }
        if (this.pendingThrow != null) {
            if (!this.whileEating.getValue() && EntityUtils.isEating()) {
                return;
            }
            int n = this.pendingThrow.delayTicks() - 1;
            if (n <= 0) {
                TrajectorySolution trajectorySolution = this.solveTrajectory(this.pendingThrow.targetPos());
                if (trajectorySolution != null) {
                    this.executeThrow(trajectorySolution.yaw(), trajectorySolution.pitch(), this.pendingThrow.targetPos(), trajectorySolution.path());
                } else {
                    this.executeThrow(this.pendingThrow.yaw(), this.pendingThrow.pitch(), this.pendingThrow.targetPos(), this.pendingThrow.path());
                }
                this.pendingThrow = null;
            } else {
                this.pendingThrow = new PendingThrow(this.pendingThrow.targetPos(), this.pendingThrow.yaw(), this.pendingThrow.pitch(), n, this.pendingThrow.path());
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        long l;
        if (!this.render.getValue() || this.renderLandingPos == null || PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return;
        }
        long l2 = this.renderTimer.timeElapsed();
        if (l2 > (l = (long)(this.renderDuration.getValue().doubleValue() * 1000.0))) {
            this.renderLandingPos = null;
            this.renderPath = null;
            return;
        }
        float f = 1.0f - (float)l2 / (float)l;
        int n = (int)(60.0f * f);
        int n2 = (int)(220.0f * f);
        if (n <= 0 && n2 <= 0) {
            return;
        }
        Color color = new Color(0, 230, 255, n);
        Color color2 = new Color(0, 230, 255, n2);
        Color color3 = new Color(80, 255, 180, n2);
        AABB aABB = new AABB(this.renderLandingPos.x - 0.35, this.renderLandingPos.y, this.renderLandingPos.z - 0.35, this.renderLandingPos.x + 0.35, this.renderLandingPos.y + 0.7, this.renderLandingPos.z + 0.35);
        Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, color);
        Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, color2);
        if (this.renderPath != null && this.renderPath.size() > 1) {
            for (int i = 0; i < this.renderPath.size() - 1; ++i) {
                Renderer3D.renderLine(renderWorldEvent.getMatrices(), this.renderPath.get(i), this.renderPath.get(i + 1), color3);
            }
        }
    }

    private void executeThrow(float f, float f2, Vec3 vec3, List<Vec3> list) {
        if (this.switchMode.getValue().equalsIgnoreCase("None") && PearlChaseModule.mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            if (this.itemDisable.getValue()) {
                Night.CHAT_MANAGER.tagged("You are currently not holding an Ender Pearl.", this.getName());
                this.setToggled(false);
            }
            return;
        }
        int n2 = InventoryUtils.find(Items.ENDER_PEARL, 0, this.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.switchMode.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int n3 = PearlChaseModule.mc.player.getInventory().getSelectedSlot();
        if (n2 == -1) {
            if (this.itemDisable.getValue()) {
                Night.CHAT_MANAGER.tagged("No Ender Pearls could be found in your inventory.", this.getName());
                this.setToggled(false);
            }
            return;
        }
        if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.packetRotate(f, f2, true);
        } else if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(new float[]{f, f2}, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            PearlChaseModule.mc.player.setYRot(f);
            PearlChaseModule.mc.player.setXRot(f2);
        }
        InventoryUtils.switchSlot(this.switchMode.getValue(), n2, n3);
        NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, n, f, f2));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(this.switchMode.getValue(), n2, n3);
        this.renderLandingPos = vec3;
        this.renderPath = list;
        this.renderTimer.reset();
    }

    private Vec3 predictEnemyLanding(ThrownEnderpearl thrownEnderpearl, Player player) {
        Vec3 vec3 = thrownEnderpearl.position();
        Vec3 vec32 = thrownEnderpearl.getDeltaMovement();
        if (vec32.lengthSqr() < 1.0E-4 && player != null) {
            vec32 = PearlChaseModule.getPearlInitialVelocity(player.getYRot(), player.getXRot(), player.getDeltaMovement(), player.onGround());
        }
        for (int i = 0; i < 300; ++i) {
            Vec3 vec33 = vec3.add(vec32);
            BlockHitResult blockHitResult = PearlChaseModule.mc.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)thrownEnderpearl));
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                return blockHitResult.getLocation();
            }
            vec3 = vec33;
            vec32 = vec32.scale(0.99).subtract(0.0, 0.03, 0.0);
        }
        return vec3;
    }

    private TrajectorySolution solveTrajectory(Vec3 vec3) {
        float[] fArray;
        float f;
        Vec3 vec32 = PearlChaseModule.mc.player.getEyePosition();
        Vec3 vec33 = vec3.subtract(vec32);
        float f2 = f = (float)Math.toDegrees(Math.atan2(-vec33.x, vec33.z));
        float f3 = 0.0f;
        double d = Double.MAX_VALUE;
        if (this.avoidObstacles.getValue()) {
            fArray = new float[]{0.0f, -1.0f, 1.0f, -2.5f, 2.5f, -4.0f, 4.0f};
        } else {
            fArray = new float[]{0.0f};
        }
        float[] fArray2 = fArray;
        for (float f4 : fArray) {
            float f5 = f + f4;
            for (float f6 = -85.0f; f6 <= 85.0f; f6 += 1.0f) {
                SimulationResult simulationResult = this.simulatePearl(vec32, f5, f6, vec3, false);
                double d2 = simulationResult.distToTarget();
                if (simulationResult.hitEarlyObstacle()) {
                    d2 += 500.0;
                }
                if (this.arcMode.getValue().equalsIgnoreCase("Low")) {
                    d2 += (double)Math.max(0.0f, -f6) * 0.1;
                } else if (this.arcMode.getValue().equalsIgnoreCase("High")) {
                    d2 += Math.max(0.0, (double)f6 + 50.0) * 0.2;
                } else if (this.arcMode.getValue().equalsIgnoreCase("Smart") && !simulationResult.hitEarlyObstacle()) {
                    d2 += (double)Math.max(0.0f, -f6) * 0.02;
                }
                if (!(d2 < d)) continue;
                d = d2;
                f3 = f6;
                f2 = f5;
            }
            if (d < 1.0) break;
        }
        float f7 = f3;
        for (float f8 = f3 - 1.5f; f8 <= f3 + 1.5f; f8 += 0.1f) {
            SimulationResult simulationResult = this.simulatePearl(vec32, f2, f8, vec3, false);
            double d3 = simulationResult.distToTarget();
            if (simulationResult.hitEarlyObstacle()) {
                d3 += 500.0;
            }
            if (!(d3 < d)) continue;
            d = d3;
            f7 = f8;
        }
        SimulationResult simulationResult = this.simulatePearl(vec32, f2, f7, vec3, true);
        return new TrajectorySolution(f2, f7, simulationResult.path());
    }

    private SimulationResult simulatePearl(Vec3 vec3, float f, float f2, Vec3 vec32, boolean bl) {
        Vec3 vec33 = PearlChaseModule.mc.player != null ? PearlChaseModule.mc.player.getDeltaMovement() : Vec3.ZERO;
        boolean bl2 = PearlChaseModule.mc.player == null || PearlChaseModule.mc.player.onGround();
        Vec3 vec34 = PearlChaseModule.getPearlInitialVelocity(f, f2, vec33, bl2);
        Vec3 vec35 = vec3;
        ArrayList<Vec3> arrayList = bl ? new ArrayList<Vec3>() : null;
        ArrayList<Vec3> arrayList2 = arrayList;
        if (bl) {
            arrayList.add(vec35);
        }
        Vec3 vec36 = null;
        boolean bl3 = false;
        double d = vec32.distanceTo(vec3);
        for (int i = 0; i < 250; ++i) {
            Vec3 vec37 = vec35.add(vec34);
            BlockHitResult blockHitResult = PearlChaseModule.mc.level.clip(new ClipContext(vec35, vec37, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PearlChaseModule.mc.player));
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                vec36 = blockHitResult.getLocation();
                if (bl) {
                    arrayList.add(vec36);
                }
                double d2 = vec36.distanceTo(vec3);
                double d3 = vec36.distanceTo(vec32);
                if (!(d2 < d * 0.7) || !(d3 > 3.0)) break;
                bl3 = true;
                break;
            }
            vec35 = vec37;
            if (bl) {
                arrayList.add(vec35);
            }
            vec34 = vec34.scale(0.99).subtract(0.0, 0.03, 0.0);
        }
        if (vec36 == null) {
            vec36 = vec35;
        }
        double d4 = vec36.distanceTo(vec32);
        return new SimulationResult(vec36, d4, bl3, arrayList != null ? arrayList.size() : 0, arrayList);
    }

    private static Vec3 getLookVector(float f, float f2) {
        float f3 = -Mth.sin((double)(f * ((float)Math.PI / 180))) * Mth.cos((double)(f2 * ((float)Math.PI / 180)));
        float f4 = -Mth.sin((double)(f2 * ((float)Math.PI / 180)));
        float f5 = Mth.cos((double)(f * ((float)Math.PI / 180))) * Mth.cos((double)(f2 * ((float)Math.PI / 180)));
        return new Vec3((double)f3, (double)f4, (double)f5).normalize();
    }

    private static Vec3 getPearlInitialVelocity(float f, float f2, Vec3 vec3, boolean bl) {
        Vec3 vec32 = PearlChaseModule.getLookVector(f, f2);
        Vec3 vec33 = new Vec3(vec3.x, bl ? 0.0 : vec3.y, vec3.z);
        return vec32.scale(1.5).add(vec33);
    }

    private boolean validTarget(Player player) {
        if (player == null || player == PearlChaseModule.mc.player) {
            return false;
        }
        if (player.isDeadOrDying()) {
            return false;
        }
        if (PearlChaseModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue())) {
            return false;
        }
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return false;
        }
        return !this.ignoreNaked.getValue() || !EntityUtils.isNaked(player);
    }

    private Player getBestTarget() {
        if (PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return null;
        }
        Player player = null;
        double d = 0.0;
        for (Player player2 : PearlChaseModule.mc.level.players()) {
            if (!this.validTarget(player2)) continue;
            double score = switch (this.target.getValue()) {
                case "Lowest" -> (double)(player2.getHealth() + player2.getAbsorptionAmount());
                case "Furthest" -> -PearlChaseModule.mc.player.distanceToSqr((Entity)player2);
                default -> PearlChaseModule.mc.player.distanceToSqr((Entity)player2);
            };
            if (player != null && !(score < d)) continue;
            player = player2;
            d = score;
        }
        return player;
    }

    private record PendingThrow(Vec3 targetPos, float yaw, float pitch, int delayTicks, List<Vec3> path) {
    }

    private record TrajectorySolution(float yaw, float pitch, List<Vec3> path) {
    }

    private record SimulationResult(Vec3 landingPos, double distToTarget, boolean hitEarlyObstacle, int ticks, List<Vec3> path) {
    }
}

