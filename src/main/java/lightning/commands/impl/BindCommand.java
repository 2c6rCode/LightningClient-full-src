/*
 * Decompiled with CFR 0.152.
 */
package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;
import lightning.utils.chat.ChatUtils;
import lightning.utils.input.KeyboardUtils;

@RegisterCommand(name="bind", tag="Bind", description="Changes the toggle keybind of a module.", syntax="<[module]> <[key]|reset> | <reset|list> ", aliases={"b", "key", "keybind"})
public class BindCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            ArrayList<String> arrayList = new ArrayList<String>(this.moduleNames());
            arrayList.add("reset");
            arrayList.add("list");
            return arrayList;
        }
        if (stringArray.length == 1 && Night.MODULE_MANAGER.getModule(stringArray[0]) != null) {
            return List.of("<bind>");
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        if (stringArray.length == 2) {
            Module module2 = Night.MODULE_MANAGER.getModule(stringArray[0]);
            if (module2 == null) {
                Night.CHAT_MANAGER.tagged("Could not find the module specified.", this.getTag(), this.getName());
                return;
            }
            if (stringArray[1].equalsIgnoreCase("reset")) {
                module2.setBind(0);
                Night.CHAT_MANAGER.tagged("Successfully reset the toggle keybind of the " + String.valueOf(ChatUtils.getPrimary()) + module2.getName() + String.valueOf(ChatUtils.getSecondary()) + " module.", this.getTag(), this.getName());
            } else {
                module2.setBind(KeyboardUtils.getKeyNumber(stringArray[1]));
                Night.CHAT_MANAGER.tagged("Successfully bound the " + String.valueOf(ChatUtils.getPrimary()) + module2.getName() + String.valueOf(ChatUtils.getSecondary()) + " module to the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(module2.getBind()) + String.valueOf(ChatUtils.getSecondary()) + " key.", this.getTag(), this.getName());
            }
        } else if (stringArray.length == 1) {
            switch (stringArray[0].toLowerCase()) {
                case "reset": {
                    Night.MODULE_MANAGER.getModules().forEach(module -> module.bind.resetValue());
                    Night.CHAT_MANAGER.tagged("Successfully reset every module's toggle keybind.", this.getTag(), this.getName());
                    break;
                }
                case "list": {
                    List<Module> list = Night.MODULE_MANAGER.getModules().stream().filter(module -> module.getBind() != 0).toList();
                    if (list.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("There are currently no bound modules.", this.getTag(), this.getName() + "-list");
                        break;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    int n = 0;
                    for (Module module3 : list) {
                        stringBuilder.append(ChatUtils.getSecondary()).append(module3.getName()).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(module3.getBind()).toUpperCase()).append(ChatUtils.getPrimary()).append("]").append(++n == list.size() ? "" : ", ");
                    }
                    Night.CHAT_MANAGER.message("Bound Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + list.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(stringBuilder), this.getName() + "-list");
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

