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

    public ImageSetting(String name, String description, String defaultValue) {
        super(name, name, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ImageSetting(String name, String tag, String description, String defaultValue) {
        super(name, tag, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ImageSetting(String name, String description, Setting.Visibility visibility, String defaultValue) {
        super(name, name, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ImageSetting(String name, String tag, String description, Setting.Visibility visibility, String defaultValue) {
        super(name, tag, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public void resetValue() {
        this.setValue(this.defaultValue);
    }

    public void setValue(String imageName) {
        this.value = imageName;
        if (Night.IMAGE_MANAGER != null) {
            Night.IMAGE_MANAGER.setCurrentActiveImage(imageName);
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

