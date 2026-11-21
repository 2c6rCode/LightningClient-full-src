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
    private void render$drawTextWithShadow(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, CallbackInfo callbackInfo) {
        try {
            int n3;
            if (Night.UPDATE_STATUS.equalsIgnoreCase("none")) {
                return;
            }
            String string = "";
            String string2 = "";
            Color color = Color.WHITE;
            if (Night.UPDATE_STATUS.equalsIgnoreCase("update-available")) {
                string2 = "An update is available for Lightning.";
                string = "Please restart the game to apply changes.";
                color = Color.ORANGE;
            }
            if (Night.UPDATE_STATUS.equalsIgnoreCase("failed-connection")) {
                string2 = "Failed to connect to Lightning's servers.";
                string = "Please make sure you have a working internet connection.";
                color = Color.RED;
            }
            if (Night.UPDATE_STATUS.equalsIgnoreCase("failed")) {
                string2 = "Failed to update Lightning.";
                string = "Please make sure the auto-updater is working properly.";
                color = Color.RED;
            }
            if (Night.UPDATE_STATUS.equalsIgnoreCase("up-to-date")) {
                string = "Lightning is on the latest version.";
            }
            if (string.isEmpty()) {
                return;
            }
            float f2 = 1.0f;
            if (this.fading) {
                float f4 = (float)(Util.getMillis() - this.fadeInStart) / 2000.0f;
                if (f4 <= 1.0f) {
                    f4 = Mth.clamp((float)f4, (float)0.0f, (float)1.0f);
                    f2 = Mth.clampedMap((float)f4, (float)0.5f, (float)1.0f, (float)0.0f, (float)1.0f);
                }
            }
            if (((n3 = Mth.ceil((float)(f2 * 255.0f)) << 24) & 0xFC000000) != 0) {
                if (!string2.isEmpty()) {
                    guiGraphicsExtractor.text(TitleScreenMixin.mc.font, string2, this.width / 2 - TitleScreenMixin.mc.font.width(string2) / 2, this.height - 30, color.getRGB() | n3, true);
                }
                guiGraphicsExtractor.text(TitleScreenMixin.mc.font, string, this.width / 2 - TitleScreenMixin.mc.font.width(string) / 2, this.height - 20, color.getRGB() | n3, true);
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

