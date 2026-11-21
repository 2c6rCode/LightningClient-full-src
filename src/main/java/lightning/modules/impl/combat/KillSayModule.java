/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.combat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.events.impl.CommandInputEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TargetDeathEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.SuicideModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.FileUtils;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;

@RegisterModule(name="KillSay", description="Automatically sends a message when you kill someone.", category=Module.Category.COMBAT)
public class KillSayModule
extends Module {
    private final String[] KILL_MESSAGES = new String[]{"Sit [username], Lightning owns me and all!", "[username] I killed you with the power of Lightning!", "Sorry [username], get good get Lightning!", "I just killed [username] thanks to Lightning!"};
    public BooleanSetting kills = new BooleanSetting("Kills", "Sends a message when you kill someone.", true);
    public BooleanSetting pops = new BooleanSetting("Pops", "Sends a message when you pop someone.", false);
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the messages.", "Default", new String[]{"Default", "Custom"});
    public StringSetting killFile = new StringSetting("KillFile", "The name of the file containing the kill messages.", "killfile.txt");
    public StringSetting popFile = new StringSetting("PopFile", "The name of the file containing the pop messages.", "popfile.txt");
    public NumberSetting delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 10);
    public BooleanSetting clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
    public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
    public BooleanSetting killStreak = new BooleanSetting("KillStreak", "Keeps track of your kills.", false);
    public BooleanSetting suicideIgnore = new BooleanSetting("SuicideIgnore", "Doesn't reset kill streak when you suicide.", false);
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();
    private List<String> killMessages = new ArrayList<String>();
    private List<String> popMessages = new ArrayList<String>();
    private final Timer timer = new Timer();
    private String lastMessage;
    private int streak;
    private boolean suicide = false;

    @Override
    public void onEnable() {
        this.queue.clear();
        this.timer.reset();
        this.streak = 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        ConcurrentLinkedQueue<String> concurrentLinkedQueue;
        if (this.getNull() || this.queue.isEmpty()) {
            return;
        }
        this.killMessages = FileUtils.readLines(new File("Lightning/Client/" + this.killFile.getValue()));
        this.popMessages = FileUtils.readLines(new File("Lightning/Client/" + this.popFile.getValue()));
        ConcurrentLinkedQueue<String> concurrentLinkedQueue2 = concurrentLinkedQueue = this.queue;
        synchronized (concurrentLinkedQueue2) {
            if (this.timer.hasTimeElapsed(this.delay.getValue().intValue() * 1000)) {
                String string = this.queue.poll();
                if (!string.isEmpty()) {
                    if (this.clientside.getValue()) {
                        Night.CHAT_MANAGER.message(string);
                    } else {
                        KillSayModule.mc.player.connection.sendChat((this.greenText.getValue() ? "> " : "") + string);
                    }
                    this.lastMessage = string;
                }
                this.timer.reset();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTargetDeath(TargetDeathEvent targetDeathEvent) {
        ConcurrentLinkedQueue<String> concurrentLinkedQueue;
        if (this.getNull() || targetDeathEvent.getPlayer() == KillSayModule.mc.player) {
            return;
        }
        ++this.streak;
        ConcurrentLinkedQueue<String> concurrentLinkedQueue2 = concurrentLinkedQueue = this.queue;
        synchronized (concurrentLinkedQueue2) {
            if (this.kills.getValue()) {
                this.queue.clear();
                String string = this.getKillMessage(targetDeathEvent.getPlayer().getName().getString());
                this.queue.add(string);
            }
        }
        if (this.killStreak.getValue() && this.streak > 1) {
            Night.CHAT_MANAGER.message("You are on a " + this.streak + " kill streak.");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent playerPopEvent) {
        ConcurrentLinkedQueue<String> concurrentLinkedQueue;
        if (this.getNull() || playerPopEvent.getPlayer() == KillSayModule.mc.player || !Night.TARGET_MANAGER.isTarget(playerPopEvent.getPlayer()) || !this.pops.getValue()) {
            return;
        }
        ConcurrentLinkedQueue<String> concurrentLinkedQueue2 = concurrentLinkedQueue = this.queue;
        synchronized (concurrentLinkedQueue2) {
            String string = this.getPopMessage(playerPopEvent.getPlayer().getName().getString(), playerPopEvent.getPops());
            this.queue.add(string);
        }
    }

    @SubscribeEvent
    public void onCommandInput(CommandInputEvent commandInputEvent) {
        if (this.getNull()) {
            return;
        }
        if (commandInputEvent.getMessage().contains("/kill")) {
            this.suicide = true;
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent chatInputEvent) {
        if (this.getNull()) {
            return;
        }
        this.timer.reset();
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (!(playerDeathEvent.getPlayer() != KillSayModule.mc.player || this.suicideIgnore.getValue() && (this.suicide || Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()))) {
            this.streak = 0;
        }
        this.suicide = false;
    }

    private String getPopMessage(String string, int n) {
        String string2;
        if (this.mode.getValue().equals("Custom") && !this.popMessages.isEmpty()) {
            while ((string2 = this.popMessages.get((int)MathUtils.random(this.popMessages.size(), 0.0)).replace("[username]", string)).equals(this.lastMessage) && this.popMessages.size() > 1) {
            }
        } else {
            string2 = n + (n > 1 ? " pops " : " pop ") + string + "!";
        }
        return string2;
    }

    private String getKillMessage(String string) {
        String string2;
        if (this.mode.getValue().equals("Custom") && !this.killMessages.isEmpty()) {
            while ((string2 = this.killMessages.get((int)MathUtils.random(this.killMessages.size(), 0.0)).replace("[username]", string)).equals(this.lastMessage) && this.killMessages.size() > 1) {
            }
        } else {
            while ((string2 = this.KILL_MESSAGES[(int)MathUtils.random(this.KILL_MESSAGES.length, 0.0)].replace("[username]", string)).equals(this.lastMessage)) {
            }
        }
        return string2;
    }

    @Override
    public String getMetaData() {
        return "" + this.streak;
    }
}

