/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="AntiVoid", description="Prevents you from falling into the void.", category=Module.Category.MOVEMENT)
public class AntiVoidModule
extends Module {
    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        if (AntiVoidModule.mc.player == null || AntiVoidModule.mc.level == null) {
            return;
        }
        if (this.falling()) {
            playerMoveEvent.setCancelled(true);
            playerMoveEvent.setMovement(new Vec3(playerMoveEvent.getMovement().x, 0.0, playerMoveEvent.getMovement().z));
            mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(AntiVoidModule.mc.player.getX(), AntiVoidModule.mc.player.getY(), AntiVoidModule.mc.player.getZ(), true, AntiVoidModule.mc.player.horizontalCollision));
        }
    }

    private boolean falling() {
        if (AntiVoidModule.mc.player == null || AntiVoidModule.mc.level == null) {
            return false;
        }
        int n = AntiVoidModule.mc.level.getMinY();
        for (int i = (int)AntiVoidModule.mc.player.getY(); i >= n; --i) {
            BlockPos blockPos = BlockPos.containing((double)AntiVoidModule.mc.player.getX(), (double)i, (double)AntiVoidModule.mc.player.getZ());
            BlockState blockState = AntiVoidModule.mc.level.getBlockState(blockPos);
            if (blockState.isAir() || blockState.getCollisionShape((BlockGetter)AntiVoidModule.mc.level, blockPos).isEmpty()) continue;
            return false;
        }
        return AntiVoidModule.mc.player.fallDistance > 0.0;
    }
}

