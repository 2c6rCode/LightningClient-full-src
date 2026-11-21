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
        double d = Math.max(this.range.getValue().doubleValue(), this.mineRange.getValue().doubleValue());
        double d2 = d * d;
        int n = (int)Math.ceil(d);
        BlockPos blockPos = localPlayer.blockPosition();
        BlockPos blockPos2 = null;
        double d3 = Double.MAX_VALUE;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    double d4;
                    BlockPos blockPos3 = blockPos.offset(i, j, k);
                    if (!clientLevel.getBlockState(blockPos3).is(Blocks.ENDER_CHEST) || (d4 = localPlayer.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos3))) > d2 || !(d4 < d3)) continue;
                    d3 = d4;
                    blockPos2 = blockPos3.immutable();
                }
            }
        }
        return blockPos2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void placeEnderChest(LocalPlayer localPlayer, ClientLevel clientLevel) {
        if (!this.placeTimer.hasTimeElapsed((long)this.delay.getValue().intValue() * 50L)) {
            return;
        }
        BlockPos blockPos = this.findBestPlacePosition(localPlayer, clientLevel);
        if (blockPos == null) {
            return;
        }
        int n = this.findEnderChestSlot(localPlayer);
        if (n == -1) {
            return;
        }
        int n2 = localPlayer.getInventory().getSelectedSlot();
        Night.ROTATION_MANAGER.beginBatchRotation();
        boolean bl = false;
        try {
            int n3;
            BlockPos blockPos2;
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, n2);
            bl = this.tryPlaceAt(blockPos);
            if (!bl && this.support.getValue() && this.isValidPlaceSpot(localPlayer, clientLevel, blockPos2 = blockPos.below()) && (n3 = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8)) != -1) {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), n3, n2);
                boolean bl2 = this.tryPlaceAt(blockPos2);
                if (bl2 && this.isValidPlaceSpot(localPlayer, clientLevel, blockPos)) {
                    InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, n2);
                    bl = this.tryPlaceAt(blockPos);
                }
            }
            InventoryUtils.switchBack(this.autoSwitch.getValue(), n, n2);
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
        this.placeTimer.reset();
        if (!bl) {
            return;
        }
        BlockPos blockPos3 = clientLevel.getBlockState(blockPos).is(Blocks.ENDER_CHEST) ? blockPos.immutable() : this.findExistingEnderChest(localPlayer, clientLevel);
        BlockPos blockPos4 = blockPos3;
        if (blockPos3 == null) {
            return;
        }
        this.enderChestPos = blockPos3;
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
        Direction direction2 = direction == null ? Direction.UP : direction;
        Vec3 vec3 = this.getHitVec(blockPos, direction2);
        boolean bl = !this.rotate.getValue().equalsIgnoreCase("None");
        boolean bl2 = bl;
        if (bl) {
            float[] fArray = RotationUtils.getRotations(vec3);
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(fArray[0], fArray[1]);
            } else {
                Night.ROTATION_MANAGER.silentRotate(fArray[0], fArray[1]);
            }
        }
        multiPlayerGameMode.startDestroyBlock(blockPos, direction2);
        if (this.swing.getValue()) {
            localPlayer.swing(InteractionHand.MAIN_HAND);
        }
        this.retryTimer.reset();
        this.mineTimer.reset();
    }

    private BlockPos findBestPlacePosition(LocalPlayer localPlayer, ClientLevel clientLevel) {
        List<BlockPos> list = this.getPlaceCandidates(localPlayer);
        list.removeIf(blockPos -> !this.isValidPlaceSpot(localPlayer, clientLevel, (BlockPos)blockPos));
        list.sort(Comparator.comparingDouble(blockPos -> localPlayer.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos))));
        return list.isEmpty() ? null : list.getFirst().immutable();
    }

    private List<BlockPos> getPlaceCandidates(LocalPlayer localPlayer) {
        LinkedHashSet<BlockPos> linkedHashSet = new LinkedHashSet<BlockPos>();
        BlockPos blockPos = localPlayer.blockPosition();
        Direction direction = localPlayer.getDirection();
        linkedHashSet.add(blockPos.relative(direction));
        linkedHashSet.add(blockPos.relative(direction).below());
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            linkedHashSet.add(blockPos.relative(direction2));
        }
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            linkedHashSet.add(blockPos.relative(direction2).below());
        }
        linkedHashSet.add(blockPos.below());
        return new ArrayList<BlockPos>(linkedHashSet);
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

    private boolean canReach(LocalPlayer localPlayer, ClientLevel clientLevel, BlockPos blockPos, double d, double d2) {
        Vec3 vec3;
        Vec3 vec32 = localPlayer.getEyePosition();
        double d3 = vec32.distanceTo(vec3 = Vec3.atCenterOf((Vec3i)blockPos));
        if (d3 <= d && this.hasLineOfSight(localPlayer, clientLevel, vec32, vec3)) {
            return true;
        }
        return d3 <= d2;
    }

    private boolean hasLineOfSight(LocalPlayer localPlayer, ClientLevel clientLevel, Vec3 vec3, Vec3 vec32) {
        BlockHitResult blockHitResult = clientLevel.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)localPlayer));
        return blockHitResult.getType() == HitResult.Type.MISS;
    }

    private Direction getMineSide(LocalPlayer localPlayer, ClientLevel clientLevel, BlockPos blockPos) {
        Vec3 vec3 = localPlayer.getEyePosition();
        Direction direction = null;
        double d = Double.MAX_VALUE;
        for (Direction direction2 : Direction.values()) {
            Vec3 vec32 = this.getHitVec(blockPos, direction2);
            double d2 = vec3.distanceTo(vec32);
            if (d2 >= d || this.strictDirection.getValue() && !this.hasLineOfSight(localPlayer, clientLevel, vec3, vec32)) continue;
            d = d2;
            direction = direction2;
        }
        return direction;
    }

    private Vec3 getHitVec(BlockPos blockPos, Direction direction) {
        return Vec3.atCenterOf((Vec3i)blockPos).add((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
    }

    private int findEnderChestSlot(LocalPlayer localPlayer) {
        int n = this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8;
        int n2 = localPlayer.getInventory().getSelectedSlot();
        if (localPlayer.getInventory().getItem(n2).is(Items.ENDER_CHEST)) {
            return n2;
        }
        return InventoryUtils.find(Items.ENDER_CHEST, 0, n);
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

