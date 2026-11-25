/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lightning.modules.Module;
import lightning.utils.rotations.Rotation;
import lombok.Generated;

public class ClientRotationEvent
extends Event {
    private final Rotation rotation;
    private Module owner;

    public ClientRotationEvent(Rotation rotation) {
        this.rotation = rotation;
    }

    public void setYaw(float yaw) {
        this.setCancelled(true);
        this.rotation.setYaw(yaw);
    }

    public void setPitch(float pitch) {
        this.setCancelled(true);
        this.rotation.setPitch(pitch);
    }

    @Generated
    public Rotation getRotation() {
        return this.rotation;
    }

    @Generated
    public Module getOwner() {
        return this.owner;
    }

    @Generated
    public void setOwner(Module module) {
        this.owner = module;
    }
}

