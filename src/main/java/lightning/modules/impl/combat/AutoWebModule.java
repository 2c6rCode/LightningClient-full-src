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
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientRotationEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.ThreadExecutor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="AutoWeb", description="Automatically places webs on other people's feet.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class AutoWebModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting delay = new NumberSetting("Delay", "The amount of ticks that have to be waited for between placements.", 0, 0, 20);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", Float.valueOf(8.0f), Float.valueOf(0.0f), Float.valueOf(16.0f));
    public NumberSetting extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", 0, 0, 20);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Lets you place webs on air.", false);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting holeCheck = new BooleanSetting("HoleCheck", "Checks if the target is in a hole or not before placing.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private Player target = null;
    private BlockPos rotatePosition = null;
    private int ticks = 0;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        List players = AutoWebModule.mc.level.players();
        Runnable runnable = () -> {
            this.rotatePosition = null;
            if (this.ticks < this.delay.getValue().intValue()) {
                ++this.ticks;
                return;
            }
            if (this.autoSwitch.getValue().equalsIgnoreCase("None") && AutoWebModule.mc.player.getMainHandItem().getItem() != Items.COBWEB) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("You are currently not holding any cobwebs.", this.getName());
                    this.setToggled(false);
                }
                this.target = null;
                return;
            }
            int cobwebSlot = InventoryUtils.find(Items.COBWEB, 0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int oldSlot = AutoWebModule.mc.player.getInventory().getSelectedSlot();
            if (cobwebSlot == -1) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("No cobwebs could be found in your hotbar.", this.getName());
                    this.setToggled(false);
                }
                this.target = null;
                return;
            }
            this.target = this.getTarget(players);
            if (this.target == null) {
                if (this.selfDisable.getValue()) {
                    this.setToggled(false);
                }
                return;
            }
            Vec3 extrapolatedPos = PositionUtils.extrapolate(this.target, this.extrapolation.getValue().intValue()).getCenter();
            BlockPos blockPos = new BlockPos((int)Math.floor(extrapolatedPos.x), (int)extrapolatedPos.y, (int)Math.floor(extrapolatedPos.z));
            if (this.target.getItemBySlot(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA) && AutoWebModule.mc.level.getBlockState(blockPos).getBlock().equals(Blocks.COBWEB)) {
                blockPos = blockPos.above();
            }
            if (!AutoWebModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
                return;
            }
            if (AutoWebModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.range.getValue().doubleValue())) {
                return;
            }
            if (PositionUtils.getFlooredPosition((Entity)AutoWebModule.mc.player).equals((Object)blockPos) && HoleUtils.isPlayerInHole((Player)AutoWebModule.mc.player)) {
                return;
            }
            Direction direction = WorldUtils.getDirection(blockPos, this.strictDirection.getValue());
            if (direction == null && !this.airPlace.getValue()) {
                return;
            }
            if (this.rotate.getValue() && this.airPlace.getValue()) {
                this.rotatePosition = blockPos;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), cobwebSlot, oldSlot);
            WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), false, this.render.getValue());
            InventoryUtils.switchBack(this.autoSwitch.getValue(), cobwebSlot, oldSlot);
            this.ticks = 0;
        };
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    @SubscribeEvent
    public void onClientRotation(ClientRotationEvent clientRotationEvent) {
        if (this.rotatePosition == null || clientRotationEvent.isCancelled()) {
            return;
        }
        float[] rotations = RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)this.rotatePosition));
        clientRotationEvent.setYaw(rotations[0]);
        clientRotationEvent.setPitch(rotations[1]);
    }

    @Override
    public void onEnable() {
        if (AutoWebModule.mc.player == null || AutoWebModule.mc.level == null) {
            this.setToggled(false);
        }
    }

    @Override
    public String getMetaData() {
        if (this.target == null) {
            return "None";
        }
        return this.target.getName().getString();
    }

    private Player getTarget(List<AbstractClientPlayer> players) {
        Player bestTarget = null;
        for (Player candidate : players) {
            if (candidate == AutoWebModule.mc.player || !candidate.isAlive() || candidate.getHealth() <= 0.0f || AutoWebModule.mc.player.distanceToSqr((Entity)candidate) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || Night.FRIEND_MANAGER.contains(candidate.getName().getString()) || this.holeCheck.getValue() && !HoleUtils.isPlayerInHole(candidate)) continue;
            if (bestTarget == null) {
                bestTarget = candidate;
                continue;
            }
            if (!(AutoWebModule.mc.player.distanceToSqr((Entity)candidate) < AutoWebModule.mc.player.distanceToSqr((Entity)bestTarget))) continue;
            bestTarget = candidate;
        }
        return bestTarget;
    }
}

