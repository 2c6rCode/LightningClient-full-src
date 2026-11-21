/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Scaffold", description="Places blocks under the player to bridge automatically.", category=Module.Category.MISCELLANEOUS)
public class ScaffoldModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to blocks.", "Silent", InventoryUtils.SWITCH_MODES);
    public NumberSetting range = new NumberSetting("Range", "Range to place blocks.", 4.0, 1.0, 6.0);
    public BooleanSetting keepY = new BooleanSetting("KeepY", "Maintains the player's Y level while moving.", false);
    public BooleanSetting downwards = new BooleanSetting("Downwards", "Places blocks below your feet when sneaking.", true);
    public BooleanSetting tower = new BooleanSetting("Tower", "Quickly towers up when jumping.", true);
    public NumberSetting towerSpeed = new NumberSetting("TowerSpeed", "Vertical tower speed.", new BooleanSetting.Visibility(this.tower, true), (Number)0.42, (Number)0.1, (Number)1.0);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates towards block placements.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions facing you.", false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without support.", false);
    public NumberSetting delay = new NumberSetting("Delay", "Delay in ticks between placements.", 0, 0, 10);
    public BooleanSetting render = new BooleanSetting("Render", "Renders placed positions.", true);
    private int groundPosY = Integer.MIN_VALUE;
    private BlockPos lastPlacement = null;
    private Block currentScaffoldBlock = null;
    private int ticks = 0;
    private final Map<BlockPos, Long> pendingAttempts = new HashMap<BlockPos, Long>();

    @Override
    public void onEnable() {
        this.groundPosY = Integer.MIN_VALUE;
        this.lastPlacement = null;
        this.ticks = 0;
        this.pendingAttempts.clear();
        WorldUtils.clearPredictedPlacements();
    }

    @Override
    public void onDisable() {
        this.groundPosY = Integer.MIN_VALUE;
        this.lastPlacement = null;
        this.currentScaffoldBlock = null;
        this.pendingAttempts.clear();
        WorldUtils.clearPredictedPlacements();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (ScaffoldModule.mc.player == null || ScaffoldModule.mc.level == null) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        int n = this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8;
        int n2 = this.findValidBlockSlot(0, n);
        if (n2 == -1) {
            Input input = ScaffoldModule.mc.player.input != null ? ScaffoldModule.mc.player.input.keyPresses : Input.EMPTY;
            Input input2 = input;
            if (!input.shift()) {
                ScaffoldModule.mc.player.input.keyPresses = new Input(input.forward(), input.backward(), input.left(), input.right(), input.jump(), true, input.sprint());
            }
            return;
        }
        if (this.tower.getValue() && ScaffoldModule.mc.options.keyJump.isDown() && !this.isMovingHorizontally()) {
            ScaffoldModule.mc.player.setDeltaMovement(ScaffoldModule.mc.player.getDeltaMovement().x, this.towerSpeed.getValue().doubleValue(), ScaffoldModule.mc.player.getDeltaMovement().z);
        }
        int n3 = (int)Math.floor(ScaffoldModule.mc.player.getY());
        if (this.keepY.getValue() && this.isMovingHorizontally()) {
            if (ScaffoldModule.mc.player.onGround() || this.groundPosY < ScaffoldModule.mc.level.getMinY()) {
                this.groundPosY = n3;
            }
            n3 = this.groundPosY;
        } else if (this.downwards.getValue() && ScaffoldModule.mc.options.keyShift.isDown()) {
            n3 = ScaffoldModule.mc.player.getBlockY() - 1;
        } else {
            this.groundPosY = n3;
        }
        BlockPos blockPos = new BlockPos(ScaffoldModule.mc.player.getBlockX(), n3, ScaffoldModule.mc.player.getBlockZ());
        BlockPos blockPos2 = blockPos.below();
        List<BlockPos> list = this.getScaffoldPlacements(blockPos2);
        if (list.isEmpty()) {
            return;
        }
        int n4 = ScaffoldModule.mc.player.getInventory().getSelectedSlot();
        if (!InventoryUtils.switchSlot(this.autoSwitch.getValue(), n2, n4)) {
            return;
        }
        for (BlockPos blockPos3 : list) {
            if (!ScaffoldModule.mc.level.getBlockState(blockPos3).canBeReplaced() || this.isOnCooldown(blockPos3)) continue;
            Direction direction = WorldUtils.getDirection(blockPos3, this.strictDirection.getValue());
            if (direction == null) {
                if (!this.airPlace.getValue()) continue;
                WorldUtils.airPlaceBlock(blockPos3, InteractionHand.MAIN_HAND, this.rotate.getValue() ? "Normal" : "None", this.render.getValue());
                this.lastPlacement = blockPos3;
                this.predictCollision(blockPos3);
                this.markAttempted(blockPos3);
                continue;
            }
            if (!WorldUtils.placeBlock(blockPos3, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), false, this.render.getValue())) continue;
            this.lastPlacement = blockPos3;
            this.predictCollision(blockPos3);
            this.markAttempted(blockPos3);
        }
        InventoryUtils.switchBack(this.autoSwitch.getValue(), n2, n4);
        this.ticks = 0;
    }

    private void predictCollision(BlockPos blockPos) {
        if (this.currentScaffoldBlock != null) {
            WorldUtils.predictPlacement(blockPos, this.currentScaffoldBlock);
        }
    }

    @SubscribeEvent
    public void night$onBlockUpdate(PacketReceiveEvent packetReceiveEvent) {
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundBlockUpdatePacket) {
            ClientboundBlockUpdatePacket clientboundBlockUpdatePacket = (ClientboundBlockUpdatePacket)packet;
            WorldUtils.clearPredictedPlacement(clientboundBlockUpdatePacket.getPos());
            this.pendingAttempts.remove(clientboundBlockUpdatePacket.getPos());
        }
    }

    private boolean isOnCooldown(BlockPos blockPos) {
        Long l = this.pendingAttempts.get(blockPos);
        if (l == null) {
            return false;
        }
        long l2 = Math.max(50L, (long)Night.SERVER_MANAGER.getPing() * 2L);
        if (System.currentTimeMillis() - l < l2) {
            return true;
        }
        this.pendingAttempts.remove(blockPos);
        return false;
    }

    private void markAttempted(BlockPos blockPos) {
        this.pendingAttempts.put(blockPos, System.currentTimeMillis());
    }

    private boolean isMovingHorizontally() {
        return Math.abs(ScaffoldModule.mc.player.getDeltaMovement().x) > 0.05 || Math.abs(ScaffoldModule.mc.player.getDeltaMovement().z) > 0.05;
    }

    private int findValidBlockSlot(int n, int n2) {
        for (int i = n; i <= n2; ++i) {
            BlockItem blockItem;
            Block block;
            Item item;
            ItemStack itemStack = ScaffoldModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || !((item = itemStack.getItem()) instanceof BlockItem) || (block = (blockItem = (BlockItem)item).getBlock()).defaultBlockState().canBeReplaced() || !block.defaultBlockState().blocksMotion()) continue;
            this.currentScaffoldBlock = block;
            return i;
        }
        return -1;
    }

    private List<BlockPos> getScaffoldPlacements(BlockPos blockPos) {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        if (this.airPlace.getValue()) {
            if (this.isWithinRange(blockPos)) {
                arrayList.add(blockPos);
            }
            return arrayList;
        }
        this.ensurePlaceableWithSupport(blockPos, arrayList);
        if (this.lastPlacement != null) {
            int n = this.lastPlacement.getX();
            int n2 = this.lastPlacement.getY();
            int n3 = this.lastPlacement.getZ();
            int n4 = blockPos.getX();
            int n5 = blockPos.getY();
            int n6 = blockPos.getZ();
            int n7 = n4 - n;
            int n8 = n5 - n2;
            int n9 = n6 - n3;
            int n10 = Integer.compare(n7, 0);
            int n11 = Integer.compare(n8, 0);
            int n12 = Integer.compare(n9, 0);
            n7 = Math.abs(n7);
            n8 = Math.abs(n8);
            n9 = Math.abs(n9);
            int n13 = n7 << 1;
            int n14 = n8 << 1;
            int n15 = n9 << 1;
            int n16 = 0;
            if (n7 >= n8 && n7 >= n9) {
                int n17 = n14 - n7;
                int n18 = n15 - n7;
                while (true) {
                    BlockPos blockPos2 = new BlockPos(n, n2, n3);
                    this.ensurePlaceableWithSupport(blockPos2, arrayList);
                    if (++n16 <= 8 && (n != n4 || n2 != n5 || n3 != n6)) {
                        if (n17 >= 0) {
                            n2 += n11;
                            n17 -= n13;
                        }
                        if (n18 >= 0) {
                            n3 += n12;
                            n18 -= n13;
                        }
                        n += n10;
                        n17 += n14;
                        n18 += n15;
                        continue;
                    }
                    break;
                }
            } else if (n8 >= n7 && n8 >= n9) {
                int n19 = n13 - n8;
                int n20 = n15 - n8;
                while (true) {
                    BlockPos blockPos3 = new BlockPos(n, n2, n3);
                    this.ensurePlaceableWithSupport(blockPos3, arrayList);
                    if (++n16 <= 8 && (n != n4 || n2 != n5 || n3 != n6)) {
                        if (n19 >= 0) {
                            n += n10;
                            n19 -= n14;
                        }
                        if (n20 >= 0) {
                            n3 += n12;
                            n20 -= n14;
                        }
                        n2 += n11;
                        n19 += n13;
                        n20 += n15;
                        continue;
                    }
                    break;
                }
            } else {
                int n21 = n13 - n9;
                int n22 = n14 - n9;
                while (true) {
                    BlockPos blockPos4 = new BlockPos(n, n2, n3);
                    this.ensurePlaceableWithSupport(blockPos4, arrayList);
                    if (++n16 > 8 || n == n4 && n2 == n5 && n3 == n6) break;
                    if (n21 >= 0) {
                        n += n10;
                        n21 -= n15;
                    }
                    if (n22 >= 0) {
                        n2 += n11;
                        n22 -= n15;
                    }
                    n3 += n12;
                    n21 += n13;
                    n22 += n14;
                }
            }
        }
        return arrayList;
    }

    private void ensurePlaceableWithSupport(BlockPos blockPos, List<BlockPos> list) {
        if (!ScaffoldModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return;
        }
        Direction direction = WorldUtils.getDirection(blockPos, this.strictDirection.getValue());
        if (direction != null) {
            if (!list.contains(blockPos) && this.isWithinRange(blockPos)) {
                list.add(blockPos);
            }
            return;
        }
        BlockPos blockPos2 = this.getSupportingBlock(blockPos);
        if (blockPos2 != null) {
            if (!list.contains(blockPos2) && this.isWithinRange(blockPos2)) {
                list.add(blockPos2);
            }
        } else {
            BlockPos blockPos3 = blockPos.below();
            int n = 0;
            while (n++ < 3 && ScaffoldModule.mc.level.getBlockState(blockPos3).canBeReplaced()) {
                if (WorldUtils.getDirection(blockPos3, this.strictDirection.getValue()) != null) {
                    if (list.contains(blockPos3) || !this.isWithinRange(blockPos3)) break;
                    list.add(blockPos3);
                    break;
                }
                blockPos3 = blockPos3.below();
            }
        }
        if (!list.contains(blockPos) && this.isWithinRange(blockPos)) {
            list.add(blockPos);
        }
    }

    private BlockPos getSupportingBlock(BlockPos blockPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (WorldUtils.getDirection(blockPos2, this.strictDirection.getValue()) == null) continue;
            return blockPos2;
        }
        return null;
    }

    private boolean isWithinRange(BlockPos blockPos) {
        return ScaffoldModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= this.range.getValue().doubleValue() * this.range.getValue().doubleValue();
    }
}

