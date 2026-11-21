/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.GameLoopEvent
 *  lightning.events.impl.TickEvent
 *  lightning.events.impl.TickEvent$Post
 *  lightning.modules.api.IFreecamModule
 *  lightning.modules.impl.combat.MainhandModule
 *  lightning.modules.impl.core.NoMiddleClickModule
 *  lightning.modules.impl.miscellaneous.AutoEscapeModule
 *  lightning.modules.impl.player.FastPlaceModule
 *  lightning.modules.impl.player.MultiTaskModule
 *  lightning.utils.IMinecraft
 *  lightning.utils.minecraft.WorldUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.main.GameConfig
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.GameLoopEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.combat.MainhandModule;
import lightning.modules.impl.core.NoMiddleClickModule;
import lightning.modules.impl.miscellaneous.AutoEscapeModule;
import lightning.modules.impl.player.FastPlaceModule;
import lightning.modules.impl.player.MultiTaskModule;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public abstract class MinecraftClientMixin
implements IMinecraft {
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    private int rightClickDelay;
    @Shadow
    @Final
    public Options options;
    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    @Nullable
    public Entity crosshairPickEntity;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void init(GameConfig gameConfig, CallbackInfo callbackInfo) {
        try {
            Night.onPostInitialize();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"runTick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;runAllTasks()V", shift=At.Shift.AFTER)})
    private void runTickHook(boolean bl, CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new GameLoopEvent());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void tick(CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new TickEvent());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void tick$post(CallbackInfo callbackInfo) {
        try {
            Night.EVENT_HANDLER.post((Event)new TickEvent.Post());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"startUseItem"}, at={@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift=At.Shift.AFTER)})
    private void doItemUse(CallbackInfo callbackInfo) {
        try {
            if (Night.MODULE_MANAGER != null && ((FastPlaceModule)Night.MODULE_MANAGER.getModule(FastPlaceModule.class)).isToggled() && ((FastPlaceModule)Night.MODULE_MANAGER.getModule(FastPlaceModule.class)).isValidItem(this.player.getMainHandItem().getItem())) {
                this.rightClickDelay = ((FastPlaceModule)Night.MODULE_MANAGER.getModule(FastPlaceModule.class)).ticks.getValue().intValue();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyExpressionValue(method={"continueAttack"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")})
    private boolean handleBlockBreaking(boolean bl) {
        try {
            if (Night.MODULE_MANAGER != null && ((MultiTaskModule)Night.MODULE_MANAGER.getModule(MultiTaskModule.class)).isToggled()) {
                return false;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @ModifyExpressionValue(method={"startUseItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z")})
    private boolean handleInputEvents(boolean bl) {
        try {
            if (Night.MODULE_MANAGER != null && ((MultiTaskModule)Night.MODULE_MANAGER.getModule(MultiTaskModule.class)).isToggled()) {
                return false;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @Redirect(method={"handleKeybinds"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V")})
    private void night$dontReleaseFakedUse(MultiPlayerGameMode multiPlayerGameMode, Player player) {
        if (((AutoEscapeModule)Night.MODULE_MANAGER.getModule(AutoEscapeModule.class)).isEating()) {
            return;
        }
        multiPlayerGameMode.releaseUsingItem(player);
    }

    @Redirect(method={"handleKeybinds"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V")})
    private void night$redirectSetSelectedSlot(Inventory inventory, int n) {
        MainhandModule mainhandModule;
        if (Night.MODULE_MANAGER != null && (mainhandModule = (MainhandModule)Night.MODULE_MANAGER.getModule(MainhandModule.class)) != null && mainhandModule.shouldLockSlot()) {
            return;
        }
        inventory.setSelectedSlot(n);
    }

    @Inject(method={"pick"}, at={@At(value="HEAD")}, cancellable=true)
    private void pick(float f, CallbackInfo callbackInfo) {
        try {
            if (MinecraftClientMixin.mc.level == null || this.player == null) {
                return;
            }
            IFreecamModule iFreecamModule = (IFreecamModule)Night.MODULE_MANAGER.getModule("Freecam");
            if (iFreecamModule.isToggled()) {
                Entity entity;
                this.hitResult = WorldUtils.getRaytraceTarget((float)iFreecamModule.getFreeYaw(), (float)iFreecamModule.getFreePitch(), (double)iFreecamModule.getFreeX(), (double)iFreecamModule.getFreeY(), (double)iFreecamModule.getFreeZ());
                HitResult hitResult = this.hitResult;
                if (hitResult instanceof EntityHitResult) {
                    EntityHitResult entityHitResult = (EntityHitResult)hitResult;
                    entity = entityHitResult.getEntity();
                } else {
                    entity = null;
                }
                this.crosshairPickEntity = entity;
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"pickBlockOrEntity"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void night$onPickBlock(CallbackInfo callbackInfo) {
        try {
            NoMiddleClickModule noMiddleClickModule;
            if (Night.MODULE_MANAGER != null && (noMiddleClickModule = (NoMiddleClickModule)Night.MODULE_MANAGER.getModule(NoMiddleClickModule.class)) != null && noMiddleClickModule.isToggled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

