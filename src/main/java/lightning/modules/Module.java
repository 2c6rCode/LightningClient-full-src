/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 */
package lightning.modules;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.events.impl.ToggleModuleEvent;
import lightning.modules.RegisterModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.modules.PbModuleManager;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.pingbypass.protocol.packets.S2CModuleStatePacket;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.PositionSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.IMinecraft;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.chat.ChatUtils;
import lightning.utils.minecraft.InventoryUtils;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public abstract class Module
implements IMinecraft {
    private final String name;
    private final String description;
    private final Category category;
    private final boolean persistent;
    private final boolean proxyEnhanced;
    private boolean toggled;
    private final List<Setting> settings;
    public BooleanSetting chatNotify;
    public BooleanSetting drawn;
    public BindSetting bind;
    public ModeSetting proxyMode;
    private final Animation animationOffset;

    public Module() {
        RegisterModule registerModule = this.getClass().getAnnotation(RegisterModule.class);
        this.name = registerModule.name();
        this.description = registerModule.description();
        this.category = registerModule.category();
        this.persistent = registerModule.persistent();
        this.proxyEnhanced = registerModule.proxyEnhanced();
        this.toggled = registerModule.toggled();
        this.settings = new ArrayList<Setting>();
        this.animationOffset = new Animation(300, Easing.Method.EASE_IN_OUT_CUBIC);
        this.chatNotify = new BooleanSetting("ChatNotify", "Notifies you in chat whenever the module gets toggled on or off.", true);
        this.drawn = new BooleanSetting("Drawn", "Renders the module's name on the HUD's module list.", registerModule.drawn());
        this.bind = new BindSetting("Bind", "The keybind that toggles the module on and off.", registerModule.bind());
        if (this.proxyEnhanced) {
            this.proxyMode = new ModeSetting("ProxyMode", "Where this module executes when connected to a PingBypass proxy.", "Auto", new String[]{"Auto", "Proxy", "Local"});
        }
        if (this.persistent) {
            this.toggled = true;
        }
        if (this.toggled) {
            Night.EVENT_HANDLER.subscribe(this);
        }
    }

    public boolean getNull() {
        return Module.mc.player == null || Module.mc.level == null;
    }

    public boolean shouldRunOnProxy() {
        if (!this.proxyEnhanced || this.proxyMode == null) {
            return false;
        }
        if (!PingBypassFlags.proxyForwardingActive) {
            return false;
        }
        if (Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return false;
        }
        return switch (this.proxyMode.getValue()) {
            case "Auto", "Proxy" -> true;
            case "Local" -> false;
            default -> true;
        };
    }

    public boolean shouldSkipActions() {
        return this.shouldRunOnProxy();
    }

    public boolean isRunningOnProxy() {
        if (!this.proxyEnhanced) {
            return false;
        }
        if (!PingBypassFlags.proxyForwardingActive) {
            return false;
        }
        if (Night.PINGBYPASS_CONFIG == null || !Night.PINGBYPASS_CONFIG.isServer()) {
            return false;
        }
        return this.proxyMode == null || !this.proxyMode.getValue().equals("Local");
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public String getMetaData() {
        return "";
    }

    public String getProxyIndicator() {
        if (this.shouldRunOnProxy() && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return " \u00a7d[PB]";
        }
        return "";
    }

    private boolean shouldSkipLocalExecution() {
        return this.shouldRunOnProxy();
    }

    public void setToggled(boolean bl) {
        this.setToggled(bl, true);
    }

    public void setToggled(boolean bl, boolean bl2) {
        if (this.persistent) {
            return;
        }
        if (bl == this.toggled) {
            return;
        }
        if (bl) {
            // empty if block
        }
        this.toggled = bl;
        Night.EVENT_HANDLER.post(new ToggleModuleEvent(this, this.toggled));
        if ((this.shouldRunOnProxy() || PbModuleManager.MIGRATED_MODULE_NAMES.contains(this.name)) && bl2 && Night.PINGBYPASS_CONFIG != null && !Night.PINGBYPASS_CONFIG.isServer()) {
            this.sendProxyToggle(this.toggled);
        }
        if (this.proxyEnhanced && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && PingBypassFlags.proxyForwardingActive && Night.PROXY_SERVER != null) {
            ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CModuleStatePacket(this.name, this.toggled)));
            for (Connection connection : Night.PROXY_SERVER.getConnections()) {
                if (!connection.isConnected()) continue;
                connection.send((Packet)clientboundCustomPayloadPacket);
            }
        }
        if (this.toggled) {
            this.animationOffset.setEasing(Easing.Method.EASE_IN_OUT_CUBIC);
            if (bl2 && this.chatNotify.getValue()) {
                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + this.name + String.valueOf(ChatUtils.getSecondary()) + " = " + String.valueOf(ChatFormatting.GREEN) + "true" + String.valueOf(ChatUtils.getSecondary()) + ";", "toggle-" + this.getName().toLowerCase());
            }
            this.onEnable();
            if (this.toggled) {
                Night.EVENT_HANDLER.subscribe(this);
            }
        } else {
            this.animationOffset.setEasing(Easing.Method.EASE_IN_OUT_CUBIC);
            Night.EVENT_HANDLER.unsubscribe(this);
            this.onDisable();
            InventoryUtils.releaseAllBorrows();
            if (bl2 && this.chatNotify.getValue()) {
                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getPrimary()) + this.name + String.valueOf(ChatUtils.getSecondary()) + " = " + String.valueOf(ChatFormatting.RED) + "false" + String.valueOf(ChatUtils.getSecondary()) + ";", "toggle-" + this.getName().toLowerCase());
            }
        }
    }

    public int getBind() {
        return this.bind.getValue();
    }

    public void setBind(int n) {
        this.bind.setValue(n);
    }

    public void resetValues() {
        for (Setting setting : this.settings) {
            Setting setting2;
            if (setting instanceof BooleanSetting) {
                setting2 = (BooleanSetting)setting;
                ((BooleanSetting)setting2).resetValue();
            }
            if (setting instanceof NumberSetting) {
                setting2 = (NumberSetting)setting;
                ((NumberSetting)setting2).resetValue();
            }
            if (setting instanceof ModeSetting) {
                setting2 = (ModeSetting)setting;
                ((ModeSetting)setting2).resetValue();
            }
            if (setting instanceof StringSetting) {
                setting2 = (StringSetting)setting;
                ((StringSetting)setting2).resetValue();
            }
            if (setting instanceof BindSetting) {
                setting2 = (BindSetting)setting;
                ((BindSetting)setting2).resetValue();
            }
            if (setting instanceof WhitelistSetting) {
                setting2 = (WhitelistSetting)setting;
                ((WhitelistSetting)setting2).clear();
            }
            if (setting instanceof ColorSetting) {
                setting2 = (ColorSetting)setting;
                ((ColorSetting)setting2).resetValue();
            }
            if (!(setting instanceof PositionSetting)) continue;
            setting2 = (PositionSetting)setting;
            ((PositionSetting)setting2).resetValue();
        }
    }

    public Setting getSetting(String string) {
        return this.settings.stream().filter(setting -> setting.getName().equalsIgnoreCase(string) && !(setting instanceof CategorySetting)).findFirst().orElse(null);
    }

    private void sendProxyToggle(boolean bl) {
        try {
            if (mc.getConnection() != null) {
                PbCustomPayload pbCustomPayload = PbCustomPayload.fromPacket(new C2SModuleTogglePacket(this.name, bl));
                mc.getConnection().getConnection().send((Packet)new ServerboundCustomPayloadPacket((CustomPacketPayload)pbCustomPayload));
            }
        }
        catch (Exception exception) {
            Night.LOGGER.warn("[PingBypass] Failed to send module toggle to proxy", (Throwable)exception);
        }
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public Category getCategory() {
        return this.category;
    }

    @Generated
    public boolean isPersistent() {
        return this.persistent;
    }

    @Generated
    public boolean isProxyEnhanced() {
        return this.proxyEnhanced;
    }

    @Generated
    public boolean isToggled() {
        return this.toggled;
    }

    @Generated
    public List<Setting> getSettings() {
        return this.settings;
    }

    @Generated
    public BooleanSetting getChatNotify() {
        return this.chatNotify;
    }

    @Generated
    public BooleanSetting getDrawn() {
        return this.drawn;
    }

    @Generated
    public ModeSetting getProxyMode() {
        return this.proxyMode;
    }

    @Generated
    public Animation getAnimationOffset() {
        return this.animationOffset;
    }

    public static enum Category {
        COMBAT("Combat"),
        PLAYER("Player"),
        VISUALS("Visuals"),
        MOVEMENT("Movement"),
        MISCELLANEOUS("Miscellaneous"),
        CORE("Core");

        private final String name;

        private Category(String string2) {
            this.name = string2;
        }

        @Generated
        public String getName() {
            return this.name;
        }
    }
}

