/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package lightning.gui.api;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.gui.impl.ProxyModuleButton;
import lightning.modules.impl.core.PingBypassModule;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class PingBypassFrame
extends Frame {
    private static final String CATEGORY_NAME = "PingBypass";
    private Set<String> lastKnownModules = Collections.emptySet();

    public PingBypassFrame(int n, int n2, int n3, int n4) {
        super(null, n, n2, n3, n4);
    }

    private boolean isActive() {
        PingBypassModule pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        return pingBypassModule != null && pingBypassModule.isToggled() && !pingBypassModule.getProxyModuleStates().isEmpty();
    }

    private void refreshButtons() {
        PingBypassModule pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class);
        if (pingBypassModule == null) {
            return;
        }
        TreeSet<String> treeSet = new TreeSet<String>(pingBypassModule.getProxyModuleStates().keySet());
        if (treeSet.equals(this.lastKnownModules)) {
            return;
        }
        this.lastKnownModules = treeSet;
        this.getButtons().clear();
        for (String string : treeSet) {
            this.getButtons().add(new ProxyModuleButton(string, this, this.getHeight()));
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, String string) {
        if (!this.isActive()) {
            return;
        }
        this.refreshButtons();
        if (this.isDragging()) {
            this.setX(n - this.getDragX());
            this.setY(n2 - this.getDragY());
        }
        boolean bl = string != null && !string.isEmpty();
        int n3 = this.getHeight();
        if (this.isOpen()) {
            ++n3;
            for (Button object : this.getButtons()) {
                if (bl && object instanceof ProxyModuleButton) {
                    Button button = (ProxyModuleButton)object;
                    if (!((ProxyModuleButton)button).getModuleName().toLowerCase().contains(string.toLowerCase())) {
                        object.setVisible(false);
                        continue;
                    }
                    object.setVisible(true);
                } else {
                    object.setVisible(true);
                }
                if (!object.isVisible()) continue;
                object.setX(this.getX());
                object.setY(this.getY() + n3);
                n3 += object.getHeight();
            }
        }
        this.setTotalHeight(n3);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), new Color(21, 21, 21, 200));
        Color color = ClickGuiScreen.getButtonColor(this.getY(), 200);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth(), this.getY() + this.getHeight(), color);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, CATEGORY_NAME, this.getX() + this.getTextPadding(), this.getY() + 2, Color.WHITE);
        if (this.isOpen()) {
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), this.getY() + n3 + 1, new Color(16, 16, 16, 180));
            for (Button button : this.getButtons()) {
                if (!button.isVisible()) continue;
                button.render(guiGraphicsExtractor, n, n2, f);
            }
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        this.render(guiGraphicsExtractor, n, n2, f, "");
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (!this.isActive()) {
            return;
        }
        if (this.isHovering(d, d2)) {
            if (n == 0) {
                this.setDragging(true);
                this.setDragX((int)(d - (double)this.getX()));
                this.setDragY((int)(d2 - (double)this.getY()));
            } else if (n == 1) {
                this.setOpen(!this.isOpen());
            }
        }
        if (this.isOpen()) {
            for (Button button : this.getButtons()) {
                if (!button.isVisible()) continue;
                button.mouseClicked(d, d2, n);
            }
        }
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
        if (!this.isActive()) {
            return;
        }
        if (n == 0) {
            this.setDragging(false);
        }
        for (Button button : this.getButtons()) {
            if (!button.isVisible()) continue;
            button.mouseReleased(d, d2, n);
        }
    }

    @Override
    public void mouseScrolled(double d, double d2, double d3, double d4) {
        if (!this.isActive()) {
            return;
        }
        super.mouseScrolled(d, d2, d3, d4);
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (!this.isActive()) {
            return;
        }
        if (this.isOpen()) {
            for (Button button : this.getButtons()) {
                if (!button.isVisible()) continue;
                button.keyPressed(n, n2, n3);
            }
        }
    }

    @Override
    public void charTyped(char c, int n) {
        if (!this.isActive()) {
            return;
        }
        if (this.isOpen()) {
            for (Button button : this.getButtons()) {
                if (!button.isVisible()) continue;
                button.charTyped(c, n);
            }
        }
    }
}

