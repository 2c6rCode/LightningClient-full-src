/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.CustomPayloadC2SPacketAccessor
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.BrandPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 */
package lightning.modules.impl.miscellaneous;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.mixins.accessors.CustomPayloadC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.StringSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@RegisterModule(name="Handshake", description="Spoofs your client handshake to make the server think that you are playing on a different client.", category=Module.Category.MISCELLANEOUS)
public class HandshakeModule
extends Module {
    public StringSetting brand = new StringSetting("Brand", "The brand that the server will think you are playing on.", "vanilla");

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        Packet<?> packet;
        if (HandshakeModule.mc.player != null && HandshakeModule.mc.level != null && (packet = packetSendEvent.getPacket()) instanceof ServerboundCustomPayloadPacket) {
            ServerboundCustomPayloadPacket serverboundCustomPayloadPacket = (ServerboundCustomPayloadPacket)packet;
            if (!serverboundCustomPayloadPacket.payload().type().id().equals((Object)BrandPayload.TYPE.id())) {
                return;
            }
            ((CustomPayloadC2SPacketAccessor)(Object)serverboundCustomPayloadPacket).setPayload((CustomPacketPayload)new BrandPayload(this.brand.getValue()));
        }
    }
}

