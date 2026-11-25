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

@RegisterCommand(name="chatnotify", tag="ChatNotify", description="Manages the toggle notification status of the client's modules.", syntax="<true|false|list|reset> | <[module]> <true|false|reset>")
public class ChatNotifyCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            ArrayList<String> suggestions = new ArrayList<String>(this.moduleNames());
            suggestions.add("true");
            suggestions.add("false");
            suggestions.add("list");
            suggestions.add("reset");
            return suggestions;
        }
        if (args.length == 1 && Night.MODULE_MANAGER.getModule(args[0]) != null) {
            return List.of("true", "false", "reset");
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
            switch (args[1]) {
                case "true": {
                    targetModule.chatNotify.setValue(true);
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "false": {
                    targetModule.chatNotify.setValue(false);
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "reset": {
                    targetModule.chatNotify.setValue(targetModule.chatNotify.getDefaultValue());
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to it's default value.", this.getTag(), this.getName());
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else if (args.length == 1) {
            switch (args[0]) {
                case "true": {
                    for (Module module : Night.MODULE_MANAGER.getModules()) {
                        module.chatNotify.setValue(true);
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "false": {
                    for (Module module : Night.MODULE_MANAGER.getModules()) {
                        module.chatNotify.setValue(false);
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "list": {
                    ArrayList<Module> notifiableModules = new ArrayList<Module>(Night.MODULE_MANAGER.getModules().stream().filter(module -> module.chatNotify.getValue()).toList());
                    if (notifiableModules.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("There are currently no modules with their notification status set to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName() + "-list");
                        break;
                    }
                    StringBuilder builder = new StringBuilder();
                    int index = 0;
                    for (Module module : notifiableModules) {
                        builder.append(ChatUtils.getSecondary()).append(module.getName()).append(index + 1 == notifiableModules.size() ? "" : ", ");
                        ++index;
                    }
                    Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getSecondary()) + "Notifiable Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + notifiableModules.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(builder), this.getName() + "-list");
                    break;
                }
                case "reset": {
                    for (Module module : Night.MODULE_MANAGER.getModules()) {
                        module.chatNotify.setValue(module.chatNotify.getDefaultValue());
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's notification status to it's default value.", this.getTag(), this.getName());
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

