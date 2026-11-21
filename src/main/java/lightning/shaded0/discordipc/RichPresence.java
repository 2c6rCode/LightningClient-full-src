/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package lightning.shaded0.discordipc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RichPresence {
    private String details;
    private String state;
    private Assets assets;
    private Timestamps timestamps;

    public void setDetails(String string) {
        this.details = string;
    }

    public void setState(String string) {
        this.state = string;
    }

    public void setLargeImage(String string, String string2) {
        if (this.assets == null) {
            this.assets = new Assets();
        }
        this.assets.large_image = string;
        this.assets.large_text = string2;
    }

    public void setSmallImage(String string, String string2) {
        if (this.assets == null) {
            this.assets = new Assets();
        }
        this.assets.small_image = string;
        this.assets.small_text = string2;
    }

    public void setStart(long l) {
        if (this.timestamps == null) {
            this.timestamps = new Timestamps();
        }
        this.timestamps.start = l;
    }

    public void setEnd(long l) {
        if (this.timestamps == null) {
            this.timestamps = new Timestamps();
        }
        this.timestamps.end = l;
    }

    public JsonObject toJson() {
        JsonObject jsonObject;
        JsonObject jsonObject2 = new JsonObject();
        if (this.details != null) {
            jsonObject2.addProperty("details", this.details);
        }
        if (this.state != null) {
            jsonObject2.addProperty("state", this.state);
        }
        if (this.assets != null) {
            jsonObject = new JsonObject();
            if (this.assets.large_image != null) {
                jsonObject.addProperty("large_image", this.assets.large_image);
            }
            if (this.assets.large_text != null) {
                jsonObject.addProperty("large_text", this.assets.large_text);
            }
            if (this.assets.small_image != null) {
                jsonObject.addProperty("small_image", this.assets.small_image);
            }
            if (this.assets.small_text != null) {
                jsonObject.addProperty("small_text", this.assets.small_text);
            }
            jsonObject2.add("assets", (JsonElement)jsonObject);
        }
        if (this.timestamps != null) {
            jsonObject = new JsonObject();
            if (this.timestamps.start != null) {
                jsonObject.addProperty("start", (Number)this.timestamps.start);
            }
            if (this.timestamps.end != null) {
                jsonObject.addProperty("end", (Number)this.timestamps.end);
            }
            jsonObject2.add("timestamps", (JsonElement)jsonObject);
        }
        jsonObject2.addProperty("type", (Number)2);
        return jsonObject2;
    }

    public static class Assets {
        public String large_image;
        public String large_text;
        public String small_image;
        public String small_text;
    }

    public static class Timestamps {
        public Long start;
        public Long end;
    }
}

