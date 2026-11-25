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
        float yaw = Mth.lerp((float)renderWorldEvent.getTickDelta(), (float)TrajectoriesModule.mc.player.yRotO, (float)TrajectoriesModule.mc.player.getYRot());
        if (TrajectoriesModule.mc.player.getOffhandItem().getItem() instanceof CrossbowItem && EnchantmentHelper.getItemEnchantmentLevel((Holder)TrajectoriesModule.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), (ItemInstance)TrajectoriesModule.mc.player.getOffhandItem()) != 0 || TrajectoriesModule.mc.player.getMainHandItem().getItem() instanceof CrossbowItem && EnchantmentHelper.getItemEnchantmentLevel((Holder)TrajectoriesModule.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), (ItemInstance)TrajectoriesModule.mc.player.getMainHandItem()) != 0) {
            this.project(renderWorldEvent.getMatrices(), interactionHand == InteractionHand.OFF_HAND ? TrajectoriesModule.mc.player.getOffhandItem().getItem() : TrajectoriesModule.mc.player.getMainHandItem().getItem(), yaw - 10.0f, renderWorldEvent.getTickDelta());
            this.project(renderWorldEvent.getMatrices(), interactionHand == InteractionHand.OFF_HAND ? TrajectoriesModule.mc.player.getOffhandItem().getItem() : TrajectoriesModule.mc.player.getMainHandItem().getItem(), yaw, renderWorldEvent.getTickDelta());
            this.project(renderWorldEvent.getMatrices(), interactionHand == InteractionHand.OFF_HAND ? TrajectoriesModule.mc.player.getOffhandItem().getItem() : TrajectoriesModule.mc.player.getMainHandItem().getItem(), yaw + 10.0f, renderWorldEvent.getTickDelta());
        } else {
            this.project(renderWorldEvent.getMatrices(), interactionHand == InteractionHand.OFF_HAND ? TrajectoriesModule.mc.player.getOffhandItem().getItem() : TrajectoriesModule.mc.player.getMainHandItem().getItem(), yaw, renderWorldEvent.getTickDelta());
        }
    }

    private void project(PoseStack poseStack, Item item, float yaw, float tickDelta) {
        Vec3 prevPos;
        double posX = Mth.lerp((double)tickDelta, (double)TrajectoriesModule.mc.player.xo, (double)TrajectoriesModule.mc.player.getX());
        double posY = Mth.lerp((double)tickDelta, (double)TrajectoriesModule.mc.player.yo, (double)TrajectoriesModule.mc.player.getY());
        double posZ = Mth.lerp((double)tickDelta, (double)TrajectoriesModule.mc.player.zo, (double)TrajectoriesModule.mc.player.getZ());
        posY = posY + (double)TrajectoriesModule.mc.player.getEyeHeight(TrajectoriesModule.mc.player.getPose()) - 0.1000000014901161;
        float initialScale = item instanceof BowItem ? 1.0f : 0.4f;
        float pitch = Mth.lerp((float)tickDelta, (float)TrajectoriesModule.mc.player.xRotO, (float)TrajectoriesModule.mc.player.getXRot());
        double velX = -Mth.sin((double)(yaw / 180.0f * (float)Math.PI)) * Mth.cos((double)(pitch / 180.0f * (float)Math.PI)) * initialScale;
        double velY = -Mth.sin((double)((pitch - (float)this.getThrowPitch(item)) / 180.0f * 3.141593f)) * initialScale;
        double velZ = Mth.cos((double)(yaw / 180.0f * (float)Math.PI)) * Mth.cos((double)(pitch / 180.0f * (float)Math.PI)) * initialScale;
        float bowCharge = (float)TrajectoriesModule.mc.player.getTicksUsingItem() / 20.0f;
        if ((bowCharge = (bowCharge * bowCharge + bowCharge * 2.0f) / 3.0f) > 1.0f || bowCharge == 0.0f) {
            bowCharge = 1.0f;
        }
        float dirLength = Mth.sqrt((float)((float)(velX * velX + velY * velY + velZ * velZ)));
        velX /= (double)dirLength;
        velY /= (double)dirLength;
        velZ /= (double)dirLength;
        float throwSpeed = (item instanceof BowItem ? bowCharge * 2.0f : (item instanceof CrossbowItem ? 2.2f : 1.0f)) * this.getThrowVelocity(item);
        velX *= (double)throwSpeed;
        velY *= (double)throwSpeed;
        velZ *= (double)throwSpeed;
        if (!TrajectoriesModule.mc.player.onGround()) {
            velY += TrajectoriesModule.mc.player.getDeltaMovement().y;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        boolean hit = false;
        BlockHitResult blockHitResult = null;
        Entity entity = null;
        while (!hit && posY > -65.0) {
            prevPos = new Vec3(posX, posY, posZ);
            if (TrajectoriesModule.mc.level.getBlockState(new BlockPos((int)(posX += velX), (int)(posY += velY), (int)(posZ += velZ))).getBlock() == Blocks.WATER) {
                velX *= 0.8;
                velY *= 0.8;
                velZ *= 0.8;
            } else {
                velX *= 0.99;
                velY *= 0.99;
                velZ *= 0.99;
            }
            velY = item instanceof BowItem ? velY - (double)0.05f : (TrajectoriesModule.mc.player.getMainHandItem().getItem() instanceof CrossbowItem ? velY - (double)0.05f : velY - (double)0.03f);
            Vec3 nextPos = new Vec3(posX, posY, posZ);
            Entity hitEntity = this.getHitEntity(new AABB(posX - 1.0, posY - 1.0, posZ - 1.0, posX + 1.0, posY + 1.0, posZ + 1.0));
            BlockHitResult hitBlock = TrajectoriesModule.mc.level.clip(new ClipContext(prevPos, nextPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)TrajectoriesModule.mc.player));
            if (hitEntity != null) {
                entity = hitEntity;
                hit = true;
            } else if (hitBlock != null && hitBlock.getType() != HitResult.Type.MISS) {
                blockHitResult = hitBlock;
                hit = true;
            }
            Renderer3D.DEBUG_LINES.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(matrix4f, (float)(prevPos.x - TrajectoriesModule.mc.gameRenderer.mainCamera().position().x), (float)(prevPos.y - TrajectoriesModule.mc.gameRenderer.mainCamera().position().y), (float)(prevPos.z - TrajectoriesModule.mc.gameRenderer.mainCamera().position().z), this.lineColor.getColor().getRGB()), new Renderer3D.Vertex(matrix4f, (float)(nextPos.x - TrajectoriesModule.mc.gameRenderer.mainCamera().position().x), (float)(nextPos.y - TrajectoriesModule.mc.gameRenderer.mainCamera().position().y), (float)(nextPos.z - TrajectoriesModule.mc.gameRenderer.mainCamera().position().z), this.lineColor.getColor().getRGB())));
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

