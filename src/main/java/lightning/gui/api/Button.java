/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.sounds.SoundEvents
 */
package lightning.gui.api;

import lightning.Night;
import lightning.gui.api.Frame;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.settings.Setting;
import lightning.utils.IMinecraft;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;

public class Button
implements IMinecraft {
    private final Setting setting;
    private final Frame parent;
    private int x;
    private int y;
    private int height;
    private int padding = 2;
    private int textPadding = 5;
    private final String description;
    private boolean visible = true;

    public Button(Frame frame, int n, String string) {
        this.setting = null;
        this.parent = frame;
        this.height = n;
        this.description = string;
    }

    public Button(Setting setting, Frame frame, int n, String string) {
        this.setting = setting;
        this.parent = frame;
        this.height = n;
        this.description = string;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
    }

    public void mouseClicked(double d, double d2, int n) {
    }

    public void mouseReleased(double d, double d2, int n) {
    }

    public void keyPressed(int n, int n2, int n3) {
    }

    public void mouseScrolled(double d, double d2, double d3, double d4) {
    }

    public void mouseDragged(double d, double d2, int n, double d3, double d4) {
    }

    public void charTyped(char c, int n) {
    }

    public int getWidth() {
        return this.parent.getWidth();
    }

    public boolean isHovering(double d, double d2) {
        return (double)(this.x + this.padding) <= d && (double)this.y <= d2 && (double)(this.x + this.getWidth() - this.padding) > d && (double)(this.y + this.getHeight()) > d2;
    }

    public void playClickSound() {
        if (Night.MODULE_MANAGER.getModule(ClickGuiModule.class).sounds.getValue()) {
            mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        }
    }

    @Generated
    public Setting getSetting() {
        return this.setting;
    }

    @Generated
    public Frame getParent() {
        return this.parent;
    }

    @Generated
    public int getX() {
        return this.x;
    }

    @Generated
    public int getY() {
        return this.y;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public int getPadding() {
        return this.padding;
    }

    @Generated
    public int getTextPadding() {
        return this.textPadding;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public boolean isVisible() {
        return this.visible;
    }

    @Generated
    public void setX(int n) {
        this.x = n;
    }

    @Generated
    public void setY(int n) {
        this.y = n;
    }

    @Generated
    public void setHeight(int n) {
        this.height = n;
    }

    @Generated
    public void setPadding(int n) {
        this.padding = n;
    }

    @Generated
    public void setTextPadding(int n) {
        this.textPadding = n;
    }

    @Generated
    public void setVisible(boolean bl) {
        this.visible = bl;
    }
}

