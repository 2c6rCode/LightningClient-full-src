/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  org.joml.Matrix3x2fc
 *  org.jspecify.annotations.Nullable
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public record GuiQuadRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose, float[] xs, float[] ys, int[] cols, float @Nullable [] us, float @Nullable [] vs, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState
{
    public GuiQuadRenderState(Matrix3x2fc matrix3x2fc, float[] fArray, float[] fArray2, int[] nArray, @Nullable ScreenRectangle screenRectangle) {
        this(RenderPipelines.GUI, TextureSetup.noTexture(), matrix3x2fc, fArray, fArray2, nArray, null, null, screenRectangle, GuiQuadRenderState.computeBounds(fArray, fArray2, matrix3x2fc, screenRectangle));
    }

    public GuiQuadRenderState(TextureSetup textureSetup, Matrix3x2fc matrix3x2fc, float[] fArray, float[] fArray2, int[] nArray, float[] fArray3, float[] fArray4, @Nullable ScreenRectangle screenRectangle) {
        this(RenderPipelines.GUI_TEXTURED, textureSetup, matrix3x2fc, fArray, fArray2, nArray, fArray3, fArray4, screenRectangle, GuiQuadRenderState.computeBounds(fArray, fArray2, matrix3x2fc, screenRectangle));
    }

    public GuiQuadRenderState(RenderPipeline renderPipeline, TextureSetup textureSetup, Matrix3x2fc matrix3x2fc, float[] fArray, float[] fArray2, int[] nArray, float @Nullable [] fArray3, float @Nullable [] fArray4, @Nullable ScreenRectangle screenRectangle) {
        this(renderPipeline, textureSetup, matrix3x2fc, fArray, fArray2, nArray, fArray3, fArray4, screenRectangle, GuiQuadRenderState.computeBounds(fArray, fArray2, matrix3x2fc, screenRectangle));
    }

    public void buildVertices(VertexConsumer vertexConsumer) {
        if (this.us != null && this.vs != null) {
            for (int i = 0; i < this.xs.length; ++i) {
                vertexConsumer.addVertexWith2DPose(this.pose, this.xs[i], this.ys[i]).setUv(this.us[i], this.vs[i]).setColor(this.cols[i]);
            }
        } else {
            for (int i = 0; i < this.xs.length; ++i) {
                vertexConsumer.addVertexWith2DPose(this.pose, this.xs[i], this.ys[i]).setColor(this.cols[i]);
            }
        }
    }

    private static @Nullable ScreenRectangle computeBounds(float[] fArray, float[] fArray2, Matrix3x2fc matrix3x2fc, @Nullable ScreenRectangle screenRectangle) {
        int n;
        if (fArray.length == 0) {
            return screenRectangle;
        }
        float f = fArray[0];
        float f2 = fArray[0];
        float f3 = fArray2[0];
        float f4 = fArray2[0];
        for (n = 1; n < fArray.length; ++n) {
            if (fArray[n] < f) {
                f = fArray[n];
            }
            if (fArray[n] > f2) {
                f2 = fArray[n];
            }
            if (fArray2[n] < f3) {
                f3 = fArray2[n];
            }
            if (!(fArray2[n] > f4)) continue;
            f4 = fArray2[n];
        }
        n = (int)Math.floor(f);
        int n2 = (int)Math.floor(f3);
        int n3 = (int)Math.ceil(f2);
        int n4 = (int)Math.ceil(f4);
        ScreenRectangle screenRectangle2 = new ScreenRectangle(n, n2, n3 - n, n4 - n2).transformMaxBounds(matrix3x2fc);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }
}

