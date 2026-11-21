/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.gui.components.AccountButton
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.gui.components.AccountButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={JoinMultiplayerScreen.class}, priority=10000)
public abstract class JoinMultiplayerScreenMixin
extends Screen {
    protected JoinMultiplayerScreenMixin(Component component) {
        super(component);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void init(CallbackInfo callbackInfo) {
        try {
            int n;
            int n2;
            int n3 = 75;
            int n4 = 20;
            AbstractWidget abstractWidget = null;
            for (GuiEventListener guiEventListener : this.children()) {
                if (!(guiEventListener instanceof AbstractWidget)) continue;
                AbstractWidget abstractWidget2 = (AbstractWidget)guiEventListener;
                String string = abstractWidget2.getClass().getName().toLowerCase();
                String string2 = abstractWidget2.getMessage() != null ? abstractWidget2.getMessage().getString().toLowerCase() : "";
                String string3 = string2;
                if (!string.contains("viafabric") && !string2.contains("viafabric") && !string2.contains("viaversion")) continue;
                abstractWidget = abstractWidget2;
                break;
            }
            if (abstractWidget != null) {
                n2 = abstractWidget.getX() - n3 - 4;
                n = abstractWidget.getY();
                n4 = abstractWidget.getHeight() > 0 ? abstractWidget.getHeight() : 20;
            } else {
                n2 = this.width - n3 - 6;
                n = 5;
            }
            this.addRenderableWidget(AccountButton.create((int)n2, (int)n, (int)n3, (int)n4, (Screen)this));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

