/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.phys.Vec3
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class PlayerMoveEvent
extends Event {
    private final MoverType movementType;
    private Vec3 movement;

    public double getX() {
        return this.movement.x();
    }

    public void setX(double d) {
        this.movement = new Vec3(d, this.movement.y(), this.movement.z());
    }

    public double getY() {
        return this.movement.y();
    }

    public void setY(double d) {
        this.movement = new Vec3(this.movement.x(), d, this.movement.z());
    }

    public double getZ() {
        return this.movement.z();
    }

    public void setZ(double d) {
        this.movement = new Vec3(this.movement.x(), this.movement.y(), d);
    }

    @Generated
    public PlayerMoveEvent(MoverType moverType, Vec3 vec3) {
        this.movementType = moverType;
        this.movement = vec3;
    }

    @Generated
    public MoverType getMovementType() {
        return this.movementType;
    }

    @Generated
    public Vec3 getMovement() {
        return this.movement;
    }

    @Generated
    public void setMovement(Vec3 vec3) {
        this.movement = vec3;
    }
}

