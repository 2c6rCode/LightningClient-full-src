/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lightning.mixins.accessors.ClientWorldAccessor
 *  lightning.mixins.accessors.PlayerMoveC2SPacketAccessor
 *  lombok.Generated
 *  net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.player;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackBlockEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.DestroyBlockEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.mixins.accessors.ClientWorldAccessor;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.modules.impl.combat.AutoMineModule;
import lightning.modules.impl.combat.AutoTrapModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CMiningStatePacket;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.IMinecraft;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="SpeedMine", description="Automatically mines blocks at a faster speed using packets.", category=Module.Category.PLAYER)
public class SpeedMineModule
extends Module {
    public ModeSetting switchMode = new ModeSetting("Switch", "The mode that will be used for automatically switching to the fastest item.", "Silent", InventoryUtils.SWITCH_MODES);
    public NumberSetting range = new NumberSetting("Range", "The maximum distance at which blocks will be mined.", 6.0, 0.0, 8.0);
    public NumberSetting speed;
    public BooleanSetting farReach;
    public BooleanSetting sixB;
    public ModeSetting rotate;
    public BooleanSetting switchReset;
    public BooleanSetting doubleMine;
    public BooleanSetting shift;
    public ModeSetting await;
    public ModeSetting rebreak;
    public NumberSetting instantDelay;
    public NumberSetting instantTimeout;
    public BooleanSetting async;
    public CategorySetting onGroundCheckCategory;
    public BooleanSetting ncpCheck;
    public BooleanSetting airCheck;
    public BooleanSetting liquidCheck;
    public BooleanSetting grim;
    public BooleanSetting whileEating;
    public ModeSetting whitelistMode;
    public WhitelistSetting whitelist;
    public CategorySetting renderCategory;
    public ModeSetting render;
    public ModeSetting animation;
    public ModeSetting color;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    public ModeSetting instantRender;
    public ColorSetting instantColor;
    private Action primary;
    private Secondary secondary;
    private static final int SECONDARY_TIMEOUT = 10;
    private int secondaryHoldSlot;
    private int secondaryOriginalSlot;
    private static final int SECONDARY_MAX_TICKS = 60;
    private boolean doubleEngaged;
    private boolean primaryPaired;
    private boolean handlingSwitchReset;
    private final Timer instantTimer;
    private static final long INSTANT_ACK_STALL_MS = 200L;
    private final Timer mineTimer;
    private double delayBalance;
    private long lastStopMs;
    private static final int STOP_COOLDOWN_TICKS = 6;
    private int stopCooldown;
    private volatile boolean interactPaused;
    private volatile long interactPausedAt;
    private boolean needsRestart;
    public final Object interactSyncLock;
    private static final long INTERACT_PAUSE_TIMEOUT_MS = 750L;
    public volatile BlockPos proxyPrimaryPos;
    public volatile float proxyPrimaryProgress;
    public volatile BlockPos proxySecondaryPos;
    public volatile float proxySecondaryProgress;
    private volatile float prevProxyPrimaryProgress;
    private volatile long proxyPrimaryUpdateTime;
    private volatile long proxyPrimaryUpdateInterval;
    private volatile float prevProxySecondaryProgress;
    private volatile long proxySecondaryUpdateTime;
    private volatile long proxySecondaryUpdateInterval;
    private static final int GRIM_DECOY_Y_OFFSET = 2000;

    public SpeedMineModule() {
        Double defaultSpeed = 0.7;
        this.speed = new NumberSetting("Speed", "The speed at which the module will mine blocks.", defaultSpeed, defaultSpeed, 1.0);
        this.farReach = new BooleanSetting("FarReach", "Bypass for Duration 0.7 on Grim. Try this if normal AutoMine doesn't work.", false);
        this.sixB = new BooleanSetting("6b", "Allows mining bedrock after 40 ticks and enables instant-mining when broken.", false);
        this.rotate = new ModeSetting("Rotate", "Automatically rotates to the block when mining it.", "Normal", new String[]{"None", "Normal", "Silent"});
        this.switchReset = new BooleanSetting("SwitchReset", "Resets the mining when switching slots.", new ModeSetting.Visibility(this.switchMode, "None", "AltSwap", "AltPickup"), true);
        this.doubleMine = new BooleanSetting("Double", "Allows the mining of 2 blocks at the same time.", false);
        this.shift = new BooleanSetting("Shift", "Selects the block behind according to crosshair as secondary when clicking while holding shift.", new BooleanSetting.Visibility(this.doubleMine, true), true);
        this.await = new ModeSetting("Await", "Wait for blocks to break before swapping back. Off: don't wait. Dynamic: wait only while the secondary isn't close to done yet. Always: always wait. Works around real NCP's FastBreak check on some Paper servers (cancels doubleMine's secondary otherwise) -- costs mining throughput while waiting.", new BooleanSetting.Visibility(this.doubleMine, true), "Off", new String[]{"Off", "Dynamic", "Always"});
        this.rebreak = new ModeSetting("Rebreak", "Automatically re-mines blocks once they have been replaced.", "None", new String[]{"None", "Fast", "Instant"});
        this.instantDelay = new NumberSetting("InstantDelay", "The amount of time that has to pass before instantly mining blocks.", new ModeSetting.Visibility(this.rebreak, "Fast", "Instant"), (Number)0, (Number)0, (Number)20);
        this.instantTimeout = new NumberSetting("InstantTimeout", "The amount of time that cancel instantly mine while no block to mine.", new ModeSetting.Visibility(this.rebreak, "Fast", "Instant"), (Number)60, (Number)0, (Number)100);
        this.async = new BooleanSetting("Async", "Keeps instantly re-firing even while the target position is currently air, instead of waiting for it to solidify.", new ModeSetting.Visibility(this.rebreak, "Fast", "Instant"), false);
        this.onGroundCheckCategory = new CategorySetting("OnGroundCheck", "Claims onGround on outgoing movement packets while mining, bypassing the not-on-the-ground mining speed penalty.");
        this.ncpCheck = new BooleanSetting("NCP", "Enables the onGround claim below. Turn off for servers that don't penalize air/liquid mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
        this.airCheck = new BooleanSetting("AirCheck", "Claims onGround while airborne and mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
        this.liquidCheck = new BooleanSetting("LiquidCheck", "Claims onGround while in liquid and mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
        this.grim = new BooleanSetting("Grim", "Adds a bypass catered to the Grim anticheat.", false);
        this.whileEating = new BooleanSetting("WhileEating", "Mines blocks while eating.", true);
        this.whitelistMode = new ModeSetting("Mode", "All = mine every block. WhiteList = mine only listed blocks. BlackList = mine every block except listed.", "All", new String[]{"All", "WhiteList", "BlackList"});
        this.whitelist = new WhitelistSetting("List", "Blocks the WhiteList/BlackList mode compares against.", WhitelistSetting.Type.BLOCKS);
        this.renderCategory = new CategorySetting("Render", "The category containing all settings related to rendering.");
        this.render = new ModeSetting("Render", "Mode", "The rendering that will be applied to the blocks highlighted.", new CategorySetting.Visibility(this.renderCategory), "Both", new String[]{"None", "Fill", "Outline", "Both"});
        this.animation = new ModeSetting("Animation", "The animation that will be used when rendering the block mining progress.", new ModeSetting.Visibility(this.render, "Fill", "Outline", "Both"), "Expand", new String[]{"None", "Expand", "Rise"});
        this.color = new ModeSetting("Color", "The color that will be used when rendering the block mining.", new ModeSetting.Visibility(this.render, "Fill", "Outline", "Both"), "Smooth", new String[]{"Static", "Smooth", "Custom"});
        this.fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.render, "Fill", "Both"), ColorUtils.getDefaultFillColor());
        this.outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.render, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
        this.instantRender = new ModeSetting("InstantRender", "Instant", "The color that will be used for rendering instantly mined blocks.", new CategorySetting.Visibility(this.renderCategory), "None", new String[]{"None", "Default", "Custom"});
        this.instantColor = new ColorSetting("InstantColor", "The custom color used for instantly mined blocks.", new ModeSetting.Visibility(this.instantRender, "Custom"), new ColorSetting.Color(new Color(148, 0, 211), false, false));
        this.primary = null;
        this.secondary = null;
        this.secondaryHoldSlot = -1;
        this.secondaryOriginalSlot = -1;
        this.doubleEngaged = false;
        this.primaryPaired = false;
        this.handlingSwitchReset = false;
        this.instantTimer = new Timer();
        this.mineTimer = new Timer();
        this.delayBalance = 0.0;
        this.lastStopMs = 0L;
        this.stopCooldown = 0;
        this.interactPaused = false;
        this.interactPausedAt = 0L;
        this.needsRestart = false;
        this.interactSyncLock = new Object();
        this.proxyPrimaryPos = null;
        this.proxyPrimaryProgress = 0.0f;
        this.proxySecondaryPos = null;
        this.proxySecondaryProgress = 0.0f;
        this.prevProxyPrimaryProgress = 0.0f;
        this.proxyPrimaryUpdateTime = 0L;
        this.proxyPrimaryUpdateInterval = 50L;
        this.prevProxySecondaryProgress = 0.0f;
        this.proxySecondaryUpdateTime = 0L;
        this.proxySecondaryUpdateInterval = 50L;
    }

    public Action getPrimary() {
        return this.primary;
    }

    public Secondary getSecondary() {
        return this.secondary;
    }

    public void setDoubleEngaged(boolean engaged) {
        this.doubleEngaged = engaged;
    }

    private boolean hasSecondarySlot() {
        return this.secondary != null;
    }

    private boolean canStartNow() {
        if (!this.whileEating.getValue() && (this.interactPaused || SpeedMineModule.mc.player != null && (SpeedMineModule.mc.player.isUsingItem() || EntityUtils.isEating()))) {
            return false;
        }
        if (!this.farReach.getValue()) {
            return true;
        }
        return this.stopCooldown == 0 && this.canBegin();
    }

    private boolean canBegin() {
        long elapsedMs = System.currentTimeMillis() - this.lastStopMs;
        if (elapsedMs >= 275L) {
            return true;
        }
        double penalty = (300L - elapsedMs) * (long)(this.farReach.getValue() ? 2 : 1);
        return this.delayBalance + penalty <= 900.0;
    }

    private void trackStarts(int count) {
        long elapsedMs = System.currentTimeMillis() - this.lastStopMs;
        for (int i = 0; i < count; ++i) {
            if (elapsedMs >= 275L) {
                this.delayBalance *= 0.9;
                continue;
            }
            this.delayBalance += (double)(300L - elapsedMs);
        }
        this.delayBalance = Mth.clamp((double)this.delayBalance, (double)-1000.0, (double)1000.0);
    }

    private void markStop() {
        this.markStop(false);
    }

    private void markStop(boolean applyCooldown) {
        this.lastStopMs = System.currentTimeMillis();
        if (applyCooldown) {
            this.stopCooldown = 6;
        }
    }

    public BlockPos getMiningPosition() {
        return this.primary != null && this.primary.isMining() ? this.primary.getPosition() : null;
    }

    public boolean isInteractPaused() {
        return this.interactPaused;
    }

    public void setInteractPaused(boolean paused) {
        this.interactPaused = paused;
        if (paused) {
            this.interactPausedAt = System.currentTimeMillis();
        } else {
            this.needsRestart = true;
        }
    }

    public void updateProxyMiningState(BlockPos primaryPos, float primaryProgress, BlockPos secondaryPos, float secondaryProgress) {
        long now = System.currentTimeMillis();
        boolean primaryChanged = primaryPos == null ? this.proxyPrimaryPos != null : !primaryPos.equals((Object)this.proxyPrimaryPos);
        this.prevProxyPrimaryProgress = primaryChanged ? primaryProgress : this.proxyPrimaryProgress;
        this.proxyPrimaryUpdateInterval = Mth.clamp((long)(now - this.proxyPrimaryUpdateTime), (long)1L, (long)500L);
        this.proxyPrimaryUpdateTime = now;
        this.proxyPrimaryPos = primaryPos;
        this.proxyPrimaryProgress = primaryProgress;
        boolean secondaryChanged = secondaryPos == null ? this.proxySecondaryPos != null : !secondaryPos.equals((Object)this.proxySecondaryPos);
        this.prevProxySecondaryProgress = secondaryChanged ? secondaryProgress : this.proxySecondaryProgress;
        this.proxySecondaryUpdateInterval = Mth.clamp((long)(now - this.proxySecondaryUpdateTime), (long)1L, (long)500L);
        this.proxySecondaryUpdateTime = now;
        this.proxySecondaryPos = secondaryPos;
        this.proxySecondaryProgress = secondaryProgress;
    }

    private float interpolatedProgress(float prevProgress, float currentProgress, long updateTimeMs, long updateIntervalMs) {
        float fraction = Mth.clamp((float)((float)(System.currentTimeMillis() - updateTimeMs) / (float)updateIntervalMs), (float)0.0f, (float)1.0f);
        return prevProgress + (currentProgress - prevProgress) * fraction;
    }

    public boolean handle(BlockPos blockPos, int priority) {
        if (!this.canHandle(blockPos)) {
            return false;
        }
        if (this.primary != null && this.primary.getPosition().equals((Object)blockPos) || this.secondary != null && this.secondary.getPosition().equals((Object)blockPos)) {
            return true;
        }
        if (this.primary != null && priority <= 0 && this.sixB.getValue() && this.primary.isBedrockOrigin() && priority <= this.primary.getPriority()) {
            return false;
        }
        boolean useDouble = this.doubleMine.getValue() && (this.doubleEngaged || priority > 0);
        if (useDouble) {
            if (this.secondary != null) {
                if (this.await.getValue().equalsIgnoreCase("Always")) {
                    return false;
                }
                if (this.await.getValue().equalsIgnoreCase("Dynamic") && !this.secondary.isHolding()) {
                    return false;
                }
            }
            Secondary demotedSecondary = this.secondary == null && this.primary != null ? this.primary.demote() : null;
            if (demotedSecondary != null) {
                this.secondary = demotedSecondary;
            } else if (this.primary != null) {
                this.primary.cancel();
            }
            this.primary = new Action(this, this, blockPos, priority);
            this.primaryPaired = this.hasSecondarySlot();
        } else {
            if (this.primary != null) {
                this.primary.cancel();
            }
            this.primary = new Action(this, this, blockPos, priority);
            this.primaryPaired = false;
        }
        return true;
    }

    public boolean fillSecondary(BlockPos blockPos, int priority) {
        if (this.primary != null && this.primary.isTerrainBase()) {
            return false;
        }
        if (!this.canHandle(blockPos)) {
            return false;
        }
        if (this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK) {
            return false;
        }
        if (this.secondary != null) {
            return this.secondary.getPosition().equals((Object)blockPos);
        }
        if (this.primary != null && this.primary.getPosition().equals((Object)blockPos)) {
            return true;
        }
        Action action = new Action(this, this, blockPos, priority);
        Secondary demoted = action.demote();
        if (demoted == null) {
            return false;
        }
        this.secondary = demoted;
        return true;
    }

    public boolean isMineTimerReady() {
        return this.mineTimer.hasTimeElapsed(50L);
    }

    public void dropPrimaryIfInvalid(Predicate<BlockPos> predicate) {
        if (this.primary == null || this.primary.getPriority() != 0) {
            return;
        }
        if (!predicate.test(this.primary.getPosition())) {
            this.dropPrimary();
        }
    }

    public void dropSecondaryIfInvalid(Predicate<BlockPos> predicate) {
        if (this.secondary == null || this.secondary.getPriority() != 0) {
            return;
        }
        if (!predicate.test(this.secondary.getPosition())) {
            this.dropSecondary();
        }
    }

    public void dropPrimary() {
        if (this.primary == null) {
            return;
        }
        this.primary.cancel();
        this.primary = null;
        this.primaryPaired = false;
    }

    public void dropSecondary() {
        if (this.secondary == null) {
            return;
        }
        this.secondary.release();
        this.secondary = null;
    }

    public boolean primaryDigging() {
        return this.primary != null && !SpeedMineModule.mc.level.getBlockState(this.primary.getPosition()).canBeReplaced();
    }

    public boolean primaryCamping() {
        if (this.primary == null || !this.primary.isInstantMine()) {
            return false;
        }
        if (!SpeedMineModule.mc.level.getBlockState(this.primary.getPosition()).canBeReplaced() && !this.sixB.getValue()) {
            return false;
        }
        Target target = this.getTarget();
        return target != null && this.isTargetSurroundPosition(this.primary.getPosition(), target.player());
    }

    public boolean slotsFull() {
        if (this.primary != null && this.sixB.getValue() && this.primary.isBedrockOrigin()) {
            return true;
        }
        return !(!this.primaryDigging() && !this.primaryCamping() || this.doubleEngaged && !this.hasSecondarySlot() && !this.primaryPaired);
    }

    public boolean phaseSlotsFull() {
        return this.primaryDigging() && (!this.doubleEngaged || this.hasSecondarySlot() || this.primaryPaired);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.isDeferringToProxy()) {
            return;
        }
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        if (this.stopCooldown > 0) {
            --this.stopCooldown;
        }
        if (!this.doubleMine.getValue()) {
            this.doubleEngaged = false;
        }
        if (this.secondary != null && !this.doubleMine.getValue()) {
            this.secondary.release();
            this.secondary = null;
        } else if (this.secondary != null && this.secondary.process()) {
            this.secondary = null;
        }
        if (this.primary != null && this.primary.process()) {
            this.primary = null;
            this.primaryPaired = false;
        }
        if (this.isProxyActive()) {
            this.syncMiningStateToClient();
        }
        AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
        AutoMineModule autoMineModule2 = autoMineModule;
        if (autoMineModule != null) {
            autoMineModule.placePendingTerrain();
        }
        if (this.primary != null && autoMineModule != null && autoMineModule.isProtectedTerrainBase(this.primary.getPosition())) {
            this.primary.cancel();
            this.primary = null;
            this.primaryPaired = false;
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        if (this.isDeferringToProxy()) {
            this.renderProxyState(renderWorldEvent.getMatrices());
            return;
        }
        if (this.doubleMine.getValue() && this.secondary != null) {
            this.secondary.render(renderWorldEvent.getMatrices());
        }
        if (this.primary != null) {
            this.primary.render(renderWorldEvent.getMatrices());
        }
    }

    @SubscribeEvent
    public void night$onBlockUpdate(PacketReceiveEvent packetReceiveEvent) {
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (!(packet instanceof ClientboundBlockUpdatePacket)) {
            return;
        }
        ClientboundBlockUpdatePacket clientboundBlockUpdatePacket = (ClientboundBlockUpdatePacket)packet;
        BlockPos blockPos = clientboundBlockUpdatePacket.getPos();
        if (this.primary != null && blockPos.equals((Object)this.primary.getPosition())) {
            this.primary.markBroken();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketSend(PacketSendEvent.Post post) {
        ServerboundPlayerActionPacket serverboundPlayerActionPacket;
        Packet<?> packet;
        if (this.isDeferringToProxy()) {
            return;
        }
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        if (this.handlingSwitchReset) {
            return;
        }
        if (post.getPacket() instanceof ServerboundSetCarriedItemPacket && this.switchReset.getValue() && (this.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.switchMode.getValue().equalsIgnoreCase("AltPickup"))) {
            this.handlingSwitchReset = true;
            try {
                if (this.primary != null) {
                    this.primary.cancel();
                    this.primary.tryStart();
                }
            }
            finally {
                this.handlingSwitchReset = false;
            }
        }
        if ((packet = post.getPacket()) instanceof ServerboundPlayerActionPacket && (serverboundPlayerActionPacket = (ServerboundPlayerActionPacket)packet).getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && serverboundPlayerActionPacket.getPos().getY() > 2000) {
            return;
        }
        packet = post.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket serverboundMovePlayerPacket = (ServerboundMovePlayerPacket)packet;
            if (this.ncpCheck.getValue() && this.primary != null) {
                boolean airborne = this.airCheck.getValue() && !SpeedMineModule.mc.player.onGround() && !SpeedMineModule.mc.player.isInWater();
                boolean inWater = this.liquidCheck.getValue() && SpeedMineModule.mc.player.isInWater();
                if (airborne || inWater) {
                    ((PlayerMoveC2SPacketAccessor)serverboundMovePlayerPacket).setOnGround(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttackBlock(AttackBlockEvent attackBlockEvent) {
        if (this.isDeferringToProxy()) {
            return;
        }
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            return;
        }
        BlockPos blockPos = attackBlockEvent.getPosition();
        if (this.doubleMine.getValue() && this.shift.getValue() && (SpeedMineModule.mc.options.keyShift.isDown() || SpeedMineModule.mc.player.isShiftKeyDown())) {
            Vec3 lookAngle = SpeedMineModule.mc.player.getLookAngle();
            double absX = Math.abs(lookAngle.x);
            double absY = Math.abs(lookAngle.y);
            double absZ = Math.abs(lookAngle.z);
            Direction direction = absY > absX && absY > absZ ? (lookAngle.y > 0.0 ? Direction.UP : Direction.DOWN) : (absX > absZ ? (lookAngle.x > 0.0 ? Direction.EAST : Direction.WEST) : (lookAngle.z > 0.0 ? Direction.SOUTH : Direction.NORTH));
            BlockPos behindPos = blockPos.relative(direction);
            if (this.isValid(behindPos) && !this.isOutOfRange(behindPos) && !SpeedMineModule.mc.level.getBlockState(behindPos).canBeReplaced()) {
                this.handle(behindPos, 1);
            }
        }
        if (this.handle(blockPos, 1)) {
            attackBlockEvent.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.primary = null;
        this.secondary = null;
        this.doubleEngaged = false;
        this.primaryPaired = false;
    }

    @Override
    public void onEnable() {
        this.doubleEngaged = false;
        this.primaryPaired = false;
    }

    @Override
    public void onDisable() {
        this.doubleEngaged = false;
        this.primaryPaired = false;
        if (this.isDeferringToProxy()) {
            return;
        }
        this.restoreIronSwap();
        if (SpeedMineModule.mc.player == null || SpeedMineModule.mc.level == null) {
            this.primary = null;
            this.secondary = null;
            return;
        }
        if (this.secondary != null) {
            this.secondary.release();
        }
        this.secondary = null;
        if (this.primary != null) {
            this.primary.cancel();
            this.primary = null;
        }
        this.setInteractPaused(false);
    }

    public void restoreIronSwap() {
        int ironSlot;
        if (this.primary != null) {
            this.primary.restoreIronSwap();
        }
        if (SpeedMineModule.mc.player != null && (ironSlot = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
            int swapTargetSlot = InventoryUtils.findFastestItem(Blocks.OBSIDIAN.defaultBlockState(), InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
            if (swapTargetSlot == -1) {
                swapTargetSlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
            }
            if (swapTargetSlot != -1 && swapTargetSlot > InventoryUtils.HOTBAR_END) {
                InventoryUtils.swap("Swap", swapTargetSlot, ironSlot);
            }
        }
    }

    @Override
    public String getMetaData() {
        String primaryText = this.primary == null ? "0.0" : new DecimalFormat("0.0").format(this.primary.getProgress() / this.primary.getSpeed());
        String secondaryText = this.secondary == null || !this.doubleMine.getValue() ? "" : ", " + new DecimalFormat("0.0").format(this.secondary.getProgress() / this.secondary.getSpeed());
        return primaryText + secondaryText;
    }

    private boolean canHandle(BlockPos blockPos) {
        if (SpeedMineModule.mc.gameMode.getPlayerMode() == GameType.CREATIVE || SpeedMineModule.mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return false;
        }
        if (!(SpeedMineModule.mc.level.getBlockState(blockPos).getBlock().defaultDestroyTime() != -1.0f || this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK)) {
            return false;
        }
        AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
        if (autoMineModule != null && autoMineModule.isProtectedTerrainBase(blockPos)) {
            return false;
        }
        boolean inWhitelist = this.whitelist.isWhitelistContains(SpeedMineModule.mc.level.getBlockState(blockPos).getBlock());
        boolean allowed = switch (this.whitelistMode.getValue()) {
            case "WhiteList" -> inWhitelist;
            case "BlackList" -> !inWhitelist;
            default -> true;
        };
        if (!allowed) {
            return false;
        }
        return !(SpeedMineModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.range.getValue().doubleValue()));
    }

    public boolean isInvalid(BlockPos blockPos) {
        if (!this.isValid(blockPos)) {
            return true;
        }
        return this.isMining(blockPos);
    }

    public boolean isValid(BlockPos blockPos) {
        if (blockPos == null) {
            return false;
        }
        if (!(SpeedMineModule.mc.level.getBlockState(blockPos).getBlock().defaultDestroyTime() != -1.0f || this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK)) {
            return false;
        }
        return !SpeedMineModule.mc.level.getBlockState(blockPos).getBlock().equals(Blocks.COBWEB);
    }

    public boolean isMining(BlockPos blockPos) {
        if (blockPos == null) {
            return true;
        }
        if (this.primary != null && this.primary.getPosition().equals((Object)blockPos)) {
            return true;
        }
        return this.secondary != null && this.secondary.getPosition().equals((Object)blockPos);
    }

    public boolean isTargetSurroundPosition(BlockPos blockPos, Player player) {
        int baseY;
        if (blockPos == null || player == null || SpeedMineModule.mc.level == null) {
            return false;
        }
        AABB aABB = player.getBoundingBox();
        for (int i = baseY = Mth.floor((double)player.getY()); i <= baseY + 2; ++i) {
            for (int j = Mth.floor((double)aABB.minX); j < Mth.ceil((double)aABB.maxX); ++j) {
                for (int k = Mth.floor((double)aABB.minZ); k < Mth.ceil((double)aABB.maxZ); ++k) {
                    BlockPos playerFootPos = new BlockPos(j, i, k);
                    if (blockPos.equals((Object)playerFootPos)) {
                        return true;
                    }
                    if (i > baseY + 1) continue;
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        if (!blockPos.equals((Object)playerFootPos.relative(direction))) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isOutOfRange(BlockPos blockPos) {
        if (blockPos == null) {
            return true;
        }
        return SpeedMineModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.range.getValue().doubleValue());
    }

    public Target getTarget() {
        LogoutSpotModule logoutSpotModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(LogoutSpotModule.class) : null;
        PopChamsModule popChamsModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(PopChamsModule.class) : null;
        ArrayList<Player> candidates = new ArrayList<Player>(SpeedMineModule.mc.level.players());
        if (logoutSpotModule != null && logoutSpotModule.isToggled()) {
            for (Player player : logoutSpotModule.getGhosts()) {
                if (player == null || candidates.contains(player)) continue;
                candidates.add(player);
            }
        }
        Target target = null;
        for (Player player : candidates) {
            LogoutSpotModule.Spot spot;
            boolean isGhost;
            if (player == SpeedMineModule.mc.player || popChamsModule != null && popChamsModule.isGhost((Entity)player) || !(isGhost = EntityUtils.isGhost((Entity)player)) && (!player.isAlive() || player.getHealth() <= 0.0f) || SpeedMineModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)(this.range.getValue().doubleValue() + 2.0)) || (logoutSpotModule != null && logoutSpotModule.isGhost((Entity)player) ? (spot = logoutSpotModule.getSpot((RemotePlayer)player)) != null && Night.FRIEND_MANAGER.contains(spot.data.name) : Night.FRIEND_MANAGER.contains(player.getName().getString()))) continue;
            List<Position> positions = this.getPositions(player);
            BlockPos targetPos = this.getTargetPosition(positions);
            if (!this.doubleMine.getValue() && (positions.isEmpty() || targetPos == null)) continue;
            if (target == null) {
                target = new Target(player, positions, targetPos);
                continue;
            }
            boolean currentTargetIsGhost = EntityUtils.isGhost((Entity)target.player());
            if (currentTargetIsGhost != isGhost) {
                if (!currentTargetIsGhost) continue;
                target = new Target(player, positions, targetPos);
                continue;
            }
            if (!(SpeedMineModule.mc.player.distanceToSqr((Entity)player) < SpeedMineModule.mc.player.distanceToSqr((Entity)target.player()))) continue;
            target = new Target(player, positions, targetPos);
        }
        return target;
    }

    private BlockPos getTargetPosition(List<Position> positions) {
        BlockPos bestPos = null;
        double bestScore = 0.0;
        for (Position position : positions) {
            if (this.doubleMine.getValue() && !position.feetPosition() || !this.isValidPosition(position.position()) || HoleUtils.isPlayerInHole((Player)SpeedMineModule.mc.player) && HoleUtils.getFeetPositions((Player)SpeedMineModule.mc.player, true, false, true).contains(position.position())) continue;
            double score = 0.0;
            if (this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.BEDROCK) {
                score = position.feetPosition() ? score + 1000.0 : score - 1000.0;
            }
            if (position.feetPosition()) {
                score += 5.0;
                if (SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.ENDER_CHEST) {
                    score += 0.95;
                } else if (WorldUtils.isCrystalPlaceable(position.position().offset(0, 1, 0))) {
                    score += 0.35;
                }
                if (this.hasCityPosition(position.position())) {
                    score += 0.6;
                }
            } else {
                score = SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.ENDER_CHEST ? score - 2.0 : (WorldUtils.isCrystalPlaceable(position.position().offset(0, 1, 0)) ? score + 0.75 : score - 2.0);
            }
            if (!(score >= bestScore)) continue;
            bestPos = position.position();
            bestScore = score;
        }
        return bestPos;
    }

    private List<Position> getPositions(Player player) {
        ArrayList<Position> positions = new ArrayList<Position>();
        for (BlockPos blockPos : HoleUtils.getFeetPositions(player, true, false, true)) {
            positions.add(new Position(blockPos, true));
            if (this.doubleMine.getValue()) continue;
            positions.add(new Position(blockPos.offset(0, 1, 0), false));
        }
        if (!this.doubleMine.getValue()) {
            positions.add(new Position(player.blockPosition().offset(0, 2, 0), false));
        }
        return positions;
    }

    private boolean isValidPosition(BlockPos blockPos) {
        if (SpeedMineModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        if (!(SpeedMineModule.mc.level.getBlockState(blockPos).getBlock().defaultDestroyTime() != -1.0f || this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK)) {
            return false;
        }
        return !this.isOutOfRange(blockPos);
    }

    private boolean hasCityPosition(BlockPos blockPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = blockPos.relative(direction);
            if (!WorldUtils.isPlaceable(adjacentPos)) continue;
            return true;
        }
        return false;
    }

    private boolean isProxyActive() {
        return PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && Night.PROXY_SERVER != null;
    }

    private boolean isDeferringToProxy() {
        return PingBypassFlags.isPingBypassActive();
    }

    private void syncMiningStateToClient() {
        ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CMiningStatePacket(this.primary != null ? this.primary.getPosition() : null, this.primary != null ? this.primary.getProgress() / this.primary.getSpeed() : 0.0f, this.secondary != null ? this.secondary.getPosition() : null, this.secondary != null ? this.secondary.getProgress() / this.secondary.getSpeed() : 0.0f)));
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundCustomPayloadPacket);
        }
    }

    private void renderProxyState(PoseStack poseStack) {
        float progress;
        if (this.proxyPrimaryPos != null) {
            progress = this.interpolatedProgress(this.prevProxyPrimaryProgress, this.proxyPrimaryProgress, this.proxyPrimaryUpdateTime, this.proxyPrimaryUpdateInterval);
            this.renderProxyBlock(poseStack, this.proxyPrimaryPos, progress);
        }
        if (this.proxySecondaryPos != null && this.doubleMine.getValue()) {
            progress = this.interpolatedProgress(this.prevProxySecondaryProgress, this.proxySecondaryProgress, this.proxySecondaryUpdateTime, this.proxySecondaryUpdateInterval);
            this.renderProxyBlock(poseStack, this.proxySecondaryPos, progress);
        }
    }

    private void renderProxyBlock(PoseStack poseStack, BlockPos blockPos, float progress) {
        if (SpeedMineModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return;
        }
        AABB aABB = new AABB(blockPos);
        if (this.animation.getValue().equalsIgnoreCase("Expand")) {
            aABB = new AABB(blockPos).deflate(0.5).inflate(Mth.clamp((double)((double)progress / 2.0), (double)0.0, (double)0.5));
        }
        if (this.animation.getValue().equalsIgnoreCase("Rise")) {
            aABB = new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)blockPos.getX() + 1.0, (double)((float)blockPos.getY() + progress), (double)blockPos.getZ() + 1.0);
        }
        Color fill = this.fillColor.getColor();
        Color outline = this.outlineColor.getColor();
        if (this.color.getValue().equalsIgnoreCase("Static")) {
            fill = (double)progress >= 0.9 ? new Color(0, 255, 0, this.fillColor.getAlpha()) : new Color(255, 0, 0, this.fillColor.getAlpha());
            outline = (double)progress >= 0.9 ? new Color(0, 255, 0, this.outlineColor.getAlpha()) : new Color(255, 0, 0, this.outlineColor.getAlpha());
        } else if (this.color.getValue().equalsIgnoreCase("Smooth")) {
            fill = new Color(255 - (int)(Mth.clamp((float)progress, (float)0.0f, (float)1.0f) * 255.0f), (int)(Mth.clamp((float)progress, (float)0.0f, (float)1.0f) * 255.0f), 0, this.fillColor.getAlpha());
            outline = new Color(255 - (int)(Mth.clamp((float)progress, (float)0.0f, (float)1.0f) * 255.0f), (int)(Mth.clamp((float)progress, (float)0.0f, (float)1.0f) * 255.0f), 0, this.outlineColor.getAlpha());
        }
        if (this.render.getValue().equalsIgnoreCase("Fill") || this.render.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBox(poseStack, aABB, fill);
        }
        if (this.render.getValue().equalsIgnoreCase("Outline") || this.render.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBoxOutline(poseStack, aABB, outline);
        }
    }

    private void serverSend(Packet<?> packet) {
        Connection connection;
        if (this.isProxyActive() && (connection = Night.PROXY_SERVER.getServerConnection()) != null && connection.isConnected()) {
            ProxyServerTickListener.allowSend(() -> connection.send(packet));
            return;
        }
        mc.getConnection().send(packet);
    }

    private <T extends Packet<?>> void serverSendSequenced(IntFunction<T> intFunction) {
        if (this.isProxyActive()) {
            try (BlockStatePredictionHandler blockStatePredictionHandler = ((ClientWorldAccessor)SpeedMineModule.mc.level).invokeGetPendingUpdateManager().startPredicting();){
                this.serverSend((Packet)intFunction.apply(blockStatePredictionHandler.currentSequence()));
            }
        } else {
            NetworkUtils.sendSequencedPacket(sequence -> {
                Packet packet = (Packet)intFunction.apply(sequence);
                return packet;
            });
        }
    }

    private void sendRawPlayerAction(ServerboundPlayerActionPacket.Action action, BlockPos blockPos, Direction direction) {
        this.sendRawPlayerAction(action, blockPos, direction, false);
    }

    private void sendRawPlayerAction(ServerboundPlayerActionPacket.Action action, BlockPos blockPos, Direction direction, boolean applyCooldown) {
        if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            this.markStop(applyCooldown);
        }
        try (BlockStatePredictionHandler blockStatePredictionHandler = ((ClientWorldAccessor)SpeedMineModule.mc.level).invokeGetPendingUpdateManager().startPredicting();){
            this.serverSend((Packet<?>)new ServerboundPlayerActionPacket(action, blockPos, direction, blockStatePredictionHandler.currentSequence()));
        }
    }

    public class Action {
        private final BlockPos position;
        private BlockState state;
        private final int priority;
        private float progress;
        private float prevProgress;
        private int attempts;
        private int ticks;
        private boolean mining;
        private long stallTime;
        private boolean instantMine;
        private int startSlot;
        private boolean started;
        private int brokenCount;
        private int lastBrokenCount;
        private final Timer instantRemineResetTimer;
        private float ghostIronProgress;
        private boolean ironSwapped;
        private int swappedIronInventorySlot;
        private int swappedIronHotbarSlot;
        private boolean terrainBase;
        private boolean terrainSurround;
        private final boolean bedrockOrigin;
        final SpeedMineModule this$0;
        final /* synthetic */ SpeedMineModule this$0$;

        public Action(SpeedMineModule outerInstance, SpeedMineModule moduleRef, BlockPos blockPos, int priority) {
            Objects.requireNonNull(outerInstance);
            this.this$0$ = outerInstance;
            Objects.requireNonNull(moduleRef);
            this.this$0 = moduleRef;
            this.startSlot = -1;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer = new Timer();
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.position = blockPos;
            this.state = IMinecraft.mc.level.getBlockState(blockPos);
            this.bedrockOrigin = this.state.getBlock() == Blocks.BEDROCK;
            this.priority = priority;
            this.tryStart();
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public BlockState getState() {
            return this.state;
        }

        public float getProgress() {
            return this.progress;
        }

        public int getTicks() {
            return this.ticks;
        }

        public boolean isMining() {
            return this.mining;
        }

        public boolean isInstantMine() {
            return this.instantMine;
        }

        public int getPriority() {
            return this.priority;
        }

        public boolean isTerrainBase() {
            return this.terrainBase;
        }

        public void setTerrainBase(boolean terrainBase) {
            this.terrainBase = terrainBase;
        }

        public boolean isTerrainSurround() {
            return this.terrainSurround;
        }

        public void setTerrainSurround(boolean terrainSurround) {
            this.terrainSurround = terrainSurround;
        }

        public boolean isStarted() {
            return this.started;
        }

        private int getBreakSlot(BlockState blockState, int fallbackSlot) {
            int ironSlot;
            AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            if (autoMineModule != null && autoMineModule.isToggled() && autoMineModule.ironPickaxe.getValue() && this.ironSwapped && (ironSlot = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
                return ironSlot;
            }
            return fallbackSlot;
        }

        private long asyncFireDelayMs() {
            long baseDelayMs = this.this$0.instantDelay.getValue().longValue() * 50L;
            if (this.position == null) {
                return baseDelayMs;
            }
            Target target = this.this$0.getTarget();
            if (target == null || target.player() == null) {
                return baseDelayMs;
            }
            boolean isHeadBlock = this.position.getY() == Mth.floor((double)target.player().getY()) + 2 && this.this$0.isTargetSurroundPosition(this.position, target.player());
            if (!isHeadBlock) {
                return baseDelayMs;
            }
            AutoTrapModule autoTrapModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoTrapModule.class) : null;
            AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            boolean autoTrapEnabled = autoTrapModule != null && autoTrapModule.isToggled();
            boolean headPriority = autoMineModule != null && autoMineModule.isToggled() && autoMineModule.priority.getValue().equalsIgnoreCase("Head");
            if (autoTrapEnabled && headPriority) {
                return Math.max(baseDelayMs, (long)Night.SERVER_MANAGER.getPing());
            }
            return baseDelayMs;
        }

        private void updateIronGhost() {
            int ironSlot;
            AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            boolean ironEnabled = autoMineModule != null && autoMineModule.isToggled() && autoMineModule.ironPickaxe.getValue();
            if (ironEnabled && !this.ironSwapped && (ironSlot = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END)) != -1 && this.state != null && !this.state.isAir()) {
                float ironSpeed = WorldUtils.getMineSpeed(this.state, ironSlot) / Night.WORLD_MANAGER.getTimerMultiplier();
                this.ghostIronProgress = Mth.clamp((float)(this.ghostIronProgress + ironSpeed), (float)0.0f, (float)this.getSpeed());
                if (this.ghostIronProgress >= this.getSpeed()) {
                    int hotbarTarget = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
                    if (hotbarTarget == -1) {
                        hotbarTarget = IMinecraft.mc.player.getInventory().getSelectedSlot();
                    }
                    if (ironSlot > InventoryUtils.HOTBAR_END) {
                        InventoryUtils.swap("Swap", ironSlot, hotbarTarget);
                        this.ironSwapped = true;
                        this.swappedIronInventorySlot = ironSlot;
                        this.swappedIronHotbarSlot = hotbarTarget;
                    } else if (ironSlot != -1) {
                        this.ironSwapped = true;
                        this.swappedIronInventorySlot = -1;
                        this.swappedIronHotbarSlot = ironSlot;
                    }
                }
            }
        }

        public void restoreIronSwap() {
            if (this.ironSwapped) {
                int ironHotbarSlot;
                if (this.swappedIronInventorySlot > InventoryUtils.HOTBAR_END && this.swappedIronHotbarSlot >= 0 && this.swappedIronHotbarSlot <= InventoryUtils.HOTBAR_END) {
                    if (IMinecraft.mc.player != null && IMinecraft.mc.player.getInventory().getItem(this.swappedIronHotbarSlot).is(Items.IRON_PICKAXE)) {
                        InventoryUtils.swap("Swap", this.swappedIronInventorySlot, this.swappedIronHotbarSlot);
                    }
                } else if (IMinecraft.mc.player != null && (ironHotbarSlot = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
                    BlockState blockState = this.state != null && !this.state.isAir() ? this.state : Blocks.OBSIDIAN.defaultBlockState();
                    int restoreInvSlot = this.swappedIronInventorySlot > InventoryUtils.HOTBAR_END ? this.swappedIronInventorySlot : InventoryUtils.findFastestItem(blockState, InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
                    if (restoreInvSlot == -1) {
                        restoreInvSlot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
                    }
                    if (restoreInvSlot != -1 && restoreInvSlot > InventoryUtils.HOTBAR_END) {
                        InventoryUtils.swap("Swap", restoreInvSlot, ironHotbarSlot);
                    }
                }
                this.ironSwapped = false;
                this.swappedIronInventorySlot = -1;
                this.swappedIronHotbarSlot = -1;
                this.ghostIronProgress = 0.0f;
            }
        }

        private Secondary demote() {
            this.restoreIronSwap();
            if (!this.started) {
                return null;
            }
            BlockState blockState = IMinecraft.mc.level.getBlockState(this.position);
            if (blockState.canBeReplaced()) {
                return null;
            }
            if (this.this$0.sixB.getValue() && blockState.getBlock() == Blocks.BEDROCK) {
                return null;
            }
            if (!this.instantMine && this.progress < this.getSpeed()) {
                Direction direction = WorldUtils.getClosestDirection(this.position, true);
                int fastestSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(blockState, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                if (fastestSlot == -1) {
                    fastestSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                this.fireBreakBurst(direction, fastestSlot, true);
            }
            return new Secondary(this.this$0$, this.this$0, this.position, this.priority, blockState, this.progress);
        }

        private void markBroken() {
            ++this.brokenCount;
        }

        private boolean ackStalled() {
            if (this.brokenCount != this.lastBrokenCount) {
                this.instantRemineResetTimer.reset();
                this.lastBrokenCount = this.brokenCount;
            }
            return this.instantRemineResetTimer.hasTimeElapsed(200L);
        }

        private boolean instantAckStalled() {
            return IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() && this.ackStalled();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void tryStart() {
            synchronized (this.this$0.interactSyncLock) {
                if (!this.this$0.canStartNow()) {
                    return;
                }
                this.start();
            }
        }

        public boolean process() {
            if (this.this$0.isOutOfRange(this.position)) {
                this.cancel();
                return true;
            }
            this.updateIronGhost();
            boolean usingItem = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
            if (this.this$0.interactPaused && !usingItem && System.currentTimeMillis() - this.this$0.interactPausedAt >= 750L) {
                this.this$0.setInteractPaused(false);
            }
            if (this.this$0.needsRestart && this.this$0.isProxyActive()) {
                this.this$0.needsRestart = false;
                this.started = false;
            }
            if (!this.started) {
                if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                    return true;
                }
                this.tryStart();
                return false;
            }
            if (!this.this$0.farReach.getValue()) {
                return this.legacyProcess();
            }
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                if (this.brokenCount == 0) {
                    this.markBroken();
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    this.instantMine = true;
                    return false;
                }
                if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                    this.progress = 0.0f;
                    this.prevProgress = 0.0f;
                }
                if (this.instantMine) {
                    if (this.this$0.async.getValue()) {
                        if ((this.this$0.whileEating.getValue() || !EntityUtils.isEating()) && !this.isAsyncFireBlockedByCombo() && this.this$0.instantTimer.hasTimeElapsed(this.asyncFireDelayMs())) {
                            Direction direction = WorldUtils.getClosestDirection(this.position, true);
                            int toolSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                            if (toolSlot == -1) {
                                toolSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                            }
                            this.fireBreakBurst(direction, this.getBreakSlot(this.state, toolSlot), false, false);
                            this.this$0.instantTimer.reset();
                            ++this.attempts;
                        }
                        return false;
                    }
                    if (this.instantAckStalled()) {
                        return false;
                    }
                    long timeoutTicks = this.this$0.instantTimeout.getValue().longValue();
                    boolean timedOut = timeoutTicks > 0L && this.this$0.instantTimer.hasTimeElapsed(timeoutTicks * 50L);
                    if (!timedOut) {
                        return false;
                    }
                }
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
                }
                this.cancel();
                return true;
            }
            Direction direction = WorldUtils.getClosestDirection(this.position, true);
            BlockState blockState = IMinecraft.mc.level.getBlockState(this.position);
            if (!blockState.canBeReplaced() && blockState.getBlock() != this.state.getBlock()) {
                this.state = blockState;
            }
            if (this.mining) {
                ++this.ticks;
                int miningSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                if (miningSlot == -1) {
                    miningSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                float mineSpeed = this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK ? 0.025f : WorldUtils.getMineSpeed(this.state, miningSlot) / Night.WORLD_MANAGER.getTimerMultiplier();
                if (this.instantMine) {
                    this.prevProgress = this.getSpeed();
                    this.progress = this.getSpeed();
                } else {
                    this.prevProgress = this.progress;
                    this.progress = Mth.clamp((float)(this.progress + mineSpeed), (float)0.0f, (float)this.getSpeed());
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    } else {
                        IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                if (this.this$0.rotate.getValue().equalsIgnoreCase("Normal") && this.progress + mineSpeed * 2.0f >= this.getSpeed()) {
                    float[] rotations = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.PosRot(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY(), IMinecraft.mc.player.getZ(), rotations[0], rotations[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                    } else {
                        Night.ROTATION_MANAGER.legacyRotate(rotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this.this$0));
                    }
                }
                boolean currentlyUsingItem = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
                boolean paused = this.this$0.interactPaused || !this.this$0.whileEating.getValue() && currentlyUsingItem;
                if (this.progress >= this.getSpeed() && !blockState.canBeReplaced() && !paused) {
                    if (this.instantMine && this.brokenCount == 0 && this.ackStalled()) {
                        this.start();
                        return false;
                    }
                    if (!this.instantMine || this.this$0.async.getValue() || this.this$0.instantTimer.hasTimeElapsed(this.this$0.instantDelay.getValue().longValue() * 50L)) {
                        this.fireBreakBurst(direction, this.getBreakSlot(this.state, miningSlot), false, !this.instantMine);
                        if (!this.instantMine) {
                            this.this$0.mineTimer.reset();
                        }
                        ++this.attempts;
                        if (this.this$0.rebreak.getValue().equalsIgnoreCase("None") && !this.this$0.sixB.getValue() || this.terrainBase) {
                            this.mining = false;
                            this.stallTime = System.currentTimeMillis();
                        } else {
                            this.instantMine = true;
                            if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                                this.progress = 0.0f;
                                this.prevProgress = 0.0f;
                            }
                            this.this$0.instantTimer.reset();
                            this.instantRemineResetTimer.reset();
                        }
                    }
                    return false;
                }
            } else if (!(IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() || this.attempts != 0 && System.currentTimeMillis() - this.stallTime < 150L)) {
                this.tryStart();
            }
            return false;
        }

        private boolean legacyProcess() {
            this.updateIronGhost();
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                if (this.brokenCount == 0) {
                    this.markBroken();
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    this.instantMine = true;
                    return false;
                }
                if (this.instantMine) {
                    if (this.this$0.async.getValue()) {
                        if ((this.this$0.whileEating.getValue() || !EntityUtils.isEating()) && !this.isAsyncFireBlockedByCombo() && this.this$0.instantTimer.hasTimeElapsed(this.asyncFireDelayMs())) {
                            Direction direction = WorldUtils.getClosestDirection(this.position, true);
                            int toolSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                            if (toolSlot == -1) {
                                toolSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                            }
                            this.legacyFireBreakBurst(direction, this.getBreakSlot(this.state, toolSlot));
                            this.this$0.instantTimer.reset();
                            ++this.attempts;
                        }
                        return false;
                    }
                    if (this.instantAckStalled()) {
                        return false;
                    }
                    long timeoutTicks = this.this$0.instantTimeout.getValue().longValue();
                    boolean timedOut = timeoutTicks > 0L && this.this$0.instantTimer.hasTimeElapsed(timeoutTicks * 50L);
                    if (!timedOut) {
                        return false;
                    }
                }
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
                }
                this.cancel();
                return true;
            }
            Direction direction = WorldUtils.getClosestDirection(this.position, true);
            BlockState blockState = IMinecraft.mc.level.getBlockState(this.position);
            if (!blockState.canBeReplaced() && blockState.getBlock() != this.state.getBlock()) {
                this.state = blockState;
            }
            if (this.mining) {
                ++this.ticks;
                int miningSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                if (miningSlot == -1) {
                    miningSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                float mineSpeed = this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK ? 0.025f : WorldUtils.getMineSpeed(this.state, miningSlot) / Night.WORLD_MANAGER.getTimerMultiplier();
                if (this.instantMine) {
                    this.prevProgress = this.getSpeed();
                    this.progress = this.getSpeed();
                } else {
                    this.prevProgress = this.progress;
                    this.progress = Mth.clamp((float)(this.progress + mineSpeed), (float)0.0f, (float)this.getSpeed());
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    } else {
                        IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                    if (IMinecraft.mc.player == null || !IMinecraft.mc.player.isUsingItem() || IMinecraft.mc.player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
                        IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                    }
                }
                if (this.this$0.rotate.getValue().equalsIgnoreCase("Normal") && this.progress + mineSpeed * 2.0f >= this.getSpeed()) {
                    float[] rotations = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.PosRot(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY(), IMinecraft.mc.player.getZ(), rotations[0], rotations[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                    } else {
                        Night.ROTATION_MANAGER.legacyRotate(rotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this.this$0));
                    }
                }
                if (this.progress >= this.getSpeed() && !blockState.canBeReplaced() && (this.this$0.whileEating.getValue() || !EntityUtils.isEating())) {
                    if (this.instantMine && this.brokenCount == 0 && this.ackStalled()) {
                        this.start();
                        return false;
                    }
                    if (!this.instantMine || this.this$0.async.getValue() || this.this$0.instantTimer.hasTimeElapsed(this.this$0.instantDelay.getValue().longValue() * 50L)) {
                        this.legacyFireBreakBurst(direction, this.getBreakSlot(this.state, miningSlot));
                        ++this.attempts;
                        if (this.this$0.rebreak.getValue().equalsIgnoreCase("None") && !this.this$0.sixB.getValue() || this.terrainBase) {
                            this.mining = false;
                            this.stallTime = System.currentTimeMillis();
                        } else {
                            this.instantMine = true;
                            if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                                this.progress = 0.0f;
                                this.prevProgress = 0.0f;
                            }
                            this.this$0.instantTimer.reset();
                            this.instantRemineResetTimer.reset();
                        }
                    }
                    return false;
                }
            } else if (!(IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() || this.attempts != 0 && System.currentTimeMillis() - this.stallTime < 150L)) {
                this.start();
            }
            return false;
        }

        private boolean isHeadCrystalComboPending() {
            AutoTrapModule autoTrapModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoTrapModule.class) : null;
            if (autoTrapModule == null || !autoTrapModule.isToggled()) {
                return false;
            }
            AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoCrystalModule.class) : null;
            if (autoCrystalModule == null || !autoCrystalModule.isToggled()) {
                return false;
            }
            Target target = this.this$0.getTarget();
            if (target == null || target.player() == null) {
                return false;
            }
            int targetY = Mth.floor((double)target.player().getY());
            return this.position.getY() >= targetY + 2 && Math.abs((double)this.position.getX() + 0.5 - target.player().getX()) < 1.3 && Math.abs((double)this.position.getZ() + 0.5 - target.player().getZ()) < 1.3;
        }

        private boolean isCivBreakComboPending() {
            AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            return autoMineModule != null && autoMineModule.isToggled() && autoMineModule.isUpComboActive(this.position);
        }

        private boolean isAsyncFireBlockedByCombo() {
            return this.isHeadCrystalComboPending() || this.isCivBreakComboPending();
        }

        private void checkAndPlaceHeadCrystal(BlockPos blockPos) {
            if (IMinecraft.mc.player == null || IMinecraft.mc.level == null || IMinecraft.mc.getConnection() == null) {
                return;
            }
            if (!this.instantMine) {
                return;
            }
            AutoTrapModule autoTrapModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoTrapModule.class) : null;
            if (autoTrapModule == null || !autoTrapModule.isToggled()) {
                return;
            }
            AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoCrystalModule.class) : null;
            if (autoCrystalModule == null || !autoCrystalModule.isToggled()) {
                return;
            }
            BlockState blockState = IMinecraft.mc.level.getBlockState(blockPos);
            if (blockState.getBlock() != Blocks.OBSIDIAN) {
                return;
            }
            Target target = this.this$0.getTarget();
            if (target == null || target.player() == null) {
                return;
            }
            int targetY = Mth.floor((double)target.player().getY());
            boolean isHeadPos = blockPos.getY() >= targetY + 2 && Math.abs((double)blockPos.getX() + 0.5 - target.player().getX()) < 1.3 && Math.abs((double)blockPos.getZ() + 0.5 - target.player().getZ()) < 1.3;
            if (!isHeadPos) {
                return;
            }
            BlockPos abovePos = blockPos.above();
            if (!WorldUtils.isCrystalPlaceable(abovePos)) {
                return;
            }
            int crystalSlot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
            boolean offhandCrystal = IMinecraft.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            if (!offhandCrystal && crystalSlot == -1) {
                return;
            }
            InteractionHand interactionHand = offhandCrystal ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            Vec3 hitVec = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5);
            BlockHitResult blockHitResult = new BlockHitResult(hitVec, Direction.UP, blockPos, false);
            if (this.this$0.isProxyActive()) {
                if (offhandCrystal) {
                    this.this$0.serverSendSequenced(sequence -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, sequence));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(interactionHand));
                } else {
                    int previousSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(crystalSlot));
                    this.this$0.serverSendSequenced(sequence -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, sequence));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(interactionHand));
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(previousSlot));
                }
            } else if (offhandCrystal) {
                NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, sequence));
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(interactionHand));
            } else {
                int previousSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.switchSlot("Silent", crystalSlot, previousSlot);
                NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, sequence));
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(interactionHand));
                InventoryUtils.switchBack("Silent", crystalSlot, previousSlot);
            }
        }

        private void legacyFireBreakBurst(Direction direction, int toolSlot) {
            this.checkAndPlaceHeadCrystal(this.position);
            Night.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
            if (this.this$0.rotate.getValue().equalsIgnoreCase("Silent")) {
                float[] rotations = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.Rot(rotations[0], rotations[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                } else {
                    Night.ROTATION_MANAGER.wireRotate("Silent", rotations);
                }
            }
            int previousSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            if (this.this$0.isProxyActive()) {
                int targetSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : toolSlot;
                boolean shouldSwap = targetSlot != -1 && targetSlot != previousSlot;
                if (shouldSwap) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(targetSlot));
                }
                this.stopDestroyBlock(this.position, direction, true);
                if (this.this$0.grim.getValue()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (shouldSwap) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(previousSlot));
                }
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), toolSlot, previousSlot);
                this.stopDestroyBlock(this.position, direction, true);
                if (this.this$0.grim.getValue()) {
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), toolSlot, previousSlot);
            }
        }

        private void fireBreakBurst(Direction direction, int toolSlot, boolean demoting) {
            this.fireBreakBurst(direction, toolSlot, demoting, !demoting);
        }

        private void fireBreakBurst(Direction direction, int toolSlot, boolean demoting, boolean applyCooldown) {
            if (!demoting) {
                this.checkAndPlaceHeadCrystal(this.position);
                Night.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
            }
            if (this.this$0.rotate.getValue().equalsIgnoreCase("Silent")) {
                float[] rotations = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.Rot(rotations[0], rotations[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                } else {
                    Night.ROTATION_MANAGER.wireRotate("Silent", rotations);
                }
            }
            int currentSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            if (this.this$0.isProxyActive()) {
                int restoreSlot = this.startSlot != -1 ? this.startSlot : currentSlot;
                int targetSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : toolSlot;
                if (targetSlot != -1) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(targetSlot));
                    if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal")) {
                        IMinecraft.mc.player.getInventory().setSelectedSlot(targetSlot);
                    }
                }
                this.stopDestroyBlock(this.position, direction, !demoting, applyCooldown);
                if (this.this$0.grim.getValue()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                if (targetSlot != -1) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(restoreSlot));
                    if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal")) {
                        IMinecraft.mc.player.getInventory().setSelectedSlot(restoreSlot);
                    }
                }
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), toolSlot, currentSlot);
                this.stopDestroyBlock(this.position, direction, !demoting, applyCooldown);
                if (this.this$0.grim.getValue()) {
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), toolSlot, currentSlot);
            }
        }

        private void stopDestroyBlock(BlockPos blockPos, Direction direction, boolean sendSwing) {
            this.stopDestroyBlock(blockPos, direction, sendSwing, false);
        }

        private void stopDestroyBlock(BlockPos blockPos, Direction direction, boolean sendSwing, boolean applyCooldown) {
            try (BlockStatePredictionHandler blockStatePredictionHandler = ((ClientWorldAccessor)IMinecraft.mc.level).invokeGetPendingUpdateManager().startPredicting();){
                ServerboundPlayerActionPacket serverboundPlayerActionPacket = new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction, blockStatePredictionHandler.currentSequence());
                this.this$0.markStop(applyCooldown);
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)serverboundPlayerActionPacket);
                } else {
                    IMinecraft.mc.getConnection().send((Packet)serverboundPlayerActionPacket);
                }
            }
        }

        public void render(PoseStack poseStack) {
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() && !this.instantMine) {
                return;
            }
            AABB aABB = new AABB(this.position);
            boolean isInstantAir = IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() && this.instantMine;
            double progressRatio = isInstantAir ? (double)(this.progress / this.getSpeed()) : (double)Mth.clamp((float)Mth.lerp((float)IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), (float)(this.prevProgress / this.getSpeed()), (float)(this.progress / this.getSpeed())), (float)0.0f, (float)1.0f);
            if (this.this$0.animation.getValue().equalsIgnoreCase("Expand")) {
                aABB = new AABB(this.position).deflate(0.5).inflate(Mth.clamp((double)(progressRatio / 2.0), (double)0.0, (double)0.5));
            }
            if (this.this$0.animation.getValue().equalsIgnoreCase("Rise")) {
                aABB = new AABB((double)this.position.getX(), (double)this.position.getY(), (double)this.position.getZ(), (double)this.position.getX() + 1.0, (double)this.position.getY() + progressRatio, (double)this.position.getZ() + 1.0);
            }
            Color fill = this.this$0.fillColor.getColor();
            Color outline = this.this$0.outlineColor.getColor();
            if (this.this$0.color.getValue().equalsIgnoreCase("Static")) {
                fill = progressRatio >= 0.9 ? new Color(0, 255, 0, this.this$0.fillColor.getAlpha()) : new Color(255, 0, 0, this.this$0.fillColor.getAlpha());
                outline = progressRatio >= 0.9 ? new Color(0, 255, 0, this.this$0.outlineColor.getAlpha()) : new Color(255, 0, 0, this.this$0.outlineColor.getAlpha());
            } else if (this.this$0.color.getValue().equalsIgnoreCase("Smooth")) {
                fill = new Color(255 - (int)(Mth.clamp((double)progressRatio, (double)0.0, (double)1.0) * 255.0), (int)(Mth.clamp((double)progressRatio, (double)0.0, (double)1.0) * 255.0), 0, this.this$0.fillColor.getAlpha());
                outline = new Color(255 - (int)(Mth.clamp((double)progressRatio, (double)0.0, (double)1.0) * 255.0), (int)(Mth.clamp((double)progressRatio, (double)0.0, (double)1.0) * 255.0), 0, this.this$0.outlineColor.getAlpha());
            }
            if (progressRatio >= (double)this.getSpeed() && this.instantMine && this.this$0.instantRender.getValue().equalsIgnoreCase("Custom")) {
                fill = ColorUtils.getColor(this.this$0.instantColor.getColor(), this.this$0.fillColor.getAlpha());
                outline = ColorUtils.getColor(this.this$0.instantColor.getColor(), this.this$0.outlineColor.getAlpha());
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Fill") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(poseStack, aABB, fill);
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Outline") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(poseStack, aABB, outline);
            }
        }

        public void start() {
            if (!this.this$0.farReach.getValue()) {
                this.legacyStart();
                return;
            }
            this.this$0.trackStarts(this.this$0.farReach.getValue() ? 2 : 1);
            Direction direction = WorldUtils.getClosestDirection(this.position, true);
            if (this.this$0.isProxyActive()) {
                this.startSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction);
                    BlockPos decoyPos = this.grimDecoyPos();
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, decoyPos, WorldUtils.getClosestDirection(decoyPos, true));
                } else {
                    this.this$0.serverSendSequenced(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, sequence));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction);
                    BlockPos decoyPos = this.grimDecoyPos();
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, decoyPos, WorldUtils.getClosestDirection(decoyPos, true));
                } else {
                    NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, sequence));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
            if (this.state.getBlock() == Blocks.BEDROCK) {
                IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
            }
            this.restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.ticks = 0;
            this.mining = true;
            this.instantMine = false;
            this.started = true;
            this.brokenCount = 0;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer.reset();
        }

        private void legacyStart() {
            Direction direction = WorldUtils.getClosestDirection(this.position, true);
            if (this.this$0.isProxyActive()) {
                int toolSlot = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                boolean shouldSwap = toolSlot != -1 && toolSlot != IMinecraft.mc.player.getInventory().getSelectedSlot();
                if (shouldSwap) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(toolSlot));
                }
                if (this.this$0.doubleMine.getValue()) {
                    this.this$0.serverSendSequenced(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, sequence));
                    this.this$0.serverSendSequenced(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, sequence));
                    this.this$0.serverSendSequenced(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, sequence));
                } else {
                    this.this$0.serverSendSequenced(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, sequence));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                if (this.this$0.doubleMine.getValue()) {
                    NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, sequence));
                    NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, sequence));
                    NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, sequence));
                } else {
                    NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, sequence));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
            if (this.state.getBlock() == Blocks.BEDROCK) {
                IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
            }
            this.restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.ticks = 0;
            this.mining = true;
            this.instantMine = false;
            this.started = true;
            this.brokenCount = 0;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer.reset();
        }

        public void cancel() {
            if (!this.this$0.farReach.getValue()) {
                this.legacyCancel();
                return;
            }
            if (!this.this$0.doubleMine.getValue() && this.started) {
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else if (this.this$0.isProxyActive()) {
                    this.this$0.serverSendSequenced(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true), sequence));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else {
                    NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true), sequence));
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }
            if (this.this$0.isProxyActive() && this.startSlot != -1) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(this.startSlot));
                IMinecraft.mc.player.getInventory().setSelectedSlot(this.startSlot);
                this.startSlot = -1;
            }
            this.restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.mining = false;
            this.started = false;
            this.instantMine = false;
        }

        private void legacyCancel() {
            if (!this.this$0.doubleMine.getValue()) {
                Direction direction = WorldUtils.getClosestDirection(this.position, true);
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSendSequenced(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, direction, sequence));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else {
                    NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, direction, sequence));
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
            }
            this.restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.mining = false;
            this.started = false;
            this.instantMine = false;
        }

        private float getSpeed() {
            return this.this$0.speed.getValue().floatValue();
        }

        private BlockPos grimDecoyPos() {
            return this.position.below(2000);
        }

        public int getTicksRemaining() {
            if (!this.mining) {
                return Integer.MAX_VALUE;
            }
            if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                float bedrockSpeed = 0.025f;
                return Math.max(0, Math.round((this.getSpeed() - this.progress) / bedrockSpeed));
            }
            int bestSlot = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (bestSlot == -1) {
                bestSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            }
            float speed = WorldUtils.getMineSpeed(this.state, bestSlot) / Night.WORLD_MANAGER.getTimerMultiplier();
            if (speed <= 0.0f) {
                return Integer.MAX_VALUE;
            }
            return Math.max(0, Math.round((this.getSpeed() - this.progress) / speed));
        }

        @Generated
        public float getPrevProgress() {
            return this.prevProgress;
        }

        @Generated
        public int getAttempts() {
            return this.attempts;
        }

        @Generated
        public long getStallTime() {
            return this.stallTime;
        }

        @Generated
        public int getStartSlot() {
            return this.startSlot;
        }

        @Generated
        public int getBrokenCount() {
            return this.brokenCount;
        }

        @Generated
        public int getLastBrokenCount() {
            return this.lastBrokenCount;
        }

        @Generated
        public Timer getInstantRemineResetTimer() {
            return this.instantRemineResetTimer;
        }

        @Generated
        public float getGhostIronProgress() {
            return this.ghostIronProgress;
        }

        @Generated
        public boolean isIronSwapped() {
            return this.ironSwapped;
        }

        @Generated
        public int getSwappedIronInventorySlot() {
            return this.swappedIronInventorySlot;
        }

        @Generated
        public int getSwappedIronHotbarSlot() {
            return this.swappedIronHotbarSlot;
        }

        @Generated
        public boolean isBedrockOrigin() {
            return this.bedrockOrigin;
        }

        @Generated
        public void setProgress(float progress) {
            this.progress = progress;
        }
    }

    public class Secondary {
        private final BlockPos position;
        private final int priority;
        private BlockState state;
        private float progress;
        private float prevProgress;
        private int ticks;
        private boolean holding;
        private int holdTicks;
        final SpeedMineModule this$0;

        private Secondary(SpeedMineModule outerInstance, SpeedMineModule moduleRef, BlockPos blockPos, int priority, BlockState blockState, float progress) {
            Objects.requireNonNull(outerInstance);
            Objects.requireNonNull(moduleRef);
            this.this$0 = moduleRef;
            this.position = blockPos;
            this.priority = priority;
            this.state = blockState;
            this.progress = progress;
            this.prevProgress = progress;
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public int getPriority() {
            return this.priority;
        }

        public boolean isMining() {
            return true;
        }

        public boolean isHolding() {
            return this.holding;
        }

        public float getSpeed() {
            return 1.0f;
        }

        public float getProgress() {
            return Mth.clamp((float)this.progress, (float)0.0f, (float)1.0f);
        }

        public boolean process() {
            BlockState blockState;
            if (this.this$0.isOutOfRange(this.position)) {
                this.release();
                return true;
            }
            boolean usingItem = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
            if (this.this$0.interactPaused && !usingItem && System.currentTimeMillis() - this.this$0.interactPausedAt >= 750L) {
                this.this$0.setInteractPaused(false);
            }
            if ((blockState = IMinecraft.mc.level.getBlockState(this.position)).canBeReplaced()) {
                Night.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
                this.this$0.mineTimer.reset();
                this.release();
                return true;
            }
            this.state = blockState;
            int bestSlot = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (bestSlot == -1) {
                bestSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            }
            float speed = WorldUtils.getMineSpeed(this.state, bestSlot) / Night.WORLD_MANAGER.getTimerMultiplier();
            if (speed <= 0.0f) {
                this.release();
                return true;
            }
            ++this.ticks;
            this.prevProgress = this.progress;
            this.progress += speed;
            boolean canSwitch = this.this$0.whileEating.getValue() || !usingItem || this.this$0.switchMode.getValue().equalsIgnoreCase("None");
            if (!this.holding && this.progress + speed >= 1.0f && canSwitch) {
                this.hold(bestSlot);
            }
            if (this.holding && !this.this$0.farReach.getValue() && ++this.holdTicks >= 3) {
                this.release();
                return true;
            }
            if (this.progress >= 1.0f + speed * 10.0f) {
                this.release();
                return true;
            }
            if (this.ticks > 60) {
                this.release();
                return true;
            }
            return false;
        }

        private void hold(int slot) {
            if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                return;
            }
            int currentSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
            this.holding = true;
            if (currentSlot == slot) {
                return;
            }
            this.this$0.secondaryOriginalSlot = currentSlot;
            this.this$0.secondaryHoldSlot = slot;
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(slot));
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), slot, currentSlot);
            }
        }

        private void release() {
            if (!this.holding) {
                return;
            }
            this.holding = false;
            if (this.this$0.secondaryHoldSlot == -1) {
                return;
            }
            int originalSlot = this.this$0.secondaryOriginalSlot;
            int holdSlot = this.this$0.secondaryHoldSlot;
            this.this$0.secondaryHoldSlot = -1;
            this.this$0.secondaryOriginalSlot = -1;
            if (IMinecraft.mc.player == null) {
                return;
            }
            if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal") && IMinecraft.mc.player.getInventory().getSelectedSlot() == originalSlot) {
                return;
            }
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(originalSlot));
            } else {
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), holdSlot, originalSlot);
            }
        }

        public void render(PoseStack poseStack) {
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                return;
            }
            double progressRatio = Mth.clamp((float)Mth.lerp((float)IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), (float)this.prevProgress, (float)this.progress), (float)0.0f, (float)1.0f);
            AABB aABB = new AABB(this.position);
            if (this.this$0.animation.getValue().equalsIgnoreCase("Expand")) {
                aABB = new AABB(this.position).deflate(0.5).inflate(Mth.clamp((double)(progressRatio / 2.0), (double)0.0, (double)0.5));
            }
            if (this.this$0.animation.getValue().equalsIgnoreCase("Rise")) {
                aABB = new AABB((double)this.position.getX(), (double)this.position.getY(), (double)this.position.getZ(), (double)this.position.getX() + 1.0, (double)this.position.getY() + progressRatio, (double)this.position.getZ() + 1.0);
            }
            Color fill = this.this$0.fillColor.getColor();
            Color outline = this.this$0.outlineColor.getColor();
            if (this.this$0.color.getValue().equalsIgnoreCase("Static")) {
                fill = progressRatio >= 0.9 ? new Color(0, 255, 0, this.this$0.fillColor.getAlpha()) : new Color(255, 0, 0, this.this$0.fillColor.getAlpha());
                outline = progressRatio >= 0.9 ? new Color(0, 255, 0, this.this$0.outlineColor.getAlpha()) : new Color(255, 0, 0, this.this$0.outlineColor.getAlpha());
            } else if (this.this$0.color.getValue().equalsIgnoreCase("Smooth")) {
                fill = new Color(255 - (int)(progressRatio * 255.0), (int)(progressRatio * 255.0), 0, this.this$0.fillColor.getAlpha());
                outline = new Color(255 - (int)(progressRatio * 255.0), (int)(progressRatio * 255.0), 0, this.this$0.outlineColor.getAlpha());
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Fill") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(poseStack, aABB, fill);
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Outline") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(poseStack, aABB, outline);
            }
        }

        @Generated
        public BlockState getState() {
            return this.state;
        }

        @Generated
        public float getPrevProgress() {
            return this.prevProgress;
        }

        @Generated
        public int getTicks() {
            return this.ticks;
        }

        @Generated
        public int getHoldTicks() {
            return this.holdTicks;
        }
    }

    public record Target(Player player, List<Position> feetPositions, BlockPos position) {
    }

    public record Position(BlockPos position, boolean feetPosition) {
    }
}

