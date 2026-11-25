/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SSettingChangePacket
extends PbPacket {
    public static final int ID = 4;
    private final String moduleName;
    private final String settingName;
    private final String value;

    public C2SSettingChangePacket(String moduleName, String settingName, String value) {
        this.moduleName = moduleName;
        this.settingName = settingName;
        this.value = value;
    }

    public C2SSettingChangePacket(FriendlyByteBuf friendlyByteBuf) {
        this.moduleName = friendlyByteBuf.readUtf();
        this.settingName = friendlyByteBuf.readUtf();
        this.value = friendlyByteBuf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 4;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.moduleName);
        friendlyByteBuf.writeUtf(this.settingName);
        friendlyByteBuf.writeUtf(this.value);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getSettingName() {
        return this.settingName;
    }

    public String getValue() {
        return this.value;
    }
}

