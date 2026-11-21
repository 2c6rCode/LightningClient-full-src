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

@RegisterCommand(name="all", tag="All", description="Mass-toggles modules. 'off' disables all enabled modules; 'off restore' re-enables them.", syntax="off | off restore")
public class AllCommand
extends Command {
    private static final List<Module> lastDisabled = new ArrayList<Module>();

    @Override
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            return List.of("off");
        }
        if (stringArray.length == 1 && stringArray[0].equalsIgnoreCase("off")) {
            return List.of("restore");
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        if (stringArray.length == 0 || !stringArray[0].equalsIgnoreCase("off")) {
            this.messageSyntax();
            return;
        }
        if (stringArray.length == 2 && stringArray[1].equalsIgnoreCase("restore")) {
            if (lastDisabled.isEmpty()) {
                Night.CHAT_MANAGER.tagged("Nothing to restore.", this.getTag(), this.getName());
                return;
            }
            int n = 0;
            for (Module module : lastDisabled) {
                if (module.isToggled()) continue;
                module.setToggled(true, false);
                ++n;
            }
            lastDisabled.clear();
            Night.CHAT_MANAGER.tagged("Restored " + n + " module(s).", this.getTag(), this.getName());
            return;
        }
        if (stringArray.length == 1) {
            lastDisabled.clear();
            int n = 0;
            for (Module module : Night.MODULE_MANAGER.getModules()) {
                if (module.isPersistent() || !module.isToggled()) continue;
                module.setToggled(false, false);
                lastDisabled.add(module);
                ++n;
            }
            Night.CHAT_MANAGER.tagged("Disabled " + n + " module(s). Use '.all off restore' to bring them back.", this.getTag(), this.getName());
            return;
        }
        this.messageSyntax();
    }
}

