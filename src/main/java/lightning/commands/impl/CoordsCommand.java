/*
 * Decompiled with CFR 0.152.
 */
package lightning.commands.impl;

import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name="coords", tag="Coords", description="Copies your position, or whispers it to a player.", syntax="| <[player]>")
public class CoordsCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0 && mc.getConnection() != null) {
            String string = CoordsCommand.mc.player != null ? CoordsCommand.mc.player.getGameProfile().name() : "";
            return mc.getConnection().getOnlinePlayers().stream().map(playerInfo -> playerInfo.getProfile().name()).filter(string2 -> string2 != null && !string2.isEmpty() && !string2.equalsIgnoreCase(string)).distinct().toList();
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        if (CoordsCommand.mc.player == null || CoordsCommand.mc.level == null) {
            return;
        }
        String string = CoordsCommand.mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
        String string2 = (int)CoordsCommand.mc.player.getX() + " " + (int)CoordsCommand.mc.player.getY() + " " + (int)CoordsCommand.mc.player.getZ() + " (" + string + ")";
        if (stringArray.length == 0) {
            CoordsCommand.mc.keyboardHandler.setClipboard(string2);
            Night.CHAT_MANAGER.tagged("Copied your position (" + String.valueOf(ChatUtils.getPrimary()) + string2 + String.valueOf(ChatUtils.getSecondary()) + ") to the clipboard.", this.getTag(), this.getName());
            return;
        }
        if (stringArray.length == 1) {
            String string3 = stringArray[0];
            if (mc.getConnection() != null) {
                mc.getConnection().sendCommand("w " + string3 + " " + string2);
                Night.CHAT_MANAGER.tagged("Sent your position to " + String.valueOf(ChatUtils.getPrimary()) + string3 + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
            }
            return;
        }
        this.messageSyntax();
    }
}

