/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderOverlayEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="ArrowTracers", description="Renders arrows towards players who are off screen.", category=Module.Category.VISUALS)
public class ArrowTracersModule
extends Module {
    public NumberSetting width = new NumberSetting("Width", "The width of the arrows being rendered.", Float.valueOf(4.0f), Float.valueOf(0.5f), Float.valueOf(15.0f));
    public NumberSetting height = new NumberSetting("Height", "The height of the arrows being rendered.", Float.valueOf(8.0f), Float.valueOf(0.5f), Float.valueOf(15.0f));
    public NumberSetting distance = new NumberSetting("Distance", "The distance that will be between the crosshair and the arrows.", 45, 5, 200);
    public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
    public BooleanSetting onScreen = new BooleanSetting("OnScreen", "Renders the arrow tracers even for players who are on screen.", false);
    public ModeSetting alpha = new ModeSetting("Alpha", "The alpha that will be used in the arrow rendering.", "Fade", new String[]{"Default", "Fade"});
    public NumberSetting fadeDistance = new NumberSetting("FadeDistance", "The distance at which the arrows will start fading.", new ModeSetting.Visibility(this.alpha, "Fade"), (Number)Float.valueOf(100.0f), (Number)Float.valueOf(10.0f), (Number)Float.valueOf(200.0f));
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the arrows", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultOutlineColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), new ColorSetting.Color(new Color(0, 0, 0), true, false));

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent renderOverlayEvent) {
        if (ArrowTracersModule.mc.player == null || ArrowTracersModule.mc.level == null) {
            return;
        }
        GuiGraphicsExtractor guiGraphicsExtractor = renderOverlayEvent.getContext();
        float f = (float)mc.getWindow().getGuiScaledWidth() / 2.0f;
        float f2 = (float)mc.getWindow().getGuiScaledHeight() / 2.0f;
        float f3 = this.distance.getValue().floatValue();
        float f4 = this.width.getValue().floatValue();
        float f5 = this.height.getValue().floatValue();
        Vec3 vec3 = EntityUtils.getRenderPos((Entity)ArrowTracersModule.mc.player, renderOverlayEvent.getTickDelta());
        for (Player player : ArrowTracersModule.mc.level.players()) {
            if (player == ArrowTracersModule.mc.player || EntityUtils.isGhost((Entity)player) || EntityUtils.isBot(player) && this.antiBot.getValue() || !this.onScreen.getValue() && Renderer3D.isFrustumVisible(player.getBoundingBox())) continue;
            Vec3 vec32 = EntityUtils.getRenderPos((Entity)player, renderOverlayEvent.getTickDelta());
            double d = vec32.x - vec3.x;
            double d2 = vec32.z - vec3.z;
            double d3 = Math.toDegrees(Math.atan2(d2, d)) - 90.0;
            double d4 = Mth.wrapDegrees((double)(d3 - (double)ArrowTracersModule.mc.player.getYRot()));
            int n = (int)Mth.clamp((float)(255.0f - 255.0f / this.fadeDistance.getValue().floatValue() * ArrowTracersModule.mc.player.distanceTo((Entity)player)), (float)80.0f, (float)255.0f);
            Color color = this.alpha.getValue().equalsIgnoreCase("Fade") ? ColorUtils.getColor(this.fillColor.getColor(), n) : this.fillColor.getColor();
            Color color2 = this.alpha.getValue().equalsIgnoreCase("Fade") ? ColorUtils.getColor(this.outlineColor.getColor(), n) : this.outlineColor.getColor();
            guiGraphicsExtractor.pose().pushMatrix();
            guiGraphicsExtractor.pose().translate(f, f2);
            guiGraphicsExtractor.pose().rotate((float)Math.toRadians(d4));
            guiGraphicsExtractor.pose().translate(-f, -f2);
            if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer2D.renderArrow(guiGraphicsExtractor, f, f2 - f3, f4, f5, color);
            }
            if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer2D.renderArrowOutline(guiGraphicsExtractor, f, f2 - f3, f4, f5, color2);
            }
            guiGraphicsExtractor.pose().popMatrix();
        }
    }
}

