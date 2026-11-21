/*
 * Decompiled with CFR 0.152.
 */
package lightning.shaded0.discordipc;

public enum Opcode {
    Handshake,
    Frame,
    Close,
    Ping,
    Pong;

    private static final Opcode[] VALUES;

    public static Opcode valueOf(int n) {
        return VALUES[n];
    }

    static {
        VALUES = Opcode.values();
    }
}

