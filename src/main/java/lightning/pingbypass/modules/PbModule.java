/*
 * Decompiled with CFR 0.152.
 */
package lightning.pingbypass.modules;

import java.util.List;
import lightning.settings.Setting;

public abstract class PbModule {
    private final String name;
    private boolean toggled = false;

    protected PbModule(String string) {
        this.name = string;
    }

    public String getName() {
        return this.name;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void setToggled(boolean bl) {
        this.toggled = bl;
        if (bl) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public abstract void tick();

    public abstract List<Setting> getSettings();

    public Setting getSetting(String string) {
        return this.getSettings().stream().filter(setting -> setting.getName().equalsIgnoreCase(string)).findFirst().orElse(null);
    }
}

