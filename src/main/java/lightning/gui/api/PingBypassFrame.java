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

    public PingBypassFrame(int x, int y, int width, int height) {
        super(null, x, y, width, height);
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
        TreeSet<String> sortedModules = new TreeSet<String>(pingBypassModule.getProxyModuleStates().keySet());
        if (sortedModules.equals(this.lastKnownModules)) {
            return;
        }
        this.lastKnownModules = sortedModules;
        this.getButtons().clear();
        for (String moduleName : sortedModules) {
            this.getButtons().add(new ProxyModuleButton(moduleName, this, this.getHeight()));
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks, String searchQuery) {
        if (!this.isActive()) {
            return;
        }
        this.refreshButtons();
        if (this.isDragging()) {
            this.setX(mouseX - this.getDragX());
            this.setY(mouseY - this.getDragY());
        }
        boolean hasSearch = searchQuery != null && !searchQuery.isEmpty();
        int totalHeight = this.getHeight();
        if (this.isOpen()) {
            ++totalHeight;
            for (Button button : this.getButtons()) {
                if (hasSearch && button instanceof ProxyModuleButton) {
                    ProxyModuleButton proxyButton = (ProxyModuleButton)button;
                    if (!proxyButton.getModuleName().toLowerCase().contains(searchQuery.toLowerCase())) {
                        button.setVisible(false);
                        continue;
                    }
                    button.setVisible(true);
                } else {
                    button.setVisible(true);
                }
                if (!button.isVisible()) continue;
                button.setX(this.getX());
                button.setY(this.getY() + totalHeight);
                totalHeight += button.getHeight();
            }
        }
        this.setTotalHeight(totalHeight);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), new Color(21, 21, 21, 200));
        Color color = ClickGuiScreen.getButtonColor(this.getY(), 200);
        Renderer2D.renderQuad(guiGraphicsExtractor, this.getX(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth(), this.getY() + this.getHeight(), color);
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, CATEGORY_NAME, this.getX() + this.getTextPadding(), this.getY() + 2, Color.WHITE);
        if (this.isOpen()) {
            Renderer2D.renderQuad(guiGraphicsExtractor, this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), this.getY() + totalHeight + 1, new Color(16, 16, 16, 180));
            for (Button button : this.getButtons()) {
                if (!button.isVisible()) continue;
                button.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        this.render(guiGraphicsExtractor, mouseX, mouseY, partialTicks, "");
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isActive()) {
            return;
        }
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.setDragging(true);
                this.setDragX((int)(mouseX - (double)this.getX()));
                this.setDragY((int)(mouseY - (double)this.getY()));
            } else if (button == 1) {
                this.setOpen(!this.isOpen());
            }
        }
        if (this.isOpen()) {
            for (Button child : this.getButtons()) {
                if (!child.isVisible()) continue;
                child.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!this.isActive()) {
            return;
        }
        if (button == 0) {
            this.setDragging(false);
        }
        for (Button child : this.getButtons()) {
            if (!child.isVisible()) continue;
            child.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.isActive()) {
            return;
        }
        super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isActive()) {
            return;
        }
        if (this.isOpen()) {
            for (Button child : this.getButtons()) {
                if (!child.isVisible()) continue;
                child.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char ch, int modifiers) {
        if (!this.isActive()) {
            return;
        }
        if (this.isOpen()) {
            for (Button child : this.getButtons()) {
                if (!child.isVisible()) continue;
                child.charTyped(ch, modifiers);
            }
        }
    }
}

