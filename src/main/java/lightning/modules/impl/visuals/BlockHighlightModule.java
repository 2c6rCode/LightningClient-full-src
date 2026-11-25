/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package lightning.modules.impl.visuals;

import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.system.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@RegisterModule(name="BlockHighlight", description="Replaces the default Minecraft block highlight with a more customizable one.", category=Module.Category.VISUALS)
public class BlockHighlightModule
extends Module {
    public ModeSetting animationMode = new ModeSetting("Animation", "The animation that will be applied to the rendering.", "Static", new String[]{"Static", "Slide"});
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the target block.", "Outline", new String[]{"None", "Fill", "Outline", "Both", "Complex"});
    public NumberSetting slideSmoothness = new NumberSetting("Smoothness", "The smoothness for the slide while target block is changing.", 1, 0, 20);
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private BlockPos prevPosition = null;
    private Vec3 renderPosition = null;
    private long animationStart = 0L;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        BlockState blockState;
        if (BlockHighlightModule.mc.player == null || BlockHighlightModule.mc.level == null) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("None")) {
            return;
        }
        HitResult hitResult = BlockHighlightModule.mc.hitResult;
        if (!(hitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult)hitResult;
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (this.animationMode.getValue().equals("Slide") && blockPos != null) {
            if (this.renderPosition == null) {
                this.renderPosition = MathUtils.getVec(blockPos);
            }
            if (!WorldUtils.equals(blockPos, this.prevPosition)) {
                this.animationStart = System.currentTimeMillis();
                this.prevPosition = blockPos;
            }
        }
        Vec3 targetRenderPos = MathUtils.getVec(blockPos);
        if (this.animationMode.getValue().equalsIgnoreCase("Slide") && this.renderPosition != null) {
            float easeProgress = Easing.ease(Easing.toDelta(this.animationStart, (int)(Math.pow(this.slideSmoothness.getValue().doubleValue(), 1.4) * 1000.0)), Easing.Method.EASE_OUT_QUART);
            targetRenderPos = this.renderPosition = this.renderPosition.add(MathUtils.scale(MathUtils.getVec(blockPos).subtract(this.renderPosition), easeProgress));
        }
        if ((blockState = BlockHighlightModule.mc.level.getBlockState(blockPos)).isAir() || !BlockHighlightModule.mc.level.getWorldBorder().isWithinBounds(blockPos)) {
            return;
        }
        VoxelShape voxelShape = blockState.getShape((BlockGetter)BlockHighlightModule.mc.level, blockPos);
        if (voxelShape.isEmpty()) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Complex")) {
            for (AABB aABB : voxelShape.toAabbs()) {
                Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB.move(targetRenderPos), this.fillColor.getColor());
                Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB.move(targetRenderPos), this.outlineColor.getColor());
            }
        } else {
            if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(renderWorldEvent.getMatrices(), voxelShape.bounds().move(targetRenderPos), this.fillColor.getColor());
            }
            if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), voxelShape.bounds().move(targetRenderPos), this.outlineColor.getColor());
            }
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue();
    }
}

