/*
 * Decompiled with CFR 0.152.
 */
package lightning.gui.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.ToIntFunction;

public final class DescriptionLayout {
    private DescriptionLayout() {
    }

    public static List<String> wrap(String string, int n, ToIntFunction<String> toIntFunction) {
        if (string.isEmpty()) {
            return List.of();
        }
        if (n < 1) {
            throw new IllegalArgumentException("No text width");
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : string.split("\\R", -1)) {
            Object object = "";
            for (String string3 : string2.strip().split("\\s+")) {
                int n2;
                String string4;
                if (string3.isEmpty()) continue;
                String string5 = string4 = ((String)object).isEmpty() ? string3 : (String)object + " " + string3;
                if (toIntFunction.applyAsInt(string4) <= n) {
                    object = string4;
                    continue;
                }
                if (!((String)object).isEmpty()) {
                    arrayList.add((String)object);
                    object = "";
                }
                for (int i = 0; i < string3.length(); i += Character.charCount(n2)) {
                    n2 = string3.codePointAt(i);
                    String string6 = new String(Character.toChars(n2));
                    if (toIntFunction.applyAsInt(string6) > n) {
                        throw new IllegalArgumentException("Glyph exceeds text width");
                    }
                    if (toIntFunction.applyAsInt((String)object + string6) > n) {
                        arrayList.add((String)object);
                        object = "";
                    }
                    object = (String)object + string6;
                }
            }
            arrayList.add((String)object);
        }
        return List.copyOf(arrayList);
    }

    public static Panel create(String string, String string2, String string3, String string4, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, List<Rect> list, ToIntFunction<String> toIntFunction) {
        int n9 = Math.max(n3, n2 + 4);
        int n10 = toIntFunction.applyAsInt(string3) + toIntFunction.applyAsInt(string4) + n * 3;
        if (n7 < n10 || n8 < n9 + n * 2) {
            return null;
        }
        LinkedHashSet<Integer> linkedHashSet = new LinkedHashSet<Integer>();
        linkedHashSet.add(Math.min(n7, Math.max(n10, n4)));
        for (int i = (Integer)linkedHashSet.iterator().next() + 24; i < n7; i += 24) {
            linkedHashSet.add(i);
        }
        linkedHashSet.add(n7);
        for (Rect rect : list) {
            int n11;
            if (rect.x >= n10 && rect.x <= n7) {
                linkedHashSet.add(rect.x);
            }
            if ((n11 = n7 - rect.x - rect.width) >= n10 && n11 <= n7) {
                linkedHashSet.add(n11);
            }
            for (Rect rect2 : list) {
                int n12 = rect2.x - rect.x - rect.width;
                if (n12 < n10 || n12 > n7) continue;
                linkedHashSet.add(n12);
            }
        }
        Iterator<Integer> iterator = linkedHashSet.iterator();
        while (iterator.hasNext()) {
            List<String> list2;
            List<String> list3;
            int n13 = iterator.next();
            try {
                list3 = DescriptionLayout.wrap(string, n13 - n * 2, toIntFunction);
                list2 = DescriptionLayout.wrap(string2, n13 - n * 2, toIntFunction);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                continue;
            }
            int n14 = n9 + n * 2 + (list3.size() + list2.size()) * n2;
            if (n14 > n8) continue;
            LinkedHashSet<Integer> linkedHashSet2 = new LinkedHashSet<Integer>();
            LinkedHashSet<Integer> linkedHashSet3 = new LinkedHashSet<Integer>();
            linkedHashSet2.add(Math.clamp((long)n5, 0, n7 - n13));
            linkedHashSet3.add(Math.clamp((long)n6, 0, n8 - n14));
            linkedHashSet2.add(0);
            linkedHashSet2.add(n7 - n13);
            linkedHashSet3.add(0);
            linkedHashSet3.add(n8 - n14);
            for (Rect rect : list) {
                linkedHashSet2.add(rect.x - n13);
                linkedHashSet2.add(rect.x + rect.width);
                linkedHashSet3.add(rect.y - n14);
                linkedHashSet3.add(rect.y + rect.height);
            }
            for (int n15 : linkedHashSet3) {
                for (int n16 : linkedHashSet2) {
                    if (n16 < 0 || n15 < 0 || n16 + n13 > n7 || n15 + n14 > n8) continue;
                    Rect rect = new Rect(n16, n15, n13, n14);
                    if (!list.stream().noneMatch(rect::intersects)) continue;
                    return new Panel(rect, n9, list3, list2);
                }
            }
        }
        return null;
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean intersects(Rect rect) {
            return this.x < rect.x + rect.width && this.x + this.width > rect.x && this.y < rect.y + rect.height && this.y + this.height > rect.y;
        }
    }

    public record Panel(Rect bounds, int headerHeight, List<String> english, List<String> vietnamese) {
        public int totalHeight() {
            return this.bounds.height();
        }
    }
}

