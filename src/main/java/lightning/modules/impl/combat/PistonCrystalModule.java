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
                    BlockPos p = this.target.blockPosition().above(y);
                    if (!PistonHelper.hasCrystal(p) || (this.crystalPos != null && this.crystalPos.equals(p))) continue;
                    this.attackCrystalAt(p);
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
                    BlockPos b = it.next();
                    if (!this.isRedstone(b)) {
                        it.remove();
                        continue;
                    }
                    if (b.equals(this.redstonePos) || !this.inRange(b, this.placeRange.getValue().floatValue())) continue;
                    this.breakBlock(b);
                    it.remove();
                    this.lastActionMs = now;
                    return;
                }
                Iterator<BlockPos> pit = this.placedPistons.iterator();
                while (pit.hasNext()) {
                    BlockPos b = pit.next();
                    if (!(mc.level.getBlockState(b).getBlock() instanceof PistonBaseBlock)) {
                        pit.remove();
                        continue;
                    }
                    if (b.equals(this.pistonPos) || !this.inRange(b, this.placeRange.getValue().floatValue())) continue;
                    this.breakBlock(b);
                    pit.remove();
                    this.lastActionMs = now;
                    return;
                }
            }
            long breakTimeout = (2L + this.delay.getValue().longValue()) * 50L;
            if (this.autoBreak.getValue() && !digging && now - this.lastActionMs >= breakTimeout) {
                Iterator<BlockPos> it = this.placedRedstone.iterator();
                while (it.hasNext()) {
                    BlockPos b = it.next();
                    if (this.isRedstone(b)) {
                        if (!this.inRange(b, this.placeRange.getValue().floatValue())) continue;
                        this.breakBlock(b);
                        it.remove();
                        this.lastActionMs = now;
                        this.resetState();
                        return;
                    }
                    it.remove();
                }
                Iterator<BlockPos> pit = this.placedPistons.iterator();
                while (pit.hasNext()) {
                    BlockPos b = pit.next();
                    if (mc.level.getBlockState(b).getBlock() instanceof PistonBaseBlock) {
                        if (!this.inRange(b, this.placeRange.getValue().floatValue())) continue;
                        this.breakBlock(b);
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
        int[] nArray;
        int n;
        if (this.isTargetPhased(player)) {
            return;
        }
        int n2 = this.extrapolationTicks.getValue().intValue();
        double d = (player.getX() - player.xOld) * (double)n2;
        double d2 = (player.getY() - player.yOld) * (double)n2 * 0.3;
        double d3 = (player.getZ() - player.zOld) * (double)n2;
        double d4 = player.getX() + d;
        double d5 = player.getY() + d2;
        double d6 = player.getZ() + d3;
        BlockPos blockPos = PistonCrystalModule.getEntityFeetPos((Entity)player, d4, d5, d6);
        AABB aABB = player.getBoundingBox().move(d, d2, d3);
        AABB aABB2 = this.selfExtrapolate.getValue() ? PistonCrystalModule.mc.player.getBoundingBox().move((PistonCrystalModule.mc.player.getX() - PistonCrystalModule.mc.player.xOld) * (double)n2, (PistonCrystalModule.mc.player.getY() - PistonCrystalModule.mc.player.yOld) * (double)n2 * 0.3, (PistonCrystalModule.mc.player.getZ() - PistonCrystalModule.mc.player.zOld) * (double)n2) : PistonCrystalModule.mc.player.getBoundingBox();
        boolean bl = player.getPose() == Pose.SWIMMING || player.isVisuallyCrawling() || player.getEyeHeight() <= 1.0f || player.getBoundingBox().getYsize() <= 1.0;
        for (n = 0; n <= 4 && !this.hasHorizontalOpening(blockPos.above(n)); ++n) {
        }
        boolean bl2 = bl && PistonCrystalModule.mc.level != null && !PistonCrystalModule.mc.level.getBlockState(blockPos.above(1)).isAir() && !this.hasHorizontalOpening(blockPos.above(0));
        boolean bl3 = !bl && PistonCrystalModule.mc.level != null && !PistonCrystalModule.mc.level.getBlockState(blockPos.above(2)).isAir() && !this.hasHorizontalOpening(blockPos.above(1));
        boolean bl4 = !bl && n >= 2;
        boolean bl5 = !PistonCrystalModule.mc.level.getBlockState(blockPos.above(2)).isAir();
        boolean bl6 = !this.hasHorizontalOpening(blockPos.above(2));
        boolean bl7 = !bl && PistonCrystalModule.mc.level != null && (bl5 || bl6) && this.hasHorizontalOpening(blockPos.above(1));
        boolean bl8 = bl7;
        if (bl2) {
            int[] nArray2 = new int[3];
            nArray2[0] = 1;
            nArray2[1] = 0;
            nArray = nArray2;
            nArray2[2] = 2;
        } else if (n >= 3) {
            int[] nArray3 = new int[5];
            nArray3[0] = n;
            nArray3[1] = n - 1;
            nArray3[2] = 2;
            nArray3[3] = 1;
            nArray = nArray3;
            nArray3[4] = 0;
        } else if (bl3 || n == 2) {
            int[] nArray4 = new int[4];
            nArray4[0] = 2;
            nArray4[1] = 1;
            nArray4[2] = 0;
            nArray = nArray4;
            nArray4[3] = 3;
        } else if (bl7) {
            int[] nArray5 = new int[4];
            nArray5[0] = 1;
            nArray5[1] = 0;
            nArray5[2] = 2;
            nArray = nArray5;
            nArray5[3] = 3;
        } else {
            int[] nArray6 = new int[4];
            nArray6[0] = 0;
            nArray6[1] = 1;
            nArray6[2] = 2;
            nArray = nArray6;
            nArray6[3] = 3;
        }
        int[] nArray7 = nArray;
        boolean bl9 = false;
        for (int n3 : nArray7) {
            if (n3 != this.preferredHeight) continue;
            bl9 = true;
            break;
        }
        if (this.preferredHeight != -1 && bl9 && this.preferredHeight != nArray7[0]) {
            int[] nArray8 = new int[nArray7.length];
            nArray8[0] = this.preferredHeight;
            int n4 = 1;
            for (int n5 : nArray7) {
                if (n5 == this.preferredHeight) continue;
                nArray8[n4++] = n5;
            }
            nArray7 = nArray8;
        }
        for (int n3 : nArray7) {
            BlockPos blockPos2 = blockPos.above(n3);
            BlockPos blockPos3 = bl2 && n3 == 1 || bl7 && n3 == 1 ? blockPos.above(1) : (!(!bl3 && !bl4 || n3 != 2 && n3 != n) ? blockPos.above(n3) : null);
            float f = DamageUtils.getCrystalDamage((Entity)player, aABB, blockPos2, blockPos3, this.blockDestruction.getValue());
            float f2 = DamageUtils.getCrystalDamage((Entity)PistonCrystalModule.mc.player, aABB2, blockPos2, null, this.blockDestruction.getValue());
            int n6 = n3 == 1 && bl2 || n3 == 1 && bl7 || n3 == 2 && bl3 || n3 == n && bl4 ? 1 : 0;
            int n7 = n6;
            if (n6 != 0) {
                if (f <= this.minDamage.getValue().floatValue() || f2 > this.maxLocalDamage.getValue().floatValue() || this.safety.getValue() && f2 >= PistonCrystalModule.mc.player.getHealth() + PistonCrystalModule.mc.player.getAbsorptionAmount()) continue;
                if (!this.trySearch(blockPos, n3, true)) {
                    this.tryPreBuildCrystalSupport(blockPos, n3);
                    continue;
                }
                this.preferredHeight = n3;
                return;
            }
            if (f <= this.minDamage.getValue().floatValue() || f2 > this.maxLocalDamage.getValue().floatValue() || this.safety.getValue() && f2 >= PistonCrystalModule.mc.player.getHealth() + PistonCrystalModule.mc.player.getAbsorptionAmount()) continue;
            if (!this.trySearch(blockPos, n3, bl3 || bl2 || bl4)) {
                if (!this.tryPreBuildCrystalSupport(blockPos, n3)) continue;
                return;
            }
            this.preferredHeight = n3;
            return;
        }
    }

    private boolean tryPreBuildCrystalSupport(BlockPos blockPos, int n) {
        if (!this.autoSupport.getValue()) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2;
            BlockPos blockPos3 = blockPos.relative(direction).above(n);
            if (!this.inRange(blockPos3, this.placeRange.getValue().floatValue()) || (PistonHelper.canPlaceCrystal(blockPos3, false) ? !this.inRange(blockPos2 = blockPos3.relative(direction).below(), this.placeRange.getValue().floatValue()) || !PistonHelper.canPlace(blockPos2, null, false, true) : this.isObsidian(blockPos2 = blockPos3.below()) || !this.canSupportCrystal(blockPos3, blockPos2))) continue;
            int n2 = this.findSupportBlock();
            if (n2 == -1) {
                return false;
            }
            this.placeBlock(n2, blockPos2, null);
            this.lastActionMs = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static BlockPos getEntityFeetPos(Entity entity, double d, double d2, double d3) {
        VoxelShape voxelShape;
        if (PistonCrystalModule.mc.level == null) {
            return new BlockPos(Mth.floor((double)d), Mth.floor((double)d2), Mth.floor((double)d3));
        }
        int n = Mth.floor((double)d2);
        BlockPos blockPos = new BlockPos(Mth.floor((double)d), n, Mth.floor((double)d3));
        BlockState blockState = PistonCrystalModule.mc.level.getBlockState(blockPos);
        if (!(blockState.isAir() || blockState.canBeReplaced() || (voxelShape = blockState.getCollisionShape((BlockGetter)PistonCrystalModule.mc.level, blockPos)).isEmpty())) {
            double d5 = (double)blockPos.getY() + voxelShape.max(Direction.Axis.Y);
            if (d2 >= d5 - 0.05) {
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

    private boolean trySearch(BlockPos blockPos, int n, boolean bl) {
        ArrayList<Direction> arrayList = new ArrayList<Direction>();
        Direction direction = PistonCrystalModule.mc.player.getDirection();
        arrayList.add(direction);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (dir == direction) continue;
            arrayList.add(dir);
        }
        boolean bl2 = this.airPlace.getValue();
        for (Direction direction2 : arrayList) {
            ArrayList<Direction> arrayList2;
            BlockPos blockPos2;
            BlockPos blockPos3 = blockPos.relative(direction2).above(n);
            if (!this.inRange(blockPos3, this.placeRange.getValue().floatValue()) || this.strictDirection.getValue() && !WorldUtils.canSeeBlock(blockPos3)) continue;
            BlockPos blockPos4 = null;
            if (!(PistonHelper.canPlaceCrystal(blockPos3, bl2) || bl && PistonHelper.canPlaceCrystalIgnoreHeadroom(blockPos3, bl2))) {
                if (bl2 || !this.autoSupport.getValue() || !this.canSupportCrystal(blockPos3, blockPos2 = blockPos3.below())) continue;
                blockPos4 = blockPos2;
            }
            blockPos2 = null;
            BlockPos blockPos5 = null;
            BlockPos blockPos6 = null;
            if (bl) {
                arrayList2 = new ArrayList<Direction>(4);
                arrayList2.add(direction2);
                arrayList2.add(direction2.getClockWise());
                arrayList2.add(direction2.getCounterClockWise());
                for (Direction direction3 : Direction.Plane.HORIZONTAL) {
                    if (direction3 == direction2.getOpposite() || arrayList2.contains(direction3)) continue;
                    arrayList2.add(direction3);
                }
            } else {
                arrayList2 = new ArrayList<Direction>(3);
                for (Direction direction3 : Direction.Plane.HORIZONTAL) {
                    if (direction3 == direction2.getOpposite()) continue;
                    arrayList2.add(direction3);
                }
            }
            block4: for (Direction direction3 : arrayList2) {
                BlockPos blockPos7;
                boolean bl3;
                BlockPos blockPos8;
                BlockPos blockPos9 = blockPos3.relative(direction2).relative(direction3);
                if ((!bl || direction3 == direction2) && !PistonCrystalModule.mc.level.getBlockState(blockPos8 = blockPos9.relative(direction2.getOpposite())).isAir() && !PistonCrystalModule.mc.level.getBlockState(blockPos8).canBeReplaced() || !(bl3 = PistonCrystalModule.mc.level.getBlockState(blockPos9).getBlock() instanceof PistonBaseBlock) && this.strictDirection.getValue() && !WorldUtils.canSeeBlock(blockPos9)) continue;
                blockPos6 = null;
                boolean bl4 = PistonHelper.canPlace(blockPos9, null, bl2, true) || bl3;
                boolean bl5 = !bl2 && blockPos4 != null && blockPos9.distManhattan((Vec3i)blockPos4) == 1;
                boolean bl6 = bl5;
                if (!bl4 && !bl5) {
                    if (bl2 || !this.autoSupport.getValue() || (blockPos7 = this.findSupportForPiston(blockPos9, direction2, direction3, blockPos4)) == null) continue;
                    blockPos6 = blockPos7;
                }
                if (!this.inRange(blockPos9, this.placeRange.getValue().floatValue())) continue;
                blockPos2 = blockPos9;
                for (Direction redstoneDir : Direction.values()) {
                    if (redstoneDir == direction2.getOpposite()) continue;
                    BlockPos redstonePosCandidate = blockPos9.relative(redstoneDir);
                    if (blockPos6 != null && redstonePosCandidate.equals(blockPos6) || blockPos4 != null && redstonePosCandidate.equals(blockPos4)) continue;
                    if (PistonCrystalModule.mc.level.getBlockState(redstonePosCandidate).getBlock() == Blocks.REDSTONE_BLOCK || PistonCrystalModule.mc.level.getBlockState(redstonePosCandidate).getBlock() == Blocks.REDSTONE_TORCH) {
                        blockPos5 = redstonePosCandidate;
                        break block4;
                    }
                    if (!PistonHelper.canPlace(redstonePosCandidate, null, bl2, true) || !this.inRange(redstonePosCandidate, this.placeRange.getValue().floatValue()) || this.strictDirection.getValue() && !WorldUtils.canSeeBlock(redstonePosCandidate)) continue;
                    blockPos5 = redstonePosCandidate;
                    break block4;
                }
            }
            if (blockPos2 == null || blockPos5 == null) continue;
            this.face = direction2;
            this.crystalPos = blockPos3;
            this.crystalForcedPush = bl;
            this.pistonPos = blockPos2;
            this.redstonePos = blockPos5;
            this.crystalSupportPos = blockPos4;
            this.pistonSupportPos = blockPos6;
            return true;
        }
        return false;
    }

    private boolean placeBlock(int n, BlockPos blockPos, Direction direction2) {
        Direction direction3;
        if (n == -1) {
            return false;
        }
        if (direction2 != null) {
            if (this.strictDirection.getValue()) {
                List<Direction> list = WorldUtils.getDirections(blockPos, null, true);
                direction3 = list.contains(direction2) ? direction2 : (list.isEmpty() ? null : list.get(0));
                if (direction3 == null && !this.airPlace.getValue()) {
                    return false;
                }
            } else {
                direction3 = PistonHelper.getPlaceSide(blockPos, direction -> true);
                if (direction3 == null && !this.airPlace.getValue()) {
                    return false;
                }
            }
        } else if (this.strictDirection.getValue() ? (direction3 = WorldUtils.getDirection(blockPos, true)) == null : (direction3 = PistonHelper.getPlaceSide(blockPos, direction -> true)) == null && !this.airPlace.getValue()) {
            return false;
        }
        int n2 = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        this.doSwap(n, n2);
        if (this.shouldRotate()) {
            if (direction2 != null) {
                this.pistonFacingSnap(direction2);
            } else {
                Direction direction4 = direction3 != null ? direction3 : Direction.UP;
                this.doRotate(blockPos, direction4);
            }
        }
        if (direction3 != null) {
            PistonHelper.placeBlock(blockPos, direction3, false);
        } else {
            PistonHelper.clickBlock(blockPos, Direction.UP, false);
        }
        this.restoreSlot(n, n2);
        Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
        Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
        return true;
    }

    private void placeCrystal(int n, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        Direction direction = PistonHelper.getClickSide(blockPos2);
        if (direction == null) {
            if (this.airPlace.getValue()) {
                blockPos2 = blockPos;
                direction = Direction.UP;
            } else {
                direction = Direction.UP;
            }
        }
        if (n == -1) {
            return;
        }
        int n2 = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        this.doSwap(n, n2);
        if (this.shouldRotate()) {
            this.doRotate(blockPos2, direction);
        }
        PistonHelper.clickBlock(blockPos2, direction, false);
        this.restoreSlot(n, n2);
        Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
        Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
    }

    private void placeTorch(int n, BlockPos blockPos) {
        if (!PistonHelper.canPlace(blockPos, null, this.airPlace.getValue(), true) || n == -1) {
            return;
        }
        List<Direction> list = PistonHelper.getPlaceSides(blockPos, null);
        if (list.isEmpty()) {
            if (this.airPlace.getValue()) {
                int n2 = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
                this.doSwap(n, n2);
                if (this.shouldRotate()) {
                    this.doRotate(blockPos, Direction.UP);
                }
                PistonHelper.clickBlock(blockPos, Direction.UP, false);
                this.restoreSlot(n, n2);
                Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
                Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
                return;
            }
            return;
        }
        int n3 = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        for (Direction direction : list) {
            if (PistonCrystalModule.mc.level.getBlockState(blockPos.relative(direction)).getBlock() instanceof PistonBaseBlock || direction == Direction.UP) continue;
            this.doSwap(n, n3);
            if (this.shouldRotate()) {
                this.doRotate(blockPos, direction);
            }
            PistonHelper.placeBlock(blockPos, direction, false);
            this.restoreSlot(n, n3);
            Night.RENDER_MANAGER.getRenderPositions().removeIf(renderPosition -> renderPosition.getPos().equals((Object)blockPos));
            Night.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(blockPos));
            return;
        }
    }

    private void attackCrystalAt(BlockPos blockPos) {
        List<EndCrystal> list = PistonCrystalModule.mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(blockPos).inflate(1.0), endCrystal -> endCrystal.isAlive());
        if (!list.isEmpty()) {
            double d = this.breakRange.getValue().floatValue() * this.breakRange.getValue().floatValue();
            EndCrystal endCrystal2 = list.stream().filter(endCrystal -> PistonCrystalModule.mc.player.distanceToSqr((Entity)endCrystal) <= d).min(Comparator.comparingDouble(endCrystal -> PistonCrystalModule.mc.player.distanceToSqr((Entity)endCrystal))).orElse(null);
            if (endCrystal2 != null) {
                if (this.shouldRotate()) {
                    Vec3 vec3 = endCrystal2.position().add(0.0, 0.5, 0.0);
                    float[] fArray = RotationUtils.getRotations(vec3);
                    Night.ROTATION_MANAGER.silentRotate(fArray[0], fArray[1]);
                }
                mc.getConnection().send((Packet)new ServerboundAttackPacket(endCrystal2.getId()));
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
        int n2 = this.findPickaxe();
        int n3 = PistonCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (n2 != -1) {
            this.doSwap(n2, n3);
        }
        if (this.shouldRotate()) {
            this.doRotate(blockPos, direction);
        }
        Direction direction2 = direction;
        NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, direction2, n));
        NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction2, n));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (n2 != -1) {
            this.restoreSlot(n2, n3);
        }
    }

    private int findPickaxe() {
        int n = this.findItem(Items.NETHERITE_PICKAXE);
        if (n != -1) {
            return n;
        }
        n = this.findItem(Items.DIAMOND_PICKAXE);
        if (n != -1) {
            return n;
        }
        n = this.findItem(Items.IRON_PICKAXE);
        if (n != -1) {
            return n;
        }
        return -1;
    }

    private boolean shouldRotate() {
        return !this.rotate.getValue().equalsIgnoreCase("None");
    }

    private void doRotate(BlockPos blockPos, Direction direction) {
        Vec3 vec3 = Vec3.atCenterOf((Vec3i)blockPos).add((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
        float[] fArray = RotationUtils.getRotations(vec3);
        Night.ROTATION_MANAGER.silentRotate(fArray[0], fArray[1]);
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

    private void doSwap(int n, int n2) {
        InventoryUtils.switchSlot(this.swapAction.getValue(), n, n2);
    }

    private void restoreSlot(int n, int n2) {
        InventoryUtils.switchBack(this.swapAction.getValue(), n, n2);
    }

    private int findItem(Item item) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            ItemStack itemStack = PistonCrystalModule.mc.player.getMainHandItem();
            return !itemStack.isEmpty() && itemStack.getItem() == item ? PistonCrystalModule.mc.player.getInventory().getSelectedSlot() : -1;
        }
        int n = this.swapAction.getValue().equalsIgnoreCase("AltSwap") || this.swapAction.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END;
        for (int i = 0; i <= n; ++i) {
            ItemStack itemStack = PistonCrystalModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || itemStack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private int findPiston() {
        int n = this.findBlock(Blocks.PISTON);
        if (n != -1) {
            return n;
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
        int n = this.swapAction.getValue().equalsIgnoreCase("AltSwap") || this.swapAction.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END;
        for (int i = 0; i <= n; ++i) {
            BlockItem blockItem;
            Item item;
            ItemStack itemStack = PistonCrystalModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || !((item = itemStack.getItem()) instanceof BlockItem) || (blockItem = (BlockItem)item).getBlock() != block) continue;
            return i;
        }
        return -1;
    }

    private int findRedstone() {
        int n = this.findItem(Items.REDSTONE_BLOCK);
        if (n != -1) {
            this.usingTorch = false;
            return n;
        }
        n = this.findItem(Items.REDSTONE_TORCH);
        if (n != -1) {
            this.usingTorch = true;
            return n;
        }
        return -1;
    }

    private Player pickTarget() {
        double d = this.targetRange.getValue().floatValue();
        return PistonCrystalModule.mc.level.players().stream().filter(abstractClientPlayer -> abstractClientPlayer != PistonCrystalModule.mc.player).filter(abstractClientPlayer -> !EntityUtils.isGhost((Entity)abstractClientPlayer)).filter(abstractClientPlayer -> !abstractClientPlayer.isSpectator() && abstractClientPlayer.isAlive()).filter(abstractClientPlayer -> PistonCrystalModule.mc.player.distanceToSqr((Entity)abstractClientPlayer) <= d * d).filter(abstractClientPlayer -> !Night.FRIEND_MANAGER.contains(abstractClientPlayer.getName().getString())).filter(abstractClientPlayer -> this.inAirTarget.getValue() || abstractClientPlayer.onGround()).min(this.getComparator()).orElse(null);
    }

    private Comparator<Player> getComparator() {
        return switch (this.sort.getValue()) {
            case "LowestHealth" -> Comparator.comparingDouble(player -> player.getHealth() + player.getAbsorptionAmount());
            default -> Comparator.comparingDouble(player -> PistonCrystalModule.mc.player.distanceToSqr((Entity)player));
        };
    }

    private boolean inRange(BlockPos blockPos, double d) {
        return PistonCrystalModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= d * d;
    }

    private boolean canSupportCrystal(BlockPos blockPos, BlockPos blockPos2) {
        if (PistonCrystalModule.mc.level == null || blockPos2 == null || blockPos == null) {
            return false;
        }
        if (!this.inRange(blockPos2, this.placeRange.getValue().floatValue())) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(blockPos).isAir() && !PistonCrystalModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(blockPos.above()).isAir() && !PistonCrystalModule.mc.level.getBlockState(blockPos.above()).canBeReplaced()) {
            return false;
        }
        if (PistonHelper.hasEntityBlockCrystal(blockPos, false) || PistonHelper.hasEntityBlockCrystal(blockPos.above(), false)) {
            return false;
        }
        if (!PistonCrystalModule.mc.level.getBlockState(blockPos2).isAir() && !PistonCrystalModule.mc.level.getBlockState(blockPos2).canBeReplaced()) {
            return false;
        }
        if (!PistonHelper.canPlace(blockPos2, null, false, true)) {
            return false;
        }
        return !this.strictDirection.getValue() || WorldUtils.getDirection(blockPos2, true) != null;
    }

    private BlockPos findSupportForPiston(BlockPos blockPos, Direction direction, Direction direction2, BlockPos blockPos2) {
        if (PistonCrystalModule.mc.level == null || blockPos == null) {
            return null;
        }
        if (blockPos2 != null && blockPos.distManhattan((Vec3i)blockPos2) == 1) {
            return null;
        }
        Direction[] directionArray = new Direction[]{Direction.DOWN, direction2, direction2.getOpposite()};
        for (Direction direction3 : directionArray) {
            BlockPos blockPos3 = blockPos.relative(direction3);
            if (!this.inRange(blockPos3, this.placeRange.getValue().floatValue()) || !PistonCrystalModule.mc.level.getBlockState(blockPos3).isAir() && !PistonCrystalModule.mc.level.getBlockState(blockPos3).canBeReplaced() || !PistonHelper.canPlace(blockPos3, null, false, true) || this.strictDirection.getValue() && WorldUtils.getDirection(blockPos3, true) == null) continue;
            return blockPos3;
        }
        return null;
    }

    private BlockPos findPistonSupportRepair(BlockPos blockPos, Direction direction) {
        if (PistonCrystalModule.mc.level == null || blockPos == null) {
            return null;
        }
        BlockPos blockPos2 = blockPos.below();
        if (!this.isReservedForSetup(blockPos2) && this.inRange(blockPos2, this.placeRange.getValue().floatValue()) && (PistonCrystalModule.mc.level.getBlockState(blockPos2).isAir() || PistonCrystalModule.mc.level.getBlockState(blockPos2).canBeReplaced()) && PistonHelper.canPlace(blockPos2, null, false, true)) {
            return blockPos2;
        }
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos3;
            if (direction != null && direction2 == direction.getOpposite() || this.isReservedForSetup(blockPos3 = blockPos.relative(direction2)) || !this.inRange(blockPos3, this.placeRange.getValue().floatValue()) || !PistonCrystalModule.mc.level.getBlockState(blockPos3).isAir() && !PistonCrystalModule.mc.level.getBlockState(blockPos3).canBeReplaced() || !PistonHelper.canPlace(blockPos3, null, false, true)) continue;
            return blockPos3;
        }
        return null;
    }

    private boolean isReservedForSetup(BlockPos blockPos) {
        return blockPos.equals((Object)this.redstonePos) || blockPos.equals((Object)this.crystalPos) || blockPos.equals((Object)this.crystalSupportPos);
    }

    public int findSupportBlock() {
        int n = this.findBlock(Blocks.OBSIDIAN);
        if (n == -1) {
            n = this.findBlock(Blocks.CRYING_OBSIDIAN);
        }
        if (n == -1) {
            n = this.findBlock(Blocks.ENDER_CHEST);
        }
        if (n == -1) {
            n = this.findBlock(Blocks.NETHERITE_BLOCK);
        }
        if (n == -1) {
            n = this.findBlock(Blocks.COBBLESTONE);
        }
        if (n == -1) {
            n = this.findBlock(Blocks.STONE);
        }
        return n;
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

