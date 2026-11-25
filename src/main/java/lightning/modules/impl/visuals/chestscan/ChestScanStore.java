/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 */
package lightning.modules.impl.visuals.chestscan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ChestScanStore {
    private final Map<BlockPos, ChestStatus> records = new HashMap<BlockPos, ChestStatus>();
    private File file;

    public static String currentWorldKey() {
        Minecraft minecraft = Minecraft.getInstance();
        String worldPart = minecraft.getCurrentServer() != null ? "server_" + minecraft.getCurrentServer().ip : (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null ? "sp_" + minecraft.getSingleplayerServer().getWorldData().getLevelName() : "unknown");
        String dimension = minecraft.level != null ? minecraft.level.dimension().identifier().toString() : "no_dimension";
        return ChestScanStore.sanitize(worldPart) + "__" + ChestScanStore.sanitize(dimension);
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    public void loadForWorld() {
        this.records.clear();
        File file = new File(FabricLoader.getInstance().getGameDir().toFile(), "lightning/chestscan");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.file = new File(file, ChestScanStore.currentWorldKey() + ".json");
        if (this.file.exists()) {
            try (FileReader fileReader = new FileReader(this.file);){
                Gson gson = new Gson();
                Type type = new TypeToken<List<Entry>>(){}.getType();
                List<Entry> entries = gson.fromJson(fileReader, type);
                if (entries != null) {
                    for (Entry entry : entries) {
                        try {
                            this.records.put(new BlockPos(entry.x, entry.y, entry.z), ChestStatus.valueOf(entry.status));
                        }
                        catch (Exception exception) {}
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
            ArrayList<Entry> entries = new ArrayList<Entry>();
            for (Map.Entry<BlockPos, ChestStatus> entry : this.records.entrySet()) {
                Entry dto = new Entry();
                dto.x = entry.getKey().getX();
                dto.y = entry.getKey().getY();
                dto.z = entry.getKey().getZ();
                dto.status = entry.getValue().name();
                entries.add(dto);
            }
            new GsonBuilder().setPrettyPrinting().create().toJson(entries, (Appendable)fileWriter);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void put(BlockPos blockPos, ChestStatus chestStatus) {
        this.records.put(blockPos.immutable(), chestStatus);
        this.save();
    }

    public ChestStatus get(BlockPos blockPos) {
        return this.records.get(blockPos);
    }

    public void remove(BlockPos blockPos) {
        if (this.records.remove(blockPos) != null) {
            this.save();
        }
    }

    public Set<BlockPos> positions() {
        return this.records.keySet();
    }

    private static class Entry {
        int x;
        int y;
        int z;
        String status;

        private Entry() {
        }
    }

    public static enum ChestStatus {
        EMPTY,
        PARTIAL,
        FULL;

    }
}

