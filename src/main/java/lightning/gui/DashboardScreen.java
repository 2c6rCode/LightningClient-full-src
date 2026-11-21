/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 */
package lightning.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.gui.api.GuiDescriptionTranslations;
import lightning.managers.FontManager;
import lightning.modules.Module;
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
import lightning.utils.graphics.Renderer2D;
import lightning.utils.input.KeyboardUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class DashboardScreen
extends ClickGuiScreen {
    private static final int W = 460;
    private static final int H = 280;
    private static final int SB = 92;
    private static final int ST = 132;
    private static final int PAD = 10;
    private static final int CW = 104;
    private static final int CH = 40;
    private static final int GAP = 8;
    private static final int TOP = 30;
    private static final int BAR = 28;
    private static final Module.Category[] CATS = new Module.Category[]{Module.Category.COMBAT, Module.Category.MOVEMENT, Module.Category.VISUALS, Module.Category.PLAYER, Module.Category.MISCELLANEOUS};
    private static final Color WINDOW = new Color(18, 18, 18, 245);
    private static final Color SIDE = new Color(14, 14, 14, 245);
    private static final Color CARD = new Color(18, 26, 46);
    private static final Color CARD_SEL = new Color(20, 32, 58);
    private static final Color LINE = new Color(31, 42, 68);
    private static final Color LINE_HI = new Color(52, 67, 107);
    private static final Color CYAN = new Color(92, 225, 255);
    private static final Color TEXT = new Color(230, 236, 255);
    private static final Color DIM = new Color(123, 136, 168);
    private static final Color OFF = new Color(38, 48, 77);
    private static final Color DARK = new Color(13, 18, 32);
    private static final Color VEIL = new Color(5, 5, 5, 150);
    private static final Color[] PALETTE = new Color[]{new Color(255, 75, 75), new Color(255, 150, 60), new Color(255, 220, 70), new Color(80, 220, 120), new Color(92, 225, 255), new Color(80, 130, 255), new Color(170, 100, 255), new Color(255, 110, 200), new Color(240, 240, 255)};
    private final List<Row> rows = new ArrayList<Row>();
    private final Map<Module, float[]> anim = new HashMap<Module, float[]>();
    private int x;
    private int y;
    private int px;
    private int pw;
    private int cat;
    private float scroll;
    private float sScroll;
    private Module selected;
    private NumberSetting dragging;
    private String query = "";
    private boolean searchFocus;
    private StringSetting editStr;
    private BindSetting listenBind;
    private NumberSetting editNum;
    private String numBuf = "";
    private String hoverDesc = "";

    private static boolean in(double d, double d2, int n, int n2, int n3, int n4) {
        return d >= (double)n && d < (double)(n + n3) && d2 >= (double)n2 && d2 < (double)(n2 + n4);
    }

    private static float approach(float f, float f2, float f3) {
        float f4 = f2 - f;
        return Math.abs(f4) < 0.01f ? f2 : f + f4 * f3;
    }

    private static Color mix(Color color, Color color2, float f) {
        return new Color((int)((float)color.getRed() + (float)(color2.getRed() - color.getRed()) * f), (int)((float)color.getGreen() + (float)(color2.getGreen() - color.getGreen()) * f), (int)((float)color.getBlue() + (float)(color2.getBlue() - color.getBlue()) * f));
    }

    private static boolean vis(Setting setting) {
        Setting.Visibility visibility = setting.getVisibility();
        return visibility == null || visibility.isVisible();
    }

    private static void rr(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, Color color) {
        LightningSkin.rrAll(guiGraphicsExtractor, f, f2, f + f3, f2 + f4, f5, color);
    }

    private void box(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3, int n4, float f, Color color, Color color2) {
        DashboardScreen.rr(guiGraphicsExtractor, n, n2, n3, n4, f, color);
        DashboardScreen.rr(guiGraphicsExtractor, n + 1, n2 + 1, n3 - 2, n4 - 2, Math.max(0.5f, f - 1.0f), color2);
    }

    private void text(GuiGraphicsExtractor guiGraphicsExtractor, String string, int n, int n2, Color color) {
        Night.FONT_MANAGER.drawText(guiGraphicsExtractor, string, n, n2, color);
    }

    private int tw(String string) {
        return Night.FONT_MANAGER.getWidth(string);
    }

    private String fit(String string, int n) {
        if (string == null) {
            return "";
        }
        FontManager fontManager = Night.FONT_MANAGER;
        if (fontManager.getWidth(string) <= n) {
            return string;
        }
        while (string.length() > 1 && fontManager.getWidth(string + "..") > n) {
            string = string.substring(0, string.length() - 1);
        }
        return string + "..";
    }

    private void toggle(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        DashboardScreen.rr(guiGraphicsExtractor, n, n2, 24.0f, 12.0f, 6.0f, DashboardScreen.mix(OFF, CYAN, f));
        DashboardScreen.rr(guiGraphicsExtractor, (float)(n + 2) + 12.0f * f, n2 + 2, 8.0f, 8.0f, 4.0f, Color.WHITE);
    }

    private List<Module> mods() {
        return Night.MODULE_MANAGER.getModules(CATS[this.cat]);
    }

    private List<Module> list() {
        if (this.query.isEmpty()) {
            return this.mods();
        }
        String string = this.query.toLowerCase(Locale.ROOT);
        ArrayList<Module> arrayList = new ArrayList<Module>();
        for (Module.Category category : CATS) {
            for (Module module : Night.MODULE_MANAGER.getModules(category)) {
                if (!module.getName().toLowerCase(Locale.ROOT).contains(string)) continue;
                arrayList.add(module);
            }
        }
        return arrayList;
    }

    private int cardX(int n) {
        return this.x + 92 + 10 + n % 2 * 112;
    }

    private int cardY(int n) {
        return this.y + 30 + n / 2 * 48 - (int)this.scroll;
    }

    private int cardsH() {
        return 206;
    }

    private int searchX() {
        return this.x + 92 + 10 + 208 + 8 - 104;
    }

    private static String desc(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        String string2 = GuiDescriptionTranslations.translate(string);
        return string2 == null || string2.isEmpty() ? string : string2;
    }

    private List<String> wrap(String string, int n, int n2) {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (string == null || string.isEmpty()) {
            return arrayList;
        }
        String string2 = "";
        for (String string3 : string.split(" ")) {
            String string4;
            String string5 = string4 = string2.isEmpty() ? string3 : string2 + " " + string3;
            if (this.tw(string4) <= n) {
                string2 = string4;
                continue;
            }
            if (!string2.isEmpty()) {
                arrayList.add(string2);
            }
            string2 = string3;
            if (arrayList.size() == n2) break;
        }
        if (arrayList.size() < n2 && !string2.isEmpty()) {
            arrayList.add(string2);
        }
        if (!arrayList.isEmpty() && String.join((CharSequence)" ", arrayList).length() < string.length()) {
            int n3 = arrayList.size() - 1;
            String string6 = (String)arrayList.get(n3);
            while (string6.length() > 1 && this.tw(string6 + "..") > n) {
                string6 = string6.substring(0, string6.length() - 1);
            }
            arrayList.set(n3, string6 + "..");
        }
        return arrayList;
    }

    private static void line(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, float f4, float f5, Color color) {
        int n = Math.max(1, (int)Math.ceil(Math.max(Math.abs(f3 - f), Math.abs(f4 - f2)) * 2.0f));
        for (int i = 0; i <= n; ++i) {
            float f6 = (float)i / (float)n;
            float f7 = f + (f3 - f) * f6;
            float f8 = f2 + (f4 - f2) * f6;
            Renderer2D.renderQuad(guiGraphicsExtractor, f7 - f5 / 2.0f, f8 - f5 / 2.0f, f7 + f5 / 2.0f, f8 + f5 / 2.0f, color);
        }
    }

    private static void bolt(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, float f3, Color color) {
        float f4 = f3 / 10.0f;
        DashboardScreen.line(guiGraphicsExtractor, f + 7.0f * f4, f2, f + 2.5f * f4, f2 + 6.0f * f4, 1.8f * f4, color);
        DashboardScreen.line(guiGraphicsExtractor, f + 2.5f * f4, f2 + 6.0f * f4, f + 7.0f * f4, f2 + 6.0f * f4, 1.8f * f4, color);
        DashboardScreen.line(guiGraphicsExtractor, f + 7.0f * f4, f2 + 6.0f * f4, f + 3.0f * f4, f2 + 10.0f * f4, 1.8f * f4, color);
    }

    private static void searchIcon(GuiGraphicsExtractor guiGraphicsExtractor, float f, float f2, Color color, Color color2) {
        DashboardScreen.rr(guiGraphicsExtractor, f, f2, 7.0f, 7.0f, 3.5f, color);
        DashboardScreen.rr(guiGraphicsExtractor, f + 1.4f, f2 + 1.4f, 4.2f, 4.2f, 2.1f, color2);
        DashboardScreen.line(guiGraphicsExtractor, f + 6.0f, f2 + 6.0f, f + 9.5f, f2 + 9.5f, 1.6f, color);
    }

    private static void catIcon(GuiGraphicsExtractor guiGraphicsExtractor, int n, float f, float f2, Color color, Color color2) {
        switch (n) {
            case 0: {
                DashboardScreen.line(guiGraphicsExtractor, f + 1.0f, f2 + 1.0f, f + 9.0f, f2 + 9.0f, 1.6f, color);
                DashboardScreen.line(guiGraphicsExtractor, f + 9.0f, f2 + 1.0f, f + 1.0f, f2 + 9.0f, 1.6f, color);
                DashboardScreen.line(guiGraphicsExtractor, f + 1.0f, f2 + 4.0f, f + 4.0f, f2 + 1.0f, 1.4f, color);
                DashboardScreen.line(guiGraphicsExtractor, f + 9.0f, f2 + 4.0f, f + 6.0f, f2 + 1.0f, 1.4f, color);
                break;
            }
            case 1: {
                DashboardScreen.line(guiGraphicsExtractor, f + 1.0f, f2 + 1.0f, f + 4.5f, f2 + 5.0f, 1.6f, color);
                DashboardScreen.line(guiGraphicsExtractor, f + 4.5f, f2 + 5.0f, f + 1.0f, f2 + 9.0f, 1.6f, color);
                DashboardScreen.line(guiGraphicsExtractor, f + 5.5f, f2 + 1.0f, f + 9.0f, f2 + 5.0f, 1.6f, color);
                DashboardScreen.line(guiGraphicsExtractor, f + 9.0f, f2 + 5.0f, f + 5.5f, f2 + 9.0f, 1.6f, color);
                break;
            }
            case 2: {
                DashboardScreen.rr(guiGraphicsExtractor, f, f2 + 1.5f, 10.0f, 7.0f, 3.5f, color);
                DashboardScreen.rr(guiGraphicsExtractor, f + 1.2f, f2 + 2.7f, 7.6f, 4.6f, 2.3f, color2);
                DashboardScreen.rr(guiGraphicsExtractor, f + 3.5f, f2 + 3.5f, 3.0f, 3.0f, 1.5f, color);
                break;
            }
            case 3: {
                DashboardScreen.rr(guiGraphicsExtractor, f + 3.0f, f2, 4.0f, 4.0f, 2.0f, color);
                DashboardScreen.rr(guiGraphicsExtractor, f + 0.5f, f2 + 5.0f, 9.0f, 5.0f, 2.5f, color);
                break;
            }
            default: {
                DashboardScreen.rr(guiGraphicsExtractor, f, f2 + 1.5f, 10.0f, 1.3f, 0.6f, color);
                DashboardScreen.rr(guiGraphicsExtractor, f, f2 + 4.4f, 10.0f, 1.3f, 0.6f, color);
                DashboardScreen.rr(guiGraphicsExtractor, f, f2 + 7.3f, 10.0f, 1.3f, 0.6f, color);
                DashboardScreen.rr(guiGraphicsExtractor, f + 6.0f, f2, 2.6f, 4.2f, 1.2f, color);
                DashboardScreen.rr(guiGraphicsExtractor, f + 2.0f, f2 + 3.0f, 2.6f, 4.2f, 1.2f, color);
                DashboardScreen.rr(guiGraphicsExtractor, f + 5.5f, f2 + 5.9f, 2.6f, 4.2f, 1.2f, color);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, VEIL);
    }

    private static int rowHeight(Setting setting) {
        if (setting instanceof NumberSetting) {
            return 26;
        }
        if (setting instanceof BooleanSetting) {
            return 16;
        }
        if (setting instanceof ModeSetting) {
            return 24;
        }
        if (setting instanceof CategorySetting) {
            return 18;
        }
        if (setting instanceof BindSetting) {
            return 16;
        }
        if (setting instanceof ColorSetting) {
            return 36;
        }
        if (setting instanceof StringSetting) {
            return 26;
        }
        if (setting instanceof WhitelistSetting) {
            return 16;
        }
        return 0;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        String string;
        int n3;
        int n4;
        Object object;
        int n5;
        int n6;
        Object object2;
        int n7;
        int n8;
        int n9;
        if (this.isClosing()) {
            ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            if (clickGuiModule != null && clickGuiModule.isToggled()) {
                clickGuiModule.setToggled(false);
            }
            this.setReadyToRemove(true);
            this.minecraft.gui.setScreen(null);
            return;
        }
        this.x = (this.width - 460) / 2;
        this.y = (this.height - 280) / 2;
        this.px = this.x + 460 - 132 + 10;
        this.pw = 112;
        this.hoverDesc = "";
        List<Module> list = this.list();
        if (this.selected == null || this.query.isEmpty() && this.selected.getCategory() != CATS[this.cat] || !list.contains(this.selected)) {
            this.selected = list.isEmpty() ? null : list.get(0);
        }
        this.box(guiGraphicsExtractor, this.x, this.y, 460, 280, 8.0f, LINE, WINDOW);
        DashboardScreen.rr(guiGraphicsExtractor, this.x + 1, this.y + 1, 91.0f, 278.0f, 7.0f, SIDE);
        DashboardScreen.rr(guiGraphicsExtractor, this.x + 460 - 132, this.y + 1, 131.0f, 278.0f, 7.0f, SIDE);
        DashboardScreen.bolt(guiGraphicsExtractor, this.x + 11, this.y + 10, 11.0f, CYAN);
        this.text(guiGraphicsExtractor, "Lightning", this.x + 27, this.y + 12, CYAN);
        for (n9 = 0; n9 < CATS.length; ++n9) {
            n8 = this.y + 34 + n9 * 24;
            boolean bl = n9 == this.cat && this.query.isEmpty();
            boolean isHoverCat = DashboardScreen.in(n, n2, this.x + 6, n8, 80, 20);
            Color color = SIDE;
            if (bl) {
                DashboardScreen.rr(guiGraphicsExtractor, this.x + 6, n8, 80.0f, 20.0f, 5.0f, CARD_SEL);
                DashboardScreen.rr(guiGraphicsExtractor, this.x + 6, n8 + 3, 2.0f, 14.0f, 1.0f, CYAN);
                color = CARD_SEL;
            } else if (isHoverCat) {
                DashboardScreen.rr(guiGraphicsExtractor, this.x + 6, n8, 80.0f, 20.0f, 5.0f, CARD);
                color = CARD;
            }
            DashboardScreen.catIcon(guiGraphicsExtractor, n9, this.x + 14, n8 + 5, bl ? CYAN : DIM, color);
            this.text(guiGraphicsExtractor, this.fit(CATS[n9].getName(), 34), this.x + 30, n8 + 6, bl ? TEXT : DIM);
            object2 = String.valueOf(Night.MODULE_MANAGER.getModules(CATS[n9]).stream().filter(Module::isToggled).count());
            this.text(guiGraphicsExtractor, (String)object2, this.x + 92 - 10 - this.tw((String)object2), n8 + 6, bl ? CYAN : DIM);
        }
        this.text(guiGraphicsExtractor, this.fit(this.query.isEmpty() ? CATS[this.cat].getName() : "Search", 100), this.x + 92 + 10, this.y + 12, TEXT);
        n9 = this.searchX();
        n8 = this.y + 7;
        this.box(guiGraphicsExtractor, n9, n8, 104, 16, 5.0f, this.searchFocus ? CYAN : LINE, CARD);
        DashboardScreen.searchIcon(guiGraphicsExtractor, n9 + 5, n8 + 3, this.searchFocus ? CYAN : DIM, CARD);
        if (this.query.isEmpty() && !this.searchFocus) {
            this.text(guiGraphicsExtractor, "Search", n9 + 20, n8 + 4, DIM);
        } else {
            String string2 = this.query;
            while (string2.length() > 1 && this.tw(string2) > 74) {
                string2 = string2.substring(1);
            }
            n7 = this.searchFocus && System.currentTimeMillis() / 500L % 2L == 0L ? 1 : 0;
            this.text(guiGraphicsExtractor, string2 + (n7 != 0 ? "|" : ""), n9 + 20, n8 + 4, TEXT);
        }
        int n10 = this.cardsH();
        n7 = Math.max(0, (list.size() + 1) / 2 * 48 - n10);
        this.scroll = Math.max(0.0f, Math.min(this.scroll, (float)n7));
        guiGraphicsExtractor.enableScissor(this.x + 92, this.y + 30, this.x + 460 - 132, this.y + 30 + n10);
        for (n6 = 0; n6 < list.size(); ++n6) {
            object2 = list.get(n6);
            int n11 = this.cardX(n6);
            n5 = this.cardY(n6);
            float[] animVals = this.anim.computeIfAbsent((Module)object2, module -> new float[2]);
            n4 = DashboardScreen.in(n, n2, n11, n5, 104, 40) && DashboardScreen.in(n, n2, this.x + 92, this.y + 30, 236, n10) ? 1 : 0;
            animVals[0] = DashboardScreen.approach(animVals[0], ((Module)object2).isToggled() ? 1.0f : 0.0f, 0.25f);
            animVals[1] = DashboardScreen.approach(animVals[1], n4 != 0 ? 1.0f : 0.0f, 0.3f);
            if (n4 != 0) {
                this.hoverDesc = ((Module)object2).getDescription();
            }
            this.box(guiGraphicsExtractor, n11, n5, 104, 40, 6.0f, object2 == this.selected ? CYAN : DashboardScreen.mix(LINE, LINE_HI, animVals[1]), object2 == this.selected ? CARD_SEL : CARD);
            this.text(guiGraphicsExtractor, this.fit(((Module)object2).getName(), 58), n11 + 8, n5 + 7, DashboardScreen.mix(DIM, TEXT, Math.max(animVals[0], animVals[1])));
            this.toggle(guiGraphicsExtractor, n11 + 104 - 32, n5 + 6, animVals[0]);
            this.text(guiGraphicsExtractor, this.fit(DashboardScreen.desc(((Module)object2).getDescription()), 88), n11 + 8, n5 + 24, DIM);
        }
        guiGraphicsExtractor.disableScissor();
        this.rows.clear();
        n6 = this.y + 32;
        int n12 = this.y + 280 - 10;
        if (this.selected != null) {
            this.text(guiGraphicsExtractor, this.fit(this.selected.getName(), this.pw), this.px, this.y + 12, CYAN);
            ArrayList<Setting> arrayList = new ArrayList<Setting>();
            n5 = 0;
            for (Setting setting : this.selected.getSettings()) {
                if (!DashboardScreen.vis(setting) || (n3 = DashboardScreen.rowHeight(setting)) == 0) continue;
                arrayList.add(setting);
                n5 += n3 + 6;
            }
            float f2 = Math.max(0, n5 - (n12 - n6 - 2));
            this.sScroll = Math.max(0.0f, Math.min(this.sScroll, f2));
            guiGraphicsExtractor.enableScissor(this.px - 4, n6, this.x + 460 - 2, n12);
            n4 = n6 + 2 - (int)this.sScroll;
            n3 = DashboardScreen.in(n, n2, this.px - 4, n6, this.pw + 8, n12 - n6) ? 1 : 0;
            for (Setting object3 : arrayList) {
                int n11 = DashboardScreen.rowHeight(object3);
                this.rows.add(new Row(object3, n4, n11));
                if (n3 != 0 && DashboardScreen.in(n, n2, this.px - 2, n4, this.pw + 4, n11)) {
                    this.hoverDesc = object3.getDescription();
                }
                this.drawSetting(guiGraphicsExtractor, object3, n4, n, n2);
                n4 += n11 + 6;
            }
            guiGraphicsExtractor.disableScissor();
        }
        if (((string = this.hoverDesc) == null || string.isEmpty()) && this.selected != null) {
            string = this.selected.getDescription();
        }
        n5 = this.x + 92 + 10;
        int n14 = this.y + 280 - 10 - 28 + 2;
        n4 = 216;
        this.box(guiGraphicsExtractor, n5, n14, n4, 26, 5.0f, LINE, CARD);
        n3 = n14 + 5;
        for (String string2 : this.wrap(DashboardScreen.desc(string), n4 - 14, 2)) {
            this.text(guiGraphicsExtractor, string2, n5 + 7, n3, DIM);
            n3 += 10;
        }
    }

    private void drawSetting(GuiGraphicsExtractor guiGraphicsExtractor, Setting setting, int n, int n2, int n3) {
        if (setting instanceof CategorySetting) {
            CategorySetting categorySetting = (CategorySetting)setting;
            boolean bl = categorySetting.isOpen();
            DashboardScreen.rr(guiGraphicsExtractor, this.px, n, this.pw, 16.0f, 4.0f, CARD);
            this.text(guiGraphicsExtractor, this.fit(categorySetting.getName(), this.pw - 24), this.px + 6, n + 4, bl ? CYAN : TEXT);
            this.text(guiGraphicsExtractor, bl ? "-" : "+", this.px + this.pw - 12, n + 4, bl ? CYAN : DIM);
            return;
        }
        this.text(guiGraphicsExtractor, this.fit(setting.getName(), this.pw - 40), this.px, n, DIM);
        if (setting instanceof NumberSetting) {
            String string;
            NumberSetting numberSetting = (NumberSetting)setting;
            Number number = numberSetting.getValue();
            String string2 = string = number instanceof Integer ? number.toString() : String.valueOf((double)Math.round(number.doubleValue() * 100.0) / 100.0);
            if (this.editNum == numberSetting) {
                boolean bl = System.currentTimeMillis() / 500L % 2L == 0L;
                String string3 = this.numBuf + (bl ? "|" : "");
                int n4 = Math.max(34, this.tw(string3) + 10);
                this.box(guiGraphicsExtractor, this.px + this.pw - n4, n - 3, n4, 14, 4.0f, CYAN, CARD);
                this.text(guiGraphicsExtractor, string3, this.px + this.pw - n4 + 5, n, CYAN);
            } else {
                this.text(guiGraphicsExtractor, string, this.px + this.pw - this.tw(string), n, TEXT);
            }
            double d = numberSetting.getMinimum().doubleValue();
            double d2 = numberSetting.getMaximum().doubleValue();
            float f = (float)((number.doubleValue() - d) / Math.max(1.0E-4, d2 - d));
            DashboardScreen.rr(guiGraphicsExtractor, this.px, n + 15, this.pw, 4.0f, 2.0f, OFF);
            DashboardScreen.rr(guiGraphicsExtractor, this.px, n + 15, Math.max(4.0f, (float)this.pw * f), 4.0f, 2.0f, CYAN);
        } else if (setting instanceof BooleanSetting) {
            BooleanSetting booleanSetting = (BooleanSetting)setting;
            DashboardScreen.rr(guiGraphicsExtractor, this.px + this.pw - 14, n - 1, 14.0f, 14.0f, 4.0f, booleanSetting.getValue() ? CYAN : OFF);
            if (booleanSetting.getValue()) {
                DashboardScreen.rr(guiGraphicsExtractor, this.px + this.pw - 10, n + 3, 6.0f, 6.0f, 2.0f, DARK);
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting modeSetting = (ModeSetting)setting;
            this.box(guiGraphicsExtractor, this.px, n + 10, this.pw, 14, 4.0f, LINE, CARD);
            this.text(guiGraphicsExtractor, this.fit(modeSetting.getValue(), this.pw - 10), this.px + 5, n + 13, TEXT);
        } else if (setting instanceof BindSetting) {
            Object object;
            boolean bl;
            BindSetting bindSetting = (BindSetting)setting;
            boolean bl2 = bl = this.listenBind == bindSetting;
            if (bl) {
                object = "...";
            } else {
                object = bindSetting.getValue() == 0 ? "None" : KeyboardUtils.getKeyName(bindSetting.getValue());
                String string = bindSetting.getMode();
                if (string != null && !string.equals("Bind")) {
                    object = (String)object + " (" + string + ")";
                }
            }
            object = this.fit((String)object, 62);
            int n5 = Math.max(30, this.tw((String)object) + 12);
            this.box(guiGraphicsExtractor, this.px + this.pw - n5, n - 2, n5, 15, 4.0f, bl ? CYAN : LINE, CARD);
            this.text(guiGraphicsExtractor, (String)object, this.px + this.pw - n5 + 6, n + 1, bl ? CYAN : TEXT);
        } else if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting)setting;
            Color color = colorSetting.getColor();
            DashboardScreen.rr(guiGraphicsExtractor, this.px + this.pw - 14, n - 1, 14.0f, 11.0f, 3.0f, colorSetting.isRainbow() ? CYAN : LINE_HI);
            DashboardScreen.rr(guiGraphicsExtractor, this.px + this.pw - 13, n, 12.0f, 9.0f, 2.5f, colorSetting.isRainbow() ? new Color(255, 120, 200) : new Color(color.getRed(), color.getGreen(), color.getBlue()));
            for (int i = 0; i <= PALETTE.length; ++i) {
                boolean bl;
                Color color2;
                int n6 = this.px + i * 11;
                boolean bl3 = i == PALETTE.length;
                Color color3 = color2 = bl3 ? new Color(255, 120, 200) : PALETTE[i];
                bl = bl3 ? colorSetting.isRainbow() : (!colorSetting.isRainbow() && color2.getRGB() == (color.getRGB() | 0xFF000000));
                if (bl) {
                    DashboardScreen.rr(guiGraphicsExtractor, n6 - 1, n + 13, 11.0f, 11.0f, 3.0f, TEXT);
                }
                DashboardScreen.rr(guiGraphicsExtractor, n6, n + 14, 9.0f, 9.0f, 2.5f, color2);
                if (!bl3) continue;
                DashboardScreen.rr(guiGraphicsExtractor, (float)n6 + 2.5f, (float)n + 16.5f, 4.0f, 4.0f, 2.0f, new Color(92, 225, 255));
            }
        } else if (setting instanceof StringSetting) {
            String string;
            StringSetting stringSetting = (StringSetting)setting;
            boolean bl = this.editStr == stringSetting;
            this.box(guiGraphicsExtractor, this.px, n + 10, this.pw, 14, 4.0f, bl ? CYAN : LINE, CARD);
            String string4 = string = stringSetting.getValue() == null ? "" : stringSetting.getValue();
            while (string.length() > 1 && this.tw(string) > this.pw - 14) {
                string = string.substring(1);
            }
            boolean bl5 = bl && System.currentTimeMillis() / 500L % 2L == 0L;
            this.text(guiGraphicsExtractor, string + (bl5 ? "|" : ""), this.px + 5, n + 13, TEXT);
        } else if (setting instanceof WhitelistSetting) {
            WhitelistSetting whitelistSetting = (WhitelistSetting)setting;
            String string = whitelistSetting.getWhitelist().size() + " items";
            this.text(guiGraphicsExtractor, string, this.px + this.pw - this.tw(string), n, TEXT);
        }
    }

    private void setNum(NumberSetting numberSetting, double d) {
        double d2 = numberSetting.getMinimum().doubleValue();
        double d3 = numberSetting.getMaximum().doubleValue();
        double d4 = numberSetting.getStep().doubleValue();
        double d5 = d2 + Math.max(0.0, Math.min(1.0, (d - (double)this.px) / (double)this.pw)) * (d3 - d2);
        if (d4 > 0.0) {
            d5 = d2 + (double)Math.round((d5 - d2) / d4) * d4;
        }
        this.applyNum(numberSetting, d5);
    }

    private void applyNum(NumberSetting numberSetting, double d) {
        double d2 = numberSetting.getMinimum().doubleValue();
        double d3 = numberSetting.getMaximum().doubleValue();
        d = Math.max(d2, Math.min(d3, d));
        Number number = numberSetting.getValue();
        numberSetting.setValue(number instanceof Integer ? (Number)((int)Math.round(d)) : (Number)(number instanceof Float ? (Number)Float.valueOf((float)d) : (Number)d));
    }

    private void resetInput() {
        this.searchFocus = false;
        this.editStr = null;
        this.listenBind = null;
        this.editNum = null;
        this.numBuf = "";
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double d = mouseButtonEvent.x();
        double d2 = mouseButtonEvent.y();
        int n = mouseButtonEvent.button();
        if (n != 0 && n != 1) {
            return true;
        }
        this.resetInput();
        if (n == 1) {
            if (DashboardScreen.in(d, d2, this.px - 4, this.y + 32, this.pw + 8, 238)) {
                for (Row row : this.rows) {
                    BindSetting bindSetting;
                    if (!DashboardScreen.in(d, d2, this.px - 2, row.y() - 2, this.pw + 4, row.h() + 2)) continue;
                    Setting setting = row.s();
                    if (setting instanceof NumberSetting) {
                        NumberSetting numberSetting;
                        this.editNum = numberSetting = (NumberSetting)setting;
                        this.numBuf = "";
                        break;
                    }
                    setting = row.s();
                    if (!(setting instanceof BindSetting) || !(bindSetting = (BindSetting)setting).isHoldModesSupported()) break;
                    bindSetting.cycleMode();
                    break;
                }
            }
            return true;
        }
        if (DashboardScreen.in(d, d2, this.searchX(), this.y + 7, 104, 16)) {
            this.searchFocus = true;
            return true;
        }
        for (int i = 0; i < CATS.length; ++i) {
            if (!DashboardScreen.in(d, d2, this.x + 6, this.y + 34 + i * 24, 80, 20)) continue;
            this.cat = i;
            this.scroll = 0.0f;
            this.sScroll = 0.0f;
            this.selected = null;
            this.query = "";
            return true;
        }
        if (DashboardScreen.in(d, d2, this.x + 92, this.y + 30, 236, this.cardsH())) {
            List<Module> list = this.list();
            for (int i = 0; i < list.size(); ++i) {
                int n2;
                int n3 = this.cardX(i);
                if (!DashboardScreen.in(d, d2, n3, n2 = this.cardY(i), 104, 40)) continue;
                Module module = list.get(i);
                if (d >= (double)(n3 + 104 - 34) && d2 < (double)(n2 + 22)) {
                    module.setToggled(!module.isToggled());
                } else {
                    this.selected = module;
                    this.sScroll = 0.0f;
                }
                return true;
            }
        }
        if (DashboardScreen.in(d, d2, this.px - 4, this.y + 32, this.pw + 8, 238)) {
            for (Row row : this.rows) {
                if (!DashboardScreen.in(d, d2, this.px - 2, row.y() - 2, this.pw + 4, row.h() + 2)) continue;
                Setting setting = row.s();
                if (setting instanceof NumberSetting) {
                    NumberSetting numberSetting;
                    this.dragging = numberSetting = (NumberSetting)setting;
                    this.setNum(numberSetting, d);
                } else if (setting instanceof BooleanSetting) {
                    BooleanSetting booleanSetting = (BooleanSetting)setting;
                    booleanSetting.setValue(!booleanSetting.getValue());
                } else if (setting instanceof ModeSetting) {
                    ModeSetting modeSetting = (ModeSetting)setting;
                    List<String> list = modeSetting.getModes();
                    modeSetting.setValue(list.get((list.indexOf(modeSetting.getValue()) + 1) % list.size()));
                } else if (setting instanceof CategorySetting) {
                    CategorySetting categorySetting = (CategorySetting)setting;
                    categorySetting.setOpen(!categorySetting.isOpen());
                } else if (setting instanceof BindSetting) {
                    BindSetting bindSetting;
                    this.listenBind = bindSetting = (BindSetting)setting;
                } else if (setting instanceof StringSetting) {
                    StringSetting stringSetting;
                    this.editStr = stringSetting = (StringSetting)setting;
                } else if (setting instanceof ColorSetting) {
                    ColorSetting colorSetting = (ColorSetting)setting;
                    int n4 = (int)((d - (double)this.px) / 11.0);
                    if (d2 >= (double)(row.y() + 13) && n4 >= 0 && n4 <= PALETTE.length) {
                        if (n4 == PALETTE.length) {
                            colorSetting.setRainbow(!colorSetting.isRainbow());
                        } else {
                            colorSetting.setRainbow(false);
                            Color color = PALETTE[n4];
                            colorSetting.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), colorSetting.getAlpha()));
                        }
                    }
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double d2) {
        if (this.dragging != null) {
            this.setNum(this.dragging, mouseButtonEvent.x());
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        this.dragging = null;
        return true;
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (DashboardScreen.in(d, d2, this.px - 4, this.y + 32, this.pw + 8, 238)) {
            this.sScroll -= (float)d4 * 20.0f;
        } else {
            this.scroll -= (float)d4 * 20.0f;
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        int n = keyEvent.key();
        if (this.listenBind != null) {
            this.listenBind.setValue(n == 256 || n == 261 ? 0 : n);
            this.listenBind = null;
            return true;
        }
        if (this.editNum != null) {
            if (n == 256) {
                this.editNum = null;
                this.numBuf = "";
            } else if (n == 257 || n == 335) {
                try {
                    this.applyNum(this.editNum, Double.parseDouble(this.numBuf.trim()));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                this.editNum = null;
                this.numBuf = "";
            } else if (n == 259 && !this.numBuf.isEmpty()) {
                this.numBuf = this.numBuf.substring(0, this.numBuf.length() - 1);
            }
            return true;
        }
        if (this.searchFocus) {
            if (n == 256) {
                this.query = "";
                this.searchFocus = false;
            } else if (n == 257 || n == 335) {
                this.searchFocus = false;
            } else if (n == 259 && !this.query.isEmpty()) {
                this.query = this.query.substring(0, this.query.length() - 1);
                this.scroll = 0.0f;
            }
            return true;
        }
        if (this.editStr != null) {
            String string;
            String string2 = string = this.editStr.getValue() == null ? "" : this.editStr.getValue();
            if (n == 256 || n == 257 || n == 335) {
                this.editStr = null;
            } else if (n == 259 && !string.isEmpty()) {
                this.editStr.setValue(string.substring(0, string.length() - 1));
            }
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        char c = (char)characterEvent.codepoint();
        if (this.editNum != null) {
            if (c == ',') {
                c = '.';
            }
            if ((Character.isDigit(c) || c == '.' || c == '-') && this.numBuf.length() < 12) {
                this.numBuf = this.numBuf + c;
            }
            return true;
        }
        if (this.searchFocus) {
            if (c >= ' ' && c != '\u007f' && this.query.length() < 24) {
                this.query = this.query + c;
                this.scroll = 0.0f;
            }
            return true;
        }
        if (this.editStr != null) {
            if (c >= ' ' && c != '\u007f') {
                this.editStr.setValue((this.editStr.getValue() == null ? "" : this.editStr.getValue()) + c);
            }
            return true;
        }
        return super.charTyped(characterEvent);
    }

    @Override
    public void removed() {
        this.resetInput();
        this.dragging = null;
        super.removed();
    }

    private record Row(Setting s, int y, int h) {
    }
}

