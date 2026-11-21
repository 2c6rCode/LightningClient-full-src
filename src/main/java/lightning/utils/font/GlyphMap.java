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

    public GlyphMap(Font[] fontArray, char c, char c2, int n) {
        this.fonts = fontArray;
        this.include = c;
        this.exclude = c2;
        this.padding = n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void generate() {
        GlyphMap glyphMap;
        GlyphMap glyphMap2 = glyphMap = this;
        synchronized (glyphMap2) {
            this.privateGenerate();
        }
    }

    private void privateGenerate() {
        if (!this.generated) {
            DynamicTexture dynamicTexture;
            int n;
            Object object;
            Object object2;
            int n2 = this.exclude - this.include - 1;
            int n3 = (int)(Math.ceil(Math.sqrt(n2)) * 1.5);
            int n4 = 0;
            int n5 = 0;
            int n6 = 0;
            int n7 = 0;
            int n8 = 0;
            int n9 = 0;
            int n10 = 0;
            ArrayList<Glyph> arrayList = new ArrayList<Glyph>();
            AffineTransform affineTransform = new AffineTransform();
            FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, true, true);
            while (n4 <= n2) {
                char c = (char)(this.include + n4);
                object2 = this.getFontForGlyph(c);
                object = ((Font)object2).getStringBounds(String.valueOf(c), fontRenderContext);
                int n11 = (int)Math.ceil(((RectangularShape)object).getWidth());
                int n12 = (int)Math.ceil(((RectangularShape)object).getHeight());
                ++n4;
                n6 = Math.max(n6, n8 + n11);
                n7 = Math.max(n7, n9 + n12);
                if (n5 >= n3) {
                    n8 = 0;
                    n9 += n10 + this.padding;
                    n5 = 0;
                    n10 = 0;
                }
                n10 = Math.max(n10, n12);
                arrayList.add(new Glyph(this, n8, n9, n11, n12, c));
                n8 += n11 + this.padding;
                ++n5;
            }
            BufferedImage bufferedImage = new BufferedImage(Math.max(n6 + this.padding, 1), Math.max(n7 + this.padding, 1), 2);
            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();
            object2 = bufferedImage.createGraphics();
            ((Graphics)object2).setColor(new Color(255, 255, 255, 0));
            ((Graphics)object2).fillRect(0, 0, this.width, this.height);
            ((Graphics)object2).setColor(Color.WHITE);
            ((Graphics2D)object2).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            ((Graphics2D)object2).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ((Graphics2D)object2).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ((Graphics2D)object2).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            ((Graphics2D)object2).setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            for (Glyph glyph : arrayList) {
                ((Graphics)object2).setFont(this.getFontForGlyph(glyph.value()));
                FontMetrics fontMetrics = ((Graphics)object2).getFontMetrics();
                ((Graphics2D)object2).drawString(String.valueOf(glyph.value()), glyph.u(), glyph.v() + fontMetrics.getAscent());
                this.glyphs.put(glyph.value(), glyph);
            }
            object = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
            IntBuffer intBuffer = MemoryUtil.memIntBuffer((long)((NativeImageAccessor)(Object)object).getPointer(), (int)(((NativeImage)object).getWidth() * ((NativeImage)object).getHeight()));
            WritableRaster writableRaster = bufferedImage.getRaster();
            ColorModel colorModel = bufferedImage.getColorModel();
            int n13 = writableRaster.getNumBands();
            int n14 = writableRaster.getDataBuffer().getDataType();
            Object objectArray = switch (n14) {
                case 0 -> new byte[n13];
                case 1 -> new short[n13];
                default -> throw new IllegalArgumentException("Unknown data buffer type: " + n14);
                case 3 -> new int[n13];
                case 4 -> new float[n13];
                case 5 -> new double[n13];
            };
            for (n = 0; n < bufferedImage.getHeight(); ++n) {
                for (int i = 0; i < bufferedImage.getWidth(); ++i) {
                    writableRaster.getDataElements(i, n, objectArray);
                    intBuffer.put(colorModel.getAlpha(objectArray) << 24 | colorModel.getBlue(objectArray) << 16 | colorModel.getGreen(objectArray) << 8 | colorModel.getRed(objectArray));
                }
            }
            int pageId = PAGE_SEQ.getAndIncrement();
            this.texture = dynamicTexture = new DynamicTexture(() -> "lightning/glyph_page_" + pageId, (NativeImage)object);
            this.textureId = Identifier.fromNamespaceAndPath((String)"lightning", (String)("glyph_page_" + pageId));
            Minecraft.getInstance().getTextureManager().register(this.textureId, (AbstractTexture)dynamicTexture);
            ((AbstractTextureAccessor)dynamicTexture).setSampler(RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.empty()));
            this.generated = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Glyph getGlyph(char c) {
        GlyphMap glyphMap;
        GlyphMap glyphMap2 = glyphMap = this;
        synchronized (glyphMap2) {
            if (!this.generated) {
                this.privateGenerate();
            }
            return (Glyph)this.glyphs.get(c);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void destroy() {
        GlyphMap glyphMap;
        GlyphMap glyphMap2 = glyphMap = this;
        synchronized (glyphMap2) {
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

    public boolean contains(char c) {
        return c >= this.include && c < this.exclude;
    }

    private Font getFontForGlyph(char c) {
        for (Font font : this.fonts) {
            if (!font.canDisplay(c)) continue;
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

