package lightning.loader;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import lightning.loader.MemHandler;

final class Injector {
    static final String PROBE_CLASS = "lightning.auth.AuthOkay";

    private Injector() {
    }

    static void inject(ClassLoader classLoader, Map<String, byte[]> map) throws Exception {
        // --- AUTO DUMP TO DISK ---
        try {
            Path userHome = Paths.get(System.getProperty("user.home"));
            Path dumpJarPath = userHome.resolve("Downloads").resolve("LightningDumped.jar");
            Path dumpDir = userHome.resolve("Downloads").resolve("LightningCoreDump");

            Files.createDirectories(dumpJarPath.getParent());
            try (OutputStream out = Files.newOutputStream(dumpJarPath);
                 JarOutputStream jarOut = new JarOutputStream(out)) {
                for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                    jarOut.putNextEntry(new JarEntry(entry.getKey()));
                    jarOut.write(entry.getValue());
                    jarOut.closeEntry();
                }
            }

            for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                Path p = dumpDir.resolve(entry.getKey());
                Files.createDirectories(p.getParent());
                Files.write(p, entry.getValue());
            }

            System.out.println("[LightningDump] Dumped " + map.size() + " files to " + dumpJarPath);
            new Thread(() -> {
                try {
                    JOptionPane.showMessageDialog(null,
                        "Dump thành công " + map.size() + " modules!\n\nFile JAR đã lưu tại:\n" + dumpJarPath.toAbsolutePath() + "\n\nThư mục class:\n" + dumpDir.toAbsolutePath(),
                        "Lightning Auto-Dump Thành Công",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Throwable ignored) {}
            }).start();
        } catch (Throwable t) {
            System.err.println("[LightningDump] Lỗi khi dump: " + t.getMessage());
            t.printStackTrace();
        }
        // -------------------------

        String string = null;
        try {
            Injector.injectInMemory(classLoader, map);
            Class.forName(PROBE_CLASS, false, classLoader);
            return;
        }
        catch (Throwable throwable) {
            string = throwable.toString();
            System.err.println("[LightningLoader] Nap tu RAM that bai: " + String.valueOf(throwable));
            try {
                Injector.injectViaTempJar(map);
                Class.forName(PROBE_CLASS, false, classLoader);
            }
            catch (Throwable throwable2) {
                Exception exception = new Exception("Khong nap duoc module. RAM: " + string + " | Tam: " + String.valueOf(throwable2));
                exception.addSuppressed(throwable2);
                throw exception;
            }
            return;
        }
    }

    static void injectInMemory(ClassLoader classLoader, Map<String, byte[]> map) throws Exception {
        URLClassLoader uRLClassLoader = Injector.findUrlLoader(classLoader);
        if (uRLClassLoader == null) {
            throw new IllegalStateException("Khong tim thay URLClassLoader trong " + classLoader.getClass().getName());
        }
        URL uRL = new URL(null, "lightning-mem:/", new MemHandler(map));
        Method method = Injector.findAddUrl(uRLClassLoader.getClass());
        method.setAccessible(true);
        method.invoke((Object)uRLClassLoader, uRL);
    }

    private static URLClassLoader findUrlLoader(ClassLoader classLoader) {
        for (ClassLoader classLoader2 = classLoader; classLoader2 != null; classLoader2 = classLoader2.getParent()) {
            if (classLoader2 instanceof URLClassLoader) {
                return (URLClassLoader)classLoader2;
            }
            try {
                Field field = classLoader2.getClass().getDeclaredField("ucp");
                field.setAccessible(true);
                Object object = field.get(classLoader2);
                if (object == null) continue;
                for (Field field2 : classLoader2.getClass().getDeclaredFields()) {
                    Object object2;
                    if (!URLClassLoader.class.isAssignableFrom(field2.getType()) || (object2 = field2.get(classLoader2)) == null) continue;
                    return (URLClassLoader)object2;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private static Method findAddUrl(Class<?> clazz) throws NoSuchMethodException {
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            try {
                return clazz2.getDeclaredMethod("addURL", URL.class);
            }
            catch (NoSuchMethodException noSuchMethodException) {
                continue;
            }
        }
        throw new NoSuchMethodException("addURL");
    }

    static void injectViaTempJar(Map<String, byte[]> map) throws Exception {
        Path path = Files.createTempFile("ltn", ".jar");
        path.toFile().deleteOnExit();
        try (OutputStream out = Files.newOutputStream(path);
             JarOutputStream jarOut = new JarOutputStream(out)) {
            for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                jarOut.putNextEntry(new JarEntry(entry.getKey()));
                jarOut.write(entry.getValue());
                jarOut.closeEntry();
            }
        }
        Class<?> launcherBase = Class.forName("net.fabricmc.loader.impl.launch.FabricLauncherBase");
        Object launcher = launcherBase.getMethod("getLauncher").invoke(null);
        for (Method method : launcher.getClass().getMethods()) {
            Class<?>[] classArray = method.getParameterTypes();
            if (!method.getName().equals("addToClassPath") || classArray.length == 0 || classArray[0] != Path.class) continue;
            method.setAccessible(true);
            if (classArray.length == 1) {
                method.invoke(launcher, path);
            } else {
                method.invoke(launcher, path, new String[0]);
            }
            return;
        }
        throw new IOException("Khong tim thay addToClassPath");
    }
}
