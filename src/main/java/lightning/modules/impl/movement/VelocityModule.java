/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.Vec3dAccessor
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundExplodePacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package lightning.modules.impl.movement;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.Vec3dAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@RegisterModule(name="Velocity", description="Modifies the amount of knockback that you receive.", category=Module.Category.MOVEMENT)
public class VelocityModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used to achieve the knockback modification.", "Normal", new String[]{"Normal", "Walls", "Cancel", "Grim"});
    public NumberSetting horizontal = new NumberSetting("Horizontal", "The amount of horizontal knockback that you will receive.", new ModeSetting.Visibility(this.mode, "Normal", "Walls"), (Number)0, (Number)0, (Number)100);
    public NumberSetting vertical = new NumberSetting("Vertical", "The amount of vertical knockback that you will receive.", new ModeSetting.Visibility(this.mode, "Normal", "Walls"), (Number)0, (Number)0, (Number)100);
    public BooleanSetting onlyOnGround = new BooleanSetting("OnlyOnGround", "Only reduces knockback while phased AND on the ground.", new ModeSetting.Visibility(this.mode, "Walls"), false);
    public BooleanSetting explosions = new BooleanSetting("Explosions", "Modifies knockback received from explosions.", true);
    public BooleanSetting pause = new BooleanSetting("Pause", "Pauses the velocity for a certain duration whenever you get rubberbanded.", new ModeSetting.Visibility(this.mode, "Cancel", "Grim"), true);
    public BooleanSetting onlyPhased = new BooleanSetting("OnlyPhased", "Only reduces knockback while phased (Grim mode).", new ModeSetting.Visibility(this.mode, "Grim"), false);
    public BooleanSetting onlyWhenHeadCovered = new BooleanSetting("OnlyCoveredHead", "Only reduces knockback when a block is above your head (Grim mode).", new ModeSetting.Visibility(this.mode, "Grim"), false);
    public NumberSetting fallDistance = new NumberSetting("FallDistance", "Pause velocity if falling from more than this distance in blocks (0 to disable).", 10.0, 0.0, 50.0);
    public BooleanSetting conceal = new BooleanSetting("Conceal", "Ignores fake 0-0-0 velocity packets sent after a setback.", false);
    public CategorySetting antiPushCategory = new CategorySetting("AntiPush", "Prevents certain things from pushing you.");
    public BooleanSetting antiPush = new BooleanSetting("AntiPush", "Entities", "Prevents other entities from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), true);
    public BooleanSetting antiLiquidPush = new BooleanSetting("AntiLiquidPush", "Liquids", "Prevents liquids from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), false);
    public BooleanSetting antiBlockPush = new BooleanSetting("AntiBlockPush", "Blocks", "Prevents you from being pushed outside of blocks.", new CategorySetting.Visibility(this.antiPushCategory), true);
    public BooleanSetting antiFishingRod = new BooleanSetting("AntiFishingRod", "FishingRods", "Prevents fishing rods from pushing you.", new CategorySetting.Visibility(this.antiPushCategory), false);
    private boolean cancel;
    private boolean pendingConcealment = false;
    private boolean pendingVelocity = false;

    @Override
    public void onEnable() {
        this.pendingVelocity = false;
        this.pendingConcealment = false;
    }

    @Override
    public void onDisable() {
        if (!this.pendingVelocity) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            this.sendRotationFix();
        }
        this.pendingVelocity = false;
        this.pendingConcealment = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (VelocityModule.mc.player == null) {
            return;
        }
        if (this.pendingVelocity && this.mode.getValue().equalsIgnoreCase("Grim")) {
            this.sendRotationFix();
        }
        this.pendingVelocity = false;
        this.pendingConcealment = false;
        this.cancel = false;
    }

    private void sendRotationFix() {
        float f = Night.ROTATION_MANAGER.getServerYaw();
        float f2 = Night.ROTATION_MANAGER.getServerPitch();
        float f3 = (float)((Math.random() * 2.0 - 1.0) * (double)0.001f);
        float f4 = Mth.clamp((float)(f2 + f3), (float)-90.0f, (float)90.0f);
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(VelocityModule.mc.player.getX(), VelocityModule.mc.player.getY(), VelocityModule.mc.player.getZ(), f, f4, VelocityModule.mc.player.onGround(), VelocityModule.mc.player.horizontalCollision));
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (VelocityModule.mc.player != null) {
            BlockPos blockPos;
            ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket;
            Object object;
            if (this.fallDistance.getValue().doubleValue() > 0.0) {
                double d = VelocityModule.mc.player.fallDistance - (double)this.fallDistance.getValue().floatValue();
                int cfr_ignored_1 = d == 0.0 ? 0 : (d > 0.0 ? 1 : -1);
            }
            if (packetReceiveEvent.getPacket() instanceof ClientboundPlayerPositionPacket && this.conceal.getValue()) {
                this.pendingConcealment = true;
            }
            if ((object = packetReceiveEvent.getPacket()) instanceof ClientboundSetEntityMotionPacket) {
                clientboundSetEntityMotionPacket = (ClientboundSetEntityMotionPacket)object;
                if (clientboundSetEntityMotionPacket.id() != VelocityModule.mc.player.getId()) {
                    return;
                }
                if (this.pendingConcealment && clientboundSetEntityMotionPacket.movement().x == 0.0 && clientboundSetEntityMotionPacket.movement().y == 0.0 && clientboundSetEntityMotionPacket.movement().z == 0.0) {
                    this.pendingConcealment = false;
                    return;
                }
                switch (this.mode.getValue()) {
                    case "Normal": {
                        this.scaleVelocity(packetReceiveEvent, clientboundSetEntityMotionPacket);
                        break;
                    }
                    case "Walls": {
                        if (!this.isPhased((Entity)VelocityModule.mc.player) || this.onlyOnGround.getValue() && !VelocityModule.mc.player.onGround()) {
                            return;
                        }
                        this.scaleVelocity(packetReceiveEvent, clientboundSetEntityMotionPacket);
                        break;
                    }
                    case "Cancel": {
                        if (this.pause.getValue() && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) {
                            return;
                        }
                        packetReceiveEvent.setCancelled(true);
                        break;
                    }
                    case "Grim": {
                        if (this.pause.getValue() && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) {
                            return;
                        }
                        if (this.onlyPhased.getValue() && !this.isPhased((Entity)VelocityModule.mc.player)) {
                            return;
                        }
                        if (this.onlyWhenHeadCovered.getValue()) {
                            blockPos = VelocityModule.mc.player.blockPosition().above(2);
                            if (VelocityModule.mc.player.isVisuallyCrawling()) {
                                blockPos = VelocityModule.mc.player.blockPosition().above(1);
                            }
                            if (VelocityModule.mc.level.getBlockState(blockPos).isAir()) {
                                return;
                            }
                        }
                        this.scaleVelocity(packetReceiveEvent, clientboundSetEntityMotionPacket);
                        this.pendingVelocity = true;
                    }
                }
            }
            if ((object = packetReceiveEvent.getPacket()) instanceof ClientboundExplodePacket clientboundExplodePacket) {
                if (this.explosions.getValue()) {
                    switch (this.mode.getValue()) {
                        case "Normal": {
                            this.scaleExplosion(clientboundExplodePacket);
                            break;
                        }
                        case "Walls": {
                            if (!this.isPhased((Entity)VelocityModule.mc.player)) break;
                            this.scaleExplosion(clientboundExplodePacket);
                            break;
                        }
                        case "Cancel": {
                            if (this.pause.getValue() && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) {
                                return;
                            }
                            packetReceiveEvent.setCancelled(true);
                            break;
                        }
                        case "Grim": {
                            if (this.pause.getValue() && !Night.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) {
                                return;
                            }
                            if (this.onlyPhased.getValue() && !this.isPhased((Entity)VelocityModule.mc.player)) {
                                return;
                            }
                            if (this.onlyWhenHeadCovered.getValue()) {
                                blockPos = VelocityModule.mc.player.blockPosition().above(2);
                                if (VelocityModule.mc.player.isVisuallyCrawling()) {
                                    blockPos = VelocityModule.mc.player.blockPosition().above(1);
                                }
                                if (VelocityModule.mc.level.getBlockState(blockPos).isAir()) {
                                    return;
                                }
                            }
                            this.scaleExplosion(clientboundExplodePacket);
                            this.pendingVelocity = true;
                        }
                    }
                    if (packetReceiveEvent.isCancelled()) {
                        mc.executeBlocking(() -> VelocityModule.lambda$onPacketReceive$0(clientboundExplodePacket));
                    }
                }
            }
        }
    }

    private void scaleVelocity(PacketReceiveEvent packetReceiveEvent, ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket) {
        Vec3 vec3 = VelocityModule.mc.player.getDeltaMovement();
        Vec3 vec32 = clientboundSetEntityMotionPacket.movement();
        double d = (vec32.x - vec3.x) * (this.horizontal.getValue().doubleValue() / 100.0) + vec3.x;
        double d2 = (vec32.y - vec3.y) * (this.vertical.getValue().doubleValue() / 100.0) + vec3.y;
        double d3 = (vec32.z - vec3.z) * (this.horizontal.getValue().doubleValue() / 100.0) + vec3.z;
        packetReceiveEvent.setCancelled(true);
        VelocityModule.mc.player.lerpMotion(new Vec3(d, d2, d3));
    }

    private void scaleExplosion(ClientboundExplodePacket clientboundExplodePacket) {
        if (clientboundExplodePacket.playerKnockback().isPresent()) {
            ((Vec3dAccessor)clientboundExplodePacket.playerKnockback().get()).setX((double)((float)(((Vec3)clientboundExplodePacket.playerKnockback().get()).x * (this.horizontal.getValue().doubleValue() / 100.0))));
        }
        if (clientboundExplodePacket.playerKnockback().isPresent()) {
            ((Vec3dAccessor)clientboundExplodePacket.playerKnockback().get()).setY((double)((float)(((Vec3)clientboundExplodePacket.playerKnockback().get()).y * (this.vertical.getValue().doubleValue() / 100.0))));
        }
        if (clientboundExplodePacket.playerKnockback().isPresent()) {
            ((Vec3dAccessor)clientboundExplodePacket.playerKnockback().get()).setZ((double)((float)(((Vec3)clientboundExplodePacket.playerKnockback().get()).z * (this.horizontal.getValue().doubleValue() / 100.0))));
        }
    }

    private boolean isPhased(Entity entity) {
        if (entity == null || VelocityModule.mc.level == null) {
            return false;
        }
        AABB aABB = entity.getBoundingBox();
        int n = Mth.floor((double)aABB.minX);
        int n2 = Mth.ceil((double)aABB.maxX);
        int n3 = Mth.floor((double)aABB.minY);
        int n4 = Mth.ceil((double)aABB.maxY);
        int n5 = Mth.floor((double)aABB.minZ);
        int n6 = Mth.ceil((double)aABB.maxZ);
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    VoxelShape voxelShape = VelocityModule.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)VelocityModule.mc.level, blockPos);
                    if (voxelShape.isEmpty() || !voxelShape.bounds().move(blockPos).intersects(aABB)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getMetaData() {
        if (this.mode.getValue().equalsIgnoreCase("Cancel")) {
            return "0%, 0%";
        }
        if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            return "Grim";
        }
        return this.horizontal.getValue().intValue() + "%, " + this.vertical.getValue().intValue() + "%";
    }

    private static /* synthetic */ void lambda$onPacketReceive$0(ClientboundExplodePacket clientboundExplodePacket) {
        Vec3 vec3 = clientboundExplodePacket.center();
        mc.getSoundManager().play((SoundInstance)new SimpleSoundInstance((SoundEvent)clientboundExplodePacket.explosionSound().value(), SoundSource.BLOCKS, 4.0f, (1.0f + (VelocityModule.mc.level.getRandom().nextFloat() - VelocityModule.mc.level.getRandom().nextFloat()) * 0.2f) * 0.7f, VelocityModule.mc.level.getRandom(), vec3.x, vec3.y, vec3.z));
        VelocityModule.mc.level.addParticle(clientboundExplodePacket.explosionParticle(), vec3.x, vec3.y, vec3.z, 1.0, 0.0, 0.0);
    }
}

