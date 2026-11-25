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

    public ModeSetting(String name, String description, String defaultValue, String[] modes) {
        super(name, name, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.modes = Arrays.asList(modes);
    }

    public ModeSetting(String name, String tag, String description, String defaultValue, String[] modes) {
        super(name, tag, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.modes = Arrays.asList(modes);
    }

    public ModeSetting(String name, String description, Setting.Visibility visibility, String defaultValue, String[] modes) {
        super(name, name, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.modes = Arrays.asList(modes);
    }

    public ModeSetting(String name, String tag, String description, Setting.Visibility visibility, String defaultValue, String[] modes) {
        super(name, tag, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.modes = Arrays.asList(modes);
    }

    public void setValue(String mode) {
        if (!this.modes.contains(mode)) {
            return;
        }
        this.value = mode;
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

        public Visibility(ModeSetting modeSetting, String ... targetValues) {
            super(modeSetting);
            this.value = modeSetting;
            this.targetValues = Arrays.asList(targetValues);
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
            boolean matches = false;
            for (String targetValue : this.targetValues) {
                if (!this.value.getValue().equals(targetValue)) continue;
                matches = true;
                break;
            }
            this.setVisible(matches);
        }
    }
}

