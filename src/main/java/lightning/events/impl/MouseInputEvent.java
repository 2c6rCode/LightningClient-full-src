/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class MouseInputEvent
extends Event {
    private final int button;

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public MouseInputEvent(int button) {
        this.button = button;
    }
}

