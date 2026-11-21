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
        BlockPos blockPos2;
        if (player == null || AutoMineModule.mc.level == null) {
            return null;
        }
        if (!this.logic.getValue().equalsIgnoreCase("Grim")) {
            return null;
        }
        if (!this.isTargetPhasedInBedrock(player)) {
            return null;
        }
        int n = Mth.floor((double)player.getY());
        int n2 = player.isVisuallyCrawling() || player.getBoundingBox().getYsize() <= 1.0 ? n + 1 : n + 2;
        SpeedMineModule.Action action = speedMineModule.getPrimary();
        if (action != null && action.isInstantMine() && action.getPosition().getY() == n2 && speedMineModule.isTargetSurroundPosition(action.getPosition(), player)) {
            return action.getPosition();
        }
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        for (BlockPos object : HoleUtils.getInsidePositions((Entity)player)) {
            BlockPos blockPos3 = new BlockPos(object.getX(), n2, object.getZ());
            if (arrayList.contains(blockPos3)) continue;
            arrayList.add(blockPos3);
        }
        BlockPos blockPos4 = player.blockPosition().above(n2 - n);
        if (!arrayList.contains(blockPos4)) {
            arrayList.add(blockPos4);
        }
        if (!arrayList.contains(blockPos2 = BlockPos.containing((double)player.getX(), (double)(player.getBoundingBox().maxY + 0.2), (double)player.getZ()))) {
            arrayList.add(blockPos2);
        }
        arrayList.sort(Comparator.comparingDouble(blockPos -> {
            double d = AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos));
            if (this.isRaytrace() && WorldUtils.canSeeBlock(blockPos)) {
                d -= 1000.0;
            }
            return d;
        }));
        for (BlockPos blockPos3 : arrayList) {
            BlockState blockState;
            if (speedMineModule.isOutOfRange(blockPos3) || this.isRaytrace() && !WorldUtils.canSeeBlock(blockPos3) || (blockState = AutoMineModule.mc.level.getBlockState(blockPos3)).canBeReplaced() || !WorldUtils.canBreak(blockPos3) || blockState.is(Blocks.COBWEB) || !speedMineModule.isValid(blockPos3)) continue;
            return blockPos3;
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
        BlockPos blockPos = BlockPos.containing((Position)AutoMineModule.mc.player.getEyePosition());
        if (WorldUtils.canBreak(blockPos) && !WorldUtils.isReplaceable(blockPos)) {
            return blockPos;
        }
        BlockPos blockPos2 = AutoMineModule.mc.player.blockPosition().above();
        if (WorldUtils.canBreak(blockPos2) && !WorldUtils.isReplaceable(blockPos2)) {
            return blockPos2;
        }
        return null;
    }

    public boolean isProtectedTerrainBase(BlockPos blockPos) {
        return this.terrain.getValue() && this.activeTerrainPair != null && blockPos != null && blockPos.equals((Object)this.activeTerrainPair.basePos()) && this.isRealTerrainBase(blockPos);
    }

    private boolean allSidesTerraformed(Player player, SpeedMineModule speedMineModule) {
        BlockPos blockPos = player.blockPosition();
        boolean bl = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            boolean bl2;
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockPos blockPos3 = blockPos2.below();
            if (speedMineModule.isOutOfRange(blockPos2) || speedMineModule.isOutOfRange(blockPos3)) continue;
            BlockState blockState2 = AutoMineModule.mc.level.getBlockState(blockPos2);
            if (blockState2.getBlock().defaultDestroyTime() < 0.0f && (!speedMineModule.sixB.getValue() || blockState2.getBlock() != Blocks.BEDROCK)) continue;
            bl = true;
            BlockState blockState3 = AutoMineModule.mc.level.getBlockState(blockPos3);
            boolean bl3 = bl2 = blockState3.getBlock() == Blocks.OBSIDIAN || blockState3.getBlock() == Blocks.BEDROCK;
            if (bl2) continue;
            return false;
        }
        return bl;
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
        UUID uUID = player.getUUID();
        BlockPos blockPos = HoleUtils.getTargetFeetPosition(player);
        if (this.activeTerrainPair != null && uUID.equals(this.activeTerrainTarget)) {
            BlockPos surroundPos = this.activeTerrainPair.surroundPos();
            BlockPos basePos = this.activeTerrainPair.basePos();
            boolean bl2 = !speedMineModule.isOutOfRange(surroundPos) && !speedMineModule.isOutOfRange(basePos);
            boolean bl3 = blockPos.distManhattan((Vec3i)surroundPos) <= 2;
            BlockState surroundState = AutoMineModule.mc.level.getBlockState(surroundPos);
            BlockState baseState = AutoMineModule.mc.level.getBlockState(basePos);
            boolean bl = surroundState.getBlock().defaultDestroyTime() >= 0.0f || speedMineModule.sixB.getValue() && surroundState.getBlock() == Blocks.BEDROCK || speedMineModule.isMining(surroundPos);
            boolean bl4 = baseState.getBlock().defaultDestroyTime() >= 0.0f || baseState.getBlock() == Blocks.BEDROCK || baseState.getBlock() == Blocks.OBSIDIAN || this.pendingTerrainPlacements.contains(basePos);
            if (bl2 && bl3 && bl && bl4) {
                return this.activeTerrainPair;
            }
        }
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        if (this.avoidSharing.getValue()) {
            hashSet.addAll(HoleUtils.getFeetPositions((Player)AutoMineModule.mc.player, true, false, true));
            for (BlockPos blockPos2 : HoleUtils.getInsidePositions((Entity)AutoMineModule.mc.player)) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    hashSet.add(blockPos2.relative(direction));
                }
            }
        }
        TerrainPair bestPair = null;
        double d = Double.MAX_VALUE;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos3 = blockPos.relative(direction);
            BlockPos blockPos4 = blockPos3.below();
            if (speedMineModule.isOutOfRange(blockPos3) || speedMineModule.isOutOfRange(blockPos4)) continue;
            BlockState blockState = AutoMineModule.mc.level.getBlockState(blockPos3);
            BlockState blockState2 = AutoMineModule.mc.level.getBlockState(blockPos4);
            if (blockState.getBlock().defaultDestroyTime() < 0.0f && !speedMineModule.isMining(blockPos3) || blockState2.getBlock().defaultDestroyTime() < 0.0f && blockState2.getBlock() != Blocks.BEDROCK && blockState2.getBlock() != Blocks.OBSIDIAN && !this.pendingTerrainPlacements.contains(blockPos4)) continue;
            double d2 = 0.0;
            if (this.avoidSharing.getValue() && (hashSet.contains(blockPos3) || hashSet.contains(blockPos4))) {
                d2 += 100000.0;
            }
            boolean bl = blockState2.getBlock() == Blocks.OBSIDIAN || blockState2.getBlock() == Blocks.BEDROCK;
            if (bl) {
                d2 -= 20000.0;
            } else if (!blockState2.canBeReplaced()) {
                d2 += (double)blockState2.getBlock().defaultDestroyTime() * 10.0;
            }
            if (!blockState.canBeReplaced()) {
                d2 -= 10000.0;
            }
            boolean bl7 = speedMineModule.isMining(blockPos3) || speedMineModule.isMining(blockPos4);
            if (bl7) {
                d2 -= 50000.0;
            }
            if (!((d2 += AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos3))) < d)) continue;
            d = d2;
            bestPair = new TerrainPair(direction, blockPos3, blockPos4, d2);
        }
        this.activeTerrainPair = bestPair;
        this.activeTerrainTarget = bestPair != null ? uUID : null;
        return bestPair;
    }

    public void placePendingTerrain() {
        int n;
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
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        for (BlockPos blockPos : new ArrayList<BlockPos>(this.pendingTerrainPlacements)) {
            if (speedMineModule.isOutOfRange(blockPos)) {
                arrayList.add(blockPos);
                continue;
            }
            BlockState blockState = AutoMineModule.mc.level.getBlockState(blockPos);
            if (blockState.getBlock() == Blocks.OBSIDIAN) {
                arrayList.add(blockPos);
                speedMineModule.dropPrimaryIfInvalid(blockPos2 -> !blockPos2.equals((Object)blockPos));
                speedMineModule.dropSecondaryIfInvalid(blockPos2 -> !blockPos2.equals((Object)blockPos));
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
            arrayList.add(blockPos);
            speedMineModule.dropPrimaryIfInvalid(blockPos2 -> !blockPos2.equals((Object)blockPos));
            speedMineModule.dropSecondaryIfInvalid(blockPos2 -> !blockPos2.equals((Object)blockPos));
        }
        arrayList.forEach(this.pendingTerrainPlacements::remove);
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
                SpeedMineModule.Target target2 = rawTarget == null || (this.ignoreNaked.getValue() && EntityUtils.isNaked(rawTarget.player())) ? null : rawTarget;
                BlockPos secondaryBlockPos = secondaryAction != null ? secondaryAction.getPosition() : null;
                int secPriority = secondaryAction != null ? secondaryAction.getPriority() : 0;
                if (!(primaryAction != null && primaryAction.getPriority() > 0 && !WorldUtils.isReplaceable(primaryAction.getPosition()) || secondaryBlockPos != null && secPriority > 0 && !WorldUtils.isReplaceable(secondaryBlockPos) || primaryAction != null && primaryAction.getPriority() == 0 && speedMineModule.sixB.getValue() && primaryAction.getState().getBlock() == Blocks.BEDROCK && target2 != null && speedMineModule.isTargetSurroundPosition(primaryAction.getPosition(), target2.player()))) {
                    if (speedMineModule.doubleMine.getValue()) {
                        if (!speedMineModule.isMineTimerReady()) {
                            return;
                        }
                        if (target2 == null) {
                            speedMineModule.setDoubleEngaged(false);
                        }
                        if (target2 != null) {
                            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
                            List<BlockPos> insideList = HoleUtils.getInsidePositions((Entity)target2.player());
                            arrayList.addAll(insideList);
                            HashSet<BlockPos> feetPositions = HoleUtils.getFeetPositions(target2.player(), true, false, true);
                            arrayList.addAll(feetPositions);
                            for (BlockPos feetBp : feetPositions) {
                                arrayList.add(feetBp.below());
                                for (Direction direction : Direction.Plane.HORIZONTAL) {
                                    arrayList.add(feetBp.relative(direction).below());
                                }
                            }
                            for (BlockPos insideBp : insideList) {
                                for (Direction direction : Direction.Plane.HORIZONTAL) {
                                    arrayList.add(insideBp.relative(direction));
                                    arrayList.add(insideBp.relative(direction).below());
                                }
                                arrayList.add(insideBp.above());
                                arrayList.add(insideBp.below());
                            }
                            TerrainPair terrainPair = this.terrain.getValue() ? this.getBestTerrainPair(target2.player()) : null;
                            if (terrainPair != null) {
                                arrayList.add(terrainPair.surroundPos());
                                arrayList.add(terrainPair.basePos());
                            }
                            BlockPos grimHeadTarget = this.logic.getValue().equalsIgnoreCase("Grim") ? this.getGrimBedrockHeadTarget(target2.player(), speedMineModule) : null;
                            if (grimHeadTarget != null) {
                                arrayList.add(grimHeadTarget);
                            }
                            if (primaryAction != null && primaryAction.isTerrainBase() && this.isRealTerrainBase(primaryAction.getPosition())) {
                                speedMineModule.dropPrimary();
                                primaryAction = null;
                            }
                            speedMineModule.setDoubleEngaged(true);
                            if (primaryAction != null && primaryAction.getPriority() == 0 && !primaryAction.isTerrainBase()) {
                                boolean bl = speedMineModule.isTargetSurroundPosition(primaryAction.getPosition(), target2.player());
                                if (!bl) {
                                    speedMineModule.dropPrimary();
                                    primaryAction = null;
                                } else if (!primaryAction.isInstantMine()) {
                                    BlockPos primPos = primaryAction.getPosition();
                                    boolean bl2 = HoleUtils.getInsidePositions((Entity)target2.player()).contains(primPos) && AutoMineModule.mc.level.getBlockState(primPos).canBeReplaced();
                                    if (!arrayList.contains(primPos) || bl2) {
                                        speedMineModule.dropPrimary();
                                        primaryAction = null;
                                    }
                                }
                            }
                            if (secondaryAction != null && secondaryAction.getPriority() == 0 && !secondaryAction.isHolding()) {
                                BlockPos secPos = secondaryAction.getPosition();
                                boolean bl = HoleUtils.getInsidePositions((Entity)target2.player()).contains(secPos) && AutoMineModule.mc.level.getBlockState(secPos).canBeReplaced();
                                if (!arrayList.contains(secPos) || bl) {
                                    speedMineModule.dropSecondary();
                                    secondaryAction = null;
                                }
                            }
                            TerrainPair finalTerrainPair = terrainPair;
                            Runnable runnable2 = () -> {
                                if (this.terrain.getValue() && finalTerrainPair != null) {
                                    BlockPos terrainBase = finalTerrainPair.basePos();
                                    BlockPos terrainSurround = finalTerrainPair.surroundPos();
                                    BlockState baseState = AutoMineModule.mc.level.getBlockState(terrainBase);
                                    BlockState surroundState = AutoMineModule.mc.level.getBlockState(terrainSurround);
                                    boolean bl = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
                                    if (!bl && this.terrainPlace.getValue() && !this.pendingTerrainPlacements.contains(terrainBase)) {
                                        this.pendingTerrainPlacements.add(terrainBase);
                                    }
                                    if (this.isPhased(target2.player())) {
                                        if (speedMineModule.primaryCamping()) {
                                            BlockPos campPos = speedMineModule.getPrimary().getPosition();
                                            if (!campPos.equals(terrainBase) && !campPos.equals(terrainSurround)) {
                                                return;
                                            }
                                        }
                                        boolean bl3 = speedMineModule.getPrimary() == null || speedMineModule.getSecondary() == null;
                                        if (!bl && !baseState.canBeReplaced()) {
                                            if (!speedMineModule.isMining(terrainBase) && bl3) {
                                                speedMineModule.handle(terrainBase, 0);
                                                SpeedMineModule.Action action = speedMineModule.getPrimary();
                                                if (action != null && action.getPosition().equals(terrainBase)) {
                                                    action.setTerrainBase(true);
                                                }
                                            }
                                        } else if (bl && !surroundState.canBeReplaced()) {
                                            SpeedMineModule.Action action = speedMineModule.getPrimary();
                                            boolean bl5 = action != null && action.getPosition().equals(terrainSurround);
                                            if (!bl5) {
                                                speedMineModule.handle(terrainSurround, 0);
                                                SpeedMineModule.Action action2 = speedMineModule.getPrimary();
                                                if (action2 != null && action2.getPosition().equals(terrainSurround)) {
                                                    action2.setTerrainSurround(true);
                                                }
                                            }
                                        }
                                    } else {
                                        if (!(bl || baseState.canBeReplaced() || speedMineModule.isMining(terrainBase))) {
                                            speedMineModule.handle(terrainBase, 0);
                                        }
                                        if (!surroundState.canBeReplaced() && !speedMineModule.isMining(terrainSurround)) {
                                            if (bl && speedMineModule.doubleMine.getValue()) {
                                                BlockPos candidate = HoleUtils.getFeetPositions(target2.player(), true, false, true).stream()
                                                    .filter(p -> !p.equals(terrainSurround))
                                                    .filter(p -> !AutoMineModule.mc.level.getBlockState(p).canBeReplaced())
                                                    .filter(p -> !speedMineModule.isMining(p) && !speedMineModule.isInvalid(p) && !speedMineModule.isOutOfRange(p))
                                                    .min(Comparator.comparingDouble(p -> AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)p))))
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
                            Runnable runnable3 = () -> {
                                block13: {
                                    SpeedMineModule.Action action = speedMineModule.getPrimary();
                                    boolean bl = action != null && speedMineModule.primaryCamping() && HoleUtils.getInsidePositions((Entity)target2.player()).contains(action.getPosition());
                                    if (speedMineModule.primaryCamping() && !bl || action != null && speedMineModule.sixB.getValue() && action.getState().getBlock() == Blocks.BEDROCK || HoleUtils.getInsidePositions((Entity)target2.player()).stream().filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced()).toList().size() >= 2 || (this.terrain.getValue() && action != null && action.isTerrainSurround() && !action.isStarted())) break block13;
                                    if (this.terrain.getValue() && finalTerrainPair != null) {
                                        BlockState baseState = AutoMineModule.mc.level.getBlockState(finalTerrainPair.basePos());
                                        boolean bl3 = baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK;
                                        if (!bl3) {
                                            return;
                                        }
                                        if ((speedMineModule.isMining(finalTerrainPair.basePos()) || speedMineModule.isMining(finalTerrainPair.surroundPos())) && speedMineModule.slotsFull()) {
                                            return;
                                        }
                                    }
                                    if (this.logic.getValue().equals("NCP")) {
                                        for (BlockPos feetPosCandidate : HoleUtils.getFeetPositions(target2.player(), true, false, true).stream().filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced()).filter(bp -> !this.isRaytrace() || WorldUtils.canSeeBlock(bp)).toList()) {
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
                                        List<BlockPos> list2 = HoleUtils.getFeetPositions(target2.player(), true, false, true).stream().filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced()).filter(bp -> !this.isRaytrace() || WorldUtils.canSeeBlock(bp)).sorted(Comparator.comparingDouble(arg_0 -> this.scoreFeetBlockPos(shareAvoid, speedMineModule, finalTerrainPair, arg_0))).toList();
                                        boolean bl5 = bl;
                                        for (BlockPos targetFeetPos : list2) {
                                            if (!bl5 && speedMineModule.slotsFull()) break;
                                            if (!speedMineModule.isMining(targetFeetPos)) {
                                                if (speedMineModule.isInvalid(targetFeetPos) || speedMineModule.isOutOfRange(targetFeetPos)) continue;
                                                boolean bl6 = speedMineModule.sixB.getValue() && AutoMineModule.mc.level.getBlockState(targetFeetPos).getBlock() == Blocks.BEDROCK;
                                                if (bl6 && speedMineModule.doubleMine.getValue() && speedMineModule.getPrimary() == null) {
                                                    BlockPos secondFeet = list2.stream()
                                                        .filter(p -> !p.equals(targetFeetPos))
                                                        .filter(p -> AutoMineModule.mc.level.getBlockState(p).getBlock() != Blocks.BEDROCK)
                                                        .filter(p -> !speedMineModule.isMining(p) && !speedMineModule.isInvalid(p) && !speedMineModule.isOutOfRange(p))
                                                        .findFirst().orElse(null);
                                                    if (secondFeet != null) {
                                                        speedMineModule.handle(secondFeet, 0);
                                                    }
                                                }
                                                speedMineModule.handle(targetFeetPos, 0);
                                                bl5 = false;
                                            }
                                            if (bl5 || !speedMineModule.slotsFull()) continue;
                                            break;
                                        }
                                    }
                                }
                            };
                            Runnable phaseRunnable = () -> {
                                SpeedMineModule.Action action = speedMineModule.getPrimary();
                                if (!this.terrain.getValue() || action == null || !action.isTerrainSurround() || action.isStarted()) {
                                    int targetFloorY = Mth.floor((double)target2.player().getY());
                                    for (BlockPos phaseInsidePos : HoleUtils.getInsidePositions((Entity)target2.player()).stream().filter(bp -> !AutoMineModule.mc.level.getBlockState(bp).canBeReplaced()).filter(bp -> !this.isRaytrace() || WorldUtils.canSeeBlock(bp)).sorted(Comparator.comparingDouble(bp -> bp.getY() > targetFloorY ? 1.0 : 0.0)).toList()) {
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
                                headTarget = this.getHeadTarget(target2.player(), speedMineModule);
                            }
                            if (headTarget == null && this.logic.getValue().equalsIgnoreCase("Grim")) {
                                headTarget = this.getGrimBedrockHeadTarget(target2.player(), speedMineModule);
                            }
                            if (headTarget != null && !speedMineModule.isMining(headTarget)) {
                                speedMineModule.handle(headTarget, 0);
                            }
                            if (headTarget == null) {
                                phaseRunnable.run();
                            }
                            if (headTarget == null && this.terrain.getValue()) {
                                runnable2.run();
                            }
                            if (headTarget == null) {
                                runnable3.run();
                            }
                        }
                    } else {
                        SpeedMineModule.Action action;
                        TerrainPair terrainPair;
                        BlockPos blockPos10 = null;
                        BlockPos blockPos11 = null;
                        boolean bl = false;
                        if (target2 == null) {
                            return;
                        }
                        if (primaryAction != null && primaryAction.getPriority() == 0 && !primaryAction.isTerrainBase()) {
                            boolean bl5;
                            boolean bl6 = speedMineModule.isTargetSurroundPosition(primaryAction.getPosition(), target2.player());
                            boolean bl7 = bl5 = HoleUtils.getInsidePositions((Entity)target2.player()).contains(primaryAction.getPosition()) && AutoMineModule.mc.level.getBlockState(primaryAction.getPosition()).canBeReplaced();
                            if (!bl6) {
                                speedMineModule.dropPrimary();
                                primaryAction = null;
                            } else if (bl5) {
                                speedMineModule.dropPrimary();
                                primaryAction = null;
                            } else if (speedMineModule.sixB.getValue() && primaryAction.getState().getBlock() == Blocks.BEDROCK) {
                                return;
                            }
                        }
                        if (this.priority.getValue().equalsIgnoreCase("Head")) {
                            blockPos10 = this.getHeadTarget(target2.player(), speedMineModule);
                        }
                        if (blockPos10 == null && this.logic.getValue().equalsIgnoreCase("Grim")) {
                            blockPos10 = blockPos11 = this.getGrimBedrockHeadTarget(target2.player(), speedMineModule);
                        }
                        if (blockPos10 == null && this.terrain.getValue() && (terrainPair = this.getBestTerrainPair(target2.player())) != null) {
                            boolean bl8;
                            BlockState blockState = AutoMineModule.mc.level.getBlockState(terrainPair.basePos());
                            BlockState blockState2 = AutoMineModule.mc.level.getBlockState(terrainPair.surroundPos());
                            boolean bl9 = bl8 = blockState.getBlock() == Blocks.OBSIDIAN || blockState.getBlock() == Blocks.BEDROCK;
                            if (!bl8 && !blockState.canBeReplaced()) {
                                blockPos10 = terrainPair.basePos();
                                bl = true;
                                if (this.terrainPlace.getValue() && !this.pendingTerrainPlacements.contains(blockPos10)) {
                                    this.pendingTerrainPlacements.add(blockPos10);
                                }
                            } else if (!blockState2.canBeReplaced()) {
                                blockPos10 = terrainPair.surroundPos();
                            }
                        }
                        if (blockPos10 == null) {
                            int n3 = Mth.floor((double)target2.player().getY());
                            List<BlockPos> list = HoleUtils.getInsidePositions((Entity)target2.player()).stream().filter(blockPos -> blockPos.getY() <= n3).filter(blockPos -> !AutoMineModule.mc.level.getBlockState(blockPos).canBeReplaced()).filter(blockPos -> !this.isRaytrace() || WorldUtils.canSeeBlock(blockPos)).toList();
                            BlockPos blockPos12 = blockPos10 = !list.isEmpty() ? list.get(0) : target2.position();
                            if (blockPos10 != null && this.isRaytrace() && !WorldUtils.canSeeBlock(blockPos10)) {
                                blockPos10 = null;
                            }
                        }
                        if (blockPos10 == null) {
                            return;
                        }
                        if (primaryAction != null && blockPos10.equals(primaryAction.getPosition())) {
                            return;
                        }
                        if (!(primaryAction == null || primaryAction.getPriority() != 0 || primaryAction.isTerrainBase() || !speedMineModule.isTargetSurroundPosition(primaryAction.getPosition(), target2.player()) || (blockPos11 != null && blockPos10.equals(blockPos11)))) {
                            return;
                        }
                        speedMineModule.handle(blockPos10, 0);
                        if (bl && (action = speedMineModule.getPrimary()) != null && action.getPosition().equals(blockPos10)) {
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
        int n = Mth.floor((double)player.getY());
        int n2 = n + 2;
        SpeedMineModule.Action action = speedMineModule.getPrimary();
        if (action != null && action.isInstantMine() && action.getPosition().getY() == n2 && speedMineModule.isTargetSurroundPosition(action.getPosition(), player)) {
            return action.getPosition();
        }
        for (BlockPos blockPos : HoleUtils.getInsidePositions((Entity)player)) {
            BlockPos blockPos2 = new BlockPos(blockPos.getX(), n2, blockPos.getZ());
            if (!speedMineModule.isValid(blockPos2) || speedMineModule.isOutOfRange(blockPos2) || this.isRaytrace() && !WorldUtils.canSeeBlock(blockPos2) || AutoMineModule.mc.level.getBlockState(blockPos2).canBeReplaced()) continue;
            return blockPos2;
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
        Player player = target != null ? target.player() : null;
        Player player2 = player;
        if (player == null || EntityUtils.isGhost((Entity)player)) {
            this.resetUpCombo();
            return false;
        }
        if (!HoleUtils.isPlayerInHole(player)) {
            this.resetUpCombo();
            return false;
        }
        HashSet<BlockPos> hashSet = HoleUtils.getFeetPositions(player, true, false, true);
        if (this.upComboPos != null) {
            if (!hashSet.contains(this.upComboPos.below())) {
                this.resetUpCombo();
            } else {
                if (this.isPhased(player) || this.elsewhereDealsMoreDamage(speedMineModule, player, this.upComboPos)) {
                    this.resetUpCombo();
                    return false;
                }
                return this.tickUpComboCycle(speedMineModule, this.upComboPos);
            }
        }
        boolean bl = hashSet.stream().anyMatch(blockPos -> !AutoMineModule.mc.level.getBlockState(blockPos).canBeReplaced());
        boolean bl2 = hashSet.stream().anyMatch(blockPos -> this.isMinableLegCandidate((BlockPos)blockPos, speedMineModule) && WorldUtils.canSeeBlock(blockPos));
        if (!bl || bl2) {
            return false;
        }
        if (this.isPhased(player)) {
            return false;
        }
        BlockPos blockPos2 = null;
        for (BlockPos blockPos3 : hashSet) {
            BlockPos blockPos4 = blockPos3.above();
            if (speedMineModule.isOutOfRange(blockPos4) || !WorldUtils.canSeeBlock(blockPos4)) continue;
            blockPos2 = blockPos4;
            break;
        }
        if (blockPos2 == null) {
            return false;
        }
        this.upComboPos = blockPos2;
        return this.tickUpComboCycle(speedMineModule, blockPos2);
    }

    private boolean elsewhereDealsMoreDamage(SpeedMineModule speedMineModule, Player player, BlockPos blockPos) {
        AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        if (autoCrystalModule == null) {
            return false;
        }
        float f = autoCrystalModule.getBestPlaceDamage();
        if (f <= 0.0f) {
            return false;
        }
        BlockPos blockPos2 = blockPos.above();
        float f2 = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, autoCrystalModule.extrapolation.getValue().intValue()), blockPos2, blockPos, autoCrystalModule.ignoreTerrain.getValue());
        return f > f2;
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
        String string = autoCrystalModule != null ? autoCrystalModule.autoSwitch.getValue() : "Silent";
        boolean bl = string.equalsIgnoreCase("AltSwap");
        int n = InventoryUtils.find(Items.OBSIDIAN, 0, bl ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
        if (n == -1) {
            n = InventoryUtils.findHardestBlock(0, 8);
        }
        if (n == -1) {
            return;
        }
        Direction direction = WorldUtils.getDirection(blockPos, false);
        if (direction == null) {
            direction = WorldUtils.getClosestDirection(blockPos, true);
        }
        if (direction == null) {
            return;
        }
        int n2 = AutoMineModule.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot(string, n, n2);
        WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.upFallbackRotate.getValue(), true, speedMineModule.render.getValue().equalsIgnoreCase("Both") || speedMineModule.render.getValue().equalsIgnoreCase("Fill"));
        InventoryUtils.switchBack(string, n, n2);
    }

    private double scoreFeetBlockPos(Set set, SpeedMineModule speedMineModule, TerrainPair terrainPair, BlockPos blockPos) {
        double d;
        double d2 = d = this.avoidSharing.getValue() && set.contains(blockPos) ? 100000.0 : 0.0;
        if (this.isRaytrace() && !WorldUtils.canSeeBlock(blockPos)) {
            d += 500000.0;
        } else if (WorldUtils.canSeeBlock(blockPos)) {
            d -= 50000.0;
        }
        if (speedMineModule.sixB.getValue() && AutoMineModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK) {
            d -= 200000.0;
        }
        if (this.terrain.getValue() && terrainPair != null && blockPos.equals((Object)terrainPair.surroundPos())) {
            d -= 100000.0;
        } else {
            BlockPos blockPos2 = blockPos.below();
            BlockState blockState = AutoMineModule.mc.level.getBlockState(blockPos2);
            boolean bl = blockState.getBlock() == Blocks.OBSIDIAN || blockState.getBlock() == Blocks.BEDROCK;
            boolean bl2 = speedMineModule.isMining(blockPos2);
            if (bl2) {
                d -= 50000.0;
            } else if (bl) {
                d -= 20000.0;
            } else if (this.terrain.getValue()) {
                d += 50000.0;
            }
        }
        return d + AutoMineModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos));
    }

    public record TerrainPair(Direction direction, BlockPos surroundPos, BlockPos basePos, double score) {
    }
}

