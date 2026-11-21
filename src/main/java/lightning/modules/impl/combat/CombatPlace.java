/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.InterpolationHandler
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="CombatPlace", description="Automatically places blocks in front of moving targets to block them.", category=Module.Category.COMBAT, proxyEnhanced=true)
public class CombatPlace
extends Module {
    public BooleanSetting flatten = new BooleanSetting("Flatten", "Place blocks under the target.", true);
    public ModeSetting mode = new ModeSetting("Mode", "Prediction mode.", "Two", new String[]{"None", "One", "Two", "Three"});
    public ModeSetting targetPriority = new ModeSetting("Priority", "Target selection priority.", "Closest", new String[]{"Closest", "Health"});
    public NumberSetting predictTicks = new NumberSetting("PredictTicks", "Prediction scale.", 2, 1, 8);
    public NumberSetting minKmh = new NumberSetting("MinKMH", "Min speed to trigger blocker.", 20, 1, 40);
    public NumberSetting limit = new NumberSetting("Limit", "The maximum number of blocks that can be placed per tick.", 4, 1, 20);
    public NumberSetting delay = new NumberSetting("Delay", "The delay in ticks between placements.", 0, 0, 20);
    public BooleanSetting strict = new BooleanSetting("Strict", "Prevent placement if entities intersect.", true);
    public BooleanSetting safety = new BooleanSetting("Safety", "Prevent trapping yourself.", true);
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which blocks will be placed.", 5.0, 0.0, 12.0);
    public BooleanSetting rotate = new BooleanSetting("Rotate", "Sends a packet rotation whenever placing a block.", true);
    public BooleanSetting crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
    private int delayTicks = 0;

    @Override
    public void onEnable() {
        this.delayTicks = 0;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (this.shouldRunOnProxy()) {
            return;
        }
        if (this.getNull()) {
            return;
        }
        Player player = this.findTarget();
        if (player == null) {
            return;
        }
        if (this.delayTicks < this.delay.getValue().intValue()) {
            ++this.delayTicks;
            return;
        }
        List<BlockPos> list = this.getPlacePositions(player);
        if (list.isEmpty()) {
            return;
        }
        double d = Math.pow(this.range.getValue().doubleValue(), 2.0);
        list.removeIf(blockPos -> CombatPlace.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > d);
        if (list.isEmpty()) {
            return;
        }
        this.placeBlocks(list);
        this.delayTicks = 0;
    }

    private void placeBlocks(List<BlockPos> list) {
        if (CombatPlace.mc.player == null) {
            return;
        }
        int n = InventoryUtils.findHardestBlock(0, 8);
        if (n == -1) {
            return;
        }
        int n2 = CombatPlace.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot("Silent", n, n2);
        int n3 = 0;
        for (BlockPos blockPos : list) {
            if (n3 >= this.limit.getValue().intValue()) break;
            Direction direction = WorldUtils.getDirection(blockPos, false);
            if (direction == null) continue;
            WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), false);
            ++n3;
        }
        InventoryUtils.switchBack("Silent", n, n2);
    }

    private List<BlockPos> getPlacePositions(Player player) {
        String string;
        BlockPos blockPos;
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        if (this.flatten.getValue() && this.isValidSpot(blockPos = BlockPos.containing((Position)player.position()).below())) {
            arrayList.add(blockPos);
        }
        if (!(string = this.mode.getValue()).equals("None")) {
            double speedKmh = EntityUtils.getSpeed((Entity)player, EntityUtils.SpeedUnit.KILOMETERS);
            if (speedKmh >= this.minKmh.getValue().doubleValue()) {
                double d3;
                InterpolationHandler interpolationHandler = player.getInterpolation();
                Vec3 vec3 = interpolationHandler != null ? interpolationHandler.position() : player.position();
                double d4 = vec3.x;
                double d5 = vec3.z;
                double d6 = d4 - player.getX();
                double d7 = Math.sqrt(d6 * d6 + (d3 = d5 - player.getZ()) * d3);
                if (d7 > 0.001) {
                    double d8;
                    double d9 = d6 / d7;
                    double d10 = d3 / d7;
                    double d11 = Math.sqrt(Math.pow(player.getX() - player.xo, 2.0) + Math.pow(player.getZ() - player.zo, 2.0));
                    BlockPos blockPos2 = BlockPos.containing((double)(d4 + d9 * d11 * (d8 = this.predictTicks.getValue().doubleValue())), (double)player.getY(), (double)(d5 + d10 * d11 * d8));
                    if (this.isValidSpot(blockPos2)) {
                        BlockPos blockPos3;
                        BlockPos blockPos4;
                        arrayList.add(blockPos2);
                        if ((string.equals("Two") || string.equals("Three")) && this.isValidSpot(blockPos4 = blockPos2.above())) {
                            arrayList.add(blockPos4);
                        }
                        if (string.equals("Three") && this.isValidSpot(blockPos3 = blockPos2.above(2))) {
                            arrayList.add(blockPos3);
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    private boolean isValidSpot(BlockPos blockPos) {
        AABB aABB;
        if (CombatPlace.mc.level == null) {
            return false;
        }
        if (CombatPlace.mc.level.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        if (!CombatPlace.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        if (this.strict.getValue() && CombatPlace.mc.level.getEntities((Entity)null, aABB = new AABB(blockPos), entity -> true).stream().anyMatch(entity -> entity instanceof Player)) {
            return false;
        }
        return !this.safety.getValue() || CombatPlace.mc.player == null || !CombatPlace.mc.player.getBoundingBox().intersects(new AABB(blockPos));
    }

    private Player findTarget() {
        if (CombatPlace.mc.level == null || CombatPlace.mc.player == null) {
            return null;
        }
        return CombatPlace.mc.level.players().stream().filter(abstractClientPlayer -> abstractClientPlayer != CombatPlace.mc.player && !abstractClientPlayer.isRemoved() && !Night.FRIEND_MANAGER.contains(abstractClientPlayer.getName().getString())).filter(abstractClientPlayer -> (double)CombatPlace.mc.player.distanceTo((Entity)abstractClientPlayer) <= this.range.getValue().doubleValue() + 4.0).min(this.getComparator()).orElse(null);
    }

    private Comparator<Player> getComparator() {
        if (this.targetPriority.getValue().equals("Health")) {
            return Comparator.comparingDouble(player -> player.getHealth() + player.getAbsorptionAmount());
        }
        return Comparator.comparingDouble(player -> CombatPlace.mc.player != null ? (double)CombatPlace.mc.player.distanceTo((Entity)player) : 0.0);
    }
}

