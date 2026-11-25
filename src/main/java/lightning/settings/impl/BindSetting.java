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

    public BindSetting(String name, String description, int defaultValue) {
        super(name, name, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public BindSetting(String name, String tag, String description, int defaultValue) {
        super(name, tag, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public BindSetting(String name, String description, Setting.Visibility visibility, int defaultValue) {
        super(name, name, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public BindSetting(String name, String tag, String description, Setting.Visibility visibility, int defaultValue) {
        super(name, tag, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public void resetValue() {
        this.value = this.defaultValue;
        this.mode = "Bind";
    }

    public void setValue(int keyCode) {
        this.value = keyCode;
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void cycleMode() {
        if (!this.holdModesSupported) {
            return;
        }
        int nextIndex = (Arrays.asList(MODES).indexOf(this.mode) + 1) % MODES.length;
        this.mode = MODES[nextIndex];
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void setMode(String mode) {
        if (!this.holdModesSupported && !mode.equals("Bind")) {
            return;
        }
        if (!Arrays.asList(MODES).contains(mode)) {
            return;
        }
        this.mode = mode;
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
    public void setHoldModesSupported(boolean holdModesSupported) {
        this.holdModesSupported = holdModesSupported;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final BindSetting value;
        private final int targetValue;

        public Visibility(BindSetting bindSetting, int targetValue) {
            super(bindSetting);
            this.value = bindSetting;
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
            this.setVisible(this.value.getValue() == this.targetValue);
        }
    }
}

