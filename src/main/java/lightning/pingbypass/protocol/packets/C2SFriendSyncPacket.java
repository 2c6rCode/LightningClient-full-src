/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package lightning.pingbypass.protocol.packets;

import java.util.ArrayList;
import java.util.List;
import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SFriendSyncPacket
extends PbPacket {
    public static final int ID = 14;
    private final List<String> friends;

    public C2SFriendSyncPacket(List<String> list) {
        this.friends = list;
    }

    public C2SFriendSyncPacket(FriendlyByteBuf friendlyByteBuf) {
        int count = friendlyByteBuf.readVarInt();
        this.friends = new ArrayList<String>(count);
        for (int i = 0; i < count; ++i) {
            this.friends.add(friendlyByteBuf.readUtf());
        }
    }

    @Override
    public int getPacketId() {
        return 14;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.friends.size());
        for (String friend : this.friends) {
            friendlyByteBuf.writeUtf(friend);
        }
    }

    public List<String> getFriends() {
        return this.friends;
    }
}

