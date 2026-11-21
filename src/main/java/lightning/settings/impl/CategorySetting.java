/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.settings.impl;

import lightning.settings.Setting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lombok.Generated;

public class CategorySetting
extends Setting {
    private boolean open = false;
    private final Animation openAnim = new Animation(200, Easing.Method.EASE_OUT_CUBIC);

    public CategorySetting(String string, String string2) {
        super(string, string, string2, new Setting.Visibility());
    }

    public CategorySetting(String string, String string2, String string3) {
        super(string, string2, string3, new Setting.Visibility());
    }

    public CategorySetting(String string, String string2, Setting.Visibility visibility) {
        super(string, string, string2, visibility);
    }

    public CategorySetting(String string, String string2, String string3, Setting.Visibility visibility) {
        super(string, string2, string3, visibility);
    }

    public float getOpenAmount() {
        return this.openAnim.get(this.open ? 1.0f : 0.0f);
    }

    @Generated
    public boolean isOpen() {
        return this.open;
    }

    @Generated
    public Animation getOpenAnim() {
        return this.openAnim;
    }

    @Generated
    public void setOpen(boolean bl) {
        this.open = bl;
    }

    public static class Visibility
    extends Setting.Visibility {
        private final CategorySetting value;

        public Visibility(CategorySetting categorySetting) {
            super(categorySetting);
            this.value = categorySetting;
        }

        public CategorySetting getValue() {
            return this.value;
        }

        @Override
        public void update() {
            if (this.value.getVisibility() != null) {
                this.value.getVisibility().update();
                if (!this.value.getVisibility().isVisible()) {
                    this.setVisible(false);
                    return;
                }
            }
            this.setVisible(this.value.getOpenAmount() > 0.001f);
        }
    }
}

