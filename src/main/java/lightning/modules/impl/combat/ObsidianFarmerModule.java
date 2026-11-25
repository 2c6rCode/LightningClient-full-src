/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.Timer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="ObsidianFarmer", description="Places and mines ender chests using SpeedMine.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class ObsidianFarmerModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing and mining blocks.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places and mines using directions that face you.", false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which blocks will be placed.", 5.0, 1.0, 6.0);
    public NumberSetting mineRange = new NumberSetting("MineRange", "The maximum range at which ender chests will be mined.", 5.0, 1.0, 8.0);
    public NumberSetting wallsRange = new NumberSetting("WallsRange", "The maximum range through walls for mining.", 3.5, 1.0, 8.0);
    public NumberSetting delay = new NumberSetting("Delay", "The delay in ticks between placements.", 2, 0, 20);
    public NumberSetting mineDelay = new NumberSetting("MineDelay", "The delay in ticks before mining.", 1, 0, 20);
    public NumberSetting retryDelay = new NumberSetting("RetryDelay", "The delay in ticks before retrying a failed mine attempt.", 1, 1, 20);
    public BooleanSetting requirePacketMine = new BooleanSetting("RequirePacketMine", "Only mines when SpeedMine is enabled.", true);
    public BooleanSetting support = new BooleanSetting("Support", "Places a supporting block under the ender chest if needed.", true);
    public BooleanSetting swing = new BooleanSetting("Swing", "Swings your hand when starting to break blocks.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places and mines blocks while eating.", true);
    public BooleanSetting onlyOnGround = new BooleanSetting("OnlyGround", "Only works while on ground.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render place and mine positions.", true);
    private final Timer placeTimer = new Timer();
    private final Timer mineTimer = new Timer();
    private final Timer retryTimer = new Timer();
    private BlockPos enderChestPos;
    private Phase phase = Phase.Place;

    @Override
    public void onEnable() {
        this.enderChestPos = null;
        this.phase = Phase.Place;
        this.placeTimer.reset();
        this.mineTimer.reset();
        this.retryTimer.reset();
    }

    @Override
    public void onDisable() {
        this.enderChestPos = null;
        this.phase = Phase.Place;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (this.getNull()) {
            return;
        }
        LocalPlayer localPlayer = ObsidianFarmerModule.mc.player;
        ClientLevel clientLevel = ObsidianFarmerModule.mc.level;
        MultiPlayerGameMode multiPlayerGameMode = ObsidianFarmerModule.mc.gameMode;
        if (localPlayer == null || clientLevel == null || multiPlayerGameMode == null) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        if (this.onlyOnGround.getValue() && !localPlayer.onGround()) {
            return;
        }
        this.updateCurrentEnderChest(localPlayer, clientLevel);
        if (this.enderChestPos != null && clientLevel.getBlockState(this.enderChestPos).is(Blocks.ENDER_CHEST)) {
            this.phase = Phase.Mine;
            if (this.requirePacketMine.getValue() && !this.isPacketMineActive()) {
                return;
            }
            this.targetEnderChest(localPlayer, clientLevel, multiPlayerGameMode, this.enderChestPos);
            return;
        }
        this.phase = Phase.Place;
        this.enderChestPos = null;
        this.placeEnderChest(localPlayer, clientLevel);
    }

    private boolean isPacketMineActive() {
        if (Night.MODULE_MANAGER == null) {
            return false;
        }
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        return speedMineModule != null && speedMineModule.isToggled();
    }

    private void updateCurrentEnderChest(LocalPlayer localPlayer, ClientLevel clientLevel) {
        if (this.enderChestPos != null) {
            if (!clientLevel.getBlockState(this.enderChestPos).is(Blocks.ENDER_CHEST)) {
                this.enderChestPos = null;
            }
            return;
        }
        this.enderChestPos = this.findExistingEnderChest(localPlayer, clientLevel);
    }

    private BlockPos findExistingEnderChest(LocalPlayer localPlayer, ClientLevel clientLevel) {
        double maxRange = Math.max(this.range.getValue().doubleValue(), this.mineRange.getValue().doubleValue());
        double maxRangeSq = maxRange * maxRange;
        int rangeBlocks = (int)Math.ceil(maxRange);
        BlockPos playerPos = localPlayer.blockPosition();
        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int i = -rangeBlocks; i <= rangeBlocks; ++i) {
            for (int j = -rangeBlocks; j <= rangeBlocks; ++j) {
                for (int k = -rangeBlocks; k <= rangeBlocks; ++k) {
                    double distSq;
                    BlockPos pos = playerPos.offset(i, j, k);
                    if (!clientLevel.getBlockState(pos).is(Blocks.ENDER_CHEST) || (distSq = localPlayer.distanceToSqr(Vec3.atCenterOf((Vec3i)pos))) > maxRangeSq || !(distSq < bestDistSq)) continue;
                    bestDistSq = distSq;
                    bestPos = pos.immutable();
                }
            }
        }
        return bestPos;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void placeEnderChest(LocalPlayer localPlayer, ClientLevel clientLevel) {
        if (!this.placeTimer.hasTimeElapsed((long)this.delay.getValue().intValue() * 50L)) {
            return;
        }
        BlockPos placePos = this.findBestPlacePosition(localPlayer, clientLevel);
        if (placePos == null) {
            return;
        }
        int echestSlot = this.findEnderChestSlot(localPlayer);
        if (echestSlot == -1) {
            return;
        }
        int oldSlot = localPlayer.getInventory().getSelectedSlot();
        Night.ROTATION_MANAGER.beginBatchRotation();
        boolean placed = false;
        try {
            int supportSlot;
            BlockPos supportPos;
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), echestSlot, oldSlot);
            placed = this.tryPlaceAt(placePos);
            if (!placed && this.support.getValue() && this.isValidPlaceSpot(localPlayer, clientLevel, supportPos = placePos.below()) && (supportSlot = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8)) != -1) {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), supportSlot, oldSlot);
                boolean supportPlaced = this.tryPlaceAt(supportPos);
                if (supportPlaced && this.isValidPlaceSpot(localPlayer, clientLevel, placePos)) {
                    InventoryUtils.switchSlot(this.autoSwitch.getValue(), echestSlot, oldSlot);
                    placed = this.tryPlaceAt(placePos);
                }
            }
            InventoryUtils.switchBack(this.autoSwitch.getValue(), echestSlot, oldSlot);
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
        this.placeTimer.reset();
        if (!placed) {
            return;
        }
        BlockPos foundPos = clientLevel.getBlockState(placePos).is(Blocks.ENDER_CHEST) ? placePos.immutable() : this.findExistingEnderChest(localPlayer, clientLevel);
        if (foundPos == null) {
            return;
        }
        this.enderChestPos = foundPos;
        this.phase = Phase.Mine;
        this.mineTimer.reset();
        this.retryTimer.reset();
    }

    private boolean tryPlaceAt(BlockPos blockPos) {
        Direction direction = WorldUtils.getDirection(blockPos, this.strictDirection.getValue());
        if (direction != null) {
            return WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        }
        if (this.airPlace.getValue()) {
            WorldUtils.airPlaceBlock(blockPos, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
            return true;
        }
        return false;
    }

    private void targetEnderChest(LocalPlayer localPlayer, ClientLevel clientLevel, MultiPlayerGameMode multiPlayerGameMode, BlockPos blockPos) {
        if (!this.mineTimer.hasTimeElapsed((long)this.mineDelay.getValue().intValue() * 50L)) {
            return;
        }
        if (!this.retryTimer.hasTimeElapsed((long)this.retryDelay.getValue().intValue() * 50L)) {
            return;
        }
        if (!this.canTargetEnderChest(localPlayer, clientLevel, blockPos)) {
            this.enderChestPos = null;
            this.phase = Phase.Place;
            return;
        }
        Direction direction = this.getMineSide(localPlayer, clientLevel, blockPos);
        if (this.strictDirection.getValue() && direction == null) {
            return;
        }
        Direction mineSide = direction == null ? Direction.UP : direction;
        Vec3 hitVec = this.getHitVec(blockPos, mineSide);
        boolean shouldRotate = !this.rotate.getValue().equalsIgnoreCase("None");
        if (shouldRotate) {
            float[] rotations = RotationUtils.getRotations(hitVec);
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
            } else {
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
            }
        }
        multiPlayerGameMode.startDestroyBlock(blockPos, mineSide);
        if (this.swing.getValue()) {
            localPlayer.swing(InteractionHand.MAIN_HAND);
        }
        this.retryTimer.reset();
        this.mineTimer.reset();
    }

    private BlockPos findBestPlacePosition(LocalPlayer localPlayer, ClientLevel clientLevel) {
        List<BlockPos> candidates = this.getPlaceCandidates(localPlayer);
        candidates.removeIf(blockPos -> !this.isValidPlaceSpot(localPlayer, clientLevel, (BlockPos)blockPos));
        candidates.sort(Comparator.comparingDouble(blockPos -> localPlayer.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos))));
        return candidates.isEmpty() ? null : candidates.getFirst().immutable();
    }

    private List<BlockPos> getPlaceCandidates(LocalPlayer localPlayer) {
        LinkedHashSet<BlockPos> candidates = new LinkedHashSet<BlockPos>();
        BlockPos playerPos = localPlayer.blockPosition();
        Direction facing = localPlayer.getDirection();
        candidates.add(playerPos.relative(facing));
        candidates.add(playerPos.relative(facing).below());
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            candidates.add(playerPos.relative(dir));
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            candidates.add(playerPos.relative(dir).below());
        }
        candidates.add(playerPos.below());
        return new ArrayList<BlockPos>(candidates);
    }

    private boolean isValidPlaceSpot(LocalPlayer localPlayer, ClientLevel clientLevel, BlockPos blockPos) {
        if (clientLevel.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        BlockState blockState = clientLevel.getBlockState(blockPos);
        if (!blockState.isAir() && !blockState.canBeReplaced()) {
            return false;
        }
        if (localPlayer.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > this.range.getValue().doubleValue() * this.range.getValue().doubleValue()) {
            return false;
        }
        AABB aABB = new AABB(blockPos);
        if (localPlayer.getBoundingBox().intersects(aABB)) {
            return false;
        }
        if (!clientLevel.getEntitiesOfClass(Entity.class, aABB, entity -> !(entity instanceof ItemEntity)).isEmpty()) {
            return false;
        }
        return this.airPlace.getValue() || WorldUtils.getDirection(blockPos, this.strictDirection.getValue()) != null;
    }

    private boolean canTargetEnderChest(LocalPlayer localPlayer, ClientLevel clientLevel, BlockPos blockPos) {
        if (!clientLevel.getBlockState(blockPos).is(Blocks.ENDER_CHEST)) {
            return false;
        }
        if (!this.canReach(localPlayer, clientLevel, blockPos, this.mineRange.getValue().doubleValue(), this.wallsRange.getValue().doubleValue())) {
            return false;
        }
        return !this.strictDirection.getValue() || this.getMineSide(localPlayer, clientLevel, blockPos) != null;
    }

    private boolean canReach(LocalPlayer localPlayer, ClientLevel clientLevel, BlockPos blockPos, double maxRange, double maxWallRange) {
        Vec3 targetCenter;
        Vec3 eyePos = localPlayer.getEyePosition();
        double dist = eyePos.distanceTo(targetCenter = Vec3.atCenterOf((Vec3i)blockPos));
        if (dist <= maxRange && this.hasLineOfSight(localPlayer, clientLevel, eyePos, targetCenter)) {
            return true;
        }
        return dist <= maxWallRange;
    }

    private boolean hasLineOfSight(LocalPlayer localPlayer, ClientLevel clientLevel, Vec3 start, Vec3 end) {
        BlockHitResult blockHitResult = clientLevel.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)localPlayer));
        return blockHitResult.getType() == HitResult.Type.MISS;
    }

    private Direction getMineSide(LocalPlayer localPlayer, ClientLevel clientLevel, BlockPos blockPos) {
        Vec3 eyePos = localPlayer.getEyePosition();
        Direction bestDir = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction dir : Direction.values()) {
            Vec3 hitVec = this.getHitVec(blockPos, dir);
            double dist = eyePos.distanceTo(hitVec);
            if (dist >= bestDist || this.strictDirection.getValue() && !this.hasLineOfSight(localPlayer, clientLevel, eyePos, hitVec)) continue;
            bestDist = dist;
            bestDir = dir;
        }
        return bestDir;
    }

    private Vec3 getHitVec(BlockPos blockPos, Direction direction) {
        return Vec3.atCenterOf((Vec3i)blockPos).add((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
    }

    private int findEnderChestSlot(LocalPlayer localPlayer) {
        int maxSlot = this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8;
        int currentSlot = localPlayer.getInventory().getSelectedSlot();
        if (localPlayer.getInventory().getItem(currentSlot).is(Items.ENDER_CHEST)) {
            return currentSlot;
        }
        return InventoryUtils.find(Items.ENDER_CHEST, 0, maxSlot);
    }

    @Override
    public String getMetaData() {
        if (this.enderChestPos != null) {
            return String.valueOf(ChatFormatting.GREEN) + this.phase.name() + String.valueOf(ChatFormatting.RESET);
        }
        return "Place";
    }

    private static enum Phase {
        Place,
        Mine;

    }
}

