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

    public static List<Class<?>> findSubtypesOf(Class<?> baseClass, Class<?> anchorClass) {
        ArrayList<Class<?>> subtypes = new ArrayList<>();
        ClassLoader classLoader = anchorClass.getClassLoader();
        URL codeSourceUrl = anchorClass.getProtectionDomain().getCodeSource() != null ? anchorClass.getProtectionDomain().getCodeSource().getLocation() : null;
        List<String> classNames = new ArrayList<>();
        if (codeSourceUrl != null) {
            try {
                File locationFile;
                try {
                    locationFile = new File(codeSourceUrl.toURI());
                }
                catch (Throwable throwable) {
                    locationFile = new File(codeSourceUrl.getPath());
                }
                if (locationFile.isDirectory()) {
                    classNames = JarClassScanner.scanDirectory(locationFile);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (classNames.isEmpty()) {
                try (InputStream stream = codeSourceUrl.openStream()){
                    JarInputStream jarStream = new JarInputStream(stream);
                    try {
                        JarEntry entry;
                        while ((entry = jarStream.getNextJarEntry()) != null) {
                            String entryName = entry.getName();
                            if (entry.isDirectory() || !entryName.endsWith(".class") || entryName.contains("module-info")) continue;
                            classNames.add(JarClassScanner.toClassName(entryName));
                        }
                    }
                    finally {
                        jarStream.close();
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        for (String className : classNames) {
            Class<?> candidate;
            int modifiers;
            try {
                candidate = Class.forName(className, false, classLoader);
            }
            catch (Throwable throwable) {
                continue;
            }
            if (candidate == baseClass || !baseClass.isAssignableFrom(candidate) || Modifier.isAbstract(modifiers = candidate.getModifiers()) || Modifier.isInterface(modifiers)) continue;
            subtypes.add(candidate);
        }
        return subtypes;
    }

    private static List<String> scanJar(File file) {
        ArrayList<String> classNames = new ArrayList<String>();
        try (JarFile jarFile = new JarFile(file);){
            Enumeration<JarEntry> enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement();
                String entryName = jarEntry.getName();
                if (jarEntry.isDirectory() || !entryName.endsWith(".class") || entryName.contains("module-info")) continue;
                classNames.add(JarClassScanner.toClassName(entryName));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return classNames;
    }

    private static List<String> scanDirectory(File directory) {
        Path rootPath = directory.toPath();
        try (Stream<Path> stream = Files.walk(rootPath, new FileVisitOption[0])) {
            return stream.filter(path -> path.toString().endsWith(".class") && !path.toString().contains("module-info"))
                .map(classPath -> JarClassScanner.toClassName(rootPath.relativize(classPath).toString().replace(File.separatorChar, '/')))
                .toList();
        } catch (Exception exception) {
            return List.of();
        }
    }

    private static String toClassName(String entryName) {
        return entryName.substring(0, entryName.length() - ".class".length()).replace('/', '.');
    }
}

