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

    public static Connection open(Consumer<Packet> consumer) {
        String string = System.getProperty("os.name").toLowerCase();
        if (string.contains("win")) {
            for (int i = 0; i < 10; ++i) {
                try {
                    return new WinConnection("\\\\?\\pipe\\discord-ipc-" + i, consumer);
                }
                catch (IOException iOException) {
                    continue;
                }
            }
        } else {
            String string2;
            int n;
            Object object = null;
            String[] stringArray = UNIX_TEMP_PATHS;
            int n2 = stringArray.length;
            for (n = 0; n < n2 && (object = System.getenv(string2 = stringArray[n])) == null; ++n) {
            }
            if (object == null) {
                object = "/tmp";
            }
            object = (String)object + "/discord-ipc-";
            for (n = 0; n < 10; ++n) {
                try {
                    return new UnixConnection((String)object + n, consumer);
                }
                catch (IOException iOException) {
                    continue;
                }
            }
        }
        return null;
    }

    public void write(Opcode opcode, JsonObject jsonObject) {
        jsonObject.addProperty("nonce", UUID.randomUUID().toString());
        byte[] byArray = jsonObject.toString().getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(byArray.length + 8);
        byteBuffer.putInt(Integer.reverseBytes(opcode.ordinal()));
        byteBuffer.putInt(Integer.reverseBytes(byArray.length));
        byteBuffer.put(byArray);
        byteBuffer.rewind();
        this.write(byteBuffer);
    }

    protected abstract void write(ByteBuffer var1);

    public abstract void close();
}

