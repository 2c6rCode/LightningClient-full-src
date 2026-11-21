/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.StringRepresentable
 */
package lightning.utils.text;

import lombok.Generated;
import net.minecraft.util.StringRepresentable;

public enum CustomFormatting implements StringRepresentable
{
    CLIENT('z'),
    RAINBOW('y');

    private final char code;

    private CustomFormatting(char c) {
        this.code = c;
    }

    public String getName() {
        return this.name().toLowerCase();
    }

    public static CustomFormatting byCode(char c) {
        char c2 = Character.toLowerCase(c);
        for (CustomFormatting customFormatting : CustomFormatting.values()) {
            if (customFormatting.code != c2) continue;
            return customFormatting;
        }
        return null;
    }

    public String toString() {
        return "\u00a7" + this.code;
    }

    public String getSerializedName() {
        return null;
    }

    @Generated
    public char getCode() {
        return this.code;
    }
}

