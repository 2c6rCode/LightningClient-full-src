/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.chat;

import lightning.Night;
import lightning.modules.impl.core.CommandsModule;
import lightning.utils.text.FormattingUtils;

public class ChatUtils {
    public static Object getPrimary() {
        return FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).primaryMessageColor.getValue());
    }

    public static Object getSecondary() {
        return FormattingUtils.getFormatting(Night.MODULE_MANAGER.getModule(CommandsModule.class).secondaryMessageColor.getValue());
    }
}

