/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 */
package lightning.modules.impl.visuals.stashfinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class StashFinderStore {
    private final Set<Long> evaluated = new HashSet<Long>();
    private final List<StashData> stashes = new ArrayList<StashData>();
    private File file;
    private boolean dirty = false;
    private long lastSaveMs = 0L;
    private static final long SAVE_INTERVAL_MS = 10000L;

    public static String currentWorldKey() {
        Minecraft minecraft = Minecraft.getInstance();
        Object object = minecraft.getCurrentServer() != null ? "server_" + minecraft.getCurrentServer().ip : (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null ? "sp_" + minecraft.getSingleplayerServer().getWorldData().getLevelName() : "unknown");
        String string = minecraft.level != null ? minecraft.level.dimension().identifier().toString() : "no_dimension";
        return StashFinderStore.sanitize((String)object) + "__" + StashFinderStore.sanitize(string);
    }

    private static String sanitize(String string) {
        return string.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    public static long chunkKey(int n, int n2) {
        return (long)n & 0xFFFFFFFFL | ((long)n2 & 0xFFFFFFFFL) << 32;
    }

    public void loadForWorld() {
        this.flush();
        this.evaluated.clear();
        this.stashes.clear();
        File file = new File(FabricLoader.getInstance().getGameDir().toFile(), "lightning/stashfinder");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.file = new File(file, StashFinderStore.currentWorldKey() + ".json");
        if (this.file.exists()) {
            try (FileReader fileReader = new FileReader(this.file);){
                Gson gson = new Gson();
                Type type = new TypeToken<PersistentData>(){}.getType();
                PersistentData persistentData = (PersistentData)gson.fromJson((Reader)fileReader, type);
                if (persistentData != null) {
                    if (persistentData.evaluated != null) {
                        this.evaluated.addAll(persistentData.evaluated);
                    }
                    if (persistentData.stashes != null) {
                        this.stashes.addAll(persistentData.stashes);
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private void save() {
        if (this.file == null) {
            return;
        }
        try (FileWriter fileWriter = new FileWriter(this.file);){
            PersistentData persistentData = new PersistentData();
            persistentData.evaluated = this.evaluated;
            persistentData.stashes = this.stashes;
            new GsonBuilder().create().toJson((Object)persistentData, (Appendable)fileWriter);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public boolean isEvaluated(long l) {
        return this.evaluated.contains(l);
    }

    public void markEvaluated(long l) {
        if (this.evaluated.add(l)) {
            this.dirty = true;
        }
    }

    public void recordStash(int n, int n2, String string, Map<String, Integer> map) {
        this.stashes.add(new StashData(n, n2, string, map, System.currentTimeMillis()));
        this.dirty = true;
    }

    public List<StashData> getStashes() {
        return Collections.unmodifiableList(this.stashes);
    }

    public void flushIfStale() {
        if (this.dirty && System.currentTimeMillis() - this.lastSaveMs >= 10000L) {
            this.flush();
        }
    }

    public void flush() {
        if (!this.dirty) {
            return;
        }
        this.save();
        this.dirty = false;
        this.lastSaveMs = System.currentTimeMillis();
    }

    public void clearAll() {
        this.evaluated.clear();
        this.stashes.clear();
        this.dirty = true;
        this.flush();
    }

    private static class PersistentData {
        Set<Long> evaluated = new HashSet<Long>();
        List<StashData> stashes = new ArrayList<StashData>();

        private PersistentData() {
        }
    }

    public record StashData(int x, int z, String dimension, Map<String, Integer> counts, long timestamp) {
    }
}

