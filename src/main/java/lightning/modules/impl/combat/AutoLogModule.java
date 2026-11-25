/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Items
 */
package lightning.modules.impl.combat;

import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

@RegisterModule(name="AutoLog", description="Logs out automatically to avoid dying.", category=Module.Category.COMBAT)
public class AutoLogModule
extends Module {
    public BooleanSetting healthCheck = new BooleanSetting("HealthCheck", "Checks if you are at a specific health to log out.", false);
    public NumberSetting health = new NumberSetting("Health", "The health the player must be at to log out.", new BooleanSetting.Visibility(this.healthCheck, true), (Number)10, (Number)0, (Number)20);
    public BooleanSetting totemCheck = new BooleanSetting("TotemCheck", "Checks if you ran out of totems to be able to log out.", true);
    public NumberSetting totemCount = new NumberSetting("Totems", "The amount of totems to have in your inventory to log out.", new BooleanSetting.Visibility(this.totemCheck, true), (Number)2, (Number)0, (Number)9);
    public BooleanSetting selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module after logging out.", true);

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (this.getNull()) {
            return;
        }
        int currentTotems = AutoLogModule.mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
        if (this.healthCheck.getValue() && AutoLogModule.mc.player.getHealth() <= (float)this.health.getValue().intValue()) {
            mc.getConnection().onDisconnect(new DisconnectionDetails((Component)Component.literal((String)("Health was lower than or equal to " + this.health.getValue().intValue() + "."))));
            if (this.selfDisable.getValue()) {
                this.setToggled(false);
            }
        }
        if (this.totemCheck.getValue() && currentTotems <= this.totemCount.getValue().intValue()) {
            mc.getConnection().onDisconnect(new DisconnectionDetails((Component)Component.literal((String)"Couldn't find totems in your inventory.")));
            if (this.selfDisable.getValue()) {
                this.setToggled(false);
            }
        }
    }
}

