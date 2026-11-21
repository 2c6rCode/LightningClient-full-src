/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.core;

import java.awt.Color;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;

@RegisterModule(name="Renders", description="Manages the client world renders.", category=Module.Category.CORE, persistent=true, drawn=false)
public class RendersModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the place render.", "Fade", new String[]{"Fade", "Shrink"});
    public NumberSetting duration = new NumberSetting("Duration", "The duration for the place render.", 300, 0, 1000);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "The rendering that will be applied to the blocks highlighted.", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.renderMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.renderMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());

    public Color getColor(String string, Color color, float f) {
        if (string.equalsIgnoreCase("Fade")) {
            return ColorUtils.getColor(color, (int)((float)color.getAlpha() * f));
        }
        return color;
    }
}

