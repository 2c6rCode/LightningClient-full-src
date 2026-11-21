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
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            return List.of("add", "del", "clear", "list");
        }
        if (stringArray.length == 1 && stringArray[0].equalsIgnoreCase("del")) {
            ArrayList<String> arrayList = new ArrayList<String>();
            for (WaypointManager.Waypoint waypoint : Night.WAYPOINT_MANAGER.getWaypoints()) {
                arrayList.add(waypoint.getName());
            }
            return arrayList;
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        block15: {
            if (stringArray.length == 1) {
                if (stringArray[0].equalsIgnoreCase("clear")) {
                    Night.WAYPOINT_MANAGER.clear();
                    Night.CHAT_MANAGER.tagged("Successfully cleared your custom waypoints.", this.getTag(), this.getName() + "-list");
                } else if (stringArray[0].equalsIgnoreCase("list")) {
                    ArrayList<WaypointManager.Waypoint> arrayList = Night.WAYPOINT_MANAGER.getWaypoints();
                    if (arrayList.isEmpty()) {
                        Night.CHAT_MANAGER.tagged("You currently have no custom waypoints set.", this.getTag());
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        int n = 0;
                        for (WaypointManager.Waypoint waypoint : arrayList) {
                            stringBuilder.append(ChatUtils.getSecondary()).append(waypoint.getName()).append(++n == arrayList.size() ? "" : ", ");
                        }
                        Night.CHAT_MANAGER.message("Custom waypoints " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + arrayList.size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(stringBuilder), this.getName() + "-list");
                    }
                } else {
                    this.messageSyntax();
                }
            } else if (stringArray.length == 2 || stringArray.length == 4 || stringArray.length == 5) {
                try {
                    int n = stringArray.length == 2 ? (int)WaypointCommand.mc.player.getX() : Integer.parseInt(stringArray[2]);
                    int n2 = stringArray.length == 2 || stringArray.length == 4 ? (int)WaypointCommand.mc.player.getY() + 1 : Integer.parseInt(stringArray[3]);
                    int n3 = n2;
                    int n4 = stringArray.length == 2 ? (int)WaypointCommand.mc.player.getZ() : (stringArray.length == 4 ? Integer.parseInt(stringArray[3]) : Integer.parseInt(stringArray[4]));
                    Vec3 vec3 = new Vec3((double)n, (double)n2, (double)n4);
                    if (stringArray[0].equalsIgnoreCase("add")) {
                        Night.WAYPOINT_MANAGER.add(stringArray[1], vec3);
                        Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + " [" + (int)vec3.x + ", " + (int)vec3.y + ", " + (int)vec3.z + "]" + String.valueOf(ChatUtils.getSecondary()) + " to your custom waypoints.", this.getTag(), this.getName());
                        break block15;
                    }
                    if (stringArray[0].equalsIgnoreCase("del")) {
                        Night.WAYPOINT_MANAGER.remove(stringArray[1]);
                        Night.CHAT_MANAGER.tagged("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + String.valueOf(ChatUtils.getSecondary()) + " to your custom waypoints.", this.getTag(), this.getName());
                        break block15;
                    }
                    this.messageSyntax();
                }
                catch (NumberFormatException numberFormatException) {
                    Night.CHAT_MANAGER.tagged("Please input valid " + String.valueOf(ChatUtils.getPrimary()) + "integer" + String.valueOf(ChatUtils.getSecondary()) + " numbers for the coordinates.", this.getTag(), this.getName());
                }
            } else {
                this.messageSyntax();
            }
        }
    }
}

