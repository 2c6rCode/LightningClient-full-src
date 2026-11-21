/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.util.Util
 */
package lightning.accounts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import lightning.Night;
import net.minecraft.util.Util;

public class MicrosoftAuth {
    public static final String CLIENT_ID = "54fd49e4-2103-4044-9603-2b028c814ec3";
    public static final String SCOPE = "XboxLive.signin XboxLive.offline_access";
    public static final String REDIRECT_PATH = "/in_game_account_switcher_long_enough_uri_to_prevent_accidental_leaks_on_screensharing_even_if_you_have_like_extremely_big_screen_though_it_might_not_mork_but_we_will_try_it_anyway_to_prevent_funny_things_from_happening_or_something";

    public static BrowserLoginSession startBrowserLogin(Consumer<String> consumer) throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int n = httpServer.getAddress().getPort();
        String string = "http://localhost:" + n + REDIRECT_PATH;
        String string2 = UUID.randomUUID().toString().replace("-", "");
        String string3 = "https://login.live.com/oauth20_authorize.srf?client_id=54fd49e4-2103-4044-9603-2b028c814ec3&response_type=code&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) + "&redirect_uri=" + URLEncoder.encode(string, StandardCharsets.UTF_8) + "&prompt=select_account&state=" + string2;
        CompletableFuture completableFuture = new CompletableFuture();
        httpServer.createContext(REDIRECT_PATH, httpExchange -> {
            String rawQuery = httpExchange.getRequestURI().getRawQuery();
            Map<String, String> map = MicrosoftAuth.parseQueryParams(rawQuery);
            if (map.containsKey("code")) {
                String authCode = map.get("code");
                String htmlSuccess = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Lightning</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #6450a0;box-shadow:0 8px 24px rgba(0,0,0,0.5);}h1{color:#55ffff;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Successful!</h1><p>You can close this browser tab and return to Minecraft.</p></div></body></html>";
                byte[] byArray = htmlSuccess.getBytes(StandardCharsets.UTF_8);
                httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                httpExchange.sendResponseHeaders(200, byArray.length);
                try (OutputStream outputStream = httpExchange.getResponseBody();){
                    outputStream.write(byArray);
                }
                completableFuture.complete(authCode);
            } else {
                String string4 = map.getOrDefault("error_description", map.getOrDefault("error", "Login was cancelled or failed"));
                String string5 = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Lightning</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #a03030;}h1{color:#ff5555;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Cancelled</h1><p>" + string4 + "</p></div></body></html>";
                byte[] byArray = string5.getBytes(StandardCharsets.UTF_8);
                httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                httpExchange.sendResponseHeaders(400, byArray.length);
                try (OutputStream outputStream = httpExchange.getResponseBody();){
                    outputStream.write(byArray);
                }
                completableFuture.completeExceptionally(new RuntimeException(string4));
            }
            new Thread(() -> {
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                httpServer.stop(0);
            }).start();
        });
        httpServer.start();
        CompletionStage completionStage = completableFuture.thenApplyAsync(object -> {
            try {
                consumer.accept("Exchanging authorization code with Microsoft...");
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("client_id", CLIENT_ID);
                hashMap.put("grant_type", "authorization_code");
                hashMap.put("code", (String)object);
                hashMap.put("redirect_uri", string);
                hashMap.put("scope", SCOPE);
                JsonObject jsonObject = MicrosoftAuth.postForm("https://login.live.com/oauth20_token.srf", MicrosoftAuth.encodeParams(hashMap));
                if (!jsonObject.has("access_token")) {
                    String errDesc = jsonObject.has("error_description") ? jsonObject.get("error_description").getAsString() : "Failed to obtain Microsoft token.";
                    return new AuthResult(false, null, null, null, null, errDesc);
                }
                String msToken = jsonObject.get("access_token").getAsString();
                String msRefresh = jsonObject.has("refresh_token") ? jsonObject.get("refresh_token").getAsString() : "";
                return MicrosoftAuth.completeMinecraftLogin(msToken, msRefresh, consumer);
            }
            catch (Exception exception) {
                Night.LOGGER.error("Error during Microsoft login exchange", (Throwable)exception);
                return new AuthResult(false, null, null, null, null, exception.getMessage());
            }
        });
        try {
            Util.getPlatform().openUri(URI.create(string3));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new BrowserLoginSession(string3, (CompletableFuture)completionStage, () -> {
            completableFuture.cancel(true);
            try {
                httpServer.stop(0);
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }

    public static AuthResult refreshAccount(String string2) {
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("client_id", CLIENT_ID);
            hashMap.put("grant_type", "refresh_token");
            hashMap.put("refresh_token", string2);
            hashMap.put("scope", SCOPE);
            JsonObject jsonObject = MicrosoftAuth.postForm("https://login.live.com/oauth20_token.srf", MicrosoftAuth.encodeParams(hashMap));
            if (!jsonObject.has("access_token")) {
                String string3 = jsonObject.has("error_description") ? jsonObject.get("error_description").getAsString() : "Failed to refresh Microsoft token.";
                return new AuthResult(false, null, null, null, null, string3);
            }
            String string4 = jsonObject.get("access_token").getAsString();
            String string5 = jsonObject.has("refresh_token") ? jsonObject.get("refresh_token").getAsString() : string2;
            return MicrosoftAuth.completeMinecraftLogin(string4, string5, string -> {});
        }
        catch (Exception exception) {
            Night.LOGGER.error("Failed to refresh Microsoft account", (Throwable)exception);
            return new AuthResult(false, null, null, null, null, exception.getMessage());
        }
    }

    public static AuthResult completeMinecraftLogin(String string, String string2, Consumer<String> consumer) throws Exception {
        consumer.accept("Authenticating with Xbox Live...");
        JsonObject jsonObject = new JsonObject();
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("AuthMethod", "RPS");
        jsonObject2.addProperty("SiteName", "user.auth.xboxlive.com");
        jsonObject2.addProperty("RpsTicket", "d=" + string);
        jsonObject.add("Properties", (JsonElement)jsonObject2);
        jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
        jsonObject.addProperty("TokenType", "JWT");
        JsonObject jsonObject3 = MicrosoftAuth.postJson("https://user.auth.xboxlive.com/user/authenticate", jsonObject);
        String string3 = jsonObject3.get("Token").getAsString();
        String string4 = jsonObject3.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
        consumer.accept("Requesting XSTS authorization...");
        JsonObject jsonObject4 = new JsonObject();
        JsonObject jsonObject5 = new JsonObject();
        jsonObject5.addProperty("SandboxId", "RETAIL");
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(string3);
        jsonObject5.add("UserTokens", (JsonElement)jsonArray);
        jsonObject4.add("Properties", (JsonElement)jsonObject5);
        jsonObject4.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        jsonObject4.addProperty("TokenType", "JWT");
        JsonObject jsonObject6 = MicrosoftAuth.postJson("https://xsts.auth.xboxlive.com/xsts/authorize", jsonObject4);
        if (jsonObject6.has("XErr")) {
            long l = jsonObject6.get("XErr").getAsLong();
            if (l == 2148916233L) {
                return new AuthResult(false, null, null, null, null, "This Microsoft account does not have an Xbox Live account.");
            }
            if (l == 2148916238L) {
                return new AuthResult(false, null, null, null, null, "This account is a child account and must be added to a Family.");
            }
            return new AuthResult(false, null, null, null, null, "XSTS Error: " + l);
        }
        String string5 = jsonObject6.get("Token").getAsString();
        consumer.accept("Logging into Minecraft Services...");
        JsonObject jsonObject7 = new JsonObject();
        jsonObject7.addProperty("identityToken", "XBL3.0 x=" + string4 + ";" + string5);
        JsonObject jsonObject8 = MicrosoftAuth.postJson("https://api.minecraftservices.com/authentication/login_with_xbox", jsonObject7);
        if (!jsonObject8.has("access_token")) {
            String string6 = "Failed to obtain Minecraft access token.";
            if (jsonObject8.has("errorMessage")) {
                string6 = jsonObject8.get("errorMessage").getAsString();
            } else if (jsonObject8.has("error")) {
                string6 = jsonObject8.get("error").getAsString();
            }
            return new AuthResult(false, null, null, null, null, string6);
        }
        String string7 = jsonObject8.get("access_token").getAsString();
        consumer.accept("Retrieving Minecraft profile...");
        JsonObject jsonObject9 = MicrosoftAuth.getJson("https://api.minecraftservices.com/minecraft/profile", string7);
        if (!jsonObject9.has("name") || !jsonObject9.has("id")) {
            return new AuthResult(false, null, null, null, null, "You do not own Minecraft on this Microsoft account.");
        }
        String string8 = jsonObject9.get("name").getAsString();
        String string9 = jsonObject9.get("id").getAsString();
        consumer.accept("Logged in as " + string8 + "!");
        return new AuthResult(true, string8, string9, string7, string2, null);
    }

    private static JsonObject postForm(String string, String string2) throws Exception {
        URL uRL = new URI(string).toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.setReadTimeout(15000);
        try (OutputStream os = httpURLConnection.getOutputStream()){
            os.write(string2.getBytes(StandardCharsets.UTF_8));
        }
        InputStreamReader isr = httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400 ? new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(httpURLConnection.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader(isr).getAsJsonObject();
    }

    private static JsonObject postJson(String string, JsonObject jsonObject) throws Exception {
        URL uRL = new URI(string).toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.setReadTimeout(15000);
        try (OutputStream os = httpURLConnection.getOutputStream()){
            os.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
        }
        InputStreamReader isr = httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400 ? new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(httpURLConnection.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader(isr).getAsJsonObject();
    }

    private static JsonObject getJson(String string, String string2) throws Exception {
        URL uRL = new URI(string).toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + string2);
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.setReadTimeout(15000);
        InputStreamReader inputStreamReader = httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400 ? new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(httpURLConnection.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader((Reader)inputStreamReader).getAsJsonObject();
    }

    private static String encodeParams(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (bl) {
                bl = false;
            } else {
                stringBuilder.append("&");
            }
            stringBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return stringBuilder.toString();
    }

    private static Map<String, String> parseQueryParams(String string) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        if (string == null || string.isEmpty()) {
            return hashMap;
        }
        for (String string2 : string.split("&")) {
            String[] stringArray = string2.split("=", 2);
            if (stringArray.length == 2) {
                hashMap.put(URLDecoder.decode(stringArray[0], StandardCharsets.UTF_8), URLDecoder.decode(stringArray[1], StandardCharsets.UTF_8));
                continue;
            }
            if (stringArray.length != 1) continue;
            hashMap.put(URLDecoder.decode(stringArray[0], StandardCharsets.UTF_8), "");
        }
        return hashMap;
    }

    public record BrowserLoginSession(String loginUrl, CompletableFuture<AuthResult> future, Runnable cancelAction) {
    }

    public record AuthResult(boolean success, String username, String uuid, String accessToken, String refreshToken, String errorMessage) {
    }
}

