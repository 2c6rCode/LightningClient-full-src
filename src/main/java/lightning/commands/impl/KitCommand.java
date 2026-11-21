/*
 * Decompiled with CFR 0.152.
 */
package lightning.commands.impl;

import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.impl.player.RekitModule;

@RegisterCommand(name="kit", tag="Kit", description="Manages Rekit's saved kits.", syntax="<save|load|delete> <name> | <list|active>")
public class KitCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            return List.of("save", "load", "delete", "list", "active");
        }
        if (stringArray.length == 1 && (stringArray[0].equalsIgnoreCase("load") || stringArray[0].equalsIgnoreCase("delete"))) {
            return Night.MODULE_MANAGER.getModule(RekitModule.class).getKitNames();
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        RekitModule rekitModule = Night.MODULE_MANAGER.getModule(RekitModule.class);
        if (stringArray.length == 0) {
            this.messageSyntax();
            return;
        }
        switch (stringArray[0].toLowerCase()) {
            case "save": {
                if (stringArray.length < 2) {
                    this.messageSyntax();
                    return;
                }
                rekitModule.saveKit(stringArray[1]);
                break;
            }
            case "load": {
                if (stringArray.length < 2) {
                    rekitModule.listKits();
                    break;
                }
                rekitModule.loadKit(stringArray[1]);
                break;
            }
            case "delete": {
                if (stringArray.length < 2) {
                    this.messageSyntax();
                    return;
                }
                rekitModule.deleteKit(stringArray[1]);
                break;
            }
            case "active": {
                rekitModule.showActiveKit();
                break;
            }
            case "list": {
                rekitModule.listKits();
                break;
            }
            default: {
                this.messageSyntax();
            }
        }
    }
}

