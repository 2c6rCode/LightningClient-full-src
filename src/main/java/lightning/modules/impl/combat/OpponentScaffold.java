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
        List list = OpponentScaffold.mc.level.players();
        if (list == null || list.isEmpty()) {
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
            int n = InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int n2 = OpponentScaffold.mc.player.getInventory() != null ? OpponentScaffold.mc.player.getInventory().getSelectedSlot() : 0;
            int n3 = n2;
            if (n == -1) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("No blocks could be found in your hotbar.", this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            Target target = this.getTarget(list);
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
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, n2);
                ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
                for (BlockPos blockPos : this.positions) {
                    if (this.blocksPlaced >= this.limit.getValue().intValue()) break;
                    Direction direction = WorldUtils.getDirection(blockPos, arrayList, this.strictDirection.getValue());
                    if (direction == null) {
                        Direction direction2;
                        BlockPos blockPos2 = blockPos.offset(0, -1, 0);
                        if (!WorldUtils.isPlaceable(blockPos2) || (direction2 = WorldUtils.getDirection(blockPos2, arrayList, this.strictDirection.getValue())) == null) continue;
                        WorldUtils.placeBlock(blockPos2, direction2, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
                        arrayList.add(blockPos2);
                        ++this.blocksPlaced;
                        if (this.blocksPlaced >= this.limit.getValue().intValue()) break;
                        if (this.await.getValue() || (direction = WorldUtils.getDirection(blockPos, arrayList, this.strictDirection.getValue())) == null) continue;
                    }
                    WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
                    arrayList.add(blockPos);
                    ++this.blocksPlaced;
                }
                InventoryUtils.switchBack(this.autoSwitch.getValue(), n, n2);
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

    private Target getTarget(List<AbstractClientPlayer> list) {
        if (OpponentScaffold.mc.player == null) {
            return null;
        }
        AbstractClientPlayer object2 = null;
        double d = Double.MAX_VALUE;
        for (AbstractClientPlayer object3 : list) {
            double arrayList;
            if (object3 == OpponentScaffold.mc.player || object3.isInvisible() || Night.FRIEND_MANAGER.contains(object3.getName().getString()) || object3.getHealth() <= 0.0f || (arrayList = OpponentScaffold.mc.player.distanceToSqr((Entity)object3)) > (double)Mth.square((float)this.enemyRange.getValue().floatValue()) || !(arrayList < d)) continue;
            d = arrayList;
            object2 = object3;
        }
        if (object2 == null) {
            return null;
        }
        BlockPos blockPos = this.getTargetPosition((Player)object2);
        if (blockPos == null) {
            return null;
        }
        HitboxDesyncModule hitboxDesyncModule = Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        BlockPos blockPos2 = blockPos.below();
        boolean bl = hitboxDesyncModule != null && hitboxDesyncModule.isToggled() && !Boolean.TRUE.equals(hitboxDesyncModule.close.getValue());
        boolean bl2 = bl;
        if (WorldUtils.isPlaceable(blockPos2, bl)) {
            double distSq = OpponentScaffold.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos2));
            if (distSq <= Mth.square((double)this.range.getValue().doubleValue())) {
                arrayList.add(blockPos2);
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        return new Target((Player)object2, (List<BlockPos>)arrayList);
    }

    private BlockPos getTargetPosition(Player player) {
        if (player == null) {
            return null;
        }
        int n = this.extrapolation.getValue().intValue();
        if (n > 0) {
            double d = player.getX() - player.xo;
            double d2 = player.getZ() - player.zo;
            double d3 = player.getX() + d * (double)n;
            double d4 = player.getZ() + d2 * (double)n;
            return new BlockPos((int)Math.floor(d3), (int)Math.floor(player.getY()), (int)Math.floor(d4));
        }
        return PositionUtils.getFlooredPosition((Entity)player);
    }

    private record Target(Player player, List<BlockPos> positions) {
    }
}

