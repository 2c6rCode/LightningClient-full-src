/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.AABB
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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="HoleFill", description="Automatically places blocks inside of holes to prevent others from getting inside of them.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class HoleFillModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting mode = new ModeSetting("Mode", "The offsets that will be used when trapping.", "Normal", new String[]{"Normal", "Smart"});
    public BooleanSetting webs = new BooleanSetting("Webs", "Use webs to holefill instead of blocks.", false);
    public BooleanSetting selfWeb = new BooleanSetting("SelfWeb", "Includes your own hole when holefilling with webs.", false);
    public BooleanSetting refill = new BooleanSetting("Refill", "Fills the hole you just exited.", false);
    public BooleanSetting waitUntilExited = new BooleanSetting("WaitUntilExited", "Waits until your hitbox completely leaves the hole before refilling.", new BooleanSetting.Visibility(this.refill, true), true);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public NumberSetting bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which the target should be at.", new ModeSetting.Visibility(this.mode, "Smart"), (Number)Float.valueOf(8.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(16.0f));
    public NumberSetting smartRange = new NumberSetting("SmartRange", "The distance at which the holes will have to be away from the target.", new ModeSetting.Visibility(this.mode, "Smart"), (Number)Float.valueOf(3.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(6.0f));
    public BooleanSetting safety = new BooleanSetting("Safety", "Prevents holes close to you from getting filled.", true);
    public NumberSetting safetyRange = new NumberSetting("SafetyRange", "The maximum distance the holes can be at to be prevented from getting filled.", new BooleanSetting.Visibility(this.safety, true), (Number)Float.valueOf(2.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(6.0f));
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with hole filling.", false);
    public BooleanSetting attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
    public BooleanSetting attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to fill double holes.", false);
    public BooleanSetting holeCheck = new BooleanSetting("HoleCheck", "Checks if the target isn't in a hole before placing.", true);
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module whenever you run out of items to place with.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private List<BlockPos> positions = new ArrayList<BlockPos>();
    private int blocksPlaced = 0;
    private BlockPos lastHolePos = null;
    private BlockPos refillPos = null;
    private boolean wasInHole = false;

    @Override
    public void onEnable() {
        BlockPos blockPos;
        this.lastHolePos = null;
        this.refillPos = null;
        this.wasInHole = false;
        if (!this.getNull() && HoleFillModule.mc.player != null && this.isHole(blockPos = PositionUtils.getFlooredPosition((Entity)HoleFillModule.mc.player))) {
            this.lastHolePos = blockPos.immutable();
            this.wasInHole = true;
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (!this.whileEating.getValue() && EntityUtils.isEating()) {
            return;
        }
        List players = HoleFillModule.mc.level.players();
        Runnable runnable = () -> {
            Target target;
            this.blocksPlaced = 0;
            boolean notHoldingItem = this.webs.getValue() ? !HoleFillModule.mc.player.getMainHandItem().getItem().equals(Items.COBWEB) : !(HoleFillModule.mc.player.getMainHandItem().getItem() instanceof BlockItem);
            if (this.autoSwitch.getValue().equalsIgnoreCase("None") && notHoldingItem) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("You are currently not holding any " + (this.webs.getValue() ? "cobwebs." : "blocks."), this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            int itemSlot = this.webs.getValue() ? InventoryUtils.find(Items.COBWEB, 0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8) : InventoryUtils.findHardestBlock(0, this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            int oldSlot = HoleFillModule.mc.player.getInventory().getSelectedSlot();
            if (itemSlot == -1) {
                if (this.itemDisable.getValue()) {
                    Night.CHAT_MANAGER.tagged("No " + (this.webs.getValue() ? "cobwebs" : "blocks") + " could be found in your hotbar.", this.getName());
                    this.setToggled(false);
                }
                this.positions = new ArrayList<BlockPos>();
                return;
            }
            this.positions = this.mode.getValue().equalsIgnoreCase("Smart") ? ((target = this.getTarget(players)) == null ? new ArrayList<BlockPos>() : target.positions()) : this.getPositions(null);
            if (this.refill.getValue()) {
                this.updateExitedHole(HoleFillModule.mc.player);
                if (this.refillPos != null) {
                    if (!WorldUtils.isPlaceable(this.refillPos)) {
                        this.refillPos = null;
                    } else if (!this.waitUntilExited.getValue() || !this.playerIntersects(this.refillPos, (Player)HoleFillModule.mc.player)) {
                        if (HoleFillModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)this.refillPos)) <= Mth.square((double)this.range.getValue().doubleValue())) {
                            if (!this.positions.contains(this.refillPos)) {
                                this.positions.add(0, this.refillPos);
                            }
                        } else {
                            this.refillPos = null;
                        }
                    }
                }
            } else {
                this.refillPos = null;
                this.wasInHole = false;
                this.lastHolePos = null;
            }
            if (this.attack.getValue()) {
                this.attackHoleCrystals();
            }
            if (this.positions.isEmpty()) {
                if (this.selfDisable.getValue() && !this.mode.getValue().equalsIgnoreCase("Smart")) {
                    this.setToggled(false);
                }
                return;
            }
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), itemSlot, oldSlot);
                for (BlockPos blockPos : this.positions) {
                    if (this.blocksPlaced >= this.bpt.getValue().intValue()) break;
                    Direction direction = WorldUtils.getDirection(blockPos, this.strictDirection.getValue());
                    if (direction == null || !WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                    ++this.blocksPlaced;
                    if (!blockPos.equals((Object)this.refillPos)) continue;
                    this.refillPos = null;
                }
                InventoryUtils.switchBack(this.autoSwitch.getValue(), itemSlot, oldSlot);
            }
            finally {
                Night.ROTATION_MANAGER.endBatchRotation();
            }
        };
        if (this.asynchronous.getValue()) {
            ThreadExecutor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    private void attackHoleCrystals() {
        Night.ROTATION_MANAGER.beginBatchRotation();
        for (EndCrystal endCrystal : HoleFillModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(HoleFillModule.mc.player.blockPosition()).inflate(this.attackRange.getValue().doubleValue() + 3.0))) {
            if (!endCrystal.isAlive() || endCrystal.isRemoved() || endCrystal.tickCount < this.attackAge.getValue().intValue()) continue;
            AABB aABB = endCrystal.getBoundingBox();
            boolean intersectsHole = false;
            for (BlockPos blockPos : this.positions) {
                if (!HoleFillModule.mc.level.getBlockState(blockPos).canBeReplaced() || !new AABB(blockPos).intersects(aABB)) continue;
                intersectsHole = true;
                break;
            }
            if (!intersectsHole || aABB.distanceToSqr(HoleFillModule.mc.player.getEyePosition()) > this.attackRange.getValue().doubleValue() * this.attackRange.getValue().doubleValue() || !this.attackMultiTask.getValue() && HoleFillModule.mc.player.isUsingItem()) continue;
            if (this.attackRotate.getValue()) {
                float[] rotations = RotationUtils.getRotations(endCrystal.getBoundingBox().getCenter());
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
                boolean insideEye = aABB.contains(HoleFillModule.mc.player.getEyePosition());
                if (!insideEye && !WorldUtils.canSee(endCrystal.getBoundingBox().getCenter())) continue;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(endCrystal.getId()));
            if (!this.attackSwing.getValue()) continue;
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        Night.ROTATION_MANAGER.endBatchRotation();
    }

    private void updateExitedHole(LocalPlayer localPlayer) {
        BlockPos currentPos = PositionUtils.getFlooredPosition((Entity)localPlayer);
        boolean inHole = this.isHole(currentPos);
        if (inHole) {
            this.lastHolePos = currentPos.immutable();
            this.wasInHole = true;
            return;
        }
        if (!this.wasInHole || this.lastHolePos == null) {
            this.wasInHole = false;
            return;
        }
        if (!currentPos.equals((Object)this.lastHolePos)) {
            this.refillPos = this.lastHolePos.immutable();
        }
        this.wasInHole = false;
    }

    private boolean isHole(BlockPos blockPos) {
        if (HoleFillModule.mc.level == null || blockPos == null) {
            return false;
        }
        if (HoleUtils.getSingleHole(blockPos, 1.0, false) != null) {
            return true;
        }
        return this.doubleHoles.getValue() && HoleUtils.getDoubleHole(blockPos, 1.0) != null;
    }

    private boolean playerIntersects(BlockPos blockPos, Player player) {
        return player.getBoundingBox().intersects(new AABB(blockPos));
    }

    @Override
    public void onDisable() {
        this.positions = new ArrayList<BlockPos>();
        this.lastHolePos = null;
        this.refillPos = null;
        this.wasInHole = false;
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }

    private Target getTarget(List<AbstractClientPlayer> players) {
        Target bestTarget = null;
        for (Player player : players) {
            List<BlockPos> holePositions;
            if (player == HoleFillModule.mc.player || !player.isAlive() || player.getHealth() <= 0.0f || HoleFillModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || Night.FRIEND_MANAGER.contains(player.getName().getString()) || this.holeCheck.getValue() && HoleUtils.isPlayerInHole(player) || (holePositions = this.getPositions(player)).isEmpty()) continue;
            if (bestTarget == null) {
                bestTarget = new Target(player, holePositions);
                continue;
            }
            if (!(HoleFillModule.mc.player.distanceToSqr((Entity)player) < HoleFillModule.mc.player.distanceToSqr((Entity)bestTarget.player()))) continue;
            bestTarget = new Target(player, holePositions);
        }
        return bestTarget;
    }

    private List<BlockPos> getPositions(Player player) {
        ArrayList<BlockPos> holePositions = new ArrayList<BlockPos>();
        int rangeBlocks = this.range.getValue().intValue();
        BlockPos playerPos = HoleFillModule.mc.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed((BlockPos)playerPos.offset(-rangeBlocks, -rangeBlocks, -rangeBlocks), (BlockPos)playerPos.offset(rangeBlocks, rangeBlocks, rangeBlocks))) {
            if (HoleFillModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > Mth.square((double)this.range.getValue().doubleValue()) || !WorldUtils.isPlaceable(pos) || this.safety.getValue() && HoleFillModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) <= Mth.square((double)this.safetyRange.getValue().doubleValue())) continue;
            if (this.selfWeb.getValue() && HoleUtils.isPlayerInHole((Player)HoleFillModule.mc.player) && HoleFillModule.mc.player.blockPosition().equals((Object)pos)) {
                holePositions.add(pos.immutable());
                continue;
            }
            if (HoleUtils.getSingleHole(pos, 1.0) != null) {
                if (player != null && player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > Mth.square((double)this.smartRange.getValue().doubleValue())) continue;
                holePositions.add(pos.immutable());
                continue;
            }
            if (!this.doubleHoles.getValue() || HoleUtils.getDoubleHole(pos, 1.0) == null || player != null && player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > Mth.square((double)this.smartRange.getValue().doubleValue())) continue;
            holePositions.add(pos.immutable());
        }
        return holePositions;
    }

    private record Target(Player player, List<BlockPos> positions) {
    }
}

