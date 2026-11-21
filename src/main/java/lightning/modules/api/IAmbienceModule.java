/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.api;

import java.awt.Color;

public interface IAmbienceModule {
    public boolean isToggled();

    public Color getColor();

    public boolean isLightmapEnabled();

    public boolean isSkyEnabled();

    public boolean isBlocksEnabled();
}

