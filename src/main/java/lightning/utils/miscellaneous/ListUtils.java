/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.miscellaneous;

import java.util.List;

public class ListUtils {
    public static int getIndex(List<String> list, String string) {
        for (int i = 0; i < list.size(); ++i) {
            if (!list.get(i).equalsIgnoreCase(string)) continue;
            return i;
        }
        return -1;
    }
}

