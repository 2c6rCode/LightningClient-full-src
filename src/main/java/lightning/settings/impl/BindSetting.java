/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.settings.impl;

import java.util.Arrays;
import lightning.Night;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lombok.Generated;

public class BindSetting
extends Setting {
    private int value;
    private final int defaultValue;
    public static final String[] MODES = new String[]{"Bind", "Hold", "ReverseHold"};
    private String mode = "Bind";
    private boolean holdModesSupported = true;

    public BindSetting disableHoldModes() {
        this.holdModesSupported = false;
        return this;
    }

    public BindSetting(String string, String string2, int n) {
        super(string, string, string2, new Setting.Visibility());
        this.value = n;
        this.defaultValue = n;
    }

    public BindSetting(String string, String string2, String string3, int n) {
        super(string, string2, string3, new Setting.Visibility());
        this.value = n;
        this.defaultValue = n;
    }

    public BindSetting(String string, String string2, Setting.Visibility visibility, int n) {
        super(string, string, string2, visibility);
        this.value = n;
        this.defaultValue = n;
    }

    public BindSetting(String string, String string2, String string3, Setting.Visibility visibility, int n) {
        super(string, string2, string3, visibility);
        this.value = n;
        this.defaultValue = n;
    }

    public void resetValue() {
        this.value = this.defaultValue;
        this.mode = "Bind";
    }

    public void setValue(int n) {
        this.value = n;
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void cycleMode() {
        if (!this.holdModesSupported) {
            return;
        }
        int n = (Arrays.asList(MODES).indexOf(this.mode) + 1) % MODES.length;
        this.mode = MODES[n];
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void setMode(String string) {
        if (!this.holdModesSupported && !string.equals("Bind")) {
            return;
        }
        if (!Arrays.asList(MODES).contains(string)) {
            return;
        }
        this.mode = string;
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    @Generated
    public int getValue() {
        return this.value;
    }

    @Generated
    public int getDefaultValue() {
        return this.defaultValue;
    }

    @Generated
    public String getMode() {
        return this.mode;
    }

    @Generated
    public boolean isHoldModesSupported() {
        return this.holdModesSupported;
    }

    @Generated
    public void setHoldModesSupported(boolean bl) {
        this.holdModesSupported = bl;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final BindSetting value;
        private final int targetValue;

        public Visibility(BindSetting bindSetting, int n) {
            super(bindSetting);
            this.value = bindSetting;
            this.targetValue = n;
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
            this.setVisible(this.value.getValue() == this.targetValue);
        }
    }
}

