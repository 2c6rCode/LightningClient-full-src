/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.textures.GpuSampler
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 *  lightning.mixins.accessors.GuiGraphicsExtractorAccessor
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package lightning.utils.font;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.awt.Font;
import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lightning.Night;
import lightning.mixins.accessors.GuiGraphicsExtractorAccessor;
import lightning.modules.Module;
import lightning.modules.impl.core.FontModule;
import lightning.utils.IMinecraft;
import lightning.utils.font.Glyph;
import lightning.utils.font.GlyphMap;
import lightning.utils.graphics.GuiQuadRenderState;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.graphics.TextGlowShader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class FontRenderer
implements Closeable,
IMinecraft {
    private final ObjectList<GlyphMap> maps = new ObjectArrayList();
    private static final Map<Identifier, RenderType> WORLD_TYPES = new HashMap<Identifier, RenderType>();
    private final Font[] originalFonts;
    private Font[] fonts;
    private final float size;
    private final int charsPerPage;
    private final int padding;
    private int multiplier = 0;
    private int previousGameScale = -1;
    private boolean initialized;
    private final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    public FontRenderer(Font[] fontArray, float f) {
        this(fontArray, f, 256, 48);
    }

    public FontRenderer(Font[] fontArray, float f, int n, int n2) {
        Preconditions.checkArgument((f > 0.0f ? 1 : 0) != 0, (Object)"size <= 0");
        Preconditions.checkArgument((fontArray.length > 0 ? 1 : 0) != 0, (Object)"fonts.length <= 0");
        Preconditions.checkArgument((n > 4 ? 1 : 0) != 0, (Object)"Unreasonable charactersPerPage count");
        Preconditions.checkArgument((n2 > 0 ? 1 : 0) != 0, (Object)"paddingBetweenCharacters <= 0");
        this.originalFonts = (Font[])fontArray.clone();
        this.size = f;
        this.charsPerPage = n;
        this.padding = n2;
        this.init(this.originalFonts, f);
    }

    public void drawString(GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2, int n, boolean bl) {
        this.drawString(guiGraphicsExtractor, string, f, f2, n, bl, false, true);
    }

    public void drawString(GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2, int n, boolean bl, boolean bl2) {
        this.drawString(guiGraphicsExtractor, string, f, f2, n, bl, bl2, true);
    }

    public void drawString(GuiGraphicsExtractor guiGraphicsExtractor, String string, float f, float f2, int n, boolean bl, boolean bl2, boolean bl3) {
        this.drawText(guiGraphicsExtractor, Component.literal((String)string).getVisualOrderText(), f, f2, n, bl, bl2, bl3);
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl) {
        this.drawText(guiGraphicsExtractor, formattedCharSequence, f, f2, n, bl, false, true);
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl, boolean bl2) {
        this.drawText(guiGraphicsExtractor, formattedCharSequence, f, f2, n, bl, bl2, true);
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl, boolean bl2, boolean bl3) {
        GuiRenderState guiRenderState = ((GuiGraphicsExtractorAccessor)guiGraphicsExtractor).night$getGuiRenderState();
        this.submitGlyphs(guiRenderState, (Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)guiGraphicsExtractor.pose()), Renderer2D.peekScissor(guiGraphicsExtractor), formattedCharSequence, f, f2, n, bl, bl2, bl3);
    }

    public void submitGlyphs(GuiRenderState guiRenderState, Matrix3x2fc matrix3x2fc, ScreenRectangle screenRectangle, FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl) {
        this.submitGlyphs(guiRenderState, matrix3x2fc, screenRectangle, formattedCharSequence, f, f2, n, bl, false, true);
    }

    public void submitGlyphs(GuiRenderState guiRenderState, Matrix3x2fc matrix3x2fc, ScreenRectangle screenRectangle, FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl, boolean bl2, boolean bl3) {
        this.ensureScale();
        if (Night.MODULE_MANAGER != null) {
            FontModule fontMod = (FontModule)Night.MODULE_MANAGER.getModule(FontModule.class);
            if (fontMod != null && fontMod.isToggled()) {
                f += fontMod.xOffset.getValue().floatValue();
                f2 += fontMod.yOffset.getValue().floatValue();
            }
        }
        Map<GlyphMap, ObjectList<DrawEntry>> glyphMapListMap = this.layoutGlyphs(formattedCharSequence, n, bl, new float[1]);
        if (!glyphMapListMap.isEmpty()) {
            float f3 = 1.0f / (float)this.multiplier;
            for (Map.Entry<GlyphMap, ObjectList<DrawEntry>> entry : glyphMapListMap.entrySet()) {
                GlyphMap glyphMap = entry.getKey();
                List<DrawEntry> list = entry.getValue();
                int n2 = list.size();
                TextureSetup textureSetup = TextureSetup.singleTexture((GpuTextureView)glyphMap.getTexture().getTextureView(), (GpuSampler)glyphMap.getTexture().getSampler());
                if (bl2) {
                    float f8 = 17.0f;
                    float f9 = f8 / (float)glyphMap.getWidth();
                    float f10 = f8 / (float)glyphMap.getHeight();
                    float f11 = f8 * f3;
                    float[] xCoords = new float[n2 * 4];
                    float[] yCoords = new float[n2 * 4];
                    float[] uCoords = new float[n2 * 4];
                    float[] vCoords = new float[n2 * 4];
                    int[] colors = new int[n2 * 4];
                    int n3 = 0;
                    for (DrawEntry drawEntry : list) {
                        Glyph glyph = drawEntry.toDraw();
                        float f7 = (float)glyph.u() / (float)glyphMap.getWidth();
                        float f6 = (float)glyph.v() / (float)glyphMap.getHeight();
                        float f5 = (float)(glyph.u() + glyph.width()) / (float)glyphMap.getWidth();
                        float f4 = (float)(glyph.v() + glyph.height()) / (float)glyphMap.getHeight();
                        float f12 = f + drawEntry.atX() * f3 - f11;
                        float f13 = f2 + drawEntry.atY() * f3 - f11;
                        float f14 = f + (drawEntry.atX() + (float)glyph.width()) * f3 + f11;
                        float f15 = f2 + (drawEntry.atY() + (float)glyph.height()) * f3 + f11;
                        int n4 = n3 * 4;
                        xCoords[n4] = f12;
                        yCoords[n4] = f13;
                        uCoords[n4] = f7 - f9;
                        vCoords[n4] = f6 - f10;
                        xCoords[n4 + 1] = f12;
                        yCoords[n4 + 1] = f15;
                        uCoords[n4 + 1] = f7 - f9;
                        vCoords[n4 + 1] = f4 + f10;
                        xCoords[n4 + 2] = f14;
                        yCoords[n4 + 2] = f15;
                        uCoords[n4 + 2] = f5 + f9;
                        vCoords[n4 + 2] = f4 + f10;
                        xCoords[n4 + 3] = f14;
                        yCoords[n4 + 3] = f13;
                        uCoords[n4 + 3] = f5 + f9;
                        vCoords[n4 + 3] = f6 - f10;
                        int n5 = drawEntry.argb();
                        colors[n4 + 3] = n5;
                        colors[n4 + 2] = n5;
                        colors[n4 + 1] = n5;
                        colors[n4] = n5;
                        ++n3;
                    }
                    guiRenderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState(TextGlowShader.TEXT_GLOW_PIPELINE, textureSetup, (Matrix3x2fc)new Matrix3x2f(matrix3x2fc), xCoords, yCoords, colors, uCoords, vCoords, screenRectangle));
                }
                if (!bl3) continue;
                float[] fArray = new float[n2 * 4];
                float[] fArray2 = new float[n2 * 4];
                float[] fArray3 = new float[n2 * 4];
                float[] fArray4 = new float[n2 * 4];
                int[] objectArray = new int[n2 * 4];
                int n6 = 0;
                for (DrawEntry drawEntry : list) {
                    Glyph glyph = drawEntry.toDraw();
                    float f16 = (float)glyph.u() / (float)glyphMap.getWidth();
                    float f17 = (float)glyph.v() / (float)glyphMap.getHeight();
                    float f18 = (float)(glyph.u() + glyph.width()) / (float)glyphMap.getWidth();
                    float f19 = (float)(glyph.v() + glyph.height()) / (float)glyphMap.getHeight();
                    float f7 = f + drawEntry.atX() * f3;
                    float f6 = f2 + drawEntry.atY() * f3;
                    float f5 = f + (drawEntry.atX() + (float)glyph.width()) * f3;
                    float f4 = f2 + (drawEntry.atY() + (float)glyph.height()) * f3;
                    int n7 = n6 * 4;
                    fArray[n7] = f7;
                    fArray2[n7] = f6;
                    fArray3[n7] = f16;
                    fArray4[n7] = f17;
                    fArray[n7 + 1] = f7;
                    fArray2[n7 + 1] = f4;
                    fArray3[n7 + 1] = f16;
                    fArray4[n7 + 1] = f19;
                    fArray[n7 + 2] = f5;
                    fArray2[n7 + 2] = f4;
                    fArray3[n7 + 2] = f18;
                    fArray4[n7 + 2] = f19;
                    fArray[n7 + 3] = f5;
                    fArray2[n7 + 3] = f6;
                    fArray3[n7 + 3] = f18;
                    fArray4[n7 + 3] = f17;
                    int n8 = drawEntry.argb();
                    objectArray[n7 + 3] = n8;
                    objectArray[n7 + 2] = n8;
                    objectArray[n7 + 1] = n8;
                    objectArray[n7] = n8;
                    ++n6;
                }
                guiRenderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState(textureSetup, (Matrix3x2fc)new Matrix3x2f(matrix3x2fc), fArray, fArray2, objectArray, fArray3, fArray4, screenRectangle));
            }
        }
    }

    public void drawString(PoseStack poseStack, String string, float f, float f2, int n, boolean bl) {
        this.drawString(poseStack, string, f, f2, n, bl, false, true);
    }

    public void drawString(PoseStack poseStack, String string, float f, float f2, int n, boolean bl, boolean bl2) {
        this.drawString(poseStack, string, f, f2, n, bl, bl2, true);
    }

    public void drawString(PoseStack poseStack, String string, float f, float f2, int n, boolean bl, boolean bl2, boolean bl3) {
        this.drawText(poseStack, Component.literal((String)string).getVisualOrderText(), f, f2, n, bl, bl2, bl3);
    }

    public void drawText(PoseStack poseStack, FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl) {
        this.drawText(poseStack, formattedCharSequence, f, f2, n, bl, false, true);
    }

    public void drawText(PoseStack poseStack, FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl, boolean bl2) {
        this.drawText(poseStack, formattedCharSequence, f, f2, n, bl, bl2, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(PoseStack poseStack, FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl, boolean bl2, boolean bl3) {
        Map<GlyphMap, ObjectList<DrawEntry>> map;
        FontModule fontModule;
        this.ensureScale();
        if (Night.MODULE_MANAGER != null && (fontModule = Night.MODULE_MANAGER.getModule(FontModule.class)) != null && fontModule.isToggled()) {
            f += fontModule.xOffset.getValue().floatValue();
            f2 += fontModule.yOffset.getValue().floatValue();
        }
        if ((map = this.layoutGlyphs(formattedCharSequence, n, bl, new float[1])).isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(f, f2, 0.0f);
        poseStack.scale(1.0f / (float)this.multiplier, 1.0f / (float)this.multiplier, 1.0f);
        Matrix4f matrix4f = poseStack.last().pose();
        for (Map.Entry<GlyphMap, ObjectList<DrawEntry>> entry : map.entrySet()) {
            float f3;
            Object object;
            GlyphMap glyphMap = entry.getKey();
            if (bl2) {
                try {
                    float f4 = 17.0f;
                    float f5 = f4 / (float)glyphMap.getWidth();
                    float f6 = f4 / (float)glyphMap.getHeight();
                    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(entry.getValue().size() * 4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
                        BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                        for (DrawEntry drawEntry : entry.getValue()) {
                            Glyph glyph = drawEntry.toDraw();
                            f3 = (float)glyph.u() / (float)glyphMap.getWidth();
                            float f7 = (float)glyph.v() / (float)glyphMap.getHeight();
                            float f8 = (float)(glyph.u() + glyph.width()) / (float)glyphMap.getWidth();
                            float f9 = (float)(glyph.v() + glyph.height()) / (float)glyphMap.getHeight();
                            bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() - f4, drawEntry.atY() + (float)glyph.height() + f4, 0.0f).setUv(f3 - f5, f9 + f6).setColor(drawEntry.argb());
                            bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() + (float)glyph.width() + f4, drawEntry.atY() + (float)glyph.height() + f4, 0.0f).setUv(f8 + f5, f9 + f6).setColor(drawEntry.argb());
                            bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() + (float)glyph.width() + f4, drawEntry.atY() - f4, 0.0f).setUv(f8 + f5, f7 - f6).setColor(drawEntry.argb());
                            bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() - f4, drawEntry.atY() - f4, 0.0f).setUv(f3 - f5, f7 - f6).setColor(drawEntry.argb());
                        }
                        MeshData meshData = bufferBuilder.build();
                        if (meshData != null) {
                            Renderer3D.draw(TextGlowShader.getWorldGlowType(glyphMap.getTextureId()), meshData);
                        }
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            if (!bl3) continue;
            try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(entry.getValue().size() * 4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
                BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                for (DrawEntry drawEntry : entry.getValue()) {
                    Glyph glyph = drawEntry.toDraw();
                    float f10 = (float)glyph.u() / (float)glyphMap.getWidth();
                    float f11 = (float)glyph.v() / (float)glyphMap.getHeight();
                    float f12 = (float)(glyph.u() + glyph.width()) / (float)glyphMap.getWidth();
                    f3 = (float)(glyph.v() + glyph.height()) / (float)glyphMap.getHeight();
                    bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX(), drawEntry.atY() + (float)glyph.height(), 0.0f).setUv(f10, f3).setColor(drawEntry.argb());
                    bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() + (float)glyph.width(), drawEntry.atY() + (float)glyph.height(), 0.0f).setUv(f12, f3).setColor(drawEntry.argb());
                    bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() + (float)glyph.width(), drawEntry.atY(), 0.0f).setUv(f12, f11).setColor(drawEntry.argb());
                    bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX(), drawEntry.atY(), 0.0f).setUv(f10, f11).setColor(drawEntry.argb());
                }
                MeshData meshData = bufferBuilder.build();
                if (meshData != null) {
                    Renderer3D.draw(FontRenderer.worldTexturedType(glyphMap.getTextureId()), meshData);
                }
            }
        }
        poseStack.popPose();
    }

    private Map<GlyphMap, ObjectList<DrawEntry>> layoutGlyphs(FormattedCharSequence formattedCharSequence, int n, boolean bl, float[] fArray) {
        int baseColor = n;
        if ((baseColor & 0xFC000000) == 0) {
            baseColor |= 0xFF000000;
        }
        if (bl) {
            baseColor = (baseColor & 0xFCFCFC) >> 2 | baseColor & 0xFF000000;
        }
        final int effectiveBaseColor = baseColor;
        int n2 = effectiveBaseColor >> 24 & 0xFF;
        int n3 = effectiveBaseColor >> 16 & 0xFF;
        int n4 = effectiveBaseColor >> 8 & 0xFF;
        int n7 = effectiveBaseColor & 0xFF;
        LinkedHashMap<GlyphMap, ObjectList<DrawEntry>> linkedHashMap = new LinkedHashMap<GlyphMap, ObjectList<DrawEntry>>();
        float[] fArray2 = new float[]{0.0f};
        formattedCharSequence.accept((n5, style, n6) -> {
            int charColor = effectiveBaseColor;
            char[] cArray = Character.toChars(n6);
            int n8 = n3;
            int n9 = n4;
            int n10 = n7;
            if (style.getColor() != null) {
                charColor = style.getColor().getValue();
                if ((charColor & 0xFC000000) == 0) {
                    charColor |= 0xFF000000;
                }
                if (bl) {
                    charColor = (charColor & 0xFCFCFC) >> 2 | charColor & 0xFF000000;
                }
                n8 = charColor >> 16 & 0xFF;
                n9 = charColor >> 8 & 0xFF;
                n10 = charColor & 0xFF;
            }
            int entryColor = n2 << 24 | n8 << 16 | n9 << 8 | n10;
            for (char c : cArray) {
                Glyph glyph = this.locateGlyph(c);
                if (glyph == null) continue;
                if (glyph.value() != ' ') {
                    linkedHashMap.computeIfAbsent(glyph.parent(), glyphMap -> new ObjectArrayList()).add((Object)new DrawEntry(fArray2[0], 0.0f, entryColor, glyph));
                }
                fArray2[0] = fArray2[0] + (float)glyph.width();
            }
            return true;
        });
        fArray[0] = fArray2[0];
        return linkedHashMap;
    }

    private static RenderType worldTexturedType(Identifier identifier2) {
        return WORLD_TYPES.computeIfAbsent(identifier2, identifier -> RenderType.create((String)("night_font_" + String.valueOf(identifier)), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", identifier).createRenderSetup()));
    }

    private void ensureScale() {
        if (mc.getWindow().getGuiScale() != this.previousGameScale) {
            this.close();
            this.init(this.originalFonts, this.size);
        }
    }

    public float getTextWidth(String string) {
        char[] cArray = FontRenderer.stripControlCodes(string).toCharArray();
        float f = 0.0f;
        for (char c : cArray) {
            Glyph glyph = this.locateGlyph(c);
            if (glyph == null) continue;
            f += (float)glyph.width() / (float)this.multiplier;
        }
        return Math.max(f, 0.0f);
    }

    public float getTextWidth(FormattedCharSequence formattedCharSequence) {
        float[] fArray = new float[2];
        formattedCharSequence.accept((n, style, n2) -> {
            char c = (char)n2;
            Glyph glyph = this.locateGlyph(c);
            if (glyph != null) {
                fArray[0] = fArray[0] + (float)glyph.width() / (float)this.multiplier;
            }
            return true;
        });
        return Math.max(fArray[0], fArray[1]);
    }

    public float getHeight() {
        Glyph glyph = this.locateGlyph('A');
        if (glyph != null) {
            return (float)glyph.height() / (float)this.multiplier;
        }
        return 0.0f;
    }

    public static String stripControlCodes(String string) {
        char[] cArray = string.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < cArray.length; ++i) {
            char c = cArray[i];
            if (c == '\u00a7') {
                ++i;
                continue;
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void init(Font[] fontArray, float f) {
        if (this.initialized) {
            throw new IllegalStateException("Double call to init()");
        }
        this.LOCK.writeLock().lock();
        try {
            int n = mc != null && mc.getWindow() != null ? mc.getWindow().getGuiScale() : 2;
            this.previousGameScale = n <= 0 ? 2 : n;
            this.multiplier = this.previousGameScale * 2;
            this.fonts = new Font[fontArray.length];
            for (int i = 0; i < fontArray.length; ++i) {
                this.fonts[i] = fontArray[i].deriveFont(f * (float)this.multiplier);
            }
            this.initialized = true;
        }
        finally {
            this.LOCK.writeLock().unlock();
        }
        this.locateGlyph('A');
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Glyph locateGlyph(char c) {
        GlyphMap glyphMap2;
        this.LOCK.readLock().lock();
        try {
            for (GlyphMap map : this.maps) {
                if (!map.contains(c)) continue;
                Glyph glyph = map.getGlyph(c);
                return glyph;
            }
        }
        finally {
            this.LOCK.readLock().unlock();
        }
        int n = this.charsPerPage * (int)Math.floor((double)c / (double)this.charsPerPage);
        glyphMap2 = new GlyphMap(this.fonts, (char)n, (char)(n + this.charsPerPage), this.padding);
        this.LOCK.writeLock().lock();
        try {
            glyphMap2.generate();
            this.maps.add(glyphMap2);
        }
        finally {
            this.LOCK.writeLock().unlock();
        }
        return glyphMap2.getGlyph(c);
    }

    @Override
    public void close() {
        this.LOCK.writeLock().lock();
        try {
            for (GlyphMap glyphMap : this.maps) {
                glyphMap.destroy();
            }
            this.maps.clear();
            this.initialized = false;
        }
        finally {
            this.LOCK.writeLock().unlock();
        }
    }

    public record DrawEntry(float atX, float atY, int argb, Glyph toDraw) {
    }
}

