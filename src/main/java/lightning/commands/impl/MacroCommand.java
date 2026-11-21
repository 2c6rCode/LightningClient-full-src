/*
 * Decompiled with CFR 0.152.
 */
package lightning.commands.impl;

import java.util.Arrays;
import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.utils.chat.ChatUtils;
import lightning.utils.input.KeyboardUtils;

@RegisterCommand(name="macro", tag="Macro", description="Allows you to manage the client's macro system.", syntax="add <[key]> <[message]> | remove <[key]> | <clear|list>")
public class MacroCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            return List.of("add", "remove", "clear", "list");
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        if (stringArray.length >= 3) {
            if (stringArray[0].equalsIgnoreCase("add")) {
                int n = KeyboardUtils.getKeyNumber(stringArray[1]);
                if (n == 0) {
                    Night.CHAT_MANAGER.tagged("The keybind specified is not valid.", this.getTag());
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                String[] stringArray2 = Arrays.copyOfRange(stringArray, 2, stringArray.length);
                int n2 = 0;
                for (String string : stringArray2) {
                    stringBuilder.append(string).append(++n2 == stringArray2.length ? "" : " ");
                }
                Night.MACRO_MANAGER.add(stringBuilder.toString(), n);
                Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + String.valueOf(stringBuilder) + String.valueOf(ChatUtils.getSecondary()) + " as a macro, bound to the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(n) + String.valueOf(ChatUtils.getSecondary()) + " key.", this.getTag(), "command-" + this.getName());
            } else {
                this.messageSyntax();
            }
        } else if (stringArray.length == 2) {
            if (stringArray[0].equalsIgnoreCase("remove")) {
                int n = KeyboardUtils.getKeyNumber(stringArray[1]);
                if (n == 0) {
                    Night.CHAT_MANAGER.tagged("The keybind specified is not valid.", this.getTag());
                    return;
                }
                if (!Night.MACRO_MANAGER.containsValue(n)) {
                    Night.CHAT_MANAGER.tagged("There is no macro with the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(n) + String.valueOf(ChatUtils.getSecondary()) + " key.", this.getTag(), "command-" + this.getName());
                } else {
                    Night.CHAT_MANAGER.tagged("Successfully removed the " + String.valueOf(ChatUtils.getPrimary()) + Night.MACRO_MANAGER.getKey(n) + String.valueOf(ChatUtils.getSecondary()) + " macro.", this.getTag(), "command-" + this.getName());
                    Night.MACRO_MANAGER.remove(Night.MACRO_MANAGER.getKey(n));
                }
            } else {
                this.messageSyntax();
            }
        } else if (stringArray.length == 1) {
            if (stringArray[0].equalsIgnoreCase("clear")) {
                Night.MACRO_MANAGER.clear();
                Night.CHAT_MANAGER.tagged("Successfully removed all macros.", this.getTag(), "command-" + this.getName());
            } else if (stringArray[0].equalsIgnoreCase("list")) {
                if (Night.MACRO_MANAGER.getMacros().isEmpty()) {
                    Night.CHAT_MANAGER.tagged("There are currently no macros added.", this.getTag(), "command-" + this.getName() + "-list");
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    int n = 0;
                    for (String string : Night.MACRO_MANAGER.getMacros().keySet()) {
                        int n3 = Night.MACRO_MANAGER.getValue(string);
                        stringBuilder.append(ChatUtils.getSecondary()).append(string).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(n3)).append(ChatUtils.getPrimary()).append("]").append(ChatUtils.getSecondary()).append(++n == Night.MACRO_MANAGER.getMacros().size() ? "" : ", ");
                    }
                    Night.CHAT_MANAGER.message("Macros " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + Night.MACRO_MANAGER.getMacros().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(stringBuilder), "command-" + this.getName() + "-list");
                }
            } else {
                this.messageSyntax();
            }
        } else {
            this.messageSyntax();
        }
    }
}

