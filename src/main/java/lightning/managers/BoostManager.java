/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundExplodePacket
 */
package lightning.managers;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.TickEvent;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.MovementUtils;
import lightning.utils.system.Timer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;

public class BoostManager
implements IMinecraft {
    public static BoostManager INSTANCE;
    private final Timer explosionTimer = new Timer();
    private final Timer longjumpTimer = new Timer();
    double boostExplosionSpeed;
    boolean canLongjump = false;

    public BoostManager() {
        INSTANCE = this;
        Night.EVENT_HANDLER.subscribe(this);
        this.boostExplosionSpeed = 0.0;
    }

    @SubscribeEvent(priority=100)
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (BoostManager.mc.player == null || BoostManager.mc.level == null) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundExplodePacket) {
            ClientboundExplodePacket clientboundExplodePacket = (ClientboundExplodePacket)packet;
            if (BoostManager.mc.player.position().distanceTo(clientboundExplodePacket.center()) <= 6.0) {
                clientboundExplodePacket.playerKnockback().ifPresent(vec3 -> {
                    if (vec3.x != 0.0 || vec3.z != 0.0) {
                        this.explosionTimer.reset();
                        this.boostExplosionSpeed = Math.hypot(vec3.x, vec3.z);
                        this.canLongjump = true;
                        this.longjumpTimer.reset();
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (BoostManager.mc.player == null || BoostManager.mc.level == null) {
            return;
        }
        if (!MovementUtils.isMoving()) {
            this.boostExplosionSpeed = 0.0;
            this.canLongjump = false;
        }
        if (this.explosionTimer.timeElapsed() >= 400L) {
            this.boostExplosionSpeed = 0.0;
        }
        if (this.longjumpTimer.timeElapsed() >= 500L) {
            this.canLongjump = false;
        }
    }

    public double getBoostSpeed(boolean bl) {
        return this.boostExplosionSpeed;
    }

    public boolean canDoLongjump() {
        return this.canLongjump;
    }
}

