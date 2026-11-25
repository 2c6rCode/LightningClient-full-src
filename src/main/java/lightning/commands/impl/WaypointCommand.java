/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package lightning.commands.impl;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.managers.WaypointManager;
import lightning.utils.chat.ChatUtils;
import net.minecraft.world.phys.Vec3;

@RegisterCommand(name="waypoint", tag="Waypoint", description="Allows you to manage the client's custom waypoints.", syntax="<add|del> <[x, y, z]|[x, z]> | <clear|list>", aliases={"w"})
public class WaypointCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("add", "del", "clear", "list");
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("del")) {
            ArrayList<String> names = new ArrayList<String>();
            for (WaypointManager.Waypoint waypoint : Night.WAYPOINT_MANAGER.getWaypoints()) {
                names.add(waypoint.getName());
            }
            return names;
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                Night.WAYPOINT_MANAGER.clear();
                Night.CHAT_MANAGER.tagged("Successfully cleared your custom waypoints.", this.getTag(), this.getName() + "-list");
            } else if (args[0].equalsIgnoreCase("list")) {
                ArrayList<WaypointManager.Waypoint> waypoints = Night.WAYPOINT_MANAGER.getWaypoints();
                if (waypoints.isEmpty()) {
                    Night.CHAT_MANAGER.tagged("You currently have no custom waypoints set.", this.getTag());
                } else {
                    StringBuilder builder = new StringBuilder();
                    int index = 0;
                    for (WaypointManager.Waypoint waypoint : waypoints) {
                        builder.append(ChatUtils.getSecondary()).append(waypoint.getName()).append(++index == waypoints.size() ? "" : ", ");
                    }
                    Night.CHAT_MANAGER.message("Custom waypoints " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + waypoints.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(builder), this.getName() + "-list");
                }
            } else {
                this.messageSyntax();
            }
        } else if (args.length == 2 || args.length == 4 || args.length == 5) {
            try {
                int x = args.length == 2 ? (int)WaypointCommand.mc.player.getX() : Integer.parseInt(args[2]);
                int y = args.length == 2 || args.length == 4 ? (int)WaypointCommand.mc.player.getY() + 1 : Integer.parseInt(args[3]);
                int z = args.length == 2 ? (int)WaypointCommand.mc.player.getZ() : (args.length == 4 ? Integer.parseInt(args[3]) : Integer.parseInt(args[4]));
                Vec3 pos = new Vec3((double)x, (double)y, (double)z);
                if (args[0].equalsIgnoreCase("add")) {
                    Night.WAYPOINT_MANAGER.add(args[1], pos);
                    Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + args[1] + " [" + (int)pos.x + ", " + (int)pos.y + ", " + (int)pos.z + "]" + String.valueOf(ChatUtils.getSecondary()) + " to your custom waypoints.", this.getTag(), this.getName());
                } else if (args[0].equalsIgnoreCase("del")) {
                    Night.WAYPOINT_MANAGER.remove(args[1]);
                    Night.CHAT_MANAGER.tagged("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " to your custom waypoints.", this.getTag(), this.getName());
                } else {
                    this.messageSyntax();
                }
            }
            catch (NumberFormatException numberFormatException) {
                Night.CHAT_MANAGER.tagged("Please input valid " + String.valueOf(ChatUtils.getPrimary()) + "integer" + String.valueOf(ChatUtils.getSecondary()) + " numbers for the coordinates.", this.getTag(), this.getName());
            }
        } else {
            this.messageSyntax();
        }
    }
}

