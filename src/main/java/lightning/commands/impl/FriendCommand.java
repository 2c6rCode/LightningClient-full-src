/*
 * Decompiled with CFR 0.152.
 */
package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.impl.core.FriendModule;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name="friend", tag="Friend", description="Allows you to manage the client's friend list.", syntax="<add|del> <[player]> | <clear|list>", aliases={"f", "friends"})
public class FriendCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            return List.of("add", "del", "clear", "list");
        }
        if (stringArray.length == 1 && stringArray[0].equalsIgnoreCase("del")) {
            return Night.FRIEND_MANAGER.getFriends();
        }
        if (stringArray.length == 1 && stringArray[0].equalsIgnoreCase("add") && FriendCommand.mc.level != null && FriendCommand.mc.player != null) {
            return FriendCommand.mc.level.players().stream().filter(abstractClientPlayer -> abstractClientPlayer != FriendCommand.mc.player).map(abstractClientPlayer -> abstractClientPlayer.getName().getString()).filter(string -> !Night.FRIEND_MANAGER.contains((String)string)).toList();
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        if (stringArray.length == 2) {
            switch (stringArray[0].toLowerCase()) {
                case "add": {
                    if (!Night.FRIEND_MANAGER.contains(stringArray[1])) {
                        if (Night.MODULE_MANAGER.getModule(FriendModule.class).friendMessage.getValue()) {
                            Night.FRIEND_MANAGER.sendFriendMessage(stringArray[1]);
                        }
                        Night.FRIEND_MANAGER.add(stringArray[1]);
                        Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + String.valueOf(ChatUtils.getSecondary()) + " to your friends list.", this.getTag(), this.getName());
                        break;
                    }
                    Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + String.valueOf(ChatUtils.getSecondary()) + " is already on your friends list.", this.getTag(), this.getName());
                    break;
                }
                case "del": {
                    if (Night.FRIEND_MANAGER.contains(stringArray[1])) {
                        Night.FRIEND_MANAGER.remove(stringArray[1]);
                        Night.CHAT_MANAGER.tagged("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + String.valueOf(ChatUtils.getSecondary()) + " from your friends list.", this.getTag(), this.getName());
                        break;
                    }
                    Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + String.valueOf(ChatUtils.getSecondary()) + " is not on your friends list.", this.getTag(), this.getName());
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else if (stringArray.length == 1) {
            switch (stringArray[0].toLowerCase()) {
                case "clear": {
                    Night.FRIEND_MANAGER.clear();
                    Night.CHAT_MANAGER.tagged("Successfully cleared your friends list.", this.getTag(), this.getName() + "-list");
                    break;
                }
                case "list": {
                    ArrayList<String> arrayList = Night.FRIEND_MANAGER.getFriends();
                    if (arrayList.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("You currently have no friends.", this.getTag());
                        break;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    int n = 0;
                    for (String string : arrayList) {
                        stringBuilder.append(ChatUtils.getSecondary()).append(string).append(++n == arrayList.size() ? "" : ", ");
                    }
                    Night.CHAT_MANAGER.message("Friends " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + arrayList.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(stringBuilder), this.getName() + "-list");
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else {
            this.messageSyntax();
        }
    }
}

