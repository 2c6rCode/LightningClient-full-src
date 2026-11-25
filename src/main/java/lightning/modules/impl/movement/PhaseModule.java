/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.movement;

import lightning.Night;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.ISprintModule;
import lightning.modules.impl.player.MultiTaskModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Phase", description="Throws an ender pearl to phase into blocks. KingMC Crawl mode requires crawling away from the target block before throwing.", category=Module.Category.MOVEMENT)
public class PhaseModule
extends Module {
    public ModeSetting logic = new ModeSetting("Logic", "Phase bypass targeting logic. PIP = one combined logic: KingMC standing/crawl targeting, automatic Grim/NCP fallback when the throw would land in the void, and the NCP bypass item step when the item is in your hotbar.", "PIP", new String[]{"PIP"});
    public ModeSetting swap = new ModeSetting("Swap", "The mode that will be used for switching to the pearl before throwing it.", "Silent", InventoryUtils.SWITCH_MODES);
    public BooleanSetting crawl = new BooleanSetting("Crawl", "Crawl phase mode. In KingMC mode, crawl away from the target block before throwing.", false);
    public BooleanSetting antiVoid = new BooleanSetting("AntiVoid", "Prevents phasing into or through blocks that lead directly into the void.", true);
    public BooleanSetting debug = new BooleanSetting("Debug", "Chat-prints pos/yaw/pitch/target for every throw, so a failed (popped-up) attempt can be matched back to its exact numbers.", false);
    public ModeSetting ncpMode = new ModeSetting("NCP-Mode", "Item used to bypass pearl distance checks on NCP.", new ModeSetting.Visibility(this.logic, "PIP"), "FireCharge", new String[]{"FireCharge", "FlintAndSteel", "Cobweb"});
    public ModeSetting bypassSwitch = new ModeSetting("Switch", "The mode that will be used for switching to the bypass item.", new ModeSetting.Visibility(this.logic, "PIP"), "Silent", new String[]{"Normal", "Silent", "AltPickup", "AltSwap"});
    public BooleanSetting autoRemove = new BooleanSetting("AutoRemove", "Automatically removes the bypass block/fire once you have phased.", new ModeSetting.Visibility(this.logic, "PIP"), true);
    private static final double CORNER_THRESHOLD = 0.5;
    private static final double CORNER_OFFSET = 0.5;
    private int attempt = 0;

    private boolean isInWeb(Entity entity) {
        if (entity.getInBlockState().is(Blocks.COBWEB)) {
            return true;
        }
        for (BlockPos blockPos : BlockPos.betweenClosed((BlockPos)BlockPos.containing((double)entity.getBoundingBox().minX, (double)entity.getBoundingBox().minY, (double)entity.getBoundingBox().minZ), (BlockPos)BlockPos.containing((double)entity.getBoundingBox().maxX, (double)entity.getBoundingBox().maxY, (double)entity.getBoundingBox().maxZ))) {
            if (!entity.level().getBlockState(blockPos).is(Blocks.COBWEB)) continue;
            return true;
        }
        return false;
    }

    private boolean isInLadder(Entity entity) {
        if (entity.getInBlockState().is(Blocks.LADDER)) {
            return true;
        }
        for (BlockPos blockPos : BlockPos.betweenClosed((BlockPos)BlockPos.containing((double)entity.getBoundingBox().minX, (double)entity.getBoundingBox().minY, (double)entity.getBoundingBox().minZ), (BlockPos)BlockPos.containing((double)entity.getBoundingBox().maxX, (double)entity.getBoundingBox().maxY, (double)entity.getBoundingBox().maxZ))) {
            if (!entity.level().getBlockState(blockPos).is(Blocks.LADDER)) continue;
            return true;
        }
        return false;
    }

    private boolean isPlayerCrawling() {
        if (PhaseModule.mc.player == null) {
            return false;
        }
        return PhaseModule.mc.player.getPose() == Pose.SWIMMING || PhaseModule.mc.player.isVisuallyCrawling() || PhaseModule.mc.player.getEyeHeight() <= 1.0f;
    }

    @Override
    public void onEnable() {
        if (PhaseModule.mc.player == null || PhaseModule.mc.level == null) {
            this.setToggled(false);
            return;
        }
        for (BlockPos blockPos : new BlockPos[]{PhaseModule.mc.player.blockPosition(), PhaseModule.mc.player.blockPosition().below()}) {
            if (!PhaseModule.mc.level.getBlockState(blockPos).is(Blocks.SCAFFOLDING)) continue;
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP, sequence));
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP, sequence));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            PhaseModule.mc.level.removeBlock(blockPos, false);
        }
        boolean pipMode = true;
        boolean isCrawling = this.isPlayerCrawling();
        if (!this.crawl.getValue() && isCrawling && !pipMode) {
            this.setToggled(false);
            return;
        }
        boolean crawlPhase = this.crawl.getValue() && isCrawling;
        boolean inWeb = this.isInWeb((Entity)PhaseModule.mc.player);
        boolean inLadder = this.isInLadder((Entity)PhaseModule.mc.player);
        if (!(crawlPhase || inWeb || inLadder || PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).canBeReplaced())) {
            this.setToggled(false);
            return;
        }
        if (PhaseModule.mc.player.isCrouching()) {
            this.setToggled(false);
            return;
        }
        if (PhaseModule.mc.player.getCooldowns().isOnCooldown(new ItemStack((ItemLike)Items.ENDER_PEARL))) {
            this.setToggled(false);
            return;
        }
        MultiTaskModule multiTaskModule = Night.MODULE_MANAGER.getModule(MultiTaskModule.class);
        boolean usingMultiTask = multiTaskModule != null && multiTaskModule.isToggled() && multiTaskModule.pearl.getValue() && PhaseModule.mc.player.isUsingItem();
        String swapMode = usingMultiTask ? "AltSwap" : this.swap.getValue();
        int pearlSlot = InventoryUtils.find(Items.ENDER_PEARL, 0, swapMode.equalsIgnoreCase("AltSwap") || swapMode.equalsIgnoreCase("AltPickup") ? 35 : 8);
        int previousSlot = PhaseModule.mc.player.getInventory().getSelectedSlot();
        boolean placedBypass = false;
        if (pearlSlot == -1) {
            Night.CHAT_MANAGER.tagged("No pearls could be found in your hotbar.", this.getName());
            this.setToggled(false);
            return;
        }
        BlockPos blockPos = PhaseModule.mc.player.blockPosition().below();
        float phaseYaw = Mth.wrapDegrees((float)this.getPhaseYaw());
        float sectorAngle = (phaseYaw % 90.0f + 90.0f) % 90.0f;
        boolean isStraight = Math.min(sectorAngle, 90.0f - sectorAngle) < 22.5f;
        boolean onBedrock = this.isOnBedrock();
        Vec3 targetPos = this.pipTarget(phaseYaw, isCrawling, isStraight, onBedrock);
        if (this.antiVoid.getValue() && this.isVoidHazard(targetPos, phaseYaw)) {
            Night.CHAT_MANAGER.tagged("Phase cancelled to prevent falling into the void.", this.getName());
            if (mc.getSoundManager() != null) {
                mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.NOTE_BLOCK_PLING, (float)1.5f));
            }
            this.setToggled(false);
            return;
        }
        float targetYaw = this.calcYaw(targetPos);
        float targetPitch = this.solvePitch(targetPos);
        if (this.debug.getValue()) {
            ++this.attempt;
            Night.CHAT_MANAGER.tagged(String.format("#%d [%s%s] pos=(%.4f, %.4f, %.4f) yaw=%.2f pitch=%.2f target=(%.4f, %.4f, %.4f) sector=%s", this.attempt, this.logic.getValue(), pipMode && onBedrock ? "/Bedrock" : (pipMode && isCrawling ? "/Crawl" : (pipMode ? "/Standing" : "")), PhaseModule.mc.player.getX(), PhaseModule.mc.player.getY(), PhaseModule.mc.player.getZ(), Float.valueOf(targetYaw), Float.valueOf(targetPitch), targetPos.x, targetPos.y, targetPos.z, isStraight ? "straight" : "corner"), this.getName());
        }
        if ((PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).isAir() || inWeb || inLadder) && !PhaseModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            Item item = switch (this.ncpMode.getValue()) {
                case "FlintAndSteel" -> Items.FLINT_AND_STEEL;
                case "Cobweb" -> Items.COBWEB;
                default -> Items.FIRE_CHARGE;
            };
            int bypassSlot = InventoryUtils.find(item, InventoryUtils.HOTBAR_START, this.bypassSwitch.getValue().equalsIgnoreCase("AltSwap") || this.bypassSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            if (bypassSlot != -1) {
                Night.ROTATION_MANAGER.packetRotate(targetYaw, 90.0f, true);
                InventoryUtils.switchSlot(this.bypassSwitch.getValue(), bypassSlot, previousSlot);
                NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 1.0, 0.0), Direction.UP, blockPos, false), sequence));
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                InventoryUtils.switchBack(this.bypassSwitch.getValue(), bypassSlot, previousSlot);
                placedBypass = true;
            }
        }
        Night.ROTATION_MANAGER.packetRotate(targetYaw, targetPitch, true);
        if (InventoryUtils.switchSlot(swapMode, pearlSlot, previousSlot)) {
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, targetYaw, targetPitch));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            InventoryUtils.switchBack(swapMode, pearlSlot, previousSlot);
        }
        if (placedBypass && this.autoRemove.getValue()) {
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, PhaseModule.mc.player.blockPosition(), Direction.UP, sequence));
            NetworkUtils.sendSequencedPacket(sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, PhaseModule.mc.player.blockPosition(), Direction.UP, sequence));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        this.setToggled(false);
    }

    private float getPhaseYaw() {
        ISprintModule iSprintModule;
        if (PhaseModule.mc.player == null) {
            return 0.0f;
        }
        float baseYaw = PhaseModule.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (iSprintModule = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && iSprintModule.isGrimCompensating()) {
            return iSprintModule.getGrimYaw();
        }
        int strafeInput = (PhaseModule.mc.options.keyRight.isDown() ? 1 : 0) - (PhaseModule.mc.options.keyLeft.isDown() ? 1 : 0);
        int forwardInput = (PhaseModule.mc.options.keyUp.isDown() ? 1 : 0) - (PhaseModule.mc.options.keyDown.isDown() ? 1 : 0);
        if (strafeInput != 0 || forwardInput != 0) {
            float moveAngle = (float)Math.toDegrees(Math.atan2(strafeInput, forwardInput));
            return Mth.wrapDegrees((float)(baseYaw + moveAngle));
        }
        return baseYaw;
    }

    private boolean isOnBedrock() {
        if (PhaseModule.mc.player == null || PhaseModule.mc.level == null) {
            return false;
        }
        double minY = PhaseModule.mc.player.getBoundingBox().minY;
        BlockPos blockPos = BlockPos.containing((double)PhaseModule.mc.player.getX(), (double)(minY - 0.1), (double)PhaseModule.mc.player.getZ());
        if (PhaseModule.mc.level.getBlockState(blockPos).is(Blocks.BEDROCK)) {
            return true;
        }
        if (PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition().below()).is(Blocks.BEDROCK)) {
            return true;
        }
        return PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).is(Blocks.BEDROCK);
    }

    private Vec3 calculateKingMCStandingTarget(float yaw) {
        double px = PhaseModule.mc.player.getX();
        double py = PhaseModule.mc.player.getY();
        double pz = PhaseModule.mc.player.getZ();
        double yawRad = Math.toRadians(yaw);
        Vec3 dir = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad)).normalize();
        float sectorAngle = (yaw % 90.0f + 90.0f) % 90.0f;
        boolean isStraight = Math.min(sectorAngle, 90.0f - sectorAngle) < 22.5f;
        if (isStraight) {
            boolean zDominant = Math.abs(dir.z) > Math.abs(dir.x);
            if (zDominant) {
                double edgeZ = dir.z > 0.0 ? Math.floor(pz) + 1.0 : Math.floor(pz);
                double targetZ = dir.z > 0.0 ? edgeZ - 0.165 : edgeZ + 0.165;
                return new Vec3(px, py, targetZ);
            }
            double edgeX = dir.x > 0.0 ? Math.floor(px) + 1.0 : Math.floor(px);
            double targetX = dir.x > 0.0 ? edgeX - 0.165 : edgeX + 0.165;
            return new Vec3(targetX, py, pz);
        }
        double edgeX = dir.x > 0.0 ? Math.floor(px) + 1.0 : Math.floor(px);
        double edgeZ = dir.z > 0.0 ? Math.floor(pz) + 1.0 : Math.floor(pz);
        double targetX = dir.x > 0.0 ? edgeX - 0.165 : edgeX + 0.165;
        double targetZ = dir.z > 0.0 ? edgeZ - 0.165 : edgeZ + 0.165;
        return new Vec3(targetX, py, targetZ);
    }

    private Vec3 calculateKingMCCrawlTarget(float yaw) {
        double targetZ;
        double targetX;
        Vec3 rayDir;
        float playerYaw;
        double playerYawRad;
        Vec3 playerDir;
        Vec3 eyePos = this.getEffectiveEyePos();
        BlockHitResult blockHitResult = PhaseModule.mc.level.clip(new ClipContext(eyePos, eyePos.add((playerDir = new Vec3(-Math.sin(playerYawRad = Math.toRadians(playerYaw = PhaseModule.mc.player.getYRot())), 0.0, Math.cos(playerYawRad)).normalize()).scale(4.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PhaseModule.mc.player));
        if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
            yaw = playerYaw;
            rayDir = playerDir;
        } else {
            double yawRad = Math.toRadians(yaw);
            rayDir = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad)).normalize();
            blockHitResult = PhaseModule.mc.level.clip(new ClipContext(eyePos, eyePos.add(rayDir.scale(4.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PhaseModule.mc.player));
        }
        BlockPos hitPos = null;
        Direction hitSide = null;
        Vec3 hitLocation = null;
        if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
            hitPos = blockHitResult.getBlockPos();
            hitSide = blockHitResult.getDirection();
            hitLocation = blockHitResult.getLocation();
        } else {
            Direction dir = Direction.fromYRot((double)yaw);
            hitPos = PhaseModule.mc.player.blockPosition().relative(dir, 1);
            hitSide = dir.getOpposite();
        }
        double targetY = (double)hitPos.getY() + 0.52;
        float sectorAngle = (yaw % 90.0f + 90.0f) % 90.0f;
        boolean isStraight = Math.min(sectorAngle, 90.0f - sectorAngle) < 22.5f;
        if (isStraight && hitSide != null && hitSide.getAxis().isHorizontal()) {
            double straightX;
            double straightZ;
            if (hitSide.getAxis() == Direction.Axis.Z) {
                straightZ = hitSide == Direction.NORTH ? (double)hitPos.getZ() : (double)hitPos.getZ() + 1.0;
                straightX = hitLocation != null && Math.abs(hitLocation.x - (double)Math.round(hitLocation.x)) < 0.25 ? (double)Math.round(hitLocation.x) : (double)hitPos.getX() + 0.5;
            } else {
                straightX = hitSide == Direction.WEST ? (double)hitPos.getX() : (double)hitPos.getX() + 1.0;
                straightZ = hitLocation != null && Math.abs(hitLocation.z - (double)Math.round(hitLocation.z)) < 0.25 ? (double)Math.round(hitLocation.z) : (double)hitPos.getZ() + 0.5;
            }
            return new Vec3(straightX, targetY, straightZ);
        }
        if (hitLocation != null) {
            targetX = Math.round(hitLocation.x);
            targetZ = Math.round(hitLocation.z);
        } else {
            targetX = Math.round(PhaseModule.mc.player.getX() + rayDir.x);
            targetZ = Math.round(PhaseModule.mc.player.getZ() + rayDir.z);
        }
        return new Vec3(targetX, targetY, targetZ);
    }

    private Vec3 calculateTargetPos() {
        double px = PhaseModule.mc.player.getX();
        double pz = PhaseModule.mc.player.getZ();
        double targetY = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double roundedX = Math.round(px);
        double roundedZ = Math.round(pz);
        double offsetX = roundedX - px;
        double offsetZ = roundedZ - pz;
        if (Math.abs(offsetX) <= 0.5 && Math.abs(offsetZ) <= 0.5) {
            return new Vec3(px + Mth.clamp((double)offsetX, (double)-0.5, (double)0.5), targetY, pz + Mth.clamp((double)offsetZ, (double)-0.5, (double)0.5));
        }
        double targetX = px + Mth.clamp((double)(this.toClosest(px, Math.floor(px) + 0.241660973353061, Math.floor(px) + 0.7853981633974483) - px), (double)-0.2, (double)0.2);
        double targetZ = pz + Mth.clamp((double)(this.toClosest(pz, Math.floor(pz) + 0.241660973353061, Math.floor(pz) + 0.7853981633974483) - pz), (double)-0.2, (double)0.2);
        return new Vec3(targetX, targetY, targetZ);
    }

    private double toClosest(double value, double low, double high) {
        return value - low > high - value ? high : low;
    }

    private Vec3 calculateGrimSectorTarget() {
        double centerX;
        double px = PhaseModule.mc.player.getX();
        double pz = PhaseModule.mc.player.getZ();
        double targetY = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double blockX = Math.floor(px);
        double blockZ = Math.floor(pz);
        double centerZ = blockZ + 0.5;
        double angleDeg = Math.toDegrees(Math.atan2(pz - centerZ, px - (centerX = blockX + 0.5)));
        if (angleDeg < 0.0) {
            angleDeg += 360.0;
        }
        int sector = (int)Math.floor((angleDeg + 22.5) / 45.0) & 7;
        double eastX = blockX + 1.0;
        double southZ = blockZ + 1.0;
        return switch (sector) {
            case 0 -> new Vec3(eastX, targetY, centerZ);
            case 1 -> new Vec3(eastX, targetY, southZ);
            case 2 -> new Vec3(centerX, targetY, southZ);
            case 3 -> new Vec3(blockX, targetY, southZ);
            case 4 -> new Vec3(blockX, targetY, centerZ);
            case 5 -> new Vec3(blockX, targetY, blockZ);
            case 6 -> new Vec3(centerX, targetY, blockZ);
            default -> new Vec3(eastX, targetY, blockZ);
        };
    }

    private Vec3 boundaryTarget(float yaw) {
        double px = PhaseModule.mc.player.getX();
        double pz = PhaseModule.mc.player.getZ();
        double targetY = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double yawRad = Math.toRadians(yaw);
        Vec3 dir = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad)).normalize();
        boolean zDominant = Math.abs(dir.z) > Math.abs(dir.x);
        if (zDominant) {
            double edgeZ = dir.z > 0.0 ? Math.floor(pz) + 1.0 : Math.floor(pz);
            return new Vec3(px, targetY, edgeZ);
        }
        double edgeX = dir.x > 0.0 ? Math.floor(px) + 1.0 : Math.floor(px);
        return new Vec3(edgeX, targetY, pz);
    }

    private Vec3 getEffectiveEyePos() {
        if (PhaseModule.mc.player == null) {
            return Vec3.ZERO;
        }
        double eyeY = PhaseModule.mc.player.getEyeY() - 0.1;
        return new Vec3(PhaseModule.mc.player.getX(), eyeY, PhaseModule.mc.player.getZ());
    }

    private float calcYaw(Vec3 target) {
        Vec3 eyePos = this.getEffectiveEyePos();
        Vec3 delta = target.subtract(eyePos);
        return (float)Math.toDegrees(Math.atan2(-delta.x, delta.z));
    }

    private float solvePitch(Vec3 target) {
        Vec3 eyePos = this.getEffectiveEyePos();
        double horizDist = Math.hypot(target.x - eyePos.x, target.z - eyePos.z);
        double deltaY = target.y - eyePos.y;
        if (horizDist < 1.0E-4) {
            return deltaY < 0.0 ? 85.0f : -85.0f;
        }
        double lowPitch = -85.0;
        double highPitch = 89.9;
        for (int i = 0; i < 30; ++i) {
            double midPitch = (lowPitch + highPitch) * 0.5;
            double simY = PhaseModule.simulatePearlYAtDist(midPitch, horizDist);
            if (simY < deltaY) {
                highPitch = midPitch;
                continue;
            }
            lowPitch = midPitch;
        }
        return (float)((lowPitch + highPitch) * 0.5);
    }

    private static double simulatePearlYAtDist(double pitchDeg, double targetDist) {
        double pitchRad = Math.toRadians(pitchDeg);
        double vx = 1.5 * Math.cos(pitchRad);
        double vy = -1.5 * Math.sin(pitchRad);
        double dist = 0.0;
        double posY = 0.0;
        for (int i = 0; i < 100; ++i) {
            double prevDist = dist;
            double prevY = posY;
            dist += vx;
            posY += vy;
            if (dist >= targetDist) {
                double fraction = (targetDist - prevDist) / (dist - prevDist);
                return prevY + (posY - prevY) * fraction;
            }
            vx *= 0.99;
            vy = vy * 0.99 - 0.03;
        }
        return posY;
    }

    private boolean isFloorVoid(double x, double y, double z) {
        if (PhaseModule.mc.level == null) {
            return false;
        }
        int minY = PhaseModule.mc.level.getMinY();
        int startY = (int)Math.floor(y);
        if (startY < minY) {
            return true;
        }
        for (int i = startY; i >= minY; --i) {
            BlockPos blockPos = BlockPos.containing((double)x, (double)i, (double)z);
            BlockState blockState = PhaseModule.mc.level.getBlockState(blockPos);
            if (blockState.isAir() || blockState.getCollisionShape((BlockGetter)PhaseModule.mc.level, blockPos).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean isVoidHazard(Vec3 targetPos, float yaw) {
        if (PhaseModule.mc.player == null || PhaseModule.mc.level == null) {
            return false;
        }
        double px = PhaseModule.mc.player.getX();
        double py = PhaseModule.mc.player.getY();
        double pz = PhaseModule.mc.player.getZ();
        int minY = PhaseModule.mc.level.getMinY();
        if (py < (double)minY) {
            return true;
        }
        double yawRad = Math.toRadians(yaw);
        Vec3 dir = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad)).normalize();
        BlockPos targetBlock = BlockPos.containing((double)targetPos.x, (double)targetPos.y, (double)targetPos.z);
        BlockPos stepBlock = BlockPos.containing((double)(px + dir.x * 0.5), (double)py, (double)(pz + dir.z * 0.5));
        boolean targetHasFloor = !this.isFloorVoid((double)targetBlock.getX() + 0.5, targetPos.y, (double)targetBlock.getZ() + 0.5);
        boolean stepHasFloor = !this.isFloorVoid((double)stepBlock.getX() + 0.5, py, (double)stepBlock.getZ() + 0.5);
        return !targetHasFloor && !stepHasFloor;
    }

    private Vec3 pipTarget(float yaw, boolean isCrawling, boolean isStraight, boolean onBedrock) {
        Vec3 target = !onBedrock ? (isCrawling ? this.calculateKingMCCrawlTarget(yaw) : this.calculateKingMCStandingTarget(yaw)) : (isStraight ? this.boundaryTarget(yaw) : this.calculateTargetPos());
        if (this.antiVoid.getValue() && this.isVoidHazard(target, yaw)) {
            Vec3 grimTarget = this.calculateGrimSectorTarget();
            if (!this.isVoidHazard(grimTarget, yaw)) {
                return grimTarget;
            }
            Vec3 fallbackTarget = isStraight ? this.boundaryTarget(yaw) : this.calculateTargetPos();
            if (!this.isVoidHazard(fallbackTarget, yaw)) {
                return fallbackTarget;
            }
        }
        return target;
    }
}

