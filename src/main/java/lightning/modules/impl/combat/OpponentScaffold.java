/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.HitboxDesyncModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.system.ThreadExecutor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="OpponentScaffold", description="Automatically places blocks under non-friended players to prevent them from falling.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class OpponentScaffold
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", 0, 0, 20);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which blocks will be placed at.", 5.0, 0.0, 12.0);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", Float.valueOf(10.0f), Float.valueOf(0.0f), Float.valueOf(20.0f));
    public NumberSetting extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to predict where they will move.", 0, 0, 20);
    public BooleanSetting await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Whether or not you should rotate when you place blocks.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private List<BlockPos> positions = new ArrayList<BlockPos>();
    private int ticks = 0;
    private int blocksPlaced = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (OpponentScaffold.mc.player == null || OpponentScaffold.mc.level == null) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        List players = OpponentScaffold.mc.level.players();
        if (players == null || players.isEmpty()) {
            return;
        }
        Runnable runnable = () -> {
            if (OpponentScaffold.mc.player == null || OpponentScaffold.mc.level == null) {
                return;
            }
            this.blocksPlaced = 0;
            if (this.ticks < this.delay.getValue().intValue()) {
                ++this.ticks;
                return;
            }
            if (this.autoSwitch.getValue().equalsIgnoreCase("None") && (OpponentScaffold.mc.player.getMainHandItem() == null || !(OpponentScaffold.mc.player.getMainHandItem().getItem() instanceof BlockItem))) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("You are currently not holding any blocks.", this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            int blockSlot = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int oldSlot = OpponentScaffold.mc.player.getInventory() != null ? OpponentScaffold.mc.player.getInventory().getSelectedSlot() : 0;
            if (blockSlot == -1) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("No blocks could be found in your hotbar.", this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            Target target = this.getTarget(players);
            if (target == null) {
                if (this.selfDisable.getValue()) {
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            this.positions = target.positions();
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), blockSlot, oldSlot);
                ArrayList<BlockPos> placedPositions = new ArrayList<BlockPos>();
                for (BlockPos blockPos : this.positions) {
                    if (this.blocksPlaced >= this.limit.getValue().intValue()) break;
                    Direction direction = WorldUtils.getDirection(blockPos, placedPositions, this.strictDirection.getValue());
                    if (direction == null) {
                        Direction supportDir;
                        BlockPos supportPos = blockPos.offset(0, -1, 0);
                        if (!WorldUtils.isPlaceable(supportPos) || (supportDir = WorldUtils.getDirection(supportPos, placedPositions, this.strictDirection.getValue())) == null) continue;
                        WorldUtils.placeBlock(supportPos, supportDir, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
                        placedPositions.add(supportPos);
                        ++this.blocksPlaced;
                        if (this.blocksPlaced >= this.limit.getValue().intValue()) break;
                        if (this.await.getValue() || (direction = WorldUtils.getDirection(blockPos, placedPositions, this.strictDirection.getValue())) == null) continue;
                    }
                    WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
                    placedPositions.add(blockPos);
                    ++this.blocksPlaced;
                }
                InventoryUtils.switchBack(this.autoSwitch.getValue(), blockSlot, oldSlot);
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

    @Override
    public void onEnable() {
        if (OpponentScaffold.mc.player == null || OpponentScaffold.mc.level == null) {
            this.setToggled(false);
        }
    }

    @Override
    public void onDisable() {
        this.positions = new ArrayList<BlockPos>();
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }

    private Target getTarget(List<AbstractClientPlayer> players) {
        if (OpponentScaffold.mc.player == null) {
            return null;
        }
        AbstractClientPlayer bestTarget = null;
        double bestDistSq = Double.MAX_VALUE;
        for (AbstractClientPlayer candidate : players) {
            double distSq;
            if (candidate == OpponentScaffold.mc.player || candidate.isInvisible() || Night.FRIEND_MANAGER.contains(candidate.getName().getString()) || candidate.getHealth() <= 0.0f || (distSq = OpponentScaffold.mc.player.distanceToSqr((Entity)candidate)) > (double)Mth.square((float)this.enemyRange.getValue().floatValue()) || !(distSq < bestDistSq)) continue;
            bestDistSq = distSq;
            bestTarget = candidate;
        }
        if (bestTarget == null) {
            return null;
        }
        BlockPos blockPos = this.getTargetPosition((Player)bestTarget);
        if (blockPos == null) {
            return null;
        }
        HitboxDesyncModule hitboxDesyncModule = Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
        ArrayList<BlockPos> validPositions = new ArrayList<BlockPos>();
        BlockPos belowPos = blockPos.below();
        boolean ignoreDesync = hitboxDesyncModule != null && hitboxDesyncModule.isToggled() && !Boolean.TRUE.equals(hitboxDesyncModule.close.getValue());
        if (WorldUtils.isPlaceable(belowPos, ignoreDesync)) {
            double distSq = OpponentScaffold.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)belowPos));
            if (distSq <= Mth.square((double)this.range.getValue().doubleValue())) {
                validPositions.add(belowPos);
            }
        }
        if (validPositions.isEmpty()) {
            return null;
        }
        return new Target((Player)bestTarget, (List<BlockPos>)validPositions);
    }

    private BlockPos getTargetPosition(Player player) {
        if (player == null) {
            return null;
        }
        int ticks = this.extrapolation.getValue().intValue();
        if (ticks > 0) {
            double velX = player.getX() - player.xo;
            double velZ = player.getZ() - player.zo;
            double predX = player.getX() + velX * (double)ticks;
            double predZ = player.getZ() + velZ * (double)ticks;
            return new BlockPos((int)Math.floor(predX), (int)Math.floor(player.getY()), (int)Math.floor(predZ));
        }
        return PositionUtils.getFlooredPosition((Entity)player);
    }

    private record Target(Player player, List<BlockPos> positions) {
    }
}

