/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  lombok.Generated
 *  org.joml.Matrix4f
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import lightning.utils.graphics.Glint;
import lightning.utils.graphics.Renderer3D;
import lombok.Generated;
import org.joml.Matrix4f;

public class ChamsVertexConsumer
implements VertexConsumer {
    private static final Matrix4f IDENTITY = new Matrix4f();
    private final VertexConsumer real;
    private final boolean fill;
    private final int fillColor;
    private final boolean outline;
    private final int outlineColor;
    private final boolean shine;
    private final boolean glint;
    private final int glintColor;
    private final Glint.GlintParams glintParams;
    private final boolean suppressReal;
    private final float realAlphaMultiplier;
    private final float[] xs = new float[4];
    private final float[] ys = new float[4];
    private final float[] zs = new float[4];
    private int i = 0;

    public ChamsVertexConsumer(VertexConsumer vertexConsumer, boolean bl, int n, boolean bl2, int n2, boolean bl3) {
        this(vertexConsumer, bl, n, bl2, n2, bl3, false, -1, null, false, 1.0f);
    }

    public ChamsVertexConsumer(VertexConsumer vertexConsumer, boolean bl, int n, boolean bl2, int n2, boolean bl3, boolean bl4) {
        this(vertexConsumer, bl, n, bl2, n2, bl3, false, -1, null, bl4, 1.0f);
    }

    public ChamsVertexConsumer(VertexConsumer vertexConsumer, boolean bl, int n, boolean bl2, int n2, boolean bl3, boolean bl4, float f) {
        this(vertexConsumer, bl, n, bl2, n2, bl3, false, -1, null, bl4, f);
    }

    public ChamsVertexConsumer(VertexConsumer vertexConsumer, boolean bl, int n, boolean bl2, int n2, boolean bl3, boolean bl4, int n3, boolean bl5, float f) {
        this(vertexConsumer, bl, n, bl2, n2, bl3, bl4, n3, null, bl5, f);
    }

    public ChamsVertexConsumer(VertexConsumer vertexConsumer, boolean bl, int n, boolean bl2, int n2, boolean bl3, boolean bl4, int n3, Glint.GlintParams glintParams, boolean bl5, float f) {
        this.real = vertexConsumer;
        this.fill = bl;
        this.fillColor = n;
        this.outline = bl2;
        this.outlineColor = n2;
        this.shine = bl3;
        this.glint = bl4;
        this.glintColor = n3;
        this.glintParams = glintParams;
        this.suppressReal = bl5;
        this.realAlphaMultiplier = f;
    }

    public VertexConsumer addVertex(float f, float f2, float f3) {
        if (!this.suppressReal && this.real != null) {
            this.real.addVertex(f, f2, f3);
        }
        this.xs[this.i] = f;
        this.ys[this.i] = f2;
        this.zs[this.i] = f3;
        ++this.i;
        if (this.i == 4) {
            this.flushQuad();
            this.i = 0;
        }
        return this;
    }

    private void flushQuad() {
        List<Renderer3D.VertexCollection> list;
        List<Renderer3D.VertexCollection> list2 = this.shine ? Renderer3D.SHINE_QUADS : Renderer3D.QUADS;
        List<Renderer3D.VertexCollection> list3 = list = this.shine ? Renderer3D.SHINE_DEBUG_LINES : Renderer3D.DEBUG_LINES;
        if (this.fill) {
            list2.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.fillColor), new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.fillColor), new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.fillColor), new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.fillColor)));
        }
        if (this.outline) {
            list.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.outlineColor), new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.outlineColor)));
            list.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], this.outlineColor), new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.outlineColor)));
            list.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], this.outlineColor), new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.outlineColor)));
            list.add(new Renderer3D.VertexCollection(new Renderer3D.Vertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], this.outlineColor), new Renderer3D.Vertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], this.outlineColor)));
        }
        if (this.glint) {
            Glint.GlintParams glintParams = this.glintParams != null ? this.glintParams : Glint.ENTITY;
            float[] fArray = new float[4];
            float[] fArray2 = new float[4];
            for (int i = 0; i < 4; ++i) {
                float[] fArray3 = glintParams.getGlintUV(this.xs[i], this.ys[i], this.zs[i]);
                fArray[i] = fArray3[0];
                fArray2[i] = fArray3[1];
            }
            Glint.QUADS.add(new Glint.TexturedVertexCollection(new Glint.TexturedVertex[]{
                new Glint.TexturedVertex(IDENTITY, this.xs[0], this.ys[0], this.zs[0], fArray[0], fArray2[0], this.glintColor),
                new Glint.TexturedVertex(IDENTITY, this.xs[1], this.ys[1], this.zs[1], fArray[1], fArray2[1], this.glintColor),
                new Glint.TexturedVertex(IDENTITY, this.xs[2], this.ys[2], this.zs[2], fArray[2], fArray2[2], this.glintColor),
                new Glint.TexturedVertex(IDENTITY, this.xs[3], this.ys[3], this.zs[3], fArray[3], fArray2[3], this.glintColor)
            }));
        }
    }

    public VertexConsumer setColor(int n, int n2, int n3, int n4) {
        if (!this.suppressReal && this.real != null) {
            int n5 = this.realAlphaMultiplier >= 1.0f ? n4 : Math.round((float)n4 * this.realAlphaMultiplier);
            this.real.setColor(n, n2, n3, n5);
        }
        return this;
    }

    public VertexConsumer setColor(int n) {
        if (!this.suppressReal && this.real != null) {
            if (this.realAlphaMultiplier >= 1.0f) {
                this.real.setColor(n);
            } else {
                int n2 = n >>> 24 & 0xFF;
                int n3 = Math.round((float)n2 * this.realAlphaMultiplier);
                this.real.setColor(n & 0xFFFFFF | n3 << 24);
            }
        }
        return this;
    }

    public VertexConsumer setUv(float f, float f2) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv(f, f2);
        }
        return this;
    }

    public VertexConsumer setUv1(int n, int n2) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv1(n, n2);
        }
        return this;
    }

    public VertexConsumer setUv2(int n, int n2) {
        if (!this.suppressReal && this.real != null) {
            this.real.setUv2(n, n2);
        }
        return this;
    }

    public VertexConsumer setNormal(float f, float f2, float f3) {
        if (!this.suppressReal && this.real != null) {
            this.real.setNormal(f, f2, f3);
        }
        return this;
    }

    public VertexConsumer setLineWidth(float f) {
        if (!this.suppressReal && this.real != null) {
            this.real.setLineWidth(f);
        }
        return this;
    }

    @Generated
    public VertexConsumer getReal() {
        return this.real;
    }
}

