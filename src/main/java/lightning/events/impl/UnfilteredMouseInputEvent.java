/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;

public class UnfilteredMouseInputEvent
extends Event {
    private final int button;
    private final int action;
    private final int mods;

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public int getMods() {
        return this.mods;
    }

    @Generated
    public UnfilteredMouseInputEvent(int n, int n2, int n3) {
        this.button = n;
        this.action = n2;
        this.mods = n3;
    }
}

