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
        int maxSlot = this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8;
        int blockSlot = this.findValidBlockSlot(0, maxSlot);
        if (blockSlot == -1) {
            Input input = ScaffoldModule.mc.player.input != null ? ScaffoldModule.mc.player.input.keyPresses : Input.EMPTY;
            if (!input.shift()) {
                ScaffoldModule.mc.player.input.keyPresses = new Input(input.forward(), input.backward(), input.left(), input.right(), input.jump(), true, input.sprint());
            }
            return;
        }
        if (this.tower.getValue() && ScaffoldModule.mc.options.keyJump.isDown() && !this.isMovingHorizontally()) {
            ScaffoldModule.mc.player.setDeltaMovement(ScaffoldModule.mc.player.getDeltaMovement().x, this.towerSpeed.getValue().doubleValue(), ScaffoldModule.mc.player.getDeltaMovement().z);
        }
        int targetY = (int)Math.floor(ScaffoldModule.mc.player.getY());
        if (this.keepY.getValue() && this.isMovingHorizontally()) {
            if (ScaffoldModule.mc.player.onGround() || this.groundPosY < ScaffoldModule.mc.level.getMinY()) {
                this.groundPosY = targetY;
            }
            targetY = this.groundPosY;
        } else if (this.downwards.getValue() && ScaffoldModule.mc.options.keyShift.isDown()) {
            targetY = ScaffoldModule.mc.player.getBlockY() - 1;
        } else {
            this.groundPosY = targetY;
        }
        BlockPos playerPos = new BlockPos(ScaffoldModule.mc.player.getBlockX(), targetY, ScaffoldModule.mc.player.getBlockZ());
        BlockPos belowPos = playerPos.below();
        List<BlockPos> placements = this.getScaffoldPlacements(belowPos);
        if (placements.isEmpty()) {
            return;
        }
        int previousSlot = ScaffoldModule.mc.player.getInventory().getSelectedSlot();
        if (!InventoryUtils.switchSlot(this.autoSwitch.getValue(), blockSlot, previousSlot)) {
            return;
        }
        for (BlockPos placePos : placements) {
            if (!ScaffoldModule.mc.level.getBlockState(placePos).canBeReplaced() || this.isOnCooldown(placePos)) continue;
            Direction direction = WorldUtils.getDirection(placePos, this.strictDirection.getValue());
            if (direction == null) {
                if (!this.airPlace.getValue()) continue;
                WorldUtils.airPlaceBlock(placePos, InteractionHand.MAIN_HAND, this.rotate.getValue() ? "Normal" : "None", this.render.getValue());
                this.lastPlacement = placePos;
                this.predictCollision(placePos);
                this.markAttempted(placePos);
                continue;
            }
            if (!WorldUtils.placeBlock(placePos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), false, this.render.getValue())) continue;
            this.lastPlacement = placePos;
            this.predictCollision(placePos);
            this.markAttempted(placePos);
        }
        InventoryUtils.switchBack(this.autoSwitch.getValue(), blockSlot, previousSlot);
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
        Long lastAttemptMs = this.pendingAttempts.get(blockPos);
        if (lastAttemptMs == null) {
            return false;
        }
        long cooldownMs = Math.max(50L, (long)Night.SERVER_MANAGER.getPing() * 2L);
        if (System.currentTimeMillis() - lastAttemptMs < cooldownMs) {
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

    private int findValidBlockSlot(int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; ++i) {
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
        ArrayList<BlockPos> placements = new ArrayList<BlockPos>();
        if (this.airPlace.getValue()) {
            if (this.isWithinRange(blockPos)) {
                placements.add(blockPos);
            }
            return placements;
        }
        this.ensurePlaceableWithSupport(blockPos, placements);
        if (this.lastPlacement != null) {
            int curX = this.lastPlacement.getX();
            int curY = this.lastPlacement.getY();
            int curZ = this.lastPlacement.getZ();
            int targetX = blockPos.getX();
            int targetY = blockPos.getY();
            int targetZ = blockPos.getZ();
            int dx = targetX - curX;
            int dy = targetY - curY;
            int dz = targetZ - curZ;
            int stepX = Integer.compare(dx, 0);
            int stepY = Integer.compare(dy, 0);
            int stepZ = Integer.compare(dz, 0);
            dx = Math.abs(dx);
            dy = Math.abs(dy);
            dz = Math.abs(dz);
            int dx2 = dx << 1;
            int dy2 = dy << 1;
            int dz2 = dz << 1;
            int steps = 0;
            if (dx >= dy && dx >= dz) {
                int err1 = dy2 - dx;
                int err2 = dz2 - dx;
                while (true) {
                    BlockPos stepPos = new BlockPos(curX, curY, curZ);
                    this.ensurePlaceableWithSupport(stepPos, placements);
                    if (++steps <= 8 && (curX != targetX || curY != targetY || curZ != targetZ)) {
                        if (err1 >= 0) {
                            curY += stepY;
                            err1 -= dx2;
                        }
                        if (err2 >= 0) {
                            curZ += stepZ;
                            err2 -= dx2;
                        }
                        curX += stepX;
                        err1 += dy2;
                        err2 += dz2;
                        continue;
                    }
                    break;
                }
            } else if (dy >= dx && dy >= dz) {
                int err1 = dx2 - dy;
                int err2 = dz2 - dy;
                while (true) {
                    BlockPos stepPos = new BlockPos(curX, curY, curZ);
                    this.ensurePlaceableWithSupport(stepPos, placements);
                    if (++steps <= 8 && (curX != targetX || curY != targetY || curZ != targetZ)) {
                        if (err1 >= 0) {
                            curX += stepX;
                            err1 -= dy2;
                        }
                        if (err2 >= 0) {
                            curZ += stepZ;
                            err2 -= dy2;
                        }
                        curY += stepY;
                        err1 += dx2;
                        err2 += dz2;
                        continue;
                    }
                    break;
                }
            } else {
                int err1 = dx2 - dz;
                int err2 = dy2 - dz;
                while (true) {
                    BlockPos stepPos = new BlockPos(curX, curY, curZ);
                    this.ensurePlaceableWithSupport(stepPos, placements);
                    if (++steps > 8 || curX == targetX && curY == targetY && curZ == targetZ) break;
                    if (err1 >= 0) {
                        curX += stepX;
                        err1 -= dz2;
                    }
                    if (err2 >= 0) {
                        curY += stepY;
                        err2 -= dz2;
                    }
                    curZ += stepZ;
                    err1 += dx2;
                    err2 += dy2;
                }
            }
        }
        return placements;
    }

    private void ensurePlaceableWithSupport(BlockPos pos, List<BlockPos> placements) {
        if (!ScaffoldModule.mc.level.getBlockState(pos).canBeReplaced()) {
            return;
        }
        Direction direction = WorldUtils.getDirection(pos, this.strictDirection.getValue());
        if (direction != null) {
            if (!placements.contains(pos) && this.isWithinRange(pos)) {
                placements.add(pos);
            }
            return;
        }
        BlockPos supportPos = this.getSupportingBlock(pos);
        if (supportPos != null) {
            if (!placements.contains(supportPos) && this.isWithinRange(supportPos)) {
                placements.add(supportPos);
            }
        } else {
            BlockPos belowPos = pos.below();
            int depth = 0;
            while (depth++ < 3 && ScaffoldModule.mc.level.getBlockState(belowPos).canBeReplaced()) {
                if (WorldUtils.getDirection(belowPos, this.strictDirection.getValue()) != null) {
                    if (placements.contains(belowPos) || !this.isWithinRange(belowPos)) break;
                    placements.add(belowPos);
                    break;
                }
                belowPos = belowPos.below();
            }
        }
        if (!placements.contains(pos) && this.isWithinRange(pos)) {
            placements.add(pos);
        }
    }

    private BlockPos getSupportingBlock(BlockPos blockPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = blockPos.relative(direction);
            if (WorldUtils.getDirection(neighborPos, this.strictDirection.getValue()) == null) continue;
            return neighborPos;
        }
        return null;
    }

    private boolean isWithinRange(BlockPos blockPos) {
        return ScaffoldModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= this.range.getValue().doubleValue() * this.range.getValue().doubleValue();
    }
}

