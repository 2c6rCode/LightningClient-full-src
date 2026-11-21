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
            for (Class<?> object : JarClassScanner.findSubtypesOf(Command.class, Night.class)) {
                if (object.getAnnotation(RegisterCommand.class) == null || object == ModuleCommand.class) continue;
                this.commands.add((Command)object.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
            }
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException reflectiveOperationException) {
            Night.LOGGER.error("Failed to register the client's modules!", (Throwable)reflectiveOperationException);
        }
        HashSet hashSet = new HashSet();
        for (Command command : this.commands) {
            hashSet.add(command.getName().toLowerCase());
            for (String string : command.getAliases()) {
                hashSet.add(string.toLowerCase());
            }
        }
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (hashSet.contains(module.getName().toLowerCase())) continue;
            this.commands.add(new ModuleCommand(module));
        }
        for (Command command : this.commands) {
            this.register(command);
        }
    }

    private void register(Command command) {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(command.getName());
        arrayList.addAll(command.getAliases());
        for (String string : arrayList) {
            LiteralArgumentBuilder literalArgumentBuilder = LiteralArgumentBuilder.literal((String)string);
            literalArgumentBuilder.executes(commandContext -> {
                command.execute(new String[0]);
                return 1;
            });
            literalArgumentBuilder.then(RequiredArgumentBuilder.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).suggests((commandContext, suggestionsBuilder) -> {
                String remStr = suggestionsBuilder.getRemaining();
                String[] stringArray = remStr.split(" ", -1);
                String string2 = stringArray[stringArray.length - 1].toLowerCase();
                String[] stringArray2 = Arrays.copyOf(stringArray, stringArray.length - 1);
                SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(suggestionsBuilder.getStart() + remStr.length() - string2.length());
                for (String string3 : command.getSuggestions(stringArray2)) {
                    if (!string3.toLowerCase().startsWith(string2)) continue;
                    suggestionsBuilder2.suggest(string3);
                }
                return suggestionsBuilder2.buildFuture();
            }).executes(commandContext -> {
                String argsStr = StringArgumentType.getString((CommandContext)commandContext, (String)"args");
                command.execute(argsStr.isEmpty() ? new String[]{} : argsStr.split(" "));
                return 1;
            }));
            this.dispatcher.register(literalArgumentBuilder);
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent chatInputEvent) {
        String string = chatInputEvent.getMessage();
        if (!string.startsWith(this.prefix)) {
            return;
        }
        chatInputEvent.setCancelled(true);
        this.execute(string);
    }

    public void execute(String string) {
        try {
            this.dispatcher.execute(string.substring(this.prefix.length()), this);
        }
        catch (CommandSyntaxException commandSyntaxException) {
            Night.CHAT_MANAGER.warn("Could not find the command specified.");
        }
    }

    public Command getCommand(String string) {
        return this.commands.stream().filter(command -> command.getName().equalsIgnoreCase(string.toLowerCase()) || command.getAliases().contains(string.toLowerCase())).findFirst().orElse(null);
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
    public void setPrefix(String string) {
        this.prefix = string;
    }

    @Generated
    public CommandDispatcher<CommandManager> getDispatcher() {
        return this.dispatcher;
    }
}

