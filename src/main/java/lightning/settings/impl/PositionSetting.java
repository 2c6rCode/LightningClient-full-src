/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.settings.impl;

import lightning.settings.Setting;
import lombok.Generated;

public class PositionSetting
extends Setting {
    private float x = 0.0f;
    private float y = 0.0f;

    public PositionSetting(String string, String string2) {
        super(string, string, string2, new Setting.Visibility());
    }

    public void set(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    public void resetValue() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public void setX(float f) {
        this.x = f;
    }

    @Generated
    public void setY(float f) {
        this.y = f;
    }
}

