/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.core.MenuModule
 *  lightning.utils.color.ColorUtils
 *  lightning.utils.graphics.Renderer2D
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.LevelLoadingScreen
 *  net.minecraft.client.gui.screens.LevelLoadingScreen$Reason
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import java.awt.Color;
import lightning.Night;
import lightning.modules.impl.core.MenuModule;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelLoadingScreen.class})
public class DownloadingTerrainScreenMixin
extends Screen {
    @Shadow
    @Final
    private LevelLoadingScreen.Reason reason;

    protected DownloadingTerrainScreenMixin(Component component) {
        super(component);
    }

    @Inject(method={"extractBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, CallbackInfo callbackInfo) {
        try {
            if (this.reason.equals((Object)LevelLoadingScreen.Reason.OTHER) && ((MenuModule)Night.MODULE_MANAGER.getModule(MenuModule.class)).isToggled() && ((MenuModule)Night.MODULE_MANAGER.getModule(MenuModule.class)).mainMenu.getValue()) {
                Renderer2D.renderQuad((GuiGraphicsExtractor)guiGraphicsExtractor, (float)0.0f, (float)0.0f, (float)this.width, (float)this.height, (Color)new Color(25, 25, 25, 255));
                for (int i = 0; i < this.width; ++i) {
                    Color color = ColorUtils.getRainbow((long)2L, (float)0.7f, (float)1.0f, (int)255, (long)((long)i * 5L));
                    Renderer2D.renderQuad((GuiGraphicsExtractor)guiGraphicsExtractor, (float)i, (float)0.0f, (float)(i + 1), (float)1.0f, (Color)color);
                }
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

