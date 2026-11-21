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
        Double d = 0.7;
        this.speed = new NumberSetting("Speed", "The speed at which the module will mine blocks.", d, d, 1.0);
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

    public void setDoubleEngaged(boolean bl) {
        this.doubleEngaged = bl;
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
        long l = System.currentTimeMillis() - this.lastStopMs;
        if (l >= 275L) {
            return true;
        }
        double d = (300L - l) * (long)(this.farReach.getValue() ? 2 : 1);
        return this.delayBalance + d <= 900.0;
    }

    private void trackStarts(int n) {
        long l = System.currentTimeMillis() - this.lastStopMs;
        for (int i = 0; i < n; ++i) {
            if (l >= 275L) {
                this.delayBalance *= 0.9;
                continue;
            }
            this.delayBalance += (double)(300L - l);
        }
        this.delayBalance = Mth.clamp((double)this.delayBalance, (double)-1000.0, (double)1000.0);
    }

    private void markStop() {
        this.markStop(false);
    }

    private void markStop(boolean bl) {
        this.lastStopMs = System.currentTimeMillis();
        if (bl) {
            this.stopCooldown = 6;
        }
    }

    public BlockPos getMiningPosition() {
        return this.primary != null && this.primary.isMining() ? this.primary.getPosition() : null;
    }

    public boolean isInteractPaused() {
        return this.interactPaused;
    }

    public void setInteractPaused(boolean bl) {
        this.interactPaused = bl;
        if (bl) {
            this.interactPausedAt = System.currentTimeMillis();
        } else {
            this.needsRestart = true;
        }
    }

    public void updateProxyMiningState(BlockPos blockPos, float f, BlockPos blockPos2, float f2) {
        long l = System.currentTimeMillis();
        boolean bl = blockPos == null ? this.proxyPrimaryPos != null : !blockPos.equals((Object)this.proxyPrimaryPos);
        this.prevProxyPrimaryProgress = bl ? f : this.proxyPrimaryProgress;
        this.proxyPrimaryUpdateInterval = Mth.clamp((long)(l - this.proxyPrimaryUpdateTime), (long)1L, (long)500L);
        this.proxyPrimaryUpdateTime = l;
        this.proxyPrimaryPos = blockPos;
        this.proxyPrimaryProgress = f;
        boolean bl2 = blockPos2 == null ? this.proxySecondaryPos != null : !blockPos2.equals((Object)this.proxySecondaryPos);
        this.prevProxySecondaryProgress = bl2 ? f2 : this.proxySecondaryProgress;
        this.proxySecondaryUpdateInterval = Mth.clamp((long)(l - this.proxySecondaryUpdateTime), (long)1L, (long)500L);
        this.proxySecondaryUpdateTime = l;
        this.proxySecondaryPos = blockPos2;
        this.proxySecondaryProgress = f2;
    }

    private float interpolatedProgress(float f, float f2, long l, long l2) {
        float f3 = Mth.clamp((float)((float)(System.currentTimeMillis() - l) / (float)l2), (float)0.0f, (float)1.0f);
        return f + (f2 - f) * f3;
    }

    public boolean handle(BlockPos blockPos, int n) {
        if (!this.canHandle(blockPos)) {
            return false;
        }
        if (this.primary != null && this.primary.getPosition().equals((Object)blockPos) || this.secondary != null && this.secondary.getPosition().equals((Object)blockPos)) {
            return true;
        }
        if (this.primary != null && n <= 0 && this.sixB.getValue() && this.primary.isBedrockOrigin() && n <= this.primary.getPriority()) {
            return false;
        }
        boolean bl = this.doubleMine.getValue() && (this.doubleEngaged || n > 0);
        boolean bl2 = bl;
        if (bl) {
            if (this.secondary != null) {
                if (this.await.getValue().equalsIgnoreCase("Always")) {
                    return false;
                }
                if (this.await.getValue().equalsIgnoreCase("Dynamic") && !this.secondary.isHolding()) {
                    return false;
                }
            }
            Secondary secondary = this.secondary == null && this.primary != null ? this.primary.demote() : null;
            Secondary secondary2 = secondary;
            if (secondary != null) {
                this.secondary = secondary;
            } else if (this.primary != null) {
                this.primary.cancel();
            }
            this.primary = new Action(this, this, blockPos, n);
            this.primaryPaired = this.hasSecondarySlot();
        } else {
            if (this.primary != null) {
                this.primary.cancel();
            }
            this.primary = new Action(this, this, blockPos, n);
            this.primaryPaired = false;
        }
        return true;
    }

    public boolean fillSecondary(BlockPos blockPos, int n) {
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
        Action action = new Action(this, this, blockPos, n);
        Secondary secondary = action.demote();
        if (secondary == null) {
            return false;
        }
        this.secondary = secondary;
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
                boolean bl = this.airCheck.getValue() && !SpeedMineModule.mc.player.onGround() && !SpeedMineModule.mc.player.isInWater();
                boolean bl2 = this.liquidCheck.getValue() && SpeedMineModule.mc.player.isInWater();
                boolean bl3 = bl2;
                if (bl || bl2) {
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
            Vec3 vec3 = SpeedMineModule.mc.player.getLookAngle();
            double d = Math.abs(vec3.x);
            double d2 = Math.abs(vec3.y);
            double d3 = Math.abs(vec3.z);
            Direction direction = d2 > d && d2 > d3 ? (vec3.y > 0.0 ? Direction.UP : Direction.DOWN) : (d > d3 ? (vec3.x > 0.0 ? Direction.EAST : Direction.WEST) : (vec3.z > 0.0 ? Direction.SOUTH : Direction.NORTH));
            BlockPos blockPos2 = blockPos.relative(direction);
            if (this.isValid(blockPos2) && !this.isOutOfRange(blockPos2) && !SpeedMineModule.mc.level.getBlockState(blockPos2).canBeReplaced()) {
                this.handle(blockPos2, 1);
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
        int n;
        if (this.primary != null) {
            this.primary.restoreIronSwap();
        }
        if (SpeedMineModule.mc.player != null && (n = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
            int n2 = InventoryUtils.findFastestItem(Blocks.OBSIDIAN.defaultBlockState(), InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
            if (n2 == -1) {
                n2 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
            }
            if (n2 != -1 && n2 > InventoryUtils.HOTBAR_END) {
                InventoryUtils.swap("Swap", n2, n);
            }
        }
    }

    @Override
    public String getMetaData() {
        String string = this.primary == null ? "0.0" : new DecimalFormat("0.0").format(this.primary.getProgress() / this.primary.getSpeed());
        String string2 = this.secondary == null || !this.doubleMine.getValue() ? "" : ", " + new DecimalFormat("0.0").format(this.secondary.getProgress() / this.secondary.getSpeed());
        return string + string2;
    }

    private boolean canHandle(BlockPos blockPos) {
        boolean bl;
        if (SpeedMineModule.mc.gameMode.getPlayerMode() == GameType.CREATIVE || SpeedMineModule.mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return false;
        }
        if (!(SpeedMineModule.mc.level.getBlockState(blockPos).getBlock().defaultDestroyTime() != -1.0f || this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK)) {
            return false;
        }
        AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
        AutoMineModule autoMineModule2 = autoMineModule;
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
        int n;
        if (blockPos == null || player == null || SpeedMineModule.mc.level == null) {
            return false;
        }
        AABB aABB = player.getBoundingBox();
        for (int i = n = Mth.floor((double)player.getY()); i <= n + 2; ++i) {
            for (int j = Mth.floor((double)aABB.minX); j < Mth.ceil((double)aABB.maxX); ++j) {
                for (int k = Mth.floor((double)aABB.minZ); k < Mth.ceil((double)aABB.maxZ); ++k) {
                    BlockPos blockPos2 = new BlockPos(j, i, k);
                    if (blockPos.equals((Object)blockPos2)) {
                        return true;
                    }
                    if (i > n + 1) continue;
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        if (!blockPos.equals((Object)blockPos2.relative(direction))) continue;
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
        ArrayList<Player> arrayList = new ArrayList<Player>(SpeedMineModule.mc.level.players());
        if (logoutSpotModule != null && logoutSpotModule.isToggled()) {
            for (Player player : logoutSpotModule.getGhosts()) {
                if (player == null || arrayList.contains(player)) continue;
                arrayList.add(player);
            }
        }
        Target target = null;
        for (Player player : arrayList) {
            LogoutSpotModule.Spot spot;
            boolean bl;
            if (player == SpeedMineModule.mc.player || popChamsModule != null && popChamsModule.isGhost((Entity)player) || !(bl = EntityUtils.isGhost((Entity)player)) && (!player.isAlive() || player.getHealth() <= 0.0f) || SpeedMineModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)(this.range.getValue().doubleValue() + 2.0)) || (logoutSpotModule != null && logoutSpotModule.isGhost((Entity)player) ? (spot = logoutSpotModule.getSpot((RemotePlayer)player)) != null && Night.FRIEND_MANAGER.contains(spot.data.name) : Night.FRIEND_MANAGER.contains(player.getName().getString()))) continue;
            List<Position> list = this.getPositions(player);
            BlockPos blockPos = this.getTargetPosition(list);
            if (!this.doubleMine.getValue() && (list.isEmpty() || blockPos == null)) continue;
            if (target == null) {
                target = new Target(player, list, blockPos);
                continue;
            }
            boolean bl2 = EntityUtils.isGhost((Entity)target.player());
            if (bl2 != bl) {
                if (!bl2) continue;
                target = new Target(player, list, blockPos);
                continue;
            }
            if (!(SpeedMineModule.mc.player.distanceToSqr((Entity)player) < SpeedMineModule.mc.player.distanceToSqr((Entity)target.player()))) continue;
            target = new Target(player, list, blockPos);
        }
        return target;
    }

    private BlockPos getTargetPosition(List<Position> list) {
        BlockPos blockPos = null;
        double d = 0.0;
        for (Position position : list) {
            if (this.doubleMine.getValue() && !position.feetPosition() || !this.isValidPosition(position.position()) || HoleUtils.isPlayerInHole((Player)SpeedMineModule.mc.player) && HoleUtils.getFeetPositions((Player)SpeedMineModule.mc.player, true, false, true).contains(position.position())) continue;
            double d2 = 0.0;
            if (this.sixB.getValue() && SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.BEDROCK) {
                double d3 = d2 = position.feetPosition() ? (d2 = d2 + 1000.0) : (d2 = d2 - 1000.0);
            }
            if (position.feetPosition()) {
                d2 += 5.0;
                if (SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.ENDER_CHEST) {
                    d2 += 0.95;
                } else if (WorldUtils.isCrystalPlaceable(position.position().offset(0, 1, 0))) {
                    d2 += 0.35;
                }
                if (this.hasCityPosition(position.position())) {
                    d2 += 0.6;
                }
            } else {
                double d4 = SpeedMineModule.mc.level.getBlockState(position.position()).getBlock() == Blocks.ENDER_CHEST ? (d2 = d2 - 2.0) : (d2 = WorldUtils.isCrystalPlaceable(position.position().offset(0, 1, 0)) ? (d2 = d2 + 0.75) : (d2 = d2 - 2.0));
            }
            if (!(d2 >= d)) continue;
            blockPos = position.position();
            d = d2;
        }
        return blockPos;
    }

    private List<Position> getPositions(Player player) {
        ArrayList<Position> arrayList = new ArrayList<Position>();
        for (BlockPos blockPos : HoleUtils.getFeetPositions(player, true, false, true)) {
            arrayList.add(new Position(blockPos, true));
            if (this.doubleMine.getValue()) continue;
            arrayList.add(new Position(blockPos.offset(0, 1, 0), false));
        }
        if (!this.doubleMine.getValue()) {
            arrayList.add(new Position(player.blockPosition().offset(0, 2, 0), false));
        }
        return arrayList;
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
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!WorldUtils.isPlaceable(blockPos2)) continue;
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
        float f;
        if (this.proxyPrimaryPos != null) {
            f = this.interpolatedProgress(this.prevProxyPrimaryProgress, this.proxyPrimaryProgress, this.proxyPrimaryUpdateTime, this.proxyPrimaryUpdateInterval);
            this.renderProxyBlock(poseStack, this.proxyPrimaryPos, f);
        }
        if (this.proxySecondaryPos != null && this.doubleMine.getValue()) {
            f = this.interpolatedProgress(this.prevProxySecondaryProgress, this.proxySecondaryProgress, this.proxySecondaryUpdateTime, this.proxySecondaryUpdateInterval);
            this.renderProxyBlock(poseStack, this.proxySecondaryPos, f);
        }
    }

    private void renderProxyBlock(PoseStack poseStack, BlockPos blockPos, float f) {
        if (SpeedMineModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return;
        }
        AABB aABB = new AABB(blockPos);
        if (this.animation.getValue().equalsIgnoreCase("Expand")) {
            aABB = new AABB(blockPos).deflate(0.5).inflate(Mth.clamp((double)((double)f / 2.0), (double)0.0, (double)0.5));
        }
        if (this.animation.getValue().equalsIgnoreCase("Rise")) {
            aABB = new AABB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)blockPos.getX() + 1.0, (double)((float)blockPos.getY() + f), (double)blockPos.getZ() + 1.0);
        }
        Color color = this.fillColor.getColor();
        Color color2 = this.outlineColor.getColor();
        if (this.color.getValue().equalsIgnoreCase("Static")) {
            color = (double)f >= 0.9 ? new Color(0, 255, 0, this.fillColor.getAlpha()) : new Color(255, 0, 0, this.fillColor.getAlpha());
            color2 = (double)f >= 0.9 ? new Color(0, 255, 0, this.outlineColor.getAlpha()) : new Color(255, 0, 0, this.outlineColor.getAlpha());
        } else if (this.color.getValue().equalsIgnoreCase("Smooth")) {
            color = new Color(255 - (int)(Mth.clamp((float)f, (float)0.0f, (float)1.0f) * 255.0f), (int)(Mth.clamp((float)f, (float)0.0f, (float)1.0f) * 255.0f), 0, this.fillColor.getAlpha());
            color2 = new Color(255 - (int)(Mth.clamp((float)f, (float)0.0f, (float)1.0f) * 255.0f), (int)(Mth.clamp((float)f, (float)0.0f, (float)1.0f) * 255.0f), 0, this.outlineColor.getAlpha());
        }
        if (this.render.getValue().equalsIgnoreCase("Fill") || this.render.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBox(poseStack, aABB, color);
        }
        if (this.render.getValue().equalsIgnoreCase("Outline") || this.render.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBoxOutline(poseStack, aABB, color2);
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
            NetworkUtils.sendSequencedPacket(n -> {
                Packet packet = (Packet)intFunction.apply(n);
                return packet;
            });
        }
    }

    private void sendRawPlayerAction(ServerboundPlayerActionPacket.Action action, BlockPos blockPos, Direction direction) {
        this.sendRawPlayerAction(action, blockPos, direction, false);
    }

    private void sendRawPlayerAction(ServerboundPlayerActionPacket.Action action, BlockPos blockPos, Direction direction, boolean bl) {
        if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            this.markStop(bl);
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

        public Action(SpeedMineModule speedMineModule, SpeedMineModule speedMineModule2, BlockPos blockPos, int n) {
            SpeedMineModule speedMineModule3 = speedMineModule;
            Objects.requireNonNull(speedMineModule3);
            this.this$0$ = speedMineModule3;
            SpeedMineModule speedMineModule4 = speedMineModule2;
            Objects.requireNonNull(speedMineModule4);
            this.this$0 = speedMineModule4;
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
            this.priority = n;
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

        public void setTerrainBase(boolean bl) {
            this.terrainBase = bl;
        }

        public boolean isTerrainSurround() {
            return this.terrainSurround;
        }

        public void setTerrainSurround(boolean bl) {
            this.terrainSurround = bl;
        }

        public boolean isStarted() {
            return this.started;
        }

        private int getBreakSlot(BlockState blockState, int n) {
            int n2;
            AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            AutoMineModule autoMineModule2 = autoMineModule;
            if (autoMineModule != null && autoMineModule.isToggled() && autoMineModule.ironPickaxe.getValue() && this.ironSwapped && (n2 = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
                return n2;
            }
            return n;
        }

        private long asyncFireDelayMs() {
            long l = this.this$0.instantDelay.getValue().longValue() * 50L;
            if (this.position == null) {
                return l;
            }
            Target target = this.this$0.getTarget();
            if (target == null || target.player() == null) {
                return l;
            }
            boolean bl = this.position.getY() == Mth.floor((double)target.player().getY()) + 2 && this.this$0.isTargetSurroundPosition(this.position, target.player());
            boolean bl2 = bl;
            if (!bl) {
                return l;
            }
            AutoTrapModule autoTrapModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoTrapModule.class) : null;
            AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            boolean bl3 = autoTrapModule != null && autoTrapModule.isToggled();
            boolean bl4 = autoMineModule != null && autoMineModule.isToggled() && autoMineModule.priority.getValue().equalsIgnoreCase("Head");
            boolean bl5 = bl4;
            if (bl3 && bl4) {
                return Math.max(l, (long)Night.SERVER_MANAGER.getPing());
            }
            return l;
        }

        private void updateIronGhost() {
            int n;
            AutoMineModule autoMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoMineModule.class) : null;
            boolean bl = autoMineModule != null && autoMineModule.isToggled() && autoMineModule.ironPickaxe.getValue();
            boolean bl2 = bl;
            if (bl && !this.ironSwapped && (n = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END)) != -1 && this.state != null && !this.state.isAir()) {
                float f = WorldUtils.getMineSpeed(this.state, n) / Night.WORLD_MANAGER.getTimerMultiplier();
                this.ghostIronProgress = Mth.clamp((float)(this.ghostIronProgress + f), (float)0.0f, (float)this.getSpeed());
                if (this.ghostIronProgress >= this.getSpeed()) {
                    int n2 = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
                    if (n2 == -1) {
                        n2 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                    }
                    if (n > InventoryUtils.HOTBAR_END) {
                        InventoryUtils.swap("Swap", n, n2);
                        this.ironSwapped = true;
                        this.swappedIronInventorySlot = n;
                        this.swappedIronHotbarSlot = n2;
                    } else if (n != -1) {
                        this.ironSwapped = true;
                        this.swappedIronInventorySlot = -1;
                        this.swappedIronHotbarSlot = n;
                    }
                }
            }
        }

        public void restoreIronSwap() {
            if (this.ironSwapped) {
                int n;
                if (this.swappedIronInventorySlot > InventoryUtils.HOTBAR_END && this.swappedIronHotbarSlot >= 0 && this.swappedIronHotbarSlot <= InventoryUtils.HOTBAR_END) {
                    if (IMinecraft.mc.player != null && IMinecraft.mc.player.getInventory().getItem(this.swappedIronHotbarSlot).is(Items.IRON_PICKAXE)) {
                        InventoryUtils.swap("Swap", this.swappedIronInventorySlot, this.swappedIronHotbarSlot);
                    }
                } else if (IMinecraft.mc.player != null && (n = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
                    BlockState blockState = this.state != null && !this.state.isAir() ? this.state : Blocks.OBSIDIAN.defaultBlockState();
                    int n2 = this.swappedIronInventorySlot > InventoryUtils.HOTBAR_END ? this.swappedIronInventorySlot : InventoryUtils.findFastestItem(blockState, InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
                    int n3 = n2;
                    if (n2 == -1) {
                        n2 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_END + 1, InventoryUtils.INVENTORY_END);
                    }
                    if (n2 != -1 && n2 > InventoryUtils.HOTBAR_END) {
                        InventoryUtils.swap("Swap", n2, n);
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
            Object object;
            Object object2 = object = this.this$0.interactSyncLock;
            synchronized (object2) {
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
            boolean bl = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
            boolean bl2 = bl;
            if (this.this$0.interactPaused && !bl && System.currentTimeMillis() - this.this$0.interactPausedAt >= 750L) {
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
                    long l = this.this$0.instantTimeout.getValue().longValue();
                    boolean bl3 = l > 0L && this.this$0.instantTimer.hasTimeElapsed(l * 50L);
                    boolean bl4 = bl3;
                    if (!bl3) {
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
                float f = this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK ? 0.025f : WorldUtils.getMineSpeed(this.state, miningSlot) / Night.WORLD_MANAGER.getTimerMultiplier();
                float f2 = f;
                if (this.instantMine) {
                    this.prevProgress = this.getSpeed();
                    this.progress = this.getSpeed();
                } else {
                    this.prevProgress = this.progress;
                    this.progress = Mth.clamp((float)(this.progress + f), (float)0.0f, (float)this.getSpeed());
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    } else {
                        IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                if (this.this$0.rotate.getValue().equalsIgnoreCase("Normal") && this.progress + f * 2.0f >= this.getSpeed()) {
                    float[] fArray = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.PosRot(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY(), IMinecraft.mc.player.getZ(), fArray[0], fArray[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                    } else {
                        Night.ROTATION_MANAGER.legacyRotate(fArray, Night.ROTATION_MANAGER.getLegacyModulePriority(this.this$0));
                    }
                }
                boolean bl5 = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
                boolean bl6 = this.this$0.interactPaused || !this.this$0.whileEating.getValue() && bl5;
                boolean bl7 = bl6;
                if (this.progress >= this.getSpeed() && !blockState.canBeReplaced() && !bl6) {
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
                    long l = this.this$0.instantTimeout.getValue().longValue();
                    boolean bl = l > 0L && this.this$0.instantTimer.hasTimeElapsed(l * 50L);
                    boolean bl2 = bl;
                    if (!bl) {
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
                float f = this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK ? 0.025f : WorldUtils.getMineSpeed(this.state, miningSlot) / Night.WORLD_MANAGER.getTimerMultiplier();
                float f2 = f;
                if (this.instantMine) {
                    this.prevProgress = this.getSpeed();
                    this.progress = this.getSpeed();
                } else {
                    this.prevProgress = this.progress;
                    this.progress = Mth.clamp((float)(this.progress + f), (float)0.0f, (float)this.getSpeed());
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
                if (this.this$0.rotate.getValue().equalsIgnoreCase("Normal") && this.progress + f * 2.0f >= this.getSpeed()) {
                    float[] fArray = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.PosRot(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY(), IMinecraft.mc.player.getZ(), fArray[0], fArray[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                    } else {
                        Night.ROTATION_MANAGER.legacyRotate(fArray, Night.ROTATION_MANAGER.getLegacyModulePriority(this.this$0));
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
            AutoTrapModule autoTrapModule2 = autoTrapModule;
            if (autoTrapModule == null || !autoTrapModule.isToggled()) {
                return false;
            }
            AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoCrystalModule.class) : null;
            AutoCrystalModule autoCrystalModule2 = autoCrystalModule;
            if (autoCrystalModule == null || !autoCrystalModule.isToggled()) {
                return false;
            }
            Target target = this.this$0.getTarget();
            if (target == null || target.player() == null) {
                return false;
            }
            int n = Mth.floor((double)target.player().getY());
            return this.position.getY() >= n + 2 && Math.abs((double)this.position.getX() + 0.5 - target.player().getX()) < 1.3 && Math.abs((double)this.position.getZ() + 0.5 - target.player().getZ()) < 1.3;
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
            AutoTrapModule autoTrapModule2 = autoTrapModule;
            if (autoTrapModule == null || !autoTrapModule.isToggled()) {
                return;
            }
            AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(AutoCrystalModule.class) : null;
            AutoCrystalModule autoCrystalModule2 = autoCrystalModule;
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
            int n2 = Mth.floor((double)target.player().getY());
            boolean bl = blockPos.getY() >= n2 + 2 && Math.abs((double)blockPos.getX() + 0.5 - target.player().getX()) < 1.3 && Math.abs((double)blockPos.getZ() + 0.5 - target.player().getZ()) < 1.3;
            boolean bl2 = bl;
            if (!bl) {
                return;
            }
            BlockPos blockPos2 = blockPos.above();
            if (!WorldUtils.isCrystalPlaceable(blockPos2)) {
                return;
            }
            int n3 = InventoryUtils.findHotbar(Items.END_CRYSTAL);
            boolean bl3 = IMinecraft.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            boolean bl4 = bl3;
            if (!bl3 && n3 == -1) {
                return;
            }
            InteractionHand interactionHand = bl3 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            Vec3 vec3 = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5);
            BlockHitResult blockHitResult = new BlockHitResult(vec3, Direction.UP, blockPos, false);
            if (this.this$0.isProxyActive()) {
                if (bl3) {
                    this.this$0.serverSendSequenced(n -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, n));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(interactionHand));
                } else {
                    int n4 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n3));
                    this.this$0.serverSendSequenced(n -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, n));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(interactionHand));
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n4));
                }
            } else if (bl3) {
                NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, n));
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(interactionHand));
            } else {
                int n5 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.switchSlot("Silent", n3, n5);
                NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemOnPacket(interactionHand, blockHitResult, n));
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(interactionHand));
                InventoryUtils.switchBack("Silent", n3, n5);
            }
        }

        private void legacyFireBreakBurst(Direction direction, int n) {
            this.checkAndPlaceHeadCrystal(this.position);
            Night.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
            if (this.this$0.rotate.getValue().equalsIgnoreCase("Silent")) {
                float[] fArray = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.Rot(fArray[0], fArray[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                } else {
                    Night.ROTATION_MANAGER.wireRotate("Silent", fArray);
                }
            }
            int n2 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            if (this.this$0.isProxyActive()) {
                int n3 = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : n;
                boolean bl = n3 != -1 && n3 != n2;
                boolean bl2 = bl;
                if (bl) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n3));
                }
                this.stopDestroyBlock(this.position, direction, true);
                if (this.this$0.grim.getValue()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (bl) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n2));
                }
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), n, n2);
                this.stopDestroyBlock(this.position, direction, true);
                if (this.this$0.grim.getValue()) {
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), n, n2);
            }
        }

        private void fireBreakBurst(Direction direction, int n, boolean bl) {
            this.fireBreakBurst(direction, n, bl, !bl);
        }

        private void fireBreakBurst(Direction direction, int n, boolean bl, boolean bl2) {
            if (!bl) {
                this.checkAndPlaceHeadCrystal(this.position);
                Night.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
            }
            if (this.this$0.rotate.getValue().equalsIgnoreCase("Silent")) {
                float[] fArray = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, direction));
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundMovePlayerPacket.Rot(fArray[0], fArray[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                } else {
                    Night.ROTATION_MANAGER.wireRotate("Silent", fArray);
                }
            }
            int n2 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            if (this.this$0.isProxyActive()) {
                int n3 = this.startSlot != -1 ? this.startSlot : n2;
                int n4 = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : n;
                int n5 = n4;
                if (n4 != -1) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n4));
                    if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal")) {
                        IMinecraft.mc.player.getInventory().setSelectedSlot(n4);
                    }
                }
                this.stopDestroyBlock(this.position, direction, !bl, bl2);
                if (this.this$0.grim.getValue()) {
                    this.this$0.serverSend((Packet<?>)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                if (n4 != -1) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n3));
                    if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal")) {
                        IMinecraft.mc.player.getInventory().setSelectedSlot(n3);
                    }
                }
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), n, n2);
                this.stopDestroyBlock(this.position, direction, !bl, bl2);
                if (this.this$0.grim.getValue()) {
                    IMinecraft.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), direction));
                }
                IMinecraft.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), n, n2);
            }
        }

        private void stopDestroyBlock(BlockPos blockPos, Direction direction, boolean bl) {
            this.stopDestroyBlock(blockPos, direction, bl, false);
        }

        private void stopDestroyBlock(BlockPos blockPos, Direction direction, boolean bl, boolean bl2) {
            try (BlockStatePredictionHandler blockStatePredictionHandler = ((ClientWorldAccessor)IMinecraft.mc.level).invokeGetPendingUpdateManager().startPredicting();){
                ServerboundPlayerActionPacket serverboundPlayerActionPacket = new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction, blockStatePredictionHandler.currentSequence());
                this.this$0.markStop(bl2);
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
            boolean bl = IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() && this.instantMine;
            double d = bl ? (double)(this.progress / this.getSpeed()) : (double)Mth.clamp((float)Mth.lerp((float)IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), (float)(this.prevProgress / this.getSpeed()), (float)(this.progress / this.getSpeed())), (float)0.0f, (float)1.0f);
            double d2 = d;
            if (this.this$0.animation.getValue().equalsIgnoreCase("Expand")) {
                aABB = new AABB(this.position).deflate(0.5).inflate(Mth.clamp((double)(d / 2.0), (double)0.0, (double)0.5));
            }
            if (this.this$0.animation.getValue().equalsIgnoreCase("Rise")) {
                aABB = new AABB((double)this.position.getX(), (double)this.position.getY(), (double)this.position.getZ(), (double)this.position.getX() + 1.0, (double)this.position.getY() + d, (double)this.position.getZ() + 1.0);
            }
            Color color = this.this$0.fillColor.getColor();
            Color color2 = this.this$0.outlineColor.getColor();
            if (this.this$0.color.getValue().equalsIgnoreCase("Static")) {
                color = d >= 0.9 ? new Color(0, 255, 0, this.this$0.fillColor.getAlpha()) : new Color(255, 0, 0, this.this$0.fillColor.getAlpha());
                color2 = d >= 0.9 ? new Color(0, 255, 0, this.this$0.outlineColor.getAlpha()) : new Color(255, 0, 0, this.this$0.outlineColor.getAlpha());
            } else if (this.this$0.color.getValue().equalsIgnoreCase("Smooth")) {
                color = new Color(255 - (int)(Mth.clamp((double)d, (double)0.0, (double)1.0) * 255.0), (int)(Mth.clamp((double)d, (double)0.0, (double)1.0) * 255.0), 0, this.this$0.fillColor.getAlpha());
                color2 = new Color(255 - (int)(Mth.clamp((double)d, (double)0.0, (double)1.0) * 255.0), (int)(Mth.clamp((double)d, (double)0.0, (double)1.0) * 255.0), 0, this.this$0.outlineColor.getAlpha());
            }
            if (d >= (double)this.getSpeed() && this.instantMine && this.this$0.instantRender.getValue().equalsIgnoreCase("Custom")) {
                color = ColorUtils.getColor(this.this$0.instantColor.getColor(), this.this$0.fillColor.getAlpha());
                color2 = ColorUtils.getColor(this.this$0.instantColor.getColor(), this.this$0.outlineColor.getAlpha());
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Fill") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(poseStack, aABB, color);
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Outline") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(poseStack, aABB, color2);
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
                    BlockPos blockPos = this.grimDecoyPos();
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, WorldUtils.getClosestDirection(blockPos, true));
                } else {
                    this.this$0.serverSendSequenced(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, n));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction);
                    BlockPos blockPos = this.grimDecoyPos();
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, WorldUtils.getClosestDirection(blockPos, true));
                } else {
                    NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, n));
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
                int n2 = this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap") || this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                boolean bl = n2 != -1 && n2 != IMinecraft.mc.player.getInventory().getSelectedSlot();
                boolean bl2 = bl;
                if (bl) {
                    this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n2));
                }
                if (this.this$0.doubleMine.getValue()) {
                    this.this$0.serverSendSequenced(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, n));
                    this.this$0.serverSendSequenced(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, n));
                    this.this$0.serverSendSequenced(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, n));
                } else {
                    this.this$0.serverSendSequenced(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, n));
                }
                this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                if (this.this$0.doubleMine.getValue()) {
                    NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, n));
                    NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, n));
                    NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, direction, n));
                } else {
                    NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, direction, n));
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
                    this.this$0.serverSendSequenced(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true), n));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else {
                    NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true), n));
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
                    this.this$0.serverSendSequenced(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, direction, n));
                    this.this$0.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else {
                    NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, direction, n));
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
            float f;
            if (!this.mining) {
                return Integer.MAX_VALUE;
            }
            if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                float f2 = 0.025f;
                return Math.max(0, Math.round((this.getSpeed() - this.progress) / f2));
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
        public void setProgress(float f) {
            this.progress = f;
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

        private Secondary(SpeedMineModule speedMineModule, SpeedMineModule speedMineModule2, BlockPos blockPos, int n, BlockState blockState, float f) {
            Objects.requireNonNull(speedMineModule);
            SpeedMineModule speedMineModule3 = speedMineModule2;
            Objects.requireNonNull(speedMineModule3);
            this.this$0 = speedMineModule3;
            this.position = blockPos;
            this.priority = n;
            this.state = blockState;
            this.progress = f;
            this.prevProgress = f;
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
            float f;
            BlockState blockState;
            if (this.this$0.isOutOfRange(this.position)) {
                this.release();
                return true;
            }
            boolean bl = IMinecraft.mc.player != null && (IMinecraft.mc.player.isUsingItem() || EntityUtils.isEating());
            boolean bl2 = bl;
            if (this.this$0.interactPaused && !bl && System.currentTimeMillis() - this.this$0.interactPausedAt >= 750L) {
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
            boolean bl3 = this.this$0.whileEating.getValue() || !bl || this.this$0.switchMode.getValue().equalsIgnoreCase("None");
            boolean bl4 = bl3;
            if (!this.holding && this.progress + speed >= 1.0f && bl3) {
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

        private void hold(int n) {
            if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                return;
            }
            int n2 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            this.holding = true;
            if (n2 == n) {
                return;
            }
            this.this$0.secondaryOriginalSlot = n2;
            this.this$0.secondaryHoldSlot = n;
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n));
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), n, n2);
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
            int n = this.this$0.secondaryOriginalSlot;
            int n2 = this.this$0.secondaryHoldSlot;
            this.this$0.secondaryHoldSlot = -1;
            this.this$0.secondaryOriginalSlot = -1;
            if (IMinecraft.mc.player == null) {
                return;
            }
            if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal") && IMinecraft.mc.player.getInventory().getSelectedSlot() == n) {
                return;
            }
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend((Packet<?>)new ServerboundSetCarriedItemPacket(n));
            } else {
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), n2, n);
            }
        }

        public void render(PoseStack poseStack) {
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                return;
            }
            double d = Mth.clamp((float)Mth.lerp((float)IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), (float)this.prevProgress, (float)this.progress), (float)0.0f, (float)1.0f);
            AABB aABB = new AABB(this.position);
            if (this.this$0.animation.getValue().equalsIgnoreCase("Expand")) {
                aABB = new AABB(this.position).deflate(0.5).inflate(Mth.clamp((double)(d / 2.0), (double)0.0, (double)0.5));
            }
            if (this.this$0.animation.getValue().equalsIgnoreCase("Rise")) {
                aABB = new AABB((double)this.position.getX(), (double)this.position.getY(), (double)this.position.getZ(), (double)this.position.getX() + 1.0, (double)this.position.getY() + d, (double)this.position.getZ() + 1.0);
            }
            Color color = this.this$0.fillColor.getColor();
            Color color2 = this.this$0.outlineColor.getColor();
            if (this.this$0.color.getValue().equalsIgnoreCase("Static")) {
                color = d >= 0.9 ? new Color(0, 255, 0, this.this$0.fillColor.getAlpha()) : new Color(255, 0, 0, this.this$0.fillColor.getAlpha());
                color2 = d >= 0.9 ? new Color(0, 255, 0, this.this$0.outlineColor.getAlpha()) : new Color(255, 0, 0, this.this$0.outlineColor.getAlpha());
            } else if (this.this$0.color.getValue().equalsIgnoreCase("Smooth")) {
                color = new Color(255 - (int)(d * 255.0), (int)(d * 255.0), 0, this.this$0.fillColor.getAlpha());
                color2 = new Color(255 - (int)(d * 255.0), (int)(d * 255.0), 0, this.this$0.outlineColor.getAlpha());
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Fill") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(poseStack, aABB, color);
            }
            if (this.this$0.render.getValue().equalsIgnoreCase("Outline") || this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(poseStack, aABB, color2);
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

