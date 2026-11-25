/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.PlayerMineEvent
 *  net.minecraft.server.level.BlockDestructionProgress
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.PlayerMineEvent;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockDestructionProgress.class})
public class BlockBreakingInfoMixin {
    @Inject(method={"setProgress"}, at={@At(value="HEAD")})
    private void setProgress(int progress, CallbackInfo callbackInfo) {
        try {
            BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)(Object)this;
            if (progress >= 0 && progress <= 9) {
                Night.EVENT_HANDLER.post((Event)new PlayerMineEvent(blockDestructionProgress.getId(), blockDestructionProgress.getPos()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

