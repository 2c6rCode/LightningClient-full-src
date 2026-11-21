/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.arrow.Arrow
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.IMinecraft;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

@RegisterModule(name="Icons", description="Renders icons for specific events.", category=Module.Category.VISUALS)
public class IconsModule
extends Module {
    public CategorySetting chorusCategory = new CategorySetting("Chorus", "The category for chorus icons.");
    public BooleanSetting chorus = new BooleanSetting("Chorus", "Enabled", "Renders an icon for chorus positions.", new CategorySetting.Visibility(this.chorusCategory), true);
    public ColorSetting chorusColor = new ColorSetting("ChorusColor", "Color", "The color for the chorus icons.", new CategorySetting.Visibility(this.chorusCategory), new ColorSetting.Color(new Color(192, 147, 212), false, false));
    public NumberSetting duration = new NumberSetting("Duration", "The duration of icon renders.", new CategorySetting.Visibility(this.chorusCategory), (Number)1500, (Number)0, (Number)5000);
    public CategorySetting pearlsCategory = new CategorySetting("Pearls", "The category for pearl icons.");
    public BooleanSetting pearls = new BooleanSetting("Pearls", "Enabled", "Renders an icon for pearl positions.", new CategorySetting.Visibility(this.pearlsCategory), true);
    public ColorSetting pearlsColor = new ColorSetting("PearlsColor", "Color", "The color for the pearl icons.", new CategorySetting.Visibility(this.pearlsCategory), new ColorSetting.Color(new Color(30, 131, 89), false, false));
    public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the text ESP rendering.", 40, 10, 50);
    private final ArrayList<Icon> iconList = new ArrayList();
    private final Map<Icon, Integer> pearlMap = new HashMap<Icon, Integer>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent entitySpawnEvent) {
        ArrayList<Icon> arrayList;
        if (this.getNull()) {
            return;
        }
        ArrayList<Icon> arrayList2 = arrayList = this.iconList;
        synchronized (arrayList2) {
            Entity entity = entitySpawnEvent.getEntity();
            if (entity instanceof ThrownEnderpearl) {
                ThrownEnderpearl thrownEnderpearl = (ThrownEnderpearl)entity;
                IconsModule.mc.level.players().stream().min(Comparator.comparingDouble(abstractClientPlayer -> abstractClientPlayer.distanceTo((Entity)thrownEnderpearl))).ifPresent(abstractClientPlayer -> {
                    Vec3 vec3 = this.projectPearl(thrownEnderpearl.position(), abstractClientPlayer.getYRot(), abstractClientPlayer.getXRot(), abstractClientPlayer.getDeltaMovement());
                    if (vec3 != null) {
                        Icon icon = new Icon(this, this, new Vec3(vec3.x, vec3.y, vec3.z), Icon.Type.PEARL);
                        this.pearlMap.put(icon, thrownEnderpearl.getId());
                        this.iconList.add(icon);
                    }
                });
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
        ArrayList<Icon> arrayList;
        if (this.getNull()) {
            return;
        }
        ArrayList<Icon> arrayList2 = arrayList = this.iconList;
        synchronized (arrayList2) {
            ClientboundSoundPacket clientboundSoundPacket;
            SoundEvent soundEvent;
            Packet<?> packet = packetReceiveEvent.getPacket();
            if (packet instanceof ClientboundSoundPacket && ((soundEvent = (SoundEvent)(clientboundSoundPacket = (ClientboundSoundPacket)packet).getSound().value()) == SoundEvents.CHORUS_FRUIT_TELEPORT || soundEvent == SoundEvents.ENDERMAN_TELEPORT)) {
                this.iconList.add(new Icon(this, this, new Vec3(clientboundSoundPacket.getX(), clientboundSoundPacket.getY(), clientboundSoundPacket.getZ()), Icon.Type.CHORUS));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        ArrayList<Icon> arrayList;
        if (this.getNull()) {
            return;
        }
        PoseStack poseStack = renderWorldEvent.getMatrices();
        ArrayList<Icon> arrayList2 = arrayList = this.iconList;
        synchronized (arrayList2) {
            this.iconList.removeIf(icon -> icon.type == Icon.Type.CHORUS && System.currentTimeMillis() - icon.time >= (long)(this.duration.getValue().intValue() + 1200) || icon.type == Icon.Type.PEARL && !(IconsModule.mc.level.getEntity(this.pearlMap.get(icon).intValue()) instanceof ThrownEnderpearl) && icon.animation.value() == 0.0f);
            if (this.chorus.getValue()) {
                for (Icon icon2 : this.iconList.stream().filter(icon -> icon.type == Icon.Type.CHORUS).toList()) {
                    icon2.render(poseStack, System.currentTimeMillis() - icon2.time >= (long)(600 + this.duration.getValue().intValue()) ? 0.0f : 1.0f, () -> Renderer2D.renderTexture(poseStack, -6.0f, -6.5f, 6.0f, 6.5f, Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/chorus.png"), Color.WHITE), this.chorusColor.getColor());
                }
            }
            if (this.pearls.getValue()) {
                for (Icon icon2 : this.iconList.stream().filter(icon -> icon.type == Icon.Type.PEARL).toList()) {
                    icon2.render(poseStack, !(IconsModule.mc.level.getEntity(this.pearlMap.get(icon2).intValue()) instanceof ThrownEnderpearl) ? 0.0f : 1.0f, () -> Renderer2D.renderTexture(poseStack, -6.5f, -6.5f, 6.5f, 6.5f, Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/pearl.png"), Color.WHITE), this.pearlsColor.getColor());
                }
            }
        }
    }

    private Vec3 projectPearl(Vec3 vec3, float f, float f2, Vec3 vec32) {
        double d = vec3.x;
        double d2 = vec3.y;
        double d3 = vec3.z;
        d2 = d2 + (double)IconsModule.mc.player.getEyeHeight(IconsModule.mc.player.getPose()) - 0.1000000014901161;
        float f3 = 0.4f;
        double d4 = -Mth.sin((double)(f / 180.0f * (float)Math.PI)) * Mth.cos((double)(f2 / 180.0f * (float)Math.PI)) * f3;
        double d5 = -Mth.sin((double)(f2 / 180.0f * 3.141593f)) * f3;
        double d6 = Mth.cos((double)(f / 180.0f * (float)Math.PI)) * Mth.cos((double)(f2 / 180.0f * (float)Math.PI)) * f3;
        float f4 = Mth.sqrt((float)((float)(d4 * d4 + d5 * d5 + d6 * d6)));
        d4 /= (double)f4;
        d5 /= (double)f4;
        d6 /= (double)f4;
        float f5 = 1.5f;
        d4 *= (double)f5;
        d5 *= (double)f5;
        d6 *= (double)f5;
        d4 += vec32.x;
        d5 += vec32.y;
        d6 += vec32.z;
        while (d2 > -65.0) {
            Vec3 vec33 = new Vec3(d, d2, d3);
            if (IconsModule.mc.level.getBlockState(new BlockPos((int)(d += d4), (int)(d2 += d5), (int)(d3 += d6))).getBlock() == Blocks.WATER) {
                d4 *= 0.8;
                d5 *= 0.8;
                d6 *= 0.8;
            } else {
                d4 *= 0.99;
                d5 *= 0.99;
                d6 *= 0.99;
            }
            d5 -= (double)0.03f;
            Vec3 vec34 = new Vec3(d, d2, d3);
            for (Entity entity : IconsModule.mc.level.entitiesForRendering()) {
                if (entity == IconsModule.mc.player || entity instanceof Arrow || entity instanceof ThrownEnderpearl || !entity.getBoundingBox().intersects(new AABB(d - 1.0, d2 - 1.0, d3 - 1.0, d + 1.0, d2 + 1.0, d3 + 1.0))) continue;
                return entity.getBoundingBox().getCenter();
            }
            BlockHitResult blockHitResult = IconsModule.mc.level.clip(new ClipContext(vec33, vec34, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)IconsModule.mc.player));
            if (blockHitResult == null || blockHitResult.getType() == HitResult.Type.MISS) continue;
            return blockHitResult.getLocation();
        }
        return null;
    }

    private class Icon {
        private final Vec3 pos;
        private final Animation animation;
        private final Type type;
        private final long time;
        final IconsModule this$0;

        public Icon(IconsModule iconsModule, IconsModule iconsModule2, Vec3 vec3, Type type) {
            Objects.requireNonNull(iconsModule);
            IconsModule iconsModule3 = iconsModule2;
            Objects.requireNonNull(iconsModule3);
            this.this$0 = iconsModule3;
            this.pos = vec3;
            this.animation = new Animation(0.0f, 1.0f, 600, Easing.Method.EASE_IN_OUT_ELASTIC);
            this.type = type;
            this.time = System.currentTimeMillis();
        }

        private void render(PoseStack poseStack, float f, Runnable runnable, Color color) {
            float f2 = this.animation.get(f);
            float f3 = (float)Math.sqrt(IMinecraft.mc.gameRenderer.mainCamera().position().distanceToSqr(this.pos.x, this.pos.y, this.pos.z));
            float f4 = 0.0018f + this.this$0.scale.getValue().floatValue() / 10000.0f * f3;
            if ((double)f3 <= 4.0) {
                f4 = 0.0245f;
            }
            Vec3 vec3 = new Vec3(this.pos.x - IMinecraft.mc.gameRenderer.mainCamera().position().x, this.pos.y - IMinecraft.mc.gameRenderer.mainCamera().position().y, this.pos.z - IMinecraft.mc.gameRenderer.mainCamera().position().z);
            poseStack.pushPose();
            poseStack.translate(vec3.x, vec3.y, vec3.z);
            poseStack.mulPose((Quaternionfc)IMinecraft.mc.gameRenderer.mainCamera().rotation());
            poseStack.scale(f4, -f4, f4);
            poseStack.scale(f2, f2, 1.0f);
            Renderer2D.renderCircle(poseStack, 0.0f, 0.0f, 12.0f, new Color(0, 0, 0, 100));
            Renderer2D.renderCircle(poseStack, 0.0f, 0.0f, 10.0f, color);
            runnable.run();
            poseStack.popPose();
        }

        @Generated
        public Vec3 getPos() {
            return this.pos;
        }

        @Generated
        public Animation getAnimation() {
            return this.animation;
        }

        @Generated
        public Type getType() {
            return this.type;
        }

        @Generated
        public long getTime() {
            return this.time;
        }

        public static enum Type {
            CHORUS,
            PEARL;

        }
    }
}

