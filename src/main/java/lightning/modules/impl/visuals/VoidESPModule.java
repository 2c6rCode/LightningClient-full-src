/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

@RegisterModule(name="VoidESP", description="Highlights any non-bedrock blocks that can drop you into the void.", category=Module.Category.VISUALS)
public class VoidESPModule
extends Module {
    public NumberSetting range = new NumberSetting("Range", "The maximum range at which void blocks will be rendered.", 10, 1, 50);
    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
    public CategorySetting fillCategory = new CategorySetting("Fill", "The category for settings related to fill rendering.");
    public ModeSetting fill = new ModeSetting("Fill", "Mode", "The mode for the fill rendering on the void blocks.", new CategorySetting.Visibility(this.fillCategory), "Normal", new String[]{"None", "Normal", "Gradient"});
    public NumberSetting fillHeight = new NumberSetting("FillHeight", "Height", "The height of the fill rendering on the void blocks.", new ModeSetting.Visibility(this.fill, "Normal", "Gradient"), 1.0, 0.0, 2.0);
    public ColorSetting fillColor = new ColorSetting("FillColor", "Color", "The color for the fill rendering on the void blocks.", new ModeSetting.Visibility(this.fill, "Normal", "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
    public CategorySetting outlineCategory = new CategorySetting("Outline", "The category for settings related to outline rendering.");
    public ModeSetting outline = new ModeSetting("Outline", "Mode", "The mode for the outline rendering on the void blocks.", new CategorySetting.Visibility(this.outlineCategory), "Normal", new String[]{"None", "Normal", "Gradient"});
    public NumberSetting outlineHeight = new NumberSetting("OutlineHeight", "Height", "The height of the outline rendering on the void blocks.", new ModeSetting.Visibility(this.outline, "Normal", "Gradient"), 1.0, 0.0, 2.0);
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "Color", "The color for the outline rendering on the void blocks.", new ModeSetting.Visibility(this.outline, "Normal", "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
    public CategorySetting shaderCategory = new CategorySetting("Shader", "The category for settings related to shader rendering.");
    public ModeSetting shader = new ModeSetting("Shader", "Mode", "The animated shader that will be rendered on the void blocks.", new CategorySetting.Visibility(this.shaderCategory), "None", EspShader.MODES);
    public NumberSetting shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "Void block shader opacity.", new ModeSetting.Visibility(this.shader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
    public NumberSetting shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "Void block shader animation speed.", new ModeSetting.Visibility(this.shader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
    public NumberSetting shaderStep = new NumberSetting("ShaderStep", "Step", "Void block gradient step size.", new ModeSetting.Visibility(this.shader, "Gradient"), 50.0, 0.1, 200.0);
    public ColorSetting shaderColor1 = new ColorSetting("ShaderColor1", "The first gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    public ColorSetting shaderColor2 = new ColorSetting("ShaderColor2", "The second gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(180, 0, 255, 255), false, false));
    public ColorSetting shaderColor3 = new ColorSetting("ShaderColor3", "The third gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(255, 0, 100, 255), false, false));
    public ColorSetting shaderColor4 = new ColorSetting("ShaderColor4", "The fourth gradient color.", new ModeSetting.Visibility(this.shader, "Gradient"), new ColorSetting.Color(new Color(100, 0, 255, 255), false, false));
    public ColorSetting shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The glow tint color for void blocks.", new ModeSetting.Visibility(this.shader, "Glowing"), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<BlockPos> positions = Collections.synchronizedList(new ArrayList());

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (VoidESPModule.mc.player == null || VoidESPModule.mc.level == null) {
            return;
        }
        Runnable runnable = () -> {
            BlockPos blockPos;
            BlockPos blockPos2;
            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
            int n = (int)(VoidESPModule.mc.player.getX() - (double)this.range.getValue().intValue());
            while ((double)n < VoidESPModule.mc.player.getX() + (double)this.range.getValue().intValue()) {
                int n2 = (int)(VoidESPModule.mc.player.getZ() - (double)this.range.getValue().intValue());
                while ((double)n2 < VoidESPModule.mc.player.getZ() + (double)this.range.getValue().intValue()) {
                    blockPos2 = BlockPos.containing((double)n, (double)VoidESPModule.mc.level.getMinY(), (double)n2);
                    if (VoidESPModule.mc.level.getBlockState(blockPos2).getBlock() != Blocks.BEDROCK && VoidESPModule.mc.level.getWorldBorder().isWithinBounds(blockPos2)) {
                        arrayList.add(blockPos2);
                    }
                    ++n2;
                }
                ++n;
            }
            synchronized (this.positions) {
                this.positions.clear();
                this.positions.addAll(arrayList);
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
        List<BlockPos> list;
        if (VoidESPModule.mc.level == null) {
            return;
        }
        List<BlockPos> list2 = list = this.positions;
        synchronized (list2) {
            if (this.positions.isEmpty()) {
                return;
            }
            boolean bl = !this.shader.getValue().equalsIgnoreCase("None");
            ArrayList<Renderer3D.VertexCollection> arrayList = new ArrayList<Renderer3D.VertexCollection>();
            ArrayList<Renderer3D.VertexCollection> arrayList2 = new ArrayList<Renderer3D.VertexCollection>();
            boolean bl2 = this.fill.getValue().equalsIgnoreCase("Normal");
            boolean bl3 = this.fill.getValue().equalsIgnoreCase("Gradient");
            boolean bl4 = this.outline.getValue().equalsIgnoreCase("Normal");
            boolean bl5 = this.outline.getValue().equalsIgnoreCase("Gradient");
            for (BlockPos blockPos : this.positions) {
                AABB aABB = new AABB(blockPos);
                AABB aABB2 = new AABB(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.minY + this.fillHeight.getValue().doubleValue(), aABB.maxZ);
                AABB aABB3 = new AABB(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.minY + this.outlineHeight.getValue().doubleValue(), aABB.maxZ);
                if (bl) {
                    if (bl2) {
                        Renderer3D.renderGradientBox(arrayList, renderWorldEvent.getMatrices(), aABB2, this.fillColor.getColor(), this.fillColor.getColor());
                    }
                    if (bl3) {
                        Renderer3D.renderGradientBox(arrayList, renderWorldEvent.getMatrices(), aABB2, this.fillHeight.getValue().floatValue() < 0.0f ? this.fillColor.getColor() : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : this.fillColor.getColor());
                    }
                    if (bl4) {
                        Renderer3D.renderGradientBoxOutline(arrayList2, renderWorldEvent.getMatrices(), aABB3, this.outlineColor.getColor(), this.outlineColor.getColor());
                    }
                    if (!bl5) continue;
                    Renderer3D.renderGradientBoxOutline(arrayList2, renderWorldEvent.getMatrices(), aABB3, this.outlineHeight.getValue().floatValue() < 0.0f ? this.outlineColor.getColor() : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : this.outlineColor.getColor());
                    continue;
                }
                if (bl2) {
                    Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB2, this.fillColor.getColor());
                }
                if (bl3) {
                    Renderer3D.renderGradientBox(renderWorldEvent.getMatrices(), aABB2, this.fillHeight.getValue().floatValue() < 0.0f ? this.fillColor.getColor() : new Color(0, 0, 0, 0), this.fillHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : this.fillColor.getColor());
                }
                if (bl4) {
                    Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB3, this.outlineColor.getColor());
                }
                if (!bl5) continue;
                Renderer3D.renderGradientBoxOutline(renderWorldEvent.getMatrices(), aABB3, this.outlineHeight.getValue().floatValue() < 0.0f ? this.outlineColor.getColor() : new Color(0, 0, 0, 0), this.outlineHeight.getValue().floatValue() < 0.0f ? new Color(0, 0, 0, 0) : this.outlineColor.getColor());
            }
            if (!(!bl || arrayList.isEmpty() && arrayList2.isEmpty())) {
                EspShader.Settings settings = EspShader.createSettings(this.shader.getValue(), this.shaderOpacity.getValue().floatValue(), this.shaderSpeed.getValue().floatValue(), this.shaderStep.getValue().floatValue(), this.shaderColor1.getColor(), this.shaderColor2.getColor(), this.shaderColor3.getColor(), this.shaderColor4.getColor(), this.shaderGlowColor.getColor(), null);
                EspShader.draw(arrayList, arrayList2, settings);
            }
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }
}

