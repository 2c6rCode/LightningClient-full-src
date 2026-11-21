/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class ServerConnectEvent
extends Event {
    private final ServerAddress address;
    private final ServerData info;

    @Generated
    public ServerConnectEvent(ServerAddress serverAddress, ServerData serverData) {
        this.address = serverAddress;
        this.info = serverData;
    }

    @Generated
    public ServerAddress getAddress() {
        return this.address;
    }

    @Generated
    public ServerData getInfo() {
        return this.info;
    }
}

