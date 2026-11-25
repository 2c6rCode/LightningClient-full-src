/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 */
package lightning.pingbypass.protocol;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.Connection;

@FunctionalInterface
public interface PbPacketHandler<T extends PbPacket> {
    public void handle(T packet, Connection connection);
}

