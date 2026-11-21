/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class ChangePitchEvent
extends Event {
    private final float pitch;

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public ChangePitchEvent(float f) {
        this.pitch = f;
    }
}

