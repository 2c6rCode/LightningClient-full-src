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

    public BooleanSetting(String string, String string2, boolean bl) {
        super(string, string, string2, new Setting.Visibility());
        this.value = bl;
        this.defaultValue = bl;
        this.openAnim = BooleanSetting.newOpenAnim(bl);
    }

    public BooleanSetting(String string, String string2, String string3, boolean bl) {
        super(string, string2, string3, new Setting.Visibility());
        this.value = bl;
        this.defaultValue = bl;
        this.openAnim = BooleanSetting.newOpenAnim(bl);
    }

    public BooleanSetting(String string, String string2, Setting.Visibility visibility, boolean bl) {
        super(string, string, string2, visibility);
        this.value = bl;
        this.defaultValue = bl;
        this.openAnim = BooleanSetting.newOpenAnim(bl);
    }

    public BooleanSetting(String string, String string2, String string3, Setting.Visibility visibility, boolean bl) {
        super(string, string2, string3, visibility);
        this.value = bl;
        this.defaultValue = bl;
        this.openAnim = BooleanSetting.newOpenAnim(bl);
    }

    private static Animation newOpenAnim(boolean bl) {
        float f = bl ? 1.0f : 0.0f;
        return new Animation(f, f, 180, Easing.Method.EASE_OUT_QUAD);
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean bl) {
        this.value = bl;
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

        public Visibility(BooleanSetting booleanSetting, boolean bl) {
            super(booleanSetting);
            this.value = booleanSetting;
            this.targetValue = bl;
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
            float f = this.value.getOpenAmount();
            return this.targetValue ? f : 1.0f - f;
        }
    }
}

