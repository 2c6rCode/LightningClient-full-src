/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.core.PingBypassModule
 *  lightning.pingbypass.PingBypassFlags
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.PauseScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.core.PingBypassModule;
import lightning.pingbypass.PingBypassFlags;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PauseScreen.class})
public abstract class GameMenuScreenMixin
extends Screen {
    protected GameMenuScreenMixin(Component component) {
        super(component);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void init(CallbackInfo callbackInfo) {
        try {
            if (!PingBypassFlags.proxyForwardingActive) {
                return;
            }
            this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u00a7cDisconnect Proxy"), button -> {
                PingBypassModule pingBypassModule = (PingBypassModule)Night.MODULE_MANAGER.getModule(PingBypassModule.class);
                if (pingBypassModule != null && pingBypassModule.isToggled()) {
                    pingBypassModule.setToggled(false);
                }
            }).bounds(this.width / 2 - 102, this.height - 40, 204, 20).build());
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

