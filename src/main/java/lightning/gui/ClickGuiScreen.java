/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 */
package lightning.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lightning.Night;
import lightning.gui.ColorGui;
import lightning.gui.HeaderFx;
import lightning.gui.LightningSkin;
import lightning.gui.api.Button;
import lightning.gui.api.DescriptionFrame;
import lightning.gui.api.Frame;
import lightning.gui.api.PingBypassFrame;
import lightning.gui.api.SearchFrame;
import lightning.gui.impl.BindButton;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.modules.impl.miscellaneous.FightLogModule;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.ClickGuiBackground;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.system.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class ClickGuiScreen
extends Screen {
    private static final Color CARD_BG = new Color(21, 20, 35, 242);
    private static final Color CARD_RING = new Color(255, 255, 255, 22);
    private static final Color CARD_LINE = new Color(255, 255, 255, 20);
    private static final Color INNER_BG = new Color(30, 29, 48, 235);
    private static final Color PURPLE = new Color(132, 90, 255);
    private static final Color PURPLE_LT = new Color(170, 140, 255);
    private static final Color TRACK = new Color(38, 35, 64, 255);
    private static final Color TXT_MAIN = new Color(232, 232, 242);
    private static final Color TXT_DIM = new Color(150, 148, 178);
    private static final Color TXT_SOFT = new Color(200, 198, 222);
    private static final float CARD_R = 8.0f;
    private static final float S_TITLE = 0.85f;
    private static final float S_BODY = 0.78f;
    private static final float S_SMALL = 0.66f;
    private final ArrayList<Frame> frames = new ArrayList();
    private final ArrayList<Button> buttons = new ArrayList();
    private final DescriptionFrame descriptionFrame;
    private final SearchFrame searchFrame;
    private final PingBypassFrame pingBypassFrame;
    private final Timer lineTimer = new Timer();
    private boolean showLine = false;
    private Color colorClipboard = null;
    private float appliedScale = 1.0f;
    private static final int SLIDE_DURATION_MS = 320;
    private final Animation slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
    private boolean closing = false;
    private static final int CATEGORY_STAGGER_MS = 35;
    private boolean readyToRemove = false;
    private static final int PANEL_WIDTH = 100;
    private static final int PANEL_GAP = 10;
    private static final int RESERVE_TOP = 22;
    private static final int RESERVE_BOTTOM = 46;
    private static final int HEADER_H = 22;
    private static final int ROW_H = 16;
    private static final int SCREEN_PADDING = 8;
    private float ovX1 = 0.0f;
    private float ovX2 = 0.0f;
    private float ovY2 = 0.0f;
    private boolean ovShown = false;
    private float pcY2 = 0.0f;
    private float stX1 = 0.0f;
    private float stY1 = 0.0f;
    private float stX2 = 0.0f;
    private float stY2 = 0.0f;
    private boolean stShown = false;
    private static boolean headLogged = false;

    public boolean isReadyToRemove() {
        return this.readyToRemove;
    }

    public void requestClose() {
        this.closing = true;
        this.readyToRemove = false;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void cancelClose() {
        this.closing = false;
        this.readyToRemove = false;
        this.slideAnim.setEasing(Easing.Method.EASE_OUT_CUBIC);
    }

    public ClickGuiScreen() {
        super((Component)Component.literal((String)"night-click-gui"));
        Window window = Minecraft.getInstance().getWindow();
        int n = window != null ? window.getGuiScaledWidth() : 480;
        int n2 = window != null ? window.getGuiScaledHeight() : 270;
        Module.Category[] categoryArray = Module.Category.values();
        int n3 = n2 - 22 - 46;
        int n4 = Math.max(40, n3 - 22 - 4);
        for (Module.Category category : categoryArray) {
            Frame frame0 = new Frame(category, 0, 22, 100, 22);
            frame0.setMaxViewHeight(n4);
            this.frames.add(frame0);
        }
        HeaderFx.fitWidths(this.frames, n, 10);
        int n5 = (this.frames.size() - 1) * 10;
        for (Frame frame : this.frames) {
            n5 += frame.getWidth();
        }
        int n6 = (n - n5) / 2;
        int n7 = 0;
        for (Frame frame : this.frames) {
            int n8 = Math.min(frame.getButtons().size() * 16, n4);
            n7 = Math.max(n7, 22 + n8 + 4);
        }
        int n9 = 22 + Math.max(0, (n3 - n7) / 2);
        for (Frame frame : this.frames) {
            frame.setX(n6);
            frame.setY(n9);
            n6 += frame.getWidth() + 10;
        }
        this.pingBypassFrame = new PingBypassFrame(n6, n9, 100, 13);
        this.descriptionFrame = new DescriptionFrame((n - 200) / 2, n2 - 34, 200, 13);
        this.searchFrame = new SearchFrame();
    }

    private float getSlideDistance() {
        Window window = Minecraft.getInstance().getWindow();
        if (window != null) {
            return (float)window.getGuiScaledWidth() + 30.0f;
        }
        return 500.0f;
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        float f3;
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float f2 = this.slideAnim.get(this.closing ? 0.0f : 1.0f);
        float f4 = f3 = this.frames.isEmpty() ? 0.0f : this.categoryFrameProgress(this.frames.size() - 1);
        if (this.closing && f2 <= 0.005f && f3 <= 0.005f) {
            this.readyToRemove = true;
            Minecraft.getInstance().gui.setScreen(null);
            return;
        }
        this.descriptionFrame.setDescription("");
        String string = this.searchFrame.getQuery();
        float f5 = this.getSlideDistance();
        int n3 = Math.round((float)n / this.appliedScale);
        int n4 = Math.round((float)n2 / this.appliedScale);
        ColorGui.poseHook(guiGraphicsExtractor).pushMatrix();
        guiGraphicsExtractor.pose().scale(this.appliedScale, this.appliedScale);
        for (int i = 0; i < this.frames.size(); ++i) {
            Frame frame = this.frames.get(i);
            float f6 = this.categoryFrameProgress(i);
            float f7 = Mth.lerp((float)f6, (float)(-f5), (float)0.0f);
            guiGraphicsExtractor.pose().pushMatrix();
            guiGraphicsExtractor.pose().translate(f7, 0.0f);
            frame.render(guiGraphicsExtractor, n3, n4, f, string);
            guiGraphicsExtractor.pose().popMatrix();
        }
        float f8 = Mth.lerp((float)f2, (float)(-f5), (float)0.0f);
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate(f8, 0.0f);
        HeaderFx.descRender(HeaderFx.desc(this), guiGraphicsExtractor, n3, n4, f);
        this.searchFrame.render(guiGraphicsExtractor, n3, n4, f);
        this.renderOverview(guiGraphicsExtractor);
        this.renderPlayerCard(guiGraphicsExtractor);
        this.renderSettingsButton(guiGraphicsExtractor, n3, n4);
        this.renderInfoCard(guiGraphicsExtractor);
        this.renderLogo(guiGraphicsExtractor);
        guiGraphicsExtractor.pose().popMatrix();
        guiGraphicsExtractor.pose().popMatrix();
    }

    private void renderLogo(GuiGraphicsExtractor g) {
        try {
            float bs = 13.0f;
            float bx = 10.0f;
            float by = 8.0f;
            LightningSkin.bolt(g, bx, by, bs, 253);
            this.drawScaledText(g, "Lightning - mc26.2", bx + bs * 0.561f + 6.0f, by + 2.0f, 1.2f, Color.WHITE);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private float categoryFrameProgress(int n) {
        long l = System.currentTimeMillis() - this.slideAnim.getStartTime() - (long)n * 35L;
        float f = Mth.clamp((float)((float)l / 320.0f), (float)0.0f, (float)1.0f);
        float f2 = Easing.ease(f, this.closing ? Easing.Method.EASE_IN_CUBIC : Easing.Method.EASE_OUT_CUBIC);
        return this.closing ? 1.0f - f2 : f2;
    }

    private static int overviewRgb(String string) {
        switch (string) {
            case "COMBAT": {
                return 16736126;
            }
            case "PLAYER": {
                return 4906624;
            }
            case "VISUALS": {
                return 10980346;
            }
            case "MOVEMENT": {
                return 3718648;
            }
            case "MISCELLANEOUS": 
            case "MISC": {
                return 16498468;
            }
            case "CORE": {
                return 3003583;
            }
        }
        return 0xBFBFBF;
    }

    private static String overviewLabel(String string) {
        switch (string) {
            case "COMBAT": {
                return "Combat";
            }
            case "PLAYER": {
                return "Player";
            }
            case "VISUALS": {
                return "Visuals";
            }
            case "MOVEMENT": {
                return "Movement";
            }
            case "MISCELLANEOUS": 
            case "MISC": {
                return "Misc";
            }
            case "CORE": {
                return "Core";
            }
        }
        if (string.isEmpty()) {
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    private static float tw(String t, float sc) {
        return (float)Night.FONT_MANAGER.getWidth(t) * sc;
    }

    private static float th(float sc) {
        return (float)Night.FONT_MANAGER.getHeight() * sc;
    }

    private static void tx(GuiGraphicsExtractor g, String t, float x, float y, float sc, Color c, boolean shadow) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(sc, sc);
        if (shadow) {
            Night.FONT_MANAGER.drawTextWithShadow(g, t, 0, 0, c);
        } else {
            Night.FONT_MANAGER.drawText(g, t, 0, 0, c);
        }
        g.pose().popMatrix();
    }

    private static void txB(GuiGraphicsExtractor g, String t, float x, float y, float sc, Color c) {
        ClickGuiScreen.tx(g, t, x, y, sc, c, false);
        ClickGuiScreen.tx(g, t, x + 0.45f, y, sc, c, false);
    }

    private static Color legendColor(String name) {
        switch (name) {
            case "COMBAT": {
                return new Color(138, 80, 255);
            }
            case "PLAYER": {
                return new Color(150, 100, 255);
            }
            case "VISUALS": {
                return new Color(166, 130, 255);
            }
            case "MOVEMENT": {
                return new Color(80, 205, 150);
            }
            case "MISCELLANEOUS": 
            case "MISC": {
                return new Color(255, 188, 135);
            }
        }
        return new Color(160, 115, 255);
    }

    private static void overviewArc(GuiGraphicsExtractor g, float cx, float cy, float ro, float ri, float frac, Color track, Color arc) {
        float top = -1.5707964f;
        float tau = (float)Math.PI * 2;
        LightningSkin.arcQ(cx, cy, ri, ro, top, top + tau, track);
        float f = Math.max(0.0f, Math.min(1.0f, frac));
        if (f > 0.0f) {
            float a1 = top + tau * f;
            LightningSkin.arcQ(cx, cy, ri, ro, top, a1, arc);
            float rm = (ri + ro) * 0.5f;
            float rc = (ro - ri) * 0.5f;
            LightningSkin.arcQ(cx + (float)Math.cos(top) * rm, cy + (float)Math.sin(top) * rm, 0.0f, rc, 0.0f, tau, arc);
            LightningSkin.arcQ(cx + (float)Math.cos(a1) * rm, cy + (float)Math.sin(a1) * rm, 0.0f, rc, 0.0f, tau, arc);
        }
        LightningSkin.commit(g);
    }

    private void renderOverview(GuiGraphicsExtractor g) {
        this.ovShown = false;
        try {
            if (this.frames.isEmpty()) {
                return;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            for (Frame fr : this.frames) {
                minX = Math.min(minX, fr.getX());
                minY = Math.min(minY, fr.getY());
            }
            float avail = (float)minX - 10.0f - 6.0f;
            float w = Math.min(150.0f, avail);
            if (w < 104.0f) {
                return;
            }
            float x1 = (float)minX - 10.0f - w;
            float x2 = x1 + w;
            float y1 = minY;
            Module.Category[] cats = Module.Category.values();
            int nc = cats.length;
            int[] on = new int[nc];
            String[] labels = new String[nc];
            String[] names = new String[nc];
            int sumOn = 0;
            int sumTot = 0;
            for (int i = 0; i < nc; ++i) {
                int tot = 0;
                for (Module m : Night.MODULE_MANAGER.getModules(cats[i])) {
                    ++tot;
                    if (!m.isToggled()) continue;
                    on[i] = on[i] + 1;
                }
                names[i] = cats[i].name();
                labels[i] = ClickGuiScreen.overviewLabel(names[i]);
                sumOn += on[i];
                sumTot += tot;
            }
            float fhB = ClickGuiScreen.th(0.78f);
            float pad = 10.0f;
            int cols = 2;
            int rows = (nc + cols - 1) / cols;
            float colW = (w - pad * 2.0f) / (float)cols;
            float rowH = fhB + 6.0f;
            float ro = 25.0f;
            float ri = 20.5f;
            float logoH = 32.0f;
            float titleY = y1 + logoH + 8.0f;
            float cx = x1 + w / 2.0f;
            float cy = titleY + ClickGuiScreen.th(0.85f) + 8.0f + ro;
            float ly = cy + ro + 10.0f;
            float y2 = ly + rowH * (float)rows + 3.0f;
            this.ovX1 = x1;
            this.ovX2 = x2;
            this.ovY2 = y2;
            this.ovShown = true;
            LightningSkin.rrAll(g, x1, y1, x2, y2, 8.0f, CARD_BG);
            LightningSkin.ring(g, x1, y1, x2, y2, 8.0f, 1.0f, CARD_RING);
            float bs = 21.0f;
            LightningSkin.bolt(g, x1 + pad, y1 + (logoH - bs) / 2.0f, bs, 253);
            float logoSc = 1.0f;
            ClickGuiScreen.txB(g, "LightningClient", x1 + pad + bs * 0.561f + 7.0f, y1 + (logoH - ClickGuiScreen.th(logoSc)) / 2.0f, logoSc, Color.WHITE);
            Renderer2D.renderQuad(g, x1, y1 + logoH, x2, y1 + logoH + 1.0f, CARD_LINE);
            ClickGuiScreen.tx(g, "T\u1ed5ng quan", x1 + pad, titleY, 0.85f, TXT_MAIN, false);
            float frac = sumTot <= 0 ? 0.0f : (float)sumOn / (float)sumTot;
            ClickGuiScreen.overviewArc(g, cx, cy, ro, ri, frac, TRACK, PURPLE);
            String big = String.valueOf(sumOn);
            float sc = 1.45f;
            String sub = "/ " + sumTot;
            float ssc = 0.7f;
            float blockH = ClickGuiScreen.th(sc) + 2.0f + ClickGuiScreen.th(ssc);
            float top = cy - blockH / 2.0f;
            ClickGuiScreen.tx(g, big, cx - ClickGuiScreen.tw(big, sc) / 2.0f, top, sc, Color.WHITE, false);
            ClickGuiScreen.tx(g, sub, cx - ClickGuiScreen.tw(sub, ssc) / 2.0f, top + ClickGuiScreen.th(sc) + 2.0f, ssc, TXT_DIM, false);
            for (int i = 0; i < nc; ++i) {
                int col = i % cols;
                int row = i / cols;
                float lx = x1 + pad + colW * (float)col;
                float ry = ly + rowH * (float)row;
                float dy = ry + (fhB - 6.0f) / 2.0f;
                LightningSkin.rrAll(g, lx, dy, lx + 6.0f, dy + 6.0f, 1.5f, ClickGuiScreen.legendColor(names[i]));
                ClickGuiScreen.tx(g, labels[i], lx + 11.0f, ry, 0.78f, TXT_SOFT, false);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Object rcall(Object o, String m) {
        try {
            if (o == null) {
                return null;
            }
            return o.getClass().getMethod(m, new Class[0]).invoke(o, new Object[0]);
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static Object selfPlayer() {
        try {
            Minecraft m = Minecraft.getInstance();
            return m.getClass().getField("player").get(m);
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static String rname(Object o) {
        Object c = ClickGuiScreen.rcall(o, "getName");
        Object str = ClickGuiScreen.rcall(c, "getString");
        return str == null ? null : String.valueOf(str);
    }

    private static Object rcallOn(String cls, Object o, String m) throws Exception {
        Method mm = Class.forName(cls).getMethod(m, new Class[0]);
        mm.setAccessible(true);
        return mm.invoke(o, new Object[0]);
    }

    private static boolean drawFace(GuiGraphicsExtractor g, Object tex, float x, float y, float size) throws Exception {
        Class<?> pf = Class.forName("net.minecraft.client.gui.components.PlayerFaceExtractor");
        Exception last = null;
        for (Method fm : pf.getMethods()) {
            Class<?>[] pt;
            if (!fm.getName().equals("extractRenderState") || fm.getParameterCount() != 8 || !(pt = fm.getParameterTypes())[1].isInstance(tex)) continue;
            try {
                fm.setAccessible(true);
                fm.invoke(null, g, tex, Math.round(x), Math.round(y), Math.round(size), true, false, -1);
                return true;
            }
            catch (Exception e) {
                last = e;
            }
        }
        if (last != null) {
            throw last;
        }
        return false;
    }

    private void drawHead(GuiGraphicsExtractor g, String name, float x, float y, float size) {
        boolean ok;
        block9: {
            ok = false;
            try {
                Object info;
                Object conn;
                Object skin = null;
                Object me = ClickGuiScreen.selfPlayer();
                if (me != null) {
                    try {
                        Method gs = me.getClass().getMethod("getSkin", new Class[0]);
                        gs.setAccessible(true);
                        skin = gs.invoke(me, new Object[0]);
                    }
                    catch (Throwable t) {
                        skin = null;
                    }
                }
                if (skin == null && (conn = Minecraft.getInstance().getConnection()) != null && name != null && (info = conn.getClass().getMethod("getPlayerInfo", String.class).invoke(conn, name)) != null) {
                    skin = ClickGuiScreen.rcallOn("net.minecraft.client.multiplayer.PlayerInfo", info, "getSkin");
                }
                if (skin != null) {
                    Object body = ClickGuiScreen.rcallOn("net.minecraft.world.entity.player.PlayerSkin", skin, "body");
                    Object tex = ClickGuiScreen.rcallOn("net.minecraft.core.ClientAsset$Texture", body, "texturePath");
                    ok = ClickGuiScreen.drawFace(g, tex, x, y, size);
                }
                if (!ok && !headLogged) {
                    headLogged = true;
                    System.err.println("[LightningGUI] drawHead: khong lay duoc skin (skin=" + String.valueOf(skin) + ")");
                }
            }
            catch (Throwable t) {
                if (headLogged) break block9;
                headLogged = true;
                Throwable c = t.getCause() != null ? t.getCause() : t;
                System.err.println("[LightningGUI] drawHead loi: " + String.valueOf(c));
                c.printStackTrace();
            }
        }
        if (!ok) {
            LightningSkin.rrAll(g, x, y, x + size, y + size, 4.0f, new Color(60, 56, 96, 255));
            String ch = name == null || name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            int cw = Night.FONT_MANAGER.getWidth(ch);
            int fh = Night.FONT_MANAGER.getHeight();
            Night.FONT_MANAGER.drawText(g, ch, Math.round(x + (size - (float)cw) / 2.0f), Math.round(y + (size - (float)fh) / 2.0f), Color.WHITE);
        }
    }

    private void drawScaledText(GuiGraphicsExtractor g, String text, float x, float y, float scale, Color color) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        Night.FONT_MANAGER.drawText(g, text, 0, 0, color);
        g.pose().popMatrix();
    }

    private void renderPlayerCard(GuiGraphicsExtractor g) {
        this.pcY2 = 0.0f;
        try {
            int i;
            Color lastCol;
            Object lastTxt;
            float y2;
            int fit;
            Object al;
            if (!this.ovShown) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            float fhB = ClickGuiScreen.th(0.78f);
            float fhT = ClickGuiScreen.th(0.85f);
            float x1 = this.ovX1;
            float x2 = this.ovX2;
            float w = x2 - x1;
            float y1 = this.ovY2 + 8.0f;
            float pad = 10.0f;
            float inner = w - pad * 2.0f;
            Object me = ClickGuiScreen.selfPlayer();
            String myName = ClickGuiScreen.rname(me);
            if (myName == null || myName.isEmpty()) {
                myName = "Player";
            }
            boolean alive = (al = ClickGuiScreen.rcall(me, "isAlive")) == null || Boolean.TRUE.equals(al);
            List<FightLogModule.Entry> hist = FightLogModule.getHistory();
            int kills = FightLogModule.getKills();
            int lostTotems = FightLogModule.getTotemsLost();
            int survived = FightLogModule.getSurvived();
            float headSize = 24.0f;
            float numScale = 1.05f;
            float statH = 5.0f + ClickGuiScreen.th(numScale) + 1.0f + ClickGuiScreen.th(0.66f) + 5.0f;
            float footH = fhB + 11.0f;
            float rowH = fhB + 4.0f;
            float yTitle = y1 + 8.0f;
            float yHead = yTitle + fhT + 8.0f;
            float yStat = yHead + headSize + 8.0f;
            float yHist = yStat + statH + 9.0f;
            float yRows = yHist + fhB + 4.0f;
            float fixedBottom = 7.0f + footH + 8.0f;
            float scrH = (float)mc.getWindow().getGuiScaledHeight() / Math.max(0.01f, this.appliedScale);
            int want = Math.max(1, Math.min(4, hist.size()));
            int rows = Math.min(want, fit = (int)Math.floor((scrH - 4.0f - yRows - fixedBottom) / rowH));
            if (rows < 1) {
                return;
            }
            float yFoot = yRows + rowH * (float)rows + 7.0f;
            this.pcY2 = y2 = yFoot + footH + 8.0f;
            Color green = new Color(95, 235, 150);
            Color red = new Color(245, 100, 110);
            Color grey = TXT_DIM;
            LightningSkin.rrAll(g, x1, y1, x2, y2, 8.0f, CARD_BG);
            LightningSkin.ring(g, x1, y1, x2, y2, 8.0f, 1.0f, CARD_RING);
            ClickGuiScreen.tx(g, "Ng\u01b0\u1eddi ch\u01a1i", x1 + pad, yTitle, 0.85f, TXT_MAIN, false);
            String st = alive ? "S\u1ed1ng s\u00f3t" : "\u0110\u00e3 ch\u1ebft";
            float pw = ClickGuiScreen.tw(st, 0.78f) + 12.0f;
            float pillH = fhB + 5.0f;
            float px1 = x2 - pad - pw;
            float pillY = yTitle + fhT / 2.0f - pillH / 2.0f;
            LightningSkin.rrAll(g, px1, pillY, px1 + pw, pillY + pillH, 4.0f, alive ? new Color(28, 84, 60, 230) : new Color(100, 35, 45, 230));
            ClickGuiScreen.tx(g, st, px1 + 6.0f, pillY + (pillH - fhB) / 2.0f, 0.78f, alive ? green : red, false);
            this.drawHead(g, myName, x1 + pad, yHead, headSize);
            float tX = x1 + pad + headSize + 7.0f;
            float nameSc = 0.9f;
            float blockTop = yHead + (headSize - (ClickGuiScreen.th(nameSc) + 2.0f + fhB)) / 2.0f;
            ClickGuiScreen.tx(g, myName, tX, blockTop, nameSc, Color.WHITE, false);
            if (FightLogModule.hasLast()) {
                lastTxt = "Tr\u1eadn g\u1ea7n nh\u1ea5t: " + (FightLogModule.isLastWin() ? "th\u1eafng" : "thua");
                lastCol = FightLogModule.isLastWin() ? green : red;
            } else {
                lastTxt = "Ch\u01b0a c\u00f3 tr\u1eadn n\u00e0o";
                lastCol = grey;
            }
            ClickGuiScreen.tx(g, (String)lastTxt, tX, blockTop + ClickGuiScreen.th(nameSc) + 2.0f, 0.78f, lastCol, false);
            float gapB = 5.0f;
            float bw = (inner - gapB * 2.0f) / 3.0f;
            String[] vals = new String[]{String.valueOf(kills), String.valueOf(lostTotems), String.valueOf(survived)};
            String[] lbls = new String[]{"H\u1ea1 g\u1ee5c", "Totem m\u1ea5t", "S\u1ed1ng s\u00f3t"};
            for (i = 0; i < 3; ++i) {
                float bx1 = x1 + pad + (bw + gapB) * (float)i;
                LightningSkin.rrAll(g, bx1, yStat, bx1 + bw, yStat + statH, 4.0f, INNER_BG);
                LightningSkin.ring(g, bx1, yStat, bx1 + bw, yStat + statH, 4.0f, 1.0f, CARD_RING);
                ClickGuiScreen.tx(g, vals[i], bx1 + (bw - ClickGuiScreen.tw(vals[i], numScale)) / 2.0f, yStat + 5.0f, numScale, Color.WHITE, false);
                float ls = 0.66f;
                if (ClickGuiScreen.tw(lbls[i], ls) > bw - 6.0f) {
                    ls = (bw - 6.0f) / (float)Night.FONT_MANAGER.getWidth(lbls[i]);
                }
                ClickGuiScreen.tx(g, lbls[i], bx1 + (bw - ClickGuiScreen.tw(lbls[i], ls)) / 2.0f, yStat + 5.0f + ClickGuiScreen.th(numScale) + 1.0f, ls, grey, false);
            }
            ClickGuiScreen.tx(g, "L\u1ecbch s\u1eed tr\u1eadn", x1 + pad, yHist, 0.78f, TXT_MAIN, false);
            if (hist.isEmpty()) {
                ClickGuiScreen.tx(g, "Ch\u01b0a c\u00f3 tr\u1eadn n\u00e0o", x1 + pad, yRows, 0.78f, grey, false);
            } else {
                for (i = 0; i < rows && i < hist.size(); ++i) {
                    FightLogModule.Entry e = hist.get(i);
                    float ry = yRows + rowH * (float)i;
                    Color cc = e.win ? green : red;
                    float dy = ry + (fhB - 4.0f) / 2.0f;
                    LightningSkin.rrAll(g, x1 + pad, dy, x1 + pad + 4.0f, dy + 4.0f, 2.0f, cc);
                    String right = (e.win ? "H\u1ea1 g\u1ee5c" : "B\u1ecb h\u1ea1") + " -" + e.totems;
                    float rw = ClickGuiScreen.tw(right, 0.78f);
                    ClickGuiScreen.tx(g, right, x2 - pad - rw, ry, 0.78f, cc, false);
                    String nm = e.name == null ? "?" : e.name;
                    float maxW = inner - 9.0f - rw - 6.0f;
                    while (nm.length() > 2 && ClickGuiScreen.tw(nm, 0.78f) > maxW) {
                        nm = nm.substring(0, nm.length() - 1);
                    }
                    ClickGuiScreen.tx(g, nm, x1 + pad + 9.0f, ry, 0.78f, TXT_MAIN, false);
                }
            }
            LightningSkin.rrAll(g, x1 + pad, yFoot, x2 - pad, yFoot + footH, 5.0f, new Color(76, 54, 190, 240));
            LightningSkin.ring(g, x1 + pad, yFoot, x2 - pad, yFoot + footH, 5.0f, 1.0f, new Color(150, 125, 255, 110));
            String ft = "\u0110\u00e3 l\u01b0u v\u00e0o module FightLog";
            float fs = 0.78f;
            float maxFt = inner - 12.0f;
            if (ClickGuiScreen.tw(ft, fs) > maxFt) {
                fs = maxFt / (float)Night.FONT_MANAGER.getWidth(ft);
            }
            ClickGuiScreen.tx(g, ft, x1 + (w - ClickGuiScreen.tw(ft, fs)) / 2.0f, yFoot + (footH - ClickGuiScreen.th(fs)) / 2.0f, fs, Color.WHITE, false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void renderSettingsButton(GuiGraphicsExtractor g, int mx, int my) {
        this.stShown = false;
        try {
            if (!this.ovShown) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            float fhB = ClickGuiScreen.th(0.78f);
            float scrH = (float)mc.getWindow().getGuiScaledHeight() / Math.max(0.01f, this.appliedScale);
            String icon = "\u2699";
            String label = "Settings";
            float h = fhB + 12.0f;
            float w = Math.min(this.ovX2 - this.ovX1, 10.0f + ClickGuiScreen.tw(icon, 0.78f) + 7.0f + ClickGuiScreen.tw(label, 0.78f) + 12.0f);
            float base = this.pcY2 > 0.0f ? this.pcY2 : this.ovY2;
            float y1 = Math.max(base + 8.0f, scrH - 14.0f - h);
            if (y1 + h > scrH - 2.0f) {
                return;
            }
            float x1 = this.ovX1;
            float x2 = x1 + w;
            float y2 = y1 + h;
            boolean hover = (float)mx >= x1 && (float)mx <= x2 && (float)my >= y1 && (float)my <= y2;
            LightningSkin.rrAll(g, x1, y1, x2, y2, 7.0f, hover ? new Color(34, 32, 54, 244) : CARD_BG);
            LightningSkin.ring(g, x1, y1, x2, y2, 7.0f, 1.0f, CARD_RING);
            float ty = y1 + (h - fhB) / 2.0f;
            ClickGuiScreen.tx(g, icon, x1 + 10.0f, ty, 0.78f, PURPLE_LT, false);
            ClickGuiScreen.tx(g, label, x1 + 10.0f + ClickGuiScreen.tw(icon, 0.78f) + 7.0f, ty, 0.78f, Color.WHITE, false);
            this.stX1 = x1;
            this.stY1 = y1;
            this.stX2 = x2;
            this.stY2 = y2;
            this.stShown = true;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void onSettingsClicked() {
    }

    private void renderInfoCard(GuiGraphicsExtractor g) {
        try {
            if (this.frames.isEmpty()) {
                return;
            }
            Frame core = this.frames.get(this.frames.size() - 1);
            Minecraft mc = Minecraft.getInstance();
            float fhB = ClickGuiScreen.th(0.78f);
            String name = "LightningClient";
            Object server = "Singleplayer";
            Object ping = "--";
            int pingVal = -1;
            try {
                Object ipVal;
                Object sd = mc.getCurrentServer();
                if (sd != null && (ipVal = sd.getClass().getField("ip").get(sd)) != null) {
                    server = String.valueOf(ipVal);
                }
            }
            catch (Throwable sd) {
                // empty catch block
            }
            try {
                Object conn = mc.getConnection();
                Object player = ClickGuiScreen.selfPlayer();
                if (conn != null && player != null) {
                    Object uuid = player.getClass().getMethod("getUUID", new Class[0]).invoke(player, new Object[0]);
                    Object info = conn.getClass().getMethod("getPlayerInfo", UUID.class).invoke(conn, uuid);
                    if (info != null) {
                        Object lat = info.getClass().getMethod("getLatency", new Class[0]).invoke(info, new Object[0]);
                        pingVal = ((Number)lat).intValue();
                        ping = pingVal + "ms";
                    }
                }
            }
            catch (Throwable conn) {
                // empty catch block
            }
            try {
                int pg;
                Object sv;
                String svo = Night.SERVER_MANAGER.getServer();
                Object object = sv = svo == null ? null : String.valueOf(svo);
                if (sv != null && !((String)sv).isEmpty()) {
                    server = sv;
                }
                if ((pg = Night.SERVER_MANAGER.getPing()) > 0) {
                    pingVal = pg;
                    ping = pg + "ms";
                }
            }
            catch (Throwable svo) {
                // empty catch block
            }
            int enabled = 0;
            for (Module.Category cat : Module.Category.values()) {
                for (Module m : Night.MODULE_MANAGER.getModules(cat)) {
                    if (!m.isToggled()) continue;
                    ++enabled;
                }
            }
            float x1 = core.getX();
            float w = core.getWidth();
            float x2 = x1 + w;
            float y1 = core.getY() + core.getHeight() + Math.min(core.getButtons().size() * 16, core.getMaxViewHeight()) + 4 + 8;
            float rowH = fhB + 4.0f;
            float iconS = 22.0f;
            float pad = 9.0f;
            float h = 9.0f + iconS + 9.0f + rowH * 2.0f + 6.0f;
            float y2 = y1 + h;
            float scrH = (float)mc.getWindow().getGuiScaledHeight() / Math.max(0.01f, this.appliedScale);
            if (y2 > scrH - 4.0f) {
                return;
            }
            Color grey = TXT_DIM;
            LightningSkin.rrAll(g, x1, y1, x2, y2, 8.0f, CARD_BG);
            LightningSkin.ring(g, x1, y1, x2, y2, 8.0f, 1.0f, CARD_RING);
            float ix1 = x1 + pad;
            float iy1 = y1 + 9.0f;
            try {
                LightningSkin.bolt(g, ix1, iy1, iconS, 253);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            float tX = ix1 + iconS * 0.561f + 8.0f;
            float nameSc = 0.9f;
            float blockTop = iy1 + (iconS - (ClickGuiScreen.th(nameSc) + 2.0f + fhB)) / 2.0f;
            ClickGuiScreen.tx(g, name, tX, blockTop, nameSc, Color.WHITE, false);
            Object srv = server;
            float maxW = x2 - 6.0f - tX;
            while (((String)srv).length() > 3 && ClickGuiScreen.tw((String)srv, 0.78f) > maxW) {
                srv = ((String)srv).substring(0, ((String)srv).length() - 1);
            }
            ClickGuiScreen.tx(g, (String)srv, tX, blockTop + ClickGuiScreen.th(nameSc) + 2.0f, 0.78f, grey, false);
            float ry = iy1 + iconS + 9.0f;
            Color pc = pingVal < 0 ? grey : (pingVal <= 80 ? new Color(60, 235, 150) : (pingVal <= 160 ? new Color(240, 200, 70) : new Color(240, 90, 90)));
            ClickGuiScreen.tx(g, "Ping", x1 + pad, ry, 0.78f, grey, false);
            ClickGuiScreen.tx(g, (String)ping, x2 - pad - ClickGuiScreen.tw((String)ping, 0.78f), ry, 0.78f, pc, false);
            float ry2 = ry + rowH;
            ClickGuiScreen.tx(g, "B\u1eadt", x1 + pad, ry2, 0.78f, grey, false);
            String en = String.valueOf(enabled);
            ClickGuiScreen.tx(g, en, x2 - pad - ClickGuiScreen.tw(en, 0.78f), ry2, 0.78f, Color.WHITE, false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double d2) {
        double d3 = mouseButtonEvent.x() / (double)this.appliedScale;
        double d4 = mouseButtonEvent.y() / (double)this.appliedScale;
        int n = mouseButtonEvent.button();
        for (Frame frame : this.frames) {
            frame.mouseDragged(d3, d4, n, d / (double)this.appliedScale, d2 / (double)this.appliedScale);
        }
        return super.mouseDragged(mouseButtonEvent, d, d2);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        int n;
        double d;
        double d2 = mouseButtonEvent.x() / (double)this.appliedScale;
        if (this.searchFrame.mouseClicked(d2, d = mouseButtonEvent.y() / (double)this.appliedScale, n = mouseButtonEvent.button())) {
            return true;
        }
        if (n == 0 && this.stShown && d2 >= (double)this.stX1 && d2 <= (double)this.stX2 && d >= (double)this.stY1 && d <= (double)this.stY2) {
            this.onSettingsClicked();
            return true;
        }
        for (Frame frame : this.frames) {
            frame.mouseClicked(d2, d, n);
        }
        this.descriptionFrame.mouseClicked(d2, d, n);
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        double d = mouseButtonEvent.x() / (double)this.appliedScale;
        double d2 = mouseButtonEvent.y() / (double)this.appliedScale;
        int n = mouseButtonEvent.button();
        for (Frame frame : this.frames) {
            frame.mouseReleased(d, d2, n);
        }
        this.descriptionFrame.mouseReleased(d, d2, n);
        return super.mouseReleased(mouseButtonEvent);
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        double d5 = d / (double)this.appliedScale;
        double d6 = d2 / (double)this.appliedScale;
        for (Frame frame : this.frames) {
            frame.mouseScrolled(d5, d6, d3, d4);
        }
        return false;
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int n = keyEvent.key();
        int n2 = keyEvent.scancode();
        int n3 = keyEvent.modifiers();
        if (n == 256 && !BindButton.isAnyListening()) {
            this.requestClose();
            return true;
        }
        if (n == Night.MODULE_MANAGER.getModule(ClickGuiModule.class).getBind()) {
            if (this.closing) {
                this.cancelClose();
            }
            return true;
        }
        boolean bl = InputConstants.isKeyDown((Window)Minecraft.getInstance().getWindow(), (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (bl && n == 70) {
            this.searchFrame.toggle();
            return true;
        }
        if (this.searchFrame.keyPressed(n, n2, n3)) {
            return true;
        }
        for (Frame frame : this.frames) {
            frame.keyPressed(n, n2, n3);
        }
        if (n == 256) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char c = (char)characterEvent.codepoint();
        int n = 0;
        if (this.searchFrame.charTyped(c, 0)) {
            return true;
        }
        for (Frame frame : this.frames) {
            frame.charTyped(c, n);
        }
        return super.charTyped(characterEvent);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (clickGuiModule.blur.getValue()) {
            guiGraphicsExtractor.blurBeforeThisStratum();
        }
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, sw, sh, new Color(8, 7, 14, 215), new Color(22, 15, 40, 205));
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, (float)sh * 0.55f, sw, sh, new Color(110, 60, 200, 0), new Color(120, 66, 220, 22));
        String string = clickGuiModule.background.getValue();
        int n3 = Arrays.asList(ClickGuiModule.BG_MODES).indexOf(string);
        if (n3 >= 0 && n3 < ClickGuiModule.BG_MODES.length - 1) {
            float f2 = clickGuiModule.bgSpeed.getValue().floatValue();
            float f3 = clickGuiModule.bgOpacity.getValue().floatValue() / 100.0f;
            Color color = clickGuiModule.color.getColor();
            boolean bl = clickGuiModule.isRainbow();
            ClickGuiBackground.renderBackground(guiGraphicsExtractor, sw, sh, n3, f2, f3, color, bl);
        }
    }

    public void removed() {
        this.searchFrame.setQuery("");
        this.searchFrame.setCursorIndex(0);
        this.searchFrame.setFocused(false);
        this.searchFrame.setVisible(false);
        super.removed();
        Night.MODULE_MANAGER.getModule(ClickGuiModule.class).setToggled(false);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public static Color getButtonColor(int n, int n2) {
        Color color = Night.MODULE_MANAGER.getModule(ClickGuiModule.class).isRainbow() ? ColorUtils.getOffsetRainbow((long)n * 2L) : ColorGui.accent(n);
        return ColorUtils.getColor(color, n2);
    }

    public ArrayList<Frame> getFrames() {
        return this.frames;
    }

    public ArrayList<Button> getButtons() {
        return this.buttons;
    }

    public DescriptionFrame getDescriptionFrame() {
        return this.descriptionFrame;
    }

    public SearchFrame getSearchFrame() {
        return this.searchFrame;
    }

    public PingBypassFrame getPingBypassFrame() {
        return this.pingBypassFrame;
    }

    public Timer getLineTimer() {
        return this.lineTimer;
    }

    public boolean isShowLine() {
        return this.showLine;
    }

    public Color getColorClipboard() {
        return this.colorClipboard;
    }

    public float getAppliedScale() {
        return this.appliedScale;
    }

    public Animation getSlideAnim() {
        return this.slideAnim;
    }

    public boolean isClosing() {
        return this.closing;
    }

    public void setShowLine(boolean bl) {
        this.showLine = bl;
    }

    public void setColorClipboard(Color color) {
        this.colorClipboard = color;
    }

    public void setAppliedScale(float f) {
        this.appliedScale = f;
    }

    public void setClosing(boolean bl) {
        this.closing = bl;
    }

    public void setReadyToRemove(boolean bl) {
        this.readyToRemove = bl;
    }
}

