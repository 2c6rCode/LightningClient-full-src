/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.boss.enderdragon.EndCrystal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ExperienceBottleItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package lightning.pingbypass.modules.submodules.crystal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.DestroyBlockEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.modules.impl.combat.CrystalPlacementHelper;
import lightning.modules.impl.combat.SuicideModule;
import lightning.modules.impl.movement.PhaseModule;
import lightning.modules.impl.player.KeyActionModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.modules.PbModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.Counter;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ServerAutoCrystal
extends PbModule
implements IMinecraft {
    public BooleanSetting attack = new BooleanSetting("Attack", "Automatically attacks crystals that are deemed safe.", true);
    public NumberSetting attackSpeed = new NumberSetting("AttackSpeed", "The speed at which crystals will be attacked.", Float.valueOf(20.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting attackRange = new NumberSetting("AttackRange", "The maximum distance at which crystals will be attacked.", 4.5, 0.0, 8.0);
    public NumberSetting attackWallsRange = new NumberSetting("AttackWallsRange", "The maximum distance at which crystals will be attacked through walls.", 4.5, 0.0, 8.0);
    public ModeSetting antiWeakness = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", "None", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting instant = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", true);
    public BooleanSetting inhibit = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", true);
    public BooleanSetting place = new BooleanSetting("Place", "Automatically places crystals on positions that are deemed safe and lethal enough.", true);
    public NumberSetting placeSpeed = new NumberSetting("PlaceSpeed", "The speed at which crystals will be placed.", Float.valueOf(20.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "The maximum distance at which positions will be placed on.", 4.5, 0.0, 8.0);
    public NumberSetting placeWallsRange = new NumberSetting("PlaceWallsRange", "The maximum distance at which positions will be placed on through walls.", 4.5, 0.0, 8.0);
    public ModeSetting placements = new ModeSetting("Placements", "The version of the game that will be used for crystal placement calculations.", "Native", new String[]{"Native", "Protocol"});
    public BooleanSetting blockDestruction = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", true);
    public ModeSetting autoSwitch = new ModeSetting("Switch", "Automatically switches to a crystal if you aren't currently holding one.", "None", new String[]{"None", "Normal", "Silent", "AltSwap"});
    public BooleanSetting swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were holding before the module started switching to crystals.", false);
    public NumberSetting swapDelay = new NumberSetting("SwapDelay", "The delay in ticks after swapping before placing or attacking crystals.", 0, 0, 20);
    public ModeSetting sequential = new ModeSetting("Sequential", "The sequence that the module's processes will be run in.", "Strong", new String[]{"None", "Strict", "Strong"});
    public ModeSetting rotate = new ModeSetting("Rotate", "Automatically rotates to the crystal whenever attacking or placing.", "Normal", new String[]{"None", "Normal", "Packet", "Silent"});
    public ModeSetting swing = new ModeSetting("Swing", "The hand that will be used for swinging.", "Default", new String[]{"Default", "None", "Packet", "Mainhand", "Offhand", "Both"});
    public BooleanSetting yawStep = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", false);
    public NumberSetting yawStepThreshold = new NumberSetting("YawStepThreshold", "The threshold in order for yaw to be modified.", 75, 1, 180);
    public BooleanSetting raytrace = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", false);
    public NumberSetting extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", 0, 0, 20);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", 10.0, 0.0, 24.0);
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignores naked players (even if wearing only elytra), but still targets them if they are holding an End Crystal.", false);
    public BooleanSetting chestBreak = new BooleanSetting("ChestBreak", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", false);
    public BooleanSetting pauseOnPearl = new BooleanSetting("PauseOnPearl", "Pauses AutoCrystal when Phase is active, KeyAction pearl is used, or when throwing an ender pearl manually.", true);
    public BooleanSetting pauseOnXP = new BooleanSetting("PauseOnXP", "Pauses AutoCrystal when throwing experience bottles manually or when KeyAction XP is active.", true);
    public BooleanSetting gameLoop = new BooleanSetting("GameLoop", "Runs the module on loop instead of ticks.", false);
    public NumberSetting loopDelay = new NumberSetting("LoopDelay", "The delay that has to be waited out before running the module again.", 50, 0, 1000);
    public ModeSetting whileEating = new ModeSetting("WhileEating", "Places and attacks crystal while eating or using items.", "Both", new String[]{"None", "Attack", "Place", "Both"});
    public BooleanSetting godSync = new BooleanSetting("GodSync", "Makes the attacking way faster by predicting entity IDs.", false);
    public NumberSetting predictions = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", 10, 1, 20);
    public NumberSetting offset = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", 0, 0, 2);
    public ModeSetting godSwing = new ModeSetting("GodSwing", "The swinging that will be done for each predicted attack.", "Normal", new String[]{"None", "Normal", "Strict"});
    public BooleanSetting fast = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", false);
    public BooleanSetting antiKick = new BooleanSetting("AntiKick", "Prevents you from getting kicked by attacking invalid entity IDs.", false);
    public NumberSetting kickThreshold = new NumberSetting("KickThreshold", "The tick threshold for the kick prevention.", 5, 1, 10);
    public ModeSetting facePlaceMode = new ModeSetting("FaceplaceMode", "The checks that will be done in order to faceplace.", "Dynamic", new String[]{"None", "Dynamic", "Always"});
    public ModeSetting facePlaceSpeed = new ModeSetting("FaceplaceSpeed", "The speed that players will be faceplaced at.", "Normal", new String[]{"Normal", "Custom"});
    public NumberSetting facePlaceDelay = new NumberSetting("FaceplaceDelay", "The ticks that have to be waited for before faceplacing again.", 11, 0, 20);
    public BooleanSetting healthPlace = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", true);
    public NumberSetting health = new NumberSetting("Health", "The health that the target needs to be at in order for the module to start faceplacing.", Float.valueOf(8.0f), Float.valueOf(0.0f), Float.valueOf(36.0f));
    public BooleanSetting armorPlace = new BooleanSetting("ArmorPlace", "Whether or not to faceplace when the target's armor is low on durability.", true);
    public NumberSetting percentage = new NumberSetting("Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", 10, 1, 100);
    public NumberSetting minimumDamage = new NumberSetting("MinimumDamage", "The minimum damage that has to be dealt to enemies.", 6.0, 0.0, 36.0);
    public NumberSetting maximumSelfDamage = new NumberSetting("MaximumSelfDamage", "The maximum damage that can be dealt to you by crystals.", 10.0, 0.0, 36.0);
    public NumberSetting lethalMultiplier = new NumberSetting("LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", Float.valueOf(1.5f), Float.valueOf(0.0f), Float.valueOf(4.0f));
    public BooleanSetting antiSuicide = new BooleanSetting("AntiSuicide", "Prevents crystals from accidentally killing you when you're low on health.", true);
    public BooleanSetting ignoreTerrain = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", true);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<?> pendingCalc = null;
    private Runnable attackRunnable = null;
    private Runnable placeRunnable = null;
    private final Map<Integer, Long> attackedCrystals = new ConcurrentHashMap<Integer, Long>();
    private final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<BlockPos, Long>();
    private final Map<BlockPos, Long> countedCrystals = new ConcurrentHashMap<BlockPos, Long>();
    private final Timer attackTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer facePlaceTimer = new Timer();
    private final Timer loopTimer = new Timer();
    private final Timer swapTimer = new Timer();
    private int lastSelectedSlot = -1;
    private long totalPlaces = 0L;
    private long totalAttacks = 0L;
    private boolean sequenceAttack = false;
    private boolean sequencePlace = true;
    private boolean attackedSequentially = false;
    private boolean placedSequentially = false;
    private Player target = null;
    private EndCrystal attackTarget = null;
    private PlaceTarget placeTarget = null;
    private PlaceTarget mineTarget = null;
    private String calculationTime = "0.00ms";
    private int calculationCount = 0;
    private String calculationDamage = "0.00";
    private final Counter crystalCounter = new Counter();
    private int crystalsPerSecond = 0;
    private int highestID = -100000;
    private int kickTicks = 0;
    private int savedSlot = -1;
    private static final float STICKY_EPSILON = 0.5f;
    private long lastPearlThrowTime = 0L;
    private long lastXpThrowTime = 0L;

    public ServerAutoCrystal() {
        super("AutoCrystal");
    }

    @Override
    public void onEnable() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @Override
    public void onDisable() {
        Night.EVENT_HANDLER.unsubscribe(this);
        if (this.pendingCalc != null) {
            this.pendingCalc.cancel(false);
            this.pendingCalc = null;
        }
        if (this.savedSlot != -1) {
            InventoryUtils.switchBackNormal(this.savedSlot);
            this.savedSlot = -1;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
        Night.RENDER_MANAGER.setRenderPosition(null);
        this.attackedCrystals.clear();
        this.placedCrystals.clear();
        this.countedCrystals.clear();
        this.attackedSequentially = false;
        this.placedSequentially = false;
        this.target = null;
        this.placeTarget = null;
        this.mineTarget = null;
        this.calculationTime = "0.00ms";
        this.calculationCount = 0;
        this.calculationDamage = "0.00";
        this.crystalCounter.reset();
        this.highestID = -100000;
    }

    @Override
    public List<Setting> getSettings() {
        return List.of(this.attack, this.attackSpeed, this.attackRange, this.attackWallsRange, this.antiWeakness, this.instant, this.inhibit, this.place, this.placeSpeed, this.placeRange, this.placeWallsRange, this.placements, this.blockDestruction, this.autoSwitch, this.swapBack, this.swapDelay, this.sequential, this.rotate, this.swing, this.yawStep, this.yawStepThreshold, this.raytrace, this.extrapolation, this.enemyRange, this.chestBreak, this.gameLoop, this.loopDelay, this.whileEating, this.godSync, this.predictions, this.offset, this.godSwing, this.fast, this.antiKick, this.kickThreshold, this.facePlaceMode, this.facePlaceSpeed, this.facePlaceDelay, this.healthPlace, this.health, this.armorPlace, this.percentage, this.minimumDamage, this.maximumSelfDamage, this.lethalMultiplier, this.antiSuicide, this.ignoreTerrain);
    }

    private boolean isDead() {
        if (ServerAutoCrystal.mc.player == null || ServerAutoCrystal.mc.level == null) {
            return true;
        }
        if (!ServerAutoCrystal.mc.player.isAlive() || ServerAutoCrystal.mc.player.isDeadOrDying() || ServerAutoCrystal.mc.player.getHealth() <= 0.0f) {
            return true;
        }
        return ServerAutoCrystal.mc.gui.screen() instanceof DeathScreen;
    }

    @Override
    public void tick() {
        if (this.isDead()) {
            this.attackRunnable = null;
            this.placeRunnable = null;
            this.target = null;
            this.placeTarget = null;
            this.attackTarget = null;
            this.mineTarget = null;
            this.placedCrystals.clear();
            this.attackedCrystals.clear();
            this.countedCrystals.clear();
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        int n = ServerAutoCrystal.mc.player.getInventory().getSelectedSlot();
        if (this.lastSelectedSlot != -1 && this.lastSelectedSlot != n) {
            this.swapTimer.reset();
        }
        this.lastSelectedSlot = n;
        long l = 50L;
        this.attackedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, l));
        long l2 = Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, 500L) + (long)((20.0f - this.attackSpeed.getValue().floatValue()) * 50.0f);
        this.placedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > l2);
        this.countedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, l));
        this.crystalsPerSecond = this.crystalCounter.getCount();
        Runnable runnable = () -> {
            long startNano = System.nanoTime();
            this.attackTarget = this.calculateCrystals();
            this.placeTarget = this.calculatePlacements(null);
            long durationNano = System.nanoTime() - startNano;
            this.calculationTime = new DecimalFormat("0.00").format((double)durationNano / 1000000.0) + "ms";
            this.calculationCount = this.placeTarget == null ? 0 : this.placeTarget.getCalculations();
            this.calculationDamage = this.placeTarget == null ? "0.00" : new DecimalFormat("0.00").format(this.placeTarget.getDamage());
            this.target = this.placeTarget == null ? null : this.placeTarget.getPlayer();
            Player player = this.target;
            if (this.blockDestruction.getValue()) {
                SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
                BlockPos blockPos = null;
                if (speedMineModule.getPrimary() != null && speedMineModule.getPrimary().isMining()) {
                    blockPos = speedMineModule.getPrimary().getPosition();
                }
                if (blockPos != null) {
                    this.mineTarget = this.calculatePlacements(blockPos);
                }
            }
        };
        if (this.pendingCalc == null || this.pendingCalc.isDone()) {
            this.pendingCalc = this.executor.submit(runnable);
        }
        if (this.gameLoop.getValue()) {
            if (!this.loopTimer.hasTimeElapsed(this.loopDelay.getValue().longValue())) {
                return;
            }
            this.loopTimer.reset();
        }
        this.run();
        if (this.attackRunnable != null) {
            this.attackRunnable.run();
            this.attackRunnable = null;
        }
        if (this.placeRunnable != null) {
            this.placeRunnable.run();
            this.placeRunnable = null;
        }
    }

    private void run() {
        if (this.isDead() || this.isPearlPaused() || this.isXpPaused()) {
            return;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
        if (this.sequential.getValue().equalsIgnoreCase("None")) {
            if (this.sequenceAttack) {
                this.sequenceAttack = false;
                this.sequencePlace = true;
                this.attackCrystals();
                return;
            }
            if (this.sequencePlace) {
                this.sequenceAttack = true;
                this.sequencePlace = false;
                this.placeCrystals(false);
            }
        } else {
            if (this.attack.getValue()) {
                this.attackCrystals();
            }
            if (this.place.getValue()) {
                this.placeCrystals(false);
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent entitySpawnEvent) {
        if (!this.isToggled() || this.isDead() || this.getPlayers().isEmpty() || this.shouldPause("Attack")) {
            return;
        }
        if (!this.attack.getValue() || !this.instant.getValue()) {
            return;
        }
        if (!this.attackTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.attackSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        Entity entity = entitySpawnEvent.getEntity();
        if (!(entity instanceof EndCrystal)) {
            return;
        }
        EndCrystal endCrystal = (EndCrystal)entity;
        if (this.inhibit.getValue() && this.attackedCrystals.containsKey(endCrystal.getId())) {
            return;
        }
        if (!this.placedCrystals.containsKey(endCrystal.blockPosition().below())) {
            return;
        }
        if (endCrystal.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            return;
        }
        if (!ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(endCrystal.blockPosition())) {
            return;
        }
        if (!WorldUtils.canSee((Entity)endCrystal) && (this.raytrace.getValue() || endCrystal.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue()))) {
            return;
        }
        if (!this.rotate.getValue().equalsIgnoreCase("None")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition())));
        }
        this.attack(endCrystal);
        this.attackedSequentially = true;
        if (this.sequential.getValue().equalsIgnoreCase("Strong")) {
            this.placeCrystals(true);
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent destroyBlockEvent) {
        if (!this.isToggled() || this.isDead() || this.getPlayers().isEmpty() || this.shouldPause("Place")) {
            return;
        }
        this.kickTicks = 0;
        if (!this.blockDestruction.getValue()) {
            return;
        }
        if (!this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        BlockPos blockPos = destroyBlockEvent.getPosition();
        if (blockPos == null) {
            return;
        }
        int n = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int n2 = ServerAutoCrystal.mc.player.getInventory().getSelectedSlot();
        boolean bl = false;
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && n == -1 && ServerAutoCrystal.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        PlaceTarget placeTarget = this.mineTarget == null ? null : this.mineTarget.clone();
        PlaceTarget placeTarget2 = placeTarget;
        if (placeTarget == null || placeTarget.getPosition() != null && !blockPos.equals((Object)placeTarget.getException())) {
            placeTarget = this.calculatePlacements(blockPos);
        }
        if (placeTarget == null || placeTarget.getPosition() == null || placeTarget.getDamage() <= 0.0f) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        BlockPos blockPos2 = placeTarget.getPosition();
        if (ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos2)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        Night.RENDER_MANAGER.setRenderPosition(blockPos2);
        if (!WorldUtils.canSeeBlock(blockPos2) && (this.raytrace.getValue() || ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos2)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue()))) {
            return;
        }
        EndCrystal endCrystal = null;
        for (Entity e : ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(blockPos2.above()), entity -> true)) {
            if (!(e instanceof EndCrystal)) continue;
            endCrystal = (EndCrystal)e;
            break;
        }
        if (endCrystal != null) {
            if (this.isAtActiveUnbrokenMinePos(endCrystal)) {
                Night.RENDER_MANAGER.setRenderPosition(null);
                return;
            }
            if (!this.attack.getValue()) {
                Night.RENDER_MANAGER.setRenderPosition(null);
                return;
            }
            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations((Entity)endCrystal));
            }
            this.attack(endCrystal);
            return;
        }
        CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(blockPos2);
        Vec3 vec3 = placementResult != null && placementResult.hitVec != null ? placementResult.hitVec : Vec3.atCenterOf((Vec3i)blockPos2).add(0.0, 0.5, 0.0);
        if (!this.rotate.getValue().equalsIgnoreCase("None")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(vec3));
        }
        SpeedMineModule speedMine = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        boolean bl2 = speedMine.switchReset.getValue() && (speedMine.switchMode.getValue().equalsIgnoreCase("Normal") || speedMine.switchMode.getValue().equalsIgnoreCase("AltSwap") || speedMine.switchMode.getValue().equalsIgnoreCase("AltPickup"));
        boolean bl3 = bl2;
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            if (!bl2 && this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                this.savedSlot = n2;
            }
            InventoryUtils.switchSlot(bl2 ? "AltSwap" : this.autoSwitch.getValue(), n, n2);
            bl = true;
        }
        this.place(blockPos2);
        if (bl) {
            InventoryUtils.switchBack(bl2 ? "AltSwap" : this.autoSwitch.getValue(), n, n2);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (ServerAutoCrystal.mc.player == null || ServerAutoCrystal.mc.level == null) {
            return;
        }
        Packet<?> packet = packetReceiveEvent.getPacket();
        if (packet instanceof ClientboundAddEntityPacket) {
            BlockPos blockPos;
            ClientboundAddEntityPacket clientboundAddEntityPacket = (ClientboundAddEntityPacket)packet;
            if (clientboundAddEntityPacket.getId() > this.highestID) {
                this.highestID = clientboundAddEntityPacket.getId();
            }
            if (this.countedCrystals.containsKey(blockPos = BlockPos.containing((double)clientboundAddEntityPacket.getX(), (double)clientboundAddEntityPacket.getY(), (double)clientboundAddEntityPacket.getZ()).offset(0, -1, 0))) {
                if (this.facePlaceTimer.hasTimeElapsed(this.facePlaceDelay.getValue().longValue() * 50L)) {
                    this.facePlaceTimer.reset();
                }
                this.countedCrystals.remove(blockPos);
                this.crystalCounter.increment();
                this.crystalsPerSecond = this.crystalCounter.getCount();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        this.kickTicks = 0;
        this.attackRunnable = null;
        this.placeRunnable = null;
        this.target = null;
        this.placeTarget = null;
        this.attackTarget = null;
        this.mineTarget = null;
        this.placedCrystals.clear();
        this.attackedCrystals.clear();
        this.countedCrystals.clear();
        Night.RENDER_MANAGER.setRenderPosition(null);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (packetSendEvent.getPacket() instanceof ServerboundSetCarriedItemPacket) {
            this.swapTimer.reset();
        }
        if (packetSendEvent.getPacket() instanceof ServerboundUseItemPacket && ServerAutoCrystal.mc.player != null) {
            if (ServerAutoCrystal.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
                this.lastPearlThrowTime = System.currentTimeMillis();
            } else if (ServerAutoCrystal.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE) {
                this.lastXpThrowTime = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent clientConnectEvent) {
        this.highestID = -100000;
    }

    public String getMetaData() {
        return this.calculationTime + ", " + this.calculationCount + ", " + this.calculationDamage + ", " + this.crystalsPerSecond;
    }

    private void attackCrystals() {
        Object object;
        if (this.isDead() || this.shouldPause("Attack")) {
            return;
        }
        if (this.getPlayers().isEmpty()) {
            this.attackTarget = null;
            return;
        }
        EndCrystal endCrystal = null;
        PlaceTarget placeTarget = this.placeTarget;
        boolean bl = placeTarget != null && placeTarget.getPosition() == null && placeTarget.obstructions != null && !placeTarget.obstructions.isEmpty();
        for (Entity entity2 : bl ? placeTarget.obstructions : ServerAutoCrystal.mc.level.entitiesForRendering()) {
            if (!(entity2 instanceof EndCrystal crystal) || !crystal.isAlive() || (this.inhibit.getValue() && this.attackedCrystals.containsKey(entity2.getId())) || (!bl && !this.placedCrystals.containsKey(crystal.blockPosition().below())) || crystal.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(crystal.blockPosition()) || (!WorldUtils.canSee((Entity)crystal) && (this.raytrace.getValue() || crystal.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue())))) continue;
            endCrystal = crystal;
            break;
        }
        EndCrystal endCrystal2 = endCrystal == null ? this.attackTarget : endCrystal;
        EndCrystal endCrystal3 = endCrystal2;
        if (endCrystal2 == null) {
            return;
        }
        if (this.swapDelay.getValue().intValue() > 0 && !this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) {
            return;
        }
        if (!this.attackTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.attackSpeed.getValue().floatValue() * 50.0f)) || this.attackedSequentially) {
            if (this.attackedSequentially) {
                this.attackedSequentially = false;
            }
            return;
        }
        Entity entity = ServerAutoCrystal.mc.level.getEntity(endCrystal2.getId());
        object = null;
        if (entity == null) {
            object = "entity-gone";
        } else if (!(entity instanceof EndCrystal)) {
            object = "not-end-crystal";
        } else if (!((EndCrystal)entity).isAlive()) {
            object = "dead";
        } else if (this.inhibit.getValue() && this.attackedCrystals.containsKey(entity.getId())) {
            object = "inhibit";
        } else if (entity.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            object = "range";
        } else if (!ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
            object = "border";
        } else if (!WorldUtils.canSee(entity) && (this.raytrace.getValue() || entity.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue()))) {
            object = "cannot-see";
        }
        if (object != null) {
            return;
        }
        this.attackRunnable = () -> {
            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)endCrystal2.blockPosition())));
            }
            this.attack(endCrystal2);
        };
    }

    private void placeCrystals(boolean bl) {
        if (this.isDead() || this.shouldPause("Place")) {
            return;
        }
        PlaceTarget placeTarget = this.placeTarget == null ? null : this.placeTarget.clone();
        PlaceTarget placeTarget2 = placeTarget;
        if (placeTarget == null || placeTarget.getPosition() == null) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        if (placeTarget.getPlayer() == null || !placeTarget.getPlayer().isAlive() || placeTarget.getPlayer().isDeadOrDying() || placeTarget.getPlayer().getHealth() <= 0.0f) {
            this.placeTarget = null;
            this.target = null;
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        int n = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int n2 = ServerAutoCrystal.mc.player.getInventory().getSelectedSlot();
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && n == -1 && ServerAutoCrystal.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        BlockPos blockPos = placeTarget.getPosition();
        if (ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        Night.RENDER_MANAGER.setRenderPosition(blockPos);
        if (!ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(blockPos)) {
            return;
        }
        if (ServerAutoCrystal.mc.level.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN && ServerAutoCrystal.mc.level.getBlockState(blockPos).getBlock() != Blocks.BEDROCK) {
            return;
        }
        if (!ServerAutoCrystal.mc.level.getBlockState(blockPos.offset(0, 1, 0)).isAir() || this.placements.getValue().equalsIgnoreCase("Protocol") && !ServerAutoCrystal.mc.level.getBlockState(blockPos.offset(0, 2, 0)).isAir()) {
            return;
        }
        if (!WorldUtils.canSeeBlock(blockPos) && (this.raytrace.getValue() || ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue()))) {
            return;
        }
        if (ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(blockPos.offset(0, 1, 0)), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal))) {
            return;
        }
        if (this.swapDelay.getValue().intValue() > 0 && !this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) {
            return;
        }
        if (!this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        if (!bl && this.placedSequentially) {
            this.placedSequentially = false;
            return;
        }
        this.placeRunnable = () -> {
            boolean switched = false;
            CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(blockPos);
            Vec3 vec3 = placementResult != null && placementResult.hitVec != null ? placementResult.hitVec : Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.5, 0.0);
            Vec3 vec32 = vec3;
            if (!this.rotate.getValue().equalsIgnoreCase("None")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(vec3));
            }
            if (ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                    this.savedSlot = n2;
                }
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, n2);
                switched = true;
            }
            this.place(blockPos);
            if (switched) {
                InventoryUtils.switchBack(this.autoSwitch.getValue(), n, n2);
            }
            if (this.godSync.getValue()) {
                boolean bl2 = !this.antiKick.getValue() || !(ServerAutoCrystal.mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) && !(ServerAutoCrystal.mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem) && !Night.MODULE_MANAGER.getModule(KeyActionModule.class).isXpActive();
                boolean bl3 = bl2;
                if ((!this.antiKick.getValue() || this.kickTicks > this.kickThreshold.getValue().intValue()) && bl2) {
                    if (!this.fast.getValue()) {
                        for (Entity entity : ServerAutoCrystal.mc.level.entitiesForRendering()) {
                            if (entity.getId() <= this.highestID) continue;
                            this.highestID = entity.getId();
                        }
                    }
                    for (int i = 1 - this.offset.getValue().intValue(); i < this.predictions.getValue().intValue(); ++i) {
                        Entity entity;
                        entity = ServerAutoCrystal.mc.level.getEntity(this.highestID);
                        if (entity != null && !(entity instanceof EndCrystal)) continue;
                        int n3 = this.highestID + i;
                        mc.getConnection().send((Packet)new ServerboundAttackPacket(n3));
                        if (this.godSwing.getValue().equals("Strict")) {
                            mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                        }
                        this.attackedCrystals.put(n3, System.currentTimeMillis());
                    }
                    if (this.godSwing.getValue().equals("Normal")) {
                        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                }
                ++this.kickTicks;
            }
        };
        if (bl) {
            this.placeRunnable.run();
            this.placeRunnable = null;
            this.placedSequentially = true;
        }
    }

    private EndCrystal calculateCrystals() {
        if (!this.attack.getValue()) {
            return null;
        }
        if (this.shouldPause("Attack")) {
            return null;
        }
        List<Player> list = this.getPlayers();
        if (list.isEmpty()) {
            return null;
        }
        EndCrystal endCrystal = null;
        float f = 0.0f;
        for (Entity entity : ServerAutoCrystal.mc.level.entitiesForRendering()) {
            float f2;
            EndCrystal endCrystal2;
            if (!(entity instanceof EndCrystal) || !(endCrystal2 = (EndCrystal)entity).isAlive() || this.inhibit.getValue() && this.attackedCrystals.containsKey(entity.getId()) || endCrystal2.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(endCrystal2.blockPosition()) || !WorldUtils.canSee((Entity)endCrystal2) && (this.raytrace.getValue() || endCrystal2.getBoundingBox().distanceToSqr(ServerAutoCrystal.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue())) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((f2 = DamageUtils.getCrystalDamage((Entity)ServerAutoCrystal.mc.player, null, endCrystal2, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && f2 > ServerAutoCrystal.mc.player.getHealth() + ServerAutoCrystal.mc.player.getAbsorptionAmount())) continue;
            boolean bl = false;
            for (Player player : list) {
                float f3 = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()), endCrystal2, this.ignoreTerrain.getValue());
                if (f3 < this.getMinimumDamage(player, this.minimumDamage.getValue().floatValue()) && f3 < player.getHealth() + player.getAbsorptionAmount() && !(f3 * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()) || !(f3 > f) && !(f3 > player.getHealth() + player.getAbsorptionAmount())) continue;
                endCrystal = endCrystal2;
                f = f3;
                if (!(f3 > player.getHealth() + player.getAbsorptionAmount())) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
        }
        return endCrystal;
    }

    private PlaceTarget calculatePlacements(BlockPos blockPos) {
        if (!this.place.getValue()) {
            return null;
        }
        if (this.shouldPause("Place") || (this.autoSwitch.getValue().equalsIgnoreCase("None") || InventoryUtils.findHotbar(Items.END_CRYSTAL) == -1) && ServerAutoCrystal.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && ServerAutoCrystal.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return null;
        }
        List<Player> list = this.getPlayers();
        if (list.isEmpty()) {
            return null;
        }
        BlockPos blockPos2 = null;
        Player player = null;
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        float f = 0.0f;
        BlockPos blockPos3 = this.placeTarget == null ? null : this.placeTarget.getPosition();
        int n = 0;
        for (int i = 0; i < Night.WORLD_MANAGER.getRadius(Math.max(this.placeRange.getValue().doubleValue(), this.placeWallsRange.getValue().doubleValue())); ++i) {
            float f2;
            BlockPos blockPos4 = ServerAutoCrystal.mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
            if (ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos4)) > Mth.square((double)this.placeRange.getValue().doubleValue()) || !ServerAutoCrystal.mc.level.getWorldBorder().isWithinBounds(blockPos4) || ServerAutoCrystal.mc.level.getBlockState(blockPos4).getBlock() != Blocks.OBSIDIAN && ServerAutoCrystal.mc.level.getBlockState(blockPos4).getBlock() != Blocks.BEDROCK || !ServerAutoCrystal.mc.level.getBlockState(blockPos4.offset(0, 1, 0)).isAir() || this.placements.getValue().equalsIgnoreCase("Protocol") && !ServerAutoCrystal.mc.level.getBlockState(blockPos4.offset(0, 2, 0)).isAir() || !WorldUtils.canSeeBlock(blockPos4) && (this.raytrace.getValue() || ServerAutoCrystal.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos4)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue())) || ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(blockPos4.offset(0, 1, 0)), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal))) continue;
            List<Entity> list2 = ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(blockPos4.offset(0, 1, 0)), entity -> true).stream().filter(entity -> {
                EndCrystal endCrystal;
                return entity instanceof EndCrystal && (!this.placedCrystals.containsKey((endCrystal = (EndCrystal)entity).blockPosition().below()) || endCrystal.tickCount >= 20 - this.attackSpeed.getValue().intValue() + 15);
            }).toList();
            if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((f2 = DamageUtils.getCrystalDamage((Entity)ServerAutoCrystal.mc.player, null, blockPos4, blockPos, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && f2 > ServerAutoCrystal.mc.player.getHealth() + ServerAutoCrystal.mc.player.getAbsorptionAmount())) continue;
            boolean bl = false;
            for (Player player2 : list) {
                ++n;
                float f3 = DamageUtils.getCrystalDamage((Entity)player2, PositionUtils.extrapolate(player2, this.extrapolation.getValue().intValue()), blockPos4, blockPos, this.ignoreTerrain.getValue());
                if (f3 < this.getMinimumDamage(player2, this.minimumDamage.getValue().floatValue()) && f3 < player2.getHealth() + player2.getAbsorptionAmount() && !(f3 * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= player2.getHealth() + player2.getAbsorptionAmount())) continue;
                if (blockPos == null && !list2.isEmpty()) {
                    arrayList.add(list2.getFirst());
                    break;
                }
                float f4 = f3 + (blockPos4.equals((Object)blockPos3) ? 0.5f : 0.0f);
                if (!(f4 > f) && !(f3 > player2.getHealth() + player2.getAbsorptionAmount())) continue;
                blockPos2 = blockPos4;
                player = player2;
                f = f4;
                if (!(f3 > player2.getHealth() + player2.getAbsorptionAmount())) continue;
                bl = true;
                break;
            }
            if (bl) break;
        }
        if (blockPos2 == null) {
            return new PlaceTarget(null, null, arrayList, null, 0.0f, n);
        }
        return new PlaceTarget(blockPos2, player, arrayList, blockPos, f, n);
    }

    private float[] calculateRotations(Vec3 vec3) {
        float[] fArray = RotationUtils.getRotations(vec3);
        if (this.yawStep.getValue()) {
            float f;
            float f2 = Night.ROTATION_MANAGER.getServerYaw() - fArray[0];
            if (Math.abs(f2) > 180.0f) {
                f2 += f2 > 0.0f ? -360.0f : 360.0f;
            }
            float f3 = (float)(f2 > 0.0f ? -1 : 1) * this.yawStepThreshold.getValue().floatValue();
            fArray[0] = f = Math.abs(f2) > this.yawStepThreshold.getValue().floatValue() ? Night.ROTATION_MANAGER.getServerYaw() + f3 : fArray[0];
        }
        return fArray;
    }

    private boolean isEnemyAtMinePos(BlockPos blockPos) {
        if (blockPos == null || ServerAutoCrystal.mc.level == null) {
            return false;
        }
        AABB aABB = new AABB(blockPos).inflate(1.5, 2.0, 1.5);
        for (Player player : ServerAutoCrystal.mc.level.players()) {
            if (player.equals((Object)ServerAutoCrystal.mc.player) || EntityUtils.isGhost((Entity)player) || !player.isAlive() || Night.FRIEND_MANAGER != null && Night.FRIEND_MANAGER.contains(player.getName().getString()) || !player.getBoundingBox().intersects(aABB)) continue;
            return true;
        }
        return false;
    }

    private boolean isCrystalNearMinePos(EndCrystal endCrystal, BlockPos blockPos) {
        if (endCrystal == null || blockPos == null) {
            return false;
        }
        BlockPos blockPos2 = endCrystal.blockPosition().below();
        if (Math.abs(blockPos2.getX() - blockPos.getX()) <= 2 && Math.abs(blockPos2.getZ() - blockPos.getZ()) <= 2 && Math.abs(blockPos2.getY() - blockPos.getY()) <= 2) {
            return true;
        }
        return endCrystal.getBoundingBox().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= 9.0;
    }

    private boolean isAtActiveUnbrokenMinePos(EndCrystal endCrystal) {
        BlockPos blockPos;
        BlockPos blockPos2;
        if (endCrystal == null || ServerAutoCrystal.mc.level == null || ServerAutoCrystal.mc.player == null) {
            return false;
        }
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        SpeedMineModule speedMineModule2 = speedMineModule;
        if (speedMineModule == null || !speedMineModule.isToggled()) {
            return false;
        }
        SpeedMineModule.Action action = speedMineModule.getPrimary();
        if (action != null && action.getPosition() != null && this.isCrystalNearMinePos(endCrystal, blockPos2 = action.getPosition())) {
            return !this.attack.getValue() || !this.isEnemyAtMinePos(blockPos2);
        }
        SpeedMineModule.Secondary secondary = speedMineModule.getSecondary();
        if (secondary != null && secondary.getPosition() != null && this.isCrystalNearMinePos(endCrystal, blockPos = secondary.getPosition())) {
            return !this.attack.getValue() || !this.isEnemyAtMinePos(blockPos);
        }
        return false;
    }

    private void attack(EndCrystal endCrystal) {
        int n;
        if (endCrystal == null || !endCrystal.isAlive() || endCrystal.isRemoved()) {
            return;
        }
        if (this.isAtActiveUnbrokenMinePos(endCrystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float f = DamageUtils.getCrystalDamage((Entity)ServerAutoCrystal.mc.player, null, endCrystal, this.ignoreTerrain.getValue());
            if (f > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && f > ServerAutoCrystal.mc.player.getHealth() + ServerAutoCrystal.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        int n2 = ServerAutoCrystal.mc.player.getInventory().getSelectedSlot();
        int n3 = -1;
        if (!this.antiWeakness.getValue().equalsIgnoreCase("None") && ServerAutoCrystal.mc.player.hasEffect(MobEffects.WEAKNESS) && (n = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
            InventoryUtils.switchSlot(this.antiWeakness.getValue(), n, n2);
            n3 = n;
        }
        mc.getConnection().send((Packet)new ServerboundAttackPacket(endCrystal.getId()));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (n3 != -1) {
            InventoryUtils.switchBack(this.antiWeakness.getValue(), n3, n2);
        }
        this.attackedCrystals.put(endCrystal.getId(), System.currentTimeMillis());
        this.attackTimer.reset();
        ++this.totalAttacks;
    }

    private void place(BlockPos blockPos) {
        InteractionHand interactionHand = ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(blockPos);
        NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemOnPacket(interactionHand, new BlockHitResult(placementResult.hitVec, placementResult.direction, blockPos, false), n));
        switch (this.swing.getValue()) {
            case "Default": {
                ServerAutoCrystal.mc.player.swing(interactionHand);
                break;
            }
            case "Packet": {
                mc.getConnection().send((Packet)new ServerboundSwingPacket(interactionHand));
                break;
            }
            case "Mainhand": {
                ServerAutoCrystal.mc.player.swing(InteractionHand.MAIN_HAND);
                break;
            }
            case "Offhand": {
                ServerAutoCrystal.mc.player.swing(InteractionHand.OFF_HAND);
                break;
            }
            case "Both": {
                ServerAutoCrystal.mc.player.swing(InteractionHand.MAIN_HAND);
                ServerAutoCrystal.mc.player.swing(InteractionHand.OFF_HAND);
            }
        }
        this.placedCrystals.put(blockPos, System.currentTimeMillis());
        this.countedCrystals.put(blockPos, System.currentTimeMillis());
        this.placeTimer.reset();
        ++this.totalPlaces;
    }

    private List<Player> getPlayers() {
        ArrayList<Player> arrayList = new ArrayList<Player>();
        if (Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            arrayList.add((Player)ServerAutoCrystal.mc.player);
            return arrayList;
        }
        for (Player player : ServerAutoCrystal.mc.level.players()) {
            if (player == ServerAutoCrystal.mc.player || EntityUtils.isGhost((Entity)player) || !player.isAlive() || ServerAutoCrystal.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || Night.FRIEND_MANAGER.contains(player.getName().getString())) continue;
            boolean bl = player.getMainHandItem().getItem() == Items.END_CRYSTAL || player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            boolean bl2 = bl;
            if (this.ignoreNaked.getValue() && EntityUtils.isNaked(player) && !bl) continue;
            arrayList.add(player);
        }
        return arrayList;
    }

    private boolean isPearlPaused() {
        if (!this.pauseOnPearl.getValue()) {
            return false;
        }
        PhaseModule phaseModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(PhaseModule.class) : null;
        PhaseModule phaseModule2 = phaseModule;
        if (phaseModule != null && phaseModule.isToggled()) {
            return true;
        }
        KeyActionModule keyActionModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(KeyActionModule.class) : null;
        KeyActionModule keyActionModule2 = keyActionModule;
        if (keyActionModule != null && keyActionModule.isPearlActive()) {
            return true;
        }
        if (System.currentTimeMillis() - this.lastPearlThrowTime < 250L) {
            return true;
        }
        if (ServerAutoCrystal.mc.player != null) {
            boolean bl = ServerAutoCrystal.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL;
            boolean bl2 = bl;
            if (bl && (ServerAutoCrystal.mc.options.keyUse.isDown() || ServerAutoCrystal.mc.player.isUsingItem())) {
                return true;
            }
        }
        return false;
    }

    private boolean isXpPaused() {
        if (!this.pauseOnXP.getValue()) {
            return false;
        }
        KeyActionModule keyActionModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(KeyActionModule.class) : null;
        KeyActionModule keyActionModule2 = keyActionModule;
        if (keyActionModule != null && keyActionModule.isXpActive()) {
            return true;
        }
        if (System.currentTimeMillis() - this.lastXpThrowTime < 250L) {
            return true;
        }
        if (ServerAutoCrystal.mc.player != null) {
            boolean bl = ServerAutoCrystal.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || ServerAutoCrystal.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE;
            boolean bl2 = bl;
            if (bl && (ServerAutoCrystal.mc.options.keyUse.isDown() || ServerAutoCrystal.mc.player.isUsingItem())) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldPause(String string) {
        if (this.isPearlPaused()) {
            return true;
        }
        if (this.isXpPaused()) {
            return true;
        }
        boolean bl = this.whileEating.getValue().equalsIgnoreCase("None") || string.equalsIgnoreCase("Attack") && this.whileEating.getValue().equalsIgnoreCase("Place") || string.equalsIgnoreCase("Place") && this.whileEating.getValue().equalsIgnoreCase("Attack");
        return bl && ServerAutoCrystal.mc.player.isUsingItem();
    }

    private float getMinimumDamage(Player player, float f) {
        if (player == null) {
            return f;
        }
        if (this.chestBreak.getValue() && ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(player.blockPosition()).inflate(1.0), entity -> true).stream().anyMatch(entity -> {
            ItemEntity itemEntity;
            return entity instanceof ItemEntity && (itemEntity = (ItemEntity)entity).getItem().getItem() == Items.OBSIDIAN && itemEntity.getItem().getCount() >= 8 && itemEntity.tickCount <= 2 + Night.SERVER_MANAGER.getPingDelay() + (20 - this.placeSpeed.getValue().intValue());
        }) && !ServerAutoCrystal.mc.level.getEntities((Entity)null, new AABB(ServerAutoCrystal.mc.player.blockPosition()).inflate(1.0), entity -> true).stream().anyMatch(entity -> {
            ItemEntity itemEntity;
            return entity instanceof ItemEntity && (itemEntity = (ItemEntity)entity).getItem().getItem() == Items.OBSIDIAN && itemEntity.getItem().getCount() >= 8 && itemEntity.tickCount <= 2 + Night.SERVER_MANAGER.getPingDelay() + (20 - this.placeSpeed.getValue().intValue());
        })) {
            return 2.0f;
        }
        if (this.facePlaceMode.getValue().equalsIgnoreCase("None")) {
            return f;
        }
        if (this.facePlaceSpeed.getValue().equalsIgnoreCase("Normal") || this.facePlaceTimer.hasTimeElapsed(this.facePlaceDelay.getValue().longValue() * 50L)) {
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Always")) {
                return Math.min(f, 2.0f);
            }
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && this.healthPlace.getValue() && player.getHealth() + player.getAbsorptionAmount() <= this.health.getValue().floatValue()) {
                return Math.min(f, 2.0f);
            }
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && this.armorPlace.getValue()) {
                for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                    ItemStack itemStack = player.getItemBySlot(equipmentSlot);
                    if (itemStack.isEmpty() || itemStack.get(DataComponents.EQUIPPABLE) == null || Math.round((double)(itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0 / (double)itemStack.getMaxDamage()) > (long)this.percentage.getValue().intValue()) continue;
                    return Math.min(f, 2.0f);
                }
            }
        }
        return f;
    }

    @Generated
    public Player getTarget() {
        return this.target;
    }

    @Generated
    public String getCalculationDamage() {
        return this.calculationDamage;
    }

    public static class PlaceTarget {
        private BlockPos position;
        private Player player;
        private List<Entity> obstructions;
        private BlockPos exception;
        private float damage;
        private int calculations;

        public PlaceTarget clone() {
            return new PlaceTarget(this.position, this.player, this.obstructions, this.exception, this.damage, this.calculations);
        }

        @Generated
        public BlockPos getPosition() {
            return this.position;
        }

        @Generated
        public Player getPlayer() {
            return this.player;
        }

        @Generated
        public List<Entity> getObstructions() {
            return this.obstructions;
        }

        @Generated
        public BlockPos getException() {
            return this.exception;
        }

        @Generated
        public float getDamage() {
            return this.damage;
        }

        @Generated
        public int getCalculations() {
            return this.calculations;
        }

        @Generated
        public PlaceTarget(BlockPos blockPos, Player player, List<Entity> list, BlockPos blockPos2, float f, int n) {
            this.position = blockPos;
            this.player = player;
            this.obstructions = list;
            this.exception = blockPos2;
            this.damage = f;
            this.calculations = n;
        }
    }
}

