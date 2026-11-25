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

    public FontRenderer(Font[] fonts, float size) {
        this(fonts, size, 256, 48);
    }

    public FontRenderer(Font[] fonts, float size, int charsPerPage, int padding) {
        Preconditions.checkArgument((size > 0.0f ? 1 : 0) != 0, (Object)"size <= 0");
        Preconditions.checkArgument((fonts.length > 0 ? 1 : 0) != 0, (Object)"fonts.length <= 0");
        Preconditions.checkArgument((charsPerPage > 4 ? 1 : 0) != 0, (Object)"Unreasonable charactersPerPage count");
        Preconditions.checkArgument((padding > 0 ? 1 : 0) != 0, (Object)"paddingBetweenCharacters <= 0");
        this.originalFonts = (Font[])fonts.clone();
        this.size = size;
        this.charsPerPage = charsPerPage;
        this.padding = padding;
        this.init(this.originalFonts, size);
    }

    public void drawString(GuiGraphicsExtractor guiGraphicsExtractor, String text, float x, float y, int color, boolean dropShadow) {
        this.drawString(guiGraphicsExtractor, text, x, y, color, dropShadow, false, true);
    }

    public void drawString(GuiGraphicsExtractor guiGraphicsExtractor, String text, float x, float y, int color, boolean dropShadow, boolean glow) {
        this.drawString(guiGraphicsExtractor, text, x, y, color, dropShadow, glow, true);
    }

    public void drawString(GuiGraphicsExtractor guiGraphicsExtractor, String text, float x, float y, int color, boolean dropShadow, boolean glow, boolean textLayer) {
        this.drawText(guiGraphicsExtractor, Component.literal((String)text).getVisualOrderText(), x, y, color, dropShadow, glow, textLayer);
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        this.drawText(guiGraphicsExtractor, text, x, y, color, dropShadow, false, true);
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow) {
        this.drawText(guiGraphicsExtractor, text, x, y, color, dropShadow, glow, true);
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow, boolean textLayer) {
        GuiRenderState guiRenderState = ((GuiGraphicsExtractorAccessor)guiGraphicsExtractor).night$getGuiRenderState();
        this.submitGlyphs(guiRenderState, (Matrix3x2fc)new Matrix3x2f((Matrix3x2fc)guiGraphicsExtractor.pose()), Renderer2D.peekScissor(guiGraphicsExtractor), text, x, y, color, dropShadow, glow, textLayer);
    }

    public void submitGlyphs(GuiRenderState guiRenderState, Matrix3x2fc matrix3x2fc, ScreenRectangle screenRectangle, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        this.submitGlyphs(guiRenderState, matrix3x2fc, screenRectangle, text, x, y, color, dropShadow, false, true);
    }

    public void submitGlyphs(GuiRenderState guiRenderState, Matrix3x2fc matrix3x2fc, ScreenRectangle screenRectangle, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow, boolean textLayer) {
        this.ensureScale();
        if (Night.MODULE_MANAGER != null) {
            FontModule fontMod = (FontModule)Night.MODULE_MANAGER.getModule(FontModule.class);
            if (fontMod != null && fontMod.isToggled()) {
                x += fontMod.xOffset.getValue().floatValue();
                y += fontMod.yOffset.getValue().floatValue();
            }
        }
        Map<GlyphMap, ObjectList<DrawEntry>> glyphMapListMap = this.layoutGlyphs(text, color, dropShadow, new float[1]);
        if (!glyphMapListMap.isEmpty()) {
            float invScale = 1.0f / (float)this.multiplier;
            for (Map.Entry<GlyphMap, ObjectList<DrawEntry>> entry : glyphMapListMap.entrySet()) {
                GlyphMap glyphMap = entry.getKey();
                List<DrawEntry> list = entry.getValue();
                int glyphCount = list.size();
                TextureSetup textureSetup = TextureSetup.singleTexture((GpuTextureView)glyphMap.getTexture().getTextureView(), (GpuSampler)glyphMap.getTexture().getSampler());
                if (glow) {
                    float glowPad = 17.0f;
                    float glowPadU = glowPad / (float)glyphMap.getWidth();
                    float glowPadV = glowPad / (float)glyphMap.getHeight();
                    float glowPadWorld = glowPad * invScale;
                    float[] xCoords = new float[glyphCount * 4];
                    float[] yCoords = new float[glyphCount * 4];
                    float[] uCoords = new float[glyphCount * 4];
                    float[] vCoords = new float[glyphCount * 4];
                    int[] colors = new int[glyphCount * 4];
                    int quadIndex = 0;
                    for (DrawEntry drawEntry : list) {
                        Glyph glyph = drawEntry.toDraw();
                        float u1 = (float)glyph.u() / (float)glyphMap.getWidth();
                        float v1 = (float)glyph.v() / (float)glyphMap.getHeight();
                        float u2 = (float)(glyph.u() + glyph.width()) / (float)glyphMap.getWidth();
                        float v2 = (float)(glyph.v() + glyph.height()) / (float)glyphMap.getHeight();
                        float x1 = x + drawEntry.atX() * invScale - glowPadWorld;
                        float y1 = y + drawEntry.atY() * invScale - glowPadWorld;
                        float x2 = x + (drawEntry.atX() + (float)glyph.width()) * invScale + glowPadWorld;
                        float y2 = y + (drawEntry.atY() + (float)glyph.height()) * invScale + glowPadWorld;
                        int baseIndex = quadIndex * 4;
                        xCoords[baseIndex] = x1;
                        yCoords[baseIndex] = y1;
                        uCoords[baseIndex] = u1 - glowPadU;
                        vCoords[baseIndex] = v1 - glowPadV;
                        xCoords[baseIndex + 1] = x1;
                        yCoords[baseIndex + 1] = y2;
                        uCoords[baseIndex + 1] = u1 - glowPadU;
                        vCoords[baseIndex + 1] = v2 + glowPadV;
                        xCoords[baseIndex + 2] = x2;
                        yCoords[baseIndex + 2] = y2;
                        uCoords[baseIndex + 2] = u2 + glowPadU;
                        vCoords[baseIndex + 2] = v2 + glowPadV;
                        xCoords[baseIndex + 3] = x2;
                        yCoords[baseIndex + 3] = y1;
                        uCoords[baseIndex + 3] = u2 + glowPadU;
                        vCoords[baseIndex + 3] = v1 - glowPadV;
                        int argb = drawEntry.argb();
                        colors[baseIndex + 3] = argb;
                        colors[baseIndex + 2] = argb;
                        colors[baseIndex + 1] = argb;
                        colors[baseIndex] = argb;
                        ++quadIndex;
                    }
                    guiRenderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState(TextGlowShader.TEXT_GLOW_PIPELINE, textureSetup, (Matrix3x2fc)new Matrix3x2f(matrix3x2fc), xCoords, yCoords, colors, uCoords, vCoords, screenRectangle));
                }
                if (!textLayer) continue;
                float[] textX = new float[glyphCount * 4];
                float[] textY = new float[glyphCount * 4];
                float[] textU = new float[glyphCount * 4];
                float[] textV = new float[glyphCount * 4];
                int[] textColors = new int[glyphCount * 4];
                int quadIndex = 0;
                for (DrawEntry drawEntry : list) {
                    Glyph glyph = drawEntry.toDraw();
                    float u1 = (float)glyph.u() / (float)glyphMap.getWidth();
                    float v1 = (float)glyph.v() / (float)glyphMap.getHeight();
                    float u2 = (float)(glyph.u() + glyph.width()) / (float)glyphMap.getWidth();
                    float v2 = (float)(glyph.v() + glyph.height()) / (float)glyphMap.getHeight();
                    float x1 = x + drawEntry.atX() * invScale;
                    float y1 = y + drawEntry.atY() * invScale;
                    float x2 = x + (drawEntry.atX() + (float)glyph.width()) * invScale;
                    float y2 = y + (drawEntry.atY() + (float)glyph.height()) * invScale;
                    int baseIndex = quadIndex * 4;
                    textX[baseIndex] = x1;
                    textY[baseIndex] = y1;
                    textU[baseIndex] = u1;
                    textV[baseIndex] = v1;
                    textX[baseIndex + 1] = x1;
                    textY[baseIndex + 1] = y2;
                    textU[baseIndex + 1] = u1;
                    textV[baseIndex + 1] = v2;
                    textX[baseIndex + 2] = x2;
                    textY[baseIndex + 2] = y2;
                    textU[baseIndex + 2] = u2;
                    textV[baseIndex + 2] = v2;
                    textX[baseIndex + 3] = x2;
                    textY[baseIndex + 3] = y1;
                    textU[baseIndex + 3] = u2;
                    textV[baseIndex + 3] = v1;
                    int argb = drawEntry.argb();
                    textColors[baseIndex + 3] = argb;
                    textColors[baseIndex + 2] = argb;
                    textColors[baseIndex + 1] = argb;
                    textColors[baseIndex] = argb;
                    ++quadIndex;
                }
                guiRenderState.addGuiElement((GuiElementRenderState)new GuiQuadRenderState(textureSetup, (Matrix3x2fc)new Matrix3x2f(matrix3x2fc), textX, textY, textColors, textU, textV, screenRectangle));
            }
        }
    }

    public void drawString(PoseStack poseStack, String text, float x, float y, int color, boolean dropShadow) {
        this.drawString(poseStack, text, x, y, color, dropShadow, false, true);
    }

    public void drawString(PoseStack poseStack, String text, float x, float y, int color, boolean dropShadow, boolean glow) {
        this.drawString(poseStack, text, x, y, color, dropShadow, glow, true);
    }

    public void drawString(PoseStack poseStack, String text, float x, float y, int color, boolean dropShadow, boolean glow, boolean textLayer) {
        this.drawText(poseStack, Component.literal((String)text).getVisualOrderText(), x, y, color, dropShadow, glow, textLayer);
    }

    public void drawText(PoseStack poseStack, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        this.drawText(poseStack, text, x, y, color, dropShadow, false, true);
    }

    public void drawText(PoseStack poseStack, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow) {
        this.drawText(poseStack, text, x, y, color, dropShadow, glow, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(PoseStack poseStack, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, boolean glow, boolean textLayer) {
        Map<GlyphMap, ObjectList<DrawEntry>> map;
        FontModule fontModule;
        this.ensureScale();
        if (Night.MODULE_MANAGER != null && (fontModule = Night.MODULE_MANAGER.getModule(FontModule.class)) != null && fontModule.isToggled()) {
            x += fontModule.xOffset.getValue().floatValue();
            y += fontModule.yOffset.getValue().floatValue();
        }
        if ((map = this.layoutGlyphs(text, color, dropShadow, new float[1])).isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0f);
        poseStack.scale(1.0f / (float)this.multiplier, 1.0f / (float)this.multiplier, 1.0f);
        Matrix4f matrix4f = poseStack.last().pose();
        for (Map.Entry<GlyphMap, ObjectList<DrawEntry>> entry : map.entrySet()) {
            GlyphMap glyphMap = entry.getKey();
            if (glow) {
                try {
                    float glowPad = 17.0f;
                    float glowPadU = glowPad / (float)glyphMap.getWidth();
                    float glowPadV = glowPad / (float)glyphMap.getHeight();
                    try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(entry.getValue().size() * 4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
                        BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                        for (DrawEntry drawEntry : entry.getValue()) {
                            Glyph glyph = drawEntry.toDraw();
                            float u1 = (float)glyph.u() / (float)glyphMap.getWidth();
                            float v1 = (float)glyph.v() / (float)glyphMap.getHeight();
                            float u2 = (float)(glyph.u() + glyph.width()) / (float)glyphMap.getWidth();
                            float v2 = (float)(glyph.v() + glyph.height()) / (float)glyphMap.getHeight();
                            bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() - glowPad, drawEntry.atY() + (float)glyph.height() + glowPad, 0.0f).setUv(u1 - glowPadU, v2 + glowPadV).setColor(drawEntry.argb());
                            bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() + (float)glyph.width() + glowPad, drawEntry.atY() + (float)glyph.height() + glowPad, 0.0f).setUv(u2 + glowPadU, v2 + glowPadV).setColor(drawEntry.argb());
                            bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() + (float)glyph.width() + glowPad, drawEntry.atY() - glowPad, 0.0f).setUv(u2 + glowPadU, v1 - glowPadV).setColor(drawEntry.argb());
                            bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() - glowPad, drawEntry.atY() - glowPad, 0.0f).setUv(u1 - glowPadU, v1 - glowPadV).setColor(drawEntry.argb());
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
            if (!textLayer) continue;
            try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(entry.getValue().size() * 4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
                BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                for (DrawEntry drawEntry : entry.getValue()) {
                    Glyph glyph = drawEntry.toDraw();
                    float u1 = (float)glyph.u() / (float)glyphMap.getWidth();
                    float v1 = (float)glyph.v() / (float)glyphMap.getHeight();
                    float u2 = (float)(glyph.u() + glyph.width()) / (float)glyphMap.getWidth();
                    float v2 = (float)(glyph.v() + glyph.height()) / (float)glyphMap.getHeight();
                    bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX(), drawEntry.atY() + (float)glyph.height(), 0.0f).setUv(u1, v2).setColor(drawEntry.argb());
                    bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() + (float)glyph.width(), drawEntry.atY() + (float)glyph.height(), 0.0f).setUv(u2, v2).setColor(drawEntry.argb());
                    bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX() + (float)glyph.width(), drawEntry.atY(), 0.0f).setUv(u2, v1).setColor(drawEntry.argb());
                    bufferBuilder.addVertex((Matrix4fc)matrix4f, drawEntry.atX(), drawEntry.atY(), 0.0f).setUv(u1, v1).setColor(drawEntry.argb());
                }
                MeshData meshData = bufferBuilder.build();
                if (meshData != null) {
                    Renderer3D.draw(FontRenderer.worldTexturedType(glyphMap.getTextureId()), meshData);
                }
            }
        }
        poseStack.popPose();
    }

    private Map<GlyphMap, ObjectList<DrawEntry>> layoutGlyphs(FormattedCharSequence text, int color, boolean dropShadow, float[] widthOut) {
        int baseColor = color;
        if ((baseColor & 0xFC000000) == 0) {
            baseColor |= 0xFF000000;
        }
        if (dropShadow) {
            baseColor = (baseColor & 0xFCFCFC) >> 2 | baseColor & 0xFF000000;
        }
        final int effectiveBaseColor = baseColor;
        int alpha = effectiveBaseColor >> 24 & 0xFF;
        int baseRed = effectiveBaseColor >> 16 & 0xFF;
        int baseGreen = effectiveBaseColor >> 8 & 0xFF;
        int baseBlue = effectiveBaseColor & 0xFF;
        LinkedHashMap<GlyphMap, ObjectList<DrawEntry>> linkedHashMap = new LinkedHashMap<GlyphMap, ObjectList<DrawEntry>>();
        float[] cursorX = new float[]{0.0f};
        text.accept((index, style, codePoint) -> {
            int charColor = effectiveBaseColor;
            char[] cArray = Character.toChars(codePoint);
            int red = baseRed;
            int green = baseGreen;
            int blue = baseBlue;
            if (style.getColor() != null) {
                charColor = style.getColor().getValue();
                if ((charColor & 0xFC000000) == 0) {
                    charColor |= 0xFF000000;
                }
                if (dropShadow) {
                    charColor = (charColor & 0xFCFCFC) >> 2 | charColor & 0xFF000000;
                }
                red = charColor >> 16 & 0xFF;
                green = charColor >> 8 & 0xFF;
                blue = charColor & 0xFF;
            }
            int entryColor = alpha << 24 | red << 16 | green << 8 | blue;
            for (char ch : cArray) {
                Glyph glyph = this.locateGlyph(ch);
                if (glyph == null) continue;
                if (glyph.value() != ' ') {
                    linkedHashMap.computeIfAbsent(glyph.parent(), glyphMap -> new ObjectArrayList()).add((Object)new DrawEntry(cursorX[0], 0.0f, entryColor, glyph));
                }
                cursorX[0] = cursorX[0] + (float)glyph.width();
            }
            return true;
        });
        widthOut[0] = cursorX[0];
        return linkedHashMap;
    }

    private static RenderType worldTexturedType(Identifier textureId) {
        return WORLD_TYPES.computeIfAbsent(textureId, identifier -> RenderType.create((String)("night_font_" + String.valueOf(identifier)), (RenderSetup)RenderSetup.builder((RenderPipeline)RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", identifier).createRenderSetup()));
    }

    private void ensureScale() {
        if (mc.getWindow().getGuiScale() != this.previousGameScale) {
            this.close();
            this.init(this.originalFonts, this.size);
        }
    }

    public float getTextWidth(String text) {
        char[] cArray = FontRenderer.stripControlCodes(text).toCharArray();
        float width = 0.0f;
        for (char ch : cArray) {
            Glyph glyph = this.locateGlyph(ch);
            if (glyph == null) continue;
            width += (float)glyph.width() / (float)this.multiplier;
        }
        return Math.max(width, 0.0f);
    }

    public float getTextWidth(FormattedCharSequence text) {
        float[] widths = new float[2];
        text.accept((index, style, codePoint) -> {
            char ch = (char)codePoint;
            Glyph glyph = this.locateGlyph(ch);
            if (glyph != null) {
                widths[0] = widths[0] + (float)glyph.width() / (float)this.multiplier;
            }
            return true;
        });
        return Math.max(widths[0], widths[1]);
    }

    public float getHeight() {
        Glyph glyph = this.locateGlyph('A');
        if (glyph != null) {
            return (float)glyph.height() / (float)this.multiplier;
        }
        return 0.0f;
    }

    public static String stripControlCodes(String text) {
        char[] cArray = text.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < cArray.length; ++i) {
            char ch = cArray[i];
            if (ch == '\u00a7') {
                ++i;
                continue;
            }
            stringBuilder.append(ch);
        }
        return stringBuilder.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void init(Font[] fonts, float size) {
        if (this.initialized) {
            throw new IllegalStateException("Double call to init()");
        }
        this.LOCK.writeLock().lock();
        try {
            int guiScale = mc != null && mc.getWindow() != null ? mc.getWindow().getGuiScale() : 2;
            this.previousGameScale = guiScale <= 0 ? 2 : guiScale;
            this.multiplier = this.previousGameScale * 2;
            this.fonts = new Font[fonts.length];
            for (int i = 0; i < fonts.length; ++i) {
                this.fonts[i] = fonts[i].deriveFont(size * (float)this.multiplier);
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
    private Glyph locateGlyph(char ch) {
        GlyphMap glyphMap;
        this.LOCK.readLock().lock();
        try {
            for (GlyphMap map : this.maps) {
                if (!map.contains(ch)) continue;
                Glyph glyph = map.getGlyph(ch);
                return glyph;
            }
        }
        finally {
            this.LOCK.readLock().unlock();
        }
        int pageStart = this.charsPerPage * (int)Math.floor((double)ch / (double)this.charsPerPage);
        glyphMap = new GlyphMap(this.fonts, (char)pageStart, (char)(pageStart + this.charsPerPage), this.padding);
        this.LOCK.writeLock().lock();
        try {
            glyphMap.generate();
            this.maps.add(glyphMap);
        }
        finally {
            this.LOCK.writeLock().unlock();
        }
        return glyphMap.getGlyph(ch);
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

