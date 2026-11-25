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
        List<BlockPos> positions = this.getPlacePositions(player);
        if (positions.isEmpty()) {
            return;
        }
        double rangeSq = Math.pow(this.range.getValue().doubleValue(), 2.0);
        positions.removeIf(blockPos -> CombatPlace.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > rangeSq);
        if (positions.isEmpty()) {
            return;
        }
        this.placeBlocks(positions);
        this.delayTicks = 0;
    }

    private void placeBlocks(List<BlockPos> positions) {
        if (CombatPlace.mc.player == null) {
            return;
        }
        int blockSlot = InventoryUtils.findHardestBlock(0, 8);
        if (blockSlot == -1) {
            return;
        }
        int oldSlot = CombatPlace.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot("Silent", blockSlot, oldSlot);
        int placedCount = 0;
        for (BlockPos blockPos : positions) {
            if (placedCount >= this.limit.getValue().intValue()) break;
            Direction direction = WorldUtils.getDirection(blockPos, false);
            if (direction == null) continue;
            WorldUtils.placeBlock(blockPos, direction, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), false);
            ++placedCount;
        }
        InventoryUtils.switchBack("Silent", blockSlot, oldSlot);
    }

    private List<BlockPos> getPlacePositions(Player player) {
        String predictMode;
        BlockPos belowPos;
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        if (this.flatten.getValue() && this.isValidSpot(belowPos = BlockPos.containing((Position)player.position()).below())) {
            positions.add(belowPos);
        }
        if (!(predictMode = this.mode.getValue()).equals("None")) {
            double speedKmh = EntityUtils.getSpeed((Entity)player, EntityUtils.SpeedUnit.KILOMETERS);
            if (speedKmh >= this.minKmh.getValue().doubleValue()) {
                double deltaZ;
                InterpolationHandler interpolationHandler = player.getInterpolation();
                Vec3 interpPos = interpolationHandler != null ? interpolationHandler.position() : player.position();
                double interpX = interpPos.x;
                double interpZ = interpPos.z;
                double deltaX = interpX - player.getX();
                double horizDist = Math.sqrt(deltaX * deltaX + (deltaZ = interpZ - player.getZ()) * deltaZ);
                if (horizDist > 0.001) {
                    double scale;
                    double dirX = deltaX / horizDist;
                    double dirZ = deltaZ / horizDist;
                    double stepSpeed = Math.sqrt(Math.pow(player.getX() - player.xo, 2.0) + Math.pow(player.getZ() - player.zo, 2.0));
                    BlockPos frontPos = BlockPos.containing((double)(interpX + dirX * stepSpeed * (scale = this.predictTicks.getValue().doubleValue())), (double)player.getY(), (double)(interpZ + dirZ * stepSpeed * scale));
                    if (this.isValidSpot(frontPos)) {
                        BlockPos topPos;
                        BlockPos upperPos;
                        positions.add(frontPos);
                        if ((predictMode.equals("Two") || predictMode.equals("Three")) && this.isValidSpot(upperPos = frontPos.above())) {
                            positions.add(upperPos);
                        }
                        if (predictMode.equals("Three") && this.isValidSpot(topPos = frontPos.above(2))) {
                            positions.add(topPos);
                        }
                    }
                }
            }
        }
        return positions;
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

