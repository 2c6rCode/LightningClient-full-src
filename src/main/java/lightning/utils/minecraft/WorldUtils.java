/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  lightning.mixins.accessors.ClientPlayerEntityAccessor
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectUtil
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.entity.projectile.arrow.AbstractArrow
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.item.ItemInstance
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.AbstractFurnaceBlock
 *  net.minecraft.world.level.block.AnvilBlock
 *  net.minecraft.world.level.block.BarrelBlock
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.CrafterBlock
 *  net.minecraft.world.level.block.CraftingTableBlock
 *  net.minecraft.world.level.block.DoorBlock
 *  net.minecraft.world.level.block.EnderChestBlock
 *  net.minecraft.world.level.block.FenceGateBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.RespawnAnchorBlock
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.level.block.SignBlock
 *  net.minecraft.world.level.block.TrapDoorBlock
 *  net.minecraft.world.level.block.WallSignBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.utils.minecraft;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lightning.Night;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.impl.combat.CrystalPlacementHelper;
import lightning.modules.impl.core.RotationsModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CBlockRenderPacket;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WorldUtils
implements IMinecraft {
    public static Set<Block> RIGHT_CLICKABLE_BLOCKS = Sets.newHashSet(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.SHULKER_BOX, Blocks.BARREL, Blocks.RESPAWN_ANCHOR, Blocks.CRAFTER, (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.WHITE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.ORANGE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.MAGENTA), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_BLUE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.YELLOW), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIME), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.PINK), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.GRAY), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_GRAY), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.CYAN), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.PURPLE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BLUE), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BROWN), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.GREEN), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.RED), (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BLACK), Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.BELL, Blocks.OAK_BUTTON, Blocks.ACACIA_BUTTON, Blocks.BIRCH_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.STONE_BUTTON, Blocks.COMPARATOR, Blocks.REPEATER, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.BREWING_STAND, Blocks.DISPENSER, Blocks.DROPPER, Blocks.LEVER, Blocks.NOTE_BLOCK, Blocks.JUKEBOX, Blocks.BEACON, (Block)Blocks.BED.pick(DyeColor.BLACK), (Block)Blocks.BED.pick(DyeColor.BLUE), (Block)Blocks.BED.pick(DyeColor.BROWN), (Block)Blocks.BED.pick(DyeColor.CYAN), (Block)Blocks.BED.pick(DyeColor.GRAY), (Block)Blocks.BED.pick(DyeColor.GREEN), (Block)Blocks.BED.pick(DyeColor.LIGHT_BLUE), (Block)Blocks.BED.pick(DyeColor.LIGHT_GRAY), (Block)Blocks.BED.pick(DyeColor.LIME), (Block)Blocks.BED.pick(DyeColor.MAGENTA), (Block)Blocks.BED.pick(DyeColor.ORANGE), (Block)Blocks.BED.pick(DyeColor.PINK), (Block)Blocks.BED.pick(DyeColor.PURPLE), (Block)Blocks.BED.pick(DyeColor.RED), (Block)Blocks.BED.pick(DyeColor.WHITE), (Block)Blocks.BED.pick(DyeColor.YELLOW), Blocks.FURNACE, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.CAKE, Blocks.ENCHANTING_TABLE, Blocks.DRAGON_EGG, Blocks.HOPPER, Blocks.REPEATING_COMMAND_BLOCK, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.CRAFTING_TABLE, Blocks.ACACIA_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.CAKE, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.BLAST_FURNACE, Blocks.SMOKER, Blocks.CARTOGRAPHY_TABLE, Blocks.GRINDSTONE, Blocks.LECTERN, Blocks.LOOM, Blocks.STONECUTTER, Blocks.SMITHING_TABLE});
    private static final ItemStack NETHERITE_PICKAXE = new ItemStack((ItemLike)Items.NETHERITE_PICKAXE);
    private static final Map<BlockPos, Long> pendingAirPlaces = new ConcurrentHashMap<BlockPos, Long>();
    private static final Map<BlockPos, PredictedPlacement> predictedPlacements = new ConcurrentHashMap<BlockPos, PredictedPlacement>();
    private static final long PREDICTION_TIMEOUT_MS = 1000L;
    public static volatile long lastInteractablePlaceTime = 0L;
    private static final String[] FACING_NAMES = new String[]{"South", "South West", "West", "North West", "North", "North East", "East", "South East"};
    private static final String[] FACING_AXES = new String[]{"+Z", "-X +Z", "-X", "-X -Z", "-Z", "+X -Z", "+X", "+X +Z"};

    public static boolean isInteractable(BlockState blockState) {
        if (blockState == null || blockState.isAir()) {
            return false;
        }
        Block block = blockState.getBlock();
        if (RIGHT_CLICKABLE_BLOCKS.contains(block)) {
            return true;
        }
        return block instanceof BaseEntityBlock || block instanceof ShulkerBoxBlock || block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof AnvilBlock || block instanceof CraftingTableBlock || block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock || block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof AbstractFurnaceBlock || block instanceof BedBlock || block instanceof SignBlock || block instanceof WallSignBlock || block instanceof CrafterBlock || block instanceof RespawnAnchorBlock || block instanceof BarrelBlock;
    }

    public static boolean placeBlock(BlockPos blockPos, Direction direction, InteractionHand interactionHand, boolean bl, boolean bl2) {
        return WorldUtils.placeBlock(blockPos, direction, interactionHand, bl ? "Normal" : "None", bl2, false);
    }

    public static boolean placeBlock(BlockPos blockPos, Direction direction, InteractionHand interactionHand, boolean bl, boolean bl2, boolean bl3) {
        return WorldUtils.placeBlock(blockPos, direction, interactionHand, null, bl ? "Normal" : "None", bl2, bl3);
    }

    public static boolean placeBlock(BlockPos blockPos, Direction direction, InteractionHand interactionHand, Runnable runnable, boolean bl, boolean bl2, boolean bl3) {
        return WorldUtils.placeBlock(blockPos, direction, interactionHand, runnable, bl ? "Normal" : "None", bl2, bl3);
    }

    public static boolean placeBlock(BlockPos blockPos, Direction direction, InteractionHand interactionHand, String string, boolean bl) {
        return WorldUtils.placeBlock(blockPos, direction, interactionHand, string, bl, false);
    }

    public static boolean placeBlock(BlockPos blockPos, Direction direction, InteractionHand interactionHand, String string, boolean bl, boolean bl2) {
        return WorldUtils.placeBlock(blockPos, direction, interactionHand, null, string, bl, bl2);
    }

    public static void predictPlacement(BlockPos blockPos, Block block) {
        if (blockPos == null || block == null) {
            return;
        }
        long l = System.currentTimeMillis();
        if (predictedPlacements.size() > 256) {
            predictedPlacements.entrySet().removeIf(entry -> l - ((PredictedPlacement)entry.getValue()).time() >= 1000L);
        }
        predictedPlacements.put(blockPos.immutable(), new PredictedPlacement(block, l));
    }

    public static void clearPredictedPlacement(BlockPos blockPos) {
        if (blockPos != null) {
            predictedPlacements.remove(blockPos);
        }
    }

    public static void clearPredictedPlacements() {
        predictedPlacements.clear();
    }

    public static BlockState getPredictedCollisionState(BlockPos blockPos) {
        if (predictedPlacements.isEmpty()) {
            return null;
        }
        PredictedPlacement predictedPlacement = predictedPlacements.get(blockPos);
        if (predictedPlacement == null) {
            return null;
        }
        if (System.currentTimeMillis() - predictedPlacement.time() >= 1000L) {
            predictedPlacements.remove(blockPos);
            return null;
        }
        return predictedPlacement.block().defaultBlockState();
    }

    public static void airPlaceBlock(BlockPos blockPos, InteractionHand interactionHand, String string, boolean bl) {
        RenderPosition renderPosition;
        if (blockPos == null || WorldUtils.mc.player == null || WorldUtils.mc.level == null || mc.getConnection() == null) {
            return;
        }
        long l = System.currentTimeMillis();
        long l2 = Math.max(50L, (long)Night.SERVER_MANAGER.getPing() * 2L);
        Long l3 = pendingAirPlaces.get(blockPos);
        if (l3 != null) {
            if (l - l3 < l2) {
                return;
            }
            pendingAirPlaces.remove(blockPos);
        }
        if (pendingAirPlaces.size() > 256) {
            pendingAirPlaces.entrySet().removeIf(entry -> l - (Long)entry.getValue() >= l2);
        }
        pendingAirPlaces.put(blockPos, l);
        Vec3 vec3 = Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.5, 0.0);
        boolean bl2 = string != null && !string.equalsIgnoreCase("None") && !string.equalsIgnoreCase("false");
        boolean bl3 = bl2;
        if (bl2) {
            float[] rotations = RotationUtils.getRotations(vec3);
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(rotations[0], rotations[1]);
            } else {
                Night.ROTATION_MANAGER.silentRotate(rotations[0], rotations[1]);
            }
        }
        if (bl && !Night.RENDER_MANAGER.renderPositions.contains(renderPosition = new RenderPosition(blockPos))) {
            Night.RENDER_MANAGER.renderPositions.add(renderPosition);
        }
        BlockHitResult blockHitResult = new BlockHitResult(vec3, Direction.UP, blockPos, false);
        NetworkUtils.sendSequencedPacket(arg_0 -> WorldUtils.createAirPlacePacket(interactionHand, blockHitResult, arg_0));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(interactionHand));
    }

    public static boolean placeBlock(BlockPos blockPos, Direction direction, InteractionHand interactionHand, Runnable runnable, String string, boolean bl, boolean bl2) {
        RenderPosition renderPosition;
        if (blockPos == null || direction == null) {
            return false;
        }
        BlockPos blockPos2 = blockPos.relative(direction);
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null) {
            return false;
        }
        boolean bl3 = WorldUtils.isInteractable(WorldUtils.mc.level.getBlockState(blockPos2)) || WorldUtils.isInteractable(WorldUtils.mc.level.getBlockState(blockPos));
        boolean bl4 = bl3;
        if (bl3) {
            lastInteractablePlaceTime = System.currentTimeMillis();
        }
        Vec3 vec3 = Vec3.atCenterOf((Vec3i)blockPos2).add((double)(-direction.getStepX()) * 0.45, (double)(-direction.getStepY()) * 0.45, (double)(-direction.getStepZ()) * 0.45);
        Night.WORLD_MANAGER.reservePlacement(blockPos);
        float f = Night.ROTATION_MANAGER.getServerYaw();
        float f2 = Night.ROTATION_MANAGER.getServerPitch();
        float f3 = (float)RotationUtils.getYRotToVec((Entity)WorldUtils.mc.player, vec3);
        float f4 = (float)RotationUtils.getXRotToVec((Entity)WorldUtils.mc.player, vec3);
        boolean bl5 = string != null && !string.equalsIgnoreCase("None") && !string.equalsIgnoreCase("false");
        boolean bl6 = bl5;
        if (bl5) {
            if (Night.ROTATION_MANAGER.isBatchingRotations()) {
                Night.ROTATION_MANAGER.batchRotate(f3, f4);
            } else {
                Night.ROTATION_MANAGER.silentRotate(f3, f4);
            }
        }
        if (bl) {
            Direction direction2 = direction;
            Runnable retryRunnable = () -> WorldUtils.placeBlock(blockPos, direction2, interactionHand, runnable, string, bl, bl2);
            if (WorldUtils.destroyCrystals(blockPos, WorldUtils.mc.player.getEyePosition(), vec3, retryRunnable, string)) {
                return false;
            }
        }
        if (runnable != null) {
            runnable.run();
        }
        boolean bl7 = (WorldUtils.isInteractable(WorldUtils.mc.level.getBlockState(blockPos2)) || WorldUtils.isInteractable(WorldUtils.mc.level.getBlockState(blockPos))) && !WorldUtils.mc.player.isShiftKeyDown();
        boolean bl8 = bl7;
        if (bl7) {
            Input currentInput = WorldUtils.mc.player.input != null ? WorldUtils.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
            Input sneakingInput = new Input(currentInput.forward(), currentInput.backward(), currentInput.left(), currentInput.right(), currentInput.jump(), true, currentInput.sprint());
            mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(sneakingInput));
            LocalPlayer localPlayer = WorldUtils.mc.player;
            if (localPlayer instanceof ClientPlayerEntityAccessor accessor) {
                accessor.setLastSentInput(sneakingInput);
            }
        }
        BlockHitResult blockHitResult = new BlockHitResult(vec3, direction.getOpposite(), blockPos2, false);
        NetworkUtils.sendSequencedPacket(arg_0 -> WorldUtils.createPlacePacket(interactionHand, blockHitResult, arg_0));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(interactionHand));
        if (bl5 && !Night.ROTATION_MANAGER.isBatchingRotations() && Night.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) {
            Night.ROTATION_MANAGER.packetRotate(f, f2);
        }
        Night.WORLD_MANAGER.getPlaceTimer().reset();
        if (bl7) {
            Input restoredInput = WorldUtils.mc.player.input != null ? WorldUtils.mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
            mc.getConnection().send((Packet)new ServerboundPlayerInputPacket(restoredInput));
            LocalPlayer localPlayer = WorldUtils.mc.player;
            if (localPlayer instanceof ClientPlayerEntityAccessor accessor) {
                accessor.setLastSentInput(restoredInput);
            }
        }
        if (bl2 && !Night.RENDER_MANAGER.renderPositions.contains(renderPosition = new RenderPosition(blockPos))) {
            Night.RENDER_MANAGER.renderPositions.add(renderPosition);
            if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && Night.PROXY_SERVER != null) {
                ClientboundCustomPayloadPacket payloadPacket = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CBlockRenderPacket(blockPos)));
                for (Connection connection : Night.PROXY_SERVER.getConnections()) {
                    if (!connection.isConnected()) continue;
                    connection.send((Packet)payloadPacket);
                }
            }
        }
        return true;
    }

    public static boolean isPlaceable(BlockPos blockPos) {
        return WorldUtils.isPlaceable(blockPos, false);
    }

    public static boolean isPlaceable(BlockPos blockPos, boolean bl) {
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null) {
            return false;
        }
        if (WorldUtils.mc.level.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        if (!WorldUtils.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        boolean bl2 = (double)blockPos.getY() < Math.floor(WorldUtils.mc.player.getBoundingBox().minY + 0.01);
        boolean bl3 = new AABB(blockPos).contains(WorldUtils.mc.player.position().x, WorldUtils.mc.player.getBoundingBox().minY + 0.1, WorldUtils.mc.player.position().z);
        boolean bl4 = !bl2 && blockPos.getY() == (int)Math.floor(WorldUtils.mc.player.getBoundingBox().minY);
        double d = bl4 ? 0.05 : 0.25;
        boolean bl5 = !bl && !bl2 && (bl3 || !EntityUtils.isPhased((Entity)WorldUtils.mc.player) && WorldUtils.mc.player.getBoundingBox().deflate(d).intersects(new AABB(blockPos)));
        boolean bl6 = bl5;
        if (bl5) {
            return false;
        }
        return WorldUtils.mc.level.getEntities((Entity)null, new AABB(blockPos), entity -> true).stream().noneMatch(entity -> !(entity instanceof EndCrystal) && !(entity instanceof ExperienceOrb) && !(entity instanceof ItemEntity) && !(entity instanceof AbstractArrow) && (!entity.equals((Object)WorldUtils.mc.player) || !bl && !bl2 && bl5) && !EntityUtils.isGhost(entity));
    }

    public static boolean isCrystalPlaceable(BlockPos blockPos) {
        if (WorldUtils.mc.level == null) {
            return false;
        }
        if (!WorldUtils.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        return WorldUtils.mc.level.getEntities((Entity)null, new AABB(blockPos), entity -> true).stream().noneMatch(entity -> !(entity instanceof EndCrystal) && !(entity instanceof ExperienceOrb) && !(entity instanceof ItemEntity) && !(entity instanceof AbstractArrow) && !EntityUtils.isGhost(entity));
    }

    public static boolean isInstantBreakable(BlockPos blockPos) {
        if (WorldUtils.mc.level == null) {
            return false;
        }
        BlockState blockState = WorldUtils.mc.level.getBlockState(blockPos);
        if (blockState.isAir() || blockState.canBeReplaced()) {
            return false;
        }
        return blockState.getDestroySpeed((BlockGetter)WorldUtils.mc.level, blockPos) == 0.0f;
    }

    public static void instantBreak(BlockPos blockPos, Direction direction) {
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null) {
            return;
        }
        mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, direction != null ? direction : Direction.UP));
        mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction != null ? direction : Direction.UP));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }

    public static Direction getDirection(BlockPos blockPos) {
        return WorldUtils.getDirection(blockPos, null, false);
    }

    public static Direction getDirection(BlockPos blockPos, boolean bl) {
        return WorldUtils.getDirection(blockPos, null, bl);
    }

    public static BlockPos findSupportBlock(BlockPos blockPos, List<BlockPos> list, boolean bl, int n) {
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null || blockPos == null) {
            return null;
        }
        for (int i = 1; i <= n; ++i) {
            for (Direction direction : Direction.values()) {
                Direction direction2;
                BlockPos blockPos2 = blockPos.relative(direction, i);
                if (!WorldUtils.mc.player.isWithinBlockInteractionRange(blockPos2, 0.0) || !WorldUtils.isPlaceable(blockPos2, false) || (direction2 = WorldUtils.getDirection(blockPos2, list, bl)) == null) continue;
                return blockPos2;
            }
        }
        return null;
    }

    private static boolean isVisibleBlock(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3) {
        if (blockPos == null) {
            return false;
        }
        return blockPos.equals((Object)blockPos2) || blockPos.equals((Object)blockPos3);
    }

    public static List<Direction> getDirections(BlockPos blockPos, List<BlockPos> list, boolean bl) {
        if (WorldUtils.mc.level == null || WorldUtils.mc.player == null) {
            return Collections.emptyList();
        }
        Vec3 vec3 = WorldUtils.mc.player.getEyePosition();
        ArrayList<DirectionCandidate> arrayList = new ArrayList<DirectionCandidate>();
        ArrayList arrayList2 = new ArrayList();
        for (Direction direction : Direction.values()) {
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState = WorldUtils.mc.level.getBlockState(blockPos2);
            if (blockState.canBeReplaced() && (list == null || !list.contains(blockPos2)) || !blockState.getFluidState().isEmpty() || !WorldUtils.mc.player.isWithinBlockInteractionRange(blockPos2, 0.0)) continue;
            Vec3 vec32 = Vec3.atCenterOf((Vec3i)blockPos).relative(direction, 0.5);
            if (bl) {
                List<Direction> list2;
                if (direction == Direction.DOWN ? vec3.y < (double)blockPos2.getY() + 1.0 : (direction == Direction.UP ? vec3.y > (double)blockPos2.getY() : !(list2 = WorldUtils.getStrictDirections(vec3, Vec3.atCenterOf((Vec3i)blockPos2))).contains(direction.getOpposite()))) continue;
                Direction direction2 = direction.getOpposite();
                int[] nArray = new int[]{direction2.getStepX(), direction2.getStepY(), direction2.getStepZ()};
                double[][] dArrayArray = new double[][]{{0.5, 0.5}, {0.25, 0.5}, {0.75, 0.5}, {0.5, 0.25}, {0.5, 0.75}};
                boolean bl2 = false;
                for (double[] dArray : dArrayArray) {
                    BlockHitResult blockHitResult;
                    double[] dArray2 = new double[]{blockPos2.getX(), blockPos2.getY(), blockPos2.getZ()};
                    int n = 0;
                    for (int i = 0; i < 3; ++i) {
                        int n2 = i;
                        dArray2[n2] = dArray2[n2] + (nArray[i] != 0 ? 0.5 + (double)nArray[i] * 0.48 : dArray[n++]);
                    }
                    BlockHitResult blockHitResult2 = WorldUtils.mc.level.clip(new ClipContext(vec3, new Vec3(dArray2[0], dArray2[1], dArray2[2]), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
                    if (blockHitResult2.getType() != HitResult.Type.BLOCK || !(blockHitResult2 instanceof BlockHitResult) || !(blockHitResult = blockHitResult2).getBlockPos().equals((Object)blockPos2) || blockHitResult.getDirection() != direction2) continue;
                    bl2 = true;
                    break;
                }
                if (!bl2) continue;
            }
            double d = vec3.distanceToSqr(vec32);
            if (RIGHT_CLICKABLE_BLOCKS.contains(blockState.getBlock())) {
                d += 20.0;
            }
            if (direction == Direction.DOWN) {
                d -= 50.0;
            }
            arrayList.add(new DirectionCandidate(direction, d));
        }
        arrayList.sort(Comparator.comparingDouble(directionCandidate -> directionCandidate.distSq));
        ArrayList arrayList3 = new ArrayList();
        for (DirectionCandidate directionCandidate2 : arrayList) {
            arrayList3.add(directionCandidate2.direction);
        }
        return arrayList3;
    }

    public static Direction getDirection(BlockPos blockPos, List<BlockPos> list, boolean bl) {
        List<Direction> list2 = WorldUtils.getDirections(blockPos, list, bl);
        return list2.isEmpty() ? null : list2.get(0);
    }

    public static boolean destroyCrystals(BlockPos blockPos) {
        return WorldUtils.destroyCrystals(blockPos, null, null, null, "None");
    }

    public static boolean destroyCrystals(BlockPos blockPos, Vec3 vec3, Vec3 vec32, Runnable runnable, String string) {
        List<Entity> list;
        AABB aABB = vec3 != null && vec32 != null ? new AABB(vec3, vec32).inflate(0.5) : new AABB(blockPos);
        try {
            list = WorldUtils.mc.level.getEntities((Entity)null, aABB, entity -> entity instanceof EndCrystal && (vec3 == null || vec32 == null || new AABB(blockPos).intersects(entity.getBoundingBox()) || entity.getBoundingBox().clip(vec3, vec32).isPresent()));
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            return false;
        }
        if (list.isEmpty()) {
            return false;
        }
        boolean bl = string != null && !string.equalsIgnoreCase("None") && !string.equalsIgnoreCase("false");
        for (Entity entity2 : list) {
            if (!entity2.isAlive() || entity2.isRemoved() || WorldUtils.mc.player.getEyePosition().distanceToSqr(entity2.position()) > 36.0) continue;
            if (bl) {
                float[] fArray = RotationUtils.getRotations(entity2.getBoundingBox().getCenter());
                Night.ROTATION_MANAGER.silentRotate(fArray[0], fArray[1]);
            }
            WorldUtils.mc.player.connection.send((Packet)new ServerboundAttackPacket(entity2.getId()));
            WorldUtils.mc.player.connection.send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            if (runnable == null) continue;
            Night.WORLD_MANAGER.onCrystalAttacked(entity2.getId(), runnable);
        }
        return true;
    }

    public static Vec3 getHitVector(BlockPos blockPos, Direction direction) {
        return Vec3.atCenterOf((Vec3i)blockPos).add((double)direction.getStepX() / 2.0, (double)direction.getStepY() / 2.0, (double)direction.getStepZ() / 2.0);
    }

    public static Direction getClosestDirection(BlockPos blockPos, boolean bl) {
        if (bl) {
            if (WorldUtils.mc.player.getY() >= (double)blockPos.getY()) {
                return Direction.UP;
            }
            BlockHitResult blockHitResult = WorldUtils.mc.level.clip(new ClipContext(WorldUtils.mc.player.getEyePosition(), Vec3.atCenterOf((Vec3i)blockPos), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
            if (blockHitResult == null || blockHitResult.getType() != HitResult.Type.BLOCK || blockHitResult.getDirection() == null) {
                return WorldUtils.getClosestDirection(blockPos);
            }
            return blockHitResult.getDirection();
        }
        return WorldUtils.getClosestDirection(blockPos);
    }

    private static Direction getClosestDirection(BlockPos blockPos) {
        Direction direction = null;
        Vec3 vec3 = null;
        for (Direction direction2 : Direction.values()) {
            Vec3 vec32 = WorldUtils.getHitVector(blockPos, direction2);
            if (direction == null) {
                direction = direction2;
                vec3 = vec32;
                continue;
            }
            if (!(WorldUtils.mc.player.distanceToSqr(vec32) < WorldUtils.mc.player.distanceToSqr(vec3))) continue;
            direction = direction2;
            vec3 = vec32;
        }
        return direction;
    }

    public static List<Direction> getStrictDirections(Vec3 vec3, Vec3 vec32) {
        ArrayList<Direction> arrayList = new ArrayList<Direction>();
        double d = vec3.x - vec32.x;
        double d2 = vec3.y - vec32.y;
        double d3 = vec3.z - vec32.z;
        if (d2 > 0.5) {
            arrayList.add(Direction.UP);
        } else if (d2 < -0.5) {
            arrayList.add(Direction.DOWN);
        }
        if (d > 0.5) {
            arrayList.add(Direction.EAST);
        } else if (d < -0.5) {
            arrayList.add(Direction.WEST);
        }
        if (d3 > 0.5) {
            arrayList.add(Direction.SOUTH);
        } else if (d3 < -0.5) {
            arrayList.add(Direction.NORTH);
        }
        return arrayList;
    }

    public static HitResult getRaytraceTarget(float f, float f2, double d, double d2, double d3) {
        AABB aABB;
        Vec3 vec3;
        EntityHitResult entityHitResult;
        Vec3 vec32 = new Vec3((double)(Mth.sin((double)(-f * ((float)Math.PI / 180))) * Mth.cos((double)(f2 * ((float)Math.PI / 180)))), (double)(-Mth.sin((double)(f2 * ((float)Math.PI / 180)))), (double)(Mth.cos((double)(-f * ((float)Math.PI / 180))) * Mth.cos((double)(f2 * ((float)Math.PI / 180)))));
        BlockHitResult blockHitResult = WorldUtils.mc.level.clip(new ClipContext(new Vec3(d, d2, d3), new Vec3(d + vec32.x * 5.0, d2 + vec32.y * 5.0, d3 + vec32.z * 5.0), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
        Vec3 vec33 = new Vec3(d, d2 + (double)WorldUtils.mc.player.getEyeHeight(WorldUtils.mc.player.getPose()), d3);
        double d4 = 25.0;
        if (blockHitResult != null) {
            d4 = blockHitResult.getLocation().distanceToSqr(vec33);
        }
        if ((entityHitResult = ProjectileUtil.getEntityHitResult((Entity)WorldUtils.mc.player, (Vec3)vec33, (Vec3)(vec3 = vec33.add(vec32.x * 5.0, vec32.y * 5.0, vec32.z * 5.0)), (AABB)(aABB = new AABB(d - 0.3, d2, d3 - 0.3, d + 0.3, d2 + 1.8, d3 + 0.3).expandTowards(vec32.scale(5.0)).inflate(1.0, 1.0, 1.0)), entity -> !entity.isSpectator() && entity.isPickable(), (double)d4)) != null && (vec33.distanceToSqr(entityHitResult.getLocation()) < d4 || blockHitResult == null) && entityHitResult.getEntity() instanceof LivingEntity) {
            return entityHitResult;
        }
        return blockHitResult;
    }

    public static boolean canSee(Entity entity) {
        if (entity == null || WorldUtils.mc.player == null || WorldUtils.mc.level == null) {
            return false;
        }
        AABB aABB = entity.getBoundingBox();
        Vec3 vec3 = new Vec3(WorldUtils.mc.player.getX(), WorldUtils.mc.player.getEyeY(), WorldUtils.mc.player.getZ());
        if (WorldUtils.canSee(vec3, aABB.getCenter())) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(aABB.getCenter().x, aABB.maxY - 0.1, aABB.getCenter().z))) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(aABB.getCenter().x, aABB.minY + 0.1, aABB.getCenter().z))) {
            return true;
        }
        double d = aABB.minX + 0.1;
        double d2 = aABB.maxX - 0.1;
        double d3 = aABB.minY + 0.1;
        double d4 = aABB.maxY - 0.1;
        double d5 = aABB.minZ + 0.1;
        double d6 = aABB.maxZ - 0.1;
        if (WorldUtils.canSee(vec3, new Vec3(d, d4, d5))) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(d2, d4, d5))) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(d, d4, d6))) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(d2, d4, d6))) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(d, d3, d5))) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(d2, d3, d5))) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(d, d3, d6))) {
            return true;
        }
        if (WorldUtils.canSee(vec3, new Vec3(d2, d3, d6))) {
            return true;
        }
        return WorldUtils.canSeeBlock(BlockPos.containing((double)aABB.getCenter().x, (double)(aABB.minY - 0.5), (double)aABB.getCenter().z));
    }

    public static boolean canSee(Vec3 vec3, Vec3 vec32) {
        return WorldUtils.mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean canSee(BlockPos blockPos) {
        return WorldUtils.canSee((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
    }

    public static boolean canSee(Vec3 vec3) {
        return WorldUtils.canSee(vec3.x, vec3.y, vec3.z);
    }

    public static boolean canSee(double d, double d2, double d3) {
        return WorldUtils.mc.level.clip(new ClipContext(new Vec3(WorldUtils.mc.player.getX(), WorldUtils.mc.player.getEyeY(), WorldUtils.mc.player.getZ()), new Vec3(d, d2, d3), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean canSeeBlock(BlockPos blockPos) {
        BlockHitResult blockHitResult;
        BlockHitResult blockHitResult2;
        if (WorldUtils.mc.player == null || WorldUtils.mc.level == null) {
            return false;
        }
        Vec3 vec3 = new Vec3(WorldUtils.mc.player.getX(), WorldUtils.mc.player.getEyeY(), WorldUtils.mc.player.getZ());
        BlockHitResult blockHitResult3 = WorldUtils.mc.level.clip(new ClipContext(vec3, Vec3.atCenterOf((Vec3i)blockPos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
        if (blockHitResult3.getType() == HitResult.Type.MISS || blockHitResult3 instanceof BlockHitResult && (blockHitResult2 = blockHitResult3).getBlockPos().equals((Object)blockPos)) {
            return true;
        }
        Vec3 vec32 = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.0, (double)blockPos.getZ() + 0.5);
        BlockHitResult blockHitResult4 = WorldUtils.mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)WorldUtils.mc.player));
        if (blockHitResult4.getType() == HitResult.Type.MISS || blockHitResult4 instanceof BlockHitResult && (blockHitResult = blockHitResult4).getBlockPos().equals((Object)blockPos)) {
            return true;
        }
        return CrystalPlacementHelper.isPlacementVisible(blockPos);
    }

    public static boolean canBreak(BlockPos ... blockPosArray) {
        return Arrays.stream(blockPosArray).allMatch(blockPos -> WorldUtils.mc.level.getBlockState(blockPos).getBlock().defaultDestroyTime() != -1.0f);
    }

    public static boolean isReplaceable(BlockPos ... blockPosArray) {
        return Arrays.stream(blockPosArray).allMatch(blockPos -> WorldUtils.mc.level.getBlockState(blockPos).canBeReplaced());
    }

    public static Block getBlock(BlockPos blockPos) {
        return WorldUtils.mc.level.getBlockState(blockPos).getBlock();
    }

    public static int getNetherPosition(int n) {
        return WorldUtils.mc.player.level().dimension() == Level.NETHER ? n * 8 : n / 8;
    }

    public static String getMovementDirection(Direction direction) {
        if (direction.getName().equalsIgnoreCase("North")) {
            return "-Z";
        }
        if (direction.getName().equalsIgnoreCase("East")) {
            return "+X";
        }
        if (direction.getName().equalsIgnoreCase("South")) {
            return "+Z";
        }
        if (direction.getName().equalsIgnoreCase("West")) {
            return "-X";
        }
        return "N/A";
    }

    public static String getFacingName(float f) {
        return FACING_NAMES[WorldUtils.getFacingOctant(f)];
    }

    public static String getFacingAxes(float f) {
        return FACING_AXES[WorldUtils.getFacingOctant(f)];
    }

    private static int getFacingOctant(float f) {
        float f2 = f % 360.0f;
        if (f2 < 0.0f) {
            f2 += 360.0f;
        }
        return Math.round(f2 / 45.0f) % 8;
    }

    public static float getBreakTime(Player player, BlockState blockState) {
        if (player == null) {
            return 0.0f;
        }
        float f = NETHERITE_PICKAXE.getItem().getDestroySpeed(NETHERITE_PICKAXE, blockState) + 26.0f;
        return 1.0f / (f / blockState.getBlock().defaultDestroyTime() / 30.0f) * 50.0f;
    }

    public static double getBreakDelta(BlockState blockState, int n) {
        int n2;
        ItemStack itemStack;
        if (n == -1) {
            return 0.0;
        }
        float f = WorldUtils.mc.player.getInventory().getItem(n).getItem().getDestroySpeed(WorldUtils.mc.player.getInventory().getItem(n), blockState);
        if (f > 1.0f) {
            itemStack = WorldUtils.mc.player.getInventory().getItem(n);
            n2 = EnchantmentHelper.getItemEnchantmentLevel((Holder)WorldUtils.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), (ItemInstance)itemStack);
            if (n2 > 0 && !itemStack.isEmpty()) {
                f += (float)(n2 * n2 + 1);
            }
        }
        if (MobEffectUtil.hasDigSpeed((LivingEntity)WorldUtils.mc.player)) {
            f *= 1.0f + (float)(MobEffectUtil.getDigSpeedAmplification((LivingEntity)WorldUtils.mc.player) + 1) * 0.2f;
        }
        if (WorldUtils.mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
            f *= (switch (WorldUtils.mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        boolean liquidCheck = speedMineModule != null && speedMineModule.isToggled() && speedMineModule.liquidCheck.getValue();
        boolean airCheck = speedMineModule != null && speedMineModule.isToggled() && speedMineModule.airCheck.getValue();
        if (!liquidCheck && WorldUtils.mc.player.isEyeInFluid(FluidTags.WATER) && EnchantmentHelper.getEnchantmentLevel((Holder)WorldUtils.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.AQUA_AFFINITY), (LivingEntity)WorldUtils.mc.player) <= 0) {
            f /= 5.0f;
        }
        if (!airCheck && !WorldUtils.mc.player.onGround()) {
            f /= 5.0f;
        }
        return f / blockState.getBlock().defaultDestroyTime() / (float)(!blockState.requiresCorrectToolForDrops() || WorldUtils.mc.player.getInventory().getItem(n).isCorrectToolForDrops(blockState) ? 30 : 100);
    }

    public static float getMineSpeed(BlockState blockState, int n) {
        int n2;
        ItemStack itemStack;
        if (WorldUtils.mc.player == null) {
            return 0.0f;
        }
        float f = WorldUtils.mc.player.getInventory().getItem(n).getItem().getDestroySpeed(WorldUtils.mc.player.getInventory().getItem(n), blockState);
        if (f > 1.0f) {
            itemStack = WorldUtils.mc.player.getInventory().getItem(n);
            n2 = EnchantmentHelper.getItemEnchantmentLevel((Holder)WorldUtils.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), (ItemInstance)itemStack);
            if (n2 > 0 && !itemStack.isEmpty()) {
                f += (float)(StrictMath.pow(n2, 2.0) + 1.0);
            }
        }
        if (WorldUtils.mc.player.hasEffect(MobEffects.HASTE)) {
            f *= 1.0f + (float)(WorldUtils.mc.player.getEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2f;
        }
        if (WorldUtils.mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
            f *= (float)Math.pow(0.3f, WorldUtils.mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier() + 1);
        }
        SpeedMineModule speedMineModule2 = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        boolean liquidCheck2 = speedMineModule2 != null && speedMineModule2.isToggled() && speedMineModule2.liquidCheck.getValue();
        boolean airCheck2 = speedMineModule2 != null && speedMineModule2.isToggled() && speedMineModule2.airCheck.getValue();
        if (!liquidCheck2 && WorldUtils.mc.player.isEyeInFluid(FluidTags.WATER)) {
            f *= (float)WorldUtils.mc.player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }
        if (!airCheck2 && !WorldUtils.mc.player.onGround()) {
            f /= 5.0f;
        }
        f = f < 0.0f ? 0.0f : f;
        return f / blockState.getBlock().defaultDestroyTime() / (float)(!blockState.requiresCorrectToolForDrops() || WorldUtils.mc.player.getInventory().getItem(n).isCorrectToolForDrops(blockState) ? 30 : 100);
    }

    public static boolean blocksMovement(BlockState blockState) {
        return blockState.getBlock() != Blocks.COBWEB && blockState.getBlock() != Blocks.BAMBOO_SAPLING && !blockState.canBeReplaced();
    }

    public static boolean equals(BlockPos blockPos, BlockPos blockPos2) {
        if (blockPos == null && blockPos2 == null) {
            return true;
        }
        if (blockPos == null || blockPos2 == null) {
            return false;
        }
        return blockPos.equals((Object)blockPos2);
    }

    public static String getDimension() {
        return WorldUtils.mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
    }

    public static List<Player> getCollisions(BlockPos blockPos) {
        ArrayList<Player> arrayList = new ArrayList<Player>();
        for (Player player : WorldUtils.mc.level.players()) {
            if (player == null || player.isRemoved() || EntityUtils.isGhost((Entity)player) || !player.getBoundingBox().intersects(new AABB(blockPos))) continue;
            arrayList.add(player);
        }
        return arrayList;
    }

    private static Packet createPlacePacket(InteractionHand interactionHand, BlockHitResult blockHitResult, int n) {
        return new ServerboundUseItemOnPacket(interactionHand, blockHitResult, n);
    }

    private static Packet createAirPlacePacket(InteractionHand interactionHand, BlockHitResult blockHitResult, int n) {
        return new ServerboundUseItemOnPacket(interactionHand, blockHitResult, n);
    }

    private record PredictedPlacement(Block block, long time) {
    }

    private record DirectionCandidate(Direction direction, double distSq) {
    }
}

