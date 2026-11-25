/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package lightning.modules.impl.player;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.SprintModule;
import lightning.modules.impl.player.MultiTaskModule;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.input.KeyboardUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@RegisterModule(name="KeyAction", description="Keybinds for throwing fireworks, pearls and experience bottles.", category=Module.Category.PLAYER, persistent=true, proxyEnhanced=true)
public class KeyActionModule
extends Module {
    public CategorySetting fireworkCategory = new CategorySetting("FireWork", "Keybind and settings for throwing a firework.");
    public BindSetting fireworkBind = new BindSetting("FireWorkBind", "Bind", "The key that throws a firework. Hold/ReverseHold repeat it every tick the bind is (or isn't) held.", new CategorySetting.Visibility(this.fireworkCategory), 0);
    public ModeSetting fireworkSwitch = new ModeSetting("FireWorkSwitch", "Switch", "The mode that will be used for automatically switching to fireworks.", new CategorySetting.Visibility(this.fireworkCategory), "Silent", InventoryUtils.SWITCH_MODES);
    public CategorySetting pearlsCategory = new CategorySetting("Pearls", "Keybind and settings for throwing an ender pearl.");
    public BindSetting pearlsBind = new BindSetting("PearlsBind", "Bind", "The key that throws an ender pearl. Hold/ReverseHold repeat it every tick the bind is (or isn't) held.", new CategorySetting.Visibility(this.pearlsCategory), 0);
    public ModeSetting pearlsSwitch = new ModeSetting("PearlsSwitch", "Switch", "The mode that will be used for automatically switching to pearls.", new CategorySetting.Visibility(this.pearlsCategory), "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting pearlsRotate = new BooleanSetting("PearlsRotate", "Rotate", "Sends a packet rotation right before throwing the pearl.", new CategorySetting.Visibility(this.pearlsCategory), true);
    public CategorySetting xpCategory = new CategorySetting("XP", "Keybind and settings for throwing experience bottles.");
    public BindSetting xpBind = new BindSetting("XPBind", "Bind", "The key that starts/stops throwing experience bottles. Hold/ReverseHold keep the loop matched to whether the bind is (or isn't) held instead of toggling it.", new CategorySetting.Visibility(this.xpCategory), 0);
    public ModeSetting xpSwitch = new ModeSetting("XPSwitch", "Switch", "The mode that will be used for automatically switching to experience bottles.", new CategorySetting.Visibility(this.xpCategory), "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting xpRotate = new BooleanSetting("XPRotate", "Rotate", "Rotates down to your feet when throwing experience bottles.", new CategorySetting.Visibility(this.xpCategory), true);
    public NumberSetting xpDelay = new NumberSetting("XPDelay", "Delay", "The delay in ticks between throwing experience bottles.", new CategorySetting.Visibility(this.xpCategory), 1, 0, 20);
    public NumberSetting xpRepeat = new NumberSetting("XPRepeat", "Repeat", "Allows you to throw a lot more XP bottles at once.", new CategorySetting.Visibility(this.xpCategory), 1, 1, 15);
    public ModeSetting xpAntiWaste;
    public BooleanSetting xpItemDisable;
    private boolean xpActive = false;
    private int xpTicks = 0;
    private long lastPearlTime = 0L;
    public BooleanSetting xpFullRepair = new BooleanSetting("XPFullRepair", "FullRepair", "One press repairs all armor to 100%: throws as many bottles as needed per burst and stops automatically when armor is full.", new CategorySetting.Visibility(this.xpCategory), true);

    public KeyActionModule() {
        this.xpAntiWaste = new ModeSetting("XPAntiWaste", "AntiWaste", "How wasting of experience bottles should be prevented.", new CategorySetting.Visibility(this.xpCategory), "Avoid", new String[]{"None", "Avoid", "Disable"});
        this.xpItemDisable = new BooleanSetting("XPItemDisable", "ItemDisable", "Automatically stops the XP loop when you run out of XP.", new CategorySetting.Visibility(this.xpCategory), true);
    }

    public boolean isXpActive() {
        return this.xpActive;
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent keyInputEvent) {
        if (this.shouldRunOnProxy() || this.getNull() || KeyActionModule.mc.gui != null && KeyActionModule.mc.gui.screen() != null) {
            return;
        }
        if (this.fireworkBind.getValue() != 0 && keyInputEvent.getKey() == this.fireworkBind.getValue() && this.fireworkBind.getMode().equals("Bind")) {
            this.throwFirework();
        }
        if (this.pearlsBind.getValue() != 0 && keyInputEvent.getKey() == this.pearlsBind.getValue() && this.pearlsBind.getMode().equals("Bind")) {
            this.throwPearl();
        }
        if (this.xpBind.getValue() != 0 && keyInputEvent.getKey() == this.xpBind.getValue() && this.xpBind.getMode().equals("Bind")) {
            this.xpActive = !this.xpActive;
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent mouseInputEvent) {
        if (this.shouldRunOnProxy() || this.getNull() || KeyActionModule.mc.gui != null && KeyActionModule.mc.gui.screen() != null) {
            return;
        }
        int mouseBind = -mouseInputEvent.getButton() - 1;
        if (this.fireworkBind.getValue() != 0 && mouseBind == this.fireworkBind.getValue() && this.fireworkBind.getMode().equals("Bind")) {
            this.throwFirework();
        }
        if (this.pearlsBind.getValue() != 0 && mouseBind == this.pearlsBind.getValue() && this.pearlsBind.getMode().equals("Bind")) {
            this.throwPearl();
        }
        if (this.xpBind.getValue() != 0 && mouseBind == this.xpBind.getValue() && this.xpBind.getMode().equals("Bind")) {
            this.xpActive = !this.xpActive;
        }
    }

    private void throwFirework() {
        if (KeyActionModule.mc.player == null || KeyActionModule.mc.level == null) {
            return;
        }
        if (this.fireworkSwitch.getValue().equalsIgnoreCase("None") && KeyActionModule.mc.player.getMainHandItem().getItem() != Items.FIREWORK_ROCKET) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any fireworks.", this.getName());
            return;
        }
        if (KeyActionModule.mc.player.getCooldowns().isOnCooldown(new ItemStack((ItemLike)Items.FIREWORK_ROCKET))) {
            return;
        }
        int fireworkSlot = InventoryUtils.find(Items.FIREWORK_ROCKET, 0, this.fireworkSwitch.getValue().equalsIgnoreCase("AltSwap") || this.fireworkSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = KeyActionModule.mc.player.getInventory().getSelectedSlot();
        if (fireworkSlot == -1) {
            Night.CHAT_MANAGER.tagged("No fireworks could be found in your hotbar.", this.getName());
            return;
        }
        InventoryUtils.switchSlot(this.fireworkSwitch.getValue(), fireworkSlot, previousSlot);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, KeyActionModule.mc.player.getYRot(), KeyActionModule.mc.player.getXRot()));
        InventoryUtils.switchBack(this.fireworkSwitch.getValue(), fireworkSlot, previousSlot);
    }

    public boolean isPearlActive() {
        return System.currentTimeMillis() - this.lastPearlTime < 100L;
    }

    private void throwPearl() {
        if (KeyActionModule.mc.player == null || KeyActionModule.mc.level == null) {
            return;
        }
        if (this.pearlsSwitch.getValue().equalsIgnoreCase("None") && KeyActionModule.mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any pearls.", this.getName());
            return;
        }
        if (KeyActionModule.mc.player.getCooldowns().isOnCooldown(new ItemStack((ItemLike)Items.ENDER_PEARL))) {
            return;
        }
        this.lastPearlTime = System.currentTimeMillis();
        MultiTaskModule multiTaskModule = Night.MODULE_MANAGER.getModule(MultiTaskModule.class);
        boolean useAltSwap = multiTaskModule != null && multiTaskModule.isToggled() && multiTaskModule.pearl.getValue() && KeyActionModule.mc.player.isUsingItem();
        String switchMode = useAltSwap ? "AltSwap" : this.pearlsSwitch.getValue();
        int pearlSlot = InventoryUtils.find(Items.ENDER_PEARL, 0, switchMode.equalsIgnoreCase("AltSwap") || switchMode.equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = KeyActionModule.mc.player.getInventory().getSelectedSlot();
        if (pearlSlot == -1) {
            Night.CHAT_MANAGER.tagged("No pearls could be found in your hotbar.", this.getName());
            return;
        }
        if (this.pearlsRotate.getValue()) {
            Night.ROTATION_MANAGER.packetRotate(KeyActionModule.mc.player.getYRot(), KeyActionModule.mc.player.getXRot());
        }
        InventoryUtils.switchSlot(switchMode, pearlSlot, previousSlot);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, KeyActionModule.mc.player.getYRot(), KeyActionModule.mc.player.getXRot()), this::serverSend);
        this.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(switchMode, pearlSlot, previousSlot);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy() || this.getNull() || KeyActionModule.mc.gui != null && KeyActionModule.mc.gui.screen() != null) {
            return;
        }
        this.checkHoldBind(this.fireworkBind, this::throwFirework);
        this.checkHoldBind(this.pearlsBind, this::throwPearl);
        if (this.xpBind.getValue() != 0 && !this.xpBind.getMode().equals("Bind")) {
            boolean bindDown = KeyboardUtils.isBindDown(this.xpBind.getValue());
            this.xpActive = this.xpBind.getMode().equals("Hold") == bindDown;
        }
        this.tickXp();
    }

    private void checkHoldBind(BindSetting bindSetting, Runnable runnable) {
        if (bindSetting.getValue() == 0 || bindSetting.getMode().equals("Bind")) {
            return;
        }
        boolean bindDown = KeyboardUtils.isBindDown(bindSetting.getValue());
        if (bindSetting.getMode().equals("Hold") == bindDown) {
            runnable.run();
        }
    }

    private void tickXp() {
        if (!this.xpActive) {
            return;
        }
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (KeyActionModule.mc.player == null || KeyActionModule.mc.level == null) {
            return;
        }
        if (this.xpSwitch.getValue().equalsIgnoreCase("None") && !(KeyActionModule.mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            Night.CHAT_MANAGER.tagged("You are currently not holding any experience bottles.", this.getName());
            this.xpActive = false;
            return;
        }
        if (this.xpTicks < this.xpDelayInt()) {
            ++this.xpTicks;
            return;
        }
        if (!this.needsExperience() && !this.xpAwValue().equals("None")) {
            if (this.xpAwValue().equals("Disable")) {
                this.xpActive = false;
            }
            return;
        }
        int xpSlot = InventoryUtils.find(Items.EXPERIENCE_BOTTLE, 0, this.xpSwitch.getValue().equalsIgnoreCase("AltSwap") || this.xpSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = KeyActionModule.mc.player.getInventory().getSelectedSlot();
        if (xpSlot == -1) {
            Night.CHAT_MANAGER.tagged("No experience bottles could be found in your hotbar.", this.getName());
            this.xpActive = false;
            return;
        }
        SprintModule sprintModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SprintModule.class) : null;
        float yaw = sprintModule != null && sprintModule.isToggled() && sprintModule.isGrimCompensating() && sprintModule.getPendingYaw() != null ? sprintModule.getPendingYaw().floatValue() : (Night.ROTATION_MANAGER != null && Night.ROTATION_MANAGER.getRotation() != null ? Night.ROTATION_MANAGER.getRotation().getYaw() : KeyActionModule.mc.player.getYRot());
        float pitch = this.xpRotate.getValue() ? 90.0f : KeyActionModule.mc.player.getXRot();
        if (this.xpRotate.getValue() && Night.ROTATION_MANAGER != null) {
            Night.ROTATION_MANAGER.silentRotate(yaw, pitch);
        }
        InventoryUtils.switchSlot(this.xpSwitch.getValue(), xpSlot, previousSlot);
        for (int i = 0; i < this.xpBurst(); ++i) {
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch), this::serverSend);
        }
        this.serverSend((Packet<?>)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(this.xpSwitch.getValue(), xpSlot, previousSlot);
        this.xpTicks = 0;
    }

    private void serverSend(Packet<?> packet) {
        Connection connection;
        if (this.isRunningOnProxy() && Night.PROXY_SERVER != null && (connection = Night.PROXY_SERVER.getServerConnection()) != null && connection.isConnected()) {
            ProxyServerTickListener.allowSend(() -> connection.send(packet));
            return;
        }
        mc.getConnection().send(packet);
    }

    private boolean needsExperience() {
        for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack itemStack = KeyActionModule.mc.player.getItemBySlot(equipmentSlot);
            if (itemStack.isEmpty() || !itemStack.is(ItemTags.FOOT_ARMOR) && !itemStack.is(ItemTags.LEG_ARMOR) && !itemStack.is(ItemTags.CHEST_ARMOR) && !itemStack.is(ItemTags.HEAD_ARMOR) || !((float)Math.round((float)(itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f / (float)itemStack.getMaxDamage()) < 100.0f)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        this.xpActive = false;
        this.xpTicks = 0;
    }

    private int xpDelayInt() {
        int delayTicks = this.xpDelay.getValue().intValue();
        if (this.xpFullRepair.getValue()) {
            delayTicks = Math.max(delayTicks, 5);
        }
        return delayTicks;
    }

    private String xpAwValue() {
        if (this.xpFullRepair.getValue()) {
            return "Disable";
        }
        return this.xpAntiWaste.getValue();
    }

    private int xpMissing() {
        return KeyActionModule.mc.player.getItemBySlot(EquipmentSlot.FEET).getDamageValue() + KeyActionModule.mc.player.getItemBySlot(EquipmentSlot.LEGS).getDamageValue() + KeyActionModule.mc.player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue() + KeyActionModule.mc.player.getItemBySlot(EquipmentSlot.HEAD).getDamageValue();
    }

    private int xpBurst() {
        if (!this.xpFullRepair.getValue()) {
            return this.xpRepeat.getValue().intValue();
        }
        return Math.min(Math.max((this.xpMissing() + 15) / 16, 1), 32);
    }
}

