/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 */
package lightning.gui.impl;

import java.awt.Color;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.modules.impl.core.PingBypassModule;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ProxyModuleButton
extends Button {
    private final String moduleName;

    public ProxyModuleButton(String string, Frame frame, int n) {
        super(frame, n, "PingBypass proxy module: " + string);
        this.moduleName = string;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    private boolean isEnabled() {
        PingBypassModule pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        if (pingBypassModule == null) {
            return false;
        }
        return pingBypassModule.isProxyModuleEnabled(this.moduleName);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Object object;
        boolean bl;
        if (this.isHovering(n, n2) && Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            Night.CLICK_GUI.getDescriptionFrame().setDescription(this.getDescription());
        }
        Color color = this.isHovering(n, n2) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
        Color color2 = color;
        if (color.getAlpha() > 0) {
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY(), this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight() - 1, color);
        }
        if (bl = this.isEnabled()) {
            object = ClickGuiScreen.getButtonColor(this.getY(), 255);
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY() + 1, this.getX() + this.getPadding() + 2, this.getY() + this.getHeight() - 2, (Color)object);
        }
        object = this.moduleName;
        int n3 = this.getX() + this.getTextPadding() + (bl ? 1 : 0);
        int n4 = this.getY() + 2;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(bl ? "" : ChatFormatting.GRAY) + (String)object, n3, n4, Color.WHITE);
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2) && n == 0) {
            boolean bl = !this.isEnabled();
            this.sendModuleToggle(bl);
            this.playClickSound();
        }
    }

    private void sendModuleToggle(boolean bl) {
        if (mc.getConnection() == null) {
            return;
        }
        C2SModuleTogglePacket c2SModuleTogglePacket = new C2SModuleTogglePacket(this.moduleName, bl);
        mc.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(c2SModuleTogglePacket)));
    }
}

