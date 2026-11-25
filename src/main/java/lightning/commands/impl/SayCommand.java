/*
 * Decompiled with CFR 0.152.
 */
package lightning.commands.impl;

import lightning.commands.Command;
import lightning.commands.RegisterCommand;

@RegisterCommand(name="say", description="Sends a message through commands, allowing you to bypass the client's prefix and minecraft's command prefix.", syntax="<[message]>")
public class SayCommand
extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length >= 1) {
            StringBuilder builder = new StringBuilder();
            int index = 0;
            for (String part : args) {
                builder.append(part).append(index + 1 == args.length ? "" : " ");
                ++index;
            }
            mc.getConnection().sendChat(builder.toString());
        } else {
            this.messageSyntax();
        }
    }
}

