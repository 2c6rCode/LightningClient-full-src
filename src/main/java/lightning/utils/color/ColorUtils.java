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

    public static Color getGlobalColor(int alpha) {
        return ColorUtils.getGlobalColor(alpha, 0L);
    }

    public static Color getGlobalColor(int alpha, long offset) {
        ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
        if (colorModule == null) {
            return new Color(160, 120, 255, alpha);
        }
        if (colorModule.color.isRainbow()) {
            return ColorUtils.getRainbow(colorModule.rainbowSpeed.getValue().longValue(), colorModule.rainbowSaturation.getValue().floatValue() / 100.0f, colorModule.rainbowBrightness.getValue().floatValue() / 100.0f, alpha, offset);
        }
        return ColorUtils.getColor(colorModule.color.getValue().getColor(), alpha);
    }

    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color getRainbow() {
        return ColorUtils.getRainbow(255);
    }

    public static Color getOffsetRainbow(long offset) {
        return ColorUtils.getOffsetRainbow(255, offset);
    }

    public static Color getRainbow(int alpha) {
        return ColorUtils.getOffsetRainbow(alpha, 0L);
    }

    public static Color getOffsetRainbow(int alpha, long offset) {
        ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
        long speed = colorModule != null ? colorModule.rainbowSpeed.getValue().longValue() : 6L;
        float saturation = colorModule != null ? colorModule.rainbowSaturation.getValue().floatValue() / 100.0f : 1.0f;
        float brightness = colorModule != null ? colorModule.rainbowBrightness.getValue().floatValue() / 100.0f : 1.0f;
        float scale = colorModule != null && colorModule.rainbowScale != null ? colorModule.rainbowScale.getValue().floatValue() / 100.0f : 1.0f;
        return ColorUtils.getRainbow(speed, saturation, brightness, alpha, (long)((float)offset * scale));
    }

    public static Color getRainbow(long speed, float saturation, float brightness, int alpha) {
        return ColorUtils.getRainbow(speed, saturation, brightness, alpha, 0L);
    }

    public static Color getRainbow(long speed, float saturation, float brightness, int alpha, long offset) {
        speed = Math.clamp(speed, 1, 20);
        double cycleMs = 10500.0 - 500.0 * (double)speed;
        double baseHue = (double)(System.currentTimeMillis() % (long)cycleMs) / cycleMs;
        double offsetHue = (double)offset * 0.0035;
        float hue = (float)((baseHue + offsetHue) % 1.0);
        if (hue < 0.0f) {
            hue += 1.0f;
        }
        Color color = new Color(Color.HSBtoRGB(Math.clamp(hue, 0.0f, 1.0f), Math.clamp(saturation, 0.0f, 1.0f), Math.clamp(brightness, 0.0f, 1.0f)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color getOffsetWave(Color color, long offset) {
        ColorModule colorModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(ColorModule.class) : null;
        long speed = colorModule != null ? colorModule.rainbowSpeed.getValue().longValue() : 6L;
        return ColorUtils.getWave(color, speed, 255, offset);
    }

    public static Color getWave(Color color, long speed, int alpha, long offset) {
        speed = Math.max(1L, Math.min(speed, 20L));
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        double cycleMs = 10500.0 - 500.0 * (double)speed;
        double basePhase = (double)(System.currentTimeMillis() % (long)cycleMs) / cycleMs;
        double offsetPhase = (double)offset * 0.0035;
        double phase = (basePhase + offsetPhase) % 1.0;
        if (phase < 0.0) {
            phase += 1.0;
        }
        float wave = (float)Math.abs(phase * 2.0 % 2.0 - 1.0);
        hsb[2] = 0.5f + 0.5f * wave;
        Color waveColor = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        return new Color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), alpha);
    }

    public static Color getPulse(Color color) {
        return ColorUtils.getPulse(color, 15L);
    }

    public static Color getPulse(Color color, long speed) {
        speed = Math.max(1L, Math.min(speed, 20L));
        double wave = Math.sin(Math.PI * 2 * (double)((float)speed / 20.0f) * (double)((float)(System.currentTimeMillis() - Night.UPTIME) / 1000.0f));
        double factor = (wave + 1.0) / 2.0;
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((double)color.getAlpha() * factor));
    }

    public static Color getHashColor(String input) {
        int hash = input.hashCode();
        return new Color((hash & 0xFF0000) >> 16, (hash & 0xFF00) >> 8, hash & 0xFF);
    }

    public static boolean isValidColorCode(String code) {
        if (code.startsWith("#")) {
            code = code.substring(1);
        }
        if (code.length() != 3 && code.length() != 6) {
            return false;
        }
        for (int i = 0; i < code.length(); ++i) {
            if (code.charAt(i) >= '0' && code.charAt(i) <= '\t' || code.charAt(i) >= 'a' && code.charAt(i) <= 'f' || code.charAt(i) >= 'A' || code.charAt(i) <= 'F') continue;
            return false;
        }
        return true;
    }

    public static ChatFormatting getHealthColor(double health) {
        if (health > 18.0) {
            return ChatFormatting.GREEN;
        }
        if (health > 16.0) {
            return ChatFormatting.DARK_GREEN;
        }
        if (health > 12.0) {
            return ChatFormatting.YELLOW;
        }
        if (health > 8.0) {
            return ChatFormatting.GOLD;
        }
        if (health > 5.0) {
            return ChatFormatting.RED;
        }
        return ChatFormatting.DARK_RED;
    }

    public static ChatFormatting getTotemColor(int count) {
        if (count == 1) {
            return ChatFormatting.GREEN;
        }
        if (count == 2) {
            return ChatFormatting.DARK_GREEN;
        }
        if (count == 3) {
            return ChatFormatting.YELLOW;
        }
        if (count == 4) {
            return ChatFormatting.GOLD;
        }
        if (count == 5) {
            return ChatFormatting.RED;
        }
        return ChatFormatting.DARK_RED;
    }
}

