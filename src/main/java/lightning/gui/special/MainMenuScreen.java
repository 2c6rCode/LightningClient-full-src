/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.client.gui.screens.options.OptionsScreen
 *  net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvents
 */
package lightning.gui.special;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.screens.account.AccountManagerScreen;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.graphics.MenuShader;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class MainMenuScreen
extends Screen {
    private final List<Entry> entries = new ArrayList<Entry>();
    private static final long LERP_DURATION_MS = 4000L;
    private static final long BUTTON_FADE_DURATION_MS = 800L;
    private static long initialOpenTime = -1L;
    private static final float[][] BOLT = new float[][]{{22.0f, 0.0f}, {0.0f, 30.0f}, {14.0f, 30.0f}, {8.0f, 54.0f}, {34.0f, 20.0f}, {19.0f, 20.0f}, {28.0f, 0.0f}};
    private int sideWidth = 200;

    public MainMenuScreen() {
        super((Component)Component.literal((String)"Lightning Client"));
        if (initialOpenTime < 0L) {
            initialOpenTime = System.currentTimeMillis();
        }
    }

    protected void init() {
        if (initialOpenTime < 0L) {
            initialOpenTime = System.currentTimeMillis();
        }
        this.entries.clear();
        this.sideWidth = Math.max(150, Math.min(230, (int)((float)this.width * 0.26f)));
        int rowH = 22;
        int gap = 2;
        int y = 82;
        this.entries.add(new Entry("S  Singleplayer", 0, y, this.sideWidth, rowH, false, () -> this.open((Screen)new SelectWorldScreen((Screen)this))));
        this.entries.add(new Entry("M  Multiplayer", 0, y += rowH + gap, this.sideWidth, rowH, false, () -> this.open((Screen)new JoinMultiplayerScreen((Screen)this))));
        this.entries.add(new Entry("A  Account Manager", 0, y += rowH + gap, this.sideWidth, rowH, false, () -> this.open(new AccountManagerScreen(this))));
        this.entries.add(new Entry("O  Options", 0, y += rowH + gap, this.sideWidth, rowH, false, () -> this.open((Screen)new OptionsScreen((Screen)this, this.minecraft.options, false))));
        int quitY = Math.max(y + rowH + gap + 8, this.height - 62);
        this.entries.add(new Entry("Q  Quit", 0, quitY, this.sideWidth, rowH, true, () -> this.minecraft.stop()));
    }

    private void open(Screen screen) {
        Gui gui = this.minecraft.gui;
        for (Class<?> c = gui.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod("setScreen", Screen.class);
                m.setAccessible(true);
                m.invoke((Object)gui, screen);
                return;
            }
            catch (NoSuchMethodException m) {
                continue;
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
        long l = System.currentTimeMillis() - initialOpenTime;
        float f2 = Math.min(1.0f, (float)l / 4000.0f);
        float f3 = 0.0f;
        if (f2 >= 1.0f) {
            long l2 = l - 4000L;
            float f4 = Math.min(1.0f, (float)l2 / 800.0f);
            f3 = 1.0f - (float)Math.pow(1.0f - f4, 3.0);
        }
        if (f3 > 0.001f) {
            this.renderSidebar(guiGraphicsExtractor, f3);
            for (Entry entry : this.entries) {
                entry.render(guiGraphicsExtractor, n, n2, f, f3);
            }
            this.renderFooter(guiGraphicsExtractor, f3);
        }
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        long l = System.currentTimeMillis() - initialOpenTime;
        float f2 = Math.min(1.0f, (float)l / 4000.0f);
        MenuShader.render(guiGraphicsExtractor, this.width, this.height, 1.0f, f2);
    }

    private void renderSidebar(GuiGraphicsExtractor g, float f) {
        Renderer2D.renderQuad(g, this.sideWidth, 0.0f, this.sideWidth + 1, this.height, new Color(150, 140, 255, MainMenuScreen.clamp255(70.0f * f)));
        MainMenuScreen.fillBolt(g, 14.0f, 12.0f, 0.42f, new Color(176, 160, 255, MainMenuScreen.clamp255(255.0f * f)));
        int fh = Night.FONT_MANAGER.getHeight();
        String title = "Lightning Client";
        int ty = 42;
        if (MainMenuScreen.isRainbow()) {
            Night.FONT_MANAGER.drawRainbowString(g, title, 14, ty, 6L);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(g, title, 14, ty, new Color(220, 212, 255, MainMenuScreen.clamp255(255.0f * f)));
        }
        Night.FONT_MANAGER.drawTextWithShadow(g, "Recode By PhatNho", 14, ty + fh + 3, new Color(140, 130, 200, MainMenuScreen.clamp255(220.0f * f)));
    }

    private static void fillBolt(GuiGraphicsExtractor g, float ox, float oy, float scale, Color color) {
        int n = BOLT.length;
        float step = 0.5f;
        float bottom = 54.0f * scale;
        float[] xs = new float[n];
        for (float yy = 0.0f; yy < bottom; yy += step) {
            int i;
            float yc = (yy + step / 2.0f) / scale;
            int cnt = 0;
            for (i = 0; i < n; ++i) {
                float[] a = BOLT[i];
                float[] b = BOLT[(i + 1) % n];
                if (!(a[1] <= yc && b[1] > yc || b[1] <= yc && a[1] > yc)) continue;
                float t = (yc - a[1]) / (b[1] - a[1]);
                xs[cnt++] = a[0] + t * (b[0] - a[0]);
            }
            for (i = 1; i < cnt; ++i) {
                float v = xs[i];
                int j = i - 1;
                while (j >= 0 && xs[j] > v) {
                    xs[j + 1] = xs[j];
                    --j;
                }
                xs[j + 1] = v;
            }
            i = 0;
            while (i + 1 < cnt) {
                Renderer2D.renderQuad(g, ox + xs[i] * scale, oy + yy, ox + xs[i + 1] * scale, oy + yy + step, color);
                i += 2;
            }
        }
    }

    private void renderFooter(GuiGraphicsExtractor g, float f) {
        String name;
        int fh = Night.FONT_MANAGER.getHeight();
        Renderer2D.renderQuad(g, 0.0f, this.height - 36, this.sideWidth, this.height - 35, new Color(150, 140, 255, MainMenuScreen.clamp255(60.0f * f)));
        float cy = this.height - 18;
        Renderer2D.renderQuad(g, 14.0f, cy - 3.0f, 20.0f, cy + 3.0f, new Color(93, 202, 122, MainMenuScreen.clamp255(255.0f * f)));
        String string = name = Night.ACCOUNT_MANAGER != null ? Night.ACCOUNT_MANAGER.getCurrentName() : "";
        if (name == null) {
            name = "";
        }
        Night.FONT_MANAGER.drawTextWithShadow(g, name, 26, (int)cy - fh / 2, new Color(214, 206, 255, MainMenuScreen.clamp255(230.0f * f)));
        String tag = "Lightning Client \u00b7 26.2";
        int tw = Night.FONT_MANAGER.getWidth(tag);
        Night.FONT_MANAGER.drawTextWithShadow(g, tag, this.width - tw - 8, this.height - fh - 8, new Color(150, 142, 210, MainMenuScreen.clamp255(200.0f * f)));
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        long l = System.currentTimeMillis() - initialOpenTime;
        if (l < 4000L) {
            return false;
        }
        double d = mouseButtonEvent.x();
        double d2 = mouseButtonEvent.y();
        if (mouseButtonEvent.button() == 0) {
            for (Entry entry : this.entries) {
                if (!entry.isHovered(d, d2)) continue;
                this.playClick();
                entry.action.run();
                return true;
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void playClick() {
        if (Night.MODULE_MANAGER.getModule(ClickGuiModule.class).sounds.getValue()) {
            this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        }
    }

    private static boolean isRainbow() {
        return Night.MODULE_MANAGER.getModule(ClickGuiModule.class).isRainbow();
    }

    private static Color accentColor(int n) {
        return ClickGuiScreen.getButtonColor(0, n);
    }

    private static int clamp255(float f) {
        return Math.max(0, Math.min(255, (int)f));
    }

    private static int mixInt(int n, int n2, float f) {
        return (int)((float)n + (float)(n2 - n) * f);
    }

    private static class Entry {
        private final String label;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final boolean danger;
        private final Runnable action;
        private float hover;

        private Entry(String string, int n, int n2, int n3, int n4, boolean bl, Runnable runnable) {
            this.label = string;
            this.x = n;
            this.y = n2;
            this.width = n3;
            this.height = n4;
            this.danger = bl;
            this.action = runnable;
        }

        private boolean isHovered(double d, double d2) {
            return d >= (double)this.x && d <= (double)(this.x + this.width) && d2 >= (double)this.y && d2 <= (double)(this.y + this.height);
        }

        private void render(GuiGraphicsExtractor g, int mx, int my, float partial, float f) {
            int idx;
            float target = this.isHovered(mx, my) ? 1.0f : 0.0f;
            this.hover += (target - this.hover) * 0.2f;
            if (this.hover > 0.01f) {
                Renderer2D.renderQuad(g, this.x, this.y, this.x + this.width, this.y + this.height, new Color(120, 95, 230, MainMenuScreen.clamp255(f * 48.0f * this.hover)));
                Renderer2D.renderQuad(g, this.x, this.y, this.x + 2, this.y + this.height, MainMenuScreen.accentColor(MainMenuScreen.clamp255(f * 255.0f * this.hover)));
            }
            String name = (idx = this.label.indexOf("  ")) > 0 ? this.label.substring(idx + 2) : this.label;
            int fh = Night.FONT_MANAGER.getHeight();
            int ty = this.y + (this.height - fh) / 2;
            int tx = this.x + 14 + Math.round(this.hover * 3.0f);
            int a = MainMenuScreen.clamp255(f * 255.0f);
            Color main = this.danger ? new Color(240, 149, 149, a) : new Color(MainMenuScreen.mixInt(228, 255, this.hover), MainMenuScreen.mixInt(224, 255, this.hover), 255, a);
            Night.FONT_MANAGER.drawTextWithShadow(g, name, tx, ty, main);
        }
    }
}

