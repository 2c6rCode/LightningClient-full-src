/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.Items
 */
package lightning.modules.impl.combat;

import java.util.ArrayDeque;
import java.util.Iterator;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.MainhandSlotGuard;
import lightning.modules.impl.combat.SuicideModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

@RegisterModule(name="KingMCTotem", description="Automatically manages totems and golden apples in your mainhand.", category=Module.Category.COMBAT)
public class MainhandModule
extends Module {
    public NumberSetting slot = new NumberSetting("Slot", "The hotbar slot (1-9) to keep a totem in.", 1, 1, 9);
    public NumberSetting delay = new NumberSetting("Delay", "Delay (ms) before refilling a totem to hotbar.", 0, 0, 2000);
    public ModeSetting refillMode = new ModeSetting("RefillMode", "Refill Mode", "When to refill the configured hotbar slot.", "Immediate", new String[]{"Immediate", "After Pops"});
    public NumberSetting popsBeforeRefill = new NumberSetting("PopsBeforeRefill", "Pops Before Refill", "Confirmed main-hand Totem pops per inventory refill.", new ModeSetting.Visibility(this.refillMode, "After Pops"), 4, 1, 9);
    public BooleanSetting useGapple = new BooleanSetting("UseGapple", "Switches to golden apple when right clicking while holding a totem in mainhand.", true);
    public BooleanSetting lethalOverride = new BooleanSetting("LethalOverride", "Allows eating a golden apple regardless of health when no enemies are in visual range.", new BooleanSetting.Visibility(this.useGapple, true), false);
    public NumberSetting health = new NumberSetting("Health", "The health at which a totem will be prioritized.", 16, 0, 36);
    public BooleanSetting elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
    public NumberSetting fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", Float.valueOf(20.0f), Float.valueOf(0.0f), Float.valueOf(80.0f));
    public BooleanSetting antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
    public NumberSetting maceRange = new NumberSetting("MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", new BooleanSetting.Visibility(this.antiMace, true), (Number)Float.valueOf(12.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(24.0f));
    private long pendingRefillTime = 0L;
    private static final int POP_WINDOW_UPDATES = 8;
    private static final int MAX_POP_CANDIDATES = 32;
    private final ArrayDeque<PopCandidate> popCandidates = new ArrayDeque();
    private final boolean[] observedTotems = new boolean[9];
    private int updateSequence;
    private int confirmedPopDebt;
    private int refillSource = -1;
    private int refillTarget = -1;
    private int refillAttemptUpdate;
    private String observedMode;
    private Object observedPlayer;
    private Object observedWorld;
    private String pendingReason;
    private boolean eatingHotbar = false;
    private int previousHotbarSlot = -1;
    private boolean eatingInventory = false;
    private int swappedInvSlot = -1;
    private int swappedHotbarSlot = -1;
    public static volatile boolean hidingLegitInventory = false;
    public BooleanSetting lockSlot = new BooleanSetting("TotemOnlySlot", "Keeps hotbar slots LockFrom to LockTo reserved for totems only. Any other item that lands there is moved to the inventory or dropped.", true);
    public NumberSetting lockFrom = new NumberSetting("LockFrom", "Lock From", "First hotbar slot (1-9) reserved for totems.", new BooleanSetting.Visibility(this.lockSlot, true), 4, 1, 9);
    public NumberSetting lockTo = new NumberSetting("LockTo", "Lock To", "Last hotbar slot (1-9) reserved for totems.", new BooleanSetting.Visibility(this.lockSlot, true), 7, 1, 9);
    public ModeSetting lockAction = new ModeSetting("LockAction", "Lock Action", "What to do with a non-totem item found in the locked Slot.", new BooleanSetting.Visibility(this.lockSlot, true), "Inventory", new String[]{"Inventory", "Drop"});

    @Override
    public void onEnable() {
        MainhandSlotGuard.onModuleEnable();
        this.pendingRefillTime = 0L;
        this.resetAfterPops("enable");
        this.observedMode = this.refillMode.getValue();
    }

    private boolean afterPops() {
        return "After Pops".equals(this.refillMode.getValue());
    }

    private void resetAfterPops(String string) {
        if (!this.popCandidates.isEmpty() || this.confirmedPopDebt != 0 || this.pendingRefillTime != 0L || this.refillSource != -1) {
            Night.LOGGER.info("[Mainhand] RESET reason={}", (Object)string);
        }
        this.popCandidates.clear();
        this.confirmedPopDebt = 0;
        this.pendingRefillTime = 0L;
        this.refillSource = -1;
        this.refillTarget = -1;
        this.pendingReason = null;
        this.updateSequence = 0;
        for (int i = 0; i < this.observedTotems.length; ++i) {
            this.observedTotems[i] = false;
        }
    }

    private void observeModeAndSession() {
        String string = this.refillMode.getValue();
        if (this.observedMode == null || !this.observedMode.equals(string)) {
            this.resetAfterPops("mode_change");
            this.observedMode = string;
            Night.LOGGER.info("[Mainhand] MODE mode={}", (Object)string);
        }
        if (MainhandModule.mc.player == null || MainhandModule.mc.level == null || MainhandModule.mc.player.isDeadOrDying()) {
            this.resetAfterPops("invalid_session");
            this.observedPlayer = MainhandModule.mc.player;
            this.observedWorld = MainhandModule.mc.level;
            return;
        }
        if (this.observedPlayer != MainhandModule.mc.player || this.observedWorld != MainhandModule.mc.level) {
            this.resetAfterPops("session_change");
            this.observedPlayer = MainhandModule.mc.player;
            this.observedWorld = MainhandModule.mc.level;
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.resetAfterPops("disconnect");
        this.observedPlayer = null;
        this.observedWorld = null;
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onPlayerPop(PlayerPopEvent playerPopEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (playerPopEvent.getPlayer() != MainhandModule.mc.player || Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            return;
        }
        if (!this.isToggled()) {
            return;
        }
        mc.execute(() -> {
            if (MainhandModule.mc.player == null || MainhandModule.mc.level == null) {
                return;
            }
            this.observeModeAndSession();
            if (this.afterPops()) {
                if (MainhandModule.mc.player.isDeadOrDying()) {
                    return;
                }
                int n = MainhandModule.mc.player.getInventory().getSelectedSlot();
                if (n < 0 || n > 8 || !MainhandModule.mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || !MainhandModule.mc.player.getInventory().getItem(n).is(Items.TOTEM_OF_UNDYING)) {
                    Night.LOGGER.info("[Mainhand] POP_REJECTED reason=offhand_or_no_mainhand");
                    return;
                }
                if (this.popCandidates.size() == 32) {
                    this.popCandidates.removeFirst();
                }
                this.popCandidates.addLast(new PopCandidate(n, this.updateSequence));
                this.observedTotems[n] = true;
                Night.LOGGER.info("[Mainhand] POP_CANDIDATE slot={} age=0", (Object)(n + 1));
                return;
            }
            int n = this.slot.getValue().intValue() - 1;
            long l = this.delay.getValue().longValue();
            if (l <= 0L) {
                this.refill(n);
            } else {
                this.pendingRefillTime = System.currentTimeMillis() + l;
            }
        });
    }

    private void refill(int n) {
        if (MainhandModule.mc.player == null || MainhandModule.mc.level == null) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        Screen screen = MainhandModule.mc.gui.screen();
        if (screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen) && !MainhandModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        if (MainhandModule.mc.player.getInventory().getItem(n).getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }
        int n2 = this.findRefillTotem(n);
        if (n2 != -1) {
            InventoryUtils.swap("Swap", n2, n);
        }
    }

    public boolean legitOwnsTotems() {
        return this.isToggled();
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        this.observeModeAndSession();
        if (this.afterPops() && MainhandModule.mc.player != null && MainhandModule.mc.level != null && !MainhandModule.mc.player.isDeadOrDying()) {
            this.observeAfterPops();
        }
        this.updateMainhand();
        MainhandSlotGuard.tick(this, this.lockSlot, this.lockAction, this.lockFrom, this.lockTo);
    }

    private void observeAfterPops() {
        ++this.updateSequence;
        boolean[] blArray = new boolean[9];
        for (int i = 0; i < blArray.length; ++i) {
            blArray[i] = MainhandModule.mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING);
        }
        Iterator<PopCandidate> iterator = this.popCandidates.iterator();
        while (iterator.hasNext()) {
            PopCandidate popCandidate = iterator.next();
            if (this.updateSequence - popCandidate.createdUpdate > 8) {
                iterator.remove();
                Night.LOGGER.info("[Mainhand] POP_REJECTED reason=timeout slot={}", (Object)(popCandidate.slot + 1));
                continue;
            }
            if (!this.observedTotems[popCandidate.slot] || blArray[popCandidate.slot]) continue;
            iterator.remove();
            ++this.confirmedPopDebt;
            Night.LOGGER.info("[Mainhand] POP_CONFIRMED slot={} debt={} threshold={}", new Object[]{popCandidate.slot + 1, this.confirmedPopDebt, this.popsBeforeRefill.getValue().intValue()});
            this.observedTotems[popCandidate.slot] = false;
        }
        System.arraycopy(blArray, 0, this.observedTotems, 0, blArray.length);
    }

    private void logPending(String string) {
        if (!string.equals(this.pendingReason)) {
            Night.LOGGER.info("[Mainhand] REFILL_PENDING reason={}", (Object)string);
            this.pendingReason = string;
        }
    }

    private void updateAfterPopsRefill(int n, long l) {
        int n2 = this.popsBeforeRefill.getValue().intValue();
        if (this.refillSource != -1) {
            boolean bl;
            boolean bl2 = MainhandModule.mc.player.getInventory().getItem(this.refillTarget).is(Items.TOTEM_OF_UNDYING);
            boolean bl3 = bl = !MainhandModule.mc.player.getInventory().getItem(this.refillSource).is(Items.TOTEM_OF_UNDYING);
            if (bl2 && bl) {
                this.confirmedPopDebt = Math.max(0, this.confirmedPopDebt - n2);
                Night.LOGGER.info("[Mainhand] REFILL_SUCCESS sourceSlot={} targetSlot={} remainingDebt={}", new Object[]{this.refillSource, this.refillTarget + 1, this.confirmedPopDebt});
                this.refillSource = -1;
                this.refillTarget = -1;
                this.pendingRefillTime = 0L;
                this.pendingReason = null;
                return;
            }
            if (this.updateSequence - this.refillAttemptUpdate <= 8) {
                return;
            }
            this.refillSource = -1;
            this.refillTarget = -1;
            this.pendingRefillTime = 0L;
            this.logPending("swap_not_observed");
            return;
        }
        if (this.confirmedPopDebt < n2) {
            this.pendingRefillTime = 0L;
            this.pendingReason = null;
            return;
        }
        if (MainhandModule.mc.player.getInventory().getItem(n).is(Items.TOTEM_OF_UNDYING)) {
            this.pendingRefillTime = 0L;
            this.logPending("target_still_totem");
            return;
        }
        if (this.pendingRefillTime == 0L) {
            long l2 = this.delay.getValue().longValue();
            this.pendingRefillTime = l + Math.max(1L, l2);
            Night.LOGGER.info("[Mainhand] THRESHOLD_REACHED debt={} threshold={} targetSlot={}", new Object[]{this.confirmedPopDebt, n2, n + 1});
            if (l2 > 0L) {
                Night.LOGGER.info("[Mainhand] REFILL_DELAY ms={}", (Object)l2);
            }
        }
        if (l < this.pendingRefillTime || PingBypassFlags.isPingBypassActive()) {
            return;
        }
        int n3 = InventoryUtils.find(Items.TOTEM_OF_UNDYING, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
        if (n3 == -1) {
            this.logPending("no_inventory_totem");
            return;
        }
        this.pendingReason = null;
        this.refillSource = n3;
        this.refillTarget = n;
        this.refillAttemptUpdate = this.updateSequence;
        InventoryUtils.swap("Swap", n3, n);
    }

    private void updateMainhand() {
        int n;
        int n2;
        int n3;
        if (MainhandModule.mc.player == null || MainhandModule.mc.level == null) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        Screen screen = MainhandModule.mc.gui.screen();
        boolean bl = screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen);
        boolean bl2 = bl;
        if (bl && !MainhandModule.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        boolean bl3 = MainhandModule.mc.options.keyUse.isDown() || MainhandModule.mc.player.isUsingItem();
        boolean bl4 = bl3;
        if (this.eatingHotbar) {
            if (!bl3) {
                if (this.previousHotbarSlot != -1) {
                    MainhandModule.mc.player.getInventory().setSelectedSlot(this.previousHotbarSlot);
                    ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
                }
                this.eatingHotbar = false;
                this.previousHotbarSlot = -1;
            }
            return;
        }
        if (this.eatingInventory) {
            if (!bl3) {
                if (this.swappedInvSlot != -1 && this.swappedHotbarSlot != -1) {
                    InventoryUtils.swap("Swap", this.swappedInvSlot, this.swappedHotbarSlot);
                }
                this.eatingInventory = false;
                this.swappedInvSlot = -1;
                this.swappedHotbarSlot = -1;
            }
            return;
        }
        boolean bl5 = !this.needsTotem() || this.lethalOverride.getValue() && !this.hasEnemyInVisualRange();
        boolean bl6 = bl5;
        if (this.useGapple.getValue() && MainhandModule.mc.options.keyUse.isDown() && MainhandModule.mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && bl5) {
            n3 = this.findGappleHotbar();
            if (n3 != -1) {
                this.previousHotbarSlot = MainhandModule.mc.player.getInventory().getSelectedSlot();
                MainhandModule.mc.player.getInventory().setSelectedSlot(n3);
                ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
                this.eatingHotbar = true;
                return;
            }
            int n4 = this.findGappleInventory();
            if (n4 != -1) {
                this.swappedInvSlot = n4;
                this.swappedHotbarSlot = MainhandModule.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.swap("Swap", n4, this.swappedHotbarSlot);
                this.eatingInventory = true;
                return;
            }
        }
        n3 = this.slot.getValue().intValue() - 1;
        long l = System.currentTimeMillis();
        if (this.afterPops()) {
            this.updateAfterPopsRefill(n3, l);
        } else if (this.pendingRefillTime > 0L) {
            if (l >= this.pendingRefillTime) {
                this.pendingRefillTime = 0L;
                this.refill(n3);
            }
        } else {
            n2 = MainhandModule.mc.player.getInventory().getItem(n3).getItem() != Items.TOTEM_OF_UNDYING ? 1 : 0;
            n = n2;
            if (n2 != 0) {
                long l2 = this.delay.getValue().longValue();
                if (l2 <= 0L) {
                    this.refill(n3);
                } else {
                    this.pendingRefillTime = l + l2;
                }
            }
        }
        if (this.needsTotem()) {
            n = MainhandModule.mc.player.getInventory().getSelectedSlot();
            n2 = MainhandModule.mc.player.getInventory().getItem(n3).getItem() == Items.TOTEM_OF_UNDYING ? 1 : 0;
            int n5 = n2;
            if (n != n3 || n2 == 0) {
                if (n2 != 0) {
                    MainhandModule.mc.player.getInventory().setSelectedSlot(n3);
                    ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
                } else {
                    int n6 = InventoryUtils.find(Items.TOTEM_OF_UNDYING, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
                    if (n6 != -1 && n != n6) {
                        MainhandModule.mc.player.getInventory().setSelectedSlot(n6);
                        ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
                    }
                }
            }
        }
    }

    private int findRefillTotem(int n) {
        int n2 = InventoryUtils.findInventory(Items.TOTEM_OF_UNDYING);
        if (n2 != -1) {
            return n2;
        }
        for (int i = InventoryUtils.HOTBAR_START; i <= InventoryUtils.HOTBAR_END; ++i) {
            if (i == n || MainhandModule.mc.player.getInventory().getItem(i).getItem() != Items.TOTEM_OF_UNDYING) continue;
            return i;
        }
        return -1;
    }

    private int findGappleHotbar() {
        int n = InventoryUtils.find(Items.ENCHANTED_GOLDEN_APPLE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
        if (n != -1) {
            return n;
        }
        return InventoryUtils.find(Items.GOLDEN_APPLE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
    }

    private int findGappleInventory() {
        int n = InventoryUtils.find(Items.ENCHANTED_GOLDEN_APPLE, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
        if (n != -1) {
            return n;
        }
        return InventoryUtils.find(Items.GOLDEN_APPLE, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
    }

    public boolean shouldLockSlot() {
        if (!this.isToggled()) {
            return false;
        }
        if (MainhandModule.mc.player == null) {
            return false;
        }
        if (this.eatingHotbar || this.eatingInventory) {
            return false;
        }
        if (InventoryUtils.find(Items.TOTEM_OF_UNDYING) == -1) {
            return false;
        }
        return this.needsTotem();
    }

    private boolean needsTotem() {
        if (Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && Night.MODULE_MANAGER.getModule(SuicideModule.class).offhandOverride.getValue()) {
            return false;
        }
        if (MainhandModule.mc.player.getHealth() + MainhandModule.mc.player.getAbsorptionAmount() <= this.health.getValue().floatValue()) {
            return true;
        }
        if (MainhandModule.mc.player.fallDistance > (double)this.fallDistance.getValue().floatValue()) {
            return true;
        }
        if (this.elytraCheck.getValue() && MainhandModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            return true;
        }
        return this.antiMace.getValue() && MainhandModule.mc.level.players().stream().anyMatch(abstractClientPlayer -> abstractClientPlayer != MainhandModule.mc.player && !Night.FRIEND_MANAGER.contains(abstractClientPlayer.getName().getString()) && MainhandModule.mc.player.distanceToSqr((Entity)abstractClientPlayer) <= (double)Mth.square((float)this.maceRange.getValue().floatValue()) && abstractClientPlayer.fallDistance >= 1.5 && abstractClientPlayer.getMainHandItem().getItem().equals(Items.MACE));
    }

    private boolean hasEnemyInVisualRange() {
        if (MainhandModule.mc.level == null || MainhandModule.mc.player == null) {
            return false;
        }
        FakePlayerModule fakePlayerModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FakePlayerModule.class) : null;
        return MainhandModule.mc.level.players().stream().anyMatch(abstractClientPlayer -> abstractClientPlayer != MainhandModule.mc.player && !abstractClientPlayer.isDeadOrDying() && !EntityUtils.isGhost((Entity)abstractClientPlayer) && (fakePlayerModule == null || !fakePlayerModule.isToggled() || abstractClientPlayer != fakePlayerModule.getPlayer()) && !Night.FRIEND_MANAGER.contains(abstractClientPlayer.getName().getString()));
    }

    @Override
    public void onDisable() {
        MainhandSlotGuard.onModuleDisable();
        if (this.eatingHotbar && MainhandModule.mc.player != null && this.previousHotbarSlot != -1) {
            MainhandModule.mc.player.getInventory().setSelectedSlot(this.previousHotbarSlot);
            ((ClientPlayerInteractionManagerAccessor)MainhandModule.mc.gameMode).invokeSyncSelectedSlot();
        }
        if (this.eatingInventory && MainhandModule.mc.player != null && this.swappedInvSlot != -1 && this.swappedHotbarSlot != -1) {
            InventoryUtils.swap("Swap", this.swappedInvSlot, this.swappedHotbarSlot);
        }
        this.eatingHotbar = false;
        this.previousHotbarSlot = -1;
        this.eatingInventory = false;
        this.swappedInvSlot = -1;
        this.swappedHotbarSlot = -1;
        this.pendingRefillTime = 0L;
        this.resetAfterPops("disable");
        this.observedMode = null;
        this.observedPlayer = null;
        this.observedWorld = null;
    }

    @Override
    public String getMetaData() {
        return "Slot " + this.slot.getValue().intValue();
    }

    private static final class PopCandidate {
        private final int slot;
        private final int createdUpdate;

        private PopCandidate(int n, int n2) {
            this.slot = n;
            this.createdUpdate = n2;
        }
    }
}

