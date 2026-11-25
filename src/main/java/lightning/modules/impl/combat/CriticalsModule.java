/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.ClientPlayerEntityAccessor
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.decoration.ItemFrame
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package lightning.modules.impl.combat;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@RegisterModule(name="Criticals", description="Changes player movement for always landing critical hits.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class CriticalsModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used to achieve critical hits.", "Packet", new String[]{"Packet", "Grim", "GrimNew"});
    public BooleanSetting onlyPhased = new BooleanSetting("OnlyPhased", "Only crits when you are phased into blocks.", new ModeSetting.Visibility(this.mode, "Grim", "GrimNew"), true);
    public BooleanSetting onlyStandingStill = new BooleanSetting("OnlyStandingStill", "Only crits when you are standing still.", new ModeSetting.Visibility(this.mode, "Grim", "GrimNew"), true);
    public BooleanSetting onlyWhenHeadCovered = new BooleanSetting("HeadCovered", "Only crits when there is a block covering your head.", new ModeSetting.Visibility(this.mode, "Grim", "GrimNew"), true);
    private long lastCritTime = 0L;

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent attackEntityEvent) {
        LivingEntity livingEntity;
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (CriticalsModule.mc.player == null || CriticalsModule.mc.level == null) {
            return;
        }
        if (CriticalsModule.mc.player.isFallFlying() || CriticalsModule.mc.player.isInWater() || CriticalsModule.mc.player.isInLava() || CriticalsModule.mc.player.isSuppressingSlidingDownLadder() || CriticalsModule.mc.player.hasEffect(MobEffects.BLINDNESS)) {
            return;
        }
        Entity entity = attackEntityEvent.getTarget();
        if (!(entity instanceof LivingEntity) || !(livingEntity = (LivingEntity)entity).isAlive() || entity instanceof EndCrystal || entity instanceof ItemFrame) {
            return;
        }
        if (CriticalsModule.mc.player.isHandsBusy()) {
            this.ridingAttack(entity);
            return;
        }
        this.onGroundAttack();
        CriticalsModule.mc.player.crit(entity);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (packetSendEvent.getPacket() instanceof ServerboundAttackPacket) {
            long now = System.currentTimeMillis();
            if (now - this.lastCritTime < 50L) {
                return;
            }
            if (CriticalsModule.mc.player == null || CriticalsModule.mc.level == null) {
                return;
            }
            if (CriticalsModule.mc.player.isFallFlying() || CriticalsModule.mc.player.isInWater() || CriticalsModule.mc.player.isInLava() || CriticalsModule.mc.player.isSuppressingSlidingDownLadder() || CriticalsModule.mc.player.hasEffect(MobEffects.BLINDNESS)) {
                return;
            }
            this.onGroundAttack();
        }
    }

    private void ridingAttack(Entity entity) {
        if (this.mode.getValue().equalsIgnoreCase("Packet")) {
            for (int i = 0; i < 5; ++i) {
                mc.getConnection().send((Packet)new ServerboundAttackPacket(entity.getId()));
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
    }

    private void onGroundAttack() {
        this.lastCritTime = System.currentTimeMillis();
        double posX = CriticalsModule.mc.player.getX();
        double posY = CriticalsModule.mc.player.getY();
        double posZ = CriticalsModule.mc.player.getZ();
        ProxyServerTickListener.allowSend(() -> {
            switch (this.mode.getValue()) {
                case "Packet": {
                    this.packetCrit(posX, posY, posZ);
                    break;
                }
                case "Grim": {
                    this.grimCrit(posX, posY, posZ);
                    break;
                }
                case "GrimNew": {
                    this.grimNewCrit(posX, posY, posZ);
                }
            }
        });
        ((ClientPlayerEntityAccessor)CriticalsModule.mc.player).setLastOnGround(false);
    }

    private void packetCrit(double posX, double posY, double posZ) {
        if (!CriticalsModule.mc.player.onGround()) {
            return;
        }
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(posX, posY + 0.0625, posZ, false, CriticalsModule.mc.player.horizontalCollision));
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(posX, posY, posZ, false, CriticalsModule.mc.player.horizontalCollision));
    }

    private void grimCrit(double posX, double posY, double posZ) {
        if (!CriticalsModule.mc.player.onGround()) {
            return;
        }
        if (this.onlyPhased.getValue() && !this.isPhased((Entity)CriticalsModule.mc.player)) {
            return;
        }
        if (this.onlyStandingStill.getValue() && (Math.abs(CriticalsModule.mc.player.getDeltaMovement().x) > 0.01 || Math.abs(CriticalsModule.mc.player.getDeltaMovement().y) > 0.01 || Math.abs(CriticalsModule.mc.player.getDeltaMovement().z) > 0.01)) {
            return;
        }
        if (this.onlyWhenHeadCovered.getValue()) {
            BlockPos blockPos = CriticalsModule.mc.player.blockPosition();
            BlockPos headPos = CriticalsModule.mc.player.isVisuallyCrawling() ? blockPos.above(1) : blockPos.above(2);
            if (CriticalsModule.mc.level.getBlockState(headPos).isAir()) {
                return;
            }
        }
        float serverYaw = Night.ROTATION_MANAGER.getServerYaw();
        float serverPitch = Night.ROTATION_MANAGER.getServerPitch();
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(posX, posY + 0.0625, posZ, serverYaw, serverPitch, false, CriticalsModule.mc.player.horizontalCollision));
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(posX, posY + 0.0625013579, posZ, serverYaw, serverPitch, false, CriticalsModule.mc.player.horizontalCollision));
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(posX, posY + 1.3579E-6, posZ, serverYaw, serverPitch, false, CriticalsModule.mc.player.horizontalCollision));
    }

    private void grimNewCrit(double posX, double posY, double posZ) {
        if (!CriticalsModule.mc.player.onGround()) {
            return;
        }
        if (!(!this.onlyPhased.getValue() || this.isPhased((Entity)CriticalsModule.mc.player) && this.eyesPhased((Player)CriticalsModule.mc.player))) {
            return;
        }
        if (this.onlyStandingStill.getValue() && (Math.abs(CriticalsModule.mc.player.getDeltaMovement().x) > 0.01 || Math.abs(CriticalsModule.mc.player.getDeltaMovement().y) > 0.01 || Math.abs(CriticalsModule.mc.player.getDeltaMovement().z) > 0.01)) {
            return;
        }
        if (this.onlyWhenHeadCovered.getValue()) {
            BlockPos blockPos = CriticalsModule.mc.player.blockPosition();
            BlockPos headPos = CriticalsModule.mc.player.isVisuallyCrawling() ? blockPos.above(1) : blockPos.above(2);
            if (CriticalsModule.mc.level.getBlockState(headPos).isAir()) {
                return;
            }
        }
        float serverYaw = Night.ROTATION_MANAGER.getServerYaw();
        float serverPitch = Night.ROTATION_MANAGER.getServerPitch();
        float jitter = (float)((Math.random() * 2.0 - 1.0) * (double)0.001f);
        float pitch = Mth.clamp((float)(serverPitch + jitter), (float)-90.0f, (float)90.0f);
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(posX, posY + 0.0626, posZ, serverYaw, pitch, false, CriticalsModule.mc.player.horizontalCollision));
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(posX, posY + 0.0455, posZ, serverYaw, pitch, false, CriticalsModule.mc.player.horizontalCollision));
    }

    private boolean isPhased(Entity entity) {
        if (entity == null || CriticalsModule.mc.level == null) {
            return false;
        }
        AABB aABB = entity.getBoundingBox();
        int minX = Mth.floor((double)aABB.minX);
        int maxX = Mth.ceil((double)aABB.maxX);
        int minY = Mth.floor((double)aABB.minY);
        int maxY = Mth.ceil((double)aABB.maxY);
        int minZ = Mth.floor((double)aABB.minZ);
        int maxZ = Mth.ceil((double)aABB.maxZ);
        for (int i = minX; i < maxX; ++i) {
            for (int j = minY; j < maxY; ++j) {
                for (int k = minZ; k < maxZ; ++k) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    VoxelShape voxelShape = CriticalsModule.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)CriticalsModule.mc.level, blockPos);
                    if (voxelShape.isEmpty() || !voxelShape.bounds().move(blockPos).intersects(aABB)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean eyesPhased(Player player) {
        Vec3 eyePos = player.getEyePosition();
        BlockPos blockPos = BlockPos.containing((Position)eyePos);
        BlockState blockState = CriticalsModule.mc.level.getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        }
        VoxelShape voxelShape = blockState.getCollisionShape((BlockGetter)CriticalsModule.mc.level, blockPos);
        if (voxelShape.isEmpty()) {
            return false;
        }
        for (AABB aABB : voxelShape.toAabbs()) {
            if (!aABB.move(blockPos).contains(eyePos)) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }
}

