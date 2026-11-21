/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.settings.impl;

import java.util.Arrays;
import java.util.List;
import lightning.Night;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lombok.Generated;

public class ModeSetting
extends Setting {
    private String value;
    private final String defaultValue;
    private final List<String> modes;

    public ModeSetting(String string, String string2, String string3, String[] stringArray) {
        super(string, string, string2, new Setting.Visibility());
        this.value = string3;
        this.defaultValue = string3;
        this.modes = Arrays.asList(stringArray);
    }

    public ModeSetting(String string, String string2, String string3, String string4, String[] stringArray) {
        super(string, string2, string3, new Setting.Visibility());
        this.value = string4;
        this.defaultValue = string4;
        this.modes = Arrays.asList(stringArray);
    }

    public ModeSetting(String string, String string2, Setting.Visibility visibility, String string3, String[] stringArray) {
        super(string, string, string2, visibility);
        this.value = string3;
        this.defaultValue = string3;
        this.modes = Arrays.asList(stringArray);
    }

    public ModeSetting(String string, String string2, String string3, Setting.Visibility visibility, String string4, String[] stringArray) {
        super(string, string2, string3, visibility);
        this.value = string4;
        this.defaultValue = string4;
        this.modes = Arrays.asList(stringArray);
    }

    public void setValue(String string) {
        if (!this.modes.contains(string)) {
            return;
        }
        this.value = string;
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    @Generated
    public String getValue() {
        return this.value;
    }

    @Generated
    public String getDefaultValue() {
        return this.defaultValue;
    }

    @Generated
    public List<String> getModes() {
        return this.modes;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final ModeSetting value;
        private final List<String> targetValues;

        public Visibility(ModeSetting modeSetting, String ... stringArray) {
            super(modeSetting);
            this.value = modeSetting;
            this.targetValues = Arrays.asList(stringArray);
        }

        @Override
        public void update() {
            if (this.value.getVisibility() != null) {
                this.value.getVisibility().update();
                if (!this.value.getVisibility().isVisible()) {
                    this.setVisible(false);
                    return;
                }
            }
            boolean bl = false;
            for (String string : this.targetValues) {
                if (!this.value.getValue().equals(string)) continue;
                bl = true;
                break;
            }
            this.setVisible(bl);
        }
    }
}

