/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="TextESP", description="Renders text ESP on the world.", category=Module.Category.VISUALS)
public class TextESPModule
extends Module {
    public BooleanSetting items = new BooleanSetting("Items", "Renders text ESP on item entities.", true);
    public ModeSetting itemListMode = new ModeSetting("Mode", "WhiteList = only listed items. BlackList = items NOT in list. All = render on every item.", new BooleanSetting.Visibility(this.items, true), "WhiteList", new String[]{"WhiteList", "BlackList", "All"});
    public WhitelistSetting itemWhitelist = new WhitelistSetting("Whitelist", "Items the WhiteList/BlackList mode compares against.", new BooleanSetting.Visibility(this.items, true), WhitelistSetting.Type.ITEMS);
    public BooleanSetting pearls = new BooleanSetting("Pearls", "Renders text ESP on pearl entities.", true);
    public BooleanSetting chorus = new BooleanSetting("Chorus", "Renders text ESP on chorus sounds.", true);
    public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the text ESP rendering.", 30, 10, 100);
    public ColorSetting color = new ColorSetting("Color", "The color that will be used for the text ESP rendering.", new ColorSetting.Color(Color.WHITE, false, false));
    public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect around text ESP.", false);
    private final ArrayList<Chorus> chorusList = new ArrayList();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (this.getNull()) {
            return;
        }
        synchronized (this.chorusList) {
            ClientboundSoundPacket clientboundSoundPacket;
            SoundEvent soundEvent;
            Packet<?> packet = packetReceiveEvent.getPacket();
            if (packet instanceof ClientboundSoundPacket && ((soundEvent = (SoundEvent)(clientboundSoundPacket = (ClientboundSoundPacket)packet).getSound().value()) == SoundEvents.CHORUS_FRUIT_TELEPORT || soundEvent == SoundEvents.ENDERMAN_TELEPORT)) {
                this.chorusList.add(new Chorus(this, this, mc.getSoundManager().getSoundEvent(soundEvent.location()).getSubtitle().getString(), new Vec3(clientboundSoundPacket.getX(), clientboundSoundPacket.getY(), clientboundSoundPacket.getZ())));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (this.getNull()) {
            return;
        }
        for (Entity entity : TextESPModule.mc.level.entitiesForRendering()) {
            if (!Renderer3D.isFrustumVisible(entity.getBoundingBox())) continue;
            if (entity instanceof ItemEntity itemEntity) {
                if (this.items.getValue() && this.itemAllowed(itemEntity.getItem().getItem())) {
                    Vec3 renderPos = EntityUtils.getRenderPos(itemEntity, renderWorldEvent.getTickDelta());
                    String itemText = itemEntity.getName().getString() + (itemEntity.getItem().getCount() > 1 ? " x" + itemEntity.getItem().getCount() : "");
                    Renderer3D.renderScaledText(renderWorldEvent.getMatrices(), itemText, renderPos.x, renderPos.y, renderPos.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
                    continue;
                }
            }
            if (!(entity instanceof ThrownEnderpearl pearl) || pearl.getOwner() == null || !this.pearls.getValue()) continue;
            Vec3 renderPos = EntityUtils.getRenderPos(pearl, renderWorldEvent.getTickDelta());
            Renderer3D.renderScaledText(renderWorldEvent.getMatrices(), pearl.getOwner().getName().getString(), renderPos.x, renderPos.y, renderPos.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
        }
        if (this.chorus.getValue()) {
            synchronized (this.chorusList) {
                this.chorusList.removeIf(chorus -> System.currentTimeMillis() - chorus.time >= 1500L);
                for (Chorus entry : this.chorusList) {
                    Renderer3D.renderScaledText(renderWorldEvent.getMatrices(), entry.subtitle, entry.pos.x, entry.pos.y, entry.pos.z, this.scale.getValue().intValue(), false, this.color.getColor(), this.glow.getValue());
                }
            }
        }
    }

    private boolean itemAllowed(Item item) {
        boolean inWhitelist = this.itemWhitelist.isWhitelistContains(item);
        return switch (this.itemListMode.getValue()) {
            case "WhiteList" -> inWhitelist;
            case "BlackList" -> {
                if (!inWhitelist) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    private class Chorus {
        private final String subtitle;
        private final Vec3 pos;
        private final long time;

        public Chorus(TextESPModule textESPModule, TextESPModule owner, String subtitle, Vec3 pos) {
            Objects.requireNonNull(textESPModule);
            Objects.requireNonNull(owner);
            this.subtitle = subtitle;
            this.pos = pos;
            this.time = System.currentTimeMillis();
        }
    }
}

