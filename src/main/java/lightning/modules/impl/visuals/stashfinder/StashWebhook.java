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

    public static void setWebhookUrl(String url) {
        StashWebhook.ensureLoaded();
        webhookUrl = url;
        StashWebhook.save();
    }

    public static void setUserId(String id) {
        StashWebhook.ensureLoaded();
        userId = id;
        StashWebhook.save();
    }

    private static synchronized boolean tryClaimSendSlot() {
        long now = System.currentTimeMillis();
        if (now - lastSentAtMs < 5000L) {
            return false;
        }
        lastSentAtMs = now;
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

    public static void send(int x, int z, String dimension, Map<String, Integer> counts, String webhookUrlOverride, String userIdOverride) {
        String targetUrl = webhookUrlOverride != null && !webhookUrlOverride.isBlank() ? webhookUrlOverride : StashWebhook.getWebhookUrl();
        if (targetUrl == null || targetUrl.isBlank()) {
            return;
        }
        if (!StashWebhook.tryClaimSendSlot()) {
            return;
        }
        String server = StashWebhook.serverLabel();
        String pingUserId = userIdOverride != null && !userIdOverride.isBlank() ? userIdOverride : StashWebhook.getUserId();
        CompletableFuture.runAsync(() -> {
            try {
                JsonObject embed = new JsonObject();
                embed.addProperty("title", "Stash Found!");
                embed.addProperty("description", "Coordinates: **X: " + x + " Z: " + z + "** (" + dimension + ")");
                embed.addProperty("color", (Number)3066993);
                JsonArray fields = new JsonArray();
                JsonObject serverField = new JsonObject();
                serverField.addProperty("name", "Server");
                serverField.addProperty("value", server);
                serverField.addProperty("inline", Boolean.valueOf(false));
                fields.add((JsonElement)serverField);
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    JsonObject fieldObj = new JsonObject();
                    fieldObj.addProperty("name", entry.getKey());
                    fieldObj.addProperty("value", String.valueOf(entry.getValue()));
                    fieldObj.addProperty("inline", Boolean.valueOf(true));
                    fields.add((JsonElement)fieldObj);
                }
                embed.add("fields", (JsonElement)fields);
                JsonArray embeds = new JsonArray();
                embeds.add((JsonElement)embed);
                JsonObject body = new JsonObject();
                body.addProperty("content", (String)(pingUserId == null || pingUserId.isBlank() ? "" : "<@" + pingUserId + ">"));
                body.add("embeds", (JsonElement)embeds);
                byte[] payloadBytes = body.toString().getBytes(StandardCharsets.UTF_8);
                URL uRL = URI.create(targetUrl).toURL();
                HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(8000);
                httpURLConnection.setReadTimeout(8000);
                try (OutputStream outputStream = httpURLConnection.getOutputStream();){
                    outputStream.write(payloadBytes);
                    outputStream.flush();
                }
                int responseCode = httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }
}

