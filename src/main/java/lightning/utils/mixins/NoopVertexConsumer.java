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


    public VertexConsumer addVertex(float x, float y, float z) {
        return this;
    }

    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this;
    }

    public VertexConsumer setColor(int color) {
        return this;
    }

    public VertexConsumer setLineWidth(float lineWidth) {
        return this;
    }

    public VertexConsumer setUv(float u, float v) {
        return this;
    }

    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        return this;
    }
}

