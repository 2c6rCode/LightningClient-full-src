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
        int depth = CLIENT_TEXT_DEPTH.get() - 1;
        CLIENT_TEXT_DEPTH.set(Math.max(0, depth));
    }

    public static boolean isClientTextRendering() {
        return CLIENT_TEXT_DEPTH.get() > 0;
    }

    public static void setHudRendering(boolean hudRendering) {
        HUD_RENDERING.set(hudRendering);
    }

    public static boolean isHudRendering() {
        return HUD_RENDERING.get();
    }

    public boolean isHudGlowEnabled() {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        HUDModule hudModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(HUDModule.class) : null;
        boolean fontGlow = fontModule != null && fontModule.isToggled() && fontModule.glow.getValue();
        boolean hudGlow = hudModule != null && hudModule.isToggled() && hudModule.textGlow.getValue();
        return fontGlow || hudGlow;
    }

    public boolean isCustomFontActive() {
        return this.useCustomFont(false);
    }

    private boolean useCustomFont() {
        return this.useCustomFont(false);
    }

    private boolean useCustomFont(boolean forceGlow) {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        if (fontModule == null || !fontModule.isToggled()) {
            return false;
        }
        boolean enabled = fontModule.customFont.getValue() || forceGlow || FontManager.isHudRendering() && this.isHudGlowEnabled();
        if (this.fontRenderer == null && enabled) {
            fontModule.updateFontRenderer();
        }
        return this.fontRenderer != null && enabled;
    }

    private boolean shadowEnabled() {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        return fontModule != null && !fontModule.shadowMode.getValue().equalsIgnoreCase("None");
    }

    public void drawText(GuiGraphicsExtractor context, String text, int x, int y, Color color) {
        this.drawText(context, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(GuiGraphicsExtractor context, String text, int x, int y, Color color, boolean glow) {
        FontManager.pushClientTextRendering();
        try {
            boolean useGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
            if (this.useCustomFont(useGlow)) {
                if (useGlow) {
                    this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false, true, false);
                }
                this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false, false, true);
            } else {
                context.text(FontManager.mc.font, text, x, y, color.getRGB(), false);
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(GuiGraphicsExtractor context, String text, int x, int y, Color color) {
        this.drawTextWithShadow(context, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(GuiGraphicsExtractor context, String text, int x, int y, Color color, boolean glow) {
        FontManager.pushClientTextRendering();
        try {
            boolean useGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
            if (this.useCustomFont(useGlow)) {
                if (useGlow) {
                    this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false, true, false);
                }
                if (this.shadowEnabled()) {
                    this.fontRenderer.drawString(context, text, (float)x + this.getShadowOffset(), (float)y + this.getShadowOffset(), color.getRGB(), true, false, true);
                }
                this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false, false, true);
            } else {
                context.text(FontManager.mc.font, text, x, y, color.getRGB(), this.shadowEnabled());
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawText(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, Color color) {
        this.drawText(context, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, Color color, boolean glow) {
        FontManager.pushClientTextRendering();
        try {
            boolean useGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
            if (this.useCustomFont(useGlow)) {
                if (useGlow) {
                    this.fontRenderer.drawText(context, text, (float)x, (float)y, color.getRGB(), false, true, false);
                }
                this.fontRenderer.drawText(context, text, (float)x, (float)y, color.getRGB(), false, false, true);
            } else {
                context.text(FontManager.mc.font, text, x, y, color.getRGB(), false);
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, Color color) {
        this.drawTextWithShadow(context, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, Color color, boolean glow) {
        FontManager.pushClientTextRendering();
        try {
            boolean useGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
            if (this.useCustomFont(useGlow)) {
                if (useGlow) {
                    this.fontRenderer.drawText(context, text, (float)x, (float)y, color.getRGB(), false, true, false);
                }
                if (this.shadowEnabled()) {
                    this.fontRenderer.drawText(context, text, (float)x + this.getShadowOffset(), (float)y + this.getShadowOffset(), color.getRGB(), true, false, true);
                }
                this.fontRenderer.drawText(context, text, (float)x, (float)y, color.getRGB(), false, false, true);
            } else {
                context.text(FontManager.mc.font, text, x, y, color.getRGB(), this.shadowEnabled());
            }
        }
        finally {
            FontManager.popClientTextRendering();
        }
    }

    public void drawTextWithShadow(PoseStack poseStack, String text, int x, int y, Color color) {
        this.drawTextWithShadow(poseStack, text, (float)x, (float)y, color, false);
    }

    public void drawTextWithShadow(PoseStack poseStack, String text, int x, int y, Color color, boolean glow) {
        this.drawTextWithShadow(poseStack, text, (float)x, (float)y, color, glow);
    }

    public void drawTextWithShadow(PoseStack poseStack, String text, float x, float y, Color color) {
        this.drawTextWithShadow(poseStack, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawTextWithShadow(final PoseStack poseStack, String text, float x, float y, Color color, boolean glow) {
        block12: {
            FontManager.pushClientTextRendering();
            try {
                boolean useGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
                if (this.useCustomFont(useGlow)) {
                    if (useGlow) {
                        this.fontRenderer.drawString(poseStack, text, x, y, color.getRGB(), false, true, false);
                    }
                    if (this.shadowEnabled() && !glow) {
                        this.fontRenderer.drawString(poseStack, text, x + this.getShadowOffset(), y + this.getShadowOffset(), color.getRGB(), true, false, true);
                    }
                    this.fontRenderer.drawString(poseStack, text, x, y, color.getRGB(), false, false, true);
                    break block12;
                }
                Font.PreparedText preparedText = FontManager.mc.font.prepareText(text, x, y, color.getRGB(), this.shadowEnabled() && !glow, 0);
                final HashMap<RenderType, ByteBufferBuilder> byteBuilders = new HashMap<>();
                final HashMap<RenderType, BufferBuilder> bufferBuilders = new HashMap<>();
                try {
                    preparedText.visit(new Font.GlyphVisitor() {
                        @Override
                        public void acceptRenderable(TextRenderable textRenderable) {
                            RenderType renderType = textRenderable.renderType(Font.DisplayMode.SEE_THROUGH);
                            BufferBuilder bufferBuilder = bufferBuilders.computeIfAbsent(renderType, key -> {
                                ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
                                byteBuilders.put(key, byteBufferBuilder);
                                return new BufferBuilder(byteBufferBuilder, key.primitiveTopology(), key.format());
                            });
                            textRenderable.render((Matrix4fc)poseStack.last().pose(), (VertexConsumer)bufferBuilder, 0xF000F0, false);
                        }
                    });
                    for (Map.Entry<RenderType, BufferBuilder> entry : bufferBuilders.entrySet()) {
                        MeshData meshData = entry.getValue().build();
                        Renderer3D.draw(entry.getKey(), meshData);
                    }
                }
                finally {
                    for (ByteBufferBuilder byteBufferBuilder : byteBuilders.values()) {
                        byteBufferBuilder.close();
                    }
                }
            }
            finally {
                FontManager.popClientTextRendering();
            }
        }
    }

    public void drawText(PoseStack poseStack, String text, int x, int y, Color color) {
        this.drawText(poseStack, text, (float)x, (float)y, color, false);
    }

    public void drawText(PoseStack poseStack, String text, int x, int y, Color color, boolean glow) {
        this.drawText(poseStack, text, (float)x, (float)y, color, glow);
    }

    public void drawText(PoseStack poseStack, String text, float x, float y, Color color) {
        this.drawText(poseStack, text, x, y, color, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void drawText(final PoseStack poseStack, String text, float x, float y, Color color, boolean glow) {
        block11: {
            FontManager.pushClientTextRendering();
            try {
                boolean useGlow = glow || FontManager.isHudRendering() && this.isHudGlowEnabled();
                if (this.useCustomFont(useGlow)) {
                    if (useGlow) {
                        this.fontRenderer.drawString(poseStack, text, x, y, color.getRGB(), false, true, false);
                    }
                    this.fontRenderer.drawString(poseStack, text, x, y, color.getRGB(), false, false, true);
                    break block11;
                }
                Font.PreparedText preparedText = FontManager.mc.font.prepareText(text, x, y, color.getRGB(), false, 0);
                final HashMap<RenderType, ByteBufferBuilder> byteBuilders = new HashMap<>();
                final HashMap<RenderType, BufferBuilder> bufferBuilders = new HashMap<>();
                try {
                    preparedText.visit(new Font.GlyphVisitor() {
                        @Override
                        public void acceptRenderable(TextRenderable textRenderable) {
                            RenderType renderType = textRenderable.renderType(Font.DisplayMode.SEE_THROUGH);
                            BufferBuilder bufferBuilder = bufferBuilders.computeIfAbsent(renderType, key -> {
                                ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256);
                                byteBuilders.put(key, byteBufferBuilder);
                                return new BufferBuilder(byteBufferBuilder, key.primitiveTopology(), key.format());
                            });
                            textRenderable.render((Matrix4fc)poseStack.last().pose(), (VertexConsumer)bufferBuilder, 0xF000F0, false);
                        }
                    });
                    for (Map.Entry<RenderType, BufferBuilder> entry : bufferBuilders.entrySet()) {
                        MeshData meshData = entry.getValue().build();
                        Renderer3D.draw(entry.getKey(), meshData);
                    }
                }
                finally {
                    for (ByteBufferBuilder byteBufferBuilder : byteBuilders.values()) {
                        byteBufferBuilder.close();
                    }
                }
            }
            finally {
                FontManager.popClientTextRendering();
            }
        }
    }

    public void drawTextWithOutline(GuiGraphicsExtractor context, String text, int x, int y, Color color, Color outlineColor) {
        if (this.useCustomFont()) {
            this.fontRenderer.drawString(context, FontRenderer.stripControlCodes(text), (float)x + 0.5f, (float)y - 0.5f, outlineColor.getRGB(), false);
            this.fontRenderer.drawString(context, FontRenderer.stripControlCodes(text), (float)x - 0.5f, (float)y + 0.5f, outlineColor.getRGB(), false);
            this.fontRenderer.drawString(context, FontRenderer.stripControlCodes(text), (float)x + 0.5f, (float)y + 0.5f, outlineColor.getRGB(), false);
            this.fontRenderer.drawString(context, FontRenderer.stripControlCodes(text), (float)x - 0.5f, (float)y - 0.5f, outlineColor.getRGB(), false);
            this.fontRenderer.drawString(context, text, (float)x, (float)y, color.getRGB(), false);
        } else {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    if (i == 0 && j == 0) continue;
                    context.text(FontManager.mc.font, text, x + i, y + j, outlineColor.getRGB(), false);
                }
            }
            context.text(FontManager.mc.font, text, x, y, color.getRGB(), false);
        }
    }

    public void drawRainbowString(GuiGraphicsExtractor context, String text, int x, int y, long offsetStep) {
        MutableComponent mutableComponent = Component.empty();
        int[] charIndex = new int[]{0};
        Component.literal((String)text).getVisualOrderText().accept((index, style, codePoint) -> {
            MutableComponent charComponent = Component.empty();
            if (style.getColor() == null) {
                long offset = (long)charIndex[0] * offsetStep;
                Color color = ColorUtils.getOffsetRainbow(offset);
                charComponent.append((Component)Component.literal((String)String.valueOf(Character.toChars(codePoint))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)color.getRGB()))));
            } else {
                charComponent.append((Component)Component.literal((String)String.valueOf(Character.toChars(codePoint))).setStyle(style));
            }
            mutableComponent.append((Component)charComponent);
            charIndex[0] = charIndex[0] + 1;
            return true;
        });
        Night.FONT_MANAGER.drawTextWithShadow(context, mutableComponent.getVisualOrderText(), x, y, Color.WHITE);
    }

    public int getWidth(String text) {
        if (this.useCustomFont()) {
            return (int)this.fontRenderer.getTextWidth(text);
        }
        return FontManager.mc.font.width(text);
    }

    public int getHeight() {
        if (this.useCustomFont()) {
            FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
            int heightOffset = fontModule != null ? fontModule.heightOffset.getValue().intValue() : 0;
            return (int)this.fontRenderer.getHeight() + heightOffset;
        }
        Objects.requireNonNull(FontManager.mc.font);
        return 9;
    }

    public float getShadowOffset() {
        FontModule fontModule = Night.MODULE_MANAGER != null ? Night.MODULE_MANAGER.getModule(FontModule.class) : null;
        if (fontModule == null || fontModule.shadowMode.getValue().equalsIgnoreCase("None")) {
            return 0.0f;
        }
        if (fontModule.shadowMode.getValue().equalsIgnoreCase("Custom")) {
            return fontModule.shadowOffset.getValue().floatValue();
        }
        return 1.0f;
    }

    public boolean hasFont(String fontName) {
        for (String availableFont : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (!availableFont.equalsIgnoreCase(fontName)) continue;
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

