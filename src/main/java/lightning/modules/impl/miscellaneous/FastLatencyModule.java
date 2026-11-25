/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket
 *  net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket
 */
package lightning.modules.impl.miscellaneous;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.system.Timer;
import lightning.utils.system.ZeroTimer;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

@RegisterModule(name="FastLatency", description="Makes ping resolving much faster.", category=Module.Category.MISCELLANEOUS)
public class FastLatencyModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "The amount of milliseconds that have to be waited for before resolving your ping again.", 100L, 0, 1000L);
    public BooleanSetting spikeNotifier = new BooleanSetting("SpikeNotifier", "Notifies you in chat whenever your ping spikes.", false);
    public NumberSetting threshold = new NumberSetting("Threshold", "The amount of milliseconds that your ping has to increase for before notifying you.", new BooleanSetting.Visibility(this.spikeNotifier, true), (Number)30, (Number)0, (Number)1000);
    private final Timer timer = new Timer();
    private final ZeroTimer receivedTimer = new ZeroTimer();
    private long time;
    private int latency;

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (FastLatencyModule.mc.player == null) {
            return;
        }
        if (this.receivedTimer.hasTimeElapsed(1000L) && this.timer.hasTimeElapsed(this.delay.getValue().longValue())) {
            mc.getConnection().send((Packet)new ServerboundCommandSuggestionPacket(1000, "/w "));
            this.time = System.currentTimeMillis();
            this.receivedTimer.reset();
            this.timer.reset();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        ClientboundCommandSuggestionsPacket clientboundCommandSuggestionsPacket;
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundCommandSuggestionsPacket && (clientboundCommandSuggestionsPacket = (ClientboundCommandSuggestionsPacket)packet).id() == 1000) {
            int pingMs = (int)(System.currentTimeMillis() - this.time);
            if (this.spikeNotifier.getValue() && pingMs - this.latency > this.threshold.getValue().intValue()) {
                Night.CHAT_MANAGER.message("Your ping has spiked to " + String.valueOf(ChatUtils.getPrimary()) + pingMs + "ms" + String.valueOf(ChatUtils.getSecondary()) + " from " + String.valueOf(ChatUtils.getPrimary()) + this.latency + "ms" + String.valueOf(ChatUtils.getSecondary()) + "!", "module-" + this.getName().toLowerCase() + "-spike");
            }
            this.latency = pingMs;
            this.receivedTimer.zero();
        }
    }

    @Override
    public String getMetaData() {
        return this.latency + "ms";
    }

    @Generated
    public int getLatency() {
        return this.latency;
    }
}

