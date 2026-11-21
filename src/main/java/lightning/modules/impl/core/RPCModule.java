/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.modules.impl.core;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.shaded0.discordipc.DiscordIPC;
import lightning.shaded0.discordipc.RichPresence;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterModule(name="RPC", description="Enables Discord Rich Presence for the client.", category=Module.Category.CORE)
public class RPCModule
extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Lightning/RPC");
    public BooleanSetting showUid = new BooleanSetting("UUID", "Whether or not to show your client UID on the Discord presence.", true);
    public ModeSetting detailsMode = new ModeSetting("Details", "The mode for the discord presence details.", "Random", new String[]{"Custom", "Random"});
    public StringSetting customDetails = new StringSetting("CustomDetails", "Custom RPC text.", new ModeSetting.Visibility(this.detailsMode, "Custom"), "Recode by PhatNho");
    public ModeSetting imageMode = new ModeSetting("Image", "Which large image to show on the Rich Presence card.", "Image", new String[]{"Logo", "Image", "Animated"});
    private static final String LOGO_ASSET_KEY = "logo";
    private static final String IMAGE_ASSET_KEY = "image";
    private static final long LOGO_APP_ID = 1567958367148707900L;
    private static final long IMAGE_APP_ID = 1567958367148707900L;
    private static final int ANIMATED_FRAME_COUNT = 32;
    public NumberSetting animatedDelay = new NumberSetting("AnimatedDelay", "Milliseconds between each frame swap.", 1500, 0, 10000);
    private int frameIndex = 0;
    private final Timer frameTimer = new Timer();
    private final String[] DETAILS = new String[]{"Recode by PhatNho"};
    private final RichPresence rpc = new RichPresence();
    private final Timer timer = new Timer();

    @Override
    public void onEnable() {
        DiscordIPC.setOnError((n, string) -> LOGGER.warn("[RPC] Discord IPC error {}: {}", n, string));
        this.rpc.setStart(Night.UPTIME / 1000L);
        this.connect();
    }

    private void connect() {
        DiscordIPC.stop();
        boolean bl = DiscordIPC.start(this.appIdForMode(), () -> LOGGER.info("[RPC] Connected to Discord IPC"));
        if (!bl) {
            LOGGER.warn("[RPC] DiscordIPC.start() returned false -- no local Discord IPC pipe found (is Discord running?)");
            return;
        }
        this.rpc.setDetails(this.getDetails());
        this.applyState();
        this.applyImageMode();
        DiscordIPC.setActivity(this.rpc);
    }

    private void applyState() {
        if (!this.showUid.getValue()) {
            this.rpc.setState(null);
            return;
        }
        String string = "30th6_";
        this.rpc.setState(string != null ? "UID " + string : null);
    }

    private long appIdForMode() {
        return this.imageMode.getValue().equals("Logo") ? 1567958367148707900L : 1567958367148707900L;
    }

    private void applyImageMode() {
        if (this.imageMode.getValue().equals("Logo")) {
            this.rpc.setLargeImage(LOGO_ASSET_KEY, "LightningClient");
        } else if (this.imageMode.getValue().equals("Animated")) {
            this.rpc.setLargeImage(this.frameKey(), "LightningClient");
        } else {
            this.rpc.setLargeImage(IMAGE_ASSET_KEY, "LightningClient");
        }
    }

    private String frameKey() {
        return String.format("frame_%02d", this.frameIndex + 1);
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent settingChangeEvent) {
        if (settingChangeEvent.getSetting() == this.imageMode) {
            this.connect();
        } else if (settingChangeEvent.getSetting() == this.showUid || settingChangeEvent.getSetting() == this.customDetails || settingChangeEvent.getSetting() == this.detailsMode) {
            this.rpc.setDetails(this.detailsMode.getValue().equals("Random") ? this.getDetails() : this.customDetails.getValue());
            this.applyState();
            this.applyImageMode();
            DiscordIPC.setActivity(this.rpc);
        }
    }

    @Override
    public void onDisable() {
        DiscordIPC.stop();
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (this.detailsMode.getValue().equals("Random") && this.timer.hasTimeElapsed(300000)) {
            this.rpc.setDetails(this.getDetails());
            this.applyState();
            this.applyImageMode();
            DiscordIPC.setActivity(this.rpc);
            this.timer.reset();
        }
        if (this.imageMode.getValue().equals("Animated") && this.frameTimer.hasTimeElapsed(this.animatedDelay.getValue().longValue())) {
            this.frameIndex = (this.frameIndex + 1) % 32;
            this.rpc.setLargeImage(this.frameKey(), "LightningClient");
            DiscordIPC.setActivity(this.rpc);
            this.frameTimer.reset();
        }
    }

    private String getDetails() {
        return this.DETAILS[(int)MathUtils.random(this.DETAILS.length, 0.0)];
    }
}

