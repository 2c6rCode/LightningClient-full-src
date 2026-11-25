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
        double deltaZ;
        Vec3 enemyLanding;
        Player bestTarget;
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
        Player thrower = null;
        Entity owner = thrownEnderpearl.getOwner();
        if (owner instanceof Player) {
            thrower = bestTarget = (Player)owner;
        } else {
            double bestDistSq = 16.0;
            for (Player candidate : PearlChaseModule.mc.level.players()) {
                double distSq;
                if (!this.validTarget(candidate) || !((distSq = candidate.distanceToSqr(thrownEnderpearl.position())) < bestDistSq)) continue;
                bestDistSq = distSq;
                thrower = candidate;
            }
        }
        if (thrower == null || !this.validTarget(thrower)) {
            return;
        }
        bestTarget = this.getBestTarget();
        if (bestTarget != null && thrower != bestTarget) {
            return;
        }
        if (!this.processedPearls.add(thrownEnderpearl.getId())) {
            return;
        }
        if (this.processedPearls.size() > 100) {
            this.processedPearls.clear();
        }
        if ((enemyLanding = this.predictEnemyLanding(thrownEnderpearl, thrower)) == null) {
            return;
        }
        double deltaX = enemyLanding.x - thrownEnderpearl.getX();
        double horizDist = Math.sqrt(deltaX * deltaX + (deltaZ = enemyLanding.z - thrownEnderpearl.getZ()) * deltaZ);
        if (horizDist < this.distance.getValue().doubleValue()) {
            return;
        }
        Vec3 dir = new Vec3(deltaX, 0.0, deltaZ);
        dir = horizDist > 1.0E-4 ? dir.scale(1.0 / horizDist) : Vec3.ZERO;
        Vec3 targetPos = enemyLanding.add(dir.scale(this.offset.getValue().doubleValue()));
        BlockHitResult blockHitResult = PearlChaseModule.mc.level.clip(new ClipContext(targetPos.add(0.0, 2.0, 0.0), targetPos.subtract(0.0, 10.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PearlChaseModule.mc.player));
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            targetPos = blockHitResult.getLocation();
        }
        if ((trajectorySolution = this.solveTrajectory(targetPos)) == null) {
            return;
        }
        int delayTicks = this.delay.getValue().intValue();
        if (delayTicks <= 0) {
            this.executeThrow(trajectorySolution.yaw(), trajectorySolution.pitch(), targetPos, trajectorySolution.path());
        } else {
            this.pendingThrow = new PendingThrow(targetPos, trajectorySolution.yaw(), trajectorySolution.pitch(), delayTicks, trajectorySolution.path());
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
            int remainingDelay = this.pendingThrow.delayTicks() - 1;
            if (remainingDelay <= 0) {
                TrajectorySolution trajectorySolution = this.solveTrajectory(this.pendingThrow.targetPos());
                if (trajectorySolution != null) {
                    this.executeThrow(trajectorySolution.yaw(), trajectorySolution.pitch(), this.pendingThrow.targetPos(), trajectorySolution.path());
                } else {
                    this.executeThrow(this.pendingThrow.yaw(), this.pendingThrow.pitch(), this.pendingThrow.targetPos(), this.pendingThrow.path());
                }
                this.pendingThrow = null;
            } else {
                this.pendingThrow = new PendingThrow(this.pendingThrow.targetPos(), this.pendingThrow.yaw(), this.pendingThrow.pitch(), remainingDelay, this.pendingThrow.path());
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        long durationMs;
        if (!this.render.getValue() || this.renderLandingPos == null || PearlChaseModule.mc.player == null || PearlChaseModule.mc.level == null) {
            return;
        }
        long elapsedMs = this.renderTimer.timeElapsed();
        if (elapsedMs > (durationMs = (long)(this.renderDuration.getValue().doubleValue() * 1000.0))) {
            this.renderLandingPos = null;
            this.renderPath = null;
            return;
        }
        float alphaFactor = 1.0f - (float)elapsedMs / (float)durationMs;
        int fillAlpha = (int)(60.0f * alphaFactor);
        int outlineAlpha = (int)(220.0f * alphaFactor);
        if (fillAlpha <= 0 && outlineAlpha <= 0) {
            return;
        }
        Color fillColor = new Color(0, 230, 255, fillAlpha);
        Color outlineColor = new Color(0, 230, 255, outlineAlpha);
        Color lineColor = new Color(80, 255, 180, outlineAlpha);
        AABB aABB = new AABB(this.renderLandingPos.x - 0.35, this.renderLandingPos.y, this.renderLandingPos.z - 0.35, this.renderLandingPos.x + 0.35, this.renderLandingPos.y + 0.7, this.renderLandingPos.z + 0.35);
        Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, fillColor);
        Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, outlineColor);
        if (this.renderPath != null && this.renderPath.size() > 1) {
            for (int i = 0; i < this.renderPath.size() - 1; ++i) {
                Renderer3D.renderLine(renderWorldEvent.getMatrices(), this.renderPath.get(i), this.renderPath.get(i + 1), lineColor);
            }
        }
    }

    private void executeThrow(float yaw, float pitch, Vec3 landingPos, List<Vec3> path) {
        if (this.switchMode.getValue().equalsIgnoreCase("None") && PearlChaseModule.mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            if (this.itemDisable.getValue()) {
                Night.CHAT_MANAGER.tagged("You are currently not holding an Ender Pearl.", this.getName());
                this.setToggled(false);
            }
            return;
        }
        int pearlSlot = InventoryUtils.find(Items.ENDER_PEARL, 0, this.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.switchMode.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int oldSlot = PearlChaseModule.mc.player.getInventory().getSelectedSlot();
        if (pearlSlot == -1) {
            if (this.itemDisable.getValue()) {
                Night.CHAT_MANAGER.tagged("No Ender Pearls could be found in your inventory.", this.getName());
                this.setToggled(false);
            }
            return;
        }
        if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.packetRotate(yaw, pitch, true);
        } else if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(new float[]{yaw, pitch}, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            PearlChaseModule.mc.player.setYRot(yaw);
            PearlChaseModule.mc.player.setXRot(pitch);
        }
        InventoryUtils.switchSlot(this.switchMode.getValue(), pearlSlot, oldSlot);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(this.switchMode.getValue(), pearlSlot, oldSlot);
        this.renderLandingPos = landingPos;
        this.renderPath = path;
        this.renderTimer.reset();
    }

    private Vec3 predictEnemyLanding(ThrownEnderpearl thrownEnderpearl, Player player) {
        Vec3 pos = thrownEnderpearl.position();
        Vec3 vel = thrownEnderpearl.getDeltaMovement();
        if (vel.lengthSqr() < 1.0E-4 && player != null) {
            vel = PearlChaseModule.getPearlInitialVelocity(player.getYRot(), player.getXRot(), player.getDeltaMovement(), player.onGround());
        }
        for (int i = 0; i < 300; ++i) {
            Vec3 nextPos = pos.add(vel);
            BlockHitResult blockHitResult = PearlChaseModule.mc.level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)thrownEnderpearl));
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                return blockHitResult.getLocation();
            }
            pos = nextPos;
            vel = vel.scale(0.99).subtract(0.0, 0.03, 0.0);
        }
        return pos;
    }

    private TrajectorySolution solveTrajectory(Vec3 targetPos) {
        float[] yawOffsets;
        float baseYaw;
        Vec3 eyePos = PearlChaseModule.mc.player.getEyePosition();
        Vec3 toTarget = targetPos.subtract(eyePos);
        float bestYaw = baseYaw = (float)Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
        float bestPitch = 0.0f;
        double bestScore = Double.MAX_VALUE;
        if (this.avoidObstacles.getValue()) {
            yawOffsets = new float[]{0.0f, -1.0f, 1.0f, -2.5f, 2.5f, -4.0f, 4.0f};
        } else {
            yawOffsets = new float[]{0.0f};
        }
        for (float yawOffset : yawOffsets) {
            float testYaw = baseYaw + yawOffset;
            for (float testPitch = -85.0f; testPitch <= 85.0f; testPitch += 1.0f) {
                SimulationResult simulationResult = this.simulatePearl(eyePos, testYaw, testPitch, targetPos, false);
                double score = simulationResult.distToTarget();
                if (simulationResult.hitEarlyObstacle()) {
                    score += 500.0;
                }
                if (this.arcMode.getValue().equalsIgnoreCase("Low")) {
                    score += (double)Math.max(0.0f, -testPitch) * 0.1;
                } else if (this.arcMode.getValue().equalsIgnoreCase("High")) {
                    score += Math.max(0.0, (double)testPitch + 50.0) * 0.2;
                } else if (this.arcMode.getValue().equalsIgnoreCase("Smart") && !simulationResult.hitEarlyObstacle()) {
                    score += (double)Math.max(0.0f, -testPitch) * 0.02;
                }
                if (!(score < bestScore)) continue;
                bestScore = score;
                bestPitch = testPitch;
                bestYaw = testYaw;
            }
            if (bestScore < 1.0) break;
        }
        float refinedPitch = bestPitch;
        for (float finePitch = bestPitch - 1.5f; finePitch <= bestPitch + 1.5f; finePitch += 0.1f) {
            SimulationResult simulationResult = this.simulatePearl(eyePos, bestYaw, finePitch, targetPos, false);
            double fineScore = simulationResult.distToTarget();
            if (simulationResult.hitEarlyObstacle()) {
                fineScore += 500.0;
            }
            if (!(fineScore < bestScore)) continue;
            bestScore = fineScore;
            refinedPitch = finePitch;
        }
        SimulationResult simulationResult = this.simulatePearl(eyePos, bestYaw, refinedPitch, targetPos, true);
        return new TrajectorySolution(bestYaw, refinedPitch, simulationResult.path());
    }

    private SimulationResult simulatePearl(Vec3 startPos, float yaw, float pitch, Vec3 targetPos, boolean recordPath) {
        Vec3 playerVel = PearlChaseModule.mc.player != null ? PearlChaseModule.mc.player.getDeltaMovement() : Vec3.ZERO;
        boolean onGround = PearlChaseModule.mc.player == null || PearlChaseModule.mc.player.onGround();
        Vec3 vel = PearlChaseModule.getPearlInitialVelocity(yaw, pitch, playerVel, onGround);
        Vec3 pos = startPos;
        ArrayList<Vec3> path = recordPath ? new ArrayList<Vec3>() : null;
        if (recordPath) {
            path.add(pos);
        }
        Vec3 landingPos = null;
        boolean hitObstacle = false;
        double totalDist = targetPos.distanceTo(startPos);
        for (int i = 0; i < 250; ++i) {
            Vec3 nextPos = pos.add(vel);
            BlockHitResult blockHitResult = PearlChaseModule.mc.level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PearlChaseModule.mc.player));
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                landingPos = blockHitResult.getLocation();
                if (recordPath) {
                    path.add(landingPos);
                }
                double hitDist = landingPos.distanceTo(startPos);
                double remainingDist = landingPos.distanceTo(targetPos);
                if (!(hitDist < totalDist * 0.7) || !(remainingDist > 3.0)) break;
                hitObstacle = true;
                break;
            }
            pos = nextPos;
            if (recordPath) {
                path.add(pos);
            }
            vel = vel.scale(0.99).subtract(0.0, 0.03, 0.0);
        }
        if (landingPos == null) {
            landingPos = pos;
        }
        double distToTarget = landingPos.distanceTo(targetPos);
        return new SimulationResult(landingPos, distToTarget, hitObstacle, path != null ? path.size() : 0, path);
    }

    private static Vec3 getLookVector(float yaw, float pitch) {
        float vx = -Mth.sin((double)(yaw * ((float)Math.PI / 180))) * Mth.cos((double)(pitch * ((float)Math.PI / 180)));
        float vy = -Mth.sin((double)(pitch * ((float)Math.PI / 180)));
        float vz = Mth.cos((double)(yaw * ((float)Math.PI / 180))) * Mth.cos((double)(pitch * ((float)Math.PI / 180)));
        return new Vec3((double)vx, (double)vy, (double)vz).normalize();
    }

    private static Vec3 getPearlInitialVelocity(float yaw, float pitch, Vec3 playerVel, boolean onGround) {
        Vec3 lookVec = PearlChaseModule.getLookVector(yaw, pitch);
        Vec3 inheritedVel = new Vec3(playerVel.x, onGround ? 0.0 : playerVel.y, playerVel.z);
        return lookVec.scale(1.5).add(inheritedVel);
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
        Player bestTarget = null;
        double bestScore = 0.0;
        for (Player candidate : PearlChaseModule.mc.level.players()) {
            if (!this.validTarget(candidate)) continue;
            double score = switch (this.target.getValue()) {
                case "Lowest" -> (double)(candidate.getHealth() + candidate.getAbsorptionAmount());
                case "Furthest" -> -PearlChaseModule.mc.player.distanceToSqr((Entity)candidate);
                default -> PearlChaseModule.mc.player.distanceToSqr((Entity)candidate);
            };
            if (bestTarget != null && !(score < bestScore)) continue;
            bestTarget = candidate;
            bestScore = score;
        }
        return bestTarget;
    }

    private record PendingThrow(Vec3 targetPos, float yaw, float pitch, int delayTicks, List<Vec3> path) {
    }

    private record TrajectorySolution(float yaw, float pitch, List<Vec3> path) {
    }

    private record SimulationResult(Vec3 landingPos, double distToTarget, boolean hitEarlyObstacle, int ticks, List<Vec3> path) {
    }
}

