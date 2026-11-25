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
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0 && mc.getConnection() != null) {
            String selfName = CoordsCommand.mc.player != null ? CoordsCommand.mc.player.getGameProfile().name() : "";
            return mc.getConnection().getOnlinePlayers().stream().map(playerInfo -> playerInfo.getProfile().name()).filter(playerName -> playerName != null && !playerName.isEmpty() && !playerName.equalsIgnoreCase(selfName)).distinct().toList();
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (CoordsCommand.mc.player == null || CoordsCommand.mc.level == null) {
            return;
        }
        String dimension = CoordsCommand.mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
        String coordsText = (int)CoordsCommand.mc.player.getX() + " " + (int)CoordsCommand.mc.player.getY() + " " + (int)CoordsCommand.mc.player.getZ() + " (" + dimension + ")";
        if (args.length == 0) {
            CoordsCommand.mc.keyboardHandler.setClipboard(coordsText);
            Night.CHAT_MANAGER.tagged("Copied your position (" + String.valueOf(ChatUtils.getPrimary()) + coordsText + String.valueOf(ChatUtils.getSecondary()) + ") to the clipboard.", this.getTag(), this.getName());
            return;
        }
        if (args.length == 1) {
            String targetPlayer = args[0];
            if (mc.getConnection() != null) {
                mc.getConnection().sendCommand("w " + targetPlayer + " " + coordsText);
                Night.CHAT_MANAGER.tagged("Sent your position to " + String.valueOf(ChatUtils.getPrimary()) + targetPlayer + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
            }
            return;
        }
        this.messageSyntax();
    }
}

