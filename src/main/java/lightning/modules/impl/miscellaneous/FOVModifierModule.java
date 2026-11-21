/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.miscellaneous;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFOVModifierModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name="FOVModifier", description="Gives you more customizability for the games FOV.", category=Module.Category.MISCELLANEOUS)
public class FOVModifierModule
extends Module
implements IFOVModifierModule {
    public NumberSetting fov = new NumberSetting("FOV", "The FOV you want to use.", 120, 50, 150);

    @Override
    public String getMetaData() {
        return "" + this.fov.getValue().intValue();
    }

    @Override
    public float getFovValue() {
        return this.fov.getValue().floatValue();
    }
}

