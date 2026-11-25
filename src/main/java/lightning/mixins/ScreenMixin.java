/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.gui.LightningSkin
 *  lightning.modules.impl.combat.MainhandModule
 *  lightning.modules.impl.core.MenuModule
 *  lightning.modules.impl.miscellaneous.ShulkerInfoModule
 *  lightning.modules.impl.visuals.NoRenderModule
 *  lightning.utils.IMinecraft
 *  lightning.utils.color.ColorUtils
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.world.inventory.Slot
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import java.awt.Color;
import lightning.Night;
import lightning.gui.LightningSkin;
import lightning.mixins.accessors.AbstractContainerScreenAccessor;
import lightning.modules.impl.combat.MainhandModule;
import lightning.modules.impl.core.MenuModule;
import lightning.modules.impl.miscellaneous.ShulkerInfoModule;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.IMinecraft;
import lightning.utils.color.ColorUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Screen.class})
public class ScreenMixin
implements IMinecraft {
    @Shadow
    public int width;
    @Shadow
    public int height;

    @Inject(method={"extractBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
        try {
            if (((MenuModule)Night.MODULE_MANAGER.getModule(MenuModule.class)).isToggled() && ((MenuModule)Night.MODULE_MANAGER.getModule(MenuModule.class)).mainMenu.getValue() && ScreenMixin.mc.level == null) {
                LightningSkin.screenBg((GuiGraphicsExtractor)guiGraphicsExtractor, (float)0.0f, (float)0.0f, (float)this.width, (float)this.height, (Color)new Color(25, 25, 25, 255));
                for (int i = 0; i < this.width; ++i) {
                    Color color = ColorUtils.getRainbow((long)2L, (float)0.7f, (float)1.0f, (int)255, (long)((long)i * 5L));
                    LightningSkin.topLine((GuiGraphicsExtractor)guiGraphicsExtractor, (float)i, (float)0.0f, (float)(i + 1), (float)1.0f, (Color)color);
                }
                callbackInfo.cancel();
                return;
            }
            if (((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule)Night.MODULE_MANAGER.getModule(NoRenderModule.class)).background.getValue() && ((Screen)(Object)this).isInGameUi()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$hideLegitInventory(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
        try {
            if (MainhandModule.hidingLegitInventory && (Object)this instanceof InventoryScreen) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractRenderStateWithTooltipAndSubtitles"}, at={@At(value="TAIL")})
    private void night$shulkerInfoOnTop(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
        try {
            if ((Object)this instanceof AbstractContainerScreen) {
                Slot slot;
                AbstractContainerScreen abstractContainerScreen = (AbstractContainerScreen)(Object)this;
                ShulkerInfoModule shulkerInfoModule = (ShulkerInfoModule)Night.MODULE_MANAGER.getModule(ShulkerInfoModule.class);
                if (shulkerInfoModule != null && shulkerInfoModule.isToggled() && (slot = ((AbstractContainerScreenAccessor)abstractContainerScreen).getHoveredSlot()) != null && !slot.getItem().isEmpty() && abstractContainerScreen.getMenu().getCarried().isEmpty() && shulkerInfoModule.hasItems(slot.getItem())) {
                    shulkerInfoModule.renderInfo(guiGraphicsExtractor, mouseX, mouseY, slot.getItem());
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

