/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.arrow.AbstractArrow
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.AABB
 */
package lightning.modules.impl.combat;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

@RegisterModule(name="AntiPhase", description="Places a climbable block (Scaffolding, Ladder, Vines, Rail) directly at a target enemy's feet.", category=Module.Category.COMBAT)
public class AntiPhaseModule
extends Module {
    public ModeSetting block = new ModeSetting("Block", "The item placed at the enemy's feet.", "Scaffolding", new String[]{"Scaffolding", "Ladder", "Vines", "Rail", "PoweredRail", "DetectorRail", "ActivatorRail"});
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to the selected item.", "Silent", InventoryUtils.SWITCH_MODES);
    public ModeSetting rotate = new ModeSetting("Rotate", "The rotation mode when placing.", "Grim", new String[]{"None", "Normal", "Grim"});
    public BooleanSetting strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
    public BooleanSetting render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
    public NumberSetting range = new NumberSetting("Range", "The maximum distance at which enemies will be targeted.", Float.valueOf(5.0f), Float.valueOf(1.0f), Float.valueOf(6.0f));
    public NumberSetting wallRange = new NumberSetting("WallRange", "The maximum distance at which enemies will be targeted through walls (no line of sight required).", Float.valueOf(3.0f), Float.valueOf(0.0f), Float.valueOf(6.0f));
    public BooleanSetting onlyHole = new BooleanSetting("OnlyHole", "Only places while the enemy is already standing in a hole (feet surrounded by blocks).", false);
    public BooleanSetting pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses placing while eating or using items.", false);
    private Player target;

    @Override
    public String getMetaData() {
        return this.target != null ? this.target.getName().getString() : "None";
    }

    @Override
    public void onDisable() {
        this.target = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (AntiPhaseModule.mc.player == null || AntiPhaseModule.mc.level == null) {
            return;
        }
        if (this.pauseOnEat.getValue() && (AntiPhaseModule.mc.player.isUsingItem() || AntiPhaseModule.mc.player.getUseItemRemainingTicks() > 0)) {
            return;
        }
        this.target = this.findTarget();
        if (this.target == null) {
            return;
        }
        if (this.onlyHole.getValue() && !HoleUtils.isPlayerInHole(this.target)) {
            return;
        }
        int n = this.findItemSlot(this.resolveItem());
        if (n == -1) {
            return;
        }
        Set<BlockPos> set = this.footCells(this.target);
        int n2 = AntiPhaseModule.mc.player.getInventory().getSelectedSlot();
        Night.ROTATION_MANAGER.beginBatchRotation();
        try {
            if (!InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, n2)) {
                return;
            }
            for (BlockPos blockPos : set) {
                if (!this.isPlaceable(blockPos, this.target)) continue;
                boolean bl = false;
                for (Direction direction : WorldUtils.getDirections(blockPos, List.of(), this.strictDirection.getValue())) {
                    if (!WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), true, this.render.getValue())) continue;
                    bl = true;
                    break;
                }
                if (bl) continue;
                WorldUtils.airPlaceBlock(blockPos, InteractionHand.MAIN_HAND, "None", this.render.getValue());
            }
            InventoryUtils.switchBack(this.autoSwitch.getValue(), n, n2);
        }
        finally {
            Night.ROTATION_MANAGER.endBatchRotation();
        }
    }

    private Item resolveItem() {
        if (this.block.getValue().equals("Rail")) {
            return Items.RAIL;
        }
        if (this.block.getValue().equals("PoweredRail")) {
            return Items.POWERED_RAIL;
        }
        if (this.block.getValue().equals("DetectorRail")) {
            return Items.DETECTOR_RAIL;
        }
        if (this.block.getValue().equals("ActivatorRail")) {
            return Items.ACTIVATOR_RAIL;
        }
        return switch (this.block.getValue()) {
            case "Ladder" -> Items.LADDER;
            case "Vines" -> Items.VINE;
            default -> Items.SCAFFOLDING;
        };
    }

    private int findItemSlot(Item item) {
        for (int i = 0; i <= 8; ++i) {
            if (!AntiPhaseModule.mc.player.getInventory().getItem(i).is(item)) continue;
            return i;
        }
        return -1;
    }

    private Player findTarget() {
        return AntiPhaseModule.mc.level.players().stream().filter(this::isValidTarget).min(Comparator.comparingDouble(abstractClientPlayer -> AntiPhaseModule.mc.player.distanceToSqr((Entity)abstractClientPlayer))).orElse(null);
    }

    private boolean isValidTarget(Player player) {
        if (player == AntiPhaseModule.mc.player || !player.isAlive() || player.isRemoved() || player.isSpectator()) {
            return false;
        }
        if (EntityUtils.isGhost((Entity)player)) {
            return false;
        }
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return false;
        }
        double d = AntiPhaseModule.mc.player.distanceToSqr((Entity)player);
        if (d > Mth.square((double)this.range.getValue().doubleValue())) {
            return false;
        }
        boolean bl = d > Mth.square((double)this.wallRange.getValue().doubleValue());
        return !bl || WorldUtils.canSee((Entity)player);
    }

    private boolean isPlaceable(BlockPos blockPos, Player player) {
        if (AntiPhaseModule.mc.level == null) {
            return false;
        }
        if (AntiPhaseModule.mc.level.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        if (!AntiPhaseModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        return AntiPhaseModule.mc.level.getEntities((Entity)null, new AABB(blockPos), entity -> true).stream().noneMatch(entity -> entity != player && !(entity instanceof ExperienceOrb) && !(entity instanceof ItemEntity) && !(entity instanceof AbstractArrow) && !EntityUtils.isGhost(entity));
    }

    private Set<BlockPos> footCells(Player player) {
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        AABB aABB = player.getBoundingBox();
        int n = player.blockPosition().getY();
        int n2 = (int)Math.floor(aABB.minX);
        while ((double)n2 < Math.ceil(aABB.maxX)) {
            int n3 = (int)Math.floor(aABB.minZ);
            while ((double)n3 < Math.ceil(aABB.maxZ)) {
                hashSet.add(new BlockPos(n2, n, n3));
                ++n3;
            }
            ++n2;
        }
        return hashSet;
    }
}

