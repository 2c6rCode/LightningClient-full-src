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
    private void getDisplayName(T t, CallbackInfoReturnable<Component> callbackInfoReturnable) {
        try {
            if (t instanceof RemotePlayer) {
                RemotePlayer remotePlayer = (RemotePlayer)t;
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
            if (t instanceof Player && ((NameTagsModule)Night.MODULE_MANAGER.getModule(NameTagsModule.class)).isToggled()) {
                callbackInfoReturnable.setReturnValue(null);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"shouldRender"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$limitItemRendering(T t, Frustum frustum, double d, double d2, double d3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            if (t instanceof RemotePlayer remotePlayer) {
                PopChamsModule popChamsModule = Night.MODULE_MANAGER != null ? (PopChamsModule)Night.MODULE_MANAGER.getModule(PopChamsModule.class) : null;
                if (popChamsModule != null && popChamsModule.isToggled() && popChamsModule.isGhost((Entity)(Object)remotePlayer)) {
                    callbackInfoReturnable.setReturnValue(true);
                    return;
                }
            }
            NoRenderModule noRenderModule2 = Night.MODULE_MANAGER != null ? (NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class) : null;
            if (noRenderModule2 != null && noRenderModule2.isToggled()) {
                if (noRenderModule2.items.getValue() && t instanceof ItemEntity && !noRenderModule2.shouldRenderItem()) {
                    callbackInfoReturnable.setReturnValue(false);
                }
                if (noRenderModule2.displays.getValue() && t instanceof Display) {
                    callbackInfoReturnable.setReturnValue(false);
                }
                if (noRenderModule2.armorStand.getValue() && t instanceof ArmorStand) {
                    callbackInfoReturnable.setReturnValue(false);
                }
                if (noRenderModule2.minecart.getValue() && t instanceof AbstractMinecart) {
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
    private void night$chams(T var1_1, S var2_2, float var3_3, CallbackInfo var4_4) {
        try {
            IChamsCapture var5_5 = (IChamsCapture)var2_2;
            var5_5.night$setChams(false, 0, false, 0, false);
            var5_5.night$setChamsYOffset(0.0f);
            PopChamsModule var6_6 = (PopChamsModule)Night.MODULE_MANAGER.getModule(PopChamsModule.class);
            LogoutSpotModule var7_7 = (LogoutSpotModule)Night.MODULE_MANAGER.getModule(LogoutSpotModule.class);
            boolean var8_10 = false;
            if (var1_1 instanceof RemotePlayer) {
                RemotePlayer var9_8 = (RemotePlayer)var1_1;
                if (var6_6 != null && var6_6.isGhost((Entity)var9_8)) {
                    var8_10 = true;
                }
            }
            boolean var9_9 = false;
            if (var1_1 instanceof RemotePlayer) {
                RemotePlayer var10_11 = (RemotePlayer)var1_1;
                if (var7_7 != null && var7_7.isGhost((Entity)var10_11)) {
                    var9_9 = true;
                }
            }
            boolean var10_12 = var8_10 || var9_9;
            if (!var10_12) {
                ChamsModule chams = (ChamsModule)Night.MODULE_MANAGER.getModule(ChamsModule.class);
                if (chams != null && chams.isToggled()) {
                    if (var1_1 instanceof LivingEntity living && chams.isValidEntity((Entity)(Object)living)) {
                        chams.applyEntityChams((LivingEntity)(Object)living, var5_5);
                    } else if (chams.crystals.getValue() && var1_1 instanceof EndCrystal crystal && chams.inRange((Entity)(Object)crystal)) {
                        chams.applyCrystalChams(var5_5);
                    } else if (chams.vehicles.getValue() && chams.inRange((Entity)var1_1) && (var1_1 instanceof AbstractBoat || var1_1 instanceof AbstractMinecart)) {
                        chams.applyVehicleChams(var5_5);
                    }
                }
                ShadersModule shaders = (ShadersModule)Night.MODULE_MANAGER.getModule(ShadersModule.class);
                if (shaders != null && shaders.isToggled() && shaders.isValidEntity((Entity)var1_1)) {
                    var2_2.outlineColor = shaders.getFillColor((Entity)var1_1);
                }
            }
            if (var6_6 != null && var6_6.isToggled() && var8_10) {
                RemotePlayer player = (RemotePlayer)var1_1;
                var5_5.night$setChams(var6_6.shouldFill(), var6_6.getFillColor(player).getRGB(), var6_6.shouldOutline(), var6_6.getOutlineColor(player).getRGB(), false, true);
                var5_5.night$setChamsYOffset(var6_6.getYOffset(player));
            }
            if (var7_7 != null && var7_7.isToggled() && var9_9) {
                RemotePlayer player = (RemotePlayer)var1_1;
                var5_5.night$setChams(var7_7.shouldFill(), var7_7.getFillColor(player).getRGB(), var7_7.shouldOutline(), var7_7.getOutlineColor(player).getRGB(), false, true);
            }
            return;
        }
        catch (NullPointerException v3) {
            return;
        }
    }
}

