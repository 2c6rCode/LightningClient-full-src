/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.Unpooled
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 */
package lightning.pingbypass.protocol;

import io.netty.buffer.Unpooled;
import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PbCustomPayload(byte[] data) implements CustomPacketPayload
{
    public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath((String)"lightning", (String)"pingbypass");
    public static final CustomPacketPayload.Type<PbCustomPayload> ID = new CustomPacketPayload.Type(CHANNEL);
    public static final int C2S_JOIN = 0;
    public static final int S2C_PASSWORD_REQUEST = 1;
    public static final int C2S_PASSWORD = 2;
    public static final int S2C_ERROR = 7;
    public static final StreamCodec<RegistryFriendlyByteBuf, PbCustomPayload> CODEC = new StreamCodec<RegistryFriendlyByteBuf, PbCustomPayload>(){

        public PbCustomPayload decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            byte[] byArray = new byte[registryFriendlyByteBuf.readableBytes()];
            registryFriendlyByteBuf.readBytes(byArray);
            return new PbCustomPayload(byArray);
        }

        public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, PbCustomPayload pbCustomPayload) {
            registryFriendlyByteBuf.writeBytes(pbCustomPayload.data());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public FriendlyByteBuf toBuf() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])this.data));
    }

    public static PbCustomPayload fromPacket(PbPacket pbPacket) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlyByteBuf.writeVarInt(pbPacket.getPacketId());
        pbPacket.write(friendlyByteBuf);
        byte[] byArray = new byte[friendlyByteBuf.readableBytes()];
        friendlyByteBuf.readBytes(byArray);
        friendlyByteBuf.release();
        return new PbCustomPayload(byArray);
    }

    public static ServerboundCustomPayloadPacket createC2SPacket(PbPacket pbPacket) {
        return new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(pbPacket));
    }

    public static PbCustomPayload passwordRequest() {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlyByteBuf.writeVarInt(1);
        byte[] byArray = new byte[friendlyByteBuf.readableBytes()];
        friendlyByteBuf.readBytes(byArray);
        friendlyByteBuf.release();
        return new PbCustomPayload(byArray);
    }

    public static PbCustomPayload error(String message) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlyByteBuf.writeVarInt(7);
        friendlyByteBuf.writeUtf(message);
        byte[] byArray = new byte[friendlyByteBuf.readableBytes()];
        friendlyByteBuf.readBytes(byArray);
        friendlyByteBuf.release();
        return new PbCustomPayload(byArray);
    }
}

