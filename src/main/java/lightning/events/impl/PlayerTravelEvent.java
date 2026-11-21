/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.phys.Vec3
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

public class PlayerTravelEvent
extends Event {
    private final Vec3 movementInput;

    @Generated
    public Vec3 getMovementInput() {
        return this.movementInput;
    }

    @Generated
    public PlayerTravelEvent(Vec3 vec3) {
        this.movementInput = vec3;
    }
}

