/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.entity.player.Player
 */
package lightning.events.impl;

import lightning.events.Event;
import lombok.Generated;
import net.minecraft.world.entity.player.Player;

public class PlayerDeathEvent
extends Event {
    private final Player player;

    @Generated
    public Player getPlayer() {
        return this.player;
    }

    @Generated
    public PlayerDeathEvent(Player player) {
        this.player = player;
    }
}

