/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.Items
 */
package lightning.modules.impl.combat;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.SelfBowModule;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.NetworkUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

@RegisterModule(name="AutoBowRelease", description="Automatically releases your bow after a certain amount of time has passed.", category=Module.Category.COMBAT)
public class AutoBowReleaseModule
extends Module {
    public NumberSetting ticks = new NumberSetting("Ticks", "The number of ticks that have to be waited for before releasing the bow.", 3, 0, 20);

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (Night.MODULE_MANAGER.getModule(SelfBowModule.class).isToggled()) {
            return;
        }
        if ((AutoBowReleaseModule.mc.player.getOffhandItem().getItem() == Items.BOW || AutoBowReleaseModule.mc.player.getMainHandItem().getItem() == Items.BOW) && AutoBowReleaseModule.mc.player.isUsingItem() && AutoBowReleaseModule.mc.player.getTicksUsingItem() >= this.ticks.getValue().intValue()) {
            mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, AutoBowReleaseModule.mc.player.getDirection()));
            NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemPacket(AutoBowReleaseModule.mc.player.getOffhandItem().getItem() == Items.BOW ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, n, AutoBowReleaseModule.mc.player.getYRot(), AutoBowReleaseModule.mc.player.getXRot()));
            AutoBowReleaseModule.mc.player.stopUsingItem();
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.ticks.getValue().intValue());
    }
}

