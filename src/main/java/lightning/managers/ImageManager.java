/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.platform.NativeImage$Format
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package lightning.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import lightning.Night;
import lightning.utils.graphics.CustomImageTexture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class ImageManager {
    public static final Path IMAGES_DIR = Paths.get("Lightning", "images");
    public static final Identifier CUSTOM_IMAGE_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/effect/custom_image.png");
    public static final Identifier CUSTOM_IMAGE_ID_LEGACY = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/custom_image.png");
    private final Map<String, ImageEntry> cache = new ConcurrentHashMap<String, ImageEntry>();
    private String currentImageName = "None";

    public ImageManager() {
        this.ensureDirectoryExists();
        try {
            Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID, (AbstractTexture)CustomImageTexture.INSTANCE);
            Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID_LEGACY, (AbstractTexture)CustomImageTexture.INSTANCE);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        String activeImage = ImageManager.findSavedActiveImage();
        if (activeImage != null && !activeImage.equalsIgnoreCase("None") && !activeImage.isEmpty()) {
            this.setCurrentActiveImage(activeImage);
        } else {
            this.initDefaultTexture();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String findSavedActiveImage() {
        try {
            String configName = "default";
            Path generalPath = Paths.get("Lightning", "General.json");
            if (Files.exists(generalPath, new LinkOption[0])) {
                try (InputStream generalStream = Files.newInputStream(generalPath, new OpenOption[0])) {
                    JsonObject generalJson = JsonParser.parseReader((Reader)new InputStreamReader(generalStream)).getAsJsonObject();
                    if (generalJson.has("Config")) {
                        configName = generalJson.get("Config").getAsString();
                    }
                }
            }
            Path configPath = Paths.get("Lightning", "Configs", configName + ".json");
            if (!Files.exists(configPath, new LinkOption[0])) {
                configPath = Paths.get("Lightning", "Configs", "default.json");
            }
            if (!Files.exists(configPath, new LinkOption[0])) {
                return "None";
            }
            try (InputStream configStream = Files.newInputStream(configPath, new OpenOption[0])) {
                JsonObject configJson = JsonParser.parseReader((Reader)new InputStreamReader(configStream)).getAsJsonObject();
                if (!configJson.has("Shaders")) {
                    return "None";
                }
                JsonObject shadersJson = configJson.getAsJsonObject("Shaders");
                if (!shadersJson.has("Settings")) {
                    return "None";
                }
                JsonObject settingsJson = shadersJson.getAsJsonObject("Settings");
                if (!settingsJson.has("Image")) {
                    return "None";
                }
                String imageName = settingsJson.get("Image").getAsString();
                if (imageName == null || imageName.isEmpty() || imageName.equalsIgnoreCase("None")) {
                    return "None";
                }
                return imageName;
            }
        }
        catch (Throwable throwable) {
            Night.LOGGER.debug("Could not pre-read saved image config: {}", (Object)throwable.getMessage());
            return "None";
        }
    }

    public void ensureDirectoryExists() {
        try {
            if (!Files.exists(IMAGES_DIR, new LinkOption[0])) {
                Files.createDirectories(IMAGES_DIR, new FileAttribute[0]);
            }
        }
        catch (Exception exception) {
            Night.LOGGER.error("Failed to create images directory", (Throwable)exception);
        }
    }

    public static NativeImage loadNativeImage(File file) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(file);
        if (bufferedImage == null) {
            throw new Exception("ImageIO failed to read image file: " + file.getName());
        }
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
        for (int i = 0; i < bufferedImage.getHeight(); ++i) {
            for (int j = 0; j < bufferedImage.getWidth(); ++j) {
                nativeImage.setPixel(j, i, bufferedImage.getRGB(j, i));
            }
        }
        return nativeImage;
    }

    public static NativeImage loadNativeImage(InputStream inputStream) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        if (bufferedImage == null) {
            throw new Exception("ImageIO failed to read image stream");
        }
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
        for (int i = 0; i < bufferedImage.getHeight(); ++i) {
            for (int j = 0; j < bufferedImage.getWidth(); ++j) {
                nativeImage.setPixel(j, i, bufferedImage.getRGB(j, i));
            }
        }
        return nativeImage;
    }

    private void initDefaultTexture() {
        try {
            NativeImage nativeImage = new NativeImage(1, 1, false);
            nativeImage.setPixel(0, 0, 0);
            CustomImageTexture.INSTANCE.update(nativeImage);
        }
        catch (Throwable throwable) {
            Night.LOGGER.warn("Failed to load default custom_image fallback", throwable);
        }
    }

    public static File resolveImageFile(String imageName) {
        if (imageName == null || imageName.isEmpty() || imageName.equalsIgnoreCase("None")) {
            return null;
        }
        ArrayList<Path> searchDirs = new ArrayList<Path>();
        searchDirs.add(IMAGES_DIR);
        searchDirs.add(Paths.get("Lightning", "Images"));
        searchDirs.add(Paths.get("Lightning", "images"));
        searchDirs.add(Paths.get("Lightning", "Images"));
        searchDirs.add(Paths.get("lightning", "images"));
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            searchDirs.add(gameDir.resolve("Lightning").resolve("images"));
            searchDirs.add(gameDir.resolve("Lightning").resolve("Images"));
            searchDirs.add(gameDir.resolve("Lightning").resolve("images"));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (Path dir : searchDirs) {
            File candidate = dir.resolve(imageName).toFile();
            if (!candidate.exists() || !candidate.isFile()) continue;
            return candidate;
        }
        return IMAGES_DIR.resolve(imageName).toFile();
    }

    public List<File> getImageFiles() {
        this.ensureDirectoryExists();
        LinkedHashMap<String, File> uniqueFiles = new LinkedHashMap<String, File>();
        ArrayList<Path> searchDirs = new ArrayList<Path>();
        searchDirs.add(IMAGES_DIR);
        searchDirs.add(Paths.get("Lightning", "Images"));
        searchDirs.add(Paths.get("Lightning", "images"));
        try {
            java.nio.file.Path gameDir = FabricLoader.getInstance().getGameDir();
            searchDirs.add(gameDir.resolve("Lightning").resolve("images"));
            searchDirs.add(gameDir.resolve("Lightning").resolve("Images"));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (Path dir : searchDirs) {
            File[] files;
            File dirFile = dir.toFile();
            if (!dirFile.exists() || !dirFile.isDirectory() || (files = dirFile.listFiles((parent, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp") || lower.endsWith(".webp");
            })) == null) continue;
            for (File file : files) {
                uniqueFiles.putIfAbsent(file.getName(), file);
            }
        }
        ArrayList<File> result = new ArrayList<File>(uniqueFiles.values());
        result.sort((first, second) -> first.getName().compareToIgnoreCase(second.getName()));
        return result;
    }

    public ImageEntry getImageEntry(String imageName) {
        if (imageName == null || imageName.isEmpty() || imageName.equalsIgnoreCase("None")) {
            return null;
        }
        if (this.cache.containsKey(imageName)) {
            ImageEntry cached = this.cache.get(imageName);
            if (cached.file.exists()) {
                return cached;
            }
            cached.close();
            this.cache.remove(imageName);
        }
        File file = ImageManager.resolveImageFile(imageName);
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try {
            com.mojang.blaze3d.platform.NativeImage nativeImage = ImageManager.loadNativeImage(file);
            Identifier identifier = Identifier.fromNamespaceAndPath((String)"lightning", (String)("preview_" + Math.abs(imageName.hashCode()) + "_" + System.currentTimeMillis()));
            DynamicTexture dynamicTexture = new DynamicTexture(() -> "night/" + imageName, nativeImage);
            dynamicTexture.upload();
            Minecraft.getInstance().getTextureManager().register(identifier, (AbstractTexture)dynamicTexture);
            ImageEntry entry = new ImageEntry(imageName, file, identifier, dynamicTexture, nativeImage.getWidth(), nativeImage.getHeight());
            this.cache.put(imageName, entry);
            return entry;
        }
        catch (Throwable throwable) {
            Night.LOGGER.error("Failed to load image: " + imageName, throwable);
            return null;
        }
    }

    public void setCurrentActiveImage(String imageName) {
        this.currentImageName = imageName != null ? imageName : "None";
        if (this.currentImageName.equalsIgnoreCase("None") || this.currentImageName.isEmpty()) {
            this.initDefaultTexture();
            return;
        }
        File file = ImageManager.resolveImageFile(this.currentImageName);
        if (file == null || !file.exists()) {
            this.initDefaultTexture();
            return;
        }
        try {
            NativeImage nativeImage = ImageManager.loadNativeImage(file);
            CustomImageTexture.INSTANCE.update(nativeImage);
            try {
                Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID, (AbstractTexture)CustomImageTexture.INSTANCE);
                Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID_LEGACY, (AbstractTexture)CustomImageTexture.INSTANCE);
            }
            catch (Throwable throwable) {}
        }
        catch (Throwable throwable) {
            Night.LOGGER.error("Failed to set active custom image: " + imageName, throwable);
            this.initDefaultTexture();
        }
    }

    public String getCurrentImageName() {
        return this.currentImageName;
    }

    public static class ImageEntry {
        public final String name;
        public final File file;
        public final Identifier identifier;
        public final DynamicTexture texture;
        public final int width;
        public final int height;

        public ImageEntry(String name, File file, Identifier identifier, DynamicTexture dynamicTexture, int width, int height) {
            this.name = name;
            this.file = file;
            this.identifier = identifier;
            this.texture = dynamicTexture;
            this.width = width;
            this.height = height;
        }

        public void close() {
            try {
                if (this.texture != null) {
                    this.texture.close();
                }
                if (this.identifier != null) {
                    Minecraft.getInstance().getTextureManager().release(this.identifier);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }
}

