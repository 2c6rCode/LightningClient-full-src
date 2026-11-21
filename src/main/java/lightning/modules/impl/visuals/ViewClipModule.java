/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.visuals;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IViewClipModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name="ViewClip", description="Makes your camera clip through walls and allows you to change the camera's distance from yourself.", category=Module.Category.VISUALS)
public class ViewClipModule
extends Module
implements IViewClipModule {
    public BooleanSetting extend = new BooleanSetting("Extend", "Changes the distance of the third person camera from yourself.", false);
    public NumberSetting distance = new NumberSetting("Distance", "The distance of the third person camera from your character.", new BooleanSetting.Visibility(this.extend, true), (Number)Float.valueOf(4.0f), (Number)Float.valueOf(-50.0f), (Number)Float.valueOf(50.0f));

    @Override
    public String getMetaData() {
        return this.extend.getValue() ? String.valueOf(this.distance.getValue().floatValue()) : "Vanilla";
    }

    @Override
    public boolean isExtend() {
        return this.extend.getValue();
    }

    @Override
    public float getDistanceValue() {
        return this.distance.getValue().floatValue();
    }
}

