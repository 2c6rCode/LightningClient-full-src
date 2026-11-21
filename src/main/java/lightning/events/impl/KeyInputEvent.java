/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class KeyInputEvent
extends Event {
    private final int key;
    private final int modifiers;

    @Override
    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof KeyInputEvent)) {
            return false;
        }
        KeyInputEvent keyInputEvent = (KeyInputEvent)object;
        if (!keyInputEvent.canEqual(this)) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        if (this.getKey() != keyInputEvent.getKey()) {
            return false;
        }
        return this.getModifiers() == keyInputEvent.getModifiers();
    }

    @Override
    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof KeyInputEvent;
    }

    @Override
    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = super.hashCode();
        n2 = n2 * 59 + this.getKey();
        n2 = n2 * 59 + this.getModifiers();
        return n2;
    }

    @Generated
    public KeyInputEvent(int n, int n2) {
        this.key = n;
        this.modifiers = n2;
    }

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getModifiers() {
        return this.modifiers;
    }

    @Override
    @Generated
    public String toString() {
        return "KeyInputEvent(key=" + this.getKey() + ", modifiers=" + this.getModifiers() + ")";
    }
}

