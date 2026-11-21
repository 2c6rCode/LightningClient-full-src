/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ChorusControlModule
extends Module {
    public BindSetting confirm = new BindSetting("Confirm", "The key that will be used to confirm the teleportation.", 340).disableHoldModes();
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the target block.", "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private ClientboundPlayerPositionPacket packet;
    private boolean cancel = false;

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        ItemStack itemStack;
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null) {
            return;
        }
        if (!this.cancel && ChorusControlModule.mc.player.isUsingItem() && (itemStack = ChorusControlModule.mc.player.getItemInHand(ChorusControlModule.mc.player.getUsedItemHand())).getItem() == Items.CHORUS_FRUIT && itemStack.getUseDuration((LivingEntity)ChorusControlModule.mc.player) - ChorusControlModule.mc.player.getTicksUsingItem() <= 1) {
            this.cancel = true;
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent keyInputEvent) {
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null || ChorusControlModule.mc.gui != null && ChorusControlModule.mc.gui.screen() != null) {
            return;
        }
        if (this.packet == null) {
            return;
        }
        if (keyInputEvent.getKey() != this.confirm.getValue()) {
            return;
        }
        this.packet.handle((ClientGamePacketListener)mc.getConnection());
        this.packet = null;
        this.cancel = false;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        ServerboundMovePlayerPacket serverboundMovePlayerPacket;
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null) {
            return;
        }
        if (!this.cancel) {
            return;
        }
        Packet<?> packet = packetSendEvent.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket && (serverboundMovePlayerPacket = (ServerboundMovePlayerPacket)packet).hasPosition()) {
            packetSendEvent.setCancelled(true);
        }
        if (packetSendEvent.getPacket() instanceof ServerboundAcceptTeleportationPacket) {
            packetSendEvent.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket clientboundPlayerPositionPacket = (ClientboundPlayerPositionPacket)packet;
            if (this.cancel) {
                packetReceiveEvent.setCancelled(true);
                this.packet = clientboundPlayerPositionPacket;
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (ChorusControlModule.mc.player == null || ChorusControlModule.mc.level == null || ChorusControlModule.mc.gui != null && ChorusControlModule.mc.gui.screen() != null) {
            return;
        }
        if (this.packet == null) {
            return;
        }
        Vec3 vec3 = new Vec3(this.packet.change().position().x(), this.packet.change().position().y(), this.packet.change().position().z());
        AABB aABB = EntityTypes.PLAYER.getDimensions().makeBoundingBox(vec3);
        if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, this.fillColor.getColor());
        }
        if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, this.outlineColor.getColor());
        }
    }

    @Override
    public void onDisable() {
        if (mc.getConnection() != null && this.packet != null) {
            this.packet.handle((ClientGamePacketListener)mc.getConnection());
            this.packet = null;
            this.cancel = false;
        }
    }
}

