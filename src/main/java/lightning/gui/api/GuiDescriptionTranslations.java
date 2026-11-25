/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package lightning.gui.api;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lightning.Night;

public final class GuiDescriptionTranslations {
    private GuiDescriptionTranslations() {
    }

    public static String translate(String englishText) {
        return Holder.VALUES.getOrDefault(englishText, "");
    }

    private static Map<String, String> load() {
        try (InputStream inputStream = GuiDescriptionTranslations.class.getResourceAsStream("/assets/lightning/gui/module-descriptions-vi.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing Vietnamese description resource");
            }
            Entry[] entryArray = (Entry[])new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), Entry[].class);
            if (entryArray == null) {
                throw new IllegalStateException("Empty Vietnamese description resource");
            }
            HashMap<String, String> translations = new HashMap<String, String>();
            for (Entry entry : entryArray) {
                if (entry == null || entry.english == null || entry.vi == null || entry.vi.isBlank()) {
                    throw new IllegalStateException("Invalid Vietnamese description entry");
                }
                String existing = translations.putIfAbsent(entry.english, entry.vi);
                if (existing == null || existing.equals(entry.vi)) continue;
                throw new IllegalStateException("Conflicting Vietnamese descriptions");
            }
            return Map.copyOf(translations);
        } catch (Exception exception) {
            Night.LOGGER.warn("Could not load Vietnamese GUI descriptions", (Throwable)exception);
            return Map.of();
        }
    }

    private static final class Holder {
        private static final Map<String, String> VALUES = GuiDescriptionTranslations.load();

        private Holder() {
        }
    }

    private record Entry(String module, String english, String vi) {
    }
}

