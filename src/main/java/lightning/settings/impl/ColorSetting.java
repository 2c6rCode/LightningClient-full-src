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

    public ColorSetting(String string, String string2, Color color) {
        super(string, string, string2, new Setting.Visibility());
        this.value = color;
        this.defaultValue = new Color(color.getColor(), color.isSync(), color.isRainbow());
    }

    public ColorSetting(String string, String string2, String string3, Color color) {
        super(string, string2, string3, new Setting.Visibility());
        this.value = color;
        this.defaultValue = new Color(color.getColor(), color.isSync(), color.isRainbow());
    }

    public ColorSetting(String string, String string2, Setting.Visibility visibility, Color color) {
        super(string, string, string2, visibility);
        this.value = color;
        this.defaultValue = new Color(color.getColor(), color.isSync(), color.isRainbow());
    }

    public ColorSetting(String string, String string2, String string3, Setting.Visibility visibility, Color color) {
        super(string, string2, string3, visibility);
        this.value = color;
        this.defaultValue = new Color(color.getColor(), color.isSync(), color.isRainbow());
    }

    public java.awt.Color getColor() {
        return this.getColor(0L);
    }

    public java.awt.Color getColor(long l) {
        if (this.isSync()) {
            return ColorUtils.getGlobalColor(this.getAlpha(), l);
        }
        if (this.isRainbow()) {
            ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
            long l2 = colorModule != null ? colorModule.rainbowSpeed.getValue().longValue() : 6L;
            float f = colorModule != null ? colorModule.rainbowSaturation.getValue().floatValue() / 100.0f : 1.0f;
            float f2 = colorModule != null ? colorModule.rainbowBrightness.getValue().floatValue() / 100.0f : 1.0f;
            float f3 = colorModule != null && colorModule.rainbowScale != null ? colorModule.rainbowScale.getValue().floatValue() / 100.0f : 1.0f;
            return ColorUtils.getRainbow(l2, f, f2, this.getAlpha(), (long)((float)l * f3));
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

    public void setSync(boolean bl) {
        this.value.setSync(bl);
        if (Night.EVENT_HANDLER != null) {
            Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
        }
    }

    public boolean isRainbow() {
        return this.value.isRainbow();
    }

    public void setRainbow(boolean bl) {
        this.value.setRainbow(bl);
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
        public void setSync(boolean bl) {
            this.sync = bl;
        }

        @Generated
        public void setRainbow(boolean bl) {
            this.rainbow = bl;
        }

        @Generated
        public Color(java.awt.Color color, boolean bl, boolean bl2) {
            this.color = color;
            this.sync = bl;
            this.rainbow = bl2;
        }
    }

    public static class Visibility
    extends Setting.Visibility {
        private final ColorSetting value;
        private final boolean targetValue;
        private final Target target;

        public Visibility(ColorSetting colorSetting, boolean bl, Target target) {
            super(colorSetting);
            this.value = colorSetting;
            this.targetValue = bl;
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

