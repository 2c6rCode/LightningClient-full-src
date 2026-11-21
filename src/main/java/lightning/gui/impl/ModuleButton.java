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

    public ModuleButton(Module module, Frame frame, int n) {
        super(frame, n, module.getDescription());
        this.module = module;
        float f = module.isToggled() ? 1.0f : 0.0f;
        this.fillAnim = new Animation(f, f, 200, Easing.Method.EASE_OUT_CUBIC);
        int n2 = 13;
        List<Setting> list = module.getSettings();
        for (int i = 0; i < list.size(); ++i) {
            Object object;
            Object object2;
            Setting setting;
            Setting setting2 = list.get(i);
            if (setting2 instanceof BooleanSetting) {
                setting = (BooleanSetting)setting2;
                if (setting.getTag().equals("Enabled") && (object2 = setting.getVisibility()) instanceof CategorySetting.Visibility) {
                    object = (CategorySetting.Visibility)object2;
                    if (i > 0 && list.get(i - 1) == ((CategorySetting.Visibility)object).getValue()) continue;
                }
                this.buttons.add(new BooleanButton((BooleanSetting)setting, frame, n2));
                continue;
            }
            if (setting2 instanceof NumberSetting) {
                this.buttons.add(new NumberButton((NumberSetting)setting2, frame, n2));
                continue;
            }
            if (setting2 instanceof CategorySetting) {
                Setting.Visibility visibility;
                BooleanSetting booleanSetting;
                setting = (CategorySetting)setting2;
                object2 = null;
                if (i + 1 < list.size() && (object = list.get(i + 1)) instanceof BooleanSetting && (booleanSetting = (BooleanSetting)object).getTag().equals("Enabled") && (visibility = booleanSetting.getVisibility()) instanceof CategorySetting.Visibility && ((CategorySetting.Visibility)visibility).getValue() == setting) {
                    object2 = booleanSetting;
                }
                this.buttons.add(new CategoryButton((CategorySetting)setting, (BooleanSetting)object2, frame, n2));
                continue;
            }
            if (setting2 instanceof BindSetting) {
                this.buttons.add(new BindButton((BindSetting)setting2, frame, n2));
                continue;
            }
            if (setting2 instanceof ModeSetting) {
                this.buttons.add(new ModeButton((ModeSetting)setting2, frame, n2));
                continue;
            }
            if (setting2 instanceof WhitelistSetting) {
                this.buttons.add(new WhitelistButton((WhitelistSetting)setting2, frame, n2));
                continue;
            }
            if (setting2 instanceof StringSetting) {
                this.buttons.add(new StringButton((StringSetting)setting2, frame, n2));
                continue;
            }
            if (setting2 instanceof ImageSetting) {
                this.buttons.add(new ImageButton((ImageSetting)setting2, frame, n2));
                continue;
            }
            if (!(setting2 instanceof ColorSetting)) continue;
            this.buttons.add(new ColorButton((ColorSetting)setting2, frame, n2));
        }
    }

    public int getRequiredWidth() {
        int n = 0;
        try {
            ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            if (clickGuiModule.triangles.getValue() && !this.buttons.isEmpty()) {
                n = Night.FONT_MANAGER.getWidth("\u25bc") + 4;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        int n2 = Night.FONT_MANAGER.getWidth(this.module.getName());
        return this.getPadding() * 2 + 6 + 3 + n2 + 8 + 16 + n + 5;
    }

    private static Color mixColor(Color color, Color color2, float f) {
        float f2 = Math.max(0.0f, Math.min(1.0f, f));
        int n = Math.round((float)color.getRed() + (float)(color2.getRed() - color.getRed()) * f2);
        int n2 = Math.round((float)color.getGreen() + (float)(color2.getGreen() - color.getGreen()) * f2);
        int n3 = Math.round((float)color.getBlue() + (float)(color2.getBlue() - color.getBlue()) * f2);
        int n4 = Math.round((float)color.getAlpha() + (float)(color2.getAlpha() - color.getAlpha()) * f2);
        return new Color(n, n2, n3, n4);
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        ColorGui.renderModule(this, guiGraphicsExtractor, n, n2, f);
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (this.isHovering(d, d2)) {
            if (n == 0) {
                this.module.setToggled(!this.module.isToggled());
                this.playClickSound();
            } else if (n == 1) {
                this.open = !this.open;
                this.openAnim.setEasing(this.open ? Easing.Method.EASE_OUT_QUAD : Easing.Method.EASE_IN_QUAD);
                if (this.open) {
                    for (Button button : this.buttons) {
                        if (button instanceof BooleanButton) {
                            ((BooleanButton)button).resetSlide();
                            continue;
                        }
                        if (!(button instanceof NumberButton)) continue;
                        ((NumberButton)button).resetSlide();
                    }
                }
                this.playClickSound();
            }
        }
        if (this.open) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.mouseClicked(d, d2, n);
            }
        }
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        for (Button button : this.buttons) {
            button.mouseReleased(d, d2, n);
        }
    }

    @Override
    public void mouseDragged(double d, double d2, int n, double d3, double d4) {
        for (Button button : this.buttons) {
            button.mouseDragged(d, d2, n, d3, d4);
        }
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (this.open) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.keyPressed(n, n2, n3);
            }
        }
    }

    @Override
    public void charTyped(char c, int n) {
        if (this.open) {
            for (Button button : this.buttons) {
                if (!button.isVisible()) continue;
                button.charTyped(c, n);
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

    public void setOpen(boolean bl) {
        this.open = bl;
    }

    @Override
    public void setSearchQuery(String string) {
        this.searchQuery = string;
    }

    @Override
    public void setRevealHeight(int n) {
        this.revealHeight = n;
    }
}

