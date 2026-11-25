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
            int buttonY;
            int buttonX;
            int buttonWidth = 75;
            int buttonHeight = 20;
            AbstractWidget viaWidget = null;
            for (GuiEventListener guiEventListener : this.children()) {
                if (!(guiEventListener instanceof AbstractWidget)) continue;
                AbstractWidget widget = (AbstractWidget)guiEventListener;
                String className = widget.getClass().getName().toLowerCase();
                String messageText = widget.getMessage() != null ? widget.getMessage().getString().toLowerCase() : "";
                String unusedMessageText = messageText;
                if (!className.contains("viafabric") && !messageText.contains("viafabric") && !messageText.contains("viaversion")) continue;
                viaWidget = widget;
                break;
            }
            if (viaWidget != null) {
                buttonX = viaWidget.getX() - buttonWidth - 4;
                buttonY = viaWidget.getY();
                buttonHeight = viaWidget.getHeight() > 0 ? viaWidget.getHeight() : 20;
            } else {
                buttonX = this.width - buttonWidth - 6;
                buttonY = 5;
            }
            this.addRenderableWidget(AccountButton.create((int)buttonX, (int)buttonY, (int)buttonWidth, (int)buttonHeight, (Screen)this));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

