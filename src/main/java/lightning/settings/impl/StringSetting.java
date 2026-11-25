/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.settings.impl;

import lightning.Night;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lombok.Generated;

public class StringSetting
extends Setting {
    private String value;
    private final String defaultValue;

    public StringSetting(String name, String description, String defaultValue) {
        super(name, name, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public StringSetting(String name, String tag, String description, String defaultValue) {
        super(name, tag, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public StringSetting(String name, String description, Setting.Visibility visibility, String defaultValue) {
        super(name, name, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public StringSetting(String name, String tag, String description, Setting.Visibility visibility, String defaultValue) {
        super(name, tag, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public void setValue(String value) {
        this.value = value;
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    @Generated
    public String getValue() {
        return this.value;
    }

    @Generated
    public String getDefaultValue() {
        return this.defaultValue;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final StringSetting value;
        private final String targetValue;

        public Visibility(StringSetting stringSetting, String targetValue) {
            super(stringSetting);
            this.value = stringSetting;
            this.targetValue = targetValue;
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
            this.setVisible(this.value.getValue().equals(this.targetValue));
        }
    }
}

