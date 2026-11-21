/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.managers;

import java.util.HashMap;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.utils.IMinecraft;
import lightning.utils.chat.ChatUtils;
import lightning.utils.input.KeyboardUtils;
import lombok.Generated;

public class MacroManager
implements IMinecraft {
    private final HashMap<String, Integer> macros = new HashMap();

    public MacroManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent keyInputEvent) {
        if (MacroManager.mc.player == null || MacroManager.mc.level == null || MacroManager.mc.gui != null && MacroManager.mc.gui.screen() != null) {
            return;
        }
        for (String string : this.macros.keySet()) {
            String[] stringArray;
            int n = this.macros.get(string);
            if (keyInputEvent.getKey() != n) continue;
            if (string == null) {
                Night.CHAT_MANAGER.error("An error happened while executing the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(n) + String.valueOf(ChatUtils.getSecondary()) + " macro.");
                continue;
            }
            for (String string2 : stringArray = string.split(";")) {
                if (string2.startsWith("/")) {
                    MacroManager.mc.player.connection.sendCommand(string2.substring(1));
                    continue;
                }
                if (string2.startsWith(Night.COMMAND_MANAGER.getPrefix())) {
                    Night.COMMAND_MANAGER.execute(string2);
                    continue;
                }
                MacroManager.mc.player.connection.sendChat(string2);
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent mouseInputEvent) {
        if (MacroManager.mc.player == null || MacroManager.mc.level == null || MacroManager.mc.gui != null && MacroManager.mc.gui.screen() != null) {
            return;
        }
        for (String string : this.macros.keySet()) {
            int n = this.macros.get(string);
            if (n != -mouseInputEvent.getButton() - 1) continue;
            if (string == null) {
                Night.CHAT_MANAGER.error("An error happened while executing the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(n) + String.valueOf(ChatUtils.getSecondary()) + " macro.");
                continue;
            }
            if (string.startsWith("/")) {
                MacroManager.mc.player.connection.sendCommand(string.substring(1));
                continue;
            }
            MacroManager.mc.player.connection.sendChat(string);
        }
    }

    public String getKey(int n) {
        for (String string : this.macros.keySet()) {
            if (n != this.macros.get(string)) continue;
            return string;
        }
        return null;
    }

    public int getValue(String string) {
        return this.macros.get(string);
    }

    public boolean containsKey(String string) {
        return this.macros.containsKey(string);
    }

    public boolean containsValue(int n) {
        return this.macros.containsValue(n);
    }

    public void add(String string, int n) {
        this.macros.put(string, n);
    }

    public void remove(String string) {
        this.macros.remove(string);
    }

    public void clear() {
        this.macros.clear();
    }

    @Generated
    public HashMap<String, Integer> getMacros() {
        return this.macros;
    }
}

