/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 */
package lightning.modules.impl.visuals.stashfinder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class StashWebhook {
    private static volatile String webhookUrl = "";
    private static volatile String userId = "";
    private static volatile boolean loaded = false;
    private static final long PING_INTERVAL_MS = 5000L;
    private static volatile long lastSentAtMs = 0L;

    private static File file() {
        File file = new File(FabricLoader.getInstance().getGameDir().toFile(), "lightning/stashfinder");
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(file, "webhook.json");
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        File file = StashWebhook.file();
        if (!file.exists()) {
            return;
        }
        try (FileReader fileReader = new FileReader(file);){
            JsonObject jsonObject = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            if (jsonObject.has("url")) {
                webhookUrl = jsonObject.get("url").getAsString();
            }
            if (jsonObject.has("userId")) {
                userId = jsonObject.get("userId").getAsString();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static synchronized void save() {
        try (FileWriter fileWriter = new FileWriter(StashWebhook.file());){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("url", webhookUrl);
            jsonObject.addProperty("userId", userId);
            fileWriter.write(jsonObject.toString());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static String getWebhookUrl() {
        StashWebhook.ensureLoaded();
        return webhookUrl;
    }

    public static String getUserId() {
        StashWebhook.ensureLoaded();
        return userId;
    }

    public static void setWebhookUrl(String string) {
        StashWebhook.ensureLoaded();
        webhookUrl = string;
        StashWebhook.save();
    }

    public static void setUserId(String string) {
        StashWebhook.ensureLoaded();
        userId = string;
        StashWebhook.save();
    }

    private static synchronized boolean tryClaimSendSlot() {
        long l = System.currentTimeMillis();
        if (l - lastSentAtMs < 5000L) {
            return false;
        }
        lastSentAtMs = l;
        return true;
    }

    private static String serverLabel() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCurrentServer() != null) {
            return minecraft.getCurrentServer().ip;
        }
        if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null) {
            return "Singleplayer: " + minecraft.getSingleplayerServer().getWorldData().getLevelName();
        }
        return "Unknown";
    }

    public static void send(int n, int n2, String string, Map<String, Integer> map, String string2, String string3) {
        String string4 = string2 != null && !string2.isBlank() ? string2 : StashWebhook.getWebhookUrl();
        String string5 = string4;
        if (string4 == null || string4.isBlank()) {
            return;
        }
        if (!StashWebhook.tryClaimSendSlot()) {
            return;
        }
        String string6 = StashWebhook.serverLabel();
        String string7 = string3 != null && !string3.isBlank() ? string3 : StashWebhook.getUserId();
        CompletableFuture.runAsync(() -> {
            try {
                Object object;
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("title", "Stash Found!");
                jsonObject2.addProperty("description", "Coordinates: **X: " + n + " Z: " + n2 + "** (" + string + ")");
                jsonObject2.addProperty("color", (Number)3066993);
                JsonArray jsonArray = new JsonArray();
                JsonObject jsonObject3 = new JsonObject();
                jsonObject3.addProperty("name", "Server");
                jsonObject3.addProperty("value", string6);
                jsonObject3.addProperty("inline", Boolean.valueOf(false));
                jsonArray.add((JsonElement)jsonObject3);
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    JsonObject fieldObj = new JsonObject();
                    fieldObj.addProperty("name", entry.getKey());
                    fieldObj.addProperty("value", String.valueOf(entry.getValue()));
                    fieldObj.addProperty("inline", Boolean.valueOf(true));
                    jsonArray.add((JsonElement)fieldObj);
                }
                jsonObject2.add("fields", (JsonElement)jsonArray);
                JsonArray jsonArray2 = new JsonArray();
                jsonArray2.add((JsonElement)jsonObject2);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("content", (String)(string7 == null || string7.isBlank() ? "" : "<@" + string7 + ">"));
                jsonObject.add("embeds", (JsonElement)jsonArray2);
                object = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                URL uRL = URI.create(string4).toURL();
                HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(8000);
                httpURLConnection.setReadTimeout(8000);
                try (OutputStream n3 = httpURLConnection.getOutputStream();){
                    n3.write((byte[])object);
                    n3.flush();
                }
                int n3 = httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }
}

