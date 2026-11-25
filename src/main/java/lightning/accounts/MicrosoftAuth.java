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

    public static BrowserLoginSession startBrowserLogin(Consumer<String> statusConsumer) throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = httpServer.getAddress().getPort();
        String redirectUri = "http://localhost:" + port + REDIRECT_PATH;
        String state = UUID.randomUUID().toString().replace("-", "");
        String authUrl = "https://login.live.com/oauth20_authorize.srf?client_id=54fd49e4-2103-4044-9603-2b028c814ec3&response_type=code&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) + "&prompt=select_account&state=" + state;
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        httpServer.createContext(REDIRECT_PATH, httpExchange -> {
            String rawQuery = httpExchange.getRequestURI().getRawQuery();
            Map<String, String> params = MicrosoftAuth.parseQueryParams(rawQuery);
            if (params.containsKey("code")) {
                String authCode = params.get("code");
                String htmlSuccess = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Lightning</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #6450a0;box-shadow:0 8px 24px rgba(0,0,0,0.5);}h1{color:#55ffff;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Successful!</h1><p>You can close this browser tab and return to Minecraft.</p></div></body></html>";
                byte[] bodyBytes = htmlSuccess.getBytes(StandardCharsets.UTF_8);
                httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                httpExchange.sendResponseHeaders(200, bodyBytes.length);
                try (OutputStream outputStream = httpExchange.getResponseBody();){
                    outputStream.write(bodyBytes);
                }
                codeFuture.complete(authCode);
            } else {
                String errorMessage = params.getOrDefault("error_description", params.getOrDefault("error", "Login was cancelled or failed"));
                String htmlError = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Lightning</title><style>body{background:#120e1e;color:#fff;font-family:sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}div{text-align:center;background:#181426;padding:40px;border-radius:12px;border:1px solid #a03030;}h1{color:#ff5555;margin-bottom:10px;}p{color:#a0a0b4;font-size:16px;}</style></head><body><div><h1>Login Cancelled</h1><p>" + errorMessage + "</p></div></body></html>";
                byte[] bodyBytes = htmlError.getBytes(StandardCharsets.UTF_8);
                httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                httpExchange.sendResponseHeaders(400, bodyBytes.length);
                try (OutputStream outputStream = httpExchange.getResponseBody();){
                    outputStream.write(bodyBytes);
                }
                codeFuture.completeExceptionally(new RuntimeException(errorMessage));
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
        CompletionStage<AuthResult> loginStage = codeFuture.thenApplyAsync(authCode -> {
            try {
                statusConsumer.accept("Exchanging authorization code with Microsoft...");
                HashMap<String, String> formParams = new HashMap<String, String>();
                formParams.put("client_id", CLIENT_ID);
                formParams.put("grant_type", "authorization_code");
                formParams.put("code", authCode);
                formParams.put("redirect_uri", redirectUri);
                formParams.put("scope", SCOPE);
                JsonObject tokenResponse = MicrosoftAuth.postForm("https://login.live.com/oauth20_token.srf", MicrosoftAuth.encodeParams(formParams));
                if (!tokenResponse.has("access_token")) {
                    String errDesc = tokenResponse.has("error_description") ? tokenResponse.get("error_description").getAsString() : "Failed to obtain Microsoft token.";
                    return new AuthResult(false, null, null, null, null, errDesc);
                }
                String msToken = tokenResponse.get("access_token").getAsString();
                String msRefresh = tokenResponse.has("refresh_token") ? tokenResponse.get("refresh_token").getAsString() : "";
                return MicrosoftAuth.completeMinecraftLogin(msToken, msRefresh, statusConsumer);
            }
            catch (Exception exception) {
                Night.LOGGER.error("Error during Microsoft login exchange", (Throwable)exception);
                return new AuthResult(false, null, null, null, null, exception.getMessage());
            }
        });
        try {
            Util.getPlatform().openUri(URI.create(authUrl));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new BrowserLoginSession(authUrl, (CompletableFuture<AuthResult>)loginStage, () -> {
            codeFuture.cancel(true);
            try {
                httpServer.stop(0);
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }

    public static AuthResult refreshAccount(String refreshToken) {
        try {
            HashMap<String, String> formParams = new HashMap<String, String>();
            formParams.put("client_id", CLIENT_ID);
            formParams.put("grant_type", "refresh_token");
            formParams.put("refresh_token", refreshToken);
            formParams.put("scope", SCOPE);
            JsonObject tokenResponse = MicrosoftAuth.postForm("https://login.live.com/oauth20_token.srf", MicrosoftAuth.encodeParams(formParams));
            if (!tokenResponse.has("access_token")) {
                String errorDesc = tokenResponse.has("error_description") ? tokenResponse.get("error_description").getAsString() : "Failed to refresh Microsoft token.";
                return new AuthResult(false, null, null, null, null, errorDesc);
            }
            String msToken = tokenResponse.get("access_token").getAsString();
            String newRefreshToken = tokenResponse.has("refresh_token") ? tokenResponse.get("refresh_token").getAsString() : refreshToken;
            return MicrosoftAuth.completeMinecraftLogin(msToken, newRefreshToken, status -> {});
        }
        catch (Exception exception) {
            Night.LOGGER.error("Failed to refresh Microsoft account", (Throwable)exception);
            return new AuthResult(false, null, null, null, null, exception.getMessage());
        }
    }

    public static AuthResult completeMinecraftLogin(String msAccessToken, String msRefreshToken, Consumer<String> statusConsumer) throws Exception {
        statusConsumer.accept("Authenticating with Xbox Live...");
        JsonObject xblPayload = new JsonObject();
        JsonObject xblProperties = new JsonObject();
        xblProperties.addProperty("AuthMethod", "RPS");
        xblProperties.addProperty("SiteName", "user.auth.xboxlive.com");
        xblProperties.addProperty("RpsTicket", "d=" + msAccessToken);
        xblPayload.add("Properties", (JsonElement)xblProperties);
        xblPayload.addProperty("RelyingParty", "http://auth.xboxlive.com");
        xblPayload.addProperty("TokenType", "JWT");
        JsonObject xblResponse = MicrosoftAuth.postJson("https://user.auth.xboxlive.com/user/authenticate", xblPayload);
        String xblToken = xblResponse.get("Token").getAsString();
        String userHash = xblResponse.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
        statusConsumer.accept("Requesting XSTS authorization...");
        JsonObject xstsPayload = new JsonObject();
        JsonObject xstsProperties = new JsonObject();
        xstsProperties.addProperty("SandboxId", "RETAIL");
        JsonArray userTokens = new JsonArray();
        userTokens.add(xblToken);
        xstsProperties.add("UserTokens", (JsonElement)userTokens);
        xstsPayload.add("Properties", (JsonElement)xstsProperties);
        xstsPayload.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        xstsPayload.addProperty("TokenType", "JWT");
        JsonObject xstsResponse = MicrosoftAuth.postJson("https://xsts.auth.xboxlive.com/xsts/authorize", xstsPayload);
        if (xstsResponse.has("XErr")) {
            long xstsError = xstsResponse.get("XErr").getAsLong();
            if (xstsError == 2148916233L) {
                return new AuthResult(false, null, null, null, null, "This Microsoft account does not have an Xbox Live account.");
            }
            if (xstsError == 2148916238L) {
                return new AuthResult(false, null, null, null, null, "This account is a child account and must be added to a Family.");
            }
            return new AuthResult(false, null, null, null, null, "XSTS Error: " + xstsError);
        }
        String xstsToken = xstsResponse.get("Token").getAsString();
        statusConsumer.accept("Logging into Minecraft Services...");
        JsonObject mcLoginPayload = new JsonObject();
        mcLoginPayload.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
        JsonObject mcLoginResponse = MicrosoftAuth.postJson("https://api.minecraftservices.com/authentication/login_with_xbox", mcLoginPayload);
        if (!mcLoginResponse.has("access_token")) {
            String errorMsg = "Failed to obtain Minecraft access token.";
            if (mcLoginResponse.has("errorMessage")) {
                errorMsg = mcLoginResponse.get("errorMessage").getAsString();
            } else if (mcLoginResponse.has("error")) {
                errorMsg = mcLoginResponse.get("error").getAsString();
            }
            return new AuthResult(false, null, null, null, null, errorMsg);
        }
        String mcAccessToken = mcLoginResponse.get("access_token").getAsString();
        statusConsumer.accept("Retrieving Minecraft profile...");
        JsonObject profileResponse = MicrosoftAuth.getJson("https://api.minecraftservices.com/minecraft/profile", mcAccessToken);
        if (!profileResponse.has("name") || !profileResponse.has("id")) {
            return new AuthResult(false, null, null, null, null, "You do not own Minecraft on this Microsoft account.");
        }
        String username = profileResponse.get("name").getAsString();
        String uuid = profileResponse.get("id").getAsString();
        statusConsumer.accept("Logged in as " + username + "!");
        return new AuthResult(true, username, uuid, mcAccessToken, msRefreshToken, null);
    }

    private static JsonObject postForm(String urlString, String body) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        try (OutputStream os = connection.getOutputStream()){
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        InputStreamReader isr = connection.getResponseCode() >= 200 && connection.getResponseCode() < 400 ? new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader(isr).getAsJsonObject();
    }

    private static JsonObject postJson(String urlString, JsonObject payload) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        try (OutputStream os = connection.getOutputStream()){
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }
        InputStreamReader isr = connection.getResponseCode() >= 200 && connection.getResponseCode() < 400 ? new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader(isr).getAsJsonObject();
    }

    private static JsonObject getJson(String urlString, String bearerToken) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        InputStreamReader inputStreamReader = connection.getResponseCode() >= 200 && connection.getResponseCode() < 400 ? new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8) : new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8);
        return JsonParser.parseReader((Reader)inputStreamReader).getAsJsonObject();
    }

    private static String encodeParams(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private static Map<String, String> parseQueryParams(String query) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                params.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                continue;
            }
            if (parts.length != 1) continue;
            params.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), "");
        }
        return params;
    }

    public record BrowserLoginSession(String loginUrl, CompletableFuture<AuthResult> future, Runnable cancelAction) {
    }

    public record AuthResult(boolean success, String username, String uuid, String accessToken, String refreshToken, String errorMessage) {
    }
}

