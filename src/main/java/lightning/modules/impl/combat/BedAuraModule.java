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
            boolean bl = this.countBeds() > 0;
            boolean bl2 = bl;
            if (!bl && this.lastHadBeds) {
                this.startAutoCraft();
            }
            this.lastHadBeds = bl;
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
        float f = BedAuraModule.mc.player.getHealth() + BedAuraModule.mc.player.getAbsorptionAmount();
        if (f <= this.minHealth.getValue().floatValue()) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastActionMs < this.delay.getValue().longValue()) {
            return;
        }
        if (this.bedPlacedThisCycle) {
            this.submitDetonate();
            return;
        }
        boolean bl = this.findBedSlot() != -1;
        boolean bl2 = bl;
        if (!bl) {
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
        float f = direction.toYRot();
        float[] fArray = RotationUtils.getRotations(blockHitResult.getLocation());
        float f2 = fArray[1];
        Night.ROTATION_MANAGER.silentRotate(f, f2);
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
            float f = this.computeDamage(Vec3.atCenterOf((Vec3i)this.placedBedPos), this.currentTarget);
            float f2 = this.computeDamage(Vec3.atCenterOf((Vec3i)this.placedBedHeadPos), this.currentTarget);
            if (f2 > f) {
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
        int n = this.findDetonateSlot();
        float[] fArray = RotationUtils.getRotations(blockHitResult.getLocation());
        Night.ROTATION_MANAGER.silentRotate(fArray[0], fArray[1]);
        int n2 = BedAuraModule.mc.player.getInventory().getSelectedSlot();
        boolean bl = false;
        String string = this.swapMode.getValue().equalsIgnoreCase("Alt") ? "AltPickup" : this.swapMode.getValue();
        String string2 = string;
        if (n != Integer.MIN_VALUE) {
            bl = InventoryUtils.switchSlot(string, n, n2);
        }
        try {
            BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult);
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        finally {
            if (bl) {
                InventoryUtils.switchBack(string, n, n2);
            }
        }
        this.bedPlacedThisCycle = false;
        this.placedBedPos = null;
        this.placedBedHeadPos = null;
        this.currentPlacement = null;
        this.lastActionMs = System.currentTimeMillis();
    }

    private boolean executePlaceBed(BlockPos blockPos, BlockHitResult blockHitResult) {
        boolean bl;
        int n;
        String string;
        int n2;
        block11: {
            n2 = this.findBedSlot();
            if (n2 == -1) {
                return false;
            }
            string = this.swapMode.getValue().equalsIgnoreCase("Alt") ? "AltPickup" : this.swapMode.getValue();
            bl = InventoryUtils.switchSlot(string, n2, n = BedAuraModule.mc.player.getInventory().getSelectedSlot());
            if (!bl && mc.getConnection() != null) {
                mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket(BedAuraModule.mc.player.getInventory().getSelectedSlot()));
            }
            boolean bl2 = WorldUtils.isInteractable(BedAuraModule.mc.level.getBlockState(blockHitResult.getBlockPos())) && !BedAuraModule.mc.player.isShiftKeyDown();
            boolean bl3 = bl2;
            if (bl2) {
                Input input3 = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
                Input input2 = new Input(input3.forward(), input3.backward(), input3.left(), input3.right(), input3.jump(), true, input3.sprint());
                mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(input2));
                if (BedAuraModule.mc.player instanceof ClientPlayerEntityAccessor accessor) {
                    accessor.setLastSentInput(input2);
                }
            }
            Input restoredInput;
            try {
                BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult);
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (!bl2) break block11;
                restoredInput = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
            }
            catch (Throwable throwable) {
                if (bl2) {
                    Input input = BedAuraModule.mc.player.input != null ? BedAuraModule.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
                    mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(input));
                    if (BedAuraModule.mc.player instanceof ClientPlayerEntityAccessor accessor) {
                        accessor.setLastSentInput(input);
                    }
                }
                if (bl) {
                    InventoryUtils.switchBack(string, n2, n);
                }
                throw throwable;
            }
            mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(restoredInput));
            if (BedAuraModule.mc.player instanceof ClientPlayerEntityAccessor accessor) {
                accessor.setLastSentInput(restoredInput);
            }
        }
        if (bl) {
            InventoryUtils.switchBack(string, n2, n);
        }
        return true;
    }

    private boolean canSeePlacementFace(Vec3 vec3, Vec3 vec32, BlockPos blockPos) {
        BlockHitResult blockHitResult = BedAuraModule.mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)BedAuraModule.mc.player));
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return true;
        }
        if (blockHitResult instanceof BlockHitResult) {
            BlockHitResult blockHitResult2 = blockHitResult;
            return blockHitResult2.getBlockPos().equals((Object)blockPos);
        }
        return false;
    }

    private BlockHitResult computeBedHit(BlockPos blockPos) {
        double d = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        if (!this.withinReach(blockPos, d)) {
            return null;
        }
        BlockPos blockPos2 = blockPos.below();
        boolean bl = !BedAuraModule.mc.level.getBlockState(blockPos2).canBeReplaced();
        Vec3 vec3 = BedAuraModule.mc.player.getEyePosition();
        if (bl) {
            Vec3 vec32 = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
            if (this.strictDirection.getValue() && (vec3.y < (double)blockPos.getY() || !this.canSeePlacementFace(vec3, vec32, blockPos2))) {
                return null;
            }
            return new BlockHitResult(vec32, Direction.UP, blockPos2, false);
        }
        if (!this.airPlace.getValue()) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            BlockPos blockPos3 = blockPos.relative(direction);
            if (BedAuraModule.mc.level.getBlockState(blockPos3).canBeReplaced()) continue;
            Direction direction2 = direction.getOpposite();
            Vec3 vec33 = new Vec3((double)blockPos.getX() + 0.5 - (double)direction.getStepX() * 0.5, (double)blockPos.getY() + 0.5 - (double)direction.getStepY() * 0.5, (double)blockPos.getZ() + 0.5 - (double)direction.getStepZ() * 0.5);
            if (this.strictDirection.getValue() && !this.canSeePlacementFace(vec3, vec33, blockPos3)) continue;
            return new BlockHitResult(vec33, direction2, blockPos3, false);
        }
        if (this.strictDirection.getValue()) {
            return null;
        }
        Vec3 vec34 = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
        return new BlockHitResult(vec34, Direction.UP, blockPos, false);
    }

    private BlockHitResult computeBedFace(BlockPos blockPos) {
        Vec3 vec3 = BedAuraModule.mc.player.getEyePosition();
        double d = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        Vec3 vec32 = Vec3.atCenterOf((Vec3i)blockPos);
        BlockHitResult blockHitResult = null;
        double d2 = Double.MAX_VALUE;
        BlockHitResult blockHitResult2 = null;
        double d3 = Double.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            BlockHitResult blockHitResult3;
            BlockPos blockPos2;
            BlockHitResult blockHitResult4;
            Vec3 vec33 = new Vec3((double)direction.getStepX(), (double)direction.getStepY(), (double)direction.getStepZ());
            Vec3 vec34 = vec32.add(vec33.scale(0.5));
            if (vec3.subtract(vec34).dot(vec33) <= 0.0) continue;
            double d5 = vec34.distanceTo(vec3);
            if (d5 > d) continue;
            if (d5 < d3) {
                d3 = d5;
                blockHitResult2 = new BlockHitResult(vec34, direction, blockPos, false);
            }
            if (!(((blockHitResult4 = BedAuraModule.mc.level.clip(new ClipContext(vec3, vec34, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)BedAuraModule.mc.player))).getType() != HitResult.Type.BLOCK || !(blockHitResult4 instanceof BlockHitResult) || (blockPos2 = (blockHitResult3 = blockHitResult4).getBlockPos()).equals((Object)blockPos) || this.placedBedPos != null && blockPos2.equals((Object)this.placedBedPos) || this.placedBedHeadPos != null && blockPos2.equals((Object)this.placedBedHeadPos)) && d5 < d2)) continue;
            d2 = d5;
            blockHitResult = new BlockHitResult(vec34, direction, blockPos, false);
        }
        return blockHitResult != null ? blockHitResult : blockHitResult2;
    }

    private JointPlacement searchPlacement() {
        int n = (int)Math.ceil(this.bedRange.getValue().doubleValue());
        double d = this.minDamage.getValue().doubleValue();
        double d2 = this.maxSelfDamage.getValue().doubleValue();
        double d3 = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        JointPlacement jointPlacement = null;
        double d4 = -1.0;
        Vec3 vec3 = BedAuraModule.mc.player.getEyePosition();
        double d5 = d3 + 0.5;
        for (Player player : this.targets) {
            int n2;
            int n3;
            int n4;
            int n5;
            int n6;
            int n7;
            BlockPos blockPos = player.blockPosition();
            Vec3 vec32 = player.getBoundingBox().getCenter();
            if (Vec3.atCenterOf((Vec3i)blockPos).distanceTo(vec3) > (double)n * Math.sqrt(3.0) + d5 || (n7 = Math.max(-n, (int)Math.floor(vec3.x - d5 - (double)blockPos.getX()))) > (n6 = Math.min(n, (int)Math.ceil(vec3.x + d5 - (double)blockPos.getX()))) || (n5 = Math.max(-n, (int)Math.floor(vec3.y - d5 - (double)blockPos.getY()))) > (n4 = Math.min(n, (int)Math.ceil(vec3.y + d5 - (double)blockPos.getY()))) || (n3 = Math.max(-n, (int)Math.floor(vec3.z - d5 - (double)blockPos.getZ()))) > (n2 = Math.min(n, (int)Math.ceil(vec3.z + d5 - (double)blockPos.getZ())))) continue;
            for (int i = n7; i <= n6; ++i) {
                for (int j = n5; j <= n4; ++j) {
                    for (int k = n3; k <= n2; ++k) {
                        BlockPos blockPos2 = blockPos.offset(i, j, k);
                        if (Vec3.atCenterOf((Vec3i)blockPos2).distanceTo(vec3) > d5 || !this.footPlaceable(blockPos2, d3)) continue;
                        for (Direction direction : Direction.Plane.HORIZONTAL) {
                            float f;
                            BlockPos blockPos3 = blockPos2.relative(direction);
                            if (!this.headPlaceable(blockPos3)) continue;
                            Vec3 vec33 = Vec3.atCenterOf((Vec3i)blockPos2);
                            Vec3 vec34 = Vec3.atCenterOf((Vec3i)blockPos3);
                            float f2 = this.computeDamage(vec33, player);
                            float f3 = this.computeDamage(vec34, player);
                            boolean bl = f3 >= f2;
                            Vec3 vec35 = bl ? vec34 : vec33;
                            Vec3 vec36 = vec35;
                            float f4 = Math.max(f2, f3);
                            if ((double)f4 < d) continue;
                            float f5 = this.computeDamage(vec35, (Player)BedAuraModule.mc.player);
                            if ((double)f5 > d2) continue;
                            float f6 = BedAuraModule.mc.player.getHealth() + BedAuraModule.mc.player.getAbsorptionAmount();
                            if (this.antiSuicide.getValue() && f6 - f5 <= this.minHealth.getValue().floatValue() || this.balance.getValue().doubleValue() > 0.0 && f5 > 0.0f && (double)(f4 / f5) < this.balance.getValue().doubleValue()) continue;
                            double d6 = f4;
                            if (vec34.distanceTo(vec32) < vec33.distanceTo(vec32)) {
                                d6 += 0.01;
                            }
                            if (jointPlacement != null && !(d6 > d4)) continue;
                            jointPlacement = new JointPlacement(blockPos2, direction, player, f4);
                            d4 = d6;
                        }
                    }
                }
            }
        }
        return jointPlacement;
    }

    private boolean footPlaceable(BlockPos blockPos, double d) {
        if (!this.withinReach(blockPos, d)) {
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
        float f;
        if (this.currentFoot == null || this.currentDir == null || this.currentTarget == null) {
            return false;
        }
        if (!this.currentTarget.isAlive() || this.currentTarget.getHealth() <= 0.0f) {
            return false;
        }
        BlockPos blockPos = this.currentFoot.relative(this.currentDir);
        double d = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        if (!this.withinReach(this.currentFoot, d) && !this.withinReach(blockPos, d)) {
            return false;
        }
        if (this.strictDirection.getValue() && this.computeBedHit(this.currentFoot) == null) {
            return false;
        }
        Vec3 vec3 = Vec3.atCenterOf((Vec3i)this.currentFoot);
        Vec3 vec32 = Vec3.atCenterOf((Vec3i)blockPos);
        float f2 = this.computeDamage(vec3, this.currentTarget);
        float f3 = Math.max(f2, f = this.computeDamage(vec32, this.currentTarget));
        if ((double)f3 < this.minDamage.getValue().doubleValue()) {
            return false;
        }
        Vec3 vec33 = f >= f2 ? vec32 : vec3;
        float f4 = this.computeDamage(vec33, (Player)BedAuraModule.mc.player);
        if ((double)f4 > this.maxSelfDamage.getValue().doubleValue()) {
            return false;
        }
        this.currentEstimatedDamage = f3;
        return true;
    }

    private ExistingBed findExistingBed() {
        double d = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        int n = (int)Math.ceil(this.bedRange.getValue().doubleValue());
        ExistingBed existingBed = null;
        for (Player player : this.targets) {
            BlockPos blockPos = player.blockPosition();
            Vec3 vec3 = player.getBoundingBox().getCenter();
            for (int i = -n; i <= n; ++i) {
                for (int j = -n; j <= n; ++j) {
                    for (int k = -n; k <= n; ++k) {
                        float f;
                        float f2;
                        BlockPos blockPos2 = blockPos.offset(i, j, k);
                        if (!(BedAuraModule.mc.level.getBlockState(blockPos2).getBlock() instanceof BedBlock) || Vec3.atCenterOf((Vec3i)blockPos2).distanceTo(vec3) > 6.0 || !this.withinReach(blockPos2, d) || (double)(f2 = this.computeDamage(Vec3.atCenterOf((Vec3i)blockPos2), (Player)BedAuraModule.mc.player)) > this.maxSelfDamage.getValue().doubleValue() || (double)(f = this.computeDamage(Vec3.atCenterOf((Vec3i)blockPos2), player)) < this.minDamage.getValue().doubleValue() || existingBed != null && !(f > existingBed.damage())) continue;
                        existingBed = new ExistingBed(blockPos2, player, f);
                    }
                }
            }
        }
        return existingBed;
    }

    private float computeDamage(Vec3 vec3, Player player) {
        if (this.predict.getValue()) {
            Vec3 vec32 = player.position().add(player.getDeltaMovement());
            AABB aABB = player.getBoundingBox().move(vec32.subtract(player.position()));
            return DamageUtils.getDamage((Entity)player, aABB, vec3, 5.0f, null, false);
        }
        return DamageUtils.getDamage((Entity)player, player.getBoundingBox(), vec3, 5.0f, null, false);
    }

    private boolean withinReach(BlockPos blockPos, double d) {
        return Vec3.atCenterOf((Vec3i)blockPos).distanceTo(BedAuraModule.mc.player.getEyePosition()) <= d + 0.5;
    }

    private void updateTargets() {
        this.targets.clear();
        Vec3 vec3 = BedAuraModule.mc.player.getEyePosition();
        ArrayList<Player> arrayList = new ArrayList<Player>();
        for (Object object : BedAuraModule.mc.level.entitiesForRendering()) {
            Player player2;
            if (!(object instanceof Player) || (player2 = (Player)object) == BedAuraModule.mc.player || !player2.isAlive() || player2.getHealth() <= 0.0f || this.ignoreFriends.getValue() && Night.FRIEND_MANAGER.contains(player2.getName().getString()) || Vec3.atCenterOf((Vec3i)player2.blockPosition()).distanceTo(vec3) > this.targetRange.getValue().doubleValue() && (double)player2.distanceTo((Entity)BedAuraModule.mc.player) > this.targetRange.getValue().doubleValue()) continue;
            arrayList.add(player2);
        }
        Comparator<Player> comparator = switch (this.targetMode.getValue()) {
            case "Furthest" -> Comparator.comparingDouble((Player player) -> player.distanceTo((Entity)BedAuraModule.mc.player)).reversed();
            case "Health" -> Comparator.comparingDouble(LivingEntity::getHealth);
            default -> Comparator.comparingDouble(player -> player.distanceTo((Entity)BedAuraModule.mc.player));
        };
        arrayList.sort(comparator);
        int n = this.maxTargets.getValue().intValue();
        for (Player player3 : arrayList) {
            if (this.targets.size() >= n) break;
            this.targets.add(player3);
        }
    }

    private BlockPos findOtherBedHalf(BlockPos blockPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!(BedAuraModule.mc.level.getBlockState(blockPos2).getBlock() instanceof BedBlock)) continue;
            return blockPos2;
        }
        return null;
    }

    private int findBedSlot() {
        boolean bl = this.swapMode.getValue().equalsIgnoreCase("Alt");
        Predicate<ItemStack> predicate = itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof BedItem;
        if (bl) {
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
        int n;
        boolean bl = this.swapMode.getValue().equalsIgnoreCase("Alt");
        Predicate<ItemStack> predicate = itemStack -> !itemStack.isEmpty() && !(itemStack.getItem() instanceof BedItem) && !(itemStack.getItem() instanceof BlockItem);
        if (bl) {
            for (n = 0; n < 36; ++n) {
                if (!predicate.test(BedAuraModule.mc.player.getInventory().getItem(n))) continue;
                return n;
            }
        } else {
            for (n = 0; n < 9; ++n) {
                if (!predicate.test(BedAuraModule.mc.player.getInventory().getItem(n))) continue;
                return n;
            }
        }
        for (n = 0; n < 9; ++n) {
            if (!BedAuraModule.mc.player.getInventory().getItem(n).isEmpty()) continue;
            return n;
        }
        for (n = 0; n < 9; ++n) {
            if (BedAuraModule.mc.player.getInventory().getItem(n).getItem() instanceof BedItem) continue;
            return n;
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
        BlockPos blockPos;
        BlockPos blockPos2;
        if (!this.renderPreview.getValue() || BedAuraModule.mc.level == null || BedAuraModule.mc.player == null) {
            return;
        }
        if (this.bedPlacedThisCycle && this.placedBedPos != null) {
            blockPos2 = this.placedBedPos;
            blockPos = this.placedBedHeadPos != null ? this.placedBedHeadPos : this.findOtherBedHalf(blockPos2);
            BlockPos blockPos3 = blockPos;
            if (blockPos != null) {
                this.placedBedHeadPos = blockPos;
            }
        } else if (this.currentPlacement != null) {
            blockPos2 = this.currentPlacement.pos();
            blockPos = blockPos2.relative(this.currentPlacement.dir());
        } else if (this.currentFoot != null) {
            blockPos2 = this.currentFoot;
            blockPos = this.currentHead != null ? this.currentHead : (this.currentDir != null ? this.currentFoot.relative(this.currentDir) : this.findOtherBedHalf(blockPos2));
        } else {
            this.lastRenderedPos = null;
            return;
        }
        if (!blockPos2.equals((Object)this.lastRenderedPos)) {
            this.lastRenderedPos = blockPos2;
            this.placementRenderMs = System.currentTimeMillis();
        }
        float f = 1.0f;
        if (this.interpolate.getValue()) {
            long l = System.currentTimeMillis() - this.placementRenderMs;
            float f2 = Math.min(1.0f, (float)l / 350.0f);
            f = 0.15f + 0.85f * f2;
        }
        double d = 0.5625;
        PoseStack poseStack = renderWorldEvent.getMatrices();
        double d2 = blockPos2.getX();
        double d3 = blockPos2.getY();
        double d4 = blockPos2.getZ();
        double d5 = (double)blockPos2.getX() + 1.0;
        double d6 = (double)blockPos2.getY() + 0.5625;
        double d7 = (double)blockPos2.getZ() + 1.0;
        if (blockPos != null) {
            d2 = Math.min(blockPos2.getX(), blockPos.getX());
            d3 = Math.min(blockPos2.getY(), blockPos.getY());
            d4 = Math.min(blockPos2.getZ(), blockPos.getZ());
            d5 = (double)Math.max(blockPos2.getX(), blockPos.getX()) + 1.0;
            d6 = (double)Math.max(blockPos2.getY(), blockPos.getY()) + 0.5625;
            d7 = (double)Math.max(blockPos2.getZ(), blockPos.getZ()) + 1.0;
        }
        double d8 = (d2 + d5) * 0.5;
        double d9 = (d3 + d6) * 0.5;
        double d10 = (d4 + d7) * 0.5;
        double d11 = (d5 - d2) * 0.5 * (double)f;
        double d12 = (d6 - d3) * 0.5 * (double)f;
        double d13 = (d7 - d4) * 0.5 * (double)f;
        AABB aABB = new AABB(d8 - d11, d9 - d12, d10 - d13, d8 + d11, d9 + d12, d10 + d13);
        Color color = this.fillColor.getColor();
        Color color2 = this.outlineColor.getColor();
        boolean bl = this.renderMode.getValue().equalsIgnoreCase("Fill") || this.renderMode.getValue().equalsIgnoreCase("Both");
        boolean bl2 = this.renderMode.getValue().equalsIgnoreCase("Outline") || this.renderMode.getValue().equalsIgnoreCase("Both");
        boolean bl3 = bl2;
        if (bl) {
            Renderer3D.renderBox(poseStack, aABB, color);
        }
        if (bl2) {
            Renderer3D.renderBoxOutline(poseStack, aABB, color2);
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
        boolean bl = BedAuraModule.mc.player.containerMenu instanceof CraftingMenu;
        if (!bl) {
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
            boolean bl2 = BedAuraModule.mc.level.getBlockState(this.placedTablePos).getBlock() == Blocks.CRAFTING_TABLE;
            boolean bl3 = bl2;
            if (bl2 && !this.tableOpenSent) {
                if (this.rotate.getValue()) {
                    float[] fArray = RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)this.placedTablePos));
                    Night.ROTATION_MANAGER.silentRotate(fArray[0], fArray[1]);
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
        int n = 0;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = BedAuraModule.mc.player.getInventory().getItem(i);
            if (!(itemStack.getItem() instanceof BedItem)) continue;
            n += itemStack.getCount();
        }
        return n;
    }

    private boolean isInventoryFull() {
        for (int i = 0; i < 36; ++i) {
            if (!BedAuraModule.mc.player.getInventory().getItem(i).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean gridHasMaterials(AbstractContainerMenu abstractContainerMenu) {
        for (int n : WOOL_GRID_SLOTS) {
            if (this.isWool(abstractContainerMenu.getSlot(n).getItem())) continue;
            return false;
        }
        for (int n : PLANK_GRID_SLOTS) {
            if (this.isPlanks(abstractContainerMenu.getSlot(n).getItem())) continue;
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
        int n;
        int n2 = this.findMenuSlot(abstractContainerMenu, this::isWool);
        int n3 = this.findMenuSlot(abstractContainerMenu, this::isPlanks);
        if (n2 == -1 || n3 == -1) {
            return false;
        }
        for (int n4 : WOOL_GRID_SLOTS) {
            if (this.isWool(abstractContainerMenu.getSlot(n4).getItem())) continue;
            n = this.findMenuSlot(abstractContainerMenu, this::isWool);
            if (n == -1) {
                return false;
            }
            this.placeOneItem(abstractContainerMenu, n, n4);
        }
        for (int n4 : PLANK_GRID_SLOTS) {
            if (this.isPlanks(abstractContainerMenu.getSlot(n4).getItem())) continue;
            n = this.findMenuSlot(abstractContainerMenu, this::isPlanks);
            if (n == -1) {
                return false;
            }
            this.placeOneItem(abstractContainerMenu, n, n4);
        }
        return true;
    }

    private void placeOneItem(AbstractContainerMenu abstractContainerMenu, int n, int n2) {
        BedAuraModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, n, 1, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
        BedAuraModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, n2, 1, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
        BedAuraModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, n, 0, ContainerInput.PICKUP, (Player)BedAuraModule.mc.player);
    }

    private int findMenuSlot(AbstractContainerMenu abstractContainerMenu, Predicate<ItemStack> predicate) {
        for (int i = 10; i < abstractContainerMenu.slots.size(); ++i) {
            if (!predicate.test(abstractContainerMenu.getSlot(i).getItem())) continue;
            return i;
        }
        return -1;
    }

    private BlockPos tryPlaceCraftingTable() {
        int n;
        int n2 = -1;
        for (int i = 0; i < 9; ++i) {
            if (BedAuraModule.mc.player.getInventory().getItem(i).getItem() != Blocks.CRAFTING_TABLE.asItem()) continue;
            n2 = i;
            break;
        }
        if (n2 == -1) {
            return null;
        }
        BlockPos blockPos = BedAuraModule.mc.player.blockPosition();
        BlockPos blockPos2 = null;
        double d = Double.MAX_VALUE;
        int n3 = (int)Math.ceil(this.range.getValue().doubleValue());
        for (int i = -n3; i <= n3; ++i) {
            for (int j = -n3; j <= n3; ++j) {
                for (n = -n3; n <= n3; ++n) {
                    BlockPos blockPos3 = blockPos.offset(i, j, n);
                    double d2 = BedAuraModule.mc.player.distanceToSqr((double)blockPos3.getX() + 0.5, (double)blockPos3.getY() + 0.5, (double)blockPos3.getZ() + 0.5);
                    if (d2 > this.range.getValue().doubleValue() * this.range.getValue().doubleValue() || !BedAuraModule.mc.level.getBlockState(blockPos3).canBeReplaced() || BedAuraModule.mc.level.getBlockState(blockPos3.below()).canBeReplaced() || new AABB(blockPos3).intersects(BedAuraModule.mc.player.getBoundingBox()) || !(d2 < d)) continue;
                    d = d2;
                    blockPos2 = blockPos3;
                }
            }
        }
        if (blockPos2 == null) {
            return null;
        }
        BlockPos blockPos4 = blockPos2.below();
        BlockHitResult blockHitResult = new BlockHitResult(new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5), Direction.UP, blockPos4, false);
        n = BedAuraModule.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot("Normal", n2, n);
        BedAuraModule.mc.gameMode.useItemOn(BedAuraModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult);
        InventoryUtils.switchBack("Normal", n2, n);
        return blockPos2;
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

