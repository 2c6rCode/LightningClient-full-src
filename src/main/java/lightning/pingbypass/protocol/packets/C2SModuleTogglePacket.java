/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SModuleTogglePacket
extends PbPacket {
    public static final int ID = 3;
    private final String moduleName;
    private final boolean enabled;

    public C2SModuleTogglePacket(String moduleName, boolean enabled) {
        this.moduleName = moduleName;
        this.enabled = enabled;
    }

    public C2SModuleTogglePacket(FriendlyByteBuf friendlyByteBuf) {
        this.moduleName = friendlyByteBuf.readUtf();
        this.enabled = friendlyByteBuf.readBoolean();
    }

    @Override
    public int getPacketId() {
        return 3;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.moduleName);
        friendlyByteBuf.writeBoolean(this.enabled);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}

