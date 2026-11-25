/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  lightning.Night
 *  lightning.managers.ChatManager
 *  lightning.modules.impl.core.HUDModule
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.modules.impl.visuals.NoRenderModule
 *  lightning.utils.graphics.HotbarCache
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import lightning.Night;
import lightning.managers.ChatManager;
import lightning.modules.impl.core.HUDModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.graphics.HotbarCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Hud.class})
public class HudMixin {
    private static final Identifier POWDER_SNOW_OUTLINE_LOCATION = Identifier.withDefaultNamespace((String)"textures/misc/powder_snow_outline.png");

    @Inject(method={"extractEffects"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderStatusEffectOverlay(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        try {
            if (Night.MODULE_MANAGER != null && ((HUDModule)Night.MODULE_MANAGER.getModule(HUDModule.class)).isToggled() && ((HUDModule)Night.MODULE_MANAGER.getModule(HUDModule.class)).vanillaPotions.getValue().equalsIgnoreCase("Hide")) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderPortalOverlay(GuiGraphicsExtractor guiGraphicsExtractor, float nauseaIntensity, CallbackInfo callbackInfo) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).portalOverlay.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractVignette"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderVignetteOverlay(GuiGraphicsExtractor guiGraphicsExtractor, Entity entity, CallbackInfo callbackInfo) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).vignette.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractSelectedItemName"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderSelectedItemName(GuiGraphicsExtractor guiGraphicsExtractor, CallbackInfo callbackInfo) {
        try {
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).itemName.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractScoreboardSidebar"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderScoreboardSidebar(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        try {
            if (Night.MODULE_MANAGER != null && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).scoreboard.getValue()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @WrapWithCondition(method={"extractCameraOverlays"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/Hud;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V")})
    private boolean renderTextureOverlay(Hud hud, GuiGraphicsExtractor guiGraphicsExtractor, Identifier identifier, float alpha) {
        if (identifier.equals((Object)POWDER_SNOW_OUTLINE_LOCATION)) {
            return !((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() || !((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).snowOverlay.getValue();
        }
        if (identifier.getPath().contains("pumpkin")) {
            return !((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() || !((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).pumpkinOverlay.getValue();
        }
        return true;
    }

    @Inject(method={"extractChat"}, at={@At(value="HEAD")})
    private void night$onExtractChatHead(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        try {
            ChatManager.isInsideHudExtractChat = true;
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractChat"}, at={@At(value="RETURN")})
    private void night$onExtractChatReturn(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        try {
            ChatManager.isInsideHudExtractChat = false;
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Redirect(method={"extractItemHotbar"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;")})
    private ItemStack night$grimHotbarVisual(Inventory inventory, int slot) {
        ItemStack itemStack = inventory.getItem(slot);
        if (Night.MODULE_MANAGER == null) {
            return itemStack;
        }
        ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        if (elytraFlyModule.getGrimParkedSlot() == slot) {
            return elytraFlyModule.getGrimParkedDisplaced();
        }
        return itemStack;
    }

    @Redirect(method={"extractSlot"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V")})
    private void night$extractSlotCached(GuiGraphicsExtractor guiGraphicsExtractor, LivingEntity livingEntity, ItemStack itemStack, int x, int y, int seed) {
        HotbarCache.extractItem((GuiGraphicsExtractor)guiGraphicsExtractor, (LivingEntity)livingEntity, (ItemStack)itemStack, (int)x, (int)y, (int)seed);
    }

    @Inject(method={"clearCache"}, at={@At(value="HEAD")})
    private void night$onClearCache(CallbackInfo callbackInfo) {
        try {
            HotbarCache.clear();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"onDisconnected"}, at={@At(value="HEAD")})
    private void night$onDisconnected(CallbackInfo callbackInfo) {
        try {
            HotbarCache.clear();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

