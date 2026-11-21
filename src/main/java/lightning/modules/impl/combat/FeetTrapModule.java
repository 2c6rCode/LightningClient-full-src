/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.arrow.Arrow
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FeetTrapModule
extends Module {
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public NumberSetting limit = new NumberSetting("Limit", "The number of blocks that can be placed per tick.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The amount of ticks waited between each group of placements.", 0, 0, 20);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which the blocks will be placed at.", 5.0, 0.0, 12.0);
    public BooleanSetting extension = new BooleanSetting("Extension", "Extends the trap if there are entities obstructing block placement.", false);
    public BooleanSetting floor = new BooleanSetting("Floor", "Places blocks under your feet as well.", true);
    public BooleanSetting corners = new BooleanSetting("Corners", "Also covers the four diagonals at feet level.", false);
    public BooleanSetting jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever you leave the ground.", false);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing blocks.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", true);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Places blocks in the air using the block below as support, facing UP, when no direct neighbor is available.", false);
    public BooleanSetting grim = new BooleanSetting("Grim", "AirPlace only: places from the offhand via a swap-and-swapback trick instead of the mainhand.", new BooleanSetting.Visibility(this.airPlace, true), false);
    public BooleanSetting multiTask = new BooleanSetting("MultiTask", "Allows placing while an item is already in use (eating, blocking, drawing a bow).", false);
    public BooleanSetting swing = new BooleanSetting("Swing", "Sends a swing packet whenever placing a block.", true);
    public BooleanSetting antiBreak = new BooleanSetting("AntiBreak", "Pre-places extra blocks around a spot an enemy is actively digging through toward your trap.", false);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module once it is finished with placing.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    public BooleanSetting attack = new BooleanSetting("Attack", "Attacks any crystal that lands on one of your trap positions.", false);
    public BooleanSetting attackRotate = new BooleanSetting("AttackRotate", "Rotate", "Rotates toward the crystal before attacking it.", new BooleanSetting.Visibility(this.attack, true), true);
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum range at which crystals will be attacked.", new BooleanSetting.Visibility(this.attack, true), Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting attackAge = new NumberSetting("AttackAge", "Age", "The minimum age (in ticks) a crystal must reach before being attacked.", new BooleanSetting.Visibility(this.attack, true), 5, 0, 20);
    public BooleanSetting attackMultiTask = new BooleanSetting("AttackMultiTask", "Multitask", "Allows attacking while an item is already in use.", new BooleanSetting.Visibility(this.attack, true), true);
    public BooleanSetting attackSwing = new BooleanSetting("AttackSwing", "Swing", "Sends a swing packet whenever attacking a crystal.", new BooleanSetting.Visibility(this.attack, true), true);
    private int ticks = 0;
    private int targetCount = 0;
    private Set<BlockPos> targetPositions = new LinkedHashSet<BlockPos>();
    private final Map<Integer, BreakState> breakStates = new HashMap<Integer, BreakState>();

    private float getMiningSpeed(BlockState blockState, Player player) {
        ItemStack itemStack = new ItemStack((ItemLike)Items.NETHERITE_PICKAXE);
        float f = itemStack.getDestroySpeed(blockState);
        if (f > 1.0f) {
            int n = 5;
            f += (float)(n * n + 1);
        }
        if (!player.onGround()) {
            f /= 5.0f;
        }
        return f;
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.targetCount);
    }

    @Override
    public void onDisable() {
        this.ticks = 0;
        this.targetCount = 0;
        this.targetPositions.clear();
        this.breakStates.clear();
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent playerJumpEvent) {
        if (this.jumpDisable.getValue()) {
            this.setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (!this.antiBreak.getValue() || FeetTrapModule.mc.level == null) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (!(packet instanceof ClientboundBlockDestructionPacket)) {
            return;
        }
        ClientboundBlockDestructionPacket clientboundBlockDestructionPacket = (ClientboundBlockDestructionPacket)packet;
        if (!(FeetTrapModule.mc.level.getEntity(clientboundBlockDestructionPacket.getId()) instanceof Player)) {
            return;
        }
        this.breakStates.computeIfAbsent(clientboundBlockDestructionPacket.getId(), n -> new BreakState(this, this, (int)n)).startBreak(clientboundBlockDestructionPacket.getPos(), 0.7f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        BlockItem blockItem;
        Item item;
        if (FeetTrapModule.mc.player == null || FeetTrapModule.mc.level == null) {
            return;
        }
        if (this.jumpDisable.getValue() && !FeetTrapModule.mc.player.onGround()) {
            this.setToggled(false);
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        if (!(!this.autoSwitch.getValue().equalsIgnoreCase("None") || (item = FeetTrapModule.mc.player.getMainHandItem().getItem()) instanceof BlockItem && this.isBlastProof((blockItem = (BlockItem)item).getBlock()))) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any blast-proof blocks.", this.getName());
            this.setToggled(false);
            return;
        }
        int n = this.findBlastProofBlock();
        int n2 = FeetTrapModule.mc.player.getInventory().getSelectedSlot();
        if (n == -1) {
            Night.CHAT_MANAGER.tagged("No blast-proof blocks could be found in your hotbar.", this.getName());
            this.setToggled(false);
            return;
        }
        this.targetPositions = this.getTargets();
        if (this.antiBreak.getValue()) {
            this.applyAntiBreak();
        }
        List<BlockPos> list = this.targetPositions.stream().filter(blockPos -> FeetTrapModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= Mth.square((double)this.range.getValue().doubleValue())).filter(WorldUtils::isPlaceable).toList();
        this.targetCount = list.size();
        if (this.attack.getValue()) {
            this.attackTrapCrystals();
        }
        if (list.isEmpty()) {
            if (this.selfDisable.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        if (!this.multiTask.getValue() && FeetTrapModule.mc.player.isUsingItem()) {
            return;
        }
        Night.ROTATION_MANAGER.beginBatchRotation();
        try {
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, n2);
            int n3 = 0;
            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
            for (BlockPos blockPos2 : list) {
                if (n3 >= this.limit.getValue().intValue()) break;
                Direction direction = WorldUtils.getDirection(blockPos2, arrayList, this.strictDirection.getValue());
                if (direction == null) {
                    if (!this.airPlace.getValue()) continue;
                    this.placeAir(blockPos2);
                    arrayList.add(blockPos2);
                    ++n3;
                    continue;
                }
                if (!WorldUtils.placeBlock(blockPos2, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                arrayList.add(blockPos2);
                ++n3;
            }
            InventoryUtils.switchBack(this.autoSwitch.getValue(), n, n2);
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
        this.ticks = 0;
    }

    private void placeAir(BlockPos blockPos) {
        RenderPosition renderPosition;
        if (!this.grim.getValue()) {
            WorldUtils.airPlaceBlock(blockPos, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
            return;
        }
        Vec3 vec3 = Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.5, 0.0);
        if (!this.rotate.getValue().equalsIgnoreCase("None")) {
            float[] rotations = RotationUtils.getRotations(vec3);
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
            } else {
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
            }
        }
        if (this.render.getValue() && !Night.RENDER_MANAGER.renderPositions.contains(renderPosition = new RenderPosition(blockPos))) {
            Night.RENDER_MANAGER.renderPositions.add(renderPosition);
        }
        BlockHitResult blockHitResult = new BlockHitResult(vec3, Direction.UP, blockPos, false);
        mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        NetworkUtils.sendSequencedPacket(arg_0 -> FeetTrapModule.lambda$placeAir$0(blockHitResult, arg_0));
        if (this.swing.getValue()) {
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.OFF_HAND));
        }
        mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
    }

    private void attackTrapCrystals() {
        Night.ROTATION_MANAGER.beginBatchRotation();
        for (EndCrystal endCrystal : FeetTrapModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(FeetTrapModule.mc.player.blockPosition()).inflate(this.range.getValue().doubleValue() + 3.0))) {
            if (!endCrystal.isAlive() || endCrystal.isRemoved() || endCrystal.tickCount < this.attackAge.getValue().intValue()) continue;
            AABB aABB = endCrystal.getBoundingBox();
            boolean bl = false;
            for (BlockPos blockPos : this.targetPositions) {
                if (!FeetTrapModule.mc.level.getBlockState(blockPos).canBeReplaced() || !new AABB(blockPos).intersects(aABB)) continue;
                bl = true;
                break;
            }
            if (!bl || aABB.distanceToSqr(FeetTrapModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !this.attackMultiTask.getValue() && FeetTrapModule.mc.player.isUsingItem()) continue;
            if (this.attackRotate.getValue()) {
                float[] object = RotationUtils.getRotations(endCrystal.getBoundingBox().getCenter());
                Night.ROTATION_MANAGER.silentRotate((float)object[0], (float)object[1]);
                boolean bl2 = aABB.contains(FeetTrapModule.mc.player.getEyePosition());
                if (!bl2 && !WorldUtils.canSee(endCrystal.getBoundingBox().getCenter())) continue;
            }
            mc.getConnection().send((Packet)new ServerboundAttackPacket(endCrystal.getId()));
            if (!this.attackSwing.getValue()) continue;
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        Night.ROTATION_MANAGER.endBatchRotation();
    }

    private void applyAntiBreak() {
        for (BreakState object : this.breakStates.values()) {
            object.onTick();
        }
        if (!this.antiBreak.getValue() || this.targetPositions.isEmpty()) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (BlockPos blockPos : this.targetPositions) {
            boolean bl = false;
            for (BreakState breakState : this.breakStates.values()) {
                if (!breakState.isBreaking(blockPos)) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
            for (Direction direction : Direction.values()) {
                BlockPos blockPos2;
                if (direction == Direction.DOWN || this.targetPositions.contains(blockPos2 = blockPos.relative(direction)) || arrayList.contains(blockPos2) || !FeetTrapModule.mc.level.getBlockState(blockPos2).canBeReplaced() || this.isEntityBlocking(blockPos2)) continue;
                arrayList.add(blockPos2);
            }
        }
        this.targetPositions.addAll(arrayList);
    }

    private boolean isEntityBlocking(BlockPos blockPos) {
        AABB aABB = new AABB(blockPos);
        for (Entity entity : FeetTrapModule.mc.level.entitiesForRendering()) {
            if (entity.distanceToSqr((Entity)FeetTrapModule.mc.player) > 10.0 || entity instanceof EndCrystal || entity instanceof ItemEntity || entity instanceof Arrow || !entity.getBoundingBox().intersects(aABB)) continue;
            return true;
        }
        return false;
    }

    private Set<BlockPos> getTargets() {
        LinkedHashSet<BlockPos> linkedHashSet = new LinkedHashSet<BlockPos>(HoleUtils.getFeetPositions((Player)FeetTrapModule.mc.player, this.extension.getValue(), this.floor.getValue(), false));
        if (!this.corners.getValue()) {
            return linkedHashSet;
        }
        AABB aABB = FeetTrapModule.mc.player.getBoundingBox();
        int n = Mth.floor((double)FeetTrapModule.mc.player.getY());
        int n2 = Mth.floor((double)aABB.minX);
        while ((double)n2 < Math.ceil(aABB.maxX)) {
            int n3 = Mth.floor((double)aABB.minZ);
            while ((double)n3 < Math.ceil(aABB.maxZ)) {
                BlockPos blockPos = new BlockPos(n2, n, n3);
                for (BlockPos blockPos2 : new BlockPos[]{blockPos.north().east(), blockPos.north().west(), blockPos.south().east(), blockPos.south().west()}) {
                    if (this.isEntityBlocking(blockPos2)) continue;
                    linkedHashSet.add(blockPos2);
                }
                ++n3;
            }
            ++n2;
        }
        return linkedHashSet;
    }

    private int findBlastProofBlock() {
        float f = -1.0f;
        int n = -1;
        for (int i = 0; i <= 8; ++i) {
            BlockItem blockItem;
            Block block;
            Item item = FeetTrapModule.mc.player.getInventory().getItem(i).getItem();
            if (!(item instanceof BlockItem) || !this.isBlastProof(block = (blockItem = (BlockItem)item).getBlock())) continue;
            float f2 = block.defaultDestroyTime();
            if (f2 == -1.0f) {
                return i;
            }
            if (!(f2 > f)) continue;
            f = f2;
            n = i;
        }
        return n;
    }

    private boolean isBlastProof(Block block) {
        return block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ANCIENT_DEBRIS || block == Blocks.ENCHANTING_TABLE || block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL;
    }

    private static /* synthetic */ Packet lambda$placeAir$0(BlockHitResult blockHitResult, int n) {
        return new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND, blockHitResult, n);
    }

    private class BreakState {
        final int entityId;
        final BreakTask current;
        final BreakTask doubleMine;
        final FeetTrapModule this$0;

        BreakState(FeetTrapModule feetTrapModule, FeetTrapModule feetTrapModule2, int n) {
            Objects.requireNonNull(feetTrapModule);
            FeetTrapModule feetTrapModule3 = feetTrapModule2;
            Objects.requireNonNull(feetTrapModule3);
            this.this$0 = feetTrapModule3;
            this.current = new BreakTask();
            this.doubleMine = new BreakTask();
            this.entityId = n;
        }

        void startBreak(BlockPos blockPos, float f) {
            if (this.current.active && !this.current.pos.equals((Object)blockPos)) {
                if (!this.doubleMine.active) {
                    this.doubleMine.copyFrom(this.current);
                    this.doubleMine.targetSpeed = 1.0f;
                }
                this.current.reset();
            }
            if (!this.current.active || !this.current.pos.equals((Object)blockPos)) {
                this.current.start(blockPos, f);
            }
        }

        void onTick() {
            this.tickTask(this.current);
            this.tickTask(this.doubleMine);
        }

        private void tickTask(BreakTask breakTask) {
            if (!breakTask.active) {
                return;
            }
            Entity entity = IMinecraft.mc.level.getEntity(this.entityId);
            if (!(entity instanceof Player)) {
                breakTask.reset();
                return;
            }
            Player player = (Player)entity;
            BlockState blockState = IMinecraft.mc.level.getBlockState(breakTask.pos);
            if (blockState.isAir()) {
                breakTask.active = false;
                return;
            }
            float f = blockState.getDestroySpeed((BlockGetter)IMinecraft.mc.level, breakTask.pos);
            if (f == -1.0f) {
                return;
            }
            float f2 = this.this$0.getMiningSpeed(blockState, player);
            breakTask.progress += f2 / f / 30.0f;
            if (breakTask.progress >= breakTask.targetSpeed) {
                breakTask.active = false;
            }
        }

        boolean isBreaking(BlockPos blockPos) {
            if (this.current.pos != null && this.current.pos.equals((Object)blockPos) && this.current.active) {
                return true;
            }
            return this.doubleMine.pos != null && this.doubleMine.pos.equals((Object)blockPos) && this.doubleMine.active;
        }
    }

    private static class BreakTask {
        BlockPos pos;
        float targetSpeed;
        float progress;
        boolean active;

        private BreakTask() {
        }

        void start(BlockPos blockPos, float f) {
            this.pos = blockPos;
            this.targetSpeed = f;
            this.progress = 0.0f;
            this.active = true;
        }

        void copyFrom(BreakTask breakTask) {
            this.pos = breakTask.pos;
            this.targetSpeed = breakTask.targetSpeed;
            this.progress = breakTask.progress;
            this.active = breakTask.active;
        }

        void reset() {
            this.active = false;
            this.progress = 0.0f;
        }
    }
}

