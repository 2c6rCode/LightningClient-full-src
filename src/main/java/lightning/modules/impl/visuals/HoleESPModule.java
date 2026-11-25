/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.AABB
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.HoleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

@RegisterModule(name="HoleESP", description="Highlights all holes in a specified radius.", category=Module.Category.VISUALS)
public class HoleESPModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which holes will be rendered.", 10, 1, 50);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public BooleanSetting doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to render the ESP on double holes.", true);
    public BooleanSetting quadHoles = new BooleanSetting("QuadHoles", "Whether or not to render the ESP on quad holes.", true);
    public BooleanSetting partialHoles = new BooleanSetting("PartialHoles", "Whether or not to render the ESP on partial (incomplete) holes.", true);
    public BooleanSetting fade = new BooleanSetting("Fade", "Fades the holes in and out based on your distance to them.", false);
    public ModeSetting fill = new ModeSetting("Fill", "The mode for the fill rendering on the hole boxes.", "Normal", new String[]{"None", "Normal", "Gradient"});
    public NumberSetting fillHeight = new NumberSetting("FillHeight", "The height of the fill rendering on the holes.", new ModeSetting.Visibility(this.fill, "Normal", "Gradient"), 1.0, -2.0, 2.0);
    public ModeSetting outline = new ModeSetting("Outline", "The mode for the outline rendering on the hole boxes.", "Normal", new String[]{"None", "Normal", "Gradient"});
    public NumberSetting outlineHeight = new NumberSetting("OutlineHeight", "The height of the outline rendering on the holes.", new ModeSetting.Visibility(this.outline, "Normal", "Gradient"), 1.0, -2.0, 2.0);
    public BooleanSetting wireframe = new BooleanSetting("Wireframe", "Renders an X wireframe on the hole boxes.", false);
    public CategorySetting safeColorsCategory = new CategorySetting("Safe", "The category that contains the settings for coloring of safe (all-bedrock) holes.");
    public ColorSetting safeFillColor = new ColorSetting("SafeFillColor", "Fill", "The color for the fill rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting safeOutlineColor = new ColorSetting("SafeOutlineColor", "Outline", "The color for the outline rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public ColorSetting safeWireframeColor = new ColorSetting("SafeWireframeColor", "Wireframe", "The color for the wireframe rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, 125), false, false));
    public ModeSetting safeShader = new ModeSetting("SafeShader", "Shader", "The animated shader for safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), "None", EspShader.MODES);
    public NumberSetting safeShaderOpacity = new NumberSetting("SafeShaderOpacity", "Opacity", "Safe hole shader opacity.", new ModeSetting.Visibility(this.safeShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting safeShaderSpeed = new NumberSetting("SafeShaderSpeed", "Speed", "Safe hole shader speed.", new ModeSetting.Visibility(this.safeShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
    public NumberSetting safeShaderStep = new NumberSetting("SafeShaderStep", "Step", "Safe hole gradient step size.", new ModeSetting.Visibility(this.safeShader, "Gradient"), 50.0, 0.1, 200.0);
    public ColorSetting safeShaderColor1 = new ColorSetting("SafeShaderColor1", "The first gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Gradient"), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
    public ColorSetting safeShaderColor2 = new ColorSetting("SafeShaderColor2", "The second gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Gradient"), new ColorSetting.Color(new Color(0, 200, 100, 255), false, false));
    public ColorSetting safeShaderColor3 = new ColorSetting("SafeShaderColor3", "The third gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Gradient"), new ColorSetting.Color(new Color(0, 255, 200, 255), false, false));
    public ColorSetting safeShaderColor4 = new ColorSetting("SafeShaderColor4", "The fourth gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Gradient"), new ColorSetting.Color(new Color(100, 255, 0, 255), false, false));
    public ColorSetting safeShaderGlowColor = new ColorSetting("SafeShaderGlowColor", "GlowColor", "The glow tint color for safe holes.", new ModeSetting.Visibility(this.safeShader, "Glowing"), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
    public CategorySetting unsafeColorsCategory = new CategorySetting("Unsafe", "The category that contains the settings for coloring of unsafe (blast-proof but not pure bedrock) holes.");
    public ColorSetting unsafeFillColor = new ColorSetting("UnsafeFillColor", "Fill", "The color for the fill rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting unsafeOutlineColor = new ColorSetting("UnsafeOutlineColor", "Outline", "The color for the outline rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public ColorSetting unsafeWireframeColor = new ColorSetting("UnsafeWireframeColor", "Wireframe", "The color for the wireframe rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, 125), false, false));
    public ModeSetting unsafeShader = new ModeSetting("UnsafeShader", "Shader", "The animated shader for unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), "None", EspShader.MODES);
    public NumberSetting unsafeShaderOpacity = new NumberSetting("UnsafeShaderOpacity", "Opacity", "Unsafe hole shader opacity.", new ModeSetting.Visibility(this.unsafeShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting unsafeShaderSpeed = new NumberSetting("UnsafeShaderSpeed", "Speed", "Unsafe hole shader speed.", new ModeSetting.Visibility(this.unsafeShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
    public NumberSetting unsafeShaderStep = new NumberSetting("UnsafeShaderStep", "Step", "Unsafe hole gradient step size.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), 50.0, 0.1, 200.0);
    public ColorSetting unsafeShaderColor1 = new ColorSetting("UnsafeShaderColor1", "The first gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public ColorSetting unsafeShaderColor2 = new ColorSetting("UnsafeShaderColor2", "The second gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), new ColorSetting.Color(new Color(255, 100, 0, 255), false, false));
    public ColorSetting unsafeShaderColor3 = new ColorSetting("UnsafeShaderColor3", "The third gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 100, 255), false, false));
    public ColorSetting unsafeShaderColor4 = new ColorSetting("UnsafeShaderColor4", "The fourth gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Gradient"), new ColorSetting.Color(new Color(200, 0, 0, 255), false, false));
    public ColorSetting unsafeShaderGlowColor = new ColorSetting("UnsafeShaderGlowColor", "GlowColor", "The glow tint color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, "Glowing"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public CategorySetting partialColorsCategory = new CategorySetting("Partial", "The category that contains the settings for coloring of partial (incomplete) holes.");
    public ColorSetting partialFillColor = new ColorSetting("PartialFillColor", "Fill", "The color for the fill rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public ColorSetting partialOutlineColor = new ColorSetting("PartialOutlineColor", "Outline", "The color for the outline rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public ColorSetting partialWireframeColor = new ColorSetting("PartialWireframeColor", "Wireframe", "The color for the wireframe rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, 125), false, false));
    public ModeSetting partialShader = new ModeSetting("PartialShader", "Shader", "The animated shader for partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), "None", EspShader.MODES);
    public NumberSetting partialShaderOpacity = new NumberSetting("PartialShaderOpacity", "Opacity", "Partial hole shader opacity.", new ModeSetting.Visibility(this.partialShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting partialShaderSpeed = new NumberSetting("PartialShaderSpeed", "Speed", "Partial hole shader speed.", new ModeSetting.Visibility(this.partialShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
    public NumberSetting partialShaderStep = new NumberSetting("PartialShaderStep", "Step", "Partial hole gradient step size.", new ModeSetting.Visibility(this.partialShader, "Gradient"), 50.0, 0.1, 200.0);
    public ColorSetting partialShaderColor1 = new ColorSetting("PartialShaderColor1", "The first gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Gradient"), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    public ColorSetting partialShaderColor2 = new ColorSetting("PartialShaderColor2", "The second gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Gradient"), new ColorSetting.Color(new Color(200, 200, 200, 255), false, false));
    public ColorSetting partialShaderColor3 = new ColorSetting("PartialShaderColor3", "The third gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Gradient"), new ColorSetting.Color(new Color(220, 220, 220, 255), false, false));
    public ColorSetting partialShaderColor4 = new ColorSetting("PartialShaderColor4", "The fourth gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Gradient"), new ColorSetting.Color(new Color(180, 180, 180, 255), false, false));
    public ColorSetting partialShaderGlowColor = new ColorSetting("PartialShaderGlowColor", "GlowColor", "The glow tint color for partial holes.", new ModeSetting.Visibility(this.partialShader, "Glowing"), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<HoleUtils.Hole> holes = Collections.synchronizedList(new ArrayList());

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (HoleESPModule.mc.player == null || HoleESPModule.mc.level == null) {
            return;
        }
        BlockPos playerPos = HoleESPModule.mc.player.blockPosition();
        int radiusCount = Night.WORLD_MANAGER.getRadius(this.range.getValue().doubleValue());
        Runnable runnable = () -> {
            ArrayList<HoleUtils.Hole> foundHoles = new ArrayList<>();
            for (int i = 0; i < radiusCount; ++i) {
                HoleUtils.Hole quadHole;
                HoleUtils.Hole doubleHole;
                BlockPos offsetPos = playerPos.offset(Night.WORLD_MANAGER.getOffset(i));
                HoleUtils.Hole singleHole = HoleUtils.getSingleHole(offsetPos, 0.0);
                if (singleHole != null) {
                    foundHoles.add(singleHole);
                    continue;
                }
                if (this.doubleHoles.getValue() && (doubleHole = HoleUtils.getDoubleHole(offsetPos, 0.0)) != null) {
                    foundHoles.add(doubleHole);
                    continue;
                }
                if (this.quadHoles.getValue() && (quadHole = HoleUtils.getQuadHole(offsetPos, 0.0)) != null) {
                    foundHoles.add(quadHole);
                    continue;
                }
                HoleUtils.Hole partialHole;
                if (!this.partialHoles.getValue() || (partialHole = HoleUtils.getPartialHole(offsetPos, 0.0)) == null) continue;
                foundHoles.add(partialHole);
            }
            synchronized (this.holes) {
                this.holes.clear();
                this.holes.addAll(foundHoles);
            }
        };
        if (this.asynchronous.getValue()) {
            this.executor.submit(runnable);
        } else {
            runnable.run();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (HoleESPModule.mc.level == null) {
            return;
        }
        synchronized (this.holes) {
            EspShader.Settings shaderSettings;
            if (this.holes.isEmpty()) {
                return;
            }
            boolean useSafeShader = !this.safeShader.getValue().equalsIgnoreCase("None");
            boolean useUnsafeShader = !this.unsafeShader.getValue().equalsIgnoreCase("None");
            boolean usePartialShader = !this.partialShader.getValue().equalsIgnoreCase("None");
            ArrayList<Renderer3D.VertexCollection> safeFillQuads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> safeOutlineQuads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> unsafeFillQuads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> unsafeOutlineQuads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> partialFillQuads = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> partialOutlineQuads = new ArrayList<Renderer3D.VertexCollection>();
            boolean normalFill = this.fill.getValue().equalsIgnoreCase("Normal");
            boolean gradientFill = this.fill.getValue().equalsIgnoreCase("Gradient");
            boolean normalOutline = this.outline.getValue().equalsIgnoreCase("Normal");
            boolean gradientOutline = this.outline.getValue().equalsIgnoreCase("Gradient");
            for (HoleUtils.Hole hole : this.holes) {
                if (!Renderer3D.isFrustumVisible(hole.box())) continue;
                boolean isSafe = hole.safety() == HoleUtils.HoleSafety.SAFE;
                boolean isPartial = hole.safety() == HoleUtils.HoleSafety.PARTIAL;
                boolean hasShader = isSafe ? useSafeShader : (isPartial ? usePartialShader : useUnsafeShader);
                AABB fillBox = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY + this.fillHeight.getValue().doubleValue(), hole.box().maxZ);
                AABB outlineBox = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY + this.outlineHeight.getValue().doubleValue(), hole.box().maxZ);
                AABB wireframeBox = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY, hole.box().maxZ);
                Color fillCol = this.getFillColor(hole);
                Color outlineCol = this.getOutlineColor(hole);
                Color wireframeCol = this.getWireframeColor(hole);
                if (hasShader) {
                    ArrayList<Renderer3D.VertexCollection> fillList = isSafe ? safeFillQuads : (isPartial ? partialFillQuads : unsafeFillQuads);
                    ArrayList<Renderer3D.VertexCollection> outlineList = isSafe ? safeOutlineQuads : (isPartial ? partialOutlineQuads : unsafeOutlineQuads);
                    if (normalFill) {
                        Renderer3D.renderGradientBox(fillList, renderWorldEvent.getMatrices(), fillBox, fillCol, fillCol);
                    }
                    if (gradientFill) {
                        Renderer3D.renderGradientBox(fillList, renderWorldEvent.getMatrices(), fillBox, this.fillHeight.getValue().floatValue() < 0.0f ? fillCol : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : fillCol);
                    }
                    if (normalOutline) {
                        Renderer3D.renderGradientBoxOutline(outlineList, renderWorldEvent.getMatrices(), outlineBox, outlineCol, outlineCol);
                    }
                    if (gradientOutline) {
                        Renderer3D.renderGradientBoxOutline(outlineList, renderWorldEvent.getMatrices(), outlineBox, this.outlineHeight.getValue().floatValue() < 0.0f ? outlineCol : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : outlineCol);
                    }
                    if (!this.wireframe.getValue()) continue;
                    Renderer3D.renderBoxWireframe(outlineList, renderWorldEvent.getMatrices(), wireframeBox, wireframeCol, wireframeCol);
                    continue;
                }
                if (normalFill) {
                    Renderer3D.renderBox(renderWorldEvent.getMatrices(), fillBox, fillCol);
                }
                if (gradientFill) {
                    Renderer3D.renderGradientBox(renderWorldEvent.getMatrices(), fillBox, this.fillHeight.getValue().floatValue() < 0.0f ? fillCol : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : fillCol);
                }
                if (normalOutline) {
                    Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), outlineBox, outlineCol);
                }
                if (gradientOutline) {
                    Renderer3D.renderGradientBoxOutline(renderWorldEvent.getMatrices(), outlineBox, this.outlineHeight.getValue().floatValue() < 0.0f ? outlineCol : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : outlineCol);
                }
                if (!this.wireframe.getValue()) continue;
                Renderer3D.renderBoxWireframe(renderWorldEvent.getMatrices(), wireframeBox, wireframeCol);
            }
            if (!(!useSafeShader || safeFillQuads.isEmpty() && safeOutlineQuads.isEmpty())) {
                shaderSettings = EspShader.createSettings(this.safeShader.getValue(), this.safeShaderOpacity.getValue().floatValue(), this.safeShaderSpeed.getValue().floatValue(), this.safeShaderStep.getValue().floatValue(), this.safeShaderColor1.getColor(), this.safeShaderColor2.getColor(), this.safeShaderColor3.getColor(), this.safeShaderColor4.getColor(), this.safeShaderGlowColor.getColor(), null);
                EspShader.draw(safeFillQuads, safeOutlineQuads, shaderSettings);
            }
            if (!(!useUnsafeShader || unsafeFillQuads.isEmpty() && unsafeOutlineQuads.isEmpty())) {
                shaderSettings = EspShader.createSettings(this.unsafeShader.getValue(), this.unsafeShaderOpacity.getValue().floatValue(), this.unsafeShaderSpeed.getValue().floatValue(), this.unsafeShaderStep.getValue().floatValue(), this.unsafeShaderColor1.getColor(), this.unsafeShaderColor2.getColor(), this.unsafeShaderColor3.getColor(), this.unsafeShaderColor4.getColor(), this.unsafeShaderGlowColor.getColor(), null);
                EspShader.draw(unsafeFillQuads, unsafeOutlineQuads, shaderSettings);
            }
            if (!(!usePartialShader || partialFillQuads.isEmpty() && partialOutlineQuads.isEmpty())) {
                shaderSettings = EspShader.createSettings(this.partialShader.getValue(), this.partialShaderOpacity.getValue().floatValue(), this.partialShaderSpeed.getValue().floatValue(), this.partialShaderStep.getValue().floatValue(), this.partialShaderColor1.getColor(), this.partialShaderColor2.getColor(), this.partialShaderColor3.getColor(), this.partialShaderColor4.getColor(), this.partialShaderGlowColor.getColor(), null);
                EspShader.draw(partialFillQuads, partialOutlineQuads, shaderSettings);
            }
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.holes.size());
    }

    private Color getFillColor(HoleUtils.Hole hole) {
        Color color;
        switch (hole.safety()) {
            case SAFE: {
                color = this.safeFillColor.getColor();
                break;
            }
            case PARTIAL: {
                color = this.partialFillColor.getColor();
                break;
            }
            default: {
                color = this.unsafeFillColor.getColor();
            }
        }
        if (!this.fade.getValue()) {
            return color;
        }
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * this.getEasing(hole)));
    }

    private Color getOutlineColor(HoleUtils.Hole hole) {
        Color color;
        switch (hole.safety()) {
            case SAFE: {
                color = this.safeOutlineColor.getColor();
                break;
            }
            case PARTIAL: {
                color = this.partialOutlineColor.getColor();
                break;
            }
            default: {
                color = this.unsafeOutlineColor.getColor();
            }
        }
        if (!this.fade.getValue()) {
            return color;
        }
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * this.getEasing(hole)));
    }

    private Color getWireframeColor(HoleUtils.Hole hole) {
        Color color;
        switch (hole.safety()) {
            case SAFE: {
                color = this.safeWireframeColor.getColor();
                break;
            }
            case PARTIAL: {
                color = this.partialWireframeColor.getColor();
                break;
            }
            default: {
                color = this.unsafeWireframeColor.getColor();
            }
        }
        if (!this.fade.getValue()) {
            return color;
        }
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * this.getEasing(hole)));
    }

    private float getEasing(HoleUtils.Hole hole) {
        float distanceFactor = (float)(1.0 - Mth.clamp((double)(Math.sqrt(HoleESPModule.mc.player.distanceToSqr(hole.box().getCenter())) / this.range.getValue().doubleValue()), (double)0.0, (double)1.0));
        return Easing.ease(distanceFactor, Easing.Method.EASE_OUT_CUBIC);
    }
}

