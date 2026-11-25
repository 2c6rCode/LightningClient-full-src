/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
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
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.combat;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.DestroyBlockEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.GameLoopEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.CrystalPlacementHelper;
import lightning.modules.impl.combat.SuicideModule;
import lightning.modules.impl.movement.PhaseModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.modules.impl.player.KeyActionModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.EspShader;
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
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="AutoCrystal", description="Automatically places and attacks crystals to annihilate your opponents.", category=Module.Category.COMBAT)
public class AutoCrystalModule
extends Module {
    public CategorySetting attackCategory = new CategorySetting("Attack", "The category for settings related to attacking crystals.");
    public BooleanSetting attack = new BooleanSetting("Attack", "Enabled", "Automatically attacks crystals that are deemed safe.", new CategorySetting.Visibility(this.attackCategory), true);
    public NumberSetting attackSpeed = new NumberSetting("AttackSpeed", "Speed", "The speed at which crystals will be attacked.", new CategorySetting.Visibility(this.attackCategory), Float.valueOf(20.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting attackRange = new NumberSetting("AttackRange", "Range", "The maximum distance at which crystals will be attacked.", new CategorySetting.Visibility(this.attackCategory), 4.5, 0.0, 8.0);
    public NumberSetting attackWallsRange = new NumberSetting("AttackWallsRange", "WallsRange", "The maximum distance at which crystals will be attacked through walls.", new CategorySetting.Visibility(this.attackCategory), 4.5, 0.0, 8.0);
    public ModeSetting antiWeakness = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", new CategorySetting.Visibility(this.attackCategory), "None", new String[]{"None", "Normal", "Silent"});
    public BooleanSetting instant = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", new CategorySetting.Visibility(this.attackCategory), true);
    public BooleanSetting inhibit = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", new CategorySetting.Visibility(this.attackCategory), true);
    public CategorySetting placeCategory = new CategorySetting("Place", "The category for settings related to placing crystals.");
    public BooleanSetting place = new BooleanSetting("Place", "Enabled", "Automatically places crystals on positions that are deemed safe and lethal enough.", new CategorySetting.Visibility(this.placeCategory), true);
    public NumberSetting placeSpeed = new NumberSetting("PlaceSpeed", "Speed", "The speed at which crystals will be placed.", new CategorySetting.Visibility(this.placeCategory), Float.valueOf(10.0f), Float.valueOf(0.1f), Float.valueOf(20.0f));
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "Range", "The maximum distance at which positions will be placed on.", new CategorySetting.Visibility(this.placeCategory), 4.5, 0.0, 8.0);
    public NumberSetting placeWallsRange = new NumberSetting("PlaceWallsRange", "WallsRange", "The maximum distance at which positions will be placed on through walls.", new CategorySetting.Visibility(this.placeCategory), 4.5, 0.0, 8.0);
    public ModeSetting placements = new ModeSetting("Placements", "The version of the game that will be used for crystal placement calculations.", new CategorySetting.Visibility(this.placeCategory), "Native", new String[]{"Native", "Protocol"});
    public BooleanSetting blockDestruction = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", new CategorySetting.Visibility(this.placeCategory), true);
    public ModeSetting autoSwitch = new ModeSetting("Switch", "Automatically switches to a crystal if you aren't currently holding one.", new CategorySetting.Visibility(this.placeCategory), "None", new String[]{"None", "Normal", "Silent", "AltSwap"});
    public BooleanSetting swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were holding before the module started switching to crystals, once the module is disabled.", new ModeSetting.Visibility(this.autoSwitch, "Normal"), false);
    public NumberSetting swapDelay = new NumberSetting("SwapDelay", "Delay", "The delay in ticks after swapping before placing or attacking crystals.", new CategorySetting.Visibility(this.placeCategory), 0, 0, 20);
    public CategorySetting miscellaneousCategory = new CategorySetting("Miscellaneous", "The category for all miscellaneous settings.");
    public ModeSetting sequential = new ModeSetting("Sequential", "The sequence that the module's processes will be run in.", new CategorySetting.Visibility(this.miscellaneousCategory), "Strong", new String[]{"None", "Strict", "Strong"});
    public ModeSetting rotate = new ModeSetting("Rotate", "Automatically rotates to the crystal whenever attacking or placing.", new CategorySetting.Visibility(this.miscellaneousCategory), "Normal", new String[]{"None", "Normal", "Packet", "Silent"});
    public ModeSetting swing = new ModeSetting("Swing", "The hand that will be used for swinging.", new CategorySetting.Visibility(this.miscellaneousCategory), "Default", new String[]{"Default", "None", "Packet", "Mainhand", "Offhand", "Both"});
    public BooleanSetting yawStep = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public NumberSetting yawStepThreshold = new NumberSetting("YawStepThreshold", "Threshold", "The threshold in order for yaw to be modified.", new BooleanSetting.Visibility(this.yawStep, true), 75, 1, 180);
    public BooleanSetting raytrace = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public BooleanSetting losDebug = new BooleanSetting("LosDebug", "Debug", "Prints once a second why the block you are aiming at passes/fails the place gates (LOS steps + damage).", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public ModeSetting targetMode = new ModeSetting("Target", "Which player to target when multiple are in range -- narrowing this to one collapses the per-position player scan from O(players) to O(1), the actual fix for multitarget FPS drops.", new CategorySetting.Visibility(this.miscellaneousCategory), "All", new String[]{"All", "Nearest", "Farthest", "Health"});
    public NumberSetting extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", new CategorySetting.Visibility(this.miscellaneousCategory), (Number)0, (Number)0, (Number)20);
    public NumberSetting enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", new CategorySetting.Visibility(this.miscellaneousCategory), (Number)10.0, (Number)0.0, (Number)24.0);
    public BooleanSetting ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignores naked players (even if wearing only elytra), but still targets them if they are holding an End Crystal.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public BooleanSetting chestBreak = new BooleanSetting("ChestBreak", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public BooleanSetting mineIgnore = new BooleanSetting("MineIgnore", "Pre-places a crystal on the block SpeedMine is about to break, and detonates it the instant that block is gone.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public NumberSetting mineIgnoreTicks = new NumberSetting("MineIgnoreTicks", "Tick", "How many ticks before the block breaks to place the crystal.", new BooleanSetting.Visibility(this.mineIgnore, true), 3, 0, 10);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
    public BooleanSetting pauseOnSecondaryMine = new BooleanSetting("PauseOnSecondaryMine", "Pauses AutoCrystal for one tick while SpeedMine's secondary block is about to break, so it finishes first.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
    public BooleanSetting awaitMine = new BooleanSetting("AwaitMine", "Pauses AutoCrystal when AutoMine selects a target block so AutoMine can lock and mine properly.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public NumberSetting awaitMineTicks = new NumberSetting("AwaitMineTicks", "Ticks", "The number of ticks to wait for AutoMine to lock target.", new BooleanSetting.Visibility(this.awaitMine, true), 2, 1, 10);
    public BooleanSetting pauseOnPearl = new BooleanSetting("PauseOnPearl", "Pauses AutoCrystal when Phase is active, KeyAction pearl is used, or when throwing an ender pearl manually.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
    public BooleanSetting pauseOnXP = new BooleanSetting("PauseOnXP", "Pauses AutoCrystal when throwing experience bottles manually or when KeyAction XP is active.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public BooleanSetting gameLoop = new BooleanSetting("GameLoop", "Runs the module on loop instead of ticks.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
    public NumberSetting loopDelay = new NumberSetting("LoopDelay", "The delay that has to be waited out before running the module again.", new BooleanSetting.Visibility(this.gameLoop, true), (Number)50, (Number)0, (Number)1000);
    public ModeSetting whileEating = new ModeSetting("WhileEating", "Places and attacks crystal while eating or using items.", new CategorySetting.Visibility(this.miscellaneousCategory), "Both", new String[]{"None", "Attack", "Place", "Both"});
    public CategorySetting predictionCategory = new CategorySetting("Prediction", "The category for settings related to attack prediction.");
    public BooleanSetting godSync = new BooleanSetting("GodSync", "Makes the attacking way faster by predicting entity IDs.", new CategorySetting.Visibility(this.predictionCategory), false);
    public NumberSetting predictions = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", new CategorySetting.Visibility(this.predictionCategory), (Number)10, (Number)1, (Number)20);
    public NumberSetting offset = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", new CategorySetting.Visibility(this.predictionCategory), (Number)0, (Number)0, (Number)2);
    public ModeSetting godSwing = new ModeSetting("GodSwing", "Swing", "The swinging that will be done for each predicted attack.", new CategorySetting.Visibility(this.predictionCategory), "Normal", new String[]{"None", "Normal", "Strict"});
    public BooleanSetting fast = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", new CategorySetting.Visibility(this.predictionCategory), false);
    public BooleanSetting antiKick = new BooleanSetting("AntiKick", "Prevents you from getting kicked by attacking invalid entity IDs.", new CategorySetting.Visibility(this.predictionCategory), false);
    public NumberSetting kickThreshold = new NumberSetting("KickThreshold", "Threshold", "The tick threshold for the kick prevention.", new BooleanSetting.Visibility(this.antiKick, true), 5, 1, 10);
    public CategorySetting facePlaceCategory = new CategorySetting("Faceplace", "The category for settings relating to faceplacing.");
    public ModeSetting facePlaceMode = new ModeSetting("FaceplaceMode", "Mode", "The checks that will be done in order to faceplace.", new CategorySetting.Visibility(this.facePlaceCategory), "Dynamic", new String[]{"None", "Dynamic", "Always"});
    public ModeSetting facePlaceSpeed = new ModeSetting("FaceplaceSpeed", "Speed", "The speed that players will be faceplaced at.", new CategorySetting.Visibility(this.facePlaceCategory), "Normal", new String[]{"Normal", "Custom"});
    public NumberSetting facePlaceDelay = new NumberSetting("FaceplaceDelay", "Delay", "The ticks that have to be waited for before faceplacing again.", new ModeSetting.Visibility(this.facePlaceSpeed, "Custom"), 11, 0, 20);
    public BooleanSetting healthPlace = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", new ModeSetting.Visibility(this.facePlaceMode, "Dynamic"), true);
    public NumberSetting health = new NumberSetting("Health", "The health that the target needs to be at in order for the module to start faceplacing.", new BooleanSetting.Visibility(this.healthPlace, true), (Number)Float.valueOf(8.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(36.0f));
    public BooleanSetting armorPlace = new BooleanSetting("ArmorPlace", "Whether or not to faceplace when the target's armor is low on durability.", new ModeSetting.Visibility(this.facePlaceMode, "Dynamic"), true);
    public NumberSetting percentage = new NumberSetting("Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", new BooleanSetting.Visibility(this.armorPlace, true), (Number)10, (Number)1, (Number)100);
    public CategorySetting basePlaceCategory = new CategorySetting("BasePlace", "The category for settings related to placing base obsidian blocks for elevated crystal attacks.");
    public BooleanSetting basePlace = new BooleanSetting("BasePlace", "Enabled", "Automatically places obsidian base blocks under/next to targets.", new CategorySetting.Visibility(this.basePlaceCategory), true);
    public ModeSetting basePlaceSwitch = new ModeSetting("BaseSwitch", "Switch", "Switch mode for placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), "Silent", new String[]{"None", "Normal", "Silent", "AltSwap"});
    public BooleanSetting basePlaceAirPlace = new BooleanSetting("BaseAirPlace", "AirPlace", "Allows placing base blocks in the air without neighboring blocks.", new CategorySetting.Visibility(this.basePlaceCategory), false);
    public BooleanSetting basePlaceStrictDirection = new BooleanSetting("BaseStrictDirection", "StrictDirection", "Only places using directions that face you.", new CategorySetting.Visibility(this.basePlaceCategory), false);
    public NumberSetting basePlaceMinDamage = new NumberSetting("BaseDamage", "Min Damage", "The minimum damage a crystal on the base must deal to the target.", new CategorySetting.Visibility(this.basePlaceCategory), 6.0, 0.0, 36.0);
    public NumberSetting basePlaceRatio = new NumberSetting("BaseRatio", "Ratio", "Damage ratio required over existing standard crystal placement to trigger baseplace.", new CategorySetting.Visibility(this.basePlaceCategory), 1.5, 1.0, 3.0);
    public NumberSetting basePlaceRadius = new NumberSetting("BaseRadius", "Scan Radius", "The horizontal radius around the target to scan for base placements.", new CategorySetting.Visibility(this.basePlaceCategory), 3.0, 1.0, 6.0);
    public NumberSetting basePlaceRange = new NumberSetting("BaseRange", "Range", "The maximum distance at which base blocks will be placed.", new CategorySetting.Visibility(this.basePlaceCategory), 4.5, 0.0, 6.0);
    public NumberSetting basePlaceDelay = new NumberSetting("BaseDelay", "Delay", "Delay in ticks between placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), 0, 0, 10);
    public BooleanSetting basePlaceRotate = new BooleanSetting("BaseRotate", "Rotate", "Rotates when placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), false);
    public CategorySetting damageCategory = new CategorySetting("Damage", "The category for settings related to damage calculations.");
    public NumberSetting minimumDamage = new NumberSetting("MinimumDamage", "Minimum", "The minimum damage that has to be dealt to enemies.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
    public NumberSetting maximumSelfDamage = new NumberSetting("MaximumSelfDamage", "MaximumSelf", "The maximum damage that can be dealt to you by crystals.", new CategorySetting.Visibility(this.damageCategory), 10.0, 0.0, 36.0);
    public NumberSetting lethalMultiplier = new NumberSetting("LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", new CategorySetting.Visibility(this.damageCategory), (Number)Float.valueOf(1.5f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(4.0f));
    public BooleanSetting antiSuicide = new BooleanSetting("AntiSuicide", "Prevents crystals from accidentally killing you when you're low on health.", new CategorySetting.Visibility(this.damageCategory), true);
    public BooleanSetting ignoreTerrain = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", new CategorySetting.Visibility(this.damageCategory), true);
    public CategorySetting renderCategory = new CategorySetting("Render", "Contains all of the settings relating to position rendering.");
    public ModeSetting animationMode = new ModeSetting("Animation", "The animation that will be applied to the rendering.", new CategorySetting.Visibility(this.renderCategory), "Static", new String[]{"Static", "Slide"});
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the auto crystal render.", new CategorySetting.Visibility(this.renderCategory), "Fade", new String[]{"Fade", "Shrink"});
    public NumberSetting duration = new NumberSetting("Duration", "The duration for the place render.", new CategorySetting.Visibility(this.renderCategory), (Number)300, (Number)0, (Number)1000);
    public NumberSetting slideSmoothness = new NumberSetting("Smoothness", "The smoothness for the slide while place position is changing.", new CategorySetting.Visibility(this.renderCategory), (Number)1, (Number)0, (Number)20);
    public ModeSetting renderMode = new ModeSetting("RenderMode", "The rendering that will be applied to the target position.", new CategorySetting.Visibility(this.renderCategory), "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColorUp = new ColorSetting("FillColorUp", "The color that will be used for the fill gradiant upper part rendering.", new ModeSetting.Visibility(this.renderMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting fillColorDown = new ColorSetting("FillColorDown", "The color that will be used for the fill gradiant lower part rendering.", new ModeSetting.Visibility(this.renderMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColorUp = new ColorSetting("OutlineColorUp", "The color that will be used for the outline gradiant upper part rendering.", new ModeSetting.Visibility(this.renderMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public ColorSetting outlineColorDown = new ColorSetting("OutlineColorDown", "The color that will be used for the outline gradiant lower part rendering.", new ModeSetting.Visibility(this.renderMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting renderDamage = new BooleanSetting("RenderDamage", "Damage", "Renders the damage that the position will do to the opponent.", new CategorySetting.Visibility(this.renderCategory), false);
    public BooleanSetting icon = new BooleanSetting("Icon", "Renders a customizable crystal icon on the rendered position.", new CategorySetting.Visibility(this.renderCategory), false);
    public NumberSetting iconScale = new NumberSetting("IconScale", "The scaling that will be applied to the crystal icon rendering.", new BooleanSetting.Visibility(this.icon, true), (Number)3, (Number)1, (Number)5);
    public NumberSetting iconRadius = new NumberSetting("IconRadius", "The difference between the outer circle and the inner circle.", new BooleanSetting.Visibility(this.icon, true), (Number)Float.valueOf(2.0f), (Number)Float.valueOf(0.0f), (Number)Float.valueOf(5.0f));
    public ColorSetting iconColor = new ColorSetting("IconColor", "The color that will be used for the crystal icon rendering.", new BooleanSetting.Visibility(this.icon, true), ColorUtils.getDefaultColor());
    private static final String[] SHADER_MODES = EspShader.MODES;
    private static final String[] SHADER_ACTIVE_MODES = Arrays.copyOfRange(SHADER_MODES, 1, SHADER_MODES.length);
    public ModeSetting shader = new ModeSetting("Shader", "The animated shader that will be drawn on the target position instead of a flat color.", new CategorySetting.Visibility(this.renderCategory), "None", SHADER_MODES);
    public NumberSetting shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "The speed at which the shader animates.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f));
    public NumberSetting shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "The opacity of the shader rendering.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), 100, 0, 100);
    public BooleanSetting shaderDistanceScaling = new BooleanSetting("ShaderDistanceScaling", "DistanceScaling", "Scales the shader pattern by your distance to the position.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), false);
    public NumberSetting shaderStep = new NumberSetting("ShaderStep", "Step", "The size of the gradient bands.", new ModeSetting.Visibility(this.shader, "Gradient"), Float.valueOf(50.0f), Float.valueOf(0.1f), Float.valueOf(200.0f));
    public ColorSetting shaderColor1 = new ColorSetting("ShaderColor1", "The first gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
    public ColorSetting shaderColor2 = new ColorSetting("ShaderColor2", "The second gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public ColorSetting shaderColor3 = new ColorSetting("ShaderColor3", "The third gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
    public ColorSetting shaderColor4 = new ColorSetting("ShaderColor4", "The fourth gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(0, 0, 255, 255), false, false));
    public ColorSetting shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The color that the glow shader will be tinted with.", new ModeSetting.Visibility(this.shader, "Glowing"), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<?> pendingCalc = null;
    private volatile boolean asyncLoopActive = false;
    private volatile Set<BlockPos> latestReservedPlacements = Set.of();
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
    private final Timer losDebugTimer = new Timer();
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
    private PlaceTarget mineTargetSecondary = null;
    private BlockPos mineIgnoreMinedPos = null;
    private BlockPos mineIgnorePlacedPos = null;
    private String calculationTime = "0.00ms";
    private int calculationCount = 0;
    private String calculationDamage = "0.00";
    private final Counter crystalCounter = new Counter();
    private int crystalsPerSecond = 0;
    private int highestID = -100000;
    private int kickTicks = 0;
    private volatile List<Player> cachedPlayers = Collections.emptyList();
    private volatile long lastPlayerCacheTime = 0L;
    private static final long PLAYER_CACHE_DURATION = 50L;
    private int basePlaceTicks = 0;
    private int savedSlot = -1;
    private static final float SECONDARY_PAUSE_PROGRESS = 0.85f;
    private long lastPearlThrowTime = 0L;
    private long lastXpThrowTime = 0L;

    private boolean isDead() {
        if (AutoCrystalModule.mc.player == null || AutoCrystalModule.mc.level == null) {
            return true;
        }
        if (!AutoCrystalModule.mc.player.isAlive() || AutoCrystalModule.mc.player.isDeadOrDying() || AutoCrystalModule.mc.player.getHealth() <= 0.0f) {
            return true;
        }
        return AutoCrystalModule.mc.gui.screen() instanceof DeathScreen;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        AutoShopModule autoShopModule = Night.MODULE_MANAGER.getModule(AutoShopModule.class);
        if (autoShopModule != null && autoShopModule.isRunning()) {
        }
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
        int currentSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (this.lastSelectedSlot != -1 && this.lastSelectedSlot != currentSlot) {
            this.swapTimer.reset();
        }
        this.lastSelectedSlot = currentSlot;
        long attackClearDelay = 50L;
        this.attackedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, attackClearDelay));
        long placeClearDelay = Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, 500L) + (long)((20.0f - this.attackSpeed.getValue().floatValue()) * 50.0f);
        this.placedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > placeClearDelay);
        this.countedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - (Long)entry.getValue() > Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, attackClearDelay));
        this.crystalsPerSecond = this.crystalCounter.getCount();
        if (this.losDebug.getValue() && this.losDebugTimer.hasTimeElapsed(1000L)) {
            this.losDebugTimer.reset();
            this.losDebug();
        }
        if (this.gameLoop.getValue()) {
            return;
        }
        this.run();
    }

    /*
     * Unable to fully structure code
     */
    private void losDebug() {
    }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent updateMovementEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead()) {
            return;
        }
        this.mineIgnoreTick();
        this.latestReservedPlacements = Set.copyOf(Night.WORLD_MANAGER.getReservedPlacements());
        if (this.asynchronous.getValue()) {
            if (!this.asyncLoopActive || this.pendingCalc == null || this.pendingCalc.isDone() || this.pendingCalc.isCancelled()) {
                this.asyncLoopActive = true;
                this.pendingCalc = this.executor.submit(this::asyncLoopStep);
            }
        } else {
            this.asyncLoopActive = false;
            this.runCalculation(this.latestReservedPlacements, false);
        }
    }

    private void asyncLoopStep() {
        if (!(this.asyncLoopActive && this.asynchronous.getValue() && this.isToggled())) {
            this.asyncLoopActive = false;
            return;
        }
        try {
            if (AutoCrystalModule.mc.player != null && AutoCrystalModule.mc.level != null) {
                this.runCalculation(this.latestReservedPlacements, true);
            }
        }
        catch (Throwable throwable) {
        }
        finally {
            if (this.asyncLoopActive && this.asynchronous.getValue() && this.isToggled()) {
                try {
                    Thread.sleep(5L);
                    this.pendingCalc = this.executor.submit(this::asyncLoopStep);
                }
                catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    this.asyncLoopActive = false;
                }
                catch (Throwable throwable) {
                    this.asyncLoopActive = false;
                }
            } else {
                this.asyncLoopActive = false;
            }
        }
    }

    private void runCalculation(Set<BlockPos> reservedPlacements, boolean calcMineTargets) {
        long startTimeNs = System.nanoTime();
        try {
            this.attackTarget = this.calculateCrystals();
            this.placeTarget = this.calculatePlacements(null, reservedPlacements);
        }
        catch (Throwable throwable) {
            return;
        }
        long elapsedNs = System.nanoTime() - startTimeNs;
        this.calculationTime = new DecimalFormat("0.00").format((double)elapsedNs / 1000000.0) + "ms";
        this.calculationCount = this.placeTarget == null ? 0 : this.placeTarget.getCalculations();
        this.calculationDamage = this.placeTarget == null ? "0.00" : new DecimalFormat("0.00").format(this.placeTarget.getDamage());
        this.target = this.placeTarget == null ? null : this.placeTarget.getPlayer();
        if (this.blockDestruction.getValue() && calcMineTargets) {
            SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
            BlockPos primaryMinePos = null;
            if (speedMineModule.getPrimary() != null && speedMineModule.getPrimary().isMining()) {
                primaryMinePos = speedMineModule.getPrimary().getPosition();
            }
            if (primaryMinePos != null) {
                this.mineTarget = this.calculatePlacements(primaryMinePos, reservedPlacements);
            }
            BlockPos secondaryMinePos = null;
            if (speedMineModule.farReach.getValue()) {
                if (speedMineModule.getSecondary() != null && speedMineModule.getSecondary().isMining()) {
                    secondaryMinePos = speedMineModule.getSecondary().getPosition();
                }
            } else if (speedMineModule.getSecondary() != null && speedMineModule.getSecondary().isMining()) {
                secondaryMinePos = speedMineModule.getSecondary().getPosition();
            }
            if (secondaryMinePos != null) {
                this.mineTargetSecondary = this.calculatePlacements(secondaryMinePos, reservedPlacements);
            }
        }
    }

    @SubscribeEvent
    public void onGameLoop(GameLoopEvent gameLoopEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead()) {
            return;
        }
        if (!this.gameLoop.getValue()) {
            return;
        }
        if (!this.loopTimer.hasTimeElapsed(this.loopDelay.getValue().longValue())) {
            return;
        }
        this.loopTimer.reset();
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
        if (this.isDead()) {
            return;
        }
        if (this.pauseForSecondaryMine() || this.pauseForAutoMine()) {
            return;
        }
        if (this.isPearlPaused() || this.isXpPaused()) {
            return;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
        if (this.basePlace.getValue()) {
            if (this.basePlaceTicks < this.basePlaceDelay.getValue().intValue()) {
                ++this.basePlaceTicks;
            } else if (this.executeBasePlace()) {
                this.basePlaceTicks = 0;
            }
        }
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

    private boolean pauseForSecondaryMine() {
        if (!this.pauseOnSecondaryMine.getValue()) {
            return false;
        }
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        if (speedMineModule == null) {
            return false;
        }
        if (speedMineModule.farReach.getValue()) {
            SpeedMineModule.Secondary secondary = speedMineModule.getSecondary();
            return secondary != null && secondary.isMining() && secondary.getProgress() >= 0.85f;
        }
        SpeedMineModule.Secondary secondary = speedMineModule.getSecondary();
        return secondary != null && secondary.isMining() && secondary.getProgress() >= 0.85f;
    }

    private boolean pauseForAutoMine() {
        if (!this.awaitMine.getValue()) {
            return false;
        }
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        if (speedMineModule == null) {
            return false;
        }
        SpeedMineModule.Action action = speedMineModule.getPrimary();
        return action != null && action.isMining() && action.getTicks() < this.awaitMineTicks.getValue().intValue();
    }

    private boolean isPearlPaused() {
        if (!this.pauseOnPearl.getValue()) {
            return false;
        }
        PhaseModule phaseModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(PhaseModule.class) : null;
        if (phaseModule != null && phaseModule.isToggled()) {
            return true;
        }
        KeyActionModule keyActionModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(KeyActionModule.class) : null;
        if (keyActionModule != null && keyActionModule.isPearlActive()) {
            return true;
        }
        if (System.currentTimeMillis() - this.lastPearlThrowTime < 250L) {
            return true;
        }
        if (AutoCrystalModule.mc.player != null) {
            boolean holdingPearl = AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL;
            if (holdingPearl && (AutoCrystalModule.mc.options.keyUse.isDown() || AutoCrystalModule.mc.player.isUsingItem())) {
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
        if (keyActionModule != null && keyActionModule.isXpActive()) {
            return true;
        }
        if (System.currentTimeMillis() - this.lastXpThrowTime < 250L) {
            return true;
        }
        if (AutoCrystalModule.mc.player != null) {
            boolean holdingXp = AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE;
            if (holdingXp && (AutoCrystalModule.mc.options.keyUse.isDown() || AutoCrystalModule.mc.player.isUsingItem())) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onUpdateMovement$POST(UpdateMovementEvent.Post post) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead()) {
            return;
        }
        if (this.attackRunnable != null) {
            this.attackRunnable.run();
        }
        if (this.placeRunnable != null) {
            this.placeRunnable.run();
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent entitySpawnEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead() || this.getPlayers().isEmpty()) {
            return;
        }
        if (this.shouldPause("Attack")) {
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
        if (this.isAtActiveUnbrokenMinePos(endCrystal)) {
            return;
        }
        BlockPos basePos = endCrystal.blockPosition().below();
        if (!this.placedCrystals.containsKey(basePos)) {
            basePos = BlockPos.containing((double)endCrystal.getX(), (double)(endCrystal.getY() - 0.5), (double)endCrystal.getZ());
        }
        if (!this.placedCrystals.containsKey(basePos)) {
            return;
        }
        if (endCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            return;
        }
        if (!AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(endCrystal.blockPosition())) {
            return;
        }
        boolean checkLos = this.raytrace.getValue() || endCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
        if (checkLos && !WorldUtils.canSee((Entity)endCrystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal, this.ignoreTerrain.getValue());
            if (selfDamage > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        float[] rotations = RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition()));
        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), rotations);
        }
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition())), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        this.attack(endCrystal);
        this.attackedSequentially = true;
        if (this.sequential.getValue().equalsIgnoreCase("Strong")) {
            this.placeCrystals(true);
            this.placeRunnable = null;
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent destroyBlockEvent) {
        PlaceTarget selectedTarget;
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (this.isDead() || this.getPlayers().isEmpty()) {
            return;
        }
        if (this.shouldPause("Place")) {
            return;
        }
        this.kickTicks = 0;
        if (this.mineIgnore.getValue() && destroyBlockEvent.getPosition() != null && destroyBlockEvent.getPosition().equals((Object)this.mineIgnoreMinedPos)) {
            this.mineIgnoreDetonate();
        }
        if (!this.blockDestruction.getValue()) {
            return;
        }
        if (!this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        BlockPos minedPos = destroyBlockEvent.getPosition();
        if (minedPos == null) {
            return;
        }
        int crystalSlot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int prevSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        boolean switched = false;
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && crystalSlot == -1 && AutoCrystalModule.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        PlaceTarget primaryMineTarget = this.mineTarget == null ? null : this.mineTarget.clone();
        PlaceTarget secondaryMineTarget = this.mineTargetSecondary == null ? null : this.mineTargetSecondary.clone();
        if (primaryMineTarget != null && primaryMineTarget.getPosition() != null && minedPos.equals((Object)primaryMineTarget.getException())) {
            selectedTarget = primaryMineTarget;
        } else if (secondaryMineTarget != null && secondaryMineTarget.getPosition() != null && minedPos.equals((Object)secondaryMineTarget.getException())) {
            selectedTarget = secondaryMineTarget;
        } else if (!this.asynchronous.getValue()) {
            selectedTarget = this.calculatePlacements(minedPos);
        } else {
            selectedTarget = primaryMineTarget != null ? primaryMineTarget : secondaryMineTarget;
        }
        if (selectedTarget == null || selectedTarget.getPosition() == null || selectedTarget.getDamage() <= 0.0f) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        BlockPos targetPos = selectedTarget.getPosition();
        if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(targetPos)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        Night.RENDER_MANAGER.setRenderPosition(targetPos);
        boolean checkLos = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(targetPos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
        if (checkLos && !WorldUtils.canSeeBlock(targetPos)) {
            return;
        }
        EndCrystal endCrystal = null;
        for (Entity entity : AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(targetPos.above()), e -> true)) {
            if (!(entity instanceof EndCrystal)) continue;
            endCrystal = (EndCrystal)entity;
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
            float[] rotations = RotationUtils.getRotations((Entity)endCrystal);
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                Night.ROTATION_MANAGER.legacyRotate(rotations, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), rotations);
            }
            this.attack(endCrystal);
            return;
        }
        CrystalPlacementHelper.PlacementResult placement = CrystalPlacementHelper.getVisiblePlacement(targetPos);
        Vec3 hitVec = placement != null && placement.hitVec != null ? placement.hitVec : Vec3.atCenterOf(targetPos).add(0.0, 0.5, 0.0);
        float[] rotations = this.calculateRotations(hitVec);
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(rotations, this, Night.ROTATION_MANAGER.getLegacyModulePriority(this) + 1);
        }
        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), rotations);
        }
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                this.savedSlot = prevSlot;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), crystalSlot, prevSlot);
            switched = true;
        }
        this.place(targetPos);
        if (switched) {
            InventoryUtils.switchBack(this.autoSwitch.getValue(), crystalSlot, prevSlot);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        if (AutoCrystalModule.mc.player == null || AutoCrystalModule.mc.level == null) {
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
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (packetSendEvent.getPacket() instanceof ServerboundSetCarriedItemPacket) {
            this.swapTimer.reset();
        }
        if (packetSendEvent.getPacket() instanceof ServerboundUseItemPacket && AutoCrystalModule.mc.player != null) {
            if (AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
                this.lastPearlThrowTime = System.currentTimeMillis();
            } else if (AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE) {
                this.lastXpThrowTime = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
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
    public void onClientConnect(ClientConnectEvent clientConnectEvent) {
        if (PingBypassFlags.isPingBypassActive()) {
            return;
        }
        this.highestID = -100000;
    }

    @Override
    public void onDisable() {
        this.asyncLoopActive = false;
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
        this.cachedPlayers.clear();
        this.lastPlayerCacheTime = 0L;
    }

    @Override
    public String getMetaData() {
        return this.calculationTime + ", " + this.calculationCount + ", " + this.calculationDamage + ", " + this.crystalsPerSecond;
    }

    public boolean hasLiveCrystalNear(BlockPos blockPos) {
        if (blockPos == null || AutoCrystalModule.mc.level == null) {
            return false;
        }
        return !AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(blockPos).inflate(1.0), entity -> entity instanceof EndCrystal && entity.isAlive()).isEmpty();
    }

    private boolean isEnemyAtMinePos(BlockPos blockPos) {
        if (blockPos == null || AutoCrystalModule.mc.level == null) {
            return false;
        }
        AABB aABB = new AABB(blockPos).inflate(1.5, 2.0, 1.5);
        for (Player player : AutoCrystalModule.mc.level.players()) {
            if (player.equals((Object)AutoCrystalModule.mc.player) || EntityUtils.isGhost((Entity)player) || !player.isAlive() || Night.FRIEND_MANAGER != null && Night.FRIEND_MANAGER.contains(player.getName().getString()) || !player.getBoundingBox().intersects(aABB)) continue;
            return true;
        }
        return false;
    }

    private boolean isCrystalNearMinePos(EndCrystal endCrystal, BlockPos blockPos) {
        if (endCrystal == null || blockPos == null) {
            return false;
        }
        BlockPos crystalBasePos = endCrystal.blockPosition().below();
        if (Math.abs(crystalBasePos.getX() - blockPos.getX()) <= 2 && Math.abs(crystalBasePos.getZ() - blockPos.getZ()) <= 2 && Math.abs(crystalBasePos.getY() - blockPos.getY()) <= 2) {
            return true;
        }
        return endCrystal.getBoundingBox().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= 9.0;
    }

    private boolean isAtActiveUnbrokenMinePos(EndCrystal endCrystal) {
        BlockPos secondaryMinePos;
        BlockPos primaryMinePos;
        if (endCrystal == null || AutoCrystalModule.mc.level == null || AutoCrystalModule.mc.player == null) {
            return false;
        }
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(SpeedMineModule.class) : null;
        if (speedMineModule == null || !speedMineModule.isToggled()) {
            return false;
        }
        SpeedMineModule.Action action = speedMineModule.getPrimary();
        if (action != null && action.getPosition() != null && this.isCrystalNearMinePos(endCrystal, primaryMinePos = action.getPosition())) {
            return !this.attack.getValue() || !this.isEnemyAtMinePos(primaryMinePos);
        }
        SpeedMineModule.Secondary secondary = speedMineModule.getSecondary();
        if (secondary != null && secondary.getPosition() != null && this.isCrystalNearMinePos(endCrystal, secondaryMinePos = secondary.getPosition())) {
            return !this.attack.getValue() || !this.isEnemyAtMinePos(secondaryMinePos);
        }
        return false;
    }

    private void attackCrystals() {
        boolean checkLos;
        if (this.isDead()) {
            return;
        }
        if (this.shouldPause("Attack")) {
            return;
        }
        if (this.getPlayers().isEmpty()) {
            this.attackTarget = null;
            return;
        }
        EndCrystal targetCrystal = null;
        boolean bypassInhibit = false;
        PlaceTarget currentPlaceTarget = this.placeTarget;
        if (currentPlaceTarget != null && currentPlaceTarget.getPosition() == null && currentPlaceTarget.getObstructions() != null && !currentPlaceTarget.getObstructions().isEmpty()) {
            for (Entity entity : currentPlaceTarget.getObstructions()) {
                float selfDamage;
                EndCrystal obstructingCrystal;
                if (!(entity instanceof EndCrystal) || !(obstructingCrystal = (EndCrystal)entity).isAlive() || this.isAtActiveUnbrokenMinePos(obstructingCrystal) || obstructingCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(obstructingCrystal.blockPosition())) continue;
                checkLos = this.raytrace.getValue() || obstructingCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
                if (checkLos && !WorldUtils.canSee((Entity)obstructingCrystal) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), obstructingCrystal, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
                targetCrystal = obstructingCrystal;
                bypassInhibit = true;
                break;
            }
        }
        if (targetCrystal == null) {
            targetCrystal = this.attackTarget;
        }
        if (targetCrystal == null) {
            List<Player> targetPlayers = this.selectTargets(this.getPlayers());
            float bestTargetDamage = -1.0f;
            for (Entity worldEntity : AutoCrystalModule.mc.level.entitiesForRendering()) {
                float selfDamage;
                EndCrystal candidateCrystal;
                if (!(worldEntity instanceof EndCrystal) || !(candidateCrystal = (EndCrystal)worldEntity).isAlive() || this.isAtActiveUnbrokenMinePos(candidateCrystal) || this.inhibit.getValue() && this.attackedCrystals.containsKey(candidateCrystal.getId()) && System.currentTimeMillis() - this.attackedCrystals.get(candidateCrystal.getId()) < Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, 100L)) continue;
                BlockPos basePos = candidateCrystal.blockPosition().below();
                if (!this.placedCrystals.containsKey(basePos)) {
                    basePos = BlockPos.containing((double)candidateCrystal.getX(), (double)(candidateCrystal.getY() - 0.5), (double)candidateCrystal.getZ());
                }
                if (!this.placedCrystals.containsKey(basePos) || candidateCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(candidateCrystal.blockPosition())) continue;
                checkLos = this.raytrace.getValue() || candidateCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
                if (checkLos && !WorldUtils.canSee((Entity)candidateCrystal) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), candidateCrystal, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
                float maxEnemyDamage = 0.0f;
                for (Player player : targetPlayers) {
                    float enemyDamage = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()), candidateCrystal, this.ignoreTerrain.getValue());
                    if (!(enemyDamage > maxEnemyDamage)) continue;
                    maxEnemyDamage = enemyDamage;
                }
                if (!(maxEnemyDamage > bestTargetDamage)) continue;
                bestTargetDamage = maxEnemyDamage;
                targetCrystal = candidateCrystal;
                bypassInhibit = true;
            }
        }
        if (targetCrystal == null) {
            return;
        }
        EndCrystal crystalToAttack = targetCrystal;
        if (this.isAtActiveUnbrokenMinePos(crystalToAttack)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), crystalToAttack, this.ignoreTerrain.getValue());
            if (selfDamage > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        float[] rotations = this.calculateRotations(Vec3.atCenterOf((Vec3i)crystalToAttack.blockPosition()));
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(rotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        if (this.swapDelay.getValue().intValue() > 0 && !this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) {
            return;
        }
        if (!this.attackTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.attackSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        Entity entity = AutoCrystalModule.mc.level.getEntity(crystalToAttack.getId());
        String rejectReason = null;
        if (entity == null) {
            rejectReason = "entity-gone";
        } else if (!(entity instanceof EndCrystal)) {
            rejectReason = "not-end-crystal";
        } else if (!((EndCrystal)entity).isAlive()) {
            rejectReason = "dead";
        } else if (this.inhibit.getValue() && !bypassInhibit && this.attackedCrystals.containsKey(entity.getId())) {
            rejectReason = "inhibit";
        } else if (entity.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            rejectReason = "range";
        } else if (!AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
            rejectReason = "border";
        } else {
            boolean needLos = this.raytrace.getValue() || entity.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
            if (needLos && !WorldUtils.canSee(entity)) {
                rejectReason = "cannot-see";
            }
        }
        if (rejectReason != null) {
            return;
        }
        this.attackRunnable = () -> {
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)crystalToAttack.blockPosition())));
            }
            this.attack(crystalToAttack);
        };
    }

    private void placeCrystals(boolean sequentialPlace) {
        if (!this.isDead() && !this.shouldPause("Place")) {
            PlaceTarget targetPlacement = this.placeTarget == null ? null : this.placeTarget.clone();
            if (targetPlacement != null && targetPlacement.getPosition() != null) {
                if (targetPlacement.getPlayer() != null && targetPlacement.getPlayer().isAlive() && !targetPlacement.getPlayer().isDeadOrDying() && !(targetPlacement.getPlayer().getHealth() <= 0.0f)) {
                    int crystalSlot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
                    int prevSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
                    if (this.autoSwitch.getValue().equalsIgnoreCase("None") || crystalSlot != -1 || AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                        BlockPos blockPos = targetPlacement.getPosition();
                        if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
                            Night.RENDER_MANAGER.setRenderPosition(null);
                        } else {
                            Night.RENDER_MANAGER.setRenderPosition(blockPos);
                            if (AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(blockPos) && (AutoCrystalModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN || AutoCrystalModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK) && AutoCrystalModule.mc.level.getBlockState(blockPos.offset(0, 1, 0)).isAir() && (!this.placements.getValue().equalsIgnoreCase("Protocol") || AutoCrystalModule.mc.level.getBlockState(blockPos.offset(0, 2, 0)).isAir())) {
                                boolean checkLos = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
                                if (!(checkLos && !WorldUtils.canSeeBlock(blockPos) || AutoCrystalModule.mc.level.getEntities((Entity)null, this.getCrystalPlacementBox(blockPos), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal)))) {
                                    EndCrystal existingCrystal = null;
                                    Iterator<Entity> iterator = AutoCrystalModule.mc.level.getEntities((Entity)null, this.getCrystalPlacementBox(blockPos), entity -> entity instanceof EndCrystal && entity.isAlive()).iterator();
                                    if (iterator.hasNext()) {
                                        existingCrystal = (EndCrystal)iterator.next();
                                    }
                                    if (existingCrystal != null) {
                                        if (this.attack.getValue() && !this.isAtActiveUnbrokenMinePos(existingCrystal)) {
                                            if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                                                float selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), existingCrystal, this.ignoreTerrain.getValue());
                                                if (selfDamage <= this.maximumSelfDamage.getValue().floatValue() && (!this.antiSuicide.getValue() || selfDamage <= AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) {
                                                    float[] rotations = this.calculateRotations(Vec3.atCenterOf((Vec3i)existingCrystal.blockPosition()));
                                                    if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                        Night.ROTATION_MANAGER.legacyRotate(rotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                    }
                                                    if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                        Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)existingCrystal.blockPosition())));
                                                    }
                                                    this.attack(existingCrystal);
                                                }
                                            } else {
                                                float[] rotations = this.calculateRotations(Vec3.atCenterOf((Vec3i)existingCrystal.blockPosition()));
                                                if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                    Night.ROTATION_MANAGER.legacyRotate(rotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                }
                                                if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                    Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)existingCrystal.blockPosition())));
                                                }
                                                this.attack(existingCrystal);
                                            }
                                        }
                                    } else {
                                        for (Entity rendEntity : AutoCrystalModule.mc.level.entitiesForRendering()) {
                                            if (!(rendEntity instanceof EndCrystal)) continue;
                                            EndCrystal cry = (EndCrystal)rendEntity;
                                            if (!cry.isAlive() || this.isAtActiveUnbrokenMinePos(cry)) continue;
                                            BlockPos cryBasePos = cry.blockPosition().below();
                                            if (!this.placedCrystals.containsKey(cryBasePos)) {
                                                cryBasePos = BlockPos.containing((double)cry.getX(), (double)(cry.getY() - 0.5), (double)cry.getZ());
                                            }
                                            if (!this.placedCrystals.containsKey(cryBasePos)) continue;
                                            double distSq = cry.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition());
                                            boolean withinAttackRange = distSq <= Mth.square((double)this.attackRange.getValue().doubleValue());
                                            boolean needCryLos = this.raytrace.getValue() || distSq > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
                                            boolean canAttackCry = withinAttackRange && (!needCryLos || WorldUtils.canSee((Entity)cry));
                                            if (canAttackCry) {
                                                if (this.attack.getValue()) {
                                                    if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                                                        float selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), cry, this.ignoreTerrain.getValue());
                                                        if (selfDamage <= this.maximumSelfDamage.getValue().floatValue() && (!this.antiSuicide.getValue() || selfDamage <= AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) {
                                                            float[] rotations = this.calculateRotations(Vec3.atCenterOf((Vec3i)cry.blockPosition()));
                                                            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                                Night.ROTATION_MANAGER.legacyRotate(rotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                            }
                                                            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)cry.blockPosition())));
                                                            }
                                                            this.attack(cry);
                                                        }
                                                    } else {
                                                        float[] rotations = this.calculateRotations(Vec3.atCenterOf((Vec3i)cry.blockPosition()));
                                                        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                            Night.ROTATION_MANAGER.legacyRotate(rotations, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                        }
                                                        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)cry.blockPosition())));
                                                        }
                                                        this.attack(cry);
                                                    }
                                                }
                                                return;
                                            }
                                            if (!cryBasePos.equals((Object)blockPos)) continue;
                                            return;
                                        }
                                        CrystalPlacementHelper.PlacementResult placeRes = CrystalPlacementHelper.getVisiblePlacement(blockPos);
                                        Vec3 hitVec = placeRes != null && placeRes.hitVec != null ? placeRes.hitVec : Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.5, 0.0);
                                        float[] rot = this.calculateRotations(hitVec);
                                        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                            Night.ROTATION_MANAGER.legacyRotate(rot, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                        }
                                        if ((this.swapDelay.getValue().intValue() <= 0 || this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) && this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
                                            if (!sequentialPlace && this.placedSequentially) {
                                                this.placedSequentially = false;
                                            } else {
                                                this.placeRunnable = () -> this.executePlaceCrystals(hitVec, prevSlot, crystalSlot, blockPos);
                                                if (sequentialPlace) {
                                                    this.placeRunnable.run();
                                                    this.placeRunnable = null;
                                                    this.placedSequentially = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    this.placeTarget = null;
                    this.target = null;
                    Night.RENDER_MANAGER.setRenderPosition(null);
                }
            } else {
                Night.RENDER_MANAGER.setRenderPosition(null);
            }
        }
    }

    private EndCrystal calculateCrystals() {
        if (!this.attack.getValue()) {
            return null;
        }
        if (this.shouldPause("Attack")) {
            return null;
        }
        List<Player> targetPlayers = this.selectTargets(this.getPlayers());
        if (targetPlayers.isEmpty()) {
            return null;
        }
        EndCrystal bestCrystal = null;
        float bestDamage = 0.0f;
        for (Entity entity : AutoCrystalModule.mc.level.entitiesForRendering()) {
            float selfDamage;
            EndCrystal candidateCrystal;
            if (!(entity instanceof EndCrystal) || !(candidateCrystal = (EndCrystal)entity).isAlive() || this.isAtActiveUnbrokenMinePos(candidateCrystal) || this.inhibit.getValue() && this.attackedCrystals.containsKey(entity.getId()) || candidateCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(candidateCrystal.blockPosition())) continue;
            boolean checkLos = this.raytrace.getValue() || candidateCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
            if (checkLos && !WorldUtils.canSee((Entity)candidateCrystal) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), candidateCrystal, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
            boolean isLethal = false;
            for (Player player : targetPlayers) {
                float targetDamage = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()), candidateCrystal, this.ignoreTerrain.getValue());
                if (targetDamage < this.getMinimumDamage(player, this.minimumDamage.getValue().floatValue()) && targetDamage < player.getHealth() + player.getAbsorptionAmount() && !(targetDamage * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()) || !(targetDamage > bestDamage) && !(targetDamage > player.getHealth() + player.getAbsorptionAmount())) continue;
                bestCrystal = candidateCrystal;
                bestDamage = targetDamage;
                if (!(targetDamage > player.getHealth() + player.getAbsorptionAmount())) continue;
                isLethal = true;
                break;
            }
            if (!isLethal) continue;
        }
        return bestCrystal;
    }

    private PlaceTarget calculatePlacements(BlockPos blockPos) {
        return this.calculatePlacements(blockPos, Night.WORLD_MANAGER.getReservedPlacements());
    }

    private PlaceTarget calculatePlacements(BlockPos exceptionPos, Set<BlockPos> reservedPlacements) {
        if (!this.place.getValue()) {
            return null;
        }
        if (!this.shouldPause("Place") && (!this.autoSwitch.getValue().equalsIgnoreCase("None") && InventoryUtils.findHotbar(Items.END_CRYSTAL) != -1 || AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL)) {
            List<Player> targetPlayers = this.selectTargets(this.getPlayers());
            if (targetPlayers.isEmpty()) {
                return null;
            }
            BlockPos bestPos = null;
            Player bestPlayer = null;
            float bestScore = 0.0f;
            float bestDamage = 0.0f;
            BlockPos currentPos = this.placeTarget == null ? null : this.placeTarget.getPosition();
            ArrayList<Entity> obstructions = new ArrayList<Entity>();
            int calcCount = 0;
            block0: for (int i = 0; i < Night.WORLD_MANAGER.getRadius(Math.max(this.placeRange.getValue().doubleValue(), this.placeWallsRange.getValue().doubleValue())); ++i) {
                float selfDamage;
                boolean checkLos;
                BlockPos candidatePos = AutoCrystalModule.mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
                if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)candidatePos)) > Mth.square((double)this.placeRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(candidatePos) || AutoCrystalModule.mc.level.getBlockState(candidatePos).getBlock() != Blocks.OBSIDIAN && AutoCrystalModule.mc.level.getBlockState(candidatePos).getBlock() != Blocks.BEDROCK || !AutoCrystalModule.mc.level.getBlockState(candidatePos.offset(0, 1, 0)).isAir() || this.placements.getValue().equalsIgnoreCase("Protocol") && !AutoCrystalModule.mc.level.getBlockState(candidatePos.offset(0, 2, 0)).isAir()) continue;
                checkLos = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)candidatePos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
                if (checkLos && !WorldUtils.canSeeBlock(candidatePos)) continue;
                AABB crystalBox = new AABB((double)(candidatePos.getX() - 1), (double)(candidatePos.getY() + 1), (double)(candidatePos.getZ() - 1), (double)(candidatePos.getX() + 2), (double)(candidatePos.getY() + 3), (double)(candidatePos.getZ() + 2));
                if (reservedPlacements.stream().anyMatch(reservedPos -> crystalBox.intersects(new AABB(reservedPos)))) continue;
                List<Entity> entitiesInBox = AutoCrystalModule.mc.level.getEntities((Entity)null, this.getCrystalPlacementBox(candidatePos), entity -> entity.isAlive() && !(entity instanceof ExperienceOrb));
                boolean blocked = false;
                ArrayList<EndCrystal> blockingCrystals = new ArrayList<EndCrystal>();
                for (Entity entityInBox : entitiesInBox) {
                    if (!(entityInBox instanceof EndCrystal)) {
                        blocked = true;
                        break;
                    }
                    EndCrystal endCrystal = (EndCrystal)entityInBox;
                    if (this.isAtActiveUnbrokenMinePos(endCrystal)) {
                        blocked = true;
                        break;
                    }
                    BlockPos crystalBasePos = endCrystal.blockPosition().below();
                    if (!this.placedCrystals.containsKey(crystalBasePos)) {
                        crystalBasePos = BlockPos.containing((double)endCrystal.getX(), (double)(endCrystal.getY() - 0.5), (double)endCrystal.getZ());
                    }
                    if (this.placedCrystals.containsKey(crystalBasePos) && endCrystal.tickCount < 20 - this.attackSpeed.getValue().intValue() + 15) continue;
                    blockingCrystals.add(endCrystal);
                }
                if (blocked) continue;
                double maxTargetDistSq = Mth.square((double)10.0);
                boolean hasNearTarget = false;
                for (Player candidateTarget : targetPlayers) {
                    if (!(candidateTarget.distanceToSqr((double)candidatePos.getX() + 0.5, (double)candidatePos.getY() + 1.0, (double)candidatePos.getZ() + 0.5) <= maxTargetDistSq)) continue;
                    hasNearTarget = true;
                    break;
                }
                if (!hasNearTarget || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), candidatePos, exceptionPos, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
                for (Player candidateTarget : targetPlayers) {
                    ++calcCount;
                    float targetDamage = DamageUtils.getCrystalDamage((Entity)candidateTarget, PositionUtils.extrapolate(candidateTarget, this.extrapolation.getValue().intValue()), candidatePos, exceptionPos, this.ignoreTerrain.getValue());
                    if (targetDamage < this.getMinimumDamage(candidateTarget, this.minimumDamage.getValue().floatValue()) && targetDamage < candidateTarget.getHealth() + candidateTarget.getAbsorptionAmount() && !(targetDamage * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= candidateTarget.getHealth() + candidateTarget.getAbsorptionAmount()) || targetDamage <= 0.0f) continue;
                    if (exceptionPos == null && !blockingCrystals.isEmpty()) {
                        obstructions.add((Entity)blockingCrystals.getFirst());
                        continue block0;
                    }
                    float bias = candidatePos.equals((Object)currentPos) ? 0.5f : 0.0f;
                    float score = targetDamage + bias;
                    if (!(score > bestScore)) continue;
                    bestPos = candidatePos;
                    bestPlayer = candidateTarget;
                    bestScore = score;
                    bestDamage = targetDamage;
                }
            }
            return bestPos == null ? new PlaceTarget(null, null, obstructions, null, 0.0f, calcCount) : new PlaceTarget(bestPos, bestPlayer, obstructions, exceptionPos, bestDamage, calcCount);
        }
        return null;
    }

    private float[] calculateRotations(Vec3 targetPos) {
        float[] rotations = RotationUtils.getRotations(targetPos);
        if (this.yawStep.getValue()) {
            float diff = Night.ROTATION_MANAGER.getServerYaw() - rotations[0];
            if (Math.abs(diff) > 180.0f) {
                diff += diff > 0.0f ? -360.0f : 360.0f;
            }
            float step = (float)(diff > 0.0f ? -1 : 1) * this.yawStepThreshold.getValue().floatValue();
            rotations[0] = Math.abs(diff) > this.yawStepThreshold.getValue().floatValue() ? Night.ROTATION_MANAGER.getServerYaw() + step : rotations[0];
        }
        return rotations;
    }

    private void attack(EndCrystal endCrystal) {
        int swordSlot;
        if (endCrystal == null || !endCrystal.isAlive() || endCrystal.isRemoved()) {
            return;
        }
        if (this.isAtActiveUnbrokenMinePos(endCrystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal, this.ignoreTerrain.getValue());
            if (selfDamage > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        int prevSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        int switchedSlot = -1;
        if (!this.antiWeakness.getValue().equalsIgnoreCase("None") && AutoCrystalModule.mc.player.hasEffect(MobEffects.WEAKNESS) && (swordSlot = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
            InventoryUtils.switchSlot(this.antiWeakness.getValue(), swordSlot, prevSlot);
            switchedSlot = swordSlot;
        }
        mc.getConnection().send((Packet)new ServerboundAttackPacket(endCrystal.getId()));
        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (switchedSlot != -1) {
            InventoryUtils.switchBack(this.antiWeakness.getValue(), switchedSlot, prevSlot);
        }
        this.attackedCrystals.put(endCrystal.getId(), System.currentTimeMillis());
        this.attackTimer.reset();
        ++this.totalAttacks;
    }

    private void place(BlockPos blockPos) {
        InteractionHand interactionHand = AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(blockPos);
        NetworkUtils.sendSequencedPacket(sequence -> new ServerboundUseItemOnPacket(interactionHand, new BlockHitResult(placementResult.hitVec, placementResult.direction, blockPos, false), sequence));
        switch (this.swing.getValue()) {
            case "Default": {
                AutoCrystalModule.mc.player.swing(interactionHand);
                break;
            }
            case "Packet": {
                mc.getConnection().send((Packet)new ServerboundSwingPacket(interactionHand));
                break;
            }
            case "Mainhand": {
                AutoCrystalModule.mc.player.swing(InteractionHand.MAIN_HAND);
                break;
            }
            case "Offhand": {
                AutoCrystalModule.mc.player.swing(InteractionHand.OFF_HAND);
                break;
            }
            case "Both": {
                AutoCrystalModule.mc.player.swing(InteractionHand.MAIN_HAND);
                AutoCrystalModule.mc.player.swing(InteractionHand.OFF_HAND);
            }
        }
        this.placedCrystals.put(blockPos, System.currentTimeMillis());
        this.countedCrystals.put(blockPos, System.currentTimeMillis());
        this.placeTimer.reset();
        ++this.totalPlaces;
    }

    private void mineIgnoreTick() {
        if (!this.mineIgnore.getValue()) {
            this.mineIgnoreMinedPos = null;
            this.mineIgnorePlacedPos = null;
            return;
        }
        SpeedMineModule.Action action = Night.MODULE_MANAGER.getModule(SpeedMineModule.class).getPrimary();
        if (action == null || !action.isMining()) {
            this.mineIgnoreMinedPos = null;
            this.mineIgnorePlacedPos = null;
            return;
        }
        BlockPos blockPos = action.getPosition();
        if (!blockPos.equals((Object)this.mineIgnoreMinedPos)) {
            this.mineIgnoreMinedPos = blockPos;
            this.mineIgnorePlacedPos = null;
        }
        if (this.mineIgnorePlacedPos != null) {
            return;
        }
        if (action.getTicksRemaining() > this.mineIgnoreTicks.getValue().intValue()) {
            return;
        }
        this.mineIgnoreTryPlace(blockPos);
    }

    private void mineIgnoreTryPlace(BlockPos minedPos) {
        if (this.shouldPause("Place")) {
            return;
        }
        if (!this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        List<Player> targetPlayers = this.selectTargets(this.getPlayers());
        if (targetPlayers.isEmpty()) {
            return;
        }
        boolean enemyNearMinedPos = false;
        for (Player targetPlayer : targetPlayers) {
            if (targetPlayer.equals((Object)AutoCrystalModule.mc.player)) continue;
            BlockPos targetPos = targetPlayer.blockPosition();
            if (Math.abs(minedPos.getX() - targetPos.getX()) > 1 || Math.abs(minedPos.getZ() - targetPos.getZ()) > 1 || Math.abs(minedPos.getY() - targetPos.getY()) > 1) continue;
            enemyNearMinedPos = true;
            break;
        }
        if (!enemyNearMinedPos) {
            return;
        }
        ArrayList<BlockPos> candidatePositions = new ArrayList<BlockPos>();
        candidatePositions.add(minedPos.above());
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            candidatePositions.add(minedPos.relative(dir).above());
        }
        int crystalSlot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int prevSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && crystalSlot == -1 && AutoCrystalModule.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        Iterator<BlockPos> iterator = candidatePositions.iterator();
        while (iterator.hasNext()) {
            float selfDamage;
            BlockPos crystalPos = iterator.next();
            BlockPos basePos = crystalPos.below();
            if (AutoCrystalModule.mc.level.getBlockState(basePos).getBlock() != Blocks.OBSIDIAN && AutoCrystalModule.mc.level.getBlockState(basePos).getBlock() != Blocks.BEDROCK || !AutoCrystalModule.mc.level.getBlockState(crystalPos).isAir() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)crystalPos)) > Mth.square((double)this.placeRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(crystalPos)) continue;
            boolean checkLos = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)crystalPos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
            if (checkLos && !WorldUtils.canSeeBlock(basePos) || AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(crystalPos), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal))) continue;
            AABB crystalBox = new AABB((double)(crystalPos.getX() - 1), (double)crystalPos.getY(), (double)(crystalPos.getZ() - 1), (double)(crystalPos.getX() + 2), (double)(crystalPos.getY() + 2), (double)(crystalPos.getZ() + 2));
            if (Night.WORLD_MANAGER.getReservedPlacements().stream().anyMatch(reservedPos -> crystalBox.intersects(new AABB(reservedPos))) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, null, crystalPos, null, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
            boolean dealsEnoughDamage = false;
            for (Player enemy : targetPlayers) {
                float enemyDamage;
                if (enemy.equals((Object)AutoCrystalModule.mc.player) || !((enemyDamage = DamageUtils.getCrystalDamage((Entity)enemy, PositionUtils.extrapolate(enemy, this.extrapolation.getValue().intValue()), crystalPos, minedPos, this.ignoreTerrain.getValue())) >= this.getMinimumDamage(enemy, this.minimumDamage.getValue().floatValue()))) continue;
                dealsEnoughDamage = true;
                break;
            }
            if (!dealsEnoughDamage) continue;
            CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(crystalPos);
            Vec3 hitVec = placementResult != null && placementResult.hitVec != null ? placementResult.hitVec : Vec3.atCenterOf((Vec3i)crystalPos).add(0.0, 0.5, 0.0);
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(hitVec), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(hitVec));
            }
            boolean switched = false;
            if (AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                    this.savedSlot = prevSlot;
                }
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), crystalSlot, prevSlot);
                switched = true;
            }
            this.place(crystalPos);
            if (switched) {
                InventoryUtils.switchBack(this.autoSwitch.getValue(), crystalSlot, prevSlot);
            }
            this.mineIgnorePlacedPos = crystalPos;
            return;
        }
    }

    private void mineIgnoreDetonate() {
        if (this.shouldPause("Attack")) {
            return;
        }
        BlockPos blockPos = this.mineIgnorePlacedPos;
        this.mineIgnoreMinedPos = null;
        this.mineIgnorePlacedPos = null;
        if (blockPos == null) {
            return;
        }
        if (!this.attack.getValue()) {
            return;
        }
        for (Entity entity : AutoCrystalModule.mc.level.entitiesForRendering()) {
            EndCrystal endCrystal;
            if (!(entity instanceof EndCrystal) || !(endCrystal = (EndCrystal)entity).isAlive() || !endCrystal.blockPosition().below().equals((Object)blockPos)) continue;
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition())), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition())));
            }
            this.attack(endCrystal);
            return;
        }
    }

    private List<Player> getPlayers() {
        if (Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            return List.of(AutoCrystalModule.mc.player);
        }
        long nowMs = System.currentTimeMillis();
        if (nowMs - this.lastPlayerCacheTime < 50L) {
            return this.cachedPlayers;
        }
        ArrayList<Player> players = new ArrayList<Player>();
        for (Player player : AutoCrystalModule.mc.level.players()) {
            if (player == AutoCrystalModule.mc.player || EntityUtils.isGhost((Entity)player) || !player.isAlive() || AutoCrystalModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || Night.FRIEND_MANAGER.contains(player.getName().getString())) continue;
            boolean holdingCrystal = player.getMainHandItem().getItem() == Items.END_CRYSTAL || player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            if (this.ignoreNaked.getValue() && EntityUtils.isNaked(player) && !holdingCrystal) continue;
            players.add(player);
        }
        this.cachedPlayers = players;
        this.lastPlayerCacheTime = nowMs;
        return players;
    }

    private List<Player> selectTargets(List<Player> players) {
        if (players.size() <= 1 || this.targetMode.getValue().equalsIgnoreCase("All")) {
            return players;
        }
        Player bestPlayer = null;
        double bestScore = 0.0;
        for (Player candidate : players) {
            double score;
            switch (this.targetMode.getValue()) {
                case "Nearest": {
                    score = -AutoCrystalModule.mc.player.distanceToSqr((Entity)candidate);
                    break;
                }
                case "Farthest": {
                    score = AutoCrystalModule.mc.player.distanceToSqr((Entity)candidate);
                    break;
                }
                default: {
                    score = -(candidate.getHealth() + candidate.getAbsorptionAmount());
                }
            }
            if (bestPlayer != null && !(score > bestScore)) continue;
            bestPlayer = candidate;
            bestScore = score;
        }
        return List.of(bestPlayer);
    }

    private boolean shouldPause(String actionType) {
        if (this.isPearlPaused()) {
            return true;
        }
        if (this.isXpPaused()) {
            return true;
        }
        if (this.awaitMine.getValue() && this.pauseForAutoMine()) {
            return true;
        }
        boolean pauseWhileEating = this.whileEating.getValue().equalsIgnoreCase("None") || actionType.equalsIgnoreCase("Attack") && this.whileEating.getValue().equalsIgnoreCase("Place") || actionType.equalsIgnoreCase("Place") && this.whileEating.getValue().equalsIgnoreCase("Attack");
        return pauseWhileEating && EntityUtils.isEating();
    }

    private float getMinimumDamage(Player player, float minDamage) {
        if (player == null) {
            return minDamage;
        }
        if (this.chestBreak.getValue() && AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(player.blockPosition()).inflate(1.0), entity -> true).stream().anyMatch(entity -> {
            ItemEntity itemEntity;
            return entity instanceof ItemEntity && (itemEntity = (ItemEntity)entity).getItem().getItem() == Items.OBSIDIAN && itemEntity.getItem().getCount() >= 8 && itemEntity.tickCount <= 2 + Night.SERVER_MANAGER.getPingDelay() + (20 - this.placeSpeed.getValue().intValue());
        }) && !AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(AutoCrystalModule.mc.player.blockPosition()).inflate(1.0), entity -> true).stream().anyMatch(entity -> {
            ItemEntity itemEntity;
            return entity instanceof ItemEntity && (itemEntity = (ItemEntity)entity).getItem().getItem() == Items.OBSIDIAN && itemEntity.getItem().getCount() >= 8 && itemEntity.tickCount <= 2 + Night.SERVER_MANAGER.getPingDelay() + (20 - this.placeSpeed.getValue().intValue());
        })) {
            return 2.0f;
        }
        if (this.facePlaceMode.getValue().equalsIgnoreCase("None")) {
            return minDamage;
        }
        if (this.facePlaceSpeed.getValue().equalsIgnoreCase("Normal") || this.facePlaceTimer.hasTimeElapsed(this.facePlaceDelay.getValue().longValue() * 50L)) {
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Always")) {
                return Math.min(minDamage, 2.0f);
            }
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && this.healthPlace.getValue() && player.getHealth() + player.getAbsorptionAmount() <= this.health.getValue().floatValue()) {
                return Math.min(minDamage, 2.0f);
            }
            if (this.facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && this.armorPlace.getValue()) {
                for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                    ItemStack itemStack = player.getItemBySlot(equipmentSlot);
                    if (itemStack.isEmpty() || itemStack.get(DataComponents.EQUIPPABLE) == null || Math.round((double)(itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0 / (double)itemStack.getMaxDamage()) > (long)this.percentage.getValue().intValue()) continue;
                    return Math.min(minDamage, 2.0f);
                }
            }
        }
        return minDamage;
    }

    private boolean executeBasePlace() {
        boolean placed;
        BlockState blockState;
        if (this.isDead()) {
            return false;
        }
        if (!this.basePlace.getValue()) {
            return false;
        }
        if (this.shouldPause("Place")) {
            return false;
        }
        Player targetPlayer = this.target;
        if (targetPlayer == null) {
            double bestDistSq = Double.MAX_VALUE;
            for (Player candidate : AutoCrystalModule.mc.level.players()) {
                if (EntityUtils.isGhost((Entity)candidate) || candidate == AutoCrystalModule.mc.player || !candidate.isAlive() || Night.FRIEND_MANAGER.contains(candidate.getName().getString())) continue;
                double distSq = AutoCrystalModule.mc.player.distanceToSqr((Entity)candidate);
                if (!(distSq <= Mth.square((double)this.enemyRange.getValue().doubleValue())) || !(distSq < bestDistSq)) continue;
                bestDistSq = distSq;
                targetPlayer = candidate;
            }
        }
        if (targetPlayer == null) {
            return false;
        }
        PlaceTarget currentPlace = this.placeTarget;
        float currentDamage = currentPlace != null && currentPlace.getPosition() != null ? currentPlace.getDamage() : 0.0f;
        if (currentDamage >= targetPlayer.getHealth() + targetPlayer.getAbsorptionAmount()) {
            return false;
        }
        BlockPos targetFootPos = PositionUtils.getFlooredPosition((Entity)targetPlayer);
        double scanRadius = this.basePlaceRadius.getValue().doubleValue();
        int radiusCeil = (int)Math.ceil(scanRadius);
        int maxY = targetFootPos.getY() - 1;
        int minY = targetFootPos.getY() - 3;
        LinkedHashSet<BlockPos> candidateSet = new LinkedHashSet<BlockPos>();
        for (int i = maxY; i >= minY; --i) {
            for (int j = -radiusCeil; j <= radiusCeil; ++j) {
                for (int k = -radiusCeil; k <= radiusCeil; ++k) {
                    if ((double)(j * j + k * k) > scanRadius * scanRadius) continue;
                    candidateSet.add(new BlockPos(targetFootPos.getX() + j, i, targetFootPos.getZ() + k));
                }
            }
        }
        BlockPos bestBasePos = null;
        float bestBaseDamage = 0.0f;
        double bestScore = Double.MAX_VALUE;
        for (BlockPos candidatePos : candidateSet) {
            double score;
            float targetDamage;
            float selfDamage;
            if (!this.isBaseCandidateValid(candidatePos) || (blockState = AutoCrystalModule.mc.level.getBlockState(candidatePos)).getBlock() == Blocks.OBSIDIAN || blockState.getBlock() == Blocks.BEDROCK || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((selfDamage = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), candidatePos, null, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && selfDamage > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) || (targetDamage = DamageUtils.getCrystalDamage((Entity)targetPlayer, PositionUtils.extrapolate(targetPlayer, this.extrapolation.getValue().intValue()), candidatePos, null, this.ignoreTerrain.getValue())) < this.basePlaceMinDamage.getValue().floatValue() || targetDamage < this.getMinimumDamage(targetPlayer, this.minimumDamage.getValue().floatValue()) && targetDamage < targetPlayer.getHealth() + targetPlayer.getAbsorptionAmount() && !(targetDamage * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= targetPlayer.getHealth() + targetPlayer.getAbsorptionAmount()) || currentDamage > 0.0f && targetDamage < currentDamage * this.basePlaceRatio.getValue().floatValue() || !((score = (double)(-targetDamage) * 10.0 + AutoCrystalModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)candidatePos)) + targetPlayer.distanceToSqr(Vec3.atCenterOf((Vec3i)candidatePos))) < bestScore)) continue;
            bestScore = score;
            bestBasePos = candidatePos;
            bestBaseDamage = targetDamage;
        }
        if (bestBasePos == null) {
            return false;
        }
        int obsidianSlot = this.basePlaceSwitch.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.find(Items.OBSIDIAN, 0, this.basePlaceSwitch.getValue().equalsIgnoreCase("AltSwap") || this.basePlaceSwitch.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
        if (obsidianSlot == -1) {
            return false;
        }
        Direction direction = WorldUtils.getDirection(bestBasePos, this.basePlaceStrictDirection.getValue());
        if (direction == null && !this.basePlaceAirPlace.getValue()) {
            return false;
        }
        int prevSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (!InventoryUtils.switchSlot(this.basePlaceSwitch.getValue(), obsidianSlot, prevSlot)) {
            return false;
        }
        if (direction != null) {
            placed = WorldUtils.placeBlock(bestBasePos, direction, InteractionHand.MAIN_HAND, this.basePlaceRotate.getValue() ? "Normal" : "None", true, this.renderMode.getValue().equalsIgnoreCase("Both") || this.renderMode.getValue().equalsIgnoreCase("Fill"));
        } else {
            WorldUtils.airPlaceBlock(bestBasePos, InteractionHand.MAIN_HAND, this.basePlaceRotate.getValue() ? "Normal" : "None", this.renderMode.getValue().equalsIgnoreCase("Both") || this.renderMode.getValue().equalsIgnoreCase("Fill"));
            placed = true;
        }
        InventoryUtils.switchBack(this.basePlaceSwitch.getValue(), obsidianSlot, prevSlot);
        if (placed) {
            this.placeTarget = new PlaceTarget(bestBasePos, targetPlayer, Collections.emptyList(), null, bestBaseDamage, 1);
            if (this.place.getValue()) {
                int crystalSlot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
                int currentSlot = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
                boolean hasCrystal = AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL || AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL || crystalSlot != -1 && !this.autoSwitch.getValue().equalsIgnoreCase("None");
                if (hasCrystal) {
                    CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(bestBasePos);
                    Vec3 hitVec = placementResult != null && placementResult.hitVec != null ? placementResult.hitVec : Vec3.atCenterOf((Vec3i)bestBasePos).add(0.0, 0.5, 0.0);
                    if (this.rotate.getValue().equalsIgnoreCase("Normal") || this.basePlaceRotate.getValue()) {
                        Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(hitVec), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                    } else if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                        Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(hitVec));
                    }
                    boolean switchedCrystal = false;
                    if (AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL && crystalSlot != -1) {
                        InventoryUtils.switchSlot(this.autoSwitch.getValue(), crystalSlot, currentSlot);
                        switchedCrystal = true;
                    }
                    this.place(bestBasePos);
                    if (switchedCrystal) {
                        InventoryUtils.switchBack(this.autoSwitch.getValue(), crystalSlot, currentSlot);
                    }
                }
            }
            Night.RENDER_MANAGER.setRenderPosition(bestBasePos);
        }
        return placed;
    }

    private boolean isBaseCandidateValid(BlockPos blockPos) {
        BlockState twoAboveState;
        if (AutoCrystalModule.mc.level == null || AutoCrystalModule.mc.player == null) {
            return false;
        }
        if (AutoCrystalModule.mc.level.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        if (AutoCrystalModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.basePlaceRange.getValue().doubleValue())) {
            return false;
        }
        if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            return false;
        }
        boolean checkLos = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
        if (checkLos && !WorldUtils.canSeeBlock(blockPos)) {
            return false;
        }
        BlockState baseState = AutoCrystalModule.mc.level.getBlockState(blockPos);
        if (!baseState.canBeReplaced()) {
            return false;
        }
        if (!WorldUtils.isPlaceable(blockPos)) {
            return false;
        }
        if (!this.basePlaceAirPlace.getValue() && WorldUtils.getDirection(blockPos, this.basePlaceStrictDirection.getValue()) == null) {
            return false;
        }
        BlockPos abovePos = blockPos.above();
        BlockState aboveState = AutoCrystalModule.mc.level.getBlockState(abovePos);
        if (!aboveState.canBeReplaced() || !AutoCrystalModule.mc.level.getFluidState(abovePos).isEmpty()) {
            return false;
        }
        if (this.placements.getValue().equalsIgnoreCase("Protocol") && !(twoAboveState = AutoCrystalModule.mc.level.getBlockState(blockPos.above(2))).canBeReplaced()) {
            return false;
        }
        return AutoCrystalModule.mc.level.getEntities((Entity)null, this.getCrystalPlacementBox(blockPos), entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal)).isEmpty();
    }

    private AABB getCrystalPlacementBox(BlockPos blockPos) {
        return new AABB(blockPos.offset(0, 1, 0));
    }

    public float getBestPlaceDamage() {
        return this.placeTarget == null ? 0.0f : this.placeTarget.getDamage();
    }

    @Generated
    public Player getTarget() {
        return this.target;
    }

    @Generated
    public String getCalculationDamage() {
        return this.calculationDamage;
    }

    private void executePlaceCrystals(Vec3 hitVec, int prevSlot, int crystalSlot, BlockPos blockPos) {
        boolean switched = false;
        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(hitVec));
        }
        if (AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                this.savedSlot = prevSlot;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), crystalSlot, prevSlot);
            switched = true;
        }
        this.place(blockPos);
        if (switched) {
            InventoryUtils.switchBack(this.autoSwitch.getValue(), crystalSlot, prevSlot);
        }
        if (this.godSync.getValue()) {
            boolean allowPredict = !this.antiKick.getValue() || !(AutoCrystalModule.mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) && !(AutoCrystalModule.mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem) && !Night.MODULE_MANAGER.getModule(KeyActionModule.class).isXpActive();
            if ((!this.antiKick.getValue() || this.kickTicks > this.kickThreshold.getValue().intValue()) && allowPredict) {
                if (!this.fast.getValue()) {
                    for (Entity entity : AutoCrystalModule.mc.level.entitiesForRendering()) {
                        if (entity.getId() <= this.highestID) continue;
                        this.highestID = entity.getId();
                    }
                }
                for (int i = 1 - this.offset.getValue().intValue(); i < this.predictions.getValue().intValue(); ++i) {
                    Entity entity = AutoCrystalModule.mc.level.getEntity(this.highestID);
                    if (entity != null && !(entity instanceof EndCrystal)) continue;
                    int predictedId = this.highestID + i;
                    mc.getConnection().send((Packet)new ServerboundAttackPacket(predictedId));
                    if (this.godSwing.getValue().equals("Strict")) {
                        mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                    this.attackedCrystals.put(predictedId, System.currentTimeMillis());
                }
                if (this.godSwing.getValue().equals("Normal")) {
                    mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }
            ++this.kickTicks;
        }
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
        public PlaceTarget(BlockPos position, Player player, List<Entity> obstructions, BlockPos exception, float damage, int calculations) {
            this.position = position;
            this.player = player;
            this.obstructions = obstructions;
            this.exception = exception;
            this.damage = damage;
            this.calculations = calculations;
        }
    }
}

