/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 */
package lightning.modules.impl.core;

import java.awt.Color;
import java.util.Arrays;
import lightning.Night;
import lightning.gui.HeaderFx;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.EspShader;
import net.minecraft.client.gui.screens.Screen;

@RegisterModule(name="ClickGui", description="Allows you to change and interact with the client's modules and settings through a GUI.", category=Module.Category.CORE, drawn=false, bind=344)
public class ClickGuiModule
extends Module {
    public BooleanSetting sounds = new BooleanSetting("Sounds", "Plays Minecraft UI sounds when interacting with the client's GUI.", true);
    public BooleanSetting blur = new BooleanSetting("Blur", "Whether or not to blur the background behind the GUI.", true);
    public NumberSetting scrollSpeed = new NumberSetting("ScrollSpeed", "The speed at which the scrolling of the frames will be at.", 1, 1, 10);
    public NumberSetting scale = new NumberSetting("Scale", "Scale", "The overall size of the ClickGUI. Applies next time you open the GUI.", new Setting.Visibility(), Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(2.0f), Float.valueOf(0.1f));
    public ColorSetting color = new ColorSetting("Color", "The color that will be used in the GUI.", new ColorSetting.Color(new Color(160, 120, 255), false, false));
    private static final String[] FILL_MODES = ClickGuiModule.buildFillModes();
    public ModeSetting fillMode = new ModeSetting("FillMode", "How a toggled module's row background fills.", "Default", FILL_MODES);
    private static final String[] SHADER_FILL_MODES = Arrays.copyOfRange(FILL_MODES, 1, FILL_MODES.length);
    public NumberSetting neekeriSpeed = new NumberSetting("NeekeriSpeed", "Speed", "The speed at which the fill animates.", new ModeSetting.Visibility(this.fillMode, SHADER_FILL_MODES), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f));
    public NumberSetting neekeriOpacity = new NumberSetting("NeekeriOpacity", "Opacity", "The opacity of the fill.", new ModeSetting.Visibility(this.fillMode, SHADER_FILL_MODES), 90, 0, 100);
    public static final String[] BG_MODES = new String[]{"Aurora", "CyberGrid", "Constellations", "MeshGlow", "DarkFrost", "None"};
    public ModeSetting background = new ModeSetting("Background", "Shader effect rendered behind the GUI.", "Aurora", BG_MODES);
    private static final String[] ACTIVE_BG_MODES = new String[]{"Aurora", "CyberGrid", "Constellations", "MeshGlow", "DarkFrost"};
    public NumberSetting bgSpeed = new NumberSetting("BgSpeed", "Speed", "The speed at which the background shader animates.", new ModeSetting.Visibility(this.background, ACTIVE_BG_MODES), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f));
    public NumberSetting bgOpacity = new NumberSetting("BgOpacity", "Opacity", "The opacity of the background shader.", new ModeSetting.Visibility(this.background, ACTIVE_BG_MODES), 65, 0, 100);
    public BooleanSetting glowAccent = new BooleanSetting("GlowAccent", "Renders glowing accent lines and header highlights.", true);
    public BooleanSetting categoryIcons = new BooleanSetting("CategoryIcons", "Displays modern icons in frame headers.", true);
    public BooleanSetting triangles = new BooleanSetting("Triangles", "Displays triangle collapse/expand indicators for modules and categories.", false);
    public BooleanSetting switches = new BooleanSetting("Switches", "Displays toggle switch buttons for modules and settings.", true);

    public boolean isRainbow() {
        return this.color.isRainbow();
    }

    private static String[] buildFillModes() {
        String[] stringArray = EspShader.MODES;
        String[] stringArray2 = new String[stringArray.length];
        stringArray2[0] = "Default";
        System.arraycopy(stringArray, 1, stringArray2, 1, stringArray.length - 1);
        return stringArray2;
    }

    @Override
    public void onEnable() {
        if (ClickGuiModule.mc.player == null) {
            this.setToggled(false);
            return;
        }
        HeaderFx.applyScale(Night.CLICK_GUI, this.scale.getValue().floatValue());
        Night.CLICK_GUI.cancelClose();
        ClickGuiModule.mc.gui.setScreen((Screen)Night.CLICK_GUI);
    }

    @Override
    public void onDisable() {
        if (ClickGuiModule.mc.gui.screen() == Night.CLICK_GUI) {
            Night.CLICK_GUI.requestClose();
        } else {
            ClickGuiModule.mc.gui.setScreen(null);
        }
    }
}

