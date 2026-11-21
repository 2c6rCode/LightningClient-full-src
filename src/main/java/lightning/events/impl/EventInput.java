/*
 * Decompiled with CFR 0.152.
 */
package lightning.events.impl;

import lightning.events.Event;

public class EventInput
extends Event {
    public boolean forward;
    public boolean backward;
    public boolean left;
    public boolean right;
    public boolean jumping;
    public boolean sneaking;
    public boolean sprinting;

    public EventInput(boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7) {
        this.forward = bl;
        this.backward = bl2;
        this.left = bl3;
        this.right = bl4;
        this.jumping = bl5;
        this.sneaking = bl6;
        this.sprinting = bl7;
    }
}

