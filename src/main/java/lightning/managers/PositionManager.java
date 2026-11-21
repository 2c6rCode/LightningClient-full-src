/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 */
package lightning.managers;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.utils.IMinecraft;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public class PositionManager
implements IMinecraft {
    private double serverX;
    private double serverY;
    private double serverZ;
    private boolean serverOnGround;
    private boolean serverSprinting;
    private boolean serverSneaking;
    private int serverSlot;

    public PositionManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (PositionManager.mc.player != null) {
            ServerboundMovePlayerPacket serverboundMovePlayerPacket;
            Packet<?> packet = packetSendEvent.getPacket();
            if (packet instanceof ServerboundMovePlayerPacket) {
                serverboundMovePlayerPacket = (ServerboundMovePlayerPacket)packet;
                if (serverboundMovePlayerPacket.hasPosition()) {
                    this.serverX = serverboundMovePlayerPacket.getX(PositionManager.mc.player.getX());
                    this.serverY = serverboundMovePlayerPacket.getY(PositionManager.mc.player.getY());
                    this.serverZ = serverboundMovePlayerPacket.getZ(PositionManager.mc.player.getZ());
                }
                this.serverOnGround = serverboundMovePlayerPacket.isOnGround();
            }
            if ((packet = packetSendEvent.getPacket()) instanceof ServerboundSetCarriedItemPacket slotPacket) {
                this.serverSlot = slotPacket.getSlot();
            }
            if ((packet = packetSendEvent.getPacket()) instanceof ServerboundPlayerCommandPacket cmdPacket) {
                switch (cmdPacket.getAction()) {
                    case START_SPRINTING: {
                        this.serverSprinting = true;
                        break;
                    }
                    case STOP_SPRINTING: {
                        this.serverSprinting = false;
                    }
                }
            }
            if ((packet = packetSendEvent.getPacket()) instanceof ServerboundPlayerInputPacket inputPacket) {
                this.serverSneaking = inputPacket.input().shift();
            }
        }
    }

    public double getServerX() {
        return PositionManager.mc.player != null ? PositionManager.mc.player.getX() : this.serverX;
    }

    public double getServerY() {
        return PositionManager.mc.player != null ? PositionManager.mc.player.getY() : this.serverY;
    }

    public double getServerZ() {
        return PositionManager.mc.player != null ? PositionManager.mc.player.getZ() : this.serverZ;
    }

    public boolean isServerOnGround() {
        return PositionManager.mc.player != null ? PositionManager.mc.player.onGround() : this.serverOnGround;
    }

    @Generated
    public boolean isServerSprinting() {
        return this.serverSprinting;
    }

    @Generated
    public boolean isServerSneaking() {
        return this.serverSneaking;
    }

    @Generated
    public int getServerSlot() {
        return this.serverSlot;
    }
}

