/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.protocol.Packet
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;

public class PacketSendEvent
extends Event {
    private final Packet<?> packet;

    @Generated
    public Packet<?> getPacket() {
        return this.packet;
    }

    @Generated
    public PacketSendEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public static class Post
    extends Event {
        private final Packet<?> packet;

        @Generated
        public Packet<?> getPacket() {
            return this.packet;
        }

        @Generated
        public Post(Packet<?> packet) {
            this.packet = packet;
        }
    }
}

