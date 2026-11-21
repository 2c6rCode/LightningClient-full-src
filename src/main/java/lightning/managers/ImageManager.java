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
        String string = ImageManager.findSavedActiveImage();
        if (string != null && !string.equalsIgnoreCase("None") && !string.isEmpty()) {
            this.setCurrentActiveImage(string);
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
            Path path;
            JsonObject jsonObject;
            InputStream inputStream;
            String string = "default";
            Path path2 = Paths.get("Lightning", "General.json");
            if (Files.exists(path2, new LinkOption[0])) {
                inputStream = Files.newInputStream(path2, new OpenOption[0]);
                try {
                    jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream)).getAsJsonObject();
                    if (jsonObject.has("Config")) {
                        string = jsonObject.get("Config").getAsString();
                    }
                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
            if (!Files.exists(path = Paths.get("Lightning", "Configs", string + ".json"), new LinkOption[0])) {
                path = Paths.get("Lightning", "Configs", "default.json");
            }
            if (!Files.exists(path, new LinkOption[0])) {
                return "None";
            }
            inputStream = Files.newInputStream(path, new OpenOption[0]);
            try {
                String string2;
                jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream)).getAsJsonObject();
                if (!jsonObject.has("Shaders")) {
                    String string3 = "None";
                    return string3;
                }
                JsonObject jsonObject2 = jsonObject.getAsJsonObject("Shaders");
                if (!jsonObject2.has("Settings")) {
                    String string4 = "None";
                    return string4;
                }
                JsonObject jsonObject3 = jsonObject2.getAsJsonObject("Settings");
                if (!jsonObject3.has("Image")) {
                    String string5 = "None";
                    return string5;
                }
                String string6 = jsonObject3.get("Image").getAsString();
                if (string6 == null) {
                    String string7 = "None";
                    return string7;
                }
                if (string6.isEmpty()) {
                    String string8 = "None";
                    return string8;
                }
                if (string6.equalsIgnoreCase("None")) {
                    String string9 = "None";
                    return string9;
                }
                String string10 = string2 = string6;
                return string10;
            }
            finally {
                if (inputStream != null) {
                    inputStream.close();
                }
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

    public static File resolveImageFile(String string) {
        if (string == null || string.isEmpty() || string.equalsIgnoreCase("None")) {
            return null;
        }
        ArrayList<Path> arrayList = new ArrayList<Path>();
        arrayList.add(IMAGES_DIR);
        arrayList.add(Paths.get("Lightning", "Images"));
        arrayList.add(Paths.get("Lightning", "images"));
        arrayList.add(Paths.get("Lightning", "Images"));
        arrayList.add(Paths.get("lightning", "images"));
        try {
            Path path = FabricLoader.getInstance().getGameDir();
            arrayList.add(path.resolve("Lightning").resolve("images"));
            arrayList.add(path.resolve("Lightning").resolve("Images"));
            arrayList.add(path.resolve("Lightning").resolve("images"));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (Path path : arrayList) {
            File file = path.resolve(string).toFile();
            if (!file.exists() || !file.isFile()) continue;
            return file;
        }
        return IMAGES_DIR.resolve(string).toFile();
    }

    public List<File> getImageFiles() {
        ArrayList<File> arrayList;
        this.ensureDirectoryExists();
        LinkedHashMap<String, File> linkedHashMap = new LinkedHashMap<String, File>();
        ArrayList<Path> arrayList2 = new ArrayList<Path>();
        arrayList2.add(IMAGES_DIR);
        arrayList2.add(Paths.get("Lightning", "Images"));
        arrayList2.add(Paths.get("Lightning", "images"));
        try {
            java.nio.file.Path gameDir = FabricLoader.getInstance().getGameDir();
            arrayList2.add(gameDir.resolve("Lightning").resolve("images"));
            arrayList2.add(gameDir.resolve("Lightning").resolve("Images"));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (Path path : arrayList2) {
            File[] fileArray;
            File file3 = path.toFile();
            if (!file3.exists() || !file3.isDirectory() || (fileArray = file3.listFiles((file, string) -> {
                String string2 = string.toLowerCase();
                return string2.endsWith(".png") || string2.endsWith(".jpg") || string2.endsWith(".jpeg") || string2.endsWith(".bmp") || string2.endsWith(".webp");
            })) == null) continue;
            for (File file4 : fileArray) {
                linkedHashMap.putIfAbsent(file4.getName(), file4);
            }
        }
        arrayList = new ArrayList<File>(linkedHashMap.values());
        arrayList.sort((file, file2) -> file.getName().compareToIgnoreCase(file2.getName()));
        return arrayList;
    }

    public ImageEntry getImageEntry(String string) {
        File file;
        ImageEntry imageEntry;
        if (string == null || string.isEmpty() || string.equalsIgnoreCase("None")) {
            return null;
        }
        if (this.cache.containsKey(string)) {
            imageEntry = this.cache.get(string);
            if (imageEntry.file.exists()) {
                return imageEntry;
            }
            imageEntry.close();
            this.cache.remove(string);
        }
        if ((file = ImageManager.resolveImageFile(string)) == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try {
            com.mojang.blaze3d.platform.NativeImage nativeImage = ImageManager.loadNativeImage(file);
            Identifier identifier = Identifier.fromNamespaceAndPath((String)"lightning", (String)("preview_" + Math.abs(string.hashCode()) + "_" + System.currentTimeMillis()));
            DynamicTexture dynamicTexture = new DynamicTexture(() -> "night/" + string, nativeImage);
            dynamicTexture.upload();
            Minecraft.getInstance().getTextureManager().register(identifier, (AbstractTexture)dynamicTexture);
            ImageEntry imageEntry2 = new ImageEntry(string, file, identifier, dynamicTexture, nativeImage.getWidth(), nativeImage.getHeight());
            this.cache.put(string, imageEntry2);
            return imageEntry2;
        }
        catch (Throwable throwable) {
            Night.LOGGER.error("Failed to load image: " + string, throwable);
            return null;
        }
    }

    public void setCurrentActiveImage(String string) {
        this.currentImageName = string != null ? string : "None";
        String string2 = this.currentImageName;
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
            Night.LOGGER.error("Failed to set active custom image: " + string, throwable);
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

        public ImageEntry(String string, File file, Identifier identifier, DynamicTexture dynamicTexture, int n, int n2) {
            this.name = string;
            this.file = file;
            this.identifier = identifier;
            this.texture = dynamicTexture;
            this.width = n;
            this.height = n2;
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

