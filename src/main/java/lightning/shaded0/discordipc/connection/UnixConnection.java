/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 */
package lightning.shaded0.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import lightning.shaded0.discordipc.Opcode;
import lightning.shaded0.discordipc.Packet;
import lightning.shaded0.discordipc.connection.Connection;

public class UnixConnection
extends Connection {
    private final Selector s = Selector.open();
    private final SocketChannel sc;
    private final Consumer<Packet> callback;

    public UnixConnection(String socketPath, Consumer<Packet> callback) throws IOException {
        this.sc = SocketChannel.open(UnixDomainSocketAddress.of(socketPath));
        this.callback = callback;
        this.sc.configureBlocking(false);
        this.sc.register(this.s, 1);
        Thread thread = new Thread(this::run);
        thread.setName("Discord IPC - Read thread");
        thread.start();
    }

    private void run() {
        State state = State.Opcode;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        Buffer buffer = null;
        Opcode opcode = null;
        try {
            while (true) {
                this.s.select();
                switch (state.ordinal()) {
                    case 0: {
                        this.sc.read(byteBuffer);
                        if (byteBuffer.hasRemaining()) break;
                        opcode = Opcode.valueOf(Integer.reverseBytes(byteBuffer.getInt(0)));
                        state = State.Length;
                        byteBuffer.rewind();
                        break;
                    }
                    case 1: {
                        this.sc.read(byteBuffer);
                        if (byteBuffer.hasRemaining()) break;
                        buffer = ByteBuffer.allocate(Integer.reverseBytes(byteBuffer.getInt(0)));
                        state = State.Data;
                        byteBuffer.rewind();
                        break;
                    }
                    case 2: {
                        this.sc.read((ByteBuffer)buffer);
                        if (buffer.hasRemaining()) break;
                        String payloadJson = Charset.defaultCharset().decode(((ByteBuffer)buffer).rewind()).toString();
                        this.callback.accept(new Packet(opcode, JsonParser.parseString(payloadJson).getAsJsonObject()));
                        buffer = null;
                        state = State.Opcode;
                    }
                }
            }
        }
        catch (Exception exception) {
            return;
        }
    }

    @Override
    protected void write(ByteBuffer byteBuffer) {
        try {
            this.sc.write(byteBuffer);
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            this.s.close();
            this.sc.close();
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    private static enum State {
        Opcode,
        Length,
        Data;

    }
}

