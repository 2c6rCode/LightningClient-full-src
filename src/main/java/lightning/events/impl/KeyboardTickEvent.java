/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class KeyboardTickEvent
extends Event {
    private float movementForward;
    private float movementSideways;

    @Generated
    public KeyboardTickEvent(float f, float f2) {
        this.movementForward = f;
        this.movementSideways = f2;
    }

    @Generated
    public float getMovementForward() {
        return this.movementForward;
    }

    @Generated
    public float getMovementSideways() {
        return this.movementSideways;
    }

    @Generated
    public void setMovementForward(float f) {
        this.movementForward = f;
    }

    @Generated
    public void setMovementSideways(float f) {
        this.movementSideways = f;
    }
}

