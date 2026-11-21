/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.settings;

import lombok.Generated;

public class Setting {
    private final String name;
    private final String tag;
    private final String description;
    private final Visibility visibility;

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getTag() {
        return this.tag;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Generated
    public Setting(String string, String string2, String string3, Visibility visibility) {
        this.name = string;
        this.tag = string2;
        this.description = string3;
        this.visibility = visibility;
    }

    public static class Visibility {
        private final Setting setting;
        private boolean visible = true;

        public Visibility() {
            this.setting = null;
        }

        public void update() {
        }

        @Generated
        public Setting getSetting() {
            return this.setting;
        }

        @Generated
        public boolean isVisible() {
            return this.visible;
        }

        @Generated
        public void setVisible(boolean bl) {
            this.visible = bl;
        }

        @Generated
        public Visibility(Setting setting) {
            this.setting = setting;
        }
    }
}

