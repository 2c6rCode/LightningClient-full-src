/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.block.Block
 */
package lightning.commands.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.modules.Module;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.color.ColorUtils;
import lightning.utils.input.KeyboardUtils;
import lightning.utils.minecraft.IdentifierUtils;
import lightning.utils.miscellaneous.ListUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@RegisterCommand(name="module", tag="Module", description="Allows you to view and change this module's settings.", syntax="<[setting]> <[...]> | <list|reset>")
public class ModuleCommand
extends Command {
    private final Module module;

    public ModuleCommand(Module module) {
        this.module = module;
        this.setName(module.getName().toLowerCase());
        this.setTag(module.getName());
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        String subAction;
        if (args.length == 0) {
            ArrayList<String> suggestions = new ArrayList<String>();
            suggestions.add("list");
            suggestions.add("reset");
            for (Setting setting : this.module.getSettings()) {
                if (setting instanceof CategorySetting) continue;
                suggestions.add(setting.getName().toLowerCase());
            }
            return suggestions;
        }
        if (args.length == 1) {
            Setting setting = this.module.getSetting(args[0]);
            if (setting == null) {
                return List.of();
            }
            return switch (setting) {
                case BooleanSetting booleanSetting -> List.of("true", "false", "reset");
                case NumberSetting numberSetting -> List.of("reset");
                case ModeSetting modeSetting -> {
                    ArrayList<String> modes = new ArrayList<>(modeSetting.getModes());
                    modes.add("reset");
                    modes.add("list");
                    yield modes;
                }
                case StringSetting stringSetting -> List.of("force-reset");
                case BindSetting bindSetting -> List.of("reset");
                case ColorSetting colorSetting -> List.of("red", "green", "blue", "alpha", "sync", "rainbow", "code", "reset");
                case WhitelistSetting whitelistSetting -> List.of("add", "del", "clear", "list");
                default -> List.of();
            };
        }
        if (args.length == 2 && this.module.getSetting(args[0]) instanceof ColorSetting && ((subAction = args[1].toLowerCase()).equals("sync") || subAction.equals("rainbow"))) {
            return List.of("true", "false");
        }
        return List.of();
    }

    @Override
    public void execute(String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (this.module.getSettings().isEmpty()) {
                    Night.CHAT_MANAGER.tagged("This module currently has no registered settings.", this.module.getName(), "module-cmd-" + this.getName() + "-list");
                } else {
                    StringBuilder listBuilder = new StringBuilder();
                    int index = 0;
                    for (Setting setting : this.module.getSettings()) {
                        if (setting instanceof CategorySetting) continue;
                        listBuilder.append(ChatUtils.getSecondary()).append(setting.getName()).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary());
                        if (setting instanceof BooleanSetting) {
                            listBuilder.append(((BooleanSetting)setting).getValue());
                        }
                        if (setting instanceof NumberSetting) {
                            listBuilder.append(((NumberSetting)setting).getValue());
                        }
                        if (setting instanceof ModeSetting) {
                            listBuilder.append(((ModeSetting)setting).getValue());
                        }
                        if (setting instanceof StringSetting) {
                            listBuilder.append(((StringSetting)setting).getValue());
                        }
                        if (setting instanceof BindSetting) {
                            listBuilder.append(KeyboardUtils.getKeyName(((BindSetting)setting).getValue()).toUpperCase());
                        }
                        if (setting instanceof WhitelistSetting) {
                            listBuilder.append("...");
                        }
                        if (setting instanceof ColorSetting) {
                            listBuilder.append("RGBA(").append(((ColorSetting)setting).getColor().getRed()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting)setting).getColor().getGreen()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting)setting).getColor().getBlue()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting)setting).getColor().getAlpha()).append(")").append(ChatUtils.getPrimary()).append(",").append(ChatUtils.getSecondary()).append(" SyRa(").append(((ColorSetting)setting).isSync()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting)setting).isRainbow()).append(")");
                        }
                        listBuilder.append(ChatUtils.getPrimary()).append("]").append(ChatUtils.getSecondary()).append(index + 1 == this.module.getSettings().size() ? "" : ", ");
                        ++index;
                    }
                    Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getSecondary()) + this.module.getName() + " " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + this.module.getSettings().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(listBuilder), "module-cmd-" + this.getName() + "-list");
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                this.module.resetValues();
                Night.CHAT_MANAGER.tagged("Successfully reset all of the module's settings.", this.module.getName(), "module-cmd-" + this.getName());
            } else {
                Setting setting = this.module.getSetting(args[0]);
                if (setting == null) {
                    Night.CHAT_MANAGER.tagged("Could not find the setting specified.", this.module.getName(), "module-cmd-" + this.getName());
                    return;
                }
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                switch (setting) {
                    case BooleanSetting booleanSetting: {
                        if (booleanSetting == this.module.chatNotify) {
                            Night.CHAT_MANAGER.tagged("This setting is the module's main ChatNotify setting. Please use the " + String.valueOf(ChatUtils.getPrimary()) + "chatnotify" + String.valueOf(ChatUtils.getSecondary()) + " command instead.", this.module.getName(), this.getName());
                            return;
                        }
                        if (booleanSetting == this.module.drawn) {
                            Night.CHAT_MANAGER.tagged("This setting is the module's main Drawn setting. Please use the " + String.valueOf(ChatUtils.getPrimary()) + "drawn" + String.valueOf(ChatUtils.getSecondary()) + " command instead.", this.module.getName(), this.getName());
                            return;
                        }
                        if (subArgs.length == 1) {
                            if (subArgs[0].equalsIgnoreCase("reset")) {
                                booleanSetting.resetValue();
                                Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + booleanSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting.", this.module.getName(), "module-cmd-" + this.getName());
                                break;
                            }
                            booleanSetting.setValue(Boolean.parseBoolean(subArgs[0]));
                            Night.CHAT_MANAGER.tagged("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + booleanSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + booleanSetting.getValue() + String.valueOf(ChatUtils.getSecondary()) + ".", this.module.getName(), "module-cmd-" + this.getName());
                            break;
                        }
                        Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + booleanSetting.getName().toLowerCase() + " <[value]|reset>");
                        break;
                    }
                    case NumberSetting numberSetting: {
                        if (subArgs.length == 1) {
                            if (subArgs[0].equalsIgnoreCase("reset")) {
                                numberSetting.resetValue();
                                Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + numberSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting.", this.module.getName(), "module-cmd-" + this.getName());
                                break;
                            }
                            try {
                                switch (numberSetting.getType()) {
                                    case LONG: {
                                        numberSetting.setValue(Long.parseLong(subArgs[0]));
                                        break;
                                    }
                                    case DOUBLE: {
                                        numberSetting.setValue(Double.parseDouble(subArgs[0]));
                                        break;
                                    }
                                    case FLOAT: {
                                        numberSetting.setValue(Float.valueOf(Float.parseFloat(subArgs[0])));
                                        break;
                                    }
                                    default: {
                                        numberSetting.setValue(Integer.parseInt(subArgs[0]));
                                    }
                                }
                                Night.CHAT_MANAGER.tagged("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + numberSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + String.valueOf(numberSetting.getValue()) + String.valueOf(ChatUtils.getSecondary()) + ".", this.module.getName(), "module-cmd-" + this.getName());
                            }
                            catch (NumberFormatException numberFormatException) {
                                Night.CHAT_MANAGER.tagged("Please input a valid " + String.valueOf(ChatUtils.getPrimary()) + numberSetting.getType().name().toLowerCase() + String.valueOf(ChatUtils.getSecondary()) + " number.", this.module.getName(), "module-cmd-" + this.getName());
                            }
                            break;
                        }
                        Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + numberSetting.getName().toLowerCase() + " <[value]|reset>");
                        break;
                    }
                    case ModeSetting modeSetting: {
                        if (subArgs.length >= 1) {
                            if (!(subArgs[0].equalsIgnoreCase("reset") && modeSetting.getModes().stream().noneMatch("reset"::equalsIgnoreCase) || subArgs[0].equalsIgnoreCase("force-reset"))) {
                                if (!(subArgs[0].equalsIgnoreCase("list") && modeSetting.getModes().stream().noneMatch("list"::equalsIgnoreCase) || subArgs[0].equalsIgnoreCase("force-list"))) {
                                    if (subArgs[0].equalsIgnoreCase("reset") && modeSetting.getModes().stream().anyMatch("reset"::equalsIgnoreCase)) {
                                        Night.CHAT_MANAGER.info("If you would like to reset this setting's value, write \"" + String.valueOf(ChatUtils.getPrimary()) + "force-reset" + String.valueOf(ChatUtils.getSecondary()) + "\" instead.");
                                    }
                                    if (subArgs[0].equalsIgnoreCase("list") && modeSetting.getModes().stream().anyMatch("list"::equalsIgnoreCase)) {
                                        Night.CHAT_MANAGER.info("If you would like to view a list of valid values for this setting, write \"" + String.valueOf(ChatUtils.getPrimary()) + "force-list" + String.valueOf(ChatUtils.getSecondary()) + "\" instead.");
                                    }
                                    StringBuilder valueBuilder = new StringBuilder();
                                    int partIndex = 0;
                                    for (String part : subArgs) {
                                        valueBuilder.append(part).append(partIndex + 1 == subArgs.length ? "" : " ");
                                        ++partIndex;
                                    }
                                    if (modeSetting.getModes().stream().anyMatch(valueBuilder.toString()::equalsIgnoreCase)) {
                                        modeSetting.setValue(modeSetting.getModes().get(ListUtils.getIndex(modeSetting.getModes(), valueBuilder.toString())));
                                        Night.CHAT_MANAGER.tagged("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + modeSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + modeSetting.getValue() + String.valueOf(ChatUtils.getSecondary()) + ".", this.module.getName(), "module-cmd-" + this.getName());
                                        break;
                                    }
                                    Night.CHAT_MANAGER.tagged("Please input a valid value for this setting.", this.module.getName(), "module-cmd-" + this.getName());
                                    break;
                                }
                                if (modeSetting.getModes().isEmpty()) {
                                    Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + modeSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " currently has no values registered.", this.module.getName(), "module-cmd-" + this.getName());
                                    break;
                                }
                                StringBuilder modesBuilder = new StringBuilder();
                                int modeIndex = 0;
                                for (String modeName : modeSetting.getModes()) {
                                    modesBuilder.append(ChatUtils.getSecondary()).append(modeName).append(modeIndex + 1 == modeSetting.getModes().size() ? "" : ", ");
                                    ++modeIndex;
                                }
                                Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getSecondary()) + modeSetting.getName() + " " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + modeSetting.getModes().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(modesBuilder), this.module.getName(), "module-cmd-" + this.getName());
                                break;
                            }
                            modeSetting.resetValue();
                            Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + modeSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting.", this.module.getName(), "module-cmd-" + this.getName());
                            break;
                        }
                        Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + modeSetting.getName().toLowerCase() + " <[value]|reset|list>");
                        break;
                    }
                    case StringSetting stringSetting: {
                        if (subArgs.length >= 1) {
                            if (subArgs[0].equalsIgnoreCase("force-reset")) {
                                stringSetting.setValue(stringSetting.getDefaultValue());
                                Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + stringSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting.", this.module.getName(), "module-cmd-" + this.getName());
                                break;
                            }
                            if (subArgs[0].equalsIgnoreCase("reset")) {
                                Night.CHAT_MANAGER.info("If you would like to reset this setting's value, write \"" + String.valueOf(ChatUtils.getPrimary()) + "force-reset" + String.valueOf(ChatUtils.getSecondary()) + "\" instead.");
                            }
                            StringBuilder textBuilder = new StringBuilder();
                            int partIndex = 0;
                            for (String part : subArgs) {
                                textBuilder.append(part).append(partIndex + 1 == subArgs.length ? "" : " ");
                                ++partIndex;
                            }
                            stringSetting.setValue(textBuilder.toString());
                            Night.CHAT_MANAGER.tagged("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + stringSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + stringSetting.getValue() + String.valueOf(ChatUtils.getSecondary()) + ".", this.module.getName(), "module-cmd-" + this.getName());
                            break;
                        }
                        Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + stringSetting.getName().toLowerCase() + " <[value]|reset>");
                        break;
                    }
                    case BindSetting bindSetting: {
                        if (setting == this.module.bind) {
                            Night.CHAT_MANAGER.tagged("This setting is the module's main toggle keybind. Please use the " + String.valueOf(ChatUtils.getPrimary()) + "bind" + String.valueOf(ChatUtils.getSecondary()) + " command instead.", this.module.getName(), "module-cmd-" + this.getName());
                            return;
                        }
                        if (subArgs.length == 1) {
                            if (subArgs[0].equalsIgnoreCase("reset")) {
                                bindSetting.resetValue();
                                Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + bindSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting.", this.module.getName(), "module-cmd-" + this.getName());
                                break;
                            }
                            int keyCode = 0;
                            try {
                                keyCode = KeyboardUtils.getKeyNumber(subArgs[0]);
                            }
                            catch (IllegalArgumentException illegalArgumentException) {
                                // empty catch block
                            }
                            bindSetting.setValue(keyCode);
                            Night.CHAT_MANAGER.tagged("Successfully set " + String.valueOf(ChatUtils.getPrimary()) + bindSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " to " + String.valueOf(ChatUtils.getPrimary()) + KeyboardUtils.getKeyName(bindSetting.getValue()).toUpperCase() + String.valueOf(ChatUtils.getSecondary()) + ".", this.module.getName(), "module-cmd-" + this.getName());
                            break;
                        }
                        Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + bindSetting.getName().toLowerCase() + " <[value]|reset>");
                        break;
                    }
                    case ColorSetting colorSetting: {
                        if (subArgs.length == 2) {
                            int channelValue = 0;
                            if (subArgs[0].equalsIgnoreCase("red") || subArgs[0].equalsIgnoreCase("green") || subArgs[0].equalsIgnoreCase("blue") || subArgs[0].equalsIgnoreCase("alpha")) {
                                try {
                                    channelValue = Math.clamp((long)Integer.parseInt(subArgs[1]), 0, 255);
                                }
                                catch (NumberFormatException numberFormatException) {
                                    Night.CHAT_MANAGER.tagged("Please input a valid number for the " + String.valueOf(ChatUtils.getPrimary()) + subArgs[0].toLowerCase() + String.valueOf(ChatUtils.getSecondary()) + " value.", this.module.getName());
                                    return;
                                }
                            }
                            String fieldName = "";
                            String fieldValue = "";
                            if (subArgs[0].equalsIgnoreCase("red")) {
                                colorSetting.setColor(new Color(channelValue, colorSetting.getValue().getColor().getGreen(), colorSetting.getValue().getColor().getBlue(), colorSetting.getValue().getColor().getAlpha()));
                                fieldName = "red";
                                fieldValue = String.valueOf(colorSetting.getValue().getColor().getRed());
                            } else if (subArgs[0].equalsIgnoreCase("green")) {
                                colorSetting.setColor(new Color(colorSetting.getValue().getColor().getRed(), channelValue, colorSetting.getValue().getColor().getBlue(), colorSetting.getValue().getColor().getAlpha()));
                                fieldName = "green";
                                fieldValue = String.valueOf(colorSetting.getValue().getColor().getGreen());
                            } else if (subArgs[0].equalsIgnoreCase("blue")) {
                                colorSetting.setColor(new Color(colorSetting.getValue().getColor().getRed(), colorSetting.getValue().getColor().getGreen(), channelValue, colorSetting.getValue().getColor().getAlpha()));
                                fieldName = "blue";
                                fieldValue = String.valueOf(colorSetting.getValue().getColor().getBlue());
                            } else if (subArgs[0].equalsIgnoreCase("alpha")) {
                                colorSetting.setColor(new Color(colorSetting.getValue().getColor().getRed(), colorSetting.getValue().getColor().getGreen(), colorSetting.getValue().getColor().getBlue(), channelValue));
                                fieldName = "alpha";
                                fieldValue = String.valueOf(colorSetting.getValue().getColor().getAlpha());
                            } else if (subArgs[0].equalsIgnoreCase("sync")) {
                                colorSetting.setSync(Boolean.parseBoolean(subArgs[1]));
                                fieldName = "sync";
                                fieldValue = String.valueOf(colorSetting.isSync());
                            } else if (subArgs[0].equalsIgnoreCase("rainbow")) {
                                colorSetting.setRainbow(Boolean.parseBoolean(subArgs[1]));
                                fieldName = "rainbow";
                                fieldValue = String.valueOf(colorSetting.isRainbow());
                            } else if (subArgs[0].equalsIgnoreCase("code")) {
                                if (!ColorUtils.isValidColorCode(subArgs[1])) {
                                    Night.CHAT_MANAGER.tagged("Please input a valid color code.", this.module.getName(), "module-cmd-" + this.getName());
                                    return;
                                }
                                try {
                                    Color color = Color.decode((subArgs[1].startsWith("#") ? "" : "#") + subArgs[1]);
                                    colorSetting.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
                                }
                                catch (NumberFormatException numberFormatException) {
                                    Night.CHAT_MANAGER.tagged("Please input a valid color code.", this.module.getName(), "module-cmd-" + this.getName());
                                    return;
                                }
                                fieldName = "color";
                                fieldValue = "rgba(" + colorSetting.getValue().getColor().getRed() + ", " + colorSetting.getValue().getColor().getGreen() + ", " + colorSetting.getValue().getColor().getBlue() + ", " + colorSetting.getValue().getColor().getAlpha() + ")";
                            } else {
                                Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + colorSetting.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
                            }
                            Night.CHAT_MANAGER.tagged("Successfully set the " + String.valueOf(ChatUtils.getPrimary()) + fieldName + String.valueOf(ChatUtils.getSecondary()) + " value to " + String.valueOf(ChatUtils.getPrimary()) + fieldValue + String.valueOf(ChatUtils.getSecondary()) + ".", this.module.getName(), "module-cmd-" + this.getName());
                            break;
                        }
                        if (subArgs.length == 1) {
                            if (subArgs[0].equalsIgnoreCase("reset")) {
                                colorSetting.resetValue();
                                Night.CHAT_MANAGER.tagged("Successfully reset the " + String.valueOf(ChatUtils.getPrimary()) + colorSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " setting.", this.module.getName(), "module-cmd-" + this.getName());
                                break;
                            }
                            Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + colorSetting.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
                            break;
                        }
                        Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + colorSetting.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
                        break;
                    }
                    case WhitelistSetting whitelistSetting: {
                        if (subArgs.length == 2) {
                            if (subArgs[0].equalsIgnoreCase("add")) {
                                if (whitelistSetting.getType() == WhitelistSetting.Type.ITEMS) {
                                    Item item = IdentifierUtils.getItem(subArgs[1]);
                                    if (item == null) {
                                        Night.CHAT_MANAGER.tagged("Please input a valid item ID.", this.module.getName(), "module-cmd-" + this.getName());
                                        return;
                                    }
                                    if (whitelistSetting.isWhitelistContains(item)) {
                                        Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + item.getName(item.getDefaultInstance()).getString() + String.valueOf(ChatUtils.getSecondary()) + " is already on the whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                        break;
                                    }
                                    whitelistSetting.add(item);
                                    Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + item.getName(item.getDefaultInstance()).getString() + String.valueOf(ChatUtils.getSecondary()) + " to the whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                    break;
                                }
                                if (whitelistSetting.getType() == WhitelistSetting.Type.BLOCKS) {
                                    Block block = IdentifierUtils.getBlock(subArgs[1]);
                                    if (block == null) {
                                        Night.CHAT_MANAGER.tagged("Please input a valid block ID.", this.module.getName(), "module-cmd-" + this.getName());
                                        return;
                                    }
                                    if (whitelistSetting.isWhitelistContains(block)) {
                                        Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + block.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " is already on the whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                        break;
                                    }
                                    whitelistSetting.add(block);
                                    Night.CHAT_MANAGER.tagged("Successfully added " + String.valueOf(ChatUtils.getPrimary()) + block.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " to the whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                    break;
                                }
                                Night.CHAT_MANAGER.error("Something went wrong while detecting the setting's type.");
                                break;
                            }
                            if (subArgs[0].equalsIgnoreCase("del")) {
                                Block block;
                                Item item;
                                if (whitelistSetting.getType() == WhitelistSetting.Type.ITEMS && (item = IdentifierUtils.getItem(subArgs[1])) != null && whitelistSetting.isWhitelistContains(item)) {
                                    whitelistSetting.remove(item);
                                    Night.CHAT_MANAGER.tagged("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + item.getName(item.getDefaultInstance()).getString() + String.valueOf(ChatUtils.getSecondary()) + " from the whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                    break;
                                }
                                if (whitelistSetting.getType() == WhitelistSetting.Type.BLOCKS && (block = IdentifierUtils.getBlock(subArgs[1])) != null && whitelistSetting.isWhitelistContains(block)) {
                                    whitelistSetting.remove(block);
                                    Night.CHAT_MANAGER.tagged("Successfully removed " + String.valueOf(ChatUtils.getPrimary()) + block.getName().getString() + String.valueOf(ChatUtils.getSecondary()) + " from the whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                    break;
                                }
                                Night.CHAT_MANAGER.tagged(String.valueOf(ChatUtils.getPrimary()) + subArgs[1] + String.valueOf(ChatUtils.getSecondary()) + " is not on the whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                break;
                            }
                            Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + whitelistSetting.getName().toLowerCase() + " <add|del> <[id]> | <list|clear>");
                            break;
                        }
                        if (subArgs.length == 1) {
                            if (subArgs[0].equalsIgnoreCase("clear")) {
                                whitelistSetting.getWhitelist().clear();
                                Night.CHAT_MANAGER.tagged("Successfully cleared the " + String.valueOf(ChatUtils.getPrimary()) + whitelistSetting.getName() + String.valueOf(ChatUtils.getSecondary()) + " whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                break;
                            }
                            if (subArgs[0].equalsIgnoreCase("list")) {
                                if (whitelistSetting.getWhitelist().isEmpty()) {
                                    Night.CHAT_MANAGER.tagged("There are currently no " + (whitelistSetting.getType() == WhitelistSetting.Type.ITEMS ? "items" : "blocks") + " on the whitelist.", this.module.getName(), "module-cmd-" + this.getName());
                                    break;
                                }
                                StringBuilder listBuilder = new StringBuilder();
                                int itemIndex = 0;
                                for (Object entry : whitelistSetting.getWhitelist()) {
                                    listBuilder.append(ChatUtils.getSecondary());
                                    switch (entry) {
                                        case Block block -> listBuilder.append(block.getName().getString());
                                        case Item item -> listBuilder.append(item.getName(item.getDefaultInstance()).getString());
                                        default -> listBuilder.append("Invalid");
                                    }
                                    listBuilder.append(ChatUtils.getPrimary()).append(itemIndex + 1 == whitelistSetting.getWhitelist().size() ? "" : ", ");
                                    ++itemIndex;
                                }
                                Night.CHAT_MANAGER.message(String.valueOf(ChatUtils.getSecondary()) + whitelistSetting.getName() + " " + String.valueOf(ChatUtils.getPrimary()) + "[" + String.valueOf(ChatUtils.getSecondary()) + whitelistSetting.getWhitelist().size() + String.valueOf(ChatUtils.getPrimary()) + "]: " + String.valueOf(ChatUtils.getSecondary()) + String.valueOf(listBuilder), "module-cmd-" + this.getName());
                                break;
                            }
                            Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + whitelistSetting.getName().toLowerCase() + " <add|del> <[id]> | <clear|list>");
                            break;
                        }
                        Night.CHAT_MANAGER.info(this.module.getName().toLowerCase() + " " + whitelistSetting.getName().toLowerCase() + " <add|del> <[id]> | <clear|list>");
                        break;
                    }
                    default: {
                        this.messageSyntax();
                    }
                }
            }
        } else {
            this.messageSyntax();
        }
    }
}

