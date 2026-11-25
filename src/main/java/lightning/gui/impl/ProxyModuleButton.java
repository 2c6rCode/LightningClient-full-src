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

    public ProxyModuleButton(String moduleName, Frame frame, int height) {
        super(frame, height, "PingBypass proxy module: " + moduleName);
        this.moduleName = moduleName;
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
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        boolean enabled;
        if (this.isHovering(mouseX, mouseY) && Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            Night.CLICK_GUI.getDescriptionFrame().setDescription(this.getDescription());
        }
        Color hoverBg = this.isHovering(mouseX, mouseY) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
        if (hoverBg.getAlpha() > 0) {
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY(), this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight() - 1, hoverBg);
        }
        if (enabled = this.isEnabled()) {
            Color accentColor = ClickGuiScreen.getButtonColor(this.getY(), 255);
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY() + 1, this.getX() + this.getPadding() + 2, this.getY() + this.getHeight() - 2, accentColor);
        }
        String label = this.moduleName;
        int textX = this.getX() + this.getTextPadding() + (enabled ? 1 : 0);
        int textY = this.getY() + 2;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, String.valueOf(enabled ? "" : ChatFormatting.GRAY) + label, textX, textY, Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY) && button == 0) {
            boolean nextState = !this.isEnabled();
            this.sendModuleToggle(nextState);
            this.playClickSound();
        }
    }

    private void sendModuleToggle(boolean enabled) {
        if (mc.getConnection() == null) {
            return;
        }
        C2SModuleTogglePacket c2SModuleTogglePacket = new C2SModuleTogglePacket(this.moduleName, enabled);
        mc.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(c2SModuleTogglePacket)));
    }
}

