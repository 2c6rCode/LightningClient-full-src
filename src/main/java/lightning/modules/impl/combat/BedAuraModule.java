/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.mixins.accessors.ClientPlayerEntityAccessor
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.CraftingMenu
 *  net.minecraft.world.item.BedItem
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="BedAura", description="Automatically places and detonates beds in the Nether and End.", category=Module.Category.COMBAT)
public class BedAuraModule
extends Module {
    public CategorySetting targetingCategory = new CategorySetting("Targeting", "Settings related to target selection.");
    public ModeSetting targetMode = new ModeSetting("Target", "Mode", "How targets are sorted.", new CategorySetting.Visibility(this.targetingCategory), "Closest", new String[]{"Closest", "Furthest", "Health"});
    public NumberSetting targetRange = new NumberSetting("TargetRange", "TargetRange", "The range within which players are considered targets.", new CategorySetting.Visibility(this.targetingCategory), 8.0, 1.0, 16.0);
    public NumberSetting maxTargets = new NumberSetting("MaxTargets", "MaxTargets", "Maximum number of targets to evaluate simultaneously.", new CategorySetting.Visibility(this.targetingCategory), 3.0, 1.0, 8.0);
    public NumberSetting bedRange = new NumberSetting("BedRange", "BedRange", "Search cube radius around the target to look for placement spots.", new CategorySetting.Visibility(this.targetingCategory), 4.0, 1.0, 8.0);
    public BooleanSetting ignoreFriends = new BooleanSetting("IgnoreFriends", "Ignore friends when targeting.", new CategorySetting.Visibility(this.targetingCategory), true);
    public CategorySetting damageCategory = new CategorySetting("Damage", "Damage calculation and safety settings.");
    public NumberSetting minDamage = new NumberSetting("MinDamage", "MinDamage", "Minimum predicted damage to target.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
    public NumberSetting maxSelfDamage = new NumberSetting("MaxSelfDamage", "MaxSelfDamage", "Maximum allowed damage to yourself.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
    public NumberSetting minHealth = new NumberSetting("MinHealth", "MinHealth", "Do not act if your health (+absorption) is at or below this.", new CategorySetting.Visibility(this.damageCategory), 4.0, 0.0, 36.0);
    public BooleanSetting antiSuicide = new BooleanSetting("AntiSuicide", "Cancel if self damage drops health below MinHealth.", new CategorySetting.Visibility(this.damageCategory), true);
    public NumberSetting balance = new NumberSetting("Balance", "Balance", "Minimum target to self damage ratio (0 = off).", new CategorySetting.Visibility(this.damageCategory), 0.0, 0.0, 10.0);
    public BooleanSetting predict = new BooleanSetting("Predict", "Extrapolate target velocity for damage estimation.", new CategorySetting.Visibility(this.damageCategory), true);
    public CategorySetting behaviorCategory = new CategorySetting("Behavior", "Settings for placement and detonation behavior.");
    public NumberSetting delay = new NumberSetting("Delay", "Delay", "Delay in milliseconds between actions.", new CategorySetting.Visibility(this.behaviorCategory), 100, 0, 500);
    public NumberSetting range = new NumberSetting("Range", "Range", "Direct reach for placing and detonating beds.", new CategorySetting.Visibility(this.behaviorCategory), 4.5, 1.0, 6.0);
    public NumberSetting wallsRange = new NumberSetting("WallsRange", "WallsRange", "Reach through walls / obstacles.", new CategorySetting.Visibility(this.behaviorCategory), 4.0, 0.0, 6.0);
    public ModeSetting swapMode = new ModeSetting("SwapMode", "SwapMode", "How to swap to the bed and non-bed item.", new CategorySetting.Visibility(this.behaviorCategory), "Silent", new String[]{"Silent", "Normal", "Alt"});
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotate towards bed before placing and detonating.", new CategorySetting.Visibility(this.behaviorCategory), true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Enforce strict raycast line of sight on placement face.", new CategorySetting.Visibility(this.behaviorCategory), false);
    public BooleanSetting airPlace = new BooleanSetting("AirPlace", "Allow placing beds without a solid supporting block below.", new CategorySetting.Visibility(this.behaviorCategory), false);
    public BooleanSetting placeOnFeet = new BooleanSetting("PlaceOnFeet", "Allow bed head to overlap target entity hitbox for maximum damage.", new CategorySetting.Visibility(this.behaviorCategory), true);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pause while eating or consuming items.", new CategorySetting.Visibility(this.behaviorCategory), true);
    public CategorySetting autoCraftCategory = new CategorySetting("AutoCraft", "Automated bed crafting from wool and planks.");
    public BooleanSetting autoCraft = new BooleanSetting("AutoCraft", "Enable automated bed crafting.", new CategorySetting.Visibility(this.autoCraftCategory), true);
    public BindSetting autoCraftBind = new BindSetting("AutoCraftBind", "Keybind to toggle AutoCraft.", new CategorySetting.Visibility(this.autoCraftCategory), -1).disableHoldModes();
    public BooleanSetting autoPlaceTable = new BooleanSetting("AutoPlace", "Place and open a crafting table when out of beds.", new CategorySetting.Visibility(this.autoCraftCategory), true);
    public BooleanSetting autoCloseTable = new BooleanSetting("AutoClose", "Close the crafting table GUI automatically when crafting is finished.", new CategorySetting.Visibility(this.autoCraftCategory), true);
    public CategorySetting renderCategory = new CategorySetting("Render", "Placement preview rendering.");
    public BooleanSetting renderPreview = new BooleanSetting("Render", "Render preview box at target bed placement.", new CategorySetting.Visibility(this.renderCategory), true);
    public BooleanSetting interpolate = new BooleanSetting("Interpolate", "Smooth linear grow-in animation for preview box.", new CategorySetting.Visibility(this.renderCategory), true);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "Mode", "Render mode for preview box.", new CategorySetting.Visibility(this.renderCategory), "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "FillColor", "Color used for fill rendering.", new CategorySetting.Visibility(this.renderCategory), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "OutlineColor", "Color used for outline rendering.", new CategorySetting.Visibility(this.renderCategory), ColorUtils.getDefaultOutlineColor());
    private static final int RECOMPUTE_INTERVAL_TICKS = 10;
    private int recomputeTicks = 0;
    private BlockPos currentFoot = null;
    private Direction currentDir = null;
    private Player currentTarget = null;
    private float currentEstimatedDamage = 0.0f;
    private BlockPos currentHead = null;
    private boolean currentAdopt = false;
    private long lastActionMs = 0L;
    private boolean bedPlacedThisCycle = false;
    private BlockPos placedBedPos = null;
    private BlockPos placedBedHeadPos = null;
    private PlacementCandidate currentPlacement = null;
    private final List<Player> targets = new ArrayList<Player>();
    private long placementRenderMs = 0L;
    private static final long PLACEMENT_ANIM_MS = 350L;
    private BlockPos lastRenderedPos = null;
    private boolean autoCraftRunning = false;
    private int autoCraftTicks = 0;
    private BlockPos placedTablePos = null;
    private boolean tableOpenSent = false;
    private boolean lastHadBeds = true;
    private static final int CRAFTING_RESULT_SLOT = 0;
    private static final int[] WOOL_GRID_SLOTS = new int[]{1, 2, 3};
    private static final int[] PLANK_GRID_SLOTS = new int[]{4, 5, 6};
    private static final int CRAFTING_CONTAINER_SIZE = 10;

    @Override
    public void onEnable() {
        this.clearSelection();
        this.recomputeTicks = 0;
        this.bedPlacedThisCycle = false;
        this.placedBedPos = null;
        this.placedBedHeadPos = null;
        this.autoCraftRunning = false;
        this.placedTablePos = null;
        this.tableOpenSent = false;
    }

    @Override
    public void onDisable() {
        this.clearSelection();
        this.autoCraftRunning = false;
        this.placedTablePos = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (BedAuraModule.mc.player == null || BedAuraModule.mc.level == null) {
            return;
        }
        if (this.autoCraft.getValue() && this.autoPlaceTable.getValue()) {
            boolean hasBeds = this.countBeds() > 0;
            if (!hasBeds && this.lastHadBeds) {
                this.startAutoCraft();
            }
            this.lastHadBeds = hasBeds;
        }
        if (this.autoCraftRunning) {
            this.handleAutoCraftTick();
        }
        this.runBedCycle();
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent keyInputEvent) {
        if (BedAuraModule.mc.gui.screen() != null) {
            return;
        }
        if (this.autoCraftBind.getValue() != -1 && keyInputEvent.getKey() == this.autoCraftBind.getValue()) {
            this.startAutoCraft();
            Night.CHAT_MANAGER.tagged("AutoCraft triggered!", "BedAura");
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent mouseInputEvent) {
        if (BedAuraModule.mc.gui.screen() != null) {
            return;
        }
        if (this.autoCraftBind.getValue() != -1 && mouseInputEvent.getButton() == this.autoCraftBind.getValue()) {
            this.startAutoCraft();
            Night.CHAT_MANAGER.tagged("AutoCraft triggered!", "BedAura");
        }
    }

    private void runBedCycle() {
        ResourceKey resourceKey = BedAuraModule.mc.player.level().dimension();
        if (resourceKey != Level.NETHER && resourceKey != Level.END) {
            this.clearSelection();
            return;
        }
        if (this.pauseOnEat.getValue() && BedAuraModule.mc.player.isUsingItem() && (BedAuraModule.mc.player.getUseItem().has(DataComponents.FOOD) || BedAuraModule.mc.player.getUseItem().has(DataComponents.POTION_CONTENTS))) {
            return;
        }
        float effectiveHealth = BedAuraModule.mc.player.getHealth() + BedAuraModule.mc.player.getAbsorptionAmount();
        if (effectiveHealth <= this.minHealth.getValue().floatValue()) {
            return;
        }
        long nowMs = System.currentTimeMillis();
        if (nowMs - this.lastActionMs < this.delay.getValue().longValue()) {
            return;
        }
        if (this.bedPlacedThisCycle) {
            this.submitDetonate();
            return;
        }
        boolean hasBedInHotbar = this.findBedSlot() != -1;
        if (!hasBedInHotbar) {
            if (this.autoCraft.getValue() && this.countBeds() == 0 && !this.autoCraftRunning) {
                this.startAutoCraft();
            }
            this.clearSelection();
            return;
        }
        this.updateTargets();
        if (this.targets.isEmpty()) {
            this.clearSelection();
            return;
        }
        if (this.recomputeTicks <= 0) {
            this.recomputeTicks = 10;
            ExistingBed existingBed = this.findExistingBed();
            if (existingBed != null) {
                this.currentFoot = existingBed.pos();
                this.currentHead = this.findOtherBedHalf(existingBed.pos());
                this.currentDir = null;
                this.currentTarget = existingBed.target();
                this.currentAdopt = true;
                this.currentEstimatedDamage = existingBed.damage();
                this.bedPlacedThisCycle = true;
                this.placedBedPos = this.currentFoot;
                this.placedBedHeadPos = this.currentHead;
                this.currentPlacement = null;
                this.submitDetonate();
                return;
            }
            JointPlacement jointPlacement = this.searchPlacement();
            if (jointPlacement == null) {
                this.clearSelection();
                return;
            }
            this.currentFoot = jointPlacement.foot();
            this.currentDir = jointPlacement.dir();
            this.currentTarget = jointPlacement.target();
            this.currentEstimatedDamage = jointPlacement.estimatedDamage();
            this.currentAdopt = false;
            this.currentHead = this.currentFoot.relative(this.currentDir);
        } else {
            --this.recomputeTicks;
            if (this.currentFoot != null && !this.currentAdopt && !this.currentSelectionStillGood()) {
                this.recomputeTicks = 0;
            }
        }
        if (this.currentFoot == null || this.currentDir == null || this.currentTarget == null) {
            this.clearSelection();
            return;
        }
        this.currentPlacement = new PlacementCandidate(this.currentFoot, this.currentDir, this.currentEstimatedDamage);
        this.submitPlaceBed(this.currentFoot, this.currentDir);
    }

    private void submitPlaceBed(BlockPos blockPos, Direction direction) {
        BlockHitResult blockHitResult = this.computeBedHit(blockPos);
        if (blockHitResult == null) {
            this.clearSelection();
            this.recomputeTicks = 4;
            return;
        }
        float yaw = direction.toYRot();
        float[] hitRotations = RotationUtils.getRotations(blockHitResult.getLocation());
        float pitch = hitRotations[1];
        Night.ROTATION_MANAGER.silentRotate(yaw, pitch);
        if (this.executePlaceBed(blockPos, blockHitResult)) {
            this.bedPlacedThisCycle = true;
            this.placedBedPos = blockPos;
            this.placedBedHeadPos = blockPos.relative(direction);
            this.lastActionMs = System.currentTimeMillis();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void submitDetonate() {
        BlockHitResult blockHitResult;
        if (this.placedBedPos == null) {
            this.bedPlacedThisCycle = false;
            return;
        }
        BlockPos blockPos = this.placedBedPos;
        if (this.placedBedHeadPos != null && this.currentTarget != null) {
            float footDamage = this.computeDamage(Vec3.atCenterOf((Vec3i)this.placedBedPos), this.currentTarget);
            float headDamage = this.computeDamage(Vec3.atCenterOf((Vec3i)this.placedBedHeadPos), this.currentTarget);
            if (headDamage > footDamage) {
                blockPos = this.placedBedHeadPos;
            }
        }
        if ((blockHitResult = this.computeBedFace(blockPos)) == null) {
            this.bedPlacedThisCycle = false;
            this.placedBedPos = null;
            this.placedBedHeadPos = null;
            this.currentPlacement = null;
            this.recomputeTicks = 4;
            return;
        }
        int detonateSlot = this.findDetonateSlot();
        float[] rotations = RotationUtils.getRotations(blockHitResult.getLocation());
        Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
        int prevSlot = BedAuraModule.mc.player.getInventory().getSelectedSlot();
        boolean switched = false;
        String switchMode = this.swapMode.getValue().equalsIgnoreCase("Alt") ? "AltPickup" : this.swapMode.getValue();
        if (detonateSlot != Integer.MIN_VALUE) {
            switched = InventoryUtils.switchSlot(switchMode, detonateSlot, prevSlot);
        }
        try {
            BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult);
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        finally {
            if (switched) {
                InventoryUtils.switchBack(switchMode, detonateSlot, prevSlot);
            }
        }
        this.bedPlacedThisCycle = false;
        this.placedBedPos = null;
        this.placedBedHeadPos = null;
        this.currentPlacement = null;
        this.lastActionMs = System.currentTimeMillis();
    }

    private boolean executePlaceBed(BlockPos blockPos, BlockHitResult blockHitResult) {
        boolean switched;
        int prevSlot;
        String switchMode;
        int bedSlot;
        block11: {
            bedSlot = this.findBedSlot();
            if (bedSlot == -1) {
                return false;
            }
            switchMode = this.swapMode.getValue().equalsIgnoreCase("Alt") ? "AltPickup" : this.swapMode.getValue();
            switched = InventoryUtils.switchSlot(switchMode, bedSlot, prevSlot = BedAuraModule.mc.player.getInventory().getSelectedSlot());
            if (!switched && mc.getConnection() != null) {
                mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket(BedAuraModule.mc.player.getInventory().getSelectedSlot()));
            }
            boolean needSneak = WorldUtils.isInteractable(BedAuraModule.mc.level.getBlockState(blockHitResult.getBlockPos())) && !BedAuraModule.mc.player.isShiftKeyDown();
            if (needSneak) {
                Input currentInput = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
                Input sneakInput = new Input(currentInput.forward(), currentInput.backward(), currentInput.left(), currentInput.right(), currentInput.jump(), true, currentInput.sprint());
                mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(sneakInput));
                if (BedAuraModule.mc.player instanceof ClientPlayerEntityAccessor accessor) {
                    accessor.setLastSentInput(sneakInput);
                }
            }
            Input restoredInput;
            try {
                BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult);
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (!needSneak) break block11;
                restoredInput = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
            }
            catch (Throwable throwable) {
                if (needSneak) {
                    Input input = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
                    mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(input));
                    if (BedAuraModule.mc.player instanceof ClientPlayerEntityAccessor accessor) {
                        accessor.setLastSentInput(input);
                    }
                }
                if (switched) {
                    InventoryUtils.switchBack(switchMode, bedSlot, prevSlot);
                }
                throw throwable;
            }
            mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(restoredInput));
            if (BedAuraModule.mc.player instanceof ClientPlayerEntityAccessor accessor) {
                accessor.setLastSentInput(restoredInput);
            }
        }
        if (switched) {
            InventoryUtils.switchBack(switchMode, bedSlot, prevSlot);
        }
        return true;
    }

    private boolean canSeePlacementFace(Vec3 eyePos, Vec3 targetPos, BlockPos blockPos) {
        BlockHitResult blockHitResult = BedAuraModule.mc.level.clip(new ClipContext(eyePos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)BedAuraModule.mc.player));
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return true;
        }
        if (blockHitResult instanceof BlockHitResult) {
            BlockHitResult hit = blockHitResult;
            return hit.getBlockPos().equals((Object)blockPos);
        }
        return false;
    }

    private BlockHitResult computeBedHit(BlockPos blockPos) {
        double maxRange = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        if (!this.withinReach(blockPos, maxRange)) {
            return null;
        }
        BlockPos belowPos = blockPos.below();
        boolean hasSupport = !BedAuraModule.mc.level.getBlockState(belowPos).canBeReplaced();
        Vec3 eyePos = BedAuraModule.mc.player.getEyePosition();
        if (hasSupport) {
            Vec3 hitVec = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
            if (this.strictDirection.getValue() && (eyePos.y < (double)blockPos.getY() || !this.canSeePlacementFace(eyePos, hitVec, belowPos))) {
                return null;
            }
            return new BlockHitResult(hitVec, Direction.UP, belowPos, false);
        }
        if (!this.airPlace.getValue()) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = blockPos.relative(direction);
            if (BedAuraModule.mc.level.getBlockState(neighborPos).canBeReplaced()) continue;
            Direction oppositeDir = direction.getOpposite();
            Vec3 hitVec = new Vec3((double)blockPos.getX() + 0.5 - (double)direction.getStepX() * 0.5, (double)blockPos.getY() + 0.5 - (double)direction.getStepY() * 0.5, (double)blockPos.getZ() + 0.5 - (double)direction.getStepZ() * 0.5);
            if (this.strictDirection.getValue() && !this.canSeePlacementFace(eyePos, hitVec, neighborPos)) continue;
            return new BlockHitResult(hitVec, oppositeDir, neighborPos, false);
        }
        if (this.strictDirection.getValue()) {
            return null;
        }
        Vec3 airHitVec = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
        return new BlockHitResult(airHitVec, Direction.UP, blockPos, false);
    }

    private BlockHitResult computeBedFace(BlockPos blockPos) {
        Vec3 eyePos = BedAuraModule.mc.player.getEyePosition();
        double maxRange = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        Vec3 centerVec = Vec3.atCenterOf((Vec3i)blockPos);
        BlockHitResult bestVisibleHit = null;
        double bestVisibleDist = Double.MAX_VALUE;
        BlockHitResult bestFallbackHit = null;
        double bestFallbackDist = Double.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            BlockHitResult clipHit;
            BlockPos hitPos;
            BlockHitResult blockHit;
            Vec3 normal = new Vec3((double)direction.getStepX(), (double)direction.getStepY(), (double)direction.getStepZ());
            Vec3 faceVec = centerVec.add(normal.scale(0.5));
            if (eyePos.subtract(faceVec).dot(normal) <= 0.0) continue;
            double dist = faceVec.distanceTo(eyePos);
            if (dist > maxRange) continue;
            if (dist < bestFallbackDist) {
                bestFallbackDist = dist;
                bestFallbackHit = new BlockHitResult(faceVec, direction, blockPos, false);
            }
            if (!(((blockHit = BedAuraModule.mc.level.clip(new ClipContext(eyePos, faceVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)BedAuraModule.mc.player))).getType() != HitResult.Type.BLOCK || !(blockHit instanceof BlockHitResult) || (hitPos = (clipHit = blockHit).getBlockPos()).equals((Object)blockPos) || this.placedBedPos != null && hitPos.equals((Object)this.placedBedPos) || this.placedBedHeadPos != null && hitPos.equals((Object)this.placedBedHeadPos)) && dist < bestVisibleDist)) continue;
            bestVisibleDist = dist;
            bestVisibleHit = new BlockHitResult(faceVec, direction, blockPos, false);
        }
        return bestVisibleHit != null ? bestVisibleHit : bestFallbackHit;
    }

    private JointPlacement searchPlacement() {
        int searchRadius = (int)Math.ceil(this.bedRange.getValue().doubleValue());
        double minDmg = this.minDamage.getValue().doubleValue();
        double maxSelfDmg = this.maxSelfDamage.getValue().doubleValue();
        double maxRange = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        JointPlacement bestPlacement = null;
        double bestScore = -1.0;
        Vec3 eyePos = BedAuraModule.mc.player.getEyePosition();
        double maxCenterDist = maxRange + 0.5;
        for (Player player : this.targets) {
            int maxZ;
            int minZ;
            int maxY;
            int minY;
            int maxX;
            int minX;
            BlockPos playerPos = player.blockPosition();
            Vec3 targetCenter = player.getBoundingBox().getCenter();
            if (Vec3.atCenterOf((Vec3i)playerPos).distanceTo(eyePos) > (double)searchRadius * Math.sqrt(3.0) + maxCenterDist || (minX = Math.max(-searchRadius, (int)Math.floor(eyePos.x - maxCenterDist - (double)playerPos.getX()))) > (maxX = Math.min(searchRadius, (int)Math.ceil(eyePos.x + maxCenterDist - (double)playerPos.getX()))) || (minY = Math.max(-searchRadius, (int)Math.floor(eyePos.y - maxCenterDist - (double)playerPos.getY()))) > (maxY = Math.min(searchRadius, (int)Math.ceil(eyePos.y + maxCenterDist - (double)playerPos.getY()))) || (minZ = Math.max(-searchRadius, (int)Math.floor(eyePos.z - maxCenterDist - (double)playerPos.getZ()))) > (maxZ = Math.min(searchRadius, (int)Math.ceil(eyePos.z + maxCenterDist - (double)playerPos.getZ())))) continue;
            for (int i = minX; i <= maxX; ++i) {
                for (int j = minY; j <= maxY; ++j) {
                    for (int k = minZ; k <= maxZ; ++k) {
                        BlockPos footPos = playerPos.offset(i, j, k);
                        if (Vec3.atCenterOf((Vec3i)footPos).distanceTo(eyePos) > maxCenterDist || !this.footPlaceable(footPos, maxRange)) continue;
                        for (Direction direction : Direction.Plane.HORIZONTAL) {
                            BlockPos headPos = footPos.relative(direction);
                            if (!this.headPlaceable(headPos)) continue;
                            Vec3 footCenter = Vec3.atCenterOf((Vec3i)footPos);
                            Vec3 headCenter = Vec3.atCenterOf((Vec3i)headPos);
                            float footDamage = this.computeDamage(footCenter, player);
                            float headDamage = this.computeDamage(headCenter, player);
                            boolean useHead = headDamage >= footDamage;
                            Vec3 detonateVec = useHead ? headCenter : footCenter;
                            float targetDamage = Math.max(footDamage, headDamage);
                            if ((double)targetDamage < minDmg) continue;
                            float selfDamage = this.computeDamage(detonateVec, (Player)BedAuraModule.mc.player);
                            if ((double)selfDamage > maxSelfDmg) continue;
                            float effectiveHealth = BedAuraModule.mc.player.getHealth() + BedAuraModule.mc.player.getAbsorptionAmount();
                            if (this.antiSuicide.getValue() && effectiveHealth - selfDamage <= this.minHealth.getValue().floatValue() || this.balance.getValue().doubleValue() > 0.0 && selfDamage > 0.0f && (double)(targetDamage / selfDamage) < this.balance.getValue().doubleValue()) continue;
                            double score = targetDamage;
                            if (headCenter.distanceTo(targetCenter) < footCenter.distanceTo(targetCenter)) {
                                score += 0.01;
                            }
                            if (bestPlacement != null && !(score > bestScore)) continue;
                            bestPlacement = new JointPlacement(footPos, direction, player, targetDamage);
                            bestScore = score;
                        }
                    }
                }
            }
        }
        return bestPlacement;
    }

    private boolean footPlaceable(BlockPos blockPos, double maxRange) {
        if (!this.withinReach(blockPos, maxRange)) {
            return false;
        }
        if (!BedAuraModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        if (!this.airPlace.getValue() && BedAuraModule.mc.level.getBlockState(blockPos.below()).canBeReplaced()) {
            return false;
        }
        return !this.strictDirection.getValue() || this.computeBedHit(blockPos) != null;
    }

    private boolean headPlaceable(BlockPos blockPos) {
        if (!BedAuraModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        if (this.placeOnFeet.getValue()) {
            return true;
        }
        return !this.entityAt(blockPos);
    }

    private boolean entityAt(BlockPos blockPos) {
        AABB aABB = new AABB(blockPos);
        for (Entity entity : BedAuraModule.mc.level.entitiesForRendering()) {
            if (entity == BedAuraModule.mc.player || entity instanceof ItemEntity || !entity.isAlive() || !entity.getBoundingBox().intersects(aABB)) continue;
            return true;
        }
        return false;
    }

    private boolean currentSelectionStillGood() {
        float headDamage;
        if (this.currentFoot == null || this.currentDir == null || this.currentTarget == null) {
            return false;
        }
        if (!this.currentTarget.isAlive() || this.currentTarget.getHealth() <= 0.0f) {
            return false;
        }
        BlockPos headPos = this.currentFoot.relative(this.currentDir);
        double maxRange = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        if (!this.withinReach(this.currentFoot, maxRange) && !this.withinReach(headPos, maxRange)) {
            return false;
        }
        if (this.strictDirection.getValue() && this.computeBedHit(this.currentFoot) == null) {
            return false;
        }
        Vec3 footCenter = Vec3.atCenterOf((Vec3i)this.currentFoot);
        Vec3 headCenter = Vec3.atCenterOf((Vec3i)headPos);
        float footDamage = this.computeDamage(footCenter, this.currentTarget);
        float maxDamage = Math.max(footDamage, headDamage = this.computeDamage(headCenter, this.currentTarget));
        if ((double)maxDamage < this.minDamage.getValue().doubleValue()) {
            return false;
        }
        Vec3 detonateVec = headDamage >= footDamage ? headCenter : footCenter;
        float selfDamage = this.computeDamage(detonateVec, (Player)BedAuraModule.mc.player);
        if ((double)selfDamage > this.maxSelfDamage.getValue().doubleValue()) {
            return false;
        }
        this.currentEstimatedDamage = maxDamage;
        return true;
    }

    private ExistingBed findExistingBed() {
        double maxRange = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        int searchRadius = (int)Math.ceil(this.bedRange.getValue().doubleValue());
        ExistingBed bestBed = null;
        for (Player player : this.targets) {
            BlockPos playerPos = player.blockPosition();
            Vec3 targetCenter = player.getBoundingBox().getCenter();
            for (int i = -searchRadius; i <= searchRadius; ++i) {
                for (int j = -searchRadius; j <= searchRadius; ++j) {
                    for (int k = -searchRadius; k <= searchRadius; ++k) {
                        float targetDamage;
                        float selfDamage;
                        BlockPos bedPos = playerPos.offset(i, j, k);
                        if (!(BedAuraModule.mc.level.getBlockState(bedPos).getBlock() instanceof BedBlock) || Vec3.atCenterOf((Vec3i)bedPos).distanceTo(targetCenter) > 6.0 || !this.withinReach(bedPos, maxRange) || (double)(selfDamage = this.computeDamage(Vec3.atCenterOf((Vec3i)bedPos), (Player)BedAuraModule.mc.player)) > this.maxSelfDamage.getValue().doubleValue() || (double)(targetDamage = this.computeDamage(Vec3.atCenterOf((Vec3i)bedPos), player)) < this.minDamage.getValue().doubleValue() || bestBed != null && !(targetDamage > bestBed.damage())) continue;
                        bestBed = new ExistingBed(bedPos, player, targetDamage);
                    }
                }
            }
        }
        return bestBed;
    }

    private float computeDamage(Vec3 explosionPos, Player player) {
        if (this.predict.getValue()) {
            Vec3 predictedPos = player.position().add(player.getDeltaMovement());
            AABB predictedBox = player.getBoundingBox().move(predictedPos.subtract(player.position()));
            return DamageUtils.getDamage((Entity)player, predictedBox, explosionPos, 5.0f, null, false);
        }
        return DamageUtils.getDamage((Entity)player, player.getBoundingBox(), explosionPos, 5.0f, null, false);
    }

    private boolean withinReach(BlockPos blockPos, double maxRange) {
        return Vec3.atCenterOf((Vec3i)blockPos).distanceTo(BedAuraModule.mc.player.getEyePosition()) <= maxRange + 0.5;
    }

    private void updateTargets() {
        this.targets.clear();
        Vec3 eyePos = BedAuraModule.mc.player.getEyePosition();
        ArrayList<Player> candidates = new ArrayList<Player>();
        for (Entity entity : BedAuraModule.mc.level.entitiesForRendering()) {
            Player candidate;
            if (!(entity instanceof Player) || (candidate = (Player)entity) == BedAuraModule.mc.player || !candidate.isAlive() || candidate.getHealth() <= 0.0f || this.ignoreFriends.getValue() && Night.FRIEND_MANAGER.contains(candidate.getName().getString()) || Vec3.atCenterOf((Vec3i)candidate.blockPosition()).distanceTo(eyePos) > this.targetRange.getValue().doubleValue() && (double)candidate.distanceTo((Entity)BedAuraModule.mc.player) > this.targetRange.getValue().doubleValue()) continue;
            candidates.add(candidate);
        }
        Comparator<Player> comparator = switch (this.targetMode.getValue()) {
            case "Furthest" -> Comparator.comparingDouble((Player player) -> player.distanceTo((Entity)BedAuraModule.mc.player)).reversed();
            case "Health" -> Comparator.comparingDouble(LivingEntity::getHealth);
            default -> Comparator.comparingDouble(player -> player.distanceTo((Entity)BedAuraModule.mc.player));
        };
        candidates.sort(comparator);
        int maxCount = this.maxTargets.getValue().intValue();
        for (Player candidate : candidates) {
            if (this.targets.size() >= maxCount) break;
            this.targets.add(candidate);
        }
    }

    private BlockPos findOtherBedHalf(BlockPos blockPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = blockPos.relative(direction);
            if (!(BedAuraModule.mc.level.getBlockState(neighborPos).getBlock() instanceof BedBlock)) continue;
            return neighborPos;
        }
        return null;
    }

    private int findBedSlot() {
        boolean altSwap = this.swapMode.getValue().equalsIgnoreCase("Alt");
        Predicate<ItemStack> predicate = itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof BedItem;
        if (altSwap) {
            for (int i = 0; i < 36; ++i) {
                if (!predicate.test(BedAuraModule.mc.player.getInventory().getItem(i))) continue;
                return i;
            }
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (!predicate.test(BedAuraModule.mc.player.getInventory().getItem(i))) continue;
            return i;
        }
        return -1;
    }

    private int findDetonateSlot() {
        int slot;
        boolean altSwap = this.swapMode.getValue().equalsIgnoreCase("Alt");
        Predicate<ItemStack> predicate = itemStack -> !itemStack.isEmpty() && !(itemStack.getItem() instanceof BedItem) && !(itemStack.getItem() instanceof BlockItem);
        if (altSwap) {
            for (slot = 0; slot < 36; ++slot) {
                if (!predicate.test(BedAuraModule.mc.player.getInventory().getItem(slot))) continue;
                return slot;
            }
        } else {
            for (slot = 0; slot < 9; ++slot) {
                if (!predicate.test(BedAuraModule.mc.player.getInventory().getItem(slot))) continue;
                return slot;
            }
        }
        for (slot = 0; slot < 9; ++slot) {
            if (!BedAuraModule.mc.player.getInventory().getItem(slot).isEmpty()) continue;
            return slot;
        }
        for (slot = 0; slot < 9; ++slot) {
            if (BedAuraModule.mc.player.getInventory().getItem(slot).getItem() instanceof BedItem) continue;
            return slot;
        }
        return Integer.MIN_VALUE;
    }

    private void clearSelection() {
        this.currentFoot = null;
        this.currentDir = null;
        this.currentTarget = null;
        this.currentEstimatedDamage = 0.0f;
        this.currentHead = null;
        this.currentAdopt = false;
        this.currentPlacement = null;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        BlockPos headPos;
        BlockPos footPos;
        if (!this.renderPreview.getValue() || BedAuraModule.mc.level == null || BedAuraModule.mc.player == null) {
            return;
        }
        if (this.bedPlacedThisCycle && this.placedBedPos != null) {
            footPos = this.placedBedPos;
            headPos = this.placedBedHeadPos != null ? this.placedBedHeadPos : this.findOtherBedHalf(footPos);
            if (headPos != null) {
                this.placedBedHeadPos = headPos;
            }
        } else if (this.currentPlacement != null) {
            footPos = this.currentPlacement.pos();
            headPos = footPos.relative(this.currentPlacement.dir());
        } else if (this.currentFoot != null) {
            footPos = this.currentFoot;
            headPos = this.currentHead != null ? this.currentHead : (this.currentDir != null ? this.currentFoot.relative(this.currentDir) : this.findOtherBedHalf(footPos));
        } else {
            this.lastRenderedPos = null;
            return;
        }
        if (!footPos.equals((Object)this.lastRenderedPos)) {
            this.lastRenderedPos = footPos;
            this.placementRenderMs = System.currentTimeMillis();
        }
        float scale = 1.0f;
        if (this.interpolate.getValue()) {
            long elapsedMs = System.currentTimeMillis() - this.placementRenderMs;
            float progress = Math.min(1.0f, (float)elapsedMs / 350.0f);
            scale = 0.15f + 0.85f * progress;
        }
        PoseStack poseStack = renderWorldEvent.getMatrices();
        double minX = footPos.getX();
        double minY = footPos.getY();
        double minZ = footPos.getZ();
        double maxX = (double)footPos.getX() + 1.0;
        double maxY = (double)footPos.getY() + 0.5625;
        double maxZ = (double)footPos.getZ() + 1.0;
        if (headPos != null) {
            minX = Math.min(footPos.getX(), headPos.getX());
            minY = Math.min(footPos.getY(), headPos.getY());
            minZ = Math.min(footPos.getZ(), headPos.getZ());
            maxX = (double)Math.max(footPos.getX(), headPos.getX()) + 1.0;
            maxY = (double)Math.max(footPos.getY(), headPos.getY()) + 0.5625;
            maxZ = (double)Math.max(footPos.getZ(), headPos.getZ()) + 1.0;
        }
        double centerX = (minX + maxX) * 0.5;
        double centerY = (minY + maxY) * 0.5;
        double centerZ = (minZ + maxZ) * 0.5;
        double halfX = (maxX - minX) * 0.5 * (double)scale;
        double halfY = (maxY - minY) * 0.5 * (double)scale;
        double halfZ = (maxZ - minZ) * 0.5 * (double)scale;
        AABB aABB = new AABB(centerX - halfX, centerY - halfY, centerZ - halfZ, centerX + halfX, centerY + halfY, centerZ + halfZ);
        Color fill = this.fillColor.getColor();
        Color outline = this.outlineColor.getColor();
        boolean drawFill = this.renderMode.getValue().equalsIgnoreCase("Fill") || this.renderMode.getValue().equalsIgnoreCase("Both");
        boolean drawOutline = this.renderMode.getValue().equalsIgnoreCase("Outline") || this.renderMode.getValue().equalsIgnoreCase("Both");
        if (drawFill) {
            Renderer3D.renderBox(poseStack, aABB, fill);
        }
        if (drawOutline) {
            Renderer3D.renderBoxOutline(poseStack, aABB, outline);
        }
    }

    private void startAutoCraft() {
        if (this.autoCraftRunning) {
            return;
        }
        this.autoCraftRunning = true;
        this.autoCraftTicks = 0;
        this.placedTablePos = null;
        this.tableOpenSent = false;
    }

    private void handleAutoCraftTick() {
        ++this.autoCraftTicks;
        if (this.autoCraftTicks > 200) {
            this.autoCraftRunning = false;
            return;
        }
        boolean inCraftingMenu = BedAuraModule.mc.player.containerMenu instanceof CraftingMenu;
        if (!inCraftingMenu) {
            if (!this.autoPlaceTable.getValue()) {
                this.autoCraftRunning = false;
                return;
            }
            if (this.placedTablePos == null) {
                this.placedTablePos = this.tryPlaceCraftingTable();
                if (this.placedTablePos == null) {
                    this.autoCraftRunning = false;
                    return;
                }
                this.tableOpenSent = false;
                this.autoCraftTicks = 0;
                return;
            }
            boolean tablePlaced = BedAuraModule.mc.level.getBlockState(this.placedTablePos).getBlock() == Blocks.CRAFTING_TABLE;
            if (tablePlaced && !this.tableOpenSent) {
                if (this.rotate.getValue()) {
                    float[] rotations = RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)this.placedTablePos));
                    Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
                }
                BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)this.placedTablePos), Direction.UP, this.placedTablePos, false));
                this.tableOpenSent = true;
                this.autoCraftTicks = 0;
                return;
            }
            if (this.autoCraftTicks > 40) {
                this.autoCraftRunning = false;
                this.placedTablePos = null;
                this.tableOpenSent = false;
            }
            return;
        }
        this.placedTablePos = null;
        this.tableOpenSent = false;
        AbstractContainerMenu abstractContainerMenu = BedAuraModule.mc.player.containerMenu;
        if (!this.gridHasMaterials(abstractContainerMenu) && !this.fillCraftingGrid(abstractContainerMenu)) {
            if (this.autoCloseTable.getValue()) {
                BedAuraModule.mc.player.closeContainer();
            }
            this.autoCraftRunning = false;
            return;
        }
        BedAuraModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, 0, 0, ContainerInput.QUICK_MOVE, (Player)BedAuraModule.mc.player);
        if (this.isInventoryFull()) {
            if (this.autoCloseTable.getValue()) {
                BedAuraModule.mc.player.closeContainer();
            }
            this.autoCraftRunning = false;
        }
    }

    private int countBeds() {
        int totalBeds = 0;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = BedAuraModule.mc.player.getInventory().getItem(i);
            if (!(itemStack.getItem() instanceof BedItem)) continue;
            totalBeds += itemStack.getCount();
        }
        return totalBeds;
    }

    private boolean isInventoryFull() {
        for (int i = 0; i < 36; ++i) {
            if (!BedAuraModule.mc.player.getInventory().getItem(i).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean gridHasMaterials(AbstractContainerMenu abstractContainerMenu) {
        for (int slot : WOOL_GRID_SLOTS) {
            if (this.isWool(abstractContainerMenu.getSlot(slot).getItem())) continue;
            return false;
        }
        for (int slot : PLANK_GRID_SLOTS) {
            if (this.isPlanks(abstractContainerMenu.getSlot(slot).getItem())) continue;
            return false;
        }
        return true;
    }

    private boolean isWool(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ItemTags.WOOL);
    }

    private boolean isPlanks(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ItemTags.PLANKS);
    }

    private boolean fillCraftingGrid(AbstractContainerMenu abstractContainerMenu) {
        int sourceSlot;
        int woolSlot = this.findMenuSlot(abstractContainerMenu, this::isWool);
        int plankSlot = this.findMenuSlot(abstractContainerMenu, this::isPlanks);
        if (woolSlot == -1 || plankSlot == -1) {
            return false;
        }
        for (int gridSlot : WOOL_GRID_SLOTS) {
            if (this.isWool(abstractContainerMenu.getSlot(gridSlot).getItem())) continue;
            sourceSlot = this.findMenuSlot(abstractContainerMenu, this::isWool);
            if (sourceSlot == -1) {
                return false;
            }
            this.placeOneItem(abstractContainerMenu, sourceSlot, gridSlot);
        }
        for (int gridSlot : PLANK_GRID_SLOTS) {
            if (this.isPlanks(abstractContainerMenu.getSlot(gridSlot).getItem())) continue;
            sourceSlot = this.findMenuSlot(abstractContainerMenu, this::isPlanks);
            if (sourceSlot == -1) {
                return false;
            }
            this.placeOneItem(abstractContainerMenu, sourceSlot, gridSlot);
        }
        return true;
    }

    private void placeOneItem(AbstractContainerMenu abstractContainerMenu, int sourceSlot, int targetSlot) {
        BedAuraModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, sourceSlot, 1, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
        BedAuraModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, targetSlot, 1, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
        BedAuraModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, sourceSlot, 0, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
    }

    private int findMenuSlot(AbstractContainerMenu abstractContainerMenu, Predicate<ItemStack> predicate) {
        for (int i = 10; i < abstractContainerMenu.slots.size(); ++i) {
            if (!predicate.test(abstractContainerMenu.getSlot(i).getItem())) continue;
            return i;
        }
        return -1;
    }

    private BlockPos tryPlaceCraftingTable() {
        int prevSlot;
        int tableSlot = -1;
        for (int i = 0; i < 9; ++i) {
            if (BedAuraModule.mc.player.getInventory().getItem(i).getItem() != Blocks.CRAFTING_TABLE.asItem()) continue;
            tableSlot = i;
            break;
        }
        if (tableSlot == -1) {
            return null;
        }
        BlockPos playerPos = BedAuraModule.mc.player.blockPosition();
        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;
        int searchRadius = (int)Math.ceil(this.range.getValue().doubleValue());
        for (int i = -searchRadius; i <= searchRadius; ++i) {
            for (int j = -searchRadius; j <= searchRadius; ++j) {
                for (int k = -searchRadius; k <= searchRadius; ++k) {
                    BlockPos candidatePos = playerPos.offset(i, j, k);
                    double distSq = BedAuraModule.mc.player.distanceToSqr((double)candidatePos.getX() + 0.5, (double)candidatePos.getY() + 0.5, (double)candidatePos.getZ() + 0.5);
                    if (distSq > this.range.getValue().doubleValue() * this.range.getValue().doubleValue() || !BedAuraModule.mc.level.getBlockState(candidatePos).canBeReplaced() || BedAuraModule.mc.level.getBlockState(candidatePos.below()).canBeReplaced() || new AABB(candidatePos).intersects(BedAuraModule.mc.player.getBoundingBox()) || !(distSq < bestDistSq)) continue;
                    bestDistSq = distSq;
                    bestPos = candidatePos;
                }
            }
        }
        if (bestPos == null) {
            return null;
        }
        BlockPos supportPos = bestPos.below();
        BlockHitResult blockHitResult = new BlockHitResult(new Vec3((double)bestPos.getX() + 0.5, (double)bestPos.getY(), (double)bestPos.getZ() + 0.5), Direction.UP, supportPos, false);
        prevSlot = BedAuraModule.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot("Normal", tableSlot, prevSlot);
        BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult);
        InventoryUtils.switchBack("Normal", tableSlot, prevSlot);
        return bestPos;
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.clearSelection();
        this.autoCraftRunning = false;
    }

    @SubscribeEvent
    public void onConnect(ClientConnectEvent clientConnectEvent) {
        this.clearSelection();
        this.autoCraftRunning = false;
    }

    public record PlacementCandidate(BlockPos pos, Direction dir, float estimatedDamage) {
    }

    private record ExistingBed(BlockPos pos, Player target, float damage) {
    }

    public record JointPlacement(BlockPos foot, Direction dir, Player target, float estimatedDamage) {
    }
}

