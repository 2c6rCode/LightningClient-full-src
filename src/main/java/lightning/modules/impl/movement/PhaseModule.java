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
            NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP, n));
            NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP, n));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            PhaseModule.mc.level.removeBlock(blockPos, false);
        }
        boolean bl = true;
        boolean isCrawling = this.isPlayerCrawling();
        if (!this.crawl.getValue() && isCrawling && !bl) {
            this.setToggled(false);
            return;
        }
        boolean bl2 = this.crawl.getValue() && isCrawling;
        boolean bl3 = this.isInWeb((Entity)PhaseModule.mc.player);
        boolean bl4 = this.isInLadder((Entity)PhaseModule.mc.player);
        if (!(bl2 || bl3 || bl4 || PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).canBeReplaced())) {
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
        boolean bl5 = multiTaskModule != null && multiTaskModule.isToggled() && multiTaskModule.pearl.getValue() && PhaseModule.mc.player.isUsingItem();
        String string = bl5 ? "AltSwap" : this.swap.getValue();
        int n4 = InventoryUtils.find(Items.ENDER_PEARL, 0, string.equalsIgnoreCase("AltSwap") || string.equalsIgnoreCase("AltPickup") ? 35 : 8);
        int n5 = PhaseModule.mc.player.getInventory().getSelectedSlot();
        boolean bl6 = false;
        if (n4 == -1) {
            Night.CHAT_MANAGER.tagged("No pearls could be found in your hotbar.", this.getName());
            this.setToggled(false);
            return;
        }
        float f = PhaseModule.mc.player.getYRot();
        float f2 = PhaseModule.mc.player.getXRot();
        BlockPos blockPos = PhaseModule.mc.player.blockPosition().below();
        float f3 = Mth.wrapDegrees((float)this.getPhaseYaw());
        float f4 = (f3 % 90.0f + 90.0f) % 90.0f;
        boolean bl7 = Math.min(f4, 90.0f - f4) < 22.5f;
        boolean bl8 = this.isOnBedrock();
        boolean bl9 = this.logic.getValue().equalsIgnoreCase("Grim");
        Vec3 vec3 = this.pipTarget(f3, isCrawling, bl7, bl8);
        if (this.antiVoid.getValue() && this.isVoidHazard(vec3, f3)) {
            Night.CHAT_MANAGER.tagged("Phase cancelled to prevent falling into the void.", this.getName());
            if (mc.getSoundManager() != null) {
                mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.NOTE_BLOCK_PLING, (float)1.5f));
            }
            this.setToggled(false);
            return;
        }
        float f5 = this.calcYaw(vec3);
        float f6 = this.solvePitch(vec3);
        if (this.debug.getValue()) {
            ++this.attempt;
            Night.CHAT_MANAGER.tagged(String.format("#%d [%s%s] pos=(%.4f, %.4f, %.4f) yaw=%.2f pitch=%.2f target=(%.4f, %.4f, %.4f) sector=%s", this.attempt, this.logic.getValue(), bl && bl8 ? "/Bedrock" : (bl && isCrawling ? "/Crawl" : (bl ? "/Standing" : "")), PhaseModule.mc.player.getX(), PhaseModule.mc.player.getY(), PhaseModule.mc.player.getZ(), Float.valueOf(f5), Float.valueOf(f6), vec3.x, vec3.y, vec3.z, bl7 ? "straight" : "corner"), this.getName());
        }
        if ((PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).isAir() || bl3 || bl4) && !PhaseModule.mc.level.getBlockState(blockPos).canBeReplaced()) {
            Item item = switch (this.ncpMode.getValue()) {
                case "FlintAndSteel" -> Items.FLINT_AND_STEEL;
                case "Cobweb" -> Items.COBWEB;
                default -> Items.FIRE_CHARGE;
            };
            int n6 = InventoryUtils.find(item, InventoryUtils.HOTBAR_START, this.bypassSwitch.getValue().equalsIgnoreCase("AltSwap") || this.bypassSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
            if (n6 != -1) {
                Night.ROTATION_MANAGER.packetRotate(f5, 90.0f, true);
                InventoryUtils.switchSlot(this.bypassSwitch.getValue(), n6, n5);
                NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 1.0, 0.0), Direction.UP, blockPos, false), n));
                mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                InventoryUtils.switchBack(this.bypassSwitch.getValue(), n6, n5);
                bl6 = true;
            }
        }
        Night.ROTATION_MANAGER.packetRotate(f5, f6, true);
        if (InventoryUtils.switchSlot(string, n4, n5)) {
            NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, n, f5, f6));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            InventoryUtils.switchBack(string, n4, n5);
        }
        if (bl6 && this.autoRemove.getValue()) {
            NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, PhaseModule.mc.player.blockPosition(), Direction.UP, n));
            NetworkUtils.sendSequencedPacket(n -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, PhaseModule.mc.player.blockPosition(), Direction.UP, n));
            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        this.setToggled(false);
    }

    private float getPhaseYaw() {
        ISprintModule iSprintModule;
        if (PhaseModule.mc.player == null) {
            return 0.0f;
        }
        float f = PhaseModule.mc.player.getYRot();
        if (Night.MODULE_MANAGER != null && (iSprintModule = (ISprintModule)((Object)Night.MODULE_MANAGER.getModule("Sprint"))) != null && iSprintModule.isGrimCompensating()) {
            return iSprintModule.getGrimYaw();
        }
        int n = (PhaseModule.mc.options.keyRight.isDown() ? 1 : 0) - (PhaseModule.mc.options.keyLeft.isDown() ? 1 : 0);
        int n2 = (PhaseModule.mc.options.keyUp.isDown() ? 1 : 0) - (PhaseModule.mc.options.keyDown.isDown() ? 1 : 0);
        if (n != 0 || n2 != 0) {
            float f2 = (float)Math.toDegrees(Math.atan2(n, n2));
            return Mth.wrapDegrees((float)(f + f2));
        }
        return f;
    }

    private boolean isOnBedrock() {
        if (PhaseModule.mc.player == null || PhaseModule.mc.level == null) {
            return false;
        }
        double d = PhaseModule.mc.player.getBoundingBox().minY;
        BlockPos blockPos = BlockPos.containing((double)PhaseModule.mc.player.getX(), (double)(d - 0.1), (double)PhaseModule.mc.player.getZ());
        if (PhaseModule.mc.level.getBlockState(blockPos).is(Blocks.BEDROCK)) {
            return true;
        }
        if (PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition().below()).is(Blocks.BEDROCK)) {
            return true;
        }
        return PhaseModule.mc.level.getBlockState(PhaseModule.mc.player.blockPosition()).is(Blocks.BEDROCK);
    }

    private Vec3 calculateKingMCStandingTarget(float f) {
        double d = PhaseModule.mc.player.getX();
        double d2 = PhaseModule.mc.player.getY();
        double d3 = PhaseModule.mc.player.getZ();
        double d4 = Math.toRadians(f);
        Vec3 vec3 = new Vec3(-Math.sin(d4), 0.0, Math.cos(d4)).normalize();
        float f2 = (f % 90.0f + 90.0f) % 90.0f;
        boolean bl = Math.min(f2, 90.0f - f2) < 22.5f;
        double d5 = 0.165;
        if (bl) {
            boolean bl2 = Math.abs(vec3.z) > Math.abs(vec3.x);
            boolean bl3 = bl2;
            if (bl2) {
                double d6 = vec3.z > 0.0 ? Math.floor(d3) + 1.0 : Math.floor(d3);
                double d7 = vec3.z > 0.0 ? d6 - 0.165 : d6 + 0.165;
                return new Vec3(d, d2, d7);
            }
            double d8 = vec3.x > 0.0 ? Math.floor(d) + 1.0 : Math.floor(d);
            double d9 = vec3.x > 0.0 ? d8 - 0.165 : d8 + 0.165;
            return new Vec3(d9, d2, d3);
        }
        double d10 = vec3.x > 0.0 ? Math.floor(d) + 1.0 : Math.floor(d);
        double d11 = vec3.z > 0.0 ? Math.floor(d3) + 1.0 : Math.floor(d3);
        double d12 = vec3.x > 0.0 ? d10 - 0.165 : d10 + 0.165;
        double d13 = vec3.z > 0.0 ? d11 - 0.165 : d11 + 0.165;
        return new Vec3(d12, d2, d13);
    }

    private Vec3 calculateKingMCCrawlTarget(float f) {
        double d;
        double d2;
        Vec3 vec3;
        float f2;
        double d3;
        Vec3 vec32;
        Vec3 vec33 = this.getEffectiveEyePos();
        BlockHitResult blockHitResult = PhaseModule.mc.level.clip(new ClipContext(vec33, vec33.add((vec32 = new Vec3(-Math.sin(d3 = Math.toRadians(f2 = PhaseModule.mc.player.getYRot())), 0.0, Math.cos(d3)).normalize()).scale(4.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PhaseModule.mc.player));
        if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
            f = f2;
            vec3 = vec32;
        } else {
            double d4 = Math.toRadians(f);
            vec3 = new Vec3(-Math.sin(d4), 0.0, Math.cos(d4)).normalize();
            blockHitResult = PhaseModule.mc.level.clip(new ClipContext(vec33, vec33.add(vec3.scale(4.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)PhaseModule.mc.player));
        }
        BlockPos blockPos = null;
        Direction direction = null;
        Vec3 vec34 = null;
        if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
            blockPos = blockHitResult.getBlockPos();
            direction = blockHitResult.getDirection();
            vec34 = blockHitResult.getLocation();
        } else {
            Direction dir = Direction.fromYRot((double)f);
            blockPos = PhaseModule.mc.player.blockPosition().relative(dir, 1);
            direction = dir.getOpposite();
        }
        double d5 = (double)blockPos.getY() + 0.52;
        float f3 = (f % 90.0f + 90.0f) % 90.0f;
        boolean bl = Math.min(f3, 90.0f - f3) < 22.5f;
        boolean bl2 = bl;
        if (bl && direction != null && direction.getAxis().isHorizontal()) {
            double d6;
            double d7;
            if (direction.getAxis() == Direction.Axis.Z) {
                d7 = direction == Direction.NORTH ? (double)blockPos.getZ() : (double)blockPos.getZ() + 1.0;
                double d8 = d7;
                d6 = vec34 != null && Math.abs(vec34.x - (double)Math.round(vec34.x)) < 0.25 ? (double)Math.round(vec34.x) : (double)blockPos.getX() + 0.5;
            } else {
                d6 = direction == Direction.WEST ? (double)blockPos.getX() : (double)blockPos.getX() + 1.0;
                d7 = vec34 != null && Math.abs(vec34.z - (double)Math.round(vec34.z)) < 0.25 ? (double)Math.round(vec34.z) : (double)blockPos.getZ() + 0.5;
            }
            return new Vec3(d6, d5, d7);
        }
        if (vec34 != null) {
            d2 = Math.round(vec34.x);
            d = Math.round(vec34.z);
        } else {
            d2 = Math.round(PhaseModule.mc.player.getX() + vec3.x);
            d = Math.round(PhaseModule.mc.player.getZ() + vec3.z);
        }
        return new Vec3(d2, d5, d);
    }

    private Vec3 calculateTargetPos() {
        double d = PhaseModule.mc.player.getX();
        double d2 = PhaseModule.mc.player.getZ();
        double d3 = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double d4 = Math.round(d);
        double d5 = Math.round(d2);
        double d6 = d4 - d;
        double d7 = d5 - d2;
        if (Math.abs(d6) <= 0.5 && Math.abs(d7) <= 0.5) {
            return new Vec3(d + Mth.clamp((double)d6, (double)-0.5, (double)0.5), d3, d2 + Mth.clamp((double)d7, (double)-0.5, (double)0.5));
        }
        double d8 = 0.241660973353061;
        double d9 = 0.7853981633974483;
        double d10 = d + Mth.clamp((double)(this.toClosest(d, Math.floor(d) + 0.241660973353061, Math.floor(d) + 0.7853981633974483) - d), (double)-0.2, (double)0.2);
        double d11 = d2 + Mth.clamp((double)(this.toClosest(d2, Math.floor(d2) + 0.241660973353061, Math.floor(d2) + 0.7853981633974483) - d2), (double)-0.2, (double)0.2);
        return new Vec3(d10, d3, d11);
    }

    private double toClosest(double d, double d2, double d3) {
        return d - d2 > d3 - d ? d3 : d2;
    }

    private Vec3 calculateGrimSectorTarget() {
        double d;
        double d2 = PhaseModule.mc.player.getX();
        double d3 = PhaseModule.mc.player.getZ();
        double d4 = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double d5 = Math.floor(d2);
        double d6 = Math.floor(d3);
        double d7 = d6 + 0.5;
        double d8 = Math.toDegrees(Math.atan2(d3 - d7, d2 - (d = d5 + 0.5)));
        if (d8 < 0.0) {
            d8 += 360.0;
        }
        int n = (int)Math.floor((d8 + 22.5) / 45.0) & 7;
        double d9 = d5 + 1.0;
        double d10 = d6 + 1.0;
        return switch (n) {
            case 0 -> new Vec3(d9, d4, d7);
            case 1 -> new Vec3(d9, d4, d10);
            case 2 -> new Vec3(d, d4, d10);
            case 3 -> new Vec3(d5, d4, d10);
            case 4 -> new Vec3(d5, d4, d7);
            case 5 -> new Vec3(d5, d4, d6);
            case 6 -> new Vec3(d, d4, d6);
            default -> new Vec3(d9, d4, d6);
        };
    }

    private Vec3 boundaryTarget(float f) {
        double d = PhaseModule.mc.player.getX();
        double d2 = PhaseModule.mc.player.getZ();
        double d3 = this.crawl.getValue() && this.isPlayerCrawling() ? (double)PhaseModule.mc.player.blockPosition().below().getY() : PhaseModule.mc.player.getY() - 0.5;
        double d4 = Math.toRadians(f);
        Vec3 vec3 = new Vec3(-Math.sin(d4), 0.0, Math.cos(d4)).normalize();
        boolean bl = Math.abs(vec3.z) > Math.abs(vec3.x);
        boolean bl2 = bl;
        if (bl) {
            double d5 = vec3.z > 0.0 ? Math.floor(d2) + 1.0 : Math.floor(d2);
            return new Vec3(d, d3, d5);
        }
        double d6 = vec3.x > 0.0 ? Math.floor(d) + 1.0 : Math.floor(d);
        return new Vec3(d6, d3, d2);
    }

    private Vec3 getEffectiveEyePos() {
        if (PhaseModule.mc.player == null) {
            return Vec3.ZERO;
        }
        double d = PhaseModule.mc.player.getEyeY() - 0.1;
        return new Vec3(PhaseModule.mc.player.getX(), d, PhaseModule.mc.player.getZ());
    }

    private float calcYaw(Vec3 vec3) {
        Vec3 vec32 = this.getEffectiveEyePos();
        Vec3 vec33 = vec3.subtract(vec32);
        return (float)Math.toDegrees(Math.atan2(-vec33.x, vec33.z));
    }

    private float solvePitch(Vec3 vec3) {
        Vec3 vec32 = this.getEffectiveEyePos();
        double d = Math.hypot(vec3.x - vec32.x, vec3.z - vec32.z);
        double d2 = vec3.y - vec32.y;
        if (d < 1.0E-4) {
            return d2 < 0.0 ? 85.0f : -85.0f;
        }
        double d3 = -85.0;
        double d4 = 89.9;
        for (int i = 0; i < 30; ++i) {
            double d5 = (d3 + d4) * 0.5;
            double d6 = PhaseModule.simulatePearlYAtDist(d5, d);
            if (d6 < d2) {
                d4 = d5;
                continue;
            }
            d3 = d5;
        }
        return (float)((d3 + d4) * 0.5);
    }

    private static double simulatePearlYAtDist(double d, double d2) {
        double d3 = Math.toRadians(d);
        double d4 = 1.5 * Math.cos(d3);
        double d5 = -1.5 * Math.sin(d3);
        double d6 = 0.0;
        double d7 = 0.0;
        for (int i = 0; i < 100; ++i) {
            double d8 = d6;
            double d9 = d7;
            d6 += d4;
            d7 += d5;
            if (d6 >= d2) {
                double d10 = (d2 - d8) / (d6 - d8);
                return d9 + (d7 - d9) * d10;
            }
            d4 *= 0.99;
            d5 = d5 * 0.99 - 0.03;
        }
        return d7;
    }

    private boolean isFloorVoid(double d, double d2, double d3) {
        if (PhaseModule.mc.level == null) {
            return false;
        }
        int n = PhaseModule.mc.level.getMinY();
        int n2 = (int)Math.floor(d2);
        if (n2 < n) {
            return true;
        }
        for (int i = n2; i >= n; --i) {
            BlockPos blockPos = BlockPos.containing((double)d, (double)i, (double)d3);
            BlockState blockState = PhaseModule.mc.level.getBlockState(blockPos);
            if (blockState.isAir() || blockState.getCollisionShape((BlockGetter)PhaseModule.mc.level, blockPos).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean isVoidHazard(Vec3 vec3, float f) {
        if (PhaseModule.mc.player == null || PhaseModule.mc.level == null) {
            return false;
        }
        double d = PhaseModule.mc.player.getX();
        double d2 = PhaseModule.mc.player.getY();
        double d3 = PhaseModule.mc.player.getZ();
        int n = PhaseModule.mc.level.getMinY();
        if (d2 < (double)n) {
            return true;
        }
        double d4 = Math.toRadians(f);
        Vec3 vec32 = new Vec3(-Math.sin(d4), 0.0, Math.cos(d4)).normalize();
        BlockPos blockPos = BlockPos.containing((double)vec3.x, (double)vec3.y, (double)vec3.z);
        BlockPos blockPos2 = BlockPos.containing((double)(d + vec32.x * 0.5), (double)d2, (double)(d3 + vec32.z * 0.5));
        boolean bl = !this.isFloorVoid((double)blockPos.getX() + 0.5, vec3.y, (double)blockPos.getZ() + 0.5);
        boolean bl2 = !this.isFloorVoid((double)blockPos2.getX() + 0.5, d2, (double)blockPos2.getZ() + 0.5);
        return !bl && !bl2;
    }

    private Vec3 pipTarget(float f, boolean bl, boolean bl2, boolean bl3) {
        Vec3 vec3 = !bl3 ? (bl ? this.calculateKingMCCrawlTarget(f) : this.calculateKingMCStandingTarget(f)) : (bl2 ? this.boundaryTarget(f) : this.calculateTargetPos());
        if (this.antiVoid.getValue() && this.isVoidHazard(vec3, f)) {
            Vec3 vec32 = this.calculateGrimSectorTarget();
            if (!this.isVoidHazard(vec32, f)) {
                return vec32;
            }
            Vec3 vec33 = bl2 ? this.boundaryTarget(f) : this.calculateTargetPos();
            if (!this.isVoidHazard(vec33, f)) {
                return vec33;
            }
        }
        return vec3;
    }
}

