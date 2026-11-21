/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 */
package lightning.shaded0.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import lightning.shaded0.discordipc.Opcode;
import lightning.shaded0.discordipc.Packet;
import lightning.shaded0.discordipc.connection.Connection;

public class WinConnection
extends Connection {
    private final RandomAccessFile raf;
    private final Consumer<Packet> callback;

    WinConnection(String string, Consumer<Packet> consumer) throws IOException {
        this.raf = new RandomAccessFile(string, "rw");
        this.callback = consumer;
        Thread thread = new Thread(this::run);
        thread.setName("Discord IPC - Read thread");
        thread.start();
    }

    @Override
    protected void write(ByteBuffer byteBuffer) {
        try {
            this.raf.write(byteBuffer.array());
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    private void run() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        try {
            while (true) {
                this.readFully(byteBuffer);
                Opcode opcode = Opcode.valueOf(Integer.reverseBytes(byteBuffer.getInt(0)));
                this.readFully(byteBuffer);
                int n = Integer.reverseBytes(byteBuffer.getInt(0));
                ByteBuffer byteBuffer2 = ByteBuffer.allocate(n);
                this.readFully(byteBuffer2);
                String string = Charset.defaultCharset().decode(byteBuffer2.rewind()).toString();
                this.callback.accept(new Packet(opcode, JsonParser.parseString((String)string).getAsJsonObject()));
            }
        }
        catch (Exception exception) {
            return;
        }
    }

    private void readFully(ByteBuffer byteBuffer) throws IOException {
        byteBuffer.rewind();
        while (this.raf.length() < (long)byteBuffer.remaining()) {
            Thread.onSpinWait();
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
        while (byteBuffer.hasRemaining()) {
            this.raf.getChannel().read(byteBuffer);
        }
    }

    @Override
    public void close() {
        try {
            this.raf.close();
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }
}

