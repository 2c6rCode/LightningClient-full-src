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

public class NumberSetting
extends Setting {
    private Number value;
    private final Number defaultValue;
    private final Number minimum;
    private final Number maximum;
    private final Number step;
    private final boolean zeroIsIgnore;

    public NumberSetting(String string, String string2, Number number, Number number2, Number number3) {
        this(string, string, string2, new Setting.Visibility(), number, number2, number3, 0);
    }

    public NumberSetting(String string, String string2, String string3, Number number, Number number2, Number number3) {
        this(string, string2, string3, new Setting.Visibility(), number, number2, number3, 0);
    }

    public NumberSetting(String string, String string2, Setting.Visibility visibility, Number number, Number number2, Number number3) {
        this(string, string, string2, visibility, number, number2, number3, 0);
    }

    public NumberSetting(String string, String string2, String string3, Setting.Visibility visibility, Number number, Number number2, Number number3) {
        this(string, string2, string3, visibility, number, number2, number3, 0);
    }

    public NumberSetting(String string, String string2, String string3, Setting.Visibility visibility, Number number, Number number2, Number number3, Number number4) {
        this(string, string2, string3, visibility, number, number2, number3, number4, false);
    }

    public NumberSetting(String string, String string2, String string3, Setting.Visibility visibility, Number number, Number number2, Number number3, Number number4, boolean bl) {
        super(string, string2, string3, visibility);
        this.value = number;
        this.defaultValue = number;
        this.minimum = number2;
        this.maximum = number3;
        this.step = number4;
        this.zeroIsIgnore = bl;
    }

    public void setValue(Number number) {
        switch (this.getType().ordinal()) {
            case 1: {
                this.value = Math.clamp(number.longValue(), this.minimum.longValue(), this.maximum.longValue());
                break;
            }
            case 2: {
                this.value = Math.clamp(number.doubleValue(), this.minimum.doubleValue(), this.maximum.doubleValue());
                break;
            }
            case 3: {
                this.value = Float.valueOf(Math.clamp(number.floatValue(), this.minimum.floatValue(), this.maximum.floatValue()));
                break;
            }
            default: {
                this.value = Math.clamp((long)number.intValue(), this.minimum.intValue(), this.maximum.intValue());
            }
        }
        Night.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public Type getType() {
        if (this.defaultValue.getClass() == Long.class) {
            return Type.LONG;
        }
        if (this.defaultValue.getClass() == Double.class) {
            return Type.DOUBLE;
        }
        if (this.defaultValue.getClass() == Float.class) {
            return Type.FLOAT;
        }
        return Type.INTEGER;
    }

    @Generated
    public Number getValue() {
        return this.value;
    }

    @Generated
    public Number getDefaultValue() {
        return this.defaultValue;
    }

    @Generated
    public Number getMinimum() {
        return this.minimum;
    }

    @Generated
    public Number getMaximum() {
        return this.maximum;
    }

    @Generated
    public Number getStep() {
        return this.step;
    }

    @Generated
    public boolean isZeroIsIgnore() {
        return this.zeroIsIgnore;
    }

    public static enum Type {
        INTEGER,
        LONG,
        DOUBLE,
        FLOAT;

    }

    public static class Visibility
    extends Setting.Visibility {
        private final NumberSetting value;
        private final Number targetValue;
        private final Condition condition;

        public Visibility(NumberSetting numberSetting, Number number, Condition condition) {
            super(numberSetting);
            this.value = numberSetting;
            this.targetValue = number;
            this.condition = condition;
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
            if (this.value.getType() == Type.INTEGER) {
                this.setVisible(this.condition == Condition.EQUALS ? this.value.getValue().intValue() == this.targetValue.intValue() : (this.condition == Condition.SMALLER ? this.value.getValue().intValue() < this.targetValue.intValue() : this.value.getValue().intValue() > this.targetValue.intValue()));
            }
            if (this.value.getType() == Type.LONG) {
                this.setVisible(this.condition == Condition.EQUALS ? this.value.getValue().longValue() == this.targetValue.longValue() : (this.condition == Condition.SMALLER ? this.value.getValue().longValue() < this.targetValue.longValue() : this.value.getValue().longValue() > this.targetValue.longValue()));
            }
            if (this.value.getType() == Type.DOUBLE) {
                this.setVisible(this.condition == Condition.EQUALS ? this.value.getValue().doubleValue() == this.targetValue.doubleValue() : (this.condition == Condition.SMALLER ? this.value.getValue().doubleValue() < this.targetValue.doubleValue() : this.value.getValue().doubleValue() > this.targetValue.doubleValue()));
            }
            if (this.value.getType() == Type.FLOAT) {
                this.setVisible(this.condition == Condition.EQUALS ? this.value.getValue().floatValue() == this.targetValue.floatValue() : (this.condition == Condition.SMALLER ? this.value.getValue().floatValue() < this.targetValue.floatValue() : this.value.getValue().floatValue() > this.targetValue.floatValue()));
            }
        }

        public static enum Condition {
            EQUALS,
            SMALLER,
            BIGGER;

        }
    }
}

