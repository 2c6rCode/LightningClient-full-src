/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.gui.PeekScreen
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.gui.PeekScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MultiPlayerGameMode.class})
public class MultiPlayerGameModeMixin {
    @Inject(method={"handleContainerInput"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$blockPeekClicks(int n, int n2, int n3, ContainerInput containerInput, Player player, CallbackInfo callbackInfo) {
        try {
            if (Minecraft.getInstance().gui.screen() instanceof PeekScreen) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

