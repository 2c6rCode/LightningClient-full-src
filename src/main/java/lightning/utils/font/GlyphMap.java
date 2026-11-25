/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.platform.NativeImage$Format
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.AddressMode
 *  com.mojang.blaze3d.textures.FilterMode
 *  it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap
 *  lightning.mixins.accessors.AbstractTextureAccessor
 *  lightning.mixins.accessors.NativeImageAccessor
 *  lombok.Generated
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 *  org.lwjgl.system.MemoryUtil
 */
package lightning.utils.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import lightning.mixins.accessors.AbstractTextureAccessor;
import lightning.mixins.accessors.NativeImageAccessor;
import lightning.utils.font.Glyph;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

public class GlyphMap {
    private static final AtomicInteger PAGE_SEQ = new AtomicInteger();
    private DynamicTexture texture;
    private Identifier textureId;
    private final Font[] fonts;
    private int width;
    private int height;
    private final char include;
    private final char exclude;
    private final int padding;
    private final Char2ObjectArrayMap<Glyph> glyphs = new Char2ObjectArrayMap();
    boolean generated = false;

    public GlyphMap(Font[] fonts, char include, char exclude, int padding) {
        this.fonts = fonts;
        this.include = include;
        this.exclude = exclude;
        this.padding = padding;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void generate() {
        synchronized (this) {
            this.privateGenerate();
        }
    }

    private void privateGenerate() {
        if (!this.generated) {
            DynamicTexture dynamicTexture;
            int charRange = this.exclude - this.include - 1;
            int maxRowChars = (int)(Math.ceil(Math.sqrt(charRange)) * 1.5);
            int offset = 0;
            int rowCount = 0;
            int maxWidth = 0;
            int maxHeight = 0;
            int currentX = 0;
            int currentY = 0;
            int rowMaxHeight = 0;
            ArrayList<Glyph> arrayList = new ArrayList<Glyph>();
            AffineTransform affineTransform = new AffineTransform();
            FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, true, true);
            while (offset <= charRange) {
                char ch = (char)(this.include + offset);
                Font font = this.getFontForGlyph(ch);
                RectangularShape bounds = font.getStringBounds(String.valueOf(ch), fontRenderContext);
                int glyphWidth = (int)Math.ceil(bounds.getWidth());
                int glyphHeight = (int)Math.ceil(bounds.getHeight());
                ++offset;
                maxWidth = Math.max(maxWidth, currentX + glyphWidth);
                maxHeight = Math.max(maxHeight, currentY + glyphHeight);
                if (rowCount >= maxRowChars) {
                    currentX = 0;
                    currentY += rowMaxHeight + this.padding;
                    rowCount = 0;
                    rowMaxHeight = 0;
                }
                rowMaxHeight = Math.max(rowMaxHeight, glyphHeight);
                arrayList.add(new Glyph(this, currentX, currentY, glyphWidth, glyphHeight, ch));
                currentX += glyphWidth + this.padding;
                ++rowCount;
            }
            BufferedImage bufferedImage = new BufferedImage(Math.max(maxWidth + this.padding, 1), Math.max(maxHeight + this.padding, 1), 2);
            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();
            Graphics2D graphics = bufferedImage.createGraphics();
            graphics.setColor(new Color(255, 255, 255, 0));
            graphics.fillRect(0, 0, this.width, this.height);
            graphics.setColor(Color.WHITE);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            for (Glyph glyph : arrayList) {
                graphics.setFont(this.getFontForGlyph(glyph.value()));
                FontMetrics fontMetrics = graphics.getFontMetrics();
                graphics.drawString(String.valueOf(glyph.value()), glyph.u(), glyph.v() + fontMetrics.getAscent());
                this.glyphs.put(glyph.value(), glyph);
            }
            NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
            IntBuffer intBuffer = MemoryUtil.memIntBuffer((long)((NativeImageAccessor)(Object)nativeImage).getPointer(), (int)(nativeImage.getWidth() * nativeImage.getHeight()));
            WritableRaster writableRaster = bufferedImage.getRaster();
            ColorModel colorModel = bufferedImage.getColorModel();
            int numBands = writableRaster.getNumBands();
            int dataType = writableRaster.getDataBuffer().getDataType();
            Object pixelData = switch (dataType) {
                case 0 -> new byte[numBands];
                case 1 -> new short[numBands];
                default -> throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
                case 3 -> new int[numBands];
                case 4 -> new float[numBands];
                case 5 -> new double[numBands];
            };
            for (int row = 0; row < bufferedImage.getHeight(); ++row) {
                for (int i = 0; i < bufferedImage.getWidth(); ++i) {
                    writableRaster.getDataElements(i, row, pixelData);
                    intBuffer.put(colorModel.getAlpha(pixelData) << 24 | colorModel.getBlue(pixelData) << 16 | colorModel.getGreen(pixelData) << 8 | colorModel.getRed(pixelData));
                }
            }
            int pageId = PAGE_SEQ.getAndIncrement();
            this.texture = dynamicTexture = new DynamicTexture(() -> "lightning/glyph_page_" + pageId, nativeImage);
            this.textureId = Identifier.fromNamespaceAndPath((String)"lightning", (String)("glyph_page_" + pageId));
            Minecraft.getInstance().getTextureManager().register(this.textureId, (AbstractTexture)dynamicTexture);
            ((AbstractTextureAccessor)dynamicTexture).setSampler(RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.empty()));
            this.generated = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Glyph getGlyph(char ch) {
        synchronized (this) {
            if (!this.generated) {
                this.privateGenerate();
            }
            return (Glyph)this.glyphs.get(ch);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void destroy() {
        synchronized (this) {
            this.generated = false;
            if (this.textureId != null) {
                Minecraft.getInstance().getTextureManager().release(this.textureId);
                this.textureId = null;
            }
            if (this.texture != null) {
                this.texture.close();
            }
            this.glyphs.clear();
            this.width = -1;
            this.height = -1;
        }
    }

    public boolean contains(char ch) {
        return ch >= this.include && ch < this.exclude;
    }

    private Font getFontForGlyph(char ch) {
        for (Font font : this.fonts) {
            if (!font.canDisplay(ch)) continue;
            return font;
        }
        return this.fonts[0];
    }

    @Generated
    public DynamicTexture getTexture() {
        return this.texture;
    }

    @Generated
    public Identifier getTextureId() {
        return this.textureId;
    }

    @Generated
    public Font[] getFonts() {
        return this.fonts;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public char getInclude() {
        return this.include;
    }

    @Generated
    public char getExclude() {
        return this.exclude;
    }

    @Generated
    public int getPadding() {
        return this.padding;
    }

    @Generated
    public Char2ObjectArrayMap<Glyph> getGlyphs() {
        return this.glyphs;
    }

    @Generated
    public boolean isGenerated() {
        return this.generated;
    }
}

