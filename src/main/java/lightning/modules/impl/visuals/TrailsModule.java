/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.arrow.AbstractArrow
 *  net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball
 *  net.minecraft.world.entity.projectile.hurtingprojectile.Fireball
 *  net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball
 *  net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball
 *  net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.ServerConnectEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Trails", description="Renders smooth trails behind projectiles and throwables.", category=Module.Category.VISUALS)
public class TrailsModule
extends Module {
    public CategorySetting targets = new CategorySetting("Targets", "The projectiles to render trails for.");
    public BooleanSetting pearls = new BooleanSetting("Pearls", "Renders trails behind ender pearls.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting arrows = new BooleanSetting("Arrows", "Renders trails behind arrows.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting witherSkull = new BooleanSetting("WitherSkull", "Renders trails behind wither skulls.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting egg = new BooleanSetting("Egg", "Renders trails behind eggs.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting snowball = new BooleanSetting("Snowball", "Renders trails behind snowballs.", new CategorySetting.Visibility(this.targets), true);
    public BooleanSetting fireCharge = new BooleanSetting("FireCharge", "Renders trails behind fire charges.", new CategorySetting.Visibility(this.targets), true);
    public NumberSetting fade = new NumberSetting("Fade", "The duration in milliseconds before a trail segment fades away.", 1000, 100, 5000);
    public ColorSetting color = new ColorSetting("Color", "The color used for the trails.", ColorUtils.getDefaultOutlineColor());
    private final Map<UUID, List<TrailPoint>> trails = new ConcurrentHashMap<UUID, List<TrailPoint>>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (TrailsModule.mc.level == null || TrailsModule.mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Entity entity : TrailsModule.mc.level.entitiesForRendering()) {
            if (!this.isValidTarget(entity) || !entity.isAlive()) continue;
            UUID entityId = entity.getUUID();
            Vec3 centerPos = entity.position().add(0.0, (double)entity.getBbHeight() * 0.5, 0.0);
            List<TrailPoint> points = this.trails.computeIfAbsent(entityId, id -> new ArrayList());
            synchronized (points) {
                if (points.isEmpty()) {
                    Projectile projectile;
                    Entity owner;
                    if (entity instanceof Projectile && (owner = (projectile = (Projectile)entity).getOwner()) instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity)owner;
                        points.add(new TrailPoint(livingEntity.getEyePosition().subtract(0.0, 0.1, 0.0), now));
                    } else if ((double)entity.distanceTo((Entity)TrailsModule.mc.player) <= 3.5 && TrailsModule.mc.player.isAlive()) {
                        points.add(new TrailPoint(TrailsModule.mc.player.getEyePosition().subtract(0.0, 0.1, 0.0), now));
                    }
                }
                if (points.isEmpty() || points.get(points.size() - 1).pos().distanceToSqr(centerPos) > 1.0E-4) {
                    points.add(new TrailPoint(centerPos, now));
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (TrailsModule.mc.level == null || TrailsModule.mc.player == null || this.trails.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        long fadeMs = this.fade.getValue().longValue();
        PoseStack poseStack = renderWorldEvent.getMatrices();
        Color color = this.color.getColor();
        HashMap<UUID, Entity> aliveEntities = new HashMap<UUID, Entity>();
        for (Entity entity : TrailsModule.mc.level.entitiesForRendering()) {
            if (!entity.isAlive()) continue;
            aliveEntities.put(entity.getUUID(), entity);
        }
        Iterator<Map.Entry<UUID, List<TrailPoint>>> iterator = this.trails.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<TrailPoint>> entry = iterator.next();
            UUID entityId = entry.getKey();
            List<TrailPoint> points = entry.getValue();
            Entity entity = aliveEntities.get(entityId);
            synchronized (points) {
                points.removeIf(trailPoint -> now - trailPoint.time() > fadeMs);
                if (points.isEmpty() && entity == null) {
                    iterator.remove();
                    continue;
                }
                if (points.size() < 1 && entity == null) {
                    continue;
                }
                for (int i = 0; i < points.size() - 1; ++i) {
                    TrailPoint p1 = points.get(i);
                    TrailPoint p2 = points.get(i + 1);
                    float alpha1 = fadeMs > 0L ? Mth.clamp((float)(1.0f - (float)(now - p1.time()) / (float)fadeMs), (float)0.0f, (float)1.0f) : 1.0f;
                    float alpha2 = fadeMs > 0L ? Mth.clamp((float)(1.0f - (float)(now - p2.time()) / (float)fadeMs), (float)0.0f, (float)1.0f) : 1.0f;
                    Color c1 = ColorUtils.getColor(color, Math.round((float)color.getAlpha() * alpha1));
                    Color c2 = ColorUtils.getColor(color, Math.round((float)color.getAlpha() * alpha2));
                    Renderer3D.renderLine(poseStack, p1.pos(), p2.pos(), c1, c2);
                }
                if (entity != null && !points.isEmpty()) {
                    TrailPoint lastPoint = points.get(points.size() - 1);
                    Vec3 currentPos = EntityUtils.getRenderPos(entity, renderWorldEvent.getTickDelta()).add(0.0, (double)entity.getBbHeight() * 0.5, 0.0);
                    float tailAlpha = fadeMs > 0L ? Mth.clamp((float)(1.0f - (float)(now - lastPoint.time()) / (float)fadeMs), (float)0.0f, (float)1.0f) : 1.0f;
                    Color tailColor = ColorUtils.getColor(color, Math.round((float)color.getAlpha() * tailAlpha));
                    Color headColor = ColorUtils.getColor(color, color.getAlpha());
                    Renderer3D.renderLine(poseStack, lastPoint.pos(), currentPos, tailColor, headColor);
                }
            }
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (this.pearls.getValue() && (entity instanceof ThrownEnderpearl || entity.getType() == EntityTypes.ENDER_PEARL)) {
            return true;
        }
        if (this.arrows.getValue() && (entity instanceof AbstractArrow || entity.getType() == EntityTypes.ARROW || entity.getType() == EntityTypes.SPECTRAL_ARROW)) {
            return true;
        }
        if (this.witherSkull.getValue() && (entity instanceof WitherSkull || entity.getType() == EntityTypes.WITHER_SKULL)) {
            return true;
        }
        if (this.egg.getValue() && (entity instanceof ThrownEgg || entity.getType() == EntityTypes.EGG)) {
            return true;
        }
        if (this.snowball.getValue() && (entity instanceof Snowball || entity.getType() == EntityTypes.SNOWBALL)) {
            return true;
        }
        return this.fireCharge.getValue() && (entity instanceof SmallFireball || entity instanceof LargeFireball || entity instanceof Fireball || entity instanceof DragonFireball || entity.getType() == EntityTypes.SMALL_FIREBALL || entity.getType() == EntityTypes.FIREBALL || entity.getType() == EntityTypes.DRAGON_FIREBALL);
    }

    @Override
    public void onDisable() {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent clientDisconnectEvent) {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onConnect(ClientConnectEvent clientConnectEvent) {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent serverConnectEvent) {
        this.trails.clear();
    }

    public record TrailPoint(Vec3 pos, long time) {
    }
}

