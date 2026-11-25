/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.miscellaneous;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.FileUtils;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;

@RegisterModule(name="Spammer", description="Spams messages in chat from a text file.", category=Module.Category.MISCELLANEOUS)
public class SpammerModule
extends Module {
    public StringSetting fileName = new StringSetting("FileName", "The name of the spammer text file.", "spammer.txt");
    public NumberSetting delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
    public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
    public BooleanSetting shuffled = new BooleanSetting("Shuffled", "Sends the spammer messages out of order.", false);
    private final Timer timer = new Timer();
    private List<String> messages = new ArrayList<String>();
    private int line;

    @Override
    public void onEnable() {
        this.line = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (this.getNull()) {
            return;
        }
        File file = new File("Lightning/Client/" + this.fileName.getValue());
        this.messages = FileUtils.readLines(file);
        if (!this.messages.isEmpty() && this.timer.hasTimeElapsed(this.delay.getValue().intValue() * 1000)) {
            if (this.line >= this.messages.size()) {
                this.line = 0;
            }
            String message = this.shuffled.getValue() ? this.messages.get((int)MathUtils.random(this.messages.size(), 0.0)) : this.messages.get(this.line);
            SpammerModule.mc.player.connection.sendChat((this.greenText.getValue() ? "> " : "") + message);
            ++this.line;
            this.timer.reset();
        }
    }
}

