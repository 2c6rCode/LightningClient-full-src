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
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("off");
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("off")) {
            return List.of("restore");
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("off")) {
            this.messageSyntax();
            return;
        }
        if (args.length == 2 && args[1].equalsIgnoreCase("restore")) {
            if (lastDisabled.isEmpty()) {
                Night.CHAT_MANAGER.tagged("Nothing to restore.", this.getTag(), this.getName());
                return;
            }
            int restoredCount = 0;
            for (Module module : lastDisabled) {
                if (module.isToggled()) continue;
                module.setToggled(true, false);
                ++restoredCount;
            }
            lastDisabled.clear();
            Night.CHAT_MANAGER.tagged("Restored " + restoredCount + " module(s).", this.getTag(), this.getName());
            return;
        }
        if (args.length == 1) {
            lastDisabled.clear();
            int disabledCount = 0;
            for (Module module : Night.MODULE_MANAGER.getModules()) {
                if (module.isPersistent() || !module.isToggled()) continue;
                module.setToggled(false, false);
                lastDisabled.add(module);
                ++disabledCount;
            }
            Night.CHAT_MANAGER.tagged("Disabled " + disabledCount + " module(s). Use '.all off restore' to bring them back.", this.getTag(), this.getName());
            return;
        }
        this.messageSyntax();
    }
}

