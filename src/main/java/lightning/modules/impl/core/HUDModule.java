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

    private int getFakedCoord(int n, int n2) {
        if (!this.fakeCoords.getValue()) {
            return n;
        }
        if (Math.abs(n) < this.fakeThreshold.getValue().intValue()) {
            return n;
        }
        long l = (long)n2 + (long)Math.abs(n) / 3L;
        boolean bl = (Math.abs(n) * 31 + n2) % 2 == 0;
        long l2 = bl ? -l : l;
        long l3 = l2;
        if (HUDModule.mc.player != null && HUDModule.mc.player.level() != null && HUDModule.mc.player.level().dimension() == Level.NETHER) {
            l2 /= 8L;
        }
        return (int)l2;
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
                boolean bl = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
                List<ModuleEntry> entries = new ArrayList<ModuleEntry>();
                for (Module module2 : Night.MODULE_MANAGER.getModules().stream().filter(module -> module.drawn.getValue()).filter(module -> {
                    if (bl) {
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
                Comparator<Player> playerCompDist = Comparator.comparingDouble(p -> HUDModule.mc.player.distanceTo((Entity)p));
                Comparator<Player> playerCompName = Comparator.comparing(p -> p.getName().getString());
                ArrayList<PlayerEntry> arrayList = new ArrayList<PlayerEntry>();
                for (Player player : HUDModule.mc.level.players().stream().filter(p -> p != HUDModule.mc.player).filter(p -> !EntityUtils.isGhost((Entity)p)).filter(p -> !this.playerRadarAntiBot.getValue() || !EntityUtils.isBot(p)).filter(p -> !Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled() || !EntityUtils.isNaked(p)).sorted(this.playerRadarSorting.getValue().equalsIgnoreCase("Distance") ? playerCompDist : playerCompName).limit(this.playerRadarLimit.getValue().longValue()).toList()) {
                    Identifier skinId = null;
                    MobEffectInstance mobEffectInstance2;
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
                    int n = Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(player.getUUID(), 0);
                    if (this.playerRadarTotems.getValue() && n > 0) {
                        text = text + String.valueOf(ColorUtils.getTotemColor(n)) + " -" + n + String.valueOf(ChatFormatting.RESET);
                    }
                    if (this.playerRadarTurtlePot.getValue() && (mobEffectInstance2 = player.getEffect(MobEffects.RESISTANCE)) != null && mobEffectInstance2.getAmplifier() >= 2) {
                        text = text + String.valueOf(ChatFormatting.RED) + " [TM]" + String.valueOf(ChatFormatting.RESET);
                    }
                    arrayList.add(new PlayerEntry(player, text, skinId));
                }
                this.playerEntries = arrayList;
            }
            if (this.potions.getValue()) {
                Comparator<MobEffectInstance> potionCompWidth = Comparator.comparingInt(mobEffectInstance -> -Night.FONT_MANAGER.getWidth(((MobEffect)mobEffectInstance.getEffect().value()).getDisplayName().getString() + " " + (mobEffectInstance.getAmplifier() + 1)));
                Comparator<MobEffectInstance> potionCompName = Comparator.comparing(mobEffectInstance -> ((MobEffect)mobEffectInstance.getEffect().value()).getDisplayName().getString());
                ArrayList<PotionEntry> arrayList = new ArrayList<PotionEntry>();
                for (MobEffectInstance mobEffectInstance3 : HUDModule.mc.player.getActiveEffects().stream().sorted(this.potionSorting.getValue().equalsIgnoreCase("Width") ? potionCompWidth : potionCompName).toList()) {
                    String string = this.getPotionText(mobEffectInstance3);
                    Identifier sprite = null;
                    if (this.potionIcons.getValue()) {
                        sprite = Hud.getMobEffectSprite((Holder)mobEffectInstance3.getEffect());
                    }
                    arrayList.add(new PotionEntry(string, sprite, this.potionColor.getValue().equalsIgnoreCase("Vanilla") ? new Color(((MobEffect)mobEffectInstance3.getEffect().value()).getColor()) : (this.potionColor.getValue().equalsIgnoreCase("Enhanced") ? (EntityUtils.POTION_COLORS.containsKey(mobEffectInstance3.getEffect().value()) ? EntityUtils.POTION_COLORS.get(mobEffectInstance3.getEffect().value()) : new Color(((MobEffect)mobEffectInstance3.getEffect().value()).getColor())) : null)));
                }
                this.potionEntries = arrayList;
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
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        if (this.watermark.getValue() || this.uptime.getValue()) {
            String[] stringArray;
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(this.watermarkPosition.getX(), this.watermarkPosition.getY());
            int n = 0;
            int n2 = 0;
            if (this.watermark.getValue()) {
                boolean bl = true;
                String versionNum = bl ? "" : "26";
                String string = !bl || !this.watermarkVersion.getValue() ? "" : (!this.watermarkVersionMode.getValue().equals("GitHash") ? String.valueOf(ChatFormatting.GRAY) : "+436.eeeb439b78");
                String string2 = this.watermarkText.getValue() + (String)(this.watermarkVersion.getValue() ? String.valueOf(this.watermarkSync.getValue() ? "" : (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE)) + " " + versionNum + (this.watermarkMinecraftVersion.getValue() ? "-mc26.2" : "") + string : "");
                float f = 2.0f;
                if (this.watermarkLogo.getValue()) {
                    int n3 = Night.FONT_MANAGER.getHeight();
                    int n4 = this.getHudColor(2.0f).getRGB();
                    renderOverlayEvent.getContext().blit(RenderPipelines.GUI_TEXTURED, LOGO_ID, 2, 2, 0.0f, 0.0f, n3, n3, 64, 64, 64, 64, n4);
                    f = 2 + n3 + 3;
                }
                this.drawText(renderOverlayEvent.getContext(), string2, f, 2.0f);
                n = Math.max(n, (int)(f - 2.0f) + Night.FONT_MANAGER.getWidth(string2));
                ++n2;
            }
            if (this.uptime.getValue()) {
                stringArray = FormattingUtils.formatSeconds((System.currentTimeMillis() - Night.UPTIME) / 1000L);
                String string = "Uptime " + String.valueOf(ChatFormatting.WHITE) + stringArray[0] + ":" + stringArray[1] + ":" + stringArray[2];
                this.drawText(renderOverlayEvent.getContext(), string, 2.0f, 2 + (this.watermark.getValue() ? Night.FONT_MANAGER.getHeight() : 0), this.informationSync.getValue() ? null : new Color(170, 170, 170));
                n = Math.max(n, Night.FONT_MANAGER.getWidth(string));
                ++n2;
            }
            HudElementRegistry.reportBounds("Watermark", 2.0f, 2.0f, 2 + n, 2 + n2 * Night.FONT_MANAGER.getHeight());
            matrix3x2fStack.popMatrix();
        }
        if (this.welcomer.getValue()) {
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(this.welcomerPosition.getX(), this.welcomerPosition.getY());
            String string = this.welcomerText.getValue().replace("[username]", String.valueOf(this.welcomerSync.getValue() ? "" : (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE)) + HUDModule.mc.player.getName().getString() + String.valueOf(ChatFormatting.RESET));
            float f = (float)mc.getWindow().getGuiScaledWidth() / 2.0f - (float)Night.FONT_MANAGER.getWidth(string) / 2.0f;
            this.drawText(renderOverlayEvent.getContext(), string, f, 6.0f);
            HudElementRegistry.reportBounds("Welcomer", f, 6.0f, f + (float)Night.FONT_MANAGER.getWidth(string), 6 + Night.FONT_MANAGER.getHeight());
            matrix3x2fStack.popMatrix();
        }
    }

    private void renderModuleList(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null || HUDModule.mc.level == null) {
            return;
        }
        if (!this.moduleList.getValue()) {
            return;
        }
        float f = this.potionsAnimation.get(!this.vanillaPotions.getValue().equalsIgnoreCase("Move") || HUDModule.mc.player.getActiveEffects().isEmpty() ? 0.0f : (float)(EntityUtils.hasNegativeEffects((Player)HUDModule.mc.player) ? 51 : 25));
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.moduleListPosition.getX(), this.moduleListPosition.getY());
        boolean bl = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        int n = 0;
        int n2 = 0;
        for (ModuleEntry moduleEntry : this.moduleEntries) {
            boolean bl2 = moduleEntry.module().isToggled() || bl && moduleEntry.module().getBind() != 0;
            float f2 = bl2 ? (float)(Night.FONT_MANAGER.getWidth(moduleEntry.text()) + 2) : 0.0f;
            float f3 = (float)mc.getWindow().getGuiScaledWidth() - moduleEntry.module().getAnimationOffset().get(f2);
            float f4 = 2.0f + f + (float)(n2 * Night.FONT_MANAGER.getHeight());
            this.drawModuleText(moduleEntry.module(), renderOverlayEvent.getContext(), moduleEntry.text(), f3, f4);
            n = Math.max(n, Night.FONT_MANAGER.getWidth(moduleEntry.text()) + 2);
            ++n2;
        }
        if (!this.moduleEntries.isEmpty()) {
            float f5 = mc.getWindow().getGuiScaledWidth();
            HudElementRegistry.reportBounds("ModuleList", f5 - (float)n, 2.0f + f, f5, 2.0f + f + (float)(this.moduleEntries.size() * Night.FONT_MANAGER.getHeight()));
        }
        matrix3x2fStack.popMatrix();
    }

    private void renderPlayerRadar(RenderOverlayEvent renderOverlayEvent) {
        if (!this.playerRadar.getValue()) {
            return;
        }
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.playerRadarPosition.getX(), this.playerRadarPosition.getY());
        int n = 0;
        int n2 = 0;
        for (PlayerEntry playerEntry : this.playerEntries) {
            if (playerEntry.headTexture() != null) {
                PlayerFaceExtractor.extractRenderState((GuiGraphicsExtractor)renderOverlayEvent.getContext(), (Identifier)playerEntry.headTexture(), (int)2, (int)(1 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * n2), (int)Night.FONT_MANAGER.getHeight(), (boolean)true, (boolean)false, (int)Color.WHITE.getRGB());
            }
            int n3 = 2 + (playerEntry.headTexture() != null ? Night.FONT_MANAGER.getHeight() + 2 : 0);
            this.drawText(renderOverlayEvent.getContext(), playerEntry.text(), n3, 2 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * n2, Night.FRIEND_MANAGER.contains(playerEntry.player().getName().getString()) ? Night.FRIEND_MANAGER.getDefaultFriendColor() : null);
            n = Math.max(n, n3 + Night.FONT_MANAGER.getWidth(playerEntry.text()));
            ++n2;
        }
        if (!this.playerEntries.isEmpty()) {
            HudElementRegistry.reportBounds("PlayerRadar", 2.0f, 1 + Night.FONT_MANAGER.getHeight() * 2, n, 1 + Night.FONT_MANAGER.getHeight() * 2 + (Night.FONT_MANAGER.getHeight() + 1) * this.playerEntries.size());
        }
        matrix3x2fStack.popMatrix();
    }

    private static int countItem(Player player, Item item) {
        if (player == null) {
            return 0;
        }
        int n = 0;
        for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
            if (!itemStack.is(item)) continue;
            n += itemStack.getCount();
        }
        if (player.getOffhandItem().is(item)) {
            n += player.getOffhandItem().getCount();
        }
        return n;
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
        boolean bl = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        int n = HUDModule.mc.player.isEyeInFluid(FluidTags.WATER) || HUDModule.mc.player.getAirSupply() < HUDModule.mc.player.getMaxAirSupply() ? 10 : 0;
        int n2 = mc.getWindow().getGuiScaledWidth() / 2 + 15;
        int n3 = mc.getWindow().getGuiScaledHeight() - 55 - n;
        HudElementRegistry.reportBounds("Armor", n2 - 1, n3 - 3, n2 + 71, n3 + 17);
        if (!this.armor.getValue() && !bl) {
            return;
        }
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.armorPosition.getX(), this.armorPosition.getY());
        boolean bl2 = false;
        for (EquipmentSlot equipmentSlot : ARMOR_SLOTS) {
            if (HUDModule.mc.player.getItemBySlot(equipmentSlot).isEmpty()) continue;
            bl2 = true;
            break;
        }
        if (bl2 && this.armor.getValue()) {
            int n4 = 0;
            for (EquipmentSlot equipmentSlot : ARMOR_SLOTS) {
                ItemStack itemStack = HUDModule.mc.player.getItemBySlot(equipmentSlot);
                if (itemStack.isEmpty()) continue;
                int n5 = mc.getWindow().getGuiScaledWidth() / 2 + 69 - 18 * n4;
                int n6 = n3;
                renderOverlayEvent.getContext().item(itemStack, n5, n6);
                if (this.armorDurability.getValue().equalsIgnoreCase("Bar") || this.armorDurability.getValue().equalsIgnoreCase("Both")) {
                    renderOverlayEvent.getContext().itemDecorations(HUDModule.mc.font, itemStack, n5, n6);
                }
                int n7 = itemStack.getDamageValue();
                int n8 = itemStack.getMaxDamage();
                if ((this.armorDurability.getValue().equalsIgnoreCase("Percentage") || this.armorDurability.getValue().equalsIgnoreCase("Both")) && n8 > 0) {
                    matrix3x2fStack.pushMatrix();
                    matrix3x2fStack.scale(0.625f, 0.625f);
                    this.drawText(renderOverlayEvent.getContext(), (n8 - n7) * 100 / n8 + "%", (int)((float)((mc.getWindow().getGuiScaledWidth() >> 1) + 70 - 18 * n4) * 1.6f), (int)((float)(n3 - 3) * 1.6f - 5.0f), false, new Color(1.0f - (float)(n8 - n7) / (float)n8, (float)(n8 - n7) / (float)n8, 0.0f));
                    matrix3x2fStack.popMatrix();
                }
                ++n4;
            }
        } else if (bl) {
            ItemStack[] equipmentSlotArray = HUDModule.dummyArmor();
            for (int i = 0; i < equipmentSlotArray.length; ++i) {
                int n9 = mc.getWindow().getGuiScaledWidth() / 2 + 69 - 18 * i;
                int n10 = n3;
                renderOverlayEvent.getContext().item(equipmentSlotArray[i], n9, n10);
            }
        }
        matrix3x2fStack.popMatrix();
    }

    private void renderTotemCounter(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean bl = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float f = (float)mc.getWindow().getGuiScaledWidth() / 2.0f - 9.0f;
        float f2 = mc.getWindow().getGuiScaledHeight() - 55 - (HUDModule.mc.player.isEyeInFluid(FluidTags.WATER) && HUDModule.mc.gameMode != null && HUDModule.mc.gameMode.getPlayerMode() != GameType.CREATIVE ? 10 : 0);
        HudElementRegistry.reportBounds("TotemCounter", f - 1.0f, f2 - 1.0f, f + 19.0f, f2 + 19.0f);
        if (!this.totemCounter.getValue() && !bl) {
            return;
        }
        int n = HUDModule.countItem((Player)HUDModule.mc.player, Items.TOTEM_OF_UNDYING);
        this.totemSmoothCounter.update(n);
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.totemCounterPosition.getX(), this.totemCounterPosition.getY());
        if (this.totemCounter.getValue() && (n > 0 || this.totemSmoothCounter.lastCount > 0)) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING);
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(f, f2);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            this.totemSmoothCounter.render(renderOverlayEvent.getContext(), this.itemsSmooth.getValue());
            matrix3x2fStack.popMatrix();
        } else if (bl) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING);
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(f, f2);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(renderOverlayEvent.getContext(), "1", 17 - Night.FONT_MANAGER.getWidth("1"), 9, Color.WHITE);
            } else {
                renderOverlayEvent.getContext().text(HUDModule.mc.font, "1", 17 - HUDModule.mc.font.width("1"), 9, -1, true);
            }
            matrix3x2fStack.popMatrix();
        }
        matrix3x2fStack.popMatrix();
    }

    private void renderCrystalCounter(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean bl = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float f = (float)mc.getWindow().getGuiScaledWidth() / 2.0f + 106.0f;
        float f2 = (float)(mc.getWindow().getGuiScaledHeight() - 40) - (this.counterChatOffset.getValue() ? this.chatOffset : 0.0f);
        HudElementRegistry.reportBounds("CrystalCounter", f - 1.0f, f2 - 1.0f, f + 19.0f, f2 + 19.0f);
        if (!this.crystalCounter.getValue() && !bl) {
            return;
        }
        int n = HUDModule.countItem((Player)HUDModule.mc.player, Items.END_CRYSTAL);
        this.crystalSmoothCounter.update(n);
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.crystalCounterPosition.getX(), this.crystalCounterPosition.getY());
        if (this.crystalCounter.getValue() && (n > 0 || this.crystalSmoothCounter.lastCount > 0)) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.END_CRYSTAL);
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(f, f2);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            this.crystalSmoothCounter.render(renderOverlayEvent.getContext(), this.itemsSmooth.getValue());
            matrix3x2fStack.popMatrix();
        } else if (bl) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.END_CRYSTAL);
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(f, f2);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(renderOverlayEvent.getContext(), "64", 17 - Night.FONT_MANAGER.getWidth("64"), 9, Color.WHITE);
            } else {
                renderOverlayEvent.getContext().text(HUDModule.mc.font, "64", 17 - HUDModule.mc.font.width("64"), 9, -1, true);
            }
            matrix3x2fStack.popMatrix();
        }
        matrix3x2fStack.popMatrix();
    }

    private void renderXPCounter(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        boolean bl = HUDModule.mc.gui.screen() instanceof HUDEditorScreen;
        float f = (float)mc.getWindow().getGuiScaledWidth() / 2.0f + 106.0f;
        float f2 = (float)(mc.getWindow().getGuiScaledHeight() - 20) - (this.counterChatOffset.getValue() ? this.chatOffset : 0.0f);
        HudElementRegistry.reportBounds("XPCounter", f - 1.0f, f2 - 1.0f, f + 19.0f, f2 + 19.0f);
        if (!this.xpCounter.getValue() && !bl) {
            return;
        }
        int n = HUDModule.countItem((Player)HUDModule.mc.player, Items.EXPERIENCE_BOTTLE);
        this.xpSmoothCounter.update(n);
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.xpCounterPosition.getX(), this.xpCounterPosition.getY());
        if (this.xpCounter.getValue() && (n > 0 || this.xpSmoothCounter.lastCount > 0)) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(f, f2);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            this.xpSmoothCounter.render(renderOverlayEvent.getContext(), this.itemsSmooth.getValue());
            matrix3x2fStack.popMatrix();
        } else if (bl) {
            ItemStack itemStack = new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE);
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(f, f2);
            renderOverlayEvent.getContext().item(itemStack, 0, 0);
            if (Night.FONT_MANAGER != null) {
                Night.FONT_MANAGER.drawTextWithShadow(renderOverlayEvent.getContext(), "64", 17 - Night.FONT_MANAGER.getWidth("64"), 9, Color.WHITE);
            } else {
                renderOverlayEvent.getContext().text(HUDModule.mc.font, "64", 17 - HUDModule.mc.font.width("64"), 9, -1, true);
            }
            matrix3x2fStack.popMatrix();
        }
        matrix3x2fStack.popMatrix();
    }

    private void renderInventory(RenderOverlayEvent renderOverlayEvent) {
        Color color;
        int n;
        if (HUDModule.mc.player == null) {
            return;
        }
        if (!this.inventory.getValue()) {
            return;
        }
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.inventoryPosition.getX(), this.inventoryPosition.getY());
        int n2 = 3;
        int n3 = 18;
        int n4 = 9 * n3 + n2 * 2;
        int n5 = 3 * n3 + n2 * 2;
        HudElementRegistry.reportBounds("Inventory", 0.0f, 0.0f, n4, n5);
        if (this.inventoryBackground.getValue()) {
            n = (int)Math.clamp(this.inventoryAlpha.getValue().floatValue(), 0.0f, 255.0f);
            color = new Color(15, 15, 18, n);
            Color color2 = new Color(255, 255, 255, Math.min(n, 30));
            Renderer2D.renderQuad(renderOverlayEvent.getContext(), 0.0f, 0.0f, n4, n5, color);
            Renderer2D.renderOutline(renderOverlayEvent.getContext(), 0.0f, 0.0f, n4, n5, color2);
        }
        for (n = 9; n <= 35; ++n) {
            int n6 = (n - 9) % 9;
            int n7 = (n - 9) / 9;
            int n8 = n2 + n6 * n3;
            int n9 = n2 + n7 * n3;
            if (this.inventorySlotBackground.getValue()) {
                Color color3 = new Color(255, 255, 255, 10);
                Renderer2D.renderQuad(renderOverlayEvent.getContext(), n8, n9, n8 + 18, n9 + 18, color3);
            }
            ItemStack itemStack = HUDModule.mc.player.getInventory().getItem(n);
            if (itemStack.isEmpty()) continue;
            renderOverlayEvent.getContext().item(itemStack, n8 + 1, n9 + 1);
            renderOverlayEvent.getContext().itemDecorations(HUDModule.mc.font, itemStack, n8 + 1, n9 + 1);
        }
        matrix3x2fStack.popMatrix();
    }

    private void renderPotions(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null) {
            return;
        }
        if (!this.potions.getValue()) {
            return;
        }
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.potionsPosition.getX(), this.potionsPosition.getY());
        float f = this.informationChatOffset.getValue() ? this.chatOffset : 0.0f;
        int n = 0;
        int n2 = 0;
        for (PotionEntry potionEntry : this.potionEntries) {
            int n3 = Night.FONT_MANAGER.getWidth(potionEntry.text());
            int n4 = n3 + (potionEntry.sprite() != null ? Night.FONT_MANAGER.getHeight() + 2 : 0);
            n2 = Math.max(n2, n4);
            int n5 = mc.getWindow().getGuiScaledWidth() - 2 - n3;
            int n6 = mc.getWindow().getGuiScaledHeight() - (int)f - 2 - Night.FONT_MANAGER.getHeight() - Night.FONT_MANAGER.getHeight() * n;
            if (potionEntry.sprite() != null) {
                matrix3x2fStack.pushMatrix();
                matrix3x2fStack.translate((float)(n5 - Night.FONT_MANAGER.getHeight() - 2), (float)(n6 - 1));
                renderOverlayEvent.getContext().blitSprite(RenderPipelines.GUI_TEXTURED, potionEntry.sprite(), 0, 0, Night.FONT_MANAGER.getHeight(), Night.FONT_MANAGER.getHeight());
                matrix3x2fStack.popMatrix();
            }
            this.drawText(renderOverlayEvent.getContext(), potionEntry.text(), n5, n6, this.potionColor.getValue().equals("Client") && this.colorMode.getValue().equals("Rainbow") && this.rainbowMode.getValue().equals("Horizontal"), potionEntry.color());
            ++n;
        }
        if (n > 0) {
            float f2 = mc.getWindow().getGuiScaledWidth();
            float f3 = (float)mc.getWindow().getGuiScaledHeight() - f;
            HudElementRegistry.reportBounds("Potions", f2 - 2.0f - (float)n2 - 4.0f, f3 - 2.0f - (float)(n * Night.FONT_MANAGER.getHeight()), f2, f3);
        }
        matrix3x2fStack.popMatrix();
    }

    private void renderInformation(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player != null && this.informationElement.getValue()) {
            Object object;
            float f;
            Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
            matrix3x2fStack.pushMatrix();
            matrix3x2fStack.translate(this.informationPosition.getX(), this.informationPosition.getY());
            int n = 0;
            float f2 = f = this.informationChatOffset.getValue() ? this.chatOffset : 0.0f;
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
            float f3 = Night.SERVER_MANAGER.getTickRate();
            if (this.tps.getValue()) {
                infoList.add(String.valueOf(this.getPrimary()) + "TPS " + String.valueOf(this.getSecondary()) + ((double)f3 > 19.79 ? "20.00" : new DecimalFormat("00.00").format(f3)));
            }
            if (!infoList.isEmpty()) {
                infoList.sort(Comparator.comparingInt(Night.FONT_MANAGER::getWidth).reversed());
                for (String infoText : infoList) {
                    if (infoText.startsWith("Durability")) {
                        if (!HUDModule.mc.player.getMainHandItem().isDamageableItem()) continue;
                        int n2 = HUDModule.mc.player.getMainHandItem().getMaxDamage();
                        int n3 = HUDModule.mc.player.getMainHandItem().getDamageValue();
                        String string = String.valueOf(n2 - n3);
                        this.drawText(renderOverlayEvent.getContext(), String.valueOf(this.getPrimary()) + "Durability ", mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth("Durability ") - Night.FONT_MANAGER.getWidth(string), (float)mc.getWindow().getGuiScaledHeight() - f - 2.0f - (float)Night.FONT_MANAGER.getHeight() + (float)(n * -Night.FONT_MANAGER.getHeight()), this.informationSync.getValue() ? null : new Color(170, 170, 170));
                        this.drawText(renderOverlayEvent.getContext(), string, mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth(string), (float)mc.getWindow().getGuiScaledHeight() - f - 2.0f - (float)Night.FONT_MANAGER.getHeight() + (float)(n * -Night.FONT_MANAGER.getHeight()), false, new Color(1.0f - (float)(n2 - n3) / (float)n2, (float)(n2 - n3) / (float)n2, 0.0f));
                        ++n;
                        continue;
                    }
                    this.drawText(renderOverlayEvent.getContext(), infoText, mc.getWindow().getGuiScaledWidth() - 2 - Night.FONT_MANAGER.getWidth(infoText), (float)mc.getWindow().getGuiScaledHeight() - f - 2.0f - (float)Night.FONT_MANAGER.getHeight() + (float)(n * -Night.FONT_MANAGER.getHeight()), this.informationSync.getValue() ? null : new Color(170, 170, 170));
                    ++n;
                }
                int n4 = 0;
                for (String string : infoList) {
                    n4 = Math.max(n4, Night.FONT_MANAGER.getWidth(string));
                }
                float f4 = mc.getWindow().getGuiScaledWidth();
                float f5 = (float)mc.getWindow().getGuiScaledHeight() - f;
                HudElementRegistry.reportBounds("Information", f4 - 2.0f - (float)n4, f5 - 2.0f - (float)(n * Night.FONT_MANAGER.getHeight()), f4, f5);
            }
            matrix3x2fStack.popMatrix();
        }
    }

    private void renderCoordinates(RenderOverlayEvent renderOverlayEvent) {
        if (HUDModule.mc.player == null || HUDModule.mc.level == null) {
            return;
        }
        if (!this.coordinates.getValue() && !this.direction.getValue()) {
            return;
        }
        Matrix3x2fStack matrix3x2fStack = renderOverlayEvent.getMatrices();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(this.coordinatesPosition.getX(), this.coordinatesPosition.getY());
        float f = this.positionChatOffset.getValue() ? this.chatOffset : 0.0f;
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        if (this.coordinates.getValue()) {
            int n4 = this.getFakedCoord(HUDModule.mc.player.getBlockX(), this.fakeOffsetX);
            int n5 = this.getFakedCoord(HUDModule.mc.player.getBlockZ(), this.fakeOffsetZ);
            String string = String.valueOf(this.getSecondary()) + String.valueOf(n4) + (String)(this.netherCoordinates.getValue() ? String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(this.getSecondary()) + WorldUtils.getNetherPosition(n4) + String.valueOf(ChatFormatting.GRAY) + "]" : "") + String.valueOf(this.inversion.getValue() || this.positionSync.getValue() ? ChatFormatting.RESET : ChatFormatting.GRAY) + ", " + String.valueOf(this.getSecondary()) + HUDModule.mc.player.getBlockY() + String.valueOf(this.inversion.getValue() || this.positionSync.getValue() ? ChatFormatting.RESET : ChatFormatting.GRAY) + ", " + String.valueOf(this.getSecondary()) + String.valueOf(n5) + (String)(this.netherCoordinates.getValue() ? String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(this.getSecondary()) + WorldUtils.getNetherPosition(n5) + String.valueOf(ChatFormatting.GRAY) + "]" : "");
            this.drawText(renderOverlayEvent.getContext(), string, 2.0f, (float)mc.getWindow().getGuiScaledHeight() - f - (float)n - (float)Night.FONT_MANAGER.getHeight() - 2.0f);
            n2 = Math.max(n2, Night.FONT_MANAGER.getWidth(string));
            n += Night.FONT_MANAGER.getHeight();
            ++n3;
        }
        if (this.direction.getValue()) {
            String string = String.valueOf(this.getPrimary()) + WorldUtils.getFacingName(HUDModule.mc.player.getYRot()) + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.GRAY) + " [" + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.WHITE) + WorldUtils.getFacingAxes(HUDModule.mc.player.getYRot()) + String.valueOf(this.inversion.getValue() ? this.getSecondary() : ChatFormatting.GRAY) + "]";
            this.drawText(renderOverlayEvent.getContext(), string, 2.0f, (float)mc.getWindow().getGuiScaledHeight() - f - (float)n - (float)Night.FONT_MANAGER.getHeight() - 2.0f, this.positionSync.getValue() ? null : Color.WHITE);
            n2 = Math.max(n2, Night.FONT_MANAGER.getWidth(string));
            ++n3;
        }
        float f2 = (float)mc.getWindow().getGuiScaledHeight() - f;
        HudElementRegistry.reportBounds("Coordinates", 2.0f, f2 - 2.0f - (float)(n3 * Night.FONT_MANAGER.getHeight()), 2 + n2, f2 - 2.0f);
        matrix3x2fStack.popMatrix();
    }

    private static Color lerpColor(Color color, Color color2, float f) {
        float f2 = Math.max(0.0f, Math.min(1.0f, f));
        int n = Math.round((float)color.getRed() + f2 * (float)(color2.getRed() - color.getRed()));
        int n2 = Math.round((float)color.getGreen() + f2 * (float)(color2.getGreen() - color.getGreen()));
        int n3 = Math.round((float)color.getBlue() + f2 * (float)(color2.getBlue() - color.getBlue()));
        int n4 = Math.round((float)color.getAlpha() + f2 * (float)(color2.getAlpha() - color.getAlpha()));
        return new Color(n, n2, n3, n4);
    }

    private void drawModuleText(Module module2, GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2) {
        boolean bl = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        if (!bl) {
            if (!module2.isToggled()) {
                this.drawText(guiGraphicsExtractor, string, f, f2, false, new Color(170, 170, 170));
                return;
            }
            Color color = this.getHudColor(f2);
            if (this.moduleColorMode.getValue().equalsIgnoreCase("Rainbow")) {
                long l = (long)f2 / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
                color = ColorUtils.getOffsetRainbow(l);
            } else if (this.moduleColorMode.getValue().equals("Random")) {
                color = ColorUtils.getHashColor(module2.getName());
            }
            this.drawText(guiGraphicsExtractor, string, f, f2, (this.moduleColorMode.getValue().equals("Rainbow") || this.moduleColorMode.getValue().equals("Default") && this.colorMode.getValue().equals("Rainbow")) && this.rainbowMode.getValue().equals("Horizontal"), color);
            return;
        }
        Animation animation = this.moduleColorAnimations.computeIfAbsent(module2, module -> new Animation(module.isToggled() ? 1.0f : 0.0f, module.isToggled() ? 1.0f : 0.0f, 350, Easing.Method.EASE_IN_OUT_CUBIC));
        float f3 = animation.get(module2.isToggled() ? 1.0f : 0.0f);
        Color color = this.getHudColor(f2);
        if (this.moduleColorMode.getValue().equalsIgnoreCase("Rainbow")) {
            long l = (long)f2 / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
            color = ColorUtils.getOffsetRainbow(l);
        } else if (this.moduleColorMode.getValue().equals("Random")) {
            color = ColorUtils.getHashColor(module2.getName());
        }
        boolean bl2 = (this.moduleColorMode.getValue().equals("Rainbow") || this.moduleColorMode.getValue().equals("Default") && this.colorMode.getValue().equals("Rainbow")) && this.rainbowMode.getValue().equals("Horizontal");
        HudListStyle.drawNewbie(this, module2, guiGraphicsExtractor, f, f2, f3, color, bl2);
    }

    private void drawNewbieModuleText(Module module, GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, Color color, boolean bl) {
        String string;
        String string2;
        Object object = module.getBind() != 0 ? "[" + KeyboardUtils.getKeyName(module.getBind()) + "] " : "";
        String string3 = (String)object + (string2 = module.getName()) + (string = module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : " [" + module.getMetaData() + "]");
        if (string3.isEmpty()) {
            return;
        }
        Color color2 = new Color(170, 170, 170);
        Color color3 = Color.WHITE;
        float f4 = Night.FONT_MANAGER.getWidth(string3);
        float f5 = 0.0f;
        float f6 = 0.35f;
        MutableComponent mutableComponent = Component.empty();
        int n = ((String)object).length();
        int n2 = string2.length();
        for (int i = 0; i < string3.length(); ++i) {
            char c = string3.charAt(i);
            String string4 = String.valueOf(c);
            float f7 = Night.FONT_MANAGER.getWidth(string4);
            float f8 = f4 > 0.0f ? (f5 + f7 / 2.0f) / f4 : 0.0f;
            f5 += f7;
            Color color4 = color2;
            Color color5 = i < n ? (c == '[' || c == ']' || c == ' ' ? color2 : color3) : (i < n + n2 ? (bl ? ColorUtils.getOffsetRainbow((long)i * this.rainbowOffset.getValue().longValue() * 5L) : color) : (c == '[' || c == ']' || c == ' ' ? color2 : color3));
            float f9 = Math.max(0.0f, Math.min(1.0f, (f3 * (1.0f + f6) - f8) / f6));
            float f10 = f9 * f9 * (3.0f - 2.0f * f9);
            Color color6 = HUDModule.lerpColor(color4, color5, f10);
            mutableComponent.append((Component)Component.literal((String)string4).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)color6.getRGB()))));
        }
        Matrix3x2fStack matrix3x2fStack = guiGraphicsExtractor.pose();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(f, f2);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, mutableComponent.getVisualOrderText(), 0, 0, Color.WHITE);
        matrix3x2fStack.popMatrix();
    }

    private void drawText(GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2) {
        this.drawText(guiGraphicsExtractor, string, f, f2, null);
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2, Color color) {
        this.drawText(guiGraphicsExtractor, string, f, f2, this.colorMode.getValue().equals("Rainbow") && this.rainbowMode.getValue().equals("Horizontal"), color);
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2, boolean bl, Color color) {
        if (color == null) {
            color = this.getHudColor(f2);
        }
        Matrix3x2fStack matrix3x2fStack = guiGraphicsExtractor.pose();
        matrix3x2fStack.pushMatrix();
        matrix3x2fStack.translate(f, f2);
        if (bl) {
            Night.FONT_MANAGER.drawRainbowString(guiGraphicsExtractor, string, 0, 0, this.rainbowOffset.getValue().longValue() * 5L);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, 0, 0, color);
        }
        matrix3x2fStack.popMatrix();
    }

    private Color getHudColor(float f) {
        if (this.colorMode.getValue().equalsIgnoreCase("Rainbow")) {
            long l = (long)f / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 10L);
            return ColorUtils.getOffsetRainbow(l);
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Wave")) {
            long l = (long)f / (long)Night.FONT_MANAGER.getHeight() * (this.rainbowOffset.getValue().longValue() * 20L);
            return ColorUtils.getOffsetWave(ColorUtils.getGlobalColor(), l);
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Custom")) {
            return ColorUtils.getColor(this.customColor.getColor(), 255);
        }
        return ColorUtils.getGlobalColor();
    }

    @Override
    public Setting getSetting(String string) {
        if (string.equalsIgnoreCase("ModuleListMode") || string.equalsIgnoreCase("Mode")) {
            return this.moduleListMode;
        }
        return super.getSetting(string);
    }

    private String getModuleText(Module module) {
        boolean bl = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        Object object = "";
        if (bl && module.getBind() != 0) {
            object = String.valueOf(ChatFormatting.GRAY) + "[" + String.valueOf(ChatFormatting.WHITE) + KeyboardUtils.getKeyName(module.getBind()) + String.valueOf(ChatFormatting.GRAY) + "] ";
        }
        if (!bl && !module.isToggled()) {
            return (String)object + String.valueOf(ChatFormatting.GRAY) + module.getName() + (String)(module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : String.valueOf(ChatFormatting.DARK_GRAY) + " [" + String.valueOf(ChatFormatting.GRAY) + module.getMetaData() + String.valueOf(ChatFormatting.DARK_GRAY) + "]");
        }
        return (String)object + module.getName() + (String)(module.getMetaData().isEmpty() || !this.metaData.getValue() ? "" : String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(ChatFormatting.WHITE) + module.getMetaData() + String.valueOf(ChatFormatting.GRAY) + "]");
    }

    private String getPotionText(MobEffectInstance mobEffectInstance) {
        String string;
        if (mobEffectInstance.isInfiniteDuration()) {
            string = "**:**";
        } else {
            int n = Math.round((float)mobEffectInstance.getDuration() / HUDModule.mc.level.tickRateManager().tickrate());
            string = String.format("%02d:%02d", n / 60, n % 60);
        }
        return ((MobEffect)mobEffectInstance.getEffect().value()).getDisplayName().getString() + " " + (mobEffectInstance.getAmplifier() + 1) + " " + String.valueOf(ChatFormatting.WHITE) + string;
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

        public void update(int n) {
            if (this.lastCount == -1) {
                this.lastCount = n;
                this.prevCount = n;
                return;
            }
            if (n != this.lastCount) {
                this.prevCount = this.lastCount;
                this.lastCount = n;
                this.direction = n < this.prevCount ? -1 : 1;
                this.changeTime = System.currentTimeMillis();
            }
        }

        public void render(GuiGraphicsExtractor guiGraphicsExtractor, boolean bl) {
            Color color;
            float f;
            float f2;
            if (this.lastCount <= 0 && this.prevCount <= 0) {
                return;
            }
            String string = String.valueOf(Math.max(0, this.lastCount));
            int n = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getHeight() : 9;
            long l = System.currentTimeMillis() - this.changeTime;
            float f3 = 320.0f;
            float f4 = this.changeTime == 0L ? 1.0f : Math.min(1.0f, (float)l / f3);
            float f5 = f4;
            if (!bl || f4 >= 1.0f || this.prevCount == -1 || this.prevCount == this.lastCount) {
                int n2 = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(string) : Minecraft.getInstance().font.width(string);
                float f6 = 17 - n2;
                float f7 = 9.0f;
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, (int)f6, (int)f7, Color.WHITE);
                } else {
                    guiGraphicsExtractor.text(Minecraft.getInstance().font, string, (int)f6, (int)f7, -1, true);
                }
                return;
            }
            float f8 = Easing.ease(f4, Easing.Method.EASE_OUT_CUBIC);
            String string2 = String.valueOf(Math.max(0, this.prevCount));
            int n3 = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(string) : Minecraft.getInstance().font.width(string);
            int n4 = Night.FONT_MANAGER != null ? Night.FONT_MANAGER.getWidth(string2) : Minecraft.getInstance().font.width(string2);
            float f9 = 17 - n3;
            float f10 = 17 - n4;
            float f11 = 9.0f;
            float f12 = n + 3;
            if (this.direction < 0) {
                f2 = f8 * f12;
                f = -(1.0f - f8) * f12;
            } else {
                f2 = -f8 * f12;
                f = (1.0f - f8) * f12;
            }
            int n5 = Math.max(0, Math.min(255, Math.round((1.0f - f8) * 255.0f)));
            int n6 = Math.max(0, Math.min(255, Math.round(f8 * 255.0f)));
            if (n5 > 5) {
                color = new Color(255, 255, 255, n5);
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, (int)f10, (int)(f11 + f2), color);
                } else {
                    guiGraphicsExtractor.text(Minecraft.getInstance().font, string2, (int)f10, (int)(f11 + f2), n5 << 24 | 0xFFFFFF, true);
                }
            }
            if (n6 > 5) {
                color = new Color(255, 255, 255, n6);
                if (Night.FONT_MANAGER != null) {
                    Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, (int)f9, (int)(f11 + f), color);
                } else {
                    guiGraphicsExtractor.text(Minecraft.getInstance().font, string, (int)f9, (int)(f11 + f), n6 << 24 | 0xFFFFFF, true);
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

