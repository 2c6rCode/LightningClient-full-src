/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Util
 */
package lightning.commands.impl;

import java.io.File;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import net.minecraft.util.Util;

@RegisterCommand(name="folder", description="Opens the clients folder.")
public class FolderCommand
extends Command {
    @Override
    public void execute(String[] stringArray) {
        File file = new File("Lightning");
        if (file.exists()) {
            Util.getPlatform().openFile(file);
        } else {
            Night.CHAT_MANAGER.info("Could not find the client's configuration folder.");
        }
    }
}

