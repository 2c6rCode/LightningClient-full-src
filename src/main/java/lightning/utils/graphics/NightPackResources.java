/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.AbstractPackResources
 *  net.minecraft.server.packs.PackLocationInfo
 *  net.minecraft.server.packs.PackResources$ResourceOutput
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.repository.PackSource
 *  net.minecraft.server.packs.resources.IoSupplier
 *  org.jspecify.annotations.Nullable
 */
package lightning.utils.graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipInputStream;
import lightning.Night;
import lightning.managers.ImageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

public class NightPackResources
extends AbstractPackResources {
    public static final NightPackResources INSTANCE = new NightPackResources();
    private static final String NAMESPACE = "lightning";
    private static final Set<String> NAMESPACES = Set.of("lightning");
    private static final List<String> FALLBACK_ASSETS = List.of("icon.png", "post_effect/clickgui_bg.json", "post_effect/glow_blur.json", "post_effect/menu_bg.json", "post_effect/neekeri_ui.json", "post_effect/outline_bloom.json", "post_effect/outline_both_100.json", "post_effect/outline_both_20.json", "post_effect/outline_both_40.json", "post_effect/outline_both_60.json", "post_effect/outline_both_80.json", "post_effect/outline_fill_100.json", "post_effect/outline_fill_20.json", "post_effect/outline_fill_40.json", "post_effect/outline_fill_60.json", "post_effect/outline_fill_80.json", "post_effect/outline_glow.json", "post_effect/outline_outline.json", "post_effect/star_glow.json", "shaders/core/esp.fsh", "shaders/core/killeffect_meme.fsh", "shaders/core/menu_shader.fsh", "shaders/core/sky.fsh", "shaders/core/text_glow.fsh", "shaders/program/clickgui_bg.fsh", "shaders/program/glow_blur.fsh", "shaders/program/menu_bg.fsh", "shaders/program/neekeri_ui.fsh", "shaders/program/outline.fsh", "shaders/program/outline_bloom.fsh", "shaders/program/outline_glow.fsh", "shaders/program/outline_glow_blur.fsh", "shaders/program/sky/screen_blit.vsh", "shaders/program/sky/sky_shader.fsh", "shaders/program/sky/sky_shader.json", "shaders/program/sky/sky_shader.vsh", "shaders/program/smoke/smoke.fsh", "shaders/program/smoke/smoke.json", "shaders/program/smoke/smoke.vsh", "shaders/program/star_glow.fsh", "shaders/program/star_glow_blur.fsh", "sounds.json", "sounds/vine.ogg", "splash.txt", "textures/cape.png", "textures/chorus.png", "textures/crystal.png", "textures/custom_image.png", "textures/effect/custom_image.png", "textures/effect/meme_arrow.png", "textures/effect/meme_circle.png", "textures/gear.png", "textures/glint.png", "textures/gui/logo.png", "textures/gui/logo_mask.png", "textures/gui/moonlight_bg.png", "textures/pearl.png");

    public NightPackResources() {
        super(new PackLocationInfo(NAMESPACE, (Component)Component.literal((String)"Lightning Resources"), PackSource.BUILT_IN, Optional.empty()));
    }

    public @Nullable IoSupplier<InputStream> getRootResource(String ... stringArray) {
        String string = String.join((CharSequence)"/", stringArray);
        if (NightPackResources.class.getClassLoader().getResource(string) != null) {
            return () -> NightPackResources.class.getClassLoader().getResourceAsStream(string);
        }
        return null;
    }

    public @Nullable IoSupplier<InputStream> getResource(PackType packType, Identifier identifier) {
        File file;
        String string;
        if (packType != PackType.CLIENT_RESOURCES || !NAMESPACE.equals(identifier.getNamespace())) {
            return null;
        }
        if ((identifier.getPath().equals("textures/effect/custom_image.png") || identifier.getPath().equals("textures/custom_image.png")) && Night.IMAGE_MANAGER != null && (string = Night.IMAGE_MANAGER.getCurrentImageName()) != null && !string.isEmpty() && !string.equalsIgnoreCase("None") && (file = ImageManager.resolveImageFile(string)) != null && file.exists() && file.isFile()) {
            return () -> new FileInputStream(file);
        }
        String string2 = "assets/lightning/" + identifier.getPath();
        if (NightPackResources.class.getClassLoader().getResource(string2) != null) {
            return () -> NightPackResources.class.getClassLoader().getResourceAsStream(string2);
        }
        return null;
    }

    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        Object object;
        InputStream inputStream;
        String string3;
        HashSet<String> hashSet;
        block16: {
            if (packType != PackType.CLIENT_RESOURCES || !NAMESPACE.equals(string)) {
                return;
            }
            hashSet = new HashSet<String>();
            string3 = string2.isEmpty() ? "" : (string2.endsWith("/") ? string2 : string2 + "/");
            try {
                URL uRL = NightPackResources.class.getProtectionDomain().getCodeSource().getLocation();
                if (uRL == null) break block16;
                String object2 = "assets/lightning/" + string3;
                inputStream = uRL.openStream();
                try {
                    object = new JarInputStream(inputStream);
                    try {
                        JarEntry jarEntry;
                        while ((jarEntry = ((JarInputStream)object).getNextJarEntry()) != null) {
                            Identifier identifier;
                            String string4;
                            String string5 = jarEntry.getName();
                            if (jarEntry.isDirectory() || !string5.startsWith(object2) || !hashSet.add(string4 = string5.substring("assets/lightning/".length())) || (identifier = Identifier.tryBuild((String)NAMESPACE, (String)string4)) == null) continue;
                            resourceOutput.accept(identifier, () -> NightPackResources.class.getClassLoader().getResourceAsStream(string5));
                        }
                    }
                    finally {
                        ((ZipInputStream)object).close();
                    }
                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        for (String fallbackPath : FALLBACK_ASSETS) {
            if (!fallbackPath.startsWith(string3) || !hashSet.add(fallbackPath)) continue;
            final String resourcePath = "assets/lightning/" + fallbackPath;
            Identifier fallbackId;
            if (NightPackResources.class.getClassLoader().getResource(resourcePath) == null || (fallbackId = Identifier.tryBuild(NAMESPACE, fallbackPath)) == null) continue;
            resourceOutput.accept(fallbackId, () -> NightPackResources.class.getClassLoader().getResourceAsStream(resourcePath));
        }
    }

    public Set<String> getNamespaces(PackType packType) {
        return packType == PackType.CLIENT_RESOURCES ? NAMESPACES : Set.of();
    }

    public void close() {
    }
}

