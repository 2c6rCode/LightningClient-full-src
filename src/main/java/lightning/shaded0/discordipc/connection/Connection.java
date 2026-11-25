/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package lightning.shaded0.discordipc.connection;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;
import lightning.shaded0.discordipc.Opcode;
import lightning.shaded0.discordipc.Packet;
import lightning.shaded0.discordipc.connection.UnixConnection;
import lightning.shaded0.discordipc.connection.WinConnection;

public abstract class Connection {
    private static final String[] UNIX_TEMP_PATHS = new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"};

    public static Connection open(Consumer<Packet> callback) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            for (int i = 0; i < 10; ++i) {
                try {
                    return new WinConnection("\\\\?\\pipe\\discord-ipc-" + i, callback);
                }
                catch (IOException iOException) {
                    continue;
                }
            }
        } else {
            String tempDir = null;
            for (String envVar : UNIX_TEMP_PATHS) {
                tempDir = System.getenv(envVar);
                if (tempDir != null) break;
            }
            if (tempDir == null) {
                tempDir = "/tmp";
            }
            String socketPrefix = tempDir + "/discord-ipc-";
            for (int i = 0; i < 10; ++i) {
                try {
                    return new UnixConnection(socketPrefix + i, callback);
                }
                catch (IOException iOException) {
                    continue;
                }
            }
        }
        return null;
    }

    public void write(Opcode opcode, JsonObject payload) {
        payload.addProperty("nonce", UUID.randomUUID().toString());
        byte[] payloadBytes = payload.toString().getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(payloadBytes.length + 8);
        byteBuffer.putInt(Integer.reverseBytes(opcode.ordinal()));
        byteBuffer.putInt(Integer.reverseBytes(payloadBytes.length));
        byteBuffer.put(payloadBytes);
        byteBuffer.rewind();
        this.write(byteBuffer);
    }

    protected abstract void write(ByteBuffer buffer);

    public abstract void close();
}

