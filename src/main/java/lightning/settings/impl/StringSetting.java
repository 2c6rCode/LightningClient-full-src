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

    public StringSetting(String string, String string2, String string3) {
        super(string, string, string2, new Setting.Visibility());
        this.value = string3;
        this.defaultValue = string3;
    }

    public StringSetting(String string, String string2, String string3, String string4) {
        super(string, string2, string3, new Setting.Visibility());
        this.value = string4;
        this.defaultValue = string4;
    }

    public StringSetting(String string, String string2, Setting.Visibility visibility, String string3) {
        super(string, string, string2, visibility);
        this.value = string3;
        this.defaultValue = string3;
    }

    public StringSetting(String string, String string2, String string3, Setting.Visibility visibility, String string4) {
        super(string, string2, string3, visibility);
        this.value = string4;
        this.defaultValue = string4;
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public void setValue(String string) {
        this.value = string;
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

        public Visibility(StringSetting stringSetting, String string) {
            super(stringSetting);
            this.value = stringSetting;
            this.targetValue = string;
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

