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

public class ImageSetting
extends Setting {
    private String value;
    private final String defaultValue;

    public ImageSetting(String string, String string2, String string3) {
        super(string, string, string2, new Setting.Visibility());
        this.value = string3;
        this.defaultValue = string3;
    }

    public ImageSetting(String string, String string2, String string3, String string4) {
        super(string, string2, string3, new Setting.Visibility());
        this.value = string4;
        this.defaultValue = string4;
    }

    public ImageSetting(String string, String string2, Setting.Visibility visibility, String string3) {
        super(string, string, string2, visibility);
        this.value = string3;
        this.defaultValue = string3;
    }

    public ImageSetting(String string, String string2, String string3, Setting.Visibility visibility, String string4) {
        super(string, string2, string3, visibility);
        this.value = string4;
        this.defaultValue = string4;
    }

    public void resetValue() {
        this.setValue(this.defaultValue);
    }

    public void setValue(String string) {
        this.value = string;
        if (Night.IMAGE_MANAGER != null) {
            Night.IMAGE_MANAGER.setCurrentActiveImage(string);
        }
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
}

