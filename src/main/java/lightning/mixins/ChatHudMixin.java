/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.managers.ChatManager
 *  lightning.modules.impl.core.CommandsModule
 *  lightning.modules.impl.miscellaneous.BetterChatModule
 *  lightning.utils.text.FormattingUtils
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.OptionInstance
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.ChatComponent
 *  net.minecraft.client.gui.components.ChatComponent$ChatGraphicsAccess
 *  net.minecraft.client.gui.components.ChatComponent$DisplayMode
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.multiplayer.chat.GuiMessage
 *  net.minecraft.client.multiplayer.chat.GuiMessage$Line
 *  net.minecraft.client.multiplayer.chat.GuiMessageSource
 *  net.minecraft.client.multiplayer.chat.GuiMessageTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MessageSignature
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.ARGB
 *  org.joml.Matrix3x2f
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package lightning.mixins;

import java.awt.Color;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import lightning.Night;
import lightning.managers.ChatManager;
import lightning.modules.impl.core.CommandsModule;
import lightning.modules.impl.miscellaneous.BetterChatModule;
import lightning.utils.text.FormattingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={ChatComponent.class})
public abstract class ChatHudMixin {
    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Inject(method={"addMessageToDisplayQueue"}, at={@At(value="TAIL")})
    private void night$trackAnimation(GuiMessage guiMessage, CallbackInfo callbackInfo) {
        try {
            BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
            if (betterChatModule.isToggled() && betterChatModule.animation.getValue() && !this.trimmedMessages.isEmpty()) {
                betterChatModule.getAnimationMap().put(this.trimmedMessages.getFirst(), System.currentTimeMillis());
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$preventChatOverGui(GuiGraphicsExtractor guiGraphicsExtractor, Font font, int n, int n2, int n3, ChatComponent.DisplayMode displayMode, boolean bl, CallbackInfo callbackInfo) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gui != null && minecraft.gui.screen() != null && !(minecraft.gui.screen() instanceof ChatScreen) && !ChatManager.isInsideHudExtractChat) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"}, at={@At(value="TAIL")})
    private void night$cleanupAnimations(CallbackInfo callbackInfo) {
        try {
            BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
            if (betterChatModule.isToggled() && betterChatModule.animation.getValue()) {
                betterChatModule.getAnimationMap().entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > (long)betterChatModule.delay.getValue().intValue());
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Redirect(method={"extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;updatePose(Ljava/util/function/Consumer;)V")})
    private void night$applyOffset(ChatComponent.ChatGraphicsAccess chatGraphicsAccess, Consumer<Matrix3x2f> consumer) {
        BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        int n = betterChatModule.isToggled() ? betterChatModule.offset.getValue().intValue() : 0;
        chatGraphicsAccess.updatePose(matrix3x2f -> {
            consumer.accept((Matrix3x2f)matrix3x2f);
            if (n != 0) {
                matrix3x2f.translate((float)n, 0.0f);
            }
        });
    }

    @Redirect(method={"extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal=0)})
    private Object night$textOpacity(OptionInstance<?> optionInstance) {
        BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (betterChatModule.isToggled()) {
            return betterChatModule.textAlpha.getValue().doubleValue() / 255.0;
        }
        return optionInstance.get();
    }

    @Redirect(method={"extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal=1)})
    private Object night$backgroundOpacity(OptionInstance<?> optionInstance) {
        BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (betterChatModule.isToggled()) {
            return switch (betterChatModule.background.getValue()) {
                case "Clear" -> 0.0;
                case "Custom" -> (double)betterChatModule.color.getColor().getAlpha() / 255.0;
                default -> optionInstance.get();
            };
        }
        return optionInstance.get();
    }

    @Redirect(method={"lambda$extractRenderState$1(IILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IFLnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/ARGB;black(F)I")})
    private static int night$backgroundColor(float f) {
        BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (betterChatModule.isToggled() && betterChatModule.background.getValue().equalsIgnoreCase("Custom")) {
            return ARGB.color((int)Math.round(f * 255.0f), (int)(betterChatModule.color.getColor().getRGB() & 0xFFFFFF));
        }
        return ARGB.black((float)f);
    }

    @ModifyArgs(method={"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/chat/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V")})
    private void addMessage(Args args, Component component, MessageSignature messageSignature, GuiMessageSource guiMessageSource, GuiMessageTag guiMessageTag) {
        try {
            String string;
            boolean bl;
            String string2;
            BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
            if (!betterChatModule.isToggled()) {
                return;
            }
            Component component2 = component;
            Minecraft minecraft = Minecraft.getInstance();
            if (betterChatModule.highlight.getValue() && minecraft.player != null && (string2 = minecraft.player.getGameProfile().name()) != null && !string2.isEmpty() && !(bl = ChatHudMixin.night$isSender(string = component.getString(), string2)) && ChatHudMixin.night$containsWord(string.toLowerCase(), string2.toLowerCase())) {
                if (betterChatModule.highlightSound.getValue()) {
                    minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
                component2 = ChatHudMixin.night$highlightMessage(component2, string2, betterChatModule.highlightColor.getColor());
            }
            if (betterChatModule.timestamps.getValue()) {
                component2 = Component.literal((String)"").append((Component)Component.literal((String)(String.valueOf(FormattingUtils.getFormatting((String)((CommandsModule)Night.MODULE_MANAGER.getModule(CommandsModule.class)).secondaryWatermarkColor.getValue())) + betterChatModule.opening.getValue() + String.valueOf(FormattingUtils.getFormatting((String)((CommandsModule)Night.MODULE_MANAGER.getModule(CommandsModule.class)).primaryWatermarkColor.getValue())) + new SimpleDateFormat("HH:mm").format(new Date()) + String.valueOf(FormattingUtils.getFormatting((String)((CommandsModule)Night.MODULE_MANAGER.getModule(CommandsModule.class)).secondaryWatermarkColor.getValue())) + betterChatModule.closing.getValue() + String.valueOf(ChatFormatting.RESET) + " "))).append(component2);
            }
            args.set(1, (Object)component2);
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    private static boolean night$isSender(String string, String string2) {
        String string3;
        int n;
        if (string == null || string2 == null || string2.isEmpty()) {
            return false;
        }
        String string4 = string.toLowerCase();
        String string5 = string2.toLowerCase();
        if (string4.startsWith("you whisper to") || string4.startsWith("to ")) {
            return true;
        }
        int n2 = -1;
        if (string.startsWith("<") && (n = string.indexOf(62)) != -1) {
            n2 = n;
        }
        if (n2 == -1) {
            String[] stringArray = new String[]{": ", " \u00bb ", " > ", " -> ", ":"};
            for (String string6 : stringArray) {
                int n3 = string.indexOf(string6);
                if (n3 == -1 || n2 != -1 && n3 >= n2) continue;
                n2 = n3;
            }
        }
        return n2 != -1 && ChatHudMixin.night$containsWord((string3 = string.substring(0, n2)).toLowerCase(), string5);
    }

    private static boolean night$containsWord(String string, String string2) {
        if (string == null || string2 == null) {
            return false;
        }
        int n = 0;
        while ((n = string.indexOf(string2, n)) != -1) {
            boolean bl = n == 0 || !ChatHudMixin.night$isWordChar(string.charAt(n - 1));
            boolean bl2 = n + string2.length() == string.length() || !ChatHudMixin.night$isWordChar(string.charAt(n + string2.length()));
            boolean bl3 = bl2;
            if (bl && bl2) {
                return true;
            }
            n += string2.length();
        }
        return false;
    }

    private static boolean night$isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static Component night$highlightMessage(Component component, String string, Color color) {
        TextColor textColor = TextColor.fromRgb((int)(color.getRGB() & 0xFFFFFF));
        Style style = Style.EMPTY.withColor(textColor);
        MutableComponent mutableComponent = Component.empty();
        component.visit((style2, string2) -> {
            if (string2 == null || string2.isEmpty()) {
                return Optional.empty();
            }
            String string3 = string2.toLowerCase();
            String string4 = string.toLowerCase();
            int n = 0;
            while (n < string2.length()) {
                int n2 = string3.indexOf(string4, n);
                if (n2 == -1) {
                    mutableComponent.append((Component)Component.literal((String)string2.substring(n)).withStyle(style2));
                    break;
                }
                boolean bl = n2 == 0 || !ChatHudMixin.night$isWordChar(string2.charAt(n2 - 1));
                boolean bl2 = n2 + string4.length() == string2.length() || !ChatHudMixin.night$isWordChar(string2.charAt(n2 + string4.length()));
                boolean bl3 = bl2;
                if (bl && bl2) {
                    if (n2 > n) {
                        mutableComponent.append((Component)Component.literal((String)string2.substring(n, n2)).withStyle(style2));
                    }
                    mutableComponent.append((Component)Component.literal((String)string2.substring(n2, n2 + string4.length())).withStyle(style));
                    n = n2 + string4.length();
                    continue;
                }
                mutableComponent.append((Component)Component.literal((String)string2.substring(n, n2 + 1)).withStyle(style2));
                n = n2 + 1;
            }
            return Optional.empty();
        }, Style.EMPTY);
        return mutableComponent;
    }

    @Inject(method={"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$hideInventoryFull(Component component, MessageSignature messageSignature, GuiMessageSource guiMessageSource, GuiMessageTag guiMessageTag, CallbackInfo callbackInfo) {
        if (component != null && Normalizer.normalize(component.getString(), Normalizer.Form.NFC).toLowerCase(Locale.ROOT).contains("t\u00fai \u0111\u1ed3 c\u1ee7a b\u1ea1n \u0111\u00e3 \u0111\u1ea7y")) {
            callbackInfo.cancel();
        }
    }
}

