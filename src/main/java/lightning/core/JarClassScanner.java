/*
 * Decompiled with CFR 0.152.
 */
package lightning.core;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class JarClassScanner {
    private JarClassScanner() {
    }

    public static List<Class<?>> findSubtypesOf(Class<?> clazz, Class<?> clazz2) {
        Object object;
        ArrayList arrayList = new ArrayList();
        ClassLoader classLoader = clazz2.getClassLoader();
        URL uRL = clazz2.getProtectionDomain().getCodeSource() != null ? clazz2.getProtectionDomain().getCodeSource().getLocation() : null;
        List<String> list = new ArrayList<>();
        if (uRL != null) {
            Object object2;
            try {
                try {
                    object2 = new File(uRL.toURI());
                }
                catch (Throwable throwable) {
                    object2 = new File(uRL.getPath());
                }
                if (((File)object2).isDirectory()) {
                    list = JarClassScanner.scanDirectory((File)object2);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (list.isEmpty()) {
                try (InputStream object3 = uRL.openStream()){
                    object = new JarInputStream((InputStream)object3);
                    try {
                        while ((object2 = ((JarInputStream)object).getNextJarEntry()) != null) {
                            String string = ((ZipEntry)object2).getName();
                            if (((ZipEntry)object2).isDirectory() || !string.endsWith(".class") || string.contains("module-info")) continue;
                            list.add(JarClassScanner.toClassName(string));
                        }
                    }
                    finally {
                        ((ZipInputStream)object).close();
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        for (Object object3 : list) {
            int n;
            try {
                object = Class.forName((String)object3, false, classLoader);
            }
            catch (Throwable throwable) {
                continue;
            }
            if (object == clazz || !clazz.isAssignableFrom((Class<?>)object) || Modifier.isAbstract(n = ((Class)object).getModifiers()) || Modifier.isInterface(n)) continue;
            arrayList.add((Class<?>)object);
        }
        return arrayList;
    }

    private static List<String> scanJar(File file) {
        ArrayList<String> arrayList = new ArrayList<String>();
        try (JarFile jarFile = new JarFile(file);){
            Enumeration<JarEntry> enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement();
                String string = jarEntry.getName();
                if (jarEntry.isDirectory() || !string.endsWith(".class") || string.contains("module-info")) continue;
                arrayList.add(JarClassScanner.toClassName(string));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return arrayList;
    }

    private static List<String> scanDirectory(File file) {
        Path path3 = file.toPath();
        try (Stream<Path> stream = Files.walk(path3, new FileVisitOption[0])) {
            return stream.filter(path -> path.toString().endsWith(".class") && !path.toString().contains("module-info"))
                .map(path2 -> JarClassScanner.toClassName(path3.relativize(path2).toString().replace(File.separatorChar, '/')))
                .toList();
        } catch (Exception exception) {
            return List.of();
        }
    }

    private static String toClassName(String string) {
        return string.substring(0, string.length() - ".class".length()).replace('/', '.');
    }
}

