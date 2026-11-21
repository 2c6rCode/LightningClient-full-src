/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events.impl;

import lightning.events.Event;
import lightning.settings.Setting;
import lombok.Generated;

public class SettingChangeEvent
extends Event {
    private final Setting setting;

    @Generated
    public Setting getSetting() {
        return this.setting;
    }

    @Generated
    public SettingChangeEvent(Setting setting) {
        this.setting = setting;
    }
}

