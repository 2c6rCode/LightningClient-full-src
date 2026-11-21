/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class ChatInputEvent
extends Event {
    private String message;

    @Generated
    public String getMessage() {
        return this.message;
    }

    @Generated
    public void setMessage(String string) {
        this.message = string;
    }

    @Generated
    public ChatInputEvent(String string) {
        this.message = string;
    }
}

