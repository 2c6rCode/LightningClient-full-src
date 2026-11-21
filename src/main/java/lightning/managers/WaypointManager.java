/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.phys.Vec3
 */
package lightning.managers;

import java.util.ArrayList;
import lightning.Night;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.WorldUtils;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

public class WaypointManager
implements IMinecraft {
    private final ArrayList<Waypoint> waypoints = new ArrayList();

    public boolean contains(String string) {
        return this.waypoints.stream().anyMatch(waypoint -> waypoint.getName().equalsIgnoreCase(string));
    }

    public void add(String string, Vec3 vec3) {
        this.add(string, vec3, WorldUtils.getDimension(), Night.SERVER_MANAGER.getServer());
    }

    public void add(String string, Vec3 vec3, String string2, String string3) {
        if (this.contains(string)) {
            return;
        }
        this.waypoints.add(new Waypoint(string, vec3, string2, string3));
    }

    public void remove(String string) {
        this.waypoints.removeIf(waypoint -> waypoint.getName().equalsIgnoreCase(string));
    }

    public void clear() {
        this.waypoints.clear();
    }

    @Generated
    public ArrayList<Waypoint> getWaypoints() {
        return this.waypoints;
    }

    public static class Waypoint {
        private final String name;
        private final Vec3 pos;
        private final String dimension;
        private final String server;

        public Waypoint(String string, Vec3 vec3, String string2, String string3) {
            this.name = string;
            this.pos = vec3;
            this.dimension = string2;
            this.server = string3;
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public Vec3 getPos() {
            return this.pos;
        }

        @Generated
        public String getDimension() {
            return this.dimension;
        }

        @Generated
        public String getServer() {
            return this.server;
        }
    }
}

