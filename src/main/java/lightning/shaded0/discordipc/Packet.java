/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package lightning.shaded0.discordipc;

import com.google.gson.JsonObject;
import lightning.shaded0.discordipc.Opcode;

public record Packet(Opcode opcode, JsonObject data) {
}

