/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSystemChatPacket
 */
package lightning.modules.impl.miscellaneous;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.Timer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@RegisterModule(name="AutoLogin", description="Automatically logs in on cracked servers.", category=Module.Category.MISCELLANEOUS)
public class AutoLoginModule
extends Module {
    public StringSetting password = new StringSetting("Password", "The password to use when logging in.", "password");
    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (this.getNull() || !this.timer.hasTimeElapsed(10000)) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket clientboundSystemChatPacket = (ClientboundSystemChatPacket)packet;
            String message = clientboundSystemChatPacket.content().getString().toLowerCase();
            if (message.contains("/register")) {
                mc.getConnection().sendCommand("register " + this.password.getValue() + " " + this.password.getValue());
                Night.CHAT_MANAGER.tagged("Registered successfully.", this.getName());
                this.timer.reset();
            } else if (message.contains("/login")) {
                mc.getConnection().sendCommand("login " + this.password.getValue());
                Night.CHAT_MANAGER.tagged("Logged in as " + mc.getUser().getName() + ".", this.getName());
                this.timer.reset();
            }
        }
    }
}

