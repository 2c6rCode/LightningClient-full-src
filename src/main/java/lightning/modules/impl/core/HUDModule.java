/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  net.minecraft.client.gui.components.PlayerFaceExtractor
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.resources.Identifier
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  org.joml.Matrix3x2fStack
 */
package lightning.modules.impl.core;

import java.awt.Color;
import java.lang.invoke.CallSite;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderOverlayEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.events.impl.TickEvent;
import lightning.gui.HUDEditorScreen;
import lightning.managers.FontManager;
import lightning.managers.HudElementRegistry;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.core.FontModule;
import lightning.modules.impl.core.HudListStyle;
import lightning.modules.impl.core.IgnoreNakedModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.PositionSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.input.KeyboardUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.text.FormattingUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.joml.Matrix3x2fStack;

@RegisterModule(name="HUD", description="Renders information about the game and the client on the screen.", category=Module.Category.CORE, toggled=true, drawn=false)
public class HUDModule
extends Module {
    private static final Identifier LOGO_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/gui/nametag_bolt.png");
    private static final int LOGO_SIZE = 64;
    public CategorySetting watermarkCategory = new CategorySetting("Watermark", "The settings for the client's watermark.");
    public BooleanSetting watermark = new BooleanSetting("Watermark", "Enabled", "Renders the client's name and version at the top left.", new CategorySetting.Visibility(this.watermarkCategory), true);
    public StringSetting watermarkText = new StringSetting("WatermarkText", "Component", "The client name that will be rendered.", new CategorySetting.Visibility(this.watermarkCategory), "LightningClient");
    public BooleanSetting watermarkLogo = new BooleanSetting("WatermarkLogo", "Logo", "Renders the client's logo before the watermark text, scaled to the text's own line height.", new CategorySetting.Visibility(this.watermarkCategory), true);
    public BooleanSetting watermarkVersion = new BooleanSetting("WatermarkVersion", "Version", "Renders the client's version after the name.", new CategorySetting.Visibility(this.watermarkCategory), true);
    public BooleanSetting watermarkMinecraftVersion = new BooleanSetting("WatermarkMinecraftVersion", "MinecraftVersion", "Renders the client's minecraft version after the version.", new CategorySetting.Visibility(this.watermarkCategory), false);
    public ModeSetting watermarkVersionMode = new ModeSetting("WatermarkVersionMode", "Revision", "What to render next to the version: the git revision/hash, or a nightly-build date stamp.", new BooleanSetting.Visibility(this.watermarkVersion, true), "GitHash", new String[]{"GitHash", "Nightly"});
    public BooleanSetting watermarkSync = new BooleanSetting("WatermarkSync", "ColorSync", "Uses the client's color for the version.", new CategorySetting.Visibility(this.watermarkCategory), false);
    public CategorySetting welcomerCategory = new CategorySetting("Welcomer", "The settings for the client's welcomer.");
    public BooleanSetting welcomer = new BooleanSetting("Welcomer", "Enabled", "Renders a nice welcome message directed to you.", new CategorySetting.Visibility(this.welcomerCategory), true);
    public StringSetting welcomerText = new StringSetting("WelcomerText", "Component", "The message that will be rendered.", new CategorySetting.Visibility(this.welcomerCategory), "Welcome back, [username]");
    public BooleanSetting welcomerSync = new BooleanSetting("WelcomerSync", "ColorSync", "Uses the client's color for the username.", new CategorySetting.Visibility(this.welcomerCategory), false);
    public CategorySetting moduleListCategory = new CategorySetting("ModuleList", "The settings for the client's list of enabled modules.");
    public BooleanSetting moduleList = new BooleanSetting("ModuleList", "Enabled", "Renders every enabled module in an organized list.", new CategorySetting.Visibility(this.moduleListCategory), true);
    public ModeSetting moduleListMode = new ModeSetting("ModuleListMode", "Mode", "The display mode for the module list.", new CategorySetting.Visibility(this.moduleListCategory), "Pro", new String[]{"Pro", "Newbie"});
    public BooleanSetting metaData = new BooleanSetting("MetaData", "Whether or not to show module metadata in the module list.", new CategorySetting.Visibility(this.moduleListCategory), true);
    public BooleanSetting bindOnly = new BooleanSetting("BindOnly", "Only lists modules that have a keybind set.", new CategorySetting.Visibility(this.moduleListCategory), false);
    public ModeSetting moduleColorMode = new ModeSetting("ModuleColor", "Color", "The color mode for the modules on the list.", new CategorySetting.Visibility(this.moduleListCategory), "Default", new String[]{"Default", "Rainbow", "Random"});
    public ModeSetting moduleListSorting = new ModeSetting("ModuleListSorting", "Sorting", "The sorting for the modules on the list.", new CategorySetting.Visibility(this.moduleListCategory), "Width", new String[]{"Width", "Alphabetical"});
    public CategorySetting playerRadarCategory = new CategorySetting("Player Radar", "The settings for the client's list of players in render distance.");
    public BooleanSetting playerRadar = new BooleanSetting("PlayerRadar", "Enabled", "Renders the name of every player in render distance.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public NumberSetting playerRadarLimit = new NumberSetting("PlayerRadarLimit", "Limit", "The maximum amount of players that will be listed. Setting it to 0 means removing the limiter.", new CategorySetting.Visibility(this.playerRadarCategory), 8, 0, 100);
    public ModeSetting playerRadarSorting = new ModeSetting("PlayerRadarSorting", "Sorting", "The sorting for the players on the radar.", new CategorySetting.Visibility(this.playerRadarCategory), "Distance", new String[]{"None", "Distance", "Alphabetical"});
    public BooleanSetting playerRadarAntiBot = new BooleanSetting("PlayerRadarAntiBot", "AntiBot", "Prevents bots from being listed on the radar.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerIcons = new BooleanSetting("PlayerIcons", "Icons", "Renders the player's head icon next to their name.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarDistance = new BooleanSetting("PlayerRadarDistance", "Distance", "Renders the distance between you and the player.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarEntityID = new BooleanSetting("PlayerRadarEntityID", "EntityID", "Renders the player's entity ID.", new CategorySetting.Visibility(this.playerRadarCategory), false);
    public BooleanSetting playerRadarGameMode = new BooleanSetting("PlayerRadarGameMode", "GameType", "Renders the player's current gamemode.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarPing = new BooleanSetting("PlayerRadarPing", "Ping", "Renders the player's current latency.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarHealth = new BooleanSetting("PlayerRadarHealth", "Health", "Renders the player's current health.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarTotems = new BooleanSetting("PlayerRadarTotems", "Totems", "Renders the amount of totems that the player has popped.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public BooleanSetting playerRadarTurtlePot = new BooleanSetting("PlayerRadarTurtlePot", "TurtlePotIndicator", "Indicates if the player has Resistance 3 or higher.", new CategorySetting.Visibility(this.playerRadarCategory), true);
    public CategorySetting itemsCategory = new CategorySetting("Items", "The settings for information about items in your inventory and specific item counters.");
    public BooleanSetting armor = new BooleanSetting("Armor", "Renders the armor you're currently wearing and its status.", new CategorySetting.Visibility(this.itemsCategory), true);
    public ModeSetting armorDurability = new ModeSetting("ArmorDurability", "Durability", "The way that the durability will be rendered in.", new BooleanSetting.Visibility(this.armor, true), "Both", new String[]{"None", "Bar", "Percentage", "Both"});
    public BooleanSetting totemCounter = new BooleanSetting("TotemCounter", "Renders the amount of totems that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
    public BooleanSetting crystalCounter = new BooleanSetting("CrystalCounter", "Renders the amount of crystals that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
    public BooleanSetting xpCounter = new BooleanSetting("XPCounter", "Renders the amount of totems that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
    public BooleanSetting counterChatOffset = new BooleanSetting("CounterChatOffset", "ChatOffset", "Offsets the crystal and XP counter's positions whenever the chat is open.", new CategorySetting.Visibility(this.itemsCategory), false);
    public BooleanSetting itemsSmooth = new BooleanSetting("ItemsSmooth", "Smooth", "Smoothly animates item counts when they change (sliding/lerping).", new CategorySetting.Visibility(this.itemsCategory), true);
    public CategorySetting inventoryCategory = new CategorySetting("Inventory", "The settings for displaying the inventory on the HUD.");
    public BooleanSetting inventory = new BooleanSetting("Inventory", "Enabled", "Renders your inventory (excluding hotbar) on the screen even when closed.", new CategorySetting.Visibility(this.inventoryCategory), false);
    public BooleanSetting inventoryBackground = new BooleanSetting("InventoryBackground", "Background", "Renders a dark background behind the inventory grid.", new CategorySetting.Visibility(this.inventoryCategory), true);
    public BooleanSetting inventorySlotBackground = new BooleanSetting("InventorySlotBackground", "SlotBoxes", "Renders subtle slot boxes for each inventory slot.", new CategorySetting.Visibility(this.inventoryCategory), true);
    public NumberSetting inventoryAlpha = new NumberSetting("InventoryAlpha", "Alpha", "Background opacity (0-255).", new CategorySetting.Visibility(this.inventoryCategory), 140.0, 0.0, 255.0);
    public PositionSetting inventoryPosition = new PositionSetting("InventoryPosition", "Drag offset for the inventory HUD element.");
    public CategorySetting informationCategory = new CategorySetting("Information", "The settings for information about the game and the client.");
    public BooleanSetting health = new BooleanSetting("Health", "Renders your current health in the middle of the screen.", new CategorySetting.Visibility(this.informationCategory), false);
    public BooleanSetting ping = new BooleanSetting("Ping", "Renders your current latency to the server in milliseconds.", new CategorySetting.Visibility(this.informationCategory), true);
    public BooleanSetting tps = new BooleanSetting("TPS", "Renders the server's current tick-rate.", new CategorySetting.Visibility(this.informationCategory), true);
    public BooleanSetting fps = new BooleanSetting("FPS", "Renders the game's frames per second counter.", new CategorySetting.Visibility(this.informationCategory), true);
    public BooleanSetting durability = new BooleanSetting("Durability", "Renders your held item durability.", new CategorySetting.Visibility(this.informationCategory), true);
    public ModeSetting speed = new ModeSetting("Speed", "Renders the speed that you are currently moving at.", new CategorySetting.Visibility(this.informationCategory), "Kilometers", new String[]{"None", "Meters", "Kilometers"});
    public BooleanSetting uptime = new BooleanSetting("Uptime", "Renders the uptime of the client.", new CategorySetting.Visibility(this.informationCategory), false);
    public BooleanSetting serverBrand = new BooleanSetting("ServerBrand", "Renders the brand of the server that you are currently on.", new CategorySetting.Visibility(this.informationCategory), false);
    public BooleanSetting informationSync = new BooleanSetting("InformationSync", "ColorSync", "Uses the client's color for the information elements.", new CategorySetting.Visibility(this.informationCategory), true);
    public BooleanSetting informationChatOffset = new BooleanSetting("InformationChatOffset", "ChatOffset", "Offsets the rendering when the chat is open.", new CategorySetting.Visibility(this.informationCategory), true);
    public CategorySetting potionsCategory = new CategorySetting("Potions", "The settings for information about potion effects and their status.");
    public BooleanSetting potions = new BooleanSetting("Potions", "Enabled", "Renders the name and status of every potion effect you have.", new CategorySetting.Visibility(this.potionsCategory), true);
    public BooleanSetting potionIcons = new BooleanSetting("PotionIcons", "Icons", "Whether or not to render the icons next to the potion's name.", new CategorySetting.Visibility(this.potionsCategory), true);
    public ModeSetting potionColor = new ModeSetting("PotionColor", "Color", "The color that will be used in rendering the potion's text.", new CategorySetting.Visibility(this.potionsCategory), "Enhanced", new String[]{"Vanilla", "Enhanced", "Client"});
    public ModeSetting potionSorting = new ModeSetting("PotionSorting", "Sorting", "The sorting for the potion effects rendered.", new CategorySetting.Visibility(this.potionsCategory), "Alphabetical", new String[]{"None", "Width", "Alphabetical"});
    public ModeSetting vanillaPotions = new ModeSetting("VanillaPotions", "The way that the vanilla potion icons will be handled.", new CategorySetting.Visibility(this.potionsCategory), "Hide", new String[]{"Keep", "Move", "Hide"});
    public CategorySetting positionCategory = new CategorySetting("Position", "The settings for information about your current position and velocity.");
    public BooleanSetting coordinates = new BooleanSetting("Coordinates", "Renders your current coordinates.", new CategorySetting.Visibility(this.positionCategory), true);
    public BooleanSetting netherCoordinates = new BooleanSetting("NetherCoordinates", "Renders your current coordinates in the alternate dimension.", new BooleanSetting.Visibility(this.coordinates, true), true);
    public BooleanSetting fakeCoords = new BooleanSetting("FakeCoords", "Fakes your coordinates to prevent leaking.", new CategorySetting.Visibility(this.positionCategory), false);
    public NumberSetting fakeThreshold = new NumberSetting("FakeThreshold", "Threshold", "Minimum coordinate value required to start faking.", new BooleanSetting.Visibility(this.fakeCoords, true), 1000, 0, 100000);
    public BooleanSetting direction = new BooleanSetting("Direction", "Renders the current direction that you are facing.", new CategorySetting.Visibility(this.positionCategory), true);
    public BooleanSetting positionSync = new BooleanSetting("PositionSync", "ColorSync", "Uses the client's color for the position elements.", new CategorySetting.Visibility(this.positionCategory), true);
    public BooleanSetting positionChatOffset = new BooleanSetting("PositionChatOffset", "ChatOffset", "Offsets the text when the chat is open.", new CategorySetting.Visibility(this.positionCategory), true);
    public CategorySetting colorCategory = new CategorySetting("Color", "The settings for the coloring of the text.");
    public ModeSetting colorMode = new ModeSetting("Color", "The color that will be applied to the text.", new CategorySetting.Visibility(this.colorCategory), "Default", new String[]{"Default", "Rainbow", "Wave", "Custom"});
    public ColorSetting customColor = new ColorSetting("CustomColor", "The color that will be used for the Custom mode.", new ModeSetting.Visibility(this.colorMode, "Custom"), ColorUtils.getDefaultColor());
    public ModeSetting rainbowMode = new ModeSetting("Rainbow", "The mode for the HUD Rainbow.", new ModeSetting.Visibility(this.colorMode, "Rainbow"), "Vertical", new String[]{"Vertical", "Horizontal"});
    public NumberSetting rainbowOffset = new NumberSetting("RainbowOffset", "Offset", "The offset that will be applied to the rainbow.", new ModeSetting.Visibility(this.colorMode, "Rainbow", "Wave"), 10L, 1L, 50L);
    public BooleanSetting inversion = new BooleanSetting("Inversion", "Inverts primary and secondary colors.", new CategorySetting.Visibility(this.colorCategory), false);
    public BooleanSetting textGlow = new BooleanSetting("TextGlow", "Glow", "Renders a bloom glow effect for HUD text.", new CategorySetting.Visibility(this.colorCategory), false);
    public BooleanSetting informationElement = new BooleanSetting("InformationElement", "Whether the ping/fps/tps/etc information block is shown at all. See HUDEditor.", true);
    public PositionSetting watermarkPosition = new PositionSetting("WatermarkPosition", "Drag offset for the watermark HUD element.");
    public PositionSetting welcomerPosition = new PositionSetting("WelcomerPosition", "Drag offset for the welcomer HUD element.");
    public PositionSetting moduleListPosition = new PositionSetting("ModuleListPosition", "Drag offset for the module list HUD element.");
    public PositionSetting playerRadarPosition = new PositionSetting("PlayerRadarPosition", "Drag offset for the player radar HUD element.");
    public PositionSetting armorPosition = new PositionSetting("ArmorPosition", "Drag offset for the armor HUD element.");
    public PositionSetting totemCounterPosition = new PositionSetting("TotemCounterPosition", "Drag offset for the totem counter HUD element.");
    public PositionSetting crystalCounterPosition = new PositionSetting("CrystalCounterPosition", "Drag offset for the crystal counter HUD element.");
    public PositionSetting xpCounterPosition = new PositionSetting("XPCounterPosition", "Drag offset for the XP counter HUD element.");
    public PositionSetting informationPosition = new PositionSetting("InformationPosition", "Drag offset for the information HUD element.");
    public PositionSetting coordinatesPosition = new PositionSetting("CoordinatesPosition", "Drag offset for the coordinates HUD element.");
    public PositionSetting potionsPosition = new PositionSetting("PotionsPosition", "Drag offset for the potions HUD element.");
    public PositionSetting musicHudPosition = new PositionSetting("MusicHUDPosition", "Drag offset for the Music HUD element.");
    public CategorySetting musicHudCategory = new CategorySetting("MusicHUD", "Settings for the Music HUD.");
    public BooleanSetting musicHud = new BooleanSetting("MusicHUD", "Enabled", "Toggle Music HUD display.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public NumberSetting musicHudBarAlpha = new NumberSetting("MusicHudBarAlpha", "BarAlpha", "Transparency of the visualizer bars (0-255).", new CategorySetting.Visibility(this.musicHudCategory), 160.0, 0.0, 255.0);
    public BooleanSetting musicHudGradientBars = new BooleanSetting("MusicHudGradientBars", "GradientBars", "Gradient colored visualizer bars.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public ModeSetting musicHudBgMode = new ModeSetting("MusicHudBackground", "Background", "Background style of the HUD.", new CategorySetting.Visibility(this.musicHudCategory), "LiquidGlass", new String[]{"LiquidGlass", "Blur", "Default"});
    public NumberSetting musicHudBlurIntensity = new NumberSetting("MusicHudBlurIntensity", "BlurIntensity", "Intensity of the blur effect.", new CategorySetting.Visibility(this.musicHudCategory), 3.0, 0.0, 50.0);
    public BooleanSetting musicHudCompactMode = new BooleanSetting("MusicHudCompactMode", "CompactMode", "Collapse the HUD, limiting its width.", new CategorySetting.Visibility(this.musicHudCategory), false);
    public BooleanSetting musicHudDisk = new BooleanSetting("MusicHudDisk", "Disk", "Enable to show the spinning record.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public BooleanSetting musicHudUltraDisk = new BooleanSetting("MusicHudUltraDisk", "UltraDisk", "Only display the record, hide everything else.", new CategorySetting.Visibility(this.musicHudCategory), false);
    public NumberSetting musicHudDiskSize = new NumberSetting("MusicHudDiskSize", "DiskSize", "Size of the music disk (Ultra Disk).", new CategorySetting.Visibility(this.musicHudCategory), 100.0, 30.0, 300.0);
    public BooleanSetting musicHudTextBloom = new BooleanSetting("MusicHudTextBloom", "TextBloom", "Text bloom glow effect.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public BooleanSetting musicHudTextBloomPlus = new BooleanSetting("MusicHudTextBloomPlus", "TextBloomPlus", "Enhanced Skia text bloom glow.", new CategorySetting.Visibility(this.musicHudCategory), true);
    public ColorSetting musicHudColor = new ColorSetting("MusicHudColor", "Color", "Accent color used for the visualizer.", new CategorySetting.Visibility(this.musicHudCategory), ColorUtils.getDefaultColor());
    public ModeSetting musicHudButtonEffect = new ModeSetting("MusicHudButtonEffect", "ButtonEffect", "Visual effect when clicking buttons.", new CategorySetting.Visibility(this.musicHudCategory), "Gradient", new String[]{"Splash", "Topo", "Gradient"});
    public static HUDModule INSTANCE;
    private final SmoothCounter totemSmoothCounter = new SmoothCounter();
    private final SmoothCounter crystalSmoothCounter = new SmoothCounter();
    private final SmoothCounter xpSmoothCounter = new SmoothCounter();
    private final Animation potionsAnimation = new Animation(300, Easing.Method.EASE_OUT_CUBIC);
    private final Animation chatAnimation = new Animation(300, Easing.Method.EASE_OUT_CUBIC);
    private float chatOffset;
    private List<ModuleEntry> moduleEntries = new ArrayList<ModuleEntry>();
    private List<PlayerEntry> playerEntries = new ArrayList<PlayerEntry>();
    private List<PotionEntry> potionEntries = new ArrayList<PotionEntry>();
    private final int fakeOffsetX = 2000000 + new Random().nextInt(8000000);
    private final int fakeOffsetZ = 2000000 + new Random().nextInt(8000000);
    private static final EquipmentSlot[] ARMOR_SLOTS;
    private static ItemStack[] dummyArmor;
    private final Map<Module, Animation> moduleColorAnimations = new HashMap<Module, Animation>();

    public static boolean isMusicAddonLoaded() {
        return FabricLoader.getInstance().isModLoaded("night-music") || Night.MODULE_MANAGER != null && Night.MODULE_MANAGER.getModule("PlayMusic") != null;
    }

    public HUDModule() {
        INSTANCE = this;
        HudElementRegistry.register("Watermark", this.watermark, this.watermarkPosition, this.watermarkCategory);
        HudElementRegistry.register("Welcomer", this.welcomer, this.welcomerPosition, this.welcomerCategory);
        HudElementRegistry.register("ModuleList", this.moduleList, this.moduleListPosition, this.moduleListCategory);
        HudElementRegistry.register("PlayerRadar", this.playerRadar, this.playerRadarPosition, this.playerRadarCategory);
        HudElementRegistry.register("Armor", this.armor, this.armorPosition);
        HudElementRegistry.register("TotemCounter", this.totemCounter, this.totemCounterPosition);
        HudElementRegistry.register("CrystalCounter", this.crystalCounter, this.crystalCounterPosition);
        HudElementRegistry.register("XPCounter", this.xpCounter, this.xpCounterPosition);
        HudElementRegistry.register("Inventory", this.inventory, this.inventoryPosition, this.inventoryCategory);
        HudElementRegistry.register("Information", this.informationElement, this.informationPosition, this.informationCategory);
        HudElementRegistry.register("Coordinates", this.coordinates, this.coordinatesPosition, this.positionCategory);
        HudElementRegistry.register("Potions", this.potions, this.potionsPosition, this.potionsCategory);
        this.musicHudPosition.set(20.0f, 60.0f);
        this.inventoryPosition.set(20.0f, 120.0f);
    }

    private int getFakedCoord(int coord, int offset) {
        if (!this.fakeCoords.getValue()) {
            return coord;
        }
        if (Math.abs(coord) < this.fakeThreshold.getValue().intValue()) {
            return coord;
        }
        long baseCoord = (long)offset + (long)Math.abs(coord) / 3L;
        boolean negate = (Math.abs(coord) * 31 + offset) % 2 == 0;
        long fakedCoord = negate ? -baseCoord : baseCoord;
        long result = fakedCoord;
        if (HUDModule.mc.player != null && HUDModule.mc.player.level() != null && HUDModule.mc.player.level().dimension() == Level.NETHER) {
            fakedCoord /= 8L;
        }
        return (int)fakedCoord;
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent settingChangeEvent) {
        if (settingChangeEvent.getSetting() == this.textGlow) {
            FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
            FontModule fontModule2 = fontModule;
            if (fontModule != null && fontModule.glow.getValue() != this.textGlow.getValue()) {
                fontModule.glow.setValue(this.textGlow.getValue());
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (HUDModule.mc.player != null && HUDModule.mc.level != null) {
            if (this.moduleList.getValue()) {
                Comparator<Module> modCompWidth = Comparator.comparingInt(module -> -Night.FONT_MANAGER.getWidth(HudListStyle.moduleText(this, module)));
                Comparator<Module> modCompName = Comparator.comparing(Module::getName);
                boolean isNewbie = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
                List<ModuleEntry> entries = new ArrayList<ModuleEntry>();
                for (Module module2 : Night.MODULE_MANAGER.getModules().stream().filter(module -> module.drawn.getValue()).filter(module -> {
                    if (isNewbie) {
                        return module.getBind() != 0 ? true : !this.bindOnly.getValue() && (module.isToggled() || module.getAnimationOffset().value() > 0.01f);
                    }
                    return this.bindOnly.getValue() && module.getBind() == 0 ? false : module.isToggled() || module.getAnimationOffset().value() > 0.01f;
                }).sorted(this.moduleListSorting.getValue().equalsIgnoreCase("Width") ? modCompWidth : modCompName).toList()) {
                    String modText = HudListStyle.moduleText(this, module2);
                    entries.add(new ModuleEntry(module2, modText));
                }
                this.moduleEntries = entries;
            }
            if (this.playerRadar.getValue()) {
                Comparator<Player> playerCompDist = Comparator.comparingDouble(player -> HUDModule.mc.player.distanceTo((Entity)player));
                Comparator<Player> playerCompName = Comparator.comparing(player -> player.getName().getString());
                ArrayList<PlayerEntry> radarEntries = new ArrayList<PlayerEntry>();
                for (Player player : HUDModule.mc.level.players().stream().filter(candidate -> candidate != HUDModule.mc.player).filter(candidate -> !EntityUtils.isGhost((Entity)candidate)).filter(candidate -> !this.playerRadarAntiBot.getValue() || !EntityUtils.isBot(candidate)).filter(candidate -> !Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled() || !EntityUtils.isNaked(candidate)).sorted(this.playerRadarSorting.getValue().equalsIgnoreCase("Distance") ? playerCompDist : playerCompName).limit(this.playerRadarLimit.getValue().longValue()).toList()) {
                    Identifier skinId = null;
                    MobEffectInstance resistanceEffect;
                    PlayerInfo playerInfo;
                    if (this.playerIcons.getValue() && mc.getConnection() != null && (playerInfo = mc.getConnection().getPlayerInfo(player.getName().getString())) != null) {
                        skinId = playerInfo.getSkin().body().texturePath();
                    }
                    String text = player.getName().getString();
                    if (this.playerRadarDistance.getValue()) {
                        text = text + String.valueOf(ChatFormatting.WHITE) + " " + new DecimalFormat("0.0").format(HUDModule.mc.player.distanceTo((Entity)player));
                    }
                    if (this.playerRadarEntityID.getValue()) {
                        text = text + String.valueOf(ChatFormatting.WHITE) + " " + player.getId();
                    }
                    if (this.playerRadarGameMode.getValue()) {
                        text = text + String.valueOf(ChatFormatting.WHITE) + " [" + EntityUtils.getGameModeName(EntityUtils.getGameMode(player)) + "]";
                    }
                    if (this.playerRadarPing.getValue()) {
                        text = text + String.valueOf(ChatFormatting.WHITE) + " " + EntityUtils.getLatency(player) + "ms";
                    }
                    if (this.playerRadarHealth.getValue()) {
                        text = text + String.valueOf(ColorUtils.getHealthColor(player.getHealth() + player.getAbsorptionAmount())) + " " + new DecimalFormat("0.0").format(player.getHealth() + player.getAbsorptionAmount()) + String.valueOf(ChatFormatting.RESET);
                    }
                    int poppedTotems = Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(player.getUUID(), 0);
                    if (this.playerRadarTotems.getValue() && poppedTotems > 0) {
                        text = text + String.valueOf(ColorUtils.getTotemColor(poppedTotems)) + " -" + poppedTotems + String.valueOf(ChatFormatting.RESET);
                    }
                    if (this.playerRadarTurtlePot.getValue() && (resistanceEffect = player.getEffect(MobEffects.RESISTANCE)) != null && resistanceEffect.getAmplifier() >= 2) {
                        text = text + String.valueOf(ChatFormatting.RED) + " [TM]" + String.valueOf(ChatFormatting.RESET);
                    }
                    radarEntries.add(new PlayerEntry(player, text, skinId));
                }
                this.playerEntries = radarEntries;
            }
            if (this.potions.getValue()) {
                Comparator<MobEffectInstance> potionCompWidth = Comparator.comparingInt(mobEffectInstance -> -Night.FONT_MANAGER.getWidth(((MobEffect)mobEffectInstance.getEffect().value()).getDisplayName().getString() + " " + (mobEffectInstance.getAmplifier() + 1)));
                Comparator<MobEffectInstance> potionCompName = Comparator.comparing(mobEffectInstance -> ((MobEffect)mobEffectInstance.getEffect().value()).getDisplayName().getString());
                ArrayList<PotionEntry> activePotions = new ArrayList<PotionEntry>();
                for (MobEffectInstance effectInstance : HUDModule.mc.player.getActiveEffects().stream().sorted(this.potionSorting.getValue().equalsIgnoreCase("Width") ? potionCompWidth : potionCompName).toList()) {
                    String potionText = this.getPotionText(effectInstance);
                    Identifier sprite = null;
                    if (this.potionIcons.getValue()) {
                        sprite = Hud.getMobEffectSprite((Holder)effectInstance.getEffect());
                    }
                    activePotions.add(new PotionEntry(potionText, sprite, this.potionColor.getValue().equalsIgnoreCase("Vanilla") ? new Color(((MobEffect)effectInstance.getEffect().value()).getColor()) : (this.potionColor.getValue().equalsIgnoreCase("Enhanced") ? (EntityUtils.POTION_COLORS.containsKey(effectInstance.getEffect().value()) ? EntityUtils.POTION_COLORS.get(effectInstance.getEffect().value()) : new Color(((MobEffect)effectInstance.getEffect().value()).getColor())) : null)));
                }
                this.potionEntries = activePotions;
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        FontManager.setHudRendering(true);
        try {
            this.chatOffset = this.chatAnimation.get(HUDModule.mc.gui.screen() instanceof ChatScreen ? 14.0f : 0.0f);
            Renderer2D.renderQuad(renderOverlayEvent.getContext(), 2.0f, (float)mc.getWindow().getGuiScaledHeight() - this.chatOffset, mc.getWindow().getGuiScaledWidth() - 2, (float)(mc.getWindow().getGuiScaledHeight() + 12) - this.chatOffset, new Color(0, 0, 0, (int)((Double)HUDModule.mc.options.textBackgroundOpacity().get() * 255.0)));
            this.renderWatermark(renderOverlayEvent);
            this.renderModuleList(renderOverlayEvent);
            this.renderPlayerRadar(renderOverlayEvent);
            this.renderItems(renderOverlayEvent);
            this.renderInventory(renderOverlayEvent);
            this.renderPotions(renderOverlayEvent);
            this.renderInformation(renderOverlayEvent);
            this.renderCoordinates(renderOverlayEvent);
        }
        finally {
            FontManager.setHudRendering(false);
        }
    }

    private void renderWatermark(RenderOverlayEvent renderOverlayEvent) {
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        if (this.watermark.getValue() || this.uptime.getValue()) {
            String[] uptimeParts;
            matrixStack.pushMatrix();
            matrixStack.translate(this.watermarkPosition.getX(), this.watermarkPosition.getY());
            int maxWidth = 0;
            int lineCount = 0;
            if (this.watermark.getValue()) {
                boolean hideVersion = true;
                String versionNum = hideVersion ? "" : "26";
                String revisionSuffix = !hideVersion || !this.watermarkVersion.getValue() ? "" : (!this.watermarkVersionMode.getValue().equals("GitHash") ? String.valueOf(ChatFormatting.GRAY) : "+436.eeeb439b78");
                String watermarkStr = this.watermarkText.getValue() + (String)(this.watermarkVersion.getValue() ? String.valueOf(this.watermarkSync.getValue() ? "" : (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE)) + " " + versionNum + (this.watermarkMinecraftVersion.getValue() ? "-mc26.2" : "") + revisionSuffix : "");
                float textX = 2.0f;
                if (this.watermarkLogo.getValue()) {
                    int logoSize = Night.FONT_MANAGER.getHeight();
                    int logoColor = this.getHudColor(2.0f).getRGB();
                    renderOverlayEvent.getContext().blit(RenderPipelines.GUI_TEXTURED, LOGO_ID, 2, 2, 0.0f, 0.0f, logoSize, logoSize, 64, 64, 64, 64, logoColor);
                    textX = 2 + logoSize + 3;
                }
                this.drawText(renderOverlayEvent.getContext(), watermarkStr, textX, 2.0f);
                maxWidth = Math.max(maxWidth, (int)(textX - 2.0f) + Night.FONT_MANAGER.getWidth(watermarkStr));
                ++lineCount;
            }
            if (this.uptime.getValue()) {
                uptimeParts = FormattingUtils.formatSeconds((System.currentTimeMillis() - Night.UPTIME) / 1000L);
                String uptimeText = "Uptime " + String.valueOf(ChatFormatting.WHITE) + uptimeParts[0] + ":" + uptimeParts[1] + ":" + uptimeParts[2];
                this.drawText(renderOverlayEvent.getContext(), uptimeText, 2.0f, 2 + (this.watermark.getValue() ? Night.FONT_MANAGER.getHeight() : 0), this.informationSync.getValue() ? null : new Color(170, 170, 170));
                maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(uptimeText));
                ++lineCount;
            }
            HudElementRegistry.reportBounds("Watermark", 2.0f, 2.0f, 2 + maxWidth, 2 + lineCount * Night.FONT_MANAGER.getHeight());
            matrixStack.popMatrix();
        }
        if (this.welcomer.getValue()) {
            matrixStack.pushMatrix();
            matrixStack.translate(this.welcomerPosition.getX(), this.welcomerPosition.getY());
            String welcomeText = this.welcomerText.getValue().replace("[username]", String.valueOf(this.welcomerSync.getValue() ? "" : (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE)) + HUDModule.mc.player.getName().getString() + String.valueOf(ChatFormatting.RESET));
            float welcomeX = (float)mc.getWindow().getGuiScaledWidth() / 2.0f - (float)Night.FONT_MANAGER.getWidth(welcomeText) / 2.0f;
            this.drawText(renderOverlayEvent.getContext(), welcomeText, welcomeX, 6.0f);
            HudElementRegistry.reportBounds("Welcomer", welcomeX, 6.0f, welcomeX + (float)Night.FONT_MANAGER.getWidth(welcomeText), 6 + Night.FONT_MANAGER.getHeight());
            matrixStack.popMatrix();
        }
    }

    private void renderModuleList(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null || HUDModule.mc.level == null) {
            return;
        }
        if (!this.moduleList.getValue()) {
            return;
        }
        float potionOffset = this.potionsAnimation.get(!this.vanillaPotions.getValue().equalsIgnoreCase("Move") || HUDModule.mc.player.getActiveEffects().isEmpty() ? 0.0f : (float)(EntityUtils.hasNegativeEffects((Player)HUDModule.mc.player) ? 51 : 25));
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.moduleListPosition.getX(), this.moduleListPosition.getY());
        boolean isNewbie = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        int maxWidth = 0;
        int index = 0;
        for (ModuleEntry moduleEntry : this.moduleEntries) {
            boolean active = moduleEntry.module().isToggled() || isNewbie && moduleEntry.module().getBind() != 0;
            float targetOffset = active ? (float)(Night.FONT_MANAGER.getWidth(moduleEntry.text()) + 2) : 0.0f;
            float drawX = (float)mc.getWindow().getGuiScaledWidth() - moduleEntry.module().getAnimationOffset().get(targetOffset);
            float drawY = 2.0f + potionOffset + (float)(index * Night.FONT_MANAGER.getHeight());
            this.drawModuleText(moduleEntry.module(), renderOverlayEvent.getContext(), moduleEntry.text(), drawX, drawY);
            maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(moduleEntry.text()) + 2);
            ++index;
        }
        if (!this.moduleEntries.isEmpty()) {
            float screenWidth = mc.getWindow().getGuiScaledWidth();
            HudElementRegistry.reportBounds("ModuleList", screenWidth - (float)maxWidth, 2.0f + potionOffset, screenWidth, 2.0f + potionOffset + (float)(this.moduleEntries.size() * Night.FONT_MANAGER.getHeight()));
        }
        matrixStack.popMatrix();
    }

    private void renderPlayerRadar(RenderOverlayEvent renderOverlayEvent) {
        if (!this.playerRadar.getValue()) {
            return;
        }
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.playerRadarPosition.getX(), this.playerRadarPosition.getY());
        int maxWidth = 0;
        int index = 0;
        for (PlayerEntry playerEntry : this.playerEntries) {
            if (playerEntry.headTexture() != null) {
                PlayerFaceExtractor.extractRenderState((GuiGraphicsExtractor)renderOverlayEvent.getContext(), (Identifier)playerEntry.headTexture(), (int)2, (int)(1 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * index), (int)Night.FONT_MANAGER.getHeight(), (boolean)true, (boolean)false, (int)Color.WHITE.getRGB());
            }
            int textX = 2 + (playerEntry.headTexture() != null ? Night.FONT_MANAGER.getHeight() + 2 : 0);
            this.drawText(renderOverlayEvent.getContext(), playerEntry.text(), textX, 2 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * index, Night.FRIEND_MANAGER.contains(playerEntry.player().getName().getString()) ? Night.FRIEND_MANAGER.getDefaultFriendColor() : null);
            maxWidth = Math.max(maxWidth, textX + Night.FONT_MANAGER.getWidth(playerEntry.text()));
            ++index;
        }
        if (!this.playerEntries.isEmpty()) {
            HudElementRegistry.reportBounds("PlayerRadar", 2.0f, 1 + Night.FONT_MANAGER.getHeight() * 2, maxWidth, 1 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * this.playerEntries.size());
        }
        matrixStack.popMatrix();
    }

    private static int countItem(Player player, Item item) {
        if (player == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
            if (!itemStack.is(item)) continue;
            count += itemStack.getCount();
        }
        if (player.getOffhandItem().is(item)) {
            count += player.getOffhandItem().getCount();
        }
        return count;
    }

    private static ItemStack[] dummyArmor() {
        if (dummyArmor == null) {
            dummyArmor = new ItemStack[]{new ItemStack((ItemLike)Items.NETHERITE_BOOTS), new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS), new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE), new ItemStack((ItemLike)Items.NETHERITE_HELMET)};
        }
        return dummyArmor;
    }

    private void renderItems(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        this.renderArmor(renderOverlayEvent);
        this.renderTotemCounter(renderOverlayEvent);
        this.renderCrystalCounter(renderOverlayEvent);
        this.renderXPCounter(renderOverlayEvent);
    }

    private void renderArmor(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean inHudEditor = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        int airOffset = HUDModule.mc.player.isEyeInFluid(FluidTags.WATER) || HUDModule.mc.player.getAirSupply() < HUDModule.mc.player.getMaxAirSupply() ? 10 : 0;
        int baseX = mc.getWindow().getGuiScaledWidth() / 2 + 15;
        int baseY = mc.getWindow().getGuiScaledHeight() - 55 - airOffset;
        HudElementRegistry.reportBounds("Armor", baseX - 1, baseY - 3, baseX + 71, baseY + 17);
        if (!this.armor.getValue() && !inHudEditor) {
            return;
        }
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.armorPosition.getX(), this.armorPosition.getY());
        boolean hasArmor = false;
        for (EquipmentSlot equipmentSlot : ARMOR_SLOTS) {
            if (HUDModule.mc.player.getItemBySlot(equipmentSlot).isEmpty()) continue;
            hasArmor = true;
            break;
        }
        if (hasArmor && this.armor.getValue()) {
            int slotIndex = 0;
            for (EquipmentSlot equipmentSlot : ARMOR_SLOTS) {
                ItemStack itemStack = HUDModule.mc.player.getItemBySlot(equipmentSlot);
                if (itemStack.isEmpty()) continue;
                int itemX = mc.getWindow().getGuiScaledWidth() / 2 + 69 - 18 * slotIndex;
                int itemY = baseY;
                renderOverlayEvent.getContext().item(itemStack, itemX, itemY);
                if (this.armorDurability.getValue().equalsIgnoreCase("Bar") || this.armorDurability.getValue().equalsIgnoreCase("Both")) {
                    renderOverlayEvent.getContext().itemDecorations(HUDModule.mc.font, itemStack, itemX, itemY);
                }
                int damageValue = itemStack.getDamageValue();
                int maxDamage = itemStack.getMaxDamage();
                if ((this.armorDurability.getValue().equalsIgnoreCase("Percentage") || this.armorDurability.getValue().equalsIgnoreCase("Both")) && maxDamage > 0) {
                    matrixStack.pushMatrix();
                    matrixStack.scale(0.625f, 0.625f);
                    this.drawText(renderOverlayEvent.getContext(), (maxDamage - damageValue) * 100 / maxDamage + "%", (int)((float)((mc.getWindow().getGuiScaledWidth() >> 1) + 70 - 18 * slotIndex) * 1.6f), (int)((float)(baseY - 3) * 1.6f - 5.0f), false, new Color(1.0f - (float)(maxDamage - damageValue) / (float)maxDamage, (float)(maxDamage - damageValue) / (float)maxDamage, 0.0f));
                    matrixStack.popMatrix();
                }
                ++slotIndex;
            }
        } else if (inHudEditor) {
            ItemStack[] previewArmor = HUDModule.dummyArmor();
            for (int i = 0; i < previewArmor.length; ++i) {
                int itemX = mc.getWindow().getGuiScaledWidth() / 2 + 69 - 18 * i;
                int itemY = baseY;
                renderOverlayEvent.getContext().item(previewArmor[i], itemX, itemY);
            }
        }
        matrixStack.popMatrix();
    }

    private void renderTotemCounter(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean inHudEditor = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float posX = (float)mc.getWindow().getGuiScaledWidth() / 2.0f - 9.0f;
        float posY = mc.getWindow().getGuiScaledHeight() - 55 - (HUDModule.mc.player.isEyeInFluid(FluidTags.WATER) && HUDModule.mc.gameMode != null && HUDModule.mc.gameMode.getPlayerMode() != GameType.CREATIVE ? 10 : 0);
        HudElementRegistry.reportBounds("TotemCounter", posX - 1.0f, posY - 1.0f, posX + 19.0f, posY + 19.0f);
        if (!this.totemCounter.getValue() && !inHudEditor) {
            return;
        }
        int totemCount = HUDModule.countItem((Player)HUDModule.mc.player, Items.TOTEM_OF_UNDYING);
        this.totemSmoothCounter.update(totemCount);
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.totemCounterPosition.getX(), this.totemCounterPosition.getY());
        if (this.totemCounter.getValue() && (totemCount > 0 || this.totemSmoothCounter.lastCount > 0)) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING);
            matrixStack.pushMatrix();
            matrixStack.translate(posX, posY);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            this.totemSmoothCounter.render(renderOverlayEvent.getContext(), this.itemsSmooth.getValue());
            matrixStack.popMatrix();
        } else if (inHudEditor) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING);
            matrixStack.pushMatrix();
            matrixStack.translate(posX, posY);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(renderOverlayEvent.getContext(), "1", 17 - Night.FONT_MANAGER.getWidth("1"), 9, Color.WHITE);
            } else {
                renderOverlayEvent.getContext().text(HUDModule.mc.font, "1", 17 - HUDModule.mc.font.width("1"), 9, -1, true);
            }
            matrixStack.popMatrix();
        }
        matrixStack.popMatrix();
    }

    private void renderCrystalCounter(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean inHudEditor = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float posX = (float)mc.getWindow().getGuiScaledWidth() / 2.0f + 106.0f;
        float posY = (float)(mc.getWindow().getGuiScaledHeight() - 40) - (this.counterChatOffset.getValue() ? this.chatOffset : 0.0f);
        HudElementRegistry.reportBounds("CrystalCounter", posX - 1.0f, posY - 1.0f, posX + 19.0f, posY + 19.0f);
        if (!this.crystalCounter.getValue() && !inHudEditor) {
            return;
        }
        int crystalCount = HUDModule.countItem((Player)HUDModule.mc.player, Items.END_CRYSTAL);
        this.crystalSmoothCounter.update(crystalCount);
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.crystalCounterPosition.getX(), this.crystalCounterPosition.getY());
        if (this.crystalCounter.getValue() && (crystalCount > 0 || this.crystalSmoothCounter.lastCount > 0)) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.END_CRYSTAL);
            matrixStack.pushMatrix();
            matrixStack.translate(posX, posY);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            this.crystalSmoothCounter.render(renderOverlayEvent.getContext(), this.itemsSmooth.getValue());
            matrixStack.popMatrix();
        } else if (inHudEditor) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.END_CRYSTAL);
            matrixStack.pushMatrix();
            matrixStack.translate(posX, posY);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(renderOverlayEvent.getContext(), "64", 17 - Night.FONT_MANAGER.getWidth("64"), 9, Color.WHITE);
            } else {
                renderOverlayEvent.getContext().text(HUDModule.mc.font, "64", 17 - HUDModule.mc.font.width("64"), 9, -1, true);
            }
            matrixStack.popMatrix();
        }
        matrixStack.popMatrix();
    }

    private void renderXPCounter(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean inHudEditor = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float posX = (float)mc.getWindow().getGuiScaledWidth() / 2.0f + 106.0f;
        float posY = (float)(mc.getWindow().getGuiScaledHeight() - 20) - (this.counterChatOffset.getValue() ? this.chatOffset : 0.0f);
        HudElementRegistry.reportBounds("XPCounter", posX - 1.0f, posY - 1.0f, posX + 19.0f, posY + 19.0f);
        if (!this.xpCounter.getValue() && !inHudEditor) {
            return;
        }
        int xpCount = HUDModule.countItem((Player)HUDModule.mc.player, Items.EXPERIENCE_BOTTLE);
        this.xpSmoothCounter.update(xpCount);
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.xpCounterPosition.getX(), this.xpCounterPosition.getY());
        if (this.xpCounter.getValue() && (xpCount > 0 || this.xpSmoothCounter.lastCount > 0)) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
            matrixStack.pushMatrix();
            matrixStack.translate(posX, posY);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            this.xpSmoothCounter.render(renderOverlayEvent.getContext(), this.itemsSmooth.getValue());
            matrixStack.popMatrix();
        } else if (inHudEditor) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
            matrixStack.pushMatrix();
            matrixStack.translate(posX, posY);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(renderOverlayEvent.getContext(), "64", 17 - Night.FONT_MANAGER.getWidth("64"), 9, Color.WHITE);
            } else {
                renderOverlayEvent.getContext().text(HUDModule.mc.font, "64", 17 - HUDModule.mc.font.width("64"), 9, -1, true);
            }
            matrixStack.popMatrix();
        }
        matrixStack.popMatrix();
    }

    private void renderInventory(RenderOverlayEvent renderOverlayEvent) {
        Color bgColor;
        int alpha;
        if (HUDModule.mc.player == null) {
            return;
        }
        if (!this.inventory.getValue()) {
            return;
        }
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.inventoryPosition.getX(), this.inventoryPosition.getY());
        int padding = 3;
        int slotSize = 18;
        int totalWidth = 9 * slotSize + padding * 2;
        int totalHeight = 3 * slotSize + padding * 2;
        HudElementRegistry.reportBounds("Inventory", 0.0f, 0.0f, totalWidth, totalHeight);
        if (this.inventoryBackground.getValue()) {
            alpha = (int)Math.clamp(this.inventoryAlpha.getValue().floatValue(), 0.0f, 255.0f);
            bgColor = new Color(15, 15, 18, alpha);
            Color outlineColor = new Color(255, 255, 255, Math.min(alpha, 30));
            Renderer2D.renderQuad(renderOverlayEvent.getContext(), 0.0f, 0.0f, totalWidth, totalHeight, bgColor);
            Renderer2D.renderOutline(renderOverlayEvent.getContext(), 0.0f, 0.0f, totalWidth, totalHeight, outlineColor);
        }
        for (int slotIndex = 9; slotIndex <= 35; ++slotIndex) {
            int col = (slotIndex - 9) % 9;
            int row = (slotIndex - 9) / 9;
            int slotX = padding + col * slotSize;
            int slotY = padding + row * slotSize;
            if (this.inventorySlotBackground.getValue()) {
                Color slotBgColor = new Color(255, 255, 255, 10);
                Renderer2D.renderQuad(renderOverlayEvent.getContext(), slotX, slotY, slotX + 18, slotY + 18, slotBgColor);
            }
            ItemStack itemStack = HUDModule.mc.player.getInventory().getItem(slotIndex);
            if (itemStack.isEmpty()) continue;
            renderOverlayEvent.getContext().item(itemStack, slotX + 1, slotY + 1);
            renderOverlayEvent.getContext().itemDecorations(HUDModule.mc.font, itemStack, slotX + 1, slotY + 1);
        }
        matrixStack.popMatrix();
    }

    private void renderPotions(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        if (!this.potions.getValue()) {
            return;
        }
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.potionsPosition.getX(), this.potionsPosition.getY());
        float bottomOffset = this.informationChatOffset.getValue() ? this.chatOffset : 0.0f;
        int index = 0;
        int maxWidth = 0;
        for (PotionEntry potionEntry : this.potionEntries) {
            int textWidth = Night.FONT_MANAGER.getWidth(potionEntry.text());
            int entryWidth = textWidth + (potionEntry.sprite() != null ? Night.FONT_MANAGER.getHeight() + 2 : 0);
            maxWidth = Math.max(maxWidth, entryWidth);
            int drawX = mc.getWindow().getGuiScaledWidth() - 2 - textWidth;
            int drawY = mc.getWindow().getGuiScaledHeight() - (int)bottomOffset - 2 - Night.FONT_MANAGER.getHeight() - Night.FONT_MANAGER.getHeight() * index;
            if (potionEntry.sprite() != null) {
                matrixStack.pushMatrix();
                matrixStack.translate((float)(drawX - Night.FONT_MANAGER.getHeight() - 2), (float)(drawY - 1));
                renderOverlayEvent.getContext().blitSprite(RenderPipelines.GUI_TEXTURED, potionEntry.sprite(), 0, 0, Night.FONT_MANAGER.getHeight(), Night.FONT_MANAGER.getHeight());
                matrixStack.popMatrix();
            }
            this.drawText(renderOverlayEvent.getContext(), potionEntry.text(), drawX, drawY, this.potionColor.getValue().equals("Client") && this.colorMode.getValue().equals("Rainbow") && this.rainbowMode.getValue().equals("Horizontal"), potionEntry.color());
            ++index;
        }
        if (index > 0) {
            float screenWidth = mc.getWindow().getGuiScaledWidth();
            float bottomY = (float)mc.getWindow().getGuiScaledHeight() - bottomOffset;
            HudElementRegistry.reportBounds("Potions", screenWidth - 2.0f - (float)maxWidth - 4.0f, bottomY - 2.0f - (float)(index * Night.FONT_MANAGER.getHeight()), screenWidth, bottomY);
        }
        matrixStack.popMatrix();
    }

    private void renderInformation(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player != null && this.informationElement.getValue()) {
            float bottomOffset;
            Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
            matrixStack.pushMatrix();
            matrixStack.translate(this.informationPosition.getX(), this.informationPosition.getY());
            int lineIndex = 0;
            float unusedOffset = bottomOffset = this.informationChatOffset.getValue() ? this.chatOffset : 0.0f;
            if (this.health.getValue()) {
                String healthStr = new DecimalFormat("0").format(HUDModule.mc.player.getHealth() + HUDModule.mc.player.getAbsorptionAmount());
                Color color = new Color(TextColor.fromLegacyFormat((ChatFormatting)ColorUtils.getHealthColor(HUDModule.mc.player.getHealth() + HUDModule.mc.player.getAbsorptionAmount())).getValue());
                Night.FONT_MANAGER.drawTextWithOutline(renderOverlayEvent.getContext(), healthStr, mc.getWindow().getGuiScaledWidth() / 2 - Night.FONT_MANAGER.getWidth(healthStr) / 2, mc.getWindow().getGuiScaledHeight() / 2 + 16, color, Color.BLACK);
            }
            List<String> infoList = new ArrayList<String>();
            if (this.ping.getValue()) {
                infoList.add(String.valueOf(this.getPrimary()) + "Ping " + String.valueOf(this.getSecondary()) + Night.SERVER_MANAGER.getPing() + "ms");
            }
            if (this.fps.getValue()) {
                infoList.add(String.valueOf(this.getPrimary()) + "FPS " + String.valueOf(this.getSecondary()) + Night.RENDER_MANAGER.getFps());
            }
            if (this.durability.getValue()) {
                infoList.add("Durability " + (HUDModule.mc.player.getMainHandItem().getMaxDamage() - HUDModule.mc.player.getMainHandItem().getDamageValue()));
            }
            if (!this.speed.getValue().equalsIgnoreCase("None")) {
                infoList.add(String.valueOf(this.getPrimary()) + "Speed " + String.valueOf(this.getSecondary()) + new DecimalFormat("0.00").format(EntityUtils.getSpeed((Entity)HUDModule.mc.player, this.speed.getValue().equalsIgnoreCase("Meters") ? EntityUtils.SpeedUnit.METERS : EntityUtils.SpeedUnit.KILOMETERS)) + (this.speed.getValue().equalsIgnoreCase("Meters") ? "m/s" : "km/h"));
            }
            if (this.serverBrand.getValue()) {
                infoList.add(String.valueOf(this.getPrimary()) + "Brand " + String.valueOf(this.getSecondary()) + Night.SERVER_MANAGER.getServerBrand());
            }
            float tickRate = Night.SERVER_MANAGER.getTickRate();
            if (this.tps.getValue()) {
                infoList.add(String.valueOf(this.getPrimary()) + "TPS " + String.valueOf(this.getSecondary()) + ((double)tickRate > 19.79 ? "20.00" : new DecimalFormat("00.00").format(tickRate)));
            }
            if (!infoList.isEmpty()) {
                infoList.sort(Comparator.comparingInt(Night.FONT_MANAGER::getWidth).reversed());
                for (String infoText : infoList) {
                    if (infoText.startsWith("Durability")) {
                        if (!HUDModule.mc.player.getMainHandItem().isDamageableItem()) continue;
                        int maxDamage = HUDModule.mc.player.getMainHandItem().getMaxDamage();
                        int damageValue = HUDModule.mc.player.getMainHandItem().getDamageValue();
                        String durabilityStr = String.valueOf(maxDamage - damageValue);
                        this.drawText(renderOverlayEvent.getContext(), String.valueOf(this.getPrimary()) + "Durability ", mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth("Durability ") - Night.FONT_MANAGER.getWidth(durabilityStr), (float)mc.getWindow().getGuiScaledHeight() - bottomOffset - 2.0f - (float)Night.FONT_MANAGER.getHeight() + (float)(lineIndex * -Night.FONT_MANAGER.getHeight()), this.informationSync.getValue() ? null : new Color(170, 170, 170));
                        this.drawText(renderOverlayEvent.getContext(), durabilityStr, mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth(durabilityStr), (float)mc.getWindow().getGuiScaledHeight() - bottomOffset - 2.0f - (float)Night.FONT_MANAGER.getHeight() + (float)(lineIndex * -Night.FONT_MANAGER.getHeight()), false, new Color(1.0f - (float)(maxDamage - damageValue) / (float)maxDamage, (float)(maxDamage - damageValue) / (float)maxDamage, 0.0f));
                        ++lineIndex;
                        continue;
                    }
                    this.drawText(renderOverlayEvent.getContext(), infoText, mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth(infoText), (float)mc.getWindow().getGuiScaledHeight() - bottomOffset - 2.0f - (float)Night.FONT_MANAGER.getHeight() + (float)(lineIndex * -Night.FONT_MANAGER.getHeight()), this.informationSync.getValue() ? null : new Color(170, 170, 170));
                    ++lineIndex;
                }
                int maxWidth = 0;
                for (String infoText : infoList) {
                    maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(infoText));
                }
                float screenWidth = mc.getWindow().getGuiScaledWidth();
                float bottomY = (float)mc.getWindow().getGuiScaledHeight() - bottomOffset;
                HudElementRegistry.reportBounds("Information", screenWidth - 2.0f - (float)maxWidth, bottomY - 2.0f - (float)(lineIndex * Night.FONT_MANAGER.getHeight()), screenWidth, bottomY);
            }
            matrixStack.popMatrix();
        }
    }

    private void renderCoordinates(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null || HUDModule.mc.level == null) {
            return;
        }
        if (!this.coordinates.getValue() && !this.direction.getValue()) {
            return;
        }
        Matrix3x2fStack matrixStack = renderOverlayEvent.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(this.coordinatesPosition.getX(), this.coordinatesPosition.getY());
        float bottomOffset = this.positionChatOffset.getValue() ? this.chatOffset : 0.0f;
        int yOffset = 0;
        int maxWidth = 0;
        int lineCount = 0;
        if (this.coordinates.getValue()) {
            int coordX = this.getFakedCoord(HUDModule.mc.player.getBlockX(), this.fakeOffsetX);
            int coordZ = this.getFakedCoord(HUDModule.mc.player.getBlockZ(), this.fakeOffsetZ);
            String coordsText = String.valueOf(this.getSecondary()) + String.valueOf(coordX) + (String)(this.netherCoordinates.getValue() ? String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(this.getSecondary()) + WorldUtils.getNetherPosition(coordX) + String.valueOf(ChatFormatting.GRAY) + "]" : "") + String.valueOf(this.inversion.getValue() || this.positionSync.getValue() ? ChatFormatting.RESET : ChatFormatting.GRAY) + ", " + String.valueOf(this.getSecondary()) + HUDModule.mc.player.getBlockY() + String.valueOf(this.inversion.getValue() || this.positionSync.getValue() ? ChatFormatting.RESET : ChatFormatting.GRAY) + ", " + String.valueOf(this.getSecondary()) + String.valueOf(coordZ) + (String)(this.netherCoordinates.getValue() ? String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(this.getSecondary()) + WorldUtils.getNetherPosition(coordZ) + String.valueOf(ChatFormatting.GRAY) + "]" : "");
            this.drawText(renderOverlayEvent.getContext(), coordsText, 2.0f, (float)mc.getWindow().getGuiScaledHeight() - bottomOffset - (float)yOffset - (float)Night.FONT_MANAGER.getHeight() - 2.0f);
            maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(coordsText));
            yOffset += Night.FONT_MANAGER.getHeight();
            ++lineCount;
        }
        if (this.direction.getValue()) {
            String dirText = String.valueOf(this.getPrimary()) + WorldUtils.getFacingName(HUDModule.mc.player.getYRot()) + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.GRAY) + " [" + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.WHITE) + WorldUtils.getFacingAxes(HUDModule.mc.player.getYRot()) + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.GRAY) + "]";
            this.drawText(renderOverlayEvent.getContext(), dirText, 2.0f, (float)mc.getWindow().getGuiScaledHeight() - bottomOffset - (float)yOffset - (float)Night.FONT_MANAGER.getHeight() - 2.0f, this.positionSync.getValue() ? null : Color.WHITE);
            maxWidth = Math.max(maxWidth, Night.FONT_MANAGER.getWidth(dirText));
            ++lineCount;
        }
        float bottomY = (float)mc.getWindow().getGuiScaledHeight() - bottomOffset;
        HudElementRegistry.reportBounds("Coordinates", 2.0f, bottomY - 2.0f - (float)(lineCount * Night.FONT_MANAGER.getHeight()), 2 + maxWidth, bottomY - 2.0f);
        matrixStack.popMatrix();
    }

    private static Color lerpColor(Color fromColor, Color toColor, float progress) {
        float t = Math.max(0.0f, Math.min(1.0f, progress));
        int red = Math.round((float)fromColor.getRed() + t * (float)(toColor.getRed() - fromColor.getRed()));
        int green = Math.round((float)fromColor.getGreen() + t * (float)(toColor.getGreen() - fromColor.getGreen()));
        int blue = Math.round((float)fromColor.getBlue() + t * (float)(toColor.getBlue() - fromColor.getBlue()));
        int alpha = Math.round((float)fromColor.getAlpha() + t * (float)(toColor.getAlpha() - fromColor.getAlpha()));
        return new Color(red, green, blue, alpha);
    }

    private void drawModuleText(Module targetModule, GuiGraphicsExtractor graphics, String text, float x, float y) {
        boolean isNewbie = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        if (!isNewbie) {
            if (!targetModule.isToggled()) {
                this.drawText(graphics, text, x, y, false, new Color(170, 170, 170));
                return;
            }
            Color color = this.getHudColor(y);
            if (this.moduleColorMode.getValue().equalsIgnoreCase("Rainbow")) {
                long offset = (long)y / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
                color = ColorUtils.getOffsetRainbow(offset);
            } else if (this.moduleColorMode.getValue().equals("Random")) {
                color = ColorUtils.getHashColor(targetModule.getName());
            }
            this.drawText(graphics, text, x, y, (this.moduleColorMode.getValue().equals("Rainbow") || this.moduleColorMode.getValue().equals("Default") && this.colorMode.getValue().equals("Rainbow")) && this.rainbowMode.getValue().equals("Horizontal"), color);
            return;
        }
        Animation animation = this.moduleColorAnimations.computeIfAbsent(targetModule, module -> new Animation(module.isToggled() ? 1.0f : 0.0f, module.isToggled() ? 1.0f : 0.0f, 350, Easing.Method.EASE_IN_OUT_CUBIC));
        float progress = animation.get(targetModule.isToggled() ? 1.0f : 0.0f);
        Color color = this.getHudColor(y);
        if (this.moduleColorMode.getValue().equalsIgnoreCase("Rainbow")) {
            long offset = (long)y / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
            color = ColorUtils.getOffsetRainbow(offset);
        } else if (this.moduleColorMode.getValue().equals("Random")) {
            color = ColorUtils.getHashColor(targetModule.getName());
        }
        boolean horizontalRainbow = (this.moduleColorMode.getValue().equals("Rainbow") || this.moduleColorMode.getValue().equals("Default") && this.colorMode.getValue().equals("Rainbow")) && this.rainbowMode.getValue().equals("Horizontal");
        HudListStyle.drawNewbie(this, targetModule, graphics, x, y, progress, color, horizontalRainbow);
    }

    private void drawNewbieModuleText(Module module, GuiGraphicsExtractor graphics, float x, float y, float progress, Color color, boolean horizontalRainbow) {
        String metaSuffix;
        String moduleName;
        String bindPrefix = module.getBind() != 0 ? "[" + KeyboardUtils.getKeyName(module.getBind()) + "] " : "";
        String fullText = bindPrefix + (moduleName = module.getName()) + (metaSuffix = module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : " [" + module.getMetaData() + "]");
        if (fullText.isEmpty()) {
            return;
        }
        Color offColor = new Color(170, 170, 170);
        Color whiteColor = Color.WHITE;
        float totalWidth = Night.FONT_MANAGER.getWidth(fullText);
        float cursorX = 0.0f;
        float sweepWidth = 0.35f;
        MutableComponent mutableComponent = Component.empty();
        int bindLen = bindPrefix.length();
        int nameLen = moduleName.length();
        for (int i = 0; i < fullText.length(); ++i) {
            char ch = fullText.charAt(i);
            String chStr = String.valueOf(ch);
            float charWidth = Night.FONT_MANAGER.getWidth(chStr);
            float charCenter = totalWidth > 0.0f ? (cursorX + charWidth / 2.0f) / totalWidth : 0.0f;
            cursorX += charWidth;
            Color startColor = offColor;
            Color endColor = i < bindLen ? (ch == '[' || ch == ']' || ch == ' ' ? offColor : whiteColor) : (i < bindLen + nameLen ? (horizontalRainbow ? ColorUtils.getOffsetRainbow((long)i * this.rainbowOffset.getValue().longValue() * 5L) : color) : (ch == '[' || ch == ']' || ch == ' ' ? offColor : whiteColor));
            float rawT = Math.max(0.0f, Math.min(1.0f, (progress * (1.0f + sweepWidth) - charCenter) / sweepWidth));
            float smoothT = rawT * rawT * (3.0f - 2.0f * rawT);
            Color charColor = HUDModule.lerpColor(startColor, endColor, smoothT);
            mutableComponent.append((Component)Component.literal((String)chStr).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)charColor.getRGB()))));
        }
        Matrix3x2fStack matrixStack = graphics.pose();
        matrixStack.pushMatrix();
        matrixStack.translate(x, y);
        Night.FONT_MANAGER.drawTextWithShadow(graphics, mutableComponent.getVisualOrderText(), 0, 0, Color.WHITE);
        matrixStack.popMatrix();
    }

    private void drawText(GuiGraphicsExtractor graphics, String text, float x, float y) {
        this.drawText(graphics, text, x, y, null);
    }

    public void drawText(GuiGraphicsExtractor graphics, String text, float x, float y, Color color) {
        this.drawText(graphics, text, x, y, this.colorMode.getValue().equals("Rainbow") && this.rainbowMode.getValue().equals("Horizontal"), color);
    }

    public void drawText(GuiGraphicsExtractor graphics, String text, float x, float y, boolean horizontalRainbow, Color color) {
        if (color == null) {
            color = this.getHudColor(y);
        }
        Matrix3x2fStack matrixStack = graphics.pose();
        matrixStack.pushMatrix();
        matrixStack.translate(x, y);
        if (horizontalRainbow) {
            Night.FONT_MANAGER.drawRainbowString(graphics, text, 0, 0, this.rainbowOffset.getValue().longValue() * 5L);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(graphics, text, 0, 0, color);
        }
        matrixStack.popMatrix();
    }

    private Color getHudColor(float y) {
        if (this.colorMode.getValue().equalsIgnoreCase("Rainbow")) {
            long offset = (long)y / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
            return ColorUtils.getOffsetRainbow(offset);
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Wave")) {
            long offset = (long)y / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 20L);
            return ColorUtils.getOffsetWave(ColorUtils.getGlobalColor(), offset);
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Custom")) {
            return ColorUtils.getColor(this.customColor.getColor(), 255);
        }
        return ColorUtils.getGlobalColor();
    }

    @Override
    public Setting getSetting(String name) {
        if (name.equalsIgnoreCase("ModuleListMode") || name.equalsIgnoreCase("Mode")) {
            return this.moduleListMode;
        }
        return super.getSetting(name);
    }

    private String getModuleText(Module module) {
        boolean isNewbie = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        String bindPrefix = "";
        if (isNewbie && module.getBind() != 0) {
            bindPrefix = String.valueOf(ChatFormatting.GRAY) + "[" + String.valueOf(ChatFormatting.WHITE) + KeyboardUtils.getKeyName(module.getBind()) + String.valueOf(ChatFormatting.GRAY) + "] ";
        }
        if (!isNewbie && !module.isToggled()) {
            return bindPrefix + String.valueOf(ChatFormatting.GRAY) + module.getName() + (String)(module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : String.valueOf(ChatFormatting.DARK_GRAY) + " [" + String.valueOf(ChatFormatting.GRAY) + module.getMetaData() + String.valueOf(ChatFormatting.DARK_GRAY) + "]");
        }
        return bindPrefix + module.getName() + (String)(module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(ChatFormatting.WHITE) + module.getMetaData() + String.valueOf(ChatFormatting.GRAY) + "]");
    }

    private String getPotionText(MobEffectInstance mobEffectInstance) {
        String durationStr;
        if (mobEffectInstance.isInfiniteDuration()) {
            durationStr = "**:**";
        } else {
            int seconds = Math.round((float)mobEffectInstance.getDuration() / HUDModule.mc.level.tickRateManager().tickrate());
            durationStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
        }
        return ((MobEffect)mobEffectInstance.getEffect().value()).getDisplayName().getString() + " " + (mobEffectInstance.getAmplifier() + 1) + " " + String.valueOf(ChatFormatting.WHITE) + durationStr;
    }

    private ChatFormatting getPrimary() {
        return this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.RESET;
    }

    private ChatFormatting getSecondary() {
        return this.inversion.getValue() ? ChatFormatting.RESET : ChatFormatting.WHITE;
    }

    static {
        ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    }

    private static class SmoothCounter {
        private int lastCount = -1;
        private int prevCount = -1;
        private long changeTime = 0L;
        private int direction = 0;

        private SmoothCounter() {
        }

        public void update(int count) {
            if (this.lastCount == -1) {
                this.lastCount = count;
                this.prevCount = count;
                return;
            }
            if (count != this.lastCount) {
                this.prevCount = this.lastCount;
                this.lastCount = count;
                this.direction = count < this.prevCount ? -1 : 1;
                this.changeTime = System.currentTimeMillis();
            }
        }

        public void render(GuiGraphicsExtractor graphics, boolean smooth) {
            Color color;
            float enterOffsetY;
            float exitOffsetY;
            if (this.lastCount <= 0 && this.prevCount <= 0) {
                return;
            }
            String countText = String.valueOf(Math.max(0, this.lastCount));
            int fontHeight = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getHeight() : 9;
            long elapsed = System.currentTimeMillis() - this.changeTime;
            float durationMs = 320.0f;
            float progress = this.changeTime == 0L ? 1.0f : Math.min(1.0f, (float)elapsed / durationMs);
            if (!smooth || progress >= 1.0f || this.prevCount == -1 || this.prevCount == this.lastCount) {
                int textWidth = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(countText) : Minecraft.getInstance().font.width(countText);
                float drawX = 17 - textWidth;
                float drawY = 9.0f;
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(graphics, countText, (int)drawX, (int)drawY, Color.WHITE);
                } else {
                    graphics.text(Minecraft.getInstance().font, countText, (int)drawX, (int)drawY, -1, true);
                }
                return;
            }
            float eased = Easing.ease(progress, Easing.Method.EASE_OUT_CUBIC);
            String prevText = String.valueOf(Math.max(0, this.prevCount));
            int countWidth = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(countText) : Minecraft.getInstance().font.width(countText);
            int prevWidth = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(prevText) : Minecraft.getInstance().font.width(prevText);
            float drawX = 17 - countWidth;
            float prevDrawX = 17 - prevWidth;
            float drawY = 9.0f;
            float slideDist = fontHeight + 3;
            if (this.direction < 0) {
                exitOffsetY = eased * slideDist;
                enterOffsetY = -(1.0f - eased) * slideDist;
            } else {
                exitOffsetY = -eased * slideDist;
                enterOffsetY = (1.0f - eased) * slideDist;
            }
            int prevAlpha = Math.max(0, Math.min(255, Math.round((1.0f - eased) * 255.0f)));
            int currAlpha = Math.max(0, Math.min(255, Math.round(eased * 255.0f)));
            if (prevAlpha > 5) {
                color = new Color(255, 255, 255, prevAlpha);
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(graphics, prevText, (int)prevDrawX, (int)(drawY + exitOffsetY), color);
                } else {
                    graphics.text(Minecraft.getInstance().font, prevText, (int)prevDrawX, (int)(drawY + exitOffsetY), prevAlpha << 24 | 0xFFFFFF, true);
                }
            }
            if (currAlpha > 5) {
                color = new Color(255, 255, 255, currAlpha);
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(graphics, countText, (int)drawX, (int)(drawY + enterOffsetY), color);
                } else {
                    graphics.text(Minecraft.getInstance().font, countText, (int)drawX, (int)(drawY + enterOffsetY), currAlpha << 24 | 0xFFFFFF, true);
                }
            }
        }
    }

    public record ModuleEntry(Module module, String text) {
    }

    public record PlayerEntry(Player player, String text, Identifier headTexture) {
    }

    public record PotionEntry(String text, Identifier sprite, Color color) {
    }
}

