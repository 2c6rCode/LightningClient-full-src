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
        Vec3 vec3;
        if (this.getNull()) {
            return;
        }
        if (TracersModule.mc.player == null) {
            return;
        }
        IFreecamModule iFreecamModule = (IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"));
        boolean bl = iFreecamModule != null && iFreecamModule.isToggled();
        boolean bl2 = bl;
        if (bl) {
            float f = iFreecamModule.getFreePitch();
            float f2 = iFreecamModule.getFreeYaw();
            float f3 = f * ((float)Math.PI / 180);
            float f4 = -f2 * ((float)Math.PI / 180);
            float f5 = Mth.cos((double)f4);
            float f6 = Mth.sin((double)f4);
            float f7 = Mth.cos((double)f3);
            float f8 = Mth.sin((double)f3);
            Vec3 vec32 = new Vec3((double)(f6 * f7), (double)(-f8), (double)(f5 * f7));
            vec3 = new Vec3(iFreecamModule.getFreeX(), iFreecamModule.getFreeY(), iFreecamModule.getFreeZ()).add(vec32);
        } else {
            Vec3 vec33 = TracersModule.mc.gameRenderer.mainCamera().position();
            Vec3 vec34 = Vec3.directionFromRotation((float)TracersModule.mc.gameRenderer.mainCamera().xRot(), (float)TracersModule.mc.gameRenderer.mainCamera().yRot());
            vec3 = vec33.add(vec34);
        }
        for (Player player : TracersModule.mc.level.players()) {
            if (player == TracersModule.mc.player && !bl || EntityUtils.isGhost((Entity)player) || EntityUtils.isBot(player) && this.antiBot.getValue() || Night.MODULE_MANAGER.getModule(IgnoreNakedModule.class).isToggled() && EntityUtils.isNaked(player)) continue;
            Vec3 vec35 = EntityUtils.getRenderPos((Entity)player, renderWorldEvent.getTickDelta());
            Renderer3D.renderLine(Renderer3D.TRACER_DEBUG_LINES, renderWorldEvent.getMatrices(), vec3, vec35, this.getColor(player, vec3));
        }
    }

    private Color getColor(Player player, Vec3 vec3) {
        if (Night.FRIEND_MANAGER.contains(player.getName().getString())) {
            return Night.FRIEND_MANAGER.getDefaultFriendColor(this.color.getColor().getAlpha());
        }
        if (this.mode.getValue().equals("Custom")) {
            return this.color.getColor();
        }
        float f = 80.0f;
        double d = vec3.distanceToSqr(player.getX(), player.getY(), player.getZ());
        float f2 = Mth.clamp((float)((float)Math.sqrt(d)), (float)0.0f, (float)f);
        return new Color((f - f2) / f, 1.0f - (f - f2) / f, 0.0f, (float)this.color.getColor().getAlpha() / 255.0f);
    }
}

