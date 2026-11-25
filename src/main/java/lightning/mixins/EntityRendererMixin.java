/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.visuals.ChamsModule
 *  lightning.modules.impl.visuals.LogoutSpotModule
 *  lightning.modules.impl.visuals.NameTagsModule
 *  lightning.modules.impl.visuals.NoRenderModule
 *  lightning.modules.impl.visuals.PopChamsModule
 *  lightning.modules.impl.visuals.ShadersModule
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Display
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.vehicle.boat.AbstractBoat
 *  net.minecraft.world.entity.vehicle.minecart.AbstractMinecart
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.visuals.ChamsModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.NameTagsModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.modules.impl.visuals.ShadersModule;
import lightning.utils.mixins.IChamsCapture;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityRenderer.class})
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method={"getNameTag"}, at={@At(value="HEAD")}, cancellable=true)
    private void getDisplayName(T entity, CallbackInfoReturnable<Component> callbackInfoReturnable) {
        try {
            if (entity instanceof RemotePlayer) {
                RemotePlayer remotePlayer = (RemotePlayer)entity;
                PopChamsModule popChamsModule = (PopChamsModule)Night.MODULE_MANAGER.getModule(PopChamsModule.class);
                if (popChamsModule != null && popChamsModule.isToggled() && popChamsModule.isGhost((Entity)remotePlayer)) {
                    callbackInfoReturnable.setReturnValue(null);
                    return;
                }
                LogoutSpotModule logoutSpotModule = (LogoutSpotModule)Night.MODULE_MANAGER.getModule(LogoutSpotModule.class);
                if (logoutSpotModule != null && logoutSpotModule.isToggled() && logoutSpotModule.isGhost((Entity)remotePlayer)) {
                    callbackInfoReturnable.setReturnValue(null);
                    return;
                }
            }
            if (entity instanceof Player && ((NameTagsModule)Night.MODULE_MANAGER.getModule(NameTagsModule.class)).isToggled()) {
                callbackInfoReturnable.setReturnValue(null);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"shouldRender"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$limitItemRendering(T entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            if (entity instanceof RemotePlayer remotePlayer) {
                PopChamsModule popChamsModule = Night.MODULE_MANAGER != null ? (PopChamsModule)Night.MODULE_MANAGER.getModule(PopChamsModule.class) : null;
                if (popChamsModule != null && popChamsModule.isToggled() && popChamsModule.isGhost((Entity)(Object)remotePlayer)) {
                    callbackInfoReturnable.setReturnValue(true);
                    return;
                }
            }
            NoRenderModule noRenderModule = Night.MODULE_MANAGER != null ? (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
            if (noRenderModule != null && noRenderModule.isToggled()) {
                if (noRenderModule.items.getValue() && entity instanceof ItemEntity && !noRenderModule.shouldRenderItem()) {
                    callbackInfoReturnable.setReturnValue(false);
                }
                if (noRenderModule.displays.getValue() && entity instanceof Display) {
                    callbackInfoReturnable.setReturnValue(false);
                }
                if (noRenderModule.armorStand.getValue() && entity instanceof ArmorStand) {
                    callbackInfoReturnable.setReturnValue(false);
                }
                if (noRenderModule.minecart.getValue() && entity instanceof AbstractMinecart) {
                    callbackInfoReturnable.setReturnValue(false);
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    /*
     * Unable to fully structure code
     */
    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void night$chams(T entity, S renderState, float partialTick, CallbackInfo callbackInfo) {
        try {
            IChamsCapture chamsCapture = (IChamsCapture)renderState;
            chamsCapture.night$setChams(false, 0, false, 0, false);
            chamsCapture.night$setChamsYOffset(0.0f);
            PopChamsModule popChamsModule = (PopChamsModule)Night.MODULE_MANAGER.getModule(PopChamsModule.class);
            LogoutSpotModule logoutSpotModule = (LogoutSpotModule)Night.MODULE_MANAGER.getModule(LogoutSpotModule.class);
            boolean isPopChamGhost = false;
            if (entity instanceof RemotePlayer) {
                RemotePlayer remotePlayer = (RemotePlayer)entity;
                if (popChamsModule != null && popChamsModule.isGhost((Entity)remotePlayer)) {
                    isPopChamGhost = true;
                }
            }
            boolean isLogoutGhost = false;
            if (entity instanceof RemotePlayer) {
                RemotePlayer remotePlayer = (RemotePlayer)entity;
                if (logoutSpotModule != null && logoutSpotModule.isGhost((Entity)remotePlayer)) {
                    isLogoutGhost = true;
                }
            }
            boolean isGhost = isPopChamGhost || isLogoutGhost;
            if (!isGhost) {
                ChamsModule chams = (ChamsModule)Night.MODULE_MANAGER.getModule(ChamsModule.class);
                if (chams != null && chams.isToggled()) {
                    if (entity instanceof LivingEntity living && chams.isValidEntity((Entity)(Object)living)) {
                        chams.applyEntityChams((LivingEntity)(Object)living, chamsCapture);
                    } else if (chams.crystals.getValue() && entity instanceof EndCrystal crystal && chams.inRange((Entity)(Object)crystal)) {
                        chams.applyCrystalChams(chamsCapture);
                    } else if (chams.vehicles.getValue() && chams.inRange((Entity)entity) && (entity instanceof AbstractBoat || entity instanceof AbstractMinecart)) {
                        chams.applyVehicleChams(chamsCapture);
                    }
                }
                ShadersModule shaders = (ShadersModule)Night.MODULE_MANAGER.getModule(ShadersModule.class);
                if (shaders != null && shaders.isToggled() && shaders.isValidEntity((Entity)entity)) {
                    renderState.outlineColor = shaders.getFillColor((Entity)entity);
                }
            }
            if (popChamsModule != null && popChamsModule.isToggled() && isPopChamGhost) {
                RemotePlayer player = (RemotePlayer)entity;
                chamsCapture.night$setChams(popChamsModule.shouldFill(), popChamsModule.getFillColor(player).getRGB(), popChamsModule.shouldOutline(), popChamsModule.getOutlineColor(player).getRGB(), false, true);
                chamsCapture.night$setChamsYOffset(popChamsModule.getYOffset(player));
            }
            if (logoutSpotModule != null && logoutSpotModule.isToggled() && isLogoutGhost) {
                RemotePlayer player = (RemotePlayer)entity;
                chamsCapture.night$setChams(logoutSpotModule.shouldFill(), logoutSpotModule.getFillColor(player).getRGB(), logoutSpotModule.shouldOutline(), logoutSpotModule.getOutlineColor(player).getRGB(), false, true);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

