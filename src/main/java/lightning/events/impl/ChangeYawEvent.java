/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class ChangeYawEvent
extends Event {
    private final float yaw;

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public ChangeYawEvent(float yaw) {
        this.yaw = yaw;
    }
}

