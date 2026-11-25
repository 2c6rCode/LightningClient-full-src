/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.BannerBlock
 *  net.minecraft.world.level.block.BaseRailBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.CarpetBlock
 *  net.minecraft.world.level.block.FlowerBlock
 *  net.minecraft.world.level.block.FlowerPotBlock
 *  net.minecraft.world.level.block.LadderBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.MushroomBlock
 *  net.minecraft.world.level.block.PressurePlateBlock
 *  net.minecraft.world.level.block.ScaffoldingBlock
 *  net.minecraft.world.level.block.SignBlock
 *  net.minecraft.world.level.block.TallFlowerBlock
 *  net.minecraft.world.level.block.TorchBlock
 *  net.minecraft.world.level.block.TripWireBlock
 *  net.minecraft.world.level.block.TripWireHookBlock
 *  net.minecraft.world.level.block.VineBlock
 *  net.minecraft.world.level.block.WebBlock
 *  net.minecraft.world.level.block.piston.PistonBaseBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.PistonHelper;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@RegisterModule(name="PistonCrystal", description="Places piston + redstone to push a crystal into targets.", category=Module.Category.COMBAT)
public class PistonCrystalModule
extends Module {
    public NumberSetting minDamage = new NumberSetting("MinDamage", "Minimum damage to attempt a sequence", Float.valueOf(6.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public NumberSetting maxLocalDamage = new NumberSetting("MaxLocalDamage", "Max self-damage allowed", Float.valueOf(12.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public NumberSetting delay = new NumberSetting("Delay", "Delay between place actions (ticks)", 0, 0, 10);
    public ModeSetting sort = new ModeSetting("Sort", "How to pick target", "LowestDistance", new String[]{"LowestDistance", "LowestHealth", "CloseAngle"});
    public NumberSetting targetRange = new NumberSetting("TargetRange", "Max target range", Float.valueOf(6.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public BooleanSetting inAirTarget = new BooleanSetting("InAirTarget", "Target players in the air", true);
    public BooleanSetting breakCrystal = new BooleanSetting("BreakCrystal", "Attack crystal after placing it", true);
    public BooleanSetting autoBreak = new BooleanSetting("AutoBreak", "Automatically breaks leftover redstone/piston with pickaxe after crystal explosion.", true);
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "Range to place blocks", Float.valueOf(4.5f), Float.valueOf(0.1f), Float.valueOf(6.0f));
    public NumberSetting breakRange = new NumberSetting("BreakRange", "Range to break crystals", Float.valueOf(4.5f), Float.valueOf(0.1f), Float.valueOf(6.0f));
    public ModeSetting rotate = new ModeSetting("Rotate", "Silently rotate to placed blocks", "Silent", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting grim = new BooleanSetting("Grim", "YawDeceive - filter dir by player facing & override piston yaw", true);
    public ModeSetting swapAction = new ModeSetting("Swap", "How to swap to required items", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Allow placing when not on ground", false);
    public BooleanSetting safety = new BooleanSetting("Safety", "Skip if crystal would kill self", true);
    public BooleanSetting blockDestruction = new BooleanSetting("BlockDestruction", "Account for explosion block destruction", false);
    public BooleanSetting selfExtrapolate = new BooleanSetting("SelfExtrapolate", "Extrapolate self position for self-damage calc", false);
    public NumberSetting extrapolationTicks = new NumberSetting("ExtrapolationTicks", "Ticks to extrapolate enemy position", 0, 0, 10);
    public BooleanSetting assumeBestArmor = new BooleanSetting("AssumeBestArmor", "Assume target has Prot 4 netherite", false);
    public BooleanSetting autoSupport = new BooleanSetting("AutoSupport", "Automatically places obsidian support blocks when no platform exists.", true);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses the module while eating or using an item.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Use raytrace to verify line-of-sight before placing piston/redstone (required on servers that check raytrace).", false);
    private BlockPos pistonPos;
    private BlockPos redstonePos;
    private BlockPos crystalPos;
    private boolean crystalForcedPush;
    private BlockPos crystalSupportPos;
    private BlockPos pistonSupportPos;
    private BlockPos lastPiston;
    private BlockPos lastRedstone;
    private final Set<BlockPos> placedRedstone = new LinkedHashSet<BlockPos>();
    private final Set<BlockPos> placedPistons = new LinkedHashSet<BlockPos>();
    private int preferredHeight = -1;
    private Direction face;
    private boolean usingTorch;
    private Player target;
    private long lastActionMs = 0L;
    private long breakTimerMs = 0L;
    private int preSwapSlot = -1;

    @Override
    public void onEnable() {
        this.resetState();
        this.placedRedstone.clear();
        this.placedPistons.clear();
        this.breakTimerMs = System.currentTimeMillis() + 99999999L;
    }

    @Override
    public void onDisable() {
        this.resetState();
        this.placedRedstone.clear();
        this.placedPistons.clear();
    }

    /*
     * Unable to fully structure code
     */
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null || (this.pauseOnEat.getValue() && EntityUtils.isEating())) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastActionMs < this.delay.getValue().longValue() * 50L) {
            return;
        }
        int crystalSlot = this.findItem(Items.END_CRYSTAL);
        int pistonSlot = this.findPiston();
        int redstoneSlot = this.findRedstone();
        if (crystalSlot == -1 || pistonSlot == -1 || redstoneSlot == -1) {
            this.resetState();
            return;
        }
        this.target = this.pickTarget();
        if (this.target == null) {
            this.resetState();
            return;
        }
        if (this.pistonPos != null && this.redstonePos != null && this.crystalPos != null) {
            boolean active = mc.level.getBlockState(this.pistonPos).getBlock() instanceof PistonBaseBlock &&
                (mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_BLOCK || mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_TORCH);
            if (!active) {
                boolean air = this.airPlace.getValue();
                boolean canPiston = PistonHelper.canPlace(this.pistonPos, null, air, true) ||
                    mc.level.getBlockState(this.pistonPos).getBlock() instanceof PistonBaseBlock ||
                    (!air && this.pistonSupportPos != null && (PistonHelper.canPlace(this.pistonSupportPos, null, air, true) || this.isSolidBlock(this.pistonSupportPos)));
                boolean canRedstone = PistonHelper.canPlace(this.redstonePos, null, air, true) ||
                    mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_BLOCK ||
                    mc.level.getBlockState(this.redstonePos).getBlock() == Blocks.REDSTONE_TORCH ||
                    this.redstonePos.equals(this.pistonPos.above()) ||
                    this.redstonePos.equals(this.pistonPos.relative(this.face));
                boolean canCrystal = (!this.crystalForcedPush ? PistonHelper.canPlaceCrystal(this.crystalPos, air) : PistonHelper.canPlaceCrystalIgnoreHeadroom(this.crystalPos, air))
                    || (this.crystalSupportPos != null && (PistonHelper.canPlace(this.crystalSupportPos, null, false, true) || this.isObsidian(this.crystalSupportPos)));

                if (!canPiston && !air && this.autoSupport.getValue() && this.pistonSupportPos == null) {
                    BlockPos repair = this.findPistonSupportRepair(this.pistonPos, this.face);
                    if (repair != null) {
                        this.pistonSupportPos = repair;
                        canPiston = true;
                    }
                }
                if (!canCrystal && this.autoSupport.getValue() && this.crystalSupportPos == null && this.canSupportCrystal(this.crystalPos, this.crystalPos.below())) {
                    this.crystalSupportPos = this.crystalPos.below();
                    canCrystal = true;
                }
                if (!(canPiston && canRedstone && canCrystal)) {
                    this.resetState();
                }
            }
        }
        if (this.pistonPos == null && this.crystalPos == null && this.redstonePos == null) {
            this.doPistonCrystal(this.target);
        }
        if (this.autoSupport.getValue() && this.crystalSupportPos != null && !this.isObsidian(this.crystalSupportPos) && PistonHelper.canPlace(this.crystalSupportPos, null, false, true)) {
            int supportSlot = this.findSupportBlock();
            if (supportSlot != -1) {
                if (!this.placeBlock(supportSlot, this.crystalSupportPos, null)) {
                    this.resetState();
                    return;
                }
                this.lastActionMs = now;
                return;
            }
        }
        if (!this.airPlace.getValue() && this.autoSupport.getValue() && this.pistonSupportPos != null && !this.isSolidBlock(this.pistonSupportPos) && PistonHelper.canPlace(this.pistonSupportPos, null, false, true)) {
            int supportSlot = this.findSupportBlock();
            if (supportSlot != -1) {
                if (!this.placeBlock(supportSlot, this.pistonSupportPos, null)) {
                    this.resetState();
                    return;
                }
                this.lastActionMs = now;
                return;
            }
        }
        if (this.pistonPos != null && PistonHelper.canPlace(this.pistonPos, null, this.airPlace.getValue(), true)) {
            boolean placed = this.placeBlock(pistonSlot, this.pistonPos, this.face);
            if (!placed && this.strictDirection.getValue()) {
                this.resetState();
                return;
            }
            this.lastPiston = this.pistonPos;
            this.placedPistons.add(this.pistonPos);
            this.lastActionMs = now;
        }
        if (this.redstonePos != null && PistonHelper.canPlace(this.redstonePos, null, this.airPlace.getValue(), true)) {
            if (this.usingTorch) {
                this.placeTorch(redstoneSlot, this.redstonePos);
            } else {
                this.placeBlock(redstoneSlot, this.redstonePos, null);
            }
            this.lastRedstone = this.redstonePos;
            this.placedRedstone.add(this.redstonePos);
            this.lastActionMs = now;
        }
        boolean canPlaceCryst = this.crystalForcedPush ? PistonHelper.canPlaceCrystalIgnoreHeadroom(this.crystalPos, this.airPlace.getValue()) : PistonHelper.canPlaceCrystal(this.crystalPos, this.airPlace.getValue());
        if (this.crystalPos == null || !canPlaceCryst) {
            if (this.breakCrystal.getValue() && now - this.breakTimerMs >= this.delay.getValue().longValue() * 50L) {
                for (int y = -1; y <= 4; ++y) {
                    BlockPos checkPos = this.target.blockPosition().above(y);
                    if (!PistonHelper.hasCrystal(checkPos) || (this.crystalPos != null && this.crystalPos.equals(checkPos))) continue;
                    this.attackCrystalAt(checkPos);
                    this.lastActionMs = now;
                    this.breakTimerMs = now;
                    this.resetState();
                    return;
                }
            }
            SpeedMineModule speedMine = Night.MODULE_MANAGER != null ? (SpeedMineModule)Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
            boolean digging = speedMine != null && speedMine.isToggled() && speedMine.primaryDigging();
            boolean justActed = this.lastActionMs == now;
            if (this.autoBreak.getValue() && !digging && !justActed) {
                Iterator<BlockPos> it = this.placedRedstone.iterator();
                while (it.hasNext()) {
                    BlockPos redstoneBlock = it.next();
                    if (!this.isRedstone(redstoneBlock)) {
                        it.remove();
                        continue;
                    }
                    if (redstoneBlock.equals(this.redstonePos) || !this.inRange(redstoneBlock, this.placeRange.getValue().floatValue())) continue;
                    this.breakBlock(redstoneBlock);
                    it.remove();
                    this.lastActionMs = now;
                    return;
                }
                Iterator<BlockPos> pit = this.placedPistons.iterator();
                while (pit.hasNext()) {
                    BlockPos pistonBlock = pit.next();
                    if (!(mc.level.getBlockState(pistonBlock).getBlock() instanceof PistonBaseBlock)) {
                        pit.remove();
                        continue;
                    }
                    if (pistonBlock.equals(this.pistonPos) || !this.inRange(pistonBlock, this.placeRange.getValue().floatValue())) continue;
                    this.breakBlock(pistonBlock);
                    pit.remove();
                    this.lastActionMs = now;
                    return;
                }
            }
            long breakTimeout = (2L + this.delay.getValue().longValue()) * 50L;
            if (this.autoBreak.getValue() && !digging && now - this.lastActionMs >= breakTimeout) {
                Iterator<BlockPos> it = this.placedRedstone.iterator();
                while (it.hasNext()) {
                    BlockPos redstoneBlock = it.next();
                    if (this.isRedstone(redstoneBlock)) {
                        if (!this.inRange(redstoneBlock, this.placeRange.getValue().floatValue())) continue;
                        this.breakBlock(redstoneBlock);
                        it.remove();
                        this.lastActionMs = now;
                        this.resetState();
                        return;
                    }
                    it.remove();
                }
                Iterator<BlockPos> pit = this.placedPistons.iterator();
                while (pit.hasNext()) {
                    BlockPos pistonBlock = pit.next();
                    if (mc.level.getBlockState(pistonBlock).getBlock() instanceof PistonBaseBlock) {
                        if (!this.inRange(pistonBlock, this.placeRange.getValue().floatValue())) continue;
                        this.breakBlock(pistonBlock);
                        pit.remove();
                        this.lastActionMs = now;
                        this.resetState();
                        return;
                    }
                    pit.remove();
                }
            }
        } else {
            this.placeCrystal(crystalSlot, this.crystalPos);
            this.lastActionMs = now;
            this.breakTimerMs = now;
        }
    }

    private void doPistonCrystal(Player player) {
        int[] rawHeightOrder;
        int openHeight;
        if (this.isTargetPhased(player)) {
            return;
        }
        int extrapTicks = this.extrapolationTicks.getValue().intValue();
        double deltaX = (player.getX() - player.xOld) * (double)extrapTicks;
        double deltaY = (player.getY() - player.yOld) * (double)extrapTicks * 0.3;
        double deltaZ = (player.getZ() - player.zOld) * (double)extrapTicks;
        double extrapX = player.getX() + deltaX;
        double extrapY = player.getY() + deltaY;
        double extrapZ = player.getZ() + deltaZ;
        BlockPos blockPos = PistonCrystalModule.getEntityFeetPos((Entity)player, extrapX, extrapY, extrapZ);
        AABB targetBox = player.getBoundingBox().move(deltaX, deltaY, deltaZ);
        AABB selfBox = this.selfExtrapolate.getValue() ? PistonCrystalModule.mc.player.getBoundingBox().move((PistonCrystalModule.mc.player.getX() - PistonCrystalModule.mc.player.xOld) * (double)extrapTicks, (PistonCrystalModule.mc.player.getY() - PistonCrystalModule.mc.player.yOld) * (double)extrapTicks * 0.3, (PistonCrystalModule.mc.player.getZ() - PistonCrystalModule.mc.player.zOld) * (double)extrapTicks) : PistonCrystalModule.mc.player.getBoundingBox();
        boolean isCrawling = player.getPose() == Pose.SWIMMING || player.isVisuallyCrawling() || player.getEyeHeight() <= 1.0f || player.getBoundingBox().getYsize() <= 1.0;
        for (openHeight = 0; openHeight <= 4 && !this.hasHorizontalOpening(blockPos.above(openHeight)); ++openHeight) {
        }
        boolean crawlHeadCovered = isCrawling && PistonCrystalModule.mc.level != null && !PistonCrystalModule.mc.level.getBlockState(blockPos.above(1)).isAir() && !this.hasHorizontalOpening(blockPos.above(0));
        boolean headCovered = !isCrawling && PistonCrystalModule.mc.level != null && !PistonCrystalModule.mc.level.getBlockState(blockPos.above(2)).isAir() && !this.hasHorizontalOpening(blockPos.above(1));
        boolean tallCover = !isCrawling && openHeight >= 2;
        boolean headBlockSolid = !PistonCrystalModule.mc.level.getBlockState(blockPos.above(2)).isAir();
        boolean headNotOpen = !this.hasHorizontalOpening(blockPos.above(2));
        boolean midOpenUnderHead = !isCrawling && PistonCrystalModule.mc.level != null && (headBlockSolid || headNotOpen) && this.hasHorizontalOpening(blockPos.above(1));
        if (crawlHeadCovered) {
            rawHeightOrder = new int[]{1, 0, 2};
        } else if (openHeight >= 3) {
            rawHeightOrder = new int[]{openHeight, openHeight - 1, 2, 1, 0};
        } else if (headCovered || openHeight == 2) {
            rawHeightOrder = new int[]{2, 1, 0, 3};
        } else if (midOpenUnderHead) {
            rawHeightOrder = new int[]{1, 0, 2, 3};
        } else {
            rawHeightOrder = new int[]{0, 1, 2, 3};
        }
        int[] heightOrder = rawHeightOrder;
        boolean hasPreferred = false;
        for (int candidateHeight : heightOrder) {
            if (candidateHeight != this.preferredHeight) continue;
            hasPreferred = true;
            break;
        }
        if (this.preferredHeight != -1 && hasPreferred && this.preferredHeight != heightOrder[0]) {
            int[] reordered = new int[heightOrder.length];
            reordered[0] = this.preferredHeight;
            int idx = 1;
            for (int height : heightOrder) {
                if (height == this.preferredHeight) continue;
                reordered[idx++] = height;
            }
            heightOrder = reordered;
        }
        for (int heightOffset : heightOrder) {
            BlockPos crystalTargetPos = blockPos.above(heightOffset);
            BlockPos ignoredPos = crawlHeadCovered && heightOffset == 1 || midOpenUnderHead && heightOffset == 1 ? blockPos.above(1) : (!(!headCovered && !tallCover || heightOffset != 2 && heightOffset != openHeight) ? blockPos.above(heightOffset) : null);
            float targetDamage = DamageUtils.getCrystalDamage((Entity)player, targetBox, crystalTargetPos, ignoredPos, this.blockDestruction.getValue());
            float selfDamage = DamageUtils.getCrystalDamage((Entity)PistonCrystalModule.mc.player, selfBox, crystalTargetPos, null, this.blockDestruction.getValue());
            boolean forcePush = heightOffset == 1 && crawlHeadCovered || heightOffset == 1 && midOpenUnderHead || heightOffset == 2 && headCovered || heightOffset == openHeight && tallCover;
            if (forcePush) {
                if (targetDamage <= this.minDamage.getValue().floatValue() || selfDamage > this.maxLocalDamage.getValue().floatValue() || this.safety.getValue() && selfDamage >= PistonCrystalModule.mc.player.getHealth() + PistonCrystalModule.mc.player.getAbsorptionAmount()) continue;
                if (!this.trySearch(blockPos, heightOffset, true)) {
                    this.tryPreBuildCrystalSupport(blockPos, heightOffset);
                    continue;
                }
                this.preferredHeight = heightOffset;
                return;
            }
            if (targetDamage <= this.minDamage.getValue().floatValue() || selfDamage > this.maxLocalDamage.getValue().floatValue() || this.safety.getValue() && selfDamage >= PistonCrystalModule.mc.player.getHealth() + PistonCrystalModule.mc.player.getAbsorptionAmount()) continue;
            if (!this.trySearch(blockPos, heightOffset, headCovered || crawlHeadCovered || tallCover)) {
                if (!this.tryPreBuildCrystalSupport(blockPos, heightOffset)) continue;
                return;
            }
            this.preferredHeight = heightOffset;
            return;
        }
    }

    private boolean tryPreBuildCrystalSupport(BlockPos blockPos, int heightOffset) {
        if (!this.autoSupport.getValue()) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos supportPos;
            BlockPos crystalBasePos = blockPos.relative(direction).above(heightOffset);
            if (!this.inRange(crystalBasePos, this.placeRange.getValue().floatValue()) || (PistonHelper.canPlaceCrystal(crystalBasePos, false) ? !this.inRange(supportPos = crystalBasePos.relative(direction).below(), this.placeRange.getValue().floatValue()) || !PistonHelper.canPlace(supportPos, null, false, true) : this.isObsidian(supportPos = crystalBasePos.below()) || !this.canSupportCrystal(crystalBasePos, supportPos))) continue;
            int supportSlot = this.findSupportBlock();
            if (supportSlot == -1) {
                return false;
            }
            this.placeBlock(supportSlot, supportPos, null);
            this.lastActionMs = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static BlockPos getEntityFeetPos(Entity entity, double x, double y, double z) {
        VoxelShape voxelShape;
        if (PistonCrystalModule.mc.level == null) {
            return new BlockPos(Mth.floor((double)x), Mth.floor((double)y), Mth.floor((double)z));
        }
        int floorY = Mth.floor((double)y);
        BlockPos blockPos = new BlockPos(Mth.floor((double)x), floorY, Mth.floor((double)z));
        BlockState blockState = PistonCrystalModule.mc.level.getBlockState(blockPos);
        if (!(blockState.isAir() || blockState.canBeReplaced() || (voxelShape = blockState.getCollisionShape((BlockGetter)PistonCrystalModule.mc.level, blockPos)).isEmpty())) {
            double topY = (double)blockPos.getY() + voxelShape.max(Direction.Axis.Y);
            if (y >= topY - 0.05) {
                return blockPos.above();
            }
        }
        return blockPos;
    }

    private boolean isTargetPhased(Player player) {
        if (PistonCrystalModule.mc.level == null || player == null) {
            return false;
        }
        BlockPos blockPos = PistonCrystalModule.getEntityFeetPos((Entity)player, player.getX(), player.getY(), player.getZ());
        BlockState blockState = PistonCrystalModule.mc.level.getBlockState(blockPos);
        return this.isHardPhasedBlock(blockState);
    }

    private boolean hasHorizontalOpening(BlockPos blockPos) {
        if (PistonCrystalModule.mc.level == null) {
            return true;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState blockState = PistonCrystalModule.mc.level.getBlockState(blockPos.relative(direction));
            if (!blockState.isAir() && !blockState.canBeReplaced()) continue;
            return true;
        }
        return false;
    }

    private boolean isDecoyOrPassableBlock(BlockState blockState) {
        if (blockState == null || blockState.isAir() || blockState.canBeReplaced()) {
            return true;
        }
        Block block = blockState.getBlock();
        return block instanceof WebBlock || block instanceof ButtonBlock || block instanceof MushroomBlock || block instanceof LadderBlock || block instanceof ScaffoldingBlock || block instanceof FlowerBlock || block instanceof FlowerPotBlock || block instanceof TallFlowerBlock || block instanceof VineBlock || block instanceof TorchBlock || block instanceof LeverBlock || block instanceof PressurePlateBlock || block instanceof SignBlock || block instanceof BannerBlock || block instanceof CarpetBlock || block instanceof TripWireBlock || block instanceof TripWireHookBlock || block instanceof BaseRailBlock || block == Blocks.COBWEB || block == Blocks.LADDER || block == Blocks.SCAFFOLDING || block == Blocks.VINE || block == Blocks.BROWN_MUSHROOM || block == Blocks.RED_MUSHROOM || block == Blocks.TORCH || block == Blocks.SOUL_TORCH || block == Blocks.REDSTONE_TORCH;
    }

    private boolean isHardPhasedBlock(BlockState blockState) {
        if (this.isDecoyOrPassableBlock(blockState)) {
            return false;
        }
        Block block = blockState.getBlock();
        if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.ENDER_CHEST || block == Blocks.RESPAWN_ANCHOR || block == Blocks.CRYING_OBSIDIAN || block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL || block == Blocks.REINFORCED_DEEPSLATE || block == Blocks.NETHERITE_BLOCK || block == Blocks.ANCIENT_DEBRIS) {
            return true;
        }
        return blockState.isSolid() && blockState.blocksMotion();
    }

    private boolean trySearch(BlockPos blockPos, int heightOffset, boolean forcePush) {
        ArrayList<Direction> pushDirections = new ArrayList<Direction>();
        Direction playerFacing = PistonCrystalModule.mc.player.getDirection();
        pushDirections.add(playerFacing);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (dir == playerFacing) continue;
            pushDirections.add(dir);
        }
        boolean allowAirPlace = this.airPlace.getValue();
        for (Direction pushDir : pushDirections) {
            ArrayList<Direction> pistonOffsets;
            BlockPos selectedPistonPos;
            BlockPos crystalCandidate = blockPos.relative(pushDir).above(heightOffset);
            if (!this.inRange(crystalCandidate, this.placeRange.getValue().floatValue()) || this.strictDirection.getValue() && !WorldUtils.canSeeBlock(crystalCandidate)) continue;
            BlockPos candidateCrystalSupport = null;
            if (!(PistonHelper.canPlaceCrystal(crystalCandidate, allowAirPlace) || forcePush && PistonHelper.canPlaceCrystalIgnoreHeadroom(crystalCandidate, allowAirPlace))) {
                BlockPos belowCrystal;
                if (allowAirPlace || !this.autoSupport.getValue() || !this.canSupportCrystal(crystalCandidate, belowCrystal = crystalCandidate.below())) continue;
                candidateCrystalSupport = belowCrystal;
            }
            selectedPistonPos = null;
            BlockPos selectedRedstonePos = null;
            BlockPos selectedPistonSupportPos = null;
            if (forcePush) {
                pistonOffsets = new ArrayList<Direction>(4);
                pistonOffsets.add(pushDir);
                pistonOffsets.add(pushDir.getClockWise());
                pistonOffsets.add(pushDir.getCounterClockWise());
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (dir == pushDir.getOpposite() || pistonOffsets.contains(dir)) continue;
                    pistonOffsets.add(dir);
                }
            } else {
                pistonOffsets = new ArrayList<Direction>(3);
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (dir == pushDir.getOpposite()) continue;
                    pistonOffsets.add(dir);
                }
            }
            block4: for (Direction pistonOffsetDir : pistonOffsets) {
                BlockPos supportForPiston;
                boolean pistonAlreadyPresent;
                BlockPos pistonFrontPos;
                BlockPos pistonCandidate = crystalCandidate.relative(pushDir).relative(pistonOffsetDir);
                if ((!forcePush || pistonOffsetDir == pushDir) && !PistonCrystalModule.mc.level.getBlockState(pistonFrontPos = pistonCandidate.relative(pushDir.getOpposite())).isAir() && !PistonCrystalModule.mc.level.getBlockState(pistonFrontPos).canBeReplaced() || !(pistonAlreadyPresent = PistonCrystalModule.mc.level.getBlockState(pistonCandidate).getBlock() instanceof PistonBaseBlock) && this.strictDirection.getValue() && !WorldUtils.canSeeBlock(pistonCandidate)) continue;
                selectedPistonSupportPos = null;
                boolean canPlacePiston = PistonHelper.canPlace(pistonCandidate, null, allowAirPlace, true) || pistonAlreadyPresent;
                boolean supportedByCrystalBase = !allowAirPlace && candidateCrystalSupport != null && pistonCandidate.distManhattan((Vec3i)candidateCrystalSupport) == 1;
                if (!canPlacePiston && !supportedByCrystalBase) {
                    if (allowAirPlace || !this.autoSupport.getValue() || (supportForPiston = this.findSupportForPiston(pistonCandidate, pushDir, pistonOffsetDir, candidateCrystalSupport)) == null) continue;
                    selectedPistonSupportPos = supportForPiston;
                }
                if (!this.inRange(pistonCandidate, this.placeRange.getValue().floatValue())) continue;
                selectedPistonPos = pistonCandidate;
                for (Direction redstoneDir : Direction.values()) {
                    if (redstoneDir == pushDir.getOpposite()) continue;
                    BlockPos redstonePosCandidate = pistonCandidate.relative(redstoneDir);
                    if (selectedPistonSupportPos != null && redstonePosCandidate.equals(selectedPistonSupportPos) || candidateCrystalSupport != null && redstonePosCandidate.equals(candidateCrystalSupport)) continue;
                    if (PistonCrystalModule.mc.level.getBlockState(redstonePosCandidate).getBlock() == Blocks.REDSTONE_BLOCK || PistonCrystalModule.mc.level.getBlockState(redstonePosCandidate).getBlock() == Blocks.REDSTONE_TORCH) {
                        selectedRedstonePos = redstonePosCandidate;
                        break block4;
                    }
                    if (!PistonHelper.canPlace(redstonePosCandidate, null, allowAirPlace, true) || !this.inRange(redstonePosCandidate, this.placeRange.getValue().floatValue()) || this.strictDirection.getValue() && !WorldUtils.canSeeBlock(redstonePosCandidate)) continue;
                    selectedRedstonePos = redstonePosCandidate;
                    break block4;
                }
            }
            if (selectedPistonPos == null || selectedRedstonePos == null) continue;
            this.face = pushDir;
            this.crystalPos = crystalCandidate;
            this.crystalForcedPush = forcePush;
            this.pistonPos = selectedPistonPos;
            this.redstonePos = selectedRedstonePos;
            this.crystalSupportPos = candidateCrystalSupport;
            this.pistonSupportPos = selectedPistonSupportPos;
            return true;
        }
        return false;
    }

    private boolean placeBlock(int slot, BlockPos blockPos, Direction facing) {
        Direction placeSide;
        if (slot == -1) {
            return false;
        }
        if (facing != null) {
            if (this.strictDirection.getValue()) {
                List<Direction> validDirs = WorldUtils.getDirections(blockPos, null, true);
                placeSide = validDirs.contains(facing) ? facing : (validDirs.isEmpty() ? null : validDirs.get(0));
                if (placeSide == null && !this.airPlace.getValue()) {
                    return false;
                }
            } else {
                placeSide = PistonHelper.getPlaceSide(blockPos, direction -> true);
                if (placeSide == null && !this.airPlace.getValue()) {
                    return false;
                }
            }
        } else if (this.strictDirection.getValue() ? (placeSide = WorldUtils.getDirection(blockPos, true)) == null : (placeSide = PistonHelper.getPlaceSide(blockPos, direction -> true)) == null && !this.airPlace.getValue()) {
            return false;
        }
        int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        this.doSwap(slot, prevSlot);
        if (this.shouldRotate()) {
            if (facing != null) {
                this.pistonFacingSnap(facing);
            } else {
                Direction rotateSide = placeSide != null ? placeSide : Direction.UP;
                this.doRotate(blockPos, rotateSide);
            }
        }
        if (placeSide != null) {
            PistonHelper.placeBlock(blockPos, placeSide, false);
        } else {
            PistonHelper.clickBlock(blockPos, Direction.UP, false);
        }
        this.restoreSlot(slot, prevSlot);
        Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
        Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
        return true;
    }

    private void placeCrystal(int slot, BlockPos blockPos) {
        BlockPos basePos = blockPos.below();
        Direction direction = PistonHelper.getClickSide(basePos);
        if (direction == null) {
            if (this.airPlace.getValue()) {
                basePos = blockPos;
                direction = Direction.UP;
            } else {
                direction = Direction.UP;
            }
        }
        if (slot == -1) {
            return;
        }
        int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        this.doSwap(slot, prevSlot);
        if (this.shouldRotate()) {
            this.doRotate(basePos, direction);
        }
        PistonHelper.clickBlock(basePos, direction, false);
        this.restoreSlot(slot, prevSlot);
        Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
        Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
    }

    private void placeTorch(int slot, BlockPos blockPos) {
        if (!PistonHelper.canPlace(blockPos, null, this.airPlace.getValue(), true) || slot == -1) {
            return;
        }
        List<Direction> placeSides = PistonHelper.getPlaceSides(blockPos, null);
        if (placeSides.isEmpty()) {
            if (this.airPlace.getValue()) {
                int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
                this.doSwap(slot, prevSlot);
                if (this.shouldRotate()) {
                    this.doRotate(blockPos, Direction.UP);
                }
                PistonHelper.clickBlock(blockPos, Direction.UP, false);
                this.restoreSlot(slot, prevSlot);
                Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
                Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
                return;
            }
            return;
        }
        int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        for (Direction direction : placeSides) {
            if (PistonCrystalModule.mc.level.getBlockState(blockPos.relative(direction)).getBlock() instanceof PistonBaseBlock || direction == Direction.UP) continue;
            this.doSwap(slot, prevSlot);
            if (this.shouldRotate()) {
                this.doRotate(blockPos, direction);
            }
            PistonHelper.placeBlock(blockPos, direction, false);
            this.restoreSlot(slot, prevSlot);
            Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
            Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
            return;
        }
    }

    private void attackCrystalAt(BlockPos blockPos) {
        List<EndCrystal> crystals = PistonCrystalModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(blockPos).inflate(1.0), endCrystal -> endCrystal.isAlive());
        if (!crystals.isEmpty()) {
            double maxRangeSq = this.breakRange.getValue().floatValue() * this.breakRange.getValue().floatValue();
            EndCrystal nearestCrystal = crystals.stream().filter(endCrystal -> PistonCrystalModule.mc.player.distanceToSqr((Entity)endCrystal) <= maxRangeSq).min(Comparator.comparingDouble(endCrystal -> PistonCrystalModule.mc.player.distanceToSqr((Entity)endCrystal))).orElse(null);
            if (nearestCrystal != null) {
                if (this.shouldRotate()) {
                    Vec3 targetVec = nearestCrystal.position().add(0.0, 0.5, 0.0);
                    float[] rotations = RotationUtils.getRotations(targetVec);
                    Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
                }
                mc.getConnection().send((Packet)new ServerboundAttackPacket(nearestCrystal.getId()));
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
    }

    private void breakBlock(BlockPos blockPos) {
        if (PistonCrystalModule.mc.player == null || mc.getConnection() == null || PistonCrystalModule.mc.level == null || blockPos == null) {
            return;
        }
        Direction direction = PistonHelper.getClickSide(blockPos);
        if (direction == null) {
            direction = Direction.UP;
        }
        int pickaxeSlot = this.findPickaxe();
        int prevSlot = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (pickaxeSlot != -1) {
            this.doSwap(pickaxeSlot, prevSlot);
        }
        if (this.shouldRotate()) {
            this.doRotate(blockPos, direction);
        }
        Direction clickSide = direction;
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, clickSide, sequence));
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, clickSide, sequence));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (pickaxeSlot != -1) {
            this.restoreSlot(pickaxeSlot, prevSlot);
        }
    }

    private int findPickaxe() {
        int slot = this.findItem(Items.NETHERITE_PICKAXE);
        if (slot != -1) {
            return slot;
        }
        slot = this.findItem(Items.DIAMOND_PICKAXE);
        if (slot != -1) {
            return slot;
        }
        slot = this.findItem(Items.IRON_PICKAXE);
        if (slot != -1) {
            return slot;
        }
        return -1;
    }

    private boolean shouldRotate() {
        return !this.rotate.getValue().equalsIgnoreCase("None");
    }

    private void doRotate(BlockPos blockPos, Direction direction) {
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
        float[] rotations = RotationUtils.getRotations(hitVec);
        Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
    }

    private void pistonFacingSnap(Direction direction) {
        if (direction == Direction.EAST) {
            Night.ROTATION_MANAGER.silentRotate(-90.0f, 0.0f);
        } else if (direction == Direction.WEST) {
            Night.ROTATION_MANAGER.silentRotate(90.0f, 0.0f);
        } else if (direction == Direction.NORTH) {
            Night.ROTATION_MANAGER.silentRotate(180.0f, 0.0f);
        } else {
            Night.ROTATION_MANAGER.silentRotate(0.0f, 0.0f);
        }
    }

    private void doSwap(int slot, int prevSlot) {
        InventoryUtils.switchSlot(this.swapAction.getValue(), slot, prevSlot);
    }

    private void restoreSlot(int slot, int prevSlot) {
        InventoryUtils.switchBack(this.swapAction.getValue(), slot, prevSlot);
    }

    private int findItem(Item item) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            ItemStack itemStack = PistonCrystalModule.mc.player.getMainHandItem();
            return !itemStack.isEmpty() && itemStack.getItem() == item ? PistonCrystalModule.mc.player.getInventory().getSelectedSlot() : -1;
        }
        int maxSlot = this.swapAction.getValue().equalsIgnoreCase("AltSwap") || this.swapAction.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END;
        for (int i = 0; i <= maxSlot; ++i) {
            ItemStack itemStack = PistonCrystalModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || itemStack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private int findPiston() {
        int slot = this.findBlock(Blocks.PISTON);
        if (slot != -1) {
            return slot;
        }
        return this.findBlock(Blocks.STICKY_PISTON);
    }

    private int findBlock(Block block) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            BlockItem blockItem;
            Item item;
            ItemStack itemStack = PistonCrystalModule.mc.player.getMainHandItem();
            if (!itemStack.isEmpty() && (item = itemStack.getItem()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() == block) {
                return PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
            }
            return -1;
        }
        int maxSlot = this.swapAction.getValue().equalsIgnoreCase("AltSwap") || this.swapAction.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END;
        for (int i = 0; i <= maxSlot; ++i) {
            BlockItem blockItem;
            Item item;
            ItemStack itemStack = PistonCrystalModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || !((item = itemStack.getItem()) instanceof BlockItem) || (blockItem = (BlockItem)item).getBlock() != block) continue;
            return i;
        }
        return -1;
    }

    private int findRedstone() {
        int slot = this.findItem(Items.REDSTONE_BLOCK);
        if (slot != -1) {
            this.usingTorch = false;
            return slot;
        }
        slot = this.findItem(Items.REDSTONE_TORCH);
        if (slot != -1) {
            this.usingTorch = true;
            return slot;
        }
        return -1;
    }

    private Player pickTarget() {
        double maxRange = this.targetRange.getValue().floatValue();
        return PistonCrystalModule.mc.level.players().stream().filter(abstractClientPlayer -> abstractClientPlayer != PistonCrystalModule.mc.player).filter(abstractClientPlayer -> !EntityUtils.isGhost((Entity)abstractClientPlayer)).filter(abstractClientPlayer -> !abstractClientPlayer.isSpectator() && abstractClientPlayer.isAlive()).filter(abstractClientPlayer -> PistonCrystalModule.mc.player.distanceToSqr((Entity)abstractClientPlayer) <= maxRange * maxRange).filter(abstractClientPlayer -> !Night.FRIEND_MANAGER.contains(abstractClientPlayer.getName().getString())).filter(abstractClientPlayer -> this.inAirTarget.getValue() || abstractClientPlayer.onGround()).min(this.getComparator()).orElse(null);
    }

    private Comparator<Player> getComparator() {
        return switch (this.sort.getValue()) {
            case "LowestHealth" -> Comparator.comparingDouble(player -> player.getHealth() + player.getAbsorptionAmount());
            default -> Comparator.comparingDouble(player -> PistonCrystalModule.mc.player.distanceToSqr((Entity)player));
        };
    }

    private boolean inRange(BlockPos blockPos, double maxRange) {
        return PistonCrystalModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= maxRange * maxRange;
    }

    private boolean canSupportCrystal(BlockPos crystalPos, BlockPos supportPos) {
        if (PistonCrystalModule.mc.level == null || supportPos == null || crystalPos == null) {
            return false;
        }
        if (!this.inRange(supportPos, this.placeRange.getValue().floatValue())) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(crystalPos).isAir() && !PistonCrystalModule.mc.level.getBlockState(crystalPos).canBeReplaced()) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(crystalPos.above()).isAir() && !PistonCrystalModule.mc.level.getBlockState(crystalPos.above()).canBeReplaced()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(crystalPos, false) || PistonHelper.hasEntityBlockCrystal(crystalPos.above(), false)) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(supportPos).isAir() && !PistonCrystalModule.mc.level.getBlockState(supportPos).canBeReplaced()) {
            return false;
        }
        if (!PistonHelper.canPlace(supportPos, null, false, true)) {
            return false;
        }
        return !this.strictDirection.getValue() || WorldUtils.getDirection(supportPos, true) != null;
    }

    private BlockPos findSupportForPiston(BlockPos pistonPos, Direction pushDir, Direction pistonOffsetDir, BlockPos crystalSupportPos) {
        if (PistonCrystalModule.mc.level == null || pistonPos == null) {
            return null;
        }
        if (crystalSupportPos != null && pistonPos.distManhattan((Vec3i)crystalSupportPos) == 1) {
            return null;
        }
        Direction[] candidateDirs = new Direction[]{Direction.DOWN, pistonOffsetDir, pistonOffsetDir.getOpposite()};
        for (Direction dir : candidateDirs) {
            BlockPos candidatePos = pistonPos.relative(dir);
            if (!this.inRange(candidatePos, this.placeRange.getValue().floatValue()) || !PistonCrystalModule.mc.level.getBlockState(candidatePos).isAir() && !PistonCrystalModule.mc.level.getBlockState(candidatePos).canBeReplaced() || !PistonHelper.canPlace(candidatePos, null, false, true) || this.strictDirection.getValue() && WorldUtils.getDirection(candidatePos, true) == null) continue;
            return candidatePos;
        }
        return null;
    }

    private BlockPos findPistonSupportRepair(BlockPos pistonPos, Direction pushDir) {
        if (PistonCrystalModule.mc.level == null || pistonPos == null) {
            return null;
        }
        BlockPos belowPos = pistonPos.below();
        if (!this.isReservedForSetup(belowPos) && this.inRange(belowPos, this.placeRange.getValue().floatValue()) && (PistonCrystalModule.mc.level.getBlockState(belowPos).isAir() || PistonCrystalModule.mc.level.getBlockState(belowPos).canBeReplaced()) && PistonHelper.canPlace(belowPos, null, false, true)) {
            return belowPos;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos;
            if (pushDir != null && dir == pushDir.getOpposite() || this.isReservedForSetup(sidePos = pistonPos.relative(dir)) || !this.inRange(sidePos, this.placeRange.getValue().floatValue()) || !PistonCrystalModule.mc.level.getBlockState(sidePos).isAir() && !PistonCrystalModule.mc.level.getBlockState(sidePos).canBeReplaced() || !PistonHelper.canPlace(sidePos, null, false, true)) continue;
            return sidePos;
        }
        return null;
    }

    private boolean isReservedForSetup(BlockPos blockPos) {
        return blockPos.equals((Object)this.redstonePos) || blockPos.equals((Object)this.crystalPos) || blockPos.equals((Object)this.crystalSupportPos);
    }

    public int findSupportBlock() {
        int slot = this.findBlock(Blocks.OBSIDIAN);
        if (slot == -1) {
            slot = this.findBlock(Blocks.CRYING_OBSIDIAN);
        }
        if (slot == -1) {
            slot = this.findBlock(Blocks.ENDER_CHEST);
        }
        if (slot == -1) {
            slot = this.findBlock(Blocks.NETHERITE_BLOCK);
        }
        if (slot == -1) {
            slot = this.findBlock(Blocks.COBBLESTONE);
        }
        if (slot == -1) {
            slot = this.findBlock(Blocks.STONE);
        }
        return slot;
    }

    private boolean isObsidian(BlockPos blockPos) {
        if (PistonCrystalModule.mc.level == null || blockPos == null) {
            return false;
        }
        Block block = PistonCrystalModule.mc.level.getBlockState(blockPos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.ENDER_CHEST;
    }

    private boolean isSolidBlock(BlockPos blockPos) {
        if (PistonCrystalModule.mc.level == null || blockPos == null) {
            return false;
        }
        BlockState blockState = PistonCrystalModule.mc.level.getBlockState(blockPos);
        return !blockState.isAir() && blockState.isSolid();
    }

    private boolean isRedstone(BlockPos blockPos) {
        if (PistonCrystalModule.mc.level == null || blockPos == null) {
            return false;
        }
        Block block = PistonCrystalModule.mc.level.getBlockState(blockPos).getBlock();
        return block == Blocks.REDSTONE_BLOCK || block == Blocks.REDSTONE_TORCH;
    }

    private void resetState() {
        this.pistonPos = null;
        this.redstonePos = null;
        this.crystalPos = null;
        this.crystalForcedPush = false;
        this.crystalSupportPos = null;
        this.pistonSupportPos = null;
        this.lastPiston = null;
        this.face = null;
        this.usingTorch = false;
    }
}

