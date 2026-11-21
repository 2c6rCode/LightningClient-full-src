/*
 * Decompiled with CFR 0.152.
 */
package lightning.pingbypass.modules;

import java.awt.Color;
import lightning.pingbypass.modules.PbModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;

public class SyncModule {
    public static void applyToggle(PbModule pbModule, boolean bl) {
        pbModule.setToggled(bl);
    }

    public static void applySetting(PbModule pbModule, String string, String string2) {
        Setting setting = pbModule.getSetting(string);
        if (setting == null) {
            return;
        }
        if (setting instanceof BooleanSetting) {
            BooleanSetting booleanSetting = (BooleanSetting)setting;
            booleanSetting.setValue(Boolean.parseBoolean(string2));
        } else if (setting instanceof NumberSetting) {
            NumberSetting numberSetting = (NumberSetting)setting;
            switch (numberSetting.getType()) {
                case INTEGER: {
                    numberSetting.setValue(Integer.parseInt(string2));
                    break;
                }
                case LONG: {
                    numberSetting.setValue(Long.parseLong(string2));
                    break;
                }
                case FLOAT: {
                    numberSetting.setValue(Float.valueOf(Float.parseFloat(string2)));
                    break;
                }
                case DOUBLE: {
                    numberSetting.setValue(Double.parseDouble(string2));
                }
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting modeSetting = (ModeSetting)setting;
            modeSetting.setValue(string2);
        } else if (setting instanceof StringSetting) {
            StringSetting stringSetting = (StringSetting)setting;
            stringSetting.setValue(string2);
        } else if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting)setting;
            String[] stringArray = string2.split(",");
            if (stringArray.length >= 4) {
                colorSetting.setColor(new Color(Integer.parseInt(stringArray[0]), Integer.parseInt(stringArray[1]), Integer.parseInt(stringArray[2]), Integer.parseInt(stringArray[3])));
                if (stringArray.length >= 5) {
                    colorSetting.setSync(Boolean.parseBoolean(stringArray[4]));
                }
                if (stringArray.length >= 6) {
                    colorSetting.setRainbow(Boolean.parseBoolean(stringArray[5]));
                }
            }
        }
    }
}

