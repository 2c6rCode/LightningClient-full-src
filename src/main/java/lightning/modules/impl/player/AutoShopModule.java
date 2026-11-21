/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 */
package lightning.modules.impl.player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.AutoShopSync;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@RegisterModule(name="AutoShop", description="Automates the server /shop GUI to buy totems, end crystals, or exp bottles.", category=Module.Category.PLAYER, persistent=true)
public class AutoShopModule
extends Module {
    public CategorySetting totemCategory = new CategorySetting("Totem", "Automatic Totem restock and manual purchase settings.");
    public BooleanSetting totemBelowThreshold = new BooleanSetting("TotemBelowThreshold", "Enabled", "Automatically restock when Totem stock is at or below the threshold.", new CategorySetting.Visibility(this.totemCategory), false);
    public BindSetting totem = new BindSetting("Totem", "Bind", "Keybind to auto buy totems.", new CategorySetting.Visibility(this.totemCategory), 0).disableHoldModes();
    public BindSetting totemToggle = new BindSetting("TotemToggle", "Toggle", "Keybind to turn auto buy totems on/off.", new CategorySetting.Visibility(this.totemCategory), 0).disableHoldModes();
    public NumberSetting totemAmount = new NumberSetting("TotemAmount", "Amount", "How many totems to buy.", new CategorySetting.Visibility(this.totemCategory), 1, 1, 10);
    public NumberSetting totemLostCount = new NumberSetting("TotemLostCount", "Stock Threshold", "Automatically buy when total Totem stock is at or below this threshold.", new CategorySetting.Visibility(this.totemCategory), 7, 1, 64);
    public CategorySetting crystalCategory = new CategorySetting("Crystal", "Automatic Crystal restock and manual purchase settings.");
    public BooleanSetting crystalBelowThreshold = new BooleanSetting("CrystalBelowThreshold", "Enabled", "Buy one Crystal order when stock reaches the threshold.", new CategorySetting.Visibility(this.crystalCategory), false);
    public BindSetting crystal = new BindSetting("Crystal", "Bind", "Keybind to auto buy end crystals.", new CategorySetting.Visibility(this.crystalCategory), 0).disableHoldModes();
    public NumberSetting crystalStacks = new NumberSetting("CrystalStacks", "Stacks", "How many stacks (64) of end crystals to buy.", new CategorySetting.Visibility(this.crystalCategory), 1, 1, 10);
    public NumberSetting crystalThreshold = new NumberSetting("CrystalThreshold", "Threshold", "Crystal stack threshold.", new CategorySetting.Visibility(this.crystalCategory), 1, 0, 20);
    public CategorySetting expCategory = new CategorySetting("Exp", "Automatic Exp restock and manual purchase settings.");
    public BooleanSetting expBelowThreshold = new BooleanSetting("ExpBelowThreshold", "Enabled", "Buy one Exp order when stock reaches the threshold.", new CategorySetting.Visibility(this.expCategory), false);
    public BindSetting exp = new BindSetting("Exp", "Bind", "Keybind to auto buy exp bottles.", new CategorySetting.Visibility(this.expCategory), 0).disableHoldModes();
    public NumberSetting expStacks = new NumberSetting("ExpStacks", "Stacks", "How many stacks (64) of exp bottles to buy.", new CategorySetting.Visibility(this.expCategory), 1, 1, 10);
    public NumberSetting expThreshold = new NumberSetting("ExpThreshold", "Threshold", "Exp stack threshold.", new CategorySetting.Visibility(this.expCategory), 1, 0, 20);
    public CategorySetting obsidianCategory = new CategorySetting("Obsidian", "Automatic Obsidian restock and manual purchase settings.");
    public BooleanSetting obsidianBelowThreshold = new BooleanSetting("ObsidianBelowThreshold", "Enabled", "Buy one Obsidian order when stock reaches the threshold.", new CategorySetting.Visibility(this.obsidianCategory), false);
    public BindSetting obsidian = new BindSetting("Obsidian", "Bind", "Keybind to auto buy obsidian.", new CategorySetting.Visibility(this.obsidianCategory), 0).disableHoldModes();
    public NumberSetting obsidianStacks = new NumberSetting("ObsidianStacks", "Stacks", "How many stacks (64) of obsidian to buy.", new CategorySetting.Visibility(this.obsidianCategory), 1, 1, 10);
    public NumberSetting obsidianThreshold = new NumberSetting("ObsidianThreshold", "Threshold", "Obsidian stack threshold.", new CategorySetting.Visibility(this.obsidianCategory), 1, 0, 20);
    public CategorySetting enderPearlCategory = new CategorySetting("EnderPearl", "Automatic EnderPearl restock and manual purchase settings.");
    public BooleanSetting enderPearlBelowThreshold = new BooleanSetting("EnderPearlBelowThreshold", "Enabled", "Buy one EnderPearl order when stock reaches the threshold.", new CategorySetting.Visibility(this.enderPearlCategory), false);
    public BindSetting enderPearl = new BindSetting("EnderPearl", "Bind", "Keybind to auto buy ender pearls.", new CategorySetting.Visibility(this.enderPearlCategory), 0).disableHoldModes();
    public NumberSetting enderPearlStacks = new NumberSetting("EnderPearlStacks", "Stacks", "How many stacks (64) of ender pearls to buy.", new CategorySetting.Visibility(this.enderPearlCategory), 1, 1, 10);
    public NumberSetting enderPearlThreshold = new NumberSetting("EnderPearlThreshold", "Threshold", "EnderPearl stack threshold.", new CategorySetting.Visibility(this.enderPearlCategory), 1, 0, 20);
    public BooleanSetting silent = new BooleanSetting("Silent", "Suppress expected real shop screens before presentation.", false);
    public NumberSetting actionDelay;
    private static final int SLOT_ITEM_TOTEM = 13;
    private static final int SLOT_ITEM_CRYSTAL = 10;
    private static final int SLOT_ITEM_EXP = 16;
    private static final int SLOT_ITEM_OBSIDIAN = 9;
    private static final int SLOT_ITEM_ENDERPEARL = 14;
    private static final int SLOT_SET_1 = 15;
    private static final int SLOT_SET_11 = 16;
    private static final int SLOT_CONFIRM = 23;
    private static final int STEP_TIMEOUT_TICKS = 100;
    private State state;
    private final Queue<PurchaseOrder> orderQueue;
    private PurchaseOrder currentOrder = null;
    private int ticks = 0;
    private int timeoutTicks = 0;
    private boolean quantitySet = false;
    private final AutoControl[] autoControls;
    private boolean totemTrackingInitialized;
    private boolean totemContainerResyncPending;
    private boolean totemZeroFailsafePending;
    private boolean totemZeroFailsafeUsed;
    private boolean totemOrderRecovered;
    private String totemEpisodeTriggerReason = "LOSS";
    private int baselineTotems;
    private int previousObservedTotems;
    private Object lastWorld;
    private Object lastPlayer;
    private long generation;
    private long expectedGeneration;
    private ResponseReason expectedReason;
    private long expectationExpiresAt;
    private boolean realShopScreenIntercepted;
    private boolean silentSession;
    public BooleanSetting antiGhostTotem = new BooleanSetting("AntiGhostTotem", "Stops AutoTotem from swapping while AutoShop is buying (keybind or auto), preventing ghost totems.", true);
    private String lastBuyMsg;

    public AutoShopModule() {
        this.actionDelay = new NumberSetting("ActionDelay", "Ticks between each shop GUI click.", 1, 0, 20);
        this.state = State.IDLE;
        this.orderQueue = new ConcurrentLinkedQueue<PurchaseOrder>();
        this.autoControls = new AutoControl[]{new AutoControl(), new AutoControl(), new AutoControl(), new AutoControl(), new AutoControl()};
        this.expectedReason = ResponseReason.NONE;
        this.bind.disableHoldModes();
    }

    public void handleKey(int n) {
        this.totemToggleKey(n);
        if (n == 0) {
            return;
        }
        if (this.totem.getValue() != 0 && n == this.totem.getValue()) {
            this.enqueueOrder(ShopMode.Totem, this.totemAmount.getValue().intValue());
        } else if (this.crystal.getValue() != 0 && n == this.crystal.getValue()) {
            this.enqueueOrder(ShopMode.Crystal, this.crystalStacks.getValue().intValue());
        } else if (this.exp.getValue() != 0 && n == this.exp.getValue()) {
            this.enqueueOrder(ShopMode.Exp, this.expStacks.getValue().intValue());
        } else if (this.obsidian.getValue() != 0 && n == this.obsidian.getValue()) {
            this.enqueueOrder(ShopMode.Obsidian, this.obsidianStacks.getValue().intValue());
        } else if (this.enderPearl.getValue() != 0 && n == this.enderPearl.getValue()) {
            this.enqueueOrder(ShopMode.EnderPearl, this.enderPearlStacks.getValue().intValue());
        }
    }

    public synchronized void enqueueOrder(ShopMode shopMode, int n) {
        if (!this.canAccept(AutoShopModule.itemFor(shopMode))) {
            return;
        }
        Night.LOGGER.info("[AutoShop] MANUAL_REQUEST mode={} amount={}", (Object)shopMode, (Object)n);
        this.enqueuePurchaseOrder(new PurchaseOrder(shopMode, n));
    }

    private synchronized void enqueuePurchaseOrder(PurchaseOrder purchaseOrder) {
        if (AutoShopModule.mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (purchaseOrder.remaining <= 0) {
            return;
        }
        this.orderQueue.add(purchaseOrder);
        if (this.state == State.IDLE) {
            this.currentOrder = this.orderQueue.poll();
            this.beginOrder();
        }
    }

    private void beginOrder() {
        ++this.generation;
        this.clearSilentState();
        this.lastWorld = AutoShopModule.mc.level;
        this.lastPlayer = AutoShopModule.mc.player;
        this.ticks = 0;
        this.timeoutTicks = 0;
        this.quantitySet = false;
        this.sendShopCommand();
        this.state = State.WAIT_SHOP;
    }

    private void sendShopCommand() {
        if (mc.getConnection() != null) {
            this.armExpectedResponse(ResponseReason.SHOP_COMMAND);
            Night.LOGGER.info("[AutoShop] SHOP_COMMAND source={} mode={}", this.currentOrder == null ? "unknown" : this.currentOrder.origin, this.currentOrder == null ? "unknown" : this.currentOrder.mode);
            mc.getConnection().sendCommand("shop open gear");
        }
    }

    public synchronized ScreenDecision classifyScreen(Screen screen) {
        AbstractContainerScreen abstractContainerScreen;
        block7: {
            block6: {
                if (this.state == State.IDLE || !this.silent.getValue() || this.currentOrder == null || !(screen instanceof AbstractContainerScreen)) break block6;
                abstractContainerScreen = (AbstractContainerScreen)screen;
                if (!(screen instanceof InventoryScreen)) break block7;
            }
            return ScreenDecision.PASS;
        }
        if (AutoShopModule.mc.player == null || AutoShopModule.mc.player.containerMenu == null || AutoShopModule.mc.player.containerMenu == AutoShopModule.mc.player.inventoryMenu || abstractContainerScreen.getMenu() != AutoShopModule.mc.player.containerMenu || AutoShopModule.mc.gui.screen() != null) {
            this.resetTransactionWithoutClosingContainer("Unexpected container structure");
            return ScreenDecision.FAIL_OPEN_UNEXPECTED;
        }
        this.expireExpectedResponse();
        if (this.expectedReason != ResponseReason.NONE && this.expectedGeneration != this.generation) {
            this.resetTransactionWithoutClosingContainer("No valid expected shop response");
            return ScreenDecision.FAIL_OPEN_UNEXPECTED;
        }
        ResponseReason responseReason = this.expectedReason;
        Night.LOGGER.info("[AutoShop] SHOP_SCREEN_INTERCEPT state={} reason={} generation={} containerId={} screenClass={}", new Object[]{this.state, responseReason, this.generation, AutoShopModule.mc.player.containerMenu.containerId, screen.getClass().getSimpleName()});
        if (responseReason == ResponseReason.SHOP_COMMAND) {
            this.realShopScreenIntercepted = true;
        }
        this.silentSession = true;
        this.expectedReason = ResponseReason.NONE;
        Night.LOGGER.info("[AutoShop] SILENT_SUPPRESS generation={} containerId={}", (Object)this.generation, (Object)AutoShopModule.mc.player.containerMenu.containerId);
        return ScreenDecision.SUPPRESS_EXPECTED;
    }

    private void armExpectedResponse(ResponseReason responseReason) {
        if (!this.silent.getValue() || this.currentOrder == null) {
            return;
        }
        this.expectedReason = responseReason;
        this.expectedGeneration = this.generation;
        this.expectationExpiresAt = System.nanoTime() + 2000000000L;
        Night.LOGGER.info("[AutoShop] SILENT_EXPECT reason={} generation={} mode={}", new Object[]{responseReason, this.generation, this.currentOrder.mode});
    }

    private void expireExpectedResponse() {
        if (this.expectedReason != ResponseReason.NONE && System.nanoTime() - this.expectationExpiresAt >= 0L) {
            Night.LOGGER.info("[AutoShop] SILENT_EXPECT_EXPIRE reason={} generation={}", (Object)this.expectedReason, (Object)this.expectedGeneration);
            this.expectedReason = ResponseReason.NONE;
        }
    }

    private void clearSilentState() {
        this.expectedReason = ResponseReason.NONE;
        this.expectedGeneration = 0L;
        this.realShopScreenIntercepted = false;
        this.silentSession = false;
    }

    private void resetTransactionWithoutClosingContainer(String string) {
        Night.LOGGER.warn("[AutoShop] UNEXPECTED_SCREEN FAIL_OPEN_RESET reason={} state={} generation={}", new Object[]{string, this.state, this.generation});
        this.markAutoFailure();
        this.totemOrderRecovered = false;
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.quantitySet = false;
        this.ticks = 0;
        this.timeoutTicks = 0;
        ++this.generation;
        this.clearSilentState();
    }

    public boolean isRunning() {
        return this.state != State.IDLE;
    }

    private void resetAutoControls() {
        for (AutoControl autoControl : this.autoControls) {
            autoControl.reset();
        }
    }

    private AutoControl totemAutoControl() {
        return this.autoControls[ShopMode.Totem.ordinal()];
    }

    private void clearTotemTracking() {
        this.totemTrackingInitialized = false;
        this.totemContainerResyncPending = false;
        this.totemZeroFailsafePending = false;
        this.totemZeroFailsafeUsed = false;
        this.totemOrderRecovered = false;
        this.totemEpisodeTriggerReason = "LOSS";
        this.baselineTotems = 0;
        this.previousObservedTotems = 0;
        this.totemAutoControl().reset();
    }

    private void resetTotemBaseline(int n, String string) {
        int n2 = this.baselineTotems;
        this.baselineTotems = n;
        this.previousObservedTotems = n;
        this.totemTrackingInitialized = true;
        this.totemContainerResyncPending = false;
        this.totemZeroFailsafePending = false;
        this.totemZeroFailsafeUsed = false;
        this.totemEpisodeTriggerReason = "LOSS";
        AutoControl autoControl = this.totemAutoControl();
        autoControl.reset();
        autoControl.wasEnabled = true;
        Night.LOGGER.info("[AutoShop] TOTEM_BASELINE_RESET reason={} old={} new={}", new Object[]{string, n2, n});
    }

    private boolean isTotemAutoOrder() {
        return this.currentOrder != null && this.currentOrder.mode == ShopMode.Totem && this.currentOrder.origin != OrderOrigin.MANUAL;
    }

    private boolean isUnrelatedInventorySession() {
        boolean bl;
        Screen screen = AutoShopModule.mc.gui.screen();
        boolean bl2 = bl = this.state != State.IDLE && this.currentOrder != null;
        if (screen instanceof InventoryScreen) {
            return true;
        }
        if (screen instanceof AbstractContainerScreen) {
            return !bl;
        }
        if (AutoShopModule.mc.player.hasContainerOpen()) {
            return !bl || !this.silent.getValue() || !this.silentSession && this.expectedReason == ResponseReason.NONE;
        }
        return false;
    }

    private void observeTotemStock() {
        AutoControl autoControl = this.totemAutoControl();
        if (!this.totemBelowThreshold.getValue()) {
            if (this.totemTrackingInitialized || autoControl.wasEnabled) {
                Night.LOGGER.info("[AutoShop] TOTEM_AUTO_DISABLE");
                this.clearTotemTracking();
            }
            return;
        }
        if (AutoShopModule.mc.player == null || AutoShopModule.mc.level == null || mc.getConnection() == null || !AutoShopModule.mc.player.isAlive() || AutoShopModule.mc.player.isDeadOrDying() || AutoShopModule.mc.player.getHealth() <= 0.0f) {
            if (this.totemTrackingInitialized) {
                this.clearTotemTracking();
            }
            return;
        }
        int n = this.stockCount(Items.TOTEM_OF_UNDYING);
        if (this.isUnrelatedInventorySession()) {
            this.totemContainerResyncPending = true;
            return;
        }
        if (this.totemContainerResyncPending) {
            this.resetTotemBaseline(n, "CONTAINER_RESYNC");
            return;
        }
        if (!this.totemTrackingInitialized) {
            this.resetTotemBaseline(n, "ENABLE/SESSION");
            Night.LOGGER.info("[AutoShop] TOTEM_AUTO_ENABLE baseline={}", (Object)n);
            return;
        }
        if (n > this.previousObservedTotems) {
            if (this.isTotemAutoOrder()) {
                this.totemOrderRecovered = true;
            }
            this.resetTotemBaseline(n, "STOCK_INCREASE");
            Night.LOGGER.info("[AutoShop] TOTEM_AUTO_RECOVER stock={}", (Object)n);
            return;
        }
        if (n < this.previousObservedTotems) {
            if (this.previousObservedTotems > 0 && n == 0 && this.baselineTotems > 0 && !this.totemZeroFailsafeUsed) {
                this.totemZeroFailsafePending = true;
                Night.LOGGER.info("[AutoShop] TOTEM_ZERO_FAILSAFE baseline={} current=0 configured={}", (Object)this.baselineTotems, (Object)this.totemLostCount.getValue().intValue());
            }
            int n2 = Math.max(0, this.baselineTotems - n);
            Night.LOGGER.info("[AutoShop] TOTEM_LOSS baseline={} current={} lost={} configured={}", new Object[]{this.baselineTotems, n, n2, this.totemLostCount.getValue().intValue()});
        }
        this.previousObservedTotems = n;
    }

    @Override
    public void onEnable() {
        this.resetAutoControls();
        this.clearTotemTracking();
    }

    @Override
    public void onDisable() {
        this.resetAutoControls();
        this.clearTotemTracking();
    }

    private void markAutoFailure() {
        if (this.currentOrder == null || this.currentOrder.origin == OrderOrigin.MANUAL) {
            return;
        }
        if (this.isTotemAutoOrder()) {
            if (!this.totemBelowThreshold.getValue() || this.totemOrderRecovered) {
                return;
            }
            if (this.currentOrder.origin == OrderOrigin.AUTO_PRIMARY) {
                Night.LOGGER.warn("[AutoShop] TOTEM_AUTO_RETRY cooldownSeconds=5");
            } else {
                Night.LOGGER.warn("[AutoShop] TOTEM_AUTO_RETRY_EXHAUSTED");
            }
        }
        AutoControl autoControl = this.autoControls[this.currentOrder.mode.ordinal()];
        if (this.currentOrder.origin == OrderOrigin.AUTO_PRIMARY) {
            autoControl.phase = AutoPhase.RETRY_COOLDOWN;
            autoControl.retryAtNanos = System.nanoTime() + 5000000000L;
            Night.LOGGER.warn("[AutoShop] AUTO_RETRY_SCHEDULED mode={} cooldownSeconds=5", (Object)this.currentOrder.mode);
        } else {
            autoControl.phase = AutoPhase.EXHAUSTED;
            Night.LOGGER.warn("[AutoShop] AUTO_RETRY_EXHAUSTED mode={}", (Object)this.currentOrder.mode);
        }
    }

    private void markAutoServiced() {
        if (this.currentOrder == null || this.currentOrder.origin == OrderOrigin.MANUAL) {
            return;
        }
        if (this.isTotemAutoOrder() && (!this.totemBelowThreshold.getValue() || this.totemOrderRecovered)) {
            return;
        }
        AutoControl autoControl = this.autoControls[this.currentOrder.mode.ordinal()];
        autoControl.phase = AutoPhase.SERVICED;
        int n = this.stockCount(AutoShopModule.itemFor(this.currentOrder.mode));
        autoControl.peakStock = Math.max(autoControl.startStock, n);
        Night.LOGGER.info("[AutoShop] AUTO_SERVICED mode={} observedStock={} startStock={}", new Object[]{this.currentOrder.mode, n, autoControl.startStock});
    }

    private static Item itemFor(ShopMode shopMode) {
        return switch (shopMode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Items.TOTEM_OF_UNDYING;
            case 1 -> Items.END_CRYSTAL;
            case 2 -> Items.EXPERIENCE_BOTTLE;
            case 3 -> Items.OBSIDIAN;
            case 4 -> Items.ENDER_PEARL;
        };
    }

    private int stockCount(Item item) {
        if (AutoShopModule.mc.player == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = AutoShopModule.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() != item) continue;
            n += itemStack.getCount();
        }
        return n;
    }

    private boolean tryStartAutomaticTotemOrder() {
        OrderOrigin orderOrigin;
        boolean bl;
        if (!this.totemBelowThreshold.getValue() || !this.totemTrackingInitialized || this.totemContainerResyncPending || AutoShopModule.mc.player == null || AutoShopModule.mc.level == null || mc.getConnection() == null || !AutoShopModule.mc.player.isAlive() || AutoShopModule.mc.player.isDeadOrDying() || AutoShopModule.mc.player.getHealth() <= 0.0f) {
            return false;
        }
        AutoControl autoControl = this.totemAutoControl();
        if (this.state != State.IDLE || !this.orderQueue.isEmpty()) {
            return false;
        }
        if (autoControl.phase != AutoPhase.READY && autoControl.phase != AutoPhase.RETRY_COOLDOWN) {
            return false;
        }
        int n = this.previousObservedTotems;
        int n2 = n;
        int n3 = this.totemLostCount.getValue().intValue();
        boolean bl2 = n2 <= n3;
        boolean bl3 = bl = this.baselineTotems > 0 && n == 0 && this.totemZeroFailsafePending && !this.totemZeroFailsafeUsed;
        if (autoControl.phase != AutoPhase.RETRY_COOLDOWN && !bl2 && !bl || !this.autoStartSafe(Items.TOTEM_OF_UNDYING)) {
            return false;
        }
        if (autoControl.phase == AutoPhase.RETRY_COOLDOWN && System.nanoTime() - autoControl.retryAtNanos < 0L) {
            return false;
        }
        OrderOrigin orderOrigin2 = orderOrigin = autoControl.phase == AutoPhase.READY ? OrderOrigin.AUTO_PRIMARY : OrderOrigin.AUTO_RETRY;
        if (orderOrigin == OrderOrigin.AUTO_PRIMARY) {
            autoControl.startStock = n;
        }
        autoControl.phase = orderOrigin == OrderOrigin.AUTO_PRIMARY ? AutoPhase.RUNNING_PRIMARY : AutoPhase.RUNNING_RETRY;
        autoControl.peakStock = Math.max(autoControl.peakStock, n);
        this.totemOrderRecovered = false;
        if (orderOrigin == OrderOrigin.AUTO_PRIMARY) {
            String string = this.totemEpisodeTriggerReason = bl2 ? "LOSS" : "ZERO_FAILSAFE";
            if (bl && !bl2) {
                this.totemZeroFailsafePending = false;
                this.totemZeroFailsafeUsed = true;
            }
        }
        int n4 = this.totemAmount.getValue().intValue();
        Night.LOGGER.info("[AutoShop] TOTEM_AUTO_TRIGGER reason={} lost={} configured={} amount={} attempt={}", new Object[]{this.totemEpisodeTriggerReason, n2, n3, n4, orderOrigin});
        this.enqueuePurchaseOrder(new PurchaseOrder(ShopMode.Totem, n4, orderOrigin));
        return true;
    }

    private void tryStartAutomaticOrder() {
        ShopMode[] shopModeArray;
        if (AutoShopModule.mc.player == null || AutoShopModule.mc.level == null || mc.getConnection() == null) {
            return;
        }
        for (ShopMode shopMode : shopModeArray = ShopMode.values()) {
            OrderOrigin orderOrigin;
            if (shopMode == ShopMode.Totem) {
                if (!this.tryStartAutomaticTotemOrder()) continue;
                return;
            }
            int n = shopMode.ordinal();
            AutoControl autoControl = this.autoControls[n];
            boolean bl = this.autoEnabled(shopMode);
            if (!bl) {
                autoControl.wasEnabled = false;
                continue;
            }
            if (!autoControl.wasEnabled) {
                autoControl.reset();
                autoControl.wasEnabled = true;
                Night.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason=TOGGLE_ON", (Object)shopMode);
            }
            int n2 = this.stockCount(AutoShopModule.itemFor(shopMode));
            int n3 = this.thresholdCount(shopMode, AutoShopModule.itemFor(shopMode));
            if (autoControl.phase == AutoPhase.SERVICED || autoControl.phase == AutoPhase.EXHAUSTED) {
                autoControl.peakStock = Math.max(autoControl.peakStock, n2);
                if (n2 > n3 || autoControl.peakStock > autoControl.startStock && n2 < autoControl.peakStock && n2 <= n3) {
                    autoControl.phase = AutoPhase.READY;
                    Night.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason={}", (Object)shopMode, (Object)(n2 > n3 ? "ABOVE_THRESHOLD" : "RECOVERY_THEN_CONSUMPTION"));
                }
            } else if (autoControl.phase == AutoPhase.RETRY_COOLDOWN) {
                if (n2 > autoControl.startStock) {
                    autoControl.phase = AutoPhase.SERVICED;
                    autoControl.peakStock = n2;
                    Night.LOGGER.info("[AutoShop] AUTO_SERVICED mode={} reason=STOCK_INCREASE_DURING_COOLDOWN", (Object)shopMode);
                } else if (n2 > n3) {
                    autoControl.phase = AutoPhase.READY;
                    Night.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason=ABOVE_THRESHOLD", (Object)shopMode);
                }
            }
            if (this.state != State.IDLE || !this.orderQueue.isEmpty() || autoControl.phase != AutoPhase.READY && autoControl.phase != AutoPhase.RETRY_COOLDOWN || n2 > n3 || !this.autoStartSafe(AutoShopModule.itemFor(shopMode)) || autoControl.phase == AutoPhase.RETRY_COOLDOWN && System.nanoTime() - autoControl.retryAtNanos < 0L) continue;
            OrderOrigin orderOrigin2 = orderOrigin = autoControl.phase == AutoPhase.READY ? OrderOrigin.AUTO_PRIMARY : OrderOrigin.AUTO_RETRY;
            if (orderOrigin == OrderOrigin.AUTO_PRIMARY) {
                autoControl.startStock = n2;
            }
            autoControl.phase = orderOrigin == OrderOrigin.AUTO_PRIMARY ? AutoPhase.RUNNING_PRIMARY : AutoPhase.RUNNING_RETRY;
            autoControl.peakStock = Math.max(autoControl.peakStock, n2);
            int n4 = this.manualAmount(shopMode);
            Night.LOGGER.info("[AutoShop] {} mode={} attempt={} stock={} threshold={} amount={}", new Object[]{orderOrigin == OrderOrigin.AUTO_PRIMARY ? "AUTO_TRIGGER" : "AUTO_RETRY", shopMode, orderOrigin == OrderOrigin.AUTO_PRIMARY ? "PRIMARY" : "RETRY", n2, n3, n4});
            this.enqueuePurchaseOrder(new PurchaseOrder(shopMode, n4, orderOrigin));
            return;
        }
    }

    private boolean autoStartSafe(Item item) {
        if (AutoShopModule.mc.gui.screen() != null || AutoShopModule.mc.player.hasContainerOpen()) {
            return false;
        }
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = AutoShopModule.mc.player.getInventory().getItem(i);
            if (!itemStack.isEmpty() && (itemStack.getItem() != item || itemStack.getCount() >= itemStack.getMaxStackSize())) continue;
            return true;
        }
        return false;
    }

    private boolean autoEnabled(ShopMode shopMode) {
        return switch (shopMode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.totemBelowThreshold.getValue();
            case 1 -> this.crystalBelowThreshold.getValue();
            case 2 -> this.expBelowThreshold.getValue();
            case 3 -> this.obsidianBelowThreshold.getValue();
            case 4 -> this.enderPearlBelowThreshold.getValue();
        };
    }

    private int thresholdCount(ShopMode shopMode, Item item) {
        int n = switch (shopMode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> throw new IllegalArgumentException("Totem automatic buying uses loss tracking");
            case 1 -> this.crystalThreshold.getValue().intValue();
            case 2 -> this.expThreshold.getValue().intValue();
            case 3 -> this.obsidianThreshold.getValue().intValue();
            case 4 -> this.enderPearlThreshold.getValue().intValue();
        };
        return shopMode == ShopMode.Totem ? n : n * item.getDefaultInstance().getMaxStackSize();
    }

    private int manualAmount(ShopMode shopMode) {
        return switch (shopMode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.totemAmount.getValue().intValue();
            case 1 -> this.crystalStacks.getValue().intValue();
            case 2 -> this.expStacks.getValue().intValue();
            case 3 -> this.obsidianStacks.getValue().intValue();
            case 4 -> this.enderPearlStacks.getValue().intValue();
        };
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.lastWorld = null;
        this.lastPlayer = null;
        this.timeoutTicks = 0;
        this.ticks = 0;
        this.quantitySet = false;
        ++this.generation;
        this.clearSilentState();
        this.resetAutoControls();
        this.clearTotemTracking();
        this.totemBelowThreshold.setValue(false);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.lastWorld != AutoShopModule.mc.level || this.lastPlayer != AutoShopModule.mc.player) {
            if (this.lastWorld != null && this.state != State.IDLE) {
                this.abort("Player changed world/session");
            }
            this.lastWorld = AutoShopModule.mc.level;
            this.lastPlayer = AutoShopModule.mc.player;
            ++this.generation;
            this.clearSilentState();
            this.resetAutoControls();
            this.clearTotemTracking();
        }
        if (AutoShopModule.mc.player != null && (AutoShopModule.mc.player.isDeadOrDying() || !AutoShopModule.mc.player.isAlive() || AutoShopModule.mc.player.getHealth() <= 0.0f) && this.isTotemAutoOrder()) {
            this.abort("Player dead during automatic Totem order");
            this.clearTotemTracking();
        }
        this.observeTotemStock();
        this.tryStartAutomaticOrder();
        if (this.state == State.IDLE) {
            return;
        }
        if (AutoShopModule.mc.player == null || AutoShopModule.mc.level == null) {
            this.abort("Player left world");
            return;
        }
        this.expireExpectedResponse();
        if (this.ticks < AutoShopSync.delay(this.actionDelay.getValue())) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        Screen screen = AutoShopModule.mc.gui.screen();
        boolean bl = screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen);
        boolean bl2 = bl;
        if (this.silent.getValue() && this.silentSession) {
            boolean bl3 = bl = bl || AutoShopModule.mc.player.hasContainerOpen();
        }
        if (this.state != State.WAIT_SHOP && !bl) {
            this.abort("Shop GUI closed unexpectedly.");
            return;
        }
        block14: do {
            switch (this.state.ordinal()) {
                case 1: {
                    if (bl || this.silent.getValue() && this.realShopScreenIntercepted) {
                        this.realShopScreenIntercepted = false;
                        Night.LOGGER.info("[AutoShop] SHOP_ACCEPTED mode={} generation={}", this.currentOrder == null ? "unknown" : this.currentOrder.mode, (Object)this.generation);
                        this.state = State.CLICK_ITEM;
                        this.timeoutTicks = 0;
                        break;
                    }
                    ++this.timeoutTicks;
                    if (this.timeoutTicks % 20 == 0) {
                        this.sendShopCommand();
                    }
                    if (this.timeoutTicks <= 100) continue block14;
                    this.abort("Shop GUI never opened.");
                    break;
                }
                case 2: {
                    if (this.currentOrder == null) {
                        this.state = State.DECIDE;
                        return;
                    }
                    int slot = switch (this.currentOrder.mode.ordinal()) {
                        case 0 -> 13;
                        case 1 -> 10;
                        case 2 -> 16;
                        case 3 -> 9;
                        case 4 -> 14;
                        default -> throw new MatchException(null, null);
                    };
                    if (this.silent.getValue() && this.silentSession) {
                        this.armExpectedResponse(ResponseReason.ITEM_SELECTION);
                    }
                    Night.LOGGER.info("[AutoShop] ITEM_CLICK mode={} slot={}", (Object)this.currentOrder.mode, (Object)slot);
                    this.click(slot);
                    this.state = State.DECIDE;
                    break;
                }
                case 3: {
                    if (this.fullGate()) {
                        return;
                    }
                    if (this.currentOrder == null || this.currentOrder.remaining <= 0) {
                        if (!this.orderQueue.isEmpty()) {
                            if (this.isTotemAutoOrder()) {
                                this.markAutoServiced();
                                this.totemOrderRecovered = false;
                            }
                            this.currentOrder = this.orderQueue.poll();
                            this.beginOrder();
                            return;
                        }
                        this.finish();
                        return;
                    }
                    if (this.currentOrder.mode == ShopMode.Totem) {
                        this.state = State.CONFIRM;
                        break;
                    }
                    if (!this.quantitySet) {
                        this.state = State.SET_11;
                        break;
                    }
                    this.state = State.CONFIRM;
                    break;
                }
                case 4: {
                    int n;
                    int n3 = n = this.currentOrder != null && this.currentOrder.mode == ShopMode.EnderPearl ? 15 : 16;
                    if (this.silent.getValue() && this.silentSession) {
                        this.armExpectedResponse(ResponseReason.QUANTITY_SELECTION);
                    }
                    Night.LOGGER.info("[AutoShop] QUANTITY_CLICK mode={} slot={}", this.currentOrder == null ? "unknown" : this.currentOrder.mode, (Object)n);
                    this.click(n);
                    this.quantitySet = true;
                    this.state = State.CONFIRM;
                    break;
                }
                case 5: {
                    Night.LOGGER.info("[AutoShop] CONFIRM mode={} remaining={}", this.currentOrder == null ? "unknown" : this.currentOrder.mode, (Object)(this.currentOrder == null ? 0 : this.currentOrder.remaining));
                    this.click(23);
                    if (this.currentOrder != null) {
                        --this.currentOrder.remaining;
                    }
                    this.state = State.DECIDE;
                }
            }
        } while (this.state == State.DECIDE);
    }

    private void finish() {
        this.lastBuyMsg = AutoShopModule.buildBuyMessage(this);
        Night.LOGGER.info("[AutoShop] FINISH mode={}", this.currentOrder == null ? "unknown" : this.currentOrder.mode);
        this.markAutoServiced();
        this.totemOrderRecovered = false;
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        ++this.generation;
        this.clearSilentState();
        if (AutoShopModule.mc.player != null) {
            AutoShopModule.mc.player.closeContainer();
        }
        Night.CHAT_MANAGER.tagged(this.lastBuyMsg, this.getName());
    }

    public void abort(String string) {
        Night.LOGGER.warn("[AutoShop] ABORT reason={} state={}", (Object)string, (Object)this.state);
        this.markAutoFailure();
        this.totemOrderRecovered = false;
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        ++this.generation;
        this.clearSilentState();
        if (AutoShopModule.mc.player != null) {
            AutoShopModule.mc.player.closeContainer();
        }
        Night.CHAT_MANAGER.tagged("Aborted: " + string, this.getName());
    }

    private void click(int n) {
        AutoShopSync.click(n, 0, ContainerInput.PICKUP);
    }

    @Override
    public String getMetaData() {
        if (this.currentOrder != null) {
            return this.currentOrder.mode.name() + " " + this.currentOrder.remaining + (String)(this.orderQueue.isEmpty() ? "" : " (+" + this.orderQueue.size() + ")");
        }
        return "";
    }

    private void totemToggleKey(int n) {
        if (this.totemToggle.getValue() != 0 && n == this.totemToggle.getValue()) {
            this.totemBelowThreshold.setValue(this.totemBelowThreshold.getValue() ^ true);
            if (this.chatNotify.getValue()) {
                if (this.totemBelowThreshold.getValue()) {
                    Night.CHAT_MANAGER.message("\u00a7bAutoShop \u00a77Auto buy totem: \u00a7aON", "autoshop_totem_toggle");
                } else {
                    Night.CHAT_MANAGER.message("\u00a7bAutoShop \u00a77Auto buy totem: \u00a7cOFF", "autoshop_totem_toggle");
                }
            }
        }
    }

    private static /* synthetic */ String buildBuyMessage(AutoShopModule autoShopModule) {
        PurchaseOrder purchaseOrder = autoShopModule.currentOrder;
        if (purchaseOrder != null) {
            return "Buying " + purchaseOrder.mode.name() + "...";
        }
        return "Buying...";
    }

    private boolean canAccept(Item item) {
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = AutoShopModule.mc.player.getInventory().getItem(i);
            if (!itemStack.isEmpty() && (itemStack.getItem() != item || itemStack.getCount() >= itemStack.getMaxStackSize())) continue;
            return true;
        }
        return false;
    }

    private boolean fullGate() {
        PurchaseOrder purchaseOrder = this.currentOrder;
        if (purchaseOrder == null || purchaseOrder.remaining <= 0 || this.canAccept(AutoShopModule.itemFor(purchaseOrder.mode))) {
            return false;
        }
        this.markAutoServiced();
        this.state = State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        ++this.generation;
        this.totemOrderRecovered = false;
        this.clearSilentState();
        if (AutoShopModule.mc.player != null) {
            AutoShopModule.mc.player.closeContainer();
        }
        return true;
    }

    private static enum State {
        IDLE,
        WAIT_SHOP,
        CLICK_ITEM,
        DECIDE,
        SET_11,
        CONFIRM;

    }

    public static class PurchaseOrder {
        public final ShopMode mode;
        public int remaining;
        private final OrderOrigin origin;

        public PurchaseOrder(ShopMode shopMode, int n) {
            this(shopMode, n, OrderOrigin.MANUAL);
        }

        private PurchaseOrder(ShopMode shopMode, int n, OrderOrigin orderOrigin) {
            this.mode = shopMode;
            this.remaining = n;
            this.origin = orderOrigin;
        }
    }

    private static final class AutoControl {
        private AutoPhase phase = AutoPhase.READY;
        private boolean wasEnabled;
        private int startStock;
        private int peakStock;
        private long retryAtNanos;

        private AutoControl() {
        }

        private void reset() {
            this.phase = AutoPhase.READY;
            this.wasEnabled = false;
            this.startStock = 0;
            this.peakStock = 0;
            this.retryAtNanos = 0L;
        }
    }

    private static enum ResponseReason {
        NONE,
        SHOP_COMMAND,
        ITEM_SELECTION,
        QUANTITY_SELECTION;

    }

    public static enum ShopMode {
        Totem,
        Crystal,
        Exp,
        Obsidian,
        EnderPearl;

    }

    private static enum OrderOrigin {
        MANUAL,
        AUTO_PRIMARY,
        AUTO_RETRY;

    }

    public static enum ScreenDecision {
        PASS,
        SUPPRESS_EXPECTED,
        FAIL_OPEN_UNEXPECTED;

    }

    private static enum AutoPhase {
        READY,
        RUNNING_PRIMARY,
        RETRY_COOLDOWN,
        RUNNING_RETRY,
        SERVICED,
        EXHAUSTED;

    }
}

