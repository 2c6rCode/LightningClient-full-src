/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 */
package lightning.utils.mixins;

import com.mojang.blaze3d.vertex.VertexConsumer;

public enum NoopVertexConsumer implements VertexConsumer
{
    INSTANCE;


    public VertexConsumer addVertex(float f, float f2, float f3) {
        return this;
    }

    public VertexConsumer setColor(int n, int n2, int n3, int n4) {
        return this;
    }

    public VertexConsumer setColor(int n) {
        return this;
    }

    public VertexConsumer setLineWidth(float f) {
        return this;
    }

    public VertexConsumer setUv(float f, float f2) {
        return this;
    }

    public VertexConsumer setUv1(int n, int n2) {
        return this;
    }

    public VertexConsumer setUv2(int n, int n2) {
        return this;
    }

    public VertexConsumer setNormal(float f, float f2, float f3) {
        return this;
    }
}

