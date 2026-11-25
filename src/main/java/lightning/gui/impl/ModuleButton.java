/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.gui.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.gui.ColorGui;
import lightning.gui.api.Button;
import lightning.gui.api.ExpandableRow;
import lightning.gui.api.Frame;
import lightning.gui.impl.BindButton;
import lightning.gui.impl.BooleanButton;
import lightning.gui.impl.CategoryButton;
import lightning.gui.impl.ColorButton;
import lightning.gui.impl.ImageButton;
import lightning.gui.impl.ModeButton;
import lightning.gui.impl.NumberButton;
import lightning.gui.impl.StringButton;
import lightning.gui.impl.WhitelistButton;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ImageSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ModuleButton
extends Button
implements ExpandableRow {
    private static final int TEXT_INSET = 6;
    private static final int TEXT_SLIDE = 3;
    private static final int RIGHT_INSET = 5;
    private static final int PILL_W = 16;
    private static final int PILL_H = 8;
    private final Module module;
    private boolean open = false;
    private final ArrayList<Button> buttons = new ArrayList();
    private String searchQuery = "";
    private final Animation openAnim = new Animation(220, Easing.Method.EASE_OUT_CUBIC);
    private int revealHeight = 0;
    private final Animation fillAnim;

    @Override
    public float getOpenAmount() {
        return this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Override
    public String getRowName() {
        return this.module.getName();
    }

    public ModuleButton(Module module, Frame frame, int height) {
        super(frame, height, module.getDescription());
        this.module = module;
        float initial = module.isToggled() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(initial, initial, 200, Easing.Method.EASE_OUT_CUBIC);
        int childHeight = 13;
        List<Setting> settings = module.getSettings();
        for (int i = 0; i < settings.size(); ++i) {
            Setting setting = settings.get(i);
            if (setting instanceof BooleanSetting) {
                BooleanSetting boolSetting = (BooleanSetting)setting;
                if (boolSetting.getTag().equals("Enabled") && boolSetting.getVisibility() instanceof CategorySetting.Visibility) {
                    CategorySetting.Visibility catVis = (CategorySetting.Visibility)boolSetting.getVisibility();
                    if (i > 0 && settings.get(i - 1) == catVis.getValue()) continue;
                }
                this.buttons.add(new BooleanButton(boolSetting, frame, childHeight));
                continue;
            }
            if (setting instanceof NumberSetting) {
                this.buttons.add(new NumberButton((NumberSetting)setting, frame, childHeight));
                continue;
            }
            if (setting instanceof CategorySetting) {
                Setting.Visibility visibility;
                BooleanSetting booleanSetting;
                CategorySetting catSetting = (CategorySetting)setting;
                BooleanSetting enableSetting = null;
                if (i + 1 < settings.size() && settings.get(i + 1) instanceof BooleanSetting && (booleanSetting = (BooleanSetting)settings.get(i + 1)).getTag().equals("Enabled") && (visibility = booleanSetting.getVisibility()) instanceof CategorySetting.Visibility && ((CategorySetting.Visibility)visibility).getValue() == catSetting) {
                    enableSetting = booleanSetting;
                }
                this.buttons.add(new CategoryButton(catSetting, enableSetting, frame, childHeight));
                continue;
            }
            if (setting instanceof BindSetting) {
                this.buttons.add(new BindButton((BindSetting)setting, frame, childHeight));
                continue;
            }
            if (setting instanceof ModeSetting) {
                this.buttons.add(new ModeButton((ModeSetting)setting, frame, childHeight));
                continue;
            }
            if (setting instanceof WhitelistSetting) {
                this.buttons.add(new WhitelistButton((WhitelistSetting)setting, frame, childHeight));
                continue;
            }
            if (setting instanceof StringSetting) {
                this.buttons.add(new StringButton((StringSetting)setting, frame, childHeight));
                continue;
            }
            if (setting instanceof ImageSetting) {
                this.buttons.add(new ImageButton((ImageSetting)setting, frame, childHeight));
                continue;
            }
            if (!(setting instanceof ColorSetting)) continue;
            this.buttons.add(new ColorButton((ColorSetting)setting, frame, childHeight));
        }
    }

    public int getRequiredWidth() {
        int arrowWidth = 0;
        try {
            ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            if (clickGuiModule.triangles.getValue() && !this.buttons.isEmpty()) {
                arrowWidth = Night.FONT_MANAGER.getWidth("\u25bc") + 4;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        int nameWidth = Night.FONT_MANAGER.getWidth(this.module.getName());
        return this.getPadding() * 2 + 6 + 3 + nameWidth + 8 + 16 + arrowWidth + 5;
    }

    private static Color mixColor(Color color, Color color2, float t) {
        float clamped = Math.max(0.0f, Math.min(1.0f, t));
        int r = Math.round((float)color.getRed() + (float)(color2.getRed() - color.getRed()) * clamped);
        int g = Math.round((float)color.getGreen() + (float)(color2.getGreen() - color.getGreen()) * clamped);
        int b = Math.round((float)color.getBlue() + (float)(color2.getBlue() - color.getBlue()) * clamped);
        int a = Math.round((float)color.getAlpha() + (float)(color2.getAlpha() - color.getAlpha()) * clamped);
        return new Color(r, g, b, a);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        ColorGui.renderModule(this, guiGraphicsExtractor, mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.module.setToggled(!this.module.isToggled());
                this.playClickSound();
            } else if (button == 1) {
                this.open = !this.open;
                this.openAnim.setEasing(this.open ? Easing.Method.EASE_OUT_QUAD : Easing.Method.EASE_IN_QUAD);
                if (this.open) {
                    for (Button child : this.buttons) {
                        if (child instanceof BooleanButton) {
                            ((BooleanButton)child).resetSlide();
                            continue;
                        }
                        if (!(child instanceof NumberButton)) continue;
                        ((NumberButton)child).resetSlide();
                    }
                }
                this.playClickSound();
            }
        }
        if (this.open) {
            for (Button child : this.buttons) {
                if (!child.isVisible()) continue;
                child.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (Button child : this.buttons) {
            child.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Button child : this.buttons) {
            child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.open) {
            for (Button child : this.buttons) {
                if (!child.isVisible()) continue;
                child.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char ch, int modifiers) {
        if (this.open) {
            for (Button child : this.buttons) {
                if (!child.isVisible()) continue;
                child.charTyped(ch, modifiers);
            }
        }
    }

    public Module getModule() {
        return this.module;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    public ArrayList<Button> getButtons() {
        return this.buttons;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public Animation getOpenAnim() {
        return this.openAnim;
    }

    public int getRevealHeight() {
        return this.revealHeight;
    }

    public Animation getFillAnim() {
        return this.fillAnim;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    public void setRevealHeight(int revealHeight) {
        this.revealHeight = revealHeight;
    }
}

