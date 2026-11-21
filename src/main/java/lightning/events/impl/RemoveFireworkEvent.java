/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.projectile.FireworkRocketEntity
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

public class RemoveFireworkEvent
extends Event {
    private final FireworkRocketEntity entity;

    @Generated
    public FireworkRocketEntity getEntity() {
        return this.entity;
    }

    @Generated
    public RemoveFireworkEvent(FireworkRocketEntity fireworkRocketEntity) {
        this.entity = fireworkRocketEntity;
    }
}

