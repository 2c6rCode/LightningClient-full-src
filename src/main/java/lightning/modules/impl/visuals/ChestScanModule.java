/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.ChestType
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.visuals.chestscan.ChestScanChain;
import lightning.modules.impl.visuals.chestscan.ChestScanStore;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@RegisterModule(name="ChestScan", description="Highlights opened chests by contents (empty/partial/full), with optional hopper-chain inference.", category=Module.Category.VISUALS)
public class ChestScanModule
extends Module {
    public NumberSetting scanRadius = new NumberSetting("ScanRadius", "How far (in blocks) to render tracked chests and consider hopper chains.", 64.0, 8.0, 128.0);
    public BooleanSetting hopperChain = new BooleanSetting("HopperChain", "Smart mode to check chests linked to the bottom chest by hoppers.", false);
    public ModeSetting renderMode = new ModeSetting("Mode", "Render mode for chests.", "Both", new String[]{"Both", "Fill", "Outline"});
    public CategorySetting emptyCategory = new CategorySetting("Empty", "Color for empty chests.");
    public ColorSetting emptyFillColor = new ColorSetting("EmptyFillColor", "Fill", "Fill color for empty chests.", new CategorySetting.Visibility(this.emptyCategory), new ColorSetting.Color(new Color(0, 200, 0, 120), false, false));
    public ColorSetting emptyOutlineColor = new ColorSetting("EmptyOutlineColor", "Outline", "Outline color for empty chests.", new CategorySetting.Visibility(this.emptyCategory), new ColorSetting.Color(new Color(0, 200, 0, 255), false, false));
    public CategorySetting partialCategory = new CategorySetting("Partial", "Color for partially filled chests.");
    public ColorSetting partialFillColor = new ColorSetting("PartialFillColor", "Fill", "Fill color for partially filled chests.", new CategorySetting.Visibility(this.partialCategory), new ColorSetting.Color(new Color(230, 200, 0, 120), false, false));
    public ColorSetting partialOutlineColor = new ColorSetting("PartialOutlineColor", "Outline", "Outline color for partially filled chests.", new CategorySetting.Visibility(this.partialCategory), new ColorSetting.Color(new Color(230, 200, 0, 255), false, false));
    public CategorySetting fullCategory = new CategorySetting("Full", "Color for full chests.");
    public ColorSetting fullFillColor = new ColorSetting("FullFillColor", "Fill", "Fill color for full chests.", new CategorySetting.Visibility(this.fullCategory), new ColorSetting.Color(new Color(220, 0, 0, 120), false, false));
    public ColorSetting fullOutlineColor = new ColorSetting("FullOutlineColor", "Outline", "Outline color for full chests.", new CategorySetting.Visibility(this.fullCategory), new ColorSetting.Color(new Color(220, 0, 0, 255), false, false));
    private final ChestScanStore store = new ChestScanStore();
    private String lastWorldKey = null;
    private BlockPos lastLookedAtChestPos = null;
    private boolean wasChestMenuOpenLastTick = false;
    private BlockPos openChestPos = null;
    private ChestScanStore.ChestStatus lastSnapshotStatus = null;
    private int chainTicks = 0;
    private Set<BlockPos> lastInferredEmpty = Collections.emptySet();
    private final Map<BlockPos, Long> missingSinceMs = new HashMap<BlockPos, Long>();
    private static final long PRUNE_GRACE_MS = 1500L;

    public ChestScanStore getStore() {
        return this.store;
    }

    @Override
    public void onEnable() {
        this.store.loadForWorld();
        this.lastWorldKey = ChestScanStore.currentWorldKey();
    }

    @Override
    public void onDisable() {
        if (this.openChestPos != null && this.lastSnapshotStatus != null) {
            this.finalizeChestState(mc, this.openChestPos, this.lastSnapshotStatus);
        }
        this.lastWorldKey = null;
        this.lastLookedAtChestPos = null;
        this.wasChestMenuOpenLastTick = false;
        this.openChestPos = null;
        this.lastSnapshotStatus = null;
        this.missingSinceMs.clear();
    }

    private void maybeReloadStoreForWorld() {
        String string = ChestScanStore.currentWorldKey();
        if (!string.equals(this.lastWorldKey)) {
            this.store.loadForWorld();
            this.lastWorldKey = string;
            this.missingSinceMs.clear();
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (ChestScanModule.mc.player == null || ChestScanModule.mc.level == null) {
            return;
        }
        this.maybeReloadStoreForWorld();
        boolean bl = ChestScanModule.mc.player.containerMenu instanceof ChestMenu;
        if (!bl && ChestScanModule.mc.gui.screen() == null) {
            this.lastLookedAtChestPos = this.resolveLookedAtChestPos(mc);
        }
        if (bl && !this.wasChestMenuOpenLastTick) {
            this.openChestPos = this.lastLookedAtChestPos;
            this.lastSnapshotStatus = null;
        }
        if (bl) {
            ChestMenu chestMenu = (ChestMenu)ChestScanModule.mc.player.containerMenu;
            this.lastSnapshotStatus = this.computeStatus(chestMenu.getContainer());
        }
        if (!bl && this.wasChestMenuOpenLastTick) {
            this.finalizeChestState(mc, this.openChestPos, this.lastSnapshotStatus);
            this.openChestPos = null;
            this.lastSnapshotStatus = null;
        }
        this.wasChestMenuOpenLastTick = bl;
        this.tickChainRecompute(mc);
    }

    private void tickChainRecompute(Minecraft minecraft) {
        if (!this.hopperChain.getValue()) {
            this.lastInferredEmpty = Collections.emptySet();
            return;
        }
        ++this.chainTicks;
        if (this.chainTicks < 20) {
            return;
        }
        this.chainTicks = 0;
        BlockPos blockPos = minecraft.player.blockPosition();
        int n = this.scanRadius.getValue().intValue();
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>(this.store.positions());
        Map<BlockPos, BlockPos> map = ChestScanChain.findEdges((Level)minecraft.level, hashSet, blockPos, n);
        HashMap<BlockPos, ChestScanStore.ChestStatus> hashMap = new HashMap<BlockPos, ChestScanStore.ChestStatus>();
        for (BlockPos blockPos2 : hashSet) {
            hashMap.put(blockPos2, this.store.get(blockPos2));
        }
        this.lastInferredEmpty = ChestScanChain.inferEmpty(map, hashMap);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (ChestScanModule.mc.player == null || ChestScanModule.mc.level == null) {
            return;
        }
        double d = this.scanRadius.getValue().doubleValue();
        double d2 = d * d;
        BlockPos playerPos = ChestScanModule.mc.player.blockPosition();
        boolean bl = this.renderMode.getValue().equalsIgnoreCase("Fill") || this.renderMode.getValue().equalsIgnoreCase("Both");
        boolean bl2 = this.renderMode.getValue().equalsIgnoreCase("Outline") || this.renderMode.getValue().equalsIgnoreCase("Both");
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        for (BlockPos pos : new ArrayList<BlockPos>(this.store.positions())) {
            if (playerPos.distSqr(pos) > d2 || !ChestScanModule.mc.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) continue;
            if (!(ChestScanModule.mc.level.getBlockState(pos).getBlock() instanceof ChestBlock)) {
                long l = System.currentTimeMillis();
                Long missing = this.missingSinceMs.putIfAbsent(pos, l);
                if (missing == null || l - missing < 1500L) continue;
                this.store.remove(pos);
                this.missingSinceMs.remove(pos);
                continue;
            }
            this.missingSinceMs.remove(pos);
            ChestScanStore.ChestStatus status = this.store.get(pos);
            if (hashSet.contains(pos) || status == null) continue;
            AABB aABB = this.getChestBox(pos, hashSet);
            Color fillColor = this.getFillColor(status);
            Color outlineColor = this.getOutlineColor(status);
            if (bl) {
                Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, fillColor);
            }
            if (!bl2) continue;
            Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, outlineColor);
        }
        if (this.hopperChain.getValue()) {
            Color color2 = this.emptyFillColor.getColor();
            Color outlineColor2 = this.emptyOutlineColor.getColor();
            for (BlockPos blockPos2 : this.lastInferredEmpty) {
                if (this.store.get(blockPos2) != null || playerPos.distSqr(blockPos2) > d2 || !ChestScanModule.mc.level.hasChunk(blockPos2.getX() >> 4, blockPos2.getZ() >> 4) || hashSet.contains(blockPos2) || !(ChestScanModule.mc.level.getBlockState(blockPos2).getBlock() instanceof ChestBlock)) continue;
                AABB aABB = this.getChestBox(blockPos2, hashSet);
                if (bl) {
                    Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, color2);
                }
                if (!bl2) continue;
                Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, outlineColor2);
            }
        }
    }

    private AABB getChestBox(BlockPos blockPos, Set<BlockPos> set) {
        set.add(blockPos);
        BlockState blockState = ChestScanModule.mc.level.getBlockState(blockPos);
        if (blockState.getBlock() instanceof ChestBlock && blockState.hasProperty((Property)ChestBlock.TYPE) && blockState.getValue((Property)ChestBlock.TYPE) != ChestType.SINGLE) {
            Direction direction = (Direction)blockState.getValue((Property)ChestBlock.FACING);
            ChestType chestType = (ChestType)blockState.getValue((Property)ChestBlock.TYPE);
            Direction direction2 = chestType == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
            BlockPos blockPos2 = blockPos.relative(direction2);
            BlockState blockState2 = ChestScanModule.mc.level.getBlockState(blockPos2);
            if (blockState2.getBlock() instanceof ChestBlock && blockState2.hasProperty((Property)ChestBlock.TYPE) && blockState2.getValue((Property)ChestBlock.TYPE) != ChestType.SINGLE) {
                set.add(blockPos2);
                return new AABB((double)Math.min(blockPos.getX(), blockPos2.getX()), (double)Math.min(blockPos.getY(), blockPos2.getY()), (double)Math.min(blockPos.getZ(), blockPos2.getZ()), (double)Math.max(blockPos.getX(), blockPos2.getX()) + 1.0, (double)Math.max(blockPos.getY(), blockPos2.getY()) + 1.0, (double)Math.max(blockPos.getZ(), blockPos2.getZ()) + 1.0);
            }
        }
        return new AABB(blockPos);
    }

    private Color getFillColor(ChestScanStore.ChestStatus chestStatus) {
        return switch (chestStatus) {
            default -> throw new MatchException(null, null);
            case ChestScanStore.ChestStatus.EMPTY -> this.emptyFillColor.getColor();
            case ChestScanStore.ChestStatus.PARTIAL -> this.partialFillColor.getColor();
            case ChestScanStore.ChestStatus.FULL -> this.fullFillColor.getColor();
        };
    }

    private Color getOutlineColor(ChestScanStore.ChestStatus chestStatus) {
        return switch (chestStatus) {
            default -> throw new MatchException(null, null);
            case ChestScanStore.ChestStatus.EMPTY -> this.emptyOutlineColor.getColor();
            case ChestScanStore.ChestStatus.PARTIAL -> this.partialOutlineColor.getColor();
            case ChestScanStore.ChestStatus.FULL -> this.fullOutlineColor.getColor();
        };
    }

    private BlockPos resolveLookedAtChestPos(Minecraft minecraft) {
        BlockHitResult blockHitResult;
        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult) || (blockHitResult = (BlockHitResult)hitResult).getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos blockPos = blockHitResult.getBlockPos();
        return minecraft.level.getBlockState(blockPos).getBlock() instanceof ChestBlock ? blockPos : null;
    }

    private ChestScanStore.ChestStatus computeStatus(Container container) {
        int n = container.getContainerSize();
        if (n == 0) {
            return ChestScanStore.ChestStatus.EMPTY;
        }
        int n2 = 0;
        for (int i = 0; i < n; ++i) {
            if (container.getItem(i).isEmpty()) continue;
            ++n2;
        }
        if (n2 == 0) {
            return ChestScanStore.ChestStatus.EMPTY;
        }
        return n2 == n ? ChestScanStore.ChestStatus.FULL : ChestScanStore.ChestStatus.PARTIAL;
    }

    private void finalizeChestState(Minecraft minecraft, BlockPos blockPos, ChestScanStore.ChestStatus chestStatus) {
        if (blockPos == null || chestStatus == null) {
            return;
        }
        this.store.put(blockPos, chestStatus);
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        BlockState blockState = minecraft.level.getBlockState(blockPos);
        if (blockState.getBlock() instanceof ChestBlock && blockState.hasProperty((Property)ChestBlock.TYPE) && blockState.getValue((Property)ChestBlock.TYPE) != ChestType.SINGLE) {
            Direction direction = (Direction)blockState.getValue((Property)ChestBlock.FACING);
            ChestType chestType = (ChestType)blockState.getValue((Property)ChestBlock.TYPE);
            Direction direction2 = chestType == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
            BlockPos blockPos2 = blockPos.relative(direction2);
            if (minecraft.level.getBlockState(blockPos2).getBlock() instanceof ChestBlock) {
                this.store.put(blockPos2, chestStatus);
            }
        }
    }
}

