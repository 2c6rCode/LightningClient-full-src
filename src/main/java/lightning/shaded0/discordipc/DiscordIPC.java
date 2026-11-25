/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package lightning.shaded0.discordipc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.management.ManagementFactory;
import java.util.function.BiConsumer;
import lightning.shaded0.discordipc.IPCUser;
import lightning.shaded0.discordipc.Opcode;
import lightning.shaded0.discordipc.Packet;
import lightning.shaded0.discordipc.RichPresence;
import lightning.shaded0.discordipc.connection.Connection;

public class DiscordIPC {
    private static final Gson GSON = new Gson();
    private static BiConsumer<Integer, String> onError = DiscordIPC::defaultErrorCallback;
    private static Connection c;
    private static Runnable onReady;
    private static boolean receivedDispatch;
    private static JsonObject queuedActivity;
    private static IPCUser user;

    public static void setOnError(BiConsumer<Integer, String> biConsumer) {
        onError = biConsumer;
    }

    public static boolean start(long clientId, Runnable runnable) {
        c = Connection.open(DiscordIPC::onPacket);
        if (c == null) {
            return false;
        }
        onReady = runnable;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("v", (Number)1);
        jsonObject.addProperty("client_id", Long.toString(clientId));
        c.write(Opcode.Handshake, jsonObject);
        return true;
    }

    public static boolean isConnected() {
        return c != null;
    }

    public static IPCUser getUser() {
        return user;
    }

    public static void setActivity(RichPresence richPresence) {
        if (c == null) {
            return;
        }
        queuedActivity = richPresence.toJson();
        if (receivedDispatch) {
            DiscordIPC.sendActivity();
        }
    }

    public static void stop() {
        if (c != null) {
            c.close();
            c = null;
            onReady = null;
            receivedDispatch = false;
            queuedActivity = null;
            user = null;
        }
    }

    private static void sendActivity() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pid", (Number)DiscordIPC.getPID());
        jsonObject.add("activity", (JsonElement)queuedActivity);
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("cmd", "SET_ACTIVITY");
        jsonObject2.add("args", (JsonElement)jsonObject);
        c.write(Opcode.Frame, jsonObject2);
        queuedActivity = null;
    }

    private static void onPacket(Packet packet) {
        if (packet.opcode() == Opcode.Close) {
            if (onError != null) {
                onError.accept(packet.data().get("code").getAsInt(), packet.data().get("message").getAsString());
            }
            DiscordIPC.stop();
        } else if (packet.opcode() == Opcode.Frame) {
            if (packet.data().has("evt") && packet.data().get("evt").getAsString().equals("ERROR")) {
                JsonObject jsonObject = packet.data().getAsJsonObject("data");
                if (onError != null) {
                    onError.accept(jsonObject.get("code").getAsInt(), jsonObject.get("message").getAsString());
                }
            } else if (packet.data().has("cmd") && packet.data().get("cmd").getAsString().equals("DISPATCH")) {
                receivedDispatch = true;
                user = (IPCUser)GSON.fromJson((JsonElement)packet.data().getAsJsonObject("data").getAsJsonObject("user"), IPCUser.class);
                if (onReady != null) {
                    onReady.run();
                }
                if (queuedActivity != null) {
                    DiscordIPC.sendActivity();
                }
            }
        }
    }

    private static int getPID() {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(runtimeName.substring(0, runtimeName.indexOf(64)));
    }

    private static void defaultErrorCallback(int errorCode, String errorMessage) {
        System.err.println("Discord IPC error " + errorCode + " with message: " + errorMessage);
    }
}

