/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  lightning.mixins.accessors.PlayerListS2CPacketAccessor
 *  lombok.Generated
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action
 *  net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry
 *  net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
 *  net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.PositionMoveRotation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 */
package lightning.modules.impl.miscellaneous;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.PlayerListS2CPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

@RegisterModule(name="FakePlayer", description="Spawns in a fake player entity that you can use to test modules on.", category=Module.Category.MISCELLANEOUS, proxyEnhanced=true)
public class FakePlayerModule
extends Module {
    public StringSetting name = new StringSetting("Name", "The name that will be assigned to the fake player.", "Dummy");
    public NumberSetting health = new NumberSetting("Health", "The amount of health that will be assigned to the fake player.", Float.valueOf(20.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public NumberSetting absorption = new NumberSetting("Absorption", "The amount of absorption that will be assigned to the fake player.", 16, 0, 16);
    public CategorySetting movementCategory = new CategorySetting("Movement", "The category that contains settings related to movement.");
    public ModeSetting movementMode = new ModeSetting("Movement", "Mode", "The mode that will be used for the fake player's movement.", new CategorySetting.Visibility(this.movementCategory), "None", new String[]{"None", "Random"});
    public NumberSetting velocity = new NumberSetting("Velocity", "Velocity", "The velocity to apply on the fake player.", new CategorySetting.Visibility(this.movementCategory), Float.valueOf(0.3f), Float.valueOf(0.1f), Float.valueOf(0.4f));
    public BooleanSetting changeDirection = new BooleanSetting("ChangeDirection", "Changes the player's direction when a specified amount of time has passed.", new CategorySetting.Visibility(this.movementCategory), false);
    public NumberSetting timeout = new NumberSetting("Timeout", "The amount of time that it takes for the player to change direction.", new BooleanSetting.Visibility(this.changeDirection, true), (Number)5, (Number)1, (Number)15);
    private RemotePlayer player = null;
    private double[] direction = this.generateDirection();
    private final Timer timer = new Timer();
    private boolean stepping = false;
    private final Timer stepTimer = new Timer();
    private RecordState recordState = RecordState.NONE;
    private final List<RecordFrame> frames = new ArrayList<RecordFrame>();
    private int playIndex = 0;

    public void startRecording() {
        if (this.player == null) {
            Night.CHAT_MANAGER.warn("Spawn a fake player first before recording!");
            return;
        }
        this.frames.clear();
        this.recordState = RecordState.RECORDING;
        Night.CHAT_MANAGER.tagged("Started recording movements...", "FakePlayer");
    }

    public void stopRecording() {
        this.recordState = RecordState.NONE;
        Night.CHAT_MANAGER.tagged("Record!", "FakePlayer");
    }

    public void startPlaying() {
        if (this.frames.isEmpty()) {
            Night.CHAT_MANAGER.warn("No recorded frames to play! Use '.fakeplayer record' first.");
            return;
        }
        if (this.player == null) {
            this.setToggled(true);
        }
        this.recordState = RecordState.PLAYING;
        this.playIndex = 0;
        Night.CHAT_MANAGER.tagged("Playing recorded movements in a loop...", "FakePlayer");
    }

    @SubscribeEvent
    public void onDisconnect(ClientConnectEvent clientConnectEvent) {
        if (this.isToggled()) {
            this.setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (playerDeathEvent.getPlayer() != FakePlayerModule.mc.player) {
            return;
        }
        if (this.isToggled()) {
            this.setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        Entity entity;
        ClientboundEntityEventPacket clientboundEntityEventPacket;
        if (FakePlayerModule.mc.level == null || FakePlayerModule.mc.player == null) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundEntityEventPacket && (clientboundEntityEventPacket = (ClientboundEntityEventPacket)packet).getEventId() == 3 && (entity = clientboundEntityEventPacket.getEntity((Level)FakePlayerModule.mc.level)) == FakePlayerModule.mc.player && this.isToggled()) {
            this.setToggled(false);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (FakePlayerModule.mc.player != null && (FakePlayerModule.mc.player.isDeadOrDying() || FakePlayerModule.mc.player.getHealth() <= 0.0f || !FakePlayerModule.mc.player.isAlive() || FakePlayerModule.mc.gui != null && FakePlayerModule.mc.gui.screen() instanceof DeathScreen) && this.isToggled()) {
            this.setToggled(false);
        }
    }

    /*
     * WARNING - void declaration
     */
    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (FakePlayerModule.mc.player == null || FakePlayerModule.mc.level == null) {
            return;
        }
        if (this.recordState == RecordState.RECORDING) {
            this.frames.add(new RecordFrame(FakePlayerModule.mc.player.getX(), FakePlayerModule.mc.player.getY(), FakePlayerModule.mc.player.getZ(), FakePlayerModule.mc.player.getYRot(), FakePlayerModule.mc.player.getXRot(), FakePlayerModule.mc.player.getYHeadRot(), FakePlayerModule.mc.player.getPose()));
            return;
        }
        if (this.recordState == RecordState.PLAYING) {
            if (this.player == null || this.frames.isEmpty()) {
                return;
            }
            ++this.playIndex;
            if (this.playIndex >= this.frames.size()) {
                this.playIndex = 0;
            }
            this.broadcastPosition();
            return;
        }
        if (this.player == null) {
            return;
        }
        if (!this.movementMode.getValue().equalsIgnoreCase("Random")) {
            return;
        }
        BlockPos blockPos = this.player.blockPosition();
        boolean bl = false;
        if (this.changeDirection.getValue() && this.timer.hasTimeElapsed(this.timeout.getValue().longValue() * 1000L)) {
            bl = true;
            this.timer.reset();
        }
        if (this.hasObstruction(blockPos)) {
            for (Direction direction : Direction.values()) {
                BlockPos blockPos2 = blockPos.relative(direction);
                if (!WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos2)) && !WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos2.above())) || !WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos2.above().above())) || !this.player.getDirection().equals((Object)direction)) continue;
                bl = true;
                this.timer.reset();
            }
        }
        if (bl) {
            double[] dArray = this.generateDirection();
            if (this.direction == dArray) {
                dArray = this.generateDirection();
            }
            this.direction = dArray;
        }
        if (this.stepping && this.stepTimer.hasTimeElapsed(500L)) {
            this.stepping = false;
            this.stepTimer.reset();
        }
        this.player.jumpFromGround();
        float[] fArray = RotationUtils.getRotations((Entity)this.player, this.player.getX() + this.direction[0], this.player.getY() + (double)this.fixAxisY((Player)this.player), this.player.getZ() + this.direction[1]);
        this.player.setYRot(fArray[0]);
        this.player.yHeadRot = fArray[0];
        this.player.setXRot(0.0f);
        this.player.setPos(this.player.getX() + this.direction[0], this.player.getY() + (double)this.fixAxisY((Player)this.player), this.player.getZ() + this.direction[1]);
        this.broadcastPosition();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (this.recordState != RecordState.PLAYING || this.player == null || this.frames.isEmpty()) {
            return;
        }
        int n = this.playIndex;
        int n2 = (n + 1) % this.frames.size();
        RecordFrame recordFrame = this.frames.get(n);
        RecordFrame recordFrame2 = this.frames.get(n2);
        float f = renderWorldEvent.getTickDelta();
        if (n2 == 0) {
            f = 0.0f;
        }
        double d = Mth.lerp((double)f, (double)recordFrame.x(), (double)recordFrame2.x());
        double d2 = Mth.lerp((double)f, (double)recordFrame.y(), (double)recordFrame2.y());
        double d3 = Mth.lerp((double)f, (double)recordFrame.z(), (double)recordFrame2.z());
        float f2 = Mth.rotLerp((float)f, (float)recordFrame.yRot(), (float)recordFrame2.yRot());
        float f3 = Mth.lerp((float)f, (float)recordFrame.xRot(), (float)recordFrame2.xRot());
        float f4 = Mth.rotLerp((float)f, (float)recordFrame.yHeadRot(), (float)recordFrame2.yHeadRot());
        this.player.setPos(d, d2, d3);
        this.player.setYRot(f2);
        this.player.setXRot(f3);
        this.player.yHeadRot = f4;
        this.player.yBodyRot = f2;
        this.player.setOldPosAndRot();
        this.player.yHeadRotO = f4;
        this.player.yBodyRotO = f2;
        this.player.setPose(recordFrame.pose());
        this.player.refreshDimensions();
    }

    private void broadcastPosition() {
        if (!this.isRunningOnProxy() || Night.PROXY_SERVER == null) {
            return;
        }
        if (this.player == null) {
            return;
        }
        ClientboundTeleportEntityPacket clientboundTeleportEntityPacket = ClientboundTeleportEntityPacket.teleport((int)this.player.getId(), (PositionMoveRotation)PositionMoveRotation.of((Entity)this.player), Set.of(), (boolean)this.player.onGround());
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundTeleportEntityPacket);
        }
    }

    @Override
    public void onEnable() {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (FakePlayerModule.mc.level == null || FakePlayerModule.mc.player == null) {
            this.setToggled(false);
            return;
        }
        this.player = new RemotePlayer(FakePlayerModule.mc.level, new GameProfile(UUID.randomUUID(), this.name.getValue()));
        this.player.copyPosition((Entity)FakePlayerModule.mc.player);
        this.player.setId(-673);
        this.player.restoreFrom((Entity)FakePlayerModule.mc.player);
        this.player.setHealth(this.health.getValue().floatValue());
        this.player.setAbsorptionAmount(this.absorption.getValue().floatValue());
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            this.player.setItemSlot(equipmentSlot, FakePlayerModule.mc.player.getItemBySlot(equipmentSlot).copy());
        }
        FakePlayerModule.mc.level.addEntity((Entity)this.player);
        this.player.tick();
        this.timer.reset();
        this.broadcastSpawn();
        Night.CHAT_MANAGER.tagged("Spawned fake player " + this.name.getValue(), "FakePlayer");
    }

    @Override
    public void onDisable() {
        this.recordState = RecordState.NONE;
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (this.player != null) {
            this.broadcastDespawn();
            if (FakePlayerModule.mc.level != null) {
                FakePlayerModule.mc.level.removeEntity(this.player.getId(), Entity.RemovalReason.DISCARDED);
            }
            this.player = null;
            Night.CHAT_MANAGER.tagged("Removed fake player.", "FakePlayer");
        }
    }

    private void broadcastSpawn() {
        if (!this.isRunningOnProxy() || Night.PROXY_SERVER == null) {
            return;
        }
        if (this.player == null) {
            return;
        }
        List<ClientboundPlayerInfoUpdatePacket.Entry> list = List.of(new ClientboundPlayerInfoUpdatePacket.Entry(this.player.getUUID(), this.player.getGameProfile(), false, 0, GameType.SURVIVAL, null, false, 0, null));
        ClientboundPlayerInfoUpdatePacket clientboundPlayerInfoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), Collections.emptyList());
        ((PlayerListS2CPacketAccessor)clientboundPlayerInfoUpdatePacket).setActions(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER));
        ((PlayerListS2CPacketAccessor)clientboundPlayerInfoUpdatePacket).setEntries(list);
        ClientboundAddEntityPacket clientboundAddEntityPacket = new ClientboundAddEntityPacket(this.player.getId(), this.player.getUUID(), this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getXRot(), this.player.getYRot(), this.player.getType(), 0, this.player.getDeltaMovement(), (double)this.player.getYHeadRot());
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundPlayerInfoUpdatePacket);
            connection.send((Packet)clientboundAddEntityPacket);
        }
    }

    private void broadcastDespawn() {
        if (!this.isRunningOnProxy() || Night.PROXY_SERVER == null) {
            return;
        }
        if (this.player == null) {
            return;
        }
        ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket = new ClientboundRemoveEntitiesPacket(new int[]{this.player.getId()});
        ClientboundPlayerInfoRemovePacket clientboundPlayerInfoRemovePacket = new ClientboundPlayerInfoRemovePacket(List.of(this.player.getUUID()));
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundRemoveEntitiesPacket);
            connection.send((Packet)clientboundPlayerInfoRemovePacket);
        }
    }

    public boolean hasObstruction(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos2))) {
                return true;
            }
            if (!WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos2.above()))) continue;
            return true;
        }
        return false;
    }

    public float fixAxisY(Player player) {
        if (FakePlayerModule.mc.level.getBlockState(player.blockPosition().below()).getBlock() == Blocks.AIR && !this.stepping) {
            return -1.0f;
        }
        if (this.hasObstruction(player.blockPosition())) {
            for (Direction direction : Direction.values()) {
                BlockPos blockPos = player.blockPosition().relative(direction);
                if (WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos)) && !WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos.above())) && player.getDirection().equals((Object)direction) && !WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos.above().above()))) {
                    this.stepping = true;
                    this.stepTimer.reset();
                    return 1.0f;
                }
                if (!WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos.above())) || !player.getDirection().equals((Object)direction) || WorldUtils.blocksMovement(FakePlayerModule.mc.level.getBlockState(blockPos.above().above()))) continue;
                this.stepping = true;
                this.stepTimer.reset();
                return 2.0f;
            }
            return 0.0f;
        }
        return 0.0f;
    }

    public double[] generateDirection() {
        double d = MathUtils.random(Math.PI * 2, 0.0);
        double[] dArray = new double[]{-Math.sin(d), Math.cos(d)};
        return new double[]{dArray[0] * (double)this.velocity.getValue().floatValue(), dArray[1] * (double)this.velocity.getValue().floatValue()};
    }

    @Generated
    public RemotePlayer getPlayer() {
        return this.player;
    }

    @Generated
    public RecordState getRecordState() {
        return this.recordState;
    }

    private static enum RecordState {
        NONE,
        RECORDING,
        PLAYING;

    }

    public record RecordFrame(double x, double y, double z, float yRot, float xRot, float yHeadRot, Pose pose) {
    }
}

