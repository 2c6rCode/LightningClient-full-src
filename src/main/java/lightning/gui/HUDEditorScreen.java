/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 */
package lightning.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lightning.Night;
import lightning.gui.api.Button;
import lightning.gui.api.ExpandableRow;
import lightning.gui.api.Frame;
import lightning.gui.impl.BindButton;
import lightning.gui.impl.BooleanButton;
import lightning.gui.impl.CategoryButton;
import lightning.gui.impl.ColorButton;
import lightning.gui.impl.HudElementButton;
import lightning.gui.impl.ModeButton;
import lightning.gui.impl.NumberButton;
import lightning.gui.impl.StringButton;
import lightning.gui.impl.WhitelistButton;
import lightning.managers.FontManager;
import lightning.managers.HudElementRegistry;
import lightning.modules.impl.core.HUDEditorModule;
import lightning.modules.impl.core.HUDModule;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.PositionSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HUDEditorScreen
extends Screen {
    private String draggingElement = null;
    private float dragOffsetX;
    private float dragOffsetY;
    private final Frame elementsFrame;

    public HUDEditorScreen() {
        super((Component)Component.literal((String)"night-hud-editor"));
        List<Setting> list;
        int n = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int n2 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int n3 = Math.round((float)n * 0.23f);
        int n4 = Math.round((float)n2 * 0.14f);
        this.elementsFrame = new Frame("HUD", List.of(), n3, n4, 110, 13);
        HUDModule hUDModule = Night.MODULE_MANAGER.getModule(HUDModule.class);
        List<Setting> list2 = hUDModule.getSettings();
        HashSet<CategorySetting> hashSet = new HashSet<CategorySetting>();
        HashSet<BooleanSetting> hashSet2 = new HashSet<BooleanSetting>();
        for (HudElementRegistry.Element object : HudElementRegistry.getElements().values()) {
            if (object.category() != null) {
                hashSet.add(object.category());
            }
            if (object.enabled() != null) {
                hashSet2.add(object.enabled());
            }
            list = new ArrayList<Setting>();
            if (object.category() != null) {
                list.addAll(list2.stream().filter(setting -> {
                    CategorySetting.Visibility visibility;
                    Setting.Visibility visibility2 = setting.getVisibility();
                    return visibility2 instanceof CategorySetting.Visibility && (visibility = (CategorySetting.Visibility)visibility2).getValue() == object.category();
                }).filter(setting -> !(setting instanceof BooleanSetting) || !setting.getTag().equals("Enabled")).toList());
            }
            if (object.enabled() != null) {
                list.addAll(list2.stream().filter(setting -> {
                    BooleanSetting.Visibility visibility;
                    Setting.Visibility visibility2 = setting.getVisibility();
                    return visibility2 instanceof BooleanSetting.Visibility && (visibility = (BooleanSetting.Visibility)visibility2).getSetting() == object.enabled();
                }).toList());
            }
            this.elementsFrame.getButtons().add(new HudElementButton(object, list, this.elementsFrame, 13));
        }
        for (Setting setting2 : list2) {
            CategorySetting categorySetting;
            if (!(setting2 instanceof CategorySetting) || hashSet.contains(categorySetting = (CategorySetting)setting2) || (list = list2.stream().filter(setting -> {
                CategorySetting.Visibility visibility;
                Setting.Visibility visibility2 = setting.getVisibility();
                return visibility2 instanceof CategorySetting.Visibility && (visibility = (CategorySetting.Visibility)visibility2).getValue() == categorySetting;
            }).filter(setting -> !hashSet2.contains(setting)).toList()).isEmpty()) continue;
            this.elementsFrame.getButtons().add(new CustomCategoryButton(categorySetting, list, this.elementsFrame, 13));
        }
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        FontManager.setHudRendering(true);
        try {
            for (Map.Entry<String, HudElementRegistry.Element> entry : HudElementRegistry.getElements().entrySet()) {
                String string = entry.getKey();
                HudElementRegistry.Element element = entry.getValue();
                float[] fArray = HudElementRegistry.getBounds(string);
                if (fArray == null) {
                    float f2 = element.offset().getX();
                    float f3 = element.offset().getY();
                    fArray = new float[]{f2, f3, f2 + 60.0f, f3 + 15.0f};
                }
                boolean bl = (float)n >= fArray[0] && (float)n <= fArray[2] && (float)n2 >= fArray[1] && (float)n2 <= fArray[3];
                boolean bl2 = element.enabled().getValue();
                boolean bl3 = string.equals(this.draggingElement);
                Color color = bl2 ? ColorUtils.getGlobalColor() : new Color(150, 150, 150);
                Color color2 = color;
                int n3 = bl3 ? 70 : (bl ? 45 : 25);
                Renderer2D.renderQuad(guiGraphicsExtractor, fArray[0] - 1.0f, fArray[1] - 1.0f, fArray[2] + 1.0f, fArray[3] + 1.0f, new Color(color.getRed(), color.getGreen(), color.getBlue(), n3));
                Renderer2D.renderOutline(guiGraphicsExtractor, fArray[0] - 1.0f, fArray[1] - 1.0f, fArray[2] + 1.0f, fArray[3] + 1.0f, new Color(color.getRed(), color.getGreen(), color.getBlue(), bl2 ? 220 : 120));
                if (!bl && !bl3) continue;
                String string2 = string + (bl2 ? "" : " (disabled)");
                float f4 = fArray[0];
                float f5 = fArray[1] - (float)Night.FONT_MANAGER.getHeight() - 2.0f;
                Renderer2D.renderQuad(guiGraphicsExtractor, f4 - 1.0f, f5 - 1.0f, f4 + (float)Night.FONT_MANAGER.getWidth(string2) + 1.0f, f5 + (float)Night.FONT_MANAGER.getHeight(), new Color(0, 0, 0, 180));
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, (int)f4, (int)f5, color);
            }
            this.elementsFrame.render(guiGraphicsExtractor, n, n2, f);
        }
        finally {
            FontManager.setHudRendering(false);
        }
    }

    private boolean insideElementsFrame(double d, double d2) {
        return d >= (double)this.elementsFrame.getX() && d <= (double)(this.elementsFrame.getX() + this.elementsFrame.getWidth()) && d2 >= (double)this.elementsFrame.getY() && d2 <= (double)(this.elementsFrame.getY() + this.elementsFrame.getTotalHeight() + 4);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double d;
        double d2 = mouseButtonEvent.x();
        if (this.insideElementsFrame(d2, d = mouseButtonEvent.y())) {
            this.elementsFrame.mouseClicked(d2, d, mouseButtonEvent.button());
            return true;
        }
        for (Map.Entry<String, HudElementRegistry.Element> entry : HudElementRegistry.getElements().entrySet()) {
            String string = entry.getKey();
            HudElementRegistry.Element element = entry.getValue();
            float[] fArray = HudElementRegistry.getBounds(string);
            if (fArray == null) {
                float f = element.offset().getX();
                float f2 = element.offset().getY();
                fArray = new float[]{f, f2, f + 60.0f, f2 + 15.0f};
            }
            if (d2 < (double)fArray[0] || d2 > (double)fArray[2] || d < (double)fArray[1] || d > (double)fArray[3]) continue;
            if (mouseButtonEvent.button() == 1) {
                element.enabled().setValue(!element.enabled().getValue());
            } else if (mouseButtonEvent.button() == 0) {
                this.draggingElement = string;
                this.dragOffsetX = (float)d2 - element.offset().getX();
                this.dragOffsetY = (float)d - element.offset().getY();
            }
            return true;
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double d2) {
        if (this.draggingElement != null) {
            HudElementRegistry.Element element = HudElementRegistry.getElements().get(this.draggingElement);
            if (element != null) {
                PositionSetting positionSetting = element.offset();
                positionSetting.set((float)mouseButtonEvent.x() - this.dragOffsetX, (float)mouseButtonEvent.y() - this.dragOffsetY);
                HudElementRegistry.clamp(this.draggingElement, this.width, this.height);
            }
            return true;
        }
        this.elementsFrame.mouseDragged(mouseButtonEvent.x(), mouseButtonEvent.y(), mouseButtonEvent.button(), d, d2);
        return super.mouseDragged(mouseButtonEvent, d, d2);
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        this.draggingElement = null;
        this.elementsFrame.mouseReleased(mouseButtonEvent.x(), mouseButtonEvent.y(), mouseButtonEvent.button());
        return super.mouseReleased(mouseButtonEvent);
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.insideElementsFrame(d, d2)) {
            this.elementsFrame.mouseScrolled(d, d2, d3, d4);
            return true;
        }
        return super.mouseScrolled(d, d2, d3, d4);
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int n = keyEvent.key();
        int n2 = keyEvent.scancode();
        int n3 = keyEvent.modifiers();
        this.elementsFrame.keyPressed(n, n2, n3);
        if (n == 256 && !BindButton.isAnyListening()) {
            this.onClose();
            return true;
        }
        if (n == 256) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char c = (char)characterEvent.codepoint();
        int n = 0;
        this.elementsFrame.charTyped(c, n);
        return super.charTyped(characterEvent);
    }

    public void onClose() {
        super.onClose();
        Night.MODULE_MANAGER.getModule(HUDEditorModule.class).setToggled(false);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class CustomCategoryButton
    extends CategoryButton
    implements ExpandableRow {
        private final List<Button> childButtons = new ArrayList<Button>();
        private final CategorySetting category;
        private int revealHeight = 0;

        public CustomCategoryButton(CategorySetting categorySetting, List<Setting> list, Frame frame, int n) {
            super(categorySetting, null, frame, n);
            this.category = categorySetting;
            for (Setting setting : list) {
                Setting setting2;
                if (setting instanceof BooleanSetting) {
                    setting2 = (BooleanSetting)setting;
                    this.childButtons.add(new BooleanButton((BooleanSetting)setting2, frame, n));
                    continue;
                }
                if (setting instanceof NumberSetting) {
                    setting2 = (NumberSetting)setting;
                    this.childButtons.add(new NumberButton((NumberSetting)setting2, frame, n));
                    continue;
                }
                if (setting instanceof CategorySetting) {
                    setting2 = (CategorySetting)setting;
                    this.childButtons.add(new CategoryButton((CategorySetting)setting2, null, frame, n));
                    continue;
                }
                if (setting instanceof BindSetting) {
                    setting2 = (BindSetting)setting;
                    this.childButtons.add(new BindButton((BindSetting)setting2, frame, n));
                    continue;
                }
                if (setting instanceof ModeSetting) {
                    setting2 = (ModeSetting)setting;
                    this.childButtons.add(new ModeButton((ModeSetting)setting2, frame, n));
                    continue;
                }
                if (setting instanceof WhitelistSetting) {
                    setting2 = (WhitelistSetting)setting;
                    this.childButtons.add(new WhitelistButton((WhitelistSetting)setting2, frame, n));
                    continue;
                }
                if (setting instanceof StringSetting) {
                    setting2 = (StringSetting)setting;
                    this.childButtons.add(new StringButton((StringSetting)setting2, frame, n));
                    continue;
                }
                if (!(setting instanceof ColorSetting)) continue;
                setting2 = (ColorSetting)setting;
                this.childButtons.add(new ColorButton((ColorSetting)setting2, frame, n));
            }
        }

        @Override
        public String getRowName() {
            return this.category.getTag();
        }

        @Override
        public boolean isOpen() {
            return this.category.isOpen();
        }

        @Override
        public float getOpenAmount() {
            return this.category.getOpenAmount();
        }

        @Override
        public List<Button> getButtons() {
            return this.childButtons;
        }

        @Override
        public void setRevealHeight(int n) {
            this.revealHeight = n;
        }

        @Override
        public void setSearchQuery(String string) {
        }

        @Override
        public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
            super.render(guiGraphicsExtractor, n, n2, f);
            if (this.revealHeight > 0) {
                int n3 = this.getY() + this.getHeight();
                guiGraphicsExtractor.enableScissor(this.getX(), n3, this.getX() + this.getWidth(), n3 + this.revealHeight);
                for (Button button : this.childButtons) {
                    if (!button.isVisible()) continue;
                    button.render(guiGraphicsExtractor, n, n2, f);
                }
                guiGraphicsExtractor.disableScissor();
            }
        }

        @Override
        public void mouseClicked(double d, double d2, int n) {
            super.mouseClicked(d, d2, n);
            if (this.category.isOpen()) {
                for (Button button : this.childButtons) {
                    if (!button.isVisible()) continue;
                    button.mouseClicked(d, d2, n);
                }
            }
        }

        @Override
        public void mouseReleased(double d, double d2, int n) {
            super.mouseReleased(d, d2, n);
            if (this.category.isOpen()) {
                for (Button button : this.childButtons) {
                    if (!button.isVisible()) continue;
                    button.mouseReleased(d, d2, n);
                }
            }
        }

        @Override
        public void mouseDragged(double d, double d2, int n, double d3, double d4) {
            super.mouseDragged(d, d2, n, d3, d4);
            if (this.category.isOpen()) {
                for (Button button : this.childButtons) {
                    if (!button.isVisible()) continue;
                    button.mouseDragged(d, d2, n, d3, d4);
                }
            }
        }

        @Override
        public void keyPressed(int n, int n2, int n3) {
            super.keyPressed(n, n2, n3);
            if (this.category.isOpen()) {
                for (Button button : this.childButtons) {
                    if (!button.isVisible()) continue;
                    button.keyPressed(n, n2, n3);
                }
            }
        }

        @Override
        public void charTyped(char c, int n) {
            super.charTyped(c, n);
            if (this.category.isOpen()) {
                for (Button button : this.childButtons) {
                    if (!button.isVisible()) continue;
                    button.charTyped(c, n);
                }
            }
        }
    }
}

