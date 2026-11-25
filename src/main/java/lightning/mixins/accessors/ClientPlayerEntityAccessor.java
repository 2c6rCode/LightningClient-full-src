/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Input
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package lightning.mixins.accessors;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={LocalPlayer.class})
public interface ClientPlayerEntityAccessor {
    @Invoker(value="isMoving")
    public boolean invokeIsWalking();

    @Invoker(value="isSprintingPossible")
    public boolean invokeCanSprint(boolean allowSprinting);

    @Invoker(value="sendPosition")
    public void invokeSendMovementPackets();

    @Accessor(value="lastOnGround")
    public void setLastOnGround(boolean lastOnGround);

    @Accessor(value="yRotLast")
    public void setLastYaw(float lastYaw);

    @Accessor(value="xRotLast")
    public void setLastPitch(float lastPitch);

    @Accessor(value="lastSentInput")
    public void setLastSentInput(Input lastSentInput);
}

