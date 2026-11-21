/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.StyleAccessor
 *  lightning.mixins.accessors.TextColorAccessor
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 */
package lightning.utils.text;

import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.mixins.accessors.StyleAccessor;
import lightning.mixins.accessors.TextColorAccessor;
import lightning.utils.color.ColorUtils;
import lightning.utils.text.CustomFormatting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class FormattingUtils {
    public static String[] FORMATS = new String[]{"White", "Black", "Blue", "Dark Blue", "Green", "Dark Green", "Aqua", "Dark Aqua", "Red", "Dark Red", "Light Purple", "Dark Purple", "Yellow", "Gold", "Gray", "Dark Gray", "Client", "Rainbow"};

    public static Style withExclusiveFormatting(Style style, CustomFormatting customFormatting) {
        TextColor textColor = style.getColor();
        if (customFormatting == CustomFormatting.CLIENT) {
            textColor = TextColorAccessor.create((int)ColorUtils.getGlobalColor().getRGB(), (String)"CLIENT");
        } else if (customFormatting == CustomFormatting.RAINBOW) {
            textColor = TextColorAccessor.create((int)ColorUtils.getGlobalColor().getRGB(), (String)"RAINBOW");
        }
        return StyleAccessor.create((TextColor)textColor, null, (Boolean)false, (Boolean)false, (Boolean)false, (Boolean)false, (Boolean)false, (ClickEvent)style.getClickEvent(), (HoverEvent)style.getHoverEvent(), (String)style.getInsertion(), (FontDescription)style.getFont());
    }

    public static Object getFormatting(String string) {
        return switch (string.toLowerCase()) {
            case "black" -> ChatFormatting.BLACK;
            case "blue" -> ChatFormatting.BLUE;
            case "dark blue" -> ChatFormatting.DARK_BLUE;
            case "green" -> ChatFormatting.GREEN;
            case "dark green" -> ChatFormatting.DARK_GREEN;
            case "aqua" -> ChatFormatting.AQUA;
            case "dark aqua" -> ChatFormatting.DARK_AQUA;
            case "red" -> ChatFormatting.RED;
            case "dark red" -> ChatFormatting.DARK_RED;
            case "light purple" -> ChatFormatting.LIGHT_PURPLE;
            case "dark purple" -> ChatFormatting.DARK_PURPLE;
            case "yellow" -> ChatFormatting.YELLOW;
            case "gold" -> ChatFormatting.GOLD;
            case "gray" -> ChatFormatting.GRAY;
            case "dark gray" -> ChatFormatting.DARK_GRAY;
            case "client" -> CustomFormatting.CLIENT;
            case "rainbow" -> CustomFormatting.RAINBOW;
            default -> ChatFormatting.WHITE;
        };
    }

    public static List<String> wrapText(String string, int n) {
        ArrayList<String> arrayList = new ArrayList<String>();
        String[] stringArray = string.split(" ");
        Object object = "";
        for (String string2 : stringArray) {
            if (Night.FONT_MANAGER.getWidth((String)object) + Night.FONT_MANAGER.getWidth(string2) <= n) {
                object = (String)object + string2 + " ";
                continue;
            }
            arrayList.add((String)object);
            object = string2 + " ";
        }
        if (Night.FONT_MANAGER.getWidth((String)object) > 0) {
            arrayList.add((String)object);
        }
        return arrayList;
    }

    public static String[] formatSeconds(long l) {
        String string = String.format("%02d", l / 3600L);
        String string2 = String.format("%02d", l % 3600L / 60L);
        String string3 = String.format("%02d", l % 60L);
        return new String[]{string, string2, string3};
    }
}

