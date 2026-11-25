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
    private void night$preventChatOverGui(GuiGraphicsExtractor guiGraphicsExtractor, Font font, int currentTick, int mouseX, int mouseY, ChatComponent.DisplayMode displayMode, boolean isChatFocused, CallbackInfo callbackInfo) {
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
        int xOffset = betterChatModule.isToggled() ? betterChatModule.offset.getValue().intValue() : 0;
        chatGraphicsAccess.updatePose(matrix3x2f -> {
            consumer.accept((Matrix3x2f)matrix3x2f);
            if (xOffset != 0) {
                matrix3x2f.translate((float)xOffset, 0.0f);
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
    private static int night$backgroundColor(float alpha) {
        BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
        if (betterChatModule.isToggled() && betterChatModule.background.getValue().equalsIgnoreCase("Custom")) {
            return ARGB.color((int)Math.round(alpha * 255.0f), (int)(betterChatModule.color.getColor().getRGB() & 0xFFFFFF));
        }
        return ARGB.black((float)alpha);
    }

    @ModifyArgs(method={"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/chat/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V")})
    private void addMessage(Args args, Component component, MessageSignature messageSignature, GuiMessageSource guiMessageSource, GuiMessageTag guiMessageTag) {
        try {
            String messageText;
            boolean isSender;
            String playerName;
            BetterChatModule betterChatModule = (BetterChatModule)Night.MODULE_MANAGER.getModule(BetterChatModule.class);
            if (!betterChatModule.isToggled()) {
                return;
            }
            Component modifiedComponent = component;
            Minecraft minecraft = Minecraft.getInstance();
            if (betterChatModule.highlight.getValue() && minecraft.player != null && (playerName = minecraft.player.getGameProfile().name()) != null && !playerName.isEmpty() && !(isSender = ChatHudMixin.night$isSender(messageText = component.getString(), playerName)) && ChatHudMixin.night$containsWord(messageText.toLowerCase(), playerName.toLowerCase())) {
                if (betterChatModule.highlightSound.getValue()) {
                    minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
                modifiedComponent = ChatHudMixin.night$highlightMessage(modifiedComponent, playerName, betterChatModule.highlightColor.getColor());
            }
            if (betterChatModule.timestamps.getValue()) {
                modifiedComponent = Component.literal((String)"").append((Component)Component.literal((String)(String.valueOf(FormattingUtils.getFormatting((String)((CommandsModule)Night.MODULE_MANAGER.getModule(CommandsModule.class)).secondaryWatermarkColor.getValue())) + betterChatModule.opening.getValue() + String.valueOf(FormattingUtils.getFormatting((String)((CommandsModule)Night.MODULE_MANAGER.getModule(CommandsModule.class)).primaryWatermarkColor.getValue())) + new SimpleDateFormat("HH:mm").format(new Date()) + String.valueOf(FormattingUtils.getFormatting((String)((CommandsModule)Night.MODULE_MANAGER.getModule(CommandsModule.class)).secondaryWatermarkColor.getValue())) + betterChatModule.closing.getValue() + String.valueOf(ChatFormatting.RESET) + " "))).append(modifiedComponent);
            }
            args.set(1, (Object)modifiedComponent);
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    private static boolean night$isSender(String message, String playerName) {
        String prefixSubstring;
        int closeBracketIdx;
        if (message == null || playerName == null || playerName.isEmpty()) {
            return false;
        }
        String lowerMessage = message.toLowerCase();
        String lowerPlayerName = playerName.toLowerCase();
        if (lowerMessage.startsWith("you whisper to") || lowerMessage.startsWith("to ")) {
            return true;
        }
        int separatorIdx = -1;
        if (message.startsWith("<") && (closeBracketIdx = message.indexOf(62)) != -1) {
            separatorIdx = closeBracketIdx;
        }
        if (separatorIdx == -1) {
            String[] separators = new String[]{": ", " \u00bb ", " > ", " -> ", ":"};
            for (String separator : separators) {
                int idx = message.indexOf(separator);
                if (idx == -1 || separatorIdx != -1 && idx >= separatorIdx) continue;
                separatorIdx = idx;
            }
        }
        return separatorIdx != -1 && ChatHudMixin.night$containsWord((prefixSubstring = message.substring(0, separatorIdx)).toLowerCase(), lowerPlayerName);
    }

    private static boolean night$containsWord(String text, String word) {
        if (text == null || word == null) {
            return false;
        }
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            boolean startBoundary = idx == 0 || !ChatHudMixin.night$isWordChar(text.charAt(idx - 1));
            boolean endBoundary = idx + word.length() == text.length() || !ChatHudMixin.night$isWordChar(text.charAt(idx + word.length()));
            boolean unusedEndBoundary = endBoundary;
            if (startBoundary && endBoundary) {
                return true;
            }
            idx += word.length();
        }
        return false;
    }

    private static boolean night$isWordChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }

    private static Component night$highlightMessage(Component component, String targetWord, Color color) {
        TextColor textColor = TextColor.fromRgb((int)(color.getRGB() & 0xFFFFFF));
        Style highlightStyle = Style.EMPTY.withColor(textColor);
        MutableComponent mutableComponent = Component.empty();
        component.visit((partStyle, partText) -> {
            if (partText == null || partText.isEmpty()) {
                return Optional.empty();
            }
            String lowerPart = partText.toLowerCase();
            String lowerTarget = targetWord.toLowerCase();
            int pos = 0;
            while (pos < partText.length()) {
                int matchIdx = lowerPart.indexOf(lowerTarget, pos);
                if (matchIdx == -1) {
                    mutableComponent.append((Component)Component.literal((String)partText.substring(pos)).withStyle(partStyle));
                    break;
                }
                boolean startBoundary = matchIdx == 0 || !ChatHudMixin.night$isWordChar(partText.charAt(matchIdx - 1));
                boolean endBoundary = matchIdx + lowerTarget.length() == partText.length() || !ChatHudMixin.night$isWordChar(partText.charAt(matchIdx + lowerTarget.length()));
                boolean unusedEndBoundary = endBoundary;
                if (startBoundary && endBoundary) {
                    if (matchIdx > pos) {
                        mutableComponent.append((Component)Component.literal((String)partText.substring(pos, matchIdx)).withStyle(partStyle));
                    }
                    mutableComponent.append((Component)Component.literal((String)partText.substring(matchIdx, matchIdx + lowerTarget.length())).withStyle(highlightStyle));
                    pos = matchIdx + lowerTarget.length();
                    continue;
                }
                mutableComponent.append((Component)Component.literal((String)partText.substring(pos, matchIdx + 1)).withStyle(partStyle));
                pos = matchIdx + 1;
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

