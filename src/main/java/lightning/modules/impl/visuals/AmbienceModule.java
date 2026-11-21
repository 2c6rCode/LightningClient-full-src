/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IAmbienceModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name="Ambience", description="Modifies the world ambient lighting and atmosphere color.", category=Module.Category.VISUALS)
public class AmbienceModule
extends Module
implements IAmbienceModule {
    public ColorSetting color = new ColorSetting("Color", "The ambience color applied to the world.", new ColorSetting.Color(new Color(255, 128, 128, 255), true, false));
    public BooleanSetting useSaturation = new BooleanSetting("UseSaturation", "Overrides the saturation of the ambience color.", false);
    public NumberSetting saturation = new NumberSetting("Saturation", "The custom saturation level.", new BooleanSetting.Visibility(this.useSaturation, true), (Number)Float.valueOf(0.5f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(1.0f));
    public BooleanSetting lightmap = new BooleanSetting("Lightmap", "Modifies the world lightmap lighting.", true);
    public BooleanSetting sky = new BooleanSetting("Sky", "Tints sky lighting with ambience.", true);
    public BooleanSetting blocks = new BooleanSetting("Blocks", "Tints block lighting with ambience.", true);

    @Override
    public Color getColor() {
        Color color = this.color.getColor();
        if (this.useSaturation.getValue()) {
            float[] fArray = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            Color color2 = Color.getHSBColor(fArray[0], this.saturation.getValue().floatValue(), fArray[2]);
            return new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), color.getAlpha());
        }
        return color;
    }

    public boolean useSaturation() {
        return this.useSaturation.getValue();
    }

    public float getSaturation() {
        return this.saturation.getValue().floatValue();
    }

    @Override
    public boolean isLightmapEnabled() {
        return this.lightmap.getValue();
    }

    @Override
    public boolean isSkyEnabled() {
        return this.sky.getValue();
    }

    @Override
    public boolean isBlocksEnabled() {
        return this.blocks.getValue();
    }
}

