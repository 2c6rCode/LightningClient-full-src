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
        int n = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
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

    private void runCalculation(Set<BlockPos> set, boolean bl) {
        long l = System.nanoTime();
        try {
            this.attackTarget = this.calculateCrystals();
            this.placeTarget = this.calculatePlacements(null, set);
        }
        catch (Throwable throwable) {
            return;
        }
        long l2 = System.nanoTime() - l;
        this.calculationTime = new DecimalFormat("0.00").format((double)l2 / 1000000.0) + "ms";
        this.calculationCount = this.placeTarget == null ? 0 : this.placeTarget.getCalculations();
        this.calculationDamage = this.placeTarget == null ? "0.00" : new DecimalFormat("0.00").format(this.placeTarget.getDamage());
        this.target = this.placeTarget == null ? null : this.placeTarget.getPlayer();
        Player player = this.target;
        if (this.blockDestruction.getValue() && bl) {
            SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
            BlockPos blockPos = null;
            if (speedMineModule.getPrimary() != null && speedMineModule.getPrimary().isMining()) {
                blockPos = speedMineModule.getPrimary().getPosition();
            }
            if (blockPos != null) {
                this.mineTarget = this.calculatePlacements(blockPos, set);
            }
            BlockPos blockPos2 = null;
            if (speedMineModule.farReach.getValue()) {
                if (speedMineModule.getSecondary() != null && speedMineModule.getSecondary().isMining()) {
                    blockPos2 = speedMineModule.getSecondary().getPosition();
                }
            } else if (speedMineModule.getSecondary() != null && speedMineModule.getSecondary().isMining()) {
                blockPos2 = speedMineModule.getSecondary().getPosition();
            }
            if (blockPos2 != null) {
                this.mineTargetSecondary = this.calculatePlacements(blockPos2, set);
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
        SpeedMineModule speedMineModule2 = speedMineModule;
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
        if (AutoCrystalModule.mc.player != null) {
            boolean bl = AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL;
            boolean bl2 = bl;
            if (bl && (AutoCrystalModule.mc.options.keyUse.isDown() || AutoCrystalModule.mc.player.isUsingItem())) {
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
        if (AutoCrystalModule.mc.player != null) {
            boolean bl = AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE;
            boolean bl2 = bl;
            if (bl && (AutoCrystalModule.mc.options.keyUse.isDown() || AutoCrystalModule.mc.player.isUsingItem())) {
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
        BlockPos blockPos = endCrystal.blockPosition().below();
        if (!this.placedCrystals.containsKey(blockPos)) {
            blockPos = BlockPos.containing((double)endCrystal.getX(), (double)(endCrystal.getY() - 0.5), (double)endCrystal.getZ());
        }
        if (!this.placedCrystals.containsKey(blockPos)) {
            return;
        }
        if (endCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            return;
        }
        if (!AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(endCrystal.blockPosition())) {
            return;
        }
        boolean bl = this.raytrace.getValue() || endCrystal.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
        boolean bl2 = bl;
        if (bl && !WorldUtils.canSee((Entity)endCrystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float f = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal, this.ignoreTerrain.getValue());
            if (f > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && f > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        float[] fArray = RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition()));
        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), fArray);
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
        PlaceTarget placeTarget2;
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
        BlockPos blockPos = destroyBlockEvent.getPosition();
        if (blockPos == null) {
            return;
        }
        int n = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int n2 = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        boolean bl = false;
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && n == -1 && AutoCrystalModule.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        PlaceTarget placeTarget3 = this.mineTarget == null ? null : this.mineTarget.clone();
        PlaceTarget placeTarget4 = this.mineTargetSecondary == null ? null : this.mineTargetSecondary.clone();
        PlaceTarget placeTarget5 = placeTarget4;
        if (placeTarget3 != null && placeTarget3.getPosition() != null && blockPos.equals((Object)placeTarget3.getException())) {
            placeTarget2 = placeTarget3;
        } else if (placeTarget4 != null && placeTarget4.getPosition() != null && blockPos.equals((Object)placeTarget4.getException())) {
            placeTarget2 = placeTarget4;
        } else if (!this.asynchronous.getValue()) {
            placeTarget2 = this.calculatePlacements(blockPos);
        } else {
            placeTarget2 = placeTarget3 != null ? placeTarget3 : placeTarget4;
        }
        if (placeTarget2 == null || placeTarget2.getPosition() == null || placeTarget2.getDamage() <= 0.0f) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        BlockPos targetPos = placeTarget2.getPosition();
        if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(targetPos)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
            Night.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        Night.RENDER_MANAGER.setRenderPosition(targetPos);
        boolean bl2 = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(targetPos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
        boolean bl3 = bl2;
        if (bl2 && !WorldUtils.canSeeBlock(targetPos)) {
            return;
        }
        EndCrystal endCrystal = null;
        for (Entity e : AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(targetPos.above()), entity -> true)) {
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
                this.savedSlot = n2;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, n2);
            bl = true;
        }
        this.place(targetPos);
        if (bl) {
            InventoryUtils.switchBack(this.autoSwitch.getValue(), n, n2);
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
        BlockPos blockPos2 = endCrystal.blockPosition().below();
        if (Math.abs(blockPos2.getX() - blockPos.getX()) <= 2 && Math.abs(blockPos2.getZ() - blockPos.getZ()) <= 2 && Math.abs(blockPos2.getY() - blockPos.getY()) <= 2) {
            return true;
        }
        return endCrystal.getBoundingBox().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) <= 9.0;
    }

    private boolean isAtActiveUnbrokenMinePos(EndCrystal endCrystal) {
        BlockPos blockPos;
        BlockPos blockPos2;
        if (endCrystal == null || AutoCrystalModule.mc.level == null || AutoCrystalModule.mc.player == null) {
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

    private void attackCrystals() {
        boolean bl;
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
        EndCrystal endCrystal = null;
        boolean bl2 = false;
        PlaceTarget placeTarget = this.placeTarget;
        if (placeTarget != null && placeTarget.getPosition() == null && placeTarget.getObstructions() != null && !placeTarget.getObstructions().isEmpty()) {
            for (Entity entity : placeTarget.getObstructions()) {
                float f;
                EndCrystal endCrystal2;
                if (!(entity instanceof EndCrystal) || !(endCrystal2 = (EndCrystal)entity).isAlive() || this.isAtActiveUnbrokenMinePos(endCrystal2) || endCrystal2.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(endCrystal2.blockPosition())) continue;
                boolean bl3 = this.raytrace.getValue() || endCrystal2.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
                bl = bl3;
                if (bl3 && !WorldUtils.canSee((Entity)endCrystal2) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((f = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal2, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && f > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
                endCrystal = endCrystal2;
                bl2 = true;
                break;
            }
        }
        if (endCrystal == null) {
            endCrystal = this.attackTarget;
        }
        if (endCrystal == null) {
            List<Player> entity = this.selectTargets(this.getPlayers());
            float f = -1.0f;
            for (Entity entity2 : AutoCrystalModule.mc.level.entitiesForRendering()) {
                float f2;
                EndCrystal endCrystal3;
                if (!(entity2 instanceof EndCrystal) || !(endCrystal3 = (EndCrystal)entity2).isAlive() || this.isAtActiveUnbrokenMinePos(endCrystal3) || this.inhibit.getValue() && this.attackedCrystals.containsKey(endCrystal3.getId()) && System.currentTimeMillis() - this.attackedCrystals.get(endCrystal3.getId()) < Math.max((long)Night.SERVER_MANAGER.getPing() * 2L, 100L)) continue;
                BlockPos blockPos = endCrystal3.blockPosition().below();
                if (!this.placedCrystals.containsKey(blockPos)) {
                    blockPos = BlockPos.containing((double)endCrystal3.getX(), (double)(endCrystal3.getY() - 0.5), (double)endCrystal3.getZ());
                }
                if (!this.placedCrystals.containsKey(blockPos) || endCrystal3.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(endCrystal3.blockPosition())) continue;
                bl = this.raytrace.getValue() || endCrystal3.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
                boolean bl4 = bl;
                if (bl && !WorldUtils.canSee((Entity)endCrystal3) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((f2 = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal3, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && f2 > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
                float f3 = 0.0f;
                for (Player player : entity) {
                    float f4 = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()), endCrystal3, this.ignoreTerrain.getValue());
                    if (!(f4 > f3)) continue;
                    f3 = f4;
                }
                if (!(f3 > f)) continue;
                f = f3;
                endCrystal = endCrystal3;
                bl2 = true;
            }
        }
        if (endCrystal == null) {
            return;
        }
        EndCrystal endCrystal4 = endCrystal;
        if (this.isAtActiveUnbrokenMinePos(endCrystal4)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float f = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal4, this.ignoreTerrain.getValue());
            if (f > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && f > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        float[] fArray = this.calculateRotations(Vec3.atCenterOf((Vec3i)endCrystal4.blockPosition()));
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            Night.ROTATION_MANAGER.legacyRotate(fArray, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        if (this.swapDelay.getValue().intValue() > 0 && !this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) {
            return;
        }
        if (!this.attackTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.attackSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        Entity entity = AutoCrystalModule.mc.level.getEntity(endCrystal4.getId());
        String string = null;
        if (entity == null) {
            string = "entity-gone";
        } else if (!(entity instanceof EndCrystal)) {
            string = "not-end-crystal";
        } else if (!((EndCrystal)entity).isAlive()) {
            string = "dead";
        } else if (this.inhibit.getValue() && !bl2 && this.attackedCrystals.containsKey(entity.getId())) {
            string = "inhibit";
        } else if (entity.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue())) {
            string = "range";
        } else if (!AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
            string = "border";
        } else {
            boolean bl5 = this.raytrace.getValue() || entity.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
            bl = bl5;
            if (bl5 && !WorldUtils.canSee(entity)) {
                string = "cannot-see";
            }
        }
        if (string != null) {
            return;
        }
        this.attackRunnable = () -> {
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)endCrystal4.blockPosition())));
            }
            this.attack(endCrystal4);
        };
    }

    private void placeCrystals(boolean bl) {
        if (!this.isDead() && !this.shouldPause("Place")) {
            PlaceTarget placeTarget;
            PlaceTarget placeTarget2 = placeTarget = this.placeTarget == null ? null : this.placeTarget.clone();
            if (placeTarget != null && placeTarget.getPosition() != null) {
                if (placeTarget.getPlayer() != null && placeTarget.getPlayer().isAlive() && !placeTarget.getPlayer().isDeadOrDying() && !(placeTarget.getPlayer().getHealth() <= 0.0f)) {
                    int n = InventoryUtils.findHotbar(Items.END_CRYSTAL);
                    int n2 = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
                    if (this.autoSwitch.getValue().equalsIgnoreCase("None") || n != -1 || AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                        BlockPos blockPos = placeTarget.getPosition();
                        if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeRange.getValue().doubleValue())) {
                            Night.RENDER_MANAGER.setRenderPosition(null);
                        } else {
                            Night.RENDER_MANAGER.setRenderPosition(blockPos);
                            if (AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(blockPos) && (AutoCrystalModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN || AutoCrystalModule.mc.level.getBlockState(blockPos).getBlock() == Blocks.BEDROCK) && AutoCrystalModule.mc.level.getBlockState(blockPos.offset(0, 1, 0)).isAir() && (!this.placements.getValue().equalsIgnoreCase("Protocol") || AutoCrystalModule.mc.level.getBlockState(blockPos.offset(0, 2, 0)).isAir())) {
                                boolean bl2;
                                boolean bl3 = bl2 = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
                                if (!(bl2 && !WorldUtils.canSeeBlock(blockPos) || AutoCrystalModule.mc.level.getEntities((Entity)null, this.getCrystalPlacementBox(blockPos), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal)))) {
                                    Object object;
                                    EndCrystal endCrystal = null;
                                    Iterator iterator = AutoCrystalModule.mc.level.getEntities((Entity)null, this.getCrystalPlacementBox(blockPos), entity -> entity instanceof EndCrystal && entity.isAlive()).iterator();
                                    if (iterator.hasNext()) {
                                        object = (Entity)iterator.next();
                                        endCrystal = (EndCrystal)object;
                                    }
                                    if (endCrystal != null) {
                                        if (this.attack.getValue() && !this.isAtActiveUnbrokenMinePos(endCrystal)) {
                                            if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                                                float f = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal, this.ignoreTerrain.getValue());
                                                if (f <= this.maximumSelfDamage.getValue().floatValue() && (!this.antiSuicide.getValue() || f <= AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) {
                                                    float[] fArray = this.calculateRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition()));
                                                    if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                        Night.ROTATION_MANAGER.legacyRotate(fArray, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                    }
                                                    if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                        Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition())));
                                                    }
                                                    this.attack(endCrystal);
                                                }
                                            } else {
                                                object = this.calculateRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition()));
                                                if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                    Night.ROTATION_MANAGER.legacyRotate((float[])object, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                }
                                                if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                    Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)endCrystal.blockPosition())));
                                                }
                                                this.attack(endCrystal);
                                            }
                                        }
                                    } else {
                                        for (Entity rendEntity : AutoCrystalModule.mc.level.entitiesForRendering()) {
                                            boolean bl4;
                                            if (!(rendEntity instanceof EndCrystal)) continue;
                                            EndCrystal cry = (EndCrystal)rendEntity;
                                            if (!cry.isAlive() || this.isAtActiveUnbrokenMinePos(cry)) continue;
                                            BlockPos blockPos2 = cry.blockPosition().below();
                                            if (!this.placedCrystals.containsKey(blockPos2)) {
                                                blockPos2 = BlockPos.containing((double)cry.getX(), (double)(cry.getY() - 0.5), (double)cry.getZ());
                                            }
                                            if (!this.placedCrystals.containsKey(blockPos2)) continue;
                                            double d = cry.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition());
                                            boolean bl5 = d <= Mth.square((double)this.attackRange.getValue().doubleValue());
                                            boolean bl6 = this.raytrace.getValue() || d > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
                                            boolean bl7 = bl4 = bl5 && (!bl6 || WorldUtils.canSee((Entity)cry));
                                            if (bl4) {
                                                if (this.attack.getValue()) {
                                                    if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
                                                        float f = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), cry, this.ignoreTerrain.getValue());
                                                        if (f <= this.maximumSelfDamage.getValue().floatValue() && (!this.antiSuicide.getValue() || f <= AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) {
                                                            float[] fArray = this.calculateRotations(Vec3.atCenterOf((Vec3i)cry.blockPosition()));
                                                            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                                Night.ROTATION_MANAGER.legacyRotate(fArray, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                            }
                                                            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)cry.blockPosition())));
                                                            }
                                                            this.attack(cry);
                                                        }
                                                    } else {
                                                        float[] fArray = this.calculateRotations(Vec3.atCenterOf((Vec3i)cry.blockPosition()));
                                                        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                                            Night.ROTATION_MANAGER.legacyRotate(fArray, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                                        }
                                                        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                                            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf((Vec3i)cry.blockPosition())));
                                                        }
                                                        this.attack(cry);
                                                    }
                                                }
                                                return;
                                            }
                                            if (!blockPos2.equals((Object)blockPos)) continue;
                                            return;
                                        }
                                        CrystalPlacementHelper.PlacementResult placeRes = CrystalPlacementHelper.getVisiblePlacement(blockPos);
                                        Vec3 hitVec = placeRes != null && placeRes.hitVec != null ? placeRes.hitVec : Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.5, 0.0);
                                        float[] rot = this.calculateRotations(hitVec);
                                        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                            Night.ROTATION_MANAGER.legacyRotate(rot, Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                                        }
                                        if ((this.swapDelay.getValue().intValue() <= 0 || this.swapTimer.hasTimeElapsed(this.swapDelay.getValue().longValue() * 50L)) && this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
                                            if (!bl && this.placedSequentially) {
                                                this.placedSequentially = false;
                                            } else {
                                                this.placeRunnable = () -> this.executePlaceCrystals(hitVec, n2, n, blockPos);
                                                if (bl) {
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
        List<Player> list = this.selectTargets(this.getPlayers());
        if (list.isEmpty()) {
            return null;
        }
        EndCrystal endCrystal = null;
        float f = 0.0f;
        for (Entity entity : AutoCrystalModule.mc.level.entitiesForRendering()) {
            float f2;
            EndCrystal endCrystal2;
            if (!(entity instanceof EndCrystal) || !(endCrystal2 = (EndCrystal)entity).isAlive() || this.isAtActiveUnbrokenMinePos(endCrystal2) || this.inhibit.getValue() && this.attackedCrystals.containsKey(entity.getId()) || endCrystal2.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(endCrystal2.blockPosition())) continue;
            boolean bl = this.raytrace.getValue() || endCrystal2.getBoundingBox().distanceToSqr(AutoCrystalModule.mc.player.getEyePosition()) > Mth.square((double)this.attackWallsRange.getValue().doubleValue());
            boolean bl2 = bl;
            if (bl && !WorldUtils.canSee((Entity)endCrystal2) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((f2 = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal2, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && f2 > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
            boolean bl3 = false;
            for (Player player : list) {
                float f3 = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()), endCrystal2, this.ignoreTerrain.getValue());
                if (f3 < this.getMinimumDamage(player, this.minimumDamage.getValue().floatValue()) && f3 < player.getHealth() + player.getAbsorptionAmount() && !(f3 * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()) || !(f3 > f) && !(f3 > player.getHealth() + player.getAbsorptionAmount())) continue;
                endCrystal = endCrystal2;
                f = f3;
                if (!(f3 > player.getHealth() + player.getAbsorptionAmount())) continue;
                bl3 = true;
                break;
            }
            if (!bl3) continue;
        }
        return endCrystal;
    }

    private PlaceTarget calculatePlacements(BlockPos blockPos) {
        return this.calculatePlacements(blockPos, Night.WORLD_MANAGER.getReservedPlacements());
    }

    private PlaceTarget calculatePlacements(BlockPos blockPos2, Set<BlockPos> set) {
        if (!this.place.getValue()) {
            return null;
        }
        if (!this.shouldPause("Place") && (!this.autoSwitch.getValue().equalsIgnoreCase("None") && InventoryUtils.findHotbar(Items.END_CRYSTAL) != -1 || AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL || AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL)) {
            List<Player> list = this.selectTargets(this.getPlayers());
            if (list.isEmpty()) {
                return null;
            }
            BlockPos blockPos3 = null;
            Player player = null;
            float f = 0.0f;
            float f2 = 0.0f;
            BlockPos blockPos4 = this.placeTarget == null ? null : this.placeTarget.getPosition();
            float f3 = 0.5f;
            ArrayList<Entity> arrayList = new ArrayList<Entity>();
            int n = 0;
            block0: for (int i = 0; i < Night.WORLD_MANAGER.getRadius(Math.max(this.placeRange.getValue().doubleValue(), this.placeWallsRange.getValue().doubleValue())); ++i) {
                float f4;
                boolean bl;
                BlockPos blockPos5 = AutoCrystalModule.mc.player.blockPosition().offset(Night.WORLD_MANAGER.getOffset(i));
                if (AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos5)) > Mth.square((double)this.placeRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(blockPos5) || AutoCrystalModule.mc.level.getBlockState(blockPos5).getBlock() != Blocks.OBSIDIAN && AutoCrystalModule.mc.level.getBlockState(blockPos5).getBlock() != Blocks.BEDROCK || !AutoCrystalModule.mc.level.getBlockState(blockPos5.offset(0, 1, 0)).isAir() || this.placements.getValue().equalsIgnoreCase("Protocol") && !AutoCrystalModule.mc.level.getBlockState(blockPos5.offset(0, 2, 0)).isAir()) continue;
                boolean bl2 = bl = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos5)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
                if (bl && !WorldUtils.canSeeBlock(blockPos5)) continue;
                AABB aABB = new AABB((double)(blockPos5.getX() - 1), (double)(blockPos5.getY() + 1), (double)(blockPos5.getZ() - 1), (double)(blockPos5.getX() + 2), (double)(blockPos5.getY() + 3), (double)(blockPos5.getZ() + 2));
                if (set.stream().anyMatch(blockPos -> aABB.intersects(new AABB(blockPos)))) continue;
                List<Entity> list2 = AutoCrystalModule.mc.level.getEntities((Entity)null, this.getCrystalPlacementBox(blockPos5), entity -> entity.isAlive() && !(entity instanceof ExperienceOrb));
                boolean bl3 = false;
                ArrayList<EndCrystal> arrayList2 = new ArrayList<EndCrystal>();
                for (Entity entity2 : list2) {
                    if (!(entity2 instanceof EndCrystal)) {
                        bl3 = true;
                        break;
                    }
                    EndCrystal endCrystal = (EndCrystal)entity2;
                    if (this.isAtActiveUnbrokenMinePos(endCrystal)) {
                        bl3 = true;
                        break;
                    }
                    BlockPos blockPos6 = endCrystal.blockPosition().below();
                    if (!this.placedCrystals.containsKey(blockPos6)) {
                        blockPos6 = BlockPos.containing((double)endCrystal.getX(), (double)(endCrystal.getY() - 0.5), (double)endCrystal.getZ());
                    }
                    if (this.placedCrystals.containsKey(blockPos6) && endCrystal.tickCount < 20 - this.attackSpeed.getValue().intValue() + 15) continue;
                    arrayList2.add(endCrystal);
                }
                if (bl3) continue;
                double d = Mth.square((double)10.0);
                boolean bl4 = false;
                for (Player player2 : list) {
                    if (!(player2.distanceToSqr((double)blockPos5.getX() + 0.5, (double)blockPos5.getY() + 1.0, (double)blockPos5.getZ() + 0.5) <= d)) continue;
                    bl4 = true;
                    break;
                }
                if (!bl4 || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((f4 = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), blockPos5, blockPos2, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && f4 > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
                for (Player player2 : list) {
                    ++n;
                    float f5 = DamageUtils.getCrystalDamage((Entity)player2, PositionUtils.extrapolate(player2, this.extrapolation.getValue().intValue()), blockPos5, blockPos2, this.ignoreTerrain.getValue());
                    if (f5 < this.getMinimumDamage(player2, this.minimumDamage.getValue().floatValue()) && f5 < player2.getHealth() + player2.getAbsorptionAmount() && !(f5 * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= player2.getHealth() + player2.getAbsorptionAmount()) || f5 <= 0.0f) continue;
                    if (blockPos2 == null && !arrayList2.isEmpty()) {
                        arrayList.add((Entity)arrayList2.getFirst());
                        continue block0;
                    }
                    float f6 = blockPos5.equals((Object)blockPos4) ? 0.5f : 0.0f;
                    float f7 = f5 + f6;
                    if (!(f7 > f)) continue;
                    blockPos3 = blockPos5;
                    player = player2;
                    f = f7;
                    f2 = f5;
                }
            }
            return blockPos3 == null ? new PlaceTarget(null, null, arrayList, null, 0.0f, n) : new PlaceTarget(blockPos3, player, arrayList, blockPos2, f2, n);
        }
        return null;
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

    private void attack(EndCrystal endCrystal) {
        int n;
        if (endCrystal == null || !endCrystal.isAlive() || endCrystal.isRemoved()) {
            return;
        }
        if (this.isAtActiveUnbrokenMinePos(endCrystal)) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled()) {
            float f = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), endCrystal, this.ignoreTerrain.getValue());
            if (f > this.maximumSelfDamage.getValue().floatValue()) {
                return;
            }
            if (this.antiSuicide.getValue() && f > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) {
                return;
            }
        }
        int n2 = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        int n3 = -1;
        if (!this.antiWeakness.getValue().equalsIgnoreCase("None") && AutoCrystalModule.mc.player.hasEffect(MobEffects.WEAKNESS) && (n = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END)) != -1) {
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
        InteractionHand interactionHand = AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(blockPos);
        NetworkUtils.sendSequencedPacket(n -> new ServerboundUseItemOnPacket(interactionHand, new BlockHitResult(placementResult.hitVec, placementResult.direction, blockPos, false), n));
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

    private void mineIgnoreTryPlace(BlockPos blockPos2) {
        if (this.shouldPause("Place")) {
            return;
        }
        if (!this.placeTimer.hasTimeElapsed(Float.valueOf(1000.0f - this.placeSpeed.getValue().floatValue() * 50.0f))) {
            return;
        }
        List<Player> list = this.selectTargets(this.getPlayers());
        if (list.isEmpty()) {
            return;
        }
        boolean bl = false;
        for (Player object : list) {
            if (object.equals((Object)AutoCrystalModule.mc.player)) continue;
            BlockPos n2 = object.blockPosition();
            if (Math.abs(blockPos2.getX() - n2.getX()) > 1 || Math.abs(blockPos2.getZ() - n2.getZ()) > 1 || Math.abs(blockPos2.getY() - n2.getY()) > 1) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return;
        }
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        arrayList.add(blockPos2.above());
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            arrayList.add(blockPos2.relative(dir).above());
        }
        int n = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int n2 = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (!this.autoSwitch.getValue().equalsIgnoreCase("None") && n == -1 && AutoCrystalModule.mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            return;
        }
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            float f;
            BlockPos blockPos3 = (BlockPos)iterator.next();
            BlockPos blockPos4 = blockPos3.below();
            if (AutoCrystalModule.mc.level.getBlockState(blockPos4).getBlock() != Blocks.OBSIDIAN && AutoCrystalModule.mc.level.getBlockState(blockPos4).getBlock() != Blocks.BEDROCK || !AutoCrystalModule.mc.level.getBlockState(blockPos3).isAir() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos3)) > Mth.square((double)this.placeRange.getValue().doubleValue()) || !AutoCrystalModule.mc.level.getWorldBorder().isWithinBounds(blockPos3)) continue;
            boolean bl2 = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos3)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
            boolean bl3 = bl2;
            if (bl2 && !WorldUtils.canSeeBlock(blockPos4) || AutoCrystalModule.mc.level.getEntities((Entity)null, new AABB(blockPos3), entity -> true).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrb) && !(entity instanceof EndCrystal))) continue;
            AABB aABB = new AABB((double)(blockPos3.getX() - 1), (double)blockPos3.getY(), (double)(blockPos3.getZ() - 1), (double)(blockPos3.getX() + 2), (double)(blockPos3.getY() + 2), (double)(blockPos3.getZ() + 2));
            if (Night.WORLD_MANAGER.getReservedPlacements().stream().anyMatch(blockPos -> aABB.intersects(new AABB(blockPos))) || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((f = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, null, blockPos3, null, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && f > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount())) continue;
            boolean bl4 = false;
            for (Player player2 : list) {
                float bl5;
                if (player2.equals((Object)AutoCrystalModule.mc.player) || !((bl5 = DamageUtils.getCrystalDamage((Entity)player2, PositionUtils.extrapolate(player2, this.extrapolation.getValue().intValue()), blockPos3, blockPos2, this.ignoreTerrain.getValue())) >= this.getMinimumDamage(player2, this.minimumDamage.getValue().floatValue()))) continue;
                bl4 = true;
                break;
            }
            if (!bl4) continue;
            CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(blockPos3);
            Vec3 vec3 = placementResult != null && placementResult.hitVec != null ? placementResult.hitVec : Vec3.atCenterOf((Vec3i)blockPos3).add(0.0, 0.5, 0.0);
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(vec3), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(vec3));
            }
            boolean bl5 = false;
            if (AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                    this.savedSlot = n2;
                }
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), n, n2);
                bl5 = true;
            }
            this.place(blockPos3);
            if (bl5) {
                InventoryUtils.switchBack(this.autoSwitch.getValue(), n, n2);
            }
            this.mineIgnorePlacedPos = blockPos3;
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
        long l = System.currentTimeMillis();
        if (l - this.lastPlayerCacheTime < 50L) {
            return this.cachedPlayers;
        }
        ArrayList<Player> arrayList = new ArrayList<Player>();
        for (Player player : AutoCrystalModule.mc.level.players()) {
            if (player == AutoCrystalModule.mc.player || EntityUtils.isGhost((Entity)player) || !player.isAlive() || AutoCrystalModule.mc.player.distanceToSqr((Entity)player) > Mth.square((double)this.enemyRange.getValue().doubleValue()) || Night.FRIEND_MANAGER.contains(player.getName().getString())) continue;
            boolean bl = player.getMainHandItem().getItem() == Items.END_CRYSTAL || player.getOffhandItem().getItem() == Items.END_CRYSTAL;
            boolean bl2 = bl;
            if (this.ignoreNaked.getValue() && EntityUtils.isNaked(player) && !bl) continue;
            arrayList.add(player);
        }
        this.cachedPlayers = arrayList;
        this.lastPlayerCacheTime = l;
        return arrayList;
    }

    private List<Player> selectTargets(List<Player> list) {
        if (list.size() <= 1 || this.targetMode.getValue().equalsIgnoreCase("All")) {
            return list;
        }
        Player player = null;
        double d = 0.0;
        for (Player player2 : list) {
            double d2;
            switch (this.targetMode.getValue()) {
                case "Nearest": {
                    d2 = -AutoCrystalModule.mc.player.distanceToSqr((Entity)player2);
                    break;
                }
                case "Farthest": {
                    d2 = AutoCrystalModule.mc.player.distanceToSqr((Entity)player2);
                    break;
                }
                default: {
                    double d3 = d2 = (double)(-(player2.getHealth() + player2.getAbsorptionAmount()));
                }
            }
            if (player != null && !(d2 > d)) continue;
            player = player2;
            d = d2;
        }
        return List.of(player);
    }

    private boolean shouldPause(String string) {
        if (this.isPearlPaused()) {
            return true;
        }
        if (this.isXpPaused()) {
            return true;
        }
        if (this.awaitMine.getValue() && this.pauseForAutoMine()) {
            return true;
        }
        boolean bl = this.whileEating.getValue().equalsIgnoreCase("None") || string.equalsIgnoreCase("Attack") && this.whileEating.getValue().equalsIgnoreCase("Place") || string.equalsIgnoreCase("Place") && this.whileEating.getValue().equalsIgnoreCase("Attack");
        return bl && EntityUtils.isEating();
    }

    private float getMinimumDamage(Player player, float f) {
        if (player == null) {
            return f;
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

    private boolean executeBasePlace() {
        boolean bl;
        int n;
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
        Player player = this.target;
        if (player == null) {
            double d = Double.MAX_VALUE;
            for (Player player2 : AutoCrystalModule.mc.level.players()) {
                if (EntityUtils.isGhost((Entity)player2) || player2 == AutoCrystalModule.mc.player || !player2.isAlive() || Night.FRIEND_MANAGER.contains(player2.getName().getString())) continue;
                double d3 = AutoCrystalModule.mc.player.distanceToSqr((Entity)player2);
                if (!(d3 <= Mth.square((double)this.enemyRange.getValue().doubleValue())) || !(d3 < d)) continue;
                d = d3;
                player = player2;
            }
        }
        if (player == null) {
            return false;
        }
        PlaceTarget placeTarget = this.placeTarget;
        float f = placeTarget != null && placeTarget.getPosition() != null ? placeTarget.getDamage() : 0.0f;
        float f2 = f;
        if (f >= player.getHealth() + player.getAbsorptionAmount()) {
            return false;
        }
        BlockPos blockPos = PositionUtils.getFlooredPosition((Entity)player);
        double d = this.basePlaceRadius.getValue().doubleValue();
        int n2 = (int)Math.ceil(d);
        int n3 = blockPos.getY() - 1;
        int n4 = blockPos.getY() - 3;
        LinkedHashSet<BlockPos> linkedHashSet = new LinkedHashSet<BlockPos>();
        for (int i = n3; i >= n4; --i) {
            for (int j = -n2; j <= n2; ++j) {
                for (int k = -n2; k <= n2; ++k) {
                    if ((double)(j * j + k * k) > d * d) continue;
                    linkedHashSet.add(new BlockPos(blockPos.getX() + j, i, blockPos.getZ() + k));
                }
            }
        }
        BlockPos blockPos2 = null;
        float f3 = 0.0f;
        double d4 = Double.MAX_VALUE;
        for (BlockPos blockPos3 : linkedHashSet) {
            double d5;
            float f4;
            float f5;
            if (!this.isBaseCandidateValid(blockPos3) || (blockState = AutoCrystalModule.mc.level.getBlockState(blockPos3)).getBlock() == Blocks.OBSIDIAN || blockState.getBlock() == Blocks.BEDROCK || !Night.MODULE_MANAGER.getModule(SuicideModule.class).isToggled() && ((f5 = DamageUtils.getCrystalDamage((Entity)AutoCrystalModule.mc.player, PositionUtils.extrapolate((Player)AutoCrystalModule.mc.player, this.extrapolation.getValue().intValue()), blockPos3, null, this.ignoreTerrain.getValue())) > this.maximumSelfDamage.getValue().floatValue() || this.antiSuicide.getValue() && f5 > AutoCrystalModule.mc.player.getHealth() + AutoCrystalModule.mc.player.getAbsorptionAmount()) || (f4 = DamageUtils.getCrystalDamage((Entity)player, PositionUtils.extrapolate(player, this.extrapolation.getValue().intValue()), blockPos3, null, this.ignoreTerrain.getValue())) < this.basePlaceMinDamage.getValue().floatValue() || f4 < this.getMinimumDamage(player, this.minimumDamage.getValue().floatValue()) && f4 < player.getHealth() + player.getAbsorptionAmount() && !(f4 * (1.0f + this.lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount()) || f > 0.0f && f4 < f * this.basePlaceRatio.getValue().floatValue() || !((d5 = (double)(-f4) * 10.0 + AutoCrystalModule.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos3)) + player.distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos3))) < d4)) continue;
            d4 = d5;
            blockPos2 = blockPos3;
            f3 = f4;
        }
        if (blockPos2 == null) {
            return false;
        }
        int obsidianSlot = this.basePlaceSwitch.getValue().equalsIgnoreCase("None") ? -1 : InventoryUtils.find(Items.OBSIDIAN, 0, this.basePlaceSwitch.getValue().equalsIgnoreCase("AltSwap") || this.basePlaceSwitch.getValue().equalsIgnoreCase("AltPickup") ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
        if (obsidianSlot == -1) {
            return false;
        }
        Direction direction = WorldUtils.getDirection(blockPos2, this.basePlaceStrictDirection.getValue());
        if (direction == null && !this.basePlaceAirPlace.getValue()) {
            return false;
        }
        int n6 = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
        if (!InventoryUtils.switchSlot(this.basePlaceSwitch.getValue(), obsidianSlot, n6)) {
            return false;
        }
        if (direction != null) {
            bl = WorldUtils.placeBlock(blockPos2, direction, InteractionHand.MAIN_HAND, this.basePlaceRotate.getValue() ? "Normal" : "None", true, this.renderMode.getValue().equalsIgnoreCase("Both") || this.renderMode.getValue().equalsIgnoreCase("Fill"));
        } else {
            WorldUtils.airPlaceBlock(blockPos2, InteractionHand.MAIN_HAND, this.basePlaceRotate.getValue() ? "Normal" : "None", this.renderMode.getValue().equalsIgnoreCase("Both") || this.renderMode.getValue().equalsIgnoreCase("Fill"));
            bl = true;
        }
        InventoryUtils.switchBack(this.basePlaceSwitch.getValue(), obsidianSlot, n6);
        if (bl) {
            this.placeTarget = new PlaceTarget(blockPos2, player, Collections.emptyList(), null, f3, 1);
            if (this.place.getValue()) {
                int n7 = InventoryUtils.findHotbar(Items.END_CRYSTAL);
                int n8 = AutoCrystalModule.mc.player.getInventory().getSelectedSlot();
                boolean bl2 = AutoCrystalModule.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL || AutoCrystalModule.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL || n7 != -1 && !this.autoSwitch.getValue().equalsIgnoreCase("None");
                boolean bl3 = bl2;
                if (bl2) {
                    CrystalPlacementHelper.PlacementResult placementResult = CrystalPlacementHelper.getVisiblePlacement(blockPos2);
                    Vec3 hitVec = placementResult != null && placementResult.hitVec != null ? placementResult.hitVec : Vec3.atCenterOf((Vec3i)blockPos2).add(0.0, 0.5, 0.0);
                    if (this.rotate.getValue().equalsIgnoreCase("Normal") || this.basePlaceRotate.getValue()) {
                        Night.ROTATION_MANAGER.legacyRotate(this.calculateRotations(hitVec), Night.ROTATION_MANAGER.getLegacyModulePriority(this));
                    } else if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
                        Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(hitVec));
                    }
                    boolean bl4 = false;
                    if (AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL && n7 != -1) {
                        InventoryUtils.switchSlot(this.autoSwitch.getValue(), n7, n8);
                        bl4 = true;
                    }
                    this.place(blockPos2);
                    if (bl4) {
                        InventoryUtils.switchBack(this.autoSwitch.getValue(), n7, n8);
                    }
                }
            }
            Night.RENDER_MANAGER.setRenderPosition(blockPos2);
        }
        return bl;
    }

    private boolean isBaseCandidateValid(BlockPos blockPos) {
        BlockState blockState;
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
        boolean bl = this.raytrace.getValue() || AutoCrystalModule.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)blockPos)) > Mth.square((double)this.placeWallsRange.getValue().doubleValue());
        boolean bl2 = bl;
        if (bl && !WorldUtils.canSeeBlock(blockPos)) {
            return false;
        }
        BlockState blockState2 = AutoCrystalModule.mc.level.getBlockState(blockPos);
        if (!blockState2.canBeReplaced()) {
            return false;
        }
        if (!WorldUtils.isPlaceable(blockPos)) {
            return false;
        }
        if (!this.basePlaceAirPlace.getValue() && WorldUtils.getDirection(blockPos, this.basePlaceStrictDirection.getValue()) == null) {
            return false;
        }
        BlockPos blockPos2 = blockPos.above();
        BlockState blockState3 = AutoCrystalModule.mc.level.getBlockState(blockPos2);
        if (!blockState3.canBeReplaced() || !AutoCrystalModule.mc.level.getFluidState(blockPos2).isEmpty()) {
            return false;
        }
        if (this.placements.getValue().equalsIgnoreCase("Protocol") && !(blockState = AutoCrystalModule.mc.level.getBlockState(blockPos.above(2))).canBeReplaced()) {
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

    private void executePlaceCrystals(Vec3 vec3, int n, int n2, BlockPos blockPos) {
        boolean bl = false;
        if (this.rotate.getValue().equalsIgnoreCase("Packet") || this.rotate.getValue().equalsIgnoreCase("Silent")) {
            Night.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(vec3));
        }
        if (AutoCrystalModule.mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                this.savedSlot = n;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), n2, n);
            bl = true;
        }
        this.place(blockPos);
        if (bl) {
            InventoryUtils.switchBack(this.autoSwitch.getValue(), n2, n);
        }
        if (this.godSync.getValue()) {
            boolean bl2;
            boolean bl3 = bl2 = !this.antiKick.getValue() || !(AutoCrystalModule.mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) && !(AutoCrystalModule.mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem) && !Night.MODULE_MANAGER.getModule(KeyActionModule.class).isXpActive();
            if ((!this.antiKick.getValue() || this.kickTicks > this.kickThreshold.getValue().intValue()) && bl2) {
                if (!this.fast.getValue()) {
                    for (Entity entity : AutoCrystalModule.mc.level.entitiesForRendering()) {
                        if (entity.getId() <= this.highestID) continue;
                        this.highestID = entity.getId();
                    }
                }
                for (int i = 1 - this.offset.getValue().intValue(); i < this.predictions.getValue().intValue(); ++i) {
                    Entity entity;
                    entity = AutoCrystalModule.mc.level.getEntity(this.highestID);
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

