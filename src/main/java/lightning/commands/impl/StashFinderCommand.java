/*
 * Decompiled with CFR 0.152.
 */
package lightning.commands.impl;

import java.util.List;
import java.util.Map;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.impl.visuals.StashFinderModule;
import lightning.modules.impl.visuals.stashfinder.StashWebhook;
import lightning.utils.chat.ChatUtils;

@RegisterCommand(name="stashfinder", aliases={"stash"}, tag="StashFinder", description="Configures StashFinder's Discord webhook and controls scan state.", syntax="<webhook <url> | userid <id> | test | reset>")
public class StashFinderCommand
extends Command {
    @Override
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            return List.of("webhook", "userid", "test", "reset");
        }
        return List.of();
    }

    @Override
    public void execute(String[] stringArray) {
        if (stringArray.length == 0) {
            this.messageSyntax();
            return;
        }
        switch (stringArray[0].toLowerCase()) {
            case "webhook": {
                if (stringArray.length < 2) {
                    Night.CHAT_MANAGER.tagged("Current webhook: " + String.valueOf(ChatUtils.getPrimary()) + (StashWebhook.getWebhookUrl().isBlank() ? "None" : StashWebhook.getWebhookUrl()), this.getTag());
                    return;
                }
                String string = stringArray[1];
                StashWebhook.setWebhookUrl(string);
                StashFinderModule stashFinderModule = Night.MODULE_MANAGER.getModule(StashFinderModule.class);
                if (stashFinderModule != null) {
                    stashFinderModule.webhookUrl.setValue(string);
                }
                Night.CHAT_MANAGER.tagged("Webhook URL updated.", this.getTag());
                break;
            }
            case "userid": {
                if (stringArray.length < 2) {
                    Night.CHAT_MANAGER.tagged("Current User ID: " + String.valueOf(ChatUtils.getPrimary()) + (StashWebhook.getUserId().isBlank() ? "None" : StashWebhook.getUserId()), this.getTag());
                    return;
                }
                String string = stringArray[1];
                StashWebhook.setUserId(string);
                StashFinderModule stashFinderModule = Night.MODULE_MANAGER.getModule(StashFinderModule.class);
                if (stashFinderModule != null) {
                    stashFinderModule.userId.setValue(string);
                }
                Night.CHAT_MANAGER.tagged("User ID updated (will be pinged on stash finds).", this.getTag());
                break;
            }
            case "test": {
                StashFinderModule stashFinderModule = Night.MODULE_MANAGER.getModule(StashFinderModule.class);
                String string = stashFinderModule != null && !stashFinderModule.webhookUrl.getValue().isBlank() ? stashFinderModule.webhookUrl.getValue() : StashWebhook.getWebhookUrl();
                String string2 = stashFinderModule != null && !stashFinderModule.userId.getValue().isBlank() ? stashFinderModule.userId.getValue() : StashWebhook.getUserId();
                String string3 = string2;
                if (string.isBlank()) {
                    Night.CHAT_MANAGER.tagged("No webhook URL configured. Use " + String.valueOf(ChatUtils.getPrimary()) + ".stashfinder webhook <url>" + String.valueOf(ChatUtils.getSecondary()) + " first.", this.getTag());
                    break;
                }
                StashWebhook.send(0, 0, "Test Dimension", Map.of("Chests", 25, "Shulkers", 8), string, string2);
                Night.CHAT_MANAGER.tagged("Test webhook dispatched.", this.getTag());
                break;
            }
            case "reset": {
                StashFinderModule stashFinderModule = Night.MODULE_MANAGER.getModule(StashFinderModule.class);
                if (stashFinderModule == null) break;
                stashFinderModule.resetCurrentWorldStore();
                Night.CHAT_MANAGER.tagged("Scan history cleared for this world. All chunks will be re-evaluated.", this.getTag());
                break;
            }
            default: {
                this.messageSyntax();
            }
        }
    }
}

