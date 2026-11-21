/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 */
package lightning.modules.impl.core;

import lightning.gui.HUDEditorScreen;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import net.minecraft.client.gui.screens.Screen;

@RegisterModule(name="HUDEditor", description="Lets you drag-and-drop and toggle individual HUD elements directly on screen.", category=Module.Category.CORE, drawn=false)
public class HUDEditorModule
extends Module {
    @Override
    public void onEnable() {
        HUDEditorModule.mc.gui.setScreen((Screen)new HUDEditorScreen());
    }

    @Override
    public void onDisable() {
        if (HUDEditorModule.mc.gui.screen() instanceof HUDEditorScreen) {
            HUDEditorModule.mc.gui.setScreen(null);
        }
    }
}

