/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.settings.impl;

import lightning.Night;
import lightning.events.impl.SettingChangeEvent;
import lightning.modules.impl.core.ColorModule;
import lightning.settings.Setting;
import lightning.utils.color.ColorUtils;
import lombok.Generated;

public class ColorSetting
extends Setting {
    private Color value;
    private final Color defaultValue;

    public ColorSetting(String name, String description, Color defaultValue) {
        super(name, name, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = new Color(defaultValue.getColor(), defaultValue.isSync(), defaultValue.isRainbow());
    }

    public ColorSetting(String name, String tag, String description, Color defaultValue) {
        super(name, tag, description, new Setting.Visibility());
        this.value = defaultValue;
        this.defaultValue = new Color(defaultValue.getColor(), defaultValue.isSync(), defaultValue.isRainbow());
    }

    public ColorSetting(String name, String description, Setting.Visibility visibility, Color defaultValue) {
        super(name, name, description, visibility);
        this.value = defaultValue;
        this.defaultValue = new Color(defaultValue.getColor(), defaultValue.isSync(), defaultValue.isRainbow());
    }

    public ColorSetting(String name, String tag, String description, Setting.Visibility visibility, Color defaultValue) {
        super(name, tag, description, visibility);
        this.value = defaultValue;
        this.defaultValue = new Color(defaultValue.getColor(), defaultValue.isSync(), defaultValue.isRainbow());
    }

    public java.awt.Color getColor() {
        return this.getColor(0L);
    }

    public java.awt.Color getColor(long offset) {
        if (this.isSync()) {
            return ColorUtils.getGlobalColor(this.getAlpha(), offset);
        }
        if (this.isRainbow()) {
            ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
            long speed = colorModule != null ? colorModule.rainbowSpeed.getValue().longValue() : 6L;
            float saturation = colorModule != null ? colorModule.rainbowSaturation.getValue().floatValue() / 100.0f : 1.0f;
            float brightness = colorModule != null ? colorModule.rainbowBrightness.getValue().floatValue() / 100.0f : 1.0f;
            float scale = colorModule != null && colorModule.rainbowScale != null ? colorModule.rainbowScale.getValue().floatValue() / 100.0f : 1.0f;
            return ColorUtils.getRainbow(speed, saturation, brightness, this.getAlpha(), (long)((float)offset * scale));
        }
        if (Night.MODULE_MANAGER != null && this == Night.MODULE_MANAGER.getModule(ColorModule.class).color) {
            return ColorUtils.getColor(this.value.getColor(), 255);
        }
        return this.value.getColor();
    }

    public void setValue(Color color) {
        this.value = color;
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public void setColor(java.awt.Color color) {
        this.value.setColor(color);
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public int getAlpha() {
        return this.getValue().getColor().getAlpha();
    }

    public boolean isSync() {
        return this.value.isSync();
    }

    public void setSync(boolean sync) {
        this.value.setSync(sync);
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public boolean isRainbow() {
        return this.value.isRainbow();
    }

    public void setRainbow(boolean rainbow) {
        this.value.setRainbow(rainbow);
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public void resetValue() {
        this.value = new Color(new java.awt.Color(this.defaultValue.getColor().getRGB(), true), this.defaultValue.isSync(), this.defaultValue.isRainbow());
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    @Generated
    public Color getValue() {
        return this.value;
    }

    @Generated
    public Color getDefaultValue() {
        return this.defaultValue;
    }

    public static class Color {
        private java.awt.Color color;
        private boolean sync;
        private boolean rainbow;

        @Generated
        public java.awt.Color getColor() {
            return this.color;
        }

        @Generated
        public boolean isSync() {
            return this.sync;
        }

        @Generated
        public boolean isRainbow() {
            return this.rainbow;
        }

        @Generated
        public void setColor(java.awt.Color color) {
            this.color = color;
        }

        @Generated
        public void setSync(boolean sync) {
            this.sync = sync;
        }

        @Generated
        public void setRainbow(boolean rainbow) {
            this.rainbow = rainbow;
        }

        @Generated
        public Color(java.awt.Color color, boolean sync, boolean rainbow) {
            this.color = color;
            this.sync = sync;
            this.rainbow = rainbow;
        }
    }

    public static class Visibility
    extends Setting.Visibility {
        private final ColorSetting value;
        private final boolean targetValue;
        private final Target target;

        public Visibility(ColorSetting colorSetting, boolean targetValue, Target target) {
            super(colorSetting);
            this.value = colorSetting;
            this.targetValue = targetValue;
            this.target = target;
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
            this.setVisible(this.target == Target.RAINBOW ? this.value.isRainbow() == this.targetValue : this.value.isSync() == this.targetValue);
        }

        public static enum Target {
            RAINBOW,
            SYNC;

        }
    }
}

