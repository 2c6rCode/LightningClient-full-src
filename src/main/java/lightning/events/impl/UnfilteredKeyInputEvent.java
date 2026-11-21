/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class UnfilteredKeyInputEvent
extends Event {
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getScancode() {
        return this.scancode;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public int getModifiers() {
        return this.modifiers;
    }

    @Generated
    public UnfilteredKeyInputEvent(int n, int n2, int n3, int n4) {
        this.key = n;
        this.scancode = n2;
        this.action = n3;
        this.modifiers = n4;
    }
}

