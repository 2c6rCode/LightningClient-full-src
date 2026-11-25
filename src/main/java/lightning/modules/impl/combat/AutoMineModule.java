/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="AutoMine", description="Manages which blocks SpeedMine should mine to defeat your opponents.", category=Module.Category.COMBAT)
public class AutoMineModule
extends Module {
    public ModeSetting priority = new ModeSetting("Priority", "Which area of the target to prioritize mining.", "Leg", new String[]{"Leg", "Head"});
    public ModeSetting logic = new ModeSetting("Logic", "Auto mining targeting logic.", "Grim", new String[]{"Grim", "NCP"});
    public BooleanSetting raytrace = new BooleanSetting("Raytrace", "Only mines blocks that are in line of sight (visible).", false);
    public BooleanSetting upFallback = new BooleanSetting("CivBreak", "Priority=Leg only: when Raytrace can't see any leg-level surround block, mines the block above it instead. Turn on AutoCrystal's own MineIgnore setting for the crystal pre-place/detonate combo -- this module only builds+mines the obsidian.", new BooleanSetting.Visibility(this.raytrace, true), false);
    public ModeSetting upFallbackRotate = new ModeSetting("Rotate", "The rotation mode when placing the Up-fallback obsidian.", new BooleanSetting.Visibility(this.upFallback, true), "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", false);
    public BooleanSetting avoidSharing = new BooleanSetting("AvoidSharing", "Avoids mining blocks that are part of your own surround or safety ring.", true);
    public BooleanSetting terrain = new BooleanSetting("Terrain", "Automatically mines terrain blocks to place obsidian when target has no crystal base or is fluid-covered.", false);
    public BooleanSetting terrainPlace = new BooleanSetting("TerrainPlace", "Automatically places obsidian once the terrain block is broken.", new BooleanSetting.Visibility(this.terrain, true), true);
    public BooleanSetting ironPickaxe = new BooleanSetting("IronPickaxe", "Uses iron pickaxe after obsidian matures to prevent obsidian item drops.", false);
    public BooleanSetting antiCrawl = new BooleanSetting("AntiCrawl", "While crawling, mines the block above/below your feet to stand back up instead of staying trapped.", true);
    public BooleanSetting face = new BooleanSetting("Face", "Mines the block stuck in your face to rescue you from face traps or phased blocks.", true);
    private final List<BlockPos> pendingTerrainPlacements = new ArrayList<BlockPos>();
    private TerrainPair activeTerrainPair = null;
    private UUID activeTerrainTarget = null;
    private BlockPos upComboPos = null;

    public boolean isRaytrace() {
        return this.raytrace.getValue();
    }

    public boolean isUpComboActive(BlockPos blockPos) {
        return this.upFallback.getValue() && this.upComboPos != null && this.upComboPos.equals((Object)blockPos);
    }

    private void resetUpCombo() {
        this.upComboPos = null;
    }

    private SpeedMineModule speedMine() {
        return Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        this.resetUpCombo();
    }

    @Override
    public void onEnable() {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        this.resetUpCombo();
    }

    @Override
    public void onDisable() {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        this.resetUpCombo();
        SpeedMineModule speedMineModule = this.speedMine();
        if (speedMineModule != null) {
            speedMineModule.setDoubleEngaged(false);
            speedMineModule.restoreIronSwap();
        }
    }

    private boolean isRealTerrainBase(BlockPos blockPos) {
        if (blockPos == null || AutoMineModule.mc.level == null) {
            return false;
        }
        Block block = AutoMineModule.mc.level.getBlockState(blockPos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
    }

    private boolean isMinableLegCandidate(BlockPos blockPos, SpeedMineModule speedMineModule) {
        BlockState blockState = AutoMineModule.mc.level.getBlockState(blockPos);
        if (blockState.canBeReplaced()) {
            return false;
        }
        return !(blockState.getBlock().defaultDestroyTime() < 0.0f) || speedMineModule.sixB.getValue() && blockState.getBlock() == Blocks.BEDROCK;
    }

    private boolean isPhased(Player player) {
        return EntityUtils.isPhased((Entity)player);
    }

    private boolean isTargetPhasedInBedrock(Player player) {
        if (player == null || AutoMineModule.mc.level == null) {
            return false;
        }
        for (BlockPos blockPos : EntityUtils.getPhasedBlocks((Entity)player)) {
            if (!AutoMineModule.mc.level.getBlockState(blockPos).is(Blocks.BEDROCK)) continue;
            return true;
        }
        return false;
    }

    private BlockPos getGrimBedrockHeadTarget(Player player, SpeedMineModule speedMineModule) {
        BlockPos topPos;
        if (player == null || AutoMineModule.mc.level == null) {
            return null;
        }
        if (!this.logic.getValue().equalsIgnoreCase("Grim")) {
            return null;
        }
        if (!this.isTargetPhasedInBedrock(player)) {
            return null;
        }
        int floorY = Mth.floor((double)player.getY());
        int headY = player.isVisuallyCrawling() || player.getBoundingBox().getYsize() <= 1.0 ? floorY + 1 : floorY + 2;
        SpeedMineModule.Action action = speedMineModule.getPrimary();
        if (action != null && action.isInstantMine() && action.getPosition().getY() == headY && speedMineModule.isTargetSurroundPosition(action.getPosition(), player)) {
            return action.getPosition();
        }
        ArrayList<BlockPos> candidates = new ArrayList<BlockPos>();
        for (BlockPos insidePos : HoleUtils.getInsidePositions((Entity)player)) {
            BlockPos headPos = new BlockPos(insidePos.getX(), headY, insidePos.getZ());
            if (candidates.contains(headPos)) continue;
            candidates.add(headPos);
        }
        BlockPos abovePos = player.blockPosition().above(headY - floorY);
        if (!candidates.contains(abovePos)) {
            candidates.add(abovePos);
        }
        if (!candidates.contains(topPos = BlockPos.containing((double)player.getX(), (double)(player.getBoundingBox().maxY + 0.2), (double)player.getZ()))) {
            candidates.add(topPos);
        }
        candidates.sort(Comparator.comparingDouble(blockPos -> {
            double distSq = AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos));
            if (this.isRaytrace() && WorldUtils.canSeeBlock(blockPos)) {
                distSq -= 1000.0;
            }
            return distSq;
        }));
        for (BlockPos headPos : candidates) {
            BlockState blockState;
            if (speedMineModule.isOutOfRange(headPos) || this.isRaytrace() && !WorldUtils.canSeeBlock(headPos) || (blockState = AutoMineModule.mc.level.getBlockState(headPos)).canBeReplaced() || !WorldUtils.canBreak(headPos) || blockState.is(Blocks.COBWEB) || !speedMineModule.isValid(headPos)) continue;
            return headPos;
        }
        return null;
    }

    private BlockPos getSelfFaceBlock() {
        if (AutoMineModule.mc.player == null || AutoMineModule.mc.level == null) {
            return null;
        }
        if (AutoMineModule.mc.player.isVisuallyCrawling()) {
            return null;
        }
        BlockPos eyeBlockPos = BlockPos.containing((Position)AutoMineModule.mc.player.getEyePosition());
        if (WorldUtils.canBreak(eyeBlockPos) && !WorldUtils.isReplaceable(eyeBlockPos)) {
            return eyeBlockPos;
        }
        BlockPos headBlockPos = AutoMineModule.mc.player.blockPosition().above();
        if (WorldUtils.canBreak(headBlockPos) && !WorldUtils.isReplaceable(headBlockPos)) {
            return headBlockPos;
        }
        return null;
    }

    public boolean isProtectedTerrainBase(BlockPos blockPos) {
        return this.terrain.getValue() && this.activeTerrainPair != null && blockPos != null && blockPos.equals((Object)this.activeTerrainPair.basePos()) && this.isRealTerrainBase(blockPos);
    }

    private boolean allSidesTerraformed(Player player, SpeedMineModule speedMineModule) {
        BlockPos playerPos = player.blockPosition();
        boolean hasValidSide = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            boolean hasBase;
            BlockPos surroundPos = playerPos.relative(direction);
            BlockPos basePos = surroundPos.below();
            if (speedMineModule.isOutOfRange(surroundPos) || speedMineModule.isOutOfRange(basePos)) continue;
            BlockState surroundState = AutoMineModule.mc.level.getBlockState(surroundPos);
            if (surroundState.getBlock().defaultDestroyTime() < 0.0f && (!speedMineModule.sixB.getValue() || surroundState.getBlock() != Blocks.BEDROCK)) continue;
            hasValidSide = true;
            BlockState baseState = AutoMineModule.mc.level.getBlockState(basePos);
            hasBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
            if (hasBase) continue;
            return false;
        }
        return hasValidSide;
    }

    public TerrainPair getBestTerrainPair(Player player) {
        SpeedMineModule speedMineModule = this.speedMine();
        if (player == null || AutoMineModule.mc.level == null || AutoMineModule.mc.player == null || speedMineModule == null) {
            this.activeTerrainPair = null;
            this.activeTerrainTarget = null;
            return null;
        }
        if (this.allSidesTerraformed(player, speedMineModule)) {
            this.activeTerrainPair = null;
            this.activeTerrainTarget = null;
            return null;
        }
        UUID targetUuid = player.getUUID();
        BlockPos targetFeetPos = HoleUtils.getTargetFeetPosition(player);
        if (this.activeTerrainPair != null && targetUuid.equals(this.activeTerrainTarget)) {
            BlockPos surroundPos = this.activeTerrainPair.surroundPos();
            BlockPos basePos = this.activeTerrainPair.basePos();
            boolean inRange = !speedMineModule.isOutOfRange(surroundPos) && !speedMineModule.isOutOfRange(basePos);
            boolean nearTarget = targetFeetPos.distManhattan((Vec3i)surroundPos) <= 2;
            BlockState surroundState = AutoMineModule.mc.level.getBlockState(surroundPos);
            BlockState baseState = AutoMineModule.mc.level.getBlockState(basePos);
            boolean surroundValid = surroundState.getBlock().defaultDestroyTime() >= 0.0f || speedMineModule.sixB.getValue() && surroundState.getBlock() == Blocks.BEDROCK || speedMineModule.isMining(surroundPos);
            boolean baseValid = baseState.getBlock().defaultDestroyTime() >= 0.0f || baseState.getBlock() == Blocks.BEDROCK || baseState.getBlock() == Blocks.OBSIDIAN || this.pendingTerrainPlacements.contains(basePos);
            if (inRange && nearTarget && surroundValid && baseValid) {
                return this.activeTerrainPair;
            }
        }
        HashSet<BlockPos> avoidPositions = new HashSet<BlockPos>();
        if (this.avoidSharing.getValue()) {
            avoidPositions.addAll(HoleUtils.getFeetPositions((Player)AutoMineModule.mc.player, true, false, true));
            for (BlockPos insidePos : HoleUtils.getInsidePositions((Entity)AutoMineModule.mc.player)) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    avoidPositions.add(insidePos.relative(direction));
                }
            }
        }
        TerrainPair bestPair = null;
        double bestScore = Double.MAX_VALUE;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos surroundPos = targetFeetPos.relative(direction);
            BlockPos basePos = surroundPos.below();
            if (speedMineModule.isOutOfRange(surroundPos) || speedMineModule.isOutOfRange(basePos)) continue;
            BlockState surroundState = AutoMineModule.mc.level.getBlockState(surroundPos);
            BlockState baseState = AutoMineModule.mc.level.getBlockState(basePos);
            if (surroundState.getBlock().defaultDestroyTime() < 0.0f && !speedMineModule.isMining(surroundPos) || baseState.getBlock().defaultDestroyTime() < 0.0f && baseState.getBlock() != Blocks.BEDROCK && baseState.getBlock() != Blocks.OBSIDIAN && !this.pendingTerrainPlacements.contains(basePos)) continue;
            double score = 0.0;
            if (this.avoidSharing.getValue() && (avoidPositions.contains(surroundPos) || avoidPositions.contains(basePos))) {
                score += 100000.0;
            }
            boolean isHardBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
            if (isHardBase) {
                score -= 20000.0;
            } else if (!baseState.canBeReplaced()) {
                score += (double)baseState.getBlock().defaultDestroyTime() * 10.0;
            }
            if (!surroundState.canBeReplaced()) {
                score -= 10000.0;
            }
            boolean currentlyMining = speedMineModule.isMining(surroundPos) || speedMineModule.isMining(basePos);
            if (currentlyMining) {
                score -= 50000.0;
            }
            if (!((score += AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)surroundPos))) < bestScore)) continue;
            bestScore = score;
            bestPair = new TerrainPair(direction, surroundPos, basePos, score);
        }
        this.activeTerrainPair = bestPair;
        this.activeTerrainTarget = bestPair != null ? targetUuid : null;
        return bestPair;
    }

    public void placePendingTerrain() {
        if (!this.terrainPlace.getValue() || this.pendingTerrainPlacements.isEmpty()) {
            return;
        }
        if (AutoMineModule.mc.player == null || AutoMineModule.mc.level == null) {
            return;
        }
        SpeedMineModule speedMineModule = this.speedMine();
        if (speedMineModule == null) {
            return;
        }
        if (!speedMineModule.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        int blockSlot = speedMineModule.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.find(Items.OBSIDIAN, 0, speedMineModule.switchMode.getValue().equalsIgnoreCase("AltSwap") || speedMineModule.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
        if (blockSlot == -1) {
            blockSlot = InventoryUtils.findHardestBlock(0, 8);
        }
        if (blockSlot == -1) {
            return;
        }
        ArrayList<BlockPos> completedPositions = new ArrayList<BlockPos>();
        for (BlockPos blockPos : new ArrayList<BlockPos>(this.pendingTerrainPlacements)) {
            if (speedMineModule.isOutOfRange(blockPos)) {
                completedPositions.add(blockPos);
                continue;
            }
            BlockState blockState = AutoMineModule.mc.level.getBlockState(blockPos);
            if (blockState.getBlock() == Blocks.OBSIDIAN) {
                completedPositions.add(blockPos);
                speedMineModule.dropPrimaryIfInvalid(pos -> !pos.equals((Object)blockPos));
                speedMineModule.dropSecondaryIfInvalid(pos -> !pos.equals((Object)blockPos));
                continue;
            }
            if (!blockState.canBeReplaced() || !WorldUtils.isPlaceable(blockPos)) continue;
            Direction direction = WorldUtils.getDirection(blockPos, false);
            if (direction == null) {
                direction = WorldUtils.getClosestDirection(blockPos, true);
            }
            if (direction == null) continue;
            int selectedSlot = AutoMineModule.mc.player.getInventory().getSelectedSlot();
            InventoryUtils.switchSlot(speedMineModule.switchMode.getValue(), blockSlot, selectedSlot);
            boolean placed = WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, speedMineModule.rotate.getValue().equalsIgnoreCase("Packet") || speedMineModule.rotate.getValue().equalsIgnoreCase("Normal") || speedMineModule.rotate.getValue().equalsIgnoreCase("Silent"), true, speedMineModule.render.getValue().equalsIgnoreCase("Both") || speedMineModule.render.getValue().equalsIgnoreCase("Fill"));
            InventoryUtils.switchBack(speedMineModule.switchMode.getValue(), blockSlot, selectedSlot);
            if (!placed) continue;
            completedPositions.add(blockPos);
            speedMineModule.dropPrimaryIfInvalid(pos -> !pos.equals((Object)blockPos));
            speedMineModule.dropSecondaryIfInvalid(pos -> !pos.equals((Object)blockPos));
        }
        completedPositions.forEach(this.pendingTerrainPlacements::remove);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        SpeedMineModule speedMineModule;
        if (!PingBypassFlags.isPingBypassActive() && AutoMineModule.mc.player != null && AutoMineModule.mc.level != null && (speedMineModule = this.speedMine()) != null && speedMineModule.isToggled()) {
            if (this.antiCrawl.getValue() && AutoMineModule.mc.player.isVisuallyCrawling()) {
                BlockPos playerPos = AutoMineModule.mc.player.blockPosition();
                BlockPos crawlPos = !WorldUtils.canBreak(playerPos.below()) || WorldUtils.isReplaceable(playerPos.below()) || (WorldUtils.isReplaceable(playerPos.below(2)) && HoleUtils.getSingleHole(playerPos.below(2), 1.0, false) == null) ? playerPos.above() : playerPos.below();
                if (speedMineModule.isValid(crawlPos) && !speedMineModule.isOutOfRange(crawlPos)) {
                    if (speedMineModule.isMining(crawlPos)) {
                        return;
                    }
                    speedMineModule.handle(crawlPos, 0);
                    return;
                }
            }
            BlockPos facePos;
            if (this.face.getValue() && (facePos = this.getSelfFaceBlock()) != null && speedMineModule.isValid(facePos) && !speedMineModule.isOutOfRange(facePos)) {
                if (speedMineModule.isMining(facePos)) {
                    return;
                }
                speedMineModule.handle(facePos, 0);
                return;
            }
            if (!this.tickUpFallback(speedMineModule)) {
                SpeedMineModule.Action primaryAction = speedMineModule.getPrimary();
                SpeedMineModule.Secondary secondaryAction = speedMineModule.getSecondary();
                BlockPos eyePos = BlockPos.containing((Position)AutoMineModule.mc.player.getEyePosition());
                BlockPos abovePos = AutoMineModule.mc.player.blockPosition().above();
                BlockPos feetPos = AutoMineModule.mc.player.blockPosition();
                if (primaryAction != null && primaryAction.getPriority() == 0 && (primaryAction.getPosition().equals(eyePos) || primaryAction.getPosition().equals(abovePos) || primaryAction.getPosition().equals(feetPos) || primaryAction.getPosition().equals(feetPos.below())) && AutoMineModule.mc.level.getBlockState(primaryAction.getPosition()).canBeReplaced()) {
                    speedMineModule.dropPrimary();
                    primaryAction = null;
                }
                if (secondaryAction != null && secondaryAction.getPriority() == 0 && (secondaryAction.getPosition().equals(eyePos) || secondaryAction.getPosition().equals(abovePos) || secondaryAction.getPosition().equals(feetPos) || secondaryAction.getPosition().equals(feetPos.below())) && AutoMineModule.mc.level.getBlockState(secondaryAction.getPosition()).canBeReplaced()) {
                    speedMineModule.dropSecondary();
                    secondaryAction = null;
                }
                SpeedMineModule.Target rawTarget = speedMineModule.getTarget();
                SpeedMineModule.Target target = rawTarget == null || (this.ignoreNaked.getValue() && EntityUtils.isNaked(rawTarget.player())) ? null : rawTarget;
                BlockPos secondaryBlockPos = secondaryAction != null ? secondaryAction.getPosition() : null;
                int secPriority = secondaryAction != null ? secondaryAction.getPriority() : 0;
                if (!(primaryAction != null && primaryAction.getPriority() > 0 && !WorldUtils.isReplaceable(primaryAction.getPosition()) || secondaryBlockPos != null && secPriority > 0 && !WorldUtils.isReplaceable(secondaryBlockPos) || primaryAction != null && primaryAction.getPriority() == 0 && speedMineModule.sixB.getValue() && primaryAction.getState().getBlock() == Blocks.BEDROCK && target != null && speedMineModule.isTargetSurroundPosition(primaryAction.getPosition(), target.player()))) {
                    if (speedMineModule.doubleMine.getValue()) {
                        if (!speedMineModule.isMineTimerReady()) {
                            return;
                        }
                        if (target == null) {
                            speedMineModule.setDoubleEngaged(false);
                        }
                        if (target != null) {
                            ArrayList<BlockPos> validPositions = new ArrayList<BlockPos>();
                            List<BlockPos> insideList = HoleUtils.getInsidePositions((Entity)target.player());
                            validPositions.addAll(insideList);
                            HashSet<BlockPos> feetPositions = HoleUtils.getFeetPositions(target.player(), true, false, true);
                            validPositions.addAll(feetPositions);
                            for (BlockPos feetBp : feetPositions) {
                                validPositions.add(feetBp.below());
                                for (Direction direction : Direction.Plane.HORIZONTAL) {
                                    validPositions.add(feetBp.relative(direction).below());
                                }
                            }
                            for (BlockPos insideBp : insideList) {
                                for (Direction direction : Direction.Plane.HORIZONTAL) {
                                    validPositions.add(insideBp.relative(direction));
                                    validPositions.add(insideBp.relative(direction).below());
                                }
                                validPositions.add(insideBp.above());
                                validPositions.add(insideBp.below());
                            }
                            TerrainPair terrainPair = this.terrain.getValue() ? this.getBestTerrainPair(target.player()) : null;
                            if (terrainPair != null) {
                                validPositions.add(terrainPair.surroundPos());
                                validPositions.add(terrainPair.basePos());
                            }
                            BlockPos grimHeadTarget = this.logic.getValue().equalsIgnoreCase("Grim") ? this.getGrimBedrockHeadTarget(target.player(), speedMineModule) : null;
                            if (grimHeadTarget != null) {
                                validPositions.add(grimHeadTarget);
                            }
                            if (primaryAction != null && primaryAction.isTerrainBase() && this.isRealTerrainBase(primaryAction.getPosition())) {
                                speedMineModule.dropPrimary();
                                primaryAction = null;
                            }
                            speedMineModule.setDoubleEngaged(true);
                            if (primaryAction != null && primaryAction.getPriority() == 0 && !primaryAction.isTerrainBase()) {
                                boolean isTargetSurround = speedMineModule.isTargetSurroundPosition(primaryAction.getPosition(), target.player());
                                if (!isTargetSurround) {
                                    speedMineModule.dropPrimary();
                                    primaryAction = null;
                                } else if (!primaryAction.isInstantMine()) {
                                    BlockPos primPos = primaryAction.getPosition();
                                    boolean insideBroken = HoleUtils.getInsidePositions((Entity)target.player()).contains(primPos) && AutoMineModule.mc.level.getBlockState(primPos).canBeReplaced();
                                    if (!validPositions.contains(primPos) || insideBroken) {
                                        speedMineModule.dropPrimary();
                                        primaryAction = null;
                                    }
                                }
                            }
                            if (secondaryAction != null && secondaryAction.getPriority() == 0 && !secondaryAction.isHolding()) {
                                BlockPos secPos = secondaryAction.getPosition();
                                boolean insideBroken = HoleUtils.getInsidePositions((Entity)target.player()).contains(secPos) && AutoMineModule.mc.level.getBlockState(secPos).canBeReplaced();
                                if (!validPositions.contains(secPos) || insideBroken) {
                                    speedMineModule.dropSecondary();
                                    secondaryAction = null;
                                }
                            }
                            TerrainPair finalTerrainPair = terrainPair;
                            Runnable terrainRunnable = () -> {
                                if (this.terrain.getValue() && finalTerrainPair != null) {
                                    BlockPos terrainBase = finalTerrainPair.basePos();
                                    BlockPos terrainSurround = finalTerrainPair.surroundPos();
                                    BlockState baseState = AutoMineModule.mc.level.getBlockState(terrainBase);
                                    BlockState surroundState = AutoMineModule.mc.level.getBlockState(terrainSurround);
                                    boolean hasHardBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
                                    if (!hasHardBase && this.terrainPlace.getValue() && !this.pendingTerrainPlacements.contains(terrainBase)) {
                                        this.pendingTerrainPlacements.add(terrainBase);
                                    }
                                    if (this.isPhased(target.player())) {
                                        if (speedMineModule.primaryCamping()) {
                                            BlockPos campPos = speedMineModule.getPrimary().getPosition();
                                            if (!campPos.equals(terrainBase) && !campPos.equals(terrainSurround)) {
                                                return;
                                            }
                                        }
                                        boolean hasFreeSlot = speedMineModule.getPrimary() == null || speedMineModule.getSecondary() == null;
                                        if (!hasHardBase && !baseState.canBeReplaced()) {
                                            if (!speedMineModule.isMining(terrainBase) && hasFreeSlot) {
                                                speedMineModule.handle(terrainBase, 0);
                                                SpeedMineModule.Action action = speedMineModule.getPrimary();
                                                if (action != null && action.getPosition().equals(terrainBase)) {
                                                    action.setTerrainBase(true);
                                                }
                                            }
                                        } else if (hasHardBase && !surroundState.canBeReplaced()) {
                                            SpeedMineModule.Action action = speedMineModule.getPrimary();
                                            boolean alreadyTargetingSurround = action != null && action.getPosition().equals(terrainSurround);
                                            if (!alreadyTargetingSurround) {
                                                speedMineModule.handle(terrainSurround, 0);
                                                SpeedMineModule.Action action2 = speedMineModule.getPrimary();
                                                if (action2 != null && action2.getPosition().equals(terrainSurround)) {
                                                    action2.setTerrainSurround(true);
                                                }
                                            }
                                        }
                                    } else {
                                        if (!(hasHardBase || baseState.canBeReplaced() || speedMineModule.isMining(terrainBase))) {
                                            speedMineModule.handle(terrainBase, 0);
                                        }
                                        if (!surroundState.canBeReplaced() && !speedMineModule.isMining(terrainSurround)) {
                                            if (hasHardBase && speedMineModule.doubleMine.getValue()) {
                                                BlockPos candidate = HoleUtils.getFeetPositions(target.player(), true, false, true).stream()
                                                    .filter(bp -> !bp.equals(terrainSurround))
                                                    .filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced())
                                                    .filter(bp -> !speedMineModule.isMining(bp) && !speedMineModule.isInvalid(bp) && !speedMineModule.isOutOfRange(bp))
                                                    .min(Comparator.comparingDouble(bp -> AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)bp))))
                                                    .orElse(null);
                                                if (candidate != null) {
                                                    speedMineModule.handle(candidate, 0);
                                                }
                                            }
                                            speedMineModule.handle(terrainSurround, 0);
                                            SpeedMineModule.Action action = speedMineModule.getPrimary();
                                            if (action != null && action.getPosition().equals(terrainSurround)) {
                                                action.setTerrainSurround(true);
                                            }
                                        }
                                    }
                                }
                            };
                            Runnable feetRunnable = () -> {
                                block13: {
                                    SpeedMineModule.Action action = speedMineModule.getPrimary();
                                    boolean campingInside = action != null && speedMineModule.primaryCamping() && HoleUtils.getInsidePositions((Entity)target.player()).contains(action.getPosition());
                                    if (speedMineModule.primaryCamping() && !campingInside || action != null && speedMineModule.sixB.getValue() && action.getState().getBlock() == Blocks.BEDROCK || HoleUtils.getInsidePositions((Entity)target.player()).stream().filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced()).toList().size() >= 2 || (this.terrain.getValue() && action != null && action.isTerrainSurround() && !action.isStarted())) break block13;
                                    if (this.terrain.getValue() && finalTerrainPair != null) {
                                        BlockState baseState = AutoMineModule.mc.level.getBlockState(finalTerrainPair.basePos());
                                        boolean hasHardBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
                                        if (!hasHardBase) {
                                            return;
                                        }
                                        if ((speedMineModule.isMining(finalTerrainPair.basePos()) || speedMineModule.isMining(finalTerrainPair.surroundPos())) && speedMineModule.slotsFull()) {
                                            return;
                                        }
                                    }
                                    if (this.logic.getValue().equals("NCP")) {
                                        for (BlockPos feetPosCandidate : HoleUtils.getFeetPositions(target.player(), true, false, true).stream().filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced()).filter(bp -> !this.isRaytrace() || WorldUtils.canSeeBlock(bp)).toList()) {
                                            if (!speedMineModule.slotsFull()) {
                                                if (speedMineModule.isMining(feetPosCandidate) || speedMineModule.isInvalid(feetPosCandidate) || speedMineModule.isOutOfRange(feetPosCandidate)) continue;
                                                speedMineModule.handle(feetPosCandidate, 0);
                                                continue;
                                            }
                                            break;
                                        }
                                    } else {
                                        HashSet<BlockPos> shareAvoid = new HashSet<BlockPos>();
                                        if (this.avoidSharing.getValue() && AutoMineModule.mc.player != null) {
                                            shareAvoid.addAll(HoleUtils.getFeetPositions((Player)AutoMineModule.mc.player, true, false, true));
                                            for (BlockPos playerInside : HoleUtils.getInsidePositions((Entity)AutoMineModule.mc.player)) {
                                                for (Direction dir : Direction.Plane.HORIZONTAL) {
                                                    shareAvoid.add(playerInside.relative(dir));
                                                }
                                            }
                                        }
                                        List<BlockPos> sortedFeet = HoleUtils.getFeetPositions(target.player(), true, false, true).stream().filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced()).filter(bp -> !this.isRaytrace() || WorldUtils.canSeeBlock(bp)).sorted(Comparator.comparingDouble(pos -> this.scoreFeetBlockPos(shareAvoid, speedMineModule, finalTerrainPair, pos))).toList();
                                        boolean bypassSlotCheck = campingInside;
                                        for (BlockPos targetFeetPos : sortedFeet) {
                                            if (!bypassSlotCheck && speedMineModule.slotsFull()) break;
                                            if (!speedMineModule.isMining(targetFeetPos)) {
                                                if (speedMineModule.isInvalid(targetFeetPos) || speedMineModule.isOutOfRange(targetFeetPos)) continue;
                                                boolean isBedrock = speedMineModule.sixB.getValue() && AutoMineModule.mc.level.getBlockState(targetFeetPos).getBlock() == Blocks.BEDROCK;
                                                if (isBedrock && speedMineModule.doubleMine.getValue() && speedMineModule.getPrimary() == null) {
                                                    BlockPos secondFeet = sortedFeet.stream()
                                                        .filter(bp -> !bp.equals(targetFeetPos))
                                                        .filter(bp -> AutoMineModule.mc.level.getBlockState(bp).getBlock() != Blocks.BEDROCK)
                                                        .filter(bp -> !speedMineModule.isMining(bp) && !speedMineModule.isInvalid(bp) && !speedMineModule.isOutOfRange(bp))
                                                        .findFirst().orElse(null);
                                                    if (secondFeet != null) {
                                                        speedMineModule.handle(secondFeet, 0);
                                                    }
                                                }
                                                speedMineModule.handle(targetFeetPos, 0);
                                                bypassSlotCheck = false;
                                            }
                                            if (bypassSlotCheck || !speedMineModule.slotsFull()) continue;
                                            break;
                                        }
                                    }
                                }
                            };
                            Runnable phaseRunnable = () -> {
                                SpeedMineModule.Action action = speedMineModule.getPrimary();
                                if (!this.terrain.getValue() || action == null || !action.isTerrainSurround() || action.isStarted()) {
                                    int targetFloorY = Mth.floor((double)target.player().getY());
                                    for (BlockPos phaseInsidePos : HoleUtils.getInsidePositions((Entity)target.player()).stream().filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced()).filter(bp -> !this.isRaytrace() || WorldUtils.canSeeBlock(bp)).sorted(Comparator.comparingDouble(bp -> bp.getY() > targetFloorY ? 1.0 : 0.0)).toList()) {
                                        if (speedMineModule.isInvalid(phaseInsidePos) || speedMineModule.isOutOfRange(phaseInsidePos)) continue;
                                        if (speedMineModule.phaseSlotsFull()) break;
                                        speedMineModule.handle(phaseInsidePos, 0);
                                        if (!speedMineModule.phaseSlotsFull()) continue;
                                        break;
                                    }
                                }
                            };
                            BlockPos headTarget = null;
                            if (this.priority.getValue().equalsIgnoreCase("Head")) {
                                headTarget = this.getHeadTarget(target.player(), speedMineModule);
                            }
                            if (headTarget == null && this.logic.getValue().equalsIgnoreCase("Grim")) {
                                headTarget = this.getGrimBedrockHeadTarget(target.player(), speedMineModule);
                            }
                            if (headTarget != null && !speedMineModule.isMining(headTarget)) {
                                speedMineModule.handle(headTarget, 0);
                            }
                            if (headTarget == null) {
                                phaseRunnable.run();
                            }
                            if (headTarget == null && this.terrain.getValue()) {
                                terrainRunnable.run();
                            }
                            if (headTarget == null) {
                                feetRunnable.run();
                            }
                        }
                    } else {
                        SpeedMineModule.Action action;
                        TerrainPair terrainPair;
                        BlockPos minePos = null;
                        BlockPos grimHeadPos = null;
                        boolean isTerrainBase = false;
                        if (target == null) {
                            return;
                        }
                        if (primaryAction != null && primaryAction.getPriority() == 0 && !primaryAction.isTerrainBase()) {
                            boolean insideBroken;
                            boolean isTargetSurround = speedMineModule.isTargetSurroundPosition(primaryAction.getPosition(), target.player());
                            insideBroken = HoleUtils.getInsidePositions((Entity)target.player()).contains(primaryAction.getPosition()) && AutoMineModule.mc.level.getBlockState(primaryAction.getPosition()).canBeReplaced();
                            if (!isTargetSurround) {
                                speedMineModule.dropPrimary();
                                primaryAction = null;
                            } else if (insideBroken) {
                                speedMineModule.dropPrimary();
                                primaryAction = null;
                            } else if (speedMineModule.sixB.getValue() && primaryAction.getState().getBlock() == Blocks.BEDROCK) {
                                return;
                            }
                        }
                        if (this.priority.getValue().equalsIgnoreCase("Head")) {
                            minePos = this.getHeadTarget(target.player(), speedMineModule);
                        }
                        if (minePos == null && this.logic.getValue().equalsIgnoreCase("Grim")) {
                            minePos = grimHeadPos = this.getGrimBedrockHeadTarget(target.player(), speedMineModule);
                        }
                        if (minePos == null && this.terrain.getValue() && (terrainPair = this.getBestTerrainPair(target.player())) != null) {
                            boolean hasHardBase;
                            BlockState baseState = AutoMineModule.mc.level.getBlockState(terrainPair.basePos());
                            BlockState surroundState = AutoMineModule.mc.level.getBlockState(terrainPair.surroundPos());
                            hasHardBase = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
                            if (!hasHardBase && !baseState.canBeReplaced()) {
                                minePos = terrainPair.basePos();
                                isTerrainBase = true;
                                if (this.terrainPlace.getValue() && !this.pendingTerrainPlacements.contains(minePos)) {
                                    this.pendingTerrainPlacements.add(minePos);
                                }
                            } else if (!surroundState.canBeReplaced()) {
                                minePos = terrainPair.surroundPos();
                            }
                        }
                        if (minePos == null) {
                            int targetFloorY = Mth.floor((double)target.player().getY());
                            List<BlockPos> insideCandidates = HoleUtils.getInsidePositions((Entity)target.player()).stream().filter(blockPos -> blockPos.getY() <= targetFloorY).filter(blockPos -> !AutoMineModule.mc.level.getBlockState(blockPos).canBeReplaced()).filter(blockPos -> !this.isRaytrace() || WorldUtils.canSeeBlock(blockPos)).toList();
                            minePos = !insideCandidates.isEmpty() ? insideCandidates.get(0) : target.position();
                            if (minePos != null && this.isRaytrace() && !WorldUtils.canSeeBlock(minePos)) {
                                minePos = null;
                            }
                        }
                        if (minePos == null) {
                            return;
                        }
                        if (primaryAction != null && minePos.equals(primaryAction.getPosition())) {
                            return;
                        }
                        if (!(primaryAction == null || primaryAction.getPriority() != 0 || primaryAction.isTerrainBase() || !speedMineModule.isTargetSurroundPosition(primaryAction.getPosition(), target.player()) || (grimHeadPos != null && minePos.equals(grimHeadPos)))) {
                            return;
                        }
                        speedMineModule.handle(minePos, 0);
                        if (isTerrainBase && (action = speedMineModule.getPrimary()) != null && action.getPosition().equals(minePos)) {
                            action.setTerrainBase(true);
                        }
                    }
                }
            }
        }
    }

    private BlockPos getHeadTarget(Player player, SpeedMineModule speedMineModule) {
        if (player == null || AutoMineModule.mc.level == null) {
            return null;
        }
        int floorY = Mth.floor((double)player.getY());
        int headY = floorY + 2;
        SpeedMineModule.Action action = speedMineModule.getPrimary();
        if (action != null && action.isInstantMine() && action.getPosition().getY() == headY && speedMineModule.isTargetSurroundPosition(action.getPosition(), player)) {
            return action.getPosition();
        }
        for (BlockPos blockPos : HoleUtils.getInsidePositions((Entity)player)) {
            BlockPos headPos = new BlockPos(blockPos.getX(), headY, blockPos.getZ());
            if (!speedMineModule.isValid(headPos) || speedMineModule.isOutOfRange(headPos) || this.isRaytrace() && !WorldUtils.canSeeBlock(headPos) || AutoMineModule.mc.level.getBlockState(headPos).canBeReplaced()) continue;
            return headPos;
        }
        BlockPos blockPos = player.blockPosition().above(2);
        if (!(!speedMineModule.isValid(blockPos) || speedMineModule.isOutOfRange(blockPos) || this.isRaytrace() && !WorldUtils.canSeeBlock(blockPos) || AutoMineModule.mc.level.getBlockState(blockPos).canBeReplaced())) {
            return blockPos;
        }
        return null;
    }

    private boolean tickUpFallback(SpeedMineModule speedMineModule) {
        if (!(this.priority.getValue().equalsIgnoreCase("Leg") && this.raytrace.getValue() && this.upFallback.getValue())) {
            this.resetUpCombo();
            return false;
        }
        SpeedMineModule.Target target = speedMineModule.getTarget();
        Player targetPlayer = target != null ? target.player() : null;
        if (targetPlayer == null || EntityUtils.isGhost((Entity)targetPlayer)) {
            this.resetUpCombo();
            return false;
        }
        if (!HoleUtils.isPlayerInHole(targetPlayer)) {
            this.resetUpCombo();
            return false;
        }
        HashSet<BlockPos> feetPositions = HoleUtils.getFeetPositions(targetPlayer, true, false, true);
        if (this.upComboPos != null) {
            if (!feetPositions.contains(this.upComboPos.below())) {
                this.resetUpCombo();
            } else {
                if (this.isPhased(targetPlayer) || this.elsewhereDealsMoreDamage(speedMineModule, targetPlayer, this.upComboPos)) {
                    this.resetUpCombo();
                    return false;
                }
                return this.tickUpComboCycle(speedMineModule, this.upComboPos);
            }
        }
        boolean hasSolidLeg = feetPositions.stream().anyMatch(blockPos -> !AutoMineModule.mc.level.getBlockState(blockPos).canBeReplaced());
        boolean hasVisibleLeg = feetPositions.stream().anyMatch(blockPos -> this.isMinableLegCandidate((BlockPos)blockPos, speedMineModule) && WorldUtils.canSeeBlock(blockPos));
        if (!hasSolidLeg || hasVisibleLeg) {
            return false;
        }
        if (this.isPhased(targetPlayer)) {
            return false;
        }
        BlockPos selectedUpPos = null;
        for (BlockPos feetPos : feetPositions) {
            BlockPos abovePos = feetPos.above();
            if (speedMineModule.isOutOfRange(abovePos) || !WorldUtils.canSeeBlock(abovePos)) continue;
            selectedUpPos = abovePos;
            break;
        }
        if (selectedUpPos == null) {
            return false;
        }
        this.upComboPos = selectedUpPos;
        return this.tickUpComboCycle(speedMineModule, selectedUpPos);
    }

    private boolean elsewhereDealsMoreDamage(SpeedMineModule speedMineModule, Player player, BlockPos blockPos) {
        AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        if (autoCrystalModule == null) {
            return false;
        }
        float bestPlaceDamage = autoCrystalModule.getBestPlaceDamage();
        if (bestPlaceDamage <= 0.0f) {
            return false;
        }
        BlockPos crystalBasePos = blockPos.above();
        float comboDamage = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, autoCrystalModule.extrapolation.getValue().intValue()), crystalBasePos, blockPos, autoCrystalModule.ignoreTerrain.getValue());
        return bestPlaceDamage > comboDamage;
    }

    private boolean tickUpComboCycle(SpeedMineModule speedMineModule, BlockPos blockPos) {
        if (AutoMineModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            this.placeUpComboObsidian(speedMineModule, blockPos);
            return true;
        }
        if (!speedMineModule.isMining(blockPos) && speedMineModule.isValid(blockPos) && !speedMineModule.isOutOfRange(blockPos)) {
            speedMineModule.handle(blockPos, 0);
        }
        return true;
    }

    private void placeUpComboObsidian(SpeedMineModule speedMineModule, BlockPos blockPos) {
        if (!WorldUtils.isPlaceable(blockPos)) {
            return;
        }
        AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        String switchMode = autoCrystalModule != null ? autoCrystalModule.autoSwitch.getValue() : "Silent";
        boolean altSwap = switchMode.equalsIgnoreCase("AltSwap");
        int blockSlot = InventoryUtils.find(Items.OBSIDIAN, 0, altSwap ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
        if (blockSlot == -1) {
            blockSlot = InventoryUtils.findHardestBlock(0, 8);
        }
        if (blockSlot == -1) {
            return;
        }
        Direction direction = WorldUtils.getDirection(blockPos, false);
        if (direction == null) {
            direction = WorldUtils.getClosestDirection(blockPos, true);
        }
        if (direction == null) {
            return;
        }
        int oldSlot = AutoMineModule.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot(switchMode, blockSlot, oldSlot);
        WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.upFallbackRotate.getValue(), true, speedMineModule.render.getValue().equalsIgnoreCase("Both") || speedMineModule.render.getValue().equalsIgnoreCase("Fill"));
        InventoryUtils.switchBack(switchMode, blockSlot, oldSlot);
    }

    private double scoreFeetBlockPos(Set<BlockPos> avoidPositions, SpeedMineModule speedMineModule, TerrainPair terrainPair, BlockPos blockPos) {
        double score = this.avoidSharing.getValue() && avoidPositions.contains(blockPos) ? 100000.0 : 0.0;
        if (this.isRaytrace() && !WorldUtils.canSeeBlock(blockPos)) {
            score += 500000.0;
        } else if (WorldUtils.canSeeBlock(blockPos)) {
            score -= 50000.0;
        }
        if (speedMineModule.sixB.getValue() && AutoMineModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK) {
            score -= 200000.0;
        }
        if (this.terrain.getValue() && terrainPair != null && blockPos.equals((Object)terrainPair.surroundPos())) {
            score -= 100000.0;
        } else {
            BlockPos belowPos = blockPos.below();
            BlockState belowState = AutoMineModule.mc.level.getBlockState(belowPos);
            boolean hasHardBase = belowState.getBlock() == Blocks.OBSIDIAN || belowState.getBlock() == Blocks.BEDROCK;
            boolean miningBase = speedMineModule.isMining(belowPos);
            if (miningBase) {
                score -= 50000.0;
            } else if (hasHardBase) {
                score -= 20000.0;
            } else if (this.terrain.getValue()) {
                score += 50000.0;
            }
        }
        return score + AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos));
    }

    public record TerrainPair(Direction direction, BlockPos surroundPos, BlockPos basePos, double score) {
    }
}

