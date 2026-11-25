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
        int screenWidth = window != null ? window.getGuiScaledWidth() : 480;
        int screenHeight = window != null ? window.getGuiScaledHeight() : 270;
        Module.Category[] categories = Module.Category.values();
        int availableHeight = screenHeight - 22 - 46;
        int maxViewHeight = Math.max(40, availableHeight - 22 - 4);
        for (Module.Category category : categories) {
            Frame frame = new Frame(category, 0, 22, 100, 22);
            frame.setMaxViewHeight(maxViewHeight);
            this.frames.add(frame);
        }
        HeaderFx.fitWidths(this.frames, screenWidth, 10);
        int totalWidth = (this.frames.size() - 1) * 10;
        for (Frame frame : this.frames) {
            totalWidth += frame.getWidth();
        }
        int startX = (screenWidth - totalWidth) / 2;
        int maxFrameHeight = 0;
        for (Frame frame : this.frames) {
            int frameViewHeight = Math.min(frame.getButtons().size() * 16, maxViewHeight);
            maxFrameHeight = Math.max(maxFrameHeight, 22 + frameViewHeight + 4);
        }
        int startY = 22 + Math.max(0, (availableHeight - maxFrameHeight) / 2);
        for (Frame frame : this.frames) {
            frame.setX(startX);
            frame.setY(startY);
            startX += frame.getWidth() + 10;
        }
        this.pingBypassFrame = new PingBypassFrame(startX, startY, 100, 13);
        this.descriptionFrame = new DescriptionFrame((screenWidth - 200) / 2, screenHeight - 34, 200, 13);
        this.searchFrame = new SearchFrame();
    }

    private float getSlideDistance() {
        Window window = Minecraft.getInstance().getWindow();
        if (window != null) {
            return (float)window.getGuiScaledWidth() + 30.0f;
        }
        return 500.0f;
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float slideProgress = this.slideAnim.get(this.closing ? 0.0f : 1.0f);
        float lastFrameProgress = this.frames.isEmpty() ? 0.0f : this.categoryFrameProgress(this.frames.size() - 1);
        if (this.closing && slideProgress <= 0.005f && lastFrameProgress <= 0.005f) {
            this.readyToRemove = true;
            Minecraft.getInstance().gui.setScreen(null);
            return;
        }
        this.descriptionFrame.setDescription("");
        String searchQuery = this.searchFrame.getQuery();
        float slideDistance = this.getSlideDistance();
        int scaledMouseX = Math.round((float)mouseX / this.appliedScale);
        int scaledMouseY = Math.round((float)mouseY / this.appliedScale);
        ColorGui.poseHook(guiGraphicsExtractor).pushMatrix();
        guiGraphicsExtractor.pose().scale(this.appliedScale, this.appliedScale);
        for (int i = 0; i < this.frames.size(); ++i) {
            Frame frame = this.frames.get(i);
            float frameProgress = this.categoryFrameProgress(i);
            float frameOffsetX = Mth.lerp((float)frameProgress, (float)(-slideDistance), (float)0.0f);
            guiGraphicsExtractor.pose().pushMatrix();
            guiGraphicsExtractor.pose().translate(frameOffsetX, 0.0f);
            frame.render(guiGraphicsExtractor, scaledMouseX, scaledMouseY, partialTicks, searchQuery);
            guiGraphicsExtractor.pose().popMatrix();
        }
        float slideOffsetX = Mth.lerp((float)slideProgress, (float)(-slideDistance), (float)0.0f);
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate(slideOffsetX, 0.0f);
        HeaderFx.descRender(HeaderFx.desc(this), guiGraphicsExtractor, scaledMouseX, scaledMouseY, partialTicks);
        this.searchFrame.render(guiGraphicsExtractor, scaledMouseX, scaledMouseY, partialTicks);
        this.renderOverview(guiGraphicsExtractor);
        this.renderPlayerCard(guiGraphicsExtractor);
        this.renderSettingsButton(guiGraphicsExtractor, scaledMouseX, scaledMouseY);
        this.renderInfoCard(guiGraphicsExtractor);
        this.renderLogo(guiGraphicsExtractor);
        guiGraphicsExtractor.pose().popMatrix();
        guiGraphicsExtractor.pose().popMatrix();
    }

    private void renderLogo(GuiGraphicsExtractor graphics) {
        try {
            float bs = 13.0f;
            float bx = 10.0f;
            float by = 8.0f;
            LightningSkin.bolt(graphics, bx, by, bs, 253);
            this.drawScaledText(graphics, "Lightning - mc26.2", bx + bs * 0.561f + 6.0f, by + 2.0f, 1.2f, Color.WHITE);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private float categoryFrameProgress(int index) {
        long elapsedMs = System.currentTimeMillis() - this.slideAnim.getStartTime() - (long)index * 35L;
        float progress = Mth.clamp((float)((float)elapsedMs / 320.0f), (float)0.0f, (float)1.0f);
        float eased = Easing.ease(progress, this.closing ? Easing.Method.EASE_IN_CUBIC : Easing.Method.EASE_OUT_CUBIC);
        return this.closing ? 1.0f - eased : eased;
    }

    private static int overviewRgb(String categoryName) {
        switch (categoryName) {
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

    private static String overviewLabel(String categoryName) {
        switch (categoryName) {
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
        if (categoryName.isEmpty()) {
            return categoryName;
        }
        return categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1).toLowerCase();
    }

    private static float tw(String text, float scale) {
        return (float)Night.FONT_MANAGER.getWidth(text) * scale;
    }

    private static float th(float scale) {
        return (float)Night.FONT_MANAGER.getHeight() * scale;
    }

    private static void tx(GuiGraphicsExtractor graphics, String text, float x, float y, float scale, Color color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        if (shadow) {
            Night.FONT_MANAGER.drawTextWithShadow(graphics, text, 0, 0, color);
        } else {
            Night.FONT_MANAGER.drawText(graphics, text, 0, 0, color);
        }
        graphics.pose().popMatrix();
    }

    private static void txB(GuiGraphicsExtractor graphics, String text, float x, float y, float scale, Color color) {
        ClickGuiScreen.tx(graphics, text, x, y, scale, color, false);
        ClickGuiScreen.tx(graphics, text, x + 0.45f, y, scale, color, false);
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

    private static void overviewArc(GuiGraphicsExtractor graphics, float cx, float cy, float ro, float ri, float frac, Color track, Color arc) {
        float top = -1.5707964f;
        float tau = (float)Math.PI * 2;
        LightningSkin.arcQ(cx, cy, ri, ro, top, top + tau, track);
        float clampedFrac = Math.max(0.0f, Math.min(1.0f, frac));
        if (clampedFrac > 0.0f) {
            float a1 = top + tau * clampedFrac;
            LightningSkin.arcQ(cx, cy, ri, ro, top, a1, arc);
            float rm = (ri + ro) * 0.5f;
            float rc = (ro - ri) * 0.5f;
            LightningSkin.arcQ(cx + (float)Math.cos(top) * rm, cy + (float)Math.sin(top) * rm, 0.0f, rc, 0.0f, tau, arc);
            LightningSkin.arcQ(cx + (float)Math.cos(a1) * rm, cy + (float)Math.sin(a1) * rm, 0.0f, rc, 0.0f, tau, arc);
        }
        LightningSkin.commit(graphics);
    }

    private void renderOverview(GuiGraphicsExtractor graphics) {
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
                for (Module module : Night.MODULE_MANAGER.getModules(cats[i])) {
                    ++tot;
                    if (!module.isToggled()) continue;
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
            LightningSkin.rrAll(graphics, x1, y1, x2, y2, 8.0f, CARD_BG);
            LightningSkin.ring(graphics, x1, y1, x2, y2, 8.0f, 1.0f, CARD_RING);
            float bs = 21.0f;
            LightningSkin.bolt(graphics, x1 + pad, y1 + (logoH - bs) / 2.0f, bs, 253);
            float logoSc = 1.0f;
            ClickGuiScreen.txB(graphics, "LightningClient", x1 + pad + bs * 0.561f + 7.0f, y1 + (logoH - ClickGuiScreen.th(logoSc)) / 2.0f, logoSc, Color.WHITE);
            Renderer2D.renderQuad(graphics, x1, y1 + logoH, x2, y1 + logoH + 1.0f, CARD_LINE);
            ClickGuiScreen.tx(graphics, "T\u1ed5ng quan", x1 + pad, titleY, 0.85f, TXT_MAIN, false);
            float frac = sumTot <= 0 ? 0.0f : (float)sumOn / (float)sumTot;
            ClickGuiScreen.overviewArc(graphics, cx, cy, ro, ri, frac, TRACK, PURPLE);
            String big = String.valueOf(sumOn);
            float sc = 1.45f;
            String sub = "/ " + sumTot;
            float ssc = 0.7f;
            float blockH = ClickGuiScreen.th(sc) + 2.0f + ClickGuiScreen.th(ssc);
            float top = cy - blockH / 2.0f;
            ClickGuiScreen.tx(graphics, big, cx - ClickGuiScreen.tw(big, sc) / 2.0f, top, sc, Color.WHITE, false);
            ClickGuiScreen.tx(graphics, sub, cx - ClickGuiScreen.tw(sub, ssc) / 2.0f, top + ClickGuiScreen.th(sc) + 2.0f, ssc, TXT_DIM, false);
            for (int i = 0; i < nc; ++i) {
                int col = i % cols;
                int row = i / cols;
                float lx = x1 + pad + colW * (float)col;
                float ry = ly + rowH * (float)row;
                float dy = ry + (fhB - 6.0f) / 2.0f;
                LightningSkin.rrAll(graphics, lx, dy, lx + 6.0f, dy + 6.0f, 1.5f, ClickGuiScreen.legendColor(names[i]));
                ClickGuiScreen.tx(graphics, labels[i], lx + 11.0f, ry, 0.78f, TXT_SOFT, false);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Object rcall(Object target, String methodName) {
        try {
            if (target == null) {
                return null;
            }
            return target.getClass().getMethod(methodName, new Class[0]).invoke(target, new Object[0]);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static Object selfPlayer() {
        try {
            Minecraft mc = Minecraft.getInstance();
            return mc.getClass().getField("player").get(mc);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static String rname(Object entity) {
        Object nameComp = ClickGuiScreen.rcall(entity, "getName");
        Object str = ClickGuiScreen.rcall(nameComp, "getString");
        return str == null ? null : String.valueOf(str);
    }

    private static Object rcallOn(String className, Object target, String methodName) throws Exception {
        Method method = Class.forName(className).getMethod(methodName, new Class[0]);
        method.setAccessible(true);
        return method.invoke(target, new Object[0]);
    }

    private static boolean drawFace(GuiGraphicsExtractor graphics, Object tex, float x, float y, float size) throws Exception {
        Class<?> faceClass = Class.forName("net.minecraft.client.gui.components.PlayerFaceExtractor");
        Exception last = null;
        for (Method method : faceClass.getMethods()) {
            Class<?>[] paramTypes;
            if (!method.getName().equals("extractRenderState") || method.getParameterCount() != 8 || !(paramTypes = method.getParameterTypes())[1].isInstance(tex)) continue;
            try {
                method.setAccessible(true);
                method.invoke(null, graphics, tex, Math.round(x), Math.round(y), Math.round(size), true, false, -1);
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

    private void drawHead(GuiGraphicsExtractor graphics, String name, float x, float y, float size) {
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
                    catch (Throwable ignored) {
                        skin = null;
                    }
                }
                if (skin == null && (conn = Minecraft.getInstance().getConnection()) != null && name != null && (info = conn.getClass().getMethod("getPlayerInfo", String.class).invoke(conn, name)) != null) {
                    skin = ClickGuiScreen.rcallOn("net.minecraft.client.multiplayer.PlayerInfo", info, "getSkin");
                }
                if (skin != null) {
                    Object body = ClickGuiScreen.rcallOn("net.minecraft.world.entity.player.PlayerSkin", skin, "body");
                    Object tex = ClickGuiScreen.rcallOn("net.minecraft.core.ClientAsset$Texture", body, "texturePath");
                    ok = ClickGuiScreen.drawFace(graphics, tex, x, y, size);
                }
                if (!ok && !headLogged) {
                    headLogged = true;
                    System.err.println("[LightningGUI] drawHead: khong lay duoc skin (skin=" + String.valueOf(skin) + ")");
                }
            }
            catch (Throwable throwable) {
                if (headLogged) break block9;
                headLogged = true;
                Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                System.err.println("[LightningGUI] drawHead loi: " + String.valueOf(cause));
                cause.printStackTrace();
            }
        }
        if (!ok) {
            LightningSkin.rrAll(graphics, x, y, x + size, y + size, 4.0f, new Color(60, 56, 96, 255));
            String ch = name == null || name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            int cw = Night.FONT_MANAGER.getWidth(ch);
            int fh = Night.FONT_MANAGER.getHeight();
            Night.FONT_MANAGER.drawText(graphics, ch, Math.round(x + (size - (float)cw) / 2.0f), Math.round(y + (size - (float)fh) / 2.0f), Color.WHITE);
        }
    }

    private void drawScaledText(GuiGraphicsExtractor graphics, String text, float x, float y, float scale, Color color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        Night.FONT_MANAGER.drawText(graphics, text, 0, 0, color);
        graphics.pose().popMatrix();
    }

    private void renderPlayerCard(GuiGraphicsExtractor graphics) {
        this.pcY2 = 0.0f;
        try {
            int i;
            Color lastCol;
            String lastTxt;
            float y2;
            int fitRows;
            Object aliveObj;
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
            boolean alive = (aliveObj = ClickGuiScreen.rcall(me, "isAlive")) == null || Boolean.TRUE.equals(aliveObj);
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
            int rows = Math.min(want, fitRows = (int)Math.floor((scrH - 4.0f - yRows - fixedBottom) / rowH));
            if (rows < 1) {
                return;
            }
            float yFoot = yRows + rowH * (float)rows + 7.0f;
            this.pcY2 = y2 = yFoot + footH + 8.0f;
            Color green = new Color(95, 235, 150);
            Color red = new Color(245, 100, 110);
            Color grey = TXT_DIM;
            LightningSkin.rrAll(graphics, x1, y1, x2, y2, 8.0f, CARD_BG);
            LightningSkin.ring(graphics, x1, y1, x2, y2, 8.0f, 1.0f, CARD_RING);
            ClickGuiScreen.tx(graphics, "Ng\u01b0\u1eddi ch\u01a1i", x1 + pad, yTitle, 0.85f, TXT_MAIN, false);
            String st = alive ? "S\u1ed1ng s\u00f3t" : "\u0110\u00e3 ch\u1ebft";
            float pw = ClickGuiScreen.tw(st, 0.78f) + 12.0f;
            float pillH = fhB + 5.0f;
            float px1 = x2 - pad - pw;
            float pillY = yTitle + fhT / 2.0f - pillH / 2.0f;
            LightningSkin.rrAll(graphics, px1, pillY, px1 + pw, pillY + pillH, 4.0f, alive ? new Color(28, 84, 60, 230) : new Color(100, 35, 45, 230));
            ClickGuiScreen.tx(graphics, st, px1 + 6.0f, pillY + (pillH - fhB) / 2.0f, 0.78f, alive ? green : red, false);
            this.drawHead(graphics, myName, x1 + pad, yHead, headSize);
            float tX = x1 + pad + headSize + 7.0f;
            float nameSc = 0.9f;
            float blockTop = yHead + (headSize - (ClickGuiScreen.th(nameSc) + 2.0f + fhB)) / 2.0f;
            ClickGuiScreen.tx(graphics, myName, tX, blockTop, nameSc, Color.WHITE, false);
            if (FightLogModule.hasLast()) {
                lastTxt = "Tr\u1eadn g\u1ea7n nh\u1ea5t: " + (FightLogModule.isLastWin() ? "th\u1eafng" : "thua");
                lastCol = FightLogModule.isLastWin() ? green : red;
            } else {
                lastTxt = "Ch\u01b0a c\u00f3 tr\u1eadn n\u00e0o";
                lastCol = grey;
            }
            ClickGuiScreen.tx(graphics, lastTxt, tX, blockTop + ClickGuiScreen.th(nameSc) + 2.0f, 0.78f, lastCol, false);
            float gapB = 5.0f;
            float bw = (inner - gapB * 2.0f) / 3.0f;
            String[] vals = new String[]{String.valueOf(kills), String.valueOf(lostTotems), String.valueOf(survived)};
            String[] lbls = new String[]{"H\u1ea1 g\u1ee5c", "Totem m\u1ea5t", "S\u1ed1ng s\u00f3t"};
            for (i = 0; i < 3; ++i) {
                float bx1 = x1 + pad + (bw + gapB) * (float)i;
                LightningSkin.rrAll(graphics, bx1, yStat, bx1 + bw, yStat + statH, 4.0f, INNER_BG);
                LightningSkin.ring(graphics, bx1, yStat, bx1 + bw, yStat + statH, 4.0f, 1.0f, CARD_RING);
                ClickGuiScreen.tx(graphics, vals[i], bx1 + (bw - ClickGuiScreen.tw(vals[i], numScale)) / 2.0f, yStat + 5.0f, numScale, Color.WHITE, false);
                float ls = 0.66f;
                if (ClickGuiScreen.tw(lbls[i], ls) > bw - 6.0f) {
                    ls = (bw - 6.0f) / (float)Night.FONT_MANAGER.getWidth(lbls[i]);
                }
                ClickGuiScreen.tx(graphics, lbls[i], bx1 + (bw - ClickGuiScreen.tw(lbls[i], ls)) / 2.0f, yStat + 5.0f + ClickGuiScreen.th(numScale) + 1.0f, ls, grey, false);
            }
            ClickGuiScreen.tx(graphics, "L\u1ecbch s\u1eed tr\u1eadn", x1 + pad, yHist, 0.78f, TXT_MAIN, false);
            if (hist.isEmpty()) {
                ClickGuiScreen.tx(graphics, "Ch\u01b0a c\u00f3 tr\u1eadn n\u00e0o", x1 + pad, yRows, 0.78f, grey, false);
            } else {
                for (i = 0; i < rows && i < hist.size(); ++i) {
                    FightLogModule.Entry entry = hist.get(i);
                    float ry = yRows + rowH * (float)i;
                    Color cc = entry.win ? green : red;
                    float dy = ry + (fhB - 4.0f) / 2.0f;
                    LightningSkin.rrAll(graphics, x1 + pad, dy, x1 + pad + 4.0f, dy + 4.0f, 2.0f, cc);
                    String right = (entry.win ? "H\u1ea1 g\u1ee5c" : "B\u1ecb h\u1ea1") + " -" + entry.totems;
                    float rw = ClickGuiScreen.tw(right, 0.78f);
                    ClickGuiScreen.tx(graphics, right, x2 - pad - rw, ry, 0.78f, cc, false);
                    String nm = entry.name == null ? "?" : entry.name;
                    float maxW = inner - 9.0f - rw - 6.0f;
                    while (nm.length() > 2 && ClickGuiScreen.tw(nm, 0.78f) > maxW) {
                        nm = nm.substring(0, nm.length() - 1);
                    }
                    ClickGuiScreen.tx(graphics, nm, x1 + pad + 9.0f, ry, 0.78f, TXT_MAIN, false);
                }
            }
            LightningSkin.rrAll(graphics, x1 + pad, yFoot, x2 - pad, yFoot + footH, 5.0f, new Color(76, 54, 190, 240));
            LightningSkin.ring(graphics, x1 + pad, yFoot, x2 - pad, yFoot + footH, 5.0f, 1.0f, new Color(150, 125, 255, 110));
            String ft = "\u0110\u00e3 l\u01b0u v\u00e0o module FightLog";
            float fs = 0.78f;
            float maxFt = inner - 12.0f;
            if (ClickGuiScreen.tw(ft, fs) > maxFt) {
                fs = maxFt / (float)Night.FONT_MANAGER.getWidth(ft);
            }
            ClickGuiScreen.tx(graphics, ft, x1 + (w - ClickGuiScreen.tw(ft, fs)) / 2.0f, yFoot + (footH - ClickGuiScreen.th(fs)) / 2.0f, fs, Color.WHITE, false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void renderSettingsButton(GuiGraphicsExtractor graphics, int mx, int my) {
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
            LightningSkin.rrAll(graphics, x1, y1, x2, y2, 7.0f, hover ? new Color(34, 32, 54, 244) : CARD_BG);
            LightningSkin.ring(graphics, x1, y1, x2, y2, 7.0f, 1.0f, CARD_RING);
            float ty = y1 + (h - fhB) / 2.0f;
            ClickGuiScreen.tx(graphics, icon, x1 + 10.0f, ty, 0.78f, PURPLE_LT, false);
            ClickGuiScreen.tx(graphics, label, x1 + 10.0f + ClickGuiScreen.tw(icon, 0.78f) + 7.0f, ty, 0.78f, Color.WHITE, false);
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

    private void renderInfoCard(GuiGraphicsExtractor graphics) {
        try {
            if (this.frames.isEmpty()) {
                return;
            }
            Frame core = this.frames.get(this.frames.size() - 1);
            Minecraft mc = Minecraft.getInstance();
            float fhB = ClickGuiScreen.th(0.78f);
            String name = "LightningClient";
            String server = "Singleplayer";
            String ping = "--";
            int pingVal = -1;
            try {
                Object ipVal;
                Object sd = mc.getCurrentServer();
                if (sd != null && (ipVal = sd.getClass().getField("ip").get(sd)) != null) {
                    server = String.valueOf(ipVal);
                }
            }
            catch (Throwable ignored) {
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
            catch (Throwable ignored) {
                // empty catch block
            }
            try {
                int pg;
                String svo = Night.SERVER_MANAGER.getServer();
                String sv = svo == null ? null : String.valueOf(svo);
                if (sv != null && !sv.isEmpty()) {
                    server = sv;
                }
                if ((pg = Night.SERVER_MANAGER.getPing()) > 0) {
                    pingVal = pg;
                    ping = pg + "ms";
                }
            }
            catch (Throwable ignored) {
                // empty catch block
            }
            int enabled = 0;
            for (Module.Category cat : Module.Category.values()) {
                for (Module module : Night.MODULE_MANAGER.getModules(cat)) {
                    if (!module.isToggled()) continue;
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
            LightningSkin.rrAll(graphics, x1, y1, x2, y2, 8.0f, CARD_BG);
            LightningSkin.ring(graphics, x1, y1, x2, y2, 8.0f, 1.0f, CARD_RING);
            float ix1 = x1 + pad;
            float iy1 = y1 + 9.0f;
            try {
                LightningSkin.bolt(graphics, ix1, iy1, iconS, 253);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            float tX = ix1 + iconS * 0.561f + 8.0f;
            float nameSc = 0.9f;
            float blockTop = iy1 + (iconS - (ClickGuiScreen.th(nameSc) + 2.0f + fhB)) / 2.0f;
            ClickGuiScreen.tx(graphics, name, tX, blockTop, nameSc, Color.WHITE, false);
            String srv = server;
            float maxW = x2 - 6.0f - tX;
            while (srv.length() > 3 && ClickGuiScreen.tw(srv, 0.78f) > maxW) {
                srv = srv.substring(0, srv.length() - 1);
            }
            ClickGuiScreen.tx(graphics, srv, tX, blockTop + ClickGuiScreen.th(nameSc) + 2.0f, 0.78f, grey, false);
            float ry = iy1 + iconS + 9.0f;
            Color pc = pingVal < 0 ? grey : (pingVal <= 80 ? new Color(60, 235, 150) : (pingVal <= 160 ? new Color(240, 200, 70) : new Color(240, 90, 90)));
            ClickGuiScreen.tx(graphics, "Ping", x1 + pad, ry, 0.78f, grey, false);
            ClickGuiScreen.tx(graphics, ping, x2 - pad - ClickGuiScreen.tw(ping, 0.78f), ry, 0.78f, pc, false);
            float ry2 = ry + rowH;
            ClickGuiScreen.tx(graphics, "B\u1eadt", x1 + pad, ry2, 0.78f, grey, false);
            String en = String.valueOf(enabled);
            ClickGuiScreen.tx(graphics, en, x2 - pad - ClickGuiScreen.tw(en, 0.78f), ry2, 0.78f, Color.WHITE, false);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        double scaledX = mouseButtonEvent.x() / (double)this.appliedScale;
        double scaledY = mouseButtonEvent.y() / (double)this.appliedScale;
        int button = mouseButtonEvent.button();
        for (Frame frame : this.frames) {
            frame.mouseDragged(scaledX, scaledY, button, deltaX / (double)this.appliedScale, deltaY / (double)this.appliedScale);
        }
        return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        double scaledX = mouseButtonEvent.x() / (double)this.appliedScale;
        double scaledY = mouseButtonEvent.y() / (double)this.appliedScale;
        int button = mouseButtonEvent.button();
        if (this.searchFrame.mouseClicked(scaledX, scaledY, button)) {
            return true;
        }
        if (button == 0 && this.stShown && scaledX >= (double)this.stX1 && scaledX <= (double)this.stX2 && scaledY >= (double)this.stY1 && scaledY <= (double)this.stY2) {
            this.onSettingsClicked();
            return true;
        }
        for (Frame frame : this.frames) {
            frame.mouseClicked(scaledX, scaledY, button);
        }
        this.descriptionFrame.mouseClicked(scaledX, scaledY, button);
        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        double scaledX = mouseButtonEvent.x() / (double)this.appliedScale;
        double scaledY = mouseButtonEvent.y() / (double)this.appliedScale;
        int button = mouseButtonEvent.button();
        for (Frame frame : this.frames) {
            frame.mouseReleased(scaledX, scaledY, button);
        }
        this.descriptionFrame.mouseReleased(scaledX, scaledY, button);
        return super.mouseReleased(mouseButtonEvent);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double scaledX = mouseX / (double)this.appliedScale;
        double scaledY = mouseY / (double)this.appliedScale;
        for (Frame frame : this.frames) {
            frame.mouseScrolled(scaledX, scaledY, horizontalAmount, verticalAmount);
        }
        return false;
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        int scanCode = keyEvent.scancode();
        int modifiers = keyEvent.modifiers();
        if (keyCode == 256 && !BindButton.isAnyListening()) {
            this.requestClose();
            return true;
        }
        if (keyCode == Night.MODULE_MANAGER.getModule(ClickGuiModule.class).getBind()) {
            if (this.closing) {
                this.cancelClose();
            }
            return true;
        }
        boolean ctrlHeld = InputConstants.isKeyDown((Window)Minecraft.getInstance().getWindow(), (int)(Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (ctrlHeld && keyCode == 70) {
            this.searchFrame.toggle();
            return true;
        }
        if (this.searchFrame.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        for (Frame frame : this.frames) {
            frame.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == 256) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char typedChar = (char)characterEvent.codepoint();
        int modifiers = 0;
        if (this.searchFrame.charTyped(typedChar, 0)) {
            return true;
        }
        for (Frame frame : this.frames) {
            frame.charTyped(typedChar, modifiers);
        }
        return super.charTyped(characterEvent);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (clickGuiModule.blur.getValue()) {
            guiGraphicsExtractor.blurBeforeThisStratum();
        }
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, sw, sh, new Color(8, 7, 14, 215), new Color(22, 15, 40, 205));
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, (float)sh * 0.55f, sw, sh, new Color(110, 60, 200, 0), new Color(120, 66, 220, 22));
        String bgName = clickGuiModule.background.getValue();
        int bgIndex = Arrays.asList(ClickGuiModule.BG_MODES).indexOf(bgName);
        if (bgIndex >= 0 && bgIndex < ClickGuiModule.BG_MODES.length - 1) {
            float bgSpeed = clickGuiModule.bgSpeed.getValue().floatValue();
            float bgOpacity = clickGuiModule.bgOpacity.getValue().floatValue() / 100.0f;
            Color accentColor = clickGuiModule.color.getColor();
            boolean rainbow = clickGuiModule.isRainbow();
            ClickGuiBackground.renderBackground(guiGraphicsExtractor, sw, sh, bgIndex, bgSpeed, bgOpacity, accentColor, rainbow);
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

    public static Color getButtonColor(int offset, int alpha) {
        Color color = Night.MODULE_MANAGER.getModule(ClickGuiModule.class).isRainbow() ? ColorUtils.getOffsetRainbow((long)offset * 2L) : ColorGui.accent(offset);
        return ColorUtils.getColor(color, alpha);
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

    public void setShowLine(boolean showLine) {
        this.showLine = showLine;
    }

    public void setColorClipboard(Color color) {
        this.colorClipboard = color;
    }

    public void setAppliedScale(float appliedScale) {
        this.appliedScale = appliedScale;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public void setReadyToRemove(boolean readyToRemove) {
        this.readyToRemove = readyToRemove;
    }
}

