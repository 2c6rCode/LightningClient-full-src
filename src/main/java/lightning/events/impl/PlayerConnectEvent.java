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

public class PlayerConnectEvent
extends Event {
    private final UUID id;

    @Generated
    public PlayerConnectEvent(UUID uUID) {
        this.id = uUID;
    }

    @Generated
    public UUID getId() {
        return this.id;
    }
}

