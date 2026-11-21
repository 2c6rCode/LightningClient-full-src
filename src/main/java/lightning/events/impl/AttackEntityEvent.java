/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class AttackEntityEvent
extends Event {
    private final Player player;
    private final Entity target;

    @Generated
    public Player getPlayer() {
        return this.player;
    }

    @Generated
    public Entity getTarget() {
        return this.target;
    }

    @Generated
    public AttackEntityEvent(Player player, Entity entity) {
        this.player = player;
        this.target = entity;
    }
}

