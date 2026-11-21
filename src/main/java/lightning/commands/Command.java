/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.commands;

import java.util.Arrays;
import java.util.List;
import lightning.Night;
import lightning.commands.RegisterCommand;
import lightning.utils.IMinecraft;
import lombok.Generated;

public abstract class Command
implements IMinecraft {
    private String name;
    private String tag;
    private final String description;
    private final String syntax;
    private final List<String> aliases;

    public Command() {
        RegisterCommand registerCommand = this.getClass().getAnnotation(RegisterCommand.class);
        this.name = registerCommand.name();
        this.tag = registerCommand.tag().isEmpty() ? registerCommand.name() : registerCommand.tag();
        this.description = registerCommand.description();
        this.syntax = registerCommand.syntax();
        this.aliases = Arrays.asList(registerCommand.aliases());
    }

    public abstract void execute(String[] var1);

    public List<String> getSuggestions(String[] stringArray) {
        return List.of();
    }

    protected List<String> moduleNames() {
        return Night.MODULE_MANAGER.getModules().stream().map(module -> module.getName().toLowerCase()).toList();
    }

    public void messageSyntax() {
        Night.CHAT_MANAGER.info(this.name + " " + this.syntax);
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getTag() {
        return this.tag;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public String getSyntax() {
        return this.syntax;
    }

    @Generated
    public List<String> getAliases() {
        return this.aliases;
    }

    @Generated
    public void setName(String string) {
        this.name = string;
    }

    @Generated
    public void setTag(String string) {
        this.tag = string;
    }
}

