/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  lightning.Night
 *  lightning.events.Event
 *  lightning.events.impl.ChatInputEvent
 *  lightning.events.impl.CommandInputEvent
 *  lightning.modules.impl.core.HUDModule
 *  lightning.modules.impl.core.IRCModule
 *  lightning.utils.color.ColorUtils
 *  lightning.utils.graphics.Renderer2D
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import java.awt.Color;
import lightning.Night;
import lightning.events.Event;
import lightning.events.impl.ChatInputEvent;
import lightning.events.impl.CommandInputEvent;
import lightning.modules.impl.core.HUDModule;
import lightning.modules.impl.core.IRCModule;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChatScreen.class})
public class ChatScreenMixin
extends Screen {
    @Shadow
    protected EditBox input;

    protected ChatScreenMixin(Component component) {
        super(component);
    }

    @Inject(method={"handleChatInput"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V")}, cancellable=true)
    private void sendMessage(String string, boolean bl, CallbackInfo callbackInfo) {
        try {
            ChatInputEvent chatInputEvent = new ChatInputEvent(string);
            Night.EVENT_HANDLER.post((Event)chatInputEvent);
            if (chatInputEvent.isCancelled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"handleChatInput"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V")}, cancellable=true)
    private void sendCommand(String string, boolean bl, CallbackInfo callbackInfo) {
        try {
            CommandInputEvent commandInputEvent = new CommandInputEvent(string);
            Night.EVENT_HANDLER.post((Event)commandInputEvent);
            if (commandInputEvent.isCancelled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @WrapWithCondition(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V")})
    private boolean render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3, int n4, int n5) {
        return !((HUDModule)Night.MODULE_MANAGER.getModule(HUDModule.class)).isToggled();
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void night$renderIrcOutline(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, CallbackInfo callbackInfo) {
        try {
            if (this.input == null) {
                return;
            }
            String string = this.input.getValue();
            IRCModule iRCModule = (IRCModule)Night.MODULE_MANAGER.getModule(IRCModule.class);
            if (iRCModule == null || !iRCModule.isToggled()) {
                return;
            }
            String string2 = iRCModule.prefix.getValue();
            if (string2 == null || string2.isEmpty()) {
                string2 = "$";
            }
            if (string != null && string.startsWith(string2)) {
                int n3 = this.input.getX() - 2;
                int n4 = this.input.getY() - 2;
                int n5 = this.input.getX() + this.input.getWidth() + 2;
                int n6 = this.input.getY() + this.input.getHeight() + 2;
                Color color = ColorUtils.getGlobalColor();
                Renderer2D.renderOutline((GuiGraphicsExtractor)guiGraphicsExtractor, (float)((float)n3 - 1.0f), (float)((float)n4 - 1.0f), (float)((float)n5 + 1.0f), (float)((float)n6 + 1.0f), (Color)new Color(color.getRed(), color.getGreen(), color.getBlue(), 90));
                Renderer2D.renderOutline((GuiGraphicsExtractor)guiGraphicsExtractor, (float)n3, (float)n4, (float)n5, (float)n6, (Color)color);
                Renderer2D.renderQuad((GuiGraphicsExtractor)guiGraphicsExtractor, (float)n3, (float)n4, (float)n5, (float)n6, (Color)new Color(color.getRed(), color.getGreen(), color.getBlue(), 35));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

