/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
 *  net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.events.impl.PlayerMineEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.HitboxDesyncModule;
import lightning.modules.impl.movement.SpeedModule;
import lightning.modules.impl.movement.StepModule;
import lightning.modules.impl.movement.TickShiftModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.modules.impl.player.KeyActionModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Surround", description="Automatically places blocks at your feet to prevent crystal damage.", category=Module.Category.COMBAT)
public class SurroundModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting timing = new ModeSetting("Timing", "The timing that will be used in replacing broken surround blocks.", "Sequential", new String[]{"Vanilla", "Sequential"});
    public NumberSetting bpt = new NumberSetting("BPT", "The maximum number of blocks to place per tick.", 4, 1, 8);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public BooleanSetting await = new BooleanSetting("Await", "Waits for blocks to be registered by the client before placing on them.", false);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing surround blocks.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
    public BooleanSetting attack = new BooleanSetting("Attack", "Attacks any crystal that interferes with your surround.", false);
    public BooleanSetting attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
    public BooleanSetting attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting center = new BooleanSetting("Center", "Puts you in the center of the block when you surround.", false);
    public BooleanSetting floor = new BooleanSetting("Floor", "Places blocks under your feet as well.", true);
    public BooleanSetting underFeet = new BooleanSetting("UnderFeet", "Replaces the block directly under your feet when it breaks.", new BooleanSetting.Visibility(this.floor, true), true);
    public BooleanSetting extension = new BooleanSetting("Extension", "Extends the surround if there are entities obstructing block placement.", true);
    public BooleanSetting blocker = new BooleanSetting("Blocker", "Pre-places extra blocks where an enemy is digging toward you.", true);
    public BooleanSetting blockerUp = new BooleanSetting("Up", "Blocker: places above/below the edge being mined through.", new BooleanSetting.Visibility(this.blocker, true), true);
    public BooleanSetting blockerCorner = new BooleanSetting("Corner", "Blocker: places the 2 corner cells left/right of the edge being mined through.", new BooleanSetting.Visibility(this.blocker, true), true);
    public BooleanSetting blockerEdge = new BooleanSetting("Edge", "Blocker: places the cell straight out in front of the edge being mined through.", new BooleanSetting.Visibility(this.blocker, true), true);
    public BooleanSetting advoidHelp = new BooleanSetting("AdvoidHelp", "Skips placements that would also wall in a nearby enemy.", true);
    public NumberSetting advoidHelpHealth = new NumberSetting("AdvoidHelpHealth", "Below this health, place every block regardless -- ignores AdvoidHelp entirely.", new BooleanSetting.Visibility(this.advoidHelp, true), (Number)Float.valueOf(10.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(20.0f));
    public BooleanSetting whileEating = new BooleanSetting("WhileEating", "Places blocks normally while eating.", true);
    public BooleanSetting advanced = new BooleanSetting("Advanced", "While WhileEating is off, keeps placing through an eat pause as long as your surround still has an open gap (stops once sealed, or if you're stuck inside a block).", new BooleanSetting.Visibility(this.whileEating, false), false);
    public BooleanSetting chorusCenter = new BooleanSetting("CenterOnTP", "Centers you if you have just teleported to surround against crystals easier.", true);
    public BooleanSetting predict = new BooleanSetting("Predict", "Replaces a surround block instantly the moment we see the server's own break packet for it, instead of waiting for the next normal placement cycle.", true);
    private int packetsSent = 0;
    private boolean isWorking = false;
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting itemDisable = new BooleanSetting("ItemDisable", "Toggles off the module when no valid items are found in your hotbar/inventory.", true);
    public BooleanSetting jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever your Y level changes.", true);
    public BooleanSetting pearlDisable = new BooleanSetting("PearlDisable", "Toggles off surround whenever you throw or land an ender pearl.", false);
    public BooleanSetting chorusDisable = new BooleanSetting("ChorusDisable", "Toggles off surround whenever you eat a chorus fruit.", false);
    public BooleanSetting rubberbandDisable = new BooleanSetting("RubberbandDisable", "Toggles off surround whenever you get rubberbanded/setback by the server.", false);
    public BooleanSetting stepToggle = new BooleanSetting("StepToggle", "Toggles the step module when you surround.", false);
    public BooleanSetting speedToggle = new BooleanSetting("SpeedToggle", "Toggles the speed module when you surround.", false);
    public BooleanSetting tickShiftToggle = new BooleanSetting("TickShiftToggle", "Toggles the TickShift module when you surround.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    private Set<BlockPos> targetPositions = new HashSet<BlockPos>();
    private BlockPos lastPosition = null;
    private int blocksPlaced = 0;
    private boolean awaitChorusCenter = false;
    private final Map<Integer, MineData> activeMines = new HashMap<Integer, MineData>();
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    private long lastPlacementTime = 0L;

    @Override
    public String getMetaData() {
        return "Packet: " + this.packetsSent;
    }

    @Override
    public void onEnable() {
        if (SurroundModule.mc.player == null || SurroundModule.mc.level == null) {
            return;
        }
        this.packetsSent = 0;
        this.lastPosition = PositionUtils.getFlooredPosition((Entity)SurroundModule.mc.player);
        this.awaitChorusCenter = false;
        this.activeMines.clear();
        if (this.stepToggle.getValue() && Night.MODULE_MANAGER.getModule(StepModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(StepModule.class).setToggled(false);
        }
        if (this.speedToggle.getValue() && Night.MODULE_MANAGER.getModule(SpeedModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(SpeedModule.class).setToggled(false);
        }
        if (this.tickShiftToggle.getValue() && Night.MODULE_MANAGER.getModule(TickShiftModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(TickShiftModule.class).setToggled(false);
        }
        if (this.center.getValue()) {
            SurroundModule.mc.player.setPos((double)this.lastPosition.getX() + 0.5, (double)this.lastPosition.getY(), (double)this.lastPosition.getZ() + 0.5);
        }
        if (this.rotate.getValue().equalsIgnoreCase("Grim")) {
            Night.WORLD_MANAGER.setTimerMultiplier(1.08f);
        }
    }

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
        this.targetPositions.clear();
        this.lastPosition = null;
        this.isWorking = false;
        this.awaitChorusCenter = false;
        this.activeMines.clear();
        this.blocksPlaced = 0;
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerMineEvent playerMineEvent) {
        if (!this.blocker.getValue() || SurroundModule.mc.player == null) {
            return;
        }
        if (playerMineEvent.getActorID() == SurroundModule.mc.player.getId()) {
            return;
        }
        boolean bl = false;
        for (BlockPos blockPos : this.ownFootCells()) {
            if (playerMineEvent.getPosition().getY() != blockPos.getY()) continue;
            int n = playerMineEvent.getPosition().getX() - blockPos.getX();
            int n2 = playerMineEvent.getPosition().getZ() - blockPos.getZ();
            if (Math.abs(n) + Math.abs(n2) != 1) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return;
        }
        MineData mineData = this.activeMines.get(playerMineEvent.getActorID());
        if (mineData == null || !mineData.pos.equals((Object)playerMineEvent.getPosition())) {
            this.activeMines.put(playerMineEvent.getActorID(), new MineData(playerMineEvent.getPosition()));
        } else {
            mineData.lastSeenTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent playerJumpEvent) {
        if (this.jumpDisable.getValue()) {
            this.setToggled(false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (SurroundModule.mc.player != null && SurroundModule.mc.level != null) {
            if (this.awaitChorusCenter) {
                BlockPos floorPos = PositionUtils.getFlooredPosition((Entity)SurroundModule.mc.player);
                SurroundModule.mc.player.setPos((double)floorPos.getX() + 0.5, SurroundModule.mc.player.getY(), (double)floorPos.getZ() + 0.5);
                this.lastPosition = floorPos;
                this.awaitChorusCenter = false;
            }
            if (!this.jumpDisable.getValue() || !(SurroundModule.mc.player.fallDistance > 2.0) && (!Night.MODULE_MANAGER.getModule(StepModule.class).isToggled() && !Night.MODULE_MANAGER.getModule(SpeedModule.class).isToggled() || this.lastPosition != null && this.lastPosition.getY() == PositionUtils.getFlooredPosition((Entity)SurroundModule.mc.player).getY())) {
                if (this.whileEating.getValue() || !EntityUtils.isEating() || !this.shouldPauseForEating()) {
                    this.blocksPlaced = 0;
                    Item mainHandItem = SurroundModule.mc.player.getMainHandItem().getItem();
                    if (!this.autoSwitch.getValue().equalsIgnoreCase("None") || (mainHandItem instanceof BlockItem && this.isBlastProof(((BlockItem)mainHandItem).getBlock()))) {
                        int slot = this.findBlastProofBlock(0, !this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") && !this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 8 : 35);
                        int selectedSlot = SurroundModule.mc.player.getInventory().getSelectedSlot();
                        if (slot == -1) {
                            if (this.itemDisable.getValue()) {
                                Night.CHAT_MANAGER.tagged("No blast-proof blocks could be found in your inventory.", this.getName());
                                this.setToggled(false);
                            }
                            this.targetPositions.clear();
                        } else {
                            this.targetPositions = HoleUtils.getFeetPositions((Player)SurroundModule.mc.player, this.extension.getValue(), this.floor.getValue(), this.underFeet.getValue(), false);
                            if (this.blocker.getValue()) {
                                this.applyBlocker();
                            }
                            if (this.attack.getValue()) {
                                this.attackTrapCrystals();
                            }
                            Set<BlockPos> set = this.advoidHelp.getValue() ? this.computeHelpBlocked() : Set.of();
                            this.targetPositions.stream().filter(blockPos -> SurroundModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= Mth.square((double)this.range.getValue().doubleValue())).filter(blockPos -> !WorldUtils.isPlaceable(blockPos) && WorldUtils.isInstantBreakable(blockPos)).forEach(blockPos -> WorldUtils.instantBreak(blockPos, Direction.UP));
                            HitboxDesyncModule hitboxDesyncModule = Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
                            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>(this.targetPositions.stream().filter(blockPos -> SurroundModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= Mth.square((double)this.range.getValue().doubleValue())).filter(blockPos -> WorldUtils.isPlaceable(blockPos, hitboxDesyncModule != null && hitboxDesyncModule.isToggled() && !hitboxDesyncModule.close.getValue())).filter(blockPos -> !set.contains(blockPos)).toList());
                            Vec3 vec3 = SurroundModule.mc.player.position().add(SurroundModule.mc.player.getDeltaMovement().scale(0.5));
                            arrayList.sort((blockPos, blockPos2) -> {
                                int yCompare = Integer.compare(blockPos.getY(), blockPos2.getY());
                                if (yCompare != 0) {
                                    return yCompare;
                                }
                                boolean bl2 = WorldUtils.getDirection(blockPos, false) != null;
                                boolean bl3 = WorldUtils.getDirection(blockPos2, false) != null;
                                if (bl2 != bl3) {
                                    return bl2 ? -1 : 1;
                                }
                                return Double.compare(Vec3.atCenterOf((Vec3i)blockPos).distanceToSqr(vec3), Vec3.atCenterOf((Vec3i)blockPos2).distanceToSqr(vec3));
                            });
                            KeyActionModule keyActionModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(KeyActionModule.class) : null;
                            if (this.pearlDisable.getValue() && (keyActionModule != null && keyActionModule.isPearlActive() || SurroundModule.mc.player.isUsingItem() && SurroundModule.mc.player.getUseItem().getItem() == Items.ENDER_PEARL)) {
                                this.setToggled(false);
                            } else if (this.chorusDisable.getValue() && SurroundModule.mc.player.isUsingItem() && SurroundModule.mc.player.getUseItem().getItem() == Items.CHORUS_FRUIT) {
                                this.setToggled(false);
                            } else if (keyActionModule == null || !keyActionModule.isPearlActive()) {
                                if (arrayList.isEmpty()) {
                                    if (this.selfDisable.getValue()) {
                                        this.setToggled(false);
                                    }
                                } else {
                                    this.isWorking = true;
                                    boolean grimRotate = this.rotate.getValue().equalsIgnoreCase("Grim");
                                    if (grimRotate) {
                                        Night.ROTATION_MANAGER.beginBatchRotation();
                                    }
                                    try {
                                        InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, selectedSlot);
                                        ArrayList<BlockPos> placedPositions = new ArrayList<BlockPos>();
                                        for (BlockPos blockPos4 : arrayList) {
                                            if (this.blocksPlaced >= this.bpt.getValue().intValue()) break;
                                            if (this.attack.getValue()) {
                                                for (EndCrystal endCrystal : SurroundModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(blockPos4).inflate(0.5))) {
                                                    if (!endCrystal.isAlive()) continue;
                                                    mc.getConnection().send((Packet)new ServerboundAttackPacket(endCrystal.getId()));
                                                    if (!this.attackSwing.getValue()) continue;
                                                    mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                                }
                                            }
                                            List<Direction> directions = WorldUtils.getDirections(blockPos4, placedPositions, this.strictDirection.getValue());
                                            if (directions.isEmpty()) {
                                                if (this.airPlace.getValue() && !this.strictDirection.getValue()) {
                                                    this.placeAirBlock(blockPos4);
                                                    placedPositions.add(blockPos4);
                                                    this.lastPlacementTime = System.currentTimeMillis();
                                                    ++this.blocksPlaced;
                                                    continue;
                                                }
                                                BlockPos supportBlock = WorldUtils.findSupportBlock(blockPos4, placedPositions, this.strictDirection.getValue(), 2);
                                                if (supportBlock == null) continue;
                                                Direction supportDir = WorldUtils.getDirection(supportBlock, placedPositions, this.strictDirection.getValue());
                                                if (supportDir == null || !WorldUtils.placeBlock(supportBlock, supportDir, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                                                placedPositions.add(supportBlock);
                                                this.lastPlacementTime = System.currentTimeMillis();
                                                ++this.blocksPlaced;
                                                continue;
                                            }
                                            boolean placed = false;
                                            for (Direction dir : directions) {
                                                if (!WorldUtils.placeBlock(blockPos4, dir, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                                                placedPositions.add(blockPos4);
                                                this.lastPlacementTime = System.currentTimeMillis();
                                                ++this.blocksPlaced;
                                                placed = true;
                                                break;
                                            }
                                            if (placed) continue;
                                            if (this.airPlace.getValue() && !this.strictDirection.getValue()) {
                                                this.placeAirBlock(blockPos4);
                                                placedPositions.add(blockPos4);
                                                this.lastPlacementTime = System.currentTimeMillis();
                                                ++this.blocksPlaced;
                                                continue;
                                            }
                                            BlockPos supportBlock = WorldUtils.findSupportBlock(blockPos4, placedPositions, this.strictDirection.getValue(), 2);
                                            if (supportBlock == null) continue;
                                            Direction supportDir = WorldUtils.getDirection(supportBlock, placedPositions, this.strictDirection.getValue());
                                            if (supportDir == null || !WorldUtils.placeBlock(supportBlock, supportDir, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                                            placedPositions.add(supportBlock);
                                            this.lastPlacementTime = System.currentTimeMillis();
                                            ++this.blocksPlaced;
                                        }
                                        InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, selectedSlot);
                                    }
                                    finally {
                                        if (grimRotate) {
                                            Night.ROTATION_MANAGER.endBatchRotation();
                                        }
                                        this.isWorking = false;
                                    }
                                }
                            }
                        }
                    } else {
                        if (this.itemDisable.getValue()) {
                            Night.CHAT_MANAGER.tagged("You are currently not holding any blast-proof blocks.", this.getName());
                            this.setToggled(false);
                        }
                        this.targetPositions.clear();
                    }
                }
            } else {
                this.setToggled(false);
            }
        }
    }

    private void placeAirBlock(BlockPos blockPos) {
        WorldUtils.airPlaceBlock(blockPos, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (SurroundModule.mc.player == null || SurroundModule.mc.level == null) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundOpenScreenPacket clientboundOpenScreenPacket) {
            AutoShopModule autoShopModule = Night.MODULE_MANAGER.getModule(AutoShopModule.class);
            if (autoShopModule != null && autoShopModule.isRunning()) {
                return;
            }
            if (this.isWorking || System.currentTimeMillis() - this.lastPlacementTime < 1000L || System.currentTimeMillis() - WorldUtils.lastInteractablePlaceTime < 1000L) {
                packetReceiveEvent.setCancelled(true);
                mc.getConnection().send((Packet)new ServerboundContainerClosePacket(clientboundOpenScreenPacket.getContainerId()));
                return;
            }
        }
        if (packet instanceof ClientboundPlayerPositionPacket clientboundPlayerPositionPacket) {
            if (this.rubberbandDisable.getValue() && SurroundModule.mc.player.position().distanceToSqr(clientboundPlayerPositionPacket.change().position().x(), clientboundPlayerPositionPacket.change().position().y(), clientboundPlayerPositionPacket.change().position().z()) >= 1.0) {
                this.setToggled(false);
                return;
            }
            if (this.chorusCenter.getValue() && SurroundModule.mc.player.isUsingItem() && SurroundModule.mc.player.getUseItem().getItem() == Items.CHORUS_FRUIT) {
                if (this.chorusDisable.getValue()) {
                    this.setToggled(false);
                    return;
                }
                this.awaitChorusCenter = true;
            }
        }
        if (this.attack.getValue() && packet instanceof ClientboundAddEntityPacket clientboundAddEntityPacket && clientboundAddEntityPacket.getType() == EntityTypes.END_CRYSTAL) {
            Vec3 crystalPos = new Vec3(clientboundAddEntityPacket.getX(), clientboundAddEntityPacket.getY(), clientboundAddEntityPacket.getZ());
            AABB aABB = new AABB(crystalPos.x - 1.0, crystalPos.y, crystalPos.z - 1.0, crystalPos.x + 1.0, crystalPos.y + 2.0, crystalPos.z + 1.0);
            boolean intersects = false;
            for (BlockPos blockPos : this.targetPositions) {
                if (!aABB.intersects(new AABB(blockPos)) && !(crystalPos.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= 4.0)) continue;
                intersects = true;
                break;
            }
            if (intersects && SurroundModule.mc.player.getEyePosition().distanceToSqr(crystalPos) <= Mth.square((double)this.attackRange.getValue().doubleValue()) && (this.attackMultiTask.getValue() || !SurroundModule.mc.player.isUsingItem())) {
                mc.getConnection().send((Packet)new ServerboundAttackPacket(clientboundAddEntityPacket.getId()));
                if (this.attackSwing.getValue()) {
                    mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }
        }
        if (this.timing.getValue().equalsIgnoreCase("Sequential") && this.predict.getValue() && packet instanceof ClientboundBlockUpdatePacket clientboundBlockUpdatePacket && clientboundBlockUpdatePacket.getBlockState().isAir() && this.targetPositions.contains(clientboundBlockUpdatePacket.getPos()) && this.blocksPlaced < this.bpt.getValue().intValue() && (this.whileEating.getValue() || !EntityUtils.isEating() || !this.shouldPauseForEating())) {
            HitboxDesyncModule hitboxDesync = Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class);
            boolean desyncCheck = hitboxDesync != null && hitboxDesync.isToggled() && !hitboxDesync.close.getValue();
            if (WorldUtils.isPlaceable(clientboundBlockUpdatePacket.getPos(), desyncCheck)) {
                int slot = this.findBlastProofBlock(0, !this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") && !this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 8 : 35);
                int selectedSlot = SurroundModule.mc.player.getInventory().getSelectedSlot();
                if (slot != -1) {
                    Direction dir = WorldUtils.getDirection(clientboundBlockUpdatePacket.getPos(), this.strictDirection.getValue());
                    if (dir != null) {
                        Night.ROTATION_MANAGER.beginBatchRotation();
                        try {
                            if (InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, selectedSlot)) {
                                WorldUtils.placeBlock(clientboundBlockUpdatePacket.getPos(), dir, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue());
                                ++this.blocksPlaced;
                                this.lastPlacementTime = System.currentTimeMillis();
                                InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, selectedSlot);
                                return;
                            }
                        }
                        finally {
                            Night.ROTATION_MANAGER.endBatchRotation();
                        }
                    }
                    if (this.airPlace.getValue() && !this.strictDirection.getValue()) {
                        Night.ROTATION_MANAGER.beginBatchRotation();
                        try {
                            if (InventoryUtils.switchSlot(this.autoSwitch.getValue(), slot, selectedSlot)) {
                                this.placeAirBlock(clientboundBlockUpdatePacket.getPos());
                                ++this.blocksPlaced;
                                this.lastPlacementTime = System.currentTimeMillis();
                                InventoryUtils.switchBack(this.autoSwitch.getValue(), slot, selectedSlot);
                                return;
                            }
                        }
                        finally {
                            Night.ROTATION_MANAGER.endBatchRotation();
                        }
                    }
                }
            }
        }
    }

    private void attackTrapCrystals() {
        Night.ROTATION_MANAGER.beginBatchRotation();
        for (EndCrystal endCrystal : SurroundModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(SurroundModule.mc.player.blockPosition()).inflate(this.attackRange.getValue().doubleValue() + 3.0))) {
            if (!endCrystal.isAlive() || endCrystal.isRemoved() || endCrystal.tickCount < this.attackAge.getValue().intValue()) continue;
            AABB aABB = endCrystal.getBoundingBox();
            boolean bl = false;
            for (BlockPos blockPos : this.targetPositions) {
                if (!SurroundModule.mc.level.getBlockState(blockPos).canBeReplaced() || !new AABB(blockPos).intersects(aABB)) continue;
                bl = true;
                break;
            }
            if (!bl || aABB.distanceToSqr(SurroundModule.mc.player.getEyePosition()) > this.attackRange.getValue().doubleValue() * this.attackRange.getValue().doubleValue() || !this.attackMultiTask.getValue() && SurroundModule.mc.player.isUsingItem()) continue;
            if (this.attackRotate.getValue()) {
                float[] rotations = RotationUtils.getRotations(endCrystal.getBoundingBox().getCenter());
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
                boolean bl2 = aABB.contains(SurroundModule.mc.player.getEyePosition());
                if (!bl2 && !WorldUtils.canSee(endCrystal.getBoundingBox().getCenter())) continue;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(endCrystal.getId()));
            if (!this.attackSwing.getValue()) continue;
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        Night.ROTATION_MANAGER.endBatchRotation();
    }

    private void applyBlocker() {
        this.activeMines.entrySet().removeIf(entry -> {
            MineData mineData = (MineData)entry.getValue();
            return SurroundModule.mc.level.getEntity(((Integer)entry.getKey()).intValue()) == null || SurroundModule.mc.level.getBlockState(mineData.pos).canBeReplaced() || System.currentTimeMillis() - mineData.lastSeenTime > 1000L;
        });
        for (MineData mineData : this.activeMines.values()) {
            ++mineData.ticks;
            if (mineData.ticks < 2 && System.currentTimeMillis() - mineData.startTime < 100L) continue;
            BlockPos blockPos = mineData.pos;
            for (BlockPos blockPos2 : this.ownFootCells()) {
                if (blockPos.getY() != blockPos2.getY()) continue;
                int n = blockPos.getX() - blockPos2.getX();
                int n2 = blockPos.getZ() - blockPos2.getZ();
                if (Math.abs(n) + Math.abs(n2) != 1) continue;
                if (this.blockerUp.getValue()) {
                    this.targetPositions.add(blockPos.above());
                    this.targetPositions.add(blockPos.below());
                }
                if (this.blockerCorner.getValue()) {
                    if (n != 0) {
                        this.targetPositions.add(blockPos2.offset(n, 0, 1));
                        this.targetPositions.add(blockPos2.offset(n, 0, -1));
                    } else {
                        this.targetPositions.add(blockPos2.offset(1, 0, n2));
                        this.targetPositions.add(blockPos2.offset(-1, 0, n2));
                    }
                }
                if (!this.blockerEdge.getValue()) continue;
                if (n != 0) {
                    this.targetPositions.add(blockPos2.offset(n * 2, 0, 0));
                    continue;
                }
                this.targetPositions.add(blockPos2.offset(0, 0, n2 * 2));
            }
        }
    }

    private Set<BlockPos> computeHelpBlocked() {
        Set<BlockPos> set = this.ownFootCells();
        if (SurroundModule.mc.player.getHealth() < this.advoidHelpHealth.getValue().floatValue()) {
            return Set.of();
        }
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        for (Player player : SurroundModule.mc.level.players()) {
            if (player == SurroundModule.mc.player || Night.FRIEND_MANAGER.contains(player.getName().getString()) || SurroundModule.mc.player.distanceToSqr((Entity)player) > 25.0 || !SurroundModule.mc.level.getBlockState(player.blockPosition()).canBeReplaced()) continue;
            for (BlockPos blockPos : this.ownFootCells(player)) {
                for (Direction direction : HORIZONTALS) {
                    BlockPos blockPos2 = blockPos.relative(direction);
                    if (this.ownFootCells(player).contains(blockPos2)) continue;
                    hashSet.add(blockPos2);
                }
                hashSet.add(blockPos.below());
                hashSet.add(blockPos);
            }
        }
        if (hashSet.isEmpty()) {
            return Set.of();
        }
        HashSet<BlockPos> hashSet2 = new HashSet<BlockPos>();
        for (BlockPos blockPos : hashSet) {
            boolean bl = false;
            for (BlockPos blockPos3 : set) {
                if (!blockPos.equals((Object)blockPos3) && !blockPos.equals((Object)blockPos3.above()) && !blockPos.equals((Object)blockPos3.below()) && !blockPos.equals((Object)blockPos3.north()) && !blockPos.equals((Object)blockPos3.south()) && !blockPos.equals((Object)blockPos3.east()) && !blockPos.equals((Object)blockPos3.west())) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            hashSet2.add(blockPos);
        }
        return hashSet2;
    }

    private Set<BlockPos> ownFootCells() {
        return this.ownFootCells((Player)SurroundModule.mc.player);
    }

    private Set<BlockPos> ownFootCells(Player player) {
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        AABB aABB = player.getBoundingBox();
        int n = player.blockPosition().getY();
        int n2 = (int)Math.floor(aABB.minX);
        while ((double)n2 < Math.ceil(aABB.maxX)) {
            int n3 = (int)Math.floor(aABB.minZ);
            while ((double)n3 < Math.ceil(aABB.maxZ)) {
                hashSet.add(new BlockPos(n2, n, n3));
                ++n3;
            }
            ++n2;
        }
        return hashSet;
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.setToggled(false);
    }

    private boolean shouldPauseForEating() {
        if (!this.advanced.getValue()) {
            return true;
        }
        if (EntityUtils.isPhased((Entity)SurroundModule.mc.player)) {
            return true;
        }
        HashSet<BlockPos> hashSet = HoleUtils.getFeetPositions((Player)SurroundModule.mc.player, this.extension.getValue(), this.floor.getValue(), this.underFeet.getValue(), false);
        boolean bl = hashSet.stream().filter(WorldUtils::isPlaceable).anyMatch(blockPos -> WorldUtils.getDirection(blockPos, this.strictDirection.getValue()) != null || this.airPlace.getValue());
        return !bl;
    }

    private boolean isBlastProof(Block block) {
        return block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ANCIENT_DEBRIS || block == Blocks.ENCHANTING_TABLE || block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL;
    }

    private int findBlastProofBlock(int n, int n2) {
        float f = -1.0f;
        int n3 = -1;
        for (int i = n; i <= n2; ++i) {
            BlockItem blockItem;
            Block block;
            Item item = SurroundModule.mc.player.getInventory().getItem(i).getItem();
            if (!(item instanceof BlockItem) || !this.isBlastProof(block = (blockItem = (BlockItem)item).getBlock())) continue;
            float f2 = block.defaultDestroyTime();
            if (f2 == -1.0f) {
                return i;
            }
            if (!(f2 > f)) continue;
            f = f2;
            n3 = i;
        }
        return n3;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent.Post post) {
        if (this.isWorking) {
            ++this.packetsSent;
        }
    }

    private static class MineData {
        final BlockPos pos;
        final long startTime;
        int ticks;
        long lastSeenTime;

        MineData(BlockPos blockPos) {
            this.pos = blockPos;
            this.startTime = System.currentTimeMillis();
            this.ticks = 0;
            this.lastSeenTime = System.currentTimeMillis();
        }
    }
}

