/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.Vec3dAccessor
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.mixins.accessors.Vec3dAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IHoleSnapModule;
import lightning.modules.impl.combat.SurroundModule;
import lightning.modules.impl.movement.SpeedModule;
import lightning.modules.impl.movement.StepModule;
import lightning.modules.impl.movement.TickShiftModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.MovementUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="HoleSnap", description="Pulls you toward your nearest hole.", category=Module.Category.MOVEMENT)
public class HoleSnapModule
extends Module
implements IHoleSnapModule {
    public NumberSetting range = new NumberSetting("Range", "Range for the holes.", 5, 1, 8);
    public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to snap you to double holes.", true);
    public BooleanSetting quadHoles = new BooleanSetting("QuadHoles", "Whether or not to snap you to quad holes.", true);
    public BooleanSetting step = new BooleanSetting("Step", "Keeps snapping to the next nearest hole instead of stopping once you've reached one.", false);
    public BooleanSetting fillHole = new BooleanSetting("FillHole", "Fills every cell of the hole you just stepped out of, once you've fully left its footprint (needs Step).", new BooleanSetting.Visibility(this.step, true), false);
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    public NumberSetting timer = new NumberSetting("Timer", "The tick-speed multiplier to run at while HoleSnap is active (1.0 = normal speed).", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(10.0f));
    public BooleanSetting renderTarget = new BooleanSetting("RenderTarget", "Draws an outline around the hole HoleSnap is currently walking toward.", true);
    public ColorSetting renderTargetColor = new ColorSetting("RenderTargetColor", "The color of the target hole outline.", new BooleanSetting.Visibility(this.renderTarget, true), ColorUtils.getDefaultOutlineColor());
    public AABB hole = null;
    private HoleUtils.Hole startingHole = null;
    private AABB lastUsedHole = null;
    private boolean jumpPressed = false;
    private int jumpAttempts = 0;
    private AABB unreachableHole = null;
    private int giveUpCount = 0;
    private static final int MAX_CONSECUTIVE_GIVE_UPS = 3;
    private int jumpWaitTicks = 0;
    private final Deque<BlockPos> fillQueue = new ArrayDeque<BlockPos>();
    private AABB pendingFillHole = null;
    private int fillWaitTicks = 0;
    private static final int MAX_FILL_WAIT_TICKS = 20;
    private final List<BlockPos> filledPositions = new ArrayList<BlockPos>();

    @Override
    public boolean isStepEnabled() {
        return this.step.getValue();
    }

    public static boolean isInHole(BlockPos blockPos) {
        return HoleUtils.getSingleHole(blockPos, 1.0, false) != null || HoleUtils.getDoubleHole(blockPos, 1.0) != null || HoleUtils.getQuadHole(blockPos, 1.0) != null;
    }

    @Override
    public void onEnable() {
        SurroundModule surroundModule;
        this.hole = null;
        this.startingHole = null;
        this.fillQueue.clear();
        this.pendingFillHole = null;
        this.filledPositions.clear();
        this.fillWaitTicks = 0;
        this.jumpAttempts = 0;
        this.jumpWaitTicks = 0;
        this.unreachableHole = null;
        this.giveUpCount = 0;
        Night.WORLD_MANAGER.setTimerMultiplier(this.timer.getValue().floatValue());
        if (this.step.getValue() && (surroundModule = Night.MODULE_MANAGER.getModule(SurroundModule.class)).isToggled()) {
            surroundModule.setToggled(false);
        }
    }

    @Override
    public void onDisable() {
        Night.WORLD_MANAGER.setTimerMultiplier(1.0f);
        if (this.jumpPressed) {
            HoleSnapModule.mc.options.keyJump.setDown(false);
            this.jumpPressed = false;
        }
        this.jumpWaitTicks = 0;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (!this.renderTarget.getValue() || this.hole == null) {
            return;
        }
        Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), this.hole, this.renderTargetColor.getColor());
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Night.WORLD_MANAGER.setTimerMultiplier(this.timer.getValue().floatValue());
        if (this.getNull()) {
            return;
        }
        if (this.pendingFillHole != null) {
            if (this.pendingFillHole.contains(HoleSnapModule.mc.player.getX(), this.pendingFillHole.minY + 0.5, HoleSnapModule.mc.player.getZ()) && this.fillWaitTicks++ < 20) {
                return;
            }
            if (!this.fillQueue.isEmpty()) {
                this.fillNext();
                return;
            }
            this.pendingFillHole = null;
            this.filledPositions.clear();
            this.setToggled(false);
            return;
        }
        if (HoleSnapModule.mc.player.fallDistance >= 5.0 && this.hole == null) {
            return;
        }
        if (this.hole == null && !this.pickTarget()) {
            return;
        }
        if (this.hole != null && !this.isHoleReachable(this.hole)) {
            this.unreachableHole = this.hole;
            this.hole = null;
            this.startingHole = null;
            this.jumpAttempts = 0;
            if (++this.giveUpCount >= 3) {
                Night.CHAT_MANAGER.tagged("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling.", this.getName());
                this.setToggled(false);
                return;
            }
            if (!this.pickTarget()) {
                return;
            }
        }
        if (this.hole != null && !this.isHoleStillValid(this.hole)) {
            this.unreachableHole = this.hole;
            this.hole = null;
            this.startingHole = null;
            this.jumpAttempts = 0;
            if (!this.pickTarget()) {
                return;
            }
        }
        boolean arrived = Math.abs(HoleSnapModule.mc.player.getX() - this.hole.getCenter().x) < 0.03 && Math.abs(HoleSnapModule.mc.player.getY() - this.hole.minY) < 0.05 && Math.abs(HoleSnapModule.mc.player.getZ() - this.hole.getCenter().z) < 0.03;
        if (!arrived) {
            if (HoleSnapModule.mc.player.horizontalCollision && HoleSnapModule.mc.player.onGround() && !this.jumpPressed) {
                double deltaX = this.hole.getCenter().x - HoleSnapModule.mc.player.getX();
                double deltaZ = this.hole.getCenter().z - HoleSnapModule.mc.player.getZ();
                Vec3 direction = deltaX == 0.0 && deltaZ == 0.0 ? Vec3.ZERO : new Vec3(deltaX, 0.0, deltaZ).normalize();
                AABB playerBox = HoleSnapModule.mc.player.getBoundingBox();
                AABB headroomBox = new AABB(playerBox.minX, HoleSnapModule.mc.player.getY() + 2.0, playerBox.minZ, playerBox.maxX, HoleSnapModule.mc.player.getY() + 2.5, playerBox.maxZ).move(direction.x * 0.5, 0.0, direction.z * 0.5);
                if (!HoleSnapModule.mc.level.noCollision(headroomBox)) {
                    this.unreachableHole = this.hole;
                    this.hole = null;
                    this.startingHole = null;
                    this.jumpAttempts = 0;
                    if (++this.giveUpCount >= 3) {
                        Night.CHAT_MANAGER.tagged("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling.", this.getName());
                        this.setToggled(false);
                        return;
                    }
                    this.pickTarget();
                    return;
                }
                HoleSnapModule.mc.options.keyJump.setDown(true);
                this.jumpPressed = true;
                this.jumpWaitTicks = 0;
                ++this.jumpAttempts;
                if (this.jumpAttempts > 5) {
                    HoleSnapModule.mc.options.keyJump.setDown(false);
                    this.jumpPressed = false;
                    this.jumpAttempts = 0;
                    this.unreachableHole = this.hole;
                    this.hole = null;
                    this.startingHole = null;
                    if (++this.giveUpCount >= 3) {
                        Night.CHAT_MANAGER.tagged("Gave up reaching a hole " + this.giveUpCount + " times in a row, disabling.", this.getName());
                        this.setToggled(false);
                    }
                    return;
                }
            }
            if (this.jumpPressed) {
                ++this.jumpWaitTicks;
                if (!HoleSnapModule.mc.player.onGround() || this.jumpWaitTicks > 10) {
                    HoleSnapModule.mc.options.keyJump.setDown(false);
                    this.jumpPressed = false;
                    this.jumpWaitTicks = 0;
                }
            }
            MovementUtils.moveTowards(playerMoveEvent, this.hole.getCenter(), MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
            return;
        }
        this.jumpAttempts = 0;
        this.giveUpCount = 0;
        playerMoveEvent.setMovement(new Vec3(0.0, playerMoveEvent.getMovement().y, 0.0));
        ((Vec3dAccessor)HoleSnapModule.mc.player.getDeltaMovement()).setX(0.0);
        ((Vec3dAccessor)HoleSnapModule.mc.player.getDeltaMovement()).setZ(0.0);
        playerMoveEvent.setCancelled(true);
        if (Night.MODULE_MANAGER.getModule(StepModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(StepModule.class).setToggled(false);
        }
        if (Night.MODULE_MANAGER.getModule(SpeedModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(SpeedModule.class).setToggled(false);
        }
        if (Night.MODULE_MANAGER.getModule(TickShiftModule.class).isToggled()) {
            Night.MODULE_MANAGER.getModule(TickShiftModule.class).setToggled(false);
        }
        if (this.fillHole.getValue() && this.step.getValue() && this.startingHole != null) {
            this.pendingFillHole = this.startingHole.box();
            this.fillWaitTicks = 0;
            this.queueFillPositions(this.startingHole.box());
            this.hole = null;
            this.startingHole = null;
            return;
        }
        this.setToggled(false);
        this.hole = null;
        this.startingHole = null;
    }

    private boolean isHoleReachable(AABB holeBox) {
        BlockPos headPos;
        int holeY;
        BlockPos holePos = BlockPos.containing((double)(holeBox.minX + 0.5), (double)holeBox.minY, (double)(holeBox.minZ + 0.5));
        int playerY = (int)Math.floor(HoleSnapModule.mc.player.getY());
        if (playerY > (holeY = holePos.getY())) {
            for (int i = holeY + 3; i <= playerY + 1; ++i) {
                if (HoleSnapModule.mc.level.getBlockState(new BlockPos(holePos.getX(), i, holePos.getZ())).canBeReplaced()) continue;
                return false;
            }
            return true;
        }
        return holeY - playerY < 2 || HoleSnapModule.mc.level.getBlockState(headPos = HoleSnapModule.mc.player.blockPosition().above(2)).canBeReplaced() && HoleSnapModule.mc.level.getBlockState(headPos.above()).canBeReplaced();
    }

    private boolean pickTarget() {
        HoleUtils.Hole current = this.currentHole();
        HoleUtils.Hole validCurrent = current != null && this.lastUsedHole != null && current.box().equals((Object)this.lastUsedHole) ? null : current;
        List<HoleUtils.Hole> holes;
        if (validCurrent != null && !this.step.getValue()) {
            holes = List.of(validCurrent);
        } else if (validCurrent != null) {
            holes = this.prioritizeSingle(this.getHoles().stream().filter(h -> !h.box().equals((Object)validCurrent.box())).toList());
        } else {
            holes = this.getHoles();
        }
        if (this.lastUsedHole != null) {
            List<HoleUtils.Hole> filtered = holes.stream().filter(hole -> !hole.box().equals((Object)this.lastUsedHole)).toList();
            if (!filtered.isEmpty()) {
                holes = filtered;
            }
        }
        if (this.unreachableHole != null) {
            holes = holes.stream().filter(hole -> !hole.box().equals((Object)this.unreachableHole)).toList();
        }
        if (holes.isEmpty()) {
            this.setToggled(false);
            return false;
        }
        this.startingHole = validCurrent;
        this.hole = holes.get(0).box();
        if (!this.hole.equals((Object)this.lastUsedHole)) {
            this.lastUsedHole = this.hole;
        }
        return true;
    }

    private boolean isHoleStillValid(AABB holeBox) {
        HoleUtils.Hole quadHole;
        HoleUtils.Hole doubleHole;
        BlockPos holePos = BlockPos.containing((double)(holeBox.minX + 0.5), (double)holeBox.minY, (double)(holeBox.minZ + 0.5));
        HoleUtils.Hole singleHole = HoleUtils.getSingleHole(holePos, 1.0);
        if (singleHole != null && singleHole.box().equals((Object)holeBox)) {
            return true;
        }
        if (this.doubleHoles.getValue() && (doubleHole = HoleUtils.getDoubleHole(holePos, 1.0)) != null && doubleHole.box().equals((Object)holeBox)) {
            return true;
        }
        return this.quadHoles.getValue() && (quadHole = HoleUtils.getQuadHole(holePos, 1.0)) != null && quadHole.box().equals((Object)holeBox);
    }

    private HoleUtils.Hole currentHole() {
        BlockPos playerPos = HoleSnapModule.mc.player.blockPosition();
        double centerX = (double)playerPos.getX() + 0.5;
        double centerY = (double)playerPos.getY() + 0.5;
        double centerZ = (double)playerPos.getZ() + 0.5;
        for (int i = -1; i <= 0; ++i) {
            for (int j = -1; j <= 0; ++j) {
                HoleUtils.Hole quadHole;
                HoleUtils.Hole doubleHole;
                BlockPos checkPos = playerPos.offset(i, 0, j);
                HoleUtils.Hole singleHole = HoleUtils.getSingleHole(checkPos, 1.0);
                if (singleHole != null && singleHole.box().contains(centerX, centerY, centerZ)) {
                    return singleHole;
                }
                if (this.doubleHoles.getValue() && (doubleHole = HoleUtils.getDoubleHole(checkPos, 1.0)) != null && doubleHole.box().contains(centerX, centerY, centerZ)) {
                    return doubleHole;
                }
                if (!this.quadHoles.getValue() || (quadHole = HoleUtils.getQuadHole(checkPos, 1.0)) == null || !quadHole.box().contains(centerX, centerY, centerZ)) continue;
                return quadHole;
            }
        }
        return null;
    }

    private List<HoleUtils.Hole> prioritizeSingle(List<HoleUtils.Hole> holes) {
        return holes.stream().sorted(Comparator.comparingInt(hole -> hole.type() == HoleUtils.HoleType.SINGLE ? 0 : 1)).toList();
    }

    private void queueFillPositions(AABB holeBox) {
        int minX = (int)Math.floor(holeBox.minX);
        int maxX = (int)Math.ceil(holeBox.maxX) - 1;
        int minZ = (int)Math.floor(holeBox.minZ);
        int maxZ = (int)Math.ceil(holeBox.maxZ) - 1;
        int y = (int)holeBox.minY;
        for (int i = minX; i <= maxX; ++i) {
            for (int j = minZ; j <= maxZ; ++j) {
                this.fillQueue.add(new BlockPos(i, y, j));
            }
        }
    }

    private void fillNext() {
        BlockPos blockPos = this.fillQueue.peek();
        if (blockPos == null) {
            return;
        }
        if (!WorldUtils.isPlaceable(blockPos)) {
            this.fillQueue.poll();
            return;
        }
        boolean altSwitch = this.autoSwitch.getValue().equalsIgnoreCase("AltSwap") || this.autoSwitch.getValue().equalsIgnoreCase("AltPickup");
        int blockSlot = InventoryUtils.findHardestBlock(0, altSwitch ? 35 : 8);
        if (blockSlot == -1) {
            this.fillQueue.clear();
            return;
        }
        int previousSlot = HoleSnapModule.mc.player.getInventory().getSelectedSlot();
        Direction direction = WorldUtils.getDirection(blockPos, this.filledPositions, this.strictDirection.getValue());
        if (direction == null) {
            BlockPos supportPos = blockPos.offset(0, -1, 0);
            if (!WorldUtils.isPlaceable(supportPos)) {
                this.fillQueue.poll();
                return;
            }
            Direction supportDirection = WorldUtils.getDirection(supportPos, this.filledPositions, this.strictDirection.getValue());
            if (supportDirection == null) {
                this.fillQueue.poll();
                return;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), blockSlot, previousSlot);
            boolean placedSupport = WorldUtils.placeBlock(supportPos, supportDirection, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
            InventoryUtils.switchBack(this.autoSwitch.getValue(), blockSlot, previousSlot);
            if (!placedSupport) {
                return;
            }
            this.filledPositions.add(supportPos);
            direction = WorldUtils.getDirection(blockPos, this.filledPositions, this.strictDirection.getValue());
            if (direction == null) {
                return;
            }
        }
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), blockSlot, previousSlot);
        boolean placed = WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        InventoryUtils.switchBack(this.autoSwitch.getValue(), blockSlot, previousSlot);
        if (placed) {
            this.filledPositions.add(blockPos);
            this.fillQueue.poll();
        }
    }

    private List<HoleUtils.Hole> getHoles() {
        ArrayList<HoleUtils.Hole> holes = new ArrayList<HoleUtils.Hole>();
        for (int i = 0; i < Night.WORLD_MANAGER.getRadius(this.range.getValue().doubleValue()); ++i) {
            HoleUtils.Hole quadHole;
            HoleUtils.Hole doubleHole;
            BlockPos blockPos = HoleSnapModule.mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
            if ((double)blockPos.getY() > HoleSnapModule.mc.player.getY()) continue;
            HoleUtils.Hole singleHole = HoleUtils.getSingleHole(blockPos, 1.0);
            if (singleHole != null) {
                holes.add(singleHole);
                continue;
            }
            if (this.doubleHoles.getValue() && (doubleHole = HoleUtils.getDoubleHole(blockPos, 1.0)) != null) {
                holes.add(doubleHole);
                continue;
            }
            if (!this.quadHoles.getValue() || (quadHole = HoleUtils.getQuadHole(blockPos, 1.0)) == null) continue;
            holes.add(quadHole);
        }
        return holes.stream().sorted(Comparator.comparing(hole -> HoleSnapModule.mc.player.distanceToSqr(hole.box().getCenter().x, hole.box().getCenter().y, hole.box().getCenter().z))).toList();
    }
}

