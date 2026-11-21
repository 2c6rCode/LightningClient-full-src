/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import java.util.UUID;
import lightning.events.Event;
import lombok.Generated;

public class PlayerDisconnectEvent
extends Event {
    private final UUID id;

    @Generated
    public PlayerDisconnectEvent(UUID uUID) {
        this.id = uUID;
    }

    @Generated
    public UUID getId() {
        return this.id;
    }
}

