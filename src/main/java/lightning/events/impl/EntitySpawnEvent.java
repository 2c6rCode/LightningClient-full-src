/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.Entity
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.Entity;

public class EntitySpawnEvent
extends Event {
    private final Entity entity;

    @Generated
    public EntitySpawnEvent(Entity entity) {
        this.entity = entity;
    }

    @Generated
    public Entity getEntity() {
        return this.entity;
    }
}

