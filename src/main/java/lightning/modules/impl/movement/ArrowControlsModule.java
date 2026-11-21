/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  lightning.mixins.accessors.CreativeInventoryScreenAccessor
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.inventory.AnvilScreen
 *  net.minecraft.client.gui.screens.inventory.BookEditScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen
 *  net.minecraft.client.gui.screens.inventory.SignEditScreen
 *  net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.CreativeModeTab$Type
 */
package lightning.modules.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.mixins.accessors.CreativeInventoryScreenAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;

@RegisterModule(name="ArrowControls", description="Allows you to control the camera using your arrow keys.", category=Module.Category.MOVEMENT)
public class ArrowControlsModule
extends Module {
    public NumberSetting speed = new NumberSetting("Speed", "The speed at which the camera will be moving at.", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f));
    public BooleanSetting inventory = new BooleanSetting("Inventory", "Allows you to control the camera when a screen is opened.", true);
    private long lastTime;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (ArrowControlsModule.mc.player == null || ArrowControlsModule.mc.level == null) {
            return;
        }
        if (ArrowControlsModule.mc.gui.screen() != null && (!this.inventory.getValue() || ArrowControlsModule.mc.gui.screen() instanceof ChatScreen || ArrowControlsModule.mc.gui.screen() instanceof BookEditScreen || ArrowControlsModule.mc.gui.screen() instanceof SignEditScreen || ArrowControlsModule.mc.gui.screen() instanceof JigsawBlockEditScreen || ArrowControlsModule.mc.gui.screen() instanceof StructureBlockEditScreen || ArrowControlsModule.mc.gui.screen() instanceof AnvilScreen || ArrowControlsModule.mc.gui.screen() instanceof CreativeModeInventoryScreen && CreativeInventoryScreenAccessor.getSelectedTab().getType() == CreativeModeTab.Type.SEARCH)) {
            return;
        }
        float f = 0.0f;
        float f2 = 0.0f;
        float f3 = (float)(System.currentTimeMillis() - this.lastTime) / 10.0f * this.speed.getValue().floatValue();
        this.lastTime = System.currentTimeMillis();
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)263)) {
            f -= f3;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)262)) {
            f += f3;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)265)) {
            f2 -= f3;
        }
        if (InputConstants.isKeyDown((Window)mc.getWindow(), (int)264)) {
            f2 += f3;
        }
        ArrowControlsModule.mc.player.setYRot(ArrowControlsModule.mc.player.getYRot() + f);
        ArrowControlsModule.mc.player.setXRot(Mth.clamp((float)(ArrowControlsModule.mc.player.getXRot() + f2), (float)-90.0f, (float)90.0f));
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.speed.getValue().floatValue());
    }
}

