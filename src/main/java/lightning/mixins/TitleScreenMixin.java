/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.gui.components.AccountButton
 *  lightning.utils.IMinecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Util
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import java.awt.Color;
import lightning.Night;
import lightning.gui.components.AccountButton;
import lightning.utils.IMinecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TitleScreen.class})
public abstract class TitleScreenMixin
extends Screen
implements IMinecraft {
    @Shadow
    private boolean fading;
    @Shadow
    private long fadeInStart;

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void init(CallbackInfo callbackInfo) {
        try {
            this.addRenderableWidget(AccountButton.create(8, 8, 75, 20, (Screen)(Object)this));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void render$drawTextWithShadow(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
        try {
            int alphaMask;
            if (Night.UPDATE_STATUS.equalsIgnoreCase("none")) {
                return;
            }
            String subText = "";
            String mainText = "";
            Color color = Color.WHITE;
            if (Night.UPDATE_STATUS.equalsIgnoreCase("update-available")) {
                mainText = "An update is available for Lightning.";
                subText = "Please restart the game to apply changes.";
                color = Color.ORANGE;
            }
            if (Night.UPDATE_STATUS.equalsIgnoreCase("failed-connection")) {
                mainText = "Failed to connect to Lightning's servers.";
                subText = "Please make sure you have a working internet connection.";
                color = Color.RED;
            }
            if (Night.UPDATE_STATUS.equalsIgnoreCase("failed")) {
                mainText = "Failed to update Lightning.";
                subText = "Please make sure the auto-updater is working properly.";
                color = Color.RED;
            }
            if (Night.UPDATE_STATUS.equalsIgnoreCase("up-to-date")) {
                subText = "Lightning is on the latest version.";
            }
            if (subText.isEmpty()) {
                return;
            }
            float fadeAlpha = 1.0f;
            if (this.fading) {
                float fadeProgress = (float)(Util.getMillis() - this.fadeInStart) / 2000.0f;
                if (fadeProgress <= 1.0f) {
                    fadeProgress = Mth.clamp((float)fadeProgress, (float)0.0f, (float)1.0f);
                    fadeAlpha = Mth.clampedMap((float)fadeProgress, (float)0.5f, (float)1.0f, (float)0.0f, (float)1.0f);
                }
            }
            if (((alphaMask = Mth.ceil((float)(fadeAlpha * 255.0f)) << 24) & 0xFC000000) != 0) {
                if (!mainText.isEmpty()) {
                    guiGraphicsExtractor.text(TitleScreenMixin.mc.font, mainText, this.width / 2 - TitleScreenMixin.mc.font.width(mainText) / 2, this.height - 30, color.getRGB() | alphaMask, true);
                }
                guiGraphicsExtractor.text(TitleScreenMixin.mc.font, subText, this.width / 2 - TitleScreenMixin.mc.font.width(subText) / 2, this.height - 20, color.getRGB() | alphaMask, true);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

