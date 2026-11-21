/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.Potion
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package lightning.modules.impl.combat;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.system.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

@RegisterModule(name="AutoPot", description="Throws a splash potion under/above you to keep whitelisted potions' effects topped up.", category=Module.Category.COMBAT)
public class AutoPotModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "WhiteList = keep these potions' effects topped up. BlackList = keep every potion's effects topped up except these.", "WhiteList", new String[]{"WhiteList", "BlackList"});
    public WhitelistSetting whitelist = new WhitelistSetting("Whitelist", "Potions this mode's WhiteList/BlackList compares against.", WhitelistSetting.Type.POTIONS);
    public NumberSetting amplifier = new NumberSetting("Amplifier", "Minimum amplifier level required before an effect counts as \"already have it\".", 1, 0, 4);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Rotates to look straight up/down before throwing (nami: real rotation, waits for it to land before throwing).", false);
    public ModeSetting throwMode = new ModeSetting("Throw", "Where the potion lands relative to you.", new BooleanSetting.Visibility(this.rotate, true), "Under", new String[]{"Above", "Under"});
    public ModeSetting swapMode = new ModeSetting("Swap", "How the potion slot gets selected before throwing.", "Silent", new String[]{"Normal", "Silent"});
    public BooleanSetting whenNoTarget = new BooleanSetting("NoTarget", "Only throws while no enemy is nearby.", false);
    public BooleanSetting onlyPhased = new BooleanSetting("OnlyPhased", "Only throws while phased into a block.", false);
    public BooleanSetting selfToggle = new BooleanSetting("SelfToggle", "Turns the module off once every whitelisted effect is already topped up.", true);
    public NumberSetting range = new NumberSetting("Range", "How close an enemy has to be for NoTarget to see them.", new BooleanSetting.Visibility(this.whenNoTarget, true), (Number)32, (Number)4, (Number)64);
    private final Timer throwTimer = new Timer();
    private static final long THROW_DELAY_MS = 5000L;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (AutoPotModule.mc.player == null || AutoPotModule.mc.level == null) {
            return;
        }
        Potion potion = this.findMissingPotion();
        if (potion == null) {
            if (this.selfToggle.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        if (this.whenNoTarget.getValue() && this.hasNearbyEnemy()) {
            if (this.selfToggle.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        if (this.onlyPhased.getValue() && !this.isPhased()) {
            if (this.selfToggle.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        int n = this.findPot(potion);
        if (n == -1) {
            if (this.selfToggle.getValue()) {
                this.setToggled(false);
            }
            return;
        }
        if (!this.throwTimer.hasTimeElapsed(5000L)) {
            return;
        }
        int n2 = n < 9 ? n : -1;
        int n3 = AutoPotModule.mc.player.getInventory().getSelectedSlot();
        if (n2 == -1) {
            InventoryUtils.swap("Pickup", n, InventoryUtils.HOTBAR_START);
            n2 = InventoryUtils.HOTBAR_START;
        }
        float f = this.throwMode.getValue().equalsIgnoreCase("Above") ? -90.0f : 90.0f;
        float f2 = f;
        if (this.rotate.getValue()) {
            float[] fArray = new float[]{AutoPotModule.mc.player.getYRot(), f};
            Night.ROTATION_MANAGER.legacyRotate(fArray, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        this.throwTimer.reset();
        InventoryUtils.switchSlot(this.swapMode.getValue(), n2, n3);
        AutoPotModule.mc.gameMode.useItem((Player)AutoPotModule.mc.player, InteractionHand.MAIN_HAND);
        InventoryUtils.switchBack(this.swapMode.getValue(), n2, n3);
    }

    private Potion findMissingPotion() {
        int n = this.amplifier.getValue().intValue();
        for (Potion potion : this.whitelist.getWhitelistedPotions()) {
            boolean bl = false;
            for (MobEffectInstance mobEffectInstance : potion.getEffects()) {
                boolean bl2 = false;
                for (MobEffectInstance mobEffectInstance2 : AutoPotModule.mc.player.getActiveEffects()) {
                    if (mobEffectInstance2.getEffect() != mobEffectInstance.getEffect() || mobEffectInstance2.getAmplifier() < n) continue;
                    bl2 = true;
                    break;
                }
                if (bl2) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
            return potion;
        }
        return null;
    }

    private int findPot(Potion potion) {
        for (int i = 0; i < 36; ++i) {
            PotionContents potionContents;
            ItemStack itemStack = AutoPotModule.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || itemStack.getItem() != Items.SPLASH_POTION || (potionContents = (PotionContents)itemStack.get(DataComponents.POTION_CONTENTS)) == null || potionContents.potion().isEmpty() || ((Holder)potionContents.potion().get()).value() != potion) continue;
            return i;
        }
        return -1;
    }

    private boolean hasNearbyEnemy() {
        double d = Mth.square((double)this.range.getValue().doubleValue());
        for (Player player : AutoPotModule.mc.level.players()) {
            if (player == AutoPotModule.mc.player || Night.FRIEND_MANAGER.contains(player.getName().getString()) || !(AutoPotModule.mc.player.distanceToSqr((Entity)player) <= d)) continue;
            return true;
        }
        return false;
    }

    private boolean isPhased() {
        AABB aABB = AutoPotModule.mc.player.getBoundingBox();
        int n = Mth.floor((double)aABB.minX);
        int n2 = Mth.ceil((double)aABB.maxX);
        int n3 = Mth.floor((double)aABB.minY);
        int n4 = Mth.ceil((double)aABB.maxY);
        int n5 = Mth.floor((double)aABB.minZ);
        int n6 = Mth.ceil((double)aABB.maxZ);
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    VoxelShape voxelShape = AutoPotModule.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)AutoPotModule.mc.level, blockPos);
                    if (voxelShape.isEmpty() || !voxelShape.bounds().move(blockPos).intersects(aABB)) continue;
                    return true;
                }
            }
        }
        return false;
    }
}

