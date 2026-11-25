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
        List<Setting> childSettings;
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int frameX = Math.round((float)screenWidth * 0.23f);
        int frameY = Math.round((float)screenHeight * 0.14f);
        this.elementsFrame = new Frame("HUD", List.of(), frameX, frameY, 110, 13);
        HUDModule hUDModule = Night.MODULE_MANAGER.getModule(HUDModule.class);
        List<Setting> settings = hUDModule.getSettings();
        HashSet<CategorySetting> usedCategories = new HashSet<CategorySetting>();
        HashSet<BooleanSetting> usedEnabledSettings = new HashSet<BooleanSetting>();
        for (HudElementRegistry.Element element : HudElementRegistry.getElements().values()) {
            if (element.category() != null) {
                usedCategories.add(element.category());
            }
            if (element.enabled() != null) {
                usedEnabledSettings.add(element.enabled());
            }
            childSettings = new ArrayList<Setting>();
            if (element.category() != null) {
                childSettings.addAll(settings.stream().filter(setting -> {
                    CategorySetting.Visibility visibility;
                    Setting.Visibility visibility2 = setting.getVisibility();
                    return visibility2 instanceof CategorySetting.Visibility && (visibility = (CategorySetting.Visibility)visibility2).getValue() == element.category();
                }).filter(setting -> !(setting instanceof BooleanSetting) || !setting.getTag().equals("Enabled")).toList());
            }
            if (element.enabled() != null) {
                childSettings.addAll(settings.stream().filter(setting -> {
                    BooleanSetting.Visibility visibility;
                    Setting.Visibility visibility2 = setting.getVisibility();
                    return visibility2 instanceof BooleanSetting.Visibility && (visibility = (BooleanSetting.Visibility)visibility2).getSetting() == element.enabled();
                }).toList());
            }
            this.elementsFrame.getButtons().add(new HudElementButton(element, childSettings, this.elementsFrame, 13));
        }
        for (Setting setting2 : settings) {
            CategorySetting categorySetting;
            if (!(setting2 instanceof CategorySetting) || usedCategories.contains(categorySetting = (CategorySetting)setting2) || (childSettings = settings.stream().filter(setting -> {
                CategorySetting.Visibility visibility;
                Setting.Visibility visibility2 = setting.getVisibility();
                return visibility2 instanceof CategorySetting.Visibility && (visibility = (CategorySetting.Visibility)visibility2).getValue() == categorySetting;
            }).filter(setting -> !usedEnabledSettings.contains(setting)).toList()).isEmpty()) continue;
            this.elementsFrame.getButtons().add(new CustomCategoryButton(categorySetting, childSettings, this.elementsFrame, 13));
        }
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        FontManager.setHudRendering(true);
        try {
            for (Map.Entry<String, HudElementRegistry.Element> entry : HudElementRegistry.getElements().entrySet()) {
                String name = entry.getKey();
                HudElementRegistry.Element element = entry.getValue();
                float[] bounds = HudElementRegistry.getBounds(name);
                if (bounds == null) {
                    float bx = element.offset().getX();
                    float by = element.offset().getY();
                    bounds = new float[]{bx, by, bx + 60.0f, by + 15.0f};
                }
                boolean hovered = (float)mouseX >= bounds[0] && (float)mouseX <= bounds[2] && (float)mouseY >= bounds[1] && (float)mouseY <= bounds[3];
                boolean enabled = element.enabled().getValue();
                boolean dragging = name.equals(this.draggingElement);
                Color accentColor = enabled ? ColorUtils.getGlobalColor() : new Color(150, 150, 150);
                int bgAlpha = dragging ? 70 : (hovered ? 45 : 25);
                Renderer2D.renderQuad(guiGraphicsExtractor, bounds[0] - 1.0f, bounds[1] - 1.0f, bounds[2] + 1.0f, bounds[3] + 1.0f, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), bgAlpha));
                Renderer2D.renderOutline(guiGraphicsExtractor, bounds[0] - 1.0f, bounds[1] - 1.0f, bounds[2] + 1.0f, bounds[3] + 1.0f, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), enabled ? 220 : 120));
                if (!hovered && !dragging) continue;
                String tooltip = name + (enabled ? "" : " (disabled)");
                float tipX = bounds[0];
                float tipY = bounds[1] - (float)Night.FONT_MANAGER.getHeight() - 2.0f;
                Renderer2D.renderQuad(guiGraphicsExtractor, tipX - 1.0f, tipY - 1.0f, tipX + (float)Night.FONT_MANAGER.getWidth(tooltip) + 1.0f, tipY + (float)Night.FONT_MANAGER.getHeight(), new Color(0, 0, 0, 180));
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, tooltip, (int)tipX, (int)tipY, accentColor);
            }
            this.elementsFrame.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
        }
        finally {
            FontManager.setHudRendering(false);
        }
    }

    private boolean insideElementsFrame(double mouseX, double mouseY) {
        return mouseX >= (double)this.elementsFrame.getX() && mouseX <= (double)(this.elementsFrame.getX() + this.elementsFrame.getWidth()) && mouseY >= (double)this.elementsFrame.getY() && mouseY <= (double)(this.elementsFrame.getY() + this.elementsFrame.getTotalHeight() + 4);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        double mouseY;
        double mouseX = mouseButtonEvent.x();
        if (this.insideElementsFrame(mouseX, mouseY = mouseButtonEvent.y())) {
            this.elementsFrame.mouseClicked(mouseX, mouseY, mouseButtonEvent.button());
            return true;
        }
        for (Map.Entry<String, HudElementRegistry.Element> entry : HudElementRegistry.getElements().entrySet()) {
            String name = entry.getKey();
            HudElementRegistry.Element element = entry.getValue();
            float[] bounds = HudElementRegistry.getBounds(name);
            if (bounds == null) {
                float bx = element.offset().getX();
                float by = element.offset().getY();
                bounds = new float[]{bx, by, bx + 60.0f, by + 15.0f};
            }
            if (mouseX < (double)bounds[0] || mouseX > (double)bounds[2] || mouseY < (double)bounds[1] || mouseY > (double)bounds[3]) continue;
            if (mouseButtonEvent.button() == 1) {
                element.enabled().setValue(!element.enabled().getValue());
            } else if (mouseButtonEvent.button() == 0) {
                this.draggingElement = name;
                this.dragOffsetX = (float)mouseX - element.offset().getX();
                this.dragOffsetY = (float)mouseY - element.offset().getY();
            }
            return true;
        }
        return super.mouseClicked(mouseButtonEvent, doubleClick);
    }

    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        if (this.draggingElement != null) {
            HudElementRegistry.Element element = HudElementRegistry.getElements().get(this.draggingElement);
            if (element != null) {
                PositionSetting positionSetting = element.offset();
                positionSetting.set((float)mouseButtonEvent.x() - this.dragOffsetX, (float)mouseButtonEvent.y() - this.dragOffsetY);
                HudElementRegistry.clamp(this.draggingElement, this.width, this.height);
            }
            return true;
        }
        this.elementsFrame.mouseDragged(mouseButtonEvent.x(), mouseButtonEvent.y(), mouseButtonEvent.button(), deltaX, deltaY);
        return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        this.draggingElement = null;
        this.elementsFrame.mouseReleased(mouseButtonEvent.x(), mouseButtonEvent.y(), mouseButtonEvent.button());
        return super.mouseReleased(mouseButtonEvent);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.insideElementsFrame(mouseX, mouseY)) {
            this.elementsFrame.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        int scanCode = keyEvent.scancode();
        int modifiers = keyEvent.modifiers();
        this.elementsFrame.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 256 && !BindButton.isAnyListening()) {
            this.onClose();
            return true;
        }
        if (keyCode == 256) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char ch = (char)characterEvent.codepoint();
        int modifiers = 0;
        this.elementsFrame.charTyped(ch, modifiers);
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

        public CustomCategoryButton(CategorySetting categorySetting, List<Setting> settings, Frame frame, int height) {
            super(categorySetting, null, frame, height);
            this.category = categorySetting;
            for (Setting setting : settings) {
                Setting typedSetting;
                if (setting instanceof BooleanSetting) {
                    typedSetting = (BooleanSetting)setting;
                    this.childButtons.add(new BooleanButton((BooleanSetting)typedSetting, frame, height));
                    continue;
                }
                if (setting instanceof NumberSetting) {
                    typedSetting = (NumberSetting)setting;
                    this.childButtons.add(new NumberButton((NumberSetting)typedSetting, frame, height));
                    continue;
                }
                if (setting instanceof CategorySetting) {
                    typedSetting = (CategorySetting)setting;
                    this.childButtons.add(new CategoryButton((CategorySetting)typedSetting, null, frame, height));
                    continue;
                }
                if (setting instanceof BindSetting) {
                    typedSetting = (BindSetting)setting;
                    this.childButtons.add(new BindButton((BindSetting)typedSetting, frame, height));
                    continue;
                }
                if (setting instanceof ModeSetting) {
                    typedSetting = (ModeSetting)setting;
                    this.childButtons.add(new ModeButton((ModeSetting)typedSetting, frame, height));
                    continue;
                }
                if (setting instanceof WhitelistSetting) {
                    typedSetting = (WhitelistSetting)setting;
                    this.childButtons.add(new WhitelistButton((WhitelistSetting)typedSetting, frame, height));
                    continue;
                }
                if (setting instanceof StringSetting) {
                    typedSetting = (StringSetting)setting;
                    this.childButtons.add(new StringButton((StringSetting)typedSetting, frame, height));
                    continue;
                }
                if (!(setting instanceof ColorSetting)) continue;
                typedSetting = (ColorSetting)setting;
                this.childButtons.add(new ColorButton((ColorSetting)typedSetting, frame, height));
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
        public void setRevealHeight(int revealHeight) {
            this.revealHeight = revealHeight;
        }

        @Override
        public void setSearchQuery(String query) {
        }

        @Override
        public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
            super.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
            if (this.revealHeight > 0) {
                int listTop = this.getY() + this.getHeight();
                guiGraphicsExtractor.enableScissor(this.getX(), listTop, this.getX() + this.getWidth(), listTop + this.revealHeight);
                for (Button button : this.childButtons) {
                    if (!button.isVisible()) continue;
                    button.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
                }
                guiGraphicsExtractor.disableScissor();
            }
        }

        @Override
        public void mouseClicked(double mouseX, double mouseY, int button) {
            super.mouseClicked(mouseX, mouseY, button);
            if (this.category.isOpen()) {
                for (Button child : this.childButtons) {
                    if (!child.isVisible()) continue;
                    child.mouseClicked(mouseX, mouseY, button);
                }
            }
        }

        @Override
        public void mouseReleased(double mouseX, double mouseY, int button) {
            super.mouseReleased(mouseX, mouseY, button);
            if (this.category.isOpen()) {
                for (Button child : this.childButtons) {
                    if (!child.isVisible()) continue;
                    child.mouseReleased(mouseX, mouseY, button);
                }
            }
        }

        @Override
        public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            if (this.category.isOpen()) {
                for (Button child : this.childButtons) {
                    if (!child.isVisible()) continue;
                    child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                }
            }
        }

        @Override
        public void keyPressed(int keyCode, int scanCode, int modifiers) {
            super.keyPressed(keyCode, scanCode, modifiers);
            if (this.category.isOpen()) {
                for (Button child : this.childButtons) {
                    if (!child.isVisible()) continue;
                    child.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }

        @Override
        public void charTyped(char ch, int modifiers) {
            super.charTyped(ch, modifiers);
            if (this.category.isOpen()) {
                for (Button child : this.childButtons) {
                    if (!child.isVisible()) continue;
                    child.charTyped(ch, modifiers);
                }
            }
        }
    }
}

