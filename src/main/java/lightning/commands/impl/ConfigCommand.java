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
    public List<String> getSuggestions(String[] stringArray) {
        if (stringArray.length == 0) {
            return List.of("load", "save", "reset", "reload", "current", "global", "bind", "visual", "modules");
        }
        if (stringArray.length == 1) {
            if (stringArray[0].equalsIgnoreCase("load") || stringArray[0].equalsIgnoreCase("save") || stringArray[0].equalsIgnoreCase("reset")) {
                return this.savedConfigNames();
            }
            if (stringArray[0].equalsIgnoreCase("global")) {
                return Night.MODULE_MANAGER.getModules().stream().map(Module::getName).toList();
            }
            if (stringArray[0].equalsIgnoreCase("bind") || stringArray[0].equalsIgnoreCase("visual") || stringArray[0].equalsIgnoreCase("modules")) {
                return List.of("load", "save", "reset");
            }
            return List.of("reset", "load", "save");
        }
        if (stringArray.length == 2) {
            if (stringArray[0].equalsIgnoreCase("global")) {
                return List.of("load", "save", "reset");
            }
            if (stringArray[0].equalsIgnoreCase("bind") && (stringArray[1].equalsIgnoreCase("load") || stringArray[1].equalsIgnoreCase("save") || stringArray[1].equalsIgnoreCase("reset"))) {
                return this.bindProfileNames();
            }
            if (stringArray[0].equalsIgnoreCase("visual") && (stringArray[1].equalsIgnoreCase("load") || stringArray[1].equalsIgnoreCase("save") || stringArray[1].equalsIgnoreCase("reset"))) {
                return this.visualConfigNames();
            }
            if (stringArray[0].equalsIgnoreCase("modules") && (stringArray[1].equalsIgnoreCase("load") || stringArray[1].equalsIgnoreCase("save") || stringArray[1].equalsIgnoreCase("reset"))) {
                return this.moduleStateNames();
            }
        }
        if (stringArray.length == 3 && stringArray[0].equalsIgnoreCase("global") && (stringArray[2].equalsIgnoreCase("load") || stringArray[2].equalsIgnoreCase("save") || stringArray[2].equalsIgnoreCase("reset"))) {
            return this.globalConfigNames(stringArray[1]);
        }
        return List.of();
    }

    private List<String> savedConfigNames() {
        File[] fileArray;
        File file2 = new File("Lightning/Configs");
        if (!file2.exists()) {
            file2.mkdirs();
        }
        if ((fileArray = file2.listFiles((file, string) -> string.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(fileArray).map(File::getName).map(string -> string.substring(0, string.length() - ".json".length())).sorted().toList();
    }

    private List<String> globalConfigNames(String string2) {
        File[] fileArray;
        File file2 = new File("Lightning/GlobalConfigs/" + string2);
        if (!file2.exists()) {
            file2.mkdirs();
        }
        if ((fileArray = file2.listFiles((file, string) -> string.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(fileArray).map(File::getName).map(string -> string.substring(0, string.length() - ".json".length())).sorted().toList();
    }

    private List<String> bindProfileNames() {
        File[] fileArray;
        File file2 = new File("Lightning/Binds");
        if (!file2.exists()) {
            file2.mkdirs();
        }
        if ((fileArray = file2.listFiles((file, string) -> string.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(fileArray).map(File::getName).map(string -> string.substring(0, string.length() - ".json".length())).sorted().toList();
    }

    private List<String> visualConfigNames() {
        File[] fileArray;
        File file2 = new File("Lightning/Visuals");
        if (!file2.exists()) {
            file2.mkdirs();
        }
        if ((fileArray = file2.listFiles((file, string) -> string.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(fileArray).map(File::getName).map(string -> string.substring(0, string.length() - ".json".length())).sorted().toList();
    }

    private List<String> moduleStateNames() {
        File[] fileArray;
        File file2 = new File("Lightning/Modules");
        if (!file2.exists()) {
            file2.mkdirs();
        }
        if ((fileArray = file2.listFiles((file, string) -> string.endsWith(".json"))) == null) {
            return List.of();
        }
        return Arrays.stream(fileArray).map(File::getName).map(string -> string.substring(0, string.length() - ".json".length())).sorted().toList();
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
    public void execute(String[] stringArray) {
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("bind")) {
            if (stringArray.length == 3) {
                String string = stringArray[1].toLowerCase();
                String string2 = stringArray[2];
                switch (string) {
                    case "save": {
                        this.saveBindProfile(string2);
                        break;
                    }
                    case "load": {
                        this.loadBindProfile(string2);
                        break;
                    }
                    case "reset": {
                        this.resetBindProfile(string2);
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
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("visual")) {
            if (stringArray.length == 3) {
                String string = stringArray[1].toLowerCase();
                String string3 = stringArray[2];
                switch (string) {
                    case "save": {
                        this.saveVisualConfig(string3);
                        break;
                    }
                    case "load": {
                        this.loadVisualConfig(string3);
                        break;
                    }
                    case "reset": {
                        this.resetVisualConfig(string3);
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
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("modules")) {
            if (stringArray.length == 3) {
                String string = stringArray[1].toLowerCase();
                String string4 = stringArray[2];
                switch (string) {
                    case "save": {
                        this.saveModulesState(string4);
                        break;
                    }
                    case "load": {
                        this.loadModulesState(string4);
                        break;
                    }
                    case "reset": {
                        this.resetModulesState(string4);
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
        if (stringArray.length > 0 && stringArray[0].equalsIgnoreCase("global")) {
            if (stringArray.length == 4) {
                Module module = Night.MODULE_MANAGER.getModule(stringArray[1]);
                if (module == null) {
                    Night.CHAT_MANAGER.tagged("Could not find the module specified.", this.getTag(), this.getName());
                    return;
                }
                String string = stringArray[2].toLowerCase();
                String string5 = stringArray[3];
                switch (string) {
                    case "save": {
                        this.saveGlobalConfig(module, string5);
                        break;
                    }
                    case "load": {
                        this.loadGlobalConfig(module, string5);
                        break;
                    }
                    case "reset": {
                        this.resetGlobalConfig(module, string5);
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
        if (stringArray.length == 2) {
            if (stringArray[1].equalsIgnoreCase("reset")) {
                this.resetConfig(stringArray[0]);
                return;
            }
            switch (stringArray[0].toLowerCase()) {
                case "load": {
                    if (!FileUtils.fileExists("Lightning/Configs/" + stringArray[1] + ".json")) {
                        Night.CHAT_MANAGER.tagged("The specified configuration does not exist.", this.getTag(), this.getName());
                        return;
                    }
                    try {
                        Night.CONFIG_MANAGER.loadModules(stringArray[1]);
                        Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    catch (IOException iOException) {
                        Night.CHAT_MANAGER.tagged("Failed to load the " + String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    break;
                }
                case "save": {
                    try {
                        Night.CONFIG_MANAGER.saveModules(stringArray[1]);
                        Night.CHAT_MANAGER.tagged("Successfully saved the configuration to " + String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
                    }
                    catch (IOException iOException) {
                        Night.CHAT_MANAGER.tagged("Failed to save the " + String.valueOf(ChatUtils.getPrimary()) + stringArray[1] + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
                    }
                    break;
                }
                case "reset": {
                    this.resetConfig(stringArray[1]);
                    break;
                }
                default: {
                    this.messageSyntax();
                    break;
                }
            }
        } else if (stringArray.length == 1) {
            switch (stringArray[0].toLowerCase()) {
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

    private void resetConfig(String string) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.setToggled(false);
            module.resetValues();
        }
        try {
            Night.CONFIG_MANAGER.saveModules(string);
            Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " configuration to default.", this.getTag(), this.getName());
        }
        catch (IOException iOException) {
            Night.CHAT_MANAGER.tagged("Failed to reset the " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " configuration.", this.getTag(), this.getName());
        }
    }

    private void resetGlobalConfig(Module module, String string) {
        module.resetValues();
        this.saveGlobalConfig(module, string);
    }

    private void resetBindProfile(String string) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.bind.resetValue();
            for (Setting setting : module.getSettings()) {
                if (!(setting instanceof BindSetting)) continue;
                BindSetting bindSetting = (BindSetting)setting;
                bindSetting.resetValue();
            }
        }
        this.saveBindProfile(string);
    }

    private void resetVisualConfig(String string) {
        for (Module module : this.visualModules()) {
            module.resetValues();
        }
        this.saveVisualConfig(string);
    }

    private void resetModulesState(String string) {
        for (Module module : Night.MODULE_MANAGER.getModules()) {
            module.setToggled(false);
            module.resetValues();
        }
        this.saveModulesState(string);
    }

    private void saveGlobalConfig(Module module, String string) {
        try {
            JsonObject jsonObject = Night.CONFIG_MANAGER.serializeModuleJson(module);
            File file = new File("Lightning/GlobalConfigs/" + module.getName());
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file, string + ".json");
            try (FileWriter fileWriter = new FileWriter(file2);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved global config " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " for module " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to save global config: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void loadGlobalConfig(Module module, String string2) {
        File file2 = new File("Lightning/GlobalConfigs/" + module.getName() + "/" + string2 + ".json");
        if (!file2.exists()) {
            Night.CHAT_MANAGER.tagged("Global config " + String.valueOf(ChatUtils.getPrimary()) + string2 + String.valueOf(ChatUtils.getSecondary()) + " does not exist for " + module.getName() + ".", this.getTag(), this.getName());
            return;
        }
        try (FileReader fileReader = new FileReader(file2);){
            JsonObject jsonObject = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            File file3 = new File("Lightning/Configs");
            File[] fileArray = file3.listFiles((file, string) -> string.endsWith(".json"));
            if (fileArray != null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                for (File file4 : fileArray) {
                    JsonObject jsonObject2;
                    try (FileReader fileReader2 = new FileReader(file4)) {
                        jsonObject2 = JsonParser.parseReader((Reader)fileReader2).getAsJsonObject();
                    }
                    JsonObject modulesObj = jsonObject2.has("Modules") ? jsonObject2.getAsJsonObject("Modules") : new JsonObject();
                    modulesObj.add(module.getName(), (JsonElement)jsonObject);
                    jsonObject2.add("Modules", (JsonElement)modulesObj);
                    try (FileWriter fileWriter = new FileWriter(file4)) {
                        gson.toJson((JsonElement)jsonObject2, (Appendable)fileWriter);
                    }
                }
            }
            Night.CONFIG_MANAGER.loadModules(Night.CONFIG_MANAGER.getCurrentConfig());
            Night.CHAT_MANAGER.tagged("Successfully loaded global config " + String.valueOf(ChatUtils.getPrimary()) + string2 + String.valueOf(ChatUtils.getSecondary()) + " for module " + String.valueOf(ChatUtils.getPrimary()) + module.getName() + String.valueOf(ChatUtils.getSecondary()) + " across all profiles.", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to load global config: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void saveBindProfile(String string) {
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
            File file = new File("Lightning/Binds");
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file, string + ".json");
            try (FileWriter fileWriter = new FileWriter(file2)) {
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current binds to " + String.valueOf(ChatUtils.getPrimary()) + string + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to save bind profile: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void loadBindProfile(String string) {
        File file = new File("Lightning/Binds/" + string + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Bind profile " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        try (FileReader fileReader = new FileReader(file);){
            JsonObject jsonObject = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            for (String string2 : jsonObject.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(string2);
                if (module == null) continue;
                JsonObject jsonObject2 = jsonObject.getAsJsonObject(string2);
                for (String string3 : jsonObject2.keySet()) {
                    BindSetting bindSetting = string3.equals(module.bind.getName()) ? module.bind : this.findBindSetting(module, string3);
                    BindSetting bindSetting2 = bindSetting;
                    if (bindSetting == null) continue;
                    String[] stringArray = jsonObject2.get(string3).getAsString().split(",", 2);
                    bindSetting.setValue(Integer.parseInt(stringArray[0]));
                    if (stringArray.length <= 1) continue;
                    bindSetting.setMode(stringArray[1]);
                }
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " bind profile.", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to load bind profile: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private BindSetting findBindSetting(Module module, String string) {
        for (Setting setting : module.getSettings()) {
            BindSetting bindSetting;
            if (!(setting instanceof BindSetting) || !(bindSetting = (BindSetting)setting).getName().equals(string)) continue;
            return bindSetting;
        }
        return null;
    }

    private void saveVisualConfig(String string) {
        try {
            JsonObject jsonObject = new JsonObject();
            for (Module object2 : this.visualModules()) {
                jsonObject.add(object2.getName(), (JsonElement)Night.CONFIG_MANAGER.serializeModuleJson(object2));
            }
            File file = new File("Lightning/Visuals");
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file, string + ".json");
            try (FileWriter fileWriter = new FileWriter(file2);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current visual settings to " + String.valueOf(ChatUtils.getPrimary()) + string + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to save visual config: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void loadVisualConfig(String string) {
        File file = new File("Lightning/Visuals/" + string + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Visual config " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        Set set = this.visualModules().stream().map(Module::getName).collect(Collectors.toSet());
        try (FileReader fileReader = new FileReader(file);){
            JsonObject jsonObject = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            for (String string2 : jsonObject.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(string2);
                if (module == null || !set.contains(module.getName())) continue;
                Night.CONFIG_MANAGER.applyModuleJson(module, jsonObject.getAsJsonObject(string2));
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " visual config.", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to load visual config: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void saveModulesState(String string) {
        try {
            JsonObject jsonObject = new JsonObject();
            for (Module object2 : Night.MODULE_MANAGER.getModules()) {
                jsonObject.add(object2.getName(), (JsonElement)Night.CONFIG_MANAGER.serializeModuleJson(object2, false));
            }
            File file = new File("Lightning/Modules");
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file, string + ".json");
            try (FileWriter fileWriter = new FileWriter(file2);){
                new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)jsonObject, (Appendable)fileWriter);
            }
            Night.CHAT_MANAGER.tagged("Successfully saved the current module states to " + String.valueOf(ChatUtils.getPrimary()) + string + ".json" + String.valueOf(ChatUtils.getSecondary()) + ".", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to save module states: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }

    private void loadModulesState(String string) {
        File file = new File("Lightning/Modules/" + string + ".json");
        if (!file.exists()) {
            Night.CHAT_MANAGER.tagged("Module state profile " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " does not exist.", this.getTag(), this.getName());
            return;
        }
        try (FileReader fileReader = new FileReader(file);){
            JsonObject jsonObject = JsonParser.parseReader((Reader)fileReader).getAsJsonObject();
            for (String string2 : jsonObject.keySet()) {
                Module module = Night.MODULE_MANAGER.getModule(string2);
                if (module == null) continue;
                JsonElement jsonElement = jsonObject.get(string2);
                if (jsonElement.isJsonObject()) {
                    Night.CONFIG_MANAGER.applyModuleJson(module, jsonElement.getAsJsonObject(), false);
                    continue;
                }
                if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isBoolean()) continue;
                module.setToggled(jsonElement.getAsBoolean());
            }
            Night.CHAT_MANAGER.tagged("Successfully loaded the " + String.valueOf(ChatUtils.getPrimary()) + string + String.valueOf(ChatUtils.getSecondary()) + " module states.", this.getTag(), this.getName());
        }
        catch (Exception exception) {
            Night.CHAT_MANAGER.tagged("Failed to load module states: " + exception.getMessage(), this.getTag(), this.getName());
            exception.printStackTrace();
        }
    }
}

