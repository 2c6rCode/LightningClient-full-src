/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package lightning.modules.impl.core;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.chat.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@RegisterModule(name="Friend", description="Allows you to manage the client's friend system.", category=Module.Category.CORE, persistent=true, drawn=false)
public class FriendModule
extends Module {
    public BooleanSetting middleClick = new BooleanSetting("MiddleClick", "Adds whichever entity you middle click to your friends list.", true);
    public BooleanSetting friendlyFire = new BooleanSetting("FriendlyFire", "Disables the friend system and allows the attacking of friends.", false);
    public BooleanSetting friendMessage = new BooleanSetting("FriendMessage", "Sends a message to a player whenever you add them to your friends list.", false);
    public StringSetting content = new StringSetting("Content", "The message that will be sent to the people you add. $player is placeholder for their name.", new BooleanSetting.Visibility(this.friendMessage, true), "/msg $player I have just added you to my friends list!");

    public void sendFriendMessage(String playerName) {
        if (FriendModule.mc.level == null || FriendModule.mc.player == null) {
            return;
        }
        if (!this.friendMessage.getValue()) {
            return;
        }
        if (mc.getConnection().getOnlinePlayers().stream().noneMatch(playerInfo -> playerInfo.getProfile().name().equalsIgnoreCase(playerName))) {
            return;
        }
        if (this.content.getValue().startsWith("/")) {
            mc.getConnection().sendCommand(this.content.getValue().substring(1).replace("$player", playerName));
        } else {
            mc.getConnection().sendChat(this.content.getValue().replace("$player", playerName));
        }
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent settingChangeEvent) {
        if (settingChangeEvent.getSetting().equals(this.friendlyFire)) {
            Night.CHAT_MANAGER.warn(String.valueOf(ChatUtils.getPrimary()) + "Friendly fire" + String.valueOf(ChatUtils.getSecondary()) + " has been turned " + (this.friendlyFire.getValue() ? String.valueOf(ChatFormatting.GREEN) + "on" + String.valueOf(ChatFormatting.RESET) + ". Friends will now be attacked." : String.valueOf(ChatFormatting.RED) + "off" + String.valueOf(ChatFormatting.RESET) + ". Friends will now no longer be attacked."));
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent mouseInputEvent) {
        if (!this.middleClick.getValue() || FriendModule.mc.gui != null && FriendModule.mc.gui.screen() != null) {
            return;
        }
        if (mouseInputEvent.getButton() != 2) {
            return;
        }
        if (FriendModule.mc.crosshairPickEntity == null) {
            return;
        }
        Entity entity = FriendModule.mc.crosshairPickEntity;
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        String playerName = player.getGameProfile().name();
        if (Night.FRIEND_MANAGER.contains(playerName)) {
            Night.FRIEND_MANAGER.remove(playerName);
            Night.CHAT_MANAGER.tagged("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + playerName + String.valueOf(ChatUtils.getSecondary()) + " from your friends list.", "MCF", this.getName());
        } else {
            Night.FRIEND_MANAGER.add(playerName);
            Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + playerName + String.valueOf(ChatUtils.getSecondary()) + " to your friends list.", "MCF", this.getName());
        }
    }
}

