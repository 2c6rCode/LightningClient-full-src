/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.arrow.Arrow
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ExperienceBottleItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemInstance
 *  net.minecraft.world.item.LingeringPotionItem
 *  net.minecraft.world.item.SplashPotionItem
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 */
package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@RegisterModule(name="Trajectories", description="Draws a predicted trajectory of where throwables will end up when you throw them.", category=Module.Category.VISUALS)
public class TrajectoriesModule
extends Module {
    public ColorSetting lineColor = new ColorSetting("LineColor", "The color of the trajectory line.", ColorUtils.getDefaultOutlineColor());
    public CategorySetting entitiesCategory = new CategorySetting("Entities", "The rendering that will be applied to entities hit by the trajectory.");
    public ModeSetting entitiesMode = new ModeSetting("EntitiesMode", "Mode", "The rendering that will be applied to the target entity.", new CategorySetting.Visibility(this.entitiesCategory), "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting entitiesFillColor = new ColorSetting("EntitiesFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.entitiesMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting entitiesOutlineColor = new ColorSetting("EntitiesOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.entitiesMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public CategorySetting blocksCategory = new CategorySetting("Blocks", "The rendering that will be applied to blocks hit by the trajectory.");
    public ModeSetting blocksMode = new ModeSetting("BlocksMode", "Mode", "The rendering that will be applied to the target block.", new CategorySetting.Visibility(this.blocksCategory), "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting blocksFillColor = new ColorSetting("BlocksFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.blocksMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting blocksOutlineColor = new ColorSetting("BlocksOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.blocksMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private List<Entity> hitEntities = new ArrayList<Entity>();

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        InteractionHand interactionHand;
        if (TrajectoriesModule.mc.player == null || TrajectoriesModule.mc.level == null) {
            return;
        }
        if (TrajectoriesModule.mc.gui.hud.isHidden() || !TrajectoriesModule.mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (TrajectoriesModule.mc.player.getMainHandItem().getItem() instanceof BowItem || TrajectoriesModule.mc.player.getMainHandItem().getItem() instanceof CrossbowItem || EntityUtils.isThrowable(TrajectoriesModule.mc.player.getMainHandItem().getItem())) {
            interactionHand = InteractionHand.MAIN_HAND;
        } else if (TrajectoriesModule.mc.player.getOffhandItem().getItem() instanceof BowItem || TrajectoriesModule.mc.player.getOffhandItem().getItem() instanceof CrossbowItem || EntityUtils.isThrowable(TrajectoriesModule.mc.player.getOffhandItem().getItem())) {
            interactionHand = InteractionHand.OFF_HAND;
        } else {
            return;
        }
        this.hitEntities.clear();
        float f = Mth.lerp((float)renderWorldEvent.getTickDelta(), (float)TrajectoriesModule.mc.player.yRotO, (float)TrajectoriesModule.mc.player.getYRot());
        if (TrajectoriesModule.mc.player.getOffhandItem().getItem() instanceof CrossbowItem && EnchantmentHelper.getItemEnchantmentLevel((Holder)TrajectoriesModule.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), (ItemInstance)TrajectoriesModule.mc.player.getOffhandItem()) != 0 || TrajectoriesModule.mc.player.getMainHandItem().getItem() instanceof CrossbowItem && EnchantmentHelper.getItemEnchantmentLevel((Holder)TrajectoriesModule.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), (ItemInstance)TrajectoriesModule.mc.player.getMainHandItem()) != 0) {
            this.project(renderWorldEvent.getMatrices(), interactionHand == InteractionHand.OFF_HAND ? TrajectoriesModule.mc.player.getOffhandItem().getItem() : TrajectoriesModule.mc.player.getMainHandItem().getItem(), f - 10.0f, renderWorldEvent.getTickDelta());
            this.project(renderWorldEvent.getMatrices(), interactionHand == InteractionHand.OFF_HAND ? TrajectoriesModule.mc.player.getOffhandItem().getItem() : TrajectoriesModule.mc.player.getMainHandItem().getItem(), f, renderWorldEvent.getTickDelta());
            this.project(renderWorldEvent.getMatrices(), interactionHand == InteractionHand.OFF_HAND ? TrajectoriesModule.mc.player.getOffhandItem().getItem() : TrajectoriesModule.mc.player.getMainHandItem().getItem(), f + 10.0f, renderWorldEvent.getTickDelta());
        } else {
            this.project(renderWorldEvent.getMatrices(), interactionHand == InteractionHand.OFF_HAND ? TrajectoriesModule.mc.player.getOffhandItem().getItem() : TrajectoriesModule.mc.player.getMainHandItem().getItem(), f, renderWorldEvent.getTickDelta());
        }
    }

    private void project(PoseStack poseStack, Item item, float f, float f2) {
        Vec3 vec3;
        double d = Mth.lerp((double)f2, (double)TrajectoriesModule.mc.player.xo, (double)TrajectoriesModule.mc.player.getX());
        double d2 = Mth.lerp((double)f2, (double)TrajectoriesModule.mc.player.yo, (double)TrajectoriesModule.mc.player.getY());
        double d3 = Mth.lerp((double)f2, (double)TrajectoriesModule.mc.player.zo, (double)TrajectoriesModule.mc.player.getZ());
        d2 = d2 + (double)TrajectoriesModule.mc.player.getEyeHeight(TrajectoriesModule.mc.player.getPose()) - 0.1000000014901161;
        float f3 = item instanceof BowItem ? 1.0f : 0.4f;
        float f4 = Mth.lerp((float)f2, (float)TrajectoriesModule.mc.player.xRotO, (float)TrajectoriesModule.mc.player.getXRot());
        double d4 = -Mth.sin((double)(f / 180.0f * (float)Math.PI)) * Mth.cos((double)(f4 / 180.0f * (float)Math.PI)) * f3;
        double d5 = -Mth.sin((double)((f4 - (float)this.getThrowPitch(item)) / 180.0f * 3.141593f)) * f3;
        double d6 = Mth.cos((double)(f / 180.0f * (float)Math.PI)) * Mth.cos((double)(f4 / 180.0f * (float)Math.PI)) * f3;
        float f5 = (float)TrajectoriesModule.mc.player.getTicksUsingItem() / 20.0f;
        if ((f5 = (f5 * f5 + f5 * 2.0f) / 3.0f) > 1.0f || f5 == 0.0f) {
            f5 = 1.0f;
        }
        float f6 = Mth.sqrt((float)((float)(d4 * d4 + d5 * d5 + d6 * d6)));
        d4 /= (double)f6;
        d5 /= (double)f6;
        d6 /= (double)f6;
        float f7 = (item instanceof BowItem ? f5 * 2.0f : (item instanceof CrossbowItem ? 2.2f : 1.0f)) * this.getThrowVelocity(item);
        d4 *= (double)f7;
        d5 *= (double)f7;
        d6 *= (double)f7;
        if (!TrajectoriesModule.mc.player.onGround()) {
            d5 += TrajectoriesModule.mc.player.getDeltaMovement().y;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        boolean bl = false;
        BlockHitResult blockHitResult = null;
        Entity entity = null;
        while (!bl && d2 > -65.0) {
            vec3 = new Vec3(d, d2, d3);
            if (TrajectoriesModule.mc.level.getBlockState(new BlockPos((int)(d += d4), (int)(d2 += d5), (int)(d3 += d6))).getBlock() == Blocks.WATER) {
                d4 *= 0.8;
                d5 *= 0.8;
                d6 *= 0.8;
            } else {
                d4 *= 0.99;
                d5 *= 0.99;
                d6 *= 0.99;
            }
            d5 = item instanceof BowItem ? (d5 = d5 - (double)0.05f) : (TrajectoriesModule.mc.player.getMainHandItem().getItem() instanceof CrossbowItem ? (d5 = d5 - (double)0.05f) : (d5 = d5 - (double)0.03f));
            Vec3 vec32 = new Vec3(d, d2, d3);
            Entity entity2 = this.getHitEntity(new AABB(d - 1.0, d2 - 1.0, d3 - 1.0, d + 1.0, d2 + 1.0, d3 + 1.0));
            BlockHitResult blockHitResult2 = TrajectoriesModule.mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)TrajectoriesModule.mc.player));
            if (entity2 != null) {
                entity = entity2;
                bl = true;
            } else if (blockHitResult2 != null && blockHitResult2.getType() != HitResult.Type.MISS) {
                blockHitResult = blockHitResult2;
                bl = true;
            }
            Renderer3D.DEBUG_LINES.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(matrix4f, (float)(vec3.x - TrajectoriesModule.mc.gameRenderer.mainCamera().position().x), (float)(vec3.y - TrajectoriesModule.mc.gameRenderer.mainCamera().position().y), (float)(vec3.z - TrajectoriesModule.mc.gameRenderer.mainCamera().position().z), this.lineColor.getColor().getRGB()), new Renderer3D.Vertex(matrix4f, (float)(vec32.x - TrajectoriesModule.mc.gameRenderer.mainCamera().position().x), (float)(vec32.y - TrajectoriesModule.mc.gameRenderer.mainCamera().position().y), (float)(vec32.z - TrajectoriesModule.mc.gameRenderer.mainCamera().position().z), this.lineColor.getColor().getRGB())));
        }
        if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
            AABB box = new AABB(blockHitResult.getLocation().x - 0.15, blockHitResult.getLocation().y - 0.15, blockHitResult.getLocation().z - 0.15, blockHitResult.getLocation().x + 0.15, blockHitResult.getLocation().y + 0.15, blockHitResult.getLocation().z + 0.15);
            if (this.blocksMode.getValue().equalsIgnoreCase("Fill") || this.blocksMode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(poseStack, box, this.blocksFillColor.getColor());
            }
            if (this.blocksMode.getValue().equalsIgnoreCase("Outline") || this.blocksMode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(poseStack, box, this.blocksOutlineColor.getColor());
            }
        }
        if (entity != null && !this.hitEntities.contains(entity)) {
            if (this.entitiesMode.getValue().equalsIgnoreCase("Fill") || this.entitiesMode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(poseStack, entity.getBoundingBox(), this.entitiesFillColor.getColor());
            }
            if (this.entitiesMode.getValue().equalsIgnoreCase("Outline") || this.entitiesMode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(poseStack, entity.getBoundingBox(), this.entitiesOutlineColor.getColor());
            }
            this.hitEntities.add(entity);
        }
    }

    private Entity getHitEntity(AABB aABB) {
        for (Entity entity : TrajectoriesModule.mc.level.entitiesForRendering()) {
            if (entity == TrajectoriesModule.mc.player || entity instanceof Arrow || !entity.getBoundingBox().intersects(aABB)) continue;
            return entity;
        }
        return null;
    }

    private float getThrowVelocity(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            return 0.5f;
        }
        if (item instanceof ExperienceBottleItem) {
            return 0.59f;
        }
        if (item instanceof TridentItem) {
            return 2.0f;
        }
        return 1.5f;
    }

    private int getThrowPitch(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem) {
            return 20;
        }
        return 0;
    }
}

