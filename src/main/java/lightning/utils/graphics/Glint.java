/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.BlendFactor
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  lightning.mixins.accessors.RenderPipelinesAccessor
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.resources.Identifier
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.util.ArrayList;
import java.util.List;
import lightning.mixins.accessors.RenderPipelinesAccessor;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class Glint {
    private static final Identifier GLINT_TEXTURE = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/glint.png");
    private static final RenderType NO_DEPTH_GLINT_QUADS = RenderType.create((String)"night_no_depth_glint", (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{RenderPipelinesAccessor.getGuiTexturedSnippet()}).withLocation("lightning/no_depth_glint").withCull(false).withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE))).build()).withTexture("Sampler0", GLINT_TEXTURE).sortOnUpload().createRenderSetup());
    public static List<TexturedVertexCollection> QUADS = new ArrayList<TexturedVertexCollection>();
    public static final GlintParams ENTITY = new GlintParams();
    public static final GlintParams CRYSTAL = new GlintParams();
    private static long lastMs = 0L;

    public static void tick() {
        long l = System.currentTimeMillis();
        if (lastMs == 0L) {
            lastMs = l;
        }
        float f = (float)(l - lastMs) / 1000.0f;
        lastMs = l;
        ENTITY.tick(f);
        CRYSTAL.tick(f);
    }

    public static void draw() {
        if (QUADS.isEmpty()) {
            return;
        }
        int n = 0;
        for (TexturedVertexCollection texturedVertexCollection : QUADS) {
            n += texturedVertexCollection.vertices().length;
        }
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized((int)(n * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (TexturedVertexCollection texturedVertexCollection2 : QUADS) {
                texturedVertexCollection2.vertex(bufferBuilder);
            }
            MeshData meshData = bufferBuilder.build();
            Renderer3D.draw(NO_DEPTH_GLINT_QUADS, meshData);
        }
        QUADS.clear();
    }

    public static class GlintParams {
        public float speedU = 0.25f;
        public float speedV = 0.1f;
        public float scale = 1.0f;
        public String direction = "Diagonal";
        public float scrollU = 0.0f;
        public float scrollV = 0.0f;

        public void tick(float f) {
            this.scrollU = (this.scrollU + this.speedU * f) % 1.0f;
            this.scrollV = (this.scrollV + this.speedV * f) % 1.0f;
            if (this.scrollU < 0.0f) {
                this.scrollU += 1.0f;
            }
            if (this.scrollV < 0.0f) {
                this.scrollV += 1.0f;
            }
        }

        public float[] getGlintUV(float f, float f2, float f3) {
            float f4 = this.scale * 0.06f;
            float f5, f6;
            switch (this.direction) {
                case "Horizontal": {
                    f5 = (f + f3 * 0.5f) * f4;
                    f6 = f2 * f4;
                    break;
                }
                case "Vertical": {
                    f5 = f2 * f4;
                    f6 = (f + f3 * 0.5f) * f4;
                    break;
                }
                default: {
                    f5 = f * f4 + f3 * f4 * 0.5f;
                    f6 = f2 * f4 + f3 * f4 * 0.5f;
                }
            }
            return new float[]{f5 += this.scrollU, f6 += this.scrollV};
        }
    }

    public record TexturedVertexCollection(TexturedVertex[] vertices) {
        public void vertex(BufferBuilder bufferBuilder) {
            for (TexturedVertex texturedVertex : this.vertices) {
                bufferBuilder.addVertex((Matrix4fc)texturedVertex.matrix, texturedVertex.x, texturedVertex.y, texturedVertex.z).setUv(texturedVertex.u, texturedVertex.v).setColor(texturedVertex.color);
            }
        }
    }

    public record TexturedVertex(Matrix4f matrix, float x, float y, float z, float u, float v, int color) {
    }
}

