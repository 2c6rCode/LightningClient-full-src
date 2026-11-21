/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMineEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.system.ThreadExecutor;
import lightning.utils.system.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Blocker", description="Places blocks to stop enemies from placing crystals.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class BlockerModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", 0, 0, 20);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting feet = new BooleanSetting("Feet", "Places on feet level blocks.", true);
    public BooleanSetting head = new BooleanSetting("Head", "Places on head level blocks.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private final CopyOnWriteArrayList<Position> targetPositions = new CopyOnWriteArrayList();
    private Mine mine = null;
    private int ticks = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        Runnable runnable = () -> {
            if (BlockerModule.mc.player == null || BlockerModule.mc.level == null) {
                return;
            }
            SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
            if (this.mine != null && speedMineModule.getPrimary() != null && this.mine.position().equals((Object)speedMineModule.getPrimary().getPosition()) || speedMineModule.getSecondary() != null && this.mine.position().equals((Object)speedMineModule.getSecondary().getPosition())) {
                this.mine = null;
                return;
            }
            int n = 0;
            if (this.ticks < this.delay.getValue().intValue()) {
                ++this.ticks;
                return;
            }
            HashSet<BlockPos> hashSet = HoleUtils.getFeetPositions((Player)BlockerModule.mc.player, true, false, false);
            List<BlockPos> list = HoleUtils.getInsidePositions((Entity)BlockerModule.mc.player);
            if (this.mine != null && this.mine.timer().hasTimeElapsed(Float.valueOf(Math.max(this.mine.breakTime() - 200.0f, 0.0f)))) {
                BlockPos blockPos = this.mine.position();
                if (this.mine.type() == MineType.FEET && this.feet.getValue()) {
                    if (hashSet.contains(this.mine.position())) {
                        this.targetPositions.add(new Position(blockPos, blockPos.above()));
                        for (Direction direction : Direction.values()) {
                            if (!direction.getAxis().isHorizontal()) continue;
                            this.targetPositions.add(new Position(blockPos, blockPos.relative(direction)));
                        }
                    }
                    this.mine = null;
                } else if ((this.mine.type() == MineType.HEAD || this.mine.type() == MineType.SIDE) && this.head.getValue()) {
                    if (this.mine.type() == MineType.HEAD && list.contains(this.mine.position().below().below()) || this.mine.type() == MineType.SIDE && hashSet.contains(this.mine.position().below())) {
                        this.targetPositions.add(new Position(blockPos, blockPos.above()));
                    }
                    this.mine = null;
                }
            }
            this.targetPositions.removeIf(position -> !WorldUtils.isPlaceable(position.position()));
            this.targetPositions.removeIf(position -> BlockerModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)position.position())) > Mth.square((double)this.range.getValue().doubleValue()));
            this.targetPositions.removeIf(position -> !hashSet.contains(position.original()) && !hashSet.contains(position.original().below()) && !list.contains(position.original().below().below()));
            if (this.targetPositions.isEmpty()) {
                return;
            }
            int n2 = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int n3 = BlockerModule.mc.player.getInventory().getSelectedSlot();
            if (n2 == -1) {
                this.targetPositions.clear();
                return;
            }
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), n2, n3);
                for (Position position2 : new ArrayList<Position>(this.targetPositions)) {
                    Direction direction;
                    if (n >= this.limit.getValue().intValue()) break;
                    direction = WorldUtils.getDirection(position2.position(), null, this.strictDirection.getValue());
                    if (direction == null) continue;
                    WorldUtils.placeBlock(position2.position(), direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
                    this.targetPositions.remove(position2);
                    ++n;
                }
                InventoryUtils.switchBack(this.autoSwitch.getValue(), n2, n3);
            }
            finally {
                Night.ROTATION_MANAGER.endBatchRotation();
            }
            this.ticks = 0;
        };
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerMineEvent playerMineEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (BlockerModule.mc.player == null || BlockerModule.mc.level == null) {
            return;
        }
        if (this.mine != null && this.mine.position().equals((Object)playerMineEvent.getPosition())) {
            return;
        }
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        if (speedMineModule.getPrimary() != null && playerMineEvent.getPosition().equals((Object)speedMineModule.getPrimary().getPosition()) || speedMineModule.getSecondary() != null && playerMineEvent.getPosition().equals((Object)speedMineModule.getSecondary().getPosition())) {
            return;
        }
        Entity entity = BlockerModule.mc.level.getEntity(playerMineEvent.getActorID());
        if (entity == BlockerModule.mc.player) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return;
        }
        HashSet<BlockPos> hashSet = HoleUtils.getFeetPositions((Player)BlockerModule.mc.player, true, false, false);
        List<BlockPos> list = HoleUtils.getInsidePositions((Entity)BlockerModule.mc.player);
        if (this.feet.getValue() && hashSet.contains(playerMineEvent.getPosition())) {
            this.mine = new Mine(playerMineEvent.getPosition(), new Timer(), WorldUtils.getBreakTime(player, BlockerModule.mc.level.getBlockState(playerMineEvent.getPosition())), MineType.FEET);
            return;
        }
        if (this.head.getValue()) {
            if (hashSet.contains(playerMineEvent.getPosition().below())) {
                this.mine = new Mine(playerMineEvent.getPosition(), new Timer(), WorldUtils.getBreakTime(player, BlockerModule.mc.level.getBlockState(playerMineEvent.getPosition())), MineType.SIDE);
            }
            if (list.contains(playerMineEvent.getPosition().below().below())) {
                this.mine = new Mine(playerMineEvent.getPosition(), new Timer(), WorldUtils.getBreakTime(player, BlockerModule.mc.level.getBlockState(playerMineEvent.getPosition())), MineType.HEAD);
            }
        }
    }

    @Override
    public void onDisable() {
        this.targetPositions.clear();
    }

    @Override
    public String getMetaData() {
        if (this.targetPositions == null) {
            return "0";
        }
        return String.valueOf(this.targetPositions.size());
    }

    private record Mine(BlockPos position, Timer timer, float breakTime, MineType type) {
    }

    private static enum MineType {
        FEET,
        HEAD,
        SIDE;

    }

    private record Position(BlockPos original, BlockPos position) {
    }
}

