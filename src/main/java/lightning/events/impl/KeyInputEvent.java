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
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof KeyInputEvent)) {
            return false;
        }
        KeyInputEvent keyInputEvent = (KeyInputEvent)other;
        if (!keyInputEvent.canEqual(this)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (this.getKey() != keyInputEvent.getKey()) {
            return false;
        }
        return this.getModifiers() == keyInputEvent.getModifiers();
    }

    @Override
    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof KeyInputEvent;
    }

    @Override
    @Generated
    public int hashCode() {
        int prime = 59;
        int result = super.hashCode();
        result = result * 59 + this.getKey();
        result = result * 59 + this.getModifiers();
        return result;
    }

    @Generated
    public KeyInputEvent(int key, int modifiers) {
        this.key = key;
        this.modifiers = modifiers;
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

