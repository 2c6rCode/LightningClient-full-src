/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  lombok.Generated
 */
package lightning.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.commands.impl.ModuleCommand;
import lightning.core.JarClassScanner;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.modules.Module;
import lombok.Generated;

public class CommandManager {
    private final ArrayList<Command> commands = new ArrayList();
    private String prefix = ".";
    private final CommandDispatcher<CommandManager> dispatcher = new CommandDispatcher();

    public CommandManager() {
        Night.EVENT_HANDLER.subscribe(this);
        try {
            for (Class<?> commandClass : JarClassScanner.findSubtypesOf(Command.class, Night.class)) {
                if (commandClass.getAnnotation(RegisterCommand.class) == null || commandClass == ModuleCommand.class) continue;
                this.commands.add((Command)commandClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
            }
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException reflectiveOperationException) {
            Night.LOGGER.error("Failed to register the client's modules!", (Throwable)reflectiveOperationException);
        }
        HashSet<String> registeredNames = new HashSet<>();
        for (Command command : this.commands) {
            registeredNames.add(command.getName().toLowerCase());
            for (String alias : command.getAliases()) {
                registeredNames.add(alias.toLowerCase());
            }
        }
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (registeredNames.contains(module.getName().toLowerCase())) continue;
            this.commands.add(new ModuleCommand(module));
        }
        for (Command command : this.commands) {
            this.register(command);
        }
    }

    private void register(Command command) {
        ArrayList<String> names = new ArrayList<String>();
        names.add(command.getName());
        names.addAll(command.getAliases());
        for (String commandName : names) {
            LiteralArgumentBuilder<CommandManager> literal = LiteralArgumentBuilder.literal(commandName);
            literal.executes(commandContext -> {
                command.execute(new String[0]);
                return 1;
            });
            literal.then(RequiredArgumentBuilder.<CommandManager, String>argument("args", StringArgumentType.greedyString()).suggests((commandContext, suggestionsBuilder) -> {
                String remStr = suggestionsBuilder.getRemaining();
                String[] parts = remStr.split(" ", -1);
                String prefixToken = parts[parts.length - 1].toLowerCase();
                String[] prevArgs = Arrays.copyOf(parts, parts.length - 1);
                SuggestionsBuilder offsetBuilder = suggestionsBuilder.createOffset(suggestionsBuilder.getStart() + remStr.length() - prefixToken.length());
                for (String suggestion : command.getSuggestions(prevArgs)) {
                    if (!suggestion.toLowerCase().startsWith(prefixToken)) continue;
                    offsetBuilder.suggest(suggestion);
                }
                return offsetBuilder.buildFuture();
            }).executes(commandContext -> {
                String argsStr = StringArgumentType.getString(commandContext, "args");
                command.execute(argsStr.isEmpty() ? new String[]{} : argsStr.split(" "));
                return 1;
            }));
            this.dispatcher.register(literal);
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent chatInputEvent) {
        String message = chatInputEvent.getMessage();
        if (!message.startsWith(this.prefix)) {
            return;
        }
        chatInputEvent.setCancelled(true);
        this.execute(message);
    }

    public void execute(String input) {
        try {
            this.dispatcher.execute(input.substring(this.prefix.length()), this);
        }
        catch (CommandSyntaxException commandSyntaxException) {
            Night.CHAT_MANAGER.warn("Could not find the command specified.");
        }
    }

    public Command getCommand(String name) {
        return this.commands.stream().filter(command -> command.getName().equalsIgnoreCase(name.toLowerCase()) || command.getAliases().contains(name.toLowerCase())).findFirst().orElse(null);
    }

    @Generated
    public ArrayList<Command> getCommands() {
        return this.commands;
    }

    @Generated
    public String getPrefix() {
        return this.prefix;
    }

    @Generated
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Generated
    public CommandDispatcher<CommandManager> getDispatcher() {
        return this.dispatcher;
    }
}

