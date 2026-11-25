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
        for (String macroScript : this.macros.keySet()) {
            int keyCode = this.macros.get(macroScript);
            if (keyInputEvent.getKey() != keyCode) continue;
            if (macroScript == null) {
                Night.CHAT_MANAGER.error("An error happened while executing the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(keyCode) + String.valueOf(ChatUtils.getSecondary()) + " macro.");
                continue;
            }
            for (String commandText : macroScript.split(";")) {
                if (commandText.startsWith("/")) {
                    MacroManager.mc.player.connection.sendCommand(commandText.substring(1));
                    continue;
                }
                if (commandText.startsWith(Night.COMMAND_MANAGER.getPrefix())) {
                    Night.COMMAND_MANAGER.execute(commandText);
                    continue;
                }
                MacroManager.mc.player.connection.sendChat(commandText);
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent mouseInputEvent) {
        if (MacroManager.mc.player == null || MacroManager.mc.level == null || MacroManager.mc.gui != null && MacroManager.mc.gui.screen() != null) {
            return;
        }
        for (String macroText : this.macros.keySet()) {
            int keyCode = this.macros.get(macroText);
            if (keyCode != -mouseInputEvent.getButton() - 1) continue;
            if (macroText == null) {
                Night.CHAT_MANAGER.error("An error happened while executing the " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(keyCode) + String.valueOf(ChatUtils.getSecondary()) + " macro.");
                continue;
            }
            if (macroText.startsWith("/")) {
                MacroManager.mc.player.connection.sendCommand(macroText.substring(1));
                continue;
            }
            MacroManager.mc.player.connection.sendChat(macroText);
        }
    }

    public String getKey(int keyCode) {
        for (String macroText : this.macros.keySet()) {
            if (keyCode != this.macros.get(macroText)) continue;
            return macroText;
        }
        return null;
    }

    public int getValue(String macroText) {
        return this.macros.get(macroText);
    }

    public boolean containsKey(String macroText) {
        return this.macros.containsKey(macroText);
    }

    public boolean containsValue(int keyCode) {
        return this.macros.containsValue(keyCode);
    }

    public void add(String macroText, int keyCode) {
        this.macros.put(macroText, keyCode);
    }

    public void remove(String macroText) {
        this.macros.remove(macroText);
    }

    public void clear() {
        this.macros.clear();
    }

    @Generated
    public HashMap<String, Integer> getMacros() {
        return this.macros;
    }
}

