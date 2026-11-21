/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.ParticleManager;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Particles", description="Custom particle effects for totem pops and entity hits.", category=Module.Category.VISUALS)
public class ParticlesModule
extends Module {
    private final ParticleManager particleManager = new ParticleManager();
    public CategorySetting totemCategory = new CategorySetting("Totem", "Settings for totem pop particle effects.");
    public BooleanSetting totem = new BooleanSetting("Totem", "Enabled", "Enables totem particle customization.", new CategorySetting.Visibility(this.totemCategory), true);
    public ModeSetting totemMode = new ModeSetting("TotemMode", "Mode", "Particle mode for totem pops.", new CategorySetting.Visibility(this.totemCategory), "Vanilla", new String[]{"Vanilla", "Custom"});
    public NumberSetting totemNumber = new NumberSetting("TotemNumber", "Number", "Amount of particles per totem pop.", new CategorySetting.Visibility(this.totemCategory), 15, 1, 100);
    public ColorSetting vanillaColorA = new ColorSetting("VanillaColorA", "Color A", "Primary particle color for vanilla totem pop.", new ModeSetting.Visibility(this.totemMode, "Vanilla"), new ColorSetting.Color(new Color(255, 255, 255), false, false));
    public ColorSetting vanillaColorB = new ColorSetting("VanillaColorB", "Color B", "Fade particle color for vanilla totem pop.", new ModeSetting.Visibility(this.totemMode, "Vanilla"), new ColorSetting.Color(new Color(255, 255, 255), false, false));
    public ColorSetting customColorA = new ColorSetting("CustomColorA", "Color A", "Primary particle color.", new ModeSetting.Visibility(this.totemMode, "Custom"), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public ColorSetting customColorB = new ColorSetting("CustomColorB", "Color B", "Secondary particle color.", new ModeSetting.Visibility(this.totemMode, "Custom"), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public BooleanSetting totemGlow = new BooleanSetting("TotemGlow", "Glow", "Adds a glow effect to totem particles.", new ModeSetting.Visibility(this.totemMode, "Custom"), true);
    public BooleanSetting totemPhysics = new BooleanSetting("TotemPhysics", "Physics", "Enables gravity and bouncing for totem particles.", new ModeSetting.Visibility(this.totemMode, "Custom"), true);
    public NumberSetting totemVelocityX = new NumberSetting("TotemVelocityX", "X", "X velocity multiplier.", new ModeSetting.Visibility(this.totemMode, "Custom"), 1.0, -5.0, 5.0);
    public NumberSetting totemVelocityY = new NumberSetting("TotemVelocityY", "Y", "Y velocity multiplier.", new ModeSetting.Visibility(this.totemMode, "Custom"), 1.0, -5.0, 5.0);
    public NumberSetting totemVelocityZ = new NumberSetting("TotemVelocityZ", "Z", "Z velocity multiplier.", new ModeSetting.Visibility(this.totemMode, "Custom"), 1.0, -5.0, 5.0);
    public CategorySetting hitCategory = new CategorySetting("Hit", "Settings for hit particle effects.");
    public BooleanSetting hit = new BooleanSetting("Hit", "Enabled", "Enables hit particles.", new CategorySetting.Visibility(this.hitCategory), true);
    public NumberSetting hitNumber = new NumberSetting("HitNumber", "Number", "Amount of particles per hit.", new CategorySetting.Visibility(this.hitCategory), 15, 1, 100);
    public ColorSetting hitColorA = new ColorSetting("HitColorA", "Color A", "Primary particle color.", new CategorySetting.Visibility(this.hitCategory), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public ColorSetting hitColorB = new ColorSetting("HitColorB", "Color B", "Secondary particle color.", new CategorySetting.Visibility(this.hitCategory), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public BooleanSetting hitGlow = new BooleanSetting("HitGlow", "Glow", "Adds a glow effect to hit particles.", new CategorySetting.Visibility(this.hitCategory), true);
    public BooleanSetting hitPhysics = new BooleanSetting("HitPhysics", "Physics", "Enables gravity and bouncing for hit particles.", new CategorySetting.Visibility(this.hitCategory), false);

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent playerPopEvent) {
        if (playerPopEvent.getPlayer() == null || !this.totem.getValue()) {
            return;
        }
        if (!this.totemMode.getValue().equalsIgnoreCase("Custom")) {
            return;
        }
        Vec3 vec3 = playerPopEvent.getPlayer().getBoundingBox().getCenter();
        Vec3 vec32 = new Vec3(this.totemVelocityX.getValue().doubleValue(), this.totemVelocityY.getValue().doubleValue(), this.totemVelocityZ.getValue().doubleValue());
        this.spawnTotemParticles(vec3, vec32);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent attackEntityEvent) {
        if (attackEntityEvent.getTarget() == null || !this.hit.getValue()) {
            return;
        }
        Vec3 vec3 = attackEntityEvent.getTarget().getBoundingBox().getCenter();
        this.spawnHitParticles(vec3);
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (ParticlesModule.mc.level == null || ParticlesModule.mc.player == null) {
            this.particleManager.clear();
            return;
        }
        this.particleManager.tick();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (this.particleManager.isEmpty()) {
            return;
        }
        this.particleManager.render(renderWorldEvent.getMatrices(), renderWorldEvent.getTickDelta());
    }

    private void spawnTotemParticles(Vec3 vec3, Vec3 vec32) {
        int n = this.customColorA.getColor().getRGB();
        int n2 = this.customColorB.getColor().getRGB();
        boolean bl = this.totemGlow.getValue();
        boolean bl2 = this.totemPhysics.getValue();
        int n3 = this.totemNumber.getValue().intValue();
        for (int i = 0; i < n3; ++i) {
            Vec3 vec33 = ParticleManager.randomMotion(0.5 * (0.75 + Math.random() * 0.25));
            vec33 = new Vec3(vec33.x * vec32.x, vec33.y * vec32.y, vec33.z * vec32.z);
            if (bl2) {
                this.particleManager.addBouncy(vec3, vec33, 1.5, n, n2, bl);
                continue;
            }
            this.particleManager.addFriction(vec3, vec33, 0.9, 1.5, n, n2, bl);
        }
    }

    private void spawnHitParticles(Vec3 vec3) {
        int n = this.hitColorA.getColor().getRGB();
        int n2 = this.hitColorB.getColor().getRGB();
        boolean bl = this.hitGlow.getValue();
        boolean bl2 = this.hitPhysics.getValue();
        int n3 = this.hitNumber.getValue().intValue();
        for (int i = 0; i < n3; ++i) {
            Vec3 vec32 = ParticleManager.randomMotion(0.5 * (0.75 + Math.random() * 0.25));
            if (bl2) {
                this.particleManager.addBouncy(vec3, vec32, 1.0, n, n2, bl);
                continue;
            }
            this.particleManager.addFriction(vec3, vec32, 0.9, 1.0, n, n2, bl);
        }
    }

    @Generated
    public ParticleManager getParticleManager() {
        return this.particleManager;
    }
}

