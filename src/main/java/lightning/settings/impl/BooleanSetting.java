/*
 * Decompiled with CFR 0.152.
 */
package lightning.settings.impl;

import lightning.Night;
import lightning.events.impl.SettingChangeEvent;
import lightning.settings.Setting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;

public class BooleanSetting
extends Setting {
    private boolean value;
    private final boolean defaultValue;
    private final Animation openAnim;

    public float getOpenAmount() {
        return this.openAnim.get(this.value ? 1.0f : 0.0f);
    }

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, name, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.openAnim = BooleanSetting.newOpenAnim(defaultValue);
    }

    public BooleanSetting(String name, String tag, String description, boolean defaultValue) {
        super(name, tag, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.openAnim = BooleanSetting.newOpenAnim(defaultValue);
    }

    public BooleanSetting(String name, String description, Setting.Visibility visibility, boolean defaultValue) {
        super(name, name, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.openAnim = BooleanSetting.newOpenAnim(defaultValue);
    }

    public BooleanSetting(String name, String tag, String description, Setting.Visibility visibility, boolean defaultValue) {
        super(name, tag, description, visibility);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.openAnim = BooleanSetting.newOpenAnim(defaultValue);
    }

    private static Animation newOpenAnim(boolean initialValue) {
        float initialProgress = initialValue ? 1.0f : 0.0f;
        return new Animation(initialProgress, initialProgress, 180, Easing.Method.EASE_OUT_QUAD);
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public boolean getDefaultValue() {
        return this.defaultValue;
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final BooleanSetting value;
        private final boolean targetValue;

        public Visibility(BooleanSetting booleanSetting, boolean targetValue) {
            super(booleanSetting);
            this.value = booleanSetting;
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
            this.setVisible(this.getOpenAmount() > 0.001f);
        }

        public float getOpenAmount() {
            float openAmount = this.value.getOpenAmount();
            return this.targetValue ? openAmount : 1.0f - openAmount;
        }
    }
}

