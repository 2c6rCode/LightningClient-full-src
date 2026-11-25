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
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            ArrayList<String> suggestions = new ArrayList<String>(this.moduleNames());
            suggestions.add("reset");
            suggestions.add("list");
            return suggestions;
        }
        if (args.length == 1 && Night.MODULE_MANAGER.getModule(args[0]) != null) {
            return List.of("<bind>");
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            Module targetModule = Night.MODULE_MANAGER.getModule(args[0]);
            if (targetModule == null) {
                Night.CHAT_MANAGER.tagged("Could not find the module specified.", this.getTag(), this.getName());
                return;
            }
            if (args[1].equalsIgnoreCase("reset")) {
                targetModule.setBind(0);
                Night.CHAT_MANAGER.tagged("Successfully reset the toggle keybind of the " + String.valueOf(ChatUtils.getPrimary()) + targetModule.getName() + String.valueOf(ChatUtils.getSecondary()) + " module.", this.getTag(), this.getName());
            } else {
                targetModule.setBind(KeyboardUtils.getKeyNumber(args[1]));
                Night.CHAT_MANAGER.tagged("Successfully bound the " + String.valueOf(ChatUtils.getPrimary()) + targetModule.getName() + String.valueOf(ChatUtils.getSecondary()) + " module to the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(targetModule.getBind()) + String.valueOf(ChatUtils.getSecondary()) + " key.", this.getTag(), this.getName());
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reset": {
                    Night.MODULE_MANAGER.getModules().forEach(module -> module.bind.resetValue());
                    Night.CHAT_MANAGER.tagged("Successfully reset every module's toggle keybind.", this.getTag(), this.getName());
                    break;
                }
                case "list": {
                    List<Module> boundModules = Night.MODULE_MANAGER.getModules().stream().filter(module -> module.getBind() != 0).toList();
                    if (boundModules.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("There are currently no bound modules.", this.getTag(), this.getName() + "-list");
                        break;
                    }
                    StringBuilder builder = new StringBuilder();
                    int index = 0;
                    for (Module boundModule : boundModules) {
                        builder.append(ChatUtils.getSecondary()).append(boundModule.getName()).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(boundModule.getBind()).toUpperCase()).append(ChatUtils.getPrimary()).append("]").append(++index == boundModules.size() ? "" : ", ");
                    }
                    Night.CHAT_MANAGER.message("Bound Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + boundModules.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(builder), this.getName() + "-list");
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

