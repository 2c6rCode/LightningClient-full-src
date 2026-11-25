/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundDisconnectPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetHealthPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import java.util.ArrayList;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.HoleSnapModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.system.Timer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="FakeLag", description="Chokes sent packets to look like you are lagging.", category=Module.Category.MOVEMENT)
public class FakeLagModule
extends Module {
    public NumberSetting choke = new NumberSetting("Choke", "The delay to choke packets for.", 2, 1, 5);
    public BooleanSetting onlyOnHoleLeave = new BooleanSetting("OnlyOnHoleLeave", "Only choke packets when leaving a hole.", false);
    public NumberSetting autoDisable = new NumberSetting("AutoDisable", "AutoDisable", "Delay (ms) after FakeLag is enabled before it disables itself. 0 = off.", new Setting.Visibility(), 0, 0, 1000, 100);
    public ModeSetting mode = new ModeSetting("RenderMode", "The rendering that will be applied to the target entity.", "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private final ArrayList<ServerboundMovePlayerPacket> packets = new ArrayList();
    private final Timer timer = new Timer();
    private final Timer safety = new Timer();
    private final Timer chokeTimer = new Timer();
    private final Timer autoDisableTimer = new Timer();
    private boolean pendingAutoDisable = false;
    private boolean sending = false;
    private Vec3 serverPosition = null;
    private boolean isChoking = false;
    private BlockPos currentPos = null;

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (this.getNull()) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket posPacket) {
            this.serverPosition = new Vec3(posPacket.change().position().x(), posPacket.change().position().y(), posPacket.change().position().z());
            this.sendPackets();
            this.safety.reset();
        } else if (packet instanceof ClientboundDisconnectPacket) {
            this.sendPackets();
            this.safety.reset();
        } else if (packet instanceof ClientboundSetHealthPacket healthPacket) {
            if (healthPacket.getHealth() <= 0.0f) {
                this.sendPackets();
                this.safety.reset();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        Packet<?> packet = packetSendEvent.getPacket();
        if (this.getNull() || this.sending || !(packet instanceof ServerboundMovePlayerPacket serverboundMovePlayerPacket) || !this.hasPositionChange(serverboundMovePlayerPacket)) {
            return;
        }
        if (!this.shouldChoke()) {
            this.serverPosition = FakeLagModule.mc.player.position();
            return;
        }
        synchronized (this.packets) {
            packetSendEvent.setCancelled(true);
            this.packets.add(serverboundMovePlayerPacket);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (this.getNull() || this.serverPosition == null || this.mode.getValue().equalsIgnoreCase("None")) {
            return;
        }
        AABB aABB = new AABB(this.serverPosition.x - FakeLagModule.mc.player.getBoundingBox().getXsize() / 2.0, this.serverPosition.y, this.serverPosition.z - FakeLagModule.mc.player.getBoundingBox().getZsize() / 2.0, this.serverPosition.x + FakeLagModule.mc.player.getBoundingBox().getXsize() / 2.0, this.serverPosition.y + FakeLagModule.mc.player.getBoundingBox().getYsize(), this.serverPosition.z + FakeLagModule.mc.player.getBoundingBox().getZsize() / 2.0);
        if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, this.fillColor.getColor());
        }
        if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
            Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, this.outlineColor.getColor());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (this.getNull()) {
            return;
        }
        if (this.onlyOnHoleLeave.getValue()) {
            BlockPos previousPos = this.currentPos;
            this.currentPos = BlockPos.containing((Position)FakeLagModule.mc.player.position());
            BlockPos lastPos = previousPos == null ? this.currentPos : previousPos;
            if (!this.currentPos.equals((Object)lastPos)) {
                boolean currentlyInHole = HoleSnapModule.isInHole(this.currentPos);
                boolean previouslyInHole = HoleSnapModule.isInHole(lastPos);
                if (!currentlyInHole && previouslyInHole) {
                    this.isChoking = true;
                    this.chokeTimer.reset();
                }
            }
            if (this.isChoking && this.chokeTimer.hasTimeElapsed((int)(this.choke.getValue().floatValue() * 100.0f))) {
                this.sendPackets();
                this.isChoking = false;
            }
        } else if (!this.packets.isEmpty() && this.timer.hasTimeElapsed((int)(this.choke.getValue().floatValue() * 100.0f))) {
            this.sendPackets();
            this.timer.reset();
        }
        if (this.pendingAutoDisable && this.autoDisableTimer.hasTimeElapsed(this.autoDisable.getValue().intValue())) {
            this.pendingAutoDisable = false;
            this.setToggled(false);
        }
    }

    private void startAutoDisable() {
        this.pendingAutoDisable = true;
        this.autoDisableTimer.reset();
    }

    @Override
    public void onEnable() {
        this.timer.reset();
        this.isChoking = false;
        if (this.autoDisable.getValue().intValue() > 0) {
            this.startAutoDisable();
        } else {
            this.pendingAutoDisable = false;
        }
        this.currentPos = null;
        this.serverPosition = FakeLagModule.mc.player != null ? FakeLagModule.mc.player.position() : null;
    }

    @Override
    public void onDisable() {
        if (this.getNull()) {
            return;
        }
        this.sendPackets();
        this.isChoking = false;
        this.currentPos = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendPackets() {
        synchronized (this.packets) {
            this.sending = true;
            for (ServerboundMovePlayerPacket serverboundMovePlayerPacket : this.packets) {
                FakeLagModule.mc.player.connection.send((Packet)serverboundMovePlayerPacket);
            }
            if (!this.packets.isEmpty()) {
                this.serverPosition = FakeLagModule.mc.player.position();
            }
            this.packets.clear();
            this.sending = false;
        }
    }

    private boolean shouldChoke() {
        return this.onlyOnHoleLeave.getValue() ? this.isChoking : (EntityUtils.getSpeed((Entity)FakeLagModule.mc.player, EntityUtils.SpeedUnit.KILOMETERS) >= 5.0 || FakeLagModule.mc.player.fallDistance > 0.0) && this.safety.hasTimeElapsed(1000);
    }

    private boolean hasPositionChange(ServerboundMovePlayerPacket serverboundMovePlayerPacket) {
        return serverboundMovePlayerPacket instanceof ServerboundMovePlayerPacket.Pos || serverboundMovePlayerPacket instanceof ServerboundMovePlayerPacket.PosRot;
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.shouldChoke() ? ChatFormatting.GREEN : ChatFormatting.RED) + "Choke";
    }
}

