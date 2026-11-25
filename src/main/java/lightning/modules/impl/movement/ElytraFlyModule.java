/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.EntityAccessor
 *  lightning.mixins.accessors.EntityFlagAccessor
 *  lightning.mixins.accessors.PlayerMoveC2SPacketAccessor
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientCommonPacketListener
 *  net.minecraft.network.protocol.common.ClientboundPingPacket
 *  net.minecraft.network.protocol.common.ServerboundPongPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.FireworkRocketEntity
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.Fireworks
 *  net.minecraft.world.item.equipment.Equippable
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import java.util.ArrayDeque;
import java.util.Deque;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EventInput;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerTravelEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.mixins.accessors.EntityAccessor;
import lightning.mixins.accessors.EntityFlagAccessor;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.modules.impl.movement.ElytraBounceDebugModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.BaritoneUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="ElytraFly", description="Allows you to fly using an elytra without fireworks.", category=Module.Category.MOVEMENT)
public class ElytraFlyModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The mode that will be used for elytra flying.", "ControlRocket", new String[]{"Control", "ControlRocket", "Bounce"});
    public NumberSetting horizontal = new NumberSetting("Horizontal", "The speed at which you will be flying horizontally.", new ModeSetting.Visibility(this.mode, "Control"), (Number)Float.valueOf(2.0f), (Number)Float.valueOf(0.1f), (Number)Float.valueOf(10.0f));
    public NumberSetting vertical = new NumberSetting("Vertical", "The speed at which you will be flying vertically.", new ModeSetting.Visibility(this.mode, "Control"), (Number)Float.valueOf(1.0f), (Number)Float.valueOf(0.1f), (Number)Float.valueOf(10.0f));
    public BooleanSetting moveVertically = new BooleanSetting("MoveVertically", "Whether or not to allow for vertical movement.", new ModeSetting.Visibility(this.mode, "Control"), true);
    public BooleanSetting bounceTakeoff = new BooleanSetting("Takeoff", "Automatically jumps and initiates gliding when grounded with an elytra equipped.", new ModeSetting.Visibility(this.mode, "Bounce"), true);
    public BooleanSetting bounceAutoPitch = new BooleanSetting("AutoPitch", "Automatically pitches the player's rotation down while gliding to bounce at faster speeds.", new ModeSetting.Visibility(this.mode, "Bounce"), true);
    public NumberSetting bouncePitch = new NumberSetting("Pitch", "The pitch angle to look down when auto pitch is active.", new ModeSetting.Visibility(this.mode, "Bounce"), (Number)Float.valueOf(72.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(90.0f));
    public BooleanSetting bounceMinimizePackets = new BooleanSetting("MinimizePackets", "Send the bare minimum to start flying, nothing extra.", new ModeSetting.Visibility(this.mode, "Bounce"), true);
    public NumberSetting bounceFlagPause = new NumberSetting("FlagPause", "How long (in ticks) to pause if the server flags you for a movement check.", new ModeSetting.Visibility(this.mode, "Bounce"), (Number)Float.valueOf(5.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(100.0f));
    public BooleanSetting bounceFakeLag = new BooleanSetting("FakeLag", "Emulates the player lagging to allow flying in 1x2 tunnels.", new ModeSetting.Visibility(this.mode, "Bounce"), true);
    public BooleanSetting bouncePassObstacles = new BooleanSetting("PassObstacles", "Automatically paths around obstacles in the flight line using Baritone.", new ModeSetting.Visibility(this.mode, "Bounce"), false);
    public NumberSetting bounceObstacleLookAhead = new NumberSetting("ObstacleLookAhead", "Blocks to look ahead for obstacles / step by when pathing around one.", new ModeSetting.Visibility(this.mode, "Bounce"), (Number)Float.valueOf(8.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(50.0f));
    public BooleanSetting bounceStandUpToPass = new BooleanSetting("StandUpToPass", "Stops gliding (stands up) while Baritone navigates around an obstacle, then automatically takes off again once clear.", new ModeSetting.Visibility(this.mode, "Bounce"), true);
    public BooleanSetting bouncePutOnElytra = new BooleanSetting("PutOnElytra", "Safety net: if the elytra ends up off the chest slot, immediately equip it back.", new ModeSetting.Visibility(this.mode, "Bounce"), true);
    public BooleanSetting bounceBypass = new BooleanSetting("Bypass", "Bypass for 2b and other servers (may be)", new ModeSetting.Visibility(this.mode, "Bounce"), false);
    public NumberSetting bounceBypassTicks = new NumberSetting("BypassTicks", "How many ticks before/after the recast to blink.", new BooleanSetting.Visibility(this.bounceBypass, true), (Number)Float.valueOf(3.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(20.0f));
    public ModeSetting crSteerMode = new ModeSetting("Control", "Steering mode: Key (WASD flight + FreeLook camera) or Mouse (fly where camera looks).", new ModeSetting.Visibility(this.mode, "ControlRocket"), "Key", new String[]{"Key", "Mouse"});
    public NumberSetting crTurnSpeed = new NumberSetting("TurnSpeed", "Maximum rotation change per tick.", new ModeSetting.Visibility(this.mode, "ControlRocket"), (Number)Float.valueOf(25.0f), (Number)Float.valueOf(1.0f), (Number)Float.valueOf(180.0f));
    public NumberSetting crFireworkDelay = new NumberSetting("FireworksDelay", "Delay between every fire", new ModeSetting.Visibility(this.mode, "ControlRocket"), (Number)0, (Number)0, (Number)3);
    public ModeSetting crFlipFlopMode = new ModeSetting("FlipFlopMode", "How to hold position when not pressing movement keys.", new ModeSetting.Visibility(this.mode, "ControlRocket"), "Full", new String[]{"Full", "WithFirework", "None"});
    public BooleanSetting crInventory = new BooleanSetting("Inventory", "Allows using fireworks from anywhere in the inventory, not just the hotbar.", new ModeSetting.Visibility(this.mode, "ControlRocket"), true);
    public BooleanSetting crGrimSwap = new BooleanSetting("GrimV3", "Cycles the elytra out of the chest slot so it never loses durability.", new ModeSetting.Visibility(this.mode, "ControlRocket"), true);
    public NumberSetting crGrimDelay = new NumberSetting("GrimDelay", "Gliding ticks between each GrimV3 swap-out.", new ModeSetting.Visibility(this.mode, "ControlRocket"), (Number)Float.valueOf(10.0f), (Number)Float.valueOf(1.0f), (Number)Float.valueOf(18.0f));
    public BooleanSetting muteElytra = new BooleanSetting("MuteElytra", "Mutes the armor-equip sound when ControlRocket swaps between elytra and chestplate.", new ModeSetting.Visibility(this.mode, "ControlRocket"), true);
    private boolean freeLookInitialized;
    private float freeYaw;
    private float freePitch;
    private boolean grimStill;
    private int grimStillTicks;
    private float grimFlipBaseYaw;
    private boolean grimFlipToggle;
    private long grimFireworkUsedAtMs;
    private double grimLastFireworkDurationSeconds = -1.0;
    private static final long CR_DOUBLE_JUMP_WINDOW_MS = 300L;
    private boolean crLastJumpDown;
    private long crLastJumpPressTime;
    private boolean crLaunchPending;
    private boolean crGliding;
    private int crGrimTicks;
    private int crTicksSinceGlide = 99;
    private static final int GLIDE_MIN_GAP_TICKS = 2;
    private int lastGlideStartTick = -1000;
    private final Timer crLastSwapTimer = new Timer();
    private float pitch;
    private boolean bounceDoJump = false;
    private boolean bounceJumpThisTick = false;
    private boolean bouncePendingResend = false;
    private boolean bounceTakeoffPending = false;
    private int bounceFlagPauseTicksLeft = 0;
    private boolean bouncePrevGliding = false;
    private double bounceStartY = 0.0;
    private double bounceLastGroundY = 0.0;
    private final Deque<Packet<?>> bounceSendPacketQueue = new ArrayDeque();
    private final Deque<ClientboundPingPacket> bouncePingPacketQueue = new ArrayDeque<ClientboundPingPacket>();
    private boolean bounceWasFlyingForLog = false;
    private boolean bouncePrevJumpKeyDown = false;
    public static volatile boolean bouncePitchOverrideActive = false;
    public static volatile float bounceSavedCameraPitch = 0.0f;
    private boolean isFlushingBouncePackets = false;
    private volatile int bounceBypassTicksLeft = 0;
    private Vec3 bounceSpeedPrevPos = Vec3.ZERO;
    private Vec3 bounceSpeedCurrPos = Vec3.ZERO;
    private double bouncePrevSpeedSample = -1.0;
    private Vec3 bounceObstacleStartPos = Vec3.ZERO;
    private Vec3 bounceObstaclePassingToPos = null;
    private static final double OBSTACLE_MIN_HEIGHT = 0.063;
    private static final boolean OBSTACLE_HEAD_HITTERS = true;
    private static final double OBSTACLE_ACCEPTABLE_OFFSET = 2.0;
    private static final double OBSTACLE_DIRECTION_STEP = 45.0;
    private int grimParkedSlot = -1;
    private ItemStack grimParkedDisplaced = ItemStack.EMPTY;
    private ItemStack grimHiddenElytra = null;

    public float getFreeYaw() {
        return this.freeYaw;
    }

    public float getFreePitch() {
        return this.freePitch;
    }

    public boolean isFreeLookActive() {
        if (ElytraFlyModule.mc.player == null || !this.freeLookInitialized) {
            return false;
        }
        if (!this.isToggled() || !this.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            return false;
        }
        return this.crSteerMode.getValue().equalsIgnoreCase("Key") && (ElytraFlyModule.mc.player.isFallFlying() || this.shouldPinFallFlying());
    }

    public boolean isFreeLookInitialized() {
        return this.freeLookInitialized;
    }

    public void resetFreeLook() {
        this.freeLookInitialized = false;
    }

    public void onMouseTurn(double deltaX, double deltaY) {
        if (!this.freeLookInitialized && ElytraFlyModule.mc.player != null) {
            this.freeYaw = ElytraFlyModule.mc.player.getYRot();
            this.freePitch = ElytraFlyModule.mc.player.getXRot();
            this.freeLookInitialized = true;
        }
        this.freeYaw += (float)(deltaX * 0.15);
        this.freePitch = Mth.clamp((float)(this.freePitch + (float)(deltaY * 0.15)), (float)-90.0f, (float)90.0f);
    }

    public boolean isBounceActive() {
        return this.isToggled() && this.mode.getValue().equalsIgnoreCase("Bounce");
    }

    public boolean isBounceAutoPitch() {
        return this.isBounceActive() && this.bounceAutoPitch.getValue() && this.isGliding() && !this.isFlightPaused();
    }

    public float getBouncePitch() {
        return this.bouncePitch.getValue().floatValue();
    }

    public boolean isBounceJumpActive() {
        return this.isToggled() && this.mode.getValue().equalsIgnoreCase("Bounce") && (this.bounceDoJump || this.bounceJumpThisTick);
    }

    public void resetBounceJump() {
        this.bounceDoJump = false;
        this.bounceJumpThisTick = false;
    }

    public boolean isBouncePitchOverrideActive() {
        return false;
    }

    public float getBounceSavedCameraPitch() {
        return 0.0f;
    }

    @Override
    public void onEnable() {
        if (ElytraFlyModule.mc.player == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            this.resetBounceState();
            this.bounceStartY = ElytraFlyModule.mc.player.getY();
            this.bounceLastGroundY = ElytraFlyModule.mc.player.getY();
            bounceSavedCameraPitch = ElytraFlyModule.mc.player.getXRot();
            this.bounceSpeedPrevPos = ElytraFlyModule.mc.player.position();
            this.bounceSpeedCurrPos = ElytraFlyModule.mc.player.position();
            this.bounceObstacleStartPos = ElytraFlyModule.mc.player.position();
            this.bounceObstaclePassingToPos = null;
            return;
        }
        this.resetGrimControl();
        if (!this.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            return;
        }
        if (this.crGrimSwap.getValue()) {
            if (ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                return;
            }
            int chestplateSlot = this.findChestplateSlot();
            if (chestplateSlot != -1) {
                InventoryUtils.swapEquipment(chestplateSlot, 6);
            }
            return;
        }
        if (ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            return;
        }
        int elytraSlot = InventoryUtils.find(Items.ELYTRA);
        if (elytraSlot == -1) {
            Night.CHAT_MANAGER.tagged("No elytra found in your inventory.", this.getName());
            this.setToggled(false);
            return;
        }
        InventoryUtils.swapEquipment(elytraSlot, 6);
    }

    @Override
    public void onDisable() {
        if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            this.resetBounceState();
            if (this.bounceObstaclePassingToPos != null) {
                this.cancelObstaclePath();
            }
            this.bounceObstaclePassingToPos = null;
            this.flushBouncePackets();
            bouncePitchOverrideActive = false;
            if (ElytraFlyModule.mc.player != null && ElytraFlyModule.mc.player.onGround()) {
                if (ElytraFlyModule.mc.player.getPose() == Pose.FALL_FLYING || ElytraFlyModule.mc.player.getPose() == Pose.SWIMMING) {
                    ElytraFlyModule.mc.player.setPose(Pose.STANDING);
                }
                ((EntityFlagAccessor)ElytraFlyModule.mc.player).invokeSetSharedFlag(EntityFlagAccessor.getFlagFallFlying(), false);
            }
            return;
        }
        if (ElytraFlyModule.mc.player != null && this.freeLookInitialized) {
            ElytraFlyModule.mc.player.setYRot(this.freeYaw);
            ElytraFlyModule.mc.player.setXRot(this.freePitch);
        }
        this.resetGrimControl();
        this.crGrimTicks = 0;
        this.crLaunchPending = false;
        this.crGliding = false;
        this.grimParkedSlot = -1;
        this.grimHiddenElytra = null;
        this.grimParkedDisplaced = ItemStack.EMPTY;
        if (ElytraFlyModule.mc.player != null && ElytraFlyModule.mc.player.onGround()) {
            if (ElytraFlyModule.mc.player.getPose() == Pose.FALL_FLYING || ElytraFlyModule.mc.player.getPose() == Pose.SWIMMING) {
                ElytraFlyModule.mc.player.setPose(Pose.STANDING);
            }
            ((EntityAccessor)ElytraFlyModule.mc.player).invokeSetSharedFlag(7, false);
        }
        if (ElytraFlyModule.mc.player == null) {
            return;
        }
        ElytraFlyModule.mc.player.getAbilities().flying = false;
        ElytraFlyModule.mc.player.getAbilities().setFlyingSpeed(0.05f);
    }

    @SubscribeEvent
    public void onInput(EventInput eventInput) {
        if (!this.isToggled() || !this.mode.getValue().equalsIgnoreCase("Bounce")) {
            return;
        }
        if (!this.isGliding() && !this.bounceJumpThisTick) {
            return;
        }
        this.bounceJumpThisTick = false;
        if (this.isFlightPaused()) {
            return;
        }
        eventInput.jumping = true;
    }

    private void resetGrimControl() {
        this.grimStill = false;
        this.grimStillTicks = 0;
        this.grimFireworkUsedAtMs = 0L;
        this.grimLastFireworkDurationSeconds = -1.0;
        this.freeLookInitialized = false;
    }

    @SubscribeEvent
    public void onPlayerTravel(PlayerTravelEvent playerTravelEvent) {
        if (ElytraFlyModule.mc.player == null || ElytraFlyModule.mc.level == null || !ElytraFlyModule.mc.player.isFallFlying()) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Control")) {
            playerTravelEvent.setCancelled(true);
            if (ElytraFlyModule.mc.player.input.getMoveVector().y == 0.0f && ElytraFlyModule.mc.player.input.getMoveVector().x == 0.0f) {
                ElytraFlyModule.mc.player.setDeltaMovement(new Vec3(0.0, ElytraFlyModule.mc.player.getDeltaMovement().y, 0.0));
            } else {
                this.pitch = 12.0f;
                double cosYaw = Math.cos(Math.toRadians(ElytraFlyModule.mc.player.getYRot() + 90.0f));
                double sinYaw = Math.sin(Math.toRadians(ElytraFlyModule.mc.player.getYRot() + 90.0f));
                ElytraFlyModule.mc.player.setDeltaMovement(new Vec3((double)ElytraFlyModule.mc.player.input.getMoveVector().y * this.horizontal.getValue().doubleValue() * cosYaw + (double)ElytraFlyModule.mc.player.input.getMoveVector().x * this.horizontal.getValue().doubleValue() * sinYaw, ElytraFlyModule.mc.player.getDeltaMovement().y, (double)ElytraFlyModule.mc.player.input.getMoveVector().y * this.horizontal.getValue().doubleValue() * sinYaw - (double)ElytraFlyModule.mc.player.input.getMoveVector().x * this.horizontal.getValue().doubleValue() * cosYaw));
            }
            ElytraFlyModule.mc.player.setDeltaMovement(new Vec3(ElytraFlyModule.mc.player.getDeltaMovement().x, 0.0, ElytraFlyModule.mc.player.getDeltaMovement().z));
            boolean freecamActive = ((IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"))).isToggled();
            if (this.moveVertically.getValue() && !freecamActive) {
                if (ElytraFlyModule.mc.options.keyJump.isDown()) {
                    ElytraFlyModule.mc.player.setDeltaMovement(new Vec3(ElytraFlyModule.mc.player.getDeltaMovement().x, this.vertical.getValue().doubleValue(), ElytraFlyModule.mc.player.getDeltaMovement().z));
                    this.pitch = -51.0f;
                } else if (ElytraFlyModule.mc.options.keyShift.isDown()) {
                    ElytraFlyModule.mc.player.setDeltaMovement(new Vec3(ElytraFlyModule.mc.player.getDeltaMovement().x, -this.vertical.getValue().doubleValue(), ElytraFlyModule.mc.player.getDeltaMovement().z));
                    this.pitch = 0.0f;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        ServerboundMovePlayerPacket serverboundMovePlayerPacket;
        ClientboundSoundPacket clientboundSoundPacket;
        Packet<?> packet;
        if (ElytraFlyModule.mc.player == null || ElytraFlyModule.mc.level == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            Packet<?> packet2 = packetReceiveEvent.getPacket();
            if (packet2 instanceof ClientboundPingPacket) {
                ClientboundPingPacket clientboundPingPacket = (ClientboundPingPacket)packet2;
                boolean shouldQueuePing = this.bounceFakeLag.getValue() && this.isGlidingMasked() && this.isInDipWindow() || this.bounceBypass.getValue() && this.bounceBypassTicksLeft > 0;
                if (shouldQueuePing) {
                    this.bouncePingPacketQueue.add(clientboundPingPacket);
                    packetReceiveEvent.setCancelled(true);
                }
                return;
            }
            if (packetReceiveEvent.getPacket() instanceof ClientboundPlayerPositionPacket) {
                this.bounceFlagPauseTicksLeft = this.bounceFlagPause.getValue().intValue();
                this.bounceDoJump = false;
                this.bouncePendingResend = false;
                this.bounceTakeoffPending = false;
                if (this.isBounceDebugOn()) {
                    this.debugLog("FLAG PAUSE: server corrected position, holding for " + this.bounceFlagPauseTicksLeft + " ticks");
                }
                if (this.bouncePassObstacles.getValue() && BaritoneUtils.isAvailable()) {
                    Vec3 obstacleDir = this.getSnappedObstacleDir();
                    Vec3 closestPoint = this.findClosestPointOnObstacleLine(ElytraFlyModule.mc.player.position(), obstacleDir);
                    mc.execute(() -> {
                        if (ElytraFlyModule.mc.player == null) {
                            return;
                        }
                        Vec3 targetPoint = closestPoint.add(obstacleDir.scale(this.bounceObstacleLookAhead.getValue().doubleValue()));
                        this.pathToValidObstaclePoint(targetPoint, obstacleDir, false);
                    });
                }
            }
        }
        boolean autoMaceActive = this.isAutoMaceFakeFlyActive();
        if ((this.mode.getValue().equalsIgnoreCase("ControlRocket") && this.muteElytra.getValue() || autoMaceActive) && !this.crLastSwapTimer.hasTimeElapsed(500) && (packet = packetReceiveEvent.getPacket()) instanceof ClientboundSoundPacket && ((SoundEvent)(clientboundSoundPacket = (ClientboundSoundPacket)packet).getSound().value()).location().getPath().startsWith("item.armor.equip")) {
            packetReceiveEvent.setCancelled(true);
        }
        if (this.mode.getValue().equalsIgnoreCase("Control") && (packet = packetReceiveEvent.getPacket()) instanceof ServerboundMovePlayerPacket && (serverboundMovePlayerPacket = (ServerboundMovePlayerPacket)packet).hasRotation() && ElytraFlyModule.mc.player.isFallFlying()) {
            if (ElytraFlyModule.mc.options.keyLeft.isDown()) {
                ((PlayerMoveC2SPacketAccessor)serverboundMovePlayerPacket).setYaw(serverboundMovePlayerPacket.getYRot(0.0f) - 90.0f);
            }
            if (ElytraFlyModule.mc.options.keyRight.isDown()) {
                ((PlayerMoveC2SPacketAccessor)serverboundMovePlayerPacket).setYaw(serverboundMovePlayerPacket.getYRot(0.0f) + 90.0f);
            }
            ((PlayerMoveC2SPacketAccessor)serverboundMovePlayerPacket).setPitch(this.pitch);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (ElytraFlyModule.mc.player == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Bounce")) {
            return;
        }
        if (this.isFlushingBouncePackets) {
            return;
        }
        if (packetSendEvent.getPacket() == null) {
            return;
        }
        if (!packetSendEvent.getPacket().getClass().getSimpleName().startsWith("Serverbound")) {
            return;
        }
        boolean fakeLagActive = this.bounceFakeLag.getValue() && this.isGlidingMasked() && this.isInDipWindow();
        boolean bypassActive = this.bounceBypass.getValue() && this.bounceBypassTicksLeft > 0 && (packetSendEvent.getPacket() instanceof ServerboundMovePlayerPacket || packetSendEvent.getPacket() instanceof ServerboundPongPacket);
        boolean shouldQueue = fakeLagActive || bypassActive;
        if (shouldQueue) {
            this.bounceSendPacketQueue.add(packetSendEvent.getPacket());
            packetSendEvent.setCancelled(true);
            return;
        }
        this.flushBouncePackets();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (ElytraFlyModule.mc.player == null || ElytraFlyModule.mc.level == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            if (this.bouncePutOnElytra.getValue()) {
                this.onPutOnElytraTick();
            }
            this.tickBounceMode();
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            return;
        }
        this.handleControlRocket();
    }

    private void handleControlRocket() {
        boolean shouldLaunch;
        if (!this.crGrimSwap.getValue() && ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            int elytraSlot = InventoryUtils.find(Items.ELYTRA);
            if (elytraSlot != -1) {
                InventoryUtils.swapEquipment(elytraSlot, 6);
                if (!ElytraFlyModule.mc.player.onGround()) {
                    mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)ElytraFlyModule.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                }
            }
            return;
        }
        boolean jumpDown = ElytraFlyModule.mc.options.keyJump.isDown();
        if (jumpDown && !this.crLastJumpDown) {
            shouldLaunch = this.crSteerMode.getValue().equalsIgnoreCase("Key") || System.currentTimeMillis() - this.crLastJumpPressTime <= 300L;
            if (shouldLaunch && !ElytraFlyModule.mc.player.isFallFlying()) {
                this.crLaunchPending = true;
                Vec3 velocity = ElytraFlyModule.mc.player.getDeltaMovement();
                ElytraFlyModule.mc.player.setDeltaMovement(velocity.x, Math.max(velocity.y, 0.45), velocity.z);
            }
            this.crLastJumpPressTime = System.currentTimeMillis();
        }
        this.crLastJumpDown = jumpDown;
        if (ElytraFlyModule.mc.player.onGround() && !this.crLaunchPending) {
            boolean wasGliding = this.crGliding;
            this.crGliding = false;
            this.crGrimTicks = 0;
            if (wasGliding && this.freeLookInitialized) {
                ElytraFlyModule.mc.player.setYRot(this.freeYaw);
                ElytraFlyModule.mc.player.setXRot(this.freePitch);
            }
            this.resetGrimControl();
            if (ElytraFlyModule.mc.player.getPose() == Pose.FALL_FLYING || ElytraFlyModule.mc.player.getPose() == Pose.SWIMMING) {
                ElytraFlyModule.mc.player.setPose(Pose.STANDING);
            }
            ((EntityAccessor)ElytraFlyModule.mc.player).invokeSetSharedFlag(7, false);
        }
        if (ElytraFlyModule.mc.player.isFallFlying()) {
            this.crLaunchPending = false;
        } else if (!ElytraFlyModule.mc.player.onGround() && (this.crLaunchPending || ElytraFlyModule.mc.player.getDeltaMovement().y < 0.0)) {
            this.sendGlideStart();
            this.crLaunchPending = false;
        }
        this.grimSwapCycle();
        if (!ElytraFlyModule.mc.player.isFallFlying()) {
            this.resetGrimControl();
            return;
        }
        this.tickGrimControl();
    }

    private void tickGrimControl() {
        if (this.crSteerMode.getValue().equalsIgnoreCase("Key")) {
            if (!this.freeLookInitialized) {
                this.freeYaw = ElytraFlyModule.mc.player.getYRot();
                this.freePitch = ElytraFlyModule.mc.player.getXRot();
                this.freeLookInitialized = true;
            }
            Vec3 moveDir = this.grimMovementInput();
            boolean wasStill = this.grimStill;
            this.grimStill = moveDir.lengthSqr() < 1.0E-4;
            if (this.grimStill && !wasStill) {
                this.grimFlipBaseYaw = ElytraFlyModule.mc.player.getYRot();
                this.grimFlipToggle = false;
            }
            boolean flipFlopActive = true;
            if (this.grimStill) {
                boolean hasFirework = this.hasFireworkInInventory();
                flipFlopActive = switch (this.crFlipFlopMode.getValue()) {
                    case "Full" -> true;
                    case "WithFirework" -> hasFirework;
                    case "None" -> false;
                    default -> true;
                };
            }
            this.grimStillTicks = this.grimStill ? this.grimStillTicks + 1 : 0;
            if (!this.grimStill || flipFlopActive) {
                this.tryUseGrimFirework();
            }
            if (this.grimStill) {
                if (flipFlopActive) {
                    float flipYaw = this.grimFlipToggle ? this.grimFlipBaseYaw + 180.0f : this.grimFlipBaseYaw;
                    this.applyGrimRotation(flipYaw, 0.0f);
                    this.grimFlipToggle = !this.grimFlipToggle;
                }
            } else {
                if (wasStill && !this.hasFireworkInInventory()) {
                    ElytraFlyModule.mc.player.setDeltaMovement(Vec3.ZERO);
                }
                Vec3 eyePos = ElytraFlyModule.mc.player.getEyePosition();
                float[] rotations = RotationUtils.getRotations(eyePos.x + moveDir.x, eyePos.y + moveDir.y, eyePos.z + moveDir.z);
                this.applyGrimRotation(rotations[0], rotations[1]);
            }
        } else {
            this.freeLookInitialized = false;
            float yaw = ElytraFlyModule.mc.player.getYRot();
            float pitch = ElytraFlyModule.mc.player.getXRot();
            this.applyGrimRotation(yaw, pitch);
            this.tryUseGrimFirework();
        }
    }

    private Vec3 grimMovementInput() {
        float yaw = this.freeLookInitialized ? this.freeYaw : ElytraFlyModule.mc.player.getYRot();
        Vec3 direction = Vec3.ZERO;
        if (ElytraFlyModule.mc.options.keyUp.isDown()) {
            direction = direction.add(ElytraFlyModule.fromYaw(yaw));
        }
        if (ElytraFlyModule.mc.options.keyDown.isDown()) {
            direction = direction.add(ElytraFlyModule.fromYaw(yaw + 180.0f));
        }
        if (ElytraFlyModule.mc.options.keyLeft.isDown()) {
            direction = direction.add(ElytraFlyModule.fromYaw(yaw - 90.0f));
        }
        if (ElytraFlyModule.mc.options.keyRight.isDown()) {
            direction = direction.add(ElytraFlyModule.fromYaw(yaw + 90.0f));
        }
        if (ElytraFlyModule.mc.options.keyJump.isDown()) {
            direction = direction.add(0.0, 1.0, 0.0);
        }
        if (ElytraFlyModule.mc.options.keyShift.isDown()) {
            direction = direction.add(0.0, -1.0, 0.0);
        }
        return direction;
    }

    private static Vec3 fromYaw(float yaw) {
        double yawRad = Math.toRadians(yaw);
        return new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
    }

    private void applyGrimRotation(float targetYaw, float targetPitch) {
        float turnSpeed = this.crTurnSpeed.getValue().floatValue();
        float currentYaw = ElytraFlyModule.mc.player.getYRot();
        float currentPitch = ElytraFlyModule.mc.player.getXRot();
        float deltaYaw = Mth.clamp((float)Mth.wrapDegrees((float)(targetYaw - currentYaw)), (float)(-turnSpeed), (float)turnSpeed);
        float deltaPitch = Mth.clamp((float)(targetPitch - currentPitch), (float)(-turnSpeed), (float)turnSpeed);
        ElytraFlyModule.mc.player.setYRot(currentYaw + deltaYaw);
        ElytraFlyModule.mc.player.setXRot(currentPitch + deltaPitch);
        ElytraFlyModule.mc.player.setYBodyRot(currentYaw + deltaYaw);
        ElytraFlyModule.mc.player.setYHeadRot(currentYaw + deltaYaw);
    }

    private boolean hasFireworkInInventory() {
        int slot;
        if (ElytraFlyModule.mc.player == null) {
            return false;
        }
        if (ElytraFlyModule.mc.player.getOffhandItem().is(Items.FIREWORK_ROCKET) || ElytraFlyModule.mc.player.getMainHandItem().is(Items.FIREWORK_ROCKET)) {
            return true;
        }
        for (slot = 0; slot <= 8; ++slot) {
            if (!ElytraFlyModule.mc.player.getInventory().getItem(slot).is(Items.FIREWORK_ROCKET)) continue;
            return true;
        }
        if (this.crInventory.getValue()) {
            for (slot = 9; slot <= 35; ++slot) {
                if (!ElytraFlyModule.mc.player.getInventory().getItem(slot).is(Items.FIREWORK_ROCKET)) continue;
                return true;
            }
        }
        return false;
    }

    private int findFireworkSlot() {
        int slot;
        if (ElytraFlyModule.mc.player == null) {
            return -1;
        }
        if (ElytraFlyModule.mc.player.getMainHandItem().is(Items.FIREWORK_ROCKET)) {
            return ElytraFlyModule.mc.player.getInventory().getSelectedSlot();
        }
        for (slot = 0; slot <= 8; ++slot) {
            if (!ElytraFlyModule.mc.player.getInventory().getItem(slot).is(Items.FIREWORK_ROCKET)) continue;
            return slot;
        }
        if (this.crInventory.getValue()) {
            for (slot = 9; slot <= 35; ++slot) {
                if (!ElytraFlyModule.mc.player.getInventory().getItem(slot).is(Items.FIREWORK_ROCKET)) continue;
                return slot;
            }
        }
        return -1;
    }

    public void resetFireworkCooldown() {
        this.grimFireworkUsedAtMs = 0L;
        this.grimLastFireworkDurationSeconds = -1.0;
    }

    public void tryUseGrimFirework() {
        if (ElytraFlyModule.mc.player == null) {
            return;
        }
        this.tryUseGrimFirework(ElytraFlyModule.mc.player.getYRot(), ElytraFlyModule.mc.player.getXRot());
    }

    public void tryUseGrimFirework(float yaw, float pitch) {
        if (ElytraFlyModule.mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (ElytraFlyModule.mc.player.isUsingItem() && ElytraFlyModule.mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            return;
        }
        double elapsedSeconds = (double)(System.currentTimeMillis() - this.grimFireworkUsedAtMs) / 1000.0;
        double requiredDelay = this.grimLastFireworkDurationSeconds < 0.0 ? 0.0 : this.grimLastFireworkDurationSeconds + (double)this.crFireworkDelay.getValue().floatValue();
        if (this.grimFireworkUsedAtMs != 0L && elapsedSeconds < requiredDelay && this.fireworkBoostActive()) {
            return;
        }
        if (ElytraFlyModule.mc.player.getOffhandItem().is(Items.FIREWORK_ROCKET)) {
            ItemStack offhandStack = ElytraFlyModule.mc.player.getOffhandItem();
            Fireworks fireworks = (Fireworks)offhandStack.get(DataComponents.FIREWORKS);
            int duration = fireworks != null ? fireworks.flightDuration() : 1;
            this.grimLastFireworkDurationSeconds = (double)duration * 0.5 + 0.5;
            if (!this.armServerGlide()) {
                return;
            }
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.OFF_HAND, sequence, yaw, pitch));
            this.grimFireworkUsedAtMs = System.currentTimeMillis();
            return;
        }
        int fireworkSlot = this.findFireworkSlot();
        if (fireworkSlot == -1) {
            return;
        }
        ItemStack fireworkStack = ElytraFlyModule.mc.player.getInventory().getItem(fireworkSlot);
        Fireworks fireworks = (Fireworks)fireworkStack.get(DataComponents.FIREWORKS);
        int duration = fireworks != null ? fireworks.flightDuration() : 1;
        this.grimLastFireworkDurationSeconds = (double)duration * 0.5 + 0.5;
        if (fireworkSlot > 8 && !this.crInventory.getValue()) {
            return;
        }
        if (!this.armServerGlide()) {
            return;
        }
        int previousSlot = ElytraFlyModule.mc.player.getInventory().getSelectedSlot();
        if (fireworkSlot >= 0 && fireworkSlot <= 8) {
            if (InventoryUtils.switchSlot("Silent", fireworkSlot, previousSlot)) {
                NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch));
                InventoryUtils.switchBack("Silent", fireworkSlot, previousSlot);
                this.grimFireworkUsedAtMs = System.currentTimeMillis();
            }
        } else if (this.crInventory.getValue()) {
            int selectedSlot = ElytraFlyModule.mc.player.getInventory().getSelectedSlot();
            InventoryUtils.swap("Swap", fireworkSlot, selectedSlot);
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch));
            InventoryUtils.swap("Swap", fireworkSlot, selectedSlot);
            this.grimFireworkUsedAtMs = System.currentTimeMillis();
        }
    }

    private boolean fireworkBoostActive() {
        if (ElytraFlyModule.mc.player == null || ElytraFlyModule.mc.level == null) {
            return false;
        }
        return !ElytraFlyModule.mc.level.getEntities((Entity)ElytraFlyModule.mc.player, ElytraFlyModule.mc.player.getBoundingBox().inflate(2.0), entity -> entity instanceof FireworkRocketEntity).isEmpty();
    }

    private boolean armServerGlide() {
        if (this.crGrimSwap.getValue() && ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            return this.sendGlideStart();
        }
        return true;
    }

    private boolean isAutoMaceFakeFlyActive() {
        AutoMaceModule autoMaceModule = Night.MODULE_MANAGER.getModule(AutoMaceModule.class);
        return autoMaceModule != null && autoMaceModule.isToggled() && autoMaceModule.fakeFly.getValue();
    }

    private void grimSwapCycle() {
        if (!this.crGrimSwap.getValue() || ElytraFlyModule.mc.player.onGround()) {
            return;
        }
        if (this.crGrimTicks++ < this.crGrimDelay.getValue().intValue()) {
            return;
        }
        this.crGrimTicks = 0;
        this.sendGlideStart();
    }

    public boolean sendGlideStart() {
        return this.sendGlideStart(false);
    }

    public boolean sendGlideStart(boolean force) {
        this.crGliding = true;
        if (ElytraFlyModule.mc.player == null) {
            return false;
        }
        if (ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)ElytraFlyModule.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            return true;
        }
        int currentTick = ElytraFlyModule.mc.player.tickCount;
        if (currentTick >= this.lastGlideStartTick && currentTick - this.lastGlideStartTick < 2) {
            return false;
        }
        int elytraSlot = InventoryUtils.find(Items.ELYTRA);
        if (elytraSlot == -1) {
            return false;
        }
        this.lastGlideStartTick = currentTick;
        ItemStack elytraCopy = ElytraFlyModule.mc.player.getInventory().getItem(elytraSlot).copy();
        ItemStack chestCopy = ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).copy();
        this.grimParkedSlot = elytraSlot;
        this.grimHiddenElytra = elytraCopy;
        this.grimParkedDisplaced = chestCopy;
        this.crLastSwapTimer.reset();
        InventoryUtils.swapEquipment(elytraSlot, 6);
        mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)ElytraFlyModule.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        InventoryUtils.swapEquipment(elytraSlot, 6);
        this.grimParkedSlot = -1;
        this.grimHiddenElytra = null;
        this.grimParkedDisplaced = ItemStack.EMPTY;
        return true;
    }

    public boolean shouldPinFallFlying() {
        return this.isToggled() && this.mode.getValue().equalsIgnoreCase("ControlRocket") && this.crGrimSwap.getValue() && this.crGliding && ElytraFlyModule.mc.player != null && !ElytraFlyModule.mc.player.onGround();
    }

    private int findChestplateSlot() {
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.INVENTORY_END; ++i) {
            Equippable equippable;
            ItemStack itemStack = ElytraFlyModule.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() == Items.ELYTRA || (equippable = (Equippable)itemStack.get(DataComponents.EQUIPPABLE)) == null || equippable.slot() != EquipmentSlot.CHEST) continue;
            return i;
        }
        return -1;
    }

    public int getGrimParkedSlot() {
        return this.grimParkedSlot;
    }

    public ItemStack getGrimParkedDisplaced() {
        return this.grimParkedDisplaced;
    }

    public ItemStack getGrimHiddenElytra() {
        return this.grimHiddenElytra;
    }

    @SubscribeEvent
    public void onUpdateMovementPost(UpdateMovementEvent.Post post) {
        if (ElytraFlyModule.mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (!this.mode.getValue().equalsIgnoreCase("Bounce")) {
            return;
        }
        this.bounceSpeedPrevPos = this.bounceSpeedCurrPos;
        this.bounceSpeedCurrPos = ElytraFlyModule.mc.player.position();
        if (this.isBounceDebugOn() && this.bouncePrevSpeedSample >= 0.0) {
            double currentSpeed = this.bounceHorizontalSpeedometer();
            double speedDrop = this.bouncePrevSpeedSample - currentSpeed;
            boolean horizontalCollision = ElytraFlyModule.mc.player.horizontalCollision;
            if (!horizontalCollision && this.bouncePrevSpeedSample > 5.0 && speedDrop > this.bouncePrevSpeedSample * 0.25) {
                this.debugLog(String.format("SPEED DROP: %.1f -> %.1f km/h (-%.0f%%) onGround=%b horizontalCollision=%b gliding=%b fallFlying=%b flagPauseTicksLeft=%d pos=%.2f,%.2f,%.2f", this.bouncePrevSpeedSample * 3.6, currentSpeed * 3.6, speedDrop / this.bouncePrevSpeedSample * 100.0, ElytraFlyModule.mc.player.onGround(), horizontalCollision, this.isGliding(), ElytraFlyModule.mc.player.isFallFlying(), this.bounceFlagPauseTicksLeft, ElytraFlyModule.mc.player.getX(), ElytraFlyModule.mc.player.getY(), ElytraFlyModule.mc.player.getZ()));
            }
            this.bouncePrevSpeedSample = currentSpeed;
        } else {
            this.bouncePrevSpeedSample = this.isBounceDebugOn() ? this.bounceHorizontalSpeedometer() : -1.0;
        }
    }

    private void tickBounceMode() {
        if (ElytraFlyModule.mc.player == null || ElytraFlyModule.mc.level == null || mc.getConnection() == null) {
            return;
        }
        if (!this.canGlideNow()) {
            this.resetBounceState();
            this.flushBouncePackets();
            return;
        }
        if (this.bounceBypass.getValue()) {
            if (this.bounceBypassTicksLeft > 0) {
                --this.bounceBypassTicksLeft;
            }
            if (this.isNearingBounceLanding()) {
                this.bounceBypassTicksLeft = Math.max(this.bounceBypassTicksLeft, this.bounceBypassTicks.getValue().intValue());
            }
        }
        if (this.handlePassingObstacles()) {
            this.bounceDoJump = false;
            return;
        }
        if (this.bounceFlagPauseTicksLeft > 0) {
            --this.bounceFlagPauseTicksLeft;
            return;
        }
        if (this.isFlightPaused()) {
            return;
        }
        if (!this.isGliding()) {
            if (this.bounceTakeoff.getValue() && this.canTakeoff()) {
                if (this.canStartGliding()) {
                    this.startFly();
                } else {
                    double yawRad = Math.toRadians(ElytraFlyModule.mc.player.getYRot());
                    double lookX = -Math.cos(yawRad);
                    double lookZ = -Math.sin(yawRad);
                    Vec3 velocity = ElytraFlyModule.mc.player.getDeltaMovement();
                    double forwardSpeed = Math.abs(velocity.x * lookX + velocity.z * lookZ);
                    if (forwardSpeed >= 0.001) {
                        return;
                    }
                    this.bounceJumpThisTick = true;
                }
            }
            return;
        }
        boolean fallFlyingFlag = ((EntityFlagAccessor)ElytraFlyModule.mc.player).invokeGetSharedFlag(EntityFlagAccessor.getFlagFallFlying());
        if (this.bounceMinimizePackets.getValue() && fallFlyingFlag) {
            return;
        }
        this.startFly();
    }

    public void startFly() {
        if (ElytraFlyModule.mc.player == null || mc.getConnection() == null) {
            return;
        }
        boolean rawFlying = ((EntityFlagAccessor)ElytraFlyModule.mc.player).invokeGetSharedFlag(EntityFlagAccessor.getFlagFallFlying());
        ((EntityFlagAccessor)ElytraFlyModule.mc.player).invokeSetSharedFlag(EntityFlagAccessor.getFlagFallFlying(), true);
        mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)ElytraFlyModule.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        if (this.bounceBypass.getValue()) {
            this.bounceBypassTicksLeft = this.bounceBypassTicks.getValue().intValue();
        }
        if (this.isBounceDebugOn()) {
            double speed = this.bounceHorizontalSpeedometer();
            this.debugLog(String.format("START FLY: %.2f m/s (%.1f km/h) onGround=%b rawFlying=%b flagPauseTicksLeft=%d", speed, speed * 3.6, ElytraFlyModule.mc.player.onGround(), rawFlying, this.bounceFlagPauseTicksLeft));
        }
    }

    private void onPutOnElytraTick() {
        int elytraSlot;
        ItemStack carried;
        if (!this.bouncePutOnElytra.getValue()) {
            return;
        }
        if (ElytraFlyModule.mc.gameMode == null || ElytraFlyModule.mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (ElytraFlyModule.mc.player.containerMenu == ElytraFlyModule.mc.player.inventoryMenu && (carried = ElytraFlyModule.mc.player.inventoryMenu.getCarried()).getItem() == Items.ELYTRA) {
            this.resolveCarriedElytra();
            return;
        }
        if (ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA && (elytraSlot = this.findElytraInInventory()) != -1) {
            this.swapIntoChestSlot(elytraSlot);
        }
    }

    private void resolveCarriedElytra() {
        int containerId = ElytraFlyModule.mc.player.inventoryMenu.containerId;
        ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, 6, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
        if (ElytraFlyModule.mc.player.inventoryMenu.getCarried().getItem() != Items.ELYTRA) {
            return;
        }
        int emptySlot = this.findEmptyInventorySlot();
        if (emptySlot != -1) {
            ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, emptySlot, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
            return;
        }
        int fallbackSlot = this.findAnyInventorySlot();
        if (fallbackSlot != -1) {
            ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, fallbackSlot, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
        }
    }

    private void swapIntoChestSlot(int slot) {
        int containerId = ElytraFlyModule.mc.player.inventoryMenu.containerId;
        ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, slot, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
        ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, 6, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
        ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, slot, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
    }

    private int findElytraInInventory() {
        for (int i = 9; i <= 44; ++i) {
            if (ElytraFlyModule.mc.player.inventoryMenu.getSlot(i).getItem().getItem() != Items.ELYTRA) continue;
            return i;
        }
        return -1;
    }

    private int findEmptyInventorySlot() {
        for (int i = 9; i <= 44; ++i) {
            if (!ElytraFlyModule.mc.player.inventoryMenu.getSlot(i).getItem().isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private int findAnyInventorySlot() {
        for (int i = 9; i <= 44; ++i) {
            if (ElytraFlyModule.mc.player.inventoryMenu.getSlot(i).getItem().isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public boolean isGliding() {
        if (ElytraFlyModule.mc.player == null) {
            return false;
        }
        boolean fallFlying = ((EntityFlagAccessor)ElytraFlyModule.mc.player).invokeGetSharedFlag(EntityFlagAccessor.getFlagFallFlying());
        if (this.bouncePrevGliding && !this.isFlightPaused()) {
            return true;
        }
        this.bouncePrevGliding = fallFlying;
        return fallFlying;
    }

    public boolean isGlidingMasked() {
        return this.isGliding();
    }

    public boolean isFlightPaused() {
        return this.bounceFlagPauseTicksLeft > 0 || BaritoneUtils.isActive();
    }

    public boolean canTakeoff() {
        if (ElytraFlyModule.mc.player == null) {
            return false;
        }
        return (ElytraFlyModule.mc.player.onGround() || !this.isGliding()) && !ElytraFlyModule.mc.player.getAbilities().flying && !ElytraFlyModule.mc.player.onClimbable() && !ElytraFlyModule.mc.player.isInWater() && !ElytraFlyModule.mc.player.isPassenger() && !ElytraFlyModule.mc.player.hasEffect(MobEffects.LEVITATION) && this.canGlideItem();
    }

    public boolean canStartGliding() {
        if (ElytraFlyModule.mc.player == null) {
            return false;
        }
        return !this.isGliding() && !ElytraFlyModule.mc.player.onGround() && !ElytraFlyModule.mc.player.onClimbable() && !ElytraFlyModule.mc.player.isInWater() && !ElytraFlyModule.mc.player.hasEffect(MobEffects.LEVITATION) && this.canGlideItem();
    }

    public boolean canGlideNow() {
        return this.canGlideItem();
    }

    private boolean canGlideItem() {
        if (ElytraFlyModule.mc.player == null) {
            return false;
        }
        ItemStack itemStack = ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST);
        return LivingEntity.canGlideUsing((ItemStack)itemStack, (EquipmentSlot)EquipmentSlot.CHEST);
    }

    private Vec3 getSnappedObstacleDir() {
        if (ElytraFlyModule.mc.player == null) {
            return new Vec3(0.0, 0.0, 1.0);
        }
        Vec3 delta = ElytraFlyModule.mc.player.position().subtract(this.bounceObstacleStartPos);
        Vec3 normalized = delta.lengthSqr() > 1.0E-12 ? delta.normalize() : new Vec3(0.0, 0.0, 1.0);
        double dirX = normalized.x;
        double dirZ = normalized.z;
        double angleDeg = Math.toDegrees(Math.atan2(dirZ, dirX));
        double wrappedAngle = (angleDeg % 360.0 + 360.0) % 360.0;
        double snappedDeg = (double)Math.round(wrappedAngle / 45.0) * 45.0;
        double snappedRad = Math.toRadians((snappedDeg % 360.0 + 360.0) % 360.0);
        double horizLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        return new Vec3(Math.cos(snappedRad) * horizLen, 0.0, Math.sin(snappedRad) * horizLen);
    }

    private Vec3 findClosestPointOnObstacleLine(Vec3 pos, Vec3 dir) {
        Vec3 offset = pos.subtract(this.bounceObstacleStartPos);
        double dirLenSq = dir.dot(dir);
        double proj = dirLenSq > 1.0E-9 ? offset.dot(dir) / dirLenSq : 0.0;
        return this.bounceObstacleStartPos.add(dir.scale(proj));
    }

    private boolean obstacleRayCastObstructed(Vec3 startPos, Vec3 dir) {
        if (ElytraFlyModule.mc.level == null || ElytraFlyModule.mc.player == null) {
            return false;
        }
        double lookAhead = this.bounceObstacleLookAhead.getValue().doubleValue();
        if (lookAhead <= 0.0) {
            return false;
        }
        Vec3 unitDir = dir.length() > 1.0E-6 ? dir.normalize() : new Vec3(0.0, 0.0, 1.0);
        Vec3 endPos = startPos.add(unitDir.scale(lookAhead));
        BlockHitResult blockHitResult = ElytraFlyModule.mc.level.clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)ElytraFlyModule.mc.player));
        return blockHitResult.getType() == HitResult.Type.BLOCK;
    }

    private boolean isObstacleObstructed(Vec3 pos, Vec3 dir) {
        if (ElytraFlyModule.mc.level == null) {
            return false;
        }
        BlockPos blockPos = BlockPos.containing((double)pos.x, (double)pos.y, (double)pos.z).below();
        if (!ElytraFlyModule.mc.level.getBlockState(blockPos).isFaceSturdy((BlockGetter)ElytraFlyModule.mc.level, blockPos, Direction.UP)) {
            return true;
        }
        if (this.obstacleRayCastObstructed(pos.add(0.0, 0.063, 0.0), dir)) {
            return true;
        }
        if (this.obstacleRayCastObstructed(pos.add(0.0, 1.01, 0.0), dir)) {
            return true;
        }
        return this.obstacleRayCastObstructed(pos.add(0.0, 1.99, 0.0), dir);
    }

    private void pathToValidObstaclePoint(Vec3 startPos, Vec3 dir, boolean checkStart) {
        double stepSize = Math.max(1.0, this.bounceObstacleLookAhead.getValue().doubleValue());
        Vec3 unitDir = dir.length() > 1.0E-6 ? dir.normalize() : new Vec3(0.0, 0.0, 1.0);
        boolean forceStep = !checkStart;
        Vec3 candidatePos = startPos;
        int attempts = 0;
        while ((forceStep || this.isObstacleObstructed(candidatePos, unitDir)) && attempts++ < 200) {
            candidatePos = candidatePos.add(unitDir.scale(stepSize));
            forceStep = false;
        }
        this.passObstacleTo(candidatePos);
    }

    private void passObstacleTo(Vec3 targetPos) {
        block5: {
            this.bounceObstaclePassingToPos = targetPos;
            if (this.bounceStandUpToPass.getValue()) {
                this.forceStandUpForObstacle();
            }
            try {
                Object baritone = BaritoneUtils.getPrimaryBaritone();
                if (baritone == null) {
                    return;
                }
                BlockPos blockPos = BlockPos.containing((double)targetPos.x, (double)targetPos.y, (double)targetPos.z);
                Class<?> goalClass = Class.forName("baritone.api.pathing.goals.Goal");
                Object goal = Class.forName("baritone.api.pathing.goals.GoalGetToBlock").getConstructor(BlockPos.class).newInstance(blockPos);
                Object customGoalProcess = baritone.getClass().getMethod("getCustomGoalProcess", new Class[0]).invoke(baritone, new Object[0]);
                customGoalProcess.getClass().getMethod("setGoalAndPath", goalClass).invoke(customGoalProcess, goal);
                if (this.isBounceDebugOn()) {
                    this.debugLog("OBSTACLE PASS: goal set to " + String.valueOf(blockPos));
                }
            }
            catch (Throwable throwable) {
                if (!this.isBounceDebugOn()) break block5;
                this.debugLog("OBSTACLE PASS FAILED: " + String.valueOf(throwable));
            }
        }
    }

    private void forceStandUpForObstacle() {
        if (ElytraFlyModule.mc.player == null || !ElytraFlyModule.mc.player.isFallFlying()) {
            return;
        }
        if (ElytraFlyModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            return;
        }
        int emptySlot = this.findEmptyInventorySlot();
        if (emptySlot == -1) {
            ((EntityFlagAccessor)ElytraFlyModule.mc.player).invokeSetSharedFlag(EntityFlagAccessor.getFlagFallFlying(), false);
            if (this.isBounceDebugOn()) {
                this.debugLog("STAND-UP-TO-PASS: inventory full, fell back to flag-only clear");
            }
            return;
        }
        int containerId = ElytraFlyModule.mc.player.inventoryMenu.containerId;
        ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, 6, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
        ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, emptySlot, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
        ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, emptySlot, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
        ElytraFlyModule.mc.gameMode.handleContainerInput(containerId, 6, 0, ContainerInput.PICKUP, (Player)ElytraFlyModule.mc.player);
        if (this.isBounceDebugOn()) {
            this.debugLog("STAND-UP-TO-PASS: unequipped/re-equipped elytra for obstacle passing");
        }
    }

    private void cancelObstaclePath() {
        try {
            Object baritone = BaritoneUtils.getPrimaryBaritone();
            if (baritone == null) {
                return;
            }
            Object pathingBehavior = baritone.getClass().getMethod("getPathingBehavior", new Class[0]).invoke(baritone, new Object[0]);
            pathingBehavior.getClass().getMethod("cancelEverything", new Class[0]).invoke(pathingBehavior, new Object[0]);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private boolean handlePassingObstacles() {
        if (ElytraFlyModule.mc.player == null) {
            return false;
        }
        if (!this.bouncePassObstacles.getValue()) {
            return false;
        }
        if (!BaritoneUtils.isAvailable()) {
            return false;
        }
        if (!BaritoneUtils.isActive()) {
            this.bounceObstaclePassingToPos = null;
        }
        Vec3 playerPos = ElytraFlyModule.mc.player.position();
        double distFromStart = Math.sqrt(Math.pow(playerPos.x - this.bounceObstacleStartPos.x, 2.0) + Math.pow(playerPos.z - this.bounceObstacleStartPos.z, 2.0));
        if (distFromStart <= 0.1) {
            return false;
        }
        Vec3 obstacleDir = this.getSnappedObstacleDir();
        Vec3 closestPoint = this.findClosestPointOnObstacleLine(playerPos, obstacleDir);
        if (this.bounceObstaclePassingToPos != null) {
            if (this.bounceStandUpToPass.getValue()) {
                this.forceStandUpForObstacle();
            }
            if (this.isObstacleObstructed(this.bounceObstaclePassingToPos, obstacleDir)) {
                this.pathToValidObstaclePoint(this.bounceObstaclePassingToPos, obstacleDir, false);
            }
            return true;
        }
        if (!ElytraFlyModule.mc.player.onGround()) {
            return false;
        }
        boolean isStopped = this.bounceHorizontalSpeedometer() < 0.01;
        if (this.isGliding() && isStopped) {
            this.pathToValidObstaclePoint(closestPoint, obstacleDir, false);
            return true;
        }
        Vec3 projectedPos = new Vec3(playerPos.x, closestPoint.y, playerPos.z);
        double deviation = projectedPos.distanceTo(closestPoint) + Math.min(0.0, playerPos.y - closestPoint.y);
        if (deviation > 2.0) {
            this.pathToValidObstaclePoint(closestPoint, obstacleDir, true);
            return true;
        }
        if (this.isObstacleObstructed(projectedPos, obstacleDir)) {
            this.pathToValidObstaclePoint(closestPoint, obstacleDir, false);
            return true;
        }
        return BaritoneUtils.isActive();
    }

    private double bounceHorizontalSpeedometer() {
        Vec3 delta = this.bounceSpeedCurrPos.subtract(this.bounceSpeedPrevPos);
        return Math.sqrt(delta.x * delta.x + delta.z * delta.z) * 20.0;
    }

    private boolean isInDipWindow() {
        if (ElytraFlyModule.mc.player == null) {
            return false;
        }
        return ElytraFlyModule.mc.player.getY() - this.bounceStartY < 0.163;
    }

    private boolean isNearingBounceLanding() {
        if (ElytraFlyModule.mc.level == null || ElytraFlyModule.mc.player == null) {
            return false;
        }
        Vec3 velocity = ElytraFlyModule.mc.player.getDeltaMovement();
        if (velocity.y >= -0.01) {
            return false;
        }
        Vec3 startPos = ElytraFlyModule.mc.player.position();
        Vec3 endPos = startPos.add(0.0, -8.0, 0.0);
        BlockHitResult blockHitResult = ElytraFlyModule.mc.level.clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)ElytraFlyModule.mc.player));
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        double distToGround = startPos.y - blockHitResult.getLocation().y;
        double ticksToLand = distToGround / Math.abs(velocity.y);
        return ticksToLand <= this.bounceBypassTicks.getValue().doubleValue();
    }

    private double getBounceHorizontalSpeed() {
        if (ElytraFlyModule.mc.player == null) {
            return 0.0;
        }
        Vec3 velocity = ElytraFlyModule.mc.player.getDeltaMovement();
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0;
    }

    private void flushBouncePackets() {
        if (this.isFlushingBouncePackets) {
            return;
        }
        this.isFlushingBouncePackets = true;
        try {
            Connection connection;
            if (mc.getConnection() == null) {
                this.bounceSendPacketQueue.clear();
                this.bouncePingPacketQueue.clear();
                return;
            }
            if (!this.bounceSendPacketQueue.isEmpty()) {
                connection = mc.getConnection().getConnection();
                if (connection == null || !connection.isConnected()) {
                    this.bounceSendPacketQueue.clear();
                } else {
                    Packet<?> packet;
                    while ((packet = this.bounceSendPacketQueue.poll()) != null) {
                        connection.send(packet, null, true);
                    }
                }
            }
            if (!this.bouncePingPacketQueue.isEmpty()) {
                ClientboundPingPacket pingPacket;
                while ((pingPacket = this.bouncePingPacketQueue.poll()) != null) {
                    pingPacket.handle((ClientCommonPacketListener)mc.getConnection());
                }
            }
        }
        finally {
            this.isFlushingBouncePackets = false;
        }
    }

    private void resetBounceState() {
        this.bounceDoJump = false;
        this.bounceJumpThisTick = false;
        this.bouncePendingResend = false;
        this.bounceTakeoffPending = false;
        this.bounceFlagPauseTicksLeft = 0;
        this.bouncePrevGliding = false;
        this.bouncePrevJumpKeyDown = false;
        this.bounceWasFlyingForLog = false;
        this.bounceBypassTicksLeft = 0;
    }

    private void debugLog(String message) {
        if (ElytraFlyModule.mc.player != null) {
            Night.CHAT_MANAGER.tagged(message, "EBounce+");
        }
    }

    private boolean isBounceDebugOn() {
        ElytraBounceDebugModule elytraBounceDebugModule = Night.MODULE_MANAGER.getModule(ElytraBounceDebugModule.class);
        return elytraBounceDebugModule != null && elytraBounceDebugModule.isToggled();
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }
}

