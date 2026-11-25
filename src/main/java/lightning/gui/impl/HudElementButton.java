/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.gui.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.api.Button;
import lightning.gui.api.ExpandableRow;
import lightning.gui.api.Frame;
import lightning.gui.impl.BindButton;
import lightning.gui.impl.BooleanButton;
import lightning.gui.impl.CategoryButton;
import lightning.gui.impl.ColorButton;
import lightning.gui.impl.ModeButton;
import lightning.gui.impl.NumberButton;
import lightning.gui.impl.StringButton;
import lightning.gui.impl.WhitelistButton;
import lightning.managers.HudElementRegistry;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.NeekeriFill;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HudElementButton
extends Button
implements ExpandableRow {
    private final HudElementRegistry.Element element;
    private boolean open = false;
    private final ArrayList<Button> buttons = new ArrayList();
    private String searchQuery = "";
    private final Animation openAnim = new Animation(180, Easing.Method.EASE_OUT_QUAD);
    private int revealHeight = 0;
    private final Animation fillAnim;

    public HudElementRegistry.Element getElement() {
        return this.element;
    }

    @Override
    public boolean isOpen() {
        return this.element.category() != null ? this.element.category().isOpen() : this.open;
    }

    @Override
    public float getOpenAmount() {
        return this.element.category() != null ? this.element.category().getOpenAmount() : this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Override
    public String getRowName() {
        return this.element.name();
    }

    @Override
    public List<Button> getButtons() {
        return this.buttons;
    }

    @Override
    public void setRevealHeight(int revealHeight) {
        this.revealHeight = revealHeight;
    }

    @Override
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public int getRevealHeight() {
        return this.revealHeight;
    }

    public HudElementButton(HudElementRegistry.Element element, List<Setting> settings, Frame frame, int height) {
        super(frame, height, element.name());
        this.element = element;
        float initialFill = element.enabled().getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(initialFill, initialFill, 180, Easing.Method.EASE_OUT_QUAD);
        for (Setting setting : settings) {
            if (setting instanceof BooleanSetting booleanSetting) {
                this.buttons.add(new BooleanButton(booleanSetting, frame, height));
                continue;
            }
            if (setting instanceof NumberSetting numberSetting) {
                this.buttons.add(new NumberButton(numberSetting, frame, height));
                continue;
            }
            if (setting instanceof CategorySetting categorySetting) {
                this.buttons.add(new CategoryButton(categorySetting, null, frame, height));
                continue;
            }
            if (setting instanceof BindSetting bindSetting) {
                this.buttons.add(new BindButton(bindSetting, frame, height));
                continue;
            }
            if (setting instanceof ModeSetting modeSetting) {
                this.buttons.add(new ModeButton(modeSetting, frame, height));
                continue;
            }
            if (setting instanceof WhitelistSetting whitelistSetting) {
                this.buttons.add(new WhitelistButton(whitelistSetting, frame, height));
                continue;
            }
            if (setting instanceof StringSetting stringSetting) {
                this.buttons.add(new StringButton(stringSetting, frame, height));
                continue;
            }
            if (!(setting instanceof ColorSetting colorSetting)) continue;
            this.buttons.add(new ColorButton(colorSetting, frame, height));
        }
    }

    private void updateChildBounds() {
        int currentY = this.getY() + this.getHeight();
        for (Button button : this.buttons) {
            button.setX(this.getX());
            button.setY(currentY);
            if (!button.isVisible()) continue;
            currentY += button.getHeight();
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        this.updateChildBounds();
        if (this.isHovering(mouseX, mouseY) && Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            Night.CLICK_GUI.getDescriptionFrame().setDescription(this.getDescription());
        }
        Color hoverColor = this.isHovering(mouseX, mouseY) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
        if (hoverColor.getAlpha() > 0) {
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY(), this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight() - 1, hoverColor);
        }
        float fillProgress = this.fillAnim.get(this.element.enabled().getValue() ? 1.0f : 0.0f);
        if (fillProgress > 0.001f) {
            ClickGuiModule clickGui = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            boolean useNeekeri = !clickGui.fillMode.getValue().equalsIgnoreCase("Default");
            int fillEndX = this.getX() + this.getPadding() + Math.round((float)(this.getWidth() - this.getPadding() * 2) * fillProgress);
            if (useNeekeri) {
                int opacityAlpha = Math.round(clickGui.neekeriOpacity.getValue().floatValue() / 100.0f * 255.0f);
                guiGraphicsExtractor.enableScissor(this.getX() + this.getPadding(), this.getY(), fillEndX, this.getY() + this.getHeight() - 1);
                NeekeriFill.fill(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY(), this.getWidth() - this.getPadding() * 2, this.getHeight() - 1, opacityAlpha);
                guiGraphicsExtractor.disableScissor();
            } else {
                Color fillColor = ColorUtils.getColor(clickGui.color.getColor(), 90);
                Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY(), fillEndX, this.getY() + this.getHeight() - 1, fillColor);
            }
        }
        Color dividerColor = ClickGuiScreen.getButtonColor(this.getY(), 60);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight(), dividerColor);
        int textX = this.getX() + this.getTextPadding() + (this.element.enabled().getValue() ? 1 : 0);
        int textY = this.getY() + 2;
        String prefix = this.element.enabled().getValue() ? "" : ChatFormatting.GRAY.toString();
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, prefix + this.element.name(), textX, textY, Color.WHITE);
        if (!this.buttons.isEmpty()) {
            String expandIcon = this.isOpen() ? "..." : "+";
            int iconX = this.getX() + this.getWidth() - this.getTextPadding() - Night.FONT_MANAGER.getWidth(expandIcon) - 1;
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, expandIcon, iconX, this.getY() + 2, Color.WHITE);
        }
        if (this.revealHeight > 0) {
            int scissorTop = this.getY() + this.getHeight();
            guiGraphicsExtractor.enableScissor(this.getX(), scissorTop, this.getX() + this.getWidth(), scissorTop + this.revealHeight);
            for (Button button : this.buttons) {
                if (!button.isVisible()) {
                    if (button instanceof BooleanButton booleanButton) {
                        booleanButton.setPrevVisible(false);
                        continue;
                    }
                    if (!(button instanceof NumberButton numberButton)) continue;
                    numberButton.setPrevVisible(false);
                    continue;
                }
                button.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
                if (!button.isHovering(mouseX, mouseY) || !Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) continue;
                Night.CLICK_GUI.getDescriptionFrame().setDescription(button.getDescription());
            }
            guiGraphicsExtractor.disableScissor();
        } else {
            for (Button button : this.buttons) {
                if (button instanceof BooleanButton booleanButton) {
                    booleanButton.setPrevVisible(false);
                    continue;
                }
                if (!(button instanceof NumberButton numberButton)) continue;
                numberButton.setPrevVisible(false);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        this.updateChildBounds();
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.element.enabled().setValue(!this.element.enabled().getValue());
                this.playClickSound();
            } else if (button == 1) {
                if (this.element.category() != null) {
                    this.element.category().setOpen(!this.element.category().isOpen());
                } else {
                    this.open = !this.open;
                    this.openAnim.setEasing(this.open ? Easing.Method.EASE_OUT_QUAD : Easing.Method.EASE_IN_QUAD);
                }
                for (Button childButton : this.buttons) {
                    if (childButton instanceof BooleanButton booleanButton) {
                        booleanButton.resetSlide();
                        continue;
                    }
                    if (!(childButton instanceof NumberButton numberButton)) continue;
                    numberButton.resetSlide();
                }
                this.playClickSound();
            }
        }
        if (this.isOpen()) {
            for (Button childButton : this.buttons) {
                if (!childButton.isVisible()) continue;
                childButton.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        this.updateChildBounds();
        if (this.isOpen()) {
            for (Button childButton : this.buttons) {
                if (!childButton.isVisible()) continue;
                childButton.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.updateChildBounds();
        if (this.isOpen()) {
            for (Button childButton : this.buttons) {
                if (!childButton.isVisible()) continue;
                childButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isOpen()) {
            for (Button childButton : this.buttons) {
                if (!childButton.isVisible()) continue;
                childButton.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char typedChar, int modifiers) {
        if (this.isOpen()) {
            for (Button childButton : this.buttons) {
                if (!childButton.isVisible()) continue;
                childButton.charTyped(typedChar, modifiers);
            }
        }
    }
}

