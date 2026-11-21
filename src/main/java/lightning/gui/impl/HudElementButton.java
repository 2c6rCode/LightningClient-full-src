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
    public void setRevealHeight(int n) {
        this.revealHeight = n;
    }

    @Override
    public void setSearchQuery(String string) {
        this.searchQuery = string;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public int getRevealHeight() {
        return this.revealHeight;
    }

    public HudElementButton(HudElementRegistry.Element element, List<Setting> list, Frame frame, int n) {
        super(frame, n, element.name());
        this.element = element;
        float f = element.enabled().getValue() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(f, f, 180, Easing.Method.EASE_OUT_QUAD);
        for (Setting setting : list) {
            Setting setting2;
            if (setting instanceof BooleanSetting) {
                setting2 = (BooleanSetting)setting;
                this.buttons.add(new BooleanButton((BooleanSetting)setting2, frame, n));
                continue;
            }
            if (setting instanceof NumberSetting) {
                setting2 = (NumberSetting)setting;
                this.buttons.add(new NumberButton((NumberSetting)setting2, frame, n));
                continue;
            }
            if (setting instanceof CategorySetting) {
                setting2 = (CategorySetting)setting;
                this.buttons.add(new CategoryButton((CategorySetting)setting2, null, frame, n));
                continue;
            }
            if (setting instanceof BindSetting) {
                setting2 = (BindSetting)setting;
                this.buttons.add(new BindButton((BindSetting)setting2, frame, n));
                continue;
            }
            if (setting instanceof ModeSetting) {
                setting2 = (ModeSetting)setting;
                this.buttons.add(new ModeButton((ModeSetting)setting2, frame, n));
                continue;
            }
            if (setting instanceof WhitelistSetting) {
                setting2 = (WhitelistSetting)setting;
                this.buttons.add(new WhitelistButton((WhitelistSetting)setting2, frame, n));
                continue;
            }
            if (setting instanceof StringSetting) {
                setting2 = (StringSetting)setting;
                this.buttons.add(new StringButton((StringSetting)setting2, frame, n));
                continue;
            }
            if (!(setting instanceof ColorSetting)) continue;
            setting2 = (ColorSetting)setting;
            this.buttons.add(new ColorButton((ColorSetting)setting2, frame, n));
        }
    }

    private void updateChildBounds() {
        int n = this.getY() + this.getHeight();
        for (Button button : this.buttons) {
            button.setX(this.getX());
            button.setY(n);
            if (!button.isVisible()) continue;
            n += button.getHeight();
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        int n3;
        int n4;
        Object object;
        float f2;
        this.updateChildBounds();
        if (this.isHovering(n, n2) && Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
            Night.CLICK_GUI.getDescriptionFrame().setDescription(this.getDescription());
        }
        Color color = this.isHovering(n, n2) ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
        Color color2 = color;
        if (color.getAlpha() > 0) {
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY(), this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight() - 1, color);
        }
        float f3 = this.fillAnim.get(this.element.enabled().getValue() ? 1.0f : 0.0f);
        if (f3 > 0.001f) {
            object = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            n4 = !((ClickGuiModule)object).fillMode.getValue().equalsIgnoreCase("Default") ? 1 : 0;
            n3 = this.getX() + this.getPadding() + Math.round((float)(this.getWidth() - this.getPadding() * 2) * f3);
            if (n4 != 0) {
                int n5 = Math.round(((ClickGuiModule)object).neekeriOpacity.getValue().floatValue() / 100.0f * 255.0f);
                guiGraphicsExtractor.enableScissor(this.getX() + this.getPadding(), this.getY(), n3, this.getY() + this.getHeight() - 1);
                NeekeriFill.fill(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY(), this.getWidth() - this.getPadding() * 2, this.getHeight() - 1, n5);
                guiGraphicsExtractor.disableScissor();
            } else {
                Color color3 = ColorUtils.getColor(((ClickGuiModule)object).color.getColor(), 90);
                Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY(), n3, this.getY() + this.getHeight() - 1, color3);
            }
        }
        object = ClickGuiScreen.getButtonColor(this.getY(), 60);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX() + this.getPadding(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth() - this.getPadding(), this.getY() + this.getHeight(), (Color)object);
        n4 = this.getX() + this.getTextPadding() + (this.element.enabled().getValue() ? 1 : 0);
        n3 = this.getY() + 2;
        String string = this.element.enabled().getValue() ? "" : ChatFormatting.GRAY.toString();
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string + this.element.name(), n4, n3, Color.WHITE);
        if (!this.buttons.isEmpty()) {
            String string2 = this.isOpen() ? "..." : "+";
            int n6 = this.getX() + this.getWidth() - this.getTextPadding() - Night.FONT_MANAGER.getWidth(string2) - 1;
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, n6, this.getY() + 2, Color.WHITE);
        }
        if (this.revealHeight > 0) {
            int n7 = this.getY() + this.getHeight();
            guiGraphicsExtractor.enableScissor(this.getX(), n7, this.getX() + this.getWidth(), n7 + this.revealHeight);
            for (Button button : this.buttons) {
                if (!button.isVisible()) {
                    Button button2;
                    if (button instanceof BooleanButton) {
                        button2 = (BooleanButton)button;
                        ((BooleanButton)button2).setPrevVisible(false);
                        continue;
                    }
                    if (!(button instanceof NumberButton)) continue;
                    button2 = (NumberButton)button;
                    ((NumberButton)button2).setPrevVisible(false);
                    continue;
                }
                button.render(guiGraphicsExtractor, n, n2, f);
                if (!button.isHovering(n, n2) || !Night.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) continue;
                Night.CLICK_GUI.getDescriptionFrame().setDescription(button.getDescription());
            }
            guiGraphicsExtractor.disableScissor();
        } else {
            for (Button button : this.buttons) {
                Button button3;
                if (button instanceof BooleanButton) {
                    button3 = (BooleanButton)button;
                    ((BooleanButton)button3).setPrevVisible(false);
                    continue;
                }
                if (!(button instanceof NumberButton)) continue;
                button3 = (NumberButton)button;
                ((NumberButton)button3).setPrevVisible(false);
            }
        }
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        this.updateChildBounds();
        if (this.isHovering(d, d2)) {
            if (n == 0) {
                this.element.enabled().setValue(!this.element.enabled().getValue());
                this.playClickSound();
            } else if (n == 1) {
                if (this.element.category() != null) {
                    this.element.category().setOpen(!this.element.category().isOpen());
                } else {
                    this.open = !this.open;
                    this.openAnim.setEasing(this.open ? Easing.Method.EASE_OUT_QUAD : Easing.Method.EASE_IN_QUAD);
                }
                for (Button button : this.buttons) {
                    Button button2;
                    if (button instanceof BooleanButton) {
                        button2 = (BooleanButton)button;
                        ((BooleanButton)button2).resetSlide();
                        continue;
                    }
                    if (!(button instanceof NumberButton)) continue;
                    button2 = (NumberButton)button;
                    ((NumberButton)button2).resetSlide();
                }
                this.playClickSound();
            }
        }
        if (this.isOpen()) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.mouseClicked(d, d2, n);
            }
        }
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        this.updateChildBounds();
        if (this.isOpen()) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.mouseReleased(d, d2, n);
            }
        }
    }

    @Override
    public void mouseDragged(double d, double d2, int n, double d3, double d4) {
        this.updateChildBounds();
        if (this.isOpen()) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.mouseDragged(d, d2, n, d3, d4);
            }
        }
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (this.isOpen()) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.keyPressed(n, n2, n3);
            }
        }
    }

    @Override
    public void charTyped(char c, int n) {
        if (this.isOpen()) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.charTyped(c, n);
            }
        }
    }
}

