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
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            ArrayList<String> arrayList = new ArrayList<String>(this.moduleNames());
            arrayList.add("true");
            arrayList.add("false");
            arrayList.add("list");
            arrayList.add("reset");
            return arrayList;
        }
        if (stringArray.length == 1 && Night.MODULE_MANAGER.getModule(stringArray[0]) != null) {
            return List.of("true", "false", "reset");
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
            switch (stringArray[1]) {
                case "true": {
                    module2.chatNotify.setValue(true);
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "false": {
                    module2.chatNotify.setValue(false);
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "reset": {
                    module2.chatNotify.setValue(module2.chatNotify.getDefaultValue());
                    Night.CHAT_MANAGER.tagged("Successfully set the module's notification status to it's default value.", this.getTag(), this.getName());
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else if (stringArray.length == 1) {
            switch (stringArray[0]) {
                case "true": {
                    for (Module module3 : Night.MODULE_MANAGER.getModules()) {
                        module3.chatNotify.setValue(true);
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "false": {
                    for (Module module4 : Night.MODULE_MANAGER.getModules()) {
                        module4.chatNotify.setValue(false);
                    }
                    Night.CHAT_MANAGER.tagged("Successfully set every module's notification status to " + String.valueOf(ChatUtils.getPrimary()) + "false" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    break;
                }
                case "list": {
                    ArrayList<Module> arrayList = new ArrayList<Module>(Night.MODULE_MANAGER.getModules().stream().filter(module -> module.chatNotify.getValue()).toList());
                    if (arrayList.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("There are currently no modules with their notification status set to " + String.valueOf(ChatUtils.getPrimary()) + "true" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName() + "-list");
                        break;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    int n = 0;
                    for (Module module5 : arrayList) {
                        stringBuilder.append(ChatUtils.getSecondary()).append(module5.getName()).append(n + 1 == arrayList.size() ? "" : ", ");
                        ++n;
                    }
                    Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getSecondary()) + "Notifiable Modules " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + arrayList.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(stringBuilder), this.getName() + "-list");
                    break;
                }
                case "reset": {
                    for (Module module6 : Night.MODULE_MANAGER.getModules()) {
                        module6.chatNotify.setValue(module6.chatNotify.getDefaultValue());
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

