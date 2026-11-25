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
        for (Class<?> cls = gui.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                Method method = cls.getDeclaredMethod("setScreen", Screen.class);
                method.setAccessible(true);
                method.invoke((Object)gui, screen);
                return;
            }
            catch (NoSuchMethodException ignored) {
                continue;
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
        long elapsedMs = System.currentTimeMillis() - initialOpenTime;
        float shaderProgress = Math.min(1.0f, (float)elapsedMs / 4000.0f);
        float fadeAlpha = 0.0f;
        if (shaderProgress >= 1.0f) {
            long fadeElapsedMs = elapsedMs - 4000L;
            float fadeProgress = Math.min(1.0f, (float)fadeElapsedMs / 800.0f);
            fadeAlpha = 1.0f - (float)Math.pow(1.0f - fadeProgress, 3.0);
        }
        if (fadeAlpha > 0.001f) {
            this.renderSidebar(guiGraphicsExtractor, fadeAlpha);
            for (Entry entry : this.entries) {
                entry.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks, fadeAlpha);
            }
            this.renderFooter(guiGraphicsExtractor, fadeAlpha);
        }
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        long elapsedMs = System.currentTimeMillis() - initialOpenTime;
        float shaderProgress = Math.min(1.0f, (float)elapsedMs / 4000.0f);
        MenuShader.render(guiGraphicsExtractor, this.width, this.height, 1.0f, shaderProgress);
    }

    private void renderSidebar(GuiGraphicsExtractor graphics, float alpha) {
        Renderer2D.renderQuad(graphics, this.sideWidth, 0.0f, this.sideWidth + 1, this.height, new Color(150, 140, 255, MainMenuScreen.clamp255(70.0f * alpha)));
        MainMenuScreen.fillBolt(graphics, 14.0f, 12.0f, 0.42f, new Color(176, 160, 255, MainMenuScreen.clamp255(255.0f * alpha)));
        int fh = Night.FONT_MANAGER.getHeight();
        String title = "Lightning Client";
        int ty = 42;
        if (MainMenuScreen.isRainbow()) {
            Night.FONT_MANAGER.drawRainbowString(graphics, title, 14, ty, 6L);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(graphics, title, 14, ty, new Color(220, 212, 255, MainMenuScreen.clamp255(255.0f * alpha)));
        }
        Night.FONT_MANAGER.drawTextWithShadow(graphics, "fucked by FPG! Nice skidded client", 14, ty + fh + 3, new Color(140, 130, 200, MainMenuScreen.clamp255(220.0f * alpha)));
    }

    private static void fillBolt(GuiGraphicsExtractor graphics, float ox, float oy, float scale, Color color) {
        int pointCount = BOLT.length;
        float step = 0.5f;
        float bottom = 54.0f * scale;
        float[] xs = new float[pointCount];
        for (float yy = 0.0f; yy < bottom; yy += step) {
            int i;
            float yc = (yy + step / 2.0f) / scale;
            int cnt = 0;
            for (i = 0; i < pointCount; ++i) {
                float[] pointA = BOLT[i];
                float[] pointB = BOLT[(i + 1) % pointCount];
                if (!(pointA[1] <= yc && pointB[1] > yc || pointB[1] <= yc && pointA[1] > yc)) continue;
                float interp = (yc - pointA[1]) / (pointB[1] - pointA[1]);
                xs[cnt++] = pointA[0] + interp * (pointB[0] - pointA[0]);
            }
            for (i = 1; i < cnt; ++i) {
                float val = xs[i];
                int j = i - 1;
                while (j >= 0 && xs[j] > val) {
                    xs[j + 1] = xs[j];
                    --j;
                }
                xs[j + 1] = val;
            }
            i = 0;
            while (i + 1 < cnt) {
                Renderer2D.renderQuad(graphics, ox + xs[i] * scale, oy + yy, ox + xs[i + 1] * scale, oy + yy + step, color);
                i += 2;
            }
        }
    }

    private void renderFooter(GuiGraphicsExtractor graphics, float alpha) {
        int fh = Night.FONT_MANAGER.getHeight();
        Renderer2D.renderQuad(graphics, 0.0f, this.height - 36, this.sideWidth, this.height - 35, new Color(150, 140, 255, MainMenuScreen.clamp255(60.0f * alpha)));
        float cy = this.height - 18;
        Renderer2D.renderQuad(graphics, 14.0f, cy - 3.0f, 20.0f, cy + 3.0f, new Color(93, 202, 122, MainMenuScreen.clamp255(255.0f * alpha)));
        String name = Night.ACCOUNT_MANAGER != null ? Night.ACCOUNT_MANAGER.getCurrentName() : "";
        if (name == null) {
            name = "";
        }
        Night.FONT_MANAGER.drawTextWithShadow(graphics, name, 26, (int)cy - fh / 2, new Color(214, 206, 255, MainMenuScreen.clamp255(230.0f * alpha)));
        String tag = "Lightning Client \u00b7 26.2";
        int tw = Night.FONT_MANAGER.getWidth(tag);
        Night.FONT_MANAGER.drawTextWithShadow(graphics, tag, this.width - tw - 8, this.height - fh - 8, new Color(150, 142, 210, MainMenuScreen.clamp255(200.0f * alpha)));
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        long elapsedMs = System.currentTimeMillis() - initialOpenTime;
        if (elapsedMs < 4000L) {
            return false;
        }
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (mouseButtonEvent.button() == 0) {
            for (Entry entry : this.entries) {
                if (!entry.isHovered(mouseX, mouseY)) continue;
                this.playClick();
                entry.action.run();
                return true;
            }
        }
        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
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

    private static Color accentColor(int alpha) {
        return ClickGuiScreen.getButtonColor(0, alpha);
    }

    private static int clamp255(float value) {
        return Math.max(0, Math.min(255, (int)value));
    }

    private static int mixInt(int start, int end, float progress) {
        return (int)((float)start + (float)(end - start) * progress);
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

        private Entry(String label, int x, int y, int width, int height, boolean danger, Runnable action) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.danger = danger;
            this.action = action;
        }

        private boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
        }

        private void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, float alpha) {
            int idx;
            float target = this.isHovered(mouseX, mouseY) ? 1.0f : 0.0f;
            this.hover += (target - this.hover) * 0.2f;
            if (this.hover > 0.01f) {
                Renderer2D.renderQuad(graphics, this.x, this.y, this.x + this.width, this.y + this.height, new Color(120, 95, 230, MainMenuScreen.clamp255(alpha * 48.0f * this.hover)));
                Renderer2D.renderQuad(graphics, this.x, this.y, this.x + 2, this.y + this.height, MainMenuScreen.accentColor(MainMenuScreen.clamp255(alpha * 255.0f * this.hover)));
            }
            String name = (idx = this.label.indexOf("  ")) > 0 ? this.label.substring(idx + 2) : this.label;
            int fh = Night.FONT_MANAGER.getHeight();
            int ty = this.y + (this.height - fh) / 2;
            int tx = this.x + 14 + Math.round(this.hover * 3.0f);
            int alphaByte = MainMenuScreen.clamp255(alpha * 255.0f);
            Color main = this.danger ? new Color(240, 149, 149, alphaByte) : new Color(MainMenuScreen.mixInt(228, 255, this.hover), MainMenuScreen.mixInt(224, 255, this.hover), 255, alphaByte);
            Night.FONT_MANAGER.drawTextWithShadow(graphics, name, tx, ty, main);
        }
    }
}

