/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.RenderOverlayEvent
 *  lightning.gui.ClickGuiScreen
 *  lightning.gui.special.MainMenuScreen
 *  lightning.modules.impl.core.MenuModule
 *  lightning.modules.impl.miscellaneous.AutoRespawnModule
 *  lightning.modules.impl.miscellaneous.FakePlayerModule
 *  lightning.modules.impl.player.AutoShopModule
 *  lightning.modules.impl.player.AutoShopModule$ScreenDecision
 *  lightning.modules.impl.player.AutoShopSync
 *  lightning.utils.minecraft.WorldUtils
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.client.gui.screens.PauseScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.gui.screens.social.SocialInteractionsScreen
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.RenderOverlayEvent;
import lightning.gui.ClickGuiScreen;
import lightning.gui.special.MainMenuScreen;
import lightning.modules.impl.core.MenuModule;
import lightning.modules.impl.miscellaneous.AutoRespawnModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.modules.impl.player.AutoShopSync;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Gui.class})
public class InGameHudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift=At.Shift.AFTER)})
    private void render(DeltaTracker deltaTracker, boolean bl, boolean bl2, CallbackInfo callbackInfo, @Local(ordinal=0) GuiGraphicsExtractor guiGraphicsExtractor) {
        try {
            if (this.minecraft.gui.hud.isHidden()) {
                return;
            }
            Night.EVENT_HANDLER.post((Event)new RenderOverlayEvent(guiGraphicsExtractor, deltaTracker.getGameTimeDeltaPartialTick(true)));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$setScreen(Screen screen, CallbackInfo callbackInfo) {
        try {
            ClickGuiScreen clickGuiScreen;
            Screen screen2;
            AutoShopModule.ScreenDecision screenDecision;
            AutoShopModule autoShopModule;
            if (Night.MODULE_MANAGER != null && (autoShopModule = (AutoShopModule)Night.MODULE_MANAGER.getModule(AutoShopModule.class)) != null) {
                screenDecision = AutoShopSync.classify((AutoShopModule)autoShopModule, (Screen)screen);
                if (screenDecision == AutoShopModule.ScreenDecision.SUPPRESS_EXPECTED) {
                    callbackInfo.cancel();
                    return;
                }
                if (screenDecision == AutoShopModule.ScreenDecision.FAIL_OPEN_UNEXPECTED) {
                    return;
                }
            }
            if (screen instanceof SocialInteractionsScreen) {
                callbackInfo.cancel();
                return;
            }
            if ((screen == null || screen instanceof PauseScreen) && (screen2 = this.minecraft.gui.screen()) instanceof ClickGuiScreen && !(clickGuiScreen = (ClickGuiScreen)screen2).isReadyToRemove()) {
                clickGuiScreen.requestClose();
                callbackInfo.cancel();
                return;
            }
            if (screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen) && System.currentTimeMillis() - WorldUtils.lastInteractablePlaceTime < 500L) {
                callbackInfo.cancel();
                if (this.minecraft.getConnection() != null && this.minecraft.player != null && this.minecraft.player.containerMenu != null) {
                    this.minecraft.getConnection().send((Packet)new ServerboundContainerClosePacket(this.minecraft.player.containerMenu.containerId));
                }
                return;
            }
            if (Night.MODULE_MANAGER == null) {
                return;
            }
            if (screen instanceof DeathScreen && this.minecraft.player != null) {
                FakePlayerModule fakePlayerModule = (FakePlayerModule)Night.MODULE_MANAGER.getModule(FakePlayerModule.class);
                if (fakePlayerModule != null && fakePlayerModule.isToggled()) {
                    fakePlayerModule.setToggled(false);
                }
                AutoRespawnModule autoRespawnModule = (AutoRespawnModule)Night.MODULE_MANAGER.getModule(AutoRespawnModule.class);
                if (autoRespawnModule != null && autoRespawnModule.isToggled()) {
                    this.minecraft.player.respawn();
                    callbackInfo.cancel();
                    return;
                }
            }
            if (screen instanceof TitleScreen) {
                Night.checkForUpdates();
                MenuModule menuModule = (MenuModule)Night.MODULE_MANAGER.getModule(MenuModule.class);
                if (menuModule != null && menuModule.isToggled() && menuModule.mainMenu.getValue()) {
                    this.minecraft.gui.setScreen((Screen)new MainMenuScreen());
                    callbackInfo.cancel();
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

