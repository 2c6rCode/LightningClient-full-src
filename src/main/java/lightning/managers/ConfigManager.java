/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.phys.Vec3
 */
package lightning.managers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.runtime.SwitchBootstraps;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Stream;
import lightning.Night;
import net.minecraft.world.level.block.Block;
import lightning.managers.WaypointManager;
import lightning.modules.Module;
import lightning.modules.impl.core.PingBypassModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ImageSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.PositionSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.minecraft.IdentifierUtils;
import lightning.utils.system.FileUtils;
import lombok.Generated;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

public class ConfigManager {
    private String currentConfig = "default";
    private boolean loadingConfig = false;

    public ConfigManager() {
        this.loadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveConfig));
    }

    public void loadConfig() {
        block2: {
            try {
                this.migrateOldConfig();
                FileUtils.createDirectory("Lightning");
                FileUtils.createDirectory("Lightning/Configs");
                FileUtils.createDirectory("Lightning/Client");
                FileUtils.createDirectory("Lightning/Modules");
                FileUtils.createDirectory("Lightning/Binds");
                FileUtils.createDirectory("Lightning/Visuals");
                FileUtils.createDirectory("Lightning/GlobalConfigs");
                this.loadGeneral();
                this.loadWaypoints();
                this.loadModules(this.currentConfig);
            }
            catch (Exception exception) {
                Night.LOGGER.error("Failed to load the client's configuration!", (Throwable)exception);
                if (Night.CHAT_MANAGER == null) break block2;
                Night.CHAT_MANAGER.await("The configuration has not been loaded properly. Read the stacktrace for more information.");
            }
        }
    }

    private void migrateOldConfig() {
        try {
            Path path = Paths.get("EUClient", new String[0]);
            Path path2 = Paths.get("Lightning", new String[0]);
            if (!(!Files.exists(path, new LinkOption[0]) || Files.exists(path2, new LinkOption[0]) && Files.exists(path2.resolve("Configs"), new LinkOption[0]))) {
                Night.LOGGER.info("Migrating configuration from EUClient to Lightning...");
                ConfigManager.copyDirectory(path, path2);
            }
        }
        catch (Exception exception) {
            Night.LOGGER.warn("Failed to migrate old EUClient configuration: {}", (Object)exception.getMessage());
        }
    }

    private static void copyDirectory(Path path, Path path2) throws IOException {
        try (Stream<Path> stream = Files.walk(path, new FileVisitOption[0]);){
            stream.forEach(path3 -> {
                try {
                    Path path4 = path2.resolve(path.relativize((Path)path3));
                    if (Files.isDirectory(path3, new LinkOption[0])) {
                        if (!Files.exists(path4, new LinkOption[0])) {
                            Files.createDirectories(path4, new FileAttribute[0]);
                        }
                    } else if (!Files.exists(path4, new LinkOption[0])) {
                        if (path4.getParent() != null && !Files.exists(path4.getParent(), new LinkOption[0])) {
                            Files.createDirectories(path4.getParent(), new FileAttribute[0]);
                        }
                        Files.copy(path3, path4, StandardCopyOption.COPY_ATTRIBUTES);
                    }
                }
                catch (IOException iOException) {
                    Night.LOGGER.warn("Could not copy config file {}: {}", path3, (Object)iOException.getMessage());
                }
            });
        }
    }

    public void saveConfig() {
        try {
            FileUtils.createDirectory("Lightning");
            FileUtils.createDirectory("Lightning/Configs");
            FileUtils.createDirectory("Lightning/Client");
            FileUtils.createDirectory("Lightning/Modules");
            FileUtils.createDirectory("Lightning/Binds");
            FileUtils.createDirectory("Lightning/Visuals");
            FileUtils.createDirectory("Lightning/GlobalConfigs");
            this.saveGeneral();
            this.saveWaypoints();
            this.saveModules(this.currentConfig);
        }
        catch (IOException iOException) {
            Night.LOGGER.error("Failed to save the client's configuration!", (Throwable)iOException);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadWaypoints() throws IOException {
        if (!FileUtils.fileExists("Lightning/Waypoints.json")) {
            return;
        }
        InputStream inputStream = Files.newInputStream(Paths.get("Lightning/Waypoints.json", new String[0]), new OpenOption[0]);
        try {
            JsonObject jsonObject;
            try {
                jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream)).getAsJsonObject();
            }
            catch (IllegalStateException illegalStateException) {
                Night.LOGGER.error("Failed to load the client's Waypoint configuration!", (Throwable)illegalStateException);
                Night.CHAT_MANAGER.await("The Waypoint configuration has not been loaded properly. Read the stacktrace for more information.");
                if (Collections.singletonList(inputStream).get(0) != null) {
                    inputStream.close();
                }
                if (Collections.singletonList(inputStream).get(0) != null) {
                    inputStream.close();
                }
                return;
            }
            if (jsonObject.has("Waypoints")) {
                for (JsonElement jsonElement : jsonObject.get("Waypoints").getAsJsonArray()) {
                    String[] stringArray = jsonElement.getAsString().split(":");
                    if (Night.WAYPOINT_MANAGER.contains(stringArray[0])) continue;
                    Night.WAYPOINT_MANAGER.add(stringArray[0], new Vec3((double)Integer.parseInt(stringArray[1]), (double)Integer.parseInt(stringArray[2]), (double)Integer.parseInt(stringArray[3])), stringArray[4], stringArray[5]);
                }
            }
        }
        finally {
            if (Collections.singletonList(inputStream).get(0) != null) {
                inputStream.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadGeneral() throws IOException {
        if (!FileUtils.fileExists("Lightning/General.json")) {
            return;
        }
        InputStream inputStream = Files.newInputStream(Paths.get("Lightning/General.json", new String[0]), new OpenOption[0]);
        try {
            JsonObject jsonObject;
            JsonElement jsonElement = JsonParser.parseReader((Reader)new InputStreamReader(inputStream));
            if (jsonElement == null || !jsonElement.isJsonObject()) {
                Night.LOGGER.warn("General.json is empty or invalid JSON.");
                return;
            }
            try {
                jsonObject = jsonElement.getAsJsonObject();
            }
            catch (Exception exception) {
                Night.LOGGER.error("Failed to load the client's General configuration!", (Throwable)exception);
                if (Night.CHAT_MANAGER != null) {
                    Night.CHAT_MANAGER.await("The General configuration has not been loaded properly. Read the stacktrace for more information.");
                }
                if (Collections.singletonList(inputStream).get(0) != null) {
                    inputStream.close();
                }
                return;
            }
            if (jsonObject.has("Config")) {
                this.currentConfig = jsonObject.get("Config").getAsString();
            }
            if (jsonObject.has("Prefix") && Night.COMMAND_MANAGER != null) {
                Night.COMMAND_MANAGER.setPrefix(jsonObject.get("Prefix").getAsString());
            }
            if (jsonObject.has("Friends") && Night.FRIEND_MANAGER != null) {
                for (JsonElement jsonElement2 : jsonObject.get("Friends").getAsJsonArray()) {
                    if (Night.FRIEND_MANAGER.contains(jsonElement2.getAsString())) continue;
                    Night.FRIEND_MANAGER.add(jsonElement2.getAsString());
                }
            }
            if (jsonObject.has("Macros") && Night.MACRO_MANAGER != null) {
                for (JsonElement macroElement : jsonObject.get("Macros").getAsJsonArray()) {
                    try {
                        String[] parts = macroElement.getAsString().split(":", 2);
                        if (parts.length <= 1) continue;
                        int keyCode = Integer.parseInt(parts[0]);
                        String macroCommand = parts[1];
                        Night.MACRO_MANAGER.add(macroCommand, keyCode);
                    }
                    catch (NumberFormatException numberFormatException) {
                        Night.LOGGER.error("Failed to load the " + macroElement.getAsString() + " macro!", (Throwable)numberFormatException);
                        if (Night.CHAT_MANAGER == null) continue;
                        Night.CHAT_MANAGER.await("The " + macroElement.getAsString() + " macro has failed to load. Read the stacktrace for more information.");
                    }
                }
            }
        }
        finally {
            if (Collections.singletonList(inputStream).get(0) != null) {
                inputStream.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveGeneral() throws IOException {
        FileUtils.resetFile("Lightning/General.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("Config", (JsonElement)new JsonPrimitive(this.currentConfig));
        jsonObject.add("Prefix", (JsonElement)new JsonPrimitive(Night.COMMAND_MANAGER.getPrefix()));
        JsonArray friendsArray = new JsonArray();
        Night.FRIEND_MANAGER.getFriends().forEach(friend -> friendsArray.add(friend));
        jsonObject.add("Friends", (JsonElement)friendsArray);
        JsonArray macrosArray = new JsonArray();
        Night.MACRO_MANAGER.getMacros().forEach((macroText, keyCode) -> macrosArray.add(keyCode + ":" + macroText));
        jsonObject.add("Macros", (JsonElement)macrosArray);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((OutputStream)new FileOutputStream("Lightning/General.json"), StandardCharsets.UTF_8);
        try {
            outputStreamWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString((String)jsonObject.toString())));
        }
        finally {
            if (Collections.singletonList(outputStreamWriter).get(0) != null) {
                outputStreamWriter.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveWaypoints() throws IOException {
        FileUtils.resetFile("Lightning/Waypoints.json");
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (WaypointManager.Waypoint waypoint : Night.WAYPOINT_MANAGER.getWaypoints()) {
            jsonArray.add(waypoint.getName() + ":" + (int)waypoint.getPos().x + ":" + (int)waypoint.getPos().y + ":" + (int)waypoint.getPos().z + ":" + waypoint.getDimension() + ":" + waypoint.getServer());
        }
        jsonObject.add("Waypoints", (JsonElement)jsonArray);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((OutputStream)new FileOutputStream("Lightning/Waypoints.json"), StandardCharsets.UTF_8);
        try {
            outputStreamWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString((String)jsonObject.toString())));
        }
        finally {
            if (Collections.singletonList(outputStreamWriter).get(0) != null) {
                outputStreamWriter.close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadModules(String configName) throws IOException {
        if (!FileUtils.fileExists("Lightning/Configs/" + configName + ".json")) {
            return;
        }
        InputStream inputStream = Files.newInputStream(Paths.get("Lightning/Configs/" + configName + ".json", new String[0]), new OpenOption[0]);
        try {
            PingBypassModule pingBypassModule;
            JsonObject jsonObject;
            JsonElement jsonElement = JsonParser.parseReader((Reader)new InputStreamReader(inputStream));
            if (jsonElement == null || !jsonElement.isJsonObject()) {
                Night.LOGGER.warn("Module configuration is empty or invalid JSON: {}", (Object)configName);
                return;
            }
            try {
                jsonObject = jsonElement.getAsJsonObject();
            }
            catch (Exception exception) {
                Night.LOGGER.error("Failed to load the client's Module configuration!", (Throwable)exception);
                if (Night.CHAT_MANAGER != null) {
                    Night.CHAT_MANAGER.await("The configuration for the Modules has not been loaded properly. Read the stacktrace for more information.");
                }
                if (Collections.singletonList(inputStream).get(0) != null) {
                    inputStream.close();
                }
                return;
            }
            if (!jsonObject.has("Modules")) {
                return;
            }
            JsonObject modulesJson = jsonObject.get("Modules").getAsJsonObject();
            this.loadingConfig = true;
            try {
                this.loadModulesInner(modulesJson);
            }
            finally {
                this.loadingConfig = false;
            }
            this.currentConfig = configName;
            try {
                this.saveGeneral();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && !Night.PINGBYPASS_CONFIG.isServer() && (pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class)) != null && pingBypassModule.isToggled()) {
                pingBypassModule.syncAllSettingsToProxy();
            }
        }
        finally {
            if (Collections.singletonList(inputStream).get(0) != null) {
                inputStream.close();
            }
        }
    }

    private void loadModulesInner(JsonObject jsonObject) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            if (!jsonObject.has(module.getName())) {
                module.setToggled(false);
                module.resetValues();
                continue;
            }
            this.applyModuleJson(module, jsonObject.get(module.getName()).getAsJsonObject());
        }
    }

    public void applyModuleJson(Module module, JsonObject jsonObject) {
        this.applyModuleJson(module, jsonObject, true);
    }

    public void applyModuleJson(Module module, JsonObject jsonObject, boolean includeBinds) {
        boolean enabled = jsonObject.has("Status") && jsonObject.get("Status").getAsBoolean();
        if (!jsonObject.has("Settings")) {
            if (includeBinds) {
                module.resetValues();
            }
            module.setToggled(enabled, false);
        } else {
            JsonObject settingsJson = jsonObject.get("Settings").getAsJsonObject();
            for (Setting setting : module.getSettings()) {
                if (!includeBinds && setting instanceof BindSetting) continue;
                JsonElement jsonElement = settingsJson.get(setting.getName());
                if (jsonElement != null && jsonElement.isJsonPrimitive()) {
                    if (setting instanceof BooleanSetting booleanSetting) {
                        if (jsonElement.getAsJsonPrimitive().isBoolean()) {
                            booleanSetting.setValue(jsonElement.getAsBoolean());
                        } else {
                            booleanSetting.resetValue();
                        }
                    } else if (setting instanceof NumberSetting numberSetting) {
                        if (jsonElement.getAsJsonPrimitive().isNumber()) {
                            numberSetting.setValue(jsonElement.getAsNumber());
                        } else {
                            numberSetting.resetValue();
                        }
                    } else if (setting instanceof ModeSetting modeSetting) {
                        modeSetting.setValue(jsonElement.getAsString());
                    } else if (setting instanceof StringSetting stringSetting) {
                        String strVal = jsonElement.getAsString();
                        if (strVal != null && strVal.equalsIgnoreCase("EUClient")) {
                            strVal = "Lightning";
                        }
                        stringSetting.setValue(strVal);
                    } else if (setting instanceof ImageSetting imageSetting) {
                        imageSetting.setValue(jsonElement.getAsString());
                        if (Night.IMAGE_MANAGER != null) {
                            Night.IMAGE_MANAGER.setCurrentActiveImage(jsonElement.getAsString());
                        }
                    } else if (setting instanceof BindSetting bindSetting) {
                        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
                            bindSetting.setValue(jsonElement.getAsInt());
                        } else {
                            String[] parts = jsonElement.getAsString().split(",", 2);
                            bindSetting.setValue(Integer.parseInt(parts[0]));
                            if (parts.length > 1) {
                                bindSetting.setMode(parts[1]);
                            }
                        }
                    } else if (setting instanceof ColorSetting colorSetting) {
                        String[] parts = jsonElement.getAsString().split(",");
                        if (parts.length == 6) {
                            colorSetting.setColor(new Color(Math.clamp((long)Integer.parseInt(parts[0]), 0, 255), Math.clamp((long)Integer.parseInt(parts[1]), 0, 255), Math.clamp((long)Integer.parseInt(parts[2]), 0, 255), Math.clamp((long)Integer.parseInt(parts[3]), 0, 255)));
                            colorSetting.setSync(Boolean.parseBoolean(parts[4]));
                            colorSetting.setRainbow(Boolean.parseBoolean(parts[5]));
                        }
                    } else if (setting instanceof PositionSetting positionSetting) {
                        String[] parts = jsonElement.getAsString().split(",");
                        if (parts.length == 2) {
                            positionSetting.set(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
                        }
                    } else if (setting instanceof WhitelistSetting whitelistSetting) {
                        whitelistSetting.clear();
                        String[] parts = jsonElement.getAsString().split(",");
                        if (parts.length > 0) {
                            for (String str : parts) {
                                if (whitelistSetting.getType() == WhitelistSetting.Type.ITEMS) {
                                    Item item = IdentifierUtils.getItem(str);
                                    if (item != null) whitelistSetting.add(item);
                                } else if (whitelistSetting.getType() == WhitelistSetting.Type.BLOCKS) {
                                    Block block = IdentifierUtils.getBlock(str);
                                    if (block != null) whitelistSetting.add(block);
                                }
                            }
                        }
                    }
                    continue;
                }
                if (setting instanceof BooleanSetting booleanSetting) {
                    booleanSetting.resetValue();
                } else if (setting instanceof NumberSetting numberSetting) {
                    numberSetting.resetValue();
                } else if (setting instanceof ModeSetting modeSetting) {
                    modeSetting.resetValue();
                } else if (setting instanceof StringSetting stringSetting) {
                    stringSetting.resetValue();
                } else if (setting instanceof ImageSetting imageSetting) {
                    imageSetting.resetValue();
                } else if (setting instanceof BindSetting bindSetting) {
                    bindSetting.resetValue();
                } else if (setting instanceof ColorSetting colorSetting) {
                    colorSetting.resetValue();
                } else if (setting instanceof WhitelistSetting whitelistSetting) {
                    whitelistSetting.clear();
                } else if (setting instanceof PositionSetting positionSetting) {
                    positionSetting.resetValue();
                }
            }
            module.setToggled(enabled, false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveModules(String configName) throws IOException {
        FileUtils.resetFile("Lightning/Configs/" + configName + ".json");
        JsonObject rootJson = new JsonObject();
        rootJson.add("Config", (JsonElement)new JsonPrimitive(configName));
        JsonObject modulesJson = new JsonObject();
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            JsonObject moduleJson = this.serializeModuleJson(module);
            if (module instanceof FakePlayerModule) {
                moduleJson.add("Status", (JsonElement)new JsonPrimitive(Boolean.valueOf(false)));
            }
            modulesJson.add(module.getName(), (JsonElement)moduleJson);
        }
        rootJson.add("Modules", (JsonElement)modulesJson);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter((OutputStream)new FileOutputStream("Lightning/Configs/" + configName + ".json"), StandardCharsets.UTF_8);
        try {
            outputStreamWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString((String)rootJson.toString())));
            this.currentConfig = configName;
            try {
                this.saveGeneral();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        finally {
            if (Collections.singletonList(outputStreamWriter).get(0) != null) {
                outputStreamWriter.close();
            }
        }
    }

    public JsonObject serializeModuleJson(Module module) {
        return this.serializeModuleJson(module, true);
    }

    public JsonObject serializeModuleJson(Module module, boolean includeBinds) {
        JsonObject moduleJson = new JsonObject();
        moduleJson.add("Status", (JsonElement)new JsonPrimitive(Boolean.valueOf(module.isToggled())));
        JsonObject settingsJson = new JsonObject();
        for (Setting setting : module.getSettings()) {
            if (!includeBinds && setting instanceof BindSetting) continue;
            if (setting instanceof BooleanSetting booleanSetting) {
                settingsJson.add(booleanSetting.getName(), (JsonElement)new JsonPrimitive(Boolean.valueOf(booleanSetting.getValue())));
            } else if (setting instanceof NumberSetting numberSetting) {
                settingsJson.add(numberSetting.getName(), (JsonElement)new JsonPrimitive(numberSetting.getValue()));
            } else if (setting instanceof ModeSetting modeSetting) {
                settingsJson.add(modeSetting.getName(), (JsonElement)new JsonPrimitive(modeSetting.getValue()));
            } else if (setting instanceof StringSetting stringSetting) {
                settingsJson.add(stringSetting.getName(), (JsonElement)new JsonPrimitive(stringSetting.getValue()));
            } else if (setting instanceof ImageSetting imageSetting) {
                settingsJson.add(imageSetting.getName(), (JsonElement)new JsonPrimitive(imageSetting.getValue()));
            } else if (setting instanceof BindSetting bindSetting) {
                settingsJson.add(bindSetting.getName(), (JsonElement)new JsonPrimitive(bindSetting.getValue() + "," + bindSetting.getMode()));
            } else if (setting instanceof ColorSetting colorSetting) {
                settingsJson.add(colorSetting.getName(), (JsonElement)new JsonPrimitive(colorSetting.getValue().getColor().getRed() + "," + colorSetting.getValue().getColor().getGreen() + "," + colorSetting.getValue().getColor().getBlue() + "," + colorSetting.getValue().getColor().getAlpha() + "," + colorSetting.isSync() + "," + colorSetting.isRainbow()));
            } else if (setting instanceof WhitelistSetting whitelistSetting) {
                StringJoiner joiner = new StringJoiner(",");
                for (String entryId : whitelistSetting.getWhitelistIds()) {
                    joiner.add(entryId);
                }
                settingsJson.add(whitelistSetting.getName(), (JsonElement)new JsonPrimitive(joiner.toString()));
            } else if (setting instanceof PositionSetting positionSetting) {
                settingsJson.add(positionSetting.getName(), (JsonElement)new JsonPrimitive(positionSetting.getX() + "," + positionSetting.getY()));
            }
        }
        moduleJson.add("Settings", (JsonElement)settingsJson);
        return moduleJson;
    }

    @Generated
    public String getCurrentConfig() {
        return this.currentConfig;
    }

    @Generated
    public boolean isLoadingConfig() {
        return this.loadingConfig;
    }

    @Generated
    public void setCurrentConfig(String currentConfig) {
        this.currentConfig = currentConfig;
    }

    @Generated
    public void setLoadingConfig(boolean loadingConfig) {
        this.loadingConfig = loadingConfig;
    }
}

