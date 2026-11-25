/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor
 *  lombok.Generated
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.ShulkerBoxMenu
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.input.KeyboardUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.FileUtils;
import lombok.Generated;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Rekit", description="Automatically regears from an ender chest shulker using a saved kit.", category=Module.Category.PLAYER)
public class RekitModule
extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "Tick delay between pull actions.", 1, 0, 10);
    public NumberSetting actionsPerTick = new NumberSetting("Frequency", "Max pull actions per tick.", 1, 1, 5);
    public BooleanSetting auto = new BooleanSetting("Auto", "Auto pull shulkers from the ender chest.", false);
    public BooleanSetting silentContainer = new BooleanSetting("SilentContainer", "Pull items without the container GUI showing on screen.", false);
    public ModeSetting swapMode = new ModeSetting("SwapMode", "Swap type used when placing shulkers and swapping to a breaking tool in Auto mode.", "AltSwap", InventoryUtils.SWITCH_MODES);
    public BooleanSetting considerShulkerName = new BooleanSetting("ConsiderShulkerName", "Prefer the shulker whose custom name matches the active kit's name, overriding content-based scoring.", false);
    public BooleanSetting ignoreCustomName = new BooleanSetting("IgnoreCustomName", "Skip any container with a custom (non-vanilla) title such as a shop GUI, except shulker boxes.", false);
    public BindSetting autoPlaceBind = new BindSetting("AutoPlace", "Keybind: places the shulker in your inventory, opens it, pulls kit items, closes, then breaks it. No ender chest -- one-shot version of Auto.", 0).disableHoldModes();
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing the shulker.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public ModeSetting pickaxePref = new ModeSetting("Pickaxe", "Preferred pickaxe enchant when choosing between candidates.", "Efficiency", new String[]{"Efficiency", "SilkTouch"});
    public ModeSetting helmetPref = new ModeSetting("Helmet", "Preferred helmet enchant when choosing between candidates.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting chestplatePref = new ModeSetting("Chestplate", "Preferred chestplate enchant when choosing between candidates.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting leggingsPref = new ModeSetting("Leggings", "Preferred leggings enchant when choosing between candidates.", "Prot", new String[]{"Blast", "Prot"});
    public ModeSetting bootsPref = new ModeSetting("Boots", "Preferred boots enchant when choosing between candidates.", "Prot", new String[]{"Blast", "Prot"});
    public Map<Integer, KitItem> activeKit = new HashMap<Integer, KitItem>();
    public String activeKitName = "";
    private int ticks = 0;
    private static final String FOLDER = "Lightning/Kits/";
    private static final String LAST_KIT_FILE = "Lightning/last_kit_save.txt";
    public static volatile long lastContainerActionMs = 0L;
    private AutoState autoState = AutoState.IDLE;
    private int autoTicks = 0;
    private int emptyTicks = 0;
    private int shulkerEnderSlot = -1;
    private BlockPos placedShulkerPos = null;
    private long placedShulkerRenderMs = 0L;
    private BlockPos enderChestPos = null;
    private boolean hotbarSwapSettled = false;
    private int hotbarSwapAttempts = 0;
    private static final int MAX_HOTBAR_SWAP_ATTEMPTS = 3;
    private int placeFailStreak = 0;
    private static final int MAX_PLACE_FAIL_STREAK = 3;
    private int cursorWaitTicks = 0;
    private static final int CURSOR_DUMP_AFTER_TICKS = 20;
    private int displacedHotbarSlot = -1;
    private int displacedEnderSlot = -1;
    private boolean autoPlaceActive = false;
    private boolean autoPlaceBindWasDown = false;
    private int savedSlot = -1;
    private static final long PLACE_ANIM_MS = 400L;
    private static final long RECENT_COMBAT_TICKS = 100L;
    private static final int FAR_REACH_DECOY_Y_OFFSET = 2000;

    public boolean isAutoActive() {
        return this.autoState != AutoState.IDLE;
    }

    public RekitModule() {
        this.restoreLastKit();
    }

    private void restoreLastKit() {
        try {
            if (!FileUtils.fileExists(LAST_KIT_FILE)) {
                return;
            }
            String kitName = Files.readString(Paths.get(LAST_KIT_FILE, new String[0])).trim();
            if (kitName.isEmpty()) {
                return;
            }
            File file = new File(FOLDER, kitName + ".json");
            if (!file.exists()) {
                return;
            }
            Gson gson = new Gson();
            try (FileReader fileReader = new FileReader(file);){
                Type type = new TypeToken<Map<Integer, KitItem>>(){}.getType();
                this.activeKit = (Map)gson.fromJson((Reader)fileReader, type);
                this.activeKitName = kitName;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void info(String message) {
        Night.CHAT_MANAGER.tagged(message, this.getName());
    }

    private void error(String message) {
        Night.CHAT_MANAGER.tagged("\u00a7c" + message, this.getName());
    }

    public List<String> getKitNames() {
        ArrayList<String> kitNames = new ArrayList<String>();
        File[] files = new File(FOLDER).listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().endsWith(".json")) continue;
                kitNames.add(file.getName().replace(".json", ""));
            }
        }
        return kitNames;
    }

    public void saveKit(String kitName) {
        if (RekitModule.mc.player == null) {
            return;
        }
        if (RekitModule.mc.player.isCreative() || RekitModule.mc.player.isSpectator()) {
            return;
        }
        HashMap<Integer, KitItem> kitMap = new HashMap<Integer, KitItem>();
        for (int i = 0; i < 36; ++i) {
            int handlerSlot = this.getHandlerSlotPlayerOnly(i);
            ItemStack itemStack = RekitModule.mc.player.inventoryMenu.getSlot(handlerSlot).getItem();
            if (itemStack.isEmpty()) continue;
            KitItem kitItem = new KitItem();
            kitItem.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            kitItem.maxCount = itemStack.getMaxStackSize();
            if (itemStack.has(DataComponents.CUSTOM_NAME)) {
                kitItem.name = itemStack.getHoverName().getString();
            }
            kitMap.put(i, kitItem);
        }
        try {
            FileUtils.createDirectory(FOLDER);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter fileWriter = new FileWriter(new File(FOLDER, kitName + ".json"));){
                gson.toJson(kitMap, (Appendable)fileWriter);
            }
            this.activeKit = kitMap;
            this.activeKitName = kitName;
            Files.writeString(Paths.get(LAST_KIT_FILE, new String[0]), (CharSequence)kitName, new OpenOption[0]);
            this.info("Kit saved and activated: " + kitName);
        }
        catch (Exception exception) {
            this.error("Error saving kit!");
        }
    }

    public void loadKit(String kitName) {
        try {
            File file = new File(FOLDER, kitName + ".json");
            if (!file.exists()) {
                this.error("Kit not found: " + kitName);
                return;
            }
            Gson gson = new Gson();
            try (FileReader fileReader = new FileReader(file);){
                Type type = new TypeToken<Map<Integer, KitItem>>(){}.getType();
                this.activeKit = (Map)gson.fromJson((Reader)fileReader, type);
                this.activeKitName = kitName;
            }
            Files.writeString(Paths.get(LAST_KIT_FILE, new String[0]), (CharSequence)kitName, new OpenOption[0]);
            this.info("Kit loaded: " + kitName);
        }
        catch (Exception exception) {
            this.error("Error occurred while reading kit!");
        }
    }

    public void listKits() {
        File[] files = new File(FOLDER).listFiles();
        if (files == null || files.length == 0) {
            this.info("You don't have any kits.");
            return;
        }
        this.info("Available kits:");
        for (File file : files) {
            if (!file.getName().endsWith(".json")) continue;
            String kitName = file.getName().replace(".json", "");
            if (kitName.equals(this.activeKitName)) {
                Night.CHAT_MANAGER.tagged("\u00a79- " + kitName + " [active]", this.getName());
                continue;
            }
            Night.CHAT_MANAGER.tagged("\u00a77- " + kitName, this.getName());
        }
    }

    public void showActiveKit() {
        if (this.activeKitName == null || this.activeKitName.isEmpty()) {
            this.error("No kit is currently active.");
        } else {
            Night.CHAT_MANAGER.tagged("\u00a7aActive kit: \u00a79" + this.activeKitName, this.getName());
        }
    }

    public void deleteKit(String kitName) {
        File file = new File(FOLDER, kitName + ".json");
        if (file.exists() && file.delete()) {
            if (kitName.equals(this.activeKitName)) {
                this.activeKit.clear();
                this.activeKitName = "";
            }
            this.info("Kit deleted: " + kitName);
        } else {
            this.error("Failed to delete kit.");
        }
    }

    @Override
    public void onEnable() {
        this.autoState = AutoState.IDLE;
        this.autoTicks = 0;
        this.emptyTicks = 0;
        this.shulkerEnderSlot = -1;
        this.placedShulkerPos = null;
        this.enderChestPos = null;
        this.hotbarSwapSettled = false;
        this.cursorWaitTicks = 0;
        this.displacedHotbarSlot = -1;
        this.displacedEnderSlot = -1;
        this.autoPlaceActive = false;
        this.autoPlaceBindWasDown = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (RekitModule.mc.player == null) {
            return;
        }
        if (RekitModule.mc.gui.screen() == null) {
            boolean bindDown = KeyboardUtils.isBindDown(this.autoPlaceBind.getValue());
            if (bindDown && !this.autoPlaceBindWasDown && this.autoState == AutoState.IDLE && !RekitModule.mc.player.isCreative() && !RekitModule.mc.player.isSpectator() && this.findShulkerInInventory() != -1) {
                this.autoPlaceActive = true;
                this.autoState = AutoState.PLACE_SHULKER;
                this.autoTicks = 0;
                this.hotbarSwapSettled = false;
                this.hotbarSwapAttempts = 0;
            }
            this.autoPlaceBindWasDown = bindDown;
        } else {
            this.autoPlaceBindWasDown = false;
        }
        if (RekitModule.mc.player.isCreative() || RekitModule.mc.player.isSpectator()) {
            return;
        }
        if (this.auto.getValue() || this.autoPlaceActive) {
            this.handleAutoTick();
            return;
        }
        this.hideContainerScreenIfSilent();
        if (!RekitModule.mc.player.hasContainerOpen()) {
            return;
        }
        this.manualPullTick();
    }

    private void hideContainerScreenIfSilent() {
        if (this.silentContainer.getValue() && RekitModule.mc.gui.screen() instanceof AbstractContainerScreen && !(RekitModule.mc.gui.screen() instanceof InventoryScreen)) {
            RekitModule.mc.gui.setScreen(null);
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldEvent renderWorldEvent) {
        if (!this.auto.getValue() || this.placedShulkerPos == null) {
            return;
        }
        long elapsedMs = System.currentTimeMillis() - this.placedShulkerRenderMs;
        float progress = Math.min(1.0f, (float)elapsedMs / 400.0f);
        float scale = 0.15f + 0.85f * progress;
        double centerX = (double)this.placedShulkerPos.getX() + 0.5;
        double centerY = (double)this.placedShulkerPos.getY() + 0.5;
        double centerZ = (double)this.placedShulkerPos.getZ() + 0.5;
        double halfExtent = 0.5 * (double)scale;
        AABB aABB = new AABB(centerX - halfExtent, centerY - halfExtent, centerZ - halfExtent, centerX + halfExtent, centerY + halfExtent, centerZ + halfExtent);
        Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, new Color(255, 220, 0, 230));
    }

    private boolean hasCustomContainerTitle() {
        Screen screen = RekitModule.mc.gui.screen();
        if (!(screen instanceof AbstractContainerScreen)) {
            return false;
        }
        AbstractContainerScreen abstractContainerScreen = (AbstractContainerScreen)screen;
        Component component = abstractContainerScreen.getTitle();
        if (component == null) {
            return false;
        }
        return !(component.getContents() instanceof TranslatableContents);
    }

    private void manualPullTick() {
        if (this.activeKit.isEmpty()) {
            return;
        }
        if (this.ignoreCustomName.getValue() && this.hasCustomContainerTitle() && !(RekitModule.mc.player.containerMenu instanceof ShulkerBoxMenu)) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        for (int i = 0; i < this.actionsPerTick.getValue().intValue() && this.pullFromContainerTick(); ++i) {
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleAutoTick() {
        this.hideContainerScreenIfSilent();
        boolean containerOpen = RekitModule.mc.player.hasContainerOpen();
        if (this.autoState == AutoState.IDLE) {
            this.autoPlaceActive = false;
            if (containerOpen) {
                if (RekitModule.mc.player.containerMenu instanceof ShulkerBoxMenu) {
                    this.manualPullTick();
                    return;
                }
                this.detectEnderChestPos();
                if (this.enderChestPos == null) {
                    this.manualPullTick();
                    return;
                }
                if (this.activeKit.isEmpty()) {
                    this.setToggled(false);
                    return;
                }
                this.autoState = AutoState.FIND_SHULKER;
                this.autoTicks = 0;
                this.emptyTicks = 0;
            }
            return;
        }
        if (this.autoState == AutoState.FIND_SHULKER) {
            if (!containerOpen) {
                return;
            }
            if (this.isKitComplete()) {
                RekitModule.mc.player.closeContainer();
                this.autoState = AutoState.IDLE;
                return;
            }
            int containerSlots = RekitModule.mc.player.containerMenu.slots.size() - 36;
            if (containerSlots <= 0) {
                return;
            }
            this.shulkerEnderSlot = this.findShulkerInContainer(containerSlots);
            if (this.shulkerEnderSlot == -1) {
                this.error("Auto stopped: no shulker has the items this kit still needs.");
                RekitModule.mc.player.closeContainer();
                this.autoState = AutoState.IDLE;
                this.setToggled(false);
                return;
            }
            this.autoState = AutoState.GRAB_SHULKER;
            this.autoTicks = 0;
            return;
        }
        if (this.autoState == AutoState.GRAB_SHULKER) {
            if (!containerOpen) {
                return;
            }
            int containerSlots = RekitModule.mc.player.containerMenu.slots.size() - 36;
            if (containerSlots <= 0) {
                return;
            }
            AbstractContainerMenu abstractContainerMenu = RekitModule.mc.player.containerMenu;
            ItemStack itemStack = abstractContainerMenu.getSlot(this.shulkerEnderSlot).getItem();
            if (!this.isShulkerBox(itemStack)) {
                this.autoState = AutoState.FIND_SHULKER;
                return;
            }
            int emptyHotbarSlot = this.firstEmptyHotbarSlot();
            if (emptyHotbarSlot != -1) {
                RekitModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, this.shulkerEnderSlot, emptyHotbarSlot, ContainerInput.SWAP, (Player)RekitModule.mc.player);
            } else {
                int fallbackHotbarSlot = RekitModule.mc.player.getInventory().getSelectedSlot() == 0 ? 1 : 0;
                RekitModule.mc.gameMode.handleContainerInput(abstractContainerMenu.containerId, this.shulkerEnderSlot, fallbackHotbarSlot, ContainerInput.SWAP, (Player)RekitModule.mc.player);
                this.displacedHotbarSlot = fallbackHotbarSlot;
                this.displacedEnderSlot = this.shulkerEnderSlot;
            }
            this.autoState = AutoState.PLACE_SHULKER;
            this.autoTicks = 0;
            this.hotbarSwapSettled = false;
            this.hotbarSwapAttempts = 0;
            return;
        }
        if (this.autoState == AutoState.PLACE_SHULKER) {
            boolean placed;
            if (containerOpen) {
                RekitModule.mc.player.closeContainer();
                return;
            }
            if (this.autoTicks < 1) {
                ++this.autoTicks;
                return;
            }
            int shulkerSlot = this.findShulkerInInventory();
            if (shulkerSlot == -1) {
                this.autoState = AutoState.IDLE;
                return;
            }
            if (shulkerSlot > 8 && !this.hotbarSwapSettled) {
                this.ensureHotbar(shulkerSlot);
                this.hotbarSwapSettled = true;
                return;
            }
            if (shulkerSlot > 8) {
                ++this.hotbarSwapAttempts;
                if (this.hotbarSwapAttempts > 3) {
                    ++this.placeFailStreak;
                    this.autoState = AutoState.RETURN_SHULKER;
                    return;
                }
                this.hotbarSwapSettled = false;
                return;
            }
            BlockPos placePos = this.findPlaceableSpot();
            if (placePos == null) {
                ++this.placeFailStreak;
                this.autoState = AutoState.RETURN_SHULKER;
                return;
            }
            BlockHitResult blockHitResult = this.getHitResultForPlace(placePos);
            if (blockHitResult == null) {
                ++this.placeFailStreak;
                this.autoState = AutoState.RETURN_SHULKER;
                return;
            }
            shulkerSlot = this.findShulkerInInventory();
            if (shulkerSlot < 0 || shulkerSlot > 8) {
                ++this.placeFailStreak;
                this.autoState = AutoState.RETURN_SHULKER;
                return;
            }
            Night.ROTATION_MANAGER.beginBatchRotation();
            try {
                if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                    float[] rotations = RotationUtils.getRotations(blockHitResult.getLocation());
                    Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
                }
                this.swapToSlotNormal(shulkerSlot);
                placed = this.isPositionClear(placePos) && RekitModule.mc.gameMode.useItemOn(RekitModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult) != InteractionResult.FAIL && !RekitModule.mc.level.getBlockState(placePos).canBeReplaced();
            }
            finally {
                Night.ROTATION_MANAGER.endBatchRotation();
            }
            if (!placed) {
                this.swapBackNormal();
                ++this.placeFailStreak;
                this.autoState = AutoState.RETURN_SHULKER;
                return;
            }
            this.placedShulkerPos = placePos;
            this.placedShulkerRenderMs = System.currentTimeMillis();
            this.autoState = AutoState.OPEN_SHULKER;
            this.autoTicks = 0;
            return;
        }
        if (this.autoState == AutoState.OPEN_SHULKER) {
            if (containerOpen) {
                this.swapBackNormal();
                this.autoState = AutoState.PULL_ITEMS;
                this.autoTicks = 0;
                this.emptyTicks = 0;
                this.placeFailStreak = 0;
                return;
            }
            if (this.autoTicks == 0 || this.autoTicks % 4 == 0) {
                BlockHitResult blockHitResult = new BlockHitResult(Vec3.atCenterOf((Vec3i)this.placedShulkerPos), Direction.UP, this.placedShulkerPos, false);
                RekitModule.mc.gameMode.useItemOn(RekitModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult);
            }
            ++this.autoTicks;
            if (this.autoTicks > 60) {
                this.swapBackNormal();
                this.autoState = AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
            }
            return;
        }
        if (this.autoState == AutoState.PULL_ITEMS) {
            if (!containerOpen) {
                this.autoState = AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.activeKit.isEmpty()) {
                this.autoState = AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.ticks < this.delay.getValue().intValue()) {
                ++this.ticks;
                return;
            }
            this.ticks = 0;
            boolean pulledAny = false;
            int containerSlots = RekitModule.mc.player.containerMenu.slots.size() - 36;
            if (containerSlots > 0) {
                for (int i = 0; i < this.actionsPerTick.getValue().intValue() && this.pullFromContainerTick(); ++i) {
                    pulledAny = true;
                }
            }
            this.emptyTicks = pulledAny ? 0 : this.emptyTicks + 1;
            if (this.emptyTicks >= 5) {
                this.autoState = AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
            }
            return;
        }
        if (this.autoState == AutoState.BREAK_SHULKER) {
            if (containerOpen) {
                RekitModule.mc.player.closeContainer();
                return;
            }
            if (this.autoTicks < 2) {
                ++this.autoTicks;
                return;
            }
            if (this.placedShulkerPos == null || RekitModule.mc.level == null) {
                this.autoState = AutoState.IDLE;
                return;
            }
            if (RekitModule.mc.level.getBlockState(this.placedShulkerPos).canBeReplaced()) {
                if (this.findShulkerInInventory() == -1 && this.autoTicks < 100) {
                    ++this.autoTicks;
                    return;
                }
                this.autoState = AutoState.RETURN_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.autoTicks > 200) {
                this.autoState = AutoState.RETURN_SHULKER;
                this.autoTicks = 0;
                return;
            }
            this.instantBreak(this.placedShulkerPos);
            ++this.autoTicks;
            return;
        }
        if (this.autoState == AutoState.RETURN_SHULKER) {
            if (this.enderChestPos == null) {
                this.autoState = AutoState.IDLE;
                return;
            }
            if (!containerOpen) {
                if (this.autoTicks < 2) {
                    ++this.autoTicks;
                    return;
                }
                if (this.autoTicks == 2) {
                    BlockHitResult blockHitResult = new BlockHitResult(Vec3.atCenterOf((Vec3i)this.enderChestPos), Direction.UP, this.enderChestPos, false);
                    RekitModule.mc.gameMode.useItemOn(RekitModule.mc.player, InteractionHand.MAIN_HAND, blockHitResult);
                }
                ++this.autoTicks;
                if (this.autoTicks > 40) {
                    this.autoState = AutoState.IDLE;
                }
                return;
            }
            this.autoTicks = 0;
            int shulkerSlot = this.findShulkerInInventory();
            if (shulkerSlot == -1) {
                if (this.placedShulkerPos != null && RekitModule.mc.level != null && !RekitModule.mc.level.getBlockState(this.placedShulkerPos).canBeReplaced()) {
                    this.autoState = AutoState.BREAK_SHULKER;
                    this.autoTicks = 0;
                    return;
                }
                this.autoState = AutoState.FIND_SHULKER;
                this.autoTicks = 0;
                return;
            }
            int containerSlots = RekitModule.mc.player.containerMenu.slots.size() - 36;
            if (containerSlots <= 0) {
                return;
            }
            AbstractContainerMenu abstractContainerMenu = RekitModule.mc.player.containerMenu;
            if (this.displacedHotbarSlot != -1) {
                int shulkerHandlerSlot = this.getPlayerHandlerSlot(containerSlots, shulkerSlot);
                this.click(abstractContainerMenu.containerId, shulkerHandlerSlot, 0, ContainerInput.PICKUP);
                this.click(abstractContainerMenu.containerId, this.displacedEnderSlot, 0, ContainerInput.PICKUP);
                int displacedHandlerSlot = this.getPlayerHandlerSlot(containerSlots, this.displacedHotbarSlot);
                this.click(abstractContainerMenu.containerId, displacedHandlerSlot, 0, ContainerInput.PICKUP);
                this.displacedHotbarSlot = -1;
                this.displacedEnderSlot = -1;
            } else {
                int slotIndex;
                int emptyEnderSlot = -1;
                for (slotIndex = 0; slotIndex < containerSlots; ++slotIndex) {
                    if (!abstractContainerMenu.getSlot(slotIndex).getItem().isEmpty()) continue;
                    emptyEnderSlot = slotIndex;
                    break;
                }
                if (emptyEnderSlot == -1) {
                    this.autoState = AutoState.IDLE;
                    return;
                }
                slotIndex = this.getPlayerHandlerSlot(containerSlots, shulkerSlot);
                this.atomicSwap(abstractContainerMenu.containerId, slotIndex, emptyEnderSlot);
            }
            this.placedShulkerPos = null;
            this.shulkerEnderSlot = -1;
            if (this.placeFailStreak >= 3) {
                this.error("Auto stopped: could not place/open a shulker after " + this.placeFailStreak + " attempts.");
                RekitModule.mc.player.closeContainer();
                this.autoState = AutoState.IDLE;
                this.setToggled(false);
                this.placeFailStreak = 0;
            } else if (this.isKitComplete()) {
                RekitModule.mc.player.closeContainer();
                this.autoState = AutoState.IDLE;
            } else {
                this.autoState = AutoState.FIND_SHULKER;
            }
            this.autoTicks = 0;
        }
    }

    private void detectEnderChestPos() {
        BlockHitResult blockHitResult;
        HitResult hitResult = RekitModule.mc.hitResult;
        if (hitResult instanceof BlockHitResult && RekitModule.mc.level.getBlockState((blockHitResult = (BlockHitResult)hitResult).getBlockPos()).is(Blocks.ENDER_CHEST)) {
            this.enderChestPos = blockHitResult.getBlockPos();
            return;
        }
        BlockPos playerPos = RekitModule.mc.player.blockPosition();
        for (int i = -3; i <= 3; ++i) {
            for (int j = -1; j <= 3; ++j) {
                for (int k = -3; k <= 3; ++k) {
                    BlockPos checkPos = playerPos.offset(i, j, k);
                    if (!RekitModule.mc.level.getBlockState(checkPos).is(Blocks.ENDER_CHEST)) continue;
                    this.enderChestPos = checkPos;
                    return;
                }
            }
        }
    }

    private int findShulkerInContainer(int containerSlots) {
        AbstractContainerMenu abstractContainerMenu = RekitModule.mc.player.containerMenu;
        HashSet<Item> neededItems = new HashSet<Item>();
        for (Map.Entry<Integer, KitItem> entry : this.activeKit.entrySet()) {
            Item item;
            ItemStack invStack = RekitModule.mc.player.getInventory().getItem(entry.getKey().intValue());
            if (entry.getValue() == null || entry.getValue().id == null || entry.getValue().id.isEmpty() || this.isCorrectItem(invStack, entry.getValue())) continue;
            Identifier itemId = Identifier.tryParse(entry.getValue().id);
            if (itemId == null || (item = (Item)BuiltInRegistries.ITEM.getValue(itemId)) == null) continue;
            neededItems.add(item);
        }
        int bestSlot = -1;
        int bestScore = -1;
        for (int i = 0; i < containerSlots; ++i) {
            ItemStack itemStack = abstractContainerMenu.getSlot(i).getItem();
            if (!this.isShulkerBox(itemStack)) continue;
            int score = 0;
            ItemContainerContents itemContainerContents = (ItemContainerContents)itemStack.get(DataComponents.CONTAINER);
            if (itemContainerContents != null) {
                for (ItemStack contentStack : itemContainerContents.nonEmptyItemCopyStream().toList()) {
                    if (!neededItems.contains(contentStack.getItem())) continue;
                    score += contentStack.getMaxStackSize() == 1 ? 100 : 1;
                }
            }
            if (this.considerShulkerName.getValue() && !this.activeKitName.isEmpty() && itemStack.has(DataComponents.CUSTOM_NAME) && RekitModule.normalizeName(itemStack.getHoverName().getString()).equalsIgnoreCase(RekitModule.normalizeName(this.activeKitName))) {
                score += 1000000;
            }
            if (score <= bestScore) continue;
            bestScore = score;
            bestSlot = i;
        }
        return bestScore <= 0 ? -1 : bestSlot;
    }

    private int ensureHotbar(int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return invSlot;
        }
        int selectedSlot = RekitModule.mc.player.getInventory().getSelectedSlot();
        RekitModule.mc.gameMode.handleContainerInput(RekitModule.mc.player.inventoryMenu.containerId, this.getHandlerSlotPlayerOnly(invSlot), selectedSlot, ContainerInput.SWAP, (Player)RekitModule.mc.player);
        return selectedSlot;
    }

    private int findShulkerInInventory() {
        for (int i = 0; i < 36; ++i) {
            if (!this.isShulkerBox(RekitModule.mc.player.getInventory().getItem(i))) continue;
            return i;
        }
        return -1;
    }

    private int firstEmptyHotbarSlot() {
        for (int i = 0; i <= 8; ++i) {
            if (!RekitModule.mc.player.getInventory().getItem(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private Player getActiveCombatOpponent() {
        LivingEntity livingEntity = RekitModule.mc.player.getLastHurtByMob();
        if (!(livingEntity instanceof Player)) {
            return null;
        }
        Player player = (Player)livingEntity;
        long ticksSinceHurt = RekitModule.mc.level.getGameTime() - (long)RekitModule.mc.player.getLastHurtByMobTimestamp();
        return ticksSinceHurt <= 100L ? player : null;
    }

    private boolean isPositionClear(BlockPos blockPos) {
        return RekitModule.mc.level.getEntities((Entity)null, new AABB(blockPos), Entity::isAlive).isEmpty();
    }

    private BlockPos findPlaceableSpot() {
        BlockPos playerPos = RekitModule.mc.player.blockPosition();
        Player opponent = this.getActiveCombatOpponent();
        int reach = (int)Math.ceil(RekitModule.mc.player.blockInteractionRange());
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;
        for (int i = -reach; i <= reach; ++i) {
            for (int j = -reach; j <= reach; ++j) {
                BlockPos candidatePos = playerPos.offset(i, 0, j);
                if (candidatePos.distSqr((Vec3i)playerPos) > (double)reach * (double)reach || candidatePos.below().equals((Object)this.enderChestPos) || !this.isPositionClear(candidatePos) || !RekitModule.mc.level.getBlockState(candidatePos).canBeReplaced() || RekitModule.mc.level.getBlockState(candidatePos.below()).canBeReplaced() || RekitModule.mc.level.getBlockState(candidatePos.below()).is(Blocks.AIR) || !RekitModule.mc.level.getBlockState(candidatePos.above()).canBeReplaced()) continue;
                double score = opponent != null ? -candidatePos.distSqr((Vec3i)opponent.blockPosition()) : (double)(i * i + j * j);
                if (!(score < bestScore)) continue;
                bestScore = score;
                bestPos = candidatePos;
            }
        }
        return bestPos;
    }

    private BlockHitResult getHitResultForPlace(BlockPos blockPos) {
        Direction direction = WorldUtils.getDirection(blockPos, null, this.strictDirection.getValue());
        if (direction == null) {
            return null;
        }
        BlockPos supportPos = blockPos.relative(direction);
        Vec3 hitVec = Vec3.atCenterOf((Vec3i)supportPos).add((double)(-direction.getStepX()) * 0.48, (double)(-direction.getStepY()) * 0.48, (double)(-direction.getStepZ()) * 0.48);
        return new BlockHitResult(hitVec, direction.getOpposite(), supportPos, false);
    }

    private boolean isKitComplete() {
        if (this.activeKit.isEmpty()) {
            return true;
        }
        for (Map.Entry<Integer, KitItem> entry : this.activeKit.entrySet()) {
            if (this.isCorrectItem(RekitModule.mc.player.getInventory().getItem(entry.getKey().intValue()), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    private boolean pullFromContainerTick() {
        AbstractContainerMenu abstractContainerMenu = RekitModule.mc.player.containerMenu;
        int containerSlots = abstractContainerMenu.slots.size() - 36;
        if (containerSlots <= 0) {
            return false;
        }
        if (!abstractContainerMenu.getCarried().isEmpty()) {
            int slotIndex;
            ++this.cursorWaitTicks;
            if (this.cursorWaitTicks < 20) {
                return false;
            }
            this.cursorWaitTicks = 0;
            int emptySlot = -1;
            for (slotIndex = 0; slotIndex < containerSlots; ++slotIndex) {
                if (!abstractContainerMenu.getSlot(slotIndex).getItem().isEmpty()) continue;
                emptySlot = slotIndex;
                break;
            }
            if (emptySlot == -1) {
                for (slotIndex = 0; slotIndex < 36; ++slotIndex) {
                    int handlerSlot = this.getPlayerHandlerSlot(containerSlots, slotIndex);
                    if (!abstractContainerMenu.getSlot(handlerSlot).getItem().isEmpty()) continue;
                    emptySlot = handlerSlot;
                    break;
                }
            }
            if (emptySlot != -1) {
                this.click(abstractContainerMenu.containerId, emptySlot, 0, ContainerInput.PICKUP);
                return true;
            }
            return false;
        }
        this.cursorWaitTicks = 0;
        for (int invSlot = 0; invSlot < 36; ++invSlot) {
            ItemStack candidateStack;
            int sourceSlot;
            KitItem kitItem = this.activeKit.get(invSlot);
            if (kitItem == null) continue;
            int targetHandlerSlot = this.getPlayerHandlerSlot(containerSlots, invSlot);
            ItemStack currentStack = abstractContainerMenu.getSlot(targetHandlerSlot).getItem();
            boolean isCorrect = this.isCorrectItem(currentStack, kitItem);
            if (!isCorrect) {
                if (this.isShulkerBox(currentStack) || (sourceSlot = this.findBestItemInContainer(abstractContainerMenu, containerSlots, kitItem)) == -1) continue;
                this.atomicSwap(abstractContainerMenu.containerId, sourceSlot, targetHandlerSlot);
                return true;
            }
            if (currentStack.getCount() >= currentStack.getMaxStackSize()) continue;
            sourceSlot = this.findExactItemInContainer(abstractContainerMenu, containerSlots, currentStack);
            if (sourceSlot != -1) {
                this.atomicSwap(abstractContainerMenu.containerId, sourceSlot, targetHandlerSlot);
                return true;
            }
            int betterSlot = this.findBestItemInContainer(abstractContainerMenu, containerSlots, kitItem);
            if (betterSlot == -1 || (candidateStack = abstractContainerMenu.getSlot(betterSlot).getItem()).getCount() <= currentStack.getCount()) continue;
            this.atomicSwap(abstractContainerMenu.containerId, betterSlot, targetHandlerSlot);
            return true;
        }
        for (int invSlot = 0; invSlot < 36; ++invSlot) {
            int targetHandlerSlot;
            ItemStack currentStack;
            KitItem kitItem = this.activeKit.get(invSlot);
            if (kitItem == null || !this.isShulkerBox(currentStack = abstractContainerMenu.getSlot(targetHandlerSlot = this.getPlayerHandlerSlot(containerSlots, invSlot)).getItem()) || this.isItemCompensated(abstractContainerMenu, containerSlots, kitItem)) continue;
            int sourceSlot = this.findBestItemInContainer(abstractContainerMenu, containerSlots, kitItem);
            int emptyUnassignedSlot = this.findEmptyUnassignedSlot(abstractContainerMenu, containerSlots);
            if (sourceSlot == -1 || emptyUnassignedSlot == -1) continue;
            this.atomicSwap(abstractContainerMenu.containerId, sourceSlot, emptyUnassignedSlot);
            return true;
        }
        return false;
    }

    private void atomicSwap(int containerId, int slotA, int slotB) {
        this.click(containerId, slotA, 0, ContainerInput.PICKUP);
        this.click(containerId, slotB, 0, ContainerInput.PICKUP);
        this.click(containerId, slotA, 0, ContainerInput.PICKUP);
    }

    private void click(int containerId, int slotIndex, int button, ContainerInput containerInput) {
        lastContainerActionMs = System.currentTimeMillis();
        RekitModule.mc.gameMode.handleContainerInput(containerId, slotIndex, button, containerInput, (Player)RekitModule.mc.player);
    }

    private void swapToSlotNormal(int slot) {
        this.savedSlot = RekitModule.mc.player.getInventory().getSelectedSlot();
        RekitModule.mc.player.getInventory().setSelectedSlot(slot);
        ((ClientPlayerInteractionManagerAccessor)RekitModule.mc.gameMode).invokeSyncSelectedSlot();
    }

    private void swapBackNormal() {
        if (this.savedSlot == -1) {
            return;
        }
        RekitModule.mc.player.getInventory().setSelectedSlot(this.savedSlot);
        ((ClientPlayerInteractionManagerAccessor)RekitModule.mc.gameMode).invokeSyncSelectedSlot();
        this.savedSlot = -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void instantBreak(BlockPos blockPos) {
        Direction direction = Direction.UP;
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        boolean farReach = speedMineModule != null && speedMineModule.farReach.getValue();
        Night.ROTATION_MANAGER.beginBatchRotation();
        try {
            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                float[] rotations = RotationUtils.getRotations(WorldUtils.getHitVector(blockPos, direction));
                Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
            }
            mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
            if (farReach) {
                BlockPos farPos = blockPos.below(2000);
                mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, farPos, WorldUtils.getClosestDirection(farPos, true)));
            }
            mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
    }

    private boolean isCorrectItem(ItemStack itemStack, KitItem kitItem) {
        if (kitItem == null) {
            return true;
        }
        if (itemStack.isEmpty()) {
            return false;
        }
        if (kitItem.id == null || kitItem.id.isEmpty()) {
            return false;
        }
        Identifier identifier = Identifier.tryParse((String)kitItem.id);
        if (identifier == null) {
            return false;
        }
        Item item = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        return item != null && itemStack.getItem() == item;
    }

    private int findBestItemInContainer(AbstractContainerMenu abstractContainerMenu, int containerSlots, KitItem kitItem) {
        if (kitItem == null || kitItem.id == null || kitItem.id.isEmpty()) {
            return -1;
        }
        Identifier identifier = Identifier.tryParse((String)kitItem.id);
        if (identifier == null) {
            return -1;
        }
        Item item = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        if (item == null) {
            return -1;
        }
        int bestSlot = -1;
        long bestScore = -1L;
        for (int i = 0; i < containerSlots; ++i) {
            long score;
            ItemStack itemStack = abstractContainerMenu.getSlot(i).getItem();
            if (itemStack.isEmpty() || itemStack.getItem() != item || (score = this.scoreCandidate(itemStack)) <= bestScore) continue;
            bestScore = score;
            bestSlot = i;
        }
        return bestSlot;
    }

    private long scoreCandidate(ItemStack itemStack) {
        if (itemStack.getMaxStackSize() > 1) {
            return itemStack.getCount();
        }
        long score = 0L;
        String enchantId = this.preferredEnchantId(itemStack);
        if (enchantId != null && this.hasEnchant(itemStack, enchantId)) {
            score += 1000000L;
        }
        if (itemStack.isDamageableItem()) {
            score += (long)(itemStack.getMaxDamage() - itemStack.getDamageValue());
        }
        return score;
    }

    private String preferredEnchantId(ItemStack itemStack) {
        String prefValue;
        String itemPath = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
        if (itemPath.endsWith("_pickaxe")) {
            return this.pickaxePref.getValue().equalsIgnoreCase("SilkTouch") ? "silk_touch" : "efficiency";
        }
        if (itemPath.endsWith("_helmet") || itemPath.equals("turtle_helmet")) {
            prefValue = this.helmetPref.getValue();
        } else if (itemPath.endsWith("_chestplate")) {
            prefValue = this.chestplatePref.getValue();
        } else if (itemPath.endsWith("_leggings")) {
            prefValue = this.leggingsPref.getValue();
        } else if (itemPath.endsWith("_boots")) {
            prefValue = this.bootsPref.getValue();
        } else {
            return null;
        }
        return prefValue.equalsIgnoreCase("Blast") ? "blast_protection" : "protection";
    }

    private boolean hasEnchant(ItemStack itemStack, String enchantPath) {
        ItemEnchantments itemEnchantments = (ItemEnchantments)itemStack.get(DataComponents.ENCHANTMENTS);
        if (itemEnchantments == null || itemEnchantments.isEmpty()) {
            return false;
        }
        Identifier identifier = Identifier.tryParse((String)("minecraft:" + enchantPath));
        if (identifier == null) {
            return false;
        }
        for (Holder holder : itemEnchantments.keySet()) {
            if (!holder.is(identifier)) continue;
            return true;
        }
        return false;
    }

    private int findExactItemInContainer(AbstractContainerMenu abstractContainerMenu, int containerSlots, ItemStack targetStack) {
        for (int i = 0; i < containerSlots; ++i) {
            ItemStack candidateStack = abstractContainerMenu.getSlot(i).getItem();
            if (candidateStack.isEmpty() || !ItemStack.isSameItemSameComponents((ItemStack)candidateStack, (ItemStack)targetStack)) continue;
            return i;
        }
        return -1;
    }

    private int getPlayerHandlerSlot(int containerSlots, int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return containerSlots + 27 + invSlot;
        }
        if (invSlot >= 9 && invSlot <= 35) {
            return containerSlots + (invSlot - 9);
        }
        return -1;
    }

    private int getHandlerSlotPlayerOnly(int invSlot) {
        if (invSlot >= 0 && invSlot <= 8) {
            return 36 + invSlot;
        }
        if (invSlot >= 9 && invSlot <= 35) {
            return invSlot;
        }
        return -1;
    }

    private static String normalizeName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFKC).trim();
    }

    private boolean isShulkerBox(ItemStack itemStack) {
        BlockItem blockItem;
        Item item;
        return !itemStack.isEmpty() && (item = itemStack.getItem()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock;
    }

    private boolean isItemCompensated(AbstractContainerMenu abstractContainerMenu, int containerSlots, KitItem kitItem) {
        if (kitItem == null || kitItem.id == null || kitItem.id.isEmpty()) {
            return false;
        }
        Identifier identifier = Identifier.tryParse((String)kitItem.id);
        if (identifier == null) {
            return false;
        }
        Item item = (Item)BuiltInRegistries.ITEM.getValue(identifier);
        if (item == null) {
            return false;
        }
        for (int i = 0; i < 36; ++i) {
            int handlerSlot;
            if (this.activeKit.get(i) != null || abstractContainerMenu.getSlot(handlerSlot = this.getPlayerHandlerSlot(containerSlots, i)).getItem().getItem() != item) continue;
            return true;
        }
        return false;
    }

    private int findEmptyUnassignedSlot(AbstractContainerMenu abstractContainerMenu, int containerSlots) {
        for (int i = 0; i < 36; ++i) {
            int handlerSlot;
            if (this.activeKit.get(i) != null || !abstractContainerMenu.getSlot(handlerSlot = this.getPlayerHandlerSlot(containerSlots, i)).getItem().isEmpty()) continue;
            return handlerSlot;
        }
        return -1;
    }

    @Generated
    public Map<Integer, KitItem> getActiveKit() {
        return this.activeKit;
    }

    @Generated
    public String getActiveKitName() {
        return this.activeKitName;
    }

    private static enum AutoState {
        IDLE,
        FIND_SHULKER,
        GRAB_SHULKER,
        PLACE_SHULKER,
        OPEN_SHULKER,
        PULL_ITEMS,
        BREAK_SHULKER,
        RETURN_SHULKER;

    }

    public static class KitItem {
        public String id;
        public String name;
        public int maxCount;
    }

    public static enum PickaxePref {
        Efficiency,
        SilkTouch;

    }

    public static enum ArmorPref {
        Blast,
        Prot;

    }
}

