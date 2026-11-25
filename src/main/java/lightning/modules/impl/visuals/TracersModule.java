/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.core.IgnoreNakedModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Tracers", description="Renders a line showing where other players are located.", category=Module.Category.VISUALS)
public class TracersModule
extends Module {
    public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the tracers color.", "Distance", new String[]{"Distance", "Custom"});
    public ColorSetting color = new ColorSetting("Color", "The color used for the fill rendering.", ColorUtils.getDefaultOutlineColor());

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        Vec3 startPos;
        if (this.getNull()) {
            return;
        }
        if (TracersModule.mc.player == null) {
            return;
        }
        IFreecamModule iFreecamModule = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"));
        boolean isFreecam = iFreecamModule != null && iFreecamModule.isToggled();
        if (isFreecam) {
            float pitch = iFreecamModule.getFreePitch();
            float yaw = iFreecamModule.getFreeYaw();
            float pitchRad = pitch * ((float)Math.PI / 180);
            float yawRad = -yaw * ((float)Math.PI / 180);
            float cosYaw = Mth.cos((double)yawRad);
            float sinYaw = Mth.sin((double)yawRad);
            float cosPitch = Mth.cos((double)pitchRad);
            float sinPitch = Mth.sin((double)pitchRad);
            Vec3 lookDir = new Vec3((double)(sinYaw * cosPitch), (double)(-sinPitch), (double)(cosYaw * cosPitch));
            startPos = new Vec3(iFreecamModule.getFreeX(), iFreecamModule.getFreeY(), iFreecamModule.getFreeZ()).add(lookDir);
        } else {
            Vec3 camPos = TracersModule.mc.gameRenderer.mainCamera().position();
            Vec3 camDir = Vec3.directionFromRotation((float)TracersModule.mc.gameRenderer.mainCamera().xRot(), (float)TracersModule.mc.gameRenderer.mainCamera().yRot());
            startPos = camPos.add(camDir);
        }
        for (Player player : TracersModule.mc.level.players()) {
            if (player == TracersModule.mc.player && !isFreecam || EntityUtils.isGhost((Entity)player) || EntityUtils.isBot(player) && this.antiBot.getValue() || Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled() && EntityUtils.isNaked(player)) continue;
            Vec3 targetPos = EntityUtils.getRenderPos((Entity)player, renderWorldEvent.getTickDelta());
            Renderer3D.renderLine(Renderer3D.TRACER_DEBUG_LINES, renderWorldEvent.getMatrices(), startPos, targetPos, this.getColor(player, startPos));
        }
    }

    private Color getColor(Player player, Vec3 fromPos) {
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return Night.FRIEND_MANAGER.getDefaultFriendColor(this.color.getColor().getAlpha());
        }
        if (this.mode.getValue().equals("Custom")) {
            return this.color.getColor();
        }
        float maxDistance = 80.0f;
        double distSq = fromPos.distanceToSqr(player.getX(), player.getY(), player.getZ());
        float clampedDist = Mth.clamp((float)((float)Math.sqrt(distSq)), (float)0.0f, (float)maxDistance);
        return new Color((maxDistance - clampedDist) / maxDistance, 1.0f - (maxDistance - clampedDist) / maxDistance, 0.0f, (float)this.color.getColor().getAlpha() / 255.0f);
    }
}

