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
        BlockPos blockPos = HoleESPModule.mc.player.blockPosition();
        int n = Night.WORLD_MANAGER.getRadius(this.range.getValue().doubleValue());
        Runnable runnable = () -> {
            ArrayList<HoleUtils.Hole> arrayList = new ArrayList<>();
            for (int i = 0; i < n; ++i) {
                HoleUtils.Hole hole;
                HoleUtils.Hole hole2;
                BlockPos blockPos2 = blockPos.offset(Night.WORLD_MANAGER.getOffset(i));
                HoleUtils.Hole hole3 = HoleUtils.getSingleHole(blockPos2, 0.0);
                if (hole3 != null) {
                    arrayList.add(hole3);
                    continue;
                }
                if (this.doubleHoles.getValue() && (hole2 = HoleUtils.getDoubleHole(blockPos2, 0.0)) != null) {
                    arrayList.add(hole2);
                    continue;
                }
                if (this.quadHoles.getValue() && (hole = HoleUtils.getQuadHole(blockPos2, 0.0)) != null) {
                    arrayList.add(hole);
                    continue;
                }
                HoleUtils.Hole partialHole;
                if (!this.partialHoles.getValue() || (partialHole = HoleUtils.getPartialHole(blockPos2, 0.0)) == null) continue;
                arrayList.add(partialHole);
            }
            synchronized (this.holes) {
                this.holes.clear();
                this.holes.addAll(arrayList);
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
        List<HoleUtils.Hole> list;
        if (HoleESPModule.mc.level == null) {
            return;
        }
        List<HoleUtils.Hole> list2 = list = this.holes;
        synchronized (list2) {
            Object object;
            if (this.holes.isEmpty()) {
                return;
            }
            boolean bl = !this.safeShader.getValue().equalsIgnoreCase("None");
            boolean bl2 = !this.unsafeShader.getValue().equalsIgnoreCase("None");
            boolean bl3 = !this.partialShader.getValue().equalsIgnoreCase("None");
            ArrayList<Renderer3D.VertexCollection> arrayList = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> arrayList2 = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> arrayList3 = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> arrayList4 = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> arrayList5 = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> arrayList6 = new ArrayList<Renderer3D.VertexCollection>();
            boolean bl4 = this.fill.getValue().equalsIgnoreCase("Normal");
            boolean bl5 = this.fill.getValue().equalsIgnoreCase("Gradient");
            boolean bl6 = this.outline.getValue().equalsIgnoreCase("Normal");
            boolean bl7 = this.outline.getValue().equalsIgnoreCase("Gradient");
            for (HoleUtils.Hole hole : this.holes) {
                if (!Renderer3D.isFrustumVisible(hole.box())) continue;
                boolean bl8 = hole.safety() == HoleUtils.HoleSafety.SAFE;
                boolean bl9 = hole.safety() == HoleUtils.HoleSafety.PARTIAL;
                boolean bl10 = bl9;
                boolean bl11 = bl8 ? bl : (bl9 ? bl3 : bl2);
                AABB aABB = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY + this.fillHeight.getValue().doubleValue(), hole.box().maxZ);
                AABB aABB2 = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY + this.outlineHeight.getValue().doubleValue(), hole.box().maxZ);
                AABB aABB3 = new AABB(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY, hole.box().maxZ);
                Color color = this.getFillColor(hole);
                Color color2 = this.getOutlineColor(hole);
                Color color3 = this.getWireframeColor(hole);
                if (bl11) {
                    ArrayList<Renderer3D.VertexCollection> fillList = bl8 ? arrayList : (bl9 ? arrayList5 : arrayList3);
                    ArrayList<Renderer3D.VertexCollection> outlineList = bl8 ? arrayList2 : (bl9 ? arrayList6 : arrayList4);
                    if (bl4) {
                        Renderer3D.renderGradientBox(fillList, renderWorldEvent.getMatrices(), aABB, color, color);
                    }
                    if (bl5) {
                        Renderer3D.renderGradientBox(fillList, renderWorldEvent.getMatrices(), aABB, this.fillHeight.getValue().floatValue() < 0.0f ? color : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : color);
                    }
                    if (bl6) {
                        Renderer3D.renderGradientBoxOutline(outlineList, renderWorldEvent.getMatrices(), aABB2, color2, color2);
                    }
                    if (bl7) {
                        Renderer3D.renderGradientBoxOutline(outlineList, renderWorldEvent.getMatrices(), aABB2, this.outlineHeight.getValue().floatValue() < 0.0f ? color2 : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : color2);
                    }
                    if (!this.wireframe.getValue()) continue;
                    Renderer3D.renderBoxWireframe(outlineList, renderWorldEvent.getMatrices(), aABB3, color3, color3);
                    continue;
                }
                if (bl4) {
                    Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, color);
                }
                if (bl5) {
                    Renderer3D.renderGradientBox(renderWorldEvent.getMatrices(), aABB, this.fillHeight.getValue().floatValue() < 0.0f ? color : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : color);
                }
                if (bl6) {
                    Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB2, color2);
                }
                if (bl7) {
                    Renderer3D.renderGradientBoxOutline(renderWorldEvent.getMatrices(), aABB2, this.outlineHeight.getValue().floatValue() < 0.0f ? color2 : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : color2);
                }
                if (!this.wireframe.getValue()) continue;
                Renderer3D.renderBoxWireframe(renderWorldEvent.getMatrices(), aABB3, color3);
            }
            if (!(!bl || arrayList.isEmpty() && arrayList2.isEmpty())) {
                object = EspShader.createSettings(this.safeShader.getValue(), this.safeShaderOpacity.getValue().floatValue(), this.safeShaderSpeed.getValue().floatValue(), this.safeShaderStep.getValue().floatValue(), this.safeShaderColor1.getColor(), this.safeShaderColor2.getColor(), this.safeShaderColor3.getColor(), this.safeShaderColor4.getColor(), this.safeShaderGlowColor.getColor(), null);
                EspShader.draw(arrayList, arrayList2, (EspShader.Settings)object);
            }
            if (!(!bl2 || arrayList3.isEmpty() && arrayList4.isEmpty())) {
                object = EspShader.createSettings(this.unsafeShader.getValue(), this.unsafeShaderOpacity.getValue().floatValue(), this.unsafeShaderSpeed.getValue().floatValue(), this.unsafeShaderStep.getValue().floatValue(), this.unsafeShaderColor1.getColor(), this.unsafeShaderColor2.getColor(), this.unsafeShaderColor3.getColor(), this.unsafeShaderColor4.getColor(), this.unsafeShaderGlowColor.getColor(), null);
                EspShader.draw(arrayList3, arrayList4, (EspShader.Settings)object);
            }
            if (!(!bl3 || arrayList5.isEmpty() && arrayList6.isEmpty())) {
                object = EspShader.createSettings(this.partialShader.getValue(), this.partialShaderOpacity.getValue().floatValue(), this.partialShaderSpeed.getValue().floatValue(), this.partialShaderStep.getValue().floatValue(), this.partialShaderColor1.getColor(), this.partialShaderColor2.getColor(), this.partialShaderColor3.getColor(), this.partialShaderColor4.getColor(), this.partialShaderGlowColor.getColor(), null);
                EspShader.draw(arrayList5, arrayList6, (EspShader.Settings)object);
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
                Color color2 = color = this.unsafeFillColor.getColor();
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
                Color color2 = color = this.unsafeOutlineColor.getColor();
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
                Color color2 = color = this.unsafeWireframeColor.getColor();
            }
        }
        if (!this.fade.getValue()) {
            return color;
        }
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * this.getEasing(hole)));
    }

    private float getEasing(HoleUtils.Hole hole) {
        float f = (float)(1.0 - Mth.clamp((double)(Math.sqrt(HoleESPModule.mc.player.distanceToSqr(hole.box().getCenter())) / this.range.getValue().doubleValue()), (double)0.0, (double)1.0));
        return Easing.ease(f, Easing.Method.EASE_OUT_CUBIC);
    }
}

