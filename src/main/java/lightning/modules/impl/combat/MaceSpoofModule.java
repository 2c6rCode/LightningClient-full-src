/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.PlayerMoveC2SPacketAccessor
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.item.Items
 */
package lightning.modules.impl.combat;

import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;

@RegisterModule(name="MaceSpoof", description="Spoof fall height for mace damage.", category=Module.Category.COMBAT)
public class MaceSpoofModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Mode of spoofing.", "Vanilla", new String[]{"Vanilla", "NCP", "Swap"});
    public BooleanSetting noCrystal = new BooleanSetting("NoCrystal", "Don't apply on crystal attack.", new ModeSetting.Visibility(this.mode, "Swap"), true);
    public BooleanSetting inventorySwap = new BooleanSetting("InventorySwap", "Use inventory swap (vs hotbar swap).", new ModeSetting.Visibility(this.mode, "Swap"), false);
    public BooleanSetting onlyGround = new BooleanSetting("OnlyGround", "Only spoof when on ground or flying.", new ModeSetting.Visibility(this.mode, "Vanilla"), true);
    public NumberSetting height = new NumberSetting("Height", "Fake fall height.", new ModeSetting.Visibility(this.mode, "Vanilla"), (Number)25.0, (Number)1.0, (Number)2000.0);
    private boolean attacking = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent attackEntityEvent) {
        if (this.attacking) {
            return;
        }
        if (MaceSpoofModule.mc.player == null || MaceSpoofModule.mc.level == null || mc.getConnection() == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Vanilla")) {
            if (!MaceSpoofModule.mc.player.getMainHandItem().is(Items.MACE)) {
                return;
            }
            Entity entity = attackEntityEvent.getTarget();
            if (entity == null || entity instanceof EndCrystal) {
                return;
            }
            if (this.onlyGround.getValue() && !MaceSpoofModule.mc.player.onGround() && !MaceSpoofModule.mc.player.getAbilities().flying) {
                return;
            }
            if (MaceSpoofModule.mc.player.isInLava() || MaceSpoofModule.mc.player.isUnderWater() || MaceSpoofModule.mc.player.isInWater()) {
                return;
            }
            for (int i = 0; i < 4; ++i) {
                this.sendFakeY(0.0);
            }
            this.sendFakeY(this.height.getValue().doubleValue());
            this.sendFakeY(0.0);
        } else if (this.mode.getValue().equalsIgnoreCase("Swap")) {
            Entity entity = attackEntityEvent.getTarget();
            if (this.noCrystal.getValue() && entity instanceof EndCrystal) {
                return;
            }
            int maceSlot = this.getMaceSlot();
            if (maceSlot == -1) {
                return;
            }
            try {
                this.attacking = true;
                this.doSpoof(entity, maceSlot);
            }
            finally {
                this.attacking = false;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        Packet<?> packet;
        if (MaceSpoofModule.mc.player == null || MaceSpoofModule.mc.level == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("NCP") && (packet = packetSendEvent.getPacket()) instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket serverboundMovePlayerPacket = (ServerboundMovePlayerPacket)packet;
            ((PlayerMoveC2SPacketAccessor)serverboundMovePlayerPacket).setOnGround(false);
        }
    }

    private void doSpoof(Entity entity, int maceSlot) {
        int oldSlot = MaceSpoofModule.mc.player.getInventory().getSelectedSlot();
        if (this.inventorySwap.getValue()) {
            InventoryUtils.click(maceSlot, oldSlot, ContainerInput.SWAP);
            MaceSpoofModule.mc.gameMode.attack((Player)MaceSpoofModule.mc.player, entity);
            MaceSpoofModule.mc.player.resetAttackStrengthTicker();
            InventoryUtils.click(maceSlot, oldSlot, ContainerInput.SWAP);
        } else {
            InventoryUtils.switchSlot("Silent", maceSlot, oldSlot);
            MaceSpoofModule.mc.gameMode.attack((Player)MaceSpoofModule.mc.player, entity);
            MaceSpoofModule.mc.player.resetAttackStrengthTicker();
            InventoryUtils.switchBack("Silent", maceSlot, oldSlot);
        }
    }

    private int getMaceSlot() {
        if (MaceSpoofModule.mc.player == null) {
            return -1;
        }
        if (this.inventorySwap.getValue()) {
            for (int i = 9; i <= 35; ++i) {
                if (!MaceSpoofModule.mc.player.getInventory().getItem(i).is(Items.MACE)) continue;
                return i;
            }
        } else {
            for (int i = 0; i < 9; ++i) {
                if (!MaceSpoofModule.mc.player.getInventory().getItem(i).is(Items.MACE)) continue;
                return i;
            }
        }
        return -1;
    }

    private void sendFakeY(double yOffset) {
        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Pos(MaceSpoofModule.mc.player.getX(), MaceSpoofModule.mc.player.getY() + yOffset, MaceSpoofModule.mc.player.getZ(), false, MaceSpoofModule.mc.player.horizontalCollision));
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }
}

