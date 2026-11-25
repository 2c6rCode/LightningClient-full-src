/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.BannerBlock
 *  net.minecraft.world.level.block.BaseRailBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.CarpetBlock
 *  net.minecraft.world.level.block.FlowerBlock
 *  net.minecraft.world.level.block.FlowerPotBlock
 *  net.minecraft.world.level.block.LadderBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.MushroomBlock
 *  net.minecraft.world.level.block.PressurePlateBlock
 *  net.minecraft.world.level.block.ScaffoldingBlock
 *  net.minecraft.world.level.block.SignBlock
 *  net.minecraft.world.level.block.TallFlowerBlock
 *  net.minecraft.world.level.block.TorchBlock
 *  net.minecraft.world.level.block.TripWireBlock
 *  net.minecraft.world.level.block.TripWireHookBlock
 *  net.minecraft.world.level.block.VineBlock
 *  net.minecraft.world.level.block.WebBlock
 *  net.minecraft.world.level.block.piston.PistonBaseBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PistonHelper;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="PistonKick", description="Pushes players out of holes or solid blocks using pistons.", category=Module.Category.COMBAT)
public final class PistonKickModule
extends Module {
    public static PistonKickModule INSTANCE;
    public NumberSetting delay = new NumberSetting("Delay", "Delay between pushes (ticks)", 1, 0, 10);
    public NumberSetting pushDelay = new NumberSetting("PushDelay", "Duration to hold piston extended before retracting (ms)", 150, 50, 500);
    public ModeSetting power = new ModeSetting("Power", "How the piston is powered.", "Redstone", new String[]{"Redstone", "Lever"});
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Allow placing when not on ground.", false);
    public BooleanSetting oneTwelve = new BooleanSetting("1.12", "Place piston and power in the same tick.", true);
    public NumberSetting targetRange = new NumberSetting("TargetRange", "Target range.", Float.valueOf(5.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public BooleanSetting inAirTarget = new BooleanSetting("InAirTarget", "Target players in the air.", true);
    public ModeSetting rotate = new ModeSetting("Rotate", "Silently rotate when placing.", "Silent", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting breakCrystal = new BooleanSetting("BreakCrystal", "Break end crystals blocking piston path.", false);
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "Block place range.", Float.valueOf(5.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting breakRange = new NumberSetting("BreakRange", "Crystal break range.", new BooleanSetting.Visibility(this.breakCrystal, true), (Number)Float.valueOf(4.5f), (Number)Float.valueOf(1.0f), (Number)Float.valueOf(6.0f));
    public NumberSetting maxLocalDamage = new NumberSetting("MaxLocalDamage", "Max self-damage allowed to break a crystal.", new BooleanSetting.Visibility(this.breakCrystal, true), (Number)Float.valueOf(8.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(20.0f));
    public BooleanSetting safety = new BooleanSetting("Safety", "Never break a crystal that would kill you.", new BooleanSetting.Visibility(this.breakCrystal, true), true);
    public ModeSetting swapAction = new ModeSetting("Swap", "How to swap to required items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting consecutive = new BooleanSetting("Consecutive", "Keep targeting the same player consecutively.", true);
    public BooleanSetting merge = new BooleanSetting("Merge", "Burst piston and power packets together.", false);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pause while eating or using items.", false);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Use raytrace to verify line-of-sight before placing piston/redstone (required on servers that check raytrace).", false);
    public BooleanSetting render = new BooleanSetting("Render", "Render placed blocks using RendersModule.", true);
    private BlockPos targetPhasedPos;
    private BlockPos pistonPos;
    private BlockPos redstonePos;
    private BlockPos supportPos;
    private final List<BlockPos> supportPositions = new ArrayList<BlockPos>();
    private Direction pushDir;
    private Player currentTarget;
    private long lastActionMs = 0L;
    private long extendedSinceMs = 0L;
    private long leverTurnedOnMs = 0L;

    public PistonKickModule() {
        INSTANCE = this;
    }

    public static PistonKickModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        this.resetState();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    private void resetState() {
        this.targetPhasedPos = null;
        this.pistonPos = null;
        this.redstonePos = null;
        this.supportPos = null;
        this.supportPositions.clear();
        this.pushDir = null;
        this.currentTarget = null;
        this.extendedSinceMs = 0L;
        this.leverTurnedOnMs = 0L;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        int supportSlot;
        boolean hasSupport;
        if (PistonKickModule.mc.player == null || PistonKickModule.mc.level == null) {
            return;
        }
        if (this.pauseOnEat.getValue() && EntityUtils.isEating()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastActionMs < this.delay.getValue().longValue() * 50L) {
            return;
        }
        int pistonSlot = this.findPiston();
        int powerSlot = this.findPower();
        if (pistonSlot == -1 || powerSlot == -1) {
            this.resetState();
            return;
        }
        if (this.currentTarget != null) {
            if (!this.currentTarget.isAlive() || this.currentTarget.isSpectator() || PistonKickModule.mc.player.distanceToSqr((Entity)this.currentTarget) > (double)(this.targetRange.getValue().floatValue() * this.targetRange.getValue().floatValue())) {
                this.resetState();
            } else {
                List<BlockPos> phased = this.getPhasedSolidBlocks(this.currentTarget);
                if (phased.isEmpty()) {
                    this.resetState();
                } else if (this.targetPhasedPos == null || !phased.contains(this.targetPhasedPos)) {
                    this.targetPhasedPos = phased.get(0);
                }
            }
        }
        if (this.currentTarget == null) {
            List<Player> enemies = this.getEnemies();
            for (Player candidate : enemies) {
                List<BlockPos> enemyPhased;
                if (!this.inAirTarget.getValue() && !candidate.onGround() || (enemyPhased = this.getPhasedSolidBlocks(candidate)).isEmpty()) continue;
                this.currentTarget = candidate;
                this.targetPhasedPos = enemyPhased.get(0);
                break;
            }
        }
        if (this.currentTarget == null || this.targetPhasedPos == null) {
            this.resetState();
            return;
        }
        if (this.breakCrystal.getValue()) {
            this.breakNearbyCrystals(this.currentTarget);
        }
        if (this.pistonPos != null && this.pushDir != null) {
            boolean pistonPlaced = this.isPistonPlacedAndFacing(this.pistonPos, this.pushDir);
            boolean pistonExtended = this.isPistonExtended(this.pistonPos);
            boolean pistonPowered = this.isPistonPowered(this.pistonPos);
            boolean useLever = this.power.getValue().equalsIgnoreCase("Lever");
            if (pistonExtended) {
                long holdDelayMs;
                if (this.extendedSinceMs == 0L) {
                    this.extendedSinceMs = now;
                }
                if (now - this.extendedSinceMs < (holdDelayMs = Math.max(50L, this.pushDelay.getValue().longValue())) || this.leverTurnedOnMs > 0L && now - this.leverTurnedOnMs < 50L) {
                    this.lastActionMs = now;
                    return;
                }
                if (useLever && this.redstonePos != null && this.isLever(this.redstonePos) && this.isLeverPowered(this.redstonePos)) {
                    this.clickLever(this.redstonePos);
                    this.leverTurnedOnMs = 0L;
                    this.lastActionMs = now;
                    return;
                }
                if (useLever && this.redstonePos != null && !this.isLeverPowered(this.redstonePos) && pistonExtended) {
                    this.lastActionMs = now;
                    return;
                }
                List<BlockPos> phased = this.getPhasedSolidBlocks(this.currentTarget);
                if (phased.isEmpty()) {
                    this.resetState();
                    return;
                }
                this.pistonPos = null;
                this.redstonePos = null;
                this.supportPos = null;
                this.pushDir = null;
                this.extendedSinceMs = 0L;
                this.leverTurnedOnMs = 0L;
                this.lastActionMs = now;
            } else {
                this.extendedSinceMs = 0L;
            }
            if (pistonPlaced && pistonPowered && !pistonExtended) {
                this.lastActionMs = now;
                return;
            }
            boolean airPlaceCurrent = this.airPlace.getValue();
            if (!pistonPlaced && !PistonHelper.canPlace(this.pistonPos, null, airPlaceCurrent, true)) {
                hasSupport = !useLever && this.redstonePos != null && this.redstonePos.equals((Object)this.pistonPos.below()) && (PistonHelper.canPlace(this.redstonePos, null, airPlaceCurrent, true) || this.isPowerSource(this.redstonePos)) || !this.supportPositions.isEmpty() && this.supportPositions.stream().anyMatch(blockPos -> PistonHelper.canPlace(blockPos, null, airPlaceCurrent, true) || this.isSolidBlock((BlockPos)blockPos)) || this.supportPos != null && (PistonHelper.canPlace(this.supportPos, null, airPlaceCurrent, true) || this.isSolidBlock(this.supportPos));
                if (!hasSupport) {
                    this.resetState();
                }
            }
        }
        if (!(this.pistonPos != null && this.pushDir != null || this.searchPlacement(this.currentTarget))) {
            return;
        }
        if (this.pistonPos == null || this.pushDir == null) {
            return;
        }
        boolean airPlace = this.airPlace.getValue();
        boolean useLeverMode = this.power.getValue().equalsIgnoreCase("Lever");
        for (BlockPos supportBlockPos : this.supportPositions) {
            int candidateSupportSlot;
            if (this.isSolidBlock(supportBlockPos) || !PistonHelper.canPlace(supportBlockPos, null, airPlace, true) || (candidateSupportSlot = this.findSupportBlock()) == -1) continue;
            this.placeBlock(candidateSupportSlot, supportBlockPos, null);
            this.lastActionMs = now;
            if (this.oneTwelve.getValue() || this.merge.getValue()) continue;
            return;
        }
        if (this.supportPos != null && !this.isSolidBlock(this.supportPos) && !this.supportPositions.contains(this.supportPos) && PistonHelper.canPlace(this.supportPos, null, airPlace, true) && (supportSlot = this.findSupportBlock()) != -1) {
            this.placeBlock(supportSlot, this.supportPos, null);
            this.lastActionMs = now;
            if (!this.oneTwelve.getValue() && !this.merge.getValue()) {
                return;
            }
        }
        boolean powerBelowPiston = !useLeverMode && this.redstonePos != null && this.redstonePos.equals((Object)this.pistonPos.below());
        if (powerBelowPiston && !this.isPowerSource(this.redstonePos) && PistonHelper.canPlace(this.redstonePos, null, airPlace, true)) {
            this.placeBlock(powerSlot, this.redstonePos, null);
            this.lastActionMs = now;
            if (!this.oneTwelve.getValue() && !this.merge.getValue()) {
                return;
            }
        }
        boolean needPiston = !this.isPistonPlacedAndFacing(this.pistonPos, this.pushDir);
        if (needPiston && PistonHelper.canPlace(this.pistonPos, null, airPlace, true)) {
            this.placeBlock(pistonSlot, this.pistonPos, this.pushDir);
            this.lastActionMs = now;
            if (!this.oneTwelve.getValue() && !this.merge.getValue()) {
                return;
            }
        }
        if (this.redstonePos != null && !this.isPistonPowered(this.pistonPos) && !this.isPowerSource(this.redstonePos)) {
            boolean canPlacePower = !useLeverMode || this.isPistonPlacedAndFacing(this.pistonPos, this.pushDir) || airPlace;
            if (canPlacePower && PistonHelper.canPlace(this.redstonePos, null, airPlace, true)) {
                this.placeBlock(powerSlot, this.redstonePos, null);
                this.lastActionMs = now;
                if (useLeverMode) {
                    this.clickLever(this.redstonePos);
                    this.leverTurnedOnMs = now;
                }
            }
        } else if (useLeverMode && this.redstonePos != null && this.isLever(this.redstonePos) && !this.isLeverPowered(this.redstonePos) && !this.isPistonExtended(this.pistonPos) && this.extendedSinceMs == 0L) {
            this.clickLever(this.redstonePos);
            this.leverTurnedOnMs = now;
            this.lastActionMs = now;
        }
    }

    private boolean searchPlacement(Player player) {
        List<BlockPos> phasedBlocks = this.getPhasedSolidBlocks(player);
        if (phasedBlocks.isEmpty()) {
            return false;
        }
        if (phasedBlocks.size() >= 2) {
            Direction direction = PistonKickModule.mc.player.getDirection();
            Direction.Axis axis = direction.getAxis();
            phasedBlocks.sort((posA, posB) -> {
                boolean aligned = axis == Direction.Axis.Z && posA.getZ() != posB.getZ() || axis == Direction.Axis.X && posA.getX() != posB.getX();
                return aligned ? -1 : 1;
            });
        }
        for (BlockPos phasedPos : phasedBlocks) {
            if (!this.searchPlacementForBlock(player, phasedPos)) continue;
            this.targetPhasedPos = phasedPos;
            return true;
        }
        return false;
    }

    private boolean searchPlacementForBlock(Player player, BlockPos blockPos) {
        ArrayList<Direction> directions = new ArrayList<>();
        Direction playerDir = PistonKickModule.mc.player.getDirection();
        directions.add(playerDir);
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            if (direction2 == playerDir) continue;
            directions.add(direction2);
        }
        BlockPos targetAbove = blockPos.above();
        boolean airPlace = this.airPlace.getValue();
        boolean isLever = this.power.getValue().equalsIgnoreCase("Lever");
        for (Direction pushDirection : directions) {
            if (!this.isDestinationOpen(blockPos, pushDirection)) continue;
            BlockPos pistonCandidatePos = targetAbove.relative(pushDirection.getOpposite());
            if (!PistonKickModule.mc.level.getBlockState(targetAbove).isAir() && !PistonKickModule.mc.level.getBlockState(targetAbove).canBeReplaced() || !this.inRange(pistonCandidatePos)) continue;
            boolean isPlaced = this.isPistonPlacedAndFacing(pistonCandidatePos, pushDirection);
            boolean isAirOrReplaceable = PistonKickModule.mc.level.getBlockState(pistonCandidatePos).isAir() || PistonKickModule.mc.level.getBlockState(pistonCandidatePos).canBeReplaced();
            if (!isPlaced && !isAirOrReplaceable) continue;
            boolean canPlacePiston = isPlaced || PistonHelper.canPlace(pistonCandidatePos, null, airPlace, true);
            if (canPlacePiston) {
                if (this.isPistonPowered(pistonCandidatePos)) {
                    this.supportPos = null;
                    this.pistonPos = pistonCandidatePos;
                    this.redstonePos = null;
                    this.pushDir = pushDirection;
                    return true;
                }
                if (isLever) {
                    BlockPos pistonBelow = pistonCandidatePos.below();
                    boolean needPistonSupport = !airPlace && !this.isSolidBlock(pistonBelow);
                    List<LeverCandidate> candidates = new ArrayList<>();

                    BlockPos backPos = pistonCandidatePos.relative(pushDirection.getOpposite());
                    BlockPos backBelow = backPos.below();
                    List<BlockPos> backSupports = new ArrayList<>();
                    if (needPistonSupport) {
                        backSupports.add(pistonBelow);
                    }
                    if (!airPlace && !this.isSolidBlock(backBelow)) {
                        backSupports.add(backBelow);
                    }
                    candidates.add(new LeverCandidate(backPos, backSupports));

                    BlockPos ccwPos = pistonCandidatePos.relative(pushDirection.getCounterClockWise());
                    BlockPos ccwBelow = ccwPos.below();
                    List<BlockPos> ccwSupports = new ArrayList<>();
                    if (needPistonSupport) {
                        ccwSupports.add(pistonBelow);
                    }
                    if (!airPlace && !this.isSolidBlock(ccwBelow)) {
                        ccwSupports.add(ccwBelow);
                    }
                    candidates.add(new LeverCandidate(ccwPos, ccwSupports));
                    if (this.isSolidBlock(ccwPos)) {
                        BlockPos ccwAbove = ccwPos.above();
                        List<BlockPos> ccwAboveSupports = new ArrayList<>();
                        if (needPistonSupport) {
                            ccwAboveSupports.add(pistonBelow);
                        }
                        candidates.add(new LeverCandidate(ccwAbove, ccwAboveSupports));
                    }

                    BlockPos cwPos = pistonCandidatePos.relative(pushDirection.getClockWise());
                    BlockPos cwBelow = cwPos.below();
                    List<BlockPos> cwSupports = new ArrayList<>();
                    if (needPistonSupport) {
                        cwSupports.add(pistonBelow);
                    }
                    if (!airPlace && !this.isSolidBlock(cwBelow)) {
                        cwSupports.add(cwBelow);
                    }
                    candidates.add(new LeverCandidate(cwPos, cwSupports));
                    if (this.isSolidBlock(cwPos)) {
                        BlockPos cwAbove = cwPos.above();
                        List<BlockPos> cwAboveSupports = new ArrayList<>();
                        if (needPistonSupport) {
                            cwAboveSupports.add(pistonBelow);
                        }
                        candidates.add(new LeverCandidate(cwAbove, cwAboveSupports));
                    }

                    if (this.isSolidBlock(backPos)) {
                        BlockPos back2Pos = pistonCandidatePos.relative(pushDirection.getOpposite(), 2);
                        List<BlockPos> back2Supports = new ArrayList<>();
                        if (needPistonSupport) {
                            back2Supports.add(pistonBelow);
                        }
                        candidates.add(new LeverCandidate(back2Pos, back2Supports));

                        BlockPos backAbove = backPos.above();
                        List<BlockPos> backAboveSupports = new ArrayList<>();
                        if (needPistonSupport) {
                            backAboveSupports.add(pistonBelow);
                        }
                        candidates.add(new LeverCandidate(backAbove, backAboveSupports));
                    }

                    BlockPos backBelowPos = pistonCandidatePos.relative(pushDirection.getOpposite()).below();
                    List<BlockPos> backBelowSupports = new ArrayList<>();
                    if (needPistonSupport) {
                        backBelowSupports.add(pistonBelow);
                    }
                    candidates.add(new LeverCandidate(backBelowPos, backBelowSupports));

                    for (LeverCandidate candidate : candidates) {
                        BlockPos leverPos = candidate.leverPos();
                        if (leverPos.equals((Object)pistonCandidatePos.relative(pushDirection)) || leverPos.equals((Object)targetAbove) || leverPos.equals((Object)targetAbove.below()) || leverPos.equals((Object)blockPos.relative(pushDirection)) || leverPos.equals((Object)targetAbove.relative(pushDirection)) || !this.inRange(leverPos)) continue;
                        boolean canPlaceLever = this.isLever(leverPos) || PistonKickModule.mc.level.getBlockState(leverPos).isAir() || PistonKickModule.mc.level.getBlockState(leverPos).canBeReplaced();
                        if (!canPlaceLever) continue;
                        boolean valid = true;
                        for (BlockPos supportBlock : candidate.neededSupports()) {
                            if (supportBlock.equals((Object)targetAbove) || supportBlock.equals((Object)targetAbove.below()) || supportBlock.equals((Object)blockPos.relative(pushDirection)) || supportBlock.equals((Object)targetAbove.relative(pushDirection))) {
                                valid = false;
                                break;
                            }
                            if (!this.inRange(supportBlock)) {
                                valid = false;
                                break;
                            }
                            if (this.isSolidBlock(supportBlock) || PistonHelper.canPlace(supportBlock, null, airPlace, true)) continue;
                            valid = false;
                            break;
                        }
                        if (!valid) continue;
                        this.supportPositions.clear();
                        this.supportPositions.addAll(candidate.neededSupports());
                        this.supportPos = candidate.neededSupports().isEmpty() ? null : candidate.neededSupports().get(0);
                        this.pistonPos = pistonCandidatePos;
                        this.redstonePos = leverPos;
                        this.pushDir = pushDirection;
                        return true;
                    }
                } else {
                    BlockPos bestPower = this.findBestPowerPos(pistonCandidatePos, targetAbove, blockPos.relative(pushDirection), pushDirection);
                    if (bestPower != null) {
                        this.supportPos = null;
                        this.pistonPos = pistonCandidatePos;
                        this.redstonePos = bestPower;
                        this.pushDir = pushDirection;
                        return true;
                    }
                    for (Direction powerDir : new Direction[]{Direction.UP, pushDirection.getOpposite(), pushDirection.getClockWise(), pushDirection.getCounterClockWise(), Direction.DOWN}) {
                        BlockPos powerPos = pistonCandidatePos.relative(powerDir);
                        if (powerPos.equals((Object)pistonCandidatePos.relative(pushDirection)) || powerPos.equals((Object)targetAbove) || powerPos.equals((Object)targetAbove.below()) || powerPos.equals((Object)blockPos.relative(pushDirection)) || powerPos.equals((Object)targetAbove.relative(pushDirection)) || !this.inRange(powerPos) || !PistonKickModule.mc.level.getBlockState(powerPos).isAir() && !PistonKickModule.mc.level.getBlockState(powerPos).canBeReplaced() || !airPlace && !PistonHelper.canPlace(powerPos, null, false, true)) continue;
                        this.supportPos = null;
                        this.pistonPos = pistonCandidatePos;
                        this.redstonePos = powerPos;
                        this.pushDir = pushDirection;
                        return true;
                    }
                }
            }
            BlockPos pistonBelow = pistonCandidatePos.below();
            if (airPlace || !isAirOrReplaceable || !this.inRange(pistonBelow)) continue;
            if (isLever) {
                if (!PistonHelper.canPlace(pistonBelow, null, false, true) && !this.isSolidBlock(pistonBelow)) continue;
                List<LeverCandidate> candidates2 = new ArrayList<>();
                BlockPos backPos = pistonCandidatePos.relative(pushDirection.getOpposite());
                BlockPos backBelow = backPos.below();
                List<BlockPos> backSupports = new ArrayList<>();
                if (!this.isSolidBlock(pistonBelow)) {
                    backSupports.add(pistonBelow);
                }
                if (!this.isSolidBlock(backBelow)) {
                    backSupports.add(backBelow);
                }
                candidates2.add(new LeverCandidate(backPos, backSupports));
                BlockPos ccwPos = pistonCandidatePos.relative(pushDirection.getCounterClockWise());
                BlockPos ccwBelow = ccwPos.below();
                List<BlockPos> ccwSupports = new ArrayList<>();
                if (!this.isSolidBlock(pistonBelow)) {
                    ccwSupports.add(pistonBelow);
                }
                if (!this.isSolidBlock(ccwBelow)) {
                    ccwSupports.add(ccwBelow);
                }
                candidates2.add(new LeverCandidate(ccwPos, ccwSupports));
                BlockPos cwPos = pistonCandidatePos.relative(pushDirection.getClockWise());
                BlockPos cwBelow = cwPos.below();
                List<BlockPos> cwSupports = new ArrayList<>();
                if (!this.isSolidBlock(pistonBelow)) {
                    cwSupports.add(pistonBelow);
                }
                if (!this.isSolidBlock(cwBelow)) {
                    cwSupports.add(cwBelow);
                }
                candidates2.add(new LeverCandidate(cwPos, cwSupports));
                for (LeverCandidate candidate : candidates2) {
                    BlockPos leverPos = candidate.leverPos();
                    if (leverPos.equals((Object)pistonCandidatePos.relative(pushDirection)) || leverPos.equals((Object)targetAbove) || leverPos.equals((Object)targetAbove.below()) || leverPos.equals((Object)blockPos.relative(pushDirection)) || leverPos.equals((Object)targetAbove.relative(pushDirection)) || !this.inRange(leverPos)) continue;
                    boolean canPlaceLever = this.isLever(leverPos) || PistonKickModule.mc.level.getBlockState(leverPos).isAir() || PistonKickModule.mc.level.getBlockState(leverPos).canBeReplaced();
                    if (!canPlaceLever) continue;
                    boolean valid = true;
                    for (BlockPos supportBlock : candidate.neededSupports()) {
                        if (supportBlock.equals((Object)targetAbove) || supportBlock.equals((Object)targetAbove.below()) || supportBlock.equals((Object)blockPos.relative(pushDirection)) || supportBlock.equals((Object)targetAbove.relative(pushDirection))) {
                            valid = false;
                            break;
                        }
                        if (this.inRange(supportBlock) && (this.isSolidBlock(supportBlock) || PistonHelper.canPlace(supportBlock, null, false, true))) continue;
                        valid = false;
                        break;
                    }
                    if (!valid) continue;
                    this.supportPositions.clear();
                    this.supportPositions.addAll(candidate.neededSupports());
                    this.supportPos = candidate.neededSupports().isEmpty() ? null : candidate.neededSupports().get(0);
                    this.pistonPos = pistonCandidatePos;
                    this.redstonePos = leverPos;
                    this.pushDir = pushDirection;
                    return true;
                }
                continue;
            }
            if (!PistonHelper.canPlace(pistonBelow, null, false, true) && !this.isPowerSource(pistonBelow)) continue;
            this.supportPositions.clear();
            this.supportPos = null;
            this.pistonPos = pistonCandidatePos;
            this.redstonePos = pistonBelow;
            this.pushDir = pushDirection;
            return true;
        }
        return false;
    }

    private BlockPos findBestPowerPos(BlockPos pistonPos, BlockPos targetAbove, BlockPos pushDest, Direction pushDirection) {
        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;
        boolean airPlace = this.airPlace.getValue();
        Direction[] dirs = new Direction[]{Direction.DOWN, pushDirection.getOpposite(), pushDirection.getClockWise(), pushDirection.getCounterClockWise(), Direction.UP};
        for (Direction dir : dirs) {
            double distSq;
            BlockPos candidatePos = pistonPos.relative(dir);
            if (candidatePos.equals((Object)targetAbove) || candidatePos.equals((Object)targetAbove.below()) || candidatePos.equals((Object)pushDest) || candidatePos.equals((Object)pushDest.above()) || candidatePos.equals((Object)pistonPos.relative(pushDirection))) continue;
            if (this.isPowerSource(candidatePos)) {
                return candidatePos;
            }
            if (!PistonHelper.canPlace(candidatePos, null, airPlace, true) || !this.inRange(candidatePos) || !((distSq = PistonKickModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)candidatePos))) < bestDistSq)) continue;
            bestDistSq = distSq;
            bestPos = candidatePos;
        }
        return bestPos;
    }

    private void placeBlock(int slot, BlockPos blockPos, Direction pistonFacing) {
        if (slot == -1 || PistonKickModule.mc.player == null) {
            return;
        }
        Direction placeSide = this.strictDirection.getValue() ? WorldUtils.getDirection(blockPos, true) : PistonHelper.getPlaceSide(blockPos, direction -> true);
        if (placeSide == null) {
            if (this.airPlace.getValue()) {
                if (this.power.getValue().equalsIgnoreCase("Lever") && this.pistonPos != null && this.pushDir != null && blockPos.equals((Object)this.pistonPos.relative(this.pushDir.getOpposite()))) {
                    placeSide = this.pushDir;
                } else {
                    placeSide = PistonHelper.getClickSide(blockPos);
                    if (placeSide == null) {
                        placeSide = Direction.UP;
                    }
                }
            } else {
                return;
            }
        }
        int oldSlot = PistonKickModule.mc.player.getInventory().getSelectedSlot();
        this.doSwap(slot, oldSlot);
        if (this.shouldRotate()) {
            this.doRotate(blockPos, placeSide);
            if (pistonFacing != null) {
                this.snapPistonFacing(pistonFacing);
            }
        }
        PistonHelper.placeBlock(blockPos, placeSide, false);
        this.restoreSlot(slot, oldSlot);
        if (this.render.getValue()) {
            Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
            Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
        }
    }

    private void snapPistonFacing(Direction direction) {
        float yaw = switch (direction) {
            case Direction.SOUTH -> 180.0f;
            case Direction.NORTH -> 0.0f;
            case Direction.EAST -> 90.0f;
            case Direction.WEST -> -90.0f;
            default -> 0.0f;
        };
        Night.ROTATION_MANAGER.silentRotate(yaw, 0.0f);
    }

    private boolean isPistonPlacedAndFacing(BlockPos blockPos, Direction direction) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        BlockState blockState = PistonKickModule.mc.level.getBlockState(blockPos);
        if (blockState.getBlock() instanceof PistonBaseBlock) {
            return blockState.getValue((Property)PistonBaseBlock.FACING) == direction;
        }
        return false;
    }

    private boolean isPistonExtended(BlockPos blockPos) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        BlockState blockState = PistonKickModule.mc.level.getBlockState(blockPos);
        if (blockState.getBlock() instanceof PistonBaseBlock) {
            return (Boolean)blockState.getValue((Property)PistonBaseBlock.EXTENDED);
        }
        return false;
    }

    private boolean isPistonPowered(BlockPos blockPos) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        if (this.power.getValue().equalsIgnoreCase("Lever")) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = blockPos.relative(direction);
                if (!this.isLever(neighborPos) || !this.isLeverPowered(neighborPos)) continue;
                return true;
            }
            return PistonKickModule.mc.level.hasNeighborSignal(blockPos);
        }
        for (Direction direction : Direction.values()) {
            if (!this.isRedstone(blockPos.relative(direction))) continue;
            return true;
        }
        return false;
    }

    private boolean isPowerSource(BlockPos blockPos) {
        if (PistonKickModule.mc.level == null || blockPos == null) {
            return false;
        }
        Block block = PistonKickModule.mc.level.getBlockState(blockPos).getBlock();
        if (this.power.getValue().equalsIgnoreCase("Lever")) {
            return block == Blocks.LEVER;
        }
        return block == Blocks.REDSTONE_BLOCK || block == Blocks.REDSTONE_TORCH;
    }

    private boolean isRedstone(BlockPos blockPos) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        Block block = PistonKickModule.mc.level.getBlockState(blockPos).getBlock();
        return block == Blocks.REDSTONE_BLOCK || block == Blocks.REDSTONE_TORCH;
    }

    private boolean isLever(BlockPos blockPos) {
        if (PistonKickModule.mc.level == null || blockPos == null) {
            return false;
        }
        return PistonKickModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.LEVER;
    }

    private boolean isLeverPowered(BlockPos blockPos) {
        if (PistonKickModule.mc.level == null || blockPos == null) {
            return false;
        }
        BlockState blockState = PistonKickModule.mc.level.getBlockState(blockPos);
        if (blockState.getBlock() == Blocks.LEVER) {
            return (Boolean)blockState.getValue((Property)LeverBlock.POWERED);
        }
        return false;
    }

    private void clickLever(BlockPos blockPos) {
        if (PistonKickModule.mc.player == null || PistonKickModule.mc.level == null) {
            return;
        }
        Direction direction = PistonHelper.getClickSide(blockPos);
        if (direction == null) {
            direction = Direction.UP;
        }
        PistonHelper.clickBlock(blockPos, direction, this.shouldRotate());
    }

    private boolean isDestinationOpen(BlockPos blockPos, Direction direction) {
        if (PistonKickModule.mc.level == null) {
            return false;
        }
        BlockPos pushPos = blockPos.relative(direction);
        BlockPos pushAbovePos = pushPos.above();
        BlockState pushState = PistonKickModule.mc.level.getBlockState(pushPos);
        BlockState pushAboveState = PistonKickModule.mc.level.getBlockState(pushAbovePos);
        if (!pushState.isAir() && (!pushState.canBeReplaced() || pushState.blocksMotion() || this.isImmovable(pushState))) {
            return false;
        }
        return pushAboveState.isAir() || pushAboveState.canBeReplaced() && !pushAboveState.blocksMotion() && !this.isImmovable(pushAboveState);
    }

    private boolean isImmovable(BlockState blockState) {
        Block block = blockState.getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.CRYING_OBSIDIAN || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ENDER_CHEST || blockState.isSolid();
    }

    public List<BlockPos> getPhasedSolidBlocks(Player player) {
        ArrayList<BlockPos> solidBlocks = new ArrayList<BlockPos>();
        if (player == null || PistonKickModule.mc.level == null) {
            return solidBlocks;
        }
        List<BlockPos> phasedBlocks = EntityUtils.getPhasedBlocks((Entity)player);
        for (BlockPos pos : phasedBlocks) {
            BlockState blockState = PistonKickModule.mc.level.getBlockState(pos);
            if (!this.isHardPhasedBlock(blockState)) continue;
            solidBlocks.add(pos);
        }
        if (solidBlocks.isEmpty()) {
            BlockPos feetPos = BlockPos.containing((double)player.getX(), (double)(player.getY() + 0.1), (double)player.getZ());
            if (this.isHardPhasedBlock(PistonKickModule.mc.level.getBlockState(feetPos))) {
                solidBlocks.add(feetPos);
            } else {
                BlockPos blockPos = player.blockPosition();
                if (this.isHardPhasedBlock(PistonKickModule.mc.level.getBlockState(blockPos))) {
                    solidBlocks.add(blockPos);
                } else {
                    BlockPos abovePos = feetPos.above();
                    if (this.isHardPhasedBlock(PistonKickModule.mc.level.getBlockState(abovePos))) {
                        solidBlocks.add(abovePos);
                    }
                }
            }
        }
        return solidBlocks;
    }

    public BlockPos getPhasedSolidBlock(Player player) {
        List<BlockPos> blocks = this.getPhasedSolidBlocks(player);
        return blocks.isEmpty() ? null : blocks.get(0);
    }

    private boolean isDecoyOrPassableBlock(BlockState blockState) {
        if (blockState == null || blockState.isAir() || blockState.canBeReplaced()) {
            return true;
        }
        Block block = blockState.getBlock();
        return block instanceof WebBlock || block instanceof ButtonBlock || block instanceof MushroomBlock || block instanceof LadderBlock || block instanceof ScaffoldingBlock || block instanceof FlowerBlock || block instanceof FlowerPotBlock || block instanceof TallFlowerBlock || block instanceof VineBlock || block instanceof TorchBlock || block instanceof LeverBlock || block instanceof PressurePlateBlock || block instanceof SignBlock || block instanceof BannerBlock || block instanceof CarpetBlock || block instanceof TripWireBlock || block instanceof TripWireHookBlock || block instanceof BaseRailBlock || block == Blocks.COBWEB || block == Blocks.LADDER || block == Blocks.SCAFFOLDING || block == Blocks.VINE || block == Blocks.BROWN_MUSHROOM || block == Blocks.RED_MUSHROOM || block == Blocks.TORCH || block == Blocks.SOUL_TORCH || block == Blocks.REDSTONE_TORCH;
    }

    private boolean isHardPhasedBlock(BlockState blockState) {
        if (this.isDecoyOrPassableBlock(blockState)) {
            return false;
        }
        Block block = blockState.getBlock();
        if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.ENDER_CHEST || block == Blocks.RESPAWN_ANCHOR || block == Blocks.CRYING_OBSIDIAN || block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL || block == Blocks.REINFORCED_DEEPSLATE || block == Blocks.NETHERITE_BLOCK || block == Blocks.ANCIENT_DEBRIS) {
            return true;
        }
        return blockState.isSolid() && blockState.blocksMotion();
    }

    private boolean inRange(BlockPos blockPos) {
        double maxRange = this.placeRange.getValue().floatValue();
        return PistonKickModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= maxRange * maxRange;
    }

    private void breakNearbyCrystals(Player player) {
        double maxRange = this.breakRange.getValue().floatValue();
        AABB aABB = player.getBoundingBox().inflate(maxRange);
        PistonKickModule.mc.level.getEntitiesOfClass(EndCrystal.class, aABB, endCrystal -> endCrystal.isAlive() && PistonKickModule.mc.player.distanceToSqr((Entity)endCrystal) <= maxRange * maxRange).stream().min(Comparator.comparingDouble(endCrystal -> PistonKickModule.mc.player.distanceToSqr((Entity)endCrystal))).ifPresent(endCrystal -> {
            float selfDamage = DamageUtils.getCrystalDamage((Entity)PistonKickModule.mc.player, null, endCrystal, false);
            if (selfDamage > this.maxLocalDamage.getValue().floatValue()) {
                return;
            }
            if (this.safety.getValue() && selfDamage >= PistonKickModule.mc.player.getHealth() + PistonKickModule.mc.player.getAbsorptionAmount()) {
                return;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(endCrystal.getId()));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        });
    }

    private boolean shouldRotate() {
        return !this.rotate.getValue().equalsIgnoreCase("None");
    }

    private void doRotate(BlockPos blockPos, Direction direction) {
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
        float[] rotations = RotationUtils.getRotations(hitVec);
        Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
    }

    private void doSwap(int targetSlot, int oldSlot) {
        InventoryUtils.switchSlot(this.swapAction.getValue(), targetSlot, oldSlot);
    }

    private void restoreSlot(int targetSlot, int oldSlot) {
        InventoryUtils.switchBack(this.swapAction.getValue(), targetSlot, oldSlot);
    }

    private int findPower() {
        if (this.power.getValue().equalsIgnoreCase("Lever")) {
            return this.findBlock(Blocks.LEVER);
        }
        int slot = this.findBlock(Blocks.REDSTONE_BLOCK);
        if (slot != -1) {
            return slot;
        }
        return this.findBlock(Blocks.REDSTONE_TORCH);
    }

    private int findSupportBlock() {
        int slot = this.findBlock(Blocks.OBSIDIAN);
        if (slot != -1) {
            return slot;
        }
        slot = this.findBlock(Blocks.CRYING_OBSIDIAN);
        if (slot != -1) {
            return slot;
        }
        slot = this.findBlock(Blocks.ENDER_CHEST);
        if (slot != -1) {
            return slot;
        }
        slot = this.findBlock(Blocks.NETHERITE_BLOCK);
        if (slot != -1) {
            return slot;
        }
        slot = this.findBlock(Blocks.COBBLESTONE);
        if (slot != -1) {
            return slot;
        }
        return this.findPower();
    }

    private boolean isSolidBlock(BlockPos blockPos) {
        if (PistonKickModule.mc.level == null || blockPos == null) {
            return false;
        }
        BlockState blockState = PistonKickModule.mc.level.getBlockState(blockPos);
        return !blockState.isAir() && !blockState.canBeReplaced() && blockState.isSolid();
    }

    private int findPiston() {
        int slot = this.findBlock(Blocks.PISTON);
        if (slot != -1) {
            return slot;
        }
        return this.findBlock(Blocks.STICKY_PISTON);
    }

    private int findBlock(Block block) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            BlockItem blockItem;
            Item item;
            ItemStack itemStack = PistonKickModule.mc.player.getMainHandItem();
            if (!itemStack.isEmpty() && (item = itemStack.getItem()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() == block) {
                return PistonKickModule.mc.player.getInventory().getSelectedSlot();
            }
            return -1;
        }
        int maxSlot = this.swapAction.getValue().equalsIgnoreCase("AltSwap") || this.swapAction.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END;
        for (int i = 0; i <= maxSlot; ++i) {
            BlockItem blockItem;
            Item item;
            ItemStack itemStack = PistonKickModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || !((item = itemStack.getItem()) instanceof BlockItem) || (blockItem = (BlockItem)item).getBlock() != block) continue;
            return i;
        }
        return -1;
    }

    private List<Player> getEnemies() {
        double maxRange = this.targetRange.getValue().floatValue();
        return PistonKickModule.mc.level.players().stream().filter(abstractClientPlayer -> abstractClientPlayer != PistonKickModule.mc.player).filter(abstractClientPlayer -> !EntityUtils.isGhost((Entity)abstractClientPlayer)).filter(abstractClientPlayer -> !abstractClientPlayer.isSpectator() && abstractClientPlayer.isAlive()).filter(abstractClientPlayer -> !Night.FRIEND_MANAGER.contains(abstractClientPlayer.getName().getString())).filter(abstractClientPlayer -> PistonKickModule.mc.player.distanceToSqr((Entity)abstractClientPlayer) <= maxRange * maxRange).sorted(Comparator.comparingDouble(abstractClientPlayer -> PistonKickModule.mc.player.distanceToSqr((Entity)abstractClientPlayer))).collect(Collectors.toList());
    }

    private record LeverCandidate(BlockPos leverPos, List<BlockPos> neededSupports) {
    }
}

