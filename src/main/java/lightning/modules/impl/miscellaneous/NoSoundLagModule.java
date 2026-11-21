/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.miscellaneous;

import java.util.Set;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

@RegisterModule(name="NoSoundLag", description="Prevents lagging caused by a large amount of sounds being played.", category=Module.Category.MISCELLANEOUS)
public class NoSoundLagModule
extends Module {
    public BooleanSetting armor = new BooleanSetting("Armor", "Prevents lagging caused by armor sounds.", true);
    public BooleanSetting withers = new BooleanSetting("Withers", "Prevents lagging caused by wither sounds.", true);
    public BooleanSetting ghasts = new BooleanSetting("Ghasts", "Prevents lagging caused by ghast sounds.", true);
    public BooleanSetting piston = new BooleanSetting("Piston", "Prevents lagging caused by piston sounds.", true);
    public BooleanSetting dispenser = new BooleanSetting("Dispenser", "Prevents lagging caused by dispenser sounds.", true);
    public static final Set<Holder<SoundEvent>> ARMOR_SOUNDS = Set.of(SoundEvents.ARMOR_EQUIP_GENERIC, SoundEvents.ARMOR_EQUIP_ELYTRA, SoundEvents.ARMOR_EQUIP_DIAMOND, SoundEvents.ARMOR_EQUIP_IRON, SoundEvents.ARMOR_EQUIP_GOLD, SoundEvents.ARMOR_EQUIP_CHAIN, SoundEvents.ARMOR_EQUIP_LEATHER);
    public static final Set<SoundEvent> WITHER_SOUNDS = Set.of(SoundEvents.WITHER_AMBIENT, SoundEvents.WITHER_DEATH, SoundEvents.WITHER_BREAK_BLOCK, SoundEvents.WITHER_HURT, SoundEvents.WITHER_SPAWN, SoundEvents.WITHER_SHOOT);
    public Set<SoundEvent> GHAST_SOUNDS = Set.of(SoundEvents.GHAST_AMBIENT, SoundEvents.GHAST_DEATH, SoundEvents.GHAST_HURT, SoundEvents.GHAST_SCREAM, SoundEvents.GHAST_SHOOT, SoundEvents.GHAST_WARN);
    public static final Set<SoundEvent> PISTON_SOUNDS = Set.of(SoundEvents.PISTON_EXTEND, SoundEvents.PISTON_CONTRACT);
    public static final Set<SoundEvent> DISPENSER_SOUNDS = Set.of(SoundEvents.DISPENSER_DISPENSE, SoundEvents.DISPENSER_FAIL, SoundEvents.DISPENSER_LAUNCH);

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundSoundPacket soundPacket) {
            if (this.armor.getValue() && ARMOR_SOUNDS.contains(soundPacket.getSound()) || this.withers.getValue() && WITHER_SOUNDS.contains(soundPacket.getSound().value()) || this.ghasts.getValue() && this.GHAST_SOUNDS.contains(soundPacket.getSound().value()) || this.piston.getValue() && PISTON_SOUNDS.contains(soundPacket.getSound().value()) || this.dispenser.getValue() && DISPENSER_SOUNDS.contains(soundPacket.getSound().value())) {
                packetReceiveEvent.setCancelled(true);
            }
        } else if (packet instanceof ClientboundLevelEventPacket levelEventPacket) {
            if (this.dispenser.getValue() && (levelEventPacket.getType() == 1000 || levelEventPacket.getType() == 1001 || levelEventPacket.getType() == 1002)) {
                packetReceiveEvent.setCancelled(true);
            }
        }
    }
}
