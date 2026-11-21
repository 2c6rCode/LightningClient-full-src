/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 */
package lightning.utils.color;

import java.awt.Color;
import lightning.Night;
import lightning.modules.impl.core.ColorModule;
import lightning.settings.impl.ColorSetting;
import net.minecraft.ChatFormatting;

public class ColorUtils {
    public static ColorSetting.Color getDefaultColor() {
        return new ColorSetting.Color(new Color(160, 120, 255), true, false);
    }

    public static ColorSetting.Color getDefaultFillColor() {
        return new ColorSetting.Color(new Color(160, 120, 255, 40), true, false);
    }

    public static ColorSetting.Color getDefaultOutlineColor() {
        return new ColorSetting.Color(new Color(160, 120, 255, 120), true, false);
    }

    public static Color getGlobalColor() {
        return ColorUtils.getGlobalColor(255, 0L);
    }

    public static Color getGlobalColor(int n) {
        return ColorUtils.getGlobalColor(n, 0L);
    }

    public static Color getGlobalColor(int n, long l) {
        ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
        ColorModule colorModule2 = colorModule;
        if (colorModule == null) {
            return new Color(160, 120, 255, n);
        }
        if (colorModule.color.isRainbow()) {
            return ColorUtils.getRainbow(colorModule.rainbowSpeed.getValue().longValue(), colorModule.rainbowSaturation.getValue().floatValue() / 100.0f, colorModule.rainbowBrightness.getValue().floatValue() / 100.0f, n, l);
        }
        return ColorUtils.getColor(colorModule.color.getValue().getColor(), n);
    }

    public static Color getColor(Color color, int n) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), n);
    }

    public static Color getRainbow() {
        return ColorUtils.getRainbow(255);
    }

    public static Color getOffsetRainbow(long l) {
        return ColorUtils.getOffsetRainbow(255, l);
    }

    public static Color getRainbow(int n) {
        return ColorUtils.getOffsetRainbow(n, 0L);
    }

    public static Color getOffsetRainbow(int n, long l) {
        ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
        long l2 = colorModule != null ? colorModule.rainbowSpeed.getValue().longValue() : 6L;
        float f = colorModule != null ? colorModule.rainbowSaturation.getValue().floatValue() / 100.0f : 1.0f;
        float f2 = colorModule != null ? colorModule.rainbowBrightness.getValue().floatValue() / 100.0f : 1.0f;
        float f3 = colorModule != null && colorModule.rainbowScale != null ? colorModule.rainbowScale.getValue().floatValue() / 100.0f : 1.0f;
        return ColorUtils.getRainbow(l2, f, f2, n, (long)((float)l * f3));
    }

    public static Color getRainbow(long l, float f, float f2, int n) {
        return ColorUtils.getRainbow(l, f, f2, n, 0L);
    }

    public static Color getRainbow(long l, float f, float f2, int n, long l2) {
        double d;
        l = Math.clamp(l, 1, 20);
        double d2 = 10500.0 - 500.0 * (double)l;
        double d3 = (double)(System.currentTimeMillis() % (long)d2) / d2;
        float f3 = (float)((d3 + (d = (double)l2 * 0.0035)) % 1.0);
        if (f3 < 0.0f) {
            f3 += 1.0f;
        }
        Color color = new Color(Color.HSBtoRGB(Math.clamp(f3, 0.0f, 1.0f), Math.clamp(f, 0.0f, 1.0f), Math.clamp(f2, 0.0f, 1.0f)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), n);
    }

    public static Color getOffsetWave(Color color, long l) {
        ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
        long l2 = colorModule != null ? colorModule.rainbowSpeed.getValue().longValue() : 6L;
        return ColorUtils.getWave(color, l2, 255, l);
    }

    public static Color getWave(Color color, long l, int n, long l2) {
        l = Math.max(1L, Math.min(l, 20L));
        float[] fArray = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), fArray);
        double d = 10500.0 - 500.0 * (double)l;
        double d2 = (double)(System.currentTimeMillis() % (long)d) / d;
        double d3 = (double)l2 * 0.0035;
        double d4 = (d2 + d3) % 1.0;
        if (d4 < 0.0) {
            d4 += 1.0;
        }
        float f = (float)Math.abs(d4 * 2.0 % 2.0 - 1.0);
        fArray[2] = 0.5f + 0.5f * f;
        Color color2 = new Color(Color.HSBtoRGB(fArray[0], fArray[1], fArray[2]));
        return new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), n);
    }

    public static Color getPulse(Color color) {
        return ColorUtils.getPulse(color, 15L);
    }

    public static Color getPulse(Color color, long l) {
        l = Math.max(1L, Math.min(l, 20L));
        double d = Math.sin(Math.PI * 2 * (double)((float)l / 20.0f) * (double)((float)(System.currentTimeMillis() - Night.UPTIME) / 1000.0f));
        double d2 = (d + 1.0) / 2.0;
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((double)color.getAlpha() * d2));
    }

    public static Color getHashColor(String string) {
        int n = string.hashCode();
        return new Color((n & 0xFF0000) >> 16, (n & 0xFF00) >> 8, n & 0xFF);
    }

    public static boolean isValidColorCode(String string) {
        if (string.startsWith("#")) {
            string = string.substring(1);
        }
        if (string.length() != 3 && string.length() != 6) {
            return false;
        }
        for (int i = 0; i < string.length(); ++i) {
            if (string.charAt(i) >= '0' && string.charAt(i) <= '\t' || string.charAt(i) >= 'a' && string.charAt(i) <= 'f' || string.charAt(i) >= 'A' || string.charAt(i) <= 'F') continue;
            return false;
        }
        return true;
    }

    public static ChatFormatting getHealthColor(double d) {
        if (d > 18.0) {
            return ChatFormatting.GREEN;
        }
        if (d > 16.0) {
            return ChatFormatting.DARK_GREEN;
        }
        if (d > 12.0) {
            return ChatFormatting.YELLOW;
        }
        if (d > 8.0) {
            return ChatFormatting.GOLD;
        }
        if (d > 5.0) {
            return ChatFormatting.RED;
        }
        return ChatFormatting.DARK_RED;
    }

    public static ChatFormatting getTotemColor(int n) {
        if (n == 1) {
            return ChatFormatting.GREEN;
        }
        if (n == 2) {
            return ChatFormatting.DARK_GREEN;
        }
        if (n == 3) {
            return ChatFormatting.YELLOW;
        }
        if (n == 4) {
            return ChatFormatting.GOLD;
        }
        if (n == 5) {
            return ChatFormatting.RED;
        }
        return ChatFormatting.DARK_RED;
    }
}

