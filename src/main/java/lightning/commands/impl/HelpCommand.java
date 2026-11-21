/*
 * Decompiled with CFR 0.152.
 */
package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name="help", tag="Help", description="Shows you a list of all of the client's commands or information about a certain command.", syntax="empty | <[command]>", aliases={"cmds"})
public class HelpCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length != 0) {
            return List.of();
        }
        return Night.COMMAND_MANAGER.getCommands().stream().map(Command::getName).toList();
    }

    @Override
    public void execute(String[] stringArray) {
        if (stringArray.length == 1) {
            Command command = Night.COMMAND_MANAGER.getCommand(stringArray[0]);
            if (command == null) {
                Night.CHAT_MANAGER.tagged("Could not find the command specified.", this.getTag(), this.getName());
                return;
            }
            Night.CHAT_MANAGER.info(command.getTag() + String.valueOf(ChatUtils.getPrimary()) + " - " + String.valueOf(ChatUtils.getSecondary()) + command.getName() + " " + command.getSyntax());
            Night.CHAT_MANAGER.info(command.getDescription());
        } else if (stringArray.length == 0) {
            ArrayList<Command> arrayList = Night.COMMAND_MANAGER.getCommands();
            if (arrayList.isEmpty()) {
                Night.CHAT_MANAGER.tagged("There are currently no registered commands.", this.getTag(), this.getName() + "-list");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                int n = 0;
                for (Command command : arrayList) {
                    stringBuilder.append(ChatUtils.getSecondary()).append(command.getName()).append(++n == arrayList.size() ? "" : ", ");
                }
                Night.CHAT_MANAGER.message("Commands " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + arrayList.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(stringBuilder), this.getName() + "-list");
            }
        } else {
            this.messageSyntax();
        }
    }
}

