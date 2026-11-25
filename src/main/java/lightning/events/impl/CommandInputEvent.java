/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class CommandInputEvent
extends Event {
    private final String message;

    @Generated
    public String getMessage() {
        return this.message;
    }

    @Generated
    public CommandInputEvent(String message) {
        this.message = message;
    }
}

