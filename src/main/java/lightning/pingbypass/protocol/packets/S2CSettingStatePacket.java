/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CSettingStatePacket
extends PbPacket {
    public static final int ID = 6;
    private final String moduleName;
    private final String settingName;
    private final String value;

    public S2CSettingStatePacket(String moduleName, String settingName, String value) {
        this.moduleName = moduleName;
        this.settingName = settingName;
        this.value = value;
    }

    public S2CSettingStatePacket(FriendlyByteBuf friendlyByteBuf) {
        this.moduleName = friendlyByteBuf.readUtf();
        this.settingName = friendlyByteBuf.readUtf();
        this.value = friendlyByteBuf.readUtf();
    }

    @Override
    public int getPacketId() {
        return 6;
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

