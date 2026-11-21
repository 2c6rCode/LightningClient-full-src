/*
 * Decompiled with CFR 0.152.
 */
package lightning.loader;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class MemScan {
    private static volatile List<String> names;

    private MemScan() {
    }

    public static void register(Map<String, byte[]> map) {
        TreeSet<String> treeSet = new TreeSet<String>();
        for (String string : map.keySet()) {
            if (string == null || !string.endsWith(".class") || string.contains("module-info") || string.startsWith("lightning/mixins/") || string.startsWith("lightning/loader/") || string.startsWith("META-INF/")) continue;
            treeSet.add(string.substring(0, string.length() - 6).replace('/', '.'));
        }
        names = new ArrayList<String>(treeSet);
    }

    public static List<Class<?>> find(Class<?> clazz, Class<?> clazz2) {
        List<String> list = names;
        if (list == null || clazz == null || clazz2 == null) {
            return null;
        }
        ClassLoader classLoader = clazz2.getClassLoader();
        ArrayList arrayList = new ArrayList();
        for (String string : list) {
            try {
                Class<?> clazz3 = Class.forName(string, false, classLoader);
                if (clazz3 == clazz || !clazz.isAssignableFrom(clazz3) || clazz3.isInterface() || Modifier.isAbstract(clazz3.getModifiers())) continue;
                arrayList.add(clazz3);
            }
            catch (Throwable throwable) {}
        }
        return arrayList;
    }
}

