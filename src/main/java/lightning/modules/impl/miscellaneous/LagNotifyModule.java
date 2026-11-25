/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.miscellaneous;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderOverlayEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.system.MathUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="LagNotify", description="Notifies you when you lag.", category=Module.Category.MISCELLANEOUS)
public class LagNotifyModule
extends Module {
    BooleanSetting server = new BooleanSetting("Server", "Notifies you when the server stops responding.", true);
    BooleanSetting lagback = new BooleanSetting("Lagback", "Notifies you when you lagback.", true);
    ColorSetting color = new ColorSetting("Color", "The color of the notification text.", ColorUtils.getDefaultOutlineColor());
    Vec3 lagPos = null;
    double lagDistance;
    long lagTime = System.currentTimeMillis();

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (this.getNull()) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket clientboundPlayerPositionPacket = (ClientboundPlayerPositionPacket)packet;
            this.lagPos = new Vec3(clientboundPlayerPositionPacket.change().position().x(), clientboundPlayerPositionPacket.change().position().y(), clientboundPlayerPositionPacket.change().position().z());
            this.lagDistance = LagNotifyModule.mc.player.position().distanceTo(this.lagPos);
            this.lagTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent renderOverlayEvent) {
        String message;
        if (this.getNull()) {
            return;
        }
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int baseY = mc.getWindow().getGuiScaledHeight() / 4;
        boolean showedServerWarning = false;
        if (this.server.getValue() && Night.SERVER_MANAGER.getResponseTimer().hasTimeElapsed(1000)) {
            message = "Detected server not responding for " + MathUtils.round((float)Night.SERVER_MANAGER.getResponseTimer().timeElapsed() / 1000.0f, 1) + "s.";
            Night.FONT_MANAGER.drawTextWithShadow(renderOverlayEvent.getContext(), message, centerX - Night.FONT_MANAGER.getWidth(message) / 2, baseY - Night.FONT_MANAGER.getHeight(), this.color.getColor());
            showedServerWarning = true;
        }
        if (this.lagback.getValue() && System.currentTimeMillis() - this.lagTime < 3000L) {
            message = "Detected lagback of " + MathUtils.round(this.lagDistance, 1) + " blocks " + MathUtils.round((float)(System.currentTimeMillis() - this.lagTime) / 1000.0f, 1) + "s.";
            Night.FONT_MANAGER.drawTextWithShadow(renderOverlayEvent.getContext(), message, centerX - Night.FONT_MANAGER.getWidth(message) / 2, baseY - Night.FONT_MANAGER.getHeight() + (showedServerWarning ? Night.FONT_MANAGER.getHeight() : 0), this.color.getColor());
        }
    }
}

