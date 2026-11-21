/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.ChatHudAccessor
 *  lightning.utils.mixins.IChatHudLine
 *  lightning.utils.mixins.IChatHudLineVisible
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.components.ComponentRenderUtils
 *  net.minecraft.client.multiplayer.chat.GuiMessage
 *  net.minecraft.client.multiplayer.chat.GuiMessage$Line
 *  net.minecraft.client.multiplayer.chat.GuiMessageSource
 *  net.minecraft.client.multiplayer.chat.GuiMessageTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Mth
 */
package lightning.managers;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.ChatHudAccessor;
import lightning.modules.impl.core.CommandsModule;
import lightning.modules.impl.miscellaneous.BetterChatModule;
import lightning.utils.IMinecraft;
import lightning.utils.chat.ChatUtils;
import lightning.utils.mixins.IChatHudLine;
import lightning.utils.mixins.IChatHudLineVisible;
import lightning.utils.text.FormattingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class ChatManager
implements IMinecraft {
    public static boolean isInsideHudExtractChat = false;
    private final List<String> awaitMessages = new ArrayList<String>();

    public ChatManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        if (this.awaitMessages.isEmpty()) {
            return;
        }
        for (String string : new ArrayList<String>(this.awaitMessages)) {
            this.addMessage(string);
            this.awaitMessages.remove(string);
        }
    }

    public void message(String string) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void message(String string, String string2) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        ChatManager.deleteMessage(string2);
        this.addMessage(this.getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + string, string2);
    }

    public void tagged(String string, String string2) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + string2 + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void tagged(String string, String string2, String string3) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        ChatManager.deleteMessage(string3);
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + string2 + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + string, string3);
    }

    public void info$await(String string) {
        this.awaitMessages.add(this.getWatermark() + String.valueOf(ChatFormatting.DARK_BLUE) + " [" + String.valueOf(ChatFormatting.BLUE) + "?" + String.valueOf(ChatFormatting.DARK_BLUE) + "] " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void info(String string) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.DARK_BLUE) + " [" + String.valueOf(ChatFormatting.BLUE) + "?" + String.valueOf(ChatFormatting.DARK_BLUE) + "] " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void warn$await(String string) {
        this.awaitMessages.add(this.getWatermark() + String.valueOf(ChatFormatting.GOLD) + " [" + String.valueOf(ChatFormatting.YELLOW) + "!" + String.valueOf(ChatFormatting.GOLD) + "] " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void warn(String string) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.GOLD) + " [" + String.valueOf(ChatFormatting.YELLOW) + "!" + String.valueOf(ChatFormatting.GOLD) + "] " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void error$await(String string) {
        this.awaitMessages.add(this.getWatermark() + String.valueOf(ChatFormatting.DARK_RED) + " [" + String.valueOf(ChatFormatting.RED) + "!!" + String.valueOf(ChatFormatting.DARK_RED) + "] " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void error(String string) {
        if (ChatManager.mc.player == null || ChatManager.mc.gui == null) {
            return;
        }
        this.addMessage(this.getWatermark() + String.valueOf(ChatFormatting.DARK_RED) + " [" + String.valueOf(ChatFormatting.RED) + "!!" + String.valueOf(ChatFormatting.DARK_RED) + "] " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void await(String string) {
        this.awaitMessages.add(this.getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void await(String string, String string2) {
        this.awaitMessages.add(this.getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + string2 + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + string);
    }

    public void addMessage(String string) {
        this.addTextMessage((Component)Component.literal((String)string), "");
    }

    public void addMessage(String string, String string2) {
        this.addTextMessage((Component)Component.literal((String)string), string2);
    }

    public void addMessage(Component component) {
        this.addTextMessage(component, "");
    }

    public void addMessage(Component component, String string) {
        this.addTextMessage(component, string);
    }

    private void addTextMessage(Component component, String string) {
        if (!mc.isSameThread()) {
            mc.execute(() -> this.addTextMessage(component, string));
            return;
        }
        GuiMessage guiMessage = new GuiMessage(ChatManager.mc.gui.hud.getGuiTicks(), component, null, GuiMessageSource.SYSTEM_CLIENT, GuiMessageTag.system());
        ((IChatHudLine)(Object)guiMessage).night$setClientMessage(true);
        ((IChatHudLine)(Object)guiMessage).night$setClientIdentifier(string);
        ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).invokeLogChatMessage(guiMessage);
        ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).invokeAddMessage(guiMessage);
        List list = ComponentRenderUtils.wrapComponents((FormattedText)guiMessage.content(), (int)Mth.floor((double)((double)((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).invokeGetWidth() / ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).invokeGetScale())), (Font)ChatManager.mc.font);
        for (int i = 0; i < list.size(); ++i) {
            FormattedCharSequence formattedCharSequence = (FormattedCharSequence)list.get(i);
            if (ChatManager.mc.gui.hud.getChat().isChatFocused() && ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getScrolledLines() > 0) {
                ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).setHasUnreadNewMessages(true);
                ChatManager.mc.gui.hud.getChat().scrollChat(1);
            }
            boolean bl = i == list.size() - 1;
            GuiMessage.Line line = new GuiMessage.Line(guiMessage, formattedCharSequence, bl);
            ((IChatHudLineVisible)(Object)line).night$setClientMessage(true);
            ((IChatHudLineVisible)(Object)line).night$setClientIdentifier(string);
            ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages().addFirst(line);
            if (!Night.MODULE_MANAGER.getModule(BetterChatModule.class).isToggled() || !Night.MODULE_MANAGER.getModule(BetterChatModule.class).animation.getValue()) continue;
            Night.MODULE_MANAGER.getModule(BetterChatModule.class).getAnimationMap().put(line, System.currentTimeMillis());
        }
        while (((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages().size() > 100) {
            ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages().removeLast();
        }
    }

    public static void deleteMessage(String string) {
        try {
            ArrayList<Object> arrayList = new ArrayList<Object>();
            for (Object object : ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getMessages()) {
                if (!((IChatHudLine)object).night$isClientMessage() || ((IChatHudLine)object).night$getClientIdentifier().isEmpty() || !((IChatHudLine)object).night$getClientIdentifier().equals(string)) continue;
                arrayList.add(object);
            }
            ArrayList arrayList2 = new ArrayList();
            for (GuiMessage.Line line : ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages()) {
                if (!((IChatHudLineVisible)(Object)line).night$isClientMessage() || ((IChatHudLineVisible)(Object)line).night$getClientIdentifier().isEmpty() || !((IChatHudLineVisible)(Object)line).night$getClientIdentifier().equals(string)) continue;
                arrayList2.add(line);
            }
            ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getMessages().removeAll(arrayList);
            ((ChatHudAccessor)ChatManager.mc.gui.hud.getChat()).getVisibleMessages().removeAll(arrayList2);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String getWatermark() {
        return this.getWatermark(null);
    }

    private String getWatermark(String string) {
        if (!Night.MODULE_MANAGER.getModule(CommandsModule.class).watermark.getValue()) {
            return "";
        }
        return String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).secondaryWatermarkColor.getValue())) + Night.MODULE_MANAGER.getModule(CommandsModule.class).opening.getValue() + String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).primaryWatermarkColor.getValue())) + (string == null ? Night.MODULE_MANAGER.getModule(CommandsModule.class).watermarkText.getValue() : string) + String.valueOf(FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).secondaryWatermarkColor.getValue())) + Night.MODULE_MANAGER.getModule(CommandsModule.class).closing.getValue();
    }
}

