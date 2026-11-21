/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  lombok.Generated
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.gui.Font$GlyphVisitor
 *  net.minecraft.client.gui.Font$PreparedText
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.font.TextRenderable
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix4fc
 */
package lightning.managers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lightning.Night;
import lightning.modules.impl.core.FontModule;
import lightning.modules.impl.core.HUDModule;
import lightning.utils.IMinecraft;
import lightning.utils.color.ColorUtils;
import lightning.utils.font.FontRenderer;
import lightning.utils.graphics.Renderer3D;
import lombok.Generated;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4fc;

public class FontManager
implements IMinecraft {
    private FontRenderer fontRenderer;
    private static final ThreadLocal<Boolean> HUD_RENDERING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> CLIENT_TEXT_DEPTH = ThreadLocal.withInitial(() -> 0);

    public static void pushClientTextRendering() {
        CLIENT_TEXT_DEPTH.set(CLIENT_TEXT_DEPTH.get() + 1);
    }

    public static void popClientTextRendering() {
        int n = CLIENT_TEXT_DEPTH.get() - 1;
        CLIENT_TEXT_DEPTH.set(Math.max(0, n));
    }

    public static boolean isClientTextRendering() {
        return CLIENT_TEXT_DEPTH.get() > 0;
    }

    public static void setHudRendering(boolean bl) {
        HUD_RENDERING.set(bl);
    }

    public static boolean isHudRendering() {
        return HUD_RENDERING.get();
    }

    public boolean isHudGlowEnabled() {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        HUDModule hUDModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(HUDModule.class) : null;
        boolean bl = fontModule != null && fontModule.isToggled() && fontModule.glow.getValue();
        boolean bl2 = hUDModule != null && hUDModule.isToggled() && hUDModule.textGlow.getValue();
        return bl || bl2;
    }

    public boolean isCustomFontActive() {
        return this.useCustomFont(false);
    }

    private boolean useCustomFont() {
        return this.useCustomFont(false);
    }

    private boolean useCustomFont(boolean bl) {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        FontModule fontModule2 = fontModule;
        if (fontModule == null || !fontModule.isToggled()) {
            return false;
        }
        boolean bl2 = fontModule.customFont.getValue() || bl || FontManager.isHudRendering() && this.isHudGlowEnabled();
        boolean bl3 = bl2;
        if (this.fontRenderer == null && bl2) {
            fontModule.updateFontRenderer();
        }
        return this.fontRenderer != null && bl2;
    }

    private boolean shadowEnabled() {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        return fontModule != null && !fontModule.shadowMode.getValue().equalsIgnoreCase("None");
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
        this.drawText(guiGraphicsExtractor, string, n, n2, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color, boolean bl) {
        FontManager.pushClientTextRendering();
        try {
            boolean bl2 = bl || FontManager.isHudRendering() && this.isHudGlowEnabled();
            boolean bl3 = bl2;
            if (this.useCustomFont(bl2)) {
                if (bl2) {
                    this.fontRenderer.drawString(guiGraphicsExtractor, string, (float)n, (float)n2, color.getRGB(), false, true, false);
                }
                this.fontRenderer.drawString(guiGraphicsExtractor, string, (float)n, (float)n2, color.getRGB(), false, false, true);
            } else {
                guiGraphicsExtractor.text(FontManager.mc.font, string, n, n2, color.getRGB(), false);
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
        this.drawTextWithShadow(guiGraphicsExtractor, string, n, n2, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color, boolean bl) {
        FontManager.pushClientTextRendering();
        try {
            boolean bl2 = bl || FontManager.isHudRendering() && this.isHudGlowEnabled();
            boolean bl3 = bl2;
            if (this.useCustomFont(bl2)) {
                if (bl2) {
                    this.fontRenderer.drawString(guiGraphicsExtractor, string, (float)n, (float)n2, color.getRGB(), false, true, false);
                }
                if (this.shadowEnabled()) {
                    this.fontRenderer.drawString(guiGraphicsExtractor, string, (float)n + this.getShadowOffset(), (float)n2 + this.getShadowOffset(), color.getRGB(), true, false, true);
                }
                this.fontRenderer.drawString(guiGraphicsExtractor, string, (float)n, (float)n2, color.getRGB(), false, false, true);
            } else {
                guiGraphicsExtractor.text(FontManager.mc.font, string, n, n2, color.getRGB(), this.shadowEnabled());
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence formattedCharSequence, int n, int n2, Color color) {
        this.drawText(guiGraphicsExtractor, formattedCharSequence, n, n2, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence formattedCharSequence, int n, int n2, Color color, boolean bl) {
        FontManager.pushClientTextRendering();
        try {
            boolean bl2 = bl || FontManager.isHudRendering() && this.isHudGlowEnabled();
            boolean bl3 = bl2;
            if (this.useCustomFont(bl2)) {
                if (bl2) {
                    this.fontRenderer.drawText(guiGraphicsExtractor, formattedCharSequence, (float)n, (float)n2, color.getRGB(), false, true, false);
                }
                this.fontRenderer.drawText(guiGraphicsExtractor, formattedCharSequence, (float)n, (float)n2, color.getRGB(), false, false, true);
            } else {
                guiGraphicsExtractor.text(FontManager.mc.font, formattedCharSequence, n, n2, color.getRGB(), false);
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence formattedCharSequence, int n, int n2, Color color) {
        this.drawTextWithShadow(guiGraphicsExtractor, formattedCharSequence, n, n2, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(GuiGraphicsExtractor guiGraphicsExtractor, FormattedCharSequence formattedCharSequence, int n, int n2, Color color, boolean bl) {
        FontManager.pushClientTextRendering();
        try {
            boolean bl2 = bl || FontManager.isHudRendering() && this.isHudGlowEnabled();
            boolean bl3 = bl2;
            if (this.useCustomFont(bl2)) {
                if (bl2) {
                    this.fontRenderer.drawText(guiGraphicsExtractor, formattedCharSequence, (float)n, (float)n2, color.getRGB(), false, true, false);
                }
                if (this.shadowEnabled()) {
                    this.fontRenderer.drawText(guiGraphicsExtractor, formattedCharSequence, (float)n + this.getShadowOffset(), (float)n2 + this.getShadowOffset(), color.getRGB(), true, false, true);
                }
                this.fontRenderer.drawText(guiGraphicsExtractor, formattedCharSequence, (float)n, (float)n2, color.getRGB(), false, false, true);
            } else {
                guiGraphicsExtractor.text(FontManager.mc.font, formattedCharSequence, n, n2, color.getRGB(), this.shadowEnabled());
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(PoseStack poseStack, String string, int n, int n2, Color color) {
        this.drawTextWithShadow(poseStack, string, (float)n, (float)n2, color, false);
    }

    public void drawTextWithShadow(PoseStack poseStack, String string, int n, int n2, Color color, boolean bl) {
        this.drawTextWithShadow(poseStack, string, (float)n, (float)n2, color, bl);
    }

    public void drawTextWithShadow(PoseStack poseStack, String string, float f, float f2, Color color) {
        this.drawTextWithShadow(poseStack, string, f, f2, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(final PoseStack poseStack, String string, float f, float f2, Color color, boolean bl) {
        block12: {
            FontManager.pushClientTextRendering();
            try {
                boolean bl2 = bl || FontManager.isHudRendering() && this.isHudGlowEnabled();
                boolean bl3 = bl2;
                if (this.useCustomFont(bl2)) {
                    if (bl2) {
                        this.fontRenderer.drawString(poseStack, string, f, f2, color.getRGB(), false, true, false);
                    }
                    if (this.shadowEnabled() && !bl) {
                        this.fontRenderer.drawString(poseStack, string, f + this.getShadowOffset(), f2 + this.getShadowOffset(), color.getRGB(), true, false, true);
                    }
                    this.fontRenderer.drawString(poseStack, string, f, f2, color.getRGB(), false, false, true);
                    break block12;
                }
                Font.PreparedText preparedText = FontManager.mc.font.prepareText(string, f, f2, color.getRGB(), this.shadowEnabled() && !bl, 0);
                final HashMap<RenderType, ByteBufferBuilder> hashMap = new HashMap<>();
                final HashMap<RenderType, BufferBuilder> hashMap2 = new HashMap<>();
                try {
                    preparedText.visit(new Font.GlyphVisitor() {
                        @Override
                        public void acceptRenderable(TextRenderable textRenderable) {
                            RenderType renderType2 = textRenderable.renderType(Font.DisplayMode.SEE_THROUGH);
                            BufferBuilder bufferBuilder = hashMap2.computeIfAbsent(renderType2, renderType -> {
                                ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
                                hashMap.put(renderType, byteBufferBuilder);
                                return new BufferBuilder(byteBufferBuilder, renderType.primitiveTopology(), renderType.format());
                            });
                            textRenderable.render((Matrix4fc)poseStack.last().pose(), (VertexConsumer)bufferBuilder, 0xF000F0, false);
                        }
                    });
                    for (Map.Entry<RenderType, BufferBuilder> entry : hashMap2.entrySet()) {
                        MeshData meshData = entry.getValue().build();
                        Renderer3D.draw(entry.getKey(), meshData);
                    }
                }
                finally {
                    for (ByteBufferBuilder byteBufferBuilder : hashMap.values()) {
                        byteBufferBuilder.close();
                    }
                }
            }
            finally {
                FontManager.popClientTextRendering();
            }
        }
    }

    public void drawText(PoseStack poseStack, String string, int n, int n2, Color color) {
        this.drawText(poseStack, string, (float)n, (float)n2, color, false);
    }

    public void drawText(PoseStack poseStack, String string, int n, int n2, Color color, boolean bl) {
        this.drawText(poseStack, string, (float)n, (float)n2, color, bl);
    }

    public void drawText(PoseStack poseStack, String string, float f, float f2, Color color) {
        this.drawText(poseStack, string, f, f2, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(final PoseStack poseStack, String string, float f, float f2, Color color, boolean bl) {
        block11: {
            FontManager.pushClientTextRendering();
            try {
                boolean bl2 = bl || FontManager.isHudRendering() && this.isHudGlowEnabled();
                boolean bl3 = bl2;
                if (this.useCustomFont(bl2)) {
                    if (bl2) {
                        this.fontRenderer.drawString(poseStack, string, f, f2, color.getRGB(), false, true, false);
                    }
                    this.fontRenderer.drawString(poseStack, string, f, f2, color.getRGB(), false, false, true);
                    break block11;
                }
                Font.PreparedText preparedText = FontManager.mc.font.prepareText(string, f, f2, color.getRGB(), false, 0);
                final HashMap<RenderType, ByteBufferBuilder> hashMap = new HashMap<>();
                final HashMap<RenderType, BufferBuilder> hashMap2 = new HashMap<>();
                try {
                    preparedText.visit(new Font.GlyphVisitor() {
                        @Override
                        public void acceptRenderable(TextRenderable textRenderable) {
                            RenderType renderType2 = textRenderable.renderType(Font.DisplayMode.SEE_THROUGH);
                            BufferBuilder bufferBuilder = hashMap2.computeIfAbsent(renderType2, renderType -> {
                                ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
                                hashMap.put(renderType, byteBufferBuilder);
                                return new BufferBuilder(byteBufferBuilder, renderType.primitiveTopology(), renderType.format());
                            });
                            textRenderable.render((Matrix4fc)poseStack.last().pose(), (VertexConsumer)bufferBuilder, 0xF000F0, false);
                        }
                    });
                    for (Map.Entry<RenderType, BufferBuilder> entry : hashMap2.entrySet()) {
                        MeshData meshData = entry.getValue().build();
                        Renderer3D.draw(entry.getKey(), meshData);
                    }
                }
                finally {
                    for (ByteBufferBuilder byteBufferBuilder : hashMap.values()) {
                        byteBufferBuilder.close();
                    }
                }
            }
            finally {
                FontManager.popClientTextRendering();
            }
        }
    }

    public void drawTextWithOutline(GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color, Color color2) {
        if (this.useCustomFont()) {
            this.fontRenderer.drawString(guiGraphicsExtractor, FontRenderer.stripControlCodes(string), (float)n + 0.5f, (float)n2 - 0.5f, color2.getRGB(), false);
            this.fontRenderer.drawString(guiGraphicsExtractor, FontRenderer.stripControlCodes(string), (float)n - 0.5f, (float)n2 + 0.5f, color2.getRGB(), false);
            this.fontRenderer.drawString(guiGraphicsExtractor, FontRenderer.stripControlCodes(string), (float)n + 0.5f, (float)n2 + 0.5f, color2.getRGB(), false);
            this.fontRenderer.drawString(guiGraphicsExtractor, FontRenderer.stripControlCodes(string), (float)n - 0.5f, (float)n2 - 0.5f, color2.getRGB(), false);
            this.fontRenderer.drawString(guiGraphicsExtractor, string, (float)n, (float)n2, color.getRGB(), false);
        } else {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    if (i == 0 && j == 0) continue;
                    guiGraphicsExtractor.text(FontManager.mc.font, string, n + i, n2 + j, color2.getRGB(), false);
                }
            }
            guiGraphicsExtractor.text(FontManager.mc.font, string, n, n2, color.getRGB(), false);
        }
    }

    public void drawRainbowString(GuiGraphicsExtractor guiGraphicsExtractor, String string, int n3, int n4, long l) {
        MutableComponent mutableComponent = Component.empty();
        int[] nArray = new int[]{0};
        Component.literal((String)string).getVisualOrderText().accept((n, style, n2) -> {
            MutableComponent mutableComponent2 = Component.empty();
            if (style.getColor() == null) {
                long l2 = (long)nArray[0] * l;
                Color color = ColorUtils.getOffsetRainbow(l2);
                mutableComponent2.append((Component)Component.literal((String)String.valueOf(Character.toChars(n2))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)color.getRGB()))));
            } else {
                mutableComponent2.append((Component)Component.literal((String)String.valueOf(Character.toChars(n2))).setStyle(style));
            }
            mutableComponent.append((Component)mutableComponent2);
            nArray[0] = nArray[0] + 1;
            return true;
        });
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, mutableComponent.getVisualOrderText(), n3, n4, Color.WHITE);
    }

    public int getWidth(String string) {
        if (this.useCustomFont()) {
            return (int)this.fontRenderer.getTextWidth(string);
        }
        return FontManager.mc.font.width(string);
    }

    public int getHeight() {
        if (this.useCustomFont()) {
            FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
            int n = fontModule != null ? fontModule.heightOffset.getValue().intValue() : 0;
            return (int)this.fontRenderer.getHeight() + n;
        }
        Objects.requireNonNull(FontManager.mc.font);
        return 9;
    }

    public float getShadowOffset() {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        FontModule fontModule2 = fontModule;
        if (fontModule == null || fontModule.shadowMode.getValue().equalsIgnoreCase("None")) {
            return 0.0f;
        }
        if (fontModule.shadowMode.getValue().equalsIgnoreCase("Custom")) {
            return fontModule.shadowOffset.getValue().floatValue();
        }
        return 1.0f;
    }

    public boolean hasFont(String string) {
        for (String string2 : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (!string2.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    @Generated
    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    @Generated
    public void setFontRenderer(FontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }
}

