/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.utils.graphics.Glint$GlintParams
 */
package lightning.utils.mixins;

import lightning.utils.graphics.Glint;

public interface IChamsCapture {
    public boolean night$chamsFill();

    public int night$chamsFillColor();

    public boolean night$chamsOutline();

    public int night$chamsOutlineColor();

    public boolean night$chamsShine();

    public boolean night$chamsGlint();

    public int night$chamsGlintColor();

    public Glint.GlintParams night$chamsGlintParams();

    public boolean night$chamsSuppressReal();

    public float night$chamsRealAlpha();

    public void night$setChamsRealAlpha(float alpha);

    public float night$chamsYOffset();

    public void night$setChamsYOffset(float yOffset);

    public void night$setChams(boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine);

    public void night$setChams(boolean fill, int fillColor, boolean outline, int outlineColor, boolean shine, boolean suppressReal);

    public void night$setChamsGlint(boolean glint, int glintColor);

    public void night$setChamsGlint(boolean glint, int glintColor, Glint.GlintParams glintParams);
}

