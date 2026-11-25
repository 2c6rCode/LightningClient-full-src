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

    private static boolean in(double mouseX, double mouseY, int boxX, int boxY, int boxW, int boxH) {
        return mouseX >= (double)boxX && mouseX < (double)(boxX + boxW) && mouseY >= (double)boxY && mouseY < (double)(boxY + boxH);
    }

    private static float approach(float current, float target, float factor) {
        float delta = target - current;
        return Math.abs(delta) < 0.01f ? target : current + delta * factor;
    }

    private static Color mix(Color start, Color end, float progress) {
        return new Color((int)((float)start.getRed() + (float)(end.getRed() - start.getRed()) * progress), (int)((float)start.getGreen() + (float)(end.getGreen() - start.getGreen()) * progress), (int)((float)start.getBlue() + (float)(end.getBlue() - start.getBlue()) * progress));
    }

    private static boolean vis(Setting setting) {
        Setting.Visibility visibility = setting.getVisibility();
        return visibility == null || visibility.isVisible();
    }

    private static void rr(GuiGraphicsExtractor guiGraphicsExtractor, float x, float y, float width, float height, float radius, Color color) {
        LightningSkin.rrAll(guiGraphicsExtractor, x, y, x + width, y + height, radius, color);
    }

    private void box(GuiGraphicsExtractor guiGraphicsExtractor, int x, int y, int width, int height, float radius, Color borderColor, Color fillColor) {
        DashboardScreen.rr(guiGraphicsExtractor, x, y, width, height, radius, borderColor);
        DashboardScreen.rr(guiGraphicsExtractor, x + 1, y + 1, width - 2, height - 2, Math.max(0.5f, radius - 1.0f), fillColor);
    }

    private void text(GuiGraphicsExtractor guiGraphicsExtractor, String text, int x, int y, Color color) {
        Night.FONT_MANAGER.drawText(guiGraphicsExtractor, text, x, y, color);
    }

    private int tw(String text) {
        return Night.FONT_MANAGER.getWidth(text);
    }

    private String fit(String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        FontManager fontManager = Night.FONT_MANAGER;
        if (fontManager.getWidth(text) <= maxWidth) {
            return text;
        }
        while (text.length() > 1 && fontManager.getWidth(text + "..") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "..";
    }

    private void toggle(GuiGraphicsExtractor guiGraphicsExtractor, int x, int y, float progress) {
        DashboardScreen.rr(guiGraphicsExtractor, x, y, 24.0f, 12.0f, 6.0f, DashboardScreen.mix(OFF, CYAN, progress));
        DashboardScreen.rr(guiGraphicsExtractor, (float)(x + 2) + 12.0f * progress, y + 2, 8.0f, 8.0f, 4.0f, Color.WHITE);
    }

    private List<Module> mods() {
        return Night.MODULE_MANAGER.getModules(CATS[this.cat]);
    }

    private List<Module> list() {
        if (this.query.isEmpty()) {
            return this.mods();
        }
        String lowerQuery = this.query.toLowerCase(Locale.ROOT);
        ArrayList<Module> modules = new ArrayList<Module>();
        for (Module.Category category : CATS) {
            for (Module module : Night.MODULE_MANAGER.getModules(category)) {
                if (!module.getName().toLowerCase(Locale.ROOT).contains(lowerQuery)) continue;
                modules.add(module);
            }
        }
        return modules;
    }

    private int cardX(int index) {
        return this.x + 92 + 10 + index % 2 * 112;
    }

    private int cardY(int index) {
        return this.y + 30 + index / 2 * 48 - (int)this.scroll;
    }

    private int cardsH() {
        return 206;
    }

    private int searchX() {
        return this.x + 92 + 10 + 208 + 8 - 104;
    }

    private static String desc(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        String translated = GuiDescriptionTranslations.translate(description);
        return translated == null || translated.isEmpty() ? description : translated;
    }

    private List<String> wrap(String text, int maxWidth, int maxLines) {
        ArrayList<String> lines = new ArrayList<String>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        String currentLine = "";
        for (String word : text.split(" ")) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (this.tw(candidate) <= maxWidth) {
                currentLine = candidate;
                continue;
            }
            if (!currentLine.isEmpty()) {
                lines.add(currentLine);
            }
            currentLine = word;
            if (lines.size() == maxLines) break;
        }
        if (lines.size() < maxLines && !currentLine.isEmpty()) {
            lines.add(currentLine);
        }
        if (!lines.isEmpty() && String.join((CharSequence)" ", lines).length() < text.length()) {
            int lastIndex = lines.size() - 1;
            String lastLine = lines.get(lastIndex);
            while (lastLine.length() > 1 && this.tw(lastLine + "..") > maxWidth) {
                lastLine = lastLine.substring(0, lastLine.length() - 1);
            }
            lines.set(lastIndex, lastLine + "..");
        }
        return lines;
    }

    private static void line(GuiGraphicsExtractor guiGraphicsExtractor, float x1, float y1, float x2, float y2, float thickness, Color color) {
        int steps = Math.max(1, (int)Math.ceil(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) * 2.0f));
        for (int i = 0; i <= steps; ++i) {
            float progress = (float)i / (float)steps;
            float px = x1 + (x2 - x1) * progress;
            float py = y1 + (y2 - y1) * progress;
            Renderer2D.renderQuad(guiGraphicsExtractor, px - thickness / 2.0f, py - thickness / 2.0f, px + thickness / 2.0f, py + thickness / 2.0f, color);
        }
    }

    private static void bolt(GuiGraphicsExtractor guiGraphicsExtractor, float x, float y, float size, Color color) {
        float scale = size / 10.0f;
        DashboardScreen.line(guiGraphicsExtractor, x + 7.0f * scale, y, x + 2.5f * scale, y + 6.0f * scale, 1.8f * scale, color);
        DashboardScreen.line(guiGraphicsExtractor, x + 2.5f * scale, y + 6.0f * scale, x + 7.0f * scale, y + 6.0f * scale, 1.8f * scale, color);
        DashboardScreen.line(guiGraphicsExtractor, x + 7.0f * scale, y + 6.0f * scale, x + 3.0f * scale, y + 10.0f * scale, 1.8f * scale, color);
    }

    private static void searchIcon(GuiGraphicsExtractor guiGraphicsExtractor, float x, float y, Color strokeColor, Color fillColor) {
        DashboardScreen.rr(guiGraphicsExtractor, x, y, 7.0f, 7.0f, 3.5f, strokeColor);
        DashboardScreen.rr(guiGraphicsExtractor, x + 1.4f, y + 1.4f, 4.2f, 4.2f, 2.1f, fillColor);
        DashboardScreen.line(guiGraphicsExtractor, x + 6.0f, y + 6.0f, x + 9.5f, y + 9.5f, 1.6f, strokeColor);
    }

    private static void catIcon(GuiGraphicsExtractor guiGraphicsExtractor, int categoryIndex, float x, float y, Color primaryColor, Color backgroundColor) {
        switch (categoryIndex) {
            case 0: {
                DashboardScreen.line(guiGraphicsExtractor, x + 1.0f, y + 1.0f, x + 9.0f, y + 9.0f, 1.6f, primaryColor);
                DashboardScreen.line(guiGraphicsExtractor, x + 9.0f, y + 1.0f, x + 1.0f, y + 9.0f, 1.6f, primaryColor);
                DashboardScreen.line(guiGraphicsExtractor, x + 1.0f, y + 4.0f, x + 4.0f, y + 1.0f, 1.4f, primaryColor);
                DashboardScreen.line(guiGraphicsExtractor, x + 9.0f, y + 4.0f, x + 6.0f, y + 1.0f, 1.4f, primaryColor);
                break;
            }
            case 1: {
                DashboardScreen.line(guiGraphicsExtractor, x + 1.0f, y + 1.0f, x + 4.5f, y + 5.0f, 1.6f, primaryColor);
                DashboardScreen.line(guiGraphicsExtractor, x + 4.5f, y + 5.0f, x + 1.0f, y + 9.0f, 1.6f, primaryColor);
                DashboardScreen.line(guiGraphicsExtractor, x + 5.5f, y + 1.0f, x + 9.0f, y + 5.0f, 1.6f, primaryColor);
                DashboardScreen.line(guiGraphicsExtractor, x + 9.0f, y + 5.0f, x + 5.5f, y + 9.0f, 1.6f, primaryColor);
                break;
            }
            case 2: {
                DashboardScreen.rr(guiGraphicsExtractor, x, y + 1.5f, 10.0f, 7.0f, 3.5f, primaryColor);
                DashboardScreen.rr(guiGraphicsExtractor, x + 1.2f, y + 2.7f, 7.6f, 4.6f, 2.3f, backgroundColor);
                DashboardScreen.rr(guiGraphicsExtractor, x + 3.5f, y + 3.5f, 3.0f, 3.0f, 1.5f, primaryColor);
                break;
            }
            case 3: {
                DashboardScreen.rr(guiGraphicsExtractor, x + 3.0f, y, 4.0f, 4.0f, 2.0f, primaryColor);
                DashboardScreen.rr(guiGraphicsExtractor, x + 0.5f, y + 5.0f, 9.0f, 5.0f, 2.5f, primaryColor);
                break;
            }
            default: {
                DashboardScreen.rr(guiGraphicsExtractor, x, y + 1.5f, 10.0f, 1.3f, 0.6f, primaryColor);
                DashboardScreen.rr(guiGraphicsExtractor, x, y + 4.4f, 10.0f, 1.3f, 0.6f, primaryColor);
                DashboardScreen.rr(guiGraphicsExtractor, x, y + 7.3f, 10.0f, 1.3f, 0.6f, primaryColor);
                DashboardScreen.rr(guiGraphicsExtractor, x + 6.0f, y, 2.6f, 4.2f, 1.2f, primaryColor);
                DashboardScreen.rr(guiGraphicsExtractor, x + 2.0f, y + 3.0f, 2.6f, 4.2f, 1.2f, primaryColor);
                DashboardScreen.rr(guiGraphicsExtractor, x + 5.5f, y + 5.9f, 2.6f, 4.2f, 1.2f, primaryColor);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
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
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
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
        List<Module> modules = this.list();
        if (this.selected == null || this.query.isEmpty() && this.selected.getCategory() != CATS[this.cat] || !modules.contains(this.selected)) {
            this.selected = modules.isEmpty() ? null : modules.get(0);
        }
        this.box(guiGraphicsExtractor, this.x, this.y, 460, 280, 8.0f, LINE, WINDOW);
        DashboardScreen.rr(guiGraphicsExtractor, this.x + 1, this.y + 1, 91.0f, 278.0f, 7.0f, SIDE);
        DashboardScreen.rr(guiGraphicsExtractor, this.x + 460 - 132, this.y + 1, 131.0f, 278.0f, 7.0f, SIDE);
        DashboardScreen.bolt(guiGraphicsExtractor, this.x + 11, this.y + 10, 11.0f, CYAN);
        this.text(guiGraphicsExtractor, "Lightning", this.x + 27, this.y + 12, CYAN);
        for (int i = 0; i < CATS.length; ++i) {
            int catY = this.y + 34 + i * 24;
            boolean isSelectedCat = i == this.cat && this.query.isEmpty();
            boolean isHoverCat = DashboardScreen.in(mouseX, mouseY, this.x + 6, catY, 80, 20);
            Color iconBg = SIDE;
            if (isSelectedCat) {
                DashboardScreen.rr(guiGraphicsExtractor, this.x + 6, catY, 80.0f, 20.0f, 5.0f, CARD_SEL);
                DashboardScreen.rr(guiGraphicsExtractor, this.x + 6, catY + 3, 2.0f, 14.0f, 1.0f, CYAN);
                iconBg = CARD_SEL;
            } else if (isHoverCat) {
                DashboardScreen.rr(guiGraphicsExtractor, this.x + 6, catY, 80.0f, 20.0f, 5.0f, CARD);
                iconBg = CARD;
            }
            DashboardScreen.catIcon(guiGraphicsExtractor, i, this.x + 14, catY + 5, isSelectedCat ? CYAN : DIM, iconBg);
            this.text(guiGraphicsExtractor, this.fit(CATS[i].getName(), 34), this.x + 30, catY + 6, isSelectedCat ? TEXT : DIM);
            String toggledCount = String.valueOf(Night.MODULE_MANAGER.getModules(CATS[i]).stream().filter(Module::isToggled).count());
            this.text(guiGraphicsExtractor, toggledCount, this.x + 92 - 10 - this.tw(toggledCount), catY + 6, isSelectedCat ? CYAN : DIM);
        }
        this.text(guiGraphicsExtractor, this.fit(this.query.isEmpty() ? CATS[this.cat].getName() : "Search", 100), this.x + 92 + 10, this.y + 12, TEXT);
        int searchX = this.searchX();
        int searchY = this.y + 7;
        this.box(guiGraphicsExtractor, searchX, searchY, 104, 16, 5.0f, this.searchFocus ? CYAN : LINE, CARD);
        DashboardScreen.searchIcon(guiGraphicsExtractor, searchX + 5, searchY + 3, this.searchFocus ? CYAN : DIM, CARD);
        if (this.query.isEmpty() && !this.searchFocus) {
            this.text(guiGraphicsExtractor, "Search", searchX + 20, searchY + 4, DIM);
        } else {
            String visibleQuery = this.query;
            while (visibleQuery.length() > 1 && this.tw(visibleQuery) > 74) {
                visibleQuery = visibleQuery.substring(1);
            }
            boolean showCaret = this.searchFocus && System.currentTimeMillis() / 500L % 2L == 0L;
            this.text(guiGraphicsExtractor, visibleQuery + (showCaret ? "|" : ""), searchX + 20, searchY + 4, TEXT);
        }
        int cardsHeight = this.cardsH();
        int maxScroll = Math.max(0, (modules.size() + 1) / 2 * 48 - cardsHeight);
        this.scroll = Math.max(0.0f, Math.min(this.scroll, (float)maxScroll));
        guiGraphicsExtractor.enableScissor(this.x + 92, this.y + 30, this.x + 460 - 132, this.y + 30 + cardsHeight);
        for (int i = 0; i < modules.size(); ++i) {
            Module module = modules.get(i);
            int cx = this.cardX(i);
            int cy = this.cardY(i);
            float[] animVals = this.anim.computeIfAbsent(module, mod -> new float[2]);
            boolean hovered = DashboardScreen.in(mouseX, mouseY, cx, cy, 104, 40) && DashboardScreen.in(mouseX, mouseY, this.x + 92, this.y + 30, 236, cardsHeight);
            animVals[0] = DashboardScreen.approach(animVals[0], module.isToggled() ? 1.0f : 0.0f, 0.25f);
            animVals[1] = DashboardScreen.approach(animVals[1], hovered ? 1.0f : 0.0f, 0.3f);
            if (hovered) {
                this.hoverDesc = module.getDescription();
            }
            this.box(guiGraphicsExtractor, cx, cy, 104, 40, 6.0f, module == this.selected ? CYAN : DashboardScreen.mix(LINE, LINE_HI, animVals[1]), module == this.selected ? CARD_SEL : CARD);
            this.text(guiGraphicsExtractor, this.fit(module.getName(), 58), cx + 8, cy + 7, DashboardScreen.mix(DIM, TEXT, Math.max(animVals[0], animVals[1])));
            this.toggle(guiGraphicsExtractor, cx + 104 - 32, cy + 6, animVals[0]);
            this.text(guiGraphicsExtractor, this.fit(DashboardScreen.desc(module.getDescription()), 88), cx + 8, cy + 24, DIM);
        }
        guiGraphicsExtractor.disableScissor();
        this.rows.clear();
        int settingsTop = this.y + 32;
        int settingsBottom = this.y + 280 - 10;
        if (this.selected != null) {
            this.text(guiGraphicsExtractor, this.fit(this.selected.getName(), this.pw), this.px, this.y + 12, CYAN);
            ArrayList<Setting> visibleSettings = new ArrayList<Setting>();
            int totalSettingsHeight = 0;
            for (Setting setting : this.selected.getSettings()) {
                int settingHeight;
                if (!DashboardScreen.vis(setting) || (settingHeight = DashboardScreen.rowHeight(setting)) == 0) continue;
                visibleSettings.add(setting);
                totalSettingsHeight += settingHeight + 6;
            }
            float maxSettingScroll = Math.max(0, totalSettingsHeight - (settingsBottom - settingsTop - 2));
            this.sScroll = Math.max(0.0f, Math.min(this.sScroll, maxSettingScroll));
            guiGraphicsExtractor.enableScissor(this.px - 4, settingsTop, this.x + 460 - 2, settingsBottom);
            int rowY = settingsTop + 2 - (int)this.sScroll;
            boolean inSettingsArea = DashboardScreen.in(mouseX, mouseY, this.px - 4, settingsTop, this.pw + 8, settingsBottom - settingsTop);
            for (Setting setting : visibleSettings) {
                int settingHeight = DashboardScreen.rowHeight(setting);
                this.rows.add(new Row(setting, rowY, settingHeight));
                if (inSettingsArea && DashboardScreen.in(mouseX, mouseY, this.px - 2, rowY, this.pw + 4, settingHeight)) {
                    this.hoverDesc = setting.getDescription();
                }
                this.drawSetting(guiGraphicsExtractor, setting, rowY, mouseX, mouseY);
                rowY += settingHeight + 6;
            }
            guiGraphicsExtractor.disableScissor();
        }
        String description = this.hoverDesc;
        if ((description == null || description.isEmpty()) && this.selected != null) {
            description = this.selected.getDescription();
        }
        int descBoxX = this.x + 92 + 10;
        int descBoxY = this.y + 280 - 10 - 28 + 2;
        int descBoxWidth = 216;
        this.box(guiGraphicsExtractor, descBoxX, descBoxY, descBoxWidth, 26, 5.0f, LINE, CARD);
        int descLineY = descBoxY + 5;
        for (String line : this.wrap(DashboardScreen.desc(description), descBoxWidth - 14, 2)) {
            this.text(guiGraphicsExtractor, line, descBoxX + 7, descLineY, DIM);
            descLineY += 10;
        }
    }

    private void drawSetting(GuiGraphicsExtractor guiGraphicsExtractor, Setting setting, int rowY, int mouseX, int mouseY) {
        if (setting instanceof CategorySetting categorySetting) {
            boolean isOpen = categorySetting.isOpen();
            DashboardScreen.rr(guiGraphicsExtractor, this.px, rowY, this.pw, 16.0f, 4.0f, CARD);
            this.text(guiGraphicsExtractor, this.fit(categorySetting.getName(), this.pw - 24), this.px + 6, rowY + 4, isOpen ? CYAN : TEXT);
            this.text(guiGraphicsExtractor, isOpen ? "-" : "+", this.px + this.pw - 12, rowY + 4, isOpen ? CYAN : DIM);
            return;
        }
        this.text(guiGraphicsExtractor, this.fit(setting.getName(), this.pw - 40), this.px, rowY, DIM);
        if (setting instanceof NumberSetting numberSetting) {
            Number value = numberSetting.getValue();
            String valueText = value instanceof Integer ? value.toString() : String.valueOf((double)Math.round(value.doubleValue() * 100.0) / 100.0);
            if (this.editNum == numberSetting) {
                boolean showCaret = System.currentTimeMillis() / 500L % 2L == 0L;
                String displayBuf = this.numBuf + (showCaret ? "|" : "");
                int boxWidth = Math.max(34, this.tw(displayBuf) + 10);
                this.box(guiGraphicsExtractor, this.px + this.pw - boxWidth, rowY - 3, boxWidth, 14, 4.0f, CYAN, CARD);
                this.text(guiGraphicsExtractor, displayBuf, this.px + this.pw - boxWidth + 5, rowY, CYAN);
            } else {
                this.text(guiGraphicsExtractor, valueText, this.px + this.pw - this.tw(valueText), rowY, TEXT);
            }
            double minVal = numberSetting.getMinimum().doubleValue();
            double maxVal = numberSetting.getMaximum().doubleValue();
            float ratio = (float)((value.doubleValue() - minVal) / Math.max(1.0E-4, maxVal - minVal));
            DashboardScreen.rr(guiGraphicsExtractor, this.px, rowY + 15, this.pw, 4.0f, 2.0f, OFF);
            DashboardScreen.rr(guiGraphicsExtractor, this.px, rowY + 15, Math.max(4.0f, (float)this.pw * ratio), 4.0f, 2.0f, CYAN);
        } else if (setting instanceof BooleanSetting booleanSetting) {
            DashboardScreen.rr(guiGraphicsExtractor, this.px + this.pw - 14, rowY - 1, 14.0f, 14.0f, 4.0f, booleanSetting.getValue() ? CYAN : OFF);
            if (booleanSetting.getValue()) {
                DashboardScreen.rr(guiGraphicsExtractor, this.px + this.pw - 10, rowY + 3, 6.0f, 6.0f, 2.0f, DARK);
            }
        } else if (setting instanceof ModeSetting modeSetting) {
            this.box(guiGraphicsExtractor, this.px, rowY + 10, this.pw, 14, 4.0f, LINE, CARD);
            this.text(guiGraphicsExtractor, this.fit(modeSetting.getValue(), this.pw - 10), this.px + 5, rowY + 13, TEXT);
        } else if (setting instanceof BindSetting bindSetting) {
            boolean isListening = this.listenBind == bindSetting;
            String bindText;
            if (isListening) {
                bindText = "...";
            } else {
                bindText = bindSetting.getValue() == 0 ? "None" : KeyboardUtils.getKeyName(bindSetting.getValue());
                String mode = bindSetting.getMode();
                if (mode != null && !mode.equals("Bind")) {
                    bindText = bindText + " (" + mode + ")";
                }
            }
            bindText = this.fit(bindText, 62);
            int boxWidth = Math.max(30, this.tw(bindText) + 12);
            this.box(guiGraphicsExtractor, this.px + this.pw - boxWidth, rowY - 2, boxWidth, 15, 4.0f, isListening ? CYAN : LINE, CARD);
            this.text(guiGraphicsExtractor, bindText, this.px + this.pw - boxWidth + 6, rowY + 1, isListening ? CYAN : TEXT);
        } else if (setting instanceof ColorSetting colorSetting) {
            Color currentColor = colorSetting.getColor();
            DashboardScreen.rr(guiGraphicsExtractor, this.px + this.pw - 14, rowY - 1, 14.0f, 11.0f, 3.0f, colorSetting.isRainbow() ? CYAN : LINE_HI);
            DashboardScreen.rr(guiGraphicsExtractor, this.px + this.pw - 13, rowY, 12.0f, 9.0f, 2.5f, colorSetting.isRainbow() ? new Color(255, 120, 200) : new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()));
            for (int i = 0; i <= PALETTE.length; ++i) {
                int swatchX = this.px + i * 11;
                boolean isRainbowSwatch = i == PALETTE.length;
                Color swatchColor = isRainbowSwatch ? new Color(255, 120, 200) : PALETTE[i];
                boolean isSelectedSwatch = isRainbowSwatch ? colorSetting.isRainbow() : (!colorSetting.isRainbow() && swatchColor.getRGB() == (currentColor.getRGB() | 0xFF000000));
                if (isSelectedSwatch) {
                    DashboardScreen.rr(guiGraphicsExtractor, swatchX - 1, rowY + 13, 11.0f, 11.0f, 3.0f, TEXT);
                }
                DashboardScreen.rr(guiGraphicsExtractor, swatchX, rowY + 14, 9.0f, 9.0f, 2.5f, swatchColor);
                if (!isRainbowSwatch) continue;
                DashboardScreen.rr(guiGraphicsExtractor, (float)swatchX + 2.5f, (float)rowY + 16.5f, 4.0f, 4.0f, 2.0f, new Color(92, 225, 255));
            }
        } else if (setting instanceof StringSetting stringSetting) {
            boolean isEditing = this.editStr == stringSetting;
            this.box(guiGraphicsExtractor, this.px, rowY + 10, this.pw, 14, 4.0f, isEditing ? CYAN : LINE, CARD);
            String value = stringSetting.getValue() == null ? "" : stringSetting.getValue();
            while (value.length() > 1 && this.tw(value) > this.pw - 14) {
                value = value.substring(1);
            }
            boolean showCaret = isEditing && System.currentTimeMillis() / 500L % 2L == 0L;
            this.text(guiGraphicsExtractor, value + (showCaret ? "|" : ""), this.px + 5, rowY + 13, TEXT);
        } else if (setting instanceof WhitelistSetting whitelistSetting) {
            String countText = whitelistSetting.getWhitelist().size() + " items";
            this.text(guiGraphicsExtractor, countText, this.px + this.pw - this.tw(countText), rowY, TEXT);
        }
    }

    private void setNum(NumberSetting numberSetting, double mouseX) {
        double minVal = numberSetting.getMinimum().doubleValue();
        double maxVal = numberSetting.getMaximum().doubleValue();
        double step = numberSetting.getStep().doubleValue();
        double value = minVal + Math.max(0.0, Math.min(1.0, (mouseX - (double)this.px) / (double)this.pw)) * (maxVal - minVal);
        if (step > 0.0) {
            value = minVal + (double)Math.round((value - minVal) / step) * step;
        }
        this.applyNum(numberSetting, value);
    }

    private void applyNum(NumberSetting numberSetting, double value) {
        double minVal = numberSetting.getMinimum().doubleValue();
        double maxVal = numberSetting.getMaximum().doubleValue();
        value = Math.max(minVal, Math.min(maxVal, value));
        Number currentValue = numberSetting.getValue();
        numberSetting.setValue(currentValue instanceof Integer ? (Number)((int)Math.round(value)) : (Number)(currentValue instanceof Float ? (Number)Float.valueOf((float)value) : (Number)value));
    }

    private void resetInput() {
        this.searchFocus = false;
        this.editStr = null;
        this.listenBind = null;
        this.editNum = null;
        this.numBuf = "";
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
        if (button != 0 && button != 1) {
            return true;
        }
        this.resetInput();
        if (button == 1) {
            if (DashboardScreen.in(mouseX, mouseY, this.px - 4, this.y + 32, this.pw + 8, 238)) {
                for (Row row : this.rows) {
                    if (!DashboardScreen.in(mouseX, mouseY, this.px - 2, row.y() - 2, this.pw + 4, row.h() + 2)) continue;
                    Setting setting = row.s();
                    if (setting instanceof NumberSetting numberSetting) {
                        this.editNum = numberSetting;
                        this.numBuf = "";
                        break;
                    }
                    if (!(setting instanceof BindSetting bindSetting) || !bindSetting.isHoldModesSupported()) break;
                    bindSetting.cycleMode();
                    break;
                }
            }
            return true;
        }
        if (DashboardScreen.in(mouseX, mouseY, this.searchX(), this.y + 7, 104, 16)) {
            this.searchFocus = true;
            return true;
        }
        for (int i = 0; i < CATS.length; ++i) {
            if (!DashboardScreen.in(mouseX, mouseY, this.x + 6, this.y + 34 + i * 24, 80, 20)) continue;
            this.cat = i;
            this.scroll = 0.0f;
            this.sScroll = 0.0f;
            this.selected = null;
            this.query = "";
            return true;
        }
        if (DashboardScreen.in(mouseX, mouseY, this.x + 92, this.y + 30, 236, this.cardsH())) {
            List<Module> modules = this.list();
            for (int i = 0; i < modules.size(); ++i) {
                int cx = this.cardX(i);
                int cy = this.cardY(i);
                if (!DashboardScreen.in(mouseX, mouseY, cx, cy, 104, 40)) continue;
                Module module = modules.get(i);
                if (mouseX >= (double)(cx + 104 - 34) && mouseY < (double)(cy + 22)) {
                    module.setToggled(!module.isToggled());
                } else {
                    this.selected = module;
                    this.sScroll = 0.0f;
                }
                return true;
            }
        }
        if (DashboardScreen.in(mouseX, mouseY, this.px - 4, this.y + 32, this.pw + 8, 238)) {
            for (Row row : this.rows) {
                if (!DashboardScreen.in(mouseX, mouseY, this.px - 2, row.y() - 2, this.pw + 4, row.h() + 2)) continue;
                Setting setting = row.s();
                if (setting instanceof NumberSetting numberSetting) {
                    this.dragging = numberSetting;
                    this.setNum(numberSetting, mouseX);
                } else if (setting instanceof BooleanSetting booleanSetting) {
                    booleanSetting.setValue(!booleanSetting.getValue());
                } else if (setting instanceof ModeSetting modeSetting) {
                    List<String> modes = modeSetting.getModes();
                    modeSetting.setValue(modes.get((modes.indexOf(modeSetting.getValue()) + 1) % modes.size()));
                } else if (setting instanceof CategorySetting categorySetting) {
                    categorySetting.setOpen(!categorySetting.isOpen());
                } else if (setting instanceof BindSetting bindSetting) {
                    this.listenBind = bindSetting;
                } else if (setting instanceof StringSetting stringSetting) {
                    this.editStr = stringSetting;
                } else if (setting instanceof ColorSetting colorSetting) {
                    int swatchIndex = (int)((mouseX - (double)this.px) / 11.0);
                    if (mouseY >= (double)(row.y() + 13) && swatchIndex >= 0 && swatchIndex <= PALETTE.length) {
                        if (swatchIndex == PALETTE.length) {
                            colorSetting.setRainbow(!colorSetting.isRainbow());
                        } else {
                            colorSetting.setRainbow(false);
                            Color color = PALETTE[swatchIndex];
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
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (DashboardScreen.in(mouseX, mouseY, this.px - 4, this.y + 32, this.pw + 8, 238)) {
            this.sScroll -= (float)verticalAmount * 20.0f;
        } else {
            this.scroll -= (float)verticalAmount * 20.0f;
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        if (this.listenBind != null) {
            this.listenBind.setValue(keyCode == 256 || keyCode == 261 ? 0 : keyCode);
            this.listenBind = null;
            return true;
        }
        if (this.editNum != null) {
            if (keyCode == 256) {
                this.editNum = null;
                this.numBuf = "";
            } else if (keyCode == 257 || keyCode == 335) {
                try {
                    this.applyNum(this.editNum, Double.parseDouble(this.numBuf.trim()));
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                this.editNum = null;
                this.numBuf = "";
            } else if (keyCode == 259 && !this.numBuf.isEmpty()) {
                this.numBuf = this.numBuf.substring(0, this.numBuf.length() - 1);
            }
            return true;
        }
        if (this.searchFocus) {
            if (keyCode == 256) {
                this.query = "";
                this.searchFocus = false;
            } else if (keyCode == 257 || keyCode == 335) {
                this.searchFocus = false;
            } else if (keyCode == 259 && !this.query.isEmpty()) {
                this.query = this.query.substring(0, this.query.length() - 1);
                this.scroll = 0.0f;
            }
            return true;
        }
        if (this.editStr != null) {
            String value = this.editStr.getValue() == null ? "" : this.editStr.getValue();
            if (keyCode == 256 || keyCode == 257 || keyCode == 335) {
                this.editStr = null;
            } else if (keyCode == 259 && !value.isEmpty()) {
                this.editStr.setValue(value.substring(0, value.length() - 1));
            }
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        char typedChar = (char)characterEvent.codepoint();
        if (this.editNum != null) {
            if (typedChar == ',') {
                typedChar = '.';
            }
            if ((Character.isDigit(typedChar) || typedChar == '.' || typedChar == '-') && this.numBuf.length() < 12) {
                this.numBuf = this.numBuf + typedChar;
            }
            return true;
        }
        if (this.searchFocus) {
            if (typedChar >= ' ' && typedChar != '\u007f' && this.query.length() < 24) {
                this.query = this.query + typedChar;
                this.scroll = 0.0f;
            }
            return true;
        }
        if (this.editStr != null) {
            if (typedChar >= ' ' && typedChar != '\u007f') {
                this.editStr.setValue((this.editStr.getValue() == null ? "" : this.editStr.getValue()) + typedChar);
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

