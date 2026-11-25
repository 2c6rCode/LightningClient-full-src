/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 */
package lightning.commands.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;
import lightning.modules.impl.core.CapesModule;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.modules.impl.core.ColorModule;
import lightning.modules.impl.core.HUDModule;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.system.FileUtils;

@RegisterCommand(name="config", aliases={"cfg"}, tag="Config", description="Allows you to manage the client's configuration system.", syntax="<load|save|reset> <[name]> | <name> reset | global <[module]> <load|save|reset> <[name]> | bind <load|save|reset> <[name]> | visual <load|save|reset> <[name]> | modules <load|save|reset> <[name]> | <reload|save|current|reset>")
public class ConfigCommand
extends Command {
    private static final List<Class<? extends Module>> EXTRA_VISUAL_MODULES = List.of(ColorModule.class, ClickGuiModule.class, HUDModule.class, CapesModule.class);

    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("load", "save", "reset", "reload", "current", "global", "bind", "visual", "modules");
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("save") || args[0].equalsIgnoreCase("reset")) {
                return this.savedConfigNames();
            }
            if (args[0].equalsIgnoreCase("global")) {
                return Night.MODULE_MANAGER.getModules().stream().map(Module::getName).toList();
            }
            if (args[0].equalsIgnoreCase("bind") || args[0].equalsIgnoreCase("visual") || args[0].equalsIgnoreCase("modules")) {
                return List.of("load", "save", "reset");
            }
            return List.of("reset", "load", "save");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("global")) {
                return List.of("load", "save", "reset");
            }
            if (args[0].equalsIgnoreCase("bind") && (args[1].equalsIgnoreCase("load") || args[1].equalsIgnoreCase("save") || args[1].equalsIgnoreCase("reset"))) {
                return this.bindProfileNames();
            }
            if (args[0].equalsIgnoreCase("visual") && (args[1].equalsIgnoreCase("load") || args[1].equalsIgnoreCase("save") || args[1].equalsIgnoreCase("reset"))) {
                return this.visualConfigNames();
            }
            if (args[0].equalsIgnoreCase("modules") && (args[1].equalsIgnoreCase("load") || args[1].equalsIgnoreCase("save") || args[1].equalsIgnoreCase("reset"))) {
                return this.moduleStateNames();
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("global") && (args[2].equalsIgnoreCase("load") || args[2].equalsIgnoreCase("save") || args[2].equalsIgnoreCase("reset"))) {
            return this.globalConfigNames(args[1]);
        }
        return List.of();
    }

    private List<String> savedConfigNames() {
        File dir = new File("Lightning/Configs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles((parent, name) -> name.endsWith(".json"));
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(fileName -> fileName.substring(0, fileName.length() - ".json".length())).sorted().toList();
    }

    private List<String> globalConfigNames(String moduleName) {
        File dir = new File("Lightning/GlobalConfigs/" + moduleName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles((parent, name) -> name.endsWith(".json"));
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(fileName -> fileName.substring(0, fileName.length() - ".json".length())).sorted().toList();
    }

    private List<String> bindProfileNames() {
        File dir = new File("Lightning/Binds");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles((parent, name) -> name.endsWith(".json"));
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(fileName -> fileName.substring(0, fileName.length() - ".json".length())).sorted().toList();
    }

    private List<String> visualConfigNames() {
        File dir = new File("Lightning/Visuals");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles((parent, name) -> name.endsWith(".json"));
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(fileName -> fileName.substring(0, fileName.length() - ".json".length())).sorted().toList();
    }

    private List<String> moduleStateNames() {
        File dir = new File("Lightning/Modules");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles((parent, name) -> name.endsWith(".json"));
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files).map(File::getName).map(fileName -> fileName.substring(0, fileName.length() - ".json".length())).sorted().toList();
    }

    private List<Module> visualModules() {
        ArrayList<Module> arrayList = new ArrayList<Module>(Night.MODULE_MANAGER.getModules(Module.Category.VISUALS));
        for (Class<? extends Module> clazz : EXTRA_VISUAL_MODULES) {
            Module module = Night.MODULE_MANAGER.getModule(clazz);
            if (module == null) continue;
            arrayList.add(module);
        }
        return arrayList;
    }

    @Override
    public void execute(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("bind")) {
            if (args.length == 3) {
                String action = args[1].toLowerCase();
                String profileName = args[2];
                switch (action) {
                    case "save": {
                        this.saveBindProfile(profileName);
                        break;
                    }
                    case "load": {
                        this.loadBindProfile(profileName);
                        break;
                    }
                    case "reset": {
                        this.resetBindProfile(profileName);
                        break;
                    }
                    default: {
                        Night.CHAT_MANAGER.info("config bind <load|save|reset> <name>");
                        break;
                    }
                }
            } else {
                Night.CHAT_MANAGER.info("config bind <load|save|reset> <name>");
            }
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("visual")) {
            if (args.length == 3) {
                String action = args[1].toLowerCase();
                String configName = args[2];
                switch (action) {
                    case "save": {
                        this.saveVisualConfig(configName);
                        break;
                    }
                    case "load": {
                        this.loadVisualConfig(configName);
                        break;
                    }
                    case "reset": {
                        this.resetVisualConfig(configName);
                        break;
                    }
                    default: {
                        Night.CHAT_MANAGER.info("config visual <load|save|reset> <name>");
                        break;
                    }
                }
            } else {
                Night.CHAT_MANAGER.info("config visual <load|save|reset> <name>");
            }
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("modules")) {
            if (args.length == 3) {
                String action = args[1].toLowerCase();
                String profileName = args[2];
                switch (action) {
                    case "save": {
                        this.saveModulesState(profileName);
                        break;
                    }
                    case "load": {
                        this.loadModulesState(profileName);
                        break;
                    }
                    case "reset": {
                        this.resetModulesState(profileName);
                        break;
                    }
                    default: {
                        Night.CHAT_MANAGER.info("config modules <load|save|reset> <name>");
                        break;
                    }
                }
            } else {
                Night.CHAT_MANAGER.info("config modules <load|save|reset> <name>");
            }
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("global")) {
            if (args.length == 4) {
                Module module = Night.MODULE_MANAGER.getModule(args[1]);
                if (module == null) {
                    Night.CHAT_MANAGER.tagged("Could not find the module specified.", this.getTag(), this.getName());
                    return;
                }
                String action = args[2].toLowerCase();
                String configName = args[3];
                switch (action) {
                    case "save": {
                        this.saveGlobalConfig(module, configName);
                        break;
                    }
                    case "load": {
                        this.loadGlobalConfig(module, configName);
                        break;
                    }
                    case "reset": {
                        this.resetGlobalConfig(module, configName);
                        break;
                    }
                    default: {
                        Night.CHAT_MANAGER.info("config global <module> <load|save|reset> <name>");
                        break;
                    }
                }
            } else {
                Night.CHAT_MANAGER.info("config global <module> <load|save|reset> <name>");
            }
            return;
        }
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("reset")) {
                this.resetConfig(args[0]);
                return;
            }
            switch (args[0].toLowerCase()) {
                case "load": {
                    if (!FileUtils.fileExists("Lightning/Configs/" + args[1] + ".json")) {
                        Night.CHAT_MANAGER.tagged("The specified configuration does not exist.", this.getTag(), this.getName());
                        return;
                    }
                    try {
                        Night.CONFIG_MANAGER.loadModules(args[1]);
                        Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    catch (IOException iOException) {
                        Night.CHAT_MANAGER.tagged("Failed to load the " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    break;
                }
                case "save": {
                    try {
                        Night.CONFIG_MANAGER.saveModules(args[1]);
                        Night.CHAT_MANAGER.tagged("Successfully saved the configuration to " + String.valueOf(ChatUtils.getPrimary()) + args[1] + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    }
                    catch (IOException iOException) {
                        Night.CHAT_MANAGER.tagged("Failed to save the " + String.valueOf(ChatUtils.getPrimary()) + args[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    break;
                }
                case "reset": {
                    this.resetConfig(args[1]);
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload": {
                    Night.CONFIG_MANAGER.loadConfig();
                    Night.CHAT_MANAGER.tagged("Successfully reloaded the current configuration.", this.getTag(), this.getName());
                    break;
                }
                case "save": {
                    Night.CONFIG_MANAGER.saveConfig();
                    Night.CHAT_MANAGER.tagged("Successfully saved the current configuration.", this.getTag(), this.getName());
                    break;
                }
                case "reset": {
                    this.resetConfig(Night.CONFIG_MANAGER.getCurrentConfig());
                    break;
                }
                case "current": {
                    Night.CHAT_MANAGER.tagged("The client is currently using the " + String.valueOf(ChatUtils.getPrimary()) + Night.CONFIG_MANAGER.getCurrentConfig() + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else {
            this.messageSyntax();
        }
    }

    private void resetConfig(String configName) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.setToggled(false);
            module.resetValues();
        }
        try {
            Night.CONFIG_MANAGER.saveModules(configName);
            Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " configuration to default.", this.getTag(), this.getName());
        }
        catch (IOException iOException) {
            Night.CHAT_MANAGER.tagged("Failed to reset the " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
        }
    }

    private void resetGlobalConfig(Module module, String configName) {
        module.resetValues();
        this.saveGlobalConfig(module, configName);
    }

    private void resetBindProfile(String profileName) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.bind.resetValue();
            for (Setting setting : module.getSettings()) {
                if (!(setting instanceof BindSetting)) continue;
                BindSetting bindSetting = (BindSetting)setting;
                bindSetting.resetValue();
            }
        }
        this.saveBindProfile(profileName);
    }

    private void resetVisualConfig(String configName) {
        for (Module module : this.visualModules()) {
            module.resetValues();
        }
        this.saveVisualConfig(configName);
    }

    private void resetModulesState(String profileName) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.setToggled(false);
            module.resetValues();
        }
        this.saveModulesState(profileName);
    }

    private void saveGlobalConfig(Module module, String configName) {
        try {
            JsonObject jsonObject = Night.CONFIG_MANAGER.serializeModuleJson(module);
            File dir = new File("Lightning/GlobalConfigs/" + module.getName());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File outFile = new File(dir, configName + ".json");
            try (FileWriter fileWriter = new FileWriter(outFile);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved global config " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " for module " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to save global config: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void loadGlobalConfig(Module module, String configName) {
        File configFile = new File("Lightning/GlobalConfigs/" + module.getName() + "/" + configName + ".json");
        if (!configFile.exists()) {
            Night.CHAT_MANAGER.tagged("Global config " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " does not exist for " + module.getName() + ".", this.getTag(), this.getName());
            return;
        }
        try (FileReader fileReader = new FileReader(configFile);){
            JsonObject moduleJson = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            File configsDir = new File("Lightning/Configs");
            File[] profileFiles = configsDir.listFiles((parent, name) -> name.endsWith(".json"));
            if (profileFiles != null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                for (File profileFile : profileFiles) {
                    JsonObject profileJson;
                    try (FileReader profileReader = new FileReader(profileFile)) {
                        profileJson = JsonParser.parseReader((Reader)profileReader).getAsJsonObject();
                    }
                    JsonObject modulesObj = profileJson.has("Modules") ? profileJson.getAsJsonObject("Modules") : new JsonObject();
                    modulesObj.add(module.getName(), (JsonElement)moduleJson);
                    profileJson.add("Modules", (JsonElement)modulesObj);
                    try (FileWriter fileWriter = new FileWriter(profileFile)) {
                        gson.toJson((JsonElement)profileJson, (Appendable)fileWriter);
                    }
                }
            }
            Night.CONFIG_MANAGER.loadModules(Night.CONFIG_MANAGER.getCurrentConfig());
            Night.CHAT_MANAGER.tagged("Successfully loaded global config " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " for module " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + " across all profiles.", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to load global config: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void saveBindProfile(String profileName) {
        try {
            JsonObject jsonObject = new JsonObject();
            for (Module mod : Night.MODULE_MANAGER.getModules()) {
                JsonObject modJson = new JsonObject();
                modJson.add(mod.bind.getName(), (JsonElement)new JsonPrimitive(mod.bind.getValue() + "," + mod.bind.getMode()));
                for (Setting setting : mod.getSettings()) {
                    if (setting == mod.bind || !(setting instanceof BindSetting)) continue;
                    BindSetting bindSetting = (BindSetting)setting;
                    modJson.add(bindSetting.getName(), (JsonElement)new JsonPrimitive(bindSetting.getValue() + "," + bindSetting.getMode()));
                }
                jsonObject.add(mod.getName(), (JsonElement)modJson);
            }
            File dir = new File("Lightning/Binds");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File outFile = new File(dir, profileName + ".json");
            try (FileWriter fileWriter = new FileWriter(outFile)) {
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current binds to " + String.valueOf(ChatUtils.getPrimary()) + profileName + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to save bind profile: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void loadBindProfile(String profileName) {
        File file = new File("Lightning/Binds/" + profileName + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Bind profile " + String.valueOf(ChatUtils.getPrimary()) + profileName + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        try (FileReader fileReader = new FileReader(file);){
            JsonObject jsonObject = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            for (String moduleName : jsonObject.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(moduleName);
                if (module == null) continue;
                JsonObject modJson = jsonObject.getAsJsonObject(moduleName);
                for (String settingName : modJson.keySet()) {
                    BindSetting bindSetting = settingName.equals(module.bind.getName()) ? module.bind : this.findBindSetting(module, settingName);
                    if (bindSetting == null) continue;
                    String[] parts = modJson.get(settingName).getAsString().split(",", 2);
                    bindSetting.setValue(Integer.parseInt(parts[0]));
                    if (parts.length <= 1) continue;
                    bindSetting.setMode(parts[1]);
                }
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + profileName + String.valueOf(ChatUtils.getSecondary()) + " bind profile.", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to load bind profile: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private BindSetting findBindSetting(Module module, String settingName) {
        for (Setting setting : module.getSettings()) {
            BindSetting bindSetting;
            if (!(setting instanceof BindSetting) || !(bindSetting = (BindSetting)setting).getName().equals(settingName)) continue;
            return bindSetting;
        }
        return null;
    }

    private void saveVisualConfig(String configName) {
        try {
            JsonObject jsonObject = new JsonObject();
            for (Module module : this.visualModules()) {
                jsonObject.add(module.getName(), (JsonElement)Night.CONFIG_MANAGER.serializeModuleJson(module));
            }
            File dir = new File("Lightning/Visuals");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File outFile = new File(dir, configName + ".json");
            try (FileWriter fileWriter = new FileWriter(outFile);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current visual settings to " + String.valueOf(ChatUtils.getPrimary()) + configName + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to save visual config: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void loadVisualConfig(String configName) {
        File file = new File("Lightning/Visuals/" + configName + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Visual config " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        Set<String> visualModuleNames = this.visualModules().stream().map(Module::getName).collect(Collectors.toSet());
        try (FileReader fileReader = new FileReader(file);){
            JsonObject jsonObject = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            for (String moduleName : jsonObject.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(moduleName);
                if (module == null || !visualModuleNames.contains(module.getName())) continue;
                Night.CONFIG_MANAGER.applyModuleJson(module, jsonObject.getAsJsonObject(moduleName));
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + configName + String.valueOf(ChatUtils.getSecondary()) + " visual config.", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to load visual config: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void saveModulesState(String profileName) {
        try {
            JsonObject jsonObject = new JsonObject();
            for (Module module : Night.MODULE_MANAGER.getModules()) {
                jsonObject.add(module.getName(), (JsonElement)Night.CONFIG_MANAGER.serializeModuleJson(module, false));
            }
            File dir = new File("Lightning/Modules");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File outFile = new File(dir, profileName + ".json");
            try (FileWriter fileWriter = new FileWriter(outFile);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current module states to " + String.valueOf(ChatUtils.getPrimary()) + profileName + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to save module states: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void loadModulesState(String profileName) {
        File file = new File("Lightning/Modules/" + profileName + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Module state profile " + String.valueOf(ChatUtils.getPrimary()) + profileName + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        try (FileReader fileReader = new FileReader(file);){
            JsonObject jsonObject = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            for (String moduleName : jsonObject.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(moduleName);
                if (module == null) continue;
                JsonElement jsonElement = jsonObject.get(moduleName);
                if (jsonElement.isJsonObject()) {
                    Night.CONFIG_MANAGER.applyModuleJson(module, jsonElement.getAsJsonObject(), false);
                    continue;
                }
                if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isBoolean()) continue;
                module.setToggled(jsonElement.getAsBoolean());
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + profileName + String.valueOf(ChatUtils.getSecondary()) + " module states.", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to load module states: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }
}

