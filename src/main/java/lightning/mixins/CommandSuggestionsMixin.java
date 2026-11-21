/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.suggestion.Suggestions
 *  lightning.Night
 *  net.minecraft.client.gui.components.CommandSuggestions
 *  net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.multiplayer.ClientSuggestionProvider
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import lightning.Night;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CommandSuggestions.class})
public abstract class CommandSuggestionsMixin {
    @Shadow
    @Nullable
    private ParseResults<ClientSuggestionProvider> currentParse;
    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;
    @Shadow
    private boolean keepSuggestions;
    @Shadow
    @Final
    private EditBox input;

    @Shadow
    private void updateUsageInfo(ParseResults<ClientSuggestionProvider> parseResults, Suggestions suggestions) {
    }

    @Inject(method={"updateCommandInfo"}, at={@At(value="RETURN")})
    private void night$updateCommandInfo(CallbackInfo callbackInfo) {
        try {
            ParseResults parseResults;
            String string = this.input.getValue();
            String string2 = Night.COMMAND_MANAGER.getPrefix();
            if (!string.startsWith(string2)) {
                return;
            }
            StringReader stringReader = new StringReader(string);
            stringReader.setCursor(string2.length());
            CommandDispatcher commandDispatcher = Night.COMMAND_MANAGER.getDispatcher();
            this.currentParse = parseResults = commandDispatcher.parse(stringReader, (Object)Night.COMMAND_MANAGER);
            if (this.suggestions == null || !this.keepSuggestions) {
                CompletableFuture completableFuture;
                this.pendingSuggestions = completableFuture = commandDispatcher.getCompletionSuggestions(parseResults, this.input.getCursorPosition());
                completableFuture.thenRun(() -> {
                    if (completableFuture.isDone()) {
                        this.updateUsageInfo(this.currentParse, (Suggestions)completableFuture.join());
                    }
                });
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

