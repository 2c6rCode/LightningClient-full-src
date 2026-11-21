/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.ClientConnectEvent
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.pingbypass.PingBypassFlags
 *  lightning.utils.IMinecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundLoginPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.ClientConnectEvent;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.utils.IMinecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ClientPlayNetworkHandlerMixin
implements IMinecraft {
    @Unique
    private float handleMovePlayer$prevYaw;
    @Unique
    private float handleMovePlayer$prevPitch;
    @Unique
    private boolean handleMovePlayer$guard;

    @Inject(method={"handleLogin"}, at={@At(value="TAIL")})
    private void onGameJoin(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new ClientConnectEvent());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"handleMovePlayer"}, at={@At(value="HEAD")})
    private void handleMovePlayer$HEAD(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket, CallbackInfo callbackInfo) {
        try {
            this.handleMovePlayer$guard = false;
            if (ClientPlayNetworkHandlerMixin.mc.player == null) {
                return;
            }
            if (PingBypassFlags.isPingBypassActive()) {
                return;
            }
            ElytraFlyModule elytraFlyModule = Night.MODULE_MANAGER != null ? (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class) : null;
            boolean bl = elytraFlyModule != null && elytraFlyModule.isToggled() && elytraFlyModule.mode.getValue().equalsIgnoreCase("Bounce");
            boolean bl2 = bl;
            if (Night.ROTATION_MANAGER != null && Night.ROTATION_MANAGER.isPacketRotateActive() || bl) {
                this.handleMovePlayer$prevYaw = ClientPlayNetworkHandlerMixin.mc.player.getYRot();
                this.handleMovePlayer$prevPitch = bl && elytraFlyModule.isBouncePitchOverrideActive() ? elytraFlyModule.getBounceSavedCameraPitch() : ClientPlayNetworkHandlerMixin.mc.player.getXRot();
                this.handleMovePlayer$guard = true;
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"handleMovePlayer"}, at={@At(value="TAIL")})
    private void handleMovePlayer$TAIL(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket, CallbackInfo callbackInfo) {
        if (!this.handleMovePlayer$guard) {
            return;
        }
        this.handleMovePlayer$guard = false;
        ClientPlayNetworkHandlerMixin.mc.player.setYRot(this.handleMovePlayer$prevYaw);
        ClientPlayNetworkHandlerMixin.mc.player.setXRot(this.handleMovePlayer$prevPitch);
        ClientPlayNetworkHandlerMixin.mc.player.xRotO = this.handleMovePlayer$prevPitch;
    }
}

