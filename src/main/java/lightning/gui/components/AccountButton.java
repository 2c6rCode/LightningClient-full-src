/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package lightning.gui.components;

import lightning.gui.screens.account.AccountManagerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AccountButton {
    public static Button create(int n, int n2, int n3, int n4, Screen screen) {
        return Button.builder((Component)Component.literal((String)"Accounts"), button -> {
            if (Minecraft.getInstance() != null) {
                Minecraft.getInstance().gui.setScreen((Screen)new AccountManagerScreen(screen));
            }
        }).bounds(n, n2, n3, n4).build();
    }
}

