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
    public void execute(String[] stringArray) {
        if (stringArray.length >= 1) {
            StringBuilder stringBuilder = new StringBuilder();
            int n = 0;
            for (String string : stringArray) {
                stringBuilder.append(string).append(n + 1 == stringArray.length ? "" : " ");
                ++n;
            }
            mc.getConnection().sendChat(stringBuilder.toString());
        } else {
            this.messageSyntax();
        }
    }
}

