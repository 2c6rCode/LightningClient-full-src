/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lightning.modules.Module;
import lombok.Generated;

public class ToggleModuleEvent
extends Event {
    private final Module module;
    private final boolean state;

    @Generated
    public ToggleModuleEvent(Module module, boolean state) {
        this.module = module;
        this.state = state;
    }

    @Generated
    public Module getModule() {
        return this.module;
    }

    @Generated
    public boolean isState() {
        return this.state;
    }
}

