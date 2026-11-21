/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.EntitySpawnEvent
 *  lightning.events.impl.PlayerMineEvent
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PlayerMineEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientLevel.class})
public class ClientWorldMixin {
    @Inject(method={"addEntity"}, at={@At(value="HEAD")})
    private void addEntity(Entity entity, CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new EntitySpawnEvent(entity));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"destroyBlockProgress"}, at={@At(value="HEAD")})
    private void destroyBlockProgress(int n, BlockPos blockPos, int n2, CallbackInfo callbackInfo) {
        try {
            if (n2 >= 0 && n2 <= 9) {
                Night.EVENT_HANDLER.post((Event)new PlayerMineEvent(n, blockPos));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

